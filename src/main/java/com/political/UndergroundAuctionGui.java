package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class UndergroundAuctionGui {

    public static void open(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X4, player, false);
        gui.setTitle(Text.literal("🌙 Underground Auction 🌙"));

        // Fill with dark glass
        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.PURPLE_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        // Player credits display
        int playerCredits = CreditItem.countCredits(player);
        gui.setSlot(4, new GuiElementBuilder(Items.GOLD_NUGGET)
                .setName(Text.literal("Your Credits: " + playerCredits).formatted(Formatting.GOLD, Formatting.BOLD))
                .glow()
                .build());

        if (!UndergroundAuctionManager.isAuctionActive()) {
            // No active auction - show countdown
            long timeUntil = UndergroundAuctionManager.getTimeUntilNextAuction();
            String timeStr = PoliticalServer.formatTime(timeUntil);

            gui.setSlot(13, new GuiElementBuilder(Items.CLOCK)
                    .setName(Text.literal("No Auction Active").formatted(Formatting.RED, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Next auction in:").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal(timeStr).formatted(Formatting.YELLOW))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Check back later!").formatted(Formatting.GRAY))
                    .build());

            gui.setSlot(22, new GuiElementBuilder(Items.BOOK)
                    .setName(Text.literal("How It Works").formatted(Formatting.LIGHT_PURPLE))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Auctions happen every 6 hours").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("Rare items are up for bidding").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("Highest bidder wins!").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Use /bid <amount> to bid").formatted(Formatting.YELLOW))
                    .build());
        } else {
            // Active auction - show current item
            UndergroundAuctionManager.AuctionItem currentItem = UndergroundAuctionManager.getCurrentItem();

            if (currentItem != null) {
                int timeout = UndergroundAuctionManager.getSecondsUntilTimeout();
                int currentIndex = UndergroundAuctionManager.getCurrentItemIndex();
                int totalItems = UndergroundAuctionManager.getTotalItems();

                // Progress indicator
                gui.setSlot(0, new GuiElementBuilder(Items.PAPER)
                        .setName(Text.literal("Item " + (currentIndex + 1) + " of " + totalItems).formatted(Formatting.GRAY))
                        .build());

                // Display current item
                gui.setSlot(13, new GuiElementBuilder(currentItem.itemStack.getItem())
                        .setName(Text.literal(currentItem.name).formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                        .addLoreLine(Text.literal(""))
                        .addLoreLine(Text.literal("Current Bid: " + currentItem.currentBid + " credits").formatted(Formatting.GOLD, Formatting.BOLD))
                        .addLoreLine(currentItem.highestBidderName != null
                                ? Text.literal("Highest Bidder: " + currentItem.highestBidderName).formatted(Formatting.GREEN)
                                : Text.literal("No bids yet!").formatted(Formatting.GRAY))
                        .addLoreLine(Text.literal(""))
                        .addLoreLine(Text.literal("Time remaining: " + timeout + "s").formatted(timeout <= 5 ? Formatting.RED : Formatting.YELLOW))
                        .glow()
                        .build());

                // Bid buttons
                int[] bidIncrements = {50, 100, 250, 500, 1000};
                int[] slots = {28, 29, 30, 31, 32};

                for (int i = 0; i < bidIncrements.length; i++) {
                    int increment = bidIncrements[i];
                    int bidAmount = currentItem.currentBid + increment;
                    boolean canAfford = playerCredits >= bidAmount;

                    final int finalBid = bidAmount;
                    gui.setSlot(slots[i], new GuiElementBuilder(canAfford ? Items.GOLD_INGOT : Items.IRON_INGOT)
                            .setName(Text.literal("Bid " + bidAmount).formatted(canAfford ? Formatting.GREEN : Formatting.RED, Formatting.BOLD))
                            .addLoreLine(Text.literal("(+" + increment + " from current)").formatted(Formatting.GRAY))
                            .addLoreLine(Text.literal(""))
                            .addLoreLine(canAfford
                                    ? Text.literal("Click to bid!").formatted(Formatting.YELLOW)
                                    : Text.literal("Not enough credits!").formatted(Formatting.RED))
                            .setCallback((index, type, action) -> {
                                if (canAfford) {
                                    UndergroundAuctionManager.placeBid(player, finalBid);
                                    open(player); // Refresh
                                } else {
                                    player.sendMessage(Text.literal("You don't have enough credits!").formatted(Formatting.RED));
                                }
                            })
                            .build());
                }

                // Custom bid instruction
                gui.setSlot(34, new GuiElementBuilder(Items.NAME_TAG)
                        .setName(Text.literal("Custom Bid").formatted(Formatting.LIGHT_PURPLE))
                        .addLoreLine(Text.literal(""))
                        .addLoreLine(Text.literal("Use /bid <amount>").formatted(Formatting.GRAY))
                        .addLoreLine(Text.literal("for a custom amount").formatted(Formatting.GRAY))
                        .build());
            }
        }

        // Refresh button
        gui.setSlot(35, new GuiElementBuilder(Items.ENDER_EYE)
                .setName(Text.literal("Refresh").formatted(Formatting.AQUA))
                .addLoreLine(Text.literal("Click to update").formatted(Formatting.GRAY))
                .setCallback((index, type, action) -> open(player))
                .build());

        // Close button
        gui.setSlot(27, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("Close").formatted(Formatting.RED))
                .setCallback((index, type, action) -> player.closeHandledScreen())
                .build());

        gui.open();
    }
}