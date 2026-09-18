package com.political;


import com.political.economy.AuctionHouseManager;
import com.political.economy.StockMarket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

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
    private static final Map<String, Integer> stockTrades = new ConcurrentHashMap<>();
    private static final Map<String, Integer> cropsHarvested = new ConcurrentHashMap<>();
    private static final Map<String, Integer> fishCaught = new ConcurrentHashMap<>();
    private static final Map<String, Integer> logsChopped = new ConcurrentHashMap<>();
    
    // Dimension/Warp tracking
    private static final Map<String, Integer> warpUses = new ConcurrentHashMap<>();
    private static final Map<String, Integer> backroomsEscapes = new ConcurrentHashMap<>();
    private static final Map<String, Long> dimensionTimeMs = new ConcurrentHashMap<>();
    private static final Map<String, Integer> miningDimensionOres = new ConcurrentHashMap<>();
    
    // Death/PvP tracking
    private static final Map<String, Integer> totalDeaths = new ConcurrentHashMap<>();
    private static final Map<String, Long> totalSyphoned = new ConcurrentHashMap<>();
    private static final Map<String, Integer> bountyKills = new ConcurrentHashMap<>();
    private static final Map<String, Long> totalCoinsLostToDeath = new ConcurrentHashMap<>();
    
    // Banking tracking
    private static final Map<String, Integer> bankUpgradesPurchased = new ConcurrentHashMap<>();

    private static final Map<String, Integer> electionVotesCast = new ConcurrentHashMap<>();
    private static final Map<String, Long> totalTaxPaid = new ConcurrentHashMap<>();
    private static final Map<String, Long> totalPartyDonations = new ConcurrentHashMap<>();
    
    // Last rare drop tracking for LUCKY buff
    private static final Map<String, Long> lastRareDropTime = new ConcurrentHashMap<>();
    private static final int RARE_DROP_WINDOW_TICKS = 100; // 5 seconds between drops to count as consecutive

    // ============================================================
    // PROGRESS UPDATE METHODS
    // ============================================================

    /** @deprecated Election streak is tracked in {@link DataManager}; use {@link #tryGrantDiplomatic(ServerPlayer)} after results. */
    @Deprecated
    public static void onElectionWin(ServerPlayer player) {
        tryGrantDiplomatic(player);
    }

    public static void onElectionLoss(String uuid) {
        DataManager.resetConsecutiveElectionWins(uuid);
        consecutiveElectionsWon.put(uuid, 0);
    }

    public static void tryGrantDiplomatic(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        consecutiveElectionsWon.put(uuid, DataManager.getConsecutiveElectionWins(uuid));
        if (DataManager.getConsecutiveElectionWins(uuid) >= 2
                && !PlayerBuffManager.hasBuff(uuid, PlayerBuffManager.PlayerBuff.DIPLOMATIC)) {
            PlayerBuffManager.grantBuff(player, PlayerBuffManager.PlayerBuff.DIPLOMATIC);
            player.sendSystemMessage(Component.literal("§b§l✦ ACHIEVEMENT UNLOCKED: DIPLOMATIC BUFF!").withStyle(ChatFormatting.AQUA), false);
            player.sendSystemMessage(Component.literal("§7You've won 2 consecutive elections! Grants +1 perk slot."), false);
        }
    }

    public static void onLawPassed(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        DataManager.incrementLawsPassed(uuid);
        int count = DataManager.getLawsPassed(uuid);
        lawsPassed.put(uuid, count);
        
        // Check for LEGISLATOR buff (5 laws passed)
        if (count >= 5 && !PlayerBuffManager.hasBuff(uuid, PlayerBuffManager.PlayerBuff.LEGISLATOR)) {
            PlayerBuffManager.grantBuff(player, PlayerBuffManager.PlayerBuff.LEGISLATOR);
            player.sendSystemMessage(Component.literal("§6§l✦ ACHIEVEMENT UNLOCKED: LEGISLATOR BUFF!").withStyle(ChatFormatting.GOLD), false);
            player.sendSystemMessage(Component.literal("§7You've passed 5 laws as Chair! Grants +2 perk slots."), false);
        }
        
        DataManager.save(PoliticalServer.server);
    }

    public static void onJudicialRuling(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        DataManager.incrementJudicialRulings(uuid);
        int count = DataManager.getJudicialRulings(uuid);
        judicialRulings.put(uuid, count);
        
        // Check for JUDGE_MASTER buff (50 rulings)
        if (count >= 50 && !PlayerBuffManager.hasBuff(uuid, PlayerBuffManager.PlayerBuff.JUDGE_MASTER)) {
            PlayerBuffManager.grantBuff(player, PlayerBuffManager.PlayerBuff.JUDGE_MASTER);
            player.sendSystemMessage(Component.literal("§5§l✦ ACHIEVEMENT UNLOCKED: JUDGE MASTER BUFF!").withStyle(ChatFormatting.LIGHT_PURPLE), false);
            player.sendSystemMessage(Component.literal("§7You've made 50 judicial rulings! Prison sentences are 25% longer."), false);
        }
        
        DataManager.save(PoliticalServer.server);
    }

    public static void onBountyQuestComplete(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        int count = bountyQuestsCompleted.getOrDefault(uuid, 0) + 1;
        bountyQuestsCompleted.put(uuid, count);
        
        // Check for SLAYER_ELITE buff (100 bounty quests)
        if (count >= 100 && !PlayerBuffManager.hasBuff(uuid, PlayerBuffManager.PlayerBuff.SLAYER_ELITE)) {
            PlayerBuffManager.grantBuff(player, PlayerBuffManager.PlayerBuff.SLAYER_ELITE);
            player.sendSystemMessage(Component.literal("§c§l✦ ACHIEVEMENT UNLOCKED: SLAYER ELITE BUFF!").withStyle(ChatFormatting.RED), false);
            player.sendSystemMessage(Component.literal("§7You've completed 100 bounty quests! +10% bounty damage."), false);
        }
        
        DataManager.save(PoliticalServer.server);
    }

    public static void onBossKill(ServerPlayer player, int tier, String bossType) {
        String uuid = player.getUUID().toString();
        String t = bossType != null ? bossType.toUpperCase(Locale.ROOT) : "";

        if (tier >= 5) {
            DataManager.incrementTier5BossKills(uuid);
            int count = DataManager.getTier5BossKills(uuid);
            tier5BossKills.put(uuid, count);
            if (count >= 50 && !PlayerBuffManager.hasBuff(uuid, PlayerBuffManager.PlayerBuff.BOSS_SLAYER)) {
                PlayerBuffManager.grantBuff(player, PlayerBuffManager.PlayerBuff.BOSS_SLAYER);
                player.sendSystemMessage(Component.literal("§4§l✦ ACHIEVEMENT UNLOCKED: BOSS SLAYER BUFF!").withStyle(ChatFormatting.DARK_RED), false);
                player.sendSystemMessage(Component.literal("§7You've killed 50 tier 5 bosses! +5% boss drop rates."), false);
            }
        }

        if (t.contains("ZOMBIE")) {
            DataManager.incrementZombieBossKills(uuid);
            int zc = DataManager.getZombieBossKills(uuid);
            zombieBossKills.put(uuid, zc);
            if (zc >= 500 && !PlayerBuffManager.hasBuff(uuid, PlayerBuffManager.PlayerBuff.UNDEAD_HUNTER)) {
                PlayerBuffManager.grantBuff(player, PlayerBuffManager.PlayerBuff.UNDEAD_HUNTER);
                player.sendSystemMessage(Component.literal("§2§l✦ ACHIEVEMENT UNLOCKED: UNDEAD HUNTER BUFF!").withStyle(ChatFormatting.DARK_GREEN), false);
                player.sendSystemMessage(Component.literal("§7You've killed 500 zombie bosses! +15% damage to undead."), false);
            }
        }

        // SlayerType names: IRON_GOLEM = Warden line, ENDERMAN, SPIDER, SKELETON
        if (t.contains("IRON_GOLEM") || t.contains("WARDEN")) {
            bumpBossType(uuid, player, wardenBossKills, 100, PlayerBuffManager.PlayerBuff.WARDEN_HUNTER,
                    "§8§l✦ ACHIEVEMENT UNLOCKED: WARDEN HUNTER BUFF!", "§7100 Warden-tier bosses!");
        } else if (t.contains("ENDERMAN") || t.contains("ENDER")) {
            bumpBossType(uuid, player, enderBossKills, 200, PlayerBuffManager.PlayerBuff.ENDER_HUNTER,
                    "§5§l✦ ACHIEVEMENT UNLOCKED: ENDER HUNTER BUFF!", "§7200 Enderman bosses!");
        } else if (t.contains("SPIDER")) {
            bumpBossType(uuid, player, spiderBossKills, 300, PlayerBuffManager.PlayerBuff.SPIDER_SLAYER,
                    "§4§l✦ ACHIEVEMENT UNLOCKED: SPIDER SLAYER BUFF!", "§7300 Spider bosses!");
        } else if (t.contains("SKELETON")) {
            bumpBossType(uuid, player, skeletonBossKills, 200, PlayerBuffManager.PlayerBuff.SKELETON_HUNTER,
                    "§7§l✦ ACHIEVEMENT UNLOCKED: SKELETON HUNTER BUFF!", "§7200 Skeleton bosses!");
        }

        DataManager.save(PoliticalServer.server);
    }

    private static void bumpBossType(String uuid, ServerPlayer player, Map<String, Integer> map, int need,
                                     PlayerBuffManager.PlayerBuff buff, String title, String sub) {
        int c = map.getOrDefault(uuid, 0) + 1;
        map.put(uuid, c);
        if (c >= need && !PlayerBuffManager.hasBuff(uuid, buff)) {
            PlayerBuffManager.grantBuff(player, buff);
            player.sendSystemMessage(Component.literal(title).withStyle(ChatFormatting.WHITE), false);
            player.sendSystemMessage(Component.literal(sub).withStyle(ChatFormatting.GRAY), false);
        }
    }

    public static void onCoinsEarned(ServerPlayer player, long amount) {
        if (amount <= 0) return;
        String uuid = player.getUUID().toString();
        DataManager.addTotalCoinsEarned(uuid, amount);
        long total = DataManager.getTotalCoinsEarned(uuid);
        totalCoinsAccumulated.put(uuid, total);
        if (total >= 1_000_000L && !PlayerBuffManager.hasBuff(uuid, PlayerBuffManager.PlayerBuff.TYCOON)) {
            PlayerBuffManager.grantBuff(player, PlayerBuffManager.PlayerBuff.TYCOON);
            player.sendSystemMessage(Component.literal("§e§l✦ ACHIEVEMENT UNLOCKED: TYCOON BUFF!").withStyle(ChatFormatting.YELLOW), false);
            player.sendSystemMessage(Component.literal("§7You've accumulated 1,000,000 coins total! +10% shop sell prices."), false);
        }
    }

    public static void onTrade(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        // Use StockMarket's trade count
        int count = StockMarket.getTradeCount(uuid);
        
        // Check for INVESTOR buff (100 trades)
        if (count >= 100 && !PlayerBuffManager.hasBuff(uuid, PlayerBuffManager.PlayerBuff.INVESTOR)) {
            PlayerBuffManager.grantBuff(player, PlayerBuffManager.PlayerBuff.INVESTOR);
            player.sendSystemMessage(Component.literal("§a§l✦ ACHIEVEMENT UNLOCKED: INVESTOR BUFF!").withStyle(ChatFormatting.GREEN), false);
            player.sendSystemMessage(Component.literal("§7You've made 100 stock/crypto trades! 5% reduced market fees."), false);
        }
        
        DataManager.save(PoliticalServer.server);
    }

    public static void onBankDeposit(ServerPlayer player, long balance) {
        String uuid = player.getUUID().toString();
        bankBalance.put(uuid, balance);
        
        // Check for BANKER buff (100,000 coins in bank)
        if (balance >= 100000 && !PlayerBuffManager.hasBuff(uuid, PlayerBuffManager.PlayerBuff.BANKER)) {
            PlayerBuffManager.grantBuff(player, PlayerBuffManager.PlayerBuff.BANKER);
            player.sendSystemMessage(Component.literal("§2§l✦ ACHIEVEMENT UNLOCKED: BANKER BUFF!").withStyle(ChatFormatting.DARK_GREEN), false);
            player.sendSystemMessage(Component.literal("§7You have 100,000 coins in the bank! +2% bank interest."), false);
        }
        
        DataManager.save(PoliticalServer.server);
    }

    public static void onOreMined(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        DataManager.incrementOresMined(uuid, 1);
        int count = DataManager.getOresMined(uuid);
        oresMined.put(uuid, count);
        
        // Check for MINER_MASTER buff (10,000 ores)
        if (count >= 10000 && !PlayerBuffManager.hasBuff(uuid, PlayerBuffManager.PlayerBuff.MINER_MASTER)) {
            PlayerBuffManager.grantBuff(player, PlayerBuffManager.PlayerBuff.MINER_MASTER);
            player.sendSystemMessage(Component.literal("§7§l✦ ACHIEVEMENT UNLOCKED: MINER MASTER BUFF!"), false);
            player.sendSystemMessage(Component.literal("§7You've mined 10,000 ores! +10% mining speed."), false);
        }
        
        DataManager.save(PoliticalServer.server);
    }

    public static void onUniqueItemCollected(ServerPlayer player, String itemId) {
        String uuid = player.getUUID().toString();
        Set<String> items = uniqueItemsCollected.computeIfAbsent(uuid, k -> ConcurrentHashMap.newKeySet());
        items.add(itemId);
        
        // Check for COLLECTOR buff (50 unique custom items)
        if (items.size() >= 50 && !PlayerBuffManager.hasBuff(uuid, PlayerBuffManager.PlayerBuff.COLLECTOR)) {
            PlayerBuffManager.grantBuff(player, PlayerBuffManager.PlayerBuff.COLLECTOR);
            player.sendSystemMessage(Component.literal("§d§l✦ ACHIEVEMENT UNLOCKED: COLLECTOR BUFF!").withStyle(ChatFormatting.LIGHT_PURPLE), false);
            player.sendSystemMessage(Component.literal("§7You've collected 50 unique custom items! +15% chance for double ore drops."), false);
        }
        
        DataManager.save(PoliticalServer.server);
    }

    public static void onVoteReceived(ServerPlayer player, int totalVotes) {
        String uuid = player.getUUID().toString();
        int cur = DataManager.getTotalVotesReceived(uuid);
        if (totalVotes > cur) {
            DataManager.addVotesReceived(uuid, totalVotes - cur);
        }
        totalVotesReceived.put(uuid, DataManager.getTotalVotesReceived(uuid));
        tryGrantPopular(player);
        DataManager.save(PoliticalServer.server);
    }

    public static void tryGrantPopular(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        int v = DataManager.getTotalVotesReceived(uuid);
        if (v >= 100 && !PlayerBuffManager.hasBuff(uuid, PlayerBuffManager.PlayerBuff.POPULAR)) {
            PlayerBuffManager.grantBuff(player, PlayerBuffManager.PlayerBuff.POPULAR);
            player.sendSystemMessage(Component.literal("§b§l✦ ACHIEVEMENT UNLOCKED: POPULAR BUFF!").withStyle(ChatFormatting.AQUA), false);
            player.sendSystemMessage(Component.literal("§7You've received 100 total votes! +1 election vote weight."), false);
        }
    }

    public static void onRareDrop(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        long currentTime = PoliticalServer.server.getTickCount();
        Long lastDrop = lastRareDropTime.get(uuid);
        
        if (lastDrop != null && currentTime - lastDrop <= RARE_DROP_WINDOW_TICKS) {
            int consecutive = consecutiveRareDrops.getOrDefault(uuid, 0) + 1;
            consecutiveRareDrops.put(uuid, consecutive);
            
            // Check for LUCKY buff (10 consecutive rare drops)
            if (consecutive >= 10 && !PlayerBuffManager.hasBuff(uuid, PlayerBuffManager.PlayerBuff.LUCKY)) {
                PlayerBuffManager.grantBuff(player, PlayerBuffManager.PlayerBuff.LUCKY);
                player.sendSystemMessage(Component.literal("§e§l✦ ACHIEVEMENT UNLOCKED: LUCKY BUFF!").withStyle(ChatFormatting.YELLOW), false);
                player.sendSystemMessage(Component.literal("§7You've rolled 10 rare drops in a row! +5% rare drop chance."), false);
            }
        } else {
            consecutiveRareDrops.put(uuid, 1);
        }
        
        lastRareDropTime.put(uuid, currentTime);
        DataManager.save(PoliticalServer.server);
    }

    public static void onPlaytimeUpdate(ServerPlayer player, long totalMinutes) {
        String uuid = player.getUUID().toString();
        DataManager.setPlaytimeMinutesAtLeast(uuid, totalMinutes);
        totalPlaytimeMinutes.put(uuid, DataManager.getPlaytimeMinutes(uuid));
        tryGrantVeteran(player);
        DataManager.save(PoliticalServer.server);
    }

    private static void tryGrantVeteran(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        if (DataManager.getPlaytimeMinutes(uuid) >= 6000L
                && !PlayerBuffManager.hasBuff(uuid, PlayerBuffManager.PlayerBuff.VETERAN)) {
            PlayerBuffManager.grantBuff(player, PlayerBuffManager.PlayerBuff.VETERAN);
            player.sendSystemMessage(Component.literal("§6§l✦ ACHIEVEMENT UNLOCKED: VETERAN BUFF!").withStyle(ChatFormatting.GOLD), false);
            player.sendSystemMessage(Component.literal("§7You've played for 100 hours total! +5% XP from all sources."), false);
        }
    }

    // ============================================================
    // DIMENSION/WARP PROGRESS METHODS
    // ============================================================

    public static void onWarpUse(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        int count = warpUses.getOrDefault(uuid, 0) + 1;
        warpUses.put(uuid, count);
        
        if (count >= 100 && !PlayerBuffManager.hasBuff(uuid, PlayerBuffManager.PlayerBuff.DIMENSION_HOPPER)) {
            PlayerBuffManager.grantBuff(player, PlayerBuffManager.PlayerBuff.DIMENSION_HOPPER);
            player.sendSystemMessage(Component.literal("§d§l✦ ACHIEVEMENT UNLOCKED: DIMENSION HOPPER BUFF!"), false);
            player.sendSystemMessage(Component.literal("§7You've used /warp 100 times! Free warp cooldown, -50% warp cost."), false);
        }
        
        DataManager.save(PoliticalServer.server);
    }

    public static void onBackroomsEscape(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        int count = backroomsEscapes.getOrDefault(uuid, 0) + 1;
        backroomsEscapes.put(uuid, count);
        
        if (count >= 25 && !PlayerBuffManager.hasBuff(uuid, PlayerBuffManager.PlayerBuff.BACKROOMS_SURVIVOR)) {
            PlayerBuffManager.grantBuff(player, PlayerBuffManager.PlayerBuff.BACKROOMS_SURVIVOR);
            player.sendSystemMessage(Component.literal("§e§l✦ ACHIEVEMENT UNLOCKED: BACKROOMS SURVIVOR BUFF!"), false);
            player.sendSystemMessage(Component.literal("§7You've escaped the Backrooms 25 times! +20% damage in Backrooms, see boss locations."), false);
        }
        
        DataManager.save(PoliticalServer.server);
    }

    public static void onDimensionTime(ServerPlayer player, long additionalMs) {
        String uuid = player.getUUID().toString();
        long total = dimensionTimeMs.getOrDefault(uuid, 0L) + additionalMs;
        dimensionTimeMs.put(uuid, total);
        
        if (total >= 36000000L && !PlayerBuffManager.hasBuff(uuid, PlayerBuffManager.PlayerBuff.VOID_WALKER)) { // 10 hours
            PlayerBuffManager.grantBuff(player, PlayerBuffManager.PlayerBuff.VOID_WALKER);
            player.sendSystemMessage(Component.literal("§5§l✦ ACHIEVEMENT UNLOCKED: VOID WALKER BUFF!"), false);
            player.sendSystemMessage(Component.literal("§7You've spent 10 hours in custom dimensions! No damage in Void, +10% ore rates in Mining."), false);
        }
        
        DataManager.save(PoliticalServer.server);
    }

    public static void onMiningDimensionOre(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        int count = miningDimensionOres.getOrDefault(uuid, 0) + 1;
        miningDimensionOres.put(uuid, count);
        
        if (count >= 100000 && !PlayerBuffManager.hasBuff(uuid, PlayerBuffManager.PlayerBuff.MINING_TYCOON)) {
            PlayerBuffManager.grantBuff(player, PlayerBuffManager.PlayerBuff.MINING_TYCOON);
            player.sendSystemMessage(Component.literal("§b§l✦ ACHIEVEMENT UNLOCKED: MINING TYCOON BUFF!"), false);
            player.sendSystemMessage(Component.literal("§7You've mined 100,000 ores in Mining dimension! 3x ore rates in Mining."), false);
        }
        
        DataManager.save(PoliticalServer.server);
    }

    // ============================================================
    // DEATH/PVP PROGRESS METHODS
    // ============================================================

    public static void onDeath(ServerPlayer player, long coinsLost) {
        String uuid = player.getUUID().toString();
        int deathCount = totalDeaths.getOrDefault(uuid, 0) + 1;
        totalDeaths.put(uuid, deathCount);
        
        long totalLost = totalCoinsLostToDeath.getOrDefault(uuid, 0L) + coinsLost;
        totalCoinsLostToDeath.put(uuid, totalLost);
        
        // Check for DEATH_DEFYING buff (100 deaths)
        if (deathCount >= 100 && !PlayerBuffManager.hasBuff(uuid, PlayerBuffManager.PlayerBuff.DEATH_DEFYING)) {
            PlayerBuffManager.grantBuff(player, PlayerBuffManager.PlayerBuff.DEATH_DEFYING);
            player.sendSystemMessage(Component.literal("§c§l✦ ACHIEVEMENT UNLOCKED: DEATH DEFYING BUFF!"), false);
            player.sendSystemMessage(Component.literal("§7You've died 100 times! Lose only 40% coins on death instead of 50%."), false);
        }
        
        // Check for IRON_WALLET buff (1B coins lost to death)
        if (totalLost >= 1_000_000_000L && !PlayerBuffManager.hasBuff(uuid, PlayerBuffManager.PlayerBuff.IRON_WALLET)) {
            PlayerBuffManager.grantBuff(player, PlayerBuffManager.PlayerBuff.IRON_WALLET);
            player.sendSystemMessage(Component.literal("§7§l✦ ACHIEVEMENT UNLOCKED: IRON WALLET BUFF!"), false);
            player.sendSystemMessage(Component.literal("§7You've lost 1,000,000,000 coins to death! Gain +1% syphon protection per 1B lost."), false);
        }
        
        DataManager.save(PoliticalServer.server);
    }

    public static void onSyphon(ServerPlayer killer, long amount) {
        String uuid = killer.getUUID().toString();
        long total = totalSyphoned.getOrDefault(uuid, 0L) + amount;
        totalSyphoned.put(uuid, total);
        
        if (total >= 100000 && !PlayerBuffManager.hasBuff(uuid, PlayerBuffManager.PlayerBuff.SYPHON_MASTER)) {
            PlayerBuffManager.grantBuff(killer, PlayerBuffManager.PlayerBuff.SYPHON_MASTER);
            killer.sendSystemMessage(Component.literal("§6§l✦ ACHIEVEMENT UNLOCKED: SYPHON MASTER BUFF!"), false);
            killer.sendSystemMessage(Component.literal("§7You've syphoned 100,000 coins total! Syphon 15% instead of 10% from victims."), false);
        }
        
        DataManager.save(PoliticalServer.server);
    }

    public static void onBountyKill(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        int count = bountyKills.getOrDefault(uuid, 0) + 1;
        bountyKills.put(uuid, count);
        
        if (count >= 50 && !PlayerBuffManager.hasBuff(uuid, PlayerBuffManager.PlayerBuff.BOUNTY_HUNTER)) {
            PlayerBuffManager.grantBuff(player, PlayerBuffManager.PlayerBuff.BOUNTY_HUNTER);
            player.sendSystemMessage(Component.literal("§4§l✦ ACHIEVEMENT UNLOCKED: BOUNTY HUNTER BUFF!"), false);
            player.sendSystemMessage(Component.literal("§7You've killed 50 players with bounties! +25% bonus coins from bounty kills."), false);
        }
        
        DataManager.save(PoliticalServer.server);
    }

    // ============================================================
    // BANKING PROGRESS METHODS
    // ============================================================

    public static void onBankUpgradePurchase(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        int count = bankUpgradesPurchased.getOrDefault(uuid, 0) + 1;
        bankUpgradesPurchased.put(uuid, count);
        
        if (count >= 5 && !PlayerBuffManager.hasBuff(uuid, PlayerBuffManager.PlayerBuff.BANK_UPGRADER)) {
            PlayerBuffManager.grantBuff(player, PlayerBuffManager.PlayerBuff.BANK_UPGRADER);
            player.sendSystemMessage(Component.literal("§6§l✦ ACHIEVEMENT UNLOCKED: BANK UPGRADER BUFF!"), false);
            player.sendSystemMessage(Component.literal("§7You've purchased 5 bank upgrades! -10% bank upgrade costs."), false);
        }
        
        DataManager.save(PoliticalServer.server);
    }

    public static void checkWealthyBuff(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        long total = DataManager.getBankMain(uuid) + DataManager.getBankSavings(uuid);
        
        if (total >= 10000000 && !PlayerBuffManager.hasBuff(uuid, PlayerBuffManager.PlayerBuff.WEALTHY)) {
            PlayerBuffManager.grantBuff(player, PlayerBuffManager.PlayerBuff.WEALTHY);
            player.sendSystemMessage(Component.literal("§e§l✦ ACHIEVEMENT UNLOCKED: WEALTHY BUFF!"), false);
            player.sendSystemMessage(Component.literal("§7You have 10,000,000 coins in bank! +1% extra interest, bypass 50M savings cap."), false);
        }
        
        DataManager.save(PoliticalServer.server);
    }

    // ============================================================
    // PROGRESS GETTERS (for GUI display)
    // ============================================================

    public static int getConsecutiveElectionsWon(String uuid) {
        return Math.max(consecutiveElectionsWon.getOrDefault(uuid, 0), DataManager.getConsecutiveElectionWins(uuid));
    }

    public static int getLawsPassed(String uuid) {
        return Math.max(lawsPassed.getOrDefault(uuid, 0), DataManager.getLawsPassed(uuid));
    }

    public static int getJudicialRulings(String uuid) {
        return Math.max(judicialRulings.getOrDefault(uuid, 0), DataManager.getJudicialRulings(uuid));
    }

    public static int getBountyQuestsCompleted(String uuid) {
        return bountyQuestsCompleted.getOrDefault(uuid, 0);
    }

    public static int getTier5BossKills(String uuid) {
        return Math.max(tier5BossKills.getOrDefault(uuid, 0), DataManager.getTier5BossKills(uuid));
    }

    public static int getZombieBossKills(String uuid) {
        return Math.max(zombieBossKills.getOrDefault(uuid, 0), DataManager.getZombieBossKills(uuid));
    }

    public static long getTotalCoinsAccumulated(String uuid) {
        return Math.max(totalCoinsAccumulated.getOrDefault(uuid, 0L), DataManager.getTotalCoinsEarned(uuid));
    }

    public static int getTradeCount(String uuid) {
        return StockMarket.getTradeCount(uuid);
    }

    public static long getBankBalance(String uuid) {
        long live = (long) DataManager.getBankMain(uuid) + (long) DataManager.getBankSavings(uuid);
        return Math.max(bankBalance.getOrDefault(uuid, 0L), live);
    }

    public static int getOresMined(String uuid) {
        return Math.max(oresMined.getOrDefault(uuid, 0), DataManager.getOresMined(uuid));
    }

    public static int getUniqueItemsCount(String uuid) {
        Set<String> items = uniqueItemsCollected.get(uuid);
        int setSize = items != null ? items.size() : 0;
        return Math.max(setSize, DataManager.getUniqueItemsCollected(uuid));
    }

    public static int getTotalVotesReceived(String uuid) {
        return Math.max(totalVotesReceived.getOrDefault(uuid, 0), DataManager.getTotalVotesReceived(uuid));
    }

    public static int getConsecutiveRareDrops(String uuid) {
        return consecutiveRareDrops.getOrDefault(uuid, 0);
    }

    public static long getPlaytimeHours(String uuid) {
        return Math.max(totalPlaytimeMinutes.getOrDefault(uuid, 0L) / 60, (long) DataManager.getPlaytimeHours(uuid));
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

    /** Stock-market buys/sells only (not crypto). */
    public static int getStockOnlyTrades(String uuid) {
        return stockTrades.getOrDefault(uuid, 0);
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

    public static void recordCropHarvested(ServerPlayer player) {
        String u = player.getUUID().toString();
        cropsHarvested.merge(u, 1, Integer::sum);
        PlayerBuffManager.checkBuffUnlocks(player, u);
    }

    public static void recordFishCaught(ServerPlayer player) {
        String u = player.getUUID().toString();
        fishCaught.merge(u, 1, Integer::sum);
        PlayerBuffManager.checkBuffUnlocks(player, u);
    }

    public static void recordLogChopped(ServerPlayer player) {
        String u = player.getUUID().toString();
        logsChopped.merge(u, 1, Integer::sum);
        PlayerBuffManager.checkBuffUnlocks(player, u);
    }

    public static void recordAuctionWon(ServerPlayer player) {
        String u = player.getUUID().toString();
        auctionsWon.merge(u, 1, Integer::sum);
        PlayerBuffManager.checkBuffUnlocks(player, u);
    }

    /** Win in the final seconds of a listing (see {@link AuctionHouseManager#LISTING_MAX_DURATION_MS}). */
    public static void recordSnipedAuction(ServerPlayer player) {
        String u = player.getUUID().toString();
        snipedAuctions.merge(u, 1, Integer::sum);
        PlayerBuffManager.checkBuffUnlocks(player, u);
    }

    // Dimension/Warp Buffs
    public static int getWarpUses(String uuid) {
        return warpUses.getOrDefault(uuid, 0);
    }

    public static int getBackroomsEscapes(String uuid) {
        return backroomsEscapes.getOrDefault(uuid, 0);
    }

    public static long getDimensionHours(String uuid) {
        return dimensionTimeMs.getOrDefault(uuid, 0L) / 3600000L;
    }

    public static int getMiningDimensionOres(String uuid) {
        return miningDimensionOres.getOrDefault(uuid, 0);
    }

    // Death/PvP Buffs
    public static int getTotalDeaths(String uuid) {
        return totalDeaths.getOrDefault(uuid, 0);
    }

    public static long getTotalSyphoned(String uuid) {
        return totalSyphoned.getOrDefault(uuid, 0L);
    }

    public static int getBountyKills(String uuid) {
        return bountyKills.getOrDefault(uuid, 0);
    }

    public static long getTotalCoinsLostToDeath(String uuid) {
        return totalCoinsLostToDeath.getOrDefault(uuid, 0L);
    }

    // Banking Buffs
    public static long getTotalBankBalance(String uuid) {
        return DataManager.getBankMain(uuid) + DataManager.getBankSavings(uuid);
    }

    public static int getBankUpgradesPurchased(String uuid) {
        return bankUpgradesPurchased.getOrDefault(uuid, 0);
    }

    public static int getElectionVotesCast(String uuid) {
        return electionVotesCast.getOrDefault(uuid, 0);
    }

    public static long getTotalTaxPaid(String uuid) {
        return totalTaxPaid.getOrDefault(uuid, 0L);
    }

    public static long getTotalPartyDonations(String uuid) {
        return totalPartyDonations.getOrDefault(uuid, 0L);
    }

    public static int getMaxAscensionCollectionTier(String uuid) {
        return AscensionGrindManager.getMaxCollectionTier(uuid);
    }

    public static void recordStockTrade(ServerPlayer player) {
        String u = player.getUUID().toString();
        stockTrades.merge(u, 1, Integer::sum);
        if (stockTrades.getOrDefault(u, 0) >= 200 && !PlayerBuffManager.hasBuff(u, PlayerBuffManager.PlayerBuff.STOCK_BARON)) {
            PlayerBuffManager.grantBuff(player, PlayerBuffManager.PlayerBuff.STOCK_BARON);
        }
        DataManager.save(PoliticalServer.server);
    }

    public static void recordCryptoTrade(ServerPlayer player) {
        String u = player.getUUID().toString();
        cryptoTrades.merge(u, 1, Integer::sum);
        if (cryptoTrades.getOrDefault(u, 0) >= 200 && !PlayerBuffManager.hasBuff(u, PlayerBuffManager.PlayerBuff.CRYPTO_KING)) {
            PlayerBuffManager.grantBuff(player, PlayerBuffManager.PlayerBuff.CRYPTO_KING);
        }
        DataManager.save(PoliticalServer.server);
    }

    public static void onElectionVoteCast(ServerPlayer player) {
        String u = player.getUUID().toString();
        electionVotesCast.merge(u, 1, Integer::sum);
        if (electionVotesCast.getOrDefault(u, 0) >= 50 && !PlayerBuffManager.hasBuff(u, PlayerBuffManager.PlayerBuff.CIVIC_DUTY)) {
            PlayerBuffManager.grantBuff(player, PlayerBuffManager.PlayerBuff.CIVIC_DUTY);
        }
        DataManager.save(PoliticalServer.server);
    }

    public static void onTaxPaid(ServerPlayer player, long amount) {
        if (amount <= 0) return;
        String u = player.getUUID().toString();
        totalTaxPaid.merge(u, amount, Long::sum);
        if (totalTaxPaid.getOrDefault(u, 0L) >= 500_000L && !PlayerBuffManager.hasBuff(u, PlayerBuffManager.PlayerBuff.PUBLIC_PATRON)) {
            PlayerBuffManager.grantBuff(player, PlayerBuffManager.PlayerBuff.PUBLIC_PATRON);
        }
        DataManager.save(PoliticalServer.server);
    }

    public static void onPartyDonation(ServerPlayer player, long amount) {
        if (amount <= 0) return;
        String u = player.getUUID().toString();
        totalPartyDonations.merge(u, amount, Long::sum);
        if (totalPartyDonations.getOrDefault(u, 0L) >= 500_000L && !PlayerBuffManager.hasBuff(u, PlayerBuffManager.PlayerBuff.PARTY_PATRON)) {
            PlayerBuffManager.grantBuff(player, PlayerBuffManager.PlayerBuff.PARTY_PATRON);
        }
        DataManager.save(PoliticalServer.server);
    }

    public static void tryGrantAscensionTitan(ServerPlayer player) {
        String u = player.getUUID().toString();
        if (getMaxAscensionCollectionTier(u) >= 5 && !PlayerBuffManager.hasBuff(u, PlayerBuffManager.PlayerBuff.ASCENSION_TITAN)) {
            PlayerBuffManager.grantBuff(player, PlayerBuffManager.PlayerBuff.ASCENSION_TITAN);
        }
    }

    public static void loadFromData(DataManager.SaveData root) {
        BuffProgressSnapshot snap = root.buffProgressSnapshot;
        if (snap == null) {
            mergeLegacySaveMaps(root);
            return;
        }
        copySnapshotToRuntime(snap);
        mergeLegacySaveMaps(root);
    }

    public static void saveToData(DataManager.SaveData root) {
        BuffProgressSnapshot s = new BuffProgressSnapshot();
        s.bountyQuestsCompleted.putAll(bountyQuestsCompleted);
        s.lawsPassed.putAll(lawsPassed);
        s.judicialRulings.putAll(judicialRulings);
        s.tier5BossKills.putAll(tier5BossKills);
        s.zombieBossKills.putAll(zombieBossKills);
        s.totalCoinsAccumulated.putAll(totalCoinsAccumulated);
        s.bankBalance.putAll(bankBalance);
        s.oresMined.putAll(oresMined);
        for (Map.Entry<String, Set<String>> e : uniqueItemsCollected.entrySet()) {
            s.uniqueItemsCollected.put(e.getKey(), new ArrayList<>(e.getValue()));
        }
        s.totalVotesReceived.putAll(totalVotesReceived);
        s.consecutiveRareDrops.putAll(consecutiveRareDrops);
        s.totalPlaytimeMinutes.putAll(totalPlaytimeMinutes);
        s.consecutiveElectionsWon.putAll(consecutiveElectionsWon);
        s.auctionsWon.putAll(auctionsWon);
        s.snipedAuctions.putAll(snipedAuctions);
        s.wardenBossKills.putAll(wardenBossKills);
        s.enderBossKills.putAll(enderBossKills);
        s.spiderBossKills.putAll(spiderBossKills);
        s.skeletonBossKills.putAll(skeletonBossKills);
        s.prisonEscapes.putAll(prisonEscapes);
        s.playersImprisoned.putAll(playersImprisoned);
        s.impeachmentsSurvived.putAll(impeachmentsSurvived);
        s.termsServed.putAll(termsServed);
        s.cryptoTrades.putAll(cryptoTrades);
        s.stockTrades.putAll(stockTrades);
        s.cropsHarvested.putAll(cropsHarvested);
        s.fishCaught.putAll(fishCaught);
        s.logsChopped.putAll(logsChopped);
        s.warpUses.putAll(warpUses);
        s.backroomsEscapes.putAll(backroomsEscapes);
        s.dimensionTimeMs.putAll(dimensionTimeMs);
        s.miningDimensionOres.putAll(miningDimensionOres);
        s.totalDeaths.putAll(totalDeaths);
        s.totalSyphoned.putAll(totalSyphoned);
        s.bountyKills.putAll(bountyKills);
        s.totalCoinsLostToDeath.putAll(totalCoinsLostToDeath);
        s.bankUpgradesPurchased.putAll(bankUpgradesPurchased);
        s.lastRareDropTime.putAll(lastRareDropTime);
        s.electionVotesCast.putAll(electionVotesCast);
        s.totalTaxPaid.putAll(totalTaxPaid);
        s.totalPartyDonations.putAll(totalPartyDonations);
        root.buffProgressSnapshot = s;
    }

    private static void copySnapshotToRuntime(BuffProgressSnapshot snap) {
        bountyQuestsCompleted.clear();
        bountyQuestsCompleted.putAll(snap.bountyQuestsCompleted);
        lawsPassed.clear();
        lawsPassed.putAll(snap.lawsPassed);
        judicialRulings.clear();
        judicialRulings.putAll(snap.judicialRulings);
        tier5BossKills.clear();
        tier5BossKills.putAll(snap.tier5BossKills);
        zombieBossKills.clear();
        zombieBossKills.putAll(snap.zombieBossKills);
        totalCoinsAccumulated.clear();
        totalCoinsAccumulated.putAll(snap.totalCoinsAccumulated);
        bankBalance.clear();
        bankBalance.putAll(snap.bankBalance);
        oresMined.clear();
        oresMined.putAll(snap.oresMined);
        uniqueItemsCollected.clear();
        if (snap.uniqueItemsCollected != null) {
            for (Map.Entry<String, List<String>> e : snap.uniqueItemsCollected.entrySet()) {
                Set<String> set = ConcurrentHashMap.newKeySet();
                if (e.getValue() != null) {
                    set.addAll(e.getValue());
                }
                uniqueItemsCollected.put(e.getKey(), set);
            }
        }
        totalVotesReceived.clear();
        totalVotesReceived.putAll(snap.totalVotesReceived);
        consecutiveRareDrops.clear();
        consecutiveRareDrops.putAll(snap.consecutiveRareDrops);
        totalPlaytimeMinutes.clear();
        totalPlaytimeMinutes.putAll(snap.totalPlaytimeMinutes);
        consecutiveElectionsWon.clear();
        consecutiveElectionsWon.putAll(snap.consecutiveElectionsWon);
        auctionsWon.clear();
        auctionsWon.putAll(snap.auctionsWon);
        snipedAuctions.clear();
        snipedAuctions.putAll(snap.snipedAuctions);
        wardenBossKills.clear();
        wardenBossKills.putAll(snap.wardenBossKills);
        enderBossKills.clear();
        enderBossKills.putAll(snap.enderBossKills);
        spiderBossKills.clear();
        spiderBossKills.putAll(snap.spiderBossKills);
        skeletonBossKills.clear();
        skeletonBossKills.putAll(snap.skeletonBossKills);
        prisonEscapes.clear();
        prisonEscapes.putAll(snap.prisonEscapes);
        playersImprisoned.clear();
        playersImprisoned.putAll(snap.playersImprisoned);
        impeachmentsSurvived.clear();
        impeachmentsSurvived.putAll(snap.impeachmentsSurvived);
        termsServed.clear();
        termsServed.putAll(snap.termsServed);
        cryptoTrades.clear();
        cryptoTrades.putAll(snap.cryptoTrades);
        stockTrades.clear();
        stockTrades.putAll(snap.stockTrades);
        cropsHarvested.clear();
        cropsHarvested.putAll(snap.cropsHarvested);
        fishCaught.clear();
        fishCaught.putAll(snap.fishCaught);
        logsChopped.clear();
        logsChopped.putAll(snap.logsChopped);
        warpUses.clear();
        warpUses.putAll(snap.warpUses);
        backroomsEscapes.clear();
        backroomsEscapes.putAll(snap.backroomsEscapes);
        dimensionTimeMs.clear();
        dimensionTimeMs.putAll(snap.dimensionTimeMs);
        miningDimensionOres.clear();
        miningDimensionOres.putAll(snap.miningDimensionOres);
        totalDeaths.clear();
        totalDeaths.putAll(snap.totalDeaths);
        totalSyphoned.clear();
        totalSyphoned.putAll(snap.totalSyphoned);
        bountyKills.clear();
        bountyKills.putAll(snap.bountyKills);
        totalCoinsLostToDeath.clear();
        totalCoinsLostToDeath.putAll(snap.totalCoinsLostToDeath);
        bankUpgradesPurchased.clear();
        bankUpgradesPurchased.putAll(snap.bankUpgradesPurchased);
        lastRareDropTime.clear();
        lastRareDropTime.putAll(snap.lastRareDropTime);
        electionVotesCast.clear();
        electionVotesCast.putAll(snap.electionVotesCast);
        totalTaxPaid.clear();
        totalTaxPaid.putAll(snap.totalTaxPaid);
        totalPartyDonations.clear();
        totalPartyDonations.putAll(snap.totalPartyDonations);
    }

    /** Adds this election's tallies to lifetime votes received (POPULAR buff). */
    public static void applyElectionResultsToVoteTotals(MinecraftServer server, Map<String, Integer> tallies) {
        if (tallies == null) return;
        for (Map.Entry<String, Integer> e : tallies.entrySet()) {
            int v = e.getValue();
            if (v <= 0) continue;
            String uuid = e.getKey();
            DataManager.addVotesReceived(uuid, v);
            totalVotesReceived.put(uuid, DataManager.getTotalVotesReceived(uuid));
            if (server != null) {
                ServerPlayer p = server.getPlayerList().getPlayer(java.util.UUID.fromString(uuid));
                if (p != null) {
                    tryGrantPopular(p);
                }
            }
        }
        DataManager.save(PoliticalServer.server);
    }

    private static void mergeLegacySaveMaps(DataManager.SaveData d) {
        if (d.totalCoinsEarned != null) {
            for (Map.Entry<String, Long> e : d.totalCoinsEarned.entrySet()) {
                totalCoinsAccumulated.merge(e.getKey(), e.getValue(), Math::max);
            }
        }
        if (d.playtimeMinutes != null) {
            for (Map.Entry<String, Long> e : d.playtimeMinutes.entrySet()) {
                totalPlaytimeMinutes.merge(e.getKey(), e.getValue(), Math::max);
            }
        }
        if (d.totalVotesReceived != null) {
            for (Map.Entry<String, Integer> e : d.totalVotesReceived.entrySet()) {
                totalVotesReceived.merge(e.getKey(), e.getValue(), Math::max);
            }
        }
        if (d.consecutiveElectionWins != null) {
            for (Map.Entry<String, Integer> e : d.consecutiveElectionWins.entrySet()) {
                consecutiveElectionsWon.merge(e.getKey(), e.getValue(), Math::max);
            }
        }
        if (d.lawsPassed != null) {
            for (Map.Entry<String, Integer> e : d.lawsPassed.entrySet()) {
                lawsPassed.merge(e.getKey(), e.getValue(), Math::max);
            }
        }
        if (d.judicialRulings != null) {
            for (Map.Entry<String, Integer> e : d.judicialRulings.entrySet()) {
                judicialRulings.merge(e.getKey(), e.getValue(), Math::max);
            }
        }
        if (d.tier5BossKills != null) {
            for (Map.Entry<String, Integer> e : d.tier5BossKills.entrySet()) {
                tier5BossKills.merge(e.getKey(), e.getValue(), Math::max);
            }
        }
        if (d.zombieBossKills != null) {
            for (Map.Entry<String, Integer> e : d.zombieBossKills.entrySet()) {
                zombieBossKills.merge(e.getKey(), e.getValue(), Math::max);
            }
        }
        if (d.oresMined != null) {
            for (Map.Entry<String, Integer> e : d.oresMined.entrySet()) {
                oresMined.merge(e.getKey(), e.getValue(), Math::max);
            }
        }
        if (d.consecutiveRareDrops != null) {
            for (Map.Entry<String, Integer> e : d.consecutiveRareDrops.entrySet()) {
                consecutiveRareDrops.merge(e.getKey(), e.getValue(), Math::max);
            }
        }
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
            case STOCK_BARON -> getStockOnlyTrades(uuid) + "/200 stock trades";
            case CIVIC_DUTY -> getElectionVotesCast(uuid) + "/50 votes cast in elections";
            case PUBLIC_PATRON -> String.format("%,d", getTotalTaxPaid(uuid)) + "/500,000 tax paid (credits)";
            case PARTY_PATRON -> String.format("%,d", getTotalPartyDonations(uuid)) + "/500,000 donated to party";
            case ASCENSION_TITAN -> getMaxAscensionCollectionTier(uuid) + "/5 max collection tier";
            // Farming/Resource Buffs
            case FARMER -> getCropsHarvested(uuid) + "/10,000 crops harvested";
            case FISHER -> getFishCaught(uuid) + "/1,000 fish caught";
            case WOODCUTTER -> getLogsChopped(uuid) + "/5,000 logs chopped";
            // Dimension/Warp Buffs
            case DIMENSION_HOPPER -> getWarpUses(uuid) + "/100 warp uses";
            case BACKROOMS_SURVIVOR -> getBackroomsEscapes(uuid) + "/25 backrooms escapes";
            case VOID_WALKER -> getDimensionHours(uuid) + "/10 hours in dimensions";
            case MINING_TYCOON -> getMiningDimensionOres(uuid) + "/100,000 ores in Mining";
            // Death/PvP Buffs
            case DEATH_DEFYING -> getTotalDeaths(uuid) + "/100 deaths";
            case SYPHON_MASTER -> String.format("%,d", getTotalSyphoned(uuid)) + "/100,000 coins syphoned";
            case BOUNTY_HUNTER -> getBountyKills(uuid) + "/50 bounty kills";
            case IRON_WALLET -> String.format("%,d", getTotalCoinsLostToDeath(uuid)) + "/1,000,000,000 coins lost to death";
            // Banking Buffs
            case WEALTHY -> String.format("%,d", getTotalBankBalance(uuid)) + "/10,000,000 in bank";
            case BANK_UPGRADER -> getBankUpgradesPurchased(uuid) + "/5 bank upgrades";
            // Admin-Only Buffs (no progress tracking)
            case GODMODE -> "§c§lADMIN ONLY - Cannot be earned";
            case DEITY -> "§d§lADMIN ONLY - Cannot be earned";
            case TRANSCENDENT -> "§b§lADMIN ONLY - Cannot be earned";
        };
    }
}
