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
 * The crate hall.
 *
 * <p>Four screens: the hall itself, one crate in detail, the full odds table, and the result of a
 * pull. The odds screen is reachable before buying anything and lists every band with its exact
 * percentage and how many items are currently in that band's pool, which is the difference between
 * selling somebody a crate and selling them a gamble.</p>
 */
public final class CrateGui {

    private CrateGui() {}

    private static final int SIZE = 54;

    // ── The hall ──────────────────────────────────────────────────────────

    public static void open(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Crates").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        GuiKit.frame(gui, SIZE);

        GuiKit.header(gui, Items.ENDER_CHEST, "Crates", ChatFormatting.GOLD,
                "Balance " + GuiKit.num(CoinManager.getCoinsLong(player)) + " coins",
                CrateManager.totalKeys(player) + " keys held",
                "Every crate shows its odds before you buy");

        int[] slots = {20, 21, 22, 23, 24};
        Crate[] all = Crate.values();
        for (int i = 0; i < all.length && i < slots.length; i++) {
            Crate c = all[i];
            int keys = CrateManager.keysOf(player, c);
            gui.setSlot(slots[i], new GuiElementBuilder(c.icon)
                    .setName(Component.literal(c.display).withStyle(c.colour, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§8" + c.blurb))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§7Key price §6" + GuiKit.num(c.price)))
                    .addLoreLine(Component.literal("§7You hold §f" + keys
                            + (keys == 1 ? " key" : " keys")))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§7Legendary §f" + c.oddsFor(Crate.Band.LEGENDARY)
                            + "%  §7Epic §f" + c.oddsFor(Crate.Band.EPIC) + "%"))
                    .addLoreLine(Component.literal("§8Guaranteed " + c.pityBand().label
                            + " within " + c.pityAfter() + " pulls"))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§eClick to open the crate page"))
                    .setCallback((a, t, ci, sg) -> openCrate(player, c))
                    .build());
        }

        gui.setSlot(38, GuiKit.tile(Items.WRITABLE_BOOK, "Recent pulls", ChatFormatting.AQUA,
                () -> openFeed(player), "What has been coming out lately"));

        gui.setSlot(40, GuiKit.info(Items.PAPER, "How crates work", ChatFormatting.WHITE,
                "Keys are bought with coins you earned.",
                "Odds are published in full, per band.",
                "A pity counter guarantees a good pull",
                "after a set number of poor ones.",
                "",
                "Nothing here takes real money."));

        gui.setSlot(42, GuiKit.tile(Items.GOLD_INGOT, "Bazaar", ChatFormatting.GOLD,
                () -> BazaarGui.open(player), "Where the coins come from"));

        GuiKit.footer(gui, SIZE, player, null);
        gui.open();
    }

    // ── One crate ─────────────────────────────────────────────────────────

    private static void openCrate(ServerPlayer player, Crate c) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal(c.display).withStyle(c.colour, ChatFormatting.BOLD));
        GuiKit.frame(gui, SIZE);

        int keys = CrateManager.keysOf(player, c);
        int since = CrateManager.pitySince(player, c);

        GuiKit.header(gui, c.icon, c.display, c.colour,
                c.blurb,
                keys + (keys == 1 ? " key held" : " keys held"),
                "Balance " + GuiKit.num(CoinManager.getCoinsLong(player)) + " coins");

        // Buying.
        int[] buyCounts = {1, 5, 10};
        for (int i = 0; i < buyCounts.length; i++) {
            final int n = buyCounts[i];
            long cost = c.price * n;
            gui.setSlot(19 + i, GuiKit.action(Items.GOLD_INGOT, "Buy " + n
                            + (n == 1 ? " key" : " keys"), ChatFormatting.GOLD,
                    () -> {
                        CrateManager.buyKeys(player, c, n);
                        openCrate(player, c);
                    },
                    GuiKit.num(cost) + " coins",
                    CoinManager.getCoinsLong(player) >= cost ? "You can afford this"
                            : "§cNot enough coins"));
        }

        // Opening.
        gui.setSlot(23, GuiKit.action(keys > 0 ? Items.TRIAL_KEY : Items.BARRIER, "Open one",
                keys > 0 ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY,
                () -> {
                    CrateManager.Pull pull = CrateManager.open(player, c);
                    if (pull != null) openResult(player, pull);
                    else openCrate(player, c);
                },
                keys > 0 ? "Uses one key" : "You have no keys"));

        gui.setSlot(24, GuiKit.action(keys >= 10 ? Items.TRIAL_KEY : Items.BARRIER, "Open ten",
                keys >= 10 ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY,
                () -> {
                    if (CrateManager.keysOf(player, c) < 10) {
                        player.sendSystemMessage(Component.literal("§cYou need ten keys for that."));
                        openCrate(player, c);
                        return;
                    }
                    List<CrateManager.Pull> pulls = new ArrayList<>();
                    for (int i = 0; i < 10; i++) {
                        CrateManager.Pull p = CrateManager.open(player, c);
                        if (p != null) pulls.add(p);
                    }
                    openBatch(player, c, pulls);
                },
                keys >= 10 ? "Uses ten keys" : "Needs ten keys"));

        // Pity state, shown plainly.
        gui.setSlot(31, GuiKit.info(Items.CLOCK, "Pity counter", ChatFormatting.LIGHT_PURPLE,
                since + " of " + c.pityAfter() + " pulls since a good one",
                GuiKit.bar(since / (double) c.pityAfter(),
                        ChatFormatting.LIGHT_PURPLE, ChatFormatting.DARK_GRAY),
                "At " + c.pityAfter() + " the next pull is at least "
                        + c.pityBand().label + "."));

        gui.setSlot(33, GuiKit.tile(Items.BOOK, "Full odds", ChatFormatting.AQUA,
                () -> openOdds(player, c), "Every band, every percentage"));

        GuiKit.footer(gui, SIZE, player, () -> open(player));
        gui.open();
    }

    // ── Odds ──────────────────────────────────────────────────────────────

    private static void openOdds(ServerPlayer player, Crate c) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal(c.display + " odds")
                .withStyle(c.colour, ChatFormatting.BOLD));
        GuiKit.frame(gui, SIZE);
        GuiKit.header(gui, Items.BOOK, "Published odds", ChatFormatting.AQUA,
                c.display,
                "These are the real numbers the roll uses");

        int[] slots = {19, 20, 21, 22, 23};
        Crate.Band[] bands = Crate.Band.values();
        for (int i = 0; i < bands.length; i++) {
            Crate.Band b = bands[i];
            int pct = c.oddsFor(b);
            List<String> sample = CrateManager.sampleOf(b, 4);

            GuiElementBuilder e = new GuiElementBuilder(bandIcon(b))
                    .setName(Component.literal(b.label + "  " + pct + "%")
                            .withStyle(b.colour, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal(GuiKit.bar(pct / 100.0, b.colour,
                            ChatFormatting.DARK_GRAY)))
                    .addLoreLine(Component.literal("§7" + CrateManager.poolSize(b)
                            + " items in this band"));
            if (!sample.isEmpty()) {
                e.addLoreLine(Component.literal(""));
                e.addLoreLine(Component.literal("§8For example:"));
                for (String sName : sample) {
                    e.addLoreLine(Component.literal("  §8" + sName));
                }
            }
            if (pct == 0) e.addLoreLine(Component.literal("§8Not in this crate at all."));
            gui.setSlot(slots[i], e.build());
        }

        gui.setSlot(40, GuiKit.info(Items.CLOCK, "Pity", ChatFormatting.LIGHT_PURPLE,
                "After " + c.pityAfter() + " pulls without a "
                        + c.pityBand().label + " or better,",
                "the next pull is forced to at least " + c.pityBand().label + ".",
                "",
                "The counter resets whenever that happens."));

        GuiKit.footer(gui, SIZE, player, () -> openCrate(player, c));
        gui.open();
    }

    // ── Results ───────────────────────────────────────────────────────────

    private static void openResult(ServerPlayer player, CrateManager.Pull pull) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal(pull.band.label)
                .withStyle(pull.band.colour, ChatFormatting.BOLD));
        GuiKit.frame(gui, 27);

        GuiElementBuilder e = new GuiElementBuilder(
                pull.reward.isEmpty() ? Items.GOLD_INGOT : pull.reward.getItem())
                .setName(Component.literal(pull.label).withStyle(pull.band.colour, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7From a " + pull.crate.display))
                .addLoreLine(Component.literal(pull.band.colour + pull.band.label));
        if (pull.pityFired) {
            e.addLoreLine(Component.literal("§dThe pity counter fired."));
        }
        e.glow();
        gui.setSlot(13, e.build());

        gui.setSlot(11, GuiKit.action(Items.ENDER_CHEST, "Open another", ChatFormatting.GREEN,
                () -> openCrate(player, pull.crate),
                CrateManager.keysOf(player, pull.crate) + " keys left"));
        gui.setSlot(15, GuiKit.action(Items.ARROW, "Back to the hall", ChatFormatting.YELLOW,
                () -> open(player), ""));

        gui.open();
    }

    private static void openBatch(ServerPlayer player, Crate c, List<CrateManager.Pull> pulls) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Ten pulls").withStyle(c.colour, ChatFormatting.BOLD));
        GuiKit.frame(gui, SIZE);

        int best = 0;
        for (CrateManager.Pull p : pulls) best = Math.max(best, p.band.ordinal());
        Crate.Band bestBand = Crate.Band.values()[best];

        GuiKit.header(gui, c.icon, "Ten pulls", bestBand.colour,
                "Best: " + bestBand.label,
                pulls.size() + " opened");

        for (int i = 0; i < pulls.size() && i < GuiKit.LIST_SLOTS.length; i++) {
            CrateManager.Pull p = pulls.get(i);
            gui.setSlot(GuiKit.LIST_SLOTS[i], new GuiElementBuilder(
                    p.reward.isEmpty() ? Items.GOLD_INGOT : p.reward.getItem())
                    .setName(Component.literal(p.label).withStyle(p.band.colour))
                    .addLoreLine(Component.literal(p.band.colour + p.band.label))
                    .build());
        }

        GuiKit.footer(gui, SIZE, player, () -> openCrate(player, c));
        gui.open();
    }

    // ── Feed ──────────────────────────────────────────────────────────────

    private static void openFeed(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Recent pulls")
                .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        GuiKit.frame(gui, SIZE);
        GuiKit.header(gui, Items.WRITABLE_BOOK, "Recent pulls", ChatFormatting.AQUA,
                "Newest first, server wide");

        List<String> feed = CrateManager.feed();
        if (feed.isEmpty()) {
            gui.setSlot(31, GuiKit.info(Items.BARRIER, "Nothing yet", ChatFormatting.GRAY,
                    "No crates have been opened."));
        }
        for (int i = 0; i < feed.size() && i < GuiKit.LIST_SLOTS.length; i++) {
            gui.setSlot(GuiKit.LIST_SLOTS[i], new GuiElementBuilder(Items.PAPER)
                    .setName(Component.literal(feed.get(i)))
                    .build());
        }

        GuiKit.footer(gui, SIZE, player, () -> open(player));
        gui.open();
    }

    private static net.minecraft.world.item.Item bandIcon(Crate.Band b) {
        return switch (b) {
            case COMMON -> Items.GRAY_DYE;
            case UNCOMMON -> Items.LIME_DYE;
            case RARE -> Items.LIGHT_BLUE_DYE;
            case EPIC -> Items.PURPLE_DYE;
            case LEGENDARY -> Items.ORANGE_DYE;
        };
    }
}
