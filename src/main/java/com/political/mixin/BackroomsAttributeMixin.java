package com.political.mixin;

import com.political.backrooms.attribute.ArmorAttribute;
import com.political.backrooms.attribute.ArmorAttributeManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Handles Backrooms armor attribute effects that require damage interception:
 * - THORNED: reflect damage back to attacker
 */
@Mixin(LivingEntity.class)
public class BackroomsAttributeMixin {

    @Inject(
        method = "damage(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/damage/DamageSource;F)Z",
        at = @At("TAIL")
    )
    private void backrooms_onDamage(ServerWorld world, DamageSource source, float amount,
                                    CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity)(Object)this;
        if (!(self instanceof ServerPlayerEntity victim)) return;
        if (amount <= 0) return;
        if (!Boolean.TRUE.equals(cir.getReturnValue())) return;

        // THORNED: reflect 20% damage per piece back to attacker
        int thornedPieces = ArmorAttributeManager.countArmorWithAttribute(victim, ArmorAttribute.THORNED);
        if (thornedPieces > 0 && source.getAttacker() instanceof LivingEntity attacker) {
            float reflectDamage = amount * 0.20f * thornedPieces;
            if (reflectDamage > 0) {
                attacker.damage(world, world.getDamageSources().thorns(victim), reflectDamage);
            }
        }
    }
}
