package com.political.mixin;

import com.political.BossAbilityManager;
import com.political.SlayerManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityDamageMixin {

    @ModifyVariable(
            method = "damage(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/damage/DamageSource;F)Z",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true
    )
    private float modifyDamageForBoss(float amount) {
        LivingEntity self = (LivingEntity) (Object) this;

        if (SlayerManager.isSlayerBoss(self.getUuid())) {
            return BossAbilityManager.modifyIncomingDamage(self.getUuid(), amount);
        }

        return amount;
    }
}