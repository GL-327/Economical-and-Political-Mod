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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * GUI system for the stock market with multiple pages and trading functionality.
 */
public class StockMarketGui {

    // Track open GUIs for auto-refresh
    private static final Map<UUID, SimpleGui> openStockGuis = new ConcurrentHashMap<>();
    private static final Map<UUID, String> guiViewType = new ConcurrentHashMap<>(); // Track which view is open
    private static final Map<UUID, Integer> guiPage = new ConcurrentHashMap<>(); // Track page number
    private static final Map<UUID, String> guiSymbol = new ConcurrentHashMap<>(); // Track symbol for detail view
    private static int tickCounter = 0;

    // Call this from ServerTickEvents.END_SERVER_TICK
    public static void tick() {
        tickCounter++;
        // Only refresh every 20 ticks (1 second) to avoid performance issues
        if (tickCounter % 20 != 0) return;
        
        openStockGuis.entrySet().removeIf(entry -> {
            SimpleGui gui = entry.getValue();
            if (gui.isOpen()) {
                UUID uuid = entry.getKey();
                String viewType = guiViewType.getOrDefault(uuid, "main");
                
                // Refresh based on view type
                switch (viewType) {
                    case "main" -> refreshHeader(gui);
                    case "browser" -> refreshBrowserView(gui, uuid);
                    case "portfolio" -> refreshPortfolioView(gui, uuid);
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
        StockMarket.MarketTrend trend = StockMarket.getCurrentTrend();
        long currentTick = PoliticalServer.server.getTicks();
        String countdown = StockMarket.getCountdownString(currentTick);
        
        gui.setSlot(4, new GuiElementBuilder(Items.GOLD_BLOCK)
                .setName(Text.literal("📈 Stock Market").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal(trend.name).formatted(trend.color))
                .addLoreLine(Text.literal("Market: " + (StockMarket.isMarketOpen() ? "§aOpen" : "§cClosed")))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§7Your Portfolio Value: §6" + 
                        StockMarket.formatPrice(StockMarket.getPortfolioValue(player.getUuidAsString())) + " coins"))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§e⏱ Next update: " + countdown).formatted(Formatting.YELLOW))
                .glow().build());
    }

    private static void refreshBrowserView(SimpleGui gui, UUID uuid) {
        ServerPlayerEntity player = gui.getPlayer();
        if (player == null) return;
        
        int page = guiPage.getOrDefault(uuid, 0);
        StockMarket.Stock[] stocks = StockMarket.Stock.values();
        int itemsPerPage = 28;
        int startIdx = page * itemsPerPage;
        
        int slot = 10;
        int count = 0;
        
        for (int i = startIdx; i < stocks.length && count < itemsPerPage; i++) {
            StockMarket.Stock stock = stocks[i];
            final String symbol = stock.symbol;
            double price = StockMarket.getPrice(stock.symbol);
            double change = StockMarket.calculateChange(stock.symbol);
            
            Formatting changeColor = change >= 0 ? Formatting.GREEN : Formatting.RED;
            String changeStr = (change >= 0 ? "+" : "") + String.format("%.2f", change) + "%";
            
            ItemStack displayItem = getStockDisplayItem(stock);
            
            gui.setSlot(slot, new GuiElementBuilder(displayItem.getItem())
                    .setName(Text.literal(stock.icon + " " + stock.symbol).formatted(Formatting.BOLD))
                    .addLoreLine(Text.literal(stock.name).formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("§6Price: " + StockMarket.formatPrice(price) + " coins"))
                    .addLoreLine(Text.literal("§7Change: " + changeStr).formatted(changeColor))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("§eClick to trade"))
                    .setCallback((idx, type, action) -> openStockDetail(player, symbol))
                    .build());
            
            count++;
            slot++;
            if (slot == 17) slot = 19;
            else if (slot == 26) slot = 28;
            else if (slot == 35) slot = 37;
            else if (slot == 44) slot = 46;
        }
    }

    private static void refreshPortfolioView(SimpleGui gui, UUID uuid) {
        ServerPlayerEntity player = gui.getPlayer();
        if (player == null) return;
        
        // Update portfolio value in header
        double portfolioValue = StockMarket.getPortfolioValue(player.getUuidAsString());
        int liquidCash = CoinManager.getCoins(player);
        
        gui.setSlot(4, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Text.literal("💼 Portfolio Summary").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Portfolio Value: §6" + StockMarket.formatPrice(portfolioValue)).formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Liquid Cash: §6" + liquidCash + " coins").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Total Net Worth: §6" + StockMarket.formatPrice(portfolioValue + liquidCash)).formatted(Formatting.GOLD, Formatting.BOLD))
                .glow().build());
        
        // Update individual stock values
        Map<String, Integer> portfolio = StockMarket.getPortfolio(player.getUuidAsString());
        int slot = 9;
        
        for (Map.Entry<String, Integer> entry : portfolio.entrySet()) {
            if (slot >= 45) break;
            
            String symbol = entry.getKey();
            int shares = entry.getValue();
            double price = StockMarket.getPrice(symbol);
            double value = price * shares;
            double change = StockMarket.calculateChange(symbol);
            
            Formatting changeColor = change >= 0 ? Formatting.GREEN : Formatting.RED;
            StockMarket.Stock stock = StockMarket.getStockBySymbol(symbol);
            Item displayItem = stock != null ? getStockDisplayItem(stock).getItem() : Items.PAPER;
            
            gui.setSlot(slot, new GuiElementBuilder(displayItem)
                    .setName(Text.literal(symbol).formatted(Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Shares: §f" + shares).formatted(Formatting.WHITE))
                    .addLoreLine(Text.literal("Price: §6" + StockMarket.formatPrice(price)).formatted(Formatting.YELLOW))
                    .addLoreLine(Text.literal("Value: §6" + StockMarket.formatPrice(value)).formatted(Formatting.GOLD))
                    .addLoreLine(Text.literal("Change: " + (change >= 0 ? "+" : "") + String.format("%.2f", change) + "%").formatted(changeColor))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("§eClick to trade"))
                    .setCallback((idx, type, action) -> openStockDetail(player, symbol))
                    .build());
            slot++;
        }
    }

    private static void refreshDetailView(SimpleGui gui, UUID uuid) {
        ServerPlayerEntity player = gui.getPlayer();
        if (player == null) return;
        
        String symbol = guiSymbol.get(uuid);
        if (symbol == null) return;
        
        StockMarket.Stock stock = StockMarket.getStockBySymbol(symbol);
        if (stock == null) return;
        
        double price = StockMarket.getPrice(symbol);
        double change = StockMarket.calculateChange(symbol);
        double totalChange = StockMarket.calculateTotalChange(symbol);
        
        Formatting changeColor = change >= 0 ? Formatting.GREEN : Formatting.RED;
        Formatting totalColor = totalChange >= 0 ? Formatting.GREEN : Formatting.RED;
        
        // Update stock info
        gui.setSlot(4, new GuiElementBuilder(getStockDisplayItem(stock).getItem())
                .setName(Text.literal(stock.icon + " " + stock.symbol).formatted(Formatting.BOLD))
                .addLoreLine(Text.literal(stock.name).formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§6Current Price: " + StockMarket.formatPrice(price)))
                .addLoreLine(Text.literal("§7Session Change: " + (change >= 0 ? "+" : "") + 
                        String.format("%.2f", change) + "%").formatted(changeColor))
                .addLoreLine(Text.literal("§7Total Change: " + (totalChange >= 0 ? "+" : "") + 
                        String.format("%.2f", totalChange) + "%").formatted(totalColor))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§7Volatility: " + String.format("%.0f%%", stock.volatility * 100)))
                .glow().build());
        
        // Update holdings
        int owned = StockMarket.getPortfolio(player.getUuidAsString()).getOrDefault(symbol, 0);
        gui.setSlot(13, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Text.literal("Your Holdings").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§7Shares Owned: §f" + owned))
                .addLoreLine(Text.literal("§7Value: §6" + StockMarket.formatPrice(price * owned)))
                .build());
        
        // Update buy buttons with current prices
        gui.setSlot(20, new GuiElementBuilder(Items.LIME_DYE)
                .setName(Text.literal("Buy 1 Share").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal("§7Cost: §6" + StockMarket.formatPrice(price) + " coins"))
                .build());
        
        gui.setSlot(21, new GuiElementBuilder(Items.LIME_DYE)
                .setName(Text.literal("Buy 10 Shares").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal("§7Cost: §6" + StockMarket.formatPrice(price * 10) + " coins"))
                .build());
        
        gui.setSlot(22, new GuiElementBuilder(Items.LIME_DYE)
                .setName(Text.literal("Buy 100 Shares").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal("§7Cost: §6" + StockMarket.formatPrice(price * 100) + " coins"))
                .build());
    }

    // ═══════════════════════════════════════════════════════════════
    // MAIN MENU
    // ═══════════════════════════════════════════════════════════════

    public static void openMainMenu(ServerPlayerEntity player) {
        // Close existing GUI if open
        SimpleGui existingGui = openStockGuis.remove(player.getUuid());
        if (existingGui != null && existingGui.isOpen()) {
            existingGui.close();
        }
        
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
        gui.setTitle(Text.literal("📈 Stock Market"));

        // Track this GUI
        openStockGuis.put(player.getUuid(), gui);
        guiViewType.put(player.getUuid(), "main");

        // Background
        GuiElementBuilder bg = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE).setName(Text.literal(""));
        for (int i = 0; i < 27; i++) gui.setSlot(i, bg.build());

        // Header
        StockMarket.MarketTrend trend = StockMarket.getCurrentTrend();
        
        // Get countdown for next price update
        long currentTick = PoliticalServer.server.getTicks();
        String countdown = StockMarket.getCountdownString(currentTick);
        
        gui.setSlot(4, new GuiElementBuilder(Items.GOLD_BLOCK)
                .setName(Text.literal("📈 Stock Market").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal(trend.name).formatted(trend.color))
                .addLoreLine(Text.literal("Market: " + (StockMarket.isMarketOpen() ? "§aOpen" : "§cClosed")))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§7Your Portfolio Value: §6" + 
                        StockMarket.formatPrice(StockMarket.getPortfolioValue(player.getUuidAsString())) + " coins"))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§e⏱ Next update: " + countdown).formatted(Formatting.YELLOW))
                .glow().build());

