package com.political;


import com.political.settlement.Settlement;
import com.political.settlement.SettlementEconomy;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/**
 * V5 Pillar 3 — the commodities that move along caravan routes.
 *
 * <p>Each good has a base value and derives its local <b>supply</b> and <b>demand</b> purely from
 * a settlement's live {@link SettlementEconomy} metrics (food, goods, jobSites, appeal, wonders,
 * happiness, footprint, tier). Nothing here is stored — prices are a pure function of the world
 * state, so a settlement cannot "farm" a price by holding items; it must actually be built and
 * staffed differently to move its numbers.</p>
 *
 * <p>Price at a settlement = baseValue scaled by the demand/supply gap, clamped to 0.5x..2.0x.
 * A route earns the <i>difference</i> between the destination price and the origin price, so the
 * profitable routes are the ones that connect genuinely complementary settlements — a grain
 * surplus feeding a starving neighbour, an ore town supplying a workshop city.</p>
 */
public enum TradeGood {

    GRAIN("Grain", "§f", Items.WHEAT, 8),
    LIVESTOCK("Livestock", "§f", Items.BEEF, 14),
    TIMBER("Timber", "§6", Items.OAK_LOG, 6),
    STONE("Cut Stone", "§7", Items.STONE_BRICKS, 7),
    ORE("Ore", "§b", Items.RAW_IRON, 20),
    CLOTH("Cloth", "§d", Items.WOOL.white(), 16),
    TOOLS("Tools", "§e", Items.IRON_PICKAXE, 30),
    MEDICINE("Medicine", "§a", Items.GLISTERING_MELON_SLICE, 38),
    REAGENTS("Reagents", "§5", Items.BLAZE_POWDER, 46),
    LUXURIES("Luxuries", "§6", Items.GOLDEN_APPLE, 62),
    RELICS("Relics", "§b", Items.HEART_OF_THE_SEA, 95);

    public final String displayName;
    public final String colorCode;
    public final Item icon;
    /** Coins per unit before local supply/demand pricing. */
    public final int baseValue;

    TradeGood(String displayName, String colorCode, Item icon, int baseValue) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.icon = icon;
        this.baseValue = baseValue;
    }

    public String colored() {
        return colorCode + displayName;
    }

    public static TradeGood byName(String name) {
        if (name == null) return null;
        for (TradeGood g : values()) {
            if (g.name().equalsIgnoreCase(name) || g.displayName.equalsIgnoreCase(name)) return g;
        }
        return null;
    }

    private static int clamp100(double v) {
        if (v < 0) return 0;
        if (v > 100) return 100;
        return (int) Math.round(v);
    }

    /** How much of this good the settlement produces, 0..100. */
    public int supply(Settlement s) {
        if (s == null) return 0;
        int rank = s.tierEnum().rank();
        double surplus = s.food - s.population;
        return switch (this) {
            case GRAIN -> clamp100(surplus * 5.0 + s.food * 0.5);
            case LIVESTOCK -> clamp100(surplus * 3.0 + s.rawBeds * 0.4);
            case TIMBER -> clamp100(s.footprintCells * 2.5);
            case STONE -> clamp100(s.footprintCells * 1.8 + rank * 4.0);
            case ORE -> clamp100(s.jobSites * 1.6 + rank * 3.0);
            case CLOTH -> clamp100(s.goods * 2.0);
            case TOOLS -> clamp100(s.goods * 1.2 + s.jobSites * 0.8);
            case MEDICINE -> clamp100(s.happiness * 0.5 + s.goods * 0.6);
            case REAGENTS -> clamp100(s.goods * 0.9 + rank * 6.0);
            case LUXURIES -> clamp100(s.appeal * 0.05 + rank * 5.0);
            case RELICS -> clamp100(s.wonders * 22.0 + rank * 3.0);
        };
    }

    /** How badly the settlement wants this good, 0..100. */
    public int demand(Settlement s) {
        if (s == null) return 0;
        int rank = s.tierEnum().rank();
        int pop = s.population;
        double hunger = Math.max(0, s.population - s.food);
        return switch (this) {
            case GRAIN -> clamp100(hunger * 8.0 + pop * 0.8);
            case LIVESTOCK -> clamp100(pop * 1.1 + hunger * 4.0);
            case TIMBER -> clamp100(Math.max(0, s.beds - pop) * 3.0 + pop * 0.5);
            case STONE -> clamp100(pop * 0.9 + Math.max(0, 40 - s.footprintCells) * 1.2);
            case ORE -> clamp100(pop * 1.2 + Math.max(0, 30 - s.jobSites) * 1.5);
            case CLOTH -> clamp100(pop * 1.4 + (100 - s.happiness) * 0.3);
            case TOOLS -> clamp100(s.jobSites * 1.4 + pop * 0.6);
            case MEDICINE -> clamp100((100 - s.happiness) * 1.1 + pop * 0.4);
            case REAGENTS -> clamp100(rank * 8.0 + pop * 0.5);
            case LUXURIES -> clamp100(rank * 9.0 + s.happiness * 0.35);
            case RELICS -> clamp100(rank * 10.0 + Math.max(0, 3 - s.wonders) * 12.0);
        };
    }

    /** Local unit price in coins. Never below 1. */
    public int priceAt(Settlement s) {
        if (s == null) return baseValue;
        double gap = (demand(s) - supply(s)) / 160.0;
        double mult = 1.0 + gap;
        if (mult < 0.5) mult = 0.5;
        if (mult > 2.0) mult = 2.0;
        return (int) Math.max(1, Math.round(baseValue * mult));
    }

    /** Positive if shipping this good from {@code from} to {@code to} makes money per unit. */
    public int marginBetween(Settlement from, Settlement to) {
        return priceAt(to) - priceAt(from);
    }

    /** Human-readable market note for GUIs. */
    public String marketNote(Settlement s) {
        int sup = supply(s);
        int dem = demand(s);
        if (dem - sup >= 35) return "§cIn demand";
        if (sup - dem >= 35) return "§aSurplus";
        return "§7Balanced";
    }
}
