package com.political;


import com.political.economy.UndergroundAuctionManager;
import com.political.government.DictatorManager;
import com.political.government.ElectionManager;
import com.political.government.Perk;
import com.political.government.PerkManager;
import com.political.stats.HealthScalingManager;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SignGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.*;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import java.util.List;
import java.util.ArrayList;
import java.util.List;
import eu.pb4.sgui.api.ClickType;
import java.util.Map;
import java.util.Random;
import java.util.Set;import com.political.bounty.SlayerManager;
import com.political.bounty.SlayerData;
import com.political.bounty.SlayerItems;import net.minecraft.world.item.ItemStack;import net.minecraft.world.phys.Vec3;import eu.pb4.sgui.api.ClickType;import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.ArrayList;
import java.util.List;

import com.political.bounty.SlayerManager;
import com.political.bounty.SlayerData;
import com.political.bounty.SlayerItems;
import com.political.economy.CoinManager;
import com.political.items.CustomItemHandler;
import com.political.PoliticalServer;import eu.pb4.sgui.api.elements.GuiElement;

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
    public static void open(ServerPlayer player) {
        openPage(player, AdminPage.MAIN);
    }

    public static void openPage(ServerPlayer player, AdminPage page) {
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

    private static void openMainPage(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x5, player, false);
        gui.setTitle(Component.literal("✦ Admin Control Panel ✦"));

        // Fill with colored glass borders (cyan border, gray interior)
        fillBackground(gui);
        gui.setSlot(4, new GuiElementBuilder(Items.COMMAND_BLOCK)
                .setName(Component.literal("★ Admin Control Panel ★").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Server Administration Hub").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Select a category below").withStyle(ChatFormatting.DARK_GRAY))
                .glow()
                .build());

        // Category buttons
        gui.setSlot(10, new GuiElementBuilder(Items.BELL)
                .setName(Component.literal("Elections").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("• Start/End elections").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("• Toggle election system").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("• Reset impeachment").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to open").withStyle(ChatFormatting.YELLOW))
                .setCallback((int index, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openPage(player, AdminPage.ELECTIONS))
                .build());

        gui.setSlot(12, new GuiElementBuilder(Items.GOLDEN_HELMET)
                .setName(Component.literal("Government").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("• Set Chair/Vice/Judge").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("• Manage dictator").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("• Reset perks").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to open").withStyle(ChatFormatting.YELLOW))
                .setCallback((int index, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openPage(player, AdminPage.GOVERNMENT))
                .build());

        gui.setSlot(14, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Component.literal("Player Management").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("• Exile/Smite players").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("• Prison controls").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("• Give credits").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to open").withStyle(ChatFormatting.YELLOW))
                .setCallback((int index, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openPage(player, AdminPage.PLAYERS))
                .build());

        gui.setSlot(16, new GuiElementBuilder(Items.GOLD_BLOCK)
                .setName(Component.literal("Economy").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("• Tax system").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("• View debtors").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("• Manage credits/coins").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to open").withStyle(ChatFormatting.YELLOW))
                .setCallback((int index, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openPage(player, AdminPage.ECONOMY))
                .build());

        gui.setSlot(28, new GuiElementBuilder(Items.COMPASS)
                .setName(Component.literal("World Settings").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("• Set spawn location").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("• Manage NPCs").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to open").withStyle(ChatFormatting.YELLOW))
                .setCallback((int index, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openPage(player, AdminPage.WORLD))
                .build());
        gui.setSlot(22, new GuiElementBuilder(Items.IRON_SWORD)
                .setName(Component.literal("⚔ Bounty Admin").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Manage the bounty system").withStyle(ChatFormatting.GRAY))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openSlayerAdminGui(player))
                .build());
        gui.setSlot(30, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Component.literal("✦ Custom Items").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Give custom items to yourself").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("• Armor, swords, legendary gear").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("• Special weapons & materials").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to open").withStyle(ChatFormatting.YELLOW))
                .glow()
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openCustomItemsPage(player))
                .build());
        // Server status
        String chair = DataManager.getChair();
        String viceChair = DataManager.getViceChair();
        boolean dictatorActive = DictatorManager.isDictatorActive();

        gui.setSlot(31, new GuiElementBuilder(Items.BOOK)
                .setName(Component.literal("Server Status").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Chair: " + (chair != null ? DataManager.getPlayerName(chair) : "None")).withStyle(ChatFormatting.GOLD))
                .addLoreLine(Component.literal("Vice Chair: " + (viceChair != null ? DataManager.getPlayerName(viceChair) : "None")).withStyle(ChatFormatting.AQUA))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Election: " + (ElectionManager.isElectionActive() ? "ACTIVE" : "Waiting")).withStyle(ElectionManager.isElectionActive() ? ChatFormatting.GREEN : ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Dictator: " + (dictatorActive ? DictatorManager.getDictatorName() : "None")).withStyle(dictatorActive ? ChatFormatting.RED : ChatFormatting.GRAY))
                .build());
        gui.setSlot(34, new GuiElementBuilder(Items.ENDER_CHEST)
                .setName(Component.literal("Underground Auction").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("• Force start auction").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("• Get auction items").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to open").withStyle(ChatFormatting.YELLOW))
                .setCallback((int index, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openPage(player, AdminPage.UNDERGROUND_AUCTION))
                .build());
        gui.setSlot(32, new GuiElementBuilder(Items.COMPARATOR)
                .setName(Component.literal("⚙ Game Settings").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("• Boss settings (HP, damage, kills)").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("• Item level requirements").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("• Defence settings").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("• Drop rates & mob settings").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to open").withStyle(ChatFormatting.YELLOW))
                .setCallback((int index, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openGameSettingsPage(player))
                .build());

        gui.setSlot(11, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Component.literal("✦ Perk Management").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("• Activate/deactivate any perk").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("• View all active perks").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("• Reset all perks").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to open").withStyle(ChatFormatting.YELLOW))
                .setCallback((int index, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openPerkManagementPage(player))
                .build());
        // Close button
        gui.setSlot(40, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("Close").withStyle(ChatFormatting.RED))
                .setCallback((int index, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> gui.close())
                .build());

        gui.open();
    }

    // ═══════════════════════════════════════════════════════════
    // ELECTIONS PAGE
    // ═══════════════════════════════════════════════════════════

    private static void openElectionsPage(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x5, player, false);
        gui.setTitle(Component.literal("Elections Management"));

        fillBackground(gui);

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.BELL)
                .setName(Component.literal("Elections").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .glow()
                .build());

        boolean electionActive = ElectionManager.isElectionActive();
        boolean electionEnabled = ElectionManager.isElectionSystemEnabled();
        boolean electionPaused = ElectionManager.isElectionSystemPaused();
        boolean impeachmentActive = ElectionManager.isImpeachmentActive();

        // Start Election
        gui.setSlot(10, new GuiElementBuilder(Items.GREEN_CONCRETE)
                .setName(Component.literal("Start Election").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Force start an election now").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("▶ Click to start").withStyle(ChatFormatting.YELLOW))
                .setCallback((int index, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    ElectionManager.startElection(PoliticalServer.server);
                    player.sendSystemMessage(Component.literal("✓ Election started!").withStyle(ChatFormatting.GREEN));
                    openPage(player, AdminPage.ELECTIONS);
                })
                .build());

        // End Election
        gui.setSlot(11, new GuiElementBuilder(Items.RED_CONCRETE)
                .setName(Component.literal("End Election").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Force end current election").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("▶ Click to end").withStyle(ChatFormatting.YELLOW))
                .setCallback((int index, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    ElectionManager.endElection(PoliticalServer.server);
                    player.sendSystemMessage(Component.literal("✓ Election ended!").withStyle(ChatFormatting.RED));
                    openPage(player, AdminPage.ELECTIONS);
                })
                .build());

        // Toggle System
        gui.setSlot(12, new GuiElementBuilder(electionEnabled ? Items.LIME_DYE : Items.GRAY_DYE)
                .setName(Component.literal("Election System").withStyle(electionEnabled ? ChatFormatting.GREEN : ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Status: " + (electionEnabled ? "ENABLED" : "DISABLED")).withStyle(electionEnabled ? ChatFormatting.GREEN : ChatFormatting.RED))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("▶ Click to toggle").withStyle(ChatFormatting.YELLOW))
                .setCallback((int index, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    ElectionManager.setElectionSystemEnabled(!electionEnabled);
                    player.sendSystemMessage(Component.literal("✓ System " + (!electionEnabled ? "enabled" : "disabled")).withStyle(ChatFormatting.GREEN));
                    openPage(player, AdminPage.ELECTIONS);
                })
                .build());

        // Pause/Resume
        gui.setSlot(13, new GuiElementBuilder(electionPaused ? Items.ORANGE_DYE : Items.LIGHT_BLUE_DYE)
                .setName(Component.literal("Election Timer").withStyle(electionPaused ? ChatFormatting.GOLD : ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Status: " + (electionPaused ? "PAUSED" : "RUNNING")).withStyle(electionPaused ? ChatFormatting.GOLD : ChatFormatting.AQUA))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("▶ Click to " + (electionPaused ? "resume" : "pause")).withStyle(ChatFormatting.YELLOW))
                .setCallback((int index, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    ElectionManager.setElectionSystemPaused(!electionPaused);
                    player.sendSystemMessage(Component.literal("✓ Timer " + (!electionPaused ? "paused" : "resumed")).withStyle(ChatFormatting.YELLOW));
                    openPage(player, AdminPage.ELECTIONS);
                })
                .build());

        // Reset Impeachment
        gui.setSlot(14, new GuiElementBuilder(Items.TNT)
                .setName(Component.literal("Reset Impeachment").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Status: " + (impeachmentActive ? "ACTIVE" : "None")).withStyle(impeachmentActive ? ChatFormatting.RED : ChatFormatting.GREEN))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("▶ Click to reset").withStyle(ChatFormatting.YELLOW))
                .setCallback((int index, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    ElectionManager.resetImpeachment();
                    player.sendSystemMessage(Component.literal("✓ Impeachment reset!").withStyle(ChatFormatting.GREEN));
                    openPage(player, AdminPage.ELECTIONS);
                })
                .build());

        // Back button
        addBackButton(gui, player);

        gui.open();
    }
    private static int undergroundAuctionItemPage = 0;

    private static void openUndergroundAuctionPage(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Underground Auction Admin"));
        fillBackground(gui);

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.ENDER_CHEST)
                .setName(Component.literal("Underground Auction").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                .glow()
                .build());

        boolean auctionActive = UndergroundAuctionManager.isAuctionActive();
        long timeUntil = UndergroundAuctionManager.getTimeUntilNextAuction();
        String timeStr = PoliticalServer.formatTime(timeUntil);

        // Status display
        gui.setSlot(10, new GuiElementBuilder(auctionActive ? Items.LIME_CONCRETE : Items.RED_CONCRETE)
                .setName(Component.literal("Auction Status").withStyle(auctionActive ? ChatFormatting.GREEN : ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Status: " + (auctionActive ? "ACTIVE" : "Waiting")).withStyle(auctionActive ? ChatFormatting.GREEN : ChatFormatting.RED))
                .addLoreLine(Component.literal("Next auction: " + timeStr).withStyle(ChatFormatting.GRAY))
                .build());

        // Force Start Auction
        gui.setSlot(12, new GuiElementBuilder(Items.CLOCK)
                .setName(Component.literal("Force Start Auction").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Immediately starts an auction").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Resets the 6-hour timer").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("▶ Click to start").withStyle(ChatFormatting.YELLOW))
                .setCallback((int index, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    UndergroundAuctionManager.forceStartAuction(PoliticalServer.server);
                    player.sendSystemMessage(Component.literal("✓ Underground auction started!").withStyle(ChatFormatting.LIGHT_PURPLE));
                    gui.close();
                })
                .build());

        // End Current Auction
        gui.setSlot(14, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("End Current Auction").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Force ends the current auction").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("▶ Click to end").withStyle(ChatFormatting.YELLOW))
                .setCallback((int index, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    UndergroundAuctionManager.forceEndAuction();
                    player.sendSystemMessage(Component.literal("✓ Auction ended!").withStyle(ChatFormatting.RED));
                    openPage(player, AdminPage.UNDERGROUND_AUCTION);
                })
                .build());

        // Custom Items Section (separate from auction items)
        gui.setSlot(16, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Component.literal("Custom Items").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to open custom items panel").withStyle(ChatFormatting.GRAY))
                .glow()
                .setCallback((int index, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    openCustomItemsPage(player);
                })
                .build());

        // Section header for auction items
        gui.setSlot(22, new GuiElementBuilder(Items.CHEST)
                .setName(Component.literal("Auction Items").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click items below to receive them").withStyle(ChatFormatting.GRAY))
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
                            .setName(Component.literal(item.name).withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                            .addLoreLine(Component.literal(""))
                            .addLoreLine(Component.literal("Starting bid: " + item.startingBid + " credits").withStyle(ChatFormatting.GOLD))
                            .addLoreLine(Component.literal(""))
                            .addLoreLine(Component.literal("▶ Click to receive").withStyle(ChatFormatting.GREEN))
                            .glow()
                            .setCallback((int index, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                                // Give the ACTUAL item stack, not just by index
                                player.addItem(item.itemStack.copy());
                                player.sendSystemMessage(Component.literal("✓ Given " + item.name).withStyle(ChatFormatting.GREEN));
                            })
                            .build());
                }
            }
        }

        // Pagination buttons
        if (undergroundAuctionItemPage > 0) {
            gui.setSlot(18, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("◀ Previous Page").withStyle(ChatFormatting.YELLOW))
                    .setCallback((int index, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                        undergroundAuctionItemPage--;
                        openPage(player, AdminPage.UNDERGROUND_AUCTION);
                    })
                    .build());
        }

        if (undergroundAuctionItemPage < totalPages - 1) {
            gui.setSlot(26, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("Next Page ▶").withStyle(ChatFormatting.YELLOW))
                    .setCallback((int index, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                        undergroundAuctionItemPage++;
                        openPage(player, AdminPage.UNDERGROUND_AUCTION);
                    })
                    .build());
        }

        // Page indicator
        gui.setSlot(22, new GuiElementBuilder(Items.PAPER)
                .setName(Component.literal("Page " + (undergroundAuctionItemPage + 1) + "/" + Math.max(1, totalPages)).withStyle(ChatFormatting.WHITE))
                .build());

        // Back button
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.GRAY))
                .setCallback((int index, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    undergroundAuctionItemPage = 0; // Reset page
                    openPage(player, AdminPage.MAIN);
                })
                .build());

        // Close button
        gui.setSlot(53, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("Close").withStyle(ChatFormatting.RED))
                .setCallback((int index, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> gui.close())
                .build());

        gui.open();
    }

    private static void openCustomItemsPage(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("✦ Custom Items ✦"));
        fillBackground(gui);

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Component.literal("✦ Custom Items ✦").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Select a category to give items").withStyle(ChatFormatting.GRAY))
                .glow()
                .build());

        // Bounty Swords
        gui.setSlot(10, new GuiElementBuilder(Items.IRON_SWORD)
                .setName(Component.literal("⚔ Bounty Swords").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("T1 & T2 swords for all 7 types").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to open").withStyle(ChatFormatting.YELLOW))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openAdminCustomSwords(player))
                .build());

        // T1 Armor
        gui.setSlot(11, new GuiElementBuilder(Items.LEATHER_CHESTPLATE)
                .setName(Component.literal("T1 Armor").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("T1 armor sets for all 7 types").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to open").withStyle(ChatFormatting.YELLOW))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openAdminArmorSelector(player, 1))
                .build());

        // T2 Armor
        gui.setSlot(12, new GuiElementBuilder(Items.DIAMOND_CHESTPLATE)
                .setName(Component.literal("T2 Armor").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("T2 armor sets for all 7 types").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to open").withStyle(ChatFormatting.YELLOW))
                .glow()
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openAdminArmorSelector(player, 2))
                .build());

        // Legendary Gear
        gui.setSlot(13, new GuiElementBuilder(Items.ZOMBIE_HEAD)
                .setName(Component.literal("★ Legendary Gear").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("• Berserker Helmet").withStyle(ChatFormatting.DARK_GREEN))
                .addLoreLine(Component.literal("• Spider Leggings").withStyle(ChatFormatting.DARK_RED))
                .addLoreLine(Component.literal("• Skeleton Bow").withStyle(ChatFormatting.WHITE))
                .addLoreLine(Component.literal("• Slime Boots").withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal("• Warden Chestplate").withStyle(ChatFormatting.DARK_AQUA))
                .addLoreLine(Component.literal("• Voidwalker's Crown").withStyle(ChatFormatting.DARK_PURPLE))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to open").withStyle(ChatFormatting.YELLOW))
                .glow()
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openAdminLegendary(player))
                .build());

        // Special Weapons
        gui.setSlot(14, new GuiElementBuilder(Items.GOLDEN_AXE)
                .setName(Component.literal("⚡ Special Weapons").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("• Harvey's Stick").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("• The Gavel").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("• Hermes Shoes").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("• HPEBM Mk1–Mk5").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("• Ultra Overclocked Beam").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to open").withStyle(ChatFormatting.YELLOW))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openAdminSpecialWeapons(player))
                .build());

        // Materials (Chunks & Cores)
        gui.setSlot(15, new GuiElementBuilder(Items.ECHO_SHARD)
                .setName(Component.literal("◈ Materials").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Chunks & Cores for all 7 types").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to open").withStyle(ChatFormatting.YELLOW))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openAdminMaterials(player))
                .build());

        // Piglin Items
        gui.setSlot(16, new GuiElementBuilder(Items.GOLDEN_HELMET)
                .setName(Component.literal("👑 Piglin Items").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("• Crown of Greed").withStyle(ChatFormatting.GOLD))
                .addLoreLine(Component.literal("• Piglin Core & Flesh").withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal("• Piglin T1 & T2 Armor").withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to open").withStyle(ChatFormatting.YELLOW))
                .glow()
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openAdminPiglinItems(player))
                .build());

        // Back button
        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openPage(player, AdminPage.UNDERGROUND_AUCTION))
                .build());

        // Close button
        gui.setSlot(53, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("Close").withStyle(ChatFormatting.RED))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> gui.close())
                .build());

        gui.open();
    }

    private static void openAdminCustomSwords(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("⚔ Bounty Swords"));
        fillBackground(gui);

        gui.setSlot(4, new GuiElementBuilder(Items.IRON_SWORD)
                .setName(Component.literal("⚔ Bounty Swords").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Click any sword to receive it").withStyle(ChatFormatting.GRAY))
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
            String name = sword.getHoverName().getString();
            gui.setSlot(t1Slots[i], new GuiElementBuilder(sword.getItem())
                    .setName(Component.literal(name).withStyle(ChatFormatting.YELLOW))
                    .addLoreLine(Component.literal("T1 Sword").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("▶ Click to receive").withStyle(ChatFormatting.GREEN))
                    .setCallback((idx, type2, containerInput, slotGui) -> {
                        player.addItem(SlayerItems.createSlayerSword(t));
                        player.sendSystemMessage(Component.literal("✓ Given " + t.name() + " T1 sword!").withStyle(ChatFormatting.GREEN));
                    })
                    .build());

            ItemStack t2sword = SlayerItems.createUpgradedSlayerSword(t);
            String t2name = t2sword.getHoverName().getString();
            gui.setSlot(t2Slots[i], new GuiElementBuilder(t2sword.getItem())
                    .setName(Component.literal(t2name).withStyle(ChatFormatting.LIGHT_PURPLE))
                    .addLoreLine(Component.literal("T2 Upgraded Sword").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("▶ Click to receive").withStyle(ChatFormatting.GREEN))
                    .glow()
                    .setCallback((idx, type2, containerInput, slotGui) -> {
                        player.addItem(SlayerItems.createUpgradedSlayerSword(t));
                        player.sendSystemMessage(Component.literal("✓ Given " + t.name() + " T2 sword!").withStyle(ChatFormatting.GREEN));
                    })
                    .build());
        }

        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type2, containerInput, slotGui) -> openCustomItemsPage(player))
                .build());
        gui.setSlot(53, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("Close").withStyle(ChatFormatting.RED))
                .setCallback((idx, type2, containerInput, slotGui) -> gui.close())
                .build());

        gui.open();
    }

    private static void openAdminArmorSelector(ServerPlayer player, int tier) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("T" + tier + " Armor — Select Type"));
        fillBackground(gui);

        gui.setSlot(4, new GuiElementBuilder(tier == 1 ? Items.LEATHER_CHESTPLATE : Items.DIAMOND_CHESTPLATE)
                .setName(Component.literal("T" + tier + " Armor").withStyle(tier == 1 ? ChatFormatting.BLUE : ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Select a bounty type to receive full set").withStyle(ChatFormatting.GRAY))
                .build());

        SlayerManager.SlayerType[] types = SlayerManager.SlayerType.values();
        int[] slots = {10, 11, 12, 13, 14, 15, 16};
        net.minecraft.world.item.Item[] icons = {Items.ROTTEN_FLESH, Items.SPIDER_EYE, Items.BONE, Items.SLIME_BALL,
                Items.ENDER_PEARL, Items.ECHO_SHARD, Items.GOLD_INGOT};

        for (int i = 0; i < types.length && i < slots.length; i++) {
            final SlayerManager.SlayerType t = types[i];
            final int finalTier = tier;
            gui.setSlot(slots[i], new GuiElementBuilder(icons[i])
                    .setName(Component.literal(capitalize(t) + " T" + tier)
                            .withStyle(tier == 1 ? ChatFormatting.BLUE : ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("Give full T" + tier + " " + t.name().toLowerCase() + " armor set").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("▶ Click to receive all 4 pieces").withStyle(ChatFormatting.GREEN))
                    .setCallback((idx, type2, containerInput, slotGui) -> {
                        player.addItem(SlayerItems.createSlayerHelmet(t, finalTier));
                        player.addItem(SlayerItems.createSlayerChestplate(t, finalTier));
                        player.addItem(SlayerItems.createSlayerLeggings(t, finalTier));
                        player.addItem(SlayerItems.createSlayerBoots(t, finalTier));
                        player.sendSystemMessage(Component.literal("✓ Given T" + finalTier + " " + t.name().toLowerCase() + " armor set!").withStyle(ChatFormatting.GREEN));
                    })
                    .build());
        }

        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type2, containerInput, slotGui) -> openCustomItemsPage(player))
                .build());
        gui.setSlot(53, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("Close").withStyle(ChatFormatting.RED))
                .setCallback((idx, type2, containerInput, slotGui) -> gui.close())
                .build());

        gui.open();
    }

    private static void openAdminLegendary(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("★ Legendary Gear"));
        fillBackground(gui);

        gui.setSlot(4, new GuiElementBuilder(Items.ZOMBIE_HEAD)
                .setName(Component.literal("★ Legendary Gear").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Click any item to receive it").withStyle(ChatFormatting.GRAY))
                .glow()
                .build());

        // Zombie Berserker Helmet
        gui.setSlot(10, new GuiElementBuilder(Items.LEATHER_HELMET)
                .setName(Component.literal("Zombie Berserker Helmet").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Legendary Zombie Bounty Helmet").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("▶ Click to receive").withStyle(ChatFormatting.GREEN))
                .glow()
                .setCallback((idx, type2, containerInput, slotGui) -> {
                    player.addItem(SlayerItems.createZombieBerserkerHelmet());
                    player.sendSystemMessage(Component.literal("✓ Given Zombie Berserker Helmet!").withStyle(ChatFormatting.GREEN));
                })
                .build());

        // Spider Leggings
        gui.setSlot(11, new GuiElementBuilder(Items.LEATHER_LEGGINGS)
                .setName(Component.literal("Spider Leggings").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Legendary Spider Bounty Leggings").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("▶ Click to receive").withStyle(ChatFormatting.GREEN))
                .glow()
                .setCallback((idx, type2, containerInput, slotGui) -> {
                    player.addItem(SlayerItems.createSpiderLeggings());
                    player.sendSystemMessage(Component.literal("✓ Given Spider Leggings!").withStyle(ChatFormatting.GREEN));
                })
                .build());

        // Skeleton Bow
        gui.setSlot(12, new GuiElementBuilder(Items.BOW)
                .setName(Component.literal("Skeleton Bow").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Legendary Skeleton Bounty Bow").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("▶ Click to receive").withStyle(ChatFormatting.GREEN))
                .glow()
                .setCallback((idx, type2, containerInput, slotGui) -> {
                    player.addItem(SlayerItems.createSkeletonBow());
                    player.sendSystemMessage(Component.literal("✓ Given Skeleton Bow!").withStyle(ChatFormatting.GREEN));
                })
                .build());

        // Slime Boots
        gui.setSlot(13, new GuiElementBuilder(Items.LEATHER_BOOTS)
                .setName(Component.literal("Slime Boots").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Legendary Slime Bounty Boots").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("▶ Click to receive").withStyle(ChatFormatting.GREEN))
                .glow()
                .setCallback((idx, type2, containerInput, slotGui) -> {
                    player.addItem(SlayerItems.createSlimeBoots());
                    player.sendSystemMessage(Component.literal("✓ Given Slime Boots!").withStyle(ChatFormatting.GREEN));
                })
                .build());

        // Warden Chestplate
        gui.setSlot(14, new GuiElementBuilder(Items.LEATHER_CHESTPLATE)
                .setName(Component.literal("Warden Chestplate").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Legendary Warden Bounty Chestplate").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("▶ Click to receive").withStyle(ChatFormatting.GREEN))
                .glow()
                .setCallback((idx, type2, containerInput, slotGui) -> {
                    player.addItem(SlayerItems.createWardenChestplate());
                    player.sendSystemMessage(Component.literal("✓ Given Warden Chestplate!").withStyle(ChatFormatting.GREEN));
                })
                .build());

        // Voidwalker's Crown
        gui.setSlot(15, new GuiElementBuilder(Items.NETHERITE_HELMET)
                .setName(Component.literal("Voidwalker's Crown").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Legendary Enderman Bounty Helmet").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("▶ Click to receive").withStyle(ChatFormatting.GREEN))
                .glow()
                .setCallback((idx, type2, containerInput, slotGui) -> {
                    player.addItem(SlayerItems.createVoidwalkerCrown());
                    player.sendSystemMessage(Component.literal("✓ Given Voidwalker's Crown!").withStyle(ChatFormatting.GREEN));
                })
                .build());

        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type2, containerInput, slotGui) -> openCustomItemsPage(player))
                .build());
        gui.setSlot(53, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("Close").withStyle(ChatFormatting.RED))
                .setCallback((idx, type2, containerInput, slotGui) -> gui.close())
                .build());

        gui.open();
    }

    private static void openAdminSpecialWeapons(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("⚡ Special Weapons"));
        fillBackground(gui);

        gui.setSlot(4, new GuiElementBuilder(Items.GOLDEN_AXE)
                .setName(Component.literal("⚡ Special Weapons").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Click any item to receive it").withStyle(ChatFormatting.GRAY))
                .glow()
                .build());

        // Harvey's Stick
        gui.setSlot(10, new GuiElementBuilder(Items.STICK)
                .setName(Component.literal("Harvey's Stick").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Summons lightning on hit").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("▶ Click to receive").withStyle(ChatFormatting.GREEN))
                .glow()
                .setCallback((idx, type2, containerInput, slotGui) -> {
                    player.addItem(CustomItemHandler.createHarveysStick());
                    player.sendSystemMessage(Component.literal("✓ Given Harvey's Stick!").withStyle(ChatFormatting.GREEN));
                })
                .build());

        // The Gavel
        gui.setSlot(11, new GuiElementBuilder(Items.MACE)
                .setName(Component.literal("The Gavel").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("AOE explosion attack").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("▶ Click to receive").withStyle(ChatFormatting.GREEN))
                .glow()
                .setCallback((idx, type2, containerInput, slotGui) -> {
                    player.addItem(CustomItemHandler.createTheGavel());
                    player.sendSystemMessage(Component.literal("✓ Given The Gavel!").withStyle(ChatFormatting.GREEN));
                })
                .build());

        // Hermes Shoes
        gui.setSlot(12, new GuiElementBuilder(Items.IRON_BOOTS)
                .setName(Component.literal("Hermes Shoes").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Permanent Speed III").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("▶ Click to receive").withStyle(ChatFormatting.GREEN))
                .glow()
                .setCallback((idx, type2, containerInput, slotGui) -> {
                    player.addItem(CustomItemHandler.createHermesShoes());
                    player.sendSystemMessage(Component.literal("✓ Given Hermes Shoes!").withStyle(ChatFormatting.GREEN));
                })
                .build());

        // HPEBM Mk1–Mk5
        int[] hpebmSlots = {19, 20, 21, 22, 23};
        for (int mk = 1; mk <= 5; mk++) {
            final int finalMk = mk;
            gui.setSlot(hpebmSlots[mk - 1], new GuiElementBuilder(Items.IRON_SHOVEL)
                    .setName(Component.literal("H.P.E.B.M. Mk" + mk).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("Plasma beam — Mk" + mk + " variant").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("▶ Click to receive").withStyle(ChatFormatting.GREEN))
                    .glow()
                    .setCallback((idx, type2, containerInput, slotGui) -> {
                        player.addItem(CustomItemHandler.createHPEBM(finalMk));
                        player.sendSystemMessage(Component.literal("✓ Given HPEBM Mk" + finalMk + "!").withStyle(ChatFormatting.GREEN));
                    })
                    .build());
        }

        // Ultra Overclocked Beam
        gui.setSlot(25, new GuiElementBuilder(Items.GOLDEN_SHOVEL)
                .setName(Component.literal("Ultra Overclocked Beam").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Ultimate beam weapon").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("▶ Click to receive").withStyle(ChatFormatting.GREEN))
                .glow()
                .setCallback((idx, type2, containerInput, slotGui) -> {
                    player.addItem(CustomItemHandler.createUltraOverclockedBeam());
                    player.sendSystemMessage(Component.literal("✓ Given Ultra Overclocked Beam!").withStyle(ChatFormatting.GREEN));
                })
                .build());

        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type2, containerInput, slotGui) -> openCustomItemsPage(player))
                .build());
        gui.setSlot(53, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("Close").withStyle(ChatFormatting.RED))
                .setCallback((idx, type2, containerInput, slotGui) -> gui.close())
                .build());

        gui.open();
    }

    private static void openAdminMaterials(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("◈ Materials"));
        fillBackground(gui);

        gui.setSlot(4, new GuiElementBuilder(Items.ECHO_SHARD)
                .setName(Component.literal("◈ Materials").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Chunks & Cores for all 7 bounty types").withStyle(ChatFormatting.GRAY))
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
                    .setName(Component.literal(capitalize(t) + " Chunk").withStyle(ChatFormatting.YELLOW))
                    .addLoreLine(Component.literal("▶ Click to receive").withStyle(ChatFormatting.GREEN))
                    .setCallback((idx, type2, containerInput, slotGui) -> {
                        player.addItem(SlayerItems.createChunk(t));
                        player.sendSystemMessage(Component.literal("✓ Given " + t.name().toLowerCase() + " chunk!").withStyle(ChatFormatting.GREEN));
                    })
                    .build());

            ItemStack core = SlayerItems.createCore(t);
            gui.setSlot(coreSlots[i], new GuiElementBuilder(core.getItem())
                    .setName(Component.literal(capitalize(t) + " Core").withStyle(ChatFormatting.AQUA))
                    .addLoreLine(Component.literal("▶ Click to receive").withStyle(ChatFormatting.GREEN))
                    .glow()
                    .setCallback((idx, type2, containerInput, slotGui) -> {
                        player.addItem(SlayerItems.createCore(t));
                        player.sendSystemMessage(Component.literal("✓ Given " + t.name().toLowerCase() + " core!").withStyle(ChatFormatting.GREEN));
                    })
                    .build());
        }

        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type2, containerInput, slotGui) -> openCustomItemsPage(player))
                .build());
        gui.setSlot(53, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("Close").withStyle(ChatFormatting.RED))
                .setCallback((idx, type2, containerInput, slotGui) -> gui.close())
                .build());

        gui.open();
    }

    private static void openAdminPiglinItems(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("👑 Piglin Items"));
        fillBackground(gui);

        gui.setSlot(4, new GuiElementBuilder(Items.GOLDEN_HELMET)
                .setName(Component.literal("👑 Piglin Items").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Click any item to receive it").withStyle(ChatFormatting.GRAY))
                .glow()
                .build());

        // Crown of Greed
        gui.setSlot(10, new GuiElementBuilder(Items.GOLDEN_HELMET)
                .setName(Component.literal("Crown of Greed").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Legendary Piglin item").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("▶ Click to receive").withStyle(ChatFormatting.GREEN))
                .glow()
                .setCallback((idx, type2, containerInput, slotGui) -> {
                    player.addItem(SlayerItems.createCrownOfGreed());
                    player.sendSystemMessage(Component.literal("✓ Given Crown of Greed!").withStyle(ChatFormatting.GREEN));
                })
                .build());

        // Piglin Core
        gui.setSlot(11, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Component.literal("Piglin Core").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Piglin crafting material").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("▶ Click to receive").withStyle(ChatFormatting.GREEN))
                .setCallback((idx, type2, containerInput, slotGui) -> {
                    player.addItem(SlayerItems.createPiglinCore());
                    player.sendSystemMessage(Component.literal("✓ Given Piglin Core!").withStyle(ChatFormatting.GREEN));
                })
                .build());

        // Piglin Flesh
        gui.setSlot(12, new GuiElementBuilder(Items.PORKCHOP)
                .setName(Component.literal("Piglin Flesh").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Piglin crafting material").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("▶ Click to receive").withStyle(ChatFormatting.GREEN))
                .setCallback((idx, type2, containerInput, slotGui) -> {
                    player.addItem(SlayerItems.createPiglinFlesh());
                    player.sendSystemMessage(Component.literal("✓ Given Piglin Flesh!").withStyle(ChatFormatting.GREEN));
                })
                .build());

        // Piglin T1 full armor set
        gui.setSlot(14, new GuiElementBuilder(Items.GOLDEN_CHESTPLATE)
                .setName(Component.literal("Piglin T1 Armor Set").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Give full Piglin T1 armor").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("▶ Click to receive").withStyle(ChatFormatting.GREEN))
                .setCallback((idx, type2, containerInput, slotGui) -> {
                    player.addItem(SlayerItems.createSlayerHelmet(SlayerManager.SlayerType.PIGLIN, 1));
                    player.addItem(SlayerItems.createSlayerChestplate(SlayerManager.SlayerType.PIGLIN, 1));
                    player.addItem(SlayerItems.createSlayerLeggings(SlayerManager.SlayerType.PIGLIN, 1));
                    player.addItem(SlayerItems.createSlayerBoots(SlayerManager.SlayerType.PIGLIN, 1));
                    player.sendSystemMessage(Component.literal("✓ Given Piglin T1 armor set!").withStyle(ChatFormatting.GREEN));
                })
                .build());

        // Piglin T2 full armor set
        gui.setSlot(15, new GuiElementBuilder(Items.GOLDEN_CHESTPLATE)
                .setName(Component.literal("Piglin T2 Armor Set").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Give full Piglin T2 armor").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("▶ Click to receive").withStyle(ChatFormatting.GREEN))
                .glow()
                .setCallback((idx, type2, containerInput, slotGui) -> {
                    player.addItem(SlayerItems.createSlayerHelmet(SlayerManager.SlayerType.PIGLIN, 2));
                    player.addItem(SlayerItems.createSlayerChestplate(SlayerManager.SlayerType.PIGLIN, 2));
                    player.addItem(SlayerItems.createSlayerLeggings(SlayerManager.SlayerType.PIGLIN, 2));
                    player.addItem(SlayerItems.createSlayerBoots(SlayerManager.SlayerType.PIGLIN, 2));
                    player.sendSystemMessage(Component.literal("✓ Given Piglin T2 armor set!").withStyle(ChatFormatting.GREEN));
                })
                .build());

        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type2, containerInput, slotGui) -> openCustomItemsPage(player))
                .build());
        gui.setSlot(53, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("Close").withStyle(ChatFormatting.RED))
                .setCallback((idx, type2, containerInput, slotGui) -> gui.close())
                .build());

        gui.open();
    }
    // ═══════════════════════════════════════════════════════════
    // GOVERNMENT PAGE
    // ═══════════════════════════════════════════════════════════

    private static void openGovernmentPage(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x5, player, false);
        gui.setTitle(Component.literal("Government Management"));

        fillBackground(gui);

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.GOLDEN_HELMET)
                .setName(Component.literal("Government").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .glow()
                .build());

        String chair = DataManager.getChair();
        String viceChair = DataManager.getViceChair();
        String judge = DataManager.getJudge();
        boolean dictatorActive = DictatorManager.isDictatorActive();

        // Set Chair
        gui.setSlot(10, new GuiElementBuilder(Items.GOLDEN_HELMET)
                .setName(Component.literal("Set Chair").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Current: " + (chair != null ? DataManager.getPlayerName(chair) : "None")).withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("▶ Click to select").withStyle(ChatFormatting.GRAY))
                .setCallback((int index, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openPlayerSelector(player, "chair"))
                .build());

        // Set Vice Chair
        gui.setSlot(11, new GuiElementBuilder(Items.IRON_HELMET)
                .setName(Component.literal("Set Vice Chair").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Current: " + (viceChair != null ? DataManager.getPlayerName(viceChair) : "None")).withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("▶ Click to select").withStyle(ChatFormatting.GRAY))
                .setCallback((int index, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openPlayerSelector(player, "vicechair"))
                .build());

        // Set Judge
        gui.setSlot(12, new GuiElementBuilder(Items.LEATHER_HELMET)
                .setName(Component.literal("Set Judge").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Current: " + (judge != null ? DataManager.getPlayerName(judge) : "None")).withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("▶ Click to select").withStyle(ChatFormatting.GRAY))
                .setCallback((int index, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openPlayerSelector(player, "judge"))
                .build());

        // Set Dictator
        gui.setSlot(14, new GuiElementBuilder(dictatorActive ? Items.NETHERITE_HELMET : Items.CHAINMAIL_HELMET)
                .setName(Component.literal("Set Dictator").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Status: " + (dictatorActive ? "ACTIVE" : "Inactive")).withStyle(dictatorActive ? ChatFormatting.RED : ChatFormatting.GREEN))
                .addLoreLine(Component.literal(dictatorActive ? "Dictator: " + DictatorManager.getDictatorName() : "No dictator").withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("▶ Click to select (must be Chair)").withStyle(ChatFormatting.GRAY))
                .setCallback((int index, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openPlayerSelector(player, "dictator"))
                .build());

        // Remove Dictator
        gui.setSlot(15, new GuiElementBuilder(Items.EMERALD)
                .setName(Component.literal("Remove Dictator").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("End current dictatorship").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("▶ Click to remove").withStyle(ChatFormatting.YELLOW))
                .setCallback((int index, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    if (dictatorActive) {
                        DictatorManager.removeDictator();
                        player.sendSystemMessage(Component.literal("✓ Dictatorship ended!").withStyle(ChatFormatting.GREEN));
                        openPage(player, AdminPage.GOVERNMENT);
                    } else {
                        player.sendSystemMessage(Component.literal("✗ No active dictator!").withStyle(ChatFormatting.RED));
                    }
                })
                .build());

        // Reset Perks
        gui.setSlot(19, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Component.literal("Reset Perks").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Unlock perk selection").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("for Chair and Vice Chair").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("▶ Click to reset").withStyle(ChatFormatting.YELLOW))
                .setCallback((int index, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    PerkManager.onNewTermStart();
                    PerkManager.setPreviousTermPerks(new ArrayList<>());
                    DataManager.save(PoliticalServer.server);
                    player.sendSystemMessage(Component.literal("✓ Perks reset!").withStyle(ChatFormatting.GREEN));
                    openPage(player, AdminPage.GOVERNMENT);
                })
                .build());

        // Clear All Perks
        gui.setSlot(20, new GuiElementBuilder(Items.FIRE_CHARGE)
                .setName(Component.literal("Clear All Perks").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Remove all active perks").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("▶ Click to clear").withStyle(ChatFormatting.YELLOW))
                .setCallback((int index, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    PerkManager.clearAllPerks();
                    DataManager.save(PoliticalServer.server);
                    player.sendSystemMessage(Component.literal("✓ All perks cleared!").withStyle(ChatFormatting.RED));
                    openPage(player, AdminPage.GOVERNMENT);
                })
                .build());

        addBackButton(gui, player);
        gui.open();
    }

    // ═══════════════════════════════════════════════════════════
    // PLAYERS PAGE
    // ═══════════════════════════════════════════════════════════

    private static void openPlayersPage(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x5, player, false);
        gui.setTitle(Component.literal("Player Management"));

        fillBackground(gui);

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Component.literal("Player Management").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .glow()
                .build());

        // Give Credits
        gui.setSlot(10, new GuiElementBuilder(Items.GOLD_NUGGET)
                .setName(Component.literal("Give Credits").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Add credits to a player").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("▶ Click to select").withStyle(ChatFormatting.YELLOW))
                .glow()
                .setCallback((int index, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openCreditsGui(player))
                .build());

        // Prison Controls
        gui.setSlot(11, new GuiElementBuilder(Items.IRON_BARS)
                .setName(Component.literal("Prison Controls").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Imprison/release players").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("▶ Click to open").withStyle(ChatFormatting.YELLOW))
                .setCallback((int index, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openPrisonGui(player))
                .build());

        // Exile Player
        gui.setSlot(12, new GuiElementBuilder(Items.ENDER_PEARL)
                .setName(Component.literal("Exile Player").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Teleport player far away").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("▶ Click to select").withStyle(ChatFormatting.YELLOW))
                .setCallback((int index, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openPlayerSelector(player, "exile"))
                .build());

        // Smite Player
        gui.setSlot(13, new GuiElementBuilder(Items.LIGHTNING_ROD)
                .setName(Component.literal("Smite Player").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Strike with lightning").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("▶ Click to select").withStyle(ChatFormatting.YELLOW))
                .setCallback((int index, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openPlayerSelector(player, "smite"))
                .build());

        // Give Coins
        gui.setSlot(14, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Component.literal("Give Coins").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Add coins to a player").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("L: +100 | R: -100 | Shift: ±1000").withStyle(ChatFormatting.DARK_GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("▶ Click player to select").withStyle(ChatFormatting.YELLOW))
                .setCallback((int index, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openPlayerSelector(player, "give_coins"))
                .build());

        // Set Slayer Level
        gui.setSlot(15, new GuiElementBuilder(Items.EXPERIENCE_BOTTLE)
                .setName(Component.literal("Set Slayer Level").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Adjust bounty slayer levels/XP").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("▶ Click to select player").withStyle(ChatFormatting.YELLOW))
                .setCallback((int index, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openPlayerSelector(player, "slayer_level"))
                .build());

        // Teleport to Player
        gui.setSlot(16, new GuiElementBuilder(Items.ENDER_EYE)
                .setName(Component.literal("Teleport to Player").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Teleport yourself to a player").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("▶ Click to select").withStyle(ChatFormatting.YELLOW))
                .setCallback((int index, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openPlayerSelector(player, "tp_to"))
                .build());

        // Row 2 — additional player management
        // Reset Bounty Data
        gui.setSlot(19, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("Reset Bounty Data").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Resets ALL slayer levels + XP").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("to 0 for a player").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("▶ Click to select").withStyle(ChatFormatting.YELLOW))
                .setCallback((int index, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openPlayerSelector(player, "reset_bounty"))
                .build());

        // Set Exact Coins
        gui.setSlot(20, new GuiElementBuilder(Items.GOLD_BLOCK)
                .setName(Component.literal("Set Exact Coins").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Set a player's coin balance to").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("an exact amount via sign").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("▶ Click to select").withStyle(ChatFormatting.YELLOW))
                .setCallback((int index, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openPlayerSelector(player, "set_coins"))
                .build());

        // Set Exact Credits
        gui.setSlot(21, new GuiElementBuilder(Items.EMERALD)
                .setName(Component.literal("Set Exact Credits").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Set a player's credit balance to").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("an exact amount via sign").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("▶ Click to select").withStyle(ChatFormatting.YELLOW))
                .setCallback((int index, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openPlayerSelector(player, "set_credits"))
                .build());

        addBackButton(gui, player);
        gui.open();
    }

    // ═══════════════════════════════════════════════════════════
    // ECONOMY PAGE
    // ═══════════════════════════════════════════════════════════

    private static void openEconomyPage(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x5, player, false);
        gui.setTitle(Component.literal("Economy Management"));

        fillBackground(gui);

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.GOLD_BLOCK)
                .setName(Component.literal("Economy").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .glow()
                .build());

        boolean taxEnabled = TaxManager.isTaxEnabled();

        // Tax System Toggle
        gui.setSlot(10, new GuiElementBuilder(taxEnabled ? Items.GOLD_BLOCK : Items.GOLD_INGOT)
                .setName(Component.literal("Tax System").withStyle(taxEnabled ? ChatFormatting.GREEN : ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Status: " + (taxEnabled ? "ENABLED" : "DISABLED")).withStyle(taxEnabled ? ChatFormatting.GREEN : ChatFormatting.RED))
                .addLoreLine(Component.literal("Rate: " + String.format("%.1f", TaxManager.getDailyTaxPercentage() * 100.0f) + "% of coins").withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("▶ Click to toggle").withStyle(ChatFormatting.GRAY))
                .setCallback((int index, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    TaxManager.setTaxEnabled(!taxEnabled);
                    player.sendSystemMessage(Component.literal("✓ Tax " + (!taxEnabled ? "enabled" : "disabled")).withStyle(ChatFormatting.GREEN));
                    openPage(player, AdminPage.ECONOMY);
                })
                .build());

        // Set Tax Rate
        gui.setSlot(11, new GuiElementBuilder(Items.GOLD_NUGGET)
                .setName(Component.literal("Set Tax Rate").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Current: " + String.format("%.1f", TaxManager.getDailyTaxPercentage() * 100.0f) + "% of coins/day").withStyle(ChatFormatting.GOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Left: +0.5%  |  Right: -0.5%").withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal("Shift-click: ±2.5%").withStyle(ChatFormatting.AQUA))
                .setCallback((int index, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    float current = TaxManager.getDailyTaxPercentage();
                    float change = type.isLeft ? 0.005f : -0.005f;
                    if (type.shift) change *= 5f;
                    TaxManager.setDailyTaxPercentage(current + change);
                    openPage(player, AdminPage.ECONOMY);
                })
                .build());

        // View Debtors
        gui.setSlot(12, new GuiElementBuilder(Items.PAPER)
                .setName(Component.literal("View Debtors").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("See players in debt").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("▶ Click to view").withStyle(ChatFormatting.YELLOW))
                .setCallback((int index, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> RulerGui.openDebtorsPage(player, "admin"))
                .build());

        // Auction House Toggle
        boolean auctionActive = UndergroundAuctionManager.isAuctionActive();
        gui.setSlot(13, new GuiElementBuilder(auctionActive ? Items.LIME_STAINED_GLASS_PANE : Items.GRAY_STAINED_GLASS_PANE)
                .setName(Component.literal("Underground Auction").withStyle(auctionActive ? ChatFormatting.GREEN : ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Status: " + (auctionActive ? "§aACTIVE" : "§cINACTIVE")).withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Next: §f" + PoliticalServer.formatTime(UndergroundAuctionManager.getTimeUntilNextAuction())).withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Click: Force start/stop auction").withStyle(ChatFormatting.YELLOW))
                .setCallback((int index, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    if (auctionActive) {
                        UndergroundAuctionManager.forceEndAuction();
                        player.sendSystemMessage(Component.literal("✓ Auction forcibly ended!").withStyle(ChatFormatting.RED));
                    } else {
                        UndergroundAuctionManager.forceStartAuction(PoliticalServer.server);
                        player.sendSystemMessage(Component.literal("✓ Auction forcibly started!").withStyle(ChatFormatting.GREEN));
                    }
                    openPage(player, AdminPage.ECONOMY);
                })
                .build());

        // Give Coins to Player
        gui.setSlot(14, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Component.literal("Give Coins to Player").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Add coins to an online player").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("▶ Click to select player").withStyle(ChatFormatting.YELLOW))
                .setCallback((int index, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openPlayerSelector(player, "give_coins"))
                .build());

        addBackButton(gui, player);
        gui.open();
    }

    // ═══════════════════════════════════════════════════════════
    // WORLD PAGE
    // ═══════════════════════════════════════════════════════════

    private static void openWorldPage(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x5, player, false);
        gui.setTitle(Component.literal("World Settings"));

        fillBackground(gui);

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.COMPASS)
                .setName(Component.literal("World Settings").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .glow()
                .build());

        boolean hasSpawn = SpawnManager.hasSpawn();

        // Set Spawn
        gui.setSlot(10, new GuiElementBuilder(Items.RESPAWN_ANCHOR)
                .setName(Component.literal("Set Spawn Location").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Current: " + SpawnManager.getSpawnInfo()).withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("▶ Click to set to your location").withStyle(ChatFormatting.GRAY))
                .setCallback((int index, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    SpawnManager.setSpawn(player);
                    openPage(player, AdminPage.WORLD);
                })
                .build());

        // Test Spawn
        if (hasSpawn) {
            gui.setSlot(11, new GuiElementBuilder(Items.ENDER_PEARL)
                    .setName(Component.literal("Test Spawn").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Teleport to spawn").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("▶ Click to teleport").withStyle(ChatFormatting.YELLOW))
                    .setCallback((int index, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                        SpawnManager.teleportToSpawn(player);
                        gui.close();
                    })
                    .build());
        }

        // Place Auction Master
        gui.setSlot(19, new GuiElementBuilder(Items.EMERALD_BLOCK)
                .setName(Component.literal("Place Auction Master").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Spawn at your location").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("▶ Click to spawn").withStyle(ChatFormatting.YELLOW))
                .setCallback((int index, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    var world = player.level();
                    AuctionMasterManager.spawnAuctionMaster(world, player.getX(), player.getY(), player.getZ(), player.getYRot());
                    player.sendSystemMessage(Component.literal("✓ Auction Master spawned!").withStyle(ChatFormatting.GREEN));
                    gui.close();
                })
                .build());

        // Place Underground Auctioneer
        gui.setSlot(20, new GuiElementBuilder(Items.PURPLE_STAINED_GLASS)
                .setName(Component.literal("Place Underground Auctioneer").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Spawn at your location").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("▶ Click to spawn").withStyle(ChatFormatting.YELLOW))
                .setCallback((int index, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    var world = player.level();
                    UndergroundAuctionManager.spawnAuctioneer(world, player.getX(), player.getY(), player.getZ(), player.getYRot());
                    player.sendSystemMessage(Component.literal("✓ Underground Auctioneer spawned!").withStyle(ChatFormatting.LIGHT_PURPLE));
                    gui.close();
                })
                .build());

        // Spawn Protection Region
        gui.setSlot(21, new GuiElementBuilder(SpawnProtectionManager.isActive() ? Items.ORANGE_TERRACOTTA : Items.WHITE_TERRACOTTA)
                .setName(Component.literal("Spawn Protection").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Status: " + (SpawnProtectionManager.isActive() ? "§aACTIVE" : "§cINACTIVE")).withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Min: " + SpawnProtectionManager.getMinX() + "," + SpawnProtectionManager.getMinY() + "," + SpawnProtectionManager.getMinZ()).withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal("Max: " + SpawnProtectionManager.getMaxX() + "," + SpawnProtectionManager.getMaxY() + "," + SpawnProtectionManager.getMaxZ()).withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal("Left: Set corner 1 here | Right: Set corner 2 here").withStyle(ChatFormatting.DARK_GRAY))
                .addLoreLine(Component.literal("Shift-click: Toggle active").withStyle(ChatFormatting.DARK_GRAY))
                .setCallback((int index, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    if (type.shift) {
                        if (SpawnProtectionManager.isActive()) SpawnProtectionManager.clearRegion();
                        else SpawnProtectionManager.setRegion(SpawnProtectionManager.getMinX(), SpawnProtectionManager.getMinY(), SpawnProtectionManager.getMinZ(), SpawnProtectionManager.getMaxX(), SpawnProtectionManager.getMaxY(), SpawnProtectionManager.getMaxZ());
                        boolean nowActive = SpawnProtectionManager.isActive();
                        player.sendSystemMessage(Component.literal("✓ Spawn protection " + (nowActive ? "enabled" : "disabled") + "!")
                                .withStyle(nowActive ? ChatFormatting.GREEN : ChatFormatting.RED));
                    } else if (type.isLeft) {
                        int x = (int) player.getX(), y = (int) player.getY(), z = (int) player.getZ();
                        SpawnProtectionManager.setRegion(x, y, z, SpawnProtectionManager.getMaxX(), SpawnProtectionManager.getMaxY(), SpawnProtectionManager.getMaxZ());
                        player.sendSystemMessage(Component.literal("✓ Corner 1 set to " + x + "," + y + "," + z + "!").withStyle(ChatFormatting.GREEN));
                    } else {
                        int x = (int) player.getX(), y = (int) player.getY(), z = (int) player.getZ();
                        SpawnProtectionManager.setRegion(SpawnProtectionManager.getMinX(), SpawnProtectionManager.getMinY(), SpawnProtectionManager.getMinZ(), x, y, z);
                        player.sendSystemMessage(Component.literal("✓ Corner 2 set to " + x + "," + y + "," + z + "!").withStyle(ChatFormatting.GREEN));
                    }
                    openPage(player, AdminPage.WORLD);
                })
                .build());

        // Weather Override
        net.minecraft.server.level.ServerLevel overworld = PoliticalServer.server.overworld();
        boolean isRaining = overworld.isRaining();
        boolean isThundering = overworld.isThundering();
        gui.setSlot(22, new GuiElementBuilder(isRaining ? Items.WATER_BUCKET : Items.SUNFLOWER)
                .setName(Component.literal("Weather Control").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Current: " + (isThundering ? "§cThunder" : isRaining ? "§bRain" : "§eClear")).withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Left: Clear | Right: Rain | Shift: Thunder").withStyle(ChatFormatting.DARK_GRAY))
                .setCallback((int index, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    var cmds = PoliticalServer.server.getCommands();
                    var src = player.createCommandSourceStack().withSuppressedOutput();
                    if (type.shift) {
                        cmds.performPrefixedCommand(src, "weather thunder 24000");
                        player.sendSystemMessage(Component.literal("✓ Weather set to Thunder!").withStyle(ChatFormatting.AQUA));
                    } else if (type.isLeft) {
                        cmds.performPrefixedCommand(src, "weather clear 24000");
                        player.sendSystemMessage(Component.literal("✓ Weather set to Clear!").withStyle(ChatFormatting.YELLOW));
                    } else {
                        cmds.performPrefixedCommand(src, "weather rain 24000");
                        player.sendSystemMessage(Component.literal("✓ Weather set to Rain!").withStyle(ChatFormatting.AQUA));
                    }
                    openPage(player, AdminPage.WORLD);
                })
                .build());

        // Time Control
        long currentTime = overworld.getOverworldClockTime() % 24000;
        gui.setSlot(23, new GuiElementBuilder(currentTime >= 13000 ? Items.ENDER_PEARL : Items.GLOWSTONE)
                .setName(Component.literal("Time Control").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Current time: §f" + currentTime).withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Left: Daytime (1000) | Right: Midnight (18000)").withStyle(ChatFormatting.DARK_GRAY))
                .addLoreLine(Component.literal("Shift+Left: Sunrise (23000) | Shift+Right: Sunset (13000)").withStyle(ChatFormatting.DARK_GRAY))
                .setCallback((int index, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    var cmds = PoliticalServer.server.getCommands();
                    var src = player.createCommandSourceStack().withSuppressedOutput();
                    if (type.shift && type.isLeft) {
                        cmds.performPrefixedCommand(src, "time set 23000");
                        player.sendSystemMessage(Component.literal("✓ Time set to sunrise!").withStyle(ChatFormatting.YELLOW));
                    } else if (type.shift) {
                        cmds.performPrefixedCommand(src, "time set 13000");
                        player.sendSystemMessage(Component.literal("✓ Time set to sunset!").withStyle(ChatFormatting.GOLD));
                    } else if (type.isLeft) {
                        cmds.performPrefixedCommand(src, "time set 1000");
                        player.sendSystemMessage(Component.literal("✓ Time set to day!").withStyle(ChatFormatting.YELLOW));
                    } else {
                        cmds.performPrefixedCommand(src, "time set 18000");
                        player.sendSystemMessage(Component.literal("✓ Time set to midnight!").withStyle(ChatFormatting.DARK_BLUE));
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
    /** Delegates to the shared frame so the legacy panel matches the console behind it. */
    private static void fillBackground(SimpleGui gui) {
        GuiKit.frame(gui, gui.getSize());
    }

    private static void addBackButton(SimpleGui gui, ServerPlayer player) {
        gui.setSlot(gui.getSize() - 5, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((int index, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openMainPage(player))
                .build());
    }

    private static String capitalize(SlayerManager.SlayerType type) {
        String name = type.name();
        return Character.toString(name.charAt(0)) + name.substring(1).toLowerCase();
    }

    private static void openPlayerSelector(ServerPlayer admin, String action) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, admin, false);
        gui.setTitle(Component.literal("Select Player - " + action.substring(0, 1).toUpperCase() + action.substring(1)));

        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Component.literal(""))
                    .build());
        }

        gui.setSlot(4, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Component.literal("Select a Player").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Action: " + action).withStyle(ChatFormatting.GRAY))
                .build());

        List<ServerPlayer> players = new ArrayList<>(PoliticalServer.server.getPlayerList().getPlayers());
        int slot = 10;
        int maxSlot = 44;

        for (ServerPlayer target : players) {
            if (slot > maxSlot) break;
            if (slot % 9 == 0) slot++;
            if (slot % 9 == 8) slot += 2;

            final ServerPlayer finalTarget = target;
            gui.setSlot(slot, new GuiElementBuilder(Items.PLAYER_HEAD)
                    .setProfile(target.getGameProfile())
                    .setName(Component.literal(target.getName().getString()).withStyle(ChatFormatting.WHITE))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("▶ Click to select").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        executeAction(admin, finalTarget, action);
                    })
                    .build());
            slot++;
        }

        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.GRAY))
                .setCallback((index, type, containerInput, slotGui) -> open(admin))
                .build());

        gui.open();
    }

    private static void executeAction(ServerPlayer admin, ServerPlayer target, String action) {
        switch (action) {
            case "chair" -> {
                DataManager.setChair(target.getUUID().toString());
                DataManager.setChairTermCount(1);
                DataManager.save(PoliticalServer.server);
                admin.sendSystemMessage(Component.literal("✓ Set " + target.getName().getString() + " as Chair!").withStyle(ChatFormatting.GREEN));
                target.sendSystemMessage(Component.literal("You have been appointed as Chair!").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
            }
            case "vicechair" -> {
                DataManager.setViceChair(target.getUUID().toString());
                DataManager.save(PoliticalServer.server);
                admin.sendSystemMessage(Component.literal("✓ Set " + target.getName().getString() + " as Vice Chair!").withStyle(ChatFormatting.GREEN));
                target.sendSystemMessage(Component.literal("You have been appointed as Vice Chair!").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
            }
            case "judge" -> {
                DataManager.setJudge(target.getUUID().toString());
                DataManager.save(PoliticalServer.server);
                admin.sendSystemMessage(Component.literal("✓ Set " + target.getName().getString() + " as Judge!").withStyle(ChatFormatting.GREEN));
                target.sendSystemMessage(Component.literal("You have been appointed as Judge!").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
            }
            case "dictator" -> {
                String chair = DataManager.getChair();
                if (!target.getUUID().toString().equals(chair)) {
                    admin.sendSystemMessage(Component.literal("✗ Player must be Chair to become Dictator!").withStyle(ChatFormatting.RED));
                    open(admin);
                    return;
                }
                DictatorManager.setDictator(target);
                admin.sendSystemMessage(Component.literal("✓ Set " + target.getName().getString() + " as DICTATOR!").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
            }
            case "exile" -> {
                Random rand = new Random();
                double distance = 10_000 + rand.nextDouble() * 90_000;
                double angle = rand.nextDouble() * Math.PI * 2;
                double x = Math.cos(angle) * distance;
                double z = Math.sin(angle) * distance;
                PlayerTeleportHelper.teleport(target, PoliticalServer.server.overworld(), x, 100, z, 0f, 0f);
                target.sendSystemMessage(Component.literal("You have been EXILED!").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
                admin.sendSystemMessage(Component.literal("✓ Exiled " + target.getName().getString() + "!").withStyle(ChatFormatting.RED));
                for (ServerPlayer p : PoliticalServer.server.getPlayerList().getPlayers()) {
                    p.sendSystemMessage(Component.literal("⚖ " + target.getName().getString() + " has been exiled!").withStyle(ChatFormatting.RED));
                }
            }
            case "smite" -> {
                DictatorManager.smitePlayer(admin, target);
                admin.sendSystemMessage(Component.literal("✓ Smited " + target.getName().getString() + "!").withStyle(ChatFormatting.YELLOW));
            }
            case "release" -> {
                PrisonManager.release(target);
                admin.sendSystemMessage(Component.literal("✓ Released " + target.getName().getString() + "!").withStyle(ChatFormatting.GREEN));
                target.sendSystemMessage(Component.literal("You have been released!").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
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
                PlayerTeleportHelper.teleport(admin, (net.minecraft.server.level.ServerLevel) target.level(),
                    target.getX(), target.getY(), target.getZ(),
                    admin.getYRot(), admin.getXRot());
                admin.sendSystemMessage(Component.literal("✓ Teleported to " + target.getName().getString() + "!").withStyle(ChatFormatting.GREEN));
                return;
            }
            case "reset_bounty" -> {
                for (SlayerManager.SlayerType t : SlayerManager.SlayerType.values()) {
                    SlayerData.setSlayerLevel(target.getUUID().toString(), t, 0);
                    SlayerData.setSlayerXp(target.getUUID().toString(), t, 0);
                }
                DataManager.save(PoliticalServer.server);
                admin.sendSystemMessage(Component.literal("✓ Reset all bounty data for " + target.getName().getString() + "!").withStyle(ChatFormatting.RED));
                target.sendSystemMessage(Component.literal("An admin has reset your bounty progress.").withStyle(ChatFormatting.RED));
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

    private static void openSetCoinsSign(ServerPlayer admin, ServerPlayer target) {
        int current = CoinManager.getCoins(target);
        SignGui signGui = new SignGui(admin) {
            @Override
            public void onManualClose() {
                // 'set exact' — only accepts non-negative integers
                Integer amount = parseSignAmount(this.getLine(0).getString(), false);
                if (amount == null) { openPlayersPage(admin); return; }
                CoinManager.setCoins(target.getUUID().toString(), Math.max(0, amount));
                admin.sendSystemMessage(Component.literal("✓ Set " + target.getName().getString() + "'s coins to " + amount + "!").withStyle(ChatFormatting.GREEN));
                openPlayersPage(admin);
            }
        };
        signGui.setLine(0, Component.literal(""));
        signGui.setLine(1, Component.literal("Set exact coins for"));
        signGui.setLine(2, Component.literal(target.getName().getString()));
        signGui.setLine(3, Component.literal("Current: " + current));
        signGui.open();
    }

    private static void openSetCreditsSign(ServerPlayer admin, ServerPlayer target) {
        int current = DataManager.getCredits(target.getUUID().toString());
        SignGui signGui = new SignGui(admin) {
            @Override
            public void onManualClose() {
                // 'set exact' — only accepts non-negative integers
                Integer amount = parseSignAmount(this.getLine(0).getString(), false);
                if (amount == null) { openPlayersPage(admin); return; }
                DataManager.setCredits(target.getUUID().toString(), Math.max(0, amount));
                DataManager.save(PoliticalServer.server);
                admin.sendSystemMessage(Component.literal("✓ Set " + target.getName().getString() + "'s credits to " + amount + "!").withStyle(ChatFormatting.GREEN));
                openPlayersPage(admin);
            }
        };
        signGui.setLine(0, Component.literal(""));
        signGui.setLine(1, Component.literal("Set exact credits for"));
        signGui.setLine(2, Component.literal(target.getName().getString()));
        signGui.setLine(3, Component.literal("Current: " + current));
        signGui.open();
    }

    private static void openCreditsGui(ServerPlayer admin) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, admin, false);
        gui.setTitle(Component.literal("Give Credits - Select Player"));

        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Component.literal(""))
                    .build());
        }

        gui.setSlot(4, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Component.literal("Give Credits").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Select a player").withStyle(ChatFormatting.GRAY))
                .glow()
                .build());

        List<ServerPlayer> players = new ArrayList<>(PoliticalServer.server.getPlayerList().getPlayers());
        int slot = 10;
        int maxSlot = 44;

        for (ServerPlayer target : players) {
            if (slot > maxSlot) break;
            if (slot % 9 == 0) slot++;
            if (slot % 9 == 8) slot += 2;

            final ServerPlayer finalTarget = target;
            int currentCredits = DataManager.getCredits(target.getUUID().toString());

            gui.setSlot(slot, new GuiElementBuilder(Items.PLAYER_HEAD)
                    .setProfile(target.getGameProfile())
                    .setName(Component.literal(target.getName().getString()).withStyle(ChatFormatting.WHITE))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Credits: " + currentCredits).withStyle(ChatFormatting.YELLOW))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("▶ Click to give credits").withStyle(ChatFormatting.GREEN))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        openCreditsAmountSelector(admin, finalTarget);
                    })
                    .build());
            slot++;
        }

        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.GRAY))
                .setCallback((int index, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openPage(admin, AdminPage.PLAYERS))
                .build());

        gui.open();
    }
    private static void spawnTestBoss(ServerPlayer admin, SlayerManager.SlayerType slayerType, int tier) {
        // Cancel any existing quest
        if (SlayerManager.hasActiveQuest(admin)) {
            SlayerManager.cancelQuest(admin);
        }

        // Directly spawn boss without starting a quest (admin bypass)
        SlayerManager.spawnTestBoss(admin, slayerType, tier);

        admin.sendSystemMessage(Component.literal("✔ Spawned " + slayerType.bossName + " Tier " + tier + "!")
                .withStyle(ChatFormatting.GREEN), false);
    }

    private static void openCreditsAmountSelector(ServerPlayer admin, ServerPlayer target) {
        int currentCredits = DataManager.getCredits(target.getUUID().toString());
        SignGui signGui = new SignGui(admin) {
            @Override
            public void onManualClose() {
                // allow negative to mean "remove" — e.g. "-100" removes 100 credits
                Integer amount = parseSignAmount(this.getLine(0).getString(), true);
                if (amount == null) { openCreditsGui(admin); return; }
                if (amount > 0) {
                    CreditItem.giveCredits(target, amount);
                    admin.sendSystemMessage(Component.literal("✓ Gave " + amount + " credits to " + target.getName().getString() + "!").withStyle(ChatFormatting.GREEN));
                    target.sendSystemMessage(Component.literal("An admin gave you " + amount + " credits!").withStyle(ChatFormatting.GREEN));
                } else if (amount < 0) {
                    CreditItem.removeCredits(target, -amount);
                    admin.sendSystemMessage(Component.literal("✓ Removed " + (-amount) + " credits from " + target.getName().getString() + "!").withStyle(ChatFormatting.RED));
                }
                openCreditsGui(admin);
            }
        };
        signGui.setLine(0, Component.literal(""));
        signGui.setLine(1, Component.literal("+ give / - remove"));
        signGui.setLine(2, Component.literal(target.getName().getString()));
        signGui.setLine(3, Component.literal("Credits: " + currentCredits));
        signGui.open();
    }

    private static void openCreditsRemoveSelector(ServerPlayer admin, ServerPlayer target) {
        // Delegate to the combined sign-based selector
        openCreditsAmountSelector(admin, target);
    }
    private static void openBasicSwordsMenu(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal("⚔ Basic Bounty Swords"));

        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Component.literal("")).build());
        }

        int slot = 10;
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            final SlayerManager.SlayerType finalType = type;
            gui.setSlot(slot, new GuiElementBuilder(Items.IRON_SWORD)
                    .setName(Component.literal(type.displayName + " Bounty Sword").withStyle(type.color, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("2x damage to " + type.displayName + "s").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("Click to receive!").withStyle(ChatFormatting.GREEN))
                    .setCallback((idx, clickType, containerInput, slotGui) -> SlayerItems.giveSlayerSword(player, finalType))
                    .build());
            slot++;
        }

        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openCustomItemsAdminGui(player))
                .build());

        gui.open();
    }
    private static void openUpgradedSwordsMenu(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal("⚔ Upgraded Bounty Swords"));

        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Component.literal("")).build());
        }

        int slot = 10;
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            final SlayerManager.SlayerType finalType = type;
            gui.setSlot(slot, new GuiElementBuilder(Items.DIAMOND_SWORD)
                    .setName(Component.literal(type.displayName + " Bounty Sword II").withStyle(type.color, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("3x damage, counts as 3 kills").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("Click to receive!").withStyle(ChatFormatting.GREEN))
                    .glow()
                    .setCallback((idx, clickType, containerInput, slotGui) -> {
                        ItemStack sword = SlayerItems.createUpgradedSlayerSword(finalType);
                        if (!player.addItem(sword)) {
                            player.drop(sword, false);
                        }
                        player.sendSystemMessage(Component.literal("✔ Received " + finalType.displayName + " Bounty Sword II!")
                                .withStyle(ChatFormatting.GREEN), false);
                    })
                    .build());
            slot++;
        }

        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openCustomItemsAdminGui(player))
                .build());

        gui.open();
    }
    // ═══════════════════════════════════════════════════════════
// BOUNTY ADMIN PAGE
// ═══════════════════════════════════════════════════════════
// SLAYER/BOUNTY ADMIN GUI
// ═══════════════════════════════════════════════════════════

    public static void openSlayerAdminGui(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("⚔ Bounty Administration ⚔"));

        // Background with dark theme
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Component.literal(""))
                    .build());
        }

        // Decorative border
        for (int i = 0; i < 9; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.RED_STAINED_GLASS_PANE).setName(Component.literal("")).build());
            gui.setSlot(45 + i, new GuiElementBuilder(Items.RED_STAINED_GLASS_PANE).setName(Component.literal("")).build());
        }
        for (int i = 0; i < 6; i++) {
            gui.setSlot(i * 9, new GuiElementBuilder(Items.RED_STAINED_GLASS_PANE).setName(Component.literal("")).build());
            gui.setSlot(i * 9 + 8, new GuiElementBuilder(Items.RED_STAINED_GLASS_PANE).setName(Component.literal("")).build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.NETHERITE_SWORD)
                .setName(Component.literal("⚔ Bounty Admin Panel ⚔").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Manage all bounty items & player data").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Online: §a" + PoliticalServer.server.getPlayerList().getPlayers().size() + " players"))
                .glow()
                .build());

        // ═══ ROW 1: GIVE ITEMS ═══
        // Section label
        gui.setSlot(10, new GuiElementBuilder(Items.CHEST)
                .setName(Component.literal("📦 GIVE ITEMS").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .glow()
                .build());

        // T1 Armor
        gui.setSlot(11, new GuiElementBuilder(Items.LEATHER_CHESTPLATE)
                .setName(Component.literal("T1 Hunter Armor").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Level " + SlayerItems.T1_ARMOR_LEVEL_REQ + " requirement").withStyle(ChatFormatting.DARK_GRAY))
                .addLoreLine(Component.literal("Click to give →").withStyle(ChatFormatting.GREEN))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openArmorGiveGui(player, 1))
                .build());

        // T2 Armor
        gui.setSlot(12, new GuiElementBuilder(Items.NETHERITE_CHESTPLATE)
                .setName(Component.literal("T2 Bounty II Armor").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Level " + SlayerItems.T2_ARMOR_LEVEL_REQ + " requirement").withStyle(ChatFormatting.DARK_GRAY))
                .addLoreLine(Component.literal("Click to give →").withStyle(ChatFormatting.GREEN))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openArmorGiveGui(player, 2))
                .glow()
                .build());

        // Legendary Items
        gui.setSlot(13, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Component.literal("★ Legendary Items").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("• Zombie Berserker Helmet").withStyle(ChatFormatting.DARK_GREEN))
                .addLoreLine(Component.literal("• Venomous Crawler Leggings").withStyle(ChatFormatting.DARK_RED))
                .addLoreLine(Component.literal("• Bone Desperado's Bow").withStyle(ChatFormatting.WHITE))
                .addLoreLine(Component.literal("• Gelatinous Rustler Boots").withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal("• Sculk Terror Chestplate").withStyle(ChatFormatting.DARK_AQUA))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to give →").withStyle(ChatFormatting.YELLOW))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openSpecialItemsGui(player))
                .glow()
                .build());

        // Bounty Swords
        gui.setSlot(14, new GuiElementBuilder(Items.DIAMOND_SWORD)
                .setName(Component.literal("⚔ Bounty Swords").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("• T1 Swords (2x damage)").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("• T2 Swords II (3x damage)").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to give →").withStyle(ChatFormatting.YELLOW))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openSwordsGiveGui(player))
                .build());

        // Cores
        gui.setSlot(15, new GuiElementBuilder(Items.SCULK)
                .setName(Component.literal("✦ Bounty Cores").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Crafting materials for all types").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to give →").withStyle(ChatFormatting.YELLOW))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openCoresGiveGui(player))
                .build());

        // Piglin Items
        gui.setSlot(16, new GuiElementBuilder(Items.GOLDEN_HELMET)
                .setName(Component.literal("👑 Piglin Items").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("• Crown of Greed").withStyle(ChatFormatting.GOLD))
                .addLoreLine(Component.literal("• Piglin Core").withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to give →").withStyle(ChatFormatting.YELLOW))
                .glow()
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openPiglinItemsGui(player))
                .build());

        // Other Weapons
        gui.setSlot(17, new GuiElementBuilder(Items.GOLDEN_AXE)
                .setName(Component.literal("⚒ Other Weapons").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("• The Gavel • Hermes Shoes").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("• Harvey's Stick • HPEBM").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to give →").withStyle(ChatFormatting.YELLOW))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openOtherWeaponsGui(player))
                .build());

        // ═══ ROW 2: PLAYER MANAGEMENT ═══
        // Section label
        gui.setSlot(19, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Component.literal("👤 PLAYER MANAGEMENT").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .glow()
                .build());

        // Set Level
        gui.setSlot(20, new GuiElementBuilder(Items.EXPERIENCE_BOTTLE)
                .setName(Component.literal("Set Bounty Level").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Set any player's bounty level").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("for any bounty type").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to manage →").withStyle(ChatFormatting.YELLOW))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openSetLevelGui(player))
                .build());

        // Set XP
        gui.setSlot(21, new GuiElementBuilder(Items.ENCHANTING_TABLE)
                .setName(Component.literal("Add Bounty XP").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Add XP to any player's bounty").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to manage →").withStyle(ChatFormatting.YELLOW))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openSetXpGui(player))
                .build());

        // View Stats
        gui.setSlot(22, new GuiElementBuilder(Items.WRITTEN_BOOK)
                .setName(Component.literal("View Player Stats").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("View detailed bounty stats").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to view →").withStyle(ChatFormatting.YELLOW))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openViewStatsGui(player))
                .build());

        // Cancel Quest
        gui.setSlot(23, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("Cancel Player Quest").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Force cancel a player's").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("active bounty quest").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to manage →").withStyle(ChatFormatting.YELLOW))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openCancelQuestGui(player))
                .build());

        // Reset Data
        gui.setSlot(24, new GuiElementBuilder(Items.TNT)
                .setName(Component.literal("⚠ Reset Player Data").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Reset ALL bounty progress").withStyle(ChatFormatting.RED))
                .addLoreLine(Component.literal("for a specific player").withStyle(ChatFormatting.RED))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("⚠ DESTRUCTIVE").withStyle(ChatFormatting.DARK_RED))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openResetDataGui(player))
                .build());

        // ═══ ROW 3: BOSS MANAGEMENT ═══
        // Section label
        gui.setSlot(28, new GuiElementBuilder(Items.WITHER_SKELETON_SKULL)
                .setName(Component.literal("💀 BOSS MANAGEMENT").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .glow()
                .build());

        // Force Spawn boss
        gui.setSlot(29, new GuiElementBuilder(Items.IRON_GOLEM_SPAWN_EGG)
                .setName(Component.literal("⚔ Force Spawn Boss").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Force spawn a bounty boss").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("for a player's active quest").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to select player →").withStyle(ChatFormatting.YELLOW))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openForceSpawnBossGui(player))
                .build());

        // Kill all bosses
        gui.setSlot(30, new GuiElementBuilder(Items.DIAMOND_SWORD)
                .setName(Component.literal("☠ Kill All Bosses").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Kill all active bounty bosses").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to execute →").withStyle(ChatFormatting.YELLOW))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    int killed = SlayerManager.killAllBosses();
                    player.sendSystemMessage(Component.literal("✓ Killed " + killed + " active bounty bosses").withStyle(ChatFormatting.GREEN), false);
                })
                .build());

        // List active bosses
        gui.setSlot(31, new GuiElementBuilder(Items.SPYGLASS)
                .setName(Component.literal("👁 List Active Bosses").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("View all currently spawned").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("bounty bosses and their owners").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to view →").withStyle(ChatFormatting.YELLOW))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openActiveBossesGui(player))
                .build());

        // ═══ ROW 4: QUICK ACTIONS ═══
        // Section label
        gui.setSlot(37, new GuiElementBuilder(Items.EMERALD)
                .setName(Component.literal("⚡ QUICK ACTIONS").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .glow()
                .build());

        // Give all items to self
        gui.setSlot(38, new GuiElementBuilder(Items.CHEST_MINECART)
                .setName(Component.literal("Give All Items (Self)").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Give yourself one of each").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("bounty item for testing").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to receive →").withStyle(ChatFormatting.YELLOW))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openQuickGiveGui(player))
                .build());

        // Announce to all
        gui.setSlot(39, new GuiElementBuilder(Items.BELL)
                .setName(Component.literal("🔔 Announce Bounty Event").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Send a server-wide bounty").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("announcement message").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to announce →").withStyle(ChatFormatting.YELLOW))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openAnnounceGui(player))
                .build());

        // Reload config
        gui.setSlot(40, new GuiElementBuilder(Items.STRUCTURE_BLOCK)
                .setName(Component.literal("🔄 Reload Bounty Config").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Reload bounty configuration").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to reload →").withStyle(ChatFormatting.YELLOW))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    DataManager.save(PoliticalServer.server);
                    player.sendSystemMessage(Component.literal("✓ Bounty configuration reloaded!").withStyle(ChatFormatting.GREEN), false);
                })
                .build());

        // Back button
        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back to Admin Panel").withStyle(ChatFormatting.GRAY))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openMainPage(player))
                .build());

        gui.open();
    }

    private static void openActiveBossesGui(ServerPlayer admin) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, admin, false);
        gui.setTitle(Component.literal("👁 Active Bounty Bosses"));

        fillBackground(gui);

        int slot = 0;
        for (Map.Entry<java.util.UUID, String> entry : SlayerManager.getActiveBosses().entrySet()) {
            if (slot >= 18) break;

            java.util.UUID bossUuid = entry.getKey();
            String ownerUuid = entry.getValue();
            
            // Find boss entity
            net.minecraft.world.entity.Entity boss = null;
            for (net.minecraft.server.level.ServerLevel world : PoliticalServer.server.getAllLevels()) {
                net.minecraft.world.entity.Entity e = world.getEntity(bossUuid);
                if (e != null) {
                    boss = e;
                    break;
                }
            }

            if (boss != null) {
                ServerPlayer owner = PoliticalServer.server.getPlayerList().getPlayer(java.util.UUID.fromString(ownerUuid));
                String ownerName = owner != null ? owner.getName().getString() : "Unknown";

                GuiElementBuilder bossHead = new GuiElementBuilder(Items.PLAYER_HEAD);
                if (owner != null) {
                    bossHead.setProfile(owner.getGameProfile());
                }
                bossHead.setName(Component.literal(ownerName).withStyle(ChatFormatting.YELLOW))
                        .addLoreLine(Component.literal(""))
                        .addLoreLine(Component.literal("Boss: " + boss.getName().getString()).withStyle(ChatFormatting.RED))
                        .addLoreLine(Component.literal("Health: " + String.format("%.0f", ((net.minecraft.world.entity.LivingEntity)boss).getHealth()) + "/" + String.format("%.0f", ((net.minecraft.world.entity.LivingEntity)boss).getMaxHealth())).withStyle(ChatFormatting.GREEN))
                        .addLoreLine(Component.literal("Location: " + boss.blockPosition().toShortString()).withStyle(ChatFormatting.GRAY));
                gui.setSlot(slot, bossHead.build());
                slot++;
            }
        }

        if (slot == 0) {
            gui.setSlot(13, new GuiElementBuilder(Items.BARRIER)
                    .setName(Component.literal("No active bosses").withStyle(ChatFormatting.GRAY))
                    .build());
        }

        gui.setSlot(26, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.GRAY))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openSlayerAdminGui(admin))
                .build());

        gui.open();
    }

    private static void openQuickGiveGui(ServerPlayer admin) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, admin, false);
        gui.setTitle(Component.literal("Quick Give - Select Type"));

        fillBackground(gui);

        gui.setSlot(10, new GuiElementBuilder(Items.NETHERITE_CHESTPLATE)
                .setName(Component.literal("All T1 Armor Sets").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Give all T1 armor pieces").withStyle(ChatFormatting.GRAY))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    for (SlayerManager.SlayerType t : SlayerManager.SlayerType.values()) {
                        for (String piece : new String[]{"Helmet", "Chestplate", "Leggings", "Boots"}) {
                            admin.addItem(SlayerItems.createT1Armor(t, piece));
                        }
                    }
                    admin.sendSystemMessage(Component.literal("✓ Gave all T1 armor sets!").withStyle(ChatFormatting.GREEN), false);
                })
                .build());

        gui.setSlot(11, new GuiElementBuilder(Items.DIAMOND_CHESTPLATE)
                .setName(Component.literal("All T2 Armor Sets").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Give all T2 armor pieces").withStyle(ChatFormatting.GRAY))
                .glow()
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    for (SlayerManager.SlayerType t : SlayerManager.SlayerType.values()) {
                        for (String piece : new String[]{"Helmet", "Chestplate", "Leggings", "Boots"}) {
                            admin.addItem(SlayerItems.createT2Armor(t, piece));
                        }
                    }
                    admin.sendSystemMessage(Component.literal("✓ Gave all T2 armor sets!").withStyle(ChatFormatting.GREEN), false);
                })
                .build());

        gui.setSlot(12, new GuiElementBuilder(Items.DIAMOND_SWORD)
                .setName(Component.literal("All T1 Swords").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Give all T1 bounty swords").withStyle(ChatFormatting.GRAY))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    for (SlayerManager.SlayerType t : SlayerManager.SlayerType.values()) {
                        SlayerItems.giveSlayerSword(admin, t);
                    }
                    admin.sendSystemMessage(Component.literal("✓ Gave all T1 swords!").withStyle(ChatFormatting.GREEN), false);
                })
                .build());

        gui.setSlot(13, new GuiElementBuilder(Items.NETHERITE_SWORD)
                .setName(Component.literal("All T2 Swords").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Give all T2 bounty swords").withStyle(ChatFormatting.GRAY))
                .glow()
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    for (SlayerManager.SlayerType t : SlayerManager.SlayerType.values()) {
                        ItemStack sword = SlayerItems.createUpgradedSlayerSword(t);
                        admin.addItem(sword);
                    }
                    admin.sendSystemMessage(Component.literal("✓ Gave all T2 swords!").withStyle(ChatFormatting.GREEN), false);
                })
                .build());

        gui.setSlot(14, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Component.literal("All Cores").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Give one of each core").withStyle(ChatFormatting.GRAY))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    for (SlayerManager.SlayerType t : SlayerManager.SlayerType.values()) {
                        admin.addItem(SlayerItems.createCore(t));
                    }
                    admin.sendSystemMessage(Component.literal("✓ Gave all cores!").withStyle(ChatFormatting.GREEN), false);
                })
                .build());

        gui.setSlot(15, new GuiElementBuilder(Items.CHEST)
                .setName(Component.literal("Everything!").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Give ALL bounty items").withStyle(ChatFormatting.GRAY))
                .glow()
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    // All armor
                    for (SlayerManager.SlayerType t : SlayerManager.SlayerType.values()) {
                        for (String piece : new String[]{"Helmet", "Chestplate", "Leggings", "Boots"}) {
                            admin.addItem(SlayerItems.createT1Armor(t, piece));
                            admin.addItem(SlayerItems.createT2Armor(t, piece));
                        }
                        admin.addItem(SlayerItems.createCore(t));
                        SlayerItems.giveSlayerSword(admin, t);
                        admin.addItem(SlayerItems.createUpgradedSlayerSword(t));
                    }
                    admin.sendSystemMessage(Component.literal("✓ Gave EVERYTHING!").withStyle(ChatFormatting.GOLD), false);
                })
                .build());

        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.GRAY))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openSlayerAdminGui(admin))
                .build());

        gui.open();
    }

    private static void openAnnounceGui(ServerPlayer admin) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, admin, false);
        gui.setTitle(Component.literal("🔔 Bounty Announcement"));

        fillBackground(gui);

        gui.setSlot(10, new GuiElementBuilder(Items.ZOMBIE_HEAD)
                .setName(Component.literal("Zombie Bounty Event").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Announce a zombie bounty event").withStyle(ChatFormatting.GRAY))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    announceBountyEvent("ZOMBIE", "The Undying Outlaw has appeared!");
                    admin.sendSystemMessage(Component.literal("✓ Announcement sent!").withStyle(ChatFormatting.GREEN), false);
                })
                .build());

        gui.setSlot(11, new GuiElementBuilder(Items.SPIDER_EYE)
                .setName(Component.literal("Spider Bounty Event").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    announceBountyEvent("SPIDER", "The Venomous Bandit lurks in the shadows!");
                    admin.sendSystemMessage(Component.literal("✓ Announcement sent!").withStyle(ChatFormatting.GREEN), false);
                })
                .build());

        gui.setSlot(12, new GuiElementBuilder(Items.BONE)
                .setName(Component.literal("Skeleton Bounty Event").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    announceBountyEvent("SKELETON", "The Bone Desperado challenges all!");
                    admin.sendSystemMessage(Component.literal("✓ Announcement sent!").withStyle(ChatFormatting.GREEN), false);
                })
                .build());

        gui.setSlot(13, new GuiElementBuilder(Items.SCULK)
                .setName(Component.literal("Warden Bounty Event").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                .glow()
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    announceBountyEvent("IRON_GOLEM", "The Sculk Terror emerges from the deep!");
                    admin.sendSystemMessage(Component.literal("✓ Announcement sent!").withStyle(ChatFormatting.GREEN), false);
                })
                .build());

        gui.setSlot(14, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Component.literal("Piglin Bounty Event").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    announceBountyEvent("PIGLIN", "The Gilded Ravager demands tribute!");
                    admin.sendSystemMessage(Component.literal("✓ Announcement sent!").withStyle(ChatFormatting.GREEN), false);
                })
                .build());

        gui.setSlot(16, new GuiElementBuilder(Items.BELL)
                .setName(Component.literal("General Bounty Alert").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Send a generic bounty alert").withStyle(ChatFormatting.GRAY))
                .glow()
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    for (ServerPlayer p : PoliticalServer.server.getPlayerList().getPlayers()) {
                        p.sendSystemMessage(Component.literal(""), false);
                        p.sendSystemMessage(Component.literal("══════════════════════════════════").withStyle(ChatFormatting.GOLD), false);
                        p.sendSystemMessage(Component.literal("  ⚔ BOUNTY HUNTERS NEEDED! ⚔").withStyle(ChatFormatting.RED, ChatFormatting.BOLD), false);
                        p.sendSystemMessage(Component.literal("══════════════════════════════════").withStyle(ChatFormatting.GOLD), false);
                        p.sendSystemMessage(Component.literal(""), false);
                    }
                    admin.sendSystemMessage(Component.literal("✓ Announcement sent!").withStyle(ChatFormatting.GREEN), false);
                })
                .build());

        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.GRAY))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openSlayerAdminGui(admin))
                .build());

        gui.open();
    }

    private static void announceBountyEvent(String type, String message) {
        SlayerManager.SlayerType slayerType = SlayerManager.SlayerType.fromString(type);
        if (slayerType == null) return;

        for (ServerPlayer p : PoliticalServer.server.getPlayerList().getPlayers()) {
            p.sendSystemMessage(Component.literal(""), false);
            p.sendSystemMessage(Component.literal("══════════════════════════════════").withStyle(ChatFormatting.GOLD), false);
            p.sendSystemMessage(Component.literal("  ⚠ BOUNTY ALERT! ⚠").withStyle(slayerType.color, ChatFormatting.BOLD), false);
            p.sendSystemMessage(Component.literal(""), false);
            p.sendSystemMessage(Component.literal("  " + message).withStyle(slayerType.color), false);
            p.sendSystemMessage(Component.literal(""), false);
            p.sendSystemMessage(Component.literal("  Use /bounty to hunt " + slayerType.displayName + "s!").withStyle(ChatFormatting.GRAY), false);
            p.sendSystemMessage(Component.literal("══════════════════════════════════").withStyle(ChatFormatting.GOLD), false);
            p.sendSystemMessage(Component.literal(""), false);
        }
    }
    private static void openForceSpawnBossGui(ServerPlayer admin) {
        List<ServerPlayer> players = new ArrayList<>(PoliticalServer.server.getPlayerList().getPlayers());

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, admin, false);
        gui.setTitle(Component.literal("Force Spawn Boss - Select Player"));

        fillBackground(gui);

        int slot = 0;
        for (ServerPlayer target : players) {
            if (slot >= 18) break;

            SlayerManager.ActiveQuest quest = SlayerManager.getActiveQuest(target);
            boolean hasQuest = quest != null && !quest.bossSpawned;

            GuiElementBuilder builder = new GuiElementBuilder(Items.PLAYER_HEAD)
                    .setProfile(target.getGameProfile())
                    .setName(Component.literal(target.getName().getString())
                            .withStyle(hasQuest ? ChatFormatting.GREEN : ChatFormatting.GRAY));

            if (hasQuest) {
                builder.addLoreLine(Component.literal(""));
                builder.addLoreLine(Component.literal("Quest: " + quest.slayerType.displayName + " T" + quest.tier)
                        .withStyle(ChatFormatting.YELLOW));
                builder.addLoreLine(Component.literal("Kills: " + quest.killCount + "/" + quest.getKillsRequired())
                        .withStyle(ChatFormatting.GRAY));
                builder.addLoreLine(Component.literal(""));
                builder.addLoreLine(Component.literal("Click to force spawn boss").withStyle(ChatFormatting.GREEN));
            } else {
                builder.addLoreLine(Component.literal(""));
                builder.addLoreLine(Component.literal("No active quest or boss already spawned")
                        .withStyle(ChatFormatting.RED));
            }

            final ServerPlayer finalTarget = target;
            builder.setCallback((idx, clickType, containerInput, slotGui) -> {
                SlayerManager.ActiveQuest targetQuest = SlayerManager.getActiveQuest(finalTarget);
                if (targetQuest != null && !targetQuest.bossSpawned) {
                    // Force the kill count to required amount and spawn
                    targetQuest.killCount = targetQuest.getKillsRequired();
                    SlayerManager.checkBossSpawn(finalTarget);
                    admin.sendSystemMessage(Component.literal("✓ Force spawned " + targetQuest.slayerType.bossName +
                            " for " + finalTarget.getName().getString()).withStyle(ChatFormatting.GREEN), false);
                    gui.close();
                } else {
                    admin.sendSystemMessage(Component.literal("✗ Cannot spawn boss for this player").withStyle(ChatFormatting.RED), true);
                }
            });

            gui.setSlot(slot, builder.build());
            slot++;
        }

        // Back button
        gui.setSlot(26, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.GRAY))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openSlayerAdminGui(admin))
                .build());

        gui.open();
    }
