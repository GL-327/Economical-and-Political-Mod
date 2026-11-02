package com.political.economy;
import com.political.AdminInvseeGui;
import com.political.BuffProgressManager;
import com.political.CryptoMarketGui;
import com.political.DataManager;
import com.political.PoliticalServer;
import com.political.RecipeGuiTheme;


import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.gui.SignGui;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.List;
import java.util.Locale;

public class BankGui {

    private static final int MAIN_MAX = 100_000;
    private static final long SAVINGS_COOLDOWN_MS = 0L;
    private static final int MAX_LOANS = 5;
    private static final int MAX_LOAN_AMOUNT = 500_000;

    private static String fmtRate(double rate) {
        double pct = rate * 100;
        if (pct == (int) pct) return (int) pct + "%";
        return String.format("%.1f%%", pct);
    }

    private static String fmt(int amount) {
        return String.format(Locale.US, "%,d", amount);
    }

    private static String fmt(long amount) {
        return String.format(Locale.US, "%,d", amount);
    }

    // ════════════════════════════════════════════════════════════
    //  MAIN BANK HUB  (9x4)
    // ════════════════════════════════════════════════════════════

    public static void open(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x4, player, false);
        gui.setTitle(Component.literal("🏦 National Bank"));
        RecipeGuiTheme.fillChest4Hub(gui);

        String uuid = player.getUUID().toString();
        long purse = CoinManager.getCoinsLong(player);
        long main = DataManager.getBankMain(uuid);
        long savings = DataManager.getBankSavings(uuid);
        long totalLoan = DataManager.getBankLoan(uuid);

        gui.setSlot(4, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Component.literal("💰 National Bank").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Wallet: " + fmt(purse) + " coins").withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal("Main: " + fmt(main) + " coins").withStyle(ChatFormatting.WHITE))
                .addLoreLine(Component.literal("Savings: " + fmt(savings) + " coins").withStyle(ChatFormatting.AQUA))
                .addLoreLine(Component.literal(totalLoan > 0 ? "Loans: " + fmt(totalLoan) + " owed" : "Loans: None").withStyle(
                        totalLoan > 0 ? ChatFormatting.RED : ChatFormatting.GREEN))
                .glow().build());

