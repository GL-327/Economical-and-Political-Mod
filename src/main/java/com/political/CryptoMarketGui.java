package com.political;


import com.political.economy.CryptoMarket;
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
                    // detail: do not refresh — rebuilding slots strips click handlers from buy/sell buttons
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
        CryptoMarket.MarketPhase phase = CryptoMarket.getCurrentPhase();
        double fgIndex = CryptoMarket.getFearGreedIndex();
        ChatFormatting fgColor = fgIndex < 40 ? ChatFormatting.RED : (fgIndex < 60 ? ChatFormatting.YELLOW : ChatFormatting.GREEN);
        long currentTick = PoliticalServer.server.getTickCount();
        String countdown = CryptoMarket.getCountdownString(currentTick);
        
        gui.setSlot(4, new GuiElementBuilder(Items.END_CRYSTAL)
                .setName(Component.literal("🔮 Crypto Market").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal(phase.name).withStyle(phase.color))
                .addLoreLine(Component.literal("Fear & Greed: " + String.format("%.0f", fgIndex) + " (" + 
                        CryptoMarket.getFearGreedLabel() + ")").withStyle(fgColor))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Total Market Cap: §6" + 
                        CryptoMarket.formatCrypto(CryptoMarket.getTotalMarketCap())))
                .addLoreLine(Component.literal("§7Your Wallet: §6" + 
                        CryptoMarket.formatCrypto(CryptoMarket.getWalletValue(player.getUUID().toString())) + " USD"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§e⏱ Next update: " + countdown).withStyle(ChatFormatting.YELLOW))
                .glow().build());
    }

    private static void refreshBrowserView(SimpleGui gui, UUID uuid) {
        ServerPlayer player = gui.getPlayer();
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
            
            ChatFormatting changeColor = change >= 0 ? ChatFormatting.GREEN : ChatFormatting.RED;
            String changeStr = (change >= 0 ? "+" : "") + String.format("%.2f", change) + "%";
            
            gui.setSlot(slot, new GuiElementBuilder(getCryptoDisplayItem(crypto).getItem())
                    .setName(Component.literal(crypto.icon + " " + crypto.symbol).withStyle(ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(crypto.name).withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§6Price: $" + CryptoMarket.formatCrypto(price)))
                    .addLoreLine(Component.literal("§7Change: " + changeStr).withStyle(changeColor))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§eClick to trade"))
                    .setCallback((idx, type, containerInput, slotGui) -> openCryptoDetail(player, symbol))
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
        ServerPlayer player = gui.getPlayer();
        if (player == null) return;
        
        double walletValue = CryptoMarket.getWalletValue(player.getUUID().toString());
        
        gui.setSlot(4, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Component.literal("💼 Your Wallet").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Total Value: §6$" + CryptoMarket.formatCrypto(walletValue)).withStyle(ChatFormatting.YELLOW))
                .glow().build());
        
        Map<String, Double> wallet = CryptoMarket.getWallet(player.getUUID().toString());
        int slot = 9;
        
        for (Map.Entry<String, Double> entry : wallet.entrySet()) {
            if (slot >= 45) break;
            
            String symbol = entry.getKey();
            double amount = entry.getValue();
            double price = CryptoMarket.getPrice(symbol);
            double value = price * amount;
            double change = CryptoMarket.calculateChange(symbol);
            
            ChatFormatting changeColor = change >= 0 ? ChatFormatting.GREEN : ChatFormatting.RED;
            CryptoMarket.Crypto crypto = CryptoMarket.getCryptoBySymbol(symbol);
            
            if (crypto != null) {
                gui.setSlot(slot, new GuiElementBuilder(getCryptoDisplayItem(crypto).getItem())
                        .setName(Component.literal(symbol).withStyle(ChatFormatting.BOLD))
                        .addLoreLine(Component.literal(""))
                        .addLoreLine(Component.literal("Amount: §f" + CryptoMarket.formatCrypto(amount)).withStyle(ChatFormatting.WHITE))
                        .addLoreLine(Component.literal("Price: §6$" + CryptoMarket.formatCrypto(price)).withStyle(ChatFormatting.YELLOW))
                        .addLoreLine(Component.literal("Value: §6$" + CryptoMarket.formatCrypto(value)).withStyle(ChatFormatting.GOLD))
                        .addLoreLine(Component.literal("Change: " + (change >= 0 ? "+" : "") + String.format("%.2f", change) + "%").withStyle(changeColor))
                        .addLoreLine(Component.literal(""))
                        .addLoreLine(Component.literal("§eClick to trade"))
                        .setCallback((idx, type, containerInput, slotGui) -> openCryptoDetail(player, symbol))
                        .build());
            }
            slot++;
        }
    }

    private static void refreshDetailView(SimpleGui gui, UUID uuid) {
        ServerPlayer player = gui.getPlayer();
        if (player == null) return;
        
        String symbol = guiSymbol.get(uuid);
        if (symbol == null) return;
        
        CryptoMarket.Crypto crypto = CryptoMarket.getCryptoBySymbol(symbol);
        if (crypto == null) return;
        
        double price = CryptoMarket.getPrice(symbol);
        double change = CryptoMarket.calculateChange(symbol);
        
        ChatFormatting changeColor = change >= 0 ? ChatFormatting.GREEN : ChatFormatting.RED;
        
        gui.setSlot(4, new GuiElementBuilder(getCryptoDisplayItem(crypto).getItem())
                .setName(Component.literal(crypto.icon + " " + crypto.symbol).withStyle(ChatFormatting.BOLD))
                .addLoreLine(Component.literal(crypto.name).withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§6Current Price: $" + CryptoMarket.formatCrypto(price)))
                .addLoreLine(Component.literal("§7Session Change: " + (change >= 0 ? "+" : "") + 
                        String.format("%.2f", change) + "%").withStyle(changeColor))
                .glow().build());
        
        double owned = CryptoMarket.getWallet(player.getUUID().toString()).getOrDefault(symbol, 0.0);
        gui.setSlot(13, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Component.literal("Your Holdings").withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Owned: §f" + CryptoMarket.formatCrypto(owned)))
                .addLoreLine(Component.literal("§7Value: §6$" + CryptoMarket.formatCrypto(price * owned)))
                .build());
        
        gui.setSlot(20, new GuiElementBuilder(Items.DYE.lime())
                .setName(Component.literal("Buy 0.1 " + symbol).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Cost: §6$" + CryptoMarket.formatCrypto(price * 0.1)))
                .build());
        
        gui.setSlot(21, new GuiElementBuilder(Items.DYE.lime())
                .setName(Component.literal("Buy 1 " + symbol).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Cost: §6$" + CryptoMarket.formatCrypto(price)))
                .build());
        
        gui.setSlot(22, new GuiElementBuilder(Items.DYE.lime())
                .setName(Component.literal("Buy 10 " + symbol).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Cost: §6$" + CryptoMarket.formatCrypto(price * 10)))
                .build());
    }

    // ═══════════════════════════════════════════════════════════════
    // MAIN MENU
    // ═══════════════════════════════════════════════════════════════

    public static void openMainMenu(ServerPlayer player) {
        // Close existing GUI if open
        SimpleGui existingGui = openCryptoGuis.remove(player.getUUID());
        if (existingGui != null && existingGui.isOpen()) {
            existingGui.close();
        }
        
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x4, player, false);
        gui.setTitle(Component.literal("🔮 Crypto Market"));

        // Track this GUI
        openCryptoGuis.put(player.getUUID(), gui);
        guiViewType.put(player.getUUID(), "main");

        // Background
        GuiElementBuilder bg = new GuiElementBuilder(Items.STAINED_GLASS_PANE.black()).setName(Component.literal(""));
        for (int i = 0; i < 36; i++) gui.setSlot(i, bg.build());

        // Header with market info
        CryptoMarket.MarketPhase phase = CryptoMarket.getCurrentPhase();
        double fgIndex = CryptoMarket.getFearGreedIndex();
        ChatFormatting fgColor = fgIndex < 40 ? ChatFormatting.RED : (fgIndex < 60 ? ChatFormatting.YELLOW : ChatFormatting.GREEN);
        
        // Get countdown for next price update
        long currentTick = PoliticalServer.server.getTickCount();
        String countdown = CryptoMarket.getCountdownString(currentTick);
        
        gui.setSlot(4, new GuiElementBuilder(Items.END_CRYSTAL)
                .setName(Component.literal("🔮 Crypto Market").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal(phase.name).withStyle(phase.color))
                .addLoreLine(Component.literal("Fear & Greed: " + String.format("%.0f", fgIndex) + " (" + 
                        CryptoMarket.getFearGreedLabel() + ")").withStyle(fgColor))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Total Market Cap: §6" + 
                        CryptoMarket.formatCrypto(CryptoMarket.getTotalMarketCap())))
                .addLoreLine(Component.literal("§7Your Wallet: §6" + 
                        CryptoMarket.formatCrypto(CryptoMarket.getWalletValue(player.getUUID().toString())) + " USD"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§e⏱ Next update: " + countdown).withStyle(ChatFormatting.YELLOW))
                .glow().build());

        // Browse Cryptos
        gui.setSlot(10, new GuiElementBuilder(Items.KNOWLEDGE_BOOK)
                .setName(Component.literal("📊 Browse Cryptos").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7View all cryptocurrencies"))
                .addLoreLine(Component.literal("§7Check prices and charts"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to browse"))
                .setCallback((idx, type, containerInput, slotGui) -> openCryptoBrowser(player, 0))
                .build());

        // My Wallet
        gui.setSlot(12, new GuiElementBuilder(Items.ENDER_CHEST)
                .setName(Component.literal("💼 My Wallet").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7View your crypto holdings"))
                .addLoreLine(Component.literal("§7Manage your portfolio"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to view"))
                .setCallback((idx, type, containerInput, slotGui) -> openWallet(player))
                .build());

        // Staking
        gui.setSlot(14, new GuiElementBuilder(Items.RECOVERY_COMPASS)
                .setName(Component.literal("🔒 Staking").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Earn passive income"))
                .addLoreLine(Component.literal("§7Stake your crypto"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to stake"))
                .setCallback((idx, type, containerInput, slotGui) -> openStaking(player))
                .build());

        // DeFi
        gui.setSlot(16, new GuiElementBuilder(Items.BREWING_STAND)
                .setName(Component.literal("🧪 DeFi Hub").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Liquidity Pools"))
                .addLoreLine(Component.literal("§7Token Swaps"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to enter"))
                .setCallback((idx, type, containerInput, slotGui) -> openDeFiHub(player))
                .build());

        // Margin Trading
        gui.setSlot(19, new GuiElementBuilder(Items.DIAMOND_SWORD)
                .setName(Component.literal("📊 Margin Trading").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Leveraged positions"))
                .addLoreLine(Component.literal("§7Long & Short"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to trade"))
                .setCallback((idx, type, containerInput, slotGui) -> openMarginTrading(player))
                .build());

        // Top Movers
        gui.setSlot(21, new GuiElementBuilder(Items.SPECTRAL_ARROW)
                .setName(Component.literal("🚀 Top Movers").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Biggest gainers & losers"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to view"))
                .setCallback((idx, type, containerInput, slotGui) -> openTopMovers(player))
                .build());

        // Stablecoins
        gui.setSlot(23, new GuiElementBuilder(Items.EMERALD)
                .setName(Component.literal("💎 Stablecoins").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7EUSD & Gold Standard"))
                .addLoreLine(Component.literal("§7Low volatility"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to view"))
                .setCallback((idx, type, containerInput, slotGui) -> openStablecoins(player))
                .build());

        // Meme Coins
        gui.setSlot(25, new GuiElementBuilder(Items.WOLF_SPAWN_EGG)
                .setName(Component.literal("🐕 Meme Coins").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7High risk, high reward"))
                .addLoreLine(Component.literal("§7Doge, Chicken, Warden"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to gamble"))
                .setCallback((idx, type, containerInput, slotGui) -> openMemeCoins(player))
                .build());

        gui.open();
    }

    // ═══════════════════════════════════════════════════════════════
    // CRYPTO BROWSER
    // ═══════════════════════════════════════════════════════════════

    public static void openCryptoBrowser(ServerPlayer player, int page) {
        // Close existing GUI if open
        SimpleGui existingGui = openCryptoGuis.remove(player.getUUID());
        if (existingGui != null && existingGui.isOpen()) {
            existingGui.close();
        }
        
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("📊 Browse Cryptos"));

        // Track this GUI
        openCryptoGuis.put(player.getUUID(), gui);
        guiViewType.put(player.getUUID(), "browser");
        guiPage.put(player.getUUID(), page);

        GuiElementBuilder bg = new GuiElementBuilder(Items.STAINED_GLASS_PANE.black()).setName(Component.literal(""));
        for (int i = 0; i < 54; i++) gui.setSlot(i, bg.build());

        gui.setSlot(4, new GuiElementBuilder(Items.KNOWLEDGE_BOOK)
                .setName(Component.literal("📊 All Cryptos").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Page " + (page + 1)))
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
            
            ChatFormatting changeColor = change >= 0 ? ChatFormatting.GREEN : ChatFormatting.RED;
            
            final String symbol = crypto.symbol;
            gui.setSlot(slot, new GuiElementBuilder(getCryptoDisplayItem(crypto).getItem())
                    .setName(Component.literal(crypto.icon + " " + crypto.symbol).withStyle(ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(crypto.name).withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§6Price: $" + CryptoMarket.formatCrypto(price)))
                    .addLoreLine(Component.literal("§7Change: " + (change >= 0 ? "+" : "") + 
                            String.format("%.2f", change) + "%").withStyle(changeColor))
                    .addLoreLine(Component.literal("§7Market Cap: $" + 
                            CryptoMarket.formatCrypto(CryptoMarket.getMarketCap(symbol))))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§eClick to trade"))
                    .setCallback((idx, type, containerInput, slotGui) -> openCryptoDetail(player, symbol))
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
                    .setName(Component.literal("← Previous Page").withStyle(ChatFormatting.YELLOW))
                    .setCallback((idx, type, containerInput, slotGui) -> openCryptoBrowser(player, prev))
                    .build());
        }
        
        if ((page + 1) * itemsPerPage < cryptos.length) {
            final int next = page + 1;
            gui.setSlot(53, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("Next Page →").withStyle(ChatFormatting.YELLOW))
                    .setCallback((idx, type, containerInput, slotGui) -> openCryptoBrowser(player, next))
                    .build());
        }

        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> openMainMenu(player))
                .build());

        gui.open();
    }

    // ═══════════════════════════════════════════════════════════════
    // CRYPTO DETAIL & TRADING
    // ═══════════════════════════════════════════════════════════════

    public static void openCryptoDetail(ServerPlayer player, String symbol) {
        CryptoMarket.Crypto crypto = CryptoMarket.getCryptoBySymbol(symbol);
        if (crypto == null) return;

        // Close existing GUI if open
        SimpleGui existingGui = openCryptoGuis.remove(player.getUUID());
        if (existingGui != null && existingGui.isOpen()) {
            existingGui.close();
        }

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x4, player, false);
        gui.setTitle(Component.literal(crypto.symbol + " Trading"));

        // Track this GUI
        openCryptoGuis.put(player.getUUID(), gui);
        guiViewType.put(player.getUUID(), "detail");
        guiSymbol.put(player.getUUID(), symbol);

        GuiElementBuilder bg = new GuiElementBuilder(Items.STAINED_GLASS_PANE.black()).setName(Component.literal(""));
        for (int i = 0; i < 36; i++) gui.setSlot(i, bg.build());

        double price = CryptoMarket.getPrice(symbol);
        double change = CryptoMarket.calculateChange(symbol);
        double change24h = CryptoMarket.calculate24hChange(symbol);
        
        ChatFormatting changeColor = change >= 0 ? ChatFormatting.GREEN : ChatFormatting.RED;

        // Crypto info
        gui.setSlot(4, new GuiElementBuilder(getCryptoDisplayItem(crypto).getItem())
                .setName(Component.literal(crypto.icon + " " + crypto.symbol).withStyle(ChatFormatting.BOLD))
                .addLoreLine(Component.literal(crypto.name).withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§6Price: $" + CryptoMarket.formatCrypto(price)))
                .addLoreLine(Component.literal("§7Change: " + (change >= 0 ? "+" : "") + 
                        String.format("%.2f", change) + "%").withStyle(changeColor))
                .addLoreLine(Component.literal("§724h: " + (change24h >= 0 ? "+" : "") + 
                        String.format("%.2f", change24h) + "%").withStyle(changeColor))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Market Cap: $" + 
                        CryptoMarket.formatCrypto(CryptoMarket.getMarketCap(symbol))))
                .addLoreLine(Component.literal("§7Supply: " + 
                        CryptoMarket.formatCrypto(CryptoMarket.getCirculatingSupply(symbol))))
                .addLoreLine(Component.literal("§7Max Supply: " + (crypto.maxSupply > 0 ? 
                        CryptoMarket.formatCrypto(crypto.maxSupply) : "Unlimited")))
                .glow().build());

        // Player's balance
        double balance = CryptoMarket.getBalance(player.getUUID().toString(), symbol);
        double staked = CryptoMarket.getStakedAmount(player.getUUID().toString(), symbol);
        gui.setSlot(13, new GuiElementBuilder(Items.ENDER_CHEST)
                .setName(Component.literal("Your Holdings").withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Balance: §f" + CryptoMarket.formatCrypto(balance)))
                .addLoreLine(Component.literal("§7Staked: §f" + CryptoMarket.formatCrypto(staked)))
                .addLoreLine(Component.literal("§6Value: $" + CryptoMarket.formatCrypto(price * (balance + staked))))
                .build());

        // Buy buttons
        final String sym = symbol;
        String pu = player.getUUID().toString();
        gui.setSlot(20, new GuiElementBuilder(Items.DYE.lime())
                .setName(Component.literal("Buy 1 " + symbol).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Cost: §6" + CryptoMarket.formatCrypto(CryptoMarket.estimateBuyCoinCost(pu, sym, 1)) + " coins"))
                .setCallback((idx, type, containerInput, slotGui) -> {
                    CryptoMarket.buyCrypto(player, sym, 1);
                    openCryptoDetail(player, sym);
                })
                .build());

        gui.setSlot(21, new GuiElementBuilder(Items.DYE.lime())
                .setName(Component.literal("Buy 10 " + symbol).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Cost: §6" + CryptoMarket.formatCrypto(CryptoMarket.estimateBuyCoinCost(pu, sym, 10)) + " coins"))
                .setCallback((idx, type, containerInput, slotGui) -> {
                    CryptoMarket.buyCrypto(player, sym, 10);
                    openCryptoDetail(player, sym);
                })
                .build());

        gui.setSlot(22, new GuiElementBuilder(Items.DYE.lime())
                .setName(Component.literal("Buy 100 " + symbol).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Cost: §6" + CryptoMarket.formatCrypto(CryptoMarket.estimateBuyCoinCost(pu, sym, 100)) + " coins"))
                .setCallback((idx, type, containerInput, slotGui) -> {
                    CryptoMarket.buyCrypto(player, sym, 100);
                    openCryptoDetail(player, sym);
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
        if (balance > 0) {
            gui.setSlot(24, new GuiElementBuilder(Items.DYE.red())
                    .setName(Component.literal("Sell 1 " + symbol).withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7Value: §6" + CryptoMarket.formatCrypto(price * 1000) + " coins"))
                    .setCallback((idx, type, containerInput, slotGui) -> {
                        CryptoMarket.sellCrypto(player, sym, 1);
                        openCryptoDetail(player, sym);
                    })
                    .build());

            gui.setSlot(25, new GuiElementBuilder(Items.DYE.red())
                    .setName(Component.literal("Sell All").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7Value: §6" + CryptoMarket.formatCrypto(price * balance * 1000) + " coins"))
                    .setCallback((idx, type, containerInput, slotGui) -> {
                        CryptoMarket.sellCrypto(player, sym, balance);
                        openCryptoDetail(player, sym);
                    })
                    .build());

            // Custom sell button with sign input
            gui.setSlot(26, new GuiElementBuilder(Items.WRITABLE_BOOK)
                    .setName(Component.literal("Custom Sell").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7Enter custom amount"))
                    .addLoreLine(Component.literal("§7Supports k/m suffixes"))
                    .addLoreLine(Component.literal("§eClick to enter amount"))
                    .setCallback((idx, type, containerInput, slotGui) -> openCustomSellSign(player, sym, balance))
                    .build());
        }

        // Stake button
        if (CryptoMarket.getStakingApy(symbol) > 0) {
            double apy = CryptoMarket.getStakingApy(symbol) * 100;
            gui.setSlot(30, new GuiElementBuilder(Items.RECOVERY_COMPASS)
                    .setName(Component.literal("🔒 Stake " + symbol).withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§a" + String.format("%.1f", apy) + "% APY"))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§eClick to stake"))
                    .setCallback((idx, type, containerInput, slotGui) -> openStakingDetail(player, sym))
                    .build());
        }

        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> openCryptoBrowser(player, 0))
                .build());

        // Price graph button
        gui.setSlot(32, new GuiElementBuilder(Items.SPYGLASS)
                .setName(Component.literal("📊 Price Graph").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7View price history chart"))
                .addLoreLine(Component.literal("§eClick to view"))
                .setCallback((idx, type, containerInput, slotGui) -> openPriceGraph(player, sym))
                .build());

        gui.open();
    }

    // ═══════════════════════════════════════════════════════════════
    // WALLET VIEW
    // ═══════════════════════════════════════════════════════════════

    public static void openWallet(ServerPlayer player) {
        // Close existing GUI if open
        SimpleGui existingGui = openCryptoGuis.remove(player.getUUID());
        if (existingGui != null && existingGui.isOpen()) {
            existingGui.close();
        }
        
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("💼 My Wallet"));

        // Track this GUI
        openCryptoGuis.put(player.getUUID(), gui);
        guiViewType.put(player.getUUID(), "wallet");

        GuiElementBuilder bg = new GuiElementBuilder(Items.STAINED_GLASS_PANE.black()).setName(Component.literal(""));
        for (int i = 0; i < 54; i++) gui.setSlot(i, bg.build());

        Map<String, Double> wallet = CryptoMarket.getWallet(player.getUUID().toString());
        double totalValue = CryptoMarket.getWalletValue(player.getUUID().toString());

        gui.setSlot(4, new GuiElementBuilder(Items.ENDER_CHEST)
                .setName(Component.literal("💼 Your Wallet").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§6Total Value: $" + CryptoMarket.formatCrypto(totalValue)))
                .addLoreLine(Component.literal("§7Holdings: " + wallet.size() + " different cryptos"))
                .glow().build());

        if (wallet.isEmpty()) {
            gui.setSlot(22, new GuiElementBuilder(Items.BARRIER)
                    .setName(Component.literal("Empty Wallet").withStyle(ChatFormatting.RED))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§7You don't own any crypto yet."))
                    .addLoreLine(Component.literal("§7Browse cryptos to start trading!"))
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
                
                ChatFormatting changeColor = change >= 0 ? ChatFormatting.GREEN : ChatFormatting.RED;
                
                final String symbol = entry.getKey();
                gui.setSlot(slot, new GuiElementBuilder(getCryptoDisplayItem(crypto).getItem())
                        .setName(Component.literal(crypto.icon + " " + crypto.symbol).withStyle(ChatFormatting.BOLD))
                        .addLoreLine(Component.literal("§7Balance: §f" + CryptoMarket.formatCrypto(entry.getValue())))
                        .addLoreLine(Component.literal("§6Value: $" + CryptoMarket.formatCrypto(value)))
                        .addLoreLine(Component.literal("§7Change: " + (change >= 0 ? "+" : "") + 
                                String.format("%.2f", change) + "%").withStyle(changeColor))
                        .addLoreLine(Component.literal(""))
                        .addLoreLine(Component.literal("§eClick to trade"))
                        .setCallback((idx, type, containerInput, slotGui) -> openCryptoDetail(player, symbol))
                        .build());
                
                slot++;
                if (slot == 17) slot = 19;
                else if (slot == 26) slot = 28;
                else if (slot == 35) slot = 37;
            }
        }

        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> openMainMenu(player))
                .build());

        gui.open();
    }

    // ═══════════════════════════════════════════════════════════════
    // STAKING
    // ═══════════════════════════════════════════════════════════════

    public static void openStaking(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal("🔒 Staking"));

        GuiElementBuilder bg = new GuiElementBuilder(Items.STAINED_GLASS_PANE.black()).setName(Component.literal(""));
        for (int i = 0; i < 27; i++) gui.setSlot(i, bg.build());

        gui.setSlot(4, new GuiElementBuilder(Items.RECOVERY_COMPASS)
                .setName(Component.literal("🔒 Staking").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Earn passive income by staking"))
                .glow().build());

        int slot = 10;
        for (CryptoMarket.Crypto crypto : CryptoMarket.Crypto.values()) {
            double apy = CryptoMarket.getStakingApy(crypto.symbol);
            if (apy <= 0) continue;
            
            final String symbol = crypto.symbol;
            double staked = CryptoMarket.getStakedAmount(player.getUUID().toString(), symbol);
            
            gui.setSlot(slot, new GuiElementBuilder(getCryptoDisplayItem(crypto).getItem())
                    .setName(Component.literal(crypto.icon + " " + crypto.symbol).withStyle(ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§a" + String.format("%.1f", apy * 100) + "% APY"))
                    .addLoreLine(Component.literal("§7Staked: " + CryptoMarket.formatCrypto(staked)))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§eClick to manage"))
                    .setCallback((idx, type, containerInput, slotGui) -> openStakingDetail(player, symbol))
                    .build());
            
            slot++;
            if (slot > 16) break;
        }

        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> openMainMenu(player))
                .build());

        gui.open();
    }

    public static void openStakingDetail(ServerPlayer player, String symbol) {
        CryptoMarket.Crypto crypto = CryptoMarket.getCryptoBySymbol(symbol);
        if (crypto == null) return;

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal("🔒 Stake " + symbol));

        GuiElementBuilder bg = new GuiElementBuilder(Items.STAINED_GLASS_PANE.black()).setName(Component.literal(""));
        for (int i = 0; i < 27; i++) gui.setSlot(i, bg.build());

        double apy = CryptoMarket.getStakingApy(symbol);
        double balance = CryptoMarket.getBalance(player.getUUID().toString(), symbol);
        double staked = CryptoMarket.getStakedAmount(player.getUUID().toString(), symbol);

        gui.setSlot(4, new GuiElementBuilder(getCryptoDisplayItem(crypto).getItem())
                .setName(Component.literal(crypto.icon + " " + symbol + " Staking").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§a" + String.format("%.1f", apy * 100) + "% Annual Yield"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Balance: " + CryptoMarket.formatCrypto(balance)))
                .addLoreLine(Component.literal("§7Staked: " + CryptoMarket.formatCrypto(staked)))
                .glow().build());

        final String sym = symbol;
        
        // Stake buttons
        if (balance >= 1) {
            gui.setSlot(10, new GuiElementBuilder(Items.DYE.lime())
                    .setName(Component.literal("Stake 1 " + symbol).withStyle(ChatFormatting.GREEN))
                    .setCallback((idx, type, containerInput, slotGui) -> {
                        CryptoMarket.stakeCrypto(player, sym, 1);
                        openStakingDetail(player, sym);
                    })
                    .build());
        }
        
        if (balance >= 10) {
            gui.setSlot(11, new GuiElementBuilder(Items.DYE.lime())
                    .setName(Component.literal("Stake 10 " + symbol).withStyle(ChatFormatting.GREEN))
                    .setCallback((idx, type, containerInput, slotGui) -> {
                        CryptoMarket.stakeCrypto(player, sym, 10);
                        openStakingDetail(player, sym);
                    })
                    .build());
        }
        
        if (balance >= 100) {
            gui.setSlot(12, new GuiElementBuilder(Items.DYE.lime())
                    .setName(Component.literal("Stake All").withStyle(ChatFormatting.GREEN))
                    .setCallback((idx, type, containerInput, slotGui) -> {
                        CryptoMarket.stakeCrypto(player, sym, balance);
                        openStakingDetail(player, sym);
                    })
                    .build());
        }

        // Unstake buttons
        if (staked >= 1) {
            gui.setSlot(14, new GuiElementBuilder(Items.DYE.red())
                    .setName(Component.literal("Unstake 1 " + symbol).withStyle(ChatFormatting.RED))
                    .setCallback((idx, type, containerInput, slotGui) -> {
                        CryptoMarket.unstakeCrypto(player, sym, 1);
                        openStakingDetail(player, sym);
                    })
                    .build());
            
            gui.setSlot(15, new GuiElementBuilder(Items.DYE.red())
                    .setName(Component.literal("Unstake All").withStyle(ChatFormatting.RED))
                    .setCallback((idx, type, containerInput, slotGui) -> {
                        CryptoMarket.unstakeCrypto(player, sym, staked);
                        openStakingDetail(player, sym);
                    })
                    .build());
        }

        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> openStaking(player))
                .build());

        gui.open();
    }

    // ═══════════════════════════════════════════════════════════════
    // DEFI HUB
    // ═══════════════════════════════════════════════════════════════

    public static void openDeFiHub(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal("🧪 DeFi Hub"));

        GuiElementBuilder bg = new GuiElementBuilder(Items.STAINED_GLASS_PANE.black()).setName(Component.literal(""));
        for (int i = 0; i < 27; i++) gui.setSlot(i, bg.build());

        gui.setSlot(4, new GuiElementBuilder(Items.BREWING_STAND)
                .setName(Component.literal("🧪 DeFi Hub").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Decentralized Finance"))
                .glow().build());

        // Swap
        gui.setSlot(10, new GuiElementBuilder(Items.SPECTRAL_ARROW)
                .setName(Component.literal("🔄 Token Swap").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Swap tokens instantly"))
                .addLoreLine(Component.literal("§7Automated Market Maker"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to swap"))
                .setCallback((idx, type, containerInput, slotGui) -> openSwapMenu(player))
                .build());

        // Liquidity Pools
        gui.setSlot(12, new GuiElementBuilder(Items.WATER_BUCKET)
                .setName(Component.literal("💧 Liquidity Pools").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Provide liquidity"))
                .addLoreLine(Component.literal("§7Earn trading fees"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to view"))
                .setCallback((idx, type, containerInput, slotGui) -> openLiquidityPools(player))
                .build());

        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> openMainMenu(player))
                .build());

        gui.open();
    }

    public static void openSwapMenu(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal("🔄 Token Swap"));

        GuiElementBuilder bg = new GuiElementBuilder(Items.STAINED_GLASS_PANE.black()).setName(Component.literal(""));
        for (int i = 0; i < 27; i++) gui.setSlot(i, bg.build());

        gui.setSlot(4, new GuiElementBuilder(Items.SPECTRAL_ARROW)
                .setName(Component.literal("🔄 Swap Tokens").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
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
            
            double balance = CryptoMarket.getBalance(player.getUUID().toString(), from);
            
            gui.setSlot(slot, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal(fromCrypto.icon + " " + from + " → " + toCrypto.icon + " " + to)
                            .withStyle(ChatFormatting.AQUA))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§7Balance: " + CryptoMarket.formatCrypto(balance) + " " + from))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§eClick to swap"))
                    .setCallback((idx, type, containerInput, slotGui) -> openSwapDetail(player, from, to))
                    .build());
            
            slot++;
            if (slot > 16) break;
        }

        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> openDeFiHub(player))
                .build());

        gui.open();
    }

    public static void openSwapDetail(ServerPlayer player, String from, String to) {
        CryptoMarket.Crypto fromCrypto = CryptoMarket.getCryptoBySymbol(from);
        CryptoMarket.Crypto toCrypto = CryptoMarket.getCryptoBySymbol(to);
        if (fromCrypto == null || toCrypto == null) return;

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal("Swap " + from + " → " + to));

        GuiElementBuilder bg = new GuiElementBuilder(Items.STAINED_GLASS_PANE.black()).setName(Component.literal(""));
        for (int i = 0; i < 27; i++) gui.setSlot(i, bg.build());

        double balance = CryptoMarket.getBalance(player.getUUID().toString(), from);
        double fromPrice = CryptoMarket.getPrice(from);
        double toPrice = CryptoMarket.getPrice(to);
        double rate = fromPrice / toPrice;

        gui.setSlot(4, new GuiElementBuilder(Items.SPECTRAL_ARROW)
                .setName(Component.literal(fromCrypto.icon + " " + from + " → " + toCrypto.icon + " " + to)
                        .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Rate: 1 " + from + " = " + CryptoMarket.formatCrypto(rate) + " " + to))
                .addLoreLine(Component.literal("§7Balance: " + CryptoMarket.formatCrypto(balance) + " " + from))
                .glow().build());

        final String fromSym = from;
        final String toSym = to;

        if (balance >= 1) {
            gui.setSlot(10, new GuiElementBuilder(Items.DYE.lime())
                    .setName(Component.literal("Swap 1 " + from).withStyle(ChatFormatting.GREEN))
                    .addLoreLine(Component.literal("§7→ " + CryptoMarket.formatCrypto(rate) + " " + to))
                    .setCallback((idx, type, containerInput, slotGui) -> {
                        CryptoMarket.swapTokens(player, fromSym, toSym, 1);
                        openSwapDetail(player, fromSym, toSym);
                    })
                    .build());
        }
        
        if (balance >= 10) {
            gui.setSlot(11, new GuiElementBuilder(Items.DYE.lime())
                    .setName(Component.literal("Swap 10 " + from).withStyle(ChatFormatting.GREEN))
                    .addLoreLine(Component.literal("§7→ " + CryptoMarket.formatCrypto(rate * 10) + " " + to))
                    .setCallback((idx, type, containerInput, slotGui) -> {
                        CryptoMarket.swapTokens(player, fromSym, toSym, 10);
                        openSwapDetail(player, fromSym, toSym);
                    })
                    .build());
        }
        
        if (balance > 0) {
            gui.setSlot(12, new GuiElementBuilder(Items.DYE.lime())
                    .setName(Component.literal("Swap All").withStyle(ChatFormatting.GREEN))
                    .addLoreLine(Component.literal("§7→ " + CryptoMarket.formatCrypto(rate * balance) + " " + to))
                    .setCallback((idx, type, containerInput, slotGui) -> {
                        CryptoMarket.swapTokens(player, fromSym, toSym, balance);
                        openSwapDetail(player, fromSym, toSym);
                    })
                    .build());
        }

        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> openSwapMenu(player))
                .build());

        gui.open();
    }

    public static void openLiquidityPools(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal("💧 Liquidity Pools"));

        GuiElementBuilder bg = new GuiElementBuilder(Items.STAINED_GLASS_PANE.black()).setName(Component.literal(""));
        for (int i = 0; i < 27; i++) gui.setSlot(i, bg.build());

        gui.setSlot(4, new GuiElementBuilder(Items.WATER_BUCKET)
                .setName(Component.literal("💧 Liquidity Pools").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD))
                .glow().build());

        int slot = 10;
        for (Map.Entry<String, CryptoMarket.LiquidityPool> entry : CryptoMarket.getLiquidityPools().entrySet()) {
            String poolId = entry.getKey();
            CryptoMarket.LiquidityPool pool = entry.getValue();
            
            gui.setSlot(slot, new GuiElementBuilder(Items.WATER_BUCKET)
                    .setName(Component.literal(poolId).withStyle(ChatFormatting.AQUA))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§7Reserves:"))
                    .addLoreLine(Component.literal("§f" + CryptoMarket.formatCrypto(pool.reserveA) + " " + pool.tokenA))
                    .addLoreLine(Component.literal("§f" + CryptoMarket.formatCrypto(pool.reserveB) + " " + pool.tokenB))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§eClick to provide liquidity"))
                    .build());
            
            slot++;
            if (slot > 16) break;
        }

        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> openDeFiHub(player))
                .build());

        gui.open();
    }

    // ═══════════════════════════════════════════════════════════════
    // MARGIN TRADING
    // ═══════════════════════════════════════════════════════════════

    public static void openMarginTrading(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal("📊 Margin Trading"));

        GuiElementBuilder bg = new GuiElementBuilder(Items.STAINED_GLASS_PANE.black()).setName(Component.literal(""));
        for (int i = 0; i < 27; i++) gui.setSlot(i, bg.build());

        gui.setSlot(4, new GuiElementBuilder(Items.DIAMOND_SWORD)
                .setName(Component.literal("📊 Margin Trading").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§cHigh Risk - High Reward"))
                .addLoreLine(Component.literal("§7Trade with leverage"))
                .glow().build());

        // Show active positions
        Map<String, CryptoMarket.MarginPosition> positions = CryptoMarket.getMarginPositions(player.getUUID().toString());
        if (!positions.isEmpty()) {
            int slot = 10;
            for (Map.Entry<String, CryptoMarket.MarginPosition> entry : positions.entrySet()) {
                CryptoMarket.MarginPosition pos = entry.getValue();
                double currentPrice = CryptoMarket.getPrice(pos.symbol);
                
                double pnl = pos.isLong ? 
                        (currentPrice - pos.entryPrice) / pos.entryPrice * pos.size * pos.leverage :
                        (pos.entryPrice - currentPrice) / pos.entryPrice * pos.size * pos.leverage;
                
                ChatFormatting pnlColor = pnl >= 0 ? ChatFormatting.GREEN : ChatFormatting.RED;
                
                final String key = entry.getKey();
                gui.setSlot(slot, new GuiElementBuilder(pos.isLong ? Items.DYE.lime() : Items.DYE.red())
                        .setName(Component.literal((pos.isLong ? "📈 " : "📉 ") + pos.symbol + " " + 
                                (int)pos.leverage + "x").withStyle(ChatFormatting.BOLD))
                        .addLoreLine(Component.literal(""))
                        .addLoreLine(Component.literal("§7Size: " + CryptoMarket.formatCrypto(pos.size)))
                        .addLoreLine(Component.literal("§7Entry: $" + CryptoMarket.formatCrypto(pos.entryPrice)))
                        .addLoreLine(Component.literal("§7Current: $" + CryptoMarket.formatCrypto(currentPrice)))
                        .addLoreLine(Component.literal("§7Liq: $" + CryptoMarket.formatCrypto(pos.liquidationPrice)))
                        .addLoreLine(Component.literal(""))
                        .addLoreLine(Component.literal("PnL: " + (pnl >= 0 ? "+" : "") + 
                                CryptoMarket.formatCrypto(pnl) + " EUSD").withStyle(pnlColor))
                        .addLoreLine(Component.literal(""))
                        .addLoreLine(Component.literal("§eClick to close"))
                        .setCallback((idx, type, containerInput, slotGui) -> {
                            CryptoMarket.closeMarginPosition(player, key);
                            openMarginTrading(player);
                        })
                        .build());
                
                slot++;
                if (slot > 16) break;
            }
        } else {
            gui.setSlot(13, new GuiElementBuilder(Items.BARRIER)
                    .setName(Component.literal("No Active Positions").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§7Open a position from a crypto page"))
                    .build());
        }

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

        GuiElementBuilder bg = new GuiElementBuilder(Items.STAINED_GLASS_PANE.black()).setName(Component.literal(""));
        for (int i = 0; i < 27; i++) gui.setSlot(i, bg.build());

        gui.setSlot(4, new GuiElementBuilder(Items.SPECTRAL_ARROW)
                .setName(Component.literal("🚀 Top Movers").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
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
            gui.setSlot(10 + i, new GuiElementBuilder(Items.DYE.lime())
                    .setName(Component.literal("📈 " + crypto.symbol).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(crypto.name).withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§a+" + String.format("%.2f", entry.getValue()) + "%"))
                    .addLoreLine(Component.literal("§6$" + CryptoMarket.formatCrypto(CryptoMarket.getPrice(symbol))))
                    .setCallback((idx, type, containerInput, slotGui) -> openCryptoDetail(player, symbol))
                    .build());
        }

        // Top losers
        for (int i = 0; i < 3 && i < changes.size(); i++) {
            int idx = changes.size() - 1 - i;
            Map.Entry<String, Double> entry = changes.get(idx);
            CryptoMarket.Crypto crypto = CryptoMarket.getCryptoBySymbol(entry.getKey());
            if (crypto == null) continue;
            
            final String symbol = entry.getKey();
            gui.setSlot(14 + i, new GuiElementBuilder(Items.DYE.red())
                    .setName(Component.literal("📉 " + crypto.symbol).withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(crypto.name).withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§c" + String.format("%.2f", entry.getValue()) + "%"))
                    .addLoreLine(Component.literal("§6$" + CryptoMarket.formatCrypto(CryptoMarket.getPrice(symbol))))
                    .setCallback((idx2, type, containerInput, slotGui) -> openCryptoDetail(player, symbol))
                    .build());
        }

        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> openMainMenu(player))
                .build());

        gui.open();
    }

    // ═══════════════════════════════════════════════════════════════
    // STABLECOINS & MEMECOINS
    // ═══════════════════════════════════════════════════════════════

    public static void openStablecoins(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal("💎 Stablecoins"));

        GuiElementBuilder bg = new GuiElementBuilder(Items.STAINED_GLASS_PANE.black()).setName(Component.literal(""));
        for (int i = 0; i < 27; i++) gui.setSlot(i, bg.build());

        gui.setSlot(4, new GuiElementBuilder(Items.EMERALD)
                .setName(Component.literal("💎 Stablecoins").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Low volatility, stable value"))
                .glow().build());

        String[] stablecoins = {"EUSD", "GSTD"};
        int slot = 10;
        for (String symbol : stablecoins) {
            CryptoMarket.Crypto crypto = CryptoMarket.getCryptoBySymbol(symbol);
            if (crypto == null) continue;
            
            double price = CryptoMarket.getPrice(symbol);
            double balance = CryptoMarket.getBalance(player.getUUID().toString(), symbol);
            
            final String sym = symbol;
            gui.setSlot(slot, new GuiElementBuilder(Items.EMERALD)
                    .setName(Component.literal(crypto.icon + " " + symbol).withStyle(ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(crypto.name).withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§6Price: $" + CryptoMarket.formatCrypto(price)))
                    .addLoreLine(Component.literal("§7Balance: " + CryptoMarket.formatCrypto(balance)))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§eClick to trade"))
                    .setCallback((idx, type, containerInput, slotGui) -> openCryptoDetail(player, sym))
                    .build());
            
            slot++;
        }

        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> openMainMenu(player))
                .build());

        gui.open();
    }

    public static void openMemeCoins(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal("🐕 Meme Coins"));

        GuiElementBuilder bg = new GuiElementBuilder(Items.STAINED_GLASS_PANE.black()).setName(Component.literal(""));
        for (int i = 0; i < 27; i++) gui.setSlot(i, bg.build());

        gui.setSlot(4, new GuiElementBuilder(Items.WOLF_SPAWN_EGG)
                .setName(Component.literal("🐕 Meme Coins").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§c⚠ Extreme Volatility"))
                .addLoreLine(Component.literal("§7High risk, potential high reward"))
                .glow().build());

        String[] memecoins = {"DOGE", "CHK", "WRD"};
        int slot = 10;
        for (String symbol : memecoins) {
            CryptoMarket.Crypto crypto = CryptoMarket.getCryptoBySymbol(symbol);
            if (crypto == null) continue;
            
            double price = CryptoMarket.getPrice(symbol);
            double change = CryptoMarket.calculateChange(symbol);
            ChatFormatting changeColor = change >= 0 ? ChatFormatting.GREEN : ChatFormatting.RED;
            
            final String sym = symbol;
            gui.setSlot(slot, new GuiElementBuilder(Items.WOLF_SPAWN_EGG)
                    .setName(Component.literal(crypto.icon + " " + symbol).withStyle(ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(crypto.name).withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§6Price: $" + CryptoMarket.formatCrypto(price)))
                    .addLoreLine(Component.literal("§7Change: " + (change >= 0 ? "+" : "") + 
                            String.format("%.2f", change) + "%").withStyle(changeColor))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§eClick to gamble"))
                    .setCallback((idx, type, containerInput, slotGui) -> openCryptoDetail(player, sym))
                    .build());
            
            slot++;
        }

        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> openMainMenu(player))
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

    private static void openCustomBuySign(ServerPlayer player, String symbol) {
        SignGui signGui = new SignGui(player) {
            @Override
            public void onManualClose() {
                String input = this.getLine(0).getString().trim();
                double amount = parseAmountWithSuffix(input);
                if (amount <= 0) {
                    player.sendSystemMessage(Component.literal("✗ Invalid amount!").withStyle(ChatFormatting.RED), false);
                    openCryptoDetail(player, symbol);
                    return;
                }
                CryptoMarket.buyCrypto(player, symbol, amount);
                openCryptoDetail(player, symbol);
            }
        };
        signGui.setLine(0, Component.literal(""));
        signGui.setLine(1, Component.literal("^^^^^^^^^^^^^^").withStyle(ChatFormatting.YELLOW));
        signGui.setLine(2, Component.literal("Enter amount").withStyle(ChatFormatting.AQUA));
        signGui.setLine(3, Component.literal("(k=1000, m=1M)").withStyle(ChatFormatting.GRAY));
        signGui.open();
    }

    private static void openCustomSellSign(ServerPlayer player, String symbol, double maxAmount) {
        SignGui signGui = new SignGui(player) {
            @Override
            public void onManualClose() {
                String input = this.getLine(0).getString().trim();
                double amount = parseAmountWithSuffix(input);
                if (amount <= 0) {
                    player.sendSystemMessage(Component.literal("✗ Invalid amount!").withStyle(ChatFormatting.RED), false);
                    openCryptoDetail(player, symbol);
                    return;
                }
                if (amount > maxAmount) {
                    player.sendSystemMessage(Component.literal("✗ You only have " + CryptoMarket.formatCrypto(maxAmount) + "!").withStyle(ChatFormatting.RED), false);
                    openCryptoDetail(player, symbol);
                    return;
                }
                CryptoMarket.sellCrypto(player, symbol, amount);
                openCryptoDetail(player, symbol);
            }
        };
        signGui.setLine(0, Component.literal(""));
        signGui.setLine(1, Component.literal("^^^^^^^^^^^^^^").withStyle(ChatFormatting.YELLOW));
        signGui.setLine(2, Component.literal("Enter amount").withStyle(ChatFormatting.AQUA));
        signGui.setLine(3, Component.literal("Max: " + CryptoMarket.formatCrypto(maxAmount)).withStyle(ChatFormatting.GRAY));
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

    public static void openPriceGraph(ServerPlayer player, String symbol) {
        CryptoMarket.Crypto crypto = CryptoMarket.getCryptoBySymbol(symbol);
        if (crypto == null) return;

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("📊 " + symbol + " Price Chart"));

        // Dark background for better contrast
        GuiElementBuilder bg = new GuiElementBuilder(Items.STAINED_GLASS_PANE.black()).setName(Component.literal(""));
        for (int i = 0; i < 54; i++) gui.setSlot(i, bg.build());

        List<Double> history = CryptoMarket.getPriceHistory(symbol);
        double currentPrice = CryptoMarket.getPrice(symbol);
        
        // Header with current price info
        double priceChange = history.size() >= 2 ? currentPrice - history.get(history.size() - 2) : 0;
        ChatFormatting priceColor = priceChange >= 0 ? ChatFormatting.GREEN : ChatFormatting.RED;
        String changeSymbol = priceChange >= 0 ? "▲" : "▼";
        
        gui.setSlot(4, new GuiElementBuilder(getCryptoDisplayItem(crypto).getItem())
                .setName(Component.literal(crypto.icon + " " + crypto.name).withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Current: ").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal("$" + CryptoMarket.formatCrypto(currentPrice)).withStyle(ChatFormatting.WHITE)))
                .addLoreLine(Component.literal("Change: ").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(changeSymbol + " $" + CryptoMarket.formatCrypto(Math.abs(priceChange))).withStyle(priceColor)))
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
                                        .append(Component.literal("$" + CryptoMarket.formatCrypto(price)).withStyle(ChatFormatting.WHITE)))
                                .addLoreLine(Component.literal("Close: ").withStyle(ChatFormatting.GRAY)
                                        .append(Component.literal("$" + CryptoMarket.formatCrypto(nextPrice)).withStyle(isUp ? ChatFormatting.GREEN : ChatFormatting.RED)))
                                .addLoreLine(Component.literal("Range: ").withStyle(ChatFormatting.GRAY)
                                        .append(Component.literal("$" + CryptoMarket.formatCrypto(wickHigh - wickLow)).withStyle(ChatFormatting.YELLOW)))
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
                                    .append(Component.literal("$" + CryptoMarket.formatCrypto(price)).withStyle(ChatFormatting.WHITE)))
                            .addLoreLine(Component.literal("Close: ").withStyle(ChatFormatting.GRAY)
                                    .append(Component.literal("$" + CryptoMarket.formatCrypto(nextPrice)).withStyle(isUp ? ChatFormatting.GREEN : ChatFormatting.RED)))
                            .addLoreLine(Component.literal("Change: ").withStyle(ChatFormatting.GRAY)
                                    .append(Component.literal("$" + CryptoMarket.formatCrypto(Math.abs(nextPrice - price))).withStyle(isUp ? ChatFormatting.GREEN : ChatFormatting.RED)))
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
                    .append(Component.literal("$" + CryptoMarket.formatCrypto(maxPrice)).withStyle(ChatFormatting.WHITE)))
            .addLoreLine(Component.literal("Min: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal("$" + CryptoMarket.formatCrypto(minPrice)).withStyle(ChatFormatting.WHITE)))
                    .build());
        }

        // Back button
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back to Market").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> openCryptoDetail(player, symbol))
                .build());

        gui.open();
    }
}
