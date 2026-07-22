package com.political.mixin;

import com.political.SlayerItems;
import com.political.ArmourAttribute;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Armour attribute combat effects (26.x: damage flows through {@link LivingEntity#hurtServer}).
 */
@Mixin(LivingEntity.class)
public class AttributeDamageMixin {

    @ModifyVariable(
            method = "hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true
    )
    private float political_modifyAttributeOutgoingDamage(float amount, ServerLevel world, DamageSource source) {
        if (!(source.getEntity() instanceof Player player)) {
            return amount;
        }
        if (hasAttribute(player, ArmourAttribute.BURNING) && player.isOnFire()) {
            amount *= 3.0f;
        }
        if (hasAttribute(player, ArmourAttribute.FRENZIED)) {
            if (player.level() instanceof ServerLevel sl && player.getRandom().nextFloat() < 0.1f) {
                player.hurtServer(sl, player.damageSources().magic(), 2.0f);
            }
            amount *= 1.5f;
        }
        return amount;
    }

    @Inject(
            method = "hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            at = @At("RETURN")
    )
    private void political_cursedLifesteal(ServerLevel world, DamageSource source, float amount,
                                            CallbackInfoReturnable<Boolean> cir) {
        if (!Boolean.TRUE.equals(cir.getReturnValue())) {
            return;
        }
        if (!(source.getEntity() instanceof Player player)) {
            return;
        }
        if (!hasAttribute(player, ArmourAttribute.CURSED)) {
            return;
        }
        player.heal(amount * 0.2f);
    }

    private static boolean hasAttribute(Player player, ArmourAttribute attribute) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (!slot.isArmor()) {
                continue;
            }
            if (SlayerItems.getAppliedAttribute(player.getItemBySlot(slot)) == attribute) {
                return true;
            }
        }
        return false;
    }
}
