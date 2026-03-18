package com.political;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks player progress towards buff obtain conditions.
 * Each buff has specific requirements that must be met.
 */
public class BuffProgressManager {

    // Progress tracking maps
    private static final Map<String, Integer> consecutiveElectionsWon = new ConcurrentHashMap<>();
    private static final Map<String, Integer> lawsPassed = new ConcurrentHashMap<>();
    private static final Map<String, Integer> judicialRulings = new ConcurrentHashMap<>();
    private static final Map<String, Integer> bountyQuestsCompleted = new ConcurrentHashMap<>();
    private static final Map<String, Integer> tier5BossKills = new ConcurrentHashMap<>();
    private static final Map<String, Integer> zombieBossKills = new ConcurrentHashMap<>();
    private static final Map<String, Long> totalCoinsAccumulated = new ConcurrentHashMap<>();
    private static final Map<String, Long> bankBalance = new ConcurrentHashMap<>();
    private static final Map<String, Integer> oresMined = new ConcurrentHashMap<>();
    private static final Map<String, Set<String>> uniqueItemsCollected = new ConcurrentHashMap<>();
    private static final Map<String, Integer> totalVotesReceived = new ConcurrentHashMap<>();
    private static final Map<String, Integer> consecutiveRareDrops = new ConcurrentHashMap<>();
    private static final Map<String, Long> totalPlaytimeMinutes = new ConcurrentHashMap<>();
    
    // New buff progress tracking
    private static final Map<String, Integer> auctionsWon = new ConcurrentHashMap<>();
    private static final Map<String, Integer> snipedAuctions = new ConcurrentHashMap<>();
    private static final Map<String, Integer> wardenBossKills = new ConcurrentHashMap<>();
    private static final Map<String, Integer> enderBossKills = new ConcurrentHashMap<>();
    private static final Map<String, Integer> spiderBossKills = new ConcurrentHashMap<>();
    private static final Map<String, Integer> skeletonBossKills = new ConcurrentHashMap<>();
    private static final Map<String, Integer> prisonEscapes = new ConcurrentHashMap<>();
    private static final Map<String, Integer> playersImprisoned = new ConcurrentHashMap<>();
    private static final Map<String, Integer> impeachmentsSurvived = new ConcurrentHashMap<>();
    private static final Map<String, Integer> termsServed = new ConcurrentHashMap<>();
    private static final Map<String, Integer> cryptoTrades = new ConcurrentHashMap<>();
    private static final Map<String, Integer> cropsHarvested = new ConcurrentHashMap<>();
    private static final Map<String, Integer> fishCaught = new ConcurrentHashMap<>();
    private static final Map<String, Integer> logsChopped = new ConcurrentHashMap<>();
    
    // Last rare drop tracking for LUCKY buff
    private static final Map<String, Long> lastRareDropTime = new ConcurrentHashMap<>();
    private static final int RARE_DROP_WINDOW_TICKS = 100; // 5 seconds between drops to count as consecutive

    // ============================================================
    // PROGRESS UPDATE METHODS
    // ============================================================

    public static void onElectionWin(ServerPlayerEntity player) {
        String uuid = player.getUuidAsString();
        int consecutive = consecutiveElectionsWon.getOrDefault(uuid, 0) + 1;
        consecutiveElectionsWon.put(uuid, consecutive);
        
        // Check for DIPLOMATIC buff (2 consecutive wins)
        if (consecutive >= 2 && !PlayerBuffManager.hasBuff(uuid, PlayerBuffManager.PlayerBuff.DIPLOMATIC)) {
            PlayerBuffManager.grantBuff(player, PlayerBuffManager.PlayerBuff.DIPLOMATIC);
            player.sendMessage(Text.literal("§b§l✦ ACHIEVEMENT UNLOCKED: DIPLOMATIC BUFF!").formatted(Formatting.AQUA), false);
            player.sendMessage(Text.literal("§7You've won 2 consecutive elections! Grants +1 perk slot."), false);
        }
        
        // Reset on loss is handled separately
        DataManager.save(PoliticalServer.server);
    }
    
