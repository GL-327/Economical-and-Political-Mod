package com.political.economy;
import com.political.BuffProgressManager;
import com.political.PlayerBuffManager;
import com.political.PoliticalServer;


import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Full stock market system with dynamic prices, player portfolios, and market events.
 */
public class StockMarket {

    // ═══════════════════════════════════════════════════════════════
    // STOCK DEFINITIONS
    // ═══════════════════════════════════════════════════════════════

    public enum Stock {
        // Technology Sector
        REDSTONE_TECH("RED", "Redstone Technologies", 150.0, 0.15, "⚙"),
        COMPUTING_CORP("CMP", "Computing Corp", 280.0, 0.12, "💻"),
        AUTOMATION_INC("AUTO", "Automation Inc", 95.0, 0.18, "🔧"),
        
        // Mining Sector
        DEEP_MINE_CO("MINE", "DeepMine Co", 220.0, 0.20, "⛏"),
        DIAMOND_EXTRACT("DIA", "Diamond Extract Ltd", 450.0, 0.25, "💎"),
        NETHER_DRILLING("NDR", "Nether Drilling", 180.0, 0.22, "🔥"),
        
        // Agriculture Sector
        GOLDEN_FARMS("FARM", "Golden Farms", 75.0, 0.10, "🌾"),
        ENCHANTED_CROPS("ECRP", "Enchanted Crops", 120.0, 0.14, "✨"),
        MOCHI_FOODS("MOCH", "Mochi Foods", 65.0, 0.08, "🍚"),
        
        // Combat/Defense Sector
        BOUNTY_ARMS("BNT", "Bounty Arms", 320.0, 0.16, "⚔"),
        SLAYER_INDUSTRIES("SLY", "Slayer Industries", 550.0, 0.20, "🗡"),
        ARMOR_WORKS("ARM", "Armor Works", 190.0, 0.12, "🛡"),
        
        // Magic/Enchanting Sector
        ENCHANT_UNLIMITED("ENC", "Enchant Unlimited", 380.0, 0.18, "📖"),
        POTION_MASTERS("POT", "Potion Masters", 145.0, 0.15, "🧪"),
        SCULK_DYNAMICS("SCL", "Sculk Dynamics", 420.0, 0.28, "👁"),
        
        // Trading/Commerce Sector
        VILLAGER_TRADE("VTR", "Villager Trade Co", 88.0, 0.09, "🛒"),
        EMERALD_BANK("EBK", "Emerald Bank", 260.0, 0.11, "💰"),
        AUCTION_HOUSE("AUC", "Auction House", 175.0, 0.14, "🔨"),
        
        // Transportation Sector
        RAIL_NETWORK("RAIL", "Rail Network", 110.0, 0.07, "🚂"),
        ELYTRA_AIR("ELY", "Elytra Air", 340.0, 0.19, "🪂"),
        NETHER_PORT("NPT", "Nether Port Authority", 95.0, 0.10, "🚪"),
        
        // Special/Volatile Stocks
        DRAGON_HOLDINGS("DRG", "Dragon Holdings", 890.0, 0.35, "🐉"),
        WITHER_VENTURES("WTH", "Wither Ventures", 720.0, 0.32, "💀"),
        ENDER_INVESTMENTS("END", "Ender Investments", 650.0, 0.30, "🌌");

        public final String symbol;
        public final String name;
        public final double basePrice;
        public final double volatility; // 0.0 to 1.0
        public final String icon;

