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
 * The collections browser.
 *
 * <p>Three screens. The hub shows the six fields and how far along each one is, a field lists its ten
 * collections with a progress bar apiece, and a collection opens the full ladder of nine tiers with
 * every threshold and reward written out. Nothing here is hidden behind a command.</p>
 */
public final class CollectionGui {

    private CollectionGui() {}

    private static final int SIZE = 54;

    // ── Hub ───────────────────────────────────────────────────────────────

    public static void open(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Collections")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        GuiKit.frame(gui, SIZE);

        int total = CollectionManager.totalTiers(player);
        int maxPossible = CollectionType.values().length * CollectionType.MAX_TIER;
        int maxed = CollectionManager.maxedCount(player);

        GuiKit.header(gui, Items.BOOKSHELF, "Collections", ChatFormatting.GOLD,
                total + " of " + maxPossible + " tiers earned",
                maxed + " collections maxed",
                GuiKit.bar(total / (double) maxPossible, ChatFormatting.GREEN, ChatFormatting.DARK_GRAY));

        int[] slots = {20, 21, 22, 23, 24, 31};
        CollectionType.Field[] fields = CollectionType.Field.values();
        for (int i = 0; i < fields.length && i < slots.length; i++) {
            CollectionType.Field f = fields[i];
            List<CollectionType> inField = CollectionType.inField(f);

            int earned = 0;
            int possible = inField.size() * CollectionType.MAX_TIER;
            for (CollectionType c : inField) earned += CollectionManager.tierOf(player, c);

            final CollectionType.Field field = f;
            final int fieldEarned = earned;
            gui.setSlot(slots[i], GuiKit.tile(f.icon, f.label, f.colour,
                    () -> openField(player, field, 0),
                    inField.size() + " collections",
                    fieldEarned + " of " + possible + " tiers",
                    GuiKit.bar(fieldEarned / (double) possible, f.colour, ChatFormatting.DARK_GRAY)));
        }

        gui.setSlot(40, GuiKit.info(Items.EXPERIENCE_BOTTLE, "How this works", ChatFormatting.AQUA,
                "Every material you gather counts forever.",
                "Nine tiers per collection.",
                "Each tier pays coins and a permanent stat.",
                "Collections cannot be traded or bought."));

        GuiKit.footer(gui, SIZE, player, null);
        gui.open();
    }

    // ── One field ─────────────────────────────────────────────────────────

