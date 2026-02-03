package com.political.mixin;

import com.political.PerkManager;
import net.minecraft.village.TradeOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TradeOffer.class)
public abstract class TradeOfferMixin {

    @Mutable  // ADD THIS LINE
    @Shadow
    private int maxUses;

    @Inject(method = "getDisplayedFirstBuyItem", at = @At("RETURN"), cancellable = true)
    private void political_modifyTradePrice(CallbackInfoReturnable<net.minecraft.item.ItemStack> cir) {
        float multiplier = PerkManager.getTradeMultiplier();
        if (multiplier != 1.0f) {
            net.minecraft.item.ItemStack stack = cir.getReturnValue();
            if (!stack.isEmpty()) {
                int newCount = (int) Math.ceil(stack.getCount() * multiplier);
                newCount = Math.min(newCount, stack.getMaxCount());
                newCount = Math.max(1, newCount);
                stack.setCount(newCount);
                cir.setReturnValue(stack);
            }
        }
    }

    @Inject(method = "<init>(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;IIFIF)V", at = @At("RETURN"))
    private void political_increaseMaxUses(CallbackInfo ci) {
        this.maxUses = this.maxUses * 3;
    }
}