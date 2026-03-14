package com.political.backrooms.fracture3;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * The Presence Event Handler
 */
public class FakePlayerPacketHandler {

    // Tracks if a player has seen The Presence this session
    private static final Map<UUID, Boolean> hasSeenPresence = new HashMap<>();
    
    // Tracks start time in dimension to trigger event
    private static final Map<UUID, Long> dimensionEntryTime = new HashMap<>();
    
    // Pending despawns
    private static final Map<UUID, Long> pendingDespawns = new HashMap<>();
    private static final Map<UUID, Integer> fakeEntityIds = new HashMap<>();

    private static final Random random = new Random();

    public static void onPlayerEnter(ServerPlayerEntity player) {
        if (!hasSeenPresence.containsKey(player.getUuid())) {
            hasSeenPresence.put(player.getUuid(), false);
            // Target trigger: 5 to 15 mins
            long triggerDelay = (5 * 60 * 1000L) + random.nextInt(10 * 60 * 1000);
            dimensionEntryTime.put(player.getUuid(), System.currentTimeMillis() + triggerDelay);
        }
    }

    public static void tickPlayer(ServerPlayerEntity player) {
        if (!Fracture3DimensionManager.isInFracture3(player)) {
            dimensionEntryTime.remove(player.getUuid());
            return;
        }

        UUID uuid = player.getUuid();
        if (hasSeenPresence.getOrDefault(uuid, false)) {
            return; // Already seen it
        }

        Long triggerTime = dimensionEntryTime.get(uuid);
        if (triggerTime != null && System.currentTimeMillis() > triggerTime) {
            triggerPresence(player);
        }
    }

    private static void triggerPresence(ServerPlayerEntity player) {
        // Mark as seen
        hasSeenPresence.put(player.getUuid(), true);
        dimensionEntryTime.remove(player.getUuid());

        // Spawn fake player 60-80 blocks away down current corridor
        // For simplicity in this shell, we just use the player's look vector
        double distance = 60.0 + random.nextInt(20);
        double spawnX = player.getX() + player.getRotationVec(1.0f).x * distance;
        double spawnY = player.getY();
        double spawnZ = player.getZ() + player.getRotationVec(1.0f).z * distance;

        // Create fake profile using player's name
        GameProfile fakeProfile = new GameProfile(UUID.randomUUID(), player.getName().getString());
        int fakeEntityId = 99999 + random.nextInt(10000);

        // Send Spawn Packet (We would use a proper PlayerSpawnPacket and PlayerListS2CPacket for skins)
        // Here we just use the basic logic placeholder
        // net.minecraft.network.packet.s2c.play.PlayerListS2CPacket (to add the profile temporarily)
        // net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket (to spawn)
        
        // Track for despawn in 4 seconds
        fakeEntityIds.put(player.getUuid(), fakeEntityId);
        pendingDespawns.put(player.getUuid(), System.currentTimeMillis() + 4000L);
    }

    public static void tickServer(net.minecraft.server.MinecraftServer server) {
        long now = System.currentTimeMillis();
        
        pendingDespawns.entrySet().removeIf(entry -> {
            if (now > entry.getValue()) {
                UUID playerId = entry.getKey();
                int fakeId = fakeEntityIds.get(playerId);
                
                ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerId);
                if (player != null) {
                    player.networkHandler.sendPacket(new EntitiesDestroyS2CPacket(fakeId));
                    // Also send PlayerList packet to remove profile to clean up tab list
                }
                
                fakeEntityIds.remove(playerId);
                return true;
            }
            return false;
        });
    }
}
