package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SignGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.*;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import java.util.List;
import java.util.ArrayList;
import java.util.List;
import eu.pb4.sgui.api.ClickType;
import java.util.Map;
import java.util.Random;
import java.util.Set;import com.political.SlayerManager;
import com.political.SlayerData;
import com.political.SlayerItems;import net.minecraft.item.ItemStack;import net.minecraft.util.math.Vec3d;import eu.pb4.sgui.api.ClickType;import net.minecraft.screen.slot.SlotActionType;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

import com.political.SlayerManager;
import com.political.SlayerData;
import com.political.SlayerItems;
import com.political.CoinManager;
import com.political.CustomItemHandler;
import com.political.PoliticalServer;import eu.pb4.sgui.api.elements.GuiElementInterface;

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
    List<ServerPlayerEntity> players = new ArrayList<>(PoliticalServer.server.getPlayerManager().getPlayerList());
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

        // Fill with colored glass borders (cyan border, gray interior)
        fillBackground(gui);
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
        gui.setSlot(30, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Text.literal("✦ Custom Items").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Give custom items to yourself").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("• Armor, swords, legendary gear").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("• Special weapons & materials").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to open").formatted(Formatting.YELLOW))
                .glow()
                .setCallback((idx, type, action) -> openCustomItemsPage(player))
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
        gui.setSlot(32, new GuiElementBuilder(Items.COMPARATOR)
                .setName(Text.literal("⚙ Game Settings").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("• Boss settings (HP, damage, kills)").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("• Item level requirements").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("• Defence settings").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("• Drop rates & mob settings").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to open").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> openGameSettingsPage(player))
                .build());

        gui.setSlot(11, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Text.literal("✦ Perk Management").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("• Activate/deactivate any perk").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("• View all active perks").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("• Reset all perks").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to open").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> openPerkManagementPage(player))
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
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("✦ Custom Items ✦"));
        fillBackground(gui);

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Text.literal("✦ Custom Items ✦").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Select a category to give items").formatted(Formatting.GRAY))
                .glow()
                .build());

        // Bounty Swords
        gui.setSlot(10, new GuiElementBuilder(Items.IRON_SWORD)
                .setName(Text.literal("⚔ Bounty Swords").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("T1 & T2 swords for all 7 types").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to open").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openAdminCustomSwords(player))
                .build());

        // T1 Armor
        gui.setSlot(11, new GuiElementBuilder(Items.LEATHER_CHESTPLATE)
                .setName(Text.literal("T1 Armor").formatted(Formatting.BLUE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("T1 armor sets for all 7 types").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to open").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openAdminArmorSelector(player, 1))
                .build());

        // T2 Armor
        gui.setSlot(12, new GuiElementBuilder(Items.DIAMOND_CHESTPLATE)
                .setName(Text.literal("T2 Armor").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("T2 armor sets for all 7 types").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to open").formatted(Formatting.YELLOW))
                .glow()
                .setCallback((idx, type, action) -> openAdminArmorSelector(player, 2))
                .build());

        // Legendary Gear
        gui.setSlot(13, new GuiElementBuilder(Items.ZOMBIE_HEAD)
                .setName(Text.literal("★ Legendary Gear").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("• Berserker Helmet").formatted(Formatting.DARK_GREEN))
                .addLoreLine(Text.literal("• Spider Leggings").formatted(Formatting.DARK_RED))
                .addLoreLine(Text.literal("• Skeleton Bow").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("• Slime Boots").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal("• Warden Chestplate").formatted(Formatting.DARK_AQUA))
                .addLoreLine(Text.literal("• Voidwalker's Crown").formatted(Formatting.DARK_PURPLE))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to open").formatted(Formatting.YELLOW))
                .glow()
                .setCallback((idx, type, action) -> openAdminLegendary(player))
                .build());

        // Special Weapons
        gui.setSlot(14, new GuiElementBuilder(Items.GOLDEN_AXE)
                .setName(Text.literal("⚡ Special Weapons").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("• Harvey's Stick").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("• The Gavel").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("• Hermes Shoes").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("• HPEBM Mk1–Mk5").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("• Ultra Overclocked Beam").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to open").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openAdminSpecialWeapons(player))
                .build());

        // Materials (Chunks & Cores)
        gui.setSlot(15, new GuiElementBuilder(Items.ECHO_SHARD)
                .setName(Text.literal("◈ Materials").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Chunks & Cores for all 7 types").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to open").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openAdminMaterials(player))
                .build());

        // Piglin Items
        gui.setSlot(16, new GuiElementBuilder(Items.GOLDEN_HELMET)
                .setName(Text.literal("👑 Piglin Items").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("• Crown of Greed").formatted(Formatting.GOLD))
                .addLoreLine(Text.literal("• Piglin Core & Flesh").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("• Piglin T1 & T2 Armor").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to open").formatted(Formatting.YELLOW))
                .glow()
                .setCallback((idx, type, action) -> openAdminPiglinItems(player))
                .build());

        // Back button
        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openPage(player, AdminPage.UNDERGROUND_AUCTION))
                .build());

        // Close button
        gui.setSlot(53, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("Close").formatted(Formatting.RED))
                .setCallback((idx, type, action) -> gui.close())
                .build());

        gui.open();
    }

    private static void openAdminCustomSwords(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("⚔ Bounty Swords"));
        fillBackground(gui);

        gui.setSlot(4, new GuiElementBuilder(Items.IRON_SWORD)
                .setName(Text.literal("⚔ Bounty Swords").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal("Click any sword to receive it").formatted(Formatting.GRAY))
                .glow()
                .build());

        SlayerManager.SlayerType[] types = SlayerManager.SlayerType.values();
        // Row 1 (slots 10–16): T1 swords
        // Row 2 (slots 19–25): T2 swords
        int[] t1Slots = {10, 11, 12, 13, 14, 15, 16};
        int[] t2Slots = {19, 20, 21, 22, 23, 24, 25};

        for (int i = 0; i < types.length && i < t1Slots.length; i++) {
            final SlayerManager.SlayerType t = types[i];
            ItemStack sword = SlayerItems.createSlayerSword(t);
            String name = sword.getName().getString();
            gui.setSlot(t1Slots[i], new GuiElementBuilder(sword.getItem())
                    .setName(Text.literal(name).formatted(Formatting.YELLOW))
                    .addLoreLine(Text.literal("T1 Sword").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("▶ Click to receive").formatted(Formatting.GREEN))
                    .setCallback((idx, type2, action) -> {
                        player.giveItemStack(SlayerItems.createSlayerSword(t));
                        player.sendMessage(Text.literal("✓ Given " + t.name() + " T1 sword!").formatted(Formatting.GREEN));
                    })
                    .build());

            ItemStack t2sword = SlayerItems.createUpgradedSlayerSword(t);
            String t2name = t2sword.getName().getString();
            gui.setSlot(t2Slots[i], new GuiElementBuilder(t2sword.getItem())
                    .setName(Text.literal(t2name).formatted(Formatting.LIGHT_PURPLE))
                    .addLoreLine(Text.literal("T2 Upgraded Sword").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("▶ Click to receive").formatted(Formatting.GREEN))
                    .glow()
                    .setCallback((idx, type2, action) -> {
                        player.giveItemStack(SlayerItems.createUpgradedSlayerSword(t));
                        player.sendMessage(Text.literal("✓ Given " + t.name() + " T2 sword!").formatted(Formatting.GREEN));
                    })
                    .build());
        }

        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type2, action) -> openCustomItemsPage(player))
                .build());
        gui.setSlot(53, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("Close").formatted(Formatting.RED))
                .setCallback((idx, type2, action) -> gui.close())
                .build());

        gui.open();
    }

    private static void openAdminArmorSelector(ServerPlayerEntity player, int tier) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("T" + tier + " Armor — Select Type"));
        fillBackground(gui);

        gui.setSlot(4, new GuiElementBuilder(tier == 1 ? Items.LEATHER_CHESTPLATE : Items.DIAMOND_CHESTPLATE)
                .setName(Text.literal("T" + tier + " Armor").formatted(tier == 1 ? Formatting.BLUE : Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal("Select a bounty type to receive full set").formatted(Formatting.GRAY))
                .build());

        SlayerManager.SlayerType[] types = SlayerManager.SlayerType.values();
        int[] slots = {10, 11, 12, 13, 14, 15, 16};
        net.minecraft.item.Item[] icons = {Items.ROTTEN_FLESH, Items.SPIDER_EYE, Items.BONE, Items.SLIME_BALL,
                Items.ENDER_PEARL, Items.ECHO_SHARD, Items.GOLD_INGOT};

        for (int i = 0; i < types.length && i < slots.length; i++) {
            final SlayerManager.SlayerType t = types[i];
            final int finalTier = tier;
            gui.setSlot(slots[i], new GuiElementBuilder(icons[i])
                    .setName(Text.literal(capitalize(t) + " T" + tier)
                            .formatted(tier == 1 ? Formatting.BLUE : Formatting.LIGHT_PURPLE, Formatting.BOLD))
                    .addLoreLine(Text.literal("Give full T" + tier + " " + t.name().toLowerCase() + " armor set").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("▶ Click to receive all 4 pieces").formatted(Formatting.GREEN))
                    .setCallback((idx, type2, action) -> {
                        player.giveItemStack(SlayerItems.createSlayerHelmet(t, finalTier));
                        player.giveItemStack(SlayerItems.createSlayerChestplate(t, finalTier));
                        player.giveItemStack(SlayerItems.createSlayerLeggings(t, finalTier));
                        player.giveItemStack(SlayerItems.createSlayerBoots(t, finalTier));
                        player.sendMessage(Text.literal("✓ Given T" + finalTier + " " + t.name().toLowerCase() + " armor set!").formatted(Formatting.GREEN));
                    })
                    .build());
        }

        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type2, action) -> openCustomItemsPage(player))
                .build());
        gui.setSlot(53, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("Close").formatted(Formatting.RED))
                .setCallback((idx, type2, action) -> gui.close())
                .build());

        gui.open();
    }

    private static void openAdminLegendary(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("★ Legendary Gear"));
        fillBackground(gui);

        gui.setSlot(4, new GuiElementBuilder(Items.ZOMBIE_HEAD)
                .setName(Text.literal("★ Legendary Gear").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal("Click any item to receive it").formatted(Formatting.GRAY))
                .glow()
                .build());

        // Zombie Berserker Helmet
        gui.setSlot(10, new GuiElementBuilder(Items.LEATHER_HELMET)
                .setName(Text.literal("Zombie Berserker Helmet").formatted(Formatting.DARK_GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal("Legendary Zombie Bounty Helmet").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("▶ Click to receive").formatted(Formatting.GREEN))
                .glow()
                .setCallback((idx, type2, action) -> {
                    player.giveItemStack(SlayerItems.createZombieBerserkerHelmet());
                    player.sendMessage(Text.literal("✓ Given Zombie Berserker Helmet!").formatted(Formatting.GREEN));
                })
                .build());

        // Spider Leggings
        gui.setSlot(11, new GuiElementBuilder(Items.LEATHER_LEGGINGS)
                .setName(Text.literal("Spider Leggings").formatted(Formatting.DARK_RED, Formatting.BOLD))
                .addLoreLine(Text.literal("Legendary Spider Bounty Leggings").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("▶ Click to receive").formatted(Formatting.GREEN))
                .glow()
                .setCallback((idx, type2, action) -> {
                    player.giveItemStack(SlayerItems.createSpiderLeggings());
                    player.sendMessage(Text.literal("✓ Given Spider Leggings!").formatted(Formatting.GREEN));
                })
                .build());

        // Skeleton Bow
        gui.setSlot(12, new GuiElementBuilder(Items.BOW)
                .setName(Text.literal("Skeleton Bow").formatted(Formatting.WHITE, Formatting.BOLD))
                .addLoreLine(Text.literal("Legendary Skeleton Bounty Bow").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("▶ Click to receive").formatted(Formatting.GREEN))
                .glow()
                .setCallback((idx, type2, action) -> {
                    player.giveItemStack(SlayerItems.createSkeletonBow());
                    player.sendMessage(Text.literal("✓ Given Skeleton Bow!").formatted(Formatting.GREEN));
                })
                .build());

        // Slime Boots
        gui.setSlot(13, new GuiElementBuilder(Items.LEATHER_BOOTS)
                .setName(Text.literal("Slime Boots").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal("Legendary Slime Bounty Boots").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("▶ Click to receive").formatted(Formatting.GREEN))
                .glow()
                .setCallback((idx, type2, action) -> {
                    player.giveItemStack(SlayerItems.createSlimeBoots());
                    player.sendMessage(Text.literal("✓ Given Slime Boots!").formatted(Formatting.GREEN));
                })
                .build());

        // Warden Chestplate
        gui.setSlot(14, new GuiElementBuilder(Items.LEATHER_CHESTPLATE)
                .setName(Text.literal("Warden Chestplate").formatted(Formatting.DARK_AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal("Legendary Warden Bounty Chestplate").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("▶ Click to receive").formatted(Formatting.GREEN))
                .glow()
                .setCallback((idx, type2, action) -> {
                    player.giveItemStack(SlayerItems.createWardenChestplate());
                    player.sendMessage(Text.literal("✓ Given Warden Chestplate!").formatted(Formatting.GREEN));
                })
                .build());

        // Voidwalker's Crown
        gui.setSlot(15, new GuiElementBuilder(Items.NETHERITE_HELMET)
                .setName(Text.literal("Voidwalker's Crown").formatted(Formatting.DARK_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal("Legendary Enderman Bounty Helmet").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("▶ Click to receive").formatted(Formatting.GREEN))
                .glow()
                .setCallback((idx, type2, action) -> {
                    player.giveItemStack(SlayerItems.createVoidwalkerCrown());
                    player.sendMessage(Text.literal("✓ Given Voidwalker's Crown!").formatted(Formatting.GREEN));
                })
                .build());

        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type2, action) -> openCustomItemsPage(player))
                .build());
        gui.setSlot(53, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("Close").formatted(Formatting.RED))
                .setCallback((idx, type2, action) -> gui.close())
                .build());

        gui.open();
    }

    private static void openAdminSpecialWeapons(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("⚡ Special Weapons"));
        fillBackground(gui);

        gui.setSlot(4, new GuiElementBuilder(Items.GOLDEN_AXE)
                .setName(Text.literal("⚡ Special Weapons").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal("Click any item to receive it").formatted(Formatting.GRAY))
                .glow()
                .build());

        // Harvey's Stick
        gui.setSlot(10, new GuiElementBuilder(Items.STICK)
                .setName(Text.literal("Harvey's Stick").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal("Summons lightning on hit").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("▶ Click to receive").formatted(Formatting.GREEN))
                .glow()
                .setCallback((idx, type2, action) -> {
                    player.giveItemStack(CustomItemHandler.createHarveysStick());
                    player.sendMessage(Text.literal("✓ Given Harvey's Stick!").formatted(Formatting.GREEN));
                })
                .build());

        // The Gavel
        gui.setSlot(11, new GuiElementBuilder(Items.MACE)
                .setName(Text.literal("The Gavel").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal("AOE explosion attack").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("▶ Click to receive").formatted(Formatting.GREEN))
                .glow()
                .setCallback((idx, type2, action) -> {
                    player.giveItemStack(CustomItemHandler.createTheGavel());
                    player.sendMessage(Text.literal("✓ Given The Gavel!").formatted(Formatting.GREEN));
                })
                .build());

        // Hermes Shoes
        gui.setSlot(12, new GuiElementBuilder(Items.IRON_BOOTS)
                .setName(Text.literal("Hermes Shoes").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal("Permanent Speed III").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("▶ Click to receive").formatted(Formatting.GREEN))
                .glow()
                .setCallback((idx, type2, action) -> {
                    player.giveItemStack(CustomItemHandler.createHermesShoes());
                    player.sendMessage(Text.literal("✓ Given Hermes Shoes!").formatted(Formatting.GREEN));
                })
                .build());

        // HPEBM Mk1–Mk5
        int[] hpebmSlots = {19, 20, 21, 22, 23};
        for (int mk = 1; mk <= 5; mk++) {
            final int finalMk = mk;
            gui.setSlot(hpebmSlots[mk - 1], new GuiElementBuilder(Items.IRON_SHOVEL)
                    .setName(Text.literal("H.P.E.B.M. Mk" + mk).formatted(Formatting.GREEN, Formatting.BOLD))
                    .addLoreLine(Text.literal("Plasma beam — Mk" + mk + " variant").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("▶ Click to receive").formatted(Formatting.GREEN))
                    .glow()
                    .setCallback((idx, type2, action) -> {
                        player.giveItemStack(CustomItemHandler.createHPEBM(finalMk));
                        player.sendMessage(Text.literal("✓ Given HPEBM Mk" + finalMk + "!").formatted(Formatting.GREEN));
                    })
                    .build());
        }

        // Ultra Overclocked Beam
        gui.setSlot(25, new GuiElementBuilder(Items.GOLDEN_SHOVEL)
                .setName(Text.literal("Ultra Overclocked Beam").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal("Ultimate beam weapon").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("▶ Click to receive").formatted(Formatting.GREEN))
                .glow()
                .setCallback((idx, type2, action) -> {
                    player.giveItemStack(CustomItemHandler.createUltraOverclockedBeam());
                    player.sendMessage(Text.literal("✓ Given Ultra Overclocked Beam!").formatted(Formatting.GREEN));
                })
                .build());

        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type2, action) -> openCustomItemsPage(player))
                .build());
        gui.setSlot(53, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("Close").formatted(Formatting.RED))
                .setCallback((idx, type2, action) -> gui.close())
                .build());

        gui.open();
    }

    private static void openAdminMaterials(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("◈ Materials"));
        fillBackground(gui);

        gui.setSlot(4, new GuiElementBuilder(Items.ECHO_SHARD)
                .setName(Text.literal("◈ Materials").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal("Chunks & Cores for all 7 bounty types").formatted(Formatting.GRAY))
                .glow()
                .build());

        SlayerManager.SlayerType[] types = SlayerManager.SlayerType.values();
        // Row 1 (slots 10–16): Chunks
        // Row 2 (slots 19–25): Cores
        int[] chunkSlots = {10, 11, 12, 13, 14, 15, 16};
        int[] coreSlots  = {19, 20, 21, 22, 23, 24, 25};

        for (int i = 0; i < types.length && i < chunkSlots.length; i++) {
            final SlayerManager.SlayerType t = types[i];
            ItemStack chunk = SlayerItems.createChunk(t);
            gui.setSlot(chunkSlots[i], new GuiElementBuilder(chunk.getItem())
                    .setName(Text.literal(capitalize(t) + " Chunk").formatted(Formatting.YELLOW))
                    .addLoreLine(Text.literal("▶ Click to receive").formatted(Formatting.GREEN))
                    .setCallback((idx, type2, action) -> {
                        player.giveItemStack(SlayerItems.createChunk(t));
                        player.sendMessage(Text.literal("✓ Given " + t.name().toLowerCase() + " chunk!").formatted(Formatting.GREEN));
                    })
                    .build());

            ItemStack core = SlayerItems.createCore(t);
            gui.setSlot(coreSlots[i], new GuiElementBuilder(core.getItem())
                    .setName(Text.literal(capitalize(t) + " Core").formatted(Formatting.AQUA))
                    .addLoreLine(Text.literal("▶ Click to receive").formatted(Formatting.GREEN))
                    .glow()
                    .setCallback((idx, type2, action) -> {
                        player.giveItemStack(SlayerItems.createCore(t));
                        player.sendMessage(Text.literal("✓ Given " + t.name().toLowerCase() + " core!").formatted(Formatting.GREEN));
                    })
                    .build());
        }

        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type2, action) -> openCustomItemsPage(player))
                .build());
        gui.setSlot(53, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("Close").formatted(Formatting.RED))
                .setCallback((idx, type2, action) -> gui.close())
                .build());

        gui.open();
    }

    private static void openAdminPiglinItems(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("👑 Piglin Items"));
        fillBackground(gui);

        gui.setSlot(4, new GuiElementBuilder(Items.GOLDEN_HELMET)
                .setName(Text.literal("👑 Piglin Items").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal("Click any item to receive it").formatted(Formatting.GRAY))
                .glow()
                .build());

        // Crown of Greed
        gui.setSlot(10, new GuiElementBuilder(Items.GOLDEN_HELMET)
                .setName(Text.literal("Crown of Greed").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal("Legendary Piglin item").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("▶ Click to receive").formatted(Formatting.GREEN))
                .glow()
                .setCallback((idx, type2, action) -> {
                    player.giveItemStack(SlayerItems.createCrownOfGreed());
                    player.sendMessage(Text.literal("✓ Given Crown of Greed!").formatted(Formatting.GREEN));
                })
                .build());

        // Piglin Core
        gui.setSlot(11, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Text.literal("Piglin Core").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal("Piglin crafting material").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("▶ Click to receive").formatted(Formatting.GREEN))
                .setCallback((idx, type2, action) -> {
                    player.giveItemStack(SlayerItems.createPiglinCore());
                    player.sendMessage(Text.literal("✓ Given Piglin Core!").formatted(Formatting.GREEN));
                })
                .build());

        // Piglin Flesh
        gui.setSlot(12, new GuiElementBuilder(Items.PORKCHOP)
                .setName(Text.literal("Piglin Flesh").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal("Piglin crafting material").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("▶ Click to receive").formatted(Formatting.GREEN))
                .setCallback((idx, type2, action) -> {
                    player.giveItemStack(SlayerItems.createPiglinFlesh());
                    player.sendMessage(Text.literal("✓ Given Piglin Flesh!").formatted(Formatting.GREEN));
                })
                .build());

        // Piglin T1 full armor set
        gui.setSlot(14, new GuiElementBuilder(Items.GOLDEN_CHESTPLATE)
                .setName(Text.literal("Piglin T1 Armor Set").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal("Give full Piglin T1 armor").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("▶ Click to receive").formatted(Formatting.GREEN))
                .setCallback((idx, type2, action) -> {
                    player.giveItemStack(SlayerItems.createSlayerHelmet(SlayerManager.SlayerType.PIGLIN, 1));
                    player.giveItemStack(SlayerItems.createSlayerChestplate(SlayerManager.SlayerType.PIGLIN, 1));
                    player.giveItemStack(SlayerItems.createSlayerLeggings(SlayerManager.SlayerType.PIGLIN, 1));
                    player.giveItemStack(SlayerItems.createSlayerBoots(SlayerManager.SlayerType.PIGLIN, 1));
                    player.sendMessage(Text.literal("✓ Given Piglin T1 armor set!").formatted(Formatting.GREEN));
                })
                .build());

        // Piglin T2 full armor set
        gui.setSlot(15, new GuiElementBuilder(Items.GOLDEN_CHESTPLATE)
                .setName(Text.literal("Piglin T2 Armor Set").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal("Give full Piglin T2 armor").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("▶ Click to receive").formatted(Formatting.GREEN))
                .glow()
                .setCallback((idx, type2, action) -> {
                    player.giveItemStack(SlayerItems.createSlayerHelmet(SlayerManager.SlayerType.PIGLIN, 2));
                    player.giveItemStack(SlayerItems.createSlayerChestplate(SlayerManager.SlayerType.PIGLIN, 2));
                    player.giveItemStack(SlayerItems.createSlayerLeggings(SlayerManager.SlayerType.PIGLIN, 2));
                    player.giveItemStack(SlayerItems.createSlayerBoots(SlayerManager.SlayerType.PIGLIN, 2));
                    player.sendMessage(Text.literal("✓ Given Piglin T2 armor set!").formatted(Formatting.GREEN));
                })
                .build());

        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type2, action) -> openCustomItemsPage(player))
                .build());
        gui.setSlot(53, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("Close").formatted(Formatting.RED))
                .setCallback((idx, type2, action) -> gui.close())
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

        // Give Coins
        gui.setSlot(14, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Text.literal("Give Coins").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Add coins to a player").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("L: +100 | R: -100 | Shift: ±1000").formatted(Formatting.DARK_GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click player to select").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> openPlayerSelector(player, "give_coins"))
                .build());

        // Set Slayer Level
        gui.setSlot(15, new GuiElementBuilder(Items.EXPERIENCE_BOTTLE)
                .setName(Text.literal("Set Slayer Level").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Adjust bounty slayer levels/XP").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to select player").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> openPlayerSelector(player, "slayer_level"))
                .build());

        // Teleport to Player
        gui.setSlot(16, new GuiElementBuilder(Items.ENDER_EYE)
                .setName(Text.literal("Teleport to Player").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Teleport yourself to a player").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to select").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> openPlayerSelector(player, "tp_to"))
                .build());

        // Row 2 — additional player management
        // Reset Bounty Data
        gui.setSlot(19, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("Reset Bounty Data").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Resets ALL slayer levels + XP").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("to 0 for a player").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to select").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> openPlayerSelector(player, "reset_bounty"))
                .build());

        // Set Exact Coins
        gui.setSlot(20, new GuiElementBuilder(Items.GOLD_BLOCK)
                .setName(Text.literal("Set Exact Coins").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Set a player's coin balance to").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("an exact amount via sign").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to select").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> openPlayerSelector(player, "set_coins"))
                .build());

        // Set Exact Credits
        gui.setSlot(21, new GuiElementBuilder(Items.EMERALD)
                .setName(Text.literal("Set Exact Credits").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Set a player's credit balance to").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("an exact amount via sign").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to select").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> openPlayerSelector(player, "set_credits"))
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

        // Auction House Toggle
        boolean auctionActive = UndergroundAuctionManager.isAuctionActive();
        gui.setSlot(13, new GuiElementBuilder(auctionActive ? Items.LIME_STAINED_GLASS_PANE : Items.GRAY_STAINED_GLASS_PANE)
                .setName(Text.literal("Underground Auction").formatted(auctionActive ? Formatting.GREEN : Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal("Status: " + (auctionActive ? "§aACTIVE" : "§cINACTIVE")).formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Next: §f" + PoliticalServer.formatTime(UndergroundAuctionManager.getTimeUntilNextAuction())).formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Click: Force start/stop auction").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> {
                    if (auctionActive) {
                        UndergroundAuctionManager.forceEndAuction();
                        player.sendMessage(Text.literal("✓ Auction forcibly ended!").formatted(Formatting.RED));
                    } else {
                        UndergroundAuctionManager.forceStartAuction(PoliticalServer.server);
                        player.sendMessage(Text.literal("✓ Auction forcibly started!").formatted(Formatting.GREEN));
                    }
                    openPage(player, AdminPage.ECONOMY);
                })
                .build());

        // Give Coins to Player
        gui.setSlot(14, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Text.literal("Give Coins to Player").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Add coins to an online player").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("▶ Click to select player").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> openPlayerSelector(player, "give_coins"))
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

        // Spawn Protection Region
        gui.setSlot(21, new GuiElementBuilder(SpawnProtectionManager.isActive() ? Items.ORANGE_TERRACOTTA : Items.WHITE_TERRACOTTA)
                .setName(Text.literal("Spawn Protection").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal("Status: " + (SpawnProtectionManager.isActive() ? "§aACTIVE" : "§cINACTIVE")).formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Min: " + SpawnProtectionManager.getMinX() + "," + SpawnProtectionManager.getMinY() + "," + SpawnProtectionManager.getMinZ()).formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Max: " + SpawnProtectionManager.getMaxX() + "," + SpawnProtectionManager.getMaxY() + "," + SpawnProtectionManager.getMaxZ()).formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Left: Set corner 1 here | Right: Set corner 2 here").formatted(Formatting.DARK_GRAY))
                .addLoreLine(Text.literal("Shift-click: Toggle active").formatted(Formatting.DARK_GRAY))
                .setCallback((index, type, action) -> {
                    if (type.shift) {
                        if (SpawnProtectionManager.isActive()) SpawnProtectionManager.clearRegion();
                        else SpawnProtectionManager.setRegion(SpawnProtectionManager.getMinX(), SpawnProtectionManager.getMinY(), SpawnProtectionManager.getMinZ(), SpawnProtectionManager.getMaxX(), SpawnProtectionManager.getMaxY(), SpawnProtectionManager.getMaxZ());
                        boolean nowActive = SpawnProtectionManager.isActive();
                        player.sendMessage(Text.literal("✓ Spawn protection " + (nowActive ? "enabled" : "disabled") + "!")
                                .formatted(nowActive ? Formatting.GREEN : Formatting.RED));
                    } else if (type.isLeft) {
                        int x = (int) player.getX(), y = (int) player.getY(), z = (int) player.getZ();
                        SpawnProtectionManager.setRegion(x, y, z, SpawnProtectionManager.getMaxX(), SpawnProtectionManager.getMaxY(), SpawnProtectionManager.getMaxZ());
                        player.sendMessage(Text.literal("✓ Corner 1 set to " + x + "," + y + "," + z + "!").formatted(Formatting.GREEN));
                    } else {
                        int x = (int) player.getX(), y = (int) player.getY(), z = (int) player.getZ();
                        SpawnProtectionManager.setRegion(SpawnProtectionManager.getMinX(), SpawnProtectionManager.getMinY(), SpawnProtectionManager.getMinZ(), x, y, z);
                        player.sendMessage(Text.literal("✓ Corner 2 set to " + x + "," + y + "," + z + "!").formatted(Formatting.GREEN));
                    }
                    openPage(player, AdminPage.WORLD);
                })
                .build());

        // Weather Override
        net.minecraft.server.world.ServerWorld overworld = PoliticalServer.server.getOverworld();
        boolean isRaining = overworld.isRaining();
        boolean isThundering = overworld.isThundering();
        gui.setSlot(22, new GuiElementBuilder(isRaining ? Items.WATER_BUCKET : Items.SUNFLOWER)
                .setName(Text.literal("Weather Control").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal("Current: " + (isThundering ? "§cThunder" : isRaining ? "§bRain" : "§eClear")).formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Left: Clear | Right: Rain | Shift: Thunder").formatted(Formatting.DARK_GRAY))
                .setCallback((index, type, action) -> {
                    if (type.shift) {
                        PoliticalServer.server.getOverworld().setWeather(0, 24000, true, true);
                        player.sendMessage(Text.literal("✓ Weather set to Thunder!").formatted(Formatting.AQUA));
                    } else if (type.isLeft) {
                        PoliticalServer.server.getOverworld().setWeather(24000, 0, false, false);
                        player.sendMessage(Text.literal("✓ Weather set to Clear!").formatted(Formatting.YELLOW));
                    } else {
                        PoliticalServer.server.getOverworld().setWeather(0, 24000, true, false);
                        player.sendMessage(Text.literal("✓ Weather set to Rain!").formatted(Formatting.AQUA));
                    }
                    openPage(player, AdminPage.WORLD);
                })
                .build());

        // Time Control
        long currentTime = overworld.getTimeOfDay() % 24000;
        gui.setSlot(23, new GuiElementBuilder(currentTime >= 13000 ? Items.ENDER_PEARL : Items.GLOWSTONE)
                .setName(Text.literal("Time Control").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal("Current time: §f" + currentTime).formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Left: Daytime (1000) | Right: Midnight (18000)").formatted(Formatting.DARK_GRAY))
                .addLoreLine(Text.literal("Shift+Left: Sunrise (23000) | Shift+Right: Sunset (13000)").formatted(Formatting.DARK_GRAY))
                .setCallback((index, type, action) -> {
                    long day = overworld.getTimeOfDay() / 24000;
                    if (type.shift && type.isLeft) {
                        overworld.setTimeOfDay(day * 24000 + 23000);
                        player.sendMessage(Text.literal("✓ Time set to sunrise!").formatted(Formatting.YELLOW));
                    } else if (type.shift) {
                        overworld.setTimeOfDay(day * 24000 + 13000);
                        player.sendMessage(Text.literal("✓ Time set to sunset!").formatted(Formatting.GOLD));
                    } else if (type.isLeft) {
                        overworld.setTimeOfDay(day * 24000 + 1000);
                        player.sendMessage(Text.literal("✓ Time set to day!").formatted(Formatting.YELLOW));
                    } else {
                        overworld.setTimeOfDay(day * 24000 + 18000);
                        player.sendMessage(Text.literal("✓ Time set to midnight!").formatted(Formatting.DARK_BLUE));
                    }
                    openPage(player, AdminPage.WORLD);
                })
                .build());

        addBackButton(gui, player);
        gui.open();
    }

    // ═══════════════════════════════════════════════════════════
    // HELPER METHODS (from old AdminGui)
    // ═══════════════════════════════════════════════════════════

    /**
     * Fills the GUI background with a cyan stained glass pane border (top row, bottom row,
     * left column, right column) and a gray stained glass pane interior.
     */
    private static void fillBackground(SimpleGui gui) {
        int size = gui.getSize();
        int cols = 9;
        int rows = size / cols;

        for (int i = 0; i < size; i++) {
            int row = i / cols;
            int col = i % cols;
            boolean isBorder = (row == 0 || row == rows - 1 || col == 0 || col == cols - 1);
            net.minecraft.item.Item pane = isBorder
                    ? Items.CYAN_STAINED_GLASS_PANE
                    : Items.GRAY_STAINED_GLASS_PANE;
            gui.setSlot(i, new GuiElementBuilder(pane)
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

    private static String capitalize(SlayerManager.SlayerType type) {
        String name = type.name();
        return Character.toString(name.charAt(0)) + name.substring(1).toLowerCase();
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
            case "give_coins" -> {
                openGiveCoinsSelector(admin, target);
                return;
            }
            case "slayer_level" -> {
                openSlayerLevelSelector(admin, target);
                return;
            }
            case "tp_to" -> {
                admin.teleport(
                    (net.minecraft.server.world.ServerWorld) target.getEntityWorld(),
                    target.getX(), target.getY(), target.getZ(),
                    java.util.Set.of(), admin.getYaw(), admin.getPitch(), false);
                admin.sendMessage(Text.literal("✓ Teleported to " + target.getName().getString() + "!").formatted(Formatting.GREEN));
                return;
            }
            case "reset_bounty" -> {
                for (SlayerManager.SlayerType t : SlayerManager.SlayerType.values()) {
                    SlayerData.setSlayerLevel(target.getUuidAsString(), t, 0);
                    SlayerData.setSlayerXp(target.getUuidAsString(), t, 0);
                }
                DataManager.save(PoliticalServer.server);
                admin.sendMessage(Text.literal("✓ Reset all bounty data for " + target.getName().getString() + "!").formatted(Formatting.RED));
                target.sendMessage(Text.literal("An admin has reset your bounty progress.").formatted(Formatting.RED));
                return;
            }
            case "set_coins" -> {
                openSetCoinsSign(admin, target);
                return;
            }
            case "set_credits" -> {
                openSetCreditsSign(admin, target);
                return;
            }
        }
        open(admin);
    }

    /**
     * Parse the first line of a sign input.
     * @param raw the raw sign line text
     * @param allowNegative if true strips everything except digits and minus sign;
     *                      if false strips everything except digits (for 'set exact' operations)
     * @return the parsed integer, or null if the input is empty or unparseable
     */
    private static Integer parseSignAmount(String raw, boolean allowNegative) {
        String cleaned = allowNegative
                ? raw.trim().replaceAll("[^0-9\\-]", "")
                : raw.trim().replaceAll("[^0-9]", "");
        if (cleaned.isEmpty()) return null;
        try {
            return Integer.parseInt(cleaned);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static void openSetCoinsSign(ServerPlayerEntity admin, ServerPlayerEntity target) {
        int current = CoinManager.getCoins(target);
        SignGui signGui = new SignGui(admin) {
            @Override
            public void onClose() {
                // 'set exact' — only accepts non-negative integers
                Integer amount = parseSignAmount(this.getLine(0).getString(), false);
                if (amount == null) { openPlayersPage(admin); return; }
                CoinManager.setCoins(target.getUuidAsString(), Math.max(0, amount));
                admin.sendMessage(Text.literal("✓ Set " + target.getName().getString() + "'s coins to " + amount + "!").formatted(Formatting.GREEN));
                openPlayersPage(admin);
            }
        };
        signGui.setLine(0, Text.literal(""));
        signGui.setLine(1, Text.literal("Set exact coins for"));
        signGui.setLine(2, Text.literal(target.getName().getString()));
        signGui.setLine(3, Text.literal("Current: " + current));
        signGui.open();
    }

    private static void openSetCreditsSign(ServerPlayerEntity admin, ServerPlayerEntity target) {
        int current = DataManager.getCredits(target.getUuidAsString());
        SignGui signGui = new SignGui(admin) {
            @Override
            public void onClose() {
                // 'set exact' — only accepts non-negative integers
                Integer amount = parseSignAmount(this.getLine(0).getString(), false);
                if (amount == null) { openPlayersPage(admin); return; }
                DataManager.setCredits(target.getUuidAsString(), Math.max(0, amount));
                DataManager.save(PoliticalServer.server);
                admin.sendMessage(Text.literal("✓ Set " + target.getName().getString() + "'s credits to " + amount + "!").formatted(Formatting.GREEN));
                openPlayersPage(admin);
            }
        };
        signGui.setLine(0, Text.literal(""));
        signGui.setLine(1, Text.literal("Set exact credits for"));
        signGui.setLine(2, Text.literal(target.getName().getString()));
        signGui.setLine(3, Text.literal("Current: " + current));
        signGui.open();
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
        int currentCredits = DataManager.getCredits(target.getUuidAsString());
        SignGui signGui = new SignGui(admin) {
            @Override
            public void onClose() {
                // allow negative to mean "remove" — e.g. "-100" removes 100 credits
                Integer amount = parseSignAmount(this.getLine(0).getString(), true);
                if (amount == null) { openCreditsGui(admin); return; }
                if (amount > 0) {
                    CreditItem.giveCredits(target, amount);
                    admin.sendMessage(Text.literal("✓ Gave " + amount + " credits to " + target.getName().getString() + "!").formatted(Formatting.GREEN));
                    target.sendMessage(Text.literal("An admin gave you " + amount + " credits!").formatted(Formatting.GREEN));
                } else if (amount < 0) {
                    CreditItem.removeCredits(target, -amount);
                    admin.sendMessage(Text.literal("✓ Removed " + (-amount) + " credits from " + target.getName().getString() + "!").formatted(Formatting.RED));
                }
                openCreditsGui(admin);
            }
        };
        signGui.setLine(0, Text.literal(""));
        signGui.setLine(1, Text.literal("+ give / - remove"));
        signGui.setLine(2, Text.literal(target.getName().getString()));
        signGui.setLine(3, Text.literal("Credits: " + currentCredits));
        signGui.open();
    }

    private static void openCreditsRemoveSelector(ServerPlayerEntity admin, ServerPlayerEntity target) {
        // Delegate to the combined sign-based selector
        openCreditsAmountSelector(admin, target);
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
                    .setName(Text.literal(type.displayName + " Bounty Sword").formatted(type.color, Formatting.BOLD))
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
                    .setName(Text.literal(type.displayName + " Bounty Sword II").formatted(type.color, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("3x damage, counts as 3 kills").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("Click to receive!").formatted(Formatting.GREEN))
                    .glow()
                    .setCallback((idx, clickType, action) -> {
                        ItemStack sword = SlayerItems.createUpgradedSlayerSword(finalType);
                        if (!player.getInventory().insertStack(sword)) {
                            player.dropItem(sword, false);
                        }
                        player.sendMessage(Text.literal("✔ Received " + finalType.displayName + " Bounty Sword II!")
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
    // ═══════════════════════════════════════════════════════════
// BOUNTY ADMIN PAGE
// ═══════════════════════════════════════════════════════════
// SLAYER/BOUNTY ADMIN GUI
// ═══════════════════════════════════════════════════════════

    public static void openSlayerAdminGui(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("⚔ Bounty Administration ⚔"));

        // Background with dark theme
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        // Decorative border
        for (int i = 0; i < 9; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.RED_STAINED_GLASS_PANE).setName(Text.literal("")).build());
            gui.setSlot(45 + i, new GuiElementBuilder(Items.RED_STAINED_GLASS_PANE).setName(Text.literal("")).build());
        }
        for (int i = 0; i < 6; i++) {
            gui.setSlot(i * 9, new GuiElementBuilder(Items.RED_STAINED_GLASS_PANE).setName(Text.literal("")).build());
            gui.setSlot(i * 9 + 8, new GuiElementBuilder(Items.RED_STAINED_GLASS_PANE).setName(Text.literal("")).build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.NETHERITE_SWORD)
                .setName(Text.literal("⚔ Bounty Admin Panel ⚔").formatted(Formatting.DARK_RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Manage all bounty items & player data").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§7Online: §a" + PoliticalServer.server.getPlayerManager().getPlayerList().size() + " players"))
                .glow()
                .build());

        // ═══ ROW 1: GIVE ITEMS ═══
        // Section label
        gui.setSlot(10, new GuiElementBuilder(Items.CHEST)
                .setName(Text.literal("📦 GIVE ITEMS").formatted(Formatting.YELLOW, Formatting.BOLD))
                .glow()
                .build());

        // T1 Armor
        gui.setSlot(11, new GuiElementBuilder(Items.LEATHER_CHESTPLATE)
                .setName(Text.literal("T1 Hunter Armor").formatted(Formatting.BLUE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Level " + SlayerItems.T1_ARMOR_LEVEL_REQ + " requirement").formatted(Formatting.DARK_GRAY))
                .addLoreLine(Text.literal("Click to give →").formatted(Formatting.GREEN))
                .setCallback((idx, type, action) -> openArmorGiveGui(player, 1))
                .build());

        // T2 Armor
        gui.setSlot(12, new GuiElementBuilder(Items.NETHERITE_CHESTPLATE)
                .setName(Text.literal("T2 Bounty II Armor").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Level " + SlayerItems.T2_ARMOR_LEVEL_REQ + " requirement").formatted(Formatting.DARK_GRAY))
                .addLoreLine(Text.literal("Click to give →").formatted(Formatting.GREEN))
                .setCallback((idx, type, action) -> openArmorGiveGui(player, 2))
                .glow()
                .build());

        // Legendary Items
        gui.setSlot(13, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Text.literal("★ Legendary Items").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("• Zombie Berserker Helmet").formatted(Formatting.DARK_GREEN))
                .addLoreLine(Text.literal("• Venomous Crawler Leggings").formatted(Formatting.DARK_RED))
                .addLoreLine(Text.literal("• Bone Desperado's Bow").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("• Gelatinous Rustler Boots").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal("• Sculk Terror Chestplate").formatted(Formatting.DARK_AQUA))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to give →").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openSpecialItemsGui(player))
                .glow()
                .build());

        // Bounty Swords
        gui.setSlot(14, new GuiElementBuilder(Items.DIAMOND_SWORD)
                .setName(Text.literal("⚔ Bounty Swords").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("• T1 Swords (2x damage)").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("• T2 Swords II (3x damage)").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to give →").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openSwordsGiveGui(player))
                .build());

        // Cores
        gui.setSlot(15, new GuiElementBuilder(Items.SCULK)
                .setName(Text.literal("✦ Bounty Cores").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Crafting materials for all types").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to give →").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openCoresGiveGui(player))
                .build());

        // Piglin Items
        gui.setSlot(16, new GuiElementBuilder(Items.GOLDEN_HELMET)
                .setName(Text.literal("👑 Piglin Items").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("• Crown of Greed").formatted(Formatting.GOLD))
                .addLoreLine(Text.literal("• Piglin Core").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to give →").formatted(Formatting.YELLOW))
                .glow()
                .setCallback((idx, type, action) -> openPiglinItemsGui(player))
                .build());

        // Other Weapons
        gui.setSlot(17, new GuiElementBuilder(Items.GOLDEN_AXE)
                .setName(Text.literal("⚒ Other Weapons").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("• The Gavel • Hermes Shoes").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("• Harvey's Stick • HPEBM").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to give →").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openOtherWeaponsGui(player))
                .build());

        // ═══ ROW 2: PLAYER MANAGEMENT ═══
        // Section label
        gui.setSlot(19, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Text.literal("👤 PLAYER MANAGEMENT").formatted(Formatting.AQUA, Formatting.BOLD))
                .glow()
                .build());

        // Set Level
        gui.setSlot(20, new GuiElementBuilder(Items.EXPERIENCE_BOTTLE)
                .setName(Text.literal("Set Bounty Level").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Set any player's bounty level").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("for any bounty type").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to manage →").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openSetLevelGui(player))
                .build());

        // Set XP
        gui.setSlot(21, new GuiElementBuilder(Items.ENCHANTING_TABLE)
                .setName(Text.literal("Add Bounty XP").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Add XP to any player's bounty").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to manage →").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openSetXpGui(player))
                .build());

        // View Stats
        gui.setSlot(22, new GuiElementBuilder(Items.WRITTEN_BOOK)
                .setName(Text.literal("View Player Stats").formatted(Formatting.WHITE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("View detailed bounty stats").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to view →").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openViewStatsGui(player))
                .build());

        // Cancel Quest
        gui.setSlot(23, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("Cancel Player Quest").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Force cancel a player's").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("active bounty quest").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to manage →").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openCancelQuestGui(player))
                .build());

        // Reset Data
        gui.setSlot(24, new GuiElementBuilder(Items.TNT)
                .setName(Text.literal("⚠ Reset Player Data").formatted(Formatting.DARK_RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Reset ALL bounty progress").formatted(Formatting.RED))
                .addLoreLine(Text.literal("for a specific player").formatted(Formatting.RED))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("⚠ DESTRUCTIVE").formatted(Formatting.DARK_RED))
                .setCallback((idx, type, action) -> openResetDataGui(player))
                .build());

        // ═══ ROW 3: BOSS MANAGEMENT ═══
        // Section label
        gui.setSlot(28, new GuiElementBuilder(Items.WITHER_SKELETON_SKULL)
                .setName(Text.literal("💀 BOSS MANAGEMENT").formatted(Formatting.RED, Formatting.BOLD))
                .glow()
                .build());

        // Force Spawn boss
        gui.setSlot(29, new GuiElementBuilder(Items.IRON_GOLEM_SPAWN_EGG)
                .setName(Text.literal("⚔ Force Spawn Boss").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Force spawn a bounty boss").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("for a player's active quest").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to select player →").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openForceSpawnBossGui(player))
                .build());

        // Kill all bosses
        gui.setSlot(30, new GuiElementBuilder(Items.DIAMOND_SWORD)
                .setName(Text.literal("☠ Kill All Bosses").formatted(Formatting.DARK_RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Kill all active bounty bosses").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to execute →").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> {
                    int killed = SlayerManager.killAllBosses();
                    player.sendMessage(Text.literal("✓ Killed " + killed + " active bounty bosses").formatted(Formatting.GREEN), false);
                })
                .build());

        // List active bosses
        gui.setSlot(31, new GuiElementBuilder(Items.SPYGLASS)
                .setName(Text.literal("👁 List Active Bosses").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("View all currently spawned").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("bounty bosses and their owners").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to view →").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openActiveBossesGui(player))
                .build());

        // ═══ ROW 4: QUICK ACTIONS ═══
        // Section label
        gui.setSlot(37, new GuiElementBuilder(Items.EMERALD)
                .setName(Text.literal("⚡ QUICK ACTIONS").formatted(Formatting.GREEN, Formatting.BOLD))
                .glow()
                .build());

        // Give all items to self
        gui.setSlot(38, new GuiElementBuilder(Items.CHEST_MINECART)
                .setName(Text.literal("Give All Items (Self)").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Give yourself one of each").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("bounty item for testing").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to receive →").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openQuickGiveGui(player))
                .build());

        // Announce to all
        gui.setSlot(39, new GuiElementBuilder(Items.BELL)
                .setName(Text.literal("🔔 Announce Bounty Event").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Send a server-wide bounty").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("announcement message").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to announce →").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openAnnounceGui(player))
                .build());

        // Reload config
        gui.setSlot(40, new GuiElementBuilder(Items.STRUCTURE_BLOCK)
                .setName(Text.literal("🔄 Reload Bounty Config").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Reload bounty configuration").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to reload →").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> {
                    DataManager.save(PoliticalServer.server);
                    player.sendMessage(Text.literal("✓ Bounty configuration reloaded!").formatted(Formatting.GREEN), false);
                })
                .build());

        // Back button
        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to Admin Panel").formatted(Formatting.GRAY))
                .setCallback((idx, type, action) -> openMainPage(player))
                .build());

        gui.open();
    }

    private static void openActiveBossesGui(ServerPlayerEntity admin) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, admin, false);
        gui.setTitle(Text.literal("👁 Active Bounty Bosses"));

        fillBackground(gui);

        int slot = 0;
        for (Map.Entry<java.util.UUID, String> entry : SlayerManager.getActiveBosses().entrySet()) {
            if (slot >= 18) break;

            java.util.UUID bossUuid = entry.getKey();
            String ownerUuid = entry.getValue();
            
            // Find boss entity
            net.minecraft.entity.Entity boss = null;
            for (net.minecraft.server.world.ServerWorld world : PoliticalServer.server.getWorlds()) {
                net.minecraft.entity.Entity e = world.getEntity(bossUuid);
                if (e != null) {
                    boss = e;
                    break;
                }
            }

            if (boss != null) {
                ServerPlayerEntity owner = PoliticalServer.server.getPlayerManager().getPlayer(java.util.UUID.fromString(ownerUuid));
                String ownerName = owner != null ? owner.getName().getString() : "Unknown";

                gui.setSlot(slot, new GuiElementBuilder(Items.PLAYER_HEAD)
                        .setSkullOwner(owner != null ? owner.getGameProfile() : null, PoliticalServer.server)
                        .setName(Text.literal(ownerName).formatted(Formatting.YELLOW))
                        .addLoreLine(Text.literal(""))
                        .addLoreLine(Text.literal("Boss: " + boss.getName().getString()).formatted(Formatting.RED))
                        .addLoreLine(Text.literal("Health: " + String.format("%.0f", ((net.minecraft.entity.LivingEntity)boss).getHealth()) + "/" + String.format("%.0f", ((net.minecraft.entity.LivingEntity)boss).getMaxHealth())).formatted(Formatting.GREEN))
                        .addLoreLine(Text.literal("Location: " + boss.getBlockPos().toShortString()).formatted(Formatting.GRAY))
                        .build());
                slot++;
            }
        }

        if (slot == 0) {
            gui.setSlot(13, new GuiElementBuilder(Items.BARRIER)
                    .setName(Text.literal("No active bosses").formatted(Formatting.GRAY))
                    .build());
        }

        gui.setSlot(26, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.GRAY))
                .setCallback((idx, type, action) -> openSlayerAdminGui(admin))
                .build());

        gui.open();
    }

    private static void openQuickGiveGui(ServerPlayerEntity admin) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, admin, false);
        gui.setTitle(Text.literal("Quick Give - Select Type"));

        fillBackground(gui);

        gui.setSlot(10, new GuiElementBuilder(Items.NETHERITE_CHESTPLATE)
                .setName(Text.literal("All T1 Armor Sets").formatted(Formatting.BLUE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Give all T1 armor pieces").formatted(Formatting.GRAY))
                .setCallback((idx, type, action) -> {
                    for (SlayerManager.SlayerType t : SlayerManager.SlayerType.values()) {
                        for (String piece : new String[]{"Helmet", "Chestplate", "Leggings", "Boots"}) {
                            admin.getInventory().insertStack(SlayerItems.createT1Armor(t, piece));
                        }
                    }
                    admin.sendMessage(Text.literal("✓ Gave all T1 armor sets!").formatted(Formatting.GREEN), false);
                })
                .build());

        gui.setSlot(11, new GuiElementBuilder(Items.DIAMOND_CHESTPLATE)
                .setName(Text.literal("All T2 Armor Sets").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Give all T2 armor pieces").formatted(Formatting.GRAY))
                .glow()
                .setCallback((idx, type, action) -> {
                    for (SlayerManager.SlayerType t : SlayerManager.SlayerType.values()) {
                        for (String piece : new String[]{"Helmet", "Chestplate", "Leggings", "Boots"}) {
                            admin.getInventory().insertStack(SlayerItems.createT2Armor(t, piece));
                        }
                    }
                    admin.sendMessage(Text.literal("✓ Gave all T2 armor sets!").formatted(Formatting.GREEN), false);
                })
                .build());

        gui.setSlot(12, new GuiElementBuilder(Items.DIAMOND_SWORD)
                .setName(Text.literal("All T1 Swords").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Give all T1 bounty swords").formatted(Formatting.GRAY))
                .setCallback((idx, type, action) -> {
                    for (SlayerManager.SlayerType t : SlayerManager.SlayerType.values()) {
                        SlayerItems.giveSlayerSword(admin, t);
                    }
                    admin.sendMessage(Text.literal("✓ Gave all T1 swords!").formatted(Formatting.GREEN), false);
                })
                .build());

        gui.setSlot(13, new GuiElementBuilder(Items.NETHERITE_SWORD)
                .setName(Text.literal("All T2 Swords").formatted(Formatting.DARK_RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Give all T2 bounty swords").formatted(Formatting.GRAY))
                .glow()
                .setCallback((idx, type, action) -> {
                    for (SlayerManager.SlayerType t : SlayerManager.SlayerType.values()) {
                        ItemStack sword = SlayerItems.createUpgradedSlayerSword(t);
                        admin.getInventory().insertStack(sword);
                    }
                    admin.sendMessage(Text.literal("✓ Gave all T2 swords!").formatted(Formatting.GREEN), false);
                })
                .build());

        gui.setSlot(14, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Text.literal("All Cores").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Give one of each core").formatted(Formatting.GRAY))
                .setCallback((idx, type, action) -> {
                    for (SlayerManager.SlayerType t : SlayerManager.SlayerType.values()) {
                        admin.getInventory().insertStack(SlayerItems.createCore(t));
                    }
                    admin.sendMessage(Text.literal("✓ Gave all cores!").formatted(Formatting.GREEN), false);
                })
                .build());

        gui.setSlot(15, new GuiElementBuilder(Items.CHEST)
                .setName(Text.literal("Everything!").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Give ALL bounty items").formatted(Formatting.GRAY))
                .glow()
                .setCallback((idx, type, action) -> {
                    // All armor
                    for (SlayerManager.SlayerType t : SlayerManager.SlayerType.values()) {
                        for (String piece : new String[]{"Helmet", "Chestplate", "Leggings", "Boots"}) {
                            admin.getInventory().insertStack(SlayerItems.createT1Armor(t, piece));
                            admin.getInventory().insertStack(SlayerItems.createT2Armor(t, piece));
                        }
                        admin.getInventory().insertStack(SlayerItems.createCore(t));
                        SlayerItems.giveSlayerSword(admin, t);
                        admin.getInventory().insertStack(SlayerItems.createUpgradedSlayerSword(t));
                    }
                    admin.sendMessage(Text.literal("✓ Gave EVERYTHING!").formatted(Formatting.GOLD), false);
                })
                .build());

        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.GRAY))
                .setCallback((idx, type, action) -> openSlayerAdminGui(admin))
                .build());

        gui.open();
    }

    private static void openAnnounceGui(ServerPlayerEntity admin) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, admin, false);
        gui.setTitle(Text.literal("🔔 Bounty Announcement"));

        fillBackground(gui);

        gui.setSlot(10, new GuiElementBuilder(Items.ZOMBIE_HEAD)
                .setName(Text.literal("Zombie Bounty Event").formatted(Formatting.DARK_GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Announce a zombie bounty event").formatted(Formatting.GRAY))
                .setCallback((idx, type, action) -> {
                    announceBountyEvent("ZOMBIE", "The Undying Outlaw has appeared!");
                    admin.sendMessage(Text.literal("✓ Announcement sent!").formatted(Formatting.GREEN), false);
                })
                .build());

        gui.setSlot(11, new GuiElementBuilder(Items.SPIDER_EYE)
                .setName(Text.literal("Spider Bounty Event").formatted(Formatting.DARK_RED, Formatting.BOLD))
                .setCallback((idx, type, action) -> {
                    announceBountyEvent("SPIDER", "The Venomous Bandit lurks in the shadows!");
                    admin.sendMessage(Text.literal("✓ Announcement sent!").formatted(Formatting.GREEN), false);
                })
                .build());

        gui.setSlot(12, new GuiElementBuilder(Items.BONE)
                .setName(Text.literal("Skeleton Bounty Event").formatted(Formatting.WHITE, Formatting.BOLD))
                .setCallback((idx, type, action) -> {
                    announceBountyEvent("SKELETON", "The Bone Desperado challenges all!");
                    admin.sendMessage(Text.literal("✓ Announcement sent!").formatted(Formatting.GREEN), false);
                })
                .build());

        gui.setSlot(13, new GuiElementBuilder(Items.SCULK)
                .setName(Text.literal("Warden Bounty Event").formatted(Formatting.DARK_AQUA, Formatting.BOLD))
                .glow()
                .setCallback((idx, type, action) -> {
                    announceBountyEvent("IRON_GOLEM", "The Sculk Terror emerges from the deep!");
                    admin.sendMessage(Text.literal("✓ Announcement sent!").formatted(Formatting.GREEN), false);
                })
                .build());

        gui.setSlot(14, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Text.literal("Piglin Bounty Event").formatted(Formatting.GOLD, Formatting.BOLD))
                .setCallback((idx, type, action) -> {
                    announceBountyEvent("PIGLIN", "The Gilded Ravager demands tribute!");
                    admin.sendMessage(Text.literal("✓ Announcement sent!").formatted(Formatting.GREEN), false);
                })
                .build());

        gui.setSlot(16, new GuiElementBuilder(Items.BELL)
                .setName(Text.literal("General Bounty Alert").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Send a generic bounty alert").formatted(Formatting.GRAY))
                .glow()
                .setCallback((idx, type, action) -> {
                    for (ServerPlayerEntity p : PoliticalServer.server.getPlayerManager().getPlayerList()) {
                        p.sendMessage(Text.literal(""), false);
                        p.sendMessage(Text.literal("══════════════════════════════════").formatted(Formatting.GOLD), false);
                        p.sendMessage(Text.literal("  ⚔ BOUNTY HUNTERS NEEDED! ⚔").formatted(Formatting.RED, Formatting.BOLD), false);
                        p.sendMessage(Text.literal("══════════════════════════════════").formatted(Formatting.GOLD), false);
                        p.sendMessage(Text.literal(""), false);
                    }
                    admin.sendMessage(Text.literal("✓ Announcement sent!").formatted(Formatting.GREEN), false);
                })
                .build());

        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.GRAY))
                .setCallback((idx, type, action) -> openSlayerAdminGui(admin))
                .build());

        gui.open();
    }

    private static void announceBountyEvent(String type, String message) {
        SlayerManager.SlayerType slayerType = SlayerManager.SlayerType.fromString(type);
        if (slayerType == null) return;

        for (ServerPlayerEntity p : PoliticalServer.server.getPlayerManager().getPlayerList()) {
            p.sendMessage(Text.literal(""), false);
            p.sendMessage(Text.literal("══════════════════════════════════").formatted(Formatting.GOLD), false);
            p.sendMessage(Text.literal("  ⚠ BOUNTY ALERT! ⚠").formatted(slayerType.color, Formatting.BOLD), false);
            p.sendMessage(Text.literal(""), false);
            p.sendMessage(Text.literal("  " + message).formatted(slayerType.color), false);
            p.sendMessage(Text.literal(""), false);
            p.sendMessage(Text.literal("  Use /bounty to hunt " + slayerType.displayName + "s!").formatted(Formatting.GRAY), false);
            p.sendMessage(Text.literal("══════════════════════════════════").formatted(Formatting.GOLD), false);
            p.sendMessage(Text.literal(""), false);
        }
    }
    private static void openForceSpawnBossGui(ServerPlayerEntity admin) {
        List<ServerPlayerEntity> players = new ArrayList<>(PoliticalServer.server.getPlayerManager().getPlayerList());

        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, admin, false);
        gui.setTitle(Text.literal("Force Spawn Boss - Select Player"));

        fillBackground(gui);

        int slot = 0;
        for (ServerPlayerEntity target : players) {
            if (slot >= 18) break;

            SlayerManager.ActiveQuest quest = SlayerManager.getActiveQuest(target);
            boolean hasQuest = quest != null && !quest.bossSpawned;

            GuiElementBuilder builder = new GuiElementBuilder(Items.PLAYER_HEAD)
                    .setSkullOwner(target.getGameProfile(), PoliticalServer.server)
                    .setName(Text.literal(target.getName().getString())
                            .formatted(hasQuest ? Formatting.GREEN : Formatting.GRAY));

            if (hasQuest) {
                builder.addLoreLine(Text.literal(""));
                builder.addLoreLine(Text.literal("Quest: " + quest.slayerType.displayName + " T" + quest.tier)
                        .formatted(Formatting.YELLOW));
                builder.addLoreLine(Text.literal("Kills: " + quest.killCount + "/" + quest.getKillsRequired())
                        .formatted(Formatting.GRAY));
                builder.addLoreLine(Text.literal(""));
                builder.addLoreLine(Text.literal("Click to force spawn boss").formatted(Formatting.GREEN));
            } else {
                builder.addLoreLine(Text.literal(""));
                builder.addLoreLine(Text.literal("No active quest or boss already spawned")
                        .formatted(Formatting.RED));
            }

            final ServerPlayerEntity finalTarget = target;
            builder.setCallback((idx, clickType, clickAction) -> {
                SlayerManager.ActiveQuest targetQuest = SlayerManager.getActiveQuest(finalTarget);
                if (targetQuest != null && !targetQuest.bossSpawned) {
                    // Force the kill count to required amount and spawn
                    targetQuest.killCount = targetQuest.getKillsRequired();
                    SlayerManager.checkBossSpawn(finalTarget);
                    admin.sendMessage(Text.literal("✓ Force spawned " + targetQuest.slayerType.bossName +
                            " for " + finalTarget.getName().getString()).formatted(Formatting.GREEN), false);
                    gui.close();
                } else {
                    admin.sendMessage(Text.literal("✗ Cannot spawn boss for this player").formatted(Formatting.RED), true);
                }
            });

            gui.setSlot(slot, builder.build());
            slot++;
        }

        // Back button
        gui.setSlot(26, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("Back").formatted(Formatting.GRAY))
                .setCallback((idx, type, action) -> openSlayerAdminGui(admin))
                .build());

        gui.open();
    }
// ═══════════════════════════════════════════════════════════
// ARMOR GIVE GUI (T1 or T2)
// ═══════════════════════════════════════════════════════════

    private static void openArmorGiveGui(ServerPlayerEntity player, int tier) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        String tierName = tier == 1 ? "T1 Hunter" : "T2 Bounty II";
        gui.setTitle(Text.literal("Give " + tierName + " Armor"));

        // Background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.LEATHER_CHESTPLATE)
                .setName(Text.literal("Select Bounty Type").formatted(tier == 1 ? Formatting.BLUE : Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Choose a bounty type, then armor piece").formatted(Formatting.GRAY))
                .build());

        // Slayer type buttons
        SlayerManager.SlayerType[] types = {
                SlayerManager.SlayerType.ZOMBIE,
                SlayerManager.SlayerType.SPIDER,
                SlayerManager.SlayerType.SKELETON,
                SlayerManager.SlayerType.SLIME,
                SlayerManager.SlayerType.ENDERMAN,
                SlayerManager.SlayerType.IRON_GOLEM,
                SlayerManager.SlayerType.PIGLIN
        };

        int[] slots = {19, 20, 21, 22, 23, 24, 25};

        for (int i = 0; i < types.length; i++) {
            SlayerManager.SlayerType type = types[i];
            final int finalTier = tier;

            gui.setSlot(slots[i], new GuiElementBuilder(type.icon)
                    .setName(Text.literal(type.displayName + " Armor").formatted(type.color, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Click to select armor piece").formatted(Formatting.GRAY))
                    .setCallback((idx, clickType, action) -> openArmorPieceSelectGui(player, type, finalTier))
                    .build());
        }

        // Back button
        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.GRAY))
                .setCallback((idx, type, action) -> openSlayerAdminGui(player))
                .build());

        gui.open();
    }

    private static Item getPieceItem(String piece) {
        return switch (piece) {
            case "Helmet" -> Items.NETHERITE_HELMET;
            case "Chestplate" -> Items.NETHERITE_CHESTPLATE;
            case "Leggings" -> Items.NETHERITE_LEGGINGS;
            case "Boots" -> Items.NETHERITE_BOOTS;
            default -> Items.BARRIER;
        };
    }

    private static void openArmorPieceSelectGui(ServerPlayerEntity player, SlayerManager.SlayerType selectedType, int tier) {
        final SlayerManager.SlayerType finalType = selectedType;
        final int finalTier = tier;

        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
        String tierName = tier == 1 ? "Hunter" : "Bounty II";
        gui.setTitle(Text.literal("Give " + finalType.displayName + " " + tierName + " Armor"));

        // Background
        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        // Armor piece buttons - using arrays instead of enum
        String[] pieceNames = {"Helmet", "Chestplate", "Leggings", "Boots"};
        Item[] pieceItems = {Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS};
        int[] slots = {10, 11, 12, 13};

        for (int i = 0; i < pieceNames.length; i++) {
            final String pieceName = pieceNames[i];
            final Item pieceItem = pieceItems[i];

            gui.setSlot(slots[i], new GuiElementBuilder(pieceItem)
                    .setName(Text.literal(finalType.displayName + " " + tierName + " " + pieceName)
                            .formatted(finalType.color, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Click to receive").formatted(Formatting.GREEN))
                    .setCallback((idx, clickType, action) -> {
                        ItemStack armor = finalTier == 1
                                ? SlayerItems.createT1Armor(finalType, pieceName)
                                : SlayerItems.createT2Armor(finalType, pieceName);
                        player.getInventory().insertStack(armor);
                        player.sendMessage(Text.literal("✓ Gave " + finalType.displayName + " " + tierName + " " + pieceName)
                                .formatted(Formatting.GREEN), false);
                    })
                    .build());
        }

        // Give Full Set button
        gui.setSlot(16, new GuiElementBuilder(Items.CHEST)
                .setName(Text.literal("Give Full Set").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Give all 4 armor pieces").formatted(Formatting.YELLOW))
                .setCallback((idx, clickType, action) -> {
                    for (String pieceName : pieceNames) {
                        ItemStack armor = finalTier == 1
                                ? SlayerItems.createT1Armor(finalType, pieceName)
                                : SlayerItems.createT2Armor(finalType, pieceName);
                        player.getInventory().insertStack(armor);
                    }
                    player.sendMessage(Text.literal("✓ Gave full " + finalType.displayName + " " + tierName + " armor set!")
                            .formatted(Formatting.GREEN), false);
                })
                .build());

        // Back button
        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.GRAY))
                .setCallback((idx, clickType, action) -> openArmorGiveGui(player, finalTier))
                .build());

        gui.open();
    }
    // ═══════════════════════════════════════════════════════════
// SWORDS GIVE GUI
// ═══════════════════════════════════════════════════════════

    private static void openSwordsGiveGui(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("⚔ Give Bounty Swords ⚔"));

        // Background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.IRON_SWORD)
                .setName(Text.literal("⚔ Bounty Swords ⚔").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("T1 = Iron Sword (2x damage, Lvl 2)").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("T2 = Diamond Sword (3x damage, Lvl 6)").formatted(Formatting.LIGHT_PURPLE))
                .build());

        // T1 Swords Row
        gui.setSlot(18, new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal("T1 Bounty Swords").formatted(Formatting.BLUE, Formatting.BOLD))
                .build());

        SlayerManager.SlayerType[] types = SlayerManager.SlayerType.values();
        int[] t1Slots = {19, 20, 21, 22, 23, 24, 25};

        for (int i = 0; i < types.length && i < t1Slots.length; i++) {
            SlayerManager.SlayerType type = types[i];

            gui.setSlot(t1Slots[i], new GuiElementBuilder(Items.IRON_SWORD)
                    .setName(Text.literal(type.displayName + " Bounty Sword").formatted(type.color, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("2x damage to " + type.displayName + "s").formatted(Formatting.RED))
                    .addLoreLine(Text.literal("Requires Level 2").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Click to receive").formatted(Formatting.GREEN))
                    .setCallback((idx, clickType, action) -> {
                        player.getInventory().insertStack(SlayerItems.createSlayerSword(type));
                        player.sendMessage(Text.literal("✓ Gave " + type.displayName + " Bounty Sword")
                                .formatted(Formatting.GREEN), false);
                    })
                    .build());
        }

        // T2 Swords Row
        gui.setSlot(27, new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal("T2 Bounty Swords II").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .glow()
                .build());

        int[] t2Slots = {28, 29, 30, 31, 32, 33, 34};

        for (int i = 0; i < types.length && i < t2Slots.length; i++) {
            SlayerManager.SlayerType type = types[i];

            gui.setSlot(t2Slots[i], new GuiElementBuilder(Items.DIAMOND_SWORD)
                    .setName(Text.literal(type.displayName + " Bounty Sword II").formatted(type.color, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("3x damage to " + type.displayName + "s").formatted(Formatting.RED))
                    .addLoreLine(Text.literal("Requires Level 6").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Click to receive").formatted(Formatting.GREEN))
                    .setCallback((idx, clickType, action) -> {
                        player.getInventory().insertStack(SlayerItems.createUpgradedSlayerSword(type));
                        player.sendMessage(Text.literal("✓ Gave " + type.displayName + " Bounty Sword II")
                                .formatted(Formatting.GREEN), false);
                    })
                    .glow()
                    .build());
        }

        // Give All T1 Swords
        gui.setSlot(37, new GuiElementBuilder(Items.CHEST)
                .setName(Text.literal("Give All T1 Swords").formatted(Formatting.BLUE, Formatting.BOLD))
                .setCallback((idx, clickType, action) -> {
                    for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
                        player.getInventory().insertStack(SlayerItems.createSlayerSword(type));
                    }
                    player.sendMessage(Text.literal("✓ Gave all T1 Bounty Swords").formatted(Formatting.GREEN), false);
                })
                .build());

        // Give All T2 Swords
        gui.setSlot(43, new GuiElementBuilder(Items.ENDER_CHEST)
                .setName(Text.literal("Give All T2 Swords").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .setCallback((idx, clickType, action) -> {
                    for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
                        player.getInventory().insertStack(SlayerItems.createUpgradedSlayerSword(type));
                    }
                    player.sendMessage(Text.literal("✓ Gave all T2 Bounty Swords II").formatted(Formatting.GREEN), false);
                })
                .glow()
                .build());

        // Back button
        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.GRAY))
                .setCallback((idx, type, action) -> openSlayerAdminGui(player))
                .build());

        gui.open();
    }
    // ═══════════════════════════════════════════════════════════
// SPECIAL/LEGENDARY ITEMS GUI
// ═══════════════════════════════════════════════════════════

    private static void openSpecialItemsGui(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
        gui.setTitle(Text.literal("★ Legendary Bounty Items ★"));

        // Background
        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        // Zombie Berserker Helmet
        gui.setSlot(10, new GuiElementBuilder(Items.ZOMBIE_HEAD)
                .setName(Text.literal("☠ Zombie Berserker Helmet").formatted(Formatting.DARK_GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("-50% Max Health").formatted(Formatting.RED))
                .addLoreLine(Text.literal("+300% Damage").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal("Requires Zombie Lvl 12").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to receive").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> {
                    player.getInventory().insertStack(SlayerItems.createZombieBerserkerHelmet());
                    player.sendMessage(Text.literal("✓ Gave Zombie Berserker Helmet").formatted(Formatting.GREEN), false);
                })
                .glow()
                .build());

        // Spider Leggings (Venomous Crawler)
        gui.setSlot(11, new GuiElementBuilder(Items.LEATHER_LEGGINGS)
                .setName(Text.literal("🕷 Venomous Crawler Leggings").formatted(Formatting.DARK_RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Poison Immunity").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal("Wall Climbing").formatted(Formatting.AQUA))
                .addLoreLine(Text.literal("Requires Spider Lvl 5").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to receive").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> {
                    player.getInventory().insertStack(SlayerItems.createSpiderLeggings());
                    player.sendMessage(Text.literal("✓ Gave Venomous Crawler Leggings").formatted(Formatting.GREEN), false);
                })
                .glow()
                .build());

        // Skeleton Bow
        gui.setSlot(12, new GuiElementBuilder(Items.BOW)
                .setName(Text.literal("🏹 Bone Desperado's Longbow").formatted(Formatting.WHITE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("2x damage to Skeletons").formatted(Formatting.RED))
                .addLoreLine(Text.literal("Infinite Arrows").formatted(Formatting.AQUA))
                .addLoreLine(Text.literal("Requires Skeleton Lvl 5").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to receive").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> {
                    player.getInventory().insertStack(SlayerItems.createSkeletonBow());
                    player.sendMessage(Text.literal("✓ Gave Bone Desperado's Longbow").formatted(Formatting.GREEN), false);
                })
                .glow()
                .build());

        // Slime Boots
        gui.setSlot(14, new GuiElementBuilder(Items.LEATHER_BOOTS)
                .setName(Text.literal("🥾 Gelatinous Rustler Boots").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("No Fall Damage").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal("Double Jump").formatted(Formatting.AQUA))
                .addLoreLine(Text.literal("Requires Slime Lvl 5").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to receive").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> {
                    player.getInventory().insertStack(SlayerItems.createSlimeBoots());
                    player.sendMessage(Text.literal("✓ Gave Gelatinous Rustler Boots").formatted(Formatting.GREEN), false);
                })
                .glow()
                .build());

        // Warden Chestplate
        gui.setSlot(16, new GuiElementBuilder(Items.NETHERITE_CHESTPLATE)
                .setName(Text.literal("🛡 Sculk Terror Chestplate").formatted(Formatting.DARK_AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Darkness Immunity").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal("Sonic Boom Resistance").formatted(Formatting.AQUA))
                .addLoreLine(Text.literal("Requires Warden Lvl 10").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to receive").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> {
                    player.getInventory().insertStack(SlayerItems.createWardenChestplate());
                    player.sendMessage(Text.literal("✓ Gave Sculk Terror Chestplate").formatted(Formatting.GREEN), false);
                })
                .glow()
                .build());

        // Ender Phase Helmet
        gui.setSlot(13, new GuiElementBuilder(Items.NETHERITE_HELMET)
                .setName(Text.literal("⟡ Ender Phase Helmet").formatted(Formatting.DARK_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Ender Warp (Right-click teleport)").formatted(Formatting.LIGHT_PURPLE))
                .addLoreLine(Text.literal("Ender Phase Mode (Sneak+Right-click)").formatted(Formatting.DARK_PURPLE))
                .addLoreLine(Text.literal("Requires Enderman Lvl 7").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to receive").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> {
                    player.getInventory().insertStack(SlayerItems.createEnderPhaseHelmet());
                    player.sendMessage(Text.literal("✓ Gave Ender Phase Helmet").formatted(Formatting.GREEN), false);
                })
                .glow()
                .build());

        // Back button
        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.GRAY))
                .setCallback((idx, type, action) -> openSlayerAdminGui(player))
                .build());

        gui.open();
    }

    // ═══════════════════════════════════════════════════════════
// PIGLIN ITEMS GUI
// ═══════════════════════════════════════════════════════════

    private static void openPiglinItemsGui(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("👑 Piglin Items"));

        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.GOLDEN_HELMET)
                .setName(Text.literal("👑 Piglin Items").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Piglin-themed bounty items").formatted(Formatting.YELLOW))
                .glow()
                .build());

        // Crown of Greed
        gui.setSlot(10, new GuiElementBuilder(Items.GOLDEN_HELMET)
                .setName(Text.literal("👑 Crown of Greed").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Damage scales with your coins").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Lose all coins on death!").formatted(Formatting.DARK_RED))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to receive").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> {
                    player.getInventory().insertStack(SlayerItems.createCrownOfGreed());
                    player.sendMessage(Text.literal("✓ Gave Crown of Greed").formatted(Formatting.GREEN), false);
                })
                .glow()
                .build());

        // Piglin Core
        gui.setSlot(11, new GuiElementBuilder(Items.GOLD_NUGGET)
                .setName(Text.literal("✦ Piglin Core ✦").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Used to craft Crown of Greed").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Dropped by Gilded Ravager boss").formatted(Formatting.GOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to receive").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> {
                    player.getInventory().insertStack(SlayerItems.createPiglinCore());
                    player.sendMessage(Text.literal("✓ Gave Piglin Core").formatted(Formatting.GREEN), false);
                })
                .glow()
                .build());

        // Piglin Flesh
        gui.setSlot(12, new GuiElementBuilder(Items.GOLD_NUGGET)
                .setName(Text.literal("Piglin Flesh").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Crafting material from Piglins").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to receive").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> {
                    player.getInventory().insertStack(SlayerItems.createPiglinFlesh());
                    player.sendMessage(Text.literal("✓ Gave Piglin Flesh").formatted(Formatting.GREEN), false);
                })
                .build());

        // Separator
        gui.setSlot(18, new GuiElementBuilder(Items.GOLD_BLOCK)
                .setName(Text.literal("━ Piglin T1 Armor ━").formatted(Formatting.GOLD, Formatting.BOLD))
                .build());

        // Piglin T1 Armor pieces
        String[] pieceNames = {"Helmet", "Chestplate", "Leggings", "Boots"};
        Item[] pieceIcons = {Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS};
        int[] t1Slots = {19, 20, 21, 22};
        int[] t2Slots = {28, 29, 30, 31};

        for (int i = 0; i < pieceNames.length; i++) {
            final String pieceName = pieceNames[i];
            gui.setSlot(t1Slots[i], new GuiElementBuilder(pieceIcons[i])
                    .setName(Text.literal("Piglin T1 " + pieceName).formatted(Formatting.GOLD, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Click to receive").formatted(Formatting.GREEN))
                    .setCallback((idx, type, action) -> {
                        player.getInventory().insertStack(SlayerItems.createT1Armor(SlayerManager.SlayerType.PIGLIN, pieceName));
                        player.sendMessage(Text.literal("✓ Gave Piglin T1 " + pieceName).formatted(Formatting.GREEN), false);
                    })
                    .build());
        }

        // Give T1 Full Set
        gui.setSlot(24, new GuiElementBuilder(Items.CHEST)
                .setName(Text.literal("Give T1 Full Set").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Give all 4 T1 Piglin armor pieces").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> {
                    for (String p : pieceNames) {
                        player.getInventory().insertStack(SlayerItems.createT1Armor(SlayerManager.SlayerType.PIGLIN, p));
                    }
                    player.sendMessage(Text.literal("✓ Gave full Piglin T1 armor set!").formatted(Formatting.GREEN), false);
                })
                .build());

        // Separator
        gui.setSlot(27, new GuiElementBuilder(Items.GOLD_BLOCK)
                .setName(Text.literal("━ Piglin T2 Armor ━").formatted(Formatting.YELLOW, Formatting.BOLD))
                .glow()
                .build());

        // Piglin T2 Armor pieces
        for (int i = 0; i < pieceNames.length; i++) {
            final String pieceName = pieceNames[i];
            gui.setSlot(t2Slots[i], new GuiElementBuilder(pieceIcons[i])
                    .setName(Text.literal("Piglin T2 " + pieceName + " II").formatted(Formatting.YELLOW, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Click to receive").formatted(Formatting.GREEN))
                    .setCallback((idx, type, action) -> {
                        player.getInventory().insertStack(SlayerItems.createT2Armor(SlayerManager.SlayerType.PIGLIN, pieceName));
                        player.sendMessage(Text.literal("✓ Gave Piglin T2 " + pieceName).formatted(Formatting.GREEN), false);
                    })
                    .glow()
                    .build());
        }

        // Give T2 Full Set
        gui.setSlot(33, new GuiElementBuilder(Items.ENDER_CHEST)
                .setName(Text.literal("Give T2 Full Set").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Give all 4 T2 Piglin armor pieces").formatted(Formatting.GOLD))
                .setCallback((idx, type, action) -> {
                    for (String p : pieceNames) {
                        player.getInventory().insertStack(SlayerItems.createT2Armor(SlayerManager.SlayerType.PIGLIN, p));
                    }
                    player.sendMessage(Text.literal("✓ Gave full Piglin T2 armor set!").formatted(Formatting.GREEN), false);
                })
                .glow()
                .build());

        // Back button
        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.GRAY))
                .setCallback((idx, type, action) -> openSlayerAdminGui(player))
                .build());

        gui.open();
    }

    // ═══════════════════════════════════════════════════════════