// ═══════════════════════════════════════════════════════════
// ARMOR GIVE GUI (T1 or T2)
// ═══════════════════════════════════════════════════════════

    private static void openArmorGiveGui(ServerPlayer player, int tier) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        String tierName = tier == 1 ? "T1 Hunter" : "T2 Bounty II";
        gui.setTitle(Component.literal("Give " + tierName + " Armor"));

        // Background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Component.literal(""))
                    .build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.LEATHER_CHESTPLATE)
                .setName(Component.literal("Select Bounty Type").withStyle(tier == 1 ? ChatFormatting.BLUE : ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Choose a bounty type, then armor piece").withStyle(ChatFormatting.GRAY))
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
                    .setName(Component.literal(type.displayName + " Armor").withStyle(type.color, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to select armor piece").withStyle(ChatFormatting.GRAY))
                    .setCallback((idx, clickType, containerInput, slotGui) -> openArmorPieceSelectGui(player, type, finalTier))
                    .build());
        }

        // Back button
        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.GRAY))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openSlayerAdminGui(player))
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

    private static void openArmorPieceSelectGui(ServerPlayer player, SlayerManager.SlayerType selectedType, int tier) {
        final SlayerManager.SlayerType finalType = selectedType;
        final int finalTier = tier;

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        String tierName = tier == 1 ? "Hunter" : "Bounty II";
        gui.setTitle(Component.literal("Give " + finalType.displayName + " " + tierName + " Armor"));

        // Background
        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Component.literal(""))
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
                    .setName(Component.literal(finalType.displayName + " " + tierName + " " + pieceName)
                            .withStyle(finalType.color, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to receive").withStyle(ChatFormatting.GREEN))
                    .setCallback((idx, clickType, containerInput, slotGui) -> {
                        ItemStack armor = finalTier == 1
                                ? SlayerItems.createT1Armor(finalType, pieceName)
                                : SlayerItems.createT2Armor(finalType, pieceName);
                        player.addItem(armor);
                        player.sendSystemMessage(Component.literal("✓ Gave " + finalType.displayName + " " + tierName + " " + pieceName)
                                .withStyle(ChatFormatting.GREEN), false);
                    })
                    .build());
        }

        // Give Full Set button
        gui.setSlot(16, new GuiElementBuilder(Items.CHEST)
                .setName(Component.literal("Give Full Set").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Give all 4 armor pieces").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, clickType, containerInput, slotGui) -> {
                    for (String pieceName : pieceNames) {
                        ItemStack armor = finalTier == 1
                                ? SlayerItems.createT1Armor(finalType, pieceName)
                                : SlayerItems.createT2Armor(finalType, pieceName);
                        player.addItem(armor);
                    }
                    player.sendSystemMessage(Component.literal("✓ Gave full " + finalType.displayName + " " + tierName + " armor set!")
                            .withStyle(ChatFormatting.GREEN), false);
                })
                .build());

        // Back button
        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.GRAY))
                .setCallback((idx, clickType, containerInput, slotGui) -> openArmorGiveGui(player, finalTier))
                .build());

        gui.open();
    }
    // ═══════════════════════════════════════════════════════════
