package com.political;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.util.*;

public class RecipeBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(RecipeBuilder.class);


    public static void main(String[] args) throws IOException {
        Map<String, String> materials = new HashMap<>();
        RecipeMaterials.registerAll(materials);
        // Match legacy generator: later keys overwrote some block entries with ingots/dust for tool-style rows.
        materials.put("coal", "minecraft:coal");
        materials.put("emerald", "minecraft:emerald");
        materials.put("diamond", "minecraft:diamond");
        materials.put("iron_ingot", "minecraft:iron_ingot");
        materials.put("gold_ingot", "minecraft:gold_ingot");
        materials.put("netherite_ingot", "minecraft:netherite_ingot");
        materials.put("copper_ingot", "minecraft:copper_ingot");
        materials.put("lapis_lazuli", "minecraft:lapis_lazuli");
        materials.put("quartz", "minecraft:quartz");
        materials.put("redstone", "minecraft:redstone");

        // Generate recipes
        List<String> recipes = new ArrayList<>();

        // Generate for each material
        for (Map.Entry<String, String> entry : materials.entrySet()) {
            String matName = entry.getKey();
            String matId = entry.getValue();
            int tier = getTier(matId);
            String coreId = "political:custom_crafter_core_t" + tier;

            // Armor recipes
            recipes.add(createArmorRecipe(matName, matId, coreId, "helmet"));
            recipes.add(createArmorRecipe(matName, matId, coreId, "chestplate"));
            recipes.add(createArmorRecipe(matName, matId, coreId, "leggings"));
            recipes.add(createArmorRecipe(matName, matId, coreId, "boots"));

            // Sword recipe
            recipes.add(createSwordRecipe(matName, matId, coreId));

            // Tool recipes
            recipes.add(createToolRecipe(matName, matId, coreId, "pickaxe"));
            recipes.add(createToolRecipe(matName, matId, coreId, "axe"));
            recipes.add(createToolRecipe(matName, matId, coreId, "shovel"));
            recipes.add(createToolRecipe(matName, matId, coreId, "hoe"));
        }

        // Write to file
        try (PrintWriter out = new PrintWriter(new FileWriter("config/political/custom_recipes.json"))) {
            out.println("[");
            for (int i = 0; i < recipes.size(); i++) {
                out.println(recipes.get(i));
                if (i < recipes.size() - 1) {
                    out.println(",");
                }
            }
            out.println("]");
        }

        LOGGER.info("Generated {} recipes", recipes.size());
    }

    private static int getTier(String material) {
        String m = material.toLowerCase();
        if (m.contains("obsidian") || m.contains("netherite") || m.contains("ancient_debris") || m.contains("crying_obsidian") || m.contains("reinforced_deepslate")) {
            return 3;
        }
        if (m.contains("diamond") || m.contains("emerald") || m.contains("amethyst") || m.contains("end_stone") ||
            m.contains("purpur") || m.contains("sculk") || m.contains("terracotta") || m.contains("mossy_cobblestone") ||
            m.contains("blackstone") || m.contains("gilded") || m.contains("prismarine_bricks") || m.contains("dark_prismarine") ||
            m.contains("red_sandstone") || m.contains("cut_copper") || m.contains("exposed_copper") ||
            m.contains("weathered_copper") || m.contains("oxidized_copper") || m.contains("waxed_cut_copper") ||
            m.contains("chiseled_copper") || m.contains("polished_blackstone") || m.contains("crimson") ||
            m.contains("warped") || m.contains("basalt") || m.contains("polished_basalt") || m.contains("smooth_basalt") ||
            m.contains("polished_deepslate") || m.contains("reinforced_deepslate") || m.contains("tuff") ||
            m.contains("polished_tuff") || m.contains("calcite") || m.contains("dripstone") ||
            m.contains("chiseled_deepslate") || m.contains("chiseled_nether_bricks") || m.contains("cracked_nether_bricks") ||
            m.contains("cracked_stone_bricks") || m.contains("chiseled_stone_bricks") || m.contains("deepslate_bricks") ||
            m.contains("deepslate_tiles") || m.contains("ochre_froglight") || m.contains("verdant_froglight") ||
            m.contains("pearlescent_froglight") || m.contains("shroomlight") || m.contains("stripped_warped") ||
            m.contains("stripped_crimson") || m.contains("stripped_bamboo") || m.contains("bamboo_block") ||
            m.contains("cherry_log") || m.contains("cherry_planks") || m.contains("stripped_cherry") ||
            m.contains("mangrove_log") || m.contains("mangrove_planks") || m.contains("stripped_mangrove") ||
            m.contains("mud_bricks") || m.contains("packed_mud")) {
            return 2;
        }
        return 1;
    }

    private static String createArmorRecipe(String matName, String matId, String coreId, String type) {
        String name = matName.replace("_", " ").toUpperCase().charAt(0) + matName.replace("_", " ").substring(1) + " " + type.substring(0, 1).toUpperCase() + type.substring(1);
        String itemId = matName + "_" + type;

        StringBuilder sb = new StringBuilder();
        sb.append("  {\n");
        sb.append("    \"name\": \"").append(name).append("\",\n");
        sb.append("    \"requiredSlayer\": \"\",\n");
        sb.append("    \"requiredLevel\": 0,\n");
        sb.append("    \"result\": { \"id\": \"political:").append(itemId).append("\", \"count\": 1 },\n");
        sb.append("    \"ingredients\": [\n");

        if (type.equals("helmet")) {
            sb.append("      { \"id\": \"").append(matId).append("\", \"count\": 1 },\n");
            sb.append("      { \"id\": \"").append(matId).append("\", \"count\": 1 },\n");
            sb.append("      { \"id\": \"").append(matId).append("\", \"count\": 1 },\n");
            sb.append("      { \"id\": \"").append(matId).append("\", \"count\": 1 },\n");
            sb.append("      \"\",\n");
            sb.append("      { \"id\": \"").append(matId).append("\", \"count\": 1 },\n");
            sb.append("      \"\",\n");
            sb.append("      { \"id\": \"").append(coreId).append("\", \"count\": 1 },\n");
            sb.append("      \"\"\n");
        } else if (type.equals("chestplate")) {
            sb.append("      { \"id\": \"").append(matId).append("\", \"count\": 1 },\n");
            sb.append("      \"\",\n");
            sb.append("      { \"id\": \"").append(matId).append("\", \"count\": 1 },\n");
            sb.append("      { \"id\": \"").append(matId).append("\", \"count\": 1 },\n");
            sb.append("      { \"id\": \"").append(matId).append("\", \"count\": 1 },\n");
            sb.append("      { \"id\": \"").append(matId).append("\", \"count\": 1 },\n");
            sb.append("      { \"id\": \"").append(matId).append("\", \"count\": 1 },\n");
            sb.append("      { \"id\": \"").append(coreId).append("\", \"count\": 1 },\n");
            sb.append("      { \"id\": \"").append(matId).append("\", \"count\": 1 }\n");
        } else if (type.equals("leggings")) {
            sb.append("      { \"id\": \"").append(matId).append("\", \"count\": 1 },\n");
            sb.append("      { \"id\": \"").append(matId).append("\", \"count\": 1 },\n");
            sb.append("      { \"id\": \"").append(matId).append("\", \"count\": 1 },\n");
            sb.append("      { \"id\": \"").append(matId).append("\", \"count\": 1 },\n");
            sb.append("      \"\",\n");
            sb.append("      { \"id\": \"").append(matId).append("\", \"count\": 1 },\n");
            sb.append("      { \"id\": \"").append(matId).append("\", \"count\": 1 },\n");
            sb.append("      { \"id\": \"").append(coreId).append("\", \"count\": 1 },\n");
            sb.append("      { \"id\": \"").append(matId).append("\", \"count\": 1 }\n");
        } else if (type.equals("boots")) {
            sb.append("      \"\",\n");
            sb.append("      \"\",\n");
            sb.append("      \"\",\n");
            sb.append("      { \"id\": \"").append(matId).append("\", \"count\": 1 },\n");
            sb.append("      \"\",\n");
            sb.append("      { \"id\": \"").append(matId).append("\", \"count\": 1 },\n");
            sb.append("      { \"id\": \"").append(matId).append("\", \"count\": 1 },\n");
            sb.append("      { \"id\": \"").append(coreId).append("\", \"count\": 1 },\n");
            sb.append("      { \"id\": \"").append(matId).append("\", \"count\": 1 }\n");
        }

        sb.append("    ],\n");
        sb.append("    \"lockedSlots\": [true, true, true, true, true, true, true, true, true]\n");
        sb.append("  }");
        return sb.toString();
    }

    private static String createSwordRecipe(String matName, String matId, String coreId) {
        String name = matName.replace("_", " ").toUpperCase().charAt(0) + matName.replace("_", " ").substring(1) + " Sword";
        String itemId = matName + "_sword";

        StringBuilder sb = new StringBuilder();
        sb.append("  {\n");
        sb.append("    \"name\": \"").append(name).append("\",\n");
        sb.append("    \"requiredSlayer\": \"\",\n");
        sb.append("    \"requiredLevel\": 0,\n");
        sb.append("    \"result\": { \"id\": \"political:").append(itemId).append("\", \"count\": 1 },\n");
        sb.append("    \"ingredients\": [\n");
        sb.append("      \"\",\n");
        sb.append("      { \"id\": \"").append(matId).append("\", \"count\": 1 },\n");
        sb.append("      \"\",\n");
        sb.append("      \"\",\n");
        sb.append("      { \"id\": \"").append(matId).append("\", \"count\": 1 },\n");
        sb.append("      \"\",\n");
        sb.append("      \"\",\n");
        sb.append("      { \"id\": \"minecraft:stick\", \"count\": 1 },\n");
        sb.append("      { \"id\": \"").append(coreId).append("\", \"count\": 1 }\n");
        sb.append("    ],\n");
        sb.append("    \"lockedSlots\": [true, true, true, true, true, true, true, true, true]\n");
        sb.append("  }");
        return sb.toString();
    }

    private static String createToolRecipe(String matName, String matId, String coreId, String type) {
        String name = matName.replace("_", " ").toUpperCase().charAt(0) + matName.replace("_", " ").substring(1) + " " + type.substring(0, 1).toUpperCase() + type.substring(1);
        String itemId = matName + "_" + type;

        StringBuilder sb = new StringBuilder();
        sb.append("  {\n");
        sb.append("    \"name\": \"").append(name).append("\",\n");
        sb.append("    \"requiredSlayer\": \"\",\n");
        sb.append("    \"requiredLevel\": 0,\n");
        sb.append("    \"result\": { \"id\": \"political:").append(itemId).append("\", \"count\": 1 },\n");
        sb.append("    \"ingredients\": [\n");

        if (type.equals("pickaxe")) {
            sb.append("      { \"id\": \"").append(matId).append("\", \"count\": 1 },\n");
            sb.append("      { \"id\": \"").append(matId).append("\", \"count\": 1 },\n");
            sb.append("      { \"id\": \"").append(matId).append("\", \"count\": 1 },\n");
            sb.append("      \"\",\n");
            sb.append("      { \"id\": \"minecraft:stick\", \"count\": 1 },\n");
            sb.append("      \"\",\n");
            sb.append("      \"\",\n");
            sb.append("      { \"id\": \"minecraft:stick\", \"count\": 1 },\n");
            sb.append("      { \"id\": \"").append(coreId).append("\", \"count\": 1 }\n");
        } else if (type.equals("axe")) {
            sb.append("      { \"id\": \"").append(matId).append("\", \"count\": 1 },\n");
            sb.append("      { \"id\": \"").append(matId).append("\", \"count\": 1 },\n");
            sb.append("      \"\",\n");
            sb.append("      { \"id\": \"").append(matId).append("\", \"count\": 1 },\n");
            sb.append("      { \"id\": \"minecraft:stick\", \"count\": 1 },\n");
            sb.append("      \"\",\n");
            sb.append("      \"\",\n");
            sb.append("      { \"id\": \"minecraft:stick\", \"count\": 1 },\n");
            sb.append("      { \"id\": \"").append(coreId).append("\", \"count\": 1 }\n");
        } else if (type.equals("shovel")) {
            sb.append("      \"\",\n");
            sb.append("      { \"id\": \"").append(matId).append("\", \"count\": 1 },\n");
            sb.append("      \"\",\n");
            sb.append("      \"\",\n");
            sb.append("      { \"id\": \"minecraft:stick\", \"count\": 1 },\n");
            sb.append("      \"\",\n");
            sb.append("      \"\",\n");
            sb.append("      { \"id\": \"minecraft:stick\", \"count\": 1 },\n");
            sb.append("      { \"id\": \"").append(coreId).append("\", \"count\": 1 }\n");
        } else if (type.equals("hoe")) {
            sb.append("      { \"id\": \"").append(matId).append("\", \"count\": 1 },\n");
            sb.append("      { \"id\": \"").append(matId).append("\", \"count\": 1 },\n");
            sb.append("      \"\",\n");
            sb.append("      \"\",\n");
            sb.append("      { \"id\": \"minecraft:stick\", \"count\": 1 },\n");
            sb.append("      \"\",\n");
            sb.append("      \"\",\n");
            sb.append("      { \"id\": \"minecraft:stick\", \"count\": 1 },\n");
            sb.append("      { \"id\": \"").append(coreId).append("\", \"count\": 1 }\n");
        }

        sb.append("    ],\n");
        sb.append("    \"lockedSlots\": [true, true, true, true, true, true, true, true, true]\n");
        sb.append("  }");
        return sb.toString();
    }
}
