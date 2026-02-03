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

import java.util.List;

public class ShopGui {

    // ════════════════════════════════════════════════════════════
    // MAIN MENU - Category Selection
    // ════════════════════════════════════════════════════════════

    public static void openMainMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("🏪 Item Shop"));

        // Fill background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        // Header with balance
        int coins = CoinManager.getCoins(player);
        gui.setSlot(4, new GuiElementBuilder(Items.SUNFLOWER)
                .setName(Text.literal("Your Coins: " + ShopManager.formatCoins(coins)).formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Buy items at 2x sell price").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Sell items for coins!").formatted(Formatting.GRAY))
                .glow()
                .build());

        // Category buttons
        Item[] categoryIcons = {
                Items.STONE_BRICKS,      // Building Blocks
                Items.DIAMOND,           // Ores & Materials
                Items.COOKED_BEEF,       // Food
                Items.BONE,              // Mob Drops
                Items.REDSTONE,          // Redstone
                Items.DIAMOND_PICKAXE,   // Tools
                Items.DIAMOND_CHESTPLATE,// Armor
                Items.LANTERN,           // Decoration
                Items.OAK_SAPLING,       // Plants
                Items.BREWING_STAND,     // Brewing
                Items.RED_DYE,           // Dyes
                Items.ENDER_PEARL        // Miscellaneous
        };

        List<String> categories = ShopManager.getCategoryNames();
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};

        for (int i = 0; i < categories.size() && i < slots.length; i++) {
            String category = categories.get(i);
            Item icon = i < categoryIcons.length ? categoryIcons[i] : Items.CHEST;

            final String cat = category;
            gui.setSlot(slots[i], new GuiElementBuilder(icon)
                    .setName(Text.literal(category).formatted(Formatting.YELLOW, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Click to browse").formatted(Formatting.GRAY))
                    .setCallback((index, type, action) -> {
                        openCategoryPage(player, cat, 0, false);
                    })
                    .build());
        }

        // Sell mode button
        gui.setSlot(48, new GuiElementBuilder(Items.HOPPER)
                .setName(Text.literal("Quick Sell").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Sell items from your inventory").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Click to open sell menu").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> {
                    openSellInventory(player);
                })
                .build());

        // Sell all button
        gui.setSlot(50, new GuiElementBuilder(Items.CHEST)
                .setName(Text.literal("Sell All").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Sell ALL sellable items").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("in your inventory!").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Shift+Click to confirm").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> {
                    if (type.shift) {
                        sellAllItems(player);
                    } else {
                        player.sendMessage(Text.literal("Shift+Click to sell all items!").formatted(Formatting.YELLOW));
                    }
                })
                .build());

        gui.open();
    }

    // ════════════════════════════════════════════════════════════
    // CATEGORY PAGE - Browse Items in Category
    // ════════════════════════════════════════════════════════════

    public static void openCategoryPage(ServerPlayerEntity player, String category, int page, boolean sellMode) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal((sellMode ? "Sell: " : "Buy: ") + category + " - Page " + (page + 1)));

        // Fill background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        // Header with balance
        int coins = CoinManager.getCoins(player);
        gui.setSlot(4, new GuiElementBuilder(Items.SUNFLOWER)
                .setName(Text.literal("Coins: " + ShopManager.formatCoins(coins)).formatted(Formatting.GOLD))
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
            String itemName = ShopManager.getItemName(item);

            GuiElementBuilder builder = new GuiElementBuilder(item)
                    .setName(Text.literal(itemName).formatted(Formatting.WHITE));

            if (sellMode) {
                // SELL MODE
                boolean canSell = playerHas > 0;
                builder.addLoreLine(Text.literal(""))
                        .addLoreLine(Text.literal("Sell Price: " + sellPrice + " coins").formatted(Formatting.GREEN))
                        .addLoreLine(Text.literal("You have: " + playerHas).formatted(Formatting.GRAY))
                        .addLoreLine(Text.literal(""));

                if (canSell) {
                    builder.addLoreLine(Text.literal("Left-click: Sell 1").formatted(Formatting.YELLOW))
                            .addLoreLine(Text.literal("Right-click: Sell 10").formatted(Formatting.YELLOW))
                            .addLoreLine(Text.literal("Shift+Click: Sell All").formatted(Formatting.YELLOW));

                    final Item finalItem = item;
                    builder.setCallback((index, type, action) -> {
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
                    builder.addLoreLine(Text.literal("You don't have any!").formatted(Formatting.RED));
                }
            } else {
                // BUY MODE
                boolean canAfford = coins >= buyPrice;
                builder.addLoreLine(Text.literal(""))
                        .addLoreLine(Text.literal("Buy Price: " + buyPrice + " coins").formatted(Formatting.RED))
                        .addLoreLine(Text.literal("Sell Price: " + sellPrice + " coins").formatted(Formatting.GREEN))
                        .addLoreLine(Text.literal("You have: " + playerHas).formatted(Formatting.GRAY))
                        .addLoreLine(Text.literal(""));

                if (canAfford) {
                    builder.addLoreLine(Text.literal("Left-click: Buy 1").formatted(Formatting.YELLOW))
                            .addLoreLine(Text.literal("Right-click: Buy 10").formatted(Formatting.YELLOW))
                            .addLoreLine(Text.literal("Shift+Click: Buy 64").formatted(Formatting.YELLOW));

                    final Item finalItem = item;
                    builder.setCallback((index, type, action) -> {
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
                    builder.addLoreLine(Text.literal("Not enough coins!").formatted(Formatting.RED));
                }
            }

            gui.setSlot(slot, builder.build());
            slot++;
            if (slot == 45) break; // Don't overflow into bottom row
        }

        // Navigation - Previous Page
        if (page > 0) {
            gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("← Previous Page").formatted(Formatting.YELLOW))
                    .setCallback((index, type, action) -> {
                        openCategoryPage(player, category, page - 1, sellMode);
                    })
                    .build());
        }

        // Back to main menu
        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("Back to Categories").formatted(Formatting.RED))
                .setCallback((index, type, action) -> {
                    openMainMenu(player);
                })
                .build());

        // Toggle Buy/Sell mode
        gui.setSlot(48, new GuiElementBuilder(sellMode ? Items.GOLD_INGOT : Items.EMERALD)
                .setName(Text.literal(sellMode ? "Switch to BUY" : "Switch to SELL").formatted(sellMode ? Formatting.GREEN : Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal("Currently: " + (sellMode ? "SELLING" : "BUYING")).formatted(Formatting.GRAY))
                .setCallback((index, type, action) -> {
                    openCategoryPage(player, category, page, !sellMode);
                })
                .build());

        // Navigation - Next Page
        if (page < totalPages - 1) {
            gui.setSlot(53, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("Next Page →").formatted(Formatting.YELLOW))
                    .setCallback((index, type, action) -> {
                        openCategoryPage(player, category, page + 1, sellMode);
                    })
                    .build());
        }

        gui.open();
    }

    // ════════════════════════════════════════════════════════════
    // SELL INVENTORY - Shows all sellable items from player inventory
    // ════════════════════════════════════════════════════════════

    public static void openSellInventory(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("Quick Sell - Your Items"));

        // Fill background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.LIME_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        // Header
        int coins = CoinManager.getCoins(player);
        gui.setSlot(4, new GuiElementBuilder(Items.SUNFLOWER)
                .setName(Text.literal("Coins: " + ShopManager.formatCoins(coins)).formatted(Formatting.GOLD))
                .build());

        // Scan player inventory for sellable items
        int slot = 9;
        java.util.Map<Item, Integer> sellableItems = new java.util.LinkedHashMap<>();

        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (!stack.isEmpty() && ShopManager.canBeSold(stack.getItem())) {
                sellableItems.merge(stack.getItem(), stack.getCount(), Integer::sum);
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
                    .setName(Text.literal(itemName + " x" + count).formatted(Formatting.WHITE))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Each: " + sellPrice + " coins").formatted(Formatting.GREEN))
                    .addLoreLine(Text.literal("Total: " + totalValue + " coins").formatted(Formatting.GOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Left-click: Sell 1").formatted(Formatting.YELLOW))
                    .addLoreLine(Text.literal("Right-click: Sell 10").formatted(Formatting.YELLOW))
                    .addLoreLine(Text.literal("Shift+Click: Sell All").formatted(Formatting.YELLOW))
                    .setCallback((index, type, action) -> {
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
                    .setName(Text.literal("No Sellable Items").formatted(Formatting.RED))
                    .addLoreLine(Text.literal("Your inventory has no items").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("that can be sold.").formatted(Formatting.GRAY))
                    .build());
        }

        // Back button
        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("Back").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> {
                    openMainMenu(player);
                })
                .build());

        // Sell all button
        if (!sellableItems.isEmpty()) {
            int totalValue = sellableItems.entrySet().stream()
                    .mapToInt(e -> ShopManager.getSellPrice(e.getKey()) * e.getValue())
                    .sum();

            gui.setSlot(50, new GuiElementBuilder(Items.CHEST)
                    .setName(Text.literal("Sell All").formatted(Formatting.GREEN, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Total Value: " + totalValue + " coins").formatted(Formatting.GOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Shift+Click to confirm!").formatted(Formatting.YELLOW))
                    .setCallback((index, type, action) -> {
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

    private static void sellAllItems(ServerPlayerEntity player) {
        int totalCoins = 0;
        int totalItems = 0;

        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (!stack.isEmpty() && ShopManager.canBeSold(stack.getItem())) {
                int sellPrice = ShopManager.getSellPrice(stack.getItem());
                int count = stack.getCount();
                totalCoins += sellPrice * count;
                totalItems += count;
                player.getInventory().setStack(i, ItemStack.EMPTY);
            }
        }

        if (totalCoins > 0) {
            CoinManager.giveCoinsQuiet(player, totalCoins);
            player.sendMessage(Text.literal("Sold " + totalItems + " items for ")
                    .formatted(Formatting.GREEN)
                    .append(Text.literal(ShopManager.formatCoins(totalCoins)).formatted(Formatting.GOLD))
                    .append(Text.literal("!").formatted(Formatting.GREEN)));
            DataManager.save(PoliticalServer.server);
        } else {
            player.sendMessage(Text.literal("No sellable items in your inventory!").formatted(Formatting.RED));
        }
    }
}