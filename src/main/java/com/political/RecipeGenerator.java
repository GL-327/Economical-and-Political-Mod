package com.political;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class RecipeGenerator {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

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
        MATERIAL_MAP.put("endstone", "minecraft:end_stone");
        MATERIAL_MAP.put("end_stone", "minecraft:end_stone");
        MATERIAL_MAP.put("ice", "minecraft:packed_ice");
        MATERIAL_MAP.put("packed_ice", "minecraft:packed_ice");
        MATERIAL_MAP.put("prismarine", "minecraft:prismarine");
        MATERIAL_MAP.put("terracotta", "minecraft:terracotta");
        MATERIAL_MAP.put("mossy", "minecraft:mossy_cobblestone");
        MATERIAL_MAP.put("mossy_cobblestone", "minecraft:mossy_cobblestone");
        MATERIAL_MAP.put("soul_sand", "minecraft:soul_sand");
        MATERIAL_MAP.put("soul_soil", "minecraft:soul_soil");
        MATERIAL_MAP.put("magma", "minecraft:magma_block");
        MATERIAL_MAP.put("sponge", "minecraft:sponge");
        MATERIAL_MAP.put("snow", "minecraft:snow_block");
        MATERIAL_MAP.put("snow_block", "minecraft:snow_block");
        MATERIAL_MAP.put("sculk", "minecraft:sculk");
        MATERIAL_MAP.put("wet_sponge", "minecraft:wet_sponge");
        MATERIAL_MAP.put("lapis", "minecraft:lapis_block");
        MATERIAL_MAP.put("lapis_lazuli", "minecraft:lapis_lazuli");
        MATERIAL_MAP.put("lapis_block", "minecraft:lapis_block");
        MATERIAL_MAP.put("emerald", "minecraft:emerald_block");
        MATERIAL_MAP.put("emerald_block", "minecraft:emerald_block");
        MATERIAL_MAP.put("coal", "minecraft:coal_block");
        MATERIAL_MAP.put("coal_block", "minecraft:coal_block");
        MATERIAL_MAP.put("diamond", "minecraft:diamond_block");
        MATERIAL_MAP.put("diamond_block", "minecraft:diamond_block");
        MATERIAL_MAP.put("gold", "minecraft:gold_block");
        MATERIAL_MAP.put("gold_block", "minecraft:gold_block");
        MATERIAL_MAP.put("iron", "minecraft:iron_block");
        MATERIAL_MAP.put("iron_block", "minecraft:iron_block");
        MATERIAL_MAP.put("netherite", "minecraft:netherite_block");
        MATERIAL_MAP.put("netherite_block", "minecraft:netherite_block");
        MATERIAL_MAP.put("copper", "minecraft:copper_block");
        MATERIAL_MAP.put("copper_block", "minecraft:copper_block");
        MATERIAL_MAP.put("raw_iron", "minecraft:raw_iron_block");
        MATERIAL_MAP.put("raw_iron_block", "minecraft:raw_iron_block");
        MATERIAL_MAP.put("raw_gold", "minecraft:raw_gold_block");
        MATERIAL_MAP.put("raw_gold_block", "minecraft:raw_gold_block");
        MATERIAL_MAP.put("ancient_debris", "minecraft:ancient_debris");
        MATERIAL_MAP.put("amethyst", "minecraft:amethyst_block");
        MATERIAL_MAP.put("amethyst_block", "minecraft:amethyst_block");
        MATERIAL_MAP.put("amethyst_cluster", "minecraft:amethyst_cluster");
        MATERIAL_MAP.put("amethyst_shard", "minecraft:amethyst_shard");
        MATERIAL_MAP.put("stone", "minecraft:stone");
        MATERIAL_MAP.put("cobblestone", "minecraft:cobblestone");
        MATERIAL_MAP.put("stone_bricks", "minecraft:stone_bricks");
        MATERIAL_MAP.put("smooth_stone", "minecraft:smooth_stone");
        MATERIAL_MAP.put("granite", "minecraft:granite");
        MATERIAL_MAP.put("diorite", "minecraft:diorite");
        MATERIAL_MAP.put("andesite", "minecraft:andesite");
        MATERIAL_MAP.put("polished_granite", "minecraft:polished_granite");
        MATERIAL_MAP.put("polished_diorite", "minecraft:polished_diorite");
        MATERIAL_MAP.put("polished_andesite", "minecraft:polished_andesite");
        MATERIAL_MAP.put("deepslate", "minecraft:deepslate");
        MATERIAL_MAP.put("cobbled_deepslate", "minecraft:cobbled_deepslate");
        MATERIAL_MAP.put("polished_deepslate", "minecraft:polished_deepslate");
        MATERIAL_MAP.put("reinforced_deepslate", "minecraft:reinforced_deepslate");
        MATERIAL_MAP.put("tuff", "minecraft:tuff");
        MATERIAL_MAP.put("polished_tuff", "minecraft:polished_tuff");
        MATERIAL_MAP.put("calcite", "minecraft:calcite");
        MATERIAL_MAP.put("dripstone", "minecraft:dripstone_block");
        MATERIAL_MAP.put("sandstone", "minecraft:sandstone");
        MATERIAL_MAP.put("red_sandstone", "minecraft:red_sandstone");
        MATERIAL_MAP.put("sand", "minecraft:sand");
        MATERIAL_MAP.put("red_sand", "minecraft:red_sand");
        MATERIAL_MAP.put("cactus", "minecraft:cactus");
        MATERIAL_MAP.put("brick", "minecraft:bricks");
        MATERIAL_MAP.put("bricks", "minecraft:bricks");
        MATERIAL_MAP.put("mud_bricks", "minecraft:mud_bricks");
        MATERIAL_MAP.put("nether_brick", "minecraft:nether_bricks");
        MATERIAL_MAP.put("nether_bricks", "minecraft:nether_bricks");
        MATERIAL_MAP.put("cracked_nether_brick", "minecraft:cracked_nether_bricks");
        MATERIAL_MAP.put("cracked_stone_brick", "minecraft:cracked_stone_bricks");
        MATERIAL_MAP.put("chiseled_stone_brick", "minecraft:chiseled_stone_bricks");
        MATERIAL_MAP.put("chiseled_nether_brick", "minecraft:chiseled_nether_bricks");
        MATERIAL_MAP.put("basalt", "minecraft:basalt");
        MATERIAL_MAP.put("polished_basalt", "minecraft:polished_basalt");
        MATERIAL_MAP.put("smooth_basalt", "minecraft:smooth_basalt");
        MATERIAL_MAP.put("blackstone", "minecraft:blackstone");
        MATERIAL_MAP.put("blackstone_bricks", "minecraft:polished_blackstone_bricks");
        MATERIAL_MAP.put("polished_blackstone", "minecraft:polished_blackstone");
        MATERIAL_MAP.put("gilded_blackstone", "minecraft:gilded_blackstone");
        MATERIAL_MAP.put("crying_obsidian", "minecraft:crying_obsidian");
        MATERIAL_MAP.put("obsidian_block", "minecraft:obsidian");
        MATERIAL_MAP.put("end_stone_brick", "minecraft:end_stone_bricks");
        MATERIAL_MAP.put("purpur", "minecraft:purpur_block");
        MATERIAL_MAP.put("purpur_block", "minecraft:purpur_block");
        MATERIAL_MAP.put("bone_block", "minecraft:bone_block");
        MATERIAL_MAP.put("bone_meal", "minecraft:bone_meal");
        MATERIAL_MAP.put("dried_kelp", "minecraft:dried_kelp_block");
        MATERIAL_MAP.put("dried_kelp_block", "minecraft:dried_kelp_block");
        MATERIAL_MAP.put("sponge", "minecraft:sponge");
        MATERIAL_MAP.put("wet_sponge", "minecraft:wet_sponge");
        MATERIAL_MAP.put("hay", "minecraft:hay_block");
        MATERIAL_MAP.put("hay_block", "minecraft:hay_block");
        MATERIAL_MAP.put("honeycomb", "minecraft:honeycomb");
        MATERIAL_MAP.put("honeycomb_block", "minecraft:honeycomb_block");
        MATERIAL_MAP.put("slime", "minecraft:slime_block");
        MATERIAL_MAP.put("slime_block", "minecraft:slime_block");
        MATERIAL_MAP.put("pumpkin", "minecraft:pumpkin");
        MATERIAL_MAP.put("melon", "minecraft:melon");
        MATERIAL_MAP.put("cactus", "minecraft:cactus");
        MATERIAL_MAP.put("lily_pad", "minecraft:lily_pad");
        MATERIAL_MAP.put("oak_log", "minecraft:oak_log");
        MATERIAL_MAP.put("oak_planks", "minecraft:oak_planks");
        MATERIAL_MAP.put("spruce_log", "minecraft:spruce_log");
        MATERIAL_MAP.put("spruce_planks", "minecraft:spruce_planks");
        MATERIAL_MAP.put("birch_log", "minecraft:birch_log");
        MATERIAL_MAP.put("birch_planks", "minecraft:birch_planks");
        MATERIAL_MAP.put("jungle_log", "minecraft:jungle_log");
        MATERIAL_MAP.put("jungle_planks", "minecraft:jungle_planks");
        MATERIAL_MAP.put("acacia_log", "minecraft:acacia_log");
        MATERIAL_MAP.put("acacia_planks", "minecraft:acacia_planks");
        MATERIAL_MAP.put("dark_oak_log", "minecraft:dark_oak_log");
        MATERIAL_MAP.put("dark_oak_planks", "minecraft:dark_oak_planks");
        MATERIAL_MAP.put("mangrove_log", "minecraft:mangrove_log");
        MATERIAL_MAP.put("mangrove_planks", "minecraft:mangrove_planks");
        MATERIAL_MAP.put("cherry_log", "minecraft:cherry_log");
        MATERIAL_MAP.put("cherry_planks", "minecraft:cherry_planks");
        MATERIAL_MAP.put("bamboo_block", "minecraft:bamboo_block");
        MATERIAL_MAP.put("bamboo_planks", "minecraft:bamboo_planks");
        MATERIAL_MAP.put("crimson_stem", "minecraft:crimson_stem");
        MATERIAL_MAP.put("crimson_planks", "minecraft:crimson_planks");
        MATERIAL_MAP.put("warped_stem", "minecraft:warped_stem");
        MATERIAL_MAP.put("warped_planks", "minecraft:warped_planks");
        MATERIAL_MAP.put("stripped_oak_log", "minecraft:stripped_oak_log");
        MATERIAL_MAP.put("stripped_spruce_log", "minecraft:stripped_spruce_log");
        MATERIAL_MAP.put("stripped_birch_log", "minecraft:stripped_birch_log");
        MATERIAL_MAP.put("stripped_jungle_log", "minecraft:stripped_jungle_log");
        MATERIAL_MAP.put("stripped_acacia_log", "minecraft:stripped_acacia_log");
        MATERIAL_MAP.put("stripped_dark_oak_log", "minecraft:stripped_dark_oak_log");
        MATERIAL_MAP.put("stripped_mangrove_log", "minecraft:stripped_mangrove_log");
        MATERIAL_MAP.put("stripped_cherry_log", "minecraft:stripped_cherry_log");
        MATERIAL_MAP.put("stripped_bamboo_block", "minecraft:stripped_bamboo_block");
        MATERIAL_MAP.put("stripped_crimson_stem", "minecraft:stripped_crimson_stem");
        MATERIAL_MAP.put("stripped_warped_stem", "minecraft:stripped_warped_stem");
        MATERIAL_MAP.put("moss_block", "minecraft:moss_block");
        MATERIAL_MAP.put("mycelium", "minecraft:mycelium");
        MATERIAL_MAP.put("podzol", "minecraft:podzol");
        MATERIAL_MAP.put("dirt", "minecraft:dirt");
        MATERIAL_MAP.put("coarse_dirt", "minecraft:coarse_dirt");
        MATERIAL_MAP.put("rooted_dirt", "minecraft:rooted_dirt");
        MATERIAL_MAP.put("mud", "minecraft:mud");
        MATERIAL_MAP.put("clay", "minecraft:clay");
        MATERIAL_MAP.put("gravel", "minecraft:gravel");
        MATERIAL_MAP.put("mangrove_roots", "minecraft:mangrove_roots");
        MATERIAL_MAP.put("muddy_mangrove_roots", "minecraft:muddy_mangrove_roots");
        MATERIAL_MAP.put("sea_lantern", "minecraft:sea_lantern");
        MATERIAL_MAP.put("shroomlight", "minecraft:shroomlight");
        MATERIAL_MAP.put("ochre_froglight", "minecraft:ochre_froglight");
        MATERIAL_MAP.put("verdant_froglight", "minecraft:verdant_froglight");
        MATERIAL_MAP.put("pearlescent_froglight", "minecraft:pearlescent_froglight");
        MATERIAL_MAP.put("blue_ice", "minecraft:blue_ice");
        MATERIAL_MAP.put("dark_prismarine", "minecraft:dark_prismarine");
        MATERIAL_MAP.put("prismarine_bricks", "minecraft:prismarine_bricks");
        MATERIAL_MAP.put("lodestone", "minecraft:lodestone");
        MATERIAL_MAP.put("target", "minecraft:target");
        MATERIAL_MAP.put("tnt", "minecraft:tnt");
        MATERIAL_MAP.put("gold_nugget", "minecraft:gold_nugget");
        MATERIAL_MAP.put("iron_nugget", "minecraft:iron_nugget");
        MATERIAL_MAP.put("nether_gold_ore", "minecraft:nether_gold_ore");
        MATERIAL_MAP.put("cut_copper", "minecraft:cut_copper");
        MATERIAL_MAP.put("exposed_copper", "minecraft:exposed_copper");
        MATERIAL_MAP.put("weathered_copper", "minecraft:weathered_copper");
        MATERIAL_MAP.put("oxidised_copper", "minecraft:oxidized_copper");
        MATERIAL_MAP.put("waxed_cut_copper", "minecraft:waxed_cut_copper");
        MATERIAL_MAP.put("chiseled_copper", "minecraft:chiseled_copper");
        MATERIAL_MAP.put("waxed", "minecraft:waxed_copper_block");
        MATERIAL_MAP.put("warped", "minecraft:warped_planks");
        MATERIAL_MAP.put("warped_wart_block", "minecraft:warped_wart_block");
        MATERIAL_MAP.put("nether_wart_block", "minecraft:nether_wart_block");
    }

    public static void main(String[] args) throws IOException {
        List<String> itemIds = Files.readAllLines(Paths.get("all_item_ids.txt"));
        JsonArray recipes = new JsonArray();

        for (String itemId : itemIds) {
            itemId = itemId.trim();
            if (itemId.isEmpty() || itemId.startsWith("enchanted_") || 
                itemId.startsWith("compacted_") || itemId.startsWith("super_compacted_") ||
                itemId.startsWith("attribute_") || itemId.endsWith("_") ||
                itemId.contains("crown") || itemId.contains("gilded") ||
                itemId.contains("midas") || itemId.contains("hardened") ||
                itemId.contains("heavy_") || itemId.contains("reinforced_") ||
                itemId.contains("sharpened_") || itemId.contains("swift_") ||
                itemId.contains("venomous") || itemId.contains("void_touched") ||
                itemId.contains("voidwalker") || itemId.contains("ender_phase") ||
                itemId.contains("glistening") || itemId.contains("polished_gold") ||
                itemId.contains("pure_gold") || itemId.contains("shiny_gold") ||
                itemId.contains("zombie_berserker") || itemId.contains("spider_leggings") ||
                itemId.contains("warden_chestplate") || itemId.contains("slime_boots") ||
                itemId.contains("piglin_core") || itemId.contains("piglin_flesh") ||
                itemId.contains("skeleton_bow") || itemId.contains("weapon_attribute")) {
                continue; // Skip non-craftable items for now
            }

            JsonObject recipe = createRecipe(itemId);
            if (recipe != null) {
                recipes.add(recipe);
            }
        }

        // Add existing special recipes from the current file
        addSpecialRecipes(recipes);

        try (FileWriter writer = new FileWriter("config/political/custom_recipes.json")) {
            GSON.toJson(recipes, writer);
        }

        System.out.println("Generated " + recipes.size() + " recipes");
    }

    private static JsonObject createRecipe(String itemId) {
        JsonObject recipe = new JsonObject();
        String name = snakeToTitle(itemId);
        recipe.addProperty("name", name);
        recipe.addProperty("requiredSlayer", "");
        recipe.addProperty("requiredLevel", 0);

        JsonObject result = new JsonObject();
        result.addProperty("id", "political:" + itemId);
        result.addProperty("count", 1);
        recipe.add("result", result);

        JsonArray ingredients = new JsonArray();
        JsonArray lockedSlots = new JsonArray();

        // Determine item type and generate appropriate recipe
        if (itemId.endsWith("_helmet")) {
            generateArmorRecipe(ingredients, lockedSlots, itemId, "helmet");
        } else if (itemId.endsWith("_chestplate")) {
            generateArmorRecipe(ingredients, lockedSlots, itemId, "chestplate");
        } else if (itemId.endsWith("_leggings")) {
            generateArmorRecipe(ingredients, lockedSlots, itemId, "leggings");
        } else if (itemId.endsWith("_boots")) {
            generateArmorRecipe(ingredients, lockedSlots, itemId, "boots");
        } else if (itemId.endsWith("_sword")) {
            generateSwordRecipe(ingredients, lockedSlots, itemId);
        } else if (itemId.endsWith("_pickaxe")) {
            generateToolRecipe(ingredients, lockedSlots, itemId, "pickaxe");
        } else if (itemId.endsWith("_axe")) {
            generateToolRecipe(ingredients, lockedSlots, itemId, "axe");
        } else if (itemId.endsWith("_shovel")) {
            generateToolRecipe(ingredients, lockedSlots, itemId, "shovel");
        } else if (itemId.endsWith("_hoe")) {
            generateToolRecipe(ingredients, lockedSlots, itemId, "hoe");
        } else if (itemId.contains("_core")) {
            return null; // Cores don't have recipes
        } else {
            return null; // Unknown type
        }

        recipe.add("ingredients", ingredients);
        recipe.add("lockedSlots", lockedSlots);

        return recipe;
    }

    private static void generateArmorRecipe(JsonArray ingredients, JsonArray lockedSlots, String itemId, String type) {
        String material = getMaterial(itemId.replace("_" + type, ""));
        int coreTier = getCoreTier(material);
        String coreId = "political:custom_crafter_core_t" + coreTier;

        // Initialize 9 slots (all empty)
        for (int i = 0; i < 9; i++) {
            ingredients.add("");
            lockedSlots.add(new JsonPrimitive(false));
        }

        // Set material based on armor type
        JsonObject mat = new JsonObject();
        mat.addProperty("id", material);
        mat.addProperty("count", 1);

        if (type.equals("helmet")) {
            ingredients.set(0, mat); lockedSlots.set(0, new JsonPrimitive(true));
            ingredients.set(1, mat); lockedSlots.set(1, new JsonPrimitive(true));
            ingredients.set(2, mat); lockedSlots.set(2, new JsonPrimitive(true));
            ingredients.set(3, mat); lockedSlots.set(3, new JsonPrimitive(true));
            ingredients.set(5, mat); lockedSlots.set(5, new JsonPrimitive(true));
            JsonObject core = new JsonObject(); core.addProperty("id", coreId); core.addProperty("count", 1);
            ingredients.set(7, core); lockedSlots.set(7, new JsonPrimitive(true));
        } else if (type.equals("chestplate")) {
            ingredients.set(0, mat); lockedSlots.set(0, new JsonPrimitive(true));
            ingredients.set(2, mat); lockedSlots.set(2, new JsonPrimitive(true));
            ingredients.set(3, mat); lockedSlots.set(3, new JsonPrimitive(true));
            ingredients.set(4, mat); lockedSlots.set(4, new JsonPrimitive(true));
            ingredients.set(5, mat); lockedSlots.set(5, new JsonPrimitive(true));
            ingredients.set(6, mat); lockedSlots.set(6, new JsonPrimitive(true));
            JsonObject core = new JsonObject(); core.addProperty("id", coreId); core.addProperty("count", 1);
            ingredients.set(7, core); lockedSlots.set(7, new JsonPrimitive(true));
            ingredients.set(8, mat); lockedSlots.set(8, new JsonPrimitive(true));
        } else if (type.equals("leggings")) {
            ingredients.set(0, mat); lockedSlots.set(0, new JsonPrimitive(true));
            ingredients.set(1, mat); lockedSlots.set(1, new JsonPrimitive(true));
            ingredients.set(2, mat); lockedSlots.set(2, new JsonPrimitive(true));
            ingredients.set(3, mat); lockedSlots.set(3, new JsonPrimitive(true));
            ingredients.set(5, mat); lockedSlots.set(5, new JsonPrimitive(true));
            ingredients.set(6, mat); lockedSlots.set(6, new JsonPrimitive(true));
            JsonObject core = new JsonObject(); core.addProperty("id", coreId); core.addProperty("count", 1);
            ingredients.set(7, core); lockedSlots.set(7, new JsonPrimitive(true));
            ingredients.set(8, mat); lockedSlots.set(8, new JsonPrimitive(true));
        } else if (type.equals("boots")) {
            ingredients.set(3, mat); lockedSlots.set(3, new JsonPrimitive(true));
            ingredients.set(5, mat); lockedSlots.set(5, new JsonPrimitive(true));
            ingredients.set(6, mat); lockedSlots.set(6, new JsonPrimitive(true));
            JsonObject core = new JsonObject(); core.addProperty("id", coreId); core.addProperty("count", 1);
            ingredients.set(7, core); lockedSlots.set(7, new JsonPrimitive(true));
            ingredients.set(8, mat); lockedSlots.set(8, new JsonPrimitive(true));
        }
    }

    private static void generateSwordRecipe(JsonArray ingredients, JsonArray lockedSlots, String itemId) {
        String material = getMaterial(itemId.replace("_sword", ""));
        int coreTier = getCoreTier(material);
        String coreId = "political:custom_crafter_core_t" + coreTier;

        for (int i = 0; i < 9; i++) {
            ingredients.add("");
            lockedSlots.add(new JsonPrimitive(false));
        }

        JsonObject mat = new JsonObject();
        mat.addProperty("id", material);
        mat.addProperty("count", 1);

        ingredients.set(1, mat); lockedSlots.set(1, new JsonPrimitive(true));
        ingredients.set(4, mat); lockedSlots.set(4, new JsonPrimitive(true));
        JsonObject stick = new JsonObject(); stick.addProperty("id", "minecraft:stick"); stick.addProperty("count", 1);
        ingredients.set(7, stick); lockedSlots.set(7, new JsonPrimitive(true));
        JsonObject core = new JsonObject(); core.addProperty("id", coreId); core.addProperty("count", 1);
        ingredients.set(8, core); lockedSlots.set(8, new JsonPrimitive(true));
    }

    private static void generateToolRecipe(JsonArray ingredients, JsonArray lockedSlots, String itemId, String type) {
        String material = getMaterial(itemId.replace("_" + type, ""));
        int coreTier = getCoreTier(material);
        String coreId = "political:custom_crafter_core_t" + coreTier;

        for (int i = 0; i < 9; i++) {
            ingredients.add("");
            lockedSlots.add(new JsonPrimitive(false));
        }

        JsonObject mat = new JsonObject();
        mat.addProperty("id", material);
        mat.addProperty("count", 1);

        JsonObject stick = new JsonObject(); stick.addProperty("id", "minecraft:stick"); stick.addProperty("count", 1);
        JsonObject core = new JsonObject(); core.addProperty("id", coreId); core.addProperty("count", 1);

        if (type.equals("pickaxe")) {
            ingredients.set(0, mat); lockedSlots.set(0, new JsonPrimitive(true));
            ingredients.set(1, mat); lockedSlots.set(1, new JsonPrimitive(true));
            ingredients.set(2, mat); lockedSlots.set(2, new JsonPrimitive(true));
            ingredients.set(4, stick); lockedSlots.set(4, new JsonPrimitive(true));
            ingredients.set(7, stick); lockedSlots.set(7, new JsonPrimitive(true));
            ingredients.set(8, core); lockedSlots.set(8, new JsonPrimitive(true));
        } else if (type.equals("axe")) {
            ingredients.set(0, mat); lockedSlots.set(0, new JsonPrimitive(true));
            ingredients.set(1, mat); lockedSlots.set(1, new JsonPrimitive(true));
            ingredients.set(3, mat); lockedSlots.set(3, new JsonPrimitive(true));
            ingredients.set(4, stick); lockedSlots.set(4, new JsonPrimitive(true));
            ingredients.set(7, stick); lockedSlots.set(7, new JsonPrimitive(true));
            ingredients.set(8, core); lockedSlots.set(8, new JsonPrimitive(true));
        } else if (type.equals("shovel")) {
            ingredients.set(1, mat); lockedSlots.set(1, new JsonPrimitive(true));
            ingredients.set(4, stick); lockedSlots.set(4, new JsonPrimitive(true));
            ingredients.set(7, stick); lockedSlots.set(7, new JsonPrimitive(true));
            ingredients.set(8, core); lockedSlots.set(8, new JsonPrimitive(true));
        } else if (type.equals("hoe")) {
            ingredients.set(0, mat); lockedSlots.set(0, new JsonPrimitive(true));
            ingredients.set(1, mat); lockedSlots.set(1, new JsonPrimitive(true));
            ingredients.set(4, stick); lockedSlots.set(4, new JsonPrimitive(true));
            ingredients.set(7, stick); lockedSlots.set(7, new JsonPrimitive(true));
            ingredients.set(8, core); lockedSlots.set(8, new JsonPrimitive(true));
        }
    }

    private static String getMaterial(String itemBase) {
        // Check direct map
        if (MATERIAL_MAP.containsKey(itemBase)) {
            return MATERIAL_MAP.get(itemBase);
        }

        // Try with _block suffix
        if (MATERIAL_MAP.containsKey(itemBase + "_block")) {
            return MATERIAL_MAP.get(itemBase + "_block");
        }

        // Default fallback
        return "minecraft:" + itemBase.replace("_block", "").replace("_planks", "");
    }

    private static int getCoreTier(String material) {
        // T3 materials (high tier)
        if (material.contains("obsidian") || material.contains("netherite") || 
            material.contains("ancient_debris") || material.contains("crying_obsidian") ||
            material.contains("reinforced_deepslate")) {
            return 3;
        }
        // T2 materials (mid tier)
        if (material.contains("diamond") || material.contains("emerald") || 
            material.contains("amethyst") || material.contains("end") ||
            material.contains("purpur") || material.contains("sculk") ||
            material.contains("terracotta") || material.contains("mossy_cobblestone") ||
            material.contains("mossy") || material.contains("blackstone") ||
            material.contains("gilded") || material.contains("prismarine_bricks") ||
            material.contains("dark_prismarine") || material.contains("red_sandstone") ||
            material.contains("cut_copper") || material.contains("exposed_copper") ||
            material.contains("weathered_copper") || material.contains("oxidised_copper") ||
            material.contains("waxed_cut_copper") || material.contains("chiseled_copper") ||
            material.contains("polished_blackstone") || material.contains("crimson") ||
            material.contains("warped") || material.contains("basalt") ||
            material.contains("polished_basalt") || material.contains("smooth_basalt")) {
            return 2;
        }
        // T1 materials (basic)
        return 1;
    }

    private static String snakeToTitle(String snake) {
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;
        for (char c : snake.toCharArray()) {
            if (c == '_') {
                result.append(' ');
                capitalizeNext = true;
            } else {
                result.append(capitalizeNext ? Character.toUpperCase(c) : c);
                capitalizeNext = false;
            }
        }
        return result.toString();
    }

    private static void addSpecialRecipes(JsonArray recipes) {
        // Add custom crafter core recipes here if needed
    }
}
