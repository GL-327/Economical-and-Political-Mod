package com.political;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import java.util.Arrays;

/**
 * Custom Legendary Tools - 15 unique tool sets with special abilities
 */
public class LegendaryTools {

    // Tool Set 1: Dragon Tools - Fire/Explosion themed
    public static ItemStack createDragonPickaxe() {
        return createLegendaryTool(Items.NETHERITE_PICKAXE, "Dragon Pickaxe", Formatting.DARK_RED,
            "Blazing Excavation", "Mines 3x3 and auto-smelts ores", 10);
    }
    public static ItemStack createDragonAxe() {
        return createLegendaryTool(Items.NETHERITE_AXE, "Dragon Axe", Formatting.DARK_RED,
            "Inferno Cut", "Chops entire trees at once", 12);
    }
    public static ItemStack createDragonShovel() {
        return createLegendaryTool(Items.NETHERITE_SHOVEL, "Dragon Shovel", Formatting.DARK_RED,
            "Magma Dig", "3x3 digging with lava immunity", 8);
    }
    public static ItemStack createDragonHoe() {
        return createLegendaryTool(Items.NETHERITE_HOE, "Dragon Hoe", Formatting.DARK_RED,
            "Scorched Earth", "Tills 5x5 and auto-replants", 6);
    }

    // Tool Set 2: Aether Tools - Sky/Floating themed
    public static ItemStack createAetherPickaxe() {
        return createLegendaryTool(Items.DIAMOND_PICKAXE, "Aether Pickaxe", Formatting.AQUA,
            "Skyward Mining", "Mining speed increases with height", 8);
    }
    public static ItemStack createAetherAxe() {
        return createLegendaryTool(Items.DIAMOND_AXE, "Aether Axe", Formatting.AQUA,
            "Wind Cut", "Throws mobs into the air on hit", 10);
    }
    public static ItemStack createAetherShovel() {
        return createLegendaryTool(Items.DIAMOND_SHOVEL, "Aether Shovel", Formatting.AQUA,
            "Cloud Walker", "Creates temporary cloud platforms", 6);
    }
    public static ItemStack createAetherHoe() {
        return createLegendaryTool(Items.DIAMOND_HOE, "Aether Hoe", Formatting.AQUA,
            "Serene Cultivation", "Crops grow instantly when tilled", 5);
    }

    // Tool Set 3: Void Tools - Dark/Mystery themed
    public static ItemStack createVoidPickaxe() {
        return createLegendaryTool(Items.NETHERITE_PICKAXE, "Void Pickaxe", Formatting.DARK_PURPLE,
            "Null Mining", "Teleports mined blocks to inventory", 12);
    }
    public static ItemStack createVoidAxe() {
        return createLegendaryTool(Items.NETHERITE_AXE, "Void Axe", Formatting.DARK_PURPLE,
            "Shadow Strike", "Silent tree felling + invisibility", 14);
    }
    public static ItemStack createVoidShovel() {
        return createLegendaryTool(Items.NETHERITE_SHOVEL, "Void Shovel", Formatting.DARK_PURPLE,
            "Ender Excavation", "3x3 mining + ender pearl teleport", 10);
    }
    public static ItemStack createVoidHoe() {
        return createLegendaryTool(Items.NETHERITE_HOE, "Void Hoe", Formatting.DARK_PURPLE,
            "Dark Harvest", "2x crop yield + night vision", 8);
    }

    // Tool Set 4: Nature Tools - Growth/Life themed
    public static ItemStack createNaturePickaxe() {
        return createLegendaryTool(Items.DIAMOND_PICKAXE, "Nature Pickaxe", Formatting.GREEN,
            "Living Stone", "Plants saplings when mining stone", 7);
    }
    public static ItemStack createNatureAxe() {
        return createLegendaryTool(Items.DIAMOND_AXE, "Nature Axe", Formatting.GREEN,
            "Regrowth", "Auto-replants trees after chopping", 9);
    }
    public static ItemStack createNatureShovel() {
        return createLegendaryTool(Items.DIAMOND_SHOVEL, "Nature Shovel", Formatting.GREEN,
            "Terraformer", "Creates grass paths + bone meal effect", 6);
    }
    public static ItemStack createNatureHoe() {
        return createLegendaryTool(Items.DIAMOND_HOE, "Nature Hoe", Formatting.GREEN,
            "Fertile Blessing", "3x3 tilling + instant growth", 5);
    }

