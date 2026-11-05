package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.List;

public final class PrevChestsAdminGui {

    private static final int PER_PAGE = 21;

    /** Where Back leads from the first page: the menu that opened it, remembered per admin. */
    private static final com.political.core.gui.ReturnTo FROM = new com.political.core.gui.ReturnTo();

    /** Opened from a command or from the invsee screen: Back goes to the invsee screen. */
    public static void open(ServerPlayer admin, ServerPlayer watchedPlayer) {
        FROM.set(admin.getUUID(), null);
        open(admin, watchedPlayer, 0);
    }

    /** Opened from a parent menu: Back returns there. */
    public static void openFrom(ServerPlayer admin, ServerPlayer watchedPlayer, Runnable back) {
        FROM.set(admin.getUUID(), back);
        open(admin, watchedPlayer, 0);
    }

    private static void open(ServerPlayer admin, ServerPlayer watchedPlayer, int page) {
        List<ChestAccessHistory.Entry> entries = ChestAccessHistory.copyFor(watchedPlayer.getUUID());
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, admin, false);
        int totalPages = Math.max(1, (entries.size() + PER_PAGE - 1) / PER_PAGE);
        int p = Math.min(Math.max(0, page), totalPages - 1);
        gui.setTitle(Component.literal("Chests (page " + (p + 1) + "/" + totalPages + "): ")
                .withStyle(ChatFormatting.GOLD).append(watchedPlayer.getDisplayName()));

        var bg = new GuiElementBuilder(Items.STAINED_GLASS_PANE.black()).setName(Component.literal("")).build();
        for (int i = 0; i < 54; i++) gui.setSlot(i, bg);

        gui.setSlot(4, new GuiElementBuilder(Items.SPYGLASS)
                .setName(Component.literal("Recent Containers").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Target: " + watchedPlayer.getName().getString()).withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Entries: " + entries.size() + " (max " + ChestAccessHistory.MAX_ENTRIES + ")")
                        .withStyle(ChatFormatting.DARK_GRAY))
                .build());

        int start = p * PER_PAGE;
        int slot = 10;
        for (int i = start; i < entries.size() && slot < 44; i++) {
            while (slot < 44 && (slot % 9 == 0 || slot % 9 == 8)) slot++;
            if (slot >= 44) break;
            ChestAccessHistory.Entry e = entries.get(i);
            final ChestAccessHistory.Entry fe = e;
            gui.setSlot(slot, new GuiElementBuilder(Items.CHEST)
                    .setName(Component.literal(e.blockId).withStyle(ChatFormatting.GOLD))
                    .addLoreLine(Component.literal(e.worldId).withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(e.x + ", " + e.y + ", " + e.z).withStyle(ChatFormatting.AQUA))
                    .addLoreLine(Component.literal("Tick " + e.tick).withStyle(ChatFormatting.DARK_GRAY))
                    .addLoreLine(Component.literal("Open remote").withStyle(ChatFormatting.YELLOW))
                    .setCallback((a, b, c, __slotGui) -> RemoteChestAdminGui.open(admin, watchedPlayer, fe))
                    .build());
            slot++;
        }

        if (p > 0) {
            gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("Previous page").withStyle(ChatFormatting.YELLOW))
                    .setCallback((a, b, c, __slotGui) -> open(admin, watchedPlayer, p - 1))
                    .build());
        }
        if (p < totalPages - 1) {
            gui.setSlot(53, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("Next page").withStyle(ChatFormatting.YELLOW))
                    .setCallback((a, b, c, __slotGui) -> open(admin, watchedPlayer, p + 1))
                    .build());
        }

        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("Close").withStyle(ChatFormatting.RED))
                .setCallback((a, b, c, __slotGui) -> admin.closeContainer())
                .build());
        Runnable parent = FROM.get(admin.getUUID());
        gui.setSlot(48, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal(parent != null ? "← Back" : "Back to Invsee").withStyle(ChatFormatting.YELLOW))
                .setCallback((a, b, c, __slotGui) -> {
                    if (parent != null) {
                        parent.run();
                    } else {
                        AdminInvseeGui.openMainMenu(admin, watchedPlayer);
                    }
                })
                .build());

        gui.open();
    }
}
