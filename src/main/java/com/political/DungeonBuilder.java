package com.political;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Lays out one dungeon floor and builds it.
 *
 * <p>The rooms in these packs are not a jigsaw set. They run from nine blocks square to forty
 * eight, they were authored for different generators, and none of them agree on where a doorway
 * belongs. Assuming a tidy grid and hoping is the thing that produces a dungeon full of rooms you
 * cannot walk between, so this does not assume one.</p>
 *
 * <p>Instead each room is dropped into a cell large enough for the biggest of them, the floor level
 * of each is <em>measured after placement</em> rather than guessed, and corridors are cut between
 * them afterwards, ramping in Y so two rooms sitting at different heights still connect. Doorways
 * are carved through whatever wall the corridor happens to meet. Nothing here needs the rooms to
 * cooperate, which is the only way this works across three packs that have never met.</p>
 */
public final class DungeonBuilder {

    private DungeonBuilder() {}

    /** Cell pitch. The largest room is 48 across, so this leaves eight blocks of corridor space. */
    public static final int CELL = 56;

    /** The Y every room's origin sits on. Floors are measured up from here. */
    public static final int BASE = 64;

    /** How wide the grid may be. Six by six is the Skyblock shape and it reads well. */
    public static final int GRID = 6;

    /** Returned when no walkable surface exists anywhere in a room. */
    private static final int NO_FLOOR = Integer.MIN_VALUE;

    /** Corridors are this wide and this tall on the inside. */
    private static final int HALL_HALF = 1;
    private static final int HALL_HEIGHT = 4;

    // ── Results ───────────────────────────────────────────────────────────

    /** One room, once it is in the world. */
    public static final class Placed {
        public DungeonCatalog.Room room;
        public int gx;
        public int gz;
        public BlockPos origin;
        public Vec3i size;
        /** Y of the first walkable surface inside, measured rather than assumed. */
        public int floorY;
        /** False when no surface was found anywhere and floorY is the structure's own base. */
        public boolean floorMeasured;
        public DungeonRoomKind kind = DungeonRoomKind.NORMAL;
        public boolean cleared;

        /** The middle of the room, standing on its floor. */
        public BlockPos centre() {
            return new BlockPos(origin.getX() + size.getX() / 2, floorY + 1,
                    origin.getZ() + size.getZ() / 2);
        }
    }

    /** Everything one built floor consists of. */
    public static final class Layout {
        public final List<Placed> rooms = new ArrayList<>();
        public BlockPos entrance;
        public Placed bossRoom;
        /** Cell key to room, for connecting neighbours. */
        public final Map<Long, Placed> byCell = new HashMap<>();
    }

    private static long key(int gx, int gz) {
        return ((long) gx << 32) ^ (gz & 0xffffffffL);
    }

    // ── Building ──────────────────────────────────────────────────────────

