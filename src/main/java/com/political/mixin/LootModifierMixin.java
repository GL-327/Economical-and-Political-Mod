package com.political.mixin;

import com.political.stats.HealthScalingManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LootModifierMixin {

    @Inject(
            method = "dropAllDeathLoot(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;)V",
            at = @At("TAIL")
    )
    private void dropExtraLoot(ServerLevel world, DamageSource source, CallbackInfo ci) {
        LivingEntity self = (LivingEntity)(Object)this;

        int tier = HealthScalingManager.getMobTier(self);

        if (tier > 0) {
            // Drop extra XP orbs based on tier [4]
            double x = self.getX();
            double y = self.getY() + 0.5;
            double z = self.getZ();
            int bonusXp = tier * 5; // 5 XP per tier

            for (int i = 0; i < tier; i++) {
                ExperienceOrb orb = new ExperienceOrb(world, x, y, z, bonusXp);
                world.addFreshEntity(orb);
            }
        }
    }
}