package com.political.mixin;

import com.political.PerkManager;
import net.minecraft.item.ItemStack;
import net.minecraft.village.TradeOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(TradeOffer.class)
public abstract class TradeOfferMixin {

    @Mutable
    @Shadow
    private int maxUses;

    @Unique
    private float political_gambitMultiplier = 1.0f;

    @Inject(method = "<init>(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;IIFIF)V", at = @At("RETURN"))
    private void political_modifyTrade(CallbackInfo ci) {
        // Triple max uses [4]
        this.maxUses = this.maxUses * 3;

        // Trader's Gambit - random per-trade pricing on creation
        if (PerkManager.hasActivePerk("TRADERS_GAMBIT")) {
            Random random = new Random();
            int roll = random.nextInt(10); // 0-9
            if (roll == 0) {
                political_gambitMultiplier = 0.1f;  // 90% off (1 in 10 chance)
            } else if (roll == 1) {
                political_gambitMultiplier = 2.0f;  // 200% markup (1 in 10 chance)
            }
        }
    }

    @Inject(method = "getDisplayedFirstBuyItem", at = @At("RETURN"), cancellable = true)
    private void political_modifyTradePrice(CallbackInfoReturnable<ItemStack> cir) {
        // Base economic multiplier [4]
        float multiplier = PerkManager.getTradeMultiplier();

        // Apply gambit multiplier
        multiplier *= political_gambitMultiplier;

        if (multiplier != 1.0f) {
            ItemStack stack = cir.getReturnValue();
            if (!stack.isEmpty()) {
                int newCount = (int) Math.ceil(stack.getCount() * multiplier);
                newCount = Math.min(newCount, stack.getMaxCount());
                newCount = Math.max(1, newCount);
                stack.setCount(newCount);
                cir.setReturnValue(stack);
            }
        }
    }
}