package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.gui.SignGui;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Admin GUI for stock market management.
 */
public class StockAdminGui {

    public static void openMainMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
        gui.setTitle(Text.literal("📊 Stock Admin"));

        // Background
        GuiElementBuilder bg = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE).setName(Text.literal(""));
        for (int i = 0; i < 27; i++) gui.setSlot(i, bg.build());

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.GOLD_BLOCK)
                .setName(Text.literal("📊 Stock Market Admin").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§7Manage the stock market"))
                .glow().build());

        // Trigger Event
        gui.setSlot(10, new GuiElementBuilder(Items.FIRE_CHARGE)
                .setName(Text.literal("⚡ Trigger Event").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§7Trigger a random market event"))
                .addLoreLine(Text.literal("§7that affects stock prices"))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§eClick to trigger"))
                .setCallback((idx, type, action) -> {
                    StockMarket.triggerRandomEvent();
                    player.sendMessage(Text.literal("✓ Triggered random market event!").formatted(Formatting.YELLOW), false);
                    openMainMenu(player);
                })
                .build());

        // Reset Market
        gui.setSlot(12, new GuiElementBuilder(Items.RECOVERY_COMPASS)
                .setName(Text.literal("🔄 Reset Market").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§7Reset all stock prices"))
                .addLoreLine(Text.literal("§7to their base values"))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§eClick to reset"))
                .setCallback((idx, type, action) -> {
                    StockMarket.initializeMarket();
                    player.sendMessage(Text.literal("✓ Reset all stock prices to base values!").formatted(Formatting.GREEN), false);
                    openMainMenu(player);
                })
                .build());

        // Toggle Market Open/Closed
        boolean isOpen = StockMarket.isMarketOpen();
        gui.setSlot(14, new GuiElementBuilder(isOpen ? Items.LIME_DYE : Items.RED_DYE)
                .setName(Text.literal(isOpen ? "🔒 Close Market" : "🔓 Open Market").formatted(isOpen ? Formatting.RED : Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§7Market is currently: " + (isOpen ? "§aOpen" : "§cClosed")))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§eClick to toggle"))
                .setCallback((idx, type, action) -> {
                    StockMarket.toggleMarket();
                    player.sendMessage(Text.literal("✓ Market is now " + (StockMarket.isMarketOpen() ? "§aopen" : "§cclosed") + "!").formatted(Formatting.YELLOW), false);
                    openMainMenu(player);
                })
                .build());

        // Set Price
        gui.setSlot(16, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Text.literal("💰 Set Stock Price").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§7Manually set a stock's price"))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§eClick to configure"))
                .setCallback((idx, type, action) -> openStockSelector(player))
                .build());

        // Close button
        gui.setSlot(22, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("Close").formatted(Formatting.RED))
                .setCallback((idx, type, action) -> player.closeHandledScreen())
                .build());

        gui.open();
    }

    private static void openStockSelector(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("📊 Select Stock"));

        GuiElementBuilder bg = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE).setName(Text.literal(""));
        for (int i = 0; i < 54; i++) gui.setSlot(i, bg.build());

        gui.setSlot(4, new GuiElementBuilder(Items.KNOWLEDGE_BOOK)
                .setName(Text.literal("Select a Stock").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal("§7Click to set its price"))
                .glow().build());

        StockMarket.Stock[] stocks = StockMarket.Stock.values();
        int slot = 10;
        for (StockMarket.Stock stock : stocks) {
            if (slot > 43) break;
            
            double price = StockMarket.getPrice(stock.symbol);
            final String symbol = stock.symbol;
            
            gui.setSlot(slot, new GuiElementBuilder(StockMarketGui.getStockDisplayItem(stock).getItem())
                    .setName(Text.literal(stock.icon + " " + stock.symbol).formatted(Formatting.BOLD))
                    .addLoreLine(Text.literal(stock.name).formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("§6Current: " + StockMarket.formatPrice(price)))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("§eClick to set price"))
                    .setCallback((idx, type, action) -> openPriceSetter(player, symbol))
                    .build());
            
            slot++;
            if (slot == 17) slot = 19;
            else if (slot == 26) slot = 28;
            else if (slot == 35) slot = 37;
        }

        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openMainMenu(player))
                .build());

        gui.open();
    }

    private static void openPriceSetter(ServerPlayerEntity player, String symbol) {
        SignGui signGui = new SignGui(player) {
            @Override
            public void onClose() {
                String input = this.getLine(0).getString().trim().replaceAll("[^0-9.]", "");
                if (input.isEmpty()) {
                    player.sendMessage(Text.literal("✗ Invalid price!").formatted(Formatting.RED), false);
                    openStockSelector(player);
                    return;
                }
                try {
                    double newPrice = Double.parseDouble(input);
                    if (newPrice <= 0) {
                        player.sendMessage(Text.literal("✗ Price must be positive!").formatted(Formatting.RED), false);
                    } else {
                        StockMarket.setPrice(symbol, newPrice);
                        player.sendMessage(Text.literal("✓ Set " + symbol + " price to " + StockMarket.formatPrice(newPrice)).formatted(Formatting.GREEN), false);
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage(Text.literal("✗ Invalid number!").formatted(Formatting.RED), false);
                }
                openStockSelector(player);
            }
        };
        signGui.setLine(0, Text.literal("Enter price:"));
        signGui.setLine(1, Text.literal("for " + symbol));
        signGui.open();
    }
}
