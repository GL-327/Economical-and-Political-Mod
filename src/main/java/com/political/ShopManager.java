package com.political;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;

import java.util.*;

public class ShopManager {

    // Player balances (UUID -> coins)
    private static final Map<String, Long> playerBalances = new HashMap<>();

    // Item prices (sell price - buy price is 10x)
    private static final Map<Item, Integer> SELL_PRICES = new LinkedHashMap<>();

    // Default prices snapshot (set once at initialization, never mutated)
    private static final Map<Item, Integer> DEFAULT_PRICES = new LinkedHashMap<>();

    // Shop categories
    public static final Map<String, List<Item>> CATEGORIES = new LinkedHashMap<>();

    // Custom price overrides
    private static final Map<Item, Integer> customPriceOverrides = new LinkedHashMap<>();

    // Items disabled by operators
    private static final Set<Item> disabledItems = new HashSet<>();

    static {
        initializeCategories();
        initializePrices();
    }

    private static void initializeCategories() {
        // ===== BUILDING BLOCKS =====
        List<Item> buildingBlocks = Arrays.asList(
                Items.STONE, Items.COBBLESTONE, Items.MOSSY_COBBLESTONE, Items.GRANITE, Items.POLISHED_GRANITE,
                Items.DIORITE, Items.POLISHED_DIORITE, Items.ANDESITE, Items.POLISHED_ANDESITE,
                Items.DEEPSLATE, Items.COBBLED_DEEPSLATE, Items.POLISHED_DEEPSLATE, Items.CALCITE, Items.TUFF,
                Items.DRIPSTONE_BLOCK, Items.DIRT, Items.GRASS_BLOCK, Items.PODZOL, Items.MYCELIUM, Items.MUD,
                Items.CLAY, Items.CLAY_BALL, Items.BRICK, Items.BRICKS,
                Items.SAND, Items.RED_SAND, Items.SANDSTONE, Items.RED_SANDSTONE, Items.GRAVEL,
                Items.GLASS, Items.TINTED_GLASS, Items.GLOWSTONE, Items.SEA_LANTERN, Items.SHROOMLIGHT,
                Items.OBSIDIAN, Items.CRYING_OBSIDIAN,
                // Logs & Planks
                Items.OAK_LOG, Items.SPRUCE_LOG, Items.BIRCH_LOG, Items.JUNGLE_LOG, Items.ACACIA_LOG, Items.DARK_OAK_LOG,
                Items.MANGROVE_LOG, Items.CHERRY_LOG, Items.CRIMSON_STEM, Items.WARPED_STEM,
                Items.OAK_PLANKS, Items.SPRUCE_PLANKS, Items.BIRCH_PLANKS, Items.JUNGLE_PLANKS,
                Items.ACACIA_PLANKS, Items.DARK_OAK_PLANKS, Items.MANGROVE_PLANKS, Items.CHERRY_PLANKS,
                Items.BAMBOO_PLANKS, Items.CRIMSON_PLANKS, Items.WARPED_PLANKS,
                // Nether blocks
                Items.NETHERRACK, Items.NETHER_BRICKS, Items.RED_NETHER_BRICKS, Items.BASALT, Items.POLISHED_BASALT,
                Items.SMOOTH_BASALT, Items.BLACKSTONE, Items.POLISHED_BLACKSTONE,
                // End blocks
                Items.END_STONE, Items.END_STONE_BRICKS, Items.PURPUR_BLOCK, Items.PURPUR_PILLAR,
                // Ocean blocks
                Items.PRISMARINE, Items.PRISMARINE_BRICKS, Items.DARK_PRISMARINE,
                // Quartz
                Items.QUARTZ_BLOCK, Items.SMOOTH_QUARTZ, Items.QUARTZ_BRICKS, Items.QUARTZ_PILLAR,
                // Special blocks
                Items.COPPER_BLOCK, Items.EXPOSED_COPPER, Items.WEATHERED_COPPER, Items.OXIDIZED_COPPER
                // Ore blocks - REMOVED to prevent dupes (these are in Ores & Materials category)
                // Material blocks (iron/gold/diamond/emerald/lapis/redstone/netherite blocks) - REMOVED - see Material Blocks category
        );
        CATEGORIES.put("Building Blocks", buildingBlocks);

        // ===== ORES & MATERIALS =====
        List<Item> oresMaterials = Arrays.asList(
                Items.COAL, Items.CHARCOAL, Items.RAW_IRON, Items.RAW_COPPER, Items.RAW_GOLD,
                Items.IRON_INGOT, Items.COPPER_INGOT, Items.GOLD_INGOT, Items.IRON_NUGGET, Items.GOLD_NUGGET,
                Items.DIAMOND, Items.EMERALD, Items.LAPIS_LAZULI, Items.REDSTONE, Items.QUARTZ,
                Items.AMETHYST_SHARD, Items.ECHO_SHARD, Items.FLINT
                // Ore blocks REMOVED - see Material Blocks category
        );
        CATEGORIES.put("Ores & Materials", oresMaterials);

        // ===== FOOD =====
        List<Item> food = Arrays.asList(
                Items.APPLE, Items.GOLDEN_APPLE,
                Items.BREAD, Items.WHEAT, Items.WHEAT_SEEDS,
                Items.CARROT, Items.GOLDEN_CARROT, Items.POTATO, Items.BAKED_POTATO, Items.POISONOUS_POTATO,
                Items.BEETROOT, Items.BEETROOT_SEEDS, Items.BEETROOT_SOUP,
                Items.MELON_SLICE, Items.MELON, Items.GLISTERING_MELON_SLICE,
                Items.PUMPKIN, Items.PUMPKIN_PIE, Items.CARVED_PUMPKIN,
                Items.SWEET_BERRIES, Items.GLOW_BERRIES, Items.CHORUS_FRUIT,
                Items.BEEF, Items.COOKED_BEEF, Items.PORKCHOP, Items.COOKED_PORKCHOP,
                Items.CHICKEN, Items.COOKED_CHICKEN, Items.MUTTON, Items.COOKED_MUTTON,
                Items.RABBIT, Items.COOKED_RABBIT, Items.RABBIT_STEW,
                Items.COD, Items.COOKED_COD, Items.SALMON, Items.COOKED_SALMON,
                Items.TROPICAL_FISH, Items.PUFFERFISH,
                Items.MUSHROOM_STEW, Items.SUSPICIOUS_STEW, Items.COOKIE, Items.CAKE,
                Items.HONEY_BOTTLE, Items.DRIED_KELP, Items.ROTTEN_FLESH, Items.SPIDER_EYE,
                Items.SUGAR, Items.SUGAR_CANE, Items.COCOA_BEANS, Items.EGG, Items.MILK_BUCKET
        );
        CATEGORIES.put("Food", food);

        // ===== MOB DROPS =====
        List<Item> mobDrops = Arrays.asList(
                Items.STRING, Items.FEATHER, Items.LEATHER, Items.RABBIT_HIDE, Items.RABBIT_FOOT,
                Items.BONE, Items.BONE_MEAL, Items.BONE_BLOCK,
                Items.GUNPOWDER, Items.SLIME_BALL, Items.SLIME_BLOCK,
                Items.ENDER_PEARL, Items.ENDER_EYE,
                Items.BLAZE_ROD, Items.BLAZE_POWDER, Items.GHAST_TEAR, Items.MAGMA_CREAM, Items.MAGMA_BLOCK,
                Items.PRISMARINE_SHARD, Items.PRISMARINE_CRYSTALS, Items.SHULKER_SHELL, Items.PHANTOM_MEMBRANE,
                Items.DRAGON_BREATH, Items.INK_SAC, Items.GLOW_INK_SAC,
                Items.HONEYCOMB, Items.HONEYCOMB_BLOCK, Items.FERMENTED_SPIDER_EYE,
                Items.TURTLE_SCUTE, Items.ARMADILLO_SCUTE, Items.GOAT_HORN,
                Items.EXPERIENCE_BOTTLE,
                Items.NAUTILUS_SHELL, Items.TURTLE_EGG,
                Items.WHITE_WOOL, Items.BLACK_WOOL, Items.COBWEB
        );
        CATEGORIES.put("Mob Drops", mobDrops);

        // ===== REDSTONE =====
        List<Item> redstone = Arrays.asList(
                Items.REDSTONE, Items.REDSTONE_BLOCK, Items.REDSTONE_TORCH, Items.REDSTONE_LAMP,
                Items.REPEATER, Items.COMPARATOR, Items.PISTON, Items.STICKY_PISTON,
                Items.SLIME_BLOCK, Items.HONEY_BLOCK, Items.OBSERVER, Items.HOPPER,
                Items.DROPPER, Items.DISPENSER, Items.LEVER,
                Items.STONE_BUTTON, Items.OAK_BUTTON,
                Items.STONE_PRESSURE_PLATE, Items.OAK_PRESSURE_PLATE,
                Items.LIGHT_WEIGHTED_PRESSURE_PLATE, Items.HEAVY_WEIGHTED_PRESSURE_PLATE,
                Items.TRIPWIRE_HOOK, Items.TRAPPED_CHEST, Items.TNT,
                Items.NOTE_BLOCK, Items.JUKEBOX, Items.DAYLIGHT_DETECTOR, Items.TARGET, Items.LIGHTNING_ROD,
                Items.SCULK_SENSOR, Items.CALIBRATED_SCULK_SENSOR
        );
        CATEGORIES.put("Redstone", redstone);

        // ===== TOOLS & UTILITY =====
        List<Item> toolsUtility = Arrays.asList(
                Items.SPRUCE_FENCE, Items.NETHER_BRICK_FENCE, Items.OAK_FENCE_GATE, Items.SPRUCE_FENCE_GATE,
                Items.BOOKSHELF, Items.CHISELED_BOOKSHELF, Items.LECTERN,
                Items.BREWING_STAND, Items.CAULDRON, Items.ENCHANTING_TABLE, Items.ANVIL, Items.GRINDSTONE,
                Items.STONECUTTER, Items.SMITHING_TABLE, Items.LOOM, Items.CARTOGRAPHY_TABLE, Items.FLETCHING_TABLE,
                Items.COMPOSTER, Items.BARREL, Items.SMOKER, Items.BLAST_FURNACE, Items.FURNACE,
                Items.CAMPFIRE, Items.SOUL_CAMPFIRE
        );
        CATEGORIES.put("Tools & Utility", toolsUtility);

        // ===== PLANTS (FLOWERS REMOVED) =====
        List<Item> plantsOnly = Arrays.asList(
                Items.OAK_SAPLING, Items.SPRUCE_SAPLING, Items.BIRCH_SAPLING, Items.JUNGLE_SAPLING,
                Items.ACACIA_SAPLING, Items.DARK_OAK_SAPLING, Items.CHERRY_SAPLING, Items.MANGROVE_PROPAGULE,
                Items.OAK_LEAVES, Items.SPRUCE_LEAVES, Items.BIRCH_LEAVES,
                Items.AZALEA, Items.FLOWERING_AZALEA, Items.MOSS_BLOCK, Items.MOSS_CARPET, Items.VINE, Items.GLOW_LICHEN,
                Items.SCULK, Items.SCULK_VEIN, Items.LILY_PAD, Items.SEAGRASS, Items.KELP, Items.DRIED_KELP_BLOCK,
                Items.SEA_PICKLE, Items.CACTUS, Items.BAMBOO, Items.DEAD_BUSH, Items.FERN, Items.LARGE_FERN,
                Items.SHORT_GRASS, Items.TALL_GRASS,
                Items.BROWN_MUSHROOM, Items.RED_MUSHROOM, Items.CRIMSON_FUNGUS, Items.WARPED_FUNGUS,
                Items.NETHER_WART, Items.SOUL_SAND, Items.CHORUS_FLOWER
        );
        CATEGORIES.put("Plants", plantsOnly);

        // ===== BREWING & POTIONS =====
        List<Item> brewing = Arrays.asList(
                Items.GLASS_BOTTLE, Items.WATER_BUCKET, Items.GLOWSTONE_DUST
        );
        CATEGORIES.put("Brewing", brewing);

        // ===== DYES & DECORATION =====
        List<Item> dyesDecoration = Arrays.asList(
                Items.WHITE_DYE, Items.ORANGE_DYE, Items.MAGENTA_DYE, Items.LIGHT_BLUE_DYE,
                Items.YELLOW_DYE, Items.LIME_DYE, Items.PINK_DYE, Items.GRAY_DYE, Items.LIGHT_GRAY_DYE,
                Items.CYAN_DYE, Items.PURPLE_DYE, Items.BLUE_DYE, Items.BROWN_DYE, Items.GREEN_DYE,
                Items.RED_DYE, Items.BLACK_DYE,
                Items.WHITE_CONCRETE, Items.ORANGE_CONCRETE, Items.MAGENTA_CONCRETE, Items.LIGHT_BLUE_CONCRETE,
                Items.YELLOW_CONCRETE, Items.LIME_CONCRETE, Items.PINK_CONCRETE, Items.GRAY_CONCRETE,
                Items.LIGHT_GRAY_CONCRETE, Items.CYAN_CONCRETE, Items.PURPLE_CONCRETE, Items.BLUE_CONCRETE,
                Items.BROWN_CONCRETE, Items.GREEN_CONCRETE, Items.RED_CONCRETE, Items.BLACK_CONCRETE,
                Items.WHITE_CONCRETE_POWDER, Items.BLACK_CONCRETE_POWDER,
                Items.TERRACOTTA, Items.WHITE_TERRACOTTA, Items.ORANGE_TERRACOTTA, Items.BLACK_TERRACOTTA,
                Items.WHITE_GLAZED_TERRACOTTA, Items.BLACK_GLAZED_TERRACOTTA,
                Items.WHITE_STAINED_GLASS, Items.BLACK_STAINED_GLASS
        );
        CATEGORIES.put("Dyes & Decoration", dyesDecoration);

        // ===== MISCELLANEOUS =====
        List<Item> miscellaneous = Arrays.asList(
                Items.BUCKET, Items.LAVA_BUCKET, Items.POWDER_SNOW_BUCKET,
                Items.AXOLOTL_BUCKET, Items.TADPOLE_BUCKET, Items.COD_BUCKET, Items.SALMON_BUCKET,
                Items.TROPICAL_FISH_BUCKET, Items.PUFFERFISH_BUCKET,
                Items.BOOK, Items.WRITABLE_BOOK, Items.ENCHANTED_BOOK, Items.KNOWLEDGE_BOOK, Items.PAPER,
                Items.MAP, Items.FILLED_MAP,
                Items.MINECART, Items.CHEST_MINECART, Items.FURNACE_MINECART, Items.TNT_MINECART, Items.HOPPER_MINECART,
                Items.OAK_BOAT, Items.OAK_CHEST_BOAT, Items.BAMBOO_RAFT,
                Items.RAIL, Items.POWERED_RAIL, Items.DETECTOR_RAIL, Items.ACTIVATOR_RAIL,
                Items.CHEST, Items.ENDER_CHEST, Items.SHULKER_BOX, Items.WHITE_SHULKER_BOX, Items.BLACK_SHULKER_BOX,
                Items.BUNDLE, Items.RECOVERY_COMPASS, Items.SPONGE, Items.WET_SPONGE,
                Items.RESPAWN_ANCHOR, Items.LODESTONE,
                Items.SPAWNER // Silk touch pickaxe required to mine
        );
        CATEGORIES.put("Miscellaneous", miscellaneous);

        // ===== FARMING & NATURAL (Vanilla - Cheap) =====
        // Note: Logs are in Building Blocks, Flowers are in Plants & Flowers
        List<Item> farmingItems = Arrays.asList(
                // Crops only (no duplicates)
                Items.WHEAT,
                Items.CARROT,
                Items.POTATO,
                Items.BEETROOT,
                Items.SUGAR_CANE,
                Items.PUMPKIN,
                Items.MELON_SLICE
        );
        CATEGORIES.put("Farming & Natural", farmingItems);

        // Note: Block Armor, Block Tools, and Block Weapons are custom items
        // They are not sold as vanilla items - players craft them via /recipes

        // ===== WOOL & CARPETS =====
        List<Item> woolCarpets = Arrays.asList(
                Items.WHITE_WOOL, Items.ORANGE_WOOL, Items.MAGENTA_WOOL, Items.LIGHT_BLUE_WOOL, Items.YELLOW_WOOL, Items.LIME_WOOL, Items.PINK_WOOL, Items.GRAY_WOOL, Items.LIGHT_GRAY_WOOL, Items.CYAN_WOOL, Items.PURPLE_WOOL, Items.BLUE_WOOL, Items.BROWN_WOOL, Items.GREEN_WOOL, Items.RED_WOOL, Items.BLACK_WOOL,
                Items.WHITE_CARPET, Items.ORANGE_CARPET, Items.MAGENTA_CARPET, Items.LIGHT_BLUE_CARPET, Items.YELLOW_CARPET, Items.LIME_CARPET, Items.PINK_CARPET, Items.GRAY_CARPET, Items.LIGHT_GRAY_CARPET, Items.CYAN_CARPET, Items.PURPLE_CARPET, Items.BLUE_CARPET, Items.BROWN_CARPET, Items.GREEN_CARPET, Items.RED_CARPET, Items.BLACK_CARPET
        );
        CATEGORIES.put("Wool & Carpets", woolCarpets);

        // ===== HEADS & BANNERS =====
        List<Item> headsBanners = Arrays.asList(
                Items.PLAYER_HEAD, Items.ZOMBIE_HEAD, Items.SKELETON_SKULL, Items.WITHER_SKELETON_SKULL, Items.CREEPER_HEAD, Items.PIGLIN_HEAD, Items.DRAGON_HEAD,
                Items.WHITE_BANNER, Items.BLACK_BANNER, Items.RED_BANNER, Items.BLUE_BANNER
        );
        CATEGORIES.put("Heads & Banners", headsBanners);

        // ===== COMPACTED ITEMS (REMOVED - Dupe Prevention) =====
        // Compacted items removed to prevent crafting/selling dupe exploits
        
        // ===== SUPER COMPACTED (REMOVED - Dupe Prevention) =====
        // Super compacted items removed to prevent crafting/selling dupe exploits

        // ===== ENCHANTED ITEMS (REMOVED - Dupe Prevention) =====
        // Enchanted items removed to prevent potential dupe exploits
    }