// SWORDS GIVE GUI
// ═══════════════════════════════════════════════════════════

    private static void openSwordsGiveGui(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("⚔ Give Bounty Swords ⚔"));

        // Background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Component.literal(""))
                    .build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.IRON_SWORD)
                .setName(Component.literal("⚔ Bounty Swords ⚔").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("T1 = Iron Sword (2x damage, Lvl 2)").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("T2 = Diamond Sword (3x damage, Lvl 6)").withStyle(ChatFormatting.LIGHT_PURPLE))
                .build());

        // T1 Swords Row
        gui.setSlot(18, new GuiElementBuilder(Items.PAPER)
                .setName(Component.literal("T1 Bounty Swords").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD))
                .build());

        SlayerManager.SlayerType[] types = SlayerManager.SlayerType.values();
        int[] t1Slots = {19, 20, 21, 22, 23, 24, 25};

        for (int i = 0; i < types.length && i < t1Slots.length; i++) {
            SlayerManager.SlayerType type = types[i];

            gui.setSlot(t1Slots[i], new GuiElementBuilder(Items.IRON_SWORD)
                    .setName(Component.literal(type.displayName + " Bounty Sword").withStyle(type.color, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("2x damage to " + type.displayName + "s").withStyle(ChatFormatting.RED))
                    .addLoreLine(Component.literal("Requires Level 2").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to receive").withStyle(ChatFormatting.GREEN))
                    .setCallback((idx, clickType, containerInput, slotGui) -> {
                        player.addItem(SlayerItems.createSlayerSword(type));
                        player.sendSystemMessage(Component.literal("✓ Gave " + type.displayName + " Bounty Sword")
                                .withStyle(ChatFormatting.GREEN), false);
                    })
                    .build());
        }

        // T2 Swords Row
        gui.setSlot(27, new GuiElementBuilder(Items.PAPER)
                .setName(Component.literal("T2 Bounty Swords II").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .glow()
                .build());

        int[] t2Slots = {28, 29, 30, 31, 32, 33, 34};

        for (int i = 0; i < types.length && i < t2Slots.length; i++) {
            SlayerManager.SlayerType type = types[i];

            gui.setSlot(t2Slots[i], new GuiElementBuilder(Items.DIAMOND_SWORD)
                    .setName(Component.literal(type.displayName + " Bounty Sword II").withStyle(type.color, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("3x damage to " + type.displayName + "s").withStyle(ChatFormatting.RED))
                    .addLoreLine(Component.literal("Requires Level 6").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to receive").withStyle(ChatFormatting.GREEN))
                    .setCallback((idx, clickType, containerInput, slotGui) -> {
                        player.addItem(SlayerItems.createUpgradedSlayerSword(type));
                        player.sendSystemMessage(Component.literal("✓ Gave " + type.displayName + " Bounty Sword II")
                                .withStyle(ChatFormatting.GREEN), false);
                    })
                    .glow()
                    .build());
        }

        // Give All T1 Swords
        gui.setSlot(37, new GuiElementBuilder(Items.CHEST)
                .setName(Component.literal("Give All T1 Swords").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD))
                .setCallback((idx, clickType, containerInput, slotGui) -> {
                    for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
                        player.addItem(SlayerItems.createSlayerSword(type));
                    }
                    player.sendSystemMessage(Component.literal("✓ Gave all T1 Bounty Swords").withStyle(ChatFormatting.GREEN), false);
                })
                .build());

        // Give All T2 Swords
        gui.setSlot(43, new GuiElementBuilder(Items.ENDER_CHEST)
                .setName(Component.literal("Give All T2 Swords").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .setCallback((idx, clickType, containerInput, slotGui) -> {
                    for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
                        player.addItem(SlayerItems.createUpgradedSlayerSword(type));
                    }
                    player.sendSystemMessage(Component.literal("✓ Gave all T2 Bounty Swords II").withStyle(ChatFormatting.GREEN), false);
                })
                .glow()
                .build());

        // Back button
        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.GRAY))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openSlayerAdminGui(player))
                .build());

        gui.open();
    }
    // ═══════════════════════════════════════════════════════════
