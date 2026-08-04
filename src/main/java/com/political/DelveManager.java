package com.political;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Starting, running and ending delve runs.
 *
 * <p>Runs are instanced but share one dimension, spaced far enough apart that two parties never see
 * each other. A dimension per run would be a folder per run on disk, and a server up for a month
 * would be carrying hundreds of them.</p>
 *
 * <p>Which floors a player has cleared is the one thing that survives a run, so it is the one thing
 * that gets saved.</p>
 */
public final class DelveManager {

    private DelveManager() {}

    /** The dimension every instance is built in. */
    public static final ResourceKey<Level> REALM = ResourceKey.create(Registries.DIMENSION,
            Identifier.fromNamespaceAndPath("politicalserver", "delve_realm"));

    /** How far apart two instances sit. Comfortably past any render distance. */
    private static final int SLOT_PITCH = 4096;

    /** Runs are abandoned after this long, so a party that logs out does not hold a slot forever. */
    private static final long RUN_TIMEOUT_TICKS = 20L * 60 * 90;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path savePath;

    private static final Map<UUID, DelveRun> RUNS = new HashMap<>();
    /** Player to run, so a lookup during combat is one map hit rather than a scan. */
    private static final Map<UUID, UUID> PLAYER_RUN = new HashMap<>();
    private static final Set<Integer> USED_SLOTS = new HashSet<>();

    /** Highest floor tier each player has cleared. */
    private static final Map<UUID, Integer> PROGRESS = new HashMap<>();
    /** Best score per player per floor, kept for the menu. */
    private static final Map<String, String> BEST = new HashMap<>();

    // ── Lifecycle ─────────────────────────────────────────────────────────

    public static void initialize(Path worldRoot) {
        savePath = worldRoot.resolve("political").resolve("delves.json");
        DelveCatalog.load();
        load();
    }

    public static ServerLevel realm(MinecraftServer server) {
        return server.getLevel(REALM);
    }

    /** How far a player has climbed. Zero means they have cleared nothing. */
    public static int progressOf(ServerPlayer p) {
        return PROGRESS.getOrDefault(p.getUUID(), 0);
    }

    public static boolean canEnter(ServerPlayer p, DelveDepth floor) {
        return floor.tier <= progressOf(p) + 1;
    }

    public static String bestScore(ServerPlayer p, DelveDepth floor) {
        return BEST.get(p.getUUID() + ":" + floor.name());
    }

    public static DelveRun runOf(ServerPlayer p) {
        UUID r = PLAYER_RUN.get(p.getUUID());
        return r == null ? null : RUNS.get(r);
    }

    public static boolean inDelve(ServerPlayer p) {
        return p.level().dimension().equals(REALM);
    }

    public static int activeRuns() {
        return RUNS.size();
    }

    // ── Starting ──────────────────────────────────────────────────────────

    /**
     * Builds a floor and moves the party into it.
     *
     * <p>Returns a reason on refusal rather than a boolean, so the caller can say why instead of
     * silently doing nothing, which is the failure mode people report as "the button is broken".</p>
     */
    public static String start(ServerPlayer leader, DelveDepth floor, List<ServerPlayer> party) {
        MinecraftServer server = PoliticalServer.server;
        if (server == null) return "The server is not ready.";

        ServerLevel level = realm(server);
        if (level == null) {
            return "The delve dimension is missing. It needs a restart after installing.";
        }
        if (!canEnter(leader, floor)) {
            DelveDepth prev = floor.previous();
            return "Clear " + (prev == null ? "the first floor" : prev.display) + " first.";
        }
        if (runOf(leader) != null) return "You are already in a run.";
        if (DelveCatalog.countFor(floor) < 4) {
            return "That floor has no rooms installed.";
        }

        int slot = claimSlot();
        long seed = leader.getUUID().getLeastSignificantBits() ^ server.getTickCount() ^ floor.tier;
        DelveRun run = new DelveRun(floor, slot, seed);

        BlockPos offset = new BlockPos(slot * SLOT_PITCH, 0, 0);
        run.layout = DelveBuilder.build(server, level, floor, offset, seed);
        if (run.layout.rooms.isEmpty()) {
            USED_SLOTS.remove(slot);
            return "The floor would not build. Nothing was changed.";
        }

        run.entrance = run.layout.entrance;
        run.startedTick = server.getTickCount();
        for (DelveBuilder.Placed p : run.layout.rooms) {
            if (p.kind.countsForClear()) run.roomsTotal++;
        }

        DelvePopulator.populate(level, run);
        DelveMechanics.raiseGate(level, run);

        for (ServerPlayer p : party) {
            if (runOf(p) != null) continue;
            run.party.add(p.getUUID());
            run.returns.put(p.getUUID(), DelveRun.ReturnPoint.of(p));
            PLAYER_RUN.put(p.getUUID(), run.id);
            send(p, level, run.entrance);
        }
        if (run.party.isEmpty()) {
            USED_SLOTS.remove(slot);
            return "Nobody was able to enter.";
        }

        RUNS.put(run.id, run);

        for (ServerPlayer p : run.online()) {
            p.sendSystemMessage(Component.literal(""));
            p.sendSystemMessage(Component.literal("  " + floor.colour + "§l"
                    + floor.display.toUpperCase(java.util.Locale.ROOT)));
            p.sendSystemMessage(Component.literal("  §7" + floor.flavour));
            p.sendSystemMessage(Component.literal("  §8" + run.roomsTotal + " rooms, "
                    + run.secretsTotal + " secrets, one way out"));
            p.sendSystemMessage(Component.literal(""));
        }
        return null;
    }

