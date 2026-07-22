package com.political;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * NPC-democracy elections for settlements.
 *  - CITY (below metropolis): elects a Constituent (city-wide perks handled by SettlementManager).
 *  - METROPOLIS+: elects Chair + Vice (winner/runner-up). Sub-metropolis settlements follow their
 *    nearest metropolis — those citizens vote in its election.
 *  - The single largest metropolis's Chair/Vice drive the EXISTING global roles via
 *    ElectionManager.bridgeSettlementChair.
 * NPC citizens vote via their personality model. Terms are 3 days. Own file: settlement_elections.json.
 */
public class SettlementElectionManager {

    private static final long TERM_MS = 3L * 24L * 60L * 60L * 1000L;

    public static class ElectionState {
        public String settlementId;
        public long termEndTime = 0L;
        public String constituent;   // city role
        public String chair;         // metro role
        public String vice;          // metro role
        public ElectionState() {}
        public ElectionState(String id) { this.settlementId = id; }
    }

    private static final Map<String, ElectionState> states = new LinkedHashMap<>();
    private static Path dataFile;
    private static int tickCounter = 0;
    private static final int CHECK_INTERVAL = 200; // ~10s

    private static class Candidate {
        String uuid;
        String name;
        double lean;
        double charisma;
        boolean incumbent;
    }

    // ── lifecycle ──

    public static void initialize(Path worldRoot) {
        dataFile = worldRoot.resolve("settlement_elections.json");
        load();
    }

    private static void load() {
        states.clear();
        if (dataFile == null || !Files.exists(dataFile)) return;
        try (Reader r = Files.newBufferedReader(dataFile)) {
            Gson g = new GsonBuilder().create();
            Type t = new TypeToken<List<ElectionState>>(){}.getType();
            List<ElectionState> loaded = g.fromJson(r, t);
            if (loaded != null) {
                for (ElectionState s : loaded) {
                    if (s != null && s.settlementId != null) states.put(s.settlementId, s);
                }
            }
        } catch (Exception e) {
            System.err.println("[SettlementElectionManager] Failed to load: " + e.getMessage());
        }
    }

    public static void save() {
        if (dataFile == null) return;
        try {
            Files.createDirectories(dataFile.getParent());
            Gson g = new GsonBuilder().setPrettyPrinting().create();
            try (Writer w = Files.newBufferedWriter(dataFile)) {
                g.toJson(new ArrayList<>(states.values()), w);
            }
        } catch (Exception e) {
            System.err.println("[SettlementElectionManager] Failed to save: " + e.getMessage());
        }
    }

    public static ElectionState stateOf(String settlementId) {
        return states.computeIfAbsent(settlementId, ElectionState::new);
    }

    // ── tick ──

    public static void tick(MinecraftServer server) {
        tickCounter++;
        if (tickCounter % CHECK_INTERVAL != 0) return;
        if (DictatorManager.isDictatorActive()) return;

        long now = System.currentTimeMillis();
        for (Settlement s : SettlementManager.getAllSettlements()) {
            SettlementTier tier = s.tierEnum();
            if (!tier.isAtLeast(SettlementTier.CITY)) continue;

            ElectionState st = stateOf(s.id);
            if (st.termEndTime <= 0) {
                st.termEndTime = now + TERM_MS;
                continue;
            }
            if (now >= st.termEndTime) {
                if (tier.isAtLeast(SettlementTier.METROPOLIS)) {
                    runMetropolisElection(server, s, st);
                } else {
                    runCityElection(server, s, st);
                }
                st.termEndTime = now + TERM_MS;
            }
        }

        bridgeLargestMetropolis(server);
        save();
    }

    // ── city election (Constituent) ──