// SPECIAL/LEGENDARY ITEMS GUI
// ═══════════════════════════════════════════════════════════

    private static void openSpecialItemsGui(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal("★ Legendary Bounty Items ★"));

        // Background
        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Component.literal(""))
                    .build());
        }

        // Zombie Berserker Helmet
        gui.setSlot(10, new GuiElementBuilder(Items.ZOMBIE_HEAD)
                .setName(Component.literal("☠ Zombie Berserker Helmet").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("-50% Max Health").withStyle(ChatFormatting.RED))
                .addLoreLine(Component.literal("+300% Damage").withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal("Requires Zombie Lvl 12").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to receive").withStyle(ChatFormatting.YELLOW))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    player.addItem(SlayerItems.createZombieBerserkerHelmet());
                    player.sendSystemMessage(Component.literal("✓ Gave Zombie Berserker Helmet").withStyle(ChatFormatting.GREEN), false);
                })
                .glow()
                .build());

        // Spider Leggings (Venomous Crawler)
        gui.setSlot(11, new GuiElementBuilder(Items.LEATHER_LEGGINGS)
                .setName(Component.literal("🕷 Venomous Crawler Leggings").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Poison Immunity").withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal("Wall Climbing").withStyle(ChatFormatting.AQUA))
                .addLoreLine(Component.literal("Requires Spider Lvl 5").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to receive").withStyle(ChatFormatting.YELLOW))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    player.addItem(SlayerItems.createSpiderLeggings());
                    player.sendSystemMessage(Component.literal("✓ Gave Venomous Crawler Leggings").withStyle(ChatFormatting.GREEN), false);
                })
                .glow()
                .build());

        // Skeleton Bow
        gui.setSlot(12, new GuiElementBuilder(Items.BOW)
                .setName(Component.literal("🏹 Bone Desperado's Longbow").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("2x damage to Skeletons").withStyle(ChatFormatting.RED))
                .addLoreLine(Component.literal("Infinite Arrows").withStyle(ChatFormatting.AQUA))
                .addLoreLine(Component.literal("Requires Skeleton Lvl 5").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to receive").withStyle(ChatFormatting.YELLOW))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    player.addItem(SlayerItems.createSkeletonBow());
                    player.sendSystemMessage(Component.literal("✓ Gave Bone Desperado's Longbow").withStyle(ChatFormatting.GREEN), false);
                })
                .glow()
                .build());

        // Slime Boots
        gui.setSlot(14, new GuiElementBuilder(Items.LEATHER_BOOTS)
                .setName(Component.literal("🥾 Gelatinous Rustler Boots").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("No Fall Damage").withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal("Double Jump").withStyle(ChatFormatting.AQUA))
                .addLoreLine(Component.literal("Requires Slime Lvl 5").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to receive").withStyle(ChatFormatting.YELLOW))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    player.addItem(SlayerItems.createSlimeBoots());
                    player.sendSystemMessage(Component.literal("✓ Gave Gelatinous Rustler Boots").withStyle(ChatFormatting.GREEN), false);
                })
                .glow()
                .build());

        // Warden Chestplate
        gui.setSlot(16, new GuiElementBuilder(Items.NETHERITE_CHESTPLATE)
                .setName(Component.literal("🛡 Sculk Terror Chestplate").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Darkness Immunity").withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal("Sonic Boom Resistance").withStyle(ChatFormatting.AQUA))
                .addLoreLine(Component.literal("Requires Warden Lvl 10").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to receive").withStyle(ChatFormatting.YELLOW))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    player.addItem(SlayerItems.createWardenChestplate());
                    player.sendSystemMessage(Component.literal("✓ Gave Sculk Terror Chestplate").withStyle(ChatFormatting.GREEN), false);
                })
                .glow()
                .build());

        // Ender Phase Helmet
        gui.setSlot(13, new GuiElementBuilder(Items.NETHERITE_HELMET)
                .setName(Component.literal("⟡ Ender Phase Helmet").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Ender Warp (Right-click teleport)").withStyle(ChatFormatting.LIGHT_PURPLE))
                .addLoreLine(Component.literal("Ender Phase Mode (Sneak+Right-click)").withStyle(ChatFormatting.DARK_PURPLE))
                .addLoreLine(Component.literal("Requires Enderman Lvl 7").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to receive").withStyle(ChatFormatting.YELLOW))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    player.addItem(SlayerItems.createEnderPhaseHelmet());
                    player.sendSystemMessage(Component.literal("✓ Gave Ender Phase Helmet").withStyle(ChatFormatting.GREEN), false);
                })
                .glow()
                .build());

        // Back button
        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.GRAY))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openSlayerAdminGui(player))
                .build());

        gui.open();
    }

    // ═══════════════════════════════════════════════════════════
