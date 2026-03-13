package com.political.backrooms.generator;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.Chunk;
import com.political.backrooms.protection.BackroomsProtectionManager;
import com.political.PoliticalServer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Advanced Backrooms Procedural Generator
 * Based on specific room types and generation patterns
 */
public class AdvancedBackroomsGenerator {
    
    // Room types
    public enum RoomType {
        PITFALLS,           // Medium square room with 9-12 deep holes (appears once)
        MFLUX_DOOR,         // Iron blocks + smooth stone piston door (appears once)
        STANDARD_ROOM,      // Basic yellow room (appears infinitely)
        MFLUX_LAB,          // MFLUX laboratory (appears once)
        MFLUX_OUTPOST,      // MFLUX outpost (appears once)
        END_LIBRARY,        // Library at end of long hall (appears once)
        LONG_HALL,          // Long hallway to library (appears infinitely)
        GIANT_POOLROOM,     // Giant pool with floats (appears once)
        PIPE_HALLWAY,       // Long pipe hallway (appears once)
        GIANT_STAIRCASE,    // Staircase with numbers (appears once)
        LEVEL_RUN,          // Special level run (appears once)
        EXIT_ROOM           // Room with exit beacon (appears rarely)
    }
    
    // Dimension types
    public enum DimensionType {
        MAIN_BACKROOMS,     // Primary backrooms dimension
        ALTERNATE_1,        // First additional dimension
        ALTERNATE_2         // Second additional dimension
    }
    
    // Track spawned unique rooms
    private static final Map<DimensionType, Map<RoomType, Boolean>> spawnedRooms = new HashMap<>();
    
    // Block materials for different dimensions
    private static final Map<DimensionType, RoomMaterials> dimensionMaterials = new HashMap<>();
    
    static {
        // Initialize spawn tracking
        for (DimensionType dim : DimensionType.values()) {
            spawnedRooms.put(dim, new HashMap<>());
            for (RoomType room : RoomType.values()) {
                spawnedRooms.get(dim).put(room, false);
            }
        }
        
        // Setup materials for each dimension
        dimensionMaterials.put(DimensionType.MAIN_BACKROOMS, new RoomMaterials(
            Blocks.YELLOW_CONCRETE,    // Walls
            Blocks.LIGHT_GRAY_CONCRETE, // Floor
            Blocks.YELLOW_CONCRETE,    // Ceiling
            Blocks.SEA_LANTERN,        // Lights
            Blocks.YELLOW_WOOL,        // Decor
            Blocks.IRON_BLOCK          // Special
        ));
        
        dimensionMaterials.put(DimensionType.ALTERNATE_1, new RoomMaterials(
            Blocks.BLUE_CONCRETE,      // Blue theme
            Blocks.LIGHT_BLUE_CONCRETE,
            Blocks.BLUE_CONCRETE,
            Blocks.GLOWSTONE,
            Blocks.BLUE_WOOL,
            Blocks.LAPIS_BLOCK
        ));
        
        dimensionMaterials.put(DimensionType.ALTERNATE_2, new RoomMaterials(
            Blocks.RED_CONCRETE,       // Red theme
            Blocks.ORANGE_CONCRETE,
            Blocks.RED_CONCRETE,
            Blocks.MAGMA_BLOCK,
            Blocks.RED_WOOL,
            Blocks.NETHERITE_BLOCK
        ));
    }
    
    /**
     * Generate backrooms structure at position
     */
    public static void generateBackroomsStructure(ServerWorld world, BlockPos centerPos, DimensionType dimensionType) {
        Random random = Random.create();
        
        // Generate entrance area
        generateEntranceArea(world, centerPos, dimensionType, random);
        
        // Generate main structure pattern
        generateMainStructure(world, centerPos, dimensionType, random);
        
        // Protect the generated area
        protectGeneratedArea(world, centerPos, dimensionType);
        
        PoliticalServer.LOGGER.info("Generated advanced backrooms structure for dimension: " + dimensionType);
    }
    
