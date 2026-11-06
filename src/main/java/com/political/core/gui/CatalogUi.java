package com.political.core.gui;

import com.political.core.text.Text;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SignGui;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Shared server-only catalog shell: paired category cards, breathing room, search and fixed navigation. */
public final class CatalogUi {
    public record Tile(Item icon, Component label, int count, Runnable open) {
        public Tile {
            if (count < 0) throw new IllegalArgumentException("negative item count");
        }
    }

    private CatalogUi() {}

    public static Screen categories(ServerPlayer player, Component title, Component hint, List<Tile> tiles,
                                    int page, IntConsumer openPage, Runnable search, Runnable back) {
        Screen screen = Screen.chest(player, 6, title).frame(Screen.Edge.INFO)
                .header(Items.KNOWLEDGE_BOOK, title, Text.tr("political.catalog.categories", tiles.size()), hint)
                .footer(back);
        int current = Pager.clamp(page, Pager.pageCount(tiles.size(), CatalogModel.CARDS_PER_PAGE));
        int start = current * CatalogModel.CARDS_PER_PAGE;
        for (int i = 0; i < CatalogModel.CARDS_PER_PAGE && start + i < tiles.size(); i++) {
            Tile tile = tiles.get(start + i);
            CatalogModel.Card card = CatalogModel.card(i, tile.count());
            Component count = Text.tr("political.catalog.items", tile.count());
            screen.set(card.iconSlot(), Elements.tile(tile.icon(), tile.label(), Palette.ACCENT, tile.open(), count));
            // A nameplate belongs to the icon above it and opens the same category. Its lore gives the exact count.
            screen.set(card.labelSlot(), new GuiElementBuilder(Items.PAPER).setCount(card.badgeCount())
                    .setName(Elements.name(tile.label(), Palette.BODY))
                    .addLoreLine(Elements.lore(count))
                    .addLoreLine(Elements.lore(Text.tr("political.gui.click_to_open"), Palette.WARN))
                    .setCallback((a, b, c, d) -> tile.open().run()).build());
        }
        screen.pager(tiles.size(), CatalogModel.CARDS_PER_PAGE, current, openPage);
        search(screen, search);
        return screen;
    }

    public static Screen items(ServerPlayer player, Component title, Item icon, Component hint, int count,
                               int page, IntConsumer openPage, Runnable search, Runnable back) {
        Screen screen = Screen.chest(player, 6, title).frame(Screen.Edge.INFO)
                .header(icon, title, Text.tr("political.catalog.items", count), hint).footer(back);
        screen.pager(count, CatalogModel.ITEMS_PER_PAGE, page, openPage);
        if (count == 0) screen.set(22, Elements.info(Items.GLASS_BOTTLE,
                Text.tr("political.catalog.empty"), Palette.MUTED, Text.tr("political.catalog.try_search")));
        search(screen, search);
        return screen;
    }

    private static void search(Screen screen, Runnable search) {
        if (search != null) screen.set(47, Elements.tile(Items.SPYGLASS, Text.tr("political.catalog.search"),
                Palette.ACCENT, search, Text.tr("political.catalog.search_hint")));
    }

    public static GuiElement item(ItemStack item, Component prompt, Runnable click) {
        return new GuiElementBuilder(item.copy())
                .setName(Elements.name(item.getHoverName(), Palette.BODY))
                .addLoreLine(Component.empty()).addLoreLine(Elements.lore(prompt, Palette.WARN))
                .setCallback((a, b, c, d) -> click.run()).build();
    }

    public static void search(ServerPlayer player, Consumer<String> results, Runnable cancel) {
        SignGui sign = new SignGui(player) {
            @Override public void onManualClose() {
                String query = getLine(0).getString().strip();
                if (query.isEmpty()) cancel.run(); else results.accept(query);
            }
        };
        sign.setLine(0, Text.plain(""));
        sign.setLine(1, Text.plain("^^^^^^^^^^^^^^"));
        sign.setLine(2, Text.tr("political.catalog.search"));
        sign.setLine(3, Text.tr("political.catalog.partial"));
        sign.open();
    }
}
