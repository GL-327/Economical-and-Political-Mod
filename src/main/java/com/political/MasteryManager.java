package com.political;


import com.political.bounty.SlayerItems;
import com.political.bounty.SlayerManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mastery Tracks System - Pillar 1
 * 
 * Every custom item family has a mastery level that persists per player.
 * Players earn Mastery XP by actively using items in that family.
 * Max level is 10 per family. Total grind to max one family should feel like weeks of active play.
 */
public class MasteryManager {

    // ════════════════════════════════════════════════════════════
    // ITEM FAMILIES
    // ════════════════════════════════════════════════════════════
    
    public enum MasteryFamily {
        MELEE("Melee", "⚔", ChatFormatting.RED),
        RANGED("Ranged", "🏹", ChatFormatting.GREEN),
        MAGIC("Magic", "✨", ChatFormatting.LIGHT_PURPLE),
        GATHERING("Gathering", "⛏", ChatFormatting.AQUA),
        CRAFTING("Crafting", "🔨", ChatFormatting.YELLOW),
        ARMOR("Armor", "🛡", ChatFormatting.BLUE);

        public final String displayName;
        public final String icon;
        public final ChatFormatting color;

        MasteryFamily(String displayName, String icon, ChatFormatting color) {
            this.displayName = displayName;
            this.icon = icon;
            this.color = color;
        }
    }

    // ════════════════════════════════════════════════════════════
    // XP REQUIREMENTS PER LEVEL
    // ════════════════════════════════════════════════════════════
    
    private static final int[] XP_PER_LEVEL = {
        0,      // Level 1 (starting)
        900,    // Level 2
        2200,   // Level 3
        4800,   // Level 4
        9500,   // Level 5
        17000,  // Level 6
        28000,  // Level 7
        43000,  // Level 8
        65000,  // Level 9
        95000   // Level 10 (max)
    };

    // Total XP needed to reach each level (cumulative)
    private static final int[] CUMULATIVE_XP = new int[11];
    static {
        int total = 0;
        for (int i = 1; i <= 10; i++) {
            total += XP_PER_LEVEL[i - 1];
            CUMULATIVE_XP[i] = total;
        }
    }
    
    /** Get cumulative XP needed to reach a level */
    public static int getCumulativeXpForLevel(int level) {
        if (level < 1) return 0;
        if (level > 10) return CUMULATIVE_XP[10];
        return CUMULATIVE_XP[level];
    }
    
    /** Get XP needed to go from level-1 to level */
    public static int getXpForLevel(int level) {
        if (level < 1 || level > 10) return 0;
        return XP_PER_LEVEL[level - 1];
    }

    // ════════════════════════════════════════════════════════════
    // PLAYER MASTERY DATA
    // ════════════════════════════════════════════════════════════
    
    // UUID -> Family -> MasteryData
    private static final Map<String, Map<MasteryFamily, MasteryData>> playerMastery = new ConcurrentHashMap<>();

    /** Throttle armor XP from repeated damage ticks (game time). */
    private static final ConcurrentHashMap<String, Long> lastArmorXpGameTime = new ConcurrentHashMap<>();

    /** Heart of Mastery unlocks (coin-purchased tree). */
    private static final Map<String, Set<String>> heartUnlocks = new ConcurrentHashMap<>();
    
    // File path for persistence
    private static Path dataFile;
    
    public static class MasteryData {
        public int level = 1;
        public int xp = 0;
        public Set<String> unlockedPerks = new HashSet<>();
        public MasteryFamily selectedFamily = null;
        public long lastFamilyChangeTime = 0;
        
        public MasteryData() {}
        
        public MasteryData(int level, int xp) {
            this.level = level;
            this.xp = xp;
        }
        
        public int getXpToNextLevel() {
            if (level >= 10) return 0;
            return XP_PER_LEVEL[level];
        }
        
        
        public int getXpProgressInCurrentLevel() {
            if (level >= 10) return 0;
            return xp - CUMULATIVE_XP[level];
        }
        
        public double getProgressPercent() {
            if (level >= 10) return 100.0;
            int needed = XP_PER_LEVEL[level];
            int current = getXpProgressInCurrentLevel();
            return (current * 100.0) / needed;
        }
    }

    // ════════════════════════════════════════════════════════════
    // INITIALIZATION & PERSISTENCE
    // ════════════════════════════════════════════════════════════
    
    public static void initialize(Path dataDir) {
        dataFile = dataDir.resolve("mastery_data.json");
        loadData();
    }
    
