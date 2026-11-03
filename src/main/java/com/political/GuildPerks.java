package com.political;


import com.political.government.Perk;
import com.political.settlement.Settlement;
import com.political.settlement.SettlementManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * V5 Pillar 2 — the guild perk tree.
 *
 * <p>Perks are bought with the <b>guild</b> treasury (never a player's own coins) and gated
 * behind a guild level, so a rich solo player cannot buy the whole tree on day one. Every
 * bonus is deliberately small: they are a reason to belong to an organisation, not a
 * replacement for playing. The static getters at the bottom are the public surface other
 * systems call — they are all null-safe and return the neutral value (1.0 / 0 / false) for
 * players who are not in a guild.</p>
 */
public final class GuildPerks {

    private GuildPerks() {}

    /** A single purchasable guild perk. */
    public enum Perk {
        COFFERS_I("coffers_1", "Merchant Coffers I", Items.GOLD_INGOT, 100_000L, 2, null,
                "§7+1.5% coins earned by every member."),
        COFFERS_II("coffers_2", "Merchant Coffers II", Items.GOLD_BLOCK, 750_000L, 6, "coffers_1",
                "§7+1.5% more coins (§f3.0% total§7)."),
        COFFERS_III("coffers_3", "Merchant Coffers III", Items.NETHERITE_INGOT, 3_000_000L, 12, "coffers_2",
                "§7+2.0% more coins (§f5.0% total§7)."),

        SCHOLARS_I("scholars_1", "Scholars' Circle I", Items.BOOK, 150_000L, 3, null,
                "§7+1.5% mastery XP for every member."),
        SCHOLARS_II("scholars_2", "Scholars' Circle II", Items.ENCHANTED_BOOK, 1_000_000L, 8, "scholars_1",
                "§7+2.5% more mastery XP (§f4.0% total§7)."),

        HALL_CHARTER("hall_charter", "Hall Charter", Items.BELL, 250_000L, 4, null,
                "§7Your declared hall settlement gains",
                "§7up to §a+4 happiness §7from resident members."),
        HALL_GRAND("hall_grand", "Grand Hall", Items.BEACON, 2_000_000L, 10, "hall_charter",
                "§7Hall happiness bonus rises to §a+8§7,",
                "§7and hall members earn §f+10% §7guild XP."),

        WAYPOINT("waypoint", "Guild Waystone", Items.ENDER_EYE, 500_000L, 5, null,
                "§7Members may travel to the guild hall",
                "§7from the Guild menu (§f10 min §7cooldown)."),

        EXPEDITION_BANNER("expedition_banner", "Expedition Banner", Items.BANNER.white(), 1_250_000L, 7, null,
                "§7+5% coins from expedition payouts,",
                "§7and +25% guild XP from expeditions."),

        STANDING_ORDERS("standing_orders", "Standing Orders", Items.WRITABLE_BOOK, 4_000_000L, 14, "expedition_banner",
                "§7+10% guild XP from every source."),

        PATRON_SEAL("patron_seal", "Patron's Seal", Items.EMERALD_BLOCK, 5_000_000L, 15, "hall_grand",
                "§7Settlement registration fees paid by",
                "§7members are refunded §f50% §7from the",
                "§7guild treasury (if it can afford it).");

        public final String id;
        public final String display;
        public final Item icon;
        public final long cost;
        public final int requiredLevel;
        /** Perk id that must be owned first, or null. */
        public final String requires;
        public final String[] lore;

        Perk(String id, String display, Item icon, long cost, int requiredLevel, String requires, String... lore) {
            this.id = id;
            this.display = display;
            this.icon = icon;
            this.cost = cost;
            this.requiredLevel = requiredLevel;
            this.requires = requires;
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

    /** True when the guild owns every prerequisite and has the level to buy this perk. */
    public static boolean isAvailable(Guild g, Perk p) {
        if (g == null || p == null) return false;
        if (g.hasPerk(p.id)) return false;
        if (g.level < p.requiredLevel) return false;
        return p.requires == null || g.hasPerk(p.requires);
    }

    public static String blockedReason(Guild g, Perk p) {
        if (g == null || p == null) return "§cUnavailable.";
        if (g.hasPerk(p.id)) return "§aAlready unlocked.";
        if (p.requires != null && !g.hasPerk(p.requires)) {
            Perk req = Perk.byId(p.requires);
            return "§cRequires: §f" + (req == null ? p.requires : req.display);
        }
        if (g.level < p.requiredLevel) return "§cRequires guild level §f" + p.requiredLevel;
        return "§eClick to purchase";
    }

    // ════════════════════════════════════════════════════════════════════
    // PUBLIC STATIC GETTERS — safe for any other system to call
    // ════════════════════════════════════════════════════════════════════

    private static Guild guildOf(String uuid) {
        return GuildManager.getGuildOf(uuid);
    }

    private static boolean has(String uuid, Perk p) {
        Guild g = guildOf(uuid);
        return g != null && g.hasPerk(p.id);
    }

    /** Multiplier on coins earned by this player (1.0 .. 1.05). */
    public static double coinMultiplier(String uuid) {
        Guild g = guildOf(uuid);
        if (g == null) return 1.0;
        double m = 1.0;
        if (g.hasPerk(Perk.COFFERS_I.id)) m += 0.015;
        if (g.hasPerk(Perk.COFFERS_II.id)) m += 0.015;
        if (g.hasPerk(Perk.COFFERS_III.id)) m += 0.020;
        return m;
    }

    /** Multiplier on mastery XP earned by this player (1.0 .. 1.04). */
    public static double masteryXpMultiplier(String uuid) {
        Guild g = guildOf(uuid);
        if (g == null) return 1.0;
        double m = 1.0;
        if (g.hasPerk(Perk.SCHOLARS_I.id)) m += 0.015;
        if (g.hasPerk(Perk.SCHOLARS_II.id)) m += 0.025;
        return m;
    }

    /** Multiplier on expedition coin payouts (1.0 or 1.05). */
    public static double expeditionCoinMultiplier(String uuid) {
        return has(uuid, Perk.EXPEDITION_BANNER) ? 1.05 : 1.0;
    }

    /** True when the player may use the guild waystone. */
    public static boolean hasWaystone(String uuid) {
        return has(uuid, Perk.WAYPOINT);
    }

    /** True when settlement registration fees are partly refunded from the guild treasury. */
    public static boolean hasPatronSeal(String uuid) {
        return has(uuid, Perk.PATRON_SEAL);
    }

    /**
     * Extra happiness a settlement receives for being a guild hall, already scaled by how many
     * guild members are actually registered there. 0 when the settlement is nobody's hall.
     */
    public static int hallHappinessBonus(String settlementId) {
        if (settlementId == null) return 0;
        Guild g = GuildManager.guildWithHall(settlementId);
        if (g == null) return 0;
        int cap;
        if (g.hasPerk(Perk.HALL_GRAND.id)) cap = 8;
        else if (g.hasPerk(Perk.HALL_CHARTER.id)) cap = 4;
        else cap = 2;

        Settlement s = SettlementManager.get(settlementId);
        if (s == null) return 0;
        int resident = 0;
        for (String uuid : g.memberMap().keySet()) {
            if (s.registered().contains(uuid)) resident++;
        }
        if (resident <= 0) return 0;
        // One point of happiness per two resident members, capped by the perk tier.
        return Math.min(cap, Math.max(1, resident / 2));
    }

    /** Guild XP multiplier for a whole guild (1.0 .. 1.21). */
    public static double guildXpMultiplier(Guild g) {
        if (g == null) return 1.0;
        double m = 1.0;
        if (g.hasPerk(Perk.STANDING_ORDERS.id)) m += 0.10;
        return m;
    }

    /** Extra multiplier applied only to expedition-sourced guild XP. */
    public static double expeditionGuildXpMultiplier(Guild g) {
        if (g == null) return 1.0;
        return g.hasPerk(Perk.EXPEDITION_BANNER.id) ? 1.25 : 1.0;
    }

    /** Extra guild XP for members registered to the guild's own hall settlement. */
    public static double hallResidentXpMultiplier(Guild g, String uuid) {
        if (g == null || uuid == null) return 1.0;
        if (!g.hasPerk(Perk.HALL_GRAND.id)) return 1.0;
        if (g.hallSettlementId == null) return 1.0;
        Settlement s = SettlementManager.get(g.hallSettlementId);
        if (s == null || !s.registered().contains(uuid)) return 1.0;
        return 1.10;
    }
}
