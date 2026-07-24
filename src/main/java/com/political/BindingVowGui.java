package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

/** Accept or renounce binding vows: opt-in tradeoffs that reshape how you fight. */
public final class BindingVowGui {

    private BindingVowGui() {}

    private static final int[] SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};

    public static void open(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Binding Vows").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        WarGui.fill(gui, 54);

        SorcererManager.PlayerSorcerer p = SorcererManager.getOrNull(uuid);

        gui.setSlot(4, new GuiElementBuilder(Items.LEAD)
                .setName(Component.literal("Binding Vows").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7A vow is a promise the world enforces."))
                .addLoreLine(Component.literal("§7Give something up, gain something back."))
                .glow().build());

        BindingVow[] vows = BindingVow.values();
        for (int i = 0; i < vows.length && i < SLOTS.length; i++) {
            BindingVow v = vows[i];
            boolean held = p != null && SorcererManager.hasVow(p, v);

            GuiElementBuilder b = new GuiElementBuilder(held ? Items.LIME_CONCRETE : Items.YELLOW_CONCRETE)
                    .setName(Component.literal(v.display).withStyle(v.color, ChatFormatting.BOLD));
            for (String up : v.upsides) b.addLoreLine(Component.literal("§a+ §7" + up));
            for (String down : v.downsides) b.addLoreLine(Component.literal("§c- §7" + down));
            b.addLoreLine(Component.literal(""));
            b.addLoreLine(Component.literal(held ? "§a§lSWORN §7- click to renounce" : "§eClick to swear this vow"));
            if (held) b.glow();
            b.setCallback((a, c, d, g) -> {
                SorcererManager.toggleVow(player, v);
                open(player);
            });
            gui.setSlot(SLOTS[i], b.build());
        }

        gui.setSlot(49, WarGui.back(() -> SorcererGui.open(player)));
        gui.open();
    }
}
