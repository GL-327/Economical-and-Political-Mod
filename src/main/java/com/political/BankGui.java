package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class BankGui {

    private static final int MAIN_MAX = 100_000;
    private static final long SAVINGS_COOLDOWN_MS = TimeUnit.HOURS.toMillis(6);

    // ════════════════════════════════════════════════════════════
    // MAIN BANK MENU
    // ════════════════════════════════════════════════════════════

    public static void open(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("🏦 Bank"));

        // Fill background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        // Slot 4 — header: player's purse balance
        int purse = CoinManager.getCoins(player);
        gui.setSlot(4, new GuiElementBuilder(Items.SUNFLOWER)
                .setName(Text.literal("Your Purse: " + fmt(purse) + " coins").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Welcome to the bank!").formatted(Formatting.GRAY))
                .glow()
                .build());

        String uuid = player.getUuidAsString();

        // Slot 10 — Main Account
        int main = DataManager.getBankMain(uuid);
        gui.setSlot(10, new GuiElementBuilder(Items.CHEST)
                .setName(Text.literal("💰 Main Account").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Balance: " + fmt(main) + " coins").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("Cap: " + fmt(MAIN_MAX) + " coins").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Interest: 1% per hour").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to manage").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> openMainAccount(player))
                .build());

        // Slot 12 — Savings Account
        int savings = DataManager.getBankSavings(uuid);
        gui.setSlot(12, new GuiElementBuilder(Items.BARREL)
                .setName(Text.literal("🏦 Savings Account").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Balance: " + fmt(savings) + " coins").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("No deposit cap").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("⚠ Withdraw cooldown: 6 hours").formatted(Formatting.RED))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to manage").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> openSavingsAccount(player))
                .build());

        // Slot 14 — Loans
        int loan = DataManager.getBankLoan(uuid);
        GuiElementBuilder loansBtn = new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal("📜 Loans").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""));
        if (loan > 0) {
            loansBtn.addLoreLine(Text.literal("Debt owed: " + fmt(loan) + " coins").formatted(Formatting.RED));
            loansBtn.addLoreLine(Text.literal("Interest: 5% per hour").formatted(Formatting.DARK_RED));
        } else {
            loansBtn.addLoreLine(Text.literal("No active loan").formatted(Formatting.GREEN));
            loansBtn.addLoreLine(Text.literal("Available: 10,000 coins @ 5%/hr").formatted(Formatting.GRAY));
        }
        loansBtn.addLoreLine(Text.literal("")).addLoreLine(Text.literal("Click to manage").formatted(Formatting.YELLOW));
        loansBtn.setCallback((index, type, action) -> openLoans(player));
        gui.setSlot(14, loansBtn.build());

        // Slot 49 — Close
        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("Close").formatted(Formatting.RED))
                .setCallback((index, type, action) -> player.closeHandledScreen())
                .build());

        gui.open();
    }

    // ════════════════════════════════════════════════════════════
    // MAIN ACCOUNT SUB-MENU
    // ════════════════════════════════════════════════════════════

    private static void openMainAccount(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("💰 Main Account"));

        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLUE_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        String uuid = player.getUuidAsString();
        int main = DataManager.getBankMain(uuid);
        int purse = CoinManager.getCoins(player);

        // Slot 4 — balance header
        gui.setSlot(4, new GuiElementBuilder(Items.CHEST)
                .setName(Text.literal("Main Account Balance").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Balance: " + fmt(main) + " coins").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("Your purse: " + fmt(purse) + " coins").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Cap: " + fmt(MAIN_MAX) + " coins").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Earns 1% interest per hour").formatted(Formatting.GREEN))
                .glow()
                .build());

        boolean atCap = main >= MAIN_MAX;

        // Deposit buttons (slots 19, 20, 21)
        int[] depositAmounts = {1000, 5000, 10000};
        int[] depositSlots = {19, 20, 21};
        for (int i = 0; i < depositAmounts.length; i++) {
            final int amount = depositAmounts[i];
            boolean canDeposit = !atCap && purse >= amount;
            GuiElementBuilder btn = new GuiElementBuilder(Items.EMERALD)
                    .setName(Text.literal("Deposit " + fmt(amount) + " coins").formatted(
                            canDeposit ? Formatting.GREEN : Formatting.DARK_GRAY, Formatting.BOLD));
            if (atCap) {
                btn.addLoreLine(Text.literal("✗ Account is at cap (" + fmt(MAIN_MAX) + ")").formatted(Formatting.RED));
            } else if (purse < amount) {
                btn.addLoreLine(Text.literal("✗ Not enough coins in purse").formatted(Formatting.RED));
            } else {
                int space = MAIN_MAX - main;
                int actual = Math.min(amount, space);
                btn.addLoreLine(Text.literal("Will deposit: " + fmt(actual) + " coins").formatted(Formatting.GRAY));
                btn.addLoreLine(Text.literal("Click to deposit").formatted(Formatting.YELLOW));
            }
            if (canDeposit) {
                btn.setCallback((index, type, action) -> {
                    int cur = DataManager.getBankMain(uuid);
                    int p = CoinManager.getCoins(player);
                    if (cur >= MAIN_MAX) {
                        player.sendMessage(Text.literal("✗ Main account is at cap!").formatted(Formatting.RED), false);
                        openMainAccount(player);
                        return;
                    }
                    if (p < amount) {
                        player.sendMessage(Text.literal("✗ Not enough coins!").formatted(Formatting.RED), false);
                        openMainAccount(player);
                        return;
                    }
                    int space = MAIN_MAX - cur;
                    int actual = Math.min(amount, space);
                    CoinManager.removeCoins(player, actual);
                    DataManager.setBankMain(uuid, cur + actual);
                    player.sendMessage(Text.literal("✓ Deposited " + fmt(actual) + " coins into Main Account.").formatted(Formatting.GREEN), false);
                    openMainAccount(player);
                });
            }
            gui.setSlot(depositSlots[i], btn.build());
        }

        // Withdraw all button (slot 23)
        GuiElementBuilder withdrawBtn = new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Text.literal("Withdraw All").formatted(main > 0 ? Formatting.GREEN : Formatting.DARK_GRAY, Formatting.BOLD));
        if (main > 0) {
            withdrawBtn.addLoreLine(Text.literal("Will receive: " + fmt(main) + " coins").formatted(Formatting.GRAY));
            withdrawBtn.addLoreLine(Text.literal("Click to withdraw all").formatted(Formatting.YELLOW));
            withdrawBtn.setCallback((index, type, action) -> {
                int bal = DataManager.getBankMain(uuid);
                if (bal <= 0) {
                    player.sendMessage(Text.literal("✗ Nothing to withdraw!").formatted(Formatting.RED), false);
                } else {
                    DataManager.setBankMain(uuid, 0);
                    CoinManager.giveCoins(player, bal);
                    player.sendMessage(Text.literal("✓ Withdrew " + fmt(bal) + " coins from Main Account.").formatted(Formatting.GREEN), false);
                }
                openMainAccount(player);
            });
        } else {
            withdrawBtn.addLoreLine(Text.literal("No balance to withdraw").formatted(Formatting.RED));
        }
        gui.setSlot(23, withdrawBtn.build());

        // Back button (slot 49)
        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.GRAY))
                .setCallback((index, type, action) -> open(player))
                .build());

        gui.open();
    }

    // ════════════════════════════════════════════════════════════
    // SAVINGS ACCOUNT SUB-MENU
    // ════════════════════════════════════════════════════════════

    private static void openSavingsAccount(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("🏦 Savings Account"));

        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.CYAN_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        String uuid = player.getUuidAsString();
        int savings = DataManager.getBankSavings(uuid);
        int purse = CoinManager.getCoins(player);
        long lastWithdraw = DataManager.getBankSavingsWithdrawTime(uuid);
        long now = System.currentTimeMillis();
        long elapsed = now - lastWithdraw;
        boolean onCooldown = lastWithdraw > 0 && elapsed < SAVINGS_COOLDOWN_MS;
        long remainingMs = onCooldown ? (SAVINGS_COOLDOWN_MS - elapsed) : 0;

        // Slot 4 — balance + warning header
        gui.setSlot(4, new GuiElementBuilder(Items.BARREL)
                .setName(Text.literal("Savings Account Balance").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Balance: " + fmt(savings) + " coins").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("Your purse: " + fmt(purse) + " coins").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("⚠ You can only withdraw once every 6 hours!").formatted(Formatting.RED))
                .glow()
                .build());

        // Deposit buttons (slots 19, 20, 21)
        int[] depositAmounts = {1000, 5000, 10000};
        int[] depositSlots = {19, 20, 21};
        for (int i = 0; i < depositAmounts.length; i++) {
            final int amount = depositAmounts[i];
            boolean canDeposit = purse >= amount;
            GuiElementBuilder btn = new GuiElementBuilder(Items.EMERALD)
                    .setName(Text.literal("Deposit " + fmt(amount) + " coins").formatted(
                            canDeposit ? Formatting.GREEN : Formatting.DARK_GRAY, Formatting.BOLD));
            if (!canDeposit) {
                btn.addLoreLine(Text.literal("✗ Not enough coins in purse").formatted(Formatting.RED));
            } else {
                btn.addLoreLine(Text.literal("Click to deposit").formatted(Formatting.YELLOW));
                btn.setCallback((index, type, action) -> {
                    int p = CoinManager.getCoins(player);
                    if (p < amount) {
                        player.sendMessage(Text.literal("✗ Not enough coins!").formatted(Formatting.RED), false);
                        openSavingsAccount(player);
                        return;
                    }
                    CoinManager.removeCoins(player, amount);
                    DataManager.setBankSavings(uuid, DataManager.getBankSavings(uuid) + amount);
                    player.sendMessage(Text.literal("✓ Deposited " + fmt(amount) + " coins into Savings Account.").formatted(Formatting.GREEN), false);
                    openSavingsAccount(player);
                });
            }
            gui.setSlot(depositSlots[i], btn.build());
        }

        // Withdraw button (slot 23)
        GuiElementBuilder withdrawBtn = new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Text.literal("Withdraw All").formatted(
                        (!onCooldown && savings > 0) ? Formatting.GREEN : Formatting.DARK_GRAY, Formatting.BOLD));
        if (onCooldown) {
            long hoursLeft = remainingMs / 3_600_000L;
            long minutesLeft = (remainingMs % 3_600_000L) / 60_000L;
            withdrawBtn.addLoreLine(Text.literal("⚠ Cooldown active!").formatted(Formatting.RED));
            withdrawBtn.addLoreLine(Text.literal("Wait: " + hoursLeft + "h " + minutesLeft + "m").formatted(Formatting.YELLOW));
        } else if (savings <= 0) {
            withdrawBtn.addLoreLine(Text.literal("No balance to withdraw").formatted(Formatting.RED));
        } else {
            withdrawBtn.addLoreLine(Text.literal("Will receive: " + fmt(savings) + " coins").formatted(Formatting.GRAY));
            withdrawBtn.addLoreLine(Text.literal("Click to withdraw all").formatted(Formatting.YELLOW));
            withdrawBtn.setCallback((index, type, action) -> {
                long nowCheck = System.currentTimeMillis();
                long lastW = DataManager.getBankSavingsWithdrawTime(uuid);
                if (lastW > 0 && (nowCheck - lastW) < SAVINGS_COOLDOWN_MS) {
                    long rem = SAVINGS_COOLDOWN_MS - (nowCheck - lastW);
                    long h = rem / 3_600_000L;
                    long m = (rem % 3_600_000L) / 60_000L;
                    player.sendMessage(Text.literal("✗ Withdrawal on cooldown! Wait " + h + "h " + m + "m.").formatted(Formatting.RED), false);
                    openSavingsAccount(player);
                    return;
                }
                int bal = DataManager.getBankSavings(uuid);
                if (bal <= 0) {
                    player.sendMessage(Text.literal("✗ Nothing to withdraw!").formatted(Formatting.RED), false);
                } else {
                    DataManager.setBankSavings(uuid, 0);
                    DataManager.setBankSavingsWithdrawTime(uuid, nowCheck);
                    CoinManager.giveCoins(player, bal);
                    player.sendMessage(Text.literal("✓ Withdrew " + fmt(bal) + " coins from Savings Account.").formatted(Formatting.GREEN), false);
                }
                openSavingsAccount(player);
            });
        }
        gui.setSlot(23, withdrawBtn.build());

        // Back button (slot 49)
        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.GRAY))
                .setCallback((index, type, action) -> open(player))
                .build());

        gui.open();
    }

    // ════════════════════════════════════════════════════════════
    // LOANS SUB-MENU
    // ════════════════════════════════════════════════════════════

    private static void openLoans(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("📜 Loans"));

        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.RED_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        String uuid = player.getUuidAsString();
        int loan = DataManager.getBankLoan(uuid);
        int purse = CoinManager.getCoins(player);

        if (loan <= 0) {
            // No active loan — offer to take one
            gui.setSlot(4, new GuiElementBuilder(Items.PAPER)
                    .setName(Text.literal("No Active Loan").formatted(Formatting.GREEN, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("You have no outstanding loan.").formatted(Formatting.GRAY))
                    .build());

            gui.setSlot(22, new GuiElementBuilder(Items.GOLD_NUGGET)
                    .setName(Text.literal("Take Loan: 10,000 coins").formatted(Formatting.YELLOW, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Amount: 10,000 coins").formatted(Formatting.WHITE))
                    .addLoreLine(Text.literal("Interest: 5% per hour").formatted(Formatting.RED))
                    .addLoreLine(Text.literal("Debt is automatically collected").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("from your accounts when online.").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Click to accept loan").formatted(Formatting.GREEN))
                    .setCallback((index, type, action) -> {
                        int existingLoan = DataManager.getBankLoan(uuid);
                        if (existingLoan > 0) {
                            player.sendMessage(Text.literal("✗ You already have an active loan!").formatted(Formatting.RED), false);
                        } else {
                            DataManager.setBankLoan(uuid, 10_000);
                            CoinManager.giveCoins(player, 10_000);
                            player.sendMessage(Text.literal("✓ Loan of 10,000 coins issued. Repay with 5% interest per hour!").formatted(Formatting.GREEN), false);
                        }
                        openLoans(player);
                    })
                    .build());

        } else {
            // Active loan
            gui.setSlot(4, new GuiElementBuilder(Items.PAPER)
                    .setName(Text.literal("Active Loan").formatted(Formatting.RED, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Amount owed: " + fmt(loan) + " coins").formatted(Formatting.RED))
                    .addLoreLine(Text.literal("Interest: 5% per hour").formatted(Formatting.DARK_RED))
                    .addLoreLine(Text.literal("Your purse: " + fmt(purse) + " coins").formatted(Formatting.GRAY))
                    .glow()
                    .build());

            boolean canPay = purse >= loan;
            GuiElementBuilder payBtn = new GuiElementBuilder(Items.EMERALD)
                    .setName(Text.literal("Pay Back Loan").formatted(canPay ? Formatting.GREEN : Formatting.DARK_GRAY, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Full amount: " + fmt(loan) + " coins").formatted(Formatting.WHITE));
            if (!canPay) {
                payBtn.addLoreLine(Text.literal("✗ Not enough coins in purse").formatted(Formatting.RED));
                payBtn.addLoreLine(Text.literal("Need " + fmt(loan - purse) + " more coins").formatted(Formatting.YELLOW));
            } else {
                payBtn.addLoreLine(Text.literal("Click to pay in full").formatted(Formatting.YELLOW));
                payBtn.setCallback((index, type, action) -> {
                    int owed = DataManager.getBankLoan(uuid);
                    int p = CoinManager.getCoins(player);
                    if (p < owed) {
                        player.sendMessage(Text.literal("✗ Not enough coins to pay back the loan!").formatted(Formatting.RED), false);
                    } else {
                        CoinManager.removeCoins(player, owed);
                        DataManager.setBankLoan(uuid, 0);
                        player.sendMessage(Text.literal("✓ Loan of " + fmt(owed) + " coins repaid in full!").formatted(Formatting.GREEN), false);
                    }
                    openLoans(player);
                });
            }
            gui.setSlot(22, payBtn.build());
        }

        // Back button (slot 49)
        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.GRAY))
                .setCallback((index, type, action) -> open(player))
                .build());

        gui.open();
    }

    // ════════════════════════════════════════════════════════════
    // HELPERS
    // ════════════════════════════════════════════════════════════

    private static String fmt(int amount) {
        return String.format(Locale.US, "%,d", amount);
    }
}
