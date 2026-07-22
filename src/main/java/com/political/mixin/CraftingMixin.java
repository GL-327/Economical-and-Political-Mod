package com.political.mixin;

import com.political.BeamCraftingHandler;
import com.political.BountyCraftingHandler;
import com.political.CustomItemHandler;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(CraftingMenu.class)
public class CraftingMixin {

    @Inject(method = "slotChangedCraftingGrid(Lnet/minecraft/world/inventory/AbstractContainerMenu;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/inventory/CraftingContainer;Lnet/minecraft/world/inventory/ResultContainer;Lnet/minecraft/world/item/crafting/RecipeHolder;)V", at = @At("HEAD"), cancellable = true)
    private static void onSlotChangedCraftingGrid(AbstractContainerMenu handler, ServerLevel world,
                                                  net.minecraft.world.entity.player.Player player, CraftingContainer craftingInventory,
                                                  ResultContainer resultInventory, RecipeHolder<CraftingRecipe> recipe,
                                                  CallbackInfo ci) {

        List<ItemStack> grid = new ArrayList<>();
        boolean hasAnyItem = false;
        for (int i = 0; i < craftingInventory.getContainerSize(); i++) {
            ItemStack stack = craftingInventory.getItem(i);
            grid.add(stack);
            if (!stack.isEmpty()) hasAnyItem = true;
        }

        if (!hasAnyItem) {
            return;
        }

        com.political.SlayerRecipes.Recipe matchedRecipe = BountyCraftingHandler.getMatchedCustomRecipe(grid, player);
        if (matchedRecipe != null) {
            resultInventory.setItem(0, matchedRecipe.result.copy());
            ci.cancel();
            return;
        }

        boolean hasProtectedItem = false;
        for (int i = 0; i < craftingInventory.getContainerSize(); i++) {
            ItemStack stack = craftingInventory.getItem(i);
            if (stack.isEmpty()) continue;
            if (CustomItemHandler.isProtectedItem(stack)) {
                hasProtectedItem = true;
                break;
            }
        }

        if (hasProtectedItem) {
            if (BeamCraftingHandler.checkBeamRecipe(grid) != null) return;
            if (BountyCraftingHandler.checkBountyGridRecipe(grid, player) != null) return;

            resultInventory.setItem(0, ItemStack.EMPTY);
            ci.cancel();
        }
    }

}
