package com.political.backrooms.mobs;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.util.math.random.Random;

/**
 * Aranea Membri - Extremely hostile and dangerous creatures with skin as black as night
 * They crawl around at speeds exceeding 60mph, making fight the least worst option
 * 2 metres long with spider-like form, most who get close become red mist seconds later
 */
public class AraneaMembriEntity extends HostileEntity {
    
    public AraneaMembriEntity(EntityType<? extends AraneaMembriEntity> entityType, World world) {
        super(entityType, world);
        this.experiencePoints = 50;
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(3, new MeleeAttackGoal(this, 2.0, false));
        this.goalSelector.add(4, new PounceAtTargetGoal(this, 0.4F));
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 1.0));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.add(6, new LookAroundGoal(this));
        
        this.targetSelector.add(1, new RevengeGoal(this));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    @Override
    public boolean canSpawn(WorldAccess world, SpawnReason spawnReason) {
        return super.canSpawn(world, spawnReason) && world.getBaseLightLevel(this.getBlockPos(), 0) <= 7;
    }

    @Override
    public void tick() {
        super.tick();
        
        // Emit particles when moving fast
        if (this.getVelocity().lengthSquared() > 0.1 && this.getWorld().isClient()) {
            // Add red mist particle effect when moving at high speed
            for (int i = 0; i < 3; i++) {
                double offsetX = (Random.create().nextDouble() - 0.5) * 0.5;
                double offsetY = Random.create().nextDouble() * 0.5;
                double offsetZ = (Random.create().nextDouble() - 0.5) * 0.5;
                
                // This would need client-side particle implementation
                // this.getWorld().addParticle(ParticleTypes.ANGRY_VILLAGER, 
                //     this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ, 0, 0, 0);
            }
        }
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return false; // Never despawn naturally
    }

    @Override
    public boolean cannotDespawn() {
        return true;
    }
}
