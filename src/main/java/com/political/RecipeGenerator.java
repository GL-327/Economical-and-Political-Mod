package com.political;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;

import java.util.*;

/**
 * Auto-generates default crafting recipes for all custom items.
 * T1: Less than Diamond strength
 * T2: Diamond/Netherite strength  
 * T3: Stronger than Netherite
 */
public class RecipeGenerator {

    // GSON instance for JSON serialization (unused but kept for potential future use)
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // Crafting cores for different tiers
    private static final String T1_CORE = "custom_crafter_core_t1";
    private static final String T2_CORE = "custom_crafter_core_t2";
    private static final String T3_CORE = "custom_crafter_core_t3";
    
    // Item tiers based on their strength
    private static final Map<String, Integer> ITEM_TIERS = new HashMap<>();
    static {
        // T1 items (less than diamond)
        ITEM_TIERS.put("glass", 1);
        ITEM_TIERS.put("quartz", 1);
        ITEM_TIERS.put("glowstone", 1);
        ITEM_TIERS.put("redstone", 1);
        ITEM_TIERS.put("netherrack", 1);
        ITEM_TIERS.put("packed_ice", 1);
        ITEM_TIERS.put("prismarine", 1);
        ITEM_TIERS.put("terracotta", 1);
        ITEM_TIERS.put("soul_sand", 1);
        ITEM_TIERS.put("sandstone", 1);
        ITEM_TIERS.put("emerald", 1);
        ITEM_TIERS.put("lapis", 1);
        
        // T2 items (diamond/netherite strength)
        ITEM_TIERS.put("obsidian", 2);
        ITEM_TIERS.put("end_stone", 2);
        ITEM_TIERS.put("mossy", 2);
        ITEM_TIERS.put("magma", 2);
        ITEM_TIERS.put("iron", 2);
        ITEM_TIERS.put("gold", 2);
        
        // T3 items (stronger than netherite)
        ITEM_TIERS.put("diamond", 3);
        ITEM_TIERS.put("netherite", 3);
    }
    
    // Map item name to minecraft material
    private static final Map<String, String> MATERIAL_MAP = new HashMap<>();

    static {
        // Block materials
        MATERIAL_MAP.put("glass", "minecraft:glass");
        MATERIAL_MAP.put("obsidian", "minecraft:obsidian");
        MATERIAL_MAP.put("quartz", "minecraft:quartz_block");
        MATERIAL_MAP.put("glowstone", "minecraft:glowstone");
        MATERIAL_MAP.put("redstone", "minecraft:redstone_block");
        MATERIAL_MAP.put("netherrack", "minecraft:netherrack");
        MATERIAL_MAP.put("end_stone", "minecraft:end_stone");
        MATERIAL_MAP.put("packed_ice", "minecraft:packed_ice");
        MATERIAL_MAP.put("prismarine", "minecraft:prismarine");
        MATERIAL_MAP.put("terracotta", "minecraft:terracotta");
        MATERIAL_MAP.put("mossy", "minecraft:mossy_cobblestone");
        MATERIAL_MAP.put("soul_sand", "minecraft:soul_sand");
        MATERIAL_MAP.put("magma", "minecraft:magma_block");
        MATERIAL_MAP.put("sandstone", "minecraft:sandstone");
        MATERIAL_MAP.put("emerald", "minecraft:emerald_block");
        MATERIAL_MAP.put("lapis", "minecraft:lapis_block");
    }
    
    /**
     * Generates default recipes for all custom items
     */
    public static JsonArray generateAllDefaultRecipes() {
        JsonArray recipes = new JsonArray();
        
        // Generate Block Armor recipes
        for (JsonObject recipe : generateBlockArmorRecipes()) {
            recipes.add(recipe);
        }
        
        // Generate Gem Gear recipes  
        for (JsonObject recipe : generateGemGearRecipes()) {
            recipes.add(recipe);
        }
        
        // Generate Slayer recipes
        for (JsonObject recipe : generateSlayerRecipes()) {
            recipes.add(recipe);
        }
        
        return recipes;
    }
    
