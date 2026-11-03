package com.political;


import com.political.economy.ShopManager;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.gui.SignGui;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.List;

public class SetPriceGui {

    // ════════════════════════════════════════════════════════════
    // MAIN MENU - Category Selection
    // ════════════════════════════════════════════════════════════

    public static void openMainMenu(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("§6⚙ Set Prices — Categories"));

        // Fill background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.black())
                    .setName(Component.literal(""))
                    .build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Component.literal("⚙ Price Editor").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click a category to edit prices").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Buy price is always sell × 10").withStyle(ChatFormatting.DARK_GRAY))
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
                Items.DYE.red(),             // Dyes
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
                    .setName(Component.literal(category).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Items: " + items.size()).withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("Priced: " + priced).withStyle(ChatFormatting.GREEN))
                    .addLoreLine(disabled > 0
                            ? Component.literal("Disabled: " + disabled).withStyle(ChatFormatting.RED)
                            : Component.literal("None disabled").withStyle(ChatFormatting.DARK_GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to edit").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> openCategoryPage(player, cat, 0))
                    .build());
        }

        // Link to /shop
        gui.setSlot(48, new GuiElementBuilder(Items.CHEST)
                .setName(Component.literal("🏪 Open Shop").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Open the player shop GUI").withStyle(ChatFormatting.GRAY))
                .setCallback((index, type, containerInput, slotGui) -> ShopGui.openMainMenu(player))
                .build());

        // Reset all prices
        gui.setSlot(50, new GuiElementBuilder(Items.TNT)
                .setName(Component.literal("⚠ Reset All Prices").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Resets ALL prices and disabled items").withStyle(ChatFormatting.RED))
                .addLoreLine(Component.literal("to their defaults.").withStyle(ChatFormatting.RED))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Shift+Click to confirm").withStyle(ChatFormatting.YELLOW))
                .setCallback((index, type, containerInput, slotGui) -> {
                    if (type.shift) {
                        ShopManager.resetPrices();
                        player.sendSystemMessage(Component.literal("✓ Reset all prices to defaults.").withStyle(ChatFormatting.GREEN), false);
                        openMainMenu(player);
                    } else {
                        player.sendSystemMessage(Component.literal("Shift+Click to confirm reset!").withStyle(ChatFormatting.YELLOW), false);
                    }
                })
                .build());

        gui.open();
    }

    // ════════════════════════════════════════════════════════════
    // CATEGORY PAGE - List items with prices
    // ════════════════════════════════════════════════════════════

    public static void openCategoryPage(ServerPlayer player, String category, int page) {
        List<Item> items = ShopManager.getItemsInCategory(category);
        int itemsPerPage = 36;
        int totalPages = Math.max(1, (int) Math.ceil((double) items.size() / itemsPerPage));
        if (page < 0) page = 0;
        if (page >= totalPages) page = totalPages - 1;
        final int currentPage = page;

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("§6⚙ " + category + " — Page " + (page + 1) + "/" + totalPages));

        // Fill background (bottom row only)
        for (int i = 45; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.gray())
                    .setName(Component.literal(""))
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

            ChatFormatting nameColor = disabled ? ChatFormatting.RED : (sellPrice > 0 ? ChatFormatting.WHITE : ChatFormatting.DARK_GRAY);
            GuiElementBuilder builder = new GuiElementBuilder(item)
                    .setName(Component.literal(itemName).withStyle(nameColor))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(sellPrice > 0
                            ? Component.literal("Sell: " + sellPrice + " | Buy: " + buyPrice).withStyle(ChatFormatting.GREEN)
                            : Component.literal("No price set").withStyle(ChatFormatting.DARK_GRAY))
                    .addLoreLine(disabled
                            ? Component.literal("§c[DISABLED]")
                            : Component.literal("§a[ENABLED]"))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to edit").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> openItemEditGui(player, item, category, currentPage));

            gui.setSlot(slot, builder.build());
        }

        // Prev/Next
        if (page > 0) {
            gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("← Previous Page").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> openCategoryPage(player, category, currentPage - 1))
                    .build());
        }
        if (page < totalPages - 1) {
            gui.setSlot(53, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("Next Page →").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> openCategoryPage(player, category, currentPage + 1))
                    .build());
        }

        // Back
        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("← Back to Categories").withStyle(ChatFormatting.GRAY))
                .setCallback((index, type, containerInput, slotGui) -> openMainMenu(player))
                .build());

        gui.open();
    }

    // ════════════════════════════════════════════════════════════
    // ITEM EDIT GUI - Adjust price for a single item
    // ════════════════════════════════════════════════════════════

    public static void openItemEditGui(ServerPlayer player, Item item, String category, int returnPage) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        String itemName = ShopManager.getItemName(item);
        gui.setTitle(Component.literal("§6⚙ Edit: " + itemName));

        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.gray())
                    .setName(Component.literal(""))
                    .build());
        }

        int sellPrice = ShopManager.getSellPrice(item);
        int buyPrice = ShopManager.getBuyPrice(item);
        boolean disabled = ShopManager.isDisabled(item);

        // Center: item info display
        gui.setSlot(13, new GuiElementBuilder(item)
                .setName(Component.literal(itemName).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Sell Price: " + sellPrice + " coins").withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal("Buy Price:  " + buyPrice + " coins").withStyle(ChatFormatting.RED))
                .addLoreLine(Component.literal("Status: " + (disabled ? "§c[DISABLED]" : "§a[ENABLED]")))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Adjust with buttons below").withStyle(ChatFormatting.GRAY))
                .build());

        // Subtract buttons
        int[] diffs = {-1000, -100, -10, 10, 100, 1000};
        Item[] diffIcons = {Items.CONCRETE.red(), Items.CONCRETE.red(), Items.CONCRETE.red(),
                Items.CONCRETE.lime(), Items.CONCRETE.lime(), Items.CONCRETE.lime()};
        ChatFormatting[] diffColors = {ChatFormatting.DARK_RED, ChatFormatting.RED, ChatFormatting.LIGHT_PURPLE,
                ChatFormatting.GREEN, ChatFormatting.YELLOW, ChatFormatting.GOLD};
        int[] diffSlots = {0, 1, 2, 6, 7, 8};

        for (int i = 0; i < diffs.length; i++) {
            final int delta = diffs[i];
            String label = (delta > 0 ? "+" : "") + delta + " Sell Price";
            gui.setSlot(diffSlots[i], new GuiElementBuilder(diffIcons[i])
                    .setName(Component.literal(label).withStyle(diffColors[i], ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Current: " + sellPrice).withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("New: " + Math.max(0, sellPrice + delta)).withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        int cur = ShopManager.getSellPrice(item);
                        int newPrice = Math.max(0, cur + delta);
                        ShopManager.setCustomPrice(item, newPrice);
                        player.sendSystemMessage(Component.literal("✓ Set " + itemName + " sell price to " + newPrice)
                                .withStyle(ChatFormatting.GREEN), false);
                        openItemEditGui(player, item, category, returnPage);
                    })
                    .build());
        }

        // Custom price input via SignGui
        gui.setSlot(3, new GuiElementBuilder(Items.WRITABLE_BOOK)
                .setName(Component.literal("✏ Set Custom Price").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to enter a custom").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("sell price using a sign.").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Buy price = Sell × 10").withStyle(ChatFormatting.DARK_GRAY))
                .setCallback((index, type, containerInput, slotGui) -> openPriceSignGui(player, item, category, returnPage))
                .build());

        // Disable/Enable toggle
        gui.setSlot(18, new GuiElementBuilder(disabled ? Items.CONCRETE.green() : Items.CONCRETE.red())
                .setName(disabled
                        ? Component.literal("Enable Item in Shop").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)
                        : Component.literal("Disable Item from Shop").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(disabled
                        ? Component.literal("Click to re-enable this item").withStyle(ChatFormatting.GRAY)
                        : Component.literal("Click to remove from shop").withStyle(ChatFormatting.GRAY))
                .setCallback((index, type, containerInput, slotGui) -> {
                    if (ShopManager.isDisabled(item)) {
                        ShopManager.enableItem(item);
                        player.sendSystemMessage(Component.literal("✓ Enabled " + itemName + " in shop.").withStyle(ChatFormatting.GREEN), false);
                    } else {
                        ShopManager.disableItem(item);
                        player.sendSystemMessage(Component.literal("✓ Disabled " + itemName + " from shop.").withStyle(ChatFormatting.RED), false);
                    }
                    openItemEditGui(player, item, category, returnPage);
                })
                .build());

        // Reset to default
        gui.setSlot(22, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("Reset to Default Price").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Resets this item's price to its").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("default shop value.").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Shift+Click to confirm").withStyle(ChatFormatting.YELLOW))
                .setCallback((index, type, containerInput, slotGui) -> {
                    if (type.shift) {
                        ShopManager.resetItemPrice(item);
                        player.sendSystemMessage(Component.literal("✓ Reset " + itemName + " to default price.").withStyle(ChatFormatting.GREEN), false);
                        openItemEditGui(player, item, category, returnPage);
                    } else {
                        player.sendSystemMessage(Component.literal("Shift+Click to confirm reset!").withStyle(ChatFormatting.YELLOW), false);
                    }
                })
                .build());

        // Back
        gui.setSlot(26, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back to " + category).withStyle(ChatFormatting.GRAY))
                .setCallback((index, type, containerInput, slotGui) -> openCategoryPage(player, category, returnPage))
                .build());

        gui.open();
    }

    // ════════════════════════════════════════════════════════════
    // SIGN GUI - Custom price input
    // ════════════════════════════════════════════════════════════

    private static void openPriceSignGui(ServerPlayer player, Item item, String category, int returnPage) {
        String itemName = ShopManager.getItemName(item);
        int currentPrice = ShopManager.getSellPrice(item);

        SignGui signGui = new SignGui(player) {
            @Override
            public void onManualClose() {
                String input = this.getLine(0).getString().trim();

                if (!input.isEmpty()) {
                    try {
                        int newPrice = Integer.parseInt(input);
                        if (newPrice >= 0) {
                            ShopManager.setCustomPrice(item, newPrice);
                            int buyPrice = newPrice * 10;
                            player.sendSystemMessage(Component.literal("✓ Set " + itemName + " sell price to " + newPrice + 
                                    " (buy: " + buyPrice + ")").withStyle(ChatFormatting.GREEN), false);
                        } else {
                            player.sendSystemMessage(Component.literal("Price must be 0 or positive!").withStyle(ChatFormatting.RED), false);
                        }
                    } catch (NumberFormatException e) {
                        player.sendSystemMessage(Component.literal("Invalid number: " + input).withStyle(ChatFormatting.RED), false);
                    }
                }
                // Reopen the edit GUI
                openItemEditGui(player, item, category, returnPage);
            }
        };

        // Set sign lines
        signGui.setLine(0, Component.literal(""));
        signGui.setLine(1, Component.literal("^^^^^^^^^^^^^^").withStyle(ChatFormatting.YELLOW));
        signGui.setLine(2, Component.literal("Enter sell price").withStyle(ChatFormatting.AQUA));
        signGui.setLine(3, Component.literal("Current: " + currentPrice).withStyle(ChatFormatting.GRAY));

        signGui.open();
    }
}
