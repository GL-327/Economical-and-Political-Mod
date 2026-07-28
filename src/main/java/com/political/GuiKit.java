package com.political;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

/**
 * Shared furniture for chest menus.
 *
 * <p>This is a server side mod, so the chest menu is the entire interface. Before this existed every
 * screen drew its own border, invented its own back arrow and picked its own colours, which made a
 * hundred odd menus feel like a hundred odd mods. Everything here is deliberately small and boring:
 * a frame, a header, a footer, a pager and a few stock controls. The point is that a player who
 * learns one screen has learned all of them.</p>
 */
public final class GuiKit {

    private GuiKit() {}

    // ── Palette ───────────────────────────────────────────────────────────

    public static final Item FRAME = Items.BLACK_STAINED_GLASS_PANE;
    public static final Item EDGE = Items.GRAY_STAINED_GLASS_PANE;
    public static final Item ACCENT_EDGE = Items.CYAN_STAINED_GLASS_PANE;
    public static final Item DANGER_EDGE = Items.RED_STAINED_GLASS_PANE;

    /** A blank pane with no name, used to fill dead space. */
    public static GuiElement blank(Item item) {
        return new GuiElementBuilder(item).setName(Component.literal(" ")).build();
    }

    // ── Frames ────────────────────────────────────────────────────────────

    /** The standard two tone frame: an edge ring and a darker interior. */
    public static void frame(SimpleGui gui, int size) {
        frame(gui, size, EDGE);
    }

    /** A frame with a chosen edge colour, so dangerous screens can read as dangerous. */
    public static void frame(SimpleGui gui, int size, Item edgeItem) {
        int rows = Math.max(1, size / 9);
        for (int i = 0; i < size; i++) {
            int row = i / 9;
            int col = i % 9;
            boolean edge = row == 0 || row == rows - 1 || col == 0 || col == 8;
            gui.setSlot(i, blank(edge ? edgeItem : FRAME));
        }
    }

    /** Title in the top middle, with an optional subtitle underneath. */
    public static void header(SimpleGui gui, Item icon, String title, ChatFormatting colour,
                              String... subtitle) {
        GuiElementBuilder b = new GuiElementBuilder(icon)
                .setName(Component.literal(title).withStyle(colour, ChatFormatting.BOLD))
                .glow();
        if (subtitle.length > 0) b.addLoreLine(Component.literal(""));
        for (String s : subtitle) {
            b.addLoreLine(Component.literal("§7" + s));
        }
        gui.setSlot(4, b.build());
    }

    // ── Navigation ────────────────────────────────────────────────────────

    public static GuiElement back(Runnable onClick) {
        return new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .setCallback((a, b, c, d) -> onClick.run())
                .build();
    }

    public static GuiElement close(ServerPlayer player) {
        return new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("✕ Close").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .setCallback((a, b, c, d) -> player.closeContainer())
                .build();
    }

    /** Back on the left of the bottom row, close on the right. */
    public static void footer(SimpleGui gui, int size, ServerPlayer player, Runnable onBack) {
        int base = size - 9;
        if (onBack != null) gui.setSlot(base, back(onBack));
        gui.setSlot(base + 8, close(player));
    }

    // ── Buttons ───────────────────────────────────────────────────────────

    /** A category tile. The lore lines describe what is inside before the click. */
    public static GuiElement tile(Item icon, String label, ChatFormatting colour,
                                  Runnable onClick, String... lines) {
        GuiElementBuilder b = new GuiElementBuilder(icon)
                .setName(Component.literal(label).withStyle(colour, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""));
        for (String s : lines) {
            b.addLoreLine(Component.literal("§7• " + s));
        }
        b.addLoreLine(Component.literal(""));
        b.addLoreLine(Component.literal("§eClick to open"));
        return b.setCallback((a, t, c, d) -> onClick.run()).build();
    }

    /** A plain labelled action with no submenu behind it. */
    public static GuiElement action(Item icon, String label, ChatFormatting colour,
                                    Runnable onClick, String... lines) {
        GuiElementBuilder b = new GuiElementBuilder(icon)
                .setName(Component.literal(label).withStyle(colour, ChatFormatting.BOLD));
        if (lines.length > 0) b.addLoreLine(Component.literal(""));
        for (String s : lines) {
            b.addLoreLine(Component.literal("§7" + s));
        }
        return b.setCallback((a, t, c, d) -> onClick.run()).build();
    }