    private static List<JsonObject> generateBlockArmorRecipes() {
        List<JsonObject> recipes = new ArrayList<>();
        
        // All block armor types from BlockArmor class
        String[] armorTypes = {
            "glass", "obsidian", "quartz", "glowstone", "redstone", "netherrack",
            "end_stone", "packed_ice", "prismarine", "terracotta", "mossy",
            "soul_sand", "magma", "sandstone"
        };
        
        String[] armorPieces = {"helmet", "chestplate", "leggings", "boots"};
        
        for (String type : armorTypes) {
            int tier = ITEM_TIERS.getOrDefault(type, 1);
            String core = getCoreForTier(tier);
            String blockId = MATERIAL_MAP.get(type);
            
            for (String piece : armorPieces) {
                recipes.add(createArmorRecipe(type, piece, blockId, core));
            }
        }
        
        return recipes;
    }
    
    private static List<JsonObject> generateGemGearRecipes() {
        List<JsonObject> recipes = new ArrayList<>();
        
        String[] gemTypes = {"emerald", "lapis"};
        String[] armorPieces = {"helmet", "chestplate", "leggings", "boots"};
        String[] tools = {"sword", "pickaxe", "axe", "shovel", "hoe"};
        
        for (String gem : gemTypes) {
            int tier = ITEM_TIERS.getOrDefault(gem, 1);
            String core = getCoreForTier(tier);
            String blockId = MATERIAL_MAP.get(gem);
            
            // Armor recipes
            for (String piece : armorPieces) {
                recipes.add(createGemArmorRecipe(gem, piece, blockId, core));
            }
            
            // Tool recipes
            for (String tool : tools) {
                recipes.add(createGemToolRecipe(gem, tool, blockId, core));
            }
        }
        
        return recipes;
    }
    
    private static List<JsonObject> generateSlayerRecipes() {
        List<JsonObject> recipes = new ArrayList<>();
        
        // T1 and T2 slayer armor for each type
        String[] slayerTypes = {"zombie", "skeleton", "spider", "slime"};
        
        for (String type : slayerTypes) {
            // T1 recipes
            recipes.addAll(generateSlayerTierRecipes(type, 1));
            // T2 recipes  
            recipes.addAll(generateSlayerTierRecipes(type, 2));
        }
        
        return recipes;
    }
    
    private static List<JsonObject> generateSlayerTierRecipes(String type, int tier) {
        List<JsonObject> recipes = new ArrayList<>();
        String core = getCoreForTier(tier);
        
        String[] pieces = {"helmet", "chestplate", "leggings", "boots"};
        for (String piece : pieces) {
            recipes.add(createSlayerArmorRecipe(type, tier, piece, core));
        }
        
        // Sword recipe
        recipes.add(createSlayerSwordRecipe(type, tier, core));
        
        return recipes;
    }
    
    private static JsonObject createArmorRecipe(String type, String piece, String blockId, String core) {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("name", capitalize(type) + " " + capitalize(piece));
        recipe.addProperty("requiredSlayer", "");
        recipe.addProperty("requiredLevel", 0);
        
        // Result - create the actual armor piece
        ItemStack result = createBlockArmorPiece(type, piece);
        recipe.add("result", RecipeConfigManager.serializeItemStack(result));
        
        // Ingredients - standard armor pattern with missing second slot
        JsonArray ingredients = new JsonArray();
        String[] pattern = getArmorPattern(piece);
        
        for (int i = 0; i < 9; i++) {
            if (pattern[i].equals("core")) {
                ingredients.add(createCoreIngredient(core));
            } else if (pattern[i].equals("block")) {
                ingredients.add(createBlockIngredient(blockId));
            } else {
                ingredients.add("");
            }
        }
        
        recipe.add("ingredients", ingredients);
        
        // Locked slots - lock all but the core position
        JsonArray lockedSlots = new JsonArray();
        for (int i = 0; i < 9; i++) {
            lockedSlots.add(!pattern[i].equals("core"));
        }
        recipe.add("lockedSlots", lockedSlots);
        
        return recipe;
    }
    
    private static String[] getArmorPattern(String piece) {
        return switch (piece) {
            // Vanilla armor patterns with core in center (slot 4)
            // Helmet: X X X / X . X / . . .
            case "helmet" -> new String[]{"block", "block", "block", "block", "core", "block", "", "", ""};
            // Chestplate: X . X / X . X / X X X
            case "chestplate" -> new String[]{"block", "", "block", "block", "core", "block", "block", "block", "block"};
            // Leggings: X X X / X . X / X . X
            case "leggings" -> new String[]{"block", "block", "block", "block", "core", "block", "block", "", "block"};
            // Boots: . . . / X . X / X . X
            case "boots" -> new String[]{"", "", "", "block", "core", "block", "block", "", "block"};
            default -> new String[]{"", "", "", "", "", "", "", "", ""};
        };
    }
    