// CORES GIVE GUI
// ═══════════════════════════════════════════════════════════

    private static void openCoresGiveGui(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X4, player, false);
        gui.setTitle(Text.literal("✦ Give Bounty Cores ✦"));

        // Background
        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Text.literal("✦ Bounty Cores ✦").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Used for crafting bounty gear").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Left-click: Give 1").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Right-click: Give 16").formatted(Formatting.GOLD))
                .addLoreLine(Text.literal("Shift-click: Give 64").formatted(Formatting.RED))
                .build());

        // Core buttons
        SlayerManager.SlayerType[] types = SlayerManager.SlayerType.values();
        int[] slots = {10, 11, 12, 13, 14, 15};

        for (int i = 0; i < types.length && i < slots.length; i++) {
            SlayerManager.SlayerType type = types[i];

            gui.setSlot(slots[i], new GuiElementBuilder(Items.NETHER_STAR)
                    .setName(Text.literal(type.displayName + " Core").formatted(type.color, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Dropped by " + type.bossName).formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Left-click: Give 1").formatted(Formatting.YELLOW))
                    .addLoreLine(Text.literal("Right-click: Give 16").formatted(Formatting.GOLD))
                    .addLoreLine(Text.literal("Shift-click: Give 64").formatted(Formatting.RED))
                    .setCallback((idx, clickType, action) -> {
                        int amount = 1;

                        // Check for shift clicks
                        if (clickType == ClickType.MOUSE_LEFT_SHIFT || clickType == ClickType.MOUSE_RIGHT_SHIFT) {
                            amount = 64;
                        } else if (clickType == ClickType.MOUSE_RIGHT) {
                            amount = 16;
                        }

                        // Create the core item
                        ItemStack core = SlayerItems.createSlayerCore(type);
                        core.setCount(amount);
                        player.getInventory().insertStack(core);
                        player.sendMessage(Text.literal("✓ Gave " + amount + "x " + type.displayName + " Core")
                                .formatted(Formatting.GREEN), false);
                    })
                    .build());
        }

        // Give all cores (1 of each)
        gui.setSlot(28, new GuiElementBuilder(Items.CHEST)
                .setName(Text.literal("Give 1 of Each Core").formatted(Formatting.YELLOW, Formatting.BOLD))
                .setCallback((idx, clickType, action) -> {
                    for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
                        player.getInventory().insertStack(SlayerItems.createSlayerCore(type));
                    }
                    player.sendMessage(Text.literal("✓ Gave 1 of each core").formatted(Formatting.GREEN), false);
                })
                .build());

        // Give stacks of all cores
        gui.setSlot(29, new GuiElementBuilder(Items.ENDER_CHEST)
                .setName(Text.literal("Give 64 of Each Core").formatted(Formatting.GOLD, Formatting.BOLD))
                .setCallback((idx, clickType, action) -> {
                    for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
                        ItemStack core = SlayerItems.createSlayerCore(type);
                        core.setCount(64);
                        player.getInventory().insertStack(core);
                    }
                    player.sendMessage(Text.literal("✓ Gave 64 of each core").formatted(Formatting.GREEN), false);
                })
                .build());

        // Back button
        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.GRAY))
                .setCallback((idx, type, action) -> openSlayerAdminGui(player))
                .build());

        gui.open();
    }
    // ═══════════════════════════════════════════════════════════
