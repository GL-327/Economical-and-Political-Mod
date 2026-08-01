package com.political;

import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * The titles screen.
 *
 * <p>Shows every title in the mod rather than only the earned ones, because the unearned entries are
 * the interesting half: each one names something worth doing and says exactly what it takes. A list
 * of things you already have is a trophy cabinet, a list including what you have not is a map.</p>
 */
public final class TitleGui {

    private TitleGui() {}

    private static final int SIZE = 54;

    public static void open(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Titles")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        GuiKit.frame(gui, SIZE);

        List<Title> earned = TitleManager.earnedBy(player);
        Title worn = TitleManager.wornBy(player);

        GuiKit.header(gui, Items.NAME_TAG, "Titles", ChatFormatting.GOLD,
                earned.size() + " of " + Title.values().length + " earned",
                worn == null ? "Wearing none" : "Wearing " + worn.display,
                "Shown beside your name");

        List<GuiElement> entries = new ArrayList<>();
        for (Title t : Title.values()) {
            boolean has = t.earnedBy(player);
            boolean on = TitleManager.isWorn(player, t);

            GuiElementBuilder b = new GuiElementBuilder(
                    on ? Items.WRITTEN_BOOK : has ? Items.NAME_TAG : Items.GRAY_DYE)
                    .setName(Component.literal(t.display).withStyle(
                            has ? t.colour : ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§8" + t.kind.colour + t.kind.label))
                    .addLoreLine(Component.literal("§7" + t.requirement))
                    .addLoreLine(Component.literal(""));

            if (on) {
                b.addLoreLine(Component.literal("§a✔ Currently worn"));
                b.addLoreLine(Component.literal("§eClick to take it off"));
                b.glow();
            } else if (has) {
                b.addLoreLine(Component.literal("§aEarned"));
                b.addLoreLine(Component.literal("§eClick to wear it"));
            } else {
                b.addLoreLine(Component.literal("§8Not earned yet"));
            }

            entries.add(b.setCallback((a, ct, ci, sg) -> {
                if (on) TitleManager.wear(player, null);
                else TitleManager.wear(player, t);
                open(player);
            }).build());
        }

        GuiKit.paint(gui, entries, 0, GuiKit.LIST_SLOTS.length);
        GuiKit.footer(gui, SIZE, player, null);
        gui.open();
    }
}