    private static void importMasteryMap(Map<String, Map<String, MasteryData>> loaded) {
        if (loaded == null) return;
        playerMastery.clear();
        for (Map.Entry<String, Map<String, MasteryData>> entry : loaded.entrySet()) {
            Map<MasteryFamily, MasteryData> familyMap = new EnumMap<>(MasteryFamily.class);
            for (Map.Entry<String, MasteryData> familyEntry : entry.getValue().entrySet()) {
                try {
                    MasteryFamily family = MasteryFamily.valueOf(familyEntry.getKey().toUpperCase());
                    familyMap.put(family, familyEntry.getValue());
                } catch (IllegalArgumentException ignored) {}
            }
            playerMastery.put(entry.getKey(), familyMap);
        }
    }

    private static void loadData() {
        if (dataFile == null || !Files.exists(dataFile)) return;

        try {
            String content = Files.readString(dataFile);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Type masteryType = new TypeToken<Map<String, Map<String, MasteryData>>>(){}.getType();
            JsonElement root = JsonParser.parseString(content);
            if (!root.isJsonObject()) return;
            JsonObject obj = root.getAsJsonObject();

            if (obj.has("mastery") && obj.get("mastery").isJsonObject()) {
                Map<String, Map<String, MasteryData>> masteryMap = gson.fromJson(obj.get("mastery"), masteryType);
                importMasteryMap(masteryMap);
                heartUnlocks.clear();
                if (obj.has("hearts") && obj.get("hearts").isJsonObject()) {
                    Type heartType = new TypeToken<Map<String, List<String>>>(){}.getType();
                    Map<String, List<String>> hearts = gson.fromJson(obj.get("hearts"), heartType);
                    if (hearts != null) {
                        for (Map.Entry<String, List<String>> e : hearts.entrySet()) {
                            Set<String> set = ConcurrentHashMap.newKeySet();
                            set.addAll(e.getValue());
                            heartUnlocks.put(e.getKey(), set);
                        }
                    }
                }
            } else {
                Map<String, Map<String, MasteryData>> loaded = gson.fromJson(content, masteryType);
                importMasteryMap(loaded);
                heartUnlocks.clear();
            }
        } catch (Exception e) {
            System.err.println("[MasteryManager] Failed to load data: " + e.getMessage());
        }
    }
    
    public static void saveData() {
        if (dataFile == null) return;
        
        try {
            Files.createDirectories(dataFile.getParent());

            Map<String, Map<String, MasteryData>> toSave = new HashMap<>();
            for (Map.Entry<String, Map<MasteryFamily, MasteryData>> entry : playerMastery.entrySet()) {
                Map<String, MasteryData> familyMap = new HashMap<>();
                for (Map.Entry<MasteryFamily, MasteryData> familyEntry : entry.getValue().entrySet()) {
                    familyMap.put(familyEntry.getKey().name(), familyEntry.getValue());
                }
                toSave.put(entry.getKey(), familyMap);
            }

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonObject out = new JsonObject();
            out.add("mastery", gson.toJsonTree(toSave));
            JsonObject heartsJson = new JsonObject();
            for (Map.Entry<String, Set<String>> e : heartUnlocks.entrySet()) {
                heartsJson.add(e.getKey(), gson.toJsonTree(new ArrayList<>(e.getValue())));
            }
            out.add("hearts", heartsJson);
            try (Writer writer = Files.newBufferedWriter(dataFile)) {
                gson.toJson(out, writer);
            }
        } catch (Exception e) {
            System.err.println("[MasteryManager] Failed to save data: " + e.getMessage());
        }
    }

    public static Set<String> getHeartUnlocks(String uuid) {
        Set<String> s = heartUnlocks.get(uuid);
        if (s == null || s.isEmpty()) return Set.of();
        return Set.copyOf(s);
    }

    public static void addHeartUnlock(String uuid, String nodeId) {
        heartUnlocks.computeIfAbsent(uuid, k -> ConcurrentHashMap.newKeySet()).add(nodeId);
        saveData();
    }

    // ════════════════════════════════════════════════════════════
    // MASTERY DATA ACCESS
    // ════════════════════════════════════════════════════════════
    
    public static MasteryData getMasteryData(String uuid, MasteryFamily family) {
        return playerMastery
                .computeIfAbsent(uuid, k -> new EnumMap<>(MasteryFamily.class))
                .computeIfAbsent(family, k -> new MasteryData(1, 0));
    }
    
