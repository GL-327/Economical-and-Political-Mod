package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;import com.political.SlayerManager;
import com.political.SlayerData;
import com.political.SlayerItems;import net.minecraft.item.ItemStack;

public class AdminGui {

    public enum AdminPage {
        MAIN,
        ELECTIONS,
        GOVERNMENT,
        PLAYERS,
        ECONOMY,
        WORLD,
        UNDERGROUND_AUCTION,
        CUSTOM_ITEMS
    }
    public static void open(ServerPlayerEntity player) {
        openPage(player, AdminPage.MAIN);
    }

    public static void openPage(ServerPlayerEntity player, AdminPage page) {
        switch (page) {
            case MAIN -> openMainPage(player);
            case ELECTIONS -> openElectionsPage(player);
            case GOVERNMENT -> openGovernmentPage(player);
            case PLAYERS -> openPlayersPage(player);
            case ECONOMY -> openEconomyPage(player);
            case WORLD -> openWorldPage(player);
            case UNDERGROUND_AUCTION -> openUndergroundAuctionPage(player);
        }
    }

    private static void openMainPage(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X5, player, false);
        gui.setTitle(Text.literal("✦ Admin Control Panel ✦"));

        // Background
        for (int i = 0; i < 45; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.COMMAND_BLOCK)
                .setName(Text.literal("★ Admin Control Panel ★").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Server Administration Hub").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Select a category below").formatted(Formatting.DARK_GRAY))
                .glow()
                .build());

        // Category buttons
        gui.setSlot(10, new GuiElementBuilder(Items.BELL)
                .setName(Text.literal("Elections").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("• Start/End elections").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("• Toggle election system").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("• Reset impeachment").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to open").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> openPage(player, AdminPage.ELECTIONS))
                .build());

        gui.setSlot(12, new GuiElementBuilder(Items.GOLDEN_HELMET)
                .setName(Text.literal("Government").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("• Set Chair/Vice/Judge").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("• Manage dictator").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("• Reset perks").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to open").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> openPage(player, AdminPage.GOVERNMENT))
                .build());

        gui.setSlot(14, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Text.literal("Player Management").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("• Exile/Smite players").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("• Prison controls").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("• Give credits").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to open").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> openPage(player, AdminPage.PLAYERS))
                .build());

        gui.setSlot(16, new GuiElementBuilder(Items.GOLD_BLOCK)
                .setName(Text.literal("Economy").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("• Tax system").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("• View debtors").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("• Manage credits/coins").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to open").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> openPage(player, AdminPage.ECONOMY))
                .build());

        gui.setSlot(28, new GuiElementBuilder(Items.COMPASS)
                .setName(Text.literal("World Settings").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("• Set spawn location").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("• Manage NPCs").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to open").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> openPage(player, AdminPage.WORLD))
                .build());
        gui.setSlot(22, new GuiElementBuilder(Items.IRON_SWORD)
                .setName(Text.literal("⚔ Bounty Admin").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Manage the bounty system").formatted(Formatting.GRAY))
                .setCallback((idx, type, action) -> openSlayerAdminGui(player))
                .build());
        // Server status
        String chair = DataManager.getChair();
        String viceChair = DataManager.getViceChair();
        boolean dictatorActive = DictatorManager.isDictatorActive();

        gui.setSlot(31, new GuiElementBuilder(Items.BOOK)
                .setName(Text.literal("Server Status").formatted(Formatting.WHITE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Chair: " + (chair != null ? DataManager.getPlayerName(chair) : "None")).formatted(Formatting.GOLD))
                .addLoreLine(Text.literal("Vice Chair: " + (viceChair != null ? DataManager.getPlayerName(viceChair) : "None")).formatted(Formatting.AQUA))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Election: " + (ElectionManager.isElectionActive() ? "ACTIVE" : "Waiting")).formatted(ElectionManager.isElectionActive() ? Formatting.GREEN : Formatting.GRAY))
                .addLoreLine(Text.literal("Dictator: " + (dictatorActive ? DictatorManager.getDictatorName() : "None")).formatted(dictatorActive ? Formatting.RED : Formatting.GRAY))
                .build());
        gui.setSlot(34, new GuiElementBuilder(Items.ENDER_CHEST)
                .setName(Text.literal("Underground Auction").formatted(Formatting.DARK_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("• Force start auction").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("• Get auction items").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to open").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> openPage(player, AdminPage.UNDERGROUND_AUCTION))
                .build());
        // Close button
        gui.setSlot(40, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("Close").formatted(Formatting.RED))
                .setCallback((index, type, action) -> gui.close())
                .build());

        gui.open();
    }

    // ═══════════════════════════════════════════════════════════
    // ELECTIONS PAGE
    // ═══════════════════════════════════════════════════════════

    private static void openElectionsPage(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X5, player, false);
        gui.setTitle(Text.literal("Elections Management"));

        fillBackground(gui);

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.BELL)
                .setName(Text.literal("Elections").formatted(Formatting.YELLOW, Formatting.BOLD))
                .glow()
                .build());

        boolean electionActive = ElectionManager.isElectionActive();
        boolean electionEnabled = ElectionManager.isElectionSystemEnabled();
        boolean electionPaused = ElectionManager.isElectionSystemPaused();
        boolean impeachmentActive = ElectionManager.isImpeachmentActive();

        // Start Election
        gui.setSlot(10, new GuiElementBuilder(Items.GREEN_CONCRETE)
                .setName(Text.literal("Start Election").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Force start an election now").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to start").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> {
                    ElectionManager.startElection(PoliticalServer.server);
                    player.sendMessage(Text.literal("✓ Election started!").formatted(Formatting.GREEN));
                    openPage(player, AdminPage.ELECTIONS);
                })
                .build());

        // End Election
        gui.setSlot(11, new GuiElementBuilder(Items.RED_CONCRETE)
                .setName(Text.literal("End Election").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Force end current election").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to end").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> {
                    ElectionManager.endElection(PoliticalServer.server);
                    player.sendMessage(Text.literal("✓ Election ended!").formatted(Formatting.RED));
                    openPage(player, AdminPage.ELECTIONS);
                })
                .build());

        // Toggle System
        gui.setSlot(12, new GuiElementBuilder(electionEnabled ? Items.LIME_DYE : Items.GRAY_DYE)
                .setName(Text.literal("Election System").formatted(electionEnabled ? Formatting.GREEN : Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Status: " + (electionEnabled ? "ENABLED" : "DISABLED")).formatted(electionEnabled ? Formatting.GREEN : Formatting.RED))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to toggle").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> {
                    ElectionManager.setElectionSystemEnabled(!electionEnabled);
                    player.sendMessage(Text.literal("✓ System " + (!electionEnabled ? "enabled" : "disabled")).formatted(Formatting.GREEN));
                    openPage(player, AdminPage.ELECTIONS);
                })
                .build());

        // Pause/Resume
        gui.setSlot(13, new GuiElementBuilder(electionPaused ? Items.ORANGE_DYE : Items.LIGHT_BLUE_DYE)
                .setName(Text.literal("Election Timer").formatted(electionPaused ? Formatting.GOLD : Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Status: " + (electionPaused ? "PAUSED" : "RUNNING")).formatted(electionPaused ? Formatting.GOLD : Formatting.AQUA))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to " + (electionPaused ? "resume" : "pause")).formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> {
                    ElectionManager.setElectionSystemPaused(!electionPaused);
                    player.sendMessage(Text.literal("✓ Timer " + (!electionPaused ? "paused" : "resumed")).formatted(Formatting.YELLOW));
                    openPage(player, AdminPage.ELECTIONS);
                })
                .build());

        // Reset Impeachment
        gui.setSlot(14, new GuiElementBuilder(Items.TNT)
                .setName(Text.literal("Reset Impeachment").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Status: " + (impeachmentActive ? "ACTIVE" : "None")).formatted(impeachmentActive ? Formatting.RED : Formatting.GREEN))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to reset").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> {
                    ElectionManager.resetImpeachment();
                    player.sendMessage(Text.literal("✓ Impeachment reset!").formatted(Formatting.GREEN));
                    openPage(player, AdminPage.ELECTIONS);
                })
                .build());

        // Back button
        addBackButton(gui, player);

        gui.open();
    }
    private static int undergroundAuctionItemPage = 0;

    private static void openUndergroundAuctionPage(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("Underground Auction Admin"));
        fillBackground(gui);

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.ENDER_CHEST)
                .setName(Text.literal("Underground Auction").formatted(Formatting.DARK_PURPLE, Formatting.BOLD))
                .glow()
                .build());

        boolean auctionActive = UndergroundAuctionManager.isAuctionActive();
        long timeUntil = UndergroundAuctionManager.getTimeUntilNextAuction();
        String timeStr = PoliticalServer.formatTime(timeUntil);

        // Status display
        gui.setSlot(10, new GuiElementBuilder(auctionActive ? Items.LIME_CONCRETE : Items.RED_CONCRETE)
                .setName(Text.literal("Auction Status").formatted(auctionActive ? Formatting.GREEN : Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Status: " + (auctionActive ? "ACTIVE" : "Waiting")).formatted(auctionActive ? Formatting.GREEN : Formatting.RED))
                .addLoreLine(Text.literal("Next auction: " + timeStr).formatted(Formatting.GRAY))
                .build());

        // Force Start Auction
        gui.setSlot(12, new GuiElementBuilder(Items.CLOCK)
                .setName(Text.literal("Force Start Auction").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Immediately starts an auction").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Resets the 6-hour timer").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to start").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> {
                    UndergroundAuctionManager.forceStartAuction(PoliticalServer.server);
                    player.sendMessage(Text.literal("✓ Underground auction started!").formatted(Formatting.LIGHT_PURPLE));
                    gui.close();
                })
                .build());

        // End Current Auction
        gui.setSlot(14, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("End Current Auction").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Force ends the current auction").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to end").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> {
                    UndergroundAuctionManager.forceEndAuction();
                    player.sendMessage(Text.literal("✓ Auction ended!").formatted(Formatting.RED));
                    openPage(player, AdminPage.UNDERGROUND_AUCTION);
                })
                .build());

        // Custom Items Section (separate from auction items)
        gui.setSlot(16, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Text.literal("Custom Items").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to open custom items panel").formatted(Formatting.GRAY))
                .glow()
                .setCallback((index, type, action) -> {
                    openCustomItemsPage(player);
                })
                .build());

        // Section header for auction items
        gui.setSlot(22, new GuiElementBuilder(Items.CHEST)
                .setName(Text.literal("Auction Items").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click items below to receive them").formatted(Formatting.GRAY))
                .build());

        // Get all possible auction items
        List<UndergroundAuctionManager.AuctionItem> items = UndergroundAuctionManager.getAllPossibleItems();

        // Expanded item slots - 3 rows of 7
        int[] itemSlots = {28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43, 46, 47, 48, 49, 50, 51, 52};
        int itemsPerPage = itemSlots.length;
        int totalPages = (int) Math.ceil((double) items.size() / itemsPerPage);
        int startIndex = undergroundAuctionItemPage * itemsPerPage;

        for (int i = 0; i < itemSlots.length; i++) {
            int itemIndex = startIndex + i;
            if (itemIndex < items.size()) {
                UndergroundAuctionManager.AuctionItem item = items.get(itemIndex);
                final int finalItemIndex = itemIndex;

                if (item != null && item.itemStack != null) {
                    gui.setSlot(itemSlots[i], new GuiElementBuilder(item.itemStack.getItem())
                            .setName(Text.literal(item.name).formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                            .addLoreLine(Text.literal(""))
                            .addLoreLine(Text.literal("Starting bid: " + item.startingBid + " credits").formatted(Formatting.GOLD))
                            .addLoreLine(Text.literal(""))
                            .addLoreLine(Text.literal("▶ Click to receive").formatted(Formatting.GREEN))
                            .glow()
                            .setCallback((index, type, action) -> {
                                // Give the ACTUAL item stack, not just by index
                                player.giveItemStack(item.itemStack.copy());
                                player.sendMessage(Text.literal("✓ Given " + item.name).formatted(Formatting.GREEN));
                            })
                            .build());
                }
            }
        }

        // Pagination buttons
        if (undergroundAuctionItemPage > 0) {
            gui.setSlot(18, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("◀ Previous Page").formatted(Formatting.YELLOW))
                    .setCallback((index, type, action) -> {
                        undergroundAuctionItemPage--;
                        openPage(player, AdminPage.UNDERGROUND_AUCTION);
                    })
                    .build());
        }

        if (undergroundAuctionItemPage < totalPages - 1) {
            gui.setSlot(26, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("Next Page ▶").formatted(Formatting.YELLOW))
                    .setCallback((index, type, action) -> {
                        undergroundAuctionItemPage++;
                        openPage(player, AdminPage.UNDERGROUND_AUCTION);
                    })
                    .build());
        }

        // Page indicator
        gui.setSlot(22, new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal("Page " + (undergroundAuctionItemPage + 1) + "/" + Math.max(1, totalPages)).formatted(Formatting.WHITE))
                .build());

        // Back button
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("Back").formatted(Formatting.GRAY))
                .setCallback((index, type, action) -> {
                    undergroundAuctionItemPage = 0; // Reset page
                    openPage(player, AdminPage.MAIN);
                })
                .build());

        // Close button
        gui.setSlot(53, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("Close").formatted(Formatting.RED))
                .setCallback((index, type, action) -> gui.close())
                .build());

        gui.open();
    }

    private static void openCustomItemsPage(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X4, player, false);
        gui.setTitle(Text.literal("Custom Items"));
        fillBackground(gui);

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Text.literal("Custom Items").formatted(Formatting.GOLD, Formatting.BOLD))
                .glow()
                .build());

        // Harvey's Stick
        gui.setSlot(10, new GuiElementBuilder(Items.STICK)
                .setName(Text.literal("Harvey's Stick").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("◆ LEGENDARY WEAPON ◆").formatted(Formatting.GOLD))
                .addLoreLine(Text.literal("Summons lightning on hit").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to receive").formatted(Formatting.GREEN))
                .glow()
                .setCallback((index, type, action) -> {
                    player.giveItemStack(CustomItemHandler.createHarveysStick());
                    player.sendMessage(Text.literal("✓ Given Harvey's Stick!").formatted(Formatting.GREEN));
                })
                .build());

        // The Gavel
        gui.setSlot(11, new GuiElementBuilder(Items.MACE)
                .setName(Text.literal("The Gavel").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("◆ JUDICIAL AUTHORITY ◆").formatted(Formatting.LIGHT_PURPLE))
                .addLoreLine(Text.literal("AOE explosion attack").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Consumes Wind Charge").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to receive").formatted(Formatting.GREEN))
                .glow()
                .setCallback((index, type, action) -> {
                    player.giveItemStack(CustomItemHandler.createTheGavel());
                    player.sendMessage(Text.literal("✓ Given The Gavel!").formatted(Formatting.GREEN));
                })
                .build());

        // Hermes Shoes
        gui.setSlot(12, new GuiElementBuilder(Items.IRON_BOOTS)
                .setName(Text.literal("Hermes Shoes").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("◆ DIVINE FOOTWEAR ◆").formatted(Formatting.AQUA))
                .addLoreLine(Text.literal("Permanent Speed III").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to receive").formatted(Formatting.GREEN))
                .glow()
                .setCallback((index, type, action) -> {
                    player.giveItemStack(CustomItemHandler.createHermesShoes());
                    player.sendMessage(Text.literal("✓ Given Hermes Shoes!").formatted(Formatting.GREEN));
                })
                .build());

        // HPEBM
        gui.setSlot(14, new GuiElementBuilder(Items.IRON_SHOVEL)
                .setName(Text.literal("H.P.E.B.M.").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("◆ PLASMA WEAPON ◆").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal("Continuous beam attack").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Costs XP to fire").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to receive").formatted(Formatting.GREEN))
                .glow()
                .setCallback((index, type, action) -> {
                    player.giveItemStack(CustomItemHandler.createHPEBM());
                    player.sendMessage(Text.literal("✓ Given H.P.E.B.M.!").formatted(Formatting.GREEN));
                })
                .build());

        // Warden's Core
        gui.setSlot(16, new GuiElementBuilder(Items.ECHO_SHARD)
                .setName(Text.literal("Warden's Core").formatted(Formatting.DARK_AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("◆ ANCIENT ARTIFACT ◆").formatted(Formatting.DARK_AQUA))
                .addLoreLine(Text.literal("Ultra weapon crafting material").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to receive").formatted(Formatting.GREEN))
                .glow()
                .setCallback((index, type, action) -> {
                    player.giveItemStack(CustomItemHandler.createWardenCore());
                    player.sendMessage(Text.literal("✓ Given Warden's Core!").formatted(Formatting.GREEN));
                })
                .build());

        // Ultra Overclocked
        gui.setSlot(22, new GuiElementBuilder(Items.GOLDEN_SHOVEL)
                .setName(Text.literal("Ultra Overclocked").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("◆ ULTIMATE WEAPON ◆").formatted(Formatting.LIGHT_PURPLE))
                .addLoreLine(Text.literal("Devastating beam + Sonic Devastation").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to receive").formatted(Formatting.GREEN))
                .glow()
                .setCallback((index, type, action) -> {
                    player.giveItemStack(CustomItemHandler.createUltraOverclockedBeam());
                    player.sendMessage(Text.literal("✓ Given Ultra Overclocked!").formatted(Formatting.GREEN));
                })
                .build());

        // Back button
        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("Back").formatted(Formatting.GRAY))
                .setCallback((index, type, action) -> openPage(player, AdminPage.UNDERGROUND_AUCTION))
                .build());

        gui.open();
    }
    // ═══════════════════════════════════════════════════════════
    // GOVERNMENT PAGE
    // ═══════════════════════════════════════════════════════════

    private static void openGovernmentPage(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X5, player, false);
        gui.setTitle(Text.literal("Government Management"));

        fillBackground(gui);

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.GOLDEN_HELMET)
                .setName(Text.literal("Government").formatted(Formatting.GOLD, Formatting.BOLD))
                .glow()
                .build());

        String chair = DataManager.getChair();
        String viceChair = DataManager.getViceChair();
        String judge = DataManager.getJudge();
        boolean dictatorActive = DictatorManager.isDictatorActive();

        // Set Chair
        gui.setSlot(10, new GuiElementBuilder(Items.GOLDEN_HELMET)
                .setName(Text.literal("Set Chair").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Current: " + (chair != null ? DataManager.getPlayerName(chair) : "None")).formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to select").formatted(Formatting.GRAY))
                .setCallback((index, type, action) -> openPlayerSelector(player, "chair"))
                .build());

        // Set Vice Chair
        gui.setSlot(11, new GuiElementBuilder(Items.IRON_HELMET)
                .setName(Text.literal("Set Vice Chair").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Current: " + (viceChair != null ? DataManager.getPlayerName(viceChair) : "None")).formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to select").formatted(Formatting.GRAY))
                .setCallback((index, type, action) -> openPlayerSelector(player, "vicechair"))
                .build());

        // Set Judge
        gui.setSlot(12, new GuiElementBuilder(Items.LEATHER_HELMET)
                .setName(Text.literal("Set Judge").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Current: " + (judge != null ? DataManager.getPlayerName(judge) : "None")).formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to select").formatted(Formatting.GRAY))
                .setCallback((index, type, action) -> openPlayerSelector(player, "judge"))
                .build());

        // Set Dictator
        gui.setSlot(14, new GuiElementBuilder(dictatorActive ? Items.NETHERITE_HELMET : Items.CHAINMAIL_HELMET)
                .setName(Text.literal("Set Dictator").formatted(Formatting.DARK_RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Status: " + (dictatorActive ? "ACTIVE" : "Inactive")).formatted(dictatorActive ? Formatting.RED : Formatting.GREEN))
                .addLoreLine(Text.literal(dictatorActive ? "Dictator: " + DictatorManager.getDictatorName() : "No dictator").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to select (must be Chair)").formatted(Formatting.GRAY))
                .setCallback((index, type, action) -> openPlayerSelector(player, "dictator"))
                .build());

        // Remove Dictator
        gui.setSlot(15, new GuiElementBuilder(Items.EMERALD)
                .setName(Text.literal("Remove Dictator").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("End current dictatorship").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to remove").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> {
                    if (dictatorActive) {
                        DictatorManager.removeDictator();
                        player.sendMessage(Text.literal("✓ Dictatorship ended!").formatted(Formatting.GREEN));
                        openPage(player, AdminPage.GOVERNMENT);
                    } else {
                        player.sendMessage(Text.literal("✗ No active dictator!").formatted(Formatting.RED));
                    }
                })
                .build());

        // Reset Perks
        gui.setSlot(19, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Text.literal("Reset Perks").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Unlock perk selection").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("for Chair and Vice Chair").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to reset").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> {
                    PerkManager.onNewTermStart();
                    PerkManager.setPreviousTermPerks(new ArrayList<>());
                    DataManager.save(PoliticalServer.server);
                    player.sendMessage(Text.literal("✓ Perks reset!").formatted(Formatting.GREEN));
                    openPage(player, AdminPage.GOVERNMENT);
                })
                .build());

        // Clear All Perks
        gui.setSlot(20, new GuiElementBuilder(Items.FIRE_CHARGE)
                .setName(Text.literal("Clear All Perks").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Remove all active perks").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to clear").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> {
                    PerkManager.clearAllPerks();
                    DataManager.save(PoliticalServer.server);
                    player.sendMessage(Text.literal("✓ All perks cleared!").formatted(Formatting.RED));
                    openPage(player, AdminPage.GOVERNMENT);
                })
                .build());

        addBackButton(gui, player);
        gui.open();
    }

    // ═══════════════════════════════════════════════════════════
    // PLAYERS PAGE
    // ═══════════════════════════════════════════════════════════

    private static void openPlayersPage(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X5, player, false);
        gui.setTitle(Text.literal("Player Management"));

        fillBackground(gui);

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Text.literal("Player Management").formatted(Formatting.AQUA, Formatting.BOLD))
                .glow()
                .build());

        // Give Credits
        gui.setSlot(10, new GuiElementBuilder(Items.GOLD_NUGGET)
                .setName(Text.literal("Give Credits").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Add credits to a player").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to select").formatted(Formatting.YELLOW))
                .glow()
                .setCallback((index, type, action) -> openCreditsGui(player))
                .build());

        // Prison Controls
        gui.setSlot(11, new GuiElementBuilder(Items.IRON_BARS)
                .setName(Text.literal("Prison Controls").formatted(Formatting.GRAY, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Imprison/release players").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to open").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> openPrisonGui(player))
                .build());

        // Exile Player
        gui.setSlot(12, new GuiElementBuilder(Items.ENDER_PEARL)
                .setName(Text.literal("Exile Player").formatted(Formatting.DARK_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Teleport player far away").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to select").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> openPlayerSelector(player, "exile"))
                .build());

        // Smite Player
        gui.setSlot(13, new GuiElementBuilder(Items.LIGHTNING_ROD)
                .setName(Text.literal("Smite Player").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Strike with lightning").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to select").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> openPlayerSelector(player, "smite"))
                .build());

        addBackButton(gui, player);
        gui.open();
    }

    // ═══════════════════════════════════════════════════════════
    // ECONOMY PAGE
    // ═══════════════════════════════════════════════════════════

    private static void openEconomyPage(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X5, player, false);
        gui.setTitle(Text.literal("Economy Management"));

        fillBackground(gui);

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.GOLD_BLOCK)
                .setName(Text.literal("Economy").formatted(Formatting.GREEN, Formatting.BOLD))
                .glow()
                .build());

        boolean taxEnabled = TaxManager.isTaxEnabled();

        // Tax System Toggle
        gui.setSlot(10, new GuiElementBuilder(taxEnabled ? Items.GOLD_BLOCK : Items.GOLD_INGOT)
                .setName(Text.literal("Tax System").formatted(taxEnabled ? Formatting.GREEN : Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Status: " + (taxEnabled ? "ENABLED" : "DISABLED")).formatted(taxEnabled ? Formatting.GREEN : Formatting.RED))
                .addLoreLine(Text.literal("Daily: " + TaxManager.getDailyTaxAmount() + " credits").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to toggle").formatted(Formatting.GRAY))
                .setCallback((index, type, action) -> {
                    TaxManager.setTaxEnabled(!taxEnabled);
                    player.sendMessage(Text.literal("✓ Tax " + (!taxEnabled ? "enabled" : "disabled")).formatted(Formatting.GREEN));
                    openPage(player, AdminPage.ECONOMY);
                })
                .build());

        // Set Tax Amount
        gui.setSlot(11, new GuiElementBuilder(Items.GOLD_NUGGET)
                .setName(Text.literal("Set Tax Amount").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Current: " + TaxManager.getDailyTaxAmount() + " credits").formatted(Formatting.GOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Left: +1  |  Right: -1").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal("Shift-click: ±5").formatted(Formatting.AQUA))
                .setCallback((index, type, action) -> {
                    int current = TaxManager.getDailyTaxAmount();
                    int change = type.isLeft ? 1 : -1;
                    if (type.shift) change *= 5;
                    int newAmount = Math.max(1, Math.min(100, current + change));
                    TaxManager.setDailyTaxAmount(newAmount);
                    openPage(player, AdminPage.ECONOMY);
                })
                .build());

        // View Debtors
        gui.setSlot(12, new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal("View Debtors").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("See players in debt").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to view").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> RulerGui.openDebtorsPage(player, "admin"))
                .build());

        addBackButton(gui, player);
        gui.open();
    }

    // ═══════════════════════════════════════════════════════════
    // WORLD PAGE
    // ═══════════════════════════════════════════════════════════

    private static void openWorldPage(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X5, player, false);
        gui.setTitle(Text.literal("World Settings"));

        fillBackground(gui);

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.COMPASS)
                .setName(Text.literal("World Settings").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .glow()
                .build());

        boolean hasSpawn = SpawnManager.hasSpawn();

        // Set Spawn
        gui.setSlot(10, new GuiElementBuilder(Items.RESPAWN_ANCHOR)
                .setName(Text.literal("Set Spawn Location").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Current: " + SpawnManager.getSpawnInfo()).formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to set to your location").formatted(Formatting.GRAY))
                .setCallback((index, type, action) -> {
                    SpawnManager.setSpawn(player);
                    openPage(player, AdminPage.WORLD);
                })
                .build());

        // Test Spawn
        if (hasSpawn) {
            gui.setSlot(11, new GuiElementBuilder(Items.ENDER_PEARL)
                    .setName(Text.literal("Test Spawn").formatted(Formatting.AQUA, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Teleport to spawn").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("▶ Click to teleport").formatted(Formatting.YELLOW))
                    .setCallback((index, type, action) -> {
                        SpawnManager.teleportToSpawn(player);
                        gui.close();
                    })
                    .build());
        }

        // Place Auction Master
        gui.setSlot(19, new GuiElementBuilder(Items.EMERALD_BLOCK)
                .setName(Text.literal("Place Auction Master").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Spawn at your location").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to spawn").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> {
                    var world = player.getEntityWorld();
                    AuctionMasterManager.spawnAuctionMaster(world, player.getX(), player.getY(), player.getZ(), player.getYaw());
                    player.sendMessage(Text.literal("✓ Auction Master spawned!").formatted(Formatting.GREEN));
                    gui.close();
                })
                .build());

        // Place Underground Auctioneer
        gui.setSlot(20, new GuiElementBuilder(Items.PURPLE_STAINED_GLASS)
                .setName(Text.literal("Place Underground Auctioneer").formatted(Formatting.DARK_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Spawn at your location").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to spawn").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> {
                    var world = player.getEntityWorld();
                    UndergroundAuctionManager.spawnAuctioneer(world, player.getX(), player.getY(), player.getZ(), player.getYaw());
                    player.sendMessage(Text.literal("✓ Underground Auctioneer spawned!").formatted(Formatting.LIGHT_PURPLE));
                    gui.close();
                })
                .build());

        addBackButton(gui, player);
        gui.open();
    }

    // ═══════════════════════════════════════════════════════════
    // HELPER METHODS (from old AdminGui)
    // ═══════════════════════════════════════════════════════════

    private static void fillBackground(SimpleGui gui) {
        for (int i = 0; i < gui.getSize(); i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }
    }

    private static void addBackButton(SimpleGui gui, ServerPlayerEntity player) {
        gui.setSlot(gui.getSize() - 5, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> openMainPage(player))
                .build());
    }

    private static void openPlayerSelector(ServerPlayerEntity admin, String action) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, admin, false);
        gui.setTitle(Text.literal("Select Player - " + action.substring(0, 1).toUpperCase() + action.substring(1)));

        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        gui.setSlot(4, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Text.literal("Select a Player").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal("Action: " + action).formatted(Formatting.GRAY))
                .build());

        List<ServerPlayerEntity> players = new ArrayList<>(PoliticalServer.server.getPlayerManager().getPlayerList());
        int slot = 10;
        int maxSlot = 44;

        for (ServerPlayerEntity target : players) {
            if (slot > maxSlot) break;
            if (slot % 9 == 0) slot++;
            if (slot % 9 == 8) slot += 2;

            final ServerPlayerEntity finalTarget = target;
            gui.setSlot(slot, new GuiElementBuilder(Items.PLAYER_HEAD)
                    .setSkullOwner(target.getGameProfile(), PoliticalServer.server)
                    .setName(Text.literal(target.getName().getString()).formatted(Formatting.WHITE))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("▶ Click to select").formatted(Formatting.YELLOW))
                    .setCallback((index, type, clickAction) -> {
                        executeAction(admin, finalTarget, action);
                    })
                    .build());
            slot++;
        }

        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("Back").formatted(Formatting.GRAY))
                .setCallback((index, type, action1) -> open(admin))
                .build());

        gui.open();
    }

    private static void executeAction(ServerPlayerEntity admin, ServerPlayerEntity target, String action) {
        switch (action) {
            case "chair" -> {
                DataManager.setChair(target.getUuidAsString());
                DataManager.setChairTermCount(1);
                DataManager.save(PoliticalServer.server);
                admin.sendMessage(Text.literal("✓ Set " + target.getName().getString() + " as Chair!").formatted(Formatting.GREEN));
                target.sendMessage(Text.literal("You have been appointed as Chair!").formatted(Formatting.GOLD, Formatting.BOLD));
            }
            case "vicechair" -> {
                DataManager.setViceChair(target.getUuidAsString());
                DataManager.save(PoliticalServer.server);
                admin.sendMessage(Text.literal("✓ Set " + target.getName().getString() + " as Vice Chair!").formatted(Formatting.GREEN));
                target.sendMessage(Text.literal("You have been appointed as Vice Chair!").formatted(Formatting.GOLD, Formatting.BOLD));
            }
            case "judge" -> {
                DataManager.setJudge(target.getUuidAsString());
                DataManager.save(PoliticalServer.server);
                admin.sendMessage(Text.literal("✓ Set " + target.getName().getString() + " as Judge!").formatted(Formatting.GREEN));
                target.sendMessage(Text.literal("You have been appointed as Judge!").formatted(Formatting.GOLD, Formatting.BOLD));
            }
            case "dictator" -> {
                String chair = DataManager.getChair();
                if (!target.getUuidAsString().equals(chair)) {
                    admin.sendMessage(Text.literal("✗ Player must be Chair to become Dictator!").formatted(Formatting.RED));
                    open(admin);
                    return;
                }
                DictatorManager.setDictator(target);
                admin.sendMessage(Text.literal("✓ Set " + target.getName().getString() + " as DICTATOR!").formatted(Formatting.DARK_RED, Formatting.BOLD));
            }
            case "exile" -> {
                Random rand = new Random();
                double distance = 10_000 + rand.nextDouble() * 90_000;
                double angle = rand.nextDouble() * Math.PI * 2;
                double x = Math.cos(angle) * distance;
                double z = Math.sin(angle) * distance;
                target.teleport(PoliticalServer.server.getOverworld(), x, 100, z, Set.of(), 0, 0, false);
                target.sendMessage(Text.literal("You have been EXILED!").formatted(Formatting.RED, Formatting.BOLD));
                admin.sendMessage(Text.literal("✓ Exiled " + target.getName().getString() + "!").formatted(Formatting.RED));
                for (ServerPlayerEntity p : PoliticalServer.server.getPlayerManager().getPlayerList()) {
                    p.sendMessage(Text.literal("⚖ " + target.getName().getString() + " has been exiled!").formatted(Formatting.RED));
                }
            }
            case "smite" -> {
                DictatorManager.smitePlayer(admin, target);
                admin.sendMessage(Text.literal("✓ Smited " + target.getName().getString() + "!").formatted(Formatting.YELLOW));
            }
            case "release" -> {
                PrisonManager.release(target);
                admin.sendMessage(Text.literal("✓ Released " + target.getName().getString() + "!").formatted(Formatting.GREEN));
                target.sendMessage(Text.literal("You have been released!").formatted(Formatting.GREEN, Formatting.BOLD));
            }
            case "imprison" -> {
                openImprisonTimeSelector(admin, target);
                return;
            }
            case "credits" -> {
                openCreditsAmountSelector(admin, target);
                return;
            }
        }
        open(admin);
    }

    private static void openCreditsGui(ServerPlayerEntity admin) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, admin, false);
        gui.setTitle(Text.literal("Give Credits - Select Player"));

        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        gui.setSlot(4, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Text.literal("Give Credits").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal("Select a player").formatted(Formatting.GRAY))
                .glow()
                .build());

        List<ServerPlayerEntity> players = new ArrayList<>(PoliticalServer.server.getPlayerManager().getPlayerList());
        int slot = 10;
        int maxSlot = 44;

        for (ServerPlayerEntity target : players) {
            if (slot > maxSlot) break;
            if (slot % 9 == 0) slot++;
            if (slot % 9 == 8) slot += 2;

            final ServerPlayerEntity finalTarget = target;
            int currentCredits = DataManager.getCredits(target.getUuidAsString());

            gui.setSlot(slot, new GuiElementBuilder(Items.PLAYER_HEAD)
                    .setSkullOwner(target.getGameProfile(), PoliticalServer.server)
                    .setName(Text.literal(target.getName().getString()).formatted(Formatting.WHITE))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Credits: " + currentCredits).formatted(Formatting.YELLOW))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("▶ Click to give credits").formatted(Formatting.GREEN))
                    .setCallback((index, type, clickAction) -> {
                        openCreditsAmountSelector(admin, finalTarget);
                    })
                    .build());
            slot++;
        }

        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("Back").formatted(Formatting.GRAY))
                .setCallback((index, type, action) -> openPage(admin, AdminPage.PLAYERS))
                .build());

        gui.open();
    }
    private static void spawnTestBoss(ServerPlayerEntity admin, SlayerManager.SlayerType slayerType, int tier) {
        // Cancel any existing quest
        if (SlayerManager.hasActiveQuest(admin)) {
            SlayerManager.cancelQuest(admin);
        }

        // Directly spawn boss without starting a quest (admin bypass)
        SlayerManager.spawnTestBoss(admin, slayerType, tier);

        admin.sendMessage(Text.literal("✔ Spawned " + slayerType.bossName + " Tier " + tier + "!")
                .formatted(Formatting.GREEN), false);
    }

    private static void openCreditsAmountSelector(ServerPlayerEntity admin, ServerPlayerEntity target) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, admin, false);
        gui.setTitle(Text.literal("Give Credits to " + target.getName().getString()));

        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        int currentCredits = DataManager.getCredits(target.getUuidAsString());
        gui.setSlot(4, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setSkullOwner(target.getGameProfile(), PoliticalServer.server)
                .setName(Text.literal(target.getName().getString()).formatted(Formatting.WHITE, Formatting.BOLD))
                .addLoreLine(Text.literal("Current: " + currentCredits).formatted(Formatting.YELLOW))
                .build());

        int[] amounts = {10, 50, 100, 500, 1000};
        int[] slots = {10, 11, 12, 13, 14};

        for (int i = 0; i < amounts.length; i++) {
            final int amount = amounts[i];
            gui.setSlot(slots[i], new GuiElementBuilder(Items.GOLD_NUGGET)
                    .setName(Text.literal("+" + amount + " Credits").formatted(Formatting.GREEN, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("▶ Click to give").formatted(Formatting.YELLOW))
                    .setCallback((index, type, action) -> {
                        CreditItem.giveCredits(target, amount);
                        admin.sendMessage(Text.literal("✓ Gave " + amount + " credits!").formatted(Formatting.GREEN));
                        target.sendMessage(Text.literal("You received " + amount + " credits!").formatted(Formatting.GREEN));
                        openCreditsAmountSelector(admin, target);
                    })
                    .build());
        }

        gui.setSlot(16, new GuiElementBuilder(Items.REDSTONE)
                .setName(Text.literal("Remove Credits").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click for options").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> {
                    openCreditsRemoveSelector(admin, target);
                })
                .build());

        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("Back").formatted(Formatting.GRAY))
                .setCallback((index, type, action) -> {
                    openCreditsGui(admin);
                })
                .build());

        gui.open();
    }

    private static void openCreditsRemoveSelector(ServerPlayerEntity admin, ServerPlayerEntity target) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, admin, false);
        gui.setTitle(Text.literal("Remove Credits from " + target.getName().getString()));

        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        int currentCredits = DataManager.getCredits(target.getUuidAsString());
        gui.setSlot(4, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setSkullOwner(target.getGameProfile(), PoliticalServer.server)
                .setName(Text.literal(target.getName().getString()).formatted(Formatting.WHITE, Formatting.BOLD))
                .addLoreLine(Text.literal("Current: " + currentCredits).formatted(Formatting.YELLOW))
                .build());

        int[] amounts = {10, 50, 100, 500, 1000};
        int[] slots = {10, 11, 12, 13, 14};

        for (int i = 0; i < amounts.length; i++) {
            final int amount = amounts[i];
            gui.setSlot(slots[i], new GuiElementBuilder(Items.REDSTONE)
                    .setName(Text.literal("-" + amount + " Credits").formatted(Formatting.RED, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("▶ Click to remove").formatted(Formatting.YELLOW))
                    .setCallback((index, type, action) -> {
                        CreditItem.removeCredits(target, amount);
                        admin.sendMessage(Text.literal("✓ Removed " + amount + " credits!").formatted(Formatting.RED));
                        openCreditsRemoveSelector(admin, target);
                    })
                    .build());
        }

        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("Back").formatted(Formatting.GRAY))
                .setCallback((index, type, action) -> {
                    openCreditsAmountSelector(admin, target);
                })
                .build());

        gui.open();
    }
    private static void openBasicSwordsMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
        gui.setTitle(Text.literal("⚔ Basic Bounty Swords"));

        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        int slot = 10;
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            final SlayerManager.SlayerType finalType = type;
            gui.setSlot(slot, new GuiElementBuilder(Items.IRON_SWORD)
                    .setName(Text.literal(type.displayName + " Slayer Sword").formatted(type.color, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("2x damage to " + type.displayName + "s").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("Click to receive!").formatted(Formatting.GREEN))
                    .setCallback((idx, clickType, action) -> SlayerItems.giveSlayerSword(player, finalType))
                    .build());
            slot++;
        }

        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openCustomItemsAdminGui(player))
                .build());

        gui.open();
    }
    private static void openUpgradedSwordsMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
        gui.setTitle(Text.literal("⚔ Upgraded Bounty Swords"));

        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        int slot = 10;
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            final SlayerManager.SlayerType finalType = type;
            gui.setSlot(slot, new GuiElementBuilder(Items.DIAMOND_SWORD)
                    .setName(Text.literal(type.displayName + " Slayer Sword II").formatted(type.color, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("3x damage, counts as 3 kills").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("Click to receive!").formatted(Formatting.GREEN))
                    .glow()
                    .setCallback((idx, clickType, action) -> {
                        ItemStack sword = SlayerItems.createUpgradedSlayerSword(finalType);
                        if (!player.getInventory().insertStack(sword)) {
                            player.dropItem(sword, false);
                        }
                        player.sendMessage(Text.literal("✔ Received " + finalType.displayName + " Slayer Sword II!")
                                .formatted(Formatting.GREEN), false);
                    })
                    .build());
            slot++;
        }

        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openCustomItemsAdminGui(player))
                .build());

        gui.open();
    }
    public static void openSlayerAdminGui(ServerPlayerEntity player) {
        // Just redirect to the custom items admin
        openCustomItemsAdminGui(player);
    }

    private static void openPrisonGui(ServerPlayerEntity admin) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, admin, false);
        gui.setTitle(Text.literal("Prison Controls"));

        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        gui.setSlot(4, new GuiElementBuilder(Items.IRON_BARS)
                .setName(Text.literal("Prison Controls").formatted(Formatting.GRAY, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Select player to imprison/release").formatted(Formatting.WHITE))
                .build());

        List<ServerPlayerEntity> players = new ArrayList<>(PoliticalServer.server.getPlayerManager().getPlayerList());
        int slot = 10;
        int maxSlot = 44;

        for (ServerPlayerEntity target : players) {
            if (slot > maxSlot) break;
            if (slot % 9 == 0) slot++;
            if (slot % 9 == 8) slot += 2;

            final ServerPlayerEntity finalTarget = target;
            boolean isPrisoner = PrisonManager.isPrisoner(target.getUuidAsString());

            gui.setSlot(slot, new GuiElementBuilder(isPrisoner ? Items.IRON_BARS : Items.PLAYER_HEAD)
                    .setSkullOwner(target.getGameProfile(), PoliticalServer.server)
                    .setName(Text.literal(target.getName().getString()).formatted(isPrisoner ? Formatting.RED : Formatting.WHITE))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Status: " + (isPrisoner ? "IMPRISONED" : "Free")).formatted(isPrisoner ? Formatting.RED : Formatting.GREEN))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal(isPrisoner ? "▶ Click to release" : "▶ Click to imprison").formatted(Formatting.YELLOW))
                    .setCallback((index, type, clickAction) -> {
                        if (isPrisoner) {
                            executeAction(admin, finalTarget, "release");
                        } else {
                            openImprisonTimeSelector(admin, finalTarget);
                        }
                    })
                    .build());
            slot++;
        }

        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("Back").formatted(Formatting.GRAY))
                .setCallback((index, type, action) -> openPage(admin, AdminPage.PLAYERS))
                .build());

        gui.open();
    }

    private static void openImprisonTimeSelector(ServerPlayerEntity admin, ServerPlayerEntity target) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, admin, false);
        gui.setTitle(Text.literal("Imprison " + target.getName().getString()));

        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        gui.setSlot(4, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setSkullOwner(target.getGameProfile(), PoliticalServer.server)
                .setName(Text.literal("Imprison " + target.getName().getString()).formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal("Select duration").formatted(Formatting.GRAY))
                .build());

        int[] times = {1, 5, 10, 30, 60, 120};
        int[] slots = {10, 11, 12, 13, 14, 15};
        String[] labels = {"1 min", "5 min", "10 min", "30 min", "1 hour", "2 hours"};

        for (int i = 0; i < times.length; i++) {
            final int time = times[i];
            gui.setSlot(slots[i], new GuiElementBuilder(Items.CLOCK)
                    .setName(Text.literal(labels[i]).formatted(Formatting.YELLOW, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("▶ Click to imprison").formatted(Formatting.RED))
                    .setCallback((index, type, action) -> {
                        double x = admin.getX();
                        double y = admin.getY();
                        double z = admin.getZ();
                        PrisonManager.imprison(target, time, x, y, z);
                        admin.sendMessage(Text.literal("✓ Imprisoned for " + time + " min!").formatted(Formatting.RED));
                        target.sendMessage(Text.literal("You have been imprisoned for " + time + " minutes!").formatted(Formatting.RED, Formatting.BOLD));
                        for (ServerPlayerEntity p : PoliticalServer.server.getPlayerManager().getPlayerList()) {
                            p.sendMessage(Text.literal("⚖ " + target.getName().getString() + " imprisoned for " + time + " min!").formatted(Formatting.RED));
                        }
                        openPage(admin, AdminPage.PLAYERS);
                    })
                    .build());
        }

        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("Back").formatted(Formatting.GRAY))
                .setCallback((index, type, action) -> {
                    openPrisonGui(admin);
                })
                .build());

        gui.open();
    }

    public static void openCustomItemsAdminGui(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("🎁 Custom Items Admin"));

        // Background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.COMMAND_BLOCK)
                .setName(Text.literal("🎁 Item Administration").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Give custom items to yourself").formatted(Formatting.GRAY))
                .build());

        // Bounty Swords
        gui.setSlot(19, new GuiElementBuilder(Items.IRON_SWORD)
                .setName(Text.literal("⚔ Bounty Swords").formatted(Formatting.WHITE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Basic slayer swords").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Click to view!").formatted(Formatting.GREEN))
                .setCallback((idx, type, action) -> openBasicSwordsMenu(player))
                .build());

        // Upgraded Swords
        gui.setSlot(21, new GuiElementBuilder(Items.DIAMOND_SWORD)
                .setName(Text.literal("⚔ Upgraded Swords").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Enhanced slayer swords").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Click to view!").formatted(Formatting.GREEN))
                .setCallback((idx, type, action) -> openUpgradedSwordsMenu(player))
                .build());

        // Legendary Weapons
        gui.setSlot(23, new GuiElementBuilder(Items.NETHERITE_SWORD)
                .setName(Text.literal("⚔ Legendary Weapons").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Ender Sword, Abyssal Blade").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Click to view!").formatted(Formatting.GREEN))
                .setCallback((idx, type, action) -> openLegendaryWeaponsMenu(player))
                .build());

        // HPEBM Weapons
        gui.setSlot(25, new GuiElementBuilder(Items.END_ROD)
                .setName(Text.literal("⚡ HPEBM Weapons").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Energy beam weapons Mk1-Mk5").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Click to view!").formatted(Formatting.GREEN))
                .setCallback((idx, type, action) -> openHPEBMMenu(player))
                .build());

        // Cores & Chunks
        gui.setSlot(29, new GuiElementBuilder(Items.ENDER_PEARL)
                .setName(Text.literal("✦ Cores & Chunks").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Crafting materials").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Click to view!").formatted(Formatting.GREEN))
                .setCallback((idx, type, action) -> openMaterialsMenu(player))
                .build());

        // Special Items
        gui.setSlot(31, new GuiElementBuilder(Items.WOODEN_AXE)
                .setName(Text.literal("✦ Special Items").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Gavel, Credits, Coins").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Click to view!").formatted(Formatting.GREEN))
                .setCallback((idx, type, action) -> openSpecialItemsMenu(player))
                .build());

        // Player Management
        gui.setSlot(33, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Text.literal("👤 Player Stats").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Set levels, reset progress").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Click to view!").formatted(Formatting.GREEN))
                .setCallback((idx, type, action) -> openPlayerManagementMenu(player))
                .build());

        gui.setSlot(37, new GuiElementBuilder(Items.DIAMOND_CHESTPLATE)
                .setName(Text.literal("🛡 Slayer Armor").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Give slayer armor sets").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("T1 and T2 versions").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to view!").formatted(Formatting.GREEN))
                .setCallback((idx, type, action) -> openArmorAdminMenu(player))
                .build());

        // Back button
        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to Admin Menu").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> open(player))
                .build());

        gui.open();

    }

