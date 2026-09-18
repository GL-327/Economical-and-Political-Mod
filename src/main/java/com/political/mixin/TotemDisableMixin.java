package com.political.mixin;

import com.political.items.ArmorAbilityHandler;
import com.political.CoinTotemManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class TotemDisableMixin {

    @Inject(method = "checkTotemDeathProtection", at = @At("HEAD"), cancellable = true)
    private void disableTotem(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity)(Object)this;

        // If player, check for coin totem and slime boots
        if (self instanceof ServerPlayer player) {
            // Try coin totem first (one-time use to prevent coin loss)
            if (CoinTotemManager.tryUseCoinTotem(player, source)) {
                cir.setReturnValue(true);  // Coin totem activated
                return;
            }
            // Try slime boots death save
            if (ArmorAbilityHandler.trySlimeBootsDeathSave(player)) {
                cir.setReturnValue(true);  // Death was prevented by slime boots
                return;
            }
        }

        // Disable regular totems for all players (no revival functionality)
        cir.setReturnValue(false);
    }
}