    public static void onElectionLoss(String uuid) {
        consecutiveElectionsWon.put(uuid, 0);
    }

    public static void onLawPassed(ServerPlayerEntity player) {
        String uuid = player.getUuidAsString();
        int count = lawsPassed.getOrDefault(uuid, 0) + 1;
        lawsPassed.put(uuid, count);
        
        // Check for LEGISLATOR buff (5 laws passed)
        if (count >= 5 && !PlayerBuffManager.hasBuff(uuid, PlayerBuffManager.PlayerBuff.LEGISLATOR)) {
            PlayerBuffManager.grantBuff(player, PlayerBuffManager.PlayerBuff.LEGISLATOR);
            player.sendMessage(Text.literal("§6§l✦ ACHIEVEMENT UNLOCKED: LEGISLATOR BUFF!").formatted(Formatting.GOLD), false);
            player.sendMessage(Text.literal("§7You've passed 5 laws as Chair! Grants +2 perk slots."), false);
        }
        
        DataManager.save(PoliticalServer.server);
    }

    public static void onJudicialRuling(ServerPlayerEntity player) {
        String uuid = player.getUuidAsString();
        int count = judicialRulings.getOrDefault(uuid, 0) + 1;
        judicialRulings.put(uuid, count);
        
        // Check for JUDGE_MASTER buff (50 rulings)
        if (count >= 50 && !PlayerBuffManager.hasBuff(uuid, PlayerBuffManager.PlayerBuff.JUDGE_MASTER)) {
            PlayerBuffManager.grantBuff(player, PlayerBuffManager.PlayerBuff.JUDGE_MASTER);
            player.sendMessage(Text.literal("§5§l✦ ACHIEVEMENT UNLOCKED: JUDGE MASTER BUFF!").formatted(Formatting.LIGHT_PURPLE), false);
            player.sendMessage(Text.literal("§7You've made 50 judicial rulings! Prison sentences are 25% longer."), false);
        }
        
        DataManager.save(PoliticalServer.server);
    }

    public static void onBountyQuestComplete(ServerPlayerEntity player) {
        String uuid = player.getUuidAsString();
        int count = bountyQuestsCompleted.getOrDefault(uuid, 0) + 1;
        bountyQuestsCompleted.put(uuid, count);
        
        // Check for SLAYER_ELITE buff (100 bounty quests)
        if (count >= 100 && !PlayerBuffManager.hasBuff(uuid, PlayerBuffManager.PlayerBuff.SLAYER_ELITE)) {
            PlayerBuffManager.grantBuff(player, PlayerBuffManager.PlayerBuff.SLAYER_ELITE);
            player.sendMessage(Text.literal("§c§l✦ ACHIEVEMENT UNLOCKED: SLAYER ELITE BUFF!").formatted(Formatting.RED), false);
            player.sendMessage(Text.literal("§7You've completed 100 bounty quests! +10% bounty damage."), false);
        }
        
        DataManager.save(PoliticalServer.server);
    }

    public static void onBossKill(ServerPlayerEntity player, int tier, String bossType) {
        String uuid = player.getUuidAsString();
        
        // Track tier 5 boss kills for BOSS_SLAYER
        if (tier >= 5) {
            int count = tier5BossKills.getOrDefault(uuid, 0) + 1;
            tier5BossKills.put(uuid, count);
            
            if (count >= 50 && !PlayerBuffManager.hasBuff(uuid, PlayerBuffManager.PlayerBuff.BOSS_SLAYER)) {
                PlayerBuffManager.grantBuff(player, PlayerBuffManager.PlayerBuff.BOSS_SLAYER);
                player.sendMessage(Text.literal("§4§l✦ ACHIEVEMENT UNLOCKED: BOSS SLAYER BUFF!").formatted(Formatting.DARK_RED), false);
                player.sendMessage(Text.literal("§7You've killed 50 tier 5 bosses! +5% boss drop rates."), false);
            }
        }
        
        // Track zombie boss kills for UNDEAD_HUNTER
        if (bossType.toLowerCase().contains("zombie") || bossType.equalsIgnoreCase("ZOMBIE")) {
            int count = zombieBossKills.getOrDefault(uuid, 0) + 1;
            zombieBossKills.put(uuid, count);
            
            if (count >= 500 && !PlayerBuffManager.hasBuff(uuid, PlayerBuffManager.PlayerBuff.UNDEAD_HUNTER)) {
                PlayerBuffManager.grantBuff(player, PlayerBuffManager.PlayerBuff.UNDEAD_HUNTER);
                player.sendMessage(Text.literal("§2§l✦ ACHIEVEMENT UNLOCKED: UNDEAD HUNTER BUFF!").formatted(Formatting.DARK_GREEN), false);
                player.sendMessage(Text.literal("§7You've killed 500 zombie bosses! +15% damage to undead."), false);
            }
        }
        
        DataManager.save(PoliticalServer.server);
    }

