package com.political.mixin;

import com.political.BountyCraftingHandler;
import com.political.SlayerRecipes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

/**
 * Blocks shift-click for custom recipes to prevent duplication issues.
 */
@Mixin(CraftingScreenHandler.class)
public class CraftingShiftClickBlockerMixin {

    @Inject(method = "quickMove", at = @At("HEAD"), cancellable = true)
    private void political_blockShiftClick(PlayerEntity player, int slotIndex, CallbackInfoReturnable<ItemStack> cir) {
        ScreenHandler handler = (ScreenHandler) (Object) this;
        
        // Only block if clicking the crafting result slot (slot 0)
        if (slotIndex != 0) return;
        if (slotIndex < 0 || slotIndex >= handler.slots.size()) return;
        Slot slot = handler.slots.get(slotIndex);
        if (!(slot.inventory instanceof CraftingResultInventory)) return;
        
        ItemStack resultStack = slot.getStack();
        if (resultStack.isEmpty()) return;
        
        // Check if this is a custom recipe - if so, block shift-click
        try {
            List<ItemStack> grid = new ArrayList<>();
            for (int i = 1; i <= 9 && i < handler.slots.size(); i++) {
                Slot craftSlot = handler.slots.get(i);
                grid.add(craftSlot.getStack());
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
