package com.political.mixin;

import com.political.BeamCraftingHandler;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(CraftingScreenHandler.class)
public class CraftingResultMixin {

    @Inject(method = "updateResult", at = @At("TAIL"))
    private static void political_checkBeamRecipe(
            ScreenHandler handler,
            ServerWorld world,
            PlayerEntity player,
            RecipeInputInventory craftingInventory,
            CraftingResultInventory resultInventory,
            RecipeEntry<CraftingRecipe> recipe,
            CallbackInfo ci) {

        ItemStack currentResult = resultInventory.getStack(0);
        if (!currentResult.isEmpty()) return;

        List<ItemStack> grid = new ArrayList<>();
        for (int i = 0; i < craftingInventory.size(); i++) {
            grid.add(craftingInventory.getStack(i));
        }

        ItemStack beamResult = BeamCraftingHandler.checkBeamRecipe(grid);
        if (beamResult != null) {
            resultInventory.setStack(0, beamResult);
        }
    }
}