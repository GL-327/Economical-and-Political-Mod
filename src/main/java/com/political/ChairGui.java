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
        gui.setTitle(Text.literal("Chair Dashboard"));

        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        gui.setSlot(4, new GuiElementBuilder(Items.GOLDEN_HELMET)
                .setName(Text.literal("Chair Dashboard").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Welcome, Chair!").formatted(Formatting.GREEN))
                .glow()
                .build());

        gui.setSlot(19, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Text.literal("Active Perks").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal(PerkManager.getActivePerks().size() + " perks active").formatted(Formatting.GREEN))
                .build());

        List<String> activePerks = PerkManager.getActivePerks();
        int perkSlot = 28;
        for (String perkId : activePerks) {
            if (perkSlot >= 35) break;
            Perk perk = PerkManager.getPerk(perkId);
            if (perk != null) {
                gui.setSlot(perkSlot, new GuiElementBuilder(Items.ENCHANTED_BOOK)
                        .setName(Text.literal(perk.name).formatted(Formatting.GREEN))
                        .addLoreLine(Text.literal(perk.description).formatted(Formatting.GRAY))
                        .addLoreLine(Text.literal("Points: " + perk.pointValue).formatted(Formatting.YELLOW))
                        .build());
                perkSlot++;
            }
        }

        boolean taxEnabled = TaxManager.isTaxEnabled();
        gui.setSlot(21, new GuiElementBuilder(taxEnabled ? Items.GOLD_BLOCK : Items.GOLD_INGOT)
                .setName(Text.literal("Tax System").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Status: " + (taxEnabled ? "ENABLED" : "DISABLED")).formatted(taxEnabled ? Formatting.GREEN : Formatting.RED))
                .addLoreLine(Text.literal("Daily Amount: " + TaxManager.getDailyTaxAmount() + " credits").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to toggle").formatted(Formatting.GRAY))
                .setCallback((index, type, action) -> {
                    TaxManager.setTaxEnabled(!TaxManager.isTaxEnabled());
                    open(player);
                })
                .build());

        gui.setSlot(22, new GuiElementBuilder(Items.GOLD_NUGGET)
                .setName(Text.literal("Set Tax Amount").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Current: " + TaxManager.getDailyTaxAmount() + " credits").formatted(Formatting.GOLD))
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

        gui.setSlot(23, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Text.literal("View Debtors").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal("Click to view players in debt").formatted(Formatting.GRAY))
                .setCallback((index, type, action) -> {
                    RulerGui.openDebtorsPage(player, "chair");
                })
                .build());

        boolean canChangePerks = PerkManager.canChangePerks(true);
        if (canChangePerks) {
            gui.setSlot(40, new GuiElementBuilder(Items.NETHER_STAR)
                    .setName(Text.literal("Select Perks").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Choose perks for this term").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("You have 6 perk points").formatted(Formatting.YELLOW))
                    .setCallback((index, type, action) -> {
                        PerksGui.open(player, true);
                    })
                    .build());
        } else {
            gui.setSlot(40, new GuiElementBuilder(Items.BARRIER)
                    .setName(Text.literal("Perks Locked").formatted(Formatting.RED, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Perks have already been").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("selected for this term!").formatted(Formatting.GRAY))
                    .build());
        }

        gui.setSlot(25, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("Impeachment Status").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal(ElectionManager.isImpeachmentActive() ? "Vote in progress!" : "No active vote").formatted(ElectionManager.isImpeachmentActive() ? Formatting.RED : Formatting.GREEN))
                .build());

        gui.open();
    }
}