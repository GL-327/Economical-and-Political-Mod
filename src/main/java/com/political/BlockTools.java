package com.political;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

/**
 * Block-based tools that match BlockArmor and BlockWeapons sets.
 * Each tool set includes pickaxe, axe, shovel, and hoe with themed abilities.
 */
public class BlockTools {

    // ============================================================
    // GLASS TOOLS - Fragile but efficient
    // ============================================================
    
    public static ItemStack createGlassPickaxe() {
        ItemStack tool = new ItemStack(Items.DIAMOND_PICKAXE);
        tool.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⛏ Glass Pickaxe").formatted(Formatting.AQUA, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.AQUA));
        lore.add(Text.literal("⛏ Mining Speed: Very Fast").formatted(Formatting.GREEN));
        lore.add(Text.literal("⛏ Durability: 50").formatted(Formatting.YELLOW));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ ABILITY:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• 20% chance to double ore drops").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Sharp but fragile").formatted(Formatting.DARK_GRAY));
        tool.set(DataComponentTypes.LORE, new LoreComponent(lore));
        tool.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        tool.set(DataComponentTypes.MAX_DAMAGE, 50);
        SlayerItems.setCustomItemId(tool, "glass_pickaxe");
        return tool;
    }
    
    public static ItemStack createGlassAxe() {
        ItemStack tool = new ItemStack(Items.DIAMOND_AXE);
        tool.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("🪓 Glass Axe").formatted(Formatting.AQUA, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.AQUA));
        lore.add(Text.literal("🪓 Chop Speed: Very Fast").formatted(Formatting.GREEN));
        lore.add(Text.literal("🪓 Durability: 50").formatted(Formatting.YELLOW));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ ABILITY:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Instant leaf decay when chopping").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Sharp but fragile").formatted(Formatting.DARK_GRAY));
        tool.set(DataComponentTypes.LORE, new LoreComponent(lore));
        tool.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        tool.set(DataComponentTypes.MAX_DAMAGE, 50);
        SlayerItems.setCustomItemId(tool, "glass_axe");
        return tool;
    }
    
    public static ItemStack createGlassShovel() {
        ItemStack tool = new ItemStack(Items.DIAMOND_SHOVEL);
        tool.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("🪏 Glass Shovel").formatted(Formatting.AQUA, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.AQUA));
        lore.add(Text.literal("🪏 Dig Speed: Very Fast").formatted(Formatting.GREEN));
        lore.add(Text.literal("🪏 Durability: 50").formatted(Formatting.YELLOW));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ ABILITY:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• 3x3 excavation mode (sneak)").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Sharp but fragile").formatted(Formatting.DARK_GRAY));
        tool.set(DataComponentTypes.LORE, new LoreComponent(lore));
        tool.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        tool.set(DataComponentTypes.MAX_DAMAGE, 50);
        SlayerItems.setCustomItemId(tool, "glass_shovel");
        return tool;
    }
    
    public static ItemStack createGlassHoe() {
        ItemStack tool = new ItemStack(Items.DIAMOND_HOE);
        tool.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("🌾 Glass Hoe").formatted(Formatting.AQUA, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.AQUA));
        lore.add(Text.literal("🌾 Tilling Speed: Fast").formatted(Formatting.GREEN));
        lore.add(Text.literal("🌾 Durability: 50").formatted(Formatting.YELLOW));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ ABILITY:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Auto-replant crops on harvest").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Sharp but fragile").formatted(Formatting.DARK_GRAY));
        tool.set(DataComponentTypes.LORE, new LoreComponent(lore));
        tool.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        tool.set(DataComponentTypes.MAX_DAMAGE, 50);
        SlayerItems.setCustomItemId(tool, "glass_hoe");
        return tool;
    }

    // ============================================================
    // OBSIDIAN TOOLS - Unbreakable, slow but powerful
    // ============================================================
    
    public static ItemStack createObsidianPickaxe() {
        ItemStack tool = new ItemStack(Items.NETHERITE_PICKAXE);
        tool.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⛏ Obsidian Pickaxe").formatted(Formatting.DARK_PURPLE, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_PURPLE));
        lore.add(Text.literal("⛏ Mining Speed: Moderate").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⛏ Durability: 2000").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ ABILITY:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Can break bedrock (very slow)").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Fortune III (built-in)").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Unbreakable edge").formatted(Formatting.DARK_GRAY));
        tool.set(DataComponentTypes.LORE, new LoreComponent(lore));
        tool.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        tool.set(DataComponentTypes.MAX_DAMAGE, 2000);
        SlayerItems.setCustomItemId(tool, "obsidian_pickaxe");
        return tool;
    }
    
    public static ItemStack createObsidianAxe() {
        ItemStack tool = new ItemStack(Items.NETHERITE_AXE);
        tool.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("🪓 Obsidian Axe").formatted(Formatting.DARK_PURPLE, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_PURPLE));
        lore.add(Text.literal("🪓 Chop Speed: Moderate").formatted(Formatting.YELLOW));
        lore.add(Text.literal("🪓 Durability: 2000").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ ABILITY:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• +50% damage to wooden structures").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Strips logs in 3x3 area").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Unbreakable edge").formatted(Formatting.DARK_GRAY));
        tool.set(DataComponentTypes.LORE, new LoreComponent(lore));
        tool.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        tool.set(DataComponentTypes.MAX_DAMAGE, 2000);
        SlayerItems.setCustomItemId(tool, "obsidian_axe");
        return tool;
    }
    
    public static ItemStack createObsidianShovel() {
        ItemStack tool = new ItemStack(Items.NETHERITE_SHOVEL);
        tool.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("🪏 Obsidian Shovel").formatted(Formatting.DARK_PURPLE, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_PURPLE));
        lore.add(Text.literal("🪏 Dig Speed: Moderate").formatted(Formatting.YELLOW));
        lore.add(Text.literal("🪏 Durability: 2000").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ ABILITY:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• 5x5 excavation mode (sneak)").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Path created is explosion-proof").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Unbreakable edge").formatted(Formatting.DARK_GRAY));
        tool.set(DataComponentTypes.LORE, new LoreComponent(lore));
        tool.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        tool.set(DataComponentTypes.MAX_DAMAGE, 2000);
        SlayerItems.setCustomItemId(tool, "obsidian_shovel");
        return tool;
    }
    
    public static ItemStack createObsidianHoe() {
        ItemStack tool = new ItemStack(Items.NETHERITE_HOE);
        tool.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("🌾 Obsidian Hoe").formatted(Formatting.DARK_PURPLE, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_PURPLE));
        lore.add(Text.literal("🌾 Tilling Speed: Moderate").formatted(Formatting.YELLOW));
        lore.add(Text.literal("🌾 Durability: 2000").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ ABILITY:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Tilled soil never tramples").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Crops grow 2x faster").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Unbreakable edge").formatted(Formatting.DARK_GRAY));
        tool.set(DataComponentTypes.LORE, new LoreComponent(lore));
        tool.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        tool.set(DataComponentTypes.MAX_DAMAGE, 2000);
        SlayerItems.setCustomItemId(tool, "obsidian_hoe");
        return tool;
    }

    // ============================================================
    // QUARTZ TOOLS - Crystalline and precise
    // ============================================================
    
    public static ItemStack createQuartzPickaxe() {
        ItemStack tool = new ItemStack(Items.DIAMOND_PICKAXE);
        tool.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⛏ Quartz Pickaxe").formatted(Formatting.WHITE, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.WHITE));
        lore.add(Text.literal("⛏ Mining Speed: Fast").formatted(Formatting.GREEN));
        lore.add(Text.literal("⛏ Durability: 300").formatted(Formatting.YELLOW));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ ABILITY:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• 15% chance for bonus quartz drop").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Silk Touch (built-in)").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Crystalline precision").formatted(Formatting.DARK_GRAY));
        tool.set(DataComponentTypes.LORE, new LoreComponent(lore));
        tool.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        tool.set(DataComponentTypes.MAX_DAMAGE, 300);
        SlayerItems.setCustomItemId(tool, "quartz_pickaxe");
        return tool;
    }
    
    public static ItemStack createQuartzAxe() {
        ItemStack tool = new ItemStack(Items.DIAMOND_AXE);
        tool.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("🪓 Quartz Axe").formatted(Formatting.WHITE, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.WHITE));
        lore.add(Text.literal("🪓 Chop Speed: Fast").formatted(Formatting.GREEN));
        lore.add(Text.literal("🪓 Durability: 300").formatted(Formatting.YELLOW));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ ABILITY:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• +25% damage to nether mobs").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Crystalline precision").formatted(Formatting.DARK_GRAY));
        tool.set(DataComponentTypes.LORE, new LoreComponent(lore));
        tool.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        tool.set(DataComponentTypes.MAX_DAMAGE, 300);
        SlayerItems.setCustomItemId(tool, "quartz_axe");
        return tool;
    }
    
    public static ItemStack createQuartzShovel() {
        ItemStack tool = new ItemStack(Items.DIAMOND_SHOVEL);
        tool.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("🪏 Quartz Shovel").formatted(Formatting.WHITE, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.WHITE));
        lore.add(Text.literal("🪏 Dig Speed: Fast").formatted(Formatting.GREEN));
        lore.add(Text.literal("🪏 Durability: 300").formatted(Formatting.YELLOW));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ ABILITY:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• +50% speed on soul sand").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Crystalline precision").formatted(Formatting.DARK_GRAY));
        tool.set(DataComponentTypes.LORE, new LoreComponent(lore));
        tool.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        tool.set(DataComponentTypes.MAX_DAMAGE, 300);
        SlayerItems.setCustomItemId(tool, "quartz_shovel");
        return tool;
    }
    
    public static ItemStack createQuartzHoe() {
        ItemStack tool = new ItemStack(Items.DIAMOND_HOE);
        tool.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("🌾 Quartz Hoe").formatted(Formatting.WHITE, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.WHITE));
        lore.add(Text.literal("🌾 Tilling Speed: Fast").formatted(Formatting.GREEN));
        lore.add(Text.literal("🌾 Durability: 300").formatted(Formatting.YELLOW));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ ABILITY:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Nether wart grows 2x faster").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Crystalline precision").formatted(Formatting.DARK_GRAY));
        tool.set(DataComponentTypes.LORE, new LoreComponent(lore));
        tool.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        tool.set(DataComponentTypes.MAX_DAMAGE, 300);
        SlayerItems.setCustomItemId(tool, "quartz_hoe");
        return tool;
    }

    // ============================================================
    // GLOWSTONE TOOLS - Illuminating
    // ============================================================
    
    public static ItemStack createGlowstonePickaxe() {
        ItemStack tool = new ItemStack(Items.GOLDEN_PICKAXE);
        tool.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⛏ Glowstone Pickaxe").formatted(Formatting.YELLOW, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⛏ Mining Speed: Fast").formatted(Formatting.GREEN));
        lore.add(Text.literal("⛏ Durability: 200").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⛏ Light Level: 15 (when held)").formatted(Formatting.GOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ ABILITY:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Illuminates caves while mining").formatted(Formatting.GREEN));
        lore.add(Text.literal("• 30% bonus glowstone dust drops").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Radiant edge").formatted(Formatting.DARK_GRAY));
        tool.set(DataComponentTypes.LORE, new LoreComponent(lore));
        tool.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        tool.set(DataComponentTypes.MAX_DAMAGE, 200);
        SlayerItems.setCustomItemId(tool, "glowstone_pickaxe");
        return tool;
    }
    
    public static ItemStack createGlowstoneAxe() {
        ItemStack tool = new ItemStack(Items.GOLDEN_AXE);
        tool.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("🪓 Glowstone Axe").formatted(Formatting.YELLOW, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.YELLOW));
        lore.add(Text.literal("🪓 Chop Speed: Very Fast").formatted(Formatting.GREEN));
        lore.add(Text.literal("🪓 Durability: 200").formatted(Formatting.YELLOW));
        lore.add(Text.literal("🪓 Light Level: 15 (when held)").formatted(Formatting.GOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ ABILITY:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• +50% damage to undead mobs").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Radiant edge").formatted(Formatting.DARK_GRAY));
        tool.set(DataComponentTypes.LORE, new LoreComponent(lore));
        tool.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        tool.set(DataComponentTypes.MAX_DAMAGE, 200);
        SlayerItems.setCustomItemId(tool, "glowstone_axe");
        return tool;
    }
    
    public static ItemStack createGlowstoneShovel() {
        ItemStack tool = new ItemStack(Items.GOLDEN_SHOVEL);
        tool.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("🪏 Glowstone Shovel").formatted(Formatting.YELLOW, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.YELLOW));
        lore.add(Text.literal("🪏 Dig Speed: Very Fast").formatted(Formatting.GREEN));
        lore.add(Text.literal("🪏 Durability: 200").formatted(Formatting.YELLOW));
        lore.add(Text.literal("🪏 Light Level: 15 (when held)").formatted(Formatting.GOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ ABILITY:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Reveals hidden ores nearby").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Radiant edge").formatted(Formatting.DARK_GRAY));
        tool.set(DataComponentTypes.LORE, new LoreComponent(lore));
        tool.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        tool.set(DataComponentTypes.MAX_DAMAGE, 200);
        SlayerItems.setCustomItemId(tool, "glowstone_shovel");
        return tool;
    }
    
    public static ItemStack createGlowstoneHoe() {
        ItemStack tool = new ItemStack(Items.GOLDEN_HOE);
        tool.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("🌾 Glowstone Hoe").formatted(Formatting.YELLOW, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.YELLOW));
        lore.add(Text.literal("🌾 Tilling Speed: Fast").formatted(Formatting.GREEN));
        lore.add(Text.literal("🌾 Durability: 200").formatted(Formatting.YELLOW));
        lore.add(Text.literal("🌾 Light Level: 15 (when held)").formatted(Formatting.GOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ ABILITY:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Crops grow 3x faster (light boost)").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Radiant edge").formatted(Formatting.DARK_GRAY));
        tool.set(DataComponentTypes.LORE, new LoreComponent(lore));
        tool.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        tool.set(DataComponentTypes.MAX_DAMAGE, 200);
        SlayerItems.setCustomItemId(tool, "glowstone_hoe");
        return tool;
    }

    // ============================================================
    // REDSTONE TOOLS - Technical and conductive
    // ============================================================
    
    public static ItemStack createRedstonePickaxe() {
        ItemStack tool = new ItemStack(Items.IRON_PICKAXE);
        tool.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⛏ Redstone Pickaxe").formatted(Formatting.RED, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.RED));
        lore.add(Text.literal("⛏ Mining Speed: Moderate").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⛏ Durability: 250").formatted(Formatting.YELLOW));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ ABILITY:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Auto-smelts mined ores").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Detects redstone ore nearby").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Conductive edge").formatted(Formatting.DARK_GRAY));
        tool.set(DataComponentTypes.LORE, new LoreComponent(lore));
        tool.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        tool.set(DataComponentTypes.MAX_DAMAGE, 250);
        SlayerItems.setCustomItemId(tool, "redstone_pickaxe");
        return tool;
    }
    
    public static ItemStack createRedstoneAxe() {
        ItemStack tool = new ItemStack(Items.IRON_AXE);
        tool.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("🪓 Redstone Axe").formatted(Formatting.RED, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.RED));
        lore.add(Text.literal("🪓 Chop Speed: Moderate").formatted(Formatting.YELLOW));
        lore.add(Text.literal("🪓 Durability: 250").formatted(Formatting.YELLOW));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ ABILITY:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Powered hits stun enemies").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Conductive edge").formatted(Formatting.DARK_GRAY));
        tool.set(DataComponentTypes.LORE, new LoreComponent(lore));
        tool.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        tool.set(DataComponentTypes.MAX_DAMAGE, 250);
        SlayerItems.setCustomItemId(tool, "redstone_axe");
        return tool;
    }
    
    public static ItemStack createRedstoneShovel() {
        ItemStack tool = new ItemStack(Items.IRON_SHOVEL);
        tool.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("🪏 Redstone Shovel").formatted(Formatting.RED, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.RED));
        lore.add(Text.literal("🪏 Dig Speed: Moderate").formatted(Formatting.YELLOW));
        lore.add(Text.literal("🪏 Durability: 250").formatted(Formatting.YELLOW));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ ABILITY:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Excavates 3x3 when powered").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Conductive edge").formatted(Formatting.DARK_GRAY));
        tool.set(DataComponentTypes.LORE, new LoreComponent(lore));
        tool.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        tool.set(DataComponentTypes.MAX_DAMAGE, 250);
        SlayerItems.setCustomItemId(tool, "redstone_shovel");
        return tool;
    }
    
    public static ItemStack createRedstoneHoe() {
        ItemStack tool = new ItemStack(Items.IRON_HOE);
        tool.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("🌾 Redstone Hoe").formatted(Formatting.RED, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.RED));
        lore.add(Text.literal("🌾 Tilling Speed: Moderate").formatted(Formatting.YELLOW));
        lore.add(Text.literal("🌾 Durability: 250").formatted(Formatting.YELLOW));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ ABILITY:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Auto-harvests mature crops").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Conductive edge").formatted(Formatting.DARK_GRAY));
        tool.set(DataComponentTypes.LORE, new LoreComponent(lore));
        tool.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        tool.set(DataComponentTypes.MAX_DAMAGE, 250);
        SlayerItems.setCustomItemId(tool, "redstone_hoe");
        return tool;
    }

    // ============================================================
    // NETHERRACK TOOLS - Infernal
    // ============================================================
    
    public static ItemStack createNetherrackPickaxe() {
        ItemStack tool = new ItemStack(Items.STONE_PICKAXE);
        tool.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⛏ Netherrack Pickaxe").formatted(Formatting.DARK_RED, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_RED));
        lore.add(Text.literal("⛏ Mining Speed: Moderate").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⛏ Durability: 180").formatted(Formatting.YELLOW));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ ABILITY:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• +50% speed in Nether").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Auto-smelts nether ores").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Infernal edge").formatted(Formatting.DARK_GRAY));
        tool.set(DataComponentTypes.LORE, new LoreComponent(lore));
        tool.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        tool.set(DataComponentTypes.MAX_DAMAGE, 180);
        SlayerItems.setCustomItemId(tool, "netherrack_pickaxe");
        return tool;
    }
    
    public static ItemStack createNetherrackAxe() {
        ItemStack tool = new ItemStack(Items.STONE_AXE);
        tool.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("🪓 Netherrack Axe").formatted(Formatting.DARK_RED, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_RED));
        lore.add(Text.literal("🪓 Chop Speed: Moderate").formatted(Formatting.YELLOW));
        lore.add(Text.literal("🪓 Durability: 180").formatted(Formatting.YELLOW));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ ABILITY:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Fire damage on hit").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Infernal edge").formatted(Formatting.DARK_GRAY));
        tool.set(DataComponentTypes.LORE, new LoreComponent(lore));
        tool.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        tool.set(DataComponentTypes.MAX_DAMAGE, 180);
        SlayerItems.setCustomItemId(tool, "netherrack_axe");
        return tool;
    }
    
    public static ItemStack createNetherrackShovel() {
        ItemStack tool = new ItemStack(Items.STONE_SHOVEL);
        tool.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("🪏 Netherrack Shovel").formatted(Formatting.DARK_RED, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_RED));
        lore.add(Text.literal("🪏 Dig Speed: Moderate").formatted(Formatting.YELLOW));
        lore.add(Text.literal("🪏 Durability: 180").formatted(Formatting.YELLOW));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ ABILITY:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Ignores soul sand slowdown").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Infernal edge").formatted(Formatting.DARK_GRAY));
        tool.set(DataComponentTypes.LORE, new LoreComponent(lore));
        tool.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        tool.set(DataComponentTypes.MAX_DAMAGE, 180);
        SlayerItems.setCustomItemId(tool, "netherrack_shovel");
        return tool;
    }
    
    public static ItemStack createNetherrackHoe() {
        ItemStack tool = new ItemStack(Items.STONE_HOE);
        tool.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("🌾 Netherrack Hoe").formatted(Formatting.DARK_RED, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_RED));
        lore.add(Text.literal("🌾 Tilling Speed: Moderate").formatted(Formatting.YELLOW));
        lore.add(Text.literal("🌾 Durability: 180").formatted(Formatting.YELLOW));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ ABILITY:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Nether wart grows 3x faster").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Infernal edge").formatted(Formatting.DARK_GRAY));
        tool.set(DataComponentTypes.LORE, new LoreComponent(lore));
        tool.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        tool.set(DataComponentTypes.MAX_DAMAGE, 180);
        SlayerItems.setCustomItemId(tool, "netherrack_hoe");
        return tool;
    }

    // ============================================================
    // END STONE TOOLS - Void-touched
    // ============================================================
    
    public static ItemStack createEndstonePickaxe() {
        ItemStack tool = new ItemStack(Items.DIAMOND_PICKAXE);
        tool.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⛏ End Stone Pickaxe").formatted(Formatting.DARK_GRAY, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("⛏ Mining Speed: Fast").formatted(Formatting.GREEN));
        lore.add(Text.literal("⛏ Durability: 350").formatted(Formatting.YELLOW));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ ABILITY:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Teleports mined items to you").formatted(Formatting.GREEN));
        lore.add(Text.literal("• +30% end stone mining speed").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Void-touched edge").formatted(Formatting.DARK_GRAY));
        tool.set(DataComponentTypes.LORE, new LoreComponent(lore));
        tool.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        tool.set(DataComponentTypes.MAX_DAMAGE, 350);
        SlayerItems.setCustomItemId(tool, "endstone_pickaxe");
        return tool;
    }
    
    public static ItemStack createEndstoneAxe() {
        ItemStack tool = new ItemStack(Items.DIAMOND_AXE);
        tool.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("🪓 End Stone Axe").formatted(Formatting.DARK_GRAY, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("🪓 Chop Speed: Fast").formatted(Formatting.GREEN));
        lore.add(Text.literal("🪓 Durability: 350").formatted(Formatting.YELLOW));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ ABILITY:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• +50% damage to ender mobs").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Void-touched edge").formatted(Formatting.DARK_GRAY));
        tool.set(DataComponentTypes.LORE, new LoreComponent(lore));
        tool.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        tool.set(DataComponentTypes.MAX_DAMAGE, 350);
        SlayerItems.setCustomItemId(tool, "endstone_axe");
        return tool;
    }
    
    public static ItemStack createEndstoneShovel() {
        ItemStack tool = new ItemStack(Items.DIAMOND_SHOVEL);
        tool.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("🪏 End Stone Shovel").formatted(Formatting.DARK_GRAY, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("🪏 Dig Speed: Fast").formatted(Formatting.GREEN));
        lore.add(Text.literal("🪏 Durability: 350").formatted(Formatting.YELLOW));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ ABILITY:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Teleports dug blocks to inventory").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Void-touched edge").formatted(Formatting.DARK_GRAY));
        tool.set(DataComponentTypes.LORE, new LoreComponent(lore));
        tool.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        tool.set(DataComponentTypes.MAX_DAMAGE, 350);
        SlayerItems.setCustomItemId(tool, "endstone_shovel");
        return tool;
    }
    
    public static ItemStack createEndstoneHoe() {
        ItemStack tool = new ItemStack(Items.DIAMOND_HOE);
        tool.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("🌾 End Stone Hoe").formatted(Formatting.DARK_GRAY, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("🌾 Tilling Speed: Fast").formatted(Formatting.GREEN));
        lore.add(Text.literal("🌾 Durability: 350").formatted(Formatting.YELLOW));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ ABILITY:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Chorus fruit grows 2x faster").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Void-touched edge").formatted(Formatting.DARK_GRAY));
        tool.set(DataComponentTypes.LORE, new LoreComponent(lore));
        tool.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        tool.set(DataComponentTypes.MAX_DAMAGE, 350);
        SlayerItems.setCustomItemId(tool, "endstone_hoe");
        return tool;
    }

    // ============================================================
    // ICE TOOLS - Freezing
    // ============================================================
    
    public static ItemStack createIcePickaxe() {
        ItemStack tool = new ItemStack(Items.DIAMOND_PICKAXE);
        tool.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⛏ Ice Pickaxe").formatted(Formatting.AQUA, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.AQUA));
        lore.add(Text.literal("⛏ Mining Speed: Fast").formatted(Formatting.GREEN));
        lore.add(Text.literal("⛏ Durability: 120").formatted(Formatting.YELLOW));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ ABILITY:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Freezes water/lava on break").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Frozen edge").formatted(Formatting.DARK_GRAY));
        tool.set(DataComponentTypes.LORE, new LoreComponent(lore));
        tool.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        tool.set(DataComponentTypes.MAX_DAMAGE, 120);
        SlayerItems.setCustomItemId(tool, "ice_pickaxe");
        return tool;
    }
    
    public static ItemStack createIceAxe() {
        ItemStack tool = new ItemStack(Items.DIAMOND_AXE);
        tool.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("🪓 Ice Axe").formatted(Formatting.AQUA, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.AQUA));
        lore.add(Text.literal("🪓 Chop Speed: Fast").formatted(Formatting.GREEN));
        lore.add(Text.literal("🪓 Durability: 120").formatted(Formatting.YELLOW));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ ABILITY:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Slows enemies on hit").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Frozen edge").formatted(Formatting.DARK_GRAY));
        tool.set(DataComponentTypes.LORE, new LoreComponent(lore));
        tool.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        tool.set(DataComponentTypes.MAX_DAMAGE, 120);
        SlayerItems.setCustomItemId(tool, "ice_axe");
        return tool;
    }
    
    public static ItemStack createIceShovel() {
        ItemStack tool = new ItemStack(Items.DIAMOND_SHOVEL);
        tool.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("🪏 Ice Shovel").formatted(Formatting.AQUA, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.AQUA));
        lore.add(Text.literal("🪏 Dig Speed: Fast").formatted(Formatting.GREEN));
        lore.add(Text.literal("🪏 Durability: 120").formatted(Formatting.YELLOW));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ ABILITY:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Places ice path on water").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Frozen edge").formatted(Formatting.DARK_GRAY));
        tool.set(DataComponentTypes.LORE, new LoreComponent(lore));
        tool.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        tool.set(DataComponentTypes.MAX_DAMAGE, 120);
        SlayerItems.setCustomItemId(tool, "ice_shovel");
        return tool;
    }
    
    public static ItemStack createIceHoe() {
        ItemStack tool = new ItemStack(Items.DIAMOND_HOE);
        tool.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("🌾 Ice Hoe").formatted(Formatting.AQUA, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.AQUA));
        lore.add(Text.literal("🌾 Tilling Speed: Fast").formatted(Formatting.GREEN));
        lore.add(Text.literal("🌾 Durability: 120").formatted(Formatting.YELLOW));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ ABILITY:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Preserves crops in cold biomes").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Frozen edge").formatted(Formatting.DARK_GRAY));
        tool.set(DataComponentTypes.LORE, new LoreComponent(lore));
        tool.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        tool.set(DataComponentTypes.MAX_DAMAGE, 120);
        SlayerItems.setCustomItemId(tool, "ice_hoe");
        return tool;
    }

    // ============================================================
    // PRISMARINE TOOLS - Ocean guardian
    // ============================================================
    
    public static ItemStack createPrismarinePickaxe() {
        ItemStack tool = new ItemStack(Items.DIAMOND_PICKAXE);
        tool.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⛏ Prismarine Pickaxe").formatted(Formatting.DARK_AQUA, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_AQUA));
        lore.add(Text.literal("⛏ Mining Speed: Fast underwater").formatted(Formatting.GREEN));
        lore.add(Text.literal("⛏ Durability: 280").formatted(Formatting.YELLOW));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ ABILITY:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• No underwater mining penalty").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Dropped items float").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Oceanforged edge").formatted(Formatting.DARK_GRAY));
        tool.set(DataComponentTypes.LORE, new LoreComponent(lore));
        tool.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        tool.set(DataComponentTypes.MAX_DAMAGE, 280);
        SlayerItems.setCustomItemId(tool, "prismarine_pickaxe");
        return tool;
    }
    
    public static ItemStack createPrismarineAxe() {
        ItemStack tool = new ItemStack(Items.DIAMOND_AXE);
        tool.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("🪓 Prismarine Axe").formatted(Formatting.DARK_AQUA, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_AQUA));
        lore.add(Text.literal("🪓 Chop Speed: Fast").formatted(Formatting.GREEN));
        lore.add(Text.literal("🪓 Durability: 280").formatted(Formatting.YELLOW));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ ABILITY:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• +50% damage to ocean mobs").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Oceanforged edge").formatted(Formatting.DARK_GRAY));
        tool.set(DataComponentTypes.LORE, new LoreComponent(lore));
        tool.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        tool.set(DataComponentTypes.MAX_DAMAGE, 280);
        SlayerItems.setCustomItemId(tool, "prismarine_axe");
        return tool;
    }
    
    public static ItemStack createPrismarineShovel() {
        ItemStack tool = new ItemStack(Items.DIAMOND_SHOVEL);
        tool.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("🪏 Prismarine Shovel").formatted(Formatting.DARK_AQUA, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_AQUA));
        lore.add(Text.literal("🪏 Dig Speed: Fast underwater").formatted(Formatting.GREEN));
        lore.add(Text.literal("🪏 Durability: 280").formatted(Formatting.YELLOW));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ ABILITY:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Excavates underwater in 3x3").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Oceanforged edge").formatted(Formatting.DARK_GRAY));
        tool.set(DataComponentTypes.LORE, new LoreComponent(lore));
        tool.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        tool.set(DataComponentTypes.MAX_DAMAGE, 280);
        SlayerItems.setCustomItemId(tool, "prismarine_shovel");
        return tool;
    }
    
    public static ItemStack createPrismarineHoe() {
        ItemStack tool = new ItemStack(Items.DIAMOND_HOE);
        tool.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("🌾 Prismarine Hoe").formatted(Formatting.DARK_AQUA, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_AQUA));
        lore.add(Text.literal("🌾 Tilling Speed: Fast").formatted(Formatting.GREEN));
        lore.add(Text.literal("🌾 Durability: 280").formatted(Formatting.YELLOW));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ ABILITY:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Auto-waters crops when used").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Oceanforged edge").formatted(Formatting.DARK_GRAY));
        tool.set(DataComponentTypes.LORE, new LoreComponent(lore));
        tool.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        tool.set(DataComponentTypes.MAX_DAMAGE, 280);
        SlayerItems.setCustomItemId(tool, "prismarine_hoe");
        return tool;
    }

    // ============================================================
    // TERRACOTTA TOOLS - Hardened clay
    // ============================================================
    
    public static ItemStack createTerracottaPickaxe() {
        ItemStack tool = new ItemStack(Items.IRON_PICKAXE);
        tool.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⛏ Terracotta Pickaxe").formatted(Formatting.GOLD, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("⛏ Mining Speed: Moderate").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⛏ Durability: 400").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ ABILITY:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• +50% speed on terracotta/clay").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Hardened edge").formatted(Formatting.DARK_GRAY));
        tool.set(DataComponentTypes.LORE, new LoreComponent(lore));
        tool.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        tool.set(DataComponentTypes.MAX_DAMAGE, 400);
        SlayerItems.setCustomItemId(tool, "terracotta_pickaxe");
        return tool;
    }
    
    public static ItemStack createTerracottaAxe() {
        ItemStack tool = new ItemStack(Items.IRON_AXE);
        tool.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("🪓 Terracotta Axe").formatted(Formatting.GOLD, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("🪓 Chop Speed: Moderate").formatted(Formatting.YELLOW));
        lore.add(Text.literal("🪓 Durability: 400").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ ABILITY:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• High knockback on hit").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Hardened edge").formatted(Formatting.DARK_GRAY));
        tool.set(DataComponentTypes.LORE, new LoreComponent(lore));
        tool.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        tool.set(DataComponentTypes.MAX_DAMAGE, 400);
        SlayerItems.setCustomItemId(tool, "terracotta_axe");
        return tool;
    }
    
    public static ItemStack createTerracottaShovel() {
        ItemStack tool = new ItemStack(Items.IRON_SHOVEL);
        tool.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("🪏 Terracotta Shovel").formatted(Formatting.GOLD, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("🪏 Dig Speed: Moderate").formatted(Formatting.YELLOW));
        lore.add(Text.literal("🪏 Durability: 400").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ ABILITY:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• +100% speed on sand/clay").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Hardened edge").formatted(Formatting.DARK_GRAY));
        tool.set(DataComponentTypes.LORE, new LoreComponent(lore));
        tool.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        tool.set(DataComponentTypes.MAX_DAMAGE, 400);
        SlayerItems.setCustomItemId(tool, "terracotta_shovel");
        return tool;
    }
    
    public static ItemStack createTerracottaHoe() {
        ItemStack tool = new ItemStack(Items.IRON_HOE);
        tool.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("🌾 Terracotta Hoe").formatted(Formatting.GOLD, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("🌾 Tilling Speed: Moderate").formatted(Formatting.YELLOW));
        lore.add(Text.literal("🌾 Durability: 400").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ ABILITY:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Crops survive in hot biomes").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Hardened edge").formatted(Formatting.DARK_GRAY));
        tool.set(DataComponentTypes.LORE, new LoreComponent(lore));
        tool.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        tool.set(DataComponentTypes.MAX_DAMAGE, 400);
        SlayerItems.setCustomItemId(tool, "terracotta_hoe");
        return tool;
    }

    // ============================================================
    // MOSSY TOOLS - Ancient overgrown
    // ============================================================
    
    public static ItemStack createMossyPickaxe() {
        ItemStack tool = new ItemStack(Items.IRON_PICKAXE);
        tool.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⛏ Mossy Pickaxe").formatted(Formatting.DARK_GREEN, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_GREEN));
        lore.add(Text.literal("⛏ Mining Speed: Moderate").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⛏ Durability: 450").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ ABILITY:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• +50% speed on stone blocks").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Moss spreads to nearby stone").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Ancient edge").formatted(Formatting.DARK_GRAY));
        tool.set(DataComponentTypes.LORE, new LoreComponent(lore));
        tool.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        tool.set(DataComponentTypes.MAX_DAMAGE, 450);
        SlayerItems.setCustomItemId(tool, "mossy_pickaxe");
        return tool;
    }
    
    public static ItemStack createMossyAxe() {
        ItemStack tool = new ItemStack(Items.IRON_AXE);
        tool.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("🪓 Mossy Axe").formatted(Formatting.DARK_GREEN, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_GREEN));
        lore.add(Text.literal("🪓 Chop Speed: Moderate").formatted(Formatting.YELLOW));
        lore.add(Text.literal("🪓 Durability: 450").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ ABILITY:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Replants saplings automatically").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Ancient edge").formatted(Formatting.DARK_GRAY));
        tool.set(DataComponentTypes.LORE, new LoreComponent(lore));
        tool.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        tool.set(DataComponentTypes.MAX_DAMAGE, 450);
        SlayerItems.setCustomItemId(tool, "mossy_axe");
        return tool;
    }
    
    public static ItemStack createMossyShovel() {
        ItemStack tool = new ItemStack(Items.IRON_SHOVEL);
        tool.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("🪏 Mossy Shovel").formatted(Formatting.DARK_GREEN, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_GREEN));
        lore.add(Text.literal("🪏 Dig Speed: Moderate").formatted(Formatting.YELLOW));
        lore.add(Text.literal("🪏 Durability: 450").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ ABILITY:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Converts dirt to grass path").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Ancient edge").formatted(Formatting.DARK_GRAY));
        tool.set(DataComponentTypes.LORE, new LoreComponent(lore));
        tool.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        tool.set(DataComponentTypes.MAX_DAMAGE, 450);
        SlayerItems.setCustomItemId(tool, "mossy_shovel");
        return tool;
    }
    
    public static ItemStack createMossyHoe() {
        ItemStack tool = new ItemStack(Items.IRON_HOE);
        tool.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("🌾 Mossy Hoe").formatted(Formatting.DARK_GREEN, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_GREEN));
        lore.add(Text.literal("🌾 Tilling Speed: Moderate").formatted(Formatting.YELLOW));
        lore.add(Text.literal("🌾 Durability: 450").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ ABILITY:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Crops grow 1.5x faster").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Auto-bonemeals occasionally").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Ancient edge").formatted(Formatting.DARK_GRAY));
        tool.set(DataComponentTypes.LORE, new LoreComponent(lore));
        tool.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        tool.set(DataComponentTypes.MAX_DAMAGE, 450);
        SlayerItems.setCustomItemId(tool, "mossy_hoe");
        return tool;
    }

    // ============================================================
    // SOUL SAND TOOLS - Soul-infused
    // ============================================================
    
    public static ItemStack createSoulSandPickaxe() {
        ItemStack tool = new ItemStack(Items.STONE_PICKAXE);
        tool.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⛏ Soul Sand Pickaxe").formatted(Formatting.DARK_GRAY, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("⛏ Mining Speed: Slow").formatted(Formatting.RED));
        lore.add(Text.literal("⛏ Durability: 220").formatted(Formatting.YELLOW));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ ABILITY:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Slows enemies when you hit them").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Souls collect in weapon").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Soul-infused edge").formatted(Formatting.DARK_GRAY));
        tool.set(DataComponentTypes.LORE, new LoreComponent(lore));
        tool.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        tool.set(DataComponentTypes.MAX_DAMAGE, 220);
        SlayerItems.setCustomItemId(tool, "soul_sand_pickaxe");
        return tool;
    }
    
    public static ItemStack createSoulSandAxe() {
        ItemStack tool = new ItemStack(Items.STONE_AXE);
        tool.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("🪓 Soul Sand Axe").formatted(Formatting.DARK_GRAY, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("🪓 Chop Speed: Slow").formatted(Formatting.RED));
        lore.add(Text.literal("🪓 Durability: 220").formatted(Formatting.YELLOW));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ ABILITY:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Wither effect on hit").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Soul-infused edge").formatted(Formatting.DARK_GRAY));
        tool.set(DataComponentTypes.LORE, new LoreComponent(lore));
        tool.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        tool.set(DataComponentTypes.MAX_DAMAGE, 220);
        SlayerItems.setCustomItemId(tool, "soul_sand_axe");
        return tool;
    }
    
    public static ItemStack createSoulSandShovel() {
        ItemStack tool = new ItemStack(Items.STONE_SHOVEL);
        tool.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("🪏 Soul Sand Shovel").formatted(Formatting.DARK_GRAY, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("🪏 Dig Speed: Slow").formatted(Formatting.RED));
        lore.add(Text.literal("🪏 Durability: 220").formatted(Formatting.YELLOW));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ ABILITY:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Normal speed on soul sand/soil").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Soul-infused edge").formatted(Formatting.DARK_GRAY));
        tool.set(DataComponentTypes.LORE, new LoreComponent(lore));
        tool.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        tool.set(DataComponentTypes.MAX_DAMAGE, 220);
        SlayerItems.setCustomItemId(tool, "soul_sand_shovel");
        return tool;
    }
    
    public static ItemStack createSoulSandHoe() {
        ItemStack tool = new ItemStack(Items.STONE_HOE);
        tool.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("🌾 Soul Sand Hoe").formatted(Formatting.DARK_GRAY, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("🌾 Tilling Speed: Slow").formatted(Formatting.RED));
        lore.add(Text.literal("🌾 Durability: 220").formatted(Formatting.YELLOW));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ ABILITY:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Soul sand crops grow faster").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Soul-infused edge").formatted(Formatting.DARK_GRAY));
        tool.set(DataComponentTypes.LORE, new LoreComponent(lore));
        tool.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        tool.set(DataComponentTypes.MAX_DAMAGE, 220);
        SlayerItems.setCustomItemId(tool, "soul_sand_hoe");
        return tool;
    }

    // ============================================================
    // MAGMA TOOLS - Burning hot
    // ============================================================
    
    public static ItemStack createMagmaPickaxe() {
        ItemStack tool = new ItemStack(Items.IRON_PICKAXE);
        tool.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⛏ Magma Pickaxe").formatted(Formatting.DARK_RED, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_RED));
        lore.add(Text.literal("⛏ Mining Speed: Moderate").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⛏ Durability: 350").formatted(Formatting.YELLOW));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ ABILITY:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Auto-smelts ores in Nether").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Fire damage to enemies hit").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Burning edge").formatted(Formatting.DARK_GRAY));
        tool.set(DataComponentTypes.LORE, new LoreComponent(lore));
        tool.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        tool.set(DataComponentTypes.MAX_DAMAGE, 350);
        SlayerItems.setCustomItemId(tool, "magma_pickaxe");
        return tool;
    }
    
    public static ItemStack createMagmaAxe() {
        ItemStack tool = new ItemStack(Items.IRON_AXE);
        tool.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("🪓 Magma Axe").formatted(Formatting.DARK_RED, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_RED));
        lore.add(Text.literal("🪓 Chop Speed: Moderate").formatted(Formatting.YELLOW));
        lore.add(Text.literal("🪓 Durability: 350").formatted(Formatting.YELLOW));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ ABILITY:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Burns enemies on hit").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Burning edge").formatted(Formatting.DARK_GRAY));
        tool.set(DataComponentTypes.LORE, new LoreComponent(lore));
        tool.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        tool.set(DataComponentTypes.MAX_DAMAGE, 350);
        SlayerItems.setCustomItemId(tool, "magma_axe");
        return tool;
    }
    
    public static ItemStack createMagmaShovel() {
        ItemStack tool = new ItemStack(Items.IRON_SHOVEL);
        tool.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("🪏 Magma Shovel").formatted(Formatting.DARK_RED, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_RED));
        lore.add(Text.literal("🪏 Dig Speed: Moderate").formatted(Formatting.YELLOW));
        lore.add(Text.literal("🪏 Durability: 350").formatted(Formatting.YELLOW));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ ABILITY:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Turns water to stone when dug").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Burning edge").formatted(Formatting.DARK_GRAY));
        tool.set(DataComponentTypes.LORE, new LoreComponent(lore));
        tool.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        tool.set(DataComponentTypes.MAX_DAMAGE, 350);
        SlayerItems.setCustomItemId(tool, "magma_shovel");
        return tool;
    }
    
    public static ItemStack createMagmaHoe() {
        ItemStack tool = new ItemStack(Items.IRON_HOE);
        tool.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("🌾 Magma Hoe").formatted(Formatting.DARK_RED, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_RED));
        lore.add(Text.literal("🌾 Tilling Speed: Moderate").formatted(Formatting.YELLOW));
        lore.add(Text.literal("🌾 Durability: 350").formatted(Formatting.YELLOW));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ ABILITY:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Nether crops grow 2x faster").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Burning edge").formatted(Formatting.DARK_GRAY));
        tool.set(DataComponentTypes.LORE, new LoreComponent(lore));
        tool.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        tool.set(DataComponentTypes.MAX_DAMAGE, 350);
        SlayerItems.setCustomItemId(tool, "magma_hoe");
        return tool;
    }

    // ============================================================
    // SANDSTONE TOOLS - Desert protector
    // ============================================================
    
    public static ItemStack createSandstonePickaxe() {
        ItemStack tool = new ItemStack(Items.IRON_PICKAXE);
        tool.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⛏ Sandstone Pickaxe").formatted(Formatting.YELLOW, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⛏ Mining Speed: Moderate").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⛏ Durability: 320").formatted(Formatting.YELLOW));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ ABILITY:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• +100% speed on sand/sandstone").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Desert edge").formatted(Formatting.DARK_GRAY));
        tool.set(DataComponentTypes.LORE, new LoreComponent(lore));
        tool.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        tool.set(DataComponentTypes.MAX_DAMAGE, 320);
        SlayerItems.setCustomItemId(tool, "sandstone_pickaxe");
        return tool;
    }
    
    public static ItemStack createSandstoneAxe() {
        ItemStack tool = new ItemStack(Items.IRON_AXE);
        tool.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("🪓 Sandstone Axe").formatted(Formatting.YELLOW, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.YELLOW));
        lore.add(Text.literal("🪓 Chop Speed: Moderate").formatted(Formatting.YELLOW));
        lore.add(Text.literal("🪓 Durability: 320").formatted(Formatting.YELLOW));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ ABILITY:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• +30% damage in desert biomes").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Desert edge").formatted(Formatting.DARK_GRAY));
        tool.set(DataComponentTypes.LORE, new LoreComponent(lore));
        tool.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        tool.set(DataComponentTypes.MAX_DAMAGE, 320);
        SlayerItems.setCustomItemId(tool, "sandstone_axe");
        return tool;
    }
    
    public static ItemStack createSandstoneShovel() {
        ItemStack tool = new ItemStack(Items.IRON_SHOVEL);
        tool.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("🪏 Sandstone Shovel").formatted(Formatting.YELLOW, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.YELLOW));
        lore.add(Text.literal("🪏 Dig Speed: Fast on sand").formatted(Formatting.GREEN));
        lore.add(Text.literal("🪏 Durability: 320").formatted(Formatting.YELLOW));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ ABILITY:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Excavates sand in 3x3 area").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Desert edge").formatted(Formatting.DARK_GRAY));
        tool.set(DataComponentTypes.LORE, new LoreComponent(lore));
        tool.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        tool.set(DataComponentTypes.MAX_DAMAGE, 320);
        SlayerItems.setCustomItemId(tool, "sandstone_shovel");
        return tool;
    }
    
    public static ItemStack createSandstoneHoe() {
        ItemStack tool = new ItemStack(Items.IRON_HOE);
        tool.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("🌾 Sandstone Hoe").formatted(Formatting.YELLOW, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.YELLOW));
        lore.add(Text.literal("🌾 Tilling Speed: Moderate").formatted(Formatting.YELLOW));
        lore.add(Text.literal("🌾 Durability: 320").formatted(Formatting.YELLOW));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ ABILITY:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Crops survive in desert biomes").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Desert edge").formatted(Formatting.DARK_GRAY));
        tool.set(DataComponentTypes.LORE, new LoreComponent(lore));
        tool.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        tool.set(DataComponentTypes.MAX_DAMAGE, 320);
        SlayerItems.setCustomItemId(tool, "sandstone_hoe");
        return tool;
    }

    // ============================================================
    // AMETHYST TOOLS - Crystalline resonance
    // ============================================================
    
    public static ItemStack createAmethystPickaxe() {
        ItemStack tool = new ItemStack(Items.DIAMOND_PICKAXE);
        tool.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⛏ Amethyst Pickaxe").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal("⛏ Mining Speed: Fast").formatted(Formatting.GREEN));
        lore.add(Text.literal("⛏ Durability: 380").formatted(Formatting.YELLOW));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ ABILITY:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• 25% bonus amethyst shard drops").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Echo locates nearby ores").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Resonant edge").formatted(Formatting.DARK_GRAY));
        tool.set(DataComponentTypes.LORE, new LoreComponent(lore));
        tool.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        tool.set(DataComponentTypes.MAX_DAMAGE, 380);
        SlayerItems.setCustomItemId(tool, "amethyst_pickaxe");
        return tool;
    }
    
    public static ItemStack createAmethystAxe() {
        ItemStack tool = new ItemStack(Items.DIAMOND_AXE);
        tool.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("🪓 Amethyst Axe").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal("🪓 Chop Speed: Fast").formatted(Formatting.GREEN));
        lore.add(Text.literal("🪓 Durability: 380").formatted(Formatting.YELLOW));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ ABILITY:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Sonic boom on critical hit").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Resonant edge").formatted(Formatting.DARK_GRAY));
        tool.set(DataComponentTypes.LORE, new LoreComponent(lore));
        tool.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        tool.set(DataComponentTypes.MAX_DAMAGE, 380);
        SlayerItems.setCustomItemId(tool, "amethyst_axe");
        return tool;
    }
    
    public static ItemStack createAmethystShovel() {
        ItemStack tool = new ItemStack(Items.DIAMOND_SHOVEL);
        tool.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("🪏 Amethyst Shovel").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal("🪏 Dig Speed: Fast").formatted(Formatting.GREEN));
        lore.add(Text.literal("🪏 Durability: 380").formatted(Formatting.YELLOW));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ ABILITY:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Echo reveals buried treasure").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Resonant edge").formatted(Formatting.DARK_GRAY));
        tool.set(DataComponentTypes.LORE, new LoreComponent(lore));
        tool.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        tool.set(DataComponentTypes.MAX_DAMAGE, 380);
        SlayerItems.setCustomItemId(tool, "amethyst_shovel");
        return tool;
    }
    
    public static ItemStack createAmethystHoe() {
        ItemStack tool = new ItemStack(Items.DIAMOND_HOE);
        tool.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("🌾 Amethyst Hoe").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal("🌾 Tilling Speed: Fast").formatted(Formatting.GREEN));
        lore.add(Text.literal("🌾 Durability: 380").formatted(Formatting.YELLOW));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ ABILITY:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Crystal growth boost nearby").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Resonant edge").formatted(Formatting.DARK_GRAY));
        tool.set(DataComponentTypes.LORE, new LoreComponent(lore));
        tool.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        tool.set(DataComponentTypes.MAX_DAMAGE, 380);
        SlayerItems.setCustomItemId(tool, "amethyst_hoe");
        return tool;
    }

    // ============================================================
    // COAL TOOLS - Carbon-compressed
    // ============================================================
    
    public static ItemStack createCoalPickaxe() {
        ItemStack tool = new ItemStack(Items.STONE_PICKAXE);
        tool.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⛏ Coal Pickaxe").formatted(Formatting.DARK_GRAY, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("⛏ Mining Speed: Moderate").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⛏ Durability: 280").formatted(Formatting.YELLOW));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ ABILITY:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• +50% coal drop rate").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Fuel efficiency (tools last longer)").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Compressed carbon edge").formatted(Formatting.DARK_GRAY));
        tool.set(DataComponentTypes.LORE, new LoreComponent(lore));
        tool.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        tool.set(DataComponentTypes.MAX_DAMAGE, 280);
        SlayerItems.setCustomItemId(tool, "coal_pickaxe");
        return tool;
    }
    
    public static ItemStack createCoalAxe() {
        ItemStack tool = new ItemStack(Items.STONE_AXE);
        tool.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("🪓 Coal Axe").formatted(Formatting.DARK_GRAY, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("🪓 Chop Speed: Moderate").formatted(Formatting.YELLOW));
        lore.add(Text.literal("🪓 Durability: 280").formatted(Formatting.YELLOW));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ ABILITY:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Blindness effect on hit").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Compressed carbon edge").formatted(Formatting.DARK_GRAY));
        tool.set(DataComponentTypes.LORE, new LoreComponent(lore));
        tool.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        tool.set(DataComponentTypes.MAX_DAMAGE, 280);
        SlayerItems.setCustomItemId(tool, "coal_axe");
        return tool;
    }
    
    public static ItemStack createCoalShovel() {
        ItemStack tool = new ItemStack(Items.STONE_SHOVEL);
        tool.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("🪏 Coal Shovel").formatted(Formatting.DARK_GRAY, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("🪏 Dig Speed: Moderate").formatted(Formatting.YELLOW));
        lore.add(Text.literal("🪏 Durability: 280").formatted(Formatting.YELLOW));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ ABILITY:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Darkens area when digging").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Compressed carbon edge").formatted(Formatting.DARK_GRAY));
        tool.set(DataComponentTypes.LORE, new LoreComponent(lore));
        tool.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        tool.set(DataComponentTypes.MAX_DAMAGE, 280);
        SlayerItems.setCustomItemId(tool, "coal_shovel");
        return tool;
    }
    
    public static ItemStack createCoalHoe() {
        ItemStack tool = new ItemStack(Items.STONE_HOE);
        tool.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("🌾 Coal Hoe").formatted(Formatting.DARK_GRAY, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("🌾 Tilling Speed: Moderate").formatted(Formatting.YELLOW));
        lore.add(Text.literal("🌾 Durability: 280").formatted(Formatting.YELLOW));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ ABILITY:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Crops grow at night too").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Compressed carbon edge").formatted(Formatting.DARK_GRAY));
        tool.set(DataComponentTypes.LORE, new LoreComponent(lore));
        tool.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        tool.set(DataComponentTypes.MAX_DAMAGE, 280);
        SlayerItems.setCustomItemId(tool, "coal_hoe");
        return tool;
    }

    // ============================================================
    // 36+ NEW BLOCK TOOL SETS - Matching BlockArmor and BlockWeapons
    // ============================================================

    // DIAMOND BLOCK TOOLS - Ultimate mining power
    public static ItemStack createDiamondBlockPickaxe() { return createBlockTool("Diamond Block", "Pickaxe", "⛏", Items.DIAMOND_PICKAXE, Formatting.AQUA, 3000, "Instant ore breaking", 8, "pickaxe"); }
    public static ItemStack createDiamondBlockAxe() { return createBlockTool("Diamond Block", "Axe", "🪓", Items.DIAMOND_AXE, Formatting.AQUA, 3000, "Instant tree felling", 9, "axe"); }
    public static ItemStack createDiamondBlockShovel() { return createBlockTool("Diamond Block", "Shovel", "🪏", Items.DIAMOND_SHOVEL, Formatting.AQUA, 3000, "5x5 excavation", 7, "shovel"); }
    public static ItemStack createDiamondBlockHoe() { return createBlockTool("Diamond Block", "Hoe", "🌾", Items.DIAMOND_HOE, Formatting.AQUA, 3000, "Auto-harvest 7x7", 6, "hoe"); }

    // EMERALD BLOCK TOOLS - Merchant's fortune
    public static ItemStack createEmeraldBlockPickaxe() { return createBlockTool("Emerald Block", "Pickaxe", "⛏", Items.DIAMOND_PICKAXE, Formatting.GREEN, 400, "Looting on ores", 6, "pickaxe"); }
    public static ItemStack createEmeraldBlockAxe() { return createBlockTool("Emerald Block", "Axe", "🪓", Items.DIAMOND_AXE, Formatting.GREEN, 400, "Rich wood drops", 6, "axe"); }
    public static ItemStack createEmeraldBlockShovel() { return createBlockTool("Emerald Block", "Shovel", "🪏", Items.DIAMOND_SHOVEL, Formatting.GREEN, 400, "Treasure finding", 5, "shovel"); }
    public static ItemStack createEmeraldBlockHoe() { return createBlockTool("Emerald Block", "Hoe", "🌾", Items.DIAMOND_HOE, Formatting.GREEN, 400, "Profitable harvests", 4, "hoe"); }

    // GOLD BLOCK TOOLS - Lucky mining
    public static ItemStack createGoldBlockPickaxe() { return createBlockTool("Gold Block", "Pickaxe", "⛏", Items.GOLDEN_PICKAXE, Formatting.YELLOW, 150, "Fortune III built-in", 4, "pickaxe"); }
    public static ItemStack createGoldBlockAxe() { return createBlockTool("Gold Block", "Axe", "🪓", Items.GOLDEN_AXE, Formatting.YELLOW, 150, "Golden apples from leaves", 4, "axe"); }
    public static ItemStack createGoldBlockShovel() { return createBlockTool("Gold Block", "Shovel", "🪏", Items.GOLDEN_SHOVEL, Formatting.YELLOW, 150, "Gold nuggets from sand", 3, "shovel"); }
    public static ItemStack createGoldBlockHoe() { return createBlockTool("Gold Block", "Hoe", "🌾", Items.GOLDEN_HOE, Formatting.YELLOW, 150, "Golden carrots bonus", 2, "hoe"); }

    // IRON BLOCK TOOLS - Industrial strength
    public static ItemStack createIronBlockPickaxe() { return createBlockTool("Iron Block", "Pickaxe", "⛏", Items.IRON_PICKAXE, Formatting.WHITE, 600, "Magnetic ore pull", 6, "pickaxe"); }
    public static ItemStack createIronBlockAxe() { return createBlockTool("Iron Block", "Axe", "🪓", Items.IRON_AXE, Formatting.WHITE, 600, "Magnetic log pull", 6, "axe"); }
    public static ItemStack createIronBlockShovel() { return createBlockTool("Iron Block", "Shovel", "🪏", Items.IRON_SHOVEL, Formatting.WHITE, 600, "Auto-collect drops", 5, "shovel"); }
    public static ItemStack createIronBlockHoe() { return createBlockTool("Iron Block", "Hoe", "🌾", Items.IRON_HOE, Formatting.WHITE, 600, "Industrial farming", 4, "hoe"); }

    // LAPIS BLOCK TOOLS - Enchanted mining
    public static ItemStack createLapisBlockPickaxe() { return createBlockTool("Lapis Block", "Pickaxe", "⛏", Items.DIAMOND_PICKAXE, Formatting.BLUE, 350, "XP magnet", 6, "pickaxe"); }
    public static ItemStack createLapisBlockAxe() { return createBlockTool("Lapis Block", "Axe", "🪓", Items.DIAMOND_AXE, Formatting.BLUE, 350, "Knowledge from wood", 6, "axe"); }
    public static ItemStack createLapisBlockShovel() { return createBlockTool("Lapis Block", "Shovel", "🪏", Items.DIAMOND_SHOVEL, Formatting.BLUE, 350, "Lapis bonus drops", 5, "shovel"); }
    public static ItemStack createLapisBlockHoe() { return createBlockTool("Lapis Block", "Hoe", "🌾", Items.DIAMOND_HOE, Formatting.BLUE, 350, "Wisdom harvest", 4, "hoe"); }

    // COPPER BLOCK TOOLS - Charged tools
    public static ItemStack createCopperBlockPickaxe() { return createBlockTool("Copper Block", "Pickaxe", "⛏", Items.IRON_PICKAXE, Formatting.GOLD, 280, "Lightning charged mining", 5, "pickaxe"); }
    public static ItemStack createCopperBlockAxe() { return createBlockTool("Copper Block", "Axe", "🪓", Items.IRON_AXE, Formatting.GOLD, 280, "Shock chopping", 5, "axe"); }
    public static ItemStack createCopperBlockShovel() { return createBlockTool("Copper Block", "Shovel", "🪏", Items.IRON_SHOVEL, Formatting.GOLD, 280, "Conductive digging", 4, "shovel"); }
    public static ItemStack createCopperBlockHoe() { return createBlockTool("Copper Block", "Hoe", "🌾", Items.IRON_HOE, Formatting.GOLD, 280, "Electric tilling", 3, "hoe"); }

    // ANCIENT DEBRIS TOOLS - Netherite ancient
    public static ItemStack createAncientDebrisPickaxe() { return createBlockTool("Ancient Debris", "Pickaxe", "⛏", Items.NETHERITE_PICKAXE, Formatting.DARK_GRAY, 2500, "Fire immune mining", 8, "pickaxe"); }
    public static ItemStack createAncientDebrisAxe() { return createBlockTool("Ancient Debris", "Axe", "🪓", Items.NETHERITE_AXE, Formatting.DARK_GRAY, 2500, "Ancient chopping", 9, "axe"); }
    public static ItemStack createAncientDebrisShovel() { return createBlockTool("Ancient Debris", "Shovel", "🪏", Items.NETHERITE_SHOVEL, Formatting.DARK_GRAY, 2500, "Blast proof digging", 7, "shovel"); }
    public static ItemStack createAncientDebrisHoe() { return createBlockTool("Ancient Debris", "Hoe", "🌾", Items.NETHERITE_HOE, Formatting.DARK_GRAY, 2500, "Timeless tilling", 6, "hoe"); }

    // BASALT TOOLS - Volcanic
    public static ItemStack createBasaltPickaxe() { return createBlockTool("Basalt", "Pickaxe", "⛏", Items.STONE_PICKAXE, Formatting.GRAY, 500, "Lava immune mining", 5, "pickaxe"); }
    public static ItemStack createBasaltAxe() { return createBlockTool("Basalt", "Axe", "🪓", Items.STONE_AXE, Formatting.GRAY, 500, "Volcanic chopping", 5, "axe"); }
    public static ItemStack createBasaltShovel() { return createBlockTool("Basalt", "Shovel", "🪏", Items.STONE_SHOVEL, Formatting.GRAY, 500, "Heat proof digging", 4, "shovel"); }
    public static ItemStack createBasaltHoe() { return createBlockTool("Basalt", "Hoe", "🌾", Items.STONE_HOE, Formatting.GRAY, 500, "Volcanic farming", 3, "hoe"); }

    // BLACKSTONE TOOLS - Dark fortress
    public static ItemStack createBlackstonePickaxe() { return createBlockTool("Blackstone", "Pickaxe", "⛏", Items.STONE_PICKAXE, Formatting.DARK_GRAY, 450, "Nether mining", 5, "pickaxe"); }
    public static ItemStack createBlackstoneAxe() { return createBlockTool("Blackstone", "Axe", "🪓", Items.STONE_AXE, Formatting.DARK_GRAY, 450, "Dark chopping", 5, "axe"); }
    public static ItemStack createBlackstoneShovel() { return createBlockTool("Blackstone", "Shovel", "🪏", Items.STONE_SHOVEL, Formatting.DARK_GRAY, 450, "Shadow digging", 4, "shovel"); }
    public static ItemStack createBlackstoneHoe() { return createBlockTool("Blackstone", "Hoe", "🌾", Items.STONE_HOE, Formatting.DARK_GRAY, 450, "Dark farming", 3, "hoe"); }

    // BONE BLOCK TOOLS - Undead essence
    public static ItemStack createBoneBlockPickaxe() { return createBlockTool("Bone Block", "Pickaxe", "⛏", Items.STONE_PICKAXE, Formatting.WHITE, 200, "Skeleton mining", 4, "pickaxe"); }
    public static ItemStack createBoneBlockAxe() { return createBlockTool("Bone Block", "Axe", "🪓", Items.STONE_AXE, Formatting.WHITE, 200, "Rattling chops", 4, "axe"); }
    public static ItemStack createBoneBlockShovel() { return createBlockTool("Bone Block", "Shovel", "🪏", Items.STONE_SHOVEL, Formatting.WHITE, 200, "Bone digging", 3, "shovel"); }
    public static ItemStack createBoneBlockHoe() { return createBlockTool("Bone Block", "Hoe", "🌾", Items.STONE_HOE, Formatting.WHITE, 200, "Skeletal farming", 2, "hoe"); }

    // BRICK TOOLS - Builder's tools
    public static ItemStack createBrickPickaxe() { return createBlockTool("Brick", "Pickaxe", "⛏", Items.STONE_PICKAXE, Formatting.RED, 380, "Construction mining", 5, "pickaxe"); }
    public static ItemStack createBrickAxe() { return createBlockTool("Brick", "Axe", "🪓", Items.STONE_AXE, Formatting.RED, 380, "Builder's chopping", 5, "axe"); }
    public static ItemStack createBrickShovel() { return createBlockTool("Brick", "Shovel", "🪏", Items.STONE_SHOVEL, Formatting.RED, 380, "Masonry digging", 4, "shovel"); }
    public static ItemStack createBrickHoe() { return createBlockTool("Brick", "Hoe", "🌾", Items.STONE_HOE, Formatting.RED, 380, "Urban farming", 3, "hoe"); }

    // CACTUS TOOLS - Prickly tools
    public static ItemStack createCactusPickaxe() { return createBlockTool("Cactus", "Pickaxe", "⛏", Items.WOODEN_PICKAXE, Formatting.DARK_GREEN, 180, "Thorny mining", 3, "pickaxe"); }
    public static ItemStack createCactusAxe() { return createBlockTool("Cactus", "Axe", "🪓", Items.WOODEN_AXE, Formatting.DARK_GREEN, 180, "Prickly chopping", 3, "axe"); }
    public static ItemStack createCactusShovel() { return createBlockTool("Cactus", "Shovel", "🪏", Items.WOODEN_SHOVEL, Formatting.DARK_GREEN, 180, "Spiky digging", 2, "shovel"); }
    public static ItemStack createCactusHoe() { return createBlockTool("Cactus", "Hoe", "🌾", Items.WOODEN_HOE, Formatting.DARK_GREEN, 180, "Desert farming", 2, "hoe"); }

    // CALCITE TOOLS - Crystal tools
    public static ItemStack createCalcitePickaxe() { return createBlockTool("Calcite", "Pickaxe", "⛏", Items.DIAMOND_PICKAXE, Formatting.WHITE, 340, "Purifying mining", 6, "pickaxe"); }
    public static ItemStack createCalciteAxe() { return createBlockTool("Calcite", "Axe", "🪓", Items.DIAMOND_AXE, Formatting.WHITE, 340, "Clean chopping", 6, "axe"); }
    public static ItemStack createCalciteShovel() { return createBlockTool("Calcite", "Shovel", "🪏", Items.DIAMOND_SHOVEL, Formatting.WHITE, 340, "Crystal digging", 5, "shovel"); }
    public static ItemStack createCalciteHoe() { return createBlockTool("Calcite", "Hoe", "🌾", Items.DIAMOND_HOE, Formatting.WHITE, 340, "Pristine farming", 4, "hoe"); }

    // DEEPSLATE TOOLS - Deep mining
    public static ItemStack createDeepslatePickaxe() { return createBlockTool("Deepslate", "Pickaxe", "⛏", Items.STONE_PICKAXE, Formatting.DARK_GRAY, 550, "Deep mining", 5, "pickaxe"); }
    public static ItemStack createDeepslateAxe() { return createBlockTool("Deepslate", "Axe", "🪓", Items.STONE_AXE, Formatting.DARK_GRAY, 550, "Dark chopping", 5, "axe"); }
    public static ItemStack createDeepslateShovel() { return createBlockTool("Deepslate", "Shovel", "🪏", Items.STONE_SHOVEL, Formatting.DARK_GRAY, 550, "Depth digging", 4, "shovel"); }
    public static ItemStack createDeepslateHoe() { return createBlockTool("Deepslate", "Hoe", "🌾", Items.STONE_HOE, Formatting.DARK_GRAY, 550, "Deep farming", 3, "hoe"); }

    // DRIPSTONE TOOLS - Sharp tools
    public static ItemStack createDripstonePickaxe() { return createBlockTool("Dripstone", "Pickaxe", "⛏", Items.STONE_PICKAXE, Formatting.GOLD, 360, "Sharp mining", 5, "pickaxe"); }
    public static ItemStack createDripstoneAxe() { return createBlockTool("Dripstone", "Axe", "🪓", Items.STONE_AXE, Formatting.GOLD, 360, "Pointy chopping", 5, "axe"); }
    public static ItemStack createDripstoneShovel() { return createBlockTool("Dripstone", "Shovel", "🪏", Items.STONE_SHOVEL, Formatting.GOLD, 360, "Stalactite digging", 4, "shovel"); }
    public static ItemStack createDripstoneHoe() { return createBlockTool("Dripstone", "Hoe", "🌾", Items.STONE_HOE, Formatting.GOLD, 360, "Drip farming", 3, "hoe"); }

    // HAY TOOLS - Farmer's tools
    public static ItemStack createHayPickaxe() { return createBlockTool("Hay", "Pickaxe", "⛏", Items.WOODEN_PICKAXE, Formatting.YELLOW, 100, "Soft mining", 2, "pickaxe"); }
    public static ItemStack createHayAxe() { return createBlockTool("Hay", "Axe", "🪓", Items.WOODEN_AXE, Formatting.YELLOW, 100, "Bale chopping", 2, "axe"); }
    public static ItemStack createHayShovel() { return createBlockTool("Hay", "Shovel", "🪏", Items.WOODEN_SHOVEL, Formatting.YELLOW, 100, "Soft digging", 1, "shovel"); }
    public static ItemStack createHayHoe() { return createBlockTool("Hay", "Hoe", "🌾", Items.WOODEN_HOE, Formatting.YELLOW, 100, "Ultimate farming", 3, "hoe"); }

    // HONEYCOMB TOOLS - Beekeeper's tools
    public static ItemStack createHoneycombPickaxe() { return createBlockTool("Honeycomb", "Pickaxe", "⛏", Items.GOLDEN_PICKAXE, Formatting.GOLD, 220, "Sticky mining", 4, "pickaxe"); }
    public static ItemStack createHoneycombAxe() { return createBlockTool("Honeycomb", "Axe", "🪓", Items.GOLDEN_AXE, Formatting.GOLD, 220, "Sweet chopping", 4, "axe"); }
    public static ItemStack createHoneycombShovel() { return createBlockTool("Honeycomb", "Shovel", "🪏", Items.GOLDEN_SHOVEL, Formatting.GOLD, 220, "Honey digging", 3, "shovel"); }
    public static ItemStack createHoneycombHoe() { return createBlockTool("Honeycomb", "Hoe", "🌾", Items.GOLDEN_HOE, Formatting.GOLD, 220, "Bee friendly farming", 3, "hoe"); }

    // LILY PAD TOOLS - Water tools
    public static ItemStack createLilyPadPickaxe() { return createBlockTool("Lily Pad", "Pickaxe", "⛏", Items.WOODEN_PICKAXE, Formatting.DARK_GREEN, 160, "Floating mining", 3, "pickaxe"); }
    public static ItemStack createLilyPadAxe() { return createBlockTool("Lily Pad", "Axe", "🪓", Items.WOODEN_AXE, Formatting.DARK_GREEN, 160, "Aquatic chopping", 3, "axe"); }
    public static ItemStack createLilyPadShovel() { return createBlockTool("Lily Pad", "Shovel", "🪏", Items.WOODEN_SHOVEL, Formatting.DARK_GREEN, 160, "Water digging", 2, "shovel"); }
    public static ItemStack createLilyPadHoe() { return createBlockTool("Lily Pad", "Hoe", "🌾", Items.WOODEN_HOE, Formatting.DARK_GREEN, 160, "Wetland farming", 2, "hoe"); }

    // MELON TOOLS - Juicy tools
    public static ItemStack createMelonPickaxe() { return createBlockTool("Melon", "Pickaxe", "⛏", Items.WOODEN_PICKAXE, Formatting.RED, 170, "Juicy mining", 3, "pickaxe"); }
    public static ItemStack createMelonAxe() { return createBlockTool("Melon", "Axe", "🪓", Items.WOODEN_AXE, Formatting.RED, 170, "Sweet chopping", 3, "axe"); }
    public static ItemStack createMelonShovel() { return createBlockTool("Melon", "Shovel", "🪏", Items.WOODEN_SHOVEL, Formatting.RED, 170, "Melon digging", 2, "shovel"); }
    public static ItemStack createMelonHoe() { return createBlockTool("Melon", "Hoe", "🌾", Items.WOODEN_HOE, Formatting.RED, 170, "Refreshing farming", 2, "hoe"); }

    // MOSS BLOCK TOOLS - Overgrown tools
    public static ItemStack createMossBlockPickaxe() { return createBlockTool("Moss Block", "Pickaxe", "⛏", Items.WOODEN_PICKAXE, Formatting.DARK_GREEN, 175, "Mossy mining", 3, "pickaxe"); }
    public static ItemStack createMossBlockAxe() { return createBlockTool("Moss Block", "Axe", "🪓", Items.WOODEN_AXE, Formatting.DARK_GREEN, 175, "Overgrown chopping", 3, "axe"); }
    public static ItemStack createMossBlockShovel() { return createBlockTool("Moss Block", "Shovel", "🪏", Items.WOODEN_SHOVEL, Formatting.DARK_GREEN, 175, "Nature digging", 2, "shovel"); }
    public static ItemStack createMossBlockHoe() { return createBlockTool("Moss Block", "Hoe", "🌾", Items.WOODEN_HOE, Formatting.DARK_GREEN, 175, "Fast regrowth farming", 3, "hoe"); }

    // MYCELIUM TOOLS - Fungal tools
    public static ItemStack createMyceliumPickaxe() { return createBlockTool("Mycelium", "Pickaxe", "⛏", Items.STONE_PICKAXE, Formatting.DARK_PURPLE, 290, "Spore mining", 4, "pickaxe"); }
    public static ItemStack createMyceliumAxe() { return createBlockTool("Mycelium", "Axe", "🪓", Items.STONE_AXE, Formatting.DARK_PURPLE, 290, "Fungal chopping", 4, "axe"); }
    public static ItemStack createMyceliumShovel() { return createBlockTool("Mycelium", "Shovel", "🪏", Items.STONE_SHOVEL, Formatting.DARK_PURPLE, 290, "Mushroom digging", 3, "shovel"); }
    public static ItemStack createMyceliumHoe() { return createBlockTool("Mycelium", "Hoe", "🌾", Items.STONE_HOE, Formatting.DARK_PURPLE, 290, "Mushroom farming", 3, "hoe"); }

    // NETHER BRICK TOOLS - Fortress tools
    public static ItemStack createNetherBrickPickaxe() { return createBlockTool("Nether Brick", "Pickaxe", "⛏", Items.STONE_PICKAXE, Formatting.DARK_RED, 480, "Nether mining", 5, "pickaxe"); }
    public static ItemStack createNetherBrickAxe() { return createBlockTool("Nether Brick", "Axe", "🪓", Items.STONE_AXE, Formatting.DARK_RED, 480, "Fiery chopping", 5, "axe"); }
    public static ItemStack createNetherBrickShovel() { return createBlockTool("Nether Brick", "Shovel", "🪏", Items.STONE_SHOVEL, Formatting.DARK_RED, 480, "Soul digging", 4, "shovel"); }
    public static ItemStack createNetherBrickHoe() { return createBlockTool("Nether Brick", "Hoe", "🌾", Items.STONE_HOE, Formatting.DARK_RED, 480, "Nether farming", 3, "hoe"); }

    // PUMPKIN TOOLS - Halloween tools
    public static ItemStack createPumpkinPickaxe() { return createBlockTool("Pumpkin", "Pickaxe", "⛏", Items.WOODEN_PICKAXE, Formatting.GOLD, 190, "Spooky mining", 3, "pickaxe"); }
    public static ItemStack createPumpkinAxe() { return createBlockTool("Pumpkin", "Axe", "🪓", Items.WOODEN_AXE, Formatting.GOLD, 190, "Jack-o chopping", 3, "axe"); }
    public static ItemStack createPumpkinShovel() { return createBlockTool("Pumpkin", "Shovel", "🪏", Items.WOODEN_SHOVEL, Formatting.GOLD, 190, "Haunted digging", 2, "shovel"); }
    public static ItemStack createPumpkinHoe() { return createBlockTool("Pumpkin", "Hoe", "🌾", Items.WOODEN_HOE, Formatting.GOLD, 190, "Halloween farming", 2, "hoe"); }

    // PURPUR TOOLS - End tools
    public static ItemStack createPurpurPickaxe() { return createBlockTool("Purpur", "Pickaxe", "⛏", Items.DIAMOND_PICKAXE, Formatting.LIGHT_PURPLE, 420, "End mining", 6, "pickaxe"); }
    public static ItemStack createPurpurAxe() { return createBlockTool("Purpur", "Axe", "🪓", Items.DIAMOND_AXE, Formatting.LIGHT_PURPLE, 420, "Levitating chopping", 6, "axe"); }
    public static ItemStack createPurpurShovel() { return createBlockTool("Purpur", "Shovel", "🪏", Items.DIAMOND_SHOVEL, Formatting.LIGHT_PURPLE, 420, "Ender digging", 5, "shovel"); }
    public static ItemStack createPurpurHoe() { return createBlockTool("Purpur", "Hoe", "🌾", Items.DIAMOND_HOE, Formatting.LIGHT_PURPLE, 420, "City farming", 4, "hoe"); }

    // SAND TOOLS - Desert tools
    public static ItemStack createSandPickaxe() { return createBlockTool("Sand", "Pickaxe", "⛏", Items.WOODEN_PICKAXE, Formatting.YELLOW, 140, "Shifting mining", 2, "pickaxe"); }
    public static ItemStack createSandAxe() { return createBlockTool("Sand", "Axe", "🪓", Items.WOODEN_AXE, Formatting.YELLOW, 140, "Desert chopping", 2, "axe"); }
    public static ItemStack createSandShovel() { return createBlockTool("Sand", "Shovel", "🪏", Items.WOODEN_SHOVEL, Formatting.YELLOW, 140, "Sand expert", 3, "shovel"); }
    public static ItemStack createSandHoe() { return createBlockTool("Sand", "Hoe", "🌾", Items.WOODEN_HOE, Formatting.YELLOW, 140, "Desert farming", 2, "hoe"); }

    // SCULK TOOLS - Dark tools
    public static ItemStack createSculkPickaxe() { return createBlockTool("Sculk", "Pickaxe", "⛏", Items.DIAMOND_PICKAXE, Formatting.DARK_AQUA, 580, "Silent mining", 7, "pickaxe"); }
    public static ItemStack createSculkAxe() { return createBlockTool("Sculk", "Axe", "🪓", Items.DIAMOND_AXE, Formatting.DARK_AQUA, 580, "Quiet chopping", 7, "axe"); }
    public static ItemStack createSculkShovel() { return createBlockTool("Sculk", "Shovel", "🪏", Items.DIAMOND_SHOVEL, Formatting.DARK_AQUA, 580, "Stealth digging", 6, "shovel"); }
    public static ItemStack createSculkHoe() { return createBlockTool("Sculk", "Hoe", "🌾", Items.DIAMOND_HOE, Formatting.DARK_AQUA, 580, "Dark farming", 5, "hoe"); }

    // SHROOMLIGHT TOOLS - Fungal light tools
    public static ItemStack createShroomlightPickaxe() { return createBlockTool("Shroomlight", "Pickaxe", "⛏", Items.GOLDEN_PICKAXE, Formatting.LIGHT_PURPLE, 310, "Glowing mining", 4, "pickaxe"); }
    public static ItemStack createShroomlightAxe() { return createBlockTool("Shroomlight", "Axe", "🪓", Items.GOLDEN_AXE, Formatting.LIGHT_PURPLE, 310, "Bright chopping", 4, "axe"); }
    public static ItemStack createShroomlightShovel() { return createBlockTool("Shroomlight", "Shovel", "🪏", Items.GOLDEN_SHOVEL, Formatting.LIGHT_PURPLE, 310, "Luminous digging", 3, "shovel"); }
    public static ItemStack createShroomlightHoe() { return createBlockTool("Shroomlight", "Hoe", "🌾", Items.GOLDEN_HOE, Formatting.LIGHT_PURPLE, 310, "Lit farming", 3, "hoe"); }

    // SLIME TOOLS - Bouncy tools
    public static ItemStack createSlimePickaxe() { return createBlockTool("Slime", "Pickaxe", "⛏", Items.WOODEN_PICKAXE, Formatting.GREEN, 165, "Bouncy mining", 3, "pickaxe"); }
    public static ItemStack createSlimeAxe() { return createBlockTool("Slime", "Axe", "🪓", Items.WOODEN_AXE, Formatting.GREEN, 165, "Sticky chopping", 3, "axe"); }
    public static ItemStack createSlimeShovel() { return createBlockTool("Slime", "Shovel", "🪏", Items.WOODEN_SHOVEL, Formatting.GREEN, 165, "Gooey digging", 2, "shovel"); }
    public static ItemStack createSlimeHoe() { return createBlockTool("Slime", "Hoe", "🌾", Items.WOODEN_HOE, Formatting.GREEN, 165, "Bouncy farming", 2, "hoe"); }

    // SMOOTH STONE TOOLS - Refined tools
    public static ItemStack createSmoothStonePickaxe() { return createBlockTool("Smooth Stone", "Pickaxe", "⛏", Items.STONE_PICKAXE, Formatting.GRAY, 440, "Smooth mining", 5, "pickaxe"); }
    public static ItemStack createSmoothStoneAxe() { return createBlockTool("Smooth Stone", "Axe", "🪓", Items.STONE_AXE, Formatting.GRAY, 440, "Polished chopping", 5, "axe"); }
    public static ItemStack createSmoothStoneShovel() { return createBlockTool("Smooth Stone", "Shovel", "🪏", Items.STONE_SHOVEL, Formatting.GRAY, 440, "Refined digging", 4, "shovel"); }
    public static ItemStack createSmoothStoneHoe() { return createBlockTool("Smooth Stone", "Hoe", "🌾", Items.STONE_HOE, Formatting.GRAY, 440, "Efficient farming", 4, "hoe"); }

    // SNOW TOOLS - Frozen tools
    public static ItemStack createSnowPickaxe() { return createBlockTool("Snow", "Pickaxe", "⛏", Items.WOODEN_PICKAXE, Formatting.WHITE, 130, "Icy mining", 2, "pickaxe"); }
    public static ItemStack createSnowAxe() { return createBlockTool("Snow", "Axe", "🪓", Items.WOODEN_AXE, Formatting.WHITE, 130, "Frozen chopping", 2, "axe"); }
    public static ItemStack createSnowShovel() { return createBlockTool("Snow", "Shovel", "🪏", Items.WOODEN_SHOVEL, Formatting.WHITE, 130, "Snow expert", 3, "shovel"); }
    public static ItemStack createSnowHoe() { return createBlockTool("Snow", "Hoe", "🌾", Items.WOODEN_HOE, Formatting.WHITE, 130, "Winter farming", 2, "hoe"); }

    // SOUL SOIL TOOLS - Soul tools
    public static ItemStack createSoulSoilPickaxe() { return createBlockTool("Soul Soil", "Pickaxe", "⛏", Items.STONE_PICKAXE, Formatting.DARK_GRAY, 410, "Soul mining", 5, "pickaxe"); }
    public static ItemStack createSoulSoilAxe() { return createBlockTool("Soul Soil", "Axe", "🪓", Items.STONE_AXE, Formatting.DARK_GRAY, 410, "Spirit chopping", 5, "axe"); }
    public static ItemStack createSoulSoilShovel() { return createBlockTool("Soul Soil", "Shovel", "🪏", Items.STONE_SHOVEL, Formatting.DARK_GRAY, 410, "Soul digging", 4, "shovel"); }
    public static ItemStack createSoulSoilHoe() { return createBlockTool("Soul Soil", "Hoe", "🌾", Items.STONE_HOE, Formatting.DARK_GRAY, 410, "Soul farming", 3, "hoe"); }

    // SPONGE TOOLS - Absorbent tools
    public static ItemStack createSpongePickaxe() { return createBlockTool("Sponge", "Pickaxe", "⛏", Items.WOODEN_PICKAXE, Formatting.YELLOW, 185, "Absorbing mining", 3, "pickaxe"); }
    public static ItemStack createSpongeAxe() { return createBlockTool("Sponge", "Axe", "🪓", Items.WOODEN_AXE, Formatting.YELLOW, 185, "Soaking chopping", 3, "axe"); }
    public static ItemStack createSpongeShovel() { return createBlockTool("Sponge", "Shovel", "🪏", Items.WOODEN_SHOVEL, Formatting.YELLOW, 185, "Dry digging", 2, "shovel"); }
    public static ItemStack createSpongeHoe() { return createBlockTool("Sponge", "Hoe", "🌾", Items.WOODEN_HOE, Formatting.YELLOW, 185, "Moist farming", 2, "hoe"); }

    // TARGET TOOLS - Precision tools
    public static ItemStack createTargetPickaxe() { return createBlockTool("Target", "Pickaxe", "⛏", Items.WOODEN_PICKAXE, Formatting.RED, 195, "Accurate mining", 3, "pickaxe"); }
    public static ItemStack createTargetAxe() { return createBlockTool("Target", "Axe", "🪓", Items.WOODEN_AXE, Formatting.RED, 195, "Precise chopping", 3, "axe"); }
    public static ItemStack createTargetShovel() { return createBlockTool("Target", "Shovel", "🪏", Items.WOODEN_SHOVEL, Formatting.RED, 195, "Target digging", 2, "shovel"); }
    public static ItemStack createTargetHoe() { return createBlockTool("Target", "Hoe", "🌾", Items.WOODEN_HOE, Formatting.RED, 195, "Bullseye farming", 2, "hoe"); }

    // TNT TOOLS - Explosive tools
    public static ItemStack createTntPickaxe() { return createBlockTool("TNT", "Pickaxe", "⛏", Items.WOODEN_PICKAXE, Formatting.RED, 120, "Blast mining", 4, "pickaxe"); }
    public static ItemStack createTntAxe() { return createBlockTool("TNT", "Axe", "🪓", Items.WOODEN_AXE, Formatting.RED, 120, "Explosive chopping", 4, "axe"); }
    public static ItemStack createTntShovel() { return createBlockTool("TNT", "Shovel", "🪏", Items.WOODEN_SHOVEL, Formatting.RED, 120, "Boom digging", 3, "shovel"); }
    public static ItemStack createTntHoe() { return createBlockTool("TNT", "Hoe", "🌾", Items.WOODEN_HOE, Formatting.RED, 120, "Boom farming", 3, "hoe"); }

    // WARPED TOOLS - Forest tools
    public static ItemStack createWarpedPickaxe() { return createBlockTool("Warped", "Pickaxe", "⛏", Items.STONE_PICKAXE, Formatting.DARK_AQUA, 350, "Warped mining", 5, "pickaxe"); }
    public static ItemStack createWarpedAxe() { return createBlockTool("Warped", "Axe", "🪓", Items.STONE_AXE, Formatting.DARK_AQUA, 350, "Forest chopping", 5, "axe"); }
    public static ItemStack createWarpedShovel() { return createBlockTool("Warped", "Shovel", "🪏", Items.STONE_SHOVEL, Formatting.DARK_AQUA, 350, "Mushroom digging", 4, "shovel"); }
    public static ItemStack createWarpedHoe() { return createBlockTool("Warped", "Hoe", "🌾", Items.STONE_HOE, Formatting.DARK_AQUA, 350, "Warped farming", 3, "hoe"); }

    // WET SPONGE TOOLS - Soggy tools
    public static ItemStack createWetSpongePickaxe() { return createBlockTool("Wet Sponge", "Pickaxe", "⛏", Items.WOODEN_PICKAXE, Formatting.DARK_GREEN, 180, "Wet mining", 3, "pickaxe"); }
    public static ItemStack createWetSpongeAxe() { return createBlockTool("Wet Sponge", "Axe", "🪓", Items.WOODEN_AXE, Formatting.DARK_GREEN, 180, "Soggy chopping", 3, "axe"); }
    public static ItemStack createWetSpongeShovel() { return createBlockTool("Wet Sponge", "Shovel", "🪏", Items.WOODEN_SHOVEL, Formatting.DARK_GREEN, 180, "Damp digging", 2, "shovel"); }
    public static ItemStack createWetSpongeHoe() { return createBlockTool("Wet Sponge", "Hoe", "🌾", Items.WOODEN_HOE, Formatting.DARK_GREEN, 180, "Soaked farming", 2, "hoe"); }

    // ============================================================
    // NEW BLOCK TOOLS
    // ============================================================
    
    // CRIMSON STEM TOOLS - Fungal wood tools
    public static ItemStack createCrimsonStemPickaxe() { return createBlockTool("Crimson Stem", "Pickaxe", "⛏", Items.WOODEN_PICKAXE, Formatting.DARK_RED, 250, "Fungal mining", 3, "pickaxe"); }
    public static ItemStack createCrimsonStemAxe() { return createBlockTool("Crimson Stem", "Axe", "🪓", Items.WOODEN_AXE, Formatting.DARK_RED, 250, "Stem chopping", 3, "axe"); }
    public static ItemStack createCrimsonStemShovel() { return createBlockTool("Crimson Stem", "Shovel", "🪏", Items.WOODEN_SHOVEL, Formatting.DARK_RED, 250, "Mycelium digging", 2, "shovel"); }
    public static ItemStack createCrimsonStemHoe() { return createBlockTool("Crimson Stem", "Hoe", "🌾", Items.WOODEN_HOE, Formatting.DARK_RED, 250, "Nether farming", 2, "hoe"); }
    
    // CRYING OBSIDIAN TOOLS - Tears of the void
    public static ItemStack createCryingObsidianPickaxe() { return createBlockTool("Crying Obsidian", "Pickaxe", "⛏", Items.NETHERITE_PICKAXE, Formatting.DARK_PURPLE, 1800, "Void mining", 7, "pickaxe"); }
    public static ItemStack createCryingObsidianAxe() { return createBlockTool("Crying Obsidian", "Axe", "🪓", Items.NETHERITE_AXE, Formatting.DARK_PURPLE, 1800, "Tearful chopping", 7, "axe"); }
    public static ItemStack createCryingObsidianShovel() { return createBlockTool("Crying Obsidian", "Shovel", "🪏", Items.NETHERITE_SHOVEL, Formatting.DARK_PURPLE, 1800, "Crying dig", 6, "shovel"); }
    public static ItemStack createCryingObsidianHoe() { return createBlockTool("Crying Obsidian", "Hoe", "🌾", Items.NETHERITE_HOE, Formatting.DARK_PURPLE, 1800, "Portal farming", 5, "hoe"); }
    
    // GILDED BLACKSTONE TOOLS - Golden nether stone
    public static ItemStack createGildedBlackstonePickaxe() { return createBlockTool("Gilded Blackstone", "Pickaxe", "⛏", Items.DIAMOND_PICKAXE, Formatting.GOLD, 650, "Golden mining", 6, "pickaxe"); }
    public static ItemStack createGildedBlackstoneAxe() { return createBlockTool("Gilded Blackstone", "Axe", "🪓", Items.DIAMOND_AXE, Formatting.GOLD, 650, "Fortune chopping", 6, "axe"); }
    public static ItemStack createGildedBlackstoneShovel() { return createBlockTool("Gilded Blackstone", "Shovel", "🪏", Items.DIAMOND_SHOVEL, Formatting.GOLD, 650, "Gold dig", 5, "shovel"); }
    public static ItemStack createGildedBlackstoneHoe() { return createBlockTool("Gilded Blackstone", "Hoe", "🌾", Items.DIAMOND_HOE, Formatting.GOLD, 650, "Rich farming", 4, "hoe"); }
    
    // GRANITE TOOLS - Igneous rock tools
    public static ItemStack createGranitePickaxe() { return createBlockTool("Granite", "Pickaxe", "⛏", Items.STONE_PICKAXE, Formatting.RED, 420, "Granite mining", 5, "pickaxe"); }
    public static ItemStack createGraniteAxe() { return createBlockTool("Granite", "Axe", "🪓", Items.STONE_AXE, Formatting.RED, 420, "Hard chopping", 5, "axe"); }
    public static ItemStack createGraniteShovel() { return createBlockTool("Granite", "Shovel", "🪏", Items.STONE_SHOVEL, Formatting.RED, 420, "Rock dig", 4, "shovel"); }
    public static ItemStack createGraniteHoe() { return createBlockTool("Granite", "Hoe", "🌾", Items.STONE_HOE, Formatting.RED, 420, "Stone farming", 3, "hoe"); }
    
    // DIORITE TOOLS - White igneous tools
    public static ItemStack createDioritePickaxe() { return createBlockTool("Diorite", "Pickaxe", "⛏", Items.STONE_PICKAXE, Formatting.WHITE, 400, "Crystal mining", 5, "pickaxe"); }
    public static ItemStack createDioriteAxe() { return createBlockTool("Diorite", "Axe", "🪓", Items.STONE_AXE, Formatting.WHITE, 400, "White chopping", 5, "axe"); }
    public static ItemStack createDioriteShovel() { return createBlockTool("Diorite", "Shovel", "🪏", Items.STONE_SHOVEL, Formatting.WHITE, 400, "Marble dig", 4, "shovel"); }
    public static ItemStack createDioriteHoe() { return createBlockTool("Diorite", "Hoe", "🌾", Items.STONE_HOE, Formatting.WHITE, 400, "Polished farming", 3, "hoe"); }
    
    // ANDESITE TOOLS - Gray volcanic tools
    public static ItemStack createAndesitePickaxe() { return createBlockTool("Andesite", "Pickaxe", "⛏", Items.STONE_PICKAXE, Formatting.GRAY, 410, "Volcanic mining", 5, "pickaxe"); }
    public static ItemStack createAndesiteAxe() { return createBlockTool("Andesite", "Axe", "🪓", Items.STONE_AXE, Formatting.GRAY, 410, "Dark chopping", 5, "axe"); }
    public static ItemStack createAndesiteShovel() { return createBlockTool("Andesite", "Shovel", "🪏", Items.STONE_SHOVEL, Formatting.GRAY, 410, "Volcano dig", 4, "shovel"); }
    public static ItemStack createAndesiteHoe() { return createBlockTool("Andesite", "Hoe", "🌾", Items.STONE_HOE, Formatting.GRAY, 410, "Mountain farming", 3, "hoe"); }
    
    // POLISHED GRANITE TOOLS - Smooth granite
    public static ItemStack createPolishedGranitePickaxe() { return createBlockTool("Polished Granite", "Pickaxe", "⛏", Items.STONE_PICKAXE, Formatting.RED, 430, "Smooth mining", 5, "pickaxe"); }
    public static ItemStack createPolishedGraniteAxe() { return createBlockTool("Polished Granite", "Axe", "🪓", Items.STONE_AXE, Formatting.RED, 430, "Polish chopping", 5, "axe"); }
    public static ItemStack createPolishedGraniteShovel() { return createBlockTool("Polished Granite", "Shovel", "🪏", Items.STONE_SHOVEL, Formatting.RED, 430, "Slick dig", 4, "shovel"); }
    public static ItemStack createPolishedGraniteHoe() { return createBlockTool("Polished Granite", "Hoe", "🌾", Items.STONE_HOE, Formatting.RED, 430, "Tile farming", 3, "hoe"); }
    
    // POLISHED DIORITE TOOLS - Smooth white stone
    public static ItemStack createPolishedDioritePickaxe() { return createBlockTool("Polished Diorite", "Pickaxe", "⛏", Items.STONE_PICKAXE, Formatting.WHITE, 410, "White stone mining", 5, "pickaxe"); }
    public static ItemStack createPolishedDioriteAxe() { return createBlockTool("Polished Diorite", "Axe", "🪓", Items.STONE_AXE, Formatting.WHITE, 410, "Clean chopping", 5, "axe"); }
    public static ItemStack createPolishedDioriteShovel() { return createBlockTool("Polished Diorite", "Shovel", "🪏", Items.STONE_SHOVEL, Formatting.WHITE, 410, "Smooth dig", 4, "shovel"); }
    public static ItemStack createPolishedDioriteHoe() { return createBlockTool("Polished Diorite", "Hoe", "🌾", Items.STONE_HOE, Formatting.WHITE, 410, "Clean farming", 3, "hoe"); }
    
    // POLISHED ANDESITE TOOLS - Smooth volcanic
    public static ItemStack createPolishedAndesitePickaxe() { return createBlockTool("Polished Andesite", "Pickaxe", "⛏", Items.STONE_PICKAXE, Formatting.GRAY, 420, "Sleek mining", 5, "pickaxe"); }
    public static ItemStack createPolishedAndesiteAxe() { return createBlockTool("Polished Andesite", "Axe", "🪓", Items.STONE_AXE, Formatting.GRAY, 420, "Slick chopping", 5, "axe"); }
    public static ItemStack createPolishedAndesiteShovel() { return createBlockTool("Polished Andesite", "Shovel", "🪏", Items.STONE_SHOVEL, Formatting.GRAY, 420, "Sleek dig", 4, "shovel"); }
    public static ItemStack createPolishedAndesiteHoe() { return createBlockTool("Polished Andesite", "Hoe", "🌾", Items.STONE_HOE, Formatting.GRAY, 420, "Modern farming", 3, "hoe"); }
    
    // PACKED ICE TOOLS - Frozen tools
    public static ItemStack createPackedIcePickaxe() { return createBlockTool("Packed Ice", "Pickaxe", "⛏", Items.DIAMOND_PICKAXE, Formatting.AQUA, 520, "Ice mining", 5, "pickaxe"); }
    public static ItemStack createPackedIceAxe() { return createBlockTool("Packed Ice", "Axe", "🪓", Items.DIAMOND_AXE, Formatting.AQUA, 520, "Cold chopping", 5, "axe"); }
    public static ItemStack createPackedIceShovel() { return createBlockTool("Packed Ice", "Shovel", "🪏", Items.DIAMOND_SHOVEL, Formatting.AQUA, 520, "Frost dig", 4, "shovel"); }
    public static ItemStack createPackedIceHoe() { return createBlockTool("Packed Ice", "Hoe", "🌾", Items.DIAMOND_HOE, Formatting.AQUA, 520, "Frozen farming", 3, "hoe"); }
    
    // BLUE ICE TOOLS - Dense frozen tools
    public static ItemStack createBlueIcePickaxe() { return createBlockTool("Blue Ice", "Pickaxe", "⛏", Items.DIAMOND_PICKAXE, Formatting.BLUE, 680, "Deep freeze mining", 6, "pickaxe"); }
    public static ItemStack createBlueIceAxe() { return createBlockTool("Blue Ice", "Axe", "🪓", Items.DIAMOND_AXE, Formatting.BLUE, 680, "Glacier chopping", 6, "axe"); }
    public static ItemStack createBlueIceShovel() { return createBlockTool("Blue Ice", "Shovel", "🪏", Items.DIAMOND_SHOVEL, Formatting.BLUE, 680, "Permafrost dig", 5, "shovel"); }
    public static ItemStack createBlueIceHoe() { return createBlockTool("Blue Ice", "Hoe", "🌾", Items.DIAMOND_HOE, Formatting.BLUE, 680, "Snow farming", 4, "hoe"); }
    
    // NETHER GOLD ORE TOOLS - Golden nether ore
    public static ItemStack createNetherGoldOrePickaxe() { return createBlockTool("Nether Gold Ore", "Pickaxe", "⛏", Items.GOLDEN_PICKAXE, Formatting.GOLD, 280, "Golden nether mining", 4, "pickaxe"); }
    public static ItemStack createNetherGoldOreAxe() { return createBlockTool("Nether Gold Ore", "Axe", "🪓", Items.GOLDEN_AXE, Formatting.GOLD, 280, "Piglin riches", 4, "axe"); }
    public static ItemStack createNetherGoldOreShovel() { return createBlockTool("Nether Gold Ore", "Shovel", "🪏", Items.GOLDEN_SHOVEL, Formatting.GOLD, 280, "Gold dig", 3, "shovel"); }
    public static ItemStack createNetherGoldOreHoe() { return createBlockTool("Nether Gold Ore", "Hoe", "🌾", Items.GOLDEN_HOE, Formatting.GOLD, 280, "Nether farming", 2, "hoe"); }
    
    // DARK OAK LOG TOOLS - Dark forest wood
    public static ItemStack createDarkOakLogPickaxe() { return createBlockTool("Dark Oak Log", "Pickaxe", "⛏", Items.WOODEN_PICKAXE, Formatting.DARK_RED, 320, "Dark forest mining", 3, "pickaxe"); }
    public static ItemStack createDarkOakLogAxe() { return createBlockTool("Dark Oak Log", "Axe", "🪓", Items.WOODEN_AXE, Formatting.DARK_RED, 320, "Shadow chopping", 3, "axe"); }
    public static ItemStack createDarkOakLogShovel() { return createBlockTool("Dark Oak Log", "Shovel", "🪏", Items.WOODEN_SHOVEL, Formatting.DARK_RED, 320, "Dark dig", 2, "shovel"); }
    public static ItemStack createDarkOakLogHoe() { return createBlockTool("Dark Oak Log", "Hoe", "🌾", Items.WOODEN_HOE, Formatting.DARK_RED, 320, "Forest farming", 2, "hoe"); }
    
    // JUNGLE LOG TOOLS - Tropical wood
    public static ItemStack createJungleLogPickaxe() { return createBlockTool("Jungle Log", "Pickaxe", "⛏", Items.WOODEN_PICKAXE, Formatting.GREEN, 300, "Jungle mining", 3, "pickaxe"); }
    public static ItemStack createJungleLogAxe() { return createBlockTool("Jungle Log", "Axe", "🪓", Items.WOODEN_AXE, Formatting.GREEN, 300, "Tropical chopping", 3, "axe"); }
    public static ItemStack createJungleLogShovel() { return createBlockTool("Jungle Log", "Shovel", "🪏", Items.WOODEN_SHOVEL, Formatting.GREEN, 300, "Rainforest dig", 2, "shovel"); }
    public static ItemStack createJungleLogHoe() { return createBlockTool("Jungle Log", "Hoe", "🌾", Items.WOODEN_HOE, Formatting.GREEN, 300, "Tropical farming", 2, "hoe"); }
    
    // ACACIA LOG TOOLS - Savanna wood
    public static ItemStack createAcaciaLogPickaxe() { return createBlockTool("Acacia Log", "Pickaxe", "⛏", Items.WOODEN_PICKAXE, Formatting.GOLD, 290, "Savanna mining", 3, "pickaxe"); }
    public static ItemStack createAcaciaLogAxe() { return createBlockTool("Acacia Log", "Axe", "🪓", Items.WOODEN_AXE, Formatting.GOLD, 290, "Sunset chopping", 3, "axe"); }
    public static ItemStack createAcaciaLogShovel() { return createBlockTool("Acacia Log", "Shovel", "🪏", Items.WOODEN_SHOVEL, Formatting.GOLD, 290, "Savanna dig", 2, "shovel"); }
    public static ItemStack createAcaciaLogHoe() { return createBlockTool("Acacia Log", "Hoe", "🌾", Items.WOODEN_HOE, Formatting.GOLD, 290, "Plains farming", 2, "hoe"); }
    
    // MANGROVE LOG TOOLS - Swamp wood
    public static ItemStack createMangroveLogPickaxe() { return createBlockTool("Mangrove Log", "Pickaxe", "⛏", Items.WOODEN_PICKAXE, Formatting.DARK_RED, 340, "Swamp mining", 3, "pickaxe"); }
    public static ItemStack createMangroveLogAxe() { return createBlockTool("Mangrove Log", "Axe", "🪓", Items.WOODEN_AXE, Formatting.DARK_RED, 340, "Root chopping", 3, "axe"); }
    public static ItemStack createMangroveLogShovel() { return createBlockTool("Mangrove Log", "Shovel", "🪏", Items.WOODEN_SHOVEL, Formatting.DARK_RED, 340, "Marsh dig", 2, "shovel"); }
    public static ItemStack createMangroveLogHoe() { return createBlockTool("Mangrove Log", "Hoe", "🌾", Items.WOODEN_HOE, Formatting.DARK_RED, 340, "Swamp farming", 2, "hoe"); }
    
    // CHERRY LOG TOOLS - Pink blossom wood
    public static ItemStack createCherryLogPickaxe() { return createBlockTool("Cherry Log", "Pickaxe", "⛏", Items.WOODEN_PICKAXE, Formatting.LIGHT_PURPLE, 310, "Blossom mining", 3, "pickaxe"); }
    public static ItemStack createCherryLogAxe() { return createBlockTool("Cherry Log", "Axe", "🪓", Items.WOODEN_AXE, Formatting.LIGHT_PURPLE, 310, "Flower chopping", 3, "axe"); }
    public static ItemStack createCherryLogShovel() { return createBlockTool("Cherry Log", "Shovel", "🪏", Items.WOODEN_SHOVEL, Formatting.LIGHT_PURPLE, 310, "Petal dig", 2, "shovel"); }
    public static ItemStack createCherryLogHoe() { return createBlockTool("Cherry Log", "Hoe", "🌾", Items.WOODEN_HOE, Formatting.LIGHT_PURPLE, 310, "Garden farming", 2, "hoe"); }
    
    // BAMBOO BLOCK TOOLS - Bamboo scaffolding
    public static ItemStack createBambooBlockPickaxe() { return createBlockTool("Bamboo Block", "Pickaxe", "⛏", Items.WOODEN_PICKAXE, Formatting.GREEN, 180, "Light mining", 2, "pickaxe"); }
    public static ItemStack createBambooBlockAxe() { return createBlockTool("Bamboo Block", "Axe", "🪓", Items.WOODEN_AXE, Formatting.GREEN, 180, "Quick chopping", 2, "axe"); }
    public static ItemStack createBambooBlockShovel() { return createBlockTool("Bamboo Block", "Shovel", "🪏", Items.WOODEN_SHOVEL, Formatting.GREEN, 180, "Fast dig", 2, "shovel"); }
    public static ItemStack createBambooBlockHoe() { return createBlockTool("Bamboo Block", "Hoe", "🌾", Items.WOODEN_HOE, Formatting.GREEN, 180, "Quick farming", 2, "hoe"); }
    
    // TUFF TOOLS - Volcanic sediment
    public static ItemStack createTuffPickaxe() { return createBlockTool("Tuff", "Pickaxe", "⛏", Items.STONE_PICKAXE, Formatting.DARK_GRAY, 380, "Volcanic mining", 5, "pickaxe"); }
    public static ItemStack createTuffAxe() { return createBlockTool("Tuff", "Axe", "🪓", Items.STONE_AXE, Formatting.DARK_GRAY, 380, "Sediment chopping", 5, "axe"); }
    public static ItemStack createTuffShovel() { return createBlockTool("Tuff", "Shovel", "🪏", Items.STONE_SHOVEL, Formatting.DARK_GRAY, 380, "Ash dig", 4, "shovel"); }
    public static ItemStack createTuffHoe() { return createBlockTool("Tuff", "Hoe", "🌾", Items.STONE_HOE, Formatting.DARK_GRAY, 380, "Cave farming", 3, "hoe"); }
    
    // POLISHED TUFF TOOLS - Smooth tuff
    public static ItemStack createPolishedTuffPickaxe() { return createBlockTool("Polished Tuff", "Pickaxe", "⛏", Items.STONE_PICKAXE, Formatting.GRAY, 400, "Smooth mining", 5, "pickaxe"); }
    public static ItemStack createPolishedTuffAxe() { return createBlockTool("Polished Tuff", "Axe", "🪓", Items.STONE_AXE, Formatting.GRAY, 400, "Clean chopping", 5, "axe"); }
    public static ItemStack createPolishedTuffShovel() { return createBlockTool("Polished Tuff", "Shovel", "🪏", Items.STONE_SHOVEL, Formatting.GRAY, 400, "Sleek dig", 4, "shovel"); }
    public static ItemStack createPolishedTuffHoe() { return createBlockTool("Polished Tuff", "Hoe", "🌾", Items.STONE_HOE, Formatting.GRAY, 400, "Masonry farming", 3, "hoe"); }
    
    // NETHER WART BLOCK TOOLS - Crimson fungus
    public static ItemStack createNetherWartBlockPickaxe() { return createBlockTool("Nether Wart Block", "Pickaxe", "⛏", Items.WOODEN_PICKAXE, Formatting.DARK_RED, 260, "Wart mining", 3, "pickaxe"); }
    public static ItemStack createNetherWartBlockAxe() { return createBlockTool("Nether Wart Block", "Axe", "🪓", Items.WOODEN_AXE, Formatting.DARK_RED, 260, "Fungal chopping", 3, "axe"); }
    public static ItemStack createNetherWartBlockShovel() { return createBlockTool("Nether Wart Block", "Shovel", "🪏", Items.WOODEN_SHOVEL, Formatting.DARK_RED, 260, "Mycelium dig", 2, "shovel"); }
    public static ItemStack createNetherWartBlockHoe() { return createBlockTool("Nether Wart Block", "Hoe", "🌾", Items.WOODEN_HOE, Formatting.DARK_RED, 260, "Nether farming", 2, "hoe"); }
    
    // WARPED WART BLOCK TOOLS - Warped fungus
    public static ItemStack createWarpedWartBlockPickaxe() { return createBlockTool("Warped Wart Block", "Pickaxe", "⛏", Items.WOODEN_PICKAXE, Formatting.DARK_GREEN, 260, "Warped mining", 3, "pickaxe"); }
    public static ItemStack createWarpedWartBlockAxe() { return createBlockTool("Warped Wart Block", "Axe", "🪓", Items.WOODEN_AXE, Formatting.DARK_GREEN, 260, "Twisted chopping", 3, "axe"); }
    public static ItemStack createWarpedWartBlockShovel() { return createBlockTool("Warped Wart Block", "Shovel", "🪏", Items.WOODEN_SHOVEL, Formatting.DARK_GREEN, 260, "Ender dig", 2, "shovel"); }
    public static ItemStack createWarpedWartBlockHoe() { return createBlockTool("Warped Wart Block", "Hoe", "🌾", Items.WOODEN_HOE, Formatting.DARK_GREEN, 260, "Strange farming", 2, "hoe"); }
    
    // CHISELED DEEPSLATE TOOLS - Carved deepslate
    public static ItemStack createChiseledDeepslatePickaxe() { return createBlockTool("Chiseled Deepslate", "Pickaxe", "⛏", Items.DIAMOND_PICKAXE, Formatting.DARK_BLUE, 420, "Deep mining", 6, "pickaxe"); }
    public static ItemStack createChiseledDeepslateAxe() { return createBlockTool("Chiseled Deepslate", "Axe", "🪓", Items.DIAMOND_AXE, Formatting.DARK_BLUE, 420, "Rune chopping", 6, "axe"); }
    public static ItemStack createChiseledDeepslateShovel() { return createBlockTool("Chiseled Deepslate", "Shovel", "🪏", Items.DIAMOND_SHOVEL, Formatting.DARK_BLUE, 420, "Ancient dig", 5, "shovel"); }
    public static ItemStack createChiseledDeepslateHoe() { return createBlockTool("Chiseled Deepslate", "Hoe", "🌾", Items.DIAMOND_HOE, Formatting.DARK_BLUE, 420, "Deep farming", 4, "hoe"); }
    
    // REINFORCED DEEPSLATE TOOLS - Ultra durable
    public static ItemStack createReinforcedDeepslatePickaxe() { return createBlockTool("Reinforced Deepslate", "Pickaxe", "⛏", Items.NETHERITE_PICKAXE, Formatting.BLUE, 1200, "Bedrock mining", 8, "pickaxe"); }
    public static ItemStack createReinforcedDeepslateAxe() { return createBlockTool("Reinforced Deepslate", "Axe", "🪓", Items.NETHERITE_AXE, Formatting.BLUE, 1200, "Fortress chopping", 8, "axe"); }
    public static ItemStack createReinforcedDeepslateShovel() { return createBlockTool("Reinforced Deepslate", "Shovel", "🪏", Items.NETHERITE_SHOVEL, Formatting.BLUE, 1200, "Indestructible dig", 7, "shovel"); }
    public static ItemStack createReinforcedDeepslateHoe() { return createBlockTool("Reinforced Deepslate", "Hoe", "🌾", Items.NETHERITE_HOE, Formatting.BLUE, 1200, "Max farming", 6, "hoe"); }
    
    // CHISELED NETHER BRICK TOOLS - Carved nether brick
    public static ItemStack createChiseledNetherBrickPickaxe() { return createBlockTool("Chiseled Nether Brick", "Pickaxe", "⛏", Items.STONE_PICKAXE, Formatting.DARK_RED, 380, "Ancient mining", 5, "pickaxe"); }
    public static ItemStack createChiseledNetherBrickAxe() { return createBlockTool("Chiseled Nether Brick", "Axe", "🪓", Items.STONE_AXE, Formatting.DARK_RED, 380, "Rune chopping", 5, "axe"); }
    public static ItemStack createChiseledNetherBrickShovel() { return createBlockTool("Chiseled Nether Brick", "Shovel", "🪏", Items.STONE_SHOVEL, Formatting.DARK_RED, 380, "Old dig", 4, "shovel"); }
    public static ItemStack createChiseledNetherBrickHoe() { return createBlockTool("Chiseled Nether Brick", "Hoe", "🌾", Items.STONE_HOE, Formatting.DARK_RED, 380, "Fortress farming", 3, "hoe"); }
    
    // CRACKED NETHER BRICK TOOLS - Damaged nether brick
    public static ItemStack createCrackedNetherBrickPickaxe() { return createBlockTool("Cracked Nether Brick", "Pickaxe", "⛏", Items.STONE_PICKAXE, Formatting.RED, 320, "Worn mining", 4, "pickaxe"); }
    public static ItemStack createCrackedNetherBrickAxe() { return createBlockTool("Cracked Nether Brick", "Axe", "🪓", Items.STONE_AXE, Formatting.RED, 320, "Broken chopping", 4, "axe"); }
    public static ItemStack createCrackedNetherBrickShovel() { return createBlockTool("Cracked Nether Brick", "Shovel", "🪏", Items.STONE_SHOVEL, Formatting.RED, 320, "Ruin dig", 3, "shovel"); }
    public static ItemStack createCrackedNetherBrickHoe() { return createBlockTool("Cracked Nether Brick", "Hoe", "🌾", Items.STONE_HOE, Formatting.RED, 320, "Ancient farming", 2, "hoe"); }
    
    // CHISELED STONE BRICK TOOLS - Carved stone
    public static ItemStack createChiseledStoneBrickPickaxe() { return createBlockTool("Chiseled Stone Brick", "Pickaxe", "⛏", Items.STONE_PICKAXE, Formatting.GRAY, 400, "Masonry mining", 5, "pickaxe"); }
    public static ItemStack createChiseledStoneBrickAxe() { return createBlockTool("Chiseled Stone Brick", "Axe", "🪓", Items.STONE_AXE, Formatting.GRAY, 400, "Artisan chopping", 5, "axe"); }
    public static ItemStack createChiseledStoneBrickShovel() { return createBlockTool("Chiseled Stone Brick", "Shovel", "🪏", Items.STONE_SHOVEL, Formatting.GRAY, 400, "Carved dig", 4, "shovel"); }
    public static ItemStack createChiseledStoneBrickHoe() { return createBlockTool("Chiseled Stone Brick", "Hoe", "🌾", Items.STONE_HOE, Formatting.GRAY, 400, "Builder farming", 3, "hoe"); }
    
    // CRACKED STONE BRICK TOOLS - Damaged stone
    public static ItemStack createCrackedStoneBrickPickaxe() { return createBlockTool("Cracked Stone Brick", "Pickaxe", "⛏", Items.STONE_PICKAXE, Formatting.DARK_GRAY, 340, "Ruin mining", 4, "pickaxe"); }
    public static ItemStack createCrackedStoneBrickAxe() { return createBlockTool("Cracked Stone Brick", "Axe", "🪓", Items.STONE_AXE, Formatting.DARK_GRAY, 340, "Old chopping", 4, "axe"); }
    public static ItemStack createCrackedStoneBrickShovel() { return createBlockTool("Cracked Stone Brick", "Shovel", "🪏", Items.STONE_SHOVEL, Formatting.DARK_GRAY, 340, "Broken dig", 3, "shovel"); }
    public static ItemStack createCrackedStoneBrickHoe() { return createBlockTool("Cracked Stone Brick", "Hoe", "🌾", Items.STONE_HOE, Formatting.DARK_GRAY, 340, "Ruins farming", 2, "hoe"); }
    
    // END STONE BRICK TOOLS - End portal stone
    public static ItemStack createEndStoneBrickPickaxe() { return createBlockTool("End Stone Brick", "Pickaxe", "⛏", Items.STONE_PICKAXE, Formatting.YELLOW, 360, "End mining", 5, "pickaxe"); }
    public static ItemStack createEndStoneBrickAxe() { return createBlockTool("End Stone Brick", "Axe", "🪓", Items.STONE_AXE, Formatting.YELLOW, 360, "Void chopping", 5, "axe"); }
    public static ItemStack createEndStoneBrickShovel() { return createBlockTool("End Stone Brick", "Shovel", "🪏", Items.STONE_SHOVEL, Formatting.YELLOW, 360, "Outer dig", 4, "shovel"); }
    public static ItemStack createEndStoneBrickHoe() { return createBlockTool("End Stone Brick", "Hoe", "🌾", Items.STONE_HOE, Formatting.YELLOW, 360, "End farming", 3, "hoe"); }
    
    // RED SANDSTONE TOOLS - Red desert stone
    public static ItemStack createRedSandstonePickaxe() { return createBlockTool("Red Sandstone", "Pickaxe", "⛏", Items.WOODEN_PICKAXE, Formatting.RED, 280, "Desert mining", 3, "pickaxe"); }
    public static ItemStack createRedSandstoneAxe() { return createBlockTool("Red Sandstone", "Axe", "🪓", Items.WOODEN_AXE, Formatting.RED, 280, "Dune chopping", 3, "axe"); }
    public static ItemStack createRedSandstoneShovel() { return createBlockTool("Red Sandstone", "Shovel", "🪏", Items.WOODEN_SHOVEL, Formatting.RED, 280, "Arid dig", 2, "shovel"); }
    public static ItemStack createRedSandstoneHoe() { return createBlockTool("Red Sandstone", "Hoe", "🌾", Items.WOODEN_HOE, Formatting.RED, 280, "Desert farming", 2, "hoe"); }
    
    // RAW IRON BLOCK TOOLS - Raw iron storage
    public static ItemStack createRawIronBlockPickaxe() { return createBlockTool("Raw Iron Block", "Pickaxe", "⛏", Items.DIAMOND_PICKAXE, Formatting.WHITE, 500, "Heavy mining", 6, "pickaxe"); }
    public static ItemStack createRawIronBlockAxe() { return createBlockTool("Raw Iron Block", "Axe", "🪓", Items.DIAMOND_AXE, Formatting.WHITE, 500, "Metal chopping", 6, "axe"); }
    public static ItemStack createRawIronBlockShovel() { return createBlockTool("Raw Iron Block", "Shovel", "🪏", Items.DIAMOND_SHOVEL, Formatting.WHITE, 500, "Ore dig", 5, "shovel"); }
    public static ItemStack createRawIronBlockHoe() { return createBlockTool("Raw Iron Block", "Hoe", "🌾", Items.DIAMOND_HOE, Formatting.WHITE, 500, "Mineral farming", 4, "hoe"); }
    
    // RAW GOLD BLOCK TOOLS - Raw gold storage
    public static ItemStack createRawGoldBlockPickaxe() { return createBlockTool("Raw Gold Block", "Pickaxe", "⛏", Items.GOLDEN_PICKAXE, Formatting.GOLD, 350, "Rich mining", 4, "pickaxe"); }
    public static ItemStack createRawGoldBlockAxe() { return createBlockTool("Raw Gold Block", "Axe", "🪓", Items.GOLDEN_AXE, Formatting.GOLD, 350, "Fortune chopping", 4, "axe"); }
    public static ItemStack createRawGoldBlockShovel() { return createBlockTool("Raw Gold Block", "Shovel", "🪏", Items.GOLDEN_SHOVEL, Formatting.GOLD, 350, "Gold dig", 3, "shovel"); }
    public static ItemStack createRawGoldBlockHoe() { return createBlockTool("Raw Gold Block", "Hoe", "🌾", Items.GOLDEN_HOE, Formatting.GOLD, 350, "Treasure farming", 2, "hoe"); }
    
    // PRISMARINE BRICKS TOOLS - Ocean castle brick
    public static ItemStack createPrismarineBricksPickaxe() { return createBlockTool("Prismarine Bricks", "Pickaxe", "⛏", Items.DIAMOND_PICKAXE, Formatting.DARK_AQUA, 320, "Ocean mining", 5, "pickaxe"); }
    public static ItemStack createPrismarineBricksAxe() { return createBlockTool("Prismarine Bricks", "Axe", "🪓", Items.DIAMOND_AXE, Formatting.DARK_AQUA, 320, "Monument chopping", 5, "axe"); }
    public static ItemStack createPrismarineBricksShovel() { return createBlockTool("Prismarine Bricks", "Shovel", "🪏", Items.DIAMOND_SHOVEL, Formatting.DARK_AQUA, 320, "Waterproof dig", 4, "shovel"); }
    public static ItemStack createPrismarineBricksHoe() { return createBlockTool("Prismarine Bricks", "Hoe", "🌾", Items.DIAMOND_HOE, Formatting.DARK_AQUA, 320, "Ocean farming", 3, "hoe"); }
    
    // DARK PRISMARINE TOOLS - Deep ocean stone
    public static ItemStack createDarkPrismarinePickaxe() { return createBlockTool("Dark Prismarine", "Pickaxe", "⛏", Items.DIAMOND_PICKAXE, Formatting.DARK_GREEN, 360, "Deep ocean mining", 5, "pickaxe"); }
    public static ItemStack createDarkPrismarineAxe() { return createBlockTool("Dark Prismarine", "Axe", "🪓", Items.DIAMOND_AXE, Formatting.DARK_GREEN, 360, "Abyss chopping", 5, "axe"); }
    public static ItemStack createDarkPrismarineShovel() { return createBlockTool("Dark Prismarine", "Shovel", "🪏", Items.DIAMOND_SHOVEL, Formatting.DARK_GREEN, 360, "Deep dig", 4, "shovel"); }
    public static ItemStack createDarkPrismarineHoe() { return createBlockTool("Dark Prismarine", "Hoe", "🌾", Items.DIAMOND_HOE, Formatting.DARK_GREEN, 360, "Abyssal farming", 3, "hoe"); }
    
    // SEA LANTERN TOOLS - Glowing ocean light
    public static ItemStack createSeaLanternPickaxe() { return createBlockTool("Sea Lantern", "Pickaxe", "⛏", Items.GOLDEN_PICKAXE, Formatting.AQUA, 220, "Glowing mining", 3, "pickaxe"); }
    public static ItemStack createSeaLanternAxe() { return createBlockTool("Sea Lantern", "Axe", "🪓", Items.GOLDEN_AXE, Formatting.AQUA, 220, "Light chopping", 3, "axe"); }
    public static ItemStack createSeaLanternShovel() { return createBlockTool("Sea Lantern", "Shovel", "🪏", Items.GOLDEN_SHOVEL, Formatting.AQUA, 220, "Luminous dig", 2, "shovel"); }
    public static ItemStack createSeaLanternHoe() { return createBlockTool("Sea Lantern", "Hoe", "🌾", Items.GOLDEN_HOE, Formatting.AQUA, 220, "Bright farming", 2, "hoe"); }
    
    // LODESTONE TOOLS - Compass stone
    public static ItemStack createLodestonePickaxe() { return createBlockTool("Lodestone", "Pickaxe", "⛏", Items.DIAMOND_PICKAXE, Formatting.GRAY, 450, "Magnetic mining", 6, "pickaxe"); }
    public static ItemStack createLodestoneAxe() { return createBlockTool("Lodestone", "Axe", "🪓", Items.DIAMOND_AXE, Formatting.GRAY, 450, "True north chopping", 6, "axe"); }
    public static ItemStack createLodestoneShovel() { return createBlockTool("Lodestone", "Shovel", "🪏", Items.DIAMOND_SHOVEL, Formatting.GRAY, 450, "Compass dig", 5, "shovel"); }
    public static ItemStack createLodestoneHoe() { return createBlockTool("Lodestone", "Hoe", "🌾", Items.DIAMOND_HOE, Formatting.GRAY, 450, "Direction farming", 4, "hoe"); }
    
    // BLACKSTONE BRICKS TOOLS - Polished blackstone
    public static ItemStack createBlackstoneBricksPickaxe() { return createBlockTool("Blackstone Bricks", "Pickaxe", "⛏", Items.STONE_PICKAXE, Formatting.DARK_GRAY, 400, "Fortress mining", 5, "pickaxe"); }
    public static ItemStack createBlackstoneBricksAxe() { return createBlockTool("Blackstone Bricks", "Axe", "🪓", Items.STONE_AXE, Formatting.DARK_GRAY, 400, "Strong chopping", 5, "axe"); }
    public static ItemStack createBlackstoneBricksShovel() { return createBlockTool("Blackstone Bricks", "Shovel", "🪏", Items.STONE_SHOVEL, Formatting.DARK_GRAY, 400, "Dark dig", 4, "shovel"); }
    public static ItemStack createBlackstoneBricksHoe() { return createBlockTool("Blackstone Bricks", "Hoe", "🌾", Items.STONE_HOE, Formatting.DARK_GRAY, 400, "Castle farming", 3, "hoe"); }
    
    // POLISHED BLACKSTONE TOOLS - Smooth blackstone
    public static ItemStack createPolishedBlackstonePickaxe() { return createBlockTool("Polished Blackstone", "Pickaxe", "⛏", Items.STONE_PICKAXE, Formatting.GRAY, 380, "Sleek mining", 5, "pickaxe"); }
    public static ItemStack createPolishedBlackstoneAxe() { return createBlockTool("Polished Blackstone", "Axe", "🪓", Items.STONE_AXE, Formatting.GRAY, 380, "Smooth chopping", 5, "axe"); }
    public static ItemStack createPolishedBlackstoneShovel() { return createBlockTool("Polished Blackstone", "Shovel", "🪏", Items.STONE_SHOVEL, Formatting.GRAY, 380, "Polished dig", 4, "shovel"); }
    public static ItemStack createPolishedBlackstoneHoe() { return createBlockTool("Polished Blackstone", "Hoe", "🌾", Items.STONE_HOE, Formatting.GRAY, 380, "Netherite farming", 3, "hoe"); }
    
    // SMOOTH BASALT TOOLS - Polished volcanic rock
    public static ItemStack createSmoothBasaltPickaxe() { return createBlockTool("Smooth Basalt", "Pickaxe", "⛏", Items.DIAMOND_PICKAXE, Formatting.DARK_GRAY, 420, "Volcanic mining", 6, "pickaxe"); }
    public static ItemStack createSmoothBasaltAxe() { return createBlockTool("Smooth Basalt", "Axe", "🪓", Items.DIAMOND_AXE, Formatting.DARK_GRAY, 420, "Pillar chopping", 6, "axe"); }
    public static ItemStack createSmoothBasaltShovel() { return createBlockTool("Smooth Basalt", "Shovel", "🪏", Items.DIAMOND_SHOVEL, Formatting.DARK_GRAY, 420, "Lava dig", 5, "shovel"); }
    public static ItemStack createSmoothBasaltHoe() { return createBlockTool("Smooth Basalt", "Hoe", "🌾", Items.DIAMOND_HOE, Formatting.DARK_GRAY, 420, "Basalt farming", 4, "hoe"); }
    
    // AMETHYST CLUSTER TOOLS - Purple crystal
    public static ItemStack createAmethystClusterPickaxe() { return createBlockTool("Amethyst Cluster", "Pickaxe", "⛏", Items.GOLDEN_PICKAXE, Formatting.LIGHT_PURPLE, 280, "Crystal mining", 4, "pickaxe"); }
    public static ItemStack createAmethystClusterAxe() { return createBlockTool("Amethyst Cluster", "Axe", "🪓", Items.GOLDEN_AXE, Formatting.LIGHT_PURPLE, 280, "Gem chopping", 4, "axe"); }
    public static ItemStack createAmethystClusterShovel() { return createBlockTool("Amethyst Cluster", "Shovel", "🪏", Items.GOLDEN_SHOVEL, Formatting.LIGHT_PURPLE, 280, "Crystal dig", 3, "shovel"); }
    public static ItemStack createAmethystClusterHoe() { return createBlockTool("Amethyst Cluster", "Hoe", "🌾", Items.GOLDEN_HOE, Formatting.LIGHT_PURPLE, 280, "Gem farming", 2, "hoe"); }
    
    // OBSIDIAN BLOCK TOOLS - Dark volcanic glass
    public static ItemStack createObsidianBlockPickaxe() { return createBlockTool("Obsidian", "Pickaxe", "⛏", Items.NETHERITE_PICKAXE, Formatting.DARK_PURPLE, 2000, "Bedrock breaker", 8, "pickaxe"); }
    public static ItemStack createObsidianBlockAxe() { return createBlockTool("Obsidian", "Axe", "🪓", Items.NETHERITE_AXE, Formatting.DARK_PURPLE, 2000, "Void chopping", 8, "axe"); }
    public static ItemStack createObsidianBlockShovel() { return createBlockTool("Obsidian", "Shovel", "🪏", Items.NETHERITE_SHOVEL, Formatting.DARK_PURPLE, 2000, "Dark dig", 7, "shovel"); }
    public static ItemStack createObsidianBlockHoe() { return createBlockTool("Obsidian", "Hoe", "🌾", Items.NETHERITE_HOE, Formatting.DARK_PURPLE, 2000, "End farming", 6, "hoe"); }

    // ============================================================
    // NEW ARMOR SET TOOLS
    // ============================================================

    // STRIPPED LOG TOOLS
    public static ItemStack createStrippedSpruceLogPickaxe() { return createBlockTool("Stripped Spruce Log", "Pickaxe", "⛏", Items.IRON_PICKAXE, Formatting.DARK_AQUA, 250, "Forest mining", 5, "pickaxe"); }
    public static ItemStack createStrippedSpruceLogAxe() { return createBlockTool("Stripped Spruce Log", "Axe", "🪓", Items.IRON_AXE, Formatting.DARK_AQUA, 250, "Tree chop", 5, "axe"); }
    public static ItemStack createStrippedSpruceLogShovel() { return createBlockTool("Stripped Spruce Log", "Shovel", "🪏", Items.IRON_SHOVEL, Formatting.DARK_AQUA, 250, "Forest dig", 4, "shovel"); }
    public static ItemStack createStrippedSpruceLogHoe() { return createBlockTool("Stripped Spruce Log", "Hoe", "🌾", Items.IRON_HOE, Formatting.DARK_AQUA, 250, "Forest farm", 4, "hoe"); }

    public static ItemStack createStrippedBirchLogPickaxe() { return createBlockTool("Stripped Birch Log", "Pickaxe", "⛏", Items.IRON_PICKAXE, Formatting.WHITE, 200, "Light mining", 4, "pickaxe"); }
    public static ItemStack createStrippedBirchLogAxe() { return createBlockTool("Stripped Birch Log", "Axe", "🪓", Items.IRON_AXE, Formatting.WHITE, 200, "Light chop", 4, "axe"); }
    public static ItemStack createStrippedBirchLogShovel() { return createBlockTool("Stripped Birch Log", "Shovel", "🪏", Items.IRON_SHOVEL, Formatting.WHITE, 200, "Light dig", 3, "shovel"); }
    public static ItemStack createStrippedBirchLogHoe() { return createBlockTool("Stripped Birch Log", "Hoe", "🌾", Items.IRON_HOE, Formatting.WHITE, 200, "Light farm", 3, "hoe"); }

    public static ItemStack createStrippedDarkOakLogPickaxe() { return createBlockTool("Stripped Dark Oak Log", "Pickaxe", "⛏", Items.IRON_PICKAXE, Formatting.DARK_GRAY, 300, "Shadow mining", 6, "pickaxe"); }
    public static ItemStack createStrippedDarkOakLogAxe() { return createBlockTool("Stripped Dark Oak Log", "Axe", "🪓", Items.IRON_AXE, Formatting.DARK_GRAY, 300, "Shadow chop", 6, "axe"); }
    public static ItemStack createStrippedDarkOakLogShovel() { return createBlockTool("Stripped Dark Oak Log", "Shovel", "🪏", Items.IRON_SHOVEL, Formatting.DARK_GRAY, 300, "Shadow dig", 5, "shovel"); }
    public static ItemStack createStrippedDarkOakLogHoe() { return createBlockTool("Stripped Dark Oak Log", "Hoe", "🌾", Items.IRON_HOE, Formatting.DARK_GRAY, 300, "Shadow farm", 5, "hoe"); }

    public static ItemStack createStrippedJungleLogPickaxe() { return createBlockTool("Stripped Jungle Log", "Pickaxe", "⛏", Items.IRON_PICKAXE, Formatting.GREEN, 250, "Jungle mining", 5, "pickaxe"); }
    public static ItemStack createStrippedJungleLogAxe() { return createBlockTool("Stripped Jungle Log", "Axe", "🪓", Items.IRON_AXE, Formatting.GREEN, 250, "Jungle chop", 5, "axe"); }
    public static ItemStack createStrippedJungleLogShovel() { return createBlockTool("Stripped Jungle Log", "Shovel", "🪏", Items.IRON_SHOVEL, Formatting.GREEN, 250, "Jungle dig", 4, "shovel"); }
    public static ItemStack createStrippedJungleLogHoe() { return createBlockTool("Stripped Jungle Log", "Hoe", "🌾", Items.IRON_HOE, Formatting.GREEN, 250, "Jungle farm", 4, "hoe"); }

    public static ItemStack createStrippedAcaciaLogPickaxe() { return createBlockTool("Stripped Acacia Log", "Pickaxe", "⛏", Items.IRON_PICKAXE, Formatting.GOLD, 260, "Savanna mining", 5, "pickaxe"); }
    public static ItemStack createStrippedAcaciaLogAxe() { return createBlockTool("Stripped Acacia Log", "Axe", "🪓", Items.IRON_AXE, Formatting.GOLD, 260, "Savanna chop", 5, "axe"); }
    public static ItemStack createStrippedAcaciaLogShovel() { return createBlockTool("Stripped Acacia Log", "Shovel", "🪏", Items.IRON_SHOVEL, Formatting.GOLD, 260, "Savanna dig", 4, "shovel"); }
    public static ItemStack createStrippedAcaciaLogHoe() { return createBlockTool("Stripped Acacia Log", "Hoe", "🌾", Items.IRON_HOE, Formatting.GOLD, 260, "Savanna farm", 4, "hoe"); }

    public static ItemStack createStrippedMangroveLogPickaxe() { return createBlockTool("Stripped Mangrove Log", "Pickaxe", "⛏", Items.IRON_PICKAXE, Formatting.DARK_RED, 280, "Swamp mining", 5, "pickaxe"); }
    public static ItemStack createStrippedMangroveLogAxe() { return createBlockTool("Stripped Mangrove Log", "Axe", "🪓", Items.IRON_AXE, Formatting.DARK_RED, 280, "Swamp chop", 5, "axe"); }
    public static ItemStack createStrippedMangroveLogShovel() { return createBlockTool("Stripped Mangrove Log", "Shovel", "🪏", Items.IRON_SHOVEL, Formatting.DARK_RED, 280, "Swamp dig", 4, "shovel"); }
    public static ItemStack createStrippedMangroveLogHoe() { return createBlockTool("Stripped Mangrove Log", "Hoe", "🌾", Items.IRON_HOE, Formatting.DARK_RED, 280, "Swamp farm", 4, "hoe"); }

    public static ItemStack createStrippedCherryLogPickaxe() { return createBlockTool("Stripped Cherry Log", "Pickaxe", "⛏", Items.IRON_PICKAXE, Formatting.LIGHT_PURPLE, 240, "Cherry mining", 4, "pickaxe"); }
    public static ItemStack createStrippedCherryLogAxe() { return createBlockTool("Stripped Cherry Log", "Axe", "🪓", Items.IRON_AXE, Formatting.LIGHT_PURPLE, 240, "Cherry chop", 4, "axe"); }
    public static ItemStack createStrippedCherryLogShovel() { return createBlockTool("Stripped Cherry Log", "Shovel", "🪏", Items.IRON_SHOVEL, Formatting.LIGHT_PURPLE, 240, "Cherry dig", 3, "shovel"); }
    public static ItemStack createStrippedCherryLogHoe() { return createBlockTool("Stripped Cherry Log", "Hoe", "🌾", Items.IRON_HOE, Formatting.LIGHT_PURPLE, 240, "Cherry farm", 3, "hoe"); }

    public static ItemStack createStrippedBambooBlockPickaxe() { return createBlockTool("Bamboo Block", "Pickaxe", "⛏", Items.IRON_PICKAXE, Formatting.YELLOW, 180, "Quick mining", 3, "pickaxe"); }
    public static ItemStack createStrippedBambooBlockAxe() { return createBlockTool("Bamboo Block", "Axe", "🪓", Items.IRON_AXE, Formatting.YELLOW, 180, "Quick chop", 3, "axe"); }
    public static ItemStack createStrippedBambooBlockShovel() { return createBlockTool("Bamboo Block", "Shovel", "🪏", Items.IRON_SHOVEL, Formatting.YELLOW, 180, "Quick dig", 2, "shovel"); }
    public static ItemStack createStrippedBambooBlockHoe() { return createBlockTool("Bamboo Block", "Hoe", "🌾", Items.IRON_HOE, Formatting.YELLOW, 180, "Quick farm", 2, "hoe"); }

    public static ItemStack createStrippedCrimsonStemPickaxe() { return createBlockTool("Crimson Stem", "Pickaxe", "⛏", Items.DIAMOND_PICKAXE, Formatting.RED, 400, "Nether mining", 7, "pickaxe"); }
    public static ItemStack createStrippedCrimsonStemAxe() { return createBlockTool("Crimson Stem", "Axe", "🪓", Items.DIAMOND_AXE, Formatting.RED, 400, "Nether chop", 7, "axe"); }
    public static ItemStack createStrippedCrimsonStemShovel() { return createBlockTool("Crimson Stem", "Shovel", "🪏", Items.DIAMOND_SHOVEL, Formatting.RED, 400, "Nether dig", 6, "shovel"); }
    public static ItemStack createStrippedCrimsonStemHoe() { return createBlockTool("Crimson Stem", "Hoe", "🌾", Items.DIAMOND_HOE, Formatting.RED, 400, "Nether farm", 6, "hoe"); }

    public static ItemStack createStrippedWarpedStemPickaxe() { return createBlockTool("Warped Stem", "Pickaxe", "⛏", Items.DIAMOND_PICKAXE, Formatting.AQUA, 420, "Warped mining", 7, "pickaxe"); }
    public static ItemStack createStrippedWarpedStemAxe() { return createBlockTool("Warped Stem", "Axe", "🪓", Items.DIAMOND_AXE, Formatting.AQUA, 420, "Warped chop", 7, "axe"); }
    public static ItemStack createStrippedWarpedStemShovel() { return createBlockTool("Warped Stem", "Shovel", "🪏", Items.DIAMOND_SHOVEL, Formatting.AQUA, 420, "Warped dig", 6, "shovel"); }
    public static ItemStack createStrippedWarpedStemHoe() { return createBlockTool("Warped Stem", "Hoe", "🌾", Items.DIAMOND_HOE, Formatting.AQUA, 420, "Warped farm", 6, "hoe"); }

    // PLANK TOOLS
    public static ItemStack createSprucePlanksPickaxe() { return createBlockTool("Spruce Planks", "Pickaxe", "⛏", Items.STONE_PICKAXE, Formatting.DARK_AQUA, 150, "Forest floor", 3, "pickaxe"); }
    public static ItemStack createSprucePlanksAxe() { return createBlockTool("Spruce Planks", "Axe", "🪓", Items.STONE_AXE, Formatting.DARK_AQUA, 150, "Forest wood", 3, "axe"); }
    public static ItemStack createSprucePlanksShovel() { return createBlockTool("Spruce Planks", "Shovel", "🪏", Items.STONE_SHOVEL, Formatting.DARK_AQUA, 150, "Forest dirt", 2, "shovel"); }
    public static ItemStack createSprucePlanksHoe() { return createBlockTool("Spruce Planks", "Hoe", "🌾", Items.STONE_HOE, Formatting.DARK_AQUA, 150, "Forest crop", 2, "hoe"); }

    public static ItemStack createBirchPlanksPickaxe() { return createBlockTool("Birch Planks", "Pickaxe", "⛏", Items.STONE_PICKAXE, Formatting.WHITE, 120, "Light build", 2, "pickaxe"); }
    public static ItemStack createBirchPlanksAxe() { return createBlockTool("Birch Planks", "Axe", "🪓", Items.STONE_AXE, Formatting.WHITE, 120, "Light wood", 2, "axe"); }
    public static ItemStack createBirchPlanksShovel() { return createBlockTool("Birch Planks", "Shovel", "🪏", Items.STONE_SHOVEL, Formatting.WHITE, 120, "Light dirt", 1, "shovel"); }
    public static ItemStack createBirchPlanksHoe() { return createBlockTool("Birch Planks", "Hoe", "🌾", Items.STONE_HOE, Formatting.WHITE, 120, "Light crop", 1, "hoe"); }

    public static ItemStack createJunglePlanksPickaxe() { return createBlockTool("Jungle Planks", "Pickaxe", "⛏", Items.STONE_PICKAXE, Formatting.GREEN, 150, "Jungle floor", 3, "pickaxe"); }
    public static ItemStack createJunglePlanksAxe() { return createBlockTool("Jungle Planks", "Axe", "🪓", Items.STONE_AXE, Formatting.GREEN, 150, "Jungle wood", 3, "axe"); }
    public static ItemStack createJunglePlanksShovel() { return createBlockTool("Jungle Planks", "Shovel", "🪏", Items.STONE_SHOVEL, Formatting.GREEN, 150, "Jungle dirt", 2, "shovel"); }
    public static ItemStack createJunglePlanksHoe() { return createBlockTool("Jungle Planks", "Hoe", "🌾", Items.STONE_HOE, Formatting.GREEN, 150, "Jungle crop", 2, "hoe"); }

    public static ItemStack createAcaciaPlanksPickaxe() { return createBlockTool("Acacia Planks", "Pickaxe", "⛏", Items.STONE_PICKAXE, Formatting.GOLD, 160, "Savanna build", 3, "pickaxe"); }
    public static ItemStack createAcaciaPlanksAxe() { return createBlockTool("Acacia Planks", "Axe", "🪓", Items.STONE_AXE, Formatting.GOLD, 160, "Savanna wood", 3, "axe"); }
    public static ItemStack createAcaciaPlanksShovel() { return createBlockTool("Acacia Planks", "Shovel", "🪏", Items.STONE_SHOVEL, Formatting.GOLD, 160, "Savanna dirt", 2, "shovel"); }
    public static ItemStack createAcaciaPlanksHoe() { return createBlockTool("Acacia Planks", "Hoe", "🌾", Items.STONE_HOE, Formatting.GOLD, 160, "Savanna crop", 2, "hoe"); }

    public static ItemStack createDarkOakPlanksPickaxe() { return createBlockTool("Dark Oak Planks", "Pickaxe", "⛏", Items.STONE_PICKAXE, Formatting.DARK_GRAY, 180, "Dark build", 3, "pickaxe"); }
    public static ItemStack createDarkOakPlanksAxe() { return createBlockTool("Dark Oak Planks", "Axe", "🪓", Items.STONE_AXE, Formatting.DARK_GRAY, 180, "Dark wood", 3, "axe"); }
    public static ItemStack createDarkOakPlanksShovel() { return createBlockTool("Dark Oak Planks", "Shovel", "🪏", Items.STONE_SHOVEL, Formatting.DARK_GRAY, 180, "Dark dirt", 2, "shovel"); }
    public static ItemStack createDarkOakPlanksHoe() { return createBlockTool("Dark Oak Planks", "Hoe", "🌾", Items.STONE_HOE, Formatting.DARK_GRAY, 180, "Dark crop", 2, "hoe"); }

    public static ItemStack createMangrovePlanksPickaxe() { return createBlockTool("Mangrove Planks", "Pickaxe", "⛏", Items.STONE_PICKAXE, Formatting.DARK_RED, 170, "Swamp build", 3, "pickaxe"); }
    public static ItemStack createMangrovePlanksAxe() { return createBlockTool("Mangrove Planks", "Axe", "🪓", Items.STONE_AXE, Formatting.DARK_RED, 170, "Swamp wood", 3, "axe"); }
    public static ItemStack createMangrovePlanksShovel() { return createBlockTool("Mangrove Planks", "Shovel", "🪏", Items.STONE_SHOVEL, Formatting.DARK_RED, 170, "Swamp dirt", 2, "shovel"); }
    public static ItemStack createMangrovePlanksHoe() { return createBlockTool("Mangrove Planks", "Hoe", "🌾", Items.STONE_HOE, Formatting.DARK_RED, 170, "Swamp crop", 2, "hoe"); }

    public static ItemStack createCherryPlanksPickaxe() { return createBlockTool("Cherry Planks", "Pickaxe", "⛏", Items.STONE_PICKAXE, Formatting.LIGHT_PURPLE, 140, "Cherry build", 2, "pickaxe"); }
    public static ItemStack createCherryPlanksAxe() { return createBlockTool("Cherry Planks", "Axe", "🪓", Items.STONE_AXE, Formatting.LIGHT_PURPLE, 140, "Cherry wood", 2, "axe"); }
    public static ItemStack createCherryPlanksShovel() { return createBlockTool("Cherry Planks", "Shovel", "🪏", Items.STONE_SHOVEL, Formatting.LIGHT_PURPLE, 140, "Cherry dirt", 1, "shovel"); }
    public static ItemStack createCherryPlanksHoe() { return createBlockTool("Cherry Planks", "Hoe", "🌾", Items.STONE_HOE, Formatting.LIGHT_PURPLE, 140, "Cherry crop", 1, "hoe"); }

    public static ItemStack createBambooPlanksPickaxe() { return createBlockTool("Bamboo Planks", "Pickaxe", "⛏", Items.STONE_PICKAXE, Formatting.YELLOW, 100, "Quick build", 2, "pickaxe"); }
    public static ItemStack createBambooPlanksAxe() { return createBlockTool("Bamboo Planks", "Axe", "🪓", Items.STONE_AXE, Formatting.YELLOW, 100, "Quick wood", 2, "axe"); }
    public static ItemStack createBambooPlanksShovel() { return createBlockTool("Bamboo Planks", "Shovel", "🪏", Items.STONE_SHOVEL, Formatting.YELLOW, 100, "Quick dig", 1, "shovel"); }
    public static ItemStack createBambooPlanksHoe() { return createBlockTool("Bamboo Planks", "Hoe", "🌾", Items.STONE_HOE, Formatting.YELLOW, 100, "Quick farm", 1, "hoe"); }

    public static ItemStack createCrimsonPlanksPickaxe() { return createBlockTool("Crimson Planks", "Pickaxe", "⛏", Items.IRON_PICKAXE, Formatting.RED, 250, "Nether build", 5, "pickaxe"); }
    public static ItemStack createCrimsonPlanksAxe() { return createBlockTool("Crimson Planks", "Axe", "🪓", Items.IRON_AXE, Formatting.RED, 250, "Nether wood", 5, "axe"); }
    public static ItemStack createCrimsonPlanksShovel() { return createBlockTool("Crimson Planks", "Shovel", "🪏", Items.IRON_SHOVEL, Formatting.RED, 250, "Nether dirt", 4, "shovel"); }
    public static ItemStack createCrimsonPlanksHoe() { return createBlockTool("Crimson Planks", "Hoe", "🌾", Items.IRON_HOE, Formatting.RED, 250, "Nether crop", 4, "hoe"); }

    public static ItemStack createWarpedPlanksPickaxe() { return createBlockTool("Warped Planks", "Pickaxe", "⛏", Items.IRON_PICKAXE, Formatting.AQUA, 260, "Warped build", 5, "pickaxe"); }
    public static ItemStack createWarpedPlanksAxe() { return createBlockTool("Warped Planks", "Axe", "🪓", Items.IRON_AXE, Formatting.AQUA, 260, "Warped wood", 5, "axe"); }
    public static ItemStack createWarpedPlanksShovel() { return createBlockTool("Warped Planks", "Shovel", "🪏", Items.IRON_SHOVEL, Formatting.AQUA, 260, "Warped dirt", 4, "shovel"); }
    public static ItemStack createWarpedPlanksHoe() { return createBlockTool("Warped Planks", "Hoe", "🌾", Items.IRON_HOE, Formatting.AQUA, 260, "Warped crop", 4, "hoe"); }

    public static ItemStack createOakPlanksPickaxe() { return createBlockTool("Oak Planks", "Pickaxe", "⛏", Items.STONE_PICKAXE, Formatting.YELLOW, 120, "Classic build", 2, "pickaxe"); }
    public static ItemStack createOakPlanksAxe() { return createBlockTool("Oak Planks", "Axe", "🪓", Items.STONE_AXE, Formatting.YELLOW, 120, "Classic wood", 2, "axe"); }
    public static ItemStack createOakPlanksShovel() { return createBlockTool("Oak Planks", "Shovel", "🪏", Items.STONE_SHOVEL, Formatting.YELLOW, 120, "Classic dirt", 1, "shovel"); }
    public static ItemStack createOakPlanksHoe() { return createBlockTool("Oak Planks", "Hoe", "🌾", Items.STONE_HOE, Formatting.YELLOW, 120, "Classic crop", 1, "hoe"); }

    // STONE/MUD/LOG TOOLS
    public static ItemStack createOakLogPickaxe() { return createBlockTool("Oak Log", "Pickaxe", "⛏", Items.IRON_PICKAXE, Formatting.YELLOW, 200, "Ancient wood", 4, "pickaxe"); }
    public static ItemStack createOakLogAxe() { return createBlockTool("Oak Log", "Axe", "🪓", Items.IRON_AXE, Formatting.YELLOW, 200, "Ancient chop", 4, "axe"); }
    public static ItemStack createOakLogShovel() { return createBlockTool("Oak Log", "Shovel", "🪏", Items.IRON_SHOVEL, Formatting.YELLOW, 200, "Ancient dig", 3, "shovel"); }
    public static ItemStack createOakLogHoe() { return createBlockTool("Oak Log", "Hoe", "🌾", Items.IRON_HOE, Formatting.YELLOW, 200, "Ancient farm", 3, "hoe"); }

    public static ItemStack createSpruceLogPickaxe() { return createBlockTool("Spruce Log", "Pickaxe", "⛏", Items.IRON_PICKAXE, Formatting.DARK_AQUA, 210, "Pine mining", 4, "pickaxe"); }
    public static ItemStack createSpruceLogAxe() { return createBlockTool("Spruce Log", "Axe", "🪓", Items.IRON_AXE, Formatting.DARK_AQUA, 210, "Pine chop", 4, "axe"); }
    public static ItemStack createSpruceLogShovel() { return createBlockTool("Spruce Log", "Shovel", "🪏", Items.IRON_SHOVEL, Formatting.DARK_AQUA, 210, "Pine dig", 3, "shovel"); }
    public static ItemStack createSpruceLogHoe() { return createBlockTool("Spruce Log", "Hoe", "🌾", Items.IRON_HOE, Formatting.DARK_AQUA, 210, "Pine farm", 3, "hoe"); }

    public static ItemStack createBirchLogPickaxe() { return createBlockTool("Birch Log", "Pickaxe", "⛏", Items.IRON_PICKAXE, Formatting.WHITE, 180, "Birch mining", 3, "pickaxe"); }
    public static ItemStack createBirchLogAxe() { return createBlockTool("Birch Log", "Axe", "🪓", Items.IRON_AXE, Formatting.WHITE, 180, "Birch chop", 3, "axe"); }
    public static ItemStack createBirchLogShovel() { return createBlockTool("Birch Log", "Shovel", "🪏", Items.IRON_SHOVEL, Formatting.WHITE, 180, "Birch dig", 2, "shovel"); }
    public static ItemStack createBirchLogHoe() { return createBlockTool("Birch Log", "Hoe", "🌾", Items.IRON_HOE, Formatting.WHITE, 180, "Birch farm", 2, "hoe"); }

    public static ItemStack createStoneBricksPickaxe() { return createBlockTool("Stone Bricks", "Pickaxe", "⛏", Items.DIAMOND_PICKAXE, Formatting.GRAY, 500, "Fortress mining", 7, "pickaxe"); }
    public static ItemStack createStoneBricksAxe() { return createBlockTool("Stone Bricks", "Axe", "🪓", Items.DIAMOND_AXE, Formatting.GRAY, 500, "Fortress chop", 7, "axe"); }
    public static ItemStack createStoneBricksShovel() { return createBlockTool("Stone Bricks", "Shovel", "🪏", Items.DIAMOND_SHOVEL, Formatting.GRAY, 500, "Fortress dig", 6, "shovel"); }
    public static ItemStack createStoneBricksHoe() { return createBlockTool("Stone Bricks", "Hoe", "🌾", Items.DIAMOND_HOE, Formatting.GRAY, 500, "Fortress farm", 6, "hoe"); }

    public static ItemStack createCobblestonePickaxe() { return createBlockTool("Cobblestone", "Pickaxe", "⛏", Items.STONE_PICKAXE, Formatting.GRAY, 200, "Rock solid", 3, "pickaxe"); }
    public static ItemStack createCobblestoneAxe() { return createBlockTool("Cobblestone", "Axe", "🪓", Items.STONE_AXE, Formatting.GRAY, 200, "Rock chop", 3, "axe"); }
    public static ItemStack createCobblestoneShovel() { return createBlockTool("Cobblestone", "Shovel", "🪏", Items.STONE_SHOVEL, Formatting.GRAY, 200, "Rock dig", 2, "shovel"); }
    public static ItemStack createCobblestoneHoe() { return createBlockTool("Cobblestone", "Hoe", "🌾", Items.STONE_HOE, Formatting.GRAY, 200, "Rock farm", 2, "hoe"); }

    public static ItemStack createMossyCobblestonePickaxe() { return createBlockTool("Mossy Cobblestone", "Pickaxe", "⛏", Items.STONE_PICKAXE, Formatting.DARK_GREEN, 220, "Mossy mining", 3, "pickaxe"); }
    public static ItemStack createMossyCobblestoneAxe() { return createBlockTool("Mossy Cobblestone", "Axe", "🪓", Items.STONE_AXE, Formatting.DARK_GREEN, 220, "Mossy chop", 3, "axe"); }
    public static ItemStack createMossyCobblestoneShovel() { return createBlockTool("Mossy Cobblestone", "Shovel", "🪏", Items.STONE_SHOVEL, Formatting.DARK_GREEN, 220, "Mossy dig", 2, "shovel"); }
    public static ItemStack createMossyCobblestoneHoe() { return createBlockTool("Mossy Cobblestone", "Hoe", "🌾", Items.STONE_HOE, Formatting.DARK_GREEN, 220, "Mossy farm", 2, "hoe"); }

    public static ItemStack createCobbledDeepslatePickaxe() { return createBlockTool("Cobbled Deepslate", "Pickaxe", "⛏", Items.DIAMOND_PICKAXE, Formatting.DARK_GRAY, 400, "Deep mining", 6, "pickaxe"); }
    public static ItemStack createCobbledDeepslateAxe() { return createBlockTool("Cobbled Deepslate", "Axe", "🪓", Items.DIAMOND_AXE, Formatting.DARK_GRAY, 400, "Deep chop", 6, "axe"); }
    public static ItemStack createCobbledDeepslateShovel() { return createBlockTool("Cobbled Deepslate", "Shovel", "🪏", Items.DIAMOND_SHOVEL, Formatting.DARK_GRAY, 400, "Deep dig", 5, "shovel"); }
    public static ItemStack createCobbledDeepslateHoe() { return createBlockTool("Cobbled Deepslate", "Hoe", "🌾", Items.DIAMOND_HOE, Formatting.DARK_GRAY, 400, "Deep farm", 5, "hoe"); }

    public static ItemStack createMudBricksPickaxe() { return createBlockTool("Mud Bricks", "Pickaxe", "⛏", Items.IRON_PICKAXE, Formatting.RED, 250, "Mud mining", 4, "pickaxe"); }
    public static ItemStack createMudBricksAxe() { return createBlockTool("Mud Bricks", "Axe", "🪓", Items.IRON_AXE, Formatting.RED, 250, "Mud chop", 4, "axe"); }
    public static ItemStack createMudBricksShovel() { return createBlockTool("Mud Bricks", "Shovel", "🪏", Items.IRON_SHOVEL, Formatting.RED, 250, "Mud dig", 3, "shovel"); }
    public static ItemStack createMudBricksHoe() { return createBlockTool("Mud Bricks", "Hoe", "🌾", Items.IRON_HOE, Formatting.RED, 250, "Mud farm", 3, "hoe"); }

    public static ItemStack createMangroveRootsPickaxe() { return createBlockTool("Mangrove Roots", "Pickaxe", "⛏", Items.IRON_PICKAXE, Formatting.DARK_RED, 220, "Root mining", 4, "pickaxe"); }
    public static ItemStack createMangroveRootsAxe() { return createBlockTool("Mangrove Roots", "Axe", "🪓", Items.IRON_AXE, Formatting.DARK_RED, 220, "Root chop", 4, "axe"); }
    public static ItemStack createMangroveRootsShovel() { return createBlockTool("Mangrove Roots", "Shovel", "🪏", Items.IRON_SHOVEL, Formatting.DARK_RED, 220, "Root dig", 3, "shovel"); }
    public static ItemStack createMangroveRootsHoe() { return createBlockTool("Mangrove Roots", "Hoe", "🌾", Items.IRON_HOE, Formatting.DARK_RED, 220, "Root farm", 3, "hoe"); }

    public static ItemStack createMuddyMangroveRootsPickaxe() { return createBlockTool("Muddy Mangrove Roots", "Pickaxe", "⛏", Items.IRON_PICKAXE, Formatting.DARK_RED, 260, "Muddy mining", 5, "pickaxe"); }
    public static ItemStack createMuddyMangroveRootsAxe() { return createBlockTool("Muddy Mangrove Roots", "Axe", "🪓", Items.IRON_AXE, Formatting.DARK_RED, 260, "Muddy chop", 5, "axe"); }
    public static ItemStack createMuddyMangroveRootsShovel() { return createBlockTool("Muddy Mangrove Roots", "Shovel", "🪏", Items.IRON_SHOVEL, Formatting.DARK_RED, 260, "Muddy dig", 4, "shovel"); }
    public static ItemStack createMuddyMangroveRootsHoe() { return createBlockTool("Muddy Mangrove Roots", "Hoe", "🌾", Items.IRON_HOE, Formatting.DARK_RED, 260, "Muddy farm", 4, "hoe"); }

    // COPPER/NETHERITE/FROGLIGHT/BASALT TOOLS
    public static ItemStack createNetheriteBlockPickaxe() { return createBlockTool("Netherite Block", "Pickaxe", "⛏", Items.NETHERITE_PICKAXE, Formatting.DARK_PURPLE, 2000, "Ultimate mining", 8, "pickaxe"); }
    public static ItemStack createNetheriteBlockAxe() { return createBlockTool("Netherite Block", "Axe", "🪓", Items.NETHERITE_AXE, Formatting.DARK_PURPLE, 2000, "Ultimate chop", 8, "axe"); }
    public static ItemStack createNetheriteBlockShovel() { return createBlockTool("Netherite Block", "Shovel", "🪏", Items.NETHERITE_SHOVEL, Formatting.DARK_PURPLE, 2000, "Ultimate dig", 7, "shovel"); }
    public static ItemStack createNetheriteBlockHoe() { return createBlockTool("Netherite Block", "Hoe", "🌾", Items.NETHERITE_HOE, Formatting.DARK_PURPLE, 2000, "Ultimate farm", 7, "hoe"); }

    public static ItemStack createChiseledCopperPickaxe() { return createBlockTool("Chiseled Copper", "Pickaxe", "⛏", Items.IRON_PICKAXE, Formatting.GOLD, 220, "Copper mining", 4, "pickaxe"); }
    public static ItemStack createChiseledCopperAxe() { return createBlockTool("Chiseled Copper", "Axe", "🪓", Items.IRON_AXE, Formatting.GOLD, 220, "Copper chop", 4, "axe"); }
    public static ItemStack createChiseledCopperShovel() { return createBlockTool("Chiseled Copper", "Shovel", "🪏", Items.IRON_SHOVEL, Formatting.GOLD, 220, "Copper dig", 3, "shovel"); }
    public static ItemStack createChiseledCopperHoe() { return createBlockTool("Chiseled Copper", "Hoe", "🌾", Items.IRON_HOE, Formatting.GOLD, 220, "Copper farm", 3, "hoe"); }

    public static ItemStack createCutCopperPickaxe() { return createBlockTool("Cut Copper", "Pickaxe", "⛏", Items.IRON_PICKAXE, Formatting.GOLD, 200, "Clean mining", 4, "pickaxe"); }
    public static ItemStack createCutCopperAxe() { return createBlockTool("Cut Copper", "Axe", "🪓", Items.IRON_AXE, Formatting.GOLD, 200, "Clean chop", 4, "axe"); }
    public static ItemStack createCutCopperShovel() { return createBlockTool("Cut Copper", "Shovel", "🪏", Items.IRON_SHOVEL, Formatting.GOLD, 200, "Clean dig", 3, "shovel"); }
    public static ItemStack createCutCopperHoe() { return createBlockTool("Cut Copper", "Hoe", "🌾", Items.IRON_HOE, Formatting.GOLD, 200, "Clean farm", 3, "hoe"); }

    public static ItemStack createExposedCopperPickaxe() { return createBlockTool("Exposed Copper", "Pickaxe", "⛏", Items.IRON_PICKAXE, Formatting.GOLD, 190, "Weathered mining", 4, "pickaxe"); }
    public static ItemStack createExposedCopperAxe() { return createBlockTool("Exposed Copper", "Axe", "🪓", Items.IRON_AXE, Formatting.GOLD, 190, "Weathered chop", 4, "axe"); }
    public static ItemStack createExposedCopperShovel() { return createBlockTool("Exposed Copper", "Shovel", "🪏", Items.IRON_SHOVEL, Formatting.GOLD, 190, "Weathered dig", 3, "shovel"); }
    public static ItemStack createExposedCopperHoe() { return createBlockTool("Exposed Copper", "Hoe", "🌾", Items.IRON_HOE, Formatting.GOLD, 190, "Weathered farm", 3, "hoe"); }

    public static ItemStack createWeatheredCopperPickaxe() { return createBlockTool("Weathered Copper", "Pickaxe", "⛏", Items.IRON_PICKAXE, Formatting.GOLD, 180, "Aged mining", 4, "pickaxe"); }
    public static ItemStack createWeatheredCopperAxe() { return createBlockTool("Weathered Copper", "Axe", "🪓", Items.IRON_AXE, Formatting.GOLD, 180, "Aged chop", 4, "axe"); }
    public static ItemStack createWeatheredCopperShovel() { return createBlockTool("Weathered Copper", "Shovel", "🪏", Items.IRON_SHOVEL, Formatting.GOLD, 180, "Aged dig", 3, "shovel"); }
    public static ItemStack createWeatheredCopperHoe() { return createBlockTool("Weathered Copper", "Hoe", "🌾", Items.IRON_HOE, Formatting.GOLD, 180, "Aged farm", 3, "hoe"); }

    public static ItemStack createOxidisedCopperPickaxe() { return createBlockTool("Oxidised Copper", "Pickaxe", "⛏", Items.IRON_PICKAXE, Formatting.AQUA, 170, "Patina mining", 4, "pickaxe"); }
    public static ItemStack createOxidisedCopperAxe() { return createBlockTool("Oxidised Copper", "Axe", "🪓", Items.IRON_AXE, Formatting.AQUA, 170, "Patina chop", 4, "axe"); }
    public static ItemStack createOxidisedCopperShovel() { return createBlockTool("Oxidised Copper", "Shovel", "🪏", Items.IRON_SHOVEL, Formatting.AQUA, 170, "Patina dig", 3, "shovel"); }
    public static ItemStack createOxidisedCopperHoe() { return createBlockTool("Oxidised Copper", "Hoe", "🌾", Items.IRON_HOE, Formatting.AQUA, 170, "Patina farm", 3, "hoe"); }

    public static ItemStack createWaxedCutCopperPickaxe() { return createBlockTool("Waxed Cut Copper", "Pickaxe", "⛏", Items.IRON_PICKAXE, Formatting.GOLD, 210, "Preserved mining", 4, "pickaxe"); }
    public static ItemStack createWaxedCutCopperAxe() { return createBlockTool("Waxed Cut Copper", "Axe", "🪓", Items.IRON_AXE, Formatting.GOLD, 210, "Preserved chop", 4, "axe"); }
    public static ItemStack createWaxedCutCopperShovel() { return createBlockTool("Waxed Cut Copper", "Shovel", "🪏", Items.IRON_SHOVEL, Formatting.GOLD, 210, "Preserved dig", 3, "shovel"); }
    public static ItemStack createWaxedCutCopperHoe() { return createBlockTool("Waxed Cut Copper", "Hoe", "🌾", Items.IRON_HOE, Formatting.GOLD, 210, "Preserved farm", 3, "hoe"); }

    public static ItemStack createPolishedBasaltPickaxe() { return createBlockTool("Polished Basalt", "Pickaxe", "⛏", Items.DIAMOND_PICKAXE, Formatting.DARK_GRAY, 450, "Volcanic mining", 7, "pickaxe"); }
    public static ItemStack createPolishedBasaltAxe() { return createBlockTool("Polished Basalt", "Axe", "🪓", Items.DIAMOND_AXE, Formatting.DARK_GRAY, 450, "Volcanic chop", 7, "axe"); }
    public static ItemStack createPolishedBasaltShovel() { return createBlockTool("Polished Basalt", "Shovel", "🪏", Items.DIAMOND_SHOVEL, Formatting.DARK_GRAY, 450, "Volcanic dig", 6, "shovel"); }
    public static ItemStack createPolishedBasaltHoe() { return createBlockTool("Polished Basalt", "Hoe", "🌾", Items.DIAMOND_HOE, Formatting.DARK_GRAY, 450, "Volcanic farm", 6, "hoe"); }

    public static ItemStack createVerdantFroglightPickaxe() { return createBlockTool("Verdant Froglight", "Pickaxe", "⛏", Items.DIAMOND_PICKAXE, Formatting.GREEN, 480, "Frog mining", 7, "pickaxe"); }
    public static ItemStack createVerdantFroglightAxe() { return createBlockTool("Verdant Froglight", "Axe", "🪓", Items.DIAMOND_AXE, Formatting.GREEN, 480, "Frog chop", 7, "axe"); }
    public static ItemStack createVerdantFroglightShovel() { return createBlockTool("Verdant Froglight", "Shovel", "🪏", Items.DIAMOND_SHOVEL, Formatting.GREEN, 480, "Frog dig", 6, "shovel"); }
    public static ItemStack createVerdantFroglightHoe() { return createBlockTool("Verdant Froglight", "Hoe", "🌾", Items.DIAMOND_HOE, Formatting.GREEN, 480, "Frog farm", 6, "hoe"); }

    public static ItemStack createPearlescentFroglightPickaxe() { return createBlockTool("Pearlescent Froglight", "Pickaxe", "⛏", Items.DIAMOND_PICKAXE, Formatting.LIGHT_PURPLE, 500, "Pearl mining", 7, "pickaxe"); }
    public static ItemStack createPearlescentFroglightAxe() { return createBlockTool("Pearlescent Froglight", "Axe", "🪓", Items.DIAMOND_AXE, Formatting.LIGHT_PURPLE, 500, "Pearl chop", 7, "axe"); }
    public static ItemStack createPearlescentFroglightShovel() { return createBlockTool("Pearlescent Froglight", "Shovel", "🪏", Items.DIAMOND_SHOVEL, Formatting.LIGHT_PURPLE, 500, "Pearl dig", 6, "shovel"); }
    public static ItemStack createPearlescentFroglightHoe() { return createBlockTool("Pearlescent Froglight", "Hoe", "🌾", Items.DIAMOND_HOE, Formatting.LIGHT_PURPLE, 500, "Pearl farm", 6, "hoe"); }

    public static ItemStack createOchreFroglightPickaxe() { return createBlockTool("Ochre Froglight", "Pickaxe", "⛏", Items.DIAMOND_PICKAXE, Formatting.YELLOW, 460, "Ochre mining", 7, "pickaxe"); }
    public static ItemStack createOchreFroglightAxe() { return createBlockTool("Ochre Froglight", "Axe", "🪓", Items.DIAMOND_AXE, Formatting.YELLOW, 460, "Ochre chop", 7, "axe"); }
    public static ItemStack createOchreFroglightShovel() { return createBlockTool("Ochre Froglight", "Shovel", "🪏", Items.DIAMOND_SHOVEL, Formatting.YELLOW, 460, "Ochre dig", 6, "shovel"); }
    public static ItemStack createOchreFroglightHoe() { return createBlockTool("Ochre Froglight", "Hoe", "🌾", Items.DIAMOND_HOE, Formatting.YELLOW, 460, "Ochre farm", 6, "hoe"); }

    // MATERIAL ITEM TOOLS
    public static ItemStack createIronNuggetPickaxe() { return createBlockTool("Iron Nugget", "Pickaxe", "⛏", Items.STONE_PICKAXE, Formatting.WHITE, 100, "Nugget mining", 2, "pickaxe"); }
    public static ItemStack createIronNuggetAxe() { return createBlockTool("Iron Nugget", "Axe", "🪓", Items.STONE_AXE, Formatting.WHITE, 100, "Nugget chop", 2, "axe"); }
    public static ItemStack createIronNuggetShovel() { return createBlockTool("Iron Nugget", "Shovel", "🪏", Items.STONE_SHOVEL, Formatting.WHITE, 100, "Nugget dig", 1, "shovel"); }
    public static ItemStack createIronNuggetHoe() { return createBlockTool("Iron Nugget", "Hoe", "🌾", Items.STONE_HOE, Formatting.WHITE, 100, "Nugget farm", 1, "hoe"); }

    public static ItemStack createGoldNuggetPickaxe() { return createBlockTool("Gold Nugget", "Pickaxe", "⛏", Items.GOLDEN_PICKAXE, Formatting.GOLD, 50, "Golden mining", 1, "pickaxe"); }
    public static ItemStack createGoldNuggetAxe() { return createBlockTool("Gold Nugget", "Axe", "🪓", Items.GOLDEN_AXE, Formatting.GOLD, 50, "Golden chop", 1, "axe"); }
    public static ItemStack createGoldNuggetShovel() { return createBlockTool("Gold Nugget", "Shovel", "🪏", Items.GOLDEN_SHOVEL, Formatting.GOLD, 50, "Golden dig", 1, "shovel"); }
    public static ItemStack createGoldNuggetHoe() { return createBlockTool("Gold Nugget", "Hoe", "🌾", Items.GOLDEN_HOE, Formatting.GOLD, 50, "Golden farm", 1, "hoe"); }

    public static ItemStack createCopperIngotPickaxe() { return createBlockTool("Copper Ingot", "Pickaxe", "⛏", Items.IRON_PICKAXE, Formatting.GOLD, 150, "Conductive mining", 3, "pickaxe"); }
    public static ItemStack createCopperIngotAxe() { return createBlockTool("Copper Ingot", "Axe", "🪓", Items.IRON_AXE, Formatting.GOLD, 150, "Conductive chop", 3, "axe"); }
    public static ItemStack createCopperIngotShovel() { return createBlockTool("Copper Ingot", "Shovel", "🪏", Items.IRON_SHOVEL, Formatting.GOLD, 150, "Conductive dig", 2, "shovel"); }
    public static ItemStack createCopperIngotHoe() { return createBlockTool("Copper Ingot", "Hoe", "🌾", Items.IRON_HOE, Formatting.GOLD, 150, "Conductive farm", 2, "hoe"); }

    public static ItemStack createEmeraldPickaxe() { return createBlockTool("Emerald", "Pickaxe", "⛏", Items.DIAMOND_PICKAXE, Formatting.GREEN, 400, "Fortune mining", 6, "pickaxe"); }
    public static ItemStack createEmeraldAxe() { return createBlockTool("Emerald", "Axe", "🪓", Items.DIAMOND_AXE, Formatting.GREEN, 400, "Fortune chop", 6, "axe"); }
    public static ItemStack createEmeraldShovel() { return createBlockTool("Emerald", "Shovel", "🪏", Items.DIAMOND_SHOVEL, Formatting.GREEN, 400, "Fortune dig", 5, "shovel"); }
    public static ItemStack createEmeraldHoe() { return createBlockTool("Emerald", "Hoe", "🌾", Items.DIAMOND_HOE, Formatting.GREEN, 400, "Fortune farm", 5, "hoe"); }

    public static ItemStack createLapisLazuliPickaxe() { return createBlockTool("Lapis Lazuli", "Pickaxe", "⛏", Items.DIAMOND_PICKAXE, Formatting.BLUE, 300, "XP mining", 5, "pickaxe"); }
    public static ItemStack createLapisLazuliAxe() { return createBlockTool("Lapis Lazuli", "Axe", "🪓", Items.DIAMOND_AXE, Formatting.BLUE, 300, "XP chop", 5, "axe"); }
    public static ItemStack createLapisLazuliShovel() { return createBlockTool("Lapis Lazuli", "Shovel", "🪏", Items.DIAMOND_SHOVEL, Formatting.BLUE, 300, "XP dig", 4, "shovel"); }
    public static ItemStack createLapisLazuliHoe() { return createBlockTool("Lapis Lazuli", "Hoe", "🌾", Items.DIAMOND_HOE, Formatting.BLUE, 300, "XP farm", 4, "hoe"); }

    public static ItemStack createAmethystShardPickaxe() { return createBlockTool("Amethyst Shard", "Pickaxe", "⛏", Items.DIAMOND_PICKAXE, Formatting.LIGHT_PURPLE, 380, "Crystal mining", 6, "pickaxe"); }
    public static ItemStack createAmethystShardAxe() { return createBlockTool("Amethyst Shard", "Axe", "🪓", Items.DIAMOND_AXE, Formatting.LIGHT_PURPLE, 380, "Crystal chop", 6, "axe"); }
    public static ItemStack createAmethystShardShovel() { return createBlockTool("Amethyst Shard", "Shovel", "🪏", Items.DIAMOND_SHOVEL, Formatting.LIGHT_PURPLE, 380, "Crystal dig", 5, "shovel"); }
    public static ItemStack createAmethystShardHoe() { return createBlockTool("Amethyst Shard", "Hoe", "🌾", Items.DIAMOND_HOE, Formatting.LIGHT_PURPLE, 380, "Crystal farm", 5, "hoe"); }

    public static ItemStack createFlintPickaxe() { return createBlockTool("Flint", "Pickaxe", "⛏", Items.STONE_PICKAXE, Formatting.GRAY, 120, "Sharp mining", 2, "pickaxe"); }
    public static ItemStack createFlintAxe() { return createBlockTool("Flint", "Axe", "🪓", Items.STONE_AXE, Formatting.GRAY, 120, "Sharp chop", 2, "axe"); }
    public static ItemStack createFlintShovel() { return createBlockTool("Flint", "Shovel", "🪏", Items.STONE_SHOVEL, Formatting.GRAY, 120, "Sharp dig", 1, "shovel"); }
    public static ItemStack createFlintHoe() { return createBlockTool("Flint", "Hoe", "🌾", Items.STONE_HOE, Formatting.GRAY, 120, "Sharp farm", 1, "hoe"); }

    public static ItemStack createBoneMealPickaxe() { return createBlockTool("Bone Meal", "Pickaxe", "⛏", Items.STONE_PICKAXE, Formatting.WHITE, 80, "Growth mining", 1, "pickaxe"); }
    public static ItemStack createBoneMealAxe() { return createBlockTool("Bone Meal", "Axe", "🪓", Items.STONE_AXE, Formatting.WHITE, 80, "Growth chop", 1, "axe"); }
    public static ItemStack createBoneMealShovel() { return createBlockTool("Bone Meal", "Shovel", "🪏", Items.STONE_SHOVEL, Formatting.WHITE, 80, "Growth dig", 1, "shovel"); }
    public static ItemStack createBoneMealHoe() { return createBlockTool("Bone Meal", "Hoe", "🌾", Items.STONE_HOE, Formatting.WHITE, 80, "Growth farm", 1, "hoe"); }

    public static ItemStack createCharcoalPickaxe() { return createBlockTool("Charcoal", "Pickaxe", "⛏", Items.STONE_PICKAXE, Formatting.DARK_GRAY, 130, "Ash mining", 2, "pickaxe"); }
    public static ItemStack createCharcoalAxe() { return createBlockTool("Charcoal", "Axe", "🪓", Items.STONE_AXE, Formatting.DARK_GRAY, 130, "Ash chop", 2, "axe"); }
    public static ItemStack createCharcoalShovel() { return createBlockTool("Charcoal", "Shovel", "🪏", Items.STONE_SHOVEL, Formatting.DARK_GRAY, 130, "Ash dig", 1, "shovel"); }
    public static ItemStack createCharcoalHoe() { return createBlockTool("Charcoal", "Hoe", "🌾", Items.STONE_HOE, Formatting.DARK_GRAY, 130, "Ash farm", 1, "hoe"); }

    public static ItemStack createEndStonePickaxe() { return createBlockTool("End Stone", "Pickaxe", "⛏", Items.DIAMOND_PICKAXE, Formatting.YELLOW, 600, "End mining", 8, "pickaxe"); }
    public static ItemStack createEndStoneAxe() { return createBlockTool("End Stone", "Axe", "🪓", Items.DIAMOND_AXE, Formatting.YELLOW, 600, "End chop", 8, "axe"); }
    public static ItemStack createEndStoneShovel() { return createBlockTool("End Stone", "Shovel", "🪏", Items.DIAMOND_SHOVEL, Formatting.YELLOW, 600, "End dig", 7, "shovel"); }
    public static ItemStack createEndStoneHoe() { return createBlockTool("End Stone", "Hoe", "🌾", Items.DIAMOND_HOE, Formatting.YELLOW, 600, "End farm", 7, "hoe"); }

    public static ItemStack createSnowBlockPickaxe() { return createBlockTool("Snow Block", "Pickaxe", "⛏", Items.IRON_PICKAXE, Formatting.WHITE, 180, "Winter mining", 3, "pickaxe"); }
    public static ItemStack createSnowBlockAxe() { return createBlockTool("Snow Block", "Axe", "🪓", Items.IRON_AXE, Formatting.WHITE, 180, "Winter chop", 3, "axe"); }
    public static ItemStack createSnowBlockShovel() { return createBlockTool("Snow Block", "Shovel", "🪏", Items.IRON_SHOVEL, Formatting.WHITE, 180, "Winter dig", 2, "shovel"); }
    public static ItemStack createSnowBlockHoe() { return createBlockTool("Snow Block", "Hoe", "🌾", Items.IRON_HOE, Formatting.WHITE, 180, "Winter farm", 2, "hoe"); }

    // ============================================================
    // HELPER METHOD for creating block tools
    // ============================================================
    
    private static ItemStack createBlockTool(String material, String toolType, String emoji, 
            net.minecraft.item.Item baseItem, Formatting color, int durability, 
            String ability, int damage, String idSuffix) {
        ItemStack tool = new ItemStack(baseItem);
        String typeEmoji = switch(toolType) {
            case "Pickaxe" -> "⛏ ";
            case "Axe" -> "🪓 ";
            case "Shovel" -> "🪏 ";
            case "Hoe" -> "🌾 ";
            default -> "";
        };
        tool.set(DataComponentTypes.CUSTOM_NAME, Text.literal(typeEmoji + material + " " + toolType).formatted(color, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(color));
        lore.add(Text.literal(typeEmoji + "Damage: " + damage).formatted(Formatting.RED));
        lore.add(Text.literal(typeEmoji + "Durability: " + durability).formatted(Formatting.YELLOW));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ ABILITY:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• " + ability).formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8" + material.toLowerCase() + " edge").formatted(Formatting.DARK_GRAY));
        tool.set(DataComponentTypes.LORE, new LoreComponent(lore));
        tool.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        tool.set(DataComponentTypes.MAX_DAMAGE, durability);
        SlayerItems.setCustomItemId(tool, (material.toLowerCase().replace(" ", "_") + "_" + idSuffix).replace("__", "_"));
        return tool;
    }
}
