package com.political;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;

public final class DragonSwordAbilities {
    private DragonSwordAbilities() {}

    public static void tickHeld(ServerPlayer player) {
        ItemStack held = player.getMainHandItem();
        String id = SlayerItems.getCustomItemId(held);
        if (id == null || !id.startsWith("dragon_sword_")) return;
        int idx = parseIndex(id);
        if (idx < 1 || idx > 55) return;

        if (player.tickCount % 14 != 0) return;
        int tier = (idx - 1) / 10;    // 0..4
        int within = (idx - 1) % 10; // 0..9
        int tierScaled = Math.min(tier, 3);
        ServerLevel world = (ServerLevel) player.level();
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
        world.sendParticles(p, player.getX(), player.getY() + player.getBbHeight() * 0.55, player.getZ(), count, 0.05, 0.08, 0.05, 0.0);
    }

    public static void onHit(ServerPlayer attacker, LivingEntity target, ItemStack weapon) {
        String id = SlayerItems.getCustomItemId(weapon);
        if (id == null || !id.startsWith("dragon_sword_")) return;
        int idx = parseIndex(id);
        if (idx < 1 || idx > 55) return;

        int tier = (idx - 1) / 10;    // 0..4
        int within = (idx - 1) % 10; // 0..9
        ServerLevel world = (ServerLevel) attacker.level();
        int tierScaled = Math.min(tier, 3);
        float bonusDamage = 1.25f + tierScaled * 0.55f;
        target.hurtServer(world, attacker.damageSources().playerAttack(attacker), bonusDamage);

        int debuffAmp = tierScaled; // 0..3
        int duration = 80 + tierScaled * 20;

        switch (within) {
            case 0 -> target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, debuffAmp, false, true, true));
            case 1 -> target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, duration, debuffAmp, false, true, true));
            case 2 -> target.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, duration, debuffAmp, false, true, true));
            case 3 -> target.addEffect(new MobEffectInstance(MobEffects.POISON, 60 + tierScaled * 20, 0, false, true, true));
            case 4 -> target.addEffect(new MobEffectInstance(MobEffects.WITHER, 40 + tierScaled * 10, 0, false, true, true));
            case 5 -> target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60 + tier * 10, debuffAmp, false, true, true));
            case 6 -> target.addEffect(new MobEffectInstance(MobEffects.HUNGER, duration, debuffAmp, false, true, true));
            case 7 -> target.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 60 + tierScaled * 10, debuffAmp, false, true, true));
            case 8 -> target.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 40 + tier * 5, 0, false, true, true));
            case 9 -> target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60 + tierScaled * 10, debuffAmp, false, true, true));
            default -> { }
        }

        // Extra drawback for higher sword tiers (subtle, balanced).
        if (tier >= 4) {
            if (within % 2 == 0) {
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 0, false, true, true));
            } else {
                target.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 40, 0, false, true, true));
            }
        }

        // Self-debuff to prevent overpowered stacking.
        int cadence = Math.max(10, 18 - tierScaled * 2);
        if (attacker.tickCount % cadence == 0) {
            attacker.addEffect(new MobEffectInstance(MobEffects.HUNGER, 40 + tierScaled * 10, 0, true, false, false));
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

