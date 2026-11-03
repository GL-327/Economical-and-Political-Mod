package com.political.mixin;
import com.political.ProgressionTreeManager;


import com.political.BeamCraftingHandler;
import com.political.recipe.BountyCraftingHandler;
import com.political.MasteryManager;
import com.political.bounty.SlayerItems;
import com.political.recipe.SlayerRecipes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(ResultSlot.class)
public class CraftingResultSlotMixin {

    @Shadow @Final private CraftingContainer craftSlots;

    @Inject(method = "onTake", at = @At("HEAD"), cancellable = true)
    private void political_onTakeItem(Player player, ItemStack stack, CallbackInfo ci) {
        if (player == null || stack == null || stack.isEmpty()) return;
        if (this.craftSlots == null) return;

        List<ItemStack> grid = new ArrayList<>();
        for (int i = 0; i < this.craftSlots.getContainerSize(); i++) {
            grid.add(this.craftSlots.getItem(i));
        }
        if (grid.size() != 9) return;

        // Check for custom recipe - if found, set result count to max crafts and handle consumption
        SlayerRecipes.Recipe matchedCustom = BountyCraftingHandler.getMatchedCustomRecipe(grid, player);
        if (matchedCustom != null) {
            BountyCraftingHandler.consumeCustomRecipeIngredients(this.craftSlots, matchedCustom);
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

    @Inject(method = "onTake", at = @At("TAIL"))
    private void political_awardCraftingMastery(Player player, ItemStack stack, CallbackInfo ci) {
        if (ci.isCancelled()) return;
        if (!(player instanceof ServerPlayer sp)) return;
        if (stack == null || stack.isEmpty()) return;
        MasteryManager.awardCraftingXp(sp, stack);
        com.political.ProgressionTreeManager.awardSkillPoints(sp, com.political.ProgressionTreeManager.Branch.CRAFTING, 1);
    }

    private void refreshResult(Player player) {
        ResultContainer resultInventory;
        try {
            resultInventory = (ResultContainer) ((Slot) (Object) this).container;
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger("politicalserver.mixin").debug("CraftingResultSlotMixin.refreshResult: ignored an error", e);
            return;
        }

        List<ItemStack> newGrid = new ArrayList<>();
        for (int i = 0; i < this.craftSlots.getContainerSize(); i++) {
            newGrid.add(this.craftSlots.getItem(i));
        }
        if (newGrid.size() != 9) {
            resultInventory.setItem(0, ItemStack.EMPTY);
            resultInventory.setChanged();
            return;
        }

        SlayerRecipes.Recipe matchedCustom = BountyCraftingHandler.getMatchedCustomRecipe(newGrid, player);
        if (matchedCustom != null) {
            resultInventory.setItem(0, matchedCustom.result.copy());
            resultInventory.setChanged();
            return;
        }

        ItemStack beam = BeamCraftingHandler.checkBeamRecipe(newGrid);
        if (beam != null) {
            resultInventory.setItem(0, beam);
            resultInventory.setChanged();
            return;
        }

        ItemStack bounty = BountyCraftingHandler.checkBountyGridRecipe(newGrid, player);
        if (bounty != null) {
            resultInventory.setItem(0, bounty);
            resultInventory.setChanged();
            return;
        }

        resultInventory.setItem(0, ItemStack.EMPTY);
        resultInventory.setChanged();
    }

    private void decrementOneEachNonEmpty() {
        for (int i = 0; i < this.craftSlots.getContainerSize(); i++) {
            ItemStack inSlot = this.craftSlots.getItem(i);
            if (!inSlot.isEmpty()) {
                inSlot.shrink(1);
            }
        }
        this.craftSlots.setChanged();
    }
}
