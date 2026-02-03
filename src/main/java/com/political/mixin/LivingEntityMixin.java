package com.political.mixin;

import com.political.PerkManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @ModifyVariable(method = "damage", at = @At("HEAD"), argsOnly = true)
    private float political_modifyDamage(float amount) {
        LivingEntity self = (LivingEntity) (Object) this;

        // Only apply to players
        if (!(self instanceof ServerPlayerEntity)) return amount;

        // NATIONAL_UNITY - 10% damage reduction
        float multiplier = PerkManager.getDamageReductionMultiplier();
        return amount * multiplier;
    }
}