package com.political;


import com.political.settlement.Settlement;
import com.political.settlement.SettlementEconomy;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.function.ToIntBiFunction;

/**
 * V6 variety — extra trade commodities that layer on top of V5's {@link TradeGood} enum.
 *
 * <p>{@link TradeGood} is a fixed enum and is not edited. Instead each commodity here is a
 * {@link Commodity} value object that exposes exactly the same surface a route needs
 * ({@link Commodity#supply}, {@link Commodity#demand}, {@link Commodity#priceAt},
 * {@link Commodity#marginBetween}, {@link Commodity#marketNote}), computed as a pure function of a
 * settlement's live {@link SettlementEconomy} metrics. Nothing is stored, so a settlement cannot
 * "hold" a price — it has to actually be built and staffed differently to move its numbers, exactly
 * like the V5 goods.</p>
 *
 * <h2>How to wire it (no edit by me)</h2>
 * Anywhere the trade UI enumerates goods for the market board or a good-picker, append these to the
 * built-in set. The market is read-only display, so this is a display/inspection hook:
 * <pre>{@code   for (TradeGoodsExpansion.Commodity g : TradeGoodsExpansion.ALL) { ... g.priceAt(s) ... } }</pre>
 * These are surfaced for the player in {@link VarietyGui} / {@link MarketGui}-style boards.
 * (Route <i>persistence</i> keys off the {@link TradeGood} enum name, so opening a live caravan on
 * an expansion commodity would require the enum; these are exposed as market intelligence and
 * inspectable pricing, which is the additive-only, no-edit contract.)
 *
 * <h2>Balance</h2>
 * Every price is clamped to the same 0.5x..2.0x band as {@link TradeGood#priceAt}, so no expansion
 * commodity can ever be worth more than double or less than half its base — they widen the palette
 * of what a market looks like without introducing a new high-value sink or printer.
 */
public final class TradeGoodsExpansion {

    private TradeGoodsExpansion() {}

    /**
     * A trade commodity defined by data + two scoring functions. Prices behave identically to
     * {@link TradeGood}; only the supply/demand curves differ per commodity.
     */
    public static final class Commodity {
        public final String id;
        public final String displayName;
        public final String colorCode;
        public final Item icon;
        public final int baseValue;
        private final ToIntBiFunction<Commodity, Settlement> supplyFn;
        private final ToIntBiFunction<Commodity, Settlement> demandFn;

        Commodity(String id, String displayName, String colorCode, Item icon, int baseValue,
                  ToIntBiFunction<Commodity, Settlement> supplyFn,
                  ToIntBiFunction<Commodity, Settlement> demandFn) {
            this.id = id;
            this.displayName = displayName;
            this.colorCode = colorCode;
            this.icon = icon;
            this.baseValue = baseValue;
            this.supplyFn = supplyFn;
            this.demandFn = demandFn;
        }

        public String colored() {
            return colorCode + displayName;
        }

        public int supply(Settlement s) {
            if (s == null) return 0;
            return clamp100(supplyFn.applyAsInt(this, s));
        }

        public int demand(Settlement s) {
            if (s == null) return 0;
            return clamp100(demandFn.applyAsInt(this, s));
        }

        public int priceAt(Settlement s) {
            if (s == null) return baseValue;
            double gap = (demand(s) - supply(s)) / 160.0;
            double mult = 1.0 + gap;
            if (mult < 0.5) mult = 0.5;
            if (mult > 2.0) mult = 2.0;
            return (int) Math.max(1, Math.round(baseValue * mult));
        }

        public int marginBetween(Settlement from, Settlement to) {
            return priceAt(to) - priceAt(from);
        }

        public String marketNote(Settlement s) {
            int sup = supply(s);
            int dem = demand(s);
            if (dem - sup >= 35) return "§cIn demand";
            if (sup - dem >= 35) return "§aSurplus";
            return "§7Balanced";
        }
    }

    private static int clamp100(int v) {
        if (v < 0) return 0;
        if (v > 100) return 100;
        return v;
    }

    private static int rank(Settlement s) {
        return s.tierEnum().rank();
    }

