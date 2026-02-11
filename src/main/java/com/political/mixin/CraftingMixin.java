package com.political.mixin;

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

@Mixin(CraftingScreenHandler.class)
public class CraftingMixin {

    @Inject(method = "updateResult", at = @At("HEAD"), cancellable = true)
    private static void preventCustomItemCrafting(ScreenHandler handler, ServerWorld world,
                                                  net.minecraft.entity.player.PlayerEntity player, RecipeInputInventory craftingInventory,
                                                  CraftingResultInventory resultInventory, RecipeEntry<CraftingRecipe> recipe,
                                                  CallbackInfo ci) {

        boolean hasCustomItem = false;
        boolean hasNormalSword = false;
        boolean hasNormalShovel = false;
        boolean hasHPEBM = false;

        for (int i = 0; i < craftingInventory.size(); i++) {
            ItemStack stack = craftingInventory.getStack(i);
            if (stack.isEmpty()) continue;

            if (isCustomItem(stack)) {
                hasCustomItem = true;
                if (isHPEBM(stack)) hasHPEBM = true;
            }
            if (isNormalSword(stack)) hasNormalSword = true;
            if (isNormalShovel(stack)) hasNormalShovel = true;
        }

        // Prevent combining custom items with normal swords/shovels
        if (hasCustomItem && (hasNormalSword || (hasHPEBM && hasNormalShovel))) {
            resultInventory.setStack(0, ItemStack.EMPTY);
            ci.cancel();
        }
    }

    private static boolean isCustomItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        return SlayerItems.isSlayerSword(stack) ||
                SlayerItems.isSlayerCore(stack) ||
                isHPEBM(stack) ||
                isGavel(stack);
    }

    private static boolean isHPEBM(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Text name = stack.getName();
        if (name == null) return false;
        return name.getString().contains("HPEBM");
    }

    private static boolean isGavel(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Text name = stack.getName();
        if (name == null) return false;
        return name.getString().contains("Gavel");
    }

    private static boolean isNormalSword(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (isCustomItem(stack)) return false;
        return stack.isOf(Items.IRON_SWORD) ||
                stack.isOf(Items.DIAMOND_SWORD) ||
                stack.isOf(Items.NETHERITE_SWORD);
    }

    private static boolean isNormalShovel(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (isCustomItem(stack)) return false;
        return stack.isOf(Items.IRON_SHOVEL) ||
                stack.isOf(Items.DIAMOND_SHOVEL) ||
                stack.isOf(Items.NETHERITE_SHOVEL);
    }
}