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
public class BurningAttributeMixin {

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        
        // Check if entity has BURNING attribute
        if (hasBurningAttribute(entity)) {
            // Cancel fire damage
            if (source.isOf(DamageTypes.IN_FIRE) || 
                source.isOf(DamageTypes.ON_FIRE) || 
                source.isOf(DamageTypes.LAVA) ||
                source.isOf(DamageTypes.FIREBALL) ||
                source.isOf(DamageTypes.HOT_FLOOR)) {
                cir.setReturnValue(false);
                return;
            }
            
            // Take double damage from water
            if (source.isOf(DamageTypes.DROWN) || 
                (source.getAttacker() != null && source.getAttacker().isTouchingWater())) {
                // Let the original damage happen, but we'll amplify it
                return;
            }
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        
        if (hasBurningAttribute(entity)) {
            World world = entity.getEntityWorld();
            
            // Force fire to stay active if on fire
            if (entity.isOnFire()) {
                entity.setFireTicks(200); // Reset to high value
                
                // Apply fire resistance to prevent self-damage (but keep visual fire)
                if (!entity.hasStatusEffect(StatusEffects.FIRE_RESISTANCE)) {
                    entity.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 40, 4, false, false, true));
                }
            }
            
            // Check if in water and apply extra damage
            if (entity.isTouchingWater() && world instanceof ServerWorld && entity.age % 40 == 0) {
                // Apply 1 heart of damage every 2 seconds when in water
                if (world instanceof ServerWorld serverWorld) {
                    entity.damage(serverWorld, entity.getDamageSources().magic(), 2.0f);
                }
            }
        }
    }

    private boolean hasBurningAttribute(LivingEntity entity) {
        if (entity instanceof PlayerEntity player) {
            // Check specific armor slots for BURNING attribute
            EquipmentSlot[] armorSlots = {
                EquipmentSlot.HEAD,
                EquipmentSlot.CHEST, 
                EquipmentSlot.LEGS,
                EquipmentSlot.FEET
            };
            
            for (EquipmentSlot slot : armorSlots) {
                if (SlayerItems.getAppliedAttribute(player.getEquippedStack(slot)) == ArmourAttribute.BURNING) {
                    return true;
                }
            }
        }
        return false;
    }
}
