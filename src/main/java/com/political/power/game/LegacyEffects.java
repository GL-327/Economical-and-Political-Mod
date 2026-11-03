package com.political.power.game;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import com.political.technique.Technique;
import java.util.ArrayList;
import java.util.List;

/**
 * The effects of the 32 abilities of the eight older techniques, moved here unchanged from
 * {@code TechniqueManager} in M2.7. What changed around them: the cast is validated and paid for by the shared
 * pipeline, so an ability that needs a target no longer spends resource when nobody is in front of the caster;
 * damage is scaled by rank as before and now also by the school tree and the no-sprint vow; Ironmarrow's
 * Unbreakable raises the shared Barrier for the same 2.5 seconds.
 */
final class LegacyEffects {

    private LegacyEffects() {
    }

    /** Runs the effect of one of the 32 older abilities. The caller has already validated and paid for the cast. */
    static void run(ServerPlayer player, ServerLevel world, Technique technique, Technique.Ability ability) {
        switch (technique) {
            case EMBERHEART -> emberheart(player, world, ability.id());
            case GLACIER_SOUL -> glacier(player, world, ability.id());
            case STORM_VEIN -> storm(player, world, ability.id());
            case UMBRAL_STEP -> umbral(player, world, ability.id());
            case WILDGROWTH -> wildgrowth(player, world, ability.id());
            case IRONMARROW -> ironmarrow(player, world, ability.id());
            case TIDEBOUND -> tidebound(player, world, ability.id());
            case WHISPERWIND -> whisperwind(player, world, ability.id());
        }
    }

