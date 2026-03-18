package com.political.mixin;

import com.political.SlayerItems;
import com.political.ArmourAttribute;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class AttributeDamageMixin {

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void modifyAttributeDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        
        // Check if attacker is player with attributes
        if (source.getAttacker() instanceof PlayerEntity player) {
            // BURNING: 3x damage while on fire
            if (hasAttribute(player, ArmourAttribute.BURNING) && player.isOnFire()) {
                // Apply 3x damage by modifying the damage source
                try {
                    // Use reflection to access and modify the damage amount
                    java.lang.reflect.Field damageField = source.getClass().getDeclaredField("damage");
                    damageField.setAccessible(true);
                    float originalDamage = damageField.getFloat(source);
                    damageField.setFloat(source, originalDamage * 3.0f);
                } catch (Exception e) {
                    // Fallback: apply extra damage after the fact
                    if (entity.getEntityWorld() instanceof ServerWorld) {
                        entity.damage((ServerWorld)entity.getEntityWorld(), entity.getEntityWorld().getDamageSources().magic(), amount * 2.0f);
                    }
                }
            }
            
            // FRENZIED: 1.5x damage with 10% chance of self-damage
            if (hasAttribute(player, ArmourAttribute.FRENZIED)) {
                if (Math.random() < 0.1) {
                    // 10% chance to take 1 heart damage
                    player.damage((ServerWorld)player.getEntityWorld(), player.getEntityWorld().getDamageSources().magic(), 2.0f);
                }
                // Apply 1.5x damage
                try {
                    java.lang.reflect.Field damageField = source.getClass().getDeclaredField("damage");
                    damageField.setAccessible(true);
                    float originalDamage = damageField.getFloat(source);
                    damageField.setFloat(source, originalDamage * 1.5f);
                } catch (Exception e) {
                    if (entity instanceof PlayerEntity && entity.getEntityWorld() instanceof ServerWorld) {
                        entity.damage((ServerWorld)entity.getEntityWorld(), entity.getEntityWorld().getDamageSources().magic(), amount * 0.5f);
                    }
                }
            }
            
            // CURSED: Life steal 20% of damage dealt
            if (hasAttribute(player, ArmourAttribute.CURSED) && entity instanceof LivingEntity) {
                float healAmount = amount * 0.2f;
                player.heal(healAmount);
            }
        }
    }
    
    private boolean hasAttribute(PlayerEntity player, ArmourAttribute attribute) {
        // Check all armor slots for the attribute
        for (net.minecraft.entity.EquipmentSlot slot : new net.minecraft.entity.EquipmentSlot[] {
            net.minecraft.entity.EquipmentSlot.HEAD,
            net.minecraft.entity.EquipmentSlot.CHEST,
            net.minecraft.entity.EquipmentSlot.LEGS,
            net.minecraft.entity.EquipmentSlot.FEET
        }) {
            if (SlayerItems.getAppliedAttribute(player.getEquippedStack(slot)) == attribute) {
                return true;
            }
        }
        return false;
    }
}
