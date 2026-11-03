package com.political.power.formula;

import com.political.power.PowerSettings;

/**
 * Black Flash (C8), a passive: a melee hit shortly after a cast can land with a damage multiplier. Its own
 * stage in the damage pipeline (M5 rebuilds that pipeline; until then one call from the damage mixin).
 *
 * <p>It needs a minimum school tier, only hits that land within the window after the player's last cast
 * qualify, and each qualifying hit has a configured chance. A Black Flash <b>replaces</b> the critical hit
 * multiplier instead of stacking with it: the damage is divided by the crit multiplier first.</p>
 */
public final class BlackFlash {

    /** The result of the stage: the damage to use and whether a Black Flash happened. */
    public record Result(double damage, boolean triggered) {
    }

    private BlackFlash() {
    }

    /**
     * @param damage              damage before this stage
     * @param critMultiplier      the multiplier the critical hit already applied to {@code damage}; 1 when the hit was not critical
     * @param tier                the attacker's school tier
     * @param ticksSinceLastCast  ticks since the attacker's last technique cast
     * @param roll                a uniform random number in [0, 1); the hit triggers when it is below the chance
     */
    public static Result apply(double damage, double critMultiplier, int tier, long ticksSinceLastCast, double roll) {
        if (!qualifies(tier, ticksSinceLastCast)) {
            return new Result(damage, false);
        }
        if (!(roll < PowerSettings.BLACK_FLASH_CHANCE.get())) {
            return new Result(damage, false);
        }
        double base = critMultiplier > 1.0 ? damage / critMultiplier : damage;
        return new Result(base * PowerSettings.BLACK_FLASH_MULTIPLIER.get(), true);
    }

    /** The conditions that do not involve chance. */
    public static boolean qualifies(int tier, long ticksSinceLastCast) {
        return tier >= PowerSettings.BLACK_FLASH_MIN_TIER.get()
                && ticksSinceLastCast >= 0
                && ticksSinceLastCast <= PowerSettings.BLACK_FLASH_WINDOW_TICKS.get();
    }
}