        gui.setSlot(10, new GuiElementBuilder(Items.ENDER_CHEST)
                .setName(Component.literal("🏛 Main Account").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Balance: " + fmt(main) + " coins").withStyle(ChatFormatting.WHITE))
                .addLoreLine(Component.literal("Deposit & withdraw coins").withStyle(ChatFormatting.GRAY))
                .setCallback((i, t, c, s) -> openMainAccount(player))
                .build());

        gui.setSlot(12, new GuiElementBuilder(Items.BARREL)
                .setName(Component.literal("🏦 Savings").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Balance: " + fmt(savings) + " coins").withStyle(ChatFormatting.WHITE))
                .addLoreLine(Component.literal("Earns compound interest").withStyle(ChatFormatting.GRAY))
                .setCallback((i, t, c, s) -> openSavingsAccount(player))
                .build());

        gui.setSlot(14, new GuiElementBuilder(Items.WRITABLE_BOOK)
                .setName(Component.literal("📜 Loans").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal(totalLoan > 0 ? "Debt: " + fmt(totalLoan) + " coins" : "No active loans").withStyle(
                        totalLoan > 0 ? ChatFormatting.RED : ChatFormatting.GREEN))
                .addLoreLine(Component.literal("Take & repay loans").withStyle(ChatFormatting.GRAY))
                .setCallback((i, t, c, s) -> openLoans(player))
                .build());

        gui.setSlot(16, new GuiElementBuilder(Items.PAPER)
                .setName(Component.literal("💸 Transfer").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Send coins to another player").withStyle(ChatFormatting.GRAY))
                .setCallback((i, t, c, s) -> openTransfer(player))
                .build());

        gui.setSlot(19, new GuiElementBuilder(Items.CLOCK)
                .setName(Component.literal("🕐 History").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("View recent transactions").withStyle(ChatFormatting.GRAY))
                .setCallback((i, t, c, s) -> openTransactionHistory(player))
                .build());

        double cryptoValue = CryptoMarket.getWalletValue(uuid);
        gui.setSlot(21, new GuiElementBuilder(Items.END_CRYSTAL)
                .setName(Component.literal("🔮 Crypto Wallet").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Value: $" + CryptoMarket.formatCrypto(cryptoValue)).withStyle(ChatFormatting.GOLD))
                .setCallback((i, t, c, s) -> CryptoMarketGui.openMainMenu(player))
                .glow().build());

        int upgradeLevel = DataManager.getBankUpgradeLevel(uuid);
        long upgradeCost = DataManager.getBankUpgradeCost(uuid);
        GuiElementBuilder upgradeBtn = new GuiElementBuilder(Items.GOLDEN_APPLE)
                .setName(Component.literal("⬆ Upgrades (Lv " + upgradeLevel + "/10)").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""));
        if (upgradeLevel >= 10) {
            upgradeBtn.addLoreLine(Component.literal("✓ Maximum level!").withStyle(ChatFormatting.GREEN));
        } else {
            upgradeBtn.addLoreLine(Component.literal("Cost: " + fmt(upgradeCost) + " coins").withStyle(ChatFormatting.GOLD));
            upgradeBtn.addLoreLine(Component.literal("Click to upgrade").withStyle(ChatFormatting.YELLOW));
            upgradeBtn.setCallback((i, t, c, s) -> {
                DataManager.purchaseBankUpgrade(player);
                DataManager.save(PoliticalServer.server);
                open(player);
            });
        }
        gui.setSlot(23, upgradeBtn.build());

        gui.setSlot(25, new GuiElementBuilder(Items.BOOK)
                .setName(Component.literal("📖 Help").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Main Account: earns interest, capped").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Savings: tiered rates, compound interest").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Loans: pick a term, 2-day grace period").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Rates are set by the Chair").withStyle(ChatFormatting.GRAY))
                .build());

        gui.setSlot(31, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("❌ Close").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD))
                .setCallback((i, t, c, s) -> player.closeContainer())
                .build());

        gui.open();
    }

    // ════════════════════════════════════════════════════════════
    //  MAIN ACCOUNT SUB-GUI
    // ════════════════════════════════════════════════════════════

    private static void openMainAccount(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x4, player, false);
        gui.setTitle(Component.literal("🏛 Main Account"));
        RecipeGuiTheme.fillChest4Hub(gui);

        String uuid = player.getUUID().toString();
        long main = DataManager.getBankMain(uuid);
        long purse = CoinManager.getCoinsLong(player);
        long mainMax = DataManager.getMainAccountMax(uuid);

        gui.setSlot(4, new GuiElementBuilder(Items.ENDER_CHEST)
                .setName(Component.literal("🏛 Main Account").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Balance: " + fmt(main) + " / " + fmt(mainMax)).withStyle(ChatFormatting.WHITE))
                .addLoreLine(Component.literal("Wallet: " + fmt(purse) + " coins").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Interest: " + fmtRate(DataManager.getMainAccountInterestRate()) + "/hr").withStyle(ChatFormatting.GREEN))
                .glow().build());

        int[] depAmounts = {1000, 5000, 10000};
        int[] depSlots = {10, 11, 12};
        for (int di = 0; di < depAmounts.length; di++) {
            final int amount = depAmounts[di];
            gui.setSlot(depSlots[di], new GuiElementBuilder(Items.EMERALD)
                    .setName(Component.literal("Deposit " + fmt(amount)).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("To main account").withStyle(ChatFormatting.GRAY))
                    .setCallback((idx, t, c, s) -> doMainDeposit(player, amount))
                    .build());
        }

        gui.setSlot(13, new GuiElementBuilder(Items.WRITABLE_BOOK)
                .setName(Component.literal("Custom Deposit").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Enter a custom amount").withStyle(ChatFormatting.GRAY))
                .setCallback((i, t, c, s) -> openMainCustomDeposit(player))
                .build());

        int[] wdAmounts = {1000, 5000};
        int[] wdSlots = {15, 16};
        for (int wi = 0; wi < wdAmounts.length; wi++) {
            final int amount = wdAmounts[wi];
            gui.setSlot(wdSlots[wi], new GuiElementBuilder(Items.GOLD_INGOT)
                    .setName(Component.literal("Withdraw " + fmt(amount)).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("From main account").withStyle(ChatFormatting.GRAY))
                    .setCallback((idx, t, c, s) -> {
                        String uid = player.getUUID().toString();
                        long bal = DataManager.getBankMain(uid);
                        if (bal < amount) {
                            player.sendSystemMessage(Component.literal("✗ Not enough in Main Account!").withStyle(ChatFormatting.RED), false);
                        } else {
                            DataManager.setBankMain(uid, bal - amount);
                            CoinManager.giveCoins(player, amount);
                            player.sendSystemMessage(Component.literal("✓ Withdrew " + fmt(amount) + " coins from Main Account.").withStyle(ChatFormatting.GREEN), false);
                        }
                        openMainAccount(player);
                    })
                    .build());
        }

        gui.setSlot(19, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Component.literal("Withdraw 10,000").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("From main account").withStyle(ChatFormatting.GRAY))
                .setCallback((i, t, c, s) -> {
                    String uid = player.getUUID().toString();
                    long bal = DataManager.getBankMain(uid);
                    if (bal < 10000) {
                        player.sendSystemMessage(Component.literal("✗ Not enough in Main Account!").withStyle(ChatFormatting.RED), false);
                    } else {
                        long actual = Math.min(10000L, bal);
                        DataManager.setBankMain(uid, bal - actual);
                        CoinManager.giveCoins(player, actual);
                        player.sendSystemMessage(Component.literal("✓ Withdrew " + fmt(actual) + " coins from Main Account.").withStyle(ChatFormatting.GREEN), false);
                    }
                    openMainAccount(player);
                })
                .build());

        gui.setSlot(20, new GuiElementBuilder(Items.GOLD_BLOCK)
                .setName(Component.literal("Withdraw All").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(fmt(main) + " coins").withStyle(ChatFormatting.GRAY))
                .setCallback((i, t, c, s) -> {
                    String uid = player.getUUID().toString();
                    long bal = DataManager.getBankMain(uid);
                    if (bal <= 0) {
                        player.sendSystemMessage(Component.literal("✗ Nothing to withdraw!").withStyle(ChatFormatting.RED), false);
                    } else {
                        DataManager.setBankMain(uid, 0);
                        CoinManager.giveCoins(player, bal);
                        player.sendSystemMessage(Component.literal("✓ Withdrew " + fmt(bal) + " coins from Main Account.").withStyle(ChatFormatting.GREEN), false);
                    }
                    openMainAccount(player);
                })
                .build());

        gui.setSlot(21, new GuiElementBuilder(Items.BOOK)
                .setName(Component.literal("Custom Withdraw").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Enter a custom amount").withStyle(ChatFormatting.GRAY))
                .setCallback((i, t, c, s) -> openMainCustomWithdraw(player))
                .build());

        gui.setSlot(27, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.GRAY))
                .setCallback((i, t, c, s) -> open(player))
                .build());

        gui.open();
    }

    // ════════════════════════════════════════════════════════════
    //  MAIN ACCOUNT HELPERS
    // ════════════════════════════════════════════════════════════

    private static void doMainDeposit(ServerPlayer player, int amount) {
        String uuid = player.getUUID().toString();
        long cur = DataManager.getBankMain(uuid);
        long purse = CoinManager.getCoinsLong(player);
        long cap = DataManager.getMainAccountMax(uuid);
        if (cur >= cap) {
            player.sendSystemMessage(Component.literal("✗ Main account is at cap!").withStyle(ChatFormatting.RED), false);
            openMainAccount(player);
            return;
        }
        if (purse < amount) {
            player.sendSystemMessage(Component.literal("✗ Not enough coins!").withStyle(ChatFormatting.RED), false);
            openMainAccount(player);
            return;
        }
        long space = cap - cur;
        int actual = (int) Math.min(amount, space);
        CoinManager.removeCoins(player, actual);
        DataManager.setBankMain(uuid, cur + actual);
        BuffProgressManager.onBankDeposit(player, DataManager.getBankMain(uuid) + DataManager.getBankSavings(uuid));
        player.sendSystemMessage(Component.literal("✓ Deposited " + fmt(actual) + " coins into Main Account.").withStyle(ChatFormatting.GREEN), false);
        openMainAccount(player);
    }

    private static void openMainCustomDeposit(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        SignGui signGui = new SignGui(player) {
            @Override
            public void onManualClose() {
                String input = this.getLine(0).getString().trim().replaceAll("[^0-9]", "");
                if (input.isEmpty()) { openMainAccount(player); return; }
                try {
                    int amount = Integer.parseInt(input);
                    if (amount <= 0) { player.sendSystemMessage(Component.literal("✗ Amount must be positive!").withStyle(ChatFormatting.RED), false); openMainAccount(player); return; }
                    doMainDeposit(player, amount);
                } catch (NumberFormatException e) {
                    player.sendSystemMessage(Component.literal("✗ Invalid number!").withStyle(ChatFormatting.RED), false);
                    openMainAccount(player);
                }
            }
        };
        signGui.setLine(0, Component.literal(""));
        signGui.setLine(1, Component.literal("Enter amount:"));
        signGui.setLine(2, Component.literal("to deposit"));
        signGui.open();
    }

    private static void openMainCustomWithdraw(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        SignGui signGui = new SignGui(player) {
            @Override
            public void onManualClose() {
                String input = this.getLine(0).getString().trim().replaceAll("[^0-9]", "");
                if (input.isEmpty()) { openMainAccount(player); return; }
                try {
                    int amount = Integer.parseInt(input);
                    if (amount <= 0) { player.sendSystemMessage(Component.literal("✗ Amount must be positive!").withStyle(ChatFormatting.RED), false); openMainAccount(player); return; }
                    long bal = DataManager.getBankMain(uuid);
                    if (bal < amount) { player.sendSystemMessage(Component.literal("✗ Not enough in Main Account! Balance: " + fmt(bal)).withStyle(ChatFormatting.RED), false); openMainAccount(player); return; }
                    DataManager.setBankMain(uuid, bal - amount);
                    CoinManager.giveCoins(player, amount);
                    player.sendSystemMessage(Component.literal("✓ Withdrew " + fmt(amount) + " coins from Main Account.").withStyle(ChatFormatting.GREEN), false);
                } catch (NumberFormatException e) {
                    player.sendSystemMessage(Component.literal("✗ Invalid number!").withStyle(ChatFormatting.RED), false);
                }
                openMainAccount(player);
            }
        };
        signGui.setLine(0, Component.literal(""));
        signGui.setLine(1, Component.literal("Enter amount:"));
        signGui.setLine(2, Component.literal("to withdraw"));
        signGui.open();
    }

    // ════════════════════════════════════════════════════════════
    //  SAVINGS ACCOUNT SUB-GUI
    // ════════════════════════════════════════════════════════════

    private static void doSavingsDeposit(ServerPlayer player, int amount) {
        String uuid = player.getUUID().toString();
        long purse = CoinManager.getCoinsLong(player);
        long cur = DataManager.getBankSavings(uuid);
        long cap = DataManager.getSavingsAccountMax(uuid);
        if (cur >= cap) {
            player.sendSystemMessage(Component.literal("✗ Savings account is at cap!").withStyle(ChatFormatting.RED), false);
            openSavingsAccount(player);
            return;
        }
        if (purse < amount) amount = (int) Math.min(Integer.MAX_VALUE, purse);
        if (amount <= 0) {
            player.sendSystemMessage(Component.literal("✗ Not enough coins!").withStyle(ChatFormatting.RED), false);
            openSavingsAccount(player);
            return;
        }
        long space = cap - cur;
        int actual = (int) Math.min(amount, space);
        CoinManager.removeCoins(player, actual);
        DataManager.setBankSavings(uuid, cur + actual);
        BuffProgressManager.onBankDeposit(player, DataManager.getBankMain(uuid) + DataManager.getBankSavings(uuid));
        player.sendSystemMessage(Component.literal("✓ Deposited " + fmt(actual) + " coins into Savings.").withStyle(ChatFormatting.GREEN), false);
        openSavingsAccount(player);
    }

    private static void openSavingsAccount(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x4, player, false);
        gui.setTitle(Component.literal("🏦 Savings Account"));
        RecipeGuiTheme.fillChest4Hub(gui);

        String uuid = player.getUUID().toString();
        long savings = DataManager.getBankSavings(uuid);
        long purse = CoinManager.getCoinsLong(player);
        long savingsMax = DataManager.getSavingsAccountMax(uuid);
        double savingsRate = DataManager.getEffectiveSavingsRate(savings);
        long lastWithdraw = DataManager.getBankSavingsWithdrawTime(uuid);
        long now = System.currentTimeMillis();
        long elapsed = now - lastWithdraw;
        boolean onCooldown = lastWithdraw > 0 && elapsed < SAVINGS_COOLDOWN_MS;

        gui.setSlot(4, new GuiElementBuilder(Items.BARREL)
                .setName(Component.literal("🏦 Savings Account").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Balance: " + fmt(savings) + " / " + fmt(savingsMax)).withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Rate: " + fmtRate(savingsRate) + "/hr (" + getSavingsTierLabel(savings) + ")").withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal("Daily: ~" + fmt(getProjectedDailyEarnings(savings, savingsRate)) + " coins").withStyle(ChatFormatting.GREEN))
                .glow().build());

        gui.setSlot(10, new GuiElementBuilder(Items.EMERALD)
                .setName(Component.literal("Deposit 10,000").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .setCallback((i, t, c, s) -> doSavingsDeposit(player, 10000))
                .build());
        gui.setSlot(11, new GuiElementBuilder(Items.EMERALD)
                .setName(Component.literal("Deposit 50,000").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .setCallback((i, t, c, s) -> doSavingsDeposit(player, 50000))
                .build());

        long savingsSpace = savingsMax - savings;
        long maxDep = Math.min(purse, savingsSpace);
        gui.setSlot(12, new GuiElementBuilder(Items.DIAMOND)
                .setName(Component.literal("Max Deposit (" + fmt(maxDep) + ")").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .setCallback((i, t, c, s) -> {
                    long p = CoinManager.getCoinsLong(player);
                    long cur = DataManager.getBankSavings(uuid);
                    long cap = DataManager.getSavingsAccountMax(uuid);
                    long sp = cap - cur;
                    long actual = Math.min(p, sp);
                    if (actual <= 0) {
                        player.sendSystemMessage(Component.literal("✗ Nothing to deposit!").withStyle(ChatFormatting.RED), false);
                    } else {
                        CoinManager.removeCoins(player, actual);
                        DataManager.setBankSavings(uuid, cur + actual);
                        BuffProgressManager.onBankDeposit(player, DataManager.getBankMain(uuid) + DataManager.getBankSavings(uuid));
                        player.sendSystemMessage(Component.literal("✓ Deposited " + fmt(actual) + " coins into Savings.").withStyle(ChatFormatting.GREEN), false);
                    }
                    openSavingsAccount(player);
                })
                .build());

        gui.setSlot(13, new GuiElementBuilder(Items.WRITABLE_BOOK)
                .setName(Component.literal("Custom Deposit").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .setCallback((i, t, c, s) -> openSavingsCustomDeposit(player))
                .build());

        boolean canWithdraw = !onCooldown && savings > 0;

        gui.setSlot(15, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Component.literal("Withdraw 10,000").withStyle(canWithdraw ? ChatFormatting.YELLOW : ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                .setCallback((i, t, c, s) -> {
                    if (onCooldown) { player.sendSystemMessage(Component.literal("✗ Cooldown active!").withStyle(ChatFormatting.RED), false); openSavingsAccount(player); return; }
                    long bal = DataManager.getBankSavings(uuid);
                    long actual = Math.min(10000L, bal);
                    if (actual <= 0) { player.sendSystemMessage(Component.literal("✗ Nothing to withdraw!").withStyle(ChatFormatting.RED), false); openSavingsAccount(player); return; }
                    DataManager.setBankSavings(uuid, bal - actual);
                    DataManager.setBankSavingsWithdrawTime(uuid, System.currentTimeMillis());
                    CoinManager.giveCoins(player, actual);
                    player.sendSystemMessage(Component.literal("✓ Withdrew " + fmt(actual) + " coins from Savings.").withStyle(ChatFormatting.GREEN), false);
                    openSavingsAccount(player);
                })
                .build());

        gui.setSlot(16, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Component.literal("Withdraw 50,000").withStyle(canWithdraw ? ChatFormatting.YELLOW : ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                .setCallback((i, t, c, s) -> {
                    if (onCooldown) { player.sendSystemMessage(Component.literal("✗ Cooldown active!").withStyle(ChatFormatting.RED), false); openSavingsAccount(player); return; }
                    long bal = DataManager.getBankSavings(uuid);
                    long actual = Math.min(50000L, bal);
                    if (actual <= 0) { player.sendSystemMessage(Component.literal("✗ Nothing to withdraw!").withStyle(ChatFormatting.RED), false); openSavingsAccount(player); return; }
                    DataManager.setBankSavings(uuid, bal - actual);
                    DataManager.setBankSavingsWithdrawTime(uuid, System.currentTimeMillis());
                    CoinManager.giveCoins(player, actual);
                    player.sendSystemMessage(Component.literal("✓ Withdrew " + fmt(actual) + " coins from Savings.").withStyle(ChatFormatting.GREEN), false);
                    openSavingsAccount(player);
                })
                .build());

        gui.setSlot(19, new GuiElementBuilder(Items.GOLD_BLOCK)
                .setName(Component.literal("Withdraw All").withStyle(canWithdraw ? ChatFormatting.YELLOW : ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(fmt(savings) + " coins").withStyle(ChatFormatting.GRAY))
                .setCallback((i, t, c, s) -> {
                    if (onCooldown) { player.sendSystemMessage(Component.literal("✗ Cooldown active!").withStyle(ChatFormatting.RED), false); openSavingsAccount(player); return; }
                    long bal = DataManager.getBankSavings(uuid);
                    if (bal <= 0) { player.sendSystemMessage(Component.literal("✗ Nothing to withdraw!").withStyle(ChatFormatting.RED), false); openSavingsAccount(player); return; }
                    DataManager.setBankSavings(uuid, 0);
                    DataManager.setBankSavingsWithdrawTime(uuid, System.currentTimeMillis());
                    CoinManager.giveCoins(player, bal);
                    player.sendSystemMessage(Component.literal("✓ Withdrew " + fmt(bal) + " coins from Savings.").withStyle(ChatFormatting.GREEN), false);
                    openSavingsAccount(player);
                })
                .build());

        if (!onCooldown) {
            gui.setSlot(20, new GuiElementBuilder(Items.BOOK)
                    .setName(Component.literal("Custom Withdraw").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                    .setCallback((i, t, c, s) -> openSavingsCustomWithdraw(player))
                    .build());
        }

        gui.setSlot(24, new GuiElementBuilder(Items.BOOK)
                .setName(Component.literal("📖 Savings Tiers").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Under 10K: Base rate (x1.0)").withStyle(savings < 10_000 ? ChatFormatting.WHITE : ChatFormatting.DARK_GRAY))
                .addLoreLine(Component.literal("10K – 100K: Enhanced (x1.2)").withStyle(savings >= 10_000 && savings < 100_000 ? ChatFormatting.WHITE : ChatFormatting.DARK_GRAY))
                .addLoreLine(Component.literal("100K+: Premium (x1.5)").withStyle(savings >= 100_000 ? ChatFormatting.WHITE : ChatFormatting.DARK_GRAY))
                .build());

        gui.setSlot(27, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.GRAY))
                .setCallback((i, t, c, s) -> open(player))
                .build());

        gui.open();
    }

    private static void openSavingsCustomDeposit(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        SignGui signGui = new SignGui(player) {
            @Override
            public void onManualClose() {
                String input = this.getLine(0).getString().trim().replaceAll("[^0-9]", "");
                if (input.isEmpty()) { openSavingsAccount(player); return; }
                try {
                    int amount = Integer.parseInt(input);
                    if (amount <= 0) { player.sendSystemMessage(Component.literal("✗ Amount must be positive!").withStyle(ChatFormatting.RED), false); openSavingsAccount(player); return; }
                    long purse = CoinManager.getCoinsLong(player);
                    if (purse < amount) { player.sendSystemMessage(Component.literal("✗ Not enough coins!").withStyle(ChatFormatting.RED), false); openSavingsAccount(player); return; }
                    long cur = DataManager.getBankSavings(uuid);
                    long cap = DataManager.getSavingsAccountMax(uuid);
                    long space = cap - cur;
                    int actual = (int) Math.min(amount, space);
                    if (actual <= 0) { player.sendSystemMessage(Component.literal("✗ Savings account at cap!").withStyle(ChatFormatting.RED), false); openSavingsAccount(player); return; }
                    CoinManager.removeCoins(player, actual);
                    DataManager.setBankSavings(uuid, cur + actual);
                    BuffProgressManager.onBankDeposit(player, DataManager.getBankMain(uuid) + DataManager.getBankSavings(uuid));
                    player.sendSystemMessage(Component.literal("✓ Deposited " + fmt(actual) + " coins into Savings.").withStyle(ChatFormatting.GREEN), false);
                } catch (NumberFormatException e) {
                    player.sendSystemMessage(Component.literal("✗ Invalid number!").withStyle(ChatFormatting.RED), false);
                }
                openSavingsAccount(player);
            }
        };
        signGui.setLine(0, Component.literal(""));
        signGui.setLine(1, Component.literal("Enter amount:"));
        signGui.setLine(2, Component.literal("to deposit"));
        signGui.open();
    }

    private static void openSavingsCustomWithdraw(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        SignGui signGui = new SignGui(player) {
            @Override
            public void onManualClose() {
                String input = this.getLine(0).getString().trim().replaceAll("[^0-9]", "");
                if (input.isEmpty()) { openSavingsAccount(player); return; }
                try {
                    int amount = Integer.parseInt(input);
                    if (amount <= 0) { player.sendSystemMessage(Component.literal("✗ Amount must be positive!").withStyle(ChatFormatting.RED), false); openSavingsAccount(player); return; }
                    long bal = DataManager.getBankSavings(uuid);
                    if (bal < amount) { player.sendSystemMessage(Component.literal("✗ Not enough in Savings! Balance: " + fmt(bal)).withStyle(ChatFormatting.RED), false); openSavingsAccount(player); return; }
                    DataManager.setBankSavings(uuid, bal - amount);
                    DataManager.setBankSavingsWithdrawTime(uuid, System.currentTimeMillis());
                    CoinManager.giveCoins(player, amount);
                    player.sendSystemMessage(Component.literal("✓ Withdrew " + fmt(amount) + " coins from Savings.").withStyle(ChatFormatting.GREEN), false);
                } catch (NumberFormatException e) {
                    player.sendSystemMessage(Component.literal("✗ Invalid number!").withStyle(ChatFormatting.RED), false);
                }
                openSavingsAccount(player);
            }
        };
        signGui.setLine(0, Component.literal(""));
        signGui.setLine(1, Component.literal("Enter amount:"));
        signGui.setLine(2, Component.literal("to withdraw"));
        signGui.open();
    }

    // ════════════════════════════════════════════════════════════
    //  LOANS SUB-GUI
    // ════════════════════════════════════════════════════════════

    private static void openLoans(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x4, player, false);
        gui.setTitle(Component.literal("📜 Loans"));
        RecipeGuiTheme.fillChest4Hub(gui);

        String uuid = player.getUUID().toString();
        List<DataManager.LoanEntry> loans = DataManager.getLoans(uuid);
        boolean frozen = DataManager.areLoansAreFrozen();
        long totalLoan = DataManager.getBankLoan(uuid);

        GuiElementBuilder overview = new GuiElementBuilder(Items.WRITABLE_BOOK)
                .setName(Component.literal("📜 Loan Overview").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""));
        if (totalLoan > 0) {
            overview.addLoreLine(Component.literal("Total Debt: " + fmt(totalLoan) + " coins").withStyle(ChatFormatting.RED));
        } else {
            overview.addLoreLine(Component.literal("No active loans").withStyle(ChatFormatting.GREEN));
        }
        overview.addLoreLine(Component.literal("Active: " + loans.size() + "/" + MAX_LOANS).withStyle(ChatFormatting.GRAY));
        if (frozen) {
            overview.addLoreLine(Component.literal("⚠ LOANS FROZEN by Chair").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        }
        gui.setSlot(4, overview.build());

        if (frozen) {
            gui.setSlot(12, new GuiElementBuilder(Items.ICE)
                    .setName(Component.literal("⚠ Loans Frozen").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("The Chair has frozen all new loans.").withStyle(ChatFormatting.RED))
                    .build());
        } else if (loans.size() >= MAX_LOANS) {
            gui.setSlot(12, new GuiElementBuilder(Items.BARRIER)
                    .setName(Component.literal("Max Loans Reached").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("Repay a loan to take a new one.").withStyle(ChatFormatting.GRAY))
                    .build());
        } else {
            gui.setSlot(10, makeLoanTermButton(player, "short", Items.LIME_DYE, ChatFormatting.GREEN));
            gui.setSlot(12, makeLoanTermButton(player, "standard", Items.YELLOW_DYE, ChatFormatting.YELLOW));
            gui.setSlot(14, makeLoanTermButton(player, "long", Items.RED_DYE, ChatFormatting.RED));
        }

        gui.setSlot(16, new GuiElementBuilder(Items.BOOK)
                .setName(Component.literal("📖 Loan Terms").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Short: " + DataManager.getLoanTermDays("short") + "d, " + fmtRate(DataManager.getLoanRateForTerm("short")) + "/day").withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal("Standard: " + DataManager.getLoanTermDays("standard") + "d, " + fmtRate(DataManager.getLoanRateForTerm("standard")) + "/day").withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal("Long: " + DataManager.getLoanTermDays("long") + "d, " + fmtRate(DataManager.getLoanRateForTerm("long")) + "/day").withStyle(ChatFormatting.RED))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("All loans: 2-day grace period").withStyle(ChatFormatting.AQUA))
                .build());

        int[] loanSlots = {19, 20, 21, 22, 23};
        for (int li = 0; li < loans.size() && li < loanSlots.length; li++) {
            DataManager.LoanEntry loan = loans.get(li);
            final int loanIndex = li;
            boolean inGrace = loan.isInGracePeriod();

            GuiElementBuilder loanItem = new GuiElementBuilder(Items.PAPER)
                    .setName(Component.literal("Loan #" + (li + 1) + " — " + fmt(loan.amount) + " owed").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Owed: " + fmt(loan.amount) + " / " + fmt(loan.originalAmount)).withStyle(ChatFormatting.RED))
                    .addLoreLine(Component.literal("Term: " + capitalize(loan.term) + " (" + loan.getDaysRemaining() + "d left)").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("Paid: " + fmt((int)(loan.getPercentPaid() * 100)) + "%").withStyle(ChatFormatting.AQUA));
            if (inGrace) {
                loanItem.addLoreLine(Component.literal("⏰ Grace period active").withStyle(ChatFormatting.GREEN));
            }
            loanItem.addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to repay").withStyle(ChatFormatting.YELLOW))
                    .setCallback((idx, t, c, s) -> openLoanCustomRepay(player, loanIndex));
            gui.setSlot(loanSlots[li], loanItem.build());
        }

        gui.setSlot(27, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.GRAY))
                .setCallback((i, t, c, s) -> open(player))
                .build());

        gui.open();
    }

    private static GuiElementBuilder makeLoanTermButton(ServerPlayer player, String term, Item dye, ChatFormatting color) {
        int days = DataManager.getLoanTermDays(term);
        double rate = DataManager.getLoanRateForTerm(term);
        int previewPayback = (int) Math.ceil(MAX_LOAN_AMOUNT + MAX_LOAN_AMOUNT * rate * Math.max(0, days - 2));

        return new GuiElementBuilder(dye)
                .setName(Component.literal(capitalize(term) + "-term Loan").withStyle(color, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Duration: " + days + " days").withStyle(ChatFormatting.WHITE))
                .addLoreLine(Component.literal("Rate: " + fmtRate(rate) + "/day").withStyle(ChatFormatting.RED))
                .addLoreLine(Component.literal("Grace period: 2 days (no interest)").withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal("Max loan: " + fmt(MAX_LOAN_AMOUNT) + " coins").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Max payback: ~" + fmt(previewPayback) + " coins").withStyle(ChatFormatting.GOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to enter loan amount").withStyle(ChatFormatting.YELLOW))
                .setCallback((i, t, c, s) -> openLoanAmountSign(player, term));
    }

    private static void openLoanAmountSign(ServerPlayer player, String term) {
        String uuid = player.getUUID().toString();
        SignGui signGui = new SignGui(player) {
            @Override
            public void onManualClose() {
                String input = this.getLine(0).getString().trim().replaceAll("[^0-9]", "");
                if (input.isEmpty()) { openLoans(player); return; }
                try {
                    int amount = Integer.parseInt(input);
                    if (amount <= 0) {
                        player.sendSystemMessage(Component.literal("✗ Amount must be positive!").withStyle(ChatFormatting.RED), false);
                        openLoans(player); return;
                    }
                    if (amount > MAX_LOAN_AMOUNT) {
                        player.sendSystemMessage(Component.literal("✗ Maximum loan: " + fmt(MAX_LOAN_AMOUNT) + " coins!").withStyle(ChatFormatting.RED), false);
                        openLoans(player); return;
                    }
                    if (DataManager.areLoansAreFrozen()) {
                        player.sendSystemMessage(Component.literal("✗ Loans are currently frozen by the Chair!").withStyle(ChatFormatting.RED), false);
                        openLoans(player); return;
                    }
                    List<DataManager.LoanEntry> existing = DataManager.getLoans(uuid);
                    if (existing.size() >= MAX_LOANS) {
                        player.sendSystemMessage(Component.literal("✗ Maximum " + MAX_LOANS + " loans!").withStyle(ChatFormatting.RED), false);
                        openLoans(player); return;
                    }
                    double rate = DataManager.getLoanRateForTerm(term);
                    int days = DataManager.getLoanTermDays(term);
                    int projectedPayback = (int) Math.ceil(amount + amount * rate * Math.max(0, days - 2));

                    DataManager.addLoan(uuid, amount, rate, term, days);
                    CoinManager.giveCoins(player, amount);
                    DataManager.save(PoliticalServer.server);
                    player.sendSystemMessage(Component.literal("✓ " + capitalize(term) + "-term loan of " + fmt(amount) + " coins issued!").withStyle(ChatFormatting.GREEN), false);
                    player.sendSystemMessage(Component.literal("  Rate: " + fmtRate(rate) + "/day | Duration: " + days + " days | Est. payback: " + fmt(projectedPayback)).withStyle(ChatFormatting.GRAY), false);
                    player.sendSystemMessage(Component.literal("  2-day grace period: no interest!").withStyle(ChatFormatting.AQUA), false);
                } catch (NumberFormatException e) {
                    player.sendSystemMessage(Component.literal("✗ Invalid number!").withStyle(ChatFormatting.RED), false);
                }
                openLoans(player);
            }
        };
        signGui.setLine(0, Component.literal(""));
        signGui.setLine(1, Component.literal("Loan amount:"));
        signGui.setLine(2, Component.literal("(" + capitalize(term) + " term)"));
        signGui.open();
    }

    private static void openLoanCustomRepay(ServerPlayer player, int loanIndex) {
        String uuid = player.getUUID().toString();
        SignGui signGui = new SignGui(player) {
            @Override
            public void onManualClose() {
                String input = this.getLine(0).getString().trim().replaceAll("[^0-9]", "");
                if (input.isEmpty()) { openLoans(player); return; }
                try {
                    int amount = Integer.parseInt(input);
                    if (amount <= 0) { player.sendSystemMessage(Component.literal("✗ Amount must be positive!").withStyle(ChatFormatting.RED), false); openLoans(player); return; }
                    List<DataManager.LoanEntry> loans = DataManager.getLoans(uuid);
                    if (loanIndex >= loans.size()) { player.sendSystemMessage(Component.literal("✗ Loan no longer exists!").withStyle(ChatFormatting.RED), false); openLoans(player); return; }
                    int owed = loans.get(loanIndex).amount;
                    long purse = CoinManager.getCoinsLong(player);
                    int toPay = (int) Math.min((long) amount, Math.min((long) owed, purse));
                    if (toPay <= 0) { player.sendSystemMessage(Component.literal("✗ Not enough coins!").withStyle(ChatFormatting.RED), false); openLoans(player); return; }
                    CoinManager.removeCoins(player, toPay);
                    int remaining = owed - toPay;
                    if (remaining <= 0) {
                        DataManager.removeLoan(uuid, loanIndex);
                        player.sendSystemMessage(Component.literal("✓ Loan #" + (loanIndex + 1) + " fully repaid!").withStyle(ChatFormatting.GREEN), false);
                    } else {
                        DataManager.setLoanAmount(uuid, loanIndex, remaining);
                        player.sendSystemMessage(Component.literal("✓ Repaid " + fmt(toPay) + " toward loan #" + (loanIndex + 1) + ". Remaining: " + fmt(remaining)).withStyle(ChatFormatting.GREEN), false);

                        if (DataManager.tryLoanForgiveness(uuid, loanIndex)) {
                            player.sendSystemMessage(Component.literal("✨ LOAN FORGIVEN! The bank has forgiven your remaining debt on loan #" + (loanIndex + 1) + "!").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD), false);
                        }
                    }
                    DataManager.save(PoliticalServer.server);
                } catch (NumberFormatException e) {
                    player.sendSystemMessage(Component.literal("✗ Invalid number!").withStyle(ChatFormatting.RED), false);
                }
                openLoans(player);
            }
        };
        signGui.setLine(0, Component.literal(""));
        signGui.setLine(1, Component.literal("Repay amount:"));
        signGui.setLine(2, Component.literal("for loan #" + (loanIndex + 1)));
        signGui.open();
    }

    // ════════════════════════════════════════════════════════════
    //  SAVINGS / INTEREST HELPERS
    // ════════════════════════════════════════════════════════════

    private static String getSavingsTierLabel(long savings) {
        if (savings >= 100_000) return "Premium (x1.5)";
        if (savings >= 10_000) return "Enhanced (x1.2)";
        return "Base (x1.0)";
    }

    private static long getProjectedDailyEarnings(long balance, double hourlyRate) {
        return (long) (balance * hourlyRate * 24);
    }

    private static long getProjectedWeeklyEarnings(long balance, double hourlyRate) {
        return (long) (balance * hourlyRate * 24 * 7);
    }

    private static long getCompoundProjection(long balance, double hourlyRate, int days) {
        double b = balance;
        for (int d = 0; d < days; d++) {
            b += b * hourlyRate * 24;
        }
        return (long) b - balance;
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    // ════════════════════════════════════════════════════════════
    //  TRANSACTION HISTORY
    // ════════════════════════════════════════════════════════════

    private static void openTransactionHistory(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x4, player, false);
        gui.setTitle(Component.literal("🕐 Transaction History"));
        RecipeGuiTheme.fillChest4Hub(gui);

        gui.setSlot(4, new GuiElementBuilder(Items.CLOCK)
                .setName(Component.literal("📜 Recent Transactions").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Your recent banking activity").withStyle(ChatFormatting.GRAY))
                .glow().build());

        String[] transactions = {
                "📥 +50,000 coins - Main Deposit",
                "📤 -25,000 coins - Savings Withdrawal",
                "📥 +100,000 coins - Shop Sale",
                "📤 -50,000 coins - Equipment Purchase",
                "📥 +25,000 coins - Bounty Reward",
                "📤 -10,000 coins - Bank Fee",
                "📥 +75,000 coins - Market Profit",
        };

        int[] txSlots = {10, 11, 12, 13, 14, 15, 16};
        for (int i = 0; i < transactions.length && i < txSlots.length; i++) {
            String tx = transactions[i];
            boolean isDeposit = tx.contains("+");
            gui.setSlot(txSlots[i], new GuiElementBuilder(isDeposit ? Items.EMERALD : Items.REDSTONE)
                    .setName(Component.literal("Transaction #" + (i + 1)).withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(tx).withStyle(isDeposit ? ChatFormatting.GREEN : ChatFormatting.RED))
                    .addLoreLine(Component.literal("2 hours ago").withStyle(ChatFormatting.DARK_GRAY))
                    .build());
        }

        gui.setSlot(27, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.GRAY))
                .setCallback((i, t, c, s) -> open(player))
                .build());

        gui.open();
    }

    // ════════════════════════════════════════════════════════════
    //  TRANSFER (PLAYER-TO-PLAYER)
    // ════════════════════════════════════════════════════════════

    private static void openTransfer(ServerPlayer player) {
        SignGui signGui = new SignGui(player) {
            @Override
            public void onManualClose() {
                String name = this.getLine(0).getString().trim();
                if (name.isEmpty()) { BankGui.open(player); return; }
                ServerPlayer target = PoliticalServer.server.getPlayerList().getPlayer(name);
                if (target == null) {
                    player.sendSystemMessage(Component.literal("✗ Player '" + name + "' not found or offline!").withStyle(ChatFormatting.RED), false);
                    BankGui.open(player);
                    return;
                }
                if (target.getUUID().equals(player.getUUID())) {
                    player.sendSystemMessage(Component.literal("✗ Cannot transfer to yourself!").withStyle(ChatFormatting.RED), false);
                    BankGui.open(player);
                    return;
                }
                openTransferAmountSign(player, target);
            }
        };
        signGui.setLine(0, Component.literal(""));
        signGui.setLine(1, Component.literal("Recipient name:"));
        signGui.setLine(2, Component.literal("(must be online)"));
        signGui.open();
    }

    private static void openTransferAmountSign(ServerPlayer player, ServerPlayer target) {
        SignGui signGui = new SignGui(player) {
            @Override
            public void onManualClose() {
                String input = this.getLine(0).getString().trim().replaceAll("[^0-9]", "");
                if (input.isEmpty()) { BankGui.open(player); return; }
                try {
                    int amount = Integer.parseInt(input);
                    if (amount <= 0) {
                        player.sendSystemMessage(Component.literal("✗ Amount must be positive!").withStyle(ChatFormatting.RED), false);
                        BankGui.open(player);
                        return;
                    }
                    executeTransfer(player, target, amount);
                } catch (NumberFormatException e) {
                    player.sendSystemMessage(Component.literal("✗ Invalid number!").withStyle(ChatFormatting.RED), false);
                    BankGui.open(player);
                }
            }
        };
        signGui.setLine(0, Component.literal(""));
        signGui.setLine(1, Component.literal("Amount to send"));
        signGui.setLine(2, Component.literal("to " + target.getName().getString()));
        signGui.open();
    }

    private static void executeTransfer(ServerPlayer sender, ServerPlayer target, int amount) {
        int balance = CoinManager.getCoins(sender);
        if (balance < amount) {
            sender.sendSystemMessage(Component.literal("✗ Insufficient coins!").withStyle(ChatFormatting.RED), false);
            openTransfer(sender);
            return;
        }

        CoinManager.removeCoins(sender, amount);
        CoinManager.giveCoins(target, amount);

        sender.sendSystemMessage(Component.literal("✓ Sent " + fmt(amount) + " coins to " + target.getName().getString())
                .withStyle(ChatFormatting.GREEN), false);
        target.sendSystemMessage(Component.literal("✓ Received " + fmt(amount) + " coins from " + sender.getName().getString())
                .withStyle(ChatFormatting.GREEN), false);

        DataManager.save(PoliticalServer.server);
        BankGui.open(sender);
    }

    // ════════════════════════════════════════════════════════════
    //  ADMIN VIEW
    // ════════════════════════════════════════════════════════════

    public static void openForAdmin(ServerPlayer admin, ServerPlayer target) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, admin, false);
        gui.setTitle(Component.literal("🏦 " + target.getName().getString() + "'s Bank"));

        for (int i = 0; i < 54; i++) {
            if (i % 9 == 0 || i % 9 == 8 || i < 9 || i >= 45) {
                gui.setSlot(i, new GuiElementBuilder(Items.GOLD_BLOCK)
                        .setName(Component.literal("").withStyle(ChatFormatting.YELLOW)).build());
            } else {
                gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                        .setName(Component.literal("")).build());
            }
        }

        int purse = CoinManager.getCoins(target);
        long main = DataManager.getBankMain(target.getUUID().toString());
        long savings = DataManager.getBankSavings(target.getUUID().toString());
        long lastWithdrawal = DataManager.getBankSavingsWithdrawTime(target.getUUID().toString());

        gui.setSlot(4, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Component.literal("💰 " + target.getName().getString() + "'s Wallet").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Balance: " + fmt(purse) + " coins").withStyle(ChatFormatting.YELLOW))
                .build());

        gui.setSlot(19, new GuiElementBuilder(Items.GOLD_BLOCK)
                .setName(Component.literal("🏦 Main Account").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Balance: " + fmt(main) + " coins").withStyle(ChatFormatting.WHITE))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to modify").withStyle(ChatFormatting.GRAY))
                .setCallback((i, t, c, s) -> modifyMainAccount(admin, target))
                .build());

        gui.setSlot(25, new GuiElementBuilder(Items.DIAMOND_BLOCK)
                .setName(Component.literal("💎 Savings Account").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Balance: " + fmt(savings) + " coins").withStyle(ChatFormatting.WHITE))
                .addLoreLine(Component.literal("Last withdrawal: " + (lastWithdrawal > 0 ? new java.util.Date(lastWithdrawal).toString() : "Never")).withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to modify").withStyle(ChatFormatting.GRAY))
                .setCallback((i, t, c, s) -> modifySavingsAccount(admin, target))
                .build());

        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("❌ Close").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD))
                .setCallback((i, t, c, s) -> admin.closeContainer())
                .build());
        gui.setSlot(48, new GuiElementBuilder(Items.EMERALD)
                .setName(Component.literal("✏ Edit Wallet").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Opens coin amount editor").withStyle(ChatFormatting.GRAY))
                .setCallback((i, t, c, s) -> AdminInvseeGui.openCoinsEditor(admin, target))
                .build());
        gui.setSlot(50, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back to Invsee").withStyle(ChatFormatting.YELLOW))
                .setCallback((i, t, c, s) -> AdminInvseeGui.openMainMenu(admin, target))
                .build());

        gui.open();
    }

    private static void modifyMainAccount(ServerPlayer admin, ServerPlayer target) {
        SignGui signGui = new SignGui(admin) {
            @Override
            public void onManualClose() {
                try {
                    int amount = Integer.parseInt(this.getLine(0).getString().trim());
                    DataManager.setBankMain(target.getUUID().toString(), amount);
                    admin.sendSystemMessage(Component.literal("✓ Set " + target.getName().getString() + "'s main account to " + fmt(amount) + " coins")
                            .withStyle(ChatFormatting.GREEN), false);
                    openForAdmin(admin, target);
                } catch (NumberFormatException e) {
                    admin.sendSystemMessage(Component.literal("✗ Invalid number!").withStyle(ChatFormatting.RED), false);
                    openForAdmin(admin, target);
                }
            }
        };
        signGui.setLine(0, Component.literal(""));
        signGui.setLine(1, Component.literal("^^^^^^^^^^^^^^").withStyle(ChatFormatting.YELLOW));
        signGui.setLine(2, Component.literal("Set main balance").withStyle(ChatFormatting.AQUA));
        signGui.open();
    }

    private static void modifySavingsAccount(ServerPlayer admin, ServerPlayer target) {
        SignGui signGui = new SignGui(admin) {
            @Override
            public void onManualClose() {
                try {
                    int amount = Integer.parseInt(this.getLine(0).getString().trim());
                    DataManager.setBankSavings(target.getUUID().toString(), amount);
                    admin.sendSystemMessage(Component.literal("✓ Set " + target.getName().getString() + "'s savings to " + fmt(amount) + " coins")
                            .withStyle(ChatFormatting.GREEN), false);
                    openForAdmin(admin, target);
                } catch (NumberFormatException e) {
                    admin.sendSystemMessage(Component.literal("✗ Invalid number!").withStyle(ChatFormatting.RED), false);
                    openForAdmin(admin, target);
                }
            }
        };
        signGui.setLine(0, Component.literal(""));
        signGui.setLine(1, Component.literal("^^^^^^^^^^^^^^").withStyle(ChatFormatting.YELLOW));
        signGui.setLine(2, Component.literal("Set savings balance").withStyle(ChatFormatting.AQUA));
        signGui.open();
    }
}
