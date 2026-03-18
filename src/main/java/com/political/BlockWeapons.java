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

    // ============================================================
    // 36+ NEW BLOCK SWORDS - Matching BlockArmor sets
    // ============================================================

    public static ItemStack createDiamondBlockSword() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.set(DataComponentTypes.CUSTOM_NAME, Text.literal("⚔ Diamond Block Blade").formatted(Formatting.AQUA, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.AQUA));
        lore.add(Text.literal("⚔ Damage: 9").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Durability: 3000").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚔ Sharpness V effect").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ SET BONUS (Full Diamond Block Armor):").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• 20% damage reduction").formatted(Formatting.GREEN));
        lore.add(Text.literal("• High durability").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Ultimate luxury").formatted(Formatting.DARK_GRAY));
        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponentTypes.MAX_DAMAGE, 3000);
        SlayerItems.setCustomItemId(sword, "diamond_block_sword");
        return sword;
    }

    public static ItemStack createEmeraldBlockSword() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.set(DataComponentTypes.CUSTOM_NAME, Text.literal("⚔ Emerald Block Blade").formatted(Formatting.GREEN, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.GREEN));
        lore.add(Text.literal("⚔ Damage: 7").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Durability: 400").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚔ Looting II effect").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ SET BONUS (Full Emerald Block Armor):").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Better villager trades").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Emerald detection nearby").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8The merchant's blade").formatted(Formatting.DARK_GRAY));
        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponentTypes.MAX_DAMAGE, 400);
        SlayerItems.setCustomItemId(sword, "emerald_block_sword");
        return sword;
    }

    public static ItemStack createGoldBlockSword() {
        ItemStack sword = new ItemStack(Items.GOLDEN_SWORD);
        sword.set(DataComponentTypes.CUSTOM_NAME, Text.literal("⚔ Gold Block Blade").formatted(Formatting.YELLOW, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚔ Damage: 5").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Durability: 150").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚔ Fortune II on kills").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ SET BONUS (Full Gold Block Armor):").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Piglins love you").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Fortune on kills").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Gilded glory").formatted(Formatting.DARK_GRAY));
        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponentTypes.MAX_DAMAGE, 150);
        SlayerItems.setCustomItemId(sword, "gold_block_sword");
        return sword;
    }

    public static ItemStack createIronBlockSword() {
        ItemStack sword = new ItemStack(Items.IRON_SWORD);
        sword.set(DataComponentTypes.CUSTOM_NAME, Text.literal("⚔ Iron Block Blade").formatted(Formatting.WHITE, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.WHITE));
        lore.add(Text.literal("⚔ Damage: 7").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Durability: 600").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚔ Magnetic pull on hit").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ SET BONUS (Full Iron Block Armor):").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Magnetic item pickup").formatted(Formatting.GREEN));
        lore.add(Text.literal("• 15% knockback resistance").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Industrial grade").formatted(Formatting.DARK_GRAY));
        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponentTypes.MAX_DAMAGE, 600);
        SlayerItems.setCustomItemId(sword, "iron_block_sword");
        return sword;
    }

    public static ItemStack createLapisBlockSword() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.set(DataComponentTypes.CUSTOM_NAME, Text.literal("⚔ Lapis Block Blade").formatted(Formatting.BLUE, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.BLUE));
        lore.add(Text.literal("⚔ Damage: 6").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Durability: 350").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚔ XP bonus on kill").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ SET BONUS (Full Lapis Block Armor):").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• XP bonus +25%").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Enchantment boost").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Knowledge edge").formatted(Formatting.DARK_GRAY));
        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponentTypes.MAX_DAMAGE, 350);
        SlayerItems.setCustomItemId(sword, "lapis_block_sword");
        return sword;
    }

    public static ItemStack createCopperBlockSword() {
        ItemStack sword = new ItemStack(Items.IRON_SWORD);
        sword.set(DataComponentTypes.CUSTOM_NAME, Text.literal("⚔ Copper Block Blade").formatted(Formatting.GOLD, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("⚔ Damage: 6").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Durability: 280").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚔ Lightning charged strikes").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ SET BONUS (Full Copper Block Armor):").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Lightning rod").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Charged attacks").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Conductive edge").formatted(Formatting.DARK_GRAY));
        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponentTypes.MAX_DAMAGE, 280);
        SlayerItems.setCustomItemId(sword, "copper_block_sword");
        return sword;
    }

    public static ItemStack createAncientDebrisSword() {
        ItemStack sword = new ItemStack(Items.NETHERITE_SWORD);
        sword.set(DataComponentTypes.CUSTOM_NAME, Text.literal("⚔ Ancient Debris Blade").formatted(Formatting.DARK_GRAY, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("⚔ Damage: 10").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Durability: 2500").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚔ Fire Aspect II").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ SET BONUS (Full Ancient Debris Armor):").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Fire immunity").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Blast protection").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Ancient edge").formatted(Formatting.DARK_GRAY));
        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponentTypes.MAX_DAMAGE, 2500);
        SlayerItems.setCustomItemId(sword, "ancient_debris_sword");
        return sword;
    }

    public static ItemStack createBasaltSword() {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.set(DataComponentTypes.CUSTOM_NAME, Text.literal("⚔ Basalt Blade").formatted(Formatting.GRAY, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.GRAY));
        lore.add(Text.literal("⚔ Damage: 7").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Durability: 500").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚔ Lava walker strikes").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ SET BONUS (Full Basalt Armor):").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Lava resistance").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Knockback immunity").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Volcanic edge").formatted(Formatting.DARK_GRAY));
        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponentTypes.MAX_DAMAGE, 500);
        SlayerItems.setCustomItemId(sword, "basalt_sword");
        return sword;
    }

    public static ItemStack createBlackstoneSword() {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.set(DataComponentTypes.CUSTOM_NAME, Text.literal("⚔ Blackstone Blade").formatted(Formatting.DARK_GRAY, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("⚔ Damage: 6").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Durability: 450").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚔ Dark strikes").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ SET BONUS (Full Blackstone Armor):").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Piglin neutral").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Darkvision").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Dark fortress edge").formatted(Formatting.DARK_GRAY));
        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponentTypes.MAX_DAMAGE, 450);
        SlayerItems.setCustomItemId(sword, "blackstone_sword");
        return sword;
    }

    public static ItemStack createBoneBlockSword() {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.set(DataComponentTypes.CUSTOM_NAME, Text.literal("⚔ Bone Block Blade").formatted(Formatting.WHITE, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.WHITE));
        lore.add(Text.literal("⚔ Damage: 5").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Durability: 200").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚔ Undead bane").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ SET BONUS (Full Bone Block Armor):").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Undead ignore you").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Bone meal bonus").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Skeleton's edge").formatted(Formatting.DARK_GRAY));
        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponentTypes.MAX_DAMAGE, 200);
        SlayerItems.setCustomItemId(sword, "bone_block_sword");
        return sword;
    }

    public static ItemStack createBrickSword() {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.set(DataComponentTypes.CUSTOM_NAME, Text.literal("⚔ Brick Blade").formatted(Formatting.RED, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Damage: 6").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Durability: 380").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚔ Heavy knockback").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ SET BONUS (Full Brick Armor):").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Build faster").formatted(Formatting.GREEN));
        lore.add(Text.literal("• No fall damage").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Builder's edge").formatted(Formatting.DARK_GRAY));
        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponentTypes.MAX_DAMAGE, 380);
        SlayerItems.setCustomItemId(sword, "brick_sword");
        return sword;
    }

    public static ItemStack createCactusSword() {
        ItemStack sword = new ItemStack(Items.WOODEN_SWORD);
        sword.set(DataComponentTypes.CUSTOM_NAME, Text.literal("⚔ Cactus Blade").formatted(Formatting.DARK_GREEN, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_GREEN));
        lore.add(Text.literal("⚔ Damage: 4").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Durability: 180").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚔ Thorns on hit").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ SET BONUS (Full Cactus Armor):").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Thorns III").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Desert immunity").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Prickly edge").formatted(Formatting.DARK_GRAY));
        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponentTypes.MAX_DAMAGE, 180);
        SlayerItems.setCustomItemId(sword, "cactus_sword");
        return sword;
    }

    public static ItemStack createCalciteSword() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.set(DataComponentTypes.CUSTOM_NAME, Text.literal("⚔ Calcite Blade").formatted(Formatting.WHITE, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.WHITE));
        lore.add(Text.literal("⚔ Damage: 7").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Durability: 340").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚔ Purifying strikes").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ SET BONUS (Full Calcite Armor):").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Purifying aura").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Undead damage bonus").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Crystal edge").formatted(Formatting.DARK_GRAY));
        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponentTypes.MAX_DAMAGE, 340);
        SlayerItems.setCustomItemId(sword, "calcite_sword");
        return sword;
    }

    public static ItemStack createDeepslateSword() {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.set(DataComponentTypes.CUSTOM_NAME, Text.literal("⚔ Deepslate Blade").formatted(Formatting.DARK_GRAY, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("⚔ Damage: 7").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Durability: 550").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚔ Mining strike bonus").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ SET BONUS (Full Deepslate Armor):").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Mining speed +20%").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Darkvision").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Deep edge").formatted(Formatting.DARK_GRAY));
        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponentTypes.MAX_DAMAGE, 550);
        SlayerItems.setCustomItemId(sword, "deepslate_sword");
        return sword;
    }

    public static ItemStack createDripstoneSword() {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.set(DataComponentTypes.CUSTOM_NAME, Text.literal("⚔ Dripstone Blade").formatted(Formatting.GOLD, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("⚔ Damage: 6").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Durability: 360").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚔ Piercing strikes").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ SET BONUS (Full Dripstone Armor):").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Fall damage dealt").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Piercing attacks").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Sharp edge").formatted(Formatting.DARK_GRAY));
        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponentTypes.MAX_DAMAGE, 360);
        SlayerItems.setCustomItemId(sword, "dripstone_sword");
        return sword;
    }

    public static ItemStack createHaySword() {
        ItemStack sword = new ItemStack(Items.WOODEN_SWORD);
        sword.set(DataComponentTypes.CUSTOM_NAME, Text.literal("⚔ Hay Blade").formatted(Formatting.YELLOW, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚔ Damage: 3").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Durability: 100").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚔ Soft strikes").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ SET BONUS (Full Hay Armor):").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• 90% fall damage reduction").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Farmer's fortune").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Soft edge").formatted(Formatting.DARK_GRAY));
        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponentTypes.MAX_DAMAGE, 100);
        SlayerItems.setCustomItemId(sword, "hay_sword");
        return sword;
    }

    public static ItemStack createHoneycombSword() {
        ItemStack sword = new ItemStack(Items.GOLDEN_SWORD);
        sword.set(DataComponentTypes.CUSTOM_NAME, Text.literal("⚔ Honeycomb Blade").formatted(Formatting.GOLD, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("⚔ Damage: 5").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Durability: 220").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚔ Sticky strikes").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ SET BONUS (Full Honeycomb Armor):").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Bees are friendly").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Honey regen").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Sweet edge").formatted(Formatting.DARK_GRAY));
        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponentTypes.MAX_DAMAGE, 220);
        SlayerItems.setCustomItemId(sword, "honeycomb_sword");
        return sword;
    }

    public static ItemStack createLilyPadSword() {
        ItemStack sword = new ItemStack(Items.WOODEN_SWORD);
        sword.set(DataComponentTypes.CUSTOM_NAME, Text.literal("⚔ Lily Pad Blade").formatted(Formatting.DARK_GREEN, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_GREEN));
        lore.add(Text.literal("⚔ Damage: 4").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Durability: 160").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚔ Water affinity").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ SET BONUS (Full Lily Pad Armor):").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Walk on water").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Water breathing").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Floating edge").formatted(Formatting.DARK_GRAY));
        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponentTypes.MAX_DAMAGE, 160);
        SlayerItems.setCustomItemId(sword, "lily_pad_sword");
        return sword;
    }

    public static ItemStack createMelonSword() {
        ItemStack sword = new ItemStack(Items.WOODEN_SWORD);
        sword.set(DataComponentTypes.CUSTOM_NAME, Text.literal("⚔ Melon Blade").formatted(Formatting.RED, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Damage: 4").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Durability: 170").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚔ Juicy strikes").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ SET BONUS (Full Melon Armor):").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Auto-eat, food regen").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Summon melon slices").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Refreshing edge").formatted(Formatting.DARK_GRAY));
        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponentTypes.MAX_DAMAGE, 170);
        SlayerItems.setCustomItemId(sword, "melon_sword");
        return sword;
    }

    public static ItemStack createMossBlockSword() {
        ItemStack sword = new ItemStack(Items.WOODEN_SWORD);
        sword.set(DataComponentTypes.CUSTOM_NAME, Text.literal("⚔ Moss Block Blade").formatted(Formatting.DARK_GREEN, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_GREEN));
        lore.add(Text.literal("⚔ Damage: 4").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Durability: 175").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚔ Nature strikes").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ SET BONUS (Full Moss Block Armor):").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Nature regen").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Bone meal aura").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Overgrown edge").formatted(Formatting.DARK_GRAY));
        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponentTypes.MAX_DAMAGE, 175);
        SlayerItems.setCustomItemId(sword, "moss_block_sword");
        return sword;
    }

    public static ItemStack createMyceliumSword() {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.set(DataComponentTypes.CUSTOM_NAME, Text.literal("⚔ Mycelium Blade").formatted(Formatting.DARK_PURPLE, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_PURPLE));
        lore.add(Text.literal("⚔ Damage: 5").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Durability: 290").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚔ Spore strikes").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ SET BONUS (Full Mycelium Armor):").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Mushroom spread").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Spore attack").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Fungal edge").formatted(Formatting.DARK_GRAY));
        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponentTypes.MAX_DAMAGE, 290);
        SlayerItems.setCustomItemId(sword, "mycelium_sword");
        return sword;
    }

    public static ItemStack createNetherBrickSword() {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.set(DataComponentTypes.CUSTOM_NAME, Text.literal("⚔ Nether Brick Blade").formatted(Formatting.DARK_RED, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_RED));
        lore.add(Text.literal("⚔ Damage: 6").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Durability: 480").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚔ Fiery strikes").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ SET BONUS (Full Nether Brick Armor):").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Fire resistance").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Wither skeleton immunity").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Fortress edge").formatted(Formatting.DARK_GRAY));
        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponentTypes.MAX_DAMAGE, 480);
        SlayerItems.setCustomItemId(sword, "nether_brick_sword");
        return sword;
    }

    public static ItemStack createPumpkinSword() {
        ItemStack sword = new ItemStack(Items.WOODEN_SWORD);
        sword.set(DataComponentTypes.CUSTOM_NAME, Text.literal("⚔ Pumpkin Blade").formatted(Formatting.GOLD, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("⚔ Damage: 4").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Durability: 190").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚔ Halloween strikes").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ SET BONUS (Full Pumpkin Armor):").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Endermen ignore you").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Halloween themed").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Spooky edge").formatted(Formatting.DARK_GRAY));
        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponentTypes.MAX_DAMAGE, 190);
        SlayerItems.setCustomItemId(sword, "pumpkin_sword");
        return sword;
    }

    public static ItemStack createPurpurSword() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.set(DataComponentTypes.CUSTOM_NAME, Text.literal("⚔ Purpur Blade").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal("⚔ Damage: 7").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Durability: 420").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚔ End strikes").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ SET BONUS (Full Purpur Armor):").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Levitation immunity").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Shulker bonus damage").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8End city edge").formatted(Formatting.DARK_GRAY));
        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponentTypes.MAX_DAMAGE, 420);
        SlayerItems.setCustomItemId(sword, "purpur_sword");
        return sword;
    }

    public static ItemStack createSandSword() {
        ItemStack sword = new ItemStack(Items.WOODEN_SWORD);
        sword.set(DataComponentTypes.CUSTOM_NAME, Text.literal("⚔ Sand Blade").formatted(Formatting.YELLOW, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚔ Damage: 3").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Durability: 140").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚔ Shifting strikes").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ SET BONUS (Full Sand Armor):").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Desert immunity").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Sand speed boost").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Shifting edge").formatted(Formatting.DARK_GRAY));
        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponentTypes.MAX_DAMAGE, 140);
        SlayerItems.setCustomItemId(sword, "sand_sword");
        return sword;
    }

    public static ItemStack createSculkSword() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.set(DataComponentTypes.CUSTOM_NAME, Text.literal("⚔ Sculk Blade").formatted(Formatting.DARK_AQUA, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_AQUA));
        lore.add(Text.literal("⚔ Damage: 8").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Durability: 580").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚔ Silent strikes").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ SET BONUS (Full Sculk Armor):").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Silence").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Warden immunity").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Dark sensor edge").formatted(Formatting.DARK_GRAY));
        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponentTypes.MAX_DAMAGE, 580);
        SlayerItems.setCustomItemId(sword, "sculk_sword");
        return sword;
    }

    public static ItemStack createShroomlightSword() {
        ItemStack sword = new ItemStack(Items.GOLDEN_SWORD);
        sword.set(DataComponentTypes.CUSTOM_NAME, Text.literal("⚔ Shroomlight Blade").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal("⚔ Damage: 5").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Durability: 310").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚔ Light strikes").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ SET BONUS (Full Shroomlight Armor):").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Permanent light").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Night vision").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Bioluminescent edge").formatted(Formatting.DARK_GRAY));
        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponentTypes.MAX_DAMAGE, 310);
        SlayerItems.setCustomItemId(sword, "shroomlight_sword");
        return sword;
    }

    public static ItemStack createSlimeSword() {
        ItemStack sword = new ItemStack(Items.WOODEN_SWORD);
        sword.set(DataComponentTypes.CUSTOM_NAME, Text.literal("⚔ Slime Blade").formatted(Formatting.GREEN, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.GREEN));
        lore.add(Text.literal("⚔ Damage: 4").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Durability: 165").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚔ Bouncy strikes").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ SET BONUS (Full Slime Armor):").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Bounce jumps").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Fall damage immunity").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Boing edge!").formatted(Formatting.DARK_GRAY));
        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponentTypes.MAX_DAMAGE, 165);
        SlayerItems.setCustomItemId(sword, "slime_sword");
        return sword;
    }

    public static ItemStack createSmoothStoneSword() {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.set(DataComponentTypes.CUSTOM_NAME, Text.literal("⚔ Smooth Stone Blade").formatted(Formatting.GRAY, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.GRAY));
        lore.add(Text.literal("⚔ Damage: 6").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Durability: 440").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚔ Smooth strikes").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ SET BONUS (Full Smooth Stone Armor):").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Smooth movement").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Efficient mining").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Refined edge").formatted(Formatting.DARK_GRAY));
        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponentTypes.MAX_DAMAGE, 440);
        SlayerItems.setCustomItemId(sword, "smooth_stone_sword");
        return sword;
    }

    public static ItemStack createSnowSword() {
        ItemStack sword = new ItemStack(Items.WOODEN_SWORD);
        sword.set(DataComponentTypes.CUSTOM_NAME, Text.literal("⚔ Snow Blade").formatted(Formatting.WHITE, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.WHITE));
        lore.add(Text.literal("⚔ Damage: 3").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Durability: 130").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚔ Freezing strikes").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ SET BONUS (Full Snow Armor):").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Freeze attackers").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Frost walker").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Frozen edge").formatted(Formatting.DARK_GRAY));
        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponentTypes.MAX_DAMAGE, 130);
        SlayerItems.setCustomItemId(sword, "snow_sword");
        return sword;
    }

    public static ItemStack createSoulSoilSword() {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.set(DataComponentTypes.CUSTOM_NAME, Text.literal("⚔ Soul Soil Blade").formatted(Formatting.DARK_GRAY, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("⚔ Damage: 6").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Durability: 410").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚔ Soul fire strikes").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ SET BONUS (Full Soul Soil Armor):").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Soul fire immunity").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Soul speed boost").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Soul powered edge").formatted(Formatting.DARK_GRAY));
        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponentTypes.MAX_DAMAGE, 410);
        SlayerItems.setCustomItemId(sword, "soul_soil_sword");
        return sword;
    }

    public static ItemStack createSpongeSword() {
        ItemStack sword = new ItemStack(Items.WOODEN_SWORD);
        sword.set(DataComponentTypes.CUSTOM_NAME, Text.literal("⚔ Sponge Blade").formatted(Formatting.YELLOW, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚔ Damage: 4").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Durability: 185").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚔ Absorbing strikes").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ SET BONUS (Full Sponge Armor):").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Dry sponge effect").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Water breathing").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Absorbent edge").formatted(Formatting.DARK_GRAY));
        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponentTypes.MAX_DAMAGE, 185);
        SlayerItems.setCustomItemId(sword, "sponge_sword");
        return sword;
    }

    public static ItemStack createTargetSword() {
        ItemStack sword = new ItemStack(Items.WOODEN_SWORD);
        sword.set(DataComponentTypes.CUSTOM_NAME, Text.literal("⚔ Target Blade").formatted(Formatting.RED, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Damage: 4").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Durability: 195").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚔ Precise strikes").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ SET BONUS (Full Target Armor):").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Redstone signal").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Projectile deflection").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Bullseye edge!").formatted(Formatting.DARK_GRAY));
        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponentTypes.MAX_DAMAGE, 195);
        SlayerItems.setCustomItemId(sword, "target_sword");
        return sword;
    }

    public static ItemStack createTntSword() {
        ItemStack sword = new ItemStack(Items.WOODEN_SWORD);
        sword.set(DataComponentTypes.CUSTOM_NAME, Text.literal("⚔ TNT Blade").formatted(Formatting.RED, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Damage: 5").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Durability: 120").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚔ Explosive strikes").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ SET BONUS (Full TNT Armor):").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Explosion immunity").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Blast mining boost").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§4Careful!").formatted(Formatting.DARK_RED));
        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponentTypes.MAX_DAMAGE, 120);
        SlayerItems.setCustomItemId(sword, "tnt_sword");
        return sword;
    }

    public static ItemStack createWarpedSword() {
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        sword.set(DataComponentTypes.CUSTOM_NAME, Text.literal("⚔ Warped Blade").formatted(Formatting.DARK_AQUA, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_AQUA));
        lore.add(Text.literal("⚔ Damage: 6").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Durability: 350").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚔ Warped strikes").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ SET BONUS (Full Warped Armor):").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Warped forest affinity").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Endermen ignore you").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Warped edge").formatted(Formatting.DARK_GRAY));
        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponentTypes.MAX_DAMAGE, 350);
        SlayerItems.setCustomItemId(sword, "warped_sword");
        return sword;
    }

    public static ItemStack createWetSpongeSword() {
        ItemStack sword = new ItemStack(Items.WOODEN_SWORD);
        sword.set(DataComponentTypes.CUSTOM_NAME, Text.literal("⚔ Wet Sponge Blade").formatted(Formatting.DARK_GREEN, Formatting.BOLD));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_GREEN));
        lore.add(Text.literal("⚔ Damage: 4").formatted(Formatting.RED));
        lore.add(Text.literal("⚔ Durability: 180").formatted(Formatting.YELLOW));
        lore.add(Text.literal("⚔ Wet strikes").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("✨ SET BONUS (Full Wet Sponge Armor):").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        lore.add(Text.literal("• Fire extinguisher").formatted(Formatting.GREEN));
        lore.add(Text.literal("• Wet and wild").formatted(Formatting.GREEN));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Soggy edge").formatted(Formatting.DARK_GRAY));
        sword.set(DataComponentTypes.LORE, new LoreComponent(lore));
        sword.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.set(DataComponentTypes.MAX_DAMAGE, 180);
        SlayerItems.setCustomItemId(sword, "wet_sponge_sword");
        return sword;
    }
}