// PIGLIN ITEMS GUI
// ═══════════════════════════════════════════════════════════

    private static void openPiglinItemsGui(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("👑 Piglin Items"));

        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Component.literal("")).build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.GOLDEN_HELMET)
                .setName(Component.literal("👑 Piglin Items").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Piglin-themed bounty items").withStyle(ChatFormatting.YELLOW))
                .glow()
                .build());

        // Crown of Greed
        gui.setSlot(10, new GuiElementBuilder(Items.GOLDEN_HELMET)
                .setName(Component.literal("👑 Crown of Greed").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Damage scales with your coins").withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal("Lose all coins on death!").withStyle(ChatFormatting.DARK_RED))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to receive").withStyle(ChatFormatting.YELLOW))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    player.addItem(SlayerItems.createCrownOfGreed());
                    player.sendSystemMessage(Component.literal("✓ Gave Crown of Greed").withStyle(ChatFormatting.GREEN), false);
                })
                .glow()
                .build());

        // Piglin Core
        gui.setSlot(11, new GuiElementBuilder(Items.GOLD_NUGGET)
                .setName(Component.literal("✦ Piglin Core ✦").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Used to craft Crown of Greed").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Dropped by Gilded Ravager boss").withStyle(ChatFormatting.GOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to receive").withStyle(ChatFormatting.YELLOW))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    player.addItem(SlayerItems.createPiglinCore());
                    player.sendSystemMessage(Component.literal("✓ Gave Piglin Core").withStyle(ChatFormatting.GREEN), false);
                })
                .glow()
                .build());

        // Piglin Flesh
        gui.setSlot(12, new GuiElementBuilder(Items.GOLD_NUGGET)
                .setName(Component.literal("Piglin Flesh").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Crafting material from Piglins").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to receive").withStyle(ChatFormatting.YELLOW))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    player.addItem(SlayerItems.createPiglinFlesh());
                    player.sendSystemMessage(Component.literal("✓ Gave Piglin Flesh").withStyle(ChatFormatting.GREEN), false);
                })
                .build());

        // Separator
        gui.setSlot(18, new GuiElementBuilder(Items.GOLD_BLOCK)
                .setName(Component.literal("━ Piglin T1 Armor ━").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .build());

        // Piglin T1 Armor pieces
        String[] pieceNames = {"Helmet", "Chestplate", "Leggings", "Boots"};
        Item[] pieceIcons = {Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS};
        int[] t1Slots = {19, 20, 21, 22};
        int[] t2Slots = {28, 29, 30, 31};

        for (int i = 0; i < pieceNames.length; i++) {
            final String pieceName = pieceNames[i];
            gui.setSlot(t1Slots[i], new GuiElementBuilder(pieceIcons[i])
                    .setName(Component.literal("Piglin T1 " + pieceName).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to receive").withStyle(ChatFormatting.GREEN))
                    .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                        player.addItem(SlayerItems.createT1Armor(SlayerManager.SlayerType.PIGLIN, pieceName));
                        player.sendSystemMessage(Component.literal("✓ Gave Piglin T1 " + pieceName).withStyle(ChatFormatting.GREEN), false);
                    })
                    .build());
        }

        // Give T1 Full Set
        gui.setSlot(24, new GuiElementBuilder(Items.CHEST)
                .setName(Component.literal("Give T1 Full Set").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Give all 4 T1 Piglin armor pieces").withStyle(ChatFormatting.YELLOW))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    for (String p : pieceNames) {
                        player.addItem(SlayerItems.createT1Armor(SlayerManager.SlayerType.PIGLIN, p));
                    }
                    player.sendSystemMessage(Component.literal("✓ Gave full Piglin T1 armor set!").withStyle(ChatFormatting.GREEN), false);
                })
                .build());

        // Separator
        gui.setSlot(27, new GuiElementBuilder(Items.GOLD_BLOCK)
                .setName(Component.literal("━ Piglin T2 Armor ━").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .glow()
                .build());

        // Piglin T2 Armor pieces
        for (int i = 0; i < pieceNames.length; i++) {
            final String pieceName = pieceNames[i];
            gui.setSlot(t2Slots[i], new GuiElementBuilder(pieceIcons[i])
                    .setName(Component.literal("Piglin T2 " + pieceName + " II").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to receive").withStyle(ChatFormatting.GREEN))
                    .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                        player.addItem(SlayerItems.createT2Armor(SlayerManager.SlayerType.PIGLIN, pieceName));
                        player.sendSystemMessage(Component.literal("✓ Gave Piglin T2 " + pieceName).withStyle(ChatFormatting.GREEN), false);
                    })
                    .glow()
                    .build());
        }

        // Give T2 Full Set
        gui.setSlot(33, new GuiElementBuilder(Items.ENDER_CHEST)
                .setName(Component.literal("Give T2 Full Set").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Give all 4 T2 Piglin armor pieces").withStyle(ChatFormatting.GOLD))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    for (String p : pieceNames) {
                        player.addItem(SlayerItems.createT2Armor(SlayerManager.SlayerType.PIGLIN, p));
                    }
                    player.sendSystemMessage(Component.literal("✓ Gave full Piglin T2 armor set!").withStyle(ChatFormatting.GREEN), false);
                })
                .glow()
                .build());

        // Back button
        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.GRAY))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openSlayerAdminGui(player))
                .build());

        gui.open();
    }

    // ═══════════════════════════════════════════════════════════
// CORES GIVE GUI
// ═══════════════════════════════════════════════════════════

    private static void openCoresGiveGui(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x4, player, false);
        gui.setTitle(Component.literal("✦ Give Bounty Cores ✦"));

        // Background
        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Component.literal(""))
                    .build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Component.literal("✦ Bounty Cores ✦").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Used for crafting bounty gear").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Left-click: Give 1").withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal("Right-click: Give 16").withStyle(ChatFormatting.GOLD))
                .addLoreLine(Component.literal("Shift-click: Give 64").withStyle(ChatFormatting.RED))
                .build());

        // Core buttons
        SlayerManager.SlayerType[] types = SlayerManager.SlayerType.values();
        int[] slots = {10, 11, 12, 13, 14, 15};

        for (int i = 0; i < types.length && i < slots.length; i++) {
            SlayerManager.SlayerType type = types[i];

            gui.setSlot(slots[i], new GuiElementBuilder(Items.NETHER_STAR)
                    .setName(Component.literal(type.displayName + " Core").withStyle(type.color, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Dropped by " + type.bossName).withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Left-click: Give 1").withStyle(ChatFormatting.YELLOW))
                    .addLoreLine(Component.literal("Right-click: Give 16").withStyle(ChatFormatting.GOLD))
                    .addLoreLine(Component.literal("Shift-click: Give 64").withStyle(ChatFormatting.RED))
                    .setCallback((idx, clickType, containerInput, slotGui) -> {
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
                        player.addItem(core);
                        player.sendSystemMessage(Component.literal("✓ Gave " + amount + "x " + type.displayName + " Core")
                                .withStyle(ChatFormatting.GREEN), false);
                    })
                    .build());
        }

        // Give all cores (1 of each)
        gui.setSlot(28, new GuiElementBuilder(Items.CHEST)
                .setName(Component.literal("Give 1 of Each Core").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .setCallback((idx, clickType, containerInput, slotGui) -> {
                    for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
                        player.addItem(SlayerItems.createSlayerCore(type));
                    }
                    player.sendSystemMessage(Component.literal("✓ Gave 1 of each core").withStyle(ChatFormatting.GREEN), false);
                })
                .build());

        // Give stacks of all cores
        gui.setSlot(29, new GuiElementBuilder(Items.ENDER_CHEST)
                .setName(Component.literal("Give 64 of Each Core").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .setCallback((idx, clickType, containerInput, slotGui) -> {
                    for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
                        ItemStack core = SlayerItems.createSlayerCore(type);
                        core.setCount(64);
                        player.addItem(core);
                    }
                    player.sendSystemMessage(Component.literal("✓ Gave 64 of each core").withStyle(ChatFormatting.GREEN), false);
                })
                .build());

        // Back button
        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.GRAY))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openSlayerAdminGui(player))
                .build());

        gui.open();
    }
    // ═══════════════════════════════════════════════════════════
// OTHER WEAPONS GUI (Gavel, HPEBM, etc.)
// ═══════════════════════════════════════════════════════════

    private static void openOtherWeaponsGui(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x4, player, false);
        gui.setTitle(Component.literal("⚔ Other Weapons ⚔"));

        // Background
        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Component.literal(""))
                    .build());
        }

        // The Gavel (Mace)
        gui.setSlot(10, new GuiElementBuilder(Items.MACE)
                .setName(Component.literal("⚖ The Gavel (Mace)").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Judicial AOE weapon (Mace)").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("25 damage, 4.5 radius").withStyle(ChatFormatting.RED))
                .addLoreLine(Component.literal("3s cooldown").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to receive").withStyle(ChatFormatting.YELLOW))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    player.addItem(CustomItemHandler.createTheGavel());
                    player.sendSystemMessage(Component.literal("✓ Gave The Gavel (Mace)").withStyle(ChatFormatting.GREEN), false);
                })
                .glow()
                .build());

        // The Gavel (Wooden Axe)
        gui.setSlot(11, new GuiElementBuilder(Items.WOODEN_AXE)
                .setName(Component.literal("⚖ The Gavel (Axe)").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Judicial weapon (Wooden Axe)").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Right-click for gavel strike").withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to receive").withStyle(ChatFormatting.YELLOW))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    player.addItem(CustomItemHandler.createGavel());
                    player.sendSystemMessage(Component.literal("✓ Gave The Gavel (Axe)").withStyle(ChatFormatting.GREEN), false);
                })
                .glow()
                .build());

        // Harvey's Stick
        gui.setSlot(12, new GuiElementBuilder(Items.STICK)
                .setName(Component.literal("Harvey's Stick").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Summons lightning on hit").withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to receive").withStyle(ChatFormatting.YELLOW))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    player.addItem(CustomItemHandler.createHarveysStick());
                    player.sendSystemMessage(Component.literal("✓ Gave Harvey's Stick").withStyle(ChatFormatting.GREEN), false);
                })
                .glow()
                .build());

        // Hermes Shoes
        gui.setSlot(13, new GuiElementBuilder(Items.IRON_BOOTS)
                .setName(Component.literal("Hermes Shoes").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Permanent Speed III while worn").withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal("+60% movement speed").withStyle(ChatFormatting.AQUA))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to receive").withStyle(ChatFormatting.YELLOW))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    player.addItem(CustomItemHandler.createHermesShoes());
                    player.sendSystemMessage(Component.literal("✓ Gave Hermes Shoes").withStyle(ChatFormatting.GREEN), false);
                })
                .glow()
                .build());

        // HPEBM Mk.I-V
        gui.setSlot(15, new GuiElementBuilder(Items.IRON_SHOVEL)
                .setName(Component.literal("⚡ HPEBM (All Marks)").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Energy beam modules Mk.I–Mk.V").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Left-click: receive Mk.I through Mk.V").withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal("Right-click: receive Ultra Overclocked").withStyle(ChatFormatting.GOLD))
                .setCallback((idx, clickType, containerInput, slotGui) -> {
                    if (clickType == eu.pb4.sgui.api.ClickType.MOUSE_RIGHT) {
                        player.addItem(CustomItemHandler.createUltraOverclockedBeam());
                        player.sendSystemMessage(Component.literal("✓ Gave HPEBM Ultra Overclocked").withStyle(ChatFormatting.GOLD), false);
                    } else {
                        for (int mk = 1; mk <= 5; mk++) {
                            player.addItem(CustomItemHandler.createHPEBM(mk));
                        }
                        player.sendSystemMessage(Component.literal("✓ Gave HPEBM Mk.I–Mk.V").withStyle(ChatFormatting.GREEN), false);
                    }
                })
                .glow()
                .build());

        // Back button
        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.GRAY))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openSlayerAdminGui(player))
                .build());

        gui.open();
    }

    // ═══════════════════════════════════════════════════════════
// SET PLAYER LEVEL GUI
// ═══════════════════════════════════════════════════════════

    private static void openSetLevelGui(ServerPlayer admin) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, admin, false);
        gui.setTitle(Component.literal("Set Player Bounty Level"));

        // Background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Component.literal(""))
                    .build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.EXPERIENCE_BOTTLE)
                .setName(Component.literal("Set Bounty Level").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Select a player below").withStyle(ChatFormatting.GRAY))
                .build());

        // Get online players
        List<ServerPlayer> players = new ArrayList<>(PoliticalServer.server.getPlayerList().getPlayers());
        int slot = 19;

        for (int i = 0; i < Math.min(players.size(), 14); i++) {
            ServerPlayer target = players.get(i);

            gui.setSlot(slot, new GuiElementBuilder(Items.PLAYER_HEAD)
                    .setProfile(target.getGameProfile())
                    .setName(Component.literal(target.getName().getString()).withStyle(ChatFormatting.YELLOW))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to set levels").withStyle(ChatFormatting.GREEN))
                    .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openSetLevelForPlayerGui(admin, target))
                    .build());

            slot++;
            if (slot == 26) slot = 28; // Skip to next row
        }

        // Back button
        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.GRAY))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openSlayerAdminGui(admin))
                .build());

        gui.open();
    }

    private static void openSetLevelForPlayerGui(ServerPlayer admin, ServerPlayer target) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, admin, false);
        gui.setTitle(Component.literal("Set Level: " + target.getName().getString()));

        // Background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Component.literal(""))
                    .build());
        }

        String uuid = target.getUUID().toString();

        // Show each slayer type
        SlayerManager.SlayerType[] types = SlayerManager.SlayerType.values();
        int[] slots = {10, 11, 12, 13, 14, 15};

        for (int i = 0; i < types.length && i < slots.length; i++) {
            SlayerManager.SlayerType type = types[i];
            int currentLevel = SlayerData.getSlayerLevel(uuid, type);

            gui.setSlot(slots[i], new GuiElementBuilder(type.icon)
                    .setName(Component.literal(type.displayName).withStyle(type.color, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Current Level: " + currentLevel).withStyle(ChatFormatting.YELLOW))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to set level").withStyle(ChatFormatting.GREEN))
                    .setCallback((idx, clickType, containerInput, slotGui) -> openLevelSelectorGui(admin, target, type))
                    .build());
        }

        // Set All to Max
        gui.setSlot(31, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Component.literal("Set All to MAX (12)").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Sets all bounty levels to 12").withStyle(ChatFormatting.YELLOW))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    for (SlayerManager.SlayerType t : SlayerManager.SlayerType.values()) {
                        SlayerData.setSlayerLevel(uuid, t, 12);
                    }
                    admin.sendSystemMessage(Component.literal("✓ Set all levels to 12 for " + target.getName().getString())
                            .withStyle(ChatFormatting.GREEN), false);
                    openSetLevelForPlayerGui(admin, target);
                })
                .glow()
                .build());

        // Reset All to 0
        gui.setSlot(33, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("Reset All to 0").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Resets all bounty levels to 0").withStyle(ChatFormatting.RED))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    for (SlayerManager.SlayerType t : SlayerManager.SlayerType.values()) {
                        SlayerData.setSlayerLevel(uuid, t, 0);
                        SlayerData.setSlayerXp(uuid, t, 0);
                    }
                    admin.sendSystemMessage(Component.literal("✓ Reset all levels for " + target.getName().getString())
                            .withStyle(ChatFormatting.GREEN), false);
                    openSetLevelForPlayerGui(admin, target);
                })
                .build());

        // Back button
        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.GRAY))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openSetLevelGui(admin))
                .build());

        gui.open();
    }

    private static void openLevelSelectorGui(ServerPlayer admin, ServerPlayer target, SlayerManager.SlayerType slayerType) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, admin, false);
        gui.setTitle(Component.literal("Set " + slayerType.displayName + " Level"));

        // Background
        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Component.literal(""))
                    .build());
        }

        String uuid = target.getUUID().toString();
        int currentLevel = SlayerData.getSlayerLevel(uuid, slayerType);

        // Level buttons 0-12
        int[] levelSlots = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
        int[] guiSlots = {9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21};

        for (int i = 0; i < levelSlots.length && i < guiSlots.length; i++) {
            int level = levelSlots[i];
            boolean isCurrent = level == currentLevel;

            gui.setSlot(guiSlots[i], new GuiElementBuilder(isCurrent ? Items.LIME_STAINED_GLASS_PANE : Items.WHITE_STAINED_GLASS_PANE)
                    .setName(Component.literal("Level " + level).withStyle(isCurrent ? ChatFormatting.GREEN : ChatFormatting.YELLOW, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(isCurrent
                            ? Component.literal("✓ Current Level").withStyle(ChatFormatting.GREEN)
                            : Component.literal("Click to set").withStyle(ChatFormatting.GRAY))
                    .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                        SlayerData.setSlayerLevel(uuid, slayerType, level);
                        // Also set appropriate XP for level
                        if (level > 0 && level <= SlayerManager.XP_REQUIREMENTS.length) {
                            SlayerData.setSlayerXp(uuid, slayerType, SlayerManager.XP_REQUIREMENTS[level - 1]);
                        } else {
                            SlayerData.setSlayerXp(uuid, slayerType, 0);
                        }
                        admin.sendSystemMessage(Component.literal("✓ Set " + target.getName().getString() + "'s " +
                                slayerType.displayName + " level to " + level).withStyle(ChatFormatting.GREEN), false);
                        openSetLevelForPlayerGui(admin, target);
                    })
                    .build());
        }

        // Back button
        gui.setSlot(26, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.GRAY))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openSetLevelForPlayerGui(admin, target))
                .build());

        gui.open();
    }
    // ═══════════════════════════════════════════════════════════
