package com.political.mixin;

import com.political.CustomItemHandler;
import com.political.SlayerItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnvilScreenHandler.class)
public class AnvilCraftingMixin {

    @Inject(method = "updateResult", at = @At("HEAD"), cancellable = true)
    private void preventCustomItemAnvil(CallbackInfo ci) {
        AnvilScreenHandler handler = (AnvilScreenHandler) (Object) this;

        ItemStack first = handler.getSlot(0).getStack();
        ItemStack second = handler.getSlot(1).getStack();

        // Check if either item is a custom item
        boolean firstIsCustom = isCustomItem(first);
        boolean secondIsCustom = isCustomItem(second);
        boolean firstIsNormalSword = isNormalSword(first);
        boolean secondIsNormalSword = isNormalSword(second);
        boolean firstIsNormalShovel = isNormalShovel(first);
        boolean secondIsNormalShovel = isNormalShovel(second);

        // Prevent combining custom sword with normal iron sword
        if ((firstIsCustom && secondIsNormalSword) || (secondIsCustom && firstIsNormalSword)) {
            handler.getSlot(2).setStack(ItemStack.EMPTY);
            ci.cancel();
            return;
        }

        // Prevent combining HPEBM with normal shovel
        if ((isHPEBM(first) && secondIsNormalShovel) || (isHPEBM(second) && firstIsNormalShovel)) {
            handler.getSlot(2).setStack(ItemStack.EMPTY);
            ci.cancel();
            return;
        }

        // Prevent combining different custom items together
        if (firstIsCustom && secondIsCustom && !isSameCustomType(first, second)) {
            handler.getSlot(2).setStack(ItemStack.EMPTY);
            ci.cancel();
            return;
        }
    }

    private boolean isCustomItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        return SlayerItems.isSlayerSword(stack) ||
                SlayerItems.isSlayerCore(stack) ||
                isHPEBM(stack) ||
                isGavel(stack);
    }

    private boolean isHPEBM(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Text name = stack.getName();
        if (name == null) return false;
        String nameStr = name.getString();
        return nameStr.contains("HPEBM") || nameStr.contains("High-Powered");
    }

    private boolean isGavel(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Text name = stack.getName();
        if (name == null) return false;
        return name.getString().contains("Gavel");
    }

    private boolean isNormalSword(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (isCustomItem(stack)) return false;
        return stack.isOf(Items.IRON_SWORD) ||
                stack.isOf(Items.DIAMOND_SWORD) ||
                stack.isOf(Items.NETHERITE_SWORD) ||
                stack.isOf(Items.STONE_SWORD) ||
                stack.isOf(Items.WOODEN_SWORD) ||
                stack.isOf(Items.GOLDEN_SWORD);
    }

    private boolean isNormalShovel(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (isCustomItem(stack)) return false;
        return stack.isOf(Items.IRON_SHOVEL) ||
                stack.isOf(Items.DIAMOND_SHOVEL) ||
                stack.isOf(Items.NETHERITE_SHOVEL) ||
                stack.isOf(Items.STONE_SHOVEL) ||
                stack.isOf(Items.WOODEN_SHOVEL) ||
                stack.isOf(Items.GOLDEN_SHOVEL);
    }

    private boolean isSameCustomType(ItemStack first, ItemStack second) {
        // Allow combining same type (e.g., two slayer swords for repair)
        if (SlayerItems.isSlayerSword(first) && SlayerItems.isSlayerSword(second)) {
            return SlayerItems.getSwordSlayerType(first) == SlayerItems.getSwordSlayerType(second);
        }
        return false;
    }
}