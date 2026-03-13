package com.political.backrooms.mobs;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.math.Vec3d;

/**
 * Partygoer - Tall human-like entities wrapped in folds of hideous yellow skin
 * Blind but very sensitive to sound, can be avoided by crouching
 * Face carved with knife, horrific grin and vertical slit eyes
 */
public class PartygoerEntity extends HostileEntity {
    
    private static final double HEARING_RADIUS = 12.0;
    private static final double DETECTION_CHANCE_STANDING = 0.8;
    private static final double DETECTION_CHANCE_CROUCHING = 0.1;
    
    public PartygoerEntity(EntityType<? extends PartygoerEntity> entityType, World world) {
        super(entityType, world);
        this.experiencePoints = 40;
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.2, false));
        this.goalSelector.add(3, new WanderAroundFarGoal(this, 0.6));
        this.goalSelector.add(4, new LookAtEntityGoal(this, PlayerEntity.class, 16.0F));
        this.goalSelector.add(4, new LookAroundGoal(this));
        
        this.targetSelector.add(1, new RevengeGoal(this));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    @Override
    public boolean canSpawn(WorldAccess world, SpawnReason spawnReason) {
        return super.canSpawn(world, spawnReason);
    }

    @Override
    public void tick() {
        super.tick();
        
        if (!this.getWorld().isClient()) {
            // Check for nearby players based on sound sensitivity
            this.detectNearbyPlayers();
        }
    }

    /**
     * Detect nearby players based on their movement state and distance
     */
    private void detectNearbyPlayers() {
        World world = this.getWorld();
        
        for (PlayerEntity player : world.getPlayers()) {
            double distance = this.squaredDistanceTo(player);
            
            if (distance <= HEARING_RADIUS * HEARING_RADIUS) {
                // Check if player is making noise (not crouching)
                boolean isCrouching = player.isSneaking();
                double detectionChance = isCrouching ? DETECTION_CHANCE_CROUCHING : DETECTION_CHANCE_STANDING;
                
                // Additional detection based on movement speed
                Vec3d velocity = player.getVelocity();
                double movementSpeed = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
                
                if (movementSpeed > 0.1) {
                    detectionChance *= 2.0; // Moving makes more noise
                }
                
                if (Random.create().nextDouble() < detectionChance) {
                    // Detected the player
                    if (!this.getTarget().equals(player)) {
                        this.setTarget(player);
                        
                        // Alert other partygoers
                        this.alertNearbyPartygoers(player);
                    }
                }
            }
        }
    }

    /**
     * Alert nearby partygoers to the detected player
     */
    private void alertNearbyPartygoers(PlayerEntity target) {
        World world = this.getWorld();
        
        for (PartygoerEntity partygoer : world.getEntitiesByClass(PartygoerEntity.class, 
                this.getBoundingBox().expand(20.0), e -> true)) {
            if (partygoer != this && !partygoer.hasTarget()) {
                partygoer.setTarget(target);
            }
        }
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        return super.damage(source, amount);
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return false; // Never despawn naturally
    }

    @Override
    public boolean cannotDespawn() {
        return true;
    }

    /**
     * Partygoers are blind but have enhanced hearing
     */
    @Override
    public boolean canSee(PlayerEntity player) {
        // They can't "see" but can detect through sound
        return false;
    }

    /**
     * Check if this partygoer currently has a target
     */
    public boolean hasTarget() {
        return this.getTarget() != null;
    }

    /**
     * Make partygoers make party-like sounds occasionally
     */
    @Override
    public void playAmbientSound() {
        if (Random.create().nextFloat() < 0.1f) {
            // Play party horn or celebration sound
            // this.playSound(SoundEvents.ENTITY_VILLAGER_CELEBRATE, 1.0F, 1.0F);
        }
        super.playAmbientSound();
    }
}