// SET PLAYER XP GUI
// ═══════════════════════════════════════════════════════════

    private static void openSetXpGui(ServerPlayer admin) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, admin, false);
        gui.setTitle(Component.literal("Set Player Bounty XP"));

        // Background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Component.literal(""))
                    .build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.ENCHANTING_TABLE)
                .setName(Component.literal("Set Bounty XP").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Select a player below").withStyle(ChatFormatting.GRAY))
                .build());

        // Get online players
        List<ServerPlayer> players = new ArrayList<>(PoliticalServer.server.getPlayerList().getPlayers());
        int slot = 19;

        for (int i = 0; i < Math.min(players.size(), 14); i++) {
            ServerPlayer target = players.get(i);

            gui.setSlot(slot, new GuiElementBuilder(Items.PLAYER_HEAD)
                    .setProfile(target.getGameProfile())
                    .setName(Component.literal(target.getName().getString()).withStyle(ChatFormatting.YELLOW))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to set XP").withStyle(ChatFormatting.GREEN))
                    .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openSetXpForPlayerGui(admin, target))
                    .build());

            slot++;
            if (slot == 26) slot = 28;
        }

        // Back button
        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.GRAY))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openSlayerAdminGui(admin))
                .build());

        gui.open();
    }

    private static void openSetXpForPlayerGui(ServerPlayer admin, ServerPlayer target) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x5, admin, false);
        gui.setTitle(Component.literal("Set XP: " + target.getName().getString()));

        // Background
        for (int i = 0; i < 45; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Component.literal(""))
                    .build());
        }

        String uuid = target.getUUID().toString();

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
                    .setName(Component.literal(type.displayName).withStyle(type.color, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Current XP: " + formatNumber(currentXp)).withStyle(ChatFormatting.AQUA))
                    .addLoreLine(Component.literal("Current Level: " + currentLevel).withStyle(ChatFormatting.YELLOW))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Left-click: +1,000 XP").withStyle(ChatFormatting.GREEN))
                    .addLoreLine(Component.literal("Right-click: +100,000 XP").withStyle(ChatFormatting.GOLD))
                    .addLoreLine(Component.literal("Shift-click: +10,000,000 XP").withStyle(ChatFormatting.RED))
                    .setCallback((idx, clickType, containerInput, slotGui) -> {
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

                        admin.sendSystemMessage(Component.literal("✓ Added " + formatNumber(addXp) + " XP to " +
                                        target.getName().getString() + "'s " + type.displayName + " (now level " + newLevel + ")")
                                .withStyle(ChatFormatting.GREEN), false);
                        openSetXpForPlayerGui(admin, target);
                    })
                    .build());
        }

        // Back button
        gui.setSlot(40, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.GRAY))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openSetXpGui(admin))
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

    private static void openResetDataGui(ServerPlayer admin) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, admin, false);
        gui.setTitle(Component.literal("⚠ Reset Player Bounty Data ⚠"));

        // Background - red to indicate danger
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.RED_STAINED_GLASS_PANE)
                    .setName(Component.literal(""))
                    .build());
        }

        // Warning header
        gui.setSlot(4, new GuiElementBuilder(Items.TNT)
                .setName(Component.literal("⚠ DANGER ZONE ⚠").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("This will PERMANENTLY reset").withStyle(ChatFormatting.RED))
                .addLoreLine(Component.literal("a player's bounty progress!").withStyle(ChatFormatting.RED))
                .glow()
                .build());

        // Get online players
        List<ServerPlayer> players = new ArrayList<>(PoliticalServer.server.getPlayerList().getPlayers());
        int slot = 19;

        for (int i = 0; i < Math.min(players.size(), 14); i++) {
            ServerPlayer target = players.get(i);
            String uuid = target.getUUID().toString();
            int totalLevel = SlayerData.getTotalSlayerLevel(uuid);

            gui.setSlot(slot, new GuiElementBuilder(Items.PLAYER_HEAD)
                    .setProfile(target.getGameProfile())
                    .setName(Component.literal(target.getName().getString()).withStyle(ChatFormatting.YELLOW))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Total Level: " + totalLevel).withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to reset ALL data").withStyle(ChatFormatting.RED))
                    .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openConfirmResetGui(admin, target))
                    .build());

            slot++;
            if (slot == 26) slot = 28;
        }

        // Back button
        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.GRAY))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openSlayerAdminGui(admin))
                .build());

        gui.open();
    }

    private static void openConfirmResetGui(ServerPlayer admin, ServerPlayer target) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, admin, false);
        gui.setTitle(Component.literal("Confirm Reset: " + target.getName().getString()));

        // Background
        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Component.literal(""))
                    .build());
        }

        // Warning
        gui.setSlot(4, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("⚠ ARE YOU SURE? ⚠").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("This will reset ALL bounty data for:").withStyle(ChatFormatting.RED))
                .addLoreLine(Component.literal(target.getName().getString()).withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("• All levels → 0").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("• All XP → 0").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("• All boss kills → 0").withStyle(ChatFormatting.GRAY))
                .build());

        // Confirm button
        gui.setSlot(11, new GuiElementBuilder(Items.LIME_CONCRETE)
                .setName(Component.literal("✓ CONFIRM RESET").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to permanently reset").withStyle(ChatFormatting.YELLOW))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    String uuid = target.getUUID().toString();
                    for (SlayerManager.SlayerType t : SlayerManager.SlayerType.values()) {
                        SlayerData.setSlayerLevel(uuid, t, 0);
                        SlayerData.setSlayerXp(uuid, t, 0);
                        SlayerData.setBossesKilled(uuid, t, 0);
                    }
                    admin.sendSystemMessage(Component.literal("✓ Reset all bounty data for " + target.getName().getString())
                            .withStyle(ChatFormatting.GREEN), false);
                    target.sendSystemMessage(Component.literal("⚠ Your bounty data has been reset by an admin!")
                            .withStyle(ChatFormatting.RED), false);
                    openSlayerAdminGui(admin);
                })
                .build());

        // Cancel button
        gui.setSlot(15, new GuiElementBuilder(Items.RED_CONCRETE)
                .setName(Component.literal("✗ CANCEL").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Go back without resetting").withStyle(ChatFormatting.GRAY))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openResetDataGui(admin))
                .build());

        gui.open();
    }
    // ═══════════════════════════════════════════════════════════
// CANCEL PLAYER QUEST GUI
// ═══════════════════════════════════════════════════════════

    private static void openCancelQuestGui(ServerPlayer admin) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, admin, false);
        gui.setTitle(Component.literal("Cancel Player Quests"));

        // Background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Component.literal(""))
                    .build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("Cancel Active Quests").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Force cancel a player's").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("active bounty quest").withStyle(ChatFormatting.GRAY))
                .build());

        // Get online players with active quests
        List<ServerPlayer> players = new ArrayList<>(PoliticalServer.server.getPlayerList().getPlayers());
        int slot = 19;

        for (int i = 0; i < Math.min(players.size(), 14); i++) {
            ServerPlayer target = players.get(i);
            SlayerManager.ActiveQuest quest = SlayerManager.getActiveQuest(target);

            GuiElementBuilder element = new GuiElementBuilder(Items.PLAYER_HEAD)
                    .setProfile(target.getGameProfile())
                    .setName(Component.literal(target.getName().getString()).withStyle(ChatFormatting.YELLOW));

            if (quest != null) {
                element.addLoreLine(Component.literal(""))
                        .addLoreLine(Component.literal("Active Quest:").withStyle(ChatFormatting.GREEN))
                        .addLoreLine(Component.literal("• Type: " + quest.slayerType.displayName).withStyle(quest.slayerType.color))
                        .addLoreLine(Component.literal("• Tier: " + quest.tier).withStyle(ChatFormatting.WHITE))
                        .addLoreLine(Component.literal("• Progress: " + quest.killCount + "/" + quest.getKillsRequired()).withStyle(ChatFormatting.GRAY))
                        .addLoreLine(Component.literal(""))
                        .addLoreLine(Component.literal("Click to cancel quest").withStyle(ChatFormatting.RED))
                        .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                            SlayerManager.cancelQuest(target);
                            admin.sendSystemMessage(Component.literal("✓ Cancelled " + target.getName().getString() + "'s quest")
                                    .withStyle(ChatFormatting.GREEN), false);
                            target.sendSystemMessage(Component.literal("⚠ Your bounty quest was cancelled by an admin!")
                                    .withStyle(ChatFormatting.RED), false);
                            openCancelQuestGui(admin);
                        });
            } else {
                element.addLoreLine(Component.literal(""))
                        .addLoreLine(Component.literal("No active quest").withStyle(ChatFormatting.GRAY));
            }

            gui.setSlot(slot, element.build());

            slot++;
            if (slot == 26) slot = 28;
        }

        // Back button
        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.GRAY))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openSlayerAdminGui(admin))
                .build());

        gui.open();
    }
    // ═══════════════════════════════════════════════════════════
// VIEW PLAYER STATS GUI
// ═══════════════════════════════════════════════════════════

    private static void openViewStatsGui(ServerPlayer admin) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, admin, false);
        gui.setTitle(Component.literal("View Player Bounty Stats"));

        // Background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Component.literal(""))
                    .build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.BOOK)
                .setName(Component.literal("Player Statistics").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("View detailed bounty stats").withStyle(ChatFormatting.GRAY))
                .build());

        // Get online players
        List<ServerPlayer> players = new ArrayList<>(PoliticalServer.server.getPlayerList().getPlayers());
        int slot = 19;

        for (int i = 0; i < Math.min(players.size(), 14); i++) {
            ServerPlayer target = players.get(i);
            String uuid = target.getUUID().toString();
            int totalLevel = SlayerData.getTotalSlayerLevel(uuid);
            int totalBosses = SlayerData.getTotalBossesKilled(uuid);

            gui.setSlot(slot, new GuiElementBuilder(Items.PLAYER_HEAD)
                    .setProfile(target.getGameProfile())
                    .setName(Component.literal(target.getName().getString()).withStyle(ChatFormatting.YELLOW))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Total Level: " + totalLevel).withStyle(ChatFormatting.GREEN))
                    .addLoreLine(Component.literal("Total Bosses: " + totalBosses).withStyle(ChatFormatting.RED))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click for details").withStyle(ChatFormatting.GRAY))
                    .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openPlayerStatsDetailGui(admin, target))
                    .build());

            slot++;
            if (slot == 26) slot = 28;
        }

        // Back button
        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.GRAY))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openSlayerAdminGui(admin))
                .build());

        gui.open();
    }

    private static void openPlayerStatsDetailGui(ServerPlayer admin, ServerPlayer target) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x4, admin, false);
        gui.setTitle(Component.literal("Stats: " + target.getName().getString()));

        // Background
        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Component.literal(""))
                    .build());
        }

        String uuid = target.getUUID().toString();

        // Header with summary
        int totalLevel = SlayerData.getTotalSlayerLevel(uuid);
        int totalBosses = SlayerData.getTotalBossesKilled(uuid);

        gui.setSlot(4, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setProfile(target.getGameProfile())
                .setName(Component.literal(target.getName().getString()).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Total Level: " + totalLevel + " / " + (SlayerManager.MAX_LEVEL * 6)).withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal("Total Bosses Killed: " + totalBosses).withStyle(ChatFormatting.RED))
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
                    .setName(Component.literal(type.displayName).withStyle(type.color, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Level: " + level + " / " + SlayerManager.MAX_LEVEL).withStyle(ChatFormatting.YELLOW))
                    .addLoreLine(Component.literal("XP: " + formatNumber(xp)).withStyle(ChatFormatting.AQUA))
                    .addLoreLine(Component.literal("Bosses Killed: " + bosses).withStyle(ChatFormatting.RED))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Progress: " + progressText).withStyle(
                            level >= SlayerManager.MAX_LEVEL ? ChatFormatting.GOLD : ChatFormatting.GREEN))
                    .build());
        }

        // Active quest info
        SlayerManager.ActiveQuest quest = SlayerManager.getActiveQuest(target);
        if (quest != null) {
            gui.setSlot(31, new GuiElementBuilder(Items.COMPASS)
                    .setName(Component.literal("Active Quest").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Type: " + quest.slayerType.displayName).withStyle(quest.slayerType.color))
                    .addLoreLine(Component.literal("Tier: " + quest.tier).withStyle(ChatFormatting.WHITE))
                    .addLoreLine(Component.literal("Progress: " + quest.killCount + "/" + quest.getKillsRequired()).withStyle(ChatFormatting.GREEN))
                    .addLoreLine(Component.literal("Boss Spawned: " + (quest.bossSpawned ? "Yes" : "No")).withStyle(
                            quest.bossSpawned ? ChatFormatting.RED : ChatFormatting.GRAY))
                    .glow()
                    .build());
        } else {
            gui.setSlot(31, new GuiElementBuilder(Items.GRAY_DYE)
                    .setName(Component.literal("No Active Quest").withStyle(ChatFormatting.GRAY))
                    .build());
        }

        // Back button
        gui.setSlot(35, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.GRAY))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openViewStatsGui(admin))
                .build());

        gui.open();
    }
    // ============================================================
// SLAYER CORE CREATION
// ============================================================

    public static ItemStack createSlayerCore(SlayerManager.SlayerType type) {
        ItemStack core = new ItemStack(Items.NETHER_STAR);

        core.set(DataComponents.CUSTOM_NAME,
                Component.literal(type.displayName + " Core")
                        .withStyle(type.color, ChatFormatting.BOLD));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("BOUNTY MATERIAL").withStyle(ChatFormatting.DARK_PURPLE));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Dropped by: " + type.bossName).withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Used to craft " + type.displayName).withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("bounty weapons and armor.").withStyle(ChatFormatting.YELLOW));

        core.set(DataComponents.LORE, new ItemLore(lore));
        core.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);

        return core;
    }
    private static void openT2ArmorGiveGui(ServerPlayer admin) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, admin, false);
        gui.setTitle(Component.literal("Give T2 Armor"));

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
                .setName(Component.literal("← Back").withStyle(ChatFormatting.GRAY))
                .setCallback((idx, clickType, containerInput, slotGui) -> {
                    openSlayerAdminGui(admin);
                })
                .build());

        gui.open();
    }

    private static GuiElement createGiveButton(ItemStack item, ServerPlayer admin) {
        return new GuiElementBuilder(item.getItem())
                .setName(item.get(DataComponents.CUSTOM_NAME))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to receive").withStyle(ChatFormatting.GREEN))
                .setCallback((idx, clickType, containerInput, slotGui) -> {
                    admin.addItem(item.copy());
                    admin.sendSystemMessage(Component.literal("§a✓ Given!"), false);
                })
                .build();
    }
    // ═══════════════════════════════════════════════════════════
// SPAWN UPGRADED MOB SUBMENU
// ═══════════════════════════════════════════════════════════

    private static void openPrisonGui(ServerPlayer admin) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, admin, false);
        gui.setTitle(Component.literal("Prison Controls"));

        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Component.literal(""))
                    .build());
        }


        gui.setSlot(4, new GuiElementBuilder(Items.IRON_SWORD)
                .setName(Component.literal("⚔ Bounty Administration").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .glow()
                .build());

        List<ServerPlayer> players = new ArrayList<>(PoliticalServer.server.getPlayerList().getPlayers());
        int slot = 10;
        int maxSlot = 44;

        for (ServerPlayer target : players) {
            if (slot > maxSlot) break;
            if (slot % 9 == 0) slot++;
            if (slot % 9 == 8) slot += 2;

            final ServerPlayer finalTarget = target;
            boolean isPrisoner = PrisonManager.isPrisoner(target.getUUID().toString());

            gui.setSlot(slot, new GuiElementBuilder(isPrisoner ? Items.IRON_BARS : Items.PLAYER_HEAD)
                    .setProfile(target.getGameProfile())
                    .setName(Component.literal(target.getName().getString()).withStyle(isPrisoner ? ChatFormatting.RED : ChatFormatting.WHITE))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Status: " + (isPrisoner ? "IMPRISONED" : "Free")).withStyle(isPrisoner ? ChatFormatting.RED : ChatFormatting.GREEN))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal(isPrisoner ? "▶ Click to release" : "▶ Click to imprison").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> {
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
                .setName(Component.literal("← Back").withStyle(ChatFormatting.GRAY))
                .setCallback((int index, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openPage(admin, AdminPage.PLAYERS))
                .build());

        gui.open();
    }

    private static void openImprisonTimeSelector(ServerPlayer admin, ServerPlayer target) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, admin, false);
        gui.setTitle(Component.literal("Imprison " + target.getName().getString()));

        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Component.literal(""))
                    .build());
        }

        gui.setSlot(4, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setProfile(target.getGameProfile())
                .setName(Component.literal("Imprison " + target.getName().getString()).withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Select duration").withStyle(ChatFormatting.GRAY))
                .build());

        int[] times = {1, 5, 10, 30, 60, 120};
        int[] slots = {10, 11, 12, 13, 14, 15};
        String[] labels = {"1 min", "5 min", "10 min", "30 min", "1 hour", "2 hours"};

        for (int i = 0; i < times.length; i++) {
            final int time = times[i];
            gui.setSlot(slots[i], new GuiElementBuilder(Items.CLOCK)
                    .setName(Component.literal(labels[i]).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("▶ Click to imprison").withStyle(ChatFormatting.RED))
                    .setCallback((int index, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                        double x = admin.getX();
                        double y = admin.getY();
                        double z = admin.getZ();
                        PrisonManager.imprison(target, time, x, y, z);
                        admin.sendSystemMessage(Component.literal("✓ Imprisoned for " + time + " min!").withStyle(ChatFormatting.RED));
                        target.sendSystemMessage(Component.literal("You have been imprisoned for " + time + " minutes!").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
                        for (ServerPlayer p : PoliticalServer.server.getPlayerList().getPlayers()) {
                            p.sendSystemMessage(Component.literal("⚖ " + target.getName().getString() + " imprisoned for " + time + " min!").withStyle(ChatFormatting.RED));
                        }
                        openPage(admin, AdminPage.PLAYERS);
                    })
                    .build());
        }

        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.GRAY))
                .setCallback((int index, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    openPrisonGui(admin);
                })
                .build());

        gui.open();
    }

    public static void openCustomItemsAdminGui(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("🎁 Custom Items Admin"));

        // Background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Component.literal(""))
                    .build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.COMMAND_BLOCK)
                .setName(Component.literal("🎁 Item Administration").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Give custom items to yourself").withStyle(ChatFormatting.GRAY))
                .build());

        // Bounty Swords
        gui.setSlot(19, new GuiElementBuilder(Items.IRON_SWORD)
                .setName(Component.literal("⚔ Bounty Swords").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Basic bounty swords").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Click to view!").withStyle(ChatFormatting.GREEN))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openBasicSwordsMenu(player))
                .build());

        // Upgraded Swords
        gui.setSlot(21, new GuiElementBuilder(Items.DIAMOND_SWORD)
                .setName(Component.literal("⚔ Upgraded Swords").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Enhanced bounty swords").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Click to view!").withStyle(ChatFormatting.GREEN))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openUpgradedSwordsMenu(player))
                .build());

        // Legendary Weapons
        gui.setSlot(23, new GuiElementBuilder(Items.NETHERITE_SWORD)
                .setName(Component.literal("⚔ Legendary Weapons").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Ender Sword, Abyssal Blade").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Click to view!").withStyle(ChatFormatting.GREEN))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openLegendaryWeaponsMenu(player))
                .build());

        // HPEBM Weapons
        gui.setSlot(25, new GuiElementBuilder(Items.END_ROD)
                .setName(Component.literal("⚡ HPEBM Weapons").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Energy beam weapons Mk1-Mk5").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Click to view!").withStyle(ChatFormatting.GREEN))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openHPEBMMenu(player))
                .build());

        // Cores & Chunks
        gui.setSlot(29, new GuiElementBuilder(Items.ENDER_PEARL)
                .setName(Component.literal("✦ Cores & Chunks").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Crafting materials").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Click to view!").withStyle(ChatFormatting.GREEN))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openMaterialsMenu(player))
                .build());

        // Special Items
        gui.setSlot(31, new GuiElementBuilder(Items.WOODEN_AXE)
                .setName(Component.literal("✦ Special Items").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Gavel, Credits, Coins").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Click to view!").withStyle(ChatFormatting.GREEN))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openSpecialItemsMenu(player))
                .build());

        // Player Management
        gui.setSlot(33, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Component.literal("👤 Player Stats").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Set levels, reset progress").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Click to view!").withStyle(ChatFormatting.GREEN))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openPlayerManagementMenu(player))
                .build());

        gui.setSlot(37, new GuiElementBuilder(Items.DIAMOND_CHESTPLATE)
                .setName(Component.literal("🛡 Bounty Armor").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Give bounty armor sets").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("T1 and T2 versions").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to view!").withStyle(ChatFormatting.GREEN))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openArmorAdminMenu(player))
                .build());

        // Custom Enchantments
        gui.setSlot(39, new GuiElementBuilder(Items.ENCHANTED_BOOK)
                .setName(Component.literal("✦ Custom Enchantments").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Apply custom enchantments").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("to held items").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to view!").withStyle(ChatFormatting.GREEN))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openCustomEnchantmentsMenu(player))
                .build());

        // Back button
        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back to Admin Menu").withStyle(ChatFormatting.YELLOW))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> open(player))
                .build());

        gui.open();

        gui.setSlot(43, new GuiElementBuilder(Items.WITHER_SKELETON_SKULL)
                .setName(Component.literal("☠ Force Spawn Boss").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Spawn a bounty boss at your location").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to select boss type").withStyle(ChatFormatting.YELLOW))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openSpawnBossMenu(player))
                .build());

    }

// ============================================================
// LEGENDARY WEAPONS MENU
// ============================================================
private static void openSpawnBossMenu(ServerPlayer player) {
    SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x4, player, false);
    gui.setTitle(Component.literal("☠ Spawn Bounty Boss"));

    // Background
    for (int i = 0; i < 36; i++) {
        gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                .setName(Component.literal(""))
                .build());
    }

    // Boss type buttons
    int slot = 10;
    for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
        final SlayerManager.SlayerType finalType = type;

        gui.setSlot(slot, new GuiElementBuilder(type.icon)
                .setName(Component.literal("☠ " + type.bossName).withStyle(type.color, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to select tier").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, clickType, containerInput, slotGui) -> openSpawnBossTierMenu(player, finalType))
                .build());

        slot++;
        if (slot == 17) slot = 19; // Next row
    }

    // Back button
    gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
            .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
            .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openSlayerAdminGui(player))
            .build());

    gui.open();
}
    private static void openSpawnBossTierMenu(ServerPlayer player, SlayerManager.SlayerType type) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal("☠ " + type.bossName + " - Select Tier"));

        // Background
        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Component.literal(""))
                    .build());
        }

        // Tier buttons
        int[] tierSlots = {11, 12, 13, 14, 15};
        for (int tier = 1; tier <= 5; tier++) {
            final int finalTier = tier;
            SlayerManager.TierConfig config = SlayerManager.getTierConfig(tier);

            gui.setSlot(tierSlots[tier - 1], new GuiElementBuilder(Items.PAPER)
                    .setName(Component.literal("Tier " + tier).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("HP: " + (int)config.getActualHp(type)).withStyle(ChatFormatting.RED))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to spawn!").withStyle(ChatFormatting.GREEN))
                    .setCallback((idx, clickType, containerInput, slotGui) -> {
                        SlayerManager.adminSpawnBoss(player, type, finalTier);
                        player.sendSystemMessage(Component.literal("✓ Spawned " + type.bossName + " T" + finalTier + "!")
                                .withStyle(ChatFormatting.GREEN));
                        player.closeContainer();
                    })
                    .build());
        }

        // Back button
        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, clickType, containerInput, slotGui) -> openSpawnBossMenu(player))
                .build());

        gui.open();
    }

    private static void openSpawnUpgradedMobMenu(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x4, player, false);
        gui.setTitle(Component.literal("⬆ Spawn Upgraded Mob (limited to some hostiles)"));

        // Background
        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.LIME_STAINED_GLASS_PANE)
                    .setName(Component.literal(""))
                    .build());
        }

        // Mob types with tiers
        gui.setSlot(10, createUpgradedMobButton(player, "Zombie", Items.ZOMBIE_HEAD,
                net.minecraft.world.entity.EntityType.ZOMBIE));
        gui.setSlot(11, createUpgradedMobButton(player, "Skeleton", Items.SKELETON_SKULL,
                net.minecraft.world.entity.EntityType.SKELETON));
        gui.setSlot(12, createUpgradedMobButton(player, "Spider", Items.SPIDER_EYE,
                net.minecraft.world.entity.EntityType.SPIDER));
        gui.setSlot(13, createUpgradedMobButton(player, "Creeper", Items.CREEPER_HEAD,
                net.minecraft.world.entity.EntityType.CREEPER));
        gui.setSlot(14, createUpgradedMobButton(player, "Enderman", Items.ENDER_PEARL,
                net.minecraft.world.entity.EntityType.ENDERMAN));
        gui.setSlot(15, createUpgradedMobButton(player, "Slime", Items.SLIME_BALL,
                net.minecraft.world.entity.EntityType.SLIME));


        // Back button
        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openSlayerAdminGui(player))
                .build());

        gui.open();
    }

    private static GuiElementBuilder createUpgradedMobButton(ServerPlayer player, String name,
                                                             net.minecraft.world.item.Item icon, net.minecraft.world.entity.EntityType<?> entityType) {
        return new GuiElementBuilder(icon)
                .setName(Component.literal("⬆ " + name).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to spawn upgraded " + name).withStyle(ChatFormatting.GRAY))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    HealthScalingManager.spawnUpgradedMob(player, entityType);
                    player.sendSystemMessage(Component.literal("✓ Spawned upgraded " + name + "!")
                            .withStyle(ChatFormatting.GREEN));
                });
    }

    private static void openLegendaryWeaponsMenu(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x2, player, false);
        gui.setTitle(Component.literal("⚔ Legendary Weapons"));

        for (int i = 0; i < 18; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.PURPLE_STAINED_GLASS_PANE)
                    .setName(Component.literal("")).build());
        }

        // Ender Sword
        gui.setSlot(3, new GuiElementBuilder(Items.NETHERITE_SWORD)
                .setName(Component.literal("⚔ Ender Sword")
                        .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§d§lLEGENDARY"))
                .addLoreLine(Component.literal("Ability: Void Strike")
                        .withStyle(ChatFormatting.GOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to receive!")
                        .withStyle(ChatFormatting.GREEN))
                .glow()
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    ItemStack sword = SlayerItems.createEnderSword();
                    if (!player.addItem(sword)) {
                        player.drop(sword, false);
                    }
                    player.sendSystemMessage(Component.literal("✔ Received Ender Sword!")
                            .withStyle(ChatFormatting.GREEN), false);
                })
                .build());

        // Abyssal Blade
        gui.setSlot(5, new GuiElementBuilder(Items.NETHERITE_SWORD)
                .setName(Component.literal("⚔ Abyssal Blade")
                        .withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§5§lMYTHIC"))
                .addLoreLine(Component.literal("Ability: Sonic Devastation")
                        .withStyle(ChatFormatting.GOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to receive!")
                        .withStyle(ChatFormatting.GREEN))
                .glow()
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    ItemStack sword = SlayerItems.createAbyssalBlade();
                    if (!player.addItem(sword)) {
                        player.drop(sword, false);
                    }
                    player.sendSystemMessage(Component.literal("✔ Received Abyssal Blade!")
                            .withStyle(ChatFormatting.GREEN), false);
                })
                .build());

        gui.setSlot(13, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openCustomItemsAdminGui(player))
                .build());

        gui.open();
    }

