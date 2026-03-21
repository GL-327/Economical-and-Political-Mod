package com.political.mixin;

import com.political.BeamCraftingHandler;
import com.political.BountyCraftingHandler;
import com.political.SlayerItems;
import com.political.SlayerRecipes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(CraftingResultSlot.class)
public class CraftingResultSlotMixin {

    @Shadow @Final private RecipeInputInventory input;

    @Inject(method = "onTakeItem", at = @At("HEAD"), cancellable = true)
    private void political_onTakeItem(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        if (player == null || stack == null || stack.isEmpty()) return;
        if (this.input == null) return;

        List<ItemStack> grid = new ArrayList<>();
        for (int i = 0; i < this.input.size(); i++) {
            grid.add(this.input.getStack(i));
        }
        if (grid.size() != 9) return;

        // Check for custom recipe - if found, set result count to max crafts and handle consumption
        SlayerRecipes.Recipe matchedCustom = BountyCraftingHandler.getMatchedCustomRecipe(grid, player);
        if (matchedCustom != null) {
            consumeForCustomRecipe(matchedCustom);
            refreshResult(player);
            ci.cancel();
            return;
        }

        ItemStack beam = BeamCraftingHandler.checkBeamRecipe(grid);
        if (beam != null) {
            decrementOneEachNonEmpty();
            refreshResult(player);
            ci.cancel();
            return;
        }

        ItemStack bounty = BountyCraftingHandler.checkBountyGridRecipe(grid, player);
        if (bounty != null) {
            decrementOneEachNonEmpty();
            refreshResult(player);
            ci.cancel();
        }
    }

    private void refreshResult(PlayerEntity player) {
        CraftingResultInventory resultInventory;
        try {
            resultInventory = (CraftingResultInventory) ((Slot) (Object) this).inventory;
        } catch (Exception e) {
            return;
        }

        List<ItemStack> newGrid = new ArrayList<>();
        for (int i = 0; i < this.input.size(); i++) {
            newGrid.add(this.input.getStack(i));
        }
        if (newGrid.size() != 9) {
            resultInventory.setStack(0, ItemStack.EMPTY);
            resultInventory.markDirty();
            return;
        }

        SlayerRecipes.Recipe matchedCustom = BountyCraftingHandler.getMatchedCustomRecipe(newGrid, player);
        if (matchedCustom != null) {
            resultInventory.setStack(0, matchedCustom.result.copy());
            resultInventory.markDirty();
            return;
        }

        ItemStack beam = BeamCraftingHandler.checkBeamRecipe(newGrid);
        if (beam != null) {
            resultInventory.setStack(0, beam);
            resultInventory.markDirty();
            return;
        }

        ItemStack bounty = BountyCraftingHandler.checkBountyGridRecipe(newGrid, player);
        if (bounty != null) {
            resultInventory.setStack(0, bounty);
            resultInventory.markDirty();
            return;
        }

        resultInventory.setStack(0, ItemStack.EMPTY);
        resultInventory.markDirty();
    }

    private void consumeForCustomRecipe(SlayerRecipes.Recipe recipe) {
        if (recipe.ingredients == null) return;

        Map<String, Integer> required = new HashMap<>();
        // Only consume ingredients for ONE craft, not all available
        for (ItemStack ing : recipe.ingredients) {
            if (ing == null || ing.isEmpty()) continue;
            String key = getIngredientKey(ing);
            required.merge(key, ing.getCount(), Integer::sum);
        }

        for (int i = 0; i < 9; i++) {
            ItemStack slotStack = this.input.getStack(i);
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

        // Mark dirty to trigger result slot refresh so player can craft more without reopening table
        this.input.markDirty();
    }
    
    private void decrementOneEachNonEmpty() {
        for (int i = 0; i < this.input.size(); i++) {
            ItemStack inSlot = this.input.getStack(i);
            if (!inSlot.isEmpty()) {
                inSlot.decrement(1);
            }
        }
        this.input.markDirty();
    }

    private static String getIngredientKey(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return "";
        String customId = SlayerItems.getCustomItemId(stack);
        if (customId != null) return "political:" + customId;
        return net.minecraft.registry.Registries.ITEM.getId(stack.getItem()).toString();
    }
}
