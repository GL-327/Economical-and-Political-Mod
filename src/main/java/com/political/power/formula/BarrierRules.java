package com.political.power.formula;

import com.political.power.PowerSettings;

/**
 * What the Barrier blocks (C7). The rule for fall and void damage is explicit and configurable: fall damage
 * is blocked, void damage is not, and neither is a plain kill, because nothing in the game can block those.
 */
public final class BarrierRules {

    /** The kinds of damage the rule distinguishes; the game maps a damage source onto one of them. */
    public enum DamageClass {
        /** Ordinary combat, magic, fire, explosions and everything else. */
        NORMAL,
        /** Falling. */
        FALL,
        /** Falling out of the world. */
        VOID,
        /** A kill command or other damage that bypasses everything. */
        UNBLOCKABLE
    }

    private BarrierRules() {
    }

    public static boolean blocks(DamageClass damage) {
        return switch (damage) {
            case NORMAL -> true;
            case FALL -> PowerSettings.BARRIER_BLOCKS_FALL.get();
            case VOID -> PowerSettings.BARRIER_BLOCKS_VOID.get();
            case UNBLOCKABLE -> false;
        };
    }

    /** Whether a barrier that started at {@code startTick} still stands at {@code nowTick}. */
    public static boolean active(long startTick, long nowTick) {
        return nowTick >= startTick && nowTick < startTick + PowerSettings.BARRIER_DURATION_TICKS.get();
    }
}
