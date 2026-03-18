package com.political.mixin;

import com.political.SlayerItems;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class GoldArmorAbilityMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        
        if (!(entity instanceof PlayerEntity player)) return;
        if (player.getEntityWorld() instanceof ServerWorld == false) return;
        
        // Check for gold armor set bonuses
        if (hasFullGoldArmorSet(player)) {
            applyGoldArmorAbilities(player);
        }
        
        // Check for netherite armor set bonuses  
        if (hasFullNetheriteArmorSet(player)) {
            applyNetheriteArmorAbilities(player);
        }
    }
    
    private boolean hasFullGoldArmorSet(PlayerEntity player) {
        ItemStack helmet = player.getEquippedStack(net.minecraft.entity.EquipmentSlot.HEAD);
        ItemStack chestplate = player.getEquippedStack(net.minecraft.entity.EquipmentSlot.CHEST);
        ItemStack leggings = player.getEquippedStack(net.minecraft.entity.EquipmentSlot.LEGS);
        ItemStack boots = player.getEquippedStack(net.minecraft.entity.EquipmentSlot.FEET);
        
        return isGoldArmorPiece(helmet) && isGoldArmorPiece(chestplate) && 
               isGoldArmorPiece(leggings) && isGoldArmorPiece(boots);
    }
    
    private boolean hasFullNetheriteArmorSet(PlayerEntity player) {
        ItemStack helmet = player.getEquippedStack(net.minecraft.entity.EquipmentSlot.HEAD);
        ItemStack chestplate = player.getEquippedStack(net.minecraft.entity.EquipmentSlot.CHEST);
        ItemStack leggings = player.getEquippedStack(net.minecraft.entity.EquipmentSlot.LEGS);
        ItemStack boots = player.getEquippedStack(net.minecraft.entity.EquipmentSlot.FEET);
        
        return isGildedNetheritePiece(helmet) && isGildedNetheritePiece(chestplate) && 
               isGildedNetheritePiece(leggings) && isGildedNetheritePiece(boots);
    }
    
    private boolean isGoldArmorPiece(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        String id = SlayerItems.getCustomItemId(stack);
        return id != null && (id.contains("pure_gold") || id.contains("polished_gold") || 
                            id.contains("shiny_gold") || id.contains("glistening_gold") || 
                            id.contains("gilded"));
    }
    
    private boolean isGildedNetheritePiece(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        String id = SlayerItems.getCustomItemId(stack);
        return id != null && id.contains("gilded_netherite");
    }
    
    private void applyGoldArmorAbilities(PlayerEntity player) {
        Random random = player.getRandom();
        
        // Gold armor abilities:
        // - Luck effect (higher coin drops)
        // - Fire resistance 
        // - Speed boost
        if (random.nextInt(100) < 5) { // 5% chance per tick
            if (!player.hasStatusEffect(StatusEffects.LUCK)) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.LUCK, 200, 0, true, false, false));
            }
        }
        
        if (!player.hasStatusEffect(StatusEffects.FIRE_RESISTANCE)) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 400, 0, true, false, false));
        }
        
        if (random.nextInt(200) < 1) { // 0.5% chance per tick for speed
            if (!player.hasStatusEffect(StatusEffects.SPEED)) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 100, 0, true, false, false));
            }
        }
    }
    
    private void applyNetheriteArmorAbilities(PlayerEntity player) {
        Random random = player.getRandom();
        
        // Netherite armor abilities:
        // - Strength effect
        // - Resistance effect  
        // - Health boost
        // - Knockback resistance
        if (random.nextInt(100) < 10) { // 10% chance per tick
            if (!player.hasStatusEffect(StatusEffects.STRENGTH)) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 200, 0, true, false, false));
            }
        }
        
        if (!player.hasStatusEffect(StatusEffects.RESISTANCE)) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 400, 0, true, false, false));
        }
        
        if (random.nextInt(300) < 1) { // ~0.33% chance per tick for health boost
            if (!player.hasStatusEffect(StatusEffects.HEALTH_BOOST)) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.HEALTH_BOOST, 200, 0, true, false, false));
            }
        }
    }
}
