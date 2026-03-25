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
        if (idx < 1 || idx > 15) return;

        if (player.age % 14 != 0) return;
        ServerWorld world = (ServerWorld) player.getEntityWorld();
        var p = switch ((idx - 1) % 6) {
            case 0 -> ParticleTypes.SMALL_FLAME;
            case 1 -> ParticleTypes.ENCHANT;
            case 2 -> ParticleTypes.ELECTRIC_SPARK;
            case 3 -> ParticleTypes.END_ROD;
            case 4 -> ParticleTypes.ASH;
            default -> ParticleTypes.CRIT;
        };
        world.spawnParticles(p, player.getX(), player.getBodyY(0.55), player.getZ(), 1, 0.05, 0.08, 0.05, 0.0);
    }

    public static void onHit(ServerPlayerEntity attacker, LivingEntity target, ItemStack weapon) {
        String id = SlayerItems.getCustomItemId(weapon);
        if (id == null || !id.startsWith("dragon_sword_")) return;
        int idx = parseIndex(id);
        if (idx < 1 || idx > 15) return;

        ServerWorld world = (ServerWorld) attacker.getEntityWorld();
        int amp = (idx - 1) / 5; // 0..2
        target.damage(world, attacker.getDamageSources().playerAttack(attacker), 1.5f + amp);

        switch ((idx - 1) % 5) {
            case 0 -> target.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 80, amp, false, true, true));
            case 1 -> target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 80, amp, false, true, true));
            case 2 -> target.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 80, amp, false, true, true));
            case 3 -> target.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 60, 0, false, true, true));
            case 4 -> target.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 40, 0, false, true, true));
            default -> {}
        }

        // Self-debuff to prevent overpowered stacking.
        if (attacker.age % 16 == 0) {
            attacker.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 40, 0, true, false, false));
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

