package com.political.government;
import com.political.BuffProgressManager;
import com.political.CivilCraftAchievements;
import com.political.CreditItem;
import com.political.DataManager;
import com.political.PlayerBuffManager;
import com.political.PoliticalServer;



import com.political.party.PoliticalPartyManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.*;

public class ElectionManager {

    private static final long WEEK_MS = 7L * 24L * 60L * 60L * 1000L;
    private static final long DAY_MS = 24L * 60L * 60L * 1000L;

    private static long termEndTime = 0;
    private static long electionEndTime = 0;
    private static boolean electionActive = false;

    private static Map<String, Integer> votes = new HashMap<>();
    private static Map<String, Integer> fakeVotes = new HashMap<>();
    private static Set<String> corruptCandidates = new HashSet<>();
    private static Map<String, String> votedPlayers = new HashMap<>();
    private static List<String> candidates = new ArrayList<>();

    private static boolean impeachmentActive = false;
    private static int impeachYes = 0;
    private static int impeachNo = 0;
    private static List<String> impeachVoted = new ArrayList<>();

    private static boolean electionSystemEnabled = true;
    private static boolean electionSystemPaused = false;
    private static final long ELECTION_DURATION_MS = 24 * 60 * 60 * 1000L;  // 24 hours (adjust as needed)

    public static void loadFromData(DataManager.SaveData data) {
        termEndTime = data.termEndTime;
        electionEndTime = data.electionEndTime;
        electionActive = data.electionActive;
        votes = data.votes != null ? new HashMap<>(data.votes) : new HashMap<>();
        votedPlayers = data.votedPlayers != null ? new HashMap<>(data.votedPlayers) : new HashMap<>();
        candidates = data.candidates != null ? new ArrayList<>(data.candidates) : new ArrayList<>();
        impeachmentActive = data.impeachmentActive;
        impeachYes = data.impeachYes;
        impeachNo = data.impeachNo;
        impeachVoted = data.impeachVoted != null ? new ArrayList<>(data.impeachVoted) : new ArrayList<>();
        electionSystemEnabled = data.electionSystemEnabled;
        electionSystemPaused = data.electionSystemPaused;
    }

    public static void resetImpeachment() {
        DataManager.SaveData data = DataManager.getData();
        data.impeachmentActive = false;
        data.impeachYes = 0;
        data.impeachNo = 0;
        data.impeachVoted.clear();
        DataManager.save(PoliticalServer.server);
    }

    public static void saveToData(DataManager.SaveData data) {
        data.termEndTime = termEndTime;
        data.electionEndTime = electionEndTime;
        data.electionActive = electionActive;
        data.votes = new HashMap<>(votes);
        data.votedPlayers = new HashMap<>(votedPlayers);
        data.candidates = new ArrayList<>(candidates);
        data.impeachmentActive = impeachmentActive;
        data.impeachYes = impeachYes;
        data.impeachNo = impeachNo;
        data.impeachVoted = new ArrayList<>(impeachVoted);
        data.electionSystemEnabled = electionSystemEnabled;
        data.electionSystemPaused = electionSystemPaused;
    }

    public static void tick(MinecraftServer server) {
        if (!electionSystemEnabled || electionSystemPaused) {
            return;
        }

        // ADDED: Don't run elections during dictatorship
        if (DictatorManager.isDictatorActive()) {
            return;
        }

        long now = System.currentTimeMillis();

        if (!electionActive && termEndTime <= 0) {
            termEndTime = now + WEEK_MS;
            DataManager.save(server);
        }

        if (electionActive && electionEndTime <= 0) {
            electionEndTime = now + DAY_MS;
            DataManager.save(server);
        }

        if (electionActive && now >= electionEndTime) {
            endElection(server);
        }

        if (!electionActive && termEndTime > 0 && now >= termEndTime) {
            startElection(server);
        }
    }

