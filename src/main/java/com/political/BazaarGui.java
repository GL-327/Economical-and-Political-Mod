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
 * The bazaar front end.
 *
 * <p>Four screens. The hub picks a field, the field lists its products with live quotes, the product
 * screen is where the trading happens, and the orders screen is where a player manages what they have
 * posted. Quantities are chosen from fixed buttons rather than typed, because a chest menu is a poor
 * place to enter a number and a misplaced digit here costs real coins.</p>
 */
public final class BazaarGui {

    private BazaarGui() {}

    private static final int SIZE = 54;
    private static final int[] AMOUNTS = {1, 16, 64, 256, 1024};

    // ── Hub ───────────────────────────────────────────────────────────────

    public static void open(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Bazaar").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        GuiKit.frame(gui, SIZE);

        GuiKit.header(gui, Items.GOLD_INGOT, "Bazaar", ChatFormatting.GOLD,
                "Buy and sell raw materials",
                "Your balance: " + GuiKit.num(CoinManager.getCoinsLong(player)) + " coins");

        // Categories come from the merged catalogue, so the six collection fields and the old shop
        // sections sit side by side and a player has one place to look rather than two.
        List<String> cats = BazaarProduct.categories();
        // Stops short of the last row, which already holds My orders and the explainer. Eighteen
        // categories fit comfortably; the cap is here so adding more never quietly eats a button.
        int room = Math.min(GuiKit.LIST_SLOTS.length - 7, cats.size());
        for (int i = 0; i < room; i++) {
            final String cat = cats.get(i);
            gui.setSlot(GuiKit.LIST_SLOTS[i], GuiKit.tile(BazaarProduct.iconFor(cat), cat,
                    ChatFormatting.YELLOW,
                    () -> openField(player, cat, 0),
                    BazaarManager.productsIn(cat).size() + " products",
                    "Live buy and sell quotes"));
        }

        gui.setSlot(39, GuiKit.tile(Items.WRITABLE_BOOK, "My orders", ChatFormatting.AQUA,
                () -> openOrders(player, 0),
                BazaarManager.ordersOf(player).size() + " open",
                "Cancel to reclaim escrow"));

        gui.setSlot(41, GuiKit.info(Items.PAPER, "How it works", ChatFormatting.WHITE,
                "Instant sell fills the best buy orders first.",
                "Instant buy fills the cheapest sell offers first.",
                "The house always quotes, at a poor price,",
                "so the market works with nobody else online.",
                "The bazaar takes " + BazaarManager.TAX_PERCENT + "% of a filled order."));

        GuiKit.footer(gui, SIZE, player, null);
        gui.open();
    }

    // ── One field ─────────────────────────────────────────────────────────

    private static void openField(ServerPlayer player, String field, int page) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Bazaar: " + field)
                .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        GuiKit.frame(gui, SIZE);
        GuiKit.header(gui, BazaarProduct.iconFor(field), field, ChatFormatting.YELLOW,
                "Live quotes", BazaarProduct.inCategory(field).size() + " products", "");

        List<GuiElement> entries = new ArrayList<>();
        for (BazaarProduct c : BazaarManager.productsIn(field)) {
            long buyAt = BazaarManager.instantBuyPrice(c);
            long sellAt = BazaarManager.instantSellPrice(c);
            int held = BazaarManager.countInInventory(player, c);

            entries.add(new GuiElementBuilder(c.material)
                    .setName(Component.literal(c.display).withStyle(c.colour, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§7Buy at §6" + GuiKit.num(buyAt) + " §7each"))
                    .addLoreLine(Component.literal("§7Sell at §6" + GuiKit.num(sellAt) + " §7each"))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§7You hold §f" + held))
                    .addLoreLine(Component.literal("§7Market §6"
                            + GuiKit.num(BazaarTraders.midOf(c)) + " "
                            + (BazaarTraders.momentumOf(c) > 0.05 ? "§a▲"
                               : BazaarTraders.momentumOf(c) < -0.05 ? "§c▼" : "§7▬")))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§eClick to trade"))
                    .setCallback((a, t, ci, sg) -> openProduct(player, c))
                    .build());
        }

        GuiKit.paint(gui, entries, page, GuiKit.LIST_SLOTS.length);
        GuiKit.pager(gui, SIZE, page, entries.size(), GuiKit.LIST_SLOTS.length,
                p -> openField(player, field, p));
        GuiKit.footer(gui, SIZE, player, () -> open(player));
        gui.open();
    }

    // ── One product ───────────────────────────────────────────────────────

    private static void openProduct(ServerPlayer player, BazaarProduct c) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal(c.display).withStyle(c.colour, ChatFormatting.BOLD));
        GuiKit.frame(gui, SIZE);

