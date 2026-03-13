package com.political.backrooms.protection;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import com.political.PoliticalServer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Build protection system for backrooms areas
 * Protects builds while allowing interactive elements (doors, buttons, levers)
 */
public class BackroomsProtectionManager {
    
    private static final Map<UUID, ProtectedArea> protectedAreas = new HashMap<>();
    private static final Map<BlockPos, String> interactiveBlocks = new HashMap<>();
    
    /**
     * Register a protected area
     */
    public static void protectArea(UUID id, BlockPos min, BlockPos max, String owner) {
        ProtectedArea area = new ProtectedArea(min, max, owner);
        protectedAreas.put(id, area);
        PoliticalServer.LOGGER.info("Protected backrooms area: " + owner + " at " + min + " to " + max);
    }
    
    /**
     * Remove protection for an area
     */
    public static void unprotectArea(UUID id) {
        ProtectedArea area = protectedAreas.remove(id);
        if (area != null) {
            PoliticalServer.LOGGER.info("Unprotected backrooms area: " + area.owner);
        }
    }
    
    /**
     * Check if a block can be broken at the position
     */
    public static boolean canBreakBlock(ServerWorld world, BlockPos pos, PlayerEntity player) {
        // Check if position is in any protected area
        for (ProtectedArea area : protectedAreas.values()) {
            if (area.contains(pos)) {
                // Allow breaking if player is the owner
                if (area.isOwner(player)) {
                    return true;
                }
                
                // Check if it's an interactive block that can be broken
                BlockState state = world.getBlockState(pos);
                if (isInteractiveBlock(state)) {
                    return true;
                }
                
                return false; // Protected
            }
        }
        return true; // Not protected
    }
    
    /**
     * Check if a block can be placed at the position
     */
    public static boolean canPlaceBlock(ServerWorld world, BlockPos pos, PlayerEntity player) {
        for (ProtectedArea area : protectedAreas.values()) {
            if (area.contains(pos)) {
                return area.isOwner(player); // Only owners can place blocks
            }
        }
        return true; // Not protected
    }
    
    /**
     * Check if an entity can interact with a block
     */
    public static boolean canInteract(ServerWorld world, BlockPos pos, PlayerEntity player) {
        for (ProtectedArea area : protectedAreas.values()) {
            if (area.contains(pos)) {
                // Allow interaction with interactive blocks for anyone
                BlockState state = world.getBlockState(pos);
                if (isInteractiveBlock(state)) {
                    return true;
                }
                
                // Allow owners to interact with anything
                return area.isOwner(player);
            }
        }
        return true; // Not protected
    }
    
