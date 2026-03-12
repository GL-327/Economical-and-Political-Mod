package com.political.backrooms;

import com.political.CustomMobManager;
import com.political.CustomMobRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;

import java.util.List;

public class BackroomsMobs {

    public static void registerAll() {

        // 1. Aranea Membri - Ultra fast spider-like creature
        CustomMobRegistry.register("aranea_membri", CustomMobManager.builder("Aranea Membri")
            .baseEntity(EntityType.SPIDER)
            .health(100.0)
            .damage(45.0)
            .scale(2.0f)
            .displayName(Text.literal("☠ Aranea Membri").formatted(Formatting.BLACK, Formatting.BOLD))
            .onSpawn(mob -> {
                if (mob.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED) != null) {
                    mob.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED).setBaseValue(1.5);
                }
                if (mob.getAttributeInstance(EntityAttributes.FOLLOW_RANGE) != null) {
                    mob.getAttributeInstance(EntityAttributes.FOLLOW_RANGE).setBaseValue(32.0);
                }
                mob.setCustomNameVisible(true);
            })
            .onTick((mob, world) -> {
                if (mob.getTarget() == null) {
                    Box detectBox = new Box(mob.getX() - 32, mob.getY() - 10, mob.getZ() - 32,
                        mob.getX() + 32, mob.getY() + 10, mob.getZ() + 32);
                    List<ServerPlayerEntity> players = world.getEntitiesByClass(
                        ServerPlayerEntity.class, detectBox, p -> true);
                    if (!players.isEmpty()) {
                        mob.setTarget(players.get(0));
                    }
                }
            })
            .build());

        // 2. Smiler - Floating grin, only exists in darkness
        CustomMobRegistry.register("smiler", CustomMobManager.builder("Smiler")
            .baseEntity(EntityType.PHANTOM)
            .health(1.0)
            .damage(0.0)
            .scale(0.3f)
            .displayName(Text.literal("·͜·").formatted(Formatting.WHITE))
            .onSpawn(mob -> {
                mob.setCustomNameVisible(false);
                mob.setInvisible(true);
            })
            .onTick((mob, world) -> {
                int lightLevel = world.getLightLevel(mob.getBlockPos());
                if (lightLevel >= 7) {
                    mob.discard();
                    return;
                }
                Box aoeBox = new Box(mob.getX() - 4, mob.getY() - 4, mob.getZ() - 4,
                    mob.getX() + 4, mob.getY() + 4, mob.getZ() + 4);
                world.getEntitiesByClass(ServerPlayerEntity.class, aoeBox, p ->
                    world.getLightLevel(p.getBlockPos()) <= 2
                ).forEach(player -> {
                    if (world.getTime() % 20 == 0) {
                        player.damage(world, world.getDamageSources().mobAttack(mob), 8.0f);
                    }
                });
            })
            .build());

        // 3. Partygoer - Blind but hears everything
        CustomMobRegistry.register("partygoer", CustomMobManager.builder("Partygoer")
            .baseEntity(EntityType.ZOMBIE)
            .health(150.0)
            .damage(25.0)
            .scale(1.4f)
            .displayName(Text.literal("☻ Partygoer").formatted(Formatting.YELLOW))
            .onSpawn(mob -> {
                if (mob.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED) != null) {
                    mob.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED).setBaseValue(0.8);
                }
                if (mob.getAttributeInstance(EntityAttributes.FOLLOW_RANGE) != null) {
                    mob.getAttributeInstance(EntityAttributes.FOLLOW_RANGE).setBaseValue(1.0);
                }
            })
            .onTick((mob, world) -> {
                if (mob.getTarget() == null) {
                    Box wideBox = new Box(mob.getX() - 40, mob.getY() - 5, mob.getZ() - 40,
                        mob.getX() + 40, mob.getY() + 5, mob.getZ() + 40);
                    List<ServerPlayerEntity> nearby = world.getEntitiesByClass(
                        ServerPlayerEntity.class, wideBox, p -> {
                            if (p.isSneaking()) return false;
                            double dist = p.squaredDistanceTo(mob);
                            double speed = p.getVelocity().horizontalLength();
                            double threshold = (speed > 0.15 || !p.isOnGround()) ? 40.0 : 20.0;
                            return dist <= threshold * threshold;
                        });
                    if (!nearby.isEmpty()) {
                        mob.setTarget(nearby.get(0));
                    }
                }
            })
            .build());

        // 4. Skinstealer - Mimics players
        CustomMobRegistry.register("skinstealer", CustomMobManager.builder("Skinstealer")
            .baseEntity(EntityType.ZOMBIE)
            .health(200.0)
            .damage(30.0)
            .scale(1.0f)
            .displayName(Text.literal("").formatted(Formatting.DARK_GRAY))
            .onSpawn(mob -> {
                if (mob.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED) != null) {
                    mob.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED).setBaseValue(0.6);
                }
                if (mob.getAttributeInstance(EntityAttributes.FOLLOW_RANGE) != null) {
                    mob.getAttributeInstance(EntityAttributes.FOLLOW_RANGE).setBaseValue(48.0);
                }
            })
            .onTick((mob, world) -> {
                if (mob.getTarget() == null) {
                    Box detectBox = new Box(mob.getX() - 48, mob.getY() - 8, mob.getZ() - 48,
                        mob.getX() + 48, mob.getY() + 8, mob.getZ() + 48);
                    world.getEntitiesByClass(ServerPlayerEntity.class, detectBox, p -> true)
                        .stream().findFirst().ifPresent(mob::setTarget);
                }
            })
            .build());
    }
}