    public static MasteryData getMasteryData(ServerPlayer player, MasteryFamily family) {
        return getMasteryData(player.getUUID().toString(), family);
    }
    
    public static int getMasteryLevel(String uuid, MasteryFamily family) {
        return getMasteryData(uuid, family).level;
    }
    
    public static int getMasteryLevel(ServerPlayer player, MasteryFamily family) {
        return getMasteryLevel(player.getUUID().toString(), family);
    }
    
    public static Map<MasteryFamily, MasteryData> getAllMastery(String uuid) {
        return playerMastery.getOrDefault(uuid, new EnumMap<>(MasteryFamily.class));
    }

    public static int getTotalLevel(String uuid) {
        int total = 0;
        for (MasteryFamily f : MasteryFamily.values()) {
            total += getMasteryLevel(uuid, f);
        }
        return total;
    }

    public static int getGrandmasterCount(String uuid) {
        int count = 0;
        for (MasteryFamily f : MasteryFamily.values()) {
            if (getMasteryLevel(uuid, f) >= 10) count++;
        }
        return count;
    }
    
    public static Map<MasteryFamily, MasteryData> getAllMastery(ServerPlayer player) {
        return getAllMastery(player.getUUID().toString());
    }
    
    // ════════════════════════════════════════════════════════════
    // FAMILY SELECTION
    // ════════════════════════════════════════════════════════════
    
    public static boolean canChangeFamily(ServerPlayer player) {
        return true;
    }

    /** Mastery XP track follows {@link ClassManager.PlayerClass}; no separate focus. */
    public static MasteryFamily getMasteryFamilyForClass(ClassManager.PlayerClass c) {
        if (c == null) return null;
        return switch (c) {
            case WARRIOR, BERSERKER, ROGUE -> MasteryFamily.MELEE;
            case MAGE, NECROMANCER -> MasteryFamily.MAGIC;
            case RANGER -> MasteryFamily.RANGED;
            case PALADIN, CLERIC -> MasteryFamily.ARMOR;
            case DRUID -> MasteryFamily.GATHERING;
            case BARD -> MasteryFamily.CRAFTING;
        };
    }

    public static void syncMasteryTrackFromClass(ServerPlayer player, ClassManager.PlayerClass c) {
        MasteryFamily f = getMasteryFamilyForClass(c);
        if (f == null) return;
        String uuid = player.getUUID().toString();
        for (MasteryFamily fam : MasteryFamily.values()) {
            getMasteryData(uuid, fam).selectedFamily = f;
        }
    }
    
    public static boolean setSelectedFamily(ServerPlayer player, MasteryFamily family) {
        String uuid = player.getUUID().toString();
        
        for (MasteryFamily f : MasteryFamily.values()) {
            MasteryData data = getMasteryData(uuid, f);
            data.selectedFamily = family;
            data.lastFamilyChangeTime = System.currentTimeMillis();
        }
        
        player.sendSystemMessage(Component.literal("§a✓ Focus set to §e" + family.icon + " " + family.displayName + "§a. Only this family will gain Mastery XP now!"), false);
        return true;
    }
    
    public static MasteryFamily getSelectedFamily(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        MasteryData data = getMasteryData(uuid, MasteryFamily.MELEE);
        return data.selectedFamily;
    }

    // ════════════════════════════════════════════════════════════
    // XP GAIN & LEVELING
    // ════════════════════════════════════════════════════════════
    
    /**
     * Add XP to a mastery family for a player.
     * XP only goes to the player's selected family. If no family is selected,
     * falls back to the family passed in for backwards compatibility.
     * @return true if the player leveled up
     */
    public static boolean addXp(ServerPlayer player, MasteryFamily family, int amount) {
        String uuid = player.getUUID().toString();

        MasteryFamily classTrack = getMasteryFamilyForClass(ClassManager.getPlayerClass(uuid));
        if (classTrack != null) {
            if (classTrack != family) return false;
        } else {
            MasteryFamily selected = getSelectedFamily(player);
            if (selected != null && selected != family) return false;
        }

        MasteryData data = getMasteryData(uuid, family);
        
        if (data.level >= 10) return false;

        amount = Math.max(0, (int) Math.round(amount * MasteryHeartTree.masteryXpGlobalMultiplier(uuid)));
        
        int oldLevel = data.level;
        data.xp += amount;
        
        while (data.level < 10 && data.xp >= CUMULATIVE_XP[data.level + 1]) {
            data.level++;
            onLevelUp(player, family, data.level);
        }

        saveData();
        return data.level > oldLevel;
    }
    
