package com.political.mixin;

import com.political.BackroomsManager;
import com.political.PlayerBuffManager;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Dimension-gated Backrooms damage bonus: while combat happens inside the Backrooms,
 * a player carrying the BACKROOMS_SURVIVOR buff deals extra damage.
 */
@Mixin(LivingEntity.class)
public abstract class BackroomsDamageMixin {

    @ModifyVariable(
            method = "hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true
    )
    private float political_backroomsDamage(float amount, ServerLevel world, DamageSource source) {
        if (!BackroomsManager.isBackrooms(world)) return amount;
        if (source.getEntity() instanceof ServerPlayer player) {
            double bonus = PlayerBuffManager.getBackroomsDamageBonus(player.getUUID().toString());
            if (bonus > 0.0) {
                return amount * (1.0f + (float) bonus);
            }
        }
        return amount;
    }
}
