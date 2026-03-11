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

    // ════════════════════════════════════════════════════════════
    // MAIN MENU
    // ════════════════════════════════════════════════════════════

    public static void open(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("§6✦ Custom Items ✦"));

        // Black background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Text.literal("✦ Custom Items ✦").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Browse and receive custom items").formatted(Formatting.GRAY))
                .glow()
                .build());

        // ⚔ Bounty Swords
        gui.setSlot(10, new GuiElementBuilder(Items.IRON_SWORD)
                .setName(Text.literal("⚔ Bounty Swords").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("All T1 & T2 bounty swords").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("7 types × 2 tiers").formatted(Formatting.DARK_GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openSwordsMenu(player))
                .build());

        // 🛡 T1 Bounty Armor
        gui.setSlot(11, new GuiElementBuilder(Items.LEATHER_CHESTPLATE)
                .setName(Text.literal("🛡 Bounty Armor T1").formatted(Formatting.BLUE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("T1 armor sets for all slayer types").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Requires Bounty Level " + SlayerItems.T1_ARMOR_LEVEL_REQ).formatted(Formatting.DARK_GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openArmorMenu(player, 1))
                .build());

        // 🛡 T2 Bounty Armor
        gui.setSlot(12, new GuiElementBuilder(Items.DIAMOND_CHESTPLATE)
                .setName(Text.literal("🛡 Bounty Armor T2").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("T2 armor sets for all slayer types").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Requires Bounty Level " + SlayerItems.T2_ARMOR_LEVEL_REQ).formatted(Formatting.DARK_GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openArmorMenu(player, 2))
                .glow()
                .build());

        // ⭐ Legendary Gear
        gui.setSlot(13, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Text.literal("⭐ Legendary Gear").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("• Berserker Helmet").formatted(Formatting.DARK_GREEN))
                .addLoreLine(Text.literal("• Venomous Crawler Leggings").formatted(Formatting.DARK_RED))
                .addLoreLine(Text.literal("• Bone Desperado's Bow").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("• Gelatinous Rustler Boots").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal("• Sculk Terror Chestplate").formatted(Formatting.DARK_AQUA))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openLegendaryMenu(player))
                .glow()
                .build());

        // 🗡 Special Weapons
        gui.setSlot(14, new GuiElementBuilder(Items.GOLDEN_AXE)
                .setName(Text.literal("🗡 Special Weapons").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("• The Gavel").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("• Harvey's Stick").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("• Hermes Shoes").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("• HPEBM (all marks)").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openSpecialWeaponsMenu(player))
                .build());

        // ✦ Materials
        gui.setSlot(15, new GuiElementBuilder(Items.GOLD_NUGGET)
                .setName(Text.literal("✦ Materials").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Chunks & Cores for all slayer types").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Used in bounty crafting").formatted(Formatting.DARK_GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openMaterialsMenu(player))
                .build());

        // 👑 Piglin Items
        gui.setSlot(16, new GuiElementBuilder(Items.GOLDEN_HELMET)
                .setName(Text.literal("👑 Piglin Items").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("• Crown of Greed").formatted(Formatting.GOLD))
                .addLoreLine(Text.literal("• Piglin Core & Flesh").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("• Piglin T1 & T2 Armor").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openPiglinMenu(player))
                .glow()
                .build());

        gui.open();
    }

    // ════════════════════════════════════════════════════════════
    // SWORDS MENU
    // ════════════════════════════════════════════════════════════

    private static void openSwordsMenu(ServerPlayerEntity player) {
        List<ItemStack> items = new ArrayList<>();
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            items.add(SlayerItems.createSlayerSword(type));
        }
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            items.add(SlayerItems.createUpgradedSlayerSword(type));
        }
        openSubPage(player, "⚔ Bounty Swords", items, 0);
    }

    // ════════════════════════════════════════════════════════════
    // ARMOR MENUS (by type with colored glass separators)
    // ════════════════════════════════════════════════════════════

    private static void openArmorMenu(ServerPlayerEntity player, int tier) {
        String title = tier == 1 ? "🛡 Bounty Armor T1" : "🛡 Bounty Armor T2";
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("§" + (tier == 1 ? "9" : "5") + title));

        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(tier == 1 ? Items.LEATHER_CHESTPLATE : Items.DIAMOND_CHESTPLATE)
                .setName(Text.literal(title).formatted(tier == 1 ? Formatting.BLUE : Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click a type for its armor pieces").formatted(Formatting.GRAY))
                .build());

        // Slayer type buttons
        SlayerManager.SlayerType[] types = SlayerManager.SlayerType.values();
        int[] typeSlots = {10, 11, 12, 13, 14, 15, 16};

        for (int i = 0; i < types.length && i < typeSlots.length; i++) {
            SlayerManager.SlayerType type = types[i];
            final int finalTier = tier;
            gui.setSlot(typeSlots[i], new GuiElementBuilder(type.icon)
                    .setName(Text.literal(type.displayName + " Armor " + (tier == 1 ? "T1" : "T2"))
                            .formatted(type.color, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("4 armor pieces").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Click to browse").formatted(Formatting.YELLOW))
                    .setCallback((idx, clickType, action) -> openArmorTypeMenu(player, type, finalTier))
                    .build());
        }

        // Back
        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to Main").formatted(Formatting.GRAY))
                .setCallback((idx, type, action) -> open(player))
                .build());

        gui.open();
    }

    private static void openArmorTypeMenu(ServerPlayerEntity player, SlayerManager.SlayerType type, int tier) {
        List<ItemStack> pieces = new ArrayList<>();
        pieces.add(SlayerItems.createSlayerHelmet(type, tier));
        pieces.add(SlayerItems.createSlayerChestplate(type, tier));
        pieces.add(SlayerItems.createSlayerLeggings(type, tier));
        pieces.add(SlayerItems.createSlayerBoots(type, tier));

        String title = type.displayName + (tier == 1 ? " T1 Armor" : " T2 Armor II");
        openSubPageWithBack(player, title, pieces,
                () -> openArmorMenu(player, tier));
    }

    // ════════════════════════════════════════════════════════════
    // LEGENDARY GEAR MENU
    // ════════════════════════════════════════════════════════════

    private static void openLegendaryMenu(ServerPlayerEntity player) {
        List<ItemStack> items = new ArrayList<>();
        items.add(SlayerItems.createZombieBerserkerHelmet());
        items.add(SlayerItems.createSpiderLeggings());
        items.add(SlayerItems.createSkeletonBow());
        items.add(SlayerItems.createSlimeBoots());
        items.add(SlayerItems.createWardenChestplate());
        openSubPage(player, "⭐ Legendary Gear", items, 0);
    }

    // ════════════════════════════════════════════════════════════
    // SPECIAL WEAPONS MENU
    // ════════════════════════════════════════════════════════════

    private static void openSpecialWeaponsMenu(ServerPlayerEntity player) {
        List<ItemStack> items = new ArrayList<>();
        items.add(CustomItemHandler.createGavel());
        items.add(CustomItemHandler.createHarveysStick());
        items.add(CustomItemHandler.createHermesShoes());
        for (int mk = 1; mk <= 5; mk++) items.add(CustomItemHandler.createHPEBM(mk));
        items.add(CustomItemHandler.createHPEBM(7));
        openSubPage(player, "🗡 Special Weapons", items, 0);
    }

    // ════════════════════════════════════════════════════════════
    // MATERIALS MENU
    // ════════════════════════════════════════════════════════════

    private static void openMaterialsMenu(ServerPlayerEntity player) {
        List<ItemStack> items = new ArrayList<>();
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            items.add(SlayerItems.createChunk(type));
            items.add(SlayerItems.createCore(type));
        }
        openSubPage(player, "✦ Materials", items, 0);
    }

    // ════════════════════════════════════════════════════════════
    // PIGLIN ITEMS MENU
    // ════════════════════════════════════════════════════════════

    private static void openPiglinMenu(ServerPlayerEntity player) {
        List<ItemStack> items = new ArrayList<>();
        items.add(SlayerItems.createCrownOfGreed());
        items.add(SlayerItems.createPiglinCore());
        items.add(SlayerItems.createPiglinFlesh());
        for (int tier = 1; tier <= 2; tier++) {
            items.add(SlayerItems.createSlayerHelmet(SlayerManager.SlayerType.PIGLIN, tier));
            items.add(SlayerItems.createSlayerChestplate(SlayerManager.SlayerType.PIGLIN, tier));
            items.add(SlayerItems.createSlayerLeggings(SlayerManager.SlayerType.PIGLIN, tier));
            items.add(SlayerItems.createSlayerBoots(SlayerManager.SlayerType.PIGLIN, tier));
        }
        openSubPage(player, "👑 Piglin Items", items, 0);
    }

    // ════════════════════════════════════════════════════════════
    // GENERIC SUB-PAGE (with back to main)
    // ════════════════════════════════════════════════════════════

    private static void openSubPage(ServerPlayerEntity player, String title, List<ItemStack> allItems, int page) {
        openSubPageWithBack(player, title, allItems, page, () -> open(player));
    }

    private static void openSubPageWithBack(ServerPlayerEntity player, String title, List<ItemStack> allItems, Runnable backAction) {
        openSubPageWithBack(player, title, allItems, 0, backAction);
    }

    private static void openSubPageWithBack(ServerPlayerEntity player, String title, List<ItemStack> allItems, int page, Runnable backAction) {
        final int ITEMS_PER_PAGE = 45;
        int totalPages = Math.max(1, (allItems.size() + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE);
        if (page < 0) page = 0;
        if (page >= totalPages) page = totalPages - 1;
        final int currentPage = page;

        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("§6" + title + (totalPages > 1 ? " — " + (page + 1) + "/" + totalPages : "")));

        // Black background on navigation row
        for (int i = 45; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        // Previous page
        if (page > 0) {
            gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("← Previous Page").formatted(Formatting.YELLOW))
                    .setCallback((idx, type, action) -> openSubPageWithBack(player, title, allItems, currentPage - 1, backAction))
                    .build());
        }

        // Back to main menu
        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.GRAY))
                .setCallback((idx, type, action) -> backAction.run())
                .build());

        // Next page
        if (page < totalPages - 1) {
            gui.setSlot(53, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("Next Page →").formatted(Formatting.YELLOW))
                    .setCallback((idx, type, action) -> openSubPageWithBack(player, title, allItems, currentPage + 1, backAction))
                    .build());
        }

        // Fill items
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
                    .setCallback((idx, type, action) ->
                            player.getInventory().insertStack(finalItem.copy()))
                    .build());
        }

        gui.open();
    }
}