    private static void onLevelUp(ServerPlayer player, MasteryFamily family, int newLevel) {
        // Notify player
        player.sendSystemMessage(Component.literal("§6★ MASTERY LEVEL UP! §e" + family.displayName + " §6is now level §e" + newLevel + "§6!"), false);
        
        // Grant perk for this level
        grantPerk(player, family, newLevel);
        
        // Special effects at milestones
        if (newLevel == 5) {
            player.sendSystemMessage(Component.literal("§b✦ You've unlocked particle effects on hits!"), false);
        } else if (newLevel == 10) {
            player.sendSystemMessage(Component.literal("§d★ §lGRANDMASTER §r§din " + family.displayName + "! You've earned the title and exclusive access!"), false);
        }
    }
    
    private static void grantPerk(ServerPlayer player, MasteryFamily family, int level) {
        String uuid = player.getUUID().toString();
        MasteryData data = getMasteryData(uuid, family);
        String perkId = family.name() + "_L" + level;
        data.unlockedPerks.add(perkId);
        
        // Notify of perk unlock
        String perkDesc = getPerkDescription(family, level);
        if (perkDesc != null) {
            player.sendSystemMessage(Component.literal("§a✓ Unlocked: §f" + perkDesc), false);
        }
    }
    
    public static boolean hasPerk(String uuid, MasteryFamily family, String perkId) {
        MasteryData data = getMasteryData(uuid, family);
        return data.unlockedPerks.contains(perkId);
    }
    
    public static boolean hasPerk(ServerPlayer player, MasteryFamily family, String perkId) {
        return hasPerk(player.getUUID().toString(), family, perkId);
    }

    // ════════════════════════════════════════════════════════════
    // PERK DEFINITIONS
    // ════════════════════════════════════════════════════════════
    
    public static String getPerkDescription(MasteryFamily family, int level) {
        return switch (family) {
            case MELEE -> switch (level) {
                case 2 -> "+3% base damage with all melee items";
                case 3 -> "Rending Strike passive (every 10th hit applies bleed)";
                case 4 -> "+5% crit chance";
                case 5 -> "Unlock Tier 2 melee crafting recipes";
                case 6 -> "Kills have 5% chance to drop bonus coins";
                case 7 -> "Bleed duration doubled";
                case 8 -> "Unlock Tier 3 melee crafting recipes";
                case 9 -> "+10% damage against Boss Bounty targets";
                case 10 -> "Grandmaster title + exclusive cosmetic recipe + Tier 4 Expeditions";
                default -> null;
            };
            case RANGED -> switch (level) {
                case 2 -> "+3% projectile speed";
                case 3 -> "Arrow Retrieval: 10% chance to recover arrows";
                case 4 -> "+5% crit chance";
                case 5 -> "Unlock Tier 2 ranged crafting recipes";
                case 6 -> "Headshot damage +15%";
                case 7 -> "Quick Draw: -10% bow draw time";
                case 8 -> "Unlock Tier 3 ranged crafting recipes";
                case 9 -> "Piercing Shot: 10% chance to hit through armor";
                case 10 -> "Grandmaster title + exclusive cosmetic recipe + Tier 4 Expeditions";
                default -> null;
            };
            case MAGIC -> switch (level) {
                case 2 -> "+5% spell damage";
                case 3 -> "Mana Efficiency: -5% mana cost";
                case 4 -> "+8% spell crit chance";
                case 5 -> "Unlock Tier 2 magic crafting recipes";
                case 6 -> "Spell Cooldown: -5% on all cooldowns";
                case 7 -> "Arcane Shield: 5% damage reduction when casting";
                case 8 -> "Unlock Tier 3 magic crafting recipes";
                case 9 -> "Overload: 10% chance for double spell effect";
                case 10 -> "Grandmaster title + exclusive cosmetic recipe + Tier 4 Expeditions";
                default -> null;
            };
            case GATHERING -> switch (level) {
                case 2 -> "+5% mining speed";
                case 3 -> "10% chance to double drop on custom ores";
                case 4 -> "Vein Finder: Highlights nearby ores";
                case 5 -> "Unlock Vein Nodes (special high-tier blocks)";
                case 6 -> "Fortune Bonus: +1 fortune level effectively";
                case 7 -> "Auto-smelt passive for custom ores";
                case 8 -> "Deep Mining: Access to rare underground zones";
                case 9 -> "Resource Efficiency: 5% less tool durability loss";
                case 10 -> "Unlock Deep Seam expedition zone + Grandmaster title";
                default -> null;
            };
            case CRAFTING -> switch (level) {
                case 2 -> "+5% crafting speed";
                case 3 -> "Material Efficiency: 5% chance to not consume material";
                case 4 -> "Quality Boost: 10% chance for +1 quality tier";
                case 5 -> "Unlock Tier 2 crafting recipes";
                case 6 -> "Mass Production: 5% chance for double output";
                case 7 -> "Masterwork: Can add bonus stats to items";
                case 8 -> "Unlock Tier 3 crafting recipes";
                case 9 -> "Legendary Craft: 2% chance for legendary variant";
                case 10 -> "Grandmaster title + exclusive cosmetic recipe + Tier 4 Expeditions";
                default -> null;
            };
            case ARMOR -> switch (level) {
                case 2 -> "+3% armor effectiveness";
                case 3 -> "Damage Reduction: -2% all damage";
                case 4 -> "Resistance: +5% to status effects";
                case 5 -> "Unlock Tier 2 armor crafting recipes";
                case 6 -> "Thorns: 5% chance to reflect damage";
                case 7 -> "Fortitude: +10% max health from armor";
                case 8 -> "Unlock Tier 3 armor crafting recipes";
                case 9 -> "Last Stand: When below 20% HP, +15% damage reduction";
                case 10 -> "Grandmaster title + exclusive cosmetic recipe + Tier 4 Expeditions";
                default -> null;
            };
        };
    }

