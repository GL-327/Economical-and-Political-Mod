package com.political.mixin;

import com.political.bounty.SlayerItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class GoldArmorAbilityMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        
        if (!(entity instanceof Player player)) return;
        if (player.level() instanceof ServerLevel == false) return;
        
        // Check for gold armor set bonuses
        if (hasFullGoldArmorSet(player)) {
            applyGoldArmorAbilities(player);
        }
        
        // Check for netherite armor set bonuses  
        if (hasFullNetheriteArmorSet(player)) {
            applyNetheriteArmorAbilities(player);
        }
    }
    
    private boolean hasFullGoldArmorSet(Player player) {
        ItemStack helmet = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD);
        ItemStack chestplate = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST);
        ItemStack leggings = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.LEGS);
        ItemStack boots = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.FEET);
        
        return isGoldArmorPiece(helmet) && isGoldArmorPiece(chestplate) && 
               isGoldArmorPiece(leggings) && isGoldArmorPiece(boots);
    }
    
    private boolean hasFullNetheriteArmorSet(Player player) {
        ItemStack helmet = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD);
        ItemStack chestplate = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST);
        ItemStack leggings = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.LEGS);
        ItemStack boots = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.FEET);
        
        return isGildedNetheritePiece(helmet) && isGildedNetheritePiece(chestplate) && 
               isGildedNetheritePiece(leggings) && isGildedNetheritePiece(boots);
    }
    
    private boolean isGoldArmorPiece(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        String id = SlayerItems.getCustomItemId(stack);
        if (id == null) return false;
        if (id.contains("gilded_netherite")) return false;
        return id.contains("pure_gold") || id.contains("polished_gold")
                || id.contains("shiny_gold") || id.contains("glistening_gold")
                || (id.contains("gilded") && !id.contains("netherite"));
    }
    
    private boolean isGildedNetheritePiece(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        String id = SlayerItems.getCustomItemId(stack);
        return id != null && id.contains("gilded_netherite");
    }
    
    private void applyGoldArmorAbilities(Player player) {
        RandomSource random = player.getRandom();
        
        // Gold armor abilities:
        // - Luck effect (higher coin drops)
        // - Fire resistance 
        // - Speed boost
        if (random.nextInt(100) < 5) { // 5% chance per tick
            if (!player.hasEffect(MobEffects.LUCK)) {
                player.addEffect(new MobEffectInstance(MobEffects.LUCK, 200, 0, true, false, false));
            }
        }
        
        if (!player.hasEffect(MobEffects.FIRE_RESISTANCE)) {
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 400, 0, true, false, false));
        }
        
        if (random.nextInt(200) < 1) { // 0.5% chance per tick for speed
            if (!player.hasEffect(MobEffects.SPEED)) {
                player.addEffect(new MobEffectInstance(MobEffects.SPEED, 100, 0, true, false, false));
            }
        }
    }
    
    private void applyNetheriteArmorAbilities(Player player) {
        RandomSource random = player.getRandom();
        
        // Netherite armor abilities:
        // - Strength effect
        // - Resistance effect  
        // - Health boost
        // - Knockback resistance
        if (random.nextInt(100) < 10) { // 10% chance per tick
            if (!player.hasEffect(MobEffects.STRENGTH)) {
                player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 200, 0, true, false, false));
            }
        }
        
        if (!player.hasEffect(MobEffects.RESISTANCE)) {
            player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 400, 0, true, false, false));
        }
        
        if (random.nextInt(300) < 1) { // ~0.33% chance per tick for health boost
            if (!player.hasEffect(MobEffects.HEALTH_BOOST)) {
                player.addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST, 200, 0, true, false, false));
            }
        }
    }
}
