package com.political;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Realistic cryptocurrency market system with blockchain mechanics,
 * mining, staking, DeFi features, and volatile price movements.
 */
public class CryptoMarket {

    // ═══════════════════════════════════════════════════════════════
    // CRYPTOCURRENCY DEFINITIONS
    // ═══════════════════════════════════════════════════════════════

    public enum Crypto {
        // Major Coins (Layer 1)
        ENDERCOIN("END", "Endercoin", 45000.0, 0.25, "🔮", true, 21000000),
        NETHERCOIN("NTH", "Nethercoin", 2800.0, 0.30, "🔥", true, 0),
        OVERWORLD("OVW", "Overworld", 1800.0, 0.22, "🌍", true, 0),
        
        // Smart Contract Platforms
        REDSTONE_CHAIN("RSC", "Redstone Chain", 350.0, 0.35, "⚡", false, 0),
        SLIME_CHAIN("SLM", "Slime Chain", 85.0, 0.40, "🟢", false, 0),
        
        // DeFi Tokens
        YIELD_FARM("YLD", "YieldFarm", 12.0, 0.50, "🌾", false, 0),
        LIQUIDITY_POOL("LIQ", "LiquidityPool", 28.0, 0.45, "💧", false, 0),
        SWAP_TOKEN("SWP", "SwapToken", 5.50, 0.55, "🔄", false, 0),
        
        // Meme Coins
        DOGE_COIN("DOGE", "DogeCoin", 0.08, 0.70, "🐕", false, 0),
        CHICKEN_COIN("CHK", "ChickenCoin", 0.0001, 0.85, "🐔", false, 0),
        WARDEN_COIN("WRD", "WardenCoin", 0.50, 0.75, "👁", false, 0),
        
        // Stablecoins
        EMERALD_USD("EUSD", "Emerald USD", 1.0, 0.01, "💎", false, 0),
        GOLD_STANDARD("GSTD", "Gold Standard", 1.0, 0.02, "🥇", false, 0),
        
        // Privacy Coins
        SCULK_SHADOW("SHD", "Sculk Shadow", 95.0, 0.45, "🌑", false, 0),
        PHANTOM_COIN("PHM", "Phantom Coin", 42.0, 0.40, "👻", false, 0),
        
        // Gaming/Metaverse
        BLOCK_TOKEN("BLK", "BlockToken", 15.0, 0.38, "🧱", false, 0),
        CRAFT_COIN("CFT", "CraftCoin", 8.0, 0.42, "🔨", false, 0),
        REALM_TOKEN("RLM", "RealmToken", 22.0, 0.35, "🏰", false, 0),
        
        // Layer 2 Solutions
        PORTAL_LINK("PTL", "Portal Link", 420.0, 0.32, "🌀", false, 0),
        BEACON_CHAIN("BCN", "Beacon Chain", 180.0, 0.28, "🔦", false, 0),
        
        // NFT/Governance
        DRAGON_GOV("DRG", "Dragon Governance", 650.0, 0.42, "🐉", false, 0),
        ART_TOKEN("ART", "ArtToken", 35.0, 0.48, "🎨", false, 0);

        public final String symbol;
        public final String name;
        public final double basePrice;
        public final double volatility;
        public final String icon;
        public final boolean isMineable;
        public final long maxSupply; // 0 = unlimited