    private static void initializePrices() {
        SELL_PRICES.clear();
        
        // ===== BUILDING BLOCKS (common, low value) =====
        setPrice(Items.STONE, 1);
        setPrice(Items.COBBLESTONE, 1);
        setPrice(Items.MOSSY_COBBLESTONE, 2);
        setPrice(Items.GRANITE, 1);
        setPrice(Items.POLISHED_GRANITE, 2);
        setPrice(Items.DIORITE, 1);
        setPrice(Items.POLISHED_DIORITE, 2);
        setPrice(Items.ANDESITE, 1);
        setPrice(Items.POLISHED_ANDESITE, 2);
        setPrice(Items.DEEPSLATE, 1);
        setPrice(Items.COBBLED_DEEPSLATE, 1);
        setPrice(Items.POLISHED_DEEPSLATE, 2);
        setPrice(Items.CALCITE, 3);
        setPrice(Items.TUFF, 1);
        setPrice(Items.DRIPSTONE_BLOCK, 2);
        setPrice(Items.DIRT, 1);
        setPrice(Items.GRASS_BLOCK, 2);
        setPrice(Items.PODZOL, 3);
        setPrice(Items.MYCELIUM, 3);
        setPrice(Items.MUD, 2);
        setPrice(Items.CLAY, 2);
        setPrice(Items.CLAY_BALL, 1);
        setPrice(Items.BRICK, 3);
        setPrice(Items.BRICKS, 12);
        setPrice(Items.SAND, 1);
        setPrice(Items.RED_SAND, 2);
        setPrice(Items.SANDSTONE, 4);
        setPrice(Items.RED_SANDSTONE, 6);
        setPrice(Items.GRAVEL, 1);

        // Logs & Planks
        setPrice(Items.OAK_LOG, 2);
        setPrice(Items.SPRUCE_LOG, 2);
        setPrice(Items.BIRCH_LOG, 2);
        setPrice(Items.JUNGLE_LOG, 2);
        setPrice(Items.ACACIA_LOG, 3);
        setPrice(Items.DARK_OAK_LOG, 3);
        setPrice(Items.MANGROVE_LOG, 3);
        setPrice(Items.CHERRY_LOG, 4);
        setPrice(Items.CRIMSON_STEM, 3);
        setPrice(Items.WARPED_STEM, 3);
        setPrice(Items.OAK_PLANKS, 1);  // 1 log = 4 planks, so sell price lower to prevent dupe
        setPrice(Items.SPRUCE_PLANKS, 1);
        setPrice(Items.BIRCH_PLANKS, 1);
        setPrice(Items.JUNGLE_PLANKS, 1);
        setPrice(Items.ACACIA_PLANKS, 1);
        setPrice(Items.DARK_OAK_PLANKS, 1);
        setPrice(Items.MANGROVE_PLANKS, 1);
        setPrice(Items.CHERRY_PLANKS, 1);
        setPrice(Items.BAMBOO_PLANKS, 1);
        setPrice(Items.CRIMSON_PLANKS, 1);
        setPrice(Items.WARPED_PLANKS, 1);

        // Glass & Light sources
        setPrice(Items.GLASS, 2);
        setPrice(Items.TINTED_GLASS, 4);
        setPrice(Items.GLOWSTONE, 8);
        setPrice(Items.SEA_LANTERN, 15);
        setPrice(Items.SHROOMLIGHT, 10);
        setPrice(Items.OBSIDIAN, 5);
        setPrice(Items.CRYING_OBSIDIAN, 15);

        // Nether blocks
        setPrice(Items.NETHERRACK, 1);
        setPrice(Items.NETHER_BRICKS, 2);
        setPrice(Items.RED_NETHER_BRICKS, 3);
        setPrice(Items.BASALT, 2);
        setPrice(Items.POLISHED_BASALT, 4);
        setPrice(Items.SMOOTH_BASALT, 4);
        setPrice(Items.BLACKSTONE, 3);
        setPrice(Items.POLISHED_BLACKSTONE, 5);

        // End blocks
        setPrice(Items.END_STONE, 3);
        setPrice(Items.END_STONE_BRICKS, 12);
        setPrice(Items.PURPUR_BLOCK, 5);
        setPrice(Items.PURPUR_PILLAR, 5);

        // Ocean blocks
        setPrice(Items.PRISMARINE, 4);
        setPrice(Items.PRISMARINE_BRICKS, 8);
        setPrice(Items.DARK_PRISMARINE, 6);

        // Quartz
        setPrice(Items.QUARTZ_BLOCK, 16);
        setPrice(Items.SMOOTH_QUARTZ, 20);
        setPrice(Items.QUARTZ_BRICKS, 20);
        setPrice(Items.QUARTZ_PILLAR, 16);

        // Copper blocks
        setPrice(Items.COPPER_BLOCK, 45);
        setPrice(Items.EXPOSED_COPPER, 45);
        setPrice(Items.WEATHERED_COPPER, 45);
        setPrice(Items.OXIDIZED_COPPER, 45);

        // ===== ORES & RAW MATERIALS =====
        setPrice(Items.COAL, 2);
        setPrice(Items.CHARCOAL, 2);
        setPrice(Items.RAW_IRON, 5);
        setPrice(Items.RAW_COPPER, 2);
        setPrice(Items.RAW_GOLD, 15);
        setPrice(Items.IRON_INGOT, 15);
        setPrice(Items.COPPER_INGOT, 5);
        setPrice(Items.GOLD_INGOT, 45);
        setPrice(Items.IRON_NUGGET, 2);  // 9 nuggets = 1 ingot, priced lower to prevent dupe
        setPrice(Items.GOLD_NUGGET, 5);
        setPrice(Items.DIAMOND, 100);
        setPrice(Items.EMERALD, 80);
        setPrice(Items.LAPIS_LAZULI, 4);
        setPrice(Items.REDSTONE, 4);
        setPrice(Items.QUARTZ, 4);
        setPrice(Items.AMETHYST_SHARD, 3);
        setPrice(Items.ECHO_SHARD, 50);
        setPrice(Items.FLINT, 1);

        // Ore blocks (9 ingots each)
        setPrice(Items.COAL_BLOCK, 18);
        setPrice(Items.IRON_BLOCK, 135);
        setPrice(Items.COPPER_BLOCK, 45);
        setPrice(Items.GOLD_BLOCK, 405);
        setPrice(Items.DIAMOND_BLOCK, 900);
        setPrice(Items.EMERALD_BLOCK, 720);
        setPrice(Items.LAPIS_BLOCK, 36);
        setPrice(Items.REDSTONE_BLOCK, 36);
        setPrice(Items.AMETHYST_BLOCK, 27);
        
        // ===== FOOD =====
        setPrice(Items.APPLE, 3);
        setPrice(Items.GOLDEN_APPLE, 500);
        setPrice(Items.BREAD, 4);
        setPrice(Items.WHEAT, 1);
        setPrice(Items.WHEAT_SEEDS, 1);
        setPrice(Items.CARROT, 1);
        setPrice(Items.GOLDEN_CARROT, 300);
        setPrice(Items.POTATO, 1);
        setPrice(Items.BAKED_POTATO, 2);
        setPrice(Items.POISONOUS_POTATO, 1);
        setPrice(Items.BEETROOT, 1);
        setPrice(Items.BEETROOT_SEEDS, 1);
        setPrice(Items.BEETROOT_SOUP, 6);
        setPrice(Items.MELON_SLICE, 1);
        setPrice(Items.MELON, 9);
        setPrice(Items.GLISTERING_MELON_SLICE, 20);
        setPrice(Items.PUMPKIN, 2);
        setPrice(Items.PUMPKIN_PIE, 8);
        setPrice(Items.CARVED_PUMPKIN, 2);
        setPrice(Items.SWEET_BERRIES, 1);
        setPrice(Items.GLOW_BERRIES, 2);
        setPrice(Items.CHORUS_FRUIT, 3);
        setPrice(Items.BEEF, 3);
        setPrice(Items.COOKED_BEEF, 8);
        setPrice(Items.PORKCHOP, 3);
        setPrice(Items.COOKED_PORKCHOP, 8);
        setPrice(Items.CHICKEN, 2);
        setPrice(Items.COOKED_CHICKEN, 6);
        setPrice(Items.MUTTON, 2);
        setPrice(Items.COOKED_MUTTON, 6);
        setPrice(Items.RABBIT, 2);
        setPrice(Items.COOKED_RABBIT, 5);
        setPrice(Items.RABBIT_STEW, 15);
        setPrice(Items.COD, 2);
        setPrice(Items.COOKED_COD, 5);
        setPrice(Items.SALMON, 2);
        setPrice(Items.COOKED_SALMON, 5);
        setPrice(Items.TROPICAL_FISH, 5);
        setPrice(Items.PUFFERFISH, 3);
        setPrice(Items.MUSHROOM_STEW, 6);
        setPrice(Items.SUSPICIOUS_STEW, 8);
        setPrice(Items.COOKIE, 2);
        setPrice(Items.CAKE, 20);
        setPrice(Items.HONEY_BOTTLE, 5);
        setPrice(Items.DRIED_KELP, 1);
        setPrice(Items.ROTTEN_FLESH, 1);
        setPrice(Items.SPIDER_EYE, 2);
        setPrice(Items.SUGAR, 1);
        setPrice(Items.SUGAR_CANE, 1);
        setPrice(Items.COCOA_BEANS, 2);
        setPrice(Items.EGG, 2);
        setPrice(Items.MILK_BUCKET, 15);

        // ===== MOB DROPS =====
        setPrice(Items.STRING, 1);
        setPrice(Items.FEATHER, 1);
        setPrice(Items.LEATHER, 3);
        setPrice(Items.RABBIT_HIDE, 2);
        setPrice(Items.RABBIT_FOOT, 50);
        setPrice(Items.BONE, 1);
        setPrice(Items.BONE_MEAL, 1);
        setPrice(Items.BONE_BLOCK, 9);
        setPrice(Items.GUNPOWDER, 5);
        setPrice(Items.SLIME_BALL, 10);
        setPrice(Items.SLIME_BLOCK, 90);
        setPrice(Items.ENDER_PEARL, 50);
        setPrice(Items.ENDER_EYE, 150);
        setPrice(Items.BLAZE_ROD, 20);
        setPrice(Items.BLAZE_POWDER, 10);
        setPrice(Items.GHAST_TEAR, 100);
        setPrice(Items.MAGMA_CREAM, 15);
        setPrice(Items.MAGMA_BLOCK, 60);
        setPrice(Items.PRISMARINE_SHARD, 3);
        setPrice(Items.PRISMARINE_CRYSTALS, 8);
        setPrice(Items.SHULKER_SHELL, 100);
        setPrice(Items.PHANTOM_MEMBRANE, 30);
        setPrice(Items.DRAGON_BREATH, 500);
        setPrice(Items.INK_SAC, 2);
        setPrice(Items.GLOW_INK_SAC, 10);
        setPrice(Items.HONEYCOMB, 3);
        setPrice(Items.HONEYCOMB_BLOCK, 27);
        setPrice(Items.FERMENTED_SPIDER_EYE, 5);
        setPrice(Items.TURTLE_SCUTE, 50);
        setPrice(Items.ARMADILLO_SCUTE, 30);
        setPrice(Items.GOAT_HORN, 40);
        setPrice(Items.EXPERIENCE_BOTTLE, 30);
        setPrice(Items.NAUTILUS_SHELL, 80);
        setPrice(Items.TURTLE_EGG, 30);
        setPrice(Items.WHITE_WOOL, 2);
        setPrice(Items.BLACK_WOOL, 2);
        setPrice(Items.COBWEB, 15);

        // ===== REDSTONE =====
        setPrice(Items.REDSTONE, 4);
        setPrice(Items.REDSTONE_BLOCK, 36);
        setPrice(Items.REDSTONE_TORCH, 5);
        setPrice(Items.REDSTONE_LAMP, 15);
        setPrice(Items.REPEATER, 15);
        setPrice(Items.COMPARATOR, 25);
        setPrice(Items.PISTON, 20);
        setPrice(Items.STICKY_PISTON, 50);
        setPrice(Items.SLIME_BLOCK, 90);
        setPrice(Items.HONEY_BLOCK, 30);
        setPrice(Items.OBSERVER, 25);
        setPrice(Items.HOPPER, 50);
        setPrice(Items.DROPPER, 15);
        setPrice(Items.DISPENSER, 15);
        setPrice(Items.LEVER, 1);
        setPrice(Items.STONE_BUTTON, 1);
        setPrice(Items.OAK_BUTTON, 1);
        setPrice(Items.STONE_PRESSURE_PLATE, 2);
        setPrice(Items.OAK_PRESSURE_PLATE, 1);
        setPrice(Items.LIGHT_WEIGHTED_PRESSURE_PLATE, 90);
        setPrice(Items.HEAVY_WEIGHTED_PRESSURE_PLATE, 30);
        setPrice(Items.TRIPWIRE_HOOK, 10);
        setPrice(Items.TRAPPED_CHEST, 55);
        setPrice(Items.TNT, 50);
        setPrice(Items.NOTE_BLOCK, 15);
        setPrice(Items.JUKEBOX, 50);
        setPrice(Items.DAYLIGHT_DETECTOR, 20);
        setPrice(Items.TARGET, 25);
        setPrice(Items.LIGHTNING_ROD, 15);
        setPrice(Items.SCULK_SENSOR, 30);
        setPrice(Items.CALIBRATED_SCULK_SENSOR, 60);

        // ===== TOOLS & UTILITY =====
        setPrice(Items.SPRUCE_FENCE, 3);
        setPrice(Items.NETHER_BRICK_FENCE, 3);
        setPrice(Items.OAK_FENCE_GATE, 5);
        setPrice(Items.SPRUCE_FENCE_GATE, 5);
        setPrice(Items.BOOKSHELF, 20);
        setPrice(Items.CHISELED_BOOKSHELF, 25);
        setPrice(Items.LECTERN, 25);
        setPrice(Items.BREWING_STAND, 50);
        setPrice(Items.CAULDRON, 20);
        setPrice(Items.ENCHANTING_TABLE, 200);
        setPrice(Items.ANVIL, 150);
        setPrice(Items.GRINDSTONE, 30);
        setPrice(Items.STONECUTTER, 15);
        setPrice(Items.SMITHING_TABLE, 20);
        setPrice(Items.LOOM, 15);
        setPrice(Items.CARTOGRAPHY_TABLE, 15);
        setPrice(Items.FLETCHING_TABLE, 15);
        setPrice(Items.COMPOSTER, 10);
        setPrice(Items.BARREL, 10);
        setPrice(Items.SMOKER, 15);
        setPrice(Items.BLAST_FURNACE, 15);
        setPrice(Items.FURNACE, 10);
        setPrice(Items.CAMPFIRE, 15);
        setPrice(Items.SOUL_CAMPFIRE, 30);

        // ===== PLANTS =====
        setPrice(Items.OAK_SAPLING, 2);
        setPrice(Items.SPRUCE_SAPLING, 2);
        setPrice(Items.BIRCH_SAPLING, 2);
        setPrice(Items.JUNGLE_SAPLING, 2);
        setPrice(Items.ACACIA_SAPLING, 3);
        setPrice(Items.DARK_OAK_SAPLING, 3);
        setPrice(Items.CHERRY_SAPLING, 5);
        setPrice(Items.MANGROVE_PROPAGULE, 3);
        setPrice(Items.OAK_LEAVES, 1);
        setPrice(Items.SPRUCE_LEAVES, 1);
        setPrice(Items.BIRCH_LEAVES, 1);
        setPrice(Items.AZALEA, 3);
        setPrice(Items.FLOWERING_AZALEA, 5);
        setPrice(Items.MOSS_BLOCK, 3);
        setPrice(Items.MOSS_CARPET, 1);
        setPrice(Items.VINE, 2);
        setPrice(Items.GLOW_LICHEN, 5);
        setPrice(Items.SCULK, 5);
        setPrice(Items.SCULK_VEIN, 2);
        setPrice(Items.LILY_PAD, 5);
        setPrice(Items.SEAGRASS, 1);
        setPrice(Items.KELP, 1);
        setPrice(Items.DRIED_KELP_BLOCK, 9);
        setPrice(Items.SEA_PICKLE, 3);
        setPrice(Items.CACTUS, 2);
        setPrice(Items.BAMBOO, 1);
        setPrice(Items.DEAD_BUSH, 1);
        setPrice(Items.FERN, 1);
        setPrice(Items.LARGE_FERN, 2);
        setPrice(Items.SHORT_GRASS, 1);
        setPrice(Items.TALL_GRASS, 2);
        setPrice(Items.BROWN_MUSHROOM, 2);
        setPrice(Items.RED_MUSHROOM, 2);
        setPrice(Items.CRIMSON_FUNGUS, 3);
        setPrice(Items.WARPED_FUNGUS, 3);
        setPrice(Items.NETHER_WART, 10);
        setPrice(Items.CHORUS_FLOWER, 10);

        // ===== BREWING =====
        setPrice(Items.GLASS_BOTTLE, 2);
        setPrice(Items.WATER_BUCKET, 15);
        setPrice(Items.GLOWSTONE_DUST, 8);

        // ===== DYES =====
        setPrice(Items.WHITE_DYE, 2);
        setPrice(Items.ORANGE_DYE, 2);
        setPrice(Items.MAGENTA_DYE, 2);
        setPrice(Items.LIGHT_BLUE_DYE, 2);
        setPrice(Items.YELLOW_DYE, 2);
        setPrice(Items.LIME_DYE, 2);
        setPrice(Items.PINK_DYE, 2);
        setPrice(Items.GRAY_DYE, 2);
        setPrice(Items.LIGHT_GRAY_DYE, 2);
        setPrice(Items.CYAN_DYE, 2);
        setPrice(Items.PURPLE_DYE, 2);
        setPrice(Items.BLUE_DYE, 2);
        setPrice(Items.BROWN_DYE, 2);
        setPrice(Items.GREEN_DYE, 2);
        setPrice(Items.RED_DYE, 2);
        setPrice(Items.BLACK_DYE, 2);
        setPrice(Items.WHITE_CONCRETE, 3);
        setPrice(Items.ORANGE_CONCRETE, 3);
        setPrice(Items.MAGENTA_CONCRETE, 3);
        setPrice(Items.LIGHT_BLUE_CONCRETE, 3);
        setPrice(Items.YELLOW_CONCRETE, 3);
        setPrice(Items.LIME_CONCRETE, 3);
        setPrice(Items.PINK_CONCRETE, 3);
        setPrice(Items.GRAY_CONCRETE, 3);
        setPrice(Items.LIGHT_GRAY_CONCRETE, 3);
        setPrice(Items.CYAN_CONCRETE, 3);
        setPrice(Items.PURPLE_CONCRETE, 3);
        setPrice(Items.BLUE_CONCRETE, 3);
        setPrice(Items.BROWN_CONCRETE, 3);
        setPrice(Items.GREEN_CONCRETE, 3);
        setPrice(Items.RED_CONCRETE, 3);
        setPrice(Items.BLACK_CONCRETE, 3);
        setPrice(Items.WHITE_CONCRETE_POWDER, 2);
        setPrice(Items.BLACK_CONCRETE_POWDER, 2);
        setPrice(Items.TERRACOTTA, 2);
        setPrice(Items.WHITE_TERRACOTTA, 3);
        setPrice(Items.ORANGE_TERRACOTTA, 3);
        setPrice(Items.BLACK_TERRACOTTA, 3);
        setPrice(Items.WHITE_GLAZED_TERRACOTTA, 5);
        setPrice(Items.BLACK_GLAZED_TERRACOTTA, 5);
        setPrice(Items.WHITE_STAINED_GLASS, 3);
        setPrice(Items.BLACK_STAINED_GLASS, 3);

        // ===== MISCELLANEOUS =====
        setPrice(Items.BUCKET, 15);
        setPrice(Items.LAVA_BUCKET, 30);
        setPrice(Items.POWDER_SNOW_BUCKET, 20);
        setPrice(Items.AXOLOTL_BUCKET, 150);
        setPrice(Items.TADPOLE_BUCKET, 30);
        setPrice(Items.COD_BUCKET, 20);
        setPrice(Items.SALMON_BUCKET, 25);
        setPrice(Items.TROPICAL_FISH_BUCKET, 50);
        setPrice(Items.PUFFERFISH_BUCKET, 40);
        setPrice(Items.BOOK, 10);
        setPrice(Items.WRITABLE_BOOK, 30);
        setPrice(Items.ENCHANTED_BOOK, 150);
        setPrice(Items.KNOWLEDGE_BOOK, 50);
        setPrice(Items.PAPER, 2);
        setPrice(Items.MAP, 15);
        setPrice(Items.FILLED_MAP, 20);
        setPrice(Items.MINECART, 20);
        setPrice(Items.CHEST_MINECART, 30);
        setPrice(Items.FURNACE_MINECART, 35);
        setPrice(Items.TNT_MINECART, 70);
        setPrice(Items.HOPPER_MINECART, 70);
        setPrice(Items.OAK_BOAT, 5);
        setPrice(Items.OAK_CHEST_BOAT, 15);
        setPrice(Items.BAMBOO_RAFT, 5);
        setPrice(Items.RAIL, 5);
        setPrice(Items.POWERED_RAIL, 25);
        setPrice(Items.DETECTOR_RAIL, 15);
        setPrice(Items.ACTIVATOR_RAIL, 15);
        setPrice(Items.CHEST, 5);
        setPrice(Items.ENDER_CHEST, 300);
        setPrice(Items.SHULKER_BOX, 250);  // 2 shells + chest = 200+5, priced higher
        setPrice(Items.WHITE_SHULKER_BOX, 252);
        setPrice(Items.BLACK_SHULKER_BOX, 252);
        setPrice(Items.BUNDLE, 30);
        setPrice(Items.RECOVERY_COMPASS, 200);
        setPrice(Items.SPONGE, 150);
        setPrice(Items.WET_SPONGE, 100);
        setPrice(Items.RESPAWN_ANCHOR, 400);
        setPrice(Items.LODESTONE, 500);
        setPrice(Items.SPAWNER, 1_000_000); // 1M - requires silk touch to mine

        // ===== WOOL & CARPETS =====
        setPrice(Items.ORANGE_WOOL, 2);
        setPrice(Items.MAGENTA_WOOL, 2);
        setPrice(Items.LIGHT_BLUE_WOOL, 2);
        setPrice(Items.YELLOW_WOOL, 2);
        setPrice(Items.LIME_WOOL, 2);
        setPrice(Items.PINK_WOOL, 2);
        setPrice(Items.GRAY_WOOL, 2);
        setPrice(Items.LIGHT_GRAY_WOOL, 2);
        setPrice(Items.CYAN_WOOL, 2);
        setPrice(Items.PURPLE_WOOL, 2);
        setPrice(Items.BLUE_WOOL, 2);
        setPrice(Items.BROWN_WOOL, 2);
        setPrice(Items.GREEN_WOOL, 2);
        setPrice(Items.RED_WOOL, 2);
        setPrice(Items.WHITE_CARPET, 1);
        setPrice(Items.ORANGE_CARPET, 1);
        setPrice(Items.MAGENTA_CARPET, 1);
        setPrice(Items.LIGHT_BLUE_CARPET, 1);
        setPrice(Items.YELLOW_CARPET, 1);
        setPrice(Items.LIME_CARPET, 1);
        setPrice(Items.PINK_CARPET, 1);
        setPrice(Items.GRAY_CARPET, 1);
        setPrice(Items.LIGHT_GRAY_CARPET, 1);
        setPrice(Items.CYAN_CARPET, 1);
        setPrice(Items.PURPLE_CARPET, 1);
        setPrice(Items.BLUE_CARPET, 1);
        setPrice(Items.BROWN_CARPET, 1);
        setPrice(Items.GREEN_CARPET, 1);
        setPrice(Items.RED_CARPET, 1);
        setPrice(Items.BLACK_CARPET, 1);

        // ===== HEADS & BANNERS =====
        setPrice(Items.PLAYER_HEAD, 100);
        setPrice(Items.ZOMBIE_HEAD, 75);
        setPrice(Items.SKELETON_SKULL, 50);
        setPrice(Items.WITHER_SKELETON_SKULL, 500);
        setPrice(Items.CREEPER_HEAD, 75);
        setPrice(Items.PIGLIN_HEAD, 75);
        setPrice(Items.DRAGON_HEAD, 5000);
        setPrice(Items.WHITE_BANNER, 5);
        setPrice(Items.BLACK_BANNER, 5);
        setPrice(Items.RED_BANNER, 5);
        setPrice(Items.BLUE_BANNER, 5);

        // ===== CUSTOM ITEMS - COMPACTED MATERIALS (REMOVED - Dupe Prevention) =====
        // Compacted items pricing removed to prevent crafting/selling dupe exploits
        
        // ===== CUSTOM ITEMS - SUPER COMPACTED MATERIALS (REMOVED - Dupe Prevention) =====
        // Super compacted items pricing removed to prevent crafting/selling dupe exploits

        // ===== CUSTOM ITEMS - ENCHANTED MATERIALS =====
        // Premium materials - heavily reduced to prevent dupes
        setCustomPrice(SlayerItems.createEnchantedBlackstone().getItem(), 50);       // 90% reduction
        setCustomPrice(SlayerItems.createEnchantedGildedBlackstone().getItem(), 80); // 90% reduction
        
        // Custom Enchanted Blocks - heavily reduced
        setCustomPrice(SlayerItems.createEnchantedCobblestone().getItem(), 6);      // 91% reduction
        setCustomPrice(SlayerItems.createEnchantedStone().getItem(), 12);          // 91% reduction
        setCustomPrice(SlayerItems.createEnchantedIronBlock().getItem(), 25);       // 90% reduction
        setCustomPrice(SlayerItems.createEnchantedGoldBlock().getItem(), 51);       // 90% reduction
        setCustomPrice(SlayerItems.createEnchantedDiamondBlock().getItem(), 102);   // 90% reduction
        setCustomPrice(SlayerItems.createEnchantedOakLog().getItem(), 6);          // 91% reduction
        setCustomPrice(SlayerItems.createEnchantedRose().getItem(), 12);           // 91% reduction
        setCustomPrice(SlayerItems.createEnchantedDandelion().getItem(), 12);      // 91% reduction
        setCustomPrice(SlayerItems.createEnchantedWheat().getItem(), 6);            // 91% reduction
        setCustomPrice(SlayerItems.createEnchantedCarrot().getItem(), 6);          // 91% reduction
        setCustomPrice(SlayerItems.createEnchantedPotato().getItem(), 6);          // 91% reduction
        
        // New Enchanted Blocks - heavily reduced to prevent dupes
        setCustomPrice(SlayerItems.createEnchantedEmeraldBlock().getItem(), 86);     // 90% reduction
        setCustomPrice(SlayerItems.createEnchantedLapisBlock().getItem(), 23);      // 91% reduction
        setCustomPrice(SlayerItems.createEnchantedCoalBlock().getItem(), 6);        // 91% reduction
        setCustomPrice(SlayerItems.createEnchantedCopperBlock().getItem(), 13);      // 90% reduction
                setCustomPrice(SlayerItems.createEnchantedQuartzBlock().getItem(), 10);      // 91% reduction
        setCustomPrice(SlayerItems.createEnchantedRedstoneBlock().getItem(), 13);    // 90% reduction
        setCustomPrice(SlayerItems.createEnchantedSand().getItem(), 6);              // 91% reduction
        setCustomPrice(SlayerItems.createEnchantedRedSand().getItem(), 6);          // 91% reduction
        setCustomPrice(SlayerItems.createEnchantedGravel().getItem(), 6);            // 91% reduction
        setCustomPrice(SlayerItems.createEnchantedNetherrack().getItem(), 6);        // 91% reduction
        setCustomPrice(SlayerItems.createEnchantedEndStone().getItem(), 6);          // 91% reduction
        setCustomPrice(SlayerItems.createEnchantedObsidian().getItem(), 13);         // 90% reduction
        setCustomPrice(SlayerItems.createEnchantedGlowstone().getItem(), 13);        // 90% reduction
        setCustomPrice(SlayerItems.createEnchantedPrismarine().getItem(), 13);       // 90% reduction
        setCustomPrice(SlayerItems.createEnchantedBasalt().getItem(), 6);            // 91% reduction
        setCustomPrice(SlayerItems.createEnchantedDeepslate().getItem(), 6);         // 91% reduction
        setCustomPrice(SlayerItems.createEnchantedCobbledDeepslate().getItem(), 6);  // 91% reduction
        setCustomPrice(SlayerItems.createEnchantedTuff().getItem(), 6);              // 91% reduction
        setCustomPrice(SlayerItems.createEnchantedCalcite().getItem(), 6);           // 91% reduction
        setCustomPrice(SlayerItems.createEnchantedAmethystBlock().getItem(), 25);    // 90% reduction
        setCustomPrice(SlayerItems.createEnchantedGlass().getItem(), 6);             // 91% reduction
        setCustomPrice(SlayerItems.createEnchantedIce().getItem(), 6);               // 91% reduction
        setCustomPrice(SlayerItems.createEnchantedPackedIce().getItem(), 6);         // 91% reduction
        setCustomPrice(SlayerItems.createEnchantedBlueIce().getItem(), 13);          // 90% reduction
        setCustomPrice(SlayerItems.createEnchantedTerracotta().getItem(), 6);        // 91% reduction
        setCustomPrice(SlayerItems.createEnchantedClay().getItem(), 6);              // 91% reduction
        setCustomPrice(SlayerItems.createEnchantedBrick().getItem(), 6);             // 91% reduction
        setCustomPrice(SlayerItems.createEnchantedNetherBrick().getItem(), 6);       // 91% reduction
        setCustomPrice(SlayerItems.createEnchantedPurpurBlock().getItem(), 6);        // 91% reduction
        setCustomPrice(SlayerItems.createEnchantedSeaLantern().getItem(), 13);        // 90% reduction
        setCustomPrice(SlayerItems.createEnchantedShroomlight().getItem(), 13);       // 90% reduction

        // Snapshot defaults the first time (when DEFAULT_PRICES is empty)
        if (DEFAULT_PRICES.isEmpty()) {
            DEFAULT_PRICES.putAll(SELL_PRICES);
        }

        // ===== DISABLED ITEMS (cannot be bought on shop) =====
        // Disable gilded blackstone
        disableItem(Items.GILDED_BLACKSTONE);
        // Remove netherite ingots from /shop (prevents ingot conversions)
        disableItem(Items.NETHERITE_INGOT);
        
    }

