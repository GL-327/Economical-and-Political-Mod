package com.political;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

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
        // DIMENSION/WARP BUFFS
        // ═══════════════════════════════════════════════════════════════
        
        DIMENSION_HOPPER("dimension_hopper", "Dimension Hopper", "§d", Items.ENDER_PEARL,
                "Use /warp 100 times", "Free warp cooldown, -50% warp cost"),
        
        BACKROOMS_SURVIVOR("backrooms_survivor", "Backrooms Survivor", "§e", Items.WOOL.yellow(),
                "Escape the Backrooms 25 times", "+20% damage in Backrooms, see boss locations"),
        
        VOID_WALKER("void_walker", "Void Walker", "§5", Items.ENDER_EYE,
                "Spend 10 hours in custom dimensions", "No damage in Void dimension, +10% ore rates in Mining"),
        
        MINING_TYCOON("mining_tycoon", "Mining Tycoon", "§b", Items.DIAMOND_ORE,
                "Mine 100,000 ores in Mining dimension", "3x ore rates in Mining dimension"),
        
        // ═══════════════════════════════════════════════════════════════
        // DEATH/PVP BUFFS
        // ═══════════════════════════════════════════════════════════════
        
        DEATH_DEFYING("death_defying", "Death Defying", "§c", Items.TOTEM_OF_UNDYING,
                "Die 100 times", "Lose only 40% coins on death instead of 50%"),
        
        SYPHON_MASTER("syphon_master", "Syphon Master", "§6", Items.GOLD_NUGGET,
                "Syphon 100,000 coins total from kills", "Syphon 15% instead of 10% from victims"),
        
        BOUNTY_HUNTER("bounty_hunter", "Bounty Hunter", "§4", Items.IRON_SWORD,
                "Kill 50 players with bounties", "+25% bonus coins from bounty kills"),
        
        IRON_WALLET("iron_wallet", "Iron Wallet", "§7", Items.IRON_INGOT,
                "Lose 1,000,000,000 coins total to death", "Every 1B coins lost to death grants +1% syphon protection"),
        
        // ═══════════════════════════════════════════════════════════════
        // BANKING BUFFS
        // ═══════════════════════════════════════════════════════════════
        
        WEALTHY("wealthy", "Wealthy", "§e", Items.GOLD_BLOCK,
                "Have 10,000,000 coins in bank total", "+1% extra interest, bypass 50M savings cap"),
        
        BANK_UPGRADER("bank_upgrader", "Bank Upgrader", "§6", Items.GOLDEN_APPLE,
                "Purchase 5 bank upgrades", "-10% bank upgrade costs"),

        // Page 2 — civic / sector extras
        CIVIC_DUTY("civic_duty", "Civic Duty", "§b", Items.PAPER,
                "Cast 50 votes in chair elections (lifetime)", "+5% faster law proposals when Chair"),

        PUBLIC_PATRON("public_patron", "Public Patron", "§2", Items.EMERALD,
                "Pay 500,000 total tax (credits)", "+1% bank interest"),

        PARTY_PATRON("party_patron", "Party Patron", "§d", Items.HEART_OF_THE_SEA,
                "Donate 500,000 coins to your party purse", "+3% party member XP bonus"),

        ASCENSION_TITAN("ascension_titan", "Ascension Titan", "§5", Items.NETHER_STAR,
                "Reach tier 5 in any Sector Ascension collection", "+10 Magic Find (stacks under cap)"),
        
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

    public static boolean grantBuff(ServerPlayer player, PlayerBuff buff) {
        boolean isNew = !hasBuff(player.getUUID().toString(), buff);
        if (!addBuff(player.getUUID().toString(), buff.id)) return false;
        
        // Announce to all players
        if (PoliticalServer.server != null) {
            for (ServerPlayer p : PoliticalServer.server.getPlayerList().getPlayers()) {
                p.sendSystemMessage(Component.literal("════════════════════════════════").withStyle(ChatFormatting.GOLD), false);
                p.sendSystemMessage(Component.literal("✦ PLAYER BUFF UNLOCKED!").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD), false);
                p.sendSystemMessage(Component.literal(""), false);
                p.sendSystemMessage(Component.literal("  " + player.getName().getString())
                        .withStyle(ChatFormatting.YELLOW)
                        .append(Component.literal(isNew ? " has earned " : " has re-earned ").withStyle(ChatFormatting.WHITE))
                        .append(Component.literal(buff.colorCode + "【" + buff.displayName + "】").withStyle(ChatFormatting.BOLD))
                        .append(Component.literal("!").withStyle(ChatFormatting.WHITE)), false);
                p.sendSystemMessage(Component.literal("  §7" + buff.effect), false);
                p.sendSystemMessage(Component.literal("  §ePermanent buff!").withStyle(ChatFormatting.YELLOW), false);
                p.sendSystemMessage(Component.literal("════════════════════════════════").withStyle(ChatFormatting.GOLD), false);
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
        double d = hasBuff(playerUuid, PlayerBuff.BANKER) ? 0.02 : 0.0;
        if (hasBuff(playerUuid, PlayerBuff.PUBLIC_PATRON)) {
            d += 0.01;
        }
        return d;
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
        double d = hasBuff(playerUuid, PlayerBuff.MENTOR) ? 0.05 : 0.0;
        if (hasBuff(playerUuid, PlayerBuff.PARTY_PATRON)) {
            d += 0.03;
        }
        return d;
    }

    public static double getRareDropBonus(String playerUuid) {
        return hasBuff(playerUuid, PlayerBuff.LUCKY) ? 0.05 : 0.0;
    }

    public static double getXpBonus(String playerUuid) {
        return hasBuff(playerUuid, PlayerBuff.VETERAN) ? 0.05 : 0.0;
    }

    public static double getLawCooldownReduction(String playerUuid) {
        double d = 0.0;
        if (hasBuff(playerUuid, PlayerBuff.LEGISLATOR)) {
            d += 0.15;
        }
        if (hasBuff(playerUuid, PlayerBuff.CIVIC_DUTY)) {
            d += 0.05;
        }
        return d;
    }

    public static double getPrisonSentenceBonus(String playerUuid) {
        return hasBuff(playerUuid, PlayerBuff.JUDGE_MASTER) ? 0.25 : 0.0;
    }

    // ═══════════════════════════════════════════════════════════════
    // DIMENSION/WARP BUFF EFFECTS
    // ═══════════════════════════════════════════════════════════════

    public static boolean hasFreeWarpCooldown(String playerUuid) {
        return hasBuff(playerUuid, PlayerBuff.DIMENSION_HOPPER);
    }

    public static double getWarpCostReduction(String playerUuid) {
        return hasBuff(playerUuid, PlayerBuff.DIMENSION_HOPPER) ? 0.50 : 0.0;
    }

    public static double getBackroomsDamageBonus(String playerUuid) {
        return hasBuff(playerUuid, PlayerBuff.BACKROOMS_SURVIVOR) ? 0.20 : 0.0;
    }

    public static boolean canSeeBossLocations(String playerUuid) {
        return hasBuff(playerUuid, PlayerBuff.BACKROOMS_SURVIVOR);
    }

    public static boolean isVoidImmune(String playerUuid) {
        return hasBuff(playerUuid, PlayerBuff.VOID_WALKER);
    }

    public static double getMiningDimensionOreBonus(String playerUuid) {
        if (hasBuff(playerUuid, PlayerBuff.MINING_TYCOON)) return 2.0; // 3x total (1 base + 2 bonus)
        if (hasBuff(playerUuid, PlayerBuff.VOID_WALKER)) return 0.1; // +10%
        return 0.0;
    }

    // ═══════════════════════════════════════════════════════════════
    // DEATH/PVP BUFF EFFECTS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Gets the death coin loss percentage (default 50%, reduced to 40% with DEATH_DEFYING)
     */
    /** On-hand purse is not taxed on death (see {@link DeathPenaltyManager}). */
    public static double getDeathCoinLossPercent(String playerUuid) {
        return 0.0;
    }

    public static double getCryptoReceiveMultiplier(String playerUuid) {
        return hasBuff(playerUuid, PlayerBuff.CRYPTO_KING) ? 1.03 : 1.0;
    }

    /** Extra whole shares when buying (Stock Baron, +3% rounded down). */
    public static int getStockBonusShares(String playerUuid, int shares) {
        if (!hasBuff(playerUuid, PlayerBuff.STOCK_BARON) || shares <= 0) return 0;
        return (int) Math.floor(shares * 0.03);
    }

    public static double getCropYieldBonus(String playerUuid) {
        return hasBuff(playerUuid, PlayerBuff.FARMER) ? 0.10 : 0.0;
    }

    public static double getWoodYieldBonus(String playerUuid) {
        return hasBuff(playerUuid, PlayerBuff.WOODCUTTER) ? 0.10 : 0.0;
    }

    public static double getFishingSpeedBonus(String playerUuid) {
        return hasBuff(playerUuid, PlayerBuff.FISHER) ? 0.10 : 0.0;
    }

    /**
     * Gets the syphon percentage (default 10%, increased to 15% with SYPHON_MASTER)
     */
    public static double getSyphonPercent(String playerUuid) {
        return hasBuff(playerUuid, PlayerBuff.SYPHON_MASTER) ? 0.15 : 0.10;
    }

    /**
     * Gets the percentage of coins protected from syphon.
     * Iron Wallet scales permanently: every 1B coins lost to death grants +1% protection.
     */
    public static double getSyphonProtection(String playerUuid) {
        long lost = BuffProgressManager.getTotalCoinsLostToDeath(playerUuid);
        if (lost <= 0) return 0.0;
        double pct = Math.floor(lost / 1_000_000_000.0) * 0.01;
        return Math.min(0.95, pct);
    }

    public static double getBountyKillBonus(String playerUuid) {
        return hasBuff(playerUuid, PlayerBuff.BOUNTY_HUNTER) ? 0.25 : 0.0;
    }

    // ═══════════════════════════════════════════════════════════════
    // BANKING BUFF EFFECTS
    // ═══════════════════════════════════════════════════════════════

    public static double getWealthyInterestBonus(String playerUuid) {
        return hasBuff(playerUuid, PlayerBuff.WEALTHY) ? 0.01 : 0.0;
    }

    public static boolean bypassesSavingsCap(String playerUuid) {
        return hasBuff(playerUuid, PlayerBuff.WEALTHY);
    }

    public static double getBankUpgradeDiscount(String playerUuid) {
        return hasBuff(playerUuid, PlayerBuff.BANK_UPGRADER) ? 0.10 : 0.0;
    }

    // ═══════════════════════════════════════════════════════════════
    // PROGRESS TRACKING FOR BUFF UNLOCKS
    // ═══════════════════════════════════════════════════════════════

    public static void checkBuffUnlocks(ServerPlayer player, String playerUuid) {
        if (PoliticalServer.server == null) return;

        if (!hasBuff(playerUuid, PlayerBuff.DIPLOMATIC) && DataManager.getConsecutiveElectionWins(playerUuid) >= 2) {
            grantBuff(player, PlayerBuff.DIPLOMATIC);
        }

        if (!hasBuff(playerUuid, PlayerBuff.SLAYER_ELITE) && BuffProgressManager.getBountyQuestsCompleted(playerUuid) >= 100) {
            grantBuff(player, PlayerBuff.SLAYER_ELITE);
        }

        if (!hasBuff(playerUuid, PlayerBuff.TYCOON) && BuffProgressManager.getTotalCoinsAccumulated(playerUuid) >= 1_000_000L) {
            grantBuff(player, PlayerBuff.TYCOON);
        }

        if (!hasBuff(playerUuid, PlayerBuff.VETERAN) && BuffProgressManager.getPlaytimeHours(playerUuid) >= 100L) {
            grantBuff(player, PlayerBuff.VETERAN);
        }

        if (!hasBuff(playerUuid, PlayerBuff.POPULAR) && BuffProgressManager.getTotalVotesReceived(playerUuid) >= 100) {
            grantBuff(player, PlayerBuff.POPULAR);
        }

        if (!hasBuff(playerUuid, PlayerBuff.BOSS_SLAYER) && BuffProgressManager.getTier5BossKills(playerUuid) >= 50) {
            grantBuff(player, PlayerBuff.BOSS_SLAYER);
        }

        if (!hasBuff(playerUuid, PlayerBuff.UNDEAD_HUNTER) && BuffProgressManager.getZombieBossKills(playerUuid) >= 500) {
            grantBuff(player, PlayerBuff.UNDEAD_HUNTER);
        }

        if (!hasBuff(playerUuid, PlayerBuff.LEGISLATOR) && BuffProgressManager.getLawsPassed(playerUuid) >= 5) {
            grantBuff(player, PlayerBuff.LEGISLATOR);
        }

        if (!hasBuff(playerUuid, PlayerBuff.JUDGE_MASTER) && BuffProgressManager.getJudicialRulings(playerUuid) >= 50) {
            grantBuff(player, PlayerBuff.JUDGE_MASTER);
        }

        if (!hasBuff(playerUuid, PlayerBuff.MINER_MASTER) && BuffProgressManager.getOresMined(playerUuid) >= 10_000) {
            grantBuff(player, PlayerBuff.MINER_MASTER);
        }

        if (!hasBuff(playerUuid, PlayerBuff.BANKER) && BuffProgressManager.getBankBalance(playerUuid) >= 100_000L) {
            grantBuff(player, PlayerBuff.BANKER);
        }

        if (!hasBuff(playerUuid, PlayerBuff.INVESTOR) && BuffProgressManager.getTradeCount(playerUuid) >= 100) {
            grantBuff(player, PlayerBuff.INVESTOR);
        }

        if (!hasBuff(playerUuid, PlayerBuff.CRYPTO_KING) && BuffProgressManager.getCryptoTrades(playerUuid) >= 200) {
            grantBuff(player, PlayerBuff.CRYPTO_KING);
        }

        if (!hasBuff(playerUuid, PlayerBuff.STOCK_BARON) && BuffProgressManager.getStockOnlyTrades(playerUuid) >= 200) {
            grantBuff(player, PlayerBuff.STOCK_BARON);
        }

        if (!hasBuff(playerUuid, PlayerBuff.CIVIC_DUTY) && BuffProgressManager.getElectionVotesCast(playerUuid) >= 50) {
            grantBuff(player, PlayerBuff.CIVIC_DUTY);
        }

        if (!hasBuff(playerUuid, PlayerBuff.PUBLIC_PATRON) && BuffProgressManager.getTotalTaxPaid(playerUuid) >= 500_000L) {
            grantBuff(player, PlayerBuff.PUBLIC_PATRON);
        }

        if (!hasBuff(playerUuid, PlayerBuff.PARTY_PATRON) && BuffProgressManager.getTotalPartyDonations(playerUuid) >= 500_000L) {
            grantBuff(player, PlayerBuff.PARTY_PATRON);
        }

        if (!hasBuff(playerUuid, PlayerBuff.ASCENSION_TITAN) && BuffProgressManager.getMaxAscensionCollectionTier(playerUuid) >= 5) {
            grantBuff(player, PlayerBuff.ASCENSION_TITAN);
        }

        if (!hasBuff(playerUuid, PlayerBuff.FARMER) && BuffProgressManager.getCropsHarvested(playerUuid) >= 10_000) {
            grantBuff(player, PlayerBuff.FARMER);
        }
        if (!hasBuff(playerUuid, PlayerBuff.FISHER) && BuffProgressManager.getFishCaught(playerUuid) >= 1_000) {
            grantBuff(player, PlayerBuff.FISHER);
        }
        if (!hasBuff(playerUuid, PlayerBuff.WOODCUTTER) && BuffProgressManager.getLogsChopped(playerUuid) >= 5_000) {
            grantBuff(player, PlayerBuff.WOODCUTTER);
        }
        if (!hasBuff(playerUuid, PlayerBuff.AUCTION_MASTER) && BuffProgressManager.getAuctionsWon(playerUuid) >= 50) {
            grantBuff(player, PlayerBuff.AUCTION_MASTER);
        }
        if (!hasBuff(playerUuid, PlayerBuff.SNIPER) && BuffProgressManager.getSnipedAuctions(playerUuid) >= 10) {
            grantBuff(player, PlayerBuff.SNIPER);
        }

        BuffProgressManager.tryGrantAscensionTitan(player);
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
        Map<String, Set<String>> out = new HashMap<>();
        for (Map.Entry<String, Set<String>> e : playerBuffs.entrySet()) {
            Set<String> normalized = new HashSet<>();
            if (e.getValue() != null) {
                for (String id : e.getValue()) {
                    if (id == null) continue;
                    normalized.add(id.toLowerCase());
                }
            }
            out.put(e.getKey(), normalized);
        }
        return out;
    }

    public static void loadBuffs(Map<String, Set<String>> data) {
        playerBuffs.clear();
        if (data != null) {
            for (Map.Entry<String, Set<String>> e : data.entrySet()) {
                Set<String> normalized = new HashSet<>();
                if (e.getValue() != null) {
                    for (String id : e.getValue()) {
                        if (id == null) continue;
                        normalized.add(id.toLowerCase());
                    }
                }
                playerBuffs.put(e.getKey(), normalized);
            }
        }
    }
    
}
