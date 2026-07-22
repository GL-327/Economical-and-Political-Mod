package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

public class TaxGui {

    public static void open(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal("Tax Payment"));

        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Component.literal(""))
                    .build());
        }

        String uuid = player.getUUID().toString();
        int taxOwed = TaxManager.getTaxOwed(uuid);
        int playerCoins = CoinManager.getCoins(uuid);
        float taxPct = TaxManager.getDailyTaxPercentage() * 100.0f;

        gui.setSlot(4, new GuiElementBuilder(Items.PAPER)
                .setName(Component.literal("Tax Statement").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Amount Owed: " + taxOwed + " coins").withStyle(ChatFormatting.GOLD))
                .addLoreLine(Component.literal("Your Coins: " + playerCoins).withStyle(ChatFormatting.AQUA))
                .addLoreLine(Component.literal("Daily Tax Rate: " + String.format("%.1f", taxPct) + "%").withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("+50% interest daily if unpaid!").withStyle(ChatFormatting.RED))
                .build());

        if (taxOwed <= 0) {
            gui.setSlot(13, new GuiElementBuilder(Items.LIME_CONCRETE)
                    .setName(Component.literal("No Taxes Owed!").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("You're all clear!").withStyle(ChatFormatting.GRAY))
                    .build());
        } else {
            gui.setSlot(11, new GuiElementBuilder(Items.GOLD_NUGGET)
                    .setName(Component.literal("Pay 1 Coin").withStyle(ChatFormatting.YELLOW))
                    .addLoreLine(Component.literal("Click to pay 1 coin").withStyle(ChatFormatting.GRAY))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        if (TaxManager.payTaxWithCoins(player, 1)) {
                            player.sendSystemMessage(Component.literal("Paid 1 coin!").withStyle(ChatFormatting.GREEN));
                            open(player);
                        } else {
                            player.sendSystemMessage(Component.literal("Not enough coins!").withStyle(ChatFormatting.RED));
                        }
                    })
                    .build());

            gui.setSlot(12, new GuiElementBuilder(Items.GOLD_INGOT)
                    .setName(Component.literal("Pay 5 Coins").withStyle(ChatFormatting.YELLOW))
                    .addLoreLine(Component.literal("Click to pay 5 coins").withStyle(ChatFormatting.GRAY))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        if (TaxManager.payTaxWithCoins(player, 5)) {
                            player.sendSystemMessage(Component.literal("Paid 5 coins!").withStyle(ChatFormatting.GREEN));
                            open(player);
                        } else {
                            player.sendSystemMessage(Component.literal("Not enough coins!").withStyle(ChatFormatting.RED));
                        }
                    })
                    .build());

            gui.setSlot(13, new GuiElementBuilder(Items.GOLD_BLOCK)
                    .setName(Component.literal("Pay 10 Coins").withStyle(ChatFormatting.GOLD))
                    .addLoreLine(Component.literal("Click to pay 10 coins").withStyle(ChatFormatting.GRAY))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        if (TaxManager.payTaxWithCoins(player, 10)) {
                            player.sendSystemMessage(Component.literal("Paid 10 coins!").withStyle(ChatFormatting.GREEN));
                            open(player);
                        } else {
                            player.sendSystemMessage(Component.literal("Not enough coins!").withStyle(ChatFormatting.RED));
                        }
                    })
                    .build());

            gui.setSlot(14, new GuiElementBuilder(Items.DIAMOND)
                    .setName(Component.literal("Pay 25 Coins").withStyle(ChatFormatting.AQUA))
                    .addLoreLine(Component.literal("Click to pay 25 coins").withStyle(ChatFormatting.GRAY))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        if (TaxManager.payTaxWithCoins(player, 25)) {
                            player.sendSystemMessage(Component.literal("Paid 25 coins!").withStyle(ChatFormatting.GREEN));
                            open(player);
                        } else {
                            player.sendSystemMessage(Component.literal("Not enough coins!").withStyle(ChatFormatting.RED));
                        }
                    })
                    .build());

            gui.setSlot(15, new GuiElementBuilder(Items.EMERALD_BLOCK)
                    .setName(Component.literal("Pay All").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("Pay your full tax debt").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("Amount: " + taxOwed + " coins").withStyle(ChatFormatting.GOLD))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        if (TaxManager.payTaxWithCoins(player, taxOwed)) {
                            player.sendSystemMessage(Component.literal("Paid all taxes!").withStyle(ChatFormatting.GREEN));
                            open(player);
                        } else {
                            player.sendSystemMessage(Component.literal("Not enough coins!").withStyle(ChatFormatting.RED));
                        }
                    })
                    .build());
        }

        gui.open();
    }
}