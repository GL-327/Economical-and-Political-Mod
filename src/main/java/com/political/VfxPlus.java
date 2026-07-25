package com.political;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * The second wave of cinematic set pieces, built on the {@link Vfx} scheduler.
 *
 * <p>Everything here follows the same discipline as the engine: a charge that tells you something
 * is coming, a payoff that reads clearly, and an aftermath so the moment does not just stop. Each
 * one is layered from a dense core, a sheath for thickness and a loose haze, with sound underneath.</p>
 *
 * <p>None of this changes a single gameplay number. It is purely how things look.</p>
 */
public final class VfxPlus {

    private VfxPlus() {}

    // ── Orbital strike: a marker paints the ground, then a column falls ─────

    public static void orbitalStrike(ServerLevel level, Vec3 at, Vfx.Palette p) {
        Vfx.spawn(level, (lvl, f) -> {
            if (f < 18) {
                // Targeting rings tighten on the spot so you know it is coming.
                double r = 4.5 - (f / 18.0) * 3.6;
                Vfx.ring(lvl, p.sheath, at.add(0, 0.15, 0), r, 26);
                if (f % 6 == 0) {
                    Vfx.ring(lvl, p.core, at.add(0, 0.15, 0), r * 0.6, 16);
                    lvl.playSound(null, bp(at), SoundEvents.BEACON_ACTIVATE,
                            net.minecraft.sounds.SoundSource.PLAYERS, 0.5f, 0.8f + f * 0.05f);
                }
                return true;
            }
            if (f == 18) {
                lvl.playSound(null, bp(at), SoundEvents.WARDEN_SONIC_BOOM,
                        net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 0.7f);
                return true;
            }
            int a = f - 18;
            if (a <= 6) {
                // The column itself, dropping in fast.
                double top = 40 - a * 6.5;
                Vfx.line(lvl, p.core, at.add(0, Math.max(0, top), 0), at, 0.35);
                Vfx.helix(lvl, p.sheath, at.add(0, Math.max(0, top), 0), at, 0.7, 5, 40, a * 0.8);
                return true;
            }
            if (a <= 14) {
                double r = (a - 6) * 1.5;
                Vfx.ring(lvl, p.core, at.add(0, 0.2, 0), r, 30);
                Vfx.ring(lvl, p.haze, at.add(0, 0.2, 0), r * 0.75, 22);
                return true;
            }
            return false;
        });
    }

    // ── Chain lightning: arcs from target to target ────────────────────────

    public static void chainLightning(ServerLevel level, Vec3 from, List<Vec3> targets) {
        if (targets == null || targets.isEmpty()) return;
        List<Vec3> hops = new ArrayList<>(targets);
        Vfx.spawn(level, (lvl, f) -> {
            if (f >= hops.size()) return f < hops.size() + 4;
            Vec3 a = f == 0 ? from : hops.get(f - 1);
            Vec3 b = hops.get(f);
            Vfx.arc(lvl, ParticleTypes.ELECTRIC_SPARK, a, b, 0.9, 8);
            Vfx.arc(lvl, ParticleTypes.END_ROD, a, b, 0.3, 8);
            Vfx.puff(lvl, ParticleTypes.ELECTRIC_SPARK, b, 14, 0.35, 0.3);
            lvl.playSound(null, bp(b), SoundEvents.LIGHTNING_BOLT_IMPACT,
                    net.minecraft.sounds.SoundSource.PLAYERS, 0.5f, 1.4f + f * 0.1f);
            return true;
        });
    }

    // ── Flame pillar: erupts upward and burns down ─────────────────────────

    public static void flamePillar(ServerLevel level, Vec3 at) {
        Vfx.spawn(level, (lvl, f) -> {
            if (f <= 10) {
                double h = f * 0.85;
                for (double y = 0; y <= h; y += 0.4) {
                    double rr = 0.9 * (1 - y / (h + 0.5)) + 0.25;
                    Vfx.ring(lvl, ParticleTypes.FLAME, at.add(0, y, 0), rr, 8);
                }
                Vfx.puff(lvl, ParticleTypes.LARGE_SMOKE, at.add(0, h, 0), 6, 0.4, 0.05);
                if (f == 0) lvl.playSound(null, bp(at), SoundEvents.BLAZE_SHOOT,
                        net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 0.6f);
                return true;
            }
            if (f <= 20) {
                Vfx.ring(lvl, ParticleTypes.FLAME, at.add(0, 0.2, 0), 1.4, 14);
                Vfx.puff(lvl, ParticleTypes.SOUL_FIRE_FLAME, at.add(0, 0.6, 0), 4, 0.5, 0.02);
                return true;
            }
            return false;
        });
    }

