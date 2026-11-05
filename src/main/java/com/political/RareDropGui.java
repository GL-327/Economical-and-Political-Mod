package com.political;

import com.political.core.gui.ReturnTo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(RareDropGui.class);


    private RareDropGui() {}

    private static final int SIZE = 54;

    /** Where Back leads from this screen's first page: the menu that opened it, remembered per player. */
    private static final ReturnTo FROM = new ReturnTo();

    /** Opens the screen from a parent menu (Back returns there), or with {@code null} from a command (no Back). */
    public static void openFrom(ServerPlayer player, Runnable back) {
        FROM.set(player.getUUID(), back);
        open(player, 0);
    }

    public static void open(ServerPlayer player, int page) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Rare Drops")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        GuiKit.frame(gui, SIZE);

        double luck = luckPercent(player);
        GuiKit.header(gui, Items.NETHER_STAR, "Rare Drops", ChatFormatting.GOLD,
                RareDrop.values().length + " things that only fall off something",
                "None of these can be crafted",
                "Your luck: +" + String.format("%.0f", luck) + "%");

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
                "+" + String.format("%.0f", luck) + "% to every chance here",
                "",
                "Collection tiers, maxed collections and",
                "wearing the full warrant all add a little.",
                "",
                "It tops out under double on purpose. A drop",
                "you can grind to one in fifty is not rare."));

        GuiKit.footer(gui, SIZE, player, FROM.get(player.getUUID()));
        gui.open();
    }

    private static double luckPercent(ServerPlayer player) {
        double luck = 1.0;
        try {
            luck += Math.min(0.5, CollectionManager.totalTiers(player) / 1000.0);
            luck += Math.min(0.25, CollectionManager.maxedCount(player) * 0.025);
            if (WarrantArmour.fullSet(player)) luck += 0.2;
        } catch (Exception ignored) {
            LOGGER.debug("RareDropGui.luckPercent: ignored an error", ignored);
            // Same guard as the roll itself.
        }
        return (Math.min(1.95, luck) - 1.0) * 100;
    }
}
