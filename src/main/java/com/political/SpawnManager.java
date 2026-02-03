package com.political;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

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

    public static void setSpawn(ServerPlayerEntity player) {
        spawnWorld = player.getEntityWorld().getRegistryKey().getValue().toString();
        spawnX = player.getX();
        spawnY = player.getY();
        spawnZ = player.getZ();
        spawnYaw = player.getYaw();
        spawnPitch = player.getPitch();

        DataManager.save(PoliticalServer.server);
        player.sendMessage(Text.literal("✓ Spawn location set!").formatted(Formatting.GREEN));
    }

    public static void teleportToSpawn(ServerPlayerEntity player) {
        if (spawnWorld == null) {
            player.sendMessage(Text.literal("Spawn has not been set yet!").formatted(Formatting.RED));
            return;
        }

        try {
            RegistryKey<net.minecraft.world.World> worldKey = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(spawnWorld));
            ServerWorld world = PoliticalServer.server.getWorld(worldKey);

            if (world == null) {
                player.sendMessage(Text.literal("Spawn world not found!").formatted(Formatting.RED));
                return;
            }

            player.teleport(world, spawnX, spawnY, spawnZ, Set.of(), spawnYaw, spawnPitch, false);
            player.sendMessage(Text.literal("Teleported to spawn!").formatted(Formatting.GREEN));
        } catch (Exception e) {
            player.sendMessage(Text.literal("Error teleporting to spawn!").formatted(Formatting.RED));
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