    // ── Singularity: drags everything in, then detonates ───────────────────

    public static void singularity(ServerLevel level, Vec3 at, Vfx.Palette p) {
        Vfx.spawn(level, (lvl, f) -> {
            if (f <= 24) {
                // Particles spiral inward toward the core.
                double t = f / 24.0;
                double r = 6.0 * (1 - t) + 0.4;
                for (int i = 0; i < 12; i++) {
                    double a = (Math.PI * 2 * i) / 12 + f * 0.35;
                    Vfx.dot(lvl, p.sheath, at.add(Math.cos(a) * r, Math.sin(a * 1.7) * r * 0.5, Math.sin(a) * r));
                }
                Vfx.shell(lvl, p.haze, at, Math.max(0.3, r * 0.35), 10);
                if (f % 8 == 0) lvl.playSound(null, bp(at), SoundEvents.PORTAL_TRAVEL,
                        net.minecraft.sounds.SoundSource.PLAYERS, 0.4f, 0.4f + f * 0.03f);
                return true;
            }
            if (f == 25) {
                lvl.playSound(null, bp(at), SoundEvents.GENERIC_EXPLODE.value(),
                        net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 0.5f);
                Vfx.puff(lvl, p.core, at, 90, 0.3, 1.1);
                return true;
            }
            int a2 = f - 25;
            if (a2 <= 8) {
                Vfx.shell(lvl, p.core, at, a2 * 0.9, 44);
                return true;
            }
            return false;
        });
    }

    // ── Blade slash: a clean arc through the air ───────────────────────────

    public static void bladeSlash(ServerLevel level, Vec3 origin, Vec3 look, Vfx.Palette p) {
        Vec3 dir = look.normalize();
        Vec3 up = Math.abs(dir.y) > 0.99 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        Vec3 right = dir.cross(up).normalize();
        Vec3 realUp = right.cross(dir).normalize();

        Vfx.spawn(level, (lvl, f) -> {
            if (f > 5) return false;
            double sweep = Math.PI * 1.1;
            double start = -sweep / 2;
            int pts = 26;
            for (int i = 0; i < pts; i++) {
                double a = start + (sweep * i) / pts;
                double rad = 2.6 - f * 0.15;
                Vec3 off = right.scale(Math.cos(a) * rad).add(realUp.scale(Math.sin(a) * rad));
                Vfx.dot(lvl, f == 0 ? p.core : p.sheath, origin.add(dir.scale(1.1)).add(off));
            }
            if (f == 0) lvl.playSound(null, bp(origin), SoundEvents.PLAYER_ATTACK_SWEEP,
                    net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 0.8f);
            return true;
        });
    }

    // ── Shockwave cone: a blast that fans out ahead of you ─────────────────

    public static void shockCone(ServerLevel level, Vec3 origin, Vec3 look, Vfx.Palette p) {
        Vec3 dir = look.normalize();
        Vfx.spawn(level, (lvl, f) -> {
            if (f > 8) return false;
            double dist = f * 1.3;
            double spread = 0.35 + f * 0.07;
            for (int i = -6; i <= 6; i++) {
                double a = i * spread / 6;
                Vec3 rot = new Vec3(
                        dir.x * Math.cos(a) - dir.z * Math.sin(a), dir.y,
                        dir.x * Math.sin(a) + dir.z * Math.cos(a));
                Vfx.dot(lvl, p.core, origin.add(rot.normalize().scale(dist)));
                Vfx.dot(lvl, p.haze, origin.add(rot.normalize().scale(dist * 0.85)).add(0, 0.4, 0));
            }
            if (f == 0) lvl.playSound(null, bp(origin), SoundEvents.WARDEN_SONIC_BOOM,
                    net.minecraft.sounds.SoundSource.PLAYERS, 0.7f, 1.5f);
            return true;
        });
    }

    // ── Healing rain: a soft field that falls over an area ─────────────────

    public static void healingRain(ServerLevel level, Vec3 centre, double radius) {
        Vfx.spawn(level, (lvl, f) -> {
            if (f > 40) return false;
            for (int i = 0; i < 6; i++) {
                double a = lvl.getRandom().nextDouble() * Math.PI * 2;
                double r = lvl.getRandom().nextDouble() * radius;
                Vec3 at = centre.add(Math.cos(a) * r, 3.5 - (f % 10) * 0.3, Math.sin(a) * r);
                Vfx.dot(lvl, ParticleTypes.END_ROD, at);
                if (i % 3 == 0) Vfx.dot(lvl, ParticleTypes.HEART, at.add(0, -0.4, 0));
            }
            if (f % 12 == 0) lvl.playSound(null, bp(centre), SoundEvents.AMETHYST_BLOCK_CHIME,
                    net.minecraft.sounds.SoundSource.PLAYERS, 0.4f, 1.6f);
            return true;
        });
    }

