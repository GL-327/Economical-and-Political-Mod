package com.political.mixin;

import com.political.SlayerItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(PersistentProjectileEntity.class)
public class ArrowHomingMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void homeToTarget(CallbackInfo ci) {
        PersistentProjectileEntity arrow = (PersistentProjectileEntity)(Object)this;

        if (arrow.getEntityWorld().isClient()) return;
        if (arrow.isOnGround()) return;

        Entity owner = arrow.getOwner();
        if (!(owner instanceof ServerPlayerEntity player)) return;

        // Check if player shot with skeleton bow
        if (!SlayerItems.isSkeletonBow(player.getMainHandStack()) &&
                !SlayerItems.isSkeletonBow(player.getOffHandStack())) {
            return;
        }

        // Check level requirement
        if (!SlayerItems.canUseSkeletonBow(player)) return;

        // Find nearest target within 15 blocks
        ServerWorld world = (ServerWorld) arrow.getEntityWorld();
        Box searchBox = arrow.getBoundingBox().expand(15.0);

        LivingEntity closestTarget = null;
        double closestDist = Double.MAX_VALUE;

        List<LivingEntity> entities = world.getEntitiesByClass(
                LivingEntity.class, searchBox,
                e -> e != player && e.isAlive() && !e.isSpectator()
        );

        for (LivingEntity entity : entities) {
            double dist = arrow.squaredDistanceTo(entity);
            if (dist < closestDist) {
                closestDist = dist;
                closestTarget = entity;
            }
        }

        if (closestTarget != null && closestDist < 225) { // 15 blocks squared
            // Home towards target
            Vec3d arrowPos = new Vec3d(arrow.getX(), arrow.getY(), arrow.getZ());
            Vec3d targetPos = new Vec3d(closestTarget.getX(), closestTarget.getEyeY(), closestTarget.getZ());
            Vec3d direction = targetPos.subtract(arrowPos).normalize();

            Vec3d currentVel = arrow.getVelocity();
            double speed = currentVel.length();

            // Blend current velocity with homing direction (30% homing strength)
            Vec3d newVel = currentVel.normalize().multiply(0.7).add(direction.multiply(0.3)).normalize().multiply(speed);

            arrow.setVelocity(newVel);
        }
    }
}