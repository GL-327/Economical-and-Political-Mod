package com.political;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Block-based armor sets created from dyed leather armor.
 * Each set has unique stats based on the block's properties.
 * Uses custom player heads for helmets when possible.
 */
public class BlockArmor {

    // ============================================================
    // ARMOR SET DEFINITIONS
    // ============================================================
    
    // Glass Armor - Fragile but absorbs impact well
    public static final int GLASS_DURABILITY = 50; // Very low
    public static final int GLASS_COLOR = 0xE8F4F8; // Light blue-white
    public static final String GLASS_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzU4ZDg3M2ExNTQwYjI0YTQ1MmFiMzRlYjBmZjY5NjI1M2IwZjA2YWUyMjBiMjYzNzUzYjE5MmI5YmVkMjUzNSJ9fX0="; // Glass block head
    
    // Obsidian Armor - Extremely durable and tough, but heavy
    public static final int OBSIDIAN_DURABILITY = 2000; // Very high
    public static final int OBSIDIAN_COLOR = 0x1A0A2E; // Dark purple-black
    public static final String OBSIDIAN_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDJhYjQ5NzU4YjVkMzg3ZDUwNzUzYzIzZjMwMjUyZTQxM2JhMzU2YzU0NmJjNGI3YmE5NzEyZTQ0MjJmM2MifX19"; // Obsidian head
    
    // Quartz Armor - Crystalline and elegant
    public static final int QUARTZ_DURABILITY = 300;
    public static final int QUARTZ_COLOR = 0xF5F5F5; // White
    public static final String QUARTZ_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjZjYmU4NjY4MTc2MjJhMmUwNDE5MzI1ZDQxZjQ4YjE1ZGNmNzY5ZWU1ZTk0MTU3YzQ0MzZlZjI1YTNkNzUifX19"; // Quartz head
    
    // Glowstone Armor - Light-emitting golden armor
    public static final int GLOWSTONE_DURABILITY = 200;
    public static final int GLOWSTONE_COLOR = 0xFFCC00; // Golden yellow
    public static final String GLOWSTONE_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjRkZTQ1YWUzZDQ2YjM0YmM0MTUzZjM1ZjIxMTUxYzlmN2E1YzUzYjQxNjJhYzE5MjFjYzU0YmE1YWY1MyJ9fX0="; // Glowstone head
    
    // Redstone Armor - Technical and conductive
    public static final int REDSTONE_DURABILITY = 250;
    public static final int REDSTONE_COLOR = 0xAA0F01; // Dark red
    public static final String REDSTONE_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTNlYjU0MjA2NzI1NjU0YjQ0OWQ1ODU0ZjBlMDE1YjU0YWQ1OTI1MmM2ZjQ5ZjQ0YzFhNjI2MzU0MzQ5YiJ9fX0="; // Redstone head
    
    // Netherrack Armor - Fire-resistant nether armor
    public static final int NETHERRACK_DURABILITY = 180;
    public static final int NETHERRACK_COLOR = 0x8B2323; // Dark red-brown
    public static final String NETHERRACK_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjM1NzYxZTVkYzU0ZjA1MWM5YzYzZTQwYTgzNTJkOGYyMzU0YmUzNWZkZjIyZmI1NzVkYjQyNzE0YmMxZCJ9fX0="; // Netherrack head
    
    // End Stone Armor - Void-touched armor
    public static final int ENDSTONE_DURABILITY = 350;
    public static final int ENDSTONE_COLOR = 0xD6D6D6; // Pale yellow-gray
    public static final String ENDSTONE_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmFkYjU0NzUyYzU3YzZlZDM3NzI0YTUzZmUwYzYzYzQxMzE4MTU0ZjQyMmViMzU2YzYxZGJhNzUyZTNlMSJ9fX0="; // End stone head
    
    // Packed Ice Armor - Cold and slippery
    public static final int ICE_DURABILITY = 120;
    public static final int ICE_COLOR = 0xB4D4E8; // Light blue
    public static final String ICE_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGU0YzYxZDUyMWUwM2U1ZjlmNzU0MzA5OTFjM2JjYzYxNzYzZDQ4ZjU0YzUyZTRkN2E1YmVkNWE4ZjM1MyJ9fX0="; // Packed ice head
    
    // Prismarine Armor - Ocean guardian armor
    public static final int PRISMARINE_DURABILITY = 280;
    public static final int PRISMARINE_COLOR = 0x5FA89E; // Teal
    public static final String PRISMARINE_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzY0NzJiNzQxMjE2MTYyMTYyM2UyZjU3YjI0ZjU0YzE5YjU1MjU1ZmUyYjU2YzU0YjU1MjU1YjU1YjU1YiJ9fX0="; // Prismarine head
    
    // Terracotta Armor - Hardened clay armor
    public static final int TERRACOTTA_DURABILITY = 400;
    public static final int TERRACOTTA_COLOR = 0xC84C26; // Orange-brown
    public static final String TERRACOTTA_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjI0YmI4ZjU4YjU0YjU0YjU0YjU0YjU0YjU0YjU0YjU0YjU0YjU0YjU0YjU0YjU0YjU0YjU0YiJ9fX0="; // Terracotta head
    
    // Mossy Stone Armor - Ancient overgrown armor
    public static final int MOSSY_DURABILITY = 450;
    public static final int MOSSY_COLOR = 0x4A7C38; // Moss green
    public static final String MOSSY_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2UzMDVlMWRkNjhlZmY4MmEwYTk3MzZmNmJlM2E4ZTEyOTgzZDA3MGE4ZjRmZjdmNGZiZjQ2NDZlYzYyZGYifX19"; // Mossy head
    
    // Soul Sand Armor - Soul-infused armor
    public static final int SOUL_SAND_DURABILITY = 220;
    public static final int SOUL_SAND_COLOR = 0x4D3A2A; // Soul sand brown
    public static final String SOUL_SAND_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2MwZDY4ZjA0NzkxYWNlZGM3M2Y3YmMyZDA1N2Q0MjI1Y2ViMTY1YzQ3YjNiNzI3MTRhMjYyYzgzYWMifX19"; // Soul sand head
    
    // Magma Armor - Burning hot armor
    public static final int MAGMA_DURABILITY = 350;
    public static final int MAGMA_COLOR = 0x8B0000; // Dark red
    public static final String MAGMA_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzk3YjkzYzYyYjk4NGI2OTI2N2Y4ZGVjN2RkN2Y0YTI2MjE4NzNjZDYyNmE0Zjg0YzJjYWY4YzkifX19"; // Magma head
    
    // Sandstone Armor - Desert protector armor
    public static final int SANDSTONE_DURABILITY = 280;
    public static final int SANDSTONE_COLOR = 0xD2B48C; // Tan
    public static final String SANDSTONE_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzIyZDE0MjhlMmUyZmI4YzY4MmEzYWMzMmE0MmJkY2JkZDU0ZWZkZWFhMmIyY2ZkYmI0NGE1NWQxMjEifX19"; // Sandstone head
    
