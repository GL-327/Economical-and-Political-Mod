package com.political.mixin;

import com.political.HealthScalingManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LootModifierMixin {

    @Inject(
            method = "dropLoot(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/damage/DamageSource;Z)V",
            at = @At("TAIL")
    )
    private void dropExtraLoot(ServerWorld world, DamageSource source, boolean causedByPlayer, CallbackInfo ci) {
        LivingEntity self = (LivingEntity)(Object)this;

        int tier = HealthScalingManager.getMobTier(self);

        if (tier > 0) {
            // Drop extra XP orbs based on tier [4]
            double x = self.getX();
            double y = self.getY() + 0.5;
            double z = self.getZ();
            int bonusXp = tier * 5; // 5 XP per tier

            for (int i = 0; i < tier; i++) {
                ExperienceOrbEntity orb = new ExperienceOrbEntity(world, x, y, z, bonusXp);
                world.spawnEntity(orb);
            }
        }
    }
}