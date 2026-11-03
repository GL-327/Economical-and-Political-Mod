package com.political.core.tick;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TickDispatcherTest {

    /** Stand-ins for the server, a world and a player so no Minecraft class is needed. */
    private static final class World {
        final String name;
        final List<String> players = new ArrayList<>();

        World(String name, String... players) {
            this.name = name;
            this.players.addAll(List.of(players));
        }
    }

    private static final class Server {
        final List<World> worlds = new ArrayList<>();
    }

    private static final class RecordingReporter implements TickReporter {
        final List<String> failures = new ArrayList<>();
        final List<Long> failureSuppressed = new ArrayList<>();
        final List<String> overruns = new ArrayList<>();
        final List<Long> overrunSuppressed = new ArrayList<>();

        @Override
        public void failed(String service, String where, Throwable error, long suppressed) {
            failures.add(service + where + ": " + error.getMessage());
            failureSuppressed.add(suppressed);
        }

        @Override
        public void overBudget(String service, double tookMillis, double budgetMillis, long suppressed) {
            overruns.add(service + " " + tookMillis + ">" + budgetMillis);
            overrunSuppressed.add(suppressed);
        }
    }

    private final long[] nanos = {0L};
    private final RecordingReporter reporter = new RecordingReporter();
    private final Server server = new Server();
    private TickDispatcher<Server, World, String> dispatcher;
    private final List<String> log = new ArrayList<>();

    @BeforeEach
    void setUp() {
        dispatcher = new TickDispatcher<>(s -> s.worlds, w -> w.players, () -> nanos[0], reporter);
    }

    private TickService<Server, World, String> serverService(String name, int interval, Runnable body) {
        return TickService.server(name, interval, 5.0, s -> body.run());
    }

    @Test
    void servicesRunInRegistrationOrder() {
        dispatcher.register(serverService("a", 1, () -> log.add("a")));
        dispatcher.register(serverService("b", 1, () -> log.add("b")));
        dispatcher.register(serverService("c", 1, () -> log.add("c")));
        dispatcher.tick(server);
        dispatcher.tick(server);
        assertEquals(List.of("a", "b", "c", "a", "b", "c"), log);
    }

    @Test
    void intervalRunsEveryNthTickStartingAfterNTicks() {
        dispatcher.register(serverService("every3", 3, () -> log.add("x")));
        for (int i = 0; i < 7; i++) {
            dispatcher.tick(server);
        }
        assertEquals(2, log.size(), "ticks 3 and 6 of 7");
        assertEquals(2L, dispatcher.stats().get(0).runs());
    }

    @Test
    void oneServiceThrowingDoesNotStopTheOthers() {
        dispatcher.register(serverService("first", 1, () -> log.add("first")));
        dispatcher.register(serverService("broken", 1, () -> {
            throw new IllegalStateException("boom");
        }));
        dispatcher.register(serverService("last", 1, () -> log.add("last")));
        dispatcher.tick(server);
        assertEquals(List.of("first", "last"), log);
        assertEquals(List.of("broken: boom"), reporter.failures);
        assertEquals(1L, dispatcher.stats().get(1).failures());
    }

    @Test
    void repeatedFailuresAreThrottledAndCounted() {
        dispatcher.register(serverService("broken", 1, () -> {
            throw new IllegalStateException("again");
        }));
        for (int i = 0; i < TickDispatcher.REPORT_THROTTLE_TICKS + 1; i++) {
            dispatcher.tick(server);
        }
        // Reported on tick 1 and again on tick 1201; the 1199 failures in between were held back.
        assertEquals(2, reporter.failures.size());
        assertEquals(0L, reporter.failureSuppressed.get(0));
        assertEquals((long) TickDispatcher.REPORT_THROTTLE_TICKS - 1, reporter.failureSuppressed.get(1));
        assertEquals((long) TickDispatcher.REPORT_THROTTLE_TICKS + 1, dispatcher.stats().get(0).failures());
    }

    @Test
    void overBudgetServiceIsReportedButStillCounts() {
        dispatcher.register(TickService.server("slow", 1, 1.0, (Server s) -> nanos[0] += 2_500_000L));
        dispatcher.register(TickService.server("fast", 1, 1.0, (Server s) -> nanos[0] += 100_000L));
        dispatcher.tick(server);
        assertEquals(1, reporter.overruns.size());
        assertTrue(reporter.overruns.get(0).startsWith("slow 2.5>1.0"), reporter.overruns.get(0));
        TickDispatcher.ServiceStats slow = dispatcher.stats().get(0);
        assertEquals(1L, slow.overBudget());
        assertEquals(2.5, slow.maxMillis(), 1e-9);
        assertEquals(0L, dispatcher.stats().get(1).overBudget());
    }

    @Test
    void worldAndPlayerServicesRunWorldByWorld() {
        server.worlds.add(new World("overworld", "alice", "bob"));
        server.worlds.add(new World("nether", "carol"));
        dispatcher.register(TickService.world("w1", 1, 5.0, (World w) -> log.add("w1:" + w.name)));
        dispatcher.register(TickService.world("w2", 1, 5.0, (World w) -> log.add("w2:" + w.name)));
        dispatcher.register(TickService.player("p1", 1, 5.0, (String p) -> log.add("p1:" + p)));
        dispatcher.register(TickService.player("p2", 1, 5.0, (String p) -> log.add("p2:" + p)));
        dispatcher.tick(server);
        assertEquals(List.of(
                "w1:overworld", "w2:overworld", "p1:alice", "p2:alice", "p1:bob", "p2:bob",
                "w1:nether", "w2:nether", "p1:carol", "p2:carol"), log);
    }

    @Test
    void aServerServiceEndsAWorldBlock() {
        server.worlds.add(new World("overworld", "alice"));
        server.worlds.add(new World("nether"));
        dispatcher.register(TickService.world("before", 1, 5.0, (World w) -> log.add("before:" + w.name)));
        dispatcher.register(serverService("between", 1, () -> log.add("between")));
        dispatcher.register(TickService.world("after", 1, 5.0, (World w) -> log.add("after:" + w.name)));
        dispatcher.tick(server);
        assertEquals(List.of("before:overworld", "before:nether", "between", "after:overworld", "after:nether"), log);
    }

    @Test
    void failureForOnePlayerDoesNotSkipTheRest() {
        server.worlds.add(new World("overworld", "alice", "bob"));
        dispatcher.register(TickService.player("p", 1, 5.0, (String p) -> {
            if (p.equals("alice")) {
                throw new IllegalArgumentException("bad player");
            }
            log.add(p);
        }));
        dispatcher.tick(server);
        assertEquals(List.of("bob"), log);
        assertEquals(List.of("p for a player: bad player"), reporter.failures);
    }

    @Test
    void playersMayLeaveTheWorldWhileItIsBeingTicked() {
        World world = new World("overworld", "alice", "bob", "carol");
        server.worlds.add(world);
        dispatcher.register(TickService.player("kick", 1, 5.0, (String p) -> {
            log.add(p);
            world.players.remove("bob");
        }));
        dispatcher.tick(server);
        assertEquals(List.of("alice", "bob", "carol"), log, "the list is copied before it is walked");
        assertTrue(reporter.failures.isEmpty());
    }

    @Test
    void anErrorLikeOutOfMemoryIsNotSwallowed() {
        dispatcher.register(serverService("fatal", 1, () -> {
            throw new OutOfMemoryError("heap");
        }));
        OutOfMemoryError thrown = assertThrows(OutOfMemoryError.class, () -> dispatcher.tick(server));
        assertEquals("heap", thrown.getMessage());
        assertTrue(reporter.failures.isEmpty());
    }

    @Test
    void duplicateNamesAndLateRegistrationAreRejected() {
        dispatcher.register(serverService("same", 1, () -> { }));
        assertThrows(IllegalArgumentException.class, () -> dispatcher.register(serverService("same", 2, () -> { })));
        dispatcher.tick(server);
        assertThrows(IllegalStateException.class, () -> dispatcher.register(serverService("late", 1, () -> { })));
    }

    @Test
    void definitionsAreValidated() {
        assertThrows(IllegalArgumentException.class, () -> TickService.server(" ", 1, 1.0, (Server s) -> { }));
        assertThrows(IllegalArgumentException.class, () -> TickService.server("x", 0, 1.0, (Server s) -> { }));
        assertThrows(IllegalArgumentException.class, () -> TickService.server("x", 1, 0.0, (Server s) -> { }));
        assertThrows(IllegalArgumentException.class, () -> new TickService<Server, World, String>(
                "x", TickScope.WORLD, 1, 1.0, s -> { }, null, null));
    }

    @Test
    void summaryListsTheMostExpensiveFirst() {
        dispatcher.register(TickService.server("cheap", 1, 50.0, (Server s) -> nanos[0] += 1_000_000L));
        dispatcher.register(TickService.server("costly", 1, 50.0, (Server s) -> nanos[0] += 9_000_000L));
        dispatcher.tick(server);
        List<String> lines = dispatcher.summaryLines(1);
        assertEquals(1, lines.size());
        assertTrue(lines.get(0).startsWith("costly [SERVER every 1]: 1 runs, avg 9.000 ms"), lines.get(0));
        assertSame(dispatcher.stats().get(0).scope(), TickScope.SERVER);
    }
}
