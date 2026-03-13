package com.political.backrooms.items;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;

/**
 * Infused Echo Shard - Projectile that creates blue explosion and high-pitched sound
 * Used to deter enemies and stun bounty bosses
 */
public class InfusedEchoShardItem extends Item {
    
    public InfusedEchoShardItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        
        if (!world.isClient) {
            // Create the echo shard projectile
            EchoShardEntity projectile = new EchoShardEntity(world, user);
            projectile.setItem(stack);
            projectile.setVelocity(user.getRotationVector().multiply(1.5));
            world.spawnEntity(projectile);
            
            // Play high-pitched sound
            world.playSound(null, user.getX(), user.getY(), user.getZ(), 
                SoundEvents.ENTITY_WITCH_CELEBRATE, SoundCategory.PLAYERS, 1.0F, 2.0F);
            
            if (!user.getAbilities().creativeMode) {
                stack.decrement(1);
            }
        }
        
        return TypedActionResult.success(stack, world.isClient());
    }

    /**
     * Custom projectile entity for the infused echo shard
     */
    public static class EchoShardEntity extends ThrownItemEntity {
        
        public EchoShardEntity(World world, PlayerEntity owner) {
            super(null, owner, world);
        }

        public EchoShardEntity(World world, double x, double y, double z) {
            super(null, x, y, z, world);
        }

        @Override
        protected void onCollision(net.minecraft.util.hit.HitResult hitResult) {
            super.onCollision(hitResult);
            
            World world = this.getWorld();
            
            if (!world.isClient) {
                // Create blue explosion
                world.createExplosion(this, this.getX(), this.getY(), this.getZ(), 2.0F, 
                    Explosion.DestructionType.NONE);
                
                // Create blue particle effect
                for (int i = 0; i < 50; i++) {
                    double offsetX = (world.random.nextDouble() - 0.5) * 4.0;
                    double offsetY = (world.random.nextDouble() - 0.5) * 4.0;
                    double offsetZ = (world.random.nextDouble() - 0.5) * 4.0;
                    
                    world.spawnEntity(new net.minecraft.entity.projectile.thrown.EnderPearlEntity(
                        net.minecraft.entity.EntityType.ENDER_PEARL, world));
                    
                    // Add blue particles
                    world.addParticle(ParticleTypes.WITCH,
                        this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ,
                        0, 0, 0);
                }
                
                // Play high-pitched detonation sound
                world.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.ENTITY_WITCH_CELEBRATE, SoundCategory.BLOCKS, 2.0F, 3.0F);
                
                // Stun nearby enemies (bounty bosses, hostile mobs)
                stunNearbyEnemies(world, this.getX(), this.getY(), this.getZ(), 8.0);
                
                this.discard();
            }
        }

        /**
         * Stun nearby enemies and bounty bosses
         */
        private void stunNearbyEnemies(World world, double x, double y, double z, double radius) {
            world.getEntitiesByClass(net.minecraft.entity.LivingEntity.class, 
                net.minecraft.util.math.Box.of(new Vec3d(x, y, z), radius, radius, radius),
                entity -> entity != this.getOwner())
                .forEach(entity -> {
                    // Apply stun effect (slowness and weakness)
                    entity.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
                        net.minecraft.entity.effect.StatusEffects.SLOWNESS, 100, 4, false, false, false));
                    entity.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
                        net.minecraft.entity.effect.StatusEffects.WEAKNESS, 100, 2, false, false, false));
                    
                    // Knockback
                    Vec3d knockback = entity.getPos().subtract(this.getPos()).normalize().multiply(2.0);
                    entity.addVelocity(knockback.x, knockback.y + 0.5, knockback.z);
                });
        }
    }
}
