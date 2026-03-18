package com.political;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.Map;

public class VoteGui {

    public static void open(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
        gui.setTitle(Text.literal("Vote for Chair"));

        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        List<String> candidates = ElectionManager.getCandidates();
        Map<String, Integer> votes = ElectionManager.getVotes();
        String voterUuid = player.getUuidAsString();
        boolean hasVoted = ElectionManager.hasVoted(voterUuid);

        int[] slots = {10, 11, 12, 13, 14};

        for (int i = 0; i < candidates.size() && i < 5; i++) {
            String candidateUuid = candidates.get(i);
            String candidateName = DataManager.getPlayerName(candidateUuid);
            int voteCount = votes.getOrDefault(candidateUuid, 0);
            boolean isSelf = candidateUuid.equals(voterUuid);

            GuiElementBuilder builder;

            if (hasVoted) {
                builder = new GuiElementBuilder(Items.GRAY_CONCRETE)
                        .setName(Text.literal(candidateName).formatted(Formatting.GRAY))
                        .addLoreLine(Text.literal("Votes: " + voteCount).formatted(Formatting.WHITE))
                        .addLoreLine(Text.literal(""))
                        .addLoreLine(Text.literal("You have already voted!").formatted(Formatting.RED));
            } else if (isSelf) {
                builder = new GuiElementBuilder(Items.BARRIER)
                        .setName(Text.literal(candidateName).formatted(Formatting.RED))
                        .addLoreLine(Text.literal("Votes: " + voteCount).formatted(Formatting.WHITE))
                        .addLoreLine(Text.literal(""))
                        .addLoreLine(Text.literal("You cannot vote for yourself!").formatted(Formatting.RED));
            } else {
                builder = new GuiElementBuilder(Items.PLAYER_HEAD)
                        .setName(Text.literal(candidateName).formatted(Formatting.GREEN))
                        .addLoreLine(Text.literal("Votes: " + voteCount).formatted(Formatting.WHITE))
                        .addLoreLine(Text.literal(""))
                        .addLoreLine(Text.literal("Click to vote!").formatted(Formatting.YELLOW))
                        .setCallback((index, type, action) -> {
                            ElectionManager.castVote(player, candidateUuid);
                            player.closeHandledScreen();
                        });
            }

            gui.setSlot(slots[i], builder.build());
        }

        gui.setSlot(22, new GuiElementBuilder(Items.CLOCK)
                .setName(Text.literal("Time Remaining").formatted(Formatting.GOLD))
                .addLoreLine(Text.literal(PoliticalServer.formatTime(ElectionManager.getRemainingTime())).formatted(Formatting.WHITE))
                .build());

        // Buy Fake Vote button (only for candidates)
        boolean isCandidate = candidates.contains(voterUuid);
        if (isCandidate) {
            int fakeVoteCount = ElectionManager.getFakeVotes(voterUuid);
            boolean isCorrupt = ElectionManager.isCorrupt(voterUuid);
            GuiElementBuilder fakeVoteBtn = new GuiElementBuilder(Items.GOLD_NUGGET)
                    .setName(Text.literal("Buy Fake Vote").formatted(Formatting.GOLD, Formatting.BOLD))
                    .addLoreLine(Text.literal("Cost: 25 credits per vote").formatted(Formatting.YELLOW))
                    .addLoreLine(Text.literal("Fake votes: " + fakeVoteCount + "/5").formatted(Formatting.WHITE))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Adds 1 to your vote total.").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("5+ fake votes → Corrupt flag").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("  (1 fewer perk if elected)").formatted(Formatting.GRAY));
            if (isCorrupt) {
                fakeVoteBtn.addLoreLine(Text.literal("⚠ YOU ARE CORRUPT").formatted(Formatting.DARK_RED, Formatting.BOLD));
            } else {
                fakeVoteBtn.addLoreLine(Text.literal("Click to buy!").formatted(Formatting.GREEN));
                fakeVoteBtn.setCallback((index, type, action) -> {
                    ElectionManager.buyFakeVote(player);
                    player.closeHandledScreen();
                });
            }
            gui.setSlot(20, fakeVoteBtn.build());
        }

        gui.open();
    }
}