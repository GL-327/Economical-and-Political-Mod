package com.political.party;

/**
 * Party affiliation is flavor. National treasury, VAT, and government perks do not
 * flow through party perk shops.
 */
public final class PartyPerkEffects {

    private PartyPerkEffects() {}

    public static double coinIncomeMultiplier(String playerUuid) {
        return 1.0;
    }

    public static double experienceOrbMultiplier(String playerUuid) {
        return 1.0;
    }

    public static double deathCoinLossMultiplier(String playerUuid) {
        return 1.0;
    }

    public static double shopBuyMultiplier(String playerUuid) {
        return 1.0;
    }

    public static double shopSellMultiplier(String playerUuid) {
        return 1.0;
    }

    public static long homeCooldownMs(String playerUuid) {
        return 60_000L;
    }

    public static float meleeDamageBonusMultiplier(String playerUuid) {
        return 1.0f;
    }

    public static float projectileDamageBonusMultiplier(String playerUuid) {
        return 1.0f;
    }

    public static double armorEffectivenessBonus(String playerUuid) {
        return 0;
    }
}