    // Amethyst Armor - Crystal armor with magic properties
    public static final int AMETHYST_DURABILITY = 320;
    public static final int AMETHYST_COLOR = 0x9966CC; // Purple
    public static final String AMETHYST_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2IzYzNjY2VjM2E0MjEyNWE4Yzk2YWVkZjUzZDA1ZWE0N2U0YTZkNTU4OTU4OTczZTQyZGFjZTJiYzEyMyJ9fX0="; // Amethyst head
    
    // Coal Armor - Dark carbon armor
    public static final int COAL_DURABILITY = 200;
    public static final int COAL_COLOR = 0x1C1C1C; // Dark gray-black
    public static final String COAL_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2FiOTMwZDNjMmZlMjQ4YTMxMTk3MzFhM2Y0MmVlZjRmZmJiYjI3ZmZjOTZjZGRhZmQ0MjYyYzE1MDVmNyJ9fX0="; // Coal block head

    // ============================================================
    // TEXTURE PACK CODES
    // ============================================================
    
    public static String getTextureCode(String armorId) {
        return switch (armorId) {
            case "glass_helmet", "glass_chestplate", "glass_leggings", "glass_boots" ->
                "glass_armor:" + armorId.replace("glass_", "") + ":color=" + GLASS_COLOR;
            case "obsidian_helmet", "obsidian_chestplate", "obsidian_leggings", "obsidian_boots" ->
                "obsidian_armor:" + armorId.replace("obsidian_", "") + ":color=" + OBSIDIAN_COLOR;
            case "quartz_helmet", "quartz_chestplate", "quartz_leggings", "quartz_boots" ->
                "quartz_armor:" + armorId.replace("quartz_", "") + ":color=" + QUARTZ_COLOR;
            case "glowstone_helmet", "glowstone_chestplate", "glowstone_leggings", "glowstone_boots" ->
                "glowstone_armor:" + armorId.replace("glowstone_", "") + ":color=" + GLOWSTONE_COLOR;
            case "redstone_helmet", "redstone_chestplate", "redstone_leggings", "redstone_boots" ->
                "redstone_armor:" + armorId.replace("redstone_", "") + ":color=" + REDSTONE_COLOR;
            case "netherrack_helmet", "netherrack_chestplate", "netherrack_leggings", "netherrack_boots" ->
                "netherrack_armor:" + armorId.replace("netherrack_", "") + ":color=" + NETHERRACK_COLOR;
            case "endstone_helmet", "endstone_chestplate", "endstone_leggings", "endstone_boots" ->
                "endstone_armor:" + armorId.replace("endstone_", "") + ":color=" + ENDSTONE_COLOR;
            case "ice_helmet", "ice_chestplate", "ice_leggings", "ice_boots" ->
                "ice_armor:" + armorId.replace("ice_", "") + ":color=" + ICE_COLOR;
            case "prismarine_helmet", "prismarine_chestplate", "prismarine_leggings", "prismarine_boots" ->
                "prismarine_armor:" + armorId.replace("prismarine_", "") + ":color=" + PRISMARINE_COLOR;
            case "terracotta_helmet", "terracotta_chestplate", "terracotta_leggings", "terracotta_boots" ->
                "terracotta_armor:" + armorId.replace("terracotta_", "") + ":color=" + TERRACOTTA_COLOR;
            case "mossy_helmet", "mossy_chestplate", "mossy_leggings", "mossy_boots" ->
                "mossy_armor:" + armorId.replace("mossy_", "") + ":color=" + MOSSY_COLOR;
            case "soul_sand_helmet", "soul_sand_chestplate", "soul_sand_leggings", "soul_sand_boots" ->
                "soul_sand_armor:" + armorId.replace("soul_sand_", "") + ":color=" + SOUL_SAND_COLOR;
            case "magma_helmet", "magma_chestplate", "magma_leggings", "magma_boots" ->
                "magma_armor:" + armorId.replace("magma_", "") + ":color=" + MAGMA_COLOR;
            case "sandstone_helmet", "sandstone_chestplate", "sandstone_leggings", "sandstone_boots" ->
                "sandstone_armor:" + armorId.replace("sandstone_", "") + ":color=" + SANDSTONE_COLOR;
            case "amethyst_helmet", "amethyst_chestplate", "amethyst_leggings", "amethyst_boots" ->
                "amethyst_armor:" + armorId.replace("amethyst_", "") + ":color=" + AMETHYST_COLOR;
            case "coal_helmet", "coal_chestplate", "coal_leggings", "coal_boots" ->
                "coal_armor:" + armorId.replace("coal_", "") + ":color=" + COAL_COLOR;
            default -> "unknown_armor:" + armorId;
        };
    }

    // ============================================================
    // GLASS ARMOR - Fragile but high toughness
    // ============================================================
    
    public static ItemStack createGlassHelmet() {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        applyPlayerHeadTexture(stack, GLASS_HEAD);
        applyGlassStats(stack, "Helmet", 1, 4, EquipmentSlot.HEAD);
        setCustomItemId(stack, "glass_helmet");
        return stack;
    }
    
