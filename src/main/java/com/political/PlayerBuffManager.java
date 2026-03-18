package com.political;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Player Buff System - Permanent stat upgrades.
 * Once earned, buffs are permanent and always active.
 * Obtained through completing certain achievements/feats.
 */
public class PlayerBuffManager {

    // Player buffs storage (UUID -> Set of buff IDs)
    private static final Map<String, Set<String>> playerBuffs = new ConcurrentHashMap<>();
    

    // ═══════════════════════════════════════════════════════════════
    // BUFF DEFINITIONS
    // ═══════════════════════════════════════════════════════════════

    public enum PlayerBuff {
        // Political/Government Buffs
        DIPLOMATIC("diplomatic", "Diplomatic", "§b", Items.GOLDEN_APPLE, 
                "Win 2 consecutive elections", "Grants +1 perk slot"),
        
        LEGISLATOR("legislator", "Legislator", "§6", Items.WRITABLE_BOOK,
                "Pass 5 laws as Chair", "Grants +2 perk slots"),
        
        JUDGE_MASTER("judge_master", "Judge Master", "§5", Items.ENCHANTED_BOOK,
                "Make 50 judicial rulings", "Prison sentences 25% longer"),
        
        // Combat/Slayer Buffs
        SLAYER_ELITE("slayer_elite", "Slayer Elite", "§c", Items.NETHERITE_SWORD,
                "Complete 100 bounty quests", "+10% bounty damage"),
        
        BOSS_SLAYER("boss_slayer", "Boss Slayer", "§4", Items.WITHER_SKELETON_SKULL,
                "Kill 50 tier 5 bosses", "+5% boss drop rates"),
        
        UNDEAD_HUNTER("undead_hunter", "Undead Hunter", "§2", Items.ROTTEN_FLESH,
                "Kill 500 zombie bosses", "+15% damage to undead"),
        
        // Economy Buffs
        TYCOON("tycoon", "Tycoon", "§e", Items.GOLD_BLOCK,
                "Accumulate 1,000,000 coins total", "+10% shop sell prices"),
        
        INVESTOR("investor", "Investor", "§a", Items.EMERALD,
                "Make 100 stock/crypto trades", "5% reduced market fees"),
        
        BANKER("banker", "Banker", "§2", Items.GOLD_INGOT,
                "Have 100,000 coins in bank", "+2% bank interest"),
        
        // Mining/Resource Buffs
        MINER_MASTER("miner_master", "Miner Master", "§7", Items.DIAMOND_PICKAXE,
                "Mine 10,000 ores", "+10% mining speed"),
        
        COLLECTOR("collector", "Collector", "§d", Items.SHULKER_BOX,
                "Collect 50 unique custom items", "+15% chance for double ore drops"),
        
        // Social/Community Buffs
        POPULAR("popular", "Popular", "§b", Items.PLAYER_HEAD,
                "Receive 100 votes total in elections", "+1 election vote weight"),
        
        MENTOR("mentor", "Mentor", "§a", Items.KNOWLEDGE_BOOK,
                "Help 10 players reach level 10", "Party members gain +5% XP"),
        
        // Special Buffs
        LUCKY("lucky", "Lucky", "§e", Items.RABBIT_FOOT,
                "Roll 10 rare drops in a row", "+5% rare drop chance"),
        
        VETERAN("veteran", "Veteran", "§6", Items.CLOCK,
                "Play for 100 hours total", "+5% XP from all sources"),
        
        // Auction Buffs
        AUCTION_MASTER("auction_master", "Auction Master", "§6", Items.GOLD_NUGGET,
                "Win 50 auctions total", "+10% auction bid discount"),
        
        SNIPER("sniper", "Sniper", "§c", Items.BOW,
                "Win 10 auctions in the last 10 seconds", "Auction sniping notification"),
        
        // Combat Buffs (more slayer types)
        WARDEN_HUNTER("warden_hunter", "Warden Hunter", "§8", Items.SCULK,
                "Kill 100 Warden bosses", "+20% damage to Wardens"),
        
        ENDER_HUNTER("ender_hunter", "Ender Hunter", "§5", Items.ENDER_PEARL,
                "Kill 200 Enderman bosses", "+15% damage to Endermen"),
        
        SPIDER_SLAYER("spider_slayer", "Spider Slayer", "§4", Items.SPIDER_EYE,
                "Kill 300 Spider bosses", "+15% damage to spiders"),
        