    public static void onCoinsEarned(ServerPlayerEntity player, int amount) {
        String uuid = player.getUuidAsString();
        long total = totalCoinsAccumulated.getOrDefault(uuid, 0L) + amount;
        totalCoinsAccumulated.put(uuid, total);
        
        // Check for TYCOON buff (1,000,000 coins total)
        if (total >= 1000000 && !PlayerBuffManager.hasBuff(uuid, PlayerBuffManager.PlayerBuff.TYCOON)) {
            PlayerBuffManager.grantBuff(player, PlayerBuffManager.PlayerBuff.TYCOON);
            player.sendMessage(Text.literal("§e§l✦ ACHIEVEMENT UNLOCKED: TYCOON BUFF!").formatted(Formatting.YELLOW), false);
            player.sendMessage(Text.literal("§7You've accumulated 1,000,000 coins total! +10% shop sell prices."), false);
        }
        
        DataManager.save(PoliticalServer.server);
    }

    public static void onTrade(ServerPlayerEntity player) {
        String uuid = player.getUuidAsString();
        // Use StockMarket's trade count
        int count = StockMarket.getTradeCount(uuid);
        
        // Check for INVESTOR buff (100 trades)
        if (count >= 100 && !PlayerBuffManager.hasBuff(uuid, PlayerBuffManager.PlayerBuff.INVESTOR)) {
            PlayerBuffManager.grantBuff(player, PlayerBuffManager.PlayerBuff.INVESTOR);
            player.sendMessage(Text.literal("§a§l✦ ACHIEVEMENT UNLOCKED: INVESTOR BUFF!").formatted(Formatting.GREEN), false);
            player.sendMessage(Text.literal("§7You've made 100 stock/crypto trades! 5% reduced market fees."), false);
        }
        
        DataManager.save(PoliticalServer.server);
    }

    public static void onBankDeposit(ServerPlayerEntity player, long balance) {
        String uuid = player.getUuidAsString();
        bankBalance.put(uuid, balance);
        
        // Check for BANKER buff (100,000 coins in bank)
        if (balance >= 100000 && !PlayerBuffManager.hasBuff(uuid, PlayerBuffManager.PlayerBuff.BANKER)) {
            PlayerBuffManager.grantBuff(player, PlayerBuffManager.PlayerBuff.BANKER);
            player.sendMessage(Text.literal("§2§l✦ ACHIEVEMENT UNLOCKED: BANKER BUFF!").formatted(Formatting.DARK_GREEN), false);
            player.sendMessage(Text.literal("§7You have 100,000 coins in the bank! +2% bank interest."), false);
        }
        
        DataManager.save(PoliticalServer.server);
    }

    public static void onOreMined(ServerPlayerEntity player) {
        String uuid = player.getUuidAsString();
        int count = oresMined.getOrDefault(uuid, 0) + 1;
        oresMined.put(uuid, count);
        
        // Check for MINER_MASTER buff (10,000 ores)
        if (count >= 10000 && !PlayerBuffManager.hasBuff(uuid, PlayerBuffManager.PlayerBuff.MINER_MASTER)) {
            PlayerBuffManager.grantBuff(player, PlayerBuffManager.PlayerBuff.MINER_MASTER);
            player.sendMessage(Text.literal("§7§l✦ ACHIEVEMENT UNLOCKED: MINER MASTER BUFF!"), false);
            player.sendMessage(Text.literal("§7You've mined 10,000 ores! +10% mining speed."), false);
        }
        
        DataManager.save(PoliticalServer.server);
    }

