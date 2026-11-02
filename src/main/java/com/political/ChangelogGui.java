package com.political;


import com.political.settlement.WarGui;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

import java.util.List;

/** Version picker. Click a version to receive its change list in chat. */
public final class ChangelogGui {

    private ChangelogGui() {}

    private static final int[] SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
    };

    public static void open(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Changelog").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        WarGui.fill(gui, 54);

        gui.setSlot(4, new GuiElementBuilder(Items.WRITTEN_BOOK)
                .setName(Component.literal("Version History").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Click a version to read its changes in chat."))
                .addLoreLine(Component.literal("§7Each line has a §b[dig deeper]§7 prompt."))
                .glow().build());

        List<ChangelogManager.Version> versions = ChangelogManager.versions();
        for (int i = 0; i < versions.size() && i < SLOTS.length; i++) {
            ChangelogManager.Version v = versions.get(i);
            gui.setSlot(SLOTS[i], new GuiElementBuilder(v.icon())
                    .setName(Component.literal(v.title()).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7" + v.tagline()))
                    .addLoreLine(Component.literal("§8" + v.entries().size() + " changes"))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§eClick to read in chat"))
                    .setCallback((index, clickType, action, slotGui) -> {
                        player.closeContainer();
                        ChangelogManager.printVersion(player, v.id());
                    })
                    .build());
        }

        gui.setSlot(49, WarGui.back(() -> CivilizationHubGui.open(player)));
        gui.open();
    }
}