    private static int claimSlot() {
        for (int i = 1; i < 4096; i++) {
            if (USED_SLOTS.add(i)) return i;
        }
        return 1;
    }

    private static void send(ServerPlayer p, ServerLevel level, BlockPos to) {
        PlayerTeleportHelper.teleport(p, level, to.getX() + 0.5, to.getY(), to.getZ() + 0.5,
                p.getYRot(), p.getXRot());
    }

    // ── Ending ────────────────────────────────────────────────────────────

    /** Puts one player back where they came from and drops them from the run. */
    public static void leave(ServerPlayer p, boolean quiet) {
        DelveRun run = runOf(p);
        if (run == null) return;
        PLAYER_RUN.remove(p.getUUID());

        DelveRun.ReturnPoint rp = run.returns.get(p.getUUID());
        if (rp != null && PoliticalServer.server != null) {
            try {
                ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION,
                        Identifier.parse(rp.dimension()));
                ServerLevel back = PoliticalServer.server.getLevel(key);
                if (back != null) {
                    PlayerTeleportHelper.teleport(p, back, rp.x(), rp.y(), rp.z(),
                            rp.yaw(), rp.pitch());
                }
            } catch (Exception ignored) {
                // A dimension that has gone since they entered. Better left where they are than
                // thrown at world spawn from inside a delve.
            }
        }
        if (!quiet) {
            p.sendSystemMessage(Component.literal("§7You leave the delve."));
        }

