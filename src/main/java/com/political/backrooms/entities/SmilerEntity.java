package com.political.backrooms.entities;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class SmilerEntity extends EndermanEntity {

    private boolean isObserved = false;

    public SmilerEntity(EntityType<? extends EndermanEntity> entityType, World world) {
        super(entityType, world);
        this.setInvisible(true);
    }

    public static DefaultAttributeContainer.Builder createSmilerAttributes() {
        return EndermanEntity.createEndermanAttributes()
                .add(EntityAttributes.MOVEMENT_SPEED, 0.15)
                .add(EntityAttributes.MAX_HEALTH, 100.0)
                .add(EntityAttributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void initGoals() {
        // Target nearest player
        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    @Override
    public void tick() {
        super.tick();

        if (this.getEntityWorld().isClient()) return;

        ServerWorld serverWorld = (ServerWorld) this.getEntityWorld();

        // 1. Emit glowing particles at eye level (Y=67 roughly if floor is 64, so eye height ~ 3 blocks)
        // Assume entity is standing on floor. Eye height for smiler is 1.8 roughly.
        if (this.age % 2 == 0) {
            serverWorld.spawnParticles(ParticleTypes.END_ROD, 
                    this.getX(), this.getY() + this.getStandingEyeHeight(), this.getZ(),
                    2, 0.2, 0.1, 0.2, 0.0);
        }

        // 2. Check line of sight from all nearby players
        isObserved = false;
        PlayerEntity closestPlayer = null;
        double closestDistSq = Double.MAX_VALUE;

        for (PlayerEntity player : serverWorld.getPlayers()) {
            double distSq = player.squaredDistanceTo(this);
            if (distSq < 2048) { // ~45 blocks
                if (distSq < closestDistSq) {
                    closestDistSq = distSq;
                    closestPlayer = player;
                }

                // Check Line of Sight
                Vec3d lookVec = player.getRotationVec(1.0F).normalize();
                Vec3d toEntity = new Vec3d(this.getX() - player.getX(), this.getEyeY() - player.getEyeY(), this.getZ() - player.getZ());
                double dist = toEntity.length();
                toEntity = toEntity.normalize();
                double dot = lookVec.dotProduct(toEntity);

                // If within view frustum and no blocks blocking
                if (dot > 1.0 - 0.025 / dist) { // Threshold for "looking at"
                    if (player.canSee(this)) {
                        isObserved = true;
                    }
                }
            }
        }

        // 3. Despawn if player is within 4 blocks
        if (closestDistSq <= 16.0) { // 4 squared
            this.discard(); // Despawns silently
            return;
        }

        // 4. Movement logic
        if (isObserved) {
            // Freeze completely
            this.getNavigation().stop();
            this.setVelocity(0, this.getVelocity().y, 0);
        } else {
            // Move slowly towards nearest player
            if (closestPlayer != null) {
                this.getNavigation().startMovingTo(closestPlayer, 1.0); // 1.0 multiplier of base speed 0.15
            }
        }
    }
}