    /**
     * Builds a whole floor at the given world offset.
     *
     * <p>Instances are spaced far apart in the same dimension rather than given a dimension each,
     * because a dimension per run is a folder per run on disk and a server that has been up a month
     * would be carrying hundreds of them.</p>
     */
    public static Layout build(MinecraftServer server, ServerLevel level, DungeonFloor floor,
                               BlockPos worldOffset, long seed) {
        Random rng = new Random(seed);
        Layout layout = new Layout();

        List<DungeonCatalog.Room> drawn = DungeonCatalog.draw(floor, floor.roomCount(), rng);
        if (drawn.isEmpty()) {
            PoliticalServer.LOGGER.error("Dungeon floor " + floor.name() + " has no rooms to place");
            return layout;
        }

        List<int[]> cells = walkCells(drawn.size(), rng);

        // ── Place ──
        for (int i = 0; i < cells.size() && i < drawn.size(); i++) {
            DungeonCatalog.Room room = drawn.get(i);
            StructureTemplate template = DungeonCatalog.template(server, room);
            if (template == null) continue;

            int gx = cells.get(i)[0];
            int gz = cells.get(i)[1];
            Vec3i size = template.getSize();

            // Centred in the cell, so a small room does not sit against one wall of a big gap.
            int ox = worldOffset.getX() + gx * CELL + (CELL - size.getX()) / 2;
            int oz = worldOffset.getZ() + gz * CELL + (CELL - size.getZ()) / 2;
            BlockPos origin = new BlockPos(ox, BASE, oz);

            StructurePlaceSettings settings = new StructurePlaceSettings()
                    .setRotation(Rotation.NONE)
                    .setIgnoreEntities(true);
            try {
                template.placeInWorld(level, origin, origin, settings,
                        RandomSource.create(seed + i), Block.UPDATE_CLIENTS);
            } catch (Exception e) {
                PoliticalServer.LOGGER.warn("Dungeon room failed to place: " + room.id()
                        + " (" + e.getMessage() + ")");
                continue;
            }

            Placed p = new Placed();
            p.room = room;
            p.gx = gx;
            p.gz = gz;
            p.origin = origin;
            p.size = size;
            int found = measureFloor(level, origin, size);
            p.floorMeasured = found != NO_FLOOR;
            p.floorY = p.floorMeasured ? found : origin.getY();
            layout.rooms.add(p);
            layout.byCell.put(key(gx, gz), p);
        }

        if (layout.rooms.isEmpty()) return layout;

        // ── Roles ──
        assignKinds(layout, floor, rng);

        // ── Connect ──
        connect(level, layout, floor, rng);

        layout.entrance = layout.rooms.get(0).centre();
        return layout;
    }

    /**
     * Picks which cells get rooms, by walking rather than by scattering.
     *
     * <p>A random scatter leaves islands, and an island in a dungeon is a room nobody can reach. A
     * walk that only ever steps to a neighbour cannot produce one, so connectivity is a property of
     * how the cells were chosen rather than something to verify afterwards.</p>
     */
    private static List<int[]> walkCells(int want, Random rng) {
        List<int[]> out = new ArrayList<>();
        Set<Long> used = new HashSet<>();
        int gx = 0;
        int gz = 0;
        used.add(key(gx, gz));
        out.add(new int[]{gx, gz});

        int guard = 0;
        while (out.size() < want && guard++ < want * 40) {
            // Step from a cell already in the layout, so the walk cannot strand itself in a corner.
            int[] from = out.get(rng.nextInt(out.size()));
            int dir = rng.nextInt(4);
            int nx = from[0] + (dir == 0 ? 1 : dir == 1 ? -1 : 0);
            int nz = from[1] + (dir == 2 ? 1 : dir == 3 ? -1 : 0);
            if (nx < 0 || nz < 0 || nx >= GRID || nz >= GRID) continue;
            if (!used.add(key(nx, nz))) continue;
            out.add(new int[]{nx, nz});
        }
        return out;
    }

    /**
     * Finds the first surface you could stand on inside a room.
     *
     * <p>Measured, not assumed. Some of these rooms sit on a foundation and some start at their own
     * floor, so a corridor cut at a fixed height would open into solid rock in one room and into
     * thin air in the next.</p>
     */
    private static int measureFloor(ServerLevel level, BlockPos origin, Vec3i size) {
        // Nine columns rather than one. The first version sampled only the middle, and a room with
        // a pillar, a chest, a pool or a hole at its centre never found a surface: about a third of
        // them fell back to the raw base, which would have opened their corridors at the wrong
        // height. Sampling a spread and taking the commonest answer survives all of that.
        int top = origin.getY() + size.getY();
        Map<Integer, Integer> votes = new HashMap<>();

        for (int fx = 1; fx <= 3; fx++) {
            for (int fz = 1; fz <= 3; fz++) {
                int sx = origin.getX() + size.getX() * fx / 4;
                int sz = origin.getZ() + size.getZ() * fz / 4;
                for (int y = origin.getY(); y < top - 1; y++) {
                    BlockPos at = new BlockPos(sx, y, sz);
                    if (level.getBlockState(at).isAir()) continue;
                    if (!level.getBlockState(at.above()).isAir()) continue;
                    if (!level.getBlockState(at.above(2)).isAir()) continue;
                    votes.merge(y, 1, Integer::sum);
                    break;
                }
            }
        }

        if (votes.isEmpty()) {
            // Genuinely nothing to stand on anywhere across the room. The caller falls back to the
            // structure's own base, and records that it had to.
            return NO_FLOOR;
        }

        // The commonest height wins, and the lowest wins a tie, because a corridor that arrives
        // slightly low leaves a step up rather than a drop into a ceiling.
        int best = Integer.MAX_VALUE;
        int bestVotes = -1;
        for (Map.Entry<Integer, Integer> e : votes.entrySet()) {
            if (e.getValue() > bestVotes || (e.getValue() == bestVotes && e.getKey() < best)) {
                best = e.getKey();
                bestVotes = e.getValue();
            }
        }
        return best;
    }

