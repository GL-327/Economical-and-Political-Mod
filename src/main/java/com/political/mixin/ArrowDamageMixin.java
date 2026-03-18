package com.political.mixin;

import com.political.BountyArmorHandler;
import com.political.CustomArrows;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
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

    /**
     * Handle custom arrow effects when they hit a living entity.
     */
    @Inject(method = "onEntityHit", at = @At("TAIL"))
    private void political_customArrowHit(EntityHitResult hitResult, CallbackInfo ci) {
        PersistentProjectileEntity self = (PersistentProjectileEntity)(Object)this;
        
        Entity hit = hitResult.getEntity();
        if (!(hit instanceof LivingEntity livingHit)) return;
        if (!(self.getEntityWorld() instanceof ServerWorld world)) return;
        
        // Check if this arrow entity has custom arrow tags
        if (self.getCommandTags().contains("custom_arrow")) {
            // Determine arrow type from tags
            CustomArrows.ArrowType type = null;
            for (String tag : self.getCommandTags()) {
                if (tag.startsWith("arrow_type_")) {
                    String typeName = tag.substring("arrow_type_".length()).toUpperCase();
                    try {
                        type = CustomArrows.ArrowType.valueOf(typeName);
                    } catch (IllegalArgumentException ignored) {}
                    break;
                }
            }
            
            if (type != null) {
                applyCustomArrowEffects(self, livingHit, world, type);
            }
        }
    }

    /**
     * Apply custom arrow effects based on arrow type.
     */
    private void applyCustomArrowEffects(PersistentProjectileEntity arrow, LivingEntity target, ServerWorld world, CustomArrows.ArrowType type) {
        float bonusDamage = type.getBonusDamage();
        
        // Apply bonus damage
        switch (type) {
            case CustomArrows.ArrowType.VOID_ARROW -> {
                // Void arrows ignore armor - use magic damage
                target.damage(world, arrow.getDamageSources().magic(), bonusDamage);
                world.spawnParticles(net.minecraft.particle.ParticleTypes.PORTAL,
                        target.getX(), target.getY() + 1.0, target.getZ(),
                        20, 0.5, 0.5, 0.5, 0.1);
            }
            case CustomArrows.ArrowType.EXPLOSIVE_ARROW -> {
                // Create explosion on hit
                world.createExplosion(arrow, arrow.getX(), arrow.getY(), arrow.getZ(), 2.0f, net.minecraft.world.World.ExplosionSourceType.MOB);
                world.spawnParticles(net.minecraft.particle.ParticleTypes.EXPLOSION,
                        arrow.getX(), arrow.getY(), arrow.getZ(), 1, 0, 0, 0, 0);
            }
            case CustomArrows.ArrowType.INCENDIARY_ARROW -> {
                // Set target on fire for 5 seconds
                target.setFireTicks(100);
                world.spawnParticles(net.minecraft.particle.ParticleTypes.FLAME,
                        target.getX(), target.getY() + 1.0, target.getZ(),
                        15, 0.3, 0.5, 0.3, 0.05);
            }
            case CustomArrows.ArrowType.POISON_ARROW -> {
                // Apply poison for 10 seconds
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 200, 1));
                world.spawnParticles(net.minecraft.particle.ParticleTypes.ITEM_SLIME,
                        target.getX(), target.getY() + 1.0, target.getZ(),
                        10, 0.3, 0.5, 0.3, 0.05);
            }
            case CustomArrows.ArrowType.SPECTRAL_ARROW -> {
                // Apply glowing for 15 seconds
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 300));
                world.spawnParticles(net.minecraft.particle.ParticleTypes.END_ROD,
                        target.getX(), target.getY() + 1.0, target.getZ(),
                        10, 0.3, 0.5, 0.3, 0.05);
            }
            case CustomArrows.ArrowType.DIAMOND_TIPPED -> {
                // Critical hit particles
                world.spawnParticles(net.minecraft.particle.ParticleTypes.CRIT,
                        target.getX(), target.getY() + 1.0, target.getZ(),
                        8, 0.3, 0.5, 0.3, 0.1);
                target.damage(world, arrow.getDamageSources().arrow(arrow, arrow.getOwner()), bonusDamage);
            }
            case CustomArrows.ArrowType.NETHERITE_TIPPED -> {
                // Soul fire particles
                world.spawnParticles(net.minecraft.particle.ParticleTypes.SOUL_FIRE_FLAME,
                        target.getX(), target.getY() + 1.0, target.getZ(),
                        10, 0.3, 0.5, 0.3, 0.05);
                target.damage(world, arrow.getDamageSources().arrow(arrow, arrow.getOwner()), bonusDamage);
            }
            default -> {
                // Basic damage bonus for iron/steel tipped
                target.damage(world, arrow.getDamageSources().arrow(arrow, arrow.getOwner()), bonusDamage);
            }
        }
    }
}