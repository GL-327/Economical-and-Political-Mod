package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class ChairGui {

    public static void open(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("👑 Chair Dashboard"));

        // Dark elegant background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        // Decorative border with gold
        int[] goldBorder = {0, 1, 2, 3, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 50, 51, 52, 53};
        for (int slot : goldBorder) {
            gui.setSlot(slot, new GuiElementBuilder(Items.GOLD_BLOCK)
                    .setName(Text.literal("")).build());
        }

        // Header with crown
        gui.setSlot(4, new GuiElementBuilder(Items.GOLDEN_HELMET)
                .setName(Text.literal("👑 Chair Dashboard").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Welcome, Chair!").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal("Manage your government").formatted(Formatting.GRAY))
                .glow()
                .build());

        // ── Row 1: Economy Controls (with gaps) ──
        gui.setSlot(10, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Text.literal("💰 Main Interest Rate").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Current: " + formatRate(DataManager.getMainAccountInterestRate()) + "/hr").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Left-click: +1%").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal("Right-click: -1%").formatted(Formatting.RED))
                .addLoreLine(Text.literal("Shift-click: ±5%").formatted(Formatting.AQUA))
                .setCallback((index, type, action) -> {
                    double change = type.isLeft ? 0.01 : -0.01;
                    if (type.shift) change = type.isLeft ? 0.05 : -0.05;
                    DataManager.setMainAccountInterestRate(DataManager.getMainAccountInterestRate() + change);
                    DataManager.save(PoliticalServer.server);
                    broadcastInterestRateChange(player, "Main Account", DataManager.getMainAccountInterestRate());
                    open(player);
                })
                .build());

        // Gap at 11
        gui.setSlot(12, new GuiElementBuilder(Items.BARREL)
                .setName(Text.literal("🏦 Savings Interest Rate").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Current: " + formatRate(DataManager.getSavingsAccountInterestRate()) + "/hr").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Left-click: +1%").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal("Right-click: -1%").formatted(Formatting.RED))
                .addLoreLine(Text.literal("Shift-click: ±5%").formatted(Formatting.AQUA))
                .setCallback((index, type, action) -> {
                    double change = type.isLeft ? 0.01 : -0.01;
                    if (type.shift) change = type.isLeft ? 0.05 : -0.05;
                    DataManager.setSavingsAccountInterestRate(DataManager.getSavingsAccountInterestRate() + change);
                    DataManager.save(PoliticalServer.server);
                    broadcastInterestRateChange(player, "Savings Account", DataManager.getSavingsAccountInterestRate());
                    open(player);
                })
                .build());

        // Gap at 13
        gui.setSlot(14, new GuiElementBuilder(Items.GOLD_NUGGET)
                .setName(Text.literal("📊 Set Tax Amount").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Current: " + TaxManager.getDailyTaxAmount() + " credits/day").formatted(Formatting.GOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Left-click: +1").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal("Right-click: -1").formatted(Formatting.RED))
                .addLoreLine(Text.literal("Shift-click: ±5").formatted(Formatting.AQUA))
                .setCallback((index, type, action) -> {
                    int current = TaxManager.getDailyTaxAmount();
                    int change = type.isLeft ? 1 : -1;
                    if (type.shift) change *= 5;
                    int newAmount = Math.max(1, Math.min(100, current + change));
                    TaxManager.setDailyTaxAmount(newAmount);
                    open(player);
                })
                .build());

        // Tax toggle
        boolean taxEnabled = TaxManager.isTaxEnabled();
        gui.setSlot(15, new GuiElementBuilder(taxEnabled ? Items.LIME_DYE : Items.RED_DYE)
                .setName(Text.literal("📋 Tax System").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Status: " + (taxEnabled ? "§aENABLED" : "§cDISABLED")))
                .addLoreLine(Text.literal("Amount: " + TaxManager.getDailyTaxAmount() + " credits/day").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to toggle").formatted(Formatting.GRAY))
                .setCallback((index, type, action) -> {
                    TaxManager.setTaxEnabled(!TaxManager.isTaxEnabled());
                    open(player);
                })
                .build());

        // ── Row 2: Government (with gaps) ──
        int treasury = DataManager.getGovernmentTreasury();
        gui.setSlot(19, new GuiElementBuilder(Items.GOLD_BLOCK)
                .setName(Text.literal("🏛 Government Treasury").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Balance: " + String.format("%,d", treasury) + " coins").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to manage").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> GovernmentTreasuryGui.open(player))
                .build());

        // Gap at 20
        gui.setSlot(21, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Text.literal("👥 View Debtors").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("View players in debt").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Click to view").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> RulerGui.openDebtorsPage(player, "chair"))
                .build());

        // Gap at 22
        gui.setSlot(23, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("⚖ Impeachment Status").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal(ElectionManager.isImpeachmentActive() ? "§cVote in progress!" : "§aNo active vote"))
                .build());

        // ── Row 3: Perks (with gaps) ──
        List<String> activePerks = PerkManager.getActivePerks();
        gui.setSlot(28, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Text.literal("✨ Active Perks").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal(activePerks.size() + " perks active").formatted(Formatting.GREEN))
                .glow().build());

        // Show active perks
        int perkSlot = 29;
        for (String perkId : activePerks) {
            if (perkSlot >= 35) break;
            Perk perk = PerkManager.getPerk(perkId);
            if (perk != null) {
                gui.setSlot(perkSlot, new GuiElementBuilder(Items.ENCHANTED_BOOK)
                        .setName(Text.literal(perk.name).formatted(Formatting.GREEN))
                        .addLoreLine(Text.literal(perk.description).formatted(Formatting.GRAY))
                        .addLoreLine(Text.literal("Points: " + perk.pointValue).formatted(Formatting.YELLOW))
                        .build());
                perkSlot += 2; // Gap between perks
            }
        }

        // Perks selection button
        boolean canChangePerks = PerkManager.canChangePerks(true);
        if (canChangePerks) {
            gui.setSlot(40, new GuiElementBuilder(Items.NETHER_STAR)
                    .setName(Text.literal("🌟 Select Perks").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Choose perks for this term").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("You have 6 perk points").formatted(Formatting.YELLOW))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Click to select").formatted(Formatting.GREEN))
                    .setCallback((index, type, action) -> PerksGui.open(player, true))
                    .glow().build());
        } else {
            gui.setSlot(40, new GuiElementBuilder(Items.BARRIER)
                    .setName(Text.literal("🔒 Perks Locked").formatted(Formatting.RED, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Perks already selected").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("for this term!").formatted(Formatting.GRAY))
                    .build());
        }

        // Close button
        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("✖ Close").formatted(Formatting.RED, Formatting.BOLD))
                .setCallback((slot, clickType, action) -> gui.close())
                .build());

        gui.open();
    }

    /** Formats a fractional rate (e.g. 0.05) as a percent string (e.g. "5%"). */
    private static String formatRate(double rate) {
        int pct = (int) Math.round(rate * 100);
        return pct + "%";
    }

    /** Broadcasts a server-wide announcement when an interest rate changes. */
    private static void broadcastInterestRateChange(ServerPlayerEntity chair, String accountType, double newRate) {
        if (PoliticalServer.server == null) return;
        String chairName = chair.getName().getString();
        Text message = Text.literal("📢 [Government] ")
                .formatted(Formatting.GOLD)
                .append(Text.literal(chairName).formatted(Formatting.YELLOW))
                .append(Text.literal(" has set the ").formatted(Formatting.GOLD))
                .append(Text.literal(accountType).formatted(Formatting.AQUA))
                .append(Text.literal(" interest rate to ").formatted(Formatting.GOLD))
                .append(Text.literal(formatRate(newRate) + " per hour").formatted(Formatting.GREEN, Formatting.BOLD));
        PoliticalServer.server.getPlayerManager().broadcast(message, false);
    }
}