package com.political.core.gui;

import com.political.core.text.Text;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;

/**
 * The stock controls of a screen (STYLE.md). Every control has a text label as well as an icon, names are
 * bold and in a palette color, lore is {@link Palette#MUTED}, and a click prompt is {@link Palette#WARN}.
 * All words come from {@code en_us.json} through {@link Text}; the caller passes its own labels already
 * translated or plain.
 */
public final class Elements {

    private Elements() {
    }

    // ── labels ──────────────────────────────────────────────────────────────────────────────

    /** A name in a palette color, bold. */
    public static Component name(Component text, Palette color) {
        return text.copy().withStyle(color.bold());
    }

    /** A lore line in {@link Palette#MUTED}. */
    public static Component lore(Component text) {
        return text.copy().withStyle(Palette.MUTED.style());
    }

    /** A lore line in a chosen color, for example a value in {@link Palette#BODY}. */
    public static Component lore(Component text, Palette color) {
        return text.copy().withStyle(color.style());
    }

    private static GuiElementBuilder base(Item icon, Component label, Palette color, Component... lines) {
        GuiElementBuilder b = new GuiElementBuilder(icon).setName(name(label, color));
        if (lines.length > 0) {
            b.addLoreLine(Component.empty());
            for (Component line : lines) {
                b.addLoreLine(lore(line));
            }
        }
        return b;
    }

    // ── navigation ──────────────────────────────────────────────────────────────────────────

    public static GuiElement back(Runnable onClick) {
        return new GuiElementBuilder(Icon.BACK.item())
                .setName(Text.plain("← ").append(name(Text.tr("political.gui.back"), Palette.WARN)))
                .setCallback((a, t, c, d) -> onClick.run())
                .build();
    }

    public static GuiElement close(ServerPlayer player) {
        return new GuiElementBuilder(Icon.CLOSE.item())
                .setName(Text.plain("✕ ").append(name(Text.tr("political.gui.close"), Palette.BAD)))
                .setCallback((a, t, c, d) -> player.closeContainer())
                .build();
    }

    // ── controls ────────────────────────────────────────────────────────────────────────────

    /** A tile that opens another screen; the lore says what is behind it. */
    public static GuiElement tile(Item icon, Component label, Palette color, Runnable onClick, Component... lines) {
        GuiElementBuilder b = base(icon, label, color, lines);
        b.addLoreLine(Component.empty());
        b.addLoreLine(lore(Text.tr("political.gui.click_to_open"), Palette.WARN));
        return b.setCallback((a, t, c, d) -> onClick.run()).build();
    }

    /** A labelled action with no screen behind it. */
    public static GuiElement action(Item icon, Component label, Palette color, Runnable onClick, Component... lines) {
        return base(icon, label, color, lines).setCallback((a, t, c, d) -> onClick.run()).build();
    }

    /** A read only readout. Clicking does nothing, on purpose. */
    public static GuiElement info(Item icon, Component label, Palette color, Component... lines) {
        return base(icon, label, color, lines).build();
    }

    /** An on or off switch. The state is a word as well as a color. */
    public static GuiElement toggle(Component label, boolean on, Runnable onFlip, Component... lines) {
        GuiElementBuilder b = new GuiElementBuilder(on ? Icon.ON.item() : Icon.OFF.item())
                .setName(name(label, on ? Palette.GOOD : Palette.MUTED))
                .addLoreLine(Component.empty())
                .addLoreLine(lore(Text.tr(on ? "political.gui.currently_on" : "political.gui.currently_off"),
                        on ? Palette.GOOD : Palette.MUTED));
        for (Component line : lines) {
            b.addLoreLine(lore(line, Palette.FAINT));
        }
        b.addLoreLine(Component.empty());
        b.addLoreLine(lore(Text.tr(on ? "political.gui.click_to_turn_off" : "political.gui.click_to_turn_on"), Palette.WARN));
        return b.setCallback((a, t, c, d) -> onFlip.run()).build();
    }

    /**
     * A number stepper, see {@link Stepper}. {@code onChange} receives the signed amount to add; the caller
     * applies it (with {@link Stepper#clamped}) and repaints.
     */
    public static GuiElement stepper(Item icon, Component label, Palette color, long current, long step,
                                     Consumer<Long> onChange, Component... lines) {
        GuiElementBuilder b = base(icon, label, color)
                .addLoreLine(Component.empty())
                .addLoreLine(lore(Text.tr("political.gui.stepper.now", current), Palette.BODY));
        for (Component line : lines) {
            b.addLoreLine(lore(line));
        }
        b.addLoreLine(Component.empty());
        b.addLoreLine(lore(Text.tr("political.gui.stepper.left", step), Palette.GOOD));
        b.addLoreLine(lore(Text.tr("political.gui.stepper.right", step), Palette.BAD));
        b.addLoreLine(lore(Text.tr("political.gui.stepper.shift"), Palette.FAINT));
        return b.setCallback((a, type, c, d) -> onChange.accept(Stepper.delta(step, type.isRight, type.shift))).build();
    }

    /** A fraction of something: the bar, and always the percentage next to it. */
    public static GuiElement progress(Item icon, Component label, Palette color, double fraction, Component... lines) {
        GuiElementBuilder b = base(icon, label, color, lines);
        b.addLoreLine(Component.empty());
        b.addLoreLine(Text.plain(ProgressBar.filled(fraction, ProgressBar.DEFAULT_CELLS)).withStyle(Palette.GOOD.style())
                .append(Text.plain(ProgressBar.empty(fraction, ProgressBar.DEFAULT_CELLS)).withStyle(Palette.FAINT.style()))
                .append(Text.plain(" " + ProgressBar.percent(fraction) + "%").withStyle(Palette.BODY.style())));
        return b.build();
    }
}
