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

        Entity owner = arrow.getOwner();
        if (!(owner instanceof ServerPlayerEntity player)) return damage;

        Entity target = hitResult.getEntity();
        if (!(target instanceof LivingEntity living)) return damage;

        // Headshot: arrow hit the upper 25% of the entity's height (the head region)
        // Entity bounding box bottom is getY(), top is getY() + getHeight()
        double entityBottom = living.getY();
        double entityHeight = living.getHeight();
        double headThreshold = entityBottom + entityHeight * 0.75;  // top 25% = head

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