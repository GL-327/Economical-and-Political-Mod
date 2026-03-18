package com.political;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.Set;

public class HomeManager {

    public static void setHome(ServerPlayerEntity player) {
        String uuid = player.getUuidAsString();
        String worldId = player.getEntityWorld().getRegistryKey().getValue().toString();
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();
        float yaw = player.getYaw();
        float pitch = player.getPitch();

        String homeData = worldId + "," + x + "," + y + "," + z + "," + yaw + "," + pitch;
        // FIXED: Use correct field name
        DataManager.getData().playerHomes.put(uuid, homeData);
        DataManager.save(PoliticalServer.server);

        player.sendMessage(Text.literal("Home set!").formatted(Formatting.GREEN));
    }

    public static boolean teleportHome(ServerPlayerEntity player) {
        String uuid = player.getUuidAsString();
        // FIXED: Use correct field name
        String homeData = DataManager.getData().playerHomes.get(uuid);

        if (homeData == null) {
            player.sendMessage(Text.literal("You haven't set a home! Use /sethome first.").formatted(Formatting.RED));
            return false;
        }

        try {
            String[] parts = homeData.split(",");
            String worldId = parts[0];
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            float yaw = Float.parseFloat(parts[4]);
            float pitch = Float.parseFloat(parts[5]);

            RegistryKey<net.minecraft.world.World> worldKey = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(worldId));
            ServerWorld world = PoliticalServer.server.getWorld(worldKey);

            if (world == null) {
                player.sendMessage(Text.literal("Home world not found!").formatted(Formatting.RED));
                return false;
            }

            player.teleport(world, x, y, z, Set.of(), yaw, pitch, false);
            player.sendMessage(Text.literal("Teleported home!").formatted(Formatting.GREEN));
            return true;
        } catch (Exception e) {
            player.sendMessage(Text.literal("Error teleporting home!").formatted(Formatting.RED));
            return false;
        }
    }
}