    /**
     * Gives each room a job.
     *
     * <p>The entrance is the first room placed, which is the one the walk started from, and the
     * boss is the room furthest from it in grid terms so the walk to it is the longest available.
     * Everything between is dealt out.</p>
     */
    private static void assignKinds(Layout layout, DungeonFloor floor, Random rng) {
        Placed entrance = layout.rooms.get(0);
        entrance.kind = DungeonRoomKind.ENTRANCE;

        Placed boss = entrance;
        int best = -1;
        for (Placed p : layout.rooms) {
            int d = Math.abs(p.gx - entrance.gx) + Math.abs(p.gz - entrance.gz);
            if (d > best) {
                best = d;
                boss = p;
            }
        }
        if (boss != entrance) boss.kind = DungeonRoomKind.BOSS;
        layout.bossRoom = boss;

        List<Placed> rest = new ArrayList<>();
        for (Placed p : layout.rooms) {
            if (p.kind == DungeonRoomKind.NORMAL) rest.add(p);
        }
        Collections.shuffle(rest, rng);

        // Proportions kept close to what a Skyblock floor feels like: mostly fighting, a few rooms
        // that ask something else, and one hard one before the boss.
        int i = 0;
        int puzzles = Math.max(1, rest.size() / 8);
        int traps = Math.max(1, rest.size() / 8);
        int treasure = Math.max(1, rest.size() / 7);
        int miniboss = rest.size() >= 8 ? 1 : 0;

        for (int n = 0; n < puzzles && i < rest.size(); n++) rest.get(i++).kind = DungeonRoomKind.PUZZLE;
        for (int n = 0; n < traps && i < rest.size(); n++) rest.get(i++).kind = DungeonRoomKind.TRAP;
        for (int n = 0; n < treasure && i < rest.size(); n++) rest.get(i++).kind = DungeonRoomKind.TREASURE;
        for (int n = 0; n < miniboss && i < rest.size(); n++) rest.get(i++).kind = DungeonRoomKind.MINIBOSS;
    }

    // ── Corridors ─────────────────────────────────────────────────────────

    /**
     * Cuts corridors between every pair of rooms in neighbouring cells.
     *
     * <p>Every neighbouring pair, not a spanning tree. A dungeon whose rooms form a single thread is
     * a corridor with rooms attached; the loops are what make it feel like a building and what make
     * a wrong turn possible.</p>
     */
    private static void connect(ServerLevel level, Layout layout, DungeonFloor floor, Random rng) {
        Set<Long> done = new HashSet<>();
        for (Placed a : layout.rooms) {
            for (int[] d : new int[][]{{1, 0}, {0, 1}}) {
                Placed b = layout.byCell.get(key(a.gx + d[0], a.gz + d[1]));
                if (b == null) continue;
                long edge = key(a.gx * GRID + a.gz, b.gx * GRID + b.gz);
                if (!done.add(edge)) continue;
                carve(level, a, b, floor);
            }
        }
    }

