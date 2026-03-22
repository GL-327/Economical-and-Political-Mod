package com.political;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.*;

public class ShopManager {

    // Player balances (UUID -> coins)
    private static final Map<String, Long> playerBalances = new HashMap<>();

    // Item prices (sell price - buy price is 3x)
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
                Items.NETHERITE_SCRAP, Items.NETHERITE_INGOT, Items.AMETHYST_SHARD, Items.ECHO_SHARD, Items.FLINT
                // Ore blocks REMOVED - see Material Blocks category
        );
        CATEGORIES.put("Ores & Materials", oresMaterials);

        // ===== FOOD =====
        List<Item> food = Arrays.asList(
                Items.APPLE, Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE,
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
                Items.NETHER_STAR, Items.WITHER_SKELETON_SKULL,
                Items.PRISMARINE_SHARD, Items.PRISMARINE_CRYSTALS, Items.SHULKER_SHELL, Items.PHANTOM_MEMBRANE,
                Items.DRAGON_BREATH, Items.INK_SAC, Items.GLOW_INK_SAC,
                Items.HONEYCOMB, Items.HONEYCOMB_BLOCK, Items.FERMENTED_SPIDER_EYE,
                Items.TURTLE_SCUTE, Items.ARMADILLO_SCUTE, Items.GOAT_HORN,
                Items.EXPERIENCE_BOTTLE, Items.TOTEM_OF_UNDYING, Items.TRIDENT,
                Items.NAUTILUS_SHELL, Items.HEART_OF_THE_SEA, Items.TURTLE_EGG,
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

        // ===== PLANTS & FLOWERS =====
        List<Item> plantsFlowers = Arrays.asList(
                Items.OAK_SAPLING, Items.SPRUCE_SAPLING, Items.BIRCH_SAPLING, Items.JUNGLE_SAPLING,
                Items.ACACIA_SAPLING, Items.DARK_OAK_SAPLING, Items.CHERRY_SAPLING, Items.MANGROVE_PROPAGULE,
                Items.OAK_LEAVES, Items.SPRUCE_LEAVES, Items.BIRCH_LEAVES,
                Items.AZALEA, Items.FLOWERING_AZALEA, Items.MOSS_BLOCK, Items.MOSS_CARPET, Items.VINE, Items.GLOW_LICHEN,
                Items.SCULK, Items.SCULK_VEIN, Items.LILY_PAD, Items.SEAGRASS, Items.KELP, Items.DRIED_KELP_BLOCK,
                Items.SEA_PICKLE, Items.CACTUS, Items.BAMBOO, Items.DEAD_BUSH, Items.FERN, Items.LARGE_FERN,
                Items.SHORT_GRASS, Items.TALL_GRASS,
                Items.DANDELION, Items.POPPY, Items.BLUE_ORCHID, Items.ALLIUM, Items.AZURE_BLUET,
                Items.RED_TULIP, Items.ORANGE_TULIP, Items.PINK_TULIP, Items.WHITE_TULIP, Items.OXEYE_DAISY,
                Items.CORNFLOWER, Items.LILY_OF_THE_VALLEY, Items.WITHER_ROSE, Items.TORCHFLOWER, Items.PITCHER_PLANT,
                Items.SPORE_BLOSSOM, Items.SUNFLOWER, Items.LILAC, Items.ROSE_BUSH, Items.PEONY, Items.PINK_PETALS,
                Items.BROWN_MUSHROOM, Items.RED_MUSHROOM, Items.CRIMSON_FUNGUS, Items.WARPED_FUNGUS,
                Items.NETHER_WART, Items.CHORUS_FLOWER
        );
        CATEGORIES.put("Plants & Flowers", plantsFlowers);

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
                Items.CONDUIT, Items.BEACON, Items.END_CRYSTAL, Items.RESPAWN_ANCHOR, Items.LODESTONE
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

        // ===== COMPACTED ITEMS (REMOVED - Dupe Prevention) =====
        // Compacted items removed to prevent crafting/selling dupe exploits
        
        // ===== SUPER COMPACTED (REMOVED - Dupe Prevention) =====
        // Super compacted items removed to prevent crafting/selling dupe exploits

        // ===== ENCHANTED ITEMS (REMOVED - Dupe Prevention) =====
        // Enchanted items removed to prevent potential dupe exploits
    }

    private static void initializePrices() {
        SELL_PRICES.clear();
        // ===== BUILDING BLOCKS (very cheap) =====
        setPrice(Items.STONE, 1);
        setPrice(Items.COBBLESTONE, 1);
        setPrice(Items.MOSSY_COBBLESTONE, 1);
        setPrice(Items.GRANITE, 1);
        setPrice(Items.POLISHED_GRANITE, 1);
        setPrice(Items.DIORITE, 1);
        setPrice(Items.POLISHED_DIORITE, 1);
        setPrice(Items.ANDESITE, 1);
        setPrice(Items.POLISHED_ANDESITE, 1);
        setPrice(Items.DEEPSLATE, 1);
        setPrice(Items.COBBLED_DEEPSLATE, 1);
        setPrice(Items.POLISHED_DEEPSLATE, 1);
        setPrice(Items.CALCITE, 1);
        setPrice(Items.TUFF, 1);
        setPrice(Items.DRIPSTONE_BLOCK, 1);
        setPrice(Items.DIRT, 1);
        setPrice(Items.GRASS_BLOCK, 1);
        setPrice(Items.PODZOL, 1);
        setPrice(Items.MYCELIUM, 2);
        setPrice(Items.MUD, 1);
        setPrice(Items.CLAY, 1);
        setPrice(Items.CLAY_BALL, 1);
        setPrice(Items.BRICK, 1);
        setPrice(Items.BRICKS, 2);
        setPrice(Items.SAND, 1);
        setPrice(Items.RED_SAND, 1);
        setPrice(Items.SANDSTONE, 1);
        setPrice(Items.RED_SANDSTONE, 1);
        setPrice(Items.GRAVEL, 1);

        // Logs & Planks
        setPrice(Items.OAK_LOG, 1);
        setPrice(Items.SPRUCE_LOG, 1);
        setPrice(Items.BIRCH_LOG, 1);
        setPrice(Items.JUNGLE_LOG, 1);
        setPrice(Items.ACACIA_LOG, 1);
        setPrice(Items.DARK_OAK_LOG, 1);
        setPrice(Items.MANGROVE_LOG, 1);
        setPrice(Items.CHERRY_LOG, 1);
        setPrice(Items.CRIMSON_STEM, 2);
        setPrice(Items.WARPED_STEM, 2);
        setPrice(Items.OAK_PLANKS, 0);  // Cannot sell - prevents log conversion dupe (1 log = 4 planks)
        setPrice(Items.SPRUCE_PLANKS, 0);  // Cannot sell
        setPrice(Items.BIRCH_PLANKS, 0);  // Cannot sell
        setPrice(Items.JUNGLE_PLANKS, 0);  // Cannot sell
        setPrice(Items.ACACIA_PLANKS, 0);  // Cannot sell
        setPrice(Items.DARK_OAK_PLANKS, 0);  // Cannot sell
        setPrice(Items.MANGROVE_PLANKS, 0);  // Cannot sell
        setPrice(Items.CHERRY_PLANKS, 0);  // Cannot sell
        setPrice(Items.BAMBOO_PLANKS, 0);  // Cannot sell
        setPrice(Items.CRIMSON_PLANKS, 1);
        setPrice(Items.WARPED_PLANKS, 1);

        // Glass & Light sources
        setPrice(Items.GLASS, 1);
        setPrice(Items.TINTED_GLASS, 3);
        setPrice(Items.GLOWSTONE, 5);
        setPrice(Items.SEA_LANTERN, 6);
        setPrice(Items.SHROOMLIGHT, 5);
        setPrice(Items.OBSIDIAN, 3);
        setPrice(Items.CRYING_OBSIDIAN, 10);

        // Nether blocks
        setPrice(Items.NETHERRACK, 1);
        setPrice(Items.NETHER_BRICKS, 1);
        setPrice(Items.RED_NETHER_BRICKS, 2);
        setPrice(Items.BASALT, 1);
        setPrice(Items.POLISHED_BASALT, 1);
        setPrice(Items.SMOOTH_BASALT, 1);
        setPrice(Items.BLACKSTONE, 1);
        setPrice(Items.POLISHED_BLACKSTONE, 1);

        // End blocks
        setPrice(Items.END_STONE, 2);
        setPrice(Items.END_STONE_BRICKS, 2);
        setPrice(Items.PURPUR_BLOCK, 3);
        setPrice(Items.PURPUR_PILLAR, 3);

        // Ocean blocks
        setPrice(Items.PRISMARINE, 3);
        setPrice(Items.PRISMARINE_BRICKS, 4);
        setPrice(Items.DARK_PRISMARINE, 5);

        // Quartz
        setPrice(Items.QUARTZ_BLOCK, 4);
        setPrice(Items.SMOOTH_QUARTZ, 4);
        setPrice(Items.QUARTZ_BRICKS, 4);
        setPrice(Items.QUARTZ_PILLAR, 4);

        // Special blocks
        setPrice(Items.AMETHYST_BLOCK, 6);
        setPrice(Items.BUDDING_AMETHYST, 100);
        setPrice(Items.COPPER_BLOCK, 18);
        setPrice(Items.EXPOSED_COPPER, 18);
        setPrice(Items.WEATHERED_COPPER, 18);
        setPrice(Items.OXIDIZED_COPPER, 18);

        // ===== ORES & RAW MATERIALS =====
        setPrice(Items.COAL, 1);
        setPrice(Items.CHARCOAL, 1);
        setPrice(Items.RAW_IRON, 4);   // 4 > 3 (iron ingot) - prevents smelt dupe
        setPrice(Items.RAW_COPPER, 3); // 3 > 2 (copper ingot) - prevents smelt dupe
        setPrice(Items.RAW_GOLD, 5);   // 5 > 1 (gold ingot) - prevents smelt dupe
        setPrice(Items.IRON_INGOT, 3);
        setPrice(Items.COPPER_INGOT, 2);
        setPrice(Items.GOLD_INGOT, 1);
        setPrice(Items.IRON_NUGGET, 0);  // Cannot sell - prevents ingot conversion dupe
        setPrice(Items.GOLD_NUGGET, 0);  // Cannot sell - prevents ingot conversion dupe
        setPrice(Items.DIAMOND, 50);
        setPrice(Items.EMERALD, 30);
        setPrice(Items.LAPIS_LAZULI, 4);
        setPrice(Items.REDSTONE, 2);
        setPrice(Items.QUARTZ, 2);
        setPrice(Items.NETHERITE_SCRAP, 150);
        setPrice(Items.NETHERITE_INGOT, 700);
        setPrice(Items.AMETHYST_SHARD, 2);
        setPrice(Items.ECHO_SHARD, 100);
        setPrice(Items.FLINT, 1);

        // Ore blocks - balanced pricing (9x base material cost, no crafting exploit)
        // Set to exactly 9x the ingot price to prevent crafting dupes
        setPrice(Items.COAL_BLOCK, 9);           // 9 coal = 9 coins - no profit
        setPrice(Items.IRON_BLOCK, 27);          // 9 iron = 27 coins - no profit
        setPrice(Items.COPPER_BLOCK, 18);        // 9 copper = 18 coins - no profit
        setPrice(Items.GOLD_BLOCK, 9);           // 9 gold = 9 coins - no profit (was 81!)
        setPrice(Items.DIAMOND_BLOCK, 450);      // 9 diamonds = 450 coins - no profit
        setPrice(Items.EMERALD_BLOCK, 270);      // 9 emeralds = 270 coins - no profit
        setPrice(Items.LAPIS_BLOCK, 36);         // 9 lapis = 36 coins - no profit
        setPrice(Items.REDSTONE_BLOCK, 18);      // 9 redstone = 18 coins - no profit
        setPrice(Items.NETHERITE_BLOCK, 6300);   // 9 netherite = 6300 coins (was 6300, ok)

        // ===== FOOD =====
        setPrice(Items.APPLE, 2);
        setPrice(Items.GOLDEN_APPLE, 10);
        setPrice(Items.ENCHANTED_GOLDEN_APPLE, 2000);
        setPrice(Items.BREAD, 2);
        setPrice(Items.WHEAT, 1);
        setPrice(Items.WHEAT_SEEDS, 1);
        setPrice(Items.CARROT, 1);
        setPrice(Items.GOLDEN_CARROT, 9);
        setPrice(Items.POTATO, 1);
        setPrice(Items.BAKED_POTATO, 1);
        setPrice(Items.POISONOUS_POTATO, 1);
        setPrice(Items.BEETROOT, 1);
        setPrice(Items.BEETROOT_SEEDS, 1);
        setPrice(Items.BEETROOT_SOUP, 3);
        setPrice(Items.MELON_SLICE, 1);
        setPrice(Items.MELON, 3);
        setPrice(Items.GLISTERING_MELON_SLICE, 15);
        setPrice(Items.PUMPKIN, 2);
        setPrice(Items.PUMPKIN_PIE, 4);
        setPrice(Items.CARVED_PUMPKIN, 2);
        setPrice(Items.SWEET_BERRIES, 1);
        setPrice(Items.GLOW_BERRIES, 2);
        setPrice(Items.CHORUS_FRUIT, 3);
        setPrice(Items.BEEF, 2);
        setPrice(Items.COOKED_BEEF, 3);
        setPrice(Items.PORKCHOP, 2);
        setPrice(Items.COOKED_PORKCHOP, 3);
        setPrice(Items.CHICKEN, 1);
        setPrice(Items.COOKED_CHICKEN, 2);
        setPrice(Items.MUTTON, 1);
        setPrice(Items.COOKED_MUTTON, 2);
        setPrice(Items.RABBIT, 1);
        setPrice(Items.COOKED_RABBIT, 2);
        setPrice(Items.RABBIT_STEW, 7);
        setPrice(Items.COD, 1);
        setPrice(Items.COOKED_COD, 2);
        setPrice(Items.SALMON, 2);
        setPrice(Items.COOKED_SALMON, 3);
        setPrice(Items.TROPICAL_FISH, 5);
        setPrice(Items.PUFFERFISH, 6);
        setPrice(Items.MUSHROOM_STEW, 3);
        setPrice(Items.SUSPICIOUS_STEW, 10);
        setPrice(Items.COOKIE, 1);
        setPrice(Items.CAKE, 15);
        setPrice(Items.HONEY_BOTTLE, 5);
        setPrice(Items.DRIED_KELP, 1);
        setPrice(Items.ROTTEN_FLESH, 1);
        setPrice(Items.SPIDER_EYE, 2);
        setPrice(Items.SUGAR, 1);
        setPrice(Items.SUGAR_CANE, 1);
        setPrice(Items.COCOA_BEANS, 1);
        setPrice(Items.EGG, 1);
        setPrice(Items.MILK_BUCKET, 10);

        // ===== MOB DROPS =====
        setPrice(Items.STRING, 1);
        setPrice(Items.FEATHER, 1);
        setPrice(Items.LEATHER, 3);
        setPrice(Items.RABBIT_HIDE, 2);
        setPrice(Items.RABBIT_FOOT, 15);
        setPrice(Items.BONE, 1);
        setPrice(Items.BONE_MEAL, 1);
        setPrice(Items.BONE_BLOCK, 6);
        setPrice(Items.GUNPOWDER, 3);
        setPrice(Items.SLIME_BALL, 5);
        setPrice(Items.SLIME_BLOCK, 45);
        setPrice(Items.ENDER_PEARL, 20);
        setPrice(Items.ENDER_EYE, 40);
        setPrice(Items.BLAZE_ROD, 15);
        setPrice(Items.BLAZE_POWDER, 8);
        setPrice(Items.GHAST_TEAR, 30);
        setPrice(Items.MAGMA_CREAM, 10);
        setPrice(Items.MAGMA_BLOCK, 4);
        setPrice(Items.NETHER_STAR, 1000);
        setPrice(Items.WITHER_SKELETON_SKULL, 200);
        setPrice(Items.PRISMARINE_SHARD, 3);
        setPrice(Items.PRISMARINE_CRYSTALS, 4);
        setPrice(Items.SHULKER_SHELL, 50);
        setPrice(Items.PHANTOM_MEMBRANE, 5);
        setPrice(Items.DRAGON_BREATH, 800);
        setPrice(Items.INK_SAC, 1);
        setPrice(Items.GLOW_INK_SAC, 1);
        setPrice(Items.HONEYCOMB, 1);
        setPrice(Items.HONEYCOMB_BLOCK, 1);
        setPrice(Items.SPIDER_EYE, 2);
        setPrice(Items.FERMENTED_SPIDER_EYE, 2);
        setPrice(Items.TURTLE_SCUTE, 1);
        setPrice(Items.ARMADILLO_SCUTE, 100);
        setPrice(Items.GOAT_HORN, 10);
        setPrice(Items.EXPERIENCE_BOTTLE, 15);
        setPrice(Items.TOTEM_OF_UNDYING, 500);
        setPrice(Items.TRIDENT, 400);
        setPrice(Items.NAUTILUS_SHELL, 5);
        setPrice(Items.HEART_OF_THE_SEA, 50);
        setPrice(Items.TURTLE_EGG, 5);
        setPrice(Items.WHITE_WOOL, 1);
        setPrice(Items.BLACK_WOOL, 1);
        setPrice(Items.COBWEB, 1);

        // ===== REDSTONE =====
        setPrice(Items.REDSTONE, 2);
        setPrice(Items.REDSTONE_BLOCK, 18);
        setPrice(Items.REDSTONE_TORCH, 1);
        setPrice(Items.REDSTONE_LAMP, 6);
        setPrice(Items.REPEATER, 4);
        setPrice(Items.COMPARATOR, 4);
        setPrice(Items.PISTON, 4);
        setPrice(Items.STICKY_PISTON, 4);
        setPrice(Items.SLIME_BLOCK, 45);
        setPrice(Items.HONEY_BLOCK, 4);
        setPrice(Items.OBSERVER, 4);
        setPrice(Items.HOPPER, 6);
        setPrice(Items.DROPPER, 6);
        setPrice(Items.DISPENSER, 6);
        setPrice(Items.LEVER, 2);
        setPrice(Items.STONE_BUTTON, 1);
        setPrice(Items.OAK_BUTTON, 1);
        setPrice(Items.STONE_PRESSURE_PLATE, 1);
        setPrice(Items.OAK_PRESSURE_PLATE, 1);
        setPrice(Items.LIGHT_WEIGHTED_PRESSURE_PLATE, 4);
        setPrice(Items.HEAVY_WEIGHTED_PRESSURE_PLATE, 4);
        setPrice(Items.TRIPWIRE_HOOK, 4);
        setPrice(Items.TRAPPED_CHEST, 8);
        setPrice(Items.TNT, 4);
        setPrice(Items.NOTE_BLOCK, 4);
        setPrice(Items.JUKEBOX, 9);
        setPrice(Items.DAYLIGHT_DETECTOR, 4);
        setPrice(Items.TARGET, 4);
        setPrice(Items.LIGHTNING_ROD, 4);
        setPrice(Items.SCULK_SENSOR, 5);
        setPrice(Items.CALIBRATED_SCULK_SENSOR, 4);

        // ===== TOOLS & WEAPONS =====
        setPrice(Items.SPRUCE_FENCE, 1);
        setPrice(Items.NETHER_BRICK_FENCE, 2);
        setPrice(Items.OAK_FENCE_GATE, 2);
        setPrice(Items.SPRUCE_FENCE_GATE, 2);
        setPrice(Items.BOOKSHELF, 10);
        setPrice(Items.CHISELED_BOOKSHELF, 12);
        setPrice(Items.LECTERN, 15);
        setPrice(Items.BREWING_STAND, 20);
        setPrice(Items.CAULDRON, 22);
        setPrice(Items.ENCHANTING_TABLE, 300);
        setPrice(Items.ANVIL, 100);
        setPrice(Items.GRINDSTONE, 10);
        setPrice(Items.STONECUTTER, 11);
        setPrice(Items.SMITHING_TABLE, 15);
        setPrice(Items.LOOM, 8);
        setPrice(Items.CARTOGRAPHY_TABLE, 9);
        setPrice(Items.FLETCHING_TABLE, 9);
        setPrice(Items.COMPOSTER, 4);
        setPrice(Items.BARREL, 6);
        setPrice(Items.SMOKER, 9);
        setPrice(Items.BLAST_FURNACE, 20);
        setPrice(Items.FURNACE, 5);
        setPrice(Items.CAMPFIRE, 5);
        setPrice(Items.SOUL_CAMPFIRE, 7);

        // ===== PLANTS & FLOWERS =====
        setPrice(Items.OAK_SAPLING, 1);
        setPrice(Items.SPRUCE_SAPLING, 1);
        setPrice(Items.BIRCH_SAPLING, 1);
        setPrice(Items.JUNGLE_SAPLING, 2);
        setPrice(Items.ACACIA_SAPLING, 2);
        setPrice(Items.DARK_OAK_SAPLING, 2);
        setPrice(Items.CHERRY_SAPLING, 3);
        setPrice(Items.MANGROVE_PROPAGULE, 2);
        setPrice(Items.OAK_LEAVES, 1);
        setPrice(Items.SPRUCE_LEAVES, 1);
        setPrice(Items.BIRCH_LEAVES, 1);
        setPrice(Items.AZALEA, 3);
        setPrice(Items.FLOWERING_AZALEA, 5);
        setPrice(Items.MOSS_BLOCK, 2);
        setPrice(Items.MOSS_CARPET, 1);
        setPrice(Items.VINE, 1);
        setPrice(Items.GLOW_LICHEN, 3);
        setPrice(Items.SCULK, 5);
        setPrice(Items.SCULK_VEIN, 3);
        setPrice(Items.LILY_PAD, 1);
        setPrice(Items.SEAGRASS, 1);
        setPrice(Items.KELP, 1);
        setPrice(Items.DRIED_KELP_BLOCK, 5);
        setPrice(Items.SEA_PICKLE, 2);
        setPrice(Items.CACTUS, 1);
        setPrice(Items.BAMBOO, 1);
        setPrice(Items.DEAD_BUSH, 1);
        setPrice(Items.FERN, 1);
        setPrice(Items.LARGE_FERN, 1);
        setPrice(Items.SHORT_GRASS, 1);
        setPrice(Items.TALL_GRASS, 1);
        setPrice(Items.DANDELION, 1);
        setPrice(Items.POPPY, 1);
        setPrice(Items.BLUE_ORCHID, 2);
        setPrice(Items.ALLIUM, 1);
        setPrice(Items.AZURE_BLUET, 1);
        setPrice(Items.RED_TULIP, 1);
        setPrice(Items.ORANGE_TULIP, 1);
        setPrice(Items.PINK_TULIP, 1);
        setPrice(Items.WHITE_TULIP, 1);
        setPrice(Items.OXEYE_DAISY, 1);
        setPrice(Items.CORNFLOWER, 1);
        setPrice(Items.LILY_OF_THE_VALLEY, 1);
        setPrice(Items.WITHER_ROSE, 50);
        setPrice(Items.TORCHFLOWER, 20);
        setPrice(Items.PITCHER_PLANT, 20);
        setPrice(Items.SPORE_BLOSSOM, 30);
        setPrice(Items.SUNFLOWER, 2);
        setPrice(Items.LILAC, 1);
        setPrice(Items.ROSE_BUSH, 1);
        setPrice(Items.PEONY, 1);
        setPrice(Items.PINK_PETALS, 1);
        setPrice(Items.BROWN_MUSHROOM, 1);
        setPrice(Items.RED_MUSHROOM, 1);
        setPrice(Items.CRIMSON_FUNGUS, 2);
        setPrice(Items.WARPED_FUNGUS, 2);
        setPrice(Items.NETHER_WART, 3);
        setPrice(Items.CHORUS_FLOWER, 10);

        // ===== BREWING =====
        setPrice(Items.GLASS_BOTTLE, 1);
        setPrice(Items.WATER_BUCKET, 5);
        setPrice(Items.GLOWSTONE_DUST, 2);

        // ===== DYES =====
        setPrice(Items.WHITE_DYE, 1);
        setPrice(Items.ORANGE_DYE, 1);
        setPrice(Items.MAGENTA_DYE, 1);
        setPrice(Items.LIGHT_BLUE_DYE, 1);
        setPrice(Items.YELLOW_DYE, 1);
        setPrice(Items.LIME_DYE, 1);
        setPrice(Items.PINK_DYE, 1);
        setPrice(Items.GRAY_DYE, 1);
        setPrice(Items.LIGHT_GRAY_DYE, 1);
        setPrice(Items.CYAN_DYE, 1);
        setPrice(Items.PURPLE_DYE, 1);
        setPrice(Items.BLUE_DYE, 1);
        setPrice(Items.BROWN_DYE, 1);
        setPrice(Items.GREEN_DYE, 1);
        setPrice(Items.RED_DYE, 1);
        setPrice(Items.BLACK_DYE, 1);
        setPrice(Items.WHITE_CONCRETE, 1);
        setPrice(Items.ORANGE_CONCRETE, 1);
        setPrice(Items.MAGENTA_CONCRETE, 1);
        setPrice(Items.LIGHT_BLUE_CONCRETE, 1);
        setPrice(Items.YELLOW_CONCRETE, 1);
        setPrice(Items.LIME_CONCRETE, 1);
        setPrice(Items.PINK_CONCRETE, 1);
        setPrice(Items.GRAY_CONCRETE, 1);
        setPrice(Items.LIGHT_GRAY_CONCRETE, 1);
        setPrice(Items.CYAN_CONCRETE, 1);
        setPrice(Items.PURPLE_CONCRETE, 1);
        setPrice(Items.BLUE_CONCRETE, 1);
        setPrice(Items.BROWN_CONCRETE, 1);
        setPrice(Items.GREEN_CONCRETE, 1);
        setPrice(Items.RED_CONCRETE, 1);
        setPrice(Items.BLACK_CONCRETE, 1);
        setPrice(Items.WHITE_CONCRETE_POWDER, 1);
        setPrice(Items.BLACK_CONCRETE_POWDER, 1);
        setPrice(Items.TERRACOTTA, 1);
        setPrice(Items.WHITE_TERRACOTTA, 1);
        setPrice(Items.ORANGE_TERRACOTTA, 1);
        setPrice(Items.BLACK_TERRACOTTA, 1);
        setPrice(Items.WHITE_GLAZED_TERRACOTTA, 2);
        setPrice(Items.BLACK_GLAZED_TERRACOTTA, 2);
        setPrice(Items.WHITE_STAINED_GLASS, 1);
        setPrice(Items.BLACK_STAINED_GLASS, 1);

        // ===== MISCELLANEOUS =====
        setPrice(Items.BUCKET, 10);
        setPrice(Items.LAVA_BUCKET, 30);
        setPrice(Items.POWDER_SNOW_BUCKET, 15);
        setPrice(Items.AXOLOTL_BUCKET, 100);
        setPrice(Items.TADPOLE_BUCKET, 20);
        setPrice(Items.COD_BUCKET, 10);
        setPrice(Items.SALMON_BUCKET, 12);
        setPrice(Items.TROPICAL_FISH_BUCKET, 30);
        setPrice(Items.PUFFERFISH_BUCKET, 30);
        setPrice(Items.BOOK, 5);
        setPrice(Items.WRITABLE_BOOK, 10);
        setPrice(Items.ENCHANTED_BOOK, 100);
        setPrice(Items.KNOWLEDGE_BOOK, 20);
        setPrice(Items.PAPER, 1);
        setPrice(Items.MAP, 6);
        setPrice(Items.FILLED_MAP, 10);
        setPrice(Items.MINECART, 16);
        setPrice(Items.CHEST_MINECART, 20);
        setPrice(Items.FURNACE_MINECART, 21);
        setPrice(Items.TNT_MINECART, 36);
        setPrice(Items.HOPPER_MINECART, 36);
        setPrice(Items.OAK_BOAT, 3);
        setPrice(Items.OAK_CHEST_BOAT, 7);
        setPrice(Items.BAMBOO_RAFT, 2);
        setPrice(Items.RAIL, 4);
        setPrice(Items.POWERED_RAIL, 20);
        setPrice(Items.DETECTOR_RAIL, 10);
        setPrice(Items.ACTIVATOR_RAIL, 10);
        setPrice(Items.CHEST, 4);
        setPrice(Items.ENDER_CHEST, 200);
        setPrice(Items.SHULKER_BOX, 0);  // Cannot sell - prevents shulker box dupe (2 shells + chest = profit)
        setPrice(Items.WHITE_SHULKER_BOX, 0);  // Cannot sell
        setPrice(Items.BLACK_SHULKER_BOX, 0);  // Cannot sell
        setPrice(Items.BUNDLE, 15);
        setPrice(Items.RECOVERY_COMPASS, 120);
        setPrice(Items.SPONGE, 100);
        setPrice(Items.WET_SPONGE, 90);
        setPrice(Items.CONDUIT, 600);
        setPrice(Items.BEACON, 1500);
        setPrice(Items.END_CRYSTAL, 500);
        setPrice(Items.RESPAWN_ANCHOR, 200);
        setPrice(Items.LODESTONE, 300);

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
        setCustomPrice(SlayerItems.createEnchantedSugarCane().getItem(), 6);       // 91% reduction
        
        // New Enchanted Blocks - heavily reduced to prevent dupes
        setCustomPrice(SlayerItems.createEnchantedEmeraldBlock().getItem(), 86);     // 90% reduction
        setCustomPrice(SlayerItems.createEnchantedLapisBlock().getItem(), 23);      // 91% reduction
        setCustomPrice(SlayerItems.createEnchantedCoalBlock().getItem(), 6);        // 91% reduction
        setCustomPrice(SlayerItems.createEnchantedCopperBlock().getItem(), 13);      // 90% reduction
        setCustomPrice(SlayerItems.createEnchantedNetheriteBlock().getItem(), 4032); // 90% reduction
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
        return SELL_PRICES.getOrDefault(item, 0);
    }

    public static int getBuyPrice(Item item) {
        int sellPrice = getSellPrice(item);
        return sellPrice > 0 ? sellPrice * 4 : 0;  // 4x sell price - very unforgiving
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

    public static boolean sellItem(ServerPlayerEntity player, Item item, int quantity) {
        int sellPrice = getSellPrice(item);
        if (sellPrice <= 0) {
            player.sendMessage(Text.literal("This item cannot be sold!").formatted(Formatting.RED));
            return false;
        }

        // Check if player has valid vanilla items to sell
        int validCount = 0;
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack invStack = player.getInventory().getStack(i);
            if (invStack.isEmpty()) continue;
            
            // Check for bounty items - never allowed
            if (CustomItemHandler.isBountyItem(invStack)) {
                continue;
            }
            
            // Only count vanilla items with no custom names or IDs
            if (invStack.getItem() == item && invStack.get(DataComponentTypes.CUSTOM_NAME) == null 
                    && SlayerItems.getCustomItemId(invStack) == null) {
                validCount += invStack.getCount();
            }
        }

        if (validCount < quantity) {
            player.sendMessage(Text.literal("You don't have enough of this item!").formatted(Formatting.RED));
            return false;
        }

        // Remove the vanilla items
        int remaining = quantity;
        for (int i = 0; i < player.getInventory().size() && remaining > 0; i++) {
            ItemStack invStack = player.getInventory().getStack(i);
            if (invStack.isEmpty()) continue;
            
            // Only remove vanilla items
            if (invStack.getItem() == item && invStack.get(DataComponentTypes.CUSTOM_NAME) == null 
                    && SlayerItems.getCustomItemId(invStack) == null) {
                int toRemove = Math.min(remaining, invStack.getCount());
                invStack.decrement(toRemove);
                remaining -= toRemove;
            }
        }
        
        // TYCOON buff: +10% sell price bonus
        String playerUuid = player.getUuidAsString();
        double sellBonus = PlayerBuffManager.getShopSellBonus(playerUuid);
        int totalValue = (int) Math.ceil(sellPrice * quantity * (1.0 + sellBonus));
        
        CoinManager.giveCoinsQuiet(player, totalValue);

        player.sendMessage(Text.literal("Sold " + quantity + "x " + getItemName(item) + " for ")
                .formatted(Formatting.GREEN)
                .append(Text.literal(formatCoins(totalValue)).formatted(Formatting.GOLD)));

        return true;
    }

    public static boolean buyItem(ServerPlayerEntity player, Item item, int quantity) {
        int buyPrice = getBuyPrice(item);
        if (buyPrice <= 0) {
            player.sendMessage(Text.literal("This item cannot be bought!").formatted(Formatting.RED));
            return false;
        }

        int totalCost = buyPrice * quantity;

        if (!CoinManager.hasCoins(player, totalCost)) {
            player.sendMessage(Text.literal("You don't have enough coins!").formatted(Formatting.RED));
            return false;
        }

        if (!hasInventorySpace(player, item, quantity)) {
            player.sendMessage(Text.literal("You don't have enough inventory space!").formatted(Formatting.RED));
            return false;
        }

        CoinManager.removeCoins(player, totalCost);
        giveItems(player, item, quantity);

        player.sendMessage(Text.literal("Bought " + quantity + "x " + getItemName(item) + " for ")
                .formatted(Formatting.GREEN)
                .append(Text.literal(formatCoins(totalCost)).formatted(Formatting.GOLD)));

        return true;
    }

    // ===== HELPER METHODS =====

    public static int countItems(ServerPlayerEntity player, Item item) {
        int count = 0;
        for (int i = 0; i < player.getInventory().size(); i++) {
            var stack = player.getInventory().getStack(i);
            if (stack.getItem() == item) {
                count += stack.getCount();
            }
        }
        return count;
    }

    public static void removeItems(ServerPlayerEntity player, Item item, int quantity) {
        int remaining = quantity;
        for (int i = 0; i < player.getInventory().size() && remaining > 0; i++) {
            var stack = player.getInventory().getStack(i);
            if (stack.getItem() == item) {
                int toRemove = Math.min(remaining, stack.getCount());
                stack.decrement(toRemove);
                remaining -= toRemove;
            }
        }
    }
    
    public static void giveItems(ServerPlayerEntity player, Item item, int quantity) {
        int remaining = quantity;
        int maxStack = item.getMaxCount();

        // Shop only gives vanilla items
        while (remaining > 0) {
            int toGive = Math.min(remaining, maxStack);
            ItemStack stack = new net.minecraft.item.ItemStack(item, toGive);
            player.getInventory().insertStack(stack);
            remaining -= toGive;
        }
    }

    public static boolean hasInventorySpace(ServerPlayerEntity player, Item item, int quantity) {
        int space = 0;
        int maxStack = item.getMaxCount();

        for (int i = 0; i < 36; i++) {
            var stack = player.getInventory().getStack(i);
            if (stack.isEmpty()) {
                space += maxStack;
            } else if (stack.getItem() == item && stack.getCount() < maxStack) {
                space += maxStack - stack.getCount();
            }
        }

        return space >= quantity;
    }

    public static String getItemName(Item item) {
        Identifier id = Registries.ITEM.getId(item);
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
