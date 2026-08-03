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
import net.minecraft.core.component.DataComponents;

import java.util.List;

/**
 * Special Shop - For compacted and enchanted items only
 * Separated from regular /shop to keep it clean
 */
public class SpecialShopGui {

    // ════════════════════════════════════════════════════════════
    // MAIN MENU - Special Items Categories
    // ════════════════════════════════════════════════════════════

    public static void openMainMenu(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("✨ Special Item Shop"));

        // Fill background with decorative pattern
        for (int i = 0; i < 54; i++) {
            if (i % 9 == 0 || i % 9 == 8 || i < 9 || i >= 45) {
                gui.setSlot(i, new GuiElementBuilder(Items.PURPLE_STAINED_GLASS_PANE)
                        .setName(Component.literal("⬛").withStyle(ChatFormatting.DARK_PURPLE))
                        .build());
            } else {
                gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                        .setName(Component.literal(""))
                        .build());
            }
        }

        // Header with balance - center position
        int coins = CoinManager.getCoins(player);
        gui.setSlot(4, new GuiElementBuilder(Items.ENDER_CHEST)
                .setName(Component.literal("✨ Special Item Shop").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Coins: " + ShopManager.formatCoins(coins)).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("• Compacted Items").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("• Super Compacted").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("• Enchanted Items").withStyle(ChatFormatting.GRAY))
                .glow()
                .build());

        // Special categories only
        String[] specialCategories = {
            "Compacted Items",
            "Super Compacted",
            "Enchanted Items"
        };
        
        Item[] categoryIcons = {
            Items.IRON_BLOCK,      // Compacted Items
            Items.GOLD_BLOCK,      // Super Compacted
            Items.ENCHANTED_BOOK   // Enchanted Items
        };

        int[] slots = {20, 22, 24};

        for (int i = 0; i < specialCategories.length; i++) {
            String category = specialCategories[i];
            Item icon = categoryIcons[i];

            final String cat = category;
            gui.setSlot(slots[i], new GuiElementBuilder(icon)
                    .setName(Component.literal("📦 " + category).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to browse items").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("Right-click for quick buy").withStyle(ChatFormatting.DARK_GRAY))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        openCategoryPage(player, cat, 0, false);
                    })
                    .build());
        }

        // Bottom row action buttons
        gui.setSlot(47, new GuiElementBuilder(Items.HOPPER)
                .setName(Component.literal("⚡ Quick Sell").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Sell items from inventory").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Fast & convenient!").withStyle(ChatFormatting.DARK_GRAY))
                .setCallback((index, type, containerInput, slotGui) -> {
                    openSellInventory(player);
                })
                .build());

        gui.setSlot(49, new GuiElementBuilder(Items.CHEST)
                .setName(Component.literal("💸 Sell All").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Sell ALL sellable items").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("in your inventory!").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("⚠ Shift+Click to confirm").withStyle(ChatFormatting.YELLOW))
                .setCallback((index, type, containerInput, slotGui) -> {
                    if (type.shift) {
                        sellAllItems(player);
                    } else {
                        player.sendSystemMessage(Component.literal("Shift+Click to sell all items!").withStyle(ChatFormatting.YELLOW));
                    }
                })
                .build());

        gui.setSlot(51, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("❌ Close").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Close the shop").withStyle(ChatFormatting.GRAY))
                .setCallback((index, type, containerInput, slotGui) -> {
                    gui.close();
                })
                .build());

        // Credit Exchange button
        int playerCredits = DataManager.getCredits(player.getUUID().toString());
        gui.setSlot(53, new GuiElementBuilder(Items.AMETHYST_SHARD)
                .setName(Component.literal("💎 Credit Exchange").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("1 Credit = 10,000 Coins").withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Your Credits: " + playerCredits).withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(playerCredits > 0
                        ? Component.literal("Click to exchange!").withStyle(ChatFormatting.GREEN)
                        : Component.literal("No credits available").withStyle(ChatFormatting.RED))
                .setCallback((index, type, containerInput, slotGui) -> {
                    int credits = DataManager.getCredits(player.getUUID().toString());
                    if (credits >= 1) {
                        DataManager.removeCredits(player.getUUID().toString(), 1);
                        CoinManager.giveCoins(player, 10000);
                        player.sendSystemMessage(Component.literal("✓ Exchanged 1 Credit → 10,000 Coins!").withStyle(ChatFormatting.GREEN), false);
                        openMainMenu(player);
                    } else {
                        player.sendSystemMessage(Component.literal("✗ You have no credits to exchange!").withStyle(ChatFormatting.RED), false);
                    }
                })
                .build());

        gui.open();
    }

    // ════════════════════════════════════════════════════════════
    // CATEGORY PAGE - Browse Items in Category
    // ════════════════════════════════════════════════════════════

    public static void openCategoryPage(ServerPlayer player, String category, int page, boolean sellMode) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal((sellMode ? "Sell: " : "Buy: ") + category + " - Page " + (page + 1)));

        // Fill background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.PURPLE_STAINED_GLASS_PANE)
                    .setName(Component.literal(""))
                    .build());
        }

        // Header with balance
        int coins = CoinManager.getCoins(player);
        gui.setSlot(4, new GuiElementBuilder(Items.SUNFLOWER)
                .setName(Component.literal("Coins: " + ShopManager.formatCoins(coins)).withStyle(ChatFormatting.GOLD))
                .build());

        List<Item> items = ShopManager.getItemsInCategory(category);
        int itemsPerPage = 36; // 4 rows of 9
        int totalPages = (int) Math.ceil((double) items.size() / itemsPerPage);
        int startIndex = page * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, items.size());

        // Display items
        int slot = 9; // Start at second row
        for (int i = startIndex; i < endIndex; i++) {
            Item item = items.get(i);
            int sellPrice = ShopManager.getSellPrice(item);
            int buyPrice = ShopManager.getBuyPrice(item);

            if (sellPrice <= 0) continue; // Skip items without prices

            int playerHas = ShopManager.countItems(player, item);
            
            // Always use vanilla items
            String itemName = ShopManager.getItemName(item);
            GuiElementBuilder builder = new GuiElementBuilder(item)
                    .setName(Component.literal(itemName).withStyle(ChatFormatting.WHITE));

            if (sellMode) {
                // SELL MODE
                boolean canSell = playerHas > 0;
                builder.addLoreLine(Component.literal(""))
                        .addLoreLine(Component.literal("Sell Price: " + sellPrice + " coins").withStyle(ChatFormatting.GREEN))
                        .addLoreLine(Component.literal("You have: " + playerHas).withStyle(ChatFormatting.GRAY))
                        .addLoreLine(Component.literal(""));

                if (canSell) {
                    builder.addLoreLine(Component.literal("Left-click: Sell 1").withStyle(ChatFormatting.YELLOW))
                            .addLoreLine(Component.literal("Right-click: Sell 10").withStyle(ChatFormatting.YELLOW))
                            .addLoreLine(Component.literal("Shift+Click: Sell All").withStyle(ChatFormatting.YELLOW));

                    final Item finalItem = item;
                    builder.setCallback((index, type, containerInput, slotGui) -> {
                        int amount;
                        if (type.shift) {
                            amount = ShopManager.countItems(player, finalItem);
                        } else if (type.isRight) {
                            amount = Math.min(10, ShopManager.countItems(player, finalItem));
                        } else {
                            amount = 1;
                        }
                        if (amount > 0) {
                            ShopManager.sellItem(player, finalItem, amount);
                            openCategoryPage(player, category, page, true);
                        }
                    });
                } else {
                    builder.addLoreLine(Component.literal("You don't have any!").withStyle(ChatFormatting.RED));
                }
            } else {
                // BUY MODE
                boolean canAfford = coins >= buyPrice;
                builder.addLoreLine(Component.literal(""))
                        .addLoreLine(Component.literal("Buy Price: " + buyPrice + " coins").withStyle(ChatFormatting.RED))
                        .addLoreLine(Component.literal("Sell Price: " + sellPrice + " coins").withStyle(ChatFormatting.GREEN))
                        .addLoreLine(Component.literal("You have: " + playerHas).withStyle(ChatFormatting.GRAY))
                        .addLoreLine(Component.literal(""));

                if (canAfford) {
                    builder.addLoreLine(Component.literal("Left-click: Buy 1").withStyle(ChatFormatting.YELLOW))
                            .addLoreLine(Component.literal("Right-click: Buy 10").withStyle(ChatFormatting.YELLOW))
                            .addLoreLine(Component.literal("Shift+Click: Buy 64").withStyle(ChatFormatting.YELLOW));

                    final Item finalItem = item;
                    builder.setCallback((index, type, containerInput, slotGui) -> {
                        int amount;
                        if (type.shift) {
                            amount = 64;
                        } else if (type.isRight) {
                            amount = 10;
                        } else {
                            amount = 1;
                        }
                        ShopManager.buyItem(player, finalItem, amount);
                        openCategoryPage(player, category, page, false);
                    });
                } else {
                    builder.addLoreLine(Component.literal("Not enough coins!").withStyle(ChatFormatting.RED));
                }
            }

            gui.setSlot(slot, builder.build());
            slot++;
            if (slot == 45) break; // Don't overflow into bottom row
        }

        // Navigation - Previous Page
        if (page > 0) {
            gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("← Previous Page").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        openCategoryPage(player, category, page - 1, sellMode);
                    })
                    .build());
        }

        // Back to main menu
        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("Back to Categories").withStyle(ChatFormatting.RED))
                .setCallback((index, type, containerInput, slotGui) -> {
                    openMainMenu(player);
                })
                .build());

        // Toggle Buy/Sell mode
        gui.setSlot(48, new GuiElementBuilder(sellMode ? Items.GOLD_INGOT : Items.EMERALD)
                .setName(Component.literal(sellMode ? "Switch to BUY" : "Switch to SELL").withStyle(sellMode ? ChatFormatting.GREEN : ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Currently: " + (sellMode ? "SELLING" : "BUYING")).withStyle(ChatFormatting.GRAY))
                .setCallback((index, type, containerInput, slotGui) -> {
                    openCategoryPage(player, category, page, !sellMode);
                })
                .build());

        // Navigation - Next Page
        if (page < totalPages - 1) {
            gui.setSlot(53, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("Next Page →").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        openCategoryPage(player, category, page + 1, sellMode);
                    })
                    .build());
        }

        gui.open();
    }

    // ════════════════════════════════════════════════════════════
    // SELL INVENTORY - Shows all sellable items from player inventory
    // ════════════════════════════════════════════════════════════

    public static void openSellInventory(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Quick Sell - Special Items"));

        // Fill background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.MAGENTA_STAINED_GLASS_PANE)
                    .setName(Component.literal(""))
                    .build());
        }

        // Header
        int coins = CoinManager.getCoins(player);
        gui.setSlot(4, new GuiElementBuilder(Items.SUNFLOWER)
                .setName(Component.literal("Coins: " + ShopManager.formatCoins(coins)).withStyle(ChatFormatting.GOLD))
                .build());

        // Scan player inventory for sellable items
        int slot = 9;
        java.util.Map<Item, Integer> sellableItems = new java.util.LinkedHashMap<>();

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && ShopManager.canBeSold(stack.getItem())) {
                sellableItems.merge(stack.getItem(), stack.getCount(), (oldVal, newVal) -> oldVal + newVal);
            }
        }

        for (java.util.Map.Entry<Item, Integer> entry : sellableItems.entrySet()) {
            if (slot >= 45) break;

            Item item = entry.getKey();
            int count = entry.getValue();
            int sellPrice = ShopManager.getSellPrice(item);
            int totalValue = sellPrice * count;
            String itemName = ShopManager.getItemName(item);

            final Item finalItem = item;
            gui.setSlot(slot, new GuiElementBuilder(item)
                    .setCount(Math.min(count, 64))
                    .setName(Component.literal(itemName + " x" + count).withStyle(ChatFormatting.WHITE))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Each: " + sellPrice + " coins").withStyle(ChatFormatting.GREEN))
                    .addLoreLine(Component.literal("Total: " + totalValue + " coins").withStyle(ChatFormatting.GOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Left-click: Sell 1").withStyle(ChatFormatting.YELLOW))
                    .addLoreLine(Component.literal("Right-click: Sell 10").withStyle(ChatFormatting.YELLOW))
                    .addLoreLine(Component.literal("Shift+Click: Sell All").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        int amount;
                        if (type.shift) {
                            amount = ShopManager.countItems(player, finalItem);
                        } else if (type.isRight) {
                            amount = Math.min(10, ShopManager.countItems(player, finalItem));
                        } else {
                            amount = 1;
                        }
                        if (amount > 0) {
                            ShopManager.sellItem(player, finalItem, amount);
                            openSellInventory(player); // Refresh
                        }
                    })
                    .build());
            slot++;
        }

        if (sellableItems.isEmpty()) {
            gui.setSlot(22, new GuiElementBuilder(Items.BARRIER)
                    .setName(Component.literal("No Sellable Special Items").withStyle(ChatFormatting.RED))
                    .addLoreLine(Component.literal("Your inventory has no special").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("items that can be sold.").withStyle(ChatFormatting.GRAY))
                    .build());
        }

        // Back button
        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((index, type, containerInput, slotGui) -> {
                    openMainMenu(player);
                })
                .build());

        // Sell all button
        if (!sellableItems.isEmpty()) {
            int totalValue = sellableItems.entrySet().stream()
                    .mapToInt(e -> ShopManager.getSellPrice(e.getKey()) * e.getValue())
                    .sum();

            gui.setSlot(50, new GuiElementBuilder(Items.CHEST)
                    .setName(Component.literal("Sell All").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Total Value: " + totalValue + " coins").withStyle(ChatFormatting.GOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Shift+Click to confirm!").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        if (type.shift) {
                            sellAllItems(player);
                            openSellInventory(player);
                        }
                    })
                    .build());
        }

        gui.open();
    }

    // ════════════════════════════════════════════════════════════
    // SELL ALL ITEMS
    // ════════════════════════════════════════════════════════════

    private static void sellAllItems(ServerPlayer player) {
        int totalCoins = 0;
        int totalItems = 0;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && ShopManager.canBeSold(stack.getItem())) {
                int sellPrice = ShopManager.getSellPrice(stack.getItem());
                int count = stack.getCount();
                totalCoins += sellPrice * count;
                totalItems += count;
                player.getInventory().setItem(i, ItemStack.EMPTY);
            }
        }

        if (totalCoins > 0) {
            CoinManager.giveCoinsQuiet(player, totalCoins);
            player.sendSystemMessage(Component.literal("Sold " + totalItems + " items for ")
                    .withStyle(ChatFormatting.GREEN)
                    .append(Component.literal(ShopManager.formatCoins(totalCoins)).withStyle(ChatFormatting.GOLD))
                    .append(Component.literal("!").withStyle(ChatFormatting.GREEN)));
            DataManager.save(PoliticalServer.server);
        } else {
            player.sendSystemMessage(Component.literal("No sellable items in your inventory!").withStyle(ChatFormatting.RED));
        }
    }
}
