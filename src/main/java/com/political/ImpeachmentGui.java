package com.political;


import com.political.government.ElectionManager;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

public class ImpeachmentGui {

    /** Where Back leads from this screen: the menu that opened it, remembered per player. */
    private static final com.political.core.gui.ReturnTo FROM = new com.political.core.gui.ReturnTo();

    /** Opens the screen from a parent menu (Back returns there), or with {@code null} from a command or the hub. */
    public static void openFrom(ServerPlayer player, Runnable back) {
        FROM.set(player.getUUID(), back);
        open(player);
    }

    public static void open(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal("Impeachment Vote"));

        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.gray())
                    .setName(Component.literal(""))
                    .build());
        }

        String chair = DataManager.getChair();
        String chairName = chair != null ? DataManager.getPlayerName(chair) : "Unknown";

        gui.setSlot(4, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Component.literal("Impeach " + chairName + "?").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Vote to remove the current Chair").withStyle(ChatFormatting.GRAY))
                .build());

        boolean hasVoted = ElectionManager.hasImpeachVoted(player.getUUID().toString());

        if (hasVoted) {
            gui.setSlot(11, new GuiElementBuilder(Items.CONCRETE.gray())
                    .setName(Component.literal("Already Voted").withStyle(ChatFormatting.GRAY))
                    .build());

            gui.setSlot(15, new GuiElementBuilder(Items.CONCRETE.gray())
                    .setName(Component.literal("Already Voted").withStyle(ChatFormatting.GRAY))
                    .build());
        } else {
            gui.setSlot(11, new GuiElementBuilder(Items.CONCRETE.green())
                    .setName(Component.literal("YES - Remove Chair").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("Vote to impeach the Chair").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Requires 2/3 majority").withStyle(ChatFormatting.YELLOW))
                    .addLoreLine(Component.literal("Minimum 5 voters required").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        ElectionManager.castImpeachVote(player, true);
                        player.closeContainer();
                    })
                    .build());

            gui.setSlot(15, new GuiElementBuilder(Items.CONCRETE.red())
                    .setName(Component.literal("NO - Keep Chair").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("Vote against impeachment").withStyle(ChatFormatting.GRAY))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        ElectionManager.castImpeachVote(player, false);
                        player.closeContainer();
                    })
                    .build());
        }

        com.political.core.gui.BackSlot.add(gui, FROM.get(player.getUUID()));
        gui.open();
    }
}