    private static JsonObject createBlockIngredient(String blockId) {
        JsonObject ingredient = new JsonObject();
        ingredient.addProperty("nbt", "{count:1,id:\"" + blockId + "\"}");
        ingredient.addProperty("count", 1);
        return ingredient;
    }
    
    private static JsonObject createCoreIngredient(String coreId) {
        JsonObject ingredient = new JsonObject();
        ingredient.addProperty("nbt", "{components:{\"minecraft:custom_data\":{custom_id:\"" + coreId + "\"}},count:1,id:\"minecraft:heavy_core\"}");
        ingredient.addProperty("count", 1);
        return ingredient;
    }
    
    private static ItemStack createBlockArmorPiece(String type, String piece) {
        return switch (type) {
            case "glass" -> createGlassArmor(piece);
            case "obsidian" -> createObsidianArmor(piece);
            case "quartz" -> createQuartzArmor(piece);
            case "glowstone" -> createGlowstoneArmor(piece);
            case "redstone" -> createRedstoneArmor(piece);
            case "netherrack" -> createNetherrackArmor(piece);
            case "end_stone" -> createEndStoneArmor(piece);
            case "packed_ice" -> createPackedIceArmor(piece);
            case "prismarine" -> createPrismarineArmor(piece);
            case "terracotta" -> createTerracottaArmor(piece);
            case "mossy" -> createMossyArmor(piece);
            case "soul_sand" -> createSoulSandArmor(piece);
            case "magma" -> createMagmaArmor(piece);
            case "sandstone" -> createSandstoneArmor(piece);
            default -> new ItemStack(Items.LEATHER_CHESTPLATE);
        };
    }
    
    // Helper methods to create armor pieces (these would call BlockArmor methods)
    private static ItemStack createGlassArmor(String piece) {
        return switch (piece) {
            case "helmet" -> BlockArmor.createGlassHelmet();
            case "chestplate" -> BlockArmor.createGlassChestplate();
            case "leggings" -> BlockArmor.createGlassLeggings();
            case "boots" -> BlockArmor.createGlassBoots();
            default -> new ItemStack(Items.LEATHER_CHESTPLATE);
        };
    }
    
    private static ItemStack createObsidianArmor(String piece) {
        return switch (piece) {
            case "helmet" -> BlockArmor.createObsidianHelmet();
            case "chestplate" -> BlockArmor.createObsidianChestplate();
            case "leggings" -> BlockArmor.createObsidianLeggings();
            case "boots" -> BlockArmor.createObsidianBoots();
            default -> new ItemStack(Items.LEATHER_CHESTPLATE);
        };
    }
    
    // Similar methods for other armor types
    private static ItemStack createQuartzArmor(String piece) { 
        return switch (piece) {
            case "helmet" -> BlockArmor.createQuartzHelmet();
            case "chestplate" -> BlockArmor.createQuartzChestplate();
            case "leggings" -> BlockArmor.createQuartzLeggings();
            case "boots" -> BlockArmor.createQuartzBoots();
            default -> new ItemStack(Items.LEATHER_CHESTPLATE);
        };
    }
    
    private static ItemStack createGlowstoneArmor(String piece) { 
        return switch (piece) {
            case "helmet" -> BlockArmor.createGlowstoneHelmet();
            case "chestplate" -> BlockArmor.createGlowstoneChestplate();
            case "leggings" -> BlockArmor.createGlowstoneLeggings();
            case "boots" -> BlockArmor.createGlowstoneBoots();
            default -> new ItemStack(Items.LEATHER_CHESTPLATE);
        };
    }
    
    private static ItemStack createRedstoneArmor(String piece) { 
        return switch (piece) {
            case "helmet" -> BlockArmor.createRedstoneHelmet();
            case "chestplate" -> BlockArmor.createRedstoneChestplate();
            case "leggings" -> BlockArmor.createRedstoneLeggings();
            case "boots" -> BlockArmor.createRedstoneBoots();
            default -> new ItemStack(Items.LEATHER_CHESTPLATE);
        };
    }
    
    private static ItemStack createNetherrackArmor(String piece) { 
        return switch (piece) {
            case "helmet" -> BlockArmor.createNetherrackHelmet();
            case "chestplate" -> BlockArmor.createNetherrackChestplate();
            case "leggings" -> BlockArmor.createNetherrackLeggings();
            case "boots" -> BlockArmor.createNetherrackBoots();
            default -> new ItemStack(Items.LEATHER_CHESTPLATE);
        };
    }
    
