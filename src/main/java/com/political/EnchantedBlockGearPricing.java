package com.political;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
/**
 * Estimates economy sell price for enchanted block gear from its non-enchanted recipe + 160 lapis.
 */
public final class EnchantedBlockGearPricing {

    private EnchantedBlockGearPricing() {}

    public static int defaultSellFromCraft(ItemStack enchantedVariant) {
        String eid = SlayerItems.getCustomItemId(enchantedVariant);
        if (eid == null || !eid.endsWith("_sbl_ench")) {
            return 100;
        }
        String baseId = eid.substring(0, eid.length() - "_sbl_ench".length());
        ItemStack probe = enchantedVariant.copy();
        SlayerItems.setCustomItemId(probe, baseId);

        SlayerRecipes.Recipe recipe = RecipeOutputIndex.firstRecipeForOutput(probe);
        int sum = 0;
        if (recipe != null && recipe.ingredients != null) {
            for (ItemStack ing : recipe.ingredients) {
                if (ing == null || ing.isEmpty()) continue;
                sum += ingredientValue(ing);
            }
        } else {
            sum = 500;
        }
        int lapisUnit = ShopManager.getSellPrice(Items.LAPIS_LAZULI);
        sum += 160 * Math.max(1, lapisUnit);
        return Math.max(50, sum);
    }

    private static int ingredientValue(ItemStack ing) {
        String cid = SlayerItems.getCustomItemId(ing);
        if (cid != null) {
            ItemStack p = ing.copy();
            SlayerItems.setCustomItemId(p, cid);
            SlayerRecipes.Recipe sub = RecipeOutputIndex.firstRecipeForOutput(p);
            if (sub != null && sub.ingredients != null) {
                int s = 0;
                for (ItemStack x : sub.ingredients) {
                    if (x != null && !x.isEmpty()) {
                        s += ingredientValue(x);
                    }
                }
                return Math.max(1, s);
            }
            return 25;
        }
        return ShopManager.getSellPrice(ing.getItem()) * Math.max(1, ing.getCount());
    }
}