    // ════════════════════════════════════════════════════════════
    // ITEM FAMILY DETECTION
    // ════════════════════════════════════════════════════════════
    
    public static MasteryFamily getItemFamily(ItemStack stack) {
        if (stack.isEmpty()) return null;

        // Check for custom items first (order matters: "pickaxe" contains "axe" as substring)
        String customId = SlayerItems.getCustomItemId(stack);
        if (customId != null) {
            if (customId.contains("pickaxe") || customId.contains("shovel") || customId.contains("hoe") || customId.contains("scythe")) {
                return MasteryFamily.GATHERING;
            }
            if (customId.contains("bow") || customId.contains("crossbow") || customId.contains("thrown")) {
                return MasteryFamily.RANGED;
            }
            if (customId.contains("staff") || customId.contains("wand") || customId.contains("tome") || customId.contains("spell")) {
                return MasteryFamily.MAGIC;
            }
            if (customId.contains("helmet") || customId.contains("chestplate") || customId.contains("leggings") || customId.contains("boots")) {
                return MasteryFamily.ARMOR;
            }
            if (customId.contains("sword") || customId.contains("blade") || customId.contains("axe")) {
                return MasteryFamily.MELEE;
            }
        }
        
        // Vanilla item classification using Items constants
        // Swords
        if (stack.is(Items.WOODEN_SWORD) || stack.is(Items.STONE_SWORD) ||
            stack.is(Items.IRON_SWORD) || stack.is(Items.GOLDEN_SWORD) ||
            stack.is(Items.DIAMOND_SWORD) || stack.is(Items.NETHERITE_SWORD)) {
            return MasteryFamily.MELEE;
        }
        
        // Bows/Crossbows/Trident
        if (stack.is(Items.BOW) || stack.is(Items.CROSSBOW) || stack.is(Items.TRIDENT)) {
            return MasteryFamily.RANGED;
        }

        if (stack.is(Items.MACE)) {
            return MasteryFamily.MELEE;
        }
        
        // Pickaxes
        if (stack.is(Items.WOODEN_PICKAXE) || stack.is(Items.STONE_PICKAXE) ||
            stack.is(Items.IRON_PICKAXE) || stack.is(Items.GOLDEN_PICKAXE) ||
            stack.is(Items.DIAMOND_PICKAXE) || stack.is(Items.NETHERITE_PICKAXE)) {
            return MasteryFamily.GATHERING;
        }
        
        // Shovels
        if (stack.is(Items.WOODEN_SHOVEL) || stack.is(Items.STONE_SHOVEL) ||
            stack.is(Items.IRON_SHOVEL) || stack.is(Items.GOLDEN_SHOVEL) ||
            stack.is(Items.DIAMOND_SHOVEL) || stack.is(Items.NETHERITE_SHOVEL)) {
            return MasteryFamily.GATHERING;
        }
        
        // Hoes
        if (stack.is(Items.WOODEN_HOE) || stack.is(Items.STONE_HOE) ||
            stack.is(Items.IRON_HOE) || stack.is(Items.GOLDEN_HOE) ||
            stack.is(Items.DIAMOND_HOE) || stack.is(Items.NETHERITE_HOE)) {
            return MasteryFamily.GATHERING;
        }
        
        // Axes - can be melee or gathering, treat as gathering for mining
        if (stack.is(Items.WOODEN_AXE) || stack.is(Items.STONE_AXE) ||
            stack.is(Items.IRON_AXE) || stack.is(Items.GOLDEN_AXE) ||
            stack.is(Items.DIAMOND_AXE) || stack.is(Items.NETHERITE_AXE)) {
            return MasteryFamily.MELEE; // Axes are primarily melee in this system
        }
        
        // Armor
        if (stack.is(Items.LEATHER_HELMET) || stack.is(Items.LEATHER_CHESTPLATE) ||
            stack.is(Items.LEATHER_LEGGINGS) || stack.is(Items.LEATHER_BOOTS) ||
            stack.is(Items.CHAINMAIL_HELMET) || stack.is(Items.CHAINMAIL_CHESTPLATE) ||
            stack.is(Items.CHAINMAIL_LEGGINGS) || stack.is(Items.CHAINMAIL_BOOTS) ||
            stack.is(Items.IRON_HELMET) || stack.is(Items.IRON_CHESTPLATE) ||
            stack.is(Items.IRON_LEGGINGS) || stack.is(Items.IRON_BOOTS) ||
            stack.is(Items.GOLDEN_HELMET) || stack.is(Items.GOLDEN_CHESTPLATE) ||
            stack.is(Items.GOLDEN_LEGGINGS) || stack.is(Items.GOLDEN_BOOTS) ||
            stack.is(Items.DIAMOND_HELMET) || stack.is(Items.DIAMOND_CHESTPLATE) ||
            stack.is(Items.DIAMOND_LEGGINGS) || stack.is(Items.DIAMOND_BOOTS) ||
            stack.is(Items.NETHERITE_HELMET) || stack.is(Items.NETHERITE_CHESTPLATE) ||
            stack.is(Items.NETHERITE_LEGGINGS) || stack.is(Items.NETHERITE_BOOTS) ||
            stack.is(Items.TURTLE_HELMET)) {
            return MasteryFamily.ARMOR;
        }
        
        return null;
    }

