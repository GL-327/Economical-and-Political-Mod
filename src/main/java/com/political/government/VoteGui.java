package com.political.government;
import com.political.DataManager;
import com.political.PoliticalServer;


import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.List;
import java.util.Map;

public class VoteGui {

    public static void open(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal("Vote for Chair"));

        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.gray())
                    .setName(Component.literal(""))
                    .build());
        }

        List<String> candidates = ElectionManager.getCandidates();
        Map<String, Integer> votes = ElectionManager.getVotes();
        String voterUuid = player.getUUID().toString();
        boolean hasVoted = ElectionManager.hasVoted(voterUuid);

        int[] slots = {10, 11, 12, 13, 14};

        for (int i = 0; i < candidates.size() && i < 5; i++) {
            String candidateUuid = candidates.get(i);
            String candidateName = DataManager.getPlayerName(candidateUuid);
            int voteCount = votes.getOrDefault(candidateUuid, 0);
            boolean isSelf = candidateUuid.equals(voterUuid);

            GuiElementBuilder builder;

            if (hasVoted) {
                builder = new GuiElementBuilder(Items.CONCRETE.gray())
                        .setName(Component.literal(candidateName).withStyle(ChatFormatting.GRAY))
                        .addLoreLine(Component.literal("Votes: " + voteCount).withStyle(ChatFormatting.WHITE))
                        .addLoreLine(Component.literal(""))
                        .addLoreLine(Component.literal("You have already voted!").withStyle(ChatFormatting.RED));
            } else if (isSelf) {
                builder = new GuiElementBuilder(Items.BARRIER)
                        .setName(Component.literal(candidateName).withStyle(ChatFormatting.RED))
                        .addLoreLine(Component.literal("Votes: " + voteCount).withStyle(ChatFormatting.WHITE))
                        .addLoreLine(Component.literal(""))
                        .addLoreLine(Component.literal("You cannot vote for yourself!").withStyle(ChatFormatting.RED));
            } else {
                builder = new GuiElementBuilder(Items.PLAYER_HEAD)
                        .setName(Component.literal(candidateName).withStyle(ChatFormatting.GREEN))
                        .addLoreLine(Component.literal("Votes: " + voteCount).withStyle(ChatFormatting.WHITE))
                        .addLoreLine(Component.literal(""))
                        .addLoreLine(Component.literal("Click to vote!").withStyle(ChatFormatting.YELLOW))
                        .setCallback((index, type, containerInput, slotGui) -> {
                            ElectionManager.castVote(player, candidateUuid);
                            player.closeContainer();
                        });
            }

            gui.setSlot(slots[i], builder.build());
        }

        gui.setSlot(22, new GuiElementBuilder(Items.CLOCK)
                .setName(Component.literal("Time Remaining").withStyle(ChatFormatting.GOLD))
                .addLoreLine(Component.literal(PoliticalServer.formatTime(ElectionManager.getRemainingTime())).withStyle(ChatFormatting.WHITE))
                .build());

        // Buy Fake Vote button (only for candidates)
        boolean isCandidate = candidates.contains(voterUuid);
        if (isCandidate) {
            int fakeVoteCount = ElectionManager.getFakeVotes(voterUuid);
            boolean isCorrupt = ElectionManager.isCorrupt(voterUuid);
            GuiElementBuilder fakeVoteBtn = new GuiElementBuilder(Items.GOLD_NUGGET)
                    .setName(Component.literal("Buy Fake Vote").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("Cost: 25 credits per vote").withStyle(ChatFormatting.YELLOW))
                    .addLoreLine(Component.literal("Fake votes: " + fakeVoteCount + "/5").withStyle(ChatFormatting.WHITE))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Adds 1 to your vote total.").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("5+ fake votes → Corrupt flag").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("  (1 fewer perk if elected)").withStyle(ChatFormatting.GRAY));
            if (isCorrupt) {
                fakeVoteBtn.addLoreLine(Component.literal("⚠ YOU ARE CORRUPT").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
            } else {
                fakeVoteBtn.addLoreLine(Component.literal("Click to buy!").withStyle(ChatFormatting.GREEN));
                fakeVoteBtn.setCallback((index, type, containerInput, slotGui) -> {
                    ElectionManager.buyFakeVote(player);
                    player.closeContainer();
                });
            }
            gui.setSlot(20, fakeVoteBtn.build());
        }

        gui.open();
    }
}