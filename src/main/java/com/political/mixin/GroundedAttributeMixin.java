package com.political.mixin;

import com.political.SlayerItems;
import com.political.ArmourAttribute;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class GroundedAttributeMixin {

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        
        if (hasGroundedAttribute(entity)) {
            // Lightning immunity
            if (source.isOf(DamageTypes.LIGHTNING_BOLT)) {
                cir.setReturnValue(false);
                return;
            }
            
            // Reduced projectile damage will be handled in ArmourAttributeHandler
        }
    }

    @Inject(method = "jump", at = @At("HEAD"), cancellable = true)
    private void onJump(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        
        if (hasGroundedAttribute(entity)) {
            // Reduce jump height by 50%
            entity.setVelocity(entity.getVelocity().x, entity.getVelocity().y * 0.5, entity.getVelocity().z);
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
        if (entity instanceof PlayerEntity player) {
            // Check specific armor slots for GROUNDED attribute
            EquipmentSlot[] armorSlots = {
                EquipmentSlot.HEAD,
                EquipmentSlot.CHEST, 
                EquipmentSlot.LEGS,
                EquipmentSlot.FEET
            };
            
            for (EquipmentSlot slot : armorSlots) {
                if (SlayerItems.getAppliedAttribute(player.getEquippedStack(slot)) == ArmourAttribute.GROUNDED) {
                    return true;
                }
            }
        }
        return false;
    }
}
