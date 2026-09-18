package com.political.mixin;

import com.political.bounty.SlayerItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;

import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Prevents re-dyeing of custom items in the crafting grid.
 */
@Mixin(CraftingMenu.class)
public class ItemDyeMixin {

    @Inject(method = "slotChangedCraftingGrid(Lnet/minecraft/world/inventory/AbstractContainerMenu;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/inventory/CraftingContainer;Lnet/minecraft/world/inventory/ResultContainer;Lnet/minecraft/world/item/crafting/RecipeHolder;)V", at = @At("HEAD"), cancellable = true)
    private static void preventCustomItemDyeing(AbstractContainerMenu handler, ServerLevel world,
                                                Player player, CraftingContainer craftingInventory,
                                                ResultContainer resultInventory,
                                                RecipeHolder<?> recipe,
                                                CallbackInfo ci) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        int dyeSlot = -1;
        int armorSlot = -1;
        ItemStack armorStack = null;

        for (int i = 0; i < craftingInventory.getContainerSize(); i++) {
            ItemStack stack = craftingInventory.getItem(i);
            if (stack == null || stack.isEmpty()) continue;

            if (stack.getItem() instanceof DyeItem) {
                dyeSlot = i;
            }

            if (isCustomDyedItem(stack)) {
                armorSlot = i;
                armorStack = stack.copy();
            }
        }

        if (dyeSlot != -1 && armorSlot != -1 && armorStack != null) {
            serverPlayer.sendSystemMessage(Component.literal("§c✖ Cannot re-dye custom items!").withStyle(ChatFormatting.RED), true);
            resultInventory.setItem(0, ItemStack.EMPTY);
            ci.cancel();
        }
    }

    private static boolean isCustomDyedItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;

        if (stack.is(Items.LEATHER_HELMET) ||
            stack.is(Items.LEATHER_CHESTPLATE) ||
            stack.is(Items.LEATHER_LEGGINGS) ||
            stack.is(Items.LEATHER_BOOTS)) {

            if (SlayerItems.getCustomItemId(stack) != null) return true;
        }

        return false;
    }
}
