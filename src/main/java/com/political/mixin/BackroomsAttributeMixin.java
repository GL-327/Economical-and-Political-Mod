package com.political.mixin;

import com.political.backrooms.attribute.ArmorAttribute;
import com.political.backrooms.attribute.ArmorAttributeManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Handles Backrooms armor attribute effects that require damage interception:
 * - THORNED: reflect damage back to attacker
 * - VAMPIRIC: lifesteal on taking damage (extra fire damage penalty)
 */
@Mixin(LivingEntity.class)
public class BackroomsAttributeMixin {

    @Inject(method = "applyDamage", at = @At("TAIL"))
    private void backrooms_onDamage(ServerWorld world, DamageSource source, float amount, CallbackInfo ci) {
        LivingEntity self = (LivingEntity)(Object)this;
        if (!(self instanceof ServerPlayerEntity victim)) return;
        if (amount <= 0) return;

        // THORNED: reflect 20% damage per piece back to attacker
        int thornedPieces = ArmorAttributeManager.countArmorWithAttribute(victim, ArmorAttribute.THORNED);
        if (thornedPieces > 0 && source.getAttacker() instanceof LivingEntity attacker) {
            float reflectPercent = 0.20f * thornedPieces;
            float reflectDamage = amount * reflectPercent;
            if (reflectDamage > 0 && world != null) {
                attacker.damage(world, world.getDamageSources().thorns(victim), reflectDamage);
            }
        }

        // VAMPIRIC: 2x fire damage when wearing vampiric armor
        // (fire damage increase is handled by increasing incoming damage, done here as a side-effect notification)
        // TODO: ModifyVariable on fire damage path for full implementation
    }
}
