package com.political.backrooms.fracture3;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Void Respawn Handler
 * 
 * Handles players falling into the void in Fracture 3.
 * - 0-3 seconds falling: no damage
 * - 3+ seconds falling: 1 heart per second void damage
 * - Teleports player to last visited room center
 * - On respawn: Nausea II for 10 seconds, Blindness I for 3 seconds
 */
public class VoidRespawnHandler {

    // Track fall times for each player
    private static final Map<UUID, Long> fallStartTimes = new HashMap<>();
    private static final Map<UUID, BlockPos> lastValidPositions = new HashMap<>();
    
    // Void damage threshold (3 seconds in milliseconds)
    private static final long VOID_DAMAGE_THRESHOLD = 3000L;
    
    // Track player position for respawn
    public static void updatePlayerPosition(ServerPlayerEntity player) {
        if (!Fracture3DimensionManager.isInFracture3(player)) return;
        
        BlockPos pos = player.getBlockPos();
        
        // Only track valid positions (not in void)
        if (pos.getY() >= Fracture3ChunkGenerator.FLOOR_Y - 5) {
            lastValidPositions.put(player.getUuid(), pos);
            fallStartTimes.remove(player.getUuid());
        }
    }
    
    /**
     * Tick handler for void detection
     * Call this every tick for players in Fracture 3
     */
    public static void tickPlayer(ServerPlayerEntity player) {
        if (!Fracture3DimensionManager.isInFracture3(player)) {
            fallStartTimes.remove(player.getUuid());
            return;
        }
        
        BlockPos pos = player.getBlockPos();
        long now = System.currentTimeMillis();
        
        // Check if player is in void (below floor or above ceiling)
        boolean inVoid = pos.getY() < Fracture3ChunkGenerator.FLOOR_Y - 1 
                      || pos.getY() > Fracture3ChunkGenerator.CEILING_Y + 5;
        
        if (inVoid) {
            // Track fall start time
            if (!fallStartTimes.containsKey(player.getUuid())) {
                fallStartTimes.put(player.getUuid(), now);
            }
            
            long fallDuration = now - fallStartTimes.get(player.getUuid());
            
            // Apply void damage after 3 seconds
            if (fallDuration > VOID_DAMAGE_THRESHOLD) {
                if (player.age % 20 == 0) {
                    if (player.getHealth() <= 2.0f) {
                        // Prevent death, trigger handler teleport
                        player.setHealth(20.0f);
                        respawnPlayer(player);
                    } else {
                        // 1 heart (2 damage) per second
                        player.damage(player.getEntityWorld(), player.getDamageSources().outOfWorld(), 2.0f);
                    }
                }
            }
        } else {
            // Update valid position
            updatePlayerPosition(player);
        }
    }
    
    /**
     * Respawn player at last valid position
     */
    private static void respawnPlayer(ServerPlayerEntity player) {
        BlockPos lastPos = lastValidPositions.get(player.getUuid());
        
        if (lastPos == null) {
            // No tracked position - respawn at origin
            lastPos = Fracture3DimensionManager.SPAWN_POS;
        }
        
        ServerWorld world = (ServerWorld) player.getEntityWorld();
        
        // Teleport to last valid position
        player.teleport(world, lastPos.getX() + 0.5, lastPos.getY(), lastPos.getZ() + 0.5,
            java.util.Set.of(), player.getYaw(), player.getPitch(), true);
        
        // Apply effects on respawn
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 200, 1, false, false, true)); // 10 seconds
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 60, 0, false, false, true)); // 3 seconds
        
        // Reset fall time
        fallStartTimes.remove(player.getUuid());
    }
    
    /**
     * Clear tracking data for a player
     */
    public static void clearPlayer(UUID playerId) {
        fallStartTimes.remove(playerId);
        lastValidPositions.remove(playerId);
    }
}