// OTHER WEAPONS GUI (Gavel, HPEBM, etc.)
// ═══════════════════════════════════════════════════════════

    private static void openOtherWeaponsGui(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X4, player, false);
        gui.setTitle(Text.literal("⚔ Other Weapons ⚔"));

        // Background
        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        // The Gavel (Mace)
        gui.setSlot(10, new GuiElementBuilder(Items.MACE)
                .setName(Text.literal("⚖ The Gavel (Mace)").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Judicial AOE weapon (Mace)").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("25 damage, 4.5 radius").formatted(Formatting.RED))
                .addLoreLine(Text.literal("3s cooldown").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to receive").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> {
                    player.getInventory().insertStack(CustomItemHandler.createTheGavel());
                    player.sendMessage(Text.literal("✓ Gave The Gavel (Mace)").formatted(Formatting.GREEN), false);
                })
                .glow()
                .build());

        // The Gavel (Wooden Axe)
        gui.setSlot(11, new GuiElementBuilder(Items.WOODEN_AXE)
                .setName(Text.literal("⚖ The Gavel (Axe)").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Judicial weapon (Wooden Axe)").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Right-click for gavel strike").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to receive").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> {
                    player.getInventory().insertStack(CustomItemHandler.createGavel());
                    player.sendMessage(Text.literal("✓ Gave The Gavel (Axe)").formatted(Formatting.GREEN), false);
                })
                .glow()
                .build());

        // Harvey's Stick
        gui.setSlot(12, new GuiElementBuilder(Items.STICK)
                .setName(Text.literal("Harvey's Stick").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Summons lightning on hit").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to receive").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> {
                    player.getInventory().insertStack(CustomItemHandler.createHarveysStick());
                    player.sendMessage(Text.literal("✓ Gave Harvey's Stick").formatted(Formatting.GREEN), false);
                })
                .glow()
                .build());

        // Hermes Shoes
        gui.setSlot(13, new GuiElementBuilder(Items.IRON_BOOTS)
                .setName(Text.literal("Hermes Shoes").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Permanent Speed III while worn").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal("+60% movement speed").formatted(Formatting.AQUA))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to receive").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> {
                    player.getInventory().insertStack(CustomItemHandler.createHermesShoes());
                    player.sendMessage(Text.literal("✓ Gave Hermes Shoes").formatted(Formatting.GREEN), false);
                })
                .glow()
                .build());

        // HPEBM Mk.I-V
        gui.setSlot(15, new GuiElementBuilder(Items.IRON_SHOVEL)
                .setName(Text.literal("⚡ HPEBM (All Marks)").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Energy beam modules Mk.I–Mk.V").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Left-click: receive Mk.I through Mk.V").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Right-click: receive Ultra Overclocked").formatted(Formatting.GOLD))
                .setCallback((idx, clickType, action) -> {
                    if (clickType == eu.pb4.sgui.api.ClickType.MOUSE_RIGHT) {
                        player.getInventory().insertStack(CustomItemHandler.createUltraOverclockedBeam());
                        player.sendMessage(Text.literal("✓ Gave HPEBM Ultra Overclocked").formatted(Formatting.GOLD), false);
                    } else {
                        for (int mk = 1; mk <= 5; mk++) {
                            player.getInventory().insertStack(CustomItemHandler.createHPEBM(mk));
                        }
                        player.sendMessage(Text.literal("✓ Gave HPEBM Mk.I–Mk.V").formatted(Formatting.GREEN), false);
                    }
                })
                .glow()
                .build());

        // Back button
        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.GRAY))
                .setCallback((idx, type, action) -> openSlayerAdminGui(player))
                .build());

        gui.open();
    }

    // ═══════════════════════════════════════════════════════════
