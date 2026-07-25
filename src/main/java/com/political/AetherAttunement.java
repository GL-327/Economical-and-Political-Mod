package com.political;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

/**
 * The ten attunements of the Aether Arsenal.
 *
 * <p>Each one owns a signature effect that exists nowhere else in the mod. They are deliberately
 * different <i>shapes</i> rather than the same burst in different colours: a disc with spokes reads
 * nothing like reaching tendrils, which reads nothing like a growing crystal lattice. That is what
 * makes a hundred pieces of gear feel like ten distinct identities instead of one recoloured ten
 * times.</p>
 *
 * <p>Every signature is animated through the {@link Vfx} scheduler, so it plays out over frames.</p>
 */
public enum AetherAttunement {

    SOLARIS("Solaris", "sol", ChatFormatting.GOLD, "Light that does not forgive."),
    UMBRA("Umbra", "umb", ChatFormatting.DARK_GRAY, "The dark reaches back."),
    TEMPEST("Tempest", "tem", ChatFormatting.YELLOW, "The storm keeps its own counsel."),
    GLACIUS("Glacius", "gla", ChatFormatting.AQUA, "Everything stops, given cold enough."),
    VERDANT("Verdant", "ver", ChatFormatting.GREEN, "Growth is patient and total."),
    ABYSSAL("Abyssal", "aby", ChatFormatting.BLUE, "Pressure from somewhere with no floor."),
    EMBER("Ember", "emb", ChatFormatting.RED, "It was always going to burn."),
    ASTRAL("Astral", "ast", ChatFormatting.LIGHT_PURPLE, "Older light, arriving late."),
    SANGUINE("Sanguine", "san", ChatFormatting.DARK_RED, "It keeps time with your heart."),
    VOIDREND("Voidrend", "voi", ChatFormatting.DARK_PURPLE, "A seam in the world, briefly open.");

    public final String display;
    public final String prefix;
    public final ChatFormatting colour;
    public final String flavor;

    AetherAttunement(String display, String prefix, ChatFormatting colour, String flavor) {
        this.display = display;
        this.prefix = prefix;
        this.colour = colour;
        this.flavor = flavor;
    }

    /** The layered palette this attunement draws with. */
    public Vfx.Palette palette() {
        return switch (this) {
            case SOLARIS -> new Vfx.Palette(ParticleTypes.END_ROD, ParticleTypes.GLOW, ParticleTypes.WAX_ON);
            case UMBRA -> new Vfx.Palette(ParticleTypes.SCULK_SOUL, ParticleTypes.SMOKE, ParticleTypes.LARGE_SMOKE);
            case TEMPEST -> new Vfx.Palette(ParticleTypes.END_ROD, ParticleTypes.ELECTRIC_SPARK, ParticleTypes.CLOUD);
            case GLACIUS -> new Vfx.Palette(ParticleTypes.END_ROD, ParticleTypes.SNOWFLAKE, ParticleTypes.CLOUD);
            case VERDANT -> new Vfx.Palette(ParticleTypes.HAPPY_VILLAGER, ParticleTypes.COMPOSTER, ParticleTypes.SPORE_BLOSSOM_AIR);
            case ABYSSAL -> new Vfx.Palette(ParticleTypes.BUBBLE_POP, ParticleTypes.BUBBLE, ParticleTypes.SPLASH);
            case EMBER -> new Vfx.Palette(ParticleTypes.END_ROD, ParticleTypes.FLAME, ParticleTypes.LARGE_SMOKE);
            case ASTRAL -> new Vfx.Palette(ParticleTypes.END_ROD, ParticleTypes.ENCHANT, ParticleTypes.PORTAL);
            case SANGUINE -> new Vfx.Palette(ParticleTypes.DUST_PLUME, ParticleTypes.CRIMSON_SPORE, ParticleTypes.SMOKE);
            case VOIDREND -> new Vfx.Palette(ParticleTypes.END_ROD, ParticleTypes.REVERSE_PORTAL, ParticleTypes.PORTAL);
        };
    }

