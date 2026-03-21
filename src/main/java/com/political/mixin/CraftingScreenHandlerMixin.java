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
 * Handles shift-click (quickMove) for custom recipes to prevent duplication.
 */
@Mixin(CraftingScreenHandler.class)
public class CraftingScreenHandlerMixin {

    @Inject(method = "quickMove", at = @At("HEAD"), cancellable = true)
    private void political_quickMove(PlayerEntity player, int slotIndex, CallbackInfoReturnable<ItemStack> cir) {
        ScreenHandler handler = (ScreenHandler) (Object) this;
        
        // Get the slot being shift-clicked
        if (slotIndex < 0 || slotIndex >= handler.slots.size()) return;
        Slot slot = handler.slots.get(slotIndex);
        
        // Only intercept if it's the crafting result slot (slot 0 in crafting screen)
        if (slotIndex != 0) return;
        if (!(slot.inventory instanceof CraftingResultInventory)) return;
        
        ItemStack resultStack = slot.getStack();
        if (resultStack.isEmpty()) return;
        
        // We need to check if this is a custom recipe
        // Access the crafting input via reflection since it's private in CraftingScreenHandler
        try {
            // Get the crafting inventory (slot 1-9 are crafting grid in vanilla order)
            // The crafting input is usually in slots 1-9
            List<ItemStack> grid = new ArrayList<>();
            for (int i = 1; i <= 9 && i < handler.slots.size(); i++) {
                Slot craftSlot = handler.slots.get(i);
                grid.add(craftSlot.getStack());
            }
            if (grid.size() != 9) return;
            
            SlayerRecipes.Recipe matchedRecipe = BountyCraftingHandler.getMatchedCustomRecipe(grid, player);
            if (matchedRecipe != null) {
                // This is a custom recipe - handle shift-click manually
                // Copy the result to give to player
                ItemStack resultCopy = resultStack.copy();
                
                // Try to add to player inventory
                PlayerEntity playerInv = player;
                boolean added = playerInv.getInventory().insertStack(resultCopy);
                
                if (added) {
                    // Successfully added - now consume ingredients
                    slot.setStack(ItemStack.EMPTY);
                    
                    // Consume ingredients based on recipe requirements
                    consumeIngredientsForRecipe(handler, matchedRecipe);
                    
                    // Return the original result count
                    cir.setReturnValue(resultStack);
                    return;
                }
                
                // Couldn't add to inventory - don't do anything
                cir.setReturnValue(ItemStack.EMPTY);
            }
        } catch (Exception e) {
            // Fall through to vanilla
        }
    }
    
    private void consumeIngredientsForRecipe(ScreenHandler handler, SlayerRecipes.Recipe recipe) {
        if (recipe.ingredients == null) return;
        
        java.util.Map<String, Integer> required = new java.util.HashMap<>();
        for (ItemStack ing : recipe.ingredients) {
            if (ing == null || ing.isEmpty()) continue;
            String key = getIngredientKey(ing);
            required.merge(key, ing.getCount(), Integer::sum);
        }
        
        // Slots 1-9 are the crafting grid
        for (int i = 1; i <= 9 && i < handler.slots.size(); i++) {
            Slot craftSlot = handler.slots.get(i);
            ItemStack slotStack = craftSlot.getStack();
            if (slotStack.isEmpty()) continue;
            
            String key = getIngredientKey(slotStack);
            Integer needed = required.get(key);
            if (needed == null || needed <= 0) continue;
            
            int take = Math.min(needed, slotStack.getCount());
            slotStack.decrement(take);
            needed -= take;
            
            if (needed <= 0) required.remove(key);
            else required.put(key, needed);
            
            if (required.isEmpty()) break;
        }
    }
    
    private static String getIngredientKey(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return "";
        String customId = com.political.SlayerItems.getCustomItemId(stack);
        if (customId != null) return "political:" + customId;
        return net.minecraft.registry.Registries.ITEM.getId(stack.getItem()).toString();
    }
}
