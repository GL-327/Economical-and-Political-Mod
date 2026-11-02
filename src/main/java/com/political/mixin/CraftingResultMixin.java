package com.political.mixin;

import com.political.BeamCraftingHandler;
import com.political.recipe.BountyCraftingHandler;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(CraftingMenu.class)
public class CraftingResultMixin {

    @Inject(
            method = "slotChangedCraftingGrid(Lnet/minecraft/world/inventory/AbstractContainerMenu;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/inventory/CraftingContainer;Lnet/minecraft/world/inventory/ResultContainer;Lnet/minecraft/world/item/crafting/RecipeHolder;)V",
            at = @At("TAIL"))
    private static void political_checkCustomRecipes(
            AbstractContainerMenu handler,
            ServerLevel world,
            Player player,
            CraftingContainer craftingInventory,
            ResultContainer resultInventory,
            RecipeHolder<CraftingRecipe> recipe,
            CallbackInfo ci) {

        ItemStack currentResult = resultInventory.getItem(0);
        if (!currentResult.isEmpty()) {
            return;
        }

        List<ItemStack> grid = new ArrayList<>();
        for (int i = 0; i < craftingInventory.getContainerSize(); i++) {
            grid.add(craftingInventory.getItem(i));
        }

        ItemStack beamResult = BeamCraftingHandler.checkBeamRecipe(grid);
        if (beamResult != null) {
            resultInventory.setItem(0, beamResult);
            return;
        }

        ItemStack bountyResult = BountyCraftingHandler.checkBountyGridRecipe(grid, player);
        if (bountyResult != null) {
            resultInventory.setItem(0, bountyResult);
        }
    }
}
