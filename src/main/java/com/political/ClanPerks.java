package com.political;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * V7 Pillar A — clan perks. Unlike guild perks these are never purchased: they switch on
 * automatically for every member the moment the clan reaches the required prestige level, and
 * switch off again if the clan ever falls back below it.
 *
 * <p>Every bonus is deliberately small and, where it touches the economy, capped and driven by
 * a real kill (never elapsed time). The static getters at the bottom are the public surface the
 * rest of Pillar A calls; they are all null-safe and return the neutral value for players who
 * are not in a clan.</p>
 */
public final class ClanPerks {

    private ClanPerks() {}

    /** A passive perk, unlocked by clan prestige level. */
    public enum Perk {
        BLOOD_VIGOR("blood_vigor", "Blood Vigor", Items.GHAST_TEAR, 2,
                "§7Every cursed spirit a member slays",
                "§7banks §b+3 extra cursed energy§7."),
        CURSED_BANE("cursed_bane", "Cursed Bane", Items.IRON_SWORD, 3,
                "§7Members deal §c+8% §7melee damage",
                "§7to cursed spirits."),
        ANCESTRAL_HALL("ancestral_hall", "Ancestral Hall", Items.BELL, 4,
                "§7Inside the clan hall settlement, members",
                "§7gain §aResistance §7and slow §aRegeneration§7."),
        INHERITED_MASTERY("inherited_mastery", "Inherited Mastery", Items.ENCHANTED_BOOK, 6,
                "§7The clan signature technique costs",
                "§7§b20% less energy §7and cools down faster."),
        KINSHIP_DIVIDEND("kinship_dividend", "Kinship Dividend", Items.GOLD_INGOT, 8,
                "§7Each spirit a member slays drips a few",
                "§7coins into the clan treasury (daily capped).");

        public final String id;
        public final String display;
        public final Item icon;
        public final int requiredLevel;
        public final String[] lore;

        Perk(String id, String display, Item icon, int requiredLevel, String... lore) {
            this.id = id;
            this.display = display;
            this.icon = icon;
            this.requiredLevel = requiredLevel;
            this.lore = lore;
        }

        public static Perk byId(String id) {
            if (id == null) return null;
            for (Perk p : values()) {
                if (p.id.equals(id)) return p;
            }
            return null;
        }
    }

    public static List<Perk> all() {
        List<Perk> out = new ArrayList<>();
        for (Perk p : Perk.values()) out.add(p);
        return out;
    }

    /** True when the given clan has reached the prestige level that unlocks this perk. */
    public static boolean isActive(SorcererClan clan, Perk perk) {
        return clan != null && perk != null && clan.prestigeLevel >= perk.requiredLevel;
    }

    // ════════════════════════════════════════════════════════════════════
    // PUBLIC STATIC GETTERS — safe for any caller
    // ════════════════════════════════════════════════════════════════════

    private static boolean has(String uuid, Perk perk) {
        SorcererClan c = ClanManager.getClanOf(uuid);
        return isActive(c, perk);
    }

    /** Extra cursed energy banked per spirit kill (0 or 3). */
    public static long spiritKillEnergyBonus(String uuid) {
        return has(uuid, Perk.BLOOD_VIGOR) ? 3L : 0L;
    }

    /** Bonus fraction of melee damage vs cursed spirits (0.00 or 0.08). */
    public static double spiritDamageBonus(String uuid) {
        return has(uuid, Perk.CURSED_BANE) ? 0.08 : 0.0;
    }

    /** True when the member should receive the hall ward while standing in the clan hall. */
    public static boolean hasHallWard(String uuid) {
        return has(uuid, Perk.ANCESTRAL_HALL);
    }

    /** Multiplier applied to signature technique energy cost and cooldown (1.0 or 0.8). */
    public static double techniqueDiscount(String uuid) {
        return has(uuid, Perk.INHERITED_MASTERY) ? 0.8 : 1.0;
    }

    /** Coins dripped into the clan treasury per spirit kill (0 or 15). */
    public static long treasuryDividendPerKill(String uuid) {
        return has(uuid, Perk.KINSHIP_DIVIDEND) ? 15L : 0L;
    }
}
