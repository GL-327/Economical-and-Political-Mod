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
import net.minecraft.text.Text;
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

        boolean hasProtectedItem = false;
        boolean hasNormalSword = false;
        boolean hasNormalShovel = false;
        boolean hasHPEBM = false;

        for (int i = 0; i < craftingInventory.size(); i++) {
            ItemStack stack = craftingInventory.getStack(i);
            if (stack.isEmpty()) continue;

            if (CustomItemHandler.isProtectedItem(stack)) {
                hasProtectedItem = true;
                if (CustomItemHandler.isHPEBM(stack)) hasHPEBM = true;
            }
            if (isNormalSword(stack)) hasNormalSword = true;
            if (isNormalShovel(stack)) hasNormalShovel = true;
        }

        // Prevent combining protected items with normal swords/shovels
        // or prevent protected items from being consumed in any vanilla recipe.
        // BUT: if the grid matches a valid custom recipe, let the TAIL mixin handle it.
        if (hasProtectedItem && (hasNormalSword || (hasHPEBM && hasNormalShovel))) {
            // Check if this grid matches a valid custom recipe before blocking
            List<ItemStack> grid = new ArrayList<>();
            for (int i = 0; i < craftingInventory.size(); i++) {
                grid.add(craftingInventory.getStack(i));
            }
            if (BeamCraftingHandler.checkBeamRecipe(grid) != null) return;
            if (BountyCraftingHandler.checkBountyGridRecipe(grid, player) != null) return;

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