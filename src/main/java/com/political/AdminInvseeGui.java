package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.gui.SignGui;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Map;

/**
 * Full admin invsee: inventory, ender chest, bank, crypto, stocks, wallet (coins).
 */
public final class AdminInvseeGui {

    private static final int ENDER_ROWS = 3;
    private static final int ENDER_COLS = 9;

    public static void openMainMenu(ServerPlayerEntity admin, ServerPlayerEntity target) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X4, admin, false);
        gui.setTitle(Text.literal("Invsee — ").formatted(Formatting.GOLD).append(target.getDisplayName()));

        var border = new GuiElementBuilder(Items.PURPLE_STAINED_GLASS_PANE).setName(Text.literal("")).build();
        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, border);
        }

        var head = new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Text.literal("Inspecting").formatted(Formatting.GOLD, Formatting.BOLD)
                        .append(Text.literal(" ").append(target.getDisplayName())))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Choose a storage or economy view below.").formatted(Formatting.GRAY))
                .build();
        gui.setSlot(4, head);

        gui.setSlot(10, menuBtn(Items.CHEST, "Player inventory", Formatting.GREEN,
                Text.literal("Hotbar, main, armor, off-hand").formatted(Formatting.DARK_GRAY),
                () -> openPlayerInventory(admin, target)));
        gui.setSlot(12, menuBtn(Items.ENDER_CHEST, "Ender chest (27 slots)", Formatting.LIGHT_PURPLE,
                Text.literal("Full width — no clipped slots").formatted(Formatting.DARK_GRAY),
                () -> openEnderChest(admin, target)));
        gui.setSlot(14, menuBtn(Items.GOLD_BLOCK, "Bank + wallet", Formatting.YELLOW,
                Text.literal("Account balances").formatted(Formatting.DARK_GRAY),
                () -> BankGui.openForAdmin(admin, target)));
        gui.setSlot(16, menuBtn(Items.EMERALD, "Set coin amount", Formatting.GREEN,
                Text.literal("Opens numeric prompt").formatted(Formatting.DARK_GRAY),
                () -> openCoinsEditor(admin, target)));

        gui.setSlot(20, menuBtn(Items.AMETHYST_SHARD, "Crypto wallets", Formatting.LIGHT_PURPLE,
                Text.literal("Per-coin balances").formatted(Formatting.DARK_GRAY),
                () -> openCryptoEditor(admin, target)));
        gui.setSlot(22, menuBtn(Items.PAPER, "Stock portfolio", Formatting.AQUA,
                Text.literal("Share holdings").formatted(Formatting.DARK_GRAY),
                () -> openStockEditor(admin, target)));
        gui.setSlot(31, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("Close").formatted(Formatting.RED, Formatting.BOLD))
                .setCallback((a, b, c) -> admin.closeHandledScreen())
                .build());

        gui.open();
    }

    private static GuiElementBuilder menuBtn(net.minecraft.item.Item icon, String title, Formatting color,
                                             Text subtitle, Runnable onClick) {
        return new GuiElementBuilder(icon)
                .setName(Text.literal(title).formatted(color, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(subtitle)
                .setCallback((a, b, c) -> onClick.run());
    }

    public static void openPlayerInventory(ServerPlayerEntity admin, ServerPlayerEntity target) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, admin, false);
        gui.setTitle(Text.literal("Inventory — ").formatted(Formatting.GOLD).append(target.getDisplayName()));
        Runnable redraw = () -> drawPlayerInventory(gui, admin, target);
        gui.setSlot(53, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("Back").formatted(Formatting.GRAY))
                .setCallback((i, t, a) -> openMainMenu(admin, target))
                .build());
        redraw.run();
        gui.open();
    }

    private static void drawPlayerInventory(SimpleGui gui, ServerPlayerEntity admin, ServerPlayerEntity target) {
        var filler = new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE).setName(Text.literal("")).build();
        for (int i = 0; i < 54; i++) {
            if (i == 53) continue;
            gui.setSlot(i, filler);
        }
        for (int inv = 0; inv < 36; inv++) {
            int g = guiSlotForMain(inv);
            if (g >= 0) bindInvSlot(gui, admin, target, inv, g, () -> drawPlayerInventory(gui, admin, target));
        }
        bindInvSlot(gui, admin, target, 36, 32, () -> drawPlayerInventory(gui, admin, target));
        bindInvSlot(gui, admin, target, 37, 23, () -> drawPlayerInventory(gui, admin, target));
        bindInvSlot(gui, admin, target, 38, 14, () -> drawPlayerInventory(gui, admin, target));
        bindInvSlot(gui, admin, target, 39, 5, () -> drawPlayerInventory(gui, admin, target));
        bindInvSlot(gui, admin, target, 40, 49, () -> drawPlayerInventory(gui, admin, target));
    }

    private static int guiSlotForMain(int invIndex) {
        if (invIndex >= 0 && invIndex <= 8) return 45 + invIndex;
        if (invIndex >= 9 && invIndex <= 17) return 10 + (invIndex - 9);
        if (invIndex >= 18 && invIndex <= 26) return 19 + (invIndex - 18);
        if (invIndex >= 27 && invIndex <= 35) return 28 + (invIndex - 27);
        return -1;
    }

    private static void bindInvSlot(SimpleGui gui, ServerPlayerEntity admin, ServerPlayerEntity target,
                                    int invIndex, int guiIndex, Runnable redraw) {
        ItemStack st = target.getInventory().getStack(invIndex);
        GuiElementBuilder b = st.isEmpty()
                ? new GuiElementBuilder(Items.LIGHT_GRAY_STAINED_GLASS_PANE)
                .setName(Text.literal("Empty #" + invIndex).formatted(Formatting.DARK_GRAY))
                : new GuiElementBuilder(st.copy());
        b.setCallback((idx, type, action) -> {
            ItemStack cursor = admin.currentScreenHandler.getCursorStack().copy();
            ItemStack slot = target.getInventory().getStack(invIndex).copy();
            target.getInventory().setStack(invIndex, ItemStack.EMPTY);
            admin.currentScreenHandler.setCursorStack(ItemStack.EMPTY);
            target.getInventory().setStack(invIndex, cursor.isEmpty() ? ItemStack.EMPTY : cursor.copy());
            admin.currentScreenHandler.setCursorStack(slot.isEmpty() ? ItemStack.EMPTY : slot.copy());
            target.getInventory().markDirty();
            redraw.run();
        });
        gui.setSlot(guiIndex, b.build());
    }

    /** Ender chest: slots 0–26 map to GUI rows 0–2 across full 9 columns; row 3 is padding + back. */
    public static void openEnderChest(ServerPlayerEntity admin, ServerPlayerEntity target) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X4, admin, false);
        gui.setTitle(Text.literal("Ender — ").formatted(Formatting.LIGHT_PURPLE).append(target.getDisplayName()));
        Runnable redraw = () -> drawEnder(gui, admin, target);
        redraw.run();
        gui.open();
    }

    private static void drawEnder(SimpleGui gui, ServerPlayerEntity admin, ServerPlayerEntity target) {
        var filler = new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE).setName(Text.literal("")).build();
        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, filler);
        }
        for (int row = 0; row < ENDER_ROWS; row++) {
            for (int col = 0; col < ENDER_COLS; col++) {
                int idx = row * ENDER_COLS + col;
                int gSlot = row * 9 + col;
                final int enderIndex = idx;
                ItemStack st = target.getEnderChestInventory().getStack(enderIndex);
                GuiElementBuilder b = st.isEmpty()
                        ? new GuiElementBuilder(Items.LIGHT_GRAY_STAINED_GLASS_PANE)
                        .setName(Text.literal("Ender #" + enderIndex).formatted(Formatting.DARK_GRAY))
                        : new GuiElementBuilder(st.copy());
                b.setCallback((a, b2, c) -> {
                    ItemStack cursor = admin.currentScreenHandler.getCursorStack().copy();
                    ItemStack slot = target.getEnderChestInventory().getStack(enderIndex).copy();
                    target.getEnderChestInventory().setStack(enderIndex, ItemStack.EMPTY);
                    admin.currentScreenHandler.setCursorStack(ItemStack.EMPTY);
                    target.getEnderChestInventory().setStack(enderIndex, cursor.isEmpty() ? ItemStack.EMPTY : cursor.copy());
                    admin.currentScreenHandler.setCursorStack(slot.isEmpty() ? ItemStack.EMPTY : slot.copy());
                    target.getEnderChestInventory().markDirty();
                    drawEnder(gui, admin, target);
                });
                gui.setSlot(gSlot, b.build());
            }
        }
        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("Back").formatted(Formatting.GRAY))
                .setCallback((i, t, a) -> openMainMenu(admin, target))
                .build());
    }

    public static void openCoinsEditor(ServerPlayerEntity admin, ServerPlayerEntity target) {
        SignGui sign = new SignGui(admin) {
            @Override
            public void onClose() {
                try {
                    int v = Integer.parseInt(getLine(0).getString().trim());
                    CoinManager.setCoins(target.getUuidAsString(), Math.max(0, v));
                    DataManager.save(PoliticalServer.server);
                    admin.sendMessage(Text.literal("Set coins to " + v).formatted(Formatting.GREEN), false);
                } catch (NumberFormatException ignored) {
                    admin.sendMessage(Text.literal("Invalid number").formatted(Formatting.RED), false);
                }
                openMainMenu(admin, target);
            }
        };
        sign.setLine(0, Text.literal(String.valueOf(CoinManager.getCoins(target.getUuidAsString()))));
        sign.open();
    }

    public static void openCryptoEditor(ServerPlayerEntity admin, ServerPlayerEntity target) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, admin, false);
        gui.setTitle(Text.literal("Crypto: ").append(target.getDisplayName()));
        int slot = 10;
        for (CryptoMarket.Crypto c : CryptoMarket.Crypto.values()) {
            if (slot >= 43) break;
            double bal = CryptoMarket.getBalance(target.getUuidAsString(), c.symbol);
            String sym = c.symbol;
            gui.setSlot(slot, new GuiElementBuilder(Items.AMETHYST_SHARD)
                    .setName(Text.literal(c.icon + " " + sym).formatted(Formatting.LIGHT_PURPLE))
                    .addLoreLine(Text.literal("Balance: " + CryptoMarket.formatCrypto(bal)).formatted(Formatting.WHITE))
                    .addLoreLine(Text.literal("Click to set").formatted(Formatting.GRAY))
                    .setCallback((a, b2, c3) -> promptCryptoAmount(admin, target, sym))
                    .build());
            slot++;
            if ((slot + 1) % 9 == 0) slot += 2;
        }
        gui.setSlot(49, new GuiElementBuilder(Items.ARROW).setName(Text.literal("Back"))
                .setCallback((a, b, c) -> openMainMenu(admin, target)).build());
        gui.open();
    }

    private static void promptCryptoAmount(ServerPlayerEntity admin, ServerPlayerEntity target, String symbol) {
        SignGui sign = new SignGui(admin) {
            @Override
            public void onClose() {
                try {
                    double v = Double.parseDouble(getLine(0).getString().trim());
                    CryptoMarket.adminSetWalletBalance(target.getUuidAsString(), symbol, v);
                    CryptoMarket.persistSave(PoliticalServer.server);
                    admin.sendMessage(Text.literal("Set " + symbol + " to " + v).formatted(Formatting.GREEN), false);
                } catch (NumberFormatException ignored) {
                    admin.sendMessage(Text.literal("Invalid number").formatted(Formatting.RED), false);
                }
                openCryptoEditor(admin, target);
            }
        };
        sign.setLine(0, Text.literal(String.valueOf(CryptoMarket.getBalance(target.getUuidAsString(), symbol))));
        sign.open();
    }

    public static void openStockEditor(ServerPlayerEntity admin, ServerPlayerEntity target) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, admin, false);
        gui.setTitle(Text.literal("Stocks: ").append(target.getDisplayName()));
        Map<String, Integer> port = StockMarket.getPortfolio(target.getUuidAsString());
        int slot = 10;
        for (StockMarket.Stock s : StockMarket.Stock.values()) {
            if (slot >= 43) break;
            int sh = port.getOrDefault(s.symbol, 0);
            String sym = s.symbol;
            gui.setSlot(slot, new GuiElementBuilder(Items.MAP)
                    .setName(Text.literal(s.icon + " " + sym).formatted(Formatting.YELLOW))
                    .addLoreLine(Text.literal("Shares: " + sh).formatted(Formatting.WHITE))
                    .addLoreLine(Text.literal("Click to set shares").formatted(Formatting.GRAY))
                    .setCallback((a, b2, c3) -> promptStockShares(admin, target, sym))
                    .build());
            slot++;
            if ((slot + 1) % 9 == 0) slot += 2;
        }
        gui.setSlot(49, new GuiElementBuilder(Items.ARROW).setName(Text.literal("Back"))
                .setCallback((a, b, c) -> openMainMenu(admin, target)).build());
        gui.open();
    }

    private static void promptStockShares(ServerPlayerEntity admin, ServerPlayerEntity target, String symbol) {
        SignGui sign = new SignGui(admin) {
            @Override
            public void onClose() {
                try {
                    int v = Integer.parseInt(getLine(0).getString().trim());
                    StockMarket.adminSetShares(target.getUuidAsString(), symbol, v);
                    StockMarket.persistSave(PoliticalServer.server);
                    admin.sendMessage(Text.literal("Set " + symbol + " shares to " + v).formatted(Formatting.GREEN), false);
                } catch (NumberFormatException ignored) {
                    admin.sendMessage(Text.literal("Invalid integer").formatted(Formatting.RED), false);
                }
                openStockEditor(admin, target);
            }
        };
        int cur = StockMarket.getPortfolio(target.getUuidAsString()).getOrDefault(symbol, 0);
        sign.setLine(0, Text.literal(String.valueOf(cur)));
        sign.open();
    }
}
