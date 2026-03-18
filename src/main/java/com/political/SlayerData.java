package com.political;

import java.util.HashMap;
import java.util.Map;

import static com.political.DataManager.save;

public class SlayerData {

    // ============================================================
    // PLAYER SLAYER DATA STRUCTURE
    // ============================================================
    private static final Map<String, Integer> playerCredits = new HashMap<>();
    public static class PlayerSlayerData {
        // XP per slayer type
        public Map<String, Long> slayerXp = new HashMap<>();

        // Total bosses killed per type
        public Map<String, Integer> bossesKilled = new HashMap<>();

        // Highest tier completed per type
        public Map<String, Integer> highestTierCompleted = new HashMap<>();

        public PlayerSlayerData() {
            // Initialize all slayer types with 0 XP
            for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
                slayerXp.put(type.name(), 0L);
                bossesKilled.put(type.name(), 0);
                highestTierCompleted.put(type.name(), 0);
            }
        }
    }
    private static Map<String, Long> xpData = new HashMap<>();
    private static Map<String, Integer> levelData = new HashMap<>();
    private static Map<String, Integer> bossData = new HashMap<>();
    // ============================================================
    // DATA ACCESS - Integrates with DataManager.SaveData
    // ============================================================

    private static Map<String, PlayerSlayerData> getSlayerDataMap() {
        DataManager.SaveData data = DataManager.getData();
        if (data.playerSlayerData == null) {
            data.playerSlayerData = new HashMap<>();
        }
        return data.playerSlayerData;
    }

    private static PlayerSlayerData getOrCreate(String playerUuid) {
        Map<String, PlayerSlayerData> dataMap = getSlayerDataMap();
        return dataMap.computeIfAbsent(playerUuid, k -> new PlayerSlayerData());
    }

    // ============================================================
    // XP METHODS
    // ============================================================

    public static long getSlayerXp(String playerUuid, SlayerManager.SlayerType type) {
        PlayerSlayerData data = getOrCreate(playerUuid);
        return data.slayerXp.getOrDefault(type.name(), 0L);
    }

    public static void setSlayerXp(String playerUuid, SlayerManager.SlayerType type, long xp) {
        PlayerSlayerData data = getOrCreate(playerUuid);
        data.slayerXp.put(type.name(), Math.max(0, xp));
    }

    public static void addSlayerXp(String playerUuid, SlayerManager.SlayerType type, long xp) {
        long current = getSlayerXp(playerUuid, type);
        setSlayerXp(playerUuid, type, current + xp);
    }

    public static int getSlayerLevel(String playerUuid, SlayerManager.SlayerType type) {
        long xp = getSlayerXp(playerUuid, type);
        return SlayerManager.getLevelForXp(xp);
    }

    // ============================================================
    // BOSS KILL TRACKING
    // ============================================================

    public static int getBossesKilled(String playerUuid, SlayerManager.SlayerType type) {
        PlayerSlayerData data = getOrCreate(playerUuid);
        return data.bossesKilled.getOrDefault(type.name(), 0);
    }

    public static void incrementBossesKilled(String playerUuid, SlayerManager.SlayerType type) {
        PlayerSlayerData data = getOrCreate(playerUuid);
        int current = data.bossesKilled.getOrDefault(type.name(), 0);
        data.bossesKilled.put(type.name(), current + 1);
    }

    public static int getHighestTier(String playerUuid, SlayerManager.SlayerType type) {
        PlayerSlayerData data = getOrCreate(playerUuid);
        return data.highestTierCompleted.getOrDefault(type.name(), 0);
    }

    public static void updateHighestTier(String playerUuid, SlayerManager.SlayerType type, int tier) {
        PlayerSlayerData data = getOrCreate(playerUuid);
        int current = data.highestTierCompleted.getOrDefault(type.name(), 0);
        if (tier > current) {
            data.highestTierCompleted.put(type.name(), tier);
        }
    }
    public static void addCredits(String uuid, int amount) {
        int current = getCredits(uuid);
        setCredits(uuid, current + amount);
    }

    public static int getCredits(String uuid) {
        // Return credits from your data storage
        // Example if using a Map:
        return playerCredits.getOrDefault(uuid, 0);
    }

    public static void setCredits(String uuid, int amount) {
        // Store credits in your data storage
        // Example if using a Map:
        playerCredits.put(uuid, amount);
    }
    // ============================================================
    // AGGREGATE STATS
    // ============================================================

    public static int getTotalSlayerLevel(String playerUuid) {
        int total = 0;
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            total += getSlayerLevel(playerUuid, type);
        }
        return total;
    }

    public static int getTotalBossesKilled(String playerUuid) {
        int total = 0;
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            total += getBossesKilled(playerUuid, type);
        }
        return total;
    }

    // ============================================================
    // PROGRESS HELPERS
    // ============================================================

    public static double getProgressToNextLevel(String playerUuid, SlayerManager.SlayerType type) {
        long xp = getSlayerXp(playerUuid, type);
        int level = SlayerManager.getLevelForXp(xp);

        if (level >= SlayerManager.MAX_LEVEL) return 1.0;
        if (level == 0) {
            return (double) xp / SlayerManager.XP_REQUIREMENTS[0];
        }

        long currentLevelXp = SlayerManager.XP_REQUIREMENTS[level - 1];
        long nextLevelXp = SlayerManager.XP_REQUIREMENTS[level];
        long xpIntoLevel = xp - currentLevelXp;
        long xpNeeded = nextLevelXp - currentLevelXp;

        return (double) xpIntoLevel / xpNeeded;
    }

    public static long getXpToNextLevel(String playerUuid, SlayerManager.SlayerType type) {
        long xp = getSlayerXp(playerUuid, type);
        int level = SlayerManager.getLevelForXp(xp);

        if (level >= SlayerManager.MAX_LEVEL) return 0;

        long nextLevelXp = SlayerManager.XP_REQUIREMENTS[level];
        return nextLevelXp - xp;
    }

        // Assuming you have something like this:


        // Add these setter methods:



        public static void setSlayerLevel(String uuid, SlayerManager.SlayerType type, int level) {
            String key = uuid + "_" + type.name();
            levelData.put(key, Math.max(0, Math.min(level, SlayerManager.MAX_LEVEL)));
            // Set XP to match level
            if (level > 0 && level <= SlayerManager.XP_REQUIREMENTS.length) {
                xpData.put(key, SlayerManager.XP_REQUIREMENTS[level - 1]);
            } else {
                xpData.put(key, 0L);
            }
            save(PoliticalServer.server);
        }

        public static void setBossesKilled(String uuid, SlayerManager.SlayerType type, int count) {
            String key = uuid + "_" + type.name() + "_bosses";
            bossData.put(key, Math.max(0, count));
            save(PoliticalServer.server);
        }
    }