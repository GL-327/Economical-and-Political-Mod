package com.political;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Random;

public class BountySpawnManager {

    private static final Random random = new Random();
    private static final Map<UUID, Long> lastSpawnAttempt = new HashMap<>();
    private static final long SPAWN_COOLDOWN_TICKS = 100; // 5 seconds
    private static final int SPAWN_RADIUS = 24;
    private static final int MIN_DISTANCE = 8;
    private static final double SPAWN_CHANCE = 0.3; // 30% chance per attempt

    /**
     * Call this every tick from PoliticalServer.java
     */
    public static void tick(ServerWorld world) {
        for (ServerPlayerEntity player : world.getPlayers()) {
            tickPlayer(player, world);
        }
    }

    private static void tickPlayer(ServerPlayerEntity player, ServerWorld world) {
        SlayerManager.ActiveQuest quest = SlayerManager.getActiveQuest(player);
        if (quest == null) return;

        // Don't spawn if boss is already up
        if (quest.bossSpawned) return;

        // Skip Wardens - they should not have increased spawns [1]
        if (quest.slayerType == SlayerManager.SlayerType.WARDEN) return;

        // Check cooldown
        UUID uuid = player.getUuid();
        long now = world.getTime();
        long lastAttempt = lastSpawnAttempt.getOrDefault(uuid, 0L);

        if (now - lastAttempt < SPAWN_COOLDOWN_TICKS) return;
        lastSpawnAttempt.put(uuid, now);

        // Random chance to spawn
        if (random.nextDouble() > SPAWN_CHANCE) return;

        // Try to spawn the target mob
        trySpawnBountyMob(player, world, quest.slayerType);
    }

    private static void trySpawnBountyMob(ServerPlayerEntity player, ServerWorld world, SlayerManager.SlayerType type) {
        // Find a valid spawn position
        BlockPos playerPos = player.getBlockPos();

        for (int attempt = 0; attempt < 5; attempt++) {
            int offsetX = random.nextInt(SPAWN_RADIUS * 2) - SPAWN_RADIUS;
            int offsetZ = random.nextInt(SPAWN_RADIUS * 2) - SPAWN_RADIUS;

            // Ensure minimum distance
            if (Math.abs(offsetX) < MIN_DISTANCE && Math.abs(offsetZ) < MIN_DISTANCE) {
                continue;
            }

            BlockPos spawnPos = playerPos.add(offsetX, 0, offsetZ);

            // Find ground level
            spawnPos = world.getTopPosition(net.minecraft.world.Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, spawnPos);

            // Check if valid spawn location
            if (!isValidSpawnLocation(world, spawnPos, type)) {
                continue;
            }

            // Spawn the mob
            MobEntity mob = createMobForType(world, type);
            if (mob != null) {
                mob.setPosition(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
                mob.initialize(world, world.getLocalDifficulty(spawnPos), SpawnReason.NATURAL, null);
                world.spawnEntity(mob);
                return;
            }
        }
    }

    private static boolean isValidSpawnLocation(ServerWorld world, BlockPos pos, SlayerManager.SlayerType type) {
        // Check basic spawn conditions
        if (!world.getBlockState(pos).isAir()) return false;
        if (!world.getBlockState(pos.up()).isAir()) return false;

        // Undead mobs should not spawn in daylight [1]
        if (isUndeadType(type)) {
            int skyLight = world.getLightLevel(LightType.SKY, pos);
            boolean isDaytime = world.isDay();

            // Don't spawn undead during day with sky access
            if (isDaytime && skyLight > 7) {
                return false;
            }
        }

        // Slimes need specific conditions or swamp biome
        if (type == SlayerManager.SlayerType.SLIME) {
            // Allow spawning but slimes will handle their own size
            return true;
        }

        // Endermen can spawn anywhere
        if (type == SlayerManager.SlayerType.ENDERMAN) {
            return true;
        }

        return true;
    }

    private static boolean isUndeadType(SlayerManager.SlayerType type) {
        return type == SlayerManager.SlayerType.ZOMBIE ||
                type == SlayerManager.SlayerType.SKELETON;
    }

    private static MobEntity createMobForType(ServerWorld world, SlayerManager.SlayerType type) {
        return switch (type) {
            case ZOMBIE -> new ZombieEntity(EntityType.ZOMBIE, world);
            case SPIDER -> new SpiderEntity(EntityType.SPIDER, world);
            case SKELETON -> new SkeletonEntity(EntityType.SKELETON, world);
            case SLIME -> {
                SlimeEntity slime = new SlimeEntity(EntityType.SLIME, world);
                // Random size 1-3
                yield slime;
            }
            case ENDERMAN -> new EndermanEntity(EntityType.ENDERMAN, world);
            case WARDEN -> null; // Never spawn wardens [1]
        };
    }
}