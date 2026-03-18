package com.political;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Central registry for all custom item texture pack codes.
 * Each custom item has a unique code that can be used in texture packs.
 */
public class TexturePackCodes {

    /**
     * Represents a custom item with its texture code information.
     */
    public record CustomItemEntry(
        String id,
        String displayName,
        String textureCode,
        String category,
        int color
    ) {}

    // ============================================================
    // TEXTURE CODE GENERATION
    // ============================================================
    
    /**
     * Gets the texture code for a custom item by its ID.
     * Format: category:item_name:color=HEX
     */
    public static String getTextureCode(String itemId) {
        if (itemId == null || itemId.isEmpty()) return null;
        
        // Gem Armor (Lapis & Emerald)
        if (itemId.startsWith("lapis_")) {
            return "gem_armor:" + itemId + ":color=" + String.format("%06X", GemArmor.LAPIS_COLOR);
        }
        if (itemId.startsWith("emerald_")) {
            return "gem_armor:" + itemId + ":color=" + String.format("%06X", GemArmor.EMERALD_COLOR);
        }
        
        // Block Armor
        if (itemId.startsWith("glass_") || itemId.startsWith("obsidian_") || 
            itemId.startsWith("quartz_") || itemId.startsWith("glowstone_") ||
            itemId.startsWith("redstone_") || itemId.startsWith("netherrack_") ||
            itemId.startsWith("endstone_") || itemId.startsWith("ice_") ||
            itemId.startsWith("prismarine_") || itemId.startsWith("terracotta_") ||
            itemId.startsWith("mossy_") || itemId.startsWith("soul_sand_") ||
            itemId.startsWith("magma_") || itemId.startsWith("sandstone_") ||
            itemId.startsWith("amethyst_") || itemId.startsWith("coal_")) {
            return BlockArmor.getTextureCode(itemId);
        }
        
        // Block Tools
        if (itemId.contains("_pickaxe") || itemId.contains("_axe") || 
            itemId.contains("_shovel") || itemId.contains("_hoe")) {
            if (itemId.startsWith("glass_") || itemId.startsWith("obsidian_") ||
                itemId.startsWith("quartz_") || itemId.startsWith("glowstone_") ||
                itemId.startsWith("redstone_") || itemId.startsWith("netherrack_") ||
                itemId.startsWith("endstone_") || itemId.startsWith("ice_") ||
                itemId.startsWith("prismarine_") || itemId.startsWith("terracotta_") ||
                itemId.startsWith("mossy_") || itemId.startsWith("soul_sand_") ||
                itemId.startsWith("magma_") || itemId.startsWith("sandstone_") ||
                itemId.startsWith("amethyst_") || itemId.startsWith("coal_")) {
                return "block_tools:" + itemId;
            }
        }
        
        // Slayer Items
        if (itemId.contains("_helmet_t") || itemId.contains("_chestplate_t") || 
            itemId.contains("_leggings_t") || itemId.contains("_boots_t")) {
            return "slayer_armor:" + itemId;
        }
        if (itemId.endsWith("_sword_t1") || itemId.endsWith("_sword_t2")) {
            return "slayer_weapon:" + itemId;
        }
        
        // Special Slayer Items
        return switch (itemId) {
            case "zombie_berserker_helmet" -> "special_armor:zombie_berserker_helmet:color=1A6600";
            case "spider_leggings" -> "special_armor:spider_leggings:color=4D0000";
            case "skeleton_bow" -> "special_weapon:skeleton_bow:color=F5F5F5";
            case "slime_boots" -> "special_armor:slime_boots:color=00FF00";
            case "warden_chestplate" -> "special_armor:warden_chestplate:color=004D4D";
            case "voidwalker_crown" -> "special_armor:voidwalker_crown:color=1A0A2E";
            case "ender_sword" -> "special_weapon:ender_sword:color=1A0A2E";
            case "abyssal_blade" -> "special_weapon:abyssal_blade:color=0A0A0A";
            case "venomous_dagger" -> "special_weapon:venomous_dagger:color=8B0000";
            case "bouncy_slime" -> "special_item:bouncy_slime:color=00FF00";
            case "crown_of_greed" -> "piglin:crown_of_greed:color=D4AF00";
            case "crown_of_midas" -> "piglin:crown_of_midas:color=FFD700";
            case "midas_sword" -> "piglin:midas_sword:color=FFD700";
            case "piglin_core" -> "piglin:piglin_core:color=D4AF00";
            case "piglin_flesh" -> "piglin:piglin_flesh:color=8B4513";
            
            // Gold Armor Evolution
            case "pure_gold_helmet", "pure_gold_chestplate", "pure_gold_leggings", "pure_gold_boots" ->
                "gold_armor:" + itemId + ":tier=1";
            case "polished_gold_helmet", "polished_gold_chestplate", "polished_gold_leggings", "polished_gold_boots" ->
                "gold_armor:" + itemId + ":tier=2";
            case "shiny_gold_helmet", "shiny_gold_chestplate", "shiny_gold_leggings", "shiny_gold_boots" ->
                "gold_armor:" + itemId + ":tier=3";
            case "glistening_gold_helmet", "glistening_gold_chestplate", "glistening_gold_leggings", "glistening_gold_boots" ->
                "gold_armor:" + itemId + ":tier=4";
            case "gilded_helmet", "gilded_chestplate", "gilded_leggings", "gilded_boots" ->
                "gold_armor:" + itemId + ":tier=5";
            case "gilded_netherite_helmet", "gilded_netherite_chestplate", "gilded_netherite_leggings", "gilded_netherite_boots" ->
                "gold_armor:" + itemId + ":tier=6";
            
            // Cores and Chunks
            case "zombie_core", "spider_core", "skeleton_core", "ender_core" ->
                "material:slayer_core:" + itemId.replace("_core", "");
            case "zombie_chunk", "spider_chunk", "skeleton_chunk", "ender_chunk" ->
                "material:slayer_chunk:" + itemId.replace("_chunk", "");
            
            // Compacted Materials
            case "compacted_iron" -> "compacted:iron:color=D8D8D8";
            case "compacted_gold" -> "compacted:gold:color=FFD700";
            case "compacted_diamond" -> "compacted:diamond:color=00FFFF";
            case "compacted_emerald" -> "compacted:emerald:color=00FF7F";
            case "compacted_netherite" -> "compacted:netherite:color=2F2F2F";
            case "compacted_copper" -> "compacted:copper:color=FF6347";
            case "compacted_coal" -> "compacted:coal:color=2F2F2F";
            case "compacted_lapis" -> "compacted:lapis:color=1E3A8A";
            case "compacted_redstone" -> "compacted:redstone:color=AA0F01";
            case "compacted_quartz" -> "compacted:quartz:color=F5F5F5";
            
            // Super Compacted
            case "super_compacted_iron" -> "super_compacted:iron";
            case "super_compacted_gold" -> "super_compacted:gold";
            case "super_compacted_diamond" -> "super_compacted:diamond";
            case "super_compacted_emerald" -> "super_compacted:emerald";
            case "super_compacted_netherite" -> "super_compacted:netherite";
            
            // Enchanted Blocks
            case "enchanted_cobblestone" -> "enchanted_block:cobblestone";
            case "enchanted_stone" -> "enchanted_block:stone";
            case "enchanted_iron_block" -> "enchanted_block:iron_block";
            case "enchanted_gold_block" -> "enchanted_block:gold_block";
            case "enchanted_diamond_block" -> "enchanted_block:diamond_block";
            case "enchanted_blackstone" -> "enchanted_block:blackstone";
            case "enchanted_gilded_blackstone" -> "enchanted_block:gilded_blackstone";
            case "enchanted_oak_log" -> "enchanted_block:oak_log";
            
            // Enchanted Crops
            case "enchanted_rose" -> "enchanted_crop:rose";
            case "enchanted_dandelion" -> "enchanted_crop:dandelion";
            case "enchanted_wheat" -> "enchanted_crop:wheat";
            case "enchanted_carrot" -> "enchanted_crop:carrot";
            case "enchanted_potato" -> "enchanted_crop:potato";
            case "enchanted_sugar_cane" -> "enchanted_crop:sugar_cane";
            
            // Attribute Tokens
            case "burning_token" -> "attribute_token:burning";
            case "sightless_token" -> "attribute_token:sightless";
            case "frenzied_token" -> "attribute_token:frenzied";
            case "grounded_token" -> "attribute_token:grounded";
            case "webbed_token" -> "attribute_token:webbed";
            case "frost_token" -> "attribute_token:frost";
            case "phantomstep_token" -> "attribute_token:phantomstep";
            case "cursed_token" -> "attribute_token:cursed";
            case "overgrown_token" -> "attribute_token:overgrown";
            case "volatile_token" -> "attribute_token:volatile";
            
            // Weapon Attribute Tokens
            case "weapon_burning_token" -> "weapon_attribute:burning";
            case "weapon_frenzied_token" -> "weapon_attribute:frenzied";
            case "weapon_cursed_token" -> "weapon_attribute:cursed";
            case "weapon_volatile_token" -> "weapon_attribute:volatile";
            case "weapon_lifesteal_token" -> "weapon_attribute:lifesteal";
            case "weapon_critical_token" -> "weapon_attribute:critical";
            
            // Special Items
            case "the_gavel" -> "special:the_gavel";
            case "harveys_stick" -> "special:harveys_stick";
            case "hermes_shoes" -> "special:hermes_shoes";
            case "undead_heart" -> "special:undead_heart";
            case "spectral_quiver" -> "special:spectral_quiver";
            case "echoing_core" -> "special:echoing_core";
            case "enchanted_blackstone_item" -> "special:enchanted_blackstone";
            case "enchanted_gilded_blackstone_item" -> "special:enchanted_gilded_blackstone";
            
            // HPEBM Weapons
            case "hpebm_mk1" -> "hpebm:mk1";
            case "hpebm_mk2" -> "hpebm:mk2";
            case "hpebm_mk3" -> "hpebm:mk3";
            case "hpebm_mk4" -> "hpebm:mk4";
            case "hpebm_mk5" -> "hpebm:mk5";
            case "ultra_overclocked_beam" -> "hpebm:ultra_overclocked";
            
            // Block Weapons
            case "glass_sword" -> "block_weapon:glass_sword";
            case "obsidian_sword" -> "block_weapon:obsidian_sword";
            case "quartz_sword" -> "block_weapon:quartz_sword";
            case "glowstone_sword" -> "block_weapon:glowstone_sword";
            case "redstone_sword" -> "block_weapon:redstone_sword";
            case "netherrack_sword" -> "block_weapon:netherrack_sword";
            case "endstone_sword" -> "block_weapon:endstone_sword";
            case "ice_sword" -> "block_weapon:ice_sword";
            case "prismarine_sword" -> "block_weapon:prismarine_sword";
            case "terracotta_sword" -> "block_weapon:terracotta_sword";
            case "mossy_sword" -> "block_weapon:mossy_sword";
            case "soul_sand_sword" -> "block_weapon:soul_sand_sword";
            case "magma_sword" -> "block_weapon:magma_sword";
            case "sandstone_sword" -> "block_weapon:sandstone_sword";
            case "amethyst_sword" -> "block_weapon:amethyst_sword";
            case "coal_sword" -> "block_weapon:coal_sword";
            
            // Custom Arrows
            case "explosive_arrow" -> "custom_arrow:explosive";
            case "tracking_arrow" -> "custom_arrow:tracking";
            case "teleport_arrow" -> "custom_arrow:teleport";
            case "multishot_arrow" -> "custom_arrow:multishot";
            case "poison_arrow" -> "custom_arrow:poison";
            case "fire_arrow" -> "custom_arrow:fire";
            case "ice_arrow" -> "custom_arrow:ice";
            case "lightning_arrow" -> "custom_arrow:lightning";
            case "healing_arrow" -> "custom_arrow:healing";
            case "void_arrow" -> "custom_arrow:void";
            case "spectral_arrow_custom" -> "custom_arrow:spectral";
            
            // Special Tools
            case "daniels_pickaxe" -> "special_tool:daniels_pickaxe";
            
            default -> "custom:" + itemId;
        };
    }
    