// SET PLAYER LEVEL GUI
// ═══════════════════════════════════════════════════════════

    private static void openSetLevelGui(ServerPlayerEntity admin) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, admin, false);
        gui.setTitle(Text.literal("Set Player Bounty Level"));

        // Background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.EXPERIENCE_BOTTLE)
                .setName(Text.literal("Set Bounty Level").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Select a player below").formatted(Formatting.GRAY))
                .build());

        // Get online players
        List<ServerPlayerEntity> players = new ArrayList<>(PoliticalServer.server.getPlayerManager().getPlayerList());
        int slot = 19;

        for (int i = 0; i < Math.min(players.size(), 14); i++) {
            ServerPlayerEntity target = players.get(i);

            gui.setSlot(slot, new GuiElementBuilder(Items.PLAYER_HEAD)
                    .setSkullOwner(target.getGameProfile(), PoliticalServer.server)
                    .setName(Text.literal(target.getName().getString()).formatted(Formatting.YELLOW))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Click to set levels").formatted(Formatting.GREEN))
                    .setCallback((idx, type, action) -> openSetLevelForPlayerGui(admin, target))
                    .build());

            slot++;
            if (slot == 26) slot = 28; // Skip to next row
        }

        // Back button
        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.GRAY))
                .setCallback((idx, type, action) -> openSlayerAdminGui(admin))
                .build());

        gui.open();
    }

    private static void openSetLevelForPlayerGui(ServerPlayerEntity admin, ServerPlayerEntity target) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, admin, false);
        gui.setTitle(Text.literal("Set Level: " + target.getName().getString()));

        // Background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        String uuid = target.getUuidAsString();

        // Show each slayer type
        SlayerManager.SlayerType[] types = SlayerManager.SlayerType.values();
        int[] slots = {10, 11, 12, 13, 14, 15};

        for (int i = 0; i < types.length && i < slots.length; i++) {
            SlayerManager.SlayerType type = types[i];
            int currentLevel = SlayerData.getSlayerLevel(uuid, type);

            gui.setSlot(slots[i], new GuiElementBuilder(type.icon)
                    .setName(Text.literal(type.displayName).formatted(type.color, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Current Level: " + currentLevel).formatted(Formatting.YELLOW))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Click to set level").formatted(Formatting.GREEN))
                    .setCallback((idx, clickType, action) -> openLevelSelectorGui(admin, target, type))
                    .build());
        }

        // Set All to Max
        gui.setSlot(31, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Text.literal("Set All to MAX (12)").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Sets all bounty levels to 12").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> {
                    for (SlayerManager.SlayerType t : SlayerManager.SlayerType.values()) {
                        SlayerData.setSlayerLevel(uuid, t, 12);
                    }
                    admin.sendMessage(Text.literal("✓ Set all levels to 12 for " + target.getName().getString())
                            .formatted(Formatting.GREEN), false);
                    openSetLevelForPlayerGui(admin, target);
                })
                .glow()
                .build());

        // Reset All to 0
        gui.setSlot(33, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("Reset All to 0").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Resets all bounty levels to 0").formatted(Formatting.RED))
                .setCallback((idx, type, action) -> {
                    for (SlayerManager.SlayerType t : SlayerManager.SlayerType.values()) {
                        SlayerData.setSlayerLevel(uuid, t, 0);
                        SlayerData.setSlayerXp(uuid, t, 0);
                    }
                    admin.sendMessage(Text.literal("✓ Reset all levels for " + target.getName().getString())
                            .formatted(Formatting.GREEN), false);
                    openSetLevelForPlayerGui(admin, target);
                })
                .build());

        // Back button
        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.GRAY))
                .setCallback((idx, type, action) -> openSetLevelGui(admin))
                .build());

        gui.open();
    }

    private static void openLevelSelectorGui(ServerPlayerEntity admin, ServerPlayerEntity target, SlayerManager.SlayerType slayerType) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, admin, false);
        gui.setTitle(Text.literal("Set " + slayerType.displayName + " Level"));

        // Background
        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        String uuid = target.getUuidAsString();
        int currentLevel = SlayerData.getSlayerLevel(uuid, slayerType);

        // Level buttons 0-12
        int[] levelSlots = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
        int[] guiSlots = {9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21};

        for (int i = 0; i < levelSlots.length && i < guiSlots.length; i++) {
            int level = levelSlots[i];
            boolean isCurrent = level == currentLevel;

            gui.setSlot(guiSlots[i], new GuiElementBuilder(isCurrent ? Items.LIME_STAINED_GLASS_PANE : Items.WHITE_STAINED_GLASS_PANE)
                    .setName(Text.literal("Level " + level).formatted(isCurrent ? Formatting.GREEN : Formatting.YELLOW, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(isCurrent
                            ? Text.literal("✓ Current Level").formatted(Formatting.GREEN)
                            : Text.literal("Click to set").formatted(Formatting.GRAY))
                    .setCallback((idx, type, action) -> {
                        SlayerData.setSlayerLevel(uuid, slayerType, level);
                        // Also set appropriate XP for level
                        if (level > 0 && level <= SlayerManager.XP_REQUIREMENTS.length) {
                            SlayerData.setSlayerXp(uuid, slayerType, SlayerManager.XP_REQUIREMENTS[level - 1]);
                        } else {
                            SlayerData.setSlayerXp(uuid, slayerType, 0);
                        }
                        admin.sendMessage(Text.literal("✓ Set " + target.getName().getString() + "'s " +
                                slayerType.displayName + " level to " + level).formatted(Formatting.GREEN), false);
                        openSetLevelForPlayerGui(admin, target);
                    })
                    .build());
        }

        // Back button
        gui.setSlot(26, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.GRAY))
                .setCallback((idx, type, action) -> openSetLevelForPlayerGui(admin, target))
                .build());

        gui.open();
    }
    // ═══════════════════════════════════════════════════════════
