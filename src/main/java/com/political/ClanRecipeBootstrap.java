package com.political;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * Registers the V7 Pillar A recipe (the Clan Seal) into /recipe and /setrecipe.
 * Recipe names are prefixed "V7 " so nothing existing is overwritten.
 */
public final class ClanRecipeBootstrap {

    private ClanRecipeBootstrap() {}

    public static void ensureRecipes() {
        List<SlayerRecipes.Recipe> out = new ArrayList<>();
        addClanSeal(out);
        RecipeConfigManager.upsertCustomRecipesSilent(out);
    }

    private static void addClanSeal(List<SlayerRecipes.Recipe> out) {
        // Deliberately expensive: a clan is an inheritable, server-visible bloodline.
        //   skull   goldblock  skull
        //   goldblock  netherstar  goldblock
        //   skull   godapple   skull
        ItemStack[] grid = new ItemStack[9];
        grid[0] = new ItemStack(Items.WITHER_SKELETON_SKULL);
        grid[1] = new ItemStack(Items.GOLD_BLOCK);
        grid[2] = new ItemStack(Items.WITHER_SKELETON_SKULL);
        grid[3] = new ItemStack(Items.GOLD_BLOCK);
        grid[4] = new ItemStack(Items.NETHER_STAR);
        grid[5] = new ItemStack(Items.GOLD_BLOCK);
        grid[6] = new ItemStack(Items.WITHER_SKELETON_SKULL);
        grid[7] = new ItemStack(Items.ENCHANTED_GOLDEN_APPLE);
        grid[8] = new ItemStack(Items.WITHER_SKELETON_SKULL);

        boolean[] locked = new boolean[9];
        for (int i = 0; i < 9; i++) locked[i] = true;

        out.add(new SlayerRecipes.Recipe("V7 Clan Seal",
                ClanItems.createClanSeal(), grid, locked, null, 0));
    }
}
