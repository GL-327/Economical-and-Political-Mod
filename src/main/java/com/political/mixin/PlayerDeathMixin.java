package com.political.mixin;

import com.political.DeathPenaltyManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to handle player death events for:
 * - Coin penalty (50% on person)
 * - Bank/savings penalty if killed by player (50%)
 * - Coin syphon (killer gets 10% of remaining)
 */
@Mixin(LivingEntity.class)
public class PlayerDeathMixin {

    @Inject(method = "die", at = @At("HEAD"))
    private void onPlayerDeath(DamageSource source, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        
        // Only handle player deaths
        if (!(entity instanceof ServerPlayer player)) return;
        
        ServerLevel world = (ServerLevel) player.level();
        
        // Handle death penalty
        DeathPenaltyManager.onPlayerDeath(player, source);
    }
}
