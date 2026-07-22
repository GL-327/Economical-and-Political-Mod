package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CivilCraft Achievements System
 * Custom achievements that reward coins based on difficulty
 * Links to various mod and vanilla features
 */
public class CivilCraftAchievements {

    // Achievement data
    public enum Achievement {
        // Economy (Easy)
        FIRST_COINS("First Steps", "Earn your first 1,000 coins", 25_000, Items.GOLD_NUGGET, "first_coins"),
        COIN_COLLECTOR("Coin Collector", "Earn 10,000,000 total coins", 125_000, Items.GOLD_INGOT, "coin_collector"),
        COIN_MASTER("Coin Master", "Earn 100,000,000 total coins", 500_000, Items.GOLD_BLOCK, "coin_master"),
        COIN_BILLIONAIRE("Billionaire", "Earn 1,000,000,000 total coins", 2_500_000, Items.NETHERITE_INGOT, "coin_billionaire"),

        // Bounty (Easy-Medium)
        BOUNTY_HUNTER("Bounty Hunter", "Complete your first bounty quest", 50_000, Items.NETHERITE_SWORD, "bounty_hunter"),
        BOUNTY_MASTER("Bounty Master", "Complete 10 bounty quests", 250_000, Items.NETHERITE_SWORD, "bounty_master"),
        BOUNTY_LEGEND("Bounty Legend", "Complete 50 bounty quests", 1_000_000, Items.NETHERITE_SWORD, "bounty_legend"),
        BOSS_SLAYER("Boss Slayer", "Defeat a dimension boss", 250_000, Items.WITHER_SKELETON_SKULL, "boss_slayer"),

        // Political (Medium)
        FIRST_VOTE("Citizen", "Cast your first vote", 50_000, Items.PAPER, "first_vote"),
        CANDIDATE("Candidate", "Run for office", 125_000, Items.WRITABLE_BOOK, "candidate"),
        ELECTED("Elected Official", "Win an election", 500_000, Items.GOLD_BLOCK, "elected"),
        PRESIDENT("President", "Become President", 2_500_000, Items.NETHER_STAR, "president"),
        JUDGE_SERVED("Judge Dredd", "Serve as Judge for 1 week", 750_000, Items.STRUCTURE_BLOCK, "judge_served"),

        // Dimension (Medium-Hard)
        DIMENSION_EXPLORER("Explorer", "Visit 5 custom dimensions", 125_000, Items.END_PORTAL_FRAME, "dimension_explorer"),
        DIMENSION_MASTER("Dimension Master", "Visit all custom dimensions", 500_000, Items.END_PORTAL_FRAME, "dimension_master"),
        BACKROOMS_SURVIVOR("Backrooms Survivor", "Escape the Backrooms", 250_000, Items.SPAWNER, "backrooms_survivor"),
        BACKROOMS_MASTER("Backrooms Master", "Escape the Backrooms 10 times", 1_000_000, Items.SPAWNER, "backrooms_master"),

        // Stock Market (Medium)
        STOCK_TRADER("Stock Trader", "Make your first stock trade", 75_000, Items.EMERALD, "stock_trader"),
        STOCK_MASTER("Stock Master", "Make 100 stock trades", 500_000, Items.EMERALD, "stock_master"),
        CRYPTO_MINER("Crypto Miner", "Mine your first crypto", 50_000, Items.RECOVERY_COMPASS, "crypto_miner"),
        CRYPTO_BARON("Crypto Baron", "Mine 100 crypto", 375_000, Items.RECOVERY_COMPASS, "crypto_baron"),

        // Auction (Easy)
        FIRST_AUCTION("Auctioneer", "List your first auction", 25_000, Items.GOLD_NUGGET, "first_auction"),
        AUCTION_REGULAR("Regular Seller", "List 10 auctions", 125_000, Items.GOLD_INGOT, "auction_regular"),
        AUCTION_MOGUL("Auction Mogul", "List 50 auctions", 500_000, Items.GOLD_BLOCK, "auction_mogul"),
        BIG_SPENDER("Big Spender", "Win an auction for 50M+ coins", 250_000, Items.DIAMOND, "big_spender"),

        // Bank (Easy-Medium)
        BANK_USER("Saver", "Deposit into bank", 25_000, Items.ENDER_CHEST, "bank_user"),
        BANK_SAVER("Super Saver", "Have 100M in bank", 250_000, Items.ENDER_CHEST, "bank_saver"),
        BANK_MOGUL("Bank Mogul", "Have 1B in bank", 1_000_000, Items.ENDER_CHEST, "bank_mogul"),