    /**
     * The 14 expansion commodities. Each curve is deliberately anchored to a different pair of
     * settlement metrics so the market board reads differently town to town.
     */
    public static final List<Commodity> ALL = List.of(
            new Commodity("spices", "Spices", "§6", Items.NETHER_WART, 34,
                    (c, s) -> (int) (rank(s) * 5.0 + s.goods * 0.5),
                    (c, s) -> (int) (s.population * 1.3 + s.happiness * 0.3)),
            new Commodity("dyes", "Dyes", "§d", Items.LAPIS_LAZULI, 22,
                    (c, s) -> (int) (s.goods * 1.6),
                    (c, s) -> (int) (s.appeal * 0.06 + s.population * 0.8)),
            new Commodity("gems", "Cut Gems", "§b", Items.AMETHYST_SHARD, 70,
                    (c, s) -> (int) (s.jobSites * 1.2 + rank(s) * 4.0),
                    (c, s) -> (int) (rank(s) * 9.0 + s.happiness * 0.3)),
            new Commodity("saltfish", "Salt Fish", "§f", Items.COD, 12,
                    (c, s) -> (int) (Math.max(0, s.food - s.population) * 4.0),
                    (c, s) -> (int) (Math.max(0, s.population - s.food) * 6.0 + s.population * 0.6)),
            new Commodity("salt", "Salt", "§f", Items.GUNPOWDER, 15,
                    (c, s) -> (int) (s.footprintCells * 1.5 + rank(s) * 3.0),
                    (c, s) -> (int) (s.population * 1.1 + s.food * 0.3)),
            new Commodity("wine", "Wine", "§5", Items.HONEY_BOTTLE, 40,
                    (c, s) -> (int) (s.appeal * 0.04 + s.goods * 0.8),
                    (c, s) -> (int) (rank(s) * 7.0 + s.happiness * 0.4)),
            new Commodity("parchment", "Parchment", "§e", Items.PAPER, 18,
                    (c, s) -> (int) (s.goods * 1.1 + rank(s) * 2.0),
                    (c, s) -> (int) (s.jobSites * 1.0 + rank(s) * 5.0)),
            new Commodity("glass", "Glasswork", "§b", Items.GLASS, 26,
                    (c, s) -> (int) (s.jobSites * 1.3 + s.goods * 0.5),
                    (c, s) -> (int) (s.appeal * 0.05 + s.population * 0.7)),
            new Commodity("furs", "Furs", "§6", Items.LEATHER, 24,
                    (c, s) -> (int) (s.rawBeds * 0.5 + Math.max(0, s.food - s.population) * 2.0),
                    (c, s) -> (int) (s.population * 1.0 + (100 - s.happiness) * 0.4)),
            new Commodity("pottery", "Pottery", "§7", Items.FLOWER_POT, 20,
                    (c, s) -> (int) (s.footprintCells * 1.4 + s.goods * 0.4),
                    (c, s) -> (int) (s.population * 1.2 + Math.max(0, 30 - s.jobSites) * 1.0)),
            new Commodity("incense", "Incense", "§5", Items.BLAZE_POWDER, 44,
                    (c, s) -> (int) (s.goods * 0.8 + rank(s) * 5.0),
                    (c, s) -> (int) (s.happiness * 0.4 + rank(s) * 6.0)),
            new Commodity("horses", "Draught Horses", "§e", Items.SADDLE, 55,
                    (c, s) -> (int) (Math.max(0, s.food - s.population) * 3.0 + s.footprintCells * 1.0),
                    (c, s) -> (int) (s.jobSites * 1.3 + rank(s) * 4.0)),
            new Commodity("arms", "Arms", "§c", Items.IRON_SWORD, 48,
                    (c, s) -> (int) (s.goods * 1.4 + s.defense * 1.0),
                    (c, s) -> (int) (rank(s) * 6.0 + (100 - s.happiness) * 0.5)),
            new Commodity("silk", "Silk", "§d", Items.STRING, 60,
                    (c, s) -> (int) (s.goods * 0.9 + s.appeal * 0.03),
                    (c, s) -> (int) (rank(s) * 9.0 + s.happiness * 0.35))
    );

    public static Commodity byId(String id) {
        if (id == null) return null;
        for (Commodity c : ALL) {
            if (c.id.equalsIgnoreCase(id) || c.displayName.equalsIgnoreCase(id)) return c;
        }
        return null;
    }
}
