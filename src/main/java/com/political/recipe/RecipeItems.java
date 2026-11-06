package com.political.recipe;

import com.political.bounty.SlayerItems;
import java.util.*;
import net.minecraft.world.item.ItemStack;

/** Category overlap should not create repeated search/index cards for one output. */
public final class RecipeItems {
    private RecipeItems() {}
    private record VanillaKey(Object item, Object components) {}

    public static List<ItemStack> unique(List<ItemStack> source) {
        Map<Object, ItemStack> items = new LinkedHashMap<>();
        for (ItemStack stack : source) {
            if (stack == null || stack.isEmpty()) continue;
            String id = SlayerItems.getCustomItemId(stack);
            Object key = id == null || id.isBlank() ? new VanillaKey(stack.getItem(), stack.getComponents()) : id;
            items.putIfAbsent(key, stack);
        }
        return new ArrayList<>(items.values());
    }

    /** Names may differ; only truly identical enabled alternatives are hidden. Saves are retained. */
    public static boolean sameRecipe(SlayerRecipes.Recipe a, SlayerRecipes.Recipe b) {
        if (a.requiredSlayer != b.requiredSlayer || a.requiredLevel != b.requiredLevel
                || !Arrays.equals(a.lockedSlots, b.lockedSlots) || !sameStack(a.result, b.result)) return false;
        if (a.ingredients == null || b.ingredients == null) return a.ingredients == b.ingredients;
        if (a.ingredients.length != b.ingredients.length) return false;
        for (int i = 0; i < a.ingredients.length; i++) if (!sameStack(a.ingredients[i], b.ingredients[i])) return false;
        return true;
    }

    private static boolean sameStack(ItemStack a, ItemStack b) {
        if (a == null || a.isEmpty()) return b == null || b.isEmpty();
        return b != null && !b.isEmpty() && a.getCount() == b.getCount() && ItemStack.isSameItemSameComponents(a, b);
    }
}
