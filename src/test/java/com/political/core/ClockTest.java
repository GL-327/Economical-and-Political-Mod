package com.political.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ClockTest {

    /** Hand-driven time source: tests move ticks and wall time independently. */
    private static final class ManualSource implements Clock.Source {
        long ticks;
        long wall;

        @Override
        public long ticks() {
            return ticks;
        }

        @Override
        public long wallMillis() {
            return wall;
        }
    }

    @AfterEach
    void restore() {
        Clock.reset();
    }

    @Test
    void oneTickIsFiftyMillisecondsOfGameTime() {
        ManualSource source = new ManualSource();
        Clock.useSource(source);
        source.ticks = 0;
        assertEquals(0L, Clock.gameMillis());
        source.ticks = 1;
        assertEquals(50L, Clock.gameMillis());
        source.ticks = 20;
        assertEquals(1000L, Clock.gameMillis());
        assertEquals(20L, Clock.ticks());
    }

    @Test
    void gameTimeIgnoresWallClockJumps() {
        ManualSource source = new ManualSource();
        Clock.useSource(source);
        source.ticks = 100;
        source.wall = 1_000_000L;
        long before = Clock.gameMillis();
        source.wall += 3_600_000L; // an hour of lag or a laptop sleep: no tick ran
        assertEquals(before, Clock.gameMillis());
        assertEquals(1_000_000L + 3_600_000L, Clock.wallMillis());
    }

    @Test
    void cooldownWrittenInMillisecondsWorksOnGameTime() {
        ManualSource source = new ManualSource();
        Clock.useSource(source);
        long cooldownMs = 3000L; // 60 ticks
        long readyAt = Clock.gameMillis() + cooldownMs;
        source.ticks = 59;
        assertEquals(false, Clock.gameMillis() >= readyAt);
        source.ticks = 60;
        assertEquals(true, Clock.gameMillis() >= readyAt);
    }
}
