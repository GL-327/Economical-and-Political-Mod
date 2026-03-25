package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public final class PrevChestsAdminGui {

    private static final int PER_PAGE = 21;

    public static void open(ServerPlayerEntity admin, ServerPlayerEntity watchedPlayer) {
        open(admin, watchedPlayer, 0);
    }

    private static void open(ServerPlayerEntity admin, ServerPlayerEntity watchedPlayer, int page) {
        List<ChestAccessHistory.Entry> entries = ChestAccessHistory.copyFor(watchedPlayer.getUuid());
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, admin, false);
        int totalPages = Math.max(1, (entries.size() + PER_PAGE - 1) / PER_PAGE);
        int p = Math.min(Math.max(0, page), totalPages - 1);
        gui.setTitle(Text.literal("Chests (page " + (p + 1) + "/" + totalPages + "): ")
                .formatted(Formatting.GOLD).append(watchedPlayer.getDisplayName()));

        var bg = new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE).setName(Text.literal("")).build();
        for (int i = 0; i < 54; i++) gui.setSlot(i, bg);

        gui.setSlot(4, new GuiElementBuilder(Items.SPYGLASS)
                .setName(Text.literal("Recent Containers").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal("Target: " + watchedPlayer.getName().getString()).formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Entries: " + entries.size() + " (max " + ChestAccessHistory.MAX_ENTRIES + ")")
                        .formatted(Formatting.DARK_GRAY))
                .build());

        int start = p * PER_PAGE;
        int slot = 10;
        for (int i = start; i < entries.size() && slot < 44; i++) {
            while (slot < 44 && (slot % 9 == 0 || slot % 9 == 8)) slot++;
            if (slot >= 44) break;
            ChestAccessHistory.Entry e = entries.get(i);
            final ChestAccessHistory.Entry fe = e;
            gui.setSlot(slot, new GuiElementBuilder(Items.CHEST)
                    .setName(Text.literal(e.blockId).formatted(Formatting.GOLD))
                    .addLoreLine(Text.literal(e.worldId).formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal(e.x + ", " + e.y + ", " + e.z).formatted(Formatting.AQUA))
                    .addLoreLine(Text.literal("Tick " + e.tick).formatted(Formatting.DARK_GRAY))
                    .addLoreLine(Text.literal("Open remote").formatted(Formatting.YELLOW))
                    .setCallback((a, b, c) -> RemoteChestAdminGui.open(admin, watchedPlayer, fe))
                    .build());
            slot++;
        }

        if (p > 0) {
            gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("Previous page").formatted(Formatting.YELLOW))
                    .setCallback((a, b, c) -> open(admin, watchedPlayer, p - 1))
                    .build());
        }
        if (p < totalPages - 1) {
            gui.setSlot(53, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("Next page").formatted(Formatting.YELLOW))
                    .setCallback((a, b, c) -> open(admin, watchedPlayer, p + 1))
                    .build());
        }

        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("Close").formatted(Formatting.RED))
                .setCallback((a, b, c) -> admin.closeHandledScreen())
                .build());
        gui.setSlot(48, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("Back to Invsee").formatted(Formatting.YELLOW))
                .setCallback((a, b, c) -> AdminInvseeGui.openMainMenu(admin, watchedPlayer))
                .build());

        gui.open();
    }
}