        SKELETON_HUNTER("skeleton_hunter", "Skeleton Hunter", "§7", Items.BONE,
                "Kill 200 Skeleton bosses", "+15% damage to skeletons"),
        
        // Prison/Legal Buffs
        ESCAPE_ARTIST("escape_artist", "Escape Artist", "§b", Items.ENDER_PEARL,
                "Escape prison 10 times", "10% shorter prison sentences"),
        
        PRISON_WARDEN("prison_warden", "Prison Warden", "§8", Items.IRON_BARS,
                "Imprison 100 players as Judge", "+5% longer sentences you impose"),
        
        // Political Buffs
        SURVIVOR("survivor", "Survivor", "§a", Items.TOTEM_OF_UNDYING,
                "Survive 5 impeachment attempts", "Immunity to first impeachment each term"),
        
        DICTATOR("dictator", "Dictator", "§4", Items.NETHERITE_BLOCK,
                "Serve as Chair for 10 terms", "+1 perk slot while Chair"),
        
        // Market Buffs
        CRYPTO_KING("crypto_king", "Crypto King", "§d", Items.AMETHYST_SHARD,
                "Make 200 crypto trades", "+3% crypto profit margin"),
        
        STOCK_BARON("stock_baron", "Stock Baron", "§b", Items.DIAMOND,
                "Make 200 stock trades", "+3% stock profit margin"),
        
        // Farming/Resource Buffs
        FARMER("farmer", "Farmer", "§a", Items.WHEAT,
                "Harvest 10,000 crops", "+10% crop yield"),
        
        FISHER("fisher", "Fisher", "§9", Items.FISHING_ROD,
                "Catch 1,000 fish", "+10% fishing speed"),
        
        WOODCUTTER("woodcutter", "Woodcutter", "§6", Items.DIAMOND_AXE,
                "Chop 5,000 logs", "+10% wood yield"),
        
        // ═══════════════════════════════════════════════════════════════
        // ADMIN-ONLY OP BUFFS (Cannot be earned, only granted by admins)
        // ═══════════════════════════════════════════════════════════════
        
        GODMODE("godmode", "Godmode", "§c§l", Items.TOTEM_OF_UNDYING,
                "§c§lADMIN ONLY", "§c§lINVINCIBILITY - Immune to all damage"),
        
        DEITY("deity", "Deity", "§d§l", Items.NETHER_STAR,
                "§d§lADMIN ONLY", "§d§l+50% ALL damage dealt, +100% max HP"),
        
        TRANSCENDENT("transcendent", "Transcendent", "§b§l", Items.BEACON,
                "§b§lADMIN ONLY", "§b§lCreative flight, infinite saturation, night vision");

        public final String id;
        public final String displayName;
        public final String colorCode;
        public final Item icon;
        public final String obtainMethod;
        public final String effect;
        public final boolean adminOnly;

        PlayerBuff(String id, String displayName, String colorCode, Item icon, 
                   String obtainMethod, String effect) {
            this.id = id;
            this.displayName = displayName;
            this.colorCode = colorCode;
            this.icon = icon;
            this.obtainMethod = obtainMethod;
            this.effect = effect;
            this.adminOnly = obtainMethod.contains("ADMIN ONLY");
        }

