package com.political.items;

import com.political.items.DragonChestplates;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * One distinct combat identity per original dragon chestplate.
 * These are not copy-paste potion ticks.
 */
public final class DragonPlateHandler {

    private static final Map<UUID, Long> VOID_BLINK_UNTIL = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> STORM_CHAIN_UNTIL = new ConcurrentHashMap<>();

    private DragonPlateHandler() {}

    public static void tick(ServerPlayer player) {
        String id = plateId(player);
        if (id == null) return;
        switch (id) {
            case "inferno_dragon_chestplate" -> tickInferno(player);
            case "storm_dragon_chestplate" -> tickStorm(player);
            case "void_dragon_chestplate" -> tickVoid(player);
            case "aurora_dragon_chestplate" -> tickAurora(player);
            case "frost_dragon_chestplate" -> tickFrost(player);
            case "venom_dragon_chestplate" -> tickVenom(player);
            case "blood_dragon_chestplate" -> tickBlood(player);
            case "eclipse_dragon_chestplate" -> tickEclipse(player);
            default -> {
            }
        }
    }

    public static void onWearerHit(ServerPlayer player, LivingEntity attacker, float amount) {
        String id = plateId(player);
        if (id == null || attacker == null) return;
        ServerLevel world = player.level();
        switch (id) {
            case "inferno_dragon_chestplate" -> {
                attacker.igniteForSeconds(3);
                attacker.hurtServer(world, player.damageSources().thorns(player), Math.min(4f, amount * 0.18f));
            }
            case "venom_dragon_chestplate" -> {
                attacker.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 0));
                attacker.hurtServer(world, player.damageSources().thorns(player), 2f);
            }
            case "frost_dragon_chestplate" -> attacker.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1));
            case "void_dragon_chestplate" -> tryVoidBlink(player);
            default -> {
            }
        }
    }

    public static void onWearerAttack(ServerPlayer player, LivingEntity target) {
        String id = plateId(player);
        if (id == null || target == null) return;
        ServerLevel world = player.level();
        switch (id) {
            case "storm_dragon_chestplate" -> chainLightning(player, world, target);
            case "blood_dragon_chestplate" -> player.heal(1.5f);
            case "eclipse_dragon_chestplate" -> {
                if (world.isBrightOutside()) {
                    target.hurtServer(world, player.damageSources().magic(), 2.5f);
                }
            }
            default -> {
            }
        }
    }

    private static void tickInferno(ServerPlayer player) {
        if (player.tickCount % 20 != 0) return;
        ServerLevel world = player.level();
        world.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 1, player.getZ(), 2, 0.3, 0.2, 0.3, 0.01);
        if (player.isOnFire()) {
            player.heal(0.5f);
        }
    }

    private static void tickStorm(ServerPlayer player) {
        if (player.tickCount % 40 != 0) return;
        ServerLevel world = player.level();
        world.sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 1.2, player.getZ(), 4, 0.4, 0.3, 0.4, 0.02);
    }

    private static void tickVoid(ServerPlayer player) {
        if (player.getHealth() <= player.getMaxHealth() * 0.25f) {
            tryVoidBlink(player);
        }
    }

    private static void tickAurora(ServerPlayer player) {
        ServerLevel world = player.level();
        boolean night = !world.isBrightOutside();
        if (night && player.tickCount % 40 == 0 && player.getHealth() < player.getMaxHealth()) {
            player.heal(1.0f);
            world.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.4, player.getZ(), 4, 0.3, 0.4, 0.3, 0.01);
        }
    }

    private static void tickFrost(ServerPlayer player) {
        if (player.tickCount % 30 != 0) return;
        ServerLevel world = player.level();
        AABB box = player.getBoundingBox().inflate(4.0);
        for (Entity e : world.getEntities(player, box)) {
            if (e instanceof Monster monster) {
                monster.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 0, true, false, false));
            }
        }
        world.sendParticles(ParticleTypes.SNOWFLAKE, player.getX(), player.getY() + 0.4, player.getZ(), 6, 1.5, 0.2, 1.5, 0.0);
    }

    private static void tickVenom(ServerPlayer player) {
        if (player.tickCount % 80 == 0 && player.hasEffect(MobEffects.POISON)) {
            player.removeEffect(MobEffects.POISON);
        }
    }

    private static void tickBlood(ServerPlayer player) {
        if (player.tickCount % 60 == 0 && player.getHealth() < player.getMaxHealth() * 0.4f) {
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 50, 0, true, false, false));
        }
    }

    private static void tickEclipse(ServerPlayer player) {
        ServerLevel world = player.level();
        boolean night = !world.isBrightOutside();
        if (night && player.tickCount % 80 == 0) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 240, 0, true, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 90, 0, true, false, false));
        }
    }

    private static void tryVoidBlink(ServerPlayer player) {
        long now = System.currentTimeMillis();
        Long until = VOID_BLINK_UNTIL.get(player.getUUID());
        if (until != null && now < until) return;
        VOID_BLINK_UNTIL.put(player.getUUID(), now + 8000L);
        Vec3 look = player.getLookAngle().normalize().scale(-6);
        player.teleportTo(player.getX() + look.x, player.getY(), player.getZ() + look.z);
        player.fallDistance = 0;
        player.level().sendParticles(ParticleTypes.REVERSE_PORTAL, player.getX(), player.getY() + 1, player.getZ(),
                20, 0.4, 0.6, 0.4, 0.02);
    }

    private static void chainLightning(ServerPlayer player, ServerLevel world, LivingEntity first) {
        long now = System.currentTimeMillis();
        Long until = STORM_CHAIN_UNTIL.get(player.getUUID());
        if (until != null && now < until) return;
        STORM_CHAIN_UNTIL.put(player.getUUID(), now + 2500L);
        first.hurtServer(world, player.damageSources().magic(), 3f);
        int hops = 0;
        for (Entity e : world.getEntities(first, first.getBoundingBox().inflate(5))) {
            if (hops >= 2) break;
            if (e instanceof LivingEntity le && e != player && e != first) {
                le.hurtServer(world, player.damageSources().lightningBolt(), 2.5f);
                world.sendParticles(ParticleTypes.ELECTRIC_SPARK, le.getX(), le.getEyeY(), le.getZ(), 10, 0.2, 0.3, 0.2, 0.04);
                hops++;
            }
        }
    }

    private static String plateId(ServerPlayer player) {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        return DragonChestplates.getCustomItemId(chest);
    }
}
