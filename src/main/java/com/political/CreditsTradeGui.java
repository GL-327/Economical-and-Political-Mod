package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class CreditsTradeGui {

    private static final int CREDITS_TO_COINS = 10000;
    private static final int COINS_TO_CREDITS = 10000;

    public static class TradeOption {
        public final Item item;
        public final String name;
        public final int amount;
        public final int creditValue;

        public TradeOption(Item item, String name, int amount, int creditValue) {
            this.item = item;
            this.name = name;
            this.amount = amount;
            this.creditValue = creditValue;
        }
    }

    public static final List<TradeOption> TRADE_OPTIONS = new ArrayList<>();

    static {
        TRADE_OPTIONS.add(new TradeOption(Items.TOTEM_OF_UNDYING, "Totem of Undying", 1, 15));
        TRADE_OPTIONS.add(new TradeOption(Items.HEAVY_CORE, "Heavy Core", 1, 125));
        TRADE_OPTIONS.add(new TradeOption(Items.TRIDENT, "Trident", 1, 15));
    }

    public static void open(ServerPlayerEntity player) {
        openSellPage(player);
    }

    public static void openSellPage(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("Credits Trade - Sell Items"));

        fillBackground(gui);

        int playerCredits = CreditItem.countCredits(player);
        gui.setSlot(4, new GuiElementBuilder(Items.GOLD_NUGGET)
                .setName(Text.literal("Your Credits: " + playerCredits).formatted(Formatting.GOLD, Formatting.BOLD))
                .glow()
                .build());

        // CREDITS → COINS button
        gui.setSlot(50, new GuiElementBuilder(Items.GOLD_NUGGET)
                .setName(Text.literal("Convert Credits → Coins").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("1 credit = " + CREDITS_TO_COINS + " coins").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Your Credits: " + playerCredits).formatted(Formatting.GREEN))
                .addLoreLine(Text.literal(""))
                .addLoreLine(playerCredits >= 1
                        ? Text.literal("Click to convert!").formatted(Formatting.GREEN)
                        : Text.literal("Need 1+ credits").formatted(Formatting.RED))
                .setCallback((index, type, action) -> {
                    if (CreditItem.countCredits(player) >= 1) {
                        CreditItem.removeCredits(player, 1);
                        CoinManager.giveCoins(player, CREDITS_TO_COINS);
                        player.sendMessage(Text.literal("Converted 1 credit → " + CREDITS_TO_COINS + " coins!").formatted(Formatting.GREEN));
                        openSellPage(player);
                    } else {
                        player.sendMessage(Text.literal("Need at least 1 credit!").formatted(Formatting.RED));
                    }
                })
                .build());

        int slot = 19;
        for (TradeOption option : TRADE_OPTIONS) {
            if (slot == 26) slot = 28;
            if (slot >= 35) break;

            int playerHas = countItem(player, option.item);

            GuiElementBuilder builder = new GuiElementBuilder(option.item)
                    .setCount(option.amount)
                    .setName(Text.literal(option.name).formatted(Formatting.WHITE))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Sell " + option.amount + " for: " + option.creditValue + " credits").formatted(Formatting.GREEN))
                    .addLoreLine(Text.literal("You have: " + playerHas).formatted(Formatting.GRAY));

            if (playerHas >= option.amount) {
                builder.addLoreLine(Text.literal(""))
                        .addLoreLine(Text.literal("Click to sell!").formatted(Formatting.YELLOW))
                        .setCallback((index, type, action) -> {
                            if (removeItem(player, option.item, option.amount)) {
                                CreditItem.giveCredits(player, option.creditValue);
                                player.sendMessage(Text.literal("Sold " + option.name + " for " + option.creditValue + " credits!").formatted(Formatting.GREEN));
                                openSellPage(player);
                            }
                        });
            } else {
                builder.addLoreLine(Text.literal(""))
                        .addLoreLine(Text.literal("Not enough items!").formatted(Formatting.RED));
            }

            gui.setSlot(slot, builder.build());
            slot++;
        }

        gui.setSlot(49, new GuiElementBuilder(Items.EMERALD)
                .setName(Text.literal("Switch to Buy Mode").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal("Buy items with credits").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("(2x the sell price)").formatted(Formatting.RED))
                .setCallback((index, type, action) -> {
                    openBuyPage(player);
                })
                .build());

        gui.open();
    }

    public static void openBuyPage(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("Credits Trade - Buy Items"));

        fillBackground(gui);

        int playerCredits = CreditItem.countCredits(player);
        gui.setSlot(4, new GuiElementBuilder(Items.GOLD_NUGGET)
                .setName(Text.literal("Your Credits: " + playerCredits).formatted(Formatting.GOLD, Formatting.BOLD))
                .glow()
                .build());

        // COINS → CREDITS button
        int playerCoins = CoinManager.getCoins(player);
        gui.setSlot(50, new GuiElementBuilder(Items.DIAMOND)
                .setName(Text.literal("Convert Coins → Credits").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal(COINS_TO_CREDITS + " coins = 1 credit").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Your Coins: " + playerCoins).formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal(""))
                .addLoreLine(playerCoins >= COINS_TO_CREDITS
                        ? Text.literal("Click to convert!").formatted(Formatting.GREEN)
                        : Text.literal("Need " + COINS_TO_CREDITS + "+ coins").formatted(Formatting.RED))
                .setCallback((index, type, action) -> {
                    if (CoinManager.getCoins(player) >= COINS_TO_CREDITS) {
                        CoinManager.removeCoins(player, COINS_TO_CREDITS);
                        CreditItem.giveCredits(player, 1);
                        player.sendMessage(Text.literal("Converted " + COINS_TO_CREDITS + " coins → 1 credit!").formatted(Formatting.GREEN));
                        openBuyPage(player);
                    } else {
                        player.sendMessage(Text.literal("Need at least " + COINS_TO_CREDITS + " coins!").formatted(Formatting.RED));
                    }
                })
                .build());

        int slot = 19;
        for (TradeOption option : TRADE_OPTIONS) {
            if (slot == 26) slot = 28;
            if (slot >= 35) break;

            int buyCost = option.creditValue * 2;

            GuiElementBuilder builder = new GuiElementBuilder(option.item)
                    .setCount(option.amount)
                    .setName(Text.literal(option.name).formatted(Formatting.WHITE))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Buy " + option.amount + " for: " + buyCost + " credits").formatted(Formatting.RED))
                    .addLoreLine(Text.literal("You have: " + playerCredits + " credits").formatted(Formatting.GRAY));

            if (playerCredits >= buyCost) {
                builder.addLoreLine(Text.literal(""))
                        .addLoreLine(Text.literal("Click to buy!").formatted(Formatting.YELLOW))
                        .setCallback((index, type, action) -> {
                            if (CreditItem.removeCredits(player, buyCost)) {
                                giveItem(player, option.item, option.amount);
                                player.sendMessage(Text.literal("Bought " + option.name + " for " + buyCost + " credits!").formatted(Formatting.GREEN));
                                openBuyPage(player);
                            }
                        });
            } else {
                builder.addLoreLine(Text.literal(""))
                        .addLoreLine(Text.literal("Not enough credits!").formatted(Formatting.RED));
            }

            gui.setSlot(slot, builder.build());
            slot++;
        }

        gui.setSlot(49, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Text.literal("Switch to Sell Mode").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal("Sell items for credits").formatted(Formatting.GRAY))
                .setCallback((index, type, action) -> {
                    openSellPage(player);
                })
                .build());

        gui.open();
    }

    private static void fillBackground(SimpleGui gui) {
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }
    }

    private static int countItem(ServerPlayerEntity player, Item item) {
        int count = 0;
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.isOf(item)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static boolean removeItem(ServerPlayerEntity player, Item item, int amount) {
        int remaining = amount;
        for (int i = 0; i < player.getInventory().size() && remaining > 0; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.isOf(item)) {
                int toRemove = Math.min(remaining, stack.getCount());
                stack.decrement(toRemove);
                remaining -= toRemove;
            }
        }
        return remaining == 0;
    }

    private static void giveItem(ServerPlayerEntity player, Item item, int amount) {
        ItemStack stack = new ItemStack(item, amount);
        if (!player.getInventory().insertStack(stack)) {
            player.dropItem(stack, false);
        }
    }
}