    /**
     * Generate entrance area with MFLUX door (if not spawned yet)
     */
    private static void generateEntranceArea(ServerWorld world, BlockPos centerPos, DimensionType dimensionType, Random random) {
        RoomMaterials materials = dimensionMaterials.get(dimensionType);
        
        // Check if MFLUX door should spawn (only once)
        if (!spawnedRooms.get(dimensionType).get(RoomType.MFLUX_DOOR)) {
            generateMFLUXDoor(world, centerPos, materials);
            spawnedRooms.get(dimensionType).put(RoomType.MFLUX_DOOR, true);
        } else {
            // Generate standard entrance room
            generateStandardRoom(world, centerPos, materials, 8, 8, 4, random);
        }
    }
    
    /**
     * Generate main structure with various room types
     */
    private static void generateMainStructure(ServerWorld world, BlockPos centerPos, DimensionType dimensionType, Random random) {
        RoomMaterials materials = dimensionMaterials.get(dimensionType);
        int roomCount = 0;
        int maxRooms = 15 + random.nextInt(10); // 15-25 rooms
        
        // Generate pitfalls room (only once)
        if (!spawnedRooms.get(dimensionType).get(RoomType.PITFALLS) && roomCount < maxRooms) {
            BlockPos pitfallsPos = centerPos.add(random.nextInt(20) - 10, 0, random.nextInt(20) - 10);
            generatePitfallsRoom(world, pitfallsPos, materials, random);
            spawnedRooms.get(dimensionType).put(RoomType.PITFALLS, true);
            roomCount++;
        }
        
        // Generate unique rooms (only once each)
        generateUniqueRoom(world, centerPos, dimensionType, RoomType.MFLUX_LAB, materials, random, roomCount, maxRooms);
        generateUniqueRoom(world, centerPos, dimensionType, RoomType.MFLUX_OUTPOST, materials, random, roomCount, maxRooms);
        generateUniqueRoom(world, centerPos, dimensionType, RoomType.GIANT_POOLROOM, materials, random, roomCount, maxRooms);
        generateUniqueRoom(world, centerPos, dimensionType, RoomType.PIPE_HALLWAY, materials, random, roomCount, maxRooms);
        generateUniqueRoom(world, centerPos, dimensionType, RoomType.GIANT_STAIRCASE, materials, random, roomCount, maxRooms);
        generateUniqueRoom(world, centerPos, dimensionType, RoomType.LEVEL_RUN, materials, random, roomCount, maxRooms);
        
        // Generate end library (only once)
        if (!spawnedRooms.get(dimensionType).get(RoomType.END_LIBRARY) && roomCount < maxRooms) {
            BlockPos libraryPos = centerPos.add(random.nextInt(30) - 15, 0, random.nextInt(30) - 15);
            generateEndLibrary(world, libraryPos, materials, random);
            spawnedRooms.get(dimensionType).put(RoomType.END_LIBRARY, true);
            roomCount++;
        }
        
        // Fill remaining space with standard rooms and long halls
        while (roomCount < maxRooms) {
            BlockPos roomPos = centerPos.add(random.nextInt(40) - 20, 0, random.nextInt(40) - 20);
            
            if (random.nextFloat() < 0.3f) {
                // Generate long hall (can appear infinitely)
                generateLongHall(world, roomPos, materials, random);
            } else {
                // Generate standard room (can appear infinitely)
                generateStandardRoom(world, roomPos, materials, 8, 8, 4, random);
            }
            roomCount++;
        }
        
        // Generate exit rooms (appear rarely)
        if (random.nextFloat() < 0.1f) { // 10% chance
            BlockPos exitPos = centerPos.add(random.nextInt(50) - 25, 0, random.nextInt(50) - 25);
            generateExitRoom(world, exitPos, materials, random);
        }
    }
    
