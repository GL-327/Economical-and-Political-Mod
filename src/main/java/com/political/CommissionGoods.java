package com.political;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * Everything the requisition board will ever ask for, and what it considers each thing worth.
 *
 * <p>The value is the whole balance of the system. It is not a market price, it is roughly how much
 * of a player's time one unit represents, which is why a netherite block sits five thousand above
 * dirt rather than the few hundred a shop would pay. A commission's difficulty is simply value times
 * count, so the reward curve falls out of this table and nothing has to be tuned per quest.</p>
 */
public final class CommissionGoods {

    private CommissionGoods() {}

    /** One requisitionable good. */
    public record Good(Item item, String display, int value, Tier tier) {}

    /** Broad bands, used for theming and for picking a sensible count. */
    public enum Tier { SCRAP, COMMON, USEFUL, VALUABLE, PRECIOUS, EXOTIC, MYTHIC }

    private static final List<Good> GOODS = new ArrayList<>();

    private static void g(Item item, String display, int value, Tier tier) {
        GOODS.add(new Good(item, display, value, tier));
    }

    static {
        // ── Scrap: the things nobody wants but the board still counts ──────
        g(Items.DIRT, "Dirt", 1, Tier.SCRAP);
        g(Items.COBBLESTONE, "Cobblestone", 1, Tier.SCRAP);
        g(Items.GRAVEL, "Gravel", 1, Tier.SCRAP);
        g(Items.SAND, "Sand", 1, Tier.SCRAP);
        g(Items.ROTTEN_FLESH, "Rotten Flesh", 2, Tier.SCRAP);
        g(Items.STICK, "Sticks", 1, Tier.SCRAP);
        g(Items.SEAGRASS, "Seagrass", 2, Tier.SCRAP);
        g(Items.POISONOUS_POTATO, "Poisonous Potatoes", 4, Tier.SCRAP);
        g(Items.DEAD_BUSH, "Dead Bushes", 3, Tier.SCRAP);
        g(Items.ANDESITE, "Andesite", 1, Tier.SCRAP);
        g(Items.DIORITE, "Diorite", 1, Tier.SCRAP);
        g(Items.GRANITE, "Granite", 1, Tier.SCRAP);
        g(Items.TUFF, "Tuff", 2, Tier.SCRAP);
        g(Items.CLAY_BALL, "Clay", 2, Tier.SCRAP);
        g(Items.FLINT, "Flint", 3, Tier.SCRAP);

        // ── Common: the working materials ─────────────────────────────────
        g(Items.OAK_LOG, "Oak Logs", 4, Tier.COMMON);
        g(Items.BIRCH_LOG, "Birch Logs", 4, Tier.COMMON);
        g(Items.SPRUCE_LOG, "Spruce Logs", 4, Tier.COMMON);
        g(Items.JUNGLE_LOG, "Jungle Logs", 5, Tier.COMMON);
        g(Items.ACACIA_LOG, "Acacia Logs", 5, Tier.COMMON);
        g(Items.DARK_OAK_LOG, "Dark Oak Logs", 5, Tier.COMMON);
        g(Items.MANGROVE_LOG, "Mangrove Logs", 6, Tier.COMMON);
        g(Items.CHERRY_LOG, "Cherry Logs", 7, Tier.COMMON);
        g(Items.BAMBOO, "Bamboo", 2, Tier.COMMON);
        g(Items.WHEAT, "Wheat", 3, Tier.COMMON);
        g(Items.CARROT, "Carrots", 3, Tier.COMMON);
        g(Items.POTATO, "Potatoes", 3, Tier.COMMON);
        g(Items.BEETROOT, "Beetroot", 4, Tier.COMMON);
        g(Items.MELON_SLICE, "Melon", 3, Tier.COMMON);
        g(Items.PUMPKIN, "Pumpkins", 5, Tier.COMMON);
        g(Items.SUGAR_CANE, "Sugar Cane", 3, Tier.COMMON);
        g(Items.COAL, "Coal", 6, Tier.COMMON);
        g(Items.CHARCOAL, "Charcoal", 5, Tier.COMMON);
        g(Items.STRING, "String", 5, Tier.COMMON);
        g(Items.BONE, "Bone", 5, Tier.COMMON);
        g(Items.FEATHER, "Feathers", 4, Tier.COMMON);
        g(Items.LEATHER, "Leather", 8, Tier.COMMON);
        g(Items.EGG, "Eggs", 4, Tier.COMMON);
        g(Items.WOOL.white(), "Wool", 5, Tier.COMMON);
        g(Items.COD, "Cod", 5, Tier.COMMON);
        g(Items.SALMON, "Salmon", 6, Tier.COMMON);
        g(Items.KELP, "Kelp", 2, Tier.COMMON);
        g(Items.INK_SAC, "Ink Sacs", 6, Tier.COMMON);
        g(Items.SNOWBALL, "Snowballs", 2, Tier.COMMON);
        g(Items.MOSS_BLOCK, "Moss", 6, Tier.COMMON);

        // ── Useful: worth going out of your way for ───────────────────────
        g(Items.IRON_INGOT, "Iron Ingots", 18, Tier.USEFUL);
        g(Items.COPPER_INGOT, "Copper Ingots", 10, Tier.USEFUL);
        g(Items.REDSTONE, "Redstone", 12, Tier.USEFUL);
        g(Items.LAPIS_LAZULI, "Lapis Lazuli", 14, Tier.USEFUL);
        g(Items.QUARTZ, "Nether Quartz", 16, Tier.USEFUL);
        g(Items.GLOWSTONE_DUST, "Glowstone Dust", 14, Tier.USEFUL);
        g(Items.GUNPOWDER, "Gunpowder", 15, Tier.USEFUL);
        g(Items.SPIDER_EYE, "Spider Eyes", 12, Tier.USEFUL);
        g(Items.SLIME_BALL, "Slimeballs", 16, Tier.USEFUL);
        g(Items.HONEYCOMB, "Honeycomb", 18, Tier.USEFUL);
        g(Items.AMETHYST_SHARD, "Amethyst Shards", 20, Tier.USEFUL);
        g(Items.PRISMARINE_SHARD, "Prismarine Shards", 22, Tier.USEFUL);
        g(Items.PRISMARINE_CRYSTALS, "Prismarine Crystals", 26, Tier.USEFUL);
        g(Items.NAUTILUS_SHELL, "Nautilus Shells", 45, Tier.USEFUL);
        g(Items.PHANTOM_MEMBRANE, "Phantom Membrane", 40, Tier.USEFUL);
        g(Items.RABBIT_FOOT, "Rabbit Feet", 35, Tier.USEFUL);
        g(Items.MAGMA_CREAM, "Magma Cream", 28, Tier.USEFUL);
        g(Items.BLAZE_ROD, "Blaze Rods", 45, Tier.USEFUL);
        g(Items.ENDER_PEARL, "Ender Pearls", 40, Tier.USEFUL);
        g(Items.NETHER_WART, "Nether Wart", 20, Tier.USEFUL);
        g(Items.SOUL_SAND, "Soul Sand", 12, Tier.USEFUL);
        g(Items.OBSIDIAN, "Obsidian", 30, Tier.USEFUL);
        g(Items.BOOK, "Books", 20, Tier.USEFUL);
        g(Items.PAPER, "Paper", 6, Tier.USEFUL);
        g(Items.GOLD_INGOT, "Gold Ingots", 30, Tier.USEFUL);
        g(Items.HAY_BLOCK, "Hay Bales", 28, Tier.USEFUL);
        g(Items.SCULK, "Sculk", 26, Tier.USEFUL);
        g(Items.GLOW_INK_SAC, "Glow Ink Sacs", 24, Tier.USEFUL);
        g(Items.TURTLE_SCUTE, "Turtle Scutes", 60, Tier.USEFUL);
        g(Items.COCOA_BEANS, "Cocoa Beans", 10, Tier.USEFUL);

        // ── Valuable: a real afternoon of work ────────────────────────────
        g(Items.DIAMOND, "Diamonds", 120, Tier.VALUABLE);
        g(Items.EMERALD, "Emeralds", 90, Tier.VALUABLE);
        g(Items.IRON_BLOCK, "Iron Blocks", 170, Tier.VALUABLE);
        g(Items.GOLD_BLOCK, "Gold Blocks", 280, Tier.VALUABLE);
        g(Items.LAPIS_BLOCK, "Lapis Blocks", 130, Tier.VALUABLE);
        g(Items.REDSTONE_BLOCK, "Redstone Blocks", 110, Tier.VALUABLE);
        g(Items.COAL_BLOCK, "Coal Blocks", 55, Tier.VALUABLE);
        g(Items.COPPER_BLOCK.weathering().unaffected(), "Copper Blocks", 95, Tier.VALUABLE);
        g(Items.AMETHYST_BLOCK, "Amethyst Blocks", 85, Tier.VALUABLE);
        g(Items.GLOWSTONE, "Glowstone Blocks", 60, Tier.VALUABLE);
        g(Items.ENDER_EYE, "Eyes of Ender", 90, Tier.VALUABLE);
        g(Items.GHAST_TEAR, "Ghast Tears", 130, Tier.VALUABLE);
        g(Items.SHULKER_SHELL, "Shulker Shells", 220, Tier.VALUABLE);
        g(Items.ECHO_SHARD, "Echo Shards", 200, Tier.VALUABLE);
        g(Items.HEART_OF_THE_SEA, "Hearts of the Sea", 400, Tier.VALUABLE);
        g(Items.WITHER_SKELETON_SKULL, "Wither Skulls", 500, Tier.VALUABLE);
        g(Items.GOLDEN_APPLE, "Golden Apples", 260, Tier.VALUABLE);
        g(Items.BLAZE_POWDER, "Blaze Powder", 30, Tier.VALUABLE);
        g(Items.EXPERIENCE_BOTTLE, "Bottles o' Enchanting", 70, Tier.VALUABLE);
        g(Items.TRIDENT, "Tridents", 900, Tier.VALUABLE);

        // ── Precious: a project rather than a trip ────────────────────────
        g(Items.DIAMOND_BLOCK, "Diamond Blocks", 1100, Tier.PRECIOUS);
        g(Items.EMERALD_BLOCK, "Emerald Blocks", 850, Tier.PRECIOUS);
        g(Items.NETHERITE_SCRAP, "Netherite Scrap", 700, Tier.PRECIOUS);
        g(Items.ANCIENT_DEBRIS, "Ancient Debris", 800, Tier.PRECIOUS);
        g(Items.ENCHANTED_GOLDEN_APPLE, "Enchanted Golden Apples", 2200, Tier.PRECIOUS);
        g(Items.TOTEM_OF_UNDYING, "Totems of Undying", 1400, Tier.PRECIOUS);
        g(Items.ELYTRA, "Elytra", 2600, Tier.PRECIOUS);
        g(Items.DRAGON_BREATH, "Dragon's Breath", 600, Tier.PRECIOUS);
        g(Items.NETHER_STAR, "Nether Stars", 3200, Tier.PRECIOUS);
        g(Items.BEACON, "Beacons", 3600, Tier.PRECIOUS);
        g(Items.CONDUIT, "Conduits", 2400, Tier.PRECIOUS);
        g(Items.HEAVY_CORE, "Heavy Cores", 2800, Tier.PRECIOUS);

        // ── Exotic and mythic: the board asking for something absurd ──────
        g(Items.NETHERITE_INGOT, "Netherite Ingots", 3400, Tier.EXOTIC);
        g(Items.NETHERITE_BLOCK, "Netherite Blocks", 32000, Tier.MYTHIC);
        g(Items.DRAGON_EGG, "Dragon Eggs", 40000, Tier.MYTHIC);
        g(Items.END_CRYSTAL, "End Crystals", 1800, Tier.EXOTIC);
        g(Items.SHULKER_BOX, "Shulker Boxes", 1200, Tier.EXOTIC);
        g(Items.ENCHANTED_BOOK, "Enchanted Books", 900, Tier.EXOTIC);
        g(Items.SPONGE, "Sponges", 1500, Tier.EXOTIC);
        g(Items.SEA_LANTERN, "Sea Lanterns", 400, Tier.EXOTIC);
        g(Items.CRYING_OBSIDIAN, "Crying Obsidian", 500, Tier.EXOTIC);
        g(Items.RESPAWN_ANCHOR, "Respawn Anchors", 2000, Tier.EXOTIC);
        g(Items.SCULK_CATALYST, "Sculk Catalysts", 1600, Tier.EXOTIC);
        g(Items.SCULK_SHRIEKER, "Sculk Shriekers", 1700, Tier.EXOTIC);
        g(Items.REINFORCED_DEEPSLATE, "Reinforced Deepslate", 5000, Tier.MYTHIC);
        g(Items.WITHER_ROSE, "Wither Roses", 1300, Tier.EXOTIC);

        // ── Second pass ───────────────────────────────────────────────────
        // The board needs enough breadth that a player rarely sees the same posting twice, and
        // every good added here multiplies out across all nine count brackets and all seven
        // framings, so a few dozen more entries is worth several thousand more postings.
        g(Items.APPLE, "Apples", 6, Tier.COMMON);
        g(Items.SWEET_BERRIES, "Sweet Berries", 4, Tier.COMMON);
        g(Items.GLOW_BERRIES, "Glow Berries", 8, Tier.COMMON);
        g(Items.HONEY_BOTTLE, "Honey Bottles", 14, Tier.USEFUL);
        g(Items.MILK_BUCKET, "Milk", 12, Tier.USEFUL);
        g(Items.BREAD, "Bread", 6, Tier.COMMON);
        g(Items.COOKED_BEEF, "Steak", 10, Tier.COMMON);
        g(Items.COOKED_PORKCHOP, "Cooked Pork", 10, Tier.COMMON);
        g(Items.COOKED_CHICKEN, "Cooked Chicken", 8, Tier.COMMON);
        g(Items.COOKED_MUTTON, "Cooked Mutton", 8, Tier.COMMON);
        g(Items.PUFFERFISH, "Pufferfish", 20, Tier.USEFUL);
        g(Items.TROPICAL_FISH, "Tropical Fish", 16, Tier.USEFUL);
        g(Items.RED_MUSHROOM, "Red Mushrooms", 5, Tier.COMMON);
        g(Items.BROWN_MUSHROOM, "Brown Mushrooms", 5, Tier.COMMON);
        g(Items.CACTUS, "Cactus", 4, Tier.COMMON);
        g(Items.VINE, "Vines", 4, Tier.COMMON);
        g(Items.LILY_PAD, "Lily Pads", 8, Tier.COMMON);
        g(Items.BONE_MEAL, "Bone Meal", 3, Tier.SCRAP);
        g(Items.WHEAT_SEEDS, "Wheat Seeds", 2, Tier.SCRAP);
        g(Items.TERRACOTTA, "Terracotta", 4, Tier.COMMON);
        g(Items.BRICK, "Bricks", 6, Tier.COMMON);
        g(Items.GLASS, "Glass", 4, Tier.COMMON);
        g(Items.NETHERRACK, "Netherrack", 2, Tier.SCRAP);
        g(Items.BASALT, "Basalt", 4, Tier.COMMON);
        g(Items.BLACKSTONE, "Blackstone", 5, Tier.COMMON);
        g(Items.END_STONE, "End Stone", 8, Tier.COMMON);
        g(Items.PURPUR_BLOCK, "Purpur", 14, Tier.USEFUL);
        g(Items.CHORUS_FRUIT, "Chorus Fruit", 18, Tier.USEFUL);
        g(Items.POPPED_CHORUS_FRUIT, "Popped Chorus", 22, Tier.USEFUL);
        g(Items.DEEPSLATE, "Deepslate", 3, Tier.SCRAP);
        g(Items.CALCITE, "Calcite", 6, Tier.COMMON);
        g(Items.DRIPSTONE_BLOCK, "Dripstone", 6, Tier.COMMON);
        g(Items.POINTED_DRIPSTONE, "Pointed Dripstone", 8, Tier.COMMON);
        g(Items.RAIL, "Rails", 12, Tier.USEFUL);
        g(Items.REPEATER, "Repeaters", 20, Tier.USEFUL);
        g(Items.COMPARATOR, "Comparators", 26, Tier.USEFUL);
        g(Items.PISTON, "Pistons", 30, Tier.USEFUL);
        g(Items.OBSERVER, "Observers", 34, Tier.USEFUL);
        g(Items.HOPPER, "Hoppers", 60, Tier.USEFUL);
        g(Items.TNT, "TNT", 45, Tier.USEFUL);
        g(Items.LODESTONE, "Lodestones", 300, Tier.VALUABLE);
        g(Items.BELL, "Bells", 260, Tier.VALUABLE);
        g(Items.ANVIL, "Anvils", 200, Tier.VALUABLE);
        g(Items.ENCHANTING_TABLE, "Enchanting Tables", 340, Tier.VALUABLE);
        g(Items.SADDLE, "Saddles", 150, Tier.VALUABLE);
        g(Items.NAME_TAG, "Name Tags", 180, Tier.VALUABLE);
        g(Items.GOLDEN_CARROT, "Golden Carrots", 40, Tier.USEFUL);
        g(Items.GLISTERING_MELON_SLICE, "Glistering Melon", 38, Tier.USEFUL);
        g(Items.FERMENTED_SPIDER_EYE, "Fermented Spider Eyes", 24, Tier.USEFUL);
        g(Items.SUGAR, "Sugar", 4, Tier.SCRAP);
        g(Items.GLASS_BOTTLE, "Glass Bottles", 6, Tier.COMMON);
        g(Items.CAULDRON, "Cauldrons", 90, Tier.VALUABLE);
    }