        long buyAt = BazaarManager.instantBuyPrice(c);
        long sellAt = BazaarManager.instantSellPrice(c);
        int held = BazaarManager.countInInventory(player, c);

        double mom = BazaarTraders.momentumOf(c);
        String arrow = mom > 0.05 ? "§a▲" : mom < -0.05 ? "§c▼" : "§7▬";

        GuiKit.header(gui, c.material, c.display, c.colour,
                c.collection != null ? c.collection.flavor : "Traded on the open book",
                "You hold " + held + ", balance "
                        + GuiKit.num(CoinManager.getCoinsLong(player)) + " coins",
                "Market " + GuiKit.num(BazaarTraders.midOf(c)) + " " + arrow);

        // Sell row.
        gui.setSlot(19, GuiKit.info(Items.HOPPER, "Instant sell", ChatFormatting.GREEN,
                GuiKit.num(sellAt) + " coins each",
                "Fills the best buy orders first"));
        for (int i = 0; i < AMOUNTS.length; i++) {
            final int amount = AMOUNTS[i];
            gui.setSlot(20 + i, GuiKit.action(Items.LIME_CONCRETE, "Sell " + amount,
                    ChatFormatting.GREEN,
                    () -> {
                        BazaarManager.sellInstantly(player, c, amount);
                        openProduct(player, c);
                    },
                    "About " + GuiKit.num(sellAt * amount) + " coins",
                    held < amount ? "§cYou only hold " + held : "You have enough"));
        }
        gui.setSlot(25, GuiKit.action(Items.CHEST, "Sell everything", ChatFormatting.GREEN,
                () -> {
                    BazaarManager.sellInstantly(player, c, held);
                    openProduct(player, c);
                }, "All " + held + " in your inventory"));

        // Buy row.
        gui.setSlot(28, GuiKit.info(Items.DROPPER, "Instant buy", ChatFormatting.GOLD,
                GuiKit.num(buyAt) + " coins each",
                "Fills the cheapest sell offers first"));
        for (int i = 0; i < AMOUNTS.length; i++) {
            final int amount = AMOUNTS[i];
            gui.setSlot(29 + i, GuiKit.action(Items.ORANGE_CONCRETE, "Buy " + amount,
                    ChatFormatting.GOLD,
                    () -> {
                        BazaarManager.buyInstantly(player, c, amount);
                        openProduct(player, c);
                    }, "About " + GuiKit.num(buyAt * amount) + " coins"));
        }

        // Posting your own price.
        gui.setSlot(37, GuiKit.action(Items.WRITABLE_BOOK, "Post a buy order", ChatFormatting.AQUA,
                () -> GuiKit.prompt(player, "Price per unit",
                        text -> {
                            long price = parse(text);
                            if (price <= 0) {
                                player.sendSystemMessage(Component.literal("§cThat is not a price."));
                                openProduct(player, c);
                                return;
                            }
                            BazaarManager.postOrder(player, c, price, 64, true);
                            openProduct(player, c);
                        },
                        () -> openProduct(player, c)),
                "Sixty four units at your price",
                "Coins are held until it fills"));

        gui.setSlot(43, GuiKit.action(Items.WRITABLE_BOOK, "Post a sell offer", ChatFormatting.LIGHT_PURPLE,
                () -> GuiKit.prompt(player, "Price per unit",
                        text -> {
                            long price = parse(text);
                            if (price <= 0) {
                                player.sendSystemMessage(Component.literal("§cThat is not a price."));
                                openProduct(player, c);
                                return;
                            }
                            BazaarManager.postOrder(player, c, price, 64, false);
                            openProduct(player, c);
                        },
                        () -> openProduct(player, c)),
                "Sixty four units at your price",
                "Items are held until it fills"));

