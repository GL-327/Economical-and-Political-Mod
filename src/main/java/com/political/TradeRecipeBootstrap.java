package com.political;


import com.political.recipe.RecipeConfigManager;
import com.political.recipe.SlayerRecipes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * Registers the craftable V5 Pillar 3 items into {@code custom_recipes.json} so they appear in
 * {@code /recipe} and {@code /setrecipe} like every other custom recipe.
 *
 * <p>Recipe names are the unique upsert key, so every name here is prefixed {@code "V5 "}.</p>
 *
 * <p><b>Deliberately NOT craftable:</b> the Letter of Marque. It exists only as a 25% drop from
 * plundering a caravan, so the ability to raid neutral traders profitably has to be earned by
 * raiding first, and can never be mass-produced.</p>
 */
public final class TradeRecipeBootstrap {

    private TradeRecipeBootstrap() {}

    /** Call from SERVER_STARTED, after RecipeConfigManager has loaded. */
    public static void ensureRecipes() {
        List<SlayerRecipes.Recipe> out = new ArrayList<>();
        out.add(charterRecipe());
        out.add(sealRecipe());
        if (!out.isEmpty()) {
            RecipeConfigManager.upsertCustomRecipesSilent(out);
        }
    }

    private static SlayerRecipes.Recipe charterRecipe() {
        Item[] layout = {
                Items.PAPER,   Items.LEATHER, Items.PAPER,
                Items.INK_SAC, Items.EMERALD, Items.INK_SAC,
                Items.PAPER,   Items.LEATHER, Items.PAPER
        };
        return positional("V5 Caravan Charter", TradeItems.createCharter(), layout);
    }

    private static SlayerRecipes.Recipe sealRecipe() {
        Item[] layout = {
                Items.HONEYCOMB, Items.GOLD_INGOT, Items.HONEYCOMB,
                Items.GOLD_INGOT, Items.DIAMOND,   Items.GOLD_INGOT,
                Items.HONEYCOMB, Items.GOLD_INGOT, Items.HONEYCOMB
        };
        return positional("V5 Merchant's Seal", TradeItems.createMerchantSeal(), layout);
    }

    private static SlayerRecipes.Recipe positional(String name, ItemStack result, Item[] layout) {
        ItemStack[] grid = new ItemStack[9];
        boolean[] locked = new boolean[9];
        for (int i = 0; i < 9; i++) {
            if (layout[i] == null) {
                grid[i] = ItemStack.EMPTY;
            } else {
                grid[i] = new ItemStack(layout[i]);
                locked[i] = true;
            }
        }
        return new SlayerRecipes.Recipe(name, result, grid, locked, null, 0);
    }
}