    public static int size() {
        return GOODS.size();
    }

    public static Good byIndex(int i) {
        if (GOODS.isEmpty()) return null;
        return GOODS.get(Math.floorMod(i, GOODS.size()));
    }

    public static List<Good> all() {
        return new ArrayList<>(GOODS);
    }

    /**
     * How many of a good a commission at a given step should want.
     *
     * <p>Steps are shared across every good so the same nine brackets produce very different asks:
     * step eight of dirt is a stack and a half, step eight of netherite blocks is the one nobody
     * finishes. The board is more interesting when the extremes are reachable by the same rule.</p>
     */
    public static int countFor(Good good, int step) {
        int s = Math.floorMod(step, 9);
        int[] scrap = {16, 32, 64, 128, 192, 256, 384, 512, 768};
        int[] common = {8, 16, 32, 64, 96, 128, 192, 256, 384};
        int[] useful = {4, 8, 16, 24, 32, 48, 64, 96, 128};
        int[] valuable = {2, 4, 8, 12, 16, 24, 32, 48, 64};
        int[] precious = {1, 2, 3, 5, 8, 12, 16, 24, 32};
        int[] exotic = {1, 2, 3, 4, 6, 8, 12, 16, 24};
        int[] mythic = {1, 2, 3, 4, 8, 16, 24, 48, 64};

        int[] table = switch (good.tier()) {
            case SCRAP -> scrap;
            case COMMON -> common;
            case USEFUL -> useful;
            case VALUABLE -> valuable;
            case PRECIOUS -> precious;
            case EXOTIC -> exotic;
            case MYTHIC -> mythic;
        };
        return table[s];
    }

    public static final int STEPS = 9;
}
