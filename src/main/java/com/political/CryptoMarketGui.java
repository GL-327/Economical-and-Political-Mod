package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.gui.SignGui;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.*;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * GUI system for cryptocurrency trading with DeFi features.
 */
public class CryptoMarketGui {

    // Track open GUIs for auto-refresh
    private static final Map<UUID, SimpleGui> openCryptoGuis = new ConcurrentHashMap<>();
    private static final Map<UUID, String> guiViewType = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> guiPage = new ConcurrentHashMap<>();
    private static final Map<UUID, String> guiSymbol = new ConcurrentHashMap<>();
    private static int tickCounter = 0;

    // Call this from ServerTickEvents.END_SERVER_TICK
    public static void tick() {
        tickCounter++;
        // Only refresh every 20 ticks (1 second) to avoid performance issues
        if (tickCounter % 20 != 0) return;
        
        openCryptoGuis.entrySet().removeIf(entry -> {
            SimpleGui gui = entry.getValue();
            if (gui.isOpen()) {
                UUID uuid = entry.getKey();
                String viewType = guiViewType.getOrDefault(uuid, "main");
                
                // Refresh based on view type
                switch (viewType) {
                    case "main" -> refreshHeader(gui);
                    case "browser" -> refreshBrowserView(gui, uuid);
                    case "wallet" -> refreshWalletView(gui, uuid);
                    case "detail" -> refreshDetailView(gui, uuid);
                }
                return false;
            }
            // Clean up tracking maps
            guiViewType.remove(entry.getKey());
            guiPage.remove(entry.getKey());
            guiSymbol.remove(entry.getKey());
            return true;
        });
    }

