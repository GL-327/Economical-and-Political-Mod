package com.political.government;
import com.political.DataManager;
import com.political.PoliticalServer;
import com.political.RulerGui;
import com.political.TaxManager;



import com.political.party.PoliticalPartyManager;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.gui.SignGui;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.List;

public class ChairGui {

    public static void open(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("👑 Chair Dashboard"));

        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.black())
                    .setName(Component.literal("")).build());
        }

        int[] goldBorder = {0, 1, 2, 3, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 50, 51, 52, 53};
        for (int slot : goldBorder) {
            gui.setSlot(slot, new GuiElementBuilder(Items.GOLD_BLOCK)
                    .setName(Component.literal("")).build());
        }

        gui.setSlot(4, new GuiElementBuilder(Items.GOLDEN_HELMET)
                .setName(Component.literal("👑 Chair Dashboard").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Welcome, Chair!").withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal("Manage your government").withStyle(ChatFormatting.GRAY))
                .glow().build());

        // ── Row 1: Economy Controls ──

        gui.setSlot(10, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Component.literal("💰 Main Interest Rate").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Current: " + formatRate(DataManager.getMainAccountInterestRate()) + "/hr").withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal("Max: " + formatRate(DataManager.getMaxInterestRate())).withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Left-click: +0.5%").withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal("Right-click: -0.5%").withStyle(ChatFormatting.RED))
                .addLoreLine(Component.literal("Shift-click: ±2.5%").withStyle(ChatFormatting.AQUA))
                .setCallback((index, type, containerInput, slotGui) -> {
                    double change = type.isLeft ? 0.005 : -0.005;
                    if (type.shift) change = type.isLeft ? 0.025 : -0.025;
                    DataManager.setMainAccountInterestRate(DataManager.getMainAccountInterestRate() + change);
                    DataManager.save(PoliticalServer.server);
                    broadcastRateChange(player, "Main Account", DataManager.getMainAccountInterestRate(), "per hour");
                    open(player);
                })
                .build());

        gui.setSlot(12, new GuiElementBuilder(Items.BARREL)
                .setName(Component.literal("🏦 Savings Interest Rate").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Current: " + formatRate(DataManager.getSavingsAccountInterestRate()) + "/hr").withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal("Max: " + formatRate(DataManager.getMaxInterestRate())).withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Left-click: +0.5%").withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal("Right-click: -0.5%").withStyle(ChatFormatting.RED))
                .addLoreLine(Component.literal("Shift-click: ±2.5%").withStyle(ChatFormatting.AQUA))
                .setCallback((index, type, containerInput, slotGui) -> {
                    double change = type.isLeft ? 0.005 : -0.005;
                    if (type.shift) change = type.isLeft ? 0.025 : -0.025;
                    DataManager.setSavingsAccountInterestRate(DataManager.getSavingsAccountInterestRate() + change);
                    DataManager.save(PoliticalServer.server);
                    broadcastRateChange(player, "Savings", DataManager.getSavingsAccountInterestRate(), "per hour");
                    open(player);
                })
                .build());

        gui.setSlot(14, new GuiElementBuilder(Items.GOLD_NUGGET)
                .setName(Component.literal("📊 Set Tax Rate").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Current: " + String.format("%.1f", TaxManager.getDailyTaxPercentage() * 100.0f) + "% of coins/day").withStyle(ChatFormatting.GOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Left-click: +0.5%").withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal("Right-click: -0.5%").withStyle(ChatFormatting.RED))
                .addLoreLine(Component.literal("Shift-click: ±2.5%").withStyle(ChatFormatting.AQUA))
                .setCallback((index, type, containerInput, slotGui) -> {
                    float current = TaxManager.getDailyTaxPercentage();
                    float change = type.isLeft ? 0.005f : -0.005f;
                    if (type.shift) change *= 5f;
                    TaxManager.setDailyTaxPercentage(current + change);
                    open(player);
                })
                .build());

        int vat = com.political.government.Treasury.vatPercent();
        gui.setSlot(15, new GuiElementBuilder(Items.EMERALD)
                .setName(Component.literal("VAT").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Current: " + vat + "% on shop and auction buys")
                        .withStyle(ChatFormatting.GOLD))
                .addLoreLine(com.political.government.Treasury.statusLine())
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Left-click: +1%").withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal("Right-click: -1%").withStyle(ChatFormatting.RED))
                .addLoreLine(Component.literal("Range: 0-30%").withStyle(ChatFormatting.GRAY))
                .setCallback((index, type, containerInput, slotGui) -> {
                    int change = type.isLeft ? 1 : -1;
                    if (type.shift) change *= 5;
                    com.political.government.Treasury.setVatPercent(vat + change);
                    open(player);
                })
                .build());

        boolean taxEnabled = TaxManager.isTaxEnabled();
        gui.setSlot(16, new GuiElementBuilder(taxEnabled ? Items.DYE.lime() : Items.DYE.red())
                .setName(Component.literal("📋 Tax System").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Status: " + (taxEnabled ? "§aENABLED" : "§cDISABLED")))
                .addLoreLine(Component.literal("Rate: " + String.format("%.1f", TaxManager.getDailyTaxPercentage() * 100.0f) + "% of coins/day").withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to toggle").withStyle(ChatFormatting.GRAY))
                .setCallback((index, type, containerInput, slotGui) -> {
                    TaxManager.setTaxEnabled(!TaxManager.isTaxEnabled());
                    open(player);
                })
                .build());

        // ── Row 2: Government ──

        long partyTotal = 0L;
        for (PoliticalPartyManager.Party p : PoliticalPartyManager.getParties()) {
            partyTotal += p.budgetCoins;
        }
        gui.setSlot(19, new GuiElementBuilder(Items.GOLD_BLOCK)
                .setName(Component.literal("Party budgets").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("All party pots: " + String.format("%,d", partyTotal) + " coins").withStyle(ChatFormatting.WHITE))
                .addLoreLine(Component.literal("Tax & levies split across parties").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to manage").withStyle(ChatFormatting.YELLOW))
                .setCallback((index, type, containerInput, slotGui) -> GovernmentTreasuryGui.openFrom(player, () -> open(player)))
                .build());

        gui.setSlot(21, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Component.literal("👥 View Debtors").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("View players in debt").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Click to view").withStyle(ChatFormatting.YELLOW))
                .setCallback((index, type, containerInput, slotGui) -> RulerGui.openDebtorsPage(player, "chair"))
                .build());

        gui.setSlot(23, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("⚖ Impeachment Status").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal(ElectionManager.isImpeachmentActive() ? "§cVote in progress!" : "§aNo active vote"))
                .build());

        gui.setSlot(25, new GuiElementBuilder(Items.WRITABLE_BOOK)
                .setName(Component.literal("📜 Loan Rate").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Current: " + formatRate(DataManager.getLoanInterestRate()) + "/day").withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal("Min: " + formatRate(DataManager.getMinLoanRate()) + " | Max: " + formatRate(DataManager.getMaxInterestRate())).withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Left-click: +0.5%").withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal("Right-click: -0.5%").withStyle(ChatFormatting.RED))
                .addLoreLine(Component.literal("Shift-click: ±2.5%").withStyle(ChatFormatting.AQUA))
                .setCallback((index, type, containerInput, slotGui) -> {
                    float change = type.isLeft ? 0.005f : -0.005f;
                    if (type.shift) change = type.isLeft ? 0.025f : -0.025f;
                    DataManager.setLoanInterestRate(DataManager.getLoanInterestRate() + change);
                    DataManager.save(PoliticalServer.server);
                    broadcastRateChange(player, "Loan", DataManager.getLoanInterestRate(), "per day");
                    open(player);
                })
                .build());

        // ── Row 3: Bank Controls ──

        boolean frozen = DataManager.areLoansAreFrozen();
        gui.setSlot(28, new GuiElementBuilder(frozen ? Items.PACKED_ICE : Items.BLUE_ICE)
                .setName(Component.literal("❄ Freeze Loans").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Status: " + (frozen ? "§c⚠ FROZEN" : "§aACTIVE")))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("When frozen, no new loans can be taken.").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Existing loans still accrue interest.").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to toggle").withStyle(ChatFormatting.YELLOW))
                .setCallback((index, type, containerInput, slotGui) -> {
                    boolean newState = !DataManager.areLoansAreFrozen();
                    DataManager.setLoansAreFrozen(newState);
                    DataManager.save(PoliticalServer.server);
                    broadcastBankPolicy(player, newState ? "❄ Loans have been FROZEN" : "✓ Loans have been UNFROZEN");
                    open(player);
                })
                .build());

        long govTreasury = DataManager.getGovernmentTreasuryLong();
        gui.setSlot(30, new GuiElementBuilder(Items.GOLD_BLOCK)
                .setName(Component.literal("Treasury").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(com.political.government.Treasury.statusLine())
                .addLoreLine(Component.literal("VAT: " + com.political.government.Treasury.vatPercent() + "%")
                        .withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal("Daily payroll: " + com.political.government.Treasury.format(
                        com.political.government.Treasury.dailyPayroll())).withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Negative balance is debt plus 5% daily interest.")
                        .withStyle(govTreasury < 0 ? ChatFormatting.RED : ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to enter bailout amount").withStyle(ChatFormatting.GREEN))
                .setCallback((index, type, containerInput, slotGui) -> openBailoutSign(player))
                .build());

        gui.setSlot(32, new GuiElementBuilder(Items.BOOK)
                .setName(Component.literal("📊 Bank Summary").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Main Rate: " + formatRate(DataManager.getMainAccountInterestRate()) + "/hr").withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal("Savings Rate: " + formatRate(DataManager.getSavingsAccountInterestRate()) + "/hr").withStyle(ChatFormatting.AQUA))
                .addLoreLine(Component.literal("Loan Rate: " + formatRate(DataManager.getLoanInterestRate()) + "/day").withStyle(ChatFormatting.RED))
                .addLoreLine(Component.literal("Loans: " + (frozen ? "§cFROZEN" : "§aActive")).withStyle(ChatFormatting.WHITE))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Savings tiers: x1.0 / x1.2 / x1.5").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Loan terms: Short / Standard / Long").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Grace period: 2 days on all loans").withStyle(ChatFormatting.GRAY))
                .build());

        // ── Row 4: Perks ──

        List<String> activePerks = PerkManager.getActivePerks();
        gui.setSlot(37, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Component.literal("✨ Active Perks").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal(activePerks.size() + " perks active").withStyle(ChatFormatting.GREEN))
                .glow().build());

        int perkSlot = 38;
        for (String perkId : activePerks) {
            if (perkSlot >= 44) break;
            Perk perk = PerkManager.getPerk(perkId);
            if (perk != null) {
                gui.setSlot(perkSlot, new GuiElementBuilder(Items.ENCHANTED_BOOK)
                        .setName(Component.literal(perk.name).withStyle(ChatFormatting.GREEN))
                        .addLoreLine(Component.literal(perk.description).withStyle(ChatFormatting.GRAY))
                        .addLoreLine(Component.literal("Points: " + perk.pointValue).withStyle(ChatFormatting.YELLOW))
                        .build());
                perkSlot += 2;
            }
        }

        boolean canChangePerks = PerkManager.canChangePerks(true);
        if (canChangePerks) {
            gui.setSlot(34, new GuiElementBuilder(Items.NETHER_STAR)
                    .setName(Component.literal("🌟 Select Perks").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Choose perks for this term").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("You have 6 perk points").withStyle(ChatFormatting.YELLOW))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to select").withStyle(ChatFormatting.GREEN))
                    .setCallback((index, type, containerInput, slotGui) -> PerksGui.openFrom(player, true, () -> open(player)))
                    .glow().build());
        } else {
            gui.setSlot(34, new GuiElementBuilder(Items.BARRIER)
                    .setName(Component.literal("🔒 Perks Locked").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Perks already selected").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("for this term!").withStyle(ChatFormatting.GRAY))
                    .build());
        }

        // Close button
        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("✖ Close").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .setCallback((slot, clicktype, containerInput, slotGui) -> gui.close())
                .build());

        gui.open();
    }

    private static void openBailoutSign(ServerPlayer player) {
        SignGui signGui = new SignGui(player) {
            @Override
            public void onManualClose() {
                String input = this.getLine(0).getString().trim().replaceAll("[^0-9]", "");
                if (input.isEmpty()) { ChairGui.open(player); return; }
                try {
                    int amount = Integer.parseInt(input);
                    if (amount <= 0) {
                        player.sendSystemMessage(Component.literal("✗ Amount must be positive!").withStyle(ChatFormatting.RED), false);
                        ChairGui.open(player); return;
                    }
                    int treasury = DataManager.getGovernmentTreasury();
                    if (treasury < amount) {
                        player.sendSystemMessage(Component.literal("✗ Not enough in government treasury! Available: " + String.format("%,d", treasury)).withStyle(ChatFormatting.RED), false);
                        ChairGui.open(player); return;
                    }
                    DataManager.removeGovernmentTreasury(amount);
                    DataManager.save(PoliticalServer.server);
                    broadcastBankPolicy(player, "💰 Injected " + String.format("%,d", amount) + " coins as a bank bailout");
                    player.sendSystemMessage(Component.literal("✓ Bailout of " + String.format("%,d", amount) + " coins applied from government treasury.").withStyle(ChatFormatting.GREEN), false);
                } catch (NumberFormatException e) {
                    player.sendSystemMessage(Component.literal("✗ Invalid number!").withStyle(ChatFormatting.RED), false);
                }
                ChairGui.open(player);
            }
        };
        signGui.setLine(0, Component.literal(""));
        signGui.setLine(1, Component.literal("Bailout amount:"));
        signGui.setLine(2, Component.literal("from treasury"));
        signGui.open();
    }

    private static String formatRate(double rate) {
        double pct = rate * 100;
        if (pct == (int) pct) return (int) pct + "%";
        return String.format("%.1f%%", pct);
    }

    private static void broadcastRateChange(ServerPlayer chair, String accountType, double newRate, String period) {
        if (PoliticalServer.server == null) return;
        String chairName = chair.getName().getString();
        Component message = Component.literal("📢 [Government] ")
                .withStyle(ChatFormatting.GOLD)
                .append(Component.literal(chairName).withStyle(ChatFormatting.YELLOW))
                .append(Component.literal(" has set the ").withStyle(ChatFormatting.GOLD))
                .append(Component.literal(accountType).withStyle(ChatFormatting.AQUA))
                .append(Component.literal(" interest rate to ").withStyle(ChatFormatting.GOLD))
                .append(Component.literal(formatRate(newRate) + " " + period).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        PoliticalServer.server.getPlayerList().broadcastSystemMessage(message, false);
    }

    private static void broadcastBankPolicy(ServerPlayer chair, String policyMessage) {
        if (PoliticalServer.server == null) return;
        String chairName = chair.getName().getString();
        Component message = Component.literal("📢 [Government] ")
                .withStyle(ChatFormatting.GOLD)
                .append(Component.literal(chairName).withStyle(ChatFormatting.YELLOW))
                .append(Component.literal(": ").withStyle(ChatFormatting.GOLD))
                .append(Component.literal(policyMessage).withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD));
        PoliticalServer.server.getPlayerList().broadcastSystemMessage(message, false);
    }
}
