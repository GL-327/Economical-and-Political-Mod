package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class SetPriceGui {

    // ════════════════════════════════════════════════════════════
    // MAIN MENU - Category Selection
    // ════════════════════════════════════════════════════════════

    public static void openMainMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("§6⚙ Set Prices — Categories"));

        // Fill background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Text.literal("⚙ Price Editor").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click a category to edit prices").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Buy price is always sell × 3").formatted(Formatting.DARK_GRAY))
                .glow()
                .build());

        // Category icons matching ShopGui
        Item[] categoryIcons = {
                Items.STONE_BRICKS,        // Building Blocks
                Items.DIAMOND,             // Ores & Materials
                Items.COOKED_BEEF,         // Food
                Items.BONE,                // Mob Drops
                Items.REDSTONE,            // Redstone
                Items.DIAMOND_PICKAXE,     // Tools
                Items.DIAMOND_CHESTPLATE,  // Armor
                Items.LANTERN,             // Decoration
                Items.OAK_SAPLING,         // Plants
                Items.BREWING_STAND,       // Brewing
                Items.RED_DYE,             // Dyes
                Items.ENDER_PEARL          // Miscellaneous
        };

        List<String> categories = ShopManager.getCategoryNames();
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};

        for (int i = 0; i < categories.size() && i < slots.length; i++) {
            String category = categories.get(i);
            Item icon = i < categoryIcons.length ? categoryIcons[i] : Items.CHEST;
            List<Item> items = ShopManager.getItemsInCategory(category);
            long priced = items.stream().filter(it -> ShopManager.getSellPrice(it) > 0).count();
            long disabled = items.stream().filter(ShopManager::isDisabled).count();

            final String cat = category;
            gui.setSlot(slots[i], new GuiElementBuilder(icon)
                    .setName(Text.literal(category).formatted(Formatting.YELLOW, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Items: " + items.size()).formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("Priced: " + priced).formatted(Formatting.GREEN))
                    .addLoreLine(disabled > 0
                            ? Text.literal("Disabled: " + disabled).formatted(Formatting.RED)
                            : Text.literal("None disabled").formatted(Formatting.DARK_GRAY))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Click to edit").formatted(Formatting.YELLOW))
                    .setCallback((index, type, action) -> openCategoryPage(player, cat, 0))
                    .build());
        }

        // Link to /shop
        gui.setSlot(48, new GuiElementBuilder(Items.CHEST)
                .setName(Text.literal("🏪 Open Shop").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Open the player shop GUI").formatted(Formatting.GRAY))
                .setCallback((index, type, action) -> ShopGui.openMainMenu(player))
                .build());

        // Reset all prices
        gui.setSlot(50, new GuiElementBuilder(Items.TNT)
                .setName(Text.literal("⚠ Reset All Prices").formatted(Formatting.DARK_RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Resets ALL prices and disabled items").formatted(Formatting.RED))
                .addLoreLine(Text.literal("to their defaults.").formatted(Formatting.RED))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Shift+Click to confirm").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> {
                    if (type.shift) {
                        ShopManager.resetPrices();
                        player.sendMessage(Text.literal("✓ Reset all prices to defaults.").formatted(Formatting.GREEN), false);
                        openMainMenu(player);
                    } else {
                        player.sendMessage(Text.literal("Shift+Click to confirm reset!").formatted(Formatting.YELLOW), false);
                    }
                })
                .build());

        gui.open();
    }

    // ════════════════════════════════════════════════════════════
    // CATEGORY PAGE - List items with prices
    // ════════════════════════════════════════════════════════════

    public static void openCategoryPage(ServerPlayerEntity player, String category, int page) {
        List<Item> items = ShopManager.getItemsInCategory(category);
        int itemsPerPage = 36;
        int totalPages = Math.max(1, (int) Math.ceil((double) items.size() / itemsPerPage));
        if (page < 0) page = 0;
        if (page >= totalPages) page = totalPages - 1;
        final int currentPage = page;

        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("§6⚙ " + category + " — Page " + (page + 1) + "/" + totalPages));

        // Fill background (bottom row only)
        for (int i = 45; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        int start = page * itemsPerPage;
        int end = Math.min(start + itemsPerPage, items.size());

        for (int i = start; i < end; i++) {
            Item item = items.get(i);
            int slot = i - start;
            int sellPrice = ShopManager.getSellPrice(item);
            int buyPrice = ShopManager.getBuyPrice(item);
            boolean disabled = ShopManager.isDisabled(item);
            String itemName = ShopManager.getItemName(item);

            Formatting nameColor = disabled ? Formatting.RED : (sellPrice > 0 ? Formatting.WHITE : Formatting.DARK_GRAY);
            GuiElementBuilder builder = new GuiElementBuilder(item)
                    .setName(Text.literal(itemName).formatted(nameColor))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(sellPrice > 0
                            ? Text.literal("Sell: " + sellPrice + " | Buy: " + buyPrice).formatted(Formatting.GREEN)
                            : Text.literal("No price set").formatted(Formatting.DARK_GRAY))
                    .addLoreLine(disabled
                            ? Text.literal("§c[DISABLED]")
                            : Text.literal("§a[ENABLED]"))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Click to edit").formatted(Formatting.YELLOW))
                    .setCallback((index, type, action) -> openItemEditGui(player, item, category, currentPage));

            gui.setSlot(slot, builder.build());
        }

        // Prev/Next
        if (page > 0) {
            gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("← Previous Page").formatted(Formatting.YELLOW))
                    .setCallback((index, type, action) -> openCategoryPage(player, category, currentPage - 1))
                    .build());
        }
        if (page < totalPages - 1) {
            gui.setSlot(53, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("Next Page →").formatted(Formatting.YELLOW))
                    .setCallback((index, type, action) -> openCategoryPage(player, category, currentPage + 1))
                    .build());
        }

        // Back
        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("← Back to Categories").formatted(Formatting.GRAY))
                .setCallback((index, type, action) -> openMainMenu(player))
                .build());

        gui.open();
    }

    // ════════════════════════════════════════════════════════════
    // ITEM EDIT GUI - Adjust price for a single item
    // ════════════════════════════════════════════════════════════

    public static void openItemEditGui(ServerPlayerEntity player, Item item, String category, int returnPage) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
        String itemName = ShopManager.getItemName(item);
        gui.setTitle(Text.literal("§6⚙ Edit: " + itemName));

        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        int sellPrice = ShopManager.getSellPrice(item);
        int buyPrice = ShopManager.getBuyPrice(item);
        boolean disabled = ShopManager.isDisabled(item);

        // Center: item info display
        gui.setSlot(13, new GuiElementBuilder(item)
                .setName(Text.literal(itemName).formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Sell Price: " + sellPrice + " coins").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal("Buy Price:  " + buyPrice + " coins").formatted(Formatting.RED))
                .addLoreLine(Text.literal("Status: " + (disabled ? "§c[DISABLED]" : "§a[ENABLED]")))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Adjust with buttons below").formatted(Formatting.GRAY))
                .build());

        // Subtract buttons
        int[] diffs = {-100, -10, -1, 1, 10, 100};
        Item[] diffIcons = {Items.RED_CONCRETE, Items.RED_CONCRETE, Items.RED_CONCRETE,
                Items.LIME_CONCRETE, Items.LIME_CONCRETE, Items.LIME_CONCRETE};
        Formatting[] diffColors = {Formatting.DARK_RED, Formatting.RED, Formatting.LIGHT_PURPLE,
                Formatting.GREEN, Formatting.YELLOW, Formatting.GOLD};
        int[] diffSlots = {0, 1, 2, 6, 7, 8};

        for (int i = 0; i < diffs.length; i++) {
            final int delta = diffs[i];
            String label = (delta > 0 ? "+" : "") + delta + " Sell Price";
            gui.setSlot(diffSlots[i], new GuiElementBuilder(diffIcons[i])
                    .setName(Text.literal(label).formatted(diffColors[i], Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Current: " + sellPrice).formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("New: " + Math.max(0, sellPrice + delta)).formatted(Formatting.YELLOW))
                    .setCallback((index, type, action) -> {
                        int cur = ShopManager.getSellPrice(item);
                        int newPrice = Math.max(0, cur + delta);
                        ShopManager.setCustomPrice(item, newPrice);
                        player.sendMessage(Text.literal("✓ Set " + itemName + " sell price to " + newPrice)
                                .formatted(Formatting.GREEN), false);
                        openItemEditGui(player, item, category, returnPage);
                    })
                    .build());
        }

        // Disable/Enable toggle
        gui.setSlot(18, new GuiElementBuilder(disabled ? Items.GREEN_CONCRETE : Items.RED_CONCRETE)
                .setName(disabled
                        ? Text.literal("Enable Item in Shop").formatted(Formatting.GREEN, Formatting.BOLD)
                        : Text.literal("Disable Item from Shop").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(disabled
                        ? Text.literal("Click to re-enable this item").formatted(Formatting.GRAY)
                        : Text.literal("Click to remove from shop").formatted(Formatting.GRAY))
                .setCallback((index, type, action) -> {
                    if (ShopManager.isDisabled(item)) {
                        ShopManager.enableItem(item);
                        player.sendMessage(Text.literal("✓ Enabled " + itemName + " in shop.").formatted(Formatting.GREEN), false);
                    } else {
                        ShopManager.disableItem(item);
                        player.sendMessage(Text.literal("✓ Disabled " + itemName + " from shop.").formatted(Formatting.RED), false);
                    }
                    openItemEditGui(player, item, category, returnPage);
                })
                .build());

        // Reset to default
        gui.setSlot(22, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("Reset to Default Price").formatted(Formatting.DARK_RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Resets this item's price to its").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("default shop value.").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Shift+Click to confirm").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> {
                    if (type.shift) {
                        ShopManager.resetItemPrice(item);
                        player.sendMessage(Text.literal("✓ Reset " + itemName + " to default price.").formatted(Formatting.GREEN), false);
                        openItemEditGui(player, item, category, returnPage);
                    } else {
                        player.sendMessage(Text.literal("Shift+Click to confirm reset!").formatted(Formatting.YELLOW), false);
                    }
                })
                .build());

        // Back
        gui.setSlot(26, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to " + category).formatted(Formatting.GRAY))
                .setCallback((index, type, action) -> openCategoryPage(player, category, returnPage))
                .build());

        gui.open();
    }
}
