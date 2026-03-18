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
 * Admin GUI for cryptocurrency market management.
 */
public class CryptoAdminGui {

    public static void openMainMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
        gui.setTitle(Text.literal("🔮 Crypto Admin"));

        // Background
        GuiElementBuilder bg = new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE).setName(Text.literal(""));
        for (int i = 0; i < 27; i++) gui.setSlot(i, bg.build());

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.END_CRYSTAL)
                .setName(Text.literal("🔮 Crypto Market Admin").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§7Manage the crypto market"))
                .glow().build());

        // Give Crypto
        gui.setSlot(10, new GuiElementBuilder(Items.EMERALD)
                .setName(Text.literal("💰 Give Crypto").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§7Give cryptocurrency to a player"))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§eClick to give"))
                .setCallback((idx, type, action) -> openCryptoSelector(player, true))
                .build());

        // Set Price
        gui.setSlot(12, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Text.literal("💵 Set Price").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§7Manually set a crypto's price"))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§eClick to configure"))
                .setCallback((idx, type, action) -> openCryptoSelector(player, false))
                .build());

        // Market Stats
        gui.setSlot(14, new GuiElementBuilder(Items.KNOWLEDGE_BOOK)
                .setName(Text.literal("📊 Market Stats").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§7Total Market Cap: §6$" + CryptoMarket.formatCrypto(CryptoMarket.getTotalMarketCap())))
                .addLoreLine(Text.literal("§7Fear & Greed: §e" + String.format("%.0f", CryptoMarket.getFearGreedIndex())))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§eClick to refresh"))
                .setCallback((idx, type, action) -> openMainMenu(player))
                .build());

        // Reset Fear & Greed
        gui.setSlot(16, new GuiElementBuilder(Items.RECOVERY_COMPASS)
                .setName(Text.literal("🎲 Reset Fear & Greed").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§7Randomize the Fear & Greed Index"))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§eClick to reset"))
                .setCallback((idx, type, action) -> {
                    CryptoMarket.resetFearGreedIndex();
                    player.sendMessage(Text.literal("✓ Reset Fear & Greed Index!").formatted(Formatting.YELLOW), false);
                    openMainMenu(player);
                })
                .build());

        // Close button
        gui.setSlot(22, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("Close").formatted(Formatting.RED))
                .setCallback((idx, type, action) -> player.closeHandledScreen())
                .build());

        gui.open();
    }

    private static void openCryptoSelector(ServerPlayerEntity player, boolean isGiveMode) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal(isGiveMode ? "💰 Give Crypto" : "💵 Set Price"));

        GuiElementBuilder bg = new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE).setName(Text.literal(""));
        for (int i = 0; i < 54; i++) gui.setSlot(i, bg.build());

        gui.setSlot(4, new GuiElementBuilder(Items.KNOWLEDGE_BOOK)
                .setName(Text.literal(isGiveMode ? "Select Crypto to Give" : "Select Crypto to Set Price").formatted(Formatting.AQUA, Formatting.BOLD))
                .glow().build());

        CryptoMarket.Crypto[] cryptos = CryptoMarket.Crypto.values();
        int slot = 10;
        for (CryptoMarket.Crypto crypto : cryptos) {
            if (slot > 43) break;
            
            double price = CryptoMarket.getPrice(crypto.symbol);
            final String symbol = crypto.symbol;
            
            gui.setSlot(slot, new GuiElementBuilder(CryptoMarketGui.getCryptoDisplayItem(crypto).getItem())
                    .setName(Text.literal(crypto.icon + " " + crypto.symbol).formatted(Formatting.BOLD))
                    .addLoreLine(Text.literal(crypto.name).formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("§6Price: $" + CryptoMarket.formatCrypto(price)))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal(isGiveMode ? "§eClick to give" : "§eClick to set price"))
                    .setCallback((idx, type, action) -> {
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
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openMainMenu(player))
                .build());

        gui.open();
    }

    private static void openGiveSelector(ServerPlayerEntity player, String symbol) {
        SignGui signGui = new SignGui(player) {
            @Override
            public void onClose() {
                String input = this.getLine(0).getString().trim();
                double amount = parseAmount(input);
                if (amount <= 0) {
                    player.sendMessage(Text.literal("✗ Invalid amount!").formatted(Formatting.RED), false);
                    openCryptoSelector(player, true);
                    return;
                }
                CryptoMarket.deposit(player.getUuidAsString(), symbol, amount);
                player.sendMessage(Text.literal("✓ Gave yourself " + CryptoMarket.formatCrypto(amount) + " " + symbol).formatted(Formatting.GREEN), false);
                openCryptoSelector(player, true);
            }
        };
        signGui.setLine(0, Text.literal("Enter amount:"));
        signGui.setLine(1, Text.literal("(k=1000, m=1000000)"));
        signGui.open();
    }

    private static void openPriceSetter(ServerPlayerEntity player, String symbol) {
        SignGui signGui = new SignGui(player) {
            @Override
            public void onClose() {
                String input = this.getLine(0).getString().trim().replaceAll("[^0-9.]", "");
                if (input.isEmpty()) {
                    player.sendMessage(Text.literal("✗ Invalid price!").formatted(Formatting.RED), false);
                    openCryptoSelector(player, false);
                    return;
                }
                try {
                    double newPrice = Double.parseDouble(input);
                    if (newPrice <= 0) {
                        player.sendMessage(Text.literal("✗ Price must be positive!").formatted(Formatting.RED), false);
                    } else {
                        CryptoMarket.setPrice(symbol, newPrice);
                        player.sendMessage(Text.literal("✓ Set " + symbol + " price to $" + CryptoMarket.formatCrypto(newPrice)).formatted(Formatting.GREEN), false);
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage(Text.literal("✗ Invalid number!").formatted(Formatting.RED), false);
                }
                openCryptoSelector(player, false);
            }
        };
        signGui.setLine(0, Text.literal("Enter price:"));
        signGui.setLine(1, Text.literal("for " + symbol));
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
