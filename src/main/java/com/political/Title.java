package com.political;

import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;

import java.util.Locale;

/**
 * Titles, earned from what a player has actually done.
 *
 * <p>Every other reward in the mod is a thing you carry. A title is the one that shows without
 * anybody asking, which makes it the natural home for the milestones that are otherwise invisible:
 * nobody can see that you maxed a collection or settled a Nightmare grudge, and those are exactly
 * the achievements worth advertising.</p>
 *
 * <p>Each title states its own requirement and checks it live, so there is no separate progress
 * store to drift out of sync with the systems it reads.</p>
 */
public enum Title {

    // ── Starting out ──────────────────────────────────────────────────────
    NEWCOMER("Newcomer", ChatFormatting.GRAY, Kind.START,
            "Turn up.", p -> true),

    // ── Collections ───────────────────────────────────────────────────────
    GATHERER("the Gatherer", ChatFormatting.GREEN, Kind.COLLECTION,
            "Earn 20 collection tiers.", p -> CollectionManager.totalTiers(p) >= 20),
    HOARDER("the Hoarder", ChatFormatting.GREEN, Kind.COLLECTION,
            "Earn 100 collection tiers.", p -> CollectionManager.totalTiers(p) >= 100),
    COMPLETIONIST("the Completionist", ChatFormatting.DARK_GREEN, Kind.COLLECTION,
            "Earn 300 collection tiers.", p -> CollectionManager.totalTiers(p) >= 300),
    ARCHIVIST("the Archivist", ChatFormatting.AQUA, Kind.COLLECTION,
            "Max a single collection.", p -> CollectionManager.maxedCount(p) >= 1),
    KEEPER_OF_ALL("Keeper of All", ChatFormatting.GOLD, Kind.COLLECTION,
            "Max ten collections.", p -> CollectionManager.maxedCount(p) >= 10),

    // ── The board ─────────────────────────────────────────────────────────
    RUNNER("the Runner", ChatFormatting.WHITE, Kind.BOARD,
            "Complete 10 commissions.", p -> CommissionManager.completed(p) >= 10),
    CONTRACTOR("the Contractor", ChatFormatting.YELLOW, Kind.BOARD,
            "Complete 50 commissions.", p -> CommissionManager.completed(p) >= 50),
    QUARTERMASTER("Quartermaster", ChatFormatting.GOLD, Kind.BOARD,
            "Complete 200 commissions.", p -> CommissionManager.completed(p) >= 200),
    WARRANTED("the Warranted", ChatFormatting.GOLD, Kind.BOARD,
            "Wear the full Quartermaster's Warrant.", WarrantArmour::fullSet),
    WELL_REGARDED("the Well Regarded", ChatFormatting.AQUA, Kind.BOARD,
            "Reach 300 standing.", p -> CommissionManager.standing(p) >= 300),

    // ── Grudges ───────────────────────────────────────────────────────────
    HUNTED("the Hunted", ChatFormatting.DARK_GRAY, Kind.NEMESIS,
            "Have something start hunting you.", p -> !NemesisManager.forPlayer(p).isEmpty()),
    GRUDGE_BEARER("Grudge Bearer", ChatFormatting.DARK_RED, Kind.NEMESIS,
            "Be hunted by three at once.", p -> NemesisManager.forPlayer(p).size() >= 3),
    NIGHTMARE_HUNTED("the Nightmare's Quarry", ChatFormatting.LIGHT_PURPLE, Kind.NEMESIS,
            "Be hunted by a Nightmare.", p -> {
        for (Nemesis n : NemesisManager.forPlayer(p)) {
            if (n.rank() == Nemesis.Rank.NIGHTMARE) return true;
        }
        return false;
    }),

    // ── Wealth ────────────────────────────────────────────────────────────
    COIN_CLIPPER("the Coin Clipper", ChatFormatting.YELLOW, Kind.WEALTH,
            "Hold 100,000 coins.", p -> CoinManager.getCoinsLong(p) >= 100_000L),
    MAGNATE("the Magnate", ChatFormatting.GOLD, Kind.WEALTH,
            "Hold 5,000,000 coins.", p -> CoinManager.getCoinsLong(p) >= 5_000_000L),
    OBSCENELY_RICH("Obscenely Rich", ChatFormatting.GOLD, Kind.WEALTH,
            "Hold 100,000,000 coins.", p -> CoinManager.getCoinsLong(p) >= 100_000_000L),

    // ── Industry ──────────────────────────────────────────────────────────
    FOREMAN("the Foreman", ChatFormatting.GREEN, Kind.INDUSTRY,
            "Place six minions.", p -> MinionManager.countOf(p) >= 6),
    INDUSTRIALIST("the Industrialist", ChatFormatting.AQUA, Kind.INDUSTRY,
            "Place the full twelve minions.",
            p -> MinionManager.countOf(p) >= MinionManager.MAX_PER_PLAYER),

    // ── Standing in the world ─────────────────────────────────────────────
    SETTLER("the Settler", ChatFormatting.GREEN, Kind.CIVIC,
            "Be registered to a settlement.",
            p -> SettlementManager.getRegisteredSettlement(p.getUUID().toString()) != null),
    CITY_BORN("the City Born", ChatFormatting.YELLOW, Kind.CIVIC,
            "Belong to a city or better.", p -> {
        Settlement s = SettlementManager.getRegisteredSettlement(p.getUUID().toString());
        return s != null && s.tierEnum().isAtLeast(SettlementTier.CITY);
    }),
    OF_THE_UTOPIA("of the Utopia", ChatFormatting.LIGHT_PURPLE, Kind.CIVIC,
            "Belong to a utopia or better.", p -> {
        Settlement s = SettlementManager.getRegisteredSettlement(p.getUUID().toString());
        return s != null && s.tierEnum().isAtLeast(SettlementTier.UTOPIA);
    });

    /** Where a title comes from, used to group the menu. */
    public enum Kind {
        START("Beginnings", ChatFormatting.GRAY),
        COLLECTION("Collections", ChatFormatting.GREEN),
        BOARD("The Board", ChatFormatting.GOLD),
        NEMESIS("Grudges", ChatFormatting.DARK_RED),
        WEALTH("Wealth", ChatFormatting.YELLOW),
        INDUSTRY("Industry", ChatFormatting.AQUA),
        CIVIC("Standing", ChatFormatting.LIGHT_PURPLE);

        public final String label;
        public final ChatFormatting colour;

        Kind(String label, ChatFormatting colour) {
            this.label = label;
            this.colour = colour;
        }
    }

    /** Checked live rather than stored, so it can never drift from the system it reads. */
    public interface Requirement {
        boolean met(ServerPlayer player);
    }

    public final String display;
    public final ChatFormatting colour;
    public final Kind kind;
    public final String requirement;
    private final Requirement check;

    Title(String display, ChatFormatting colour, Kind kind, String requirement, Requirement check) {
        this.display = display;
        this.colour = colour;
        this.kind = kind;
        this.requirement = requirement;
        this.check = check;
    }

    public String key() {
        return name().toLowerCase(Locale.ROOT);
    }

    /** Whether this player has earned it. Guarded, since it reaches into many systems. */
    public boolean earnedBy(ServerPlayer player) {
        try {
            return check.met(player);
        } catch (Exception e) {
            // A system that is not ready simply has not granted its titles yet.
            return false;
        }
    }

    public String coloured() {
        return colour + display;
    }

    public static Title byKey(String key) {
        if (key == null) return null;
        for (Title t : values()) {
            if (t.key().equalsIgnoreCase(key)) return t;
        }
        return null;
    }
}
