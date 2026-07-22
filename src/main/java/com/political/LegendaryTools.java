package com.political;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import java.util.Arrays;

/**
 * Custom Legendary Tools - 15 unique tool sets with special abilities
 */
public class LegendaryTools {

    // Tool Set 1: Dragon Tools - Fire/Explosion themed
    public static ItemStack createDragonPickaxe() {
        return createLegendaryTool(Items.NETHERITE_PICKAXE, "Dragon Pickaxe", ChatFormatting.DARK_RED,
            "Blazing Excavation", "Mines 3x3 and auto-smelts ores", 10);
    }
    public static ItemStack createDragonAxe() {
        return createLegendaryTool(Items.NETHERITE_AXE, "Dragon Axe", ChatFormatting.DARK_RED,
            "Inferno Cut", "Chops entire trees at once", 12);
    }
    public static ItemStack createDragonShovel() {
        return createLegendaryTool(Items.NETHERITE_SHOVEL, "Dragon Shovel", ChatFormatting.DARK_RED,
            "Magma Dig", "3x3 digging with lava immunity", 8);
    }
    public static ItemStack createDragonHoe() {
        return createLegendaryTool(Items.NETHERITE_HOE, "Dragon Hoe", ChatFormatting.DARK_RED,
            "Scorched Earth", "Tills 5x5 and auto-replants", 6);
    }

    // Tool Set 2: Aether Tools - Sky/Floating themed
    public static ItemStack createAetherPickaxe() {
        return createLegendaryTool(Items.DIAMOND_PICKAXE, "Aether Pickaxe", ChatFormatting.AQUA,
            "Skyward Mining", "Mining speed increases with height", 8);
    }
    public static ItemStack createAetherAxe() {
        return createLegendaryTool(Items.DIAMOND_AXE, "Aether Axe", ChatFormatting.AQUA,
            "Wind Cut", "Throws mobs into the air on hit", 10);
    }
    public static ItemStack createAetherShovel() {
        return createLegendaryTool(Items.DIAMOND_SHOVEL, "Aether Shovel", ChatFormatting.AQUA,
            "Cloud Walker", "Creates temporary cloud platforms", 6);
    }
    public static ItemStack createAetherHoe() {
        return createLegendaryTool(Items.DIAMOND_HOE, "Aether Hoe", ChatFormatting.AQUA,
            "Serene Cultivation", "Crops grow instantly when tilled", 5);
    }

    // Tool Set 3: Void Tools - Dark/Mystery themed
    public static ItemStack createVoidPickaxe() {
        return createLegendaryTool(Items.NETHERITE_PICKAXE, "Void Pickaxe", ChatFormatting.DARK_PURPLE,
            "Null Mining", "Teleports mined blocks to inventory", 12);
    }
    public static ItemStack createVoidAxe() {
        return createLegendaryTool(Items.NETHERITE_AXE, "Void Axe", ChatFormatting.DARK_PURPLE,
            "Shadow Strike", "Silent tree felling + invisibility", 14);
    }
    public static ItemStack createVoidShovel() {
        return createLegendaryTool(Items.NETHERITE_SHOVEL, "Void Shovel", ChatFormatting.DARK_PURPLE,
            "Ender Excavation", "3x3 mining + ender pearl teleport", 10);
    }
    public static ItemStack createVoidHoe() {
        return createLegendaryTool(Items.NETHERITE_HOE, "Void Hoe", ChatFormatting.DARK_PURPLE,
            "Dark Harvest", "2x crop yield + night vision", 8);
    }

    // Tool Set 4: Nature Tools - Growth/Life themed
    public static ItemStack createNaturePickaxe() {
        return createLegendaryTool(Items.DIAMOND_PICKAXE, "Nature Pickaxe", ChatFormatting.GREEN,
            "Living Stone", "Plants saplings when mining stone", 7);
    }
    public static ItemStack createNatureAxe() {
        return createLegendaryTool(Items.DIAMOND_AXE, "Nature Axe", ChatFormatting.GREEN,
            "Regrowth", "Auto-replants trees after chopping", 9);
    }
    public static ItemStack createNatureShovel() {
        return createLegendaryTool(Items.DIAMOND_SHOVEL, "Nature Shovel", ChatFormatting.GREEN,
            "Terraformer", "Creates grass paths + bone meal effect", 6);
    }
    public static ItemStack createNatureHoe() {
        return createLegendaryTool(Items.DIAMOND_HOE, "Nature Hoe", ChatFormatting.GREEN,
            "Fertile Blessing", "3x3 tilling + instant growth", 5);
    }

