package com.political;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Progression Tree System
 * 6 branches with 10 tiers each
 * Players earn skill points through gameplay to unlock tiers
 */
public class ProgressionTreeManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProgressionTreeManager.class);


    // ════════════════════════════════════════════════════════════
    // BRANCH DEFINITIONS
    // ════════════════════════════════════════════════════════════

    public enum Branch {
        COMBAT("Combat", "⚔", ChatFormatting.RED,
            "Damage dealt", "Bounty boss damage", "Armor penetration", "Critical hit chance"),
        MINING("Mining", "⛏", ChatFormatting.GRAY,
            "Mining speed", "Ore drop rate", "Gem find chance", "Vein mining range"),
        FARMING("Farming", "🌾", ChatFormatting.GREEN,
            "Crop growth speed", "Harvest yield", "Rare crop chance", "Auto-replant"),
        FISHING("Fishing", "🎣", ChatFormatting.AQUA,
            "Fish catch speed", "Rare fish chance", "Treasure find rate", "Auto-fish duration"),
        EXPLORATION("Exploration", "🧭", ChatFormatting.YELLOW,
            "Movement speed", "Discovery bonus", "Chest loot bonus", "Waypoint capacity"),
        CRAFTING("Crafting", "🔨", ChatFormatting.LIGHT_PURPLE,
            "Craft speed", "Durability bonus", "Enchant boost", "Recipe discount");

        public final String name;
        public final String icon;
        public final ChatFormatting color;
        public final String[] statNames;

        Branch(String name, String icon, ChatFormatting color, String... statNames) {
            this.name = name;
            this.icon = icon;
            this.color = color;
            this.statNames = statNames;
        }
    }

    // ════════════════════════════════════════════════════════════
    // TIER CONFIGURATION
    // ════════════════════════════════════════════════════════════

    public static final int MAX_TIERS = 10;
    
    // Skill points required per tier (cumulative)
    public static final int[] TIER_COSTS = {250, 600, 1400, 3200, 7200, 15000, 30000, 60000, 110000, 200000};
    
    // Bonus per tier (percentage)
    public static final double[] TIER_BONUSES = {0.02, 0.04, 0.06, 0.08, 0.10, 0.12, 0.14, 0.16, 0.18, 0.20};

    // ════════════════════════════════════════════════════════════
    // DATA STORAGE
    // ════════════════════════════════════════════════════════════

    // Player UUID -> Branch -> Tier (1-10, 0 = unlocked)
    private static final Map<String, Map<String, Integer>> playerTiers = new ConcurrentHashMap<>();
    
    // Player UUID -> Total Skill Points
    private static final Map<String, Long> playerSkillPoints = new ConcurrentHashMap<>();
    
    // Player UUID -> Lifetime Skill Points Earned
    private static final Map<String, Long> playerLifetimePoints = new ConcurrentHashMap<>();

    // ════════════════════════════════════════════════════════════
    // DATA PERSISTENCE
    // ════════════════════════════════════════════════════════════

    private static Path dataPath;
    private static final String DATA_FILE = "progression_tree.dat";

    public static void initialize(Path savePath) {
        dataPath = savePath;
        loadData();
    }

    private static void loadData() {
        if (dataPath == null) return;
        
        File file = dataPath.resolve(DATA_FILE).toFile();
        if (!file.exists()) return;

        try (DataInputStream dis = new DataInputStream(new FileInputStream(file))) {
            int playerCount = dis.readInt();
            for (int i = 0; i < playerCount; i++) {
                String uuid = dis.readUTF();
                long skillPoints = dis.readLong();
                long lifetimePoints = dis.readLong();
                
                playerSkillPoints.put(uuid, skillPoints);
                playerLifetimePoints.put(uuid, lifetimePoints);
                
                Map<String, Integer> tiers = new HashMap<>();
                int branchCount = dis.readInt();
                for (int j = 0; j < branchCount; j++) {
                    String branchName = dis.readUTF();
                    int tier = dis.readInt();
                    tiers.put(branchName, tier);
                }
                playerTiers.put(uuid, tiers);
            }
        } catch (IOException e) {
            LOGGER.error("ProgressionTreeManager.loadData failed", e);
        }
    }

    public static void saveData() {
        if (dataPath == null) return;
        
        File file = dataPath.resolve(DATA_FILE).toFile();
        file.getParentFile().mkdirs();

        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(file))) {
            dos.writeInt(playerTiers.size());
            
            for (Map.Entry<String, Map<String, Integer>> entry : playerTiers.entrySet()) {
                String uuid = entry.getKey();
                dos.writeUTF(uuid);
                dos.writeLong(playerSkillPoints.getOrDefault(uuid, 0L));
                dos.writeLong(playerLifetimePoints.getOrDefault(uuid, 0L));
                
                Map<String, Integer> tiers = entry.getValue();
                dos.writeInt(tiers.size());
                for (Map.Entry<String, Integer> tierEntry : tiers.entrySet()) {
                    dos.writeUTF(tierEntry.getKey());
                    dos.writeInt(tierEntry.getValue());
                }
            }
        } catch (IOException e) {
            LOGGER.error("ProgressionTreeManager.saveData failed", e);
        }
    }

    // ════════════════════════════════════════════════════════════
    // SKILL POINT EARNING
    // ════════════════════════════════════════════════════════════

    /**
     * Award skill points to a player for various activities
     */
    public static void awardSkillPoints(ServerPlayer player, Branch branch, int amount) {
        String uuid = player.getUUID().toString();
        
        long current = playerSkillPoints.getOrDefault(uuid, 0L);
        long lifetime = playerLifetimePoints.getOrDefault(uuid, 0L);
        
        playerSkillPoints.put(uuid, current + amount);
        playerLifetimePoints.put(uuid, lifetime + amount);
        saveData();

        player.sendSystemMessage(Component.literal(branch.icon + " +" + amount + " skill points")
            .withStyle(branch.color), true);

        advanceBranchTiers(player, branch);
    }

    /**
     * Award skill points for generic activities (auto-detect branch)
     */
    public static void awardSkillPoints(ServerPlayer player, int amount, String activity) {
        String uuid = player.getUUID().toString();

        long current = playerSkillPoints.getOrDefault(uuid, 0L);
        long lifetime = playerLifetimePoints.getOrDefault(uuid, 0L);

        playerSkillPoints.put(uuid, current + amount);
        playerLifetimePoints.put(uuid, lifetime + amount);
        saveData();

        player.sendSystemMessage(Component.literal("✦ +" + amount + " skill points (" + activity + ")")
            .withStyle(ChatFormatting.AQUA), true);

        Branch tagged = branchForActivityLabel(activity);
        if (tagged != null) {
            advanceBranchTiers(player, tagged);
        } else {
            for (Branch b : Branch.values()) {
                advanceBranchTiers(player, b);
            }
        }
    }

    /**
     * Map free-form activity labels (from achievements, hunts, bosses, admin) to a progression branch.
     * {@code null} means lifetime points apply to every branch's tier unlock check (e.g. admin grant).
     */
    private static Branch branchForActivityLabel(String activity) {
        if (activity == null) return null;
        if ("Combat".equals(activity) || "PvP".equals(activity)) return Branch.COMBAT;
        if ("Mining".equals(activity)) return Branch.MINING;
        if ("Farming".equals(activity)) return Branch.FARMING;
        if (activity.startsWith("World Boss:") || activity.startsWith("Hunt:")) return Branch.COMBAT;
        return null;
    }

    // ════════════════════════════════════════════════════════════
    // TIER MANAGEMENT
    // ════════════════════════════════════════════════════════════

    public static int getTier(String uuid, Branch branch) {
        Map<String, Integer> tiers = playerTiers.get(uuid);
        if (tiers == null) return 0;
        return tiers.getOrDefault(branch.name(), 0);
    }

    public static long getSkillPoints(String uuid) {
        return playerSkillPoints.getOrDefault(uuid, 0L);
    }

    public static long getLifetimePoints(String uuid) {
        return playerLifetimePoints.getOrDefault(uuid, 0L);
    }

    public static int getTotalTiers(String uuid) {
        Map<String, Integer> tiers = playerTiers.get(uuid);
        if (tiers == null) return 0;
        return tiers.values().stream().mapToInt(Integer::intValue).sum();
    }

    private static void putTier(String uuid, Branch branch, int tier) {
        playerTiers.computeIfAbsent(uuid, k -> new HashMap<>()).put(branch.name(), tier);
    }

    /**
     * Unlock every tier for this branch that lifetime skill points can afford (may advance multiple at once).
     */
    private static void advanceBranchTiers(ServerPlayer player, Branch branch) {
        String uuid = player.getUUID().toString();
        long lifetime = getLifetimePoints(uuid);
        boolean changed = false;

        while (getTier(uuid, branch) < MAX_TIERS) {
            int nextTier = getTier(uuid, branch) + 1;
            int requiredPoints = TIER_COSTS[nextTier - 1];
            if (lifetime < requiredPoints) break;

            putTier(uuid, branch, nextTier);
            changed = true;
            player.sendSystemMessage(Component.literal("")
                .append(Component.literal("★ ").withStyle(ChatFormatting.GOLD))
                .append(Component.literal(branch.name + " Tier " + nextTier + " Unlocked!")
                    .withStyle(branch.color, ChatFormatting.BOLD)), false);
        }

        if (changed) {
            saveData();
        }
    }

    /**
     * Manually unlock a tier (for admin or special rewards)
     */
    public static boolean unlockTier(ServerPlayer player, Branch branch) {
        String uuid = player.getUUID().toString();
        int currentTier = getTier(uuid, branch);
        
        if (currentTier >= MAX_TIERS) {
            player.sendSystemMessage(Component.literal("§cAlready at max tier for " + branch.name + "!"), false);
            return false;
        }
        
        setTier(uuid, branch, currentTier + 1);
        player.sendSystemMessage(Component.literal("§a★ " + branch.name + " Tier " + (currentTier + 1) + " unlocked!"), false);
        return true;
    }

    private static void setTier(String uuid, Branch branch, int tier) {
        putTier(uuid, branch, tier);
        saveData();
    }

    // ════════════════════════════════════════════════════════════
    // BONUS CALCULATIONS
    // ════════════════════════════════════════════════════════════

    /**
     * Get the total bonus percentage for a branch
     */
    public static double getBonus(String uuid, Branch branch) {
        int tier = getTier(uuid, branch);
        if (tier <= 0) return 0;
        return TIER_BONUSES[tier - 1];
    }

    /**
     * Get damage bonus for combat branch
     */
    public static double getCombatDamageBonus(String uuid) {
        return getBonus(uuid, Branch.COMBAT);
    }

    /**
     * Get mining speed bonus
     */
    public static double getMiningSpeedBonus(String uuid) {
        return getBonus(uuid, Branch.MINING);
    }

    /**
     * Get farming yield bonus
     */
    public static double getFarmingYieldBonus(String uuid) {
        return getBonus(uuid, Branch.FARMING);
    }

    /**
     * Get fishing luck bonus
     */
    public static double getFishingLuckBonus(String uuid) {
        return getBonus(uuid, Branch.FISHING);
    }

    /**
     * Get exploration speed bonus
     */
    public static double getExplorationSpeedBonus(String uuid) {
        return getBonus(uuid, Branch.EXPLORATION);
    }

    /**
     * Get crafting efficiency bonus
     */
    public static double getCraftingEfficiencyBonus(String uuid) {
        return getBonus(uuid, Branch.CRAFTING);
    }

    // ════════════════════════════════════════════════════════════
    // STAT DISPLAY
    // ════════════════════════════════════════════════════════════

    public static void displayProgress(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        
        player.sendSystemMessage(Component.literal("═══════════════════════════════════")
            .withStyle(ChatFormatting.GOLD), false);
        player.sendSystemMessage(Component.literal("       PROGRESSION TREE")
            .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
        player.sendSystemMessage(Component.literal("═══════════════════════════════════")
            .withStyle(ChatFormatting.GOLD), false);
        
        player.sendSystemMessage(Component.literal("Skill Points: ")
            .withStyle(ChatFormatting.YELLOW)
            .append(Component.literal(String.format("%,d", getSkillPoints(uuid)))
                .withStyle(ChatFormatting.GREEN)), false);
        player.sendSystemMessage(Component.literal("Lifetime Earned: ")
            .withStyle(ChatFormatting.YELLOW)
            .append(Component.literal(String.format("%,d", getLifetimePoints(uuid)))
                .withStyle(ChatFormatting.GRAY)), false);
        player.sendSystemMessage(Component.literal(""), false);

        for (Branch branch : Branch.values()) {
            int tier = getTier(uuid, branch);
            double bonus = getBonus(uuid, branch);
            
            Component tierText = tier > 0 
                ? Component.literal("Tier " + tier).withStyle(branch.color)
                : Component.literal("Locked").withStyle(ChatFormatting.DARK_GRAY);
            
            Component bonusText = tier > 0
                ? Component.literal(" (+" + (int)(bonus * 100) + "% bonus)").withStyle(ChatFormatting.GREEN)
                : Component.literal("").withStyle(ChatFormatting.DARK_GRAY);
            
            player.sendSystemMessage(Component.literal(branch.icon + " " + branch.name + ": ")
                .withStyle(ChatFormatting.WHITE)
                .append(tierText)
                .append(bonusText), false);
        }
        
        player.sendSystemMessage(Component.literal("═══════════════════════════════════")
            .withStyle(ChatFormatting.GOLD), false);
        player.sendSystemMessage(Component.literal("Use /tree to open the GUI")
            .withStyle(ChatFormatting.GRAY), false);
    }

    // ════════════════════════════════════════════════════════════
    // INITIALIZATION
    // ════════════════════════════════════════════════════════════

    public static void registerPlayer(String uuid, String name) {
        if (!playerTiers.containsKey(uuid)) {
            playerTiers.put(uuid, new HashMap<>());
            playerSkillPoints.put(uuid, 0L);
            playerLifetimePoints.put(uuid, 0L);
        }
    }
}
