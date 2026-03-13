package com.political.backrooms.fracture3;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryFixedCodec;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.FixedBiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.VerticalBlockSample;
import net.minecraft.world.gen.noise.NoiseConfig;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

/**
 * Fracture 3 - Backrooms Dimension Chunk Generator
 * 
 * Generates a void-based dimension with procedural room generation.
 * - Y=64: structural floor level
 * - Y=79: ceiling level
 * - Y=80+ and Y<64: infinite void
 * 
 * Room types with weights:
 * - Standard Room: 45
 * - Long Corridor: 30
 * - Infinite-Impression Room: 15
 * - Corner Accumulation Room: 7
 * - Void-Edge Room: 3
 */
public class Fracture3ChunkGenerator extends ChunkGenerator {

    public static final MapCodec<Fracture3ChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            RegistryFixedCodec.of(RegistryKeys.BIOME).fieldOf("biome").forGetter(gen -> gen.biome)
        ).apply(instance, Fracture3ChunkGenerator::new)
    );

    // Structural constants
    public static final int FLOOR_Y = 64;
    public static final int CEILING_Y = 79;
    public static final int PLAYABLE_HEIGHT = 16;
    
    // Room type weights (total 100)
    private static final int WEIGHT_STANDARD = 45;
    private static final int WEIGHT_CORRIDOR = 30;
    private static final int WEIGHT_INFINITE = 15;
    private static final int WEIGHT_CORNER = 7;
    private static final int WEIGHT_VOID_EDGE = 3;

    private final net.minecraft.registry.entry.RegistryEntry<Biome> biome;

    public Fracture3ChunkGenerator(net.minecraft.registry.entry.RegistryEntry<Biome> biome) {
        super(new FixedBiomeSource(biome));
        this.biome = biome;
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> getCodec() {
        return CODEC;
    }

    @Override
    public int getSpawnHeight(net.minecraft.world.HeightLimitView world) {
        return FLOOR_Y + 1;
    }

    @Override
    public CompletableFuture<Chunk> populateNoise(Blender blender, NoiseConfig noiseConfig,
                                                   StructureAccessor structureAccessor, Chunk chunk) {
        int cx = chunk.getPos().x;
        int cz = chunk.getPos().z;
        int startX = chunk.getPos().getStartX();
        int startZ = chunk.getPos().getStartZ();

        BlockPos.Mutable mpos = new BlockPos.Mutable();
        Random random = new Random(cx * 341873128712L + cz * 132897987541L);

        // Determine room type based on chunk position
        RoomType roomType = getRoomType(cx, cz, random);

        // Generate the room
        generateRoom(chunk, cx, cz, startX, startZ, mpos, random, roomType);

        return CompletableFuture.completedFuture(chunk);
    }

    private enum RoomType {
        STANDARD,
        CORRIDOR,
        INFINITE_IMPRESSION,
        CORNER_ACCUMULATION,
        VOID_EDGE
    }

    private RoomType getRoomType(int cx, int cz, Random random) {
        // Origin chunk is always a standard room (entry point)
        if (cx == 0 && cz == 0) {
            return RoomType.STANDARD;
        }

        int roll = random.nextInt(100);
        if (roll < WEIGHT_STANDARD) return RoomType.STANDARD;
        if (roll < WEIGHT_STANDARD + WEIGHT_CORRIDOR) return RoomType.CORRIDOR;
        if (roll < WEIGHT_STANDARD + WEIGHT_CORRIDOR + WEIGHT_INFINITE) return RoomType.INFINITE_IMPRESSION;
        if (roll < WEIGHT_STANDARD + WEIGHT_CORRIDOR + WEIGHT_INFINITE + WEIGHT_CORNER) return RoomType.CORNER_ACCUMULATION;
        return RoomType.VOID_EDGE;
    }

    private void generateRoom(Chunk chunk, int cx, int cz, int startX, int startZ, 
                              BlockPos.Mutable mpos, Random random, RoomType roomType) {
        
        // Generate floor
        generateFloor(chunk, startX, startZ, mpos, random, roomType);
        
        // Generate ceiling
        generateCeiling(chunk, startX, startZ, mpos, random, roomType);
        
        // Generate walls (perimeter)
        generateWalls(chunk, startX, startZ, mpos, random, roomType);
        
        // Generate interior features based on room type
        switch (roomType) {
            case STANDARD -> generateStandardRoomInterior(chunk, startX, startZ, mpos, random);
            case CORRIDOR -> generateCorridorInterior(chunk, startX, startZ, mpos, random);
            case INFINITE_IMPRESSION -> generateInfiniteImpressionInterior(chunk, startX, startZ, mpos, random);
            case CORNER_ACCUMULATION -> generateCornerAccumulationInterior(chunk, startX, startZ, mpos, random);
            case VOID_EDGE -> generateVoidEdgeInterior(chunk, startX, startZ, mpos, random);
        }

        // Generate doorways for corridor connections
        generateDoorways(chunk, cx, cz, startX, startZ, mpos, random);
    }

    private void generateFloor(Chunk chunk, int startX, int startZ, BlockPos.Mutable mpos, 
                               Random random, RoomType roomType) {
        // Floor holes rate
        double holeRate = roomType == RoomType.VOID_EDGE ? 0.10 : 0.05;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int wx = startX + x;
                int wz = startZ + z;

                // Check for floor hole (void peek)
                if (random.nextDouble() < holeRate) {
                    // Air gap - void peek
                    chunk.setBlockState(mpos.set(wx, FLOOR_Y, wz), Blocks.AIR.getDefaultState());
                } else {
                    // Base floor: smooth_sandstone
                    chunk.setBlockState(mpos.set(wx, FLOOR_Y, wz), Blocks.SMOOTH_SANDSTONE.getDefaultState());

                    // Grout lines every 4 blocks
                    if (x % 4 == 0 || z % 4 == 0) {
                        chunk.setBlockState(mpos.set(wx, FLOOR_Y, wz), Blocks.SANDSTONE_SLAB.getDefaultState());
                    }

                    // Carpet coverage (55-65% for standard)
                    if (roomType != RoomType.VOID_EDGE && random.nextDouble() < 0.60) {
                        // Yellow carpet (primary) or brown carpet (moisture areas)
                        BlockState carpet = random.nextDouble() < 0.85 
                            ? Blocks.YELLOW_CARPET.getDefaultState()
                            : Blocks.BROWN_CARPET.getDefaultState();
                        chunk.setBlockState(mpos.set(wx, FLOOR_Y + 1, wz), carpet);
                    }
                }
            }
        }
    }

    private void generateCeiling(Chunk chunk, int startX, int startZ, BlockPos.Mutable mpos,
                                 Random random, RoomType roomType) {
        // Dead light rate (higher for infinite-impression rooms)
        double deadLightRate = roomType == RoomType.INFINITE_IMPRESSION ? 0.25 : 0.15;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int wx = startX + x;
                int wz = startZ + z;

                // Chiseled sandstone tiles in 2x2 grids
                if ((x / 2) % 2 == 0 && (z / 2) % 2 == 0) {
                    chunk.setBlockState(mpos.set(wx, CEILING_Y, wz), Blocks.CHISELED_SANDSTONE.getDefaultState());
                } else {
                    chunk.setBlockState(mpos.set(wx, CEILING_Y, wz), Blocks.SMOOTH_SANDSTONE.getDefaultState());
                }

                // Light sources every 6 blocks
                if (x % 6 == 0 && z % 6 == 0) {
                    if (random.nextDouble() < deadLightRate) {
                        // No light - dark patch
                        chunk.setBlockState(mpos.set(wx, CEILING_Y, wz), Blocks.AIR.getDefaultState());
                    } else {
                        // Sea lantern
                        chunk.setBlockState(mpos.set(wx, CEILING_Y, wz), Blocks.SEA_LANTERN.getDefaultState());
                    }
                }
            }
        }

        // Add one flickering redstone lamp per room
        int lampX = startX + 4 + random.nextInt(8);
        int lampZ = startZ + 4 + random.nextInt(8);
        chunk.setBlockState(mpos.set(lampX, CEILING_Y, lampZ), Blocks.REDSTONE_LAMP.getDefaultState());
    }

    private void generateWalls(Chunk chunk, int startX, int startZ, BlockPos.Mutable mpos,
                               Random random, RoomType roomType) {
        
        // Determine which walls are void-edge (for VOID_EDGE room type)
        boolean northVoid = false, southVoid = false, eastVoid = false, westVoid = false;
        if (roomType == RoomType.VOID_EDGE) {
            int voidWalls = 1 + random.nextInt(2); // 1 or 2 void walls
            for (int i = 0; i < voidWalls; i++) {
                int dir = random.nextInt(4);
                switch (dir) {
                    case 0 -> northVoid = true;
                    case 1 -> southVoid = true;
                    case 2 -> eastVoid = true;
                    case 3 -> westVoid = true;
                }
            }
        }

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int wx = startX + x;
                int wz = startZ + z;

                // Check if this is a perimeter wall position
                boolean isPerimeter = (x == 0) || (x == 15) || (z == 0) || (z == 15);
                if (!isPerimeter) continue;

                // Check if this wall should be void-edge
                boolean isVoidEdge = (x == 0 && westVoid) || (x == 15 && eastVoid) 
                                  || (z == 0 && northVoid) || (z == 15 && southVoid);

                for (int y = FLOOR_Y + 1; y < CEILING_Y; y++) {
                    if (isVoidEdge) {
                        // Void edge: black_concrete border, then void
                        if (x == 0 && westVoid) {
                            chunk.setBlockState(mpos.set(wx, y, wz), Blocks.BLACK_CONCRETE.getDefaultState());
                        } else if (x == 15 && eastVoid) {
                            chunk.setBlockState(mpos.set(wx, y, wz), Blocks.BLACK_CONCRETE.getDefaultState());
                        } else if (z == 0 && northVoid) {
                            chunk.setBlockState(mpos.set(wx, y, wz), Blocks.BLACK_CONCRETE.getDefaultState());
                        } else if (z == 15 && southVoid) {
                            chunk.setBlockState(mpos.set(wx, y, wz), Blocks.BLACK_CONCRETE.getDefaultState());
                        }
                    } else {
                        // Standard wall: yellow_concrete
                        chunk.setBlockState(mpos.set(wx, y, wz), Blocks.YELLOW_CONCRETE.getDefaultState());

                        // Seam strips every 8 blocks
                        if (x % 8 == 0 || z % 8 == 0) {
                            chunk.setBlockState(mpos.set(wx, y, wz), Blocks.YELLOW_GLAZED_TERRACOTTA.getDefaultState());
                        }

                        // Baseboard at Y=64
                        if (y == FLOOR_Y + 1) {
                            chunk.setBlockState(mpos.set(wx, y, wz), Blocks.SMOOTH_SANDSTONE_SLAB.getDefaultState());
                        }

                        // Crown moulding at Y=78
                        if (y == CEILING_Y - 1) {
                            chunk.setBlockState(mpos.set(wx, y, wz), Blocks.SMOOTH_SANDSTONE_SLAB.getDefaultState());
                        }

                        // Moisture stains (brown stained glass pane)
                        if (random.nextDouble() < 0.03 && y >= FLOOR_Y + 2 && y <= FLOOR_Y + 4) {
                            chunk.setBlockState(mpos.set(wx, y, wz), Blocks.BROWN_STAINED_GLASS_PANE.getDefaultState());
                        }
                    }
                }
            }
        }
    }

    private void generateStandardRoomInterior(Chunk chunk, int startX, int startZ, 
                                              BlockPos.Mutable mpos, Random random) {
        // Occasional pillars
        if (random.nextDouble() < 0.3) {
            int pillarX = startX + 4 + random.nextInt(8);
            int pillarZ = startZ + 4 + random.nextInt(8);
            for (int y = FLOOR_Y + 1; y < CEILING_Y; y++) {
                chunk.setBlockState(mpos.set(pillarX, y, pillarZ), Blocks.YELLOW_CONCRETE.getDefaultState());
            }
        }
    }

    private void generateCorridorInterior(Chunk chunk, int startX, int startZ,
                                          BlockPos.Mutable mpos, Random random) {
        // Corridors are simpler - mostly open
        // High carpet coverage (80%)
        for (int x = 2; x < 14; x++) {
            for (int z = 2; z < 14; z++) {
                if (random.nextDouble() < 0.80) {
                    chunk.setBlockState(mpos.set(startX + x, FLOOR_Y + 1, startZ + z), 
                        Blocks.YELLOW_CARPET.getDefaultState());
                }
            }
        }
    }

    private void generateInfiniteImpressionInterior(Chunk chunk, int startX, int startZ,
                                                    BlockPos.Mutable mpos, Random random) {
        // Columns every 8 blocks
        for (int x = 0; x < 16; x += 8) {
            for (int z = 0; z < 16; z += 8) {
                for (int y = FLOOR_Y + 1; y < CEILING_Y; y++) {
                    chunk.setBlockState(mpos.set(startX + x, y, startZ + z), 
                        Blocks.YELLOW_CONCRETE.getDefaultState());
                }
            }
        }

        // Alcoves at midpoints of walls (1-block deep, 2-wide, full height)
        // Back wall of alcove: black_concrete
        generateAlcove(chunk, startX, startZ, mpos, 7, 0, true);  // North wall
        generateAlcove(chunk, startX, startZ, mpos, 7, 15, true); // South wall
        generateAlcove(chunk, startX, startZ, mpos, 0, 7, false); // West wall
        generateAlcove(chunk, startX, startZ, mpos, 15, 7, false); // East wall
    }

    private void generateAlcove(Chunk chunk, int startX, int startZ, BlockPos.Mutable mpos,
                                int baseX, int baseZ, boolean northSouth) {
        // Create a 2-wide, full-height alcove
        for (int offset = -1; offset <= 0; offset++) {
            int wx = northSouth ? startX + baseX + offset : startX + baseX;
            int wz = northSouth ? startZ + baseZ : startZ + baseZ + offset;
            
            // Carve out the alcove (air)
            for (int y = FLOOR_Y + 1; y < CEILING_Y; y++) {
                chunk.setBlockState(mpos.set(wx, y, wz), Blocks.AIR.getDefaultState());
            }
            
            // Back wall: black_concrete (1 block behind)
            int backX = northSouth ? wx : (baseX == 0 ? wx - 1 : wx + 1);
            int backZ = northSouth ? (baseZ == 0 ? wz - 1 : wz + 1) : wz;
            for (int y = FLOOR_Y + 1; y < CEILING_Y; y++) {
                chunk.setBlockState(mpos.set(backX, y, backZ), Blocks.BLACK_CONCRETE.getDefaultState());
            }
        }
    }

    private void generateCornerAccumulationInterior(Chunk chunk, int startX, int startZ,
                                                    BlockPos.Mutable mpos, Random random) {
        // Water puddle in at least one corner
        int cornerX = random.nextBoolean() ? 2 : 13;
        int cornerZ = random.nextBoolean() ? 2 : 13;
        
        // Carve depression and fill with water
        chunk.setBlockState(mpos.set(startX + cornerX, FLOOR_Y - 1, startZ + cornerZ), 
            Blocks.WATER.getDefaultState());
        
        // Surround with brown carpet
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;
                chunk.setBlockState(mpos.set(startX + cornerX + dx, FLOOR_Y + 1, startZ + cornerZ + dz),
                    Blocks.BROWN_CARPET.getDefaultState());
            }
        }

        // Note block buried at Y=62 for floor thump
        chunk.setBlockState(mpos.set(startX + 8, FLOOR_Y - 2, startZ + 8), Blocks.NOTE_BLOCK.getDefaultState());
    }

    private void generateVoidEdgeInterior(Chunk chunk, int startX, int startZ,
                                          BlockPos.Mutable mpos, Random random) {
        // Void-edge rooms have no carpet, bare floor
        // All lights functional (bright contrast with void)
        for (int x = 0; x < 16; x += 6) {
            for (int z = 0; z < 16; z += 6) {
                chunk.setBlockState(mpos.set(startX + x, CEILING_Y, startZ + z), 
                    Blocks.SEA_LANTERN.getDefaultState());
            }
        }
    }

    private void generateDoorways(Chunk chunk, int cx, int cz, int startX, int startZ,
                                  BlockPos.Mutable mpos, Random random) {
        // Generate 1-4 exits based on random
        int numExits = 1 + random.nextInt(4);
        
        for (int i = 0; i < numExits; i++) {
            int direction = random.nextInt(4);
            
            // Doorway position on wall
            int doorwayX = 7;
            int doorwayZ = switch (direction) {
                case 0 -> 0;     // North
                case 1 -> 15;    // South
                case 2 -> 0;     // West (use doorwayX for wall position)
                case 3 -> 15;    // East
                default -> 0;
            };
            
            if (direction >= 2) {
                doorwayX = doorwayZ;
                doorwayZ = 7;
            }

            // Carve doorway (2 wide, 3 tall)
            for (int dx = 0; dx < 2; dx++) {
                for (int y = FLOOR_Y; y < FLOOR_Y + 3; y++) {
                    int wx = startX + doorwayX + (direction < 2 ? dx : 0);
                    int wz = startZ + doorwayZ + (direction >= 2 ? dx : 0);
                    chunk.setBlockState(mpos.set(wx, y, wz), Blocks.AIR.getDefaultState());
                }
            }

            // Frame sides: sandstone
            for (int y = FLOOR_Y; y < FLOOR_Y + 3; y++) {
                // Left frame
                int leftX = direction < 2 ? doorwayX - 1 : doorwayX;
                int leftZ = direction >= 2 ? doorwayZ - 1 : doorwayZ;
                chunk.setBlockState(mpos.set(startX + leftX, y, startZ + leftZ), 
                    Blocks.SANDSTONE.getDefaultState());
                
                // Right frame
                int rightX = direction < 2 ? doorwayX + 2 : doorwayX;
                int rightZ = direction >= 2 ? doorwayZ + 2 : doorwayZ;
                chunk.setBlockState(mpos.set(startX + rightX, y, startZ + rightZ), 
                    Blocks.SANDSTONE.getDefaultState());
            }

            // Lintel: smooth_sandstone slab at Y=67
            for (int dx = 0; dx < 2; dx++) {
                int wx = startX + doorwayX + (direction < 2 ? dx : 0);
                int wz = startZ + doorwayZ + (direction >= 2 ? dx : 0);
                chunk.setBlockState(mpos.set(wx, FLOOR_Y + 3, wz), 
                    Blocks.SMOOTH_SANDSTONE_SLAB.getDefaultState());
            }
        }
    }

    @Override
    public void buildSurface(ChunkRegion region, StructureAccessor structures,
                             NoiseConfig noiseConfig, Chunk chunk) {}

    @Override
    public void populateEntities(ChunkRegion region) {}

    @Override
    public void carve(ChunkRegion chunkRegion, long seed, NoiseConfig noiseConfig,
                      BiomeAccess biomeAccess, StructureAccessor structureAccessor, Chunk chunk) {}

    @Override
    public int getSeaLevel() { return -64; }

    @Override
    public int getMinimumY() { return -64; }

    @Override
    public int getWorldHeight() { return 384; }

    @Override
    public int getHeight(int x, int z, Heightmap.Type heightmap,
                         HeightLimitView world, NoiseConfig noiseConfig) {
        return FLOOR_Y + 1;
    }

    @Override
    public VerticalBlockSample getColumnSample(int x, int z,
                                               HeightLimitView world, NoiseConfig noiseConfig) {
        BlockState[] states = new BlockState[world.getHeight()];
        Arrays.fill(states, Blocks.AIR.getDefaultState());
        // Floor at Y=64
        if (FLOOR_Y - world.getBottomY() >= 0 && FLOOR_Y - world.getBottomY() < states.length) {
            states[FLOOR_Y - world.getBottomY()] = Blocks.SMOOTH_SANDSTONE.getDefaultState();
        }
        return new VerticalBlockSample(world.getBottomY(), states);
    }

    @Override
    public void appendDebugHudText(List<String> text, NoiseConfig noiseConfig, BlockPos pos) {}
}