    // ════════════════════════════════════════════════════════════
    // DAMAGE & STAT BONUSES
    // ════════════════════════════════════════════════════════════
    
    /**
     * Get the damage multiplier bonus for a player's mastery in a family.
     */
    public static double getDamageBonus(String uuid, MasteryFamily family) {
        int level = getMasteryLevel(uuid, family);
        return switch (family) {
            case MELEE -> {
                double bonus = 0;
                if (level >= 2) bonus += 0.06;
                if (level >= 4) bonus += 0.08;
                if (level >= 9) bonus += 0.14;
                yield bonus;
            }
            case RANGED -> {
                double bonus = 0;
                if (level >= 4) bonus += 0.08;
                if (level >= 6) bonus += 0.20;
                yield bonus;
            }
            case MAGIC -> {
                double bonus = 0;
                if (level >= 2) bonus += 0.08;
                if (level >= 4) bonus += 0.12;
                yield bonus;
            }
            default -> 0;
        };
    }
    
    /**
     * Get the crit chance bonus for a player's mastery in a family.
     */
    public static double getCritBonus(String uuid, MasteryFamily family) {
        int level = getMasteryLevel(uuid, family);
        return switch (family) {
            case MELEE, RANGED, MAGIC -> level >= 4 ? 0.09 : 0;
            default -> 0;
        };
    }
    
    /**
     * Get armor effectiveness bonus.
     */
    public static double getArmorBonus(String uuid) {
        int level = getMasteryLevel(uuid, MasteryFamily.ARMOR);
        double bonus = 0;
        if (level >= 2) bonus += 0.06;
        if (level >= 3) bonus += 0.04;
        if (level >= 7) bonus += 0.04;
        return bonus;
    }
    
    /**
     * Get mining speed bonus.
     */
    public static double getMiningSpeedBonus(String uuid) {
        int level = getMasteryLevel(uuid, MasteryFamily.GATHERING);
        double b = 0;
        if (level >= 2) b += 0.08;
        if (level >= 6) b += 0.06;
        return b;
    }
    