// ============================================================
// LEGENDARY WEAPONS MENU
// ============================================================

    private static void openLegendaryWeaponsMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X2, player, false);
        gui.setTitle(Text.literal("⚔ Legendary Weapons"));

        for (int i = 0; i < 18; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.PURPLE_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        // Ender Sword
        gui.setSlot(3, new GuiElementBuilder(Items.NETHERITE_SWORD)
                .setName(Text.literal("⚔ Ender Sword")
                        .formatted(Formatting.DARK_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§d§lLEGENDARY"))
                .addLoreLine(Text.literal("Ability: Void Strike")
                        .formatted(Formatting.GOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to receive!")
                        .formatted(Formatting.GREEN))
                .glow()
                .setCallback((idx, type, action) -> {
                    ItemStack sword = SlayerItems.createEnderSword();
                    if (!player.getInventory().insertStack(sword)) {
                        player.dropItem(sword, false);
                    }
                    player.sendMessage(Text.literal("✔ Received Ender Sword!")
                            .formatted(Formatting.GREEN), false);
                })
                .build());

        // Abyssal Blade
        gui.setSlot(5, new GuiElementBuilder(Items.NETHERITE_SWORD)
                .setName(Text.literal("⚔ Abyssal Blade")
                        .formatted(Formatting.DARK_AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§5§lMYTHIC"))
                .addLoreLine(Text.literal("Ability: Sonic Devastation")
                        .formatted(Formatting.GOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to receive!")
                        .formatted(Formatting.GREEN))
                .glow()
                .setCallback((idx, type, action) -> {
                    ItemStack sword = SlayerItems.createAbyssalBlade();
                    if (!player.getInventory().insertStack(sword)) {
                        player.dropItem(sword, false);
                    }
                    player.sendMessage(Text.literal("✔ Received Abyssal Blade!")
                            .formatted(Formatting.GREEN), false);
                })
                .build());

        gui.setSlot(13, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openCustomItemsAdminGui(player))
                .build());

        gui.open();
    }

// ============================================================
// HPEBM WEAPONS MENU
// ============================================================
private static void openArmorAdminMenu(ServerPlayerEntity player) {
    SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X4, player, false);
    gui.setTitle(Text.literal("🛡 Slayer Armor Admin"));

    for (int i = 0; i < 36; i++) {
        gui.setSlot(i, new GuiElementBuilder(Items.CYAN_STAINED_GLASS_PANE)
                .setName(Text.literal("")).build());
    }

    // T1 Armor Sets
    gui.setSlot(1, new GuiElementBuilder(Items.IRON_CHESTPLATE)
            .setName(Text.literal("T1 Armor Sets").formatted(Formatting.WHITE, Formatting.BOLD))
            .addLoreLine(Text.literal("Basic slayer armor").formatted(Formatting.GRAY))
            .build());

    int slot = 10;
    for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
        final SlayerManager.SlayerType finalType = type;
        gui.setSlot(slot, new GuiElementBuilder(Items.IRON_CHESTPLATE)
                .setName(Text.literal(type.displayName + " Armor Set").formatted(type.color, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("T1 - Basic Set").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Click to receive full set!").formatted(Formatting.GREEN))
                .setCallback((idx, clickType, action) -> {
                    SlayerItems.giveFullArmorSet(player, finalType, 1);
                })
                .build());
        slot++;
    }

    // T2 Armor Sets
    gui.setSlot(7, new GuiElementBuilder(Items.DIAMOND_CHESTPLATE)
            .setName(Text.literal("T2 Armor Sets").formatted(Formatting.AQUA, Formatting.BOLD))
            .addLoreLine(Text.literal("Upgraded slayer armor").formatted(Formatting.GRAY))
            .glow()
            .build());

    slot = 19;
    for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
        final SlayerManager.SlayerType finalType = type;
        gui.setSlot(slot, new GuiElementBuilder(Items.DIAMOND_CHESTPLATE)
                .setName(Text.literal(type.displayName + " Armor Set II").formatted(type.color, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("T2 - Upgraded Set").formatted(Formatting.AQUA))
                .addLoreLine(Text.literal("Click to receive full set!").formatted(Formatting.GREEN))
                .glow()
                .setCallback((idx, clickType, action) -> {
                    SlayerItems.giveFullArmorSet(player, finalType, 2);
                })
                .build());
        slot++;
    }

    // Back button
    gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
            .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
            .setCallback((idx, type, action) -> openCustomItemsAdminGui(player))
            .build());

    gui.open();
}


    private static void openHPEBMMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X2, player, false);
        gui.setTitle(Text.literal("⚡ HPEBM Weapons"));

        for (int i = 0; i < 18; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.CYAN_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        // Mk1
        gui.setSlot(2, new GuiElementBuilder(Items.END_ROD)
                .setName(Text.literal("HPEBM Mk1")
                        .formatted(Formatting.WHITE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Basic energy weapon").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to receive!").formatted(Formatting.GREEN))
                .setCallback((idx, type, action) -> {
                    giveHPEBM(player, 1);
                })
                .build());

        // Mk2
        gui.setSlot(4, new GuiElementBuilder(Items.END_ROD)
                .setName(Text.literal("HPEBM Mk2")
                        .formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Enhanced output").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to receive!").formatted(Formatting.GREEN))
                .setCallback((idx, type, action) -> {
                    giveHPEBM(player, 2);
                })
                .build());

        // Mk3
        gui.setSlot(6, new GuiElementBuilder(Items.END_ROD)
                .setName(Text.literal("HPEBM Mk3")
                        .formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Military-grade").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to receive!").formatted(Formatting.GREEN))
                .setCallback((idx, type, action) -> {
                    giveHPEBM(player, 3);
                })
                .build());

        // Mk4
        gui.setSlot(10, new GuiElementBuilder(Items.END_ROD)
                .setName(Text.literal("HPEBM Mk4")
                        .formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Experimental prototype").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to receive!").formatted(Formatting.GREEN))
                .glow()
                .setCallback((idx, type, action) -> {
                    giveHPEBM(player, 4);
                })
                .build());

        // Mk5
        gui.setSlot(12, new GuiElementBuilder(Items.END_ROD)
                .setName(Text.literal("HPEBM Mk5")
                        .formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§d§lLEGENDARY"))
                .addLoreLine(Text.literal("Ultimate energy weapon").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to receive!").formatted(Formatting.GREEN))
                .glow()
                .setCallback((idx, type, action) -> {
                    giveHPEBM(player, 5);
                })
                .build());

        gui.setSlot(16, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openCustomItemsAdminGui(player))
                .build());

        gui.open();
    }

    private static void giveHPEBM(ServerPlayerEntity player, int mk) {
        ItemStack weapon = CustomItemHandler.createHPEBM(mk);
        if (!player.getInventory().insertStack(weapon)) {
            player.dropItem(weapon, false);
        }
        player.sendMessage(Text.literal("✔ Received HPEBM Mk" + mk + "!")
                .formatted(Formatting.GREEN), false);
    }

// ============================================================
// MATERIALS MENU (Cores & Chunks)
// ============================================================

    private static void openMaterialsMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X4, player, false);
        gui.setTitle(Text.literal("✦ Cores & Chunks"));

        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.ORANGE_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        // Cores row
        gui.setSlot(1, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Text.literal("§d§lCORES")
                        .formatted(Formatting.LIGHT_PURPLE))
                .addLoreLine(Text.literal("Rare boss drops").formatted(Formatting.GRAY))
                .build());

        int coreSlot = 10;
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            final SlayerManager.SlayerType finalType = type;
            gui.setSlot(coreSlot, new GuiElementBuilder(type.icon)
                    .setName(Text.literal(type.displayName + " Core")
                            .formatted(type.color, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Rare drop from " + type.bossName)
                            .formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Click to receive!").formatted(Formatting.GREEN))
                    .glow()
                    .setCallback((idx, clickType, action) -> {
                        SlayerItems.giveCore(player, finalType);
                    })
                    .build());
            coreSlot++;
        }

        // Chunks row
        gui.setSlot(7, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Text.literal("§6§lCHUNKS")
                        .formatted(Formatting.GOLD))
                .addLoreLine(Text.literal("Boss crafting materials").formatted(Formatting.GRAY))
                .build());

        int chunkSlot = 19;
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            final SlayerManager.SlayerType finalType = type;
            String chunkName = SlayerItems.getChunkName(type);
            gui.setSlot(chunkSlot, new GuiElementBuilder(type.icon)
                    .setName(Text.literal(chunkName)
                            .formatted(type.color, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Drop from " + type.bossName)
                            .formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Click to receive!").formatted(Formatting.GREEN))
                    .setCallback((idx, clickType, action) -> {
                        ItemStack chunk = SlayerItems.createChunk(finalType);
                        if (!player.getInventory().insertStack(chunk)) {
                            player.dropItem(chunk, false);
                        }
                        player.sendMessage(Text.literal("✔ Received " + chunkName + "!")
                                .formatted(Formatting.GREEN), false);
                    })
                    .build());
            chunkSlot++;
        }

        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openCustomItemsAdminGui(player))
                .build());

        gui.open();
    }

