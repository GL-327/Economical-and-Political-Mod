package com.political;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Hunts System - Collection Quests
 * 5 tiers of hunts with increasing difficulty and rewards
 * Players collect specific items to complete hunts
 */
public class HuntsManager {

    // ════════════════════════════════════════════════════════════
    // HUNT TIERS
    // ════════════════════════════════════════════════════════════

    public enum HuntTier {
        NOVICE("Novice", "⭐", ChatFormatting.GRAY, 5, 35_000),
        APPRENTICE("Apprentice", "⭐⭐", ChatFormatting.GREEN, 10, 150_000),
        JOURNEYMAN("Journeyman", "⭐⭐⭐", ChatFormatting.BLUE, 15, 2_000_000),
        EXPERT("Expert", "⭐⭐⭐⭐", ChatFormatting.LIGHT_PURPLE, 20, 10_000_000),
        MASTER("Master", "⭐⭐⭐⭐⭐", ChatFormatting.GOLD, 25, 50_000_000);

        public final String name;
        public final String stars;
        public final ChatFormatting color;
        public final int itemAmount;
        public final long coinReward;

        HuntTier(String name, String stars, ChatFormatting color, int itemAmount, long coinReward) {
            this.name = name;
            this.stars = stars;
            this.color = color;
            this.itemAmount = itemAmount;
            this.coinReward = coinReward;
        }

        public static HuntTier fromLevel(int level) {
            return switch (level) {
                case 0 -> NOVICE;
                case 1 -> APPRENTICE;
                case 2 -> JOURNEYMAN;
                case 3 -> EXPERT;
                case 4 -> MASTER;
                default -> MASTER;
            };
        }
    }

    // ════════════════════════════════════════════════════════════
    // HUNT DEFINITIONS
    // ════════════════════════════════════════════════════════════

    public static class Hunt {
        public final String id;
        public final String name;
        public final ItemStack targetItem;
        public final HuntTier tier;
        public final String[] lore;
        public final int bonusSkillPoints;

        public Hunt(String id, String name, ItemStack targetItem, HuntTier tier, int bonusSkillPoints, String... lore) {
            this.id = id;
            this.name = name;
            this.targetItem = targetItem;
            this.tier = tier;
            this.bonusSkillPoints = bonusSkillPoints;
            this.lore = lore;
        }
    }