    // Tool Set 5: Frost Tools - Ice/Cold themed
    public static ItemStack createFrostPickaxe() {
        return createLegendaryTool(Items.DIAMOND_PICKAXE, "Frost Pickaxe", Formatting.BLUE,
            "Glacial Mining", "Freezes water + creates ice blocks", 8);
    }
    public static ItemStack createFrostAxe() {
        return createLegendaryTool(Items.DIAMOND_AXE, "Frost Axe", Formatting.BLUE,
            "Winter's Edge", "Slows enemies + freezes logs", 10);
    }
    public static ItemStack createFrostShovel() {
        return createLegendaryTool(Items.DIAMOND_SHOVEL, "Frost Shovel", Formatting.BLUE,
            "Permafrost", "Creates packed ice from snow", 6);
    }
    public static ItemStack createFrostHoe() {
        return createLegendaryTool(Items.DIAMOND_HOE, "Frost Hoe", Formatting.BLUE,
            "Frozen Harvest", "Preserves crops (never decays)", 5);
    }

    // Tool Set 6: Thunder Tools - Lightning/Electric themed
    public static ItemStack createThunderPickaxe() {
        return createLegendaryTool(Items.NETHERITE_PICKAXE, "Thunder Pickaxe", Formatting.YELLOW,
            "Storm Mining", "Summons lightning on ore veins", 11);
    }
    public static ItemStack createThunderAxe() {
        return createLegendaryTool(Items.NETHERITE_AXE, "Thunder Axe", Formatting.YELLOW,
            "Zeus's Wrath", "Chain lightning between trees", 13);
    }
    public static ItemStack createThunderShovel() {
        return createLegendaryTool(Items.NETHERITE_SHOVEL, "Thunder Shovel", Formatting.YELLOW,
            "Static Charge", "Charges blocks + repels mobs", 9);
    }
    public static ItemStack createThunderHoe() {
        return createLegendaryTool(Items.NETHERITE_HOE, "Thunder Hoe", Formatting.YELLOW,
            "Electric Cultivation", "Powers nearby redstone when farming", 7);
    }

    // Tool Set 7: Ocean Tools - Water/Sea themed
    public static ItemStack createOceanPickaxe() {
        return createLegendaryTool(Items.DIAMOND_PICKAXE, "Ocean Pickaxe", Formatting.DARK_BLUE,
            "Depth Mining", "Water breathing + aqua affinity max", 8);
    }
    public static ItemStack createOceanAxe() {
        return createLegendaryTool(Items.DIAMOND_AXE, "Ocean Axe", Formatting.DARK_BLUE,
            "Tidal Wave", "Creates water streams on chop", 10);
    }
    public static ItemStack createOceanShovel() {
        return createLegendaryTool(Items.DIAMOND_SHOVEL, "Ocean Shovel", Formatting.DARK_BLUE,
            "Sand Walker", "Creates quicksand traps for mobs", 7);
    }
    public static ItemStack createOceanHoe() {
        return createLegendaryTool(Items.DIAMOND_HOE, "Ocean Hoe", Formatting.DARK_BLUE,
            "Kelp Cultivator", "Grows sea crops instantly", 6);
    }

    // Tool Set 8: Lunar Tools - Moon/Night themed
    public static ItemStack createLunarPickaxe() {
        return createLegendaryTool(Items.NETHERITE_PICKAXE, "Lunar Pickaxe", Formatting.LIGHT_PURPLE,
            "Moonlight Mining", "Stronger at night + silverfish detection", 9);
    }
    public static ItemStack createLunarAxe() {
        return createLegendaryTool(Items.NETHERITE_AXE, "Lunar Axe", Formatting.LIGHT_PURPLE,
            "Silver Edge", "Bonus damage to undead mobs", 11);
    }
    public static ItemStack createLunarShovel() {
        return createLegendaryTool(Items.NETHERITE_SHOVEL, "Lunar Shovel", Formatting.LIGHT_PURPLE,
            "Night Digger", "Grants night vision when digging", 7);
    }
    public static ItemStack createLunarHoe() {
        return createLegendaryTool(Items.NETHERITE_HOE, "Lunar Hoe", Formatting.LIGHT_PURPLE,
            "Moon Harvest", "Crops glow and grow at night", 6);
    }

