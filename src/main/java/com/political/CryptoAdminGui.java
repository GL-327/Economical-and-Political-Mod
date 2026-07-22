package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.gui.SignGui;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

/**
 * Admin GUI for cryptocurrency market management.
 */
public class CryptoAdminGui {

    public static void openMainMenu(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal("🔮 Crypto Admin"));

        // Background
        GuiElementBuilder bg = new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE).setName(Component.literal(""));
        for (int i = 0; i < 27; i++) gui.setSlot(i, bg.build());

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.END_CRYSTAL)
                .setName(Component.literal("🔮 Crypto Market Admin").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Manage the crypto market"))
                .glow().build());

        // Give Crypto
        gui.setSlot(10, new GuiElementBuilder(Items.EMERALD)
                .setName(Component.literal("💰 Give Crypto").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Give cryptocurrency to a player"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to give"))
                .setCallback((idx, type, containerInput, slotGui) -> openCryptoSelector(player, true))
                .build());

        // Set Price
        gui.setSlot(12, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Component.literal("💵 Set Price").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Manually set a crypto's price"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to configure"))
                .setCallback((idx, type, containerInput, slotGui) -> openCryptoSelector(player, false))
                .build());

        // Market Stats
        gui.setSlot(14, new GuiElementBuilder(Items.KNOWLEDGE_BOOK)
                .setName(Component.literal("📊 Market Stats").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Total Market Cap: §6$" + CryptoMarket.formatCrypto(CryptoMarket.getTotalMarketCap())))
                .addLoreLine(Component.literal("§7Fear & Greed: §e" + String.format("%.0f", CryptoMarket.getFearGreedIndex())))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to refresh"))
                .setCallback((idx, type, containerInput, slotGui) -> openMainMenu(player))
                .build());

        // Reset Fear & Greed
        gui.setSlot(16, new GuiElementBuilder(Items.RECOVERY_COMPASS)
                .setName(Component.literal("🎲 Reset Fear & Greed").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Randomize the Fear & Greed Index"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to reset"))
                .setCallback((idx, type, containerInput, slotGui) -> {
                    CryptoMarket.resetFearGreedIndex();
                    player.sendSystemMessage(Component.literal("✓ Reset Fear & Greed Index!").withStyle(ChatFormatting.YELLOW), false);
                    openMainMenu(player);
                })
                .build());

        // Close button
        gui.setSlot(22, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("Close").withStyle(ChatFormatting.RED))
                .setCallback((idx, type, containerInput, slotGui) -> player.closeContainer())
                .build());

        gui.open();
    }

    private static void openCryptoSelector(ServerPlayer player, boolean isGiveMode) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal(isGiveMode ? "💰 Give Crypto" : "💵 Set Price"));

        GuiElementBuilder bg = new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE).setName(Component.literal(""));
        for (int i = 0; i < 54; i++) gui.setSlot(i, bg.build());

        gui.setSlot(4, new GuiElementBuilder(Items.KNOWLEDGE_BOOK)
                .setName(Component.literal(isGiveMode ? "Select Crypto to Give" : "Select Crypto to Set Price").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .glow().build());

        CryptoMarket.Crypto[] cryptos = CryptoMarket.Crypto.values();
        int slot = 10;
        for (CryptoMarket.Crypto crypto : cryptos) {
            if (slot > 43) break;
            
            double price = CryptoMarket.getPrice(crypto.symbol);
            final String symbol = crypto.symbol;
            
            gui.setSlot(slot, new GuiElementBuilder(CryptoMarketGui.getCryptoDisplayItem(crypto).getItem())
                    .setName(Component.literal(crypto.icon + " " + crypto.symbol).withStyle(ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(crypto.name).withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§6Price: $" + CryptoMarket.formatCrypto(price)))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal(isGiveMode ? "§eClick to give" : "§eClick to set price"))
                    .setCallback((idx, type, containerInput, slotGui) -> {
                        if (isGiveMode) {
                            openGiveSelector(player, symbol);
                        } else {
                            openPriceSetter(player, symbol);
                        }
                    })
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

    private static void openGiveSelector(ServerPlayer player, String symbol) {
        SignGui signGui = new SignGui(player) {
            @Override
            public void onManualClose() {
                String input = this.getLine(0).getString().trim();
                double amount = parseAmount(input);
                if (amount <= 0) {
                    player.sendSystemMessage(Component.literal("✗ Invalid amount!").withStyle(ChatFormatting.RED), false);
                    openCryptoSelector(player, true);
                    return;
                }
                CryptoMarket.deposit(player.getUUID().toString(), symbol, amount);
                player.sendSystemMessage(Component.literal("✓ Gave yourself " + CryptoMarket.formatCrypto(amount) + " " + symbol).withStyle(ChatFormatting.GREEN), false);
                openCryptoSelector(player, true);
            }
        };
        signGui.setLine(0, Component.literal(""));
        signGui.setLine(1, Component.literal("^^^^^^^^^^^^^^").withStyle(ChatFormatting.YELLOW));
        signGui.setLine(2, Component.literal("Enter amount").withStyle(ChatFormatting.AQUA));
        signGui.setLine(3, Component.literal("(k=1000, m=1M)").withStyle(ChatFormatting.GRAY));
        signGui.open();
    }

    private static void openPriceSetter(ServerPlayer player, String symbol) {
        SignGui signGui = new SignGui(player) {
            @Override
            public void onManualClose() {
                String input = this.getLine(0).getString().trim().replaceAll("[^0-9.]", "");
                if (input.isEmpty()) {
                    player.sendSystemMessage(Component.literal("✗ Invalid price!").withStyle(ChatFormatting.RED), false);
                    openCryptoSelector(player, false);
                    return;
                }
                try {
                    double newPrice = Double.parseDouble(input);
                    if (newPrice <= 0) {
                        player.sendSystemMessage(Component.literal("✗ Price must be positive!").withStyle(ChatFormatting.RED), false);
                    } else {
                        CryptoMarket.setPrice(symbol, newPrice);
                        player.sendSystemMessage(Component.literal("✓ Set " + symbol + " price to $" + CryptoMarket.formatCrypto(newPrice)).withStyle(ChatFormatting.GREEN), false);
                    }
                } catch (NumberFormatException e) {
                    player.sendSystemMessage(Component.literal("✗ Invalid number!").withStyle(ChatFormatting.RED), false);
                }
                openCryptoSelector(player, false);
            }
        };
        signGui.setLine(0, Component.literal(""));
        signGui.setLine(1, Component.literal("^^^^^^^^^^^^^^").withStyle(ChatFormatting.YELLOW));
        signGui.setLine(2, Component.literal("Enter price").withStyle(ChatFormatting.AQUA));
        signGui.setLine(3, Component.literal("for " + symbol).withStyle(ChatFormatting.GRAY));
        signGui.open();
    }

    private static double parseAmount(String input) {
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
}
