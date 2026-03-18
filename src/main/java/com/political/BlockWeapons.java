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
 * Block-based weapons that match BlockArmor sets.
 * Each weapon has stats and abilities themed after its block material.
 */
public class BlockWeapons {

    // ============================================================
    // WEAPON CREATION METHODS - Each matches a BlockArmor set
    // ============================================================

    public static ItemStack createGlassSword() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⚔ Glass Blade").formatted(Formatting.AQUA, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.AQUA));
        lore.add(Text.literal("⚔ Damage: 6").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Durability: 50").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚔ Attack Speed: Fast").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ SET BONUS (Full Glass Armor):").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• 50% damage reduction").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Breaks on fatal blow (saves you)").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Sharp but fragile").formatted(Formatting.DARK_GRAY));
        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponentTypes.MAX_DAMAGE, 50);
        SlayerItems.setCustomItemId(sword, "glass_sword");
        return sword;
    }

    public static ItemStack createObsidianSword() {
        ItemStack sword = new ItemStack(Items.NETHERITE_SWORD);
        sword.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⚔ Obsidian Blade").formatted(Formatting.DARK_PURPLE, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_PURPLE));
        lore.add(Text.literal("⚔ Damage: 10").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Durability: 2000").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚔ Attack Speed: Slow").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Knockback Resistance").formatted(Formatting.BLUE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ SET BONUS (Full Obsidian Armor):").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• +4 Armor Toughness").formatted(Formatting.GREEN));
        lore.add(Text.literal("• 30% explosion resistance").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Knockback immunity").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Unbreakable edge").formatted(Formatting.DARK_GRAY));
        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponentTypes.MAX_DAMAGE, 2000);
        SlayerItems.setCustomItemId(sword, "obsidian_sword");
        return sword;
    }

    public static ItemStack createQuartzSword() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⚔ Quartz Blade").formatted(Formatting.WHITE, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.WHITE));
        lore.add(Text.literal("⚔ Damage: 7").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Durability: 300").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚔ Critical Chance: +15%").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ SET BONUS (Full Quartz Armor):").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• +10% Critical Hit Chance").formatted(Formatting.GREEN));
        lore.add(Text.literal("• +15% Attack Speed").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Glowing effect on enemies hit").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Crystalline sharpness").formatted(Formatting.DARK_GRAY));
        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponentTypes.MAX_DAMAGE, 300);
        SlayerItems.setCustomItemId(sword, "quartz_sword");
        return sword;
    }

    public static ItemStack createGlowstoneSword() {
        ItemStack sword = new ItemStack(Items.GOLDEN_SWORD);
        sword.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⚔ Glowstone Blade").formatted(Formatting.YELLOW, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚔ Damage: 6").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Durability: 200").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚔ Light Level: 15 (when held)").formatted(Formatting.GOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ SET BONUS (Full Glowstone Armor):").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Permanent Night Vision").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Light aura (lights up area)").formatted(Formatting.GREEN));
        lore.add(Text.literal("• +20% damage to undead in dark").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Radiant edge").formatted(Formatting.DARK_GRAY));
        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponentTypes.MAX_DAMAGE, 200);
        SlayerItems.setCustomItemId(sword, "glowstone_sword");
        return sword;
    }

    public static ItemStack createRedstoneSword() {
        ItemStack sword = new ItemStack(Items.IRON_SWORD);
        sword.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⚔ Redstone Blade").formatted(Formatting.RED, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Damage: 7").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Durability: 250").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚔ Electric Charge: Stuns enemies").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ SET BONUS (Full Redstone Armor):").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Speed boost near redstone").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Lightning on critical hits").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Redstone awareness (see nearby ore)").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Conductive edge").formatted(Formatting.DARK_GRAY));
        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponentTypes.MAX_DAMAGE, 250);
        SlayerItems.setCustomItemId(sword, "redstone_sword");
        return sword;
    }

    public static ItemStack createNetherrackSword() {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⚔ Netherrack Blade").formatted(Formatting.DARK_RED, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_RED));
        lore.add(Text.literal("⚔ Damage: 6").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Durability: 180").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚔ Fire Aspect I (built-in)").formatted(Formatting.GOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ SET BONUS (Full Netherrack Armor):").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Fire Resistance").formatted(Formatting.GREEN));
        lore.add(Text.literal("• +25% damage in Nether").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Lava resistance (slower damage)").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Infernal edge").formatted(Formatting.DARK_GRAY));
        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponentTypes.MAX_DAMAGE, 180);
        SlayerItems.setCustomItemId(sword, "netherrack_sword");
        return sword;
    }

    public static ItemStack createEndstoneSword() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⚔ Endstone Blade").formatted(Formatting.DARK_GRAY, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("⚔ Damage: 8").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Durability: 350").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚔ Ender Resistance: Endermen can't teleport from hit").formatted(Formatting.DARK_PURPLE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ SET BONUS (Full End Stone Armor):").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Endermen are neutral").formatted(Formatting.GREEN));
        lore.add(Text.literal("• No ender pearl damage").formatted(Formatting.GREEN));
        lore.add(Text.literal("• +20% damage to end mobs").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Void-touched edge").formatted(Formatting.DARK_GRAY));
        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponentTypes.MAX_DAMAGE, 350);
        SlayerItems.setCustomItemId(sword, "endstone_sword");
        return sword;
    }

    public static ItemStack createIceSword() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⚔ Ice Blade").formatted(Formatting.AQUA, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.AQUA));
        lore.add(Text.literal("⚔ Damage: 7").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Durability: 120").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚔ Slowness II on hit (3 seconds)").formatted(Formatting.BLUE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ SET BONUS (Full Ice Armor):").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Frost Walker effect").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Immune to fire (temporary)").formatted(Formatting.GREEN));
        lore.add(Text.literal("• +30% damage in cold biomes").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Frozen edge").formatted(Formatting.DARK_GRAY));
        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponentTypes.MAX_DAMAGE, 120);
        SlayerItems.setCustomItemId(sword, "ice_sword");
        return sword;
    }

    public static ItemStack createPrismarineSword() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⚔ Prismarine Blade").formatted(Formatting.DARK_AQUA, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_AQUA));
        lore.add(Text.literal("⚔ Damage: 7").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Durability: 280").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚔ Water Breathing (when held)").formatted(Formatting.BLUE));
        lore.add(Text.literal("⚔ +30% damage underwater").formatted(Formatting.BLUE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ SET BONUS (Full Prismarine Armor):").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Permanent Water Breathing").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Dolphin's Grace in water").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Guardian beam immunity").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Ocean's edge").formatted(Formatting.DARK_GRAY));
        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponentTypes.MAX_DAMAGE, 280);
        SlayerItems.setCustomItemId(sword, "prismarine_sword");
        return sword;
    }

    public static ItemStack createTerracottaSword() {
        ItemStack sword = new ItemStack(Items.IRON_SWORD);
        sword.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⚔ Terracotta Blade").formatted(Formatting.GOLD, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("⚔ Damage: 6").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Durability: 400").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚔ Armor Piercing: +2").formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ SET BONUS (Full Terracotta Armor):").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• +3 Armor (tough clay skin)").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Arrow immunity (25% chance)").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Desert heat resistance").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Earthen edge").formatted(Formatting.DARK_GRAY));
        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponentTypes.MAX_DAMAGE, 400);
        SlayerItems.setCustomItemId(sword, "terracotta_sword");
        return sword;
    }

    public static ItemStack createMossySword() {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⚔ Mossy Blade").formatted(Formatting.DARK_GREEN, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_GREEN));
        lore.add(Text.literal("⚔ Damage: 6").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Durability: 450").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚔ Lifesteal: 10% of damage").formatted(Formatting.RED));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ SET BONUS (Full Mossy Armor):").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Regeneration I (constant)").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Poison immunity").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Bonemeal effect on walk (grows plants)").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Overgrown edge").formatted(Formatting.DARK_GRAY));
        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponentTypes.MAX_DAMAGE, 450);
        SlayerItems.setCustomItemId(sword, "mossy_sword");
        return sword;
    }

    public static ItemStack createSoulSandSword() {
        ItemStack sword = new ItemStack(Items.GOLDEN_SWORD);
        sword.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⚔ Soul Blade").formatted(Formatting.DARK_GRAY, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("⚔ Damage: 5").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Durability: 220").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚔ Soul Speed when held").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal("⚔ Soul Harvest: +XP from kills").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ SET BONUS (Full Soul Sand Armor):").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Soul Speed II on soul sand").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Piglins ignore you").formatted(Formatting.GREEN));
        lore.add(Text.literal("• +50% XP from all sources").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Soul-touched edge").formatted(Formatting.DARK_GRAY));
        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponentTypes.MAX_DAMAGE, 220);
        SlayerItems.setCustomItemId(sword, "soulsand_sword");
        return sword;
    }

    public static ItemStack createMagmaSword() {
        ItemStack sword = new ItemStack(Items.GOLDEN_SWORD);
        sword.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⚔ Magma Blade").formatted(Formatting.RED, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Damage: 7").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Durability: 350").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚔ Fire Aspect II (built-in)").formatted(Formatting.GOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ SET BONUS (Full Magma Armor):").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Fire Resistance").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Lava Walker (walk on lava)").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Burn attackers (thorns-fire)").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Burning edge").formatted(Formatting.DARK_GRAY));
        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponentTypes.MAX_DAMAGE, 350);
        SlayerItems.setCustomItemId(sword, "magma_sword");
        return sword;
    }

    public static ItemStack createSandstoneSword() {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⚔ Sandstone Blade").formatted(Formatting.YELLOW, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚔ Damage: 6").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Durability: 280").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚔ No Fall Damage").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ SET BONUS (Full Sandstone Armor):").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• No fall damage").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Desert walker (speed in sand)").formatted(Formatting.GREEN));
        lore.add(Text.literal("• +25% damage in desert biomes").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Desert's edge").formatted(Formatting.DARK_GRAY));
        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponentTypes.MAX_DAMAGE, 280);
        SlayerItems.setCustomItemId(sword, "sandstone_sword");
        return sword;
    }

    public static ItemStack createAmethystSword() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⚔ Amethyst Blade").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal("⚔ Damage: 7").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Durability: 320").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚔ Magic Resonance: +2 Magic Damage").formatted(Formatting.DARK_PURPLE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ SET BONUS (Full Amethyst Armor):").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Magic damage reduction").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Reflect 20% magic damage").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Chime sound on block (warns of danger)").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Crystalline edge").formatted(Formatting.DARK_GRAY));
        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponentTypes.MAX_DAMAGE, 320);
        SlayerItems.setCustomItemId(sword, "amethyst_sword");
        return sword;
    }

    public static ItemStack createCoalSword() {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("⚔ Coal Blade").formatted(Formatting.DARK_GRAY, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("⚔ Damage: 5").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Durability: 200").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚔ Blindness on hit (2 seconds)").formatted(Formatting.BLACK));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ SET BONUS (Full Coal Armor):").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Night Vision").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Smoke screen when hit (blind attackers)").formatted(Formatting.GREEN));
        lore.add(Text.literal("• +20% damage in darkness").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Shadowed edge").formatted(Formatting.DARK_GRAY));
        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponentTypes.MAX_DAMAGE, 200);
        SlayerItems.setCustomItemId(sword, "coal_sword");
        return sword;
    }
}
