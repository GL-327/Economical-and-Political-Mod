package com.political.mixin;
import com.political.ArmourAttributeHandler;


import com.political.bounty.SlayerItems;
import com.political.ArmourAttribute;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class GroundedAttributeMixin {

    @Inject(
            method = "hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            at = @At("HEAD"),
            cancellable = true)
    private void onDamage(ServerLevel world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        
        if (hasGroundedAttribute(entity)) {
            // Lightning immunity
            if (source.is(DamageTypes.LIGHTNING_BOLT)) {
                cir.setReturnValue(false);
                return;
            }
            
            // Reduced projectile damage will be handled in ArmourAttributeHandler
        }
    }

    @Inject(method = "jumpFromGround", at = @At("TAIL"))
    private void political_groundedReduceJump(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (hasGroundedAttribute(entity)) {
            var m = entity.getDeltaMovement();
            entity.setDeltaMovement(m.x, m.y * 0.5, m.z);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        
        if (hasGroundedAttribute(entity)) {
            // Prevent elytra flight - just reduce fall distance to prevent fall damage
            if (entity.fallDistance > 0) {
                entity.fallDistance = 0;
            }
        }
    }

    private boolean hasGroundedAttribute(LivingEntity entity) {
        if (entity instanceof Player player) {
            // Check specific armor slots for GROUNDED attribute
            EquipmentSlot[] armorSlots = {
                EquipmentSlot.HEAD,
                EquipmentSlot.CHEST, 
                EquipmentSlot.LEGS,
                EquipmentSlot.FEET
            };
            
            for (EquipmentSlot slot : armorSlots) {
                if (SlayerItems.getAppliedAttribute(player.getItemBySlot(slot)) == ArmourAttribute.GROUNDED) {
                    return true;
                }
            }
        }
        return false;
    }
}
