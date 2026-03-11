package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class CustomItemsGui {

    private static final int ITEMS_PER_PAGE = 45;

    /** Builds the full list of all custom items organized by category. */
    private static List<ItemStack> buildItemList() {
        List<ItemStack> items = new ArrayList<>();

        // Bounty Swords T1
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            items.add(SlayerItems.createSlayerSword(type));
        }

        // Bounty Swords T2
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            items.add(SlayerItems.createUpgradedSlayerSword(type));
        }

        // Legendary Bounty Armor
        items.add(SlayerItems.createZombieBerserkerHelmet());
        items.add(SlayerItems.createSpiderLeggings());
        items.add(SlayerItems.createSkeletonBow());
        items.add(SlayerItems.createSlimeBoots());
        items.add(SlayerItems.createWardenChestplate());

        // Special Weapons
        items.add(CustomItemHandler.createGavel());
        items.add(CustomItemHandler.createHarveysStick());
        items.add(CustomItemHandler.createHermesShoes());
        for (int mk = 1; mk <= 5; mk++) items.add(CustomItemHandler.createHPEBM(mk));
        items.add(CustomItemHandler.createHPEBM(7));

        // T1 Armor Sets
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            items.add(SlayerItems.createSlayerHelmet(type, 1));
            items.add(SlayerItems.createSlayerChestplate(type, 1));
            items.add(SlayerItems.createSlayerLeggings(type, 1));
            items.add(SlayerItems.createSlayerBoots(type, 1));
        }

        // T2 Armor Sets
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            items.add(SlayerItems.createSlayerHelmet(type, 2));
            items.add(SlayerItems.createSlayerChestplate(type, 2));
            items.add(SlayerItems.createSlayerLeggings(type, 2));
            items.add(SlayerItems.createSlayerBoots(type, 2));
        }

        // Materials
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            items.add(SlayerItems.createChunk(type));
            items.add(SlayerItems.createCore(type));
        }

        // Piglin Items
        items.add(SlayerItems.createCrownOfGreed());
        items.add(SlayerItems.createPiglinCore());
        items.add(SlayerItems.createPiglinFlesh());

        return items;
    }

    public static void open(ServerPlayerEntity player) {
        openPage(player, 0);
    }

    public static void openPage(ServerPlayerEntity player, int page) {
        List<ItemStack> allItems = buildItemList();
        int totalPages = Math.max(1, (allItems.size() + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE);
        if (page < 0) page = 0;
        if (page >= totalPages) page = totalPages - 1;

        final int currentPage = page;
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("§6Custom Items — Page " + (page + 1) + "/" + totalPages));

        // Gray background on navigation row
        GuiElementBuilder bg = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                .setName(Text.literal(""));
        for (int i = 45; i < 54; i++) gui.setSlot(i, bg.build());

        // Previous page button
        if (page > 0) {
            gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("← Previous Page").formatted(Formatting.YELLOW))
                    .setCallback((index, clickType, actionType) -> openPage(player, currentPage - 1))
                    .build());
        }

        // Next page button
        if (page < totalPages - 1) {
            gui.setSlot(53, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("Next Page →").formatted(Formatting.YELLOW))
                    .setCallback((index, clickType, actionType) -> openPage(player, currentPage + 1))
                    .build());
        }

        // Fill items for this page
        int start = page * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, allItems.size());
        for (int i = start; i < end; i++) {
            ItemStack item = allItems.get(i);
            if (item == null || item.isEmpty()) continue;
            int slot = i - start;
            Text customName = item.get(DataComponentTypes.CUSTOM_NAME);
            final ItemStack finalItem = item.copy();
            gui.setSlot(slot, new GuiElementBuilder(item.getItem())
                    .setName(customName != null ? customName : Text.literal(item.getName().getString()))
                    .addLoreLine(Text.literal("§aClick to receive").formatted(Formatting.GREEN))
                    .setCallback((index, clickType, actionType) ->
                            player.getInventory().insertStack(finalItem.copy()))
                    .build());
        }

        gui.open();
    }
}