    // ── Frost nova: crystallises outward from the caster ───────────────────

    public static void frostNova(ServerLevel level, Vec3 centre) {
        Vfx.spawn(level, (lvl, f) -> {
            if (f > 12) return false;
            double r = f * 0.9;
            Vfx.ring(lvl, ParticleTypes.SNOWFLAKE, centre.add(0, 0.2, 0), r, 30);
            Vfx.ring(lvl, ParticleTypes.END_ROD, centre.add(0, 0.35, 0), r * 0.9, 18);
            // Spikes rising from the ring give it a crystal read.
            if (f % 2 == 0) {
                for (int i = 0; i < 8; i++) {
                    double a = (Math.PI * 2 * i) / 8 + f * 0.2;
                    Vec3 base = centre.add(Math.cos(a) * r, 0.1, Math.sin(a) * r);
                    Vfx.line(lvl, ParticleTypes.SNOWFLAKE, base, base.add(0, 1.1, 0), 0.3);
                }
            }
            if (f == 0) lvl.playSound(null, bp(centre), SoundEvents.GLASS_BREAK,
                    net.minecraft.sounds.SoundSource.PLAYERS, 0.8f, 1.5f);
            return true;
        });
    }

    // ── Summoning circle: rotating glyph rings ─────────────────────────────

    public static void summoningCircle(ServerLevel level, Vec3 centre, Vfx.Palette p) {
        Vfx.spawn(level, (lvl, f) -> {
            if (f > 34) return false;
            double grow = Math.min(1.0, f / 10.0);
            // Three rings turning at different speeds.
            for (int ring = 0; ring < 3; ring++) {
                double rr = (1.2 + ring * 0.7) * grow;
                int pts = 12 + ring * 6;
                double spin = f * (0.18 - ring * 0.05) * (ring % 2 == 0 ? 1 : -1);
                for (int i = 0; i < pts; i++) {
                    double a = (Math.PI * 2 * i) / pts + spin;
                    Vfx.dot(lvl, ring == 1 ? p.core : p.sheath,
                            centre.add(Math.cos(a) * rr, 0.12 + ring * 0.05, Math.sin(a) * rr));
                }
            }
            if (f > 24) Vfx.puff(lvl, p.haze, centre.add(0, 0.5, 0), 4, 0.4, 0.06);
            if (f == 0) lvl.playSound(null, bp(centre), SoundEvents.BEACON_ACTIVATE,
                    net.minecraft.sounds.SoundSource.PLAYERS, 0.7f, 0.9f);
            return true;
        });
    }

    // ── Soul harvest: wisps drawn back to the caster ───────────────────────

    public static void soulHarvest(ServerLevel level, Vec3 from, Vec3 toCaster) {
        Vfx.spawn(level, (lvl, f) -> {
            if (f > 14) return false;
            double t = f / 14.0;
            Vec3 at = from.add(toCaster.subtract(from).scale(t)).add(0, Math.sin(t * Math.PI) * 1.2, 0);
            Vfx.puff(lvl, ParticleTypes.SCULK_SOUL, at, 5, 0.16, 0.02);
            Vfx.dot(lvl, ParticleTypes.SOUL_FIRE_FLAME, at);
            if (f == 14) {
                Vfx.puff(lvl, ParticleTypes.SOUL, toCaster, 16, 0.3, 0.1);
                lvl.playSound(null, bp(toCaster), SoundEvents.EXPERIENCE_ORB_PICKUP,
                        net.minecraft.sounds.SoundSource.PLAYERS, 0.8f, 0.6f);
            }
            return true;
        });
    }

    // ── Comet volley: several comets rain in on an area ────────────────────