    // Tool Set 5: Frost Tools - Ice/Cold themed
    public static ItemStack createFrostPickaxe() {
        return createLegendaryTool(Items.DIAMOND_PICKAXE, "Frost Pickaxe", ChatFormatting.BLUE,
            "Glacial Mining", "Freezes water + creates ice blocks", 8);
    }
    public static ItemStack createFrostAxe() {
        return createLegendaryTool(Items.DIAMOND_AXE, "Frost Axe", ChatFormatting.BLUE,
            "Winter's Edge", "Slows enemies + freezes logs", 10);
    }
    public static ItemStack createFrostShovel() {
        return createLegendaryTool(Items.DIAMOND_SHOVEL, "Frost Shovel", ChatFormatting.BLUE,
            "Permafrost", "Creates packed ice from snow", 6);
    }
    public static ItemStack createFrostHoe() {
        return createLegendaryTool(Items.DIAMOND_HOE, "Frost Hoe", ChatFormatting.BLUE,
            "Frozen Harvest", "Preserves crops (never decays)", 5);
    }

    // Tool Set 6: Thunder Tools - Lightning/Electric themed
    public static ItemStack createThunderPickaxe() {
        return createLegendaryTool(Items.NETHERITE_PICKAXE, "Thunder Pickaxe", ChatFormatting.YELLOW,
            "Storm Mining", "Summons lightning on ore veins", 11);
    }
    public static ItemStack createThunderAxe() {
        return createLegendaryTool(Items.NETHERITE_AXE, "Thunder Axe", ChatFormatting.YELLOW,
            "Zeus's Wrath", "Chain lightning between trees", 13);
    }
    public static ItemStack createThunderShovel() {
        return createLegendaryTool(Items.NETHERITE_SHOVEL, "Thunder Shovel", ChatFormatting.YELLOW,
            "Static Charge", "Charges blocks + repels mobs", 9);
    }
    public static ItemStack createThunderHoe() {
        return createLegendaryTool(Items.NETHERITE_HOE, "Thunder Hoe", ChatFormatting.YELLOW,
            "Electric Cultivation", "Powers nearby redstone when farming", 7);
    }

    // Tool Set 7: Ocean Tools - Water/Sea themed
    public static ItemStack createOceanPickaxe() {
        return createLegendaryTool(Items.DIAMOND_PICKAXE, "Ocean Pickaxe", ChatFormatting.DARK_BLUE,
            "Depth Mining", "Water breathing + aqua affinity max", 8);
    }
    public static ItemStack createOceanAxe() {
        return createLegendaryTool(Items.DIAMOND_AXE, "Ocean Axe", ChatFormatting.DARK_BLUE,
            "Tidal Wave", "Creates water streams on chop", 10);
    }
    public static ItemStack createOceanShovel() {
        return createLegendaryTool(Items.DIAMOND_SHOVEL, "Ocean Shovel", ChatFormatting.DARK_BLUE,
            "Sand Walker", "Creates quicksand traps for mobs", 7);
    }
    public static ItemStack createOceanHoe() {
        return createLegendaryTool(Items.DIAMOND_HOE, "Ocean Hoe", ChatFormatting.DARK_BLUE,
            "Kelp Cultivator", "Grows sea crops instantly", 6);
    }

    // Tool Set 8: Lunar Tools - Moon/Night themed
    public static ItemStack createLunarPickaxe() {
        return createLegendaryTool(Items.NETHERITE_PICKAXE, "Lunar Pickaxe", ChatFormatting.LIGHT_PURPLE,
            "Moonlight Mining", "Stronger at night + silverfish detection", 9);
    }
    public static ItemStack createLunarAxe() {
        return createLegendaryTool(Items.NETHERITE_AXE, "Lunar Axe", ChatFormatting.LIGHT_PURPLE,
            "Silver Edge", "Bonus damage to undead mobs", 11);
    }
    public static ItemStack createLunarShovel() {
        return createLegendaryTool(Items.NETHERITE_SHOVEL, "Lunar Shovel", ChatFormatting.LIGHT_PURPLE,
            "Night Digger", "Grants night vision when digging", 7);
    }
    public static ItemStack createLunarHoe() {
        return createLegendaryTool(Items.NETHERITE_HOE, "Lunar Hoe", ChatFormatting.LIGHT_PURPLE,
            "Moon Harvest", "Crops glow and grow at night", 6);
    }

