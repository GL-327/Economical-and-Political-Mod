package com.political.party;
import com.political.DataManager;
import com.political.government.Treasury;


import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.gui.SignGui;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.*;
import java.util.stream.Collectors;

public final class PartyJoinGui {

    // ════════════════════════════════════════════════════════════
    //  ENTRY POINT
    // ════════════════════════════════════════════════════════════

    public static void open(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        String partyId = PoliticalPartyManager.getPlayerPartyId(uuid);
        if (partyId != null && PoliticalPartyManager.getParty(partyId) != null) {
            openPartyHub(player, partyId);
        } else {
            openPartyPicker(player);
        }
    }

    // ════════════════════════════════════════════════════════════
    //  MY PARTY HUB (9x6)
    // ════════════════════════════════════════════════════════════

    private static void openPartyHub(ServerPlayer player, String partyId) {
        PoliticalPartyManager.Party party = PoliticalPartyManager.getParty(partyId);
        if (party == null) {
            openPartyPicker(player);
            return;
        }
        String uuid = player.getUUID().toString();
        String leaderUuid = PoliticalPartyManager.getPartyLeaderUuid(partyId);
        boolean isLeader = leaderUuid != null && leaderUuid.equals(uuid);
        List<String> council = PoliticalPartyManager.getCouncilUuids(partyId);
        boolean isCouncil = council.contains(uuid);
        boolean isRuling = partyId.equals(PoliticalPartyManager.getRulingPartyId());
        String myRank = PoliticalPartyManager.getPartyRank(partyId, uuid);

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("My Party").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

        fillPane(gui, 54, Items.BLACK_STAINED_GLASS_PANE);

        String leaderName = leaderUuid != null ? DataManager.getPlayerName(leaderUuid) : "None";
        GuiElementBuilder banner = new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Component.literal(party.displayName).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Budget: " + fmt(party.budgetCoins) + " coins").withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal("Members: " + party.members.size()).withStyle(ChatFormatting.AQUA))
                .addLoreLine(Component.literal("Leader: " + leaderName).withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal("Your Rank: " + myRank).withStyle(ChatFormatting.LIGHT_PURPLE));
        if (isRuling) {
            banner.addLoreLine(Component.literal(""));
            banner.addLoreLine(Component.literal("\u2605 Ruling Party \u2605").withStyle(ChatFormatting.GOLD));
        }
        gui.setSlot(4, banner.build());

        // --- Slot 10: Donate Coins ---
        gui.setSlot(10, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Component.literal("Donate Coins").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Your support: " + fmt(PoliticalPartyManager.getSupport(uuid))).withStyle(ChatFormatting.AQUA))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to donate coins to the").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("party budget and gain support.").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("1 support per " + fmt(PoliticalPartyManager.COINS_PER_SUPPORT_POINT) + " coins").withStyle(ChatFormatting.DARK_GRAY))
                .setCallback((index, type, containerInput, slotGui) -> openDonateSign(player))
                .build());

        // --- Slot 12: Party Platform ---
        String platformText = (party.platform != null && !party.platform.isEmpty())
                ? party.platform : "No platform set";
        GuiElementBuilder platformItem = new GuiElementBuilder(Items.WRITABLE_BOOK)
                .setName(Component.literal("Party Platform").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""));
        for (String line : wrapText(platformText, 40)) {
            platformItem.addLoreLine(Component.literal(line).withStyle(ChatFormatting.WHITE));
        }
        if (isLeader) {
            platformItem.addLoreLine(Component.literal(""));
            platformItem.addLoreLine(Component.literal("Click to set new platform").withStyle(ChatFormatting.YELLOW));
            platformItem.setCallback((index, type, containerInput, slotGui) -> openPlatformSign(player));
        }
        gui.setSlot(12, platformItem.build());

        // --- Slot 14: Members ---
        gui.setSlot(14, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Component.literal("Members").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(party.members.size() + " member" + (party.members.size() != 1 ? "s" : "")).withStyle(ChatFormatting.WHITE))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to view member list").withStyle(ChatFormatting.YELLOW))
                .setCallback((index, type, containerInput, slotGui) -> openMemberList(player, partyId))
                .build());

        // --- Slot 16: Party Budget ---
        long totalSupport = party.members.values().stream().mapToLong(m -> m.support).sum();
        gui.setSlot(16, new GuiElementBuilder(Items.EMERALD_BLOCK)
                .setName(Component.literal("Party Budget").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Total: " + fmt(party.budgetCoins) + " coins").withStyle(ChatFormatting.WHITE))
                .addLoreLine(Component.literal("Total support: " + fmt(totalSupport)).withStyle(ChatFormatting.AQUA))
                .addLoreLine(Component.literal("Your support: " + fmt(PoliticalPartyManager.getSupport(uuid))).withStyle(ChatFormatting.AQUA))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Hourly salary pool: 0.8% of budget").withStyle(ChatFormatting.DARK_GRAY))
                .addLoreLine(Component.literal("distributed by support share").withStyle(ChatFormatting.DARK_GRAY))
                .build());

        gui.setSlot(20, new GuiElementBuilder(Items.NAME_TAG)
                .setName(Component.literal("Affiliation").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Parties are a name, color, and members.").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("National tax and VAT go to the treasury.").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Donate to grow the party kitty.").withStyle(ChatFormatting.YELLOW))
                .build());

        // --- Slot 22: Diplomacy ---
        int warCount = PoliticalPartyManager.getWars(partyId).size();
        int allyCount = PoliticalPartyManager.getAlliances(partyId).size();
        gui.setSlot(22, new GuiElementBuilder(Items.SHIELD)
                .setName(Component.literal("Diplomacy").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Wars: " + warCount).withStyle(ChatFormatting.RED))
                .addLoreLine(Component.literal("Alliances: " + allyCount).withStyle(ChatFormatting.AQUA))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to manage wars & alliances").withStyle(ChatFormatting.YELLOW))
                .setCallback((index, type, containerInput, slotGui) -> openDiplomacyGui(player, partyId))
                .build());

        // --- Slot 24: Treasury Log ---
        gui.setSlot(24, new GuiElementBuilder(Items.BOOK)
                .setName(Component.literal("Treasury Log").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("View recent transactions").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to open log").withStyle(ChatFormatting.YELLOW))
                .setCallback((index, type, containerInput, slotGui) -> openTreasuryLogGui(player, partyId))
                .build());

        // --- Slot 26: Party Event ---
        PoliticalPartyManager.PartyEvent event = PoliticalPartyManager.getPartyEvent(partyId);
        GuiElementBuilder eventItem = new GuiElementBuilder(Items.FIREWORK_ROCKET)
                .setName(Component.literal("Party Event").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        if (event != null && System.currentTimeMillis() < event.endMs && !event.rewarded) {
            long totalProg = event.memberProgress.values().stream().mapToLong(Long::longValue).sum();
            long remaining = (event.endMs - System.currentTimeMillis()) / 1000L / 3600L;
            eventItem.addLoreLine(Component.literal("Active: " + PoliticalPartyManager.eventDisplayName(event.eventType)).withStyle(ChatFormatting.GREEN));
            eventItem.addLoreLine(Component.literal("Progress: " + fmt(totalProg)).withStyle(ChatFormatting.AQUA));
            eventItem.addLoreLine(Component.literal("Time left: ~" + remaining + "h").withStyle(ChatFormatting.GRAY));
        } else {
            eventItem.addLoreLine(Component.literal("No active event").withStyle(ChatFormatting.GRAY));
            if (isLeader) {
                eventItem.addLoreLine(Component.literal(""));
                eventItem.addLoreLine(Component.literal("Click to start an event").withStyle(ChatFormatting.YELLOW));
                eventItem.setCallback((index, type, containerInput, slotGui) -> openStartEventGui(player, partyId));
            }
        }
        gui.setSlot(26, eventItem.build());

        // --- Slot 37: Leave Party ---
        gui.setSlot(37, new GuiElementBuilder(Items.RED_DYE)
                .setName(Component.literal("Leave Party").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Click to leave " + party.displayName).withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("24h cooldown before joining another").withStyle(ChatFormatting.DARK_GRAY))
                .setCallback((index, type, containerInput, slotGui) -> openLeaveConfirmation(player, partyId))
                .build());

        // --- Slot 39: Party List ---
        gui.setSlot(39, new GuiElementBuilder(Items.COMPASS)
                .setName(Component.literal("Party List").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Browse all parties").withStyle(ChatFormatting.GRAY))
                .setCallback((index, type, containerInput, slotGui) -> openPartyBrowser(player))
                .build());

        // --- Slot 41: Leader Tools ---
        if (isLeader) {
            gui.setSlot(41, new GuiElementBuilder(Items.GOLDEN_HELMET)
                    .setName(Component.literal("Leader Tools").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("Ads, perks, wars, promotions").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to open").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> PartyLeaderGui.open(player))
                    .build());
        } else {
            gui.setSlot(41, new GuiElementBuilder(Items.IRON_HELMET)
                    .setName(Component.literal("Leader Tools").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("Only the party leader can access").withStyle(ChatFormatting.DARK_GRAY))
                    .build());
        }

        // --- Slot 43: Council Vote ---
        PoliticalPartyManager.PerkProposal proposal = PoliticalPartyManager.getOpenProposalForCouncil(uuid);
        if (isCouncil && proposal != null) {
            gui.setSlot(43, new GuiElementBuilder(Items.LECTERN)
                    .setName(Component.literal("Council Vote").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("Proposal: " + proposal.perkId).withStyle(ChatFormatting.WHITE))
                    .addLoreLine(Component.literal("Cost: " + fmt(proposal.costCoins) + " coins").withStyle(ChatFormatting.RED))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to vote").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> openCouncilVote(player, proposal))
                    .build());
        } else {
            gui.setSlot(43, new GuiElementBuilder(Items.FLOWER_BANNER_PATTERN)
                    .setName(Component.literal("Council Vote").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(isCouncil ? "No pending proposals" : "Council members only").withStyle(ChatFormatting.DARK_GRAY))
                    .build());
        }

        // --- Slot 49: Close ---
        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("Close").withStyle(ChatFormatting.RED))
                .setCallback((index, type, containerInput, slotGui) -> player.closeContainer())
                .build());

        gui.open();
    }

    // ════════════════════════════════════════════════════════════
    //  PARTY PERKS SUB-GUI
    // ════════════════════════════════════════════════════════════

    private static void openPerksGui(ServerPlayer player, String partyId) {
        boolean isLeader = PoliticalPartyManager.isPartyLeader(player);
        Set<String> owned = PoliticalPartyManager.getPartyPerks(partyId);
        PoliticalPartyManager.Party party = PoliticalPartyManager.getParty(partyId);

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Party Perks").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        fillPane(gui, 54, Items.PURPLE_STAINED_GLASS_PANE);

        gui.setSlot(4, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Component.literal("Party perk treasury").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Purchases spend only party budget.").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(party != null ? "Budget: " + fmt(party.budgetCoins) + " coins" : "").withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Owned: " + owned.size() + " / " + PoliticalPartyManager.PERK_DEFS.size()).withStyle(ChatFormatting.AQUA))
                .addLoreLine(Component.literal("Green = owned | Red = leader can buy | Gray = locked").withStyle(ChatFormatting.DARK_GRAY))
                .build());

        int[] slots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34
        };
        int i = 0;
        for (PoliticalPartyManager.PartyPerkDef def : PoliticalPartyManager.PERK_DEFS) {
            if (i >= slots.length) break;
            int s = slots[i++];
            boolean has = owned.contains(def.id);
            var perkIcon = has ? Items.LIME_DYE : (isLeader ? Items.RED_DYE : Items.GRAY_DYE);
            GuiElementBuilder item = new GuiElementBuilder(perkIcon)
                    .setName(Component.literal(def.displayName).withStyle(has ? ChatFormatting.GREEN : ChatFormatting.WHITE, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(def.description).withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(""));
            if (has) {
                item.addLoreLine(Component.literal("\u2714 Owned").withStyle(ChatFormatting.GREEN));
            } else {
                item.addLoreLine(Component.literal("Cost: " + fmt(def.cost) + " (party budget)").withStyle(ChatFormatting.RED));
                if (isLeader) {
                    item.addLoreLine(Component.literal("Click to purchase").withStyle(ChatFormatting.YELLOW));
                    String perkId = def.id;
                    item.setCallback((index, type, containerInput, slotGui) -> {
                        PoliticalPartyManager.tryBuyPartyPerk(player, perkId);
                        openPerksGui(player, partyId);
                    });
                } else {
                    item.addLoreLine(Component.literal("Leader purchases only").withStyle(ChatFormatting.DARK_GRAY));
                }
            }
            gui.setSlot(s, item.build());
        }
        for (; i < slots.length; i++) {
            gui.setSlot(slots[i], new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE).setName(Component.literal("")).build());
        }

        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((index, type, containerInput, slotGui) -> open(player))
                .build());

        gui.open();
    }

    // ════════════════════════════════════════════════════════════
    //  DIPLOMACY SUB-GUI
    // ════════════════════════════════════════════════════════════

    private static void openDiplomacyGui(ServerPlayer player, String partyId) {
        boolean isLeader = PoliticalPartyManager.isPartyLeader(player);
        Set<String> wars = PoliticalPartyManager.getWars(partyId);
        Set<String> allies = PoliticalPartyManager.getAlliances(partyId);
        Set<String> peaceInbox = PoliticalPartyManager.getPeacePendingFrom(partyId);
        Set<String> allyInbox = PoliticalPartyManager.getAlliancePendingFrom(partyId);

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Diplomacy").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        fillPane(gui, 54, Items.RED_STAINED_GLASS_PANE);

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.SHIELD)
                .setName(Component.literal("Diplomacy Overview").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Active Wars: " + wars.size()).withStyle(ChatFormatting.RED))
                .addLoreLine(Component.literal("Alliances: " + allies.size()).withStyle(ChatFormatting.AQUA))
                .addLoreLine(Component.literal("Peace offers: " + peaceInbox.size()).withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal("Alliance offers: " + allyInbox.size()).withStyle(ChatFormatting.LIGHT_PURPLE))
                .build());

        // Wars section
        int slot = 9;
        gui.setSlot(slot++, new GuiElementBuilder(Items.IRON_SWORD)
                .setName(Component.literal("\u2694 Active Wars").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .build());
        for (String enemyId : wars) {
            if (slot >= 18) break;
            PoliticalPartyManager.Party enemy = PoliticalPartyManager.getParty(enemyId);
            String eName = enemy != null ? enemy.displayName : enemyId;
            GuiElementBuilder warItem = new GuiElementBuilder(Items.RED_BANNER)
                    .setName(Component.literal(eName).withStyle(ChatFormatting.RED))
                    .addLoreLine(Component.literal("At war").withStyle(ChatFormatting.DARK_RED));
            if (isLeader) {
                warItem.addLoreLine(Component.literal(""));
                warItem.addLoreLine(Component.literal("Click to propose peace (5M)").withStyle(ChatFormatting.YELLOW));
                String eid = enemyId;
                warItem.setCallback((index, type, containerInput, slotGui) -> {
                    PoliticalPartyManager.proposePeace(player, eid);
                    openDiplomacyGui(player, partyId);
                });
            }
            gui.setSlot(slot++, warItem.build());
        }

        // Alliances section
        slot = 18;
        gui.setSlot(slot++, new GuiElementBuilder(Items.GOLDEN_APPLE)
                .setName(Component.literal("\u2764 Alliances").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .build());
        for (String allyId : allies) {
            if (slot >= 27) break;
            PoliticalPartyManager.Party ally = PoliticalPartyManager.getParty(allyId);
            String aName = ally != null ? ally.displayName : allyId;
            GuiElementBuilder allyItem = new GuiElementBuilder(Items.BLUE_BANNER)
                    .setName(Component.literal(aName).withStyle(ChatFormatting.AQUA))
                    .addLoreLine(Component.literal("Allied").withStyle(ChatFormatting.GREEN));
            if (isLeader) {
                allyItem.addLoreLine(Component.literal(""));
                allyItem.addLoreLine(Component.literal("Click to break alliance").withStyle(ChatFormatting.RED));
                String aid = allyId;
                allyItem.setCallback((index, type, containerInput, slotGui) -> {
                    PoliticalPartyManager.breakAlliance(player, aid);
                    openDiplomacyGui(player, partyId);
                });
            }
            gui.setSlot(slot++, allyItem.build());
        }

        // Peace offers inbox
        if (isLeader && !peaceInbox.isEmpty()) {
            slot = 27;
            gui.setSlot(slot++, new GuiElementBuilder(Items.WHITE_BANNER)
                    .setName(Component.literal("\u2618 Peace Offers").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                    .build());
            for (String fromId : peaceInbox) {
                if (slot >= 36) break;
                PoliticalPartyManager.Party from = PoliticalPartyManager.getParty(fromId);
                String fName = from != null ? from.displayName : fromId;
                String fid = fromId;
                gui.setSlot(slot++, new GuiElementBuilder(Items.YELLOW_BANNER)
                        .setName(Component.literal(fName + " offers peace").withStyle(ChatFormatting.YELLOW))
                        .addLoreLine(Component.literal("Click to accept").withStyle(ChatFormatting.GREEN))
                        .setCallback((index, type, containerInput, slotGui) -> {
                            PoliticalPartyManager.acceptPeace(player, fid);
                            openDiplomacyGui(player, partyId);
                        })
                        .build());
            }
        }

        // Alliance offers inbox
        if (isLeader && !allyInbox.isEmpty()) {
            slot = 36;
            gui.setSlot(slot++, new GuiElementBuilder(Items.CYAN_BANNER)
                    .setName(Component.literal("\u2764 Alliance Offers").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                    .build());
            for (String fromId : allyInbox) {
                if (slot >= 45) break;
                PoliticalPartyManager.Party from = PoliticalPartyManager.getParty(fromId);
                String fName = from != null ? from.displayName : fromId;
                String fid = fromId;
                gui.setSlot(slot++, new GuiElementBuilder(Items.LIGHT_BLUE_BANNER)
                        .setName(Component.literal(fName + " offers alliance").withStyle(ChatFormatting.AQUA))
                        .addLoreLine(Component.literal("Click to accept").withStyle(ChatFormatting.GREEN))
                        .setCallback((index, type, containerInput, slotGui) -> {
                            PoliticalPartyManager.acceptAlliance(player, fid);
                            openDiplomacyGui(player, partyId);
                        })
                        .build());
            }
        }

        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((index, type, containerInput, slotGui) -> open(player))
                .build());

        gui.open();
    }

    // ════════════════════════════════════════════════════════════
    //  TREASURY LOG SUB-GUI
    // ════════════════════════════════════════════════════════════

    private static void openTreasuryLogGui(ServerPlayer player, String partyId) {
        List<PoliticalPartyManager.TreasuryLog> logs = PoliticalPartyManager.getTreasuryLog(partyId);

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Treasury Log").withStyle(ChatFormatting.GREEN));
        fillPane(gui, 54, Items.LIME_STAINED_GLASS_PANE);

        List<PoliticalPartyManager.TreasuryLog> reversed = new ArrayList<>(logs);
        Collections.reverse(reversed);

        int slot = 0;
        for (PoliticalPartyManager.TreasuryLog log : reversed) {
            if (slot >= 45) break;
            long ago = (System.currentTimeMillis() - log.timestampMs) / 1000L / 60L;
            String timeStr = ago < 60 ? ago + "m ago" : (ago / 60) + "h ago";
            ChatFormatting color = log.amount >= 0 ? ChatFormatting.GREEN : ChatFormatting.RED;
            String amtStr = log.amount == 0 ? "" : (log.amount > 0 ? "+" : "") + fmt(log.amount);

            gui.setSlot(slot, new GuiElementBuilder(log.amount >= 0 ? Items.EMERALD : Items.REDSTONE)
                    .setName(Component.literal(log.action).withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(amtStr.isEmpty() ? "" : amtStr + " coins").withStyle(color))
                    .addLoreLine(Component.literal(log.detail != null ? log.detail : "").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(timeStr).withStyle(ChatFormatting.DARK_GRAY))
                    .build());
            slot++;
        }

        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((index, type, containerInput, slotGui) -> open(player))
                .build());

        gui.open();
    }

    // ════════════════════════════════════════════════════════════
    //  START EVENT SUB-GUI (leader only)
    // ════════════════════════════════════════════════════════════

    private static void openStartEventGui(ServerPlayer player, String partyId) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal("Start Party Event").withStyle(ChatFormatting.GOLD));
        fillPane(gui, 27, Items.ORANGE_STAINED_GLASS_PANE);

        gui.setSlot(10, new GuiElementBuilder(Items.DIAMOND_SWORD)
                .setName(Component.literal("Mob Kill Race").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Compete for most mob kills").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Duration: 7 days").withStyle(ChatFormatting.DARK_GRAY))
                .addLoreLine(Component.literal("Reward: 5M to party budget").withStyle(ChatFormatting.GREEN))
                .setCallback((index, type, containerInput, slotGui) -> {
                    PoliticalPartyManager.startPartyEvent(partyId, "mob_kill_race");
                    open(player);
                })
                .build());

        gui.setSlot(12, new GuiElementBuilder(Items.DIAMOND_PICKAXE)
                .setName(Component.literal("Mining Race").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Compete for most blocks mined").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Duration: 7 days").withStyle(ChatFormatting.DARK_GRAY))
                .addLoreLine(Component.literal("Reward: 5M to party budget").withStyle(ChatFormatting.GREEN))
                .setCallback((index, type, containerInput, slotGui) -> {
                    PoliticalPartyManager.startPartyEvent(partyId, "mining_race");
                    open(player);
                })
                .build());

        gui.setSlot(14, new GuiElementBuilder(Items.GOLD_BLOCK)
                .setName(Component.literal("Donation Drive").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Compete for most coins donated").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Duration: 7 days").withStyle(ChatFormatting.DARK_GRAY))
                .addLoreLine(Component.literal("Reward: 5M to party budget").withStyle(ChatFormatting.GREEN))
                .setCallback((index, type, containerInput, slotGui) -> {
                    PoliticalPartyManager.startPartyEvent(partyId, "donation_drive");
                    open(player);
                })
                .build());

        gui.setSlot(16, new GuiElementBuilder(Items.BOW)
                .setName(Component.literal("PvP Tournament").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Compete for most PvP kills").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Duration: 7 days").withStyle(ChatFormatting.DARK_GRAY))
                .addLoreLine(Component.literal("Reward: 5M to party budget").withStyle(ChatFormatting.GREEN))
                .setCallback((index, type, containerInput, slotGui) -> {
                    PoliticalPartyManager.startPartyEvent(partyId, "pvp_tournament");
                    open(player);
                })
                .build());

        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((index, type, containerInput, slotGui) -> open(player))
                .build());

        gui.open();
    }

    // ════════════════════════════════════════════════════════════
    //  COUNCIL VOTE SUB-GUI
    // ════════════════════════════════════════════════════════════

    private static void openCouncilVote(ServerPlayer player, PoliticalPartyManager.PerkProposal proposal) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal("Council Vote: " + proposal.perkId).withStyle(ChatFormatting.LIGHT_PURPLE));

        fillPane(gui, 27, Items.PURPLE_STAINED_GLASS_PANE);

        gui.setSlot(11, new GuiElementBuilder(Items.LIME_CONCRETE)
                .setName(Component.literal("Approve").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Cost: " + fmt(proposal.costCoins) + " from budget").withStyle(ChatFormatting.GRAY))
                .setCallback((index, type, containerInput, slotGui) -> {
                    PoliticalPartyManager.councilVote(player, true);
                    open(player);
                })
                .build());

        gui.setSlot(15, new GuiElementBuilder(Items.RED_CONCRETE)
                .setName(Component.literal("Reject").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Deny this perk proposal").withStyle(ChatFormatting.GRAY))
                .setCallback((index, type, containerInput, slotGui) -> {
                    PoliticalPartyManager.councilVote(player, false);
                    open(player);
                })
                .build());

        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((index, type, containerInput, slotGui) -> open(player))
                .build());

        gui.open();
    }

    // ════════════════════════════════════════════════════════════
    //  MEMBER LIST SUB-GUI (9x6) with ranks + promote/demote
    // ════════════════════════════════════════════════════════════

    public static void openMemberList(ServerPlayer player, String partyId) {
        PoliticalPartyManager.Party party = PoliticalPartyManager.getParty(partyId);
        if (party == null) {
            player.sendSystemMessage(Component.literal("Party not found.").withStyle(ChatFormatting.RED));
            return;
        }
        String leaderUuid = PoliticalPartyManager.getPartyLeaderUuid(partyId);
        String viewerUuid = player.getUUID().toString();
        boolean viewerIsLeader = leaderUuid != null && leaderUuid.equals(viewerUuid);
        String viewerRank = PoliticalPartyManager.getPartyRank(partyId, viewerUuid);
        boolean canManage = viewerIsLeader || PoliticalPartyManager.rankIndex(viewerRank) <= 1;

        List<Map.Entry<String, PoliticalPartyManager.PartyMember>> sorted = party.members.entrySet().stream()
                .sorted(Comparator.<Map.Entry<String, PoliticalPartyManager.PartyMember>>comparingLong(e -> e.getValue().support).reversed())
                .collect(Collectors.toList());

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal(party.displayName + " — Members").withStyle(ChatFormatting.AQUA));

        fillPane(gui, 54, Items.GRAY_STAINED_GLASS_PANE);

        int slot = 0;
        for (Map.Entry<String, PoliticalPartyManager.PartyMember> entry : sorted) {
            if (slot >= 45) break;
            String memberUuid = entry.getKey();
            PoliticalPartyManager.PartyMember member = entry.getValue();
            String name = DataManager.getPlayerName(memberUuid);
            String rank = PoliticalPartyManager.getPartyRank(partyId, memberUuid);

            ChatFormatting rankColor = switch (rank) {
                case "Leader" -> ChatFormatting.GOLD;
                case "Vice Leader" -> ChatFormatting.YELLOW;
                case "General" -> ChatFormatting.RED;
                case "Officer" -> ChatFormatting.AQUA;
                case "Member" -> ChatFormatting.WHITE;
                default -> ChatFormatting.GRAY;
            };

            GuiElementBuilder item = new GuiElementBuilder(Items.PLAYER_HEAD)
                    .setName(Component.literal(name).withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(rank).withStyle(rankColor))
                    .addLoreLine(Component.literal("Support: " + fmt(member.support)).withStyle(ChatFormatting.AQUA));

            if (canManage && !memberUuid.equals(viewerUuid)) {
                item.addLoreLine(Component.literal(""));
                item.addLoreLine(Component.literal("Click to manage").withStyle(ChatFormatting.YELLOW));
                String mUuid = memberUuid;
                item.setCallback((index, type, containerInput, slotGui) -> openMemberManage(player, partyId, mUuid));
            }

            gui.setSlot(slot, item.build());
            slot++;
        }

        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((index, type, containerInput, slotGui) -> open(player))
                .build());

        gui.open();
    }

    // ════════════════════════════════════════════════════════════
    //  MEMBER MANAGE (promote/demote)
    // ════════════════════════════════════════════════════════════

    private static void openMemberManage(ServerPlayer player, String partyId, String targetUuid) {
        String name = DataManager.getPlayerName(targetUuid);
        String currentRank = PoliticalPartyManager.getPartyRank(partyId, targetUuid);
        String myRank = PoliticalPartyManager.getPartyRank(partyId, player.getUUID().toString());

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal("Manage: " + name).withStyle(ChatFormatting.AQUA));
        fillPane(gui, 27, Items.LIGHT_BLUE_STAINED_GLASS_PANE);

        gui.setSlot(4, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Component.literal(name).withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Rank: " + currentRank).withStyle(ChatFormatting.AQUA))
                .build());

        if (PoliticalPartyManager.canPromote(myRank, currentRank)) {
            int nextIdx = PoliticalPartyManager.RANK_ORDER.indexOf(currentRank) - 1;
            String nextRank = PoliticalPartyManager.RANK_ORDER.get(nextIdx);
            gui.setSlot(11, new GuiElementBuilder(Items.LIME_CONCRETE)
                    .setName(Component.literal("Promote").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(currentRank + " -> " + nextRank).withStyle(ChatFormatting.GRAY))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        PoliticalPartyManager.promotePartyMember(player, targetUuid);
                        openMemberList(player, partyId);
                    })
                    .build());
        } else {
            gui.setSlot(11, new GuiElementBuilder(Items.GRAY_DYE)
                    .setName(Component.literal("Promote").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("Cannot promote further").withStyle(ChatFormatting.DARK_GRAY))
                    .build());
        }

        if (PoliticalPartyManager.canDemote(myRank, currentRank)) {
            int nextIdx = PoliticalPartyManager.RANK_ORDER.indexOf(currentRank) + 1;
            String nextRank = PoliticalPartyManager.RANK_ORDER.get(nextIdx);
            gui.setSlot(15, new GuiElementBuilder(Items.RED_CONCRETE)
                    .setName(Component.literal("Demote").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(currentRank + " -> " + nextRank).withStyle(ChatFormatting.GRAY))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        PoliticalPartyManager.demotePartyMember(player, targetUuid);
                        openMemberList(player, partyId);
                    })
                    .build());
        } else {
            gui.setSlot(15, new GuiElementBuilder(Items.GRAY_DYE)
                    .setName(Component.literal("Demote").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("Cannot demote further").withStyle(ChatFormatting.DARK_GRAY))
                    .build());
        }

        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((index, type, containerInput, slotGui) -> openMemberList(player, partyId))
                .build());

        gui.open();
    }

    // ════════════════════════════════════════════════════════════
    //  DONATE SIGN GUI
    // ════════════════════════════════════════════════════════════

    public static void openDonateSign(ServerPlayer player) {
        SignGui signGui = new SignGui(player) {
            @Override
            public void onManualClose() {
                String input = this.getLine(0).getString().trim().replaceAll("[^0-9]", "");
                if (input.isEmpty()) {
                    PartyJoinGui.open(player);
                    return;
                }
                try {
                    long amount = Long.parseLong(input);
                    if (amount <= 0) {
                        player.sendSystemMessage(Component.literal("Amount must be positive.").withStyle(ChatFormatting.RED));
                    } else {
                        PoliticalPartyManager.donateCoins(player, amount);
                    }
                } catch (NumberFormatException e) {
                    player.sendSystemMessage(Component.literal("Invalid number.").withStyle(ChatFormatting.RED));
                }
                PartyJoinGui.open(player);
            }
        };
        signGui.setLine(0, Component.literal(""));
        signGui.setLine(1, Component.literal("Enter amount"));
        signGui.setLine(2, Component.literal("to donate"));
        signGui.open();
    }

    // ════════════════════════════════════════════════════════════
    //  PLATFORM SIGN GUI (leader only)
    // ════════════════════════════════════════════════════════════

    private static void openPlatformSign(ServerPlayer player) {
        SignGui signGui = new SignGui(player) {
            @Override
            public void onManualClose() {
                String l0 = this.getLine(0).getString().trim();
                String l1 = this.getLine(1).getString().trim();
                String combined = (l0 + " " + l1).trim();
                if (!combined.isEmpty()) {
                    PoliticalPartyManager.trySetPartyPlatform(player, combined);
                }
                PartyJoinGui.open(player);
            }
        };
        signGui.setLine(0, Component.literal(""));
        signGui.setLine(1, Component.literal(""));
        signGui.setLine(2, Component.literal("Write platform").withStyle(ChatFormatting.AQUA));
        signGui.setLine(3, Component.literal("(use lines 1-2)").withStyle(ChatFormatting.GRAY));
        signGui.open();
    }

    // ════════════════════════════════════════════════════════════
    //  LEAVE CONFIRMATION SUB-GUI
    // ════════════════════════════════════════════════════════════

    private static void openLeaveConfirmation(ServerPlayer player, String partyId) {
        PoliticalPartyManager.Party party = PoliticalPartyManager.getParty(partyId);
        String partyName = party != null ? party.displayName : partyId;

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal("Leave " + partyName + "?").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));

        fillPane(gui, 27, Items.RED_STAINED_GLASS_PANE);

        gui.setSlot(11, new GuiElementBuilder(Items.LIME_CONCRETE)
                .setName(Component.literal("Confirm Leave").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("You will lose your support rank.").withStyle(ChatFormatting.RED))
                .addLoreLine(Component.literal("24h cooldown to join another party.").withStyle(ChatFormatting.GRAY))
                .setCallback((index, type, containerInput, slotGui) -> {
                    PoliticalPartyManager.leaveParty(player);
                    player.closeContainer();
                })
                .build());

        gui.setSlot(15, new GuiElementBuilder(Items.RED_CONCRETE)
                .setName(Component.literal("Cancel").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .setCallback((index, type, containerInput, slotGui) -> open(player))
                .build());

        gui.open();
    }

    // ════════════════════════════════════════════════════════════
    //  PARTY BROWSER (9x6)
    // ════════════════════════════════════════════════════════════

    private static void openPartyBrowser(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("All Parties").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD));

        fillPane(gui, 54, Items.GRAY_STAINED_GLASS_PANE);

        String rulingId = PoliticalPartyManager.getRulingPartyId();
        int slot = 10;
        for (PoliticalPartyManager.Party p : PoliticalPartyManager.getParties()) {
            if (slot >= 44) break;
            if (slot == 17) slot = 19;
            if (slot == 26) slot = 28;
            if (slot == 35) slot = 37;

            String leaderUuid = PoliticalPartyManager.getPartyLeaderUuid(p.id);
            String leaderName = leaderUuid != null ? DataManager.getPlayerName(leaderUuid) : "None";
            boolean ruling = p.id.equals(rulingId);

            String platformExcerpt = (p.platform != null && !p.platform.isEmpty())
                    ? (p.platform.length() > 60 ? p.platform.substring(0, 57) + "..." : p.platform)
                    : "No platform";

            GuiElementBuilder item = new GuiElementBuilder(Items.PAPER)
                    .setName(Component.literal((ruling ? "\u2605 " : "") + p.displayName).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("Leader: " + leaderName).withStyle(ChatFormatting.YELLOW))
                    .addLoreLine(Component.literal("Members: " + p.members.size()).withStyle(ChatFormatting.WHITE))
                    .addLoreLine(Component.literal("Budget: " + fmt(p.budgetCoins)).withStyle(ChatFormatting.GREEN))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal(platformExcerpt).withStyle(ChatFormatting.GRAY));
            if (ruling) {
                item.addLoreLine(Component.literal(""));
                item.addLoreLine(Component.literal("\u2605 Ruling Party").withStyle(ChatFormatting.GOLD));
            }
            gui.setSlot(slot, item.build());
            slot++;
        }

        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((index, type, containerInput, slotGui) -> open(player))
                .build());

        gui.open();
    }

    // ════════════════════════════════════════════════════════════
    //  PARTY PICKER (9x6 — player not in a party)
    // ════════════════════════════════════════════════════════════

    private static void openPartyPicker(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Choose your party").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

        fillPane(gui, 54, Items.GRAY_STAINED_GLASS_PANE);

        String rulingId = PoliticalPartyManager.getRulingPartyId();

        int slot = 10;
        for (PoliticalPartyManager.Party p : PoliticalPartyManager.getParties()) {
            if (slot >= 44) break;
            if (slot == 17) slot = 19;
            if (slot == 26) slot = 28;
            if (slot == 35) slot = 37;

            String leaderUuid = PoliticalPartyManager.getPartyLeaderUuid(p.id);
            String leaderName = leaderUuid != null ? DataManager.getPlayerName(leaderUuid) : "None";
            boolean ruling = p.id.equals(rulingId);

            String platformExcerpt = (p.platform != null && !p.platform.isEmpty())
                    ? (p.platform.length() > 60 ? p.platform.substring(0, 57) + "..." : p.platform)
                    : "No platform";

            GuiElementBuilder item = new GuiElementBuilder(Items.PAPER)
                    .setName(Component.literal((ruling ? "\u2605 " : "") + p.displayName).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("Leader: " + leaderName).withStyle(ChatFormatting.YELLOW))
                    .addLoreLine(Component.literal("Members: " + p.members.size()).withStyle(ChatFormatting.WHITE))
                    .addLoreLine(Component.literal("Budget: " + fmt(p.budgetCoins)).withStyle(ChatFormatting.GREEN))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal(platformExcerpt).withStyle(ChatFormatting.GRAY));
            if (ruling) {
                item.addLoreLine(Component.literal(""));
                item.addLoreLine(Component.literal("\u2605 Ruling Party").withStyle(ChatFormatting.GOLD));
            }
            item.addLoreLine(Component.literal(""));
            item.addLoreLine(Component.literal("Click to join").withStyle(ChatFormatting.YELLOW));

            String pid = p.id;
            item.setCallback((index, type, containerInput, slotGui) -> {
                PoliticalPartyManager.tryJoinParty(player, pid);
                player.closeContainer();
            });

            gui.setSlot(slot, item.build());
            slot++;
        }

        gui.setSlot(4, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Component.literal("Create Party").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Found your own political party").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Cost: " + fmt(PoliticalPartyManager.CREATE_PARTY_CREDITS) + " credits").withStyle(ChatFormatting.RED))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to name your party").withStyle(ChatFormatting.YELLOW))
                .setCallback((index, type, containerInput, slotGui) -> openCreatePartySign(player))
                .build());

        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("Decide later").withStyle(ChatFormatting.RED))
                .addLoreLine(Component.literal("Use /pp join <party> when ready").withStyle(ChatFormatting.GRAY))
                .setCallback((index, type, containerInput, slotGui) -> {
                    PoliticalPartyManager.markOnboardingDone(player.getUUID().toString());
                    player.closeContainer();
                })
                .build());

        gui.open();
    }

    // ════════════════════════════════════════════════════════════
    //  CREATE PARTY SIGN GUI
    // ════════════════════════════════════════════════════════════

    private static void openCreatePartySign(ServerPlayer player) {
        SignGui signGui = new SignGui(player) {
            @Override
            public void onManualClose() {
                String name = this.getLine(0).getString().trim();
                if (name.isEmpty()) {
                    PartyJoinGui.open(player);
                    return;
                }
                PoliticalPartyManager.tryCreateParty(player, name);
                PartyJoinGui.open(player);
            }
        };
        signGui.setLine(0, Component.literal(""));
        signGui.setLine(1, Component.literal("Party name"));
        signGui.setLine(2, Component.literal("(2-32 chars)"));
        signGui.open();
    }

    // ════════════════════════════════════════════════════════════
    //  HELPERS
    // ════════════════════════════════════════════════════════════

    private static void fillPane(SimpleGui gui, int size, net.minecraft.world.item.Item pane) {
        for (int i = 0; i < size; i++) {
            gui.setSlot(i, new GuiElementBuilder(pane).setName(Component.literal("")).build());
        }
    }

    private static String fmt(long value) {
        return String.format("%,d", value);
    }

    private static List<String> wrapText(String text, int maxLen) {
        List<String> lines = new ArrayList<>();
        while (text.length() > maxLen) {
            int breakIdx = text.lastIndexOf(' ', maxLen);
            if (breakIdx <= 0) breakIdx = maxLen;
            lines.add(text.substring(0, breakIdx));
            text = text.substring(breakIdx).trim();
        }
        if (!text.isEmpty()) lines.add(text);
        return lines;
    }

    public static void openPartyCommissionsGui(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x4, player, false);
        gui.setTitle(Component.literal("Party Commissions").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE).setName(Component.literal("")).build());
        }

        String uuid = player.getUUID().toString();
        PoliticalPartyManager.PartyCommission active = PoliticalPartyManager.getActivePartyCommission(uuid);
        int progress = PoliticalPartyManager.getPartyCommissionProgress(uuid);

        gui.setSlot(4, new GuiElementBuilder(Items.EMERALD)
                .setName(Component.literal("⚜ Party Commissions").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Complete tasks to earn party support").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("One commission at a time").withStyle(ChatFormatting.DARK_GRAY))
                .glow().build());

        if (active != null) {
            gui.setSlot(31, new GuiElementBuilder(active.icon)
                    .setName(Component.literal("Active: " + active.displayName).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Progress: " + progress + "/" + active.target).withStyle(ChatFormatting.GREEN))
                    .addLoreLine(Component.literal("Reward: +" + active.supportReward + " support").withStyle(ChatFormatting.AQUA))
                    .glow().build());
        }

        PoliticalPartyManager.PartyCommission[] commissions = PoliticalPartyManager.PartyCommission.values();
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19};
        for (int i = 0; i < commissions.length && i < slots.length; i++) {
            PoliticalPartyManager.PartyCommission c = commissions[i];
            boolean isActive = active == c;
            boolean canStart = active == null && PoliticalPartyManager.canStartPartyCommission(uuid);

            ChatFormatting color = isActive ? ChatFormatting.GREEN : (canStart ? ChatFormatting.YELLOW : ChatFormatting.GRAY);

            GuiElementBuilder el = new GuiElementBuilder(c.icon)
                    .setName(Component.literal(c.displayName).withStyle(color, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal(c.description).withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("Reward: +" + c.supportReward + " support").withStyle(ChatFormatting.AQUA));

            if (isActive) {
                el.addLoreLine(Component.literal("Progress: " + progress + "/" + c.target).withStyle(ChatFormatting.GREEN));
                el.glow();
            } else if (canStart) {
                el.addLoreLine(Component.literal("Click to start").withStyle(ChatFormatting.GREEN));
                PoliticalPartyManager.PartyCommission fc = c;
                el.setCallback((idx, type, containerInput, slotGui) -> {
                    PoliticalPartyManager.startPartyCommission(player, fc);
                    openPartyCommissionsGui(player);
                });
            } else if (active != null) {
                el.addLoreLine(Component.literal("Complete current commission first").withStyle(ChatFormatting.RED));
            } else {
                el.addLoreLine(Component.literal("Join a party to start").withStyle(ChatFormatting.RED));
            }

            gui.setSlot(slots[i], el.build());
        }

        gui.setSlot(27, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("Close").withStyle(ChatFormatting.GRAY))
                .setCallback((idx, type, containerInput, slotGui) -> player.closeContainer())
                .build());

        gui.open();
    }
}