    public static void onUniqueItemCollected(ServerPlayerEntity player, String itemId) {
        String uuid = player.getUuidAsString();
        Set<String> items = uniqueItemsCollected.computeIfAbsent(uuid, k -> ConcurrentHashMap.newKeySet());
        items.add(itemId);
        
        // Check for COLLECTOR buff (50 unique custom items)
        if (items.size() >= 50 && !PlayerBuffManager.hasBuff(uuid, PlayerBuffManager.PlayerBuff.COLLECTOR)) {
            PlayerBuffManager.grantBuff(player, PlayerBuffManager.PlayerBuff.COLLECTOR);
            player.sendMessage(Text.literal("§d§l✦ ACHIEVEMENT UNLOCKED: COLLECTOR BUFF!").formatted(Formatting.LIGHT_PURPLE), false);
            player.sendMessage(Text.literal("§7You've collected 50 unique custom items! +15% chance for double ore drops."), false);
        }
        
        DataManager.save(PoliticalServer.server);
    }

    public static void onVoteReceived(ServerPlayerEntity player, int totalVotes) {
        String uuid = player.getUuidAsString();
        totalVotesReceived.put(uuid, totalVotes);
        
        // Check for POPULAR buff (100 votes total)
        if (totalVotes >= 100 && !PlayerBuffManager.hasBuff(uuid, PlayerBuffManager.PlayerBuff.POPULAR)) {
            PlayerBuffManager.grantBuff(player, PlayerBuffManager.PlayerBuff.POPULAR);
            player.sendMessage(Text.literal("§b§l✦ ACHIEVEMENT UNLOCKED: POPULAR BUFF!").formatted(Formatting.AQUA), false);
            player.sendMessage(Text.literal("§7You've received 100 total votes! +1 election vote weight."), false);
        }
        
        DataManager.save(PoliticalServer.server);
    }

    public static void onRareDrop(ServerPlayerEntity player) {
        String uuid = player.getUuidAsString();
        long currentTime = PoliticalServer.server.getTicks();
        Long lastDrop = lastRareDropTime.get(uuid);
        
        if (lastDrop != null && currentTime - lastDrop <= RARE_DROP_WINDOW_TICKS) {
            int consecutive = consecutiveRareDrops.getOrDefault(uuid, 0) + 1;
            consecutiveRareDrops.put(uuid, consecutive);
            
            // Check for LUCKY buff (10 consecutive rare drops)
            if (consecutive >= 10 && !PlayerBuffManager.hasBuff(uuid, PlayerBuffManager.PlayerBuff.LUCKY)) {
                PlayerBuffManager.grantBuff(player, PlayerBuffManager.PlayerBuff.LUCKY);
                player.sendMessage(Text.literal("§e§l✦ ACHIEVEMENT UNLOCKED: LUCKY BUFF!").formatted(Formatting.YELLOW), false);
                player.sendMessage(Text.literal("§7You've rolled 10 rare drops in a row! +5% rare drop chance."), false);
            }
        } else {
            consecutiveRareDrops.put(uuid, 1);
        }
        
        lastRareDropTime.put(uuid, currentTime);
        DataManager.save(PoliticalServer.server);
    }

    public static void onPlaytimeUpdate(ServerPlayerEntity player, long totalMinutes) {
        String uuid = player.getUuidAsString();
        totalPlaytimeMinutes.put(uuid, totalMinutes);
        
        // Check for VETERAN buff (100 hours = 6000 minutes)
        if (totalMinutes >= 6000 && !PlayerBuffManager.hasBuff(uuid, PlayerBuffManager.PlayerBuff.VETERAN)) {
            PlayerBuffManager.grantBuff(player, PlayerBuffManager.PlayerBuff.VETERAN);
            player.sendMessage(Text.literal("§6§l✦ ACHIEVEMENT UNLOCKED: VETERAN BUFF!").formatted(Formatting.GOLD), false);
            player.sendMessage(Text.literal("§7You've played for 100 hours total! +5% XP from all sources."), false);
        }
        
        DataManager.save(PoliticalServer.server);
    }

