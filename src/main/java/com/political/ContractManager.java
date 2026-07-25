package com.political;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * The contract board. A rotating set of jobs that gives players a reason to log in without ever
 * paying better than going out and playing.
 *
 * <p>The board reshuffles on a timer, everyone sees the same offers, and a player can only hold a
 * few at once. Rewards are deliberately modest and the accept slots are the throttle, so the board
 * cannot be farmed.</p>
 */
public final class ContractManager {

    private ContractManager() {}

    /** How long a board of offers lasts before it reshuffles. */
    private static final long ROTATION_MS = 2 * 60 * 60 * 1000L;   // two hours
    /** How many contracts are offered at once. */
    private static final int BOARD_SIZE = 8;
    /** How many a single player may hold at once. */
    public static final int MAX_ACTIVE = 3;
    /** How many a player may complete per rotation, which is the real anti farm gate. */
    private static final int MAX_PER_ROTATION = 5;

    public static final class Active {
        public String contract;
        public int progress;
        public long acceptedAt;
    }

    public static final class Profile {
        public List<Active> active = new ArrayList<>();
        public int completedThisRotation = 0;
        public long rotationSeen = 0L;
        public int totalCompleted = 0;
    }

    private static final Map<String, Profile> DATA = new HashMap<>();
    private static final List<String> BOARD = new ArrayList<>();
    private static long boardRolledAt = 0L;

    private static Path file;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // ── Persistence ─────────────────────────────────────────────────────────

    public static void initialize(Path worldRoot) {
        file = worldRoot.resolve("contracts.json");
        if (Files.exists(file)) {
            try (Reader r = Files.newBufferedReader(file)) {
                java.lang.reflect.Type t = new TypeToken<Map<String, Profile>>() {}.getType();
                Map<String, Profile> loaded = GSON.fromJson(r, t);
                if (loaded != null) {
                    DATA.clear();
                    DATA.putAll(loaded);
                }
            } catch (Exception e) {
                PoliticalServer.LOGGER.warn("Failed to load contracts.json", e);
            }
        }
        rollBoard();
    }

    public static void save() {
        if (file == null) return;
        try (Writer w = Files.newBufferedWriter(file)) {
            GSON.toJson(DATA, w);
        } catch (Exception e) {
            PoliticalServer.LOGGER.warn("Failed to save contracts.json", e);
        }
    }

    public static Profile profile(String uuid) {
        Profile p = DATA.computeIfAbsent(uuid, k -> new Profile());
        // A new rotation clears the per rotation completion count.
        if (p.rotationSeen != boardRolledAt) {
            p.rotationSeen = boardRolledAt;
            p.completedThisRotation = 0;
        }
        return p;
    }

    // ── The board ───────────────────────────────────────────────────────────

    private static void rollBoard() {
        BOARD.clear();
        List<Contract> all = new ArrayList<>(List.of(Contract.values()));
        Random rng = new Random(System.currentTimeMillis());
        java.util.Collections.shuffle(all, rng);
        for (int i = 0; i < Math.min(BOARD_SIZE, all.size()); i++) {
            BOARD.add(all.get(i).name());
        }
        boardRolledAt = System.currentTimeMillis();
    }

    public static List<Contract> board() {
        List<Contract> out = new ArrayList<>();
        for (String n : BOARD) {
            Contract c = Contract.byId(n);
            if (c != null) out.add(c);
        }
        return out;
    }

    public static long nextRotationMs() {
        return Math.max(0, (boardRolledAt + ROTATION_MS) - System.currentTimeMillis());
    }

    private static int tickCounter = 0;