    /**
     * Gets all custom item entries organized by category.
     */
    public static Map<String, List<CustomItemEntry>> getAllItemsByCategory() {
        Map<String, List<CustomItemEntry>> categories = new LinkedHashMap<>();
        
        // Gem Armor
        List<CustomItemEntry> gemArmor = new ArrayList<>();
        gemArmor.add(new CustomItemEntry("lapis_helmet", "Lapis Lazuli Helmet", 
            getTextureCode("lapis_helmet"), "Gem Armor", GemArmor.LAPIS_COLOR));
        gemArmor.add(new CustomItemEntry("lapis_chestplate", "Lapis Lazuli Chestplate", 
            getTextureCode("lapis_chestplate"), "Gem Armor", GemArmor.LAPIS_COLOR));
        gemArmor.add(new CustomItemEntry("lapis_leggings", "Lapis Lazuli Leggings", 
            getTextureCode("lapis_leggings"), "Gem Armor", GemArmor.LAPIS_COLOR));
        gemArmor.add(new CustomItemEntry("lapis_boots", "Lapis Lazuli Boots", 
            getTextureCode("lapis_boots"), "Gem Armor", GemArmor.LAPIS_COLOR));
        gemArmor.add(new CustomItemEntry("emerald_helmet", "Emerald Helmet", 
            getTextureCode("emerald_helmet"), "Gem Armor", GemArmor.EMERALD_COLOR));
        gemArmor.add(new CustomItemEntry("emerald_chestplate", "Emerald Chestplate", 
            getTextureCode("emerald_chestplate"), "Gem Armor", GemArmor.EMERALD_COLOR));
        gemArmor.add(new CustomItemEntry("emerald_leggings", "Emerald Leggings", 
            getTextureCode("emerald_leggings"), "Gem Armor", GemArmor.EMERALD_COLOR));
        gemArmor.add(new CustomItemEntry("emerald_boots", "Emerald Boots", 
            getTextureCode("emerald_boots"), "Gem Armor", GemArmor.EMERALD_COLOR));
        categories.put("💎 Gem Armor (Lapis & Emerald)", gemArmor);
        
        // Block Armor
        List<CustomItemEntry> blockArmor = new ArrayList<>();
        String[] blockTypes = {"glass", "obsidian", "quartz", "glowstone", "redstone", 
            "netherrack", "endstone", "ice", "prismarine", "terracotta", "mossy", "soul_sand", "magma", "sandstone", "amethyst", "coal"};
        for (String type : blockTypes) {
            String[] pieces = {"helmet", "chestplate", "leggings", "boots"};
            for (String piece : pieces) {
                String id = type + "_" + piece;
                String displayName = type.substring(0, 1).toUpperCase() + type.substring(1).replace("_", " ") + " " + 
                    piece.substring(0, 1).toUpperCase() + piece.substring(1);
                blockArmor.add(new CustomItemEntry(id, displayName, getTextureCode(id), "Block Armor", 0));
            }
        }
        categories.put("🧱 Block Armor", blockArmor);
        
        // Slayer Armor
        List<CustomItemEntry> slayerArmor = new ArrayList<>();
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            String typeName = type.name().toLowerCase();
            for (int tier = 1; tier <= 2; tier++) {
                slayerArmor.add(new CustomItemEntry(typeName + "_helmet_t" + tier, 
                    type.displayName + " Helmet T" + tier, getTextureCode(typeName + "_helmet_t" + tier), "Slayer Armor", type.color.getColorValue()));
                slayerArmor.add(new CustomItemEntry(typeName + "_chestplate_t" + tier, 
                    type.displayName + " Chestplate T" + tier, getTextureCode(typeName + "_chestplate_t" + tier), "Slayer Armor", type.color.getColorValue()));
                slayerArmor.add(new CustomItemEntry(typeName + "_leggings_t" + tier, 
                    type.displayName + " Leggings T" + tier, getTextureCode(typeName + "_leggings_t" + tier), "Slayer Armor", type.color.getColorValue()));
                slayerArmor.add(new CustomItemEntry(typeName + "_boots_t" + tier, 
                    type.displayName + " Boots T" + tier, getTextureCode(typeName + "_boots_t" + tier), "Slayer Armor", type.color.getColorValue()));
            }
        }
        categories.put("⚔ Slayer Armor", slayerArmor);
        
