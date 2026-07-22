package com.political;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;

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

    // 30+ NEW BLOCK ARMOR SETS
    
    // Diamond Block Armor - Ultimate hardness
    public static final int DIAMOND_BLOCK_DURABILITY = 1500;
    public static final int DIAMOND_BLOCK_COLOR = 0x00FFFF; // Cyan
    public static final String DIAMOND_BLOCK_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2IxZDVkMTBmYjI1Y2FmZGEyYjk2ZDhkZjMxZGU1M2QxOTQyMGIzOWQyYjVkZjYyYjE3MjFmN2YxYzQ4ZTA3In19fQ==";
    
    // Emerald Block Armor - Merchant's pride
    public static final int EMERALD_BLOCK_DURABILITY = 400;
    public static final int EMERALD_BLOCK_COLOR = 0x50C878; // Emerald green
    public static final String EMERALD_BLOCK_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDExYmIzNDM4NzRlZWY3YTI4MDM4MzE0OWY5Y2ViMGM0YWJjOGQ4YmYwMTMyYzI0ZGNkOTY4OGZjYTgyY2ZjIn19fQ==";
    
    // Gold Block Armor - Gilded luxury
    public static final int GOLD_BLOCK_DURABILITY = 150;
    public static final int GOLD_BLOCK_COLOR = 0xFFD700; // Gold
    public static final String GOLD_BLOCK_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTAyNTBiMmM3NmRhZGM0YzQ3NDk2YWVjM2JjZjE3N2Y3ODFhMWEyOGQyZDZkZGNmZGU3MjkxY2QxYThlMyJ9fX0=";
    
    // Iron Block Armor - Industrial strength
    public static final int IRON_BLOCK_DURABILITY = 500;
    public static final int IRON_BLOCK_COLOR = 0xD8D8D8; // Iron gray
    public static final String IRON_BLOCK_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzViMWRkYjNlYzk1ZDNlNjQyY2YxNjQ4Yjg2ZTY1OGZjOWY0MjY0YzhhY2YzMGY4ODk4YzYyYzg3YjI4NDkifX19";
    
    // Lapis Block Armor - Enchanted
    public static final int LAPIS_BLOCK_DURABILITY = 300;
    public static final int LAPIS_BLOCK_COLOR = 0x1E3F66; // Deep blue
    public static final String LAPIS_BLOCK_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzdmZmY5NDUyYzkyNzg4MDExNzY3MzVkZDYxYjc5ZTU3YjE3ZjRmOWY2MmY1ZDY2NmZmYjNlMjMxZSJ9fX0=";
    
    // Copper Block Armor - Conductive
    public static final int COPPER_BLOCK_DURABILITY = 250;
    public static final int COPPER_BLOCK_COLOR = 0xB87333; // Copper orange
    public static final String COPPER_BLOCK_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzNjMzZiYWY5Zjc2Yjk3M2UwM2JhYmY3NGQ5YzViZTU2ODQxZmFkNjViZjYzZGIxMmI1ZWYyZDA3ZGYifX19";
    
    // Ancient Debris Armor - Netherite ancient
    public static final int ANCIENT_DEBRIS_DURABILITY = 1800;
    public static final int ANCIENT_DEBRIS_COLOR = 0x4A3428; // Brown
    public static final String ANCIENT_DEBRIS_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWQyYzRiZjA2ZjRlMjY4ZGYzOTBhNDE3ZjM2N2Y4YTRhZGY1YTRlNjM5YzQzYzg5YmIxMWEzZDc4ZTc5NiJ9fX0=";
    
    // Basalt Armor - Volcanic pillar
    public static final int BASALT_DURABILITY = 450;
    public static final int BASALT_COLOR = 0x3D3D3D; // Dark gray
    public static final String BASALT_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzI3MjE2YjA0ZTU2ZjE0ZTU3Yjk2MGFmZDY3YWVhMjlmZDg5YmMxM2MyMmU5MjY1YjQ1YzE1YTk5YjczYyJ9fX0=";
    
    // Blackstone Armor - Dark fortress
    public static final int BLACKSTONE_DURABILITY = 400;
    public static final int BLACKSTONE_COLOR = 0x2C2C2C; // Black
    public static final String BLACKSTONE_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjE4MmQwYjViZGMxYWVkMmQxNTY3NmY2YzI5NjA1NzViZTNiYWY2NTkyMjY3ZDNmYTRiNzgyZTdkZjExODliIn19fQ==";
    
    // Bone Block Armor - Undead essence
    public static final int BONE_BLOCK_DURABILITY = 200;
    public static final int BONE_BLOCK_COLOR = 0xE3E3D8; // Bone white
    public static final String BONE_BLOCK_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTRiZTIzYTBjMzg2ZjFkNjBhNjk2Nzk0YWNlZGNkZjE4YzJhM2MxYjZjNjlhNjQyODgzZGI2MjNhNzYyOCJ9fX0=";
    
    // Brick Armor - Builder's pride
    public static final int BRICK_DURABILITY = 350;
    public static final int BRICK_COLOR = 0xA05040; // Brick red
    public static final String BRICK_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmYwYzYzYWIwY2Y0MmU2MDJlMWZlMGVmNWM0YzJmZDI3Yzk1ZDc3ZGQyYjc0NWYzYjk1YmUxZTkxNyJ9fX0=";
    
    // Cactus Armor - Prickly defense
    public static final int CACTUS_DURABILITY = 180;
    public static final int CACTUS_COLOR = 0x2D8B2D; // Cactus green
    public static final String CACTUS_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmMyYjE1Yjk5ZDA3MDQ3YmQyYWM1ODJiYTQ1ZDg4YzAyNzNmMjIzMjE2YzQ3YjY4Yzk4YWM4ZjJjODJkNzEifX19";
    
    // Calcite Armor - Crystal white
    public static final int CALCITE_DURABILITY = 280;
    public static final int CALCITE_COLOR = 0xF5F5F0; // Off-white
    public static final String CALCITE_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzhkYzk1YzQxMThiMjYyZGI1N2FjYTRhN2I0ZjUwZWNiYjNiNjFiZTI1MzU4ZDY0MmY2ZmFmYTQyZjdkYzYifX19";
    
    // Deepslate Armor - Deep dark
    public static final int DEEPSLATE_DURABILITY = 500;
    public static final int DEEPSLATE_COLOR = 0x3D3D3D; // Dark gray
    public static final String DEEPSLATE_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTgzZmExYjZmMDYzMDI1YmU0YjA3Yjg1OTZiMzJlYWQ4NWM4MDMxOTYwNzY4YzFkZDU1Nzg0ZTBhMjIifX19";
    
    // Dripstone Armor - Sharp and pointy
    public static final int DRIPSTONE_DURABILITY = 320;
    public static final int DRIPSTONE_COLOR = 0x8B7355; // Brown
    public static final String DRIPSTONE_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2IzNDA3ZDFjNzcxMGNhOWM3MjhlN2Y5MjU4ZGFhYjk1Y2FiZGY4MjQ2YTBjN2Y3ZmE5OWM4MjE4MyJ9fX0=";
    
    // Hay Bale Armor - Farmer's protection
    public static final int HAY_DURABILITY = 100;
    public static final int HAY_COLOR = 0xD4B832; // Yellow
    public static final String HAY_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2YwNjY3ZjIwYjk4ZGM3ZjBmMGNkYzAxNjYyZTFiY2YyMjEyOTJhNWMyZTE4YjY0YTY1ODRhMTRhZCJ9fX0=";
    
    // Honeycomb Armor - Beekeeper's suit
    public static final int HONEYCOMB_DURABILITY = 220;
    public static final int HONEYCOMB_COLOR = 0xD4A017; // Honey gold
    public static final String HONEYCOMB_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmY5MmYyNDQ0OTg2OTVhZGNkZjg5YTY1ZGY3MzZlNzY5NjY4MjQyZGY3MTc4MmJkN2Q0MjI3YWI1OCJ9fX0=";
    
    // Lily Pad Armor - Water walker
    public static final int LILY_PAD_DURABILITY = 150;
    public static final int LILY_PAD_COLOR = 0x4A8B4A; // Green
    public static final String LILY_PAD_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2UwYzQyZDA0YzgxZmI4ZmJjMmZlZDllMmUyYmJkMjM5YmUzMmQyMTFjZjRmYjYyZmQxZWRjZDcwIn19fQ==";
    
    // Melon Armor - Juicy protection
    public static final int MELON_DURABILITY = 160;
    public static final int MELON_COLOR = 0xE84C3D; // Melon red
    public static final String MELON_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjk0NDJlMzViMmViZDRjNTQ3ZmY5MjhlMjZjMjdmZGY5N2M4ZGM3MjEzN2Y3MjE4N2YyMzE4YzkifX19";
    
    // Moss Block Armor - Overgrown
    public static final int MOSS_BLOCK_DURABILITY = 180;
    public static final int MOSS_BLOCK_COLOR = 0x5D8C5D; // Moss green
    public static final String MOSS_BLOCK_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTRiZDI3YWZlNzE3M2M4YWE2MDQzZjNkMjE4ZGNkZmM4OTg2MGI0NjBhMzFmZTg0MmQ4NTViNSJ9fX0=";
    
    // Mycelium Armor - Mushroom network
    public static final int MYCELIUM_DURABILITY = 240;
    public static final int MYCELIUM_COLOR = 0x6B5B8C; // Purple-gray
    public static final String MYCELIUM_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTc5Yjk4ZGIwMTg2ZjhjZDQ0YzMwYjkzZTUyYjA5Y2U3OWY4ZjViMjc2MjA0MGRkZDY3ZTFiMjcifX19";
    
    // Nether Brick Armor - Fortress guard
    public static final int NETHER_BRICK_DURABILITY = 380;
    public static final int NETHER_BRICK_COLOR = 0x2B1619; // Dark red
    public static final String NETHER_BRICK_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjA5MzYzNmIzM2Y4MzRlZTdhZGM0MzQyNjE1YTRkNjkyZGU5ZGU3Y2ViYjgzZWNkMmJiYzkifX19";
    
    // Pumpkin Armor - Halloween spirit
    public static final int PUMPKIN_DURABILITY = 200;
    public static final int PUMPKIN_COLOR = 0xFF8C00; // Orange
    public static final String PUMPKIN_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDc2ZjU3NjQ4NTQ0ZDc4NTI3ODZkZjUyNDZkMzYxMjE0MjQ4NDQyZDhiYzgyMjZlMTE4ZGJkZSJ9fX0=";
    
    // Purpur Armor - End city elegant
    public static final int PURPUR_DURABILITY = 420;
    public static final int PURPUR_COLOR = 0xB565A7; // Purple-pink
    public static final String PURPUR_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTg3YzM5YTRiZjMwMjJiYTVlN2I3YTM4ZGYxNDU4ZGM3ODg1NDUyZDI5ODVmMzA0YWM3YjAwOWQxNCJ9fX0=";
    
    // Sand Armor - Desert dweller
    public static final int SAND_DURABILITY = 140;
    public static final int SAND_COLOR = 0xE6D8AD; // Sand beige
    public static final String SAND_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZThmMTUyYjY4YWE0NjBkNzBhMWY4YjYxZDc4MDRhMmIxNDEzZjFiYWMzYjI3YzhkNmJjNWEzYjQyIn19fQ==";
    
    // Sculk Armor - Deep dark sensor
    public static final int SCULK_DURABILITY = 480;
    public static final int SCULK_COLOR = 0x0A2A3A; // Dark cyan
    public static final String SCULK_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzIzODM5MjJiZGYxMzg2MzRlZjQ3ZGU5ZTIyMjhhM2EzNzAzYzk4MmY2YzYzZmE2MTQxNmY0NGI3MjUzNCJ9fX0=";
    
    // Shroomlight Armor - Fungal light
    public static final int SHROOMLIGHT_DURABILITY = 260;
    public static final int SHROOMLIGHT_COLOR = 0xFF6B4A; // Orange-pink
    public static final String SHROOMLIGHT_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjgzNmY0MjI3NTdjODI2MGNkM2I0NjhhMTQ4NTkzZTUyNzBiZmM1MDk3NjE5NmU0MjY3MjEyZDQ0Zjc1In19fQ==";
    
    // Slime Block Armor - Bouncy
    public static final int SLIME_DURABILITY = 180;
    public static final int SLIME_COLOR = 0x7CFC00; // Lime green
    public static final String SLIME_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzk2Yjc0N2MwZDYxM2I1YzkyYzYzMGFkNjE2MjM5ZjJiYzk0ZTU0YjJkZTI0MGY5MjY5ZjdjMjIxIn19fQ==";
    
    // Smooth Stone Armor - Refined
    public static final int SMOOTH_STONE_DURABILITY = 420;
    public static final int SMOOTH_STONE_COLOR = 0x9E9E9E; // Gray
    public static final String SMOOTH_STONE_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWMzODU4YThkZjU0MmUyN2YyZTZhZDliNjEyZGI4NDUzNDI3ZjJiZDIxYzk3ZGJiZDc4ZTcxMjQxYyJ9fX0=";
    
    // Snow Block Armor - Frozen
    public static final int SNOW_DURABILITY = 120;
    public static final int SNOW_COLOR = 0xFFFFFF; // White
    public static final String SNOW_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2VkMDY1YjI4ZDNjMGNkNDM0OGY3MjU5MmQwNTljMTgzNjI4NWRkZTU1Yzk1NjQ1YTQwMzg1YjkifX19";
    
    // Soul Soil Armor - Fire immunity
    public static final int SOUL_SOIL_DURABILITY = 280;
    public static final int SOUL_SOIL_COLOR = 0x3D2817; // Brown
    public static final String SOUL_SOIL_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjM3ZGIzNjE4N2M3ZWRkZGYxZmQ2OTk2YjMwYTZmNmM1NWI5Y2I0YTc1NmE3MmRmODI4ZGY3ZCJ9fX0=";
    
    // Sponge Armor - Water absorption
    public static final int SPONGE_DURABILITY = 200;
    public static final int SPONGE_COLOR = 0xF0E68C; // Khaki
    public static final String SPONGE_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjczZGI3YzJiNzY0YzJhNjg2ZjQwMzFmMjBjOTM5MTY4ODk1ZmM3YzI2MTVkYjI4MjVjODZkZmQyNyJ9fX0=";
    
    // Target Armor - Precision
    public static final int TARGET_DURABILITY = 240;
    public static final int TARGET_COLOR = 0xFFFFFF; // White-red
    public static final String TARGET_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmNkNjBiZjMxZDYxY2Y5Y2QyYzZkY2I3YWU3MjE1MjdjMjViZDE0MjZkMDI3YzZiZGY4ZDI0N2EwIn19fQ==";
    
    // TNT Armor - Explosive
    public static final int TNT_DURABILITY = 100;
    public static final int TNT_COLOR = 0xFF4040; // Red
    public static final String TNT_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGRhZTRkYWRkMjVmMTg5NjAyY2YzZGY1NDJhMWQ0ZWZmN2YwYTIzN2YyZjdiN2Y5ZjYwZTQyNSJ9fX0=";
    
    // Warped Stem Armor - Nether forest
    public static final int WARPED_DURABILITY = 320;
    public static final int WARPED_COLOR = 0x17A589; // Teal
    public static final String WARPED_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDY4MjRmYjQ3MjNjYThhNzA4OWQ0ZjE1OTY3ZDA4MzJkMjI4OWU4NzJiZTNkM2MwZTMyYjkifX19";
    
    // Wet Sponge Armor - Damp
    public static final int WET_SPONGE_DURABILITY = 180;
    public static final int WET_SPONGE_COLOR = 0xC4B454; // Dark khaki
    public static final String WET_SPONGE_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDVkYWNhMzY3M2JhMjNkNTYyNjFiYzk1MjY0OWI3NjQ4OTVkOTQ1OTY4YjAyYTRiZDc5MTYyZCJ9fX0=";

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
            case "diamond_block_helmet", "diamond_block_chestplate", "diamond_block_leggings", "diamond_block_boots" ->
                "diamond_block_armor:" + armorId.replace("diamond_block_", "") + ":color=" + DIAMOND_BLOCK_COLOR;
            case "emerald_block_helmet", "emerald_block_chestplate", "emerald_block_leggings", "emerald_block_boots" ->
                "emerald_block_armor:" + armorId.replace("emerald_block_", "") + ":color=" + EMERALD_BLOCK_COLOR;
            case "gold_block_helmet", "gold_block_chestplate", "gold_block_leggings", "gold_block_boots" ->
                "gold_block_armor:" + armorId.replace("gold_block_", "") + ":color=" + GOLD_BLOCK_COLOR;
            case "iron_block_helmet", "iron_block_chestplate", "iron_block_leggings", "iron_block_boots" ->
                "iron_block_armor:" + armorId.replace("iron_block_", "") + ":color=" + IRON_BLOCK_COLOR;
            case "lapis_block_helmet", "lapis_block_chestplate", "lapis_block_leggings", "lapis_block_boots" ->
                "lapis_block_armor:" + armorId.replace("lapis_block_", "") + ":color=" + LAPIS_BLOCK_COLOR;
            case "copper_block_helmet", "copper_block_chestplate", "copper_block_leggings", "copper_block_boots" ->
                "copper_block_armor:" + armorId.replace("copper_block_", "") + ":color=" + COPPER_BLOCK_COLOR;
            case "ancient_debris_helmet", "ancient_debris_chestplate", "ancient_debris_leggings", "ancient_debris_boots" ->
                "ancient_debris_armor:" + armorId.replace("ancient_debris_", "") + ":color=" + ANCIENT_DEBRIS_COLOR;
            case "basalt_helmet", "basalt_chestplate", "basalt_leggings", "basalt_boots" ->
                "basalt_armor:" + armorId.replace("basalt_", "") + ":color=" + BASALT_COLOR;
            case "blackstone_helmet", "blackstone_chestplate", "blackstone_leggings", "blackstone_boots" ->
                "blackstone_armor:" + armorId.replace("blackstone_", "") + ":color=" + BLACKSTONE_COLOR;
            case "bone_block_helmet", "bone_block_chestplate", "bone_block_leggings", "bone_block_boots" ->
                "bone_block_armor:" + armorId.replace("bone_block_", "") + ":color=" + BONE_BLOCK_COLOR;
            case "brick_helmet", "brick_chestplate", "brick_leggings", "brick_boots" ->
                "brick_armor:" + armorId.replace("brick_", "") + ":color=" + BRICK_COLOR;
            case "cactus_helmet", "cactus_chestplate", "cactus_leggings", "cactus_boots" ->
                "cactus_armor:" + armorId.replace("cactus_", "") + ":color=" + CACTUS_COLOR;
            case "calcite_helmet", "calcite_chestplate", "calcite_leggings", "calcite_boots" ->
                "calcite_armor:" + armorId.replace("calcite_", "") + ":color=" + CALCITE_COLOR;
            case "deepslate_helmet", "deepslate_chestplate", "deepslate_leggings", "deepslate_boots" ->
                "deepslate_armor:" + armorId.replace("deepslate_", "") + ":color=" + DEEPSLATE_COLOR;
            case "dripstone_helmet", "dripstone_chestplate", "dripstone_leggings", "dripstone_boots" ->
                "dripstone_armor:" + armorId.replace("dripstone_", "") + ":color=" + DRIPSTONE_COLOR;
            case "hay_helmet", "hay_chestplate", "hay_leggings", "hay_boots" ->
                "hay_armor:" + armorId.replace("hay_", "") + ":color=" + HAY_COLOR;
            case "honeycomb_helmet", "honeycomb_chestplate", "honeycomb_leggings", "honeycomb_boots" ->
                "honeycomb_armor:" + armorId.replace("honeycomb_", "") + ":color=" + HONEYCOMB_COLOR;
            case "lily_pad_helmet", "lily_pad_chestplate", "lily_pad_leggings", "lily_pad_boots" ->
                "lily_pad_armor:" + armorId.replace("lily_pad_", "") + ":color=" + LILY_PAD_COLOR;
            case "melon_helmet", "melon_chestplate", "melon_leggings", "melon_boots" ->
                "melon_armor:" + armorId.replace("melon_", "") + ":color=" + MELON_COLOR;
            case "moss_block_helmet", "moss_block_chestplate", "moss_block_leggings", "moss_block_boots" ->
                "moss_block_armor:" + armorId.replace("moss_block_", "") + ":color=" + MOSS_BLOCK_COLOR;
            case "mycelium_helmet", "mycelium_chestplate", "mycelium_leggings", "mycelium_boots" ->
                "mycelium_armor:" + armorId.replace("mycelium_", "") + ":color=" + MYCELIUM_COLOR;
            case "nether_brick_helmet", "nether_brick_chestplate", "nether_brick_leggings", "nether_brick_boots" ->
                "nether_brick_armor:" + armorId.replace("nether_brick_", "") + ":color=" + NETHER_BRICK_COLOR;
            case "pumpkin_helmet", "pumpkin_chestplate", "pumpkin_leggings", "pumpkin_boots" ->
                "pumpkin_armor:" + armorId.replace("pumpkin_", "") + ":color=" + PUMPKIN_COLOR;
            case "purpur_helmet", "purpur_chestplate", "purpur_leggings", "purpur_boots" ->
                "purpur_armor:" + armorId.replace("purpur_", "") + ":color=" + PURPUR_COLOR;
            case "sand_helmet", "sand_chestplate", "sand_leggings", "sand_boots" ->
                "sand_armor:" + armorId.replace("sand_", "") + ":color=" + SAND_COLOR;
            case "sculk_helmet", "sculk_chestplate", "sculk_leggings", "sculk_boots" ->
                "sculk_armor:" + armorId.replace("sculk_", "") + ":color=" + SCULK_COLOR;
            case "shroomlight_helmet", "shroomlight_chestplate", "shroomlight_leggings", "shroomlight_boots" ->
                "shroomlight_armor:" + armorId.replace("shroomlight_", "") + ":color=" + SHROOMLIGHT_COLOR;
            case "slime_helmet", "slime_chestplate", "slime_leggings", "slime_boots" ->
                "slime_armor:" + armorId.replace("slime_", "") + ":color=" + SLIME_COLOR;
            case "smooth_stone_helmet", "smooth_stone_chestplate", "smooth_stone_leggings", "smooth_stone_boots" ->
                "smooth_stone_armor:" + armorId.replace("smooth_stone_", "") + ":color=" + SMOOTH_STONE_COLOR;
            case "snow_helmet", "snow_chestplate", "snow_leggings", "snow_boots" ->
                "snow_armor:" + armorId.replace("snow_", "") + ":color=" + SNOW_COLOR;
            case "soul_soil_helmet", "soul_soil_chestplate", "soul_soil_leggings", "soul_soil_boots" ->
                "soul_soil_armor:" + armorId.replace("soul_soil_", "") + ":color=" + SOUL_SOIL_COLOR;
            case "sponge_helmet", "sponge_chestplate", "sponge_leggings", "sponge_boots" ->
                "sponge_armor:" + armorId.replace("sponge_", "") + ":color=" + SPONGE_COLOR;
            case "target_helmet", "target_chestplate", "target_leggings", "target_boots" ->
                "target_armor:" + armorId.replace("target_", "") + ":color=" + TARGET_COLOR;
            case "tnt_helmet", "tnt_chestplate", "tnt_leggings", "tnt_boots" ->
                "tnt_armor:" + armorId.replace("tnt_", "") + ":color=" + TNT_COLOR;
            case "warped_helmet", "warped_chestplate", "warped_leggings", "warped_boots" ->
                "warped_armor:" + armorId.replace("warped_", "") + ":color=" + WARPED_COLOR;
            case "wet_sponge_helmet", "wet_sponge_chestplate", "wet_sponge_leggings", "wet_sponge_boots" ->
                "wet_sponge_armor:" + armorId.replace("wet_sponge_", "") + ":color=" + WET_SPONGE_COLOR;
            default -> "unknown_armor:" + armorId;
        };
    }

    // ============================================================
    // GLASS ARMOR - Fragile but high toughness
    // ============================================================
    
    public static ItemStack createGlassHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
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
        if (!stack.is(Items.PLAYER_HEAD)) {
            stack.set(DataComponents.DYED_COLOR, new DyedItemColor(GLASS_COLOR));
        }
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Glass " + pieceName).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, GLASS_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "glass_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "glass_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.AQUA));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.AQUA)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.AQUA)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("VERY LOW (" + GLASS_DURABILITY + ")").withStyle(ChatFormatting.RED)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.AQUA, "glass");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Fragile but blindingly fast").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // OBSIDIAN ARMOR - Extremely durable, very heavy
    // ============================================================
    
    public static ItemStack createObsidianHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
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
        if (!stack.is(Items.PLAYER_HEAD)) {
            stack.set(DataComponents.DYED_COLOR, new DyedItemColor(OBSIDIAN_COLOR));
        }
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Obsidian " + pieceName).withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, OBSIDIAN_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "obsidian_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "obsidian_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.MOVEMENT_SPEED,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "obsidian_" + pieceName.toLowerCase() + "_speed"),
                                -0.05,
                                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_PURPLE));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.DARK_PURPLE)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.DARK_PURPLE)));
        lore.add(Component.literal("🏃 Speed: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("-5% per piece").withStyle(ChatFormatting.RED)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.GOLD, "obsidian");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Slow but nearly indestructible").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // QUARTZ ARMOR - Crystalline elegance
    // ============================================================
    
    public static ItemStack createQuartzHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
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
        if (!stack.is(Items.PLAYER_HEAD)) {
            stack.set(DataComponents.DYED_COLOR, new DyedItemColor(QUARTZ_COLOR));
        }
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Quartz " + pieceName).withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, QUARTZ_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "quartz_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "quartz_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.WHITE)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.WHITE)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.WHITE, "quartz");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Crystalline sharpness reflects damage").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // GLOWSTONE ARMOR - Light-emitting
    // ============================================================
    
    public static ItemStack createGlowstoneHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
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
        if (!stack.is(Items.PLAYER_HEAD)) {
            stack.set(DataComponents.DYED_COLOR, new DyedItemColor(GLOWSTONE_COLOR));
        }
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Glowstone " + pieceName).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, GLOWSTONE_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "glowstone_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "glowstone_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.GOLD, "glowstone");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Illuminated by the Nether").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // REDSTONE ARMOR - Technical and conductive
    // ============================================================
    
    public static ItemStack createRedstoneHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
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
        if (!stack.is(Items.PLAYER_HEAD)) {
            stack.set(DataComponents.DYED_COLOR, new DyedItemColor(REDSTONE_COLOR));
        }
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Redstone " + pieceName).withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, REDSTONE_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "redstone_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "redstone_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_RED));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.DARK_RED)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.DARK_RED)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.RED, "redstone");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Powered by redstone energy").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // NETHERRACK ARMOR - Fire-resistant
    // ============================================================
    
    public static ItemStack createNetherrackHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
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
        if (!stack.is(Items.PLAYER_HEAD)) {
            stack.set(DataComponents.DYED_COLOR, new DyedItemColor(NETHERRACK_COLOR));
        }
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Netherrack " + pieceName).withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, NETHERRACK_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "netherrack_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "netherrack_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.RED)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.RED)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.RED, "netherrack");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Forged in the Nether's heat").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // END STONE ARMOR - Void-touched
    // ============================================================
    
    public static ItemStack createEndstoneHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
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
        if (!stack.is(Items.PLAYER_HEAD)) {
            stack.set(DataComponents.DYED_COLOR, new DyedItemColor(ENDSTONE_COLOR));
        }
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("End Stone " + pieceName).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, ENDSTONE_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "endstone_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "endstone_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.DARK_PURPLE, "endstone");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Touched by the Void").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // ICE ARMOR - Cold and slippery
    // ============================================================
    
    public static ItemStack createIceHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
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
        if (!stack.is(Items.PLAYER_HEAD)) {
            stack.set(DataComponents.DYED_COLOR, new DyedItemColor(ICE_COLOR));
        }
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Packed Ice " + pieceName).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, ICE_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "ice_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "ice_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.AQUA));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.AQUA)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.AQUA)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.BLUE, "ice");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Frozen in eternal cold").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // PRISMARINE ARMOR - Ocean guardian
    // ============================================================
    
    public static ItemStack createPrismarineHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
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
        if (!stack.is(Items.PLAYER_HEAD)) {
            stack.set(DataComponents.DYED_COLOR, new DyedItemColor(PRISMARINE_COLOR));
        }
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Prismarine " + pieceName).withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, PRISMARINE_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "prismarine_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "prismarine_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_AQUA));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.DARK_AQUA)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.DARK_AQUA)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.DARK_AQUA, "prismarine");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Gift of the Ocean Monument").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // TERRACOTTA ARMOR - Hardened clay
    // ============================================================
    
    public static ItemStack createTerracottaHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
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
        if (!stack.is(Items.PLAYER_HEAD)) {
            stack.set(DataComponents.DYED_COLOR, new DyedItemColor(TERRACOTTA_COLOR));
        }
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Terracotta " + pieceName).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, TERRACOTTA_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "terracotta_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "terracotta_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.GOLD)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.GOLD)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.GOLD, "terracotta");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Hardened by fire and time").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // MOSSY STONE ARMOR - Ancient overgrown armor
    // ============================================================
    
    public static ItemStack createMossyHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
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
        if (!stack.is(Items.PLAYER_HEAD)) {
            stack.set(DataComponents.DYED_COLOR, new DyedItemColor(MOSSY_COLOR));
        }
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Mossy " + pieceName).withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, MOSSY_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "mossy_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "mossy_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_GREEN));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.DARK_GREEN, "mossy");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Ancient stone overgrown with moss").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // SOUL SAND ARMOR - Soul-infused armor
    // ============================================================
    
    public static ItemStack createSoulSandHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
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
        if (!stack.is(Items.PLAYER_HEAD)) {
            stack.set(DataComponents.DYED_COLOR, new DyedItemColor(SOUL_SAND_COLOR));
        }
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Soul Sand " + pieceName).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, SOUL_SAND_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "soul_sand_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "soul_sand_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.GRAY)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.GRAY)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.DARK_GRAY, "soul_sand");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Infused with the souls of the nether").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // MAGMA ARMOR - Burning hot armor
    // ============================================================
    
    public static ItemStack createMagmaHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
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
        if (!stack.is(Items.PLAYER_HEAD)) {
            stack.set(DataComponents.DYED_COLOR, new DyedItemColor(MAGMA_COLOR));
        }
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Magma " + pieceName).withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, MAGMA_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "magma_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "magma_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.GOLD)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.GOLD)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.RED, "magma");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Burning with eternal heat").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // SANDSTONE ARMOR - Desert protector armor
    // ============================================================
    
    public static ItemStack createSandstoneHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
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
        if (!stack.is(Items.PLAYER_HEAD)) {
            stack.set(DataComponents.DYED_COLOR, new DyedItemColor(SANDSTONE_COLOR));
        }
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Sandstone " + pieceName).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, SANDSTONE_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "sandstone_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "sandstone_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.YELLOW, "sandstone");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Carved from ancient desert stone").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // AMETHYST ARMOR - Crystal armor with magic properties
    // ============================================================
    
    public static ItemStack createAmethystHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
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
        if (!stack.is(Items.PLAYER_HEAD)) {
            stack.set(DataComponents.DYED_COLOR, new DyedItemColor(AMETHYST_COLOR));
        }
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Amethyst " + pieceName).withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, AMETHYST_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "amethyst_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "amethyst_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.LIGHT_PURPLE));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.DARK_PURPLE)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.DARK_PURPLE)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.DARK_PURPLE, "amethyst");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Resonant crystal wards off magic").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // COAL ARMOR - Dark carbon armor
    // ============================================================
    
    public static ItemStack createCoalHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
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
        if (!stack.is(Items.PLAYER_HEAD)) {
            stack.set(DataComponents.DYED_COLOR, new DyedItemColor(COAL_COLOR));
        }
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Coal " + pieceName).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, COAL_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "coal_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "coal_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.GRAY)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.GRAY)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.DARK_GRAY, "coal");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Dark as the deepest caves").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // 30+ NEW BLOCK ARMOR SETS
    // ============================================================

    // DIAMOND BLOCK ARMOR - Ultimate hardness
    public static ItemStack createDiamondBlockHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyDiamondBlockStats(stack, "Helmet", 4, 5, EquipmentSlot.HEAD);
        setCustomItemId(stack, "diamond_block_helmet");
        return stack;
    }

    public static ItemStack createDiamondBlockChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyDiamondBlockStats(stack, "Chestplate", 9, 5, EquipmentSlot.CHEST);
        setCustomItemId(stack, "diamond_block_chestplate");
        return stack;
    }

    public static ItemStack createDiamondBlockLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyDiamondBlockStats(stack, "Leggings", 7, 5, EquipmentSlot.LEGS);
        setCustomItemId(stack, "diamond_block_leggings");
        return stack;
    }

    public static ItemStack createDiamondBlockBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyDiamondBlockStats(stack, "Boots", 4, 5, EquipmentSlot.FEET);
        setCustomItemId(stack, "diamond_block_boots");
        return stack;
    }

    private static void applyDiamondBlockStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        if (!stack.is(Items.PLAYER_HEAD)) {
            stack.set(DataComponents.DYED_COLOR, new DyedItemColor(DIAMOND_BLOCK_COLOR));
        }
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Diamond Block " + pieceName).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, DIAMOND_BLOCK_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "diamond_block_" + pieceName.toLowerCase() + "_armor"), armorValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "diamond_block_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.AQUA));
        lore.add(Component.literal("🛡 Armor: +" + armorValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal("💎 Toughness: +" + toughnessValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.LIGHT_PURPLE, "diamond_block");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Ultimate luxury and protection").withStyle(ChatFormatting.DARK_GRAY));
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // EMERALD BLOCK ARMOR - Merchant's pride
    public static ItemStack createEmeraldBlockHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyEmeraldBlockStats(stack, "Helmet", 3, 3, EquipmentSlot.HEAD);
        setCustomItemId(stack, "emerald_block_helmet");
        return stack;
    }

    public static ItemStack createEmeraldBlockChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyEmeraldBlockStats(stack, "Chestplate", 6, 3, EquipmentSlot.CHEST);
        setCustomItemId(stack, "emerald_block_chestplate");
        return stack;
    }

    public static ItemStack createEmeraldBlockLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyEmeraldBlockStats(stack, "Leggings", 5, 3, EquipmentSlot.LEGS);
        setCustomItemId(stack, "emerald_block_leggings");
        return stack;
    }

    public static ItemStack createEmeraldBlockBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyEmeraldBlockStats(stack, "Boots", 3, 3, EquipmentSlot.FEET);
        setCustomItemId(stack, "emerald_block_boots");
        return stack;
    }

    private static void applyEmeraldBlockStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        if (!stack.is(Items.PLAYER_HEAD)) {
            stack.set(DataComponents.DYED_COLOR, new DyedItemColor(EMERALD_BLOCK_COLOR));
        }
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Emerald Block " + pieceName).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, EMERALD_BLOCK_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "emerald_block_" + pieceName.toLowerCase() + "_armor"), armorValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "emerald_block_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("🛡 Armor: +" + armorValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal("💎 Toughness: +" + toughnessValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.LIGHT_PURPLE, "emerald_block");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8The merchant's choice").withStyle(ChatFormatting.DARK_GRAY));
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // GOLD BLOCK ARMOR - Gilded luxury
    public static ItemStack createGoldBlockHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyGoldBlockStats(stack, "Helmet", 2, 1, EquipmentSlot.HEAD);
        setCustomItemId(stack, "gold_block_helmet");
        return stack;
    }

    public static ItemStack createGoldBlockChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyGoldBlockStats(stack, "Chestplate", 4, 1, EquipmentSlot.CHEST);
        setCustomItemId(stack, "gold_block_chestplate");
        return stack;
    }

    public static ItemStack createGoldBlockLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyGoldBlockStats(stack, "Leggings", 3, 1, EquipmentSlot.LEGS);
        setCustomItemId(stack, "gold_block_leggings");
        return stack;
    }

    public static ItemStack createGoldBlockBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyGoldBlockStats(stack, "Boots", 1, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "gold_block_boots");
        return stack;
    }

    private static void applyGoldBlockStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        if (!stack.is(Items.PLAYER_HEAD)) {
            stack.set(DataComponents.DYED_COLOR, new DyedItemColor(GOLD_BLOCK_COLOR));
        }
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Gold Block " + pieceName).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, GOLD_BLOCK_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "gold_block_" + pieceName.toLowerCase() + "_armor"), armorValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "gold_block_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("🛡 Armor: +" + armorValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal("💎 Toughness: +" + toughnessValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.LIGHT_PURPLE, "gold_block");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Gilded and glorious").withStyle(ChatFormatting.DARK_GRAY));
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // IRON BLOCK ARMOR - Industrial strength
    public static ItemStack createIronBlockHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyIronBlockStats(stack, "Helmet", 3, 3, EquipmentSlot.HEAD);
        setCustomItemId(stack, "iron_block_helmet");
        return stack;
    }

    public static ItemStack createIronBlockChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyIronBlockStats(stack, "Chestplate", 7, 3, EquipmentSlot.CHEST);
        setCustomItemId(stack, "iron_block_chestplate");
        return stack;
    }

    public static ItemStack createIronBlockLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyIronBlockStats(stack, "Leggings", 5, 3, EquipmentSlot.LEGS);
        setCustomItemId(stack, "iron_block_leggings");
        return stack;
    }

    public static ItemStack createIronBlockBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyIronBlockStats(stack, "Boots", 3, 3, EquipmentSlot.FEET);
        setCustomItemId(stack, "iron_block_boots");
        return stack;
    }

    private static void applyIronBlockStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        if (!stack.is(Items.PLAYER_HEAD)) {
            stack.set(DataComponents.DYED_COLOR, new DyedItemColor(IRON_BLOCK_COLOR));
        }
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Iron Block " + pieceName).withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, IRON_BLOCK_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "iron_block_" + pieceName.toLowerCase() + "_armor"), armorValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "iron_block_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal("🛡 Armor: +" + armorValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal("💎 Toughness: +" + toughnessValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.LIGHT_PURPLE, "iron_block");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Industrial grade protection").withStyle(ChatFormatting.DARK_GRAY));
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // LAPIS BLOCK ARMOR - Enchanted
    public static ItemStack createLapisBlockHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyLapisBlockStats(stack, "Helmet", 2, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "lapis_block_helmet");
        return stack;
    }

    public static ItemStack createLapisBlockChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyLapisBlockStats(stack, "Chestplate", 5, 2, EquipmentSlot.CHEST);
        setCustomItemId(stack, "lapis_block_chestplate");
        return stack;
    }

    public static ItemStack createLapisBlockLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyLapisBlockStats(stack, "Leggings", 4, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "lapis_block_leggings");
        return stack;
    }

    public static ItemStack createLapisBlockBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyLapisBlockStats(stack, "Boots", 2, 2, EquipmentSlot.FEET);
        setCustomItemId(stack, "lapis_block_boots");
        return stack;
    }

    private static void applyLapisBlockStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        if (!stack.is(Items.PLAYER_HEAD)) {
            stack.set(DataComponents.DYED_COLOR, new DyedItemColor(LAPIS_BLOCK_COLOR));
        }
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Lapis Block " + pieceName).withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, LAPIS_BLOCK_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "lapis_block_" + pieceName.toLowerCase() + "_armor"), armorValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "lapis_block_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.BLUE));
        lore.add(Component.literal("🛡 Armor: +" + armorValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal("💎 Toughness: +" + toughnessValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.LIGHT_PURPLE, "lapis_block");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Knowledge is power").withStyle(ChatFormatting.DARK_GRAY));
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // COPPER BLOCK ARMOR - Conductive
    public static ItemStack createCopperBlockHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyCopperBlockStats(stack, "Helmet", 2, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "copper_block_helmet");
        return stack;
    }

    public static ItemStack createCopperBlockChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyCopperBlockStats(stack, "Chestplate", 4, 2, EquipmentSlot.CHEST);
        setCustomItemId(stack, "copper_block_chestplate");
        return stack;
    }

    public static ItemStack createCopperBlockLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyCopperBlockStats(stack, "Leggings", 3, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "copper_block_leggings");
        return stack;
    }

    public static ItemStack createCopperBlockBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyCopperBlockStats(stack, "Boots", 2, 2, EquipmentSlot.FEET);
        setCustomItemId(stack, "copper_block_boots");
        return stack;
    }

    private static void applyCopperBlockStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        if (!stack.is(Items.PLAYER_HEAD)) {
            stack.set(DataComponents.DYED_COLOR, new DyedItemColor(COPPER_BLOCK_COLOR));
        }
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Copper Block " + pieceName).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, COPPER_BLOCK_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "copper_block_" + pieceName.toLowerCase() + "_armor"), armorValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "copper_block_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("🛡 Armor: +" + armorValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal("💎 Toughness: +" + toughnessValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.LIGHT_PURPLE, "copper_block");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Conductive and practical").withStyle(ChatFormatting.DARK_GRAY));
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ANCIENT DEBRIS ARMOR - Netherite ancient
    public static ItemStack createAncientDebrisHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyAncientDebrisStats(stack, "Helmet", 4, 5, EquipmentSlot.HEAD);
        setCustomItemId(stack, "ancient_debris_helmet");
        return stack;
    }
    public static ItemStack createAncientDebrisChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyAncientDebrisStats(stack, "Chestplate", 9, 5, EquipmentSlot.CHEST);
        setCustomItemId(stack, "ancient_debris_chestplate");
        return stack;
    }
    public static ItemStack createAncientDebrisLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyAncientDebrisStats(stack, "Leggings", 7, 5, EquipmentSlot.LEGS);
        setCustomItemId(stack, "ancient_debris_leggings");
        return stack;
    }
    public static ItemStack createAncientDebrisBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyAncientDebrisStats(stack, "Boots", 4, 5, EquipmentSlot.FEET);
        setCustomItemId(stack, "ancient_debris_boots");
        return stack;
    }
    private static void applyAncientDebrisStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        if (!stack.is(Items.PLAYER_HEAD)) stack.set(DataComponents.DYED_COLOR, new DyedItemColor(ANCIENT_DEBRIS_COLOR));
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Ancient Debris " + pieceName).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, ANCIENT_DEBRIS_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "ancient_debris_" + pieceName.toLowerCase() + "_armor"), armorValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "ancient_debris_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("🛡 Armor: +" + armorValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal("💎 Toughness: +" + toughnessValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.LIGHT_PURPLE, "ancient_debris");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Ancient and unbreakable").withStyle(ChatFormatting.DARK_GRAY));
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // BASALT ARMOR - Volcanic pillar
    public static ItemStack createBasaltHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyBasaltStats(stack, "Helmet", 3, 3, EquipmentSlot.HEAD);
        setCustomItemId(stack, "basalt_helmet");
        return stack;
    }
    public static ItemStack createBasaltChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyBasaltStats(stack, "Chestplate", 6, 3, EquipmentSlot.CHEST);
        setCustomItemId(stack, "basalt_chestplate");
        return stack;
    }
    public static ItemStack createBasaltLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyBasaltStats(stack, "Leggings", 5, 3, EquipmentSlot.LEGS);
        setCustomItemId(stack, "basalt_leggings");
        return stack;
    }
    public static ItemStack createBasaltBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyBasaltStats(stack, "Boots", 3, 3, EquipmentSlot.FEET);
        setCustomItemId(stack, "basalt_boots");
        return stack;
    }
    private static void applyBasaltStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        if (!stack.is(Items.PLAYER_HEAD)) stack.set(DataComponents.DYED_COLOR, new DyedItemColor(BASALT_COLOR));
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Basalt " + pieceName).withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, BASALT_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "basalt_" + pieceName.toLowerCase() + "_armor"), armorValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "basalt_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("🛡 Armor: +" + armorValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal("💎 Toughness: +" + toughnessValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.LIGHT_PURPLE, "basalt");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Formed in volcanic heat").withStyle(ChatFormatting.DARK_GRAY));
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // BLACKSTONE ARMOR - Dark fortress
    public static ItemStack createBlackstoneHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyBlackstoneStats(stack, "Helmet", 3, 3, EquipmentSlot.HEAD);
        setCustomItemId(stack, "blackstone_helmet");
        return stack;
    }
    public static ItemStack createBlackstoneChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyBlackstoneStats(stack, "Chestplate", 6, 3, EquipmentSlot.CHEST);
        setCustomItemId(stack, "blackstone_chestplate");
        return stack;
    }
    public static ItemStack createBlackstoneLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyBlackstoneStats(stack, "Leggings", 5, 3, EquipmentSlot.LEGS);
        setCustomItemId(stack, "blackstone_leggings");
        return stack;
    }
    public static ItemStack createBlackstoneBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyBlackstoneStats(stack, "Boots", 3, 3, EquipmentSlot.FEET);
        setCustomItemId(stack, "blackstone_boots");
        return stack;
    }
    private static void applyBlackstoneStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        if (!stack.is(Items.PLAYER_HEAD)) stack.set(DataComponents.DYED_COLOR, new DyedItemColor(BLACKSTONE_COLOR));
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Blackstone " + pieceName).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, BLACKSTONE_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "blackstone_" + pieceName.toLowerCase() + "_armor"), armorValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "blackstone_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("🛡 Armor: +" + armorValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal("💎 Toughness: +" + toughnessValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.LIGHT_PURPLE, "blackstone");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Forged in the Nether").withStyle(ChatFormatting.DARK_GRAY));
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // BONE BLOCK ARMOR - Undead essence
    public static ItemStack createBoneBlockHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyBoneBlockStats(stack, "Helmet", 2, 1, EquipmentSlot.HEAD);
        setCustomItemId(stack, "bone_block_helmet");
        return stack;
    }
    public static ItemStack createBoneBlockChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyBoneBlockStats(stack, "Chestplate", 4, 1, EquipmentSlot.CHEST);
        setCustomItemId(stack, "bone_block_chestplate");
        return stack;
    }
    public static ItemStack createBoneBlockLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyBoneBlockStats(stack, "Leggings", 3, 1, EquipmentSlot.LEGS);
        setCustomItemId(stack, "bone_block_leggings");
        return stack;
    }
    public static ItemStack createBoneBlockBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyBoneBlockStats(stack, "Boots", 2, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "bone_block_boots");
        return stack;
    }
    private static void applyBoneBlockStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        if (!stack.is(Items.PLAYER_HEAD)) stack.set(DataComponents.DYED_COLOR, new DyedItemColor(BONE_BLOCK_COLOR));
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Bone Block " + pieceName).withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, BONE_BLOCK_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "bone_block_" + pieceName.toLowerCase() + "_armor"), armorValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "bone_block_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal("🛡 Armor: +" + armorValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal("💎 Toughness: +" + toughnessValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.LIGHT_PURPLE, "bone_block");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Skeleton's pride").withStyle(ChatFormatting.DARK_GRAY));
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // BRICK ARMOR - Builder's pride
    public static ItemStack createBrickHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyBrickStats(stack, "Helmet", 3, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "brick_helmet");
        return stack;
    }
    public static ItemStack createBrickChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyBrickStats(stack, "Chestplate", 5, 2, EquipmentSlot.CHEST);
        setCustomItemId(stack, "brick_chestplate");
        return stack;
    }
    public static ItemStack createBrickLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyBrickStats(stack, "Leggings", 4, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "brick_leggings");
        return stack;
    }
    public static ItemStack createBrickBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyBrickStats(stack, "Boots", 2, 2, EquipmentSlot.FEET);
        setCustomItemId(stack, "brick_boots");
        return stack;
    }
    private static void applyBrickStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        if (!stack.is(Items.PLAYER_HEAD)) stack.set(DataComponents.DYED_COLOR, new DyedItemColor(BRICK_COLOR));
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Brick " + pieceName).withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, BRICK_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "brick_" + pieceName.toLowerCase() + "_armor"), armorValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "brick_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("🛡 Armor: +" + armorValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal("💎 Toughness: +" + toughnessValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.LIGHT_PURPLE, "brick");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Solid construction").withStyle(ChatFormatting.DARK_GRAY));
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // CACTUS ARMOR - Prickly defense
    public static ItemStack createCactusHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyCactusStats(stack, "Helmet", 2, 1, EquipmentSlot.HEAD);
        setCustomItemId(stack, "cactus_helmet");
        return stack;
    }
    public static ItemStack createCactusChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyCactusStats(stack, "Chestplate", 3, 1, EquipmentSlot.CHEST);
        setCustomItemId(stack, "cactus_chestplate");
        return stack;
    }
    public static ItemStack createCactusLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyCactusStats(stack, "Leggings", 3, 1, EquipmentSlot.LEGS);
        setCustomItemId(stack, "cactus_leggings");
        return stack;
    }
    public static ItemStack createCactusBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyCactusStats(stack, "Boots", 1, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "cactus_boots");
        return stack;
    }
    private static void applyCactusStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        if (!stack.is(Items.PLAYER_HEAD)) stack.set(DataComponents.DYED_COLOR, new DyedItemColor(CACTUS_COLOR));
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Cactus " + pieceName).withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, CACTUS_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "cactus_" + pieceName.toLowerCase() + "_armor"), armorValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "cactus_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_GREEN));
        lore.add(Component.literal("🛡 Armor: +" + armorValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal("💎 Toughness: +" + toughnessValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.LIGHT_PURPLE, "cactus");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Prickly protection").withStyle(ChatFormatting.DARK_GRAY));
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // CALCITE ARMOR - Crystal white
    public static ItemStack createCalciteHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyCalciteStats(stack, "Helmet", 3, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "calcite_helmet");
        return stack;
    }
    public static ItemStack createCalciteChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyCalciteStats(stack, "Chestplate", 5, 2, EquipmentSlot.CHEST);
        setCustomItemId(stack, "calcite_chestplate");
        return stack;
    }
    public static ItemStack createCalciteLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyCalciteStats(stack, "Leggings", 4, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "calcite_leggings");
        return stack;
    }
    public static ItemStack createCalciteBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyCalciteStats(stack, "Boots", 2, 2, EquipmentSlot.FEET);
        setCustomItemId(stack, "calcite_boots");
        return stack;
    }
    private static void applyCalciteStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        if (!stack.is(Items.PLAYER_HEAD)) stack.set(DataComponents.DYED_COLOR, new DyedItemColor(CALCITE_COLOR));
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Calcite " + pieceName).withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, CALCITE_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "calcite_" + pieceName.toLowerCase() + "_armor"), armorValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "calcite_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal("🛡 Armor: +" + armorValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal("💎 Toughness: +" + toughnessValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.LIGHT_PURPLE, "calcite");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Pure and pristine").withStyle(ChatFormatting.DARK_GRAY));
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // DEEPSLATE ARMOR - Deep dark
    public static ItemStack createDeepslateHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyDeepslateStats(stack, "Helmet", 3, 4, EquipmentSlot.HEAD);
        setCustomItemId(stack, "deepslate_helmet");
        return stack;
    }
    public static ItemStack createDeepslateChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyDeepslateStats(stack, "Chestplate", 7, 4, EquipmentSlot.CHEST);
        setCustomItemId(stack, "deepslate_chestplate");
        return stack;
    }
    public static ItemStack createDeepslateLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyDeepslateStats(stack, "Leggings", 6, 4, EquipmentSlot.LEGS);
        setCustomItemId(stack, "deepslate_leggings");
        return stack;
    }
    public static ItemStack createDeepslateBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyDeepslateStats(stack, "Boots", 3, 4, EquipmentSlot.FEET);
        setCustomItemId(stack, "deepslate_boots");
        return stack;
    }
    private static void applyDeepslateStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        if (!stack.is(Items.PLAYER_HEAD)) stack.set(DataComponents.DYED_COLOR, new DyedItemColor(DEEPSLATE_COLOR));
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Deepslate " + pieceName).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, DEEPSLATE_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "deepslate_" + pieceName.toLowerCase() + "_armor"), armorValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "deepslate_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("🛡 Armor: +" + armorValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal("💎 Toughness: +" + toughnessValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.LIGHT_PURPLE, "deepslate");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8From the deep dark").withStyle(ChatFormatting.DARK_GRAY));
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // DRIPSTONE ARMOR - Sharp and pointy
    public static ItemStack createDripstoneHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyDripstoneStats(stack, "Helmet", 3, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "dripstone_helmet");
        return stack;
    }
    public static ItemStack createDripstoneChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyDripstoneStats(stack, "Chestplate", 5, 2, EquipmentSlot.CHEST);
        setCustomItemId(stack, "dripstone_chestplate");
        return stack;
    }
    public static ItemStack createDripstoneLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyDripstoneStats(stack, "Leggings", 4, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "dripstone_leggings");
        return stack;
    }
    public static ItemStack createDripstoneBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyDripstoneStats(stack, "Boots", 2, 2, EquipmentSlot.FEET);
        setCustomItemId(stack, "dripstone_boots");
        return stack;
    }
    private static void applyDripstoneStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        if (!stack.is(Items.PLAYER_HEAD)) stack.set(DataComponents.DYED_COLOR, new DyedItemColor(DRIPSTONE_COLOR));
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Dripstone " + pieceName).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, DRIPSTONE_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "dripstone_" + pieceName.toLowerCase() + "_armor"), armorValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "dripstone_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("🛡 Armor: +" + armorValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal("💎 Toughness: +" + toughnessValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.LIGHT_PURPLE, "dripstone");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Sharp and dripping").withStyle(ChatFormatting.DARK_GRAY));
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // HAY BALE ARMOR - Farmer's protection
    public static ItemStack createHayHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyHayStats(stack, "Helmet", 1, 0, EquipmentSlot.HEAD);
        setCustomItemId(stack, "hay_helmet");
        return stack;
    }
    public static ItemStack createHayChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyHayStats(stack, "Chestplate", 2, 0, EquipmentSlot.CHEST);
        setCustomItemId(stack, "hay_chestplate");
        return stack;
    }
    public static ItemStack createHayLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyHayStats(stack, "Leggings", 2, 0, EquipmentSlot.LEGS);
        setCustomItemId(stack, "hay_leggings");
        return stack;
    }
    public static ItemStack createHayBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyHayStats(stack, "Boots", 1, 0, EquipmentSlot.FEET);
        setCustomItemId(stack, "hay_boots");
        return stack;
    }
    private static void applyHayStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        if (!stack.is(Items.PLAYER_HEAD)) stack.set(DataComponents.DYED_COLOR, new DyedItemColor(HAY_COLOR));
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Hay " + pieceName).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, HAY_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "hay_" + pieceName.toLowerCase() + "_armor"), armorValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "hay_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("🛡 Armor: +" + armorValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal("💎 Toughness: +" + toughnessValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.LIGHT_PURPLE, "hay");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Soft but effective").withStyle(ChatFormatting.DARK_GRAY));
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // HONEYCOMB ARMOR - Beekeeper's suit
    public static ItemStack createHoneycombHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyHoneycombStats(stack, "Helmet", 2, 1, EquipmentSlot.HEAD);
        setCustomItemId(stack, "honeycomb_helmet");
        return stack;
    }
    public static ItemStack createHoneycombChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyHoneycombStats(stack, "Chestplate", 4, 1, EquipmentSlot.CHEST);
        setCustomItemId(stack, "honeycomb_chestplate");
        return stack;
    }
    public static ItemStack createHoneycombLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyHoneycombStats(stack, "Leggings", 3, 1, EquipmentSlot.LEGS);
        setCustomItemId(stack, "honeycomb_leggings");
        return stack;
    }
    public static ItemStack createHoneycombBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyHoneycombStats(stack, "Boots", 2, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "honeycomb_boots");
        return stack;
    }
    private static void applyHoneycombStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        if (!stack.is(Items.PLAYER_HEAD)) stack.set(DataComponents.DYED_COLOR, new DyedItemColor(HONEYCOMB_COLOR));
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Honeycomb " + pieceName).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, HONEYCOMB_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "honeycomb_" + pieceName.toLowerCase() + "_armor"), armorValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "honeycomb_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("🛡 Armor: +" + armorValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal("💎 Toughness: +" + toughnessValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.LIGHT_PURPLE, "honeycomb");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Sweet protection").withStyle(ChatFormatting.DARK_GRAY));
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // NETHER BRICK ARMOR - Fortress guard
    public static ItemStack createNetherBrickHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyNetherBrickStats(stack, "Helmet", 3, 3, EquipmentSlot.HEAD);
        setCustomItemId(stack, "nether_brick_helmet");
        return stack;
    }
    public static ItemStack createNetherBrickChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyNetherBrickStats(stack, "Chestplate", 6, 3, EquipmentSlot.CHEST);
        setCustomItemId(stack, "nether_brick_chestplate");
        return stack;
    }
    public static ItemStack createNetherBrickLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyNetherBrickStats(stack, "Leggings", 5, 3, EquipmentSlot.LEGS);
        setCustomItemId(stack, "nether_brick_leggings");
        return stack;
    }
    public static ItemStack createNetherBrickBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyNetherBrickStats(stack, "Boots", 3, 3, EquipmentSlot.FEET);
        setCustomItemId(stack, "nether_brick_boots");
        return stack;
    }
    private static void applyNetherBrickStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        if (!stack.is(Items.PLAYER_HEAD)) stack.set(DataComponents.DYED_COLOR, new DyedItemColor(NETHER_BRICK_COLOR));
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Nether Brick " + pieceName).withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, NETHER_BRICK_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "nether_brick_" + pieceName.toLowerCase() + "_armor"), armorValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "nether_brick_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_RED));
        lore.add(Component.literal("🛡 Armor: +" + armorValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal("💎 Toughness: +" + toughnessValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.LIGHT_PURPLE, "nether_brick");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Forged in flames").withStyle(ChatFormatting.DARK_GRAY));
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // PURPUR ARMOR - End city elegant
    public static ItemStack createPurpurHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyPlayerHeadTexture(stack, PURPUR_HEAD);
        applyPurpurStats(stack, "Helmet", 3, 3, EquipmentSlot.HEAD);
        setCustomItemId(stack, "purpur_helmet");
        return stack;
    }
    public static ItemStack createPurpurChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyPurpurStats(stack, "Chestplate", 6, 3, EquipmentSlot.CHEST);
        setCustomItemId(stack, "purpur_chestplate");
        return stack;
    }
    public static ItemStack createPurpurLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyPurpurStats(stack, "Leggings", 5, 3, EquipmentSlot.LEGS);
        setCustomItemId(stack, "purpur_leggings");
        return stack;
    }
    public static ItemStack createPurpurBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyPurpurStats(stack, "Boots", 3, 3, EquipmentSlot.FEET);
        setCustomItemId(stack, "purpur_boots");
        return stack;
    }
    private static void applyPurpurStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        if (!stack.is(Items.PLAYER_HEAD)) stack.set(DataComponents.DYED_COLOR, new DyedItemColor(PURPUR_COLOR));
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Purpur " + pieceName).withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, PURPUR_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "purpur_" + pieceName.toLowerCase() + "_armor"), armorValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "purpur_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.LIGHT_PURPLE));
        lore.add(Component.literal("🛡 Armor: +" + armorValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal("💎 Toughness: +" + toughnessValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.LIGHT_PURPLE, "purpur");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8End city elegance").withStyle(ChatFormatting.DARK_GRAY));
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // SCULK ARMOR - Deep dark sensor
    public static ItemStack createSculkHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyPlayerHeadTexture(stack, SCULK_HEAD);
        applySculkStats(stack, "Helmet", 4, 4, EquipmentSlot.HEAD);
        setCustomItemId(stack, "sculk_helmet");
        return stack;
    }
    public static ItemStack createSculkChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applySculkStats(stack, "Chestplate", 7, 4, EquipmentSlot.CHEST);
        setCustomItemId(stack, "sculk_chestplate");
        return stack;
    }
    public static ItemStack createSculkLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applySculkStats(stack, "Leggings", 6, 4, EquipmentSlot.LEGS);
        setCustomItemId(stack, "sculk_leggings");
        return stack;
    }
    public static ItemStack createSculkBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applySculkStats(stack, "Boots", 4, 4, EquipmentSlot.FEET);
        setCustomItemId(stack, "sculk_boots");
        return stack;
    }
    private static void applySculkStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        if (!stack.is(Items.PLAYER_HEAD)) stack.set(DataComponents.DYED_COLOR, new DyedItemColor(SCULK_COLOR));
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Sculk " + pieceName).withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, SCULK_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "sculk_" + pieceName.toLowerCase() + "_armor"), armorValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "sculk_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_AQUA));
        lore.add(Component.literal("🛡 Armor: +" + armorValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal("💎 Toughness: +" + toughnessValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.LIGHT_PURPLE, "sculk");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8From the deep dark").withStyle(ChatFormatting.DARK_GRAY));
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // SHROOMLIGHT ARMOR - Fungal light
    public static ItemStack createShroomlightHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyShroomlightStats(stack, "Helmet", 3, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "shroomlight_helmet");
        return stack;
    }
    public static ItemStack createShroomlightChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyShroomlightStats(stack, "Chestplate", 5, 2, EquipmentSlot.CHEST);
        setCustomItemId(stack, "shroomlight_chestplate");
        return stack;
    }
    public static ItemStack createShroomlightLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyShroomlightStats(stack, "Leggings", 4, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "shroomlight_leggings");
        return stack;
    }
    public static ItemStack createShroomlightBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyShroomlightStats(stack, "Boots", 2, 2, EquipmentSlot.FEET);
        setCustomItemId(stack, "shroomlight_boots");
        return stack;
    }
    private static void applyShroomlightStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        if (!stack.is(Items.PLAYER_HEAD)) stack.set(DataComponents.DYED_COLOR, new DyedItemColor(SHROOMLIGHT_COLOR));
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Shroomlight " + pieceName).withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, SHROOMLIGHT_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "shroomlight_" + pieceName.toLowerCase() + "_armor"), armorValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "shroomlight_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.LIGHT_PURPLE));
        lore.add(Component.literal("🛡 Armor: +" + armorValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal("💎 Toughness: +" + toughnessValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.LIGHT_PURPLE, "shroomlight");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Bioluminescent").withStyle(ChatFormatting.DARK_GRAY));
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // SMOOTH STONE ARMOR - Refined
    public static ItemStack createSmoothStoneHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyPlayerHeadTexture(stack, SMOOTH_STONE_HEAD);
        applySmoothStoneStats(stack, "Helmet", 3, 3, EquipmentSlot.HEAD);
        setCustomItemId(stack, "smooth_stone_helmet");
        return stack;
    }
    public static ItemStack createSmoothStoneChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applySmoothStoneStats(stack, "Chestplate", 6, 3, EquipmentSlot.CHEST);
        setCustomItemId(stack, "smooth_stone_chestplate");
        return stack;
    }
    public static ItemStack createSmoothStoneLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applySmoothStoneStats(stack, "Leggings", 5, 3, EquipmentSlot.LEGS);
        setCustomItemId(stack, "smooth_stone_leggings");
        return stack;
    }
    public static ItemStack createSmoothStoneBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applySmoothStoneStats(stack, "Boots", 3, 3, EquipmentSlot.FEET);
        setCustomItemId(stack, "smooth_stone_boots");
        return stack;
    }
    private static void applySmoothStoneStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        if (!stack.is(Items.PLAYER_HEAD)) stack.set(DataComponents.DYED_COLOR, new DyedItemColor(SMOOTH_STONE_COLOR));
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Smooth Stone " + pieceName).withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, SMOOTH_STONE_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "smooth_stone_" + pieceName.toLowerCase() + "_armor"), armorValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "smooth_stone_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("🛡 Armor: +" + armorValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal("💎 Toughness: +" + toughnessValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.LIGHT_PURPLE, "smooth_stone");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Refined and polished").withStyle(ChatFormatting.DARK_GRAY));
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // SNOW ARMOR - Frozen
    public static ItemStack createSnowHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyPlayerHeadTexture(stack, SNOW_HEAD);
        applySnowStats(stack, "Helmet", 1, 0, EquipmentSlot.HEAD);
        setCustomItemId(stack, "snow_helmet");
        return stack;
    }
    public static ItemStack createSnowChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applySnowStats(stack, "Chestplate", 2, 0, EquipmentSlot.CHEST);
        setCustomItemId(stack, "snow_chestplate");
        return stack;
    }
    public static ItemStack createSnowLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applySnowStats(stack, "Leggings", 2, 0, EquipmentSlot.LEGS);
        setCustomItemId(stack, "snow_leggings");
        return stack;
    }
    public static ItemStack createSnowBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applySnowStats(stack, "Boots", 1, 0, EquipmentSlot.FEET);
        setCustomItemId(stack, "snow_boots");
        return stack;
    }
    private static void applySnowStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        if (!stack.is(Items.PLAYER_HEAD)) stack.set(DataComponents.DYED_COLOR, new DyedItemColor(SNOW_COLOR));
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Snow " + pieceName).withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, SNOW_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "snow_" + pieceName.toLowerCase() + "_armor"), armorValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "snow_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal("🛡 Armor: +" + armorValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal("💎 Toughness: +" + toughnessValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.LIGHT_PURPLE, "snow");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Cold to the touch").withStyle(ChatFormatting.DARK_GRAY));
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // SOUL SOIL ARMOR - Fire immunity
    public static ItemStack createSoulSoilHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyPlayerHeadTexture(stack, SOUL_SOIL_HEAD);
        applySoulSoilStats(stack, "Helmet", 3, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "soul_soil_helmet");
        return stack;
    }
    public static ItemStack createSoulSoilChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applySoulSoilStats(stack, "Chestplate", 5, 2, EquipmentSlot.CHEST);
        setCustomItemId(stack, "soul_soil_chestplate");
        return stack;
    }
    public static ItemStack createSoulSoilLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applySoulSoilStats(stack, "Leggings", 4, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "soul_soil_leggings");
        return stack;
    }
    public static ItemStack createSoulSoilBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applySoulSoilStats(stack, "Boots", 2, 2, EquipmentSlot.FEET);
        setCustomItemId(stack, "soul_soil_boots");
        return stack;
    }
    private static void applySoulSoilStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        if (!stack.is(Items.PLAYER_HEAD)) stack.set(DataComponents.DYED_COLOR, new DyedItemColor(SOUL_SOIL_COLOR));
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Soul Soil " + pieceName).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, SOUL_SOIL_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "soul_soil_" + pieceName.toLowerCase() + "_armor"), armorValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "soul_soil_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("🛡 Armor: +" + armorValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal("💎 Toughness: +" + toughnessValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.LIGHT_PURPLE, "soul_soil");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Soul powered").withStyle(ChatFormatting.DARK_GRAY));
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // SPONGE ARMOR - Water absorption
    public static ItemStack createSpongeHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyPlayerHeadTexture(stack, SPONGE_HEAD);
        applySpongeStats(stack, "Helmet", 2, 1, EquipmentSlot.HEAD);
        setCustomItemId(stack, "sponge_helmet");
        return stack;
    }
    public static ItemStack createSpongeChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applySpongeStats(stack, "Chestplate", 4, 1, EquipmentSlot.CHEST);
        setCustomItemId(stack, "sponge_chestplate");
        return stack;
    }
    public static ItemStack createSpongeLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applySpongeStats(stack, "Leggings", 3, 1, EquipmentSlot.LEGS);
        setCustomItemId(stack, "sponge_leggings");
        return stack;
    }
    public static ItemStack createSpongeBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applySpongeStats(stack, "Boots", 2, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "sponge_boots");
        return stack;
    }
    private static void applySpongeStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        if (!stack.is(Items.PLAYER_HEAD)) stack.set(DataComponents.DYED_COLOR, new DyedItemColor(SPONGE_COLOR));
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Sponge " + pieceName).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, SPONGE_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "sponge_" + pieceName.toLowerCase() + "_armor"), armorValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "sponge_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("🛡 Armor: +" + armorValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal("💎 Toughness: +" + toughnessValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.LIGHT_PURPLE, "sponge");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Absorbent protection").withStyle(ChatFormatting.DARK_GRAY));
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // HELPER METHODS
    // ============================================================
    
    private static void applyPlayerHeadTexture(ItemStack stack, String textureValue) {
        // Create a random UUID for the skull owner
        java.util.UUID uuid = java.util.UUID.randomUUID();
        
        // Build the NBT structure for the player head texture
        CompoundTag nbt = new CompoundTag();
        CompoundTag skullOwner = new CompoundTag();
        CompoundTag properties = new CompoundTag();
        CompoundTag textures = new CompoundTag();
        
        textures.putString("Value", textureValue);
        properties.put("textures", textures);
        skullOwner.put("properties", properties);
        skullOwner.putString("Id", uuid.toString());
        nbt.put("SkullOwner", skullOwner);
        
        // Apply the NBT to the item
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
    }
    
    private static void setCustomItemId(ItemStack stack, String id) {
        CompoundTag nbt = stack.get(DataComponents.CUSTOM_DATA) != null 
                ? stack.get(DataComponents.CUSTOM_DATA).copyTag() 
                : new CompoundTag();
        nbt.putString("block_armor_id", id);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
        SlayerItems.setCustomItemId(stack, id);
    }
    
    public static String getBlockArmorId(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        CustomData nbt = stack.get(DataComponents.CUSTOM_DATA);
        if (nbt == null) return null;
        CompoundTag tag = nbt.copyTag();
        if (!tag.contains("block_armor_id")) return null;
        String id = tag.getStringOr("block_armor_id", "");
        return id.isEmpty() ? null : id;
    }
    
    public static boolean isBlockArmor(ItemStack stack) {
        return getBlockArmorId(stack) != null;
    }
    
    public static int countBlockArmorPieces(Player player, String prefix) {
        int count = 0;
        String id;
        id = getBlockArmorId(player.getItemBySlot(EquipmentSlot.HEAD));
        if (id != null && id.startsWith(prefix)) count++;
        id = getBlockArmorId(player.getItemBySlot(EquipmentSlot.CHEST));
        if (id != null && id.startsWith(prefix)) count++;
        id = getBlockArmorId(player.getItemBySlot(EquipmentSlot.LEGS));
        if (id != null && id.startsWith(prefix)) count++;
        id = getBlockArmorId(player.getItemBySlot(EquipmentSlot.FEET));
        if (id != null && id.startsWith(prefix)) count++;
        return count;
    }
    
    public static boolean hasFullBlockArmorSet(Player player, String prefix) {
        return countBlockArmorPieces(player, prefix) == 4;
    }

    /** Any dyed block-armor piece (custom block set), excluding non-block gear. */
    public static int countAnyBlockArmorPieces(Player player) {
        int n = 0;
        if (isBlockArmor(player.getItemBySlot(EquipmentSlot.HEAD))) n++;
        if (isBlockArmor(player.getItemBySlot(EquipmentSlot.CHEST))) n++;
        if (isBlockArmor(player.getItemBySlot(EquipmentSlot.LEGS))) n++;
        if (isBlockArmor(player.getItemBySlot(EquipmentSlot.FEET))) n++;
        return n;
    }
    
    private static EquipmentSlotGroup getSlotForPiece(String pieceName) {
        return switch (pieceName) {
            case "Helmet" -> EquipmentSlotGroup.HEAD;
            case "Chestplate" -> EquipmentSlotGroup.CHEST;
            case "Leggings" -> EquipmentSlotGroup.LEGS;
            case "Boots" -> EquipmentSlotGroup.FEET;
            default -> EquipmentSlotGroup.ANY;
        };
    }
    
    // ============================================================
    // GIVE ARMOR SETS
    // ============================================================
    
    public static void giveGlassSet(ServerPlayer player) {
        giveItem(player, createGlassHelmet());
        giveItem(player, createGlassChestplate());
        giveItem(player, createGlassLeggings());
        giveItem(player, createGlassBoots());
        player.sendSystemMessage(Component.literal("§b✔ Received full Glass armor set!"), false);
    }
    
    public static void giveObsidianSet(ServerPlayer player) {
        giveItem(player, createObsidianHelmet());
        giveItem(player, createObsidianChestplate());
        giveItem(player, createObsidianLeggings());
        giveItem(player, createObsidianBoots());
        player.sendSystemMessage(Component.literal("§5✔ Received full Obsidian armor set!"), false);
    }
    
    public static void giveQuartzSet(ServerPlayer player) {
        giveItem(player, createQuartzHelmet());
        giveItem(player, createQuartzChestplate());
        giveItem(player, createQuartzLeggings());
        giveItem(player, createQuartzBoots());
        player.sendSystemMessage(Component.literal("§f✔ Received full Quartz armor set!"), false);
    }
    
    public static void giveGlowstoneSet(ServerPlayer player) {
        giveItem(player, createGlowstoneHelmet());
        giveItem(player, createGlowstoneChestplate());
        giveItem(player, createGlowstoneLeggings());
        giveItem(player, createGlowstoneBoots());
        player.sendSystemMessage(Component.literal("§e✔ Received full Glowstone armor set!"), false);
    }
    
    public static void giveRedstoneSet(ServerPlayer player) {
        giveItem(player, createRedstoneHelmet());
        giveItem(player, createRedstoneChestplate());
        giveItem(player, createRedstoneLeggings());
        giveItem(player, createRedstoneBoots());
        player.sendSystemMessage(Component.literal("§c✔ Received full Redstone armor set!"), false);
    }
    
    public static void giveNetherrackSet(ServerPlayer player) {
        giveItem(player, createNetherrackHelmet());
        giveItem(player, createNetherrackChestplate());
        giveItem(player, createNetherrackLeggings());
        giveItem(player, createNetherrackBoots());
        player.sendSystemMessage(Component.literal("§4✔ Received full Netherrack armor set!"), false);
    }
    
    public static void giveEndstoneSet(ServerPlayer player) {
        giveItem(player, createEndstoneHelmet());
        giveItem(player, createEndstoneChestplate());
        giveItem(player, createEndstoneLeggings());
        giveItem(player, createEndstoneBoots());
        player.sendSystemMessage(Component.literal("§d✔ Received full End Stone armor set!"), false);
    }
    
    public static void giveIceSet(ServerPlayer player) {
        giveItem(player, createIceHelmet());
        giveItem(player, createIceChestplate());
        giveItem(player, createIceLeggings());
        giveItem(player, createIceBoots());
        player.sendSystemMessage(Component.literal("§b✔ Received full Packed Ice armor set!"), false);
    }
    
    public static void givePrismarineSet(ServerPlayer player) {
        giveItem(player, createPrismarineHelmet());
        giveItem(player, createPrismarineChestplate());
        giveItem(player, createPrismarineLeggings());
        giveItem(player, createPrismarineBoots());
        player.sendSystemMessage(Component.literal("§3✔ Received full Prismarine armor set!"), false);
    }
    
    public static void giveTerracottaSet(ServerPlayer player) {
        giveItem(player, createTerracottaHelmet());
        giveItem(player, createTerracottaChestplate());
        giveItem(player, createTerracottaLeggings());
        giveItem(player, createTerracottaBoots());
        player.sendSystemMessage(Component.literal("§6✔ Received full Terracotta armor set!"), false);
    }
    
    public static void giveMossySet(ServerPlayer player) {
        giveItem(player, createMossyHelmet());
        giveItem(player, createMossyChestplate());
        giveItem(player, createMossyLeggings());
        giveItem(player, createMossyBoots());
        player.sendSystemMessage(Component.literal("§2✔ Received full Mossy Stone armor set!"), false);
    }
    
    public static void giveSoulSandSet(ServerPlayer player) {
        giveItem(player, createSoulSandHelmet());
        giveItem(player, createSoulSandChestplate());
        giveItem(player, createSoulSandLeggings());
        giveItem(player, createSoulSandBoots());
        player.sendSystemMessage(Component.literal("§8✔ Received full Soul Sand armor set!"), false);
    }
    
    public static void giveMagmaSet(ServerPlayer player) {
        giveItem(player, createMagmaHelmet());
        giveItem(player, createMagmaChestplate());
        giveItem(player, createMagmaLeggings());
        giveItem(player, createMagmaBoots());
        player.sendSystemMessage(Component.literal("§4✔ Received full Magma armor set!"), false);
    }
    
    public static void giveSandstoneSet(ServerPlayer player) {
        giveItem(player, createSandstoneHelmet());
        giveItem(player, createSandstoneChestplate());
        giveItem(player, createSandstoneLeggings());
        giveItem(player, createSandstoneBoots());
        player.sendSystemMessage(Component.literal("§e✔ Received full Sandstone armor set!"), false);
    }
    
    public static void giveAmethystSet(ServerPlayer player) {
        giveItem(player, createAmethystHelmet());
        giveItem(player, createAmethystChestplate());
        giveItem(player, createAmethystLeggings());
        giveItem(player, createAmethystBoots());
        player.sendSystemMessage(Component.literal("§5✔ Received full Amethyst armor set!"), false);
    }
    
    public static void giveCoalSet(ServerPlayer player) {
        giveItem(player, createCoalHelmet());
        giveItem(player, createCoalChestplate());
        giveItem(player, createCoalLeggings());
        giveItem(player, createCoalBoots());
        player.sendSystemMessage(Component.literal("§8✔ Received full Coal armor set!"), false);
    }
    
    // ============================================================
    // CRIMSON STEM ARMOR - Nether forest crimson
    // ============================================================
    
    public static final int CRIMSON_STEM_DURABILITY = 280;
    public static final int CRIMSON_STEM_COLOR = 0x8B3A3A; // Dark red
    
    public static ItemStack createCrimsonStemHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyCrimsonStemStats(stack, "Helmet", 2, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "crimson_stem_helmet");
        return stack;
    }
    
    public static ItemStack createCrimsonStemChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyCrimsonStemStats(stack, "Chestplate", 5, 3, EquipmentSlot.CHEST);
        setCustomItemId(stack, "crimson_stem_chestplate");
        return stack;
    }
    
    public static ItemStack createCrimsonStemLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyCrimsonStemStats(stack, "Leggings", 3, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "crimson_stem_leggings");
        return stack;
    }
    
    public static ItemStack createCrimsonStemBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyCrimsonStemStats(stack, "Boots", 2, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "crimson_stem_boots");
        return stack;
    }
    
    private static void applyCrimsonStemStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(CRIMSON_STEM_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Crimson Stem " + pieceName).withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, CRIMSON_STEM_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "crimson_stem_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "crimson_stem_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_RED));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.DARK_RED)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.DARK_RED)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Medium").withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.DARK_PURPLE, "crimson_stem");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8From the crimson forest").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }
    
    // ============================================================
    // CRYING OBSIDIAN ARMOR - Purple weeping obsidian
    // ============================================================
    
    public static final int CRYING_OBSIDIAN_DURABILITY = 1800;
    public static final int CRYING_OBSIDIAN_COLOR = 0x4A1A6B; // Deep purple
    
    public static ItemStack createCryingObsidianHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyCryingObsidianStats(stack, "Helmet", 3, 5, EquipmentSlot.HEAD);
        setCustomItemId(stack, "crying_obsidian_helmet");
        return stack;
    }
    
    public static ItemStack createCryingObsidianChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyCryingObsidianStats(stack, "Chestplate", 7, 6, EquipmentSlot.CHEST);
        setCustomItemId(stack, "crying_obsidian_chestplate");
        return stack;
    }
    
    public static ItemStack createCryingObsidianLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyCryingObsidianStats(stack, "Leggings", 5, 5, EquipmentSlot.LEGS);
        setCustomItemId(stack, "crying_obsidian_leggings");
        return stack;
    }
    
    public static ItemStack createCryingObsidianBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyCryingObsidianStats(stack, "Boots", 3, 4, EquipmentSlot.FEET);
        setCustomItemId(stack, "crying_obsidian_boots");
        return stack;
    }
    
    private static void applyCryingObsidianStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(CRYING_OBSIDIAN_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Crying Obsidian " + pieceName).withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, CRYING_OBSIDIAN_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "crying_obsidian_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "crying_obsidian_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_PURPLE));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.DARK_PURPLE)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.DARK_PURPLE)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Very High").withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.DARK_PURPLE, "crying_obsidian");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Weeping obsidian tears").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }
    
    // ============================================================
    // GILDED BLACKSTONE ARMOR - Gold-tinted blackstone
    // ============================================================
    
    public static final int GILDED_BLACKSTONE_DURABILITY = 350;
    public static final int GILDED_BLACKSTONE_COLOR = 0x8B6914; // Gold-black
    
    public static ItemStack createGildedBlackstoneHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyGildedBlackstoneStats(stack, "Helmet", 3, 3, EquipmentSlot.HEAD);
        setCustomItemId(stack, "gilded_blackstone_helmet");
        return stack;
    }
    
    public static ItemStack createGildedBlackstoneChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyGildedBlackstoneStats(stack, "Chestplate", 6, 4, EquipmentSlot.CHEST);
        setCustomItemId(stack, "gilded_blackstone_chestplate");
        return stack;
    }
    
    public static ItemStack createGildedBlackstoneLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyGildedBlackstoneStats(stack, "Leggings", 4, 3, EquipmentSlot.LEGS);
        setCustomItemId(stack, "gilded_blackstone_leggings");
        return stack;
    }
    
    public static ItemStack createGildedBlackstoneBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyGildedBlackstoneStats(stack, "Boots", 3, 2, EquipmentSlot.FEET);
        setCustomItemId(stack, "gilded_blackstone_boots");
        return stack;
    }
    
    private static void applyGildedBlackstoneStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(GILDED_BLACKSTONE_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Gilded Blackstone " + pieceName).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, GILDED_BLACKSTONE_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "gilded_blackstone_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "gilded_blackstone_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.GOLD)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.GOLD)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Medium").withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.DARK_PURPLE, "gilded_blackstone");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Gold-touched blackstone").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }
    
    // ============================================================
    // GRANITE ARMOR - Pink-gray speckled stone
    // ============================================================
    
    public static final int GRANITE_DURABILITY = 380;
    public static final int GRANITE_COLOR = 0x9B6B5B; // Pinkish brown
    
    public static ItemStack createGraniteHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyGraniteStats(stack, "Helmet", 3, 3, EquipmentSlot.HEAD);
        setCustomItemId(stack, "granite_helmet");
        return stack;
    }
    
    public static ItemStack createGraniteChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyGraniteStats(stack, "Chestplate", 6, 4, EquipmentSlot.CHEST);
        setCustomItemId(stack, "granite_chestplate");
        return stack;
    }
    
    public static ItemStack createGraniteLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyGraniteStats(stack, "Leggings", 4, 3, EquipmentSlot.LEGS);
        setCustomItemId(stack, "granite_leggings");
        return stack;
    }
    
    public static ItemStack createGraniteBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyGraniteStats(stack, "Boots", 3, 2, EquipmentSlot.FEET);
        setCustomItemId(stack, "granite_boots");
        return stack;
    }
    
    private static void applyGraniteStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(GRANITE_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Granite " + pieceName).withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, GRANITE_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "granite_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "granite_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.RED)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.RED)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Medium").withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.DARK_PURPLE, "granite");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Pink speckled granite").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }
    
    // ============================================================
    // DIORITE ARMOR - White-gray speckled stone
    // ============================================================
    
    public static final int DIORITE_DURABILITY = 350;
    public static final int DIORITE_COLOR = 0xC4C4C4; // Light gray-white
    
    public static ItemStack createDioriteHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyDioriteStats(stack, "Helmet", 2, 3, EquipmentSlot.HEAD);
        setCustomItemId(stack, "diorite_helmet");
        return stack;
    }
    
    public static ItemStack createDioriteChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyDioriteStats(stack, "Chestplate", 5, 4, EquipmentSlot.CHEST);
        setCustomItemId(stack, "diorite_chestplate");
        return stack;
    }
    
    public static ItemStack createDioriteLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyDioriteStats(stack, "Leggings", 3, 3, EquipmentSlot.LEGS);
        setCustomItemId(stack, "diorite_leggings");
        return stack;
    }
    
    public static ItemStack createDioriteBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyDioriteStats(stack, "Boots", 2, 2, EquipmentSlot.FEET);
        setCustomItemId(stack, "diorite_boots");
        return stack;
    }
    
    private static void applyDioriteStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(DIORITE_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Diorite " + pieceName).withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, DIORITE_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "diorite_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "diorite_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.GRAY)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.GRAY)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Medium").withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.DARK_PURPLE, "diorite");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8White speckled diorite").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }
    
    private static void giveItem(ServerPlayer player, ItemStack stack) {
        if (!player.addItem(stack)) {
            player.drop(stack, false);
        }
    }
    
    // ============================================================
    // ANDESITE ARMOR - Gray speckled stone
    // ============================================================
    
    public static final int ANDESITE_DURABILITY = 360;
    public static final int ANDESITE_COLOR = 0x7A7A7A; // Gray
    
    public static ItemStack createAndesiteHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyAndesiteStats(stack, "Helmet", 3, 3, EquipmentSlot.HEAD);
        setCustomItemId(stack, "andesite_helmet");
        return stack;
    }
    
    public static ItemStack createAndesiteChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyAndesiteStats(stack, "Chestplate", 6, 4, EquipmentSlot.CHEST);
        setCustomItemId(stack, "andesite_chestplate");
        return stack;
    }
    
    public static ItemStack createAndesiteLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyAndesiteStats(stack, "Leggings", 4, 3, EquipmentSlot.LEGS);
        setCustomItemId(stack, "andesite_leggings");
        return stack;
    }
    
    public static ItemStack createAndesiteBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyAndesiteStats(stack, "Boots", 3, 2, EquipmentSlot.FEET);
        setCustomItemId(stack, "andesite_boots");
        return stack;
    }
    
    private static void applyAndesiteStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(ANDESITE_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Andesite " + pieceName).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, ANDESITE_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "andesite_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "andesite_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.DARK_GRAY)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.DARK_GRAY)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Medium").withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.DARK_PURPLE, "andesite");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Gray speckled andesite").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }
    
    // ============================================================
    // POLISHED GRANITE ARMOR - Polished pink-gray stone
    // ============================================================
    
    public static final int POLISHED_GRANITE_DURABILITY = 400;
    public static final int POLISHED_GRANITE_COLOR = 0xB07B6B; // Polished pink
    
    public static ItemStack createPolishedGraniteHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyPolishedGraniteStats(stack, "Helmet", 3, 3, EquipmentSlot.HEAD);
        setCustomItemId(stack, "polished_granite_helmet");
        return stack;
    }
    
    public static ItemStack createPolishedGraniteChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyPolishedGraniteStats(stack, "Chestplate", 6, 4, EquipmentSlot.CHEST);
        setCustomItemId(stack, "polished_granite_chestplate");
        return stack;
    }
    
    public static ItemStack createPolishedGraniteLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyPolishedGraniteStats(stack, "Leggings", 4, 3, EquipmentSlot.LEGS);
        setCustomItemId(stack, "polished_granite_leggings");
        return stack;
    }
    
    public static ItemStack createPolishedGraniteBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyPolishedGraniteStats(stack, "Boots", 3, 2, EquipmentSlot.FEET);
        setCustomItemId(stack, "polished_granite_boots");
        return stack;
    }
    
    private static void applyPolishedGraniteStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(POLISHED_GRANITE_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Polished Granite " + pieceName).withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, POLISHED_GRANITE_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "polished_granite_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "polished_granite_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.RED)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.RED)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Medium").withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.DARK_PURPLE, "polished_granite");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Polished granite glory").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }
    
    // ============================================================
    // POLISHED DIORITE ARMOR - Polished white-gray stone
    // ============================================================
    
    public static final int POLISHED_DIORITE_DURABILITY = 380;
    public static final int POLISHED_DIORITE_COLOR = 0xD4D4D4; // Polished white
    
    public static ItemStack createPolishedDioriteHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyPolishedDioriteStats(stack, "Helmet", 3, 3, EquipmentSlot.HEAD);
        setCustomItemId(stack, "polished_diorite_helmet");
        return stack;
    }
    
    public static ItemStack createPolishedDioriteChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyPolishedDioriteStats(stack, "Chestplate", 6, 4, EquipmentSlot.CHEST);
        setCustomItemId(stack, "polished_diorite_chestplate");
        return stack;
    }
    
    public static ItemStack createPolishedDioriteLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyPolishedDioriteStats(stack, "Leggings", 4, 3, EquipmentSlot.LEGS);
        setCustomItemId(stack, "polished_diorite_leggings");
        return stack;
    }
    
    public static ItemStack createPolishedDioriteBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyPolishedDioriteStats(stack, "Boots", 3, 2, EquipmentSlot.FEET);
        setCustomItemId(stack, "polished_diorite_boots");
        return stack;
    }
    
    private static void applyPolishedDioriteStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(POLISHED_DIORITE_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Polished Diorite " + pieceName).withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, POLISHED_DIORITE_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "polished_diorite_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "polished_diorite_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.WHITE)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.WHITE)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Medium").withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.DARK_PURPLE, "polished_diorite");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Polished diorite elegance").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }
    
    // ============================================================
    // POLISHED ANDESITE ARMOR - Polished gray stone
    // ============================================================
    
    public static final int POLISHED_ANDESITE_DURABILITY = 390;
    public static final int POLISHED_ANDESITE_COLOR = 0x8A8A8A; // Polished gray
    
    public static ItemStack createPolishedAndesiteHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyPolishedAndesiteStats(stack, "Helmet", 3, 3, EquipmentSlot.HEAD);
        setCustomItemId(stack, "polished_andesite_helmet");
        return stack;
    }
    
    public static ItemStack createPolishedAndesiteChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyPolishedAndesiteStats(stack, "Chestplate", 6, 4, EquipmentSlot.CHEST);
        setCustomItemId(stack, "polished_andesite_chestplate");
        return stack;
    }
    
    public static ItemStack createPolishedAndesiteLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyPolishedAndesiteStats(stack, "Leggings", 4, 3, EquipmentSlot.LEGS);
        setCustomItemId(stack, "polished_andesite_leggings");
        return stack;
    }
    
    public static ItemStack createPolishedAndesiteBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyPolishedAndesiteStats(stack, "Boots", 3, 2, EquipmentSlot.FEET);
        setCustomItemId(stack, "polished_andesite_boots");
        return stack;
    }
    
    private static void applyPolishedAndesiteStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(POLISHED_ANDESITE_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Polished Andesite " + pieceName).withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, POLISHED_ANDESITE_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "polished_andesite_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "polished_andesite_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.GRAY)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.GRAY)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Medium").withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.DARK_PURPLE, "polished_andesite");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Polished andesite grace").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }
    
    // ============================================================
    // PACKED ICE ARMOR - Frozen ice blocks
    // ============================================================
    
    public static final int PACKED_ICE_DURABILITY = 200;
    public static final int PACKED_ICE_COLOR = 0xA8D8E8; // Light icy blue
    
    public static ItemStack createPackedIceHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyPackedIceStats(stack, "Helmet", 2, 3, EquipmentSlot.HEAD);
        setCustomItemId(stack, "packed_ice_helmet");
        return stack;
    }
    
    public static ItemStack createPackedIceChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyPackedIceStats(stack, "Chestplate", 4, 4, EquipmentSlot.CHEST);
        setCustomItemId(stack, "packed_ice_chestplate");
        return stack;
    }
    
    public static ItemStack createPackedIceLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyPackedIceStats(stack, "Leggings", 3, 3, EquipmentSlot.LEGS);
        setCustomItemId(stack, "packed_ice_leggings");
        return stack;
    }
    
    public static ItemStack createPackedIceBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyPackedIceStats(stack, "Boots", 2, 2, EquipmentSlot.FEET);
        setCustomItemId(stack, "packed_ice_boots");
        return stack;
    }
    
    private static void applyPackedIceStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(PACKED_ICE_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Packed Ice " + pieceName).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, PACKED_ICE_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "packed_ice_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "packed_ice_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.AQUA));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.AQUA)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.AQUA)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Low").withStyle(ChatFormatting.RED)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.DARK_PURPLE, "packed_ice");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Packed ice cold").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }
    
    // ============================================================
    // BLUE ICE ARMOR - Dense frozen ice
    // ============================================================
    
    public static final int BLUE_ICE_DURABILITY = 350;
    public static final int BLUE_ICE_COLOR = 0x5B9BD5; // Deep icy blue
    
    public static ItemStack createBlueIceHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyBlueIceStats(stack, "Helmet", 2, 4, EquipmentSlot.HEAD);
        setCustomItemId(stack, "blue_ice_helmet");
        return stack;
    }
    
    public static ItemStack createBlueIceChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyBlueIceStats(stack, "Chestplate", 5, 5, EquipmentSlot.CHEST);
        setCustomItemId(stack, "blue_ice_chestplate");
        return stack;
    }
    
    public static ItemStack createBlueIceLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyBlueIceStats(stack, "Leggings", 3, 4, EquipmentSlot.LEGS);
        setCustomItemId(stack, "blue_ice_leggings");
        return stack;
    }
    
    public static ItemStack createBlueIceBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyBlueIceStats(stack, "Boots", 2, 3, EquipmentSlot.FEET);
        setCustomItemId(stack, "blue_ice_boots");
        return stack;
    }
    
    private static void applyBlueIceStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(BLUE_ICE_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Blue Ice " + pieceName).withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, BLUE_ICE_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "blue_ice_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "blue_ice_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.BLUE));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.BLUE)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.BLUE)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Medium").withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.DARK_PURPLE, "blue_ice");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Dense blue ice").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }
    
    // ============================================================
    // NETHER GOLD ORE ARMOR - Golden nether ore
    // ============================================================
    
    public static final int NETHER_GOLD_ORE_DURABILITY = 280;
    public static final int NETHER_GOLD_ORE_COLOR = 0xFFD700; // Gold
    
    public static ItemStack createNetherGoldOreHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyNetherGoldOreStats(stack, "Helmet", 2, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "nether_gold_ore_helmet");
        return stack;
    }
    
    public static ItemStack createNetherGoldOreChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyNetherGoldOreStats(stack, "Chestplate", 5, 3, EquipmentSlot.CHEST);
        setCustomItemId(stack, "nether_gold_ore_chestplate");
        return stack;
    }
    
    public static ItemStack createNetherGoldOreLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyNetherGoldOreStats(stack, "Leggings", 3, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "nether_gold_ore_leggings");
        return stack;
    }
    
    public static ItemStack createNetherGoldOreBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyNetherGoldOreStats(stack, "Boots", 2, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "nether_gold_ore_boots");
        return stack;
    }
    
    private static void applyNetherGoldOreStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(NETHER_GOLD_ORE_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Nether Gold Ore " + pieceName).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, NETHER_GOLD_ORE_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "nether_gold_ore_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "nether_gold_ore_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.GOLD)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.GOLD)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Medium").withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.DARK_PURPLE, "nether_gold_ore");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Golden nether ore").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }
    
    // ============================================================
    // DARK OAK LOG ARMOR - Dark forest wood
    // ============================================================
    
    public static final int DARK_OAK_LOG_DURABILITY = 320;
    public static final int DARK_OAK_LOG_COLOR = 0x3D2817; // Dark brown
    
    public static ItemStack createDarkOakLogHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyDarkOakLogStats(stack, "Helmet", 2, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "dark_oak_log_helmet");
        return stack;
    }
    
    public static ItemStack createDarkOakLogChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyDarkOakLogStats(stack, "Chestplate", 5, 3, EquipmentSlot.CHEST);
        setCustomItemId(stack, "dark_oak_log_chestplate");
        return stack;
    }
    
    public static ItemStack createDarkOakLogLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyDarkOakLogStats(stack, "Leggings", 3, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "dark_oak_log_leggings");
        return stack;
    }
    
    public static ItemStack createDarkOakLogBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyDarkOakLogStats(stack, "Boots", 2, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "dark_oak_log_boots");
        return stack;
    }
    
    private static void applyDarkOakLogStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(DARK_OAK_LOG_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Dark Oak Log " + pieceName).withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, DARK_OAK_LOG_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "dark_oak_log_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "dark_oak_log_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_RED));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.DARK_RED)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.DARK_RED)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Medium").withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.DARK_PURPLE, "dark_oak_log");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Dark oak forest").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }
    
    // ============================================================
    // JUNGLE LOG ARMOR - Tropical wood
    // ============================================================
    
    public static final int JUNGLE_LOG_DURABILITY = 300;
    public static final int JUNGLE_LOG_COLOR = 0x8B5A2B; // Jungle brown
    
    public static ItemStack createJungleLogHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyJungleLogStats(stack, "Helmet", 2, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "jungle_log_helmet");
        return stack;
    }
    
    public static ItemStack createJungleLogChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyJungleLogStats(stack, "Chestplate", 5, 3, EquipmentSlot.CHEST);
        setCustomItemId(stack, "jungle_log_chestplate");
        return stack;
    }
    
    public static ItemStack createJungleLogLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyJungleLogStats(stack, "Leggings", 3, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "jungle_log_leggings");
        return stack;
    }
    
    public static ItemStack createJungleLogBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyJungleLogStats(stack, "Boots", 2, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "jungle_log_boots");
        return stack;
    }
    
    private static void applyJungleLogStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(JUNGLE_LOG_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Jungle Log " + pieceName).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, JUNGLE_LOG_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "jungle_log_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "jungle_log_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Medium").withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.DARK_PURPLE, "jungle_log");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Tropical jungle wood").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }
    
    // ============================================================
    // ACACIA LOG ARMOR - Savanna wood
    // ============================================================
    
    public static final int ACACIA_LOG_DURABILITY = 290;
    public static final int ACACIA_LOG_COLOR = 0xD2691E; // Orange-brown
    
    public static ItemStack createAcaciaLogHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyAcaciaLogStats(stack, "Helmet", 2, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "acacia_log_helmet");
        return stack;
    }
    
    public static ItemStack createAcaciaLogChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyAcaciaLogStats(stack, "Chestplate", 5, 3, EquipmentSlot.CHEST);
        setCustomItemId(stack, "acacia_log_chestplate");
        return stack;
    }
    
    public static ItemStack createAcaciaLogLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyAcaciaLogStats(stack, "Leggings", 3, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "acacia_log_leggings");
        return stack;
    }
    
    public static ItemStack createAcaciaLogBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyAcaciaLogStats(stack, "Boots", 2, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "acacia_log_boots");
        return stack;
    }
    
    private static void applyAcaciaLogStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(ACACIA_LOG_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Acacia Log " + pieceName).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, ACACIA_LOG_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "acacia_log_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "acacia_log_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.GOLD)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.GOLD)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Medium").withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.DARK_PURPLE, "acacia_log");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Savanna acacia wood").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }
    
    // ============================================================
    // MANGROVE LOG ARMOR - Swamp wood
    // ============================================================
    
    public static final int MANGROVE_LOG_DURABILITY = 340;
    public static final int MANGROVE_LOG_COLOR = 0x6B3A3A; // Reddish brown
    
    public static ItemStack createMangroveLogHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyMangroveLogStats(stack, "Helmet", 2, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "mangrove_log_helmet");
        return stack;
    }
    
    public static ItemStack createMangroveLogChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyMangroveLogStats(stack, "Chestplate", 5, 3, EquipmentSlot.CHEST);
        setCustomItemId(stack, "mangrove_log_chestplate");
        return stack;
    }
    
    public static ItemStack createMangroveLogLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyMangroveLogStats(stack, "Leggings", 3, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "mangrove_log_leggings");
        return stack;
    }
    
    public static ItemStack createMangroveLogBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyMangroveLogStats(stack, "Boots", 2, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "mangrove_log_boots");
        return stack;
    }
    
    private static void applyMangroveLogStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(MANGROVE_LOG_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Mangrove Log " + pieceName).withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, MANGROVE_LOG_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "mangrove_log_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "mangrove_log_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_RED));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.DARK_RED)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.DARK_RED)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Medium").withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.DARK_PURPLE, "mangrove_log");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Swamp mangrove wood").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }
    
    // ============================================================
    // CHERRY LOG ARMOR - Pink blossom wood
    // ============================================================
    
    public static final int CHERRY_LOG_DURABILITY = 310;
    public static final int CHERRY_LOG_COLOR = 0xFFB7C5; // Pink
    
    public static ItemStack createCherryLogHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyCherryLogStats(stack, "Helmet", 2, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "cherry_log_helmet");
        return stack;
    }
    
    public static ItemStack createCherryLogChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyCherryLogStats(stack, "Chestplate", 5, 3, EquipmentSlot.CHEST);
        setCustomItemId(stack, "cherry_log_chestplate");
        return stack;
    }
    
    public static ItemStack createCherryLogLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyCherryLogStats(stack, "Leggings", 3, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "cherry_log_leggings");
        return stack;
    }
    
    public static ItemStack createCherryLogBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyCherryLogStats(stack, "Boots", 2, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "cherry_log_boots");
        return stack;
    }
    
    private static void applyCherryLogStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(CHERRY_LOG_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Cherry Log " + pieceName).withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, CHERRY_LOG_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "cherry_log_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "cherry_log_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.LIGHT_PURPLE));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.LIGHT_PURPLE)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.LIGHT_PURPLE)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Medium").withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.DARK_PURPLE, "cherry_log");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Pink cherry blossom").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }
    
    // ============================================================
    // BAMBOO BLOCK ARMOR - Bamboo scaffolding
    // ============================================================
    
    public static final int BAMBOO_BLOCK_DURABILITY = 180;
    public static final int BAMBOO_BLOCK_COLOR = 0xC8D96F; // Yellow-green
    
    public static ItemStack createBambooBlockHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyBambooBlockStats(stack, "Helmet", 1, 1, EquipmentSlot.HEAD);
        setCustomItemId(stack, "bamboo_block_helmet");
        return stack;
    }
    
    public static ItemStack createBambooBlockChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyBambooBlockStats(stack, "Chestplate", 3, 2, EquipmentSlot.CHEST);
        setCustomItemId(stack, "bamboo_block_chestplate");
        return stack;
    }
    
    public static ItemStack createBambooBlockLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyBambooBlockStats(stack, "Leggings", 2, 1, EquipmentSlot.LEGS);
        setCustomItemId(stack, "bamboo_block_leggings");
        return stack;
    }
    
    public static ItemStack createBambooBlockBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyBambooBlockStats(stack, "Boots", 1, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "bamboo_block_boots");
        return stack;
    }
    
    private static void applyBambooBlockStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(BAMBOO_BLOCK_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Bamboo Block " + pieceName).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, BAMBOO_BLOCK_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "bamboo_block_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "bamboo_block_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Low").withStyle(ChatFormatting.RED)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.DARK_PURPLE, "bamboo_block");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Light bamboo scaffolding").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }
    
    // ============================================================
    // TUFF ARMOR - Volcanic sediment
    // ============================================================
    
    public static final int TUFF_DURABILITY = 380;
    public static final int TUFF_COLOR = 0x6B5B4F; // Brown-gray
    
    public static ItemStack createTuffHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyTuffStats(stack, "Helmet", 3, 3, EquipmentSlot.HEAD);
        setCustomItemId(stack, "tuff_helmet");
        return stack;
    }
    
    public static ItemStack createTuffChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyTuffStats(stack, "Chestplate", 6, 4, EquipmentSlot.CHEST);
        setCustomItemId(stack, "tuff_chestplate");
        return stack;
    }
    
    public static ItemStack createTuffLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyTuffStats(stack, "Leggings", 4, 3, EquipmentSlot.LEGS);
        setCustomItemId(stack, "tuff_leggings");
        return stack;
    }
    
    public static ItemStack createTuffBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyTuffStats(stack, "Boots", 3, 2, EquipmentSlot.FEET);
        setCustomItemId(stack, "tuff_boots");
        return stack;
    }
    
    private static void applyTuffStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(TUFF_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Tuff " + pieceName).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, TUFF_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "tuff_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "tuff_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.DARK_GRAY)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.DARK_GRAY)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Medium").withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.DARK_PURPLE, "tuff");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Volcanic tuff stone").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }
    
    // ============================================================
    // POLISHED TUFF ARMOR - Smooth tuff
    // ============================================================
    
    public static final int POLISHED_TUFF_DURABILITY = 400;
    public static final int POLISHED_TUFF_COLOR = 0x7B6B5F; // Smooth brown-gray
    
    public static ItemStack createPolishedTuffHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyPolishedTuffStats(stack, "Helmet", 3, 3, EquipmentSlot.HEAD);
        setCustomItemId(stack, "polished_tuff_helmet");
        return stack;
    }
    
    public static ItemStack createPolishedTuffChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyPolishedTuffStats(stack, "Chestplate", 6, 4, EquipmentSlot.CHEST);
        setCustomItemId(stack, "polished_tuff_chestplate");
        return stack;
    }
    
    public static ItemStack createPolishedTuffLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyPolishedTuffStats(stack, "Leggings", 4, 3, EquipmentSlot.LEGS);
        setCustomItemId(stack, "polished_tuff_leggings");
        return stack;
    }
    
    public static ItemStack createPolishedTuffBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyPolishedTuffStats(stack, "Boots", 3, 2, EquipmentSlot.FEET);
        setCustomItemId(stack, "polished_tuff_boots");
        return stack;
    }
    
    private static void applyPolishedTuffStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(POLISHED_TUFF_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Polished Tuff " + pieceName).withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, POLISHED_TUFF_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "polished_tuff_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "polished_tuff_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.GRAY)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.GRAY)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Medium").withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.DARK_PURPLE, "polished_tuff");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Polished tuff elegance").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }
    
    // ============================================================
    // NETHER WART BLOCK ARMOR - Crimson fungus
    // ============================================================
    
    public static final int NETHER_WART_BLOCK_DURABILITY = 260;
    public static final int NETHER_WART_BLOCK_COLOR = 0x8B0000; // Dark red
    
    public static ItemStack createNetherWartBlockHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyNetherWartBlockStats(stack, "Helmet", 2, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "nether_wart_block_helmet");
        return stack;
    }
    
    public static ItemStack createNetherWartBlockChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyNetherWartBlockStats(stack, "Chestplate", 5, 3, EquipmentSlot.CHEST);
        setCustomItemId(stack, "nether_wart_block_chestplate");
        return stack;
    }
    
    public static ItemStack createNetherWartBlockLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyNetherWartBlockStats(stack, "Leggings", 3, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "nether_wart_block_leggings");
        return stack;
    }
    
    public static ItemStack createNetherWartBlockBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyNetherWartBlockStats(stack, "Boots", 2, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "nether_wart_block_boots");
        return stack;
    }
    
    private static void applyNetherWartBlockStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(NETHER_WART_BLOCK_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Nether Wart Block " + pieceName).withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, NETHER_WART_BLOCK_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "nether_wart_block_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "nether_wart_block_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_RED));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.DARK_RED)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.DARK_RED)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Medium").withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.DARK_PURPLE, "nether_wart_block");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Crimson nether wart").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }
    
    // ============================================================
    // WARPED WART BLOCK ARMOR - Warped fungus
    // ============================================================
    
    public static final int WARPED_WART_BLOCK_DURABILITY = 260;
    public static final int WARPED_WART_BLOCK_COLOR = 0x169E6B; // Teal-green
    
    public static ItemStack createWarpedWartBlockHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyWarpedWartBlockStats(stack, "Helmet", 2, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "warped_wart_block_helmet");
        return stack;
    }
    
    public static ItemStack createWarpedWartBlockChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyWarpedWartBlockStats(stack, "Chestplate", 5, 3, EquipmentSlot.CHEST);
        setCustomItemId(stack, "warped_wart_block_chestplate");
        return stack;
    }
    
    public static ItemStack createWarpedWartBlockLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyWarpedWartBlockStats(stack, "Leggings", 3, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "warped_wart_block_leggings");
        return stack;
    }
    
    public static ItemStack createWarpedWartBlockBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyWarpedWartBlockStats(stack, "Boots", 2, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "warped_wart_block_boots");
        return stack;
    }
    
    private static void applyWarpedWartBlockStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(WARPED_WART_BLOCK_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Warped Wart Block " + pieceName).withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, WARPED_WART_BLOCK_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "warped_wart_block_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "warped_wart_block_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_GREEN));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.DARK_GREEN)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.DARK_GREEN)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Medium").withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.DARK_PURPLE, "warped_wart_block");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Warped nether wart").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }
    
    // ============================================================
    // CHISELED DEEPSLATE ARMOR - Carved deepslate
    // ============================================================
    
    public static final int CHISELED_DEEPSLATE_DURABILITY = 420;
    public static final int CHISELED_DEEPSLATE_COLOR = 0x3A3A4A; // Dark blue-gray
    
    public static ItemStack createChiseledDeepslateHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyChiseledDeepslateStats(stack, "Helmet", 3, 4, EquipmentSlot.HEAD);
        setCustomItemId(stack, "chiseled_deepslate_helmet");
        return stack;
    }
    
    public static ItemStack createChiseledDeepslateChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyChiseledDeepslateStats(stack, "Chestplate", 7, 5, EquipmentSlot.CHEST);
        setCustomItemId(stack, "chiseled_deepslate_chestplate");
        return stack;
    }
    
    public static ItemStack createChiseledDeepslateLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyChiseledDeepslateStats(stack, "Leggings", 5, 4, EquipmentSlot.LEGS);
        setCustomItemId(stack, "chiseled_deepslate_leggings");
        return stack;
    }
    
    public static ItemStack createChiseledDeepslateBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyChiseledDeepslateStats(stack, "Boots", 3, 3, EquipmentSlot.FEET);
        setCustomItemId(stack, "chiseled_deepslate_boots");
        return stack;
    }
    
    private static void applyChiseledDeepslateStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(CHISELED_DEEPSLATE_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Chiseled Deepslate " + pieceName).withStyle(ChatFormatting.DARK_BLUE, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, CHISELED_DEEPSLATE_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "chiseled_deepslate_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "chiseled_deepslate_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_BLUE));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.DARK_BLUE)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.DARK_BLUE)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("High").withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.DARK_PURPLE, "chiseled_deepslate");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Carved deepslate runes").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }
    
    // ============================================================
    // REINFORCED DEEPSLATE ARMOR - Ultra durable deepslate
    // ============================================================
    
    public static final int REINFORCED_DEEPSLATE_DURABILITY = 1200;
    public static final int REINFORCED_DEEPSLATE_COLOR = 0x2A2A3A; // Deep dark
    
    public static ItemStack createReinforcedDeepslateHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyReinforcedDeepslateStats(stack, "Helmet", 4, 6, EquipmentSlot.HEAD);
        setCustomItemId(stack, "reinforced_deepslate_helmet");
        return stack;
    }
    
    public static ItemStack createReinforcedDeepslateChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyReinforcedDeepslateStats(stack, "Chestplate", 8, 7, EquipmentSlot.CHEST);
        setCustomItemId(stack, "reinforced_deepslate_chestplate");
        return stack;
    }
    
    public static ItemStack createReinforcedDeepslateLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyReinforcedDeepslateStats(stack, "Leggings", 6, 6, EquipmentSlot.LEGS);
        setCustomItemId(stack, "reinforced_deepslate_leggings");
        return stack;
    }
    
    public static ItemStack createReinforcedDeepslateBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyReinforcedDeepslateStats(stack, "Boots", 4, 5, EquipmentSlot.FEET);
        setCustomItemId(stack, "reinforced_deepslate_boots");
        return stack;
    }
    
    private static void applyReinforcedDeepslateStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(REINFORCED_DEEPSLATE_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Reinforced Deepslate " + pieceName).withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, REINFORCED_DEEPSLATE_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "reinforced_deepslate_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "reinforced_deepslate_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.BLUE));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.BLUE)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.BLUE)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Very High").withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.DARK_PURPLE, "reinforced_deepslate");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Indestructible deepslate").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }
    
    // ============================================================
    // CHISELED NETHER BRICK ARMOR - Carved nether brick
    // ============================================================
    
    public static final int CHISELED_NETHER_BRICK_DURABILITY = 380;
    public static final int CHISELED_NETHER_BRICK_COLOR = 0x4A2020; // Dark red-brown
    
    public static ItemStack createChiseledNetherBrickHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyChiseledNetherBrickStats(stack, "Helmet", 3, 3, EquipmentSlot.HEAD);
        setCustomItemId(stack, "chiseled_nether_brick_helmet");
        return stack;
    }
    
    public static ItemStack createChiseledNetherBrickChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyChiseledNetherBrickStats(stack, "Chestplate", 6, 4, EquipmentSlot.CHEST);
        setCustomItemId(stack, "chiseled_nether_brick_chestplate");
        return stack;
    }
    
    public static ItemStack createChiseledNetherBrickLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyChiseledNetherBrickStats(stack, "Leggings", 4, 3, EquipmentSlot.LEGS);
        setCustomItemId(stack, "chiseled_nether_brick_leggings");
        return stack;
    }
    
    public static ItemStack createChiseledNetherBrickBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyChiseledNetherBrickStats(stack, "Boots", 3, 2, EquipmentSlot.FEET);
        setCustomItemId(stack, "chiseled_nether_brick_boots");
        return stack;
    }
    
    private static void applyChiseledNetherBrickStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(CHISELED_NETHER_BRICK_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Chiseled Nether Brick " + pieceName).withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, CHISELED_NETHER_BRICK_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "chiseled_nether_brick_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "chiseled_nether_brick_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_RED));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.DARK_RED)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.DARK_RED)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Medium").withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.DARK_PURPLE, "chiseled_nether_brick");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Ancient nether brick").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }
    
    // ============================================================
    // CRACKED NETHER BRICK ARMOR - Damaged nether brick
    // ============================================================
    
    public static final int CRACKED_NETHER_BRICK_DURABILITY = 320;
    public static final int CRACKED_NETHER_BRICK_COLOR = 0x5A3030; // Cracked red-brown
    
    public static ItemStack createCrackedNetherBrickHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyCrackedNetherBrickStats(stack, "Helmet", 3, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "cracked_nether_brick_helmet");
        return stack;
    }
    
    public static ItemStack createCrackedNetherBrickChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyCrackedNetherBrickStats(stack, "Chestplate", 5, 3, EquipmentSlot.CHEST);
        setCustomItemId(stack, "cracked_nether_brick_chestplate");
        return stack;
    }
    
    public static ItemStack createCrackedNetherBrickLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyCrackedNetherBrickStats(stack, "Leggings", 4, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "cracked_nether_brick_leggings");
        return stack;
    }
    
    public static ItemStack createCrackedNetherBrickBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyCrackedNetherBrickStats(stack, "Boots", 2, 2, EquipmentSlot.FEET);
        setCustomItemId(stack, "cracked_nether_brick_boots");
        return stack;
    }
    
    private static void applyCrackedNetherBrickStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(CRACKED_NETHER_BRICK_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Cracked Nether Brick " + pieceName).withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, CRACKED_NETHER_BRICK_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "cracked_nether_brick_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "cracked_nether_brick_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.RED)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.RED)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Medium").withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.DARK_PURPLE, "cracked_nether_brick");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Worn nether brick").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }
    
    // ============================================================
    // CHISELED STONE BRICK ARMOR - Carved stone
    // ============================================================
    
    public static final int CHISELED_STONE_BRICK_DURABILITY = 400;
    public static final int CHISELED_STONE_BRICK_COLOR = 0x808080; // Gray
    
    public static ItemStack createChiseledStoneBrickHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyChiseledStoneBrickStats(stack, "Helmet", 3, 3, EquipmentSlot.HEAD);
        setCustomItemId(stack, "chiseled_stone_brick_helmet");
        return stack;
    }
    
    public static ItemStack createChiseledStoneBrickChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyChiseledStoneBrickStats(stack, "Chestplate", 6, 4, EquipmentSlot.CHEST);
        setCustomItemId(stack, "chiseled_stone_brick_chestplate");
        return stack;
    }
    
    public static ItemStack createChiseledStoneBrickLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyChiseledStoneBrickStats(stack, "Leggings", 4, 3, EquipmentSlot.LEGS);
        setCustomItemId(stack, "chiseled_stone_brick_leggings");
        return stack;
    }
    
    public static ItemStack createChiseledStoneBrickBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyChiseledStoneBrickStats(stack, "Boots", 3, 2, EquipmentSlot.FEET);
        setCustomItemId(stack, "chiseled_stone_brick_boots");
        return stack;
    }
    
    private static void applyChiseledStoneBrickStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(CHISELED_STONE_BRICK_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Chiseled Stone Brick " + pieceName).withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, CHISELED_STONE_BRICK_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "chiseled_stone_brick_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "chiseled_stone_brick_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.GRAY)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.GRAY)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Medium").withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.DARK_PURPLE, "chiseled_stone_brick");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Ancient carved stone").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }
    
    // ============================================================
    // CRACKED STONE BRICK ARMOR - Damaged stone
    // ============================================================
    
    public static final int CRACKED_STONE_BRICK_DURABILITY = 340;
    public static final int CRACKED_STONE_BRICK_COLOR = 0x707070; // Darker gray
    
    public static ItemStack createCrackedStoneBrickHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyCrackedStoneBrickStats(stack, "Helmet", 3, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "cracked_stone_brick_helmet");
        return stack;
    }
    
    public static ItemStack createCrackedStoneBrickChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyCrackedStoneBrickStats(stack, "Chestplate", 5, 3, EquipmentSlot.CHEST);
        setCustomItemId(stack, "cracked_stone_brick_chestplate");
        return stack;
    }
    
    public static ItemStack createCrackedStoneBrickLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyCrackedStoneBrickStats(stack, "Leggings", 4, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "cracked_stone_brick_leggings");
        return stack;
    }
    
    public static ItemStack createCrackedStoneBrickBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyCrackedStoneBrickStats(stack, "Boots", 2, 2, EquipmentSlot.FEET);
        setCustomItemId(stack, "cracked_stone_brick_boots");
        return stack;
    }
    
    private static void applyCrackedStoneBrickStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(CRACKED_STONE_BRICK_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Cracked Stone Brick " + pieceName).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, CRACKED_STONE_BRICK_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "cracked_stone_brick_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "cracked_stone_brick_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.DARK_GRAY)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.DARK_GRAY)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Medium").withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.DARK_PURPLE, "cracked_stone_brick");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Worn ancient stone").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }
    
    // ============================================================
    // END STONE BRICK ARMOR - End portal stone
    // ============================================================
    
    public static final int END_STONE_BRICK_DURABILITY = 360;
    public static final int END_STONE_BRICK_COLOR = 0xE8E8D8; // Pale yellow
    
    public static ItemStack createEndStoneBrickHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyEndStoneBrickStats(stack, "Helmet", 3, 3, EquipmentSlot.HEAD);
        setCustomItemId(stack, "end_stone_brick_helmet");
        return stack;
    }
    
    public static ItemStack createEndStoneBrickChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyEndStoneBrickStats(stack, "Chestplate", 6, 4, EquipmentSlot.CHEST);
        setCustomItemId(stack, "end_stone_brick_chestplate");
        return stack;
    }
    
    public static ItemStack createEndStoneBrickLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyEndStoneBrickStats(stack, "Leggings", 4, 3, EquipmentSlot.LEGS);
        setCustomItemId(stack, "end_stone_brick_leggings");
        return stack;
    }
    
    public static ItemStack createEndStoneBrickBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyEndStoneBrickStats(stack, "Boots", 3, 2, EquipmentSlot.FEET);
        setCustomItemId(stack, "end_stone_brick_boots");
        return stack;
    }
    
    private static void applyEndStoneBrickStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(END_STONE_BRICK_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("End Stone Brick " + pieceName).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, END_STONE_BRICK_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "end_stone_brick_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "end_stone_brick_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Medium").withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.DARK_PURPLE, "end_stone_brick");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8End city stone").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }
    
    // ============================================================
    // RED SANDSTONE ARMOR - Red desert stone
    // ============================================================
    
    public static final int RED_SANDSTONE_DURABILITY = 280;
    public static final int RED_SANDSTONE_COLOR = 0xB4604A; // Red
    
    public static ItemStack createRedSandstoneHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyRedSandstoneStats(stack, "Helmet", 2, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "red_sandstone_helmet");
        return stack;
    }
    
    public static ItemStack createRedSandstoneChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyRedSandstoneStats(stack, "Chestplate", 5, 3, EquipmentSlot.CHEST);
        setCustomItemId(stack, "red_sandstone_chestplate");
        return stack;
    }
    
    public static ItemStack createRedSandstoneLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyRedSandstoneStats(stack, "Leggings", 3, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "red_sandstone_leggings");
        return stack;
    }
    
    public static ItemStack createRedSandstoneBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyRedSandstoneStats(stack, "Boots", 2, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "red_sandstone_boots");
        return stack;
    }
    
    private static void applyRedSandstoneStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(RED_SANDSTONE_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Red Sandstone " + pieceName).withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, RED_SANDSTONE_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "red_sandstone_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "red_sandstone_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.RED)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.RED)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Medium").withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.DARK_PURPLE, "red_sandstone");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Red desert sandstone").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }
    
    // ============================================================
    // RAW IRON BLOCK ARMOR - Raw iron storage
    // ============================================================
    
    public static final int RAW_IRON_BLOCK_DURABILITY = 500;
    public static final int RAW_IRON_BLOCK_COLOR = 0xA89080; // Raw iron color
    
    public static ItemStack createRawIronBlockHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyRawIronBlockStats(stack, "Helmet", 3, 4, EquipmentSlot.HEAD);
        setCustomItemId(stack, "raw_iron_block_helmet");
        return stack;
    }
    
    public static ItemStack createRawIronBlockChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyRawIronBlockStats(stack, "Chestplate", 7, 5, EquipmentSlot.CHEST);
        setCustomItemId(stack, "raw_iron_block_chestplate");
        return stack;
    }
    
    public static ItemStack createRawIronBlockLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyRawIronBlockStats(stack, "Leggings", 5, 4, EquipmentSlot.LEGS);
        setCustomItemId(stack, "raw_iron_block_leggings");
        return stack;
    }
    
    public static ItemStack createRawIronBlockBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyRawIronBlockStats(stack, "Boots", 3, 3, EquipmentSlot.FEET);
        setCustomItemId(stack, "raw_iron_block_boots");
        return stack;
    }
    
    private static void applyRawIronBlockStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(RAW_IRON_BLOCK_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Raw Iron Block " + pieceName).withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, RAW_IRON_BLOCK_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "raw_iron_block_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "raw_iron_block_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.WHITE)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.WHITE)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("High").withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.DARK_PURPLE, "raw_iron_block");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Raw iron block").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }
    
    // ============================================================
    // RAW GOLD BLOCK ARMOR - Raw gold storage
    // ============================================================
    
    public static final int RAW_GOLD_BLOCK_DURABILITY = 350;
    public static final int RAW_GOLD_BLOCK_COLOR = 0xE8C868; // Raw gold color
    
    public static ItemStack createRawGoldBlockHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyRawGoldBlockStats(stack, "Helmet", 2, 3, EquipmentSlot.HEAD);
        setCustomItemId(stack, "raw_gold_block_helmet");
        return stack;
    }
    
    public static ItemStack createRawGoldBlockChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyRawGoldBlockStats(stack, "Chestplate", 5, 4, EquipmentSlot.CHEST);
        setCustomItemId(stack, "raw_gold_block_chestplate");
        return stack;
    }
    
    public static ItemStack createRawGoldBlockLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyRawGoldBlockStats(stack, "Leggings", 3, 3, EquipmentSlot.LEGS);
        setCustomItemId(stack, "raw_gold_block_leggings");
        return stack;
    }
    
    public static ItemStack createRawGoldBlockBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyRawGoldBlockStats(stack, "Boots", 2, 2, EquipmentSlot.FEET);
        setCustomItemId(stack, "raw_gold_block_boots");
        return stack;
    }
    
    private static void applyRawGoldBlockStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(RAW_GOLD_BLOCK_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Raw Gold Block " + pieceName).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, RAW_GOLD_BLOCK_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "raw_gold_block_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "raw_gold_block_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.GOLD)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.GOLD)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Medium").withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.DARK_PURPLE, "raw_gold_block");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Raw gold block").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }
    
    // ============================================================
    // PRISMARINE BRICKS ARMOR - Ocean castle brick
    // ============================================================
    
    public static final int PRISMARINE_BRICKS_DURABILITY = 320;
    public static final int PRISMARINE_BRICKS_COLOR = 0x4A8B7F; // Dark teal
    
    public static ItemStack createPrismarineBricksHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyPrismarineBricksStats(stack, "Helmet", 3, 3, EquipmentSlot.HEAD);
        setCustomItemId(stack, "prismarine_bricks_helmet");
        return stack;
    }
    
    public static ItemStack createPrismarineBricksChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyPrismarineBricksStats(stack, "Chestplate", 6, 4, EquipmentSlot.CHEST);
        setCustomItemId(stack, "prismarine_bricks_chestplate");
        return stack;
    }
    
    public static ItemStack createPrismarineBricksLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyPrismarineBricksStats(stack, "Leggings", 4, 3, EquipmentSlot.LEGS);
        setCustomItemId(stack, "prismarine_bricks_leggings");
        return stack;
    }
    
    public static ItemStack createPrismarineBricksBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyPrismarineBricksStats(stack, "Boots", 3, 2, EquipmentSlot.FEET);
        setCustomItemId(stack, "prismarine_bricks_boots");
        return stack;
    }
    
    private static void applyPrismarineBricksStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(PRISMARINE_BRICKS_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Prismarine Bricks " + pieceName).withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, PRISMARINE_BRICKS_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "prismarine_bricks_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "prismarine_bricks_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_AQUA));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.DARK_AQUA)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.DARK_AQUA)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Medium").withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.DARK_PURPLE, "prismarine_bricks");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Ocean monument brick").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }
    
    // ============================================================
    // DARK PRISMARINE ARMOR - Deep ocean stone
    // ============================================================
    
    public static final int DARK_PRISMARINE_DURABILITY = 360;
    public static final int DARK_PRISMARINE_COLOR = 0x2A4A45; // Dark teal-gray
    
    public static ItemStack createDarkPrismarineHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyDarkPrismarineStats(stack, "Helmet", 3, 3, EquipmentSlot.HEAD);
        setCustomItemId(stack, "dark_prismarine_helmet");
        return stack;
    }
    
    public static ItemStack createDarkPrismarineChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyDarkPrismarineStats(stack, "Chestplate", 6, 4, EquipmentSlot.CHEST);
        setCustomItemId(stack, "dark_prismarine_chestplate");
        return stack;
    }
    
    public static ItemStack createDarkPrismarineLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyDarkPrismarineStats(stack, "Leggings", 4, 3, EquipmentSlot.LEGS);
        setCustomItemId(stack, "dark_prismarine_leggings");
        return stack;
    }
    
    public static ItemStack createDarkPrismarineBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyDarkPrismarineStats(stack, "Boots", 3, 2, EquipmentSlot.FEET);
        setCustomItemId(stack, "dark_prismarine_boots");
        return stack;
    }
    
    private static void applyDarkPrismarineStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(DARK_PRISMARINE_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Dark Prismarine " + pieceName).withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, DARK_PRISMARINE_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "dark_prismarine_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "dark_prismarine_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_GREEN));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.DARK_GREEN)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.DARK_GREEN)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Medium").withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.DARK_PURPLE, "dark_prismarine");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Deep ocean prismarine").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }
    
    // ============================================================
    // SEA LANTERN ARMOR - Glowing ocean light
    // ============================================================
    
    public static final int SEA_LANTERN_DURABILITY = 220;
    public static final int SEA_LANTERN_COLOR = 0xA8E6CF; // Light green-white
    
    public static ItemStack createSeaLanternHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applySeaLanternStats(stack, "Helmet", 2, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "sea_lantern_helmet");
        return stack;
    }
    
    public static ItemStack createSeaLanternChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applySeaLanternStats(stack, "Chestplate", 4, 3, EquipmentSlot.CHEST);
        setCustomItemId(stack, "sea_lantern_chestplate");
        return stack;
    }
    
    public static ItemStack createSeaLanternLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applySeaLanternStats(stack, "Leggings", 3, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "sea_lantern_leggings");
        return stack;
    }
    
    public static ItemStack createSeaLanternBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applySeaLanternStats(stack, "Boots", 2, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "sea_lantern_boots");
        return stack;
    }
    
    private static void applySeaLanternStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(SEA_LANTERN_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Sea Lantern " + pieceName).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, SEA_LANTERN_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "sea_lantern_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "sea_lantern_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.AQUA));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.AQUA)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.AQUA)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Low").withStyle(ChatFormatting.RED)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.DARK_PURPLE, "sea_lantern");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Glowing sea lantern").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }
    
    // ============================================================
    // LODESTONE ARMOR - Compass stone
    // ============================================================
    
    public static final int LODESTONE_DURABILITY = 450;
    public static final int LODESTONE_COLOR = 0x6B5B4F; // Brown-gray
    
    public static ItemStack createLodestoneHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyLodestoneStats(stack, "Helmet", 3, 4, EquipmentSlot.HEAD);
        setCustomItemId(stack, "lodestone_helmet");
        return stack;
    }
    
    public static ItemStack createLodestoneChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyLodestoneStats(stack, "Chestplate", 6, 5, EquipmentSlot.CHEST);
        setCustomItemId(stack, "lodestone_chestplate");
        return stack;
    }
    
    public static ItemStack createLodestoneLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyLodestoneStats(stack, "Leggings", 4, 4, EquipmentSlot.LEGS);
        setCustomItemId(stack, "lodestone_leggings");
        return stack;
    }
    
    public static ItemStack createLodestoneBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyLodestoneStats(stack, "Boots", 3, 3, EquipmentSlot.FEET);
        setCustomItemId(stack, "lodestone_boots");
        return stack;
    }
    
    private static void applyLodestoneStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(LODESTONE_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Lodestone " + pieceName).withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, LODESTONE_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "lodestone_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "lodestone_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.GRAY)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.GRAY)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("High").withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.DARK_PURPLE, "lodestone");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Magnetic lodestone").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }
    
    // ============================================================
    // BLACKSTONE BRICKS ARMOR - Polished blackstone
    // ============================================================
    
    public static final int BLACKSTONE_BRICKS_DURABILITY = 400;
    public static final int BLACKSTONE_BRICKS_COLOR = 0x2C2C2C; // Black
    
    public static ItemStack createBlackstoneBricksHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyBlackstoneBricksStats(stack, "Helmet", 3, 3, EquipmentSlot.HEAD);
        setCustomItemId(stack, "blackstone_bricks_helmet");
        return stack;
    }
    
    public static ItemStack createBlackstoneBricksChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyBlackstoneBricksStats(stack, "Chestplate", 6, 4, EquipmentSlot.CHEST);
        setCustomItemId(stack, "blackstone_bricks_chestplate");
        return stack;
    }
    
    public static ItemStack createBlackstoneBricksLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyBlackstoneBricksStats(stack, "Leggings", 4, 3, EquipmentSlot.LEGS);
        setCustomItemId(stack, "blackstone_bricks_leggings");
        return stack;
    }
    
    public static ItemStack createBlackstoneBricksBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyBlackstoneBricksStats(stack, "Boots", 3, 2, EquipmentSlot.FEET);
        setCustomItemId(stack, "blackstone_bricks_boots");
        return stack;
    }
    
    private static void applyBlackstoneBricksStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(BLACKSTONE_BRICKS_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Blackstone Bricks " + pieceName).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, BLACKSTONE_BRICKS_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "blackstone_bricks_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "blackstone_bricks_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.DARK_GRAY)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.DARK_GRAY)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Medium").withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.DARK_PURPLE, "blackstone_bricks");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Polished blackstone brick").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }
    
    // ============================================================
    // POLISHED BLACKSTONE ARMOR - Smooth blackstone
    // ============================================================
    
    public static final int POLISHED_BLACKSTONE_DURABILITY = 380;
    public static final int POLISHED_BLACKSTONE_COLOR = 0x3C3C3C; // Smooth black
    
    public static ItemStack createPolishedBlackstoneHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyPolishedBlackstoneStats(stack, "Helmet", 3, 3, EquipmentSlot.HEAD);
        setCustomItemId(stack, "polished_blackstone_helmet");
        return stack;
    }
    
    public static ItemStack createPolishedBlackstoneChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyPolishedBlackstoneStats(stack, "Chestplate", 6, 4, EquipmentSlot.CHEST);
        setCustomItemId(stack, "polished_blackstone_chestplate");
        return stack;
    }
    
    public static ItemStack createPolishedBlackstoneLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyPolishedBlackstoneStats(stack, "Leggings", 4, 3, EquipmentSlot.LEGS);
        setCustomItemId(stack, "polished_blackstone_leggings");
        return stack;
    }
    
    public static ItemStack createPolishedBlackstoneBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyPolishedBlackstoneStats(stack, "Boots", 3, 2, EquipmentSlot.FEET);
        setCustomItemId(stack, "polished_blackstone_boots");
        return stack;
    }
    
    private static void applyPolishedBlackstoneStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(POLISHED_BLACKSTONE_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Polished Blackstone " + pieceName).withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, POLISHED_BLACKSTONE_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "polished_blackstone_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "polished_blackstone_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.GRAY)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.GRAY)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Medium").withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.DARK_PURPLE, "polished_blackstone");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Polished blackstone").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }
    
    // ============================================================
    // SMOOTH BASALT ARMOR - Polished volcanic rock
    // ============================================================
    
    public static final int SMOOTH_BASALT_DURABILITY = 420;
    public static final int SMOOTH_BASALT_COLOR = 0x4D4D4D; // Dark smooth gray
    
    public static ItemStack createSmoothBasaltHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applySmoothBasaltStats(stack, "Helmet", 3, 4, EquipmentSlot.HEAD);
        setCustomItemId(stack, "smooth_basalt_helmet");
        return stack;
    }
    
    public static ItemStack createSmoothBasaltChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applySmoothBasaltStats(stack, "Chestplate", 6, 5, EquipmentSlot.CHEST);
        setCustomItemId(stack, "smooth_basalt_chestplate");
        return stack;
    }
    
    public static ItemStack createSmoothBasaltLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applySmoothBasaltStats(stack, "Leggings", 4, 4, EquipmentSlot.LEGS);
        setCustomItemId(stack, "smooth_basalt_leggings");
        return stack;
    }
    
    public static ItemStack createSmoothBasaltBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applySmoothBasaltStats(stack, "Boots", 3, 3, EquipmentSlot.FEET);
        setCustomItemId(stack, "smooth_basalt_boots");
        return stack;
    }
    
    private static void applySmoothBasaltStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(SMOOTH_BASALT_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Smooth Basalt " + pieceName).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, SMOOTH_BASALT_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "smooth_basalt_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "smooth_basalt_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.DARK_GRAY)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.DARK_GRAY)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("High").withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.DARK_PURPLE, "smooth_basalt");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Smooth volcanic basalt").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }
    
    // ============================================================
    // AMETHYST CLUSTER ARMOR - Purple crystal
    // ============================================================
    
    public static final int AMETHYST_CLUSTER_DURABILITY = 280;
    public static final int AMETHYST_CLUSTER_COLOR = 0xB366FF; // Bright purple
    
    public static ItemStack createAmethystClusterHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyAmethystClusterStats(stack, "Helmet", 2, 3, EquipmentSlot.HEAD);
        setCustomItemId(stack, "amethyst_cluster_helmet");
        return stack;
    }
    
    public static ItemStack createAmethystClusterChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyAmethystClusterStats(stack, "Chestplate", 5, 4, EquipmentSlot.CHEST);
        setCustomItemId(stack, "amethyst_cluster_chestplate");
        return stack;
    }
    
    public static ItemStack createAmethystClusterLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyAmethystClusterStats(stack, "Leggings", 3, 3, EquipmentSlot.LEGS);
        setCustomItemId(stack, "amethyst_cluster_leggings");
        return stack;
    }
    
    public static ItemStack createAmethystClusterBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyAmethystClusterStats(stack, "Boots", 2, 2, EquipmentSlot.FEET);
        setCustomItemId(stack, "amethyst_cluster_boots");
        return stack;
    }
    
    private static void applyAmethystClusterStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(AMETHYST_CLUSTER_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Amethyst Cluster " + pieceName).withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, AMETHYST_CLUSTER_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "amethyst_cluster_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "amethyst_cluster_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.LIGHT_PURPLE));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.LIGHT_PURPLE)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.LIGHT_PURPLE)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Medium").withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.DARK_PURPLE, "amethyst_cluster");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Purple amethyst crystal").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }
    
    // ============================================================
    // STRIPPED SPRUCE LOG ARMOR
    // ============================================================
    private static final int STRIPPED_SPRUCE_LOG_COLOR = 0x8B4513; // Brown
    private static final int STRIPPED_SPRUCE_LOG_DURABILITY = 165;

    public static ItemStack createStrippedSpruceLogHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyStrippedSpruceLogStats(stack, "Helmet", 2, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "stripped_spruce_log_helmet");
        return stack;
    }
    
    public static ItemStack createStrippedSpruceLogChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyStrippedSpruceLogStats(stack, "Chestplate", 5, 3, EquipmentSlot.CHEST);
        setCustomItemId(stack, "stripped_spruce_log_chestplate");
        return stack;
    }
    
    public static ItemStack createStrippedSpruceLogLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyStrippedSpruceLogStats(stack, "Leggings", 3, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "stripped_spruce_log_leggings");
        return stack;
    }
    
    public static ItemStack createStrippedSpruceLogBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyStrippedSpruceLogStats(stack, "Boots", 2, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "stripped_spruce_log_boots");
        return stack;
    }
    
    private static void applyStrippedSpruceLogStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(STRIPPED_SPRUCE_LOG_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Stripped Spruce " + pieceName).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, STRIPPED_SPRUCE_LOG_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "stripped_spruce_log_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "stripped_spruce_log_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.GOLD)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.GOLD)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("High").withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.GOLD, "stripped_spruce_log");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Stripped spruce wood").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // STRIPPED BIRCH LOG ARMOR
    // ============================================================
    private static final int STRIPPED_BIRCH_LOG_COLOR = 0xF5F5DC; // White
    private static final int STRIPPED_BIRCH_LOG_DURABILITY = 165;

    public static ItemStack createStrippedBirchLogHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyStrippedBirchLogStats(stack, "Helmet", 2, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "stripped_birch_log_helmet");
        return stack;
    }
    
    public static ItemStack createStrippedBirchLogChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyStrippedBirchLogStats(stack, "Chestplate", 5, 3, EquipmentSlot.CHEST);
        setCustomItemId(stack, "stripped_birch_log_chestplate");
        return stack;
    }
    
    public static ItemStack createStrippedBirchLogLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyStrippedBirchLogStats(stack, "Leggings", 3, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "stripped_birch_log_leggings");
        return stack;
    }
    
    public static ItemStack createStrippedBirchLogBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyStrippedBirchLogStats(stack, "Boots", 2, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "stripped_birch_log_boots");
        return stack;
    }
    
    private static void applyStrippedBirchLogStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(STRIPPED_BIRCH_LOG_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Stripped Birch " + pieceName).withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, STRIPPED_BIRCH_LOG_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "stripped_birch_log_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "stripped_birch_log_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.WHITE)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.WHITE)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("High").withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.WHITE, "stripped_birch_log");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Stripped birch wood").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // STRIPPED DARK OAK LOG ARMOR
    // ============================================================
    private static final int STRIPPED_DARK_OAK_LOG_COLOR = 0x3D2914; // Dark brown
    private static final int STRIPPED_DARK_OAK_LOG_DURABILITY = 165;

    public static ItemStack createStrippedDarkOakLogHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyStrippedDarkOakLogStats(stack, "Helmet", 2, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "stripped_dark_oak_log_helmet");
        return stack;
    }
    
    public static ItemStack createStrippedDarkOakLogChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyStrippedDarkOakLogStats(stack, "Chestplate", 5, 3, EquipmentSlot.CHEST);
        setCustomItemId(stack, "stripped_dark_oak_log_chestplate");
        return stack;
    }
    
    public static ItemStack createStrippedDarkOakLogLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyStrippedDarkOakLogStats(stack, "Leggings", 3, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "stripped_dark_oak_log_leggings");
        return stack;
    }
    
    public static ItemStack createStrippedDarkOakLogBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyStrippedDarkOakLogStats(stack, "Boots", 2, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "stripped_dark_oak_log_boots");
        return stack;
    }
    
    private static void applyStrippedDarkOakLogStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(STRIPPED_DARK_OAK_LOG_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Stripped Dark Oak " + pieceName).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, STRIPPED_DARK_OAK_LOG_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "stripped_dark_oak_log_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "stripped_dark_oak_log_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.DARK_GRAY)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.DARK_GRAY)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("High").withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.DARK_GRAY, "stripped_dark_oak_log");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Stripped dark oak wood").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // STRIPPED JUNGLE LOG ARMOR
    // ============================================================
    private static final int STRIPPED_JUNGLE_LOG_COLOR = 0x6B8E23; // Olive
    private static final int STRIPPED_JUNGLE_LOG_DURABILITY = 165;

    public static ItemStack createStrippedJungleLogHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyStrippedJungleLogStats(stack, "Helmet", 2, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "stripped_jungle_log_helmet");
        return stack;
    }
    
    public static ItemStack createStrippedJungleLogChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyStrippedJungleLogStats(stack, "Chestplate", 5, 3, EquipmentSlot.CHEST);
        setCustomItemId(stack, "stripped_jungle_log_chestplate");
        return stack;
    }
    
    public static ItemStack createStrippedJungleLogLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyStrippedJungleLogStats(stack, "Leggings", 3, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "stripped_jungle_log_leggings");
        return stack;
    }
    
    public static ItemStack createStrippedJungleLogBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyStrippedJungleLogStats(stack, "Boots", 2, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "stripped_jungle_log_boots");
        return stack;
    }
    
    private static void applyStrippedJungleLogStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(STRIPPED_JUNGLE_LOG_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Stripped Jungle " + pieceName).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, STRIPPED_JUNGLE_LOG_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "stripped_jungle_log_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "stripped_jungle_log_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("High").withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.GREEN, "stripped_jungle_log");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Stripped jungle wood").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // STRIPPED ACACIA LOG ARMOR
    // ============================================================
    private static final int STRIPPED_ACACIA_LOG_COLOR = 0xD2691E; // Orange-brown
    private static final int STRIPPED_ACACIA_LOG_DURABILITY = 165;

    public static ItemStack createStrippedAcaciaLogHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyStrippedAcaciaLogStats(stack, "Helmet", 2, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "stripped_acacia_log_helmet");
        return stack;
    }
    
    public static ItemStack createStrippedAcaciaLogChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyStrippedAcaciaLogStats(stack, "Chestplate", 5, 3, EquipmentSlot.CHEST);
        setCustomItemId(stack, "stripped_acacia_log_chestplate");
        return stack;
    }
    
    public static ItemStack createStrippedAcaciaLogLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyStrippedAcaciaLogStats(stack, "Leggings", 3, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "stripped_acacia_log_leggings");
        return stack;
    }
    
    public static ItemStack createStrippedAcaciaLogBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyStrippedAcaciaLogStats(stack, "Boots", 2, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "stripped_acacia_log_boots");
        return stack;
    }
    
    private static void applyStrippedAcaciaLogStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(STRIPPED_ACACIA_LOG_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Stripped Acacia " + pieceName).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, STRIPPED_ACACIA_LOG_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "stripped_acacia_log_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "stripped_acacia_log_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.GOLD)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.GOLD)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("High").withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.GOLD, "stripped_acacia_log");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Stripped acacia wood").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // STRIPPED MANGROVE LOG ARMOR
    // ============================================================
    private static final int STRIPPED_MANGROVE_LOG_COLOR = 0x800000; // Maroon
    private static final int STRIPPED_MANGROVE_LOG_DURABILITY = 165;

    public static ItemStack createStrippedMangroveLogHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyStrippedMangroveLogStats(stack, "Helmet", 2, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "stripped_mangrove_log_helmet");
        return stack;
    }
    
    public static ItemStack createStrippedMangroveLogChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyStrippedMangroveLogStats(stack, "Chestplate", 5, 3, EquipmentSlot.CHEST);
        setCustomItemId(stack, "stripped_mangrove_log_chestplate");
        return stack;
    }
    
    public static ItemStack createStrippedMangroveLogLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyStrippedMangroveLogStats(stack, "Leggings", 3, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "stripped_mangrove_log_leggings");
        return stack;
    }
    
    public static ItemStack createStrippedMangroveLogBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyStrippedMangroveLogStats(stack, "Boots", 2, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "stripped_mangrove_log_boots");
        return stack;
    }
    
    private static void applyStrippedMangroveLogStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(STRIPPED_MANGROVE_LOG_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Stripped Mangrove " + pieceName).withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, STRIPPED_MANGROVE_LOG_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "stripped_mangrove_log_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "stripped_mangrove_log_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_RED));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.DARK_RED)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.DARK_RED)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("High").withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.DARK_RED, "stripped_mangrove_log");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Stripped mangrove wood").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // STRIPPED CHERRY LOG ARMOR
    // ============================================================
    private static final int STRIPPED_CHERRY_LOG_COLOR = 0xFFB7C5; // Cherry pink
    private static final int STRIPPED_CHERRY_LOG_DURABILITY = 165;

    public static ItemStack createStrippedCherryLogHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyStrippedCherryLogStats(stack, "Helmet", 2, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "stripped_cherry_log_helmet");
        return stack;
    }
    
    public static ItemStack createStrippedCherryLogChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyStrippedCherryLogStats(stack, "Chestplate", 5, 3, EquipmentSlot.CHEST);
        setCustomItemId(stack, "stripped_cherry_log_chestplate");
        return stack;
    }
    
    public static ItemStack createStrippedCherryLogLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyStrippedCherryLogStats(stack, "Leggings", 3, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "stripped_cherry_log_leggings");
        return stack;
    }
    
    public static ItemStack createStrippedCherryLogBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyStrippedCherryLogStats(stack, "Boots", 2, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "stripped_cherry_log_boots");
        return stack;
    }
    
    private static void applyStrippedCherryLogStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(STRIPPED_CHERRY_LOG_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Stripped Cherry " + pieceName).withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, STRIPPED_CHERRY_LOG_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "stripped_cherry_log_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "stripped_cherry_log_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.LIGHT_PURPLE));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.LIGHT_PURPLE)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.LIGHT_PURPLE)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("High").withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.LIGHT_PURPLE, "stripped_cherry_log");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Stripped cherry wood").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // STRIPPED BAMBOO BLOCK ARMOR
    // ============================================================
    private static final int STRIPPED_BAMBOO_BLOCK_COLOR = 0xFFD700; // Gold
    private static final int STRIPPED_BAMBOO_BLOCK_DURABILITY = 150;

    public static ItemStack createStrippedBambooBlockHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyStrippedBambooBlockStats(stack, "Helmet", 2, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "stripped_bamboo_block_helmet");
        return stack;
    }
    
    public static ItemStack createStrippedBambooBlockChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyStrippedBambooBlockStats(stack, "Chestplate", 5, 3, EquipmentSlot.CHEST);
        setCustomItemId(stack, "stripped_bamboo_block_chestplate");
        return stack;
    }
    
    public static ItemStack createStrippedBambooBlockLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyStrippedBambooBlockStats(stack, "Leggings", 3, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "stripped_bamboo_block_leggings");
        return stack;
    }
    
    public static ItemStack createStrippedBambooBlockBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyStrippedBambooBlockStats(stack, "Boots", 2, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "stripped_bamboo_block_boots");
        return stack;
    }
    
    private static void applyStrippedBambooBlockStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(STRIPPED_BAMBOO_BLOCK_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Stripped Bamboo " + pieceName).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, STRIPPED_BAMBOO_BLOCK_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "stripped_bamboo_block_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "stripped_bamboo_block_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Medium").withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.YELLOW, "stripped_bamboo_block");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Stripped bamboo").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // STRIPPED CRIMSON STEM ARMOR
    // ============================================================
    private static final int STRIPPED_CRIMSON_STEM_COLOR = 0x8B0000; // Dark red
    private static final int STRIPPED_CRIMSON_STEM_DURABILITY = 165;

    public static ItemStack createStrippedCrimsonStemHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyStrippedCrimsonStemStats(stack, "Helmet", 2, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "stripped_crimson_stem_helmet");
        return stack;
    }
    
    public static ItemStack createStrippedCrimsonStemChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyStrippedCrimsonStemStats(stack, "Chestplate", 5, 3, EquipmentSlot.CHEST);
        setCustomItemId(stack, "stripped_crimson_stem_chestplate");
        return stack;
    }
    
    public static ItemStack createStrippedCrimsonStemLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyStrippedCrimsonStemStats(stack, "Leggings", 3, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "stripped_crimson_stem_leggings");
        return stack;
    }
    
    public static ItemStack createStrippedCrimsonStemBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyStrippedCrimsonStemStats(stack, "Boots", 2, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "stripped_crimson_stem_boots");
        return stack;
    }
    
    private static void applyStrippedCrimsonStemStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(STRIPPED_CRIMSON_STEM_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Stripped Crimson " + pieceName).withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, STRIPPED_CRIMSON_STEM_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "stripped_crimson_stem_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "stripped_crimson_stem_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_RED));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.DARK_RED)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.DARK_RED)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("High").withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.DARK_RED, "stripped_crimson_stem");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Stripped crimson stem").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // STRIPPED WARPED STEM ARMOR
    // ============================================================
    private static final int STRIPPED_WARPED_STEM_COLOR = 0x008080; // Teal
    private static final int STRIPPED_WARPED_STEM_DURABILITY = 165;

    public static ItemStack createStrippedWarpedStemHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyStrippedWarpedStemStats(stack, "Helmet", 2, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "stripped_warped_stem_helmet");
        return stack;
    }
    
    public static ItemStack createStrippedWarpedStemChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyStrippedWarpedStemStats(stack, "Chestplate", 5, 3, EquipmentSlot.CHEST);
        setCustomItemId(stack, "stripped_warped_stem_chestplate");
        return stack;
    }
    
    public static ItemStack createStrippedWarpedStemLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyStrippedWarpedStemStats(stack, "Leggings", 3, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "stripped_warped_stem_leggings");
        return stack;
    }
    
    public static ItemStack createStrippedWarpedStemBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyStrippedWarpedStemStats(stack, "Boots", 2, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "stripped_warped_stem_boots");
        return stack;
    }
    
    private static void applyStrippedWarpedStemStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(STRIPPED_WARPED_STEM_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Stripped Warped " + pieceName).withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, STRIPPED_WARPED_STEM_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "stripped_warped_stem_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "stripped_warped_stem_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_AQUA));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.DARK_AQUA)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.DARK_AQUA)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("High").withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.DARK_AQUA, "stripped_warped_stem");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Stripped warped stem").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // SPRUCE PLANKS ARMOR
    // ============================================================
    private static final int SPRUCE_PLANKS_COLOR = 0xA0522D; // Sienna
    private static final int SPRUCE_PLANKS_DURABILITY = 150;

    public static ItemStack createSprucePlanksHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applySprucePlanksStats(stack, "Helmet", 2, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "spruce_planks_helmet");
        return stack;
    }
    
    public static ItemStack createSprucePlanksChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applySprucePlanksStats(stack, "Chestplate", 5, 3, EquipmentSlot.CHEST);
        setCustomItemId(stack, "spruce_planks_chestplate");
        return stack;
    }
    
    public static ItemStack createSprucePlanksLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applySprucePlanksStats(stack, "Leggings", 3, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "spruce_planks_leggings");
        return stack;
    }
    
    public static ItemStack createSprucePlanksBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applySprucePlanksStats(stack, "Boots", 2, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "spruce_planks_boots");
        return stack;
    }
    
    private static void applySprucePlanksStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(SPRUCE_PLANKS_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Spruce Planks " + pieceName).withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, SPRUCE_PLANKS_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "spruce_planks_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "spruce_planks_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_AQUA));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.DARK_AQUA)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.DARK_AQUA)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Medium").withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.DARK_AQUA, "spruce_planks");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Spruce wood planks").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // BIRCH PLANKS ARMOR
    // ============================================================
    private static final int BIRCH_PLANKS_COLOR = 0xF5F5DC; // Beige
    private static final int BIRCH_PLANKS_DURABILITY = 150;

    public static ItemStack createBirchPlanksHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyBirchPlanksStats(stack, "Helmet", 2, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "birch_planks_helmet");
        return stack;
    }
    
    public static ItemStack createBirchPlanksChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyBirchPlanksStats(stack, "Chestplate", 5, 3, EquipmentSlot.CHEST);
        setCustomItemId(stack, "birch_planks_chestplate");
        return stack;
    }
    
    public static ItemStack createBirchPlanksLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyBirchPlanksStats(stack, "Leggings", 3, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "birch_planks_leggings");
        return stack;
    }
    
    public static ItemStack createBirchPlanksBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyBirchPlanksStats(stack, "Boots", 2, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "birch_planks_boots");
        return stack;
    }
    
    private static void applyBirchPlanksStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(BIRCH_PLANKS_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Birch Planks " + pieceName).withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, BIRCH_PLANKS_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "birch_planks_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "birch_planks_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.WHITE)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.WHITE)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Medium").withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.WHITE, "birch_planks");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Birch wood planks").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // JUNGLE PLANKS ARMOR
    // ============================================================
    private static final int JUNGLE_PLANKS_COLOR = 0x9ACD32; // Yellow-green
    private static final int JUNGLE_PLANKS_DURABILITY = 150;

    public static ItemStack createJunglePlanksHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyJunglePlanksStats(stack, "Helmet", 2, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "jungle_planks_helmet");
        return stack;
    }
    
    public static ItemStack createJunglePlanksChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyJunglePlanksStats(stack, "Chestplate", 5, 3, EquipmentSlot.CHEST);
        setCustomItemId(stack, "jungle_planks_chestplate");
        return stack;
    }
    
    public static ItemStack createJunglePlanksLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyJunglePlanksStats(stack, "Leggings", 3, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "jungle_planks_leggings");
        return stack;
    }
    
    public static ItemStack createJunglePlanksBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyJunglePlanksStats(stack, "Boots", 2, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "jungle_planks_boots");
        return stack;
    }
    
    private static void applyJunglePlanksStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(JUNGLE_PLANKS_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Jungle Planks " + pieceName).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, JUNGLE_PLANKS_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "jungle_planks_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "jungle_planks_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Medium").withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.GREEN, "jungle_planks");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Jungle wood planks").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // ACACIA PLANKS ARMOR
    // ============================================================
    private static final int ACACIA_PLANKS_COLOR = 0xCD853F; // Peru
    private static final int ACACIA_PLANKS_DURABILITY = 150;

    public static ItemStack createAcaciaPlanksHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyAcaciaPlanksStats(stack, "Helmet", 2, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "acacia_planks_helmet");
        return stack;
    }
    
    public static ItemStack createAcaciaPlanksChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyAcaciaPlanksStats(stack, "Chestplate", 5, 3, EquipmentSlot.CHEST);
        setCustomItemId(stack, "acacia_planks_chestplate");
        return stack;
    }
    
    public static ItemStack createAcaciaPlanksLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyAcaciaPlanksStats(stack, "Leggings", 3, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "acacia_planks_leggings");
        return stack;
    }
    
    public static ItemStack createAcaciaPlanksBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyAcaciaPlanksStats(stack, "Boots", 2, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "acacia_planks_boots");
        return stack;
    }
    
    private static void applyAcaciaPlanksStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(ACACIA_PLANKS_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Acacia Planks " + pieceName).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, ACACIA_PLANKS_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "acacia_planks_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "acacia_planks_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.GOLD)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.GOLD)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Medium").withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.GOLD, "acacia_planks");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Acacia wood planks").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // DARK OAK PLANKS ARMOR
    // ============================================================
    private static final int DARK_OAK_PLANKS_COLOR = 0x3E2723; // Dark brown
    private static final int DARK_OAK_PLANKS_DURABILITY = 150;

    public static ItemStack createDarkOakPlanksHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyDarkOakPlanksStats(stack, "Helmet", 2, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "dark_oak_planks_helmet");
        return stack;
    }
    
    public static ItemStack createDarkOakPlanksChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyDarkOakPlanksStats(stack, "Chestplate", 5, 3, EquipmentSlot.CHEST);
        setCustomItemId(stack, "dark_oak_planks_chestplate");
        return stack;
    }
    
    public static ItemStack createDarkOakPlanksLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyDarkOakPlanksStats(stack, "Leggings", 3, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "dark_oak_planks_leggings");
        return stack;
    }
    
    public static ItemStack createDarkOakPlanksBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyDarkOakPlanksStats(stack, "Boots", 2, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "dark_oak_planks_boots");
        return stack;
    }
    
    private static void applyDarkOakPlanksStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(DARK_OAK_PLANKS_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Dark Oak Planks " + pieceName).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, DARK_OAK_PLANKS_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "dark_oak_planks_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "dark_oak_planks_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.DARK_GRAY)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.DARK_GRAY)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Medium").withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.DARK_GRAY, "dark_oak_planks");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Dark oak wood planks").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // MANGROVE PLANKS ARMOR
    // ============================================================
    private static final int MANGROVE_PLANKS_COLOR = 0x6D4C41; // Brown
    private static final int MANGROVE_PLANKS_DURABILITY = 150;

    public static ItemStack createMangrovePlanksHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyMangrovePlanksStats(stack, "Helmet", 2, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "mangrove_planks_helmet");
        return stack;
    }
    
    public static ItemStack createMangrovePlanksChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyMangrovePlanksStats(stack, "Chestplate", 5, 3, EquipmentSlot.CHEST);
        setCustomItemId(stack, "mangrove_planks_chestplate");
        return stack;
    }
    
    public static ItemStack createMangrovePlanksLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyMangrovePlanksStats(stack, "Leggings", 3, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "mangrove_planks_leggings");
        return stack;
    }
    
    public static ItemStack createMangrovePlanksBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyMangrovePlanksStats(stack, "Boots", 2, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "mangrove_planks_boots");
        return stack;
    }
    
    private static void applyMangrovePlanksStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(MANGROVE_PLANKS_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Mangrove Planks " + pieceName).withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, MANGROVE_PLANKS_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "mangrove_planks_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "mangrove_planks_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_RED));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.DARK_RED)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.DARK_RED)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Medium").withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.DARK_RED, "mangrove_planks");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Mangrove wood planks").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // CHERRY PLANKS ARMOR
    // ============================================================
    private static final int CHERRY_PLANKS_COLOR = 0xFFB7C5; // Pink
    private static final int CHERRY_PLANKS_DURABILITY = 150;

    public static ItemStack createCherryPlanksHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyCherryPlanksStats(stack, "Helmet", 2, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "cherry_planks_helmet");
        return stack;
    }
    
    public static ItemStack createCherryPlanksChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyCherryPlanksStats(stack, "Chestplate", 5, 3, EquipmentSlot.CHEST);
        setCustomItemId(stack, "cherry_planks_chestplate");
        return stack;
    }
    
    public static ItemStack createCherryPlanksLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyCherryPlanksStats(stack, "Leggings", 3, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "cherry_planks_leggings");
        return stack;
    }
    
    public static ItemStack createCherryPlanksBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyCherryPlanksStats(stack, "Boots", 2, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "cherry_planks_boots");
        return stack;
    }
    
    private static void applyCherryPlanksStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(CHERRY_PLANKS_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Cherry Planks " + pieceName).withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, CHERRY_PLANKS_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "cherry_planks_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "cherry_planks_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.LIGHT_PURPLE));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.LIGHT_PURPLE)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.LIGHT_PURPLE)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Medium").withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.LIGHT_PURPLE, "cherry_planks");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Cherry wood planks").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // BAMBOO PLANKS ARMOR
    // ============================================================
    private static final int BAMBOO_PLANKS_COLOR = 0xEEDD82; // Light gold
    private static final int BAMBOO_PLANKS_DURABILITY = 140;

    public static ItemStack createBambooPlanksHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyBambooPlanksStats(stack, "Helmet", 2, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "bamboo_planks_helmet");
        return stack;
    }
    
    public static ItemStack createBambooPlanksChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyBambooPlanksStats(stack, "Chestplate", 5, 3, EquipmentSlot.CHEST);
        setCustomItemId(stack, "bamboo_planks_chestplate");
        return stack;
    }
    
    public static ItemStack createBambooPlanksLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyBambooPlanksStats(stack, "Leggings", 3, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "bamboo_planks_leggings");
        return stack;
    }
    
    public static ItemStack createBambooPlanksBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyBambooPlanksStats(stack, "Boots", 2, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "bamboo_planks_boots");
        return stack;
    }
    
    private static void applyBambooPlanksStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(BAMBOO_PLANKS_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Bamboo Planks " + pieceName).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, BAMBOO_PLANKS_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "bamboo_planks_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "bamboo_planks_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Medium-Low").withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.YELLOW, "bamboo_planks");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Bamboo planks").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // CRIMSON PLANKS ARMOR
    // ============================================================
    private static final int CRIMSON_PLANKS_COLOR = 0x8B0000; // Dark red
    private static final int CRIMSON_PLANKS_DURABILITY = 150;

    public static ItemStack createCrimsonPlanksHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyCrimsonPlanksStats(stack, "Helmet", 2, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "crimson_planks_helmet");
        return stack;
    }
    
    public static ItemStack createCrimsonPlanksChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyCrimsonPlanksStats(stack, "Chestplate", 5, 3, EquipmentSlot.CHEST);
        setCustomItemId(stack, "crimson_planks_chestplate");
        return stack;
    }
    
    public static ItemStack createCrimsonPlanksLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyCrimsonPlanksStats(stack, "Leggings", 3, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "crimson_planks_leggings");
        return stack;
    }
    
    public static ItemStack createCrimsonPlanksBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyCrimsonPlanksStats(stack, "Boots", 2, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "crimson_planks_boots");
        return stack;
    }
    
    private static void applyCrimsonPlanksStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(CRIMSON_PLANKS_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Crimson Planks " + pieceName).withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, CRIMSON_PLANKS_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "crimson_planks_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "crimson_planks_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_RED));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.DARK_RED)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.DARK_RED)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Medium").withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.DARK_RED, "crimson_planks");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Crimson wood planks").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // WARPED PLANKS ARMOR
    // ============================================================
    private static final int WARPED_PLANKS_COLOR = 0x008080; // Teal
    private static final int WARPED_PLANKS_DURABILITY = 150;

    public static ItemStack createWarpedPlanksHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyWarpedPlanksStats(stack, "Helmet", 2, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "warped_planks_helmet");
        return stack;
    }
    
    public static ItemStack createWarpedPlanksChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyWarpedPlanksStats(stack, "Chestplate", 5, 3, EquipmentSlot.CHEST);
        setCustomItemId(stack, "warped_planks_chestplate");
        return stack;
    }
    
    public static ItemStack createWarpedPlanksLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyWarpedPlanksStats(stack, "Leggings", 3, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "warped_planks_leggings");
        return stack;
    }
    
    public static ItemStack createWarpedPlanksBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyWarpedPlanksStats(stack, "Boots", 2, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "warped_planks_boots");
        return stack;
    }
    
    private static void applyWarpedPlanksStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(WARPED_PLANKS_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Warped Planks " + pieceName).withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, WARPED_PLANKS_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "warped_planks_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "warped_planks_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_AQUA));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.DARK_AQUA)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.DARK_AQUA)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Medium").withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.DARK_AQUA, "warped_planks");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Warped wood planks").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // OAK PLANKS ARMOR
    // ============================================================
    private static final int OAK_PLANKS_COLOR = 0xDEB887; // Burlywood
    private static final int OAK_PLANKS_DURABILITY = 150;

    public static ItemStack createOakPlanksHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyOakPlanksStats(stack, "Helmet", 2, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "oak_planks_helmet");
        return stack;
    }
    
    public static ItemStack createOakPlanksChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyOakPlanksStats(stack, "Chestplate", 5, 3, EquipmentSlot.CHEST);
        setCustomItemId(stack, "oak_planks_chestplate");
        return stack;
    }
    
    public static ItemStack createOakPlanksLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyOakPlanksStats(stack, "Leggings", 3, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "oak_planks_leggings");
        return stack;
    }
    
    public static ItemStack createOakPlanksBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyOakPlanksStats(stack, "Boots", 2, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "oak_planks_boots");
        return stack;
    }
    
    private static void applyOakPlanksStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(OAK_PLANKS_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Oak Planks " + pieceName).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, OAK_PLANKS_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "oak_planks_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "oak_planks_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.GOLD)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.GOLD)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Medium").withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.GOLD, "oak_planks");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Oak wood planks").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // OAK LOG ARMOR
    // ============================================================
    private static final int OAK_LOG_COLOR = 0xC4A35A; // Tan
    private static final int OAK_LOG_DURABILITY = 165;

    public static ItemStack createOakLogHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyOakLogStats(stack, "Helmet", 2, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "oak_log_helmet");
        return stack;
    }
    
    public static ItemStack createOakLogChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyOakLogStats(stack, "Chestplate", 5, 3, EquipmentSlot.CHEST);
        setCustomItemId(stack, "oak_log_chestplate");
        return stack;
    }
    
    public static ItemStack createOakLogLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyOakLogStats(stack, "Leggings", 3, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "oak_log_leggings");
        return stack;
    }
    
    public static ItemStack createOakLogBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyOakLogStats(stack, "Boots", 2, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "oak_log_boots");
        return stack;
    }
    
    private static void applyOakLogStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(OAK_LOG_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Oak Log " + pieceName).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, OAK_LOG_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "oak_log_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "oak_log_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.GOLD)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.GOLD)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("High").withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.GOLD, "oak_log");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Oak log").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // SPRUCE LOG ARMOR
    // ============================================================
    private static final int SPRUCE_LOG_COLOR = 0x8B4513; // Brown
    private static final int SPRUCE_LOG_DURABILITY = 165;

    public static ItemStack createSpruceLogHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applySpruceLogStats(stack, "Helmet", 2, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "spruce_log_helmet");
        return stack;
    }
    
    public static ItemStack createSpruceLogChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applySpruceLogStats(stack, "Chestplate", 5, 3, EquipmentSlot.CHEST);
        setCustomItemId(stack, "spruce_log_chestplate");
        return stack;
    }
    
    public static ItemStack createSpruceLogLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applySpruceLogStats(stack, "Leggings", 3, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "spruce_log_leggings");
        return stack;
    }
    
    public static ItemStack createSpruceLogBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applySpruceLogStats(stack, "Boots", 2, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "spruce_log_boots");
        return stack;
    }
    
    private static void applySpruceLogStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(SPRUCE_LOG_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Spruce Log " + pieceName).withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, SPRUCE_LOG_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "spruce_log_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "spruce_log_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_AQUA));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.DARK_AQUA)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.DARK_AQUA)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("High").withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.DARK_AQUA, "spruce_log");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Spruce log").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // BIRCH LOG ARMOR
    // ============================================================
    private static final int BIRCH_LOG_COLOR = 0xF5F5DC; // Beige
    private static final int BIRCH_LOG_DURABILITY = 165;

    public static ItemStack createBirchLogHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyBirchLogStats(stack, "Helmet", 2, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "birch_log_helmet");
        return stack;
    }
    
    public static ItemStack createBirchLogChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyBirchLogStats(stack, "Chestplate", 5, 3, EquipmentSlot.CHEST);
        setCustomItemId(stack, "birch_log_chestplate");
        return stack;
    }
    
    public static ItemStack createBirchLogLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyBirchLogStats(stack, "Leggings", 3, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "birch_log_leggings");
        return stack;
    }
    
    public static ItemStack createBirchLogBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyBirchLogStats(stack, "Boots", 2, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "birch_log_boots");
        return stack;
    }
    
    private static void applyBirchLogStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(BIRCH_LOG_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Birch Log " + pieceName).withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, BIRCH_LOG_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "birch_log_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "birch_log_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.WHITE)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.WHITE)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("High").withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.WHITE, "birch_log");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Birch log").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // STONE BRICKS ARMOR
    // ============================================================
    private static final int STONE_BRICKS_COLOR = 0x808080; // Gray
    private static final int STONE_BRICKS_DURABILITY = 400;

    public static ItemStack createStoneBricksHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyStoneBricksStats(stack, "Helmet", 3, 4, EquipmentSlot.HEAD);
        setCustomItemId(stack, "stone_bricks_helmet");
        return stack;
    }
    
    public static ItemStack createStoneBricksChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyStoneBricksStats(stack, "Chestplate", 7, 5, EquipmentSlot.CHEST);
        setCustomItemId(stack, "stone_bricks_chestplate");
        return stack;
    }
    
    public static ItemStack createStoneBricksLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyStoneBricksStats(stack, "Leggings", 5, 4, EquipmentSlot.LEGS);
        setCustomItemId(stack, "stone_bricks_leggings");
        return stack;
    }
    
    public static ItemStack createStoneBricksBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyStoneBricksStats(stack, "Boots", 3, 3, EquipmentSlot.FEET);
        setCustomItemId(stack, "stone_bricks_boots");
        return stack;
    }
    
    private static void applyStoneBricksStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(STONE_BRICKS_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Stone Bricks " + pieceName).withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, STONE_BRICKS_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "stone_bricks_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "stone_bricks_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.GRAY)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.GRAY)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Very High").withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.GRAY, "stone_bricks");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Stone bricks").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // COBBLESTONE ARMOR
    // ============================================================
    private static final int COBBLESTONE_COLOR = 0x696969; // Dim gray
    private static final int COBBLESTONE_DURABILITY = 380;

    public static ItemStack createCobblestoneHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyCobblestoneStats(stack, "Helmet", 3, 3, EquipmentSlot.HEAD);
        setCustomItemId(stack, "cobblestone_helmet");
        return stack;
    }
    
    public static ItemStack createCobblestoneChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyCobblestoneStats(stack, "Chestplate", 6, 4, EquipmentSlot.CHEST);
        setCustomItemId(stack, "cobblestone_chestplate");
        return stack;
    }
    
    public static ItemStack createCobblestoneLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyCobblestoneStats(stack, "Leggings", 4, 3, EquipmentSlot.LEGS);
        setCustomItemId(stack, "cobblestone_leggings");
        return stack;
    }
    
    public static ItemStack createCobblestoneBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyCobblestoneStats(stack, "Boots", 3, 2, EquipmentSlot.FEET);
        setCustomItemId(stack, "cobblestone_boots");
        return stack;
    }
    
    private static void applyCobblestoneStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(COBBLESTONE_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Cobblestone " + pieceName).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, COBBLESTONE_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "cobblestone_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "cobblestone_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.DARK_GRAY)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.DARK_GRAY)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Very High").withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.DARK_GRAY, "cobblestone");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Cobblestone").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // MOSSY COBBLESTONE ARMOR
    // ============================================================
    private static final int MOSSY_COBBLESTONE_COLOR = 0x556B2F; // Dark olive green
    private static final int MOSSY_COBBLESTONE_DURABILITY = 350;

    public static ItemStack createMossyCobblestoneHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyMossyCobblestoneStats(stack, "Helmet", 3, 3, EquipmentSlot.HEAD);
        setCustomItemId(stack, "mossy_cobblestone_helmet");
        return stack;
    }
    
    public static ItemStack createMossyCobblestoneChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyMossyCobblestoneStats(stack, "Chestplate", 6, 4, EquipmentSlot.CHEST);
        setCustomItemId(stack, "mossy_cobblestone_chestplate");
        return stack;
    }
    
    public static ItemStack createMossyCobblestoneLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyMossyCobblestoneStats(stack, "Leggings", 4, 3, EquipmentSlot.LEGS);
        setCustomItemId(stack, "mossy_cobblestone_leggings");
        return stack;
    }
    
    public static ItemStack createMossyCobblestoneBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyMossyCobblestoneStats(stack, "Boots", 3, 2, EquipmentSlot.FEET);
        setCustomItemId(stack, "mossy_cobblestone_boots");
        return stack;
    }
    
    private static void applyMossyCobblestoneStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(MOSSY_COBBLESTONE_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Mossy Cobblestone " + pieceName).withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, MOSSY_COBBLESTONE_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "mossy_cobblestone_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "mossy_cobblestone_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_GREEN));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.DARK_GREEN)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.DARK_GREEN)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("High").withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.DARK_GREEN, "mossy_cobblestone");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Mossy cobblestone").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // COBBLED DEEPSLATE ARMOR
    // ============================================================
    private static final int COBBLED_DEEPSLATE_COLOR = 0x2F4F4F; // Dark slate gray
    private static final int COBBLED_DEEPSLATE_DURABILITY = 450;

    public static ItemStack createCobbledDeepslateHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyCobbledDeepslateStats(stack, "Helmet", 4, 5, EquipmentSlot.HEAD);
        setCustomItemId(stack, "cobbled_deepslate_helmet");
        return stack;
    }
    
    public static ItemStack createCobbledDeepslateChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyCobbledDeepslateStats(stack, "Chestplate", 8, 6, EquipmentSlot.CHEST);
        setCustomItemId(stack, "cobbled_deepslate_chestplate");
        return stack;
    }
    
    public static ItemStack createCobbledDeepslateLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyCobbledDeepslateStats(stack, "Leggings", 5, 5, EquipmentSlot.LEGS);
        setCustomItemId(stack, "cobbled_deepslate_leggings");
        return stack;
    }
    
    public static ItemStack createCobbledDeepslateBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyCobbledDeepslateStats(stack, "Boots", 4, 4, EquipmentSlot.FEET);
        setCustomItemId(stack, "cobbled_deepslate_boots");
        return stack;
    }
    
    private static void applyCobbledDeepslateStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(COBBLED_DEEPSLATE_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Cobbled Deepslate " + pieceName).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, COBBLED_DEEPSLATE_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "cobbled_deepslate_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "cobbled_deepslate_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.DARK_GRAY)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.DARK_GRAY)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Extreme").withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.DARK_GRAY, "cobbled_deepslate");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Cobbled deepslate").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // MUD BRICKS ARMOR
    // ============================================================
    private static final int MUD_BRICKS_COLOR = 0x8B4513; // Saddle brown
    private static final int MUD_BRICKS_DURABILITY = 280;

    public static ItemStack createMudBricksHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyMudBricksStats(stack, "Helmet", 2, 3, EquipmentSlot.HEAD);
        setCustomItemId(stack, "mud_bricks_helmet");
        return stack;
    }
    
    public static ItemStack createMudBricksChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyMudBricksStats(stack, "Chestplate", 5, 4, EquipmentSlot.CHEST);
        setCustomItemId(stack, "mud_bricks_chestplate");
        return stack;
    }
    
    public static ItemStack createMudBricksLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyMudBricksStats(stack, "Leggings", 3, 3, EquipmentSlot.LEGS);
        setCustomItemId(stack, "mud_bricks_leggings");
        return stack;
    }
    
    public static ItemStack createMudBricksBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyMudBricksStats(stack, "Boots", 2, 2, EquipmentSlot.FEET);
        setCustomItemId(stack, "mud_bricks_boots");
        return stack;
    }
    
    private static void applyMudBricksStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(MUD_BRICKS_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Mud Bricks " + pieceName).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, MUD_BRICKS_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "mud_bricks_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "mud_bricks_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.GOLD)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.GOLD)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("High").withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.GOLD, "mud_bricks");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Mud bricks").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // MANGROVE ROOTS ARMOR
    // ============================================================
    private static final int MANGROVE_ROOTS_COLOR = 0x5D4037; // Brown
    private static final int MANGROVE_ROOTS_DURABILITY = 160;

    public static ItemStack createMangroveRootsHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyMangroveRootsStats(stack, "Helmet", 2, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "mangrove_roots_helmet");
        return stack;
    }
    
    public static ItemStack createMangroveRootsChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyMangroveRootsStats(stack, "Chestplate", 5, 3, EquipmentSlot.CHEST);
        setCustomItemId(stack, "mangrove_roots_chestplate");
        return stack;
    }
    
    public static ItemStack createMangroveRootsLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyMangroveRootsStats(stack, "Leggings", 3, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "mangrove_roots_leggings");
        return stack;
    }
    
    public static ItemStack createMangroveRootsBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyMangroveRootsStats(stack, "Boots", 2, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "mangrove_roots_boots");
        return stack;
    }
    
    private static void applyMangroveRootsStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(MANGROVE_ROOTS_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Mangrove Roots " + pieceName).withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, MANGROVE_ROOTS_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "mangrove_roots_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "mangrove_roots_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_RED));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.DARK_RED)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.DARK_RED)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("High").withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.DARK_RED, "mangrove_roots");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Mangrove roots").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // MUDDY MANGROVE ROOTS ARMOR
    // ============================================================
    private static final int MUDDY_MANGROVE_ROOTS_COLOR = 0x3E2723; // Dark brown
    private static final int MUDDY_MANGROVE_ROOTS_DURABILITY = 180;

    public static ItemStack createMuddyMangroveRootsHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyMuddyMangroveRootsStats(stack, "Helmet", 2, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "muddy_mangrove_roots_helmet");
        return stack;
    }
    
    public static ItemStack createMuddyMangroveRootsChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyMuddyMangroveRootsStats(stack, "Chestplate", 5, 3, EquipmentSlot.CHEST);
        setCustomItemId(stack, "muddy_mangrove_roots_chestplate");
        return stack;
    }
    
    public static ItemStack createMuddyMangroveRootsLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyMuddyMangroveRootsStats(stack, "Leggings", 3, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "muddy_mangrove_roots_leggings");
        return stack;
    }
    
    public static ItemStack createMuddyMangroveRootsBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyMuddyMangroveRootsStats(stack, "Boots", 2, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "muddy_mangrove_roots_boots");
        return stack;
    }
    
    private static void applyMuddyMangroveRootsStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(MUDDY_MANGROVE_ROOTS_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Muddy Mangrove Roots " + pieceName).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, MUDDY_MANGROVE_ROOTS_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "muddy_mangrove_roots_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "muddy_mangrove_roots_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.DARK_GRAY)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.DARK_GRAY)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("High").withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.DARK_GRAY, "muddy_mangrove_roots");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Muddy mangrove roots").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // NETHERITE BLOCK ARMOR
    // ============================================================
    private static final int NETHERITE_BLOCK_COLOR = 0x4A4A4A; // Dark gray
    private static final int NETHERITE_BLOCK_DURABILITY = 2000;

    public static ItemStack createNetheriteBlockHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyNetheriteBlockStats(stack, "Helmet", 5, 8, EquipmentSlot.HEAD);
        setCustomItemId(stack, "netherite_block_helmet");
        return stack;
    }
    
    public static ItemStack createNetheriteBlockChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyNetheriteBlockStats(stack, "Chestplate", 10, 10, EquipmentSlot.CHEST);
        setCustomItemId(stack, "netherite_block_chestplate");
        return stack;
    }
    
    public static ItemStack createNetheriteBlockLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyNetheriteBlockStats(stack, "Leggings", 7, 8, EquipmentSlot.LEGS);
        setCustomItemId(stack, "netherite_block_leggings");
        return stack;
    }
    
    public static ItemStack createNetheriteBlockBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyNetheriteBlockStats(stack, "Boots", 5, 6, EquipmentSlot.FEET);
        setCustomItemId(stack, "netherite_block_boots");
        return stack;
    }
    
    private static void applyNetheriteBlockStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(NETHERITE_BLOCK_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Netherite Block " + pieceName).withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, NETHERITE_BLOCK_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "netherite_block_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "netherite_block_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_PURPLE));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.DARK_PURPLE)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.DARK_PURPLE)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Extreme").withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.DARK_PURPLE, "netherite_block");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Netherite block").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // CHISELED COPPER ARMOR
    // ============================================================
    private static final int CHISELED_COPPER_COLOR = 0xB87333; // Copper
    private static final int CHISELED_COPPER_DURABILITY = 200;

    public static ItemStack createChiseledCopperHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyChiseledCopperStats(stack, "Helmet", 2, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "chiseled_copper_helmet");
        return stack;
    }
    
    public static ItemStack createChiseledCopperChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyChiseledCopperStats(stack, "Chestplate", 5, 3, EquipmentSlot.CHEST);
        setCustomItemId(stack, "chiseled_copper_chestplate");
        return stack;
    }
    
    public static ItemStack createChiseledCopperLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyChiseledCopperStats(stack, "Leggings", 3, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "chiseled_copper_leggings");
        return stack;
    }
    
    public static ItemStack createChiseledCopperBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyChiseledCopperStats(stack, "Boots", 2, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "chiseled_copper_boots");
        return stack;
    }
    
    private static void applyChiseledCopperStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(CHISELED_COPPER_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Chiseled Copper " + pieceName).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, CHISELED_COPPER_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "chiseled_copper_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "chiseled_copper_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.GOLD)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.GOLD)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Medium").withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.GOLD, "chiseled_copper");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Chiseled copper").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // CUT COPPER ARMOR
    // ============================================================
    private static final int CUT_COPPER_COLOR = 0xD9874E; // Light copper
    private static final int CUT_COPPER_DURABILITY = 180;

    public static ItemStack createCutCopperHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyCutCopperStats(stack, "Helmet", 2, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "cut_copper_helmet");
        return stack;
    }
    
    public static ItemStack createCutCopperChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyCutCopperStats(stack, "Chestplate", 5, 3, EquipmentSlot.CHEST);
        setCustomItemId(stack, "cut_copper_chestplate");
        return stack;
    }
    
    public static ItemStack createCutCopperLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyCutCopperStats(stack, "Leggings", 3, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "cut_copper_leggings");
        return stack;
    }
    
    public static ItemStack createCutCopperBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyCutCopperStats(stack, "Boots", 2, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "cut_copper_boots");
        return stack;
    }
    
    private static void applyCutCopperStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(CUT_COPPER_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Cut Copper " + pieceName).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, CUT_COPPER_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "cut_copper_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "cut_copper_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.GOLD)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.GOLD)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Medium").withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.GOLD, "cut_copper");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Cut copper").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // EXPOSED COPPER ARMOR
    // ============================================================
    private static final int EXPOSED_COPPER_COLOR = 0xA0522D; // Sienna (weathered)
    private static final int EXPOSED_COPPER_DURABILITY = 170;

    public static ItemStack createExposedCopperHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyExposedCopperStats(stack, "Helmet", 2, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "exposed_copper_helmet");
        return stack;
    }
    
    public static ItemStack createExposedCopperChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyExposedCopperStats(stack, "Chestplate", 5, 3, EquipmentSlot.CHEST);
        setCustomItemId(stack, "exposed_copper_chestplate");
        return stack;
    }
    
    public static ItemStack createExposedCopperLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyExposedCopperStats(stack, "Leggings", 3, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "exposed_copper_leggings");
        return stack;
    }
    
    public static ItemStack createExposedCopperBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyExposedCopperStats(stack, "Boots", 2, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "exposed_copper_boots");
        return stack;
    }
    
    private static void applyExposedCopperStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(EXPOSED_COPPER_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Exposed Copper " + pieceName).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, EXPOSED_COPPER_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "exposed_copper_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "exposed_copper_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.GOLD)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.GOLD)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Medium").withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.GOLD, "exposed_copper");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Exposed copper").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // WEATHERED COPPER ARMOR
    // ============================================================
    private static final int WEATHERED_COPPER_COLOR = 0x8B4513; // Darker patina
    private static final int WEATHERED_COPPER_DURABILITY = 160;

    public static ItemStack createWeatheredCopperHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyWeatheredCopperStats(stack, "Helmet", 2, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "weathered_copper_helmet");
        return stack;
    }
    
    public static ItemStack createWeatheredCopperChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyWeatheredCopperStats(stack, "Chestplate", 5, 3, EquipmentSlot.CHEST);
        setCustomItemId(stack, "weathered_copper_chestplate");
        return stack;
    }
    
    public static ItemStack createWeatheredCopperLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyWeatheredCopperStats(stack, "Leggings", 3, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "weathered_copper_leggings");
        return stack;
    }
    
    public static ItemStack createWeatheredCopperBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyWeatheredCopperStats(stack, "Boots", 2, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "weathered_copper_boots");
        return stack;
    }
    
    private static void applyWeatheredCopperStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(WEATHERED_COPPER_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Weathered Copper " + pieceName).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, WEATHERED_COPPER_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "weathered_copper_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "weathered_copper_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.GOLD)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.GOLD)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Medium").withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.GOLD, "weathered_copper");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Weathered copper").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // OXIDISED COPPER ARMOR
    // ============================================================
    private static final int OXIDISED_COPPER_COLOR = 0x4A7C59; // Verdigris green
    private static final int OXIDISED_COPPER_DURABILITY = 150;

    public static ItemStack createOxidisedCopperHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyOxidisedCopperStats(stack, "Helmet", 2, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "oxidised_copper_helmet");
        return stack;
    }
    
    public static ItemStack createOxidisedCopperChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyOxidisedCopperStats(stack, "Chestplate", 5, 3, EquipmentSlot.CHEST);
        setCustomItemId(stack, "oxidised_copper_chestplate");
        return stack;
    }
    
    public static ItemStack createOxidisedCopperLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyOxidisedCopperStats(stack, "Leggings", 3, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "oxidised_copper_leggings");
        return stack;
    }
    
    public static ItemStack createOxidisedCopperBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyOxidisedCopperStats(stack, "Boots", 2, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "oxidised_copper_boots");
        return stack;
    }
    
    private static void applyOxidisedCopperStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(OXIDISED_COPPER_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Oxidised Copper " + pieceName).withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, OXIDISED_COPPER_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "oxidised_copper_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "oxidised_copper_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_GREEN));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.DARK_GREEN)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.DARK_GREEN)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Medium").withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.DARK_GREEN, "oxidised_copper");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Oxidised copper").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // WAXED CUT COPPER ARMOR
    // ============================================================
    private static final int WAXED_CUT_COPPER_COLOR = 0xE8A857; // Shiny copper
    private static final int WAXED_CUT_COPPER_DURABILITY = 190;

    public static ItemStack createWaxedCutCopperHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyWaxedCutCopperStats(stack, "Helmet", 2, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "waxed_cut_copper_helmet");
        return stack;
    }
    
    public static ItemStack createWaxedCutCopperChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyWaxedCutCopperStats(stack, "Chestplate", 5, 3, EquipmentSlot.CHEST);
        setCustomItemId(stack, "waxed_cut_copper_chestplate");
        return stack;
    }
    
    public static ItemStack createWaxedCutCopperLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyWaxedCutCopperStats(stack, "Leggings", 3, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "waxed_cut_copper_leggings");
        return stack;
    }
    
    public static ItemStack createWaxedCutCopperBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyWaxedCutCopperStats(stack, "Boots", 2, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "waxed_cut_copper_boots");
        return stack;
    }
    
    private static void applyWaxedCutCopperStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(WAXED_CUT_COPPER_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Waxed Cut Copper " + pieceName).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, WAXED_CUT_COPPER_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "waxed_cut_copper_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "waxed_cut_copper_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.GOLD)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.GOLD)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Medium").withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.GOLD, "waxed_cut_copper");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Waxed cut copper").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // POLISHED BASALT ARMOR
    // ============================================================
    private static final int POLISHED_BASALT_COLOR = 0x3D3D3D; // Dark gray
    private static final int POLISHED_BASALT_DURABILITY = 420;

    public static ItemStack createPolishedBasaltHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyPolishedBasaltStats(stack, "Helmet", 3, 4, EquipmentSlot.HEAD);
        setCustomItemId(stack, "polished_basalt_helmet");
        return stack;
    }
    
    public static ItemStack createPolishedBasaltChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyPolishedBasaltStats(stack, "Chestplate", 7, 5, EquipmentSlot.CHEST);
        setCustomItemId(stack, "polished_basalt_chestplate");
        return stack;
    }
    
    public static ItemStack createPolishedBasaltLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyPolishedBasaltStats(stack, "Leggings", 5, 4, EquipmentSlot.LEGS);
        setCustomItemId(stack, "polished_basalt_leggings");
        return stack;
    }
    
    public static ItemStack createPolishedBasaltBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyPolishedBasaltStats(stack, "Boots", 3, 3, EquipmentSlot.FEET);
        setCustomItemId(stack, "polished_basalt_boots");
        return stack;
    }
    
    private static void applyPolishedBasaltStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(POLISHED_BASALT_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Polished Basalt " + pieceName).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, POLISHED_BASALT_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "polished_basalt_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "polished_basalt_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.DARK_GRAY)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.DARK_GRAY)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Very High").withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.DARK_GRAY, "polished_basalt");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Polished basalt").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // VERDANT FROGLIGHT ARMOR
    // ============================================================
    private static final int VERDANT_FROGLIGHT_COLOR = 0x7CFC00; // Lawn green
    private static final int VERDANT_FROGLIGHT_DURABILITY = 300;

    public static ItemStack createVerdantFroglightHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyVerdantFroglightStats(stack, "Helmet", 3, 3, EquipmentSlot.HEAD);
        setCustomItemId(stack, "verdant_froglight_helmet");
        return stack;
    }
    
    public static ItemStack createVerdantFroglightChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyVerdantFroglightStats(stack, "Chestplate", 6, 4, EquipmentSlot.CHEST);
        setCustomItemId(stack, "verdant_froglight_chestplate");
        return stack;
    }
    
    public static ItemStack createVerdantFroglightLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyVerdantFroglightStats(stack, "Leggings", 4, 3, EquipmentSlot.LEGS);
        setCustomItemId(stack, "verdant_froglight_leggings");
        return stack;
    }
    
    public static ItemStack createVerdantFroglightBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyVerdantFroglightStats(stack, "Boots", 3, 2, EquipmentSlot.FEET);
        setCustomItemId(stack, "verdant_froglight_boots");
        return stack;
    }
    
    private static void applyVerdantFroglightStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(VERDANT_FROGLIGHT_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Verdant Froglight " + pieceName).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, VERDANT_FROGLIGHT_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "verdant_froglight_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "verdant_froglight_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("High").withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.GREEN, "verdant_froglight");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Verdant froglight").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // PEARLESCENT FROGLIGHT ARMOR
    // ============================================================
    private static final int PEARLESCENT_FROGLIGHT_COLOR = 0xFF69B4; // Hot pink
    private static final int PEARLESCENT_FROGLIGHT_DURABILITY = 300;

    public static ItemStack createPearlescentFroglightHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyPearlescentFroglightStats(stack, "Helmet", 3, 3, EquipmentSlot.HEAD);
        setCustomItemId(stack, "pearlescent_froglight_helmet");
        return stack;
    }
    
    public static ItemStack createPearlescentFroglightChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyPearlescentFroglightStats(stack, "Chestplate", 6, 4, EquipmentSlot.CHEST);
        setCustomItemId(stack, "pearlescent_froglight_chestplate");
        return stack;
    }
    
    public static ItemStack createPearlescentFroglightLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyPearlescentFroglightStats(stack, "Leggings", 4, 3, EquipmentSlot.LEGS);
        setCustomItemId(stack, "pearlescent_froglight_leggings");
        return stack;
    }
    
    public static ItemStack createPearlescentFroglightBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyPearlescentFroglightStats(stack, "Boots", 3, 2, EquipmentSlot.FEET);
        setCustomItemId(stack, "pearlescent_froglight_boots");
        return stack;
    }
    
    private static void applyPearlescentFroglightStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(PEARLESCENT_FROGLIGHT_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Pearlescent Froglight " + pieceName).withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, PEARLESCENT_FROGLIGHT_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "pearlescent_froglight_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "pearlescent_froglight_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.LIGHT_PURPLE));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.LIGHT_PURPLE)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.LIGHT_PURPLE)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("High").withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.LIGHT_PURPLE, "pearlescent_froglight");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Pearlescent froglight").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // OCHRE FROGLIGHT ARMOR
    // ============================================================
    private static final int OCHRE_FROGLIGHT_COLOR = 0xFFA500; // Orange
    private static final int OCHRE_FROGLIGHT_DURABILITY = 300;

    public static ItemStack createOchreFroglightHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyOchreFroglightStats(stack, "Helmet", 3, 3, EquipmentSlot.HEAD);
        setCustomItemId(stack, "ochre_froglight_helmet");
        return stack;
    }
    
    public static ItemStack createOchreFroglightChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyOchreFroglightStats(stack, "Chestplate", 6, 4, EquipmentSlot.CHEST);
        setCustomItemId(stack, "ochre_froglight_chestplate");
        return stack;
    }
    
    public static ItemStack createOchreFroglightLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyOchreFroglightStats(stack, "Leggings", 4, 3, EquipmentSlot.LEGS);
        setCustomItemId(stack, "ochre_froglight_leggings");
        return stack;
    }
    
    public static ItemStack createOchreFroglightBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyOchreFroglightStats(stack, "Boots", 3, 2, EquipmentSlot.FEET);
        setCustomItemId(stack, "ochre_froglight_boots");
        return stack;
    }
    
    private static void applyOchreFroglightStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(OCHRE_FROGLIGHT_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Ochre Froglight " + pieceName).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, OCHRE_FROGLIGHT_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "ochre_froglight_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "ochre_froglight_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.GOLD)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.GOLD)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("High").withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.GOLD, "ochre_froglight");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Ochre froglight").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // IRON NUGGET ARMOR
    // ============================================================
    private static final int IRON_NUGGET_COLOR = 0xD1D1D1; // Light gray
    private static final int IRON_NUGGET_DURABILITY = 120;

    public static ItemStack createIronNuggetHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyIronNuggetStats(stack, "Helmet", 2, 1, EquipmentSlot.HEAD);
        setCustomItemId(stack, "iron_nugget_helmet");
        return stack;
    }
    
    public static ItemStack createIronNuggetChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyIronNuggetStats(stack, "Chestplate", 4, 2, EquipmentSlot.CHEST);
        setCustomItemId(stack, "iron_nugget_chestplate");
        return stack;
    }
    
    public static ItemStack createIronNuggetLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyIronNuggetStats(stack, "Leggings", 3, 1, EquipmentSlot.LEGS);
        setCustomItemId(stack, "iron_nugget_leggings");
        return stack;
    }
    
    public static ItemStack createIronNuggetBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyIronNuggetStats(stack, "Boots", 2, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "iron_nugget_boots");
        return stack;
    }
    
    private static void applyIronNuggetStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(IRON_NUGGET_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Iron Nugget " + pieceName).withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, IRON_NUGGET_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "iron_nugget_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "iron_nugget_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.WHITE)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.WHITE)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Low").withStyle(ChatFormatting.RED)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.WHITE, "iron_nugget");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Iron nugget").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // GOLD NUGGET ARMOR
    // ============================================================
    private static final int GOLD_NUGGET_COLOR = 0xFFD700; // Gold
    private static final int GOLD_NUGGET_DURABILITY = 80;

    public static ItemStack createGoldNuggetHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyGoldNuggetStats(stack, "Helmet", 2, 1, EquipmentSlot.HEAD);
        setCustomItemId(stack, "gold_nugget_helmet");
        return stack;
    }
    
    public static ItemStack createGoldNuggetChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyGoldNuggetStats(stack, "Chestplate", 4, 2, EquipmentSlot.CHEST);
        setCustomItemId(stack, "gold_nugget_chestplate");
        return stack;
    }
    
    public static ItemStack createGoldNuggetLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyGoldNuggetStats(stack, "Leggings", 3, 1, EquipmentSlot.LEGS);
        setCustomItemId(stack, "gold_nugget_leggings");
        return stack;
    }
    
    public static ItemStack createGoldNuggetBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyGoldNuggetStats(stack, "Boots", 2, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "gold_nugget_boots");
        return stack;
    }
    
    private static void applyGoldNuggetStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(GOLD_NUGGET_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Gold Nugget " + pieceName).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, GOLD_NUGGET_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "gold_nugget_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "gold_nugget_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.GOLD)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.GOLD)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Very Low").withStyle(ChatFormatting.RED)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.GOLD, "gold_nugget");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Gold nugget").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // COPPER INGOT ARMOR
    // ============================================================
    private static final int COPPER_INGOT_COLOR = 0xB87333; // Copper
    private static final int COPPER_INGOT_DURABILITY = 140;

    public static ItemStack createCopperIngotHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyCopperIngotStats(stack, "Helmet", 2, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "copper_ingot_helmet");
        return stack;
    }
    
    public static ItemStack createCopperIngotChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyCopperIngotStats(stack, "Chestplate", 4, 2, EquipmentSlot.CHEST);
        setCustomItemId(stack, "copper_ingot_chestplate");
        return stack;
    }
    
    public static ItemStack createCopperIngotLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyCopperIngotStats(stack, "Leggings", 3, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "copper_ingot_leggings");
        return stack;
    }
    
    public static ItemStack createCopperIngotBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyCopperIngotStats(stack, "Boots", 2, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "copper_ingot_boots");
        return stack;
    }
    
    private static void applyCopperIngotStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(COPPER_INGOT_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Copper Ingot " + pieceName).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, COPPER_INGOT_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "copper_ingot_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "copper_ingot_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.GOLD)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.GOLD)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Low").withStyle(ChatFormatting.RED)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.GOLD, "copper_ingot");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Copper ingot").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // EMERALD ARMOR
    // ============================================================
    private static final int EMERALD_COLOR = 0x50C878; // Emerald green
    private static final int EMERALD_DURABILITY = 350;

    public static ItemStack createEmeraldHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyEmeraldStats(stack, "Helmet", 3, 3, EquipmentSlot.HEAD);
        setCustomItemId(stack, "emerald_helmet");
        return stack;
    }
    
    public static ItemStack createEmeraldChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyEmeraldStats(stack, "Chestplate", 6, 4, EquipmentSlot.CHEST);
        setCustomItemId(stack, "emerald_chestplate");
        return stack;
    }
    
    public static ItemStack createEmeraldLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyEmeraldStats(stack, "Leggings", 4, 3, EquipmentSlot.LEGS);
        setCustomItemId(stack, "emerald_leggings");
        return stack;
    }
    
    public static ItemStack createEmeraldBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyEmeraldStats(stack, "Boots", 3, 2, EquipmentSlot.FEET);
        setCustomItemId(stack, "emerald_boots");
        return stack;
    }
    
    private static void applyEmeraldStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(EMERALD_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Emerald " + pieceName).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, EMERALD_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "emerald_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "emerald_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("High").withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.GREEN, "emerald");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Emerald").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // LAPIS LAZULI ARMOR
    // ============================================================
    private static final int LAPIS_LAZULI_COLOR = 0x1E90FF; // Dodger blue
    private static final int LAPIS_LAZULI_DURABILITY = 250;

    public static ItemStack createLapisLazuliHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyLapisLazuliStats(stack, "Helmet", 2, 3, EquipmentSlot.HEAD);
        setCustomItemId(stack, "lapis_lazuli_helmet");
        return stack;
    }
    
    public static ItemStack createLapisLazuliChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyLapisLazuliStats(stack, "Chestplate", 5, 4, EquipmentSlot.CHEST);
        setCustomItemId(stack, "lapis_lazuli_chestplate");
        return stack;
    }
    
    public static ItemStack createLapisLazuliLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyLapisLazuliStats(stack, "Leggings", 3, 3, EquipmentSlot.LEGS);
        setCustomItemId(stack, "lapis_lazuli_leggings");
        return stack;
    }
    
    public static ItemStack createLapisLazuliBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyLapisLazuliStats(stack, "Boots", 2, 2, EquipmentSlot.FEET);
        setCustomItemId(stack, "lapis_lazuli_boots");
        return stack;
    }
    
    private static void applyLapisLazuliStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(LAPIS_LAZULI_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Lapis Lazuli " + pieceName).withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, LAPIS_LAZULI_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "lapis_lazuli_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "lapis_lazuli_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.BLUE));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.BLUE)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.BLUE)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Medium").withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.BLUE, "lapis_lazuli");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Lapis lazuli").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // AMETHYST SHARD ARMOR
    // ============================================================
    private static final int AMETHYST_SHARD_COLOR = 0x9966CC; // Purple
    private static final int AMETHYST_SHARD_DURABILITY = 300;

    public static ItemStack createAmethystShardHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyAmethystShardStats(stack, "Helmet", 3, 3, EquipmentSlot.HEAD);
        setCustomItemId(stack, "amethyst_shard_helmet");
        return stack;
    }
    
    public static ItemStack createAmethystShardChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyAmethystShardStats(stack, "Chestplate", 6, 4, EquipmentSlot.CHEST);
        setCustomItemId(stack, "amethyst_shard_chestplate");
        return stack;
    }
    
    public static ItemStack createAmethystShardLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyAmethystShardStats(stack, "Leggings", 4, 3, EquipmentSlot.LEGS);
        setCustomItemId(stack, "amethyst_shard_leggings");
        return stack;
    }
    
    public static ItemStack createAmethystShardBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyAmethystShardStats(stack, "Boots", 3, 2, EquipmentSlot.FEET);
        setCustomItemId(stack, "amethyst_shard_boots");
        return stack;
    }
    
    private static void applyAmethystShardStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(AMETHYST_SHARD_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Amethyst Shard " + pieceName).withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, AMETHYST_SHARD_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "amethyst_shard_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "amethyst_shard_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_PURPLE));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.DARK_PURPLE)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.DARK_PURPLE)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("High").withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.DARK_PURPLE, "amethyst_shard");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Amethyst shard").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // FLINT ARMOR
    // ============================================================
    private static final int FLINT_COLOR = 0x2F4F4F; // Dark slate gray
    private static final int FLINT_DURABILITY = 160;

    public static ItemStack createFlintHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyFlintStats(stack, "Helmet", 2, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "flint_helmet");
        return stack;
    }
    
    public static ItemStack createFlintChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyFlintStats(stack, "Chestplate", 5, 3, EquipmentSlot.CHEST);
        setCustomItemId(stack, "flint_chestplate");
        return stack;
    }
    
    public static ItemStack createFlintLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyFlintStats(stack, "Leggings", 3, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "flint_leggings");
        return stack;
    }
    
    public static ItemStack createFlintBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyFlintStats(stack, "Boots", 2, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "flint_boots");
        return stack;
    }
    
    private static void applyFlintStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(FLINT_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Flint " + pieceName).withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, FLINT_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "flint_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "flint_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.GRAY)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.GRAY)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Medium").withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.GRAY, "flint");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Flint").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // BONE MEAL ARMOR
    // ============================================================
    private static final int BONE_MEAL_COLOR = 0xFFFAF0; // Floral white
    private static final int BONE_MEAL_DURABILITY = 100;

    public static ItemStack createBoneMealHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyBoneMealStats(stack, "Helmet", 2, 1, EquipmentSlot.HEAD);
        setCustomItemId(stack, "bone_meal_helmet");
        return stack;
    }
    
    public static ItemStack createBoneMealChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyBoneMealStats(stack, "Chestplate", 4, 2, EquipmentSlot.CHEST);
        setCustomItemId(stack, "bone_meal_chestplate");
        return stack;
    }
    
    public static ItemStack createBoneMealLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyBoneMealStats(stack, "Leggings", 3, 1, EquipmentSlot.LEGS);
        setCustomItemId(stack, "bone_meal_leggings");
        return stack;
    }
    
    public static ItemStack createBoneMealBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyBoneMealStats(stack, "Boots", 2, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "bone_meal_boots");
        return stack;
    }
    
    private static void applyBoneMealStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(BONE_MEAL_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Bone Meal " + pieceName).withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, BONE_MEAL_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "bone_meal_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "bone_meal_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.WHITE)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.WHITE)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Very Low").withStyle(ChatFormatting.RED)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.WHITE, "bone_meal");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Bone meal").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // CHARCOAL ARMOR
    // ============================================================
    private static final int CHARCOAL_COLOR = 0x1C1C1C; // Near black
    private static final int CHARCOAL_DURABILITY = 130;

    public static ItemStack createCharcoalHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyCharcoalStats(stack, "Helmet", 2, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "charcoal_helmet");
        return stack;
    }
    
    public static ItemStack createCharcoalChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyCharcoalStats(stack, "Chestplate", 4, 2, EquipmentSlot.CHEST);
        setCustomItemId(stack, "charcoal_chestplate");
        return stack;
    }
    
    public static ItemStack createCharcoalLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyCharcoalStats(stack, "Leggings", 3, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "charcoal_leggings");
        return stack;
    }
    
    public static ItemStack createCharcoalBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyCharcoalStats(stack, "Boots", 2, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "charcoal_boots");
        return stack;
    }
    
    private static void applyCharcoalStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(CHARCOAL_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Charcoal " + pieceName).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, CHARCOAL_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "charcoal_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "charcoal_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.DARK_GRAY)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.DARK_GRAY)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Low").withStyle(ChatFormatting.RED)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.DARK_GRAY, "charcoal");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Charcoal").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // SNOW BLOCK ARMOR
    // ============================================================
    private static final int SNOW_BLOCK_COLOR = 0xFFFAFA; // Snow white
    private static final int SNOW_BLOCK_DURABILITY = 120;

    public static ItemStack createSnowBlockHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applySnowBlockStats(stack, "Helmet", 2, 1, EquipmentSlot.HEAD);
        setCustomItemId(stack, "snow_block_helmet");
        return stack;
    }
    
    public static ItemStack createSnowBlockChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applySnowBlockStats(stack, "Chestplate", 4, 2, EquipmentSlot.CHEST);
        setCustomItemId(stack, "snow_block_chestplate");
        return stack;
    }
    
    public static ItemStack createSnowBlockLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applySnowBlockStats(stack, "Leggings", 3, 1, EquipmentSlot.LEGS);
        setCustomItemId(stack, "snow_block_leggings");
        return stack;
    }
    
    public static ItemStack createSnowBlockBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applySnowBlockStats(stack, "Boots", 2, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "snow_block_boots");
        return stack;
    }
    
    private static void applySnowBlockStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(SNOW_BLOCK_COLOR));
        
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Snow Block " + pieceName).withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, SNOW_BLOCK_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "snow_block_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "snow_block_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.WHITE)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.WHITE)));
        lore.add(Component.literal("⚠ Durability: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Low").withStyle(ChatFormatting.RED)));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.WHITE, "snow_block");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Snow block").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // MISSING ARMOR SETS - For weapons without armor
    // ============================================================

    // LILY PAD ARMOR
    public static ItemStack createLilyPadHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyLilyPadStats(stack, "Helmet", 1, 0, EquipmentSlot.HEAD);
        setCustomItemId(stack, "lily_pad_helmet");
        return stack;
    }
    public static ItemStack createLilyPadChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyLilyPadStats(stack, "Chestplate", 3, 0, EquipmentSlot.CHEST);
        setCustomItemId(stack, "lily_pad_chestplate");
        return stack;
    }
    public static ItemStack createLilyPadLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyLilyPadStats(stack, "Leggings", 2, 0, EquipmentSlot.LEGS);
        setCustomItemId(stack, "lily_pad_leggings");
        return stack;
    }
    public static ItemStack createLilyPadBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyLilyPadStats(stack, "Boots", 1, 0, EquipmentSlot.FEET);
        setCustomItemId(stack, "lily_pad_boots");
        return stack;
    }
    private static void applyLilyPadStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(0x2E8B57));
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Lily Pad " + pieceName).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, 80);
        stack.set(DataComponents.DAMAGE, 0);
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "lily_pad_" + pieceName.toLowerCase() + "_armor"), armorValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("🛡 Armor: +" + armorValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.LIGHT_PURPLE, "lily_pad");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Floating protection").withStyle(ChatFormatting.DARK_GRAY));
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // MELON ARMOR
    public static ItemStack createMelonHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyMelonStats(stack, "Helmet", 2, 1, EquipmentSlot.HEAD);
        setCustomItemId(stack, "melon_helmet");
        return stack;
    }
    public static ItemStack createMelonChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyMelonStats(stack, "Chestplate", 6, 1, EquipmentSlot.CHEST);
        setCustomItemId(stack, "melon_chestplate");
        return stack;
    }
    public static ItemStack createMelonLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyMelonStats(stack, "Leggings", 5, 1, EquipmentSlot.LEGS);
        setCustomItemId(stack, "melon_leggings");
        return stack;
    }
    public static ItemStack createMelonBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyMelonStats(stack, "Boots", 2, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "melon_boots");
        return stack;
    }
    private static void applyMelonStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(0x4C8B2F));
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Melon " + pieceName).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, 150);
        stack.set(DataComponents.DAMAGE, 0);
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "melon_" + pieceName.toLowerCase() + "_armor"), armorValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "melon_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("🛡 Armor: +" + armorValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal("💎 Toughness: +" + toughnessValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.LIGHT_PURPLE, "melon");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Refreshing protection").withStyle(ChatFormatting.DARK_GRAY));
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // MOSS BLOCK ARMOR
    public static ItemStack createMossBlockHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyMossBlockStats(stack, "Helmet", 2, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "moss_block_helmet");
        return stack;
    }
    public static ItemStack createMossBlockChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyMossBlockStats(stack, "Chestplate", 6, 2, EquipmentSlot.CHEST);
        setCustomItemId(stack, "moss_block_chestplate");
        return stack;
    }
    public static ItemStack createMossBlockLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyMossBlockStats(stack, "Leggings", 5, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "moss_block_leggings");
        return stack;
    }
    public static ItemStack createMossBlockBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyMossBlockStats(stack, "Boots", 2, 2, EquipmentSlot.FEET);
        setCustomItemId(stack, "moss_block_boots");
        return stack;
    }
    private static void applyMossBlockStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(0x6B8E23));
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Moss Block " + pieceName).withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, 200);
        stack.set(DataComponents.DAMAGE, 0);
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "moss_block_" + pieceName.toLowerCase() + "_armor"), armorValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "moss_block_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_GREEN));
        lore.add(Component.literal("🛡 Armor: +" + armorValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal("💎 Toughness: +" + toughnessValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.LIGHT_PURPLE, "moss_block");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Ancient growth").withStyle(ChatFormatting.DARK_GRAY));
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // MYCELIUM ARMOR
    public static ItemStack createMyceliumHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyMyceliumStats(stack, "Helmet", 2, 1, EquipmentSlot.HEAD);
        setCustomItemId(stack, "mycelium_helmet");
        return stack;
    }
    public static ItemStack createMyceliumChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyMyceliumStats(stack, "Chestplate", 6, 1, EquipmentSlot.CHEST);
        setCustomItemId(stack, "mycelium_chestplate");
        return stack;
    }
    public static ItemStack createMyceliumLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyMyceliumStats(stack, "Leggings", 5, 1, EquipmentSlot.LEGS);
        setCustomItemId(stack, "mycelium_leggings");
        return stack;
    }
    public static ItemStack createMyceliumBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyMyceliumStats(stack, "Boots", 2, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "mycelium_boots");
        return stack;
    }
    private static void applyMyceliumStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(0x8B4513));
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Mycelium " + pieceName).withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, 180);
        stack.set(DataComponents.DAMAGE, 0);
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "mycelium_" + pieceName.toLowerCase() + "_armor"), armorValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "mycelium_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_PURPLE));
        lore.add(Component.literal("🛡 Armor: +" + armorValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal("💎 Toughness: +" + toughnessValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.LIGHT_PURPLE, "mycelium");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Fungal network").withStyle(ChatFormatting.DARK_GRAY));
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // PUMPKIN ARMOR
    public static ItemStack createPumpkinHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyPumpkinStats(stack, "Helmet", 2, 1, EquipmentSlot.HEAD);
        setCustomItemId(stack, "pumpkin_helmet");
        return stack;
    }
    public static ItemStack createPumpkinChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyPumpkinStats(stack, "Chestplate", 6, 1, EquipmentSlot.CHEST);
        setCustomItemId(stack, "pumpkin_chestplate");
        return stack;
    }
    public static ItemStack createPumpkinLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyPumpkinStats(stack, "Leggings", 5, 1, EquipmentSlot.LEGS);
        setCustomItemId(stack, "pumpkin_leggings");
        return stack;
    }
    public static ItemStack createPumpkinBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyPumpkinStats(stack, "Boots", 2, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "pumpkin_boots");
        return stack;
    }
    private static void applyPumpkinStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(0xFF8C00));
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Pumpkin " + pieceName).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, 160);
        stack.set(DataComponents.DAMAGE, 0);
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "pumpkin_" + pieceName.toLowerCase() + "_armor"), armorValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "pumpkin_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("🛡 Armor: +" + armorValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal("💎 Toughness: +" + toughnessValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.LIGHT_PURPLE, "pumpkin");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Harvest guard").withStyle(ChatFormatting.DARK_GRAY));
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // SAND ARMOR
    public static ItemStack createSandHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applySandStats(stack, "Helmet", 1, 0, EquipmentSlot.HEAD);
        setCustomItemId(stack, "sand_helmet");
        return stack;
    }
    public static ItemStack createSandChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applySandStats(stack, "Chestplate", 3, 0, EquipmentSlot.CHEST);
        setCustomItemId(stack, "sand_chestplate");
        return stack;
    }
    public static ItemStack createSandLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applySandStats(stack, "Leggings", 2, 0, EquipmentSlot.LEGS);
        setCustomItemId(stack, "sand_leggings");
        return stack;
    }
    public static ItemStack createSandBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applySandStats(stack, "Boots", 1, 0, EquipmentSlot.FEET);
        setCustomItemId(stack, "sand_boots");
        return stack;
    }
    private static void applySandStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(0xDEB887));
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Sand " + pieceName).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, 60);
        stack.set(DataComponents.DAMAGE, 0);
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "sand_" + pieceName.toLowerCase() + "_armor"), armorValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("🛡 Armor: +" + armorValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.LIGHT_PURPLE, "sand");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Desert drift").withStyle(ChatFormatting.DARK_GRAY));
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // SLIME BLOCK ARMOR
    public static ItemStack createSlimeHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applySlimeStats(stack, "Helmet", 2, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "slime_helmet");
        return stack;
    }
    public static ItemStack createSlimeChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applySlimeStats(stack, "Chestplate", 6, 2, EquipmentSlot.CHEST);
        setCustomItemId(stack, "slime_chestplate");
        return stack;
    }
    public static ItemStack createSlimeLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applySlimeStats(stack, "Leggings", 5, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "slime_leggings");
        return stack;
    }
    public static ItemStack createSlimeBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applySlimeStats(stack, "Boots", 2, 2, EquipmentSlot.FEET);
        setCustomItemId(stack, "slime_boots");
        return stack;
    }
    private static void applySlimeStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(0x7FFF00));
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Slime " + pieceName).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, 250);
        stack.set(DataComponents.DAMAGE, 0);
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "slime_" + pieceName.toLowerCase() + "_armor"), armorValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "slime_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.GREEN));
        lore.add(Component.literal("🛡 Armor: +" + armorValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal("💎 Toughness: +" + toughnessValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.LIGHT_PURPLE, "slime");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Bouncy protection").withStyle(ChatFormatting.DARK_GRAY));
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // TARGET ARMOR
    public static ItemStack createTargetHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyTargetStats(stack, "Helmet", 2, 1, EquipmentSlot.HEAD);
        setCustomItemId(stack, "target_helmet");
        return stack;
    }
    public static ItemStack createTargetChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyTargetStats(stack, "Chestplate", 6, 1, EquipmentSlot.CHEST);
        setCustomItemId(stack, "target_chestplate");
        return stack;
    }
    public static ItemStack createTargetLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyTargetStats(stack, "Leggings", 5, 1, EquipmentSlot.LEGS);
        setCustomItemId(stack, "target_leggings");
        return stack;
    }
    public static ItemStack createTargetBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyTargetStats(stack, "Boots", 2, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "target_boots");
        return stack;
    }
    private static void applyTargetStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(0xFF0000));
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Target " + pieceName).withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, 180);
        stack.set(DataComponents.DAMAGE, 0);
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "target_" + pieceName.toLowerCase() + "_armor"), armorValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "target_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("🛡 Armor: +" + armorValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal("💎 Toughness: +" + toughnessValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.LIGHT_PURPLE, "target");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Bullseye defense").withStyle(ChatFormatting.DARK_GRAY));
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // TNT ARMOR
    public static ItemStack createTntHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyTntStats(stack, "Helmet", 3, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "tnt_helmet");
        return stack;
    }
    public static ItemStack createTntChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyTntStats(stack, "Chestplate", 8, 2, EquipmentSlot.CHEST);
        setCustomItemId(stack, "tnt_chestplate");
        return stack;
    }
    public static ItemStack createTntLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyTntStats(stack, "Leggings", 6, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "tnt_leggings");
        return stack;
    }
    public static ItemStack createTntBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyTntStats(stack, "Boots", 3, 2, EquipmentSlot.FEET);
        setCustomItemId(stack, "tnt_boots");
        return stack;
    }
    private static void applyTntStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(0x8B0000));
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("TNT " + pieceName).withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, 100);
        stack.set(DataComponents.DAMAGE, 0);
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "tnt_" + pieceName.toLowerCase() + "_armor"), armorValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "tnt_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_RED));
        lore.add(Component.literal("🛡 Armor: +" + armorValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal("💎 Toughness: +" + toughnessValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.LIGHT_PURPLE, "tnt");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Boom protection").withStyle(ChatFormatting.DARK_GRAY));
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // WARPED STEM ARMOR
    public static ItemStack createWarpedHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyWarpedStats(stack, "Helmet", 2, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "warped_helmet");
        return stack;
    }
    public static ItemStack createWarpedChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyWarpedStats(stack, "Chestplate", 6, 2, EquipmentSlot.CHEST);
        setCustomItemId(stack, "warped_chestplate");
        return stack;
    }
    public static ItemStack createWarpedLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyWarpedStats(stack, "Leggings", 5, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "warped_leggings");
        return stack;
    }
    public static ItemStack createWarpedBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyWarpedStats(stack, "Boots", 2, 2, EquipmentSlot.FEET);
        setCustomItemId(stack, "warped_boots");
        return stack;
    }
    private static void applyWarpedStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(0x4C7D5F));
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Warped " + pieceName).withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, 300);
        stack.set(DataComponents.DAMAGE, 0);
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "warped_" + pieceName.toLowerCase() + "_armor"), armorValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "warped_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_GREEN));
        lore.add(Component.literal("🛡 Armor: +" + armorValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal("💎 Toughness: +" + toughnessValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.LIGHT_PURPLE, "warped");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Nether twist").withStyle(ChatFormatting.DARK_GRAY));
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // WET SPONGE ARMOR
    public static ItemStack createWetSpongeHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyWetSpongeStats(stack, "Helmet", 2, 1, EquipmentSlot.HEAD);
        setCustomItemId(stack, "wet_sponge_helmet");
        return stack;
    }
    public static ItemStack createWetSpongeChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyWetSpongeStats(stack, "Chestplate", 6, 1, EquipmentSlot.CHEST);
        setCustomItemId(stack, "wet_sponge_chestplate");
        return stack;
    }
    public static ItemStack createWetSpongeLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyWetSpongeStats(stack, "Leggings", 5, 1, EquipmentSlot.LEGS);
        setCustomItemId(stack, "wet_sponge_leggings");
        return stack;
    }
    public static ItemStack createWetSpongeBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyWetSpongeStats(stack, "Boots", 2, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "wet_sponge_boots");
        return stack;
    }
    private static void applyWetSpongeStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(0x4682B4));
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Wet Sponge " + pieceName).withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, 200);
        stack.set(DataComponents.DAMAGE, 0);
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "wet_sponge_" + pieceName.toLowerCase() + "_armor"), armorValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", "wet_sponge_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_AQUA));
        lore.add(Component.literal("🛡 Armor: +" + armorValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal("💎 Toughness: +" + toughnessValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.LIGHT_PURPLE, "wet_sponge");
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Soaked defense").withStyle(ChatFormatting.DARK_GRAY));
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ============================================================
    // GENERIC BLOCK ARMOR HELPER
    // ============================================================

    private static void applyBlockArmorStats(ItemStack stack, String materialName, String idPrefix,
            String pieceName, int color, int durability, ChatFormatting nameColor,
            int armorValue, int toughnessValue, String setBonus, String flavorText) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(color));
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(materialName + " " + pieceName).withStyle(nameColor, ChatFormatting.BOLD));
        stack.set(DataComponents.MAX_DAMAGE, durability);
        stack.set(DataComponents.DAMAGE, 0);
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", idPrefix + "_" + pieceName.toLowerCase() + "_armor"), armorValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(Identifier.fromNamespaceAndPath("politicalserver", idPrefix + "_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, AttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(nameColor));
        lore.add(Component.literal("🛡 Armor: +" + armorValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal("💎 Toughness: +" + toughnessValue).withStyle(ChatFormatting.WHITE));
        lore.add(Component.literal(""));
        BlockGearLore.appendArmorSetBonus(lore, ChatFormatting.LIGHT_PURPLE, idPrefix);
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8" + flavorText).withStyle(ChatFormatting.DARK_GRAY));
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    private static ItemStack createBlockArmorPiece(net.minecraft.world.item.Item baseItem, String materialName,
            String idPrefix, String pieceName, int color, int durability, ChatFormatting nameColor,
            int armorValue, int toughnessValue, String setBonus, String flavorText, EquipmentSlot slot) {
        ItemStack stack = new ItemStack(baseItem);
        applyBlockArmorStats(stack, materialName, idPrefix, pieceName, color, durability, nameColor, armorValue, toughnessValue, setBonus, flavorText);
        setCustomItemId(stack, idPrefix + "_" + pieceName.toLowerCase());
        return stack;
    }

    // ============================================================
    // 25 NEW BLOCK ARMOR SETS
    // ============================================================

    // 1. RAW PRISMARINE ARMOR - Cyan-green ocean theme (medium)
    public static ItemStack createRawPrismarineHelmet() { return createBlockArmorPiece(Items.LEATHER_HELMET, "Raw Prismarine", "raw_prismarine", "Helmet", 0x5B9EA6, 250, ChatFormatting.DARK_AQUA, 3, 2, "Conduit Power I", "Ocean-forged protection", EquipmentSlot.HEAD); }
    public static ItemStack createRawPrismarineChestplate() { return createBlockArmorPiece(Items.LEATHER_CHESTPLATE, "Raw Prismarine", "raw_prismarine", "Chestplate", 0x5B9EA6, 250, ChatFormatting.DARK_AQUA, 5, 3, "Conduit Power I", "Ocean-forged protection", EquipmentSlot.CHEST); }
    public static ItemStack createRawPrismarineLeggings() { return createBlockArmorPiece(Items.LEATHER_LEGGINGS, "Raw Prismarine", "raw_prismarine", "Leggings", 0x5B9EA6, 250, ChatFormatting.DARK_AQUA, 4, 2, "Conduit Power I", "Ocean-forged protection", EquipmentSlot.LEGS); }
    public static ItemStack createRawPrismarineBoots() { return createBlockArmorPiece(Items.LEATHER_BOOTS, "Raw Prismarine", "raw_prismarine", "Boots", 0x5B9EA6, 250, ChatFormatting.DARK_AQUA, 2, 2, "Conduit Power I", "Ocean-forged protection", EquipmentSlot.FEET); }

    // 2. CLAY ARMOR - Terracotta-like but weaker, orange-brown (light)
    public static ItemStack createClayHelmet() { return createBlockArmorPiece(Items.LEATHER_HELMET, "Clay", "clay", "Helmet", 0xA4825A, 130, ChatFormatting.GOLD, 2, 1, "Earthen Shield", "Soft earthen defense", EquipmentSlot.HEAD); }
    public static ItemStack createClayChestplate() { return createBlockArmorPiece(Items.LEATHER_CHESTPLATE, "Clay", "clay", "Chestplate", 0xA4825A, 130, ChatFormatting.GOLD, 4, 1, "Earthen Shield", "Soft earthen defense", EquipmentSlot.CHEST); }
    public static ItemStack createClayLeggings() { return createBlockArmorPiece(Items.LEATHER_LEGGINGS, "Clay", "clay", "Leggings", 0xA4825A, 130, ChatFormatting.GOLD, 3, 1, "Earthen Shield", "Soft earthen defense", EquipmentSlot.LEGS); }
    public static ItemStack createClayBoots() { return createBlockArmorPiece(Items.LEATHER_BOOTS, "Clay", "clay", "Boots", 0xA4825A, 130, ChatFormatting.GOLD, 2, 0, "Earthen Shield", "Soft earthen defense", EquipmentSlot.FEET); }

    // 3. GLAZED TERRACOTTA ARMOR - Decorative, moderate stats (medium)
    public static ItemStack createGlazedTerracottaHelmet() { return createBlockArmorPiece(Items.LEATHER_HELMET, "Glazed Terracotta", "glazed_terracotta", "Helmet", 0xD4A76A, 280, ChatFormatting.GOLD, 4, 2, "Pattern Shift", "Vibrant glazed defense", EquipmentSlot.HEAD); }
    public static ItemStack createGlazedTerracottaChestplate() { return createBlockArmorPiece(Items.LEATHER_CHESTPLATE, "Glazed Terracotta", "glazed_terracotta", "Chestplate", 0xD4A76A, 280, ChatFormatting.GOLD, 6, 3, "Pattern Shift", "Vibrant glazed defense", EquipmentSlot.CHEST); }
    public static ItemStack createGlazedTerracottaLeggings() { return createBlockArmorPiece(Items.LEATHER_LEGGINGS, "Glazed Terracotta", "glazed_terracotta", "Leggings", 0xD4A76A, 280, ChatFormatting.GOLD, 5, 2, "Pattern Shift", "Vibrant glazed defense", EquipmentSlot.LEGS); }
    public static ItemStack createGlazedTerracottaBoots() { return createBlockArmorPiece(Items.LEATHER_BOOTS, "Glazed Terracotta", "glazed_terracotta", "Boots", 0xD4A76A, 280, ChatFormatting.GOLD, 3, 2, "Pattern Shift", "Vibrant glazed defense", EquipmentSlot.FEET); }

    // 4. CONCRETE ARMOR - Solid gray, high armor (heavy)
    public static ItemStack createConcreteHelmet() { return createBlockArmorPiece(Items.LEATHER_HELMET, "Concrete", "concrete", "Helmet", 0x808080, 500, ChatFormatting.GRAY, 6, 4, "Reinforced Shell", "Solid concrete defense", EquipmentSlot.HEAD); }
    public static ItemStack createConcreteChestplate() { return createBlockArmorPiece(Items.LEATHER_CHESTPLATE, "Concrete", "concrete", "Chestplate", 0x808080, 500, ChatFormatting.GRAY, 8, 5, "Reinforced Shell", "Solid concrete defense", EquipmentSlot.CHEST); }
    public static ItemStack createConcreteLeggings() { return createBlockArmorPiece(Items.LEATHER_LEGGINGS, "Concrete", "concrete", "Leggings", 0x808080, 500, ChatFormatting.GRAY, 7, 4, "Reinforced Shell", "Solid concrete defense", EquipmentSlot.LEGS); }
    public static ItemStack createConcreteBoots() { return createBlockArmorPiece(Items.LEATHER_BOOTS, "Concrete", "concrete", "Boots", 0x808080, 500, ChatFormatting.GRAY, 5, 3, "Reinforced Shell", "Solid concrete defense", EquipmentSlot.FEET); }

    // 5. CONCRETE POWDER ARMOR - Lighter, faster (light)
    public static ItemStack createConcretePowderHelmet() { return createBlockArmorPiece(Items.LEATHER_HELMET, "Concrete Powder", "concrete_powder", "Helmet", 0xC0C0C0, 150, ChatFormatting.WHITE, 2, 1, "Dust Cloud", "Powdery light defense", EquipmentSlot.HEAD); }
    public static ItemStack createConcretePowderChestplate() { return createBlockArmorPiece(Items.LEATHER_CHESTPLATE, "Concrete Powder", "concrete_powder", "Chestplate", 0xC0C0C0, 150, ChatFormatting.WHITE, 4, 1, "Dust Cloud", "Powdery light defense", EquipmentSlot.CHEST); }
    public static ItemStack createConcretePowderLeggings() { return createBlockArmorPiece(Items.LEATHER_LEGGINGS, "Concrete Powder", "concrete_powder", "Leggings", 0xC0C0C0, 150, ChatFormatting.WHITE, 3, 1, "Dust Cloud", "Powdery light defense", EquipmentSlot.LEGS); }
    public static ItemStack createConcretePowderBoots() { return createBlockArmorPiece(Items.LEATHER_BOOTS, "Concrete Powder", "concrete_powder", "Boots", 0xC0C0C0, 150, ChatFormatting.WHITE, 2, 0, "Dust Cloud", "Powdery light defense", EquipmentSlot.FEET); }

    // 6. BOOKSHELF ARMOR - Enchantment-themed (medium)
    public static ItemStack createBookshelfHelmet() { return createBlockArmorPiece(Items.LEATHER_HELMET, "Bookshelf", "bookshelf", "Helmet", 0x8B5A2B, 300, ChatFormatting.GOLD, 4, 3, "Knowledge Aura +30% XP", "Bound in ancient knowledge", EquipmentSlot.HEAD); }
    public static ItemStack createBookshelfChestplate() { return createBlockArmorPiece(Items.LEATHER_CHESTPLATE, "Bookshelf", "bookshelf", "Chestplate", 0x8B5A2B, 300, ChatFormatting.GOLD, 6, 3, "Knowledge Aura +30% XP", "Bound in ancient knowledge", EquipmentSlot.CHEST); }
    public static ItemStack createBookshelfLeggings() { return createBlockArmorPiece(Items.LEATHER_LEGGINGS, "Bookshelf", "bookshelf", "Leggings", 0x8B5A2B, 300, ChatFormatting.GOLD, 5, 3, "Knowledge Aura +30% XP", "Bound in ancient knowledge", EquipmentSlot.LEGS); }
    public static ItemStack createBookshelfBoots() { return createBlockArmorPiece(Items.LEATHER_BOOTS, "Bookshelf", "bookshelf", "Boots", 0x8B5A2B, 300, ChatFormatting.GOLD, 3, 2, "Knowledge Aura +30% XP", "Bound in ancient knowledge", EquipmentSlot.FEET); }

    // 7. BARREL ARMOR - Storage-themed (medium)
    public static ItemStack createBarrelHelmet() { return createBlockArmorPiece(Items.LEATHER_HELMET, "Barrel", "barrel", "Helmet", 0x6B4423, 280, ChatFormatting.GOLD, 4, 2, "Extra Inventory Row", "Barrel-bound defense", EquipmentSlot.HEAD); }
    public static ItemStack createBarrelChestplate() { return createBlockArmorPiece(Items.LEATHER_CHESTPLATE, "Barrel", "barrel", "Chestplate", 0x6B4423, 280, ChatFormatting.GOLD, 5, 3, "Extra Inventory Row", "Barrel-bound defense", EquipmentSlot.CHEST); }
    public static ItemStack createBarrelLeggings() { return createBlockArmorPiece(Items.LEATHER_LEGGINGS, "Barrel", "barrel", "Leggings", 0x6B4423, 280, ChatFormatting.GOLD, 4, 2, "Extra Inventory Row", "Barrel-bound defense", EquipmentSlot.LEGS); }
    public static ItemStack createBarrelBoots() { return createBlockArmorPiece(Items.LEATHER_BOOTS, "Barrel", "barrel", "Boots", 0x6B4423, 280, ChatFormatting.GOLD, 3, 2, "Extra Inventory Row", "Barrel-bound defense", EquipmentSlot.FEET); }

    // 8. SMITHING TABLE ARMOR - Upgrade-themed (medium)
    public static ItemStack createSmithingTableHelmet() { return createBlockArmorPiece(Items.LEATHER_HELMET, "Smithing Table", "smithing_table", "Helmet", 0x3B3B3B, 350, ChatFormatting.DARK_GRAY, 5, 3, "Upgrade Mastery +15% craft", "Forged at the smithing table", EquipmentSlot.HEAD); }
    public static ItemStack createSmithingTableChestplate() { return createBlockArmorPiece(Items.LEATHER_CHESTPLATE, "Smithing Table", "smithing_table", "Chestplate", 0x3B3B3B, 350, ChatFormatting.DARK_GRAY, 6, 4, "Upgrade Mastery +15% craft", "Forged at the smithing table", EquipmentSlot.CHEST); }
    public static ItemStack createSmithingTableLeggings() { return createBlockArmorPiece(Items.LEATHER_LEGGINGS, "Smithing Table", "smithing_table", "Leggings", 0x3B3B3B, 350, ChatFormatting.DARK_GRAY, 5, 3, "Upgrade Mastery +15% craft", "Forged at the smithing table", EquipmentSlot.LEGS); }
    public static ItemStack createSmithingTableBoots() { return createBlockArmorPiece(Items.LEATHER_BOOTS, "Smithing Table", "smithing_table", "Boots", 0x3B3B3B, 350, ChatFormatting.DARK_GRAY, 4, 2, "Upgrade Mastery +15% craft", "Forged at the smithing table", EquipmentSlot.FEET); }

    // 9. ANVIL ARMOR - Heavy, slow, high protection (heavy)
    public static ItemStack createAnvilHelmet() { return createBlockArmorPiece(Items.LEATHER_HELMET, "Anvil", "anvil", "Helmet", 0x5A5A5A, 800, ChatFormatting.GRAY, 7, 5, "Iron Fortress + KB immune", "Heavy iron protection", EquipmentSlot.HEAD); }
    public static ItemStack createAnvilChestplate() { return createBlockArmorPiece(Items.LEATHER_CHESTPLATE, "Anvil", "anvil", "Chestplate", 0x5A5A5A, 800, ChatFormatting.GRAY, 8, 6, "Iron Fortress + KB immune", "Heavy iron protection", EquipmentSlot.CHEST); }
    public static ItemStack createAnvilLeggings() { return createBlockArmorPiece(Items.LEATHER_LEGGINGS, "Anvil", "anvil", "Leggings", 0x5A5A5A, 800, ChatFormatting.GRAY, 7, 5, "Iron Fortress + KB immune", "Heavy iron protection", EquipmentSlot.LEGS); }
    public static ItemStack createAnvilBoots() { return createBlockArmorPiece(Items.LEATHER_BOOTS, "Anvil", "anvil", "Boots", 0x5A5A5A, 800, ChatFormatting.GRAY, 6, 4, "Iron Fortress + KB immune", "Heavy iron protection", EquipmentSlot.FEET); }

    // 10. GRINDSTONE ARMOR - Sharpening-themed (medium)
    public static ItemStack createGrindstoneHelmet() { return createBlockArmorPiece(Items.LEATHER_HELMET, "Grindstone", "grindstone", "Helmet", 0x4A4A4A, 320, ChatFormatting.DARK_GRAY, 4, 3, "Sharpen Aura +10% melee dmg", "Honed by the grindstone", EquipmentSlot.HEAD); }
    public static ItemStack createGrindstoneChestplate() { return createBlockArmorPiece(Items.LEATHER_CHESTPLATE, "Grindstone", "grindstone", "Chestplate", 0x4A4A4A, 320, ChatFormatting.DARK_GRAY, 6, 3, "Sharpen Aura +10% melee dmg", "Honed by the grindstone", EquipmentSlot.CHEST); }
    public static ItemStack createGrindstoneLeggings() { return createBlockArmorPiece(Items.LEATHER_LEGGINGS, "Grindstone", "grindstone", "Leggings", 0x4A4A4A, 320, ChatFormatting.DARK_GRAY, 5, 3, "Sharpen Aura +10% melee dmg", "Honed by the grindstone", EquipmentSlot.LEGS); }
    public static ItemStack createGrindstoneBoots() { return createBlockArmorPiece(Items.LEATHER_BOOTS, "Grindstone", "grindstone", "Boots", 0x4A4A4A, 320, ChatFormatting.DARK_GRAY, 3, 2, "Sharpen Aura +10% melee dmg", "Honed by the grindstone", EquipmentSlot.FEET); }

    // 11. LECTERN ARMOR - Knowledge-themed (medium)
    public static ItemStack createLecternHelmet() { return createBlockArmorPiece(Items.LEATHER_HELMET, "Lectern", "lectern", "Helmet", 0x7A5C3A, 260, ChatFormatting.GOLD, 3, 2, "Wisdom Pulse +20% XP", "Scholarly protection", EquipmentSlot.HEAD); }
    public static ItemStack createLecternChestplate() { return createBlockArmorPiece(Items.LEATHER_CHESTPLATE, "Lectern", "lectern", "Chestplate", 0x7A5C3A, 260, ChatFormatting.GOLD, 5, 3, "Wisdom Pulse +20% XP", "Scholarly protection", EquipmentSlot.CHEST); }
    public static ItemStack createLecternLeggings() { return createBlockArmorPiece(Items.LEATHER_LEGGINGS, "Lectern", "lectern", "Leggings", 0x7A5C3A, 260, ChatFormatting.GOLD, 4, 2, "Wisdom Pulse +20% XP", "Scholarly protection", EquipmentSlot.LEGS); }
    public static ItemStack createLecternBoots() { return createBlockArmorPiece(Items.LEATHER_BOOTS, "Lectern", "lectern", "Boots", 0x7A5C3A, 260, ChatFormatting.GOLD, 3, 1, "Wisdom Pulse +20% XP", "Scholarly protection", EquipmentSlot.FEET); }

    // 12. COMPOSTER ARMOR - Nature/growth theme (medium)
    public static ItemStack createComposterHelmet() { return createBlockArmorPiece(Items.LEATHER_HELMET, "Composter", "composter", "Helmet", 0x5E7D3A, 240, ChatFormatting.GREEN, 3, 2, "Nature Regen + crop growth", "Rich in organic defense", EquipmentSlot.HEAD); }
    public static ItemStack createComposterChestplate() { return createBlockArmorPiece(Items.LEATHER_CHESTPLATE, "Composter", "composter", "Chestplate", 0x5E7D3A, 240, ChatFormatting.GREEN, 5, 2, "Nature Regen + crop growth", "Rich in organic defense", EquipmentSlot.CHEST); }
    public static ItemStack createComposterLeggings() { return createBlockArmorPiece(Items.LEATHER_LEGGINGS, "Composter", "composter", "Leggings", 0x5E7D3A, 240, ChatFormatting.GREEN, 4, 2, "Nature Regen + crop growth", "Rich in organic defense", EquipmentSlot.LEGS); }
    public static ItemStack createComposterBoots() { return createBlockArmorPiece(Items.LEATHER_BOOTS, "Composter", "composter", "Boots", 0x5E7D3A, 240, ChatFormatting.GREEN, 3, 1, "Nature Regen + crop growth", "Rich in organic defense", EquipmentSlot.FEET); }

    // 13. STONECUTTER ARMOR - Precision theme (medium)
    public static ItemStack createStonecutterHelmet() { return createBlockArmorPiece(Items.LEATHER_HELMET, "Stonecutter", "stonecutter", "Helmet", 0xB0B0B0, 300, ChatFormatting.WHITE, 4, 3, "Precision Strike +crit chance", "Cut with precision", EquipmentSlot.HEAD); }
    public static ItemStack createStonecutterChestplate() { return createBlockArmorPiece(Items.LEATHER_CHESTPLATE, "Stonecutter", "stonecutter", "Chestplate", 0xB0B0B0, 300, ChatFormatting.WHITE, 6, 3, "Precision Strike +crit chance", "Cut with precision", EquipmentSlot.CHEST); }
    public static ItemStack createStonecutterLeggings() { return createBlockArmorPiece(Items.LEATHER_LEGGINGS, "Stonecutter", "stonecutter", "Leggings", 0xB0B0B0, 300, ChatFormatting.WHITE, 5, 3, "Precision Strike +crit chance", "Cut with precision", EquipmentSlot.LEGS); }
    public static ItemStack createStonecutterBoots() { return createBlockArmorPiece(Items.LEATHER_BOOTS, "Stonecutter", "stonecutter", "Boots", 0xB0B0B0, 300, ChatFormatting.WHITE, 3, 2, "Precision Strike +crit chance", "Cut with precision", EquipmentSlot.FEET); }

    // 14. DRIED KELP ARMOR - Ocean/food theme (light)
    public static ItemStack createDriedKelpHelmet() { return createBlockArmorPiece(Items.LEATHER_HELMET, "Dried Kelp", "dried_kelp", "Helmet", 0x3B5E2B, 120, ChatFormatting.DARK_GREEN, 2, 0, "Ocean Nourish + saturation", "Dried ocean defense", EquipmentSlot.HEAD); }
    public static ItemStack createDriedKelpChestplate() { return createBlockArmorPiece(Items.LEATHER_CHESTPLATE, "Dried Kelp", "dried_kelp", "Chestplate", 0x3B5E2B, 120, ChatFormatting.DARK_GREEN, 3, 1, "Ocean Nourish + saturation", "Dried ocean defense", EquipmentSlot.CHEST); }
    public static ItemStack createDriedKelpLeggings() { return createBlockArmorPiece(Items.LEATHER_LEGGINGS, "Dried Kelp", "dried_kelp", "Leggings", 0x3B5E2B, 120, ChatFormatting.DARK_GREEN, 3, 0, "Ocean Nourish + saturation", "Dried ocean defense", EquipmentSlot.LEGS); }
    public static ItemStack createDriedKelpBoots() { return createBlockArmorPiece(Items.LEATHER_BOOTS, "Dried Kelp", "dried_kelp", "Boots", 0x3B5E2B, 120, ChatFormatting.DARK_GREEN, 2, 0, "Ocean Nourish + saturation", "Dried ocean defense", EquipmentSlot.FEET); }

    // 15. CORAL BLOCK ARMOR - Vibrant ocean (light)
    public static ItemStack createCoralBlockHelmet() { return createBlockArmorPiece(Items.LEATHER_HELMET, "Coral Block", "coral_block", "Helmet", 0xFF69B4, 150, ChatFormatting.LIGHT_PURPLE, 2, 1, "Reef Barrier + water speed", "Vibrant reef defense", EquipmentSlot.HEAD); }
    public static ItemStack createCoralBlockChestplate() { return createBlockArmorPiece(Items.LEATHER_CHESTPLATE, "Coral Block", "coral_block", "Chestplate", 0xFF69B4, 150, ChatFormatting.LIGHT_PURPLE, 4, 1, "Reef Barrier + water speed", "Vibrant reef defense", EquipmentSlot.CHEST); }
    public static ItemStack createCoralBlockLeggings() { return createBlockArmorPiece(Items.LEATHER_LEGGINGS, "Coral Block", "coral_block", "Leggings", 0xFF69B4, 150, ChatFormatting.LIGHT_PURPLE, 3, 1, "Reef Barrier + water speed", "Vibrant reef defense", EquipmentSlot.LEGS); }
    public static ItemStack createCoralBlockBoots() { return createBlockArmorPiece(Items.LEATHER_BOOTS, "Coral Block", "coral_block", "Boots", 0xFF69B4, 150, ChatFormatting.LIGHT_PURPLE, 2, 0, "Reef Barrier + water speed", "Vibrant reef defense", EquipmentSlot.FEET); }

    // 16. LANTERN ARMOR - Light theme (medium)
    public static ItemStack createLanternHelmet() { return createBlockArmorPiece(Items.LEATHER_HELMET, "Lantern", "lantern", "Helmet", 0xE8A317, 260, ChatFormatting.GOLD, 3, 2, "Light Aura + Night Vision", "Illuminated defense", EquipmentSlot.HEAD); }
    public static ItemStack createLanternChestplate() { return createBlockArmorPiece(Items.LEATHER_CHESTPLATE, "Lantern", "lantern", "Chestplate", 0xE8A317, 260, ChatFormatting.GOLD, 5, 3, "Light Aura + Night Vision", "Illuminated defense", EquipmentSlot.CHEST); }
    public static ItemStack createLanternLeggings() { return createBlockArmorPiece(Items.LEATHER_LEGGINGS, "Lantern", "lantern", "Leggings", 0xE8A317, 260, ChatFormatting.GOLD, 4, 2, "Light Aura + Night Vision", "Illuminated defense", EquipmentSlot.LEGS); }
    public static ItemStack createLanternBoots() { return createBlockArmorPiece(Items.LEATHER_BOOTS, "Lantern", "lantern", "Boots", 0xE8A317, 260, ChatFormatting.GOLD, 3, 1, "Light Aura + Night Vision", "Illuminated defense", EquipmentSlot.FEET); }

    // 17. CHAIN ARMOR - Chainmail-like (heavy)
    public static ItemStack createChainHelmet() { return createBlockArmorPiece(Items.LEATHER_HELMET, "Chain", "chain", "Helmet", 0x4A4A4A, 600, ChatFormatting.DARK_GRAY, 6, 4, "Linked Defense + thorns", "Interlocking chain defense", EquipmentSlot.HEAD); }
    public static ItemStack createChainChestplate() { return createBlockArmorPiece(Items.LEATHER_CHESTPLATE, "Chain", "chain", "Chestplate", 0x4A4A4A, 600, ChatFormatting.DARK_GRAY, 8, 5, "Linked Defense + thorns", "Interlocking chain defense", EquipmentSlot.CHEST); }
    public static ItemStack createChainLeggings() { return createBlockArmorPiece(Items.LEATHER_LEGGINGS, "Chain", "chain", "Leggings", 0x4A4A4A, 600, ChatFormatting.DARK_GRAY, 7, 4, "Linked Defense + thorns", "Interlocking chain defense", EquipmentSlot.LEGS); }
    public static ItemStack createChainBoots() { return createBlockArmorPiece(Items.LEATHER_BOOTS, "Chain", "chain", "Boots", 0x4A4A4A, 600, ChatFormatting.DARK_GRAY, 5, 3, "Linked Defense + thorns", "Interlocking chain defense", EquipmentSlot.FEET); }

    // 18. LIGHTNING ROD ARMOR - Electrical theme (special)
    public static ItemStack createLightningRodHelmet() { return createBlockArmorPiece(Items.LEATHER_HELMET, "Lightning Rod", "lightning_rod", "Helmet", 0xB87333, 350, ChatFormatting.GOLD, 4, 3, "Storm Conductor + lightning dmg", "Crackling with energy", EquipmentSlot.HEAD); }
    public static ItemStack createLightningRodChestplate() { return createBlockArmorPiece(Items.LEATHER_CHESTPLATE, "Lightning Rod", "lightning_rod", "Chestplate", 0xB87333, 350, ChatFormatting.GOLD, 6, 4, "Storm Conductor + lightning dmg", "Crackling with energy", EquipmentSlot.CHEST); }
    public static ItemStack createLightningRodLeggings() { return createBlockArmorPiece(Items.LEATHER_LEGGINGS, "Lightning Rod", "lightning_rod", "Leggings", 0xB87333, 350, ChatFormatting.GOLD, 5, 3, "Storm Conductor + lightning dmg", "Crackling with energy", EquipmentSlot.LEGS); }
    public static ItemStack createLightningRodBoots() { return createBlockArmorPiece(Items.LEATHER_BOOTS, "Lightning Rod", "lightning_rod", "Boots", 0xB87333, 350, ChatFormatting.GOLD, 4, 2, "Storm Conductor + lightning dmg", "Crackling with energy", EquipmentSlot.FEET); }

    // 19. TINTED GLASS ARMOR - Stealth/dark theme (medium)
    public static ItemStack createTintedGlassHelmet() { return createBlockArmorPiece(Items.LEATHER_HELMET, "Tinted Glass", "tinted_glass", "Helmet", 0x2C2C2C, 200, ChatFormatting.DARK_GRAY, 3, 2, "Shadow Cloak + Invisibility", "Dark transparent defense", EquipmentSlot.HEAD); }
    public static ItemStack createTintedGlassChestplate() { return createBlockArmorPiece(Items.LEATHER_CHESTPLATE, "Tinted Glass", "tinted_glass", "Chestplate", 0x2C2C2C, 200, ChatFormatting.DARK_GRAY, 5, 2, "Shadow Cloak + Invisibility", "Dark transparent defense", EquipmentSlot.CHEST); }
    public static ItemStack createTintedGlassLeggings() { return createBlockArmorPiece(Items.LEATHER_LEGGINGS, "Tinted Glass", "tinted_glass", "Leggings", 0x2C2C2C, 200, ChatFormatting.DARK_GRAY, 4, 2, "Shadow Cloak + Invisibility", "Dark transparent defense", EquipmentSlot.LEGS); }
    public static ItemStack createTintedGlassBoots() { return createBlockArmorPiece(Items.LEATHER_BOOTS, "Tinted Glass", "tinted_glass", "Boots", 0x2C2C2C, 200, ChatFormatting.DARK_GRAY, 3, 1, "Shadow Cloak + Invisibility", "Dark transparent defense", EquipmentSlot.FEET); }

    // 20. CANDLE ARMOR - Mystical/wax theme (medium)
    public static ItemStack createCandleHelmet() { return createBlockArmorPiece(Items.LEATHER_HELMET, "Candle", "candle", "Helmet", 0xF5F5DC, 220, ChatFormatting.WHITE, 3, 2, "Wax Ward + fire resistance", "Wax-sealed defense", EquipmentSlot.HEAD); }
    public static ItemStack createCandleChestplate() { return createBlockArmorPiece(Items.LEATHER_CHESTPLATE, "Candle", "candle", "Chestplate", 0xF5F5DC, 220, ChatFormatting.WHITE, 5, 2, "Wax Ward + fire resistance", "Wax-sealed defense", EquipmentSlot.CHEST); }
    public static ItemStack createCandleLeggings() { return createBlockArmorPiece(Items.LEATHER_LEGGINGS, "Candle", "candle", "Leggings", 0xF5F5DC, 220, ChatFormatting.WHITE, 4, 2, "Wax Ward + fire resistance", "Wax-sealed defense", EquipmentSlot.LEGS); }
    public static ItemStack createCandleBoots() { return createBlockArmorPiece(Items.LEATHER_BOOTS, "Candle", "candle", "Boots", 0xF5F5DC, 220, ChatFormatting.WHITE, 3, 1, "Wax Ward + fire resistance", "Wax-sealed defense", EquipmentSlot.FEET); }

    // 21. SCULK SENSOR ARMOR - Vibration/stealth (special)
    public static ItemStack createSculkSensorHelmet() { return createBlockArmorPiece(Items.LEATHER_HELMET, "Sculk Sensor", "sculk_sensor", "Helmet", 0x0D4F54, 380, ChatFormatting.DARK_AQUA, 5, 3, "Vibration Sense + mob radar", "Attuned to vibrations", EquipmentSlot.HEAD); }
    public static ItemStack createSculkSensorChestplate() { return createBlockArmorPiece(Items.LEATHER_CHESTPLATE, "Sculk Sensor", "sculk_sensor", "Chestplate", 0x0D4F54, 380, ChatFormatting.DARK_AQUA, 6, 4, "Vibration Sense + mob radar", "Attuned to vibrations", EquipmentSlot.CHEST); }
    public static ItemStack createSculkSensorLeggings() { return createBlockArmorPiece(Items.LEATHER_LEGGINGS, "Sculk Sensor", "sculk_sensor", "Leggings", 0x0D4F54, 380, ChatFormatting.DARK_AQUA, 5, 3, "Vibration Sense + mob radar", "Attuned to vibrations", EquipmentSlot.LEGS); }
    public static ItemStack createSculkSensorBoots() { return createBlockArmorPiece(Items.LEATHER_BOOTS, "Sculk Sensor", "sculk_sensor", "Boots", 0x0D4F54, 380, ChatFormatting.DARK_AQUA, 4, 2, "Vibration Sense + mob radar", "Attuned to vibrations", EquipmentSlot.FEET); }

    // 22. SCULK CATALYST ARMOR - Death/growth theme (special)
    public static ItemStack createSculkCatalystHelmet() { return createBlockArmorPiece(Items.LEATHER_HELMET, "Sculk Catalyst", "sculk_catalyst", "Helmet", 0x0A3D3D, 400, ChatFormatting.DARK_AQUA, 5, 3, "Soul Harvest + XP on kill", "Feeds on fallen souls", EquipmentSlot.HEAD); }
    public static ItemStack createSculkCatalystChestplate() { return createBlockArmorPiece(Items.LEATHER_CHESTPLATE, "Sculk Catalyst", "sculk_catalyst", "Chestplate", 0x0A3D3D, 400, ChatFormatting.DARK_AQUA, 7, 4, "Soul Harvest + XP on kill", "Feeds on fallen souls", EquipmentSlot.CHEST); }
    public static ItemStack createSculkCatalystLeggings() { return createBlockArmorPiece(Items.LEATHER_LEGGINGS, "Sculk Catalyst", "sculk_catalyst", "Leggings", 0x0A3D3D, 400, ChatFormatting.DARK_AQUA, 6, 3, "Soul Harvest + XP on kill", "Feeds on fallen souls", EquipmentSlot.LEGS); }
    public static ItemStack createSculkCatalystBoots() { return createBlockArmorPiece(Items.LEATHER_BOOTS, "Sculk Catalyst", "sculk_catalyst", "Boots", 0x0A3D3D, 400, ChatFormatting.DARK_AQUA, 4, 2, "Soul Harvest + XP on kill", "Feeds on fallen souls", EquipmentSlot.FEET); }

    // 23. SCULK SHRIEKER ARMOR - Warden/screech theme (special)
    public static ItemStack createSculkShriekerHelmet() { return createBlockArmorPiece(Items.LEATHER_HELMET, "Sculk Shrieker", "sculk_shrieker", "Helmet", 0x0A2A5E, 420, ChatFormatting.DARK_BLUE, 5, 4, "Warden's Cry + fear aura", "Echoes of the deep dark", EquipmentSlot.HEAD); }
    public static ItemStack createSculkShriekerChestplate() { return createBlockArmorPiece(Items.LEATHER_CHESTPLATE, "Sculk Shrieker", "sculk_shrieker", "Chestplate", 0x0A2A5E, 420, ChatFormatting.DARK_BLUE, 7, 5, "Warden's Cry + fear aura", "Echoes of the deep dark", EquipmentSlot.CHEST); }
    public static ItemStack createSculkShriekerLeggings() { return createBlockArmorPiece(Items.LEATHER_LEGGINGS, "Sculk Shrieker", "sculk_shrieker", "Leggings", 0x0A2A5E, 420, ChatFormatting.DARK_BLUE, 6, 4, "Warden's Cry + fear aura", "Echoes of the deep dark", EquipmentSlot.LEGS); }
    public static ItemStack createSculkShriekerBoots() { return createBlockArmorPiece(Items.LEATHER_BOOTS, "Sculk Shrieker", "sculk_shrieker", "Boots", 0x0A2A5E, 420, ChatFormatting.DARK_BLUE, 4, 3, "Warden's Cry + fear aura", "Echoes of the deep dark", EquipmentSlot.FEET); }

    // 24. BREWING STAND ARMOR - Potion theme (special)
    public static ItemStack createBrewingStandHelmet() { return createBlockArmorPiece(Items.LEATHER_HELMET, "Brewing Stand", "brewing_stand", "Helmet", 0x6A0DAD, 340, ChatFormatting.DARK_PURPLE, 4, 3, "Potion Master + random buffs", "Bubbling with potency", EquipmentSlot.HEAD); }
    public static ItemStack createBrewingStandChestplate() { return createBlockArmorPiece(Items.LEATHER_CHESTPLATE, "Brewing Stand", "brewing_stand", "Chestplate", 0x6A0DAD, 340, ChatFormatting.DARK_PURPLE, 6, 4, "Potion Master + random buffs", "Bubbling with potency", EquipmentSlot.CHEST); }
    public static ItemStack createBrewingStandLeggings() { return createBlockArmorPiece(Items.LEATHER_LEGGINGS, "Brewing Stand", "brewing_stand", "Leggings", 0x6A0DAD, 340, ChatFormatting.DARK_PURPLE, 5, 3, "Potion Master + random buffs", "Bubbling with potency", EquipmentSlot.LEGS); }
    public static ItemStack createBrewingStandBoots() { return createBlockArmorPiece(Items.LEATHER_BOOTS, "Brewing Stand", "brewing_stand", "Boots", 0x6A0DAD, 340, ChatFormatting.DARK_PURPLE, 4, 2, "Potion Master + random buffs", "Bubbling with potency", EquipmentSlot.FEET); }

    // 25. FURNACE ARMOR - Smelting/heat theme (heavy)
    public static ItemStack createFurnaceHelmet() { return createBlockArmorPiece(Items.LEATHER_HELMET, "Furnace", "furnace", "Helmet", 0x6E6E6E, 550, ChatFormatting.GRAY, 6, 4, "Smelt Ward + fire immune", "Forged in the furnace", EquipmentSlot.HEAD); }
    public static ItemStack createFurnaceChestplate() { return createBlockArmorPiece(Items.LEATHER_CHESTPLATE, "Furnace", "furnace", "Chestplate", 0x6E6E6E, 550, ChatFormatting.GRAY, 8, 5, "Smelt Ward + fire immune", "Forged in the furnace", EquipmentSlot.CHEST); }
    public static ItemStack createFurnaceLeggings() { return createBlockArmorPiece(Items.LEATHER_LEGGINGS, "Furnace", "furnace", "Leggings", 0x6E6E6E, 550, ChatFormatting.GRAY, 7, 4, "Smelt Ward + fire immune", "Forged in the furnace", EquipmentSlot.LEGS); }
    public static ItemStack createFurnaceBoots() { return createBlockArmorPiece(Items.LEATHER_BOOTS, "Furnace", "furnace", "Boots", 0x6E6E6E, 550, ChatFormatting.GRAY, 5, 3, "Smelt Ward + fire immune", "Forged in the furnace", EquipmentSlot.FEET); }
}