    public static void tick(MinecraftServer server) {
        if (server == null) return;
        if (++tickCounter < 200) return;   // ten seconds
        tickCounter = 0;
        if (System.currentTimeMillis() - boardRolledAt >= ROTATION_MS) {
            rollBoard();
            for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                p.sendSystemMessage(Component.literal("§7The contract board has been rewritten.")
                        .withStyle(ChatFormatting.GRAY));
            }
        }
    }

    // ── Accepting and progressing ───────────────────────────────────────────

    public static List<Active> activeOf(String uuid) {
        return profile(uuid).active;
    }

    public static Active find(String uuid, Contract c) {
        for (Active a : activeOf(uuid)) {
            if (a.contract.equals(c.name())) return a;
        }
        return null;
    }

    public static boolean accept(ServerPlayer player, Contract c) {
        String uuid = player.getUUID().toString();
        Profile p = profile(uuid);
        if (find(uuid, c) != null) {
            player.sendSystemMessage(Component.literal("§7You already hold that contract."));
            return false;
        }
        if (p.active.size() >= MAX_ACTIVE) {
            player.sendSystemMessage(Component.literal("§cYou can only hold "
                    + MAX_ACTIVE + " contracts at a time."));
            return false;
        }
        if (p.completedThisRotation >= MAX_PER_ROTATION) {
            player.sendSystemMessage(Component.literal(
                    "§cYou have finished all you can until the board is rewritten."));
            return false;
        }
        Active a = new Active();
        a.contract = c.name();
        a.progress = 0;
        a.acceptedAt = System.currentTimeMillis();
        p.active.add(a);
        save();
        player.sendSystemMessage(Component.literal("§7Contract taken: ")
                .append(Component.literal(c.display).withStyle(c.colour, ChatFormatting.BOLD)));
        return true;
    }

    public static void abandon(ServerPlayer player, Contract c) {
        String uuid = player.getUUID().toString();
        profile(uuid).active.removeIf(a -> a.contract.equals(c.name()));
        save();
        player.sendSystemMessage(Component.literal("§7Contract abandoned."));
    }

    /** Adds progress to every held contract of a given kind. */
    public static void progress(ServerPlayer player, Contract.Kind kind, int amount) {
        String uuid = player.getUUID().toString();
        Profile p = DATA.get(uuid);
        if (p == null || p.active.isEmpty()) return;

        boolean dirty = false;
        for (Active a : new ArrayList<>(p.active)) {
            Contract c = Contract.byId(a.contract);
            if (c == null || c.kind != kind) continue;
            if (a.progress >= c.required) continue;
            a.progress = Math.min(c.required, a.progress + amount);
            dirty = true;
            if (a.progress >= c.required) {
                player.sendSystemMessage(Component.literal("§aContract ready to claim: ")
                        .append(Component.literal(c.display).withStyle(c.colour, ChatFormatting.BOLD)));
            }
        }
        if (dirty) save();
    }

    public static boolean claim(ServerPlayer player, Contract c) {
        String uuid = player.getUUID().toString();
        Profile p = profile(uuid);
        Active a = find(uuid, c);
        if (a == null) return false;
        if (a.progress < c.required) {
            player.sendSystemMessage(Component.literal("§cThat contract is not finished yet."));
            return false;
        }
        p.active.remove(a);
        p.completedThisRotation++;
        p.totalCompleted++;
        save();

        CoinManager.giveCoinsQuiet(player, c.reward);
        CursedEnergyManager.earn(player, c.energyReward());
        CompanionManager.awardXp(player, 20);

        player.sendSystemMessage(Component.literal("§aContract complete: ")
                .append(Component.literal(c.display).withStyle(c.colour, ChatFormatting.BOLD)));
        player.sendSystemMessage(Component.literal("§7Paid §6" + c.reward
                + " §7coins and §b" + c.energyReward() + " §7cursed energy."));
        if (player.level() instanceof ServerLevel level) {
            Vfx.bloom(level, player.position().add(0, 1, 0));
        }
        return true;
    }

    public static int remainingThisRotation(String uuid) {
        return Math.max(0, MAX_PER_ROTATION - profile(uuid).completedThisRotation);
    }

    // ── Hooks ───────────────────────────────────────────────────────────────

    /** Any hostile kill feeds hunt contracts. */
    public static void onKill(ServerPlayer player) {
        progress(player, Contract.Kind.HUNT, 1);
    }

    /** Any custom craft feeds craft contracts. */
    public static void onCraft(ServerPlayer player) {
        progress(player, Contract.Kind.CRAFT, 1);
    }

    /** Finishing an expedition or escaping the backrooms feeds explore contracts. */
    public static void onExplore(ServerPlayer player) {
        progress(player, Contract.Kind.EXPLORE, 1);
    }
}
