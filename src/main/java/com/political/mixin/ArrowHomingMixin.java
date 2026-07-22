package com.political.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(AbstractArrow.class)
public class ArrowHomingMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void homeToTarget(CallbackInfo ci) {
        AbstractArrow arrow = (AbstractArrow)(Object)this;

        if (arrow.level().isClientSide()) return;
        if (arrow.onGround()) return;

        // Check if this arrow was shot from a skeleton bow (via command tag)
        if (!arrow.entityTags().contains("skeleton_bow_arrow")) {
            return;
        }

        Entity owner = arrow.getOwner();
        if (!(owner instanceof ServerPlayer player)) return;

        // Find nearest target within 15 blocks
        ServerLevel world = (ServerLevel) arrow.level();
        AABB searchBox = arrow.getBoundingBox().inflate(15.0);

        LivingEntity closestTarget = null;
        double closestDist = Double.MAX_VALUE;

        List<LivingEntity> entities = world.getEntitiesOfClass(
                LivingEntity.class, searchBox,
                e -> e != player && e.isAlive() && !e.isSpectator()
        );

        for (LivingEntity entity : entities) {
            double dist = arrow.distanceToSqr(entity);
            if (dist < closestDist) {
                closestDist = dist;
                closestTarget = entity;
            }
        }

        if (closestTarget != null && closestDist < 225) { // 15 blocks squared
            // Home towards target
            Vec3 arrowPos = new Vec3(arrow.getX(), arrow.getY(), arrow.getZ());
            Vec3 targetPos = new Vec3(closestTarget.getX(), closestTarget.getEyeY(), closestTarget.getZ());
            Vec3 direction = targetPos.subtract(arrowPos).normalize();

            Vec3 currentVel = arrow.getDeltaMovement();
            double speed = currentVel.length();

            // Blend current velocity with homing direction (30% homing strength)
            Vec3 newVel = currentVel.normalize().scale(0.7).add(direction.scale(0.3)).normalize().scale(speed);

            arrow.setDeltaMovement(newVel);
        }
    }
}