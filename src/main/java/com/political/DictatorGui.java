package com.political;


import com.political.government.DictatorManager;
import com.political.government.PerkManager;
import com.political.government.PerksGui;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.Map;

public class DictatorGui {

    public static void open(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("⚠ Dictator Control Panel ⚠"));

        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.red())
                    .setName(Component.literal(""))
                    .build());
        }

        gui.setSlot(4, new GuiElementBuilder(Items.DRAGON_HEAD)
                .setName(Component.literal("DICTATOR CONTROL").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("You have absolute power!").withStyle(ChatFormatting.RED))
                .glow()
                .build());
// CHANGED: Check if perks can be changed before opening
        boolean canChangePerks = PerkManager.canChangePerks(true);  // Dictator uses Chair rules
        if (canChangePerks) {
            gui.setSlot(37, new GuiElementBuilder(Items.NETHER_STAR)
                    .setName(Component.literal("Open Perks Menu").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Select server perks").withStyle(ChatFormatting.GRAY))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        PerksGui.openFrom(player, true, () -> open(player));
                    })
                    .build());
        } else {
            gui.setSlot(37, new GuiElementBuilder(Items.BARRIER)
                    .setName(Component.literal("Perks Locked").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Perks have already been").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("selected for this term!").withStyle(ChatFormatting.GRAY))
                    .build());
        }
        boolean taxEnabled = DictatorManager.isDictatorTaxEnabled();
        int taxAmount = DictatorManager.getDictatorTaxAmount();

        gui.setSlot(19, new GuiElementBuilder(Items.GOLD_BLOCK)
                .setName(Component.literal("Tax: 10 Credits/Day").withStyle(ChatFormatting.GOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal(taxEnabled && taxAmount == 10 ? "✓ ACTIVE" : "Click to enable").withStyle(taxEnabled && taxAmount == 10 ? ChatFormatting.GREEN : ChatFormatting.GRAY))
                .glow(taxEnabled && taxAmount == 10)
                .setCallback((index, type, containerInput, slotGui) -> {
                    DictatorManager.setDictatorTax(true, 10);
                    open(player);
                })
                .build());

        gui.setSlot(20, new GuiElementBuilder(Items.DIAMOND_BLOCK)
                .setName(Component.literal("Tax: 20 Credits/Day").withStyle(ChatFormatting.AQUA))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal(taxEnabled && taxAmount == 20 ? "✓ ACTIVE" : "Click to enable").withStyle(taxEnabled && taxAmount == 20 ? ChatFormatting.GREEN : ChatFormatting.GRAY))
                .glow(taxEnabled && taxAmount == 20)
                .setCallback((index, type, containerInput, slotGui) -> {
                    DictatorManager.setDictatorTax(true, 20);
                    open(player);
                })
                .build());

        gui.setSlot(21, new GuiElementBuilder(Items.NETHERITE_BLOCK)
                .setName(Component.literal("Tax: 30 Credits/Day").withStyle(ChatFormatting.DARK_GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal(taxEnabled && taxAmount == 30 ? "✓ ACTIVE" : "Click to enable").withStyle(taxEnabled && taxAmount == 30 ? ChatFormatting.GREEN : ChatFormatting.GRAY))
                .glow(taxEnabled && taxAmount == 30)
                .setCallback((index, type, containerInput, slotGui) -> {
                    DictatorManager.setDictatorTax(true, 30);
                    open(player);
                })
                .build());

        gui.setSlot(22, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("Disable Tax").withStyle(ChatFormatting.RED))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal(taxEnabled ? "Click to disable" : "Tax already disabled").withStyle(ChatFormatting.GRAY))
                .setCallback((index, type, containerInput, slotGui) -> {
                    DictatorManager.setDictatorTax(false, 0);
                    open(player);
                })
                .build());

        gui.setSlot(25, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Component.literal("View Debtors").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("View and summon players in debt").withStyle(ChatFormatting.GRAY))
                .setCallback((index, type, containerInput, slotGui) -> {
                    openDebtorsPage(player);
                })
                .build());

        gui.setSlot(43, new GuiElementBuilder(Items.LIGHTNING_ROD.weathering().unaffected())
                .setName(Component.literal("Smite Players").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Use /smite <player> to strike").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Cooldown: 15 seconds").withStyle(ChatFormatting.RED))
                .build());

        gui.open();
    }

    public static void openDebtorsPage(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Players in Debt - Click to Summon"));

        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.red())
                    .setName(Component.literal(""))
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
                    .setName(Component.literal(debtorName).withStyle(owed >= 50 ? ChatFormatting.RED : ChatFormatting.YELLOW))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Owes: " + owed + " credits").withStyle(ChatFormatting.GOLD))
                    .addLoreLine(Component.literal(owed >= 50 ? "RESTRICTED (Adventure Mode)" : "In Good Standing").withStyle(owed >= 50 ? ChatFormatting.RED : ChatFormatting.GREEN));

            if (owed >= 50) {
                if (canSummon) {
                    builder.addLoreLine(Component.literal(""))
                            .addLoreLine(Component.literal("Click to SUMMON").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                            .setCallback((index, type, containerInput, slotGui) -> {
                                DictatorManager.summonPlayer(player, debtorUuid);
                                openDebtorsPage(player);
                            });
                } else {
                    builder.addLoreLine(Component.literal(""))
                            .addLoreLine(Component.literal("Summon on cooldown: " + cooldown + "s").withStyle(ChatFormatting.GRAY));
                }
            }

            gui.setSlot(slot, builder.build());
            slot++;
        }

        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((index, type, containerInput, slotGui) -> {
                    open(player);
                })
                .build());

        gui.open();
    }
}