package com.political;
import com.political.mixin.AttributeDamageMixin;
import com.political.mixin.BurningAttributeMixin;



import com.political.bounty.SlayerItems;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.Level;
import net.minecraft.resources.ResourceKey;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ArmourAttributeHandler {
    private static final Map<UUID, Integer> tickCounters = new HashMap<>();

    public static void tick(ServerPlayer player) {
        UUID uuid = player.getUUID();
        int ticks = tickCounters.getOrDefault(uuid, 0) + 1;
        tickCounters.put(uuid, ticks);

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot != EquipmentSlot.HEAD && slot != EquipmentSlot.CHEST && 
                slot != EquipmentSlot.LEGS && slot != EquipmentSlot.FEET) continue;
            ItemStack stack = player.getItemBySlot(slot);
            ArmourAttribute attr = SlayerItems.getAppliedAttribute(stack);
            if (attr == null) continue;

            // Handle attribute effects
            handleAttributeEffect(player, attr, ticks);
        }
    }

    private static void handleAttributeEffect(ServerPlayer player, ArmourAttribute attr, int ticks) {
        switch (attr) {
            case BURNING -> {
                // 3x damage handled by AttributeDamageMixin
                // Fire immunity handled by BurningAttributeMixin
                if (player.isOnFire()) {
                    player.setRemainingFireTicks(200);
                }
            }
            case SIGHTLESS -> {
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 220, 0, false, false, true));
                if (player.hasEffect(MobEffects.BLINDNESS)) {
                    player.removeEffect(MobEffects.BLINDNESS);
                }
                // Downside: 20% less damage from all sources
                // This would need to be implemented in a damage mixin
            }
            case FRENZIED -> {
                player.addEffect(new MobEffectInstance(MobEffects.HASTE, 40, 1, false, false, true)); // Haste II
                player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 40, 0, false, false, true)); // Strength I
                if (ticks % 100 == 0) {
                    player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 60, 0, false, false, true));
                }
                // Self-damage handled by AttributeDamageMixin
            }
            case GROUNDED -> {
                // Lightning immunity handled by mixin
                player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 0, false, false, true)); // Jump reduction
                // Downside: 30% slower movement speed
                player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 1, false, false, true)); // Slowness I
            }
            case WEBBED -> {
                player.addEffect(new MobEffectInstance(MobEffects.SPEED, 40, 1, false, false, true)); // Speed I
                // Downside: 20% weakness
                player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 0, false, false, true)); // Weakness I
                // Spider neutrality handled by mixin
            }
            case FROST -> {
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 40, 0, false, false, true));
                // Frost walker handled by mixin
                // Downside: Slowness in hot dimensions
                if (player.level().dimensionType().hasCeiling()) { // Nether-like
                    player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 1, false, false, true));
                }
            }
            case PHANTOMSTEP -> {
                player.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 40, 1, false, false, true)); // Jump Boost I
                player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 40, 1, false, false, true)); // Slow Falling I
                // Downside: 10% less damage
                // This would need to be implemented in a damage mixin
            }
            case CURSED -> {
                // Life steal handled by AttributeDamageMixin
                // Downside: 2 hearts less max health
                player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 0, false, false, true)); // Weakness I
            }
            case OVERGROWN -> {
                if (ticks % 40 == 0 && !player.isSprinting() && isNaturalBlock(player.level(), player.blockPosition().below())) {
                    player.heal(1.0f);
                }
                // Downside: 20% slower in non-natural dimensions
                if (!(player.level().dimension() == Level.OVERWORLD)) {
                    player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 0, false, false, true));
                }
            }
            case VOLATILE -> {
                // Explosion on low health handled by existing logic
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 40, 0, false, false, true));
                // Downside: 1 heart less max health
                // This would need to be implemented with attribute modifiers
            }
        }
    }

    private static boolean isNaturalBlock(Level world, BlockPos pos) {
        return world.getBlockState(pos).is(Blocks.GRASS_BLOCK) || 
               world.getBlockState(pos).is(Blocks.DIRT) || 
               world.getBlockState(pos).is(Blocks.MOSS_BLOCK);
    }
}
