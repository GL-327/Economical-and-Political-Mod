package com.political;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * The cinematic effects engine.
 *
 * <p>This mod is server side, so there are no shaders to lean on. Everything here is built out of
 * vanilla particles placed by hand, one frame at a time, which is what separates a real effect from
 * a puff of smoke. Effects are <b>animated</b>: they register with a scheduler and run over many
 * ticks, so a beam can charge, fire, flicker and bloom on impact instead of appearing all at once.</p>
 *
 * <p>Every set piece is layered the way a film effect is layered:</p>
 * <ul>
 *   <li>a bright, dense <b>core</b> that reads as the shape,</li>
 *   <li>a <b>sheath</b> offset around the core that gives it thickness,</li>
 *   <li>a loose <b>haze</b> that softens the silhouette,</li>
 *   <li>a <b>muzzle</b> and an <b>impact</b> accent so it has a beginning and an end,</li>
 *   <li>layered sound, usually a low body plus a bright crack.</li>
 * </ul>
 *
 * <p>Nothing here deals damage or changes a single number. It is purely how things look, so it can
 * be attached to any existing ability without touching balance.</p>
 */
public final class Vfx {
    private static final Logger LOGGER = LoggerFactory.getLogger(Vfx.class);


    private Vfx() {}

    /** One frame of an animation. Return false when the effect is finished. */
    public interface Effect {
        boolean tick(ServerLevel level, int frame);
    }

    private static final class Running {
        final ServerLevel level;
        final Effect fx;
        int frame;

        Running(ServerLevel level, Effect fx) {
            this.level = level;
            this.fx = fx;
        }
    }

    /** Hard cap so a burst of activity can never flood the particle budget. */
    private static final int MAX_ACTIVE = 48;

    private static final List<Running> ACTIVE = new ArrayList<>();

    /** Queues an animated effect. Silently ignored if the scheduler is saturated. */
    public static void spawn(ServerLevel level, Effect fx) {
        if (level == null || fx == null) return;
        synchronized (ACTIVE) {
            if (ACTIVE.size() >= MAX_ACTIVE) return;
            ACTIVE.add(new Running(level, fx));
        }
    }

    /** Advances every live effect by one frame. Called once per server tick. */
    public static void tick(MinecraftServer server) {
        synchronized (ACTIVE) {
            if (ACTIVE.isEmpty()) return;
            ACTIVE.removeIf(r -> {
                try {
                    return !r.fx.tick(r.level, r.frame++);
                } catch (Exception e) {
                    LOGGER.debug("Vfx.tick: ignored an error", e);
                    // A broken effect must never take the server tick down with it.
                    return true;
                }
            });
        }
    }

    public static void clear() {
        synchronized (ACTIVE) {
            ACTIVE.clear();
        }
    }

    // ════════════════════════════════════════════════════════════════════
    // PRIMITIVES
    // ════════════════════════════════════════════════════════════════════

    /** A single crisp particle with no drift, which is what keeps shapes readable. */
    public static void dot(ServerLevel level, ParticleOptions p, Vec3 at) {
        level.sendParticles(p, at.x, at.y, at.z, 1, 0.0, 0.0, 0.0, 0.0);
    }

    /** A soft cluster used for haze and glow. */
    public static void puff(ServerLevel level, ParticleOptions p, Vec3 at, int count, double spread, double speed) {
        level.sendParticles(p, at.x, at.y, at.z, count, spread, spread, spread, speed);
    }

    /** A straight line of particles between two points. */
    public static void line(ServerLevel level, ParticleOptions p, Vec3 from, Vec3 to, double step) {
        Vec3 delta = to.subtract(from);
        double len = delta.length();
        if (len < 1.0E-4) return;
        Vec3 dir = delta.scale(1.0 / len);
        for (double d = 0; d <= len; d += step) {
            dot(level, p, from.add(dir.scale(d)));
        }
    }

    /** A flat ring on the horizontal plane. */
    public static void ring(ServerLevel level, ParticleOptions p, Vec3 centre, double radius, int points) {
        for (int i = 0; i < points; i++) {
            double a = (Math.PI * 2 * i) / points;
            dot(level, p, centre.add(Math.cos(a) * radius, 0, Math.sin(a) * radius));
        }
    }

    /** A helix wound around the axis between two points, used to give beams motion. */
    public static void helix(ServerLevel level, ParticleOptions p, Vec3 from, Vec3 to,
                             double radius, double turns, int points, double phase) {
        Vec3 delta = to.subtract(from);
        double len = delta.length();
        if (len < 1.0E-4) return;
        Vec3 dir = delta.scale(1.0 / len);
        Vec3 up = Math.abs(dir.y) > 0.99 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        Vec3 right = dir.cross(up).normalize();
        Vec3 realUp = right.cross(dir).normalize();

        for (int i = 0; i < points; i++) {
            double t = (double) i / points;
            double a = t * turns * Math.PI * 2 + phase;
            Vec3 on = from.add(dir.scale(t * len));
            Vec3 off = right.scale(Math.cos(a) * radius).add(realUp.scale(Math.sin(a) * radius));
            dot(level, p, on.add(off));
        }
    }