    /**
     * One corridor, from the middle of A to the middle of B.
     *
     * <p>Walks the straight line between the two centres one block at a time, interpolating the
     * floor height as it goes. Where it is inside a room it only clears headroom, which is what
     * turns the last few blocks of the walk into a doorway through that room's wall; where it is
     * between rooms it lays a floor, walls and a ceiling.</p>
     */
    private static void carve(ServerLevel level, Placed a, Placed b, DungeonFloor floor) {
        BlockPos from = a.centre();
        BlockPos to = b.centre();

        int dx = Integer.signum(to.getX() - from.getX());
        int dz = Integer.signum(to.getZ() - from.getZ());

        // An L shaped path: all the way along X, then all the way along Z. Straight lines read as
        // architecture; a diagonal staircase of blocks does not.
        List<BlockPos> path = new ArrayList<>();
        int x = from.getX();
        int z = from.getZ();
        while (x != to.getX()) {
            x += dx;
            path.add(new BlockPos(x, 0, z));
        }
        while (z != to.getZ()) {
            z += dz;
            path.add(new BlockPos(x, 0, z));
        }
        if (path.isEmpty()) return;

        for (int i = 0; i < path.size(); i++) {
            BlockPos step = path.get(i);
            double t = (double) i / Math.max(1, path.size() - 1);
            int y = (int) Math.round(a.floorY + (b.floorY - a.floorY) * t);

            boolean insideRoom = within(step.getX(), step.getZ(), a) || within(step.getX(), step.getZ(), b);
            // Perpendicular to the direction of travel, so the corridor is always three wide.
            boolean alongX = i < Math.abs(to.getX() - from.getX());
            cutSlice(level, step.getX(), y, step.getZ(), alongX, insideRoom, floor);
        }
    }

    private static boolean within(int x, int z, Placed p) {
        return x >= p.origin.getX() && x < p.origin.getX() + p.size.getX()
                && z >= p.origin.getZ() && z < p.origin.getZ() + p.size.getZ();
    }

    /**
     * One block of corridor: floor, air, walls, ceiling.
     *
     * <p>Inside a room only the air is cut, because laying a stone floor through somebody's build
     * would look exactly like what it is.</p>
     */
    private static void cutSlice(ServerLevel level, int x, int y, int z,
                                 boolean alongX, boolean insideRoom, DungeonFloor floor) {
        for (int off = -HALL_HALF; off <= HALL_HALF; off++) {
            int px = alongX ? x : x + off;
            int pz = alongX ? z + off : z;

            for (int h = 1; h <= HALL_HEIGHT; h++) {
                set(level, px, y + h, pz, Blocks.AIR.defaultBlockState());
            }
            if (insideRoom) continue;

            set(level, px, y, pz, floor.corridorFloor.defaultBlockState());
            set(level, px, y + HALL_HEIGHT + 1, pz, floor.corridorWall.defaultBlockState());

            // Side walls only on the outside edges of the run.
            if (off == -HALL_HALF || off == HALL_HALF) {
                for (int h = 1; h <= HALL_HEIGHT; h++) {
                    int wx = alongX ? px : px + (off < 0 ? -1 : 1);
                    int wz = alongX ? pz + (off < 0 ? -1 : 1) : pz;
                    if (level.getBlockState(new BlockPos(wx, y + h, wz)).isAir()) {
                        set(level, wx, y + h, wz, floor.corridorWall.defaultBlockState());
                    }
                }
            }
        }

        // A light every so often, so a corridor is not a dark tunnel with a mob in it.
        if (!insideRoom && Math.floorMod(x + z, 7) == 0) {
            set(level, x, y + HALL_HEIGHT + 1, z, floor.corridorLight.defaultBlockState());
        }
    }

    private static void set(ServerLevel level, int x, int y, int z, BlockState state) {
        level.setBlock(new BlockPos(x, y, z), state, Block.UPDATE_CLIENTS);
    }
}