    // All available hunts
    private static final List<Hunt> ALL_HUNTS = List.of(
        // NOVICE Hunts
        new Hunt("novice_cobblestone", "Cobble Collector", 
            new ItemStack(Items.COBBLESTONE), HuntTier.NOVICE, 10,
            "Mine stone and collect cobblestone"),
        new Hunt("novice_oak", "Woodcutter", 
            new ItemStack(Items.OAK_LOG), HuntTier.NOVICE, 10,
            "Chop down oak trees"),
        new Hunt("novice_wheat", "Farmer's Harvest", 
            new ItemStack(Items.WHEAT), HuntTier.NOVICE, 15,
            "Grow and harvest wheat"),
        new Hunt("novice_coal", "Coal Miner", 
            new ItemStack(Items.COAL), HuntTier.NOVICE, 20,
            "Mine coal ore"),
        new Hunt("novice_leather", "Leather Gatherer", 
            new ItemStack(Items.LEATHER), HuntTier.NOVICE, 15,
            "Hunt cows for leather"),

        // APPRENTICE Hunts
        new Hunt("apprentice_iron", "Iron Prospector", 
            new ItemStack(Items.IRON_INGOT), HuntTier.APPRENTICE, 25,
            "Smelt iron ore into ingots"),
        new Hunt("apprentice_gold", "Gold Rush", 
            new ItemStack(Items.GOLD_INGOT), HuntTier.APPRENTICE, 30,
            "Mine and smelt gold"),
        new Hunt("apprentice_redstone", "Redstone Engineer", 
            new ItemStack(Items.REDSTONE), HuntTier.APPRENTICE, 25,
            "Mine redstone dust"),
        new Hunt("apprentice_lapis", "Lapis Lazuli", 
            new ItemStack(Items.LAPIS_LAZULI), HuntTier.APPRENTICE, 30,
            "Mine lapis lazuli ore"),
        new Hunt("apprentice_cactus", "Desert Harvest", 
            new ItemStack(Items.CACTUS), HuntTier.APPRENTICE, 20,
            "Harvest cacti from the desert"),

        // JOURNEYMAN Hunts
        new Hunt("journeyman_diamond", "Diamond Seeker", 
            new ItemStack(Items.DIAMOND), HuntTier.JOURNEYMAN, 50,
            "Mine rare diamonds"),
        new Hunt("journeyman_emerald", "Emerald Trader", 
            new ItemStack(Items.EMERALD), HuntTier.JOURNEYMAN, 60,
            "Mine precious emeralds"),
        new Hunt("journeyman_quartz", "Nether Excavator", 
            new ItemStack(Items.QUARTZ), HuntTier.JOURNEYMAN, 40,
            "Mine nether quartz"),
        new Hunt("journeyman_glowstone", "Glowstone Harvester", 
            new ItemStack(Items.GLOWSTONE_DUST), HuntTier.JOURNEYMAN, 35,
            "Collect glowstone from the nether"),
        new Hunt("journeyman_obsidian", "Obsidian Miner", 
            new ItemStack(Items.OBSIDIAN), HuntTier.JOURNEYMAN, 45,
            "Mine obsidian with a diamond pickaxe"),

        // EXPERT Hunts
        new Hunt("expert_netherite", "Netherite Master", 
            new ItemStack(Items.NETHERITE_INGOT), HuntTier.EXPERT, 100,
            "Smelt ancient debris into netherite"),
        new Hunt("expert_ender_pearl", "Ender Hunter", 
            new ItemStack(Items.ENDER_PEARL), HuntTier.EXPERT, 80,
            "Hunt endermen for pearls"),
        new Hunt("expert_blaze_rod", "Blaze Slayer", 
            new ItemStack(Items.BLAZE_ROD), HuntTier.EXPERT, 75,
            "Defeat blazes in the nether"),
        new Hunt("expert_ghast_tear", "Ghast Hunter", 
            new ItemStack(Items.GHAST_TEAR), HuntTier.EXPERT, 70,
            "Defeat ghasts for tears"),
        new Hunt("expert_shulker", "Shulker Collector", 
            new ItemStack(Items.SHULKER_SHELL), HuntTier.EXPERT, 90,
            "Defeat shulkers in the end"),

        // MASTER Hunts
        new Hunt("master_elytra", "Elytra Master", 
            new ItemStack(Items.ELYTRA), HuntTier.MASTER, 200,
            "Obtain elytra from end cities"),
        new Hunt("master_beacon", "Beacon Builder", 
            new ItemStack(Items.BEACON), HuntTier.MASTER, 150,
            "Craft a beacon from nether stars"),
        new Hunt("master_conduit", "Conduit Constructor", 
            new ItemStack(Items.CONDUIT), HuntTier.MASTER, 180,
            "Craft a conduit from heart of the sea"),
        new Hunt("master_enchanted_gold", "Enchanted Apple", 
            new ItemStack(Items.ENCHANTED_GOLDEN_APPLE), HuntTier.MASTER, 120,
            "Find or craft enchanted golden apples"),
        new Hunt("master_nether_star", "Wither Slayer", 
            new ItemStack(Items.NETHER_STAR), HuntTier.MASTER, 250,
            "Defeat the wither for nether stars")
    );

    // ════════════════════════════════════════════════════════════
    // DATA STORAGE
    // ════════════════════════════════════════════════════════════

    // Player UUID -> Active Hunt ID
    private static final Map<String, String> playerActiveHunt = new ConcurrentHashMap<>();
    
