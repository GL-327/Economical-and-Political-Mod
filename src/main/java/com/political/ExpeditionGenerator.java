package com.political;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * V5 Pillar 1 — procedural stamping of an Expedition floor.
 *
 * <p>Instances live in a dedicated "expedition lane" of the End dimension, far past any
 * reasonable player activity ({@link #BASE_X} / {@link #BASE_Z}). Each concurrent run
 * reserves a slot index and gets its own 512-block stride, so two runs can never overlap.
 *
 * <p>Generation is fully deterministic from {@code (seed, floor)}: the same inputs always
 * produce the same layout. Nothing here spawns mobs or loot — {@link ExpeditionRunManager}
 * owns all of that, so this class stays purely block-based and side-effect free.
 *
 * <p>{@link #wipe} erases an instance back to air when a run ends, so abandoned dungeons
 * never accumulate in the world.
 */
public final class ExpeditionGenerator {

    private ExpeditionGenerator() {}

    // ── lane geometry ──
    public static final int BASE_X = 1_600_000;
    public static final int BASE_Z = 1_600_000;
    public static final int SLOT_STRIDE = 512;
    public static final int MAX_SLOTS = 16;

    // ── room geometry ──
    public static final int GRID = 3;          // GRID x GRID rooms per floor
    public static final int ROOM = 24;         // outer room pitch (walls sit on the boundaries)
    public static final int SPAN = GRID * ROOM + 1;

    public static final int FLOOR_Y = 96;      // solid floor slab
    public static final int STAND_Y = 97;      // first air level (players stand here)
    public static final int CEIL_Y = 105;      // solid ceiling slab
    // interior air column: STAND_Y .. CEIL_Y-1

    // Room type names mirror ExpeditionDungeon.Room.type
    public static final String T_ENTRY = "ENTRY";
    public static final String T_COMBAT = "COMBAT";
    public static final String T_PUZZLE = "PUZZLE";
    public static final String T_VAULT = "VAULT";
    public static final String T_BOSS = "BOSS";

    // ────────────────────────────────────────────────────────────
    // Placement
    // ────────────────────────────────────────────────────────────

    public static int originXForSlot(int slot) {
        return BASE_X + slot * SLOT_STRIDE;
    }

    public static int originZForSlot(int slot) {
        return BASE_Z;
    }

    /** Deterministic 64-bit mix (same shape as BackroomsGenerator.hash). */
    public static long hash(long seed, int a, int b, int salt) {
        long h = seed;
        h = h * 6364136223846793005L + (a * 0x9E3779B97F4A7C15L);
        h ^= (h >>> 29);
        h = h * 6364136223846793005L + (b * 0xC2B2AE3D27D4EB4FL);
        h ^= (h >>> 32);
        h = h * 6364136223846793005L + (salt * 0x165667B19E3779F9L);
        h ^= (h >>> 27);
        return h;
    }

    /** World-space center of a room (at standing height). */
    public static BlockPos roomCenter(ExpeditionDungeon d, ExpeditionDungeon.Room r) {
        return new BlockPos(
                d.originX + r.gx * ROOM + ROOM / 2,
                STAND_Y,
                d.originZ + r.gz * ROOM + ROOM / 2);
    }

    /** Grid cell containing a world position, or null when outside the instance. */
    public static int[] gridCellOf(ExpeditionDungeon d, double wx, double wz) {
        int dx = (int) Math.floor(wx) - d.originX;
        int dz = (int) Math.floor(wz) - d.originZ;
        if (dx < 0 || dz < 0 || dx >= SPAN || dz >= SPAN) return null;
        int gx = Math.min(GRID - 1, dx / ROOM);
        int gz = Math.min(GRID - 1, dz / ROOM);
        return new int[]{gx, gz};
    }

    /** Bounding box of the whole instance, generously padded for entity sweeps. */
    public static AABB instanceBounds(ExpeditionDungeon d) {
        return new AABB(
                d.originX - 4, FLOOR_Y - 8, d.originZ - 4,
                d.originX + SPAN + 4, CEIL_Y + 8, d.originZ + SPAN + 4);
    }

    /** Bounding box of one room's interior. */
    public static AABB roomBounds(ExpeditionDungeon d, ExpeditionDungeon.Room r) {
        int minX = d.originX + r.gx * ROOM;
        int minZ = d.originZ + r.gz * ROOM;
        return new AABB(minX, FLOOR_Y, minZ, minX + ROOM + 1, CEIL_Y + 1, minZ + ROOM + 1);
    }

    // ────────────────────────────────────────────────────────────
    // Layout
    // ────────────────────────────────────────────────────────────

    /**
     * Deterministic room layout for one floor. Entry is always (0,0). On the final floor the
     * far corner is the boss arena. Exactly one puzzle room and one vault are placed; every
     * other cell is a combat room.
     */
    public static List<ExpeditionDungeon.Room> layout(long seed, int floor, boolean finalFloor) {
        List<ExpeditionDungeon.Room> out = new ArrayList<>();
        List<int[]> cells = new ArrayList<>();
        for (int gx = 0; gx < GRID; gx++) {
            for (int gz = 0; gz < GRID; gz++) {
                cells.add(new int[]{gx, gz});
            }
        }

        int bossGx = GRID - 1;
        int bossGz = GRID - 1;

        // Candidate cells for special rooms: not the entry, not the boss arena.
        List<int[]> candidates = new ArrayList<>();
        for (int[] c : cells) {
            if (c[0] == 0 && c[1] == 0) continue;
            if (finalFloor && c[0] == bossGx && c[1] == bossGz) continue;
            candidates.add(c);
        }

        int puzzleIdx = candidates.isEmpty() ? -1
                : (int) Math.floorMod(hash(seed, floor, 0, 11), (long) candidates.size());
        int[] puzzle = puzzleIdx < 0 ? null : candidates.get(puzzleIdx);
        if (puzzle != null) candidates.remove(puzzleIdx);

        int vaultIdx = candidates.isEmpty() ? -1
                : (int) Math.floorMod(hash(seed, floor, 0, 23), (long) candidates.size());
        int[] vault = vaultIdx < 0 ? null : candidates.get(vaultIdx);

        for (int[] c : cells) {
            String type;
            if (c[0] == 0 && c[1] == 0) {
                type = T_ENTRY;
            } else if (finalFloor && c[0] == bossGx && c[1] == bossGz) {
                type = T_BOSS;
            } else if (puzzle != null && c[0] == puzzle[0] && c[1] == puzzle[1]) {
                type = T_PUZZLE;
            } else if (vault != null && c[0] == vault[0] && c[1] == vault[1]) {
                type = T_VAULT;
            } else {
                type = T_COMBAT;
            }
            out.add(new ExpeditionDungeon.Room(c[0], c[1], type));
        }
        return out;
    }

    // ────────────────────────────────────────────────────────────
    // Stamping
    // ────────────────────────────────────────────────────────────

    /**
     * Build the current floor of {@code d} into the world. Assumes {@code d.rooms} has already
     * been populated by {@link #layout}. Idempotent for the same dungeon state.
     */
    public static void stampFloor(ServerLevel level, ExpeditionDungeon d) {
        BlockState wall = wallFor(d);
        BlockState floor = floorFor(d);
        BlockState ceil = Blocks.POLISHED_DEEPSLATE.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        int ox = d.originX;
        int oz = d.originZ;

        // Shell: solid floor + ceiling, interior air, wall columns on every room boundary line.
        for (int dx = 0; dx < SPAN; dx++) {
            for (int dz = 0; dz < SPAN; dz++) {
                int x = ox + dx;
                int z = oz + dz;
                // A skirt below the floor stops anyone void-falling through a seam.
                set(level, x, FLOOR_Y - 1, z, Blocks.DEEPSLATE_BRICKS.defaultBlockState());
                set(level, x, FLOOR_Y, z, floor);
                set(level, x, CEIL_Y, z, ceil);

                boolean isWall = (dx % ROOM == 0) || (dz % ROOM == 0)
                        || dx == SPAN - 1 || dz == SPAN - 1;
                for (int y = STAND_Y; y < CEIL_Y; y++) {
                    set(level, x, y, z, isWall ? wall : air);
                }
            }
        }

        // Doorways: carve a 2-wide, 3-high gap in the middle of every internal boundary.
        for (int g = 1; g < GRID; g++) {
            int lineX = g * ROOM;
            int lineZ = g * ROOM;
            for (int cell = 0; cell < GRID; cell++) {
                int mid = cell * ROOM + ROOM / 2;
                for (int y = STAND_Y; y <= STAND_Y + 2; y++) {
                    // vertical boundary (constant local X)
                    set(level, ox + lineX, y, oz + mid - 1, air);
                    set(level, ox + lineX, y, oz + mid, air);
                    // horizontal boundary (constant local Z)
                    set(level, ox + mid - 1, y, oz + lineZ, air);
                    set(level, ox + mid, y, oz + lineZ, air);
                }
            }
        }

        // Ceiling lighting so the instance is never pitch black (mobs are placed, not spawned).
        BlockState lamp = Blocks.SEA_LANTERN.defaultBlockState();
        for (int dx = 6; dx < SPAN; dx += 8) {
            for (int dz = 6; dz < SPAN; dz += 8) {
                set(level, ox + dx, CEIL_Y - 1, oz + dz, lamp);
            }
        }

        // Per-room dressing
        for (ExpeditionDungeon.Room r : d.roomList()) {
            switch (r.type) {
                case T_ENTRY -> stampEntry(level, d, r);
                case T_PUZZLE -> stampPuzzle(level, d, r);
                case T_VAULT -> stampVault(level, d, r);
                case T_BOSS -> stampBoss(level, d, r);
                default -> stampCombat(level, d, r);
            }
        }
    }

    private static BlockState wallFor(ExpeditionDungeon d) {
        return switch (d.tier == null ? "" : d.tier) {
            case "DESCENT" -> Blocks.DEEPSLATE_BRICKS.defaultBlockState();
            case "ABYSS" -> Blocks.DEEPSLATE_TILES.defaultBlockState();
            case "CATACLYSM" -> Blocks.BLACKSTONE.defaultBlockState();
            case "APOTHEOSIS" -> Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
            default -> Blocks.STONE_BRICKS.defaultBlockState();
        };
    }

    private static BlockState floorFor(ExpeditionDungeon d) {
        return switch (d.tier == null ? "" : d.tier) {
            case "DESCENT" -> Blocks.POLISHED_ANDESITE.defaultBlockState();
            case "ABYSS" -> Blocks.POLISHED_DEEPSLATE.defaultBlockState();
            case "CATACLYSM" -> Blocks.POLISHED_BLACKSTONE.defaultBlockState();
            case "APOTHEOSIS" -> Blocks.SMOOTH_BASALT.defaultBlockState();
            default -> Blocks.STONE_BRICKS.defaultBlockState();
        };
    }

    private static void stampEntry(ServerLevel level, ExpeditionDungeon d, ExpeditionDungeon.Room r) {
        BlockPos c = roomCenter(d, r);
        // Raised beacon-less dais marking the safe re-entry point.
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                set(level, c.getX() + dx, FLOOR_Y, c.getZ() + dz,
                        Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState());
            }
        }
        set(level, c.getX(), FLOOR_Y, c.getZ(), Blocks.LODESTONE.defaultBlockState());
        int[][] posts = {{-2, -2}, {-2, 2}, {2, -2}, {2, 2}};
        for (int[] p : posts) {
            set(level, c.getX() + p[0], STAND_Y, c.getZ() + p[1], Blocks.END_ROD.defaultBlockState());
        }
    }

    private static void stampCombat(ServerLevel level, ExpeditionDungeon d, ExpeditionDungeon.Room r) {
        long h = hash(d.seed, r.gx, r.gz, 41 + d.floor);
        BlockPos c = roomCenter(d, r);
        int variant = (int) Math.floorMod(h, 3L);
        BlockState pillar = wallFor(d);

        if (variant == 0) {
            // four pillars
            int[][] quads = {{-6, -6}, {-6, 6}, {6, -6}, {6, 6}};
            for (int[] q : quads) {
                for (int y = STAND_Y; y < CEIL_Y; y++) {
                    set(level, c.getX() + q[0], y, c.getZ() + q[1], pillar);
                }
            }
        } else if (variant == 1) {
            // low cover walls
            for (int dx = -5; dx <= 5; dx++) {
                set(level, c.getX() + dx, STAND_Y, c.getZ() - 4, pillar);
                set(level, c.getX() + dx, STAND_Y, c.getZ() + 4, pillar);
            }
        } else {
            // sunken pit ring (cosmetic, one block deep)
            for (int dx = -4; dx <= 4; dx++) {
                for (int dz = -4; dz <= 4; dz++) {
                    if (Math.abs(dx) == 4 || Math.abs(dz) == 4) {
                        set(level, c.getX() + dx, STAND_Y, c.getZ() + dz, pillar);
                    }
                }
            }
        }
    }

    /**
     * Puzzle room: three wall levers on purpose-built pillars. The room clears when all three
     * read powered — see {@link #puzzleLeverPositions} / {@link #puzzleSolved}.
     *
     * <p>The levers are placed with {@code defaultBlockState()} on purpose. A default lever is
     * WALL-attached facing NORTH, meaning its support block sits one block SOUTH of it, so we
     * simply build that support. This avoids touching any attach-face / facing property and
     * keeps the block-state code to the single {@code POWERED} read in {@link #puzzleSolved}.
     */
    private static void stampPuzzle(ServerLevel level, ExpeditionDungeon d, ExpeditionDungeon.Room r) {
        BlockPos c = roomCenter(d, r);
        // decorative frame
        for (int dx = -6; dx <= 6; dx++) {
            set(level, c.getX() + dx, STAND_Y, c.getZ() + 6, Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
        }
        for (BlockPos p : puzzleLeverPositions(d, r)) {
            // support pillar one block SOUTH of the lever (higher Z), two blocks tall
            set(level, p.getX(), p.getY() - 1, p.getZ() + 1, Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState());
            set(level, p.getX(), p.getY(), p.getZ() + 1, Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState());
            // clear the lever cell and its floor, then place the lever
            set(level, p.getX(), p.getY() - 1, p.getZ(), Blocks.SMOOTH_QUARTZ.defaultBlockState());
            set(level, p.getX(), p.getY(), p.getZ(), Blocks.LEVER.defaultBlockState());
            set(level, p.getX(), p.getY() + 1, p.getZ(), Blocks.AIR.defaultBlockState());
        }
    }

    /** The three lever positions for a puzzle room (one block above the floor slab). */
    public static List<BlockPos> puzzleLeverPositions(ExpeditionDungeon d, ExpeditionDungeon.Room r) {
        BlockPos c = roomCenter(d, r);
        int y = STAND_Y + 1;
        List<BlockPos> out = new ArrayList<>(3);
        out.add(new BlockPos(c.getX() - 5, y, c.getZ() - 3));
        out.add(new BlockPos(c.getX(), y, c.getZ() - 3));
        out.add(new BlockPos(c.getX() + 5, y, c.getZ() - 3));
        return out;
    }

    /** True when every lever in the puzzle room is flipped on. */
    public static boolean puzzleSolved(ServerLevel level, ExpeditionDungeon d, ExpeditionDungeon.Room r) {
        for (BlockPos p : puzzleLeverPositions(d, r)) {
            BlockState st = level.getBlockState(p);
            if (!st.is(Blocks.LEVER)) return false;
            if (!st.hasProperty(BlockStateProperties.POWERED)) return false;
            if (!st.getValue(BlockStateProperties.POWERED)) return false;
        }
        return true;
    }

    private static void stampVault(ServerLevel level, ExpeditionDungeon d, ExpeditionDungeon.Room r) {
        BlockPos c = roomCenter(d, r);
        // gold-trimmed vault floor
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                set(level, c.getX() + dx, FLOOR_Y, c.getZ() + dz, Blocks.GILDED_BLACKSTONE.defaultBlockState());
            }
        }
        for (BlockPos p : vaultChestPositions(d, r)) {
            set(level, p.getX(), p.getY(), p.getZ(), Blocks.CHEST.defaultBlockState());
        }
        set(level, c.getX(), STAND_Y, c.getZ(), Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState());
        set(level, c.getX(), STAND_Y + 1, c.getZ(), Blocks.END_ROD.defaultBlockState());
    }

    /** Vault chests are stamped empty and only filled once the vault guards are dead. */
    public static List<BlockPos> vaultChestPositions(ExpeditionDungeon d, ExpeditionDungeon.Room r) {
        BlockPos c = roomCenter(d, r);
        List<BlockPos> out = new ArrayList<>(2);
        out.add(new BlockPos(c.getX() - 2, STAND_Y, c.getZ()));
        out.add(new BlockPos(c.getX() + 2, STAND_Y, c.getZ()));
        return out;
    }

    private static void stampBoss(ServerLevel level, ExpeditionDungeon d, ExpeditionDungeon.Room r) {
        BlockPos c = roomCenter(d, r);
        // sunken arena floor with an obsidian ring
        for (int dx = -8; dx <= 8; dx++) {
            for (int dz = -8; dz <= 8; dz++) {
                int dist = Math.max(Math.abs(dx), Math.abs(dz));
                if (dist == 8) {
                    set(level, c.getX() + dx, STAND_Y, c.getZ() + dz, Blocks.CRYING_OBSIDIAN.defaultBlockState());
                } else {
                    set(level, c.getX() + dx, FLOOR_Y, c.getZ() + dz, Blocks.POLISHED_BLACKSTONE.defaultBlockState());
                }
            }
        }
        int[][] posts = {{-7, -7}, {-7, 7}, {7, -7}, {7, 7}};
        for (int[] p : posts) {
            for (int y = STAND_Y; y < CEIL_Y - 1; y++) {
                set(level, c.getX() + p[0], y, c.getZ() + p[1], Blocks.OBSIDIAN.defaultBlockState());
            }
            set(level, c.getX() + p[0], CEIL_Y - 1, c.getZ() + p[1], Blocks.SEA_LANTERN.defaultBlockState());
        }
    }

    // ────────────────────────────────────────────────────────────
    // Teardown
    // ────────────────────────────────────────────────────────────

    /**
     * Erase an instance completely. Called on completion, failure, abandon, and on server
     * start for any run that survived a restart — the world must never accumulate dungeons.
     */
    public static void wipe(ServerLevel level, int originX, int originZ) {
        BlockState air = Blocks.AIR.defaultBlockState();
        for (int dx = -1; dx <= SPAN; dx++) {
            for (int dz = -1; dz <= SPAN; dz++) {
                for (int y = FLOOR_Y - 2; y <= CEIL_Y + 1; y++) {
                    set(level, originX + dx, y, originZ + dz, air);
                }
            }
        }
    }

    public static void wipe(ServerLevel level, ExpeditionDungeon d) {
        wipe(level, d.originX, d.originZ);
    }

    private static void set(ServerLevel level, int x, int y, int z, BlockState state) {
        level.setBlock(new BlockPos(x, y, z), state, Block.UPDATE_CLIENTS);
    }

    /** Deterministic RNG for a floor, so loot placement is reproducible within a run. */
    public static Random rngFor(ExpeditionDungeon d, int salt) {
        return new Random(hash(d.seed, d.floor, salt, 77));
    }
}