        Stock(String symbol, String name, double basePrice, double volatility, String icon) {
            this.symbol = symbol;
            this.name = name;
            this.basePrice = basePrice;
            this.volatility = volatility;
            this.icon = icon;
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // MARKET STATE
    // ═══════════════════════════════════════════════════════════════

    // Current prices (symbol -> price)
    private static final Map<String, Double> currentPrices = new ConcurrentHashMap<>();
    
    // Price history (symbol -> list of prices, most recent last)
    private static final Map<String, LinkedList<Double>> priceHistory = new ConcurrentHashMap<>();
    private static final int MAX_HISTORY_SIZE = 50;
    
    // Player portfolios (player UUID -> (symbol -> shares))
    private static final Map<String, Map<String, Integer>> playerPortfolios = new ConcurrentHashMap<>();
    
    // Market events
    private static final List<MarketEvent> activeEvents = new ArrayList<>();
    
    // Last update tick
    private static long lastUpdateTick = 0;
    private static final long UPDATE_INTERVAL = 1200; // Every 1 minute (20 ticks/sec * 60)
    
    /**
     * Get remaining ticks until next price update
     */
    public static long getTicksUntilUpdate(long currentTick) {
        long elapsed = currentTick - lastUpdateTick;
        return Math.max(0, UPDATE_INTERVAL - elapsed);
    }
    
    /**
     * Get formatted countdown string (e.g., "0:45")
     */
    public static String getCountdownString(long currentTick) {
        long remaining = getTicksUntilUpdate(currentTick);
        long seconds = remaining / 20; // 20 ticks per second
        long mins = seconds / 60;
        long secs = seconds % 60;
        return String.format("%d:%02d", mins, secs);
    }
    
    // Market state
    private static boolean marketOpen = true;
    private static MarketTrend currentTrend = MarketTrend.NEUTRAL;
    
    // Trade count tracking for INVESTOR buff
    private static final Map<String, Integer> playerTradeCounts = new ConcurrentHashMap<>();
    
    // Player trading impact tracking
    private static final Map<String, Integer> recentBuyPressure = new ConcurrentHashMap<>(); // symbol -> net buy shares
    
    public static void incrementTradeCount(String playerUuid) {
        playerTradeCounts.merge(playerUuid, 1, Integer::sum);
    }
    
    public static int getTradeCount(String playerUuid) {
        return playerTradeCounts.getOrDefault(playerUuid, 0);
    }
    
    public enum MarketTrend {
        BULL("📈 Bull Market", ChatFormatting.GREEN),
        BEAR("📉 Bear Market", ChatFormatting.RED),
        NEUTRAL("📊 Stable Market", ChatFormatting.GRAY);
        
        public final String name;
        public final ChatFormatting color;
        MarketTrend(String name, ChatFormatting color) {
            this.name = name;
            this.color = color;
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // INITIALIZATION
    // ═══════════════════════════════════════════════════════════════

    static {
        initializeMarket();
    }

    public static void initializeMarket() {
        for (Stock stock : Stock.values()) {
            currentPrices.put(stock.symbol, stock.basePrice);
            priceHistory.put(stock.symbol, new LinkedList<>());
            priceHistory.get(stock.symbol).add(stock.basePrice);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // MARKET UPDATE LOGIC
    // ═══════════════════════════════════════════════════════════════

    public static void tick(MinecraftServer server) {
        long currentTick = server.getTickCount();
        
        if (currentTick - lastUpdateTick >= UPDATE_INTERVAL) {
            lastUpdateTick = currentTick;
            updateMarket();
            processLimitOrders();
        }
        
        // Process dividends weekly
        processDividends();
    }

    private static void updateMarket() {
        Random random = new Random();
        
        // Determine market trend shift (10% chance to change)
        if (random.nextDouble() < 0.10) {
            currentTrend = switch (random.nextInt(3)) {
                case 0 -> MarketTrend.BULL;
                case 1 -> MarketTrend.BEAR;
                default -> MarketTrend.NEUTRAL;
            };
        }
        
        // Process active events (remove expired ones)
        activeEvents.removeIf(event -> event.durationTicks <= 0);
        
        // Update each stock price
        for (Stock stock : Stock.values()) {
            double oldPrice = currentPrices.get(stock.symbol);
            double newPrice = calculateNewPrice(stock, oldPrice, random);
            
            // Ensure price doesn't go below 1.0
            newPrice = Math.max(1.0, newPrice);
            
            currentPrices.put(stock.symbol, newPrice);
            
            // Update history
            LinkedList<Double> history = priceHistory.get(stock.symbol);
            history.addLast(newPrice);
            if (history.size() > MAX_HISTORY_SIZE) {
                history.removeFirst();
            }
        }
        
        // Decay event durations
        for (MarketEvent event : activeEvents) {
            event.durationTicks -= UPDATE_INTERVAL;
        }
        
        // Decay player buy pressure over time
        for (Map.Entry<String, Integer> entry : recentBuyPressure.entrySet()) {
            int decayed = (int) (entry.getValue() * 0.7); // 30% decay per update
            if (Math.abs(decayed) < 10) {
                recentBuyPressure.remove(entry.getKey());
            } else {
                recentBuyPressure.put(entry.getKey(), decayed);
            }
        }
    }

    private static double calculateNewPrice(Stock stock, double currentPrice, Random random) {
        // Base volatility movement (scaled for 1-min intervals - ~1/4.5 of 20-min volatility)
        double baseChange = random.nextGaussian() * stock.volatility * currentPrice * 0.011;
        
        // Apply market trend modifier (scaled for 1-min intervals)
        double trendModifier = switch (currentTrend) {
            case BULL -> 0.0044;
            case BEAR -> -0.0044;
            case NEUTRAL -> 0.0;
        };
        
        // Apply active events
        double eventModifier = 0.0;
        for (MarketEvent event : activeEvents) {
            if (event.affectedStocks.contains(stock.symbol)) {
                eventModifier += event.priceModifier;
            }
        }
        
        // Player trading impact - buys push price up, sells push down
        double playerImpact = 0.0;
        Integer buyPressure = recentBuyPressure.get(stock.symbol);
        if (buyPressure != null && buyPressure != 0) {
            // Scale impact by trade volume relative to typical volume
            // Assume typical volume of 1000 shares per update cycle
            double relativeVolume = Math.abs(buyPressure) / 1000.0;
            // Cap impact at 3% per update cycle for realistic movement
            playerImpact = Math.signum(buyPressure) * Math.min(relativeVolume * currentPrice * 0.01, currentPrice * 0.03);
        }
        
        // Calculate new price
        double trendChange = currentPrice * trendModifier;
        double newPrice = currentPrice + baseChange + trendChange + (currentPrice * eventModifier) + playerImpact;
        
        // Add some mean reversion (stocks tend to return toward base price)
        double reversionFactor = 0.01;
        double reversion = (stock.basePrice - currentPrice) * reversionFactor;
        newPrice += reversion;
        
        return newPrice;
    }

    // ═══════════════════════════════════════════════════════════════
    // MARKET EVENTS
    // ═══════════════════════════════════════════════════════════════

    public static class MarketEvent {
        public final String name;
        public final String description;
        public final List<String> affectedStocks;
        public final double priceModifier; // -0.5 to 0.5 (percentage)
        public long durationTicks;
        
        public MarketEvent(String name, String description, List<String> affectedStocks, 
                          double priceModifier, long durationTicks) {
            this.name = name;
            this.description = description;
            this.affectedStocks = affectedStocks;
            this.priceModifier = priceModifier;
            this.durationTicks = durationTicks;
        }
    }

    public static void triggerRandomEvent() {
        Random random = new Random();
        Stock[] allStocks = Stock.values();
        
        String eventName;
        String eventDesc;
        List<String> affectedStocks = new ArrayList<>();
        double modifier;
        
        int eventType = random.nextInt(8);
        switch (eventType) {
            case 0 -> {
                eventName = "Mining Boom";
                eventDesc = "Diamond prices surge!";
                affectedStocks.add(Stock.DIAMOND_EXTRACT.symbol);
                affectedStocks.add(Stock.DEEP_MINE_CO.symbol);
                modifier = 0.15;
            }
            case 1 -> {
                eventName = "Dragon Sighting";
                eventDesc = "Dragon-related stocks soar!";
                affectedStocks.add(Stock.DRAGON_HOLDINGS.symbol);
                modifier = 0.25;
            }
            case 2 -> {
                eventName = "Cave-In Disaster";
                eventDesc = "Mining stocks plummet!";
                affectedStocks.add(Stock.DEEP_MINE_CO.symbol);
                affectedStocks.add(Stock.NETHER_DRILLING.symbol);
                modifier = -0.20;
            }
            case 3 -> {
                eventName = "Tech Breakthrough";
                eventDesc = "Redstone technology advances!";
                affectedStocks.add(Stock.REDSTONE_TECH.symbol);
                affectedStocks.add(Stock.COMPUTING_CORP.symbol);
                affectedStocks.add(Stock.AUTOMATION_INC.symbol);
                modifier = 0.12;
            }
            case 4 -> {
                eventName = "Slayer Tournament";
                eventDesc = "Combat stocks rally!";
                affectedStocks.add(Stock.BOUNTY_ARMS.symbol);
                affectedStocks.add(Stock.SLAYER_INDUSTRIES.symbol);
                affectedStocks.add(Stock.ARMOR_WORKS.symbol);
                modifier = 0.18;
            }
            case 5 -> {
                eventName = "Enchantment Discovery";
                eventDesc = "New enchantments found!";
                affectedStocks.add(Stock.ENCHANT_UNLIMITED.symbol);
                affectedStocks.add(Stock.POTION_MASTERS.symbol);
                modifier = 0.14;
            }
            case 6 -> {
                eventName = "Nether Instability";
                eventDesc = "Nether operations at risk!";
                affectedStocks.add(Stock.NETHER_DRILLING.symbol);
                affectedStocks.add(Stock.NETHER_PORT.symbol);
                modifier = -0.15;
            }
            default -> {
                eventName = "Market Panic";
                eventDesc = "Investors flee to safety!";
                for (int i = 0; i < 3; i++) {
                    affectedStocks.add(allStocks[random.nextInt(allStocks.length)].symbol);
                }
                modifier = -0.10;
            }
        }
        
        MarketEvent event = new MarketEvent(eventName, eventDesc, affectedStocks, 
                                           modifier, 120000); // 10 minutes
        activeEvents.add(event);
    }

    // ═══════════════════════════════════════════════════════════════
    // TRADING OPERATIONS
    // ═══════════════════════════════════════════════════════════════

    /** Total coins for a buy including fee (matches {@link #buyStock}). */
    public static long estimateBuyCoinCost(String playerUuid, String symbol, int shares) {
        Double price = currentPrices.get(symbol);
        if (price == null) return 0;
        long totalCost = (long) (price * shares);
        double feeRate = 0.05 * (1.0 - PlayerBuffManager.getMarketFeeReduction(playerUuid));
        long fee = (long) Math.ceil(totalCost * feeRate);
        return totalCost + fee;
    }

    public static boolean buyStock(ServerPlayer player, String symbol, int shares) {
        if (!marketOpen) {
            player.sendSystemMessage(Component.literal("📉 Market is closed!").withStyle(ChatFormatting.RED), false);
            return false;
        }
        
        Double price = currentPrices.get(symbol);
        if (price == null) {
            player.sendSystemMessage(Component.literal("Invalid stock symbol!").withStyle(ChatFormatting.RED), false);
            return false;
        }
        
        long totalCost = (long) (price * shares);
        String uuid = player.getUUID().toString();
        
        // Apply transaction fee (5% base, reduced by INVESTOR buff)
        double feeRate = 0.05 * (1.0 - PlayerBuffManager.getMarketFeeReduction(uuid));
        long fee = (long) Math.ceil(totalCost * feeRate);
        long totalWithFee = totalCost + fee;
        
        // Check if player has enough coins
        int costInt = (int) Math.min(totalWithFee, Integer.MAX_VALUE);
        if (!CoinManager.hasCoins(player, costInt)) {
            player.sendSystemMessage(Component.literal("Insufficient funds! Need " + formatPrice(totalWithFee) + " coins (incl. " + formatPrice(fee) + " fee).")
                    .withStyle(ChatFormatting.RED), false);
            return false;
        }
        
        // Deduct coins
        CoinManager.removeCoins(player, costInt);
        
        // Track trade count for INVESTOR buff
        incrementTradeCount(uuid);
        BuffProgressManager.recordStockTrade(player);
        
        // Add shares to portfolio (+bonus for Stock Baron)
        int bonusShares = PlayerBuffManager.getStockBonusShares(uuid, shares);
        Map<String, Integer> portfolio = playerPortfolios.computeIfAbsent(uuid, k -> new HashMap<>());
        portfolio.merge(symbol, shares + bonusShares, Integer::sum);
        
        // Track buy pressure for price impact
        int pressure = recentBuyPressure.getOrDefault(symbol, 0);
        recentBuyPressure.put(symbol, pressure + shares);
        sharesTraded.merge(symbol, (long) shares, Long::sum);
        
        int totalGranted = shares + bonusShares;
        String bonusNote = bonusShares > 0 ? " (+" + bonusShares + " Stock Baron)" : "";
        player.sendSystemMessage(Component.literal("✅ Bought " + totalGranted + " shares of " + symbol + bonusNote + " for " + formatPrice(totalCost) + " coins (fee: " + formatPrice(fee) + ")!")
                .withStyle(ChatFormatting.GREEN), false);
        
        return true;
    }

    public static boolean sellStock(ServerPlayer player, String symbol, int shares) {
        if (!marketOpen) {
            player.sendSystemMessage(Component.literal("📉 Market is closed!").withStyle(ChatFormatting.RED), false);
            return false;
        }
        
        Double price = currentPrices.get(symbol);
        if (price == null) {
            player.sendSystemMessage(Component.literal("Invalid stock symbol!").withStyle(ChatFormatting.RED), false);
            return false;
        }
        
        String uuid = player.getUUID().toString();
        Map<String, Integer> portfolio = playerPortfolios.get(uuid);
        
        if (portfolio == null || portfolio.getOrDefault(symbol, 0) < shares) {
            player.sendSystemMessage(Component.literal("You don't have enough shares to sell!")
                    .withStyle(ChatFormatting.RED), false);
            return false;
        }
        
        // Calculate proceeds
        long totalValue = (long) (price * shares);
        
        // Apply transaction fee (5% base, reduced by INVESTOR buff)
        double feeRate = 0.05 * (1.0 - PlayerBuffManager.getMarketFeeReduction(uuid));
        long fee = (long) Math.ceil(totalValue * feeRate);
        long netValue = totalValue - fee;
        int valueInt = (int) Math.min(netValue, Integer.MAX_VALUE);
        
        // Remove shares
        portfolio.merge(symbol, -shares, Integer::sum);
        if (portfolio.get(symbol) <= 0) {
            portfolio.remove(symbol);
        }
        
        // Track sell pressure for price impact (negative = selling)
        int pressure = recentBuyPressure.getOrDefault(symbol, 0);
        recentBuyPressure.put(symbol, pressure - shares);
        sharesTraded.merge(symbol, (long) shares, Long::sum);
        
        // Track trade count for INVESTOR buff
        incrementTradeCount(uuid);
        BuffProgressManager.recordStockTrade(player);
        
        // Give coins
        CoinManager.giveCoins(player, valueInt);
        
        player.sendSystemMessage(Component.literal("✅ Sold " + shares + " shares of " + symbol + " for " + formatPrice(netValue) + " coins (fee: " + formatPrice(fee) + ")!")
                .withStyle(ChatFormatting.GREEN), false);
        
        return true;
    }

    // ═══════════════════════════════════════════════════════════════
    // GETTERS
    // ═══════════════════════════════════════════════════════════════

    public static double getPrice(String symbol) {
        return currentPrices.getOrDefault(symbol, 0.0);
    }

    public static List<Double> getPriceHistory(String symbol) {
        return new ArrayList<>(priceHistory.getOrDefault(symbol, new LinkedList<>()));
    }

    public static Map<String, Integer> getPortfolio(String playerUuid) {
        return new HashMap<>(playerPortfolios.getOrDefault(playerUuid, new HashMap<>()));
    }

    public static long getPortfolioValue(String playerUuid) {
        Map<String, Integer> portfolio = playerPortfolios.get(playerUuid);
        if (portfolio == null || portfolio.isEmpty()) return 0;
        
        long total = 0;
        for (Map.Entry<String, Integer> entry : portfolio.entrySet()) {
            double price = currentPrices.getOrDefault(entry.getKey(), 0.0);
            total += (long) (price * entry.getValue());
        }
        return total;
    }

    public static boolean isMarketOpen() {
        return marketOpen;
    }

    public static void toggleMarket() {
        marketOpen = !marketOpen;
    }

    public static void setPrice(String symbol, double price) {
        if (price > 0 && currentPrices.containsKey(symbol)) {
            currentPrices.put(symbol, price);
            LinkedList<Double> history = priceHistory.get(symbol);
            if (history != null) {
                history.addLast(price);
                if (history.size() > MAX_HISTORY_SIZE) {
                    history.removeFirst();
                }
            }
        }
    }

    public static MarketTrend getCurrentTrend() {
        return currentTrend;
    }

    public static List<MarketEvent> getActiveEvents() {
        return new ArrayList<>(activeEvents);
    }

    public static Stock getStockBySymbol(String symbol) {
        for (Stock stock : Stock.values()) {
            if (stock.symbol.equals(symbol)) {
                return stock;
            }
        }
        return null;
    }

    // ═══════════════════════════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════════════════════════

    public static String formatPrice(double price) {
        if (price >= 1000000) {
            return String.format("%.2fM", price / 1000000);
        } else if (price >= 1000) {
            return String.format("%.2fK", price / 1000);
        } else {
            return String.format("%.2f", price);
        }
    }

    public static double calculateChange(String symbol) {
        LinkedList<Double> history = priceHistory.get(symbol);
        if (history == null || history.size() < 2) return 0.0;
        
        double current = history.getLast();
        double previous = history.get(history.size() - 2);
        return ((current - previous) / previous) * 100;
    }

    public static double calculateTotalChange(String symbol) {
        LinkedList<Double> history = priceHistory.get(symbol);
        if (history == null || history.isEmpty()) return 0.0;
        
        Stock stock = getStockBySymbol(symbol);
        if (stock == null) return 0.0;
        
        double current = history.getLast();
        double base = stock.basePrice;
        return ((current - base) / base) * 100;
    }

    // ═══════════════════════════════════════════════════════════════
    // ADVANCED FEATURES: SHORT SELLING
    // ═══════════════════════════════════════════════════════════════

    // Short positions (player UUID -> (symbol -> shares shorted))
    private static final Map<String, Map<String, Integer>> shortPositions = new ConcurrentHashMap<>();
    // Short entry prices (player UUID -> (symbol -> entry price))
    private static final Map<String, Map<String, Double>> shortEntryPrices = new ConcurrentHashMap<>();

    public static boolean shortStock(ServerPlayer player, String symbol, int shares) {
        if (!marketOpen) {
            player.sendSystemMessage(Component.literal("📉 Market is closed!").withStyle(ChatFormatting.RED), false);
            return false;
        }
        
        Stock stock = getStockBySymbol(symbol);
        if (stock == null) {
            player.sendSystemMessage(Component.literal("Invalid stock symbol!").withStyle(ChatFormatting.RED), false);
            return false;
        }
        
        double price = currentPrices.get(symbol);
        String uuid = player.getUUID().toString();
        
        // Short selling requires margin (50% of position value)
        long marginRequired = (long) (price * shares * 0.5);
        if (!CoinManager.hasCoins(player, (int) Math.min(marginRequired, Integer.MAX_VALUE))) {
            player.sendSystemMessage(Component.literal("Insufficient margin! Need " + formatPrice(marginRequired) + " coins for margin.")
                    .withStyle(ChatFormatting.RED), false);
            return false;
        }
        
        // Lock margin
        CoinManager.removeCoins(player, (int) Math.min(marginRequired, Integer.MAX_VALUE));
        
        // Record short position
        Map<String, Integer> positions = shortPositions.computeIfAbsent(uuid, k -> new HashMap<>());
        Map<String, Double> entries = shortEntryPrices.computeIfAbsent(uuid, k -> new HashMap<>());
        
        positions.merge(symbol, shares, Integer::sum);
        entries.put(symbol, price); // Track average entry price
        
        player.sendSystemMessage(Component.literal("📉 Shorted " + shares + " shares of " + symbol + " at " + formatPrice(price) + 
                " (Margin locked: " + formatPrice(marginRequired) + ")").withStyle(ChatFormatting.LIGHT_PURPLE), false);
        return true;
    }

    public static boolean coverShort(ServerPlayer player, String symbol, int shares) {
        if (!marketOpen) {
            player.sendSystemMessage(Component.literal("📉 Market is closed!").withStyle(ChatFormatting.RED), false);
            return false;
        }
        
        String uuid = player.getUUID().toString();
        Map<String, Integer> positions = shortPositions.get(uuid);
        
        if (positions == null || positions.getOrDefault(symbol, 0) < shares) {
            player.sendSystemMessage(Component.literal("You don't have enough short positions to cover!")
                    .withStyle(ChatFormatting.RED), false);
            return false;
        }
        
        double currentPrice = currentPrices.get(symbol);
        double entryPrice = shortEntryPrices.get(uuid).get(symbol);
        
        // Calculate P&L: profit if price went down
        long pnl = (long) ((entryPrice - currentPrice) * shares);
        long marginReturn = (long) (entryPrice * shares * 0.5);
        
        // Update position
        positions.merge(symbol, -shares, Integer::sum);
        if (positions.get(symbol) <= 0) {
            positions.remove(symbol);
            shortEntryPrices.get(uuid).remove(symbol);
        }
        
        // Return margin +/- P&L
        long totalReturn = marginReturn + pnl;
        CoinManager.giveCoins(player, (int) Math.min(Math.max(totalReturn, 0), Integer.MAX_VALUE));
        
        if (pnl >= 0) {
            player.sendSystemMessage(Component.literal("✅ Covered " + shares + " shorts of " + symbol + " for profit of " + 
                    formatPrice(pnl) + " coins!").withStyle(ChatFormatting.GREEN), false);
        } else {
            player.sendSystemMessage(Component.literal("⚠ Covered " + shares + " shorts of " + symbol + " for loss of " + 
                    formatPrice(-pnl) + " coins.").withStyle(ChatFormatting.RED), false);
        }
        return true;
    }

    public static int getShortPosition(String playerUuid, String symbol) {
        Map<String, Integer> positions = shortPositions.get(playerUuid);
        return positions != null ? positions.getOrDefault(symbol, 0) : 0;
    }

    public static long getShortPositionValue(String playerUuid) {
        Map<String, Integer> positions = shortPositions.get(playerUuid);
        if (positions == null) return 0;
        
        long total = 0;
        for (Map.Entry<String, Integer> entry : positions.entrySet()) {
            double price = currentPrices.getOrDefault(entry.getKey(), 0.0);
            total += (long) (price * entry.getValue());
        }
        return total;
    }

    // ═══════════════════════════════════════════════════════════════
    // ADVANCED FEATURES: OPTIONS TRADING
    // ═══════════════════════════════════════════════════════════════

    public enum OptionType { CALL, PUT }
    
    public static class OptionContract {
        public final String symbol;
        public final OptionType type;
        public final double strikePrice;
        public final long expiryTick;
        public final int shares;
        public final String owner;
        public final double premium;
        
        public OptionContract(String symbol, OptionType type, double strikePrice, long expiryTick, 
                             int shares, String owner, double premium) {
            this.symbol = symbol;
            this.type = type;
            this.strikePrice = strikePrice;
            this.expiryTick = expiryTick;
            this.shares = shares;
            this.owner = owner;
            this.premium = premium;
        }
        
        public boolean isExpired(long currentTick) {
            return currentTick >= expiryTick;
        }
        
        public boolean isInTheMoney(double currentPrice) {
            if (type == OptionType.CALL) {
                return currentPrice > strikePrice;
            } else {
                return currentPrice < strikePrice;
            }
        }
    }

    private static final List<OptionContract> activeOptions = new ArrayList<>();

    public static boolean buyOption(ServerPlayer player, String symbol, OptionType type, 
                                    double strikePrice, int shares, long durationTicks) {
        if (!marketOpen) {
            player.sendSystemMessage(Component.literal("📉 Market is closed!").withStyle(ChatFormatting.RED), false);
            return false;
        }
        
        Stock stock = getStockBySymbol(symbol);
        if (stock == null) {
            player.sendSystemMessage(Component.literal("Invalid stock symbol!").withStyle(ChatFormatting.RED), false);
            return false;
        }
        
        // Calculate premium using Black-Scholes approximation
        double currentPrice = currentPrices.get(symbol);
        double premium = calculateOptionPremium(currentPrice, strikePrice, stock.volatility, durationTicks);
        long totalCost = (long) (premium * shares);
        
        if (!CoinManager.hasCoins(player, (int) Math.min(totalCost, Integer.MAX_VALUE))) {
            player.sendSystemMessage(Component.literal("Insufficient funds! Option premium: " + formatPrice(totalCost) + " coins.")
                    .withStyle(ChatFormatting.RED), false);
            return false;
        }
        
        CoinManager.removeCoins(player, (int) Math.min(totalCost, Integer.MAX_VALUE));
        
        OptionContract option = new OptionContract(symbol, type, strikePrice, 
                PoliticalServer.server.getTickCount() + durationTicks, shares, player.getUUID().toString(), premium);
        activeOptions.add(option);
        
        String typeStr = type == OptionType.CALL ? "CALL" : "PUT";
        player.sendSystemMessage(Component.literal("📜 Bought " + shares + " " + typeStr + " options for " + symbol + 
                " @ strike " + formatPrice(strikePrice) + " (Premium: " + formatPrice(totalCost) + ")")
                .withStyle(ChatFormatting.GOLD), false);
        return true;
    }

    public static boolean exerciseOption(ServerPlayer player, OptionContract option) {
        if (option.isExpired(PoliticalServer.server.getTickCount())) {
            player.sendSystemMessage(Component.literal("This option has expired!").withStyle(ChatFormatting.RED), false);
            return false;
        }
        
        double currentPrice = currentPrices.get(option.symbol);
        if (!option.isInTheMoney(currentPrice)) {
            player.sendSystemMessage(Component.literal("Option is out of the money! Cannot exercise profitably.")
                    .withStyle(ChatFormatting.RED), false);
            return false;
        }
        
        // Calculate profit
        double profit;
        if (option.type == OptionType.CALL) {
            profit = (currentPrice - option.strikePrice - option.premium) * option.shares;
        } else {
            profit = (option.strikePrice - currentPrice - option.premium) * option.shares;
        }
        
        activeOptions.remove(option);
        
        if (profit > 0) {
            CoinManager.giveCoins(player, (int) Math.min((long) profit, Integer.MAX_VALUE));
            player.sendSystemMessage(Component.literal("💰 Exercised option for profit of " + formatPrice(profit) + " coins!")
                    .withStyle(ChatFormatting.GREEN), false);
        } else {
            player.sendSystemMessage(Component.literal("📉 Option exercised at a loss.").withStyle(ChatFormatting.RED), false);
        }
        return true;
    }

    private static double calculateOptionPremium(double currentPrice, double strikePrice, 
                                                 double volatility, long durationTicks) {
        // Simplified Black-Scholes approximation
        double timeToExpiry = durationTicks / 1728000.0; // Convert ticks to days (20 ticks/sec)
        double d1 = (Math.log(currentPrice / strikePrice) + (volatility * volatility / 2) * timeToExpiry) 
                / (volatility * Math.sqrt(timeToExpiry));
        double premium = currentPrice * normalCDF(d1) - strikePrice * Math.exp(-0.05 * timeToExpiry) * normalCDF(d1 - volatility * Math.sqrt(timeToExpiry));
        return Math.max(premium, currentPrice * 0.05); // Minimum 5% premium
    }

    private static double normalCDF(double x) {
        return 0.5 * (1 + erf(x / Math.sqrt(2)));
    }

    private static double erf(double x) {
        // Approximation of error function
        double t = 1.0 / (1.0 + 0.5 * Math.abs(x));
        double tau = t * Math.exp(-x*x - 1.26551223 + t * (1.00002368 + t * (0.37409196 + t * 
                (0.09678418 + t * (-0.18628806 + t * (0.27886807 + t * (-1.13520398 + t * 
                (1.48851587 + t * (-0.82215223 + t * 0.17087277)))))))));
        return x >= 0 ? 1 - tau : tau - 1;
    }

    public static List<OptionContract> getPlayerOptions(String playerUuid) {
        List<OptionContract> result = new ArrayList<>();
        for (OptionContract option : activeOptions) {
            if (option.owner.equals(playerUuid)) {
                result.add(option);
            }
        }
        return result;
    }

    // ═══════════════════════════════════════════════════════════════
    // ADVANCED FEATURES: DIVIDENDS
    // ═══════════════════════════════════════════════════════════════

    private static long lastDividendTick = 0;
    private static final long DIVIDEND_INTERVAL = 24000 * 7; // Weekly (20 ticks/sec * 60 * 60 * 24 * 7)
    private static final Map<String, Double> dividendYields = new ConcurrentHashMap<>();

    static {
        // Set dividend yields for stable stocks
        dividendYields.put("RAIL", 0.03);  // 3% annual yield
        dividendYields.put("VTR", 0.025);
        dividendYields.put("EBK", 0.04);
        dividendYields.put("FARM", 0.02);
        dividendYields.put("MOCH", 0.015);
    }

    public static void processDividends() {
        long currentTick = PoliticalServer.server.getTickCount();
        if (currentTick - lastDividendTick < DIVIDEND_INTERVAL) return;
        lastDividendTick = currentTick;
        
        for (Map.Entry<String, Map<String, Integer>> entry : playerPortfolios.entrySet()) {
            String uuid = entry.getKey();
            Map<String, Integer> portfolio = entry.getValue();
            
            long totalDividends = 0;
            for (Map.Entry<String, Integer> holding : portfolio.entrySet()) {
                String symbol = holding.getKey();
                int shares = holding.getValue();
                Double yield = dividendYields.get(symbol);
                
                if (yield != null && shares > 0) {
                    double price = currentPrices.getOrDefault(symbol, 0.0);
                    // Weekly dividend = annual yield / 52 * price * shares
                    long dividend = (long) (yield / 52 * price * shares);
                    totalDividends += dividend;
                }
            }
            
            if (totalDividends > 0) {
                ServerPlayer player = PoliticalServer.server.getPlayerList().getPlayer(UUID.fromString(uuid));
                if (player != null) {
                    CoinManager.giveCoins(player, (int) Math.min(totalDividends, Integer.MAX_VALUE));
                    player.sendSystemMessage(Component.literal("💰 Received " + formatPrice(totalDividends) + 
                            " coins in dividends!").withStyle(ChatFormatting.GREEN), false);
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // ADVANCED FEATURES: LIMIT ORDERS
    // ═══════════════════════════════════════════════════════════════

    public enum OrderType { BUY_LIMIT, SELL_LIMIT, BUY_STOP, SELL_STOP }
    
    public static class LimitOrder {
        public final String playerUuid;
        public final String symbol;
        public final OrderType type;
        public final double targetPrice;
        public final int shares;
        public final long createdTick;

        public LimitOrder(String playerUuid, String symbol, OrderType type, double targetPrice, int shares, long createdTick) {
            this.playerUuid = playerUuid;
            this.symbol = symbol;
            this.type = type;
            this.targetPrice = targetPrice;
            this.shares = shares;
            this.createdTick = createdTick;
        }

        public LimitOrder(String playerUuid, String symbol, OrderType type, double targetPrice, int shares) {
            this(playerUuid, symbol, type, targetPrice, shares,
                    PoliticalServer.server != null ? PoliticalServer.server.getTickCount() : 0L);
        }
    }

    private static final List<LimitOrder> limitOrders = new ArrayList<>();

    public static boolean placeLimitOrder(ServerPlayer player, String symbol, OrderType type, 
                                          double targetPrice, int shares) {
        Stock stock = getStockBySymbol(symbol);
        if (stock == null) {
            player.sendSystemMessage(Component.literal("Invalid stock symbol!").withStyle(ChatFormatting.RED), false);
            return false;
        }
        
        // For buy orders, reserve the funds
        if (type == OrderType.BUY_LIMIT || type == OrderType.BUY_STOP) {
            long maxCost = (long) (targetPrice * shares);
            if (!CoinManager.hasCoins(player, (int) Math.min(maxCost, Integer.MAX_VALUE))) {
                player.sendSystemMessage(Component.literal("Insufficient funds to place order!").withStyle(ChatFormatting.RED), false);
                return false;
            }
            // Reserve funds
            CoinManager.removeCoins(player, (int) Math.min(maxCost, Integer.MAX_VALUE));
        }
        
        // For sell orders, verify ownership
        if (type == OrderType.SELL_LIMIT || type == OrderType.SELL_STOP) {
            int owned = getPortfolio(player.getUUID().toString()).getOrDefault(symbol, 0);
            if (owned < shares) {
                player.sendSystemMessage(Component.literal("You don't own enough shares!").withStyle(ChatFormatting.RED), false);
                return false;
            }
        }
        
        limitOrders.add(new LimitOrder(player.getUUID().toString(), symbol, type, targetPrice, shares));
        
        String typeStr = switch (type) {
            case BUY_LIMIT -> "Buy Limit";
            case SELL_LIMIT -> "Sell Limit";
            case BUY_STOP -> "Buy Stop";
            case SELL_STOP -> "Sell Stop";
        };
        
        player.sendSystemMessage(Component.literal("📋 Placed " + typeStr + " order for " + shares + " " + symbol + 
                " @ " + formatPrice(targetPrice)).withStyle(ChatFormatting.YELLOW), false);
        return true;
    }

    public static void processLimitOrders() {
        List<LimitOrder> toRemove = new ArrayList<>();
        
        for (LimitOrder order : limitOrders) {
            double currentPrice = currentPrices.get(order.symbol);
            boolean shouldExecute = false;
            
            switch (order.type) {
                case BUY_LIMIT -> shouldExecute = currentPrice <= order.targetPrice;
                case SELL_LIMIT -> shouldExecute = currentPrice >= order.targetPrice;
                case BUY_STOP -> shouldExecute = currentPrice >= order.targetPrice;
                case SELL_STOP -> shouldExecute = currentPrice <= order.targetPrice;
            }
            
            if (shouldExecute) {
                ServerPlayer player = PoliticalServer.server.getPlayerList()
                        .getPlayer(UUID.fromString(order.playerUuid));
                if (player != null) {
                    // Execute the order
                    if (order.type == OrderType.BUY_LIMIT || order.type == OrderType.BUY_STOP) {
                        // Funds already reserved, just give shares
                        Map<String, Integer> portfolio = playerPortfolios.computeIfAbsent(
                                order.playerUuid, k -> new HashMap<>());
                        portfolio.merge(order.symbol, order.shares, Integer::sum);
                        player.sendSystemMessage(Component.literal("✅ Limit order executed! Bought " + order.shares + 
                                " " + order.symbol + " @ " + formatPrice(currentPrice))
                                .withStyle(ChatFormatting.GREEN), false);
                    } else {
                        // Sell shares
                        Map<String, Integer> portfolio = playerPortfolios.get(order.playerUuid);
                        if (portfolio != null) {
                            portfolio.merge(order.symbol, -order.shares, Integer::sum);
                            long value = (long) (currentPrice * order.shares);
                            CoinManager.giveCoins(player, (int) Math.min(value, Integer.MAX_VALUE));
                            player.sendSystemMessage(Component.literal("✅ Limit order executed! Sold " + order.shares + 
                                    " " + order.symbol + " @ " + formatPrice(currentPrice))
                                    .withStyle(ChatFormatting.GREEN), false);
                        }
                    }
                }
                toRemove.add(order);
            }
        }
        
        limitOrders.removeAll(toRemove);
    }

    public static List<LimitOrder> getPlayerOrders(String playerUuid) {
        List<LimitOrder> result = new ArrayList<>();
        for (LimitOrder order : limitOrders) {
            if (order.playerUuid.equals(playerUuid)) {
                result.add(order);
            }
        }
        return result;
    }

    public static boolean cancelLimitOrder(ServerPlayer player, int orderIndex) {
        String uuid = player.getUUID().toString();
        List<LimitOrder> playerOrders = getPlayerOrders(uuid);
        
        if (orderIndex < 0 || orderIndex >= playerOrders.size()) {
            player.sendSystemMessage(Component.literal("Invalid order index!").withStyle(ChatFormatting.RED), false);
            return false;
        }
        
        LimitOrder order = playerOrders.get(orderIndex);
        limitOrders.remove(order);
        
        // Refund reserved funds for buy orders
        if (order.type == OrderType.BUY_LIMIT || order.type == OrderType.BUY_STOP) {
            long refund = (long) (order.targetPrice * order.shares);
            CoinManager.giveCoins(player, (int) Math.min(refund, Integer.MAX_VALUE));
        }
        
        player.sendSystemMessage(Component.literal("❌ Order cancelled.").withStyle(ChatFormatting.YELLOW), false);
        return true;
    }

    // ═══════════════════════════════════════════════════════════════
    // MARKET STATISTICS
    // ═══════════════════════════════════════════════════════════════

    public static long getTotalMarketCap() {
        long total = 0;
        for (Stock stock : Stock.values()) {
            total += (long) (currentPrices.get(stock.symbol) * 1000000); // Assume 1M shares per stock
        }
        return total;
    }

    /** Shares bought and sold of a symbol since the server started (trades through buyStock and sellStock). */
    public static int getTotalSharesTraded(String symbol) {
        return (int) Math.min(Integer.MAX_VALUE, sharesTraded.getOrDefault(symbol, 0L));
    }

    private static final Map<String, Long> sharesTraded = new HashMap<>();

    public static double getMarketVolatility() {
        double totalVol = 0;
        for (Stock stock : Stock.values()) {
            totalVol += stock.volatility;
        }
        return totalVol / Stock.values().length;
    }

    // ── Admin (invsee) ───────────────────────────────────────────────

    public static void adminSetShares(String uuid, String symbol, int shares) {
        if (shares <= 0) {
            Map<String, Integer> pf = playerPortfolios.get(uuid);
            if (pf != null) {
                pf.remove(symbol);
                if (pf.isEmpty()) playerPortfolios.remove(uuid);
            }
            return;
        }
        playerPortfolios.computeIfAbsent(uuid, k -> new HashMap<>()).put(symbol, shares);
    }

    // ═══════════════════════════════════════════════════════════════
    // PERSISTENCE (save/load with server shutdown)
    // ═══════════════════════════════════════════════════════════════

    public static void persistSave(net.minecraft.server.MinecraftServer server) {
        if (server == null) return;
        StockPersisted root = new StockPersisted();
        root.playerPortfolios = new HashMap<>();
        for (Map.Entry<String, Map<String, Integer>> e : playerPortfolios.entrySet()) {
            root.playerPortfolios.put(e.getKey(), new HashMap<>(e.getValue()));
        }
        root.currentPrices = new HashMap<>(currentPrices);
        root.priceHistory = new HashMap<>();
        for (Map.Entry<String, LinkedList<Double>> e : priceHistory.entrySet()) {
            root.priceHistory.put(e.getKey(), new ArrayList<>(e.getValue()));
        }
        root.lastUpdateTick = lastUpdateTick;
        root.marketOpen = marketOpen;
        root.currentTrend = currentTrend.name();
        root.playerTradeCounts = new HashMap<>(playerTradeCounts);
        root.recentBuyPressure = new HashMap<>(recentBuyPressure);
        root.shortPositions = new HashMap<>();
        for (Map.Entry<String, Map<String, Integer>> e : shortPositions.entrySet()) {
            root.shortPositions.put(e.getKey(), new HashMap<>(e.getValue()));
        }
        root.shortEntryPrices = new HashMap<>();
        for (Map.Entry<String, Map<String, Double>> e : shortEntryPrices.entrySet()) {
            root.shortEntryPrices.put(e.getKey(), new HashMap<>(e.getValue()));
        }
        root.dividendYields = new HashMap<>(dividendYields);
        root.limitOrders = new ArrayList<>();
        for (LimitOrder o : limitOrders) {
            LimitOrderSnapshot s = new LimitOrderSnapshot();
            s.playerUuid = o.playerUuid;
            s.symbol = o.symbol;
            s.type = o.type.name();
            s.targetPrice = o.targetPrice;
            s.shares = o.shares;
            s.createdTick = o.createdTick;
            root.limitOrders.add(s);
        }
        root.activeEvents = new ArrayList<>();
        for (MarketEvent ev : activeEvents) {
            MarketEventSnapshot ms = new MarketEventSnapshot();
            ms.name = ev.name;
            ms.description = ev.description;
            ms.affectedStocks = new ArrayList<>(ev.affectedStocks);
            ms.priceModifier = ev.priceModifier;
            ms.durationTicks = ev.durationTicks;
            root.activeEvents.add(ms);
        }
        java.nio.file.Path path = server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).resolve("political_stock_state.json");
        try {
            java.nio.file.Files.createDirectories(path.getParent());
            try (java.io.Writer w = java.nio.file.Files.newBufferedWriter(path)) {
                new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(root, w);
            }
        } catch (java.io.IOException e) {
            PoliticalServer.LOGGER.error("Failed to save stock market state", e);
        }
    }

    public static void persistLoad(net.minecraft.server.MinecraftServer server) {
        java.nio.file.Path path = server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).resolve("political_stock_state.json");
        if (!java.nio.file.Files.exists(path)) return;
        try (java.io.Reader r = java.nio.file.Files.newBufferedReader(path)) {
            com.google.gson.Gson gson = new com.google.gson.Gson();
            StockPersisted root = gson.fromJson(r, StockPersisted.class);
            if (root == null) return;
            if (root.playerPortfolios != null) {
                playerPortfolios.clear();
                for (Map.Entry<String, Map<String, Integer>> e : root.playerPortfolios.entrySet()) {
                    playerPortfolios.put(e.getKey(), new ConcurrentHashMap<>(e.getValue()));
                }
            }
            if (root.currentPrices != null) {
                for (Map.Entry<String, Double> e : root.currentPrices.entrySet()) {
                    if (e.getValue() != null && currentPrices.containsKey(e.getKey())) {
                        currentPrices.put(e.getKey(), e.getValue());
                    }
                }
            }
            if (root.priceHistory != null) {
                for (Map.Entry<String, ArrayList<Double>> e : root.priceHistory.entrySet()) {
                    LinkedList<Double> list = priceHistory.get(e.getKey());
                    if (list != null) {
                        list.clear();
                        for (Double d : e.getValue()) {
                            if (d != null) list.addLast(d);
                        }
                    }
                }
            }
            if (root.lastUpdateTick != null) lastUpdateTick = root.lastUpdateTick;
            if (root.marketOpen != null) marketOpen = root.marketOpen;
            try {
                if (root.currentTrend != null) currentTrend = MarketTrend.valueOf(root.currentTrend);
            } catch (IllegalArgumentException ignored) {}
            if (root.playerTradeCounts != null) {
                playerTradeCounts.clear();
                playerTradeCounts.putAll(root.playerTradeCounts);
            }
            if (root.recentBuyPressure != null) {
                recentBuyPressure.clear();
                recentBuyPressure.putAll(root.recentBuyPressure);
            }
            if (root.shortPositions != null) {
                shortPositions.clear();
                for (Map.Entry<String, Map<String, Integer>> e : root.shortPositions.entrySet()) {
                    shortPositions.put(e.getKey(), new ConcurrentHashMap<>(e.getValue()));
                }
            }
            if (root.shortEntryPrices != null) {
                shortEntryPrices.clear();
                for (Map.Entry<String, Map<String, Double>> e : root.shortEntryPrices.entrySet()) {
                    shortEntryPrices.put(e.getKey(), new ConcurrentHashMap<>(e.getValue()));
                }
            }
            if (root.dividendYields != null) dividendYields.putAll(root.dividendYields);
            limitOrders.clear();
            if (root.limitOrders != null) {
                for (LimitOrderSnapshot s : root.limitOrders) {
                    try {
                        OrderType t = OrderType.valueOf(s.type);
                        limitOrders.add(new LimitOrder(s.playerUuid, s.symbol, t, s.targetPrice, s.shares, s.createdTick));
                    } catch (IllegalArgumentException | NullPointerException ignored) {}
                }
            }
            activeEvents.clear();
            if (root.activeEvents != null) {
                for (MarketEventSnapshot ms : root.activeEvents) {
                    if (ms != null && ms.name != null && ms.affectedStocks != null) {
                        activeEvents.add(new MarketEvent(ms.name, ms.description != null ? ms.description : "",
                                ms.affectedStocks, ms.priceModifier, ms.durationTicks));
                    }
                }
            }
            PoliticalServer.LOGGER.info("PoliticalServer: loaded stock market state from political_stock_state.json");
        } catch (java.io.IOException e) {
            PoliticalServer.LOGGER.error("Failed to load stock market state", e);
        }
    }

    /** Gson DTO */
    @SuppressWarnings("unused")
    static final class StockPersisted {
        Map<String, Map<String, Integer>> playerPortfolios;
        Map<String, Double> currentPrices;
        Map<String, ArrayList<Double>> priceHistory;
        Long lastUpdateTick;
        Boolean marketOpen;
        String currentTrend;
        Map<String, Integer> playerTradeCounts;
        Map<String, Integer> recentBuyPressure;
        Map<String, Map<String, Integer>> shortPositions;
        Map<String, Map<String, Double>> shortEntryPrices;
        Map<String, Double> dividendYields;
        List<LimitOrderSnapshot> limitOrders;
        List<MarketEventSnapshot> activeEvents;
    }

    static final class LimitOrderSnapshot {
        String playerUuid;
        String symbol;
        String type;
        double targetPrice;
        int shares;
        long createdTick;
    }

    static final class MarketEventSnapshot {
        String name;
        String description;
        List<String> affectedStocks;
        double priceModifier;
        long durationTicks;
    }
}
