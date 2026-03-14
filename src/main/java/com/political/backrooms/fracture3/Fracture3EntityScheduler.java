package com.political.backrooms.fracture3;

import com.political.backrooms.entities.HoundEntity;
import com.political.backrooms.entities.SmilerEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

public class Fracture3EntityScheduler {

    private static long lastSpawnTick = 0;

    public static void tick(ServerWorld world) {
        if (!world.getRegistryKey().getValue().getPath().equals("fracture_3")) {
            return;
        }

        // Tick custom handlers
        FlickerHandler.tick(world);
        FloorDecayHandler.tick(world);
        SinkExitStructure.tickExits(world);
        FakePlayerPacketHandler.tickServer(world.getServer());

        // Process spawns every ~10 seconds
        long time = world.getTime();
        if (time - lastSpawnTick < 200) {
            return;
        }
        lastSpawnTick = time;

        // Vanilla spawning is disabled by dimension rules (doMobSpawning = false)
        // Manual spawn logic for Smilers and Hounds

        for (net.minecraft.server.network.ServerPlayerEntity player : world.getPlayers()) {
            
            // Smiler spawn: light level <= 2
            BlockPos playerPos = player.getBlockPos();
            if (world.getLightLevel(playerPos) <= 2) {
                // If it's dark near the player, 5% chance to spawn a smiler 10-15 blocks away
                if (world.getRandom().nextFloat() < 0.05f) {
                    SmilerEntity smiler = new SmilerEntity(EntityType.ENDERMAN, world);
                    if (smiler != null) {
                        smiler.refreshPositionAndAngles(playerPos.getX() + 10, playerPos.getY(), playerPos.getZ() + 10, 0, 0);
                        world.spawnEntity(smiler);
                    }
                }
            }
            
            // Hound spawn: Long Corridor > 48 blocks
            Optional<RoomGraphNode> currentNode = TerminalLayout.findNodeAt(playerPos.getX(), playerPos.getZ());
            if (currentNode.isPresent() && currentNode.get().getType() == RoomGraphNode.RoomType.CORRIDOR) {
                int length = Math.max(currentNode.get().getWidth(), currentNode.get().getLength());
                if (length >= 48) {
                    if (world.getRandom().nextFloat() < 0.02f) { // Rare spawn
                        HoundEntity hound = new HoundEntity(EntityType.ZOMBIE, world);
                        if (hound != null) {
                            hound.refreshPositionAndAngles(currentNode.get().getX() + 2, 65, currentNode.get().getZ() + 2, 0, 0);
                            world.spawnEntity(hound);
                        }
                    }
                }
            }
            
            // The Presence Event logic
            FakePlayerPacketHandler.tickPlayer(player);
            
            // Void Respawn tracking
            VoidRespawnHandler.tickPlayer(player);
        }
    }
}
