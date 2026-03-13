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

/**
 * Smiler - A living hallucination that appears as a terrifyingly wide, glowing grin
 * Dwelling in pitch darkness, vanishes when illuminated with flashlight
 * Very real despite their ethereal nature
 */
public class SmilerEntity extends HostileEntity {
    
    private boolean isVanishing = false;
    private int vanishTimer = 0;

    public SmilerEntity(EntityType<? extends SmilerEntity> entityType, World world) {
        super(entityType, world);
        this.experiencePoints = 30;
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.5, true));
        this.goalSelector.add(3, new WanderAroundFarGoal(this, 0.8));
        this.goalSelector.add(4, new LookAtEntityGoal(this, PlayerEntity.class, 12.0F));
        this.goalSelector.add(4, new LookAroundGoal(this));
        
        this.targetSelector.add(1, new RevengeGoal(this));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    @Override
    public boolean canSpawn(WorldAccess world, SpawnReason spawnReason) {
        // Only spawn in complete darkness
        return super.canSpawn(world, spawnReason) && world.getBaseLightLevel(this.getBlockPos(), 0) == 0;
    }

    @Override
    public void tick() {
        super.tick();
        
        // Check light level and vanish if too bright
        World world = this.getWorld();
        if (!world.isClient()) {
            int lightLevel = world.getBaseLightLevel(this.getBlockPos(), 0);
            
            if (lightLevel > 0) {
                // Start vanishing when exposed to light
                if (!isVanishing) {
                    isVanishing = true;
                    vanishTimer = 40; // 2 seconds to vanish
                }
                
                vanishTimer--;
                if (vanishTimer <= 0) {
                    this.discard(); // Remove entity when vanished
                    return;
                }
            } else {
                // Reset vanish state when back in darkness
                isVanishing = false;
                vanishTimer = 0;
            }
        }
        
        // Create glowing particles in darkness
        if (!isVanishing && world.isClient()) {
            if (Random.create().nextFloat() < 0.3f) {
                // Create eerie glow particles around the grin area
                double offsetX = (Random.create().nextDouble() - 0.5) * 1.0;
                double offsetY = Random.create().nextDouble() * 0.5 + 0.5; // Head height
                double offsetZ = (Random.create().nextDouble() - 0.5) * 1.0;
                
                // Client-side particle effect for glowing grin
                // world.addParticle(ParticleTypes.END_ROD, 
                //     this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ, 0, 0, 0);
            }
        }
    }

    @Override
    public boolean isGlowing() {
        // Smilers glow in darkness
        World world = this.getWorld();
        return !isVanishing && world.getBaseLightLevel(this.getBlockPos(), 0) == 0;
    }

    @Override
    public boolean isInvulnerable() {
        // Invulnerable while vanishing
        return isVanishing || super.isInvulnerable();
    }

    @Override
    public boolean isPushable() {
        return !isVanishing && super.isPushable();
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return false; // Never despawn naturally
    }

    @Override
    public boolean cannotDespawn() {
        return true;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        // Take reduced damage when vanishing
        if (isVanishing) {
            return false;
        }
        return super.damage(source, amount);
    }

    /**
     * Check if the smiler is currently vanishing due to light exposure
     */
    public boolean isVanishing() {
        return isVanishing;
    }
}
