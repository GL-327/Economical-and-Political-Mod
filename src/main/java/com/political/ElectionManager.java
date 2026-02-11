package com.political;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.*;

public class ElectionManager {

    private static final long WEEK_MS = 7L * 24L * 60L * 60L * 1000L;
    private static final long DAY_MS = 24L * 60L * 60L * 1000L;

    private static long termEndTime = 0;
    private static long electionEndTime = 0;
    private static boolean electionActive = false;

    private static Map<String, Integer> votes = new HashMap<>();
    private static Map<String, String> votedPlayers = new HashMap<>();
    private static List<String> candidates = new ArrayList<>();

    private static boolean impeachmentActive = false;
    private static int impeachYes = 0;
    private static int impeachNo = 0;
    private static List<String> impeachVoted = new ArrayList<>();

    private static boolean electionSystemEnabled = false;
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

        if (electionActive && now >= electionEndTime) {
            endElection(server);
        }

        if (!electionActive && termEndTime > 0 && now >= termEndTime) {
            startElection(server);
        }
    }

    public static void startElection(MinecraftServer server) {
        // ADDED: Block elections during dictatorship
        if (DictatorManager.isDictatorActive()) {
            broadcast(server, Text.literal("Elections are suspended during dictatorship!").formatted(Formatting.RED));
            return;
        }

        electionActive = true;
        electionEndTime = System.currentTimeMillis() + DAY_MS;
        votes.clear();
        votedPlayers.clear();
        candidates.clear();

        List<String> allPlayers = new ArrayList<>(DataManager.getAllPlayers().keySet());

        String currentChair = DataManager.getChair();
        int termCount = DataManager.getChairTermCount();
        if (currentChair != null && termCount >= 2) {
            allPlayers.remove(currentChair);
        }

        Collections.shuffle(allPlayers);
        int candidateCount = Math.min(5, allPlayers.size());
        for (int i = 0; i < candidateCount; i++) {
            candidates.add(allPlayers.get(i));
            votes.put(allPlayers.get(i), 0);
        }

        broadcast(server, Text.literal("═══════════════════════════════════").formatted(Formatting.GOLD));
        broadcast(server, Text.literal("    ⚡ ELECTION STARTED! ⚡").formatted(Formatting.YELLOW, Formatting.BOLD));
        broadcast(server, Text.literal("═══════════════════════════════════").formatted(Formatting.GOLD));
        broadcast(server, Text.literal("Candidates:").formatted(Formatting.WHITE));
        for (String uuid : candidates) {
            broadcast(server, Text.literal(" • " + DataManager.getPlayerName(uuid)).formatted(Formatting.AQUA));
        }
        broadcast(server, Text.literal(""));
        broadcast(server, Text.literal("Use /vote to cast your vote!").formatted(Formatting.GREEN));
        broadcast(server, Text.literal("Voting ends in 24 hours.").formatted(Formatting.GRAY));
        broadcast(server, Text.literal("═══════════════════════════════════").formatted(Formatting.GOLD));

        DataManager.save(server);
    }

    public static void startEmergencyElectionSilent(MinecraftServer server) {
        // Clear any existing election state
        votes.clear();
        votedPlayers.clear();
        candidates.clear();

        // Start the election
        electionActive = true;
        electionEndTime = System.currentTimeMillis() + (24 * 60 * 60 * 1000L);  // 24 hours

        DataManager.save(server);
    }
    public static void startEmergencyElection() {
        startEmergencyElection(PoliticalServer.server);
    }

    private static final Map<UUID, Integer> extraDropsCount = new HashMap<>();

    public static void setExtraDrops(UUID uuid, int count) {
        extraDropsCount.put(uuid, count);
    }

    public static int getAndClearExtraDrops(UUID uuid) {
        return extraDropsCount.remove(uuid) != null ? extraDropsCount.get(uuid) : 0;
    }

    public static void endElection(MinecraftServer server) {
        electionActive = false;

        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(votes.entrySet());
        sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        String newChair = null;
        String newViceChair = null;

        if (!sorted.isEmpty() && sorted.get(0).getValue() > 0) {
            newChair = sorted.get(0).getKey();
        }
        if (sorted.size() >= 2 && sorted.get(1).getValue() > 0) {
            newViceChair = sorted.get(1).getKey();
        }

        String oldChair = DataManager.getChair();

        if (newChair != null && newChair.equals(oldChair)) {
            DataManager.setChairTermCount(DataManager.getChairTermCount() + 1);
        } else {
            DataManager.setChairTermCount(1);
            // FIXED: Store previous term perks BEFORE clearing
            PerkManager.setPreviousTermPerks(PerkManager.getActivePerks());
            PerkManager.clearAllPerks();
        }

        // Allow new perk selection for the new term
        PerkManager.onNewTermStart();

        DataManager.setChair(newChair);
        DataManager.setViceChair(newViceChair);

        // REMOVED: The duplicate onNewTermStart() and setChair/setViceChair calls

        termEndTime = System.currentTimeMillis() + WEEK_MS;

        // ... rest of broadcast messages stay the same

        broadcast(server, Text.literal("═══════════════════════════════════").formatted(Formatting.GOLD));
        broadcast(server, Text.literal("    ✓ ELECTION RESULTS ✓").formatted(Formatting.GREEN, Formatting.BOLD));
        broadcast(server, Text.literal("═══════════════════════════════════").formatted(Formatting.GOLD));

        if (newChair != null) {
            broadcast(server, Text.literal("Chair: ").formatted(Formatting.WHITE)
                    .append(Text.literal(DataManager.getPlayerName(newChair)).formatted(Formatting.GREEN, Formatting.BOLD)));
        } else {
            broadcast(server, Text.literal("No Chair elected").formatted(Formatting.RED));
        }

        if (newViceChair != null) {
            broadcast(server, Text.literal("Vice Chair: ").formatted(Formatting.WHITE)
                    .append(Text.literal(DataManager.getPlayerName(newViceChair)).formatted(Formatting.AQUA)));
        }

        broadcast(server, Text.literal(""));
        broadcast(server, Text.literal("Vote Counts:").formatted(Formatting.GRAY));
        for (Map.Entry<String, Integer> entry : sorted) {
            broadcast(server, Text.literal(" " + DataManager.getPlayerName(entry.getKey()) + ": " + entry.getValue()).formatted(Formatting.WHITE));
        }
        broadcast(server, Text.literal("═══════════════════════════════════").formatted(Formatting.GOLD));

        DataManager.save(server);
    }

    public static void startEmergencyElection(MinecraftServer server) {
        if (DictatorManager.isDictatorActive()) {
            broadcast(server, Text.literal("Remove the dictator first with /dictator remove").formatted(Formatting.RED));
            return;
        }

        termEndTime = System.currentTimeMillis();
        startElection(server);
        DataManager.setChair(null);
        DataManager.setViceChair(null);
        PerkManager.clearAllPerks();
        PerkManager.onNewTermStart(); // ADDED: Allow perk selection after emergency election
        broadcast(server, Text.literal("⚠ EMERGENCY ELECTION CALLED ⚠").formatted(Formatting.RED, Formatting.BOLD));
    }

    public static void castVote(ServerPlayerEntity player, String candidateUuid) {
        String voterUuid = player.getUuidAsString();

        if (votedPlayers.containsKey(voterUuid)) {
            player.sendMessage(Text.literal("You have already voted!").formatted(Formatting.RED));
            return;
        }

        if (voterUuid.equals(candidateUuid)) {
            player.sendMessage(Text.literal("You cannot vote for yourself!").formatted(Formatting.RED));
            return;
        }

        if (!candidates.contains(candidateUuid)) {
            player.sendMessage(Text.literal("Invalid candidate!").formatted(Formatting.RED));
            return;
        }

        votedPlayers.put(voterUuid, candidateUuid);
        votes.put(candidateUuid, votes.getOrDefault(candidateUuid, 0) + 1);

        player.sendMessage(Text.literal("Vote cast for " + DataManager.getPlayerName(candidateUuid) + "!").formatted(Formatting.GREEN));
        DataManager.save(PoliticalServer.server);
    }

    public static void startImpeachment(MinecraftServer server) {
        impeachmentActive = true;
        impeachYes = 0;
        impeachNo = 0;
        impeachVoted.clear();

        broadcast(server, Text.literal("═══════════════════════════════════").formatted(Formatting.RED));
        broadcast(server, Text.literal("  ⚖ IMPEACHMENT VOTE STARTED ⚖").formatted(Formatting.RED, Formatting.BOLD));
        broadcast(server, Text.literal("═══════════════════════════════════").formatted(Formatting.RED));
        broadcast(server, Text.literal("Use /impeachment vote to vote YES or NO").formatted(Formatting.YELLOW)); // CHANGED: Updated instruction
        broadcast(server, Text.literal("2/3 majority needed, minimum 5 voters").formatted(Formatting.GRAY));
        broadcast(server, Text.literal("═══════════════════════════════════").formatted(Formatting.RED));

        DataManager.save(server);
    }

    public static void castImpeachVote(ServerPlayerEntity player, boolean voteYes) {
        String uuid = player.getUuidAsString();

        if (impeachVoted.contains(uuid)) {
            player.sendMessage(Text.literal("You have already voted!").formatted(Formatting.RED));
            return;
        }

        impeachVoted.add(uuid);
        if (voteYes) {
            impeachYes++;
        } else {
            impeachNo++;
        }

        player.sendMessage(Text.literal("Vote recorded!").formatted(Formatting.GREEN));

        int total = impeachYes + impeachNo;
        if (total >= 5) {
            double yesPercent = (double) impeachYes / total;
            if (yesPercent >= 0.666) {
                broadcast(PoliticalServer.server, Text.literal("⚠ IMPEACHMENT PASSED ⚠").formatted(Formatting.RED, Formatting.BOLD));
                impeachmentActive = false;
                startEmergencyElection(PoliticalServer.server);
            } else if ((double) impeachNo / total > 0.334) {
                broadcast(PoliticalServer.server, Text.literal("Impeachment vote failed.").formatted(Formatting.GREEN));
                impeachmentActive = false;
            }
        }

        DataManager.save(PoliticalServer.server);
    }

    public static void endImpeachmentVote(MinecraftServer server) {
        int total = impeachYes + impeachNo;

        if (total < 5) {
            broadcast(server, Text.literal("Impeachment vote voided - less than 5 voters.").formatted(Formatting.GRAY));
        } else {
            double yesPercent = (double) impeachYes / total;
            if (yesPercent >= 0.666) {
                broadcast(server, Text.literal("⚠ IMPEACHMENT PASSED ⚠").formatted(Formatting.RED, Formatting.BOLD));
                startEmergencyElection(server);
            } else {
                broadcast(server, Text.literal("Impeachment vote failed.").formatted(Formatting.GREEN));
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
            broadcast(server, Text.literal("Remove the dictator first with /dictator remove").formatted(Formatting.RED));
            return;
        }

        termEndTime = System.currentTimeMillis();
        startElection(server);
    }

    private static void broadcast(MinecraftServer server, Text message) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            player.sendMessage(message);
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
}