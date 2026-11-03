package com.political.power.formula;

import com.political.power.PowerSettings;

/** Power levels follow the Arcane Shards a player has earned in total (the only source of shards is unchanged). */
public final class Progression {

    public static final int MAX_LEVEL = 50;

    private Progression() {
    }

    public static int levelFor(int shardsEarnedTotal) {
        long level = 1L + Math.max(0, shardsEarnedTotal) / PowerSettings.SHARDS_PER_LEVEL.get();
        return (int) Math.min(MAX_LEVEL, level);
    }

    /** Shards that must have been earned in total to reach {@code level}. */
    public static int shardsForLevel(int level) {
        return Math.max(0, (Math.min(level, MAX_LEVEL) - 1)) * PowerSettings.SHARDS_PER_LEVEL.get();
    }
}
