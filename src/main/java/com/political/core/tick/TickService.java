package com.political.core.tick;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * One named unit of per-tick work: what runs, how often, and how long it is allowed to take.
 *
 * <p>The type parameters keep the dispatcher independent of Minecraft so it can be tested with plain
 * objects: {@code S} is the server, {@code W} a world and {@code P} a player.</p>
 *
 * @param name          unique, short and stable; it appears in logs and statistics
 * @param scope         whether the work runs once, per world or per player
 * @param intervalTicks runs every this many dispatcher ticks, 1 means every tick; the first run is
 *                      after {@code intervalTicks} ticks, like the counters this replaces
 * @param budgetMillis  total time all runs of this service may take within one tick before the
 *                      dispatcher logs it; the work is never cut short
 * @param serverTask    the work for {@link TickScope#SERVER}, otherwise null
 * @param worldTask     the work for {@link TickScope#WORLD}, otherwise null
 * @param playerTask    the work for {@link TickScope#PLAYER}, otherwise null
 */
public record TickService<S, W, P>(
        String name,
        TickScope scope,
        int intervalTicks,
        double budgetMillis,
        Consumer<S> serverTask,
        Consumer<W> worldTask,
        Consumer<P> playerTask) {

    public TickService {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(scope, "scope");
        if (name.isBlank()) {
            throw new IllegalArgumentException("a tick service needs a name");
        }
        if (intervalTicks < 1) {
            throw new IllegalArgumentException("interval of " + name + " must be at least 1 tick, got " + intervalTicks);
        }
        if (!(budgetMillis > 0.0)) {
            throw new IllegalArgumentException("budget of " + name + " must be positive, got " + budgetMillis);
        }
        boolean ok = switch (scope) {
            case SERVER -> serverTask != null && worldTask == null && playerTask == null;
            case WORLD -> worldTask != null && serverTask == null && playerTask == null;
            case PLAYER -> playerTask != null && serverTask == null && worldTask == null;
        };
        if (!ok) {
            throw new IllegalArgumentException("service " + name + " must give exactly the task that matches scope " + scope);
        }
    }

    public static <S, W, P> TickService<S, W, P> server(String name, int intervalTicks, double budgetMillis, Consumer<S> task) {
        return new TickService<>(name, TickScope.SERVER, intervalTicks, budgetMillis, task, null, null);
    }

    public static <S, W, P> TickService<S, W, P> world(String name, int intervalTicks, double budgetMillis, Consumer<W> task) {
        return new TickService<>(name, TickScope.WORLD, intervalTicks, budgetMillis, null, task, null);
    }

    public static <S, W, P> TickService<S, W, P> player(String name, int intervalTicks, double budgetMillis, Consumer<P> task) {
        return new TickService<>(name, TickScope.PLAYER, intervalTicks, budgetMillis, null, null, task);
    }
}
