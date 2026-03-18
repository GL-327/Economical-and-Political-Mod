package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * GUI for the /texturehelper command.
 * Shows all custom items with their texture pack codes for texture pack creators.
 */
public class TextureHelperGui {

    private static final int ITEMS_PER_PAGE = 45;

    /**
     * Opens the main menu showing all categories.
     */
    public static void openMainMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("🎨 Texture Pack Helper"));

        // Background
        GuiElementBuilder bg = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                .setName(Text.literal(""));
        for (int i = 0; i < 54; i++) gui.setSlot(i, bg.build());

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.PAINTING)
                .setName(Text.literal("🎨 Texture Pack Helper").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Browse all custom items and their").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("texture pack codes for resource packs.").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click a category to view items.").formatted(Formatting.YELLOW))
                .glow().build());

        // Categories - get this first for stats
        Map<String, List<TexturePackCodes.CustomItemEntry>> categories = TexturePackCodes.getAllItemsByCategory();

        // Export All Codes button
        gui.setSlot(48, new GuiElementBuilder(Items.WRITABLE_BOOK)
                .setName(Text.literal("📄 Export to Chat").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to get all texture codes").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("in chat for easy copying.").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§eClick to export").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> exportAllCodes(player))
                .build());

        // Export to Book button
        gui.setSlot(49, new GuiElementBuilder(Items.WRITTEN_BOOK)
                .setName(Text.literal("📕 Export to Book").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Get a written book containing").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("all texture codes.").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§eClick to receive book").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> exportToBook(player))
                .glow().build());

        // Total count info
        int totalItems = TexturePackCodes.getAllItems().size();
        gui.setSlot(50, new GuiElementBuilder(Items.KNOWLEDGE_BOOK)
                .setName(Text.literal("📊 Statistics").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Total custom items: " + totalItems).formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Categories: " + categories.size()).formatted(Formatting.GRAY))
                .glow().build());

        // Category buttons
        int slot = 10;
        for (Map.Entry<String, List<TexturePackCodes.CustomItemEntry>> entry : categories.entrySet()) {
            if (slot > 43) break;
            if (slot == 17 || slot == 26 || slot == 35) slot += 2; // Skip edges

            String categoryName = entry.getKey();
            int itemCount = entry.getValue().size();

            gui.setSlot(slot++, new GuiElementBuilder(Items.BOOK)
                    .setName(Text.literal(categoryName).formatted(Formatting.AQUA, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Contains " + itemCount + " items").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("Click to view codes").formatted(Formatting.YELLOW))
                    .setCallback((idx, type, action) -> openCategory(player, categoryName, 0))
                    .build());
        }

        gui.open();
    }

    /**
     * Opens a category showing all items with their texture codes.
     */
    public static void openCategory(ServerPlayerEntity player, String categoryName, int page) {
        Map<String, List<TexturePackCodes.CustomItemEntry>> categories = TexturePackCodes.getAllItemsByCategory();
        List<TexturePackCodes.CustomItemEntry> items = categories.get(categoryName);

        if (items == null || items.isEmpty()) {
            player.sendMessage(Text.literal("§cNo items found in this category."), false);
            return;
        }

        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("🎨 " + categoryName));

        // Background
        GuiElementBuilder bg = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                .setName(Text.literal(""));
        for (int i = 0; i < 54; i++) gui.setSlot(i, bg.build());

        // Calculate pagination
        int totalPages = (int) Math.ceil((double) items.size() / ITEMS_PER_PAGE);
        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, items.size());

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.WRITABLE_BOOK)
                .setName(Text.literal(categoryName).formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Page " + (page + 1) + " of " + totalPages).formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Showing items " + (startIndex + 1) + "-" + endIndex).formatted(Formatting.GRAY))
                .glow().build());

        // Items
        int slot = 9;
        for (int i = startIndex; i < endIndex && slot < 54; i++) {
            TexturePackCodes.CustomItemEntry entry = items.get(i);

            // Create display item
            ItemStack displayItem = new ItemStack(Items.PAPER);
            displayItem.set(net.minecraft.component.DataComponentTypes.CUSTOM_NAME,
                    Text.literal(entry.displayName()).formatted(Formatting.AQUA));

            List<Text> lore = new ArrayList<>();
            lore.add(Text.literal(""));
            lore.add(Text.literal("§7Texture Code:").formatted(Formatting.GRAY));
            lore.add(Text.literal("§e" + entry.textureCode()).formatted(Formatting.YELLOW));
            lore.add(Text.literal(""));
            lore.add(Text.literal("§7Item ID: §f" + entry.id()).formatted(Formatting.GRAY));
            if (entry.color() != 0) {
                lore.add(Text.literal("§7Color: §#" + String.format("%06X", entry.color()) + "■■■").formatted(Formatting.GRAY));
            }
            lore.add(Text.literal(""));
            lore.add(Text.literal("§bClick to copy code to chat").formatted(Formatting.AQUA));

            displayItem.set(net.minecraft.component.DataComponentTypes.LORE, new net.minecraft.component.type.LoreComponent(lore));

            gui.setSlot(slot++, new GuiElementBuilder(displayItem)
                    .setCallback((idx, type, action) -> {
                        // Send the texture code to the player's chat
                        player.sendMessage(Text.literal("§a§lTexture Code for " + entry.displayName() + ":§r"), false);
                        player.sendMessage(Text.literal("§e" + entry.textureCode()), false);
                        player.sendMessage(Text.literal("§7Item ID: §f" + entry.id()), false);
                    })
                    .build());
        }

        // Navigation
        if (page > 0) {
            gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("← Previous Page").formatted(Formatting.YELLOW))
                    .setCallback((idx, type, action) -> openCategory(player, categoryName, page - 1))
                    .build());
        }

        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("← Back to Categories").formatted(Formatting.RED))
                .setCallback((idx, type, action) -> openMainMenu(player))
                .build());

        if (page < totalPages - 1) {
            gui.setSlot(53, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("Next Page →").formatted(Formatting.YELLOW))
                    .setCallback((idx, type, action) -> openCategory(player, categoryName, page + 1))
                    .build());
        }

        gui.open();
    }

    /**
     * Opens a search view showing all items with codes in a compact format.
     */
    public static void openAllItemsView(ServerPlayerEntity player, int page) {
        List<TexturePackCodes.CustomItemEntry> allItems = TexturePackCodes.getAllItems();

        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("🎨 All Custom Items"));

        // Background
        GuiElementBuilder bg = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                .setName(Text.literal(""));
        for (int i = 0; i < 54; i++) gui.setSlot(i, bg.build());

        // Calculate pagination
        int totalPages = (int) Math.ceil((double) allItems.size() / ITEMS_PER_PAGE);
        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, allItems.size());

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.KNOWLEDGE_BOOK)
                .setName(Text.literal("All Custom Items").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Total: " + allItems.size() + " items").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Page " + (page + 1) + " of " + totalPages).formatted(Formatting.GRAY))
                .glow().build());

        // Items
        int slot = 9;
        for (int i = startIndex; i < endIndex && slot < 54; i++) {
            TexturePackCodes.CustomItemEntry entry = allItems.get(i);

            ItemStack displayItem = new ItemStack(Items.PAPER);
            displayItem.set(net.minecraft.component.DataComponentTypes.CUSTOM_NAME,
                    Text.literal(entry.displayName()).formatted(Formatting.AQUA));

            List<Text> lore = new ArrayList<>();
            lore.add(Text.literal("§e" + entry.textureCode()).formatted(Formatting.YELLOW));
            lore.add(Text.literal("§7ID: §f" + entry.id()).formatted(Formatting.GRAY));

            displayItem.set(net.minecraft.component.DataComponentTypes.LORE, new net.minecraft.component.type.LoreComponent(lore));

            gui.setSlot(slot++, new GuiElementBuilder(displayItem)
                    .setCallback((idx, type, action) -> {
                        player.sendMessage(Text.literal("§a§l" + entry.displayName() + "§r §7- §e" + entry.textureCode()), false);
                    })
                    .build());
        }

        // Navigation
        if (page > 0) {
            gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("← Previous Page").formatted(Formatting.YELLOW))
                    .setCallback((idx, type, action) -> openAllItemsView(player, page - 1))
                    .build());
        }

        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("← Back to Categories").formatted(Formatting.RED))
                .setCallback((idx, type, action) -> openMainMenu(player))
                .build());

        if (page < totalPages - 1) {
            gui.setSlot(53, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("Next Page →").formatted(Formatting.YELLOW))
                    .setCallback((idx, type, action) -> openAllItemsView(player, page + 1))
                    .build());
        }

        gui.open();
    }

    /**
     * Exports all texture codes to the player's chat for easy copying.
     */
    public static void exportAllCodes(ServerPlayerEntity player) {
        player.sendMessage(Text.literal("═══════════════════════════════════════════════════").formatted(Formatting.GOLD), false);
        player.sendMessage(Text.literal("🎨 ALL TEXTURE PACK CODES").formatted(Formatting.GOLD, Formatting.BOLD), false);
        player.sendMessage(Text.literal("═══════════════════════════════════════════════════").formatted(Formatting.GOLD), false);
        
        Map<String, List<TexturePackCodes.CustomItemEntry>> categories = TexturePackCodes.getAllItemsByCategory();
        
        for (Map.Entry<String, List<TexturePackCodes.CustomItemEntry>> entry : categories.entrySet()) {
            player.sendMessage(Text.literal(""), false);
            player.sendMessage(Text.literal("■ " + entry.getKey()).formatted(Formatting.AQUA, Formatting.BOLD), false);
            
            for (TexturePackCodes.CustomItemEntry item : entry.getValue()) {
                player.sendMessage(Text.literal("  " + item.displayName() + " → " + item.textureCode())
                        .formatted(Formatting.GRAY), false);
            }
        }
        
        player.sendMessage(Text.literal(""), false);
        player.sendMessage(Text.literal("═══════════════════════════════════════════════════").formatted(Formatting.GOLD), false);
        player.sendMessage(Text.literal("✓ Exported " + TexturePackCodes.getAllItems().size() + " items").formatted(Formatting.GREEN), false);
        player.sendMessage(Text.literal("═══════════════════════════════════════════════════").formatted(Formatting.GOLD), false);
    }

    /**
     * Exports all texture codes to a written book for easy reference.
     */
    public static void exportToBook(ServerPlayerEntity player) {
        // Create a written book with texture code reference
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        book.set(net.minecraft.component.DataComponentTypes.CUSTOM_NAME,
                Text.literal("Texture Pack Codes Reference").formatted(Formatting.GOLD, Formatting.BOLD));

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("Total Items: " + TexturePackCodes.getAllItems().size()).formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Use /texturehelper to browse").formatted(Formatting.YELLOW));
        lore.add(Text.literal("all texture codes.").formatted(Formatting.YELLOW));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Click 'Export to Chat' in GUI").formatted(Formatting.AQUA));
        lore.add(Text.literal("to get all codes in chat.").formatted(Formatting.AQUA));
        
        book.set(net.minecraft.component.DataComponentTypes.LORE, new net.minecraft.component.type.LoreComponent(lore));

        // Give book to player
        player.getInventory().offerOrDrop(book);
        player.sendMessage(Text.literal("§a§l✓ Received Texture Pack Codes reference book!").formatted(Formatting.GREEN), false);
        player.sendMessage(Text.literal("§7Use 'Export to Chat' button in /texturehelper to get all codes.").formatted(Formatting.GRAY), false);
    }
}