    private static void refreshHeader(SimpleGui gui) {
        ServerPlayerEntity player = gui.getPlayer();
        if (player == null) return;
        
        // Update the header slot with fresh data
        CryptoMarket.MarketPhase phase = CryptoMarket.getCurrentPhase();
        double fgIndex = CryptoMarket.getFearGreedIndex();
        Formatting fgColor = fgIndex < 40 ? Formatting.RED : (fgIndex < 60 ? Formatting.YELLOW : Formatting.GREEN);
        long currentTick = PoliticalServer.server.getTicks();
        String countdown = CryptoMarket.getCountdownString(currentTick);
        
        gui.setSlot(4, new GuiElementBuilder(Items.END_CRYSTAL)
                .setName(Text.literal("🔮 Crypto Market").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal(phase.name).formatted(phase.color))
                .addLoreLine(Text.literal("Fear & Greed: " + String.format("%.0f", fgIndex) + " (" + 
                        CryptoMarket.getFearGreedLabel() + ")").formatted(fgColor))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§7Total Market Cap: §6" + 
                        CryptoMarket.formatCrypto(CryptoMarket.getTotalMarketCap())))
                .addLoreLine(Text.literal("§7Your Wallet: §6" + 
                        CryptoMarket.formatCrypto(CryptoMarket.getWalletValue(player.getUuidAsString())) + " USD"))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§e⏱ Next update: " + countdown).formatted(Formatting.YELLOW))
                .glow().build());
    }

    private static void refreshBrowserView(SimpleGui gui, UUID uuid) {
        ServerPlayerEntity player = gui.getPlayer();
        if (player == null) return;
        
        int page = guiPage.getOrDefault(uuid, 0);
        CryptoMarket.Crypto[] cryptos = CryptoMarket.Crypto.values();
        int itemsPerPage = 28;
        int startIdx = page * itemsPerPage;
        
        int slot = 10;
        int count = 0;
        
        for (int i = startIdx; i < cryptos.length && count < itemsPerPage; i++) {
            CryptoMarket.Crypto crypto = cryptos[i];
            final String symbol = crypto.symbol;
            double price = CryptoMarket.getPrice(crypto.symbol);
            double change = CryptoMarket.calculateChange(crypto.symbol);
            
            Formatting changeColor = change >= 0 ? Formatting.GREEN : Formatting.RED;
            String changeStr = (change >= 0 ? "+" : "") + String.format("%.2f", change) + "%";
            
            gui.setSlot(slot, new GuiElementBuilder(getCryptoDisplayItem(crypto).getItem())
                    .setName(Text.literal(crypto.icon + " " + crypto.symbol).formatted(Formatting.BOLD))
                    .addLoreLine(Text.literal(crypto.name).formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("§6Price: $" + CryptoMarket.formatCrypto(price)))
                    .addLoreLine(Text.literal("§7Change: " + changeStr).formatted(changeColor))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("§eClick to trade"))
                    .setCallback((idx, type, action) -> openCryptoDetail(player, symbol))
                    .build());
            
            count++;
            slot++;
            if (slot == 17) slot = 19;
            else if (slot == 26) slot = 28;
            else if (slot == 35) slot = 37;
            else if (slot == 44) slot = 46;
        }
    }

    private static void refreshWalletView(SimpleGui gui, UUID uuid) {
        ServerPlayerEntity player = gui.getPlayer();
        if (player == null) return;
        
        double walletValue = CryptoMarket.getWalletValue(player.getUuidAsString());
        
        gui.setSlot(4, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Text.literal("💼 Your Wallet").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Total Value: §6$" + CryptoMarket.formatCrypto(walletValue)).formatted(Formatting.YELLOW))
                .glow().build());
        
        Map<String, Double> wallet = CryptoMarket.getWallet(player.getUuidAsString());
        int slot = 9;
        
        for (Map.Entry<String, Double> entry : wallet.entrySet()) {
            if (slot >= 45) break;
            
            String symbol = entry.getKey();
            double amount = entry.getValue();
            double price = CryptoMarket.getPrice(symbol);
            double value = price * amount;
            double change = CryptoMarket.calculateChange(symbol);
            
            Formatting changeColor = change >= 0 ? Formatting.GREEN : Formatting.RED;
            CryptoMarket.Crypto crypto = CryptoMarket.getCryptoBySymbol(symbol);
            
            if (crypto != null) {
                gui.setSlot(slot, new GuiElementBuilder(getCryptoDisplayItem(crypto).getItem())
                        .setName(Text.literal(symbol).formatted(Formatting.BOLD))
                        .addLoreLine(Text.literal(""))
                        .addLoreLine(Text.literal("Amount: §f" + CryptoMarket.formatCrypto(amount)).formatted(Formatting.WHITE))
                        .addLoreLine(Text.literal("Price: §6$" + CryptoMarket.formatCrypto(price)).formatted(Formatting.YELLOW))
                        .addLoreLine(Text.literal("Value: §6$" + CryptoMarket.formatCrypto(value)).formatted(Formatting.GOLD))
                        .addLoreLine(Text.literal("Change: " + (change >= 0 ? "+" : "") + String.format("%.2f", change) + "%").formatted(changeColor))
                        .addLoreLine(Text.literal(""))
                        .addLoreLine(Text.literal("§eClick to trade"))
                        .setCallback((idx, type, action) -> openCryptoDetail(player, symbol))
                        .build());
            }
            slot++;
        }
    }

    private static void refreshDetailView(SimpleGui gui, UUID uuid) {
        ServerPlayerEntity player = gui.getPlayer();
        if (player == null) return;
        
        String symbol = guiSymbol.get(uuid);
        if (symbol == null) return;
        
        CryptoMarket.Crypto crypto = CryptoMarket.getCryptoBySymbol(symbol);
        if (crypto == null) return;
        
        double price = CryptoMarket.getPrice(symbol);
        double change = CryptoMarket.calculateChange(symbol);
        
        Formatting changeColor = change >= 0 ? Formatting.GREEN : Formatting.RED;
        
        gui.setSlot(4, new GuiElementBuilder(getCryptoDisplayItem(crypto).getItem())
                .setName(Text.literal(crypto.icon + " " + crypto.symbol).formatted(Formatting.BOLD))
                .addLoreLine(Text.literal(crypto.name).formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§6Current Price: $" + CryptoMarket.formatCrypto(price)))
                .addLoreLine(Text.literal("§7Session Change: " + (change >= 0 ? "+" : "") + 
                        String.format("%.2f", change) + "%").formatted(changeColor))
                .glow().build());
        
        double owned = CryptoMarket.getWallet(player.getUuidAsString()).getOrDefault(symbol, 0.0);
        gui.setSlot(13, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Text.literal("Your Holdings").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§7Owned: §f" + CryptoMarket.formatCrypto(owned)))
                .addLoreLine(Text.literal("§7Value: §6$" + CryptoMarket.formatCrypto(price * owned)))
                .build());
        
        gui.setSlot(20, new GuiElementBuilder(Items.LIME_DYE)
                .setName(Text.literal("Buy 0.1 " + symbol).formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal("§7Cost: §6$" + CryptoMarket.formatCrypto(price * 0.1)))
                .build());
        
        gui.setSlot(21, new GuiElementBuilder(Items.LIME_DYE)
                .setName(Text.literal("Buy 1 " + symbol).formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal("§7Cost: §6$" + CryptoMarket.formatCrypto(price)))
                .build());
        
        gui.setSlot(22, new GuiElementBuilder(Items.LIME_DYE)
                .setName(Text.literal("Buy 10 " + symbol).formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal("§7Cost: §6$" + CryptoMarket.formatCrypto(price * 10)))
                .build());
    }

    // ═══════════════════════════════════════════════════════════════
    // MAIN MENU
    // ═══════════════════════════════════════════════════════════════

    public static void openMainMenu(ServerPlayerEntity player) {
        // Close existing GUI if open
        SimpleGui existingGui = openCryptoGuis.remove(player.getUuid());
        if (existingGui != null && existingGui.isOpen()) {
            existingGui.close();
        }
        
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X4, player, false);
        gui.setTitle(Text.literal("🔮 Crypto Market"));

        // Track this GUI
        openCryptoGuis.put(player.getUuid(), gui);
        guiViewType.put(player.getUuid(), "main");

        // Background
        GuiElementBuilder bg = new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE).setName(Text.literal(""));
        for (int i = 0; i < 36; i++) gui.setSlot(i, bg.build());

        // Header with market info
        CryptoMarket.MarketPhase phase = CryptoMarket.getCurrentPhase();
        double fgIndex = CryptoMarket.getFearGreedIndex();
        Formatting fgColor = fgIndex < 40 ? Formatting.RED : (fgIndex < 60 ? Formatting.YELLOW : Formatting.GREEN);
        
        // Get countdown for next price update
        long currentTick = PoliticalServer.server.getTicks();
        String countdown = CryptoMarket.getCountdownString(currentTick);
        
        gui.setSlot(4, new GuiElementBuilder(Items.END_CRYSTAL)
                .setName(Text.literal("🔮 Crypto Market").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal(phase.name).formatted(phase.color))
                .addLoreLine(Text.literal("Fear & Greed: " + String.format("%.0f", fgIndex) + " (" + 
                        CryptoMarket.getFearGreedLabel() + ")").formatted(fgColor))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§7Total Market Cap: §6" + 
                        CryptoMarket.formatCrypto(CryptoMarket.getTotalMarketCap())))
                .addLoreLine(Text.literal("§7Your Wallet: §6" + 
                        CryptoMarket.formatCrypto(CryptoMarket.getWalletValue(player.getUuidAsString())) + " USD"))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§e⏱ Next update: " + countdown).formatted(Formatting.YELLOW))
                .glow().build());

        // Browse Cryptos
        gui.setSlot(10, new GuiElementBuilder(Items.KNOWLEDGE_BOOK)
                .setName(Text.literal("📊 Browse Cryptos").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§7View all cryptocurrencies"))
                .addLoreLine(Text.literal("§7Check prices and charts"))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§eClick to browse"))
                .setCallback((idx, type, action) -> openCryptoBrowser(player, 0))
                .build());

        // My Wallet
        gui.setSlot(12, new GuiElementBuilder(Items.ENDER_CHEST)
                .setName(Text.literal("💼 My Wallet").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§7View your crypto holdings"))
                .addLoreLine(Text.literal("§7Manage your portfolio"))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§eClick to view"))
                .setCallback((idx, type, action) -> openWallet(player))
                .build());

        // Staking
        gui.setSlot(14, new GuiElementBuilder(Items.RECOVERY_COMPASS)
                .setName(Text.literal("🔒 Staking").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§7Earn passive income"))
                .addLoreLine(Text.literal("§7Stake your crypto"))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§eClick to stake"))
                .setCallback((idx, type, action) -> openStaking(player))
                .build());

        // DeFi
        gui.setSlot(16, new GuiElementBuilder(Items.BREWING_STAND)
                .setName(Text.literal("🧪 DeFi Hub").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§7Liquidity Pools"))
                .addLoreLine(Text.literal("§7Token Swaps"))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§eClick to enter"))
                .setCallback((idx, type, action) -> openDeFiHub(player))
                .build());

        // Margin Trading
        gui.setSlot(19, new GuiElementBuilder(Items.DIAMOND_SWORD)
                .setName(Text.literal("📊 Margin Trading").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§7Leveraged positions"))
                .addLoreLine(Text.literal("§7Long & Short"))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§eClick to trade"))
                .setCallback((idx, type, action) -> openMarginTrading(player))
                .build());

        // Top Movers
        gui.setSlot(21, new GuiElementBuilder(Items.SPECTRAL_ARROW)
                .setName(Text.literal("🚀 Top Movers").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§7Biggest gainers & losers"))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§eClick to view"))
                .setCallback((idx, type, action) -> openTopMovers(player))
                .build());

        // Stablecoins
        gui.setSlot(23, new GuiElementBuilder(Items.EMERALD)
                .setName(Text.literal("💎 Stablecoins").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§7EUSD & Gold Standard"))
                .addLoreLine(Text.literal("§7Low volatility"))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§eClick to view"))
                .setCallback((idx, type, action) -> openStablecoins(player))
                .build());

        // Meme Coins
        gui.setSlot(25, new GuiElementBuilder(Items.WOLF_SPAWN_EGG)
                .setName(Text.literal("🐕 Meme Coins").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§7High risk, high reward"))
                .addLoreLine(Text.literal("§7Doge, Chicken, Warden"))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§eClick to gamble"))
                .setCallback((idx, type, action) -> openMemeCoins(player))
                .build());

        gui.open();
    }

    // ═══════════════════════════════════════════════════════════════
    // CRYPTO BROWSER
    // ═══════════════════════════════════════════════════════════════

    public static void openCryptoBrowser(ServerPlayerEntity player, int page) {
        // Close existing GUI if open
        SimpleGui existingGui = openCryptoGuis.remove(player.getUuid());
        if (existingGui != null && existingGui.isOpen()) {
            existingGui.close();
        }
        
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("📊 Browse Cryptos"));

        // Track this GUI
        openCryptoGuis.put(player.getUuid(), gui);
        guiViewType.put(player.getUuid(), "browser");
        guiPage.put(player.getUuid(), page);

        GuiElementBuilder bg = new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE).setName(Text.literal(""));
        for (int i = 0; i < 54; i++) gui.setSlot(i, bg.build());

        gui.setSlot(4, new GuiElementBuilder(Items.KNOWLEDGE_BOOK)
                .setName(Text.literal("📊 All Cryptos").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal("§7Page " + (page + 1)))
                .glow().build());

        CryptoMarket.Crypto[] cryptos = CryptoMarket.Crypto.values();
        int itemsPerPage = 28;
        int startIdx = page * itemsPerPage;
        
        int slot = 10;
        int count = 0;
        
        for (int i = startIdx; i < cryptos.length && count < itemsPerPage; i++) {
            CryptoMarket.Crypto crypto = cryptos[i];
            double price = CryptoMarket.getPrice(crypto.symbol);
            double change = CryptoMarket.calculateChange(crypto.symbol);
            
            Formatting changeColor = change >= 0 ? Formatting.GREEN : Formatting.RED;
            
            final String symbol = crypto.symbol;
            gui.setSlot(slot, new GuiElementBuilder(getCryptoDisplayItem(crypto).getItem())
                    .setName(Text.literal(crypto.icon + " " + crypto.symbol).formatted(Formatting.BOLD))
                    .addLoreLine(Text.literal(crypto.name).formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("§6Price: $" + CryptoMarket.formatCrypto(price)))
                    .addLoreLine(Text.literal("§7Change: " + (change >= 0 ? "+" : "") + 
                            String.format("%.2f", change) + "%").formatted(changeColor))
                    .addLoreLine(Text.literal("§7Market Cap: $" + 
                            CryptoMarket.formatCrypto(CryptoMarket.getMarketCap(symbol))))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("§eClick to trade"))
                    .setCallback((idx, type, action) -> openCryptoDetail(player, symbol))
                    .build());
            
            count++;
            slot++;
            if (slot == 17) slot = 19;
            else if (slot == 26) slot = 28;
            else if (slot == 35) slot = 37;
            else if (slot == 44) slot = 46;
        }

        // Navigation
        if (page > 0) {
            final int prev = page - 1;
            gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("← Previous Page").formatted(Formatting.YELLOW))
                    .setCallback((idx, type, action) -> openCryptoBrowser(player, prev))
                    .build());
        }
        
        if ((page + 1) * itemsPerPage < cryptos.length) {
            final int next = page + 1;
            gui.setSlot(53, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("Next Page →").formatted(Formatting.YELLOW))
                    .setCallback((idx, type, action) -> openCryptoBrowser(player, next))
                    .build());
        }

        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openMainMenu(player))
                .build());

        gui.open();
    }

    // ═══════════════════════════════════════════════════════════════
    // CRYPTO DETAIL & TRADING
    // ═══════════════════════════════════════════════════════════════

    public static void openCryptoDetail(ServerPlayerEntity player, String symbol) {
        CryptoMarket.Crypto crypto = CryptoMarket.getCryptoBySymbol(symbol);
        if (crypto == null) return;

        // Close existing GUI if open
        SimpleGui existingGui = openCryptoGuis.remove(player.getUuid());
        if (existingGui != null && existingGui.isOpen()) {
            existingGui.close();
        }

        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X4, player, false);
        gui.setTitle(Text.literal(crypto.symbol + " Trading"));

        // Track this GUI
        openCryptoGuis.put(player.getUuid(), gui);
        guiViewType.put(player.getUuid(), "detail");
        guiSymbol.put(player.getUuid(), symbol);

        GuiElementBuilder bg = new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE).setName(Text.literal(""));
        for (int i = 0; i < 36; i++) gui.setSlot(i, bg.build());

        double price = CryptoMarket.getPrice(symbol);
        double change = CryptoMarket.calculateChange(symbol);
        double change24h = CryptoMarket.calculate24hChange(symbol);
        
        Formatting changeColor = change >= 0 ? Formatting.GREEN : Formatting.RED;

        // Crypto info
        gui.setSlot(4, new GuiElementBuilder(getCryptoDisplayItem(crypto).getItem())
                .setName(Text.literal(crypto.icon + " " + crypto.symbol).formatted(Formatting.BOLD))
                .addLoreLine(Text.literal(crypto.name).formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§6Price: $" + CryptoMarket.formatCrypto(price)))
                .addLoreLine(Text.literal("§7Change: " + (change >= 0 ? "+" : "") + 
                        String.format("%.2f", change) + "%").formatted(changeColor))
                .addLoreLine(Text.literal("§724h: " + (change24h >= 0 ? "+" : "") + 
                        String.format("%.2f", change24h) + "%").formatted(changeColor))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§7Market Cap: $" + 
                        CryptoMarket.formatCrypto(CryptoMarket.getMarketCap(symbol))))
                .addLoreLine(Text.literal("§7Supply: " + 
                        CryptoMarket.formatCrypto(CryptoMarket.getCirculatingSupply(symbol))))
                .addLoreLine(Text.literal("§7Max Supply: " + (crypto.maxSupply > 0 ? 
                        CryptoMarket.formatCrypto(crypto.maxSupply) : "Unlimited")))
                .glow().build());

        // Player's balance
        double balance = CryptoMarket.getBalance(player.getUuidAsString(), symbol);
        double staked = CryptoMarket.getStakedAmount(player.getUuidAsString(), symbol);
        gui.setSlot(13, new GuiElementBuilder(Items.ENDER_CHEST)
                .setName(Text.literal("Your Holdings").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§7Balance: §f" + CryptoMarket.formatCrypto(balance)))
                .addLoreLine(Text.literal("§7Staked: §f" + CryptoMarket.formatCrypto(staked)))
                .addLoreLine(Text.literal("§6Value: $" + CryptoMarket.formatCrypto(price * (balance + staked))))
                .build());

        // Buy buttons
        final String sym = symbol;
        gui.setSlot(20, new GuiElementBuilder(Items.LIME_DYE)
                .setName(Text.literal("Buy 1 " + symbol).formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal("§7Cost: §6" + CryptoMarket.formatCrypto(price * 1000) + " coins"))
                .setCallback((idx, type, action) -> {
                    CryptoMarket.buyCrypto(player, sym, 1);
                    openCryptoDetail(player, sym);
                })
                .build());

        gui.setSlot(21, new GuiElementBuilder(Items.LIME_DYE)
                .setName(Text.literal("Buy 10 " + symbol).formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal("§7Cost: §6" + CryptoMarket.formatCrypto(price * 10 * 1000) + " coins"))
                .setCallback((idx, type, action) -> {
                    CryptoMarket.buyCrypto(player, sym, 10);
                    openCryptoDetail(player, sym);
                })
                .build());

        gui.setSlot(22, new GuiElementBuilder(Items.LIME_DYE)
                .setName(Text.literal("Buy 100 " + symbol).formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal("§7Cost: §6" + CryptoMarket.formatCrypto(price * 100 * 1000) + " coins"))
                .setCallback((idx, type, action) -> {
                    CryptoMarket.buyCrypto(player, sym, 100);
                    openCryptoDetail(player, sym);
                })
                .build());

        // Custom buy button with sign input
        gui.setSlot(23, new GuiElementBuilder(Items.WRITABLE_BOOK)
                .setName(Text.literal("Custom Buy").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal("§7Enter custom amount"))
                .addLoreLine(Text.literal("§7Supports k/m suffixes"))
                .addLoreLine(Text.literal("§eClick to enter amount"))
                .setCallback((idx, type, action) -> openCustomBuySign(player, sym))
                .build());

        // Sell buttons
        if (balance > 0) {
            gui.setSlot(24, new GuiElementBuilder(Items.RED_DYE)
                    .setName(Text.literal("Sell 1 " + symbol).formatted(Formatting.RED, Formatting.BOLD))
                    .addLoreLine(Text.literal("§7Value: §6" + CryptoMarket.formatCrypto(price * 1000) + " coins"))
                    .setCallback((idx, type, action) -> {
                        CryptoMarket.sellCrypto(player, sym, 1);
                        openCryptoDetail(player, sym);
                    })
                    .build());

            gui.setSlot(25, new GuiElementBuilder(Items.RED_DYE)
                    .setName(Text.literal("Sell All").formatted(Formatting.RED, Formatting.BOLD))
                    .addLoreLine(Text.literal("§7Value: §6" + CryptoMarket.formatCrypto(price * balance * 1000) + " coins"))
                    .setCallback((idx, type, action) -> {
                        CryptoMarket.sellCrypto(player, sym, balance);
                        openCryptoDetail(player, sym);
                    })
                    .build());

            // Custom sell button with sign input
            gui.setSlot(26, new GuiElementBuilder(Items.WRITABLE_BOOK)
                    .setName(Text.literal("Custom Sell").formatted(Formatting.YELLOW, Formatting.BOLD))
                    .addLoreLine(Text.literal("§7Enter custom amount"))
                    .addLoreLine(Text.literal("§7Supports k/m suffixes"))
                    .addLoreLine(Text.literal("§eClick to enter amount"))
                    .setCallback((idx, type, action) -> openCustomSellSign(player, sym, balance))
                    .build());
        }

        // Stake button
        if (CryptoMarket.getStakingApy(symbol) > 0) {
            double apy = CryptoMarket.getStakingApy(symbol) * 100;
            gui.setSlot(30, new GuiElementBuilder(Items.RECOVERY_COMPASS)
                    .setName(Text.literal("🔒 Stake " + symbol).formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("§a" + String.format("%.1f", apy) + "% APY"))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("§eClick to stake"))
                    .setCallback((idx, type, action) -> openStakingDetail(player, sym))
                    .build());
        }

        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openCryptoBrowser(player, 0))
                .build());

        // Price graph button
        gui.setSlot(32, new GuiElementBuilder(Items.SPYGLASS)
                .setName(Text.literal("📊 Price Graph").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§7View price history chart"))
                .addLoreLine(Text.literal("§eClick to view"))
                .setCallback((idx, type, action) -> openPriceGraph(player, sym))
                .build());

        gui.open();
    }

    // ═══════════════════════════════════════════════════════════════
    // WALLET VIEW
    // ═══════════════════════════════════════════════════════════════

    public static void openWallet(ServerPlayerEntity player) {
        // Close existing GUI if open
        SimpleGui existingGui = openCryptoGuis.remove(player.getUuid());
        if (existingGui != null && existingGui.isOpen()) {
            existingGui.close();
        }
        
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("💼 My Wallet"));

        // Track this GUI
        openCryptoGuis.put(player.getUuid(), gui);
        guiViewType.put(player.getUuid(), "wallet");

        GuiElementBuilder bg = new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE).setName(Text.literal(""));
        for (int i = 0; i < 54; i++) gui.setSlot(i, bg.build());

        Map<String, Double> wallet = CryptoMarket.getWallet(player.getUuidAsString());
        double totalValue = CryptoMarket.getWalletValue(player.getUuidAsString());

        gui.setSlot(4, new GuiElementBuilder(Items.ENDER_CHEST)
                .setName(Text.literal("💼 Your Wallet").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§6Total Value: $" + CryptoMarket.formatCrypto(totalValue)))
                .addLoreLine(Text.literal("§7Holdings: " + wallet.size() + " different cryptos"))
                .glow().build());

        if (wallet.isEmpty()) {
            gui.setSlot(22, new GuiElementBuilder(Items.BARRIER)
                    .setName(Text.literal("Empty Wallet").formatted(Formatting.RED))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("§7You don't own any crypto yet."))
                    .addLoreLine(Text.literal("§7Browse cryptos to start trading!"))
                    .build());
        } else {
            List<Map.Entry<String, Double>> entries = new ArrayList<>(wallet.entrySet());
            entries.sort((a, b) -> {
                double valA = CryptoMarket.getPrice(a.getKey()) * a.getValue();
                double valB = CryptoMarket.getPrice(b.getKey()) * b.getValue();
                return Double.compare(valB, valA);
            });
            
            int slot = 10;
            for (int i = 0; i < entries.size() && slot < 44; i++) {
                Map.Entry<String, Double> entry = entries.get(i);
                if (entry.getValue() <= 0) continue;
                
                CryptoMarket.Crypto crypto = CryptoMarket.getCryptoBySymbol(entry.getKey());
                if (crypto == null) continue;
                
                double price = CryptoMarket.getPrice(entry.getKey());
                double value = price * entry.getValue();
                double change = CryptoMarket.calculateChange(entry.getKey());
                
                Formatting changeColor = change >= 0 ? Formatting.GREEN : Formatting.RED;
                
                final String symbol = entry.getKey();
                gui.setSlot(slot, new GuiElementBuilder(getCryptoDisplayItem(crypto).getItem())
                        .setName(Text.literal(crypto.icon + " " + crypto.symbol).formatted(Formatting.BOLD))
                        .addLoreLine(Text.literal("§7Balance: §f" + CryptoMarket.formatCrypto(entry.getValue())))
                        .addLoreLine(Text.literal("§6Value: $" + CryptoMarket.formatCrypto(value)))
                        .addLoreLine(Text.literal("§7Change: " + (change >= 0 ? "+" : "") + 
                                String.format("%.2f", change) + "%").formatted(changeColor))
                        .addLoreLine(Text.literal(""))
                        .addLoreLine(Text.literal("§eClick to trade"))
                        .setCallback((idx, type, action) -> openCryptoDetail(player, symbol))
                        .build());
                
                slot++;
                if (slot == 17) slot = 19;
                else if (slot == 26) slot = 28;
                else if (slot == 35) slot = 37;
            }
        }

        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openMainMenu(player))
                .build());

        gui.open();
    }

    // ═══════════════════════════════════════════════════════════════
    // STAKING
    // ═══════════════════════════════════════════════════════════════

    public static void openStaking(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
        gui.setTitle(Text.literal("🔒 Staking"));

        GuiElementBuilder bg = new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE).setName(Text.literal(""));
        for (int i = 0; i < 27; i++) gui.setSlot(i, bg.build());

        gui.setSlot(4, new GuiElementBuilder(Items.RECOVERY_COMPASS)
                .setName(Text.literal("🔒 Staking").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§7Earn passive income by staking"))
                .glow().build());

        int slot = 10;
        for (CryptoMarket.Crypto crypto : CryptoMarket.Crypto.values()) {
            double apy = CryptoMarket.getStakingApy(crypto.symbol);
            if (apy <= 0) continue;
            
            final String symbol = crypto.symbol;
            double staked = CryptoMarket.getStakedAmount(player.getUuidAsString(), symbol);
            
            gui.setSlot(slot, new GuiElementBuilder(getCryptoDisplayItem(crypto).getItem())
                    .setName(Text.literal(crypto.icon + " " + crypto.symbol).formatted(Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("§a" + String.format("%.1f", apy * 100) + "% APY"))
                    .addLoreLine(Text.literal("§7Staked: " + CryptoMarket.formatCrypto(staked)))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("§eClick to manage"))
                    .setCallback((idx, type, action) -> openStakingDetail(player, symbol))
                    .build());
            
            slot++;
            if (slot > 16) break;
        }

        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openMainMenu(player))
                .build());

        gui.open();
    }

    public static void openStakingDetail(ServerPlayerEntity player, String symbol) {
        CryptoMarket.Crypto crypto = CryptoMarket.getCryptoBySymbol(symbol);
        if (crypto == null) return;

        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
        gui.setTitle(Text.literal("🔒 Stake " + symbol));

        GuiElementBuilder bg = new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE).setName(Text.literal(""));
        for (int i = 0; i < 27; i++) gui.setSlot(i, bg.build());

        double apy = CryptoMarket.getStakingApy(symbol);
        double balance = CryptoMarket.getBalance(player.getUuidAsString(), symbol);
        double staked = CryptoMarket.getStakedAmount(player.getUuidAsString(), symbol);

        gui.setSlot(4, new GuiElementBuilder(getCryptoDisplayItem(crypto).getItem())
                .setName(Text.literal(crypto.icon + " " + symbol + " Staking").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§a" + String.format("%.1f", apy * 100) + "% Annual Yield"))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§7Balance: " + CryptoMarket.formatCrypto(balance)))
                .addLoreLine(Text.literal("§7Staked: " + CryptoMarket.formatCrypto(staked)))
                .glow().build());

        final String sym = symbol;
        
        // Stake buttons
        if (balance >= 1) {
            gui.setSlot(10, new GuiElementBuilder(Items.LIME_DYE)
                    .setName(Text.literal("Stake 1 " + symbol).formatted(Formatting.GREEN))
                    .setCallback((idx, type, action) -> {
                        CryptoMarket.stakeCrypto(player, sym, 1);
                        openStakingDetail(player, sym);
                    })
                    .build());
        }
        
        if (balance >= 10) {
            gui.setSlot(11, new GuiElementBuilder(Items.LIME_DYE)
                    .setName(Text.literal("Stake 10 " + symbol).formatted(Formatting.GREEN))
                    .setCallback((idx, type, action) -> {
                        CryptoMarket.stakeCrypto(player, sym, 10);
                        openStakingDetail(player, sym);
                    })
                    .build());
        }
        
        if (balance >= 100) {
            gui.setSlot(12, new GuiElementBuilder(Items.LIME_DYE)
                    .setName(Text.literal("Stake All").formatted(Formatting.GREEN))
                    .setCallback((idx, type, action) -> {
                        CryptoMarket.stakeCrypto(player, sym, balance);
                        openStakingDetail(player, sym);
                    })
                    .build());
        }

        // Unstake buttons
        if (staked >= 1) {
            gui.setSlot(14, new GuiElementBuilder(Items.RED_DYE)
                    .setName(Text.literal("Unstake 1 " + symbol).formatted(Formatting.RED))
                    .setCallback((idx, type, action) -> {
                        CryptoMarket.unstakeCrypto(player, sym, 1);
                        openStakingDetail(player, sym);
                    })
                    .build());
            
            gui.setSlot(15, new GuiElementBuilder(Items.RED_DYE)
                    .setName(Text.literal("Unstake All").formatted(Formatting.RED))
                    .setCallback((idx, type, action) -> {
                        CryptoMarket.unstakeCrypto(player, sym, staked);
                        openStakingDetail(player, sym);
                    })
                    .build());
        }

        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openStaking(player))
                .build());

        gui.open();
    }

    // ═══════════════════════════════════════════════════════════════
    // DEFI HUB
    // ═══════════════════════════════════════════════════════════════

    public static void openDeFiHub(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
        gui.setTitle(Text.literal("🧪 DeFi Hub"));

        GuiElementBuilder bg = new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE).setName(Text.literal(""));
        for (int i = 0; i < 27; i++) gui.setSlot(i, bg.build());

        gui.setSlot(4, new GuiElementBuilder(Items.BREWING_STAND)
                .setName(Text.literal("🧪 DeFi Hub").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§7Decentralized Finance"))
                .glow().build());

        // Swap
        gui.setSlot(10, new GuiElementBuilder(Items.SPECTRAL_ARROW)
                .setName(Text.literal("🔄 Token Swap").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§7Swap tokens instantly"))
                .addLoreLine(Text.literal("§7Automated Market Maker"))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§eClick to swap"))
                .setCallback((idx, type, action) -> openSwapMenu(player))
                .build());

        // Liquidity Pools
        gui.setSlot(12, new GuiElementBuilder(Items.WATER_BUCKET)
                .setName(Text.literal("💧 Liquidity Pools").formatted(Formatting.BLUE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§7Provide liquidity"))
                .addLoreLine(Text.literal("§7Earn trading fees"))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§eClick to view"))
                .setCallback((idx, type, action) -> openLiquidityPools(player))
                .build());

        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openMainMenu(player))
                .build());

        gui.open();
    }

    public static void openSwapMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
        gui.setTitle(Text.literal("🔄 Token Swap"));

        GuiElementBuilder bg = new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE).setName(Text.literal(""));
        for (int i = 0; i < 27; i++) gui.setSlot(i, bg.build());

        gui.setSlot(4, new GuiElementBuilder(Items.SPECTRAL_ARROW)
                .setName(Text.literal("🔄 Swap Tokens").formatted(Formatting.AQUA, Formatting.BOLD))
                .glow().build());

        // Common swaps
        String[][] commonPairs = {
            {"END", "EUSD"}, {"NTH", "EUSD"}, {"OVW", "EUSD"},
            {"END", "NTH"}, {"RSC", "EUSD"}, {"YLD", "EUSD"}
        };
        
        int slot = 10;
        for (String[] pair : commonPairs) {
            final String from = pair[0];
            final String to = pair[1];
            
            CryptoMarket.Crypto fromCrypto = CryptoMarket.getCryptoBySymbol(from);
            CryptoMarket.Crypto toCrypto = CryptoMarket.getCryptoBySymbol(to);
            if (fromCrypto == null || toCrypto == null) continue;
            
            double balance = CryptoMarket.getBalance(player.getUuidAsString(), from);
            
            gui.setSlot(slot, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal(fromCrypto.icon + " " + from + " → " + toCrypto.icon + " " + to)
                            .formatted(Formatting.AQUA))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("§7Balance: " + CryptoMarket.formatCrypto(balance) + " " + from))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("§eClick to swap"))
                    .setCallback((idx, type, action) -> openSwapDetail(player, from, to))
                    .build());
            
            slot++;
            if (slot > 16) break;
        }

        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openDeFiHub(player))
                .build());

        gui.open();
    }

    public static void openSwapDetail(ServerPlayerEntity player, String from, String to) {
        CryptoMarket.Crypto fromCrypto = CryptoMarket.getCryptoBySymbol(from);
        CryptoMarket.Crypto toCrypto = CryptoMarket.getCryptoBySymbol(to);
        if (fromCrypto == null || toCrypto == null) return;

        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
        gui.setTitle(Text.literal("Swap " + from + " → " + to));

        GuiElementBuilder bg = new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE).setName(Text.literal(""));
        for (int i = 0; i < 27; i++) gui.setSlot(i, bg.build());

        double balance = CryptoMarket.getBalance(player.getUuidAsString(), from);
        double fromPrice = CryptoMarket.getPrice(from);
        double toPrice = CryptoMarket.getPrice(to);
        double rate = fromPrice / toPrice;

        gui.setSlot(4, new GuiElementBuilder(Items.SPECTRAL_ARROW)
                .setName(Text.literal(fromCrypto.icon + " " + from + " → " + toCrypto.icon + " " + to)
                        .formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§7Rate: 1 " + from + " = " + CryptoMarket.formatCrypto(rate) + " " + to))
                .addLoreLine(Text.literal("§7Balance: " + CryptoMarket.formatCrypto(balance) + " " + from))
                .glow().build());

        final String fromSym = from;
        final String toSym = to;

        if (balance >= 1) {
            gui.setSlot(10, new GuiElementBuilder(Items.LIME_DYE)
                    .setName(Text.literal("Swap 1 " + from).formatted(Formatting.GREEN))
                    .addLoreLine(Text.literal("§7→ " + CryptoMarket.formatCrypto(rate) + " " + to))
                    .setCallback((idx, type, action) -> {
                        CryptoMarket.swapTokens(player, fromSym, toSym, 1);
                        openSwapDetail(player, fromSym, toSym);
                    })
                    .build());
        }
        
        if (balance >= 10) {
            gui.setSlot(11, new GuiElementBuilder(Items.LIME_DYE)
                    .setName(Text.literal("Swap 10 " + from).formatted(Formatting.GREEN))
                    .addLoreLine(Text.literal("§7→ " + CryptoMarket.formatCrypto(rate * 10) + " " + to))
                    .setCallback((idx, type, action) -> {
                        CryptoMarket.swapTokens(player, fromSym, toSym, 10);
                        openSwapDetail(player, fromSym, toSym);
                    })
                    .build());
        }
        
        if (balance > 0) {
            gui.setSlot(12, new GuiElementBuilder(Items.LIME_DYE)
                    .setName(Text.literal("Swap All").formatted(Formatting.GREEN))
                    .addLoreLine(Text.literal("§7→ " + CryptoMarket.formatCrypto(rate * balance) + " " + to))
                    .setCallback((idx, type, action) -> {
                        CryptoMarket.swapTokens(player, fromSym, toSym, balance);
                        openSwapDetail(player, fromSym, toSym);
                    })
                    .build());
        }

        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openSwapMenu(player))
                .build());

        gui.open();
    }

    public static void openLiquidityPools(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
        gui.setTitle(Text.literal("💧 Liquidity Pools"));

        GuiElementBuilder bg = new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE).setName(Text.literal(""));
        for (int i = 0; i < 27; i++) gui.setSlot(i, bg.build());

        gui.setSlot(4, new GuiElementBuilder(Items.WATER_BUCKET)
                .setName(Text.literal("💧 Liquidity Pools").formatted(Formatting.BLUE, Formatting.BOLD))
                .glow().build());

        int slot = 10;
        for (Map.Entry<String, CryptoMarket.LiquidityPool> entry : CryptoMarket.getLiquidityPools().entrySet()) {
            String poolId = entry.getKey();
            CryptoMarket.LiquidityPool pool = entry.getValue();
            
            gui.setSlot(slot, new GuiElementBuilder(Items.WATER_BUCKET)
                    .setName(Text.literal(poolId).formatted(Formatting.AQUA))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("§7Reserves:"))
                    .addLoreLine(Text.literal("§f" + CryptoMarket.formatCrypto(pool.reserveA) + " " + pool.tokenA))
                    .addLoreLine(Text.literal("§f" + CryptoMarket.formatCrypto(pool.reserveB) + " " + pool.tokenB))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("§eClick to provide liquidity"))
                    .build());
            
            slot++;
            if (slot > 16) break;
        }

        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openDeFiHub(player))
                .build());

        gui.open();
    }

    // ═══════════════════════════════════════════════════════════════
    // MARGIN TRADING
    // ═══════════════════════════════════════════════════════════════

    public static void openMarginTrading(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
        gui.setTitle(Text.literal("📊 Margin Trading"));

        GuiElementBuilder bg = new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE).setName(Text.literal(""));
        for (int i = 0; i < 27; i++) gui.setSlot(i, bg.build());

        gui.setSlot(4, new GuiElementBuilder(Items.DIAMOND_SWORD)
                .setName(Text.literal("📊 Margin Trading").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§cHigh Risk - High Reward"))
                .addLoreLine(Text.literal("§7Trade with leverage"))
                .glow().build());

        // Show active positions
        Map<String, CryptoMarket.MarginPosition> positions = CryptoMarket.getMarginPositions(player.getUuidAsString());
        if (!positions.isEmpty()) {
            int slot = 10;
            for (Map.Entry<String, CryptoMarket.MarginPosition> entry : positions.entrySet()) {
                CryptoMarket.MarginPosition pos = entry.getValue();
                double currentPrice = CryptoMarket.getPrice(pos.symbol);
                
                double pnl = pos.isLong ? 
                        (currentPrice - pos.entryPrice) / pos.entryPrice * pos.size * pos.leverage :
                        (pos.entryPrice - currentPrice) / pos.entryPrice * pos.size * pos.leverage;
                
                Formatting pnlColor = pnl >= 0 ? Formatting.GREEN : Formatting.RED;
                
                final String key = entry.getKey();
                gui.setSlot(slot, new GuiElementBuilder(pos.isLong ? Items.LIME_DYE : Items.RED_DYE)
                        .setName(Text.literal((pos.isLong ? "📈 " : "📉 ") + pos.symbol + " " + 
                                (int)pos.leverage + "x").formatted(Formatting.BOLD))
                        .addLoreLine(Text.literal(""))
                        .addLoreLine(Text.literal("§7Size: " + CryptoMarket.formatCrypto(pos.size)))
                        .addLoreLine(Text.literal("§7Entry: $" + CryptoMarket.formatCrypto(pos.entryPrice)))
                        .addLoreLine(Text.literal("§7Current: $" + CryptoMarket.formatCrypto(currentPrice)))
                        .addLoreLine(Text.literal("§7Liq: $" + CryptoMarket.formatCrypto(pos.liquidationPrice)))
                        .addLoreLine(Text.literal(""))
                        .addLoreLine(Text.literal("PnL: " + (pnl >= 0 ? "+" : "") + 
                                CryptoMarket.formatCrypto(pnl) + " EUSD").formatted(pnlColor))
                        .addLoreLine(Text.literal(""))
                        .addLoreLine(Text.literal("§eClick to close"))
                        .setCallback((idx, type, action) -> {
                            CryptoMarket.closeMarginPosition(player, key);
                            openMarginTrading(player);
                        })
                        .build());
                
                slot++;
                if (slot > 16) break;
            }
        } else {
            gui.setSlot(13, new GuiElementBuilder(Items.BARRIER)
                    .setName(Text.literal("No Active Positions").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("§7Open a position from a crypto page"))
                    .build());
        }

        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openMainMenu(player))
                .build());

        gui.open();
    }

    // ═══════════════════════════════════════════════════════════════
    // TOP MOVERS
    // ═══════════════════════════════════════════════════════════════

    public static void openTopMovers(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
        gui.setTitle(Text.literal("🚀 Top Movers"));

        GuiElementBuilder bg = new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE).setName(Text.literal(""));
        for (int i = 0; i < 27; i++) gui.setSlot(i, bg.build());

        gui.setSlot(4, new GuiElementBuilder(Items.SPECTRAL_ARROW)
                .setName(Text.literal("🚀 Top Movers").formatted(Formatting.YELLOW, Formatting.BOLD))
                .glow().build());

        List<Map.Entry<String, Double>> changes = new ArrayList<>();
        for (CryptoMarket.Crypto crypto : CryptoMarket.Crypto.values()) {
            changes.add(Map.entry(crypto.symbol, CryptoMarket.calculateChange(crypto.symbol)));
        }
        changes.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        // Top gainers
        for (int i = 0; i < 3 && i < changes.size(); i++) {
            Map.Entry<String, Double> entry = changes.get(i);
            CryptoMarket.Crypto crypto = CryptoMarket.getCryptoBySymbol(entry.getKey());
            if (crypto == null) continue;
            
            final String symbol = entry.getKey();
            gui.setSlot(10 + i, new GuiElementBuilder(Items.LIME_DYE)
                    .setName(Text.literal("📈 " + crypto.symbol).formatted(Formatting.GREEN, Formatting.BOLD))
                    .addLoreLine(Text.literal(crypto.name).formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("§a+" + String.format("%.2f", entry.getValue()) + "%"))
                    .addLoreLine(Text.literal("§6$" + CryptoMarket.formatCrypto(CryptoMarket.getPrice(symbol))))
                    .setCallback((idx, type, action) -> openCryptoDetail(player, symbol))
                    .build());
        }

        // Top losers
        for (int i = 0; i < 3 && i < changes.size(); i++) {
            int idx = changes.size() - 1 - i;
            Map.Entry<String, Double> entry = changes.get(idx);
            CryptoMarket.Crypto crypto = CryptoMarket.getCryptoBySymbol(entry.getKey());
            if (crypto == null) continue;
            
            final String symbol = entry.getKey();
            gui.setSlot(14 + i, new GuiElementBuilder(Items.RED_DYE)
                    .setName(Text.literal("📉 " + crypto.symbol).formatted(Formatting.RED, Formatting.BOLD))
                    .addLoreLine(Text.literal(crypto.name).formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("§c" + String.format("%.2f", entry.getValue()) + "%"))
                    .addLoreLine(Text.literal("§6$" + CryptoMarket.formatCrypto(CryptoMarket.getPrice(symbol))))
                    .setCallback((idx2, type, action) -> openCryptoDetail(player, symbol))
                    .build());
        }

        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openMainMenu(player))
                .build());

        gui.open();
    }

    // ═══════════════════════════════════════════════════════════════
    // STABLECOINS & MEMECOINS
    // ═══════════════════════════════════════════════════════════════

    public static void openStablecoins(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
        gui.setTitle(Text.literal("💎 Stablecoins"));

        GuiElementBuilder bg = new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE).setName(Text.literal(""));
        for (int i = 0; i < 27; i++) gui.setSlot(i, bg.build());

        gui.setSlot(4, new GuiElementBuilder(Items.EMERALD)
                .setName(Text.literal("💎 Stablecoins").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§7Low volatility, stable value"))
                .glow().build());

        String[] stablecoins = {"EUSD", "GSTD"};
        int slot = 10;
        for (String symbol : stablecoins) {
            CryptoMarket.Crypto crypto = CryptoMarket.getCryptoBySymbol(symbol);
            if (crypto == null) continue;
            
            double price = CryptoMarket.getPrice(symbol);
            double balance = CryptoMarket.getBalance(player.getUuidAsString(), symbol);
            
            final String sym = symbol;
            gui.setSlot(slot, new GuiElementBuilder(Items.EMERALD)
                    .setName(Text.literal(crypto.icon + " " + symbol).formatted(Formatting.BOLD))
                    .addLoreLine(Text.literal(crypto.name).formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("§6Price: $" + CryptoMarket.formatCrypto(price)))
                    .addLoreLine(Text.literal("§7Balance: " + CryptoMarket.formatCrypto(balance)))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("§eClick to trade"))
                    .setCallback((idx, type, action) -> openCryptoDetail(player, sym))
                    .build());
            
            slot++;
        }

        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openMainMenu(player))
                .build());

        gui.open();
    }

    public static void openMemeCoins(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
        gui.setTitle(Text.literal("🐕 Meme Coins"));

        GuiElementBuilder bg = new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE).setName(Text.literal(""));
        for (int i = 0; i < 27; i++) gui.setSlot(i, bg.build());

        gui.setSlot(4, new GuiElementBuilder(Items.WOLF_SPAWN_EGG)
                .setName(Text.literal("🐕 Meme Coins").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§c⚠ Extreme Volatility"))
                .addLoreLine(Text.literal("§7High risk, potential high reward"))
                .glow().build());

        String[] memecoins = {"DOGE", "CHK", "WRD"};
        int slot = 10;
        for (String symbol : memecoins) {
            CryptoMarket.Crypto crypto = CryptoMarket.getCryptoBySymbol(symbol);
            if (crypto == null) continue;
            
            double price = CryptoMarket.getPrice(symbol);
            double change = CryptoMarket.calculateChange(symbol);
            Formatting changeColor = change >= 0 ? Formatting.GREEN : Formatting.RED;
            
            final String sym = symbol;
            gui.setSlot(slot, new GuiElementBuilder(Items.WOLF_SPAWN_EGG)
                    .setName(Text.literal(crypto.icon + " " + symbol).formatted(Formatting.BOLD))
                    .addLoreLine(Text.literal(crypto.name).formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("§6Price: $" + CryptoMarket.formatCrypto(price)))
                    .addLoreLine(Text.literal("§7Change: " + (change >= 0 ? "+" : "") + 
                            String.format("%.2f", change) + "%").formatted(changeColor))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("§eClick to gamble"))
                    .setCallback((idx, type, action) -> openCryptoDetail(player, sym))
                    .build());
            
            slot++;
        }

        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openMainMenu(player))
                .build());

        gui.open();
    }

    // ═══════════════════════════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════════════════════════

    public static ItemStack getCryptoDisplayItem(CryptoMarket.Crypto crypto) {
        return switch (crypto) {
            case ENDERCOIN -> new ItemStack(Items.END_CRYSTAL);
            case NETHERCOIN -> new ItemStack(Items.NETHER_STAR);
            case OVERWORLD -> new ItemStack(Items.GRASS_BLOCK);
            case REDSTONE_CHAIN -> new ItemStack(Items.REDSTONE_BLOCK);
            case SLIME_CHAIN -> new ItemStack(Items.SLIME_BLOCK);
            case YIELD_FARM -> new ItemStack(Items.WHEAT);
            case LIQUIDITY_POOL -> new ItemStack(Items.WATER_BUCKET);
            case SWAP_TOKEN -> new ItemStack(Items.ARROW);
            case DOGE_COIN -> new ItemStack(Items.WOLF_SPAWN_EGG);
            case CHICKEN_COIN -> new ItemStack(Items.CHICKEN_SPAWN_EGG);
            case WARDEN_COIN -> new ItemStack(Items.SCULK_SENSOR);
            case EMERALD_USD -> new ItemStack(Items.EMERALD);
            case GOLD_STANDARD -> new ItemStack(Items.GOLD_INGOT);
            case SCULK_SHADOW -> new ItemStack(Items.SCULK_SHRIEKER);
            case PHANTOM_COIN -> new ItemStack(Items.PHANTOM_MEMBRANE);
            case BLOCK_TOKEN -> new ItemStack(Items.STONE);
            case CRAFT_COIN -> new ItemStack(Items.CRAFTING_TABLE);
            case REALM_TOKEN -> new ItemStack(Items.NETHERITE_BLOCK);
            case PORTAL_LINK -> new ItemStack(Items.RESPAWN_ANCHOR);
            case BEACON_CHAIN -> new ItemStack(Items.BEACON);
            case DRAGON_GOV -> new ItemStack(Items.DRAGON_HEAD);
            case ART_TOKEN -> new ItemStack(Items.PAINTING);
        };
    }

    // ═══════════════════════════════════════════════════════════════
    // SIGN GUI METHODS FOR CUSTOM BUY/SELL
    // ═══════════════════════════════════════════════════════════════

    private static void openCustomBuySign(ServerPlayerEntity player, String symbol) {
        SignGui signGui = new SignGui(player) {
            @Override
            public void onClose() {
                String input = this.getLine(0).getString().trim();
                double amount = parseAmountWithSuffix(input);
                if (amount <= 0) {
                    player.sendMessage(Text.literal("✗ Invalid amount!").formatted(Formatting.RED), false);
                    openCryptoDetail(player, symbol);
                    return;
                }
                CryptoMarket.buyCrypto(player, symbol, amount);
                openCryptoDetail(player, symbol);
            }
        };
        signGui.setLine(0, Text.literal("Enter amount:"));
        signGui.setLine(1, Text.literal("(k=1000, m=1000000)"));
        signGui.open();
    }

    private static void openCustomSellSign(ServerPlayerEntity player, String symbol, double maxAmount) {
        SignGui signGui = new SignGui(player) {
            @Override
            public void onClose() {
                String input = this.getLine(0).getString().trim();
                double amount = parseAmountWithSuffix(input);
                if (amount <= 0) {
                    player.sendMessage(Text.literal("✗ Invalid amount!").formatted(Formatting.RED), false);
                    openCryptoDetail(player, symbol);
                    return;
                }
                if (amount > maxAmount) {
                    player.sendMessage(Text.literal("✗ You only have " + CryptoMarket.formatCrypto(maxAmount) + "!").formatted(Formatting.RED), false);
                    openCryptoDetail(player, symbol);
                    return;
                }
                CryptoMarket.sellCrypto(player, symbol, amount);
                openCryptoDetail(player, symbol);
            }
        };
        signGui.setLine(0, Text.literal("Enter amount:"));
        signGui.setLine(1, Text.literal("Max: " + CryptoMarket.formatCrypto(maxAmount)));
        signGui.open();
    }

    /**
     * Parse amount with k/m suffixes (e.g., "10k" = 10000, "5m" = 5000000)
     */
    private static double parseAmountWithSuffix(String input) {
        if (input == null || input.isEmpty()) return 0;
        
        input = input.toLowerCase().trim().replaceAll("[^0-9.km]", "");
        if (input.isEmpty()) return 0;
        
        try {
            if (input.endsWith("m")) {
                String num = input.substring(0, input.length() - 1);
                return Double.parseDouble(num) * 1000000;
            } else if (input.endsWith("k")) {
                String num = input.substring(0, input.length() - 1);
                return Double.parseDouble(num) * 1000;
            } else {
                return Double.parseDouble(input);
            }
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // PRICE GRAPH
    // ═══════════════════════════════════════════════════════════════

    public static void openPriceGraph(ServerPlayerEntity player, String symbol) {
        CryptoMarket.Crypto crypto = CryptoMarket.getCryptoBySymbol(symbol);
        if (crypto == null) return;

        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("📊 " + symbol + " Price Chart"));

        // Dark background for better contrast
        GuiElementBuilder bg = new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE).setName(Text.literal(""));
        for (int i = 0; i < 54; i++) gui.setSlot(i, bg.build());

        List<Double> history = CryptoMarket.getPriceHistory(symbol);
        double currentPrice = CryptoMarket.getPrice(symbol);
        
        // Header with current price info
        double priceChange = history.size() >= 2 ? currentPrice - history.get(history.size() - 2) : 0;
        Formatting priceColor = priceChange >= 0 ? Formatting.GREEN : Formatting.RED;
        String changeSymbol = priceChange >= 0 ? "▲" : "▼";
        
        gui.setSlot(4, new GuiElementBuilder(getCryptoDisplayItem(crypto).getItem())
                .setName(Text.literal(crypto.icon + " " + crypto.name).formatted(Formatting.BOLD, Formatting.GOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Current: ").formatted(Formatting.GRAY)
                        .append(Text.literal("$" + CryptoMarket.formatCrypto(currentPrice)).formatted(Formatting.WHITE)))
                .addLoreLine(Text.literal("Change: ").formatted(Formatting.GRAY)
                        .append(Text.literal(changeSymbol + " $" + CryptoMarket.formatCrypto(Math.abs(priceChange))).formatted(priceColor)))
                .addLoreLine(Text.literal("Data Points: ").formatted(Formatting.GRAY)
                        .append(Text.literal(String.valueOf(history.size())).formatted(Formatting.WHITE)))
                .glow().build());

        if (history.size() < 2) {
            gui.setSlot(22, new GuiElementBuilder(Items.BARRIER)
                    .setName(Text.literal("Insufficient Data").formatted(Formatting.RED, Formatting.BOLD))
                    .addLoreLine(Text.literal("§7Need more price updates to"))
                    .addLoreLine(Text.literal("§7display the candlestick chart."))
                    .build());
        } else {
            // Calculate min/max for scaling with padding
            double minPrice = history.stream().mapToDouble(Double::doubleValue).min().orElse(0);
            double maxPrice = history.stream().mapToDouble(Double::doubleValue).max().orElse(1);
            double range = maxPrice - minPrice;
            if (range == 0) range = 1;
            
            // Add padding to range
            minPrice -= range * 0.1;
            maxPrice += range * 0.1;
            range = maxPrice - minPrice;

            // Draw candlestick chart (7 columns x 4 rows = 28 data points max)
            int graphStartSlot = 10;
            int maxDataPoints = 28;
            int startIdx = Math.max(0, history.size() - maxDataPoints);
            int dataCount = Math.min(maxDataPoints, history.size() - startIdx);
            
            // Calculate wick positions for each column (7 columns)
            for (int col = 0; col < 7 && (startIdx + col) < history.size() - 1; col++) {
                int idx = startIdx + col;
                double price = history.get(idx);
                double nextPrice = history.get(idx + 1);
                boolean isUp = nextPrice >= price;
                
                // Calculate wick positions
                double wickHigh = Math.max(price, nextPrice);
                double wickLow = Math.min(price, nextPrice);
                int highRow = (int) Math.min(3, Math.max(0, 3 - ((wickHigh - minPrice) / range * 3)));
                int lowRow = (int) Math.min(3, Math.max(0, 3 - ((wickLow - minPrice) / range * 3)));
                int bodyCenterRow = (int) Math.min(3, Math.max(0, 3 - (((price + nextPrice) / 2 - minPrice) / range * 3)));
                
                // Draw wick (vertical line)
                for (int row = Math.min(highRow, lowRow); row <= Math.max(highRow, lowRow); row++) {
                    int slot = graphStartSlot + (row * 9) + col;
                    if (slot >= 0 && slot < 54) {
                        Item wickItem = isUp ? Items.LIME_STAINED_GLASS_PANE : Items.RED_STAINED_GLASS_PANE;
                        gui.setSlot(slot, new GuiElementBuilder(wickItem)
                                .setName(Text.literal(isUp ? "§a▲ Bullish" : "§c▼ Bearish").formatted(Formatting.BOLD))
                                .addLoreLine(Text.literal(""))
                                .addLoreLine(Text.literal("Open: ").formatted(Formatting.GRAY)
                                        .append(Text.literal("$" + CryptoMarket.formatCrypto(price)).formatted(Formatting.WHITE)))
                                .addLoreLine(Text.literal("Close: ").formatted(Formatting.GRAY)
                                        .append(Text.literal("$" + CryptoMarket.formatCrypto(nextPrice)).formatted(isUp ? Formatting.GREEN : Formatting.RED)))
                                .addLoreLine(Text.literal("Range: ").formatted(Formatting.GRAY)
                                        .append(Text.literal("$" + CryptoMarket.formatCrypto(wickHigh - wickLow)).formatted(Formatting.YELLOW)))
                                .build());
                    }
                }
                
                // Draw body (thicker part) at center
                int bodySlot = graphStartSlot + (bodyCenterRow * 9) + col;
                if (bodySlot >= 0 && bodySlot < 54) {
                    Item bodyItem = isUp ? Items.LIME_CONCRETE : Items.RED_CONCRETE;
                    gui.setSlot(bodySlot, new GuiElementBuilder(bodyItem)
                            .setName(Text.literal(isUp ? "§a▲ Bullish Candle" : "§c▼ Bearish Candle").formatted(Formatting.BOLD))
                            .addLoreLine(Text.literal(""))
                            .addLoreLine(Text.literal("Open: ").formatted(Formatting.GRAY)
                                    .append(Text.literal("$" + CryptoMarket.formatCrypto(price)).formatted(Formatting.WHITE)))
                            .addLoreLine(Text.literal("Close: ").formatted(Formatting.GRAY)
                                    .append(Text.literal("$" + CryptoMarket.formatCrypto(nextPrice)).formatted(isUp ? Formatting.GREEN : Formatting.RED)))
                            .addLoreLine(Text.literal("Change: ").formatted(Formatting.GRAY)
                                    .append(Text.literal("$" + CryptoMarket.formatCrypto(Math.abs(nextPrice - price))).formatted(isUp ? Formatting.GREEN : Formatting.RED)))
                            .glow().build());
                }
            }
            
            // Legend with better styling
            gui.setSlot(47, new GuiElementBuilder(Items.LIME_CONCRETE)
                    .setName(Text.literal("Bullish Candle").formatted(Formatting.GREEN, Formatting.BOLD))
                    .addLoreLine(Text.literal("§7Price went UP"))
                    .build());
            gui.setSlot(48, new GuiElementBuilder(Items.RED_CONCRETE)
                    .setName(Text.literal("Bearish Candle").formatted(Formatting.RED, Formatting.BOLD))
                    .addLoreLine(Text.literal("§7Price went DOWN"))
                    .build());
            gui.setSlot(49, new GuiElementBuilder(Items.GOLD_NUGGET)
                    .setName(Text.literal("Price Range").formatted(Formatting.YELLOW, Formatting.BOLD))
                    .addLoreLine(Text.literal("§7High to Low wick"))
                    .build());
            
            // Scale info
            gui.setSlot(50, new GuiElementBuilder(Items.PAPER)
                    .setName(Text.literal("Chart Scale").formatted(Formatting.AQUA))
                    .addLoreLine(Text.literal("Max: ").formatted(Formatting.GRAY)
                    .append(Text.literal("$" + CryptoMarket.formatCrypto(maxPrice)).formatted(Formatting.WHITE)))
            .addLoreLine(Text.literal("Min: ").formatted(Formatting.GRAY)
                    .append(Text.literal("$" + CryptoMarket.formatCrypto(minPrice)).formatted(Formatting.WHITE)))
                    .build());
        }

        // Back button
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to Market").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openCryptoDetail(player, symbol))
                .build());

        gui.open();
    }
}
