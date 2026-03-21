package com.political;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.server.network.ServerPlayerEntity;

public class DataManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static SaveData data = new SaveData();
    public Map<String, String> playerHomes = new HashMap<>();
    public Map<String, Integer> playerCoins = new HashMap<>();
    public static void load(MinecraftServer server) {
        Path path = getDataPath(server);
        if (Files.exists(path)) {
            try {
                Reader reader = Files.newBufferedReader(path);
                data = GSON.fromJson(reader, SaveData.class);
                reader.close();
                if (data == null) {
                    data = new SaveData();
                }
                SlayerManager.loadActiveQuests(data.activeSlayerQuests);
                PerkManager.setActivePerks(data.activePerks);
                PerkManager.setLastChairPerks(data.lastChairPerks);
                PerkManager.setChairSelectedPerks(data.chairSelectedPerks);
                PerkManager.setViceChairPerk(data.viceChairPerk);
                PerkManager.setChairPerksSetThisTerm(data.chairPerksSetThisTerm);
                PerkManager.setViceChairPerksSetThisTerm(data.viceChairPerksSetThisTerm);
                PerkManager.setPreviousTermPerks(data.previousTermPerks);
                ElectionManager.loadFromData(data);
                PrisonManager.loadFromData(data);
                TaxManager.loadFromData(data);
                DictatorManager.loadFromData(data);
                SpawnManager.loadFromData(data);  // ADD THIS LINE
                SpawnProtectionManager.loadFromData(data);
                PlayerBuffManager.loadBuffs(data.playerBuffs);

                // Migrate old single-loan playerBankLoan → playerBankLoans
                if (data.playerBankLoans == null) {
                    data.playerBankLoans = new java.util.HashMap<>();
                }
                if (data.playerBankLoan != null) {
                    for (Map.Entry<String, Integer> e : data.playerBankLoan.entrySet()) {
                        if (e.getValue() > 0 && !data.playerBankLoans.containsKey(e.getKey())) {
                            List<LoanEntry> migrated = new ArrayList<>();
                            migrated.add(new LoanEntry(e.getValue(), 0.05));
                            data.playerBankLoans.put(e.getKey(), migrated);
                        }
                    }
                    data.playerBankLoan.clear();
                }

            } catch (IOException e) {
                PoliticalServer.LOGGER.error("Failed to load data", e);
            }
        }
    }

    public static void save(MinecraftServer server) {
        if (server == null) {
            return;
        }
        data.activeSlayerQuests = SlayerManager.getActiveQuests();
        data.activePerks = PerkManager.getActivePerks();
        data.lastChairPerks = PerkManager.getLastChairPerks();
        data.chairSelectedPerks = PerkManager.getChairSelectedPerks();
        data.viceChairPerk = PerkManager.getViceChairPerk();
        data.chairPerksSetThisTerm = PerkManager.isChairPerksSetThisTerm();
        data.viceChairPerksSetThisTerm = PerkManager.isViceChairPerksSetThisTerm();
        data.previousTermPerks = PerkManager.getPreviousTermPerks();
        ElectionManager.saveToData(data);
        PrisonManager.saveToData(data);
        TaxManager.saveToData(data);
        DictatorManager.saveToData(data);
        SpawnManager.saveToData(data);  // ADD THIS LINE
        SpawnProtectionManager.saveToData(data);
        data.playerBuffs = PlayerBuffManager.getAllBuffData();

        Path path = getDataPath(server);
        try {
            Files.createDirectories(path.getParent());
            Writer writer = Files.newBufferedWriter(path);
            GSON.toJson(data, writer);
            writer.close();
        } catch (IOException e) {
            PoliticalServer.LOGGER.error("Failed to save data", e);
        }
    }
    public static int getCoins(String uuid) {
        return data.playerCoins.getOrDefault(uuid, 0);
    }

    public static void setCoins(String uuid, int amount) {
        data.playerCoins.put(uuid, Math.max(0, amount));
    }

    public static void addCoins(String uuid, int amount) {
        setCoins(uuid, getCoins(uuid) + amount);
    }

    public static boolean removeCoins(String uuid, int amount) {
        int current = getCoins(uuid);
        if (current < amount) return false;
        setCoins(uuid, current - amount);
        return true;
    }

    // ============================================================
    // BANK SYSTEM
    // ============================================================

    public static int getBankMain(String uuid) {
        return data.playerBankMain.getOrDefault(uuid, 0);
    }

    public static void setBankMain(String uuid, int amount) {
        data.playerBankMain.put(uuid, Math.max(0, amount));
    }

    public static int getBankSavings(String uuid) {
        return data.playerBankSavings.getOrDefault(uuid, 0);
    }

    public static void setBankSavings(String uuid, int amount) {
        data.playerBankSavings.put(uuid, Math.max(0, amount));
    }

    public static int getBankLoan(String uuid) {
        List<LoanEntry> loans = getLoans(uuid);
        return loans.stream().mapToInt(l -> l.amount).sum();
    }

    public static void setBankLoan(String uuid, int amount) {
        // Legacy setter: replace all loans with a single loan at 5% if amount > 0
        if (amount <= 0) {
            data.playerBankLoans.remove(uuid);
        } else {
            List<LoanEntry> loans = new ArrayList<>();
            loans.add(new LoanEntry(amount, 0.05));
            data.playerBankLoans.put(uuid, loans);
        }
    }

    // ── Multi-loan helpers ───────────────────────────────────────────────────

    public static List<LoanEntry> getLoans(String uuid) {
        return data.playerBankLoans.getOrDefault(uuid, new ArrayList<>());
    }

    public static void addLoan(String uuid, int amount, double interestRate) {
        List<LoanEntry> loans = new ArrayList<>(getLoans(uuid));
        loans.add(new LoanEntry(amount, interestRate));
        data.playerBankLoans.put(uuid, loans);
    }

    public static void removeLoan(String uuid, int index) {
        List<LoanEntry> loans = new ArrayList<>(getLoans(uuid));
        if (index >= 0 && index < loans.size()) {
            loans.remove(index);
            if (loans.isEmpty()) {
                data.playerBankLoans.remove(uuid);
            } else {
                data.playerBankLoans.put(uuid, loans);
            }
        }
    }

    public static void setLoanAmount(String uuid, int index, int amount) {
        List<LoanEntry> loans = new ArrayList<>(getLoans(uuid));
        if (index >= 0 && index < loans.size()) {
            loans.set(index, new LoanEntry(amount, loans.get(index).interestRate));
            data.playerBankLoans.put(uuid, loans);
        }
    }

    /** Returns the interest rate (as a fraction) for the next new loan based on current loan count. */
    public static double getNextLoanInterestRate(String uuid) {
        int count = getLoans(uuid).size();
        // 5%, 10%, 20%, 40%, 80% for loans 1-5
        return 0.05 * Math.pow(2, count);
    }

    /** A single bank loan entry with its own amount and interest rate. */
    public static class LoanEntry {
        public int amount;
        public double interestRate; // e.g. 0.05 for 5% per hour

        public LoanEntry() {} // for GSON

        public LoanEntry(int amount, double interestRate) {
            this.amount = amount;
            this.interestRate = interestRate;
        }
    }

    public static long getBankSavingsWithdrawTime(String uuid) {
        return data.playerBankSavingsWithdrawTime.getOrDefault(uuid, 0L);
    }

    public static void setBankSavingsWithdrawTime(String uuid, long timestamp) {
        data.playerBankSavingsWithdrawTime.put(uuid, timestamp);
    }

    // ── Government-controlled interest rates ────────────────────────────────

    public static double getMainAccountInterestRate() {
        return data.mainAccountInterestRate;
    }

    public static void setMainAccountInterestRate(double rate) {
        data.mainAccountInterestRate = Math.max(0.0, Math.min(0.20, rate));
    }

    public static double getSavingsAccountInterestRate() {
        return data.savingsAccountInterestRate;
    }

    public static void setSavingsAccountInterestRate(double rate) {
        data.savingsAccountInterestRate = Math.max(0.0, Math.min(0.50, rate));
    }
    private static Path getDataPath(MinecraftServer server) {
        return server.getSavePath(WorldSavePath.ROOT).resolve("political_data.json");
    }

    // ============================================================
    // PLAYER ROLE GETTERS/SETTERS
    // ============================================================

    public static String getChair() {
        return data.chair;
    }

    public static void setChair(String uuid) {
        String oldChair = data.chair;

        // If chair is changing to a different person, clear chair perks
        if (oldChair != null && !oldChair.equals(uuid)) {
            // Remove chair's perks from active perks
            List<String> chairPerks = PerkManager.getChairSelectedPerks();
            List<String> activePerks = PerkManager.getActivePerks();
            activePerks.removeAll(chairPerks);
            PerkManager.setActivePerks(activePerks);
            PerkManager.setChairSelectedPerks(new ArrayList<>());

            // Unlock chair perk selection for new chair
            PerkManager.setChairPerksSetThisTerm(false);

            // Reapply remaining perks to all players
            if (PoliticalServer.server != null) {
                for (ServerPlayerEntity player : PoliticalServer.server.getPlayerManager().getPlayerList()) {
                    PerkManager.applyActivePerks(player);
                }
            }
            ElectionManager.resetImpeachment();
            DataManager.save(PoliticalServer.server);
        }

        data.chair = uuid;
    }

    public static String getViceChair() {
        return data.viceChair;
    }

    public static void setViceChair(String uuid) {
        String oldViceChair = data.viceChair;

        // If vice chair is changing to a different person, clear VC perks
        if (oldViceChair != null && !oldViceChair.equals(uuid)) {
            // Remove vice chair's perk from active perks
            String vcPerk = PerkManager.getViceChairPerk();
            if (vcPerk != null) {
                List<String> activePerks = PerkManager.getActivePerks();
                activePerks.remove(vcPerk);
                PerkManager.setActivePerks(activePerks);
            }
            PerkManager.setViceChairPerk(null);

            // Unlock vice chair perk selection for new VC
            PerkManager.setViceChairPerksSetThisTerm(false);

            // Reapply remaining perks to all players
            if (PoliticalServer.server != null) {
                for (ServerPlayerEntity player : PoliticalServer.server.getPlayerManager().getPlayerList()) {
                    PerkManager.applyActivePerks(player);
                }
            }
            ElectionManager.resetImpeachment();
        }

        data.viceChair = uuid;
    }

    public static String getJudge() {
        return data.judge;
    }

    public static void setJudge(String uuid) {
        data.judge = uuid;
    }

    public static java.util.Map<String, Long> getPrisoners() {
        return data.prisoners;
    }

    public static int getChairTermCount() {
        return data.chairTermCount;
    }

    public static void setChairTermCount(int count) {
        data.chairTermCount = count;
    }

    // ============================================================
    // PLAYER REGISTRY
    // ============================================================

    public static void registerPlayer(String uuid, String name) {
        data.playerNames.put(uuid, name);
    }

    public static String getPlayerName(String uuid) {
        return data.playerNames.getOrDefault(uuid, "Unknown");
    }

    public static Map<String, String> getAllPlayers() {
        return new HashMap<>(data.playerNames);
    }

    // ============================================================
    // CREDITS SYSTEM (NON-PHYSICAL)
    // ============================================================

    public static int getCredits(String uuid) {
        return data.playerCredits.getOrDefault(uuid, 0);
    }

    public static void setCredits(String uuid, int amount) {
        data.playerCredits.put(uuid, Math.max(0, amount));
    }

    public static void addCredits(String uuid, int amount) {
        setCredits(uuid, getCredits(uuid) + amount);
    }

    public static boolean removeCredits(String uuid, int amount) {
        int current = getCredits(uuid);
        if (current < amount) return false;
        setCredits(uuid, current - amount);
        return true;
    }

    // ============================================================
    // GOVERNMENT TREASURY
    // ============================================================

    public static int getGovernmentTreasury() {
        return data.governmentTreasury;
    }

    public static void setGovernmentTreasury(int amount) {
        data.governmentTreasury = Math.max(0, amount);
    }

    public static void addGovernmentTreasury(int amount) {
        data.governmentTreasury = Math.max(0, data.governmentTreasury + amount);
    }

    public static boolean removeGovernmentTreasury(int amount) {
        if (data.governmentTreasury < amount) return false;
        data.governmentTreasury -= amount;
        return true;
    }

    // ============================================================
    // GAME SETTINGS
    // ============================================================

    public static float getBossHpMultiplier(SlayerManager.SlayerType type) {
        return data.bossHpMultipliers.getOrDefault(type.name(), 1.0f);
    }
    public static void setBossHpMultiplier(SlayerManager.SlayerType type, float mult) {
        data.bossHpMultipliers.put(type.name(), Math.max(0.1f, Math.min(10.0f, mult)));
    }
    public static float getBossDamageMultiplier(SlayerManager.SlayerType type) {
        return data.bossDamageMultipliers.getOrDefault(type.name(), 1.0f);
    }
    public static void setBossDamageMultiplier(SlayerManager.SlayerType type, float mult) {
        data.bossDamageMultipliers.put(type.name(), Math.max(0.1f, Math.min(10.0f, mult)));
    }
    public static float getBossKillsRequiredMultiplier() { return data.bossKillsRequiredMultiplier; }
    public static void setBossKillsRequiredMultiplier(float mult) { data.bossKillsRequiredMultiplier = Math.max(0.1f, Math.min(10.0f, mult)); }
    public static boolean isBossAbilitiesEnabled(SlayerManager.SlayerType type) {
        return data.bossAbilitiesEnabled.getOrDefault(type.name(), true);
    }
    public static void setBossAbilitiesEnabled(SlayerManager.SlayerType type, boolean enabled) {
        data.bossAbilitiesEnabled.put(type.name(), enabled);
    }
    public static float getBossXpMultiplier(SlayerManager.SlayerType type) {
        return data.bossXpMultipliers.getOrDefault(type.name(), 1.0f);
    }
    public static void setBossXpMultiplier(SlayerManager.SlayerType type, float mult) {
        data.bossXpMultipliers.put(type.name(), Math.max(0.1f, Math.min(10.0f, mult)));
    }
    public static int getBossAbilityCooldown(SlayerManager.SlayerType type) {
        return data.bossAbilityCooldowns.getOrDefault(type.name(), 60);
    }
    public static void setBossAbilityCooldown(SlayerManager.SlayerType type, int seconds) {
        data.bossAbilityCooldowns.put(type.name(), Math.max(5, Math.min(300, seconds)));
    }
    public static int getBossSpecificDefense(SlayerManager.SlayerType type) {
        return data.bossSpecificDefense.getOrDefault(type.name(), 0);
    }
    public static void setBossSpecificDefense(SlayerManager.SlayerType type, int pct) {
        data.bossSpecificDefense.put(type.name(), Math.max(0, Math.min(100, pct)));
    }
    public static int getBossUniversalDefense() {
        return data.bossUniversalDefense;
    }
    public static void setBossUniversalDefense(int pct) {
        data.bossUniversalDefense = Math.max(0, Math.min(100, pct));
    }
    public static void resetAllBossConfigs() {
        data.bossHpMultipliers.clear();
        data.bossDamageMultipliers.clear();
        data.bossXpMultipliers.clear();
        data.bossAbilityCooldowns.clear();
        data.bossSpecificDefense.clear();
        data.bossUniversalDefense = 0;
        data.bossKillsRequiredMultiplier = 1.0f;
    }
    public static int getItemLevelRequirement(String itemId, int defaultLevel) {
        return data.itemLevelRequirements.getOrDefault(itemId, defaultLevel);
    }
    public static void setItemLevelRequirement(String itemId, int level) {
        data.itemLevelRequirements.put(itemId, Math.max(0, level));
    }
    public static int getDefenceCapPct() { return data.defenceCapPct; }
    public static void setDefenceCapPct(int pct) { data.defenceCapPct = Math.max(1, Math.min(100, pct)); }
    public static int getT1SetBonusPct() { return data.t1SetBonusPct; }
    public static void setT1SetBonusPct(int pct) { data.t1SetBonusPct = Math.max(0, Math.min(100, pct)); }
    public static int getT2SetBonusPct() { return data.t2SetBonusPct; }
    public static void setT2SetBonusPct(int pct) { data.t2SetBonusPct = Math.max(0, Math.min(100, pct)); }
    public static double getT1SwordDamagePercent() { return data.t1SwordDamagePercent; }
    public static void setT1SwordDamagePercent(double pct) { data.t1SwordDamagePercent = Math.max(0.0, Math.min(100.0, pct)); }
    public static double getT2SwordDamagePercent() { return data.t2SwordDamagePercent; }
    public static void setT2SwordDamagePercent(double pct) { data.t2SwordDamagePercent = Math.max(0.0, Math.min(100.0, pct)); }
    public static float getChunkDropMultiplier() { return data.chunkDropMultiplier; }
    public static void setChunkDropMultiplier(float mult) { data.chunkDropMultiplier = Math.max(0.0f, Math.min(10.0f, mult)); }
    public static float getCoreDropMultiplier() { return data.coreDropMultiplier; }
    public static void setCoreDropMultiplier(float mult) { data.coreDropMultiplier = Math.max(0.0f, Math.min(10.0f, mult)); }
    public static float getCoinRewardMultiplier() { return data.coinRewardMultiplier; }
    public static void setCoinRewardMultiplier(float mult) { data.coinRewardMultiplier = Math.max(0.0f, Math.min(10.0f, mult)); }
    public static float getFleshDropMultiplier() { return data.fleshDropMultiplier; }
    public static void setFleshDropMultiplier(float mult) { data.fleshDropMultiplier = Math.max(0.0f, Math.min(10.0f, mult)); }
    public static float getMobHealthScalingMultiplier() { return data.mobHealthScalingMultiplier; }
    public static void setMobHealthScalingMultiplier(float mult) { data.mobHealthScalingMultiplier = Math.max(0.1f, Math.min(10.0f, mult)); }
    public static boolean isScoreboardShowAuction() { return data.scoreboardShowAuction; }
    public static void setScoreboardShowAuction(boolean show) { data.scoreboardShowAuction = show; }
    public static boolean isScoreboardShowBountyGear() { return data.scoreboardShowBountyGear; }
    public static void setScoreboardShowBountyGear(boolean show) { data.scoreboardShowBountyGear = show; }

    // ============================================================
    // PLAYER BUFF TRACKING
    // ============================================================

    public static int getConsecutiveElectionWins(String uuid) {
        return data.consecutiveElectionWins.getOrDefault(uuid, 0);
    }
    public static void setConsecutiveElectionWins(String uuid, int wins) {
        data.consecutiveElectionWins.put(uuid, Math.max(0, wins));
    }
    public static void incrementConsecutiveElectionWins(String uuid) {
        setConsecutiveElectionWins(uuid, getConsecutiveElectionWins(uuid) + 1);
    }
    public static void resetConsecutiveElectionWins(String uuid) {
        data.consecutiveElectionWins.put(uuid, 0);
    }

    public static long getTotalCoinsEarned(String uuid) {
        return data.totalCoinsEarned.getOrDefault(uuid, 0L);
    }
    public static void addTotalCoinsEarned(String uuid, long amount) {
        data.totalCoinsEarned.put(uuid, getTotalCoinsEarned(uuid) + amount);
    }

    public static long getPlaytimeMinutes(String uuid) {
        return data.playtimeMinutes.getOrDefault(uuid, 0L);
    }
    public static int getPlaytimeHours(String uuid) {
        return (int) (getPlaytimeMinutes(uuid) / 60);
    }
    public static void addPlaytimeMinutes(String uuid, long minutes) {
        data.playtimeMinutes.put(uuid, getPlaytimeMinutes(uuid) + minutes);
    }

    public static int getTotalVotesReceived(String uuid) {
        return data.totalVotesReceived.getOrDefault(uuid, 0);
    }
    public static void addVotesReceived(String uuid, int votes) {
        data.totalVotesReceived.put(uuid, getTotalVotesReceived(uuid) + votes);
    }

    public static int getLawsPassed(String uuid) {
        return data.lawsPassed.getOrDefault(uuid, 0);
    }
    public static void incrementLawsPassed(String uuid) {
        data.lawsPassed.put(uuid, getLawsPassed(uuid) + 1);
    }

    public static int getJudicialRulings(String uuid) {
        return data.judicialRulings.getOrDefault(uuid, 0);
    }
    public static void incrementJudicialRulings(String uuid) {
        data.judicialRulings.put(uuid, getJudicialRulings(uuid) + 1);
    }

    public static int getTier5BossKills(String uuid) {
        return data.tier5BossKills.getOrDefault(uuid, 0);
    }
    public static void incrementTier5BossKills(String uuid) {
        data.tier5BossKills.put(uuid, getTier5BossKills(uuid) + 1);
    }

    public static int getZombieBossKills(String uuid) {
        return data.zombieBossKills.getOrDefault(uuid, 0);
    }
    public static void incrementZombieBossKills(String uuid) {
        data.zombieBossKills.put(uuid, getZombieBossKills(uuid) + 1);
    }

    public static int getTotalTrades(String uuid) {
        return data.totalTrades.getOrDefault(uuid, 0);
    }
    public static void incrementTotalTrades(String uuid) {
        data.totalTrades.put(uuid, getTotalTrades(uuid) + 1);
    }

    public static int getOresMined(String uuid) {
        return data.oresMined.getOrDefault(uuid, 0);
    }
    public static void incrementOresMined(String uuid, int amount) {
        data.oresMined.put(uuid, getOresMined(uuid) + amount);
    }

    public static int getUniqueItemsCollected(String uuid) {
        return data.uniqueItemsCollected.getOrDefault(uuid, 0);
    }
    public static void setUniqueItemsCollected(String uuid, int count) {
        data.uniqueItemsCollected.put(uuid, count);
    }

    public static int getPlayersMentored(String uuid) {
        return data.playersMentored.getOrDefault(uuid, 0);
    }
    public static void incrementPlayersMentored(String uuid) {
        data.playersMentored.put(uuid, getPlayersMentored(uuid) + 1);
    }

    public static int getConsecutiveRareDrops(String uuid) {
        return data.consecutiveRareDrops.getOrDefault(uuid, 0);
    }
    public static void setConsecutiveRareDrops(String uuid, int count) {
        data.consecutiveRareDrops.put(uuid, count);
    }
    public static void incrementConsecutiveRareDrops(String uuid) {
        setConsecutiveRareDrops(uuid, getConsecutiveRareDrops(uuid) + 1);
    }
    public static void resetConsecutiveRareDrops(String uuid) {
        setConsecutiveRareDrops(uuid, 0);
    }

    // ============================================================
    // PLAYER TITLES
    // ============================================================

    public static void setPlayerTitle(String uuid, String title, String color) {
        data.playerTitles.put(uuid, title);
        data.playerTitleColors.put(uuid, color);
    }

    public static String getPlayerTitle(String uuid) {
        return data.playerTitles.getOrDefault(uuid, null);
    }

    public static String getPlayerTitleColor(String uuid) {
        return data.playerTitleColors.getOrDefault(uuid, "f"); // Default to white
    }

    public static void removePlayerTitle(String uuid) {
        data.playerTitles.remove(uuid);
        data.playerTitleColors.remove(uuid);
    }

    // ============================================================
    // DATA ACCESS
    // ============================================================

    public static SaveData getData() {
        return data;
    }

    // ============================================================
    // SAVE DATA CLASS
    // ============================================================

    public static class SaveData {
        // Roles
        public String chair = null;
        public String viceChair = null;
        public String judge = null;
        public int chairTermCount = 0;
        public Map<String, Integer> playerCoins = new HashMap<>();
        public Map<String, String> playerHomes = new HashMap<>();
        public Map<String, SlayerData.PlayerSlayerData> playerSlayerData = new HashMap<>();
        public Map<String, SlayerManager.ActiveQuest> activeSlayerQuests = new HashMap<>();

// Add these inside the SaveData class, after the Dictator fields:

// Add these fields inside SaveData class:



        // Election
        public long termEndTime = 0;
        public long electionEndTime = 0;
        public boolean electionActive = false;
        public boolean electionSystemEnabled = false;
        public boolean electionSystemPaused = false;
        public Map<String, Integer> votes = new HashMap<>();
        public Map<String, String> votedPlayers = new HashMap<>();
        public List<String> candidates = null;

        // Player registry
        public Map<String, String> playerNames = new HashMap<>();

        // Perks
        public List<String> activePerks = null;
        public List<String> lastChairPerks = null;
        public List<String> chairSelectedPerks = null;
        public String viceChairPerk = null;
        public boolean chairPerksSetThisTerm = false;
        public boolean viceChairPerksSetThisTerm = false;
        public List<String> previousTermPerks = null;

        // Impeachment
        public boolean impeachmentActive = false;
        public int impeachYes = 0;
        public int impeachNo = 0;
        public List<String> impeachVoted = new ArrayList<>();

        // Prison
        public Map<String, Long> prisoners = new HashMap<>();
        public Map<String, String> prisonerLocations = new HashMap<>();

        // Tax
        public boolean taxEnabled = false;
        public int dailyTaxAmount = 5;
        public long lastTaxTime = 0;
        public Map<String, Integer> playerTaxOwed = new HashMap<>();

        // Credits (non-physical)
        public Map<String, Integer> playerCredits = new HashMap<>();

        // Bank system
        public Map<String, Integer> playerBankMain = new HashMap<>();
        public Map<String, Integer> playerBankSavings = new HashMap<>();
        // Legacy single-loan field kept for backward compatibility (migrated on load)
        public Map<String, Integer> playerBankLoan = new HashMap<>();
        public Map<String, List<LoanEntry>> playerBankLoans = new HashMap<>();
        public Map<String, Long> playerBankSavingsWithdrawTime = new HashMap<>();

        // Government-controlled interest rates (set via /chair)
        public double mainAccountInterestRate = 0.01;    // 1% per hour default
        public double savingsAccountInterestRate = 0.05; // 5% per hour default

        // Dictator
        public boolean dictatorActive = false;
        public String dictator = null;
        public boolean dictatorTaxEnabled = false;
        public int dictatorTaxAmount = 0;
        public String previousJudge = null;  // ADD THIS LINE
        // Add these fields inside the SaveData class:

        // Spawn location
        public String spawnWorld = null;
        public double spawnX = 0;
        public double spawnY = 64;
        public double spawnZ = 0;
        public float spawnYaw = 0;
        public float spawnPitch = 0;

        // Spawn protection region
        public boolean spawnProtectionActive = false;
        public int spawnProtectionMinX = 0;
        public int spawnProtectionMaxX = 0;
        public int spawnProtectionMinY = -64;
        public int spawnProtectionMaxY = 320;
        public int spawnProtectionMinZ = 0;
        public int spawnProtectionMaxZ = 0;

    // Government Treasury
    public int governmentTreasury = 0;

    // Game settings - Boss
    public Map<String, Float> bossHpMultipliers = new HashMap<>();
    public Map<String, Float> bossDamageMultipliers = new HashMap<>();
    public float bossKillsRequiredMultiplier = 1.0f;
    public Map<String, Boolean> bossAbilitiesEnabled = new HashMap<>();
    public Map<String, Float> bossXpMultipliers = new HashMap<>();
    public Map<String, Integer> bossAbilityCooldowns = new HashMap<>();
    public Map<String, Integer> bossSpecificDefense = new HashMap<>();
    public int bossUniversalDefense = 0;

    // Game settings - Item level requirements
    public Map<String, Integer> itemLevelRequirements = new HashMap<>();

    // Game settings - Defence
    public int defenceCapPct = 85;
    public int t1SetBonusPct = 60;
    public int t2SetBonusPct = 80;
    public int t1SwordSpecificPct = 50;
    public int t2SwordSpecificPct = 60;
    public int t1SwordUniversalPct = 10;
    public int t2SwordUniversalPct = 15;
    public double t1SwordDamagePercent = 2.5;
    public double t2SwordDamagePercent = 5.0;

    // Game settings - Drops
    public float chunkDropMultiplier = 1.0f;
    public float coreDropMultiplier = 1.0f;
    public float coinRewardMultiplier = 1.0f;
    public float fleshDropMultiplier = 1.0f;

    // Game settings - Mobs
    public float mobHealthScalingMultiplier = 1.0f;

    // Game settings - Scoreboard
    public boolean scoreboardShowAuction = true;
    public boolean scoreboardShowBountyGear = true;

    // Player Buffs
    public Map<String, Set<String>> playerBuffs = new HashMap<>();
    public Map<String, Map<String, Long>> buffGrantTimes = new HashMap<>();
    public Map<String, Set<String>> usedBuffEffects = new HashMap<>();

    // Player Titles
    public Map<String, String> playerTitles = new HashMap<>();
    public Map<String, String> playerTitleColors = new HashMap<>();

        // Checkpoints
        public List<CheckpointManager.Checkpoint> checkpoints = new ArrayList<>();

        // Player Buff Progress Tracking
        public Map<String, Integer> consecutiveElectionWins = new HashMap<>();
        public Map<String, Long> totalCoinsEarned = new HashMap<>();
        public Map<String, Long> playtimeMinutes = new HashMap<>();
        public Map<String, Integer> totalVotesReceived = new HashMap<>();
        public Map<String, Integer> lawsPassed = new HashMap<>();
        public Map<String, Integer> judicialRulings = new HashMap<>();
        public Map<String, Integer> tier5BossKills = new HashMap<>();
        public Map<String, Integer> zombieBossKills = new HashMap<>();
        public Map<String, Integer> totalTrades = new HashMap<>();
        public Map<String, Integer> oresMined = new HashMap<>();
        public Map<String, Integer> uniqueItemsCollected = new HashMap<>();
        public Map<String, Integer> playersMentored = new HashMap<>();
        public Map<String, Integer> consecutiveRareDrops = new HashMap<>();
    }
}