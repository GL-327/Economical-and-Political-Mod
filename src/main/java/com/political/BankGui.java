package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.gui.SignGui;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class BankGui {

    private static final int MAIN_MAX = 100_000;
    private static final long SAVINGS_COOLDOWN_MS = TimeUnit.HOURS.toMillis(3);

    /** Formats a fractional rate (e.g. 0.05) as a percent string like "5%". */
    private static String fmtRate(double rate) {
        int pct = (int) Math.round(rate * 100);
        return pct + "%";
    }

    // ════════════════════════════════════════════════════════════
    // MAIN BANK MENU
    // ════════════════════════════════════════════════════════════

    public static void open(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("🏦 National Bank"));

        // Fill background with decorative pattern
        for (int i = 0; i < 54; i++) {
            if (i % 9 == 0 || i % 9 == 8 || i < 9 || i >= 45) {
                gui.setSlot(i, new GuiElementBuilder(Items.GOLD_BLOCK)
                        .setName(Text.literal("⬛").formatted(Formatting.YELLOW))
                        .build());
            } else {
                gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                        .setName(Text.literal("")).build());
            }
        }

        // Slot 4 — header: player's purse balance
        int purse = CoinManager.getCoins(player);
        gui.setSlot(4, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Text.literal("💰 Your Wallet").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Balance: " + fmt(purse) + " coins").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("🏛 Secure banking services").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("💳 Safe coin storage").formatted(Formatting.GRAY))
                .glow()
                .build());

        String uuid = player.getUuidAsString();

        // Slot 13 — Main Account
        int main = DataManager.getBankMain(uuid);
        gui.setSlot(13, new GuiElementBuilder(Items.ENDER_CHEST)
                .setName(Text.literal("� Main Account").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Balance: " + fmt(main) + " coins").formatted(Formatting.WHITE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("📊 Cap: " + fmt(MAIN_MAX) + " coins").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("📈 Interest: " + fmtRate(DataManager.getMainAccountInterestRate()) + "/hr").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("✨ Click to manage").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> openMainAccount(player))
                .build());

        // Slot 22 — Savings Account
        int savings = DataManager.getBankSavings(uuid);
        gui.setSlot(22, new GuiElementBuilder(Items.BARREL)
                .setName(Text.literal("🏦 Savings Account").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Balance: " + fmt(savings) + " coins").formatted(Formatting.WHITE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("♾️ Unlimited deposits").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("📈 Interest: " + fmtRate(DataManager.getSavingsAccountInterestRate()) + "/hr").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal("⏰ Withdrawal: 3hr cooldown").formatted(Formatting.RED))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("✨ Click to manage").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> openSavingsAccount(player))
                .build());

        // Slot 31 — Loans
        int loan = DataManager.getBankLoan(uuid);
        GuiElementBuilder loansBtn = new GuiElementBuilder(Items.WRITABLE_BOOK)
                .setName(Text.literal("� Loan Services").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""));
        if (loan > 0) {
            loansBtn.addLoreLine(Text.literal("💳 Current Debt: " + fmt(loan) + " coins").formatted(Formatting.RED, Formatting.BOLD));
            loansBtn.addLoreLine(Text.literal("📈 Interest: 5% per hour").formatted(Formatting.DARK_RED));
        } else {
            loansBtn.addLoreLine(Text.literal("✅ No active loan").formatted(Formatting.GREEN));
            loansBtn.addLoreLine(Text.literal("💰 Available: 10,000 coins").formatted(Formatting.GRAY));
            loansBtn.addLoreLine(Text.literal("📈 Rate: 5% per hour").formatted(Formatting.YELLOW));
        }
        loansBtn.addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("✨ Click to manage").formatted(Formatting.YELLOW));
        loansBtn.setCallback((index, type, action) -> openLoans(player));
        gui.setSlot(31, loansBtn.build());

        // Bottom row - Navigation buttons
        gui.setSlot(47, new GuiElementBuilder(Items.CLOCK)
                .setName(Text.literal("🕐 Transaction History").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("View recent transactions").formatted(Formatting.GRAY))
                .setCallback((index, type, action) -> {
                    openTransactionHistory(player);
                })
                .build());

        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("❌ Close").formatted(Formatting.DARK_RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Exit the bank").formatted(Formatting.GRAY))
                .setCallback((index, type, action) -> player.closeHandledScreen())
                .build());

        gui.setSlot(51, new GuiElementBuilder(Items.BOOK)
                .setName(Text.literal("📖 Help & Info").formatted(Formatting.BLUE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Banking information").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Interest rates & fees").formatted(Formatting.GRAY))
                .setCallback((index, type, action) -> {
                    player.sendMessage(Text.literal("Main account: 2% interest, capped at 100k").formatted(Formatting.GRAY), false);
                    player.sendMessage(Text.literal("Savings: 1.5% interest, unlimited, 3hr withdrawal cooldown").formatted(Formatting.GRAY), false);
                    player.sendMessage(Text.literal("Loans: 5% interest, 10k max").formatted(Formatting.GRAY), false);
                })
                .build());

        // Slot 40 — Crypto Wallet
        double cryptoValue = CryptoMarket.getWalletValue(player.getUuidAsString());
        gui.setSlot(40, new GuiElementBuilder(Items.END_CRYSTAL)
                .setName(Text.literal("🔮 Crypto Wallet").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Value: $" + CryptoMarket.formatCrypto(cryptoValue)).formatted(Formatting.GOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§7View your cryptocurrency holdings"))
                .addLoreLine(Text.literal("§7Staking, DeFi, Margin trading"))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§eClick to open"))
                .setCallback((index, type, action) -> CryptoMarketGui.openMainMenu(player))
                .glow()
                .build());

        // Slot 48 — Transfer (Player-to-Player)
        gui.setSlot(48, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Text.literal("💸 Transfer").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Send coins to another player").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§eClick to transfer"))
                .setCallback((index, type, action) -> openTransfer(player))
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
                .addLoreLine(Text.literal("Earns " + fmtRate(DataManager.getMainAccountInterestRate()) + " interest per hour").formatted(Formatting.GREEN))
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

        // Custom Deposit button (slot 25)
        gui.setSlot(25, new GuiElementBuilder(Items.WRITABLE_BOOK)
                .setName(Text.literal("Custom Deposit").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal("Enter a custom amount to deposit").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Click to open sign input").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> openMainCustomDeposit(player))
                .build());

        // Custom Withdraw button (slot 26)
        gui.setSlot(26, new GuiElementBuilder(Items.BOOK)
                .setName(Text.literal("Custom Withdraw").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal("Enter a custom amount to withdraw").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Click to open sign input").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> openMainCustomWithdraw(player))
                .build());

        // Back button (slot 49)
        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.GRAY))
                .setCallback((index, type, action) -> open(player))
                .build());

        gui.open();
    }

    // ════════════════════════════════════════════════════════════
    // MAIN ACCOUNT CUSTOM AMOUNT SIGN GUIs
    // ════════════════════════════════════════════════════════════

    private static void openMainCustomDeposit(ServerPlayerEntity player) {
        String uuid = player.getUuidAsString();
        SignGui signGui = new SignGui(player) {
            @Override
            public void onClose() {
                String input = this.getLine(0).getString().trim().replaceAll("[^0-9]", "");
                if (input.isEmpty()) { openMainAccount(player); return; }
                try {
                    int amount = Integer.parseInt(input);
                    if (amount <= 0) { player.sendMessage(Text.literal("✗ Amount must be positive!").formatted(Formatting.RED), false); openMainAccount(player); return; }
                    int cur = DataManager.getBankMain(uuid);
                    int purse = CoinManager.getCoins(player);
                    if (cur >= MAIN_MAX) { player.sendMessage(Text.literal("✗ Main account is at cap!").formatted(Formatting.RED), false); openMainAccount(player); return; }
                    if (purse < amount) { player.sendMessage(Text.literal("✗ Not enough coins in purse!").formatted(Formatting.RED), false); openMainAccount(player); return; }
                    int space = MAIN_MAX - cur;
                    int actual = Math.min(amount, space);
                    CoinManager.removeCoins(player, actual);
                    DataManager.setBankMain(uuid, cur + actual);
                    player.sendMessage(Text.literal("✓ Deposited " + fmt(actual) + " coins into Main Account.").formatted(Formatting.GREEN), false);
                } catch (NumberFormatException e) {
                    player.sendMessage(Text.literal("✗ Invalid number!").formatted(Formatting.RED), false);
                }
                openMainAccount(player);
            }
        };
        signGui.setLine(0, Text.literal(""));
        signGui.setLine(1, Text.literal("Enter amount:"));
        signGui.setLine(2, Text.literal("to deposit"));
        signGui.open();
    }

    private static void openMainCustomWithdraw(ServerPlayerEntity player) {
        String uuid = player.getUuidAsString();
        SignGui signGui = new SignGui(player) {
            @Override
            public void onClose() {
                String input = this.getLine(0).getString().trim().replaceAll("[^0-9]", "");
                if (input.isEmpty()) { openMainAccount(player); return; }
                try {
                    int amount = Integer.parseInt(input);
                    if (amount <= 0) { player.sendMessage(Text.literal("✗ Amount must be positive!").formatted(Formatting.RED), false); openMainAccount(player); return; }
                    int bal = DataManager.getBankMain(uuid);
                    if (bal < amount) { player.sendMessage(Text.literal("✗ Not enough in Main Account! Balance: " + fmt(bal)).formatted(Formatting.RED), false); openMainAccount(player); return; }
                    DataManager.setBankMain(uuid, bal - amount);
                    CoinManager.giveCoins(player, amount);
                    player.sendMessage(Text.literal("✓ Withdrew " + fmt(amount) + " coins from Main Account.").formatted(Formatting.GREEN), false);
                } catch (NumberFormatException e) {
                    player.sendMessage(Text.literal("✗ Invalid number!").formatted(Formatting.RED), false);
                }
                openMainAccount(player);
            }
        };
        signGui.setLine(0, Text.literal(""));
        signGui.setLine(1, Text.literal("Enter amount:"));
        signGui.setLine(2, Text.literal("to withdraw"));
        signGui.open();
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
                .addLoreLine(Text.literal("💰 Earns " + fmtRate(DataManager.getSavingsAccountInterestRate()) + " interest per hour!").formatted(Formatting.GREEN))
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

        // Custom Deposit button (slot 25)
        gui.setSlot(25, new GuiElementBuilder(Items.WRITABLE_BOOK)
                .setName(Text.literal("Custom Deposit").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal("Enter a custom amount to deposit").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Click to open sign input").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> openSavingsCustomDeposit(player))
                .build());

        // Custom Withdraw button (slot 26, only if not on cooldown)
        if (!onCooldown) {
            gui.setSlot(26, new GuiElementBuilder(Items.BOOK)
                    .setName(Text.literal("Custom Withdraw").formatted(Formatting.YELLOW, Formatting.BOLD))
                    .addLoreLine(Text.literal("Enter a custom amount to withdraw").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("Click to open sign input").formatted(Formatting.YELLOW))
                    .setCallback((index, type, action) -> openSavingsCustomWithdraw(player))
                    .build());
        }

        // Back button (slot 49)
        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.GRAY))
                .setCallback((index, type, action) -> open(player))
                .build());

        gui.open();
    }

    // ════════════════════════════════════════════════════════════
    // SAVINGS ACCOUNT CUSTOM AMOUNT SIGN GUIs
    // ════════════════════════════════════════════════════════════

    private static void openSavingsCustomDeposit(ServerPlayerEntity player) {
        String uuid = player.getUuidAsString();
        SignGui signGui = new SignGui(player) {
            @Override
            public void onClose() {
                String input = this.getLine(0).getString().trim().replaceAll("[^0-9]", "");
                if (input.isEmpty()) { openSavingsAccount(player); return; }
                try {
                    int amount = Integer.parseInt(input);
                    if (amount <= 0) { player.sendMessage(Text.literal("✗ Amount must be positive!").formatted(Formatting.RED), false); openSavingsAccount(player); return; }
                    int purse = CoinManager.getCoins(player);
                    if (purse < amount) { player.sendMessage(Text.literal("✗ Not enough coins in purse!").formatted(Formatting.RED), false); openSavingsAccount(player); return; }
                    CoinManager.removeCoins(player, amount);
                    DataManager.setBankSavings(uuid, DataManager.getBankSavings(uuid) + amount);
                    player.sendMessage(Text.literal("✓ Deposited " + fmt(amount) + " coins into Savings Account.").formatted(Formatting.GREEN), false);
                } catch (NumberFormatException e) {
                    player.sendMessage(Text.literal("✗ Invalid number!").formatted(Formatting.RED), false);
                }
                openSavingsAccount(player);
            }
        };
        signGui.setLine(0, Text.literal(""));
        signGui.setLine(1, Text.literal("Enter amount:"));
        signGui.setLine(2, Text.literal("to deposit"));
        signGui.open();
    }

    private static void openSavingsCustomWithdraw(ServerPlayerEntity player) {
        String uuid = player.getUuidAsString();
        SignGui signGui = new SignGui(player) {
            @Override
            public void onClose() {
                String input = this.getLine(0).getString().trim().replaceAll("[^0-9]", "");
                if (input.isEmpty()) { openSavingsAccount(player); return; }
                try {
                    int amount = Integer.parseInt(input);
                    if (amount <= 0) { player.sendMessage(Text.literal("✗ Amount must be positive!").formatted(Formatting.RED), false); openSavingsAccount(player); return; }
                    long nowCheck = System.currentTimeMillis();
                    long lastW = DataManager.getBankSavingsWithdrawTime(uuid);
                    if (lastW > 0 && (nowCheck - lastW) < SAVINGS_COOLDOWN_MS) {
                        long rem = SAVINGS_COOLDOWN_MS - (nowCheck - lastW);
                        long h = rem / 3_600_000L; long m = (rem % 3_600_000L) / 60_000L;
                        player.sendMessage(Text.literal("✗ Withdrawal on cooldown! Wait " + h + "h " + m + "m.").formatted(Formatting.RED), false);
                        openSavingsAccount(player); return;
                    }
                    int bal = DataManager.getBankSavings(uuid);
                    if (bal < amount) { player.sendMessage(Text.literal("✗ Not enough in Savings! Balance: " + fmt(bal)).formatted(Formatting.RED), false); openSavingsAccount(player); return; }
                    DataManager.setBankSavings(uuid, bal - amount);
                    DataManager.setBankSavingsWithdrawTime(uuid, nowCheck);
                    CoinManager.giveCoins(player, amount);
                    player.sendMessage(Text.literal("✓ Withdrew " + fmt(amount) + " coins from Savings Account.").formatted(Formatting.GREEN), false);
                } catch (NumberFormatException e) {
                    player.sendMessage(Text.literal("✗ Invalid number!").formatted(Formatting.RED), false);
                }
                openSavingsAccount(player);
            }
        };
        signGui.setLine(0, Text.literal(""));
        signGui.setLine(1, Text.literal("Enter amount:"));
        signGui.setLine(2, Text.literal("to withdraw"));
        signGui.open();
    }

    // ════════════════════════════════════════════════════════════
    // LOANS SUB-MENU
    // ════════════════════════════════════════════════════════════

    private static final int MAX_LOANS = 5;
    private static final int MAX_LOAN_AMOUNT = 250_000;
    // Interest rates for each loan slot (5%, 10%, 20%, 40%, 80%)
    private static final double[] LOAN_RATES = {0.05, 0.10, 0.20, 0.40, 0.80};

    private static void openLoans(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("📜 Loans"));

        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.RED_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        String uuid = player.getUuidAsString();
        java.util.List<DataManager.LoanEntry> loans = DataManager.getLoans(uuid);
        int purse = CoinManager.getCoins(player);

        // Show each active loan
        int[] loanSlots = {10, 12, 14, 16, 18};
        for (int i = 0; i < loans.size(); i++) {
            DataManager.LoanEntry loan = loans.get(i);
            int slot = loanSlots[i];
            int ratePct = (int) Math.round(loan.interestRate * 100);
            final int loanIndex = i;

            boolean canPayFull = purse >= loan.amount;
            GuiElementBuilder loanItem = new GuiElementBuilder(Items.PAPER)
                    .setName(Text.literal("Loan #" + (i + 1)).formatted(Formatting.RED, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Amount owed: " + fmt(loan.amount) + " coins").formatted(Formatting.RED))
                    .addLoreLine(Text.literal("Interest: " + ratePct + "% per hour").formatted(Formatting.DARK_RED))
                    .addLoreLine(Text.literal("Your purse: " + fmt(purse) + " coins").formatted(Formatting.GRAY))
                    .glow();
            gui.setSlot(slot, loanItem.build());

            // Repay button
            int repaySlot = slot + 9;
            if (canPayFull) {
                gui.setSlot(repaySlot, new GuiElementBuilder(Items.EMERALD)
                        .setName(Text.literal("Pay Back Loan #" + (i + 1)).formatted(Formatting.GREEN, Formatting.BOLD))
                        .addLoreLine(Text.literal("Full amount: " + fmt(loan.amount) + " coins").formatted(Formatting.WHITE))
                        .addLoreLine(Text.literal("Click to pay in full").formatted(Formatting.YELLOW))
                        .setCallback((index, type, action) -> {
                            java.util.List<DataManager.LoanEntry> current = DataManager.getLoans(uuid);
                            if (loanIndex >= current.size()) {
                                player.sendMessage(Text.literal("✗ Loan no longer exists!").formatted(Formatting.RED), false);
                                openLoans(player); return;
                            }
                            int owed = current.get(loanIndex).amount;
                            int p = CoinManager.getCoins(player);
                            if (p < owed) {
                                player.sendMessage(Text.literal("✗ Not enough coins to pay back the loan!").formatted(Formatting.RED), false);
                            } else {
                                CoinManager.removeCoins(player, owed);
                                DataManager.removeLoan(uuid, loanIndex);
                                player.sendMessage(Text.literal("✓ Loan #" + (loanIndex + 1) + " of " + fmt(owed) + " coins repaid in full!").formatted(Formatting.GREEN), false);
                            }
                            openLoans(player);
                        })
                        .build());
            } else {
                gui.setSlot(repaySlot, new GuiElementBuilder(Items.EMERALD)
                        .setName(Text.literal("Pay Back Loan #" + (i + 1)).formatted(Formatting.DARK_GRAY, Formatting.BOLD))
                        .addLoreLine(Text.literal("✗ Not enough coins in purse").formatted(Formatting.RED))
                        .addLoreLine(Text.literal("Need " + fmt(loan.amount - purse) + " more coins").formatted(Formatting.YELLOW))
                        .build());
            }

            // Custom repay button
            int customSlot = repaySlot + 9;
            gui.setSlot(customSlot, new GuiElementBuilder(Items.WRITABLE_BOOK)
                    .setName(Text.literal("Custom Repay Loan #" + (i + 1)).formatted(Formatting.YELLOW, Formatting.BOLD))
                    .addLoreLine(Text.literal("Enter a custom amount to repay").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("Click to open sign input").formatted(Formatting.YELLOW))
                    .setCallback((index, type, action) -> openLoanCustomRepay(player, loanIndex))
                    .build());
        }

        // "Take New Loan" button (if fewer than MAX_LOANS active)
        if (loans.size() < MAX_LOANS) {
            int nextLoanIndex = loans.size();
            double nextRate = LOAN_RATES[nextLoanIndex];
            int nextRatePct = (int) Math.round(nextRate * 100);
            gui.setSlot(4, new GuiElementBuilder(Items.GOLD_NUGGET)
                    .setName(Text.literal("Take New Loan").formatted(Formatting.YELLOW, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Custom amount: 1 – " + fmt(MAX_LOAN_AMOUNT) + " coins").formatted(Formatting.WHITE))
                    .addLoreLine(Text.literal("Interest: " + nextRatePct + "% per hour").formatted(Formatting.RED))
                    .addLoreLine(Text.literal("Active loans: " + loans.size() + "/" + MAX_LOANS).formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Click to enter amount via sign").formatted(Formatting.GREEN))
                    .setCallback((index, type, action) -> openLoanAmountSign(player))
                    .build());
        } else {
            gui.setSlot(4, new GuiElementBuilder(Items.BARRIER)
                    .setName(Text.literal("Maximum Loans Reached").formatted(Formatting.RED, Formatting.BOLD))
                    .addLoreLine(Text.literal("You have " + MAX_LOANS + "/" + MAX_LOANS + " active loans.").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("Repay a loan before taking another.").formatted(Formatting.GRAY))
                    .build());
        }

        // Back button (slot 49)
        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.GRAY))
                .setCallback((index, type, action) -> open(player))
                .build());

        gui.open();
    }

    // ════════════════════════════════════════════════════════════
    // TAKE NEW LOAN — SIGN INPUT
    // ════════════════════════════════════════════════════════════

    private static void openLoanAmountSign(ServerPlayerEntity player) {
        String uuid = player.getUuidAsString();
        SignGui signGui = new SignGui(player) {
            @Override
            public void onClose() {
                String input = this.getLine(0).getString().trim().replaceAll("[^0-9]", "");
                if (input.isEmpty()) { openLoans(player); return; }
                try {
                    int amount = Integer.parseInt(input);
                    if (amount <= 0) {
                        player.sendMessage(Text.literal("✗ Amount must be positive!").formatted(Formatting.RED), false);
                        openLoans(player); return;
                    }
                    if (amount > MAX_LOAN_AMOUNT) {
                        player.sendMessage(Text.literal("✗ Maximum loan amount is " + fmt(MAX_LOAN_AMOUNT) + " coins!").formatted(Formatting.RED), false);
                        openLoans(player); return;
                    }
                    java.util.List<DataManager.LoanEntry> existing = DataManager.getLoans(uuid);
                    if (existing.size() >= MAX_LOANS) {
                        player.sendMessage(Text.literal("✗ You already have the maximum " + MAX_LOANS + " loans!").formatted(Formatting.RED), false);
                        openLoans(player); return;
                    }
                    double rate = LOAN_RATES[existing.size()];
                    int ratePct = (int) Math.round(rate * 100);
                    DataManager.addLoan(uuid, amount, rate);
                    CoinManager.giveCoins(player, amount);
                    player.sendMessage(Text.literal("✓ Loan of " + fmt(amount) + " coins issued at " + ratePct + "% interest per hour!").formatted(Formatting.GREEN), false);
                } catch (NumberFormatException e) {
                    player.sendMessage(Text.literal("✗ Invalid number!").formatted(Formatting.RED), false);
                }
                openLoans(player);
            }
        };
        signGui.setLine(0, Text.literal(""));
        signGui.setLine(1, Text.literal("Enter amount:"));
        signGui.setLine(2, Text.literal("(max " + fmt(MAX_LOAN_AMOUNT) + ")"));
        signGui.open();
    }

    // ════════════════════════════════════════════════════════════
    // LOAN CUSTOM REPAYMENT SIGN GUI
    // ════════════════════════════════════════════════════════════

    private static void openLoanCustomRepay(ServerPlayerEntity player, int loanIndex) {
        String uuid = player.getUuidAsString();
        SignGui signGui = new SignGui(player) {
            @Override
            public void onClose() {
                String input = this.getLine(0).getString().trim().replaceAll("[^0-9]", "");
                if (input.isEmpty()) { openLoans(player); return; }
                try {
                    int amount = Integer.parseInt(input);
                    if (amount <= 0) { player.sendMessage(Text.literal("✗ Amount must be positive!").formatted(Formatting.RED), false); openLoans(player); return; }
                    java.util.List<DataManager.LoanEntry> loans = DataManager.getLoans(uuid);
                    if (loanIndex >= loans.size()) { player.sendMessage(Text.literal("✗ Loan no longer exists!").formatted(Formatting.RED), false); openLoans(player); return; }
                    int owed = loans.get(loanIndex).amount;
                    int purse = CoinManager.getCoins(player);
                    int toPay = Math.min(amount, Math.min(owed, purse));
                    if (toPay <= 0) { player.sendMessage(Text.literal("✗ Not enough coins in purse!").formatted(Formatting.RED), false); openLoans(player); return; }
                    CoinManager.removeCoins(player, toPay);
                    int remaining = owed - toPay;
                    if (remaining <= 0) {
                        DataManager.removeLoan(uuid, loanIndex);
                        player.sendMessage(Text.literal("✓ Loan #" + (loanIndex + 1) + " fully repaid!").formatted(Formatting.GREEN), false);
                    } else {
                        DataManager.setLoanAmount(uuid, loanIndex, remaining);
                        player.sendMessage(Text.literal("✓ Repaid " + fmt(toPay) + " coins toward loan #" + (loanIndex + 1) + ". Remaining: " + fmt(remaining) + ".").formatted(Formatting.GREEN), false);
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage(Text.literal("✗ Invalid number!").formatted(Formatting.RED), false);
                }
                openLoans(player);
            }
        };
        signGui.setLine(0, Text.literal(""));
        signGui.setLine(1, Text.literal("Enter amount:"));
        signGui.setLine(2, Text.literal("to repay"));
        signGui.open();
    }

    // ════════════════════════════════════════════════════════════
    // HELPERS
    // ════════════════════════════════════════════════════════════

    private static String fmt(int amount) {
        return String.format(Locale.US, "%,d", amount);
    }

    // ════════════════════════════════════════════════════════════
    // TRANSACTION HISTORY
    // ════════════════════════════════════════════════════════════

    private static void openTransactionHistory(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("🕐 Transaction History"));

        // Decorative background
        for (int i = 0; i < 54; i++) {
            if (i % 9 == 0 || i % 9 == 8 || i < 9 || i >= 45) {
                gui.setSlot(i, new GuiElementBuilder(Items.GOLD_BLOCK)
                        .setName(Text.literal("⬛").formatted(Formatting.YELLOW))
                        .build());
            } else {
                gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                        .setName(Text.literal("")).build());
            }
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.BOOK)
                .setName(Text.literal("📜 Recent Transactions").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Your recent banking activity").formatted(Formatting.GRAY))
                .glow()
                .build());

        // Sample transaction data (in a real implementation, this would come from a database)
        String[] transactions = {
                "📥 +50,000 coins - Main Account Deposit",
                "📤 -25,000 coins - Savings Withdrawal", 
                "📥 +100,000 coins - Shop Sale",
                "📤 -50,000 coins - Equipment Purchase",
                "📥 +25,000 coins - Bounty Reward",
                "📤 -10,000 coins - Bank Fee",
                "📥 +75,000 coins - Market Profit",
                "📤 -15,000 coins - Auction Purchase"
        };

        // Display transactions
        int slot = 9;
        for (int i = 0; i < transactions.length && slot < 45; i++) {
            String transaction = transactions[i];
            boolean isDeposit = transaction.contains("+");
            Item icon = isDeposit ? Items.EMERALD : Items.REDSTONE;
            Formatting color = isDeposit ? Formatting.GREEN : Formatting.RED;
            
            gui.setSlot(slot, new GuiElementBuilder(icon)
                    .setName(Text.literal("Transaction #" + (i + 1)).formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal(transaction).formatted(color))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("2 hours ago").formatted(Formatting.DARK_GRAY))
                    .build());
            slot++;
        }

        // Navigation buttons
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to Bank").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> open(player))
                .build());

        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("❌ Close").formatted(Formatting.DARK_RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Exit transaction history").formatted(Formatting.GRAY))
                .setCallback((index, type, action) -> player.closeHandledScreen())
                .build());

        gui.setSlot(53, new GuiElementBuilder(Items.FEATHER)
                .setName(Text.literal("📄 Export History").formatted(Formatting.BLUE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Download full history").formatted(Formatting.GRAY))
                .setCallback((index, type, action) -> {
                    player.sendMessage(Text.literal("Transaction export feature coming soon!").formatted(Formatting.BLUE), false);
                })
                .build());

        gui.open();
    }

    // ════════════════════════════════════════════════════════════
    // TRANSFER (PLAYER-TO-PLAYER)
    // ════════════════════════════════════════════════════════════

    private static void openTransfer(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("💸 Transfer Coins"));

        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.LIME_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        int purse = CoinManager.getCoins(player);

        gui.setSlot(4, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Text.literal("Your Wallet").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Available: " + fmt(purse) + " coins").formatted(Formatting.YELLOW))
                .glow()
                .build());

        // Quick transfer amounts
        int[] amounts = {100, 1000, 10000, 50000};
        int[] slots = {19, 20, 21, 22};
        for (int i = 0; i < amounts.length; i++) {
            final int amount = amounts[i];
            boolean canAfford = purse >= amount;
            GuiElementBuilder btn = new GuiElementBuilder(Items.EMERALD)
                    .setName(Text.literal("Send " + fmt(amount)).formatted(
                            canAfford ? Formatting.GREEN : Formatting.DARK_GRAY, Formatting.BOLD));
            if (canAfford) {
                btn.addLoreLine(Text.literal("Click to select recipient").formatted(Formatting.YELLOW));
                btn.setCallback((index, type, action) -> openTransferRecipient(player, amount));
            } else {
                btn.addLoreLine(Text.literal("Not enough coins").formatted(Formatting.RED));
            }
            gui.setSlot(slots[i], btn.build());
        }

        // Custom amount via sign
        gui.setSlot(24, new GuiElementBuilder(Items.WRITABLE_BOOK)
                .setName(Text.literal("Custom Amount").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Enter any amount").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§eClick to enter"))
                .setCallback((index, type, action) -> {
                    SignGui signGui = new SignGui(player) {
                        @Override
                        public void onClose() {
                            String input = this.getLine(0).getString().trim().replaceAll("[^0-9]", "");
                            if (input.isEmpty()) { openTransfer(player); return; }
                            try {
                                int amount = Integer.parseInt(input);
                                if (amount <= 0) {
                                    player.sendMessage(Text.literal("✗ Amount must be positive!").formatted(Formatting.RED), false);
                                    openTransfer(player);
                                    return;
                                }
                                if (CoinManager.getCoins(player) < amount) {
                                    player.sendMessage(Text.literal("✗ Not enough coins!").formatted(Formatting.RED), false);
                                    openTransfer(player);
                                    return;
                                }
                                openTransferRecipient(player, amount);
                            } catch (NumberFormatException e) {
                                player.sendMessage(Text.literal("✗ Invalid number!").formatted(Formatting.RED), false);
                                openTransfer(player);
                            }
                        }
                    };
                    signGui.setLine(0, Text.literal(""));
                    signGui.setLine(1, Text.literal("Enter amount:"));
                    signGui.setLine(2, Text.literal(""));
                    signGui.open();
                })
                .build());

        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to Bank").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> open(player))
                .build());

        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("❌ Close").formatted(Formatting.DARK_RED, Formatting.BOLD))
                .setCallback((index, type, action) -> player.closeHandledScreen())
                .build());

        gui.open();
    }

    private static void openTransferRecipient(ServerPlayerEntity player, int amount) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("💸 Select Recipient"));

        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.LIME_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        gui.setSlot(4, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Text.literal("Sending: " + fmt(amount) + " coins").formatted(Formatting.GOLD, Formatting.BOLD))
                .glow()
                .build());

        // Show online players
        int slot = 9;
        for (ServerPlayerEntity target : PoliticalServer.server.getPlayerManager().getPlayerList()) {
            if (target.getUuid().equals(player.getUuid())) continue;
            if (slot >= 45) break;

            gui.setSlot(slot, new GuiElementBuilder(Items.PLAYER_HEAD)
                    .setName(Text.literal(target.getName().getString()).formatted(Formatting.GREEN))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Click to send " + fmt(amount) + " coins").formatted(Formatting.YELLOW))
                    .setCallback((index, type, action) -> executeTransfer(player, target, amount))
                    .build());
            slot++;
        }

        // Manual username entry via sign
        gui.setSlot(45, new GuiElementBuilder(Items.WRITABLE_BOOK)
                .setName(Text.literal("Enter Username").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Type player's name").formatted(Formatting.GRAY))
                .setCallback((index, type, action) -> {
                    SignGui signGui = new SignGui(player) {
                        @Override
                        public void onClose() {
                            String name = this.getLine(0).getString().trim();
                            if (name.isEmpty()) { openTransferRecipient(player, amount); return; }
                            ServerPlayerEntity target = PoliticalServer.server.getPlayerManager().getPlayer(name);
                            if (target == null) {
                                player.sendMessage(Text.literal("✗ Player '" + name + "' not found or offline!").formatted(Formatting.RED), false);
                                openTransferRecipient(player, amount);
                                return;
                            }
                            if (target.getUuid().equals(player.getUuid())) {
                                player.sendMessage(Text.literal("✗ Cannot send to yourself!").formatted(Formatting.RED), false);
                                openTransferRecipient(player, amount);
                                return;
                            }
                            executeTransfer(player, target, amount);
                        }
                    };
                    signGui.setLine(0, Text.literal(""));
                    signGui.setLine(1, Text.literal("Enter username:"));
                    signGui.open();
                })
                .build());

        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("❌ Cancel").formatted(Formatting.DARK_RED, Formatting.BOLD))
                .setCallback((index, type, action) -> openTransfer(player))
                .build());

        gui.open();
    }

    private static void executeTransfer(ServerPlayerEntity sender, ServerPlayerEntity target, int amount) {
        int balance = CoinManager.getCoins(sender);
        if (balance < amount) {
            sender.sendMessage(Text.literal("✗ Insufficient coins!").formatted(Formatting.RED), false);
            openTransfer(sender);
            return;
        }

        CoinManager.removeCoins(sender, amount);
        CoinManager.giveCoins(target, amount);

        sender.sendMessage(Text.literal("✓ Sent " + fmt(amount) + " coins to " + target.getName().getString())
                .formatted(Formatting.GREEN), false);
        target.sendMessage(Text.literal("✓ Received " + fmt(amount) + " coins from " + sender.getName().getString())
                .formatted(Formatting.GREEN), false);

        DataManager.save(PoliticalServer.server);
        BankGui.open(sender);
    }

    // Admin version for invsee - allows admin to view/modify target's bank
    public static void openForAdmin(ServerPlayerEntity admin, ServerPlayerEntity target) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, admin, false);
        gui.setTitle(Text.literal("🏦 " + target.getName().getString() + "'s Bank"));

        // Fill background
        for (int i = 0; i < 54; i++) {
            if (i % 9 == 0 || i % 9 == 8 || i < 9 || i >= 45) {
                gui.setSlot(i, new GuiElementBuilder(Items.GOLD_BLOCK)
                        .setName(Text.literal("⬛").formatted(Formatting.YELLOW))
                        .build());
            } else {
                gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                        .setName(Text.literal("")).build());
            }
        }

        // Show target's bank info
        int purse = CoinManager.getCoins(target);
        int main = DataManager.getBankMain(target.getUuidAsString());
        int savings = DataManager.getBankSavings(target.getUuidAsString());
        long lastWithdrawal = DataManager.getBankSavingsWithdrawTime(target.getUuidAsString());
        
        gui.setSlot(4, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Text.literal("💰 " + target.getName().getString() + "'s Wallet").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal("Balance: " + fmt(purse) + " coins").formatted(Formatting.YELLOW))
                .build());

        gui.setSlot(19, new GuiElementBuilder(Items.GOLD_BLOCK)
                .setName(Text.literal("🏦 Main Account").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal("Balance: " + fmt(main) + " coins").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to modify").formatted(Formatting.GRAY))
                .setCallback((index, type, action) -> modifyMainAccount(admin, target))
                .build());

        gui.setSlot(25, new GuiElementBuilder(Items.DIAMOND_BLOCK)
                .setName(Text.literal("💎 Savings Account").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal("Balance: " + fmt(savings) + " coins").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("Last withdrawal: " + (lastWithdrawal > 0 ? new java.util.Date(lastWithdrawal).toString() : "Never")).formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to modify").formatted(Formatting.GRAY))
                .setCallback((index, type, action) -> modifySavingsAccount(admin, target))
                .build());

        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("❌ Close").formatted(Formatting.DARK_RED, Formatting.BOLD))
                .setCallback((index, type, action) -> admin.closeHandledScreen())
                .build());

        gui.open();
    }
    
    private static void modifyMainAccount(ServerPlayerEntity admin, ServerPlayerEntity target) {
        SignGui signGui = new SignGui(admin) {
            @Override
            public void onClose() {
                try {
                    int amount = Integer.parseInt(this.getLine(0).getString().trim());
                    DataManager.setBankMain(target.getUuidAsString(), amount);
                    admin.sendMessage(Text.literal("✓ Set " + target.getName().getString() + "'s main account to " + fmt(amount) + " coins")
                        .formatted(Formatting.GREEN), false);
                    openForAdmin(admin, target);
                } catch (NumberFormatException e) {
                    admin.sendMessage(Text.literal("✗ Invalid number!").formatted(Formatting.RED), false);
                    openForAdmin(admin, target);
                }
            }
        };
        signGui.setLine(0, Text.literal("Enter amount:"));
        signGui.open();
    }
    
    private static void modifySavingsAccount(ServerPlayerEntity admin, ServerPlayerEntity target) {
        SignGui signGui = new SignGui(admin) {
            @Override
            public void onClose() {
                try {
                    int amount = Integer.parseInt(this.getLine(0).getString().trim());
                    DataManager.setBankSavings(target.getUuidAsString(), amount);
                    admin.sendMessage(Text.literal("✓ Set " + target.getName().getString() + "'s savings to " + fmt(amount) + " coins")
                        .formatted(Formatting.GREEN), false);
                    openForAdmin(admin, target);
                } catch (NumberFormatException e) {
                    admin.sendMessage(Text.literal("✗ Invalid number!").formatted(Formatting.RED), false);
                    openForAdmin(admin, target);
                }
            }
        };
        signGui.setLine(0, Text.literal("Enter amount:"));
        signGui.open();
    }
}
