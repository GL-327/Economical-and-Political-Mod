package com.political;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * V6.4–V6.5 — the world side of Domain Expansions.
 *
 * <p><b>What it stamps.</b> A domain is a decorative ring of themed accent blocks around the
 * arena edge plus four short pillars at the cardinal points, all placed on the surface. Crucially
 * every stamped block goes into a position that was <b>air</b> — the accent sits on top of the
 * ground, it never overwrites terrain. That single invariant is what makes reverting trivially
 * correct: setting the stamped positions back to air restores the world exactly.
 *
 * <p><b>Why nothing is ever left behind.</b>
 * <ul>
 *   <li>Every active domain holds the exact list of positions it stamped.</li>
 *   <li>Revert (boss death, timeout, summoner logout, {@code /specialgrade}, shutdown) walks that
 *       list and, for each position that still holds the block we placed, sets it to air. Positions
 *       a player has since changed are left alone.</li>
 *   <li>The list is persisted to JSON, so a crash/restart still cleans up: {@link #initialize}
 *       loads every saved domain into a pending-revert queue and {@link #tick} drains it as soon
 *       as the matching level is available. Restart-safe and idempotent.</li>
 * </ul>
 */
public final class DomainManager {

    private DomainManager() {}

    private static final int PILLAR_HEIGHT = 4;

    /** ownerEntity UUID -> live domain. */
    private static final Map<UUID, Active> ACTIVE = new ConcurrentHashMap<>();

    /** Domains recovered from disk on boot; reverted on the first tick that has their level. */
    private static final List<Record> PENDING_REVERT = new ArrayList<>();

    private static Path dataFile;

    // ════════════════════════════════════════════════════════════
    // PERSISTED SHAPE
    // ════════════════════════════════════════════════════════════

    /** The minimum needed to revert a domain after a restart. */
    private static final class Record {
        String dim;
        List<Long> stamped = new ArrayList<>();
        String accentBlock;   // registry path of the accent, for a match-before-remove safety check
        String pillarBlock;
    }

    /** A live, tracked domain. */
    private static final class Active {
        final UUID ownerEntity;
        final String dim;
        final BlockPos center;
        final int radius;
        final DomainExpansion.Theme theme;
        long expiresAtMs;
        final List<BlockPos> stamped = new ArrayList<>();

        Active(UUID ownerEntity, String dim, BlockPos center, int radius,
               DomainExpansion.Theme theme, long expiresAtMs) {
            this.ownerEntity = ownerEntity;
            this.dim = dim;
            this.center = center;
            this.radius = radius;
            this.theme = theme;
            this.expiresAtMs = expiresAtMs;
        }

        Record toRecord() {
            Record r = new Record();
            r.dim = dim;
            r.accentBlock = pathOf(theme.accent.defaultBlockState());
            r.pillarBlock = pathOf(theme.pillar.defaultBlockState());
            for (BlockPos p : stamped) r.stamped.add(p.asLong());
            return r;
        }
    }

    private static class SaveBlob {
        List<Record> domains = new ArrayList<>();
    }

    // ════════════════════════════════════════════════════════════
    // LIFECYCLE
    // ════════════════════════════════════════════════════════════

    public static void initialize(Path worldRoot) {
        dataFile = worldRoot.resolve("special_grade_domains.json");
        ACTIVE.clear();
        PENDING_REVERT.clear();

        if (Files.exists(dataFile)) {
            try (Reader r = Files.newBufferedReader(dataFile)) {
                Gson g = new GsonBuilder().create();
                Type t = new TypeToken<SaveBlob>() {}.getType();
                SaveBlob blob = g.fromJson(r, t);
                if (blob != null && blob.domains != null) {
                    // Bosses never survive a restart, so every saved domain is stale: queue it all.
                    PENDING_REVERT.addAll(blob.domains);
                }
            } catch (Exception e) {
                System.err.println("[DomainManager] Failed to load: " + e.getMessage());
            }
        }
        save();
    }

    public static void save() {
        if (dataFile == null) return;
        try {
            Files.createDirectories(dataFile.getParent());
            SaveBlob blob = new SaveBlob();
            for (Active a : ACTIVE.values()) blob.domains.add(a.toRecord());
            Gson g = new GsonBuilder().setPrettyPrinting().create();
            try (Writer w = Files.newBufferedWriter(dataFile)) {
                g.toJson(blob, w);
            }
        } catch (Exception e) {
            System.err.println("[DomainManager] Failed to save: " + e.getMessage());
        }
    }

    /** SERVER_STOPPING: revert every live domain so no arena is left standing. */
    public static void shutdown(MinecraftServer server) {
        for (Active a : new ArrayList<>(ACTIVE.values())) {
            ServerLevel level = levelFor(server, a.dim);
            if (level != null) revertActive(level, a);
        }
        ACTIVE.clear();
        save();
    }

    // ════════════════════════════════════════════════════════════
    // EXPAND / REVERT
    // ════════════════════════════════════════════════════════════

    /**
     * Stamp a themed arena. Idempotent per owner: an existing domain for the same boss is reverted
     * first so blocks never stack up.
     */
    public static void expand(ServerLevel level, DomainExpansion.Blueprint bp) {
        if (level == null || bp == null) return;
        revert(level, bp.ownerEntity);

        Active a = new Active(bp.ownerEntity, bp.dim, bp.center, bp.radius, bp.theme,
                System.currentTimeMillis() + bp.durationMs);

        BlockState accent = bp.theme.accent.defaultBlockState();
        BlockState pillar = bp.theme.pillar.defaultBlockState();

        int cx = bp.center.getX();
        int cz = bp.center.getZ();
        int r = bp.radius;

        // Ring of accent blocks around the edge (stamped on the surface, into air only).
        double step = Math.PI / Math.max(8, r * 2);
        for (double ang = 0; ang < Math.PI * 2; ang += step) {
            int x = cx + (int) Math.round(Math.cos(ang) * r);
            int z = cz + (int) Math.round(Math.sin(ang) * r);
            BlockPos surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, new BlockPos(x, 0, z));
            stampIfAir(level, surface, accent, a);
        }

        // Four short pillars at the cardinal points as gate markers.
        int[][] dirs = {{r, 0}, {-r, 0}, {0, r}, {0, -r}};
        for (int[] d : dirs) {
            BlockPos base = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING,
                    new BlockPos(cx + d[0], 0, cz + d[1]));
            for (int y = 0; y < PILLAR_HEIGHT; y++) {
                stampIfAir(level, base.above(y), pillar, a);
            }
        }

        ACTIVE.put(bp.ownerEntity, a);
        save();
    }

    private static void stampIfAir(ServerLevel level, BlockPos pos, BlockState state, Active a) {
        if (!level.getBlockState(pos).isAir()) return;
        level.setBlock(pos, state, net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
        a.stamped.add(pos.immutable());
    }

    /** Revert (and forget) the domain owned by this boss, if any. */
    public static void revert(ServerLevel level, UUID ownerEntity) {
        Active a = ACTIVE.remove(ownerEntity);
        if (a == null) return;
        if (level != null) revertActive(level, a);
        save();
    }

    private static void revertActive(ServerLevel level, Active a) {
        BlockState accent = a.theme.accent.defaultBlockState();
        BlockState pillar = a.theme.pillar.defaultBlockState();
        for (BlockPos p : a.stamped) {
            BlockState now = level.getBlockState(p);
            // Only remove blocks that are still exactly what we placed — never eat a player's build.
            if (now == accent || now == pillar
                    || now.is(a.theme.accent) || now.is(a.theme.pillar)) {
                level.setBlock(p, Blocks.AIR.defaultBlockState(),
                        net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
            }
        }
    }

    // ════════════════════════════════════════════════════════════
    // TICK
    // ════════════════════════════════════════════════════════════

    public static void tick(MinecraftServer server) {
        if (server == null) return;

        // Restart cleanup: drain one recovered domain per tick as its level becomes available.
        if (!PENDING_REVERT.isEmpty()) {
            for (int i = PENDING_REVERT.size() - 1; i >= 0; i--) {
                Record rec = PENDING_REVERT.get(i);
                ServerLevel level = levelFor(server, rec.dim);
                if (level == null) continue;
                revertRecord(level, rec);
                PENDING_REVERT.remove(i);
            }
        }

        if (ACTIVE.isEmpty()) return;
        long now = System.currentTimeMillis();
        for (Active a : new ArrayList<>(ACTIVE.values())) {
            if (now >= a.expiresAtMs) {
                ServerLevel level = levelFor(server, a.dim);
                revert(level, a.ownerEntity);
            }
        }
    }

    private static void revertRecord(ServerLevel level, Record rec) {
        for (Long packed : rec.stamped) {
            BlockPos p = BlockPos.of(packed);
            String path = pathOf(level.getBlockState(p));
            if (path.equals(rec.accentBlock) || path.equals(rec.pillarBlock)) {
                level.setBlock(p, Blocks.AIR.defaultBlockState(),
                        net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
            }
        }
    }

    // ════════════════════════════════════════════════════════════
    // QUERIES (used by SpecialGradeBossManager)
    // ════════════════════════════════════════════════════════════

    public static boolean hasDomain(UUID ownerEntity) {
        return ACTIVE.containsKey(ownerEntity);
    }

    public static DomainExpansion.Theme themeOf(UUID ownerEntity) {
        Active a = ACTIVE.get(ownerEntity);
        return a == null ? null : a.theme;
    }

    /** True if {@code pos} is inside the boss's active domain radius (cylinder, generous height). */
    public static boolean isInside(UUID ownerEntity, BlockPos pos) {
        Active a = ACTIVE.get(ownerEntity);
        if (a == null) return false;
        double dx = pos.getX() - a.center.getX();
        double dz = pos.getZ() - a.center.getZ();
        double dy = pos.getY() - a.center.getY();
        return (dx * dx + dz * dz) <= (a.radius * a.radius) && Math.abs(dy) <= 20;
    }

    public static BlockPos centerOf(UUID ownerEntity) {
        Active a = ACTIVE.get(ownerEntity);
        return a == null ? null : a.center;
    }

    public static int activeCount() {
        return ACTIVE.size();
    }

    // ════════════════════════════════════════════════════════════
    // HELPERS
    // ════════════════════════════════════════════════════════════

    private static ServerLevel levelFor(MinecraftServer server, String dim) {
        if (server == null || dim == null) return null;
        for (ServerLevel l : server.getAllLevels()) {
            if (l.dimension().identifier().toString().equals(dim)) return l;
        }
        return null;
    }

    private static String pathOf(BlockState state) {
        var id = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return id == null ? "" : id.getPath();
    }
}
