package com.political.mixin;

import com.political.PerkManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @ModifyVariable(method = "applyDamage", at = @At("HEAD"), argsOnly = true)
    private float political_adjustDamage(float amount, ServerWorld world, DamageSource source) {
        LivingEntity self = (LivingEntity) (Object) this;

        // Diplomatic Immunity - 50% less PvP damage
        if (self instanceof ServerPlayerEntity
                && source.getAttacker() instanceof ServerPlayerEntity) {
            amount *= PerkManager.getPvpDamageMultiplier();
        }

        // Scorched Earth - +25% fire damage
        if (source.isIn(DamageTypeTags.IS_FIRE)) {
            amount *= PerkManager.getFireDamageMultiplier();
        }

        // NATIONAL_UNITY - 10% damage reduction (players only)
        if (self instanceof ServerPlayerEntity) {
            amount *= PerkManager.getDamageReductionMultiplier();
        }

        return amount;
    }
}