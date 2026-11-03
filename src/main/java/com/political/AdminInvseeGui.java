package com.political;


import com.political.economy.BankGui;
import com.political.economy.CoinManager;
import com.political.economy.CryptoMarket;
import com.political.economy.StockMarket;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.gui.SignGui;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.Map;

/**
 * Full admin invsee: inventory, ender chest, bank, crypto, stocks, wallet (coins).
 */
public final class AdminInvseeGui {

    private static final int ENDER_ROWS = 3;
    private static final int ENDER_COLS = 9;

    // ═══════════════════════════════════════════════════════════════
    // OFFLINE PLAYER SUPPORT
    // ═══════════════════════════════════════════════════════════════

    /**
     * Open main menu for offline player (limited to data-driven views: bank, coins, crypto, stocks)
     */
    public static void openOfflineMainMenu(ServerPlayer admin, java.util.UUID uuid, String name) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, admin, false);
        gui.setTitle(Component.literal("Invsee [OFFLINE] — ").withStyle(ChatFormatting.GOLD).append(Component.literal(name)));

        var border = new GuiElementBuilder(Items.STAINED_GLASS_PANE.gray()).setName(Component.literal("")).build();
        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, border);
        }

        var head = new GuiElementBuilder(Items.SKELETON_SKULL)
                .setName(Component.literal("Offline Player: ").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD)
                        .append(Component.literal(name).withStyle(ChatFormatting.WHITE)))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("UUID: " + uuid.toString()).withStyle(ChatFormatting.DARK_GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Limited to data-driven views.").withStyle(ChatFormatting.YELLOW))
                .build();
        gui.setSlot(4, head);

        String uuidStr = uuid.toString();

        // Bank view (works offline via DataManager)
        gui.setSlot(10, menuBtn(Items.GOLD_BLOCK, "Bank + wallet", ChatFormatting.YELLOW,
                Component.literal("Account balances").withStyle(ChatFormatting.DARK_GRAY),
                () -> openOfflineBank(admin, uuidStr, name)));

        // Coins editor (works offline)
        gui.setSlot(12, menuBtn(Items.EMERALD, "Set coin amount", ChatFormatting.GREEN,
                Component.literal("Opens numeric prompt").withStyle(ChatFormatting.DARK_GRAY),
                () -> openOfflineCoinsEditor(admin, uuidStr, name)));

        // Crypto (works offline)
        gui.setSlot(14, menuBtn(Items.AMETHYST_SHARD, "Crypto wallets", ChatFormatting.LIGHT_PURPLE,
                Component.literal("Per-coin balances").withStyle(ChatFormatting.DARK_GRAY),
                () -> openOfflineCryptoEditor(admin, uuidStr, name)));

        // Stocks (works offline)
        gui.setSlot(16, menuBtn(Items.PAPER, "Stock portfolio", ChatFormatting.AQUA,
                Component.literal("Share holdings").withStyle(ChatFormatting.DARK_GRAY),
                () -> openOfflineStockEditor(admin, uuidStr, name)));

        gui.setSlot(22, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("Close").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .setCallback((a, b, containerInput, slotGui) -> admin.closeContainer())
                .build());

        gui.open();
    }

    public static void openOfflineBank(ServerPlayer admin, String uuid, String name) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, admin, false);
        gui.setTitle(Component.literal("Bank [OFFLINE] — ").withStyle(ChatFormatting.YELLOW).append(Component.literal(name)));

        var border = new GuiElementBuilder(Items.STAINED_GLASS_PANE.gray()).setName(Component.literal("")).build();
        for (int i = 0; i < 27; i++) gui.setSlot(i, border);

        long main = DataManager.getBankMain(uuid);
        long savings = DataManager.getBankSavings(uuid);
        int loan = (int) Math.min(Integer.MAX_VALUE, DataManager.getBankLoan(uuid));
        long coins = DataManager.getCoins(uuid);

        gui.setSlot(10, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Component.literal("Main Account").withStyle(ChatFormatting.GOLD))
                .addLoreLine(Component.literal("Balance: " + main).withStyle(ChatFormatting.YELLOW))
                .build());

        gui.setSlot(12, new GuiElementBuilder(Items.GOLD_BLOCK)
                .setName(Component.literal("Savings Account").withStyle(ChatFormatting.GOLD))
                .addLoreLine(Component.literal("Balance: " + savings).withStyle(ChatFormatting.YELLOW))
                .build());

        gui.setSlot(14, new GuiElementBuilder(Items.REDSTONE_BLOCK)
                .setName(Component.literal("Loans").withStyle(ChatFormatting.RED))
                .addLoreLine(Component.literal("Total Owed: " + loan).withStyle(ChatFormatting.RED))
                .build());

        gui.setSlot(16, new GuiElementBuilder(Items.EMERALD)
                .setName(Component.literal("Purse (Coins)").withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal("Balance: " + coins).withStyle(ChatFormatting.GREEN))
                .build());

        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.GRAY))
                .setCallback((a, b, containerInput, slotGui) -> openOfflineMainMenu(admin, java.util.UUID.fromString(uuid), name))
                .build());

        gui.open();
    }

    public static void openOfflineCoinsEditor(ServerPlayer admin, String uuid, String name) {
        SignGui sign = new SignGui(admin) {
            @Override
            public void onManualClose() {
                try {
                    long v = Long.parseLong(getLine(0).getString().trim());
                    CoinManager.setCoins(uuid, Math.max(0, v));
                    DataManager.save(PoliticalServer.server);
                    admin.sendSystemMessage(Component.literal("Set coins to " + v).withStyle(ChatFormatting.GREEN), false);
                } catch (NumberFormatException ignored) {
                    admin.sendSystemMessage(Component.literal("Invalid number").withStyle(ChatFormatting.RED), false);
                }
                openOfflineMainMenu(admin, java.util.UUID.fromString(uuid), name);
            }
        };
        sign.setLine(0, Component.literal(""));
        sign.setLine(1, Component.literal("^^^^^^^^^^^^^^").withStyle(ChatFormatting.YELLOW));
        sign.setLine(2, Component.literal("Set coins").withStyle(ChatFormatting.AQUA));
        sign.setLine(3, Component.literal("Current: " + DataManager.getCoins(uuid)).withStyle(ChatFormatting.GRAY));
        sign.open();
    }

    public static void openOfflineCryptoEditor(ServerPlayer admin, String uuid, String name) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, admin, false);
        gui.setTitle(Component.literal("Crypto [OFFLINE]: ").append(Component.literal(name)));
        int slot = 10;
        for (CryptoMarket.Crypto c : CryptoMarket.Crypto.values()) {
            if (slot >= 43) break;
            double bal = CryptoMarket.getBalance(uuid, c.symbol);
            String sym = c.symbol;
            final String uuidFinal = uuid;
            gui.setSlot(slot, new GuiElementBuilder(Items.AMETHYST_SHARD)
                    .setName(Component.literal(c.icon + " " + sym).withStyle(ChatFormatting.LIGHT_PURPLE))
                    .addLoreLine(Component.literal("Balance: " + CryptoMarket.formatCrypto(bal)).withStyle(ChatFormatting.WHITE))
                    .addLoreLine(Component.literal("Click to set").withStyle(ChatFormatting.GRAY))
                    .setCallback((a, b2, containerInput, slotGui) -> promptOfflineCryptoAmount(admin, uuidFinal, name, sym))
                    .build());
            slot++;
            if ((slot + 1) % 9 == 0) slot += 2;
        }
        gui.setSlot(49, new GuiElementBuilder(Items.ARROW).setName(Component.literal("← Back"))
                .setCallback((a, b, containerInput, slotGui) -> openOfflineMainMenu(admin, java.util.UUID.fromString(uuid), name)).build());
        gui.open();
    }

    private static void promptOfflineCryptoAmount(ServerPlayer admin, String uuid, String name, String symbol) {
        SignGui sign = new SignGui(admin) {
            @Override
            public void onManualClose() {
                try {
                    double v = Double.parseDouble(getLine(0).getString().trim());
                    CryptoMarket.adminSetWalletBalance(uuid, symbol, v);
                    CryptoMarket.persistSave(PoliticalServer.server);
                    admin.sendSystemMessage(Component.literal("Set " + symbol + " to " + v).withStyle(ChatFormatting.GREEN), false);
                } catch (NumberFormatException ignored) {
                    admin.sendSystemMessage(Component.literal("Invalid number").withStyle(ChatFormatting.RED), false);
                }
                openOfflineCryptoEditor(admin, uuid, name);
            }
        };
        sign.setLine(0, Component.literal(""));
        sign.setLine(1, Component.literal("^^^^^^^^^^^^^^").withStyle(ChatFormatting.YELLOW));
        sign.setLine(2, Component.literal("Set " + symbol).withStyle(ChatFormatting.AQUA));
        sign.setLine(3, Component.literal("Cur: " + CryptoMarket.getBalance(uuid, symbol)).withStyle(ChatFormatting.GRAY));
        sign.open();
    }

    public static void openOfflineStockEditor(ServerPlayer admin, String uuid, String name) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, admin, false);
        gui.setTitle(Component.literal("Stocks [OFFLINE]: ").append(Component.literal(name)));
        Map<String, Integer> port = StockMarket.getPortfolio(uuid);
        int slot = 10;
        for (StockMarket.Stock s : StockMarket.Stock.values()) {
            if (slot >= 43) break;
            int sh = port.getOrDefault(s.symbol, 0);
            String sym = s.symbol;
            final String uuidFinal = uuid;
            gui.setSlot(slot, new GuiElementBuilder(Items.MAP)
                    .setName(Component.literal(s.icon + " " + sym).withStyle(ChatFormatting.YELLOW))
                    .addLoreLine(Component.literal("Shares: " + sh).withStyle(ChatFormatting.WHITE))
                    .addLoreLine(Component.literal("Click to set shares").withStyle(ChatFormatting.GRAY))
                    .setCallback((a, b2, containerInput, slotGui) -> promptOfflineStockShares(admin, uuidFinal, name, sym))
                    .build());
            slot++;
            if ((slot + 1) % 9 == 0) slot += 2;
        }
        gui.setSlot(49, new GuiElementBuilder(Items.ARROW).setName(Component.literal("← Back"))
                .setCallback((a, b, containerInput, slotGui) -> openOfflineMainMenu(admin, java.util.UUID.fromString(uuid), name)).build());
        gui.open();
    }

    private static void promptOfflineStockShares(ServerPlayer admin, String uuid, String name, String symbol) {
        SignGui sign = new SignGui(admin) {
            @Override
            public void onManualClose() {
                try {
                    int v = Integer.parseInt(getLine(0).getString().trim());
                    StockMarket.adminSetShares(uuid, symbol, v);
                    StockMarket.persistSave(PoliticalServer.server);
                    admin.sendSystemMessage(Component.literal("Set " + symbol + " shares to " + v).withStyle(ChatFormatting.GREEN), false);
                } catch (NumberFormatException ignored) {
                    admin.sendSystemMessage(Component.literal("Invalid integer").withStyle(ChatFormatting.RED), false);
                }
                openOfflineStockEditor(admin, uuid, name);
            }
        };
        int cur = StockMarket.getPortfolio(uuid).getOrDefault(symbol, 0);
        sign.setLine(0, Component.literal(""));
        sign.setLine(1, Component.literal("^^^^^^^^^^^^^^").withStyle(ChatFormatting.YELLOW));
        sign.setLine(2, Component.literal("Set " + symbol + " shares").withStyle(ChatFormatting.AQUA));
        sign.setLine(3, Component.literal("Current: " + cur).withStyle(ChatFormatting.GRAY));
        sign.open();
    }

    // ═══════════════════════════════════════════════════════════════
    // ONLINE PLAYER SUPPORT
    // ═══════════════════════════════════════════════════════════════

    public static void openMainMenu(ServerPlayer admin, ServerPlayer target) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x4, admin, false);
        gui.setTitle(Component.literal("Invsee — ").withStyle(ChatFormatting.GOLD).append(target.getDisplayName()));

        var border = new GuiElementBuilder(Items.STAINED_GLASS_PANE.purple()).setName(Component.literal("")).build();
        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, border);
        }

        var head = new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Component.literal("Inspecting").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
                        .append(Component.literal(" ").append(target.getDisplayName())))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Choose a storage or economy view below.").withStyle(ChatFormatting.GRAY))
                .build();
        gui.setSlot(4, head);

        gui.setSlot(10, menuBtn(Items.CHEST, "Player inventory", ChatFormatting.GREEN,
                Component.literal("Hotbar, main, armor, off-hand").withStyle(ChatFormatting.DARK_GRAY),
                () -> openPlayerInventory(admin, target)));
        gui.setSlot(12, menuBtn(Items.ENDER_CHEST, "Ender chest (27 slots)", ChatFormatting.LIGHT_PURPLE,
                Component.literal("Full width — no clipped slots").withStyle(ChatFormatting.DARK_GRAY),
                () -> openEnderChest(admin, target)));
        gui.setSlot(14, menuBtn(Items.GOLD_BLOCK, "Bank + wallet", ChatFormatting.YELLOW,
                Component.literal("Account balances").withStyle(ChatFormatting.DARK_GRAY),
                () -> BankGui.openForAdmin(admin, target)));
        gui.setSlot(16, menuBtn(Items.EMERALD, "Set coin amount", ChatFormatting.GREEN,
                Component.literal("Opens numeric prompt").withStyle(ChatFormatting.DARK_GRAY),
                () -> openCoinsEditor(admin, target)));

        gui.setSlot(20, menuBtn(Items.AMETHYST_SHARD, "Crypto wallets", ChatFormatting.LIGHT_PURPLE,
                Component.literal("Per-coin balances").withStyle(ChatFormatting.DARK_GRAY),
                () -> openCryptoEditor(admin, target)));
        gui.setSlot(22, menuBtn(Items.PAPER, "Stock portfolio", ChatFormatting.AQUA,
                Component.literal("Share holdings").withStyle(ChatFormatting.DARK_GRAY),
                () -> openStockEditor(admin, target)));
        gui.setSlot(24, menuBtn(Items.CHEST, "Viewed chests", ChatFormatting.GOLD,
                Component.literal("/view prevchests shortcut").withStyle(ChatFormatting.DARK_GRAY),
                () -> PrevChestsAdminGui.open(admin, target)));
        gui.setSlot(31, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("Close").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .setCallback((a, b, containerInput, slotGui) -> admin.closeContainer())
                .build());

        gui.open();
    }

    private static GuiElementBuilder menuBtn(net.minecraft.world.item.Item icon, String title, ChatFormatting color,
                                             Component subtitle, Runnable onClick) {
        return new GuiElementBuilder(icon)
                .setName(Component.literal(title).withStyle(color, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(subtitle)
                .setCallback((a, b, containerInput, slotGui) -> onClick.run());
    }

    public static void openPlayerInventory(ServerPlayer admin, ServerPlayer target) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, admin, false);
        gui.setTitle(Component.literal("Inventory — ").withStyle(ChatFormatting.GOLD).append(target.getDisplayName()));
        Runnable redraw = () -> drawPlayerInventory(gui, admin, target);
        gui.setSlot(53, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.GRAY))
                .setCallback((i, t, containerInput, slotGui) -> openMainMenu(admin, target))
                .build());
        redraw.run();
        gui.open();
    }

    private static void drawPlayerInventory(SimpleGui gui, ServerPlayer admin, ServerPlayer target) {
        var filler = new GuiElementBuilder(Items.STAINED_GLASS_PANE.black()).setName(Component.literal("")).build();
        for (int i = 0; i < 54; i++) {
            if (i == 53) continue;
            gui.setSlot(i, filler);
        }
        for (int inv = 0; inv < 36; inv++) {
            int g = guiSlotForMain(inv);
            if (g >= 0) bindInvSlot(gui, admin, target, inv, g, () -> drawPlayerInventory(gui, admin, target));
        }
        // Armor order in GUI: helmet (top) -> boots (bottom)
        bindInvSlot(gui, admin, target, 36, 5, () -> drawPlayerInventory(gui, admin, target));
        bindInvSlot(gui, admin, target, 37, 14, () -> drawPlayerInventory(gui, admin, target));
        bindInvSlot(gui, admin, target, 38, 23, () -> drawPlayerInventory(gui, admin, target));
        bindInvSlot(gui, admin, target, 39, 32, () -> drawPlayerInventory(gui, admin, target));
        bindInvSlot(gui, admin, target, 40, 49, () -> drawPlayerInventory(gui, admin, target));
    }

    private static int guiSlotForMain(int invIndex) {
        if (invIndex >= 0 && invIndex <= 8) return 45 + invIndex;
        if (invIndex >= 9 && invIndex <= 17) return 10 + (invIndex - 9);
        if (invIndex >= 18 && invIndex <= 26) return 19 + (invIndex - 18);
        if (invIndex >= 27 && invIndex <= 35) return 28 + (invIndex - 27);
        return -1;
    }

    private static void bindInvSlot(SimpleGui gui, ServerPlayer admin, ServerPlayer target,
                                    int invIndex, int guiIndex, Runnable redraw) {
        ItemStack st = target.getInventory().getItem(invIndex);
        GuiElementBuilder b = st.isEmpty()
                ? new GuiElementBuilder(Items.STAINED_GLASS_PANE.gray())
                .setName(Component.literal("Empty #" + invIndex).withStyle(ChatFormatting.DARK_GRAY))
                : new GuiElementBuilder(st.copy());
        b.setCallback((idx, type, containerInput, slotGui) -> {
            ItemStack cursor = admin.containerMenu.getCarried().copy();
            ItemStack slot = target.getInventory().getItem(invIndex).copy();
            target.getInventory().setItem(invIndex, ItemStack.EMPTY);
            admin.containerMenu.setCarried(ItemStack.EMPTY);
            target.getInventory().setItem(invIndex, cursor.isEmpty() ? ItemStack.EMPTY : cursor.copy());
            admin.containerMenu.setCarried(slot.isEmpty() ? ItemStack.EMPTY : slot.copy());
            target.getInventory().setChanged();
            target.inventoryMenu.broadcastChanges();
            admin.inventoryMenu.broadcastChanges();
            redraw.run();
        });
        gui.setSlot(guiIndex, b.build());
    }

    /** Ender chest: slots 0–26 map to GUI rows 0–2 across full 9 columns; row 3 is padding + back. */
    public static void openEnderChest(ServerPlayer admin, ServerPlayer target) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x4, admin, false);
        gui.setTitle(Component.literal("Ender — ").withStyle(ChatFormatting.LIGHT_PURPLE).append(target.getDisplayName()));
        Runnable redraw = () -> drawEnder(gui, admin, target);
        redraw.run();
        gui.open();
    }

    private static void drawEnder(SimpleGui gui, ServerPlayer admin, ServerPlayer target) {
        var filler = new GuiElementBuilder(Items.STAINED_GLASS_PANE.black()).setName(Component.literal("")).build();
        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, filler);
        }
        for (int row = 0; row < ENDER_ROWS; row++) {
            for (int col = 0; col < ENDER_COLS; col++) {
                int idx = row * ENDER_COLS + col;
                int gSlot = row * 9 + col;
                final int enderIndex = idx;
                ItemStack st = target.getEnderChestInventory().getItem(enderIndex);
                GuiElementBuilder b = st.isEmpty()
                        ? new GuiElementBuilder(Items.STAINED_GLASS_PANE.gray())
                        .setName(Component.literal("Ender #" + enderIndex).withStyle(ChatFormatting.DARK_GRAY))
                        : new GuiElementBuilder(st.copy());
                b.setCallback((a, b2, containerInput, slotGui) -> {
                    ItemStack cursor = admin.containerMenu.getCarried().copy();
                    ItemStack slot = target.getEnderChestInventory().getItem(enderIndex).copy();
                    target.getEnderChestInventory().setItem(enderIndex, ItemStack.EMPTY);
                    admin.containerMenu.setCarried(ItemStack.EMPTY);
                    target.getEnderChestInventory().setItem(enderIndex, cursor.isEmpty() ? ItemStack.EMPTY : cursor.copy());
                    admin.containerMenu.setCarried(slot.isEmpty() ? ItemStack.EMPTY : slot.copy());
                    target.getEnderChestInventory().setChanged();
                    target.inventoryMenu.broadcastChanges();
                    admin.inventoryMenu.broadcastChanges();
                    drawEnder(gui, admin, target);
                });
                gui.setSlot(gSlot, b.build());
            }
        }
        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.GRAY))
                .setCallback((i, t, containerInput, slotGui) -> openMainMenu(admin, target))
                .build());
    }

    public static void openCoinsEditor(ServerPlayer admin, ServerPlayer target) {
        SignGui sign = new SignGui(admin) {
            @Override
            public void onManualClose() {
                try {
                    int v = Integer.parseInt(getLine(0).getString().trim());
                    CoinManager.setCoins(target.getUUID().toString(), Math.max(0, v));
                    DataManager.save(PoliticalServer.server);
                    admin.sendSystemMessage(Component.literal("Set coins to " + v).withStyle(ChatFormatting.GREEN), false);
                } catch (NumberFormatException ignored) {
                    admin.sendSystemMessage(Component.literal("Invalid number").withStyle(ChatFormatting.RED), false);
                }
                openMainMenu(admin, target);
            }
        };
        sign.setLine(0, Component.literal(""));
        sign.setLine(1, Component.literal("^^^^^^^^^^^^^^").withStyle(ChatFormatting.YELLOW));
        sign.setLine(2, Component.literal("Set coins").withStyle(ChatFormatting.AQUA));
        sign.setLine(3, Component.literal("Current: " + CoinManager.getCoins(target.getUUID().toString())).withStyle(ChatFormatting.GRAY));
        sign.open();
    }

    public static void openCryptoEditor(ServerPlayer admin, ServerPlayer target) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, admin, false);
        gui.setTitle(Component.literal("Crypto: ").append(target.getDisplayName()));
        int slot = 10;
        for (CryptoMarket.Crypto c : CryptoMarket.Crypto.values()) {
            if (slot >= 43) break;
            double bal = CryptoMarket.getBalance(target.getUUID().toString(), c.symbol);
            String sym = c.symbol;
            gui.setSlot(slot, new GuiElementBuilder(Items.AMETHYST_SHARD)
                    .setName(Component.literal(c.icon + " " + sym).withStyle(ChatFormatting.LIGHT_PURPLE))
                    .addLoreLine(Component.literal("Balance: " + CryptoMarket.formatCrypto(bal)).withStyle(ChatFormatting.WHITE))
                    .addLoreLine(Component.literal("Click to set").withStyle(ChatFormatting.GRAY))
                    .setCallback((a, b2, containerInput, slotGui) -> promptCryptoAmount(admin, target, sym))
                    .build());
            slot++;
            if ((slot + 1) % 9 == 0) slot += 2;
        }
        gui.setSlot(49, new GuiElementBuilder(Items.ARROW).setName(Component.literal("← Back"))
                .setCallback((a, b, containerInput, slotGui) -> openMainMenu(admin, target)).build());
        gui.open();
    }

    private static void promptCryptoAmount(ServerPlayer admin, ServerPlayer target, String symbol) {
        SignGui sign = new SignGui(admin) {
            @Override
            public void onManualClose() {
                try {
                    double v = Double.parseDouble(getLine(0).getString().trim());
                    CryptoMarket.adminSetWalletBalance(target.getUUID().toString(), symbol, v);
                    CryptoMarket.persistSave(PoliticalServer.server);
                    admin.sendSystemMessage(Component.literal("Set " + symbol + " to " + v).withStyle(ChatFormatting.GREEN), false);
                } catch (NumberFormatException ignored) {
                    admin.sendSystemMessage(Component.literal("Invalid number").withStyle(ChatFormatting.RED), false);
                }
                openCryptoEditor(admin, target);
            }
        };
        sign.setLine(0, Component.literal(""));
        sign.setLine(1, Component.literal("^^^^^^^^^^^^^^").withStyle(ChatFormatting.YELLOW));
        sign.setLine(2, Component.literal("Set " + symbol).withStyle(ChatFormatting.AQUA));
        sign.setLine(3, Component.literal("Cur: " + CryptoMarket.getBalance(target.getUUID().toString(), symbol)).withStyle(ChatFormatting.GRAY));
        sign.open();
    }

    public static void openStockEditor(ServerPlayer admin, ServerPlayer target) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, admin, false);
        gui.setTitle(Component.literal("Stocks: ").append(target.getDisplayName()));
        Map<String, Integer> port = StockMarket.getPortfolio(target.getUUID().toString());
        int slot = 10;
        for (StockMarket.Stock s : StockMarket.Stock.values()) {
            if (slot >= 43) break;
            int sh = port.getOrDefault(s.symbol, 0);
            String sym = s.symbol;
            gui.setSlot(slot, new GuiElementBuilder(Items.MAP)
                    .setName(Component.literal(s.icon + " " + sym).withStyle(ChatFormatting.YELLOW))
                    .addLoreLine(Component.literal("Shares: " + sh).withStyle(ChatFormatting.WHITE))
                    .addLoreLine(Component.literal("Click to set shares").withStyle(ChatFormatting.GRAY))
                    .setCallback((a, b2, containerInput, slotGui) -> promptStockShares(admin, target, sym))
                    .build());
            slot++;
            if ((slot + 1) % 9 == 0) slot += 2;
        }
        gui.setSlot(49, new GuiElementBuilder(Items.ARROW).setName(Component.literal("← Back"))
                .setCallback((a, b, containerInput, slotGui) -> openMainMenu(admin, target)).build());
        gui.open();
    }

    private static void promptStockShares(ServerPlayer admin, ServerPlayer target, String symbol) {
        SignGui sign = new SignGui(admin) {
            @Override
            public void onManualClose() {
                try {
                    int v = Integer.parseInt(getLine(0).getString().trim());
                    StockMarket.adminSetShares(target.getUUID().toString(), symbol, v);
                    StockMarket.persistSave(PoliticalServer.server);
                    admin.sendSystemMessage(Component.literal("Set " + symbol + " shares to " + v).withStyle(ChatFormatting.GREEN), false);
                } catch (NumberFormatException ignored) {
                    admin.sendSystemMessage(Component.literal("Invalid integer").withStyle(ChatFormatting.RED), false);
                }
                openStockEditor(admin, target);
            }
        };
        int cur = StockMarket.getPortfolio(target.getUUID().toString()).getOrDefault(symbol, 0);
        sign.setLine(0, Component.literal(""));
        sign.setLine(1, Component.literal("^^^^^^^^^^^^^^").withStyle(ChatFormatting.YELLOW));
        sign.setLine(2, Component.literal("Set " + symbol + " shares").withStyle(ChatFormatting.AQUA));
        sign.setLine(3, Component.literal("Current: " + cur).withStyle(ChatFormatting.GRAY));
        sign.open();
    }
}