        // Browse Stocks
        gui.setSlot(10, new GuiElementBuilder(Items.KNOWLEDGE_BOOK)
                .setName(Text.literal("📊 Browse Stocks").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§7View all available stocks"))
                .addLoreLine(Text.literal("§7Check prices and trends"))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§eClick to browse"))
                .setCallback((idx, type, action) -> openStockBrowser(player, 0))
                .build());

        // My Portfolio
        gui.setSlot(12, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Text.literal("💼 My Portfolio").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§7View your holdings"))
                .addLoreLine(Text.literal("§7Track your investments"))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§eClick to view"))
                .setCallback((idx, type, action) -> openPortfolio(player, 0))
                .build());

        // Market News
        gui.setSlot(14, new GuiElementBuilder(Items.WRITTEN_BOOK)
                .setName(Text.literal("📰 Market News").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§7Active market events"))
                .addLoreLine(Text.literal("§7Affecting stock prices"))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§eClick to view"))
                .setCallback((idx, type, action) -> openMarketNews(player))
                .build());

        // Top Movers
        gui.setSlot(16, new GuiElementBuilder(Items.SPECTRAL_ARROW)
                .setName(Text.literal("🚀 Top Movers").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§7Biggest gainers and losers"))
                .addLoreLine(Text.literal("§7In the last trading session"))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§eClick to view"))
                .setCallback((idx, type, action) -> openTopMovers(player))
                .build());

        gui.open();
    }

    // ═══════════════════════════════════════════════════════════════
    // PORTFOLIO VIEW
    // ═══════════════════════════════════════════════════════════════

    public static void openPortfolio(ServerPlayerEntity player, int page) {
        // Close existing GUI if open
        SimpleGui existingGui = openStockGuis.remove(player.getUuid());
        if (existingGui != null && existingGui.isOpen()) {
            existingGui.close();
        }
        
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("💼 Your Portfolio"));

        // Track this GUI
        openStockGuis.put(player.getUuid(), gui);
        guiViewType.put(player.getUuid(), "portfolio");
        guiPage.put(player.getUuid(), page);

        // Background
        GuiElementBuilder bg = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE).setName(Text.literal(""));
        for (int i = 0; i < 54; i++) gui.setSlot(i, bg.build());

        // Header
        double portfolioValue = StockMarket.getPortfolioValue(player.getUuidAsString());
        int liquidCash = CoinManager.getCoins(player);
        
        gui.setSlot(4, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Text.literal("💼 Portfolio Summary").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Portfolio Value: §6" + StockMarket.formatPrice(portfolioValue)).formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Liquid Cash: §6" + CoinManager.getCoins(player) + " coins").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Total Net Worth: §6" + StockMarket.formatPrice(portfolioValue + liquidCash)).formatted(Formatting.GOLD, Formatting.BOLD))
                .glow().build());

