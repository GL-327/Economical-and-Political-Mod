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

public class BackroomsChunkGenerator extends ChunkGenerator {

    public static final MapCodec<BackroomsChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            RegistryFixedCodec.of(RegistryKeys.BIOME).fieldOf("biome").forGetter(gen -> gen.biome)
        ).apply(instance, BackroomsChunkGenerator::new)
    );

    private static final long SEED = 0xDEADBEEFCAFEL;
    // Connection gate: blocks 6-8 (inclusive, local coords) form the 3-wide doorway
    private static final int GATE_MIN = 6;
    private static final int GATE_MAX = 8;

    private final RegistryEntry<Biome> biome;

    public BackroomsChunkGenerator(RegistryEntry<Biome> biome) {
        super(new FixedBiomeSource(biome));
        this.biome = biome;
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> getCodec() {
        return CODEC;
    }

    // ─── Maze connectivity ──────────────────────────────────────────────────────

    /**
     * Returns whether there is a North opening on chunk (cx, cz).
     * The boundary between (cx, cz) and (cx, cz-1) is keyed on cz,
     * so the South opening of (cx, cz-1) produces the same result.
     */
    private static boolean hasNorthConnection(int cx, int cz) {
        return (longHash(cx * 1234567L + (long) cz * 7654321L + SEED) & 1L) == 1L;
    }

    /** South connection of (cx, cz) = North connection of (cx, cz+1). */
    private static boolean hasSouthConnection(int cx, int cz) {
        return hasNorthConnection(cx, cz + 1);
    }

    /**
     * West opening of (cx, cz): keyed on cx, matches East opening of (cx-1, cz).
     */
    private static boolean hasWestConnection(int cx, int cz) {
        return (longHash((long) cx * 9876541L + cz * 1357913L + SEED + 0xABCDEFL) & 1L) == 1L;
    }

    /** East connection of (cx, cz) = West connection of (cx+1, cz). */
    private static boolean hasEastConnection(int cx, int cz) {
        return hasWestConnection(cx + 1, cz);
    }

    private static long longHash(long v) {
        v ^= v >>> 33;
        v *= 0xff51afd7ed558ccdL;
        v ^= v >>> 33;
        v *= 0xc4ceb9fe1a85ec53L;
        v ^= v >>> 33;
        return v;
    }

    /**
     * Returns true if local position (lx, lz) within a chunk should be a wall,
     * given the four connection flags.
     */
    private static boolean isWallBlock(int lx, int lz,
                                        boolean north, boolean south,
                                        boolean west, boolean east) {
        boolean onNorth = lz == 0;
        boolean onSouth = lz == 15;
        boolean onWest  = lx == 0;
        boolean onEast  = lx == 15;

        if (!onNorth && !onSouth && !onWest && !onEast) return false; // interior

        if (onNorth && !onEast && !onWest) return !(north && lx >= GATE_MIN && lx <= GATE_MAX);
        if (onSouth && !onEast && !onWest) return !(south && lx >= GATE_MIN && lx <= GATE_MAX);
        if (onWest  && !onNorth && !onSouth) return !(west  && lz >= GATE_MIN && lz <= GATE_MAX);
        if (onEast  && !onNorth && !onSouth) return !(east  && lz >= GATE_MIN && lz <= GATE_MAX);

        // Corners are always walls
        return true;
    }

    // ─── Main generation ────────────────────────────────────────────────────────

    @Override
    public CompletableFuture<Chunk> populateNoise(Blender blender, NoiseConfig noiseConfig,
                                                   StructureAccessor structureAccessor, Chunk chunk) {
        int cx = chunk.getPos().x;
        int cz = chunk.getPos().z;
        int startX = chunk.getPos().getStartX();
        int startZ = chunk.getPos().getStartZ();

        boolean north = hasNorthConnection(cx, cz);
        boolean south = hasSouthConnection(cx, cz);
        boolean west  = hasWestConnection(cx, cz);
        boolean east  = hasEastConnection(cx, cz);

        BlockState terracotta = Blocks.YELLOW_TERRACOTTA.getDefaultState();
        BlockState carpet     = Blocks.YELLOW_CARPET.getDefaultState();
        BlockState concrete   = Blocks.WHITE_CONCRETE.getDefaultState();
        BlockState glowstone  = Blocks.GLOWSTONE.getDefaultState();

        BlockPos.Mutable mpos = new BlockPos.Mutable();

        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                int wx = startX + lx;
                int wz = startZ + lz;

                boolean wall = isWallBlock(lx, lz, north, south, west, east);

                // Y=0: terracotta floor
                chunk.setBlockState(mpos.set(wx, 0, wz), terracotta);

                // Y=1: wall terracotta or yellow carpet
                chunk.setBlockState(mpos.set(wx, 1, wz), wall ? terracotta : carpet);

                // Y=2–4: wall terracotta or air (air is the default, so only set if wall)
                if (wall) {
                    chunk.setBlockState(mpos.set(wx, 2, wz), terracotta);
                    chunk.setBlockState(mpos.set(wx, 3, wz), terracotta);
                    chunk.setBlockState(mpos.set(wx, 4, wz), terracotta);
                }

                // Y=5: ceiling – glowstone strips every 4 blocks along X for fluorescent effect
                BlockState ceiling = (lx % 4 == 0) ? glowstone : concrete;
                chunk.setBlockState(mpos.set(wx, 5, wz), ceiling);
            }
        }

        return CompletableFuture.completedFuture(chunk);
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
        return 6;
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