// ============================================================
// SPECIAL ITEMS MENU
// ============================================================

    private static void openSpecialItemsMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X2, player, false);
        gui.setTitle(Text.literal("✦ Special Items"));

        for (int i = 0; i < 18; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.RED_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        // Gavel
        gui.setSlot(3, new GuiElementBuilder(Items.WOODEN_AXE)
                .setName(Text.literal("⚖ The Gavel")
                        .formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Chair holder's tool").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to receive!").formatted(Formatting.GREEN))
                .setCallback((idx, type, action) -> {
                    ItemStack gavel = CustomItemHandler.createGavel();
                    if (!player.getInventory().insertStack(gavel)) {
                        player.dropItem(gavel, false);
                    }
                    player.sendMessage(Text.literal("✔ Received The Gavel!")
                            .formatted(Formatting.GREEN), false);
                })
                .build());

        // Give Credits
        gui.setSlot(5, new GuiElementBuilder(Items.EMERALD)
                .setName(Text.literal("💎 Give Credits")
                        .formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Give yourself credits").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Left-click: +100").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal("Right-click: +1000").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Shift-click: +10000").formatted(Formatting.GOLD))
                .setCallback((idx, clickType, action) -> {
                    int amount = 100;
                    if (clickType.isRight) amount = 1000;
                    if (clickType.shift) amount = 10000;
                    CreditItem.giveCredits(player, amount);
                    player.sendMessage(Text.literal("✔ Received " + amount + " credits!")
                            .formatted(Formatting.GREEN), false);
                })
                .build());

        // Give Coins
        gui.setSlot(7, new GuiElementBuilder(Items.SUNFLOWER)
                .setName(Text.literal("🪙 Give Coins")
                        .formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Give yourself coins").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Left-click: +100").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal("Right-click: +1000").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Shift-click: +10000").formatted(Formatting.GOLD))
                .setCallback((idx, clickType, action) -> {
                    int amount = 100;
                    if (clickType.isRight) amount = 1000;
                    if (clickType.shift) amount = 10000;
                    CoinManager.giveCoins(player, amount);
                    player.sendMessage(Text.literal("✔ Received " + amount + " coins!")
                            .formatted(Formatting.GREEN), false);
                })
                .build());

        gui.setSlot(13, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openCustomItemsAdminGui(player))
                .build());

        gui.open();
    }