// SET PLAYER XP GUI
// ═══════════════════════════════════════════════════════════

    private static void openSetXpGui(ServerPlayerEntity admin) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, admin, false);
        gui.setTitle(Text.literal("Set Player Bounty XP"));

        // Background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.ENCHANTING_TABLE)
                .setName(Text.literal("Set Bounty XP").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Select a player below").formatted(Formatting.GRAY))
                .build());

        // Get online players
        List<ServerPlayerEntity> players = new ArrayList<>(PoliticalServer.server.getPlayerManager().getPlayerList());
        int slot = 19;

        for (int i = 0; i < Math.min(players.size(), 14); i++) {
            ServerPlayerEntity target = players.get(i);

            gui.setSlot(slot, new GuiElementBuilder(Items.PLAYER_HEAD)
                    .setSkullOwner(target.getGameProfile(), PoliticalServer.server)
                    .setName(Text.literal(target.getName().getString()).formatted(Formatting.YELLOW))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Click to set XP").formatted(Formatting.GREEN))
                    .setCallback((idx, type, action) -> openSetXpForPlayerGui(admin, target))
                    .build());

            slot++;
            if (slot == 26) slot = 28;
        }

        // Back button
        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.GRAY))
                .setCallback((idx, type, action) -> openSlayerAdminGui(admin))
                .build());

        gui.open();
    }

    private static void openSetXpForPlayerGui(ServerPlayerEntity admin, ServerPlayerEntity target) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X5, admin, false);
        gui.setTitle(Text.literal("Set XP: " + target.getName().getString()));

        // Background
        for (int i = 0; i < 45; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        String uuid = target.getUuidAsString();

        // All 7 slayer types with proper slots
        SlayerManager.SlayerType[] types = {
                SlayerManager.SlayerType.ZOMBIE,
                SlayerManager.SlayerType.SPIDER,
                SlayerManager.SlayerType.SKELETON,
                SlayerManager.SlayerType.SLIME,
                SlayerManager.SlayerType.ENDERMAN,
                SlayerManager.SlayerType.IRON_GOLEM,
                SlayerManager.SlayerType.PIGLIN
        };
        int[] slots = {10, 11, 12, 13, 14, 15, 16};

        for (int i = 0; i < types.length && i < slots.length; i++) {
            SlayerManager.SlayerType type = types[i];
            long currentXp = SlayerData.getSlayerXp(uuid, type);
            int currentLevel = SlayerData.getSlayerLevel(uuid, type);

            gui.setSlot(slots[i], new GuiElementBuilder(type.icon)
                    .setName(Text.literal(type.displayName).formatted(type.color, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Current XP: " + formatNumber(currentXp)).formatted(Formatting.AQUA))
                    .addLoreLine(Text.literal("Current Level: " + currentLevel).formatted(Formatting.YELLOW))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Left-click: +1,000 XP").formatted(Formatting.GREEN))
                    .addLoreLine(Text.literal("Right-click: +100,000 XP").formatted(Formatting.GOLD))
                    .addLoreLine(Text.literal("Shift-click: +10,000,000 XP").formatted(Formatting.RED))
                    .setCallback((idx, clickType, action) -> {
                        long addXp = 1000;

                        // Check for shift clicks
                        if (clickType == ClickType.MOUSE_LEFT_SHIFT || clickType == ClickType.MOUSE_RIGHT_SHIFT) {
                            addXp = 10_000_000;
                        } else if (clickType == ClickType.MOUSE_RIGHT) {
                            addXp = 100_000;
                        }

                        long newXp = currentXp + addXp;
                        SlayerData.setSlayerXp(uuid, type, newXp);
                        int newLevel = SlayerManager.getLevelForXp(newXp);
                        SlayerData.setSlayerLevel(uuid, type, newLevel);

                        admin.sendMessage(Text.literal("✓ Added " + formatNumber(addXp) + " XP to " +
                                        target.getName().getString() + "'s " + type.displayName + " (now level " + newLevel + ")")
                                .formatted(Formatting.GREEN), false);
                        openSetXpForPlayerGui(admin, target);
                    })
                    .build());
        }

        // Back button
        gui.setSlot(40, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.GRAY))
                .setCallback((idx, type, action) -> openSetXpGui(admin))
                .build());

        gui.open();
    }

    private static String formatNumber(long num) {
        if (num >= 1_000_000) {
            return String.format("%.1fM", num / 1_000_000.0);
        } else if (num >= 1_000) {
            return String.format("%.1fK", num / 1_000.0);
        }
        return String.valueOf(num);
    }
    // ═══════════════════════════════════════════════════════════
// RESET PLAYER DATA GUI
// ═══════════════════════════════════════════════════════════

    private static void openResetDataGui(ServerPlayerEntity admin) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, admin, false);
        gui.setTitle(Text.literal("⚠ Reset Player Bounty Data ⚠"));

        // Background - red to indicate danger
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.RED_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        // Warning header
        gui.setSlot(4, new GuiElementBuilder(Items.TNT)
                .setName(Text.literal("⚠ DANGER ZONE ⚠").formatted(Formatting.DARK_RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("This will PERMANENTLY reset").formatted(Formatting.RED))
                .addLoreLine(Text.literal("a player's bounty progress!").formatted(Formatting.RED))
                .glow()
                .build());

        // Get online players
        List<ServerPlayerEntity> players = new ArrayList<>(PoliticalServer.server.getPlayerManager().getPlayerList());
        int slot = 19;

        for (int i = 0; i < Math.min(players.size(), 14); i++) {
            ServerPlayerEntity target = players.get(i);
            String uuid = target.getUuidAsString();
            int totalLevel = SlayerData.getTotalSlayerLevel(uuid);

            gui.setSlot(slot, new GuiElementBuilder(Items.PLAYER_HEAD)
                    .setSkullOwner(target.getGameProfile(), PoliticalServer.server)
                    .setName(Text.literal(target.getName().getString()).formatted(Formatting.YELLOW))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Total Level: " + totalLevel).formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Click to reset ALL data").formatted(Formatting.RED))
                    .setCallback((idx, type, action) -> openConfirmResetGui(admin, target))
                    .build());

            slot++;
            if (slot == 26) slot = 28;
        }

        // Back button
        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.GRAY))
                .setCallback((idx, type, action) -> openSlayerAdminGui(admin))
                .build());

        gui.open();
    }

    private static void openConfirmResetGui(ServerPlayerEntity admin, ServerPlayerEntity target) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, admin, false);
        gui.setTitle(Text.literal("Confirm Reset: " + target.getName().getString()));

        // Background
        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        // Warning
        gui.setSlot(4, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("⚠ ARE YOU SURE? ⚠").formatted(Formatting.DARK_RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("This will reset ALL bounty data for:").formatted(Formatting.RED))
                .addLoreLine(Text.literal(target.getName().getString()).formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("• All levels → 0").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("• All XP → 0").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("• All boss kills → 0").formatted(Formatting.GRAY))
                .build());

        // Confirm button
        gui.setSlot(11, new GuiElementBuilder(Items.LIME_CONCRETE)
                .setName(Text.literal("✓ CONFIRM RESET").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to permanently reset").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> {
                    String uuid = target.getUuidAsString();
                    for (SlayerManager.SlayerType t : SlayerManager.SlayerType.values()) {
                        SlayerData.setSlayerLevel(uuid, t, 0);
                        SlayerData.setSlayerXp(uuid, t, 0);
                        SlayerData.setBossesKilled(uuid, t, 0);
                    }
                    admin.sendMessage(Text.literal("✓ Reset all bounty data for " + target.getName().getString())
                            .formatted(Formatting.GREEN), false);
                    target.sendMessage(Text.literal("⚠ Your bounty data has been reset by an admin!")
                            .formatted(Formatting.RED), false);
                    openSlayerAdminGui(admin);
                })
                .build());

        // Cancel button
        gui.setSlot(15, new GuiElementBuilder(Items.RED_CONCRETE)
                .setName(Text.literal("✗ CANCEL").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Go back without resetting").formatted(Formatting.GRAY))
                .setCallback((idx, type, action) -> openResetDataGui(admin))
                .build());

        gui.open();
    }
    // ═══════════════════════════════════════════════════════════
// CANCEL PLAYER QUEST GUI
// ═══════════════════════════════════════════════════════════

    private static void openCancelQuestGui(ServerPlayerEntity admin) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, admin, false);
        gui.setTitle(Text.literal("Cancel Player Quests"));

        // Background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("Cancel Active Quests").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Force cancel a player's").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("active bounty quest").formatted(Formatting.GRAY))
                .build());

        // Get online players with active quests
        List<ServerPlayerEntity> players = new ArrayList<>(PoliticalServer.server.getPlayerManager().getPlayerList());
        int slot = 19;

        for (int i = 0; i < Math.min(players.size(), 14); i++) {
            ServerPlayerEntity target = players.get(i);
            SlayerManager.ActiveQuest quest = SlayerManager.getActiveQuest(target);

            GuiElementBuilder element = new GuiElementBuilder(Items.PLAYER_HEAD)
                    .setSkullOwner(target.getGameProfile(), PoliticalServer.server)
                    .setName(Text.literal(target.getName().getString()).formatted(Formatting.YELLOW));

            if (quest != null) {
                element.addLoreLine(Text.literal(""))
                        .addLoreLine(Text.literal("Active Quest:").formatted(Formatting.GREEN))
                        .addLoreLine(Text.literal("• Type: " + quest.slayerType.displayName).formatted(quest.slayerType.color))
                        .addLoreLine(Text.literal("• Tier: " + quest.tier).formatted(Formatting.WHITE))
                        .addLoreLine(Text.literal("• Progress: " + quest.killCount + "/" + quest.getKillsRequired()).formatted(Formatting.GRAY))
                        .addLoreLine(Text.literal(""))
                        .addLoreLine(Text.literal("Click to cancel quest").formatted(Formatting.RED))
                        .setCallback((idx, type, action) -> {
                            SlayerManager.cancelQuest(target);
                            admin.sendMessage(Text.literal("✓ Cancelled " + target.getName().getString() + "'s quest")
                                    .formatted(Formatting.GREEN), false);
                            target.sendMessage(Text.literal("⚠ Your bounty quest was cancelled by an admin!")
                                    .formatted(Formatting.RED), false);
                            openCancelQuestGui(admin);
                        });
            } else {
                element.addLoreLine(Text.literal(""))
                        .addLoreLine(Text.literal("No active quest").formatted(Formatting.GRAY));
            }

            gui.setSlot(slot, element.build());

            slot++;
            if (slot == 26) slot = 28;
        }

        // Back button
        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.GRAY))
                .setCallback((idx, type, action) -> openSlayerAdminGui(admin))
                .build());

        gui.open();
    }
    // ═══════════════════════════════════════════════════════════
