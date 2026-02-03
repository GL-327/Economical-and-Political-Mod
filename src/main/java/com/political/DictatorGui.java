package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Map;

public class DictatorGui {

    public static void open(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("⚠ Dictator Control Panel ⚠"));

        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.RED_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        gui.setSlot(4, new GuiElementBuilder(Items.DRAGON_HEAD)
                .setName(Text.literal("DICTATOR CONTROL").formatted(Formatting.DARK_RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("You have absolute power!").formatted(Formatting.RED))
                .glow()
                .build());
// CHANGED: Check if perks can be changed before opening
        boolean canChangePerks = PerkManager.canChangePerks(true);  // Dictator uses Chair rules
        if (canChangePerks) {
            gui.setSlot(37, new GuiElementBuilder(Items.NETHER_STAR)
                    .setName(Text.literal("Open Perks Menu").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Select server perks").formatted(Formatting.GRAY))
                    .setCallback((index, type, action) -> {
                        PerksGui.open(player, true);
                    })
                    .build());
        } else {
            gui.setSlot(37, new GuiElementBuilder(Items.BARRIER)
                    .setName(Text.literal("Perks Locked").formatted(Formatting.RED, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Perks have already been").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("selected for this term!").formatted(Formatting.GRAY))
                    .build());
        }
        boolean taxEnabled = DictatorManager.isDictatorTaxEnabled();
        int taxAmount = DictatorManager.getDictatorTaxAmount();

        gui.setSlot(19, new GuiElementBuilder(Items.GOLD_BLOCK)
                .setName(Text.literal("Tax: 10 Credits/Day").formatted(Formatting.GOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal(taxEnabled && taxAmount == 10 ? "✓ ACTIVE" : "Click to enable").formatted(taxEnabled && taxAmount == 10 ? Formatting.GREEN : Formatting.GRAY))
                .glow(taxEnabled && taxAmount == 10)
                .setCallback((index, type, action) -> {
                    DictatorManager.setDictatorTax(true, 10);
                    open(player);
                })
                .build());

        gui.setSlot(20, new GuiElementBuilder(Items.DIAMOND_BLOCK)
                .setName(Text.literal("Tax: 20 Credits/Day").formatted(Formatting.AQUA))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal(taxEnabled && taxAmount == 20 ? "✓ ACTIVE" : "Click to enable").formatted(taxEnabled && taxAmount == 20 ? Formatting.GREEN : Formatting.GRAY))
                .glow(taxEnabled && taxAmount == 20)
                .setCallback((index, type, action) -> {
                    DictatorManager.setDictatorTax(true, 20);
                    open(player);
                })
                .build());

        gui.setSlot(21, new GuiElementBuilder(Items.NETHERITE_BLOCK)
                .setName(Text.literal("Tax: 30 Credits/Day").formatted(Formatting.DARK_GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal(taxEnabled && taxAmount == 30 ? "✓ ACTIVE" : "Click to enable").formatted(taxEnabled && taxAmount == 30 ? Formatting.GREEN : Formatting.GRAY))
                .glow(taxEnabled && taxAmount == 30)
                .setCallback((index, type, action) -> {
                    DictatorManager.setDictatorTax(true, 30);
                    open(player);
                })
                .build());

        gui.setSlot(22, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("Disable Tax").formatted(Formatting.RED))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal(taxEnabled ? "Click to disable" : "Tax already disabled").formatted(Formatting.GRAY))
                .setCallback((index, type, action) -> {
                    DictatorManager.setDictatorTax(false, 0);
                    open(player);
                })
                .build());

        gui.setSlot(25, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Text.literal("View Debtors").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("View and summon players in debt").formatted(Formatting.GRAY))
                .setCallback((index, type, action) -> {
                    openDebtorsPage(player);
                })
                .build());

        gui.setSlot(43, new GuiElementBuilder(Items.LIGHTNING_ROD)
                .setName(Text.literal("Smite Players").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Use /smite <player> to strike").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Cooldown: 15 seconds").formatted(Formatting.RED))
                .build());

        gui.open();
    }

    public static void openDebtorsPage(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("Players in Debt - Click to Summon"));

        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.RED_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        Map<String, Integer> allTaxes = TaxManager.getAllTaxOwed();

        int slot = 10;
        for (Map.Entry<String, Integer> entry : allTaxes.entrySet()) {
            if (slot == 17) slot = 19;
            if (slot == 26) slot = 28;
            if (slot == 35) slot = 37;
            if (slot >= 44) break;

            if (entry.getValue() <= 0) continue;

            String debtorUuid = entry.getKey();
            String debtorName = DataManager.getPlayerName(debtorUuid);
            int owed = entry.getValue();

            boolean canSummon = owed >= 50 && DictatorManager.canSummon();
            long cooldown = DictatorManager.getSummonCooldownRemaining();

            GuiElementBuilder builder = new GuiElementBuilder(Items.PLAYER_HEAD)
                    .setName(Text.literal(debtorName).formatted(owed >= 50 ? Formatting.RED : Formatting.YELLOW))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Owes: " + owed + " credits").formatted(Formatting.GOLD))
                    .addLoreLine(Text.literal(owed >= 50 ? "RESTRICTED (Adventure Mode)" : "In Good Standing").formatted(owed >= 50 ? Formatting.RED : Formatting.GREEN));

            if (owed >= 50) {
                if (canSummon) {
                    builder.addLoreLine(Text.literal(""))
                            .addLoreLine(Text.literal("Click to SUMMON").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                            .setCallback((index, type, action) -> {
                                DictatorManager.summonPlayer(player, debtorUuid);
                                openDebtorsPage(player);
                            });
                } else {
                    builder.addLoreLine(Text.literal(""))
                            .addLoreLine(Text.literal("Summon on cooldown: " + cooldown + "s").formatted(Formatting.GRAY));
                }
            }

            gui.setSlot(slot, builder.build());
            slot++;
        }

        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("Back").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> {
                    open(player);
                })
                .build());

        gui.open();
    }
}