    /** A hollow sphere shell, drawn with an even point distribution. */
    public static void shell(ServerLevel level, ParticleOptions p, Vec3 centre, double radius, int points) {
        double golden = Math.PI * (3.0 - Math.sqrt(5.0));
        for (int i = 0; i < points; i++) {
            double y = 1 - (i / (double) Math.max(1, points - 1)) * 2;
            double r = Math.sqrt(Math.max(0, 1 - y * y));
            double a = golden * i;
            dot(level, p, centre.add(Math.cos(a) * r * radius, y * radius, Math.sin(a) * r * radius));
        }
    }

    /** A jagged arc, the shape that reads as electricity rather than a straight line. */
    public static void arc(ServerLevel level, ParticleOptions p, Vec3 from, Vec3 to, double jitter, int segments) {
        Vec3 prev = from;
        for (int i = 1; i <= segments; i++) {
            double t = (double) i / segments;
            Vec3 straight = from.add(to.subtract(from).scale(t));
            Vec3 next = (i == segments) ? to : straight.add(
                    (level.getRandom().nextDouble() - 0.5) * jitter,
                    (level.getRandom().nextDouble() - 0.5) * jitter,
                    (level.getRandom().nextDouble() - 0.5) * jitter);
            line(level, p, prev, next, 0.22);
            prev = next;
        }
    }

    private static void sound(ServerLevel level, Vec3 at, SoundEvent s, float vol, float pitch) {
        level.playSound(null, new BlockPos((int) Math.floor(at.x), (int) Math.floor(at.y), (int) Math.floor(at.z)),
                s, SoundSource.PLAYERS, vol, pitch);
    }