    // ============================================================
    // PROGRESS GETTERS (for GUI display)
    // ============================================================

    public static int getConsecutiveElectionsWon(String uuid) {
        return consecutiveElectionsWon.getOrDefault(uuid, 0);
    }

    public static int getLawsPassed(String uuid) {
        return lawsPassed.getOrDefault(uuid, 0);
    }

    public static int getJudicialRulings(String uuid) {
        return judicialRulings.getOrDefault(uuid, 0);
    }

    public static int getBountyQuestsCompleted(String uuid) {
        return bountyQuestsCompleted.getOrDefault(uuid, 0);
    }

    public static int getTier5BossKills(String uuid) {
        return tier5BossKills.getOrDefault(uuid, 0);
    }

    public static int getZombieBossKills(String uuid) {
        return zombieBossKills.getOrDefault(uuid, 0);
    }

    public static long getTotalCoinsAccumulated(String uuid) {
        return totalCoinsAccumulated.getOrDefault(uuid, 0L);
    }

    public static int getTradeCount(String uuid) {
        return StockMarket.getTradeCount(uuid);
    }

    public static long getBankBalance(String uuid) {
        return bankBalance.getOrDefault(uuid, 0L);
    }

    public static int getOresMined(String uuid) {
        return oresMined.getOrDefault(uuid, 0);
    }

    public static int getUniqueItemsCount(String uuid) {
        Set<String> items = uniqueItemsCollected.get(uuid);
        return items != null ? items.size() : 0;
    }

    public static int getTotalVotesReceived(String uuid) {
        return totalVotesReceived.getOrDefault(uuid, 0);
    }

    public static int getConsecutiveRareDrops(String uuid) {
        return consecutiveRareDrops.getOrDefault(uuid, 0);
    }

    public static long getPlaytimeHours(String uuid) {
        return totalPlaytimeMinutes.getOrDefault(uuid, 0L) / 60;
    }

    public static int getPlayersHelpedToLevel10(String uuid) {
        return PartyManager.getPlayersHelpedCount(uuid);
    }

    // ============================================================
    // ADDITIONAL PROGRESS GETTERS FOR NEW BUFFS
    // ============================================================

    // Auction Buffs
    public static int getAuctionsWon(String uuid) {
        return auctionsWon.getOrDefault(uuid, 0);
    }

    public static int getSnipedAuctions(String uuid) {
        return snipedAuctions.getOrDefault(uuid, 0);
    }

    // Combat Buffs - specific boss types
    public static int getWardenBossKills(String uuid) {
        return wardenBossKills.getOrDefault(uuid, 0);
    }

    public static int getEnderBossKills(String uuid) {
        return enderBossKills.getOrDefault(uuid, 0);
    }

    public static int getSpiderBossKills(String uuid) {
        return spiderBossKills.getOrDefault(uuid, 0);
    }

    public static int getSkeletonBossKills(String uuid) {
        return skeletonBossKills.getOrDefault(uuid, 0);
    }

    // Prison/Legal Buffs
    public static int getPrisonEscapes(String uuid) {
        return prisonEscapes.getOrDefault(uuid, 0);
    }

    public static int getPlayersImprisoned(String uuid) {
        return playersImprisoned.getOrDefault(uuid, 0);
    }

    // Political Buffs
    public static int getImpeachmentsSurvived(String uuid) {
        return impeachmentsSurvived.getOrDefault(uuid, 0);
    }

    public static int getTermsServed(String uuid) {
        return termsServed.getOrDefault(uuid, 0);
    }

    // Market Buffs
    public static int getCryptoTrades(String uuid) {
        return cryptoTrades.getOrDefault(uuid, 0);
    }

    public static int getStockTrades(String uuid) {
        return StockMarket.getTradeCount(uuid);
    }