// ============================================================
// PLAYER MANAGEMENT MENU
// ============================================================

    private static void openPlayerManagementMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
        gui.setTitle(Text.literal("👤 Player Management"));

        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.LIME_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        // Set Bounty Level
        gui.setSlot(10, new GuiElementBuilder(Items.EXPERIENCE_BOTTLE)
                .setName(Text.literal("Set Bounty Level")
                        .formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Set your bounty levels").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to open!").formatted(Formatting.GREEN))
                .setCallback((idx, type, action) -> openSetLevelGui(player))
                .build());

        // View Stats
        gui.setSlot(13, new GuiElementBuilder(Items.BOOK)
                .setName(Text.literal("View Stats")
                        .formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("View your bounty stats").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to view!").formatted(Formatting.GREEN))
                .setCallback((idx, type, action) -> openViewStatsGui(player))
                .build());

        // Reset Progress
        gui.setSlot(16, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("Reset Progress")
                        .formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("⚠ Reset all bounty progress").formatted(Formatting.DARK_RED))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to confirm!").formatted(Formatting.RED))
                .setCallback((idx, type, action) -> openResetConfirmGui(player))
                .build());

        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openCustomItemsAdminGui(player))
                .build());

        gui.open();
    }

// ============================================================
// RESET CONFIRMATION GUI
// ============================================================

    private static void openResetConfirmGui(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X1, player, false);
        gui.setTitle(Text.literal("⚠ Confirm Reset?"));

        for (int i = 0; i < 9; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.RED_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        // Confirm button
        gui.setSlot(2, new GuiElementBuilder(Items.LIME_CONCRETE)
                .setName(Text.literal("✔ Confirm Reset")
                        .formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("This will reset ALL your").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("bounty progress permanently!").formatted(Formatting.RED))
                .setCallback((idx, type, action) -> {
                    resetPlayerProgress(player);
                    player.sendMessage(Text.literal("✔ Your bounty progress has been reset.")
                            .formatted(Formatting.GREEN), false);
                    DataManager.save(PoliticalServer.server);
                    openCustomItemsAdminGui(player);
                })
                .build());

        // Cancel button
        gui.setSlot(6, new GuiElementBuilder(Items.RED_CONCRETE)
                .setName(Text.literal("✖ Cancel")
                        .formatted(Formatting.RED, Formatting.BOLD))
                .setCallback((idx, type, action) -> openPlayerManagementMenu(player))
                .build());

        gui.open();
    }

    private static void resetPlayerProgress(ServerPlayerEntity player) {
        String uuid = player.getUuidAsString();
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            SlayerData.setSlayerXp(uuid, type, 0);
        }
    }

