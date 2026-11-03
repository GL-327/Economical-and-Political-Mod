package com.political.power.game;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/** Server-spawned particles, sent only to players within 64 blocks (STYLE.md: effects reach 64 blocks, no further). */
final class PowerVfx {

    static final double RADIUS = 64.0;

    private PowerVfx() {
    }

    static void particles(ServerLevel world, ParticleOptions options, double x, double y, double z,
                          int count, double dx, double dy, double dz, double speed) {
        double limit = RADIUS * RADIUS;
        for (ServerPlayer viewer : world.players()) {
            if (viewer.distanceToSqr(x, y, z) <= limit) {
                world.sendParticles(viewer, options, false, false, x, y, z, count, dx, dy, dz, speed);
            }
        }
    }

    /** A line of particles from one point to another, for rays and pulls. */
    static void beam(ServerLevel world, ParticleOptions options, Vec3 from, Vec3 to, int steps) {
        Vec3 delta = to.subtract(from);
        for (int i = 0; i <= steps; i++) {
            Vec3 p = from.add(delta.scale(i / (double) steps));
            particles(world, options, p.x, p.y, p.z, 2, 0.04, 0.04, 0.04, 0.0);
        }
    }

    /** A flat ring of particles, for a Domain's edge. */
    static void ring(ServerLevel world, ParticleOptions options, double cx, double cy, double cz, double radius, int points) {
        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;
            particles(world, options, cx + Math.cos(angle) * radius, cy + 0.2, cz + Math.sin(angle) * radius, 1, 0.0, 0.1, 0.0, 0.0);
        }
    }
}
