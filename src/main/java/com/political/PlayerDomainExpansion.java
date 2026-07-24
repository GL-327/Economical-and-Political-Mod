package com.political;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;

import java.util.HashMap;
import java.util.Map;

/**
 * The Sorcerer's own Domain Expansion, the capstone of {@link SorcererManager}. A Special Grade
 * sorcerer can seal a small area of will around themselves: while it holds they strike and endure
 * far above normal, and every cursed thing caught inside is ground down.
 *
 * <p>State lives here (not on the profile), so activation is a pure effect field with no blocks
 * placed and nothing to clean up. It is gated hard: Special Grade only, a stiff cursed energy
 * cost, and a long cooldown, so it reads as a finisher and never as a rotation button.</p>
 */
public final class PlayerDomainExpansion {

    private PlayerDomainExpansion() {}

    private static final long DURATION_MS = 12_000L;
    private static final long COOLDOWN_MS = 3 * 60_000L;
    private static final long ENERGY_COST = 400L;
    private static final double RADIUS = 8.0;

    private static final Map<String, Long> activeUntil = new HashMap<>();
    private static final Map<String, Long> cooldownUntil = new HashMap<>();

    public static boolean isActive(String uuid) {
        Long until = activeUntil.get(uuid);
        return until != null && System.currentTimeMillis() < until;
    }

    public static long cooldownRemainingMs(String uuid) {
        Long until = cooldownUntil.get(uuid);
        return until == null ? 0L : Math.max(0L, until - System.currentTimeMillis());
    }

    /** Attempts to expand the player's domain. Returns false with feedback if it cannot fire. */
    public static boolean expand(ServerPlayer player) {
        String uuid = player.getUUID().toString();

        if (SorcererManager.rankOf(uuid) != SorcererRank.SPECIAL_GRADE) {
            player.sendSystemMessage(Component.literal("§cOnly a Special Grade sorcerer can expand a domain.")
                    .withStyle(ChatFormatting.RED));
            return false;
        }
        SorcererManager.PlayerSorcerer p = SorcererManager.getOrNull(uuid);
        if (p != null && SorcererManager.domainDisabled(p)) {
            player.sendSystemMessage(Component.literal("§cA binding vow forbids your domain.")
                    .withStyle(ChatFormatting.RED));
            return false;
        }
        long cd = cooldownRemainingMs(uuid);
        if (cd > 0) {
            player.sendSystemMessage(Component.literal("§cDomain not ready for " + (cd / 1000) + "s.")
                    .withStyle(ChatFormatting.RED));
            return false;
        }
        if (!CursedEnergyManager.spend(player, ENERGY_COST)) {
            player.sendSystemMessage(Component.literal("§cNot enough Cursed Energy (need " + ENERGY_COST + ").")
                    .withStyle(ChatFormatting.RED));
            return false;
        }

        long now = System.currentTimeMillis();
        activeUntil.put(uuid, now + DURATION_MS);
        cooldownUntil.put(uuid, now + COOLDOWN_MS);

        player.sendSystemMessage(Component.literal("§5§lDOMAIN EXPANSION")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
        if (player.level() instanceof ServerLevel sl) {
            // The full cinematic: the dome slams out, holds while the domain is up, then implodes.
            Vfx.domainCollapse(sl, player.position().add(0, 1, 0), RADIUS, Vfx.VOID);
            ring(sl, player, 40);
        }
        return true;
    }

    /** Ticked once per second per sorcerer from {@link SorcererManager}. */
    public static void tick(ServerPlayer player, SorcererManager.PlayerSorcerer p) {
        String uuid = player.getUUID().toString();
        if (!isActive(uuid)) return;
        if (!(player.level() instanceof ServerLevel sl)) return;

        // Sure hit: the sorcerer is untouchable-strong while the domain holds.
        player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 30, 2, true, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 30, 2, true, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 30, 1, true, false, false));

        for (LivingEntity e : player.level().getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(RADIUS),
                ent -> ent != player && ent.isAlive() && ent instanceof Monster)) {
            e.hurtServer(sl, player.damageSources().magic(), 4.0f);
            e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 1, true, false, false));
            e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 1, true, false, false));
        }
        if (player.tickCount % 6 == 0) ring(sl, player, 20);

        // Fade message on the tick the domain closes.
        Long until = activeUntil.get(uuid);
        if (until != null && System.currentTimeMillis() >= until - 1050L) {
            player.sendSystemMessage(Component.literal("§7Your domain fades.").withStyle(ChatFormatting.GRAY), true);
        }
    }

    public static void clear(String uuid) {
        activeUntil.remove(uuid);
    }

    private static void ring(ServerLevel sl, ServerPlayer player, int points) {
        for (int i = 0; i < points; i++) {
            double a = (Math.PI * 2 * i) / points;
            sl.sendParticles(ParticleTypes.SCULK_SOUL,
                    player.getX() + Math.cos(a) * RADIUS, player.getY() + 0.3, player.getZ() + Math.sin(a) * RADIUS,
                    1, 0.02, 0.4, 0.02, 0.01);
        }
    }
}