    /** A read only readout. Clicking does nothing on purpose. */
    public static GuiElement info(Item icon, String label, ChatFormatting colour, String... lines) {
        GuiElementBuilder b = new GuiElementBuilder(icon)
                .setName(Component.literal(label).withStyle(colour, ChatFormatting.BOLD));
        if (lines.length > 0) b.addLoreLine(Component.literal(""));
        for (String s : lines) {
            b.addLoreLine(Component.literal("§7" + s));
        }
        return b.build();
    }

    /** An on or off switch that repaints its own screen after flipping. */
    public static GuiElement toggle(String label, boolean on, Runnable onFlip, String... lines) {
        GuiElementBuilder b = new GuiElementBuilder(on ? Items.LIME_DYE : Items.GRAY_DYE)
                .setName(Component.literal(label).withStyle(
                        on ? ChatFormatting.GREEN : ChatFormatting.GRAY, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal(on ? "§aCurrently on" : "§7Currently off"));
        for (String s : lines) {
            b.addLoreLine(Component.literal("§8" + s));
        }
        b.addLoreLine(Component.literal(""));
        b.addLoreLine(Component.literal("§eClick to " + (on ? "turn off" : "turn on")));
        return b.setCallback((a, t, c, d) -> onFlip.run()).build();
    }

    /**
     * A three speed stepper. Left click nudges, right click nudges the other way and shift multiplies
     * by ten, which covers almost every number an operator wants to change without a chat prompt.
     */
    public static GuiElement stepper(Item icon, String label, ChatFormatting colour,
                                     long current, long step, IntConsumer unusedLegacy,
                                     Consumer<Long> onChange, String... lines) {
        GuiElementBuilder b = new GuiElementBuilder(icon)
                .setName(Component.literal(label).withStyle(colour, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§fNow: §e" + current));
        for (String s : lines) {
            b.addLoreLine(Component.literal("§7" + s));
        }
        b.addLoreLine(Component.literal(""));
        b.addLoreLine(Component.literal("§aLeft click §7+" + step));
        b.addLoreLine(Component.literal("§cRight click §7-" + step));
        b.addLoreLine(Component.literal("§8Hold shift for ten times as much"));
        return b.setCallback((a, type, c, d) -> {
            long mult = type.shift ? 10L : 1L;
            long delta = (type.isRight ? -step : step) * mult;
            onChange.accept(delta);
        }).build();
    }

    /** A head tile for a named player, with the name and lore already applied. */
    public static GuiElement head(String name, String label, ChatFormatting colour,
                                  Runnable onClick, String... lines) {
        // The profile component is left alone on purpose. Resolving skins for a list of forty
        // offline players costs more than the cosmetic is worth on a server side menu.
        GuiElementBuilder b = new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Component.literal(label).withStyle(colour, ChatFormatting.BOLD));
        if (lines.length > 0) b.addLoreLine(Component.literal(""));
        for (String s : lines) {
            b.addLoreLine(Component.literal("§7" + s));
        }
        if (onClick != null) {
            b.addLoreLine(Component.literal(""));
            b.addLoreLine(Component.literal("§eClick to manage"));
            return b.setCallback((a, t, c, d) -> onClick.run()).build();
        }
        return b.build();
    }

    // ── Pagination ────────────────────────────────────────────────────────

    /** Where a paged list puts its entries: the interior of a 9x6, so twenty eight slots. */
    public static final int[] LIST_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    public static int pageCount(int total, int perPage) {
        return Math.max(1, (int) Math.ceil(total / (double) perPage));
    }

    /**
     * Draws previous and next arrows plus a page counter along the bottom row. The counter is a real
     * element rather than a title suffix so it survives a refresh.
     */
    public static void pager(SimpleGui gui, int size, int page, int total, int perPage,
                             IntConsumer openPage) {
        int pages = pageCount(total, perPage);
        int clamped = Math.max(0, Math.min(page, pages - 1));
        int base = size - 9;

        if (clamped > 0) {
            gui.setSlot(base + 3, new GuiElementBuilder(Items.SPECTRAL_ARROW)
                    .setName(Component.literal("← Previous").withStyle(ChatFormatting.YELLOW))
                    .setCallback((a, b, c, d) -> openPage.accept(clamped - 1))
                    .build());
        }

        gui.setSlot(base + 4, info(Items.PAPER,
                "Page " + (clamped + 1) + " of " + pages, ChatFormatting.WHITE,
                total + " entries"));

        if (clamped < pages - 1) {
            gui.setSlot(base + 5, new GuiElementBuilder(Items.SPECTRAL_ARROW)
                    .setName(Component.literal("Next →").withStyle(ChatFormatting.YELLOW))
                    .setCallback((a, b, c, d) -> openPage.accept(clamped + 1))
                    .build());
        }
    }

    /** Lays a page of already built elements into the list area. */
    public static void paint(SimpleGui gui, List<GuiElement> entries, int page, int perPage) {
        int start = page * perPage;
        for (int i = 0; i < perPage && i < LIST_SLOTS.length; i++) {
            int idx = start + i;
            if (idx >= entries.size()) break;
            gui.setSlot(LIST_SLOTS[i], entries.get(idx));
        }
    }

    // ── Confirmation ──────────────────────────────────────────────────────

    /**
     * A full screen yes or no. Anything that deletes, resets or hands out power should route through
     * here rather than firing straight off a click, because a chest menu has no undo.
     */
    public static void confirm(ServerPlayer player, String what, String consequence,
                               Runnable onYes, Runnable onCancel) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal("Confirm").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        frame(gui, 27, DANGER_EDGE);

        gui.setSlot(4, info(Items.COMMAND_BLOCK, what, ChatFormatting.RED, consequence,
                "This cannot be undone."));

        gui.setSlot(11, new GuiElementBuilder(Items.LIME_CONCRETE)
                .setName(Component.literal("✔ Confirm").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7" + what))
                .setCallback((a, b, c, d) -> onYes.run())
                .build());

        gui.setSlot(15, new GuiElementBuilder(Items.RED_CONCRETE)
                .setName(Component.literal("✖ Cancel").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Nothing happens."))
                .setCallback((a, b, c, d) -> {
                    if (onCancel != null) onCancel.run();
                    else player.closeContainer();
                })
                .build());

        gui.open();
    }

    // ── Text entry ────────────────────────────────────────────────────────

    /**
     * A sign prompt for the handful of values a stepper cannot express, like a name. Returns the
     * typed line, trimmed, or an empty string when the player leaves it blank.
     */
    public static void prompt(ServerPlayer player, String label, Consumer<String> onDone,
                              Runnable onCancel) {
        eu.pb4.sgui.api.gui.SignGui sign = new eu.pb4.sgui.api.gui.SignGui(player) {
            @Override
            public void onManualClose() {
                String typed = getLine(0).getString().trim();
                if (typed.isEmpty()) {
                    if (onCancel != null) onCancel.run();
                    return;
                }
                onDone.accept(typed);
            }
        };
        sign.setLine(1, Component.literal("^^^^^^^^^^^^"));
        sign.setLine(2, Component.literal(label));
        sign.setLine(3, Component.literal("Type above"));
        sign.open();
    }

    // ── Formatting ────────────────────────────────────────────────────────

    /** Big numbers, shortened, because a chest menu has very little room. */
    public static String num(long v) {
        if (Math.abs(v) >= 1_000_000_000L) return String.format("%.1fB", v / 1_000_000_000.0);
        if (Math.abs(v) >= 1_000_000L) return String.format("%.1fM", v / 1_000_000.0);
        if (Math.abs(v) >= 1_000L) return String.format("%.1fK", v / 1_000.0);
        return String.valueOf(v);
    }

    /** A ten segment bar, for anything that is a fraction of something. */
    public static String bar(double fraction, ChatFormatting full, ChatFormatting empty) {
        int filled = (int) Math.round(Math.max(0, Math.min(1, fraction)) * 10);
        StringBuilder sb = new StringBuilder();
        sb.append("§").append(full.getChar());
        for (int i = 0; i < filled; i++) sb.append('|');
        sb.append("§").append(empty.getChar());
        for (int i = filled; i < 10; i++) sb.append('|');
        return sb.toString();
    }

    /** Splits a long sentence into lore sized lines so nothing runs off the tooltip. */
    public static List<String> wrap(String text, int width) {
        List<String> out = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            if (line.length() + word.length() + 1 > width) {
                out.add(line.toString());
                line.setLength(0);
            }
            if (line.length() > 0) line.append(' ');
            line.append(word);
        }
        if (line.length() > 0) out.add(line.toString());
        return out;
    }

    /** Applies a name and lore to an existing stack without rebuilding it. */
    public static ItemStack label(ItemStack stack, Component name, List<Component> lore) {
        stack.set(DataComponents.CUSTOM_NAME, name);
        stack.set(DataComponents.LORE, new ItemLore(lore));
        return stack;
    }

    /** Refreshes whatever screen the supplier opens, used after a toggle or a stepper. */
    public static void refresh(Supplier<Void> opener) {
        opener.get();
    }
}
