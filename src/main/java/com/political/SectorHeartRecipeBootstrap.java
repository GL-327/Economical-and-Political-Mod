package com.political;


import com.political.recipe.RecipeConfigManager;
import com.political.recipe.SlayerRecipes;
import com.political.recipe.GearRecipes;
import com.political.bounty.SlayerItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/** Default §9“2.5 stack” §7lapis (160) recipes for sector / syndicate display items. */
public final class SectorHeartRecipeBootstrap {

    private SectorHeartRecipeBootstrap() {}

    public static void ensureRecipes() {
        List<SlayerRecipes.Recipe> out = new ArrayList<>();

        for (int t = 1; t <= 10; t++) {
            ItemStack core = t == 1 ? SlayerItems.createCustomCrafterCoreT1()
                    : t == 10 ? SlayerItems.createCustomCrafterCoreT3() : SlayerItems.createCustomCrafterCoreT2();
            ItemStack[] grid = GearRecipes.grid("leggings", new ItemStack(Items.LAPIS_LAZULI, 23), core);
            grid[1].setCount(22); // 6 * 23 + 22 = the existing 160 lapis cost, with paired sides
            if (t > 1) grid[7] = SectorHeartItems.createGardenOveralls(t - 1);
            out.add(new SlayerRecipes.Recipe("Verdant Overalls " + t + " (160 Lapis)",
                    SectorHeartItems.createGardenOveralls(t), grid, GearRecipes.locked(), null, 0));
        }
        ItemStack[] charter = new ItemStack[9];
        java.util.Arrays.setAll(charter, i -> new ItemStack(Items.LAPIS_LAZULI, 20));
        charter[4] = SlayerItems.createCustomCrafterCoreT2();
        out.add(new SlayerRecipes.Recipe("Syndicate Charter display (160 Lapis)",
                SectorHeartItems.createSyndicateCharter(), charter, GearRecipes.locked(), null, 0));

        if (!out.isEmpty()) {
            RecipeConfigManager.upsertCustomRecipesSilent(out);
        }
    }
}
