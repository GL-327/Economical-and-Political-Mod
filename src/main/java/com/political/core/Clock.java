package com.political.core;

import com.political.PoliticalServer;
import net.minecraft.server.MinecraftServer;

/**
 * The one place gameplay code asks what time it is.
 *
 * <p>Game ticks are the right clock for anything that happens in play: a lagging or paused server
 * stretches wall-clock time but not game time, so cooldowns and effect windows keep their meaning.
 * {@link #gameMillis()} is the tick count scaled to milliseconds, so existing arithmetic written in
 * milliseconds moves over without changing a single duration.</p>
 *
 * <p>Use {@link #gameMillis()} or {@link #ticks()} only for state that lives in memory. The tick
 * counter restarts at 0 with the server, so a value that is saved to disk and read back after a
 * restart would be meaningless. Saved deadlines and real-time schedules (daily rewards, weekly
 * terms) use {@link #wallMillis()} until their owning system is migrated with a saved-format
 * version; the list of what still reads the wall clock lives in
 * {@code src/test/resources/wallclock-allowlist.txt} and only ever shrinks.</p>
 *
 * <p>Tests replace the source with {@link #useSource(Source)} and put it back with {@link #reset()}.</p>
 */
public final class Clock {

    /** One server tick is 50 ms of game time. */
    public static final long MS_PER_TICK = 50L;

    /** Where time comes from. The default reads the running server. */
    public interface Source {
        /** Server ticks since the server process started. */
        long ticks();

        /** Wall-clock milliseconds since the epoch. */
        long wallMillis();
    }

    private static final Source SERVER = new Source() {
        @Override
        public long ticks() {
            MinecraftServer server = PoliticalServer.server;
            return server == null ? 0L : server.getTickCount();
        }

        @Override
        public long wallMillis() {
            return System.currentTimeMillis();
        }
    };

    private static volatile Source source = SERVER;

    private Clock() {
    }

    /** Server ticks since this server process started; 0 before the server exists. */
    public static long ticks() {
        return source.ticks();
    }

    /** {@link #ticks()} in milliseconds of game time. In-memory timers only, see the class comment. */
    public static long gameMillis() {
        return source.ticks() * MS_PER_TICK;
    }

    /** Real time, for display, log lines, IDs and schedules that are wall-clock by design. */
    public static long wallMillis() {
        return source.wallMillis();
    }

    /** Test hook: replaces the time source until {@link #reset()}. */
    public static void useSource(Source replacement) {
        source = replacement == null ? SERVER : replacement;
    }

    /** Test hook: back to the running server. */
    public static void reset() {
        source = SERVER;
    }
}