        // Gambling (Easy-Medium)
        LOTTERY_PLAYER("Gambler", "Buy a lottery ticket", 10_000, Items.PAPER, "lottery_player"),
        LOTTERY_WINNER("Lucky Winner", "Win the lottery", 1_000_000, Items.NETHER_STAR, "lottery_winner"),
        CASINO_REGULAR("Casino Regular", "Play 50 casino games", 125_000, Items.PLAYER_HEAD, "casino_regular"),
        JACKPOT("Jackpot!", "Hit the slot machine jackpot", 750_000, Items.DISPENSER, "jackpot"),

        // Combat (Medium-Hard)
        KILLER("Killer", "Kill 100 hostile mobs", 50_000, Items.IRON_SWORD, "killer"),
        WARRIOR("Warrior", "Kill 1,000 hostile mobs", 250_000, Items.DIAMOND_SWORD, "warrior"),
        PVP_MASTER("PvP Master", "Kill 10 players in PvP", 500_000, Items.NETHERITE_SWORD, "pvp_master"),
        DRAGON_SLAYER("Dragon Slayer", "Kill the Ender Dragon", 750_000, Items.DRAGON_HEAD, "dragon_slayer"),
        WITHER_KILLER("Wither Killer", "Kill the Wither", 625_000, Items.WITHER_SKELETON_SKULL, "wither_killer"),

        // Mining (Easy-Medium)
        MINER("Miner", "Mine 1,000 blocks", 25_000, Items.STONE_PICKAXE, "miner"),
        PRO_MINER("Pro Miner", "Mine 100,000 blocks", 250_000, Items.DIAMOND_PICKAXE, "pro_miner"),
        DIAMOND_MINER("Diamond Miner", "Mine 100 diamonds", 125_000, Items.DIAMOND, "diamond_miner"),
        ANCIENT_MINER("Ancient Miner", "Find ancient debris", 250_000, Items.ANCIENT_DEBRIS, "ancient_miner"),

        // Farming (Easy)
        FARMER("Farmer", "Harvest 100 crops", 25_000, Items.WHEAT, "farmer"),
        PRO_FARMER("Pro Farmer", "Harvest 10,000 crops", 250_000, Items.GOLDEN_HOE, "pro_farmer"),
        ANIMAL_FARMER("Animal Farmer", "Breed 50 animals", 75_000, Items.WHEAT_SEEDS, "animal_farmer"),

        // Building (Medium)
        BUILDER("Builder", "Place 1,000 blocks", 25_000, Items.BRICK, "builder"),
        ARCHITECT("Architect", "Place 100,000 blocks", 250_000, Items.QUARTZ_BLOCK, "architect"),
        SKY_BUILDER("Sky Builder", "Build at Y=256", 125_000, Items.SCAFFOLDING, "sky_builder"),

        // Special (Hard)
        ALL_ACHIEVEMENTS("Completionist", "Unlock all achievements", 10_000_000, Items.ENCHANTED_GOLDEN_APPLE, "all_achievements"),
        EARLY_BIRD("Early Bird", "Login within first week of server", 1_000_000, Items.CLOCK, "early_bird"),
        DEDICATED("Dedicated Player", "Play for 100 hours", 500_000, Items.CLOCK, "dedicated"),
        VETERAN("Veteran", "Play for 1,000 hours", 2_500_000, Items.NETHER_STAR, "veteran");

        public final String name;
        public final String description;
        public final long coinReward;
        public final net.minecraft.world.item.Item icon;
        public final String id;

        Achievement(String name, String description, long coinReward, net.minecraft.world.item.Item icon, String id) {
            this.name = name;
            this.description = description;
            this.coinReward = coinReward;
            this.icon = icon;
            this.id = id;
        }
    }

    // Player achievement tracking
    private static final Map<String, Set<String>> playerAchievements = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, Integer>> playerProgress = new ConcurrentHashMap<>();

