package com.political;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.phys.AABB;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Keeps a bounty Warden above ground.
 *
 * <p>A Warden digs itself back into the floor as soon as it loses interest, which is why the boss
 * had been an iron golem wearing the name. The previous code knew that, said it was spawning a real
 * Warden, spawned a golem, and then checked whether the golem was a Warden. It never was, so the
 * handling underneath had never run once.</p>
 *
 * <p>The actual fix is to keep it interested. A Warden burrows when its anger runs down and it has
 * nothing to be angry at, so this tops the anger up and hands it a target on a slow tick. It also
 * pulls it straight back out if it manages to start digging anyway, which covers the moment between
 * one player dying and the next one arriving.</p>
 */
public final class WardenKeeper {

    private WardenKeeper() {}

    /** Which Wardens are ours. A wild one is left entirely alone. */
    private static final Set<UUID> HELD = new HashSet<>();

    /** Marks a Warden as a bounty boss and gives it its first grudge. */
    public static void hold(Warden warden, ServerPlayer against) {
        HELD.add(warden.getUUID());
        warden.setPersistenceRequired();
        anger(warden, against);
    }

    public static void release(UUID id) {
        HELD.remove(id);
    }

    public static boolean isHeld(Warden warden) {
        return HELD.contains(warden.getUUID());
    }

    /**
     * Tops up anger and undoes any burrow that started.
     *
     * <p>Twice a second is plenty. The dig is an animation with a wind up, so catching it inside
     * half a second means it never finishes, and checking more often would cost more for nothing.</p>
     */
    public static void tick(MinecraftServer server) {
        if (HELD.isEmpty()) return;
        if (server.getTickCount() % 10 != 0) return;

        for (ServerLevel level : server.getAllLevels()) {
            if (level.players().isEmpty()) continue;
            for (Warden warden : level.getEntitiesOfClass(Warden.class,
                    new AABB(-30000000, -2048, -30000000, 30000000, 2048, 30000000),
                    w -> w.isAlive() && HELD.contains(w.getUUID()))) {

                ServerPlayer nearest = null;
                double best = Double.MAX_VALUE;
                for (ServerPlayer p : level.players()) {
                    double d = p.distanceToSqr(warden);
                    if (d < best) {
                        best = d;
                        nearest = p;
                    }
                }

                // Out of the ground first, then given a reason to stay out.
                if (warden.hasPose(Pose.DIGGING) || warden.hasPose(Pose.EMERGING)) {
                    warden.setPose(Pose.STANDING);
                }
                if (nearest != null && best < 64 * 64) {
                    anger(warden, nearest);
                }
            }
        }
    }

    /**
     * Puts a Warden's anger back at the top.
     *
     * <p>Reflective because the anger API has moved around between versions and a boss that will
     * not spawn is a worse outcome than a boss that occasionally loses interest. If none of the
     * approaches work the pose reset above still keeps it above ground.</p>
     */
    private static void anger(Warden warden, ServerPlayer at) {
        try {
            warden.setAttackTarget(at);
        } catch (Throwable ignored) {
            // Not present under these mappings. The anger call below is the important one.
        }
        try {
            var m = Warden.class.getMethod("increaseAngerAt", Entity.class);
            m.invoke(warden, at);
        } catch (Throwable ignored) {
            try {
                var m = Warden.class.getMethod("increaseAngerAt", Entity.class, int.class, boolean.class);
                m.invoke(warden, at, 150, true);
            } catch (Throwable ignored2) {
                // Neither shape is available. The pose reset is doing the work by itself.
            }
        }
        try {
            warden.setTarget(at);
        } catch (Throwable ignored) {
            // Same again.
        }
    }

    /** Drops a dead or removed Warden from the set, so it cannot grow forever. */
    public static void prune(MinecraftServer server) {
        if (HELD.isEmpty()) return;
        if (server.getTickCount() % 200 != 0) return;
        HELD.removeIf(id -> {
            for (ServerLevel level : server.getAllLevels()) {
                Entity e = level.getEntity(id);
                if (e != null && e.isAlive()) return false;
            }
            return true;
        });
    }
}
