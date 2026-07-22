package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

/**
 * Vote for which party becomes the ruling party (national ballot during elections).
 */
public final class PartyLeaderElectionGui {

    public static void open(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x4, player, false);
        gui.setTitle(Component.literal("Ruling party vote").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE).setName(Component.literal("")).build());
        }

        gui.setSlot(4, new GuiElementBuilder(Items.LECTERN)
                .setName(Component.literal("National ballot").withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal("Vote for the ruling party.").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Their leader may fund programs from the party pot.").withStyle(ChatFormatting.GRAY))
                .build());

        int slot = 10;
        for (PoliticalPartyManager.Party p : PoliticalPartyManager.getParties()) {
            if (slot >= 35) {
                break;
            }
            if (slot == 17) {
                slot = 19;
            }
            if (slot == 26) {
                slot = 28;
            }
            String leaderUuid = PoliticalPartyManager.getPartyLeaderUuid(p.id);
            String leaderName = leaderUuid != null ? DataManager.getPlayerName(leaderUuid) : "— (no members)";
            String v = PoliticalPartyManager.getLeaderElectionVotes().get(player.getUUID().toString());
            boolean votedHere = p.id.equals(v);
            gui.setSlot(slot, new GuiElementBuilder(votedHere ? Items.LIME_DYE : Items.WHITE_BANNER)
                    .setName(Component.literal(p.displayName).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("Party leader candidate: " + leaderName).withStyle(ChatFormatting.WHITE))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal(votedHere ? "✓ Your current vote" : "Click to vote").withStyle(votedHere ? ChatFormatting.GREEN : ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        PoliticalPartyManager.castLeaderVote(player, p.id);
                        open(player);
                    })
                    .build());
            slot++;
        }

        gui.setSlot(31, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("Close").withStyle(ChatFormatting.RED))
                .setCallback((index, type, containerInput, slotGui) -> player.closeContainer())
                .build());

        gui.open();
    }
}
