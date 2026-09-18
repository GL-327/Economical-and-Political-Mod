package com.political.mixin;

import com.political.recipe.BountyCraftingHandler;
import com.political.recipe.SlayerRecipes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

/**
 * Blocks shift-click for custom recipes to prevent duplication issues.
 */
@Mixin(CraftingMenu.class)
public class CraftingShiftClickBlockerMixin {

    @Inject(method = "quickMoveStack", at = @At("HEAD"), cancellable = true)
    private void political_blockShiftClick(Player player, int slotIndex, CallbackInfoReturnable<ItemStack> cir) {
        AbstractContainerMenu handler = (AbstractContainerMenu) (Object) this;
        
        // Only block if clicking the crafting result slot (slot 0)
        if (slotIndex != 0) return;
        if (slotIndex < 0 || slotIndex >= handler.slots.size()) return;
        Slot slot = handler.slots.get(slotIndex);
        if (!(slot.container instanceof ResultContainer)) return;
        
        ItemStack resultStack = slot.getItem();
        if (resultStack.isEmpty()) return;
        
        // Check if this is a custom recipe - if so, block shift-click
        try {
            List<ItemStack> grid = new ArrayList<>();
            for (int i = 1; i <= 9 && i < handler.slots.size(); i++) {
                Slot craftSlot = handler.slots.get(i);
                grid.add(craftSlot.getItem());
            }
            if (grid.size() != 9) return;
            
            SlayerRecipes.Recipe matchedRecipe = BountyCraftingHandler.getMatchedCustomRecipe(grid, player);
            if (matchedRecipe != null) {
                // Block shift-click for custom recipes
                cir.setReturnValue(ItemStack.EMPTY);
            }
        } catch (Exception e) {
            // Fall through to vanilla
        }
    }
}