    /**
     * Check if player has double drop chance for gathering.
     */
    public static double getDoubleDropChance(String uuid) {
        int level = getMasteryLevel(uuid, MasteryFamily.GATHERING);
        if (level >= 3) return 0.16;
        return 0;
    }
    
    /**
     * Check if player has auto-smelt for gathering.
     */
    public static boolean hasAutoSmelt(String uuid) {
        return getMasteryLevel(uuid, MasteryFamily.GATHERING) >= 7;
    }
    
    /**
     * Check if player has bonus coin drop on kills.
     */
    public static double getCoinDropBonus(String uuid) {
        int level = getMasteryLevel(uuid, MasteryFamily.MELEE);
        return level >= 6 ? 0.10 : 0;
    }

    // ════════════════════════════════════════════════════════════
    // VISUAL & DISPLAY
    // ════════════════════════════════════════════════════════════
    
    /**
     * Get the mastery prefix to display on an item name.
     * Format: [M#] ItemName
     */
    public static String getMasteryPrefix(String uuid, MasteryFamily family) {
        int level = getMasteryLevel(uuid, family);
        if (level < 5) return null; // Only show at M5+
        
        String colorCode = level >= 10 ? "§d" : level >= 7 ? "§b" : "§e";
        return colorCode + "[M" + level + "]§r ";
    }
    
    /**
     * Get formatted item name with mastery prefix.
     */
    public static Component getMasteryItemName(ServerPlayer player, ItemStack stack) {
        MasteryFamily family = getItemFamily(stack);
        if (family == null) return stack.getHoverName();
        
        String prefix = getMasteryPrefix(player.getUUID().toString(), family);
        if (prefix == null) return stack.getHoverName();
        
        return Component.literal(prefix).append(stack.getHoverName());
    }
    
    /**
     * Get the player's title for a mastery family (only at M10).
     */
    public static String getMasteryTitle(String uuid, MasteryFamily family) {
        int level = getMasteryLevel(uuid, family);
        if (level < 10) return null;
        
        return switch (family) {
            case MELEE -> "Blademaster";
            case RANGED -> "Deadeye";
            case MAGIC -> "Archmage";
            case GATHERING -> "Prospector";
            case CRAFTING -> "Artificer";
            case ARMOR -> "Guardian";
        };
    }
    
    /**
     * Check if player should show particle effects on hits (M5+).
     */
    public static boolean shouldShowHitParticles(String uuid, MasteryFamily family) {
        return getMasteryLevel(uuid, family) >= 5;
    }
    
    /**
     * Get mastery level for expedition zone access.
     */
    public static boolean canAccessExpeditionZone(String uuid, String zoneId) {
        return switch (zoneId) {
            case "ashen_mines" -> true; // M2 any
            case "drowned_archives" -> hasAnyMasteryLevel(uuid, 4);
            case "glacial_spire" -> hasAnyMasteryLevel(uuid, 5);
            case "void_rift" -> hasAnyMasteryLevel(uuid, 7);
            case "deep_seam" -> getMasteryLevel(uuid, MasteryFamily.GATHERING) >= 10;
            case "grandmaster_vault" -> countMaxedFamilies(uuid) >= 2;
            default -> false;
        };
    }
    
    private static boolean hasAnyMasteryLevel(String uuid, int minLevel) {
        Map<MasteryFamily, MasteryData> allMastery = getAllMastery(uuid);
        return allMastery.values().stream().anyMatch(data -> data.level >= minLevel);
    }
    
    private static int countMaxedFamilies(String uuid) {
        Map<MasteryFamily, MasteryData> allMastery = getAllMastery(uuid);
        return (int) allMastery.values().stream().filter(data -> data.level >= 10).count();
    }

    // ════════════════════════════════════════════════════════════
    // XP ACTIONS
    // ════════════════════════════════════════════════════════════
    
    // XP amounts per action type (families scale these in awardKillXp)
    public static final int XP_MOB_KILL = 6;
    public static final int XP_BOSS_KILL = 50;
    public static final int XP_BLOCK_BREAK = 2;
    public static final int XP_CRAFT = 10;
    public static final int XP_PLAYER_KILL = 22;
    
