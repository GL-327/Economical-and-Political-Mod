package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * GUI system for the stock market with multiple pages and trading functionality.
 */
public class StockMarketGui {

    // ═══════════════════════════════════════════════════════════════
    // MAIN MENU
    // ═══════════════════════════════════════════════════════════════

    public static void openMainMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
        gui.setTitle(Text.literal("📈 Stock Market"));

        // Background
        GuiElementBuilder bg = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE).setName(Text.literal(""));
        for (int i = 0; i < 27; i++) gui.setSlot(i, bg.build());

        // Header
        StockMarket.MarketTrend trend = StockMarket.getCurrentTrend();
        gui.setSlot(4, new GuiElementBuilder(Items.GOLD_BLOCK)
                .setName(Text.literal("📈 Stock Market").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal(trend.name).formatted(trend.color))
                .addLoreLine(Text.literal("Market: " + (StockMarket.isMarketOpen() ? "§aOpen" : "§cClosed")))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§7Your Portfolio Value: §6" + 
                        StockMarket.formatPrice(StockMarket.getPortfolioValue(player.getUuidAsString())) + " coins"))
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
    // STOCK BROWSER
    // ═══════════════════════════════════════════════════════════════

    public static void openStockBrowser(ServerPlayerEntity player, int page) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("📊 Browse Stocks"));

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

        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X4, player, false);
        gui.setTitle(Text.literal(stock.symbol + " Trading"));

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
        }

        // Back button
        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openStockBrowser(player, 0))
                .build());

        gui.open();
    }

    // ═══════════════════════════════════════════════════════════════
    // PORTFOLIO VIEW
    // ═══════════════════════════════════════════════════════════════

    public static void openPortfolio(ServerPlayerEntity player, int page) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("💼 My Portfolio"));

        // Background
        GuiElementBuilder bg = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE).setName(Text.literal(""));
        for (int i = 0; i < 54; i++) gui.setSlot(i, bg.build());

        Map<String, Integer> portfolio = StockMarket.getPortfolio(player.getUuidAsString());
        long totalValue = StockMarket.getPortfolioValue(player.getUuidAsString());

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Text.literal("💼 Your Portfolio").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§6Total Value: " + StockMarket.formatPrice(totalValue) + " coins"))
                .addLoreLine(Text.literal("§7Holdings: " + portfolio.size() + " different stocks"))
                .glow().build());

        if (portfolio.isEmpty()) {
            gui.setSlot(22, new GuiElementBuilder(Items.BARRIER)
                    .setName(Text.literal("No Holdings").formatted(Formatting.RED))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("§7You don't own any stocks yet."))
                    .addLoreLine(Text.literal("§7Browse stocks to start investing!"))
                    .build());
        } else {
            List<Map.Entry<String, Integer>> entries = new ArrayList<>(portfolio.entrySet());
            int itemsPerPage = 28;
            int startIdx = page * itemsPerPage;
            
            int slot = 10;
            int count = 0;
            
            for (int i = startIdx; i < entries.size() && count < itemsPerPage; i++) {
                Map.Entry<String, Integer> entry = entries.get(i);
                StockMarket.Stock stock = StockMarket.getStockBySymbol(entry.getKey());
                if (stock == null) continue;
                
                double price = StockMarket.getPrice(entry.getKey());
                double value = price * entry.getValue();
                double change = StockMarket.calculateChange(entry.getKey());
                
                Formatting changeColor = change >= 0 ? Formatting.GREEN : Formatting.RED;
                
                final String symbol = entry.getKey();
                gui.setSlot(slot, new GuiElementBuilder(getStockDisplayItem(stock).getItem())
                        .setName(Text.literal(stock.icon + " " + stock.symbol).formatted(Formatting.BOLD))
                        .addLoreLine(Text.literal("§7Shares: §f" + entry.getValue()))
                        .addLoreLine(Text.literal("§6Value: " + StockMarket.formatPrice(value)))
                        .addLoreLine(Text.literal("§7Change: " + (change >= 0 ? "+" : "") + 
                                String.format("%.2f", change) + "%").formatted(changeColor))
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
            
            // Pagination
            if (page > 0) {
                final int prev = page - 1;
                gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                        .setName(Text.literal("← Previous Page").formatted(Formatting.YELLOW))
                        .setCallback((idx, type, action) -> openPortfolio(player, prev))
                        .build());
            }
            
            if ((page + 1) * itemsPerPage < entries.size()) {
                final int next = page + 1;
                gui.setSlot(53, new GuiElementBuilder(Items.ARROW)
                        .setName(Text.literal("Next Page →").formatted(Formatting.YELLOW))
                        .setCallback((idx, type, action) -> openPortfolio(player, next))
                        .build());
            }
        }

        // Back button
        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openMainMenu(player))
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

    private static ItemStack getStockDisplayItem(StockMarket.Stock stock) {
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
}
