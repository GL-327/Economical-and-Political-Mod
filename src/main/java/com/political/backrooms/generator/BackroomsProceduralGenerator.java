package com.political.backrooms.generator;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.chunk.Chunk;

/**
 * Procedural Backrooms Generator - Creates endless yellow rooms with fluorescent lights
 */
public class BackroomsProceduralGenerator {
    
    private static final Block WALL_BLOCK = Blocks.YELLOW_CONCRETE;
    private static final Block FLOOR_BLOCK = Blocks.LIGHT_GRAY_CONCRETE;
    private static final Block CEILING_BLOCK = Blocks.YELLOW_CONCRETE;
    private static final Block LIGHT_BLOCK = Blocks.SEA_LANTERN;
    private static final Block CARPET_BLOCK = Blocks.YELLOW_CARPET;
    
    /**
     * Generate backrooms structure for a chunk
     */
    public static void generateBackroomsChunk(Chunk chunk, Random random) {
        int chunkX = chunk.getPos().x;
        int chunkZ = chunk.getPos().z;
        
        // Generate room structure for this chunk
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = chunkX * 16 + x;
                int worldZ = chunkZ * 16 + z;
                
                // Generate room pattern
                generateRoomColumn(chunk, worldX, worldZ, random);
            }
        }
    }
    
    private static void generateRoomColumn(Chunk chunk, int worldX, int worldZ, Random random) {
        // Room dimensions: 8x8 rooms with 2-block thick walls
        int roomX = Math.floorMod(worldX, 10);
        int roomZ = Math.floorMod(worldZ, 10);
        
        boolean isWall = roomX < 2 || roomX >= 8 || roomZ < 2 || roomZ >= 8;
        
        for (int y = 50; y < 70; y++) {
            BlockPos pos = new BlockPos(worldX, y, worldZ);
            
            if (y == 50) {
                // Floor
                chunk.setBlockState(pos, FLOOR_BLOCK.getDefaultState());
            } else if (y == 69) {
                // Ceiling
                chunk.setBlockState(pos, CEILING_BLOCK.getDefaultState());
                
                // Add fluorescent lights on ceiling
                if (!isWall && (roomX == 5 || roomZ == 5)) {
                    chunk.setBlockState(pos, LIGHT_BLOCK.getDefaultState());
                }
            } else if (isWall) {
                // Walls
                chunk.setBlockState(pos, WALL_BLOCK.getDefaultState());
            } else {
                // Empty space inside rooms
                chunk.setBlockState(pos, Blocks.AIR.getDefaultState());
            }
        }
        
        // Add random carpet patches
        if (!isWall && random.nextInt(20) == 0) {
            BlockPos carpetPos = new BlockPos(worldX, 51, worldZ);
            chunk.setBlockState(carpetPos, CARPET_BLOCK.getDefaultState());
        }
    }
    
    /**
     * Generate exit beacon structure at specific coordinates
     */
    public static void generateExitBeacon(StructureWorldAccess world, BlockPos centerPos) {
        // Generate a pillar structure with beacon on top
        for (int y = 50; y < 68; y++) {
            BlockPos pillarPos = centerPos.withY(y);
            world.setBlockState(pillarPos, Blocks.IRON_BLOCK.getDefaultState(), 3);
        }
        
        // Place beacon at top
        BlockPos beaconPos = centerPos.withY(68);
        world.setBlockState(beaconPos, Blocks.BEACON.getDefaultState(), 3);
        
        // Add glass around beacon
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) continue; // Skip beacon position
                BlockPos glassPos = centerPos.add(x, 68, z);
                world.setBlockState(glassPos, Blocks.GLASS.getDefaultState(), 3);
            }
        }
    }
    
    /**
     * Check if a position is near an exit beacon
     */
    public static boolean isNearExitBeacon(BlockPos pos) {
        // Simple check - if player is at Y=68 or higher and near iron blocks
        if (pos.getY() >= 68) {
            return true; // Simplified for simulation
        }
        return false;
    }
}