    // Stats tracking for achievements
    private static final Map<String, Long> totalCoinsEarned = new ConcurrentHashMap<>();
    private static final Map<String, Integer> bountiesCompleted = new ConcurrentHashMap<>();
    private static final Map<String, Integer> votesCast = new ConcurrentHashMap<>();
    private static final Map<String, Integer> stockTrades = new ConcurrentHashMap<>();
    private static final Map<String, Integer> cryptoMined = new ConcurrentHashMap<>();
    private static final Map<String, Integer> auctionsListed = new ConcurrentHashMap<>();
    private static final Map<String, Integer> casinoGamesPlayed = new ConcurrentHashMap<>();
    private static final Map<String, Integer> hostileKills = new ConcurrentHashMap<>();
    private static final Map<String, Integer> pvpKills = new ConcurrentHashMap<>();
    private static final Map<String, Integer> blocksMined = new ConcurrentHashMap<>();
    private static final Map<String, Integer> cropsHarvested = new ConcurrentHashMap<>();
    private static final Map<String, Integer> blocksPlaced = new ConcurrentHashMap<>();
    private static final Map<String, Integer> dimensionsVisited = new ConcurrentHashMap<>();
    private static final Map<String, Integer> backroomsEscapes = new ConcurrentHashMap<>();
    private static final Map<String, Long> playTime = new ConcurrentHashMap<>();

    public static void loadFromNbt(net.minecraft.nbt.CompoundTag tag) {
        if (tag.contains("achievements")) {
            net.minecraft.nbt.CompoundTag achievementsTag = tag.getCompoundOrEmpty("achievements");
            for (String uuid : achievementsTag.keySet()) {
                net.minecraft.nbt.ListTag list = achievementsTag.getListOrEmpty(uuid);
                Set<String> achs = new HashSet<>();
                for (int i = 0; i < list.size(); i++) {
                    achs.add(list.getStringOr(i, ""));
                }
                playerAchievements.put(uuid, achs);
            }
        }
    }

    public static void saveToNbt(net.minecraft.nbt.CompoundTag tag) {
        net.minecraft.nbt.CompoundTag achievementsTag = new net.minecraft.nbt.CompoundTag();
        for (Map.Entry<String, Set<String>> entry : playerAchievements.entrySet()) {
            net.minecraft.nbt.ListTag list = new net.minecraft.nbt.ListTag();
            for (String ach : entry.getValue()) {
                list.add(net.minecraft.nbt.StringTag.valueOf(ach));
            }
            achievementsTag.put(entry.getKey(), list);
        }
        tag.put("achievements", achievementsTag);
    }

    // ── Achievement Checking ──

    public static boolean hasAchievement(String uuid, String achievementId) {
        return playerAchievements.getOrDefault(uuid, Collections.emptySet()).contains(achievementId);
    }

    public static void grantAchievement(ServerPlayer player, Achievement achievement) {
        String uuid = player.getUUID().toString();
        if (hasAchievement(uuid, achievement.id)) return;

        playerAchievements.computeIfAbsent(uuid, k -> new HashSet<>()).add(achievement.id);

        // Give coin reward
        CoinManager.giveCoins(uuid, achievement.coinReward);

        // Broadcast
        player.sendSystemMessage(Component.literal("§6═══════════════════════════════════"), false);
        player.sendSystemMessage(Component.literal("§6§l🏆 ACHIEVEMENT UNLOCKED!"), false);
        player.sendSystemMessage(Component.literal("§e" + achievement.name), false);
        player.sendSystemMessage(Component.literal("§7" + achievement.description), false);
        player.sendSystemMessage(Component.literal("§a+" + String.format("%,d", achievement.coinReward) + " coins!"), false);
        player.sendSystemMessage(Component.literal("§6═══════════════════════════════════"), false);

        // Broadcast to all players for major achievements
        if (achievement.coinReward >= 5_000_000) {
            for (ServerPlayer p : PoliticalServer.server.getPlayerList().getPlayers()) {
                if (!p.getUUID().equals(player.getUUID())) {
                    p.sendSystemMessage(Component.literal("§6🏆 " + player.getName().getString() + " unlocked achievement: §e" + achievement.name), false);
                }
            }
        }

        DataManager.save(PoliticalServer.server);
    }

    // ── Progress Tracking Methods ──

