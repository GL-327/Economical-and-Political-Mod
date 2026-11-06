package com.political.recipe;

import com.political.ItemRegistryIds;
import com.political.bounty.SlayerItems;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** One exact ingredient/count/gate check for menu crafting. Runs on the server thread. */
public final class RecipeInventory {
    private RecipeInventory() {}

    public static String key(ItemStack stack) {
        String custom = SlayerItems.getCustomItemId(stack);
        return custom == null || custom.isBlank() ? ItemRegistryIds.asString(stack.getItem()) : "custom:" + custom;
    }

    private static Optional<List<IngredientPlan.Take>> plan(ServerPlayer player, SlayerRecipes.Recipe recipe) {
        if (recipe == null || recipe.result == null || recipe.result.isEmpty() || recipe.ingredients == null
                || !RecipeConfigManager.isRecipeEnabled(recipe.name) || !BountyCraftingHandler.meetsRecipeGate(player, recipe)) return Optional.empty();
        List<IngredientPlan.Stack> required = new ArrayList<>();
        for (ItemStack ingredient : recipe.ingredients) if (ingredient != null && !ingredient.isEmpty())
            required.add(new IngredientPlan.Stack(key(ingredient), ingredient.getCount()));
        if (required.isEmpty()) return Optional.empty();
        List<IngredientPlan.Stack> inventory = new ArrayList<>();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            inventory.add(stack.isEmpty() ? null : new IngredientPlan.Stack(key(stack), stack.getCount()));
        }
        return IngredientPlan.allocate(required, inventory);
    }

    public static boolean canCraft(ServerPlayer player, SlayerRecipes.Recipe recipe) { return plan(player, recipe).isPresent(); }

    public static boolean craft(ServerPlayer player, SlayerRecipes.Recipe recipe) {
        var planned = plan(player, recipe);
        if (planned.isEmpty()) return false;
        for (IngredientPlan.Take take : planned.get()) player.getInventory().getItem(take.slot()).shrink(take.count());
        player.getInventory().setChanged();
        ItemStack result = recipe.result.copy();
        if (!player.addItem(result)) player.drop(result, false, net.minecraft.util.Prediction.SERVER_ONLY);
        return true;
    }
}