        // Get portfolio holdings
        java.util.Map<String, Integer> portfolio = StockMarket.getPortfolio(player.getUuidAsString());
        int itemsPerPage = 36;
        int totalPages = Math.max(1, (int) Math.ceil((double) portfolio.size() / itemsPerPage));
        
        int slot = 9;
        int count = 0;
        int startIndex = page * itemsPerPage;
        
        for (java.util.Map.Entry<String, Integer> entry : portfolio.entrySet()) {
            if (count++ < startIndex) continue;
            if (slot >= 45) break;
            
            String symbol = entry.getKey();
            int shares = entry.getValue();
            double price = StockMarket.getPrice(symbol);
            double value = price * shares;
            
            StockMarket.Stock stock = StockMarket.getStockBySymbol(symbol);
            Item displayItem = stock != null ? getStockDisplayItem(stock).getItem() : Items.PAPER;
            
            gui.setSlot(slot++, new GuiElementBuilder(displayItem)
                    .setName(Text.literal(symbol).formatted(Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Shares: §f" + shares).formatted(Formatting.WHITE))
                    .addLoreLine(Text.literal("Price: §6" + StockMarket.formatPrice(price)).formatted(Formatting.YELLOW))
                    .addLoreLine(Text.literal("Value: §6" + StockMarket.formatPrice(value)).formatted(Formatting.GOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("§eClick to trade"))
                    .setCallback((idx, type, action) -> openStockDetail(player, symbol))
                    .build());
        }

        // Back button
        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("← Back").formatted(Formatting.RED))
                .setCallback((idx, type, action) -> openMainMenu(player))
                .build());

        // Navigation
        if (page > 0) {
            gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("← Previous").formatted(Formatting.YELLOW))
                    .setCallback((idx, type, action) -> openPortfolio(player, page - 1))
                    .build());
        }
        
        if (page < totalPages - 1) {
            gui.setSlot(53, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("Next →").formatted(Formatting.YELLOW))
                    .setCallback((idx, type, action) -> openPortfolio(player, page + 1))
                    .build());
        }

        gui.open();
    }

    // ═══════════════════════════════════════════════════════════════
    // STOCK BROWSER
    // ═══════════════════════════════════════════════════════════════

    public static void openStockBrowser(ServerPlayerEntity player, int page) {
        // Close existing GUI if open
        SimpleGui existingGui = openStockGuis.remove(player.getUuid());
        if (existingGui != null && existingGui.isOpen()) {
            existingGui.close();
        }
        
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("📊 Browse Stocks"));

        // Track this GUI
        openStockGuis.put(player.getUuid(), gui);
        guiViewType.put(player.getUuid(), "browser");
        guiPage.put(player.getUuid(), page);

        // Background
        GuiElementBuilder bg = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE).setName(Text.literal(""));
        for (int i = 0; i < 54; i++) gui.setSlot(i, bg.build());

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.KNOWLEDGE_BOOK)
                .setName(Text.literal("📊 All Stocks").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal("§7Page " + (page + 1)))
                .glow().build());

        // Stock items (7 per row, rows 1-4)
        StockMarket.Stock[] stocks = StockMarket.Stock.values();
        int itemsPerPage = 28;
        int startIdx = page * itemsPerPage;
        
        int slot = 10;
        int count = 0;
        
        for (int i = startIdx; i < stocks.length && count < itemsPerPage; i++) {
            StockMarket.Stock stock = stocks[i];
            double price = StockMarket.getPrice(stock.symbol);
            double change = StockMarket.calculateChange(stock.symbol);
            
            Formatting changeColor = change >= 0 ? Formatting.GREEN : Formatting.RED;
            String changeStr = (change >= 0 ? "+" : "") + String.format("%.2f", change) + "%";
            
            // Determine item based on sector
            ItemStack displayItem = getStockDisplayItem(stock);
            
            final String symbol = stock.symbol;
            gui.setSlot(slot, new GuiElementBuilder(displayItem.getItem())
                    .setName(Text.literal(stock.icon + " " + stock.symbol).formatted(Formatting.BOLD))
                    .addLoreLine(Text.literal(stock.name).formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("§6Price: " + StockMarket.formatPrice(price) + " coins"))
                    .addLoreLine(Text.literal("§7Change: " + changeStr).formatted(changeColor))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("§eClick to trade"))
                    .setCallback((idx, type, action) -> openStockDetail(player, symbol))
                    .build());
            
            count++;
            slot++;
            if (slot == 17) slot = 19; // Skip edges
            else if (slot == 26) slot = 28;
            else if (slot == 35) slot = 37;
            else if (slot == 44) slot = 46;
        }

        // Navigation
        if (page > 0) {
            final int prev = page - 1;
            gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("← Previous Page").formatted(Formatting.YELLOW))
                    .setCallback((idx, type, action) -> openStockBrowser(player, prev))
                    .build());
        }
        
        if ((page + 1) * itemsPerPage < stocks.length) {
            final int next = page + 1;
            gui.setSlot(53, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("Next Page →").formatted(Formatting.YELLOW))
                    .setCallback((idx, type, action) -> openStockBrowser(player, next))
                    .build());
        }

        // Back button
        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openMainMenu(player))
                .build());

        gui.open();
    }

    // ═══════════════════════════════════════════════════════════════
    // STOCK DETAIL & TRADING
    // ═══════════════════════════════════════════════════════════════

    public static void openStockDetail(ServerPlayerEntity player, String symbol) {
        StockMarket.Stock stock = StockMarket.getStockBySymbol(symbol);
        if (stock == null) return;

        // Close existing GUI if open
        SimpleGui existingGui = openStockGuis.remove(player.getUuid());
        if (existingGui != null && existingGui.isOpen()) {
            existingGui.close();
        }

        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X4, player, false);
        gui.setTitle(Text.literal(stock.symbol + " Trading"));

        // Track this GUI
        openStockGuis.put(player.getUuid(), gui);
        guiViewType.put(player.getUuid(), "detail");
        guiSymbol.put(player.getUuid(), symbol);

        // Background
        GuiElementBuilder bg = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE).setName(Text.literal(""));
        for (int i = 0; i < 36; i++) gui.setSlot(i, bg.build());

        double price = StockMarket.getPrice(symbol);
        double change = StockMarket.calculateChange(symbol);
        double totalChange = StockMarket.calculateTotalChange(symbol);
        
        Formatting changeColor = change >= 0 ? Formatting.GREEN : Formatting.RED;
        Formatting totalColor = totalChange >= 0 ? Formatting.GREEN : Formatting.RED;

        // Stock info
        gui.setSlot(4, new GuiElementBuilder(getStockDisplayItem(stock).getItem())
                .setName(Text.literal(stock.icon + " " + stock.symbol).formatted(Formatting.BOLD))
                .addLoreLine(Text.literal(stock.name).formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§6Current Price: " + StockMarket.formatPrice(price)))
                .addLoreLine(Text.literal("§7Session Change: " + (change >= 0 ? "+" : "") + 
                        String.format("%.2f", change) + "%").formatted(changeColor))
                .addLoreLine(Text.literal("§7Total Change: " + (totalChange >= 0 ? "+" : "") + 
                        String.format("%.2f", totalChange) + "%").formatted(totalColor))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§7Volatility: " + String.format("%.0f%%", stock.volatility * 100)))
                .glow().build());

        // Player's holdings
        int owned = StockMarket.getPortfolio(player.getUuidAsString()).getOrDefault(symbol, 0);
        gui.setSlot(13, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Text.literal("Your Holdings").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§7Shares Owned: §f" + owned))
                .addLoreLine(Text.literal("§7Value: §6" + StockMarket.formatPrice(price * owned)))
                .build());

        // Buy buttons
        final String sym = symbol;
        gui.setSlot(20, new GuiElementBuilder(Items.LIME_DYE)
                .setName(Text.literal("Buy 1 Share").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal("§7Cost: §6" + StockMarket.formatPrice(price) + " coins"))
                .setCallback((idx, type, action) -> {
                    StockMarket.buyStock(player, sym, 1);
                    openStockDetail(player, sym);
                })
                .build());

        gui.setSlot(21, new GuiElementBuilder(Items.LIME_DYE)
                .setName(Text.literal("Buy 10 Shares").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal("§7Cost: §6" + StockMarket.formatPrice(price * 10) + " coins"))
                .setCallback((idx, type, action) -> {
                    StockMarket.buyStock(player, sym, 10);
                    openStockDetail(player, sym);
                })
                .build());

        gui.setSlot(22, new GuiElementBuilder(Items.LIME_DYE)
                .setName(Text.literal("Buy 100 Shares").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal("§7Cost: §6" + StockMarket.formatPrice(price * 100) + " coins"))
                .setCallback((idx, type, action) -> {
                    StockMarket.buyStock(player, sym, 100);
                    openStockDetail(player, sym);
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
        if (owned > 0) {
            gui.setSlot(24, new GuiElementBuilder(Items.RED_DYE)
                    .setName(Text.literal("Sell 1 Share").formatted(Formatting.RED, Formatting.BOLD))
                    .addLoreLine(Text.literal("§7Value: §6" + StockMarket.formatPrice(price) + " coins"))
                    .setCallback((idx, type, action) -> {
                        StockMarket.sellStock(player, sym, 1);
                        openStockDetail(player, sym);
                    })
                    .build());

            gui.setSlot(25, new GuiElementBuilder(Items.RED_DYE)
                    .setName(Text.literal("Sell 10 Shares").formatted(Formatting.RED, Formatting.BOLD))
                    .addLoreLine(Text.literal("§7Value: §6" + StockMarket.formatPrice(price * 10) + " coins"))
                    .setCallback((idx, type, action) -> {
                        StockMarket.sellStock(player, sym, 10);
                        openStockDetail(player, sym);
                    })
                    .build());

            gui.setSlot(26, new GuiElementBuilder(Items.RED_DYE)
                    .setName(Text.literal("Sell All (" + owned + ")").formatted(Formatting.RED, Formatting.BOLD))
                    .addLoreLine(Text.literal("§7Value: §6" + StockMarket.formatPrice(price * owned) + " coins"))
                    .setCallback((idx, type, action) -> {
                        StockMarket.sellStock(player, sym, owned);
                        openStockDetail(player, sym);
                    })
                    .build());

            // Custom sell button with sign input
            gui.setSlot(27, new GuiElementBuilder(Items.WRITABLE_BOOK)
                    .setName(Text.literal("Custom Sell").formatted(Formatting.YELLOW, Formatting.BOLD))
                    .addLoreLine(Text.literal("§7Enter custom amount"))
                    .addLoreLine(Text.literal("§7Supports k/m suffixes"))
                    .addLoreLine(Text.literal("§eClick to enter amount"))
                    .setCallback((idx, type, action) -> openCustomSellSign(player, sym, owned))
                    .build());
        }

        // Back button
        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openStockBrowser(player, 0))
                .build());
        
        // Price graph button
        gui.setSlot(32, new GuiElementBuilder(Items.MAP)
                .setName(Text.literal("📊 Price Graph").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal("§7View price history"))
                .addLoreLine(Text.literal("§eClick to view"))
                .setCallback((idx, type, action) -> openPriceGraph(player, sym))
                .build());

        gui.open();
    }
    // ═══════════════════════════════════════════════════════════════
    // MARKET NEWS
    // ═══════════════════════════════════════════════════════════════

    public static void openMarketNews(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
        gui.setTitle(Text.literal("📰 Market News"));

        // Background
        GuiElementBuilder bg = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE).setName(Text.literal(""));
        for (int i = 0; i < 27; i++) gui.setSlot(i, bg.build());

        List<StockMarket.MarketEvent> events = StockMarket.getActiveEvents();
        
        gui.setSlot(4, new GuiElementBuilder(Items.WRITTEN_BOOK)
                .setName(Text.literal("📰 Market News").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§7Active events affecting prices"))
                .glow().build());

        if (events.isEmpty()) {
            gui.setSlot(13, new GuiElementBuilder(Items.BARRIER)
                    .setName(Text.literal("No Active Events").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("§7The market is calm right now."))
                    .addLoreLine(Text.literal("§7No major events are affecting prices."))
                    .build());
        } else {
            int slot = 10;
            for (StockMarket.MarketEvent event : events) {
                if (slot > 16) break;
                
                Formatting color = event.priceModifier >= 0 ? Formatting.GREEN : Formatting.RED;
                gui.setSlot(slot, new GuiElementBuilder(event.priceModifier >= 0 ? Items.LIME_DYE : Items.RED_DYE)
                        .setName(Text.literal("⚡ " + event.name).formatted(color, Formatting.BOLD))
                        .addLoreLine(Text.literal(event.description).formatted(Formatting.GRAY))
                        .addLoreLine(Text.literal(""))
                        .addLoreLine(Text.literal("§7Effect: " + (event.priceModifier >= 0 ? "+" : "") + 
                                String.format("%.0f%%", event.priceModifier * 100)).formatted(color))
                        .addLoreLine(Text.literal("§7Stocks: " + String.join(", ", event.affectedStocks)))
                        .build());
                slot++;
            }
        }

        // Back button
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

        // Background
        GuiElementBuilder bg = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE).setName(Text.literal(""));
        for (int i = 0; i < 27; i++) gui.setSlot(i, bg.build());

        // Find top gainers and losers
        List<Map.Entry<String, Double>> changes = new ArrayList<>();
        for (StockMarket.Stock stock : StockMarket.Stock.values()) {
            changes.add(Map.entry(stock.symbol, StockMarket.calculateChange(stock.symbol)));
        }
        
        changes.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        
        // Top 3 gainers
        gui.setSlot(4, new GuiElementBuilder(Items.SPECTRAL_ARROW)
                .setName(Text.literal("🚀 Top Movers").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .glow().build());

        // Gainers (left side)
        for (int i = 0; i < 3 && i < changes.size(); i++) {
            Map.Entry<String, Double> entry = changes.get(i);
            StockMarket.Stock stock = StockMarket.getStockBySymbol(entry.getKey());
            if (stock == null) continue;
            
            final String symbol = entry.getKey();
            gui.setSlot(10 + i, new GuiElementBuilder(Items.LIME_DYE)
                    .setName(Text.literal("📈 " + stock.symbol).formatted(Formatting.GREEN, Formatting.BOLD))
                    .addLoreLine(Text.literal(stock.name).formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("§a+" + String.format("%.2f", entry.getValue()) + "%"))
                    .addLoreLine(Text.literal("§6" + StockMarket.formatPrice(StockMarket.getPrice(symbol))))
                    .setCallback((idx, type, action) -> openStockDetail(player, symbol))
                    .build());
        }

        // Losers (right side)
        for (int i = 0; i < 3 && i < changes.size(); i++) {
            int idx = changes.size() - 1 - i;
            Map.Entry<String, Double> entry = changes.get(idx);
            StockMarket.Stock stock = StockMarket.getStockBySymbol(entry.getKey());
            if (stock == null) continue;
            
            final String symbol = entry.getKey();
            gui.setSlot(14 + i, new GuiElementBuilder(Items.RED_DYE)
                    .setName(Text.literal("📉 " + stock.symbol).formatted(Formatting.RED, Formatting.BOLD))
                    .addLoreLine(Text.literal(stock.name).formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("§c" + String.format("%.2f", entry.getValue()) + "%"))
                    .addLoreLine(Text.literal("§6" + StockMarket.formatPrice(StockMarket.getPrice(symbol))))
                    .setCallback((idx2, type, action) -> openStockDetail(player, symbol))
                    .build());
        }

        // Back button
        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openMainMenu(player))
                .build());

        gui.open();
    }

    // ═══════════════════════════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════════════════════════

    public static ItemStack getStockDisplayItem(StockMarket.Stock stock) {
        return switch (stock) {
            // Technology
            case REDSTONE_TECH -> new ItemStack(Items.REDSTONE);
            case COMPUTING_CORP -> new ItemStack(Items.REPEATER);
            case AUTOMATION_INC -> new ItemStack(Items.OBSERVER);
            
            // Mining
            case DEEP_MINE_CO -> new ItemStack(Items.STONE_PICKAXE);
            case DIAMOND_EXTRACT -> new ItemStack(Items.DIAMOND_PICKAXE);
            case NETHER_DRILLING -> new ItemStack(Items.NETHERRACK);
            
            // Agriculture
            case GOLDEN_FARMS -> new ItemStack(Items.WHEAT);
            case ENCHANTED_CROPS -> new ItemStack(Items.GOLDEN_CARROT);
            case MOCHI_FOODS -> new ItemStack(Items.CAKE);
            
            // Combat
            case BOUNTY_ARMS -> new ItemStack(Items.IRON_SWORD);
            case SLAYER_INDUSTRIES -> new ItemStack(Items.DIAMOND_SWORD);
            case ARMOR_WORKS -> new ItemStack(Items.DIAMOND_CHESTPLATE);
            
            // Magic
            case ENCHANT_UNLIMITED -> new ItemStack(Items.ENCHANTED_BOOK);
            case POTION_MASTERS -> new ItemStack(Items.BREWING_STAND);
            case SCULK_DYNAMICS -> new ItemStack(Items.SCULK_SENSOR);
            
            // Trading
            case VILLAGER_TRADE -> new ItemStack(Items.EMERALD);
            case EMERALD_BANK -> new ItemStack(Items.EMERALD_BLOCK);
            case AUCTION_HOUSE -> new ItemStack(Items.GOLD_INGOT);
            
            // Transportation
            case RAIL_NETWORK -> new ItemStack(Items.RAIL);
            case ELYTRA_AIR -> new ItemStack(Items.ELYTRA);
            case NETHER_PORT -> new ItemStack(Items.OBSIDIAN);
            
            // Special
            case DRAGON_HOLDINGS -> new ItemStack(Items.DRAGON_HEAD);
            case WITHER_VENTURES -> new ItemStack(Items.WITHER_SKELETON_SKULL);
            case ENDER_INVESTMENTS -> new ItemStack(Items.END_CRYSTAL);
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
                int amount = parseAmountWithSuffix(input);
                if (amount <= 0) {
                    player.sendMessage(Text.literal("✗ Invalid amount!").formatted(Formatting.RED), false);
                    openStockDetail(player, symbol);
                    return;
                }
                StockMarket.buyStock(player, symbol, amount);
                openStockDetail(player, symbol);
            }
        };
        signGui.setLine(0, Text.literal("Enter amount:"));
        signGui.setLine(1, Text.literal("(k=1000, m=1000000)"));
        signGui.open();
    }

    private static void openCustomSellSign(ServerPlayerEntity player, String symbol, int maxShares) {
        SignGui signGui = new SignGui(player) {
            @Override
            public void onClose() {
                String input = this.getLine(0).getString().trim();
                int amount = parseAmountWithSuffix(input);
                if (amount <= 0) {
                    player.sendMessage(Text.literal("✗ Invalid amount!").formatted(Formatting.RED), false);
                    openStockDetail(player, symbol);
                    return;
                }
                if (amount > maxShares) {
                    player.sendMessage(Text.literal("✗ You only have " + maxShares + " shares!").formatted(Formatting.RED), false);
                    openStockDetail(player, symbol);
                    return;
                }
                StockMarket.sellStock(player, symbol, amount);
                openStockDetail(player, symbol);
            }
        };
        signGui.setLine(0, Text.literal("Enter amount:"));
        signGui.setLine(1, Text.literal("Max: " + maxShares + " shares"));
        signGui.open();
    }

    /**
     * Parse amount with k/m suffixes (e.g., "10k" = 10000, "5m" = 5000000)
     */
    private static int parseAmountWithSuffix(String input) {
        if (input == null || input.isEmpty()) return 0;
        
        input = input.toLowerCase().trim().replaceAll("[^0-9km]", "");
        if (input.isEmpty()) return 0;
        
        try {
            if (input.endsWith("m")) {
                String num = input.substring(0, input.length() - 1);
                return (int) (Long.parseLong(num) * 1000000);
            } else if (input.endsWith("k")) {
                String num = input.substring(0, input.length() - 1);
                return (int) (Long.parseLong(num) * 1000);
            } else {
                return Integer.parseInt(input);
            }
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // PRICE GRAPH
    // ═══════════════════════════════════════════════════════════════

    public static void openPriceGraph(ServerPlayerEntity player, String symbol) {
        StockMarket.Stock stock = StockMarket.getStockBySymbol(symbol);
        if (stock == null) return;

        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("📊 " + symbol + " Price Chart"));

        // Dark background for better contrast
        GuiElementBuilder bg = new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE).setName(Text.literal(""));
        for (int i = 0; i < 54; i++) gui.setSlot(i, bg.build());

        List<Double> history = StockMarket.getPriceHistory(symbol);
        double currentPrice = StockMarket.getPrice(symbol);
        
        // Header with current price info
        double priceChange = history.size() >= 2 ? currentPrice - history.get(history.size() - 2) : 0;
        Formatting priceColor = priceChange >= 0 ? Formatting.GREEN : Formatting.RED;
        String changeSymbol = priceChange >= 0 ? "▲" : "▼";
        
        gui.setSlot(4, new GuiElementBuilder(getStockDisplayItem(stock).getItem())
                .setName(Text.literal(stock.icon + " " + stock.name).formatted(Formatting.BOLD, Formatting.GOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Current: ").formatted(Formatting.GRAY)
                        .append(Text.literal(StockMarket.formatPrice(currentPrice)).formatted(Formatting.WHITE)))
                .addLoreLine(Text.literal("Change: ").formatted(Formatting.GRAY)
                        .append(Text.literal(changeSymbol + " " + StockMarket.formatPrice(Math.abs(priceChange))).formatted(priceColor)))
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
                                        .append(Text.literal(StockMarket.formatPrice(price)).formatted(Formatting.WHITE)))
                                .addLoreLine(Text.literal("Close: ").formatted(Formatting.GRAY)
                                        .append(Text.literal(StockMarket.formatPrice(nextPrice)).formatted(isUp ? Formatting.GREEN : Formatting.RED)))
                                .addLoreLine(Text.literal("Range: ").formatted(Formatting.GRAY)
                                        .append(Text.literal(StockMarket.formatPrice(wickHigh - wickLow)).formatted(Formatting.YELLOW)))
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
                                    .append(Text.literal(StockMarket.formatPrice(price)).formatted(Formatting.WHITE)))
                            .addLoreLine(Text.literal("Close: ").formatted(Formatting.GRAY)
                                    .append(Text.literal(StockMarket.formatPrice(nextPrice)).formatted(isUp ? Formatting.GREEN : Formatting.RED)))
                            .addLoreLine(Text.literal("Change: ").formatted(Formatting.GRAY)
                                    .append(Text.literal(StockMarket.formatPrice(Math.abs(nextPrice - price))).formatted(isUp ? Formatting.GREEN : Formatting.RED)))
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
                    .append(Text.literal(StockMarket.formatPrice(maxPrice)).formatted(Formatting.WHITE)))
            .addLoreLine(Text.literal("Min: ").formatted(Formatting.GRAY)
                    .append(Text.literal(StockMarket.formatPrice(minPrice)).formatted(Formatting.WHITE)))
                    .build());
        }

        // Back button
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to Market").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openStockDetail(player, symbol))
                .build());

        gui.open();
    }
}
