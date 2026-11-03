package com.political;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.Locale;

/**
 * The refined materials: twenty four ores and reagents that do not exist in vanilla.
 *
 * <p>Mithril, titanium, tungsten and the rest. Each one runs a five step chain from a raw lump up to
 * a super compacted block, and every step is a real craft with a real recipe rather than a number
 * going up, so the whole line shows in the recipe browser and can be edited like anything else.</p>
 *
 * <p>None of these are registered items. A refined material is a vanilla base wearing a name, a
 * colour and a custom id, which is what lets the entire chain exist on a server side mod with
 * nothing installed on the client. The <b>seed</b> is the vanilla material a raw lump is smelted out
 * of, which is what ties the new economy to the existing one instead of running beside it.</p>
 */
public enum RefinedMaterial {

    // ── The metals ────────────────────────────────────────────────────────
    MITHRIL("Mithril", ChatFormatting.AQUA, Items.IRON_INGOT, Items.PRISMARINE_CRYSTALS, 4,
            "Light, cold, and it rings when struck."),
    TITANIUM("Titanium", ChatFormatting.WHITE, Items.IRON_INGOT, Items.IRON_BLOCK, 6,
            "It refuses to bend and it refuses to rust."),
    TUNGSTEN("Tungsten", ChatFormatting.GRAY, Items.IRON_INGOT, Items.NETHERRACK, 8,
            "Heavier than it has any right to be."),
    UMBER("Umber", ChatFormatting.GOLD, Items.COPPER_INGOT, Items.TERRACOTTA, 3,
            "Earth that was left in the kiln too long."),
    COBALT("Cobalt", ChatFormatting.BLUE, Items.LAPIS_LAZULI, Items.IRON_INGOT, 5,
            "Blue the whole way through, not just on the outside."),
    ADAMANTITE("Adamantite", ChatFormatting.DARK_RED, Items.NETHERITE_SCRAP, Items.REDSTONE_BLOCK, 12,
            "The word people reach for when nothing else is hard enough."),
    ORICHALCUM("Orichalcum", ChatFormatting.YELLOW, Items.GOLD_INGOT, Items.HONEYCOMB, 7,
            "Half a legend and half a metal."),
    STARMETAL("Starmetal", ChatFormatting.LIGHT_PURPLE, Items.IRON_INGOT, Items.NETHER_STAR, 20,
            "It fell, and it has not stopped being warm."),
    VOIDSTEEL("Voidsteel", ChatFormatting.DARK_PURPLE, Items.NETHERITE_SCRAP, Items.ENDER_PEARL, 15,
            "Forged somewhere with no floor."),
    MOONSILVER("Moonsilver", ChatFormatting.WHITE, Items.IRON_INGOT, Items.GLOWSTONE_DUST, 6,
            "Only worth working at night, which nobody has explained."),
    SUNSTEEL("Sunsteel", ChatFormatting.GOLD, Items.GOLD_INGOT, Items.BLAZE_POWDER, 9,
            "Too bright to look at while it cools."),
    DUSKIRON("Duskiron", ChatFormatting.DARK_GRAY, Items.IRON_INGOT, Items.COAL_BLOCK, 5,
            "Quenched at the hour nobody names."),
    RUNEGOLD("Runegold", ChatFormatting.GOLD, Items.GOLD_INGOT, Items.LAPIS_BLOCK, 10,
            "It holds a word better than paper does."),
    EMBERITE("Emberite", ChatFormatting.RED, Items.COPPER_INGOT, Items.MAGMA_CREAM, 6,
            "Still burning somewhere in the middle."),
    FROSTBITE("Frostbite", ChatFormatting.AQUA, Items.IRON_INGOT, Items.BLUE_ICE, 6,
            "Handling it barehanded is a one time decision."),

    // ── The gemstones and glass ───────────────────────────────────────────
    GLACITE("Glacite", ChatFormatting.AQUA, Items.PACKED_ICE, Items.AMETHYST_SHARD, 5,
            "Ice that decided to become a mineral."),
    BLOODSTONE("Bloodstone", ChatFormatting.DARK_RED, Items.REDSTONE, Items.NETHER_WART, 7,
            "It is not blood. It is not not blood either."),
    STORMGLASS("Stormglass", ChatFormatting.YELLOW, Items.GLASS, Items.LIGHTNING_ROD.weathering().unaffected(), 8,
            "Made where the sand was standing when it happened."),

