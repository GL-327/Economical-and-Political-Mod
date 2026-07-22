package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

/** Admin GUI toggles for CivilAC. */
public final class CivilAntiCheatGui {

    public static void open(ServerPlayer admin) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, admin, false);
        gui.setTitle(Component.literal("Civil Anti-Cheat").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));

        var edge = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE).setName(Component.literal("")).build();
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
                .setName(Component.literal("Close").withStyle(ChatFormatting.RED))
                .setCallback((a, b, c, __slotGui) -> admin.closeContainer())
                .build());

        gui.open();
    }

    private static GuiElementBuilder toggle(ServerPlayer admin, net.minecraft.world.item.Item icon, String label, boolean on, Runnable flip) {
        ChatFormatting c = on ? ChatFormatting.GREEN : ChatFormatting.RED;
        String state = on ? "ON" : "OFF";
        return new GuiElementBuilder(icon)
                .setName(Component.literal(label).withStyle(ChatFormatting.WHITE))
                .addLoreLine(Component.literal("State: " + state).withStyle(c, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Click to toggle").withStyle(ChatFormatting.GRAY))
                .setCallback((a, b, c2, __slotGui) -> {
                    flip.run();
                    open(admin);
                });
    }
}