    private static ItemStack createEndStoneArmor(String piece) { 
        return switch (piece) {
            case "helmet" -> BlockArmor.createEndStoneHelmet();
            case "chestplate" -> BlockArmor.createEndStoneChestplate();
            case "leggings" -> BlockArmor.createEndStoneLeggings();
            case "boots" -> BlockArmor.createEndStoneBoots();
            default -> new ItemStack(Items.LEATHER_CHESTPLATE);
        };
    }
    
    private static ItemStack createPackedIceArmor(String piece) { 
        return switch (piece) {
            case "helmet" -> BlockArmor.createPackedIceHelmet();
            case "chestplate" -> BlockArmor.createPackedIceChestplate();
            case "leggings" -> BlockArmor.createPackedIceLeggings();
            case "boots" -> BlockArmor.createPackedIceBoots();
            default -> new ItemStack(Items.LEATHER_CHESTPLATE);
        };
    }
    
    private static ItemStack createPrismarineArmor(String piece) { 
        return switch (piece) {
            case "helmet" -> BlockArmor.createPrismarineHelmet();
            case "chestplate" -> BlockArmor.createPrismarineChestplate();
            case "leggings" -> BlockArmor.createPrismarineLeggings();
            case "boots" -> BlockArmor.createPrismarineBoots();
            default -> new ItemStack(Items.LEATHER_CHESTPLATE);
        };
    }
    
    private static ItemStack createTerracottaArmor(String piece) { 
        return switch (piece) {
            case "helmet" -> BlockArmor.createTerracottaHelmet();
            case "chestplate" -> BlockArmor.createTerracottaChestplate();
            case "leggings" -> BlockArmor.createTerracottaLeggings();
            case "boots" -> BlockArmor.createTerracottaBoots();
            default -> new ItemStack(Items.LEATHER_CHESTPLATE);
        };
    }
    
    private static ItemStack createMossyArmor(String piece) { 
        return switch (piece) {
            case "helmet" -> BlockArmor.createMossyHelmet();
            case "chestplate" -> BlockArmor.createMossyChestplate();
            case "leggings" -> BlockArmor.createMossyLeggings();
            case "boots" -> BlockArmor.createMossyBoots();
            default -> new ItemStack(Items.LEATHER_CHESTPLATE);
        };
    }
    
    private static ItemStack createSoulSandArmor(String piece) { 
        return switch (piece) {
            case "helmet" -> BlockArmor.createSoulSandHelmet();
            case "chestplate" -> BlockArmor.createSoulSandChestplate();
            case "leggings" -> BlockArmor.createSoulSandLeggings();
            case "boots" -> BlockArmor.createSoulSandBoots();
            default -> new ItemStack(Items.LEATHER_CHESTPLATE);
        };
    }
    
    private static ItemStack createMagmaArmor(String piece) { 
        return switch (piece) {
            case "helmet" -> BlockArmor.createMagmaHelmet();
            case "chestplate" -> BlockArmor.createMagmaChestplate();
            case "leggings" -> BlockArmor.createMagmaLeggings();
            case "boots" -> BlockArmor.createMagmaBoots();
            default -> new ItemStack(Items.LEATHER_CHESTPLATE);
        };
    }
    
    private static ItemStack createSandstoneArmor(String piece) { 
        return switch (piece) {
            case "helmet" -> BlockArmor.createSandstoneHelmet();
            case "chestplate" -> BlockArmor.createSandstoneChestplate();
            case "leggings" -> BlockArmor.createSandstoneLeggings();
            case "boots" -> BlockArmor.createSandstoneBoots();
            default -> new ItemStack(Items.LEATHER_CHESTPLATE);
        };
    }
    
    private static String getCoreForTier(int tier) {
        return switch (tier) {
            case 1 -> T1_CORE;
            case 2 -> T2_CORE;
            case 3 -> T3_CORE;
            default -> T1_CORE;
        };
    }
    
    private static String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
    
