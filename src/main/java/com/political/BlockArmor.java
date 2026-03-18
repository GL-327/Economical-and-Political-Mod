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
    // 30+ NEW BLOCK ARMOR SETS
    // ============================================================

    // DIAMOND BLOCK ARMOR - Ultimate hardness
    public static ItemStack createDiamondBlockHelmet() {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        applyPlayerHeadTexture(stack, DIAMOND_BLOCK_HEAD);
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
        if (!stack.isOf(Items.PLAYER_HEAD)) {
            stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(DIAMOND_BLOCK_COLOR));
        }
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Diamond Block " + pieceName).formatted(Formatting.AQUA, Formatting.BOLD));
        stack.set(DataComponentTypes.MAX_DAMAGE, DIAMOND_BLOCK_DURABILITY);
        stack.set(DataComponentTypes.DAMAGE, 0);
        AttributeModifierSlot modifierSlot = getSlotForPiece(pieceName);
        AttributeModifiersComponent.Builder attrBuilder = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR, new EntityAttributeModifier(Identifier.of("politicalserver", "diamond_block_" + pieceName.toLowerCase() + "_armor"), armorValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(EntityAttributes.ARMOR_TOUGHNESS, new EntityAttributeModifier(Identifier.of("politicalserver", "diamond_block_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.AQUA));
        lore.add(Text.literal("🛡 Armor: +" + armorValue).formatted(Formatting.WHITE));
        lore.add(Text.literal("💎 Toughness: +" + toughnessValue).formatted(Formatting.WHITE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ SET BONUS ━━━").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal("✦ Full set: 20% damage reduction").formatted(Formatting.GREEN));
        lore.add(Text.literal("✦ High durability").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Ultimate luxury and protection").formatted(Formatting.DARK_GRAY));
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // EMERALD BLOCK ARMOR - Merchant's pride
    public static ItemStack createEmeraldBlockHelmet() {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        applyPlayerHeadTexture(stack, EMERALD_BLOCK_HEAD);
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
        if (!stack.isOf(Items.PLAYER_HEAD)) {
            stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(EMERALD_BLOCK_COLOR));
        }
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Emerald Block " + pieceName).formatted(Formatting.GREEN, Formatting.BOLD));
        stack.set(DataComponentTypes.MAX_DAMAGE, EMERALD_BLOCK_DURABILITY);
        stack.set(DataComponentTypes.DAMAGE, 0);
        AttributeModifierSlot modifierSlot = getSlotForPiece(pieceName);
        AttributeModifiersComponent.Builder attrBuilder = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR, new EntityAttributeModifier(Identifier.of("politicalserver", "emerald_block_" + pieceName.toLowerCase() + "_armor"), armorValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(EntityAttributes.ARMOR_TOUGHNESS, new EntityAttributeModifier(Identifier.of("politicalserver", "emerald_block_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.GREEN));
        lore.add(Text.literal("🛡 Armor: +" + armorValue).formatted(Formatting.WHITE));
        lore.add(Text.literal("💎 Toughness: +" + toughnessValue).formatted(Formatting.WHITE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ SET BONUS ━━━").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal("✦ Full set: Better villager trades").formatted(Formatting.GREEN));
        lore.add(Text.literal("✦ Emerald detection nearby").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8The merchant's choice").formatted(Formatting.DARK_GRAY));
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // GOLD BLOCK ARMOR - Gilded luxury
    public static ItemStack createGoldBlockHelmet() {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        applyPlayerHeadTexture(stack, GOLD_BLOCK_HEAD);
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
        if (!stack.isOf(Items.PLAYER_HEAD)) {
            stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(GOLD_BLOCK_COLOR));
        }
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Gold Block " + pieceName).formatted(Formatting.YELLOW, Formatting.BOLD));
        stack.set(DataComponentTypes.MAX_DAMAGE, GOLD_BLOCK_DURABILITY);
        stack.set(DataComponentTypes.DAMAGE, 0);
        AttributeModifierSlot modifierSlot = getSlotForPiece(pieceName);
        AttributeModifiersComponent.Builder attrBuilder = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR, new EntityAttributeModifier(Identifier.of("politicalserver", "gold_block_" + pieceName.toLowerCase() + "_armor"), armorValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(EntityAttributes.ARMOR_TOUGHNESS, new EntityAttributeModifier(Identifier.of("politicalserver", "gold_block_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.YELLOW));
        lore.add(Text.literal("🛡 Armor: +" + armorValue).formatted(Formatting.WHITE));
        lore.add(Text.literal("💎 Toughness: +" + toughnessValue).formatted(Formatting.WHITE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ SET BONUS ━━━").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal("✦ Full set: Piglins love you").formatted(Formatting.GREEN));
        lore.add(Text.literal("✦ Fortune on kills").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Gilded and glorious").formatted(Formatting.DARK_GRAY));
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // IRON BLOCK ARMOR - Industrial strength
    public static ItemStack createIronBlockHelmet() {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        applyPlayerHeadTexture(stack, IRON_BLOCK_HEAD);
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
        if (!stack.isOf(Items.PLAYER_HEAD)) {
            stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(IRON_BLOCK_COLOR));
        }
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Iron Block " + pieceName).formatted(Formatting.WHITE, Formatting.BOLD));
        stack.set(DataComponentTypes.MAX_DAMAGE, IRON_BLOCK_DURABILITY);
        stack.set(DataComponentTypes.DAMAGE, 0);
        AttributeModifierSlot modifierSlot = getSlotForPiece(pieceName);
        AttributeModifiersComponent.Builder attrBuilder = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR, new EntityAttributeModifier(Identifier.of("politicalserver", "iron_block_" + pieceName.toLowerCase() + "_armor"), armorValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(EntityAttributes.ARMOR_TOUGHNESS, new EntityAttributeModifier(Identifier.of("politicalserver", "iron_block_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.WHITE));
        lore.add(Text.literal("🛡 Armor: +" + armorValue).formatted(Formatting.WHITE));
        lore.add(Text.literal("💎 Toughness: +" + toughnessValue).formatted(Formatting.WHITE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ SET BONUS ━━━").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal("✦ Full set: Magnetic item pickup").formatted(Formatting.GREEN));
        lore.add(Text.literal("✦ 15% knockback resistance").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Industrial grade protection").formatted(Formatting.DARK_GRAY));
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // LAPIS BLOCK ARMOR - Enchanted
    public static ItemStack createLapisBlockHelmet() {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        applyPlayerHeadTexture(stack, LAPIS_BLOCK_HEAD);
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
        if (!stack.isOf(Items.PLAYER_HEAD)) {
            stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(LAPIS_BLOCK_COLOR));
        }
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Lapis Block " + pieceName).formatted(Formatting.BLUE, Formatting.BOLD));
        stack.set(DataComponentTypes.MAX_DAMAGE, LAPIS_BLOCK_DURABILITY);
        stack.set(DataComponentTypes.DAMAGE, 0);
        AttributeModifierSlot modifierSlot = getSlotForPiece(pieceName);
        AttributeModifiersComponent.Builder attrBuilder = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR, new EntityAttributeModifier(Identifier.of("politicalserver", "lapis_block_" + pieceName.toLowerCase() + "_armor"), armorValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(EntityAttributes.ARMOR_TOUGHNESS, new EntityAttributeModifier(Identifier.of("politicalserver", "lapis_block_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.BLUE));
        lore.add(Text.literal("🛡 Armor: +" + armorValue).formatted(Formatting.WHITE));
        lore.add(Text.literal("💎 Toughness: +" + toughnessValue).formatted(Formatting.WHITE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ SET BONUS ━━━").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal("✦ Full set: XP bonus +25%").formatted(Formatting.GREEN));
        lore.add(Text.literal("✦ Enchantment boost").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Knowledge is power").formatted(Formatting.DARK_GRAY));
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // COPPER BLOCK ARMOR - Conductive
    public static ItemStack createCopperBlockHelmet() {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        applyPlayerHeadTexture(stack, COPPER_BLOCK_HEAD);
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
        if (!stack.isOf(Items.PLAYER_HEAD)) {
            stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(COPPER_BLOCK_COLOR));
        }
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Copper Block " + pieceName).formatted(Formatting.GOLD, Formatting.BOLD));
        stack.set(DataComponentTypes.MAX_DAMAGE, COPPER_BLOCK_DURABILITY);
        stack.set(DataComponentTypes.DAMAGE, 0);
        AttributeModifierSlot modifierSlot = getSlotForPiece(pieceName);
        AttributeModifiersComponent.Builder attrBuilder = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR, new EntityAttributeModifier(Identifier.of("politicalserver", "copper_block_" + pieceName.toLowerCase() + "_armor"), armorValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(EntityAttributes.ARMOR_TOUGHNESS, new EntityAttributeModifier(Identifier.of("politicalserver", "copper_block_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("🛡 Armor: +" + armorValue).formatted(Formatting.WHITE));
        lore.add(Text.literal("💎 Toughness: +" + toughnessValue).formatted(Formatting.WHITE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ SET BONUS ━━━").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal("✦ Full set: Lightning rod").formatted(Formatting.GREEN));
        lore.add(Text.literal("✦ Charged attacks").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Conductive and practical").formatted(Formatting.DARK_GRAY));
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // ANCIENT DEBRIS ARMOR - Netherite ancient
    public static ItemStack createAncientDebrisHelmet() {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        applyPlayerHeadTexture(stack, ANCIENT_DEBRIS_HEAD);
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
        if (!stack.isOf(Items.PLAYER_HEAD)) stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(ANCIENT_DEBRIS_COLOR));
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Ancient Debris " + pieceName).formatted(Formatting.DARK_GRAY, Formatting.BOLD));
        stack.set(DataComponentTypes.MAX_DAMAGE, ANCIENT_DEBRIS_DURABILITY);
        stack.set(DataComponentTypes.DAMAGE, 0);
        AttributeModifierSlot modifierSlot = getSlotForPiece(pieceName);
        AttributeModifiersComponent.Builder attrBuilder = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR, new EntityAttributeModifier(Identifier.of("politicalserver", "ancient_debris_" + pieceName.toLowerCase() + "_armor"), armorValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(EntityAttributes.ARMOR_TOUGHNESS, new EntityAttributeModifier(Identifier.of("politicalserver", "ancient_debris_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("🛡 Armor: +" + armorValue).formatted(Formatting.WHITE));
        lore.add(Text.literal("💎 Toughness: +" + toughnessValue).formatted(Formatting.WHITE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ SET BONUS ━━━").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal("✦ Full set: Fire immunity").formatted(Formatting.GREEN));
        lore.add(Text.literal("✦ Blast protection").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Ancient and unbreakable").formatted(Formatting.DARK_GRAY));
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // BASALT ARMOR - Volcanic pillar
    public static ItemStack createBasaltHelmet() {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        applyPlayerHeadTexture(stack, BASALT_HEAD);
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
        if (!stack.isOf(Items.PLAYER_HEAD)) stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(BASALT_COLOR));
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Basalt " + pieceName).formatted(Formatting.GRAY, Formatting.BOLD));
        stack.set(DataComponentTypes.MAX_DAMAGE, BASALT_DURABILITY);
        stack.set(DataComponentTypes.DAMAGE, 0);
        AttributeModifierSlot modifierSlot = getSlotForPiece(pieceName);
        AttributeModifiersComponent.Builder attrBuilder = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR, new EntityAttributeModifier(Identifier.of("politicalserver", "basalt_" + pieceName.toLowerCase() + "_armor"), armorValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(EntityAttributes.ARMOR_TOUGHNESS, new EntityAttributeModifier(Identifier.of("politicalserver", "basalt_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.GRAY));
        lore.add(Text.literal("🛡 Armor: +" + armorValue).formatted(Formatting.WHITE));
        lore.add(Text.literal("💎 Toughness: +" + toughnessValue).formatted(Formatting.WHITE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ SET BONUS ━━━").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal("✦ Full set: Lava resistance").formatted(Formatting.GREEN));
        lore.add(Text.literal("✦ Knockback immunity").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Formed in volcanic heat").formatted(Formatting.DARK_GRAY));
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // BLACKSTONE ARMOR - Dark fortress
    public static ItemStack createBlackstoneHelmet() {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        applyPlayerHeadTexture(stack, BLACKSTONE_HEAD);
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
        if (!stack.isOf(Items.PLAYER_HEAD)) stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(BLACKSTONE_COLOR));
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Blackstone " + pieceName).formatted(Formatting.DARK_GRAY, Formatting.BOLD));
        stack.set(DataComponentTypes.MAX_DAMAGE, BLACKSTONE_DURABILITY);
        stack.set(DataComponentTypes.DAMAGE, 0);
        AttributeModifierSlot modifierSlot = getSlotForPiece(pieceName);
        AttributeModifiersComponent.Builder attrBuilder = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR, new EntityAttributeModifier(Identifier.of("politicalserver", "blackstone_" + pieceName.toLowerCase() + "_armor"), armorValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(EntityAttributes.ARMOR_TOUGHNESS, new EntityAttributeModifier(Identifier.of("politicalserver", "blackstone_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("🛡 Armor: +" + armorValue).formatted(Formatting.WHITE));
        lore.add(Text.literal("💎 Toughness: +" + toughnessValue).formatted(Formatting.WHITE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ SET BONUS ━━━").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal("✦ Full set: Piglin neutral").formatted(Formatting.GREEN));
        lore.add(Text.literal("✦ Darkvision").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Forged in the Nether").formatted(Formatting.DARK_GRAY));
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // BONE BLOCK ARMOR - Undead essence
    public static ItemStack createBoneBlockHelmet() {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        applyPlayerHeadTexture(stack, BONE_BLOCK_HEAD);
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
        if (!stack.isOf(Items.PLAYER_HEAD)) stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(BONE_BLOCK_COLOR));
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Bone Block " + pieceName).formatted(Formatting.WHITE, Formatting.BOLD));
        stack.set(DataComponentTypes.MAX_DAMAGE, BONE_BLOCK_DURABILITY);
        stack.set(DataComponentTypes.DAMAGE, 0);
        AttributeModifierSlot modifierSlot = getSlotForPiece(pieceName);
        AttributeModifiersComponent.Builder attrBuilder = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR, new EntityAttributeModifier(Identifier.of("politicalserver", "bone_block_" + pieceName.toLowerCase() + "_armor"), armorValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(EntityAttributes.ARMOR_TOUGHNESS, new EntityAttributeModifier(Identifier.of("politicalserver", "bone_block_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.WHITE));
        lore.add(Text.literal("🛡 Armor: +" + armorValue).formatted(Formatting.WHITE));
        lore.add(Text.literal("💎 Toughness: +" + toughnessValue).formatted(Formatting.WHITE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ SET BONUS ━━━").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal("✦ Full set: Undead ignore you").formatted(Formatting.GREEN));
        lore.add(Text.literal("✦ Bone meal bonus").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Skeleton's pride").formatted(Formatting.DARK_GRAY));
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // BRICK ARMOR - Builder's pride
    public static ItemStack createBrickHelmet() {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        applyPlayerHeadTexture(stack, BRICK_HEAD);
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
        if (!stack.isOf(Items.PLAYER_HEAD)) stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(BRICK_COLOR));
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Brick " + pieceName).formatted(Formatting.RED, Formatting.BOLD));
        stack.set(DataComponentTypes.MAX_DAMAGE, BRICK_DURABILITY);
        stack.set(DataComponentTypes.DAMAGE, 0);
        AttributeModifierSlot modifierSlot = getSlotForPiece(pieceName);
        AttributeModifiersComponent.Builder attrBuilder = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR, new EntityAttributeModifier(Identifier.of("politicalserver", "brick_" + pieceName.toLowerCase() + "_armor"), armorValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(EntityAttributes.ARMOR_TOUGHNESS, new EntityAttributeModifier(Identifier.of("politicalserver", "brick_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.RED));
        lore.add(Text.literal("🛡 Armor: +" + armorValue).formatted(Formatting.WHITE));
        lore.add(Text.literal("💎 Toughness: +" + toughnessValue).formatted(Formatting.WHITE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ SET BONUS ━━━").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal("✦ Full set: Build faster").formatted(Formatting.GREEN));
        lore.add(Text.literal("✦ No fall damage").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Solid construction").formatted(Formatting.DARK_GRAY));
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // CACTUS ARMOR - Prickly defense
    public static ItemStack createCactusHelmet() {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        applyPlayerHeadTexture(stack, CACTUS_HEAD);
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
        if (!stack.isOf(Items.PLAYER_HEAD)) stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(CACTUS_COLOR));
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Cactus " + pieceName).formatted(Formatting.DARK_GREEN, Formatting.BOLD));
        stack.set(DataComponentTypes.MAX_DAMAGE, CACTUS_DURABILITY);
        stack.set(DataComponentTypes.DAMAGE, 0);
        AttributeModifierSlot modifierSlot = getSlotForPiece(pieceName);
        AttributeModifiersComponent.Builder attrBuilder = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR, new EntityAttributeModifier(Identifier.of("politicalserver", "cactus_" + pieceName.toLowerCase() + "_armor"), armorValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(EntityAttributes.ARMOR_TOUGHNESS, new EntityAttributeModifier(Identifier.of("politicalserver", "cactus_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_GREEN));
        lore.add(Text.literal("🛡 Armor: +" + armorValue).formatted(Formatting.WHITE));
        lore.add(Text.literal("💎 Toughness: +" + toughnessValue).formatted(Formatting.WHITE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ SET BONUS ━━━").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal("✦ Full set: Thorns III").formatted(Formatting.GREEN));
        lore.add(Text.literal("✦ Desert immunity").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Prickly protection").formatted(Formatting.DARK_GRAY));
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // CALCITE ARMOR - Crystal white
    public static ItemStack createCalciteHelmet() {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        applyPlayerHeadTexture(stack, CALCITE_HEAD);
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
        if (!stack.isOf(Items.PLAYER_HEAD)) stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(CALCITE_COLOR));
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Calcite " + pieceName).formatted(Formatting.WHITE, Formatting.BOLD));
        stack.set(DataComponentTypes.MAX_DAMAGE, CALCITE_DURABILITY);
        stack.set(DataComponentTypes.DAMAGE, 0);
        AttributeModifierSlot modifierSlot = getSlotForPiece(pieceName);
        AttributeModifiersComponent.Builder attrBuilder = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR, new EntityAttributeModifier(Identifier.of("politicalserver", "calcite_" + pieceName.toLowerCase() + "_armor"), armorValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(EntityAttributes.ARMOR_TOUGHNESS, new EntityAttributeModifier(Identifier.of("politicalserver", "calcite_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.WHITE));
        lore.add(Text.literal("🛡 Armor: +" + armorValue).formatted(Formatting.WHITE));
        lore.add(Text.literal("💎 Toughness: +" + toughnessValue).formatted(Formatting.WHITE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ SET BONUS ━━━").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal("✦ Full set: Purifying aura").formatted(Formatting.GREEN));
        lore.add(Text.literal("✦ Undead damage bonus").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Pure and pristine").formatted(Formatting.DARK_GRAY));
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // DEEPSLATE ARMOR - Deep dark
    public static ItemStack createDeepslateHelmet() {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        applyPlayerHeadTexture(stack, DEEPSLATE_HEAD);
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
        if (!stack.isOf(Items.PLAYER_HEAD)) stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(DEEPSLATE_COLOR));
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Deepslate " + pieceName).formatted(Formatting.DARK_GRAY, Formatting.BOLD));
        stack.set(DataComponentTypes.MAX_DAMAGE, DEEPSLATE_DURABILITY);
        stack.set(DataComponentTypes.DAMAGE, 0);
        AttributeModifierSlot modifierSlot = getSlotForPiece(pieceName);
        AttributeModifiersComponent.Builder attrBuilder = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR, new EntityAttributeModifier(Identifier.of("politicalserver", "deepslate_" + pieceName.toLowerCase() + "_armor"), armorValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(EntityAttributes.ARMOR_TOUGHNESS, new EntityAttributeModifier(Identifier.of("politicalserver", "deepslate_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("🛡 Armor: +" + armorValue).formatted(Formatting.WHITE));
        lore.add(Text.literal("💎 Toughness: +" + toughnessValue).formatted(Formatting.WHITE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ SET BONUS ━━━").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal("✦ Full set: Mining speed +20%").formatted(Formatting.GREEN));
        lore.add(Text.literal("✦ Darkvision").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8From the deep dark").formatted(Formatting.DARK_GRAY));
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // DRIPSTONE ARMOR - Sharp and pointy
    public static ItemStack createDripstoneHelmet() {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        applyPlayerHeadTexture(stack, DRIPSTONE_HEAD);
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
        if (!stack.isOf(Items.PLAYER_HEAD)) stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(DRIPSTONE_COLOR));
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Dripstone " + pieceName).formatted(Formatting.GOLD, Formatting.BOLD));
        stack.set(DataComponentTypes.MAX_DAMAGE, DRIPSTONE_DURABILITY);
        stack.set(DataComponentTypes.DAMAGE, 0);
        AttributeModifierSlot modifierSlot = getSlotForPiece(pieceName);
        AttributeModifiersComponent.Builder attrBuilder = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR, new EntityAttributeModifier(Identifier.of("politicalserver", "dripstone_" + pieceName.toLowerCase() + "_armor"), armorValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(EntityAttributes.ARMOR_TOUGHNESS, new EntityAttributeModifier(Identifier.of("politicalserver", "dripstone_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("🛡 Armor: +" + armorValue).formatted(Formatting.WHITE));
        lore.add(Text.literal("💎 Toughness: +" + toughnessValue).formatted(Formatting.WHITE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ SET BONUS ━━━").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal("✦ Full set: Fall damage dealt").formatted(Formatting.GREEN));
        lore.add(Text.literal("✦ Piercing attacks").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Sharp and dripping").formatted(Formatting.DARK_GRAY));
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // HAY BALE ARMOR - Farmer's protection
    public static ItemStack createHayHelmet() {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        applyPlayerHeadTexture(stack, HAY_HEAD);
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
        if (!stack.isOf(Items.PLAYER_HEAD)) stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(HAY_COLOR));
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Hay " + pieceName).formatted(Formatting.YELLOW, Formatting.BOLD));
        stack.set(DataComponentTypes.MAX_DAMAGE, HAY_DURABILITY);
        stack.set(DataComponentTypes.DAMAGE, 0);
        AttributeModifierSlot modifierSlot = getSlotForPiece(pieceName);
        AttributeModifiersComponent.Builder attrBuilder = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR, new EntityAttributeModifier(Identifier.of("politicalserver", "hay_" + pieceName.toLowerCase() + "_armor"), armorValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(EntityAttributes.ARMOR_TOUGHNESS, new EntityAttributeModifier(Identifier.of("politicalserver", "hay_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.YELLOW));
        lore.add(Text.literal("🛡 Armor: +" + armorValue).formatted(Formatting.WHITE));
        lore.add(Text.literal("💎 Toughness: +" + toughnessValue).formatted(Formatting.WHITE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ SET BONUS ━━━").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal("✦ Full set: 90% fall damage reduction").formatted(Formatting.GREEN));
        lore.add(Text.literal("✦ Farmer's fortune").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Soft but effective").formatted(Formatting.DARK_GRAY));
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // HONEYCOMB ARMOR - Beekeeper's suit
    public static ItemStack createHoneycombHelmet() {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        applyPlayerHeadTexture(stack, HONEYCOMB_HEAD);
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
        if (!stack.isOf(Items.PLAYER_HEAD)) stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(HONEYCOMB_COLOR));
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Honeycomb " + pieceName).formatted(Formatting.GOLD, Formatting.BOLD));
        stack.set(DataComponentTypes.MAX_DAMAGE, HONEYCOMB_DURABILITY);
        stack.set(DataComponentTypes.DAMAGE, 0);
        AttributeModifierSlot modifierSlot = getSlotForPiece(pieceName);
        AttributeModifiersComponent.Builder attrBuilder = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR, new EntityAttributeModifier(Identifier.of("politicalserver", "honeycomb_" + pieceName.toLowerCase() + "_armor"), armorValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(EntityAttributes.ARMOR_TOUGHNESS, new EntityAttributeModifier(Identifier.of("politicalserver", "honeycomb_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("🛡 Armor: +" + armorValue).formatted(Formatting.WHITE));
        lore.add(Text.literal("💎 Toughness: +" + toughnessValue).formatted(Formatting.WHITE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ SET BONUS ━━━").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal("✦ Full set: Bees are friendly").formatted(Formatting.GREEN));
        lore.add(Text.literal("✦ Honey regen").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Sweet protection").formatted(Formatting.DARK_GRAY));
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // LILY PAD ARMOR - Water walker
    public static ItemStack createLilyPadHelmet() {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        applyPlayerHeadTexture(stack, LILY_PAD_HEAD);
        applyLilyPadStats(stack, "Helmet", 2, 1, EquipmentSlot.HEAD);
        setCustomItemId(stack, "lily_pad_helmet");
        return stack;
    }
    public static ItemStack createLilyPadChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyLilyPadStats(stack, "Chestplate", 3, 1, EquipmentSlot.CHEST);
        setCustomItemId(stack, "lily_pad_chestplate");
        return stack;
    }
    public static ItemStack createLilyPadLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyLilyPadStats(stack, "Leggings", 3, 1, EquipmentSlot.LEGS);
        setCustomItemId(stack, "lily_pad_leggings");
        return stack;
    }
    public static ItemStack createLilyPadBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyLilyPadStats(stack, "Boots", 1, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "lily_pad_boots");
        return stack;
    }
    private static void applyLilyPadStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        if (!stack.isOf(Items.PLAYER_HEAD)) stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(LILY_PAD_COLOR));
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Lily Pad " + pieceName).formatted(Formatting.DARK_GREEN, Formatting.BOLD));
        stack.set(DataComponentTypes.MAX_DAMAGE, LILY_PAD_DURABILITY);
        stack.set(DataComponentTypes.DAMAGE, 0);
        AttributeModifierSlot modifierSlot = getSlotForPiece(pieceName);
        AttributeModifiersComponent.Builder attrBuilder = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR, new EntityAttributeModifier(Identifier.of("politicalserver", "lily_pad_" + pieceName.toLowerCase() + "_armor"), armorValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(EntityAttributes.ARMOR_TOUGHNESS, new EntityAttributeModifier(Identifier.of("politicalserver", "lily_pad_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_GREEN));
        lore.add(Text.literal("🛡 Armor: +" + armorValue).formatted(Formatting.WHITE));
        lore.add(Text.literal("💎 Toughness: +" + toughnessValue).formatted(Formatting.WHITE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ SET BONUS ━━━").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal("✦ Full set: Walk on water").formatted(Formatting.GREEN));
        lore.add(Text.literal("✦ Water breathing").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Float like a lily").formatted(Formatting.DARK_GRAY));
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // MELON ARMOR - Juicy protection
    public static ItemStack createMelonHelmet() {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        applyPlayerHeadTexture(stack, MELON_HEAD);
        applyMelonStats(stack, "Helmet", 2, 1, EquipmentSlot.HEAD);
        setCustomItemId(stack, "melon_helmet");
        return stack;
    }
    public static ItemStack createMelonChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyMelonStats(stack, "Chestplate", 3, 1, EquipmentSlot.CHEST);
        setCustomItemId(stack, "melon_chestplate");
        return stack;
    }
    public static ItemStack createMelonLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyMelonStats(stack, "Leggings", 3, 1, EquipmentSlot.LEGS);
        setCustomItemId(stack, "melon_leggings");
        return stack;
    }
    public static ItemStack createMelonBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyMelonStats(stack, "Boots", 1, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "melon_boots");
        return stack;
    }
    private static void applyMelonStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        if (!stack.isOf(Items.PLAYER_HEAD)) stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(MELON_COLOR));
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Melon " + pieceName).formatted(Formatting.RED, Formatting.BOLD));
        stack.set(DataComponentTypes.MAX_DAMAGE, MELON_DURABILITY);
        stack.set(DataComponentTypes.DAMAGE, 0);
        AttributeModifierSlot modifierSlot = getSlotForPiece(pieceName);
        AttributeModifiersComponent.Builder attrBuilder = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR, new EntityAttributeModifier(Identifier.of("politicalserver", "melon_" + pieceName.toLowerCase() + "_armor"), armorValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(EntityAttributes.ARMOR_TOUGHNESS, new EntityAttributeModifier(Identifier.of("politicalserver", "melon_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.RED));
        lore.add(Text.literal("🛡 Armor: +" + armorValue).formatted(Formatting.WHITE));
        lore.add(Text.literal("💎 Toughness: +" + toughnessValue).formatted(Formatting.WHITE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ SET BONUS ━━━").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal("✦ Full set: Auto-eat, food regen").formatted(Formatting.GREEN));
        lore.add(Text.literal("✦ Summon melon slices").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Refreshingly sweet").formatted(Formatting.DARK_GRAY));
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // MOSS BLOCK ARMOR - Overgrown
    public static ItemStack createMossBlockHelmet() {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        applyPlayerHeadTexture(stack, MOSS_BLOCK_HEAD);
        applyMossBlockStats(stack, "Helmet", 2, 1, EquipmentSlot.HEAD);
        setCustomItemId(stack, "moss_block_helmet");
        return stack;
    }
    public static ItemStack createMossBlockChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyMossBlockStats(stack, "Chestplate", 3, 1, EquipmentSlot.CHEST);
        setCustomItemId(stack, "moss_block_chestplate");
        return stack;
    }
    public static ItemStack createMossBlockLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyMossBlockStats(stack, "Leggings", 3, 1, EquipmentSlot.LEGS);
        setCustomItemId(stack, "moss_block_leggings");
        return stack;
    }
    public static ItemStack createMossBlockBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyMossBlockStats(stack, "Boots", 1, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "moss_block_boots");
        return stack;
    }
    private static void applyMossBlockStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        if (!stack.isOf(Items.PLAYER_HEAD)) stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(MOSS_BLOCK_COLOR));
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Moss Block " + pieceName).formatted(Formatting.DARK_GREEN, Formatting.BOLD));
        stack.set(DataComponentTypes.MAX_DAMAGE, MOSS_BLOCK_DURABILITY);
        stack.set(DataComponentTypes.DAMAGE, 0);
        AttributeModifierSlot modifierSlot = getSlotForPiece(pieceName);
        AttributeModifiersComponent.Builder attrBuilder = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR, new EntityAttributeModifier(Identifier.of("politicalserver", "moss_block_" + pieceName.toLowerCase() + "_armor"), armorValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(EntityAttributes.ARMOR_TOUGHNESS, new EntityAttributeModifier(Identifier.of("politicalserver", "moss_block_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_GREEN));
        lore.add(Text.literal("🛡 Armor: +" + armorValue).formatted(Formatting.WHITE));
        lore.add(Text.literal("💎 Toughness: +" + toughnessValue).formatted(Formatting.WHITE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ SET BONUS ━━━").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal("✦ Full set: Nature regen").formatted(Formatting.GREEN));
        lore.add(Text.literal("✦ Bone meal aura").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Nature's embrace").formatted(Formatting.DARK_GRAY));
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // MYCELIUM ARMOR - Mushroom network
    public static ItemStack createMyceliumHelmet() {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        applyPlayerHeadTexture(stack, MYCELIUM_HEAD);
        applyMyceliumStats(stack, "Helmet", 2, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "mycelium_helmet");
        return stack;
    }
    public static ItemStack createMyceliumChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyMyceliumStats(stack, "Chestplate", 4, 2, EquipmentSlot.CHEST);
        setCustomItemId(stack, "mycelium_chestplate");
        return stack;
    }
    public static ItemStack createMyceliumLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyMyceliumStats(stack, "Leggings", 3, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "mycelium_leggings");
        return stack;
    }
    public static ItemStack createMyceliumBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyMyceliumStats(stack, "Boots", 2, 2, EquipmentSlot.FEET);
        setCustomItemId(stack, "mycelium_boots");
        return stack;
    }
    private static void applyMyceliumStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        if (!stack.isOf(Items.PLAYER_HEAD)) stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(MYCELIUM_COLOR));
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Mycelium " + pieceName).formatted(Formatting.DARK_PURPLE, Formatting.BOLD));
        stack.set(DataComponentTypes.MAX_DAMAGE, MYCELIUM_DURABILITY);
        stack.set(DataComponentTypes.DAMAGE, 0);
        AttributeModifierSlot modifierSlot = getSlotForPiece(pieceName);
        AttributeModifiersComponent.Builder attrBuilder = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR, new EntityAttributeModifier(Identifier.of("politicalserver", "mycelium_" + pieceName.toLowerCase() + "_armor"), armorValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(EntityAttributes.ARMOR_TOUGHNESS, new EntityAttributeModifier(Identifier.of("politicalserver", "mycelium_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_PURPLE));
        lore.add(Text.literal("🛡 Armor: +" + armorValue).formatted(Formatting.WHITE));
        lore.add(Text.literal("💎 Toughness: +" + toughnessValue).formatted(Formatting.WHITE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ SET BONUS ━━━").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal("✦ Full set: Mushroom spread").formatted(Formatting.GREEN));
        lore.add(Text.literal("✦ Spore attack").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Fungal network").formatted(Formatting.DARK_GRAY));
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // NETHER BRICK ARMOR - Fortress guard
    public static ItemStack createNetherBrickHelmet() {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        applyPlayerHeadTexture(stack, NETHER_BRICK_HEAD);
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
        if (!stack.isOf(Items.PLAYER_HEAD)) stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(NETHER_BRICK_COLOR));
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Nether Brick " + pieceName).formatted(Formatting.DARK_RED, Formatting.BOLD));
        stack.set(DataComponentTypes.MAX_DAMAGE, NETHER_BRICK_DURABILITY);
        stack.set(DataComponentTypes.DAMAGE, 0);
        AttributeModifierSlot modifierSlot = getSlotForPiece(pieceName);
        AttributeModifiersComponent.Builder attrBuilder = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR, new EntityAttributeModifier(Identifier.of("politicalserver", "nether_brick_" + pieceName.toLowerCase() + "_armor"), armorValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(EntityAttributes.ARMOR_TOUGHNESS, new EntityAttributeModifier(Identifier.of("politicalserver", "nether_brick_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_RED));
        lore.add(Text.literal("🛡 Armor: +" + armorValue).formatted(Formatting.WHITE));
        lore.add(Text.literal("💎 Toughness: +" + toughnessValue).formatted(Formatting.WHITE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ SET BONUS ━━━").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal("✦ Full set: Fire resistance").formatted(Formatting.GREEN));
        lore.add(Text.literal("✦ Wither skeleton immunity").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Forged in flames").formatted(Formatting.DARK_GRAY));
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // PUMPKIN ARMOR - Halloween spirit
    public static ItemStack createPumpkinHelmet() {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        applyPlayerHeadTexture(stack, PUMPKIN_HEAD);
        applyPumpkinStats(stack, "Helmet", 2, 1, EquipmentSlot.HEAD);
        setCustomItemId(stack, "pumpkin_helmet");
        return stack;
    }
    public static ItemStack createPumpkinChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyPumpkinStats(stack, "Chestplate", 4, 1, EquipmentSlot.CHEST);
        setCustomItemId(stack, "pumpkin_chestplate");
        return stack;
    }
    public static ItemStack createPumpkinLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyPumpkinStats(stack, "Leggings", 3, 1, EquipmentSlot.LEGS);
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
        if (!stack.isOf(Items.PLAYER_HEAD)) stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(PUMPKIN_COLOR));
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Pumpkin " + pieceName).formatted(Formatting.GOLD, Formatting.BOLD));
        stack.set(DataComponentTypes.MAX_DAMAGE, PUMPKIN_DURABILITY);
        stack.set(DataComponentTypes.DAMAGE, 0);
        AttributeModifierSlot modifierSlot = getSlotForPiece(pieceName);
        AttributeModifiersComponent.Builder attrBuilder = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR, new EntityAttributeModifier(Identifier.of("politicalserver", "pumpkin_" + pieceName.toLowerCase() + "_armor"), armorValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(EntityAttributes.ARMOR_TOUGHNESS, new EntityAttributeModifier(Identifier.of("politicalserver", "pumpkin_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("🛡 Armor: +" + armorValue).formatted(Formatting.WHITE));
        lore.add(Text.literal("💎 Toughness: +" + toughnessValue).formatted(Formatting.WHITE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ SET BONUS ━━━").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal("✦ Full set: Endermen ignore you").formatted(Formatting.GREEN));
        lore.add(Text.literal("✦ Halloween themed").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Spooky protection").formatted(Formatting.DARK_GRAY));
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // PURPUR ARMOR - End city elegant
    public static ItemStack createPurpurHelmet() {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
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
        if (!stack.isOf(Items.PLAYER_HEAD)) stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(PURPUR_COLOR));
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Purpur " + pieceName).formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        stack.set(DataComponentTypes.MAX_DAMAGE, PURPUR_DURABILITY);
        stack.set(DataComponentTypes.DAMAGE, 0);
        AttributeModifierSlot modifierSlot = getSlotForPiece(pieceName);
        AttributeModifiersComponent.Builder attrBuilder = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR, new EntityAttributeModifier(Identifier.of("politicalserver", "purpur_" + pieceName.toLowerCase() + "_armor"), armorValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(EntityAttributes.ARMOR_TOUGHNESS, new EntityAttributeModifier(Identifier.of("politicalserver", "purpur_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal("🛡 Armor: +" + armorValue).formatted(Formatting.WHITE));
        lore.add(Text.literal("💎 Toughness: +" + toughnessValue).formatted(Formatting.WHITE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ SET BONUS ━━━").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal("✦ Full set: Levitation immunity").formatted(Formatting.GREEN));
        lore.add(Text.literal("✦ Shulker bonus damage").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8End city elegance").formatted(Formatting.DARK_GRAY));
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // SAND ARMOR - Desert dweller
    public static ItemStack createSandHelmet() {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        applyPlayerHeadTexture(stack, SAND_HEAD);
        applySandStats(stack, "Helmet", 2, 0, EquipmentSlot.HEAD);
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
        applySandStats(stack, "Leggings", 3, 0, EquipmentSlot.LEGS);
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
        if (!stack.isOf(Items.PLAYER_HEAD)) stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(SAND_COLOR));
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Sand " + pieceName).formatted(Formatting.YELLOW, Formatting.BOLD));
        stack.set(DataComponentTypes.MAX_DAMAGE, SAND_DURABILITY);
        stack.set(DataComponentTypes.DAMAGE, 0);
        AttributeModifierSlot modifierSlot = getSlotForPiece(pieceName);
        AttributeModifiersComponent.Builder attrBuilder = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR, new EntityAttributeModifier(Identifier.of("politicalserver", "sand_" + pieceName.toLowerCase() + "_armor"), armorValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(EntityAttributes.ARMOR_TOUGHNESS, new EntityAttributeModifier(Identifier.of("politicalserver", "sand_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.YELLOW));
        lore.add(Text.literal("🛡 Armor: +" + armorValue).formatted(Formatting.WHITE));
        lore.add(Text.literal("💎 Toughness: +" + toughnessValue).formatted(Formatting.WHITE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ SET BONUS ━━━").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal("✦ Full set: Desert immunity").formatted(Formatting.GREEN));
        lore.add(Text.literal("✦ Sand speed boost").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Shifting protection").formatted(Formatting.DARK_GRAY));
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // SCULK ARMOR - Deep dark sensor
    public static ItemStack createSculkHelmet() {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
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
        if (!stack.isOf(Items.PLAYER_HEAD)) stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(SCULK_COLOR));
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Sculk " + pieceName).formatted(Formatting.DARK_AQUA, Formatting.BOLD));
        stack.set(DataComponentTypes.MAX_DAMAGE, SCULK_DURABILITY);
        stack.set(DataComponentTypes.DAMAGE, 0);
        AttributeModifierSlot modifierSlot = getSlotForPiece(pieceName);
        AttributeModifiersComponent.Builder attrBuilder = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR, new EntityAttributeModifier(Identifier.of("politicalserver", "sculk_" + pieceName.toLowerCase() + "_armor"), armorValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(EntityAttributes.ARMOR_TOUGHNESS, new EntityAttributeModifier(Identifier.of("politicalserver", "sculk_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_AQUA));
        lore.add(Text.literal("🛡 Armor: +" + armorValue).formatted(Formatting.WHITE));
        lore.add(Text.literal("💎 Toughness: +" + toughnessValue).formatted(Formatting.WHITE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ SET BONUS ━━━").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal("✦ Full set: Silence").formatted(Formatting.GREEN));
        lore.add(Text.literal("✦ Warden immunity").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8From the deep dark").formatted(Formatting.DARK_GRAY));
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // SHROOMLIGHT ARMOR - Fungal light
    public static ItemStack createShroomlightHelmet() {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        applyPlayerHeadTexture(stack, SHROOMLIGHT_HEAD);
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
        if (!stack.isOf(Items.PLAYER_HEAD)) stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(SHROOMLIGHT_COLOR));
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Shroomlight " + pieceName).formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        stack.set(DataComponentTypes.MAX_DAMAGE, SHROOMLIGHT_DURABILITY);
        stack.set(DataComponentTypes.DAMAGE, 0);
        AttributeModifierSlot modifierSlot = getSlotForPiece(pieceName);
        AttributeModifiersComponent.Builder attrBuilder = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR, new EntityAttributeModifier(Identifier.of("politicalserver", "shroomlight_" + pieceName.toLowerCase() + "_armor"), armorValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(EntityAttributes.ARMOR_TOUGHNESS, new EntityAttributeModifier(Identifier.of("politicalserver", "shroomlight_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal("🛡 Armor: +" + armorValue).formatted(Formatting.WHITE));
        lore.add(Text.literal("💎 Toughness: +" + toughnessValue).formatted(Formatting.WHITE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ SET BONUS ━━━").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal("✦ Full set: Permanent light").formatted(Formatting.GREEN));
        lore.add(Text.literal("✦ Night vision").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Bioluminescent").formatted(Formatting.DARK_GRAY));
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // SLIME ARMOR - Bouncy
    public static ItemStack createSlimeHelmet() {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        applyPlayerHeadTexture(stack, SLIME_HEAD);
        applySlimeStats(stack, "Helmet", 2, 1, EquipmentSlot.HEAD);
        setCustomItemId(stack, "slime_helmet");
        return stack;
    }
    public static ItemStack createSlimeChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applySlimeStats(stack, "Chestplate", 3, 1, EquipmentSlot.CHEST);
        setCustomItemId(stack, "slime_chestplate");
        return stack;
    }
    public static ItemStack createSlimeLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applySlimeStats(stack, "Leggings", 3, 1, EquipmentSlot.LEGS);
        setCustomItemId(stack, "slime_leggings");
        return stack;
    }
    public static ItemStack createSlimeBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applySlimeStats(stack, "Boots", 1, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "slime_boots");
        return stack;
    }
    private static void applySlimeStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        if (!stack.isOf(Items.PLAYER_HEAD)) stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(SLIME_COLOR));
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Slime " + pieceName).formatted(Formatting.GREEN, Formatting.BOLD));
        stack.set(DataComponentTypes.MAX_DAMAGE, SLIME_DURABILITY);
        stack.set(DataComponentTypes.DAMAGE, 0);
        AttributeModifierSlot modifierSlot = getSlotForPiece(pieceName);
        AttributeModifiersComponent.Builder attrBuilder = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR, new EntityAttributeModifier(Identifier.of("politicalserver", "slime_" + pieceName.toLowerCase() + "_armor"), armorValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(EntityAttributes.ARMOR_TOUGHNESS, new EntityAttributeModifier(Identifier.of("politicalserver", "slime_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.GREEN));
        lore.add(Text.literal("🛡 Armor: +" + armorValue).formatted(Formatting.WHITE));
        lore.add(Text.literal("💎 Toughness: +" + toughnessValue).formatted(Formatting.WHITE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ SET BONUS ━━━").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal("✦ Full set: Bounce jumps").formatted(Formatting.GREEN));
        lore.add(Text.literal("✦ Fall damage immunity").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Boing boing!").formatted(Formatting.DARK_GRAY));
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // SMOOTH STONE ARMOR - Refined
    public static ItemStack createSmoothStoneHelmet() {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
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
        if (!stack.isOf(Items.PLAYER_HEAD)) stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(SMOOTH_STONE_COLOR));
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Smooth Stone " + pieceName).formatted(Formatting.GRAY, Formatting.BOLD));
        stack.set(DataComponentTypes.MAX_DAMAGE, SMOOTH_STONE_DURABILITY);
        stack.set(DataComponentTypes.DAMAGE, 0);
        AttributeModifierSlot modifierSlot = getSlotForPiece(pieceName);
        AttributeModifiersComponent.Builder attrBuilder = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR, new EntityAttributeModifier(Identifier.of("politicalserver", "smooth_stone_" + pieceName.toLowerCase() + "_armor"), armorValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(EntityAttributes.ARMOR_TOUGHNESS, new EntityAttributeModifier(Identifier.of("politicalserver", "smooth_stone_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.GRAY));
        lore.add(Text.literal("🛡 Armor: +" + armorValue).formatted(Formatting.WHITE));
        lore.add(Text.literal("💎 Toughness: +" + toughnessValue).formatted(Formatting.WHITE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ SET BONUS ━━━").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal("✦ Full set: Smooth movement").formatted(Formatting.GREEN));
        lore.add(Text.literal("✦ Efficient mining").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Refined and polished").formatted(Formatting.DARK_GRAY));
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // SNOW ARMOR - Frozen
    public static ItemStack createSnowHelmet() {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
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
        if (!stack.isOf(Items.PLAYER_HEAD)) stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(SNOW_COLOR));
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Snow " + pieceName).formatted(Formatting.WHITE, Formatting.BOLD));
        stack.set(DataComponentTypes.MAX_DAMAGE, SNOW_DURABILITY);
        stack.set(DataComponentTypes.DAMAGE, 0);
        AttributeModifierSlot modifierSlot = getSlotForPiece(pieceName);
        AttributeModifiersComponent.Builder attrBuilder = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR, new EntityAttributeModifier(Identifier.of("politicalserver", "snow_" + pieceName.toLowerCase() + "_armor"), armorValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(EntityAttributes.ARMOR_TOUGHNESS, new EntityAttributeModifier(Identifier.of("politicalserver", "snow_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.WHITE));
        lore.add(Text.literal("🛡 Armor: +" + armorValue).formatted(Formatting.WHITE));
        lore.add(Text.literal("💎 Toughness: +" + toughnessValue).formatted(Formatting.WHITE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ SET BONUS ━━━").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal("✦ Full set: Freeze attackers").formatted(Formatting.GREEN));
        lore.add(Text.literal("✦ Frost walker").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Cold to the touch").formatted(Formatting.DARK_GRAY));
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // SOUL SOIL ARMOR - Fire immunity
    public static ItemStack createSoulSoilHelmet() {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
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
        if (!stack.isOf(Items.PLAYER_HEAD)) stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(SOUL_SOIL_COLOR));
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Soul Soil " + pieceName).formatted(Formatting.DARK_GRAY, Formatting.BOLD));
        stack.set(DataComponentTypes.MAX_DAMAGE, SOUL_SOIL_DURABILITY);
        stack.set(DataComponentTypes.DAMAGE, 0);
        AttributeModifierSlot modifierSlot = getSlotForPiece(pieceName);
        AttributeModifiersComponent.Builder attrBuilder = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR, new EntityAttributeModifier(Identifier.of("politicalserver", "soul_soil_" + pieceName.toLowerCase() + "_armor"), armorValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(EntityAttributes.ARMOR_TOUGHNESS, new EntityAttributeModifier(Identifier.of("politicalserver", "soul_soil_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("🛡 Armor: +" + armorValue).formatted(Formatting.WHITE));
        lore.add(Text.literal("💎 Toughness: +" + toughnessValue).formatted(Formatting.WHITE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ SET BONUS ━━━").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal("✦ Full set: Soul fire immunity").formatted(Formatting.GREEN));
        lore.add(Text.literal("✦ Soul speed boost").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Soul powered").formatted(Formatting.DARK_GRAY));
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // SPONGE ARMOR - Water absorption
    public static ItemStack createSpongeHelmet() {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
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
        if (!stack.isOf(Items.PLAYER_HEAD)) stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(SPONGE_COLOR));
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Sponge " + pieceName).formatted(Formatting.YELLOW, Formatting.BOLD));
        stack.set(DataComponentTypes.MAX_DAMAGE, SPONGE_DURABILITY);
        stack.set(DataComponentTypes.DAMAGE, 0);
        AttributeModifierSlot modifierSlot = getSlotForPiece(pieceName);
        AttributeModifiersComponent.Builder attrBuilder = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR, new EntityAttributeModifier(Identifier.of("politicalserver", "sponge_" + pieceName.toLowerCase() + "_armor"), armorValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(EntityAttributes.ARMOR_TOUGHNESS, new EntityAttributeModifier(Identifier.of("politicalserver", "sponge_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.YELLOW));
        lore.add(Text.literal("🛡 Armor: +" + armorValue).formatted(Formatting.WHITE));
        lore.add(Text.literal("💎 Toughness: +" + toughnessValue).formatted(Formatting.WHITE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ SET BONUS ━━━").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal("✦ Full set: Dry sponge effect").formatted(Formatting.GREEN));
        lore.add(Text.literal("✦ Water breathing").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Absorbent protection").formatted(Formatting.DARK_GRAY));
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // TARGET ARMOR - Precision
    public static ItemStack createTargetHelmet() {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        applyPlayerHeadTexture(stack, TARGET_HEAD);
        applyTargetStats(stack, "Helmet", 2, 1, EquipmentSlot.HEAD);
        setCustomItemId(stack, "target_helmet");
        return stack;
    }
    public static ItemStack createTargetChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyTargetStats(stack, "Chestplate", 4, 1, EquipmentSlot.CHEST);
        setCustomItemId(stack, "target_chestplate");
        return stack;
    }
    public static ItemStack createTargetLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyTargetStats(stack, "Leggings", 3, 1, EquipmentSlot.LEGS);
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
        if (!stack.isOf(Items.PLAYER_HEAD)) stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(TARGET_COLOR));
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Target " + pieceName).formatted(Formatting.RED, Formatting.BOLD));
        stack.set(DataComponentTypes.MAX_DAMAGE, TARGET_DURABILITY);
        stack.set(DataComponentTypes.DAMAGE, 0);
        AttributeModifierSlot modifierSlot = getSlotForPiece(pieceName);
        AttributeModifiersComponent.Builder attrBuilder = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR, new EntityAttributeModifier(Identifier.of("politicalserver", "target_" + pieceName.toLowerCase() + "_armor"), armorValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(EntityAttributes.ARMOR_TOUGHNESS, new EntityAttributeModifier(Identifier.of("politicalserver", "target_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.RED));
        lore.add(Text.literal("🛡 Armor: +" + armorValue).formatted(Formatting.WHITE));
        lore.add(Text.literal("💎 Toughness: +" + toughnessValue).formatted(Formatting.WHITE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ SET BONUS ━━━").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal("✦ Full set: Redstone signal").formatted(Formatting.GREEN));
        lore.add(Text.literal("✦ Projectile deflection").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Bullseye!").formatted(Formatting.DARK_GRAY));
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // TNT ARMOR - Explosive
    public static ItemStack createTntHelmet() {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        applyPlayerHeadTexture(stack, TNT_HEAD);
        applyTntStats(stack, "Helmet", 1, 0, EquipmentSlot.HEAD);
        setCustomItemId(stack, "tnt_helmet");
        return stack;
    }
    public static ItemStack createTntChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyTntStats(stack, "Chestplate", 2, 0, EquipmentSlot.CHEST);
        setCustomItemId(stack, "tnt_chestplate");
        return stack;
    }
    public static ItemStack createTntLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyTntStats(stack, "Leggings", 2, 0, EquipmentSlot.LEGS);
        setCustomItemId(stack, "tnt_leggings");
        return stack;
    }
    public static ItemStack createTntBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyTntStats(stack, "Boots", 1, 0, EquipmentSlot.FEET);
        setCustomItemId(stack, "tnt_boots");
        return stack;
    }
    private static void applyTntStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        if (!stack.isOf(Items.PLAYER_HEAD)) stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(TNT_COLOR));
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("TNT " + pieceName).formatted(Formatting.RED, Formatting.BOLD));
        stack.set(DataComponentTypes.MAX_DAMAGE, TNT_DURABILITY);
        stack.set(DataComponentTypes.DAMAGE, 0);
        AttributeModifierSlot modifierSlot = getSlotForPiece(pieceName);
        AttributeModifiersComponent.Builder attrBuilder = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR, new EntityAttributeModifier(Identifier.of("politicalserver", "tnt_" + pieceName.toLowerCase() + "_armor"), armorValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(EntityAttributes.ARMOR_TOUGHNESS, new EntityAttributeModifier(Identifier.of("politicalserver", "tnt_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.RED));
        lore.add(Text.literal("🛡 Armor: +" + armorValue).formatted(Formatting.WHITE));
        lore.add(Text.literal("💎 Toughness: +" + toughnessValue).formatted(Formatting.WHITE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ SET BONUS ━━━").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal("✦ Full set: Explosion immunity").formatted(Formatting.GREEN));
        lore.add(Text.literal("✦ Blast mining boost").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§4Handle with care!").formatted(Formatting.DARK_RED));
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // WARPED ARMOR - Nether forest
    public static ItemStack createWarpedHelmet() {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        applyPlayerHeadTexture(stack, WARPED_HEAD);
        applyWarpedStats(stack, "Helmet", 3, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "warped_helmet");
        return stack;
    }
    public static ItemStack createWarpedChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyWarpedStats(stack, "Chestplate", 5, 2, EquipmentSlot.CHEST);
        setCustomItemId(stack, "warped_chestplate");
        return stack;
    }
    public static ItemStack createWarpedLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyWarpedStats(stack, "Leggings", 4, 2, EquipmentSlot.LEGS);
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
        if (!stack.isOf(Items.PLAYER_HEAD)) stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(WARPED_COLOR));
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Warped " + pieceName).formatted(Formatting.DARK_AQUA, Formatting.BOLD));
        stack.set(DataComponentTypes.MAX_DAMAGE, WARPED_DURABILITY);
        stack.set(DataComponentTypes.DAMAGE, 0);
        AttributeModifierSlot modifierSlot = getSlotForPiece(pieceName);
        AttributeModifiersComponent.Builder attrBuilder = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR, new EntityAttributeModifier(Identifier.of("politicalserver", "warped_" + pieceName.toLowerCase() + "_armor"), armorValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(EntityAttributes.ARMOR_TOUGHNESS, new EntityAttributeModifier(Identifier.of("politicalserver", "warped_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_AQUA));
        lore.add(Text.literal("🛡 Armor: +" + armorValue).formatted(Formatting.WHITE));
        lore.add(Text.literal("💎 Toughness: +" + toughnessValue).formatted(Formatting.WHITE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ SET BONUS ━━━").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal("✦ Full set: Warped forest affinity").formatted(Formatting.GREEN));
        lore.add(Text.literal("✦ Endermen ignore you").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8From the warped forest").formatted(Formatting.DARK_GRAY));
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    // WET SPONGE ARMOR - Damp
    public static ItemStack createWetSpongeHelmet() {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        applyPlayerHeadTexture(stack, WET_SPONGE_HEAD);
        applyWetSpongeStats(stack, "Helmet", 2, 1, EquipmentSlot.HEAD);
        setCustomItemId(stack, "wet_sponge_helmet");
        return stack;
    }
    public static ItemStack createWetSpongeChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyWetSpongeStats(stack, "Chestplate", 4, 1, EquipmentSlot.CHEST);
        setCustomItemId(stack, "wet_sponge_chestplate");
        return stack;
    }
    public static ItemStack createWetSpongeLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyWetSpongeStats(stack, "Leggings", 3, 1, EquipmentSlot.LEGS);
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
        if (!stack.isOf(Items.PLAYER_HEAD)) stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(WET_SPONGE_COLOR));
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Wet Sponge " + pieceName).formatted(Formatting.DARK_GREEN, Formatting.BOLD));
        stack.set(DataComponentTypes.MAX_DAMAGE, WET_SPONGE_DURABILITY);
        stack.set(DataComponentTypes.DAMAGE, 0);
        AttributeModifierSlot modifierSlot = getSlotForPiece(pieceName);
        AttributeModifiersComponent.Builder attrBuilder = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR, new EntityAttributeModifier(Identifier.of("politicalserver", "wet_sponge_" + pieceName.toLowerCase() + "_armor"), armorValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot)
                .add(EntityAttributes.ARMOR_TOUGHNESS, new EntityAttributeModifier(Identifier.of("politicalserver", "wet_sponge_" + pieceName.toLowerCase() + "_toughness"), toughnessValue, EntityAttributeModifier.Operation.ADD_VALUE), modifierSlot);
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_GREEN));
        lore.add(Text.literal("🛡 Armor: +" + armorValue).formatted(Formatting.WHITE));
        lore.add(Text.literal("💎 Toughness: +" + toughnessValue).formatted(Formatting.WHITE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ SET BONUS ━━━").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal("✦ Full set: Fire extinguisher").formatted(Formatting.GREEN));
        lore.add(Text.literal("✦ Wet and wild").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Soggy but safe").formatted(Formatting.DARK_GRAY));
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