// VIEW PLAYER STATS GUI
// ═══════════════════════════════════════════════════════════

    private static void openViewStatsGui(ServerPlayerEntity admin) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, admin, false);
        gui.setTitle(Text.literal("View Player Bounty Stats"));

        // Background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.BOOK)
                .setName(Text.literal("Player Statistics").formatted(Formatting.WHITE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("View detailed bounty stats").formatted(Formatting.GRAY))
                .build());

        // Get online players
        List<ServerPlayerEntity> players = new ArrayList<>(PoliticalServer.server.getPlayerManager().getPlayerList());
        int slot = 19;

        for (int i = 0; i < Math.min(players.size(), 14); i++) {
            ServerPlayerEntity target = players.get(i);
            String uuid = target.getUuidAsString();
            int totalLevel = SlayerData.getTotalSlayerLevel(uuid);
            int totalBosses = SlayerData.getTotalBossesKilled(uuid);

            gui.setSlot(slot, new GuiElementBuilder(Items.PLAYER_HEAD)
                    .setSkullOwner(target.getGameProfile(), PoliticalServer.server)
                    .setName(Text.literal(target.getName().getString()).formatted(Formatting.YELLOW))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Total Level: " + totalLevel).formatted(Formatting.GREEN))
                    .addLoreLine(Text.literal("Total Bosses: " + totalBosses).formatted(Formatting.RED))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Click for details").formatted(Formatting.GRAY))
                    .setCallback((idx, type, action) -> openPlayerStatsDetailGui(admin, target))
                    .build());

            slot++;
            if (slot == 26) slot = 28;
        }

        // Back button
        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.GRAY))
                .setCallback((idx, type, action) -> openSlayerAdminGui(admin))
                .build());

        gui.open();
    }

    private static void openPlayerStatsDetailGui(ServerPlayerEntity admin, ServerPlayerEntity target) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X4, admin, false);
        gui.setTitle(Text.literal("Stats: " + target.getName().getString()));

        // Background
        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        String uuid = target.getUuidAsString();

        // Header with summary
        int totalLevel = SlayerData.getTotalSlayerLevel(uuid);
        int totalBosses = SlayerData.getTotalBossesKilled(uuid);

        gui.setSlot(4, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setSkullOwner(target.getGameProfile(), PoliticalServer.server)
                .setName(Text.literal(target.getName().getString()).formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Total Level: " + totalLevel + " / " + (SlayerManager.MAX_LEVEL * 6)).formatted(Formatting.GREEN))
                .addLoreLine(Text.literal("Total Bosses Killed: " + totalBosses).formatted(Formatting.RED))
                .build());

        // Individual slayer stats
        SlayerManager.SlayerType[] types = SlayerManager.SlayerType.values();
        int[] slots = {19, 20, 21, 22, 23, 24};

        for (int i = 0; i < types.length && i < slots.length; i++) {
            SlayerManager.SlayerType type = types[i];
            int level = SlayerData.getSlayerLevel(uuid, type);
            long xp = SlayerData.getSlayerXp(uuid, type);
            int bosses = SlayerData.getBossesKilled(uuid, type);
            double progress = SlayerData.getProgressToNextLevel(uuid, type);

            String progressText = level >= SlayerManager.MAX_LEVEL
                    ? "MAX LEVEL"
                    : String.format("%.1f%%", progress * 100);

            gui.setSlot(slots[i], new GuiElementBuilder(type.icon)
                    .setName(Text.literal(type.displayName).formatted(type.color, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Level: " + level + " / " + SlayerManager.MAX_LEVEL).formatted(Formatting.YELLOW))
                    .addLoreLine(Text.literal("XP: " + formatNumber(xp)).formatted(Formatting.AQUA))
                    .addLoreLine(Text.literal("Bosses Killed: " + bosses).formatted(Formatting.RED))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Progress: " + progressText).formatted(
                            level >= SlayerManager.MAX_LEVEL ? Formatting.GOLD : Formatting.GREEN))
                    .build());
        }

        // Active quest info
        SlayerManager.ActiveQuest quest = SlayerManager.getActiveQuest(target);
        if (quest != null) {
            gui.setSlot(31, new GuiElementBuilder(Items.COMPASS)
                    .setName(Text.literal("Active Quest").formatted(Formatting.YELLOW, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Type: " + quest.slayerType.displayName).formatted(quest.slayerType.color))
                    .addLoreLine(Text.literal("Tier: " + quest.tier).formatted(Formatting.WHITE))
                    .addLoreLine(Text.literal("Progress: " + quest.killCount + "/" + quest.getKillsRequired()).formatted(Formatting.GREEN))
                    .addLoreLine(Text.literal("Boss Spawned: " + (quest.bossSpawned ? "Yes" : "No")).formatted(
                            quest.bossSpawned ? Formatting.RED : Formatting.GRAY))
                    .glow()
                    .build());
        } else {
            gui.setSlot(31, new GuiElementBuilder(Items.GRAY_DYE)
                    .setName(Text.literal("No Active Quest").formatted(Formatting.GRAY))
                    .build());
        }

        // Back button
        gui.setSlot(35, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.GRAY))
                .setCallback((idx, type, action) -> openViewStatsGui(admin))
                .build());

        gui.open();
    }
    // ============================================================
// SLAYER CORE CREATION
// ============================================================

    public static ItemStack createSlayerCore(SlayerManager.SlayerType type) {
        ItemStack core = new ItemStack(Items.NETHER_STAR);

        core.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal(type.displayName + " Core")
                        .formatted(type.color, Formatting.BOLD));

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("BOUNTY MATERIAL").formatted(Formatting.DARK_PURPLE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Dropped by: " + type.bossName).formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Used to craft " + type.displayName).formatted(Formatting.YELLOW));
        lore.add(Text.literal("bounty weapons and armor.").formatted(Formatting.YELLOW));

        core.set(DataComponentTypes.LORE, new LoreComponent(lore));
        core.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);

        return core;
    }
    private static void openT2ArmorGiveGui(ServerPlayerEntity admin) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, admin, false);
        gui.setTitle(Text.literal("Give T2 Armor"));

        fillBackground(gui);

        int slot = 10;
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            // Helmet
            ItemStack helmet = SlayerItems.createT2Helmet(type);
            gui.setSlot(slot, createGiveButton(helmet, admin));

            // Chestplate
            ItemStack chestplate = SlayerItems.createT2Chestplate(type);
            gui.setSlot(slot + 1, createGiveButton(chestplate, admin));

            // Leggings
            ItemStack leggings = SlayerItems.createT2Leggings(type);
            gui.setSlot(slot + 2, createGiveButton(leggings, admin));

            // Boots
            ItemStack boots = SlayerItems.createT2Boots(type);
            gui.setSlot(slot + 3, createGiveButton(boots, admin));

            slot += 9; // Next row
        }

        // Back button
        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("Back").formatted(Formatting.GRAY))
                .setCallback((idx, clickType, action) -> {
                    openSlayerAdminGui(admin);
                })
                .build());

        gui.open();
    }

    private static GuiElementInterface createGiveButton(ItemStack item, ServerPlayerEntity admin) {
        return new GuiElementBuilder(item.getItem())
                .setName(item.get(DataComponentTypes.CUSTOM_NAME))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to receive").formatted(Formatting.GREEN))
                .setCallback((idx, clickType, action) -> {
                    admin.giveItemStack(item.copy());
                    admin.sendMessage(Text.literal("§a✓ Given!"), false);
                })
                .build();
    }
    // ═══════════════════════════════════════════════════════════
// SPAWN UPGRADED MOB SUBMENU
// ═══════════════════════════════════════════════════════════

    private static void openPrisonGui(ServerPlayerEntity admin) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, admin, false);
        gui.setTitle(Text.literal("Prison Controls"));

        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }


        gui.setSlot(4, new GuiElementBuilder(Items.IRON_SWORD)
                .setName(Text.literal("⚔ Bounty Administration").formatted(Formatting.RED, Formatting.BOLD))
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
                .addLoreLine(Text.literal("Basic bounty swords").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Click to view!").formatted(Formatting.GREEN))
                .setCallback((idx, type, action) -> openBasicSwordsMenu(player))
                .build());

        // Upgraded Swords
        gui.setSlot(21, new GuiElementBuilder(Items.DIAMOND_SWORD)
                .setName(Text.literal("⚔ Upgraded Swords").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Enhanced bounty swords").formatted(Formatting.GRAY))
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
                .setName(Text.literal("🛡 Bounty Armor").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Give bounty armor sets").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("T1 and T2 versions").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to view!").formatted(Formatting.GREEN))
                .setCallback((idx, type, action) -> openArmorAdminMenu(player))
                .build());

        // Custom Enchantments
        gui.setSlot(39, new GuiElementBuilder(Items.ENCHANTED_BOOK)
                .setName(Text.literal("✦ Custom Enchantments").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Apply custom enchantments").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("to held items").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to view!").formatted(Formatting.GREEN))
                .setCallback((idx, type, action) -> openCustomEnchantmentsMenu(player))
                .build());

        // Back button
        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to Admin Menu").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> open(player))
                .build());

        gui.open();

        gui.setSlot(43, new GuiElementBuilder(Items.WITHER_SKELETON_SKULL)
                .setName(Text.literal("☠ Force Spawn Boss").formatted(Formatting.DARK_RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Spawn a bounty boss at your location").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to select boss type").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openSpawnBossMenu(player))
                .build());

    }

