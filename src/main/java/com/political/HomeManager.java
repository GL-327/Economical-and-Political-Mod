package com.political;


import com.political.core.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.political.party.PartyPerkEffects;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class HomeManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(HomeManager.class);


    private static final ConcurrentHashMap<String, Long> lastHomeTeleportMs = new ConcurrentHashMap<>();

    public static void setHome(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        String worldId = player.level().dimension().identifier().toString();
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();
        float yaw = player.getYRot();
        float pitch = player.getXRot();

        String homeData = worldId + "," + x + "," + y + "," + z + "," + yaw + "," + pitch;
        // FIXED: Use correct field name
        DataManager.getData().playerHomes.put(uuid, homeData);
        DataManager.save(PoliticalServer.server);

        player.sendSystemMessage(Component.literal("Home set!").withStyle(ChatFormatting.GREEN));
    }

    public static boolean teleportHome(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        // FIXED: Use correct field name
        String homeData = DataManager.getData().playerHomes.get(uuid);

        if (homeData == null) {
            player.sendSystemMessage(Component.literal("You haven't set a home! Use /sethome first.").withStyle(ChatFormatting.RED));
            return false;
        }

        long now = Clock.gameMillis();
        long cd = PartyPerkEffects.homeCooldownMs(uuid);
        Long last = lastHomeTeleportMs.get(uuid);
        if (last != null && now - last < cd) {
            long sec = (cd - (now - last) + 999L) / 1000L;
            player.sendSystemMessage(Component.literal("Home on cooldown: " + sec + "s").withStyle(ChatFormatting.RED));
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

            ResourceKey<net.minecraft.world.level.Level> worldKey = ResourceKey.create(Registries.DIMENSION, Identifier.parse(worldId));
            ServerLevel world = PoliticalServer.server.getLevel(worldKey);

            if (world == null) {
                player.sendSystemMessage(Component.literal("Home world not found!").withStyle(ChatFormatting.RED));
                return false;
            }

            PlayerTeleportHelper.teleport(player, world, x, y, z, yaw, pitch);
            lastHomeTeleportMs.put(uuid, Clock.gameMillis());
            player.sendSystemMessage(Component.literal("Teleported home!").withStyle(ChatFormatting.GREEN));
            return true;
        } catch (Exception e) {
            LOGGER.warn("HomeManager.teleportHome: ignored an error", e);
            player.sendSystemMessage(Component.literal("Error teleporting home!").withStyle(ChatFormatting.RED));
            return false;
        }
    }
}