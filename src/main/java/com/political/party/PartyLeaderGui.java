package com.political.party;

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

public final class PartyLeaderGui {

    public static void open(ServerPlayer player) {
        if (!PoliticalPartyManager.isPartyLeader(player)) {
            player.sendSystemMessage(Component.literal("You are not the leader of your party (highest support).").withStyle(ChatFormatting.RED));
            return;
        }
        String pid = PoliticalPartyManager.getPlayerPartyId(player.getUUID().toString());
        PoliticalPartyManager.Party p = pid != null ? PoliticalPartyManager.getParty(pid) : null;
        long budget = p != null ? p.budgetCoins : 0L;

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Party Leader").withStyle(ChatFormatting.GOLD));

        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.purple()).setName(Component.literal("")).build());
        }

        // --- Header ---
        gui.setSlot(4, new GuiElementBuilder(Items.GOLDEN_HELMET)
                .setName(Component.literal(p != null ? p.displayName : "Party").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Budget: " + String.format("%,d", budget)).withStyle(ChatFormatting.WHITE))
                .addLoreLine(Component.literal("Support (you): " + PoliticalPartyManager.getSupport(player.getUUID().toString())).withStyle(ChatFormatting.AQUA))
                .build());

        // ──── Row 2: Broadcast & Proposals ────

        gui.setSlot(10, new GuiElementBuilder(Items.OAK_SIGN)
                .setName(Component.literal("Nationwide Broadcast").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Cost: 2,500,000 from party pot").withStyle(ChatFormatting.RED))
                .addLoreLine(Component.literal("Short message to all players").withStyle(ChatFormatting.GRAY))
                .setCallback((index, type, containerInput, slotGui) -> openAdSign(player))
                .build());

        gui.setSlot(13, new GuiElementBuilder(Items.BOOK)
                .setName(Component.literal("National policy").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Chair and Vice Chair set perks and cuts.").withStyle(ChatFormatting.DARK_GRAY))
                .addLoreLine(Component.literal("Party budget is donations only.").withStyle(ChatFormatting.DARK_GRAY))
                .build());

        gui.setSlot(18, new GuiElementBuilder(Items.NAME_TAG)
                .setName(Component.literal("Affiliation").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("No party perk shop. Budget stays in the kitty.").withStyle(ChatFormatting.GRAY))
                .build());

        // ──── Row 4: Diplomacy ────

        gui.setSlot(27, new GuiElementBuilder(Items.IRON_SWORD)
                .setName(Component.literal("\u2694 Declare War").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Cost: 10,000,000 from budget").withStyle(ChatFormatting.RED))
                .addLoreLine(Component.literal("Click to choose target party").withStyle(ChatFormatting.YELLOW))
                .setCallback((index, type, containerInput, slotGui) -> openDeclareWarPicker(player, pid))
                .build());

        gui.setSlot(29, new GuiElementBuilder(Items.GOLDEN_APPLE)
                .setName(Component.literal("\u2764 Propose Alliance").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Free — target must accept").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Click to choose target party").withStyle(ChatFormatting.YELLOW))
                .setCallback((index, type, containerInput, slotGui) -> openAlliancePicker(player, pid))
                .build());

        // ──── Row 5: Member Management ────

        gui.setSlot(36, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Component.literal("Promote/Demote Members").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Manage member ranks").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Click to open member list").withStyle(ChatFormatting.YELLOW))
                .setCallback((index, type, containerInput, slotGui) -> PartyJoinGui.openMemberList(player, pid))
                .build());

        gui.setSlot(38, new GuiElementBuilder(Items.FIREWORK_ROCKET)
                .setName(Component.literal("Start Party Event").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Weekly challenge for members").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Reward: 5M to budget").withStyle(ChatFormatting.GREEN))
                .setCallback((index, type, containerInput, slotGui) -> {
                    gui.close();
                    PartyJoinGui.open(player);
                })
                .build());

        // --- Back / Close ---
        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("Back to Party Hub").withStyle(ChatFormatting.YELLOW))
                .setCallback((index, type, containerInput, slotGui) -> PartyJoinGui.open(player))
                .build());

        gui.setSlot(53, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("Close").withStyle(ChatFormatting.RED))
                .setCallback((index, type, containerInput, slotGui) -> player.closeContainer())
                .build());

        gui.open();
    }

    // ════════════════════════════════════════════════════════════
    //  DECLARE WAR PICKER
    // ════════════════════════════════════════════════════════════

    private static void openDeclareWarPicker(ServerPlayer player, String myPartyId) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x4, player, false);
        gui.setTitle(Component.literal("Declare War On...").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));

        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.red()).setName(Component.literal("")).build());
        }

        int slot = 10;
        for (PoliticalPartyManager.Party p : PoliticalPartyManager.getParties()) {
            if (slot >= 26) break;
            if (p.id.equals(myPartyId)) continue;
            if (slot == 17) slot = 19;

            boolean alreadyAtWar = PoliticalPartyManager.isAtWar(myPartyId, p.id);
            boolean isAlly = PoliticalPartyManager.isAllied(myPartyId, p.id);

            GuiElementBuilder item = new GuiElementBuilder(Items.BANNER.red())
                    .setName(Component.literal(p.displayName).withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("Members: " + p.members.size()).withStyle(ChatFormatting.GRAY));

            if (alreadyAtWar) {
                item.addLoreLine(Component.literal("Already at war!").withStyle(ChatFormatting.DARK_RED));
            } else if (isAlly) {
                item.addLoreLine(Component.literal("Allied — break alliance first").withStyle(ChatFormatting.AQUA));
            } else {
                item.addLoreLine(Component.literal("Cost: 10,000,000 coins").withStyle(ChatFormatting.RED));
                item.addLoreLine(Component.literal("Click to declare war").withStyle(ChatFormatting.YELLOW));
                String targetId = p.id;
                item.setCallback((index, type, containerInput, slotGui) -> {
                    PoliticalPartyManager.declareWar(player, targetId);
                    open(player);
                });
            }
            gui.setSlot(slot, item.build());
            slot++;
        }

        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((index, type, containerInput, slotGui) -> open(player))
                .build());

        gui.open();
    }

    // ════════════════════════════════════════════════════════════
    //  PROPOSE ALLIANCE PICKER
    // ════════════════════════════════════════════════════════════

    private static void openAlliancePicker(ServerPlayer player, String myPartyId) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x4, player, false);
        gui.setTitle(Component.literal("Propose Alliance To...").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));

        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.lightBlue()).setName(Component.literal("")).build());
        }

        int slot = 10;
        for (PoliticalPartyManager.Party p : PoliticalPartyManager.getParties()) {
            if (slot >= 26) break;
            if (p.id.equals(myPartyId)) continue;
            if (slot == 17) slot = 19;

            boolean isAlly = PoliticalPartyManager.isAllied(myPartyId, p.id);
            boolean atWar = PoliticalPartyManager.isAtWar(myPartyId, p.id);

            GuiElementBuilder item = new GuiElementBuilder(Items.BANNER.blue())
                    .setName(Component.literal(p.displayName).withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("Members: " + p.members.size()).withStyle(ChatFormatting.GRAY));

            if (isAlly) {
                item.addLoreLine(Component.literal("Already allied!").withStyle(ChatFormatting.GREEN));
            } else if (atWar) {
                item.addLoreLine(Component.literal("At war — end war first").withStyle(ChatFormatting.RED));
            } else {
                item.addLoreLine(Component.literal("Free — they must accept").withStyle(ChatFormatting.GRAY));
                item.addLoreLine(Component.literal("Click to propose").withStyle(ChatFormatting.YELLOW));
                String targetId = p.id;
                item.setCallback((index, type, containerInput, slotGui) -> {
                    PoliticalPartyManager.proposeAlliance(player, targetId);
                    open(player);
                });
            }
            gui.setSlot(slot, item.build());
            slot++;
        }

        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((index, type, containerInput, slotGui) -> open(player))
                .build());

        gui.open();
    }

    // ════════════════════════════════════════════════════════════
    //  AD SIGN
    // ════════════════════════════════════════════════════════════

    private static void openAdSign(ServerPlayer player) {
        SignGui signGui = new SignGui(player) {
            @Override
            public void onManualClose() {
                String msg = this.getLine(0).getString().trim();
                if (msg.length() > 120) {
                    msg = msg.substring(0, 120);
                }
                if (!msg.isEmpty()) {
                    PoliticalPartyManager.broadcastPartyAd(player, msg);
                }
                PartyLeaderGui.open(player);
            }
        };
        signGui.setLine(0, Component.literal(""));
        signGui.setLine(1, Component.literal("^^^^^^^^^^^^^^").withStyle(ChatFormatting.YELLOW));
        signGui.setLine(2, Component.literal("Party broadcast").withStyle(ChatFormatting.AQUA));
        signGui.setLine(3, Component.literal("(max 120 chars)").withStyle(ChatFormatting.GRAY));
        signGui.open();
    }
}
