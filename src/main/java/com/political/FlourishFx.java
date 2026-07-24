package com.political;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/**
 * V7 Pillar C — a small library of short, high quality particle and sound "flourishes"
 * keyed by a theme name. Every older system can add a touch of modern polish with a single
 * static call and no new state, for example:
 *
 * <pre>{@code FlourishFx.play(serverLevel, pos, FlourishFx.Kind.LEVEL_UP);}</pre>
 *
 * <p>Each flourish is a tasteful layered burst. Nothing here changes gameplay. It only draws
 * particles and plays sounds, so it is always safe to call and always optional.
 */
public final class FlourishFx {

    private FlourishFx() {}

    /** The set of themed flourishes other systems can trigger. */
    public enum Kind {
        /** A spell or ability going off. Light, quick, purple sparks. */
        CAST,
        /** A hit landing. Sharp crit and sweep burst. */
        IMPACT,
        /** Restoration. Soft green hearts and chime. */
        HEAL,
        /** A buff or blessing settling on a player. Golden rising motes. */
        BUFF,
        /** Something being unlocked, learned or discovered. Amethyst shimmer. */
        UNLOCK,
        /** A level or rank going up. Bright celebratory column. */
        LEVEL_UP,
        /** A boss or named horror entering the world. Dark, heavy, ominous. */
        BOSS_SPAWN
    }

    /**
     * Plays the flourish for the given theme at the centre of the block position.
     * Safe to call from any server side context. Null arguments are ignored.
     */
    public static void play(ServerLevel level, BlockPos pos, Kind kind) {
        if (level == null || pos == null || kind == null) return;
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 1.0;
        double z = pos.getZ() + 0.5;
        play(level, x, y, z, kind);
    }

    /**
     * Plays the flourish for the given theme at exact coordinates.
     * Safe to call from any server side context. A null level or kind is ignored.
     */
    public static void play(ServerLevel level, double x, double y, double z, Kind kind) {
        if (level == null || kind == null) return;
        switch (kind) {
            case CAST -> cast(level, x, y, z);
            case IMPACT -> impact(level, x, y, z);
            case HEAL -> heal(level, x, y, z);
            case BUFF -> buff(level, x, y, z);
            case UNLOCK -> unlock(level, x, y, z);
            case LEVEL_UP -> levelUp(level, x, y, z);
            case BOSS_SPAWN -> bossSpawn(level, x, y, z);
        }
    }

    // ── themed flourishes ──

    private static void cast(ServerLevel level, double x, double y, double z) {
        ring(level, ParticleTypes.ELECTRIC_SPARK, x, y, z, 0.6, 18);
        burst(level, ParticleTypes.ENCHANT, x, y + 0.4, z, 14, 0.5, 0.02);
        sound(level, x, y, z, SoundEvents.AMETHYST_BLOCK_CHIME, 0.6f, 1.5f);
        sound(level, x, y, z, SoundEvents.EVOKER_PREPARE_ATTACK, 0.4f, 1.8f);
    }

    private static void impact(ServerLevel level, double x, double y, double z) {
        burst(level, ParticleTypes.CRIT, x, y, z, 20, 0.5, 0.25);
        burst(level, ParticleTypes.ENCHANTED_HIT, x, y, z, 10, 0.4, 0.15);
        level.sendParticles(ParticleTypes.SWEEP_ATTACK, x, y, z, 1, 0, 0, 0, 0);
        sound(level, x, y, z, SoundEvents.PLAYER_ATTACK_CRIT, 0.8f, 1.0f);
    }

    private static void heal(ServerLevel level, double x, double y, double z) {
        burst(level, ParticleTypes.HEART, x, y + 0.3, z, 8, 0.4, 0.05);
        burst(level, ParticleTypes.HAPPY_VILLAGER, x, y, z, 12, 0.5, 0.08);
        sound(level, x, y, z, SoundEvents.AMETHYST_BLOCK_RESONATE, 0.7f, 1.4f);
    }

    private static void buff(ServerLevel level, double x, double y, double z) {
        column(level, ParticleTypes.END_ROD, x, y, z, 14, 1.2);
        burst(level, ParticleTypes.GLOW, x, y + 0.6, z, 12, 0.5, 0.03);
        sound(level, x, y, z, SoundEvents.BEACON_ACTIVATE, 0.5f, 1.6f);
        sound(level, x, y, z, SoundEvents.AMETHYST_BLOCK_CHIME, 0.6f, 1.2f);
    }

    private static void unlock(ServerLevel level, double x, double y, double z) {
        ring(level, ParticleTypes.SCULK_SOUL, x, y, z, 0.7, 16);
        column(level, ParticleTypes.END_ROD, x, y, z, 10, 1.0);
        burst(level, ParticleTypes.ENCHANT, x, y + 0.5, z, 20, 0.6, 0.03);
        sound(level, x, y, z, SoundEvents.AMETHYST_BLOCK_CHIME, 0.8f, 1.0f);
        sound(level, x, y, z, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 0.5f, 1.5f);
    }

    private static void levelUp(ServerLevel level, double x, double y, double z) {
        column(level, ParticleTypes.END_ROD, x, y, z, 22, 1.8);
        ring(level, ParticleTypes.TOTEM_OF_UNDYING, x, y + 0.2, z, 0.8, 20);
        burst(level, ParticleTypes.FIREWORK, x, y + 1.0, z, 24, 0.6, 0.12);
        sound(level, x, y, z, SoundEvents.PLAYER_LEVELUP, 1.0f, 1.2f);
        sound(level, x, y, z, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 0.7f, 1.0f);
    }

    private static void bossSpawn(ServerLevel level, double x, double y, double z) {
        burst(level, ParticleTypes.SCULK_SOUL, x, y + 0.5, z, 40, 1.2, 0.06);
        burst(level, ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 24, 0.8, 0.05);
        ring(level, ParticleTypes.REVERSE_PORTAL, x, y, z, 1.4, 40);
        level.sendParticles(ParticleTypes.SONIC_BOOM, x, y + 0.5, z, 1, 0, 0, 0, 0);
        sound(level, x, y, z, SoundEvents.WARDEN_ROAR, 1.0f, 0.7f);
        sound(level, x, y, z, SoundEvents.END_PORTAL_SPAWN, 0.8f, 0.6f);
    }

    // ── low level shape helpers ──

    private static void burst(ServerLevel level, ParticleOptions p, double x, double y, double z,
                              int count, double spread, double speed) {
        level.sendParticles(p, x, y, z, count, spread, spread, spread, speed);
    }

    private static void column(ServerLevel level, ParticleOptions p, double x, double y, double z,
                               int count, double height) {
        for (int i = 0; i < count; i++) {
            double dy = height * i / Math.max(1, count);
            level.sendParticles(p, x, y + dy, z, 1, 0.18, 0.05, 0.18, 0.02);
        }
    }

    private static void ring(ServerLevel level, ParticleOptions p, double x, double y, double z,
                             double radius, int points) {
        for (int i = 0; i < points; i++) {
            double a = (Math.PI * 2.0) * i / points;
            double px = x + Math.cos(a) * radius;
            double pz = z + Math.sin(a) * radius;
            level.sendParticles(p, px, y + 0.1, pz, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    private static void sound(ServerLevel level, double x, double y, double z,
                              SoundEvent event, float volume, float pitch) {
        level.playSound(null, x, y, z, event, SoundSource.PLAYERS, volume, pitch);
    }
}
