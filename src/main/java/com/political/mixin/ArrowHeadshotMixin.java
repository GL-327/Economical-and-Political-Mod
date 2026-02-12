package com.political.mixin;

import com.political.SlayerItems;
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

        Entity owner = arrow.getOwner();
        if (!(owner instanceof ServerPlayerEntity player)) return damage;

        // Check if shot with skeleton bow
        if (!SlayerItems.isSkeletonBow(player.getMainHandStack()) &&
                !SlayerItems.isSkeletonBow(player.getOffHandStack())) {
            return damage;
        }

        if (!SlayerItems.canUseSkeletonBow(player)) return damage;

        Entity target = hitResult.getEntity();
        if (!(target instanceof LivingEntity living)) return damage;

        // Check for headshot (arrow hit above eye height - 0.3)
        double hitY = arrow.getY();
        double targetEyeY = living.getEyeY();

        if (hitY >= targetEyeY - 0.3) {
            // HEADSHOT! 5x damage
            player.sendMessage(Text.literal("💀 HEADSHOT!")
                    .formatted(Formatting.RED, Formatting.BOLD), true);
            return damage * 5;
        }

        return damage;
    }
}