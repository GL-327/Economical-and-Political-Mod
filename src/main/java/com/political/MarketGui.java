package com.political;

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
 * V5 Pillar 3 — the Market board. Read-only economics: which settlement is short of what, what
 * every commodity is worth there, and which lane between two settlements is currently the fattest
 * margin. Nothing here buys or sells items, so it cannot act as a coin faucet; it exists so that
 * route decisions in {@link TradeGui} are informed rather than blind.
 */
public final class MarketGui {

    private MarketGui() {}

    private static final int[] LIST_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
    };

    // ── Settlement list ─────────────────────────────────────────────────────

    public static void open(ServerPlayer player) {
        open(player, 0);
    }

    public static void open(ServerPlayer player, int page) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Markets").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        WarGui.fill(gui, 54);

        List<Settlement> markets = tradableSettlements();
        markets.sort((x, y) -> {
            int r = Integer.compare(y.tierEnum().rank(), x.tierEnum().rank());
            return r != 0 ? r : x.name.compareToIgnoreCase(y.name);
        });

        int pages = Math.max(1, (markets.size() + LIST_SLOTS.length - 1) / LIST_SLOTS.length);
        if (page < 0) page = 0;
        if (page >= pages) page = pages - 1;

        Settlement home = CivilizationHubGui.currentOrHome(player);
        gui.setSlot(4, new GuiElementBuilder(Items.EMERALD)
                .setName(Component.literal("Trade Markets").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Every VILLAGE+ settlement runs a market."))
                .addLoreLine(Component.literal("§7Prices move with what a settlement"))
                .addLoreLine(Component.literal("§7actually produces and actually lacks."))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Your market: §f"
                        + (home == null ? "§8none" : home.name)))
                .addLoreLine(Component.literal("§7Markets listed: §f" + markets.size()))
                .glow().build());

        int start = page * LIST_SLOTS.length;
        for (int i = 0; i < LIST_SLOTS.length; i++) {
            int idx = start + i;
            if (idx >= markets.size()) break;
            Settlement s = markets.get(idx);
            gui.setSlot(LIST_SLOTS[i], marketEntry(player, s));
        }

        final int p = page;
        if (page > 0) {
            gui.setSlot(48, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("← Previous page").withStyle(ChatFormatting.YELLOW))
                    .setCallback((a, b, c, d) -> open(player, p - 1)).build());
        }
        if (page < pages - 1) {
            gui.setSlot(50, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("Next page →").withStyle(ChatFormatting.YELLOW))
                    .setCallback((a, b, c, d) -> open(player, p + 1)).build());
        }

        gui.setSlot(45, WarGui.back(() -> TradeGui.open(player)));
        gui.setSlot(49, new GuiElementBuilder(Items.PAPER)
                .setName(Component.literal("How prices work").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Supply comes from what a settlement"))
                .addLoreLine(Component.literal("§7produces: food surplus, goods, workstations,"))
                .addLoreLine(Component.literal("§7footprint, appeal and wonders."))
                .addLoreLine(Component.literal("§7Demand comes from what it lacks:"))
                .addLoreLine(Component.literal("§7hunger, unhappiness, population, tier."))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7A route earns the §fdifference§7 between the"))
                .addLoreLine(Component.literal("§7two prices, per unit of capacity."))
                .build());
        gui.open();
    }

    private static eu.pb4.sgui.api.elements.GuiElement marketEntry(ServerPlayer player, Settlement s) {
        SettlementTier tier = s.tierEnum();
        TradeGood bestExport = null;
        TradeGood bestImport = null;
        int exportScore = Integer.MIN_VALUE;
        int importScore = Integer.MIN_VALUE;
        for (TradeGood g : TradeGood.values()) {
            int surplus = g.supply(s) - g.demand(s);
            if (surplus > exportScore) { exportScore = surplus; bestExport = g; }
            if (-surplus > importScore) { importScore = -surplus; bestImport = g; }
        }

        GuiElementBuilder b = new GuiElementBuilder(Items.BELL)
                .setName(Component.literal(s.name).withStyle(tier.color, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Tier: " + tier.colorCode + tier.displayName))
                .addLoreLine(Component.literal("§7Population: §f" + s.population
                        + "  §7Happiness: §f" + s.happiness + "%"))
                .addLoreLine(Component.literal("§7Caravan capacity: §f"
                        + TradeRouteManager.capacityForTier(tier) + " §8units"))
                .addLoreLine(Component.literal("§7Route slots: §f"
                        + TradeRouteManager.routesOf(s.id).size() + "§7/§f"
                        + TradeRouteManager.maxRoutesForTier(tier)))
                .addLoreLine(Component.literal(""));
        if (bestExport != null) {
            b.addLoreLine(Component.literal("§aExports: " + bestExport.colored()
                    + " §8(" + bestExport.priceAt(s) + "c)"));
        }
        if (bestImport != null) {
            b.addLoreLine(Component.literal("§cWants: " + bestImport.colored()
                    + " §8(" + bestImport.priceAt(s) + "c)"));
        }
        if (TradeRouteManager.hasSeal(s.id)) {
            b.addLoreLine(Component.literal("§6✦ Merchant's Seal active"));
        }
        b.addLoreLine(Component.literal(""));
        b.addLoreLine(Component.literal("§eClick for the full price board"));
        b.setCallback((a, c, d, e) -> openSettlement(player, s));
        return b.build();
    }

    // ── Per-settlement price board ──────────────────────────────────────────

    public static void openSettlement(ServerPlayer player, Settlement s) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal(s.name + " — Market"));
        WarGui.fill(gui, 54);

        SettlementTier tier = s.tierEnum();
        gui.setSlot(4, new GuiElementBuilder(Items.BELL)
                .setName(Component.literal(s.name).withStyle(tier.color, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Tier: " + tier.colorCode + tier.displayName))
                .addLoreLine(Component.literal("§7Treasury: §6" + s.treasury))
                .addLoreLine(Component.literal("§7Food §a" + s.food + "§7/§f" + s.population
                        + "  §7Goods §b" + s.goods))
                .addLoreLine(Component.literal("§7Workstations §f" + s.jobSites
                        + "  §7Appeal §d" + s.appeal))
                .glow().build());

        TradeGood[] goods = TradeGood.values();
        for (int i = 0; i < goods.length && i < LIST_SLOTS.length; i++) {
            TradeGood g = goods[i];
            int sup = g.supply(s);
            int dem = g.demand(s);
            gui.setSlot(LIST_SLOTS[i], new GuiElementBuilder(g.icon)
                    .setName(Component.literal(g.colored()).withStyle(ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7Price here: §6" + g.priceAt(s)
                            + " §8(base " + g.baseValue + ")"))
                    .addLoreLine(Component.literal("§7Supply: " + bar(sup) + " §f" + sup))
                    .addLoreLine(Component.literal("§7Demand: " + bar(dem) + " §f" + dem))
                    .addLoreLine(Component.literal(g.marketNote(s)))
                    .build());
        }

        gui.setSlot(45, WarGui.back(() -> open(player)));
        gui.setSlot(49, new GuiElementBuilder(Items.MINECART)
                .setName(Component.literal("Routes here").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Open trade routes touching this market."))
                .setCallback((a, b, c, d) -> TradeGui.openRoutes(player, s))
                .build());
        gui.open();
    }

    /** Ten-segment coloured bar for a 0..100 value. */
    static String bar(int value) {
        int filled = Math.max(0, Math.min(10, value / 10));
        StringBuilder sb = new StringBuilder();
        String colour = value >= 70 ? "§a" : value >= 35 ? "§e" : "§c";
        sb.append(colour);
        for (int i = 0; i < filled; i++) sb.append('|');
        sb.append("§8");
        for (int i = filled; i < 10; i++) sb.append('|');
        return sb.toString();
    }

    static List<Settlement> tradableSettlements() {
        List<Settlement> out = new ArrayList<>();
        for (Settlement s : SettlementManager.getAllSettlements()) {
            if (TradeRouteManager.capacityForTier(s.tierEnum()) > 0) out.add(s);
        }
        return out;
    }
}
