package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.gui.SignGui;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Locale;

/**
 * GUI for the Government Treasury — accessible only by the Chair.
 * Allows the Chair to view the treasury balance and withdraw coins.
 * Anyone can deposit into the treasury via the deposit button.
 */
public class GovernmentTreasuryGui {

    public static void open(ServerPlayerEntity player) {
        String uuid = player.getUuidAsString();
        String chairUuid = DataManager.getChair();
        boolean isChair = uuid.equals(chairUuid);

        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X4, player, false);
        gui.setTitle(Text.literal("🏛 Government Treasury"));

        // Background
        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.YELLOW_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        int treasury = DataManager.getGovernmentTreasury();

        // Header — treasury balance
        gui.setSlot(4, new GuiElementBuilder(Items.GOLD_BLOCK)
                .setName(Text.literal("🏛 Government Treasury").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Balance: " + fmt(treasury) + " coins").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Revenue sources:").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("  • Tax payments").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("  • Daily coin tax").formatted(Formatting.GRAY))
                .glow()
                .build());

        // Deposit button (available to all)
        gui.setSlot(11, new GuiElementBuilder(Items.EMERALD)
                .setName(Text.literal("Deposit to Treasury").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal("Enter a custom amount to donate").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Click to open sign input").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> openDepositSign(player))
                .build());

        if (isChair) {
            // Withdraw presets
            int[] amounts = {1000, 5000, 10000, 50000};
            int[] slots   = {15, 16, 17, 18};
            for (int i = 0; i < amounts.length; i++) {
                final int amount = amounts[i];
                boolean canWithdraw = treasury >= amount;
                gui.setSlot(slots[i], new GuiElementBuilder(Items.GOLD_INGOT)
                        .setName(Text.literal("Withdraw " + fmt(amount) + " coins").formatted(
                                canWithdraw ? Formatting.GREEN : Formatting.DARK_GRAY, Formatting.BOLD))
                        .addLoreLine(canWithdraw
                                ? Text.literal("Click to withdraw").formatted(Formatting.YELLOW)
                                : Text.literal("✗ Insufficient treasury funds").formatted(Formatting.RED))
                        .setCallback((index, type, action) -> {
                            if (!DataManager.removeGovernmentTreasury(amount)) {
                                player.sendMessage(Text.literal("✗ Not enough in treasury!").formatted(Formatting.RED), false);
                            } else {
                                CoinManager.giveCoins(player, amount);
                                player.sendMessage(Text.literal("✓ Withdrew " + fmt(amount) + " coins from Treasury.").formatted(Formatting.GREEN), false);
                                DataManager.save(PoliticalServer.server);
                            }
                            GovernmentTreasuryGui.open(player);
                        })
                        .build());
            }

            // Custom withdraw button
            gui.setSlot(20, new GuiElementBuilder(Items.WRITABLE_BOOK)
                    .setName(Text.literal("Custom Withdraw").formatted(Formatting.YELLOW, Formatting.BOLD))
                    .addLoreLine(Text.literal("Enter a custom amount to withdraw").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("Click to open sign input").formatted(Formatting.YELLOW))
                    .setCallback((index, type, action) -> openWithdrawSign(player))
                    .build());
        } else {
            // Non-chair: show locked message
            gui.setSlot(20, new GuiElementBuilder(Items.BARRIER)
                    .setName(Text.literal("Withdrawal Locked").formatted(Formatting.RED, Formatting.BOLD))
                    .addLoreLine(Text.literal("Only the Chair may withdraw funds").formatted(Formatting.GRAY))
                    .build());
        }

        // Close button
        gui.setSlot(31, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("Close").formatted(Formatting.RED))
                .setCallback((index, type, action) -> player.closeHandledScreen())
                .build());

        gui.open();
    }

    private static void openDepositSign(ServerPlayerEntity player) {
        SignGui signGui = new SignGui(player) {
            @Override
            public void onClose() {
                String input = this.getLine(0).getString().trim().replaceAll("[^0-9]", "");
                if (input.isEmpty()) { GovernmentTreasuryGui.open(player); return; }
                try {
                    int amount = Integer.parseInt(input);
                    if (amount <= 0) {
                        player.sendMessage(Text.literal("✗ Amount must be positive!").formatted(Formatting.RED), false);
                        GovernmentTreasuryGui.open(player); return;
                    }
                    int purse = CoinManager.getCoins(player);
                    if (purse < amount) {
                        player.sendMessage(Text.literal("✗ Not enough coins in purse!").formatted(Formatting.RED), false);
                        GovernmentTreasuryGui.open(player); return;
                    }
                    CoinManager.removeCoins(player, amount);
                    DataManager.addGovernmentTreasury(amount);
                    DataManager.save(PoliticalServer.server);
                    player.sendMessage(Text.literal("✓ Deposited " + fmt(amount) + " coins into the Government Treasury.").formatted(Formatting.GREEN), false);
                } catch (NumberFormatException e) {
                    player.sendMessage(Text.literal("✗ Invalid number!").formatted(Formatting.RED), false);
                }
                GovernmentTreasuryGui.open(player);
            }
        };
        signGui.setLine(0, Text.literal("Enter amount:"));
        signGui.setLine(1, Text.literal("to deposit"));
        signGui.open();
    }

    private static void openWithdrawSign(ServerPlayerEntity player) {
        SignGui signGui = new SignGui(player) {
            @Override
            public void onClose() {
                String chairUuid = DataManager.getChair();
                if (!player.getUuidAsString().equals(chairUuid)) {
                    player.sendMessage(Text.literal("✗ Only the Chair can withdraw!").formatted(Formatting.RED), false);
                    GovernmentTreasuryGui.open(player); return;
                }
                String input = this.getLine(0).getString().trim().replaceAll("[^0-9]", "");
                if (input.isEmpty()) { GovernmentTreasuryGui.open(player); return; }
                try {
                    int amount = Integer.parseInt(input);
                    if (amount <= 0) {
                        player.sendMessage(Text.literal("✗ Amount must be positive!").formatted(Formatting.RED), false);
                        GovernmentTreasuryGui.open(player); return;
                    }
                    if (!DataManager.removeGovernmentTreasury(amount)) {
                        player.sendMessage(Text.literal("✗ Not enough in treasury!").formatted(Formatting.RED), false);
                        GovernmentTreasuryGui.open(player); return;
                    }
                    CoinManager.giveCoins(player, amount);
                    DataManager.save(PoliticalServer.server);
                    player.sendMessage(Text.literal("✓ Withdrew " + fmt(amount) + " coins from Treasury.").formatted(Formatting.GREEN), false);
                } catch (NumberFormatException e) {
                    player.sendMessage(Text.literal("✗ Invalid number!").formatted(Formatting.RED), false);
                }
                GovernmentTreasuryGui.open(player);
            }
        };
        signGui.setLine(0, Text.literal("Enter amount:"));
        signGui.setLine(1, Text.literal("to withdraw"));
        signGui.open();
    }

    private static String fmt(int amount) {
        return String.format(Locale.US, "%,d", amount);
    }
}