        public static PlayerBuff fromId(String id) {
            for (PlayerBuff buff : values()) {
                if (buff.id.equalsIgnoreCase(id)) return buff;
            }
            return null;
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // BUFF MANAGEMENT
    // ═══════════════════════════════════════════════════════════════

    public static boolean hasBuff(String playerUuid, String buffId) {
        Set<String> buffs = playerBuffs.get(playerUuid);
        return buffs != null && buffs.contains(buffId.toLowerCase());
    }

    public static boolean hasBuff(String playerUuid, PlayerBuff buff) {
        return hasBuff(playerUuid, buff.id);
    }
    
    /**
     * Checks if a buff is currently active.
     * Since buffs are now permanent, this just checks if the player has the buff.
     */
    public static boolean isBuffActive(String playerUuid, PlayerBuff buff) {
        return hasBuff(playerUuid, buff);
    }
    
    /**
     * Gets remaining time in seconds for an active buff.
     * Returns -1 for permanent buffs (infinite time).
     */
    public static long getBuffRemainingTime(String playerUuid, PlayerBuff buff) {
        return hasBuff(playerUuid, buff) ? -1 : 0; // -1 = permanent
    }

    public static Set<String> getBuffs(String playerUuid) {
        return playerBuffs.getOrDefault(playerUuid, new HashSet<>());
    }

    public static Set<PlayerBuff> getBuffObjects(String playerUuid) {
        Set<PlayerBuff> result = new HashSet<>();
        Set<String> buffIds = getBuffs(playerUuid);
        for (String id : buffIds) {
            PlayerBuff buff = PlayerBuff.fromId(id);
            if (buff != null) result.add(buff);
        }
        return result;
    }

    public static boolean addBuff(String playerUuid, String buffId) {
        PlayerBuff buff = PlayerBuff.fromId(buffId);
        if (buff == null) return false;
        
        Set<String> buffs = playerBuffs.computeIfAbsent(playerUuid, k -> new HashSet<>());
        if (buffs.contains(buff.id)) {
            return true; // Already has buff
        }
        
        buffs.add(buff.id);
        return true;
    }

    public static boolean removeBuff(String playerUuid, String buffId) {
        Set<String> buffs = playerBuffs.get(playerUuid);
        if (buffs == null) return false;
        return buffs.remove(buffId.toLowerCase());
    }

    public static void clearBuffs(String playerUuid) {
        playerBuffs.remove(playerUuid);
    }
    

    // ═══════════════════════════════════════════════════════════════
    // BUFF ACQUISITION WITH ANNOUNCEMENT
    // ═══════════════════════════════════════════════════════════════

    public static boolean grantBuff(ServerPlayerEntity player, PlayerBuff buff) {
        boolean isNew = !hasBuff(player.getUuidAsString(), buff);
        if (!addBuff(player.getUuidAsString(), buff.id)) return false;
        
        // Announce to all players
        if (PoliticalServer.server != null) {
            for (ServerPlayerEntity p : PoliticalServer.server.getPlayerManager().getPlayerList()) {
                p.sendMessage(Text.literal("════════════════════════════════").formatted(Formatting.GOLD), false);
                p.sendMessage(Text.literal("✦ PLAYER BUFF UNLOCKED!").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD), false);
                p.sendMessage(Text.literal(""), false);
                p.sendMessage(Text.literal("  " + player.getName().getString())
                        .formatted(Formatting.YELLOW)
                        .append(Text.literal(isNew ? " has earned " : " has re-earned ").formatted(Formatting.WHITE))
                        .append(Text.literal(buff.colorCode + "【" + buff.displayName + "】").formatted(Formatting.BOLD))
                        .append(Text.literal("!").formatted(Formatting.WHITE)), false);
                p.sendMessage(Text.literal("  §7" + buff.effect), false);
                p.sendMessage(Text.literal("  §ePermanent buff!").formatted(Formatting.YELLOW), false);
                p.sendMessage(Text.literal("════════════════════════════════").formatted(Formatting.GOLD), false);
            }
        }
        
        DataManager.save(PoliticalServer.server);
        return true;
    }

    // ═══════════════════════════════════════════════════════════════
    // BUFF EFFECTS - Called by other systems
    // These check if buff is owned (permanent)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Gets extra perk slots. Returns:
     * - +1 if DIPLOMATIC buff is owned
     * - +2 if LEGISLATOR buff is owned
     * Both can stack for +3 total
     */
    public static int getExtraPerkSlots(String playerUuid) {
        int slots = 0;
        if (hasBuff(playerUuid, PlayerBuff.DIPLOMATIC)) {
            slots += 1;
        }
        if (hasBuff(playerUuid, PlayerBuff.LEGISLATOR)) {
            slots += 2;
        }
        return slots;
    }
    
    public static boolean canUseDiplomaticBuff(String playerUuid) {
        return hasBuff(playerUuid, PlayerBuff.DIPLOMATIC);
    }

    public static double getBountyDamageBonus(String playerUuid) {
        return hasBuff(playerUuid, PlayerBuff.SLAYER_ELITE) ? 0.10 : 0.0;
    }

    public static double getBossDropBonus(String playerUuid) {
        return hasBuff(playerUuid, PlayerBuff.BOSS_SLAYER) ? 0.05 : 0.0;
    }

    public static double getUndeadDamageBonus(String playerUuid) {
        return hasBuff(playerUuid, PlayerBuff.UNDEAD_HUNTER) ? 0.15 : 0.0;
    }

    public static double getShopSellBonus(String playerUuid) {
        return hasBuff(playerUuid, PlayerBuff.TYCOON) ? 0.10 : 0.0;
    }

