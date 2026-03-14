package com.political.backrooms.fracture3;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class Fracture3SoundEngine {

    // Track the last room a player was in to detect threshold crossing for room_creak
    private static final Map<UUID, UUID> playerCurrentRoom = new HashMap<>();

    public static void tickPlayer(ServerPlayerEntity player) {
        if (!Fracture3DimensionManager.isInFracture3(player)) {
            playerCurrentRoom.remove(player.getUuid());
            return;
        }

        BlockPos pos = player.getBlockPos();
        Optional<RoomGraphNode> currentRoom = TerminalLayout.findNodeAt(pos.getX(), pos.getZ());
        
        if (currentRoom.isPresent()) {
            UUID roomId = currentRoom.get().getId();
            UUID lastRoomId = playerCurrentRoom.get(player.getUuid());

            // 1. Threshold crossing: room_creak
            if (lastRoomId != null && !lastRoomId.equals(roomId)) {
                // Player crossed into a new room
                float pitch = 0.95f + player.getRandom().nextFloat() * 0.10f; // 0.95 to 1.05
                player.getEntityWorld().playSound(null, pos, 
                        SoundEvent.of(Identifier.of("backrooms", "room_creak")), 
                        SoundCategory.AMBIENT, 0.2f, pitch);
            }
            playerCurrentRoom.put(player.getUuid(), roomId);
        }

        // 2. Phone ring: 1% chance per minute
        // Measured per tick: 1% per 1200 ticks = 1 in 120,000 chance per tick
        if (player.getRandom().nextInt(120000) == 0) {
            // Find random location in current room (or within 10 blocks)
            BlockPos ringPos = pos.add(
                    player.getRandom().nextInt(20) - 10,
                    0,
                    player.getRandom().nextInt(20) - 10
            );
            player.getEntityWorld().playSound(null, ringPos, 
                    SoundEvent.of(Identifier.of("backrooms", "phone_ring")), 
                    SoundCategory.AMBIENT, 0.3f, 1.0f);
        }

        // Void Wind: When player is near a void edge
        // Handled efficiently if we check blocks below them occasionally
        if (player.age % 10 == 0) {
            boolean nearVoid = false;
            // Check 3 block radius around player for void (Y < -60)
            int yFloor = Fracture3ChunkGenerator.FLOOR_Y;
            for (int dx = -3; dx <= 3; dx++) {
                for (int dz = -3; dz <= 3; dz++) {
                    if (pos.getY() > yFloor - 2 && pos.getY() < yFloor + 5) {
                        if (player.getEntityWorld().isAir(pos.add(dx, yFloor - pos.getY() - 1, dz))) {
                            nearVoid = true;
                            // Check deeper to confirm void 
                            if (player.getEntityWorld().isAir(pos.add(dx, -50, dz))) {
                                nearVoid = true;
                                break;
                            }
                        }
                    }
                }
                if (nearVoid) break;
            }

            if (nearVoid) {
                player.getEntityWorld().playSound(null, pos.down(3), 
                        SoundEvent.of(Identifier.of("backrooms", "void_wind")), 
                        SoundCategory.AMBIENT, 0.6f, 0.7f);
            }
        }
    }
}