        Crypto(String symbol, String name, double basePrice, double volatility, 
               String icon, boolean isMineable, long maxSupply) {
            this.symbol = symbol;
            this.name = name;
            this.basePrice = basePrice;
            this.volatility = volatility;
            this.icon = icon;
            this.isMineable = isMineable;
            this.maxSupply = maxSupply;
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // MARKET STATE
    // ═══════════════════════════════════════════════════════════════

    private static final Map<String, Double> currentPrices = new ConcurrentHashMap<>();
    private static final Map<String, LinkedList<Double>> priceHistory = new ConcurrentHashMap<>();
    private static final Map<String, Double> marketCaps = new ConcurrentHashMap<>();
    private static final Map<String, Long> circulatingSupply = new ConcurrentHashMap<>();
    private static final int MAX_HISTORY_SIZE = 100;

    // Player wallets (UUID -> (symbol -> balance))
    private static final Map<String, Map<String, Double>> wallets = new ConcurrentHashMap<>();
    
    // Staking positions (UUID -> (symbol -> stakedAmount))
    private static final Map<String, Map<String, Double>> stakingPositions = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, Long>> stakingStartTimes = new ConcurrentHashMap<>();
    
    // Liquidity pools
    private static final Map<String, LiquidityPool> liquidityPools = new ConcurrentHashMap<>();
    
    // Market state
    private static long lastUpdateTick = 0;
    private static final long UPDATE_INTERVAL = 1200; // Every 1 minute (20 ticks/sec * 60)
    private static MarketPhase currentPhase = MarketPhase.NEUTRAL;
    private static double fearGreedIndex = 50.0; // 0-100
    
    // Player trading impact tracking
    private static final Map<String, Double> recentBuyPressure = new ConcurrentHashMap<>(); // symbol -> net buy volume (USD notional)
    private static final Map<String, Integer> tradeCount = new ConcurrentHashMap<>(); // symbol -> number of trades
    
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
    
    public enum MarketPhase {
        ACCUMULATION("📊 Accumulation Phase", Formatting.GRAY),
        MARKUP("📈 Markup Phase (Bull Run)", Formatting.GREEN),
        DISTRIBUTION("📉 Distribution Phase", Formatting.YELLOW),
        MARKDOWN("📉 Markdown Phase (Bear Market)", Formatting.RED),
        NEUTRAL("⚖ Neutral Market", Formatting.WHITE);
        
        public final String name;
        public final Formatting color;
        MarketPhase(String name, Formatting color) {
            this.name = name;
            this.color = color;
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // LIQUIDITY POOL
    // ═══════════════════════════════════════════════════════════════

    public static class LiquidityPool {
        public final String tokenA;
        public final String tokenB;
        public double reserveA;
        public double reserveB;
        public final Map<String, Double> providerShares; // provider -> share
        
        public LiquidityPool(String tokenA, String tokenB, double reserveA, double reserveB) {
            this.tokenA = tokenA;
            this.tokenB = tokenB;
            this.reserveA = reserveA;
            this.reserveB = reserveB;
            this.providerShares = new HashMap<>();
        }
        
        public double getPriceAInB() {
            return reserveB / reserveA;
        }
        
        public double getPriceBInA() {
            return reserveA / reserveB;
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // INITIALIZATION
    // ═══════════════════════════════════════════════════════════════

    static {
        initializeMarket();
    }

    public static void initializeMarket() {
        for (Crypto crypto : Crypto.values()) {
            currentPrices.put(crypto.symbol, crypto.basePrice);
            priceHistory.put(crypto.symbol, new LinkedList<>());
            priceHistory.get(crypto.symbol).add(crypto.basePrice);
            circulatingSupply.put(crypto.symbol, crypto.isMineable ? 1000000L : 10000000L);
            marketCaps.put(crypto.symbol, crypto.basePrice * circulatingSupply.get(crypto.symbol));
        }
        
        // Initialize liquidity pools
        liquidityPools.put("END-EUSD", new LiquidityPool("END", "EUSD", 1000, 45000000));
        liquidityPools.put("NTH-EUSD", new LiquidityPool("NTH", "EUSD", 5000, 14000000));
        liquidityPools.put("OVW-EUSD", new LiquidityPool("OVW", "EUSD", 8000, 14400000));
        liquidityPools.put("RSC-EUSD", new LiquidityPool("RSC", "EUSD", 50000, 17500000));
    }

    // ═══════════════════════════════════════════════════════════════
    // MARKET UPDATE LOGIC (Realistic Crypto Movements)
    // ═══════════════════════════════════════════════════════════════

    public static void tick(MinecraftServer server) {
        long currentTick = server.getTicks();
        
        if (currentTick - lastUpdateTick >= UPDATE_INTERVAL) {
            lastUpdateTick = currentTick;
            updateMarket();
            processStakingRewards();
            checkLiquidations();
        }
    }

    private static void updateMarket() {
        Random random = new Random();
        
        // Update Fear & Greed Index
        double fgChange = (random.nextDouble() - 0.5) * 10;
        fearGreedIndex = Math.max(0, Math.min(100, fearGreedIndex + fgChange));
        
        // Determine market phase shift (5% chance)
        if (random.nextDouble() < 0.05) {
            currentPhase = MarketPhase.values()[random.nextInt(MarketPhase.values().length)];
        }
        
        // Update each crypto price
        for (Crypto crypto : Crypto.values()) {
            double oldPrice = currentPrices.get(crypto.symbol);
            double newPrice = calculateNewPrice(crypto, oldPrice, random);
            
            // Ensure price doesn't go below 0.00000001
            newPrice = Math.max(0.00000001, newPrice);
            
            currentPrices.put(crypto.symbol, newPrice);
            
            // Update history
            LinkedList<Double> history = priceHistory.get(crypto.symbol);
            history.addLast(newPrice);
            if (history.size() > MAX_HISTORY_SIZE) {
                history.removeFirst();
            }
            
            // Update market cap
            marketCaps.put(crypto.symbol, newPrice * circulatingSupply.get(crypto.symbol));
        }
        
        // Decay player buy pressure over time
        for (Map.Entry<String, Double> entry : recentBuyPressure.entrySet()) {
            double decayed = entry.getValue() * 0.7; // 30% decay per update
            if (Math.abs(decayed) < 0.01) {
                recentBuyPressure.remove(entry.getKey());
            } else {
                recentBuyPressure.put(entry.getKey(), decayed);
            }
        }
        
        // Random whale movements (large transactions affecting price)
        if (random.nextDouble() < 0.1) {
            simulateWhaleMovement(random);
        }
    }

    private static double calculateNewPrice(Crypto crypto, double currentPrice, Random random) {
        // Base volatility movement (scaled for 1-min intervals - ~1/4.5 of 20-min volatility)
        double baseChange = random.nextGaussian() * crypto.volatility * currentPrice * 0.0067;
        
        // Market phase modifier (scaled for 1-min intervals)
        double phaseModifier = switch (currentPhase) {
            case MARKUP -> 0.0033;
            case MARKDOWN -> -0.0033;
            case ACCUMULATION -> 0.0011;
            case DISTRIBUTION -> -0.0011;
            case NEUTRAL -> 0.0;
        };
        
        // Fear & Greed modifier (scaled for 1-min intervals)
        double fgModifier = (fearGreedIndex - 50) / 22500.0;
        
        // Momentum (trend following, scaled for 1-min intervals)
        double momentum = calculateMomentum(crypto.symbol) * 0.00022;
        
        // Mean reversion (stronger for stablecoins)
        double reversionFactor = crypto.symbol.equals("EUSD") || crypto.symbol.equals("GSTD") ? 0.5 : 0.02;
        double reversion = (crypto.basePrice - currentPrice) * reversionFactor;
        
        // Player trading impact - buys push price up, sells push down
        double playerImpact = 0.0;
        Double buyPressure = recentBuyPressure.get(crypto.symbol);
        if (buyPressure != null && buyPressure != 0) {
            // Scale impact by notional volume relative to market cap
            double marketCap = marketCaps.getOrDefault(crypto.symbol, currentPrice * 1_000_000d);
            double relativeVolume = Math.abs(buyPressure) / Math.max(1.0, marketCap);
            // Cap impact at 2% per update cycle for realistic movement
            playerImpact = Math.signum(buyPressure) * Math.min(relativeVolume * currentPrice * 0.8, currentPrice * 0.02);
        }
        
        // Calculate new price
        double trendChange = currentPrice * phaseModifier;
        double fgChange = currentPrice * fgModifier;
        double newPrice = currentPrice + baseChange + trendChange + fgChange + momentum + reversion + playerImpact;
        
        // Occasional flash crash or pump (0.05% chance, scaled for more frequent updates)
        if (random.nextDouble() < 0.0005) {
            double flashMove = (random.nextDouble() - 0.5) * 0.05; // -2.5% to +2.5%
            newPrice *= (1 + flashMove);
        }
        
        return newPrice;
    }

    private static double calculateMomentum(String symbol) {
        LinkedList<Double> history = priceHistory.get(symbol);
        if (history == null || history.size() < 10) return 0;
        
        double recent = 0;
        double older = 0;
        int mid = history.size() / 2;
        
        for (int i = mid; i < history.size(); i++) {
            recent += history.get(i);
        }
        for (int i = 0; i < mid; i++) {
            older += history.get(i);
        }
        
        return recent / (mid + 1) - older / mid;
    }

    private static void simulateWhaleMovement(Random random) {
        // Pick a random crypto
        Crypto[] cryptos = Crypto.values();
        Crypto target = cryptos[random.nextInt(cryptos.length)];
        
        // Whale buys or sells
        boolean isBuy = random.nextBoolean();
        double priceImpact = (random.nextDouble() * 0.05 + 0.02) * (isBuy ? 1 : -1);
        
        double currentPrice = currentPrices.get(target.symbol);
        currentPrices.put(target.symbol, currentPrice * (1 + priceImpact));
    }

    // ═══════════════════════════════════════════════════════════════
    // WALLET OPERATIONS
    // ═══════════════════════════════════════════════════════════════

    public static Map<String, Double> getWallet(String playerUuid) {
        return new HashMap<>(wallets.getOrDefault(playerUuid, new HashMap<>()));
    }

    public static double getBalance(String playerUuid, String symbol) {
        Map<String, Double> wallet = wallets.get(playerUuid);
        return wallet != null ? wallet.getOrDefault(symbol, 0.0) : 0.0;
    }

    public static void deposit(String playerUuid, String symbol, double amount) {
        Map<String, Double> wallet = wallets.computeIfAbsent(playerUuid, k -> new HashMap<>());
        wallet.merge(symbol, amount, Double::sum);
    }

    public static boolean withdraw(String playerUuid, String symbol, double amount) {
        Map<String, Double> wallet = wallets.get(playerUuid);
        if (wallet == null) return false;
        
        double current = wallet.getOrDefault(symbol, 0.0);
        if (current < amount) return false;
        
        wallet.put(symbol, current - amount);
        return true;
    }

    public static double getWalletValue(String playerUuid) {
        Map<String, Double> wallet = wallets.get(playerUuid);
        if (wallet == null || wallet.isEmpty()) return 0;
        
        double total = 0;
        for (Map.Entry<String, Double> entry : wallet.entrySet()) {
            double price = currentPrices.getOrDefault(entry.getKey(), 0.0);
            total += price * entry.getValue();
        }
        return total;
    }

    // ═══════════════════════════════════════════════════════════════
    // TRADING OPERATIONS
    // ═══════════════════════════════════════════════════════════════

    public static boolean buyCrypto(ServerPlayerEntity player, String symbol, double amount) {
        Crypto crypto = getCryptoBySymbol(symbol);
        if (crypto == null) {
            player.sendMessage(Text.literal("Invalid cryptocurrency!").formatted(Formatting.RED), false);
            return false;
        }
        
        double price = currentPrices.get(symbol);
        double cost = price * amount;
        String uuid = player.getUuidAsString();
        
        // Apply transaction fee (5% base, reduced by INVESTOR buff)
        double feeRate = 0.05 * (1.0 - PlayerBuffManager.getMarketFeeReduction(uuid));
        double fee = Math.ceil(cost * feeRate);
        double totalCost = cost + fee;
        
        // Convert coins to EUSD first (1000 coins = 1 EUSD)
        double eusdNeeded = totalCost / currentPrices.get("EUSD");
        long coinsNeeded = (long) (eusdNeeded * 1000);
        
        if (!CoinManager.hasCoins(player, (int) Math.min(coinsNeeded, Integer.MAX_VALUE))) {
            player.sendMessage(Text.literal("Insufficient funds! Need " + formatCrypto(coinsNeeded) + " coins (incl. " + formatCrypto((long)fee) + " fee).")
                    .formatted(Formatting.RED), false);
            return false;
        }
        
        // Deduct coins
        CoinManager.removeCoins(player, (int) Math.min(coinsNeeded, Integer.MAX_VALUE));
        
        // Track trade count for INVESTOR buff
        StockMarket.incrementTradeCount(uuid);
        
        // Add crypto to wallet
        deposit(player.getUuidAsString(), symbol, amount);
        
        // Track buy pressure for price impact (USD notional)
        double pressure = recentBuyPressure.getOrDefault(symbol, 0.0);
        recentBuyPressure.put(symbol, pressure + (price * amount));
        
        player.sendMessage(Text.literal("✅ Bought " + formatCrypto(amount) + " " + symbol + " for " + 
                formatCrypto(coinsNeeded) + " coins (fee: " + formatCrypto((long)fee) + ")!").formatted(Formatting.GREEN), false);
        return true;
    }

    public static boolean sellCrypto(ServerPlayerEntity player, String symbol, double amount) {
        String uuid = player.getUuidAsString();
        
        if (getBalance(uuid, symbol) < amount) {
            player.sendMessage(Text.literal("Insufficient crypto balance!").formatted(Formatting.RED), false);
            return false;
        }
        
        double price = currentPrices.get(symbol);
        double value = price * amount;
        
        // Apply transaction fee (5% base, reduced by INVESTOR buff)
        double feeRate = 0.05 * (1.0 - PlayerBuffManager.getMarketFeeReduction(uuid));
        double fee = Math.ceil(value * feeRate);
        double netValue = value - fee;
        
        // Convert to EUSD then to coins
        double eusdValue = netValue / currentPrices.get("EUSD");
        long coinsValue = (long) (eusdValue * 1000);
        
        // Remove crypto
        withdraw(uuid, symbol, amount);
        
        // Track sell pressure for price impact (USD notional; negative = selling)
        double pressure = recentBuyPressure.getOrDefault(symbol, 0.0);
        recentBuyPressure.put(symbol, pressure - (price * amount));
        
        // Track trade count for INVESTOR buff
        StockMarket.incrementTradeCount(uuid);
        
        // Give coins
        CoinManager.giveCoins(player, (int) Math.min(coinsValue, Integer.MAX_VALUE));
        
        player.sendMessage(Text.literal("✅ Sold " + formatCrypto(amount) + " " + symbol + " for " + 
                formatCrypto(coinsValue) + " coins (fee: " + formatCrypto((long)fee) + ")!").formatted(Formatting.GREEN), false);
        return true;
    }

    // ═══════════════════════════════════════════════════════════════
    // STAKING (Proof of Stake)
    // ═══════════════════════════════════════════════════════════════

    private static final Map<String, Double> stakingApy = new ConcurrentHashMap<>();
    
    static {
        stakingApy.put("END", 0.05);   // 5% APY
        stakingApy.put("NTH", 0.07);   // 7% APY
        stakingApy.put("OVW", 0.06);
        stakingApy.put("RSC", 0.12);   // Higher for smart contract chains
        stakingApy.put("SLM", 0.15);
        stakingApy.put("YLD", 0.25);   // DeFi high yields
        stakingApy.put("LIQ", 0.20);
    }

    public static boolean stakeCrypto(ServerPlayerEntity player, String symbol, double amount) {
        String uuid = player.getUuidAsString();
        
        if (!stakingApy.containsKey(symbol)) {
            player.sendMessage(Text.literal("This crypto cannot be staked!").formatted(Formatting.RED), false);
            return false;
        }
        
        if (getBalance(uuid, symbol) < amount) {
            player.sendMessage(Text.literal("Insufficient balance to stake!").formatted(Formatting.RED), false);
            return false;
        }
        
        // Move from wallet to staking
        withdraw(uuid, symbol, amount);
        
        Map<String, Double> positions = stakingPositions.computeIfAbsent(uuid, k -> new HashMap<>());
        Map<String, Long> times = stakingStartTimes.computeIfAbsent(uuid, k -> new HashMap<>());
        
        positions.merge(symbol, amount, Double::sum);
        times.put(symbol, Long.valueOf(PoliticalServer.server.getTicks()));
        
        double apy = stakingApy.get(symbol) * 100;
        player.sendMessage(Text.literal("🔒 Staked " + formatCrypto(amount) + " " + symbol + 
                " at " + String.format("%.1f", apy) + "% APY!").formatted(Formatting.LIGHT_PURPLE), false);
        return true;
    }

    public static boolean unstakeCrypto(ServerPlayerEntity player, String symbol, double amount) {
        String uuid = player.getUuidAsString();
        Map<String, Double> positions = stakingPositions.get(uuid);
        
        if (positions == null || positions.getOrDefault(symbol, 0.0) < amount) {
            player.sendMessage(Text.literal("Insufficient staked amount!").formatted(Formatting.RED), false);
            return false;
        }
        
        // Calculate rewards
        long stakeTime = stakingStartTimes.get(uuid).get(symbol);
        long currentTime = PoliticalServer.server.getTicks();
        long duration = currentTime - stakeTime;
        
        double apy = stakingApy.get(symbol);
        double rewards = positions.get(symbol) * apy * (duration / (20.0 * 60 * 60 * 24 * 365)); // ticks to years
        
        // Return principal + rewards
        double totalReturn = amount + (rewards * (amount / positions.get(symbol)));
        
        positions.merge(symbol, -amount, Double::sum);
        deposit(uuid, symbol, totalReturn);
        
        player.sendMessage(Text.literal("🔓 Unstaked " + formatCrypto(amount) + " " + symbol + 
                " + " + formatCrypto(totalReturn - amount) + " rewards!").formatted(Formatting.GREEN), false);
        return true;
    }

    public static double getStakedAmount(String playerUuid, String symbol) {
        Map<String, Double> positions = stakingPositions.get(playerUuid);
        return positions != null ? positions.getOrDefault(symbol, 0.0) : 0.0;
    }

    private static void processStakingRewards() {
        // Rewards are calculated on unstake, this is just for notifications
    }

    // ═══════════════════════════════════════════════════════════════
    // DEFI: LIQUIDITY MINING
    // ═══════════════════════════════════════════════════════════════

    public static boolean provideLiquidity(ServerPlayerEntity player, String poolId, double amountA, double amountB) {
        LiquidityPool pool = liquidityPools.get(poolId);
        if (pool == null) {
            player.sendMessage(Text.literal("Pool not found!").formatted(Formatting.RED), false);
            return false;
        }
        
        String uuid = player.getUuidAsString();
        
        // Check balances
        if (getBalance(uuid, pool.tokenA) < amountA || getBalance(uuid, pool.tokenB) < amountB) {
            player.sendMessage(Text.literal("Insufficient token balances!").formatted(Formatting.RED), false);
            return false;
        }
        
        // Withdraw tokens
        withdraw(uuid, pool.tokenA, amountA);
        withdraw(uuid, pool.tokenB, amountB);
        
        // Calculate share
        double totalLiquidity = Math.sqrt(pool.reserveA * pool.reserveB);
        double providedLiquidity = Math.sqrt(amountA * amountB);
        double share = providedLiquidity / (totalLiquidity + providedLiquidity);
        
        // Update pool
        pool.reserveA += amountA;
        pool.reserveB += amountB;
        pool.providerShares.put(uuid, pool.providerShares.getOrDefault(uuid, 0.0) + share);
        
        player.sendMessage(Text.literal("💧 Provided liquidity to " + poolId + 
                " (Share: " + String.format("%.2f", share * 100) + "%)").formatted(Formatting.AQUA), false);
        return true;
    }

    public static boolean removeLiquidity(ServerPlayerEntity player, String poolId, double sharePercent) {
        LiquidityPool pool = liquidityPools.get(poolId);
        if (pool == null) return false;
        
        String uuid = player.getUuidAsString();
        double userShare = pool.providerShares.getOrDefault(uuid, 0.0);
        
        if (userShare <= 0) {
            player.sendMessage(Text.literal("You have no liquidity in this pool!").formatted(Formatting.RED), false);
            return false;
        }
        
        double removeShare = userShare * sharePercent;
        double amountA = pool.reserveA * removeShare;
        double amountB = pool.reserveB * removeShare;
        
        // Update pool
        pool.reserveA -= amountA;
        pool.reserveB -= amountB;
        pool.providerShares.put(uuid, userShare - removeShare);
        
        // Return tokens
        deposit(uuid, pool.tokenA, amountA);
        deposit(uuid, pool.tokenB, amountB);
        
        player.sendMessage(Text.literal("💧 Removed liquidity: " + formatCrypto(amountA) + " " + pool.tokenA + 
                " + " + formatCrypto(amountB) + " " + pool.tokenB).formatted(Formatting.AQUA), false);
        return true;
    }

    // ═══════════════════════════════════════════════════════════════
    // DEFI: SWAP (AMM)
    // ═══════════════════════════════════════════════════════════════

    public static boolean swapTokens(ServerPlayerEntity player, String fromSymbol, String toSymbol, double amount) {
        String uuid = player.getUuidAsString();
        
        if (getBalance(uuid, fromSymbol) < amount) {
            player.sendMessage(Text.literal("Insufficient balance!").formatted(Formatting.RED), false);
            return false;
        }
        
        // Find or create pool
        String poolId = fromSymbol + "-" + toSymbol;
        String reversePoolId = toSymbol + "-" + fromSymbol;
        
        LiquidityPool pool = liquidityPools.get(poolId);
        boolean isReverse = false;
        
        if (pool == null) {
            pool = liquidityPools.get(reversePoolId);
            isReverse = true;
        }
        
        if (pool == null) {
            // Use EUSD as intermediary
            if (!fromSymbol.equals("EUSD") && !toSymbol.equals("EUSD")) {
                // Swap through EUSD
                double eusdAmount = amount * currentPrices.get(fromSymbol) / currentPrices.get("EUSD");
                return swapTokens(player, fromSymbol, "EUSD", amount) && 
                       swapTokens(player, "EUSD", toSymbol, eusdAmount);
            }
            player.sendMessage(Text.literal("No liquidity pool available!").formatted(Formatting.RED), false);
            return false;
        }
        
        // Calculate output using constant product formula: x * y = k
        double inputReserve = isReverse ? pool.reserveB : pool.reserveA;
        double outputReserve = isReverse ? pool.reserveA : pool.reserveB;
        
        // With 0.3% fee
        double amountInWithFee = amount * 0.997;
        double amountOut = (amountInWithFee * outputReserve) / (inputReserve + amountInWithFee);
        
        // Slippage protection
        double expectedRate = currentPrices.get(fromSymbol) / currentPrices.get(toSymbol);
        double actualRate = amountOut / amount;
        double slippage = Math.abs(actualRate - expectedRate) / expectedRate;
        
        if (slippage > 0.05) { // 5% max slippage
            player.sendMessage(Text.literal("⚠ High slippage! Try smaller amount.").formatted(Formatting.YELLOW), false);
        }
        
        // Execute swap
        withdraw(uuid, fromSymbol, amount);
        deposit(uuid, toSymbol, amountOut);
        
        // Update pool reserves
        if (isReverse) {
            pool.reserveB += amount;
            pool.reserveA -= amountOut;
        } else {
            pool.reserveA += amount;
            pool.reserveB -= amountOut;
        }
        
        player.sendMessage(Text.literal("🔄 Swapped " + formatCrypto(amount) + " " + fromSymbol + 
                " → " + formatCrypto(amountOut) + " " + toSymbol).formatted(Formatting.AQUA), false);
        return true;
    }

    // ═══════════════════════════════════════════════════════════════
    // MARGIN TRADING & LIQUIDATIONS
    // ═══════════════════════════════════════════════════════════════

    private static final Map<String, Map<String, MarginPosition>> marginPositions = new ConcurrentHashMap<>();
    
    public static class MarginPosition {
        public final String symbol;
        public final boolean isLong;
        public final double size;
        public final double entryPrice;
        public final double leverage;
        public final double liquidationPrice;
        public final double collateral;
        
        public MarginPosition(String symbol, boolean isLong, double size, double entryPrice, 
                             double leverage, double collateral) {
            this.symbol = symbol;
            this.isLong = isLong;
            this.size = size;
            this.entryPrice = entryPrice;
            this.leverage = leverage;
            this.collateral = collateral;
            this.liquidationPrice = isLong ? 
                    entryPrice * (1 - 1/leverage + 0.05) : // 5% buffer
                    entryPrice * (1 + 1/leverage - 0.05);
        }
    }

    public static boolean openMarginPosition(ServerPlayerEntity player, String symbol, boolean isLong, 
                                             double size, double leverage) {
        Crypto crypto = getCryptoBySymbol(symbol);
        if (crypto == null) return false;
        
        double price = currentPrices.get(symbol);
        double positionValue = price * size;
        double collateralNeeded = positionValue / leverage;
        
        // Check if player has enough EUSD for collateral
        if (getBalance(player.getUuidAsString(), "EUSD") < collateralNeeded) {
            player.sendMessage(Text.literal("Insufficient EUSD for collateral! Need " + 
                    formatCrypto(collateralNeeded) + " EUSD").formatted(Formatting.RED), false);
            return false;
        }
        
        // Lock collateral
        withdraw(player.getUuidAsString(), "EUSD", collateralNeeded);
        
        MarginPosition position = new MarginPosition(symbol, isLong, size, price, leverage, collateralNeeded);
        Map<String, MarginPosition> playerPositions = marginPositions.computeIfAbsent(
                player.getUuidAsString(), k -> new HashMap<>());
        playerPositions.put(symbol + "_" + System.currentTimeMillis(), position);
        
        String direction = isLong ? "LONG" : "SHORT";
        player.sendMessage(Text.literal("📊 Opened " + leverage + "x " + direction + " on " + symbol + 
                " (Liquidation: " + formatCrypto(position.liquidationPrice) + ")").formatted(Formatting.GOLD), false);
        return true;
    }

    public static boolean closeMarginPosition(ServerPlayerEntity player, String positionKey) {
        Map<String, MarginPosition> playerPositions = marginPositions.get(player.getUuidAsString());
        if (playerPositions == null || !playerPositions.containsKey(positionKey)) {
            return false;
        }
        
        MarginPosition position = playerPositions.get(positionKey);
        double currentPrice = currentPrices.get(position.symbol);
        
        // Calculate PnL
        double priceChange = currentPrice - position.entryPrice;
        double pnl = position.isLong ? 
                (priceChange / position.entryPrice) * position.size * position.leverage :
                (-priceChange / position.entryPrice) * position.size * position.leverage;
        
        // Return collateral + PnL
        double totalReturn = position.collateral + pnl;
        if (totalReturn > 0) {
            deposit(player.getUuidAsString(), "EUSD", totalReturn);
        }
        
        playerPositions.remove(positionKey);
        
        if (pnl >= 0) {
            player.sendMessage(Text.literal("💰 Closed position with profit: " + 
                    formatCrypto(pnl) + " EUSD!").formatted(Formatting.GREEN), false);
        } else {
            player.sendMessage(Text.literal("📉 Closed position with loss: " + 
                    formatCrypto(-pnl) + " EUSD").formatted(Formatting.RED), false);
        }
        return true;
    }

    private static void checkLiquidations() {
        for (Map.Entry<String, Map<String, MarginPosition>> entry : marginPositions.entrySet()) {
            String uuid = entry.getKey();
            Map<String, MarginPosition> positions = entry.getValue();
            
            List<String> toLiquidate = new ArrayList<>();
            for (Map.Entry<String, MarginPosition> pos : positions.entrySet()) {
                MarginPosition position = pos.getValue();
                double currentPrice = currentPrices.get(position.symbol);
                
                boolean shouldLiquidate = position.isLong ? 
                        currentPrice <= position.liquidationPrice :
                        currentPrice >= position.liquidationPrice;
                
                if (shouldLiquidate) {
                    toLiquidate.add(pos.getKey());
                    ServerPlayerEntity player = PoliticalServer.server.getPlayerManager()
                            .getPlayer(UUID.fromString(uuid));
                    if (player != null) {
                        player.sendMessage(Text.literal("⚠ Position liquidated! " + position.symbol)
                                .formatted(Formatting.RED, Formatting.BOLD), false);
                    }
                }
            }
            
            for (String key : toLiquidate) {
                positions.remove(key);
            }
        }
    }

    public static Map<String, MarginPosition> getMarginPositions(String playerUuid) {
        return new HashMap<>(marginPositions.getOrDefault(playerUuid, new HashMap<>()));
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

    public static Crypto getCryptoBySymbol(String symbol) {
        for (Crypto crypto : Crypto.values()) {
            if (crypto.symbol.equals(symbol)) {
                return crypto;
            }
        }
        return null;
    }

    public static MarketPhase getCurrentPhase() {
        return currentPhase;
    }

    public static double getFearGreedIndex() {
        return fearGreedIndex;
    }

    public static void resetFearGreedIndex() {
        Random random = new Random();
        fearGreedIndex = 20 + random.nextDouble() * 60; // 20-80 range
    }

    public static void setPrice(String symbol, double price) {
        if (price > 0 && currentPrices.containsKey(symbol)) {
            currentPrices.put(symbol, price);
        }
    }

    public static String getFearGreedLabel() {
        if (fearGreedIndex < 20) return "Extreme Fear";
        if (fearGreedIndex < 40) return "Fear";
        if (fearGreedIndex < 60) return "Neutral";
        if (fearGreedIndex < 80) return "Greed";
        return "Extreme Greed";
    }

    public static double getMarketCap(String symbol) {
        return marketCaps.getOrDefault(symbol, 0.0);
    }

    public static long getCirculatingSupply(String symbol) {
        return circulatingSupply.getOrDefault(symbol, 0L);
    }

    public static double getStakingApy(String symbol) {
        return stakingApy.getOrDefault(symbol, 0.0);
    }

    public static Map<String, LiquidityPool> getLiquidityPools() {
        return new HashMap<>(liquidityPools);
    }

    // ═══════════════════════════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════════════════════════

    public static String formatCrypto(double amount) {
        if (amount >= 1000000) {
            return String.format("%.2fM", amount / 1000000);
        } else if (amount >= 1000) {
            return String.format("%.2fK", amount / 1000);
        } else if (amount >= 1) {
            return String.format("%.2f", amount);
        } else if (amount >= 0.01) {
            return String.format("%.4f", amount);
        } else {
            return String.format("%.8f", amount);
        }
    }

    public static double calculateChange(String symbol) {
        LinkedList<Double> history = priceHistory.get(symbol);
        if (history == null || history.size() < 2) return 0.0;
        
        double current = history.getLast();
        double previous = history.get(history.size() - 2);
        return ((current - previous) / previous) * 100;
    }

    public static double calculate24hChange(String symbol) {
        LinkedList<Double> history = priceHistory.get(symbol);
        if (history == null || history.size() < 24) return 0.0; // 24 intervals = 24 minutes
        
        double current = history.getLast();
        double dayAgo = history.get(history.size() - 24);
        return ((current - dayAgo) / dayAgo) * 100;
    }

    public static double getTotalMarketCap() {
        double total = 0;
        for (Crypto crypto : Crypto.values()) {
            total += marketCaps.getOrDefault(crypto.symbol, 0.0);
        }
        return total;
    }
}
