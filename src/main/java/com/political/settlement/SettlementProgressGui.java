package com.political.settlement;
import com.political.CitizenManager;
import com.political.GuiKit;
import com.political.government.Treasury;



import com.political.economy.CoinManager;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * Why a settlement is the tier it is, and what it would take to be the next one.
 *
 * <p>The tier system was not broken so much as invisible. Nine tiers each gate on five numbers, and
 * several more gates sit on top of those, but nothing anywhere told a player which of the nine
 * conditions was the one holding them at Hamlet. People built a perfectly good town, watched the
 * name never change, and concluded the whole thing did not work.</p>
 *
 * <p>So this screen exists to answer exactly one question: what is stopping me. Every requirement
 * for the next tier is listed with its current value against its target, met ones in green and the
 * blocking ones in red, and the growth section explains in a sentence why new people are or are not
 * moving in.</p>
 */
public final class SettlementProgressGui {

    private SettlementProgressGui() {}

    private static final int SIZE = 54;

    public static void open(ServerPlayer player, Settlement s) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        SettlementTier tier = s.tierEnum();
        SettlementTier next = nextOf(tier);

        gui.setTitle(Component.literal(s.name).withStyle(tier.color, ChatFormatting.BOLD));
        GuiKit.frame(gui, SIZE);

        GuiKit.header(gui, Items.CAMPFIRE, s.name, tier.color,
                "Currently " + tier.displayName,
                next == tier ? "This is the highest tier" : "Next: " + next.displayName);

        // ── The five headline numbers ─────────────────────────────────────
        req(gui, 10, Items.RED_BED, "Beds", s.beds, next.minBeds, tier == next,
                "Sheltered beds. A bed with no roof or walls does not count.");
        req(gui, 11, Items.PLAYER_HEAD, "Population", s.population, next.minPop, tier == next,
                "Citizens living here. They move in on their own.");
        req(gui, 12, Items.EMERALD, "Appeal", s.appeal, next.minAppeal, tier == next,
                "Scored from what is built, with diminishing returns per block type.");
        reqLong(gui, 13, Items.GOLD_BLOCK, "Treasury", s.treasury, next.minTreasury, tier == next,
                "Earned by working citizens over time.");
        req(gui, 14, Items.BEACON, "Wonders", s.wonders, next.minWonders, tier == next,
                "Large monuments. Only needed at the very top.");

        // ── The extra gates that catch people out ─────────────────────────
        gui.setSlot(19, gate(Items.CRAFTING_TABLE, "Job sites", s.jobSites,
                next.isAtLeast(SettlementTier.CITY) ? (next.isAtLeast(SettlementTier.UTOPIA) ? 32
                        : next.isAtLeast(SettlementTier.METROPOLIS) ? 16 : 8) : 0,
                "Workstations. Citizens need somewhere to work."));

        gui.setSlot(20, gate(Items.CAKE, "Happiness", s.happiness,
                next.isAtLeast(SettlementTier.UTOPIA) ? 70
                        : next.isAtLeast(SettlementTier.METROPOLIS) ? 50 : 45,
                "Below 45 nobody new moves in at all."));

        gui.setSlot(21, gate(Items.GRASS_BLOCK, "Footprint", s.footprintCells,
                next.isAtLeast(SettlementTier.UTOPIA) ? 20
                        : next.isAtLeast(SettlementTier.METROPOLIS) ? 8 : 0,
                "How spread out the build is. Stops a tower counting as a city."));

        gui.setSlot(22, gate(Items.BREAD, "Food", s.food,
                s.population >= SettlementEconomy.SUBSISTENCE_POP ? s.population : 0,
                "Only matters once there are " + SettlementEconomy.SUBSISTENCE_POP
                        + " or more people."));

        boolean needsPlayer = next.isAtLeast(SettlementTier.CITY);
        gui.setSlot(23, gate(Items.NAME_TAG, "Registered players", s.registered().size(),
                needsPlayer ? 1 : 0,
                "A city needs at least one player registered to it."));