    // Farming/Resource Buffs
    public static int getCropsHarvested(String uuid) {
        return cropsHarvested.getOrDefault(uuid, 0);
    }

    public static int getFishCaught(String uuid) {
        return fishCaught.getOrDefault(uuid, 0);
    }

    public static int getLogsChopped(String uuid) {
        return logsChopped.getOrDefault(uuid, 0);
    }

    /**
     * Gets progress string for a specific buff's obtain condition.
     */
    public static String getProgressString(String uuid, PlayerBuffManager.PlayerBuff buff) {
        return switch (buff) {
            case DIPLOMATIC -> getConsecutiveElectionsWon(uuid) + "/2 consecutive elections";
            case LEGISLATOR -> getLawsPassed(uuid) + "/5 laws passed";
            case JUDGE_MASTER -> getJudicialRulings(uuid) + "/50 judicial rulings";
            case SLAYER_ELITE -> getBountyQuestsCompleted(uuid) + "/100 bounty quests";
            case BOSS_SLAYER -> getTier5BossKills(uuid) + "/50 tier 5 bosses";
            case UNDEAD_HUNTER -> getZombieBossKills(uuid) + "/500 zombie bosses";
            case TYCOON -> String.format("%,d", getTotalCoinsAccumulated(uuid)) + "/1,000,000 coins earned";
            case INVESTOR -> getTradeCount(uuid) + "/100 trades";
            case BANKER -> String.format("%,d", getBankBalance(uuid)) + "/100,000 in bank";
            case MINER_MASTER -> String.format("%,d", getOresMined(uuid)) + "/10,000 ores mined";
            case COLLECTOR -> getUniqueItemsCount(uuid) + "/50 unique items";
            case POPULAR -> getTotalVotesReceived(uuid) + "/100 votes received";
            case MENTOR -> getPlayersHelpedToLevel10(uuid) + "/10 players to level 10";
            case LUCKY -> getConsecutiveRareDrops(uuid) + "/10 consecutive rare drops";
            case VETERAN -> getPlaytimeHours(uuid) + "/100 hours played";
            // Auction Buffs
            case AUCTION_MASTER -> getAuctionsWon(uuid) + "/50 auctions won";
            case SNIPER -> getSnipedAuctions(uuid) + "/10 sniped auctions";
            // Combat Buffs
            case WARDEN_HUNTER -> getWardenBossKills(uuid) + "/100 Warden bosses";
            case ENDER_HUNTER -> getEnderBossKills(uuid) + "/200 Enderman bosses";
            case SPIDER_SLAYER -> getSpiderBossKills(uuid) + "/300 Spider bosses";
            case SKELETON_HUNTER -> getSkeletonBossKills(uuid) + "/200 Skeleton bosses";
            // Prison/Legal Buffs
            case ESCAPE_ARTIST -> getPrisonEscapes(uuid) + "/10 prison escapes";
            case PRISON_WARDEN -> getPlayersImprisoned(uuid) + "/100 players imprisoned";
            // Political Buffs
            case SURVIVOR -> getImpeachmentsSurvived(uuid) + "/5 impeachments survived";
            case DICTATOR -> getTermsServed(uuid) + "/10 terms as Chair";
            // Market Buffs
            case CRYPTO_KING -> getCryptoTrades(uuid) + "/200 crypto trades";
            case STOCK_BARON -> getStockTrades(uuid) + "/200 stock trades";
            // Farming/Resource Buffs
            case FARMER -> getCropsHarvested(uuid) + "/10,000 crops harvested";
            case FISHER -> getFishCaught(uuid) + "/1,000 fish caught";
            case WOODCUTTER -> getLogsChopped(uuid) + "/5,000 logs chopped";
            // Admin-Only Buffs (no progress tracking)
            case GODMODE -> "§c§lADMIN ONLY - Cannot be earned";
            case DEITY -> "§d§lADMIN ONLY - Cannot be earned";
            case TRANSCENDENT -> "§b§lADMIN ONLY - Cannot be earned";
        };
    }
}
