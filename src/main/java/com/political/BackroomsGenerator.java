package com.political;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

/**
 * Deterministic 16x16 "cell" stamping for the Backrooms dimension.
 *
 * Every cell is fully described by (cellX, cellZ) + the world seed, so regenerating
 * the same cell always produces identical geometry (idempotent). Chest loot and mob
 * packs are handled by BackroomsManager so generation stays purely block-based here.
 *
 * Vanilla blocks only — no custom blocks (server-side mod constraint).
 */
public final class BackroomsGenerator {

    private BackroomsGenerator() {}

    public static final int CELL = 16;
    public static final int FLOOR_Y = 40;   // solid floor blocks
    public static final int STAND_Y = 41;   // where players/mobs stand (carpet / air)
    public static final int CEIL_Y = 46;    // solid ceiling
    // interior air column: STAND_Y .. CEIL_Y-1  (41..45)

    // Room variants
    public static final int VARIANT_HALL   = 0; // classic empty hall
    public static final int VARIANT_PILLAR = 1; // pillar hall
    public static final int VARIANT_POOL   = 2; // flooded poolroom
    public static final int VARIANT_DARK   = 3; // dark cell (mob pack)
    public static final int VARIANT_STASH  = 4; // stash cell (loot chest)
    public static final int VARIANT_EXIT   = 5; // rare exit portal

    /** Deterministic 64-bit hash mixing seed + cell coords + a salt. */
    public static long hash(long seed, int cx, int cz, int salt) {
        long h = seed;
        h = h * 6364136223846793005L + (cx * 0x9E3779B97F4A7C15L);
        h ^= (h >>> 29);
        h = h * 6364136223846793005L + (cz * 0xC2B2AE3D27D4EB4FL);
        h ^= (h >>> 32);
        h = h * 6364136223846793005L + (salt * 0x165667B19E3779F9L);
        h ^= (h >>> 27);
        return h;
    }

    /** Pick a room variant for a cell (0,0 is forced to a safe hall by the caller). */
    public static int pickVariant(long seed, int cx, int cz) {
        int roll = (int) Math.floorMod(hash(seed, cx, cz, 7), 100L);
        int dist = Math.abs(cx) + Math.abs(cz);
        // Exit cells are rare and never spawn right next to the entry anchor.
        if (dist >= 3 && roll < 2) return VARIANT_EXIT;
        if (roll < 14) return VARIANT_DARK;
        if (roll < 24) return VARIANT_STASH;
        if (roll < 42) return VARIANT_POOL;
        if (roll < 66) return VARIANT_PILLAR;
        return VARIANT_HALL;
    }

    /**
     * Stamp a cell into the world. Returns the exit-portal center BlockPos for
     * VARIANT_EXIT cells, otherwise null.
     */
    public static BlockPos stampCell(ServerLevel level, int cx, int cz, int variant, long seed) {
        int minX = cx * CELL;
        int minZ = cz * CELL;

        boolean pool = variant == VARIANT_POOL;
        boolean dark = variant == VARIANT_DARK;

        BlockState floorBlock  = pool ? Blocks.SMOOTH_QUARTZ.defaultBlockState()
                                      : Blocks.GRAY_WOOL.defaultBlockState();
        BlockState wallBlock   = pool ? Blocks.WHITE_CONCRETE.defaultBlockState()
                                      : Blocks.YELLOW_CONCRETE.defaultBlockState();
        BlockState ceilBlock   = Blocks.SMOOTH_STONE.defaultBlockState();
        BlockState carpetBlock = pool ? Blocks.WATER.defaultBlockState()
                                      : Blocks.LIGHT_GRAY_CARPET.defaultBlockState();

        // Floor + ceiling + interior fill
        for (int dx = 0; dx < CELL; dx++) {
            for (int dz = 0; dz < CELL; dz++) {
                int x = minX + dx;
                int z = minZ + dz;
                set(level, x, FLOOR_Y, z, floorBlock);
                set(level, x, CEIL_Y, z, ceilBlock);
                // Clear the interior air column first
                for (int y = STAND_Y; y < CEIL_Y; y++) {
                    set(level, x, y, z, Blocks.AIR.defaultBlockState());
                }
                // Floor covering (carpet, or 1-deep water for poolrooms)
                set(level, x, STAND_Y, z, carpetBlock);
            }
        }

        // Maze walls on the west (x = minX) and north (z = minZ) edges, deterministic,
        // each with a central doorway so cells stay connected.
        boolean westWall  = (hash(seed, cx, cz, 1) & 1L) == 0L;
        boolean northWall = (hash(seed, cx, cz, 2) & 1L) == 0L;

        if (westWall) {
            for (int dz = 0; dz < CELL; dz++) {
                if (dz == 7 || dz == 8) continue; // doorway
                for (int y = STAND_Y; y < CEIL_Y; y++) {
                    set(level, minX, y, minZ + dz, wallBlock);
                }
            }
        }
        if (northWall) {
            for (int dx = 0; dx < CELL; dx++) {
                if (dx == 7 || dx == 8) continue; // doorway
                for (int y = STAND_Y; y < CEIL_Y; y++) {
                    set(level, minX + dx, y, minZ, wallBlock);
                }
            }
        }

        // Ochre-froglight strip lighting in the ceiling (skipped for dark cells).
        if (!dark) {
            BlockState frog = Blocks.OCHRE_FROGLIGHT.defaultBlockState();
            for (int dz = 2; dz < CELL - 1; dz += 4) {
                set(level, minX + 7, CEIL_Y - 1, minZ + dz, frog);
                set(level, minX + 8, CEIL_Y - 1, minZ + dz, frog);
            }
        }

        // Variant-specific decoration
        switch (variant) {
            case VARIANT_PILLAR -> {
                int[][] quads = {{4, 4}, {4, 11}, {11, 4}, {11, 11}};
                for (int[] q : quads) {
                    for (int y = STAND_Y; y < CEIL_Y; y++) {
                        set(level, minX + q[0], y, minZ + q[1], wallBlock);
                        set(level, minX + q[0] + 1, y, minZ + q[1], wallBlock);
                        set(level, minX + q[0], y, minZ + q[1] + 1, wallBlock);
                        set(level, minX + q[0] + 1, y, minZ + q[1] + 1, wallBlock);
                    }
                }
            }
            case VARIANT_STASH -> {
                BlockPos chestPos = new BlockPos(minX + 8, STAND_Y, minZ + 8);
                set(level, chestPos.getX(), chestPos.getY(), chestPos.getZ(),
                        Blocks.CHEST.defaultBlockState());
                fillStashChest(level, chestPos, new Random(hash(seed, cx, cz, 99)));
            }
            case VARIANT_EXIT -> {
                return buildExitPortal(level, minX, minZ);
            }
            default -> { /* hall / pool / dark: no extra structure */ }
        }
        return null;
    }