    /**
     * True for slayer bounty bosses and major vanilla bosses (extra mastery XP).
     */
    public static boolean isBossForMastery(LivingEntity victim) {
        if (SlayerManager.isSlayerBoss(victim.getUUID())) return true;
        return victim instanceof EnderDragon
                || victim instanceof WitherBoss
                || victim instanceof Warden;
    }

    /**
     * Award mastery XP for a kill (mob, boss, or player). Uses main-hand weapon family; empty/other defaults to melee.
     */
    public static void awardKillXp(ServerPlayer player, LivingEntity victim, ItemStack weapon) {
        MasteryFamily family = resolveCombatFamilyForWeapon(weapon);
        boolean pvp = victim instanceof ServerPlayer;
        boolean boss = !pvp && isBossForMastery(victim);
        int xp = killXpFor(family, pvp, boss);
        addXp(player, family, xp);
    }

    /**
     * Award XP for killing a mob with an item.
     */
    public static void awardKillXp(ServerPlayer player, ItemStack weapon, boolean isBoss) {
        MasteryFamily family = resolveCombatFamilyForWeapon(weapon);
        int xp = killXpFor(family, false, isBoss);
        addXp(player, family, xp);
    }

    /** Kills only advance melee / ranged / magic (never gathering, armor, or crafting from a kill). */
    private static MasteryFamily resolveCombatFamilyForWeapon(ItemStack weapon) {
        MasteryFamily f = getItemFamily(weapon);
        if (f == MasteryFamily.RANGED || f == MasteryFamily.MAGIC || f == MasteryFamily.MELEE) {
            return f;
        }
        return MasteryFamily.MELEE;
    }

    private static int killXpFor(MasteryFamily family, boolean playerKill, boolean bossKill) {
        if (playerKill) {
            return switch (family) {
                case RANGED -> 30;
                case MAGIC -> 28;
                default -> XP_PLAYER_KILL;
            };
        }
        if (bossKill) {
            return switch (family) {
                case RANGED -> 62;
                case MAGIC -> 58;
                default -> XP_BOSS_KILL;
            };
        }
        return switch (family) {
            case RANGED -> 8;
            case MAGIC -> 7;
            default -> XP_MOB_KILL;
        };
    }
    
    /**
     * Award XP for breaking a block with a tool.
     */
    public static void awardMiningXp(ServerPlayer player, ItemStack tool) {
        MasteryFamily family = getItemFamily(tool);
        if (family == null) family = MasteryFamily.GATHERING;
        if (family != MasteryFamily.GATHERING) return;

        int xp = Math.max(1, (int) Math.round(XP_BLOCK_BREAK * MasteryHeartTree.masteryXpFromMiningMultiplier(player.getUUID().toString())));
        addXp(player, family, xp);
    }
    
    /**
     * Award XP for crafting an item.
     */
    public static void awardCraftingXp(ServerPlayer player, ItemStack result) {
        MasteryFamily family = resolveCraftingMasteryFamily(result);
        int base = switch (family) {
            case CRAFTING -> 16;
            case ARMOR -> 14;
            case GATHERING -> 11;
            case MELEE, RANGED, MAGIC -> 12;
            default -> XP_CRAFT;
        };
        addXp(player, family, base);
    }

    private static MasteryFamily resolveCraftingMasteryFamily(ItemStack result) {
        if (result.isEmpty()) {
            return MasteryFamily.CRAFTING;
        }
        MasteryFamily f = getItemFamily(result);
        if (f != null) {
            return f;
        }
        return MasteryFamily.CRAFTING;
    }

    /**
     * Armor track: take meaningful hits while wearing armor (throttled).
     */
    public static void awardArmorCombatXp(ServerPlayer player, float damageTaken) {
        if (damageTaken < 0.25f) {
            return;
        }
        long now = player.level().getGameTime();
        String uuid = player.getUUID().toString();
        Long last = lastArmorXpGameTime.get(uuid);
        if (last != null && now - last < 8L) {
            return;
        }
        long armorPieces = 0;
        for (EquipmentSlot slot : new EquipmentSlot[] { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET }) {
            ItemStack piece = player.getItemBySlot(slot);
            if (piece.isEmpty()) continue;
            if (getItemFamily(piece) == MasteryFamily.ARMOR) {
                armorPieces++;
            }
        }
        if (armorPieces == 0) {
            return;
        }
        lastArmorXpGameTime.put(uuid, now);
        int xp = Math.max(1, (int) Math.ceil(damageTaken * 1.8 * (armorPieces / 4.0)));
        addXp(player, MasteryFamily.ARMOR, xp);
    }
}