    public static void trackCoinsEarned(ServerPlayer player, long amount) {
        String uuid = player.getUUID().toString();
        long total = totalCoinsEarned.getOrDefault(uuid, 0L) + amount;
        totalCoinsEarned.put(uuid, total);

        if (total >= 1000 && !hasAchievement(uuid, "first_coins")) {
            grantAchievement(player, Achievement.FIRST_COINS);
        }
        if (total >= 10_000_000 && !hasAchievement(uuid, "coin_collector")) {
            grantAchievement(player, Achievement.COIN_COLLECTOR);
        }
        if (total >= 100_000_000 && !hasAchievement(uuid, "coin_master")) {
            grantAchievement(player, Achievement.COIN_MASTER);
        }
        if (total >= 1_000_000_000 && !hasAchievement(uuid, "coin_billionaire")) {
            grantAchievement(player, Achievement.COIN_BILLIONAIRE);
        }
    }

    public static void trackBountyCompleted(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        int count = bountiesCompleted.getOrDefault(uuid, 0) + 1;
        bountiesCompleted.put(uuid, count);

        if (count == 1 && !hasAchievement(uuid, "bounty_hunter")) {
            grantAchievement(player, Achievement.BOUNTY_HUNTER);
        }
        if (count >= 10 && !hasAchievement(uuid, "bounty_master")) {
            grantAchievement(player, Achievement.BOUNTY_MASTER);
        }
        if (count >= 50 && !hasAchievement(uuid, "bounty_legend")) {
            grantAchievement(player, Achievement.BOUNTY_LEGEND);
        }
    }

    public static void trackBossKill(ServerPlayer player) {
        grantAchievement(player, Achievement.BOSS_SLAYER);
    }

    public static void trackVote(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        int count = votesCast.getOrDefault(uuid, 0) + 1;
        votesCast.put(uuid, count);

        if (count == 1 && !hasAchievement(uuid, "first_vote")) {
            grantAchievement(player, Achievement.FIRST_VOTE);
        }
    }

    public static void trackRanForOffice(ServerPlayer player) {
        grantAchievement(player, Achievement.CANDIDATE);
    }

    public static void trackElectionWin(ServerPlayer player) {
        grantAchievement(player, Achievement.ELECTED);
    }

    public static void trackPresident(ServerPlayer player) {
        grantAchievement(player, Achievement.PRESIDENT);
    }

    public static void trackDimensionVisit(ServerPlayer player, int totalDimensionsVisited) {
        String uuid = player.getUUID().toString();
        dimensionsVisited.put(uuid, totalDimensionsVisited);

        if (totalDimensionsVisited >= 5 && !hasAchievement(uuid, "dimension_explorer")) {
            grantAchievement(player, Achievement.DIMENSION_EXPLORER);
        }
        // Check if visited all dimensions (18 custom dimensions)
        if (totalDimensionsVisited >= 18 && !hasAchievement(uuid, "dimension_master")) {
            grantAchievement(player, Achievement.DIMENSION_MASTER);
        }
    }

    public static void trackBackroomsEscape(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        int count = backroomsEscapes.getOrDefault(uuid, 0) + 1;
        backroomsEscapes.put(uuid, count);

        if (count == 1 && !hasAchievement(uuid, "backrooms_survivor")) {
            grantAchievement(player, Achievement.BACKROOMS_SURVIVOR);
        }
        if (count >= 10 && !hasAchievement(uuid, "backrooms_master")) {
            grantAchievement(player, Achievement.BACKROOMS_MASTER);
        }
    }

    public static void trackStockTrade(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        int count = stockTrades.getOrDefault(uuid, 0) + 1;
        stockTrades.put(uuid, count);

        if (count == 1 && !hasAchievement(uuid, "stock_trader")) {
            grantAchievement(player, Achievement.STOCK_TRADER);
        }
        if (count >= 100 && !hasAchievement(uuid, "stock_master")) {
            grantAchievement(player, Achievement.STOCK_MASTER);
        }
    }

    public static void trackCryptoMined(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        int count = cryptoMined.getOrDefault(uuid, 0) + 1;
        cryptoMined.put(uuid, count);

        if (count == 1 && !hasAchievement(uuid, "crypto_miner")) {
            grantAchievement(player, Achievement.CRYPTO_MINER);
        }
        if (count >= 100 && !hasAchievement(uuid, "crypto_baron")) {
            grantAchievement(player, Achievement.CRYPTO_BARON);
        }
    }

