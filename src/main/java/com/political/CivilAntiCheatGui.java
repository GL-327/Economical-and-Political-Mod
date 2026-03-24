package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/** Admin GUI toggles for CivilAC. */
public final class CivilAntiCheatGui {

    public static void open(ServerPlayerEntity admin) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, admin, false);
        gui.setTitle(Text.literal("Civil Anti-Cheat").formatted(Formatting.DARK_RED, Formatting.BOLD));

        var edge = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE).setName(Text.literal("")).build();
        for (int i = 0; i < 27; i++) gui.setSlot(i, edge);

        gui.setSlot(10, toggle(admin, Items.EXPERIENCE_BOTTLE, "Dupe scan (inventory tick)",
                CivilAntiCheatManager.isDupeScanEnabled(), () -> CivilAntiCheatManager.setDupeScanEnabled(!CivilAntiCheatManager.isDupeScanEnabled())));
        gui.setSlot(12, toggle(admin, Items.IRON_BARS, "Clamp illegal stack sizes",
                CivilAntiCheatManager.isClampIllegalStacksEnabled(), () -> CivilAntiCheatManager.setClampIllegalStacksEnabled(!CivilAntiCheatManager.isClampIllegalStacksEnabled())));
        gui.setSlot(14, toggle(admin, Items.TRIPWIRE_HOOK, "Notify operators on flags",
                CivilAntiCheatManager.isNotifyOperatorsEnabled(), () -> CivilAntiCheatManager.setNotifyOperatorsEnabled(!CivilAntiCheatManager.isNotifyOperatorsEnabled())));
        gui.setSlot(16, toggle(admin, Items.DIAMOND_PICKAXE, "Block instant-break hint (placeholder)",
                CivilAntiCheatManager.isBlockInstabreakEnabled(), () -> CivilAntiCheatManager.setBlockInstabreakEnabled(!CivilAntiCheatManager.isBlockInstabreakEnabled())));
        gui.setSlot(22, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("Close").formatted(Formatting.RED))
                .setCallback((a, b, c) -> admin.closeHandledScreen())
                .build());

        gui.open();
    }

    private static GuiElementBuilder toggle(ServerPlayerEntity admin, net.minecraft.item.Item icon, String label, boolean on, Runnable flip) {
        Formatting c = on ? Formatting.GREEN : Formatting.RED;
        String state = on ? "ON" : "OFF";
        return new GuiElementBuilder(icon)
                .setName(Text.literal(label).formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("State: " + state).formatted(c, Formatting.BOLD))
                .addLoreLine(Text.literal("Click to toggle").formatted(Formatting.GRAY))
                .setCallback((a, b, c2) -> {
                    flip.run();
                    open(admin);
                });
    }
}
