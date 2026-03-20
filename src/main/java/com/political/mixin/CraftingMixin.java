package com.political.mixin;

import com.political.BeamCraftingHandler;
import com.political.BountyCraftingHandler;
import com.political.CustomItemHandler;
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
import java.util.List;

@Mixin(CraftingScreenHandler.class)
public class CraftingMixin {

    @Inject(method = "updateResult", at = @At("HEAD"), cancellable = true)
    private static void preventCustomItemCrafting(ScreenHandler handler, ServerWorld world,
                                                  net.minecraft.entity.player.PlayerEntity player, RecipeInputInventory craftingInventory,
                                                  CraftingResultInventory resultInventory, RecipeEntry<CraftingRecipe> recipe,
                                                  CallbackInfo ci) {

        List<ItemStack> grid = new ArrayList<>();
        for (int i = 0; i < craftingInventory.size(); i++) {
            grid.add(craftingInventory.getStack(i));
        }

        // Check for custom recipes first - these need full control
        com.political.SlayerRecipes.Recipe matchedRecipe = BountyCraftingHandler.getMatchedCustomRecipe(grid, player);
        if (matchedRecipe != null) {
            // Custom recipe found - set result and CANCEL to prevent vanilla interference
            resultInventory.setStack(0, matchedRecipe.result.copy());
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

    private static boolean isNormalSword(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (CustomItemHandler.isProtectedItem(stack)) return false;
        return stack.isOf(Items.IRON_SWORD) ||
                stack.isOf(Items.DIAMOND_SWORD) ||
                stack.isOf(Items.NETHERITE_SWORD);
    }

    private static boolean isNormalShovel(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (CustomItemHandler.isProtectedItem(stack)) return false;
        return stack.isOf(Items.IRON_SHOVEL) ||
                stack.isOf(Items.DIAMOND_SHOVEL) ||
                stack.isOf(Items.NETHERITE_SHOVEL);
    }
}