// ============================================================
// HPEBM WEAPONS MENU
// ============================================================
private static void openArmorAdminMenu(ServerPlayer player) {
    SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x4, player, false);
    gui.setTitle(Component.literal("🛡 Bounty Armor Admin"));

    for (int i = 0; i < 36; i++) {
        gui.setSlot(i, new GuiElementBuilder(Items.CYAN_STAINED_GLASS_PANE)
                .setName(Component.literal("")).build());
    }

    // T1 Armor Sets
    gui.setSlot(1, new GuiElementBuilder(Items.IRON_CHESTPLATE)
            .setName(Component.literal("T1 Armor Sets").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD))
            .addLoreLine(Component.literal("Basic bounty armor").withStyle(ChatFormatting.GRAY))
            .build());

    int slot = 10;
    for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
        final SlayerManager.SlayerType finalType = type;
        gui.setSlot(slot, new GuiElementBuilder(Items.IRON_CHESTPLATE)
                .setName(Component.literal(type.displayName + " Armor Set").withStyle(type.color, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("T1 - Basic Set").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Click to receive full set!").withStyle(ChatFormatting.GREEN))
                .setCallback((idx, clickType, containerInput, slotGui) -> {
                    SlayerItems.giveFullArmorSet(player, finalType, 1);
                })
                .build());
        slot++;
    }

    // T2 Armor Sets
    gui.setSlot(7, new GuiElementBuilder(Items.DIAMOND_CHESTPLATE)
            .setName(Component.literal("T2 Armor Sets").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
            .addLoreLine(Component.literal("Upgraded bounty armor").withStyle(ChatFormatting.GRAY))
            .glow()
            .build());

    slot = 19;
    for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
        final SlayerManager.SlayerType finalType = type;
        gui.setSlot(slot, new GuiElementBuilder(Items.DIAMOND_CHESTPLATE)
                .setName(Component.literal(type.displayName + " Armor Set II").withStyle(type.color, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("T2 - Upgraded Set").withStyle(ChatFormatting.AQUA))
                .addLoreLine(Component.literal("Click to receive full set!").withStyle(ChatFormatting.GREEN))
                .glow()
                .setCallback((idx, clickType, containerInput, slotGui) -> {
                    SlayerItems.giveFullArmorSet(player, finalType, 2);
                })
                .build());
        slot++;
    }

    // Back button
    gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
            .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
            .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openCustomItemsAdminGui(player))
            .build());

    gui.open();
}


    private static void openHPEBMMenu(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x2, player, false);
        gui.setTitle(Component.literal("⚡ HPEBM Weapons"));

        for (int i = 0; i < 18; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.CYAN_STAINED_GLASS_PANE)
                    .setName(Component.literal("")).build());
        }

        // Mk1
        gui.setSlot(2, new GuiElementBuilder(Items.END_ROD)
                .setName(Component.literal("HPEBM Mk1")
                        .withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Basic energy weapon").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to receive!").withStyle(ChatFormatting.GREEN))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    giveHPEBM(player, 1);
                })
                .build());

        // Mk2
        gui.setSlot(4, new GuiElementBuilder(Items.END_ROD)
                .setName(Component.literal("HPEBM Mk2")
                        .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Enhanced output").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to receive!").withStyle(ChatFormatting.GREEN))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    giveHPEBM(player, 2);
                })
                .build());

        // Mk3
        gui.setSlot(6, new GuiElementBuilder(Items.END_ROD)
                .setName(Component.literal("HPEBM Mk3")
                        .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Military-grade").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to receive!").withStyle(ChatFormatting.GREEN))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    giveHPEBM(player, 3);
                })
                .build());

        // Mk4
        gui.setSlot(10, new GuiElementBuilder(Items.END_ROD)
                .setName(Component.literal("HPEBM Mk4")
                        .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Experimental prototype").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to receive!").withStyle(ChatFormatting.GREEN))
                .glow()
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    giveHPEBM(player, 4);
                })
                .build());

        // Mk5
        gui.setSlot(12, new GuiElementBuilder(Items.END_ROD)
                .setName(Component.literal("HPEBM Mk5")
                        .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§d§lLEGENDARY"))
                .addLoreLine(Component.literal("Ultimate energy weapon").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to receive!").withStyle(ChatFormatting.GREEN))
                .glow()
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    giveHPEBM(player, 5);
                })
                .build());

        gui.setSlot(16, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openCustomItemsAdminGui(player))
                .build());

        gui.open();
    }

    private static void giveHPEBM(ServerPlayer player, int mk) {
        ItemStack weapon = CustomItemHandler.createHPEBM(mk);
        if (!player.addItem(weapon)) {
            player.drop(weapon, false);
        }
        player.sendSystemMessage(Component.literal("✔ Received HPEBM Mk" + mk + "!")
                .withStyle(ChatFormatting.GREEN), false);
    }

// ============================================================
// MATERIALS MENU (Cores & Chunks)
// ============================================================

    private static void openMaterialsMenu(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x4, player, false);
        gui.setTitle(Component.literal("✦ Cores & Chunks"));

        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.ORANGE_STAINED_GLASS_PANE)
                    .setName(Component.literal("")).build());
        }

        // Cores row
        gui.setSlot(1, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Component.literal("§d§lCORES")
                        .withStyle(ChatFormatting.LIGHT_PURPLE))
                .addLoreLine(Component.literal("Rare boss drops").withStyle(ChatFormatting.GRAY))
                .build());

        int coreSlot = 10;
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            final SlayerManager.SlayerType finalType = type;
            gui.setSlot(coreSlot, new GuiElementBuilder(type.icon)
                    .setName(Component.literal(type.displayName + " Core")
                            .withStyle(type.color, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Rare drop from " + type.bossName)
                            .withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to receive!").withStyle(ChatFormatting.GREEN))
                    .glow()
                    .setCallback((idx, clickType, containerInput, slotGui) -> {
                        SlayerItems.giveCore(player, finalType);
                    })
                    .build());
            coreSlot++;
        }

        // Chunks row
        gui.setSlot(7, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Component.literal("§6§lCHUNKS")
                        .withStyle(ChatFormatting.GOLD))
                .addLoreLine(Component.literal("Boss crafting materials").withStyle(ChatFormatting.GRAY))
                .build());

        int chunkSlot = 19;
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            final SlayerManager.SlayerType finalType = type;
            String chunkName = SlayerItems.getChunkName(type);
            gui.setSlot(chunkSlot, new GuiElementBuilder(type.icon)
                    .setName(Component.literal(chunkName)
                            .withStyle(type.color, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Drop from " + type.bossName)
                            .withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to receive!").withStyle(ChatFormatting.GREEN))
                    .setCallback((idx, clickType, containerInput, slotGui) -> {
                        ItemStack chunk = SlayerItems.createChunk(finalType);
                        if (!player.addItem(chunk)) {
                            player.drop(chunk, false);
                        }
                        player.sendSystemMessage(Component.literal("✔ Received " + chunkName + "!")
                                .withStyle(ChatFormatting.GREEN), false);
                    })
                    .build());
            chunkSlot++;
        }

        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openCustomItemsAdminGui(player))
                .build());

        gui.open();
    }

// ============================================================
// SPECIAL ITEMS MENU
// ============================================================

    private static void openSpecialItemsMenu(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x2, player, false);
        gui.setTitle(Component.literal("✦ Special Items"));

        for (int i = 0; i < 18; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.RED_STAINED_GLASS_PANE)
                    .setName(Component.literal("")).build());
        }

        // Gavel
        gui.setSlot(3, new GuiElementBuilder(Items.WOODEN_AXE)
                .setName(Component.literal("⚖ The Gavel")
                        .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Chair holder's tool").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to receive!").withStyle(ChatFormatting.GREEN))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    ItemStack gavel = CustomItemHandler.createGavel();
                    if (!player.addItem(gavel)) {
                        player.drop(gavel, false);
                    }
                    player.sendSystemMessage(Component.literal("✔ Received The Gavel!")
                            .withStyle(ChatFormatting.GREEN), false);
                })
                .build());

        // Give Credits
        gui.setSlot(5, new GuiElementBuilder(Items.EMERALD)
                .setName(Component.literal("💎 Give Credits")
                        .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Give yourself credits").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Left-click: +100").withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal("Right-click: +1000").withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal("Shift-click: +10000").withStyle(ChatFormatting.GOLD))
                .setCallback((idx, clickType, containerInput, slotGui) -> {
                    int amount = 100;
                    if (clickType.isRight) amount = 1000;
                    if (clickType.shift) amount = 10000;
                    CreditItem.giveCredits(player, amount);
                    player.sendSystemMessage(Component.literal("✔ Received " + amount + " credits!")
                            .withStyle(ChatFormatting.GREEN), false);
                })
                .build());

        // Give Coins
        gui.setSlot(7, new GuiElementBuilder(Items.SUNFLOWER)
                .setName(Component.literal("🪙 Give Coins")
                        .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Give yourself coins").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Left-click: +100").withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal("Right-click: +1000").withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal("Shift-click: +10000").withStyle(ChatFormatting.GOLD))
                .setCallback((idx, clickType, containerInput, slotGui) -> {
                    int amount = 100;
                    if (clickType.isRight) amount = 1000;
                    if (clickType.shift) amount = 10000;
                    CoinManager.giveCoins(player, amount);
                    player.sendSystemMessage(Component.literal("✔ Received " + amount + " coins!")
                            .withStyle(ChatFormatting.GREEN), false);
                })
                .build());

        gui.setSlot(13, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openCustomItemsAdminGui(player))
                .build());

        gui.open();
    }

// ============================================================
// PLAYER MANAGEMENT MENU
// ============================================================

    private static void openPlayerManagementMenu(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal("👤 Player Management"));

        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.LIME_STAINED_GLASS_PANE)
                    .setName(Component.literal("")).build());
        }

        // Set Bounty Level
        gui.setSlot(10, new GuiElementBuilder(Items.EXPERIENCE_BOTTLE)
                .setName(Component.literal("Set Bounty Level")
                        .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Set your bounty levels").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to open!").withStyle(ChatFormatting.GREEN))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openSetLevelGui(player))
                .build());

        // View Stats
        gui.setSlot(13, new GuiElementBuilder(Items.BOOK)
                .setName(Component.literal("View Stats")
                        .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("View your bounty stats").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to view!").withStyle(ChatFormatting.GREEN))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openViewStatsGui(player))
                .build());

        // Reset Progress
        gui.setSlot(16, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("Reset Progress")
                        .withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("⚠ Reset all bounty progress").withStyle(ChatFormatting.DARK_RED))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to confirm!").withStyle(ChatFormatting.RED))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openResetConfirmGui(player))
                .build());

        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openCustomItemsAdminGui(player))
                .build());

        gui.open();
    }

