package com.political.mixin;

import com.political.BountyArmorHandler;
import com.political.MasteryHeartTree;
import com.political.CustomArrows;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractArrow.class)
public class ArrowDamageMixin {

    @ModifyVariable(method = "onHitEntity", at = @At("STORE"), ordinal = 0)
    private int modifyArrowDamage(int damage) {
        AbstractArrow self = (AbstractArrow)(Object)this;

        // Check if this is a skeleton bow arrow
        if (self.entityTags().contains("skeleton_bow_arrow")) {
            // 50% more damage
            return (int)(damage * 1.5f);
        }

        // Skeleton armor helmet projectile damage boost
        Entity owner = self.getOwner();
        if (owner instanceof ServerPlayer shooter) {
            float boost = BountyArmorHandler.getProjectileDamageBoost(shooter);
            if (boost > 1.0f) {
                damage = (int)(damage * boost);
            }
            damage = (int)(damage * MasteryHeartTree.projectileDamageMultiplier(shooter.getUUID().toString()));
        }

        return damage;
    }

    /**
     * When a player with Skeleton T2 Helmet fires an arrow that hits a living entity,
     * apply Glowing for 10 seconds to reveal them through walls.
     */
    @Inject(method = "onHitEntity", at = @At("HEAD"))
    private void political_glowingArrowHit(EntityHitResult hitResult, CallbackInfo ci) {
        AbstractArrow self = (AbstractArrow)(Object)this;
        Entity owner = self.getOwner();
        if (!(owner instanceof ServerPlayer shooter)) return;
        if (!BountyArmorHandler.hasGlowingArrows(shooter)) return;

        Entity hit = hitResult.getEntity();
        if (hit instanceof LivingEntity livingHit) {
            livingHit.addEffect(new MobEffectInstance(
                    MobEffects.GLOWING, 200, 0, true, false, false)); // 10s
        }
    }

    /**
     * Handle custom arrow effects when they hit a living entity.
     */
    @Inject(method = "onHitEntity", at = @At("TAIL"))
    private void political_customArrowHit(EntityHitResult hitResult, CallbackInfo ci) {
        AbstractArrow self = (AbstractArrow)(Object)this;
        
        Entity hit = hitResult.getEntity();
        if (!(hit instanceof LivingEntity livingHit)) return;
        if (!(self.level() instanceof ServerLevel world)) return;
        
        // Check if this arrow entity has custom arrow tags
        if (self.entityTags().contains("custom_arrow")) {
            // Determine arrow type from tags
            CustomArrows.ArrowType type = null;
            for (String tag : self.entityTags()) {
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
    private void applyCustomArrowEffects(AbstractArrow arrow, LivingEntity target, ServerLevel world, CustomArrows.ArrowType type) {
        float bonusDamage = type.getBonusDamage();
        
        // Apply bonus damage
        switch (type) {
            case CustomArrows.ArrowType.VOID_ARROW -> {
                // Void arrows ignore armor - use magic damage
                target.hurtServer(world, arrow.damageSources().magic(), bonusDamage);
                world.sendParticles(net.minecraft.core.particles.ParticleTypes.PORTAL,
                        target.getX(), target.getY() + 1.0, target.getZ(),
                        5, 0.5, 0.5, 0.5, 0.1);
            }
            case CustomArrows.ArrowType.EXPLOSIVE_ARROW -> {
                // Create explosion on hit
                world.explode(arrow, arrow.getX(), arrow.getY(), arrow.getZ(), 2.0f, net.minecraft.world.level.Level.ExplosionInteraction.MOB);
                world.sendParticles(net.minecraft.core.particles.ParticleTypes.EXPLOSION,
                        arrow.getX(), arrow.getY(), arrow.getZ(), 1, 0, 0, 0, 0);
            }
            case CustomArrows.ArrowType.INCENDIARY_ARROW -> {
                // Set target on fire for 5 seconds
                target.setRemainingFireTicks(100);
                world.sendParticles(net.minecraft.core.particles.ParticleTypes.FLAME,
                        target.getX(), target.getY() + 1.0, target.getZ(),
                        5, 0.3, 0.5, 0.3, 0.05);
            }
            case CustomArrows.ArrowType.POISON_ARROW -> {
                // Apply poison for 10 seconds
                target.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 1));
                world.sendParticles(net.minecraft.core.particles.ParticleTypes.ITEM_SLIME,
                        target.getX(), target.getY() + 1.0, target.getZ(),
                        4, 0.3, 0.5, 0.3, 0.05);
            }
            case CustomArrows.ArrowType.SPECTRAL_ARROW -> {
                // Apply glowing for 15 seconds
                target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 300));
                world.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD,
                        target.getX(), target.getY() + 1.0, target.getZ(),
                        4, 0.3, 0.5, 0.3, 0.05);
            }
            case CustomArrows.ArrowType.DIAMOND_TIPPED -> {
                // Critical hit particles
                world.sendParticles(net.minecraft.core.particles.ParticleTypes.CRIT,
                        target.getX(), target.getY() + 1.0, target.getZ(),
                        3, 0.3, 0.5, 0.3, 0.1);
                target.hurtServer(world, arrow.damageSources().arrow(arrow, arrow.getOwner()), bonusDamage);
            }
            case CustomArrows.ArrowType.NETHERITE_TIPPED -> {
                // Soul fire particles
                world.sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME,
                        target.getX(), target.getY() + 1.0, target.getZ(),
                        4, 0.3, 0.5, 0.3, 0.05);
                target.hurtServer(world, arrow.damageSources().arrow(arrow, arrow.getOwner()), bonusDamage);
            }
            default -> {
                // Basic damage bonus for iron/steel tipped
                target.hurtServer(world, arrow.damageSources().arrow(arrow, arrow.getOwner()), bonusDamage);
            }
        }
    }
}