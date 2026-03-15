package com.political;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.Blocks;
import net.minecraft.world.World;
import net.minecraft.registry.RegistryKey;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ArmourAttributeHandler {
    private static final Map<UUID, Integer> tickCounters = new HashMap<>();

    public static void tick(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        int ticks = tickCounters.getOrDefault(uuid, 0) + 1;
        tickCounters.put(uuid, ticks);

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot != EquipmentSlot.HEAD && slot != EquipmentSlot.CHEST && 
                slot != EquipmentSlot.LEGS && slot != EquipmentSlot.FEET) continue;
            ItemStack stack = player.getEquippedStack(slot);
            ArmourAttribute attr = SlayerItems.getAppliedAttribute(stack);
            if (attr == null) continue;

            // Handle attribute effects
            handleAttributeEffect(player, attr, ticks);
        }
    }

    private static void handleAttributeEffect(ServerPlayerEntity player, ArmourAttribute attr, int ticks) {
        switch (attr) {
            case BURNING -> {
                // 3x damage handled by AttributeDamageMixin
                // Fire immunity handled by BurningAttributeMixin
                if (player.isOnFire()) {
                    player.setFireTicks(200);
                }
            }
            case SIGHTLESS -> {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 220, 0, false, false, true));
                if (player.hasStatusEffect(StatusEffects.BLINDNESS)) {
                    player.removeStatusEffect(StatusEffects.BLINDNESS);
                }
                // Downside: 20% less damage from all sources
                // This would need to be implemented in a damage mixin
            }
            case FRENZIED -> {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 40, 1, false, false, true)); // Haste II
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 40, 0, false, false, true)); // Strength I
                if (ticks % 100 == 0) {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 60, 0, false, false, true));
                }
                // Self-damage handled by AttributeDamageMixin
            }
            case GROUNDED -> {
                // Lightning immunity handled by mixin
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 40, 0, false, false, true)); // Jump reduction
                // Downside: 30% slower movement speed
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 40, 1, false, false, true)); // Slowness I
            }
            case WEBBED -> {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 40, 1, false, false, true)); // Speed I
                // Downside: 20% weakness
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 40, 0, false, false, true)); // Weakness I
                // Spider neutrality handled by mixin
            }
            case FROST -> {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 40, 0, false, false, true));
                // Frost walker handled by mixin
                // Downside: Slowness in hot dimensions
                if (player.getEntityWorld().getDimension().hasCeiling()) { // Nether-like
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 40, 1, false, false, true));
                }
            }
            case PHANTOMSTEP -> {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 40, 1, false, false, true)); // Jump Boost I
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 40, 1, false, false, true)); // Slow Falling I
                // Downside: 10% less damage
                // This would need to be implemented in a damage mixin
            }
            case CURSED -> {
                // Life steal handled by AttributeDamageMixin
                // Downside: 2 hearts less max health
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 40, 0, false, false, true)); // Weakness I
            }
            case OVERGROWN -> {
                if (ticks % 40 == 0 && !player.isSprinting() && isNaturalBlock(player.getEntityWorld(), player.getBlockPos().down())) {
                    player.heal(1.0f);
                }
                // Downside: 20% slower in non-natural dimensions
                if (!(player.getEntityWorld().getRegistryKey() == World.OVERWORLD)) {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 40, 0, false, false, true));
                }
            }
            case VOLATILE -> {
                // Explosion on low health handled by existing logic
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 40, 0, false, false, true));
                // Downside: 1 heart less max health
                // This would need to be implemented with attribute modifiers
            }
        }
    }

    private static boolean isNaturalBlock(World world, BlockPos pos) {
        return world.getBlockState(pos).isOf(Blocks.GRASS_BLOCK) || 
               world.getBlockState(pos).isOf(Blocks.DIRT) || 
               world.getBlockState(pos).isOf(Blocks.MOSS_BLOCK);
    }
}
