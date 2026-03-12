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
 * Chunk generator for the Industrial dimension.
 * Aesthetic: iron-grate floor, gray/red concrete walls, iron-bar accents,
 * iron-block pipe columns, redstone-lamp ceiling lighting.
 */
public class IndustrialChunkGenerator extends ChunkGenerator {

    public static final MapCodec<IndustrialChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            RegistryFixedCodec.of(RegistryKeys.BIOME).fieldOf("biome").forGetter(gen -> gen.biome)
        ).apply(instance, IndustrialChunkGenerator::new)
    );

    private static final long SEED = 0xFACEFEEDB0BL;
    private static final int GATE_MIN = 6;
    private static final int GATE_MAX = 8;
    /** Iron-block pipe columns every 6 blocks (world coords) for industrial piping. */
    private static final int PIPE_INTERVAL = 6;

    private final RegistryEntry<Biome> biome;

    public IndustrialChunkGenerator(RegistryEntry<Biome> biome) {
        super(new FixedBiomeSource(biome));
        this.biome = biome;
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> getCodec() {
        return CODEC;
    }

    // ─── Maze connectivity ────────────────────────────────────────────────────

    private static boolean hasNorthConnection(int cx, int cz) {
        return (longHash(cx * 1234567L + (long) cz * 7654321L + SEED) & 1L) == 1L;
    }

    private static boolean hasSouthConnection(int cx, int cz) {
        return hasNorthConnection(cx, cz + 1);
    }

    private static boolean hasWestConnection(int cx, int cz) {
        return (longHash((long) cx * 9876541L + cz * 1357913L + SEED + 0xABCDEFL) & 1L) == 1L;
    }

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

    private static boolean isWallBlock(int lx, int lz,
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

        BlockState ironBlock    = Blocks.IRON_BLOCK.getDefaultState();
        BlockState grayConcrete = Blocks.GRAY_CONCRETE.getDefaultState();
        BlockState redConcrete  = Blocks.RED_CONCRETE.getDefaultState();
        BlockState ironBars     = Blocks.IRON_BARS.getDefaultState();
        BlockState redstoneLamp = Blocks.REDSTONE_LAMP.getDefaultState();

        BlockPos.Mutable mpos = new BlockPos.Mutable();

        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                int wx = startX + lx;
                int wz = startZ + lz;

                boolean wall = isWallBlock(lx, lz, north, south, west, east);
                // Iron-block pipe columns at pipe-interval positions (interior only)
                boolean isPipe = !wall && (wx % PIPE_INTERVAL == 0) && (wz % PIPE_INTERVAL == 0);
                // Iron-bar accent: one block inset from walls on interior edges
                boolean isBarAccent = !wall && !isPipe &&
                        ((lx == 1 || lx == 14) || (lz == 1 || lz == 14));

                // Y=0: iron-block floor (industrial grating feel)
                chunk.setBlockState(mpos.set(wx, 0, wz), ironBlock);

                if (wall) {
                    // Wall base: gray concrete Y=1-3, red concrete accent Y=4
                    chunk.setBlockState(mpos.set(wx, 1, wz), grayConcrete);
                    chunk.setBlockState(mpos.set(wx, 2, wz), grayConcrete);
                    chunk.setBlockState(mpos.set(wx, 3, wz), grayConcrete);
                    chunk.setBlockState(mpos.set(wx, 4, wz), redConcrete);
                } else if (isPipe) {
                    // Vertical pipe column Y=1-4
                    for (int y = 1; y <= 4; y++) {
                        chunk.setBlockState(mpos.set(wx, y, wz), ironBlock);
                    }
                } else if (isBarAccent) {
                    // Iron-bar accent strips along inner wall boundary
                    chunk.setBlockState(mpos.set(wx, 2, wz), ironBars);
                    chunk.setBlockState(mpos.set(wx, 3, wz), ironBars);
                }
                // else: Y=1-4 stay as air

                // Y=5: ceiling – redstone lamps for lighting (lit), red concrete otherwise
                // Lamps placed every 4 blocks along X for uniform industrial lighting
                BlockState ceiling = (lx % 4 == 0) ? redstoneLamp : redConcrete;
                chunk.setBlockState(mpos.set(wx, 5, wz), ceiling);
            }
        }

        return CompletableFuture.completedFuture(chunk);
    }

    // ─── Required overrides ─────────────────────────────────────────────────────

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
        if (states.length > 0) states[0] = Blocks.IRON_BLOCK.getDefaultState();
        if (states.length > 5) states[5] = Blocks.RED_CONCRETE.getDefaultState();
        return new VerticalBlockSample(world.getBottomY(), states);
    }

    @Override
    public void appendDebugHudText(List<String> text, NoiseConfig noiseConfig, BlockPos pos) {}
}