    // Tool Set 9: Solar Tools - Sun/Day themed
    public static ItemStack createSolarPickaxe() {
        return createLegendaryTool(Items.NETHERITE_PICKAXE, "Solar Pickaxe", Formatting.GOLD,
            "Solar Flare", "Burns stone into smooth variants", 10);
    }
    public static ItemStack createSolarAxe() {
        return createLegendaryTool(Items.NETHERITE_AXE, "Solar Axe", Formatting.GOLD,
            "Sun Strike", "Sets targets on fire (day only)", 12);
    }
    public static ItemStack createSolarShovel() {
        return createLegendaryTool(Items.NETHERITE_SHOVEL, "Solar Shovel", Formatting.GOLD,
            "Daylight Digger", "Faster mining during day", 8);
    }
    public static ItemStack createSolarHoe() {
        return createLegendaryTool(Items.NETHERITE_HOE, "Solar Hoe", Formatting.GOLD,
            "Photosynthesis", "Crops grow 10x faster in sunlight", 7);
    }

    // Tool Set 10: Terra Tools - Earth/Stone themed
    public static ItemStack createTerraPickaxe() {
        return createLegendaryTool(Items.DIAMOND_PICKAXE, "Terra Pickaxe", Formatting.DARK_GREEN,
            "Seismic Mining", "5x5 mining with haste boost", 9);
    }
    public static ItemStack createTerraAxe() {
        return createLegendaryTool(Items.DIAMOND_AXE, "Terra Axe", Formatting.DARK_GREEN,
            "Earth Shaker", "Roots enemies in place", 11);
    }
    public static ItemStack createTerraShovel() {
        return createLegendaryTool(Items.DIAMOND_SHOVEL, "Terra Shovel", Formatting.DARK_GREEN,
            "Landscaper", "Flattens large areas instantly", 7);
    }
    public static ItemStack createTerraHoe() {
        return createLegendaryTool(Items.DIAMOND_HOE, "Terra Hoe", Formatting.DARK_GREEN,
            "Rich Soil", "Generates bonemeal from tilling", 6);
    }

    // Tool Set 11: Phantom Tools - Ghost/Invisible themed
    public static ItemStack createPhantomPickaxe() {
        return createLegendaryTool(Items.NETHERITE_PICKAXE, "Phantom Pickaxe", Formatting.GRAY,
            "Ghost Mining", "Silent mining + temporary invisibility", 10);
    }
    public static ItemStack createPhantomAxe() {
        return createLegendaryTool(Items.NETHERITE_AXE, "Phantom Axe", Formatting.GRAY,
            "Spectral Cut", "Ignores armor on mobs", 12);
    }
    public static ItemStack createPhantomShovel() {
        return createLegendaryTool(Items.NETHERITE_SHOVEL, "Phantom Shovel", Formatting.GRAY,
            "Ethereal Dig", "Walk through blocks briefly after digging", 8);
    }
    public static ItemStack createPhantomHoe() {
        return createLegendaryTool(Items.NETHERITE_HOE, "Phantom Hoe", Formatting.GRAY,
            "Spirit Harvest", "Crops drop XP orbs", 6);
    }

    // Tool Set 12: Blood Tools - Vampire/Life steal themed
    public static ItemStack createBloodPickaxe() {
        return createLegendaryTool(Items.NETHERITE_PICKAXE, "Blood Pickaxe", Formatting.DARK_RED,
            "Vampiric Mining", "Heals when mining ores", 11);
    }
    public static ItemStack createBloodAxe() {
        return createLegendaryTool(Items.NETHERITE_AXE, "Blood Axe", Formatting.DARK_RED,
            "Life Drain", "Steals health from attacked mobs", 13);
    }
    public static ItemStack createBloodShovel() {
        return createLegendaryTool(Items.NETHERITE_SHOVEL, "Blood Shovel", Formatting.DARK_RED,
            "Soul Dig", "Grave digging - finds buried loot", 9);
    }
    public static ItemStack createBloodHoe() {
        return createLegendaryTool(Items.NETHERITE_HOE, "Blood Hoe", Formatting.DARK_RED,
            "Blood Harvest", "Uses health to instantly grow crops", 7);
    }

