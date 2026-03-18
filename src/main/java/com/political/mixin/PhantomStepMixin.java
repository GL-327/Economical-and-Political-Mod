package com.political.mixin;

import com.political.SlayerItems;
import com.political.ArmourAttribute;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class PhantomStepMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        
        if (hasPhantomStepAttribute(entity)) {
            World world = entity.getEntityWorld();
            
            // Apply stealth effects (noise suppression)
            if (entity instanceof PlayerEntity player && world instanceof ServerWorld) {
                // Apply invisibility effect for stealth (but not full invisibility)
                if (!entity.hasStatusEffect(StatusEffects.INVISIBILITY)) {
                    entity.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 40, 0, false, false, true));
                }
                
                // Reduce step sound frequency
                if (entity.age % 20 == 0) { // Every second
                    // This would be handled by a separate sound mixin if needed
                }
            }
            
            // No fall damage
            if (entity.fallDistance > 0) {
                entity.fallDistance = 0;
            }
        }
    }

    @Inject(method = "sleep", at = @At("HEAD"), cancellable = true)
    private void onSleep(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        
        if (hasPhantomStepAttribute(entity)) {
            // Prevent sleeping with Phantom Step attribute
            ci.cancel();
        }
    }

    private boolean hasPhantomStepAttribute(LivingEntity entity) {
        if (entity instanceof PlayerEntity player) {
            // Check specific armor slots for PHANTOMSTEP attribute
            EquipmentSlot[] armorSlots = {
                EquipmentSlot.HEAD,
                EquipmentSlot.CHEST, 
                EquipmentSlot.LEGS,
                EquipmentSlot.FEET
            };
            
            for (EquipmentSlot slot : armorSlots) {
                if (SlayerItems.getAppliedAttribute(player.getEquippedStack(slot)) == ArmourAttribute.PHANTOMSTEP) {
                    return true;
                }
            }
        }
        return false;
    }
}
