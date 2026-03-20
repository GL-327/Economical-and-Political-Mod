package com.political.mixin;

import com.political.BeamCraftingHandler;
import com.political.BountyCraftingHandler;
import com.political.SlayerItems;
import com.political.SlayerRecipes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.screen.slot.CraftingResultSlot;
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

    @Inject(method = "onTakeItem", at = @At("TAIL"))
    private void political_consumeIngredients(PlayerEntity player, ItemStack stack, 
            RecipeEntry<CraftingRecipe> recipe, CallbackInfo ci) {
        if (player == null || stack == null || stack.isEmpty()) return;
        if (this.input == null) return;

        // If there's a valid vanilla recipe, vanilla already consumed the ingredients
        if (recipe != null) return;

        List<ItemStack> grid = new ArrayList<>();
        for (int i = 0; i < this.input.size(); i++) {
            grid.add(this.input.getStack(i));
        }
        if (grid.size() != 9) return;

        SlayerRecipes.Recipe matchedCustom = BountyCraftingHandler.getMatchedCustomRecipe(grid, player);
        if (matchedCustom != null) {
            consumeForCustomRecipe(matchedCustom, grid);
            return;
        }

        ItemStack beam = BeamCraftingHandler.checkBeamRecipe(grid);
        if (beam != null) {
            decrementOneEachNonEmpty();
            return;
        }

        ItemStack bounty = BountyCraftingHandler.checkBountyGridRecipe(grid, player);
        if (bounty != null) {
            decrementOneEachNonEmpty();
        }
    }

    private void consumeForCustomRecipe(SlayerRecipes.Recipe recipe, List<ItemStack> grid) {
        if (recipe.ingredients == null) return;

        Map<String, Integer> required = new HashMap<>();
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