    // Tool Set 13: Celestial Tools - Star/Space themed
    public static ItemStack createCelestialPickaxe() {
        return createLegendaryTool(Items.NETHERITE_PICKAXE, "Celestial Pickaxe", Formatting.DARK_AQUA,
            "Star Mining", "Finds rare ores easier", 12);
    }
    public static ItemStack createCelestialAxe() {
        return createLegendaryTool(Items.NETHERITE_AXE, "Celestial Axe", Formatting.DARK_AQUA,
            "Comet Strike", "Falling star AOE damage", 14);
    }
    public static ItemStack createCelestialShovel() {
        return createLegendaryTool(Items.NETHERITE_SHOVEL, "Celestial Shovel", Formatting.DARK_AQUA,
            "Meteor Dig", "Explosive digging (controlled)", 10);
    }
    public static ItemStack createCelestialHoe() {
        return createLegendaryTool(Items.NETHERITE_HOE, "Celestial Hoe", Formatting.DARK_AQUA,
            "Cosmic Harvest", "Crops sparkle and give bonus drops", 8);
    }

    // Tool Set 14: Shadow Tools - Darkness/Stealth themed
    public static ItemStack createShadowPickaxe() {
        return createLegendaryTool(Items.NETHERITE_PICKAXE, "Shadow Pickaxe", Formatting.BLACK,
            "Darkness Mining", "Invisible particles + blindness immunity", 10);
    }
    public static ItemStack createShadowAxe() {
        return createLegendaryTool(Items.NETHERITE_AXE, "Shadow Axe", Formatting.BLACK,
            "Umbral Strike", "Crits from behind, sneak attacks", 12);
    }
    public static ItemStack createShadowShovel() {
        return createLegendaryTool(Items.NETHERITE_SHOVEL, "Shadow Shovel", Formatting.BLACK,
            "Blackout", "Creates darkness around dig site", 8);
    }
    public static ItemStack createShadowHoe() {
        return createLegendaryTool(Items.NETHERITE_HOE, "Shadow Hoe", Formatting.BLACK,
            "Nightshade", "Poisonous crops damage enemies", 7);
    }

    // Tool Set 15: Crystal Tools - Gem/Shiny themed
    public static ItemStack createCrystalPickaxe() {
        return createLegendaryTool(Items.DIAMOND_PICKAXE, "Crystal Pickaxe", Formatting.WHITE,
            "Prismatic Mining", "Finds gems + fortune boost", 9);
    }
    public static ItemStack createCrystalAxe() {
        return createLegendaryTool(Items.DIAMOND_AXE, "Crystal Axe", Formatting.WHITE,
            "Shimmering Edge", "Reflects projectiles when blocking", 11);
    }
    public static ItemStack createCrystalShovel() {
        return createLegendaryTool(Items.DIAMOND_SHOVEL, "Crystal Shove", Formatting.WHITE,
            "Gem Detector", "Highlights ores through walls briefly", 7);
    }
    public static ItemStack createCrystalHoe() {
        return createLegendaryTool(Items.DIAMOND_HOE, "Crystal Hoe", Formatting.WHITE,
            "Diamond Touch", "Crops have chance to drop diamonds", 6);
    }

    // Helper method to create a legendary tool with standard formatting
    private static ItemStack createLegendaryTool(Item baseItem, String name, Formatting color,
                                                  String ability, String description, int damage) {
        ItemStack stack = new ItemStack(baseItem);

        // Custom name with glow
        stack.set(DataComponentTypes.CUSTOM_NAME,
            Text.literal("✦ " + name).formatted(color, Formatting.BOLD));

        // Lore with ability info
        stack.set(DataComponentTypes.LORE, new LoreComponent(Arrays.asList(
            Text.literal(""),
            Text.literal("◆ LEGENDARY TOOL ◆").formatted(color, Formatting.BOLD),
            Text.literal(""),
            Text.literal("Ability: " + ability).formatted(Formatting.YELLOW),
            Text.literal("└ " + description).formatted(Formatting.GRAY),
            Text.literal(""),
            Text.literal("Damage: " + damage).formatted(Formatting.RED),
            Text.literal("Unbreakable").formatted(Formatting.GREEN),
            Text.literal(""),
            Text.literal("「Legendary」").formatted(color)
        )));

        // Make unbreakable and add enchant glow
        stack.set(DataComponentTypes.MAX_DAMAGE, Integer.MAX_VALUE);
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);

        // Set custom ID
        String id = name.toLowerCase().replace("'", "").replace(" ", "_");
        SlayerItems.setCustomItemId(stack, id);

        return stack;
    }
}
