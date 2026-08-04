package com.political;

import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * The delveforge.
 *
 * <p>Works on whatever is in your hand, like the enchanting bench, so there is no slot an item can
 * be lost to. Two buttons and a lot of explanation, because the difference between a mark and a
 * sigil is the difference between an afternoon and a fortnight and nobody should find that out by
 * spending the wrong one.</p>
 */
public final class DelveForgeGui {

    private DelveForgeGui() {}

    private static final int SIZE = 54;

    public static void open(ServerPlayer player) {
        ItemStack held = player.getMainHandItem();

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Delveforge")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
        GuiKit.frame(gui, SIZE);

        if (held.isEmpty()) {
            GuiKit.header(gui, Items.SMITHING_TABLE, "Delveforge", ChatFormatting.DARK_PURPLE,
                    "Hold the piece you want to work on",
                    "Weapons, tools, bows and armour",
                    "Anything the mod already recognises as gear");
            gui.setSlot(22, GuiKit.info(Items.BARRIER, "Nothing in hand", ChatFormatting.RED,
                    "The item never leaves your hand, so",
                    "there is nothing here that can eat it."));
            GuiKit.footer(gui, SIZE, player, null);
            gui.open();
            return;
        }

        boolean forged = DelveForge.isForged(held);
        int marks = DelveForge.marks(held);
        int sigils = DelveForge.sigils(held);
        DelveRun run = DelveManager.runOf(player);
        int depth = run == null ? 0 : run.floor.tier;

        GuiKit.header(gui, Items.SMITHING_TABLE, "Delveforge", ChatFormatting.DARK_PURPLE,
                "Working on " + held.getHoverName().getString(),
                DelveForge.describe(held),
                depth > 0 ? "Standing on Depth " + depth : "Not in a delve");

        // What it is now, and what it would be.
        double now = DelveForge.statMultiplier(held, depth);
        // Read from the same constants the maths uses, so this preview cannot drift away from
        // what the button actually does the next time the numbers are tuned.
        double next = forged
                ? now + (marks < DelveForge.MAX_MARKS ? DelveForge.MARK_STEP : DelveForge.SIGIL_STEP)
                : 1.0 + DelveForge.FORGE_STEP + depth * DelveForge.DEPTH_STEP;

        gui.setSlot(4, GuiKit.info(held.getItem(), held.getHoverName().getString(),
                ChatFormatting.WHITE,
                DelveForge.describe(held),
                "",
                "§7Stats now §f" + pct(now),
                "§7After the next step §a" + pct(next),
                "",
                marks >= DelveForge.MAX_MARKS
                        ? "§d" + sigils + "/" + DelveForge.MAX_SIGILS + " sigils"
                        : "§6" + marks + "/" + DelveForge.MAX_MARKS + " marks"));

        if (!forged) {
            boolean can = DelveForge.canForge(held);
            long cost = DelveForge.forgeCost(held);
            gui.setSlot(21, GuiKit.action(can ? Items.NETHER_STAR : Items.BARRIER,
                    "Delveforge it", can ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.RED,
                    () -> {
                        ItemStack live = player.getMainHandItem();
                        if (!DelveForge.canForge(live)) {
                            say(player, "That cannot be forged.");
                            return;
                        }
                        long price = DelveForge.forgeCost(live);
                        if (!CoinManager.removeCoins(player, price)) {
                            say(player, "That costs " + GuiKit.num(price) + " coins.");
                            return;
                        }
                        DelveForge.forge(live);
                        say(player, "Forged. It answers to the delves now.");
                        ping(player);
                        open(player);
                    },
                    can ? "Costs §6" + GuiKit.num(cost) + " coins" : "Not a piece of gear",
                    "",
                    "A one time step. Afterwards it can",
                    "carry marks, and it gets stronger the",
                    "deeper you take it.",
                    "",
                    "§8Works on anything you already own."));
        } else {
            boolean maxed = marks >= DelveForge.MAX_MARKS && sigils >= DelveForge.MAX_SIGILS;
            long cost = DelveForge.markCost(held);
            boolean afford = CoinManager.hasCoins(player, cost);
            boolean sigil = marks >= DelveForge.MAX_MARKS;

            gui.setSlot(21, GuiKit.action(maxed ? Items.BARRIER
                            : sigil ? Items.ECHO_SHARD : Items.AMETHYST_SHARD,
                    maxed ? "Fully marked" : sigil ? "Add a sigil" : "Add a mark",
                    maxed ? ChatFormatting.GRAY
                            : sigil ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.GOLD,
                    () -> {
                        ItemStack live = player.getMainHandItem();
                        if (!DelveForge.isForged(live)) return;
                        long price = DelveForge.markCost(live);
                        long keys = DelveForge.markKeys(live) * 20L;
                        if (DelveDrops.keyWeight(player) < keys) {
                            say(player, "That also wants " + keys
                                    + " of key shard weight. Secrets pay those.");
                            return;
                        }
                        if (!CoinManager.removeCoins(player, price)) {
                            say(player, "That costs " + GuiKit.num(price) + " coins.");
                            return;
                        }
                        if (!DelveDrops.takeKeys(player, keys)) {
                            CoinManager.giveCoinsQuiet(player, price);
                            say(player, "The key shards would not come out of your bag.");
                            return;
                        }
                        if (!DelveForge.addMark(live)) {
                            CoinManager.giveCoinsQuiet(player, price);
                            say(player, "It will not take another.");
                            return;
                        }
                        say(player, "Marked. " + DelveForge.describe(live));
                        ping(player);
                        open(player);
                    },
                    maxed ? "There is nothing left to add"
                            : (afford ? "§a" : "§c") + "Costs §6" + GuiKit.num(cost) + " coins",
                    maxed ? "" : "§7and §b" + (DelveForge.markKeys(held) * 20L)
                            + " §7key shard weight §8(you have "
                            + DelveDrops.keyWeight(player) + ")",
                    "",
                    sigil ? "A sigil is worth two marks"
                          : "Each mark is worth " + pctOf(DelveForge.MARK_STEP) + " of the item",
                    "",
                    "§8Five marks, then five sigils."));
        }

        gui.setSlot(23, GuiKit.info(Items.WRITABLE_BOOK, "How the numbers work",
                ChatFormatting.WHITE,
                "Three things multiply, and they stack.",
                "",
                "  Forged at all      +" + pctOf(DelveForge.FORGE_STEP),
                "  Each mark          +" + pctOf(DelveForge.MARK_STEP),
                "  Each sigil         +" + pctOf(DelveForge.SIGIL_STEP),
                "  Per depth, inside  +" + pctOf(DelveForge.DEPTH_STEP),
                "",
                "Delve made gear gets a further tenth",
                "again while it is underground, and that",
                "bonus grows with every mark.",
                "",
                "§8All of it works on the surface too. It",
                "§8is simply better where it came from."));

        gui.setSlot(31, GuiKit.info(Items.NAME_TAG, "Why the name changes",
                ChatFormatting.GRAY,
                "Marks show on the item itself, so you",
                "can read somebody's gear across a room",
                "without asking them.",
                "",
                "  §6" + DelveForge.MARK_GLYPH.repeat(3) + " §7three marks",
                "  §d" + DelveForge.SIGIL_GLYPH.repeat(2) + " §7two sigils",
                "",
                "§8The original name is kept underneath, so",
                "§8the marks are rebuilt rather than added to."));

        GuiKit.footer(gui, SIZE, player, () -> DelveGui.open(player));
        gui.open();
    }

    private static String pctOf(double step) {
        return Math.round(step * 100) + "%";
    }

    private static String pct(double mult) {
        return Math.round(mult * 100) + "%";
    }

    private static void say(ServerPlayer p, String msg) {
        p.sendSystemMessage(Component.literal("§5✦ §7" + msg));
    }

    private static void ping(ServerPlayer p) {
        if (p.level() instanceof ServerLevel level) {
            level.playSound(null, p.blockPosition(), SoundEvents.ANVIL_USE,
                    SoundSource.PLAYERS, 0.7f, 1.4f);
        }
    }
}
