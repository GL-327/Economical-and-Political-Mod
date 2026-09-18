package com.political;


import com.political.economy.StockMarket;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.gui.SignGui;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

/**
 * Admin GUI for stock market management.
 */
public class StockAdminGui {

    public static void openMainMenu(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal("📊 Stock Admin"));

        // Background
        GuiElementBuilder bg = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE).setName(Component.literal(""));
        for (int i = 0; i < 27; i++) gui.setSlot(i, bg.build());

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.GOLD_BLOCK)
                .setName(Component.literal("📊 Stock Market Admin").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Manage the stock market"))
                .glow().build());

        // Trigger Event
        gui.setSlot(10, new GuiElementBuilder(Items.FIRE_CHARGE)
                .setName(Component.literal("⚡ Trigger Event").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Trigger a random market event"))
                .addLoreLine(Component.literal("§7that affects stock prices"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to trigger"))
                .setCallback((idx, type, containerInput, slotGui) -> {
                    StockMarket.triggerRandomEvent();
                    player.sendSystemMessage(Component.literal("✓ Triggered random market event!").withStyle(ChatFormatting.YELLOW), false);
                    openMainMenu(player);
                })
                .build());

        // Reset Market
        gui.setSlot(12, new GuiElementBuilder(Items.RECOVERY_COMPASS)
                .setName(Component.literal("🔄 Reset Market").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Reset all stock prices"))
                .addLoreLine(Component.literal("§7to their base values"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to reset"))
                .setCallback((idx, type, containerInput, slotGui) -> {
                    StockMarket.initializeMarket();
                    player.sendSystemMessage(Component.literal("✓ Reset all stock prices to base values!").withStyle(ChatFormatting.GREEN), false);
                    openMainMenu(player);
                })
                .build());

        // Toggle Market Open/Closed
        boolean isOpen = StockMarket.isMarketOpen();
        gui.setSlot(14, new GuiElementBuilder(isOpen ? Items.LIME_DYE : Items.RED_DYE)
                .setName(Component.literal(isOpen ? "🔒 Close Market" : "🔓 Open Market").withStyle(isOpen ? ChatFormatting.RED : ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Market is currently: " + (isOpen ? "§aOpen" : "§cClosed")))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to toggle"))
                .setCallback((idx, type, containerInput, slotGui) -> {
                    StockMarket.toggleMarket();
                    player.sendSystemMessage(Component.literal("✓ Market is now " + (StockMarket.isMarketOpen() ? "§aopen" : "§cclosed") + "!").withStyle(ChatFormatting.YELLOW), false);
                    openMainMenu(player);
                })
                .build());

        // Set Price
        gui.setSlot(16, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Component.literal("💰 Set Stock Price").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Manually set a stock's price"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to configure"))
                .setCallback((idx, type, containerInput, slotGui) -> openStockSelector(player))
                .build());

        // Close button
        gui.setSlot(22, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("Close").withStyle(ChatFormatting.RED))
                .setCallback((idx, type, containerInput, slotGui) -> player.closeContainer())
                .build());

        gui.open();
    }

    private static void openStockSelector(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("📊 Select Stock"));

        GuiElementBuilder bg = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE).setName(Component.literal(""));
        for (int i = 0; i < 54; i++) gui.setSlot(i, bg.build());

        gui.setSlot(4, new GuiElementBuilder(Items.KNOWLEDGE_BOOK)
                .setName(Component.literal("Select a Stock").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Click to set its price"))
                .glow().build());

        StockMarket.Stock[] stocks = StockMarket.Stock.values();
        int slot = 10;
        for (StockMarket.Stock stock : stocks) {
            if (slot > 43) break;
            
            double price = StockMarket.getPrice(stock.symbol);
            final String symbol = stock.symbol;
            
            gui.setSlot(slot, new GuiElementBuilder(StockMarketGui.getStockDisplayItem(stock).getItem())
                    .setName(Component.literal(stock.icon + " " + stock.symbol).withStyle(ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(stock.name).withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§6Current: " + StockMarket.formatPrice(price)))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§eClick to set price"))
                    .setCallback((idx, type, containerInput, slotGui) -> openPriceSetter(player, symbol))
                    .build());
            
            slot++;
            if (slot == 17) slot = 19;
            else if (slot == 26) slot = 28;
            else if (slot == 35) slot = 37;
        }

        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> openMainMenu(player))
                .build());

        gui.open();
    }

    private static void openPriceSetter(ServerPlayer player, String symbol) {
        SignGui signGui = new SignGui(player) {
            @Override
            public void onManualClose() {
                String input = this.getLine(0).getString().trim().replaceAll("[^0-9.]", "");
                if (input.isEmpty()) {
                    player.sendSystemMessage(Component.literal("✗ Invalid price!").withStyle(ChatFormatting.RED), false);
                    openStockSelector(player);
                    return;
                }
                try {
                    double newPrice = Double.parseDouble(input);
                    if (newPrice <= 0) {
                        player.sendSystemMessage(Component.literal("✗ Price must be positive!").withStyle(ChatFormatting.RED), false);
                    } else {
                        StockMarket.setPrice(symbol, newPrice);
                        player.sendSystemMessage(Component.literal("✓ Set " + symbol + " price to " + StockMarket.formatPrice(newPrice)).withStyle(ChatFormatting.GREEN), false);
                    }
                } catch (NumberFormatException e) {
                    player.sendSystemMessage(Component.literal("✗ Invalid number!").withStyle(ChatFormatting.RED), false);
                }
                openStockSelector(player);
            }
        };
        signGui.setLine(0, Component.literal(""));
        signGui.setLine(1, Component.literal("^^^^^^^^^^^^^^").withStyle(ChatFormatting.YELLOW));
        signGui.setLine(2, Component.literal("Enter price").withStyle(ChatFormatting.AQUA));
        signGui.setLine(3, Component.literal("for " + symbol).withStyle(ChatFormatting.GRAY));
        signGui.open();
    }
}
