package com.political.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(PersistentProjectileEntity.class)
public class ArrowHeadshotMixin {

    @ModifyVariable(method = "onEntityHit", at = @At("STORE"), ordinal = 0)
    private int modifyArrowDamage(int damage, EntityHitResult hitResult) {
        PersistentProjectileEntity arrow = (PersistentProjectileEntity)(Object)this;

        // Check if this arrow was shot from a skeleton bow (via command tag)
        if (!arrow.getCommandTags().contains("skeleton_bow_arrow")) {
            return damage;
        }

        // Only apply headshot bonus for critical (fully-charged) shots.
        // Instant-fire arrows fired by the Skeleton Bow's instant-shot mode
        // are tagged "instant_shot" and must never trigger headshots even though
        // they are also marked critical for visual particles.
        if (!arrow.isCritical() || arrow.getCommandTags().contains("instant_shot")) {
            return damage;
        }

        Entity owner = arrow.getOwner();
        if (!(owner instanceof ServerPlayerEntity player)) return damage;

        Entity target = hitResult.getEntity();
        if (!(target instanceof LivingEntity living)) return damage;

        // Headshot: arrow hit the upper 15% of the entity's height (true head region).
        // Using the entity bounding box bottom + height to compute the head threshold.
        double entityBottom = living.getY();
        double entityHeight = living.getHeight();
        double headThreshold = entityBottom + entityHeight * 0.85; // top 15% = head

        double hitY = arrow.getY();

        if (hitY >= headThreshold) {
            // HEADSHOT! 500% damage (5x)
            player.sendMessage(Text.literal("💀 HEADSHOT!")
                    .formatted(Formatting.RED, Formatting.BOLD), true);
            return damage * 5;
        }

        return damage;
    }
}