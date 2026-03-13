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
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;

/**
 * Skinstealer - Hyper-aggressive entities that kill and steal skins/armor
 * Excellent hearing, laser vision, runs faster than humans
 * Copies player appearance and armor after killing them
 */
public class SkinstealerEntity extends HostileEntity {
    
    private LivingEntity copiedPlayer = null;
    private int copyTimer = 0;
    private static final int COPY_DURATION = 6000; // 5 minutes
    
    public SkinstealerEntity(EntityType<? extends SkinstealerEntity> entityType, World world) {
        super(entityType, world);
        this.experiencePoints = 60;
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.8, false)); // Very fast
        this.goalSelector.add(3, new WanderAroundFarGoal(this, 1.2));
        this.goalSelector.add(4, new LookAtEntityGoal(this, PlayerEntity.class, 24.0F));
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
        
        World world = this.getWorld();
        
        // Handle copy timer
        if (copiedPlayer != null && !world.isClient()) {
            copyTimer--;
            if (copyTimer <= 0) {
                // Revert to original form
                revertToOriginal();
            }
        }
        
        // Enhanced detection with laser vision
        if (!world.isClient()) {
            this.detectPlayersWithEnhancedVision();
        }
    }

    /**
     * Enhanced player detection with laser vision and excellent hearing
     */
    private void detectPlayersWithEnhancedVision() {
        World world = this.getWorld();
        
        for (PlayerEntity player : world.getPlayers()) {
            double distance = this.squaredDistanceTo(player);
            double detectionRadius = 48.0; // Enhanced vision range
            
            if (distance <= detectionRadius * detectionRadius) {
                // Check line of sight for laser vision
                if (this.hasLineOfSight(player)) {
                    // Direct detection with laser vision
                    if (!this.getTarget().equals(player)) {
                        this.setTarget(player);
                        break;
                    }
                } else if (distance <= 16.0 * 16.0) {
                    // Enhanced hearing for close range
                    Vec3d velocity = player.getVelocity();
                    double movementSpeed = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
                    
                    if (movementSpeed > 0.05 || !player.isSneaking()) {
                        // Detected through sound
                        if (!this.getTarget().equals(player)) {
                            this.setTarget(player);
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        return super.damage(source, amount);
    }

    @Override
    public void onKilledOther(ServerWorld world, LivingEntity other) {
        super.onKilledOther(world, other);
        
        if (other instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) other;
            
            // Copy the player's appearance and armor
            copyPlayerAppearance(player);
            
            // Steal armor and items
            stealPlayerItems(player);
        }
    }

    /**
     * Copy the player's appearance
     */
    private void copyPlayerAppearance(PlayerEntity player) {
        this.copiedPlayer = player;
        this.copyTimer = COPY_DURATION;
        
        // Set the skinstealer's name to match the player
        this.setCustomName(player.getName());
        this.setCustomNameVisible(true);
        
        // Copy armor appearance
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                ItemStack playerArmor = player.getEquippedStack(slot);
                if (!playerArmor.isEmpty()) {
                    this.equipStack(slot, playerArmor.copy());
                }
            }
        }
    }

    /**
     * Steal items from the killed player
     */
    private void stealPlayerItems(PlayerEntity player) {
        // Steal valuable items
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.HAND) {
                ItemStack item = player.getEquippedStack(slot);
                if (!item.isEmpty()) {
                    // Transfer to skinstealer
                    this.equipStack(slot, item.copy());
                    player.setEquippedStack(slot, ItemStack.EMPTY);
                }
            }
        }
    }

    /**
     * Revert to original skinstealer form
     */
    private void revertToOriginal() {
        this.copiedPlayer = null;
        this.copyTimer = 0;
        
        // Clear custom name
        this.setCustomName(null);
        this.setCustomNameVisible(false);
        
        // Clear copied armor
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                this.equipStack(slot, ItemStack.EMPTY);
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

    /**
     * Check if currently copying a player
     */
    public boolean isCopyingPlayer() {
        return copiedPlayer != null;
    }

    /**
     * Get the copied player
     */
    public LivingEntity getCopiedPlayer() {
        return copiedPlayer;
    }

    /**
     * Get remaining copy time
     */
    public int getCopyTimer() {
        return copyTimer;
    }

    @Override
    public boolean canTarget(LivingEntity target) {
        // Can target any player, but prefers those not looking
        if (target instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) target;
            
            // Higher priority for players not looking at the skinstealer
            if (!player.isLookingAt(this)) {
                return true;
            }
        }
        return super.canTarget(target);
    }

    @Override
    public void playAmbientSound() {
        if (Random.create().nextFloat() < 0.2f) {
            // Play unsettling sounds
            // this.playSound(SoundEvents.ENTITY_ENDERMAN_AMBIENT, 1.0F, 0.8F);
        }
        super.playAmbientSound();
    }
}
