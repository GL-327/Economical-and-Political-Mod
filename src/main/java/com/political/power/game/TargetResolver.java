package com.political.power.game;

import com.political.BossAbilityManager;
import com.political.WorldBossManager;
import com.political.bounty.SlayerManager;
import com.political.power.PowerSettings;
import com.political.power.model.AbilityKind;
import com.political.power.model.AbilitySpec;
import com.political.power.model.TargetInfo;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Target resolution on the server (program 10.6): the living entity under the crosshair, found with a ray from the
 * caster's eyes; failing that the caster themselves, where the ability allows it. Nothing here trusts the
 * client. The result is plain facts for the pipeline, plus the entity itself for the effect.
 */
final class TargetResolver {

    /** The result of a lookup: what the pipeline sees and the entity the effect will act on (null for none or self). */
    record Resolved(TargetInfo info, LivingEntity entity) {
    }

    private TargetResolver() {
    }

    static Resolved resolve(ServerPlayer player, AbilitySpec spec) {
        // Look further than the range so a target that is too far away is reported as out of range, not as missing.
        double search = Math.min(64.0, Math.max(spec.range() * 2.0, spec.range() + 8.0));
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = eye.add(look.scale(search));
        AABB box = player.getBoundingBox().expandTowards(look.scale(search)).inflate(1.0);

        LivingEntity best = null;
        double bestDistance = search;
        for (Entity e : player.level().getEntities(player, box)) {
            if (!(e instanceof LivingEntity living) || e == player) {
                continue;
            }
            if (living instanceof ServerPlayer other && other.isSpectator()) {
                continue;
            }
            Optional<Vec3> hit = living.getBoundingBox().inflate(0.3).clip(eye, end);
            if (hit.isEmpty()) {
                continue;
            }
            double distance = eye.distanceTo(hit.get());
            if (distance < bestDistance) {
                bestDistance = distance;
                best = living;
            }
        }

        if (best != null) {
            boolean clear = lineOfSight(player.level(), player, eye, best);
            return new Resolved(describe(player, spec, best, bestDistance, clear), best);
        }
        if (spec.allowSelf()) {
            return new Resolved(TargetInfo.self(missing(player)), null);
        }
        return new Resolved(TargetInfo.none(), null);
    }

    private static TargetInfo describe(ServerPlayer caster, AbilitySpec spec, LivingEntity target, double distance, boolean clear) {
        boolean isPlayer = target instanceof ServerPlayer;
        TargetInfo.Kind kind = isPlayer ? TargetInfo.Kind.PLAYER : TargetInfo.Kind.ENTITY;
        if (!target.isAlive() || target.isRemoved()) {
            return TargetInfo.invalid(kind, distance, "dead");
        }
        boolean healing = spec.kind() == AbilityKind.REVERSE;
        if (!healing) {
            // The mod's NPCs (auction master, brokers, citizens) are made invulnerable when they are placed.
            if (target.isInvulnerable()) {
                return TargetInfo.invalid(kind, distance, "protected");
            }
            if (target instanceof ServerPlayer other && !caster.canHarmPlayer(other)) {
                return TargetInfo.invalid(kind, distance, "pvp off");
            }
            boolean moves = spec.kind() == AbilityKind.BLUE || spec.kind() == AbilityKind.RED;
            if (moves && isBoss(target) && !PowerSettings.BLUE_AFFECTS_BOSSES.get()) {
                return TargetInfo.invalid(kind, distance, "boss");
            }
        }
        double missing = missing(target);
        return isPlayer ? TargetInfo.player(distance, clear, missing) : TargetInfo.entity(distance, clear, missing);
    }

    static boolean isBoss(LivingEntity entity) {
        return BossAbilityManager.getActiveBosses().contains(entity.getUUID())
                || SlayerManager.isSlayerBoss(entity.getUUID())
                || WorldBossManager.isWorldBoss(entity.getUUID());
    }

    private static double missing(LivingEntity entity) {
        return Math.max(0.0, entity.getMaxHealth() - entity.getHealth());
    }

    /** Nothing solid between the caster's eyes and the middle of the target. */
    private static boolean lineOfSight(ServerLevel world, ServerPlayer caster, Vec3 eye, LivingEntity target) {
        Vec3 middle = target.getBoundingBox().getCenter();
        BlockHitResult block = world.clip(new ClipContext(eye, middle, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster));
        return block.getType() == HitResult.Type.MISS
                || block.getLocation().distanceToSqr(eye) >= middle.distanceToSqr(eye) - 1.0e-3;
    }
}