// ============================================================
// LEGENDARY WEAPONS MENU
// ============================================================
private static void openSpawnBossMenu(ServerPlayerEntity player) {
    SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X4, player, false);
    gui.setTitle(Text.literal("☠ Spawn Bounty Boss"));

    // Background
    for (int i = 0; i < 36; i++) {
        gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                .setName(Text.literal(""))
                .build());
    }

    // Boss type buttons
    int slot = 10;
    for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
        final SlayerManager.SlayerType finalType = type;

        gui.setSlot(slot, new GuiElementBuilder(type.icon)
                .setName(Text.literal("☠ " + type.bossName).formatted(type.color, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to select tier").formatted(Formatting.YELLOW))
                .setCallback((idx, clickType, action) -> openSpawnBossTierMenu(player, finalType))
                .build());

        slot++;
        if (slot == 17) slot = 19; // Next row
    }

    // Back button
    gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
            .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
            .setCallback((idx, type, action) -> openSlayerAdminGui(player))
            .build());

    gui.open();
}
    private static void openSpawnBossTierMenu(ServerPlayerEntity player, SlayerManager.SlayerType type) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
        gui.setTitle(Text.literal("☠ " + type.bossName + " - Select Tier"));

        // Background
        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        // Tier buttons
        int[] tierSlots = {11, 12, 13, 14, 15};
        for (int tier = 1; tier <= 5; tier++) {
            final int finalTier = tier;
            SlayerManager.TierConfig config = SlayerManager.getTierConfig(tier);

            gui.setSlot(tierSlots[tier - 1], new GuiElementBuilder(Items.PAPER)
                    .setName(Text.literal("Tier " + tier).formatted(Formatting.YELLOW, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("HP: " + (int)config.getActualHp(type)).formatted(Formatting.RED))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Click to spawn!").formatted(Formatting.GREEN))
                    .setCallback((idx, clickType, action) -> {
                        SlayerManager.adminSpawnBoss(player, type, finalTier);
                        player.sendMessage(Text.literal("✓ Spawned " + type.bossName + " T" + finalTier + "!")
                                .formatted(Formatting.GREEN));
                        player.closeHandledScreen();
                    })
                    .build());
        }

        // Back button
        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, clickType, action) -> openSpawnBossMenu(player))
                .build());

        gui.open();
    }

    private static void openSpawnUpgradedMobMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X4, player, false);
        gui.setTitle(Text.literal("⬆ Spawn Upgraded Mob (limited to some hostiles)"));

        // Background
        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.LIME_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        // Mob types with tiers
        gui.setSlot(10, createUpgradedMobButton(player, "Zombie", Items.ZOMBIE_HEAD,
                net.minecraft.entity.EntityType.ZOMBIE));
        gui.setSlot(11, createUpgradedMobButton(player, "Skeleton", Items.SKELETON_SKULL,
                net.minecraft.entity.EntityType.SKELETON));
        gui.setSlot(12, createUpgradedMobButton(player, "Spider", Items.SPIDER_EYE,
                net.minecraft.entity.EntityType.SPIDER));
        gui.setSlot(13, createUpgradedMobButton(player, "Creeper", Items.CREEPER_HEAD,
                net.minecraft.entity.EntityType.CREEPER));
        gui.setSlot(14, createUpgradedMobButton(player, "Enderman", Items.ENDER_PEARL,
                net.minecraft.entity.EntityType.ENDERMAN));
        gui.setSlot(15, createUpgradedMobButton(player, "Slime", Items.SLIME_BALL,
                net.minecraft.entity.EntityType.SLIME));


        // Back button
        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openSlayerAdminGui(player))
                .build());

        gui.open();
    }

    private static GuiElementBuilder createUpgradedMobButton(ServerPlayerEntity player, String name,
                                                             net.minecraft.item.Item icon, net.minecraft.entity.EntityType<?> entityType) {
        return new GuiElementBuilder(icon)
                .setName(Text.literal("⬆ " + name).formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to spawn upgraded " + name).formatted(Formatting.GRAY))
                .setCallback((idx, type, action) -> {
                    HealthScalingManager.spawnUpgradedMob(player, entityType);
                    player.sendMessage(Text.literal("✓ Spawned upgraded " + name + "!")
                            .formatted(Formatting.GREEN));
                });
    }

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
    gui.setTitle(Text.literal("🛡 Bounty Armor Admin"));

    for (int i = 0; i < 36; i++) {
        gui.setSlot(i, new GuiElementBuilder(Items.CYAN_STAINED_GLASS_PANE)
                .setName(Text.literal("")).build());
    }

    // T1 Armor Sets
    gui.setSlot(1, new GuiElementBuilder(Items.IRON_CHESTPLATE)
            .setName(Text.literal("T1 Armor Sets").formatted(Formatting.WHITE, Formatting.BOLD))
            .addLoreLine(Text.literal("Basic bounty armor").formatted(Formatting.GRAY))
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
            .addLoreLine(Text.literal("Upgraded bounty armor").formatted(Formatting.GRAY))
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


    // ============================================================
// CUSTOM ITEMS ADMIN MENU
// ============================================================

    public static void openCustomItemsMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("§6§lAdmin: Custom Items"));

        // Fill background
        ItemStack filler = new ItemStack(Items.BLACK_STAINED_GLASS_PANE);
        filler.set(DataComponentTypes.CUSTOM_NAME, Text.literal(" "));
        for (int i = 0; i < 54; i++) {
            int finalI = i;
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal(" "))
                    .setCallback((index, type, action) -> {})
                    .build());
        }

        // === ROW 1: SLAYER SWORDS ===
        gui.setSlot(1, createGiveItemButton(player, SlayerItems.createSlayerSword(SlayerManager.SlayerType.ZOMBIE),
                "§2§lZombie Cleaver", "§7Click to receive"));
        gui.setSlot(2, createGiveItemButton(player, SlayerItems.createSlayerSword(SlayerManager.SlayerType.SPIDER),
                "§4§lSpider Fang", "§7Click to receive"));
        gui.setSlot(3, createGiveItemButton(player, SlayerItems.createSlayerSword(SlayerManager.SlayerType.SKELETON),
                "§f§lBone Blade", "§7Click to receive"));
        gui.setSlot(4, createGiveItemButton(player, SlayerItems.createSlayerSword(SlayerManager.SlayerType.SLIME),
                "§a§lGelatinous Blade", "§7Click to receive"));
        gui.setSlot(5, createGiveItemButton(player, SlayerItems.createSlayerSword(SlayerManager.SlayerType.ENDERMAN),
                "§5§lVoid Blade", "§7Click to receive"));
        gui.setSlot(6, createGiveItemButton(player, SlayerItems.createSlayerSword(SlayerManager.SlayerType.IRON_GOLEM),
                "§3§lThe Gavel", "§7Click to receive"));
        gui.setSlot(7, createGiveItemButton(player, SlayerItems.createSkeletonBow(),
                "§f§lBone Bow", "§7Click to receive"));

        // === ROW 2: SLAYER ARMOR ===
        gui.setSlot(10, createGiveItemButton(player, SlayerItems.createZombieHelmet(),
                "§2§lBerserker Helmet", "§7Zombie Bounty Set"));
        gui.setSlot(11, createGiveItemButton(player, SlayerItems.createSpiderLeggings(),
                "§4§lVenomous Leggings", "§7Spider Bounty Set"));
        gui.setSlot(12, createGiveItemButton(player, SlayerItems.createSkeletonBow(),
                "§f§lBone Chestplate", "§7Skeleton Bounty Set"));
        gui.setSlot(13, createGiveItemButton(player, SlayerItems.createSlimeBoots(),
                "§a§lSlime Boots", "§7Slime Bounty Set"));
        gui.setSlot(14, createGiveItemButton(player, SlayerItems.createEndermanSword(),
                "§5§lVoid Chestplate", "§7Enderman Bounty Set"));
        gui.setSlot(15, createGiveItemButton(player, SlayerItems.createWardenChestplate(),
                "§3§lWarden Chestplate", "§7Warden Bounty Set"));
        gui.setSlot(16, createGiveItemButton(player, SlayerItems.createVoidwalkerCrown(),
                "§5§lVoidwalker's Crown", "§7Enderman Special Drop"));

        // === ROW 3: CRAFTING CHUNKS ===
        gui.setSlot(19, createGiveItemButton(player, SlayerItems.createChunk(SlayerManager.SlayerType.ZOMBIE),
                "§2Zombie Chunk", "§7Crafting material"));
        gui.setSlot(20, createGiveItemButton(player, SlayerItems.createChunk(SlayerManager.SlayerType.SPIDER),
                "§4Spider Chunk", "§7Crafting material"));
        gui.setSlot(21, createGiveItemButton(player, SlayerItems.createChunk(SlayerManager.SlayerType.SKELETON),
                "§fSkeleton Chunk", "§7Crafting material"));
        gui.setSlot(22, createGiveItemButton(player, SlayerItems.createChunk(SlayerManager.SlayerType.SLIME),
                "§aSlime Chunk", "§7Crafting material"));
        gui.setSlot(23, createGiveItemButton(player, SlayerItems.createChunk(SlayerManager.SlayerType.ENDERMAN),
                "§5Enderman Chunk", "§7Crafting material"));
        gui.setSlot(24, createGiveItemButton(player, SlayerItems.createChunk(SlayerManager.SlayerType.IRON_GOLEM),
                "§3Warden Chunk", "§7Crafting material"));

        // === ROW 4: CORES (Rare) ===
        gui.setSlot(28, createGiveItemButton(player, SlayerItems.createCore(SlayerManager.SlayerType.ZOMBIE),
                "§2§lZombie Core", "§6Legendary material"));
        gui.setSlot(29, createGiveItemButton(player, SlayerItems.createCore(SlayerManager.SlayerType.SPIDER),
                "§4§lSpider Core", "§6Legendary material"));
        gui.setSlot(30, createGiveItemButton(player, SlayerItems.createCore(SlayerManager.SlayerType.SKELETON),
                "§f§lSkeleton Core", "§6Legendary material"));
        gui.setSlot(31, createGiveItemButton(player, SlayerItems.createCore(SlayerManager.SlayerType.SLIME),
                "§a§lSlime Core", "§6Legendary material"));
        gui.setSlot(32, createGiveItemButton(player, SlayerItems.createCore(SlayerManager.SlayerType.ENDERMAN),
                "§5§lEnderman Core", "§6Legendary material"));
        gui.setSlot(33, createGiveItemButton(player, SlayerItems.createCore(SlayerManager.SlayerType.IRON_GOLEM),
                "§3§lWarden Core", "§6Legendary material"));

        // === ROW 5: HPEBM WEAPONS ===
        gui.setSlot(37, createGiveItemButton(player, CustomItemHandler.createHPEBM(1),
                "§e§lHPEBM Mk1", "§7Tier 1 Beam"));
        gui.setSlot(38, createGiveItemButton(player, CustomItemHandler.createHPEBM(2),
                "§e§lHPEBM Mk2", "§7Tier 2 Beam"));
        gui.setSlot(39, createGiveItemButton(player, CustomItemHandler.createHPEBM(3),
                "§e§lHPEBM Mk3", "§7Tier 3 Beam"));
        gui.setSlot(40, createGiveItemButton(player, CustomItemHandler.createHPEBM(4),
                "§e§lHPEBM Mk4", "§7Tier 4 Beam"));
        gui.setSlot(41, createGiveItemButton(player, CustomItemHandler.createHPEBM(5),
                "§e§lHPEBM Mk5", "§7Tier 5 Beam"));
        gui.setSlot(42, createGiveItemButton(player, CustomItemHandler.createHPEBM(6),
                "§6§lOverclocked Beam", "§cTier 6 Ultimate"));
        gui.setSlot(43, createGiveItemButton(player, CustomItemHandler.createHPEBM(7),
                "§d§lUltra Overclocked", "§cTier 7 Mythic"));

        // === BACK BUTTON ===
        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("§c§lBack"))
                .setCallback((index, type, action) -> {
                    openMainPage(player);
                })
                .build());

        // === GIVE ALL BUTTON ===
        gui.setSlot(53, new GuiElementBuilder(Items.CHEST)
                .setName(Text.literal("§a§lGive All Items"))
                .setLore(List.of(Text.literal("§7Click to receive ALL items")))
                .setCallback((index, type, action) -> {
                    giveAllCustomItems(player);
                })
                .build());

        gui.open();
    }

    // Helper method to create give item buttons
    private static GuiElementInterface createGiveItemButton(ServerPlayerEntity player, ItemStack item, String name, String lore) {
        if (item == null || item.isEmpty()) {
            return new GuiElementBuilder(Items.BARRIER)
                    .setName(Text.literal("§c§lMissing Item"))
                    .setLore(List.of(Text.literal("§7Item not found")))
                    .build();
        }

        return new GuiElementBuilder(item.getItem())
                .setName(Text.literal(name))
                .setLore(List.of(Text.literal(lore), Text.literal("§eClick to receive")))
                .setCallback((index, type, action) -> {
                    ItemStack clone = item.copy();
                    if (player.getInventory().insertStack(clone)) {
                        player.sendMessage(Text.literal("§a✓ Given: " + name), false);
                        player.playSound(net.minecraft.sound.SoundEvents.ENTITY_ITEM_PICKUP, 0.5f, 1.0f);
                    } else {
                        player.sendMessage(Text.literal("§c✖ Inventory full!"), false);
                    }
                })
                .build();
    }

    // Give all custom items
    private static void giveAllCustomItems(ServerPlayerEntity player) {
        int given = 0;

        // All swords
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            ItemStack sword = SlayerItems.createSlayerSword(type);
            if (sword != null && player.getInventory().insertStack(sword)) given++;

            ItemStack chunk = SlayerItems.createChunk(type);
            if (chunk != null) {
                chunk.setCount(16);
                if (player.getInventory().insertStack(chunk)) given++;
            }

            ItemStack core = SlayerItems.createCore(type);
            if (core != null && player.getInventory().insertStack(core)) given++;
        }

        // HPEBM weapons
        for (int tier = 1; tier <= 7; tier++) {
            ItemStack beam = CustomItemHandler.createHPEBM(tier);
            if (beam != null && player.getInventory().insertStack(beam)) given++;
        }

        player.sendMessage(Text.literal("§a✓ Given " + given + " items!"), false);
    }

    // ============================================================
    // CUSTOM ENCHANTMENTS ADMIN MENU
    // ============================================================

    public static void openCustomEnchantmentsMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("✦ Custom Enchantments").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));

        // Background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.ENCHANTED_BOOK)
                .setName(Text.literal("✦ Custom Enchantments").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Hold an item and click to apply").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Left-click: Level 1  |  Right-click: Max Level").formatted(Formatting.YELLOW))
                .build());

        // ── OFFENSIVE ENCHANTMENTS ───────────────────────────────
        gui.setSlot(10, new GuiElementBuilder(Items.IRON_SWORD)
                .setName(Text.literal("⚔ Slayer Sharpness").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Max Level: V").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("+15% damage per level to all mobs").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Left: Lvl 1  |  Right: Lvl 5").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> applyEnchant(player, CustomEnchantmentManager.SLAYER_SHARPNESS,
                        type == ClickType.MOUSE_RIGHT ? 5 : 1, "Slayer Sharpness"))
                .build());

        gui.setSlot(11, new GuiElementBuilder(Items.WITHER_SKELETON_SKULL)
                .setName(Text.literal("☠ Boss Hunter").formatted(Formatting.DARK_RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Max Level: III").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("+25% damage per level vs slayer bosses").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Left: Lvl 1  |  Right: Lvl 3").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> applyEnchant(player, CustomEnchantmentManager.BOSS_HUNTER,
                        type == ClickType.MOUSE_RIGHT ? 3 : 1, "Boss Hunter"))
                .build());

        gui.setSlot(12, new GuiElementBuilder(Items.ZOMBIE_HEAD)
                .setName(Text.literal("☠ Mob Bane").formatted(Formatting.DARK_GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Max Level: III").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("+20% damage per level vs matching mob type").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Left: Lvl 1  |  Right: Lvl 3").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> applyEnchant(player, CustomEnchantmentManager.MOB_BANE,
                        type == ClickType.MOUSE_RIGHT ? 3 : 1, "Mob Bane"))
                .build());

        gui.setSlot(13, new GuiElementBuilder(Items.SPIDER_EYE)
                .setName(Text.literal("🩸 Vampiric").formatted(Formatting.DARK_RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Max Level: I").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Heals on kill").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Left/Right: Lvl 1").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> applyEnchant(player, CustomEnchantmentManager.VAMPIRIC,
                        1, "Vampiric"))
                .build());

        gui.setSlot(14, new GuiElementBuilder(Items.LIGHTNING_ROD)
                .setName(Text.literal("⚡ Thunderbolt").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Max Level: I").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Strikes lightning on hit").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Left/Right: Lvl 1").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> applyEnchant(player, CustomEnchantmentManager.THUNDERBOLT,
                        1, "Thunderbolt"))
                .build());

        // ── DEFENSIVE ENCHANTMENTS ────────────────────────────────
        gui.setSlot(19, new GuiElementBuilder(Items.SHIELD)
                .setName(Text.literal("🛡 Bounty Protection").formatted(Formatting.BLUE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Max Level: IV").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("-5% all damage per level").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Left: Lvl 1  |  Right: Lvl 4").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> applyEnchant(player, CustomEnchantmentManager.BOUNTY_PROTECTION,
                        type == ClickType.MOUSE_RIGHT ? 4 : 1, "Bounty Protection"))
                .build());

        gui.setSlot(20, new GuiElementBuilder(Items.TOTEM_OF_UNDYING)
                .setName(Text.literal("🛡 Boss Ward").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Max Level: III").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("-10% boss damage per level").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Left: Lvl 1  |  Right: Lvl 3").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> applyEnchant(player, CustomEnchantmentManager.BOSS_WARD,
                        type == ClickType.MOUSE_RIGHT ? 3 : 1, "Boss Ward"))
                .build());

        gui.setSlot(21, new GuiElementBuilder(Items.GOLDEN_APPLE)
                .setName(Text.literal("❤ Vitality").formatted(Formatting.DARK_RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Max Level: III").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("+4 max HP per level").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Left: Lvl 1  |  Right: Lvl 3").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> applyEnchant(player, CustomEnchantmentManager.VITALITY,
                        type == ClickType.MOUSE_RIGHT ? 3 : 1, "Vitality"))
                .build());

        gui.setSlot(22, new GuiElementBuilder(Items.CACTUS)
                .setName(Text.literal("🌵 Thorns of Vengeance").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Max Level: I").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Reflects damage to attackers").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Left/Right: Lvl 1").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> applyEnchant(player, CustomEnchantmentManager.THORNS_OF_VENGEANCE,
                        1, "Thorns of Vengeance"))
                .build());

        // Back button
        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openCustomItemsAdminGui(player))
                .build());

        gui.open();
    }

    private static void applyEnchant(ServerPlayerEntity player, String enchant, int level, String name) {
        ItemStack held = player.getMainHandStack();
        if (held.isEmpty()) {
            player.sendMessage(Text.literal("✗ Hold an item in your main hand first!").formatted(Formatting.RED), false);
            return;
        }
        CustomEnchantmentManager.addEnchantment(held, enchant, level);
        CustomEnchantmentManager.addEnchantmentLore(held);
        String roman = switch (level) {
            case 1 -> "I"; case 2 -> "II"; case 3 -> "III"; case 4 -> "IV"; case 5 -> "V"; default -> String.valueOf(level);
        };
        player.sendMessage(Text.literal("✦ Applied ").formatted(Formatting.GOLD)
                .append(Text.literal(name + " " + roman).formatted(Formatting.AQUA, Formatting.BOLD))
                .append(Text.literal(" to held item!").formatted(Formatting.GOLD)), false);
    }

    // ═══════════════════════════════════════════════════════════
    // GAME SETTINGS MAIN PAGE
    // ═══════════════════════════════════════════════════════════

    private static void openGameSettingsPage(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X5, player, false);
        gui.setTitle(Text.literal("⚙ Game Settings"));
        fillBackground(gui);

        gui.setSlot(4, new GuiElementBuilder(Items.COMPARATOR)
                .setName(Text.literal("⚙ Game Settings").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal("Configure all custom game systems").formatted(Formatting.GRAY))
                .glow().build());

        gui.setSlot(10, new GuiElementBuilder(Items.WITHER_SKELETON_SKULL)
                .setName(Text.literal("Boss Settings").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal("• HP multipliers per boss type").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("• Damage multipliers").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("• Kill requirements multiplier").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("• Ability toggle per type").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("")).addLoreLine(Text.literal("Click to configure").formatted(Formatting.YELLOW))
                .setCallback((i, t, a) -> openBossSettingsPage(player)).build());

        gui.setSlot(12, new GuiElementBuilder(Items.DIAMOND_HELMET)
                .setName(Text.literal("Item Settings").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal("• Level requirements per item").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("• Ability toggles per item").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("")).addLoreLine(Text.literal("Click to configure").formatted(Formatting.YELLOW))
                .setCallback((i, t, a) -> openItemSettingsPage(player)).build());

        gui.setSlot(14, new GuiElementBuilder(Items.SHIELD)
                .setName(Text.literal("Defence Settings").formatted(Formatting.BLUE, Formatting.BOLD))
                .addLoreLine(Text.literal("• Armour defence cap").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("• Set bonus percentages (T1/T2)").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("• Weapon bonus percentages").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("")).addLoreLine(Text.literal("Click to configure").formatted(Formatting.YELLOW))
                .setCallback((i, t, a) -> openDefenceSettingsPage(player)).build());

        gui.setSlot(16, new GuiElementBuilder(Items.CHEST)
                .setName(Text.literal("Drop Settings").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal("• Chunk drop rate multiplier").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("• Core drop rate multiplier").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("• Coin reward multiplier").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("")).addLoreLine(Text.literal("Click to configure").formatted(Formatting.YELLOW))
                .setCallback((i, t, a) -> openDropSettingsPage(player)).build());

        gui.setSlot(28, new GuiElementBuilder(Items.ZOMBIE_HEAD)
                .setName(Text.literal("Mob Settings").formatted(Formatting.DARK_GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal("• Health scaling multiplier").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("")).addLoreLine(Text.literal("Click to configure").formatted(Formatting.YELLOW))
                .setCallback((i, t, a) -> openMobSettingsPage(player)).build());

        gui.setSlot(30, new GuiElementBuilder(Items.OAK_SIGN)
                .setName(Text.literal("Scoreboard Settings").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal("• Toggle scoreboard sections on/off").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("")).addLoreLine(Text.literal("Click to configure").formatted(Formatting.YELLOW))
                .setCallback((i, t, a) -> openScoreboardSettingsPage(player)).build());

        addBackButton(gui, player);
        gui.open();
    }

    // ═══════════════════════════════════════════════════════════
    // BOSS SETTINGS PAGE
    // ═══════════════════════════════════════════════════════════

    private static void openBossSettingsPage(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("Boss Settings"));
        fillBackground(gui);

        gui.setSlot(4, new GuiElementBuilder(Items.WITHER_SKELETON_SKULL)
                .setName(Text.literal("Boss Settings").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal("Left: +0.1, Right: -0.1, Shift: ±0.5").formatted(Formatting.GRAY))
                .glow().build());

        SlayerManager.SlayerType[] types = SlayerManager.SlayerType.values();
        int[] hpSlots =   {10, 11, 12, 13, 14, 15, 16};
        int[] dmgSlots =  {19, 20, 21, 22, 23, 24, 25};
        int[] abilSlots = {28, 29, 30, 31, 32, 33, 34};

        for (int i = 0; i < types.length && i < 7; i++) {
            SlayerManager.SlayerType t = types[i];
            float hpMult  = DataManager.getBossHpMultiplier(t);
            float dmgMult = DataManager.getBossDamageMultiplier(t);
            boolean abilEnabled = DataManager.isBossAbilitiesEnabled(t);

            gui.setSlot(hpSlots[i], new GuiElementBuilder(Items.RED_STAINED_GLASS_PANE)
                    .setName(Text.literal(t.displayName + " HP").formatted(Formatting.RED, Formatting.BOLD))
                    .addLoreLine(Text.literal("Multiplier: §f" + String.format("%.2f", hpMult) + "x").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("L: +0.1 | R: -0.1 | Shift: ±0.5").formatted(Formatting.DARK_GRAY))
                    .addLoreLine(Text.literal("Middle-click: set exact value").formatted(Formatting.AQUA))
                    .setCallback((idx, clickType, action) -> {
                        if (clickType == eu.pb4.sgui.api.ClickType.MOUSE_MIDDLE) {
                            openBossMultiplierSign(player, t, true, DataManager.getBossHpMultiplier(t));
                            return;
                        }
                        float delta = clickType.isLeft ? 0.1f : -0.1f;
                        if (clickType.shift) delta *= 5;
                        DataManager.setBossHpMultiplier(t, Math.max(0.1f, DataManager.getBossHpMultiplier(t) + delta));
                        DataManager.save(PoliticalServer.server);
                        openBossSettingsPage(player);
                    }).build());

            gui.setSlot(dmgSlots[i], new GuiElementBuilder(Items.ORANGE_STAINED_GLASS_PANE)
                    .setName(Text.literal(t.displayName + " DMG").formatted(Formatting.GOLD, Formatting.BOLD))
                    .addLoreLine(Text.literal("Multiplier: §f" + String.format("%.2f", dmgMult) + "x").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("L: +0.1 | R: -0.1 | Shift: ±0.5").formatted(Formatting.DARK_GRAY))
                    .addLoreLine(Text.literal("Middle-click: set exact value").formatted(Formatting.AQUA))
                    .setCallback((idx, clickType, action) -> {
                        if (clickType == eu.pb4.sgui.api.ClickType.MOUSE_MIDDLE) {
                            openBossMultiplierSign(player, t, false, DataManager.getBossDamageMultiplier(t));
                            return;
                        }
                        float delta = clickType.isLeft ? 0.1f : -0.1f;
                        if (clickType.shift) delta *= 5;
                        DataManager.setBossDamageMultiplier(t, Math.max(0.1f, DataManager.getBossDamageMultiplier(t) + delta));
                        DataManager.save(PoliticalServer.server);
                        openBossSettingsPage(player);
                    }).build());

            gui.setSlot(abilSlots[i], new GuiElementBuilder(abilEnabled ? Items.LIME_STAINED_GLASS_PANE : Items.RED_STAINED_GLASS_PANE)
                    .setName(Text.literal(t.displayName + " Abilities").formatted(abilEnabled ? Formatting.GREEN : Formatting.RED, Formatting.BOLD))
                    .addLoreLine(Text.literal("Status: " + (abilEnabled ? "§aENABLED" : "§cDISABLED")).formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("Click to toggle").formatted(Formatting.YELLOW))
                    .setCallback((idx, clickType, action) -> {
                        DataManager.setBossAbilitiesEnabled(t, !DataManager.isBossAbilitiesEnabled(t));
                        DataManager.save(PoliticalServer.server);
                        openBossSettingsPage(player);
                    }).build());
        }

        float killMult = DataManager.getBossKillsRequiredMultiplier();
        gui.setSlot(40, new GuiElementBuilder(Items.BOOK)
                .setName(Text.literal("Kill Requirement Multiplier").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal("Global multiplier: §f" + String.format("%.2f", killMult) + "x").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("L: +0.1 | R: -0.1 | Shift: ±0.5").formatted(Formatting.DARK_GRAY))
                .addLoreLine(Text.literal("Middle-click: set exact value").formatted(Formatting.AQUA))
                .setCallback((idx, clickType, action) -> {
                    if (clickType == eu.pb4.sgui.api.ClickType.MOUSE_MIDDLE) {
                        openKillMultiplierSign(player, DataManager.getBossKillsRequiredMultiplier());
                        return;
                    }
                    float delta = clickType.isLeft ? 0.1f : -0.1f;
                    if (clickType.shift) delta *= 5;
                    DataManager.setBossKillsRequiredMultiplier(Math.max(0.1f, DataManager.getBossKillsRequiredMultiplier() + delta));
                    DataManager.save(PoliticalServer.server);
                    openBossSettingsPage(player);
                }).build());

        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.GRAY))
                .setCallback((i, t, a) -> openGameSettingsPage(player)).build());
        gui.open();
    }

    private static void openBossMultiplierSign(ServerPlayerEntity player, SlayerManager.SlayerType slayerType, boolean isHp, float current) {
        String label = isHp ? (slayerType.displayName + " HP multiplier") : (slayerType.displayName + " DMG multiplier");
        SignGui signGui = new SignGui(player) {
            @Override
            public void onClose() {
                String raw = this.getLine(0).getString().trim().replaceAll("[^0-9.]", "");
                if (raw.isEmpty()) { openBossSettingsPage(player); return; }
                try {
                    float val = Float.parseFloat(raw);
                    val = Math.max(0.1f, val);
                    if (isHp) DataManager.setBossHpMultiplier(slayerType, val);
                    else DataManager.setBossDamageMultiplier(slayerType, val);
                    DataManager.save(PoliticalServer.server);
                    player.sendMessage(Text.literal("✓ Set " + label + " to " + String.format("%.2f", val) + "x!").formatted(Formatting.GREEN));
                } catch (NumberFormatException ignored) {
                    player.sendMessage(Text.literal("✗ Invalid number!").formatted(Formatting.RED));
                }
                openBossSettingsPage(player);
            }
        };
        signGui.setLine(0, Text.literal(""));
        signGui.setLine(1, Text.literal("Set " + label));
        signGui.setLine(2, Text.literal("(e.g. 2.5)"));
        signGui.setLine(3, Text.literal("Current: " + String.format("%.2f", current)));
        signGui.open();
    }

    private static void openKillMultiplierSign(ServerPlayerEntity player, float current) {
        SignGui signGui = new SignGui(player) {
            @Override
            public void onClose() {
                String raw = this.getLine(0).getString().trim().replaceAll("[^0-9.]", "");
                if (raw.isEmpty()) { openBossSettingsPage(player); return; }
                try {
                    float val = Math.max(0.1f, Float.parseFloat(raw));
                    DataManager.setBossKillsRequiredMultiplier(val);
                    DataManager.save(PoliticalServer.server);
                    player.sendMessage(Text.literal("✓ Kill requirement multiplier set to " + String.format("%.2f", val) + "x!").formatted(Formatting.GREEN));
                } catch (NumberFormatException ignored) {
                    player.sendMessage(Text.literal("✗ Invalid number!").formatted(Formatting.RED));
                }
                openBossSettingsPage(player);
            }
        };
        signGui.setLine(0, Text.literal(""));
        signGui.setLine(1, Text.literal("Kill multiplier"));
        signGui.setLine(2, Text.literal("(e.g. 1.5)"));
        signGui.setLine(3, Text.literal("Current: " + String.format("%.2f", current)));
        signGui.open();
    }

    // ═══════════════════════════════════════════════════════════
    // ITEM SETTINGS PAGE
    // ═══════════════════════════════════════════════════════════

    private static void openItemSettingsPage(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("Item Settings"));
        fillBackground(gui);

        gui.setSlot(4, new GuiElementBuilder(Items.DIAMOND_HELMET)
                .setName(Text.literal("Item Level Requirements").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal("L: +1 level | R: -1 | Shift: ±5").formatted(Formatting.GRAY))
                .glow().build());

        Object[][] itemConfigs = {
            {"voidwalker_crown",        "Voidwalker's Crown",   Items.RECOVERY_COMPASS,  7},
            {"zombie_berserker_helmet", "Berserker Helmet",     Items.ZOMBIE_HEAD,        7},
            {"spider_leggings",         "Spider Leggings",      Items.SPIDER_EYE,         5},
            {"slime_boots",             "Slime Boots",          Items.SLIME_BALL,         5},
            {"warden_chestplate",       "Warden Chestplate",    Items.SCULK_SENSOR,       8},
            {"zombie_t1_helmet",        "Zombie T1 Helm",       Items.CHAINMAIL_HELMET,   1},
            {"zombie_t2_helmet",        "Zombie T2 Helm",       Items.IRON_HELMET,        5},
            {"spider_t1_helmet",        "Spider T1 Helm",       Items.CHAINMAIL_HELMET,   1},
            {"spider_t2_helmet",        "Spider T2 Helm",       Items.IRON_HELMET,        5},
        };

        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20};
        for (int i = 0; i < itemConfigs.length && i < slots.length; i++) {
            String itemId = (String) itemConfigs[i][0];
            String dispName = (String) itemConfigs[i][1];
            Item icon = (Item) itemConfigs[i][2];
            int defLevel = (int) itemConfigs[i][3];
            int curLevel = DataManager.getItemLevelRequirement(itemId, defLevel);

            gui.setSlot(slots[i], new GuiElementBuilder(icon)
                    .setName(Text.literal(dispName).formatted(Formatting.AQUA, Formatting.BOLD))
                    .addLoreLine(Text.literal("Level req: §f" + curLevel + " §8(default: " + defLevel + ")").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("L: +1 | R: -1 | Shift: ±5").formatted(Formatting.DARK_GRAY))
                    .setCallback((idx, clickType, action) -> {
                        int delta = clickType.isLeft ? 1 : -1;
                        if (clickType.shift) delta *= 5;
                        DataManager.setItemLevelRequirement(itemId, DataManager.getItemLevelRequirement(itemId, defLevel) + delta);
                        DataManager.save(PoliticalServer.server);
                        openItemSettingsPage(player);
                    }).build());
        }

        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.GRAY))
                .setCallback((i, t, a) -> openGameSettingsPage(player)).build());
        gui.open();
    }

    // ═══════════════════════════════════════════════════════════
    // DEFENCE SETTINGS PAGE
    // ═══════════════════════════════════════════════════════════

    private static void openDefenceSettingsPage(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X4, player, false);
        gui.setTitle(Text.literal("Defence Settings"));
        fillBackground(gui);

        gui.setSlot(4, new GuiElementBuilder(Items.SHIELD)
                .setName(Text.literal("Defence Settings").formatted(Formatting.BLUE, Formatting.BOLD))
                .addLoreLine(Text.literal("L: +1% | R: -1% | Shift: ±5%").formatted(Formatting.GRAY))
                .glow().build());

        int defCap = DataManager.getDefenceCapPct();
        gui.setSlot(10, new GuiElementBuilder(Items.IRON_CHESTPLATE)
                .setName(Text.literal("Defence Cap").formatted(Formatting.WHITE, Formatting.BOLD))
                .addLoreLine(Text.literal("Current: §f" + defCap + "%").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Max damage reduction from bounty def").formatted(Formatting.DARK_GRAY))
                .addLoreLine(Text.literal("L: +1 | R: -1 | Shift: ±5").formatted(Formatting.DARK_GRAY))
                .setCallback((idx, clickType, action) -> {
                    int delta = clickType.isLeft ? 1 : -1;
                    if (clickType.shift) delta *= 5;
                    DataManager.setDefenceCapPct(DataManager.getDefenceCapPct() + delta);
                    DataManager.save(PoliticalServer.server);
                    openDefenceSettingsPage(player);
                }).build());

        int t1Bonus = DataManager.getT1SetBonusPct();
        gui.setSlot(12, new GuiElementBuilder(Items.CHAINMAIL_CHESTPLATE)
                .setName(Text.literal("T1 Full Set Bonus").formatted(Formatting.WHITE, Formatting.BOLD))
                .addLoreLine(Text.literal("Current: §f" + t1Bonus + "% §7boss-specific def").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("L: +1 | R: -1 | Shift: ±5").formatted(Formatting.DARK_GRAY))
                .setCallback((idx, clickType, action) -> {
                    int delta = clickType.isLeft ? 1 : -1;
                    if (clickType.shift) delta *= 5;
                    DataManager.setT1SetBonusPct(DataManager.getT1SetBonusPct() + delta);
                    DataManager.save(PoliticalServer.server);
                    openDefenceSettingsPage(player);
                }).build());

        int t2Bonus = DataManager.getT2SetBonusPct();
        gui.setSlot(14, new GuiElementBuilder(Items.IRON_CHESTPLATE)
                .setName(Text.literal("T2 Full Set Bonus").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal("Current: §f" + t2Bonus + "% §7boss-specific def").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("L: +1 | R: -1 | Shift: ±5").formatted(Formatting.DARK_GRAY))
                .setCallback((idx, clickType, action) -> {
                    int delta = clickType.isLeft ? 1 : -1;
                    if (clickType.shift) delta *= 5;
                    DataManager.setT2SetBonusPct(DataManager.getT2SetBonusPct() + delta);
                    DataManager.save(PoliticalServer.server);
                    openDefenceSettingsPage(player);
                }).build());

        int t1SwordSpec = DataManager.getData().t1SwordSpecificPct;
        gui.setSlot(20, new GuiElementBuilder(Items.IRON_SWORD)
                .setName(Text.literal("T1 Sword Specific Def").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal("Current: §f" + t1SwordSpec + "% §7vs matching boss").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("L: +1 | R: -1 | Shift: ±5").formatted(Formatting.DARK_GRAY))
                .setCallback((idx, clickType, action) -> {
                    int delta = clickType.isLeft ? 1 : -1;
                    if (clickType.shift) delta *= 5;
                    DataManager.getData().t1SwordSpecificPct = Math.max(0, Math.min(100, DataManager.getData().t1SwordSpecificPct + delta));
                    DataManager.save(PoliticalServer.server);
                    openDefenceSettingsPage(player);
                }).build());

        int t2SwordSpec = DataManager.getData().t2SwordSpecificPct;
        gui.setSlot(22, new GuiElementBuilder(Items.DIAMOND_SWORD)
                .setName(Text.literal("T2 Sword Specific Def").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal("Current: §f" + t2SwordSpec + "% §7vs matching boss").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("L: +1 | R: -1 | Shift: ±5").formatted(Formatting.DARK_GRAY))
                .setCallback((idx, clickType, action) -> {
                    int delta = clickType.isLeft ? 1 : -1;
                    if (clickType.shift) delta *= 5;
                    DataManager.getData().t2SwordSpecificPct = Math.max(0, Math.min(100, DataManager.getData().t2SwordSpecificPct + delta));
                    DataManager.save(PoliticalServer.server);
                    openDefenceSettingsPage(player);
                }).build());

        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.GRAY))
                .setCallback((i, t, a) -> openGameSettingsPage(player)).build());
        gui.open();
    }

    // ═══════════════════════════════════════════════════════════
    // DROP SETTINGS PAGE
    // ═══════════════════════════════════════════════════════════

    private static void openDropSettingsPage(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X4, player, false);
        gui.setTitle(Text.literal("Drop Settings"));
        fillBackground(gui);

        gui.setSlot(4, new GuiElementBuilder(Items.CHEST)
                .setName(Text.literal("Drop Settings").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal("L: +0.1x | R: -0.1x | Shift: ±0.5x").formatted(Formatting.GRAY))
                .glow().build());

        float chunkMult = DataManager.getChunkDropMultiplier();
        gui.setSlot(10, new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal("Chunk Drop Rate Multiplier").formatted(Formatting.WHITE, Formatting.BOLD))
                .addLoreLine(Text.literal("Current: §f" + String.format("%.1f", chunkMult) + "x").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Multiplies chunk drop chance").formatted(Formatting.DARK_GRAY))
                .addLoreLine(Text.literal("L: +0.1 | R: -0.1 | Shift: ±0.5").formatted(Formatting.DARK_GRAY))
                .setCallback((idx, clickType, action) -> {
                    float delta = clickType.isLeft ? 0.1f : -0.1f;
                    if (clickType.shift) delta *= 5;
                    DataManager.setChunkDropMultiplier(DataManager.getChunkDropMultiplier() + delta);
                    DataManager.save(PoliticalServer.server);
                    openDropSettingsPage(player);
                }).build());

        float coreMult = DataManager.getCoreDropMultiplier();
        gui.setSlot(12, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Text.literal("Core Drop Rate Multiplier").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal("Current: §f" + String.format("%.1f", coreMult) + "x").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Multiplies core drop chance").formatted(Formatting.DARK_GRAY))
                .addLoreLine(Text.literal("L: +0.1 | R: -0.1 | Shift: ±0.5").formatted(Formatting.DARK_GRAY))
                .setCallback((idx, clickType, action) -> {
                    float delta = clickType.isLeft ? 0.1f : -0.1f;
                    if (clickType.shift) delta *= 5;
                    DataManager.setCoreDropMultiplier(DataManager.getCoreDropMultiplier() + delta);
                    DataManager.save(PoliticalServer.server);
                    openDropSettingsPage(player);
                }).build());

        float coinMult = DataManager.getCoinRewardMultiplier();
        gui.setSlot(14, new GuiElementBuilder(Items.GOLD_NUGGET)
                .setName(Text.literal("Coin Reward Multiplier").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal("Current: §f" + String.format("%.1f", coinMult) + "x").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Multiplies coin rewards from kills").formatted(Formatting.DARK_GRAY))
                .addLoreLine(Text.literal("L: +0.1 | R: -0.1 | Shift: ±0.5").formatted(Formatting.DARK_GRAY))
                .setCallback((idx, clickType, action) -> {
                    float delta = clickType.isLeft ? 0.1f : -0.1f;
                    if (clickType.shift) delta *= 5;
                    DataManager.setCoinRewardMultiplier(DataManager.getCoinRewardMultiplier() + delta);
                    DataManager.save(PoliticalServer.server);
                    openDropSettingsPage(player);
                }).build());

        float fleshMult = DataManager.getFleshDropMultiplier();
        gui.setSlot(16, new GuiElementBuilder(Items.ROTTEN_FLESH)
                .setName(Text.literal("Flesh Drop Rate Multiplier").formatted(Formatting.DARK_GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal("Current: §f" + String.format("%.1f", fleshMult) + "x").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Multiplies flesh drop chance (90% base)").formatted(Formatting.DARK_GRAY))
                .addLoreLine(Text.literal("Affects Zombie & Piglin bosses").formatted(Formatting.DARK_GRAY))
                .addLoreLine(Text.literal("L: +0.1 | R: -0.1 | Shift: ±0.5").formatted(Formatting.DARK_GRAY))
                .setCallback((idx, clickType, action) -> {
                    float delta = clickType.isLeft ? 0.1f : -0.1f;
                    if (clickType.shift) delta *= 5;
                    DataManager.setFleshDropMultiplier(DataManager.getFleshDropMultiplier() + delta);
                    DataManager.save(PoliticalServer.server);
                    openDropSettingsPage(player);
                }).build());

        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.GRAY))
                .setCallback((i, t, a) -> openGameSettingsPage(player)).build());
        gui.open();
    }

    // ═══════════════════════════════════════════════════════════
    // MOB SETTINGS PAGE
    // ═══════════════════════════════════════════════════════════

    private static void openMobSettingsPage(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X4, player, false);
        gui.setTitle(Text.literal("Mob Settings"));
        fillBackground(gui);

        gui.setSlot(4, new GuiElementBuilder(Items.ZOMBIE_HEAD)
                .setName(Text.literal("Mob Settings").formatted(Formatting.DARK_GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal("L: +0.1x | R: -0.1x | Shift: ±0.5x").formatted(Formatting.GRAY))
                .glow().build());

        float healthMult = DataManager.getMobHealthScalingMultiplier();
        gui.setSlot(12, new GuiElementBuilder(Items.APPLE)
                .setName(Text.literal("Health Scaling Multiplier").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal("Current: §f" + String.format("%.1f", healthMult) + "x").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Multiplies health of all scaled mobs").formatted(Formatting.DARK_GRAY))
                .addLoreLine(Text.literal("L: +0.1 | R: -0.1 | Shift: ±0.5").formatted(Formatting.DARK_GRAY))
                .setCallback((idx, clickType, action) -> {
                    float delta = clickType.isLeft ? 0.1f : -0.1f;
                    if (clickType.shift) delta *= 5;
                    DataManager.setMobHealthScalingMultiplier(DataManager.getMobHealthScalingMultiplier() + delta);
                    DataManager.save(PoliticalServer.server);
                    openMobSettingsPage(player);
                }).build());

        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.GRAY))
                .setCallback((i, t, a) -> openGameSettingsPage(player)).build());
        gui.open();
    }

    // ═══════════════════════════════════════════════════════════
    // SCOREBOARD SETTINGS PAGE
    // ═══════════════════════════════════════════════════════════

    private static void openScoreboardSettingsPage(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X4, player, false);
        gui.setTitle(Text.literal("Scoreboard Settings"));
        fillBackground(gui);

        gui.setSlot(4, new GuiElementBuilder(Items.OAK_SIGN)
                .setName(Text.literal("Scoreboard Settings").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal("Toggle scoreboard sections").formatted(Formatting.GRAY))
                .glow().build());

        boolean showAuction = DataManager.isScoreboardShowAuction();
        gui.setSlot(11, new GuiElementBuilder(showAuction ? Items.LIME_DYE : Items.GRAY_DYE)
                .setName(Text.literal("Show Auction Timer").formatted(showAuction ? Formatting.GREEN : Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal("Status: " + (showAuction ? "§aVISIBLE" : "§cHIDDEN")).formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Click to toggle").formatted(Formatting.YELLOW))
                .setCallback((idx, clickType, action) -> {
                    DataManager.setScoreboardShowAuction(!DataManager.isScoreboardShowAuction());
                    DataManager.save(PoliticalServer.server);
                    openScoreboardSettingsPage(player);
                }).build());

        boolean showBounty = DataManager.isScoreboardShowBountyGear();
        gui.setSlot(13, new GuiElementBuilder(showBounty ? Items.LIME_DYE : Items.GRAY_DYE)
                .setName(Text.literal("Show Bounty Gear Stats").formatted(showBounty ? Formatting.GREEN : Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal("Status: " + (showBounty ? "§aVISIBLE" : "§cHIDDEN")).formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Click to toggle").formatted(Formatting.YELLOW))
                .setCallback((idx, clickType, action) -> {
                    DataManager.setScoreboardShowBountyGear(!DataManager.isScoreboardShowBountyGear());
                    DataManager.save(PoliticalServer.server);
                    openScoreboardSettingsPage(player);
                }).build());

        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.GRAY))
                .setCallback((i, t, a) -> openGameSettingsPage(player)).build());
        gui.open();
    }

    // ═══════════════════════════════════════════════════════════
    // PERK MANAGEMENT PAGE
    // ═══════════════════════════════════════════════════════════

    private static void openPerkManagementPage(ServerPlayerEntity player) {
        java.util.Collection<Perk> allPerks = PerkManager.getAllPerks();
        java.util.List<String> activePerks = PerkManager.getActivePerks();

        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("✦ Perk Management"));
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE).setName(Text.literal("")).build());
        }

        gui.setSlot(4, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Text.literal("✦ Perk Management").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal("Active: §a" + activePerks.size() + " §7/ §f" + allPerks.size()).formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Left: Activate | Right: Deactivate").formatted(Formatting.DARK_GRAY))
                .glow().build());

        // Reset All Perks button
        gui.setSlot(8, new GuiElementBuilder(Items.TNT)
                .setName(Text.literal("Reset All Perks").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal("Deactivates ALL perks").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("▶ Click to reset").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> {
                    PerkManager.clearAllPerks();
                    DataManager.save(PoliticalServer.server);
                    player.sendMessage(Text.literal("✓ All perks reset!").formatted(Formatting.RED));
                    openPerkManagementPage(player);
                }).build());

        int slot = 10;
        for (Perk perk : allPerks) {
            if (slot >= 53) break;
            if (slot % 9 == 0) slot++;
            if (slot % 9 == 8) { slot++; continue; }

            boolean active = activePerks.contains(perk.id);
            net.minecraft.item.Item perkIcon = active ? Items.LIME_DYE : Items.GRAY_DYE;
            final Perk finalPerk = perk;
            gui.setSlot(slot, new GuiElementBuilder(perkIcon)
                    .setName(Text.literal(perk.name).formatted(active ? Formatting.GREEN : Formatting.GRAY, Formatting.BOLD))
                    .addLoreLine(Text.literal("Status: " + (active ? "§aACTIVE" : "§cINACTIVE")).formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("ID: §7" + perk.id).formatted(Formatting.DARK_GRAY))
                    .addLoreLine(Text.literal(active ? "Right-click: Deactivate" : "Left-click: Activate").formatted(Formatting.YELLOW))
                    .setCallback((idx, type, clickAction) -> {
                        java.util.List<String> current = new java.util.ArrayList<>(PerkManager.getActivePerks());
                        if (current.contains(finalPerk.id)) {
                            current.remove(finalPerk.id);
                        } else {
                            current.add(finalPerk.id);
                        }
                        PerkManager.setActivePerks(current);
                        PerkManager.applyActivePerks(player);
                        DataManager.save(PoliticalServer.server);
                        openPerkManagementPage(player);
                    }).build());
            slot++;
        }

        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.GRAY))
                .setCallback((i, t, a) -> openMainPage(player)).build());
        gui.open();
    }

    // ═══════════════════════════════════════════════════════════
    // GIVE COINS SELECTOR — sign-based input
    // ═══════════════════════════════════════════════════════════

    private static void openGiveCoinsSelector(ServerPlayerEntity admin, ServerPlayerEntity target) {
        int currentCoins = CoinManager.getCoins(target);
        SignGui signGui = new SignGui(admin) {
            @Override
            public void onClose() {
                // allow negative to mean "remove" — e.g. "-500" removes 500 coins
                Integer amount = parseSignAmount(this.getLine(0).getString(), true);
                if (amount == null) { openPlayersPage(admin); return; }
                if (amount > 0) {
                    CoinManager.giveCoins(target, amount);
                    admin.sendMessage(Text.literal("✓ Gave " + amount + " coins to " + target.getName().getString() + "!").formatted(Formatting.GREEN));
                } else if (amount < 0) {
                    CoinManager.removeCoins(target, -amount);
                    admin.sendMessage(Text.literal("✓ Removed " + (-amount) + " coins from " + target.getName().getString() + "!").formatted(Formatting.RED));
                }
                openPlayersPage(admin);
            }
        };
        signGui.setLine(0, Text.literal(""));
        signGui.setLine(1, Text.literal("+ give / - remove"));
        signGui.setLine(2, Text.literal(target.getName().getString()));
        signGui.setLine(3, Text.literal("Coins: " + currentCoins));
        signGui.open();
    }

    // ═══════════════════════════════════════════════════════════
    // SLAYER LEVEL SELECTOR — sign-based input per type
    // ═══════════════════════════════════════════════════════════

    private static void openSlayerLevelSelector(ServerPlayerEntity admin, ServerPlayerEntity target) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X5, admin, false);
        gui.setTitle(Text.literal("Slayer Levels — " + target.getName().getString()));
        fillBackground(gui);

        gui.setSlot(4, new GuiElementBuilder(Items.EXPERIENCE_BOTTLE)
                .setName(Text.literal(target.getName().getString()).formatted(Formatting.WHITE, Formatting.BOLD))
                .addLoreLine(Text.literal("Click a type to set its level via sign").formatted(Formatting.GRAY))
                .glow().build());

        SlayerManager.SlayerType[] types = SlayerManager.SlayerType.values();
        int[] slotPositions = {10, 11, 12, 13, 14, 15, 16};
        for (int i = 0; i < types.length && i < slotPositions.length; i++) {
            SlayerManager.SlayerType t = types[i];
            int currentLevel = SlayerData.getSlayerLevel(target.getUuidAsString(), t);
            final SlayerManager.SlayerType finalType = t;
            gui.setSlot(slotPositions[i], new GuiElementBuilder(Items.BOOK)
                    .setName(Text.literal(t.displayName + " Lvl " + currentLevel).formatted(Formatting.AQUA, Formatting.BOLD))
                    .addLoreLine(Text.literal("Current: §f" + currentLevel).formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("▶ Click to set level").formatted(Formatting.YELLOW))
                    .setCallback((idx, clickType, action) -> {
                        openSlayerLevelSign(admin, target, finalType);
                    }).build());        }

        addBackButton(gui, admin);
        gui.open();
    }

    private static void openSlayerLevelSign(ServerPlayerEntity admin, ServerPlayerEntity target, SlayerManager.SlayerType slayerType) {
        int currentLevel = SlayerData.getSlayerLevel(target.getUuidAsString(), slayerType);
        SignGui signGui = new SignGui(admin) {
            @Override
            public void onClose() {
                String raw = this.getLine(0).getString().trim().replaceAll("[^0-9]", "");
                if (raw.isEmpty()) { openSlayerLevelSelector(admin, target); return; }
                try {
                    int newLevel = Integer.parseInt(raw);
                    if (newLevel < 0) { admin.sendMessage(Text.literal("✗ Level must be >= 0!").formatted(Formatting.RED)); openSlayerLevelSelector(admin, target); return; }
                    SlayerData.setSlayerLevel(target.getUuidAsString(), slayerType, newLevel);
                    DataManager.save(PoliticalServer.server);
                    admin.sendMessage(Text.literal("✓ Set " + target.getName().getString() + "'s " + slayerType.displayName + " level to " + newLevel + "!").formatted(Formatting.GREEN));
                } catch (NumberFormatException ignored) {
                    admin.sendMessage(Text.literal("✗ Invalid number!").formatted(Formatting.RED));
                }
                openSlayerLevelSelector(admin, target);
            }
        };
        signGui.setLine(0, Text.literal(""));
        signGui.setLine(1, Text.literal(slayerType.displayName + " Level"));
        signGui.setLine(2, Text.literal(target.getName().getString()));
        signGui.setLine(3, Text.literal("Current: " + currentLevel));
        signGui.open();
    }}
