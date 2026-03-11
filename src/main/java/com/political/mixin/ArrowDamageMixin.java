package com.political.mixin;

import com.political.BountyArmorHandler;
import com.political.SkeletonBowHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PersistentProjectileEntity.class)
public class ArrowDamageMixin {

    @ModifyVariable(method = "onEntityHit", at = @At("STORE"), ordinal = 0)
    private int modifyArrowDamage(int damage) {
        PersistentProjectileEntity self = (PersistentProjectileEntity)(Object)this;

        // Check if this is a skeleton bow arrow
        if (self.getCommandTags().contains("skeleton_bow_arrow")) {
            // 50% more damage
            return (int)(damage * 1.5f);
        }

        // Skeleton armor helmet projectile damage boost
        Entity owner = self.getOwner();
        if (owner instanceof ServerPlayerEntity shooter) {
            float boost = BountyArmorHandler.getProjectileDamageBoost(shooter);
            if (boost > 1.0f) {
                return (int)(damage * boost);
            }
        }

        return damage;
    }

    /**
     * When a player with Skeleton T2 Helmet fires an arrow that hits a living entity,
     * apply Glowing for 10 seconds to reveal them through walls.
     */
    @Inject(method = "onEntityHit", at = @At("HEAD"))
    private void political_glowingArrowHit(EntityHitResult hitResult, CallbackInfo ci) {
        PersistentProjectileEntity self = (PersistentProjectileEntity)(Object)this;
        Entity owner = self.getOwner();
        if (!(owner instanceof ServerPlayerEntity shooter)) return;
        if (!BountyArmorHandler.hasGlowingArrows(shooter)) return;

        Entity hit = hitResult.getEntity();
        if (hit instanceof LivingEntity livingHit) {
            livingHit.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.GLOWING, 200, 0, true, false, false)); // 10s
        }
    }
}