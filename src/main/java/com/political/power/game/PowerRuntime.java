package com.political.power.game;

import com.political.power.formula.VowSet;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;

/**
 * What the power system remembers about a player only while the server runs: cooldowns, the recent cast
 * history, the last time resource was spent, the Barrier window and the live vow set. Server ticks restart with
 * the server, so none of this is saved (a restart clears cooldowns; only the owner can restart the server).
 */
final class PowerRuntime {

    static final long NEVER = Long.MIN_VALUE / 4;

    long lastCastTick = NEVER;
    final ArrayDeque<Long> recentCasts = new ArrayDeque<>();
    /** Ability id to the tick at which it is ready again. */
    final Map<String, Long> readyAt = new HashMap<>();
    long lastSpendTick = NEVER;
    /** The Barrier blocks damage until this tick (exclusive). */
    long barrierUntilTick;
    VowSet vows = new VowSet();
    long lastRegenTick;
    long lastVowCheckTick;

    long ticksSinceLastCast(long now) {
        return lastCastTick == NEVER ? Long.MAX_VALUE : now - lastCastTick;
    }

    int castsWithin(long now, int windowTicks) {
        while (!recentCasts.isEmpty() && now - recentCasts.peekFirst() >= Math.max(windowTicks, 1200)) {
            recentCasts.pollFirst();
        }
        int count = 0;
        for (long t : recentCasts) {
            if (now - t < windowTicks) {
                count++;
            }
        }
        return count;
    }

    long cooldownRemaining(String abilityId, long now) {
        Long ready = readyAt.get(abilityId);
        return ready == null ? 0L : Math.max(0L, ready - now);
    }

    void clearCombatState() {
        readyAt.clear();
        recentCasts.clear();
        lastCastTick = NEVER;
        barrierUntilTick = 0L;
    }
}