    // ── The organics ──────────────────────────────────────────────────────
    SOUL_STRING("Soul String", ChatFormatting.DARK_AQUA, Items.STRING, Items.SOUL_SAND, 5,
            "Spun from something that was not finished talking."),
    GRIMHIDE("Grimhide", ChatFormatting.DARK_GRAY, Items.LEATHER, Items.ROTTEN_FLESH, 4,
            "It was tough before anybody cured it."),
    WYRMSCALE("Wyrmscale", ChatFormatting.DARK_GREEN, Items.PHANTOM_MEMBRANE, Items.DRAGON_BREATH, 14,
            "One plate. There were a great many plates."),
    HEARTWOOD("Heartwood", ChatFormatting.DARK_GREEN, Items.OAK_LOG, Items.MOSS_BLOCK, 3,
            "The part of the tree that was load bearing."),
    GHOSTIRON("Ghost Iron", ChatFormatting.WHITE, Items.IRON_INGOT, Items.PHANTOM_MEMBRANE, 7,
            "Half of it is somewhere else."),
    DEEPALLOY("Deep Alloy", ChatFormatting.DARK_AQUA, Items.DEEPSLATE, Items.ECHO_SHARD, 11,
            "Poured in a place that had been quiet for a long time.");

    /**
     * The five steps every material runs through.
     *
     * <p>Each step wants nine of the one below, which is the ratio Skyblock uses and the reason a
     * super compacted block represents six and a half thousand raw lumps. That number is the point:
     * it is a target rather than a purchase.</p>
     */
    public enum Step {
        RAW("Raw", "", ChatFormatting.GRAY, 1),
        REFINED("Refined", "Refined ", ChatFormatting.WHITE, 9),
        ENCHANTED("Enchanted", "Enchanted ", ChatFormatting.AQUA, 81),
        COMPACTED("Compacted", "Compacted ", ChatFormatting.LIGHT_PURPLE, 729),
        SUPER("Super Compacted", "Super Compacted ", ChatFormatting.GOLD, 6561);

        public final String label;
        public final String prefix;
        public final ChatFormatting colour;
        /** How many raw lumps one of these is worth. */
        public final int rawWorth;

        Step(String label, String prefix, ChatFormatting colour, int rawWorth) {
            this.label = label;
            this.prefix = prefix;
            this.colour = colour;
            this.rawWorth = rawWorth;
        }

        public Step below() {
            return ordinal() == 0 ? null : values()[ordinal() - 1];
        }
    }

    public final String display;
    public final ChatFormatting colour;
    /** The vanilla item a raw lump is made from, which anchors it to the existing economy. */
    public final Item seed;
    /** The second reagent, which is what makes each material feel different to make. */
    public final Item reagent;
    /** How many seeds one raw lump costs. Higher means rarer. */
    public final int seedCost;
    public final String flavor;

    RefinedMaterial(String display, ChatFormatting colour, Item seed, Item reagent,
                    int seedCost, String flavor) {
        this.display = display;
        this.colour = colour;
        this.seed = seed;
        this.reagent = reagent;
        this.seedCost = seedCost;
        this.flavor = flavor;
    }

    public String key() {
        return name().toLowerCase(Locale.ROOT);
    }

    /** The custom item id for one step of this material. */
    public String idFor(Step step) {
        return step == Step.RAW
                ? "mat_" + key()
                : "mat_" + step.name().toLowerCase(Locale.ROOT) + "_" + key();
    }

    public String displayFor(Step step) {
        return step.prefix + display;
    }

    /** The base item a given step wears, so the chain reads as escalating even without models. */
    public Item baseFor(Step step) {
        return switch (step) {
            case RAW -> Items.RAW_IRON;
            case REFINED -> seed;
            case ENCHANTED -> Items.IRON_INGOT;
            case COMPACTED -> Items.IRON_BLOCK;
            case SUPER -> Items.NETHERITE_BLOCK;
        };
    }

    /** What one of these is worth in coins, used by the shop and the bazaar. */
    public long valueFor(Step step) {
        return (long) seedCost * step.rawWorth * 3L;
    }

    public static RefinedMaterial byKey(String key) {
        if (key == null) return null;
        for (RefinedMaterial m : values()) {
            if (m.key().equalsIgnoreCase(key)) return m;
        }
        return null;
    }

    /** How many distinct items the whole system is worth. */
    public static int totalItems() {
        return values().length * Step.values().length;
    }
}