    /**
     * Generate MFLUX door entrance
     */
    private static void generateMFLUXDoor(ServerWorld world, BlockPos centerPos, RoomMaterials materials) {
        // Create 6x6 entrance room
        generateStandardRoom(world, centerPos, materials, 6, 6, 4, Random.create());
        
        // Create piston door entrance (iron blocks + smooth stone)
        BlockPos doorPos = centerPos.add(0, 51, 3);
        
        // Iron door frame
        for (int y = 51; y <= 54; y++) {
            world.setBlockState(doorPos.add(-1, y, 0), materials.specialBlock.getDefaultState());
            world.setBlockState(doorPos.add(1, y, 0), materials.specialBlock.getDefaultState());
        }
        
        // Smooth stone walls around door
        for (int x = -2; x <= 2; x++) {
            for (int z = -1; z <= 1; z++) {
                if (Math.abs(x) == 2 || Math.abs(z) == 1) {
                    world.setBlockState(centerPos.add(x, 50, z), Blocks.SMOOTH_STONE.getDefaultState());
                    world.setBlockState(centerPos.add(x, 55, z), Blocks.SMOOTH_STONE.getDefaultState());
                }
            }
        }
        
        // Add MFLUX sign decoration
        world.setBlockState(centerPos.add(0, 52, -2), Blocks.SPRUCE_SIGN.getDefaultState());
    }
    
    /**
     * Generate pitfalls room (medium square with 9-12 deep holes)
     */
    private static void generatePitfallsRoom(ServerWorld world, BlockPos centerPos, RoomMaterials materials, Random random) {
        int roomSize = 10;
        int holeCount = 9 + random.nextInt(4); // 9-12 holes
        
        // Generate room structure
        generateStandardRoom(world, centerPos, materials, roomSize, roomSize, 4, random);
        
        // Create pitfalls
        for (int i = 0; i < holeCount; i++) {
            int holeX = random.nextInt(roomSize - 2) - roomSize/2 + 1;
            int holeZ = random.nextInt(roomSize - 2) - roomSize/2 + 1;
            
            BlockPos holePos = centerPos.add(holeX, 50, holeZ);
            
            // Create deep hole (down to bedrock level)
            for (int y = 50; y > 10; y--) {
                world.setBlockState(holePos.withY(y), Blocks.AIR.getDefaultState());
            }
            
            // Add warning blocks around hole
            world.setBlockState(holePos.add(1, 51, 0), Blocks.ORANGE_WOOL.getDefaultState());
            world.setBlockState(holePos.add(-1, 51, 0), Blocks.ORANGE_WOOL.getDefaultState());
            world.setBlockState(holePos.add(0, 51, 1), Blocks.ORANGE_WOOL.getDefaultState());
            world.setBlockState(holePos.add(0, 51, -1), Blocks.ORANGE_WOOL.getDefaultState());
        }
    }
    
    /**
     * Generate giant poolroom with floats
     */
    private static void generateGiantPoolroom(ServerWorld world, BlockPos centerPos, RoomMaterials materials, Random random) {
        int poolSize = 20;
        
        // Generate large room
        generateStandardRoom(world, centerPos, materials, poolSize, poolSize, 6, random);
        
        // Fill with water
        for (int x = -poolSize/2 + 1; x < poolSize/2; x++) {
            for (int z = -poolSize/2 + 1; z < poolSize/2; z++) {
                BlockPos waterPos = centerPos.add(x, 51, z);
                for (int y = 51; y <= 55; y++) {
                    world.setBlockState(waterPos.withY(y), Blocks.WATER.getDefaultState());
                }
            }
        }
        
        // Add floats (boats)
        int floatCount = 3 + random.nextInt(3);
        for (int i = 0; i < floatCount; i++) {
            int floatX = random.nextInt(poolSize - 4) - poolSize/2 + 2;
            int floatZ = random.nextInt(poolSize - 4) - poolSize/2 + 2;
            
            // Place boat on water surface
            world.setBlockState(centerPos.add(floatX, 56, floatZ), Blocks.OAK_PLANKS.getDefaultState());
        }
        
        // Add diving platforms
        for (int i = 0; i < 2; i++) {
            int platformX = (i == 0) ? -poolSize/2 + 2 : poolSize/2 - 2;
            for (int z = -2; z <= 2; z++) {
                BlockPos platformPos = centerPos.add(platformX, 56, z);
                world.setBlockState(platformPos, Blocks.STONE_SLAB.getDefaultState());
            }
        }
    }
    
