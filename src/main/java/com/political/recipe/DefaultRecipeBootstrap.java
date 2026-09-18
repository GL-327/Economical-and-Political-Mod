package com.political.recipe;

import com.political.items.DragonChestplates;
import com.political.items.LegendaryTools;
import com.political.recipe.RecipeConfigManager;
import com.political.bounty.SlayerItems;
import com.political.bounty.SlayerManager;
import com.political.recipe.SlayerRecipes;
import com.political.technique.Technique;
import com.political.technique.TechniqueItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * Small default recipe list after HardcodedRecipes was removed:
 * slayer gear, T2, 8 dragon plates, 4 tool sets, cores, technique manuals.
 */
public final class DefaultRecipeBootstrap {

    private DefaultRecipeBootstrap() {}

    public static void ensureRecipes() {
        com.political.DragonGearRecipeBootstrap.ensureRecipes();
        List<SlayerRecipes.Recipe> out = new ArrayList<>();
        addSlayerGear(out);
        addLegendaryTools(out);
        addTechniqueManuals(out);
        addCores(out);
        if (!out.isEmpty()) {
            RecipeConfigManager.upsertCustomRecipesSilent(out);
        }
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