    public static ItemStack createGlassChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyGlassStats(stack, "Chestplate", 3, 5, EquipmentSlot.CHEST);
        setCustomItemId(stack, "glass_chestplate");
        return stack;
    }
    
    public static ItemStack createGlassLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyGlassStats(stack, "Leggings", 2, 4, EquipmentSlot.LEGS);
        setCustomItemId(stack, "glass_leggings");
        return stack;
    }
    
    public static ItemStack createGlassBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyGlassStats(stack, "Boots", 1, 3, EquipmentSlot.FEET);
        setCustomItemId(stack, "glass_boots");
        return stack;
    }
    
    private static void applyGlassStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        if (!stack.isOf(Items.PLAYER_HEAD)) {
            stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(GLASS_COLOR));
        }
        
        stack.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Glass " + pieceName).formatted(Formatting.AQUA, Formatting.BOLD));
        stack.set(DataComponentTypes.MAX_DAMAGE, GLASS_DURABILITY);
        stack.set(DataComponentTypes.DAMAGE, 0);
        
        AttributeModifierSlot modifierSlot = getSlotForPiece(pieceName);
        AttributeModifiersComponent.Builder attrBuilder = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "glass_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(EntityAttributes.ARMOR_TOUGHNESS,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "glass_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.AQUA));
        lore.add(Text.literal("🛡 Armor: ").formatted(Formatting.WHITE)
                .append(Text.literal("+" + armorValue).formatted(Formatting.AQUA)));
        lore.add(Text.literal("💎 Toughness: ").formatted(Formatting.WHITE)
                .append(Text.literal("+" + toughnessValue).formatted(Formatting.AQUA)));
        lore.add(Text.literal("⚠ Durability: ").formatted(Formatting.WHITE)
                .append(Text.literal("VERY LOW").formatted(Formatting.RED)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ SET BONUS ━━━").formatted(Formatting.DARK_PURPLE));
        lore.add(Text.literal("✦ Full set: Speed II").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Lightweight and fragile").formatted(Formatting.DARK_GRAY));
        
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // OBSIDIAN ARMOR - Extremely durable, very heavy
    // ============================================================
    
    public static ItemStack createObsidianHelmet() {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        applyPlayerHeadTexture(stack, OBSIDIAN_HEAD);
        applyObsidianStats(stack, "Helmet", 3, 6, EquipmentSlot.HEAD);
        setCustomItemId(stack, "obsidian_helmet");
        return stack;
    }
    
    public static ItemStack createObsidianChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyObsidianStats(stack, "Chestplate", 8, 8, EquipmentSlot.CHEST);
        setCustomItemId(stack, "obsidian_chestplate");
        return stack;
    }
    
    public static ItemStack createObsidianLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyObsidianStats(stack, "Leggings", 6, 7, EquipmentSlot.LEGS);
        setCustomItemId(stack, "obsidian_leggings");
        return stack;
    }
    
    public static ItemStack createObsidianBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyObsidianStats(stack, "Boots", 3, 5, EquipmentSlot.FEET);
        setCustomItemId(stack, "obsidian_boots");
        return stack;
    }
    
    private static void applyObsidianStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        if (!stack.isOf(Items.PLAYER_HEAD)) {
            stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(OBSIDIAN_COLOR));
        }
        
        stack.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Obsidian " + pieceName).formatted(Formatting.DARK_PURPLE, Formatting.BOLD));
        stack.set(DataComponentTypes.MAX_DAMAGE, OBSIDIAN_DURABILITY);
        stack.set(DataComponentTypes.DAMAGE, 0);
        
        AttributeModifierSlot modifierSlot = getSlotForPiece(pieceName);
        AttributeModifiersComponent.Builder attrBuilder = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "obsidian_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(EntityAttributes.ARMOR_TOUGHNESS,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "obsidian_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(EntityAttributes.MOVEMENT_SPEED,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "obsidian_" + pieceName.toLowerCase() + "_speed"),
                                -0.05,
                                EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                        ),
                        modifierSlot);
        
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_PURPLE));
        lore.add(Text.literal("🛡 Armor: ").formatted(Formatting.WHITE)
                .append(Text.literal("+" + armorValue).formatted(Formatting.DARK_PURPLE)));
        lore.add(Text.literal("💎 Toughness: ").formatted(Formatting.WHITE)
                .append(Text.literal("+" + toughnessValue).formatted(Formatting.DARK_PURPLE)));
        lore.add(Text.literal("🏃 Speed: ").formatted(Formatting.WHITE)
                .append(Text.literal("-5% per piece").formatted(Formatting.RED)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ SET BONUS ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("✦ Full set: Blast resistance & Fire immunity").formatted(Formatting.YELLOW));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Forged in volcanic fury").formatted(Formatting.DARK_GRAY));
        
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // QUARTZ ARMOR - Crystalline elegance
    // ============================================================
    
    public static ItemStack createQuartzHelmet() {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        applyPlayerHeadTexture(stack, QUARTZ_HEAD);
        applyQuartzStats(stack, "Helmet", 2, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "quartz_helmet");
        return stack;
    }
    
    public static ItemStack createQuartzChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyQuartzStats(stack, "Chestplate", 5, 3, EquipmentSlot.CHEST);
        setCustomItemId(stack, "quartz_chestplate");
        return stack;
    }
    
    public static ItemStack createQuartzLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyQuartzStats(stack, "Leggings", 4, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "quartz_leggings");
        return stack;
    }
    
    public static ItemStack createQuartzBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyQuartzStats(stack, "Boots", 2, 2, EquipmentSlot.FEET);
        setCustomItemId(stack, "quartz_boots");
        return stack;
    }
    
    private static void applyQuartzStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        if (!stack.isOf(Items.PLAYER_HEAD)) {
            stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(QUARTZ_COLOR));
        }
        
        stack.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Quartz " + pieceName).formatted(Formatting.WHITE, Formatting.BOLD));
        stack.set(DataComponentTypes.MAX_DAMAGE, QUARTZ_DURABILITY);
        stack.set(DataComponentTypes.DAMAGE, 0);
        
        AttributeModifierSlot modifierSlot = getSlotForPiece(pieceName);
        AttributeModifiersComponent.Builder attrBuilder = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "quartz_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(EntityAttributes.ARMOR_TOUGHNESS,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "quartz_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.WHITE));
        lore.add(Text.literal("🛡 Armor: ").formatted(Formatting.WHITE)
                .append(Text.literal("+" + armorValue).formatted(Formatting.WHITE)));
        lore.add(Text.literal("💎 Toughness: ").formatted(Formatting.WHITE)
                .append(Text.literal("+" + toughnessValue).formatted(Formatting.WHITE)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ SET BONUS ━━━").formatted(Formatting.DARK_PURPLE));
        lore.add(Text.literal("✦ Full set: Haste I (faster attack & mining)").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Crystalline perfection").formatted(Formatting.DARK_GRAY));
        
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // GLOWSTONE ARMOR - Light-emitting
    // ============================================================
    
    public static ItemStack createGlowstoneHelmet() {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        applyPlayerHeadTexture(stack, GLOWSTONE_HEAD);
        applyGlowstoneStats(stack, "Helmet", 2, 1, EquipmentSlot.HEAD);
        setCustomItemId(stack, "glowstone_helmet");
        return stack;
    }
    
    public static ItemStack createGlowstoneChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyGlowstoneStats(stack, "Chestplate", 4, 2, EquipmentSlot.CHEST);
        setCustomItemId(stack, "glowstone_chestplate");
        return stack;
    }
    
    public static ItemStack createGlowstoneLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyGlowstoneStats(stack, "Leggings", 3, 1, EquipmentSlot.LEGS);
        setCustomItemId(stack, "glowstone_leggings");
        return stack;
    }
    
    public static ItemStack createGlowstoneBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyGlowstoneStats(stack, "Boots", 1, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "glowstone_boots");
        return stack;
    }
    
    private static void applyGlowstoneStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        if (!stack.isOf(Items.PLAYER_HEAD)) {
            stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(GLOWSTONE_COLOR));
        }
        
        stack.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Glowstone " + pieceName).formatted(Formatting.YELLOW, Formatting.BOLD));
        stack.set(DataComponentTypes.MAX_DAMAGE, GLOWSTONE_DURABILITY);
        stack.set(DataComponentTypes.DAMAGE, 0);
        
        AttributeModifierSlot modifierSlot = getSlotForPiece(pieceName);
        AttributeModifiersComponent.Builder attrBuilder = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "glowstone_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(EntityAttributes.ARMOR_TOUGHNESS,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "glowstone_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("🛡 Armor: ").formatted(Formatting.WHITE)
                .append(Text.literal("+" + armorValue).formatted(Formatting.YELLOW)));
        lore.add(Text.literal("💎 Toughness: ").formatted(Formatting.WHITE)
                .append(Text.literal("+" + toughnessValue).formatted(Formatting.YELLOW)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ SET BONUS ━━━").formatted(Formatting.DARK_PURPLE));
        lore.add(Text.literal("✦ Emits light around you").formatted(Formatting.YELLOW));
        lore.add(Text.literal("✦ Full set: Night Vision & Glowing").formatted(Formatting.GOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Illuminated by the Nether").formatted(Formatting.DARK_GRAY));
        
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // REDSTONE ARMOR - Technical and conductive
    // ============================================================
    
    public static ItemStack createRedstoneHelmet() {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        applyPlayerHeadTexture(stack, REDSTONE_HEAD);
        applyRedstoneStats(stack, "Helmet", 2, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "redstone_helmet");
        return stack;
    }
    
    public static ItemStack createRedstoneChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyRedstoneStats(stack, "Chestplate", 5, 3, EquipmentSlot.CHEST);
        setCustomItemId(stack, "redstone_chestplate");
        return stack;
    }
    
    public static ItemStack createRedstoneLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyRedstoneStats(stack, "Leggings", 4, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "redstone_leggings");
        return stack;
    }
    
    public static ItemStack createRedstoneBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyRedstoneStats(stack, "Boots", 2, 2, EquipmentSlot.FEET);
        setCustomItemId(stack, "redstone_boots");
        return stack;
    }
    
    private static void applyRedstoneStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        if (!stack.isOf(Items.PLAYER_HEAD)) {
            stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(REDSTONE_COLOR));
        }
        
        stack.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Redstone " + pieceName).formatted(Formatting.DARK_RED, Formatting.BOLD));
        stack.set(DataComponentTypes.MAX_DAMAGE, REDSTONE_DURABILITY);
        stack.set(DataComponentTypes.DAMAGE, 0);
        
        AttributeModifierSlot modifierSlot = getSlotForPiece(pieceName);
        AttributeModifiersComponent.Builder attrBuilder = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "redstone_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(EntityAttributes.ARMOR_TOUGHNESS,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "redstone_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_RED));
        lore.add(Text.literal("🛡 Armor: ").formatted(Formatting.WHITE)
                .append(Text.literal("+" + armorValue).formatted(Formatting.DARK_RED)));
        lore.add(Text.literal("💎 Toughness: ").formatted(Formatting.WHITE)
                .append(Text.literal("+" + toughnessValue).formatted(Formatting.DARK_RED)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ SET BONUS ━━━").formatted(Formatting.DARK_PURPLE));
        lore.add(Text.literal("✦ Full set: Haste II (fast mining)").formatted(Formatting.RED));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Powered by redstone dust").formatted(Formatting.DARK_GRAY));
        
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // NETHERRACK ARMOR - Fire-resistant
    // ============================================================
    
    public static ItemStack createNetherrackHelmet() {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        applyPlayerHeadTexture(stack, NETHERRACK_HEAD);
        applyNetherrackStats(stack, "Helmet", 2, 1, EquipmentSlot.HEAD);
        setCustomItemId(stack, "netherrack_helmet");
        return stack;
    }
    
    public static ItemStack createNetherrackChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyNetherrackStats(stack, "Chestplate", 4, 2, EquipmentSlot.CHEST);
        setCustomItemId(stack, "netherrack_chestplate");
        return stack;
    }
    
    public static ItemStack createNetherrackLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyNetherrackStats(stack, "Leggings", 3, 1, EquipmentSlot.LEGS);
        setCustomItemId(stack, "netherrack_leggings");
        return stack;
    }
    
    public static ItemStack createNetherrackBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyNetherrackStats(stack, "Boots", 1, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "netherrack_boots");
        return stack;
    }
    
    private static void applyNetherrackStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        if (!stack.isOf(Items.PLAYER_HEAD)) {
            stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(NETHERRACK_COLOR));
        }
        
        stack.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Netherrack " + pieceName).formatted(Formatting.RED, Formatting.BOLD));
        stack.set(DataComponentTypes.MAX_DAMAGE, NETHERRACK_DURABILITY);
        stack.set(DataComponentTypes.DAMAGE, 0);
        
        AttributeModifierSlot modifierSlot = getSlotForPiece(pieceName);
        AttributeModifiersComponent.Builder attrBuilder = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "netherrack_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(EntityAttributes.ARMOR_TOUGHNESS,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "netherrack_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.RED));
        lore.add(Text.literal("🛡 Armor: ").formatted(Formatting.WHITE)
                .append(Text.literal("+" + armorValue).formatted(Formatting.RED)));
        lore.add(Text.literal("💎 Toughness: ").formatted(Formatting.WHITE)
                .append(Text.literal("+" + toughnessValue).formatted(Formatting.RED)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ SET BONUS ━━━").formatted(Formatting.DARK_PURPLE));
        lore.add(Text.literal("✦ Full set: Fire Resistance").formatted(Formatting.GOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Forged in the Nether's heat").formatted(Formatting.DARK_GRAY));
        
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // END STONE ARMOR - Void-touched
    // ============================================================
    
    public static ItemStack createEndstoneHelmet() {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        applyPlayerHeadTexture(stack, ENDSTONE_HEAD);
        applyEndstoneStats(stack, "Helmet", 3, 3, EquipmentSlot.HEAD);
        setCustomItemId(stack, "endstone_helmet");
        return stack;
    }
    
    public static ItemStack createEndstoneChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyEndstoneStats(stack, "Chestplate", 6, 4, EquipmentSlot.CHEST);
        setCustomItemId(stack, "endstone_chestplate");
        return stack;
    }
    
    public static ItemStack createEndstoneLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyEndstoneStats(stack, "Leggings", 5, 3, EquipmentSlot.LEGS);
        setCustomItemId(stack, "endstone_leggings");
        return stack;
    }
    
    public static ItemStack createEndstoneBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyEndstoneStats(stack, "Boots", 2, 3, EquipmentSlot.FEET);
        setCustomItemId(stack, "endstone_boots");
        return stack;
    }
    
    private static void applyEndstoneStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        if (!stack.isOf(Items.PLAYER_HEAD)) {
            stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(ENDSTONE_COLOR));
        }
        
        stack.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("End Stone " + pieceName).formatted(Formatting.YELLOW, Formatting.BOLD));
        stack.set(DataComponentTypes.MAX_DAMAGE, ENDSTONE_DURABILITY);
        stack.set(DataComponentTypes.DAMAGE, 0);
        
        AttributeModifierSlot modifierSlot = getSlotForPiece(pieceName);
        AttributeModifiersComponent.Builder attrBuilder = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "endstone_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(EntityAttributes.ARMOR_TOUGHNESS,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "endstone_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.YELLOW));
        lore.add(Text.literal("🛡 Armor: ").formatted(Formatting.WHITE)
                .append(Text.literal("+" + armorValue).formatted(Formatting.YELLOW)));
        lore.add(Text.literal("💎 Toughness: ").formatted(Formatting.WHITE)
                .append(Text.literal("+" + toughnessValue).formatted(Formatting.YELLOW)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ SET BONUS ━━━").formatted(Formatting.DARK_PURPLE));
        lore.add(Text.literal("✦ Full set: Slow Falling & Void resistance").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Touched by the Void").formatted(Formatting.DARK_GRAY));
        
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // ICE ARMOR - Cold and slippery
    // ============================================================
    
    public static ItemStack createIceHelmet() {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        applyPlayerHeadTexture(stack, ICE_HEAD);
        applyIceStats(stack, "Helmet", 2, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "ice_helmet");
        return stack;
    }
    
    public static ItemStack createIceChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyIceStats(stack, "Chestplate", 4, 3, EquipmentSlot.CHEST);
        setCustomItemId(stack, "ice_chestplate");
        return stack;
    }
    
    public static ItemStack createIceLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyIceStats(stack, "Leggings", 3, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "ice_leggings");
        return stack;
    }
    
    public static ItemStack createIceBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyIceStats(stack, "Boots", 2, 2, EquipmentSlot.FEET);
        setCustomItemId(stack, "ice_boots");
        return stack;
    }
    
    private static void applyIceStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        if (!stack.isOf(Items.PLAYER_HEAD)) {
            stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(ICE_COLOR));
        }
        
        stack.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Packed Ice " + pieceName).formatted(Formatting.AQUA, Formatting.BOLD));
        stack.set(DataComponentTypes.MAX_DAMAGE, ICE_DURABILITY);
        stack.set(DataComponentTypes.DAMAGE, 0);
        
        AttributeModifierSlot modifierSlot = getSlotForPiece(pieceName);
        AttributeModifiersComponent.Builder attrBuilder = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "ice_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(EntityAttributes.ARMOR_TOUGHNESS,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "ice_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.AQUA));
        lore.add(Text.literal("🛡 Armor: ").formatted(Formatting.WHITE)
                .append(Text.literal("+" + armorValue).formatted(Formatting.AQUA)));
        lore.add(Text.literal("💎 Toughness: ").formatted(Formatting.WHITE)
                .append(Text.literal("+" + toughnessValue).formatted(Formatting.AQUA)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ SET BONUS ━━━").formatted(Formatting.DARK_PURPLE));
        lore.add(Text.literal("✦ Full set: Speed on ice & Freeze immunity").formatted(Formatting.BLUE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Frozen in eternal cold").formatted(Formatting.DARK_GRAY));
        
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // PRISMARINE ARMOR - Ocean guardian
    // ============================================================
    
    public static ItemStack createPrismarineHelmet() {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        applyPlayerHeadTexture(stack, PRISMARINE_HEAD);
        applyPrismarineStats(stack, "Helmet", 2, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "prismarine_helmet");
        return stack;
    }
    
    public static ItemStack createPrismarineChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyPrismarineStats(stack, "Chestplate", 5, 3, EquipmentSlot.CHEST);
        setCustomItemId(stack, "prismarine_chestplate");
        return stack;
    }
    
    public static ItemStack createPrismarineLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyPrismarineStats(stack, "Leggings", 4, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "prismarine_leggings");
        return stack;
    }
    
    public static ItemStack createPrismarineBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyPrismarineStats(stack, "Boots", 2, 2, EquipmentSlot.FEET);
        setCustomItemId(stack, "prismarine_boots");
        return stack;
    }
    
    private static void applyPrismarineStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        if (!stack.isOf(Items.PLAYER_HEAD)) {
            stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(PRISMARINE_COLOR));
        }
        
        stack.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Prismarine " + pieceName).formatted(Formatting.DARK_AQUA, Formatting.BOLD));
        stack.set(DataComponentTypes.MAX_DAMAGE, PRISMARINE_DURABILITY);
        stack.set(DataComponentTypes.DAMAGE, 0);
        
        AttributeModifierSlot modifierSlot = getSlotForPiece(pieceName);
        AttributeModifiersComponent.Builder attrBuilder = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "prismarine_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(EntityAttributes.ARMOR_TOUGHNESS,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "prismarine_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_AQUA));
        lore.add(Text.literal("🛡 Armor: ").formatted(Formatting.WHITE)
                .append(Text.literal("+" + armorValue).formatted(Formatting.DARK_AQUA)));
        lore.add(Text.literal("💎 Toughness: ").formatted(Formatting.WHITE)
                .append(Text.literal("+" + toughnessValue).formatted(Formatting.DARK_AQUA)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ SET BONUS ━━━").formatted(Formatting.DARK_PURPLE));
        lore.add(Text.literal("✦ Full set: Water Breathing & Dolphins Grace").formatted(Formatting.BLUE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Gift of the Ocean Monument").formatted(Formatting.DARK_GRAY));
        
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // TERRACOTTA ARMOR - Hardened clay
    // ============================================================
    
    public static ItemStack createTerracottaHelmet() {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        applyPlayerHeadTexture(stack, TERRACOTTA_HEAD);
        applyTerracottaStats(stack, "Helmet", 3, 3, EquipmentSlot.HEAD);
        setCustomItemId(stack, "terracotta_helmet");
        return stack;
    }
    
    public static ItemStack createTerracottaChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyTerracottaStats(stack, "Chestplate", 6, 4, EquipmentSlot.CHEST);
        setCustomItemId(stack, "terracotta_chestplate");
        return stack;
    }
    
    public static ItemStack createTerracottaLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyTerracottaStats(stack, "Leggings", 5, 3, EquipmentSlot.LEGS);
        setCustomItemId(stack, "terracotta_leggings");
        return stack;
    }
    
    public static ItemStack createTerracottaBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyTerracottaStats(stack, "Boots", 2, 3, EquipmentSlot.FEET);
        setCustomItemId(stack, "terracotta_boots");
        return stack;
    }
    
    private static void applyTerracottaStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        if (!stack.isOf(Items.PLAYER_HEAD)) {
            stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(TERRACOTTA_COLOR));
        }
        
        stack.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Terracotta " + pieceName).formatted(Formatting.GOLD, Formatting.BOLD));
        stack.set(DataComponentTypes.MAX_DAMAGE, TERRACOTTA_DURABILITY);
        stack.set(DataComponentTypes.DAMAGE, 0);
        
        AttributeModifierSlot modifierSlot = getSlotForPiece(pieceName);
        AttributeModifiersComponent.Builder attrBuilder = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "terracotta_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(EntityAttributes.ARMOR_TOUGHNESS,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "terracotta_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("🛡 Armor: ").formatted(Formatting.WHITE)
                .append(Text.literal("+" + armorValue).formatted(Formatting.GOLD)));
        lore.add(Text.literal("💎 Toughness: ").formatted(Formatting.WHITE)
                .append(Text.literal("+" + toughnessValue).formatted(Formatting.GOLD)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ SET BONUS ━━━").formatted(Formatting.DARK_PURPLE));
        lore.add(Text.literal("✦ Full set: Resistance I").formatted(Formatting.YELLOW));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Hardened by fire and time").formatted(Formatting.DARK_GRAY));
        
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // MOSSY STONE ARMOR - Ancient overgrown armor
    // ============================================================
    
    public static ItemStack createMossyHelmet() {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        applyPlayerHeadTexture(stack, MOSSY_HEAD);
        applyMossyStats(stack, "Helmet", 3, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "mossy_helmet");
        return stack;
    }
    
    public static ItemStack createMossyChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyMossyStats(stack, "Chestplate", 6, 3, EquipmentSlot.CHEST);
        setCustomItemId(stack, "mossy_chestplate");
        return stack;
    }
    
    public static ItemStack createMossyLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyMossyStats(stack, "Leggings", 5, 3, EquipmentSlot.LEGS);
        setCustomItemId(stack, "mossy_leggings");
        return stack;
    }
    
    public static ItemStack createMossyBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyMossyStats(stack, "Boots", 2, 2, EquipmentSlot.FEET);
        setCustomItemId(stack, "mossy_boots");
        return stack;
    }
    
    private static void applyMossyStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        if (!stack.isOf(Items.PLAYER_HEAD)) {
            stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(MOSSY_COLOR));
        }
        
        stack.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Mossy " + pieceName).formatted(Formatting.DARK_GREEN, Formatting.BOLD));
        stack.set(DataComponentTypes.MAX_DAMAGE, MOSSY_DURABILITY);
        stack.set(DataComponentTypes.DAMAGE, 0);
        
        AttributeModifierSlot modifierSlot = getSlotForPiece(pieceName);
        AttributeModifiersComponent.Builder attrBuilder = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "mossy_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(EntityAttributes.ARMOR_TOUGHNESS,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "mossy_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_GREEN));
        lore.add(Text.literal("🛡 Armor: ").formatted(Formatting.WHITE)
                .append(Text.literal("+" + armorValue).formatted(Formatting.GREEN)));
        lore.add(Text.literal("💎 Toughness: ").formatted(Formatting.WHITE)
                .append(Text.literal("+" + toughnessValue).formatted(Formatting.GREEN)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ SET BONUS ━━━").formatted(Formatting.DARK_PURPLE));
        lore.add(Text.literal("✦ Full set: Regeneration I").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Ancient stone overgrown with moss").formatted(Formatting.DARK_GRAY));
        
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // SOUL SAND ARMOR - Soul-infused armor
    // ============================================================
    
    public static ItemStack createSoulSandHelmet() {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        applyPlayerHeadTexture(stack, SOUL_SAND_HEAD);
        applySoulSandStats(stack, "Helmet", 2, 1, EquipmentSlot.HEAD);
        setCustomItemId(stack, "soul_sand_helmet");
        return stack;
    }
    
    public static ItemStack createSoulSandChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applySoulSandStats(stack, "Chestplate", 4, 2, EquipmentSlot.CHEST);
        setCustomItemId(stack, "soul_sand_chestplate");
        return stack;
    }
    
    public static ItemStack createSoulSandLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applySoulSandStats(stack, "Leggings", 3, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "soul_sand_leggings");
        return stack;
    }
    
    public static ItemStack createSoulSandBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applySoulSandStats(stack, "Boots", 1, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "soul_sand_boots");
        return stack;
    }
    
    private static void applySoulSandStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        if (!stack.isOf(Items.PLAYER_HEAD)) {
            stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(SOUL_SAND_COLOR));
        }
        
        stack.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Soul Sand " + pieceName).formatted(Formatting.DARK_GRAY, Formatting.BOLD));
        stack.set(DataComponentTypes.MAX_DAMAGE, SOUL_SAND_DURABILITY);
        stack.set(DataComponentTypes.DAMAGE, 0);
        
        AttributeModifierSlot modifierSlot = getSlotForPiece(pieceName);
        AttributeModifiersComponent.Builder attrBuilder = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "soul_sand_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(EntityAttributes.ARMOR_TOUGHNESS,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "soul_sand_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("🛡 Armor: ").formatted(Formatting.WHITE)
                .append(Text.literal("+" + armorValue).formatted(Formatting.GRAY)));
        lore.add(Text.literal("💎 Toughness: ").formatted(Formatting.WHITE)
                .append(Text.literal("+" + toughnessValue).formatted(Formatting.GRAY)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ SET BONUS ━━━").formatted(Formatting.DARK_PURPLE));
        lore.add(Text.literal("✦ Full set: Soul Speed II").formatted(Formatting.DARK_BLUE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Infused with the souls of the nether").formatted(Formatting.DARK_GRAY));
        
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // MAGMA ARMOR - Burning hot armor
    // ============================================================
    
    public static ItemStack createMagmaHelmet() {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        applyPlayerHeadTexture(stack, MAGMA_HEAD);
        applyMagmaStats(stack, "Helmet", 3, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "magma_helmet");
        return stack;
    }
    
    public static ItemStack createMagmaChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyMagmaStats(stack, "Chestplate", 6, 3, EquipmentSlot.CHEST);
        setCustomItemId(stack, "magma_chestplate");
        return stack;
    }
    
    public static ItemStack createMagmaLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyMagmaStats(stack, "Leggings", 5, 3, EquipmentSlot.LEGS);
        setCustomItemId(stack, "magma_leggings");
        return stack;
    }
    
    public static ItemStack createMagmaBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyMagmaStats(stack, "Boots", 2, 2, EquipmentSlot.FEET);
        setCustomItemId(stack, "magma_boots");
        return stack;
    }
    
    private static void applyMagmaStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        if (!stack.isOf(Items.PLAYER_HEAD)) {
            stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(MAGMA_COLOR));
        }
        
        stack.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Magma " + pieceName).formatted(Formatting.RED, Formatting.BOLD));
        stack.set(DataComponentTypes.MAX_DAMAGE, MAGMA_DURABILITY);
        stack.set(DataComponentTypes.DAMAGE, 0);
        
        AttributeModifierSlot modifierSlot = getSlotForPiece(pieceName);
        AttributeModifiersComponent.Builder attrBuilder = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "magma_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(EntityAttributes.ARMOR_TOUGHNESS,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "magma_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.RED));
        lore.add(Text.literal("🛡 Armor: ").formatted(Formatting.WHITE)
                .append(Text.literal("+" + armorValue).formatted(Formatting.GOLD)));
        lore.add(Text.literal("💎 Toughness: ").formatted(Formatting.WHITE)
                .append(Text.literal("+" + toughnessValue).formatted(Formatting.GOLD)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ SET BONUS ━━━").formatted(Formatting.DARK_PURPLE));
        lore.add(Text.literal("✦ Full set: Fire Resistance").formatted(Formatting.GOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Burning with eternal heat").formatted(Formatting.DARK_GRAY));
        
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // SANDSTONE ARMOR - Desert protector armor
    // ============================================================
    
    public static ItemStack createSandstoneHelmet() {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        applyPlayerHeadTexture(stack, SANDSTONE_HEAD);
        applySandstoneStats(stack, "Helmet", 2, 1, EquipmentSlot.HEAD);
        setCustomItemId(stack, "sandstone_helmet");
        return stack;
    }
    
    public static ItemStack createSandstoneChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applySandstoneStats(stack, "Chestplate", 4, 2, EquipmentSlot.CHEST);
        setCustomItemId(stack, "sandstone_chestplate");
        return stack;
    }
    
    public static ItemStack createSandstoneLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applySandstoneStats(stack, "Leggings", 3, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "sandstone_leggings");
        return stack;
    }
    
    public static ItemStack createSandstoneBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applySandstoneStats(stack, "Boots", 1, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "sandstone_boots");
        return stack;
    }
    
    private static void applySandstoneStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        if (!stack.isOf(Items.PLAYER_HEAD)) {
            stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(SANDSTONE_COLOR));
        }
        
        stack.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Sandstone " + pieceName).formatted(Formatting.YELLOW, Formatting.BOLD));
        stack.set(DataComponentTypes.MAX_DAMAGE, SANDSTONE_DURABILITY);
        stack.set(DataComponentTypes.DAMAGE, 0);
        
        AttributeModifierSlot modifierSlot = getSlotForPiece(pieceName);
        AttributeModifiersComponent.Builder attrBuilder = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "sandstone_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(EntityAttributes.ARMOR_TOUGHNESS,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "sandstone_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.YELLOW));
        lore.add(Text.literal("🛡 Armor: ").formatted(Formatting.WHITE)
                .append(Text.literal("+" + armorValue).formatted(Formatting.YELLOW)));
        lore.add(Text.literal("💎 Toughness: ").formatted(Formatting.WHITE)
                .append(Text.literal("+" + toughnessValue).formatted(Formatting.YELLOW)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ SET BONUS ━━━").formatted(Formatting.DARK_PURPLE));
        lore.add(Text.literal("✦ Full set: No fall damage").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Carved from ancient desert stone").formatted(Formatting.DARK_GRAY));
        
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // AMETHYST ARMOR - Crystal armor with magic properties
    // ============================================================
    
    public static ItemStack createAmethystHelmet() {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        applyPlayerHeadTexture(stack, AMETHYST_HEAD);
        applyAmethystStats(stack, "Helmet", 2, 3, EquipmentSlot.HEAD);
        setCustomItemId(stack, "amethyst_helmet");
        return stack;
    }
    
    public static ItemStack createAmethystChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyAmethystStats(stack, "Chestplate", 4, 4, EquipmentSlot.CHEST);
        setCustomItemId(stack, "amethyst_chestplate");
        return stack;
    }
    
    public static ItemStack createAmethystLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyAmethystStats(stack, "Leggings", 3, 4, EquipmentSlot.LEGS);
        setCustomItemId(stack, "amethyst_leggings");
        return stack;
    }
    
    public static ItemStack createAmethystBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyAmethystStats(stack, "Boots", 1, 3, EquipmentSlot.FEET);
        setCustomItemId(stack, "amethyst_boots");
        return stack;
    }
    
    private static void applyAmethystStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        if (!stack.isOf(Items.PLAYER_HEAD)) {
            stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(AMETHYST_COLOR));
        }
        
        stack.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Amethyst " + pieceName).formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        stack.set(DataComponentTypes.MAX_DAMAGE, AMETHYST_DURABILITY);
        stack.set(DataComponentTypes.DAMAGE, 0);
        
        AttributeModifierSlot modifierSlot = getSlotForPiece(pieceName);
        AttributeModifiersComponent.Builder attrBuilder = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "amethyst_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(EntityAttributes.ARMOR_TOUGHNESS,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "amethyst_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal("🛡 Armor: ").formatted(Formatting.WHITE)
                .append(Text.literal("+" + armorValue).formatted(Formatting.DARK_PURPLE)));
        lore.add(Text.literal("💎 Toughness: ").formatted(Formatting.WHITE)
                .append(Text.literal("+" + toughnessValue).formatted(Formatting.DARK_PURPLE)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ SET BONUS ━━━").formatted(Formatting.DARK_PURPLE));
        lore.add(Text.literal("✦ Full set: Magic damage reduction").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Crystalline armor with magical properties").formatted(Formatting.DARK_GRAY));
        
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // COAL ARMOR - Dark carbon armor
    // ============================================================
    
    public static ItemStack createCoalHelmet() {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        applyPlayerHeadTexture(stack, COAL_HEAD);
        applyCoalStats(stack, "Helmet", 2, 1, EquipmentSlot.HEAD);
        setCustomItemId(stack, "coal_helmet");
        return stack;
    }
    
    public static ItemStack createCoalChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyCoalStats(stack, "Chestplate", 3, 2, EquipmentSlot.CHEST);
        setCustomItemId(stack, "coal_chestplate");
        return stack;
    }
    
    public static ItemStack createCoalLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyCoalStats(stack, "Leggings", 2, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "coal_leggings");
        return stack;
    }
    
    public static ItemStack createCoalBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyCoalStats(stack, "Boots", 1, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "coal_boots");
        return stack;
    }
    
    private static void applyCoalStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        if (!stack.isOf(Items.PLAYER_HEAD)) {
            stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(COAL_COLOR));
        }
        
        stack.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Coal " + pieceName).formatted(Formatting.DARK_GRAY, Formatting.BOLD));
        stack.set(DataComponentTypes.MAX_DAMAGE, COAL_DURABILITY);
        stack.set(DataComponentTypes.DAMAGE, 0);
        
        AttributeModifierSlot modifierSlot = getSlotForPiece(pieceName);
        AttributeModifiersComponent.Builder attrBuilder = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "coal_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(EntityAttributes.ARMOR_TOUGHNESS,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "coal_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("🛡 Armor: ").formatted(Formatting.WHITE)
                .append(Text.literal("+" + armorValue).formatted(Formatting.GRAY)));
        lore.add(Text.literal("💎 Toughness: ").formatted(Formatting.WHITE)
                .append(Text.literal("+" + toughnessValue).formatted(Formatting.GRAY)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ SET BONUS ━━━").formatted(Formatting.DARK_PURPLE));
        lore.add(Text.literal("✦ Full set: Night Vision").formatted(Formatting.DARK_BLUE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Dark as the deepest caves").formatted(Formatting.DARK_GRAY));
        
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // HELPER METHODS
    // ============================================================
    
    private static void applyPlayerHeadTexture(ItemStack stack, String textureValue) {
        // Create a random UUID for the skull owner
        java.util.UUID uuid = java.util.UUID.randomUUID();
        
        // Build the NBT structure for the player head texture
        NbtCompound nbt = new NbtCompound();
        NbtCompound skullOwner = new NbtCompound();
        NbtCompound properties = new NbtCompound();
        NbtCompound textures = new NbtCompound();
        
        textures.putString("Value", textureValue);
        properties.put("textures", textures);
        skullOwner.put("properties", properties);
        skullOwner.putString("Id", uuid.toString());
        nbt.put("SkullOwner", skullOwner);
        
        // Apply the NBT to the item
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
    }
    
    private static void setCustomItemId(ItemStack stack, String id) {
        NbtCompound nbt = stack.get(DataComponentTypes.CUSTOM_DATA) != null 
                ? stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt() 
                : new NbtCompound();
        nbt.putString("block_armor_id", id);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
    }
    
    public static String getBlockArmorId(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        NbtComponent nbt = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (nbt == null) return null;
        String id = nbt.copyNbt().getString("block_armor_id", null);
        return (id != null && !id.isEmpty()) ? id : null;
    }
    
    public static boolean isBlockArmor(ItemStack stack) {
        return getBlockArmorId(stack) != null;
    }
    
    public static int countBlockArmorPieces(PlayerEntity player, String prefix) {
        int count = 0;
        String id;
        id = getBlockArmorId(player.getEquippedStack(EquipmentSlot.HEAD));
        if (id != null && id.startsWith(prefix)) count++;
        id = getBlockArmorId(player.getEquippedStack(EquipmentSlot.CHEST));
        if (id != null && id.startsWith(prefix)) count++;
        id = getBlockArmorId(player.getEquippedStack(EquipmentSlot.LEGS));
        if (id != null && id.startsWith(prefix)) count++;
        id = getBlockArmorId(player.getEquippedStack(EquipmentSlot.FEET));
        if (id != null && id.startsWith(prefix)) count++;
        return count;
    }
    
    public static boolean hasFullBlockArmorSet(PlayerEntity player, String prefix) {
        return countBlockArmorPieces(player, prefix) == 4;
    }
    
    private static AttributeModifierSlot getSlotForPiece(String pieceName) {
        return switch (pieceName) {
            case "Helmet" -> AttributeModifierSlot.HEAD;
            case "Chestplate" -> AttributeModifierSlot.CHEST;
            case "Leggings" -> AttributeModifierSlot.LEGS;
            case "Boots" -> AttributeModifierSlot.FEET;
            default -> AttributeModifierSlot.ANY;
        };
    }
    
    // ============================================================
    // GIVE ARMOR SETS
    // ============================================================
    
    public static void giveGlassSet(ServerPlayerEntity player) {
        giveItem(player, createGlassHelmet());
        giveItem(player, createGlassChestplate());
        giveItem(player, createGlassLeggings());
        giveItem(player, createGlassBoots());
        player.sendMessage(Text.literal("§b✔ Received full Glass armor set!"), false);
    }
    
    public static void giveObsidianSet(ServerPlayerEntity player) {
        giveItem(player, createObsidianHelmet());
        giveItem(player, createObsidianChestplate());
        giveItem(player, createObsidianLeggings());
        giveItem(player, createObsidianBoots());
        player.sendMessage(Text.literal("§5✔ Received full Obsidian armor set!"), false);
    }
    
    public static void giveQuartzSet(ServerPlayerEntity player) {
        giveItem(player, createQuartzHelmet());
        giveItem(player, createQuartzChestplate());
        giveItem(player, createQuartzLeggings());
        giveItem(player, createQuartzBoots());
        player.sendMessage(Text.literal("§f✔ Received full Quartz armor set!"), false);
    }
    
    public static void giveGlowstoneSet(ServerPlayerEntity player) {
        giveItem(player, createGlowstoneHelmet());
        giveItem(player, createGlowstoneChestplate());
        giveItem(player, createGlowstoneLeggings());
        giveItem(player, createGlowstoneBoots());
        player.sendMessage(Text.literal("§e✔ Received full Glowstone armor set!"), false);
    }
    
    public static void giveRedstoneSet(ServerPlayerEntity player) {
        giveItem(player, createRedstoneHelmet());
        giveItem(player, createRedstoneChestplate());
        giveItem(player, createRedstoneLeggings());
        giveItem(player, createRedstoneBoots());
        player.sendMessage(Text.literal("§c✔ Received full Redstone armor set!"), false);
    }
    
    public static void giveNetherrackSet(ServerPlayerEntity player) {
        giveItem(player, createNetherrackHelmet());
        giveItem(player, createNetherrackChestplate());
        giveItem(player, createNetherrackLeggings());
        giveItem(player, createNetherrackBoots());
        player.sendMessage(Text.literal("§4✔ Received full Netherrack armor set!"), false);
    }
    
    public static void giveEndstoneSet(ServerPlayerEntity player) {
        giveItem(player, createEndstoneHelmet());
        giveItem(player, createEndstoneChestplate());
        giveItem(player, createEndstoneLeggings());
        giveItem(player, createEndstoneBoots());
        player.sendMessage(Text.literal("§d✔ Received full End Stone armor set!"), false);
    }
    
    public static void giveIceSet(ServerPlayerEntity player) {
        giveItem(player, createIceHelmet());
        giveItem(player, createIceChestplate());
        giveItem(player, createIceLeggings());
        giveItem(player, createIceBoots());
        player.sendMessage(Text.literal("§b✔ Received full Packed Ice armor set!"), false);
    }
    
    public static void givePrismarineSet(ServerPlayerEntity player) {
        giveItem(player, createPrismarineHelmet());
        giveItem(player, createPrismarineChestplate());
        giveItem(player, createPrismarineLeggings());
        giveItem(player, createPrismarineBoots());
        player.sendMessage(Text.literal("§3✔ Received full Prismarine armor set!"), false);
    }
    
    public static void giveTerracottaSet(ServerPlayerEntity player) {
        giveItem(player, createTerracottaHelmet());
        giveItem(player, createTerracottaChestplate());
        giveItem(player, createTerracottaLeggings());
        giveItem(player, createTerracottaBoots());
        player.sendMessage(Text.literal("§6✔ Received full Terracotta armor set!"), false);
    }
    
    public static void giveMossySet(ServerPlayerEntity player) {
        giveItem(player, createMossyHelmet());
        giveItem(player, createMossyChestplate());
        giveItem(player, createMossyLeggings());
        giveItem(player, createMossyBoots());
        player.sendMessage(Text.literal("§2✔ Received full Mossy Stone armor set!"), false);
    }
    
    public static void giveSoulSandSet(ServerPlayerEntity player) {
        giveItem(player, createSoulSandHelmet());
        giveItem(player, createSoulSandChestplate());
        giveItem(player, createSoulSandLeggings());
        giveItem(player, createSoulSandBoots());
        player.sendMessage(Text.literal("§8✔ Received full Soul Sand armor set!"), false);
    }
    
    public static void giveMagmaSet(ServerPlayerEntity player) {
        giveItem(player, createMagmaHelmet());
        giveItem(player, createMagmaChestplate());
        giveItem(player, createMagmaLeggings());
        giveItem(player, createMagmaBoots());
        player.sendMessage(Text.literal("§4✔ Received full Magma armor set!"), false);
    }
    
    public static void giveSandstoneSet(ServerPlayerEntity player) {
        giveItem(player, createSandstoneHelmet());
        giveItem(player, createSandstoneChestplate());
        giveItem(player, createSandstoneLeggings());
        giveItem(player, createSandstoneBoots());
        player.sendMessage(Text.literal("§e✔ Received full Sandstone armor set!"), false);
    }
    
    public static void giveAmethystSet(ServerPlayerEntity player) {
        giveItem(player, createAmethystHelmet());
        giveItem(player, createAmethystChestplate());
        giveItem(player, createAmethystLeggings());
        giveItem(player, createAmethystBoots());
        player.sendMessage(Text.literal("§5✔ Received full Amethyst armor set!"), false);
    }
    
    public static void giveCoalSet(ServerPlayerEntity player) {
        giveItem(player, createCoalHelmet());
        giveItem(player, createCoalChestplate());
        giveItem(player, createCoalLeggings());
        giveItem(player, createCoalBoots());
        player.sendMessage(Text.literal("§8✔ Received full Coal armor set!"), false);
    }
    
    private static void giveItem(ServerPlayerEntity player, ItemStack stack) {
        if (!player.getInventory().insertStack(stack)) {
            player.dropItem(stack, false);
        }
    }
}
