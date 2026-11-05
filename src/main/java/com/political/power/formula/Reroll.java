package com.political.power.formula;

import com.political.power.PowerSettings;

/**
 * Rerolling the innate technique. There are three ways: spend Arcane Shards (the price grows with every reroll, and is
 * higher once the technique is awakened), spend a reroll charge (earned by defeating the configured boss), or use a
 * Technique Manual to get that one technique. Arcane Shards and charges are the only prices; nothing here creates income.
 */
public final class Reroll {

    private Reroll() {
    }

    /** Arcane Shards a random reroll costs after {@code rerolls} earlier rerolls. */
    public static int shardCost(int rerolls, boolean awakened) {
        double base = PowerSettings.REROLL_SHARD_BASE.get() + (double) PowerSettings.REROLL_SHARD_STEP.get() * Math.max(0, rerolls);
        if (awakened) {
            base *= PowerSettings.REROLL_AWAKENED_MULTIPLIER.get();
        }
        return (int) Math.min(PowerSettings.REROLL_SHARD_MAX.get(), Math.ceil(base));
    }

    /** Charges after a boss kill that grants one, never above the configured maximum. */
    public static int chargesAfterGrant(int charges) {
        return Math.min(PowerSettings.REROLL_MAX_CHARGES.get(), Math.max(0, charges) + 1);
    }
}
