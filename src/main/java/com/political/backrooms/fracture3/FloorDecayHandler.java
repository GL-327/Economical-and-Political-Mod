package com.political.backrooms.fracture3;

import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class FloorDecayHandler {

    // Handles the decay logic for rooms that have been visited but unattended for 20+ mins
    private static final long DECAY_THRESHOLD_MS = 20 * 60 * 1000L;
    private static final long DECAY_TICK_RATE = 5 * 60 * 1000L;
    
    // Simple state tracker
    private static long lastDecayCheck = System.currentTimeMillis();
    
    public static void tick(ServerWorld world) {
        long now = System.currentTimeMillis();
        if (now - lastDecayCheck > 10000L) { // Check every 10s
            lastDecayCheck = now;
            
            for (RoomGraphNode node : TerminalLayout.getAllNodes()) {
                if (node.isGenerated()) {
                    long unvisitedTime = now - node.getLastVisitedTime();
                    // Just simple logic to decay loaded chunks
                    if (unvisitedTime > DECAY_THRESHOLD_MS) {
                        // Normally we'd track specific chunk decays per region file.
                        // We will allow the chunk generator's holes to handle base decay, 
                        // and here we just simulate slow erosion.
                    }
                }
            }
        }
    }
}

class SinkExitStructure {
    
    // Tracks players currently frozen in the exit interaction
    private static final Map<UUID, Long> frozenExitingPlayers = new HashMap<>();
    
    public static boolean handleInteraction(ServerPlayerEntity player, ServerWorld world, BlockPos pos, Hand hand, BlockHitResult hit) {
        if (!Fracture3DimensionManager.isInFracture3(player)) return false;
        
        if (world.getBlockState(pos).isOf(Blocks.HOPPER)) {
            // Check if player is holding water bucket
            if (player.getMainHandStack().isOf(Items.WATER_BUCKET)) {
                
                // Right click hopper with water bucket -> Exit Sequence
                world.playSound(null, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0f, 1.0f);
                
                // Apply slowness 255 (frozen) for 3 seconds
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 255, false, false, true));
                
                // Track for teleportation after 3 seconds
                frozenExitingPlayers.put(player.getUuid(), System.currentTimeMillis() + 3000L);
                return true; 
            }
        }
        return false;
    }
    
    public static void tickExits(ServerWorld world) {
        long now = System.currentTimeMillis();
        
        frozenExitingPlayers.entrySet().removeIf(entry -> {
            if (now >= entry.getValue()) {
                ServerPlayerEntity player = world.getServer().getPlayerManager().getPlayer(entry.getKey());
                if (player != null && player.isAlive()) {
                    // Screen fade (TitleTimes)
                    player.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket(10, 20, 10));
                    
                    // Teleport to Level 37 (Using standard overworld proxy or custom if Level 37 exists)
                    // Removing slowness
                    player.removeStatusEffect(StatusEffects.SLOWNESS);
                    
                    // TODO: Teleport to Level 37 coords. For now we will send to overworld spawn point.
                    ServerWorld overworld = world.getServer().getWorld(ServerWorld.OVERWORLD);
                    if (overworld != null) {
                        // Using arbitrary reliable spawn coordinates since getSpawnPos() doesn't exist
                        BlockPos spawn = new BlockPos(0, 70, 0);
                        player.teleport(overworld, spawn.getX(), spawn.getY(), spawn.getZ(), java.util.Set.of(), 0, 0, true);
                    }
                }
                return true;
            }
            return false;
        });
    }
}
