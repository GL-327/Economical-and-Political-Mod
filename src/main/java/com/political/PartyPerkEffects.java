package com.political;

/**
 * Runtime effects for {@link PoliticalPartyManager} party perks (budget-purchased).
 * Keep multipliers small and stack only through explicit perk IDs below.
 */
public final class PartyPerkEffects {

    private PartyPerkEffects() {}

    public static double coinIncomeMultiplier(String playerUuid) {
        double m = 1.0;
        if (PoliticalPartyManager.hasPartyPerk(playerUuid, "coin_bonus")) m += 0.012;
        if (PoliticalPartyManager.hasPartyPerk(playerUuid, "prosperity_mark")) m += 0.008;
        return m;
    }

    public static double experienceOrbMultiplier(String playerUuid) {
        double m = 1.0;
        if (PoliticalPartyManager.hasPartyPerk(playerUuid, "xp_bonus")) m += 0.012;
        if (PoliticalPartyManager.hasPartyPerk(playerUuid, "scholars_circle")) m += 0.008;
        return m;
    }

    /** Multiplier applied to death coin loss fraction (lower = less lost). */
    public static double deathCoinLossMultiplier(String playerUuid) {
        double m = 1.0;
        if (PoliticalPartyManager.hasPartyPerk(playerUuid, "death_insurance")) m *= 0.88;
        if (PoliticalPartyManager.hasPartyPerk(playerUuid, "wardens_oath")) m *= 0.92;
        return m;
    }

    /** Buy price multiplier (&lt; 1 = discount). */
    public static double shopBuyMultiplier(String playerUuid) {
        double m = 1.0;
        if (PoliticalPartyManager.hasPartyPerk(playerUuid, "shop_discount")) m *= 0.98;
        if (PoliticalPartyManager.hasPartyPerk(playerUuid, "bulk_contract")) m *= 0.985;
        return m;
    }

    /** Sell value multiplier (&gt; 1 = bonus). */
    public static double shopSellMultiplier(String playerUuid) {
        double m = 1.0;
        if (PoliticalPartyManager.hasPartyPerk(playerUuid, "sell_bonus")) m += 0.02;
        if (PoliticalPartyManager.hasPartyPerk(playerUuid, "merchants_favor")) m += 0.015;
        return m;
    }

    public static long homeCooldownMs(String playerUuid) {
        long base = 60_000L;
        if (PoliticalPartyManager.hasPartyPerk(playerUuid, "fast_travel")) base = (long) (base * 0.88);
        if (PoliticalPartyManager.hasPartyPerk(playerUuid, "waystone_network")) base = (long) (base * 0.90);
        return base;
    }

    public static float meleeDamageBonusMultiplier(String playerUuid) {
        float m = 1.0f;
        if (PoliticalPartyManager.hasPartyPerk(playerUuid, "war_banner")) m += 0.02f;
        return m;
    }

    public static float projectileDamageBonusMultiplier(String playerUuid) {
        float m = 1.0f;
        if (PoliticalPartyManager.hasPartyPerk(playerUuid, "rangers_signal")) m += 0.02f;
        return m;
    }

    public static double armorEffectivenessBonus(String playerUuid) {
        double m = 0;
        if (PoliticalPartyManager.hasPartyPerk(playerUuid, "bulwark_rations")) m += 0.02;
        return m;
    }
}
