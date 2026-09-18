package com.political.recipe;

import com.political.DanielsPickaxe;
import com.political.DragonGearRecipeBootstrap;
import com.political.bounty.SlayerItems;
import com.political.bounty.SlayerManager;
import com.political.items.CustomItemHandler;
import com.political.items.DragonChestplates;
import com.political.items.LegendaryTools;
import com.political.technique.Technique;
import com.political.technique.TechniqueItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * Small default recipe list after HardcodedRecipes was removed:
 * slayer gear, T2, 8 dragon plates, 4 tool sets, cores, technique manuals, kept uniques.
 */
public final class DefaultRecipeBootstrap {

    private DefaultRecipeBootstrap() {}

    public static void ensureRecipes() {
        List<SlayerRecipes.Recipe> out = new ArrayList<>();
        out.addAll(DragonGearRecipeBootstrap.buildRecipes());
        addSlayerGear(out);
        addLegendaryTools(out);
        addTechniqueManuals(out);
        addCores(out);
        addKeptUniques(out);
        RecipeConfigManager.replaceAllCustomRecipes(out);
    }

    private static void addSlayerGear(List<SlayerRecipes.Recipe> out) {
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            ItemStack core = SlayerItems.createSlayerCore(type);
            ItemStack[] sword = grid(
                    Items.IRON_SWORD, core, Items.IRON_INGOT,
                    Items.IRON_INGOT, Items.IRON_INGOT, Items.IRON_INGOT,
                    Items.IRON_INGOT, Items.DIAMOND, Items.IRON_INGOT);
            out.add(shaped(type.displayName + " Slayer Sword", SlayerItems.createSlayerSword(type), sword, type, 1));

            ItemStack[] helm = grid(
                    Items.IRON_INGOT, core, Items.IRON_INGOT,
                    Items.IRON_INGOT, Items.IRON_HELMET, Items.IRON_INGOT,
                    Items.LEATHER, Items.DIAMOND, Items.LEATHER);
            out.add(shaped(type.displayName + " Slayer Helmet", SlayerItems.createSlayerHelmet(type, 1), helm, type, 1));

            ItemStack[] chest = grid(
                    Items.IRON_INGOT, core, Items.IRON_INGOT,
                    Items.IRON_INGOT, Items.IRON_CHESTPLATE, Items.IRON_INGOT,
                    Items.LEATHER, Items.DIAMOND, Items.LEATHER);
            out.add(shaped(type.displayName + " Slayer Chestplate", SlayerItems.createSlayerChestplate(type, 1), chest, type, 1));

            ItemStack t2Core = new ItemStack(Items.NETHERITE_INGOT, 2);
            ItemStack[] t2 = grid(
                    Items.NETHERITE_INGOT, core, Items.NETHERITE_INGOT,
                    Items.DIAMOND, SlayerItems.createSlayerChestplate(type, 1), Items.DIAMOND,
                    Items.NETHERITE_INGOT, t2Core, Items.NETHERITE_INGOT);
            out.add(shaped(type.displayName + " T2 Chestplate", SlayerItems.createSlayerChestplate(type, 2), t2, type, 5));
        }
    }

    private static void addLegendaryTools(List<SlayerRecipes.Recipe> out) {
        out.add(shaped("Dragon Pickaxe", LegendaryTools.createDragonPickaxe(), grid(
                Items.MAGMA_BLOCK, Items.NETHER_STAR, Items.MAGMA_BLOCK,
                Items.BLAZE_ROD, Items.NETHERITE_PICKAXE, Items.BLAZE_ROD,
                Items.OBSIDIAN, Items.LAVA_BUCKET, Items.OBSIDIAN)));
        out.add(shaped("Dragon Axe", LegendaryTools.createDragonAxe(), grid(
                Items.MAGMA_BLOCK, Items.NETHER_STAR, Items.MAGMA_BLOCK,
                Items.BLAZE_ROD, Items.NETHERITE_AXE, Items.BLAZE_ROD,
                Items.OBSIDIAN, Items.LAVA_BUCKET, Items.OBSIDIAN)));
        out.add(shaped("Void Pickaxe", LegendaryTools.createVoidPickaxe(), grid(
                Items.ENDER_PEARL, Items.NETHER_STAR, Items.ENDER_PEARL,
                Items.OBSIDIAN, Items.NETHERITE_PICKAXE, Items.OBSIDIAN,
                Items.ENDER_EYE, Items.CHORUS_FRUIT, Items.ENDER_EYE)));
        out.add(shaped("Void Axe", LegendaryTools.createVoidAxe(), grid(
                Items.ENDER_PEARL, Items.NETHER_STAR, Items.ENDER_PEARL,
                Items.OBSIDIAN, Items.NETHERITE_AXE, Items.OBSIDIAN,
                Items.ENDER_EYE, Items.CHORUS_FRUIT, Items.ENDER_EYE)));
        out.add(shaped("Nature Axe", LegendaryTools.createNatureAxe(), grid(
                Items.OAK_SAPLING, Items.NETHER_STAR, Items.OAK_SAPLING,
                Items.BONE_MEAL, Items.DIAMOND_AXE, Items.BONE_MEAL,
                Items.MOSS_BLOCK, Items.GOLDEN_APPLE, Items.MOSS_BLOCK)));
        out.add(shaped("Nature Pickaxe", LegendaryTools.createNaturePickaxe(), grid(
                Items.OAK_SAPLING, Items.NETHER_STAR, Items.OAK_SAPLING,
                Items.BONE_MEAL, Items.DIAMOND_PICKAXE, Items.BONE_MEAL,
                Items.MOSS_BLOCK, Items.GOLDEN_APPLE, Items.MOSS_BLOCK)));
        out.add(shaped("Frost Pickaxe", LegendaryTools.createFrostPickaxe(), grid(
                Items.PACKED_ICE, Items.NETHER_STAR, Items.PACKED_ICE,
                Items.BLUE_ICE, Items.DIAMOND_PICKAXE, Items.BLUE_ICE,
                Items.SNOW_BLOCK, Items.POWDER_SNOW_BUCKET, Items.SNOW_BLOCK)));
        out.add(shaped("Frost Shovel", LegendaryTools.createFrostShovel(), grid(
                Items.PACKED_ICE, Items.NETHER_STAR, Items.PACKED_ICE,
                Items.BLUE_ICE, Items.DIAMOND_SHOVEL, Items.BLUE_ICE,
                Items.SNOW_BLOCK, Items.POWDER_SNOW_BUCKET, Items.SNOW_BLOCK)));
    }

    private static void addTechniqueManuals(List<SlayerRecipes.Recipe> out) {
        for (Technique technique : Technique.all()) {
            ItemStack[] g = grid(
                    Items.BOOK, Items.LAPIS_LAZULI, Items.BOOK,
                    Items.AMETHYST_SHARD, Items.NETHER_STAR, Items.AMETHYST_SHARD,
                    Items.PAPER, Items.EXPERIENCE_BOTTLE, Items.PAPER);
            out.add(shaped("Technique Manual: " + technique.displayName, TechniqueItems.createManual(technique), g));
        }
    }

    private static void addCores(List<SlayerRecipes.Recipe> out) {
        out.add(shaped("Custom Core T1", DragonChestplates.createCustomCoreT1(), grid(
                Items.IRON_INGOT, Items.GOLD_INGOT, Items.IRON_INGOT,
                Items.GOLD_INGOT, Items.DIAMOND, Items.GOLD_INGOT,
                Items.IRON_INGOT, Items.GOLD_INGOT, Items.IRON_INGOT)));
        out.add(shaped("Custom Core T2", DragonChestplates.createCustomCoreT2(), grid(
                Items.DIAMOND, Items.NETHERITE_SCRAP, Items.DIAMOND,
                Items.NETHERITE_SCRAP, DragonChestplates.createCustomCoreT1(), Items.NETHERITE_SCRAP,
                Items.DIAMOND, Items.NETHERITE_SCRAP, Items.DIAMOND)));
        out.add(shaped("Dragon Core T1", SlayerItems.createDragonCrafterCoreT1(), grid(
                Items.BLAZE_ROD, Items.GOLD_INGOT, Items.BLAZE_ROD,
                Items.GOLD_INGOT, Items.NETHER_STAR, Items.GOLD_INGOT,
                Items.BLAZE_ROD, Items.GOLD_INGOT, Items.BLAZE_ROD)));
        out.add(shaped("Dragon Core T2", SlayerItems.createDragonCrafterCoreT2(), grid(
                Items.DIAMOND, Items.NETHERITE_SCRAP, Items.DIAMOND,
                Items.NETHERITE_SCRAP, SlayerItems.createDragonCrafterCoreT1(), Items.NETHERITE_SCRAP,
                Items.DIAMOND, Items.NETHERITE_SCRAP, Items.DIAMOND)));
        out.add(shaped("Dragon Core T3", SlayerItems.createDragonCrafterCoreT3(), grid(
                Items.NETHERITE_INGOT, Items.NETHER_STAR, Items.NETHERITE_INGOT,
                Items.DIAMOND, SlayerItems.createDragonCrafterCoreT2(), Items.DIAMOND,
                Items.NETHERITE_INGOT, Items.DRAGON_BREATH, Items.NETHERITE_INGOT)));
    }

    private static void addKeptUniques(List<SlayerRecipes.Recipe> out) {
        out.add(shaped("Daniel's Pickaxe", DanielsPickaxe.create(), grid(
                Items.NETHERITE_INGOT, Items.NETHER_STAR, Items.NETHERITE_INGOT,
                Items.DIAMOND, Items.NETHERITE_PICKAXE, Items.DIAMOND,
                Items.OBSIDIAN, Items.END_CRYSTAL, Items.OBSIDIAN)));
        out.add(shaped("Crown of Greed", SlayerItems.createCrownOfGreed(), grid(
                Items.GOLD_INGOT, Items.GOLD_BLOCK, Items.GOLD_INGOT,
                Items.GOLD_INGOT, Items.GOLDEN_HELMET, Items.GOLD_INGOT,
                Items.EMERALD, Items.NETHER_STAR, Items.EMERALD)));
        out.add(shaped("Crown of Midas", SlayerItems.createCrownOfMidas(), grid(
                Items.GOLD_BLOCK, Items.NETHER_STAR, Items.GOLD_BLOCK,
                Items.GOLD_INGOT, SlayerItems.createCrownOfGreed(), Items.GOLD_INGOT,
                Items.GOLD_BLOCK, Items.TOTEM_OF_UNDYING, Items.GOLD_BLOCK)));
        out.add(shaped("Midas Sword", SlayerItems.createMidasSword(), grid(
                Items.GOLD_INGOT, Items.NETHER_STAR, Items.GOLD_INGOT,
                Items.GOLD_INGOT, Items.GOLDEN_SWORD, Items.GOLD_INGOT,
                Items.GOLD_BLOCK, Items.DIAMOND, Items.GOLD_BLOCK)));
        out.add(shaped("Harvey's Stick", CustomItemHandler.createHarveysStick(), grid(
                Items.COPPER_INGOT, Items.LIGHTNING_ROD, Items.COPPER_INGOT,
                Items.COPPER_INGOT, Items.STICK, Items.COPPER_INGOT,
                Items.COPPER_BLOCK, Items.NETHER_STAR, Items.COPPER_BLOCK)));
        out.add(shaped("Hermes Shoes", CustomItemHandler.createHermesShoes(), grid(
                Items.FEATHER, Items.SUGAR, Items.FEATHER,
                Items.GOLD_INGOT, Items.IRON_BOOTS, Items.GOLD_INGOT,
                Items.FEATHER, Items.NETHER_STAR, Items.FEATHER)));
        out.add(shaped("Rift Mirror", CustomItemHandler.createRiftMirror(), grid(
                Items.ENDER_PEARL, Items.AMETHYST_SHARD, Items.ENDER_PEARL,
                Items.ENDER_EYE, Items.GLASS_PANE, Items.ENDER_EYE,
                Items.OBSIDIAN, Items.NETHER_STAR, Items.OBSIDIAN)));
        out.add(shaped("Ember Signet", CustomItemHandler.createEmberSignet(), grid(
                Items.BLAZE_POWDER, Items.MAGMA_CREAM, Items.BLAZE_POWDER,
                Items.GOLD_INGOT, Items.FIRE_CHARGE, Items.GOLD_INGOT,
                Items.BLAZE_POWDER, Items.NETHER_STAR, Items.BLAZE_POWDER)));
        out.add(shaped("Frostbound Talisman", CustomItemHandler.createFrostboundTalisman(), grid(
                Items.SNOWBALL, Items.PACKED_ICE, Items.SNOWBALL,
                Items.BLUE_ICE, Items.HEART_OF_THE_SEA, Items.BLUE_ICE,
                Items.SNOWBALL, Items.NETHER_STAR, Items.SNOWBALL)));
        out.add(shaped("Echo Chime", CustomItemHandler.createEchoChime(), grid(
                Items.ECHO_SHARD, Items.BELL, Items.ECHO_SHARD,
                Items.AMETHYST_SHARD, Items.GOAT_HORN, Items.AMETHYST_SHARD,
                Items.ECHO_SHARD, Items.NETHER_STAR, Items.ECHO_SHARD)));
        out.add(shaped("Terra Relay", CustomItemHandler.createTerraRelay(), grid(
                Items.MOSS_BLOCK, Items.OAK_SAPLING, Items.MOSS_BLOCK,
                Items.BONE_MEAL, Items.HEART_OF_THE_SEA, Items.BONE_MEAL,
                Items.DIRT, Items.NETHER_STAR, Items.DIRT)));
    }

    private static SlayerRecipes.Recipe shaped(String name, ItemStack result, ItemStack[] grid) {
        return shaped(name, result, grid, null, 0);
    }

    private static SlayerRecipes.Recipe shaped(String name, ItemStack result, ItemStack[] grid,
                                               SlayerManager.SlayerType type, int level) {
        boolean[] locked = new boolean[9];
        java.util.Arrays.fill(locked, true);
        return new SlayerRecipes.Recipe(name, result, grid, locked, type, level);
    }

    private static ItemStack[] grid(Object a, Object b, Object c, Object d, Object e, Object f, Object g, Object h, Object i) {
        return new ItemStack[]{stack(a), stack(b), stack(c), stack(d), stack(e), stack(f), stack(g), stack(h), stack(i)};
    }

    private static ItemStack stack(Object o) {
        if (o instanceof ItemStack s) return s;
        if (o instanceof net.minecraft.world.item.Item item) return new ItemStack(item);
        return ItemStack.EMPTY;
    }
}
