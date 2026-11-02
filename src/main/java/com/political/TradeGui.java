package com.political;


import com.political.economy.CoinManager;
import com.political.settlement.Settlement;
import com.political.settlement.SettlementManager;
import com.political.settlement.SettlementTier;
import com.political.settlement.WarGui;
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
 * V5 Pillar 3 — the Trade Hall. Every part of the caravan system is reachable and operable from
 * here by clicking: open a route, close a route, read a route's economics, apply a Merchant's
 * Seal, watch caravans on the road, and jump to the market price boards.
 */
public final class TradeGui {

    private TradeGui() {}

    private static final int[] LIST_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
    };

    // ── Main hall ───────────────────────────────────────────────────────────

    public static void open(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Trade Hall").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        WarGui.fill(gui, 54);

        Settlement home = CivilizationHubGui.currentOrHome(player);
        List<TradeRouteManager.TradeRoute> myRoutes =
                home == null ? new ArrayList<>() : TradeRouteManager.routesOf(home.id);
        List<CaravanManager.Caravan> caravans = CaravanManager.activeCaravans();

        gui.setSlot(4, new GuiElementBuilder(Items.CHEST_MINECART)
                .setName(Component.literal("Trade Hall").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Settlement: §f"
                        + (home == null ? "§8none" : home.name)))
                .addLoreLine(Component.literal("§7Your routes: §f" + myRoutes.size()
                        + (home == null ? "" : "§7/§f"
                        + TradeRouteManager.maxRoutesForTier(home.tierEnum()))))
                .addLoreLine(Component.literal("§7Caravans on the road: §f" + caravans.size()))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§8Route income goes to settlement treasuries,"))
                .addLoreLine(Component.literal("§8never to a player's purse."))
                .glow().build());

        gui.setSlot(19, new GuiElementBuilder(Items.MINECART)
                .setName(Component.literal("My Routes").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Profit, upkeep, risk and history"))
                .addLoreLine(Component.literal("§7for every route you hold."))
                .addLoreLine(Component.literal(home == null
                        ? "§8Register to a settlement first."
                        : "§eClick to open"))
                .setCallback((a, b, c, d) -> {
                    if (home == null) {
                        player.sendSystemMessage(Component.literal("§7You belong to no settlement."));
                        return;
                    }
                    openRoutes(player, home);
                })
                .build());

        gui.setSlot(21, new GuiElementBuilder(Items.PAPER)
                .setName(Component.literal("Open a Route").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Charter a caravan road to another"))
                .addLoreLine(Component.literal("§7settlement and pick the good it carries."))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Requires: §fCaravan Charter §8(craftable)"))
                .addLoreLine(Component.literal("§7You hold: §f"
                        + TradeItems.count(player, TradeItems.ID_CHARTER)))
                .addLoreLine(Component.literal("§7Plus §625,000–250,000 §7coins by distance."))
                .setCallback((a, b, c, d) -> {
                    if (home == null) {
                        player.sendSystemMessage(Component.literal("§7You belong to no settlement."));
                        return;
                    }
                    openPartnerPicker(player, home, 0);
                })
                .build());

        gui.setSlot(23, new GuiElementBuilder(Items.LEAD)
                .setName(Component.literal("Caravans in Transit").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Where every caravan is right now,"))
                .addLoreLine(Component.literal("§7what it carries and how far it has come."))
                .setCallback((a, b, c, d) -> openCaravans(player, 0))
                .build());

        gui.setSlot(25, new GuiElementBuilder(Items.EMERALD)
                .setName(Component.literal("Markets & Prices").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Supply, demand and price for every"))
                .addLoreLine(Component.literal("§7commodity in every settlement."))
                .setCallback((a, b, c, d) -> MarketGui.open(player))
                .build());

        boolean sealed = home != null && TradeRouteManager.hasSeal(home.id);
        gui.setSlot(31, new GuiElementBuilder(Items.HONEYCOMB)
                .setName(Component.literal("Apply Merchant's Seal")
                        .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Consumes a §fMerchant's Seal §7to give"))
                .addLoreLine(Component.literal("§7this settlement §a+12% §7delivered route"))
                .addLoreLine(Component.literal("§7value for §f24 hours§7."))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7You hold: §f"
                        + TradeItems.count(player, TradeItems.ID_SEAL)))
                .addLoreLine(Component.literal(sealed
                        ? "§6Active for " + formatDuration(TradeRouteManager.sealRemainingMs(home.id))
                        : "§8No seal active here."))
                .setCallback((a, b, c, d) -> applySeal(player, home))
                .build());

        long cd = CaravanManager.plunderCooldownSeconds(player.getUUID().toString());
        gui.setSlot(33, new GuiElementBuilder(Items.IRON_SWORD)
                .setName(Component.literal("Ambush & Plunder").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Destroy a caravan on the road to take"))
                .addLoreLine(Component.literal("§7its cargo."))
                .addLoreLine(Component.literal("§7At war with its polity: §f50% §7+ treasury cut."))
                .addLoreLine(Component.literal("§7Not at war: §f20%§7, or §f55% §7with a"))
                .addLoreLine(Component.literal("§7§fLetter of Marque §8(dropped, not craftable)."))
                .addLoreLine(Component.literal("§8Nothing at all inside a settlement's walls."))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Letters held: §f"
                        + TradeItems.count(player, TradeItems.ID_MARQUE)))
                .addLoreLine(Component.literal(cd > 0
                        ? "§cRaid cooldown: " + cd + "s"
                        : "§aReady to raid"))
                .build());

        gui.setSlot(45, WarGui.back(() -> CivilizationHubGui.open(player)));
        gui.open();
    }

    // ── Route list ──────────────────────────────────────────────────────────

    public static void openRoutes(ServerPlayer player, Settlement s) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal(s.name + " — Routes"));
        WarGui.fill(gui, 54);

        List<TradeRouteManager.TradeRoute> routes = TradeRouteManager.routesOf(s.id);
        SettlementTier tier = s.tierEnum();

        gui.setSlot(4, new GuiElementBuilder(Items.MINECART)
                .setName(Component.literal(s.name + " — Trade Routes")
                        .withStyle(tier.color, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Slots used: §f" + routes.size() + "§7/§f"
                        + TradeRouteManager.maxRoutesForTier(tier)))
                .addLoreLine(Component.literal("§7Caravan capacity: §f"
                        + TradeRouteManager.capacityForTier(tier) + " §8units per load"))
                .addLoreLine(Component.literal("§7Treasury: §6" + s.treasury))
                .glow().build());

        if (routes.isEmpty()) {
            gui.setSlot(22, new GuiElementBuilder(Items.BARRIER)
                    .setName(Component.literal("No routes").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7Open one from the Trade Hall."))
                    .build());
        }
        for (int i = 0; i < routes.size() && i < LIST_SLOTS.length; i++) {
            TradeRouteManager.TradeRoute r = routes.get(i);
            gui.setSlot(LIST_SLOTS[i], routeEntry(player, s, r));
        }

        gui.setSlot(45, WarGui.back(() -> open(player)));
        gui.open();
    }

    private static eu.pb4.sgui.api.elements.GuiElement routeEntry(ServerPlayer player, Settlement s,
                                                                  TradeRouteManager.TradeRoute r) {
        Settlement other = SettlementManager.get(r.other(s.id));
        Settlement origin = TradeRouteManager.originOf(r);
        Settlement dest = TradeRouteManager.destinationOf(r);
        TradeGood good = r.goodEnum();
        long cargo = TradeRouteManager.projectedCargoValue(r, origin, dest);
        long upkeep = TradeRouteManager.upkeepPerCycle(r);
        int risk = TradeRouteManager.riskOf(r);
        boolean rolling = CaravanManager.hasActiveCaravan(r.id);

        GuiElementBuilder b = new GuiElementBuilder(good.icon)
                .setName(Component.literal(good.colored() + " → "
                        + (other == null ? "?" : other.name)).withStyle(ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Distance: §f"
                        + Math.round(TradeRouteManager.distanceOf(r)) + " §8blocks"))
                .addLoreLine(Component.literal("§7Flow: §f"
                        + (origin == null ? "?" : origin.name) + " §7→ §f"
                        + (dest == null ? "?" : dest.name)))
                .addLoreLine(Component.literal("§7Next cargo: §6" + cargo + " §8coins"))
                .addLoreLine(Component.literal("§7Upkeep: §c-" + upkeep
                        + " §8coins/cycle from each treasury"))
                .addLoreLine(Component.literal("§7Risk: " + MarketGui.bar(risk) + " §f" + risk + "%"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Deliveries: §f" + r.deliveries
                        + "  §7Plundered: §c" + r.plundered + "  §7Lost: §8" + r.lost))
                .addLoreLine(Component.literal("§7Earned: §6" + (r.totalEarnedA + r.totalEarnedB)
                        + " §8(upkeep paid " + r.totalUpkeepPaid + ")"));

        if (r.suspended) {
            b.addLoreLine(Component.literal("§c⚠ Suspended: " + r.suspendReason));
        } else if (rolling) {
            b.addLoreLine(Component.literal("§a✦ A caravan is on the road"));
        } else if (cargo <= 0) {
            b.addLoreLine(Component.literal("§8Unprofitable right now — no caravan will leave."));
        }
        b.addLoreLine(Component.literal(""));
        b.addLoreLine(Component.literal("§cClick to close this route"));
        b.setCallback((a, c, d, e) -> openCancelConfirm(player, s, r));
        return b.build();
    }

    private static void openCancelConfirm(ServerPlayer player, Settlement s,
                                          TradeRouteManager.TradeRoute r) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal("Close route?"));
        WarGui.fill(gui, 27);

        Settlement other = SettlementManager.get(r.other(s.id));
        gui.setSlot(13, new GuiElementBuilder(r.goodEnum().icon)
                .setName(Component.literal(r.goodEnum().colored() + " → "
                        + (other == null ? "?" : other.name)).withStyle(ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Closing frees a route slot at both ends."))
                .addLoreLine(Component.literal("§cThe charter is not refunded."))
                .build());

        gui.setSlot(11, new GuiElementBuilder(Items.LIME_CONCRETE)
                .setName(Component.literal("Confirm").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .setCallback((a, b, c, d) -> {
                    TradeRouteManager.cancel(player, r);
                    openRoutes(player, s);
                })
                .build());
        gui.setSlot(15, new GuiElementBuilder(Items.RED_CONCRETE)
                .setName(Component.literal("Cancel").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .setCallback((a, b, c, d) -> openRoutes(player, s))
                .build());
        gui.open();
    }

    // ── Establish flow ──────────────────────────────────────────────────────

    public static void openPartnerPicker(ServerPlayer player, Settlement home, int page) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Choose a partner"));
        WarGui.fill(gui, 54);

        List<Settlement> options = new ArrayList<>();
        for (Settlement s : MarketGui.tradableSettlements()) {
            if (s.id.equals(home.id)) continue;
            if (!s.worldDim.equals(home.worldDim)) continue;
            options.add(s);
        }
        options.sort((x, y) -> Double.compare(home.distanceSqTo(x), home.distanceSqTo(y)));

        int pages = Math.max(1, (options.size() + LIST_SLOTS.length - 1) / LIST_SLOTS.length);
        if (page < 0) page = 0;
        if (page >= pages) page = pages - 1;

        gui.setSlot(4, new GuiElementBuilder(Items.COMPASS)
                .setName(Component.literal("Partner Settlement")
                        .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7From: §f" + home.name))
                .addLoreLine(Component.literal("§7Cost rises with distance, and so does profit."))
                .addLoreLine(Component.literal("§7Both ends must have a free route slot."))
                .glow().build());

        int start = page * LIST_SLOTS.length;
        for (int i = 0; i < LIST_SLOTS.length; i++) {
            int idx = start + i;
            if (idx >= options.size()) break;
            Settlement s = options.get(idx);
            long cost = TradeRouteManager.establishCost(home, s);
            int slotsUsed = TradeRouteManager.routesOf(s.id).size();
            int slotsMax = TradeRouteManager.maxRoutesForTier(s.tierEnum());
            boolean full = slotsUsed >= slotsMax;
            boolean embargo = TradeRouteManager.embargoed(home, s);

            GuiElementBuilder b = new GuiElementBuilder(full || embargo ? Items.BARRIER : Items.BELL)
                    .setName(Component.literal(s.name).withStyle(s.tierEnum().color, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7Tier: " + s.tierEnum().colorCode
                            + s.tierEnum().displayName))
                    .addLoreLine(Component.literal("§7Distance: §f"
                            + Math.round(Math.sqrt(home.distanceSqTo(s))) + " §8blocks"))
                    .addLoreLine(Component.literal("§7Charter cost: §6" + String.format("%,d", cost)))
                    .addLoreLine(Component.literal("§7Capacity: §f"
                            + TradeRouteManager.routeCapacity(home, s) + " §8units"))
                    .addLoreLine(Component.literal("§7Their slots: §f" + slotsUsed + "§7/§f" + slotsMax));
            if (embargo) {
                b.addLoreLine(Component.literal("§c⚠ Embargoed — at war with your polity"));
            } else if (full) {
                b.addLoreLine(Component.literal("§cNo free route slots there"));
            } else {
                b.addLoreLine(Component.literal(""));
                b.addLoreLine(Component.literal("§eClick to choose a commodity"));
                b.setCallback((a, c, d, e) -> openGoodPicker(player, home, s));
            }
            gui.setSlot(LIST_SLOTS[i], b.build());
        }

        final int p = page;
        if (page > 0) {
            gui.setSlot(48, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("← Previous page").withStyle(ChatFormatting.YELLOW))
                    .setCallback((a, b, c, d) -> openPartnerPicker(player, home, p - 1)).build());
        }
        if (page < pages - 1) {
            gui.setSlot(50, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("Next page →").withStyle(ChatFormatting.YELLOW))
                    .setCallback((a, b, c, d) -> openPartnerPicker(player, home, p + 1)).build());
        }

        gui.setSlot(45, WarGui.back(() -> open(player)));
        gui.open();
    }

    public static void openGoodPicker(ServerPlayer player, Settlement home, Settlement partner) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Choose a commodity"));
        WarGui.fill(gui, 54);

        int cap = TradeRouteManager.routeCapacity(home, partner);
        gui.setSlot(4, new GuiElementBuilder(Items.EMERALD)
                .setName(Component.literal(home.name + " ⇄ " + partner.name)
                        .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Capacity: §f" + cap + " §8units per caravan"))
                .addLoreLine(Component.literal("§7Pick the good with the widest price gap."))
                .addLoreLine(Component.literal("§8Margins move as both settlements change."))
                .glow().build());

        TradeGood[] goods = TradeGood.values();
        for (int i = 0; i < goods.length && i < LIST_SLOTS.length; i++) {
            TradeGood g = goods[i];
            int here = g.priceAt(home);
            int there = g.priceAt(partner);
            int margin = Math.abs(there - here);
            long perLoad = Math.min(TradeRouteManager.MAX_DELIVERY_VALUE, (long) margin * cap);
            String direction = here <= there
                    ? home.name + " → " + partner.name
                    : partner.name + " → " + home.name;

            GuiElementBuilder b = new GuiElementBuilder(g.icon)
                    .setName(Component.literal(g.colored()).withStyle(ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7" + home.name + ": §6" + here))
                    .addLoreLine(Component.literal("§7" + partner.name + ": §6" + there))
                    .addLoreLine(Component.literal("§7Margin: §f" + margin + " §8/unit"))
                    .addLoreLine(Component.literal("§7Flows: §f" + direction))
                    .addLoreLine(Component.literal("§7Rough load value: §6" + perLoad))
                    .addLoreLine(Component.literal(""));
            if (margin <= 0 || cap <= 0) {
                b.addLoreLine(Component.literal("§8No margin — this lane would idle."));
            } else {
                b.addLoreLine(Component.literal("§eClick to review the charter"));
                b.setCallback((a, c, d, e) -> openConfirm(player, home, partner, g));
            }
            gui.setSlot(LIST_SLOTS[i], b.build());
        }

        gui.setSlot(45, WarGui.back(() -> openPartnerPicker(player, home, 0)));
        gui.open();
    }

    public static void openConfirm(ServerPlayer player, Settlement home, Settlement partner,
                                   TradeGood good) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal("File the charter?"));
        WarGui.fill(gui, 27);

        long cost = TradeRouteManager.establishCost(home, partner);
        int charters = TradeItems.count(player, TradeItems.ID_CHARTER);
        long coins = CoinManager.getCoinsLong(player);
        String block = TradeRouteManager.blockReason(player, home, partner, good);

        GuiElementBuilder info = new GuiElementBuilder(good.icon)
                .setName(Component.literal(good.colored() + ": " + home.name + " ⇄ " + partner.name)
                        .withStyle(ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Cost: §6" + String.format("%,d", cost) + " §7coins"))
                .addLoreLine(Component.literal("§7You have: §6" + String.format("%,d", coins)))
                .addLoreLine(Component.literal("§7Charters: §f" + charters + " §8(1 consumed)"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Both treasuries pay upkeep every cycle,"))
                .addLoreLine(Component.literal("§7and a caravan takes ~3 minutes to walk."))
                .addLoreLine(Component.literal("§8Income is credited to treasuries, not to you."));
        if (block != null) {
            info.addLoreLine(Component.literal("§c⚠ " + block));
        }
        gui.setSlot(13, info.build());

        gui.setSlot(11, new GuiElementBuilder(Items.LIME_CONCRETE)
                .setName(Component.literal("File charter").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Spends coins and one Caravan Charter."))
                .setCallback((a, b, c, d) -> {
                    if (TradeRouteManager.establish(player, home, partner, good)) {
                        openRoutes(player, home);
                    } else {
                        openGoodPicker(player, home, partner);
                    }
                })
                .build());
        gui.setSlot(15, new GuiElementBuilder(Items.RED_CONCRETE)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .setCallback((a, b, c, d) -> openGoodPicker(player, home, partner))
                .build());
        gui.open();
    }

    // ── Caravans ────────────────────────────────────────────────────────────

    public static void openCaravans(ServerPlayer player, int page) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Caravans in Transit"));
        WarGui.fill(gui, 54);

        List<CaravanManager.Caravan> list = CaravanManager.activeCaravans();
        list.sort((x, y) -> Double.compare(y.progressFraction(), x.progressFraction()));

        int pages = Math.max(1, (list.size() + LIST_SLOTS.length - 1) / LIST_SLOTS.length);
        if (page < 0) page = 0;
        if (page >= pages) page = pages - 1;

        gui.setSlot(4, new GuiElementBuilder(Items.LEAD)
                .setName(Component.literal("Caravans in Transit")
                        .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7On the road: §f" + list.size()))
                .addLoreLine(Component.literal("§7Each haul takes about 3 minutes."))
                .addLoreLine(Component.literal("§8Caravans only appear in the world where"))
                .addLoreLine(Component.literal("§8a player is nearby; elsewhere they travel"))
                .addLoreLine(Component.literal("§8unescorted and deliver less."))
                .glow().build());

        if (list.isEmpty()) {
            gui.setSlot(22, new GuiElementBuilder(Items.BARRIER)
                    .setName(Component.literal("Nothing moving").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7No route has profitable cargo right now."))
                    .build());
        }

        int start = page * LIST_SLOTS.length;
        for (int i = 0; i < LIST_SLOTS.length; i++) {
            int idx = start + i;
            if (idx >= list.size()) break;
            CaravanManager.Caravan c = list.get(idx);
            Settlement from = SettlementManager.get(c.fromId);
            Settlement to = SettlementManager.get(c.toId);
            int pct = (int) Math.round(c.progressFraction() * 100);
            double dist = Math.sqrt(player.distanceToSqr(c.curX, c.curY, c.curZ));

            gui.setSlot(LIST_SLOTS[i], new GuiElementBuilder(c.goodEnum().icon)
                    .setName(Component.literal((from == null ? "?" : from.name) + " → "
                            + (to == null ? "?" : to.name)).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7Cargo: " + c.goodEnum().colored()
                            + " §8worth ~" + c.cargoValue + " coins"))
                    .addLoreLine(Component.literal("§7Progress: " + MarketGui.bar(pct) + " §f" + pct + "%"))
                    .addLoreLine(Component.literal("§7Position: §f" + (int) c.curX + ", "
                            + (int) c.curY + ", " + (int) c.curZ))
                    .addLoreLine(Component.literal("§7Distance from you: §f" + Math.round(dist) + " §8blocks"))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§8Ambush it in the open to plunder the cargo."))
                    .build());
        }

        final int p = page;
        if (page > 0) {
            gui.setSlot(48, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("← Previous page").withStyle(ChatFormatting.YELLOW))
                    .setCallback((a, b, c, d) -> openCaravans(player, p - 1)).build());
        }
        if (page < pages - 1) {
            gui.setSlot(50, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("Next page →").withStyle(ChatFormatting.YELLOW))
                    .setCallback((a, b, c, d) -> openCaravans(player, p + 1)).build());
        }

        gui.setSlot(45, WarGui.back(() -> open(player)));
        gui.open();
    }

    // ── Seal ────────────────────────────────────────────────────────────────

    private static void applySeal(ServerPlayer player, Settlement home) {
        if (home == null) {
            player.sendSystemMessage(Component.literal("§7You belong to no settlement."));
            return;
        }
        if (TradeRouteManager.hasSeal(home.id)) {
            player.sendSystemMessage(Component.literal("§7" + home.name
                    + " already bears a seal for "
                    + formatDuration(TradeRouteManager.sealRemainingMs(home.id)) + "."));
            return;
        }
        if (!TradeItems.consumeOne(player, TradeItems.ID_SEAL)) {
            player.sendSystemMessage(Component.literal(
                    "§cYou are not carrying a §fMerchant's Seal§c. §8(craftable — see /recipe)"));
            return;
        }
        TradeRouteManager.applySeal(home.id);
        player.sendSystemMessage(Component.literal("✦ §6" + home.name
                + " §7bears the Merchant's Seal — §a+12% §7route value for 24 hours."));
        open(player);
    }

    static String formatDuration(long ms) {
        long mins = ms / 60000L;
        long hours = mins / 60L;
        mins = mins % 60L;
        if (hours > 0) return hours + "h " + mins + "m";
        return mins + "m";
    }
}
