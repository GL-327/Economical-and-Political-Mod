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
 * The collector's counter.
 *
 * <p>Shows everything a collector will buy and what they pay, whether the player is carrying any or
 * not. The empty entries matter as much as the full ones, because the price beside a drop nobody has
 * is the reason somebody goes looking for it.</p>
 */
public final class CollectorGui {

    private CollectorGui() {}

    private static final int SIZE = 54;

    public static void open(ServerPlayer player) {
        open(player, 0);
    }

    public static void open(ServerPlayer player, int page) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Collector")
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        GuiKit.frame(gui, SIZE);

        long carried = 0;
        int kinds = 0;
        for (RareDrop d : RareDrop.values()) {
            int have = CollectorManager.held(player, d);
            if (have > 0) {
                kinds++;
                carried += CollectorManager.priceOf(d) * have;
            }
        }

        GuiKit.header(gui, Items.EMERALD, "Collector", ChatFormatting.LIGHT_PURPLE,
                "Buys what the market cannot price",
                kinds + " kinds on you, worth " + GuiKit.num(carried) + " coins",
                "Prices are fixed and published");

        // Carried first, then everything else, so the actionable rows are at the top.
        List<RareDrop> ordered = new ArrayList<>();
        for (RareDrop d : RareDrop.values()) {
            if (CollectorManager.held(player, d) > 0) ordered.add(d);
        }
        for (RareDrop d : RareDrop.values()) {
            if (CollectorManager.held(player, d) <= 0) ordered.add(d);
        }

        List<GuiElement> entries = new ArrayList<>();
        for (RareDrop d : ordered) {
            int have = CollectorManager.held(player, d);
            long each = CollectorManager.priceOf(d);
            boolean only = CollectorManager.collectorOnly(d);

            GuiElementBuilder b = new GuiElementBuilder(have > 0 ? d.base : Items.GRAY_DYE)
                    .setName(Component.literal(d.display)
                            .withStyle(have > 0 ? d.colour : ChatFormatting.DARK_GRAY,
                                    ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal(d.tier.colour + "" + d.tier.shout))
                    .addLoreLine(Component.literal("§7Pays §6" + GuiKit.num(each) + " §7each"))
                    .addLoreLine(Component.literal("§7You carry §f" + have))
                    .addLoreLine(Component.literal(""));

            if (only) {
                b.addLoreLine(Component.literal("§dNobody else will buy this."));
                b.addLoreLine(Component.literal("§8Too rare for an order book to match."));
            } else {
                b.addLoreLine(Component.literal("§8The bazaar may pay more. Check first."));
            }

            b.addLoreLine(Component.literal(""));
            if (have > 0) {
                b.addLoreLine(Component.literal("§aClick to sell all " + have
                        + " for §6" + GuiKit.num(each * have)));
                b.glow();
            } else {
                b.addLoreLine(Component.literal("§8From " + RareDropManager.sourceName(d)
                        + ", at " + d.chanceText()));
            }

            entries.add(b.setCallback((a, t, ci, sg) -> {
                if (CollectorManager.held(player, d) <= 0) return;
                GuiKit.confirm(player, "Sell " + CollectorManager.held(player, d) + " "
                                + d.display,
                        "They are taken and paid for at "
                                + GuiKit.num(each) + " coins each.",
                        () -> {
                            CollectorManager.sellAll(player, d);
                            open(player, page);
                        },
                        () -> open(player, page));
            }).build());
        }

        GuiKit.paint(gui, entries, page, GuiKit.LIST_SLOTS.length);
        GuiKit.pager(gui, SIZE, page, entries.size(), GuiKit.LIST_SLOTS.length,
                p -> open(player, p));

        gui.setSlot(SIZE - 5, GuiKit.info(Items.WRITABLE_BOOK, "Why a person and not the market",
                ChatFormatting.WHITE,
                "A bazaar needs somebody on the other side.",
                "",
                "At the top of the drop table there is nobody.",
                "A primal drop turns up once in a thousand,",
                "so listing one is an advert, not a sale.",
                "",
                "A collector pays a fixed price instead, so the",
                "rarest things are still worth something on a",
                "quiet server."));

        GuiKit.footer(gui, SIZE, player, null);
        gui.open();
    }
}