    // Player UUID -> Hunt ID -> Progress (items collected)
    private static final Map<String, Map<String, Integer>> playerHuntProgress = new ConcurrentHashMap<>();
    
    // Player UUID -> Completed Hunt IDs
    private static final Map<String, Set<String>> playerCompletedHunts = new ConcurrentHashMap<>();
    
    // Player UUID -> Hunt Tier Level (0-4)
    private static final Map<String, Integer> playerHuntTier = new ConcurrentHashMap<>();
    
    // Player UUID -> Total Hunts Completed
    private static final Map<String, Integer> playerTotalHunts = new ConcurrentHashMap<>();

    // ════════════════════════════════════════════════════════════
    // DATA PERSISTENCE
    // ════════════════════════════════════════════════════════════

    private static Path dataPath;
    private static final String DATA_FILE = "hunts.dat";

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
                
                playerActiveHunt.put(uuid, dis.readBoolean() ? dis.readUTF() : null);
                playerHuntTier.put(uuid, dis.readInt());
                playerTotalHunts.put(uuid, dis.readInt());
                
                // Load completed hunts
                int completedCount = dis.readInt();
                Set<String> completed = new HashSet<>();
                for (int j = 0; j < completedCount; j++) {
                    completed.add(dis.readUTF());
                }
                playerCompletedHunts.put(uuid, completed);
                
