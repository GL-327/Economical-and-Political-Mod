package com.political;

import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** All custom recipes that produce the same item (by stack equality or custom id). */
public final class RecipeOutputIndex {

    private RecipeOutputIndex() {}

    public static List<SlayerRecipes.Recipe> listRecipesForOutput(ItemStack output) {
        List<SlayerRecipes.Recipe> out = new ArrayList<>();
        if (output == null || output.isEmpty()) {
            return out;
        }
        String oid = SlayerItems.getCustomItemId(output);
        for (SlayerRecipes.Recipe r : RecipeConfigManager.getRecipes()) {
            if (r == null || r.result == null || r.result.isEmpty()) continue;
            if (!RecipeConfigManager.isRecipeEnabled(r.name)) continue;
            if (ItemStack.isSameItemSameComponents(r.result, output)) {
                out.add(r);
                continue;
            }
            if (oid != null) {
                String rid = SlayerItems.getCustomItemId(r.result);
                if (oid.equals(rid)) {
                    out.add(r);
                }
            }
        }
        return out;
    }

    public static SlayerRecipes.Recipe firstRecipeForOutput(ItemStack output) {
        List<SlayerRecipes.Recipe> list = listRecipesForOutput(output);
        return list.isEmpty() ? null : list.get(0);
    }
}
