package com.political;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public final class DragonSwordAbilities {
    private DragonSwordAbilities() {}

    public static void tickHeld(ServerPlayerEntity player) {
        ItemStack held = player.getMainHandStack();
        String id = SlayerItems.getCustomItemId(held);
        if (id == null || !id.startsWith("dragon_sword_")) return;
        int idx = parseIndex(id);
        if (idx < 1 || idx > 55) return;

        if (player.age % 14 != 0) return;
        int tier = (idx - 1) / 10;    // 0..4
        int within = (idx - 1) % 10; // 0..9
        int tierScaled = Math.min(tier, 3);
        ServerWorld world = (ServerWorld) player.getEntityWorld();
        var p = switch (within) {
            case 0 -> ParticleTypes.SMALL_FLAME;
            case 1 -> ParticleTypes.ENCHANT;
            case 2 -> ParticleTypes.ELECTRIC_SPARK;
            case 3 -> ParticleTypes.END_ROD;
            case 4 -> ParticleTypes.ASH;
            case 5 -> ParticleTypes.CRIT;
            case 6 -> ParticleTypes.CLOUD;
            case 7 -> ParticleTypes.BUBBLE_POP;
            case 8 -> ParticleTypes.LARGE_SMOKE;
            default -> ParticleTypes.WAX_ON;
        };
        // Slightly vary particle strength per tier, but keep it subtle.
        int count = 1 + tierScaled;
        world.spawnParticles(p, player.getX(), player.getBodyY(0.55), player.getZ(), count, 0.05, 0.08, 0.05, 0.0);
    }

    public static void onHit(ServerPlayerEntity attacker, LivingEntity target, ItemStack weapon) {
        String id = SlayerItems.getCustomItemId(weapon);
        if (id == null || !id.startsWith("dragon_sword_")) return;
        int idx = parseIndex(id);
        if (idx < 1 || idx > 55) return;

        int tier = (idx - 1) / 10;    // 0..4
        int within = (idx - 1) % 10; // 0..9
        ServerWorld world = (ServerWorld) attacker.getEntityWorld();
        int tierScaled = Math.min(tier, 3);
        float bonusDamage = 1.25f + tierScaled * 0.55f;
        target.damage(world, attacker.getDamageSources().playerAttack(attacker), bonusDamage);

        int debuffAmp = tierScaled; // 0..3
        int duration = 80 + tierScaled * 20;

        switch (within) {
            case 0 -> target.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, duration, debuffAmp, false, true, true));
            case 1 -> target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, duration, debuffAmp, false, true, true));
            case 2 -> target.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, duration, debuffAmp, false, true, true));
            case 3 -> target.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 60 + tierScaled * 20, 0, false, true, true));
            case 4 -> target.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 40 + tierScaled * 10, 0, false, true, true));
            case 5 -> target.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 60 + tier * 10, debuffAmp, false, true, true));
            case 6 -> target.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, duration, debuffAmp, false, true, true));
            case 7 -> target.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 60 + tierScaled * 10, debuffAmp, false, true, true));
            case 8 -> target.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 40 + tier * 5, 0, false, true, true));
            case 9 -> target.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 60 + tierScaled * 10, debuffAmp, false, true, true));
            default -> { }
        }

        // Extra drawback for higher sword tiers (subtle, balanced).
        if (tier >= 4) {
            if (within % 2 == 0) {
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 40, 0, false, true, true));
            } else {
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 40, 0, false, true, true));
            }
        }

        // Self-debuff to prevent overpowered stacking.
        int cadence = Math.max(10, 18 - tierScaled * 2);
        if (attacker.age % cadence == 0) {
            attacker.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 40 + tierScaled * 10, 0, true, false, false));
        }
    }

    private static int parseIndex(String id) {
        try {
            return Integer.parseInt(id.substring("dragon_sword_".length()));
        } catch (Exception ignored) {
            return -1;
        }
    }
}