    public static double getMarketFeeReduction(String playerUuid) {
        return hasBuff(playerUuid, PlayerBuff.INVESTOR) ? 0.05 : 0.0;
    }

    public static double getBankInterestBonus(String playerUuid) {
        return hasBuff(playerUuid, PlayerBuff.BANKER) ? 0.02 : 0.0;
    }

    public static double getMiningSpeedBonus(String playerUuid) {
        return hasBuff(playerUuid, PlayerBuff.MINER_MASTER) ? 0.10 : 0.0;
    }

    public static double getDoubleOreChance(String playerUuid) {
        return hasBuff(playerUuid, PlayerBuff.COLLECTOR) ? 0.15 : 0.0;
    }

    public static int getExtraVoteWeight(String playerUuid) {
        return hasBuff(playerUuid, PlayerBuff.POPULAR) ? 1 : 0;
    }

    public static double getPartyXpBonus(String playerUuid) {
        return hasBuff(playerUuid, PlayerBuff.MENTOR) ? 0.05 : 0.0;
    }

    public static double getRareDropBonus(String playerUuid) {
        return hasBuff(playerUuid, PlayerBuff.LUCKY) ? 0.05 : 0.0;
    }

    public static double getXpBonus(String playerUuid) {
        return hasBuff(playerUuid, PlayerBuff.VETERAN) ? 0.05 : 0.0;
    }

    public static double getLawCooldownReduction(String playerUuid) {
        return hasBuff(playerUuid, PlayerBuff.LEGISLATOR) ? 0.15 : 0.0;
    }

    public static double getPrisonSentenceBonus(String playerUuid) {
        return hasBuff(playerUuid, PlayerBuff.JUDGE_MASTER) ? 0.25 : 0.0;
    }

    // ═══════════════════════════════════════════════════════════════
    // PROGRESS TRACKING FOR BUFF UNLOCKS
    // ═══════════════════════════════════════════════════════════════

    public static void checkBuffUnlocks(ServerPlayerEntity player, String playerUuid) {
        MinecraftServer server = PoliticalServer.server;
        if (server == null) return;

        // Check Diplomatic - 2 consecutive election wins
        if (!hasBuff(playerUuid, PlayerBuff.DIPLOMATIC)) {
            if (DataManager.getConsecutiveElectionWins(playerUuid) >= 2) {
                grantBuff(player, PlayerBuff.DIPLOMATIC);
            }
        }

        // Check Slayer Elite - 100 bounty quests
        if (!hasBuff(playerUuid, PlayerBuff.SLAYER_ELITE)) {
            int totalBosses = SlayerData.getTotalBossesKilled(playerUuid);
            if (totalBosses >= 100) {
                grantBuff(player, PlayerBuff.SLAYER_ELITE);
            }
        }

        // Check Tycoon - 1,000,000 coins accumulated
        if (!hasBuff(playerUuid, PlayerBuff.TYCOON)) {
            if (DataManager.getTotalCoinsEarned(playerUuid) >= 1000000) {
                grantBuff(player, PlayerBuff.TYCOON);
            }
        }

        // Check Veteran - 100 hours played
        if (!hasBuff(playerUuid, PlayerBuff.VETERAN)) {
            if (DataManager.getPlaytimeHours(playerUuid) >= 100) {
                grantBuff(player, PlayerBuff.VETERAN);
            }
        }

        // Check Popular - 100 votes received
        if (!hasBuff(playerUuid, PlayerBuff.POPULAR)) {
            if (DataManager.getTotalVotesReceived(playerUuid) >= 100) {
                grantBuff(player, PlayerBuff.POPULAR);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // TAB DISPLAY
    // ═══════════════════════════════════════════════════════════════

    public static String getBuffDisplayForTab(String playerUuid) {
        Set<PlayerBuff> buffs = getBuffObjects(playerUuid);
        if (buffs.isEmpty()) return "";
        
        StringBuilder sb = new StringBuilder(" ");
        for (PlayerBuff buff : buffs) {
            sb.append(buff.colorCode).append("◆");
        }
        return sb.toString();
    }

    // ═══════════════════════════════════════════════════════════════
    // DATA PERSISTENCE
    // ═══════════════════════════════════════════════════════════════

    public static Map<String, Set<String>> getAllBuffData() {
        return new HashMap<>(playerBuffs);
    }

    public static void loadBuffs(Map<String, Set<String>> data) {
        playerBuffs.clear();
        if (data != null) {
            playerBuffs.putAll(data);
        }
    }
    
}
