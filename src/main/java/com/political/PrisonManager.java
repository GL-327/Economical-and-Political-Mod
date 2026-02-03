package com.political;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameMode;

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

    public static void imprison(ServerPlayerEntity target, int minutes, double x, double y, double z) {
        String uuid = target.getUuidAsString();
        long releaseTime = System.currentTimeMillis() + (minutes * 60L * 1000L);

        prisoners.put(uuid, releaseTime);
        prisonerLocations.put(uuid, x + "," + y + "," + z);

        target.teleport(x, 100, z, true);
        target.changeGameMode(GameMode.ADVENTURE);

        target.sendMessage(Text.literal("You have been imprisoned for " + minutes + " minutes!").formatted(Formatting.RED));
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

            ServerPlayerEntity player = server.getPlayerManager().getPlayer(java.util.UUID.fromString(uuid));
            if (player != null) {
                player.changeGameMode(GameMode.SURVIVAL);
                player.sendMessage(Text.literal("You have been released from prison!").formatted(Formatting.GREEN));
            }
        }

        if (!toRelease.isEmpty()) {
            DataManager.save(server);
        }
    }

    public static void checkPlayerJoin(ServerPlayerEntity player) {
        String uuid = player.getUuidAsString();

        if (prisoners.containsKey(uuid)) {
            long releaseTime = prisoners.get(uuid);
            if (System.currentTimeMillis() < releaseTime) {
                String locStr = prisonerLocations.get(uuid);
                if (locStr != null) {
                    String[] parts = locStr.split(",");
                    double x = Double.parseDouble(parts[0]);
                    double y = Double.parseDouble(parts[1]);
                    double z = Double.parseDouble(parts[2]);
                    player.teleport(x, y, z, true);
                }
                player.changeGameMode(GameMode.ADVENTURE);

                long remaining = (releaseTime - System.currentTimeMillis()) / 60000;
                player.sendMessage(Text.literal("You are imprisoned! " + remaining + " minutes remaining.").formatted(Formatting.RED));
            } else {
                prisoners.remove(uuid);
                prisonerLocations.remove(uuid);
                player.changeGameMode(GameMode.SURVIVAL);

            }
        }
    }
    public static void release(ServerPlayerEntity player) {
        String uuid = player.getUuidAsString();

        if (prisoners.containsKey(uuid)) {
            prisoners.remove(uuid);
            prisonerLocations.remove(uuid);
            player.changeGameMode(GameMode.SURVIVAL);
            player.sendMessage(Text.literal("You have been released from prison!").formatted(Formatting.GREEN));
            DataManager.save(PoliticalServer.server);
        }
    }
    public static boolean isPrisoner(String uuid) {
        return prisoners.containsKey(uuid);
    }
}