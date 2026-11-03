package com.political.core.tick;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.LongSupplier;

/**
 * Runs the registered {@link TickService}s once per server tick, in registration order.
 *
 * <ul>
 *   <li>A service that throws is reported and the tick carries on with the next one. Failures of the
 *       same service are reported at most once per {@link #REPORT_THROTTLE_TICKS} ticks, with the number
 *       that were held back, so one broken service cannot flood the log.</li>
 *   <li>A service that uses more than its budget in a tick is reported the same way. Nothing is cut
 *       short; the report is the signal to look at it.</li>
 *   <li>Order is preserved. {@code WORLD} and {@code PLAYER} services that sit next to each other form
 *       one block that runs world by world: for each world its {@code WORLD} services, then for each
 *       player of that world its {@code PLAYER} services. A {@code SERVER} service ends a block.</li>
 *   <li>{@code Error}s other than stack overflows are rethrown: an out of memory server should stop,
 *       not limp on.</li>
 * </ul>
 *
 * @param <S> server type
 * @param <W> world type
 * @param <P> player type
 */
public final class TickDispatcher<S, W, P> {

    /** A repeating failure or overrun is reported at most this often per service (one minute at 20 TPS). */
    public static final int REPORT_THROTTLE_TICKS = 1200;

    private static final long LONG_AGO = Long.MIN_VALUE / 2;

    private final Function<S, Iterable<W>> worlds;
    private final Function<W, Iterable<P>> players;
    private final LongSupplier nanoClock;
    private final TickReporter reporter;
    private final List<Entry> entries = new ArrayList<>();
    private final Set<String> names = new HashSet<>();
    private long dispatcherTick;

    public TickDispatcher(Function<S, Iterable<W>> worlds, Function<W, Iterable<P>> players,
                          LongSupplier nanoClock, TickReporter reporter) {
        this.worlds = worlds;
        this.players = players;
        this.nanoClock = nanoClock;
        this.reporter = reporter;
    }

    /** Adds a service after the ones already registered. Names must be unique and no tick may have run yet. */
    public void register(TickService<S, W, P> service) {
        if (dispatcherTick > 0) {
            throw new IllegalStateException("tick services must be registered before the first tick: " + service.name());
        }
        if (!names.add(service.name())) {
            throw new IllegalArgumentException("duplicate tick service name: " + service.name());
        }
        entries.add(new Entry(service));
    }

    public int serviceCount() {
        return entries.size();
    }

    /** Runs every service that is due this tick. Call once per server tick. */
    public void tick(S server) {
        dispatcherTick++;
        int n = entries.size();
        boolean[] due = new boolean[n];
        for (int i = 0; i < n; i++) {
            Entry e = entries.get(i);
            e.sinceRun++;
            if (e.sinceRun >= e.service.intervalTicks()) {
                due[i] = true;
                e.sinceRun = 0;
            }
        }
        int i = 0;
        while (i < n) {
            Entry e = entries.get(i);
            if (e.service.scope() == TickScope.SERVER) {
                if (due[i]) {
                    runServerService(server, e);
                }
                i++;
            } else {
                int end = i;
                while (end < n && entries.get(end).service.scope() != TickScope.SERVER) {
                    end++;
                }
                runBlock(server, i, end, due);
                i = end;
            }
        }
    }

    private void runServerService(S server, Entry e) {
        long start = nanoClock.getAsLong();
        try {
            e.service.serverTask().accept(server);
        } catch (Throwable t) {
            if (t instanceof Error err && !(t instanceof StackOverflowError)) {
                throw err;
            }
            fail(e, "", t);
        }
        e.tickNanos += nanoClock.getAsLong() - start;
        finish(e);
    }

    private void runBlock(S server, int from, int to, boolean[] due) {
        boolean anyWorld = false;
        boolean anyPlayer = false;
        for (int k = from; k < to; k++) {
            if (due[k]) {
                if (entries.get(k).service.scope() == TickScope.WORLD) {
                    anyWorld = true;
                } else {
                    anyPlayer = true;
                }
            }
        }
        if (!anyWorld && !anyPlayer) {
            return;
        }
        try {
            for (W world : snapshot(worlds.apply(server))) {
                for (int k = from; k < to; k++) {
                    Entry e = entries.get(k);
                    if (due[k] && e.service.scope() == TickScope.WORLD) {
                        runWorldService(e, world);
                    }
                }
                if (anyPlayer) {
                    for (P player : snapshot(players.apply(world))) {
                        for (int k = from; k < to; k++) {
                            Entry e = entries.get(k);
                            if (due[k] && e.service.scope() == TickScope.PLAYER) {
                                runPlayerService(e, player);
                            }
                        }
                    }
                }
            }
        } catch (Throwable t) {
            if (t instanceof Error err && !(t instanceof StackOverflowError)) {
                throw err;
            }
            // Listing worlds or players failed; report it against the first due service of the block.
            for (int k = from; k < to; k++) {
                if (due[k]) {
                    fail(entries.get(k), " while listing worlds or players", t);
                    break;
                }
            }
        }
        for (int k = from; k < to; k++) {
            if (due[k]) {
                finish(entries.get(k));
            }
        }
    }

