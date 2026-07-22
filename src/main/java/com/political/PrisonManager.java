package com.political;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.GameType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrisonManager {

    private static Map<String, Long> prisoners = new HashMap<>();
    private static Map<String, String> prisonerLocations = new HashMap<>();

    public static void loadFromData(DataManager.SaveData data) {
        prisoners = data.prisoners != null ? new HashMap<>(data.prisoners) : new HashMap<>();
        prisonerLocations = data.prisonerLocations != null ? new HashMap<>(data.prisonerLocations) : new HashMap<>();
    }

    public static void saveToData(DataManager.SaveData data) {
        data.prisoners = new HashMap<>(prisoners);
        data.prisonerLocations = new HashMap<>(prisonerLocations);
    }

    public static void imprison(ServerPlayer target, int minutes, double x, double y, double z) {
        String uuid = target.getUUID().toString();
        
        // JUDGE_MASTER buff: 25% longer sentences
        double sentenceBonus = PlayerBuffManager.getPrisonSentenceBonus(uuid);
        int adjustedMinutes = (int) Math.ceil(minutes * (1.0 + sentenceBonus));
        
        long releaseTime = System.currentTimeMillis() + (adjustedMinutes * 60L * 1000L);

        prisoners.put(uuid, releaseTime);
        prisonerLocations.put(uuid, x + "," + y + "," + z);

        PlayerTeleportHelper.teleport(target, (net.minecraft.server.level.ServerLevel) target.level(), x, 100, z, target.getYRot(), target.getXRot());
        target.setGameMode(GameType.ADVENTURE);

        target.sendSystemMessage(Component.literal("You have been imprisoned for " + minutes + " minutes!").withStyle(ChatFormatting.RED));
        DataManager.save(PoliticalServer.server);
    }

    public static void tick(MinecraftServer server) {
        long now = System.currentTimeMillis();
        List<String> toRelease = new ArrayList<>();

        for (Map.Entry<String, Long> entry : prisoners.entrySet()) {
            if (now >= entry.getValue()) {
                toRelease.add(entry.getKey());
            }
        }

        for (String uuid : toRelease) {
            prisoners.remove(uuid);
            prisonerLocations.remove(uuid);

            ServerPlayer player = server.getPlayerList().getPlayer(java.util.UUID.fromString(uuid));
            if (player != null) {
                player.setGameMode(GameType.SURVIVAL);
                player.sendSystemMessage(Component.literal("You have been released from prison!").withStyle(ChatFormatting.GREEN));
            }
        }

        if (!toRelease.isEmpty()) {
            DataManager.save(server);
        }
    }

    public static void checkPlayerJoin(ServerPlayer player) {
        String uuid = player.getUUID().toString();

        if (prisoners.containsKey(uuid)) {
            long releaseTime = prisoners.get(uuid);
            if (System.currentTimeMillis() < releaseTime) {
                String locStr = prisonerLocations.get(uuid);
                if (locStr != null) {
                    String[] parts = locStr.split(",");
                    double x = Double.parseDouble(parts[0]);
                    double y = Double.parseDouble(parts[1]);
                    double z = Double.parseDouble(parts[2]);
                    PlayerTeleportHelper.teleport(player, (net.minecraft.server.level.ServerLevel) player.level(), x, y, z, player.getYRot(), player.getXRot());
                }
                player.setGameMode(GameType.ADVENTURE);

                long remaining = (releaseTime - System.currentTimeMillis()) / 60000;
                player.sendSystemMessage(Component.literal("You are imprisoned! " + remaining + " minutes remaining.").withStyle(ChatFormatting.RED));
            } else {
                prisoners.remove(uuid);
                prisonerLocations.remove(uuid);
                player.setGameMode(GameType.SURVIVAL);

            }
        }
    }
    public static void release(ServerPlayer player) {
        String uuid = player.getUUID().toString();

        if (prisoners.containsKey(uuid)) {
            prisoners.remove(uuid);
            prisonerLocations.remove(uuid);
            player.setGameMode(GameType.SURVIVAL);
            player.sendSystemMessage(Component.literal("You have been released from prison!").withStyle(ChatFormatting.GREEN));
            DataManager.save(PoliticalServer.server);
        }
    }
    public static boolean isPrisoner(String uuid) {
        return prisoners.containsKey(uuid);
    }
}