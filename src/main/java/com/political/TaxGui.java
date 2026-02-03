package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class TaxGui {

    public static void open(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
        gui.setTitle(Text.literal("Tax Payment"));

        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        String uuid = player.getUuidAsString();
        int taxOwed = TaxManager.getTaxOwed(uuid);
        int playerCredits = CreditItem.countCredits(player);

        gui.setSlot(4, new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal("Tax Statement").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Amount Owed: " + taxOwed + " credits").formatted(Formatting.GOLD))
                .addLoreLine(Text.literal("Your Credits: " + playerCredits).formatted(Formatting.AQUA))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("+50% interest daily if unpaid!").formatted(Formatting.RED))
                .build());

        if (taxOwed <= 0) {
            gui.setSlot(13, new GuiElementBuilder(Items.LIME_CONCRETE)
                    .setName(Text.literal("No Taxes Owed!").formatted(Formatting.GREEN, Formatting.BOLD))
                    .addLoreLine(Text.literal("You're all clear!").formatted(Formatting.GRAY))
                    .build());
        } else {
            gui.setSlot(11, new GuiElementBuilder(Items.GOLD_NUGGET)
                    .setName(Text.literal("Pay 1 Credit").formatted(Formatting.YELLOW))
                    .addLoreLine(Text.literal("Click to pay 1 credit").formatted(Formatting.GRAY))
                    .setCallback((index, type, action) -> {
                        if (TaxManager.payTax(player, 1)) {
                            player.sendMessage(Text.literal("Paid 1 credit!").formatted(Formatting.GREEN));
                            open(player);
                        } else {
                            player.sendMessage(Text.literal("Not enough credits!").formatted(Formatting.RED));
                        }
                    })
                    .build());

            gui.setSlot(12, new GuiElementBuilder(Items.GOLD_INGOT)
                    .setName(Text.literal("Pay 5 Credits").formatted(Formatting.YELLOW))
                    .addLoreLine(Text.literal("Click to pay 5 credits").formatted(Formatting.GRAY))
                    .setCallback((index, type, action) -> {
                        if (TaxManager.payTax(player, 5)) {
                            player.sendMessage(Text.literal("Paid 5 credits!").formatted(Formatting.GREEN));
                            open(player);
                        } else {
                            player.sendMessage(Text.literal("Not enough credits!").formatted(Formatting.RED));
                        }
                    })
                    .build());

            gui.setSlot(13, new GuiElementBuilder(Items.GOLD_BLOCK)
                    .setName(Text.literal("Pay 10 Credits").formatted(Formatting.GOLD))
                    .addLoreLine(Text.literal("Click to pay 10 credits").formatted(Formatting.GRAY))
                    .setCallback((index, type, action) -> {
                        if (TaxManager.payTax(player, 10)) {
                            player.sendMessage(Text.literal("Paid 10 credits!").formatted(Formatting.GREEN));
                            open(player);
                        } else {
                            player.sendMessage(Text.literal("Not enough credits!").formatted(Formatting.RED));
                        }
                    })
                    .build());

            gui.setSlot(14, new GuiElementBuilder(Items.DIAMOND)
                    .setName(Text.literal("Pay 25 Credits").formatted(Formatting.AQUA))
                    .addLoreLine(Text.literal("Click to pay 25 credits").formatted(Formatting.GRAY))
                    .setCallback((index, type, action) -> {
                        if (TaxManager.payTax(player, 25)) {
                            player.sendMessage(Text.literal("Paid 25 credits!").formatted(Formatting.GREEN));
                            open(player);
                        } else {
                            player.sendMessage(Text.literal("Not enough credits!").formatted(Formatting.RED));
                        }
                    })
                    .build());

            gui.setSlot(15, new GuiElementBuilder(Items.EMERALD_BLOCK)
                    .setName(Text.literal("Pay All").formatted(Formatting.GREEN, Formatting.BOLD))
                    .addLoreLine(Text.literal("Pay your full tax debt").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("Amount: " + taxOwed + " credits").formatted(Formatting.GOLD))
                    .setCallback((index, type, action) -> {
                        if (TaxManager.payTax(player, taxOwed)) {
                            player.sendMessage(Text.literal("Paid all taxes!").formatted(Formatting.GREEN));
                            open(player);
                        } else {
                            player.sendMessage(Text.literal("Not enough credits!").formatted(Formatting.RED));
                        }
                    })
                    .build());
        }

        gui.open();
    }
}