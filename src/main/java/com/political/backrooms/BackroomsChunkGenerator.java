package com.political.backrooms;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
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
import java.util.concurrent.CompletableFuture;

/**
 * Procedural Backrooms chunk generator.
 *
 * <p>The world is divided into {@value #NUM_LEVELS} stacked room layers, each
 * {@value #LEVEL_SPAN} blocks tall (floor + 4 walkable + ceiling).  Every
 * layer has its own independent maze connectivity so corridors do not line up
 * between floors.  Approximately 1-in-5 chunks contain a vertical ladder shaft
 * connecting adjacent floors.  A single, deterministic exit beacon is placed at
 * chunk ({@value #EXIT_CHUNK_X}, {@value #EXIT_CHUNK_Z}) on the ground floor;
 * stepping on it returns the player to the overworld.
 */
public class BackroomsChunkGenerator extends ChunkGenerator {

    public static final MapCodec<BackroomsChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            RegistryFixedCodec.of(RegistryKeys.BIOME).fieldOf("biome").forGetter(gen -> gen.biome)
        ).apply(instance, BackroomsChunkGenerator::new)
    );

    // ─── Structural constants ───────────────────────────────────────────────────

    /** Blocks occupied by one floor: floor slab + 4 walkable + ceiling slab. */
    static final int LEVEL_SPAN = 6;
    /** Total number of stacked floors (Y = 0 … NUM_LEVELS*LEVEL_SPAN-1). */
    static final int NUM_LEVELS = 8;

    /** Width of the doorway gap in a perimeter wall (local chunk coords 6–8). */
    private static final int GATE_MIN = 6;
    private static final int GATE_MAX = 8;

    /** Local (chunk-relative) X/Z of the ladder-shaft position within a chunk. */
    private static final int SHAFT_LX = 7;
    private static final int SHAFT_LZ = 7;

    /** The one-and-only exit chunk. Standing on the beacon here returns the player to the overworld. */
    public static final int EXIT_CHUNK_X = 0;
    public static final int EXIT_CHUNK_Z = 0;
    /** World Y of the exit beacon (top of the beacon column on level 0). */
    public static final int EXIT_BEACON_Y = 3;

    private static final long SEED = 0xDEADBEEFCAFEL;

    // ─── Interior room types ────────────────────────────────────────────────────

    private enum RoomType {
        /** Open interior – no features except perimeter walls. */
        OPEN,
        /** Pillar columns at every 4 blocks in both axes. */
        PILLARS,
        /** Narrow central corridor; interior bulk is wall. */
        NARROW,
        /** Office partition: internal T-wall divides the room into alcoves. */
        OFFICE,
        /** Storage alcoves: two short stub walls create recessed niches. */
        ALCOVE
    }

    // ─── Per-level aesthetic palette ────────────────────────────────────────────

    private record LevelPalette(BlockState floor, BlockState wall, BlockState pillar,
                                BlockState ceiling, BlockState light, BlockState floorDecor) {}

    private static LevelPalette paletteFor(int level) {
        return switch (level) {
            // Level 0-1: classic yellow rooms
            case 0 -> new LevelPalette(
                Blocks.YELLOW_TERRACOTTA.getDefaultState(),
                Blocks.YELLOW_TERRACOTTA.getDefaultState(),
                Blocks.WHITE_CONCRETE.getDefaultState(),
                Blocks.WHITE_CONCRETE.getDefaultState(),
                Blocks.GLOWSTONE.getDefaultState(),
                Blocks.YELLOW_CARPET.getDefaultState());
            case 1 -> new LevelPalette(
                Blocks.YELLOW_TERRACOTTA.getDefaultState(),
                Blocks.SMOOTH_SANDSTONE.getDefaultState(),
                Blocks.SMOOTH_SANDSTONE.getDefaultState(),
                Blocks.SMOOTH_SANDSTONE.getDefaultState(),
                Blocks.GLOWSTONE.getDefaultState(),
                Blocks.YELLOW_CARPET.getDefaultState());
            // Level 2-3: office / cream concrete
            case 2 -> new LevelPalette(
                Blocks.WHITE_CONCRETE.getDefaultState(),
                Blocks.WHITE_CONCRETE.getDefaultState(),
                Blocks.LIGHT_GRAY_CONCRETE.getDefaultState(),
                Blocks.LIGHT_GRAY_CONCRETE.getDefaultState(),
                Blocks.SEA_LANTERN.getDefaultState(),
                Blocks.LIGHT_GRAY_CARPET.getDefaultState());
            case 3 -> new LevelPalette(
                Blocks.WHITE_TERRACOTTA.getDefaultState(),
                Blocks.WHITE_TERRACOTTA.getDefaultState(),
                Blocks.WHITE_CONCRETE.getDefaultState(),
                Blocks.WHITE_CONCRETE.getDefaultState(),
                Blocks.SEA_LANTERN.getDefaultState(),
                Blocks.WHITE_CARPET.getDefaultState());
            // Level 4: flooded basement (water on floor)
            case 4 -> new LevelPalette(
                Blocks.CYAN_TERRACOTTA.getDefaultState(),
                Blocks.CYAN_TERRACOTTA.getDefaultState(),
                Blocks.WHITE_CONCRETE.getDefaultState(),
                Blocks.WHITE_CONCRETE.getDefaultState(),
                Blocks.SEA_LANTERN.getDefaultState(),
                Blocks.WATER.getDefaultState());
            // Level 5: industrial ducts
            case 5 -> new LevelPalette(
                Blocks.IRON_BLOCK.getDefaultState(),
                Blocks.GRAY_CONCRETE.getDefaultState(),
                Blocks.IRON_BLOCK.getDefaultState(),
                Blocks.RED_CONCRETE.getDefaultState(),
                Blocks.REDSTONE_LAMP.getDefaultState(),
                null);
            // Level 6: dark maintenance tunnels
            case 6 -> new LevelPalette(
                Blocks.STONE_BRICKS.getDefaultState(),
                Blocks.MOSSY_STONE_BRICKS.getDefaultState(),
                Blocks.STONE_BRICKS.getDefaultState(),
                Blocks.STONE_BRICKS.getDefaultState(),
                Blocks.SOUL_LANTERN.getDefaultState(),
                null);
            // Level 7 (and beyond): abandoned storage
            default -> new LevelPalette(
                Blocks.GRAY_CONCRETE.getDefaultState(),
                Blocks.BROWN_TERRACOTTA.getDefaultState(),
                Blocks.GRAY_CONCRETE.getDefaultState(),
                Blocks.GRAY_CONCRETE.getDefaultState(),
                Blocks.GLOWSTONE.getDefaultState(),
                null);
        };
    }

    // ─── Fields ─────────────────────────────────────────────────────────────────

    private final RegistryEntry<Biome> biome;

    public BackroomsChunkGenerator(RegistryEntry<Biome> biome) {
        super(new FixedBiomeSource(biome));
        this.biome = biome;
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> getCodec() {
        return CODEC;
    }

    // ─── Hash utilities ─────────────────────────────────────────────────────────

    private static long longHash(long v) {
        v ^= v >>> 33;
        v *= 0xff51afd7ed558ccdL;
        v ^= v >>> 33;
        v *= 0xc4ceb9fe1a85ec53L;
        v ^= v >>> 33;
        return v;
    }

    // ─── Per-level maze connectivity ─────────────────────────────────────────────

    private static boolean hasNorthConnection(int cx, int cz, int level) {
        return (longHash(cx * 1234567L + (long) cz * 7654321L + (long) level * 0xFEED1337L + SEED) & 1L) == 1L;
    }

    private static boolean hasSouthConnection(int cx, int cz, int level) {
        return hasNorthConnection(cx, cz + 1, level);
    }

    private static boolean hasWestConnection(int cx, int cz, int level) {
        return (longHash((long) cx * 9876541L + cz * 1357913L + (long) level * 0xC0FFEL + SEED + 0xABCDEFL) & 1L) == 1L;
    }

    private static boolean hasEastConnection(int cx, int cz, int level) {
        return hasWestConnection(cx + 1, cz, level);
    }

    // ─── Interior room type selection ────────────────────────────────────────────

    private static RoomType getRoomType(int cx, int cz, int level) {
        long v = longHash((long) cx * 0x1F3D5B79L + (long) cz * 0x2E4C6A88L + (long) level * 0xAB1234L + SEED);
        RoomType[] types = RoomType.values();
        return types[(int) (Math.abs(v) % types.length)];
    }

    // ─── Vertical shaft check ────────────────────────────────────────────────────

    /** True if chunk (cx, cz) has a ladder shaft between {@code level} and {@code level+1}. */
    static boolean hasVerticalShaft(int cx, int cz, int level) {
        long v = longHash((long) cx * 0x12345L + (long) cz * 0x67890L + (long) level * 0xABCDL + SEED + 0xCAFEL);
        return Math.abs(v) % 5L == 0L;
    }

    // ─── Perimeter wall logic ────────────────────────────────────────────────────

    private static boolean isPerimeterWall(int lx, int lz,
                                            boolean north, boolean south,
                                            boolean west, boolean east) {
        boolean onNorth = lz == 0;
        boolean onSouth = lz == 15;
        boolean onWest  = lx == 0;
        boolean onEast  = lx == 15;

        if (!onNorth && !onSouth && !onWest && !onEast) return false;

        if (onNorth && !onEast && !onWest) return !(north && lx >= GATE_MIN && lx <= GATE_MAX);
        if (onSouth && !onEast && !onWest) return !(south && lx >= GATE_MIN && lx <= GATE_MAX);
        if (onWest  && !onNorth && !onSouth) return !(west  && lz >= GATE_MIN && lz <= GATE_MAX);
        if (onEast  && !onNorth && !onSouth) return !(east  && lz >= GATE_MIN && lz <= GATE_MAX);

        return true; // corners
    }

    // ─── Interior feature logic ──────────────────────────────────────────────────

    /**
     * Returns true if the interior position (lx, lz) should be a solid feature
     * (pillar, partition wall, etc.) for the given room type.
     * This is only called for non-perimeter positions.
     */
    private static boolean isInteriorFeature(int lx, int lz, int wx, int wz, RoomType type, int level) {
        return switch (type) {
            case OPEN -> false;

            // Pillar every 4 blocks in both world axes (skip shaft position)
            case PILLARS -> (wx % 4 == 0) && (wz % 4 == 0)
                && !(lx == SHAFT_LX && lz == SHAFT_LZ);

            // Narrow: interior is wall except for a 2-wide central corridor along X (lz = 7-8)
            // and the gate openings on N/S sides are still honoured by perimeter logic.
            case NARROW -> lz < 6 || lz > 9;

            // Office: T-shaped partition wall at lx==8 (vertical) and lz==8 (horizontal, partial)
            case OFFICE -> (lx == 8 && lz >= 2 && lz <= 13)
                || (lz == 8 && lx >= 2 && lx <= 6);

            // Alcove: two stub walls creating recessed niches at lx=4 and lx=11
            case ALCOVE -> ((lx == 4 || lx == 11) && (lz >= 3 && lz <= 6))
                || ((lx == 4 || lx == 11) && (lz >= 9 && lz <= 12));
        };
    }

    // ─── Main generation ────────────────────────────────────────────────────────

    @Override
    public CompletableFuture<Chunk> populateNoise(Blender blender, NoiseConfig noiseConfig,
                                                   StructureAccessor structureAccessor, Chunk chunk) {
        int cx = chunk.getPos().x;
        int cz = chunk.getPos().z;
        int startX = chunk.getPos().getStartX();
        int startZ = chunk.getPos().getStartZ();

        BlockPos.Mutable mpos = new BlockPos.Mutable();

        // Generate each stacked floor
        for (int level = 0; level < NUM_LEVELS; level++) {
            generateLevel(chunk, cx, cz, level, startX, startZ, mpos);
        }

        // Vertical ladder shafts between adjacent floors
        for (int level = 0; level < NUM_LEVELS - 1; level++) {
            if (hasVerticalShaft(cx, cz, level)) {
                generateShaft(chunk, level, startX, startZ, mpos);
            }
        }

        // The one exit beacon (only in the designated chunk, ground floor)
        if (cx == EXIT_CHUNK_X && cz == EXIT_CHUNK_Z) {
            generateExit(chunk, startX, startZ, mpos);
        }

        return CompletableFuture.completedFuture(chunk);
    }

    // ─── Floor generation ────────────────────────────────────────────────────────

    private static void generateLevel(Chunk chunk, int cx, int cz, int level,
                                       int startX, int startZ, BlockPos.Mutable mpos) {
        int baseY = level * LEVEL_SPAN;

        boolean north = hasNorthConnection(cx, cz, level);
        boolean south = hasSouthConnection(cx, cz, level);
        boolean west  = hasWestConnection(cx, cz, level);
        boolean east  = hasEastConnection(cx, cz, level);

        RoomType roomType = getRoomType(cx, cz, level);
        LevelPalette pal  = paletteFor(level);

        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                int wx = startX + lx;
                int wz = startZ + lz;

                boolean isWall = isPerimeterWall(lx, lz, north, south, west, east)
                    || isInteriorFeature(lx, lz, wx, wz, roomType, level);

                // Floor slab (baseY)
                chunk.setBlockState(mpos.set(wx, baseY, wz), pal.floor());

                // Walkable layer at baseY+1 (carpet/decor or wall base)
                if (isWall) {
                    chunk.setBlockState(mpos.set(wx, baseY + 1, wz), pal.wall());
                } else if (pal.floorDecor() != null) {
                    chunk.setBlockState(mpos.set(wx, baseY + 1, wz), pal.floorDecor());
                }

                // Wall body baseY+2 … baseY+4
                if (isWall) {
                    chunk.setBlockState(mpos.set(wx, baseY + 2, wz), pal.wall());
                    chunk.setBlockState(mpos.set(wx, baseY + 3, wz), pal.wall());
                    chunk.setBlockState(mpos.set(wx, baseY + 4, wz), pal.wall());
                }

                // Ceiling slab (baseY+5): light strips every 4 X-blocks
                BlockState ceilBlock = (lx % 4 == 0) ? pal.light() : pal.ceiling();
                chunk.setBlockState(mpos.set(wx, baseY + 5, wz), ceilBlock);
            }
        }
    }

    // ─── Vertical shaft (ladder) between level and level+1 ───────────────────────

    private static void generateShaft(Chunk chunk, int level, int startX, int startZ, BlockPos.Mutable mpos) {
        int wx = startX + SHAFT_LX;
        int wz = startZ + SHAFT_LZ;

        int lowerCeilingY = level * LEVEL_SPAN + 5;        // ceiling of lower floor
        int upperFloorY   = (level + 1) * LEVEL_SPAN;      // floor of upper floor

        // Remove the ceiling of the lower floor and floor of the upper floor
        // to create a 2-block opening.
        chunk.setBlockState(mpos.set(wx, lowerCeilingY, wz), Blocks.AIR.getDefaultState());
        chunk.setBlockState(mpos.set(wx, upperFloorY, wz), Blocks.AIR.getDefaultState());

        // Place a solid backing column at wz+1 so ladders have something to attach to.
        // Ladders face NORTH (backed by the solid block to the south at wz+1).
        BlockState backing = Blocks.YELLOW_TERRACOTTA.getDefaultState();
        int shaftBottom = level * LEVEL_SPAN + 1;
        int shaftTop    = (level + 1) * LEVEL_SPAN + 4;
        for (int y = shaftBottom; y <= shaftTop; y++) {
            chunk.setBlockState(mpos.set(wx, y, wz + 1), backing);
            chunk.setBlockState(mpos.set(wx, y, wz),
                Blocks.LADDER.getDefaultState()
                    .with(net.minecraft.block.LadderBlock.FACING,
                          net.minecraft.util.math.Direction.NORTH));
        }
    }

    // ─── Exit beacon structure ────────────────────────────────────────────────────

    /**
     * Builds a small beacon column at the centre of chunk (EXIT_CHUNK_X, EXIT_CHUNK_Z)
     * on the ground floor.  The glowing beacon top (Y={@value #EXIT_BEACON_Y}) is the
     * block players need to stand on to leave the Backrooms.
     */
    private static void generateExit(Chunk chunk, int startX, int startZ, BlockPos.Mutable mpos) {
        int cx = 8; // local centre X
        int cz = 8; // local centre Z
        int wx = startX + cx;
        int wz = startZ + cz;

        // Gold block base (Y=1) under the beacon
        chunk.setBlockState(mpos.set(wx, 1, wz), Blocks.GOLD_BLOCK.getDefaultState());
        chunk.setBlockState(mpos.set(wx, 2, wz), Blocks.GOLD_BLOCK.getDefaultState());
        // Beacon at Y=EXIT_BEACON_Y (lit beacon acts as the exit trigger)
        chunk.setBlockState(mpos.set(wx, EXIT_BEACON_Y, wz), Blocks.BEACON.getDefaultState());

        // Surround the beacon with a ring of emerald blocks at Y=1 to make it visible
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (dx == 0 && dz == 0) continue; // centre already set
                if (Math.abs(dx) == 2 || Math.abs(dz) == 2) {
                    chunk.setBlockState(mpos.set(wx + dx, 1, wz + dz),
                        Blocks.EMERALD_BLOCK.getDefaultState());
                }
            }
        }

        // Sign of light above beacon so players know this is the exit
        chunk.setBlockState(mpos.set(wx, 4, wz), Blocks.GLOWSTONE.getDefaultState());
    }

    // ─── Required overrides (no-ops or minimal) ─────────────────────────────────

    @Override
    public void buildSurface(ChunkRegion region, StructureAccessor structures,
                             NoiseConfig noiseConfig, Chunk chunk) {}

    @Override
    public void populateEntities(ChunkRegion region) {}

    @Override
    public void carve(ChunkRegion chunkRegion, long seed, NoiseConfig noiseConfig,
                      BiomeAccess biomeAccess, StructureAccessor structureAccessor, Chunk chunk) {}

    @Override
    public int getSeaLevel() { return -1; }

    @Override
    public int getMinimumY() { return 0; }

    @Override
    public int getWorldHeight() { return 64; }

    @Override
    public int getHeight(int x, int z, Heightmap.Type heightmap,
                         HeightLimitView world, NoiseConfig noiseConfig) {
        return NUM_LEVELS * LEVEL_SPAN;
    }

    @Override
    public VerticalBlockSample getColumnSample(int x, int z,
                                               HeightLimitView world, NoiseConfig noiseConfig) {
        BlockState[] states = new BlockState[world.getHeight()];
        Arrays.fill(states, Blocks.AIR.getDefaultState());
        if (states.length > 0) states[0] = Blocks.YELLOW_TERRACOTTA.getDefaultState();
        if (states.length > 1) states[1] = Blocks.YELLOW_CARPET.getDefaultState();
        if (states.length > 5) states[5] = Blocks.WHITE_CONCRETE.getDefaultState();
        return new VerticalBlockSample(world.getBottomY(), states);
    }

    @Override
    public void appendDebugHudText(List<String> text, NoiseConfig noiseConfig, BlockPos pos) {}
}