    // Tool Set 9: Solar Tools - Sun/Day themed
    public static ItemStack createSolarPickaxe() {
        return createLegendaryTool(Items.NETHERITE_PICKAXE, "Solar Pickaxe", ChatFormatting.GOLD,
            "Solar Flare", "Burns stone into smooth variants", 10);
    }
    public static ItemStack createSolarAxe() {
        return createLegendaryTool(Items.NETHERITE_AXE, "Solar Axe", ChatFormatting.GOLD,
            "Sun Strike", "Sets targets on fire (day only)", 12);
    }
    public static ItemStack createSolarShovel() {
        return createLegendaryTool(Items.NETHERITE_SHOVEL, "Solar Shovel", ChatFormatting.GOLD,
            "Daylight Digger", "Faster mining during day", 8);
    }
    public static ItemStack createSolarHoe() {
        return createLegendaryTool(Items.NETHERITE_HOE, "Solar Hoe", ChatFormatting.GOLD,
            "Photosynthesis", "Crops grow 10x faster in sunlight", 7);
    }

    // Tool Set 10: Terra Tools - Earth/Stone themed
    public static ItemStack createTerraPickaxe() {
        return createLegendaryTool(Items.DIAMOND_PICKAXE, "Terra Pickaxe", ChatFormatting.DARK_GREEN,
            "Seismic Mining", "5x5 mining with haste boost", 9);
    }
    public static ItemStack createTerraAxe() {
        return createLegendaryTool(Items.DIAMOND_AXE, "Terra Axe", ChatFormatting.DARK_GREEN,
            "Earth Shaker", "Roots enemies in place", 11);
    }
    public static ItemStack createTerraShovel() {
        return createLegendaryTool(Items.DIAMOND_SHOVEL, "Terra Shovel", ChatFormatting.DARK_GREEN,
            "Landscaper", "Flattens large areas instantly", 7);
    }
    public static ItemStack createTerraHoe() {
        return createLegendaryTool(Items.DIAMOND_HOE, "Terra Hoe", ChatFormatting.DARK_GREEN,
            "Rich Soil", "Generates bonemeal from tilling", 6);
    }

    // Tool Set 11: Phantom Tools - Ghost/Invisible themed
    public static ItemStack createPhantomPickaxe() {
        return createLegendaryTool(Items.NETHERITE_PICKAXE, "Phantom Pickaxe", ChatFormatting.GRAY,
            "Ghost Mining", "Silent mining + temporary invisibility", 10);
    }
    public static ItemStack createPhantomAxe() {
        return createLegendaryTool(Items.NETHERITE_AXE, "Phantom Axe", ChatFormatting.GRAY,
            "Spectral Cut", "Ignores armor on mobs", 12);
    }
    public static ItemStack createPhantomShovel() {
        return createLegendaryTool(Items.NETHERITE_SHOVEL, "Phantom Shovel", ChatFormatting.GRAY,
            "Ethereal Dig", "Walk through blocks briefly after digging", 8);
    }
    public static ItemStack createPhantomHoe() {
        return createLegendaryTool(Items.NETHERITE_HOE, "Phantom Hoe", ChatFormatting.GRAY,
            "Spirit Harvest", "Crops drop XP orbs", 6);
    }

    // Tool Set 12: Blood Tools - Vampire/Life steal themed
    public static ItemStack createBloodPickaxe() {
        return createLegendaryTool(Items.NETHERITE_PICKAXE, "Blood Pickaxe", ChatFormatting.DARK_RED,
            "Vampiric Mining", "Heals when mining ores", 11);
    }
    public static ItemStack createBloodAxe() {
        return createLegendaryTool(Items.NETHERITE_AXE, "Blood Axe", ChatFormatting.DARK_RED,
            "Life Drain", "Steals health from attacked mobs", 13);
    }
    public static ItemStack createBloodShovel() {
        return createLegendaryTool(Items.NETHERITE_SHOVEL, "Blood Shovel", ChatFormatting.DARK_RED,
            "Soul Dig", "Grave digging - finds buried loot", 9);
    }
    public static ItemStack createBloodHoe() {
        return createLegendaryTool(Items.NETHERITE_HOE, "Blood Hoe", ChatFormatting.DARK_RED,
            "Blood Harvest", "Uses health to instantly grow crops", 7);
    }