// ============================================================
// RESET CONFIRMATION GUI
// ============================================================

    private static void openResetConfirmGui(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x1, player, false);
        gui.setTitle(Component.literal("⚠ Confirm Reset?"));

        for (int i = 0; i < 9; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.RED_STAINED_GLASS_PANE)
                    .setName(Component.literal("")).build());
        }

        // Confirm button
        gui.setSlot(2, new GuiElementBuilder(Items.LIME_CONCRETE)
                .setName(Component.literal("✔ Confirm Reset")
                        .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("This will reset ALL your").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("bounty progress permanently!").withStyle(ChatFormatting.RED))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    resetPlayerProgress(player);
                    player.sendSystemMessage(Component.literal("✔ Your bounty progress has been reset.")
                            .withStyle(ChatFormatting.GREEN), false);
                    DataManager.save(PoliticalServer.server);
                    openCustomItemsAdminGui(player);
                })
                .build());

        // Cancel button
        gui.setSlot(6, new GuiElementBuilder(Items.RED_CONCRETE)
                .setName(Component.literal("✖ Cancel")
                        .withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openPlayerManagementMenu(player))
                .build());

        gui.open();
    }

    private static void resetPlayerProgress(ServerPlayer player) {
        String uuid = player.getUUID().toString();
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

    public static void openCustomItemsMenu(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("§6§lAdmin: Custom Items"));

        // Fill background
        ItemStack filler = new ItemStack(Items.BLACK_STAINED_GLASS_PANE);
        filler.set(DataComponents.CUSTOM_NAME, Component.literal(" "));
        for (int i = 0; i < 54; i++) {
            int finalI = i;
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Component.literal(" "))
                    .setCallback((int index, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {})
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
                .setName(Component.literal("§c§lBack"))
                .setCallback((int index, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    openMainPage(player);
                })
                .build());

        // === GIVE ALL BUTTON ===
        gui.setSlot(53, new GuiElementBuilder(Items.CHEST)
                .setName(Component.literal("§a§lGive All Items"))
                .setLore(List.of(Component.literal("§7Click to receive ALL items")))
                .setCallback((int index, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    giveAllCustomItems(player);
                })
                .build());

        gui.open();
    }

    // Helper method to create give item buttons
    private static GuiElement createGiveItemButton(ServerPlayer player, ItemStack item, String name, String lore) {
        if (item == null || item.isEmpty()) {
            return new GuiElementBuilder(Items.BARRIER)
                    .setName(Component.literal("§c§lMissing Item"))
                    .setLore(List.of(Component.literal("§7Item not found")))
                    .build();
        }

        return new GuiElementBuilder(item.getItem())
                .setName(Component.literal(name))
                .setLore(List.of(Component.literal(lore), Component.literal("§eClick to receive")))
                .setCallback((int index, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    ItemStack clone = item.copy();
                    if (player.addItem(clone)) {
                        player.sendSystemMessage(Component.literal("§a✓ Given: " + name), false);
                        player.playSound(net.minecraft.sounds.SoundEvents.ITEM_PICKUP, 0.5f, 1.0f);
                    } else {
                        player.sendSystemMessage(Component.literal("§c✖ Inventory full!"), false);
                    }
                })
                .build();
    }

    // Give all custom items
    private static void giveAllCustomItems(ServerPlayer player) {
        int given = 0;

        // All swords
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            ItemStack sword = SlayerItems.createSlayerSword(type);
            if (sword != null && player.addItem(sword)) given++;

            ItemStack chunk = SlayerItems.createChunk(type);
            if (chunk != null) {
                chunk.setCount(16);
                if (player.addItem(chunk)) given++;
            }

            ItemStack core = SlayerItems.createCore(type);
            if (core != null && player.addItem(core)) given++;
        }

        // HPEBM weapons
        for (int tier = 1; tier <= 7; tier++) {
            ItemStack beam = CustomItemHandler.createHPEBM(tier);
            if (beam != null && player.addItem(beam)) given++;
        }

        player.sendSystemMessage(Component.literal("§a✓ Given " + given + " items!"), false);
    }

    // ============================================================
    // CUSTOM ENCHANTMENTS ADMIN MENU
    // ============================================================

    public static void openCustomEnchantmentsMenu(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("✦ Custom Enchantments").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));

        // Background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Component.literal(""))
                    .build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.ENCHANTED_BOOK)
                .setName(Component.literal("✦ Custom Enchantments").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Hold an item and click to apply").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Left-click: Level 1  |  Right-click: Max Level").withStyle(ChatFormatting.YELLOW))
                .build());

        // ── OFFENSIVE ENCHANTMENTS ───────────────────────────────
        gui.setSlot(10, new GuiElementBuilder(Items.IRON_SWORD)
                .setName(Component.literal("⚔ Slayer Sharpness").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Max Level: V").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("+15% damage per level to all mobs").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Left: Lvl 1  |  Right: Lvl 5").withStyle(ChatFormatting.YELLOW))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> applyEnchant(player, CustomEnchantmentManager.SLAYER_SHARPNESS,
                        type == ClickType.MOUSE_RIGHT ? 5 : 1, "Slayer Sharpness"))
                .build());

        gui.setSlot(11, new GuiElementBuilder(Items.WITHER_SKELETON_SKULL)
                .setName(Component.literal("☠ Boss Hunter").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Max Level: III").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("+25% damage per level vs slayer bosses").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Left: Lvl 1  |  Right: Lvl 3").withStyle(ChatFormatting.YELLOW))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> applyEnchant(player, CustomEnchantmentManager.BOSS_HUNTER,
                        type == ClickType.MOUSE_RIGHT ? 3 : 1, "Boss Hunter"))
                .build());

        gui.setSlot(12, new GuiElementBuilder(Items.ZOMBIE_HEAD)
                .setName(Component.literal("☠ Mob Bane").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Max Level: III").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("+20% damage per level vs matching mob type").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Left: Lvl 1  |  Right: Lvl 3").withStyle(ChatFormatting.YELLOW))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> applyEnchant(player, CustomEnchantmentManager.MOB_BANE,
                        type == ClickType.MOUSE_RIGHT ? 3 : 1, "Mob Bane"))
                .build());

        gui.setSlot(13, new GuiElementBuilder(Items.SPIDER_EYE)
                .setName(Component.literal("🩸 Vampiric").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Max Level: I").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Heals on kill").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Left/Right: Lvl 1").withStyle(ChatFormatting.YELLOW))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> applyEnchant(player, CustomEnchantmentManager.VAMPIRIC,
                        1, "Vampiric"))
                .build());

        gui.setSlot(14, new GuiElementBuilder(Items.LIGHTNING_ROD)
                .setName(Component.literal("⚡ Thunderbolt").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Max Level: I").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Strikes lightning on hit").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Left/Right: Lvl 1").withStyle(ChatFormatting.YELLOW))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> applyEnchant(player, CustomEnchantmentManager.THUNDERBOLT,
                        1, "Thunderbolt"))
                .build());

        // ── DEFENSIVE ENCHANTMENTS ────────────────────────────────
        gui.setSlot(19, new GuiElementBuilder(Items.SHIELD)
                .setName(Component.literal("🛡 Bounty Protection").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Max Level: IV").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("-5% all damage per level").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Left: Lvl 1  |  Right: Lvl 4").withStyle(ChatFormatting.YELLOW))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> applyEnchant(player, CustomEnchantmentManager.BOUNTY_PROTECTION,
                        type == ClickType.MOUSE_RIGHT ? 4 : 1, "Bounty Protection"))
                .build());

        gui.setSlot(20, new GuiElementBuilder(Items.TOTEM_OF_UNDYING)
                .setName(Component.literal("🛡 Boss Ward").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Max Level: III").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("-10% boss damage per level").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Left: Lvl 1  |  Right: Lvl 3").withStyle(ChatFormatting.YELLOW))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> applyEnchant(player, CustomEnchantmentManager.BOSS_WARD,
                        type == ClickType.MOUSE_RIGHT ? 3 : 1, "Boss Ward"))
                .build());

        gui.setSlot(21, new GuiElementBuilder(Items.GOLDEN_APPLE)
                .setName(Component.literal("❤ Vitality").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Max Level: III").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("+4 max HP per level").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Left: Lvl 1  |  Right: Lvl 3").withStyle(ChatFormatting.YELLOW))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> applyEnchant(player, CustomEnchantmentManager.VITALITY,
                        type == ClickType.MOUSE_RIGHT ? 3 : 1, "Vitality"))
                .build());

        gui.setSlot(22, new GuiElementBuilder(Items.CACTUS)
                .setName(Component.literal("🌵 Thorns of Vengeance").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Max Level: I").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Reflects damage to attackers").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Left/Right: Lvl 1").withStyle(ChatFormatting.YELLOW))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> applyEnchant(player, CustomEnchantmentManager.THORNS_OF_VENGEANCE,
                        1, "Thorns of Vengeance"))
                .build());

        // Back button
        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> openCustomItemsAdminGui(player))
                .build());

        gui.open();
    }

    private static void applyEnchant(ServerPlayer player, String enchant, int level, String name) {
        ItemStack held = player.getMainHandItem();
        if (held.isEmpty()) {
            player.sendSystemMessage(Component.literal("✗ Hold an item in your main hand first!").withStyle(ChatFormatting.RED), false);
            return;
        }
        CustomEnchantmentManager.addEnchantment(held, enchant, level);
        CustomEnchantmentManager.addEnchantmentLore(held);
        String roman = switch (level) {
            case 1 -> "I"; case 2 -> "II"; case 3 -> "III"; case 4 -> "IV"; case 5 -> "V"; default -> String.valueOf(level);
        };
        player.sendSystemMessage(Component.literal("✦ Applied ").withStyle(ChatFormatting.GOLD)
                .append(Component.literal(name + " " + roman).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .append(Component.literal(" to held item!").withStyle(ChatFormatting.GOLD)), false);
    }

    // ═══════════════════════════════════════════════════════════
    // GAME SETTINGS MAIN PAGE
    // ═══════════════════════════════════════════════════════════

    private static void openGameSettingsPage(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x5, player, false);
        gui.setTitle(Component.literal("⚙ Game Settings"));
        fillBackground(gui);

        gui.setSlot(4, new GuiElementBuilder(Items.COMPARATOR)
                .setName(Component.literal("⚙ Game Settings").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Configure all custom game systems").withStyle(ChatFormatting.GRAY))
                .glow().build());

        gui.setSlot(10, new GuiElementBuilder(Items.WITHER_SKELETON_SKULL)
                .setName(Component.literal("Boss Settings").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("• HP multipliers per boss type").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("• Damage multipliers").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("• Kill requirements multiplier").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("• Ability toggle per type").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("")).addLoreLine(Component.literal("Click to configure").withStyle(ChatFormatting.YELLOW))
                .setCallback((i, t, containerInput, slotGui) -> openBossSettingsPage(player)).build());

        gui.setSlot(12, new GuiElementBuilder(Items.DIAMOND_HELMET)
                .setName(Component.literal("Item Settings").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("• Level requirements per item").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("• Ability toggles per item").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("")).addLoreLine(Component.literal("Click to configure").withStyle(ChatFormatting.YELLOW))
                .setCallback((i, t, containerInput, slotGui) -> openItemSettingsPage(player)).build());

        gui.setSlot(14, new GuiElementBuilder(Items.SHIELD)
                .setName(Component.literal("Defence Settings").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("• Armour defence cap").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("• Set bonus percentages (T1/T2)").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("• Weapon bonus percentages").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("")).addLoreLine(Component.literal("Click to configure").withStyle(ChatFormatting.YELLOW))
                .setCallback((i, t, containerInput, slotGui) -> openDefenceSettingsPage(player)).build());

        gui.setSlot(16, new GuiElementBuilder(Items.CHEST)
                .setName(Component.literal("Drop Settings").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("• Chunk drop rate multiplier").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("• Core drop rate multiplier").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("• Coin reward multiplier").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("")).addLoreLine(Component.literal("Click to configure").withStyle(ChatFormatting.YELLOW))
                .setCallback((i, t, containerInput, slotGui) -> openDropSettingsPage(player)).build());

        gui.setSlot(28, new GuiElementBuilder(Items.ZOMBIE_HEAD)
                .setName(Component.literal("Mob Settings").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("• Health scaling multiplier").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("")).addLoreLine(Component.literal("Click to configure").withStyle(ChatFormatting.YELLOW))
                .setCallback((i, t, containerInput, slotGui) -> openMobSettingsPage(player)).build());

        gui.setSlot(30, new GuiElementBuilder(Items.OAK_SIGN)
                .setName(Component.literal("Scoreboard Settings").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("• Toggle scoreboard sections on/off").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("")).addLoreLine(Component.literal("Click to configure").withStyle(ChatFormatting.YELLOW))
                .setCallback((i, t, containerInput, slotGui) -> openScoreboardSettingsPage(player)).build());

        addBackButton(gui, player);
        gui.open();
    }

    // ═══════════════════════════════════════════════════════════
    // BOSS SETTINGS PAGE
    // ═══════════════════════════════════════════════════════════

    private static void openBossSettingsPage(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Boss Settings"));
        fillBackground(gui);

        gui.setSlot(4, new GuiElementBuilder(Items.WITHER_SKELETON_SKULL)
                .setName(Component.literal("Boss Settings").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Left: +0.1, Right: -0.1, Shift: ±0.5").withStyle(ChatFormatting.GRAY))
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
                    .setName(Component.literal(t.displayName + " HP").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("Multiplier: §f" + String.format("%.2f", hpMult) + "x").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("L: +0.1 | R: -0.1 | Shift: ±0.5").withStyle(ChatFormatting.DARK_GRAY))
                    .addLoreLine(Component.literal("Middle-click: set exact value").withStyle(ChatFormatting.AQUA))
                    .setCallback((idx, clickType, containerInput, slotGui) -> {
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
                    .setName(Component.literal(t.displayName + " DMG").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("Multiplier: §f" + String.format("%.2f", dmgMult) + "x").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("L: +0.1 | R: -0.1 | Shift: ±0.5").withStyle(ChatFormatting.DARK_GRAY))
                    .addLoreLine(Component.literal("Middle-click: set exact value").withStyle(ChatFormatting.AQUA))
                    .setCallback((idx, clickType, containerInput, slotGui) -> {
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
                    .setName(Component.literal(t.displayName + " Abilities").withStyle(abilEnabled ? ChatFormatting.GREEN : ChatFormatting.RED, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("Status: " + (abilEnabled ? "§aENABLED" : "§cDISABLED")).withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("Click to toggle").withStyle(ChatFormatting.YELLOW))
                    .setCallback((idx, clickType, containerInput, slotGui) -> {
                        DataManager.setBossAbilitiesEnabled(t, !DataManager.isBossAbilitiesEnabled(t));
                        DataManager.save(PoliticalServer.server);
                        openBossSettingsPage(player);
                    }).build());
        }

        float killMult = DataManager.getBossKillsRequiredMultiplier();
        gui.setSlot(40, new GuiElementBuilder(Items.BOOK)
                .setName(Component.literal("Kill Requirement Multiplier").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Global multiplier: §f" + String.format("%.2f", killMult) + "x").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("L: +0.1 | R: -0.1 | Shift: ±0.5").withStyle(ChatFormatting.DARK_GRAY))
                .addLoreLine(Component.literal("Middle-click: set exact value").withStyle(ChatFormatting.AQUA))
                .setCallback((idx, clickType, containerInput, slotGui) -> {
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
                .setName(Component.literal("← Back").withStyle(ChatFormatting.GRAY))
                .setCallback((i, t, containerInput, slotGui) -> openGameSettingsPage(player)).build());
        gui.open();
    }

    private static void openBossMultiplierSign(ServerPlayer player, SlayerManager.SlayerType slayerType, boolean isHp, float current) {
        String label = isHp ? (slayerType.displayName + " HP multiplier") : (slayerType.displayName + " DMG multiplier");
        SignGui signGui = new SignGui(player) {
            @Override
            public void onManualClose() {
                String raw = this.getLine(0).getString().trim().replaceAll("[^0-9.]", "");
                if (raw.isEmpty()) { openBossSettingsPage(player); return; }
                try {
                    float val = Float.parseFloat(raw);
                    val = Math.max(0.1f, val);
                    if (isHp) DataManager.setBossHpMultiplier(slayerType, val);
                    else DataManager.setBossDamageMultiplier(slayerType, val);
                    DataManager.save(PoliticalServer.server);
                    player.sendSystemMessage(Component.literal("✓ Set " + label + " to " + String.format("%.2f", val) + "x!").withStyle(ChatFormatting.GREEN));
                } catch (NumberFormatException ignored) {
                    player.sendSystemMessage(Component.literal("✗ Invalid number!").withStyle(ChatFormatting.RED));
                }
                openBossSettingsPage(player);
            }
        };
        signGui.setLine(0, Component.literal(""));
        signGui.setLine(1, Component.literal("Set " + label));
        signGui.setLine(2, Component.literal("(e.g. 2.5)"));
        signGui.setLine(3, Component.literal("Current: " + String.format("%.2f", current)));
        signGui.open();
    }

    private static void openKillMultiplierSign(ServerPlayer player, float current) {
        SignGui signGui = new SignGui(player) {
            @Override
            public void onManualClose() {
                String raw = this.getLine(0).getString().trim().replaceAll("[^0-9.]", "");
                if (raw.isEmpty()) { openBossSettingsPage(player); return; }
                try {
                    float val = Math.max(0.1f, Float.parseFloat(raw));
                    DataManager.setBossKillsRequiredMultiplier(val);
                    DataManager.save(PoliticalServer.server);
                    player.sendSystemMessage(Component.literal("✓ Kill requirement multiplier set to " + String.format("%.2f", val) + "x!").withStyle(ChatFormatting.GREEN));
                } catch (NumberFormatException ignored) {
                    player.sendSystemMessage(Component.literal("✗ Invalid number!").withStyle(ChatFormatting.RED));
                }
                openBossSettingsPage(player);
            }
        };
        signGui.setLine(0, Component.literal(""));
        signGui.setLine(1, Component.literal("Kill multiplier"));
        signGui.setLine(2, Component.literal("(e.g. 1.5)"));
        signGui.setLine(3, Component.literal("Current: " + String.format("%.2f", current)));
        signGui.open();
    }

    // ═══════════════════════════════════════════════════════════
    // ITEM SETTINGS PAGE
    // ═══════════════════════════════════════════════════════════

    private static void openItemSettingsPage(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Item Settings"));
        fillBackground(gui);

        gui.setSlot(4, new GuiElementBuilder(Items.DIAMOND_HELMET)
                .setName(Component.literal("Item Level Requirements").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("L: +1 level | R: -1 | Shift: ±5").withStyle(ChatFormatting.GRAY))
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
                    .setName(Component.literal(dispName).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("Level req: §f" + curLevel + " §8(default: " + defLevel + ")").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("L: +1 | R: -1 | Shift: ±5").withStyle(ChatFormatting.DARK_GRAY))
                    .setCallback((idx, clickType, containerInput, slotGui) -> {
                        int delta = clickType.isLeft ? 1 : -1;
                        if (clickType.shift) delta *= 5;
                        DataManager.setItemLevelRequirement(itemId, DataManager.getItemLevelRequirement(itemId, defLevel) + delta);
                        DataManager.save(PoliticalServer.server);
                        openItemSettingsPage(player);
                    }).build());
        }

        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.GRAY))
                .setCallback((i, t, containerInput, slotGui) -> openGameSettingsPage(player)).build());
        gui.open();
    }

    // ═══════════════════════════════════════════════════════════
    // DEFENCE SETTINGS PAGE
    // ═══════════════════════════════════════════════════════════

    private static void openDefenceSettingsPage(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x4, player, false);
        gui.setTitle(Component.literal("Defence Settings"));
        fillBackground(gui);

        gui.setSlot(4, new GuiElementBuilder(Items.SHIELD)
                .setName(Component.literal("Defence Settings").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("L: +1% | R: -1% | Shift: ±5%").withStyle(ChatFormatting.GRAY))
                .glow().build());

        int defCap = DataManager.getDefenceCapPct();
        gui.setSlot(10, new GuiElementBuilder(Items.IRON_CHESTPLATE)
                .setName(Component.literal("Defence Cap").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Current: §f" + defCap + "%").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Max damage reduction from bounty def").withStyle(ChatFormatting.DARK_GRAY))
                .addLoreLine(Component.literal("L: +1 | R: -1 | Shift: ±5").withStyle(ChatFormatting.DARK_GRAY))
                .setCallback((idx, clickType, containerInput, slotGui) -> {
                    int delta = clickType.isLeft ? 1 : -1;
                    if (clickType.shift) delta *= 5;
                    DataManager.setDefenceCapPct(DataManager.getDefenceCapPct() + delta);
                    DataManager.save(PoliticalServer.server);
                    openDefenceSettingsPage(player);
                }).build());

        int t1Bonus = DataManager.getT1SetBonusPct();
        gui.setSlot(12, new GuiElementBuilder(Items.CHAINMAIL_CHESTPLATE)
                .setName(Component.literal("T1 Full Set Bonus").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Current: §f" + t1Bonus + "% §7boss-specific def").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("L: +1 | R: -1 | Shift: ±5").withStyle(ChatFormatting.DARK_GRAY))
                .setCallback((idx, clickType, containerInput, slotGui) -> {
                    int delta = clickType.isLeft ? 1 : -1;
                    if (clickType.shift) delta *= 5;
                    DataManager.setT1SetBonusPct(DataManager.getT1SetBonusPct() + delta);
                    DataManager.save(PoliticalServer.server);
                    openDefenceSettingsPage(player);
                }).build());

        int t2Bonus = DataManager.getT2SetBonusPct();
        gui.setSlot(14, new GuiElementBuilder(Items.IRON_CHESTPLATE)
                .setName(Component.literal("T2 Full Set Bonus").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Current: §f" + t2Bonus + "% §7boss-specific def").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("L: +1 | R: -1 | Shift: ±5").withStyle(ChatFormatting.DARK_GRAY))
                .setCallback((idx, clickType, containerInput, slotGui) -> {
                    int delta = clickType.isLeft ? 1 : -1;
                    if (clickType.shift) delta *= 5;
                    DataManager.setT2SetBonusPct(DataManager.getT2SetBonusPct() + delta);
                    DataManager.save(PoliticalServer.server);
                    openDefenceSettingsPage(player);
                }).build());

        int t1SwordSpec = DataManager.getData().t1SwordSpecificPct;
        gui.setSlot(20, new GuiElementBuilder(Items.IRON_SWORD)
                .setName(Component.literal("T1 Sword Specific Def").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Current: §f" + t1SwordSpec + "% §7vs matching boss").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("L: +1 | R: -1 | Shift: ±5").withStyle(ChatFormatting.DARK_GRAY))
                .setCallback((idx, clickType, containerInput, slotGui) -> {
                    int delta = clickType.isLeft ? 1 : -1;
                    if (clickType.shift) delta *= 5;
                    DataManager.getData().t1SwordSpecificPct = Math.max(0, Math.min(100, DataManager.getData().t1SwordSpecificPct + delta));
                    DataManager.save(PoliticalServer.server);
                    openDefenceSettingsPage(player);
                }).build());

        int t2SwordSpec = DataManager.getData().t2SwordSpecificPct;
        gui.setSlot(22, new GuiElementBuilder(Items.DIAMOND_SWORD)
                .setName(Component.literal("T2 Sword Specific Def").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Current: §f" + t2SwordSpec + "% §7vs matching boss").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("L: +1 | R: -1 | Shift: ±5").withStyle(ChatFormatting.DARK_GRAY))
                .setCallback((idx, clickType, containerInput, slotGui) -> {
                    int delta = clickType.isLeft ? 1 : -1;
                    if (clickType.shift) delta *= 5;
                    DataManager.getData().t2SwordSpecificPct = Math.max(0, Math.min(100, DataManager.getData().t2SwordSpecificPct + delta));
                    DataManager.save(PoliticalServer.server);
                    openDefenceSettingsPage(player);
                }).build());

        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.GRAY))
                .setCallback((i, t, containerInput, slotGui) -> openGameSettingsPage(player)).build());
        gui.open();
    }

    // ═══════════════════════════════════════════════════════════
    // DROP SETTINGS PAGE
    // ═══════════════════════════════════════════════════════════

    private static void openDropSettingsPage(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x4, player, false);
        gui.setTitle(Component.literal("Drop Settings"));
        fillBackground(gui);

        gui.setSlot(4, new GuiElementBuilder(Items.CHEST)
                .setName(Component.literal("Drop Settings").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("L: +0.1x | R: -0.1x | Shift: ±0.5x").withStyle(ChatFormatting.GRAY))
                .glow().build());

        float chunkMult = DataManager.getChunkDropMultiplier();
        gui.setSlot(10, new GuiElementBuilder(Items.PAPER)
                .setName(Component.literal("Chunk Drop Rate Multiplier").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Current: §f" + String.format("%.1f", chunkMult) + "x").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Multiplies chunk drop chance").withStyle(ChatFormatting.DARK_GRAY))
                .addLoreLine(Component.literal("L: +0.1 | R: -0.1 | Shift: ±0.5").withStyle(ChatFormatting.DARK_GRAY))
                .setCallback((idx, clickType, containerInput, slotGui) -> {
                    float delta = clickType.isLeft ? 0.1f : -0.1f;
                    if (clickType.shift) delta *= 5;
                    DataManager.setChunkDropMultiplier(DataManager.getChunkDropMultiplier() + delta);
                    DataManager.save(PoliticalServer.server);
                    openDropSettingsPage(player);
                }).build());

        float coreMult = DataManager.getCoreDropMultiplier();
        gui.setSlot(12, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Component.literal("Core Drop Rate Multiplier").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Current: §f" + String.format("%.1f", coreMult) + "x").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Multiplies core drop chance").withStyle(ChatFormatting.DARK_GRAY))
                .addLoreLine(Component.literal("L: +0.1 | R: -0.1 | Shift: ±0.5").withStyle(ChatFormatting.DARK_GRAY))
                .setCallback((idx, clickType, containerInput, slotGui) -> {
                    float delta = clickType.isLeft ? 0.1f : -0.1f;
                    if (clickType.shift) delta *= 5;
                    DataManager.setCoreDropMultiplier(DataManager.getCoreDropMultiplier() + delta);
                    DataManager.save(PoliticalServer.server);
                    openDropSettingsPage(player);
                }).build());

        float coinMult = DataManager.getCoinRewardMultiplier();
        gui.setSlot(14, new GuiElementBuilder(Items.GOLD_NUGGET)
                .setName(Component.literal("Coin Reward Multiplier").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Current: §f" + String.format("%.1f", coinMult) + "x").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Multiplies coin rewards from kills").withStyle(ChatFormatting.DARK_GRAY))
                .addLoreLine(Component.literal("L: +0.1 | R: -0.1 | Shift: ±0.5").withStyle(ChatFormatting.DARK_GRAY))
                .setCallback((idx, clickType, containerInput, slotGui) -> {
                    float delta = clickType.isLeft ? 0.1f : -0.1f;
                    if (clickType.shift) delta *= 5;
                    DataManager.setCoinRewardMultiplier(DataManager.getCoinRewardMultiplier() + delta);
                    DataManager.save(PoliticalServer.server);
                    openDropSettingsPage(player);
                }).build());

        float fleshMult = DataManager.getFleshDropMultiplier();
        gui.setSlot(16, new GuiElementBuilder(Items.ROTTEN_FLESH)
                .setName(Component.literal("Flesh Drop Rate Multiplier").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Current: §f" + String.format("%.1f", fleshMult) + "x").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Multiplies flesh drop chance (90% base)").withStyle(ChatFormatting.DARK_GRAY))
                .addLoreLine(Component.literal("Affects Zombie & Piglin bosses").withStyle(ChatFormatting.DARK_GRAY))
                .addLoreLine(Component.literal("L: +0.1 | R: -0.1 | Shift: ±0.5").withStyle(ChatFormatting.DARK_GRAY))
                .setCallback((idx, clickType, containerInput, slotGui) -> {
                    float delta = clickType.isLeft ? 0.1f : -0.1f;
                    if (clickType.shift) delta *= 5;
                    DataManager.setFleshDropMultiplier(DataManager.getFleshDropMultiplier() + delta);
                    DataManager.save(PoliticalServer.server);
                    openDropSettingsPage(player);
                }).build());

        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.GRAY))
                .setCallback((i, t, containerInput, slotGui) -> openGameSettingsPage(player)).build());
        gui.open();
    }

    // ═══════════════════════════════════════════════════════════
    // MOB SETTINGS PAGE
    // ═══════════════════════════════════════════════════════════

    private static void openMobSettingsPage(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x4, player, false);
        gui.setTitle(Component.literal("Mob Settings"));
        fillBackground(gui);

        gui.setSlot(4, new GuiElementBuilder(Items.ZOMBIE_HEAD)
                .setName(Component.literal("Mob Settings").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("L: +0.1x | R: -0.1x | Shift: ±0.5x").withStyle(ChatFormatting.GRAY))
                .glow().build());

        float healthMult = DataManager.getMobHealthScalingMultiplier();
        gui.setSlot(12, new GuiElementBuilder(Items.APPLE)
                .setName(Component.literal("Health Scaling Multiplier").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Current: §f" + String.format("%.1f", healthMult) + "x").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Multiplies health of all scaled mobs").withStyle(ChatFormatting.DARK_GRAY))
                .addLoreLine(Component.literal("L: +0.1 | R: -0.1 | Shift: ±0.5").withStyle(ChatFormatting.DARK_GRAY))
                .setCallback((idx, clickType, containerInput, slotGui) -> {
                    float delta = clickType.isLeft ? 0.1f : -0.1f;
                    if (clickType.shift) delta *= 5;
                    DataManager.setMobHealthScalingMultiplier(DataManager.getMobHealthScalingMultiplier() + delta);
                    DataManager.save(PoliticalServer.server);
                    openMobSettingsPage(player);
                }).build());

        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.GRAY))
                .setCallback((i, t, containerInput, slotGui) -> openGameSettingsPage(player)).build());
        gui.open();
    }

    // ═══════════════════════════════════════════════════════════
    // SCOREBOARD SETTINGS PAGE
    // ═══════════════════════════════════════════════════════════

    private static void openScoreboardSettingsPage(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x4, player, false);
        gui.setTitle(Component.literal("Scoreboard Settings"));
        fillBackground(gui);

        gui.setSlot(4, new GuiElementBuilder(Items.OAK_SIGN)
                .setName(Component.literal("Scoreboard Settings").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Toggle scoreboard sections").withStyle(ChatFormatting.GRAY))
                .glow().build());

        boolean showAuction = DataManager.isScoreboardShowAuction();
        gui.setSlot(11, new GuiElementBuilder(showAuction ? Items.LIME_DYE : Items.GRAY_DYE)
                .setName(Component.literal("Show Auction Timer").withStyle(showAuction ? ChatFormatting.GREEN : ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Status: " + (showAuction ? "§aVISIBLE" : "§cHIDDEN")).withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Click to toggle").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, clickType, containerInput, slotGui) -> {
                    DataManager.setScoreboardShowAuction(!DataManager.isScoreboardShowAuction());
                    DataManager.save(PoliticalServer.server);
                    openScoreboardSettingsPage(player);
                }).build());

        boolean showBounty = DataManager.isScoreboardShowBountyGear();
        gui.setSlot(13, new GuiElementBuilder(showBounty ? Items.LIME_DYE : Items.GRAY_DYE)
                .setName(Component.literal("Show Bounty Gear Stats").withStyle(showBounty ? ChatFormatting.GREEN : ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Status: " + (showBounty ? "§aVISIBLE" : "§cHIDDEN")).withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Click to toggle").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, clickType, containerInput, slotGui) -> {
                    DataManager.setScoreboardShowBountyGear(!DataManager.isScoreboardShowBountyGear());
                    DataManager.save(PoliticalServer.server);
                    openScoreboardSettingsPage(player);
                }).build());

        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.GRAY))
                .setCallback((i, t, containerInput, slotGui) -> openGameSettingsPage(player)).build());
        gui.open();
    }

    // ═══════════════════════════════════════════════════════════
    // PERK MANAGEMENT PAGE
    // ═══════════════════════════════════════════════════════════

    private static void openPerkManagementPage(ServerPlayer player) {
        java.util.Collection<Perk> allPerks = PerkManager.getAllPerks();
        java.util.List<String> activePerks = PerkManager.getActivePerks();

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("✦ Perk Management"));
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE).setName(Component.literal("")).build());
        }

        gui.setSlot(4, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Component.literal("✦ Perk Management").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Active: §a" + activePerks.size() + " §7/ §f" + allPerks.size()).withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Left: Activate | Right: Deactivate").withStyle(ChatFormatting.DARK_GRAY))
                .glow().build());

        // Reset All Perks button
        gui.setSlot(8, new GuiElementBuilder(Items.TNT)
                .setName(Component.literal("Reset All Perks").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Deactivates ALL perks").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("▶ Click to reset").withStyle(ChatFormatting.YELLOW))
                .setCallback((int idx, eu.pb4.sgui.api.ClickType type, net.minecraft.world.inventory.ContainerInput containerInput, eu.pb4.sgui.api.gui.SlotBasedGui slotGui) -> {
                    PerkManager.clearAllPerks();
                    DataManager.save(PoliticalServer.server);
                    player.sendSystemMessage(Component.literal("✓ All perks reset!").withStyle(ChatFormatting.RED));
                    openPerkManagementPage(player);
                }).build());

        int slot = 10;
        for (Perk perk : allPerks) {
            if (slot >= 53) break;
            if (slot % 9 == 0) slot++;
            if (slot % 9 == 8) { slot++; continue; }

            boolean active = activePerks.contains(perk.id);
            net.minecraft.world.item.Item perkIcon = active ? Items.LIME_DYE : Items.GRAY_DYE;
            final Perk finalPerk = perk;
            gui.setSlot(slot, new GuiElementBuilder(perkIcon)
                    .setName(Component.literal(perk.name).withStyle(active ? ChatFormatting.GREEN : ChatFormatting.GRAY, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("Status: " + (active ? "§aACTIVE" : "§cINACTIVE")).withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("ID: §7" + perk.id).withStyle(ChatFormatting.DARK_GRAY))
                    .addLoreLine(Component.literal(active ? "Right-click: Deactivate" : "Left-click: Activate").withStyle(ChatFormatting.YELLOW))
                    .setCallback((idx, type, containerInput, slotGui) -> {
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
                .setName(Component.literal("← Back").withStyle(ChatFormatting.GRAY))
                .setCallback((i, t, containerInput, slotGui) -> openMainPage(player)).build());
        gui.open();
    }

    // ═══════════════════════════════════════════════════════════
    // GIVE COINS SELECTOR — sign-based input
    // ═══════════════════════════════════════════════════════════

    private static void openGiveCoinsSelector(ServerPlayer admin, ServerPlayer target) {
        int currentCoins = CoinManager.getCoins(target);
        SignGui signGui = new SignGui(admin) {
            @Override
            public void onManualClose() {
                // allow negative to mean "remove" — e.g. "-500" removes 500 coins
                Integer amount = parseSignAmount(this.getLine(0).getString(), true);
                if (amount == null) { openPlayersPage(admin); return; }
                if (amount > 0) {
                    CoinManager.giveCoins(target, amount);
                    admin.sendSystemMessage(Component.literal("✓ Gave " + amount + " coins to " + target.getName().getString() + "!").withStyle(ChatFormatting.GREEN));
                } else if (amount < 0) {
                    CoinManager.removeCoins(target, -amount);
                    admin.sendSystemMessage(Component.literal("✓ Removed " + (-amount) + " coins from " + target.getName().getString() + "!").withStyle(ChatFormatting.RED));
                }
                openPlayersPage(admin);
            }
        };
        signGui.setLine(0, Component.literal(""));
        signGui.setLine(1, Component.literal("+ give / - remove"));
        signGui.setLine(2, Component.literal(target.getName().getString()));
        signGui.setLine(3, Component.literal("Coins: " + currentCoins));
        signGui.open();
    }

    // ═══════════════════════════════════════════════════════════
    // SLAYER LEVEL SELECTOR — sign-based input per type
    // ═══════════════════════════════════════════════════════════

    private static void openSlayerLevelSelector(ServerPlayer admin, ServerPlayer target) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x5, admin, false);
        gui.setTitle(Component.literal("Slayer Levels — " + target.getName().getString()));
        fillBackground(gui);

        gui.setSlot(4, new GuiElementBuilder(Items.EXPERIENCE_BOTTLE)
                .setName(Component.literal(target.getName().getString()).withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Click a type to set its level via sign").withStyle(ChatFormatting.GRAY))
                .glow().build());

        SlayerManager.SlayerType[] types = SlayerManager.SlayerType.values();
        int[] slotPositions = {10, 11, 12, 13, 14, 15, 16};
        for (int i = 0; i < types.length && i < slotPositions.length; i++) {
            SlayerManager.SlayerType t = types[i];
            int currentLevel = SlayerData.getSlayerLevel(target.getUUID().toString(), t);
            final SlayerManager.SlayerType finalType = t;
            gui.setSlot(slotPositions[i], new GuiElementBuilder(Items.BOOK)
                    .setName(Component.literal(t.displayName + " Lvl " + currentLevel).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("Current: §f" + currentLevel).withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("▶ Click to set level").withStyle(ChatFormatting.YELLOW))
                    .setCallback((idx, clickType, containerInput, slotGui) -> {
                        openSlayerLevelSign(admin, target, finalType);
                    }).build());        }

        addBackButton(gui, admin);
        gui.open();
    }

    private static void openSlayerLevelSign(ServerPlayer admin, ServerPlayer target, SlayerManager.SlayerType slayerType) {
        int currentLevel = SlayerData.getSlayerLevel(target.getUUID().toString(), slayerType);
        SignGui signGui = new SignGui(admin) {
            @Override
            public void onManualClose() {
                String raw = this.getLine(0).getString().trim().replaceAll("[^0-9]", "");
                if (raw.isEmpty()) { openSlayerLevelSelector(admin, target); return; }
                try {
                    int newLevel = Integer.parseInt(raw);
                    if (newLevel < 0) { admin.sendSystemMessage(Component.literal("✗ Level must be >= 0!").withStyle(ChatFormatting.RED)); openSlayerLevelSelector(admin, target); return; }
                    SlayerData.setSlayerLevel(target.getUUID().toString(), slayerType, newLevel);
                    DataManager.save(PoliticalServer.server);
                    admin.sendSystemMessage(Component.literal("✓ Set " + target.getName().getString() + "'s " + slayerType.displayName + " level to " + newLevel + "!").withStyle(ChatFormatting.GREEN));
                } catch (NumberFormatException ignored) {
                    admin.sendSystemMessage(Component.literal("✗ Invalid number!").withStyle(ChatFormatting.RED));
                }
                openSlayerLevelSelector(admin, target);
            }
        };
        signGui.setLine(0, Component.literal(""));
        signGui.setLine(1, Component.literal(slayerType.displayName + " Level"));
        signGui.setLine(2, Component.literal(target.getName().getString()));
        signGui.setLine(3, Component.literal("Current: " + currentLevel));
        signGui.open();
    }}
