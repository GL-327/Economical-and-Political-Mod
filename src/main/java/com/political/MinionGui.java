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
 * The minion menu.
 *
 * <p>Three screens: everything you have placed, one minion in detail, and the catalogue you place
 * from. The detail screen deliberately shows the interval and the cap in plain numbers rather than a
 * vague speed rating, because the only interesting decision here is whether the next tier is worth
 * its price and that is impossible to judge without them.</p>
 */
public final class MinionGui {

    private MinionGui() {}

    private static final int SIZE = 54;

    // ── What you have ─────────────────────────────────────────────────────

    public static void open(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Minions").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        GuiKit.frame(gui, SIZE);

        List<MinionManager.Minion> mine = MinionManager.minionsOf(player);
        int stored = 0;
        for (MinionManager.Minion m : mine) stored += m.stored;

        GuiKit.header(gui, Items.ARMOR_STAND, "Minions", ChatFormatting.GREEN,
                mine.size() + " of " + MinionManager.MAX_PER_PLAYER + " placed",
                stored + " items waiting to be collected");

        List<GuiElement> entries = new ArrayList<>();
        for (MinionManager.Minion m : mine) {
            CollectionType c = m.type();
            if (c == null) continue;
            entries.add(new GuiElementBuilder(c.material)
                    .setName(Component.literal(c.display + " Minion "
                            + CollectionManager.roman(Math.min(9, m.tier)))
                            .withStyle(MinionManager.tierColour(m.tier), ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§7Holding §f" + m.stored + " §8of "
                            + MinionManager.capacity(m.tier)))
                    .addLoreLine(Component.literal("§7Status: " + MinionManager.statusOf(m)))
                    .addLoreLine(Component.literal("§7At §f" + m.x + ", " + m.y + ", " + m.z))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§eClick to manage"))
                    .setCallback((a, t, ci, sg) -> openMinion(player, m))
                    .build());
        }

        GuiKit.paint(gui, entries, 0, GuiKit.LIST_SLOTS.length);

        gui.setSlot(48, GuiKit.action(Items.HOPPER, "Collect everything", ChatFormatting.GOLD,
                () -> {
                    MinionManager.collectAll(player);
                    open(player);
                }, stored + " items waiting"));

        gui.setSlot(50, GuiKit.tile(Items.ARMOR_STAND, "Place a new minion", ChatFormatting.AQUA,
                () -> openCatalogue(player, 0),
                mine.size() >= MinionManager.MAX_PER_PLAYER
                        ? "You are at the limit"
                        : (MinionManager.MAX_PER_PLAYER - mine.size()) + " slots free"));

        GuiKit.footer(gui, SIZE, player, null);
        gui.open();
    }

    // ── One minion ────────────────────────────────────────────────────────

    private static void openMinion(ServerPlayer player, MinionManager.Minion m) {
        CollectionType c = m.type();
        if (c == null) {
            open(player);
            return;
        }

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal(c.display + " Minion").withStyle(c.colour, ChatFormatting.BOLD));
        GuiKit.frame(gui, SIZE);

        GuiKit.header(gui, c.material, c.display + " Minion", MinionManager.tierColour(m.tier),
                "Tier " + m.tier + " of " + MinionManager.MAX_TIER,
                "One every " + MinionManager.intervalSeconds(m.tier) + " seconds",
                "Holds " + MinionManager.capacity(m.tier));

        gui.setSlot(20, GuiKit.info(Items.CHEST, "Storage", ChatFormatting.WHITE,
                m.stored + " of " + MinionManager.capacity(m.tier),
                GuiKit.bar(m.stored / (double) MinionManager.capacity(m.tier),
                        ChatFormatting.GREEN, ChatFormatting.DARK_GRAY),
                m.stored >= MinionManager.capacity(m.tier)
                        ? "§cFull. It has stopped working." : "§aStill working."));

        gui.setSlot(22, GuiKit.action(Items.HOPPER, "Collect", ChatFormatting.GOLD,
                () -> {
                    MinionManager.collect(player, m.id);
                    openMinion(player, m);
                }, m.stored + " waiting"));

        if (m.tier < MinionManager.MAX_TIER) {
            long cost = MinionManager.upgradeCost(c, m.tier);
            gui.setSlot(24, GuiKit.action(Items.ANVIL, "Upgrade to tier " + (m.tier + 1),
                    ChatFormatting.AQUA,
                    () -> {
                        MinionManager.upgrade(player, m.id);
                        openMinion(player, m);
                    },
                    GuiKit.num(cost) + " coins",
                    "One every " + MinionManager.intervalSeconds(m.tier + 1) + " seconds",
                    "Holds " + MinionManager.capacity(m.tier + 1)));
        } else {
            gui.setSlot(24, GuiKit.info(Items.NETHER_STAR, "Fully upgraded",
                    ChatFormatting.LIGHT_PURPLE, "Tier " + MinionManager.MAX_TIER,
                    "Nothing further to buy here"));
        }

        gui.setSlot(31, GuiKit.action(Items.ENDER_PEARL, "Teleport to it", ChatFormatting.LIGHT_PURPLE,
                () -> {
                    PlayerTeleportHelper.teleport(player,
                            (net.minecraft.server.level.ServerLevel) player.level(),
                            m.x + 0.5, m.y + 1, m.z + 0.5, player.getYRot(), player.getXRot());
                    player.closeContainer();
                }, "Only within this dimension"));

        gui.setSlot(40, GuiKit.action(Items.BARRIER, "Pick it up", ChatFormatting.RED,
                () -> GuiKit.confirm(player, "Pick up this minion",
                        "You get what it is holding and half of what it cost.",
                        () -> {
                            MinionManager.pickUp(player, m.id);
                            open(player);
                        },
                        () -> openMinion(player, m)),
                "Half the placement cost comes back"));

        GuiKit.footer(gui, SIZE, player, () -> open(player));
        gui.open();
    }

    // ── The catalogue ─────────────────────────────────────────────────────

    private static void openCatalogue(ServerPlayer player, int page) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Place a minion")
                .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        GuiKit.frame(gui, SIZE);

        GuiKit.header(gui, Items.ARMOR_STAND, "Place a minion", ChatFormatting.AQUA,
                "It is placed where you are standing",
                "Balance " + GuiKit.num(CoinManager.getCoinsLong(player)) + " coins");

        List<GuiElement> entries = new ArrayList<>();
        for (CollectionType c : CollectionType.values()) {
            long cost = MinionManager.placementCost(c);
            entries.add(GuiKit.action(c.material, c.display + " Minion", c.colour,
                    () -> GuiKit.confirm(player, "Place a " + c.display + " minion",
                            "Costs " + GuiKit.num(cost) + " coins, placed where you stand.",
                            () -> {
                                MinionManager.place(player, c);
                                open(player);
                            },
                            () -> openCatalogue(player, page)),
                    GuiKit.num(cost) + " coins",
                    "Produces " + c.display.toLowerCase(),
                    "Feeds your " + c.display + " collection"));
        }

        GuiKit.paint(gui, entries, page, GuiKit.LIST_SLOTS.length);
        GuiKit.pager(gui, SIZE, page, entries.size(), GuiKit.LIST_SLOTS.length,
                p -> openCatalogue(player, p));
        GuiKit.footer(gui, SIZE, player, () -> open(player));
        gui.open();
    }
}
