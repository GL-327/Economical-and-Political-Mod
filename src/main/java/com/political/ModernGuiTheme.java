package com.political;

import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/**
 * V7 Pillar C — a reusable sgui styling toolkit that makes older screens look modern and
 * consistent without rewriting them. Any GUI can call these static helpers to get a clean
 * header, a coloured bordered fill, stat "pills" and a footer bar, all sharing one palette.
 *
 * <p>Nothing here changes gameplay. It only decorates existing menus, so it is always safe
 * to layer on top of a {@link SimpleGui} before the real content slots are set.
 *
 * <pre>{@code
 * ModernGuiTheme.border(gui, 54);
 * ModernGuiTheme.header(gui, "Cursed Spirits", "Hunt the graded horrors");
 * gui.setSlot(22, ModernGuiTheme.pill(Items.DIAMOND, "Cursed Energy", "1,240", ChatFormatting.AQUA));
 * ModernGuiTheme.footer(gui, 54, "Everything here is playable by clicking");
 * }</pre>
 */
public final class ModernGuiTheme {

    private ModernGuiTheme() {}

    // ── palette ──
    // A single consistent colour language other screens can reuse for text and accents.

    public static final ChatFormatting ACCENT = ChatFormatting.AQUA;
    public static final ChatFormatting TITLE = ChatFormatting.GOLD;
    public static final ChatFormatting SUBTITLE = ChatFormatting.GRAY;
    public static final ChatFormatting GOOD = ChatFormatting.GREEN;
    public static final ChatFormatting WARN = ChatFormatting.RED;
    public static final ChatFormatting MUTED = ChatFormatting.DARK_GRAY;
    public static final ChatFormatting VALUE = ChatFormatting.WHITE;

    /** The glass used for the outer frame. */
    public static final Item BORDER_GLASS = Items.CYAN_STAINED_GLASS_PANE;
    /** The glass used for the top header strip behind the title. */
    public static final Item HEADER_GLASS = Items.BLUE_STAINED_GLASS_PANE;
    /** The glass used for the bottom footer strip. */
    public static final Item FOOTER_GLASS = Items.GRAY_STAINED_GLASS_PANE;

    /**
     * Draws a coloured border around the whole inventory and fills the interior with a dark
     * neutral pane, giving any screen a framed modern look. Call this first, then place content.
     *
     * @param size the total slot count, for example 54 for a 9x6 menu.
     */
    public static void border(SimpleGui gui, int size) {
        if (gui == null || size <= 0) return;
        int cols = 9;
        int rows = size / cols;
        GuiElement frame = new GuiElementBuilder(BORDER_GLASS)
                .setName(Component.literal("")).build();
        GuiElement inner = new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                .setName(Component.literal("")).build();
        for (int i = 0; i < size; i++) {
            int col = i % cols;
            int row = i / cols;
            boolean edge = row == 0 || row == rows - 1 || col == 0 || col == cols - 1;
            gui.setSlot(i, edge ? frame : inner);
        }
    }

    /**
     * Places a bold title and a soft subtitle across the top strip of the menu, on a coloured
     * header pane. The title sits centred in slot 4 with the subtitle as its first lore line,
     * and the rest of the top row is dressed with the header glass.
     */
    public static void header(SimpleGui gui, String title, String subtitle) {
        if (gui == null) return;
        GuiElement strip = new GuiElementBuilder(HEADER_GLASS)
                .setName(Component.literal("")).build();
        for (int i = 0; i < 9; i++) {
            if (i != 4) gui.setSlot(i, strip);
        }
        GuiElementBuilder head = new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Component.literal("✦ " + (title == null ? "" : title))
                        .withStyle(TITLE, ChatFormatting.BOLD))
                .glow();
        if (subtitle != null && !subtitle.isBlank()) {
            head.addLoreLine(Component.literal(subtitle).withStyle(SUBTITLE));
        }
        gui.setSlot(4, head.build());
    }

    /**
     * Fills the bottom row with a footer strip and drops a short note in the centre footer slot.
     * The note is optional. Pass null or blank to leave a plain bar.
     */
    public static void footer(SimpleGui gui, int size, String note) {
        if (gui == null || size <= 0) return;
        int cols = 9;
        int base = size - cols;
        GuiElement strip = new GuiElementBuilder(FOOTER_GLASS)
                .setName(Component.literal("")).build();
        for (int i = 0; i < cols; i++) {
            int slot = base + i;
            if (note != null && !note.isBlank() && i == 4) {
                gui.setSlot(slot, new GuiElementBuilder(Items.PAPER)
                        .setName(Component.literal(note).withStyle(SUBTITLE))
                        .build());
            } else {
                gui.setSlot(slot, strip);
            }
        }
    }

    /**
     * Builds a "stat pill": a compact icon that reads as a labelled value, the modern way of
     * showing a number on a screen. The value is coloured with the given accent, the label is
     * muted underneath.
     *
     * @param icon  the item shown for the pill.
     * @param label the small descriptive text, for example "Cursed Energy".
     * @param value the highlighted value, for example "1,240".
     * @param color the accent colour for the value.
     */
    public static GuiElement pill(Item icon, String label, String value, ChatFormatting color) {
        ChatFormatting accent = color == null ? ACCENT : color;
        return new GuiElementBuilder(icon == null ? Items.PAPER : icon)
                .setName(Component.literal(value == null ? "" : value)
                        .withStyle(accent, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(label == null ? "" : label).withStyle(SUBTITLE))
                .build();
    }

    /**
     * A clickable modern button element with a coloured bold name, up to two lore lines and a
     * glint. Use this for the primary actions on a modern screen.
     */
    public static GuiElement button(Item icon, String name, ChatFormatting color,
                                    String line1, String line2, Runnable onClick) {
        GuiElementBuilder b = new GuiElementBuilder(icon == null ? Items.PAPER : icon)
                .setName(Component.literal(name == null ? "" : name)
                        .withStyle(color == null ? ACCENT : color, ChatFormatting.BOLD))
                .glow();
        if (line1 != null && !line1.isBlank()) b.addLoreLine(Component.literal(line1).withStyle(SUBTITLE));
        if (line2 != null && !line2.isBlank()) b.addLoreLine(Component.literal(line2).withStyle(SUBTITLE));
        if (onClick != null) {
            b.addLoreLine(Component.literal(""));
            b.addLoreLine(Component.literal("➤ Click to open").withStyle(ChatFormatting.YELLOW));
            b.setCallback((a, c, d, e) -> onClick.run());
        }
        return b.build();
    }
}
