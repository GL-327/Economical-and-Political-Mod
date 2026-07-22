package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

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
    public static void openMainMenu(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("🎨 Texture Pack Helper"));

        // Background
        GuiElementBuilder bg = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                .setName(Component.literal(""));
        for (int i = 0; i < 54; i++) gui.setSlot(i, bg.build());

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.PAINTING)
                .setName(Component.literal("🎨 Texture Pack Helper").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Browse all custom items and their").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("texture pack codes for resource packs.").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click a category to view items.").withStyle(ChatFormatting.YELLOW))
                .glow().build());

        // Categories - get this first for stats
        Map<String, List<TexturePackCodes.CustomItemEntry>> categories = TexturePackCodes.getAllItemsByCategory();

        // Export All Codes button
        gui.setSlot(48, new GuiElementBuilder(Items.WRITABLE_BOOK)
                .setName(Component.literal("📄 Export to Chat").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to get all texture codes").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("in chat for easy copying.").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to export").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> exportAllCodes(player))
                .build());

        // Export to Book button
        gui.setSlot(49, new GuiElementBuilder(Items.WRITTEN_BOOK)
                .setName(Component.literal("📕 Export to Book").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Get a written book containing").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("all texture codes.").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to receive book").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> exportToBook(player))
                .glow().build());

        // Total count info
        int totalItems = TexturePackCodes.getAllItems().size();
        gui.setSlot(50, new GuiElementBuilder(Items.KNOWLEDGE_BOOK)
                .setName(Component.literal("📊 Statistics").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Total custom items: " + totalItems).withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Categories: " + categories.size()).withStyle(ChatFormatting.GRAY))
                .glow().build());

        // Category buttons
        int slot = 10;
        for (Map.Entry<String, List<TexturePackCodes.CustomItemEntry>> entry : categories.entrySet()) {
            if (slot > 43) break;
            if (slot == 17 || slot == 26 || slot == 35) slot += 2; // Skip edges

            String categoryName = entry.getKey();
            int itemCount = entry.getValue().size();

            gui.setSlot(slot++, new GuiElementBuilder(Items.BOOK)
                    .setName(Component.literal(categoryName).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Contains " + itemCount + " items").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("Click to view codes").withStyle(ChatFormatting.YELLOW))
                    .setCallback((idx, type, containerInput, slotGui) -> openCategory(player, categoryName, 0))
                    .build());
        }

        gui.open();
    }

    /**
     * Opens a category showing all items with their texture codes.
     */
    public static void openCategory(ServerPlayer player, String categoryName, int page) {
        Map<String, List<TexturePackCodes.CustomItemEntry>> categories = TexturePackCodes.getAllItemsByCategory();
        List<TexturePackCodes.CustomItemEntry> items = categories.get(categoryName);

        if (items == null || items.isEmpty()) {
            player.sendSystemMessage(Component.literal("§cNo items found in this category."), false);
            return;
        }

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("🎨 " + categoryName));

        // Background
        GuiElementBuilder bg = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                .setName(Component.literal(""));
        for (int i = 0; i < 54; i++) gui.setSlot(i, bg.build());

        // Calculate pagination
        int totalPages = (int) Math.ceil((double) items.size() / ITEMS_PER_PAGE);
        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, items.size());

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.WRITABLE_BOOK)
                .setName(Component.literal(categoryName).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Page " + (page + 1) + " of " + totalPages).withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Showing items " + (startIndex + 1) + "-" + endIndex).withStyle(ChatFormatting.GRAY))
                .glow().build());

        // Items
        int slot = 9;
        for (int i = startIndex; i < endIndex && slot < 54; i++) {
            TexturePackCodes.CustomItemEntry entry = items.get(i);

            // Create display item
            ItemStack displayItem = new ItemStack(Items.PAPER);
            displayItem.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,
                    Component.literal(entry.displayName()).withStyle(ChatFormatting.AQUA));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.literal(""));
            lore.add(Component.literal("§7Texture Code:").withStyle(ChatFormatting.GRAY));
            lore.add(Component.literal("§e" + entry.textureCode()).withStyle(ChatFormatting.YELLOW));
            lore.add(Component.literal(""));
            lore.add(Component.literal("§7Item ID: §f" + entry.id()).withStyle(ChatFormatting.GRAY));
            if (entry.color() != 0) {
                lore.add(Component.literal("§7Color: §#" + String.format("%06X", entry.color()) + "■■■").withStyle(ChatFormatting.GRAY));
            }
            lore.add(Component.literal(""));
            lore.add(Component.literal("§bClick to copy code to chat").withStyle(ChatFormatting.AQUA));

            displayItem.set(net.minecraft.core.component.DataComponents.LORE, new net.minecraft.world.item.component.ItemLore(lore));

            gui.setSlot(slot++, new GuiElementBuilder(displayItem)
                    .setCallback((idx, type, containerInput, slotGui) -> {
                        // Send the texture code to the player's chat
                        player.sendSystemMessage(Component.literal("§a§lTexture Code for " + entry.displayName() + ":§r"), false);
                        player.sendSystemMessage(Component.literal("§e" + entry.textureCode()), false);
                        player.sendSystemMessage(Component.literal("§7Item ID: §f" + entry.id()), false);
                    })
                    .build());
        }

        // Navigation
        if (page > 0) {
            gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("← Previous Page").withStyle(ChatFormatting.YELLOW))
                    .setCallback((idx, type, containerInput, slotGui) -> openCategory(player, categoryName, page - 1))
                    .build());
        }

        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("← Back to Categories").withStyle(ChatFormatting.RED))
                .setCallback((idx, type, containerInput, slotGui) -> openMainMenu(player))
                .build());

        if (page < totalPages - 1) {
            gui.setSlot(53, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("Next Page →").withStyle(ChatFormatting.YELLOW))
                    .setCallback((idx, type, containerInput, slotGui) -> openCategory(player, categoryName, page + 1))
                    .build());
        }

        gui.open();
    }

    /**
     * Opens a search view showing all items with codes in a compact format.
     */
    public static void openAllItemsView(ServerPlayer player, int page) {
        List<TexturePackCodes.CustomItemEntry> allItems = TexturePackCodes.getAllItems();

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("🎨 All Custom Items"));

        // Background
        GuiElementBuilder bg = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                .setName(Component.literal(""));
        for (int i = 0; i < 54; i++) gui.setSlot(i, bg.build());

        // Calculate pagination
        int totalPages = (int) Math.ceil((double) allItems.size() / ITEMS_PER_PAGE);
        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, allItems.size());

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.KNOWLEDGE_BOOK)
                .setName(Component.literal("All Custom Items").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Total: " + allItems.size() + " items").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Page " + (page + 1) + " of " + totalPages).withStyle(ChatFormatting.GRAY))
                .glow().build());

        // Items
        int slot = 9;
        for (int i = startIndex; i < endIndex && slot < 54; i++) {
            TexturePackCodes.CustomItemEntry entry = allItems.get(i);

            ItemStack displayItem = new ItemStack(Items.PAPER);
            displayItem.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,
                    Component.literal(entry.displayName()).withStyle(ChatFormatting.AQUA));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.literal("§e" + entry.textureCode()).withStyle(ChatFormatting.YELLOW));
            lore.add(Component.literal("§7ID: §f" + entry.id()).withStyle(ChatFormatting.GRAY));

            displayItem.set(net.minecraft.core.component.DataComponents.LORE, new net.minecraft.world.item.component.ItemLore(lore));

            gui.setSlot(slot++, new GuiElementBuilder(displayItem)
                    .setCallback((idx, type, containerInput, slotGui) -> {
                        player.sendSystemMessage(Component.literal("§a§l" + entry.displayName() + "§r §7- §e" + entry.textureCode()), false);
                    })
                    .build());
        }

        // Navigation
        if (page > 0) {
            gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("← Previous Page").withStyle(ChatFormatting.YELLOW))
                    .setCallback((idx, type, containerInput, slotGui) -> openAllItemsView(player, page - 1))
                    .build());
        }

        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("← Back to Categories").withStyle(ChatFormatting.RED))
                .setCallback((idx, type, containerInput, slotGui) -> openMainMenu(player))
                .build());

        if (page < totalPages - 1) {
            gui.setSlot(53, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("Next Page →").withStyle(ChatFormatting.YELLOW))
                    .setCallback((idx, type, containerInput, slotGui) -> openAllItemsView(player, page + 1))
                    .build());
        }

        gui.open();
    }

    /**
     * Exports all texture codes to the player's chat for easy copying.
     */
    public static void exportAllCodes(ServerPlayer player) {
        player.sendSystemMessage(Component.literal("═══════════════════════════════════════════════════").withStyle(ChatFormatting.GOLD), false);
        player.sendSystemMessage(Component.literal("🎨 ALL TEXTURE PACK CODES").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
        player.sendSystemMessage(Component.literal("═══════════════════════════════════════════════════").withStyle(ChatFormatting.GOLD), false);
        
        Map<String, List<TexturePackCodes.CustomItemEntry>> categories = TexturePackCodes.getAllItemsByCategory();
        
        for (Map.Entry<String, List<TexturePackCodes.CustomItemEntry>> entry : categories.entrySet()) {
            player.sendSystemMessage(Component.literal(""), false);
            player.sendSystemMessage(Component.literal("■ " + entry.getKey()).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), false);
            
            for (TexturePackCodes.CustomItemEntry item : entry.getValue()) {
                player.sendSystemMessage(Component.literal("  " + item.displayName() + " → " + item.textureCode())
                        .withStyle(ChatFormatting.GRAY), false);
            }
        }
        
        player.sendSystemMessage(Component.literal(""), false);
        player.sendSystemMessage(Component.literal("═══════════════════════════════════════════════════").withStyle(ChatFormatting.GOLD), false);
        player.sendSystemMessage(Component.literal("✓ Exported " + TexturePackCodes.getAllItems().size() + " items").withStyle(ChatFormatting.GREEN), false);
        player.sendSystemMessage(Component.literal("═══════════════════════════════════════════════════").withStyle(ChatFormatting.GOLD), false);
    }

    /**
     * Exports all texture codes to a written book for easy reference.
     */
    public static void exportToBook(ServerPlayer player) {
        // Create a written book with texture code reference
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        book.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,
                Component.literal("Texture Pack Codes Reference").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("Total Items: " + TexturePackCodes.getAllItems().size()).withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Use /texturehelper to browse").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("all texture codes.").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Click 'Export to Chat' in GUI").withStyle(ChatFormatting.AQUA));
        lore.add(Component.literal("to get all codes in chat.").withStyle(ChatFormatting.AQUA));
        
        book.set(net.minecraft.core.component.DataComponents.LORE, new net.minecraft.world.item.component.ItemLore(lore));

        // Give book to player
        player.addItem(book);
        player.sendSystemMessage(Component.literal("§a§l✓ Received Texture Pack Codes reference book!").withStyle(ChatFormatting.GREEN), false);
        player.sendSystemMessage(Component.literal("§7Use 'Export to Chat' button in /texturehelper to get all codes.").withStyle(ChatFormatting.GRAY), false);
    }
}