    // Placeholder methods for gem and slayer recipes
    private static JsonObject createGemArmorRecipe(String gem, String piece, String blockId, String core) {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("name", capitalize(gem) + " " + capitalize(piece));
        recipe.addProperty("requiredSlayer", "");
        recipe.addProperty("requiredLevel", 0);
        
        // Result would call GemGear.createEmeraldHelmet() etc.
        recipe.add("result", createGemResult(gem, piece));
        
        JsonArray ingredients = new JsonArray();
        String[] pattern = getArmorPattern(piece);
        
        for (int i = 0; i < 9; i++) {
            if (pattern[i].equals("core")) {
                ingredients.add(createCoreIngredient(core));
            } else if (pattern[i].equals("block")) {
                ingredients.add(createBlockIngredient(blockId));
            } else {
                ingredients.add("");
            }
        }
        
        recipe.add("ingredients", ingredients);
        
        JsonArray lockedSlots = new JsonArray();
        for (int i = 0; i < 9; i++) {
            lockedSlots.add(!pattern[i].equals("core"));
        }
        recipe.add("lockedSlots", lockedSlots);
        
        return recipe;
    }
    
    private static JsonObject createGemToolRecipe(String gem, String tool, String blockId, String core) {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("name", capitalize(gem) + " " + capitalize(tool));
        recipe.addProperty("requiredSlayer", "");
        recipe.addProperty("requiredLevel", 0);
        
        recipe.add("result", createGemToolResult(gem, tool));
        
        // Tool pattern (sword, pickaxe, etc.)
        JsonArray ingredients = new JsonArray();
        String[] pattern = getToolPattern(tool);
        
        for (int i = 0; i < 9; i++) {
            if (pattern[i].equals("core")) {
                ingredients.add(createCoreIngredient(core));
            } else if (pattern[i].equals("block")) {
                ingredients.add(createBlockIngredient(blockId));
            } else if (pattern[i].equals("stick")) {
                ingredients.add(createStickIngredient());
            } else {
                ingredients.add("");
            }
        }
        
        recipe.add("ingredients", ingredients);
        
        JsonArray lockedSlots = new JsonArray();
        for (int i = 0; i < 9; i++) {
            lockedSlots.add(!pattern[i].equals("core"));
        }
        recipe.add("lockedSlots", lockedSlots);
        
        return recipe;
    }
    
    private static String[] getToolPattern(String tool) {
        return switch (tool) {
            case "sword" -> new String[]{"", "block", "", "", "core", "", "", "stick", ""};
            case "pickaxe" -> new String[]{"block", "block", "block", "", "core", "", "", "stick", ""};
            case "axe" -> new String[]{"block", "block", "", "", "core", "", "", "stick", ""};
            case "shovel" -> new String[]{"", "", "", "", "core", "", "", "stick", ""};
            case "hoe" -> new String[]{"block", "block", "", "", "core", "", "", "stick", ""};
            default -> new String[]{"", "", "", "", "", "", "", "", ""};
        };
    }
    
    private static JsonObject createStickIngredient() {
        JsonObject ingredient = new JsonObject();
        ingredient.addProperty("nbt", "{count:1,id:\"minecraft:stick\"}");
        ingredient.addProperty("count", 1);
        return ingredient;
    }
    
    private static JsonObject createGemResult(String gem, String piece) {
        // Call actual GemGear creation methods
        ItemStack result = switch (gem) {
            case "emerald" -> switch (piece) {
                case "helmet" -> GemGear.createEmeraldHelmet();
                case "chestplate" -> GemGear.createEmeraldChestplate();
                case "leggings" -> GemGear.createEmeraldLeggings();
                case "boots" -> GemGear.createEmeraldBoots();
                default -> ItemStack.EMPTY;
            };
            case "lapis" -> switch (piece) {
                case "helmet" -> GemGear.createLapisHelmet();
                case "chestplate" -> GemGear.createLapisChestplate();
                case "leggings" -> GemGear.createLapisLeggings();
                case "boots" -> GemGear.createLapisBoots();
                default -> ItemStack.EMPTY;
            };
            default -> ItemStack.EMPTY;
        };
        return RecipeConfigManager.serializeItemStack(result).getAsJsonObject();
    }
    
    private static JsonObject createGemToolResult(String gem, String tool) {
        // Call actual GemGear creation methods
        ItemStack result = switch (gem) {
            case "emerald" -> switch (tool) {
                case "sword" -> GemGear.createEmeraldSword();
                case "pickaxe" -> GemGear.createEmeraldPickaxe();
                case "axe" -> GemGear.createEmeraldAxe();
                case "shovel" -> GemGear.createEmeraldShovel();
                case "hoe" -> GemGear.createEmeraldHoe();
                default -> ItemStack.EMPTY;
            };
            case "lapis" -> switch (tool) {
                case "sword" -> GemGear.createLapisSword();
                case "pickaxe" -> GemGear.createLapisPickaxe();
                case "axe" -> GemGear.createLapisAxe();
                case "shovel" -> GemGear.createLapisShovel();
                case "hoe" -> GemGear.createLapisHoe();
                default -> ItemStack.EMPTY;
            };
            default -> ItemStack.EMPTY;
        };
        return RecipeConfigManager.serializeItemStack(result).getAsJsonObject();
    }
    