    /**
     * Generate long pipe hallway
     */
    private static void generatePipeHallway(ServerWorld world, BlockPos centerPos, RoomMaterials materials, Random random) {
        int hallLength = 20 + random.nextInt(10); // 20-30 blocks long
        int hallWidth = 4;
        int hallHeight = 5;
        
        // Generate hallway structure
        for (int x = 0; x < hallLength; x++) {
            for (int y = 0; y < hallHeight; y++) {
                for (int z = -hallWidth/2; z <= hallWidth/2; z++) {
                    BlockPos pos = centerPos.add(x, 50 + y, z);
                    
                    if (y == 0 || y == hallHeight - 1 || z == -hallWidth/2 || z == hallWidth/2) {
                        // Walls, floor, ceiling
                        world.setBlockState(pos, materials.wallBlock.getDefaultState());
                    } else {
                        // Empty space inside
                        world.setBlockState(pos, Blocks.AIR.getDefaultState());
                    }
                }
            }
            
            // Add pipes along ceiling
            if (x % 3 == 0) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos pipePos = centerPos.add(x, 54, z);
                    world.setBlockState(pipePos, Blocks.IRON_BARS.getDefaultState());
                }
            }
            
            // Add lighting
            if (x % 5 == 0) {
                BlockPos lightPos = centerPos.add(x, 53, 0);
                world.setBlockState(lightPos, materials.lightBlock.getDefaultState());
            }
        }
    }
    
    /**
     * Generate giant staircase with numbers
     */
    private static void generateGiantStaircase(ServerWorld world, BlockPos centerPos, RoomMaterials materials, Random random) {
        int stairHeight = 20;
        int stairWidth = 8;
        
        // Generate staircase structure
        for (int y = 0; y < stairHeight; y++) {
            for (int x = -stairWidth/2; x <= stairWidth/2; x++) {
                for (int z = -stairWidth/2; z <= stairWidth/2; z++) {
                    BlockPos pos = centerPos.add(x, 50 + y, z + y);
                    
                    if (x == -stairWidth/2 || x == stairWidth/2 || z + y == -stairWidth/2 || z + y == stairWidth/2) {
                        // Walls
                        world.setBlockState(pos, materials.wallBlock.getDefaultState());
                    } else if (z + y == -stairWidth/2 + 1) {
                        // Stair step
                        world.setBlockState(pos, materials.floorBlock.getDefaultState());
                    } else {
                        // Empty space
                        world.setBlockState(pos, Blocks.AIR.getDefaultState());
                    }
                }
            }
            
            // Add number signs every 4 steps
            if (y % 4 == 0) {
                BlockPos signPos = centerPos.add(0, 51 + y, y + stairWidth/2 + 1);
                world.setBlockState(signPos, Blocks.OAK_SIGN.getDefaultState());
            }
            
            // Add lighting
            if (y % 3 == 0) {
                BlockPos lightPos = centerPos.add(0, 52 + y, y);
                world.setBlockState(lightPos, materials.lightBlock.getDefaultState());
            }
        }
    }
    
    /**
     * Generate end library with long hall
     */
    private static void generateEndLibrary(ServerWorld world, BlockPos centerPos, RoomMaterials materials, Random random) {
        // Generate long hall leading to library
        generateLongHall(world, centerPos.add(-15, 0, 0), materials, random);
        
        // Generate library room
        generateStandardRoom(world, centerPos, materials, 12, 12, 5, random);
        
        // Add bookshelves
        for (int x = -5; x <= 5; x++) {
            for (int z = -5; z <= 5; z++) {
                if ((x == -5 || x == 5 || z == -5 || z == 5) && (x % 2 == 0 || z % 2 == 0)) {
                    BlockPos shelfPos = centerPos.add(x, 51, z);
                    world.setBlockState(shelfPos, Blocks.BOOKSHELF.getDefaultState());
                }
            }
        }
        
        // Add enchanting table in center
        world.setBlockState(centerPos.add(0, 51, 0), Blocks.ENCHANTING_TABLE.getDefaultState());
        
        // Add special loot chest
        BlockPos chestPos = centerPos.add(0, 52, 2);
        world.setBlockState(chestPos, Blocks.ENDER_CHEST.getDefaultState());
    }
    
    /**
     * Generate standard room
     */
    private static void generateStandardRoom(ServerWorld world, BlockPos centerPos, RoomMaterials materials, int sizeX, int sizeZ, int height, Random random) {
        for (int x = -sizeX/2; x <= sizeX/2; x++) {
            for (int z = -sizeZ/2; z <= sizeZ/2; z++) {
                for (int y = 0; y < height; y++) {
                    BlockPos pos = centerPos.add(x, 50 + y, z);
                    
                    if (y == 0) {
                        // Floor
                        world.setBlockState(pos, materials.floorBlock.getDefaultState());
                    } else if (y == height - 1) {
                        // Ceiling
                        world.setBlockState(pos, materials.ceilingBlock.getDefaultState());
                        
                        // Add lights on ceiling
                        if (x % 4 == 0 && z % 4 == 0) {
                            world.setBlockState(pos, materials.lightBlock.getDefaultState());
                        }
                    } else if (x == -sizeX/2 || x == sizeX/2 || z == -sizeZ/2 || z == sizeZ/2) {
                        // Walls
                        world.setBlockState(pos, materials.wallBlock.getDefaultState());
                    } else {
                        // Empty space
                        world.setBlockState(pos, Blocks.AIR.getDefaultState());
                    }
                }
            }
        }
        
        // Add random decorations
        if (random.nextFloat() < 0.3f) {
            BlockPos decorPos = centerPos.add(random.nextInt(sizeX/2 - 1) - sizeX/4, 51, random.nextInt(sizeZ/2 - 1) - sizeZ/4);
            world.setBlockState(decorPos, materials.decorBlock.getDefaultState());
        }
    }
    
    /**
     * Generate long hall
     */
    private static void generateLongHall(ServerWorld world, BlockPos centerPos, RoomMaterials materials, Random random) {
        int hallLength = 15 + random.nextInt(10);
        int hallWidth = 3;
        
        for (int x = 0; x < hallLength; x++) {
            for (int y = 0; y < 4; y++) {
                for (int z = -hallWidth/2; z <= hallWidth/2; z++) {
                    BlockPos pos = centerPos.add(x, 50 + y, z);
                    
                    if (y == 0 || y == 3 || z == -hallWidth/2 || z == hallWidth/2) {
                        world.setBlockState(pos, materials.wallBlock.getDefaultState());
                    } else {
                        world.setBlockState(pos, Blocks.AIR.getDefaultState());
                    }
                }
            }
            
            // Add lights
            if (x % 4 == 0) {
                BlockPos lightPos = centerPos.add(x, 52, 0);
                world.setBlockState(lightPos, materials.lightBlock.getDefaultState());
            }
        }
    }
    
    /**
     * Generate exit room with beacon
     */
    private static void generateExitRoom(ServerWorld world, BlockPos centerPos, RoomMaterials materials, Random random) {
        generateStandardRoom(world, centerPos, materials, 8, 8, 5, random);
        
        // Create exit beacon pillar
        for (int y = 51; y < 68; y++) {
            BlockPos pillarPos = centerPos.withY(y);
            world.setBlockState(pillarPos, materials.specialBlock.getDefaultState());
        }
        
        // Place beacon on top
        world.setBlockState(centerPos.withY(68), Blocks.BEACON.getDefaultState());
        
        // Add glowstone around beacon
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x != 0 || z != 0) {
                    BlockPos glowPos = centerPos.add(x, 68, z);
                    world.setBlockState(glowPos, Blocks.GLOWSTONE.getDefaultState());
                }
            }
        }
    }
    
    /**
     * Generate unique room (only once per dimension)
     */
    private static void generateUniqueRoom(ServerWorld world, BlockPos centerPos, DimensionType dimensionType, RoomType roomType, RoomMaterials materials, Random random, int currentRoomCount, int maxRooms) {
        if (!spawnedRooms.get(dimensionType).get(roomType) && currentRoomCount < maxRooms) {
            BlockPos roomPos = centerPos.add(random.nextInt(30) - 15, 0, random.nextInt(30) - 15);
            
            switch (roomType) {
                case MFLUX_LAB:
                    generateMFLUXLab(world, roomPos, materials, random);
                    break;
                case MFLUX_OUTPOST:
                    generateMFLUXOutpost(world, roomPos, materials, random);
                    break;
                case GIANT_POOLROOM:
                    generateGiantPoolroom(world, roomPos, materials, random);
                    break;
                case PIPE_HALLWAY:
                    generatePipeHallway(world, roomPos, materials, random);
                    break;
                case GIANT_STAIRCASE:
                    generateGiantStaircase(world, roomPos, materials, random);
                    break;
                case LEVEL_RUN:
                    generateLevelRun(world, roomPos, materials, random);
                    break;
                case STANDARD_ROOM:
                case LONG_HALL:
                case PITFALLS:
                case MFLUX_DOOR:
                case END_LIBRARY:
                case EXIT_ROOM:
                    // These are handled separately
                    break;
            }
            
            spawnedRooms.get(dimensionType).put(roomType, true);
        }
    }
    
    /**
     * Generate MFLUX laboratory
     */
    private static void generateMFLUXLab(ServerWorld world, BlockPos centerPos, RoomMaterials materials, Random random) {
        generateStandardRoom(world, centerPos, materials, 12, 12, 5, random);
        
        // Add laboratory equipment
        for (int x = -4; x <= 4; x += 3) {
            for (int z = -4; z <= 4; z += 3) {
                BlockPos equipPos = centerPos.add(x, 51, z);
                world.setBlockState(equipPos, Blocks.BREWING_STAND.getDefaultState());
            }
        }
        
        // Add center experiment table
        world.setBlockState(centerPos.add(0, 51, 0), Blocks.ENCHANTING_TABLE.getDefaultState());
    }
    
    /**
     * Generate MFLUX outpost
     */
    private static void generateMFLUXOutpost(ServerWorld world, BlockPos centerPos, RoomMaterials materials, Random random) {
        generateStandardRoom(world, centerPos, materials, 10, 10, 5, random);
        
        // Add outpost equipment
        world.setBlockState(centerPos.add(0, 51, 0), Blocks.CRAFTING_TABLE.getDefaultState());
        world.setBlockState(centerPos.add(1, 51, 0), Blocks.FURNACE.getDefaultState());
        world.setBlockState(centerPos.add(-1, 51, 0), Blocks.CHEST.getDefaultState());
        
        // Add beds
        for (int x = -3; x <= 3; x += 3) {
            BlockPos bedPos = centerPos.add(x, 51, 3);
            world.setBlockState(bedPos, Blocks.RED_BED.getDefaultState());
        }
    }
    
    /**
     * Generate Level Run special area
     */
    private static void generateLevelRun(ServerWorld world, BlockPos centerPos, RoomMaterials materials, Random random) {
        // Create obstacle course
        for (int i = 0; i < 10; i++) {
            BlockPos roomPos = centerPos.add(i * 8, 0, 0);
            generateStandardRoom(world, roomPos, materials, 6, 6, 4, random);
            
            // Add obstacles
            if (i % 2 == 0) {
                // Add parkour blocks
                for (int j = 1; j < 4; j++) {
                    BlockPos jumpPos = roomPos.add(j, 52 + j/2, 0);
                    world.setBlockState(jumpPos, materials.floorBlock.getDefaultState());
                }
            }
        }
    }
    
    /**
     * Generate room chunk for chunk generator integration
     * FIXED: Now generates at correct Y levels matching BackroomsChunkGenerator (8 levels, 6 blocks each)
     */
    public static void generateRoomChunk(Chunk chunk, int chunkX, int chunkZ, int startX, int startZ, BlockPos.Mutable mpos) {
        // Generate all 8 levels matching BackroomsChunkGenerator's LEVEL_SPAN=6, NUM_LEVELS=8
        for (int level = 0; level < 8; level++) {
            int baseY = level * 6; // LEVEL_SPAN = 6
            generateLevelForChunk(chunk, chunkX, chunkZ, startX, startZ, mpos, level, baseY);
        }
    }
    
    /**
     * Generate a single level for chunk generation
     */
    private static void generateLevelForChunk(Chunk chunk, int chunkX, int chunkZ, int startX, int startZ, 
                                               BlockPos.Mutable mpos, int level, int baseY) {
        // Get dimension type based on chunk position
        DimensionType dimensionType = getDimensionTypeForChunk(chunkX, chunkZ);
        RoomMaterials materials = dimensionMaterials.get(dimensionType);
        
        // Use deterministic random based on chunk coordinates for consistency
        long seed = chunkX * 1234567L + chunkZ * 7654321L + level * 0xFEED1337L;
        java.util.Random random = new java.util.Random(seed);
        
        // Determine maze connectivity (matching BackroomsChunkGenerator logic)
        boolean north = (seed & 1L) == 1L;
        boolean south = ((seed + 1) & 1L) == 1L;
        boolean west = ((seed + 2) & 1L) == 1L;
        boolean east = ((seed + 3) & 1L) == 1L;
        
        // Room type based on chunk position
        int roomType = (int) Math.abs(seed % 5); // 5 room types
        
        // Generate floor and ceiling
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                // Floor at baseY
                chunk.setBlockState(mpos.set(startX + x, baseY, startZ + z), materials.floorBlock.getDefaultState());
                
                // Ceiling at baseY+5
                BlockState ceilingBlock = (x % 4 == 0 && z % 4 == 0) ? materials.lightBlock.getDefaultState() : materials.ceilingBlock.getDefaultState();
                chunk.setBlockState(mpos.set(startX + x, baseY + 5, startZ + z), ceilingBlock);
            }
        }
        
        // Generate perimeter walls with doorways
        int gateMin = 6, gateMax = 8;
        for (int x = 0; x < 16; x++) {
            for (int y = 1; y <= 4; y++) {
                // North wall with doorway
                if (!(north && x >= gateMin && x <= gateMax)) {
                    chunk.setBlockState(mpos.set(startX + x, baseY + y, startZ), materials.wallBlock.getDefaultState());
                }
                // South wall with doorway
                if (!(south && x >= gateMin && x <= gateMax)) {
                    chunk.setBlockState(mpos.set(startX + x, baseY + y, startZ + 15), materials.wallBlock.getDefaultState());
                }
            }
        }
        
        for (int z = 1; z < 15; z++) {
            for (int y = 1; y <= 4; y++) {
                // West wall with doorway
                if (!(west && z >= gateMin && z <= gateMax)) {
                    chunk.setBlockState(mpos.set(startX, baseY + y, startZ + z), materials.wallBlock.getDefaultState());
                }
                // East wall with doorway
                if (!(east && z >= gateMin && z <= gateMax)) {
                    chunk.setBlockState(mpos.set(startX + 15, baseY + y, startZ + z), materials.wallBlock.getDefaultState());
                }
            }
        }
        
        // Generate interior features based on room type
        generateInteriorFeatures(chunk, startX, startZ, mpos, baseY, roomType, materials, random);
        
        // Generate vertical shaft if needed (1 in 5 chance per level)
        if (level < 7 && Math.abs(seed) % 5 == 0) {
            generateVerticalShaft(chunk, startX, startZ, mpos, level, baseY, materials);
        }
    }
    
    /**
     * Generate interior features for a room
     */
    private static void generateInteriorFeatures(Chunk chunk, int startX, int startZ, BlockPos.Mutable mpos, 
                                                  int baseY, int roomType, RoomMaterials materials, java.util.Random random) {
        switch (roomType) {
            case 0: // OPEN - No interior features
                break;
                
            case 1: // PILLARS - Pillars every 4 blocks
                for (int x = 4; x < 12; x += 4) {
                    for (int z = 4; z < 12; z += 4) {
                        // Skip center shaft position
                        if (x == 7 && z == 7) continue;
                        for (int y = 1; y <= 4; y++) {
                            chunk.setBlockState(mpos.set(startX + x, baseY + y, startZ + z), materials.wallBlock.getDefaultState());
                        }
                    }
                }
                break;
                
            case 2: // NARROW - Central corridor
                for (int x = 0; x < 6; x++) {
                    for (int z = 0; z < 16; z++) {
                        for (int y = 1; y <= 4; y++) {
                            chunk.setBlockState(mpos.set(startX + x, baseY + y, startZ + z), materials.wallBlock.getDefaultState());
                        }
                    }
                }
                for (int x = 10; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        for (int y = 1; y <= 4; y++) {
                            chunk.setBlockState(mpos.set(startX + x, baseY + y, startZ + z), materials.wallBlock.getDefaultState());
                        }
                    }
                }
                break;
                
            case 3: // OFFICE - T-shaped partition
                // Vertical part
                for (int z = 2; z <= 13; z++) {
                    for (int y = 1; y <= 4; y++) {
                        chunk.setBlockState(mpos.set(startX + 8, baseY + y, startZ + z), materials.wallBlock.getDefaultState());
                    }
                }
                // Horizontal part
                for (int x = 2; x <= 6; x++) {
                    for (int y = 1; y <= 4; y++) {
                        chunk.setBlockState(mpos.set(startX + x, baseY + y, startZ + 8), materials.wallBlock.getDefaultState());
                    }
                }
                break;
                
            case 4: // ALCOVE - Two stub walls
                for (int y = 1; y <= 4; y++) {
                    // First alcove
                    for (int z = 3; z <= 6; z++) {
                        chunk.setBlockState(mpos.set(startX + 4, baseY + y, startZ + z), materials.wallBlock.getDefaultState());
                    }
                    // Second alcove
                    for (int z = 9; z <= 12; z++) {
                        chunk.setBlockState(mpos.set(startX + 11, baseY + y, startZ + z), materials.wallBlock.getDefaultState());
                    }
                }
                break;
        }
        
        // Add decorations
        for (int i = 0; i < 2; i++) {
            int decorX = 2 + random.nextInt(12);
            int decorZ = 2 + random.nextInt(12);
            if (materials.decorBlock != null && random.nextFloat() < 0.3f) {
                chunk.setBlockState(mpos.set(startX + decorX, baseY + 1, startZ + decorZ), materials.decorBlock.getDefaultState());
            }
        }
    }
    
    /**
     * Generate vertical shaft between levels
     */
    private static void generateVerticalShaft(Chunk chunk, int startX, int startZ, BlockPos.Mutable mpos, 
                                               int level, int baseY, RoomMaterials materials) {
        int shaftX = startX + 7;
        int shaftZ = startZ + 7;
        
        // Remove ceiling of current level
        chunk.setBlockState(mpos.set(shaftX, baseY + 5, shaftZ), net.minecraft.block.Blocks.AIR.getDefaultState());
        
        // Create ladder
        for (int y = 1; y <= 5; y++) {
            // Ladder backing
            chunk.setBlockState(mpos.set(shaftX, baseY + y, shaftZ + 1), materials.wallBlock.getDefaultState());
            // Ladder
            chunk.setBlockState(mpos.set(shaftX, baseY + y, shaftZ), 
                net.minecraft.block.Blocks.LADDER.getDefaultState()
                    .with(net.minecraft.block.LadderBlock.FACING, net.minecraft.util.math.Direction.NORTH));
        }
    }
    
    /**
     * Get dimension type for chunk position
     */
    private static DimensionType getDimensionTypeForChunk(int chunkX, int chunkZ) {
        // Use chunk coordinates to determine dimension type
        int hash = Math.abs(chunkX * 31 + chunkZ);
        return DimensionType.values()[hash % DimensionType.values().length];
    }
    
    /**
     * Protect generated area
     */
    private static void protectGeneratedArea(ServerWorld world, BlockPos centerPos, DimensionType dimensionType) {
        // Protect a 100x100 area around the generated structure
        BlockPos min = centerPos.add(-50, 0, -50);
        BlockPos max = centerPos.add(50, 100, 50);
        
        UUID protectionId = UUID.randomUUID();
        BackroomsProtectionManager.protectArea(protectionId, min, max, "backrooms_system");
    }
    
    /**
     * Room materials class
     */
    private static class RoomMaterials {
        final Block wallBlock;
        final Block floorBlock;
        final Block ceilingBlock;
        final Block lightBlock;
        final Block decorBlock;
        final Block specialBlock;
        
        RoomMaterials(Block wallBlock, Block floorBlock, Block ceilingBlock, Block lightBlock, Block decorBlock, Block specialBlock) {
            this.wallBlock = wallBlock;
            this.floorBlock = floorBlock;
            this.ceilingBlock = ceilingBlock;
            this.lightBlock = lightBlock;
            this.decorBlock = decorBlock;
            this.specialBlock = specialBlock;
        }
    }
}