    private static void setPrice(Item item, int sellPrice) {
        SELL_PRICES.put(item, sellPrice);
    }

    // ===== OPERATOR PRICE CONTROL =====

    public static void setCustomPrice(Item item, int sellPrice) {
        customPriceOverrides.put(item, sellPrice);
        SELL_PRICES.put(item, sellPrice);
    }

    public static void disableItem(Item item) {
        disabledItems.add(item);
    }

    public static void enableItem(Item item) {
        disabledItems.remove(item);
    }

    public static boolean isDisabled(Item item) {
        return disabledItems.contains(item);
    }

    public static void resetPrices() {
        customPriceOverrides.clear();
        disabledItems.clear();
        initializePrices();
    }

    /** Resets a single item's price to its default (removes custom override and re-enables it). */
    public static void resetItemPrice(Item item) {
        customPriceOverrides.remove(item);
        disabledItems.remove(item);
        // Restore the original default price, or remove if item had no default
        if (DEFAULT_PRICES.containsKey(item)) {
            SELL_PRICES.put(item, DEFAULT_PRICES.get(item));
        } else {
            SELL_PRICES.remove(item);
        }
    }

    public static Map<Item, Integer> getCustomOverrides() {
        return new LinkedHashMap<>(customPriceOverrides);
    }

