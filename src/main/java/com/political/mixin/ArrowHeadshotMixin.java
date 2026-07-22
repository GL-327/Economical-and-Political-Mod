package com.political.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(AbstractArrow.class)
public class ArrowHeadshotMixin {

    @ModifyVariable(method = "onHitEntity", at = @At("STORE"), ordinal = 0)
    private int modifyArrowDamage(int damage, EntityHitResult hitResult) {
        AbstractArrow arrow = (AbstractArrow)(Object)this;

        // Check if this arrow was shot from a skeleton bow (via command tag)
        if (!arrow.entityTags().contains("skeleton_bow_arrow")) {
            return damage;
        }

        // Only apply headshot bonus for critical (fully-charged) shots.
        // Instant-fire arrows fired by the Skeleton Bow's instant-shot mode
        // are tagged "instant_shot" and must never trigger headshots even though
        // they are also marked critical for visual particles.
        if (!arrow.isCritArrow() || arrow.entityTags().contains("instant_shot")) {
            return damage;
        }

        Entity owner = arrow.getOwner();
        if (!(owner instanceof ServerPlayer player)) return damage;

        Entity target = hitResult.getEntity();
        if (!(target instanceof LivingEntity living)) return damage;

        // Headshot: arrow hit the upper 15% of the entity's height (true head region).
        // Using the entity bounding box bottom + height to compute the head threshold.
        double entityBottom = living.getY();
        double entityHeight = living.getBbHeight();
        double headThreshold = entityBottom + entityHeight * 0.85; // top 15% = head

        double hitY = arrow.getY();

        if (hitY >= headThreshold) {
            // HEADSHOT! 500% damage (5x)
            player.sendSystemMessage(Component.literal("💀 HEADSHOT!")
                    .withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
            return damage * 5;
        }

        return damage;
    }
}