    public static void cometVolley(ServerLevel level, Vec3 centre, int count) {
        for (int i = 0; i < count; i++) {
            final int delay = i * 5;
            Vfx.spawn(level, (lvl, f) -> {
                if (f < delay) return true;
                int k = f - delay;
                if (k > 16) return false;
                double a = (Math.PI * 2 * delay) / Math.max(1, count * 5);
                Vec3 target = centre.add(Math.cos(a) * 3.0, 0, Math.sin(a) * 3.0);
                Vec3 start = target.add(4, 22, 4);
                double t = k / 16.0;
                Vec3 at = start.add(target.subtract(start).scale(t));
                Vfx.shell(lvl, ParticleTypes.FLAME, at, 0.45, 10);
                Vfx.puff(lvl, ParticleTypes.LARGE_SMOKE, at, 4, 0.3, 0.02);
                if (k == 16) {
                    Vfx.ring(lvl, ParticleTypes.FLAME, target.add(0, 0.15, 0), 1.8, 16);
                    lvl.playSound(null, bp(target), SoundEvents.GENERIC_EXPLODE.value(),
                            net.minecraft.sounds.SoundSource.PLAYERS, 0.7f, 1.1f);
                }
                return true;
            });
        }
    }

    // ── Shield dome: flickers into being around a defender ─────────────────

    public static void shieldDome(ServerLevel level, Vec3 centre, double radius, Vfx.Palette p) {
        Vfx.spawn(level, (lvl, f) -> {
            if (f > 30) return false;
            double flicker = 1.0 + Math.sin(f * 0.6) * 0.04;
            Vfx.shell(lvl, p.sheath, centre, radius * flicker, f % 3 == 0 ? 40 : 20);
            if (f % 5 == 0) Vfx.ring(lvl, p.core, centre, radius * flicker, 24);
            if (f == 0) lvl.playSound(null, bp(centre), SoundEvents.BEACON_ACTIVATE,
                    net.minecraft.sounds.SoundSource.PLAYERS, 0.6f, 1.5f);
            return true;
        });
    }

    // ── Ground fissure: cracks running outward ─────────────────────────────

    public static void fissure(ServerLevel level, Vec3 at, Vec3 look) {
        Vec3 dir = look.normalize();
        Vfx.spawn(level, (lvl, f) -> {
            if (f > 12) return false;
            double d = f * 1.4;
            Vec3 head = at.add(dir.scale(d));
            Vfx.puff(lvl, ParticleTypes.LARGE_SMOKE, head.add(0, 0.2, 0), 8, 0.3, 0.05);
            Vfx.puff(lvl, ParticleTypes.CRIT, head.add(0, 0.3, 0), 6, 0.35, 0.12);
            // Side cracks branching off the main line.
            if (f % 3 == 0) {
                Vec3 side = new Vec3(-dir.z, 0, dir.x).scale(1.2);
                Vfx.line(lvl, ParticleTypes.LARGE_SMOKE, head, head.add(side), 0.3);
                Vfx.line(lvl, ParticleTypes.LARGE_SMOKE, head, head.subtract(side), 0.3);
            }
            if (f == 0) lvl.playSound(null, bp(at), SoundEvents.ANVIL_LAND,
                    net.minecraft.sounds.SoundSource.PLAYERS, 0.7f, 0.5f);
            return true;
        });
    }

    // ── Execution flash: the finisher ──────────────────────────────────────

    public static void executionFlash(ServerLevel level, Vec3 at, Vfx.Palette p) {
        Vfx.spawn(level, (lvl, f) -> {
            if (f > 8) return false;
            if (f == 0) {
                Vfx.puff(lvl, p.core, at, 60, 0.1, 1.4);
                lvl.playSound(null, bp(at), SoundEvents.PLAYER_ATTACK_CRIT,
                        net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 0.6f);
                lvl.playSound(null, bp(at), SoundEvents.WARDEN_SONIC_BOOM,
                        net.minecraft.sounds.SoundSource.PLAYERS, 0.6f, 1.9f);
                return true;
            }
            // Four blades of light crossing the point.
            double r = f * 0.9;
            for (int i = 0; i < 4; i++) {
                double a = (Math.PI * i) / 4 + 0.4;
                Vec3 off = new Vec3(Math.cos(a) * r, 0, Math.sin(a) * r);
                Vfx.line(lvl, p.core, at.subtract(off), at.add(off), 0.28);
            }
            return true;
        });
    }

    /** Convenience: play the right thing for a killing blow on an entity. */
    public static void finisher(ServerLevel level, LivingEntity victim, Vfx.Palette p) {
        executionFlash(level, victim.position().add(0, victim.getBbHeight() * 0.55, 0), p);
    }

    private static net.minecraft.core.BlockPos bp(Vec3 v) {
        return new net.minecraft.core.BlockPos((int) Math.floor(v.x), (int) Math.floor(v.y), (int) Math.floor(v.z));
    }
}
