package com.political;


import com.political.economy.CoinManager;
import com.political.economy.StockMarket;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.gui.SignGui;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

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
                    // detail: do not refresh — rebuilding slots strips buy/sell callbacks
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
        ServerPlayer player = gui.getPlayer();
        if (player == null) return;
        
        // Update the header slot with fresh data
        StockMarket.MarketTrend trend = StockMarket.getCurrentTrend();
        long currentTick = PoliticalServer.server.getTickCount();
        String countdown = StockMarket.getCountdownString(currentTick);
        
        gui.setSlot(4, new GuiElementBuilder(Items.GOLD_BLOCK)
                .setName(Component.literal("📈 Stock Market").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal(trend.name).withStyle(trend.color))
                .addLoreLine(Component.literal("Market: " + (StockMarket.isMarketOpen() ? "§aOpen" : "§cClosed")))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Your Portfolio Value: §6" + 
                        StockMarket.formatPrice(StockMarket.getPortfolioValue(player.getUUID().toString())) + " coins"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§e⏱ Next update: " + countdown).withStyle(ChatFormatting.YELLOW))
                .glow().build());
    }

    private static void refreshBrowserView(SimpleGui gui, UUID uuid) {
        ServerPlayer player = gui.getPlayer();
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
            
            ChatFormatting changeColor = change >= 0 ? ChatFormatting.GREEN : ChatFormatting.RED;
            String changeStr = (change >= 0 ? "+" : "") + String.format("%.2f", change) + "%";
            
            ItemStack displayItem = getStockDisplayItem(stock);
            
            gui.setSlot(slot, new GuiElementBuilder(displayItem.getItem())
                    .setName(Component.literal(stock.icon + " " + stock.symbol).withStyle(ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(stock.name).withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§6Price: " + StockMarket.formatPrice(price) + " coins"))
                    .addLoreLine(Component.literal("§7Change: " + changeStr).withStyle(changeColor))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§eClick to trade"))
                    .setCallback((idx, type, containerInput, slotGui) -> openStockDetail(player, symbol))
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
        ServerPlayer player = gui.getPlayer();
        if (player == null) return;
        
        // Update portfolio value in header
        double portfolioValue = StockMarket.getPortfolioValue(player.getUUID().toString());
        int liquidCash = CoinManager.getCoins(player);
        
        gui.setSlot(4, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Component.literal("💼 Portfolio Summary").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Portfolio Value: §6" + StockMarket.formatPrice(portfolioValue)).withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal("Liquid Cash: §6" + liquidCash + " coins").withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Total Net Worth: §6" + StockMarket.formatPrice(portfolioValue + liquidCash)).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .glow().build());
        
        // Update individual stock values
        Map<String, Integer> portfolio = StockMarket.getPortfolio(player.getUUID().toString());
        int slot = 9;
        
        for (Map.Entry<String, Integer> entry : portfolio.entrySet()) {
            if (slot >= 45) break;
            
            String symbol = entry.getKey();
            int shares = entry.getValue();
            double price = StockMarket.getPrice(symbol);
            double value = price * shares;
            double change = StockMarket.calculateChange(symbol);
            
            ChatFormatting changeColor = change >= 0 ? ChatFormatting.GREEN : ChatFormatting.RED;
            StockMarket.Stock stock = StockMarket.getStockBySymbol(symbol);
            Item displayItem = stock != null ? getStockDisplayItem(stock).getItem() : Items.PAPER;
            
            gui.setSlot(slot, new GuiElementBuilder(displayItem)
                    .setName(Component.literal(symbol).withStyle(ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Shares: §f" + shares).withStyle(ChatFormatting.WHITE))
                    .addLoreLine(Component.literal("Price: §6" + StockMarket.formatPrice(price)).withStyle(ChatFormatting.YELLOW))
                    .addLoreLine(Component.literal("Value: §6" + StockMarket.formatPrice(value)).withStyle(ChatFormatting.GOLD))
                    .addLoreLine(Component.literal("Change: " + (change >= 0 ? "+" : "") + String.format("%.2f", change) + "%").withStyle(changeColor))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§eClick to trade"))
                    .setCallback((idx, type, containerInput, slotGui) -> openStockDetail(player, symbol))
                    .build());
            slot++;
        }
    }

    private static void refreshDetailView(SimpleGui gui, UUID uuid) {
        ServerPlayer player = gui.getPlayer();
        if (player == null) return;
        
        String symbol = guiSymbol.get(uuid);
        if (symbol == null) return;
        
        StockMarket.Stock stock = StockMarket.getStockBySymbol(symbol);
        if (stock == null) return;
        
        double price = StockMarket.getPrice(symbol);
        double change = StockMarket.calculateChange(symbol);
        double totalChange = StockMarket.calculateTotalChange(symbol);
        
        ChatFormatting changeColor = change >= 0 ? ChatFormatting.GREEN : ChatFormatting.RED;
        ChatFormatting totalColor = totalChange >= 0 ? ChatFormatting.GREEN : ChatFormatting.RED;
        
        // Update stock info
        gui.setSlot(4, new GuiElementBuilder(getStockDisplayItem(stock).getItem())
                .setName(Component.literal(stock.icon + " " + stock.symbol).withStyle(ChatFormatting.BOLD))
                .addLoreLine(Component.literal(stock.name).withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§6Current Price: " + StockMarket.formatPrice(price)))
                .addLoreLine(Component.literal("§7Session Change: " + (change >= 0 ? "+" : "") + 
                        String.format("%.2f", change) + "%").withStyle(changeColor))
                .addLoreLine(Component.literal("§7Total Change: " + (totalChange >= 0 ? "+" : "") + 
                        String.format("%.2f", totalChange) + "%").withStyle(totalColor))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Volatility: " + String.format("%.0f%%", stock.volatility * 100)))
                .glow().build());
        
        // Update holdings
        int owned = StockMarket.getPortfolio(player.getUUID().toString()).getOrDefault(symbol, 0);
        gui.setSlot(13, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Component.literal("Your Holdings").withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Shares Owned: §f" + owned))
                .addLoreLine(Component.literal("§7Value: §6" + StockMarket.formatPrice(price * owned)))
                .build());
        
        // Update buy buttons with current prices
        gui.setSlot(20, new GuiElementBuilder(Items.DYE.lime())
                .setName(Component.literal("Buy 1 Share").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Cost: §6" + StockMarket.formatPrice(price) + " coins"))
                .build());
        
        gui.setSlot(21, new GuiElementBuilder(Items.DYE.lime())
                .setName(Component.literal("Buy 10 Shares").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Cost: §6" + StockMarket.formatPrice(price * 10) + " coins"))
                .build());
        
        gui.setSlot(22, new GuiElementBuilder(Items.DYE.lime())
                .setName(Component.literal("Buy 100 Shares").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Cost: §6" + StockMarket.formatPrice(price * 100) + " coins"))
                .build());
    }

    // ═══════════════════════════════════════════════════════════════
    // MAIN MENU
    // ═══════════════════════════════════════════════════════════════

    public static void openMainMenu(ServerPlayer player) {
        // Close existing GUI if open
        SimpleGui existingGui = openStockGuis.remove(player.getUUID());
        if (existingGui != null && existingGui.isOpen()) {
            existingGui.close();
        }
        
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal("📈 Stock Market"));

        // Track this GUI
        openStockGuis.put(player.getUUID(), gui);
        guiViewType.put(player.getUUID(), "main");

        // Background
        GuiElementBuilder bg = new GuiElementBuilder(Items.STAINED_GLASS_PANE.gray()).setName(Component.literal(""));
        for (int i = 0; i < 27; i++) gui.setSlot(i, bg.build());

        // Header
        StockMarket.MarketTrend trend = StockMarket.getCurrentTrend();
        
        // Get countdown for next price update
        long currentTick = PoliticalServer.server.getTickCount();
        String countdown = StockMarket.getCountdownString(currentTick);
        
        gui.setSlot(4, new GuiElementBuilder(Items.GOLD_BLOCK)
                .setName(Component.literal("📈 Stock Market").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal(trend.name).withStyle(trend.color))
                .addLoreLine(Component.literal("Market: " + (StockMarket.isMarketOpen() ? "§aOpen" : "§cClosed")))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Your Portfolio Value: §6" + 
                        StockMarket.formatPrice(StockMarket.getPortfolioValue(player.getUUID().toString())) + " coins"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§e⏱ Next update: " + countdown).withStyle(ChatFormatting.YELLOW))
                .glow().build());

        // Browse Stocks
        gui.setSlot(10, new GuiElementBuilder(Items.KNOWLEDGE_BOOK)
                .setName(Component.literal("📊 Browse Stocks").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7View all available stocks"))
                .addLoreLine(Component.literal("§7Check prices and trends"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to browse"))
                .setCallback((idx, type, containerInput, slotGui) -> openStockBrowser(player, 0))
                .build());

        // My Portfolio
        gui.setSlot(12, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Component.literal("💼 My Portfolio").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7View your holdings"))
                .addLoreLine(Component.literal("§7Track your investments"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to view"))
                .setCallback((idx, type, containerInput, slotGui) -> openPortfolio(player, 0))
                .build());

        // Market News
        gui.setSlot(14, new GuiElementBuilder(Items.WRITTEN_BOOK)
                .setName(Component.literal("📰 Market News").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Active market events"))
                .addLoreLine(Component.literal("§7Affecting stock prices"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to view"))
                .setCallback((idx, type, containerInput, slotGui) -> openMarketNews(player))
                .build());

        // Top Movers
        gui.setSlot(16, new GuiElementBuilder(Items.SPECTRAL_ARROW)
                .setName(Component.literal("🚀 Top Movers").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Biggest gainers and losers"))
                .addLoreLine(Component.literal("§7In the last trading session"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to view"))
                .setCallback((idx, type, containerInput, slotGui) -> openTopMovers(player))
                .build());

        gui.open();
    }

    // ═══════════════════════════════════════════════════════════════
    // PORTFOLIO VIEW
    // ═══════════════════════════════════════════════════════════════

    public static void openPortfolio(ServerPlayer player, int page) {
        // Close existing GUI if open
        SimpleGui existingGui = openStockGuis.remove(player.getUUID());
        if (existingGui != null && existingGui.isOpen()) {
            existingGui.close();
        }
        
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("💼 Your Portfolio"));

        // Track this GUI
        openStockGuis.put(player.getUUID(), gui);
        guiViewType.put(player.getUUID(), "portfolio");
        guiPage.put(player.getUUID(), page);

        // Background
        GuiElementBuilder bg = new GuiElementBuilder(Items.STAINED_GLASS_PANE.gray()).setName(Component.literal(""));
        for (int i = 0; i < 54; i++) gui.setSlot(i, bg.build());

        // Header
        double portfolioValue = StockMarket.getPortfolioValue(player.getUUID().toString());
        int liquidCash = CoinManager.getCoins(player);
        
        gui.setSlot(4, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Component.literal("💼 Portfolio Summary").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Portfolio Value: §6" + StockMarket.formatPrice(portfolioValue)).withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal("Liquid Cash: §6" + CoinManager.getCoins(player) + " coins").withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Total Net Worth: §6" + StockMarket.formatPrice(portfolioValue + liquidCash)).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .glow().build());

        // Get portfolio holdings
        java.util.Map<String, Integer> portfolio = StockMarket.getPortfolio(player.getUUID().toString());
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
                    .setName(Component.literal(symbol).withStyle(ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Shares: §f" + shares).withStyle(ChatFormatting.WHITE))
                    .addLoreLine(Component.literal("Price: §6" + StockMarket.formatPrice(price)).withStyle(ChatFormatting.YELLOW))
                    .addLoreLine(Component.literal("Value: §6" + StockMarket.formatPrice(value)).withStyle(ChatFormatting.GOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§eClick to trade"))
                    .setCallback((idx, type, containerInput, slotGui) -> openStockDetail(player, symbol))
                    .build());
        }

        // Back button
        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.RED))
                .setCallback((idx, type, containerInput, slotGui) -> openMainMenu(player))
                .build());

        // Navigation
        if (page > 0) {
            gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("← Previous").withStyle(ChatFormatting.YELLOW))
                    .setCallback((idx, type, containerInput, slotGui) -> openPortfolio(player, page - 1))
                    .build());
        }
        
        if (page < totalPages - 1) {
            gui.setSlot(53, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("Next →").withStyle(ChatFormatting.YELLOW))
                    .setCallback((idx, type, containerInput, slotGui) -> openPortfolio(player, page + 1))
                    .build());
        }

        gui.open();
    }

    // ═══════════════════════════════════════════════════════════════
    // STOCK BROWSER
    // ═══════════════════════════════════════════════════════════════

    public static void openStockBrowser(ServerPlayer player, int page) {
        // Close existing GUI if open
        SimpleGui existingGui = openStockGuis.remove(player.getUUID());
        if (existingGui != null && existingGui.isOpen()) {
            existingGui.close();
        }
        
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("📊 Browse Stocks"));

        // Track this GUI
        openStockGuis.put(player.getUUID(), gui);
        guiViewType.put(player.getUUID(), "browser");
        guiPage.put(player.getUUID(), page);

        // Background
        GuiElementBuilder bg = new GuiElementBuilder(Items.STAINED_GLASS_PANE.gray()).setName(Component.literal(""));
        for (int i = 0; i < 54; i++) gui.setSlot(i, bg.build());

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.KNOWLEDGE_BOOK)
                .setName(Component.literal("📊 All Stocks").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Page " + (page + 1)))
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
            
            ChatFormatting changeColor = change >= 0 ? ChatFormatting.GREEN : ChatFormatting.RED;
            String changeStr = (change >= 0 ? "+" : "") + String.format("%.2f", change) + "%";
            
            // Determine item based on sector
            ItemStack displayItem = getStockDisplayItem(stock);
            
            final String symbol = stock.symbol;
            gui.setSlot(slot, new GuiElementBuilder(displayItem.getItem())
                    .setName(Component.literal(stock.icon + " " + stock.symbol).withStyle(ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(stock.name).withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§6Price: " + StockMarket.formatPrice(price) + " coins"))
                    .addLoreLine(Component.literal("§7Change: " + changeStr).withStyle(changeColor))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§eClick to trade"))
                    .setCallback((idx, type, containerInput, slotGui) -> openStockDetail(player, symbol))
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
                    .setName(Component.literal("← Previous Page").withStyle(ChatFormatting.YELLOW))
                    .setCallback((idx, type, containerInput, slotGui) -> openStockBrowser(player, prev))
                    .build());
        }
        
        if ((page + 1) * itemsPerPage < stocks.length) {
            final int next = page + 1;
            gui.setSlot(53, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("Next Page →").withStyle(ChatFormatting.YELLOW))
                    .setCallback((idx, type, containerInput, slotGui) -> openStockBrowser(player, next))
                    .build());
        }

        // Back button
        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> openMainMenu(player))
                .build());

        gui.open();
    }

    // ═══════════════════════════════════════════════════════════════
    // STOCK DETAIL & TRADING
    // ═══════════════════════════════════════════════════════════════

    public static void openStockDetail(ServerPlayer player, String symbol) {
        StockMarket.Stock stock = StockMarket.getStockBySymbol(symbol);
        if (stock == null) return;

        // Close existing GUI if open
        SimpleGui existingGui = openStockGuis.remove(player.getUUID());
        if (existingGui != null && existingGui.isOpen()) {
            existingGui.close();
        }

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x4, player, false);
        gui.setTitle(Component.literal(stock.symbol + " Trading"));

        // Track this GUI
        openStockGuis.put(player.getUUID(), gui);
        guiViewType.put(player.getUUID(), "detail");
        guiSymbol.put(player.getUUID(), symbol);

        // Background
        GuiElementBuilder bg = new GuiElementBuilder(Items.STAINED_GLASS_PANE.gray()).setName(Component.literal(""));
        for (int i = 0; i < 36; i++) gui.setSlot(i, bg.build());

        double price = StockMarket.getPrice(symbol);
        double change = StockMarket.calculateChange(symbol);
        double totalChange = StockMarket.calculateTotalChange(symbol);
        
        ChatFormatting changeColor = change >= 0 ? ChatFormatting.GREEN : ChatFormatting.RED;
        ChatFormatting totalColor = totalChange >= 0 ? ChatFormatting.GREEN : ChatFormatting.RED;

        // Stock info
        gui.setSlot(4, new GuiElementBuilder(getStockDisplayItem(stock).getItem())
                .setName(Component.literal(stock.icon + " " + stock.symbol).withStyle(ChatFormatting.BOLD))
                .addLoreLine(Component.literal(stock.name).withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§6Current Price: " + StockMarket.formatPrice(price)))
                .addLoreLine(Component.literal("§7Session Change: " + (change >= 0 ? "+" : "") + 
                        String.format("%.2f", change) + "%").withStyle(changeColor))
                .addLoreLine(Component.literal("§7Total Change: " + (totalChange >= 0 ? "+" : "") + 
                        String.format("%.2f", totalChange) + "%").withStyle(totalColor))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Volatility: " + String.format("%.0f%%", stock.volatility * 100)))
                .glow().build());

        // Player's holdings
        int owned = StockMarket.getPortfolio(player.getUUID().toString()).getOrDefault(symbol, 0);
        gui.setSlot(13, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Component.literal("Your Holdings").withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Shares Owned: §f" + owned))
                .addLoreLine(Component.literal("§7Value: §6" + StockMarket.formatPrice(price * owned)))
                .build());

        // Buy buttons
        final String sym = symbol;
        String pu = player.getUUID().toString();
        gui.setSlot(20, new GuiElementBuilder(Items.DYE.lime())
                .setName(Component.literal("Buy 1 Share").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Cost: §6" + StockMarket.formatPrice(StockMarket.estimateBuyCoinCost(pu, sym, 1)) + " coins"))
                .setCallback((idx, type, containerInput, slotGui) -> {
                    StockMarket.buyStock(player, sym, 1);
                    openStockDetail(player, sym);
                })
                .build());

        gui.setSlot(21, new GuiElementBuilder(Items.DYE.lime())
                .setName(Component.literal("Buy 10 Shares").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Cost: §6" + StockMarket.formatPrice(StockMarket.estimateBuyCoinCost(pu, sym, 10)) + " coins"))
                .setCallback((idx, type, containerInput, slotGui) -> {
                    StockMarket.buyStock(player, sym, 10);
                    openStockDetail(player, sym);
                })
                .build());

        gui.setSlot(22, new GuiElementBuilder(Items.DYE.lime())
                .setName(Component.literal("Buy 100 Shares").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Cost: §6" + StockMarket.formatPrice(StockMarket.estimateBuyCoinCost(pu, sym, 100)) + " coins"))
                .setCallback((idx, type, containerInput, slotGui) -> {
                    StockMarket.buyStock(player, sym, 100);
                    openStockDetail(player, sym);
                })
                .build());

        // Custom buy button with sign input
        gui.setSlot(23, new GuiElementBuilder(Items.WRITABLE_BOOK)
                .setName(Component.literal("Custom Buy").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Enter custom amount"))
                .addLoreLine(Component.literal("§7Supports k/m suffixes"))
                .addLoreLine(Component.literal("§eClick to enter amount"))
                .setCallback((idx, type, containerInput, slotGui) -> openCustomBuySign(player, sym))
                .build());

        // Sell buttons
        if (owned > 0) {
            gui.setSlot(24, new GuiElementBuilder(Items.DYE.red())
                    .setName(Component.literal("Sell 1 Share").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7Value: §6" + StockMarket.formatPrice(price) + " coins"))
                    .setCallback((idx, type, containerInput, slotGui) -> {
                        StockMarket.sellStock(player, sym, 1);
                        openStockDetail(player, sym);
                    })
                    .build());

            gui.setSlot(25, new GuiElementBuilder(Items.DYE.red())
                    .setName(Component.literal("Sell 10 Shares").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7Value: §6" + StockMarket.formatPrice(price * 10) + " coins"))
                    .setCallback((idx, type, containerInput, slotGui) -> {
                        StockMarket.sellStock(player, sym, 10);
                        openStockDetail(player, sym);
                    })
                    .build());

            gui.setSlot(26, new GuiElementBuilder(Items.DYE.red())
                    .setName(Component.literal("Sell All (" + owned + ")").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7Value: §6" + StockMarket.formatPrice(price * owned) + " coins"))
                    .setCallback((idx, type, containerInput, slotGui) -> {
                        StockMarket.sellStock(player, sym, owned);
                        openStockDetail(player, sym);
                    })
                    .build());

            // Custom sell button with sign input
            gui.setSlot(27, new GuiElementBuilder(Items.WRITABLE_BOOK)
                    .setName(Component.literal("Custom Sell").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7Enter custom amount"))
                    .addLoreLine(Component.literal("§7Supports k/m suffixes"))
                    .addLoreLine(Component.literal("§eClick to enter amount"))
                    .setCallback((idx, type, containerInput, slotGui) -> openCustomSellSign(player, sym, owned))
                    .build());
        }

        // Back button
        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> openStockBrowser(player, 0))
                .build());
        
        // Price graph button
        gui.setSlot(32, new GuiElementBuilder(Items.MAP)
                .setName(Component.literal("📊 Price Graph").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7View price history"))
                .addLoreLine(Component.literal("§eClick to view"))
                .setCallback((idx, type, containerInput, slotGui) -> openPriceGraph(player, sym))
                .build());

        gui.open();
    }
    // ═══════════════════════════════════════════════════════════════
    // MARKET NEWS
    // ═══════════════════════════════════════════════════════════════

    public static void openMarketNews(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal("📰 Market News"));

        // Background
        GuiElementBuilder bg = new GuiElementBuilder(Items.STAINED_GLASS_PANE.gray()).setName(Component.literal(""));
        for (int i = 0; i < 27; i++) gui.setSlot(i, bg.build());

        List<StockMarket.MarketEvent> events = StockMarket.getActiveEvents();
        
        gui.setSlot(4, new GuiElementBuilder(Items.WRITTEN_BOOK)
                .setName(Component.literal("📰 Market News").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Active events affecting prices"))
                .glow().build());

        if (events.isEmpty()) {
            gui.setSlot(13, new GuiElementBuilder(Items.BARRIER)
                    .setName(Component.literal("No Active Events").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§7The market is calm right now."))
                    .addLoreLine(Component.literal("§7No major events are affecting prices."))
                    .build());
        } else {
            int slot = 10;
            for (StockMarket.MarketEvent event : events) {
                if (slot > 16) break;
                
                ChatFormatting color = event.priceModifier >= 0 ? ChatFormatting.GREEN : ChatFormatting.RED;
                gui.setSlot(slot, new GuiElementBuilder(event.priceModifier >= 0 ? Items.DYE.lime() : Items.DYE.red())
                        .setName(Component.literal("⚡ " + event.name).withStyle(color, ChatFormatting.BOLD))
                        .addLoreLine(Component.literal(event.description).withStyle(ChatFormatting.GRAY))
                        .addLoreLine(Component.literal(""))
                        .addLoreLine(Component.literal("§7Effect: " + (event.priceModifier >= 0 ? "+" : "") + 
                                String.format("%.0f%%", event.priceModifier * 100)).withStyle(color))
                        .addLoreLine(Component.literal("§7Stocks: " + String.join(", ", event.affectedStocks)))
                        .build());
                slot++;
            }
        }

        // Back button
        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> openMainMenu(player))
                .build());

        gui.open();
    }

    // ═══════════════════════════════════════════════════════════════
    // TOP MOVERS
    // ═══════════════════════════════════════════════════════════════

    public static void openTopMovers(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal("🚀 Top Movers"));

        // Background
        GuiElementBuilder bg = new GuiElementBuilder(Items.STAINED_GLASS_PANE.gray()).setName(Component.literal(""));
        for (int i = 0; i < 27; i++) gui.setSlot(i, bg.build());

        // Find top gainers and losers
        List<Map.Entry<String, Double>> changes = new ArrayList<>();
        for (StockMarket.Stock stock : StockMarket.Stock.values()) {
            changes.add(Map.entry(stock.symbol, StockMarket.calculateChange(stock.symbol)));
        }
        
        changes.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        
        // Top 3 gainers
        gui.setSlot(4, new GuiElementBuilder(Items.SPECTRAL_ARROW)
                .setName(Component.literal("🚀 Top Movers").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .glow().build());

        // Gainers (left side)
        for (int i = 0; i < 3 && i < changes.size(); i++) {
            Map.Entry<String, Double> entry = changes.get(i);
            StockMarket.Stock stock = StockMarket.getStockBySymbol(entry.getKey());
            if (stock == null) continue;
            
            final String symbol = entry.getKey();
            gui.setSlot(10 + i, new GuiElementBuilder(Items.DYE.lime())
                    .setName(Component.literal("📈 " + stock.symbol).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(stock.name).withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§a+" + String.format("%.2f", entry.getValue()) + "%"))
                    .addLoreLine(Component.literal("§6" + StockMarket.formatPrice(StockMarket.getPrice(symbol))))
                    .setCallback((idx, type, containerInput, slotGui) -> openStockDetail(player, symbol))
                    .build());
        }

        // Losers (right side)
        for (int i = 0; i < 3 && i < changes.size(); i++) {
            int idx = changes.size() - 1 - i;
            Map.Entry<String, Double> entry = changes.get(idx);
            StockMarket.Stock stock = StockMarket.getStockBySymbol(entry.getKey());
            if (stock == null) continue;
            
            final String symbol = entry.getKey();
            gui.setSlot(14 + i, new GuiElementBuilder(Items.DYE.red())
                    .setName(Component.literal("📉 " + stock.symbol).withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(stock.name).withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§c" + String.format("%.2f", entry.getValue()) + "%"))
                    .addLoreLine(Component.literal("§6" + StockMarket.formatPrice(StockMarket.getPrice(symbol))))
                    .setCallback((idx2, type, containerInput, slotGui) -> openStockDetail(player, symbol))
                    .build());
        }

        // Back button
        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> openMainMenu(player))
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

    private static void openCustomBuySign(ServerPlayer player, String symbol) {
        SignGui signGui = new SignGui(player) {
            @Override
            public void onManualClose() {
                String input = this.getLine(0).getString().trim();
                int amount = parseAmountWithSuffix(input);
                if (amount <= 0) {
                    player.sendSystemMessage(Component.literal("✗ Invalid amount!").withStyle(ChatFormatting.RED), false);
                    openStockDetail(player, symbol);
                    return;
                }
                StockMarket.buyStock(player, symbol, amount);
                openStockDetail(player, symbol);
            }
        };
        signGui.setLine(0, Component.literal(""));
        signGui.setLine(1, Component.literal("^^^^^^^^^^^^^^").withStyle(ChatFormatting.YELLOW));
        signGui.setLine(2, Component.literal("Enter shares").withStyle(ChatFormatting.AQUA));
        signGui.setLine(3, Component.literal("(k=1000, m=1M)").withStyle(ChatFormatting.GRAY));
        signGui.open();
    }

    private static void openCustomSellSign(ServerPlayer player, String symbol, int maxShares) {
        SignGui signGui = new SignGui(player) {
            @Override
            public void onManualClose() {
                String input = this.getLine(0).getString().trim();
                int amount = parseAmountWithSuffix(input);
                if (amount <= 0) {
                    player.sendSystemMessage(Component.literal("✗ Invalid amount!").withStyle(ChatFormatting.RED), false);
                    openStockDetail(player, symbol);
                    return;
                }
                if (amount > maxShares) {
                    player.sendSystemMessage(Component.literal("✗ You only have " + maxShares + " shares!").withStyle(ChatFormatting.RED), false);
                    openStockDetail(player, symbol);
                    return;
                }
                StockMarket.sellStock(player, symbol, amount);
                openStockDetail(player, symbol);
            }
        };
        signGui.setLine(0, Component.literal(""));
        signGui.setLine(1, Component.literal("^^^^^^^^^^^^^^").withStyle(ChatFormatting.YELLOW));
        signGui.setLine(2, Component.literal("Enter shares").withStyle(ChatFormatting.AQUA));
        signGui.setLine(3, Component.literal("Max: " + maxShares).withStyle(ChatFormatting.GRAY));
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

    public static void openPriceGraph(ServerPlayer player, String symbol) {
        StockMarket.Stock stock = StockMarket.getStockBySymbol(symbol);
        if (stock == null) return;

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("📊 " + symbol + " Price Chart"));

        // Dark background for better contrast
        GuiElementBuilder bg = new GuiElementBuilder(Items.STAINED_GLASS_PANE.black()).setName(Component.literal(""));
        for (int i = 0; i < 54; i++) gui.setSlot(i, bg.build());

        List<Double> history = StockMarket.getPriceHistory(symbol);
        double currentPrice = StockMarket.getPrice(symbol);
        
        // Header with current price info
        double priceChange = history.size() >= 2 ? currentPrice - history.get(history.size() - 2) : 0;
        ChatFormatting priceColor = priceChange >= 0 ? ChatFormatting.GREEN : ChatFormatting.RED;
        String changeSymbol = priceChange >= 0 ? "▲" : "▼";
        
        gui.setSlot(4, new GuiElementBuilder(getStockDisplayItem(stock).getItem())
                .setName(Component.literal(stock.icon + " " + stock.name).withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Current: ").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(StockMarket.formatPrice(currentPrice)).withStyle(ChatFormatting.WHITE)))
                .addLoreLine(Component.literal("Change: ").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(changeSymbol + " " + StockMarket.formatPrice(Math.abs(priceChange))).withStyle(priceColor)))
                .addLoreLine(Component.literal("Data Points: ").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(String.valueOf(history.size())).withStyle(ChatFormatting.WHITE)))
                .glow().build());

        if (history.size() < 2) {
            gui.setSlot(22, new GuiElementBuilder(Items.BARRIER)
                    .setName(Component.literal("Insufficient Data").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7Need more price updates to"))
                    .addLoreLine(Component.literal("§7display the candlestick chart."))
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
                        Item wickItem = isUp ? Items.STAINED_GLASS_PANE.lime() : Items.STAINED_GLASS_PANE.red();
                        gui.setSlot(slot, new GuiElementBuilder(wickItem)
                                .setName(Component.literal(isUp ? "§a▲ Bullish" : "§c▼ Bearish").withStyle(ChatFormatting.BOLD))
                                .addLoreLine(Component.literal(""))
                                .addLoreLine(Component.literal("Open: ").withStyle(ChatFormatting.GRAY)
                                        .append(Component.literal(StockMarket.formatPrice(price)).withStyle(ChatFormatting.WHITE)))
                                .addLoreLine(Component.literal("Close: ").withStyle(ChatFormatting.GRAY)
                                        .append(Component.literal(StockMarket.formatPrice(nextPrice)).withStyle(isUp ? ChatFormatting.GREEN : ChatFormatting.RED)))
                                .addLoreLine(Component.literal("Range: ").withStyle(ChatFormatting.GRAY)
                                        .append(Component.literal(StockMarket.formatPrice(wickHigh - wickLow)).withStyle(ChatFormatting.YELLOW)))
                                .build());
                    }
                }
                
                // Draw body (thicker part) at center
                int bodySlot = graphStartSlot + (bodyCenterRow * 9) + col;
                if (bodySlot >= 0 && bodySlot < 54) {
                    Item bodyItem = isUp ? Items.CONCRETE.lime() : Items.CONCRETE.red();
                    gui.setSlot(bodySlot, new GuiElementBuilder(bodyItem)
                            .setName(Component.literal(isUp ? "§a▲ Bullish Candle" : "§c▼ Bearish Candle").withStyle(ChatFormatting.BOLD))
                            .addLoreLine(Component.literal(""))
                            .addLoreLine(Component.literal("Open: ").withStyle(ChatFormatting.GRAY)
                                    .append(Component.literal(StockMarket.formatPrice(price)).withStyle(ChatFormatting.WHITE)))
                            .addLoreLine(Component.literal("Close: ").withStyle(ChatFormatting.GRAY)
                                    .append(Component.literal(StockMarket.formatPrice(nextPrice)).withStyle(isUp ? ChatFormatting.GREEN : ChatFormatting.RED)))
                            .addLoreLine(Component.literal("Change: ").withStyle(ChatFormatting.GRAY)
                                    .append(Component.literal(StockMarket.formatPrice(Math.abs(nextPrice - price))).withStyle(isUp ? ChatFormatting.GREEN : ChatFormatting.RED)))
                            .glow().build());
                }
            }
            
            // Legend with better styling
            gui.setSlot(47, new GuiElementBuilder(Items.CONCRETE.lime())
                    .setName(Component.literal("Bullish Candle").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7Price went UP"))
                    .build());
            gui.setSlot(48, new GuiElementBuilder(Items.CONCRETE.red())
                    .setName(Component.literal("Bearish Candle").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7Price went DOWN"))
                    .build());
            gui.setSlot(49, new GuiElementBuilder(Items.GOLD_NUGGET)
                    .setName(Component.literal("Price Range").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7High to Low wick"))
                    .build());
            
            // Scale info
            gui.setSlot(50, new GuiElementBuilder(Items.PAPER)
                    .setName(Component.literal("Chart Scale").withStyle(ChatFormatting.AQUA))
                    .addLoreLine(Component.literal("Max: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(StockMarket.formatPrice(maxPrice)).withStyle(ChatFormatting.WHITE)))
            .addLoreLine(Component.literal("Min: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(StockMarket.formatPrice(minPrice)).withStyle(ChatFormatting.WHITE)))
                    .build());
        }

        // Back button
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back to Market").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> openStockDetail(player, symbol))
                .build());

        gui.open();
    }
}