    private static void emberheart(ServerPlayer player, ServerLevel world, String id) {
        switch (id) {
            case "ember_lash" -> {
                LivingEntity target = rayTarget(player, 20);
                if (target == null) return;
                bolt(world, player, target, ParticleTypes.FLAME);
                hurt(player, world, target, 8f);
                target.igniteForSeconds(4);
            }
            case "cinder_bloom" -> {
                nova(player, world, 5.5, 7f, true);
                world.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 1, player.getZ(), 40, 2, 0.4, 2, 0.02);
            }
            case "pyre_dash" -> {
                dash(player, 8);
                player.igniteForSeconds(1);
                nova(player, world, 2.5, 4f, true);
            }
            case "hearthguard" -> {
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 200, 0, true, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 200, 0, true, false, false));
            }
        }
        world.playSound(null, player.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.8f, 1.1f);
    }

    private static void glacier(ServerPlayer player, ServerLevel world, String id) {
        switch (id) {
            case "frost_lance" -> {
                LivingEntity target = rayTarget(player, 20);
                if (target == null) return;
                bolt(world, player, target, ParticleTypes.SNOWFLAKE);
                hurt(player, world, target, 7.5f);
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 1));
                target.setTicksFrozen(target.getTicksRequiredToFreeze() + 80);
            }
            case "rime_field" -> {
                for (LivingEntity le : nearby(player, 6)) {
                    hurt(player, world, le, 5f);
                    le.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 1));
                }
                world.sendParticles(ParticleTypes.SNOWFLAKE, player.getX(), player.getY() + 0.4, player.getZ(), 50, 3, 0.2, 3, 0.01);
            }
            case "glacial_aegis" -> {
                player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 120, 1, true, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 120, 0, true, false, false));
            }
            case "permafrost" -> {
                for (LivingEntity le : nearby(player, 7)) {
                    le.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 4));
                    le.setTicksFrozen(le.getTicksRequiredToFreeze() + 160);
                    hurt(player, world, le, 4f);
                }
            }
        }
        world.playSound(null, player.blockPosition(), SoundEvents.PLAYER_HURT_FREEZE, SoundSource.PLAYERS, 0.7f, 0.8f);
    }

    private static void storm(ServerPlayer player, ServerLevel world, String id) {
        switch (id) {
            case "volt_strike" -> {
                LivingEntity target = rayTarget(player, 22);
                if (target == null) return;
                bolt(world, player, target, ParticleTypes.ELECTRIC_SPARK);
                hurt(player, world, target, 9f);
            }
            case "arc_chain" -> {
                LivingEntity first = rayTarget(player, 16);
                List<LivingEntity> chain = new ArrayList<>();
                if (first != null) chain.add(first);
                chain.addAll(nearby(player, 8));
                int n = 0;
                for (LivingEntity le : chain) {
                    if (n++ >= 4) break;
                    hurt(player, world, le, 6.5f);
                    world.sendParticles(ParticleTypes.ELECTRIC_SPARK, le.getX(), le.getEyeY(), le.getZ(), 16, 0.3, 0.4, 0.3, 0.05);
                }
            }
            case "thunder_step" -> dash(player, 10);
            case "overload" -> {
                player.addEffect(new MobEffectInstance(MobEffects.SPEED, 160, 1, true, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 160, 1, true, false, false));
            }
        }
        world.playSound(null, player.blockPosition(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 0.35f, 1.6f);
    }

    private static void umbral(ServerPlayer player, ServerLevel world, String id) {
        switch (id) {
            case "shade_cut" -> {
                LivingEntity target = rayTarget(player, 16);
                if (target == null) return;
                hurt(player, world, target, 8.5f);
            }
            case "void_slip" -> dash(player, 12);
            case "night_shroud" -> {
                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 140, 0, true, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.SPEED, 140, 0, true, false, false));
            }
            case "umbral_rend" -> {
                LivingEntity target = rayTarget(player, 12);
                if (target == null) return;
                hurt(player, world, target, 10f);
                player.heal(4f);
            }
        }
        world.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.6f, 1.3f);
    }

    private static void wildgrowth(ServerPlayer player, ServerLevel world, String id) {
        switch (id) {
            case "thorn_lash" -> {
                LivingEntity target = rayTarget(player, 16);
                if (target == null) return;
                hurt(player, world, target, 7f);
                target.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0));
            }
            case "bloom_ward" -> {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 120, 1, true, false, false));
                for (LivingEntity le : nearbyAllies(player, 6)) {
                    le.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 80, 0, true, false, false));
                }
            }
            case "entangle" -> {
                for (LivingEntity le : nearby(player, 6)) {
                    le.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 3));
                    hurt(player, world, le, 4f);
                }
            }
            case "verdant_form" -> {
                player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 200, 0, true, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST, 200, 0, true, false, false));
                player.heal(4f);
            }
        }
        world.playSound(null, player.blockPosition(), SoundEvents.AZALEA_PLACE, SoundSource.PLAYERS, 0.8f, 1.0f);
    }

    private static void ironmarrow(ServerPlayer player, ServerLevel world, String id) {
        switch (id) {
            case "iron_smash" -> {
                LivingEntity target = rayTarget(player, 8);
                if (target == null) return;
                hurt(player, world, target, 10f);
                knock(player, target, 1.2, 0.35);
            }
            case "bulwark" -> {
                player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 160, 1, true, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 0, true, false, false));
            }
            case "quake_stomp" -> {
                for (LivingEntity le : nearby(player, 5.5)) {
                    hurt(player, world, le, 7f);
                    knock(player, le, 1.4, 0.55);
                }
            }
            case "unbreakable" -> PowerService.raiseBarrier(player, 50L); // 2.5 seconds, as before
        }
        world.playSound(null, player.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.4f, 0.8f);
    }

    private static void tidebound(ServerPlayer player, ServerLevel world, String id) {
        switch (id) {
            case "tide_lance" -> {
                LivingEntity target = rayTarget(player, 18);
                if (target == null) return;
                bolt(world, player, target, ParticleTypes.BUBBLE);
                hurt(player, world, target, 7.5f);
            }
            case "undertow" -> {
                for (LivingEntity le : nearby(player, 8)) {
                    Vec3 pull = player.position().subtract(le.position()).normalize().scale(0.9);
                    le.setDeltaMovement(le.getDeltaMovement().add(pull.x, 0.15, pull.z));
                    le.syncVelocity = true;
                    hurt(player, world, le, 4f);
                }
            }
            case "riptide" -> {
                for (LivingEntity le : nearby(player, 7)) {
                    hurt(player, world, le, 6f);
                    knock(player, le, 1.6, 0.4);
                }
            }
            case "deep_current" -> {
                player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 300, 0, true, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 300, 0, true, false, false));
            }
        }
        world.playSound(null, player.blockPosition(), SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 0.7f, 1.0f);
    }

    private static void whisperwind(ServerPlayer player, ServerLevel world, String id) {
        switch (id) {
            case "gale_cut" -> {
                LivingEntity target = rayTarget(player, 16);
                if (target == null) return;
                bolt(world, player, target, ParticleTypes.CLOUD);
                hurt(player, world, target, 6.5f);
            }
            case "cyclone" -> {
                for (LivingEntity le : nearby(player, 6.5)) {
                    hurt(player, world, le, 5f);
                    knock(player, le, 0.4, 0.95);
                }
            }
            case "zephyr_dash" -> dash(player, 11);
            case "tempest_form" -> {
                player.addEffect(new MobEffectInstance(MobEffects.SPEED, 180, 1, true, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 180, 1, true, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 180, 0, true, false, false));
            }
        }
        world.playSound(null, player.blockPosition(), SoundEvents.ELYTRA_FLYING, SoundSource.PLAYERS, 0.4f, 1.6f);
    }

    private static void hurt(ServerPlayer player, ServerLevel world, LivingEntity target, float amount) {
        String uuid = player.getUUID().toString();
        String equipped = PowerService.state(uuid).equipped;
        float scaled = amount * (1f + PowerService.equippedRank(uuid).ordinal() * 0.08f)
                * (float) PowerService.techniqueDamageMultiplier(uuid, equipped == null ? "" : equipped);
        target.hurtServer(world, player.damageSources().magic(), scaled);
    }

    private static void knock(ServerPlayer player, LivingEntity target, double xz, double y) {
        Vec3 dir = target.position().subtract(player.position());
        if (dir.lengthSqr() < 0.0001) dir = player.getLookAngle();
        Vec3 n = dir.normalize();
        target.setDeltaMovement(target.getDeltaMovement().add(n.x * xz, y, n.z * xz));
        target.syncVelocity = true;
    }

    private static void dash(ServerPlayer player, double blocks) {
        Vec3 look = player.getLookAngle().normalize().scale(blocks * 0.28);
        player.setDeltaMovement(look.x, Math.max(0.15, look.y), look.z);
        player.syncVelocity = true;
        player.fallDistance = 0;
    }

    private static void nova(ServerPlayer player, ServerLevel world, double radius, float damage, boolean ignite) {
        for (LivingEntity le : nearby(player, radius)) {
            hurt(player, world, le, damage);
            if (ignite) le.igniteForSeconds(3);
        }
    }

    private static void bolt(ServerLevel world, ServerPlayer player, LivingEntity target, net.minecraft.core.particles.ParticleOptions particle) {
        Vec3 start = player.getEyePosition();
        Vec3 end = target.getEyePosition();
        Vec3 delta = end.subtract(start);
        int steps = 12;
        for (int i = 0; i <= steps; i++) {
            Vec3 p = start.add(delta.scale(i / (double) steps));
            world.sendParticles(particle, p.x, p.y, p.z, 2, 0.05, 0.05, 0.05, 0.0);
        }
    }

    private static List<LivingEntity> nearby(ServerPlayer player, double radius) {
        ServerLevel world = player.level();
        AABB box = player.getBoundingBox().inflate(radius);
        List<LivingEntity> out = new ArrayList<>();
        for (Entity e : world.getEntities(player, box)) {
            if (e instanceof LivingEntity le && e != player && !(e instanceof ServerPlayer)) {
                out.add(le);
            }
        }
        return out;
    }

    private static List<LivingEntity> nearbyAllies(ServerPlayer player, double radius) {
        ServerLevel world = player.level();
        AABB box = player.getBoundingBox().inflate(radius);
        List<LivingEntity> out = new ArrayList<>();
        for (Entity e : world.getEntities(player, box)) {
            if (e instanceof ServerPlayer other && other != player) out.add(other);
        }
        return out;
    }

    private static LivingEntity rayTarget(ServerPlayer player, double range) {
        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = start.add(look.scale(range));
        AABB box = player.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0);
        LivingEntity best = null;
        double bestDist = range;
        for (Entity e : player.level().getEntities(player, box)) {
            if (!(e instanceof LivingEntity le) || e == player) continue;
            AABB hit = le.getBoundingBox().inflate(0.3);
            var clip = hit.clip(start, end);
            if (clip.isEmpty()) continue;
            double dist = start.distanceTo(clip.get());
            if (dist < bestDist) {
                bestDist = dist;
                best = le;
            }
        }
        return best;
    }
}
