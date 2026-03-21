package com.political.mixin;

import com.political.BeamCraftingHandler;
import com.political.BountyCraftingHandler;
import com.political.CustomItemHandler;
import com.political.SlayerItems;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(CraftingScreenHandler.class)
public class CraftingMixin {

    @Inject(method = "updateResult", at = @At("HEAD"), cancellable = true)
    private static void preventCustomItemCrafting(ScreenHandler handler, ServerWorld world,
                                                  net.minecraft.entity.player.PlayerEntity player, RecipeInputInventory craftingInventory,
                                                  CraftingResultInventory resultInventory, RecipeEntry<CraftingRecipe> recipe,
                                                  CallbackInfo ci) {

        List<ItemStack> grid = new ArrayList<>();
        boolean hasAnyItem = false;
        for (int i = 0; i < craftingInventory.size(); i++) {
            ItemStack stack = craftingInventory.getStack(i);
            grid.add(stack);
            if (!stack.isEmpty()) hasAnyItem = true;
        }

        // Block crafting if grid is completely empty
        if (!hasAnyItem) {
            resultInventory.setStack(0, ItemStack.EMPTY);
            ci.cancel();
            return;
        }

        // Check for custom recipes first - these need full control
        com.political.SlayerRecipes.Recipe matchedRecipe = BountyCraftingHandler.getMatchedCustomRecipe(grid, player);
        if (matchedRecipe != null) {
            // Custom recipe found - calculate max crafts and set result count
            ItemStack result = matchedRecipe.result.copy();
            int maxCrafts = calculateMaxCraftsForGrid(grid, matchedRecipe);
            if (maxCrafts > 0) {
                result.setCount(Math.min(maxCrafts, result.getMaxCount()));
            }
            resultInventory.setStack(0, result);
            ci.cancel();
            return;
        }

        // Check for protected items that shouldn't be used in vanilla recipes
        boolean hasProtectedItem = false;
        for (int i = 0; i < craftingInventory.size(); i++) {
            ItemStack stack = craftingInventory.getStack(i);
            if (stack.isEmpty()) continue;
            if (CustomItemHandler.isProtectedItem(stack)) {
                hasProtectedItem = true;
                break;
            }
        }

        // If protected items present but no valid custom recipe, block crafting
        if (hasProtectedItem) {
            // Check beam and bounty recipes
            if (BeamCraftingHandler.checkBeamRecipe(grid) != null) return;
            if (BountyCraftingHandler.checkBountyGridRecipe(grid, player) != null) return;

            // No valid recipe - block
            resultInventory.setStack(0, ItemStack.EMPTY);
            ci.cancel();
        }
    }

    private static int calculateMaxCraftsForGrid(List<ItemStack> grid, com.political.SlayerRecipes.Recipe recipe) {
        if (recipe.ingredients == null) return 0;

        Map<String, Integer> available = new HashMap<>();
        for (ItemStack slotStack : grid) {
            if (slotStack.isEmpty()) continue;
            String key = getIngredientKey(slotStack);
            available.merge(key, slotStack.getCount(), Integer::sum);
        }

        Map<String, Integer> required = new HashMap<>();
        for (ItemStack ing : recipe.ingredients) {
            if (ing == null || ing.isEmpty()) continue;
            String key = getIngredientKey(ing);
            required.merge(key, ing.getCount(), Integer::sum);
        }

        int maxCrafts = Integer.MAX_VALUE;
        for (Map.Entry<String, Integer> req : required.entrySet()) {
            Integer avail = available.get(req.getKey());
            if (avail == null) return 0;
            maxCrafts = Math.min(maxCrafts, avail / req.getValue());
        }

        return maxCrafts;
    }

    private static String getIngredientKey(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return "";
        String customId = SlayerItems.getCustomItemId(stack);
        if (customId != null) return "political:" + customId;
        return net.minecraft.registry.Registries.ITEM.getId(stack.getItem()).toString();
    }
}
