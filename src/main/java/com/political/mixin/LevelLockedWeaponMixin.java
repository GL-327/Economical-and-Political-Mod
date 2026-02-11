package com.political.mixin;

import com.political.SlayerItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Entity.class)
public class LevelLockedWeaponMixin {

    @ModifyVariable(method = "damage", at = @At("HEAD"), argsOnly = true)
    private float checkLevelLockedWeapon(float amount, DamageSource source) {
        if (source.getAttacker() instanceof ServerPlayerEntity player) {
            ItemStack weapon = player.getMainHandStack();
            float multiplier = SlayerItems.getLevelLockedDamageMultiplier(player, weapon);
            return amount * multiplier;
        }
        return amount;
    }
}