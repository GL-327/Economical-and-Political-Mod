package com.political;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.political.economy.CoinManager;
import com.political.settlement.Settlement;
import com.political.settlement.SettlementManager;
import com.political.settlement.SettlementTier;
import com.political.stats.HealthScalingManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * V5 Pillar 1 — the Expedition run lifecycle.
 *
 * <p>Named {@code ExpeditionRunManager} deliberately: the pre-existing {@code ExpeditionManager}
 * is orphaned (nothing ticks it, nothing calls it) and is left completely untouched, so there
 * is no duplicate-symbol clash and no behaviour change for anything that might reference it.
 *
 * <p><b>Anti-AFK design.</b> Nothing in this system pays out for elapsed time:
 * <ul>
 *   <li>Rooms only arm when a player physically walks into them, so mobs cannot pile up
 *       around an idle player.</li>
 *   <li>All coin/shard/gear payout happens once, on completion, behind {@code lootRolled}.</li>
 *   <li>Every run has a hard wall-clock deadline; blowing it fails the run for zero reward.</li>
 *   <li>A per-player cooldown gates the next launch; abandoning still costs half of it.</li>
 *   <li>Vault chests are stamped empty and only filled after their guards die.</li>
 * </ul>
 */
public final class ExpeditionRunManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExpeditionRunManager.class);


    private ExpeditionRunManager() {}

    // ════════════════════════════════════════════════════════════
    // TIERS
    // ════════════════════════════════════════════════════════════

    public enum Tier {
        DELVE      (1, "Delve",      ChatFormatting.GREEN,        1, 15,  4, HealthScalingManager.ScalingTier.REINFORCED, null),
        DESCENT    (2, "Descent",    ChatFormatting.YELLOW,       1, 20,  5, HealthScalingManager.ScalingTier.ELITE,      null),
        ABYSS      (3, "Abyss",      ChatFormatting.GOLD,         2, 30,  6, HealthScalingManager.ScalingTier.CHAMPION,   SettlementTier.CITY),
        CATACLYSM  (4, "Cataclysm",  ChatFormatting.RED,          2, 40,  7, HealthScalingManager.ScalingTier.LEGENDARY,  SettlementTier.METROPOLIS),
        APOTHEOSIS (5, "Apotheosis", ChatFormatting.LIGHT_PURPLE, 3, 55,  9, HealthScalingManager.ScalingTier.APEX,       SettlementTier.MEGALOPOLIS);

        public final int level;
        public final String displayName;
        public final ChatFormatting color;
        public final int floors;
        public final int timeLimitMinutes;
        public final int mobsPerRoom;
        public final HealthScalingManager.ScalingTier scaling;
        /** Minimum tier of the settlement the player must be registered to, or null for none. */
        public final SettlementTier requiredSettlement;

        Tier(int level, String displayName, ChatFormatting color, int floors, int timeLimitMinutes,
             int mobsPerRoom, HealthScalingManager.ScalingTier scaling, SettlementTier requiredSettlement) {
            this.level = level;
            this.displayName = displayName;
            this.color = color;
            this.floors = floors;
            this.timeLimitMinutes = timeLimitMinutes;
            this.mobsPerRoom = mobsPerRoom;
            this.scaling = scaling;
            this.requiredSettlement = requiredSettlement;
        }

        /** Cooldown between runs, in minutes. */
        public int cooldownMinutes() {
            return switch (this) {
                case DELVE -> 15;
                case DESCENT -> 25;
                case ABYSS -> 40;
                case CATACLYSM -> 60;
                case APOTHEOSIS -> 90;
            };
        }

        public String modifierLine() {
            return switch (this) {
                case DELVE -> "§7Mobs are Reinforced. One floor.";
                case DESCENT -> "§7Mobs are Elite. One floor, tighter clock.";
                case ABYSS -> "§7Mobs are Champions. Two floors.";
                case CATACLYSM -> "§7Mobs are LEGENDARY. Two floors, heavy packs.";
                case APOTHEOSIS -> "§7Mobs are APEX. Three floors. No mercy.";
            };
        }

        public static Tier byLevel(int level) {
            for (Tier t : values()) if (t.level == level) return t;
            return DELVE;
        }

        public static Tier byName(String name) {
            if (name == null) return DELVE;
            for (Tier t : values()) if (t.name().equalsIgnoreCase(name)) return t;
            return DELVE;
        }
    }

    // ════════════════════════════════════════════════════════════
    // STATE
    // ════════════════════════════════════════════════════════════

    /** runId -> dungeon */
    private static final Map<String, ExpeditionDungeon> RUNS = new ConcurrentHashMap<>();
    /** playerUuid -> runId */
    private static final Map<String, String> PLAYER_RUN = new ConcurrentHashMap<>();
    /** playerUuid -> epoch millis at which the next run is allowed */
    private static final Map<String, Long> COOLDOWNS = new ConcurrentHashMap<>();
    /** playerUuid -> completed runs, per tier level */
    private static final Map<String, Map<String, Integer>> CLEARS = new ConcurrentHashMap<>();

    private static final Map<String, ServerBossEvent> BOSS_BARS = new ConcurrentHashMap<>();
    private static final Set<Integer> USED_SLOTS = ConcurrentHashMap.newKeySet();

    /** Instances left behind by a crash/restart; wiped on the first tick that has a level. */
    private static final List<int[]> PENDING_WIPES = new ArrayList<>();

    private static Path dataFile;
    private static int tickCounter = 0;
    private static final Random RNG = new Random();

    private static final int TICK_INTERVAL = 20;      // run the whole state machine once a second
    private static final long FLOOR_ADVANCE_DELAY_MS = 5_000L;

    // ════════════════════════════════════════════════════════════
    // PERSISTENCE
    // ════════════════════════════════════════════════════════════

    private static class SaveBlob {
        List<ExpeditionDungeon> runs = new ArrayList<>();
        Map<String, Long> cooldowns = new HashMap<>();
        Map<String, Map<String, Integer>> clears = new HashMap<>();
    }

    public static void initialize(Path worldRoot) {
        dataFile = worldRoot.resolve("expeditions_v5.json");
        RUNS.clear();
        PLAYER_RUN.clear();
        COOLDOWNS.clear();
        CLEARS.clear();
        USED_SLOTS.clear();
        BOSS_BARS.clear();
        PENDING_WIPES.clear();

        if (!Files.exists(dataFile)) return;
        try (Reader r = Files.newBufferedReader(dataFile)) {
            Gson g = new GsonBuilder().create();
            Type t = new TypeToken<SaveBlob>(){}.getType();
            SaveBlob blob = g.fromJson(r, t);
            if (blob == null) return;

            if (blob.cooldowns != null) COOLDOWNS.putAll(blob.cooldowns);
            if (blob.clears != null) CLEARS.putAll(blob.clears);

            // Runs never survive a restart: the players are gone, the mobs are gone, and the
            // instance geometry is stale. Queue every saved instance for a wipe instead.
            if (blob.runs != null) {
                for (ExpeditionDungeon d : blob.runs) {
                    if (d == null) continue;
                    PENDING_WIPES.add(new int[]{d.originX, d.originZ});
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load", e);
        }
        save();
    }

    public static void save() {
        if (dataFile == null) return;
        try {
            Files.createDirectories(dataFile.getParent());
            SaveBlob blob = new SaveBlob();
            blob.runs = new ArrayList<>(RUNS.values());
            blob.cooldowns = new LinkedHashMap<>(COOLDOWNS);
            blob.clears = new LinkedHashMap<>(CLEARS);
            Gson g = new GsonBuilder().setPrettyPrinting().create();
            try (Writer w = Files.newBufferedWriter(dataFile)) {
                g.toJson(blob, w);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to save", e);
        }
    }

    /** Called from SERVER_STOPPING: end every run cleanly so no instance is orphaned. */
    public static void shutdown(MinecraftServer server) {
        for (ExpeditionDungeon d : new ArrayList<>(RUNS.values())) {
            endRun(server, d, EndReason.SHUTDOWN);
        }
        save();
    }

    // ════════════════════════════════════════════════════════════
    // QUERIES
    // ════════════════════════════════════════════════════════════

    public static ExpeditionDungeon runOf(String uuid) {
        String id = PLAYER_RUN.get(uuid);
        return id == null ? null : RUNS.get(id);
    }

    public static boolean inRun(String uuid) {
        return runOf(uuid) != null;
    }

    public static long cooldownRemaining(String uuid) {
        Long until = COOLDOWNS.get(uuid);
        if (until == null) return 0L;
        return Math.max(0L, until - System.currentTimeMillis());
    }

    public static int clearsOf(String uuid, Tier tier) {
        Map<String, Integer> m = CLEARS.get(uuid);
        return m == null ? 0 : m.getOrDefault(tier.name(), 0);
    }

    public static int activeRunCount() {
        return RUNS.size();
    }

    public static String formatDuration(long millis) {
        long s = Math.max(0L, millis) / 1000L;
        long m = s / 60L;
        long h = m / 60L;
        if (h > 0) return h + "h " + (m % 60) + "m";
        if (m > 0) return m + "m " + (s % 60) + "s";
        return s + "s";
    }

    /** The settlement a player may launch a high tier from (registered home, else standing-in). */
    public static Settlement launchSettlement(ServerPlayer player) {
        Settlement home = SettlementManager.getRegisteredSettlement(player.getUUID().toString());
        if (home != null) return home;
        return SettlementManager.getContaining(
                ((ServerLevel) player.level()).dimension().identifier().toString(),
                player.blockPosition());
    }

    /** Human-readable reason the player cannot launch this tier, or null if they can. */
    public static String blockReason(ServerPlayer player, Tier tier) {
        String uuid = player.getUUID().toString();
        if (inRun(uuid)) return "You are already inside an expedition.";
        if (tier.requiredSettlement != null) {
            Settlement s = launchSettlement(player);
            if (s == null || !s.tierEnum().isAtLeast(tier.requiredSettlement)) {
                return "Requires a " + tier.requiredSettlement.displayName
                        + "+ settlement to launch from.";
            }
        }
        long cd = cooldownRemaining(uuid);
        if (cd > 0 && ExpeditionRewards.countKeys(player) <= 0) {
            return "On cooldown for " + formatDuration(cd) + " (or spend an Expedition Key).";
        }
        if (CoinManager.getCoinsLong(player) < ExpeditionRewards.entryCost(tier.level)) {
            return "Entry costs " + String.format("%,d", ExpeditionRewards.entryCost(tier.level)) + " coins.";
        }
        return null;
    }

    // ════════════════════════════════════════════════════════════
    // LAUNCH
    // ════════════════════════════════════════════════════════════

    /**
     * Start a run for the player (solo, or with their whole V4 party if they lead one).
     * Returns false and messages the player when anything blocks the launch.
     */
    public static boolean startRun(ServerPlayer leader, Tier tier, boolean useSigil) {
        MinecraftServer server = ((ServerLevel) leader.level()).getServer();
        if (server == null) return false;

        String leaderUuid = leader.getUUID().toString();

        String block = blockReason(leader, tier);
        if (block != null) {
            leader.sendSystemMessage(Component.literal("§c" + block));
            return false;
        }

        ServerLevel level = server.getLevel(Level.END);
        if (level == null) {
            leader.sendSystemMessage(Component.literal("§cThe expedition lane is unavailable on this server."));
            return false;
        }

        // ── build the party list ──
        List<ServerPlayer> members = new ArrayList<>();
        members.add(leader);
        String partyLeader = PartyManager.getPartyLeader(leaderUuid);
        if (partyLeader != null && partyLeader.equals(leaderUuid)) {
            for (String memberUuid : PartyManager.getPartyMembers(leaderUuid)) {
                if (memberUuid.equals(leaderUuid)) continue;
                ServerPlayer m = server.getPlayerList().getPlayer(UUID.fromString(memberUuid));
                if (m == null) continue;
                if (inRun(memberUuid)) {
                    leader.sendSystemMessage(Component.literal("§c" + m.getName().getString()
                            + " is already in an expedition."));
                    return false;
                }
                members.add(m);
            }
        }

        // ── reserve an instance slot ──
        int slot = -1;
        for (int i = 0; i < ExpeditionGenerator.MAX_SLOTS; i++) {
            if (USED_SLOTS.add(i)) { slot = i; break; }
        }
        if (slot < 0) {
            leader.sendSystemMessage(Component.literal("§cEvery expedition instance is currently in use. Try again shortly."));
            return false;
        }

        // ── consume the cooldown bypass FIRST, so no coin refund path exists ──
        // (refunding through CoinManager would re-apply income multipliers and could pay out
        //  more than was charged — so we never charge until every gate has already passed).
        boolean keyUsed = false;
        if (cooldownRemaining(leaderUuid) > 0) {
            if (ExpeditionRewards.consumeOne(leader, "expedition_key")) {
                keyUsed = true;
                COOLDOWNS.remove(leaderUuid);
                leader.sendSystemMessage(Component.literal("§e✦ Expedition Key consumed — cooldown bypassed."));
            } else {
                USED_SLOTS.remove(slot);
                leader.sendSystemMessage(Component.literal("§cStill on cooldown and no Expedition Key to spend."));
                return false;
            }
        }

        // ── charge the entry fee ──
        long cost = ExpeditionRewards.entryCost(tier.level);
        if (cost > 0 && !CoinManager.removeCoins(leader, cost)) {
            USED_SLOTS.remove(slot);
            if (keyUsed) {
                // The key was already burned; hand it straight back rather than eat it.
                ExpeditionRewards.giveOrDropPublic(leader, ExpeditionRewards.createExpeditionKey(1));
            }
            leader.sendSystemMessage(Component.literal("§cYou cannot afford the "
                    + String.format("%,d", cost) + " coin entry."));
            return false;
        }

        boolean sigilUsed = false;
        if (useSigil && ExpeditionRewards.consumeOne(leader, "expedition_sigil")) {
            sigilUsed = true;
            leader.sendSystemMessage(Component.literal("§b✦ Expedition Sigil consumed — one extra loot roll on completion."));
        }

        // ── build the dungeon record ──
        ExpeditionDungeon d = new ExpeditionDungeon();
        d.id = UUID.randomUUID().toString();
        d.leaderUuid = leaderUuid;
        d.tier = tier.name();
        d.floor = 1;
        d.totalFloors = tier.floors;
        d.slotIndex = slot;
        d.originX = ExpeditionGenerator.originXForSlot(slot);
        d.originY = ExpeditionGenerator.STAND_Y;
        d.originZ = ExpeditionGenerator.originZForSlot(slot);
        d.worldDim = level.dimension().identifier().toString();
        d.seed = RNG.nextLong();
        d.startedAt = System.currentTimeMillis();
        d.floorStartedAt = d.startedAt;
        d.deadlineAt = d.startedAt + tier.timeLimitMinutes * 60_000L;
        d.sigilConsumed = sigilUsed;
        d.keyConsumed = keyUsed;
        d.state = "ACTIVE";

        Settlement home = launchSettlement(leader);
        d.settlementId = home == null ? null : home.id;

        for (ServerPlayer m : members) {
            d.party().add(m.getUUID().toString());
            ServerLevel ml = (ServerLevel) m.level();
            d.returnList().add(new ExpeditionDungeon.ReturnPoint(
                    m.getUUID().toString(),
                    ml.dimension().identifier().toString(),
                    m.getX(), m.getY(), m.getZ(), m.getYRot(), m.getXRot()));
        }

        // ── generate floor 1 ──
        buildFloor(level, d, tier);

        RUNS.put(d.id, d);
        for (String u : d.party()) PLAYER_RUN.put(u, d.id);

        BlockPos entry = ExpeditionGenerator.roomCenter(d, d.entryRoom());
        for (ServerPlayer m : members) {
            PlayerTeleportHelper.teleport(m, level, entry.getX() + 0.5, entry.getY(), entry.getZ() + 0.5, 0f, 0f);
            m.sendSystemMessage(Component.literal("✦ Expedition: ")
                    .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
                    .append(Component.literal(tier.displayName).withStyle(tier.color, ChatFormatting.BOLD)));
            m.sendSystemMessage(Component.literal("§7Clear every room, then take the arena. §c"
                    + tier.timeLimitMinutes + " minute limit."));
        }
        level.playSound(null, entry, SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, 0.6f, 1.2f);
        save();
        return true;
    }

    private static void buildFloor(ServerLevel level, ExpeditionDungeon d, Tier tier) {
        boolean finalFloor = d.floor >= d.totalFloors;
        d.rooms = ExpeditionGenerator.layout(d.seed, d.floor, finalFloor);
        ExpeditionGenerator.stampFloor(level, d);

        int total = 0;
        for (ExpeditionDungeon.Room r : d.roomList()) {
            if (ExpeditionGenerator.T_ENTRY.equals(r.type)) {
                r.activated = true;
                r.cleared = true;
            } else {
                total++;
            }
        }
        d.roomsTotalThisFloor = total;
        d.roomsCleared = 0;
        d.bossSpawned = false;
        d.floorStartedAt = System.currentTimeMillis();
    }

    // ════════════════════════════════════════════════════════════
    // TICK
    // ════════════════════════════════════════════════════════════

    public static void tick(MinecraftServer server) {
        if (server == null) return;
        tickCounter++;
        if (tickCounter % TICK_INTERVAL != 0) return;

        ServerLevel level = server.getLevel(Level.END);
        if (level == null) return;

        // Clean up anything left behind by a restart, one instance per tick window.
        if (!PENDING_WIPES.isEmpty()) {
            int[] origin = PENDING_WIPES.remove(0);
            ExpeditionGenerator.wipe(level, origin[0], origin[1]);
            LOGGER.info("Wiped orphaned instance at {},{}", origin[0], origin[1]);
            return;
        }

        if (RUNS.isEmpty()) return;

        long now = System.currentTimeMillis();
        for (ExpeditionDungeon d : new ArrayList<>(RUNS.values())) {
            try {
                tickRun(server, level, d, now);
            } catch (Exception e) {
                LOGGER.error("Run {} errored", d.id, e);
                endRun(server, d, EndReason.FAILED);
            }
        }
    }

    private static void tickRun(MinecraftServer server, ServerLevel level, ExpeditionDungeon d, long now) {
        Tier tier = Tier.byName(d.tier);

        // ── everyone logged out? end it rather than leak an instance ──
        List<ServerPlayer> online = onlineMembers(server, d);
        if (online.isEmpty()) {
            endRun(server, d, EndReason.ABANDONED);
            return;
        }

        // ── hard deadline ──
        if (now > d.deadlineAt) {
            for (ServerPlayer p : online) {
                p.sendSystemMessage(Component.literal("§4✖ The expedition collapses. You ran out of time.")
                        .withStyle(ChatFormatting.DARK_RED));
            }
            endRun(server, d, EndReason.FAILED);
            return;
        }

        // ── floor transition ──
        if ("FLOOR_CLEARED".equals(d.state)) {
            if (now >= d.floorAdvanceAt) {
                advanceFloor(server, level, d, tier);
            }
            return;
        }

        // ── safety: anyone who fell out of the instance goes back to the entry dais ──
        BlockPos entry = ExpeditionGenerator.roomCenter(d, d.entryRoom());
        for (ServerPlayer p : online) {
            boolean wrongLevel = p.level() != level;
            boolean belowFloor = p.getY() < ExpeditionGenerator.FLOOR_Y - 4;
            int[] cell = ExpeditionGenerator.gridCellOf(d, p.getX(), p.getZ());
            if (wrongLevel || belowFloor || cell == null) {
                PlayerTeleportHelper.teleport(p, level,
                        entry.getX() + 0.5, entry.getY(), entry.getZ() + 0.5, 0f, 0f);
                p.sendSystemMessage(Component.literal("§7Pulled back to the entry hall."));
            }
        }

        // ── arm rooms the party has walked into ──
        for (ServerPlayer p : online) {
            int[] cell = ExpeditionGenerator.gridCellOf(d, p.getX(), p.getZ());
            if (cell == null) continue;
            ExpeditionDungeon.Room room = d.roomAt(cell[0], cell[1]);
            if (room == null || room.activated || room.cleared) continue;

            if (ExpeditionGenerator.T_BOSS.equals(room.type)) {
                if (!d.nonBossRoomsCleared()) {
                    p.sendSystemMessage(Component.literal("§8The arena doors will not move. Clear the floor first."));
                    continue;
                }
                armBossRoom(level, d, tier, room);
            } else if (ExpeditionGenerator.T_PUZZLE.equals(room.type)) {
                room.activated = true;
                broadcast(server, d, "§b✦ Sealing mechanism: throw all three levers.");
            } else {
                armCombatRoom(level, d, tier, room);
                broadcast(server, d, "§c⚔ " + (ExpeditionGenerator.T_VAULT.equals(room.type)
                        ? "Vault guards awaken!" : "Ambush!"));
            }
        }

        // ── clear checks ──
        for (ExpeditionDungeon.Room room : d.roomList()) {
            if (room.cleared || !room.activated) continue;

            boolean cleared;
            if (ExpeditionGenerator.T_PUZZLE.equals(room.type)) {
                cleared = ExpeditionGenerator.puzzleSolved(level, d, room);
            } else {
                cleared = liveMobsIn(level, d, room) == 0;
            }
            if (!cleared) continue;

            room.cleared = true;
            d.roomsCleared++;
            d.roomsClearedLifetime++;

            if (ExpeditionGenerator.T_VAULT.equals(room.type)) {
                Random rng = ExpeditionGenerator.rngFor(d, 300 + room.gx * 7 + room.gz);
                for (BlockPos chest : ExpeditionGenerator.vaultChestPositions(d, room)) {
                    ExpeditionRewards.fillVaultChest(level, chest, tier.level, rng);
                }
                broadcast(server, d, "§6✦ The vault opens.");
            } else if (ExpeditionGenerator.T_BOSS.equals(room.type)) {
                d.bossDefeated = true;
                completeFloor(server, level, d, tier);
                return;
            } else {
                broadcast(server, d, "§a✓ Room cleared §7(" + d.roomsCleared + "/" + d.roomsTotalThisFloor + ")");
            }

            BlockPos c = ExpeditionGenerator.roomCenter(d, room);
            level.sendParticles(ParticleTypes.END_ROD, c.getX() + 0.5, c.getY() + 1.5, c.getZ() + 0.5,
                    30, 1.5, 1.0, 1.5, 0.02);
            level.playSound(null, c, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8f, 1.4f);

            // Non-final floors have no boss: clearing every room ends the floor.
            if (!d.isFinalFloor() && d.nonBossRoomsCleared()) {
                completeFloor(server, level, d, tier);
                return;
            }
        }

        // ── boss bar upkeep ──
        ServerBossEvent bar = BOSS_BARS.get(d.id);
        if (bar != null) {
            Mob boss = findBoss(level, d);
            if (boss != null && boss.isAlive()) {
                float max = Math.max(1f, boss.getMaxHealth());
                bar.setProgress(Math.max(0f, Math.min(1f, boss.getHealth() / max)));
            } else {
                bar.setProgress(0f);
            }
            for (ServerPlayer p : online) bar.addPlayer(p);
        }
    }

    private static void completeFloor(MinecraftServer server, ServerLevel level,
                                      ExpeditionDungeon d, Tier tier) {
        if (d.isFinalFloor()) {
            finishRun(server, level, d, tier);
            return;
        }
        d.state = "FLOOR_CLEARED";
        d.floorAdvanceAt = System.currentTimeMillis() + FLOOR_ADVANCE_DELAY_MS;
        broadcast(server, d, "§6▼ Floor " + d.floor + " cleared. Descending…");
        save();
    }

    private static void advanceFloor(MinecraftServer server, ServerLevel level,
                                     ExpeditionDungeon d, Tier tier) {
        clearMobs(level, d);
        removeBossBar(d);
        ExpeditionGenerator.wipe(level, d);

        d.floor++;
        d.state = "ACTIVE";
        buildFloor(level, d, tier);

        BlockPos entry = ExpeditionGenerator.roomCenter(d, d.entryRoom());
        for (ServerPlayer p : onlineMembers(server, d)) {
            PlayerTeleportHelper.teleport(p, level,
                    entry.getX() + 0.5, entry.getY(), entry.getZ() + 0.5, 0f, 0f);
            p.sendSystemMessage(Component.literal("§6▼ Floor " + d.floor + " of " + d.totalFloors)
                    .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        }
        level.playSound(null, entry, SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, 0.7f, 0.8f);
        save();
    }

    // ════════════════════════════════════════════════════════════
    // MOBS
    // ════════════════════════════════════════════════════════════

    private static void armCombatRoom(ServerLevel level, ExpeditionDungeon d, Tier tier,
                                      ExpeditionDungeon.Room room) {
        room.activated = true;
        BlockPos c = ExpeditionGenerator.roomCenter(d, room);
        int count = tier.mobsPerRoom + (ExpeditionGenerator.T_VAULT.equals(room.type) ? 2 : 0);
        Random rng = ExpeditionGenerator.rngFor(d, 100 + room.gx * 31 + room.gz);

        for (int i = 0; i < count; i++) {
            double angle = rng.nextDouble() * Math.PI * 2.0;
            double dist = 3.0 + rng.nextDouble() * 6.0;
            BlockPos pos = new BlockPos(
                    c.getX() + (int) Math.round(Math.cos(angle) * dist),
                    ExpeditionGenerator.STAND_Y,
                    c.getZ() + (int) Math.round(Math.sin(angle) * dist));
            spawnMob(level, d, tier, room, pos, rng, false);
        }
        room.mobsSpawned = count;

        level.sendParticles(ParticleTypes.LARGE_SMOKE, c.getX() + 0.5, c.getY() + 1.0, c.getZ() + 0.5,
                40, 3.0, 1.0, 3.0, 0.01);
        level.playSound(null, c, SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 0.35f, 1.6f);
    }

    private static void armBossRoom(ServerLevel level, ExpeditionDungeon d, Tier tier,
                                    ExpeditionDungeon.Room room) {
        room.activated = true;
        d.bossSpawned = true;
        BlockPos c = ExpeditionGenerator.roomCenter(d, room);
        Random rng = ExpeditionGenerator.rngFor(d, 900);

        Mob boss = spawnMob(level, d, tier, room, c.above(), rng, true);
        if (boss != null) {
            ServerBossEvent bar = new ServerBossEvent(
                    UUID.randomUUID(),
                    Component.literal(bossName(tier)).withStyle(tier.color, ChatFormatting.BOLD),
                    BossEvent.BossBarColor.PURPLE,
                    BossEvent.BossBarOverlay.PROGRESS);
            bar.setProgress(1.0f);
            BOSS_BARS.put(d.id, bar);
        }

        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, c.getX() + 0.5, c.getY() + 1.5, c.getZ() + 0.5,
                80, 4.0, 2.0, 4.0, 0.05);
        level.playSound(null, c, SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.0f, 0.7f);
    }

    private static String bossName(Tier tier) {
        return switch (tier) {
            case DELVE -> "✦ Warden of the First Seal";
            case DESCENT -> "✦ The Hollow Marshal";
            case ABYSS -> "✦ Abyssal Custodian";
            case CATACLYSM -> "✦ Cataclysm Herald";
            case APOTHEOSIS -> "✦ The Unmade";
        };
    }

    private static EntityType<? extends Mob> bossType(Tier tier) {
        return switch (tier) {
            case DELVE -> EntityTypes.RAVAGER;
            case DESCENT -> EntityTypes.EVOKER;
            case ABYSS -> EntityTypes.PIGLIN_BRUTE;
            case CATACLYSM -> EntityTypes.WITHER_SKELETON;
            case APOTHEOSIS -> EntityTypes.WARDEN;
        };
    }

    private static EntityType<? extends Mob> mobType(Tier tier, Random rng) {
        EntityType<? extends Mob>[] pool = mobPool(tier);
        return pool[rng.nextInt(pool.length)];
    }

    @SuppressWarnings("unchecked")
    private static EntityType<? extends Mob>[] mobPool(Tier tier) {
        return switch (tier) {
            case DELVE -> new EntityType[]{EntityTypes.ZOMBIE, EntityTypes.SKELETON, EntityTypes.SPIDER};
            case DESCENT -> new EntityType[]{EntityTypes.HUSK, EntityTypes.STRAY, EntityTypes.CAVE_SPIDER, EntityTypes.PILLAGER};
            case ABYSS -> new EntityType[]{EntityTypes.WITHER_SKELETON, EntityTypes.VINDICATOR, EntityTypes.BLAZE};
            case CATACLYSM -> new EntityType[]{EntityTypes.PIGLIN_BRUTE, EntityTypes.VINDICATOR, EntityTypes.WITHER_SKELETON, EntityTypes.BLAZE};
            case APOTHEOSIS -> new EntityType[]{EntityTypes.PIGLIN_BRUTE, EntityTypes.RAVAGER, EntityTypes.EVOKER, EntityTypes.WITHER_SKELETON};
        };
    }

    private static Mob spawnMob(ServerLevel level, ExpeditionDungeon d, Tier tier,
                                ExpeditionDungeon.Room room, BlockPos pos, Random rng, boolean boss) {
        EntityType<? extends Mob> type = boss ? bossType(tier) : mobType(tier, rng);
        Entity e = type.spawn(level, pos, EntitySpawnReason.EVENT);
        if (!(e instanceof Mob mob)) {
            if (e != null) e.discard();
            return null;
        }

        HealthScalingManager.ScalingTier scaling = tier.scaling;
        double healthMult = scaling.healthMultiplier * (boss ? 4.0 : 1.0);
        double damageMult = scaling.damageMultiplier * (boss ? 1.6 : 1.0);

        AttributeInstance hp = mob.getAttribute(Attributes.MAX_HEALTH);
        if (hp != null) {
            double v = hp.getBaseValue() * healthMult;
            hp.setBaseValue(v);
            mob.setHealth((float) v);
        }
        AttributeInstance dmg = mob.getAttribute(Attributes.ATTACK_DAMAGE);
        if (dmg != null) {
            dmg.setBaseValue(dmg.getBaseValue() * damageMult);
        }

        String label = boss
                ? bossName(tier)
                : "✦ " + scaling.displayName + " " + mob.getType().getDescription().getString();
        mob.setCustomName(Component.literal(label).withStyle(boss ? tier.color : scaling.color));
        mob.setCustomNameVisible(true);
        mob.setPersistenceRequired();
        mob.addTag(d.entityTag());
        mob.addTag(d.roomTag(room));
        if (boss) mob.addTag(d.entityTag() + "_boss");
        return mob;
    }

    private static int liveMobsIn(ServerLevel level, ExpeditionDungeon d, ExpeditionDungeon.Room room) {
        String tag = d.roomTag(room);
        AABB box = ExpeditionGenerator.roomBounds(d, room);
        List<Mob> found = level.getEntitiesOfClass(Mob.class, box,
                m -> m.isAlive() && m.entityTags().contains(tag));
        return found.size();
    }

    private static Mob findBoss(ServerLevel level, ExpeditionDungeon d) {
        String tag = d.entityTag() + "_boss";
        AABB box = ExpeditionGenerator.instanceBounds(d);
        List<Mob> found = level.getEntitiesOfClass(Mob.class, box,
                m -> m.isAlive() && m.entityTags().contains(tag));
        return found.isEmpty() ? null : found.get(0);
    }

    private static void clearMobs(ServerLevel level, ExpeditionDungeon d) {
        String tag = d.entityTag();
        AABB box = ExpeditionGenerator.instanceBounds(d);
        for (Mob m : level.getEntitiesOfClass(Mob.class, box, mob -> mob.entityTags().contains(tag))) {
            HealthScalingManager.onMobDeath(m);
            m.discard();
        }
    }

    // ════════════════════════════════════════════════════════════
    // FINISH / ABANDON / CLEANUP
    // ════════════════════════════════════════════════════════════

    private enum EndReason { COMPLETED, FAILED, ABANDONED, SHUTDOWN }

    private static void finishRun(MinecraftServer server, ServerLevel level,
                                  ExpeditionDungeon d, Tier tier) {
        if (d.lootRolled) return;
        d.lootRolled = true;
        d.state = "COMPLETE";

        List<ServerPlayer> online = onlineMembers(server, d);
        Random rng = new Random(d.seed ^ System.nanoTime());

        for (ServerPlayer p : online) {
            p.sendSystemMessage(Component.literal("★ EXPEDITION COMPLETE — " + tier.displayName)
                    .withStyle(tier.color, ChatFormatting.BOLD));
            ExpeditionRewards.grantCompletion(p, tier.level, d.sigilConsumed, rng);

            String uuid = p.getUUID().toString();
            CLEARS.computeIfAbsent(uuid, k -> new HashMap<>())
                    .merge(tier.name(), 1, Integer::sum);
        }

        // Settlement tie-in: the launching settlement banks a cut of the expedition's value.
        if (d.settlementId != null) {
            Settlement s = SettlementManager.get(d.settlementId);
            if (s != null) {
                long cut = (long) Math.floor(ExpeditionRewards.coinReward(tier.level)
                        * ExpeditionRewards.SETTLEMENT_TREASURY_SHARE);
                if (cut > 0) {
                    s.treasury += cut;
                    SettlementManager.save();
                    for (ServerPlayer p : online) {
                        p.sendSystemMessage(Component.literal("§7" + s.name + " treasury §6+"
                                + String.format("%,d", cut) + " coins§7."));
                    }
                }
            }
        }

        applyCooldown(d, tier, 1.0);
        endRun(server, d, EndReason.COMPLETED);
    }

    /** Player-initiated abandon. Half cooldown, no reward — leaving is allowed but never free. */
    public static boolean abandon(ServerPlayer player) {
        ExpeditionDungeon d = runOf(player.getUUID().toString());
        if (d == null) {
            player.sendSystemMessage(Component.literal("§7You are not in an expedition."));
            return false;
        }
        MinecraftServer server = ((ServerLevel) player.level()).getServer();
        Tier tier = Tier.byName(d.tier);
        broadcast(server, d, "§c✖ Expedition abandoned.");
        applyCooldown(d, tier, 0.5);
        endRun(server, d, EndReason.ABANDONED);
        return true;
    }

    private static void applyCooldown(ExpeditionDungeon d, Tier tier, double fraction) {
        long ms = (long) (tier.cooldownMinutes() * 60_000L * fraction);
        long until = System.currentTimeMillis() + ms;
        for (String uuid : d.party()) {
            COOLDOWNS.put(uuid, until);
        }
    }

    private static void endRun(MinecraftServer server, ExpeditionDungeon d, EndReason reason) {
        ServerLevel level = server == null ? null : server.getLevel(Level.END);

        if (level != null) {
            clearMobs(level, d);
            ExpeditionGenerator.wipe(level, d);
        } else {
            PENDING_WIPES.add(new int[]{d.originX, d.originZ});
        }
        removeBossBar(d);

        if (server != null) {
            for (String uuid : d.party()) {
                ServerPlayer p = server.getPlayerList().getPlayer(UUID.fromString(uuid));
                if (p == null) continue;
                ExpeditionDungeon.ReturnPoint rp = d.returnFor(uuid);
                ServerLevel target = null;
                if (rp != null) {
                    for (ServerLevel l : server.getAllLevels()) {
                        if (l.dimension().identifier().toString().equals(rp.dim)) { target = l; break; }
                    }
                }
                if (target == null) target = server.overworld();
                if (rp != null) {
                    PlayerTeleportHelper.teleport(p, target, rp.x, rp.y, rp.z, rp.yaw, rp.pitch);
                } else {
                    BlockPos sp = target.getHeightmapPos(
                            net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                            new BlockPos(0, 0, 0));
                    PlayerTeleportHelper.teleport(p, target, sp.getX() + 0.5, sp.getY(), sp.getZ() + 0.5, 0f, 0f);
                }
                if (reason == EndReason.SHUTDOWN) {
                    p.sendSystemMessage(Component.literal("§7Expedition closed: the server is stopping."));
                }
            }
        }

        for (String uuid : d.party()) {
            String mapped = PLAYER_RUN.get(uuid);
            if (d.id.equals(mapped)) PLAYER_RUN.remove(uuid);
        }
        RUNS.remove(d.id);
        if (d.slotIndex >= 0) USED_SLOTS.remove(d.slotIndex);
        save();
    }

    private static void removeBossBar(ExpeditionDungeon d) {
        ServerBossEvent bar = BOSS_BARS.remove(d.id);
        if (bar != null) {
            bar.removeAllPlayers();
            bar.setVisible(false);
        }
    }

    // ════════════════════════════════════════════════════════════
    // HELPERS
    // ════════════════════════════════════════════════════════════

    private static List<ServerPlayer> onlineMembers(MinecraftServer server, ExpeditionDungeon d) {
        List<ServerPlayer> out = new ArrayList<>();
        if (server == null) return out;
        for (String uuid : d.party()) {
            try {
                ServerPlayer p = server.getPlayerList().getPlayer(UUID.fromString(uuid));
                if (p != null) out.add(p);
            } catch (IllegalArgumentException ignored) {}
        }
        return out;
    }

    private static void broadcast(MinecraftServer server, ExpeditionDungeon d, String msg) {
        for (ServerPlayer p : onlineMembers(server, d)) {
            p.sendSystemMessage(Component.literal(msg));
        }
    }

    /** Admin escape hatch: wipe every instance and end every run. */
    public static int forceCleanupAll(MinecraftServer server) {
        int n = RUNS.size();
        for (ExpeditionDungeon d : new ArrayList<>(RUNS.values())) {
            endRun(server, d, EndReason.FAILED);
        }
        ServerLevel level = server == null ? null : server.getLevel(Level.END);
        if (level != null) {
            for (int i = 0; i < ExpeditionGenerator.MAX_SLOTS; i++) {
                ExpeditionGenerator.wipe(level,
                        ExpeditionGenerator.originXForSlot(i),
                        ExpeditionGenerator.originZForSlot(i));
            }
        }
        USED_SLOTS.clear();
        PENDING_WIPES.clear();
        save();
        return n;
    }

    /** Admin: clear one player's cooldown. */
    public static void clearCooldown(String uuid) {
        COOLDOWNS.remove(uuid);
        save();
    }
}