// ============================================================
// SET LEVEL GUI
// ============================================================

    private static void openSetLevelGui(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
        gui.setTitle(Text.literal("Set Bounty Level"));

        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        int slot = 10;
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            final SlayerManager.SlayerType finalType = type;
            int currentLevel = SlayerData.getSlayerLevel(player.getUuidAsString(), type);

            gui.setSlot(slot, new GuiElementBuilder(type.icon)
                    .setName(Text.literal(type.displayName + " Slayer")
                            .formatted(type.color, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Current Level: " + currentLevel)
                            .formatted(Formatting.YELLOW))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Click to set level!")
                            .formatted(Formatting.GREEN))
                    .setCallback((idx, clickType, action) -> openLevelSelectGui(player, finalType))
                    .build());
            slot++;
        }

        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openPlayerManagementMenu(player))
                .build());

        gui.open();
    }

    private static void openLevelSelectGui(ServerPlayerEntity player, SlayerManager.SlayerType slayerType) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X2, player, false);
        gui.setTitle(Text.literal("Set " + slayerType.displayName + " Level"));

        for (int i = 0; i < 18; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        // Levels 0-12
        for (int level = 0; level <= 12; level++) {
            final int finalLevel = level;
            gui.setSlot(level + 1, new GuiElementBuilder(level == 0 ? Items.BARRIER : Items.EXPERIENCE_BOTTLE)
                    .setCount(Math.max(1, level))
                    .setName(Text.literal("Level " + level).formatted(Formatting.GREEN))
                    .setCallback((idx, type, action) -> {
                        long xp = finalLevel > 0 ? SlayerManager.XP_REQUIREMENTS[finalLevel - 1] : 0;
                        SlayerData.setSlayerXp(player.getUuidAsString(), slayerType, xp);
                        DataManager.save(PoliticalServer.server);
                        player.sendMessage(Text.literal("✔ Set " + slayerType.displayName + " Slayer to level " + finalLevel)
                                .formatted(Formatting.GREEN), false);
                        openSetLevelGui(player);
                    })
                    .build());
        }

        gui.setSlot(17, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openSetLevelGui(player))
                .build());

        gui.open();
    }

