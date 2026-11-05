package com.political.economy;
import com.political.PoliticalServer;


import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.AnvilInputGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

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

    /** Where Back leads from this screen: the menu that opened it, remembered per player. */
    private static final com.political.core.gui.ReturnTo FROM = new com.political.core.gui.ReturnTo();

    /** Opens the screen from a parent menu (Back returns there), or with {@code null} from a command or a villager (no Back). */
    public static void openFrom(ServerPlayer player, Runnable back) {
        FROM.set(player.getUUID(), back);
        open(player);
    }

    public static void open(ServerPlayer player) {
        // Close existing GUI if open
        SimpleGui existingGui = openGuis.remove(player.getUUID());
        if (existingGui != null && existingGui.isOpen()) {
            existingGui.close();
        }

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x4, player, false);
        gui.setTitle(Component.literal("🌙 Underground Auction 🌙"));

        // Track this GUI
        openGuis.put(player.getUUID(), gui);

        // Initial population
        refreshGui(gui);

        gui.open();
    }

    private static void refreshGui(SimpleGui gui) {
        ServerPlayer player = gui.getPlayer();
        if (player == null) return;

        // Fill with dark glass
        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.purple())
                    .setName(Component.literal(""))
                    .build());
        }

        // Player coins display
        long playerCoins = CoinManager.getCoinsLong(player);
        gui.setSlot(4, new GuiElementBuilder(Items.GOLD_NUGGET)
                .setName(Component.literal("Your Coins: " + playerCoins).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .glow()
                .build());

        if (!UndergroundAuctionManager.isAuctionActive()) {
            // No active auction - show countdown
            long timeUntil = UndergroundAuctionManager.getTimeUntilNextAuction();
            String timeStr = PoliticalServer.formatTime(timeUntil);

            gui.setSlot(13, new GuiElementBuilder(Items.CLOCK)
                    .setName(Component.literal("No Auction Active").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Next auction in:").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(timeStr).withStyle(ChatFormatting.YELLOW))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Check back later!").withStyle(ChatFormatting.GRAY))
                    .build());

            gui.setSlot(22, new GuiElementBuilder(Items.BOOK)
                    .setName(Component.literal("How It Works").withStyle(ChatFormatting.LIGHT_PURPLE))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Auctions happen every 6 hours").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("Rare items are up for bidding").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("Highest bidder wins!").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Use the buttons to place bids").withStyle(ChatFormatting.YELLOW))
                    .build());

            // Clear bid slots when no auction
            for (int slot : new int[]{28, 29, 30, 31, 32, 34}) {
                gui.setSlot(slot, new GuiElementBuilder(Items.STAINED_GLASS_PANE.purple())
                        .setName(Component.literal(""))
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
                        .setName(Component.literal("Item " + (currentIndex + 1) + " of " + totalItems).withStyle(ChatFormatting.GRAY))
                        .build());

                // Timer color based on urgency
                ChatFormatting timerColor = timeout <= 5 ? ChatFormatting.RED : (timeout <= 15 ? ChatFormatting.GOLD : ChatFormatting.YELLOW);

                // Display current item
                gui.setSlot(13, new GuiElementBuilder(currentItem.itemStack.getItem())
                        .setName(Component.literal(currentItem.name).withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                        .addLoreLine(Component.literal(""))
                        .addLoreLine(Component.literal("Current Bid: " + currentItem.currentBid + " coins").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                        .addLoreLine(currentItem.highestBidderName != null
                                ? Component.literal("Highest Bidder: " + currentItem.highestBidderName).withStyle(ChatFormatting.GREEN)
                                : Component.literal("No bids yet!").withStyle(ChatFormatting.GRAY))
                        .addLoreLine(Component.literal(""))
                        .addLoreLine(Component.literal("⏱ " + timeout + "s remaining").withStyle(timerColor, ChatFormatting.BOLD))
                        .glow()
                        .build());

                // Bid buttons
                int[] bidIncrements = {50, 100, 250, 500, 1000};
                int[] slots = {28, 29, 30, 31, 32};

                for (int i = 0; i < bidIncrements.length; i++) {
                    int increment = bidIncrements[i];
                    long bidAmount = currentItem.currentBid + increment;
                    boolean canAfford = playerCoins >= bidAmount;

                    final long finalBid = bidAmount;
                    gui.setSlot(slots[i], new GuiElementBuilder(canAfford ? Items.GOLD_INGOT : Items.IRON_INGOT)
                            .setName(Component.literal("Bid " + bidAmount).withStyle(canAfford ? ChatFormatting.GREEN : ChatFormatting.RED, ChatFormatting.BOLD))
                            .addLoreLine(Component.literal("(+" + increment + " from current)").withStyle(ChatFormatting.GRAY))
                            .addLoreLine(Component.literal(""))
                            .addLoreLine(canAfford
                                    ? Component.literal("Click to bid!").withStyle(ChatFormatting.YELLOW)
                                    : Component.literal("Not enough coins!").withStyle(ChatFormatting.RED))
                            .setCallback((index, type, containerInput, slotGui) -> {
                                if (canAfford) {
                                    UndergroundAuctionManager.placeBid(player, finalBid);
                                    // No need to manually refresh - tick() handles it
                                } else {
                                    player.sendSystemMessage(Component.literal("You don't have enough coins!").withStyle(ChatFormatting.RED));
                                }
                            })
                            .build());
                }

                // Custom bid button
                gui.setSlot(34, new GuiElementBuilder(Items.NAME_TAG)
                        .setName(Component.literal("Custom Bid").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                        .addLoreLine(Component.literal(""))
                        .addLoreLine(Component.literal("Click to enter a custom amount").withStyle(ChatFormatting.GRAY))
                        .setCallback((index, type, containerInput, slotGui) -> {
                            openCustomBidGui(player);
                        })
                        .build());
            }
        }

        // Close button (no refresh button needed anymore!)
        gui.setSlot(27, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("Close").withStyle(ChatFormatting.RED))
                .setCallback((index, type, containerInput, slotGui) -> {
                    openGuis.remove(player.getUUID());
                    player.closeContainer();
                })
                .build());

        // Info indicator that GUI auto-updates
        gui.setSlot(35, new GuiElementBuilder(Items.ENDER_EYE)
                .setName(Component.literal("Live Updates").withStyle(ChatFormatting.AQUA))
                .addLoreLine(Component.literal("This display updates automatically").withStyle(ChatFormatting.GRAY))
                .glow()
                .build());
        com.political.core.gui.BackSlot.add(gui, FROM.get(player.getUUID()));
    }

    private static void openCustomBidGui(ServerPlayer player) {
        // Remove from tracking while in anvil GUI
        openGuis.remove(player.getUUID());

        UndergroundAuctionManager.AuctionItem currentItem = UndergroundAuctionManager.getCurrentItem();
        if (currentItem == null) {
            player.sendSystemMessage(Component.literal("❌ No item is currently up for auction!").withStyle(ChatFormatting.RED));
            return;
        }

        AnvilInputGui gui = new AnvilInputGui(player, false);
        gui.setTitle(Component.literal("Enter Bid Amount"));

        gui.setDefaultInputValue(String.valueOf(currentItem.currentBid + 50));

        gui.setSlot(0, new GuiElementBuilder(Items.GOLD_NUGGET)
                .setName(Component.literal("Current Bid: " + currentItem.currentBid).withStyle(ChatFormatting.GOLD))
                .addLoreLine(Component.literal("Enter a higher amount").withStyle(ChatFormatting.GRAY))
                .build());

        gui.setSlot(2, new GuiElementBuilder(Items.CONCRETE.lime())
                .setName(Component.literal("Confirm Bid").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to place your bid").withStyle(ChatFormatting.GRAY))
                .setCallback((index, type, containerInput, slotGui) -> {
                    String input = gui.getInput();
                    try {
                        long amount = Long.parseLong(input.trim());
                        gui.close();
                        UndergroundAuctionManager.placeBid(player, amount);
                        open(player); // Reopen main GUI
                    } catch (NumberFormatException e) {
                        player.sendSystemMessage(Component.literal("❌ Invalid number! Please enter a valid amount.").withStyle(ChatFormatting.RED));
                    }
                })
                .build());

        gui.open();
    }

    // Call when player disconnects
    public static void onPlayerDisconnect(ServerPlayer player) {
        openGuis.remove(player.getUUID());
    }
}