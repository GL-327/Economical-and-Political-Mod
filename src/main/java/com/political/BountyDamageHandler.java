package com.political;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class BountyDamageHandler {

    // Store modified damage for the actual damage application
    private static final java.util.Map<java.util.UUID, Float> pendingDamageModifiers = new java.util.HashMap<>();

    public static boolean onDamage(LivingEntity entity, DamageSource source, float amount) {

        // === CASE 1: Player taking damage from slayer boss ===
        if (entity instanceof ServerPlayerEntity player) {
            if (source.getAttacker() instanceof LivingEntity attacker) {
                if (SlayerManager.isSlayerBoss(attacker.getUuid())) {
                    SlayerManager.SlayerType bossType = SlayerManager.getBossSlayerType(attacker.getUuid());
                    if (bossType != null) {
                        double reduction = SlayerManager.getLevelDamageReduction(player, bossType);
                        if (reduction > 0) {
                            // Store the modifier
                            pendingDamageModifiers.put(player.getUuid(), 1.0f - (float) reduction);
                        }
                    }
                }
            }
        }

        // === CASE 2: Slayer boss taking damage from player ===
        if (SlayerManager.isSlayerBoss(entity.getUuid())) {
            if (source.getAttacker() instanceof ServerPlayerEntity player) {
                SlayerManager.SlayerType bossType = SlayerManager.getBossSlayerType(entity.getUuid());
                if (bossType != null) {
                    double multiplier = SlayerManager.getLevelDamageMultiplier(player, bossType);
                    if (multiplier > 1.0) {
                        // Store the modifier on the boss
                        pendingDamageModifiers.put(entity.getUuid(), (float) multiplier);
                    }
                }
            }
        }

        return true; // Allow damage to proceed
    }

    public static float getAndClearModifier(java.util.UUID entityUuid) {
        Float modifier = pendingDamageModifiers.remove(entityUuid);
        return modifier != null ? modifier : 1.0f;
    }
}