    private static BlockPos bp(Vec3 v) {
        return new BlockPos((int) Math.floor(v.x), (int) Math.floor(v.y), (int) Math.floor(v.z));
    }

    /**
     * The signature strike. Ten genuinely different animations, one per attunement.
     */
    public void signature(ServerLevel level, Vec3 at) {
        Vfx.Palette p = palette();
        switch (this) {

            // A solar disc that snaps open, then throws spokes outward like a sunburst.
            case SOLARIS -> Vfx.spawn(level, (lvl, f) -> {
                if (f > 14) return false;
                double r = Math.min(3.2, f * 0.45);
                Vfx.ring(lvl, p.core, at, r, 28);
                if (f % 2 == 0) {
                    for (int i = 0; i < 8; i++) {
                        double a = (Math.PI * 2 * i) / 8 + f * 0.06;
                        Vec3 dir = new Vec3(Math.cos(a), 0, Math.sin(a));
                        Vfx.line(lvl, p.sheath, at.add(dir.scale(r)), at.add(dir.scale(r + 1.6)), 0.3);
                    }
                }
                if (f == 0) lvl.playSound(null, bp(at), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.9f, 1.7f);
                return true;
            });

            // Tendrils crawling outward along the ground, then snapping back.
            case UMBRA -> Vfx.spawn(level, (lvl, f) -> {
                if (f > 18) return false;
                boolean retract = f > 10;
                double reach = retract ? (18 - f) * 0.42 : f * 0.42;
                for (int i = 0; i < 6; i++) {
                    double a = (Math.PI * 2 * i) / 6;
                    // Each tendril curves as it extends.
                    Vec3 prev = at;
                    for (double d = 0.4; d <= reach; d += 0.4) {
                        double curve = a + Math.sin(d * 0.9) * 0.35;
                        Vec3 pt = at.add(Math.cos(curve) * d, 0.12, Math.sin(curve) * d);
                        Vfx.dot(lvl, f % 3 == 0 ? p.core : p.sheath, pt);
                        prev = pt;
                    }
                }
                if (f == 0) lvl.playSound(null, bp(at), SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.PLAYERS, 0.5f, 1.2f);
                return true;
            });

            // Arcs orbiting the point, jumping between orbit anchors.
            case TEMPEST -> Vfx.spawn(level, (lvl, f) -> {
                if (f > 12) return false;
                double r = 2.4;
                int anchors = 5;
                Vec3[] pts = new Vec3[anchors];
                for (int i = 0; i < anchors; i++) {
                    double a = (Math.PI * 2 * i) / anchors + f * 0.4;
                    pts[i] = at.add(Math.cos(a) * r, 0.8 + Math.sin(a * 2 + f * 0.3) * 0.6, Math.sin(a) * r);
                }
                for (int i = 0; i < anchors; i++) {
                    Vfx.arc(lvl, p.sheath, pts[i], pts[(i + 1) % anchors], 0.5, 4);
                }
                if (f % 4 == 0) lvl.playSound(null, bp(at), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 0.35f, 1.8f);
                return true;
            });

            // A crystal lattice growing outward in stepped struts.
            case GLACIUS -> Vfx.spawn(level, (lvl, f) -> {
                if (f > 16) return false;
                double grow = Math.min(2.8, f * 0.32);
                for (int i = 0; i < 6; i++) {
                    double a = (Math.PI * 2 * i) / 6;
                    Vec3 base = at.add(Math.cos(a) * grow, 0.1, Math.sin(a) * grow);
                    // Vertical shards stepping up as the lattice forms.
                    Vfx.line(lvl, p.sheath, base, base.add(0, 0.5 + (i % 3) * 0.45, 0), 0.24);
                    if (f > 6) Vfx.line(lvl, p.core, base, at.add(0, 0.6, 0), 0.5);
                }
                if (f == 0) lvl.playSound(null, bp(at), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.7f, 1.6f);
                return true;
            });

            // A spiral of growth climbing upward and opening at the top.
            case VERDANT -> Vfx.spawn(level, (lvl, f) -> {
                if (f > 20) return false;
                double h = f * 0.16;
                for (int arm = 0; arm < 3; arm++) {
                    double a = f * 0.42 + (Math.PI * 2 * arm) / 3;
                    double rr = 0.4 + h * 0.35;
                    Vfx.dot(lvl, p.core, at.add(Math.cos(a) * rr, h, Math.sin(a) * rr));
                }
                if (f > 14) Vfx.ring(lvl, p.sheath, at.add(0, h, 0), (f - 14) * 0.4, 14);
                if (f == 0) lvl.playSound(null, bp(at), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.6f, 0.8f);
                return true;
            });

            // A column of pressure rising, then bursting outward at the surface.
            case ABYSSAL -> Vfx.spawn(level, (lvl, f) -> {
                if (f > 18) return false;
                if (f <= 12) {
                    double y = f * 0.28;
                    Vfx.ring(lvl, p.sheath, at.add(0, y, 0), 0.7 + y * 0.1, 12);
                    Vfx.puff(lvl, p.haze, at.add(0, y, 0), 4, 0.25, 0.05);
                } else {
                    double r = (f - 12) * 0.85;
                    Vfx.ring(lvl, p.core, at.add(0, 3.4, 0), r, 22);
                }
                if (f == 12) lvl.playSound(null, bp(at), SoundEvents.SPLASH_POTION_BREAK, SoundSource.PLAYERS, 0.8f, 0.7f);
                return true;
            });

            // A vortex of fire spinning up into a tight column.
            case EMBER -> Vfx.spawn(level, (lvl, f) -> {
                if (f > 18) return false;
                for (int i = 0; i < 4; i++) {
                    double t = (f * 0.55) + (Math.PI * 2 * i) / 4;
                    double y = (f * 0.13) + i * 0.12;
                    double rr = Math.max(0.15, 1.9 - f * 0.09);
                    Vfx.dot(lvl, p.sheath, at.add(Math.cos(t) * rr, y, Math.sin(t) * rr));
                }
                if (f > 12) Vfx.puff(lvl, p.core, at.add(0, 1.8, 0), 5, 0.2, 0.12);
                if (f == 0) lvl.playSound(null, bp(at), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.8f, 0.9f);
                return true;
            });

            // A sphere of stars with constellation lines drawn between them.
            case ASTRAL -> Vfx.spawn(level, (lvl, f) -> {
                if (f > 20) return false;
                double r = 2.2;
                int n = 9;
                Vec3[] stars = new Vec3[n];
                for (int i = 0; i < n; i++) {
                    double golden = Math.PI * (3.0 - Math.sqrt(5.0)) * i;
                    double y = 1 - (i / (double) (n - 1)) * 2;
                    double rad = Math.sqrt(Math.max(0, 1 - y * y));
                    stars[i] = at.add(Math.cos(golden) * rad * r, 1.0 + y * r * 0.6, Math.sin(golden) * rad * r);
                    Vfx.dot(lvl, p.core, stars[i]);
                }
                // Lines appear progressively, like a constellation being traced.
                int links = Math.min(n - 1, f / 2);
                for (int i = 0; i < links; i++) {
                    Vfx.line(lvl, p.sheath, stars[i], stars[i + 1], 0.4);
                }
                if (f == 0) lvl.playSound(null, bp(at), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.8f, 1.3f);
                return true;
            });

            // Rings pulsing outward in a heartbeat rhythm, two beats then a rest.
            case SANGUINE -> Vfx.spawn(level, (lvl, f) -> {
                if (f > 24) return false;
                int beat = f % 8;
                if (beat == 0 || beat == 2) {
                    lvl.playSound(null, bp(at), SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 0.7f, 1.1f);
                }
                if (beat <= 3) {
                    double r = 0.6 + beat * 0.7;
                    Vfx.ring(lvl, p.core, at.add(0, 0.9, 0), r, 20);
                    Vfx.ring(lvl, p.sheath, at.add(0, 0.9, 0), r * 0.7, 14);
                }
                return true;
            });

            // A vertical seam that tears open, flickers, then closes.
            case VOIDREND -> Vfx.spawn(level, (lvl, f) -> {
                if (f > 22) return false;
                double open = f <= 11 ? f / 11.0 : (22 - f) / 11.0;
                double h = 2.6 * open;
                double w = 0.55 * open;
                for (double y = -h; y <= h; y += 0.2) {
                    double taper = w * (1 - Math.abs(y) / (h + 0.01));
                    Vfx.dot(lvl, p.core, at.add(taper, 1.2 + y, 0));
                    Vfx.dot(lvl, p.core, at.add(-taper, 1.2 + y, 0));
                    if (f % 2 == 0) Vfx.dot(lvl, p.sheath, at.add(0, 1.2 + y, 0));
                }
                if (f == 0) lvl.playSound(null, bp(at), SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, 0.5f, 1.4f);
                return true;
            });
        }
    }

