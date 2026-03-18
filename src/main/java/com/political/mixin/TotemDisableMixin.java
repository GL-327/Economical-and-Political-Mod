package com.political.mixin;

import com.political.ArmorAbilityHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class TotemDisableMixin {

    @Inject(method = "tryUseDeathProtector", at = @At("HEAD"), cancellable = true)
    private void disableTotem(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity)(Object)this;

        // If player, check for slime boots first
        if (self instanceof ServerPlayerEntity player) {
            // Try slime boots death save instead of totem
            if (ArmorAbilityHandler.trySlimeBootsDeathSave(player)) {
                cir.setReturnValue(true);  // Death was prevented by slime boots
                return;
            }
        }

        // Disable regular totems for all players
        cir.setReturnValue(false);
    }
}