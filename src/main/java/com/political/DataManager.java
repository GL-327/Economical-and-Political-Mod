package com.political;
import com.political.government.Treasury;



import com.political.bounty.SlayerData;
import com.political.bounty.SlayerManager;
import com.political.economy.CoinManager;
import com.political.government.DictatorManager;
import com.political.government.ElectionManager;
import com.political.government.PerkManager;
import com.political.party.PoliticalPartyManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

public class DataManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static SaveData data = new SaveData();
    public Map<String, String> playerHomes = new HashMap<>();
    public Map<String, Long> playerCoins = new HashMap<>();
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
                if (data.governmentTreasury == 0 && data.governmentSpendingPot != 0) {
                    data.governmentTreasury = data.governmentSpendingPot;
                }
                if (!data.vatInitialized && data.vatPercent == 0) {
                    data.vatPercent = 5;
                    data.vatInitialized = true;
                }
                SlayerManager.loadActiveQuests(data.activeSlayerQuests);
                PerkManager.setActivePerks(data.activePerks);
                PerkManager.setLastChairPerks(data.lastChairPerks);
                PerkManager.setChairSelectedPerks(data.chairSelectedPerks);
                if (data.viceChairPerks != null && !data.viceChairPerks.isEmpty()) {
                    PerkManager.setViceChairPerks(data.viceChairPerks);
                } else {
                    PerkManager.setViceChairPerk(data.viceChairPerk);
                }
                PerkManager.setChairPerksSetThisTerm(data.chairPerksSetThisTerm);
                PerkManager.setViceChairPerksSetThisTerm(data.viceChairPerksSetThisTerm);
                PerkManager.setPreviousTermPerks(data.previousTermPerks);
                PerkManager.loadTemporaryPerks(data.temporaryPerkExpirations);
                if (!data.electionDeslopMigrated) {
                    data.electionSystemEnabled = true;
                    data.electionDeslopMigrated = true;
                }
                ElectionManager.loadFromData(data);
                PrisonManager.loadFromData(data);
                TaxManager.loadFromData(data);
                DictatorManager.loadFromData(data);
                SpawnManager.loadFromData(data);  
                SpawnProtectionManager.loadFromData(data);
                PlayerBuffManager.loadBuffs(data.playerBuffs);
                CoinSinkManager.loadFromData(data);
                PoliticalPartyManager.loadFromData(data);
                AscensionGrindManager.loadFromData(data);
                BuffProgressManager.loadFromData(data);
                if (data.deepstrikeSyndicate == null) {
                    data.deepstrikeSyndicate = new ArrayList<>();
                }
                if (data.crafterContractKills == null) {
                    data.crafterContractKills = new HashMap<>();
                }
                PartyCrisisManager.onLoaded();

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
        } else {
            data.electionSystemEnabled = true;
            data.electionDeslopMigrated = true;
            ElectionManager.loadFromData(data);
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
        data.viceChairPerks = PerkManager.getViceChairPerks();
        data.chairPerksSetThisTerm = PerkManager.isChairPerksSetThisTerm();
        data.viceChairPerksSetThisTerm = PerkManager.isViceChairPerksSetThisTerm();
        data.previousTermPerks = PerkManager.getPreviousTermPerks();
        data.temporaryPerkExpirations = PerkManager.getTemporaryPerkExpirations();
        ElectionManager.saveToData(data);
        data.electionDeslopMigrated = true;
        PrisonManager.saveToData(data);
        TaxManager.saveToData(data);
        DictatorManager.saveToData(data);
        SpawnManager.saveToData(data);  // ADD THIS LINE
        SpawnProtectionManager.saveToData(data);
        data.playerBuffs = PlayerBuffManager.getAllBuffData();
        CoinSinkManager.saveToData(data);
        PoliticalPartyManager.saveToData(data);
        AscensionGrindManager.saveToData(data);
        BuffProgressManager.saveToData(data);
        data.partyCrisisCooldownUntilMs = PartyCrisisManager.getCooldownUntilMs();

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
    public static long getCoins(String uuid) {
        return data.playerCoins.getOrDefault(uuid, 0L);
    }

    public static void setCoins(String uuid, long amount) {
        data.playerCoins.put(uuid, Math.max(0L, amount));
    }

    public static void addCoins(String uuid, long amount) {
        setCoins(uuid, getCoins(uuid) + amount);
    }

    public static boolean removeCoins(String uuid, long amount) {
        long current = getCoins(uuid);
        if (current < amount) return false;
        setCoins(uuid, (long) current - amount);
        return true;
    }

    private static long asLong(Long value) {
        return value == null ? 0L : value;
    }

    // ============================================================
    // BANK SYSTEM
    // ============================================================

    public static long getBankMain(String uuid) {
        return asLong(data.playerBankMain.get(uuid));
    }

    public static void setBankMain(String uuid, long amount) {
        data.playerBankMain.put(uuid, Math.max(0L, Math.min(5_000_000_000L, amount)));
    }

    public static long getBankSavings(String uuid) {
        return asLong(data.playerBankSavings.get(uuid));
    }

    public static void setBankSavings(String uuid, long amount) {
        data.playerBankSavings.put(uuid, Math.max(0L, Math.min(5_000_000_000L, amount)));
    }

    public static long getBankLoan(String uuid) {
        List<LoanEntry> loans = getLoans(uuid);
        long sum = 0;
        for (LoanEntry loan : loans) sum += loan.amount;
        return sum;
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

    public static void addLoan(String uuid, int amount, double interestRate, String term, int termDays) {
        List<LoanEntry> loans = new ArrayList<>(getLoans(uuid));
        loans.add(new LoanEntry(amount, interestRate, term, termDays));
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
        public int originalAmount;
        public double interestRate;
        public String term = "standard"; // "short", "standard", "long"
        public int termDays = 14;
        public long createdAtMs;
        public int gracePeriodDays = 0;

        public LoanEntry() {}

        public LoanEntry(int amount, double interestRate) {
            this.amount = amount;
            this.originalAmount = amount;
            this.interestRate = interestRate;
            this.createdAtMs = System.currentTimeMillis();
        }

        public LoanEntry(int amount, double interestRate, String term, int termDays) {
            this.amount = amount;
            this.originalAmount = amount;
            this.interestRate = interestRate;
            this.term = term;
            this.termDays = termDays;
            this.createdAtMs = System.currentTimeMillis();
        }

        public boolean isInGracePeriod() {
            return false;
        }

        public long getDaysRemaining() {
            long elapsed = System.currentTimeMillis() - createdAtMs;
            long termMs = termDays * 24L * 60 * 60 * 1000;
            long remaining = termMs - elapsed;
            return Math.max(0, remaining / (24L * 60 * 60 * 1000));
        }

        public long getDaysElapsed() {
            long elapsed = System.currentTimeMillis() - createdAtMs;
            return elapsed / (24L * 60 * 60 * 1000);
        }

        public int getTotalPayback() {
            long daysElapsed = getDaysElapsed();
            double interest = originalAmount * interestRate * Math.max(0, daysElapsed);
            return (int) Math.ceil(originalAmount + interest);
        }

        public int getProjectedTotalPayback() {
            double interest = originalAmount * interestRate * Math.max(0, termDays);
            return (int) Math.ceil(originalAmount + interest);
        }

        public double getPercentPaid() {
            if (originalAmount <= 0) return 1.0;
            return 1.0 - ((double) amount / originalAmount);
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
        data.mainAccountInterestRate = Math.max(0.0, Math.min(data.maxInterestRate, rate));
    }

    public static double getSavingsAccountInterestRate() {
        return data.savingsAccountInterestRate;
    }

    public static void setSavingsAccountInterestRate(double rate) {
        data.savingsAccountInterestRate = Math.max(0.0, Math.min(data.maxInterestRate, rate));
    }

    public static float getLoanInterestRate() {
        return data.loanInterestRate;
    }

    public static void setLoanInterestRate(float rate) {
        data.loanInterestRate = Math.max(data.minLoanRate, Math.min(data.maxInterestRate, rate));
    }

    public static float getMaxInterestRate() {
        return data.maxInterestRate;
    }

    public static float getMinLoanRate() {
        return data.minLoanRate;
    }

    public static boolean areLoansAreFrozen() {
        return data.loansAreFrozen;
    }

    public static void setLoansAreFrozen(boolean frozen) {
        data.loansAreFrozen = frozen;
    }

    public static double getSavingsTierMultiplier(long savingsBalance) {
        if (savingsBalance >= 100_000) return 1.5;
        if (savingsBalance >= 10_000) return 1.2;
        return 1.0;
    }

    public static double getEffectiveSavingsRate(long savingsBalance) {
        return data.savingsAccountInterestRate * getSavingsTierMultiplier(savingsBalance);
    }

    public static double getLoanRateForTerm(String term) {
        float base = data.loanInterestRate;
        switch (term) {
            case "short": return base * 0.7;
            case "long":  return base * 1.3;
            default:      return base;
        }
    }

    public static int getLoanTermDays(String term) {
        switch (term) {
            case "short": return 7;
            case "long":  return 30;
            default:      return 14;
        }
    }

    public static boolean tryLoanForgiveness(String uuid, int loanIndex) {
        List<LoanEntry> loans = new ArrayList<>(getLoans(uuid));
        if (loanIndex < 0 || loanIndex >= loans.size()) return false;
        LoanEntry loan = loans.get(loanIndex);
        if (loan.getPercentPaid() >= 0.90) {
            double roll = Math.random();
            if (roll < 0.01) {
                loans.remove(loanIndex);
                if (loans.isEmpty()) {
                    data.playerBankLoans.remove(uuid);
                } else {
                    data.playerBankLoans.put(uuid, loans);
                }
                return true;
            }
        }
        return false;
    }

    private static Path getDataPath(MinecraftServer server) {
        return server.getWorldPath(LevelResource.ROOT).resolve("political_data.json");
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
                for (ServerPlayer player : PoliticalServer.server.getPlayerList().getPlayers()) {
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
            List<String> vcPerks = PerkManager.getViceChairPerks();
            if (!vcPerks.isEmpty()) {
                List<String> activePerks = PerkManager.getActivePerks();
                activePerks.removeAll(vcPerks);
                PerkManager.setActivePerks(activePerks);
            }
            PerkManager.setViceChairPerks(List.of());

            // Unlock vice chair perk selection for new VC
            PerkManager.setViceChairPerksSetThisTerm(false);

            // Reapply remaining perks to all players
            if (PoliticalServer.server != null) {
                for (ServerPlayer player : PoliticalServer.server.getPlayerList().getPlayers()) {
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

    /**
     * Returns all UUIDs that currently have bank-related state (main/savings/loans),
     * even if they are not present in the player registry map.
     */
    public static java.util.Set<String> getAllBankAccountUuids() {
        java.util.Set<String> out = new java.util.HashSet<>();
        out.addAll(data.playerBankMain.keySet());
        out.addAll(data.playerBankSavings.keySet());
        out.addAll(data.playerBankLoans.keySet());
        return out;
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
        long v = data.governmentTreasury;
        if (v > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        if (v < Integer.MIN_VALUE) return Integer.MIN_VALUE;
        return (int) v;
    }

    public static long getGovernmentTreasuryLong() {
        return data.governmentTreasury;
    }

    public static void setGovernmentTreasury(int amount) {
        data.governmentTreasury = amount;
    }

    public static void setGovernmentTreasury(long amount) {
        data.governmentTreasury = amount;
    }

    public static void addGovernmentTreasury(int amount) {
        addGovernmentTreasury((long) amount);
    }

    public static void addGovernmentTreasury(long amount) {
        data.governmentTreasury += amount;
    }

    public static boolean removeGovernmentTreasury(int amount) {
        return removeGovernmentTreasury((long) amount);
    }

    public static boolean removeGovernmentTreasury(long amount) {
        data.governmentTreasury -= amount;
        return true;
    }

    public static int getVatPercent() {
        if (!data.vatInitialized) {
            data.vatPercent = 5;
            data.vatInitialized = true;
        }
        if (data.vatPercent < 0) data.vatPercent = 0;
        if (data.vatPercent > 30) data.vatPercent = 30;
        return data.vatPercent;
    }

    public static void setVatPercent(int percent) {
        data.vatPercent = Math.max(0, Math.min(30, percent));
        data.vatInitialized = true;
    }

    public static long getElectionTieSeed() {
        if (data.electionTieSeed == 0 && PoliticalServer.server != null) {
            data.electionTieSeed = PoliticalServer.server.overworld().getSeed();
        }
        return data.electionTieSeed;
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

    /** Vanilla play time sync: never decreases stored minutes. */
    public static void setPlaytimeMinutesAtLeast(String uuid, long minutes) {
        data.playtimeMinutes.put(uuid, Math.max(getPlaytimeMinutes(uuid), Math.max(0L, minutes)));
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
        data.consecutiveRareDrops.put(uuid, Math.max(0, count));
    }
    public static void resetConsecutiveRareDrops(String uuid) {
        setConsecutiveRareDrops(uuid, 0);
    }

    // ============================================================
    // BANK UPGRADE SYSTEM
    // ============================================================

    /**
     * Gets the player's bank upgrade level (0-10)
     */
    public static int getBankUpgradeLevel(String uuid) {
        return data.playerBankUpgradeLevel.getOrDefault(uuid, 0);
    }

    /**
     * Sets the player's bank upgrade level
     */
    public static void setBankUpgradeLevel(String uuid, int level) {
        data.playerBankUpgradeLevel.put(uuid, Math.min(10, Math.max(0, level)));
    }

    /**
     * Gets the main account max based on upgrade level.
     * Level 0: 100k, each level adds 400k up to 5B at level 10
     */
    public static long getMainAccountMax(String uuid) {
        int level = getBankUpgradeLevel(uuid);
        if (level == 0) return 100_000;
        // Each level adds 400k to max, capping at 5B at level 10
        return Math.min(5_000_000_000L, 100_000L + (level * 400_000_000L));
    }

    /**
     * Gets the savings account max based on upgrade level.
     * Level 0: 50M, each level adds 450M up to 5B at level 10
     */
    public static long getSavingsAccountMax(String uuid) {
        int level = getBankUpgradeLevel(uuid);
        // Check if player bypasses savings cap (WEALTHY buff)
        if (PlayerBuffManager.bypassesSavingsCap(uuid)) {
            return Long.MAX_VALUE; // Unlimited
        }
        if (level == 0) return 50_000_000;
        // Each level adds 450M to max, capping at 5B at level 10
        return Math.min(5_000_000_000L, 50_000_000L + (level * 450_000_000L));
    }

    /**
     * Gets the cost to upgrade to the next bank level.
     * Base cost: 100M for level 1, doubles each level up to 1B for level 10
     */
    public static long getBankUpgradeCost(String uuid) {
        int currentLevel = getBankUpgradeLevel(uuid);
        if (currentLevel >= 10) return -1; // Max level
        
        // Base cost 100M, doubles each level
        long baseCost = 100_000_000L;
        long cost = baseCost * (1L << currentLevel); // 2^level multiplier
        cost = Math.min(1_000_000_000L, cost); // Cap at 1B
        
        // Apply BANK_UPGRADER discount
        double discount = PlayerBuffManager.getBankUpgradeDiscount(uuid);
        if (discount > 0) {
            cost = (long) (cost * (1.0 - discount));
        }
        
        return cost;
    }

    /**
     * Attempts to purchase a bank upgrade for the player.
     * @return true if successful
     */
    public static boolean purchaseBankUpgrade(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        int currentLevel = getBankUpgradeLevel(uuid);
        if (currentLevel >= 10) {
            player.sendSystemMessage(Component.literal("§c✗ Maximum bank upgrade level reached!"), false);
            return false;
        }
        
        long cost = getBankUpgradeCost(uuid);
        long playerCoins = CoinManager.getCoinsLong(uuid);
        
        if (playerCoins < cost) {
            player.sendSystemMessage(Component.literal("§c✗ Not enough coins! Need " + String.format("%,d", cost) + " coins."), false);
            return false;
        }
        
        CoinManager.removeCoins(uuid, cost);
        setBankUpgradeLevel(uuid, currentLevel + 1);
        
        // Track for BANK_UPGRADER buff
        BuffProgressManager.onBankUpgradePurchase(player);
        
        player.sendSystemMessage(Component.literal("§a✓ Bank upgraded to level " + (currentLevel + 1) + "!"), false);
        player.sendSystemMessage(Component.literal("§7Main Account Cap: " + String.format("%,d", getMainAccountMax(uuid)) + " coins"), false);
        player.sendSystemMessage(Component.literal("§7Savings Account Cap: " + String.format("%,d", getSavingsAccountMax(uuid)) + " coins"), false);
        
        return true;
    }

    // ============================================================
    // GOVERNMENT SPENDING POT
    // ============================================================

    public static long getGovernmentSpendingPot() {
        return data.governmentSpendingPot;
    }

    public static void addToGovernmentSpendingPot(long amount) {
        data.governmentSpendingPot += amount;
    }

    /**
     * Spends from the government pot on approved services.
     * Leaders cannot withdraw this for personal use.
     */
    public static boolean spendFromGovernmentPot(long amount, String purpose) {
        if (data.governmentSpendingPot < amount) return false;
        data.governmentSpendingPot -= amount;
        PoliticalServer.LOGGER.info("[Government] Spent " + amount + " from spending pot on: " + purpose);
        return true;
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
    // POLITICAL PARTY PERSISTENCE (DTOs for Gson)
    // ============================================================

    public static class PartyMemberDTO {
        public long support;
        public long joinedAtMs;
    }

    public static class PartyDTO {
        public String id;
        public String displayName;
        public long budgetCoins;
        /** Short public platform / motto (shown in /pp list). */
        public String platform = "";
        public Map<String, PartyMemberDTO> members = new HashMap<>();
    }

    public static class PerkProposalDTO {
        public String partyId;
        public String perkId;
        public long costCoins;
        public long durationMs;
        public long deadlineMs;
        public List<String> councilUuids = new ArrayList<>();
        public Map<String, Boolean> votesYes = new HashMap<>();
    }

    public static class TreasuryLogDTO {
        public long timestampMs;
        public String action;
        public long amount;
        public String detail;
    }

    public static class PartyEventDTO {
        public String eventType;
        public long startMs;
        public long endMs;
        public Map<String, Long> memberProgress = new HashMap<>();
        public boolean rewarded;
    }

    public static class PoliticalPartySave {
        public List<PartyDTO> parties = new ArrayList<>();
        public Map<String, String> playerParty = new HashMap<>();
        public Map<String, Long> lastDefectMs = new HashMap<>();
        public List<String> completedPartyOnboarding = new ArrayList<>();
        public String rulingPartyId;
        public boolean leaderElectionActive;
        public long leaderElectionEndMs;
        public Map<String, String> leaderElectionVotes = new HashMap<>();
        public List<PerkProposalDTO> pendingProposals = new ArrayList<>();
        public Map<String, Map<String, String>> partyRanks = new HashMap<>();
        public Map<String, List<String>> partyPerks = new HashMap<>();
        public Map<String, List<String>> activeWars = new HashMap<>();
        public Map<String, List<String>> alliances = new HashMap<>();
        public Map<String, List<TreasuryLogDTO>> treasuryLogs = new HashMap<>();
        public Map<String, List<String>> peacePending = new HashMap<>();
        public Map<String, List<String>> alliancePending = new HashMap<>();
        public Map<String, PartyEventDTO> partyEvents = new HashMap<>();
    }

    /** Ascension / sector grind (Skyblock-style collections + MF). */
    public static class AscensionPlayerSave {
        public int magicFind;
        public Map<String, Long> collectionCounts = new HashMap<>();
        public Map<String, Integer> collectionTiers = new HashMap<>();
        public long doubleDropUntilMs;
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
        public Map<String, Long> playerCoins = new HashMap<>();
        public Map<String, String> playerHomes = new HashMap<>();
        public Map<String, SlayerData.PlayerSlayerData> playerSlayerData = new HashMap<>();
        public Map<String, SlayerManager.ActiveQuest> activeSlayerQuests = new HashMap<>();

// Add these inside the SaveData class, after the Dictator fields:

// Add these fields inside SaveData class:



        // Election
        public long termEndTime = 0;
        public long electionEndTime = 0;
        public boolean electionActive = false;
        public boolean electionSystemEnabled = true;
        public boolean electionSystemPaused = false;
        public boolean electionDeslopMigrated = false;
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
        public List<String> viceChairPerks = new ArrayList<>();
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
        public float dailyTaxPercentage = 0.05f;
        public long lastTaxTime = 0;
        public Map<String, Integer> playerTaxOwed = new HashMap<>();

        // Credits (non-physical)
        public Map<String, Integer> playerCredits = new HashMap<>();

        // Bank system
        public Map<String, Long> playerBankMain = new HashMap<>();
        public Map<String, Long> playerBankSavings = new HashMap<>();
        // Legacy single-loan field kept for backward compatibility (migrated on load)
        public Map<String, Integer> playerBankLoan = new HashMap<>();
        public Map<String, List<LoanEntry>> playerBankLoans = new HashMap<>();
        public Map<String, Long> playerBankSavingsWithdrawTime = new HashMap<>();

        // Government-controlled interest rates (set via /chair)
        public double mainAccountInterestRate = 0.01;    // 1% per hour default
        public double savingsAccountInterestRate = 0.05; // 5% per hour default

        // Chair-controlled bank policy
        public float loanInterestRate = 0.05f;   // 5% per day base loan rate
        public float maxInterestRate = 0.10f;    // 10% cap on any rate
        public float minLoanRate = 0.01f;        // 1% floor on loan rate
        public boolean loansAreFrozen = false;

        // Bank upgrade system
        public Map<String, Integer> playerBankUpgradeLevel = new HashMap<>(); // 0-10 levels

        // Government spending pot (legacy alias; the live ledger is governmentTreasury)
        public long governmentSpendingPot = 0;

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

        // Dimension structures (vanilla-style: generated once per world and persisted)
        public java.util.Set<String> generatedDimensionStructures = new java.util.HashSet<>();
        public java.util.Map<String, Long> dimensionStructureCenters = new java.util.HashMap<>();

        public PoliticalPartySave politicalPartySave;

        public Map<String, AscensionPlayerSave> ascensionPlayers = new HashMap<>();

        /** Deepstrike Syndicate members (100M+ entry). */
        public ArrayList<String> deepstrikeSyndicate = new ArrayList<>();

        /** Cooldown for ruling-party crisis mini-games. */
        public long partyCrisisCooldownUntilMs;

        /** Crafter core bounty: kill count toward reward while contract active. */
        public Map<String, Integer> crafterContractKills = new HashMap<>();

        /** Extended buff progress (mirrors {@link BuffProgressManager}); survives restarts. */
        public BuffProgressSnapshot buffProgressSnapshot;

    // Government Treasury (one national ledger; may be negative = debt)
    public long governmentTreasury = 0;
    public int vatPercent = 5;
    public boolean vatInitialized = false;
    public long lastDailyBudgetMs = 0;
    public long electionTieSeed = 0;

    // Temporary Perks (from government spending)
    public Map<String, Long> temporaryPerkExpirations = new HashMap<>();

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

    // Coin Sink System - Lottery & VIP
    public long lotteryPot = 0;
    public long lotteryLastDraw = 0;
    public Map<String, Integer> lotteryTickets = new HashMap<>();
    public Map<String, Long> vipPerkExpirations = new HashMap<>();

    // Coin Sink System - Functional Boosts
    public Map<String, Long> xpBoostExpirations = new HashMap<>();
    public Map<String, Long> luckBoostExpirations = new HashMap<>();
    public Map<String, Long> coinBoostExpirations = new HashMap<>();
    public Map<String, Integer> extraHomes = new HashMap<>();
    public Map<String, Long> deathProtection = new HashMap<>();
    public Map<String, Long> flightExpirations = new HashMap<>();
    public Map<String, Long> miningBoostExpirations = new HashMap<>();
    public Map<String, Long> damageBoostExpirations = new HashMap<>();
    public Map<String, Long> defenseBoostExpirations = new HashMap<>();
    public Set<String> chunkLoaders = new java.util.HashSet<>();
}
}