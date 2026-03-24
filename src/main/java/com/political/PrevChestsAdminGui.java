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

    public static void open(ServerPlayerEntity admin, ServerPlayerEntity watchedPlayer) {
        List<ChestAccessHistory.Entry> entries = ChestAccessHistory.copyFor(watchedPlayer.getUuid());
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, admin, false);
        gui.setTitle(Text.literal("Chests: ").append(watchedPlayer.getDisplayName()));

        var bg = new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE).setName(Text.literal("")).build();
        for (int i = 0; i < 54; i++) gui.setSlot(i, bg);

        int slot = 10;
        for (ChestAccessHistory.Entry e : entries) {
            if (slot >= 44) break;
            while (slot < 44 && (slot % 9 == 0 || slot % 9 == 8)) slot++;
            if (slot >= 44) break;
            final ChestAccessHistory.Entry fe = e;
            gui.setSlot(slot, new GuiElementBuilder(Items.CHEST)
                    .setName(Text.literal(e.blockId).formatted(Formatting.GOLD))
                    .addLoreLine(Text.literal(e.worldId).formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal(e.x + ", " + e.y + ", " + e.z).formatted(Formatting.AQUA))
                    .addLoreLine(Text.literal("Tick " + e.tick).formatted(Formatting.DARK_GRAY))
                    .addLoreLine(Text.literal("Open remote").formatted(Formatting.YELLOW))
                    .setCallback((a, b, c) -> RemoteChestAdminGui.open(admin, fe))
                    .build());
            slot++;
        }

        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("Close").formatted(Formatting.RED))
                .setCallback((a, b, c) -> admin.closeHandledScreen())
                .build());

        gui.open();
    }
}
