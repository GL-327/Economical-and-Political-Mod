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
import java.util.Map;import java.util.ArrayList;
import net.minecraft.server.network.ServerPlayerEntity;import java.util.ArrayList;

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

            } catch (IOException e) {
                PoliticalServer.LOGGER.error("Failed to load data", e);
            }
        }
    }

    public static void save(MinecraftServer server) {
        if (server == null) {
            return;
        }

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
        }

        data.chair = uuid;
    }

    public static String getViceChair() {
        return data.viceChair;
    }

    public static void resetImpeachment() {
        DataManager.SaveData data = DataManager.getData();
        data.impeachmentActive = false;
        data.impeachYes = 0;
        data.impeachNo = 0;
        data.impeachVoted.clear();
        DataManager.save(PoliticalServer.server);
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
    }
}