        // ── Growth, in a sentence ─────────────────────────────────────────
        String blocker = SettlementEconomy.growthBlocker(s);
        gui.setSlot(31, new GuiElementBuilder(blocker == null ? Items.LIME_DYE : Items.RED_DYE)
                .setName(Component.literal(blocker == null ? "People are moving in" : "Growth is stalled")
                        .withStyle(blocker == null ? ChatFormatting.GREEN : ChatFormatting.RED,
                                ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal(blocker == null
                        ? "§7New citizens arrive on their own."
                        : "§c" + blocker))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Free beds: §f" + Math.max(0, s.beds - s.population)))
                .addLoreLine(Component.literal("§7Happiness: §f" + s.happiness))
                .build());

        // ── Actions ───────────────────────────────────────────────────────
        gui.setSlot(38, GuiKit.action(Items.SPYGLASS, "Rescan now", ChatFormatting.AQUA,
                () -> {
                    try {
                        // Scan the settlement's own world. Scanning whichever world the player is
                        // standing in would read the wrong blocks entirely and then write the
                        // result back as if it were this settlement's.
                        var server = ((ServerLevel) player.level()).getServer();
                        ServerLevel home = server == null ? null
                                : SettlementManager.levelFor(server, s);
                        if (home == null) {
                            player.sendSystemMessage(Component.literal(
                                    "§cCannot reach that settlement's world."));
                        } else if (!home.hasChunkAt(s.cornerstonePos())) {
                            player.sendSystemMessage(Component.literal(
                                    "§cThat area is not loaded, so there is nothing to count."));
                        } else {
                            SettlementManager.rescanNow(home, s);
                            player.sendSystemMessage(Component.literal("§aRescanned. " + s.name
                                    + " is " + s.tierEnum().displayName + "."));
                        }
                    } catch (Exception e) {
                        player.sendSystemMessage(Component.literal("§cScan failed: " + e.getMessage()));
                    }
                    open(player, s);
                },
                "Recounts beds, appeal and job sites",
                "and recomputes the tier immediately"));

        long cost = recruitCost(s);
        gui.setSlot(40, GuiKit.action(Items.EMERALD, "Recruit a settler", ChatFormatting.GREEN,
                () -> {
                    String why = SettlementEconomy.growthBlocker(s);
                    if (why != null) {
                        player.sendSystemMessage(Component.literal("§cCannot recruit. " + why));
                        return;
                    }
                    if (!CoinManager.removeCoins(player, cost)) {
                        player.sendSystemMessage(Component.literal("§cThat costs "
                                + GuiKit.num(cost) + " coins."));
                        return;
                    }
                    try {
                        // The settlement's own level, not the player's. Someone can open this
                        // screen for a settlement in another dimension, and spawning into
                        // whichever world they happen to be standing in would be wrong.
                        var server = ((ServerLevel) player.level()).getServer();
                        ServerLevel home = server == null ? null
                                : SettlementManager.levelFor(server, s);
                        var spawned = home == null ? null
                                : CitizenManager.spawnWanderInCitizen(home, s);
                        if (spawned == null) {
                            // Refuses when the area is not loaded, so refund rather than charge
                            // for a settler that never turned up.
                            CoinManager.giveCoins(player, cost);
                            player.sendSystemMessage(Component.literal(
                                    "§cNobody came. That area is not loaded. Coins refunded."));
                        } else {
                            player.sendSystemMessage(Component.literal(
                                    "§aA settler arrives in " + s.name + "."));
                        }
                    } catch (Exception e) {
                        CoinManager.giveCoins(player, cost);
                        player.sendSystemMessage(Component.literal("§cNobody came. Coins refunded."));
                    }
                    open(player, s);
                },
                GuiKit.num(cost) + " coins",
                "Waiting still works and is free.",
                "This is for when you would rather not."));

        gui.setSlot(42, GuiKit.tile(Items.GOLD_INGOT, "Economy", ChatFormatting.GOLD,
                () -> SettlementEconomyGui.open(player, s),
                "Jobs, yield and happiness"));

        GuiKit.footer(gui, SIZE, player, null);
        gui.open();
    }

