package com.political;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gson-serializable copy of {@link BuffProgressManager} runtime maps (server restart safe).
 */
public final class BuffProgressSnapshot {

    public Map<String, Integer> bountyQuestsCompleted = new HashMap<>();
    public Map<String, Integer> lawsPassed = new HashMap<>();
    public Map<String, Integer> judicialRulings = new HashMap<>();
    public Map<String, Integer> tier5BossKills = new HashMap<>();
    public Map<String, Integer> zombieBossKills = new HashMap<>();
    public Map<String, Long> totalCoinsAccumulated = new HashMap<>();
    public Map<String, Long> bankBalance = new HashMap<>();
    public Map<String, Integer> oresMined = new HashMap<>();
    public Map<String, List<String>> uniqueItemsCollected = new HashMap<>();
    public Map<String, Integer> totalVotesReceived = new HashMap<>();
    public Map<String, Integer> consecutiveRareDrops = new HashMap<>();
    public Map<String, Long> totalPlaytimeMinutes = new HashMap<>();
    public Map<String, Integer> consecutiveElectionsWon = new HashMap<>();

    public Map<String, Integer> auctionsWon = new HashMap<>();
    public Map<String, Integer> snipedAuctions = new HashMap<>();
    public Map<String, Integer> wardenBossKills = new HashMap<>();
    public Map<String, Integer> enderBossKills = new HashMap<>();
    public Map<String, Integer> spiderBossKills = new HashMap<>();
    public Map<String, Integer> skeletonBossKills = new HashMap<>();
    public Map<String, Integer> prisonEscapes = new HashMap<>();
    public Map<String, Integer> playersImprisoned = new HashMap<>();
    public Map<String, Integer> impeachmentsSurvived = new HashMap<>();
    public Map<String, Integer> termsServed = new HashMap<>();
    public Map<String, Integer> cryptoTrades = new HashMap<>();
    public Map<String, Integer> stockTrades = new HashMap<>();
    public Map<String, Integer> cropsHarvested = new HashMap<>();
    public Map<String, Integer> fishCaught = new HashMap<>();
    public Map<String, Integer> logsChopped = new HashMap<>();

    public Map<String, Integer> warpUses = new HashMap<>();
    public Map<String, Integer> backroomsEscapes = new HashMap<>();
    public Map<String, Long> dimensionTimeMs = new HashMap<>();
    public Map<String, Integer> miningDimensionOres = new HashMap<>();

    public Map<String, Integer> totalDeaths = new HashMap<>();
    public Map<String, Long> totalSyphoned = new HashMap<>();
    public Map<String, Integer> bountyKills = new HashMap<>();
    public Map<String, Long> totalCoinsLostToDeath = new HashMap<>();

    public Map<String, Integer> bankUpgradesPurchased = new HashMap<>();
    public Map<String, Long> lastRareDropTime = new HashMap<>();

    public Map<String, Integer> electionVotesCast = new HashMap<>();
    public Map<String, Long> totalTaxPaid = new HashMap<>();
    public Map<String, Long> totalPartyDonations = new HashMap<>();

    static List<String> copyList(List<String> in) {
        return in == null ? new ArrayList<>() : new ArrayList<>(in);
    }
}