// ============================================================
// VIEW STATS GUI
// ============================================================

    private static void openViewStatsGui(ServerPlayerEntity player) {
        String uuid = player.getUuidAsString();

        player.sendMessage(Text.literal(""), false);
        player.sendMessage(Text.literal("══════════════════════════════")
                .formatted(Formatting.GOLD), false);
        player.sendMessage(Text.literal("  ☠ Your Bounty Stats ☠")
                .formatted(Formatting.YELLOW, Formatting.BOLD), false);
        player.sendMessage(Text.literal("══════════════════════════════")
                .formatted(Formatting.GOLD), false);

        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            int level = SlayerData.getSlayerLevel(uuid, type);
            int bosses = SlayerData.getBossesKilled(uuid, type);
            int highestTier = SlayerData.getHighestTier(uuid, type);
            player.sendMessage(Text.literal("  " + type.displayName + ": ")
                    .formatted(type.color)
                    .append(Text.literal("Lvl " + level).formatted(Formatting.WHITE))
                    .append(Text.literal(" | " + bosses + " kills").formatted(Formatting.GRAY))
                    .append(Text.literal(" | T" + highestTier + " max").formatted(Formatting.DARK_GRAY)), false);
        }

        player.sendMessage(Text.literal(""), false);
        player.sendMessage(Text.literal("  Total Level: " + SlayerData.getTotalSlayerLevel(uuid))
                .formatted(Formatting.AQUA), false);
        player.sendMessage(Text.literal("  Total Bosses: " + SlayerData.getTotalBossesKilled(uuid))
                .formatted(Formatting.RED), false);
        player.sendMessage(Text.literal("══════════════════════════════")
                .formatted(Formatting.GOLD), false);
    }
}