    public static Set<Item> getDisabledItems() {
        return new HashSet<>(disabledItems);
    }

    // ===== BALANCE MANAGEMENT =====

    public static void setBalance(String uuid, long balance) {
        playerBalances.put(uuid, Math.max(0, balance));
    }

    public static Map<String, Long> getAllBalances() {
        return new HashMap<>(playerBalances);
    }

    public static void setAllBalances(Map<String, Long> balances) {
        playerBalances.clear();
        playerBalances.putAll(balances);
    }

    // ===== PRICE QUERIES =====

    public static int getSellPrice(Item item) {
        return SELL_PRICES.getOrDefault(item, 1);
    }

    public static int getBuyPrice(Item item) {
        int sellPrice = getSellPrice(item);
        return sellPrice > 0 ? sellPrice * 10 : 0;
    }

    public static boolean canBeSold(Item item) {
        if (isDisabled(item)) return false;
        return SELL_PRICES.containsKey(item) && SELL_PRICES.get(item) > 0;
    }

    public static boolean canBeBought(Item item) {
        return canBeSold(item);
    }

    public static List<String> getCategoryNames() {
        return new ArrayList<>(CATEGORIES.keySet());
    }

    public static List<Item> getItemsInCategory(String category) {
        return new ArrayList<>(CATEGORIES.getOrDefault(category, new ArrayList<>()));
    }