    /** Build a rare end-gateway-flavoured exit portal; returns the trigger center. */
    private static BlockPos buildExitPortal(ServerLevel level, int minX, int minZ) {
        int cx = minX + 8;
        int cz = minZ + 8;

        // 3x3 crying-obsidian platform
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                set(level, cx + dx, FLOOR_Y, cz + dz, Blocks.CRYING_OBSIDIAN.defaultBlockState());
                set(level, cx + dx, STAND_Y, cz + dz, Blocks.AIR.defaultBlockState());
            }
        }
        // Corner sea-lantern posts + end-rod glow columns
        int[][] corners = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
        for (int[] c : corners) {
            set(level, cx + c[0], STAND_Y, cz + c[1], Blocks.SEA_LANTERN.defaultBlockState());
            set(level, cx + c[0], STAND_Y + 1, cz + c[1], Blocks.END_ROD.defaultBlockState());
        }
        // Central beacon-of-purple column (purely decorative)
        set(level, cx, STAND_Y, cz, Blocks.PURPUR_BLOCK.defaultBlockState());
        set(level, cx, STAND_Y + 1, cz, Blocks.PURPLE_STAINED_GLASS.defaultBlockState());
        set(level, cx, STAND_Y + 2, cz, Blocks.END_ROD.defaultBlockState());

        // Trigger position is just above the central purpur block.
        return new BlockPos(cx, STAND_Y + 1, cz);
    }

    private static void fillStashChest(ServerLevel level, BlockPos pos, Random rng) {
        if (!(level.getBlockEntity(pos) instanceof ChestBlockEntity chest)) return;

        int slot = 0;
        chest.setItem(slot++, new ItemStack(Items.IRON_INGOT, 2 + rng.nextInt(6)));
        chest.setItem(slot++, new ItemStack(Items.GOLD_INGOT, 1 + rng.nextInt(4)));
        chest.setItem(slot++, new ItemStack(Items.GLOWSTONE_DUST, 3 + rng.nextInt(8)));

        // Fluorescent Fragment — Backrooms loot fuel
        chest.setItem(slot++, BackroomsItems.createFluorescentFragment(2 + rng.nextInt(5)));

        // Almond Water sometimes
        if (rng.nextFloat() < 0.55f) {
            chest.setItem(slot++, BackroomsItems.createAlmondWater(1));
        }
        // Reality Shard is rare
        if (rng.nextFloat() < 0.18f) {
            chest.setItem(slot++, BackroomsItems.createRealityShard(1));
        }
        if (rng.nextFloat() < 0.10f) {
            chest.setItem(slot++, new ItemStack(Items.DIAMOND, 1 + rng.nextInt(3)));
        }
        if (rng.nextFloat() < 0.05f) {
            chest.setItem(slot, new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 1));
        }

        // Without this the contents can be lost on unload / not sync to openers.
        chest.setChanged();
    }

    private static void set(ServerLevel level, int x, int y, int z, BlockState state) {
        level.setBlock(new BlockPos(x, y, z), state, Block.UPDATE_CLIENTS);
    }
}
