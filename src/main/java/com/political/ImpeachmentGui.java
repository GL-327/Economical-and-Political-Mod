package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ImpeachmentGui {

    public static void open(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
        gui.setTitle(Text.literal("Impeachment Vote"));

        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        String chair = DataManager.getChair();
        String chairName = chair != null ? DataManager.getPlayerName(chair) : "Unknown";

        gui.setSlot(4, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Text.literal("Impeach " + chairName + "?").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal("Vote to remove the current Chair").formatted(Formatting.GRAY))
                .build());

        boolean hasVoted = ElectionManager.hasImpeachVoted(player.getUuidAsString());

        if (hasVoted) {
            gui.setSlot(11, new GuiElementBuilder(Items.GRAY_CONCRETE)
                    .setName(Text.literal("Already Voted").formatted(Formatting.GRAY))
                    .build());

            gui.setSlot(15, new GuiElementBuilder(Items.GRAY_CONCRETE)
                    .setName(Text.literal("Already Voted").formatted(Formatting.GRAY))
                    .build());
        } else {
            gui.setSlot(11, new GuiElementBuilder(Items.GREEN_CONCRETE)
                    .setName(Text.literal("YES - Remove Chair").formatted(Formatting.GREEN, Formatting.BOLD))
                    .addLoreLine(Text.literal("Vote to impeach the Chair").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Requires 2/3 majority").formatted(Formatting.YELLOW))
                    .addLoreLine(Text.literal("Minimum 5 voters required").formatted(Formatting.YELLOW))
                    .setCallback((index, type, action) -> {
                        ElectionManager.castImpeachVote(player, true);
                        player.closeHandledScreen();
                    })
                    .build());

            gui.setSlot(15, new GuiElementBuilder(Items.RED_CONCRETE)
                    .setName(Text.literal("NO - Keep Chair").formatted(Formatting.RED, Formatting.BOLD))
                    .addLoreLine(Text.literal("Vote against impeachment").formatted(Formatting.GRAY))
                    .setCallback((index, type, action) -> {
                        ElectionManager.castImpeachVote(player, false);
                        player.closeHandledScreen();
                    })
                    .build());
        }

        gui.open();
    }
}