    /**
     * Check if a block is interactive (door, button, lever, etc.)
     */
    private static boolean isInteractiveBlock(BlockState state) {
        return state.getBlock() == Blocks.OAK_DOOR ||
               state.getBlock() == Blocks.SPRUCE_DOOR ||
               state.getBlock() == Blocks.BIRCH_DOOR ||
               state.getBlock() == Blocks.JUNGLE_DOOR ||
               state.getBlock() == Blocks.ACACIA_DOOR ||
               state.getBlock() == Blocks.DARK_OAK_DOOR ||
               state.getBlock() == Blocks.IRON_DOOR ||
               state.getBlock() == Blocks.STONE_BUTTON ||
               state.getBlock() == Blocks.OAK_BUTTON ||
               state.getBlock() == Blocks.SPRUCE_BUTTON ||
               state.getBlock() == Blocks.BIRCH_BUTTON ||
               state.getBlock() == Blocks.JUNGLE_BUTTON ||
               state.getBlock() == Blocks.ACACIA_BUTTON ||
               state.getBlock() == Blocks.DARK_OAK_BUTTON ||
               state.getBlock() == Blocks.LEVER ||
               state.getBlock() == Blocks.STONE_PRESSURE_PLATE ||
               state.getBlock() == Blocks.OAK_PRESSURE_PLATE ||
               state.getBlock() == Blocks.SPRUCE_PRESSURE_PLATE ||
               state.getBlock() == Blocks.BIRCH_PRESSURE_PLATE ||
               state.getBlock() == Blocks.JUNGLE_PRESSURE_PLATE ||
               state.getBlock() == Blocks.ACACIA_PRESSURE_PLATE ||
               state.getBlock() == Blocks.DARK_OAK_PRESSURE_PLATE ||
               state.getBlock() == Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE ||
               state.getBlock() == Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE ||
               state.getBlock() == Blocks.CHEST ||
               state.getBlock() == Blocks.TRAPPED_CHEST ||
               state.getBlock() == Blocks.ENDER_CHEST ||
               state.getBlock() == Blocks.SHULKER_BOX ||
               state.getBlock() == Blocks.HOPPER ||
               state.getBlock() == Blocks.DROPPER ||
               state.getBlock() == Blocks.DISPENSER ||
               state.getBlock() == Blocks.FURNACE ||
               state.getBlock() == Blocks.BLAST_FURNACE ||
               state.getBlock() == Blocks.SMOKER ||
               state.getBlock() == Blocks.BREWING_STAND ||
               state.getBlock() == Blocks.BEACON ||
               state.getBlock() == Blocks.ENCHANTING_TABLE ||
               state.getBlock() == Blocks.ANVIL ||
               state.getBlock() == Blocks.CHIPPED_ANVIL ||
               state.getBlock() == Blocks.DAMAGED_ANVIL ||
               state.getBlock() == Blocks.CRAFTING_TABLE ||
               state.getBlock() == Blocks.CARTOGRAPHY_TABLE ||
               state.getBlock() == Blocks.FLETCHING_TABLE ||
               state.getBlock() == Blocks.SMITHING_TABLE ||
               state.getBlock() == Blocks.GRINDSTONE ||
               state.getBlock() == Blocks.LOOM ||
               state.getBlock() == Blocks.STONECUTTER;
    }
    
    /**
     * Check if an entity can be damaged in a protected area
     */
    public static boolean canDamageEntity(ServerWorld world, Entity entity, PlayerEntity attacker) {
        BlockPos pos = entity.getBlockPos();
        
        for (ProtectedArea area : protectedAreas.values()) {
            if (area.contains(pos)) {
                // Allow owners to damage entities
                if (area.isOwner(attacker)) {
                    return true;
                }
                
                // Allow damaging hostile mobs in protected areas
                if (entity instanceof net.minecraft.entity.mob.HostileEntity) {
                    return true;
                }
                
                // Protect item frames and minecarts
                if (entity instanceof ItemFrameEntity || entity instanceof AbstractMinecartEntity) {
                    return false;
                }
                
                return false; // Protected
            }
        }
        return true; // Not protected
    }
    
    /**
     * Get all protected areas
     */
    public static Map<UUID, ProtectedArea> getProtectedAreas() {
        return new HashMap<>(protectedAreas);
    }
    
    /**
     * Protected area class
     */
    public static class ProtectedArea {
        public final BlockPos min;
        public final BlockPos max;
        public final String owner;
        public final long createdTime;
        
        public ProtectedArea(BlockPos min, BlockPos max, String owner) {
            this.min = new BlockPos(Math.min(min.getX(), max.getX()), 
                                   Math.min(min.getY(), max.getY()), 
                                   Math.min(min.getZ(), max.getZ()));
            this.max = new BlockPos(Math.max(min.getX(), max.getX()), 
                                   Math.max(min.getY(), max.getY()), 
                                   Math.max(min.getZ(), max.getZ()));
            this.owner = owner;
            this.createdTime = System.currentTimeMillis();
        }
        
        public boolean contains(BlockPos pos) {
            return pos.getX() >= min.getX() && pos.getX() <= max.getX() &&
                   pos.getY() >= min.getY() && pos.getY() <= max.getY() &&
                   pos.getZ() >= min.getZ() && pos.getZ() <= max.getZ();
        }
        
        public boolean isOwner(PlayerEntity player) {
            return player.getName().getString().equals(owner); // Owner only for now
        }
        
        public Box getBoundingBox() {
            return new Box(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ());
        }
    }
}
