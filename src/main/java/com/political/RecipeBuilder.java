package com.political;

import java.io.*;
import java.util.*;

public class RecipeBuilder {

    public static void main(String[] args) throws IOException {
        // Material to Minecraft item ID mapping
        Map<String, String> materials = new HashMap<>();
        materials.put("glass", "minecraft:glass");
        materials.put("obsidian", "minecraft:obsidian");
        materials.put("quartz", "minecraft:quartz_block");
        materials.put("glowstone", "minecraft:glowstone");
        materials.put("redstone", "minecraft:redstone_block");
        materials.put("netherrack", "minecraft:netherrack");
        materials.put("endstone", "minecraft:end_stone");
        materials.put("end_stone", "minecraft:end_stone");
        materials.put("ice", "minecraft:packed_ice");
        materials.put("packed_ice", "minecraft:packed_ice");
        materials.put("prismarine", "minecraft:prismarine");
        materials.put("terracotta", "minecraft:terracotta");
        materials.put("mossy", "minecraft:mossy_cobblestone");
        materials.put("mossy_cobblestone", "minecraft:mossy_cobblestone");
        materials.put("soul_sand", "minecraft:soul_sand");
        materials.put("soul_soil", "minecraft:soul_soil");
        materials.put("magma", "minecraft:magma_block");
        materials.put("sponge", "minecraft:sponge");
        materials.put("wet_sponge", "minecraft:wet_sponge");
        materials.put("snow", "minecraft:snow_block");
        materials.put("snow_block", "minecraft:snow_block");
        materials.put("sculk", "minecraft:sculk");
        materials.put("lapis", "minecraft:lapis_block");
        materials.put("lapis_lazuli", "minecraft:lapis_lazuli");
        materials.put("lapis_block", "minecraft:lapis_block");
        materials.put("emerald", "minecraft:emerald_block");
        materials.put("coal", "minecraft:coal_block");
        materials.put("diamond", "minecraft:diamond_block");
        materials.put("gold", "minecraft:gold_block");
        materials.put("iron", "minecraft:iron_block");
        materials.put("netherite", "minecraft:netherite_block");
        materials.put("copper", "minecraft:copper_block");
        materials.put("amethyst", "minecraft:amethyst_block");
        materials.put("stone", "minecraft:stone");
        materials.put("cobblestone", "minecraft:cobblestone");
        materials.put("stone_bricks", "minecraft:stone_bricks");
        materials.put("smooth_stone", "minecraft:smooth_stone");
        materials.put("granite", "minecraft:granite");
        materials.put("diorite", "minecraft:diorite");
        materials.put("andesite", "minecraft:andesite");
        materials.put("polished_granite", "minecraft:polished_granite");
        materials.put("polished_diorite", "minecraft:polished_diorite");
        materials.put("polished_andesite", "minecraft:polished_andesite");
        materials.put("deepslate", "minecraft:deepslate");
        materials.put("cobbled_deepslate", "minecraft:cobbled_deepslate");
        materials.put("polished_deepslate", "minecraft:polished_deepslate");
        materials.put("tuff", "minecraft:tuff");
        materials.put("polished_tuff", "minecraft:polished_tuff");
        materials.put("calcite", "minecraft:calcite");
        materials.put("dripstone", "minecraft:dripstone_block");
        materials.put("sandstone", "minecraft:sandstone");
        materials.put("red_sandstone", "minecraft:red_sandstone");
        materials.put("sand", "minecraft:sand");
        materials.put("red_sand", "minecraft:red_sand");
        materials.put("bricks", "minecraft:bricks");
        materials.put("mud_bricks", "minecraft:mud_bricks");
        materials.put("nether_bricks", "minecraft:nether_bricks");
        materials.put("basalt", "minecraft:basalt");
        materials.put("polished_basalt", "minecraft:polished_basalt");
        materials.put("smooth_basalt", "minecraft:smooth_basalt");
        materials.put("blackstone", "minecraft:blackstone");
        materials.put("polished_blackstone", "minecraft:polished_blackstone");
        materials.put("crying_obsidian", "minecraft:crying_obsidian");
        materials.put("purpur", "minecraft:purpur_block");
        materials.put("bone_block", "minecraft:bone_block");
        materials.put("dried_kelp", "minecraft:dried_kelp_block");
        materials.put("hay", "minecraft:hay_block");
        materials.put("honeycomb", "minecraft:honeycomb");
        materials.put("honeycomb_block", "minecraft:honeycomb_block");
        materials.put("slime", "minecraft:slime_block");
        materials.put("pumpkin", "minecraft:pumpkin");
        materials.put("melon", "minecraft:melon");
        materials.put("cactus", "minecraft:cactus");
        materials.put("lily_pad", "minecraft:lily_pad");
        materials.put("oak_log", "minecraft:oak_log");
        materials.put("oak_planks", "minecraft:oak_planks");
        materials.put("spruce_log", "minecraft:spruce_log");
        materials.put("spruce_planks", "minecraft:spruce_planks");
        materials.put("birch_log", "minecraft:birch_log");
        materials.put("birch_planks", "minecraft:birch_planks");
        materials.put("jungle_log", "minecraft:jungle_log");
        materials.put("jungle_planks", "minecraft:jungle_planks");
        materials.put("acacia_log", "minecraft:acacia_log");
        materials.put("acacia_planks", "minecraft:acacia_planks");
        materials.put("dark_oak_log", "minecraft:dark_oak_log");
        materials.put("dark_oak_planks", "minecraft:dark_oak_planks");
        materials.put("mangrove_log", "minecraft:mangrove_log");
        materials.put("mangrove_planks", "minecraft:mangrove_planks");
        materials.put("cherry_log", "minecraft:cherry_log");
        materials.put("cherry_planks", "minecraft:cherry_planks");
        materials.put("bamboo_block", "minecraft:bamboo_block");
        materials.put("bamboo_planks", "minecraft:bamboo_planks");
        materials.put("crimson_stem", "minecraft:crimson_stem");
        materials.put("crimson_planks", "minecraft:crimson_planks");
        materials.put("warped_stem", "minecraft:warped_stem");
        materials.put("warped_planks", "minecraft:warped_planks");
        materials.put("stripped_oak_log", "minecraft:stripped_oak_log");
        materials.put("stripped_spruce_log", "minecraft:stripped_spruce_log");
        materials.put("stripped_birch_log", "minecraft:stripped_birch_log");
        materials.put("stripped_jungle_log", "minecraft:stripped_jungle_log");
        materials.put("stripped_acacia_log", "minecraft:stripped_acacia_log");
        materials.put("stripped_dark_oak_log", "minecraft:stripped_dark_oak_log");
        materials.put("stripped_mangrove_log", "minecraft:stripped_mangrove_log");
        materials.put("stripped_cherry_log", "minecraft:stripped_cherry_log");
        materials.put("stripped_bamboo_block", "minecraft:stripped_bamboo_block");
        materials.put("stripped_crimson_stem", "minecraft:stripped_crimson_stem");
        materials.put("stripped_warped_stem", "minecraft:stripped_warped_stem");
        materials.put("moss_block", "minecraft:moss_block");
        materials.put("mycelium", "minecraft:mycelium");
        materials.put("dirt", "minecraft:dirt");
        materials.put("coarse_dirt", "minecraft:coarse_dirt");
        materials.put("podzol", "minecraft:podzol");
        materials.put("rooted_dirt", "minecraft:rooted_dirt");
        materials.put("mud", "minecraft:mud");
        materials.put("clay", "minecraft:clay");
        materials.put("gravel", "minecraft:gravel");
        materials.put("mangrove_roots", "minecraft:mangrove_roots");
        materials.put("muddy_mangrove_roots", "minecraft:muddy_mangrove_roots");
        materials.put("sea_lantern", "minecraft:sea_lantern");
        materials.put("shroomlight", "minecraft:shroomlight");
        materials.put("ochre_froglight", "minecraft:ochre_froglight");
        materials.put("verdant_froglight", "minecraft:verdant_froglight");
        materials.put("pearlescent_froglight", "minecraft:pearlescent_froglight");
        materials.put("blue_ice", "minecraft:blue_ice");
        materials.put("dark_prismarine", "minecraft:dark_prismarine");
        materials.put("prismarine_bricks", "minecraft:prismarine_bricks");
        materials.put("lodestone", "minecraft:lodestone");
        materials.put("target", "minecraft:target");
        materials.put("tnt", "minecraft:tnt");
        materials.put("gold_nugget", "minecraft:gold_nugget");
        materials.put("iron_nugget", "minecraft:iron_nugget");
        materials.put("raw_iron", "minecraft:raw_iron_block");
        materials.put("raw_iron_block", "minecraft:raw_iron_block");
        materials.put("raw_gold", "minecraft:raw_gold_block");
        materials.put("raw_gold_block", "minecraft:raw_gold_block");
        materials.put("ancient_debris", "minecraft:ancient_debris");
        materials.put("coal_block", "minecraft:coal_block");
        materials.put("diamond_block", "minecraft:diamond_block");
        materials.put("emerald_block", "minecraft:emerald_block");
        materials.put("gold_block", "minecraft:gold_block");
        materials.put("iron_block", "minecraft:iron_block");
        materials.put("lapis_block", "minecraft:lapis_block");
        materials.put("netherite_block", "minecraft:netherite_block");
        materials.put("amethyst_block", "minecraft:amethyst_block");
        materials.put("quartz_block", "minecraft:quartz_block");
        materials.put("redstone_block", "minecraft:redstone_block");
        materials.put("copper_block", "minecraft:copper_block");
        materials.put("exposed_copper", "minecraft:exposed_copper");
        materials.put("weathered_copper", "minecraft:weathered_copper");
        materials.put("oxidised_copper", "minecraft:oxidized_copper");
        materials.put("cut_copper", "minecraft:cut_copper");
        materials.put("waxed_cut_copper", "minecraft:waxed_cut_copper");
        materials.put("chiseled_copper", "minecraft:chiseled_copper");
        materials.put("nether_wart_block", "minecraft:nether_wart_block");
        materials.put("warped_wart_block", "minecraft:warped_wart_block");
        materials.put("chiseled_deepslate", "minecraft:chiseled_deepslate");
        materials.put("chiseled_nether_bricks", "minecraft:chiseled_nether_bricks");
        materials.put("cracked_nether_bricks", "minecraft:cracked_nether_bricks");
        materials.put("cracked_stone_bricks", "minecraft:cracked_stone_bricks");
        materials.put("chiseled_stone_bricks", "minecraft:chiseled_stone_bricks");
        materials.put("end_stone_bricks", "minecraft:end_stone_bricks");
        materials.put("gilded_blackstone", "minecraft:gilded_blackstone");
        materials.put("blackstone_bricks", "minecraft:polished_blackstone_bricks");
        materials.put("reinforced_deepslate", "minecraft:reinforced_deepslate");
        materials.put("bone_meal", "minecraft:bone_meal");
        materials.put("nether_gold_ore", "minecraft:nether_gold_ore");
        materials.put("chiseled_tuff", "minecraft:chiseled_tuff");
        materials.put("tuff_bricks", "minecraft:tuff_bricks");
        materials.put("amethyst_cluster", "minecraft:amethyst_cluster");
        materials.put("amethyst_shard", "minecraft:amethyst_shard");
        materials.put("flint", "minecraft:flint");
        materials.put("brick", "minecraft:brick");
        materials.put("nether_brick", "minecraft:nether_brick");
        materials.put("glowstone_dust", "minecraft:glowstone_dust");
        materials.put("charcoal", "minecraft:charcoal");
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
        materials.put("obsidian_block", "minecraft:obsidian");
        materials.put("obsidian_bricks", "minecraft:obsidian");
        materials.put("chiseled_obsidian", "minecraft:obsidian");
        materials.put("crying_obsidian_bricks", "minecraft:crying_obsidian");
        materials.put("glowstone_block", "minecraft:glowstone");
        materials.put("hay_block", "minecraft:hay_block");
        materials.put("slime_block", "minecraft:slime_block");
        materials.put("warped", "minecraft:warped_planks");
        materials.put("warped_block", "minecraft:warped_planks");
        materials.put("dried_kelp_block", "minecraft:dried_kelp_block");

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

        System.out.println("Generated " + recipes.size() + " recipes");
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
