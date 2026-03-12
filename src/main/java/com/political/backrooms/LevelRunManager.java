package com.political.backrooms;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LevelRunManager {

    private static final Map<UUID, LevelRunSession> activeSessions = new HashMap<>();

    public static class LevelRunSession {
        public final ServerPlayerEntity player;
        public final long startTime;
        public final double startX;
        public int distanceTraveled;
        public static final int TOTAL_DISTANCE = 500;

        public LevelRunSession(ServerPlayerEntity player) {
            this.player = player;
            this.startTime = player.getEntityWorld().getTime();
            this.startX = player.getX();
            this.distanceTraveled = 0;
        }

        public boolean isComplete() {
            return distanceTraveled >= TOTAL_DISTANCE;
        }
    }

    public static void startLevelRun(ServerPlayerEntity player) {
        if (activeSessions.containsKey(player.getUuid())) return;

        LevelRunSession session = new LevelRunSession(player);
        activeSessions.put(player.getUuid(), session);

        player.networkHandler.sendPacket(new TitleS2CPacket(
            Text.literal("RUN!").formatted(Formatting.RED, Formatting.BOLD)));
        player.networkHandler.sendPacket(new SubtitleS2CPacket(
            Text.literal("They are coming...").formatted(Formatting.DARK_RED)));

        player.sendMessage(Text.literal("§c§lYou have entered Level !. Reach the exit!"), false);

        spawnHorde(player, (ServerWorld) player.getEntityWorld());
    }

    private static void spawnHorde(ServerPlayerEntity player, ServerWorld world) {
        int count = 3 + new java.util.Random().nextInt(3); // 3-5 hounds per wave
        for (int i = 0; i < count; i++) {
            double offsetX = (Math.random() - 0.5) * 8;
            double offsetZ = -5 - (Math.random() * 5);
            BlockPos spawnPos = BlockPos.ofFloored(
                player.getX() + offsetX,
                player.getY(),
                player.getZ() + offsetZ);

            ZombieEntity zombie = new ZombieEntity(EntityType.ZOMBIE, world);
            zombie.refreshPositionAndAngles(
                spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), 0, 0);
            zombie.setCustomName(Text.literal("Hound").formatted(Formatting.DARK_RED));
            if (zombie.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED) != null) {
                zombie.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED).setBaseValue(0.5);
            }
            zombie.setTarget(player);
            world.spawnEntity(zombie);
        }
    }

    public static void tickPlayer(ServerPlayerEntity player) {
        LevelRunSession session = activeSessions.get(player.getUuid());
        if (session == null) return;

        session.distanceTraveled = (int) Math.max(
            session.distanceTraveled,
            Math.abs(player.getX() - session.startX));

        if (player.age % 40 == 0) {
            int remaining = LevelRunSession.TOTAL_DISTANCE - session.distanceTraveled;
            player.sendMessage(Text.literal("§c⚡ Distance remaining: " + remaining + " blocks"), true);

            if (player.age % 200 == 0) {
                spawnHorde(player, (ServerWorld) player.getEntityWorld());
            }
        }

        if (session.isComplete()) {
            endLevelRun(player, true);
        }
    }

    public static boolean isInLevelRun(UUID playerUuid) {
        return activeSessions.containsKey(playerUuid);
    }

    public static void endLevelRun(ServerPlayerEntity player, boolean success) {
        activeSessions.remove(player.getUuid());
        if (success) {
            player.sendMessage(Text.literal("§a§lYou escaped Level !!"), false);
            player.addExperience(500);
        } else {
            player.sendMessage(Text.literal("§c§lYou were consumed..."), false);
        }
    }

    public static void removeSession(UUID playerUuid) {
        activeSessions.remove(playerUuid);
    }
}
