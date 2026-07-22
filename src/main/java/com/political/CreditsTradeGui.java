package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
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

    public static void open(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Credit Shop"));

        // Fill background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.PURPLE_STAINED_GLASS_PANE)
                    .setName(Component.literal(""))
                    .build());
        }

        int playerCredits = DataManager.getCredits(player.getUUID().toString());

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.AMETHYST_SHARD)
                .setName(Component.literal("Your Credits: " + playerCredits).withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .glow()
                .build());
// === CREDIT TO COIN CONVERSION (1 credit = 10,000 coins) ===
        gui.setSlot(1, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Component.literal("💎 Convert Credits → Coins")
                        .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("1 Credit = 10,000 Coins").withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Your Credits: " + DataManager.getCredits(player.getUUID().toString())).withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Your Coins: " + CoinManager.getCoins(player)).withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to convert!").withStyle(ChatFormatting.GREEN))
                .setCallback((idx, type, containerInput, slotGui) -> {
                    int credits = DataManager.getCredits(player.getUUID().toString());
                    if (credits >= 1) {
                        DataManager.removeCredits(player.getUUID().toString(), 1);
                        CoinManager.giveCoins(player, 10000);
                        player.sendSystemMessage(Component.literal("✓ Converted 1 Credit → 10,000 Coins!")
                                .withStyle(ChatFormatting.GREEN), false);
                        open(player); // Refresh GUI
                    } else {
                        player.sendSystemMessage(Component.literal("✗ Need at least 1 credit!")
                                .withStyle(ChatFormatting.RED), false);
                    }
                })
                .build());

        // === SELL SECTION (Left side) ===
        gui.setSlot(19, new GuiElementBuilder(Items.TOTEM_OF_UNDYING)
                .setName(Component.literal("Sell Totems").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("9 Totems = 1 Credit").withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to sell!").withStyle(ChatFormatting.GREEN))
                .setCallback((idx, type, containerInput, slotGui) -> {
                    int totemCount = countItem(player, Items.TOTEM_OF_UNDYING);
                    if (totemCount >= 9) {
                        removeItem(player, Items.TOTEM_OF_UNDYING, 9);
                        DataManager.addCredits(player.getUUID().toString(), 1);
                        player.sendSystemMessage(Component.literal("Sold 9 Totems for 1 Credit!").withStyle(ChatFormatting.GREEN));
                        open(player); // Refresh
                    } else {
                        player.sendSystemMessage(Component.literal("Need 9 Totems! You have: " + totemCount).withStyle(ChatFormatting.RED));
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
                    .setName(Component.literal(trimName).withStyle(canAfford ? ChatFormatting.GREEN : ChatFormatting.RED))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Cost: 10 Credits").withStyle(ChatFormatting.LIGHT_PURPLE))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(canAfford ? Component.literal("Click to buy!").withStyle(ChatFormatting.GREEN)
                            : Component.literal("Not enough credits!").withStyle(ChatFormatting.RED))
                    .setCallback((idx, clickType, act, __slotGui) -> {
                        if (DataManager.getCredits(player.getUUID().toString()) >= 10) {
                            DataManager.removeCredits(player.getUUID().toString(), 10);
                            player.addItem(new ItemStack(trim));
                            player.sendSystemMessage(Component.literal("Purchased " + trimName + "!").withStyle(ChatFormatting.GREEN));
                            open(player);
                        } else {
                            player.sendSystemMessage(Component.literal("Not enough credits!").withStyle(ChatFormatting.RED));
                        }
                    })
                    .build());
            slotIndex++;
        }

        // Close button
        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("Close").withStyle(ChatFormatting.RED))
                .setCallback((idx, type, containerInput, slotGui) -> gui.close())
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

    private static int countItem(ServerPlayer player, Item item) {
        int count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(item)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static void removeItem(ServerPlayer player, Item item, int amount) {
        int remaining = amount;
        for (int i = 0; i < player.getInventory().getContainerSize() && remaining > 0; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(item)) {
                int toRemove = Math.min(remaining, stack.getCount());
                stack.shrink(toRemove);
                remaining -= toRemove;
            }
        }
    }
}