    private static void openField(ServerPlayer player, CollectionType.Field field, int page) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal(field.label).withStyle(field.colour, ChatFormatting.BOLD));
        GuiKit.frame(gui, SIZE);

        List<CollectionType> inField = CollectionType.inField(field);
        GuiKit.header(gui, field.icon, field.label, field.colour,
                inField.size() + " collections in this field");

        List<GuiElement> entries = new ArrayList<>();
        for (CollectionType c : inField) {
            long count = CollectionManager.countOf(player, c);
            int tier = c.tierFor(count);
            entries.add(entryFor(player, c, count, tier));
        }

        GuiKit.paint(gui, entries, page, GuiKit.LIST_SLOTS.length);
        GuiKit.pager(gui, SIZE, page, entries.size(), GuiKit.LIST_SLOTS.length,
                p -> openField(player, field, p));
        GuiKit.footer(gui, SIZE, player, () -> open(player));
        gui.open();
    }

    private static GuiElement entryFor(ServerPlayer player, CollectionType c, long count, int tier) {
        boolean maxed = tier >= CollectionType.MAX_TIER;
        long next = maxed ? 0 : c.thresholdFor(tier + 1);
        double toNext = maxed ? 1.0 : Math.min(1.0, count / (double) next);

        GuiElementBuilder b = new GuiElementBuilder(c.material)
                .setName(Component.literal(c.display + (tier > 0
                        ? " " + CollectionManager.roman(tier) : ""))
                        .withStyle(c.colour, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§8" + c.flavor))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Gathered: §f" + GuiKit.num(count)))
                .addLoreLine(Component.literal("§7Tier: §f" + tier + " §8of " + CollectionType.MAX_TIER));

        if (maxed) {
            b.addLoreLine(Component.literal("§d§lMaxed"));
            b.glow();
        } else {
            b.addLoreLine(Component.literal("§7Next at §f" + GuiKit.num(next)));
            b.addLoreLine(Component.literal(GuiKit.bar(toNext, ChatFormatting.GREEN,
                    ChatFormatting.DARK_GRAY)));
        }

        b.addLoreLine(Component.literal(""));
        b.addLoreLine(Component.literal("§7Feeds §"
                + CollectionManager.statColour(c.statKey).getChar()
                + CollectionManager.statLabel(c.statKey)
                + " §8(+" + c.statBonusFor(tier) + " now)"));
        b.addLoreLine(Component.literal(""));
        b.addLoreLine(Component.literal("§eClick for the full ladder"));

        return b.setCallback((a, t, ci, sg) -> openCollection(player, c)).build();
    }

    // ── One collection ────────────────────────────────────────────────────

    private static void openCollection(ServerPlayer player, CollectionType c) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal(c.display).withStyle(c.colour, ChatFormatting.BOLD));
        GuiKit.frame(gui, SIZE);

        long count = CollectionManager.countOf(player, c);
        int tier = c.tierFor(count);

        GuiKit.header(gui, c.material, c.display, c.colour,
                c.flavor,
                "Gathered " + GuiKit.num(count),
                "Tier " + tier + " of " + CollectionType.MAX_TIER);

        // The nine rungs, laid out two rows across.
        int[] slots = {19, 20, 21, 22, 23, 24, 25, 30, 32};
        for (int t = 1; t <= CollectionType.MAX_TIER; t++) {
            int need = c.thresholdFor(t);
            boolean done = count >= need;
            boolean current = !done && t == tier + 1;

            GuiElementBuilder b = new GuiElementBuilder(
                    done ? Items.LIME_STAINED_GLASS_PANE
                            : current ? Items.YELLOW_STAINED_GLASS_PANE
                            : Items.RED_STAINED_GLASS_PANE)
                    .setName(Component.literal(c.display + " " + CollectionManager.roman(t))
                            .withStyle(done ? ChatFormatting.GREEN
                                    : current ? ChatFormatting.YELLOW : ChatFormatting.RED,
                                    ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§7Requires §f" + GuiKit.num(need)
                            + " §7" + c.display.toLowerCase()))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§7Rewards"))
                    .addLoreLine(Component.literal("  §6" + GuiKit.num(c.payoutFor(t)) + " coins"))
                    .addLoreLine(Component.literal("  §a+" + c.statBonusFor(t) + " "
                            + CollectionManager.statLabel(c.statKey) + " §8total at this tier"));

            if (done) {
                b.addLoreLine(Component.literal(""));
                b.addLoreLine(Component.literal("§a✔ Earned"));
                b.glow();
            } else if (current) {
                long remaining = need - count;
                b.addLoreLine(Component.literal(""));
                b.addLoreLine(Component.literal("§e" + GuiKit.num(remaining) + " to go"));
                b.addLoreLine(Component.literal(GuiKit.bar(count / (double) need,
                        ChatFormatting.GREEN, ChatFormatting.DARK_GRAY)));
            } else {
                b.addLoreLine(Component.literal(""));
                b.addLoreLine(Component.literal("§8Locked"));
            }

            gui.setSlot(slots[t - 1], b.build());
        }

        gui.setSlot(40, GuiKit.info(Items.NETHER_STAR, "At the top", ChatFormatting.LIGHT_PURPLE,
                "Maxing this grants +" + c.statBonusFor(CollectionType.MAX_TIER) + " "
                        + CollectionManager.statLabel(c.statKey),
                "and " + GuiKit.num(totalPayout(c)) + " coins across all nine tiers"));

        GuiKit.footer(gui, SIZE, player, () -> openField(player, c.field, 0));
        gui.open();
    }

    private static long totalPayout(CollectionType c) {
        long sum = 0;
        for (int t = 1; t <= CollectionType.MAX_TIER; t++) sum += c.payoutFor(t);
        return sum;
    }
}
