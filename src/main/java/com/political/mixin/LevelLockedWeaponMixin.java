package com.political.mixin;

import com.political.bounty.SlayerItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public class LevelLockedWeaponMixin {

    @ModifyVariable(
            method = "hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true
    )
    private float checkLevelLockedWeapon(float amount, ServerLevel world, DamageSource source) {
        if (source != null && source.getEntity() instanceof ServerPlayer player) {
            ItemStack weapon = player.getMainHandItem();
            float multiplier = SlayerItems.getLevelLockedDamageMultiplier(player, weapon);
            return amount * multiplier;
        }
        return amount;
    }
}