    public static void buyFakeVote(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        if (!electionActive) {
            player.sendSystemMessage(Component.literal("No election is active!").withStyle(ChatFormatting.RED));
            return;
        }
        if (!candidates.contains(uuid)) {
            player.sendSystemMessage(Component.literal("You are not a candidate!").withStyle(ChatFormatting.RED));
            return;
        }
        if (CreditItem.countCredits(player) < 25) {
            player.sendSystemMessage(Component.literal("You need at least 25 credits to buy a fake vote!").withStyle(ChatFormatting.RED));
            return;
        }
        CreditItem.removeCredits(player, 25);
        int count = fakeVotes.getOrDefault(uuid, 0) + 1;
        fakeVotes.put(uuid, count);
        votes.put(uuid, votes.getOrDefault(uuid, 0) + 1);
        if (count >= 5) {
            corruptCandidates.add(uuid);
            saveCorruption();
            player.sendSystemMessage(Component.literal("⚠ You have been flagged as CORRUPT! You will choose 1 fewer perk if elected.").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        } else {
            player.sendSystemMessage(Component.literal("Fake vote purchased! (" + count + "/5 before corruption flag). -25 credits.").withStyle(ChatFormatting.YELLOW));
        }
        DataManager.save(PoliticalServer.server);
    }

    public static boolean isCorrupt(String uuid) {
        return corruptCandidates.contains(uuid);
    }

    public static int getFakeVotes(String uuid) {
        return fakeVotes.getOrDefault(uuid, 0);
    }

    public static void resetFakeVotes() {
        fakeVotes.clear();
        corruptCandidates.clear();
        saveCorruption();
    }

    /** Clears per-election fake-vote tallies but KEEPS corruption flags for the term. */
    private static void clearFakeVoteTallies() {
        fakeVotes.clear();
    }

    public static void startElection(MinecraftServer server) {
        // ADDED: Block elections during dictatorship
        if (DictatorManager.isDictatorActive()) {
            broadcast(server, Component.literal("Elections are suspended during dictatorship!").withStyle(ChatFormatting.RED));
            return;
        }

        electionActive = true;
        electionEndTime = System.currentTimeMillis() + DAY_MS;
        votes.clear();
        votedPlayers.clear();
        candidates.clear();
        resetFakeVotes();

        broadcast(server, Component.literal("National election started.").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        broadcast(server, Component.literal("Nominate yourself with /run. Voting is open for 24 hours.").withStyle(ChatFormatting.YELLOW));
        broadcast(server, Component.literal("First place becomes Chair, second becomes Vice Chair.").withStyle(ChatFormatting.GRAY));

        PoliticalPartyManager.syncStartPartyLeaderElection(electionEndTime);

        DataManager.save(server);
    }

    public static void startEmergencyElectionSilent(MinecraftServer server) {
        // Clear any existing election state
        votes.clear();
        votedPlayers.clear();
        candidates.clear();
        resetFakeVotes();

        // Start the election
        electionActive = true;
        electionEndTime = System.currentTimeMillis() + (24 * 60 * 60 * 1000L);  // 24 hours
        DataManager.save(server);
    }

    public static boolean nominateSelf(ServerPlayer player) {
        if (!electionSystemEnabled) {
            player.sendSystemMessage(Component.literal("Elections are currently disabled.").withStyle(ChatFormatting.RED));
            return false;
        }
        if (electionSystemPaused) {
            player.sendSystemMessage(Component.literal("Elections are currently paused.").withStyle(ChatFormatting.YELLOW));
            return false;
        }
        if (!electionActive) {
            String time = PoliticalServer.formatTime(getTimeUntilNextElection());
            player.sendSystemMessage(Component.literal("No election is running. Next campaign in " + time + ".")
                    .withStyle(ChatFormatting.RED));
            return false;
        }
        String uuid = player.getUUID().toString();
        if (candidates.contains(uuid)) {
            player.sendSystemMessage(Component.literal("You are already on the ballot.").withStyle(ChatFormatting.YELLOW));
            return false;
        }
        candidates.add(uuid);
        votes.put(uuid, 0);
        DataManager.save(PoliticalServer.server);
        player.sendSystemMessage(Component.literal("You are running for national office.").withStyle(ChatFormatting.GREEN));
        if (PoliticalServer.server != null) {
            broadcast(PoliticalServer.server, Component.literal(player.getName().getString() + " is running for Chair.")
                    .withStyle(ChatFormatting.AQUA));
        }
        return true;
    }

    public static void startEmergencyElection() {
        startEmergencyElection(PoliticalServer.server);
    }

    private static final Map<UUID, Integer> extraDropsCount = new HashMap<>();

    public static void setExtraDrops(UUID uuid, int count) {
        extraDropsCount.put(uuid, count);
    }

    public static int getAndClearExtraDrops(UUID uuid) {
        // FIX: previously called get() after remove() -> NPE / always-0.
        Integer removed = extraDropsCount.remove(uuid);
        return removed != null ? removed : 0;
    }

    // ============================================================
    // V4: corruption persistence (own file, wired via initializeCorruption)
    // ============================================================
    private static java.nio.file.Path corruptionFile;

    public static void initializeCorruption(java.nio.file.Path worldRoot) {
        corruptionFile = worldRoot.resolve("election_corruption.json");
        loadCorruption();
    }

    private static void loadCorruption() {
        if (corruptionFile == null || !java.nio.file.Files.exists(corruptionFile)) return;
        try (java.io.Reader r = java.nio.file.Files.newBufferedReader(corruptionFile)) {
            com.google.gson.Gson g = new com.google.gson.GsonBuilder().create();
            java.lang.reflect.Type t = new com.google.gson.reflect.TypeToken<java.util.HashSet<String>>(){}.getType();
            java.util.Set<String> loaded = g.fromJson(r, t);
            if (loaded != null) {
                corruptCandidates.clear();
                corruptCandidates.addAll(loaded);
            }
        } catch (Exception e) {
            System.err.println("[ElectionManager] Failed to load corruption: " + e.getMessage());
        }
    }

    private static void saveCorruption() {
        if (corruptionFile == null) return;
        try {
            java.nio.file.Files.createDirectories(corruptionFile.getParent());
            com.google.gson.Gson g = new com.google.gson.GsonBuilder().setPrettyPrinting().create();
            try (java.io.Writer w = java.nio.file.Files.newBufferedWriter(corruptionFile)) {
                g.toJson(corruptCandidates, w);
            }
        } catch (Exception e) {
            System.err.println("[ElectionManager] Failed to save corruption: " + e.getMessage());
        }
    }

    /**
     * V4 bridge: the largest METROPOLIS+ settlement's election result drives the
     * EXISTING global Chair/Vice roles so all current perk plumbing keeps working.
     * chairUuid/viceUuid may be a real player UUID or an NPC citizen UUID.
     */
    public static void bridgeSettlementChair(MinecraftServer server, String chairUuid, String viceUuid) {
        // Local metro chairs stay local. National office is filled only by the national election.
    }

    public static void endElection(MinecraftServer server) {
        PoliticalPartyManager.syncEndPartyLeaderElection(server);

        electionActive = false;
        electionEndTime = 0;

        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(votes.entrySet());
        sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        String newChair = null;
        String newViceChair = null;

        java.util.Random rand = new java.util.Random(DataManager.getElectionTieSeed());

        if (!sorted.isEmpty() && sorted.get(0).getValue() > 0) {
            int topVotes = sorted.get(0).getValue();
            List<String> tied = new ArrayList<>();
            for (Map.Entry<String, Integer> e : sorted) {
                if (e.getValue() == topVotes) tied.add(e.getKey());
                else break;
            }
            newChair = tied.get(rand.nextInt(tied.size()));
        }

        if (newChair != null) {
            int best = 0;
            for (Map.Entry<String, Integer> e : sorted) {
                if (e.getKey().equals(newChair)) continue;
                best = e.getValue();
                break;
            }
            if (best > 0) {
                List<String> tiedVice = new ArrayList<>();
                for (Map.Entry<String, Integer> e : sorted) {
                    if (e.getKey().equals(newChair)) continue;
                    if (e.getValue() == best) tiedVice.add(e.getKey());
                    else if (!tiedVice.isEmpty()) break;
                }
                if (!tiedVice.isEmpty()) {
                    newViceChair = tiedVice.get(rand.nextInt(tiedVice.size()));
                }
            }
        }

        // Check for dictator offer
        int totalVotesCast = votedPlayers.size();
        if (newChair != null && totalVotesCast > 0) {
            int winnerVotes = sorted.get(0).getValue();
            double percentage = (double) winnerVotes / totalVotesCast;
            boolean qualifies = (totalVotesCast > 15 && percentage > 0.97)
                    || (totalVotesCast <= 15 && percentage >= 1.0);
            if (qualifies) {
                final String winnerUuid = newChair;
                ServerPlayer winner = server.getPlayerList().getPlayer(java.util.UUID.fromString(winnerUuid));
                if (winner != null) {
                    broadcast(server, Component.literal(winner.getName().getString() + " received " + String.format("%.0f", percentage * 100) + "% of votes!").withStyle(ChatFormatting.GOLD));
                    DictatorManager.offerDictatorTitle(winner);
                }
            }
        }

        String oldChair = DataManager.getChair();

        // Lifetime votes toward POPULAR (this election's tallies)
        BuffProgressManager.applyElectionResultsToVoteTotals(server, votes);

        // Consecutive wins as Chair (DIPLOMATIC) — DataManager is source of truth
        if (oldChair != null && (newChair == null || !oldChair.equals(newChair))) {
            DataManager.resetConsecutiveElectionWins(oldChair);
        }
        if (newChair != null) {
            if (newChair.equals(oldChair)) {
                DataManager.incrementConsecutiveElectionWins(newChair);
            } else {
                DataManager.setConsecutiveElectionWins(newChair, 1);
            }
        }

        // Perks chosen last term (Chair + Vice Chair) cannot be chosen next term (one-term cooldown).
        // Snapshot while active perks still reflect the ending term.
        PerkManager.setPreviousTermPerks(new ArrayList<>(PerkManager.getActivePerks()));

        if (newChair != null && newChair.equals(oldChair)) {
            DataManager.setChairTermCount(DataManager.getChairTermCount() + 1);
        } else {
            DataManager.setChairTermCount(1);
            PerkManager.clearAllPerks();
        }

        // Allow new perk selection for the new term
        PerkManager.onNewTermStart();

        DataManager.setChair(newChair);
        DataManager.setViceChair(newViceChair);

        if (newChair != null) {
            ServerPlayer elected = server.getPlayerList().getPlayer(java.util.UUID.fromString(newChair));
            if (elected != null) {
                BuffProgressManager.tryGrantDiplomatic(elected);
                PlayerBuffManager.checkBuffUnlocks(elected, newChair);
            }
        }

        // REMOVED: The duplicate onNewTermStart() and setChair/setViceChair calls

        termEndTime = System.currentTimeMillis() + WEEK_MS;

        // ... rest of broadcast messages stay the same

        broadcast(server, Component.literal("═══════════════════════════════════").withStyle(ChatFormatting.GOLD));
        broadcast(server, Component.literal("    ✓ ELECTION RESULTS ✓").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        broadcast(server, Component.literal("═══════════════════════════════════").withStyle(ChatFormatting.GOLD));

        if (newChair != null) {
            broadcast(server, Component.literal("Chair: ").withStyle(ChatFormatting.WHITE)
                    .append(Component.literal(DataManager.getPlayerName(newChair)).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)));
        } else {
            broadcast(server, Component.literal("No Chair elected").withStyle(ChatFormatting.RED));
        }

        if (newViceChair != null) {
            broadcast(server, Component.literal("Vice Chair: ").withStyle(ChatFormatting.WHITE)
                    .append(Component.literal(DataManager.getPlayerName(newViceChair)).withStyle(ChatFormatting.AQUA)));
        }

        broadcast(server, Component.literal(""));
        broadcast(server, Component.literal("Vote Counts:").withStyle(ChatFormatting.GRAY));
        for (Map.Entry<String, Integer> entry : sorted) {
            broadcast(server, Component.literal(" " + DataManager.getPlayerName(entry.getKey()) + ": " + entry.getValue()).withStyle(ChatFormatting.WHITE));
        }
        broadcast(server, Component.literal("═══════════════════════════════════").withStyle(ChatFormatting.GOLD));

        // FIX: do NOT wipe corruption flags here — the newly-elected Chair/Vice open
        // PerksGui AFTER the election ends, and PerksGui reads isCorrupt(uuid) to grant
        // one fewer perk. Clearing here made the penalty unreachable. Flags are cleared
        // fresh at the next startElection(); persisted so they survive a restart.
        clearFakeVoteTallies();
        saveCorruption();
        DataManager.save(server);
    }

    public static void startEmergencyElection(MinecraftServer server) {
        if (DictatorManager.isDictatorActive()) {
            broadcast(server, Component.literal("Remove the dictator first with /dictator remove").withStyle(ChatFormatting.RED));
            return;
        }

        termEndTime = System.currentTimeMillis();
        startElection(server);
        DataManager.setChair(null);
        DataManager.setViceChair(null);
        PerkManager.clearAllPerks();
        PerkManager.onNewTermStart(); // ADDED: Allow perk selection after emergency election
        broadcast(server, Component.literal("⚠ EMERGENCY ELECTION CALLED ⚠").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
    }

    public static void castVote(ServerPlayer player, String candidateUuid) {
        String voterUuid = player.getUUID().toString();

        if (votedPlayers.containsKey(voterUuid)) {
            player.sendSystemMessage(Component.literal("You have already voted!").withStyle(ChatFormatting.RED));
            return;
        }

        if (voterUuid.equals(candidateUuid)) {
            player.sendSystemMessage(Component.literal("You cannot vote for yourself!").withStyle(ChatFormatting.RED));
            return;
        }

        if (!candidates.contains(candidateUuid)) {
            player.sendSystemMessage(Component.literal("Invalid candidate!").withStyle(ChatFormatting.RED));
            return;
        }

        votedPlayers.put(voterUuid, candidateUuid);
        
        // POPULAR buff: +1 extra vote weight
        int voteWeight = 1 + PlayerBuffManager.getExtraVoteWeight(voterUuid);
        votes.put(candidateUuid, votes.getOrDefault(candidateUuid, 0) + voteWeight);

        BuffProgressManager.onElectionVoteCast(player);
        CivilCraftAchievements.trackVote(player);

        player.sendSystemMessage(Component.literal("Vote cast for " + DataManager.getPlayerName(candidateUuid) + "!").withStyle(ChatFormatting.GREEN));
        DataManager.save(PoliticalServer.server);
    }

    public static void startImpeachment(MinecraftServer server) {
        impeachmentActive = true;
        impeachYes = 0;
        impeachNo = 0;
        impeachVoted.clear();

        broadcast(server, Component.literal("═══════════════════════════════════").withStyle(ChatFormatting.RED));
        broadcast(server, Component.literal("  ⚖ IMPEACHMENT VOTE STARTED ⚖").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        broadcast(server, Component.literal("═══════════════════════════════════").withStyle(ChatFormatting.RED));
        broadcast(server, Component.literal("Use /impeachment vote to vote YES or NO").withStyle(ChatFormatting.YELLOW)); // CHANGED: Updated instruction
        broadcast(server, Component.literal("2/3 majority needed, minimum 5 voters").withStyle(ChatFormatting.GRAY));
        broadcast(server, Component.literal("═══════════════════════════════════").withStyle(ChatFormatting.RED));

        DataManager.save(server);
    }

    public static void castImpeachVote(ServerPlayer player, boolean voteYes) {
        String uuid = player.getUUID().toString();

        if (impeachVoted.contains(uuid)) {
            player.sendSystemMessage(Component.literal("You have already voted!").withStyle(ChatFormatting.RED));
            return;
        }

        impeachVoted.add(uuid);
        if (voteYes) {
            impeachYes++;
        } else {
            impeachNo++;
        }

        player.sendSystemMessage(Component.literal("Vote recorded!").withStyle(ChatFormatting.GREEN));

        int total = impeachYes + impeachNo;
        if (total >= 5) {
            double yesPercent = (double) impeachYes / total;
            if (yesPercent >= 0.666) {
                broadcast(PoliticalServer.server, Component.literal("⚠ IMPEACHMENT PASSED ⚠").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
                impeachmentActive = false;
                startEmergencyElection(PoliticalServer.server);
            } else if ((double) impeachNo / total > 0.334) {
                broadcast(PoliticalServer.server, Component.literal("Impeachment vote failed.").withStyle(ChatFormatting.GREEN));
                impeachmentActive = false;
            }
        }

        DataManager.save(PoliticalServer.server);
    }

    public static void endImpeachmentVote(MinecraftServer server) {
        int total = impeachYes + impeachNo;

        if (total < 5) {
            broadcast(server, Component.literal("Impeachment vote voided - less than 5 voters.").withStyle(ChatFormatting.GRAY));
        } else {
            double yesPercent = (double) impeachYes / total;
            if (yesPercent >= 0.666) {
                broadcast(server, Component.literal("⚠ IMPEACHMENT PASSED ⚠").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
                startEmergencyElection(server);
            } else {
                broadcast(server, Component.literal("Impeachment vote failed.").withStyle(ChatFormatting.GREEN));
            }
        }

        impeachmentActive = false;
        impeachYes = 0;
        impeachNo = 0;
        impeachVoted.clear();
        DataManager.save(server);
    }

    public static boolean isElectionActive() {
        return electionActive;
    }

    public static boolean isImpeachmentActive() {
        return impeachmentActive;
    }

    public static long getRemainingTime() {
        return Math.max(0, electionEndTime - System.currentTimeMillis());
    }

    public static long getTimeUntilNextElection() {
        return Math.max(0, termEndTime - System.currentTimeMillis());
    }

    public static List<String> getCandidates() {
        return new ArrayList<>(candidates);
    }

    public static Map<String, Integer> getVotes() {
        return new HashMap<>(votes);
    }

    public static boolean hasVoted(String uuid) {
        return votedPlayers.containsKey(uuid);
    }

    public static boolean hasImpeachVoted(String uuid) {
        return impeachVoted.contains(uuid);
    }

    public static void forceStartElection(MinecraftServer server) {
        // ADDED: Block during dictatorship (ops can still use /dictator remove first)
        if (DictatorManager.isDictatorActive()) {
            broadcast(server, Component.literal("Remove the dictator first with /dictator remove").withStyle(ChatFormatting.RED));
            return;
        }

        termEndTime = System.currentTimeMillis();
        startElection(server);
    }

    private static void broadcast(MinecraftServer server, Component message) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.sendSystemMessage(message);
        }
    }

    public static boolean isElectionSystemEnabled() {
        return electionSystemEnabled;
    }

    public static void setElectionSystemEnabled(boolean enabled) {
        electionSystemEnabled = enabled;
        DataManager.save(PoliticalServer.server);
    }

    public static boolean isElectionSystemPaused() {
        return electionSystemPaused;
    }

    public static void setElectionSystemPaused(boolean paused) {
        electionSystemPaused = paused;
        DataManager.save(PoliticalServer.server);
    }
    
    // Vote boost for coin sink integration
    private static final Set<String> voteBoostedPlayers = new HashSet<>();
    
    public static void grantVoteBoost(String uuid) {
        voteBoostedPlayers.add(uuid);
        DataManager.save(PoliticalServer.server);
    }
    
    public static boolean hasVoteBoost(String uuid) {
        return voteBoostedPlayers.contains(uuid);
    }
    
    public static void consumeVoteBoost(String uuid) {
        voteBoostedPlayers.remove(uuid);
        DataManager.save(PoliticalServer.server);
    }
}