    private static JsonObject createSlayerArmorRecipe(String type, int tier, String piece, String core) {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("name", capitalize(type) + " T" + tier + " " + capitalize(piece));
        recipe.addProperty("requiredSlayer", type.toUpperCase());
        recipe.addProperty("requiredLevel", tier * 10);
        
        recipe.add("result", createSlayerResult(type, tier, piece));
        
        JsonArray ingredients = new JsonArray();
        String[] pattern = getArmorPattern(piece);
        
        for (int i = 0; i < 9; i++) {
            if (pattern[i].equals("core")) {
                ingredients.add(createCoreIngredient(core));
            } else if (pattern[i].equals("block")) {
                ingredients.add(createSlayerIngredient(type));
            } else {
                ingredients.add("");
            }
        }
        
        recipe.add("ingredients", ingredients);
        
        JsonArray lockedSlots = new JsonArray();
        for (int i = 0; i < 9; i++) {
            lockedSlots.add(!pattern[i].equals("core"));
        }
        recipe.add("lockedSlots", lockedSlots);
        
        return recipe;
    }
    
    private static JsonObject createSlayerSwordRecipe(String type, int tier, String core) {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("name", capitalize(type) + " T" + tier + " Sword");
        recipe.addProperty("requiredSlayer", type.toUpperCase());
        recipe.addProperty("requiredLevel", tier * 10);
        
        recipe.add("result", createSlayerSwordResult(type, tier));
        
        JsonArray ingredients = new JsonArray();
        String[] pattern = {"", "slayer", "", "", "core", "", "", "stick", ""};
        
        for (int i = 0; i < 9; i++) {
            if (pattern[i].equals("core")) {
                ingredients.add(createCoreIngredient(core));
            } else if (pattern[i].equals("slayer")) {
                ingredients.add(createSlayerIngredient(type));
            } else if (pattern[i].equals("stick")) {
                ingredients.add(createStickIngredient());
            } else {
                ingredients.add("");
            }
        }
        
        recipe.add("ingredients", ingredients);
        
        JsonArray lockedSlots = new JsonArray();
        for (int i = 0; i < 9; i++) {
            lockedSlots.add(!pattern[i].equals("core"));
        }
        recipe.add("lockedSlots", lockedSlots);
        
        return recipe;
    }
    
    private static JsonObject createSlayerIngredient(String type) {
        // Use the slayer's icon item
        SlayerManager.SlayerType slayerType = SlayerManager.SlayerType.valueOf(type.toUpperCase());
        JsonObject ingredient = new JsonObject();
        ingredient.addProperty("nbt", "{count:1,id:\"" + Registries.ITEM.getId(slayerType.icon).toString() + "\"}");
        ingredient.addProperty("count", 1);
        return ingredient;
    }
    
    private static JsonObject createSlayerResult(String type, int tier, String piece) {
        // Call actual SlayerItems creation methods
        SlayerManager.SlayerType slayerType = SlayerManager.SlayerType.valueOf(type.toUpperCase());
        ItemStack result = switch (piece) {
            case "helmet" -> SlayerItems.createSlayerHelmet(slayerType, tier);
            case "chestplate" -> SlayerItems.createSlayerChestplate(slayerType, tier);
            case "leggings" -> SlayerItems.createSlayerLeggings(slayerType, tier);
            case "boots" -> SlayerItems.createSlayerBoots(slayerType, tier);
            default -> ItemStack.EMPTY;
        };
        return RecipeConfigManager.serializeItemStack(result).getAsJsonObject();
    }
    
    private static JsonObject createSlayerSwordResult(String type, int tier) {
        // Call actual SlayerItems creation method
        SlayerManager.SlayerType slayerType = SlayerManager.SlayerType.valueOf(type.toUpperCase());
        ItemStack result = SlayerItems.createSlayerSword(slayerType);
        return RecipeConfigManager.serializeItemStack(result).getAsJsonObject();
    }
}
