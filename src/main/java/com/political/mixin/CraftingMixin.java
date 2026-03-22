package com.political.mixin;

import com.political.BeamCraftingHandler;
import com.political.BountyCraftingHandler;
import com.political.CustomItemHandler;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
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
import java.util.List;

@Mixin(CraftingScreenHandler.class)
public class CraftingMixin {

    @Inject(method = "updateResult", at = @At("HEAD"), cancellable = true)
    private static void onUpdateResult(ScreenHandler handler, ServerWorld world,
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

        // If grid is empty, let vanilla handle it
        if (!hasAnyItem) {
            return;
        }

        // Check for custom recipes from /setrecipe
        com.political.SlayerRecipes.Recipe matchedRecipe = BountyCraftingHandler.getMatchedCustomRecipe(grid, player);
        if (matchedRecipe != null) {
            // Custom recipe found - set result
            resultInventory.setStack(0, matchedRecipe.result.copy());
            ci.cancel();
            return;
        }

        // Check for protected items - if present, check for beam/bounty recipes
        boolean hasProtectedItem = false;
        for (int i = 0; i < craftingInventory.size(); i++) {
            ItemStack stack = craftingInventory.getStack(i);
            if (stack.isEmpty()) continue;
            if (CustomItemHandler.isProtectedItem(stack)) {
                hasProtectedItem = true;
                break;
            }
        }

        // If protected items present, check for valid recipes
        if (hasProtectedItem) {
            if (BeamCraftingHandler.checkBeamRecipe(grid) != null) return;
            if (BountyCraftingHandler.checkBountyGridRecipe(grid, player) != null) return;

            // No valid recipe for protected items - block
            resultInventory.setStack(0, ItemStack.EMPTY);
            ci.cancel();
            return;
        }

        // No protected items and no custom recipe - let VANILLA handle it normally
        // DO NOT cancel - let vanilla crafting work!
    }

}