    /** Raycasts from a point along a direction and returns where the beam should stop. */
    public static Vec3 raycast(ServerLevel level, Vec3 from, Vec3 dir, double maxDistance, Entity ignore) {
        Vec3 to = from.add(dir.normalize().scale(maxDistance));
        BlockHitResult hit = level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, ignore));
        return hit.getType() == HitResult.Type.MISS ? to : hit.getLocation();
    }

    // ════════════════════════════════════════════════════════════════════
    // SET PIECES
    // ════════════════════════════════════════════════════════════════════

    /**
     * The signature beam. Charges for a beat, then fires a searing lance with a white hot core,
     * a spiralling sheath, a muzzle flash and a bloom where it lands.
     */
    public static void lance(ServerLevel level, Vec3 origin, Vec3 target, Palette palette) {
        final Vec3 dir = target.subtract(origin).normalize();
        sound(level, origin, SoundEvents.BEACON_ACTIVATE, 0.7f, 1.8f);

        spawn(level, (lvl, frame) -> {
            // Stage one: energy gathers at the muzzle.
            if (frame < 6) {
                double t = frame / 6.0;
                double r = 1.4 * (1 - t) + 0.12;
                shell(lvl, palette.haze, origin, r, 14);
                ring(lvl, palette.core, origin, r * 0.6, 10);
                return true;
            }
            // Stage two: the shot itself.
            if (frame == 6) {
                sound(lvl, origin, SoundEvents.WARDEN_SONIC_BOOM, 1.0f, 1.4f);
                sound(lvl, origin, SoundEvents.LIGHTNING_BOLT_IMPACT, 0.6f, 1.9f);
                puff(lvl, palette.core, origin, 40, 0.35, 0.55);
                sound(lvl, target, SoundEvents.GENERIC_EXPLODE.value(), 0.8f, 1.5f);
            }
            // Stage three: the beam holds and flickers, then fades.
            int beamFrame = frame - 6;
            if (beamFrame <= 10) {
                double fade = 1.0 - (beamFrame / 11.0);
                // Core: dense and bright so the shape reads instantly.
                line(lvl, palette.core, origin, target, 0.16);
                // Sheath: a double helix that spins as the beam holds.
                double phase = beamFrame * 0.9;
                helix(lvl, palette.sheath, origin, target, 0.28 * fade + 0.06, 6, 48, phase);
                helix(lvl, palette.sheath, origin, target, 0.28 * fade + 0.06, 6, 48, phase + Math.PI);
                // Haze: loose and sparse, softening the edge.
                if (beamFrame % 2 == 0) {
                    helix(lvl, palette.haze, origin, target, 0.55 * fade + 0.1, 3, 26, -phase);
                }
                // Impact bloom that expands away from the surface.
                double br = 0.4 + beamFrame * 0.28;
                ring(lvl, palette.core, target.subtract(dir.scale(0.25)), br, 20);
                shell(lvl, palette.haze, target, br * 0.8, 12);
                return true;
            }
            // Stage four: embers drift off the impact point.
            if (beamFrame <= 16) {
                puff(lvl, palette.haze, target, 6, 0.5, 0.04);
                return true;
            }
            return false;
        });
    }

    /** A column of lightning from the sky with branching forks and a ground shockwave. */
    public static void stormStrike(ServerLevel level, Vec3 at) {
        final Vec3 sky = at.add(0, 26, 0);
        sound(level, at, SoundEvents.LIGHTNING_BOLT_IMPACT, 1.0f, 0.8f);

        spawn(level, (lvl, frame) -> {
            if (frame == 0) {
                arc(lvl, ParticleTypes.ELECTRIC_SPARK, sky, at, 1.5, 12);
                arc(lvl, ParticleTypes.END_ROD, sky, at, 0.5, 12);
                puff(lvl, ParticleTypes.ELECTRIC_SPARK, at, 50, 0.6, 0.5);
                return true;
            }
            if (frame <= 3) {
                // Forks peel away from the main bolt.
                for (int i = 0; i < 3; i++) {
                    Vec3 mid = at.add(0, 4 + lvl.getRandom().nextInt(14), 0);
                    Vec3 out = mid.add((lvl.getRandom().nextDouble() - 0.5) * 8, -2,
                            (lvl.getRandom().nextDouble() - 0.5) * 8);
                    arc(lvl, ParticleTypes.ELECTRIC_SPARK, mid, out, 1.0, 6);
                }
                return true;
            }
            if (frame <= 9) {
                double r = (frame - 3) * 1.5;
                ring(lvl, ParticleTypes.ELECTRIC_SPARK, at.add(0, 0.1, 0), r, 30);
                ring(lvl, ParticleTypes.CLOUD, at.add(0, 0.1, 0), r * 0.85, 22);
                return true;
            }
            return false;
        });
    }

    /** A dome that slams outward, hangs, then implodes to a point. Used for domain effects. */
    public static void domainCollapse(ServerLevel level, Vec3 centre, double radius, Palette palette) {
        sound(level, centre, SoundEvents.WARDEN_SONIC_BOOM, 1.0f, 0.6f);

        spawn(level, (lvl, frame) -> {
            if (frame <= 8) {
                double r = radius * (frame / 8.0);
                shell(lvl, palette.sheath, centre, r, 60);
                ring(lvl, palette.core, centre, r, 36);
                return true;
            }
            if (frame <= 26) {
                // The dome holds, breathing slightly so it feels alive.
                double pulse = radius + Math.sin((frame - 8) * 0.4) * 0.35;
                shell(lvl, palette.haze, centre, pulse, 44);
                ring(lvl, palette.core, centre, pulse, 30);
                if (frame % 4 == 0) sound(lvl, centre, SoundEvents.BEACON_ACTIVATE, 0.35f, 0.7f);
                return true;
            }
            if (frame <= 34) {
                double r = radius * (1.0 - (frame - 26) / 8.0);
                shell(lvl, palette.core, centre, Math.max(0.2, r), 50);
                return true;
            }
            if (frame == 35) {
                puff(lvl, palette.core, centre, 80, 0.4, 0.9);
                sound(lvl, centre, SoundEvents.GENERIC_EXPLODE.value(), 1.0f, 0.7f);
                return true;
            }
            return false;
        });
    }

    /** A meteor that falls from above with a burning trail and cracks the ground on landing. */
    public static void meteor(ServerLevel level, Vec3 target) {
        final Vec3 start = target.add(6, 30, 6);
        spawn(level, (lvl, frame) -> {
            double t = frame / 14.0;
            if (t <= 1.0) {
                Vec3 at = start.add(target.subtract(start).scale(t));
                shell(lvl, ParticleTypes.FLAME, at, 0.7, 16);
                puff(lvl, ParticleTypes.LARGE_SMOKE, at, 8, 0.4, 0.02);
                puff(lvl, ParticleTypes.SOUL_FIRE_FLAME, at, 5, 0.3, 0.02);
                if (frame % 3 == 0) sound(lvl, at, SoundEvents.BLAZE_SHOOT, 0.7f, 0.6f);
                return true;
            }
            int after = frame - 14;
            if (after == 0) {
                sound(lvl, target, SoundEvents.GENERIC_EXPLODE.value(), 1.0f, 0.6f);
                puff(lvl, ParticleTypes.FLAME, target, 70, 0.8, 0.4);
                return true;
            }
            if (after <= 7) {
                double r = after * 1.6;
                ring(lvl, ParticleTypes.FLAME, target.add(0, 0.15, 0), r, 28);
                ring(lvl, ParticleTypes.LARGE_SMOKE, target.add(0, 0.15, 0), r * 0.8, 20);
                return true;
            }
            return false;
        });
    }

    /** A tear in space that widens, churns, then snaps shut. */
    public static void voidRift(ServerLevel level, Vec3 centre) {
        sound(level, centre, SoundEvents.PORTAL_TRAVEL, 0.6f, 0.5f);
        spawn(level, (lvl, frame) -> {
            if (frame <= 24) {
                double h = Math.min(3.0, frame * 0.28);
                double w = Math.min(0.9, frame * 0.09);
                for (double y = -h; y <= h; y += 0.22) {
                    double taper = w * (1 - Math.abs(y) / (h + 0.01));
                    dot(lvl, ParticleTypes.REVERSE_PORTAL, centre.add(taper, y, 0));
                    dot(lvl, ParticleTypes.REVERSE_PORTAL, centre.add(-taper, y, 0));
                }
                puff(lvl, ParticleTypes.PORTAL, centre, 10, 0.5, 0.3);
                if (frame % 6 == 0) sound(lvl, centre, SoundEvents.ENDERMAN_TELEPORT, 0.35f, 0.5f);
                return true;
            }
            if (frame <= 30) {
                puff(lvl, ParticleTypes.SCULK_SOUL, centre, 14, 0.35, 0.12);
                return true;
            }
            return false;
        });
    }

    /** A gentle upward bloom used for healing and restoration moments. */
    public static void bloom(ServerLevel level, Vec3 centre) {
        spawn(level, (lvl, frame) -> {
            if (frame > 14) return false;
            double r = 0.4 + frame * 0.14;
            ring(lvl, ParticleTypes.HEART, centre.add(0, frame * 0.12, 0), r, 10);
            ring(lvl, ParticleTypes.END_ROD, centre.add(0, frame * 0.1, 0), r * 1.2, 14);
            if (frame == 0) sound(lvl, centre, SoundEvents.AMETHYST_BLOCK_CHIME, 0.7f, 1.4f);
            return true;
        });
    }

    // ════════════════════════════════════════════════════════════════════
    // PALETTES
    // ════════════════════════════════════════════════════════════════════

    /** The three particle layers that give a set piece its identity. */
    public static final class Palette {
        public final ParticleOptions core;
        public final ParticleOptions sheath;
        public final ParticleOptions haze;

        public Palette(ParticleOptions core, ParticleOptions sheath, ParticleOptions haze) {
            this.core = core;
            this.sheath = sheath;
            this.haze = haze;
        }
    }

    public static final Palette ARCANE = new Palette(
            ParticleTypes.END_ROD, ParticleTypes.ENCHANT, ParticleTypes.PORTAL);
    public static final Palette INFERNO = new Palette(
            ParticleTypes.END_ROD, ParticleTypes.FLAME, ParticleTypes.LARGE_SMOKE);
    public static final Palette VOID = new Palette(
            ParticleTypes.END_ROD, ParticleTypes.REVERSE_PORTAL, ParticleTypes.SCULK_SOUL);
    public static final Palette STORM = new Palette(
            ParticleTypes.END_ROD, ParticleTypes.ELECTRIC_SPARK, ParticleTypes.CLOUD);
    public static final Palette CURSED = new Palette(
            ParticleTypes.SOUL_FIRE_FLAME, ParticleTypes.SCULK_SOUL, ParticleTypes.SMOKE);
    public static final Palette FROST = new Palette(
            ParticleTypes.END_ROD, ParticleTypes.SNOWFLAKE, ParticleTypes.CLOUD);

    /** Named set pieces, for previewing and for simple one call triggering. */
    public enum Kind {
        LANCE_ARCANE, LANCE_INFERNO, LANCE_VOID, LANCE_CURSED,
        STORM_STRIKE, DOMAIN_COLLAPSE, METEOR, VOID_RIFT, BLOOM
    }

    /** Plays a named set piece aimed away from the given look direction where relevant. */
    public static void play(ServerLevel level, Vec3 origin, Vec3 look, Kind kind, Entity source) {
        switch (kind) {
            case LANCE_ARCANE -> lance(level, origin, raycast(level, origin, look, 40, source), ARCANE);
            case LANCE_INFERNO -> lance(level, origin, raycast(level, origin, look, 40, source), INFERNO);
            case LANCE_VOID -> lance(level, origin, raycast(level, origin, look, 40, source), VOID);
            case LANCE_CURSED -> lance(level, origin, raycast(level, origin, look, 40, source), CURSED);
            case STORM_STRIKE -> stormStrike(level, origin);
            case DOMAIN_COLLAPSE -> domainCollapse(level, origin, 7.0, VOID);
            case METEOR -> meteor(level, origin);
            case VOID_RIFT -> voidRift(level, origin);
            case BLOOM -> bloom(level, origin);
        }
    }
}