    /**
     * The quiet aura worn armour gives off. Each attunement moves differently so you can tell what
     * someone is wearing at a glance.
     */
    public void aura(ServerLevel level, Vec3 at, int frame) {
        Vfx.Palette p = palette();
        switch (this) {
            case SOLARIS -> Vfx.ring(level, p.sheath, at.add(0, 2.2, 0), 0.45, 6);
            case UMBRA -> Vfx.puff(level, p.haze, at.add(0, 0.15, 0), 3, 0.45, 0.01);
            case TEMPEST -> {
                double a = frame * 0.5;
                Vfx.dot(level, p.sheath, at.add(Math.cos(a) * 0.9, 1.2, Math.sin(a) * 0.9));
            }
            case GLACIUS -> Vfx.dot(level, p.sheath, at.add(
                    (Math.random() - 0.5) * 1.2, 0.4 + Math.random() * 1.4, (Math.random() - 0.5) * 1.2));
            case VERDANT -> {
                double a = frame * 0.3;
                Vfx.dot(level, p.core, at.add(Math.cos(a) * 0.7, (frame % 20) * 0.09, Math.sin(a) * 0.7));
            }
            case ABYSSAL -> Vfx.dot(level, p.sheath, at.add(
                    (Math.random() - 0.5) * 0.8, 0.3 + (frame % 16) * 0.11, (Math.random() - 0.5) * 0.8));
            case EMBER -> {
                double a = -frame * 0.45;
                Vfx.dot(level, p.sheath, at.add(Math.cos(a) * 0.6, 0.5, Math.sin(a) * 0.6));
            }
            case ASTRAL -> {
                if (frame % 3 == 0) Vfx.dot(level, p.core, at.add(
                        (Math.random() - 0.5) * 1.8, 1.0 + (Math.random() - 0.5) * 1.4, (Math.random() - 0.5) * 1.8));
            }
            case SANGUINE -> {
                if (frame % 10 < 2) Vfx.ring(level, p.core, at.add(0, 1.0, 0), 0.75, 8);
            }
            case VOIDREND -> {
                if (frame % 2 == 0) Vfx.dot(level, p.sheath, at.add(0, 1.1 + Math.sin(frame * 0.4) * 0.5, 0));
            }
        }
    }

    public static AetherAttunement byPrefix(String prefix) {
        if (prefix == null) return null;
        for (AetherAttunement a : values()) {
            if (a.prefix.equalsIgnoreCase(prefix)) return a;
        }
        return null;
    }
}