    public static void trackAuctionListed(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        int count = auctionsListed.getOrDefault(uuid, 0) + 1;
        auctionsListed.put(uuid, count);

        if (count == 1 && !hasAchievement(uuid, "first_auction")) {
            grantAchievement(player, Achievement.FIRST_AUCTION);
        }
        if (count >= 10 && !hasAchievement(uuid, "auction_regular")) {
            grantAchievement(player, Achievement.AUCTION_REGULAR);
        }
        if (count >= 50 && !hasAchievement(uuid, "auction_mogul")) {
            grantAchievement(player, Achievement.AUCTION_MOGUL);
        }
    }

    public static void trackBigAuctionWin(ServerPlayer player) {
        grantAchievement(player, Achievement.BIG_SPENDER);
    }

    public static void trackBankDeposit(ServerPlayer player, long balance) {
        String uuid = player.getUUID().toString();

        if (!hasAchievement(uuid, "bank_user")) {
            grantAchievement(player, Achievement.BANK_USER);
        }
        if (balance >= 100_000_000 && !hasAchievement(uuid, "bank_saver")) {
            grantAchievement(player, Achievement.BANK_SAVER);
        }
        if (balance >= 1_000_000_000 && !hasAchievement(uuid, "bank_mogul")) {
            grantAchievement(player, Achievement.BANK_MOGUL);
        }
    }

    public static void trackLotteryTicket(ServerPlayer player) {
        grantAchievement(player, Achievement.LOTTERY_PLAYER);
    }

    public static void trackLotteryWin(ServerPlayer player) {
        grantAchievement(player, Achievement.LOTTERY_WINNER);
    }

    public static void trackCasinoGame(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        int count = casinoGamesPlayed.getOrDefault(uuid, 0) + 1;
        casinoGamesPlayed.put(uuid, count);

        if (count >= 50 && !hasAchievement(uuid, "casino_regular")) {
            grantAchievement(player, Achievement.CASINO_REGULAR);
        }
    }

    public static void trackJackpot(ServerPlayer player) {
        grantAchievement(player, Achievement.JACKPOT);
    }

    public static void trackHostileKill(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        int count = hostileKills.getOrDefault(uuid, 0) + 1;
        hostileKills.put(uuid, count);

        // Award skill points for combat (every 5 kills)
        if (count % 5 == 0) {
            ProgressionTreeManager.awardSkillPoints(player, 1, "Combat");
        }

        if (count >= 100 && !hasAchievement(uuid, "killer")) {
            grantAchievement(player, Achievement.KILLER);
        }
        if (count >= 1000 && !hasAchievement(uuid, "warrior")) {
            grantAchievement(player, Achievement.WARRIOR);
        }
    }

    public static void trackPvPKill(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        int count = pvpKills.getOrDefault(uuid, 0) + 1;
        pvpKills.put(uuid, count);

        // Award skill points for PvP (every kill)
        ProgressionTreeManager.awardSkillPoints(player, 2, "PvP");

        if (count >= 10 && !hasAchievement(uuid, "pvp_master")) {
            grantAchievement(player, Achievement.PVP_MASTER);
        }
    }

    public static void trackDragonKill(ServerPlayer player) {
        grantAchievement(player, Achievement.DRAGON_SLAYER);
    }

    public static void trackWitherKill(ServerPlayer player) {
        grantAchievement(player, Achievement.WITHER_KILLER);
    }

    public static void trackBlockMined(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        int count = blocksMined.getOrDefault(uuid, 0) + 1;
        blocksMined.put(uuid, count);

        // Award skill points for mining (every 10 blocks)
        if (count % 10 == 0) {
            ProgressionTreeManager.awardSkillPoints(player, 1, "Mining");
        }

        if (count >= 1000 && !hasAchievement(uuid, "miner")) {
            grantAchievement(player, Achievement.MINER);
        }
        if (count >= 100000 && !hasAchievement(uuid, "pro_miner")) {
            grantAchievement(player, Achievement.PRO_MINER);
        }
    }

    public static void trackDiamondMined(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        int count = playerProgress.computeIfAbsent(uuid, k -> new HashMap<>())
                .getOrDefault("diamonds", 0) + 1;
        playerProgress.get(uuid).put("diamonds", count);

        if (count >= 100 && !hasAchievement(uuid, "diamond_miner")) {
            grantAchievement(player, Achievement.DIAMOND_MINER);
        }
    }

    public static void trackAncientDebris(ServerPlayer player) {
        grantAchievement(player, Achievement.ANCIENT_MINER);
    }

