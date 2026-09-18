package com.political;
import com.political.government.Treasury;



import com.political.items.CustomItemManager;
import com.political.recipe.RecipeConfigManager;
import com.political.recipe.SlayerRecipes;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Ensures treasury / world-economy recipes exist in {@code custom_recipes.json} (same pipeline as /setrecipe).
 */
public final class TreasuryBootstrap {
    private TreasuryBootstrap() {}

    public static void ensureRecipes() {
        CustomItemManager.CustomItem frag = CustomItemManager.getItem("treasury_fragment");
        CustomItemManager.CustomItem sigil = CustomItemManager.getItem("treasury_sigil");
        if (frag == null || sigil == null) return;

        ItemStack one = frag.createStack(1);
        ItemStack[] grid = new ItemStack[9];
        for (int i = 0; i < 9; i++) {
            grid[i] = one.copy();
        }
        boolean[] unlocked = new boolean[9];

        SlayerRecipes.Recipe recipe = new SlayerRecipes.Recipe(
                "Treasury Sigil (9 Fragments)",
                sigil.createStack(1),
                grid,
                unlocked,
                null,
                0
        );

        RecipeConfigManager.upsertCustomRecipesSilent(List.of(recipe));
    }
}