    // ===== TRANSACTION METHODS =====

    public static boolean sellItem(ServerPlayer player, Item item, int quantity) {
        int sellPrice = getSellPrice(item);
        if (sellPrice <= 0) {
            player.sendSystemMessage(Component.literal("This item cannot be sold!").withStyle(ChatFormatting.RED));
            return false;
        }

        // Check if player has valid vanilla items to sell
        int validCount = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack invStack = player.getInventory().getItem(i);
            if (invStack.isEmpty()) continue;
            
            // Check for bounty items - never allowed
            if (CustomItemHandler.isBountyItem(invStack)) {
                continue;
            }
            
            // Only count vanilla items with no custom names or IDs
            if (invStack.getItem() == item && invStack.get(DataComponents.CUSTOM_NAME) == null 
                    && SlayerItems.getCustomItemId(invStack) == null) {
                validCount += invStack.getCount();
            }
        }

        if (validCount < quantity) {
            player.sendSystemMessage(Component.literal("You don't have enough of this item!").withStyle(ChatFormatting.RED));
            return false;
        }

        // Remove the vanilla items
        int remaining = quantity;
        for (int i = 0; i < player.getInventory().getContainerSize() && remaining > 0; i++) {
            ItemStack invStack = player.getInventory().getItem(i);
            if (invStack.isEmpty()) continue;
            
            // Only remove vanilla items
            if (invStack.getItem() == item && invStack.get(DataComponents.CUSTOM_NAME) == null 
                    && SlayerItems.getCustomItemId(invStack) == null) {
                int toRemove = Math.min(remaining, invStack.getCount());
                invStack.shrink(toRemove);
                remaining -= toRemove;
            }
        }
        
