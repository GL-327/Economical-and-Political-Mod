package com.political;

import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;

import java.util.Set;

public class SpawnManager {

    private static String spawnWorld = null;
    private static double spawnX = 0;
    private static double spawnY = 64;
    private static double spawnZ = 0;
    private static float spawnYaw = 0;
    private static float spawnPitch = 0;

    public static void loadFromData(DataManager.SaveData data) {
        if (data.spawnWorld != null) {
            spawnWorld = data.spawnWorld;
            spawnX = data.spawnX;
            spawnY = data.spawnY;
            spawnZ = data.spawnZ;
            spawnYaw = data.spawnYaw;
            spawnPitch = data.spawnPitch;
        }
    }

    public static void saveToData(DataManager.SaveData data) {
        data.spawnWorld = spawnWorld;
        data.spawnX = spawnX;
        data.spawnY = spawnY;
        data.spawnZ = spawnZ;
        data.spawnYaw = spawnYaw;
        data.spawnPitch = spawnPitch;
    }

    public static void setSpawn(ServerPlayer player) {
        spawnWorld = player.level().dimension().identifier().toString();
        spawnX = player.getX();
        spawnY = player.getY();
        spawnZ = player.getZ();
        spawnYaw = player.getYRot();
        spawnPitch = player.getXRot();

        DataManager.save(PoliticalServer.server);
        player.sendSystemMessage(Component.literal("✓ Spawn location set!").withStyle(ChatFormatting.GREEN));
    }

    public static void teleportToSpawn(ServerPlayer player) {
        // Default to worldspawn if no custom spawn is set
        if (spawnWorld == null) {
            // Use vanilla worldspawn coordinates (default Minecraft spawn)
            ServerLevel overworld = PoliticalServer.server.getLevel(ResourceKey.create(Registries.DIMENSION, Identifier.withDefaultNamespace("overworld")));
            if (overworld != null) {
                // Default spawn is at y=64 in Minecraft
                PlayerTeleportHelper.teleport(player, overworld, 0.5, 64, 0.5, 0, 0);
                player.sendSystemMessage(Component.literal("Teleported to world spawn!").withStyle(ChatFormatting.GREEN));
            } else {
                player.sendSystemMessage(Component.literal("Could not find overworld!").withStyle(ChatFormatting.RED));
            }
            return;
        }

        try {
            ResourceKey<net.minecraft.world.level.Level> worldKey = ResourceKey.create(Registries.DIMENSION, Identifier.parse(spawnWorld));
            ServerLevel world = PoliticalServer.server.getLevel(worldKey);

            if (world == null) {
                player.sendSystemMessage(Component.literal("Spawn world not found!").withStyle(ChatFormatting.RED));
                return;
            }

            PlayerTeleportHelper.teleport(player, world, spawnX, spawnY, spawnZ, spawnYaw, spawnPitch);
            player.sendSystemMessage(Component.literal("Teleported to spawn!").withStyle(ChatFormatting.GREEN));
        } catch (Exception e) {
            player.sendSystemMessage(Component.literal("Error teleporting to spawn!").withStyle(ChatFormatting.RED));
            PoliticalServer.LOGGER.error("Error teleporting to spawn", e);
        }
    }

    public static boolean hasSpawn() {
        return spawnWorld != null;
    }

    public static String getSpawnInfo() {
        if (!hasSpawn()) {
            return "Not set";
        }
        return String.format("%.1f, %.1f, %.1f", spawnX, spawnY, spawnZ);
    }
}