    /** Recruiting scales with size so it never replaces actually building the place. */
    private static long recruitCost(Settlement s) {
        return 2_000L + (long) s.population * s.population * 120L;
    }

    private static SettlementTier nextOf(SettlementTier t) {
        SettlementTier[] all = SettlementTier.values();
        int i = t.ordinal();
        return i + 1 < all.length ? all[i + 1] : t;
    }

    private static void req(SimpleGui gui, int slot, net.minecraft.world.item.Item icon,
                            String label, int have, int need, boolean maxed, String note) {
        gui.setSlot(slot, bar(icon, label, have, need, maxed, note));
    }

    private static void reqLong(SimpleGui gui, int slot, net.minecraft.world.item.Item icon,
                                String label, long have, long need, boolean maxed, String note) {
        gui.setSlot(slot, bar(icon, label, have, need, maxed, note));
    }

    private static GuiElement bar(net.minecraft.world.item.Item icon, String label,
                                  long have, long need, boolean maxed, String note) {
        boolean met = have >= need;
        GuiElementBuilder b = new GuiElementBuilder(icon)
                .setName(Component.literal(label).withStyle(
                        maxed ? ChatFormatting.GRAY : met ? ChatFormatting.GREEN : ChatFormatting.RED,
                        ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""));

        if (maxed) {
            b.addLoreLine(Component.literal("§7Have §f" + GuiKit.num(have)));
            b.addLoreLine(Component.literal("§8Already at the highest tier."));
        } else if (need <= 0) {
            b.addLoreLine(Component.literal("§7Have §f" + GuiKit.num(have)));
            b.addLoreLine(Component.literal("§aNot required for the next tier."));
        } else {
            b.addLoreLine(Component.literal("§7Have §f" + GuiKit.num(have)
                    + " §8of §f" + GuiKit.num(need)));
            b.addLoreLine(Component.literal(GuiKit.bar(have / (double) need,
                    met ? ChatFormatting.GREEN : ChatFormatting.RED, ChatFormatting.DARK_GRAY)));
            b.addLoreLine(Component.literal(met ? "§a✔ Met" : "§c✖ Short by "
                    + GuiKit.num(need - have)));
        }
        b.addLoreLine(Component.literal(""));
        b.addLoreLine(Component.literal("§8" + note));
        if (met && !maxed) b.glow();
        return b.build();
    }

    private static GuiElement gate(net.minecraft.world.item.Item icon, String label,
                                   int have, int need, String note) {
        return bar(icon, label, have, need, false, note);
    }

    /** The picker, for when a player has more than one settlement in reach. */
    public static void openList(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Settlements")
                .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        GuiKit.frame(gui, SIZE);
        GuiKit.header(gui, Items.CAMPFIRE, "Settlements", ChatFormatting.GREEN,
                "Pick one to see what is holding it back");

        List<GuiElement> entries = new ArrayList<>();
        for (Settlement s : SettlementManager.getAllSettlements()) {
            SettlementTier t = s.tierEnum();
            SettlementTier n = nextOf(t);
            String blocker = SettlementEconomy.growthBlocker(s);
            entries.add(GuiKit.action(Items.CAMPFIRE, s.name, t.color,
                    () -> open(player, s),
                    t.displayName,
                    n == t ? "At the highest tier" : "Working toward " + n.displayName,
                    "Population " + s.population + ", beds " + s.beds + ", appeal " + s.appeal,
                    blocker == null ? "§aGrowing" : "§cStalled: " + blocker));
        }

        GuiKit.paint(gui, entries, 0, GuiKit.LIST_SLOTS.length);
        GuiKit.footer(gui, SIZE, player, null);
        gui.open();
    }
}