        // The book itself, so the price is not a black box.
        gui.setSlot(40, bookElement(c));

        GuiKit.footer(gui, SIZE, player, () -> openField(player, c.category, 0));
        gui.open();
    }

    private static GuiElement bookElement(BazaarProduct c) {
        List<BazaarManager.Order> buys = BazaarManager.book(c, true);
        List<BazaarManager.Order> sells = BazaarManager.book(c, false);

        GuiElementBuilder b = new GuiElementBuilder(Items.BOOK)
                .setName(Component.literal("Order book").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§aBuy orders"));
        if (buys.isEmpty()) {
            b.addLoreLine(Component.literal("  §8none posted, house only"));
        } else {
            for (int i = 0; i < Math.min(4, buys.size()); i++) {
                BazaarManager.Order o = buys.get(i);
                b.addLoreLine(Component.literal("  §7" + o.remaining() + " at §6" + o.unitPrice
                        + " §8" + o.ownerName));
            }
        }
        b.addLoreLine(Component.literal(""));
        b.addLoreLine(Component.literal("§cSell offers"));
        if (sells.isEmpty()) {
            b.addLoreLine(Component.literal("  §8none posted, house only"));
        } else {
            for (int i = 0; i < Math.min(4, sells.size()); i++) {
                BazaarManager.Order o = sells.get(i);
                b.addLoreLine(Component.literal("  §7" + o.remaining() + " at §6" + o.unitPrice
                        + " §8" + o.ownerName));
            }
        }
        return b.build();
    }

    // ── My orders ─────────────────────────────────────────────────────────

    private static void openOrders(ServerPlayer player, int page) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("My orders").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        GuiKit.frame(gui, SIZE);

        List<BazaarManager.Order> mine = BazaarManager.ordersOf(player);
        GuiKit.header(gui, Items.WRITABLE_BOOK, "My orders", ChatFormatting.AQUA,
                mine.size() + " open",
                "Cancelling returns whatever is held");

        List<GuiElement> entries = new ArrayList<>();
        for (BazaarManager.Order o : mine) {
            BazaarProduct c = BazaarProduct.byKey(o.product);
            if (c == null) continue;
            int owed = o.claimable();
            if (owed > 0) {
                // Goods bought while the owner was offline. Collecting comes before cancelling,
                // because a player who sees a full order wants the items, not a refund.
                entries.add(GuiKit.action(c.material,
                        "Collect " + owed + " " + c.display, ChatFormatting.GOLD,
                        () -> {
                            BazaarManager.claim(player, o.id);
                            openOrders(player, 0);
                        },
                        "Your buy order filled while you were away",
                        o.remaining() + " of " + o.amount + " still open",
                        "Click to collect"));
                continue;
            }
            entries.add(GuiKit.action(c.material,
                    (o.buy ? "Buying " : "Selling ") + c.display,
                    o.buy ? ChatFormatting.GREEN : ChatFormatting.LIGHT_PURPLE,
                    () -> GuiKit.confirm(player, "Cancel this order",
                            "The escrow comes straight back to you.",
                            () -> {
                                BazaarManager.cancel(player, o.id);
                                openOrders(player, 0);
                            },
                            () -> openOrders(player, 0)),
                    o.remaining() + " of " + o.amount + " left",
                    "At " + GuiKit.num(o.unitPrice) + " coins each",
                    "Click to cancel"));
        }

        GuiKit.paint(gui, entries, page, GuiKit.LIST_SLOTS.length);
        GuiKit.pager(gui, SIZE, page, entries.size(), GuiKit.LIST_SLOTS.length,
                p -> openOrders(player, p));
        GuiKit.footer(gui, SIZE, player, () -> open(player));
        gui.open();
    }

    private static long parse(String text) {
        try {
            return Long.parseLong(text.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return -1;
        }
    }
}
