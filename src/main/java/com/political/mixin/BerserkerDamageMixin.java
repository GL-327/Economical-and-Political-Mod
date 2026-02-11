package com.political.mixin;

import com.political.CustomItemHandler;
import com.political.SlayerItems;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public class BerserkerDamageMixin {

    @ModifyVariable(
            method = "damage(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/damage/DamageSource;F)Z",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    private float modifyIncomingDamage(float amount, ServerWorld world, DamageSource source) {
        // Check if attacker is a player with berserker helmet
        if (source.getAttacker() instanceof ServerPlayerEntity player) {
            // Apply berserker damage multiplier
            float berserkerMultiplier = CustomItemHandler.getBerserkerDamageMultiplier(player);

            // Apply level-locked weapon check
            float levelMultiplier = SlayerItems.getLevelLockedDamageMultiplier(player, player.getMainHandStack());

            return amount * berserkerMultiplier * levelMultiplier;
        }
        return amount;
    }
}