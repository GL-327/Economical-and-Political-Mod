package com.political.core.gui;

import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

/**
 * The colors every screen uses (STYLE.md). The values are the sixteen classic Minecraft chat colors that
 * the old screens already use through {@code ChatFormatting}, so a screen moved onto the palette looks the
 * same. Color is never the only carrier of meaning: every state also has a word or a symbol next to it.
 */
public enum Palette {
    TITLE(0xFFAA00, "Screen titles and header names"),
    BODY(0xFFFFFF, "Main text and values"),
    MUTED(0xAAAAAA, "Descriptions and lore"),
    FAINT(0x555555, "Hints, footnotes and the empty part of a bar"),
    GOOD(0x55FF55, "Success, on, confirm, the filled part of a bar"),
    WARN(0xFFFF55, "Click prompts and cautions"),
    BAD(0xFF5555, "Danger, cancel, close and anything off limits"),
    ACCENT(0x55FFFF, "Information and links to other screens");

    private final int rgb;
    private final String use;

    Palette(int rgb, String use) {
        this.rgb = rgb;
        this.use = use;
    }

    public int rgb() {
        return rgb;
    }

    public String use() {
        return use;
    }

    /** {@code #RRGGBB}. */
    public String hex() {
        return String.format("#%06X", rgb);
    }

    public TextColor color() {
        return TextColor.fromRgb(rgb);
    }

    public Style style() {
        return Style.EMPTY.withColor(color());
    }

    /** Bold, used for names of things you can click. */
    public Style bold() {
        return style().withBold(true);
    }
}