    private void runWorldService(Entry e, W world) {
        long start = nanoClock.getAsLong();
        try {
            e.service.worldTask().accept(world);
        } catch (Throwable t) {
            if (t instanceof Error err && !(t instanceof StackOverflowError)) {
                throw err;
            }
            fail(e, " in a world", t);
        }
        e.tickNanos += nanoClock.getAsLong() - start;
    }

    private void runPlayerService(Entry e, P player) {
        long start = nanoClock.getAsLong();
        try {
            e.service.playerTask().accept(player);
        } catch (Throwable t) {
            if (t instanceof Error err && !(t instanceof StackOverflowError)) {
                throw err;
            }
            fail(e, " for a player", t);
        }
        e.tickNanos += nanoClock.getAsLong() - start;
    }

    private static <T> List<T> snapshot(Iterable<T> source) {
        List<T> copy = new ArrayList<>();
        for (T item : source) {
            copy.add(item);
        }
        return copy;
    }

    private void fail(Entry e, String where, Throwable t) {
        e.failures++;
        if (dispatcherTick - e.lastFailureReport >= REPORT_THROTTLE_TICKS) {
            reporter.failed(e.service.name(), where, t, e.suppressedFailures);
            e.suppressedFailures = 0;
            e.lastFailureReport = dispatcherTick;
        } else {
            e.suppressedFailures++;
        }
    }

    private void finish(Entry e) {
        e.runs++;
        e.totalNanos += e.tickNanos;
        e.maxNanos = Math.max(e.maxNanos, e.tickNanos);
        double millis = e.tickNanos / 1_000_000.0;
        e.tickNanos = 0;
        if (millis > e.service.budgetMillis()) {
            e.overBudget++;
            if (dispatcherTick - e.lastBudgetReport >= REPORT_THROTTLE_TICKS) {
                reporter.overBudget(e.service.name(), millis, e.service.budgetMillis(), e.suppressedOverruns);
                e.suppressedOverruns = 0;
                e.lastBudgetReport = dispatcherTick;
            } else {
                e.suppressedOverruns++;
            }
        }
    }

    /** Per-service numbers since the server started, in registration order. */
    public List<ServiceStats> stats() {
        List<ServiceStats> out = new ArrayList<>(entries.size());
        for (Entry e : entries) {
            out.add(new ServiceStats(e.service.name(), e.service.scope(), e.service.intervalTicks(),
                    e.service.budgetMillis(), e.runs, e.failures, e.overBudget,
                    e.totalNanos / 1_000_000.0, e.maxNanos / 1_000_000.0));
        }
        return out;
    }

    /** One line per service, most expensive first, for a shutdown log or a diagnostics command. */
    public List<String> summaryLines(int limit) {
        List<ServiceStats> sorted = new ArrayList<>(stats());
        sorted.sort(Comparator.comparingDouble(ServiceStats::totalMillis).reversed());
        List<String> lines = new ArrayList<>();
        for (ServiceStats s : sorted.subList(0, Math.min(limit, sorted.size()))) {
            lines.add(String.format("%s [%s every %d]: %d runs, avg %.3f ms, max %.3f ms, %d over budget, %d failures",
                    s.name(), s.scope(), s.intervalTicks(), s.runs(), s.averageMillis(), s.maxMillis(), s.overBudget(), s.failures()));
        }
        return lines;
    }

    /** What one service has done so far. */
    public record ServiceStats(String name, TickScope scope, int intervalTicks, double budgetMillis,
                               long runs, long failures, long overBudget, double totalMillis, double maxMillis) {
        public double averageMillis() {
            return runs == 0 ? 0.0 : totalMillis / runs;
        }
    }

    private final class Entry {
        final TickService<S, W, P> service;
        int sinceRun;
        long runs;
        long failures;
        long overBudget;
        long totalNanos;
        long maxNanos;
        long tickNanos;
        long lastFailureReport = LONG_AGO;
        long suppressedFailures;
        long lastBudgetReport = LONG_AGO;
        long suppressedOverruns;

        Entry(TickService<S, W, P> service) {
            this.service = service;
        }
    }
}