        // The last one out closes it.
        if (run.online().isEmpty()) {
            end(run, false);
        }
    }

    /** Finishes a run: scores it, pays it, and sends everybody home. */
    public static void end(DelveRun run, boolean completed) {
        if (run.finished) return;
        run.finished = true;
        run.endedTick = PoliticalServer.server == null ? run.startedTick
                : PoliticalServer.server.getTickCount();

        if (completed) {
            DelveScore.award(run);
            for (UUID u : run.party) {
                int had = PROGRESS.getOrDefault(u, 0);
                if (run.floor.tier > had) PROGRESS.put(u, run.floor.tier);
            }
            save();
        }

        for (ServerPlayer p : run.online()) {
            PLAYER_RUN.remove(p.getUUID());
            DelveRun.ReturnPoint rp = run.returns.get(p.getUUID());
            if (rp != null && PoliticalServer.server != null) {
                try {
                    ServerLevel back = PoliticalServer.server.getLevel(ResourceKey.create(
                            Registries.DIMENSION, Identifier.parse(rp.dimension())));
                    if (back != null) {
                        PlayerTeleportHelper.teleport(p, back, rp.x(), rp.y(), rp.z(),
                                rp.yaw(), rp.pitch());
                    }
                } catch (Exception ignored) {
                    // As above.
                }
            }
        }
        for (UUID u : run.party) PLAYER_RUN.remove(u);

        DelveBossFight.forget(run);
        DelveMechanics.forget(run);
        DelvePuzzles.forget(run);
        DelvePopulator.despawn(run);
        USED_SLOTS.remove(run.slot);
        RUNS.remove(run.id);
    }

    /** Records a best score, keeping the better of the two. */
    public static void recordScore(UUID player, DelveDepth floor, String grade) {
        String k = player + ":" + floor.name();
        String had = BEST.get(k);
        if (had == null || DelveScore.betterThan(grade, had)) {
            BEST.put(k, grade);
        }
    }

    // ── Tick ──────────────────────────────────────────────────────────────

    public static void tick(MinecraftServer server) {
        if (RUNS.isEmpty()) return;
        if (server.getTickCount() % 20 != 0) return;

        for (DelveRun run : new ArrayList<>(RUNS.values())) {
            List<ServerPlayer> here = run.online();
            if (here.isEmpty()) {
                end(run, false);
                continue;
            }
            if (server.getTickCount() - run.startedTick > RUN_TIMEOUT_TICKS) {
                for (ServerPlayer p : here) {
                    p.sendSystemMessage(Component.literal("§cThe delve closes behind you."));
                }
                end(run, false);
                continue;
            }

            // Anybody who wandered out of the realm has effectively left.
            for (ServerPlayer p : here) {
                if (!inDelve(p)) {
                    leave(p, true);
                }
            }

            DelvePopulator.tick(server, run);
            DelveBossFight.tick(server, run);
            DelveMechanics.tickHazards(server, run);

            // The boss is sealed until the floor is cleared, which is what makes the rooms matter.
            if (!run.bossSpawned && run.roomsDone() && DelvePuzzles.allSolved(run)
                    && run.layout.bossRoom != null) {
                run.bossSpawned = true;
                ServerLevel realm = realm(server);
                if (realm != null) DelveMechanics.openGate(realm, run);
                DelveBossFight.spawn(server, run);
            }

            for (ServerPlayer p : run.online()) {
                p.sendSystemMessage(Component.literal(
                        run.floor.colour + "✦ §7" + run.roomsCleared + "/" + run.roomsTotal
                                + " rooms  §b" + run.secretsFound + "/" + run.secretsTotal
                                + " secrets" + (DelvePuzzles.remaining(run) > 0
                                        ? "  §e" + DelvePuzzles.remaining(run) + " puzzles" : "")
                                + "  §f" + run.elapsedText(server.getTickCount())), true);
            }
        }
    }

    /** A death inside a run costs score and respawns the player at the entrance. */
    public static void onDeath(ServerPlayer p) {
        DelveRun run = runOf(p);
        if (run == null) return;
        run.deaths++;
        for (ServerPlayer m : run.online()) {
            m.sendSystemMessage(Component.literal("§c☠ §7" + p.getName().getString()
                    + " died. §8(" + run.deaths + ")"));
        }
    }

    /** Where a player who died inside a run should come back. */
    public static BlockPos respawnFor(ServerPlayer p) {
        DelveRun run = runOf(p);
        return run == null ? null : run.entrance;
    }

    public static void onDisconnect(ServerPlayer p) {
        DelveRun run = runOf(p);
        if (run == null) return;
        PLAYER_RUN.remove(p.getUUID());
        if (run.online().isEmpty()) end(run, false);
    }

    // ── Persistence ───────────────────────────────────────────────────────

    public static void save() {
        if (savePath == null) return;
        try {
            Files.createDirectories(savePath.getParent());
            JsonObject root = new JsonObject();
            JsonObject prog = new JsonObject();
            List<UUID> keys = new ArrayList<>(PROGRESS.keySet());
            keys.sort(java.util.Comparator.comparing(UUID::toString));
            for (UUID u : keys) prog.addProperty(u.toString(), PROGRESS.get(u));
            root.add("progress", prog);

            JsonObject best = new JsonObject();
            List<String> bk = new ArrayList<>(BEST.keySet());
            java.util.Collections.sort(bk);
            for (String k : bk) best.addProperty(k, BEST.get(k));
            root.add("best", best);

            try (Writer w = Files.newBufferedWriter(savePath, StandardCharsets.UTF_8)) {
                GSON.toJson(root, w);
            }
        } catch (Exception e) {
            PoliticalServer.LOGGER.error("Could not save delve progress: " + e.getMessage());
        }
    }

    private static void load() {
        PROGRESS.clear();
        BEST.clear();
        if (savePath == null || !Files.exists(savePath)) return;
        try (Reader r = Files.newBufferedReader(savePath, StandardCharsets.UTF_8)) {
            JsonElement root = JsonParser.parseReader(r);
            if (!root.isJsonObject()) return;
            JsonObject o = root.getAsJsonObject();
            if (o.has("progress")) {
                for (Map.Entry<String, JsonElement> e : o.getAsJsonObject("progress").entrySet()) {
                    try {
                        PROGRESS.put(UUID.fromString(e.getKey()), e.getValue().getAsInt());
                    } catch (Exception ignored) {
                        // A key that is not a uuid is somebody's hand edit. Skip it.
                    }
                }
            }
            if (o.has("best")) {
                for (Map.Entry<String, JsonElement> e : o.getAsJsonObject("best").entrySet()) {
                    BEST.put(e.getKey(), e.getValue().getAsString());
                }
            }
            PoliticalServer.LOGGER.info("Delve progress loaded for " + PROGRESS.size() + " players");
        } catch (Exception e) {
            PoliticalServer.LOGGER.error("Could not read delve progress: " + e.getMessage());
        }
    }
}