    // Tool Set 13: Celestial Tools - Star/Space themed
    public static ItemStack createCelestialPickaxe() {
        return createLegendaryTool(Items.NETHERITE_PICKAXE, "Celestial Pickaxe", ChatFormatting.DARK_AQUA,
            "Star Mining", "Finds rare ores easier", 12);
    }
    public static ItemStack createCelestialAxe() {
        return createLegendaryTool(Items.NETHERITE_AXE, "Celestial Axe", ChatFormatting.DARK_AQUA,
            "Comet Strike", "Falling star AOE damage", 14);
    }
    public static ItemStack createCelestialShovel() {
        return createLegendaryTool(Items.NETHERITE_SHOVEL, "Celestial Shovel", ChatFormatting.DARK_AQUA,
            "Meteor Dig", "Explosive digging (controlled)", 10);
    }
    public static ItemStack createCelestialHoe() {
        return createLegendaryTool(Items.NETHERITE_HOE, "Celestial Hoe", ChatFormatting.DARK_AQUA,
            "Cosmic Harvest", "Crops sparkle and give bonus drops", 8);
    }

    // Tool Set 14: Shadow Tools - Darkness/Stealth themed
    public static ItemStack createShadowPickaxe() {
        return createLegendaryTool(Items.NETHERITE_PICKAXE, "Shadow Pickaxe", ChatFormatting.BLACK,
            "Darkness Mining", "Invisible particles + blindness immunity", 10);
    }
    public static ItemStack createShadowAxe() {
        return createLegendaryTool(Items.NETHERITE_AXE, "Shadow Axe", ChatFormatting.BLACK,
            "Umbral Strike", "Crits from behind, sneak attacks", 12);
    }
    public static ItemStack createShadowShovel() {
        return createLegendaryTool(Items.NETHERITE_SHOVEL, "Shadow Shovel", ChatFormatting.BLACK,
            "Blackout", "Creates darkness around dig site", 8);
    }
    public static ItemStack createShadowHoe() {
        return createLegendaryTool(Items.NETHERITE_HOE, "Shadow Hoe", ChatFormatting.BLACK,
            "Nightshade", "Poisonous crops damage enemies", 7);
    }

    // Tool Set 15: Crystal Tools - Gem/Shiny themed
    public static ItemStack createCrystalPickaxe() {
        return createLegendaryTool(Items.DIAMOND_PICKAXE, "Crystal Pickaxe", ChatFormatting.WHITE,
            "Prismatic Mining", "Finds gems + fortune boost", 9);
    }
    public static ItemStack createCrystalAxe() {
        return createLegendaryTool(Items.DIAMOND_AXE, "Crystal Axe", ChatFormatting.WHITE,
            "Shimmering Edge", "Reflects projectiles when blocking", 11);
    }
    public static ItemStack createCrystalShovel() {
        return createLegendaryTool(Items.DIAMOND_SHOVEL, "Crystal Shovel", ChatFormatting.WHITE,
            "Gem Detector", "Highlights ores through walls briefly", 7);
    }
    public static ItemStack createCrystalHoe() {
        return createLegendaryTool(Items.DIAMOND_HOE, "Crystal Hoe", ChatFormatting.WHITE,
            "Diamond Touch", "Crops have chance to drop diamonds", 6);
    }

    // Helper method to create a legendary tool with standard formatting
    private static ItemStack createLegendaryTool(Item baseItem, String name, ChatFormatting color,
                                                  String ability, String description, int damage) {
        ItemStack stack = new ItemStack(baseItem);

        // Custom name with glow
        stack.set(DataComponents.CUSTOM_NAME,
            Component.literal("✦ " + name).withStyle(color, ChatFormatting.BOLD));

        // Lore with ability info
        stack.set(DataComponents.LORE, new ItemLore(Arrays.asList(
            Component.literal(""),
            Component.literal("◆ LEGENDARY TOOL ◆").withStyle(color, ChatFormatting.BOLD),
            Component.literal(""),
            Component.literal("Ability: " + ability).withStyle(ChatFormatting.YELLOW),
            Component.literal("└ " + description).withStyle(ChatFormatting.GRAY),
            Component.literal(""),
            Component.literal("Damage: " + damage).withStyle(ChatFormatting.RED),
            Component.literal("Unbreakable").withStyle(ChatFormatting.GREEN),
            Component.literal(""),
            Component.literal("「Legendary」").withStyle(color)
        )));

        // Make unbreakable and add enchant glow
        stack.set(DataComponents.MAX_DAMAGE, Integer.MAX_VALUE);
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);

        // Set custom ID
        String id = name.toLowerCase().replace("'", "").replace(" ", "_");
        SlayerItems.setCustomItemId(stack, id);

        return stack;
    }
}
