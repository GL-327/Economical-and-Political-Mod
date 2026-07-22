package com.political;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Random;

public class BountySpawnManager {

    private static final Random random = new Random();
    private static final Map<UUID, Long> lastSpawnAttempt = new HashMap<>();
    private static long SPAWN_COOLDOWN_TICKS = 100; // 5 seconds
    private static int SPAWN_RADIUS = 24;
    private static final int MIN_DISTANCE = 8;
    private static double SPAWN_CHANCE = 0.3; // 30% chance per attempt

    /**
     * Call this every tick from PoliticalServer.java
     */
    public static void tick(ServerLevel world) {
        for (ServerPlayer player : world.players()) {
            tickPlayer(player, world);
        }
    }

    private static void tickPlayer(ServerPlayer player, ServerLevel world) {
        SlayerManager.ActiveQuest quest = SlayerManager.getActiveQuest(player);
        if (quest == null) return;

        // Don't spawn if boss is already up
        if (quest.bossSpawned) return;

        // Skip Wardens - they should not have increased spawns [1]
        if (quest.slayerType == SlayerManager.SlayerType.IRON_GOLEM) return;

        // Check cooldown
        UUID uuid = player.getUUID();
        long now = world.getServer().getTickCount();
        long lastAttempt = lastSpawnAttempt.getOrDefault(uuid, 0L);

        if (now - lastAttempt < SPAWN_COOLDOWN_TICKS) return;
        lastSpawnAttempt.put(uuid, now);

        // Random chance to spawn
        if (random.nextDouble() > SPAWN_CHANCE) return;

        // Try to spawn the target mob
        trySpawnBountyMob(player, world, quest.slayerType);
    }

    private static void trySpawnBountyMob(ServerPlayer player, ServerLevel world, SlayerManager.SlayerType type) {
        // Find a valid spawn position
        BlockPos playerPos = player.blockPosition();

        for (int attempt = 0; attempt < 5; attempt++) {
            int offsetX = random.nextInt(SPAWN_RADIUS * 2) - SPAWN_RADIUS;
            int offsetZ = random.nextInt(SPAWN_RADIUS * 2) - SPAWN_RADIUS;

            // Ensure minimum distance
            if (Math.abs(offsetX) < MIN_DISTANCE && Math.abs(offsetZ) < MIN_DISTANCE) {
                continue;
            }

            BlockPos spawnPos = playerPos.offset(offsetX, 0, offsetZ);

            // Find ground level
            spawnPos = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawnPos);

            // Check if valid spawn location
            if (!isValidSpawnLocation(world, spawnPos, type)) {
                continue;
            }

            // Spawn the mob
            Mob mob = createMobForType(world, type);
            if (mob != null) {
                mob.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
                mob.finalizeSpawn(world, world.getCurrentDifficultyAt(spawnPos), EntitySpawnReason.NATURAL, null);
                world.addFreshEntity(mob);
                return;
            }
        }
    }

    private static boolean isValidSpawnLocation(ServerLevel world, BlockPos pos, SlayerManager.SlayerType type) {
        // Check basic spawn conditions
        if (!world.getBlockState(pos).isAir()) return false;
        if (!world.getBlockState(pos.above()).isAir()) return false;

        // Undead mobs should not spawn in daylight [1]
        if (isUndeadType(type)) {
            int skyLight = world.getBrightness(LightLayer.SKY, pos);
            boolean isDaytime = world.isBrightOutside();

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

    private static Mob createMobForType(ServerLevel world, SlayerManager.SlayerType type) {
        return switch (type) {
            case ZOMBIE -> new Zombie(EntityType.ZOMBIE, world);
            case SPIDER -> new Spider(EntityType.SPIDER, world);
            case SKELETON -> new Skeleton(EntityType.SKELETON, world);
            case SLIME -> {
                Slime slime = new Slime(EntityType.SLIME, world);
                // Random size 1-3
                yield slime;
            }
            case ENDERMAN -> new EnderMan(EntityType.ENDERMAN, world);
            case IRON_GOLEM -> null; // Never spawn wardens [1]
            case PIGLIN -> new PiglinBrute(EntityType.PIGLIN_BRUTE, world);
        };
    }

    // Configuration getters and setters
    public static double getSpawnChance() {
        return SPAWN_CHANCE;
    }

    public static void setSpawnChance(double chance) {
        SPAWN_CHANCE = chance;
    }

    public static long getSpawnCooldown() {
        return SPAWN_COOLDOWN_TICKS;
    }

    public static void setSpawnCooldown(long cooldown) {
        SPAWN_COOLDOWN_TICKS = cooldown;
    }

    public static int getSpawnRadius() {
        return SPAWN_RADIUS;
    }

    public static void setSpawnRadius(int radius) {
        SPAWN_RADIUS = radius;
    }
}