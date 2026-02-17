package com.political.mixin;

import com.political.SlayerManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public abstract class BountyDamageMixin {

    // This captures the 'amount' parameter and lets us modify it
    @ModifyVariable(
            method = "damage(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/damage/DamageSource;F)Z",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true
    )
    private float modifyDamageAmount(float amount, ServerWorld world, DamageSource source, float originalAmount) {
        LivingEntity self = (LivingEntity) (Object) this;

        // === Player taking damage from slayer boss ===
        if (self instanceof ServerPlayerEntity player) {
            if (source.getAttacker() instanceof LivingEntity attacker) {
                if (SlayerManager.isSlayerBoss(attacker.getUuid())) {
                    SlayerManager.SlayerType bossType = SlayerManager.getBossSlayerType(attacker.getUuid());
                    if (bossType != null) {
                        double reduction = SlayerManager.getLevelDamageReduction(player, bossType);
                        return amount * (1.0f - (float) reduction);
                    }
                }
            }
        }

        // === Slayer boss taking damage from player ===
        if (SlayerManager.isSlayerBoss(self.getUuid())) {
            if (source.getAttacker() instanceof ServerPlayerEntity player) {
                SlayerManager.SlayerType bossType = SlayerManager.getBossSlayerType(self.getUuid());
                if (bossType != null) {
                    double multiplier = SlayerManager.getLevelDamageMultiplier(player, bossType);
                    return amount * (float) multiplier;
                }
            }
        }

        return amount;
    }
}