package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.AnvilInputGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class UndergroundAuctionGui {

    // Track open GUIs for auto-refresh
    private static final Map<UUID, SimpleGui> openGuis = new ConcurrentHashMap<>();

    // Call this from ServerTickEvents.END_SERVER_TICK
    public static void tick() {
        openGuis.entrySet().removeIf(entry -> {
            SimpleGui gui = entry.getValue();
            if (gui.isOpen()) {
                refreshGui(gui);
                return false;
            }
            return true; // Remove closed GUIs
        });
    }

    public static void open(ServerPlayerEntity player) {
        // Close existing GUI if open
        SimpleGui existingGui = openGuis.remove(player.getUuid());
        if (existingGui != null && existingGui.isOpen()) {
            existingGui.close();
        }

        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X4, player, false);
        gui.setTitle(Text.literal("🌙 Underground Auction 🌙"));

        // Track this GUI
        openGuis.put(player.getUuid(), gui);

        // Initial population
        refreshGui(gui);

        gui.open();
    }

    private static void refreshGui(SimpleGui gui) {
        ServerPlayerEntity player = gui.getPlayer();
        if (player == null) return;

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
                    .addLoreLine(Text.literal("Use the buttons to place bids").formatted(Formatting.YELLOW))
                    .build());

            // Clear bid slots when no auction
            for (int slot : new int[]{28, 29, 30, 31, 32, 34}) {
                gui.setSlot(slot, new GuiElementBuilder(Items.PURPLE_STAINED_GLASS_PANE)
                        .setName(Text.literal(""))
                        .build());
            }
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

                // Timer color based on urgency
                Formatting timerColor = timeout <= 5 ? Formatting.RED : (timeout <= 15 ? Formatting.GOLD : Formatting.YELLOW);

                // Display current item
                gui.setSlot(13, new GuiElementBuilder(currentItem.itemStack.getItem())
                        .setName(Text.literal(currentItem.name).formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                        .addLoreLine(Text.literal(""))
                        .addLoreLine(Text.literal("Current Bid: " + currentItem.currentBid + " credits").formatted(Formatting.GOLD, Formatting.BOLD))
                        .addLoreLine(currentItem.highestBidderName != null
                                ? Text.literal("Highest Bidder: " + currentItem.highestBidderName).formatted(Formatting.GREEN)
                                : Text.literal("No bids yet!").formatted(Formatting.GRAY))
                        .addLoreLine(Text.literal(""))
                        .addLoreLine(Text.literal("⏱ " + timeout + "s remaining").formatted(timerColor, Formatting.BOLD))
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
                                    // No need to manually refresh - tick() handles it
                                } else {
                                    player.sendMessage(Text.literal("You don't have enough credits!").formatted(Formatting.RED));
                                }
                            })
                            .build());
                }

                // Custom bid button
                gui.setSlot(34, new GuiElementBuilder(Items.NAME_TAG)
                        .setName(Text.literal("Custom Bid").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                        .addLoreLine(Text.literal(""))
                        .addLoreLine(Text.literal("Click to enter a custom amount").formatted(Formatting.GRAY))
                        .setCallback((index, type, action) -> {
                            openCustomBidGui(player);
                        })
                        .build());
            }
        }

        // Close button (no refresh button needed anymore!)
        gui.setSlot(27, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("Close").formatted(Formatting.RED))
                .setCallback((index, type, action) -> {
                    openGuis.remove(player.getUuid());
                    player.closeHandledScreen();
                })
                .build());

        // Info indicator that GUI auto-updates
        gui.setSlot(35, new GuiElementBuilder(Items.ENDER_EYE)
                .setName(Text.literal("Live Updates").formatted(Formatting.AQUA))
                .addLoreLine(Text.literal("This display updates automatically").formatted(Formatting.GRAY))
                .glow()
                .build());
    }

    private static void openCustomBidGui(ServerPlayerEntity player) {
        // Remove from tracking while in anvil GUI
        openGuis.remove(player.getUuid());

        UndergroundAuctionManager.AuctionItem currentItem = UndergroundAuctionManager.getCurrentItem();
        if (currentItem == null) {
            player.sendMessage(Text.literal("❌ No item is currently up for auction!").formatted(Formatting.RED));
            return;
        }

        AnvilInputGui gui = new AnvilInputGui(player, false);
        gui.setTitle(Text.literal("Enter Bid Amount"));

        gui.setDefaultInputValue(String.valueOf(currentItem.currentBid + 50));

        gui.setSlot(0, new GuiElementBuilder(Items.GOLD_NUGGET)
                .setName(Text.literal("Current Bid: " + currentItem.currentBid).formatted(Formatting.GOLD))
                .addLoreLine(Text.literal("Enter a higher amount").formatted(Formatting.GRAY))
                .build());

        gui.setSlot(2, new GuiElementBuilder(Items.LIME_CONCRETE)
                .setName(Text.literal("Confirm Bid").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to place your bid").formatted(Formatting.GRAY))
                .setCallback((index, type, action) -> {
                    String input = gui.getInput();
                    try {
                        int amount = Integer.parseInt(input.trim());
                        gui.close();
                        UndergroundAuctionManager.placeBid(player, amount);
                        open(player); // Reopen main GUI
                    } catch (NumberFormatException e) {
                        player.sendMessage(Text.literal("❌ Invalid number! Please enter a valid amount.").formatted(Formatting.RED));
                    }
                })
                .build());

        gui.open();
    }

    // Call when player disconnects
    public static void onPlayerDisconnect(ServerPlayerEntity player) {
        openGuis.remove(player.getUuid());
    }
}