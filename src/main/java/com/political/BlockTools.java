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
}
