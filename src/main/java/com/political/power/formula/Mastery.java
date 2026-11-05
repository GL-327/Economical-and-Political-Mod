package com.political.power.formula;

import com.political.power.PowerSettings;
import com.political.technique.Technique;

/**
 * Mastery and awakening. A technique that is not awakened cannot be used (tier 0). Once awakened, the tier is the
 * mastery rank of the player's technique: Novice 1, Adept 2, Expert 3, Master 4. The rank comes from how often the
 * technique has been cast. The tier sets the energy pool (see {@code ResourcePool}) and Black Flash; each rank above
 * Novice adds a little damage and takes a little off the cooldowns; at Master the Domain unlocks.
 */
public final class Mastery {

    private Mastery() {
    }

    /** The rank for a number of casts of the technique. */
    public static Technique.MasteryRank rank(int uses) {
        return Technique.MasteryRank.forUses(Math.max(0, uses));
    }

    /** 0 while the technique sleeps, otherwise 1 (Novice) to 4 (Master). */
    public static int tier(boolean awakened, int uses) {
        return awakened ? rank(uses).ordinal() + 1 : 0;
    }

    /** Extra technique damage from mastery, as a fraction (0 at tier 0 and 1). */
    public static double damageBonus(int tier) {
        return Math.max(0, tier - 1) * PowerSettings.MASTERY_DAMAGE_PER_RANK.get();
    }

    /** Share of every cooldown taken off by mastery (0 at tier 0 and 1). */
    public static double cooldownReduction(int tier) {
        return Math.max(0, tier - 1) * PowerSettings.MASTERY_COOLDOWN_PER_RANK.get();
    }

    /** The Domain is unlocked by mastering a technique. */
    public static boolean domainUnlocked(int tier) {
        return tier >= Tiers.MAX_TIER;
    }

    /** Casts still needed for the next rank, or -1 at Master. */
    public static int usesToNextRank(int uses) {
        Technique.MasteryRank rank = rank(uses);
        Technique.MasteryRank[] all = Technique.MasteryRank.values();
        if (rank.ordinal() + 1 >= all.length) {
            return -1;
        }
        return all[rank.ordinal() + 1].usesRequired - Math.max(0, uses);
    }
}