        // TYCOON buff: +10% sell price bonus; Heart tree: +12% at max fortune branch
        String playerUuid = player.getUUID().toString();
        double sellBonus = PlayerBuffManager.getShopSellBonus(playerUuid) + MasteryHeartTree.shopSellBonus(playerUuid);
        sellBonus += PartyPerkEffects.shopSellMultiplier(playerUuid) - 1.0;
        int totalValue = (int) Math.ceil(sellPrice * quantity * (1.0 + sellBonus));
        
        CoinManager.giveCoinsQuiet(player, totalValue);

        player.sendSystemMessage(Component.literal("Sold " + quantity + "x " + getItemName(item) + " for ")
                .withStyle(ChatFormatting.GREEN)
                .append(Component.literal(formatCoins(totalValue)).withStyle(ChatFormatting.GOLD)));

        return true;
    }

    public static boolean buyItem(ServerPlayer player, Item item, int quantity) {
        int buyPrice = getBuyPrice(item);
        if (buyPrice <= 0) {
            player.sendSystemMessage(Component.literal("This item cannot be bought!").withStyle(ChatFormatting.RED));
            return false;
        }

        int totalCost = (int) Math.ceil(buyPrice * quantity * PartyPerkEffects.shopBuyMultiplier(player.getUUID().toString()));

        if (!CoinManager.hasCoins(player, totalCost)) {
            player.sendSystemMessage(Component.literal("You don't have enough coins!").withStyle(ChatFormatting.RED));
            return false;
        }

        if (!hasInventorySpace(player, item, quantity)) {
            player.sendSystemMessage(Component.literal("You don't have enough inventory space!").withStyle(ChatFormatting.RED));
            return false;
        }

        CoinManager.removeCoins(player, totalCost);
        giveItems(player, item, quantity);

        player.sendSystemMessage(Component.literal("Bought " + quantity + "x " + getItemName(item) + " for ")
                .withStyle(ChatFormatting.GREEN)
                .append(Component.literal(formatCoins(totalCost)).withStyle(ChatFormatting.GOLD)));

        return true;
    }

    // ===== HELPER METHODS =====

    public static int countItems(ServerPlayer player, Item item) {
        int count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            var stack = player.getInventory().getItem(i);
            if (stack.getItem() == item) {
                count += stack.getCount();
            }
        }
        return count;
    }

    public static void removeItems(ServerPlayer player, Item item, int quantity) {
        int remaining = quantity;
        for (int i = 0; i < player.getInventory().getContainerSize() && remaining > 0; i++) {
            var stack = player.getInventory().getItem(i);
            if (stack.getItem() == item) {
                int toRemove = Math.min(remaining, stack.getCount());
                stack.shrink(toRemove);
                remaining -= toRemove;
            }
        }
    }
    
    public static void giveItems(ServerPlayer player, Item item, int quantity) {
        int remaining = quantity;
        int maxStack = item.getDefaultMaxStackSize();

        // Shop only gives vanilla items
        while (remaining > 0) {
            int toGive = Math.min(remaining, maxStack);
            ItemStack stack = new net.minecraft.world.item.ItemStack(item, toGive);
            player.addItem(stack);
            remaining -= toGive;
        }
    }

    public static boolean hasInventorySpace(ServerPlayer player, Item item, int quantity) {
        int space = 0;
        int maxStack = item.getDefaultMaxStackSize();

        for (int i = 0; i < 36; i++) {
            var stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) {
                space += maxStack;
            } else if (stack.getItem() == item && stack.getCount() < maxStack) {
                space += maxStack - stack.getCount();
            }
        }

        return space >= quantity;
    }

    public static String getItemName(Item item) {
        Identifier id = ItemRegistryIds.asId(item);
        String name = id.getPath();
        String[] words = name.split("_");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase());
                // Add space between words (except for last word)
                if (words.length > 1 && !word.equals(words[words.length - 1])) {
                    result.append(" ");
                }
            }
        }
        return result.toString();
    }

    public static String formatCoins(int coins) {
        if (coins >= 1000000) {
            return String.format("%.1fM", coins / 1000000.0);
        } else if (coins >= 1000) {
            return String.format("%.1fK", coins / 1000.0);
        } else {
            return String.valueOf(coins);
        }
    }
}
