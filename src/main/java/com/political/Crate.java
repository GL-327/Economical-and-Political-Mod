package com.political;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.Locale;

/**
 * The crates, and the odds behind them.
 *
 * <p>Every crate publishes its full drop table and the exact percentage of each rarity before it is
 * opened. That is a deliberate design position rather than a legal one: a loot box whose odds are
 * hidden is a slot machine, and a loot box whose odds are printed on the front is a purchase. The
 * whole system is also bought with coins earned in game and never with anything else.</p>
 *
 * <p>Weights are per rarity band, and the item rolled inside a band comes from the live recipe
 * catalogue, so crates automatically cover new content instead of drifting out of date every time a
 * gear line is added.</p>
 */
public enum Crate {

    WOODEN("Wooden Crate", ChatFormatting.GRAY, Items.OAK_PLANKS, 2_500L,
            "The cheap one. Mostly materials, occasionally something worth keeping.",
            new int[]{62, 26, 9, 3, 0}),

    IRON("Iron Crate", ChatFormatting.WHITE, Items.IRON_BLOCK, 12_000L,
            "A working crate. Reliable middle of the road pulls.",
            new int[]{40, 33, 19, 7, 1}),

    GOLDEN("Golden Crate", ChatFormatting.GOLD, Items.GOLD_BLOCK, 45_000L,
            "Where the gear lines start showing up properly.",
            new int[]{20, 32, 30, 15, 3}),

    ARCANE("Arcane Crate", ChatFormatting.LIGHT_PURPLE, Items.AMETHYST_BLOCK, 140_000L,
            "Weighted hard toward the things people actually want.",
            new int[]{5, 20, 35, 30, 10}),

    CELESTIAL("Celestial Crate", ChatFormatting.AQUA, Items.NETHER_STAR, 500_000L,
            "The top of the shop. Nothing common is left in the pool at all.",
            new int[]{0, 5, 25, 45, 25});

    /** The five bands a pull can land in, worst to best. */
    public enum Band {
        COMMON("Common", ChatFormatting.GRAY),
        UNCOMMON("Uncommon", ChatFormatting.GREEN),
        RARE("Rare", ChatFormatting.BLUE),
        EPIC("Epic", ChatFormatting.DARK_PURPLE),
        LEGENDARY("Legendary", ChatFormatting.GOLD);

        public final String label;
        public final ChatFormatting colour;

        Band(String label, ChatFormatting colour) {
            this.label = label;
            this.colour = colour;
        }
    }

    public final String display;
    public final ChatFormatting colour;
    public final Item icon;
    public final long price;
    public final String blurb;
    /** Percentage chance per band, in Band order. Always sums to one hundred. */
    public final int[] odds;

    Crate(String display, ChatFormatting colour, Item icon, long price, String blurb, int[] odds) {
        this.display = display;
        this.colour = colour;
        this.icon = icon;
        this.price = price;
        this.blurb = blurb;
        this.odds = odds;
    }

    public String key() {
        return name().toLowerCase(Locale.ROOT);
    }

    public int oddsFor(Band b) {
        return odds[b.ordinal()];
    }

    /** Rolls a band from this crate's published odds. */
    public Band roll(java.util.Random rng) {
        int r = rng.nextInt(100);
        int acc = 0;
        for (Band b : Band.values()) {
            acc += oddsFor(b);
            if (r < acc) return b;
        }
        return Band.COMMON;
    }

    /**
     * How many pulls the pity counter allows before the next one is forced up to at least this
     * band. Without it a run of bad luck on an expensive crate feels like the shop is lying.
     */
    public int pityAfter() {
        return switch (this) {
            case WOODEN -> 25;
            case IRON -> 20;
            case GOLDEN -> 15;
            case ARCANE -> 12;
            case CELESTIAL -> 8;
        };
    }

    /** The band the pity counter guarantees when it fires. */
    public Band pityBand() {
        return switch (this) {
            case WOODEN, IRON -> Band.RARE;
            case GOLDEN, ARCANE -> Band.EPIC;
            case CELESTIAL -> Band.LEGENDARY;
        };
    }

    public static Crate byKey(String key) {
        if (key == null) return null;
        for (Crate c : values()) {
            if (c.key().equalsIgnoreCase(key)) return c;
        }
        return null;
    }
}