    private static void runCityElection(MinecraftServer server, Settlement s, ElectionState st) {
        List<Candidate> candidates = buildCandidates(s, false);
        if (candidates.isEmpty()) return;

        List<Citizen> voters = CitizenManager.citizensOf(s.id);
        Map<String, Integer> tally = tallyVotes(voters, candidates, s, st.constituent);

        Candidate winner = pickWinner(candidates, tally);
        if (winner == null) return;

        st.constituent = winner.uuid;
        s.leaderMap().put("constituent", winner.uuid);

        broadcastToSettlement(server, s, Component.literal("🏛 " + s.name + " elected a Constituent: ")
                .withStyle(ChatFormatting.YELLOW)
                .append(Component.literal(winner.name).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)));
    }

    // ── metropolis election (Chair + Vice) ──

    private static void runMetropolisElection(MinecraftServer server, Settlement metro, ElectionState st) {
        // Candidates from the metro plus its follower (sub-metropolis) settlements.
        List<Candidate> candidates = buildCandidates(metro, true);
        if (candidates.isEmpty()) return;

        List<Citizen> voters = new ArrayList<>(CitizenManager.citizensOf(metro.id));
        for (Settlement follower : SettlementManager.getAllSettlements()) {
            if (follower == metro) continue;
            if (metro.id.equals(follower.nearestMetropolisId)) {
                voters.addAll(CitizenManager.citizensOf(follower.id));
            }
        }

        Map<String, Integer> tally = tallyVotes(voters, candidates, metro, st.chair);

        Candidate chair = pickWinner(candidates, tally);
        Candidate vice = pickRunnerUp(candidates, tally, chair);

        // An outgoing Chair keeps influence: they take a seat in the shadow government.
        if (st.chair != null && (chair == null || !st.chair.equals(chair.uuid))) {
            ShadowGovernmentManager.recordFormerChair(metro.id, st.chair);
        }

        st.chair = chair != null ? chair.uuid : null;
        st.vice = vice != null ? vice.uuid : null;
        if (st.chair != null) metro.leaderMap().put("chair", st.chair); else metro.leaderMap().remove("chair");
        if (st.vice != null) metro.leaderMap().put("vice", st.vice); else metro.leaderMap().remove("vice");

        broadcastToSettlement(server, metro, Component.literal("👑 " + metro.name + " Chair: ")
                .withStyle(ChatFormatting.GOLD)
                .append(Component.literal(chair != null ? chair.name : "none")
                        .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)));
    }

    /** Whichever metropolis is largest drives the global Chair/Vice roles. */
    private static void bridgeLargestMetropolis(MinecraftServer server) {
        Settlement largest = SettlementManager.findLargestMetropolis();
        if (largest == null) return;
        ElectionState st = states.get(largest.id);
        if (st == null || st.chair == null) return;

        String currentGlobal = DataManager.getChair();
        if (st.chair.equals(currentGlobal)) return; // already bridged
        ElectionManager.bridgeSettlementChair(server, st.chair, st.vice);

        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            p.sendSystemMessage(Component.literal("⚑ " + largest.name
                    + " (largest metropolis) now holds the national Chair.")
                    .withStyle(ChatFormatting.GOLD));
        }
    }

    // ── voting model ──

    private static List<Candidate> buildCandidates(Settlement s, boolean includeFollowers) {
        Map<String, Candidate> byUuid = new HashMap<>();

        // Registered players of s (and followers for a metro).
        addPlayerCandidates(byUuid, s);
        if (includeFollowers) {
            for (Settlement f : SettlementManager.getAllSettlements()) {
                if (f == s) continue;
                if (s.id.equals(f.nearestMetropolisId)) addPlayerCandidates(byUuid, f);
            }
        }

        // Notable NPC citizens: top-charisma few.
        List<Citizen> npcs = CitizenManager.citizensOf(s.id);
        npcs.sort(Comparator.comparingDouble((Citizen c) -> c.charisma).reversed());
        int npcCount = Math.min(3, npcs.size());
        for (int i = 0; i < npcCount; i++) {
            Citizen c = npcs.get(i);
            Candidate cand = new Candidate();
            cand.uuid = c.uuid;
            cand.name = c.name;
            cand.lean = c.partyLean;
            cand.charisma = c.charisma;
            byUuid.putIfAbsent(cand.uuid, cand);
        }

        // Mark incumbents.
        String incumbent = s.leaderMap().get(s.tierEnum().isAtLeast(SettlementTier.METROPOLIS) ? "chair" : "constituent");
        List<Candidate> out = new ArrayList<>(byUuid.values());
        for (Candidate c : out) c.incumbent = c.uuid.equals(incumbent);
        return out;
    }

    private static void addPlayerCandidates(Map<String, Candidate> byUuid, Settlement s) {
        for (String uuid : s.registered()) {
            Candidate cand = new Candidate();
            cand.uuid = uuid;
            cand.name = DataManager.getPlayerName(uuid);
            cand.lean = playerLean(uuid);
            cand.charisma = playerCharisma(uuid);
            byUuid.putIfAbsent(uuid, cand);
        }
    }

    private static Map<String, Integer> tallyVotes(List<Citizen> voters, List<Candidate> candidates,
                                                   Settlement s, String incumbentUuid) {
        Map<String, Integer> tally = new HashMap<>();
        for (Candidate c : candidates) tally.put(c.uuid, 0);
        for (Citizen voter : voters) {
            Candidate choice = null;
            double bestScore = -Double.MAX_VALUE;
            for (Candidate cand : candidates) {
                double score = computeScore(voter, cand, s);
                if (score > bestScore) { bestScore = score; choice = cand; }
            }
            if (choice != null) tally.merge(choice.uuid, 1, Integer::sum);
        }
        return tally;
    }

    private static double computeScore(Citizen voter, Candidate cand, Settlement s) {
        // Party alignment: closer leans score higher (leans in [-1,1]).
        double partyAlign = 1.0 - Math.abs(voter.partyLean - cand.lean) / 2.0; // 0..1

        // Charisma appeal weighted by voter susceptibility (use appealSensitivity as proxy).
        double charismaPull = cand.charisma * (0.5 + 0.5 * voter.charisma);

        // Record in office: citizens judge an incumbent on how the settlement actually performs.
        // Beauty (appeal), morale (happiness) and food security all feed the verdict, so running
        // a settlement badly genuinely costs you the election.
        double appealNorm = Math.min(1.0, s.appeal / 500.0);
        double happinessNorm = Math.min(1.0, s.happiness / 100.0);
        double incumbentEffect = 0.0;
        if (cand.incumbent) {
            double record = (appealNorm - 0.5) * voter.appealSensitivity * 2.0   // -1..1
                    + (happinessNorm - 0.5) * 2.0;                                // -1..1
            // Starvation is unforgivable at the ballot box.
            if (s.population > 0 && s.food < s.population) {
                record -= 1.2;
            }
            incumbentEffect = record / 2.0;
        }

        // Tax burden: incumbents pay a small penalty scaled by how tax-averse the voter is.
        double taxPenalty = cand.incumbent ? (1.0 - voter.taxTolerance) * 0.3 : 0.0;

        // Deterministic noise per (voter, candidate) to break ties without RNG state.
        double noise = deterministicNoise(voter.uuid, cand.uuid) * 0.25;

        return partyAlign * 1.0 + charismaPull * 0.8 + incumbentEffect * 0.6 - taxPenalty + noise;
    }

    private static Candidate pickWinner(List<Candidate> candidates, Map<String, Integer> tally) {
        Candidate best = null;
        int bestVotes = -1;
        for (Candidate c : candidates) {
            int v = tally.getOrDefault(c.uuid, 0);
            if (v > bestVotes || (v == bestVotes && best != null && c.charisma > best.charisma)) {
                bestVotes = v;
                best = c;
            }
        }
        return best;
    }

    private static Candidate pickRunnerUp(List<Candidate> candidates, Map<String, Integer> tally, Candidate exclude) {
        Candidate best = null;
        int bestVotes = -1;
        for (Candidate c : candidates) {
            if (exclude != null && c.uuid.equals(exclude.uuid)) continue;
            int v = tally.getOrDefault(c.uuid, 0);
            if (v > bestVotes || (v == bestVotes && best != null && c.charisma > best.charisma)) {
                bestVotes = v;
                best = c;
            }
        }
        return best;
    }

    // ── deterministic player personality (mirrors Citizen mixing) ──

    public static double playerLean(String uuid) {
        return norm(mix(uuid.hashCode() * 0x9E3779B97F4A7C15L)) * 2.0 - 1.0;
    }

    public static double playerCharisma(String uuid) {
        return norm(mix(uuid.hashCode() * 0xC2B2AE3D27D4EB4FL + 0x165667B19E3779F9L));
    }

    private static double deterministicNoise(String a, String b) {
        return norm(mix(((long) a.hashCode() << 32) ^ (b.hashCode() & 0xFFFFFFFFL)));
    }

    private static long mix(long z) {
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }

    private static double norm(long v) {
        return (v >>> 11) * (1.0 / (1L << 53));
    }

    // ── util ──

    private static void broadcastToSettlement(MinecraftServer server, Settlement s, Component msg) {
        for (String uuid : s.registered()) {
            try {
                ServerPlayer p = server.getPlayerList().getPlayer(UUID.fromString(uuid));
                if (p != null) p.sendSystemMessage(msg);
            } catch (Exception ignored) {}
        }
    }
}
