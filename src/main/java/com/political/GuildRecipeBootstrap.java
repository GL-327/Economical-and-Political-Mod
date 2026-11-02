package com.political;


import com.political.recipe.RecipeConfigManager;
import com.political.recipe.SlayerRecipes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * Registers the V5 Pillar 2 recipes into /recipe and /setrecipe.
 * Recipe names are prefixed "V5 " so nothing existing is overwritten.
 */
public final class GuildRecipeBootstrap {

    private GuildRecipeBootstrap() {}

    public static void ensureRecipes() {
        List<SlayerRecipes.Recipe> out = new ArrayList<>();
        addGuildCharter(out);
        RecipeConfigManager.upsertCustomRecipesSilent(out);
    }

    private static void addGuildCharter(List<SlayerRecipes.Recipe> out) {
        // Deliberately expensive in materials: a guild is a server-visible institution.
        //   paper  gold   paper
        //   gold   emeraldblock  gold
        //   paper  ink    paper
        ItemStack[] grid = new ItemStack[9];
        grid[0] = new ItemStack(Items.PAPER);
        grid[1] = new ItemStack(Items.GOLD_INGOT);
        grid[2] = new ItemStack(Items.PAPER);
        grid[3] = new ItemStack(Items.GOLD_INGOT);
        grid[4] = new ItemStack(Items.EMERALD_BLOCK);
        grid[5] = new ItemStack(Items.GOLD_INGOT);
        grid[6] = new ItemStack(Items.PAPER);
        grid[7] = new ItemStack(Items.INK_SAC);
        grid[8] = new ItemStack(Items.PAPER);

        boolean[] locked = new boolean[9];
        for (int i = 0; i < 9; i++) locked[i] = true;

        out.add(new SlayerRecipes.Recipe("V5 Guild Charter",
                GuildItems.createGuildCharter(), grid, locked, null, 0));
    }
}
