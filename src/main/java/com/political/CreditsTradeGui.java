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

    // Armor trims buyable with credits
    public static final List<Item> ARMOR_TRIMS = List.of(
            Items.COAST_ARMOR_TRIM_SMITHING_TEMPLATE,
            Items.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE,
            Items.EYE_ARMOR_TRIM_SMITHING_TEMPLATE,
            Items.HOST_ARMOR_TRIM_SMITHING_TEMPLATE,
            Items.RAISER_ARMOR_TRIM_SMITHING_TEMPLATE,
            Items.RIB_ARMOR_TRIM_SMITHING_TEMPLATE,
            Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE,
            Items.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE,
            Items.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE,
            Items.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE,
            Items.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE,
            Items.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE,
            Items.VEX_ARMOR_TRIM_SMITHING_TEMPLATE,
            Items.WARD_ARMOR_TRIM_SMITHING_TEMPLATE,
            Items.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE,
            Items.WILD_ARMOR_TRIM_SMITHING_TEMPLATE
    );

    public static void open(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("Credit Shop"));

        // Fill background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.PURPLE_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        int playerCredits = DataManager.getCredits(player.getUuidAsString());

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.AMETHYST_SHARD)
                .setName(Text.literal("Your Credits: " + playerCredits).formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .glow()
                .build());
// === COIN TO CREDIT CONVERSION (10,000 coins = 1 credit) ===
// === CREDIT TO COIN CONVERSION (1 credit = 10,000 coins) ===
        gui.setSlot(1, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Text.literal("💎 Convert Credits → Coins")
                        .formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("1 Credit = 10,000 Coins").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Your Credits: " + DataManager.getCredits(player.getUuidAsString())).formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Your Coins: " + CoinManager.getCoins(player)).formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to convert!").formatted(Formatting.GREEN))
                .setCallback((idx, type, action) -> {
                    int credits = DataManager.getCredits(player.getUuidAsString());
                    if (credits >= 1) {
                        DataManager.removeCredits(player.getUuidAsString(), 1);
                        CoinManager.giveCoins(player, 10000);
                        player.sendMessage(Text.literal("✓ Converted 1 Credit → 10,000 Coins!")
                                .formatted(Formatting.GREEN), false);
                        open(player); // Refresh GUI
                    } else {
                        player.sendMessage(Text.literal("✗ Need at least 1 credit!")
                                .formatted(Formatting.RED), false);
                    }
                })
                .build());
        gui.setSlot(7, new GuiElementBuilder(Items.DIAMOND)
                        .setName(Text.literal("🪙 Convert Coins → Credits")
                                .formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                        .addLoreLine(Text.literal(""))
                        .addLoreLine(Text.literal("10,000 Coins = 1 Credit").formatted(Formatting.YELLOW))
                        .addLoreLine(Text.literal(""))
                        .addLoreLine(Text.literal("Your Credits: " + DataManager.getCredits(player.getUuidAsString())).formatted(Formatting.GRAY))
                        .addLoreLine(Text.literal("Your Coins: " + CoinManager.getCoins(player)).formatted(Formatting.GRAY))
                        .addLoreLine(Text.literal(""))
                        .addLoreLine(Text.literal("Click to convert!").formatted(Formatting.GREEN))
                        .setCallback((idx, type, action) -> {
                            int coins = DataManager.getCoins(player.getUuidAsString());
                            if (coins >= 10000) {
                                DataManager.removeCoins(player.getUuidAsString(), 10000);
                                CreditItem.giveCredits(player, 1);
                                player.sendMessage(Text.literal("✓ Converted 10,000 Coins → 1 Credit!")
                                        .formatted(Formatting.GREEN), false);
                                open(player); // Refresh GUI
                            } else {
                                player.sendMessage(Text.literal("✗ Need at least 10,000 Coins!")
                                        .formatted(Formatting.RED), false);
                            }
                        })
                                .build());

        // === SELL SECTION (Left side) ===
        gui.setSlot(19, new GuiElementBuilder(Items.TOTEM_OF_UNDYING)
                .setName(Text.literal("Sell Totems").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("9 Totems = 1 Credit").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to sell!").formatted(Formatting.GREEN))
                .setCallback((idx, type, action) -> {
                    int totemCount = countItem(player, Items.TOTEM_OF_UNDYING);
                    if (totemCount >= 9) {
                        removeItem(player, Items.TOTEM_OF_UNDYING, 9);
                        DataManager.addCredits(player.getUuidAsString(), 1);
                        player.sendMessage(Text.literal("Sold 9 Totems for 1 Credit!").formatted(Formatting.GREEN));
                        open(player); // Refresh
                    } else {
                        player.sendMessage(Text.literal("Need 9 Totems! You have: " + totemCount).formatted(Formatting.RED));
                    }
                })
                .build());

        // === BUY SECTION (Right side - Armor Trims) ===
        int[] buySlots = {23, 24, 25, 32, 33, 34, 41, 42, 43};
        int slotIndex = 0;

        for (int i = 0; i < Math.min(ARMOR_TRIMS.size(), buySlots.length); i++) {
            Item trim = ARMOR_TRIMS.get(i);
            String trimName = formatTrimName(trim);
            boolean canAfford = playerCredits >= 10;

            gui.setSlot(buySlots[slotIndex], new GuiElementBuilder(trim)
                    .setName(Text.literal(trimName).formatted(canAfford ? Formatting.GREEN : Formatting.RED))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Cost: 10 Credits").formatted(Formatting.LIGHT_PURPLE))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(canAfford ? Text.literal("Click to buy!").formatted(Formatting.GREEN)
                            : Text.literal("Not enough credits!").formatted(Formatting.RED))
                    .setCallback((idx, clickType, act) -> {
                        if (DataManager.getCredits(player.getUuidAsString()) >= 10) {
                            DataManager.removeCredits(player.getUuidAsString(), 10);
                            player.getInventory().insertStack(new ItemStack(trim));
                            player.sendMessage(Text.literal("Purchased " + trimName + "!").formatted(Formatting.GREEN));
                            open(player);
                        } else {
                            player.sendMessage(Text.literal("Not enough credits!").formatted(Formatting.RED));
                        }
                    })
                    .build());
            slotIndex++;
        }

        // Close button
        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("Close").formatted(Formatting.RED))
                .setCallback((idx, type, action) -> gui.close())
                .build());

        gui.open();
    }

    private static String formatTrimName(Item item) {
        String name = item.toString();
        // Convert "coast_armor_trim_smithing_template" to "Coast Trim"
        name = name.replace("_armor_trim_smithing_template", "")
                .replace("_", " ");
        return Character.toUpperCase(name.charAt(0)) + name.substring(1) + " Trim";
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

    private static void removeItem(ServerPlayerEntity player, Item item, int amount) {
        int remaining = amount;
        for (int i = 0; i < player.getInventory().size() && remaining > 0; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.isOf(item)) {
                int toRemove = Math.min(remaining, stack.getCount());
                stack.decrement(toRemove);
                remaining -= toRemove;
            }
        }
    }
}