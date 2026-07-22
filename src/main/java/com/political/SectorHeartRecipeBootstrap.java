package com.political;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/** Default §9“2.5 stack” §7lapis (160) recipes for sector / syndicate display items. */
public final class SectorHeartRecipeBootstrap {

    private SectorHeartRecipeBootstrap() {}

    public static void ensureRecipes() {
        List<SlayerRecipes.Recipe> out = new ArrayList<>();

        ItemStack[] lapisGrid = new ItemStack[9];
        lapisGrid[0] = new ItemStack(Items.LAPIS_LAZULI, 64);
        lapisGrid[1] = new ItemStack(Items.LAPIS_LAZULI, 64);
        lapisGrid[2] = new ItemStack(Items.LAPIS_LAZULI, 32);
        for (int i = 3; i < 9; i++) {
            lapisGrid[i] = ItemStack.EMPTY;
        }
        boolean[] u = new boolean[9];

        for (int t = 1; t <= 10; t++) {
            out.add(new SlayerRecipes.Recipe("Verdant Overalls " + t + " (160 Lapis)", SectorHeartItems.createGardenOveralls(t), lapisGrid, u, null, 0));
        }
        out.add(new SlayerRecipes.Recipe("Syndicate Charter display (160 Lapis)", SectorHeartItems.createSyndicateCharter(), lapisGrid, u, null, 0));

        if (!out.isEmpty()) {
            RecipeConfigManager.upsertCustomRecipesSilent(out);
        }
    }
}