                // Load progress
                int progressCount = dis.readInt();
                Map<String, Integer> progress = new HashMap<>();
                for (int j = 0; j < progressCount; j++) {
                    progress.put(dis.readUTF(), dis.readInt());
                }
                playerHuntProgress.put(uuid, progress);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveData() {
        if (dataPath == null) return;
        
        File file = dataPath.resolve(DATA_FILE).toFile();
        file.getParentFile().mkdirs();

        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(file))) {
            dos.writeInt(playerActiveHunt.size());
            
            for (String uuid : playerActiveHunt.keySet()) {
                dos.writeUTF(uuid);
                
                String activeHunt = playerActiveHunt.get(uuid);
                dos.writeBoolean(activeHunt != null);
                if (activeHunt != null) {
                    dos.writeUTF(activeHunt);
                }
                
                dos.writeInt(playerHuntTier.getOrDefault(uuid, 0));
                dos.writeInt(playerTotalHunts.getOrDefault(uuid, 0));
                
                Set<String> completed = playerCompletedHunts.getOrDefault(uuid, Set.of());
                dos.writeInt(completed.size());
                for (String huntId : completed) {
                    dos.writeUTF(huntId);
                }
                
                Map<String, Integer> progress = playerHuntProgress.getOrDefault(uuid, Map.of());
                dos.writeInt(progress.size());
                for (Map.Entry<String, Integer> entry : progress.entrySet()) {
                    dos.writeUTF(entry.getKey());
                    dos.writeInt(entry.getValue());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ════════════════════════════════════════════════════════════
    // HUNT MANAGEMENT
    // ════════════════════════════════════════════════════════════

    public static Hunt getHunt(String huntId) {
        for (Hunt hunt : ALL_HUNTS) {
            if (hunt.id.equals(huntId)) return hunt;
        }
        return null;
    }

    public static List<Hunt> getHuntsForTier(HuntTier tier) {
        List<Hunt> hunts = new ArrayList<>();
        for (Hunt hunt : ALL_HUNTS) {
            if (hunt.tier == tier) hunts.add(hunt);
        }
        return hunts;
    }

    public static Hunt getActiveHunt(String uuid) {
        String huntId = playerActiveHunt.get(uuid);
        return huntId != null ? getHunt(huntId) : null;
    }

    public static int getProgress(String uuid, String huntId) {
        Map<String, Integer> progress = playerHuntProgress.get(uuid);
        if (progress == null) return 0;
        return progress.getOrDefault(huntId, 0);
    }

    public static boolean isHuntCompleted(String uuid, String huntId) {
        Set<String> completed = playerCompletedHunts.get(uuid);
        return completed != null && completed.contains(huntId);
    }

    public static HuntTier getPlayerTier(String uuid) {
        int level = playerHuntTier.getOrDefault(uuid, 0);
        return HuntTier.fromLevel(level);
    }

    public static int getTotalHuntsCompleted(String uuid) {
        return playerTotalHunts.getOrDefault(uuid, 0);
    }

    // ════════════════════════════════════════════════════════════
    // HUNT ACTIONS
    // ════════════════════════════════════════════════════════════

    /**
     * Start a new hunt for a player
     */
    public static boolean startHunt(ServerPlayer player, String huntId) {
        String uuid = player.getUUID().toString();
        Hunt hunt = getHunt(huntId);
        
        if (hunt == null) {
            player.sendSystemMessage(Component.literal("§cInvalid hunt!"), false);
            return false;
        }
        
        // Check if already completed
        if (isHuntCompleted(uuid, huntId)) {
            player.sendSystemMessage(Component.literal("§cYou've already completed this hunt!"), false);
            return false;
        }
        
        // Check tier requirement
        HuntTier playerTier = getPlayerTier(uuid);
        if (hunt.tier.ordinal() > playerTier.ordinal()) {
            player.sendSystemMessage(Component.literal("§cYou need to reach " + hunt.tier.name + " tier first!"), false);
            return false;
        }
        
        // Set active hunt
        playerActiveHunt.put(uuid, huntId);
        playerHuntProgress.computeIfAbsent(uuid, k -> new HashMap<>()).put(huntId, 0);
        
        player.sendSystemMessage(Component.literal("§a★ Hunt started: " + hunt.name));
        player.sendSystemMessage(Component.literal("§7Collect " + hunt.tier.itemAmount + "x " + 
            hunt.targetItem.getHoverName().getString()));
        
        saveData();
        return true;
    }

    /**
     * Track item collection progress
     */
    public static void trackItemCollection(ServerPlayer player, ItemStack collected) {
        String uuid = player.getUUID().toString();
        Hunt activeHunt = getActiveHunt(uuid);
        
        if (activeHunt == null) return;
        
        // Check if item matches hunt target
        if (!collected.getItem().equals(activeHunt.targetItem.getItem())) return;
        
        // Update progress
        Map<String, Integer> progress = playerHuntProgress.get(uuid);
        if (progress == null) return;
        
        int current = progress.getOrDefault(activeHunt.id, 0);
        int newAmount = current + collected.getCount();
        progress.put(activeHunt.id, newAmount);
        
        // Check completion
        if (newAmount >= activeHunt.tier.itemAmount) {
            completeHunt(player, activeHunt);
        } else {
            // Show progress
            player.sendSystemMessage(Component.literal("§e" + activeHunt.name + ": §f" + 
                newAmount + "/" + activeHunt.tier.itemAmount), true);
        }
        
        saveData();
    }

    /**
     * Complete a hunt and give rewards
     */
    private static void completeHunt(ServerPlayer player, Hunt hunt) {
        String uuid = player.getUUID().toString();
        
        // Mark as completed
        playerCompletedHunts.computeIfAbsent(uuid, k -> new HashSet<>()).add(hunt.id);
        playerActiveHunt.remove(uuid);
        
        // Update stats
        int total = playerTotalHunts.getOrDefault(uuid, 0) + 1;
        playerTotalHunts.put(uuid, total);
        
        // Check tier upgrade
        checkTierUpgrade(player);
        
        // Give rewards
        CoinManager.giveCoins(player, hunt.tier.coinReward);
        ProgressionTreeManager.awardSkillPoints(player, hunt.bonusSkillPoints, "Hunt: " + hunt.name);
        
        // Announce
        player.sendSystemMessage(Component.literal("═══════════════════════════════════")
            .withStyle(ChatFormatting.GOLD), false);
        player.sendSystemMessage(Component.literal("    ★ HUNT COMPLETED! ★")
            .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
        player.sendSystemMessage(Component.literal("═══════════════════════════════════")
            .withStyle(ChatFormatting.GOLD), false);
        player.sendSystemMessage(Component.literal("§a" + hunt.name), false);
        player.sendSystemMessage(Component.literal("§6+" + String.format("%,d", hunt.tier.coinReward) + " coins"), false);
        player.sendSystemMessage(Component.literal("§b+" + hunt.bonusSkillPoints + " skill points"), false);
        player.sendSystemMessage(Component.literal("§7Total hunts completed: " + total), false);
        
        saveData();
    }

    /**
     * Check if player should upgrade tier
     */
    private static void checkTierUpgrade(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        int currentTier = playerHuntTier.getOrDefault(uuid, 0);
        
        if (currentTier >= 4) return; // Max tier
        
        int totalHunts = getTotalHuntsCompleted(uuid);
        int requiredHunts = switch (currentTier) {
            case 0 -> 5;  // Novice -> Apprentice
            case 1 -> 15; // Apprentice -> Journeyman
            case 2 -> 30; // Journeyman -> Expert
            case 3 -> 50; // Expert -> Master
            default -> Integer.MAX_VALUE;
        };
        
        if (totalHunts >= requiredHunts) {
            playerHuntTier.put(uuid, currentTier + 1);
            HuntTier newTier = HuntTier.fromLevel(currentTier + 1);
            player.sendSystemMessage(Component.literal("★ ").withStyle(ChatFormatting.GOLD)
                .append(Component.literal("Hunt Tier Upgraded to " + newTier.name + "!")
                    .withStyle(newTier.color, ChatFormatting.BOLD)), false);
        }
    }

    /**
     * Abandon current hunt
     */
    public static void abandonHunt(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        Hunt activeHunt = getActiveHunt(uuid);
        
        if (activeHunt == null) {
            player.sendSystemMessage(Component.literal("§cYou don't have an active hunt!"), false);
            return;
        }
        
        playerActiveHunt.remove(uuid);
        player.sendSystemMessage(Component.literal("§eAbandoned hunt: " + activeHunt.name), false);
        saveData();
    }

    /**
     * Claim hunt items from inventory (manual check)
     */
    public static void checkInventoryForHunt(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        Hunt activeHunt = getActiveHunt(uuid);
        
        if (activeHunt == null) {
            player.sendSystemMessage(Component.literal("§cNo active hunt! Use /hunt to start one."), false);
            return;
        }
        
        // Count items in inventory
        int count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem().equals(activeHunt.targetItem.getItem())) {
                count += stack.getCount();
            }
        }
        
        // Update progress
        Map<String, Integer> progress = playerHuntProgress.get(uuid);
        if (progress != null) {
            progress.put(activeHunt.id, count);
        }
        
        if (count >= activeHunt.tier.itemAmount) {
            // Remove items
            int toRemove = activeHunt.tier.itemAmount;
            for (int i = 0; i < player.getInventory().getContainerSize() && toRemove > 0; i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (stack.getItem().equals(activeHunt.targetItem.getItem())) {
                    int removeAmount = Math.min(stack.getCount(), toRemove);
                    stack.shrink(removeAmount);
                    toRemove -= removeAmount;
                }
            }
            completeHunt(player, activeHunt);
        } else {
            player.sendSystemMessage(Component.literal("§e" + activeHunt.name + ": §f" + 
                count + "/" + activeHunt.tier.itemAmount + " items"), false);
        }
        
        saveData();
    }

    // ════════════════════════════════════════════════════════════
    // INITIALIZATION
    // ════════════════════════════════════════════════════════════

    public static void registerPlayer(String uuid) {
        if (!playerHuntTier.containsKey(uuid)) {
            playerHuntTier.put(uuid, 0);
            playerTotalHunts.put(uuid, 0);
            playerCompletedHunts.put(uuid, new HashSet<>());
            playerHuntProgress.put(uuid, new HashMap<>());
        }
    }
}
