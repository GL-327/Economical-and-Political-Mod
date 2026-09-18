package com.political.mixin;

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
public class BurningAttributeMixin {

    @Inject(
            method = "hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            at = @At("HEAD"),
            cancellable = true)
    private void onDamage(ServerLevel world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        
        // Check if entity has BURNING attribute
        if (hasBurningAttribute(entity)) {
            // Cancel fire damage
            if (source.is(DamageTypes.IN_FIRE) || 
                source.is(DamageTypes.ON_FIRE) || 
                source.is(DamageTypes.LAVA) ||
                source.is(DamageTypes.FIREBALL) ||
                source.is(DamageTypes.HOT_FLOOR)) {
                cir.setReturnValue(false);
                return;
            }
            
            // Take double damage from water
            if (source.is(DamageTypes.DROWN) || 
                (source.getEntity() != null && source.getEntity().isInWater())) {
                // Let the original damage happen, but we'll amplify it
                return;
            }
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        
        if (hasBurningAttribute(entity)) {
            Level world = entity.level();
            
            // Force fire to stay active if on fire
            if (entity.isOnFire()) {
                entity.setRemainingFireTicks(200); // Reset to high value
                
                // Apply fire resistance to prevent self-damage (but keep visual fire)
                if (!entity.hasEffect(MobEffects.FIRE_RESISTANCE)) {
                    entity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 40, 4, false, false, true));
                }
            }
            
            // Check if in water and apply extra damage
            if (entity.isInWater() && world instanceof ServerLevel && entity.tickCount % 40 == 0) {
                // Apply 1 heart of damage every 2 seconds when in water
                if (world instanceof ServerLevel serverWorld) {
                    entity.hurtServer(serverWorld, entity.damageSources().magic(), 2.0f);
                }
            }
        }
    }

    private boolean hasBurningAttribute(LivingEntity entity) {
        if (entity instanceof Player player) {
            // Check specific armor slots for BURNING attribute
            EquipmentSlot[] armorSlots = {
                EquipmentSlot.HEAD,
                EquipmentSlot.CHEST, 
                EquipmentSlot.LEGS,
                EquipmentSlot.FEET
            };
            
            for (EquipmentSlot slot : armorSlots) {
                if (SlayerItems.getAppliedAttribute(player.getItemBySlot(slot)) == ArmourAttribute.BURNING) {
                    return true;
                }
            }
        }
        return false;
    }
}
