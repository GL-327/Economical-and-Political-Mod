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
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class Fracture3ChunkGenerator extends ChunkGenerator {

    public static final MapCodec<Fracture3ChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            RegistryFixedCodec.of(RegistryKeys.BIOME).fieldOf("biome").forGetter(gen -> gen.biome)
        ).apply(instance, Fracture3ChunkGenerator::new)
    );

    public static final int FLOOR_Y = 64;
    public static final int CEILING_Y = 79;
    
    // Perlin noise constants for carpet
    private static final double CARPET_FREQ = 0.15;

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

        // Ensure nodes exist in area
        // Note: For deterministic world seed we'd use server world seed, but Chunk generator doesn't always expose it here cleanly
        // We'll use 0 or a fixed seed mixed with chunk config, or just a constant for now since it's the Backrooms
        TerminalLayoutGenerator.ensureGraphExists(42L, cx, cz, 8); 

        BlockPos.Mutable mpos = new BlockPos.Mutable();
        
        List<RoomGraphNode> visibleNodes = TerminalLayout.getNodesInRegion(startX, startZ, startX + 16, startZ + 16);

        // Pre-fill void for stability inside the generation layer
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for(int y = FLOOR_Y; y <= CEILING_Y; y++){
                     chunk.setBlockState(mpos.set(startX + x, y, startZ + z), Blocks.AIR.getDefaultState());
                }
            }
        }

        // Draw each visible room
        for (RoomGraphNode node : visibleNodes) {
            drawRoom(chunk, startX, startZ, mpos, node);
        }

        return CompletableFuture.completedFuture(chunk);
    }
    
    private void drawRoom(Chunk chunk, int chunkStartX, int chunkStartZ, BlockPos.Mutable mpos, RoomGraphNode room) {
        Random rand = new Random(room.getId().getLeastSignificantBits());
        
        // Boundaries of this room
        int rx = room.getX();
        int rz = room.getZ();
        int rWidth = room.getWidth();
        int rLength = room.getLength();
        
        // Loop over the intersection of the chunk and the room
        int minX = Math.max(chunkStartX, rx);
        int maxX = Math.min(chunkStartX + 16, rx + rWidth);
        int minZ = Math.max(chunkStartZ, rz);
        int maxZ = Math.min(chunkStartZ + 16, rz + rLength);
        
        // Room specifics
        boolean isVoidEdge = room.getType() == RoomGraphNode.RoomType.VOID_EDGE;
        double carpetCov = 0.60;
        if (room.getType() == RoomGraphNode.RoomType.CORRIDOR) carpetCov = 0.80;
        if (isVoidEdge) carpetCov = 0.0;
        
        // Missing walls for void edge
        boolean northAbs = false, southAbs = false, eastAbs = false, westAbs = false;
        if (isVoidEdge) {
            northAbs = rand.nextBoolean();
            if (!northAbs) southAbs = rand.nextBoolean();
            if (!northAbs && !southAbs) eastAbs = true;
        }

        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                // Base math
                int localX = x - rx;
                int localZ = z - rz;
                
                boolean isEdgeX = (localX == 0 || localX == rWidth - 1);
                boolean isEdgeZ = (localZ == 0 || localZ == rLength - 1);
                boolean isWall = isEdgeX || isEdgeZ;
                
                // --- FLOOR ---
                // Void peek hole chance
                double holeRate = isVoidEdge ? 0.10 : 0.05;
                Random tileRand = new Random((long) x * 34187312 + (long) z * 132897);
                if (tileRand.nextDouble() < holeRate && !isWall) {
                    chunk.setBlockState(mpos.set(x, FLOOR_Y, z), Blocks.AIR.getDefaultState());
                } else {
                    chunk.setBlockState(mpos.set(x, FLOOR_Y, z), Blocks.SMOOTH_SANDSTONE.getDefaultState());
                    
                    if (localX % 4 == 0 || localZ % 4 == 0) {
                        chunk.setBlockState(mpos.set(x, FLOOR_Y, z), Blocks.SANDSTONE_SLAB.getDefaultState());
                    }
                    
                    // Carpet
                    if (!isWall && tileRand.nextDouble() < carpetCov) {
                        BlockState carpet = Blocks.YELLOW_CARPET.getDefaultState();
                        if (tileRand.nextDouble() < 0.1) carpet = Blocks.BROWN_CARPET.getDefaultState(); // Puddles approx
                        chunk.setBlockState(mpos.set(x, FLOOR_Y + 1, z), carpet);
                    }
                }
                
                // --- CEILING ---
                if (localX % 2 == 0 && localZ % 2 == 0) {
                    chunk.setBlockState(mpos.set(x, CEILING_Y, z), Blocks.CHISELED_SANDSTONE.getDefaultState());
                } else {
                    chunk.setBlockState(mpos.set(x, CEILING_Y, z), Blocks.SMOOTH_SANDSTONE.getDefaultState());
                }
                
                // Lights
                if (room.getType() != RoomGraphNode.RoomType.CORRIDOR) {
                    if (localX % 6 == 0 && localZ % 6 == 0 && !isWall) {
                        double deadRate = (room.getType() == RoomGraphNode.RoomType.INFINITE_IMPRESSION) ? 0.25 : 0.15;
                        if (isVoidEdge || tileRand.nextDouble() >= deadRate) {
                            chunk.setBlockState(mpos.set(x, CEILING_Y, z), Blocks.SEA_LANTERN.getDefaultState());
                        }
                    }
                } else {
                    // Corridor center lights
                    if (localX == rWidth / 2 && localZ % 6 == 0 && !isWall) {
                        if (tileRand.nextDouble() > 0.15) {
                            chunk.setBlockState(mpos.set(x, CEILING_Y, z), Blocks.SEA_LANTERN.getDefaultState());
                        }
                    } else if (localZ == rLength / 2 && localX % 6 == 0 && !isWall) {
                         if (tileRand.nextDouble() > 0.15) {
                            chunk.setBlockState(mpos.set(x, CEILING_Y, z), Blocks.SEA_LANTERN.getDefaultState());
                        }
                    }
                }
                
                // --- WALLS ---
                if (isWall) {
                    boolean isDoorway = false; // We would check doorways here
                    
                    // Simple doorway carve out: if connected adjacent, leave open
                    // Need to check if there is an adjacent room abutting this wall segment
                    if (TerminalLayout.hasIntersection(x, z, 1, 1) && !isStrictWallCorner(localX, localZ, rWidth, rLength)) {
                        // Very rough doorway carve logic:
                        // Realistically, doors are drawn as connections between nodes. Oh well!
                        // For pure block placement:
                    }
                    
                    boolean absent = (isEdgeX && localX == 0 && westAbs) ||
                                     (isEdgeX && localX == rWidth-1 && eastAbs) ||
                                     (isEdgeZ && localZ == 0 && northAbs) ||
                                     (isEdgeZ && localZ == rLength-1 && southAbs);
                                     
                    for (int y = FLOOR_Y + 1; y < CEILING_Y; y++) {
                        if (absent) {
                            chunk.setBlockState(mpos.set(x, y, z), Blocks.BLACK_CONCRETE.getDefaultState());
                        } else {
                            if (y == FLOOR_Y + 1 || y == CEILING_Y - 1) {
                                chunk.setBlockState(mpos.set(x, y, z), Blocks.SMOOTH_SANDSTONE_SLAB.getDefaultState());
                            } else {
                                if (localX % 8 == 0 || localZ % 8 == 0) {
                                    chunk.setBlockState(mpos.set(x, y, z), Blocks.YELLOW_GLAZED_TERRACOTTA.getDefaultState());
                                } else {
                                    chunk.setBlockState(mpos.set(x, y, z), Blocks.YELLOW_CONCRETE.getDefaultState());
                                    if (tileRand.nextDouble() < 0.03 && y >= FLOOR_Y + 2 && y <= FLOOR_Y + 4) {
                                        chunk.setBlockState(mpos.set(x, y, z), Blocks.BROWN_STAINED_GLASS_PANE.getDefaultState());
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Interior features
                if (room.getType() == RoomGraphNode.RoomType.INFINITE_IMPRESSION) {
                    if (localX % 8 == 0 && localZ % 8 == 0 && !isWall) {
                        for (int y = FLOOR_Y + 1; y < CEILING_Y; y++) {
                            chunk.setBlockState(mpos.set(x, y, z), Blocks.YELLOW_CONCRETE.getDefaultState());
                        }
                    }
                }
            }
        }
        
        // Draw Doorways (carve out from walls)
        // Check intersections with connected nodes and bore a 2-wide 3-high hole
        for (UUID connectedId : room.getConnectedNodes()) {
            RoomGraphNode other = TerminalLayout.getNode(connectedId);
            if (other != null) {
                // Find overlap
                int oxMax = Math.min(rx + rWidth, other.getX() + other.getWidth());
                int oxMin = Math.max(rx, other.getX());
                int ozMax = Math.min(rz + rLength, other.getZ() + other.getLength());
                int ozMin = Math.max(rz, other.getZ());
                
                if (oxMin < oxMax) {
                    // Overlap on X axis (North/South door)
                    int midX = (oxMin + oxMax) / 2;
                    int doorZ = rz;
                    if (other.getZ() > rz) doorZ = rz + rLength - 1;
                    
                    if (midX >= minX && midX - 1 <= maxX && doorZ >= minZ && doorZ < maxZ) {
                        carveDoorway(chunk, midX - 1, doorZ, false, mpos);
                    }
                } else if (ozMin < ozMax) {
                    // Overlap on Z axis (East/West door)
                    int midZ = (ozMin + ozMax) / 2;
                    int doorX = rx;
                    if (other.getX() > rx) doorX = rx + rWidth - 1;
                    
                    if (midZ >= minZ && midZ - 1 <= maxZ && doorX >= minX && doorX < maxX) {
                         carveDoorway(chunk, doorX, midZ - 1, true, mpos);
                    }
                }
            }
        }
    }
    
    private void carveDoorway(Chunk chunk, int x, int z, boolean isEastWest, BlockPos.Mutable mpos) {
        for (int d = 0; d < 2; d++) {
            int wx = x + (isEastWest ? 0 : d);
            int wz = z + (isEastWest ? d : 0);
            
            for (int y = FLOOR_Y; y < FLOOR_Y + 3; y++) {
                chunk.setBlockState(mpos.set(wx, y, wz), Blocks.AIR.getDefaultState());
            }
            chunk.setBlockState(mpos.set(wx, FLOOR_Y + 3, wz), Blocks.SMOOTH_SANDSTONE_SLAB.getDefaultState());
        }
    }
    
    private boolean isStrictWallCorner(int x, int z, int w, int l) {
        return (x == 0 && z == 0) || (x == 0 && z == l - 1) || (x == w - 1 && z == 0) || (x == w - 1 && z == l - 1);
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
        if (FLOOR_Y - world.getBottomY() >= 0 && FLOOR_Y - world.getBottomY() < states.length) {
            states[FLOOR_Y - world.getBottomY()] = Blocks.SMOOTH_SANDSTONE.getDefaultState();
        }
        return new VerticalBlockSample(world.getBottomY(), states);
    }

    @Override
    public void appendDebugHudText(List<String> text, NoiseConfig noiseConfig, BlockPos pos) {}
}
