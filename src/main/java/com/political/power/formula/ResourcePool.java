package com.political.power.formula;

import com.political.power.PowerSettings;

/**
 * The arithmetic of the cursed resource (C2): how big the pool can be, how it comes back, and what death
 * does to it. All functions are pure so every boundary can be tested.
 */
public final class ResourcePool {

    private ResourcePool() {
    }

    /**
     * Maximum of the pool: the base, plus a step for every school tier reached, plus other bonuses (the
     * intelligence stat, no-armor vow). Never below 1.
     */
    public static double max(int tier, double otherBonuses) {
        int clampedTier = Math.max(0, Math.min(4, tier));
        double max = PowerSettings.RESOURCE_BASE_MAX.get()
                + (double) PowerSettings.RESOURCE_PER_TIER.get() * clampedTier
                + otherBonuses;
        return Math.max(1.0, max);
    }

    /**
     * Resource after an interval of {@code elapsedTicks} ticks. Regeneration only runs for the ticks of the
     * interval that fall after the regeneration delay, which counts from the last time the player spent
     * resource; {@code ticksSinceSpend} is measured at the end of the interval. It never goes above
     * {@code max}, never takes anything away, and does nothing while {@code paused} (a Domain pauses it).
     */
    public static double regen(double current, double max, long ticksSinceSpend, long elapsedTicks, boolean paused) {
        if (paused || current >= max || elapsedTicks <= 0) {
            return current;
        }
        long delay = PowerSettings.RESOURCE_REGEN_DELAY_TICKS.get();
        long regenTicks = Math.max(0L, Math.min(elapsedTicks, ticksSinceSpend - delay));
        double gained = PowerSettings.RESOURCE_REGEN_PER_SECOND.get() * regenTicks / 20.0;
        return Math.min(max, current + gained);
    }

    /** What is left after death: the kept fraction of the current amount, rounded down so it never grows. */
    public static double afterDeath(double current) {
        double kept = Math.floor(Math.max(0.0, current) * PowerSettings.RESOURCE_DEATH_KEEP_FRACTION.get());
        return Math.max(0.0, kept);
    }

    /** Keeps the pool inside its (possibly smaller) maximum, for example after a vow bonus is lost. */
    public static double clampToMax(double current, double max) {
        return Math.max(0.0, Math.min(current, max));
    }
}
