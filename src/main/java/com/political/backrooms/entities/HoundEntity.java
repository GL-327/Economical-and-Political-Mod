package com.political.backrooms.entities;

import net.minecraft.entity.mob.ZombieEntity;

public class HoundEntity extends ZombieEntity {

    private int freezeTicks = 0;
    private boolean hasDetectedPlayer = false;

    public HoundEntity(EntityType<? extends ZombieEntity> entityType, World world) {
        super(entityType, world);
        this.setCanBreakDoors(false);
    }

    public static DefaultAttributeContainer.Builder createHoundAttributes() {
        return ZombieEntity.createZombieAttributes()
                .add(EntityAttributes.MOVEMENT_SPEED, 0.4) // Base patrol speed
                .add(EntityAttributes.ATTACK_DAMAGE, 3.0)
                .add(EntityAttributes.ATTACK_KNOCKBACK, 0.0);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new MeleeAttackGoal(this, 1.8, false) {
            @Override
            public boolean canStart() {
                return super.canStart() && freezeTicks <= 0;
            }
        });
        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    @Override
    public void tick() {
        super.tick();

        if (this.getEntityWorld().isClient()) return;

        PlayerEntity target = this.getEntityWorld().getClosestPlayer(this, 16.0);
        
        if (target != null && !hasDetectedPlayer) {
            // First time detection
            hasDetectedPlayer = true;
            // Freeze for exactly 3 seconds (60 ticks)
            freezeTicks = 60;
            this.setTarget(target);
        }

        if (freezeTicks > 0) {
            freezeTicks--;
            this.getNavigation().stop();
            this.setVelocity(0, this.getVelocity().y, 0);
        } else if (hasDetectedPlayer && this.getTarget() != null) {
            // Sprinting speed boost applies via goal multiplier (1.8x base)
            // Or override attributes
            this.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED).setBaseValue(0.8); // Fast sprint
            
            // Periodically play growl at 50% volume
            if (this.age % 40 == 0) {
                this.playSound(SoundEvents.ENTITY_ZOMBIE_AMBIENT, 0.5f, this.getSoundPitch());
            }
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        // Silence while frozen or patrolling until sprint triggers it manually
        return null;
    }
}