    public static void trackCropHarvest(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        BuffProgressManager.recordCropHarvested(player);
        int count = cropsHarvested.getOrDefault(uuid, 0) + 1;
        cropsHarvested.put(uuid, count);

        // Award skill points for farming (every 5 crops)
        if (count % 5 == 0) {
            ProgressionTreeManager.awardSkillPoints(player, 1, "Farming");
        }

        if (count >= 100 && !hasAchievement(uuid, "farmer")) {
            grantAchievement(player, Achievement.FARMER);
        }
        if (count >= 10000 && !hasAchievement(uuid, "pro_farmer")) {
            grantAchievement(player, Achievement.PRO_FARMER);
        }
    }

    public static void trackBlockPlaced(ServerPlayer player, int y) {
        String uuid = player.getUUID().toString();
        int count = blocksPlaced.getOrDefault(uuid, 0) + 1;
        blocksPlaced.put(uuid, count);

        if (count >= 1000 && !hasAchievement(uuid, "builder")) {
            grantAchievement(player, Achievement.BUILDER);
        }
        if (count >= 100000 && !hasAchievement(uuid, "architect")) {
            grantAchievement(player, Achievement.ARCHITECT);
        }
        if (y >= 256 && !hasAchievement(uuid, "sky_builder")) {
            grantAchievement(player, Achievement.SKY_BUILDER);
        }
    }

    public static void trackPlayTime(ServerPlayer player, long hours) {
        String uuid = player.getUUID().toString();
        playTime.put(uuid, hours);

        if (hours >= 100 && !hasAchievement(uuid, "dedicated")) {
            grantAchievement(player, Achievement.DEDICATED);
        }
        if (hours >= 1000 && !hasAchievement(uuid, "veteran")) {
            grantAchievement(player, Achievement.VETERAN);
        }
    }

    // ── GUI ──

    public static void openAchievementsGui(ServerPlayer player) {
        openAchievementsGui(player, 0);
    }

    public static void openAchievementsGui(ServerPlayer player, int page) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("§6§l🏆 CivilCraft Achievements"));

        String uuid = player.getUUID().toString();
        Set<String> unlocked = playerAchievements.getOrDefault(uuid, Collections.emptySet());

        Achievement[] achievements = Achievement.values();
        int totalPages = (achievements.length + 44) / 45;
        int startIndex = page * 45;

        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE).setName(Component.literal("")).build());
        }

        int slot = 0;
        for (int i = startIndex; i < Math.min(startIndex + 45, achievements.length); i++) {
            Achievement ach = achievements[i];
            boolean hasIt = unlocked.contains(ach.id);

            GuiElementBuilder builder = new GuiElementBuilder(hasIt ? Items.LIME_STAINED_GLASS_PANE : Items.RED_STAINED_GLASS_PANE)
                    .setName(Component.literal((hasIt ? "§a✓ " : "§c✗ ") + ach.name))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§7" + ach.description))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§6Reward: " + String.format("%,d", ach.coinReward) + " coins"))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal(hasIt ? "§aUNLOCKED!" : "§cLocked"));

            gui.setSlot(slot, builder.build());
            slot++;
        }

        // Navigation
        if (page > 0) {
            gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("§e← Previous Page"))
                    .setCallback((index, type, containerInput, slotGui) -> openAchievementsGui(player, page - 1))
                    .build());
        }

        if (page < totalPages - 1) {
            gui.setSlot(53, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("§eNext Page →"))
                    .setCallback((index, type, containerInput, slotGui) -> openAchievementsGui(player, page + 1))
                    .build());
        }

        // Stats
        gui.setSlot(49, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Component.literal("§6Your Progress"))
                .addLoreLine(Component.literal("§7Unlocked: §a" + unlocked.size() + "/" + achievements.length))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Coins Earned: §6" + String.format("%,d", totalCoinsEarned.getOrDefault(uuid, 0L))))
                .addLoreLine(Component.literal("§7Bounties: §c" + bountiesCompleted.getOrDefault(uuid, 0)))
                .addLoreLine(Component.literal("§7Stock Trades: §a" + stockTrades.getOrDefault(uuid, 0)))
                .build());

        gui.open();
    }

    public static int getAchievementCount(String uuid) {
        return playerAchievements.getOrDefault(uuid, Collections.emptySet()).size();
    }

    public static int getTotalAchievements() {
        return Achievement.values().length;
    }
}