        // Special Armor
        List<CustomItemEntry> specialArmor = new ArrayList<>();
        specialArmor.add(new CustomItemEntry("zombie_berserker_helmet", "Zombie Berserker Helmet", 
            getTextureCode("zombie_berserker_helmet"), "Special Armor", 0x1A6600));
        specialArmor.add(new CustomItemEntry("spider_leggings", "Spider Leggings", 
            getTextureCode("spider_leggings"), "Special Armor", 0x4D0000));
        specialArmor.add(new CustomItemEntry("slime_boots", "Slime Boots", 
            getTextureCode("slime_boots"), "Special Armor", 0x00FF00));
        specialArmor.add(new CustomItemEntry("warden_chestplate", "Warden Chestplate", 
            getTextureCode("warden_chestplate"), "Special Armor", 0x004D4D));
        specialArmor.add(new CustomItemEntry("voidwalker_crown", "Voidwalker Crown", 
            getTextureCode("voidwalker_crown"), "Special Armor", 0x1A0A2E));
        categories.put("🛡 Special Armor", specialArmor);
        
        // Materials
        List<CustomItemEntry> materials = new ArrayList<>();
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            String typeName = type.name().toLowerCase();
            materials.add(new CustomItemEntry(typeName + "_core", type.displayName + " Core", 
                getTextureCode(typeName + "_core"), "Materials", type.color.getColorValue()));
            materials.add(new CustomItemEntry(typeName + "_chunk", type.displayName + " Chunk", 
                getTextureCode(typeName + "_chunk"), "Materials", type.color.getColorValue()));
        }
        categories.put("📦 Materials (Cores & Chunks)", materials);
        
        // Compacted Items
        List<CustomItemEntry> compacted = new ArrayList<>();
        String[] compactedTypes = {"iron", "gold", "diamond", "emerald", "netherite", "copper", "coal", "lapis", "redstone", "quartz"};
        for (String type : compactedTypes) {
            compacted.add(new CustomItemEntry("compacted_" + type, "Compacted " + type.substring(0,1).toUpperCase() + type.substring(1), 
                getTextureCode("compacted_" + type), "Compacted Materials", 0));
        }
        categories.put("⬛ Compacted Materials", compacted);
        
        // Attribute Tokens
        List<CustomItemEntry> attributeTokens = new ArrayList<>();
        for (ArmourAttribute attr : ArmourAttribute.values()) {
            int colorValue = attr.color != null ? attr.color.getColorValue() : 0xFFFFFF;
            attributeTokens.add(new CustomItemEntry(attr.id + "_token", attr.displayName + " Token", 
                getTextureCode(attr.id + "_token"), "Attribute Tokens", colorValue));
        }
        categories.put("✧ Attribute Tokens", attributeTokens);
        
        // Weapon Attributes
        List<CustomItemEntry> weaponAttributes = new ArrayList<>();
        for (WeaponAttribute attr : WeaponAttribute.values()) {
            int colorValue = attr.color != null ? attr.color.getColorValue() : 0xFFFFFF;
            weaponAttributes.add(new CustomItemEntry("weapon_" + attr.id + "_token", attr.displayName + " Token", 
                getTextureCode("weapon_" + attr.id + "_token"), "Weapon Attributes", colorValue));
        }
        categories.put("⚔ Weapon Attributes", weaponAttributes);
        
        // Block Tools
        List<CustomItemEntry> blockTools = new ArrayList<>();
        String[] toolTypes = {"glass", "obsidian", "quartz", "glowstone", "redstone", 
            "netherrack", "endstone", "ice", "prismarine", "terracotta", "mossy", "soul_sand", "magma", "sandstone", "amethyst", "coal"};
        String[] toolPieces = {"pickaxe", "axe", "shovel", "hoe"};
        for (String type : toolTypes) {
            for (String piece : toolPieces) {
                String id = type + "_" + piece;
                String displayName = type.substring(0, 1).toUpperCase() + type.substring(1).replace("_", " ") + " " + 
                    piece.substring(0, 1).toUpperCase() + piece.substring(1);
                blockTools.add(new CustomItemEntry(id, displayName, getTextureCode(id), "Block Tools", 0));
            }
        }
        categories.put("⛏ Block Tools", blockTools);
        
        // Block Weapons
        List<CustomItemEntry> blockWeapons = new ArrayList<>();
        for (String type : toolTypes) {
            String id = type + "_sword";
            String displayName = type.substring(0, 1).toUpperCase() + type.substring(1).replace("_", " ") + " Sword";
            blockWeapons.add(new CustomItemEntry(id, displayName, "block_weapon:" + id, "Block Weapons", 0));
        }
        categories.put("⚔ Block Weapons", blockWeapons);
        
        // Super Compacted
        List<CustomItemEntry> superCompacted = new ArrayList<>();
        String[] superCompactedTypes = {"iron", "gold", "diamond", "emerald", "netherite"};
        for (String type : superCompactedTypes) {
            superCompacted.add(new CustomItemEntry("super_compacted_" + type, "Super Compacted " + type.substring(0,1).toUpperCase() + type.substring(1), 
                getTextureCode("super_compacted_" + type), "Super Compacted", 0));
        }
        categories.put("⭐ Super Compacted", superCompacted);
        
        // Enchanted Blocks
        List<CustomItemEntry> enchantedBlocks = new ArrayList<>();
        String[] enchantedTypes = {"cobblestone", "stone", "iron_block", "gold_block", "diamond_block", "blackstone", "oak_log"};
        for (String type : enchantedTypes) {
            enchantedBlocks.add(new CustomItemEntry("enchanted_" + type, "Enchanted " + type.substring(0,1).toUpperCase() + type.substring(1).replace("_", " "), 
                getTextureCode("enchanted_" + type), "Enchanted Blocks", 0));
        }
        categories.put("✧ Enchanted Blocks", enchantedBlocks);
        
        // Enchanted Crops
        List<CustomItemEntry> enchantedCrops = new ArrayList<>();
        String[] cropTypes = {"rose", "dandelion", "wheat", "carrot", "potato", "sugar_cane"};
        for (String type : cropTypes) {
            enchantedCrops.add(new CustomItemEntry("enchanted_" + type, "Enchanted " + type.substring(0,1).toUpperCase() + type.substring(1).replace("_", " "), 
                getTextureCode("enchanted_" + type), "Enchanted Crops", 0));
        }
        categories.put("✧ Enchanted Crops", enchantedCrops);
        
        // Custom Arrows
        List<CustomItemEntry> customArrows = new ArrayList<>();
        String[] arrowTypes = {"explosive", "tracking", "teleport", "multishot", "poison", "fire", "ice", "lightning", "healing", "void", "spectral"};
        for (String type : arrowTypes) {
            customArrows.add(new CustomItemEntry(type + "_arrow", type.substring(0,1).toUpperCase() + type.substring(1) + " Arrow", 
                getTextureCode(type + "_arrow"), "Custom Arrows", 0));
        }
        categories.put("➵ Custom Arrows", customArrows);
        
        // Special Tools
        List<CustomItemEntry> specialTools = new ArrayList<>();
        specialTools.add(new CustomItemEntry("daniels_pickaxe", "Daniel's Pickaxe", 
            getTextureCode("daniels_pickaxe"), "Special Tools", 0));
        categories.put("⛏ Special Tools", specialTools);
        
        return categories;
    }
    
    /**
     * Gets a flat list of all custom item entries.
     */
    public static List<CustomItemEntry> getAllItems() {
        List<CustomItemEntry> allItems = new ArrayList<>();
        for (List<CustomItemEntry> categoryItems : getAllItemsByCategory().values()) {
            allItems.addAll(categoryItems);
        }
        return allItems;
    }
}
