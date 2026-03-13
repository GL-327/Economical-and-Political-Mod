package com.political.backrooms.inventory;

import com.political.backrooms.BackroomsDimensionManager;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

/**
 * Manages player spawn points and inventory swapping when entering/exiting Backrooms dimensions.
 * Register via {@link #register(MinecraftServer)}.
 */
public class BackroomsSpawnManager {

    public static void register() {
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register(
            (player, origin, destination) -> {
                MinecraftServer server = ((ServerWorld) player.getEntityWorld()).getServer();
                handleDimensionChange(player, origin, destination, server);
            });
    }

    private static void handleDimensionChange(
            ServerPlayerEntity player,
            ServerWorld origin,
            ServerWorld destination,
            MinecraftServer server) {

        boolean destIsBackrooms = isBackroomsDimension(destination);
        boolean originIsBackrooms = isBackroomsDimension(origin);

        if (!originIsBackrooms && destIsBackrooms) {
            // Player entering Backrooms
            BackroomsInventoryManager.storeAndClearInventory(player, server);
        } else if (originIsBackrooms && !destIsBackrooms) {
            // Player exiting Backrooms
            BackroomsInventoryManager.restoreInventory(player, server);
        }
    }

    private static boolean isBackroomsDimension(ServerWorld world) {
        return world.getRegistryKey().equals(BackroomsDimensionManager.BACKROOMS_KEY)
            || world.getRegistryKey().equals(BackroomsDimensionManager.POOLROOMS_KEY)
            || world.getRegistryKey().equals(BackroomsDimensionManager.INDUSTRIAL_KEY);
    }
}
