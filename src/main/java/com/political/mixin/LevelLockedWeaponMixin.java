package com.political.mixin;

import com.political.SlayerItems;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public class LevelLockedWeaponMixin {

    @ModifyVariable(
            method = "damage(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/damage/DamageSource;F)Z",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true
    )
    private float checkLevelLockedWeapon(float amount) {
        LivingEntity self = (LivingEntity) (Object) this;

        // Get damage source from the entity's last damage source
        DamageSource source = self.getRecentDamageSource();
        if (source != null && source.getAttacker() instanceof ServerPlayerEntity player) {
            ItemStack weapon = player.getMainHandStack();
            float multiplier = SlayerItems.getLevelLockedDamageMultiplier(player, weapon);
            return amount * multiplier;
        }
        return amount;
    }
}