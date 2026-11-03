package com.political.core.gui;

import com.political.core.text.Text;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import java.util.List;
import java.util.function.IntConsumer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;

/**
 * A chest screen built from the layout module's pieces (STYLE.md): standard frame, header, footer with
 * back and close, a paged list and a confirmation dialog. Every screen is laid out by {@link Layout}, so a
 * player who has learned one has learned them all.
 *
 * <pre>{@code
 * Screen.chest(player, 6, title)
 *       .frame(Edge.NORMAL)
 *       .header(Icon.PAGE.item(), title, subtitle)
 *       .list(entries, page, p -> open(player, p))
 *       .footer(() -> HubScreen.open(player))
 *       .open();
 * }</pre>
 */
public final class Screen {

    /** Which edge color the frame gets. */
    public enum Edge {
        NORMAL(Icon.FRAME_EDGE), INFO(Icon.FRAME_ACCENT), DANGER(Icon.FRAME_DANGER);

        private final Icon icon;

        Edge(Icon icon) {
            this.icon = icon;
        }
    }

    private static final MenuType<?>[] MENUS = {
            MenuType.GENERIC_9x1, MenuType.GENERIC_9x2, MenuType.GENERIC_9x3,
            MenuType.GENERIC_9x4, MenuType.GENERIC_9x5, MenuType.GENERIC_9x6
    };

    private final ServerPlayer player;
    private final SimpleGui gui;
    private final int size;

    private Screen(ServerPlayer player, int rows, Component title) {
        this.player = player;
        this.size = Layout.size(rows);
        this.gui = new SimpleGui(MENUS[rows - 1], player, false);
        this.gui.setTitle(Elements.name(title, Palette.TITLE));
    }

    public static Screen chest(ServerPlayer player, int rows, Component title) {
        return new Screen(player, rows, title);
    }

    public SimpleGui gui() {
        return gui;
    }

    public int size() {
        return size;
    }

    private static GuiElement blank(Item item) {
        return new GuiElementBuilder(item).setName(Text.plain(" ")).build();
    }

    /** The two tone frame: an edge ring in the chosen color around a dark interior. */
    public Screen frame(Edge edge) {
        Item edgeItem = edge.icon.item();
        Item inner = Icon.FRAME_DARK.item();
        for (int slot = 0; slot < size; slot++) {
            gui.setSlot(slot, blank(Layout.isEdge(slot, size) ? edgeItem : inner));
        }
        return this;
    }

    /** The title icon in the middle of the top row, with optional subtitle lines under the name. */
    public Screen header(Item icon, Component name, Component... subtitle) {
        GuiElementBuilder b = new GuiElementBuilder(icon).setName(Elements.name(name, Palette.TITLE)).glow();
        if (subtitle.length > 0) {
            b.addLoreLine(Component.empty());
            for (Component line : subtitle) {
                b.addLoreLine(Elements.lore(line));
            }
        }
        gui.setSlot(Layout.HEADER_SLOT, b.build());
        return this;
    }

    /** Back at the bottom left when there is somewhere to go back to, close at the bottom right. */
    public Screen footer(Runnable onBack) {
        if (onBack != null) {
            gui.setSlot(Layout.backSlot(size), Elements.back(onBack));
        }
        gui.setSlot(Layout.closeSlot(size), Elements.close(player));
        return this;
    }

    public Screen set(int slot, GuiElement element) {
        gui.setSlot(slot, element);
        return this;
    }

    /**
     * Puts one page of {@code entries} into the interior and draws the pager. {@code openPage} is called with
     * the page to show when the player clicks previous or next.
     */
    public Screen list(List<GuiElement> entries, int page, IntConsumer openPage) {
        int perPage = Layout.perPage(size);
        int pages = Pager.pageCount(entries.size(), perPage);
        int current = Pager.clamp(page, pages);
        int[] slots = Layout.interior(size);
        int start = Pager.firstIndex(current, perPage);
        for (int i = 0; i < perPage && start + i < entries.size(); i++) {
            gui.setSlot(slots[i], entries.get(start + i));
        }
        if (Pager.hasPrevious(current)) {
            gui.setSlot(Layout.previousPageSlot(size), new GuiElementBuilder(Icon.PREVIOUS.item())
                    .setName(Elements.name(Text.tr("political.gui.previous"), Palette.WARN))
                    .setCallback((a, t, c, d) -> openPage.accept(current - 1)).build());
        }
        gui.setSlot(Layout.pageInfoSlot(size), Elements.info(Icon.PAGE.item(),
                Text.tr("political.gui.page", current + 1, pages), Palette.BODY,
                Text.tr("political.gui.entries", entries.size())));
        if (Pager.hasNext(current, pages)) {
            gui.setSlot(Layout.nextPageSlot(size), new GuiElementBuilder(Icon.NEXT.item())
                    .setName(Elements.name(Text.tr("political.gui.next"), Palette.WARN))
                    .setCallback((a, t, c, d) -> openPage.accept(current + 1)).build());
        }
        return this;
    }

    public void open() {
        gui.open();
    }

    /**
     * A full screen yes or no. Anything that deletes, resets or hands out power goes through here instead of
     * firing off a single click, because a chest menu has no undo. {@code onCancel} may be null to just close.
     */
    public static void confirm(ServerPlayer player, Component what, Component consequence, Runnable onYes, Runnable onCancel) {
        Screen screen = chest(player, Layout.CONFIRM_ROWS, Text.tr("political.gui.confirm")).frame(Edge.DANGER);
        screen.set(Layout.CONFIRM_INFO_SLOT, Elements.info(Icon.NOTICE.item(), what, Palette.BAD,
                consequence, Text.tr("political.gui.cannot_undo")));
        screen.set(Layout.CONFIRM_YES_SLOT, new GuiElementBuilder(Icon.CONFIRM.item())
                .setName(Text.plain("✔ ").append(Elements.name(Text.tr("political.gui.confirm"), Palette.GOOD)))
                .addLoreLine(Elements.lore(what))
                .setCallback((a, t, c, d) -> onYes.run())
                .build());
        screen.set(Layout.CONFIRM_NO_SLOT, new GuiElementBuilder(Icon.CANCEL.item())
                .setName(Text.plain("✖ ").append(Elements.name(Text.tr("political.gui.cancel"), Palette.BAD)))
                .addLoreLine(Elements.lore(Text.tr("political.gui.nothing_happens")))
                .setCallback((a, t, c, d) -> {
                    if (onCancel != null) {
                        onCancel.run();
                    } else {
                        player.closeContainer();
                    }
                })
                .build());
        screen.open();
    }
}
