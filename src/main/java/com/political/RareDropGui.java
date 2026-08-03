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
 * The drop table, published.
 *
 * <p>Hiding the odds would make these feel like a rumour rather than a goal. Every drop lists what
 * it falls off and the exact chance, so a player deciding whether to farm endermen for voidglass can
 * make that decision with the real number in front of them.</p>
 */
public final class RareDropGui {

    private RareDropGui() {}

    private static final int SIZE = 54;

    public static void open(ServerPlayer player, int page) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Rare Drops")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        GuiKit.frame(gui, SIZE);

        double luck = luckPercent(player);
        GuiKit.header(gui, Items.NETHER_STAR, "Rare Drops", ChatFormatting.GOLD,
                RareDrop.values().length + " things that only fall off something",
                "None of these can be crafted",
                "Magic Find: " + String.format("%.0f", luck) + " of "
                        + (int) RareDropManager.MAX_MAGIC_FIND);

        List<GuiElement> entries = new ArrayList<>();
        for (RareDrop d : RareDrop.values()) {
            GuiElementBuilder b = new GuiElementBuilder(d.base)
                    .setName(Component.literal(d.display)
                            .withStyle(d.colour, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§8" + d.flavor))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal(d.tier.colour + "" + ChatFormatting.BOLD
                            + d.tier.shout))
                    .addLoreLine(Component.literal("§7From §f" + RareDropManager.sourceName(d)))
                    .addLoreLine(Component.literal("§7Chance §f" + d.chanceText()))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§7" + d.use))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal(switch (d.tier.reach) {
                        case FINDER -> "§8Announced to you";
                        case NEARBY -> "§8Announced to anybody nearby";
                        case SERVER -> "§8Announced to the whole server";
                    }));
            if (d.tier == RareDrop.Tier.INSANE) b.glow();
            entries.add(b.build());
        }

        GuiKit.paint(gui, entries, page, GuiKit.LIST_SLOTS.length);
        GuiKit.pager(gui, SIZE, page, entries.size(), GuiKit.LIST_SLOTS.length,
                p -> open(player, p));

        gui.setSlot(SIZE - 5, GuiKit.info(Items.EXPERIENCE_BOTTLE, "Luck", ChatFormatting.AQUA,
                String.format("%.0f", luck) + " Magic Find, applied to every chance here",
                "",
                "Comes off gear lore like any other stat,",
                "plus collections and the full warrant.",
                "",
                "Capped at " + (int) RareDropManager.MAX_MAGIC_FIND + ". At the cap a one in a",
                "thousand drop becomes one in two hundred."));

        GuiKit.footer(gui, SIZE, player, null);
        gui.open();
    }

    private static double luckPercent(ServerPlayer player) {
        return RareDropManager.magicFindOf(player);
    }
}
