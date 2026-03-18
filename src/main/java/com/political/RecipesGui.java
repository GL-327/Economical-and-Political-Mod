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

public class RecipesGui {

    // ============================================================
    // MAIN MENU - Simplified working version
    // ============================================================
    public static void openMainMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
        gui.setTitle(Text.literal("📖 Recipe Browser"));

        // Background
        GuiElementBuilder bg = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                .setName(Text.literal(""));
        for (int i = 0; i < 27; i++) gui.setSlot(i, bg.build());

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.KNOWLEDGE_BOOK)
                .setName(Text.literal("📖 Recipe Browser").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("🎨 Browse crafting recipes").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("⚡ View all categories").formatted(Formatting.GRAY))
                .glow().build());

        // Search button
        gui.setSlot(0, new GuiElementBuilder(Items.SPYGLASS)
                .setName(Text.literal("🔍 Search Recipes").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to search items by name").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Supports partial matches!").formatted(Formatting.GREEN))
                .setCallback((idx, type, action) -> openSearchGui(player))
                .build());

        // Custom Armor category (Emerald, Lapis, and Block armors merged) - shifted right
        gui.setSlot(11, new GuiElementBuilder(Items.EMERALD)
                .setName(Text.literal("💎 Custom Armor").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("💎 Emerald, Lapis, and Block armors").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openGemAndBlockArmorMenu(player, 0))
                .build());

        // Bounty Sets (T1 & T2 combined) - replaces separate T1/T2 armor/sword buttons
        gui.setSlot(12, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Text.literal("⚔ Bounty Sets (T1 & T2)").formatted(Formatting.DARK_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("🛡 T1 & T2 Armor + Swords").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Complete slayer equipment sets").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openSlayerSetsMenu(player, 0))
                .build());

        // Materials category - shifted right
        gui.setSlot(13, new GuiElementBuilder(Items.SLIME_BALL)
                .setName(Text.literal("📦 Materials").formatted(Formatting.GRAY, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("📦 Cores, chunks & compacted items").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ New enchanted items added!").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openMaterialsMenu(player))
                .build());

        // Special/Legendary Armor category - shifted right
        gui.setSlot(15, new GuiElementBuilder(Items.NETHERITE_HELMET)
                .setName(Text.literal("🛡 Special Armor").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("🛡 Legendary & special armor pieces").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openSpecialArmorMenu(player))
                .build());

        // Row 2: More categories - shifted right
        // Special Items category
        gui.setSlot(19, new GuiElementBuilder(Items.STICK)
                .setName(Text.literal("🎁 Special Items").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("🎁 Special unique items").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openSpecialItemsMenu(player))
                .build());

        // Custom Tools category
        gui.setSlot(20, new GuiElementBuilder(Items.NETHERITE_PICKAXE)
                .setName(Text.literal("⛏ Custom Tools").formatted(Formatting.DARK_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("⛏ Legendary custom tools").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openCustomToolsMenu(player))
                .build());

        // Piglin Items category
        gui.setSlot(21, new GuiElementBuilder(Items.GOLDEN_HELMET)
                .setName(Text.literal("👑 Piglin Items").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("👑 Piglin-themed items").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openPiglinItemsMenu(player))
                .build());

        // Gold Armor Evolution category
        gui.setSlot(22, new GuiElementBuilder(Items.GOLDEN_CHESTPLATE)
                .setName(Text.literal("👑 Gold Armor Evolution").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Gold armor tiers (Pure → Gilded)").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openGoldArmorMenu(player))
                .build());

        // Attribute Tokens category
        gui.setSlot(23, new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal("✧ Attribute Tokens").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Armor attribute tokens").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openAttributeTokensMenu(player))
                .build());

        // Weapon Attributes category
        gui.setSlot(24, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Text.literal("⚔ Weapon Attributes").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("⚔ Weapon attribute tokens").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openWeaponAttributesMenu(player))
                .build());

        // Row 3: Block Tools and Block Weapons - must use slots 0-26 for 9x3
        gui.setSlot(25, new GuiElementBuilder(Items.DIAMOND_PICKAXE)
                .setName(Text.literal("⛏ Block Tools").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("⛏ Block-themed tools").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openBlockToolsMenu(player, 0))
                .build());

        // Block Weapons category
        gui.setSlot(26, new GuiElementBuilder(Items.QUARTZ)
                .setName(Text.literal("⚔ Block Weapons").formatted(Formatting.WHITE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("⚔ Block-themed swords").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openBlockWeaponsMenu(player, 0))
                .build());

        gui.open();
    }

    // ============================================================
    // CATEGORY MENU METHODS - All custom items
    // ============================================================

    private static void openT1SwordsMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X4, player, false);
        gui.setTitle(Text.literal("⚔ T1 Bounty Swords"));

        // Background with dark theme
        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.IRON_SWORD)
                .setName(Text.literal("⚔ T1 Bounty Swords").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Tier 1 slayer swords").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Click any sword to view recipe").formatted(Formatting.GRAY))
                .glow().build());

        // Swords in a grid pattern
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23};
        int idx = 0;
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            if (idx >= slots.length) break;
            ItemStack sword = SlayerItems.createSlayerSword(type);
            gui.setSlot(slots[idx++], new GuiElementBuilder(sword)
                    .setName(sword.get(DataComponentTypes.CUSTOM_NAME))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("✨ Click to view recipe").formatted(Formatting.YELLOW))
                    .setCallback((i, clickType, action) -> showItemInfo(player, SlayerItems.createSlayerSword(type)))
                    .build());
        }

        // Back button
        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to Menu").formatted(Formatting.YELLOW))
                .setCallback((slot, clickType, action) -> openMainMenu(player))
                .build());

        gui.open();
    }

    // ============================================================
    // BOUNTY SETS MENU - Main menu with T1 and T2 choices
    // ============================================================
    private static void openSlayerSetsMenu(ServerPlayerEntity player, int page) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("⚔ Bounty Sets (T1 & T2)"));

        // Background with dark theme
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Text.literal("⚔ Bounty Sets (T1 & T2)").formatted(Formatting.DARK_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Bounty Hunter Equipment").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Select Tier to Browse").formatted(Formatting.YELLOW))
                .glow().build());

        // Define slayer sets for display with icons
        java.util.List<BountySetDisplay> sets = new java.util.ArrayList<>();
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            sets.add(new BountySetDisplay(type.icon, type.displayName, type.color,
                SlayerItems.createSlayerSword(type),
                SlayerItems.createSlayerHelmet(type, 1),
                SlayerItems.createSlayerChestplate(type, 1),
                SlayerItems.createSlayerLeggings(type, 1),
                SlayerItems.createSlayerBoots(type, 1),
                SlayerItems.createUpgradedSlayerSword(type),
                SlayerItems.createSlayerHelmet(type, 2),
                SlayerItems.createSlayerChestplate(type, 2),
                SlayerItems.createSlayerLeggings(type, 2),
                SlayerItems.createSlayerBoots(type, 2)));
        }

        // Display sets with T1 and T2 buttons side by side (2 rows per set)
        // Format: [Icon] [T1 Button] [separator] [T2 Button]
        int setsPerPage = 4;
        int totalPages = Math.max(1, (sets.size() + setsPerPage - 1) / setsPerPage);
        int clampedPage = Math.min(page, totalPages - 1);
        int startIdx = clampedPage * setsPerPage;
        int endIdx = Math.min(startIdx + setsPerPage, sets.size());

        for (int i = startIdx; i < endIdx; i++) {
            BountySetDisplay set = sets.get(i);
            int rowOffset = (i - startIdx) * 18; // 2 rows per set
            int baseSlot = 10 + rowOffset;
            
            // Row 1: Icon | T1 Helmet | T1 Chest | T1 Legs | T1 Boots | T1 Sword | T1 Label
            gui.setSlot(baseSlot, new GuiElementBuilder(set.icon)
                    .setName(Text.literal(set.name + " Set").formatted(set.color, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Slayer Type:").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal(set.name).formatted(set.color))
                    .glow()
                    .build());
            
            // Gap slot
            gui.setSlot(baseSlot + 1, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
            
            // T1 Armor pieces
            gui.setSlot(baseSlot + 2, createArmorElement(set.t1Helmet, player));
            gui.setSlot(baseSlot + 3, createArmorElement(set.t1Chestplate, player));
            gui.setSlot(baseSlot + 4, createArmorElement(set.t1Leggings, player));
            gui.setSlot(baseSlot + 5, createArmorElement(set.t1Boots, player));
            
            // T1 Sword
            gui.setSlot(baseSlot + 6, new GuiElementBuilder(set.t1Sword)
                    .setName(set.t1Sword.get(DataComponentTypes.CUSTOM_NAME))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("✨ T1 Sword - Click for recipe").formatted(Formatting.YELLOW))
                    .setCallback((idx, type, action) -> showItemInfo(player, set.t1Sword))
                    .build());
            
            // T1 Label button (opens T1 submenu)
            final String setName = set.name;
            gui.setSlot(baseSlot + 7, new GuiElementBuilder(Items.IRON_BLOCK)
                    .setName(Text.literal("T1 (Iron)").formatted(Formatting.WHITE, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Browse T1 " + setName).formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("✨ Click to view T1 set").formatted(Formatting.YELLOW))
                    .setCallback((idx, type, action) -> openT1BountySetMenu(player, setName))
                    .build());
            
            // Row 2: Gap | T2 Helmet | T2 Chest | T2 Legs | T2 Boots | T2 Sword | T2 Label
            int row2Base = baseSlot + 9;
            gui.setSlot(row2Base, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
            
            // T2 Armor pieces
            gui.setSlot(row2Base + 1, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
            gui.setSlot(row2Base + 2, createArmorElement(set.t2Helmet, player));
            gui.setSlot(row2Base + 3, createArmorElement(set.t2Chestplate, player));
            gui.setSlot(row2Base + 4, createArmorElement(set.t2Leggings, player));
            gui.setSlot(row2Base + 5, createArmorElement(set.t2Boots, player));
            
            // T2 Sword
            gui.setSlot(row2Base + 6, new GuiElementBuilder(set.t2Sword)
                    .setName(set.t2Sword.get(DataComponentTypes.CUSTOM_NAME))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("✨ T2 Sword - Click for recipe").formatted(Formatting.AQUA))
                    .setCallback((idx, type, action) -> showItemInfo(player, set.t2Sword))
                    .build());
            
            // T2 Label button (opens T2 submenu)
            gui.setSlot(row2Base + 7, new GuiElementBuilder(Items.DIAMOND_BLOCK)
                    .setName(Text.literal("T2 (Diamond)").formatted(Formatting.AQUA, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Browse T2 " + setName).formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("✨ Click to view T2 set").formatted(Formatting.YELLOW))
                    .setCallback((idx, type, action) -> openT2BountySetMenu(player, setName))
                    .build());
            
            // Separator row with set name
            int row3Base = baseSlot + 18;
            Text separator = Text.literal("━━ " + set.name + " Set ━━").formatted(Formatting.DARK_GRAY);
            for (int sep = 0; sep < 9; sep++) {
                gui.setSlot(row3Base + sep, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                        .setName(separator)
                        .build());
            }
        }

        // Back button
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to Menu").formatted(Formatting.YELLOW))
                .setCallback((slot, type, action) -> openMainMenu(player))
                .build());

        // Pagination
        if (clampedPage > 0) {
            final int prev = clampedPage - 1;
            gui.setSlot(46, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("← Previous Page").formatted(Formatting.YELLOW))
                    .addLoreLine(Text.literal("Page " + (clampedPage) + " / " + totalPages))
                    .setCallback((slot, type, action) -> openSlayerSetsMenu(player, prev))
                    .build());
        }
        if (clampedPage < totalPages - 1) {
            final int next = clampedPage + 1;
            gui.setSlot(53, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("Next Page →").formatted(Formatting.YELLOW))
                    .addLoreLine(Text.literal("Page " + (clampedPage + 2) + " / " + totalPages))
                    .setCallback((slot, type, action) -> openSlayerSetsMenu(player, next))
                    .build());
        }

        gui.setSlot(49, new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal("Page " + (clampedPage + 1) + " / " + totalPages).formatted(Formatting.WHITE))
                .build());

        gui.open();
    }
    
    // Helper class for bounty set display
    private static class BountySetDisplay {
        final net.minecraft.item.Item icon;
        final String name;
        final Formatting color;
        final ItemStack t1Sword, t1Helmet, t1Chestplate, t1Leggings, t1Boots;
        final ItemStack t2Sword, t2Helmet, t2Chestplate, t2Leggings, t2Boots;
        
        BountySetDisplay(net.minecraft.item.Item icon, String name, Formatting color,
                        ItemStack t1Sword, ItemStack t1Helmet, ItemStack t1Chestplate, ItemStack t1Leggings, ItemStack t1Boots,
                        ItemStack t2Sword, ItemStack t2Helmet, ItemStack t2Chestplate, ItemStack t2Leggings, ItemStack t2Boots) {
            this.icon = icon;
            this.name = name;
            this.color = color;
            this.t1Sword = t1Sword;
            this.t1Helmet = t1Helmet;
            this.t1Chestplate = t1Chestplate;
            this.t1Leggings = t1Leggings;
            this.t1Boots = t1Boots;
            this.t2Sword = t2Sword;
            this.t2Helmet = t2Helmet;
            this.t2Chestplate = t2Chestplate;
            this.t2Leggings = t2Leggings;
            this.t2Boots = t2Boots;
        }
    }
    
    // T1 Bounty Set Submenu
    private static void openT1BountySetMenu(ServerPlayerEntity player, String setName) {
        SlayerManager.SlayerType slayerType = null;
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            if (type.displayName.equals(setName)) {
                slayerType = type;
                break;
            }
        }
        if (slayerType == null) return;
        
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
        gui.setTitle(Text.literal("🛡 T1 " + setName + " Set"));
        
        // Background
        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }
        
        // Header
        gui.setSlot(4, new GuiElementBuilder(slayerType.icon)
                .setName(Text.literal("T1 " + setName + " Set").formatted(slayerType.color, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Tier 1 (Iron) Bounty Equipment").formatted(Formatting.GRAY))
                .glow().build());
        
        // T1 items in a row
        gui.setSlot(10, createArmorElement(SlayerItems.createSlayerHelmet(slayerType, 1), player));
        gui.setSlot(11, createArmorElement(SlayerItems.createSlayerChestplate(slayerType, 1), player));
        gui.setSlot(12, createArmorElement(SlayerItems.createSlayerLeggings(slayerType, 1), player));
        gui.setSlot(13, createArmorElement(SlayerItems.createSlayerBoots(slayerType, 1), player));
        
        ItemStack t1Sword = SlayerItems.createSlayerSword(slayerType);
        gui.setSlot(14, new GuiElementBuilder(t1Sword)
                .setName(t1Sword.get(DataComponentTypes.CUSTOM_NAME))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("✨ T1 Sword").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> showItemInfo(player, t1Sword))
                .build());
        
        // Back button
        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to Bounty Sets").formatted(Formatting.YELLOW))
                .setCallback((slot, type, action) -> openSlayerSetsMenu(player, 0))
                .build());
        
        gui.open();
    }
    
    // T2 Bounty Set Submenu
    private static void openT2BountySetMenu(ServerPlayerEntity player, String setName) {
        SlayerManager.SlayerType slayerType = null;
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            if (type.displayName.equals(setName)) {
                slayerType = type;
                break;
            }
        }
        if (slayerType == null) return;
        
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
        gui.setTitle(Text.literal("🛡 T2 " + setName + " Set"));
        
        // Background
        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }
        
        // Header
        gui.setSlot(4, new GuiElementBuilder(slayerType.icon)
                .setName(Text.literal("T2 " + setName + " Set").formatted(slayerType.color, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Tier 2 (Diamond) Bounty Equipment").formatted(Formatting.GRAY))
                .glow().build());
        
        // T2 items in a row
        gui.setSlot(10, createArmorElement(SlayerItems.createSlayerHelmet(slayerType, 2), player));
        gui.setSlot(11, createArmorElement(SlayerItems.createSlayerChestplate(slayerType, 2), player));
        gui.setSlot(12, createArmorElement(SlayerItems.createSlayerLeggings(slayerType, 2), player));
        gui.setSlot(13, createArmorElement(SlayerItems.createSlayerBoots(slayerType, 2), player));
        
        ItemStack t2Sword = SlayerItems.createUpgradedSlayerSword(slayerType);
        gui.setSlot(14, new GuiElementBuilder(t2Sword)
                .setName(t2Sword.get(DataComponentTypes.CUSTOM_NAME))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("✨ T2 Sword").formatted(Formatting.AQUA))
                .setCallback((idx, type, action) -> showItemInfo(player, t2Sword))
                .build());
        
        // Back button
        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to Bounty Sets").formatted(Formatting.YELLOW))
                .setCallback((slot, type, action) -> openSlayerSetsMenu(player, 0))
                .build());
        
        gui.open();
    }

    // ============================================================
    // GEM & BLOCK ARMOR MENU - Organized sets with matching weapons
    // ============================================================
    private static void openGemAndBlockArmorMenu(ServerPlayerEntity player, int page) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("💎 Custom Armor Sets"));

        // Background with dark theme
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.EMERALD)
                .setName(Text.literal("💎 Custom Armor Sets").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Block → Armor Set → Weapon").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Complete sets with matching weapons!").formatted(Formatting.YELLOW))
                .glow().build());

        // Define armor sets: Block item, Armor pieces, Weapon
        java.util.List<ArmorSetDisplay> sets = new java.util.ArrayList<>();
        
        // Gem Armors
        sets.add(new ArmorSetDisplay(Items.EMERALD_BLOCK, "Emerald", 
            GemGear.createEmeraldSword(), GemGear.createEmeraldHelmet(), GemGear.createEmeraldChestplate(), 
            GemGear.createEmeraldLeggings(), GemGear.createEmeraldBoots()));
        sets.add(new ArmorSetDisplay(Items.LAPIS_BLOCK, "Lapis",
            GemGear.createLapisSword(), GemGear.createLapisHelmet(), GemGear.createLapisChestplate(),
            GemGear.createLapisLeggings(), GemGear.createLapisBoots()));
        
        // Block Armors with matching weapons
        sets.add(new ArmorSetDisplay(Items.GLASS, "Glass",
            BlockWeapons.createGlassSword(), BlockArmor.createGlassHelmet(), BlockArmor.createGlassChestplate(),
            BlockArmor.createGlassLeggings(), BlockArmor.createGlassBoots()));
        sets.add(new ArmorSetDisplay(Items.OBSIDIAN, "Obsidian",
            BlockWeapons.createObsidianSword(), BlockArmor.createObsidianHelmet(), BlockArmor.createObsidianChestplate(),
            BlockArmor.createObsidianLeggings(), BlockArmor.createObsidianBoots()));
        sets.add(new ArmorSetDisplay(Items.QUARTZ_BLOCK, "Quartz",
            BlockWeapons.createQuartzSword(), BlockArmor.createQuartzHelmet(), BlockArmor.createQuartzChestplate(),
            BlockArmor.createQuartzLeggings(), BlockArmor.createQuartzBoots()));
        sets.add(new ArmorSetDisplay(Items.GLOWSTONE, "Glowstone",
            BlockWeapons.createGlowstoneSword(), BlockArmor.createGlowstoneHelmet(), BlockArmor.createGlowstoneChestplate(),
            BlockArmor.createGlowstoneLeggings(), BlockArmor.createGlowstoneBoots()));
        sets.add(new ArmorSetDisplay(Items.REDSTONE_BLOCK, "Redstone",
            BlockWeapons.createRedstoneSword(), BlockArmor.createRedstoneHelmet(), BlockArmor.createRedstoneChestplate(),
            BlockArmor.createRedstoneLeggings(), BlockArmor.createRedstoneBoots()));
        sets.add(new ArmorSetDisplay(Items.NETHERRACK, "Netherrack",
            BlockWeapons.createNetherrackSword(), BlockArmor.createNetherrackHelmet(), BlockArmor.createNetherrackChestplate(),
            BlockArmor.createNetherrackLeggings(), BlockArmor.createNetherrackBoots()));
        sets.add(new ArmorSetDisplay(Items.END_STONE, "End Stone",
            BlockWeapons.createEndstoneSword(), BlockArmor.createEndstoneHelmet(), BlockArmor.createEndstoneChestplate(),
            BlockArmor.createEndstoneLeggings(), BlockArmor.createEndstoneBoots()));
        sets.add(new ArmorSetDisplay(Items.PACKED_ICE, "Ice",
            BlockWeapons.createIceSword(), BlockArmor.createIceHelmet(), BlockArmor.createIceChestplate(),
            BlockArmor.createIceLeggings(), BlockArmor.createIceBoots()));
        sets.add(new ArmorSetDisplay(Items.PRISMARINE, "Prismarine",
            BlockWeapons.createPrismarineSword(), BlockArmor.createPrismarineHelmet(), BlockArmor.createPrismarineChestplate(),
            BlockArmor.createPrismarineLeggings(), BlockArmor.createPrismarineBoots()));
        sets.add(new ArmorSetDisplay(Items.TERRACOTTA, "Terracotta",
            BlockWeapons.createTerracottaSword(), BlockArmor.createTerracottaHelmet(), BlockArmor.createTerracottaChestplate(),
            BlockArmor.createTerracottaLeggings(), BlockArmor.createTerracottaBoots()));
        sets.add(new ArmorSetDisplay(Items.MOSSY_COBBLESTONE, "Mossy",
            BlockWeapons.createMossySword(), BlockArmor.createMossyHelmet(), BlockArmor.createMossyChestplate(),
            BlockArmor.createMossyLeggings(), BlockArmor.createMossyBoots()));
        sets.add(new ArmorSetDisplay(Items.SOUL_SAND, "Soul Sand",
            BlockWeapons.createSoulSandSword(), BlockArmor.createSoulSandHelmet(), BlockArmor.createSoulSandChestplate(),
            BlockArmor.createSoulSandLeggings(), BlockArmor.createSoulSandBoots()));
        sets.add(new ArmorSetDisplay(Items.MAGMA_BLOCK, "Magma",
            BlockWeapons.createMagmaSword(), BlockArmor.createMagmaHelmet(), BlockArmor.createMagmaChestplate(),
            BlockArmor.createMagmaLeggings(), BlockArmor.createMagmaBoots()));
        sets.add(new ArmorSetDisplay(Items.SANDSTONE, "Sandstone",
            BlockWeapons.createSandstoneSword(), BlockArmor.createSandstoneHelmet(), BlockArmor.createSandstoneChestplate(),
            BlockArmor.createSandstoneLeggings(), BlockArmor.createSandstoneBoots()));
        sets.add(new ArmorSetDisplay(Items.AMETHYST_BLOCK, "Amethyst",
            BlockWeapons.createAmethystSword(), BlockArmor.createAmethystHelmet(), BlockArmor.createAmethystChestplate(),
            BlockArmor.createAmethystLeggings(), BlockArmor.createAmethystBoots()));
        sets.add(new ArmorSetDisplay(Items.COAL_BLOCK, "Coal",
            BlockWeapons.createCoalSword(), BlockArmor.createCoalHelmet(), BlockArmor.createCoalChestplate(),
            BlockArmor.createCoalLeggings(), BlockArmor.createCoalBoots()));

        // 36+ NEW BLOCK ARMOR SETS
        sets.add(new ArmorSetDisplay(Items.DIAMOND_BLOCK, "Diamond Block",
            BlockWeapons.createDiamondBlockSword(), BlockArmor.createDiamondBlockHelmet(), BlockArmor.createDiamondBlockChestplate(),
            BlockArmor.createDiamondBlockLeggings(), BlockArmor.createDiamondBlockBoots()));
        sets.add(new ArmorSetDisplay(Items.EMERALD_BLOCK, "Emerald Block",
            BlockWeapons.createEmeraldBlockSword(), BlockArmor.createEmeraldBlockHelmet(), BlockArmor.createEmeraldBlockChestplate(),
            BlockArmor.createEmeraldBlockLeggings(), BlockArmor.createEmeraldBlockBoots()));
        sets.add(new ArmorSetDisplay(Items.GOLD_BLOCK, "Gold Block",
            BlockWeapons.createGoldBlockSword(), BlockArmor.createGoldBlockHelmet(), BlockArmor.createGoldBlockChestplate(),
            BlockArmor.createGoldBlockLeggings(), BlockArmor.createGoldBlockBoots()));
        sets.add(new ArmorSetDisplay(Items.IRON_BLOCK, "Iron Block",
            BlockWeapons.createIronBlockSword(), BlockArmor.createIronBlockHelmet(), BlockArmor.createIronBlockChestplate(),
            BlockArmor.createIronBlockLeggings(), BlockArmor.createIronBlockBoots()));
        sets.add(new ArmorSetDisplay(Items.LAPIS_BLOCK, "Lapis Block",
            BlockWeapons.createLapisBlockSword(), BlockArmor.createLapisBlockHelmet(), BlockArmor.createLapisBlockChestplate(),
            BlockArmor.createLapisBlockLeggings(), BlockArmor.createLapisBlockBoots()));
        sets.add(new ArmorSetDisplay(Items.COPPER_BLOCK, "Copper Block",
            BlockWeapons.createCopperBlockSword(), BlockArmor.createCopperBlockHelmet(), BlockArmor.createCopperBlockChestplate(),
            BlockArmor.createCopperBlockLeggings(), BlockArmor.createCopperBlockBoots()));
        sets.add(new ArmorSetDisplay(Items.ANCIENT_DEBRIS, "Ancient Debris",
            BlockWeapons.createAncientDebrisSword(), BlockArmor.createAncientDebrisHelmet(), BlockArmor.createAncientDebrisChestplate(),
            BlockArmor.createAncientDebrisLeggings(), BlockArmor.createAncientDebrisBoots()));
        sets.add(new ArmorSetDisplay(Items.BASALT, "Basalt",
            BlockWeapons.createBasaltSword(), BlockArmor.createBasaltHelmet(), BlockArmor.createBasaltChestplate(),
            BlockArmor.createBasaltLeggings(), BlockArmor.createBasaltBoots()));
        sets.add(new ArmorSetDisplay(Items.BLACKSTONE, "Blackstone",
            BlockWeapons.createBlackstoneSword(), BlockArmor.createBlackstoneHelmet(), BlockArmor.createBlackstoneChestplate(),
            BlockArmor.createBlackstoneLeggings(), BlockArmor.createBlackstoneBoots()));
        sets.add(new ArmorSetDisplay(Items.BONE_BLOCK, "Bone Block",
            BlockWeapons.createBoneBlockSword(), BlockArmor.createBoneBlockHelmet(), BlockArmor.createBoneBlockChestplate(),
            BlockArmor.createBoneBlockLeggings(), BlockArmor.createBoneBlockBoots()));
        sets.add(new ArmorSetDisplay(Items.BRICKS, "Brick",
            BlockWeapons.createBrickSword(), BlockArmor.createBrickHelmet(), BlockArmor.createBrickChestplate(),
            BlockArmor.createBrickLeggings(), BlockArmor.createBrickBoots()));
        sets.add(new ArmorSetDisplay(Items.CACTUS, "Cactus",
            BlockWeapons.createCactusSword(), BlockArmor.createCactusHelmet(), BlockArmor.createCactusChestplate(),
            BlockArmor.createCactusLeggings(), BlockArmor.createCactusBoots()));
        sets.add(new ArmorSetDisplay(Items.CALCITE, "Calcite",
            BlockWeapons.createCalciteSword(), BlockArmor.createCalciteHelmet(), BlockArmor.createCalciteChestplate(),
            BlockArmor.createCalciteLeggings(), BlockArmor.createCalciteBoots()));
        sets.add(new ArmorSetDisplay(Items.DEEPSLATE, "Deepslate",
            BlockWeapons.createDeepslateSword(), BlockArmor.createDeepslateHelmet(), BlockArmor.createDeepslateChestplate(),
            BlockArmor.createDeepslateLeggings(), BlockArmor.createDeepslateBoots()));
        sets.add(new ArmorSetDisplay(Items.DRIPSTONE_BLOCK, "Dripstone",
            BlockWeapons.createDripstoneSword(), BlockArmor.createDripstoneHelmet(), BlockArmor.createDripstoneChestplate(),
            BlockArmor.createDripstoneLeggings(), BlockArmor.createDripstoneBoots()));
        sets.add(new ArmorSetDisplay(Items.HAY_BLOCK, "Hay",
            BlockWeapons.createHaySword(), BlockArmor.createHayHelmet(), BlockArmor.createHayChestplate(),
            BlockArmor.createHayLeggings(), BlockArmor.createHayBoots()));
        sets.add(new ArmorSetDisplay(Items.HONEYCOMB_BLOCK, "Honeycomb",
            BlockWeapons.createHoneycombSword(), BlockArmor.createHoneycombHelmet(), BlockArmor.createHoneycombChestplate(),
            BlockArmor.createHoneycombLeggings(), BlockArmor.createHoneycombBoots()));
        sets.add(new ArmorSetDisplay(Items.LILY_PAD, "Lily Pad",
            BlockWeapons.createLilyPadSword(), BlockArmor.createLilyPadHelmet(), BlockArmor.createLilyPadChestplate(),
            BlockArmor.createLilyPadLeggings(), BlockArmor.createLilyPadBoots()));
        sets.add(new ArmorSetDisplay(Items.MELON, "Melon",
            BlockWeapons.createMelonSword(), BlockArmor.createMelonHelmet(), BlockArmor.createMelonChestplate(),
            BlockArmor.createMelonLeggings(), BlockArmor.createMelonBoots()));
        sets.add(new ArmorSetDisplay(Items.MOSS_BLOCK, "Moss Block",
            BlockWeapons.createMossBlockSword(), BlockArmor.createMossBlockHelmet(), BlockArmor.createMossBlockChestplate(),
            BlockArmor.createMossBlockLeggings(), BlockArmor.createMossBlockBoots()));
        sets.add(new ArmorSetDisplay(Items.MYCELIUM, "Mycelium",
            BlockWeapons.createMyceliumSword(), BlockArmor.createMyceliumHelmet(), BlockArmor.createMyceliumChestplate(),
            BlockArmor.createMyceliumLeggings(), BlockArmor.createMyceliumBoots()));
        sets.add(new ArmorSetDisplay(Items.NETHER_BRICKS, "Nether Brick",
            BlockWeapons.createNetherBrickSword(), BlockArmor.createNetherBrickHelmet(), BlockArmor.createNetherBrickChestplate(),
            BlockArmor.createNetherBrickLeggings(), BlockArmor.createNetherBrickBoots()));
        sets.add(new ArmorSetDisplay(Items.PUMPKIN, "Pumpkin",
            BlockWeapons.createPumpkinSword(), BlockArmor.createPumpkinHelmet(), BlockArmor.createPumpkinChestplate(),
            BlockArmor.createPumpkinLeggings(), BlockArmor.createPumpkinBoots()));
        sets.add(new ArmorSetDisplay(Items.PURPUR_BLOCK, "Purpur",
            BlockWeapons.createPurpurSword(), BlockArmor.createPurpurHelmet(), BlockArmor.createPurpurChestplate(),
            BlockArmor.createPurpurLeggings(), BlockArmor.createPurpurBoots()));
        sets.add(new ArmorSetDisplay(Items.SAND, "Sand",
            BlockWeapons.createSandSword(), BlockArmor.createSandHelmet(), BlockArmor.createSandChestplate(),
            BlockArmor.createSandLeggings(), BlockArmor.createSandBoots()));
        sets.add(new ArmorSetDisplay(Items.SCULK, "Sculk",
            BlockWeapons.createSculkSword(), BlockArmor.createSculkHelmet(), BlockArmor.createSculkChestplate(),
            BlockArmor.createSculkLeggings(), BlockArmor.createSculkBoots()));
        sets.add(new ArmorSetDisplay(Items.SHROOMLIGHT, "Shroomlight",
            BlockWeapons.createShroomlightSword(), BlockArmor.createShroomlightHelmet(), BlockArmor.createShroomlightChestplate(),
            BlockArmor.createShroomlightLeggings(), BlockArmor.createShroomlightBoots()));
        sets.add(new ArmorSetDisplay(Items.SLIME_BLOCK, "Slime",
            BlockWeapons.createSlimeSword(), BlockArmor.createSlimeHelmet(), BlockArmor.createSlimeChestplate(),
            BlockArmor.createSlimeLeggings(), BlockArmor.createSlimeBoots()));
        sets.add(new ArmorSetDisplay(Items.SMOOTH_STONE, "Smooth Stone",
            BlockWeapons.createSmoothStoneSword(), BlockArmor.createSmoothStoneHelmet(), BlockArmor.createSmoothStoneChestplate(),
            BlockArmor.createSmoothStoneLeggings(), BlockArmor.createSmoothStoneBoots()));
        sets.add(new ArmorSetDisplay(Items.SNOW_BLOCK, "Snow",
            BlockWeapons.createSnowSword(), BlockArmor.createSnowHelmet(), BlockArmor.createSnowChestplate(),
            BlockArmor.createSnowLeggings(), BlockArmor.createSnowBoots()));
        sets.add(new ArmorSetDisplay(Items.SOUL_SOIL, "Soul Soil",
            BlockWeapons.createSoulSoilSword(), BlockArmor.createSoulSoilHelmet(), BlockArmor.createSoulSoilChestplate(),
            BlockArmor.createSoulSoilLeggings(), BlockArmor.createSoulSoilBoots()));
        sets.add(new ArmorSetDisplay(Items.SPONGE, "Sponge",
            BlockWeapons.createSpongeSword(), BlockArmor.createSpongeHelmet(), BlockArmor.createSpongeChestplate(),
            BlockArmor.createSpongeLeggings(), BlockArmor.createSpongeBoots()));
        sets.add(new ArmorSetDisplay(Items.TARGET, "Target",
            BlockWeapons.createTargetSword(), BlockArmor.createTargetHelmet(), BlockArmor.createTargetChestplate(),
            BlockArmor.createTargetLeggings(), BlockArmor.createTargetBoots()));
        sets.add(new ArmorSetDisplay(Items.TNT, "TNT",
            BlockWeapons.createTntSword(), BlockArmor.createTntHelmet(), BlockArmor.createTntChestplate(),
            BlockArmor.createTntLeggings(), BlockArmor.createTntBoots()));
        sets.add(new ArmorSetDisplay(Items.WARPED_STEM, "Warped",
            BlockWeapons.createWarpedSword(), BlockArmor.createWarpedHelmet(), BlockArmor.createWarpedChestplate(),
            BlockArmor.createWarpedLeggings(), BlockArmor.createWarpedBoots()));
        sets.add(new ArmorSetDisplay(Items.WET_SPONGE, "Wet Sponge",
            BlockWeapons.createWetSpongeSword(), BlockArmor.createWetSpongeHelmet(), BlockArmor.createWetSpongeChestplate(),
            BlockArmor.createWetSpongeLeggings(), BlockArmor.createWetSpongeBoots()));

        // Pagination - 3 sets per page (each set takes 2 rows)
        int setsPerPage = 3;
        int totalPages = Math.max(1, (sets.size() + setsPerPage - 1) / setsPerPage);
        int clampedPage = Math.min(page, totalPages - 1);
        int startIdx = clampedPage * setsPerPage;
        int endIdx = Math.min(startIdx + setsPerPage, sets.size());

        // Display sets in organized rows
        for (int i = startIdx; i < endIdx; i++) {
            ArmorSetDisplay set = sets.get(i);
            int rowOffset = (i - startIdx) * 18; // 2 rows per set
            int baseSlot = 10 + rowOffset;
            
            // Row 1: Block | (gap) | Helmet | Chestplate | Leggings | Boots | Weapon
            // Row 2: Set Name separator
            
            // Block material (left side)
            gui.setSlot(baseSlot, new GuiElementBuilder(set.blockItem)
                    .setName(Text.literal(set.name + " Set").formatted(Formatting.YELLOW, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Material:").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal(set.name).formatted(Formatting.WHITE))
                    .glow()
                    .build());
            
            // Gap slot (shifted armor 1 to the right)
            gui.setSlot(baseSlot + 1, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
            
            // Armor pieces (middle - shifted 1 slot right)
            gui.setSlot(baseSlot + 2, createArmorElement(set.helmet, player));
            gui.setSlot(baseSlot + 3, createArmorElement(set.chestplate, player));
            gui.setSlot(baseSlot + 4, createArmorElement(set.leggings, player));
            gui.setSlot(baseSlot + 5, createArmorElement(set.boots, player));
            
            // Weapon (right side)
            gui.setSlot(baseSlot + 6, new GuiElementBuilder(set.sword)
                    .setName(set.sword.get(DataComponentTypes.CUSTOM_NAME))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("✨ Click for recipe").formatted(Formatting.YELLOW))
                    .setCallback((idx, type, action) -> showItemInfo(player, set.sword))
                    .build());
            
            // Add separator lore below
            Text setName = Text.literal("━━ " + set.name + " Set ━━").formatted(Formatting.DARK_GRAY);
            for (int sep = baseSlot + 9; sep < baseSlot + 15 && sep < 54; sep++) {
                if (sep < 45) {
                    gui.setSlot(sep, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                            .setName(setName)
                            .build());
                }
            }
        }

        // Back button
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to Menu").formatted(Formatting.YELLOW))
                .setCallback((slot, type, action) -> openMainMenu(player))
                .build());

        // Pagination
        if (clampedPage > 0) {
            final int prev = clampedPage - 1;
            gui.setSlot(46, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("← Previous Page").formatted(Formatting.YELLOW))
                    .addLoreLine(Text.literal("Page " + (clampedPage) + " / " + totalPages))
                    .setCallback((slot, type, action) -> openGemAndBlockArmorMenu(player, prev))
                    .build());
        }
        if (clampedPage < totalPages - 1) {
            final int next = clampedPage + 1;
            gui.setSlot(53, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("Next Page →").formatted(Formatting.YELLOW))
                    .addLoreLine(Text.literal("Page " + (clampedPage + 2) + " / " + totalPages))
                    .setCallback((slot, type, action) -> openGemAndBlockArmorMenu(player, next))
                    .build());
        }

        gui.setSlot(49, new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal("Page " + (clampedPage + 1) + " / " + totalPages).formatted(Formatting.WHITE))
                .build());

        gui.open();
    }
    
    private static GuiElementBuilder createArmorElement(ItemStack armor, ServerPlayerEntity player) {
        Text name = armor.get(DataComponentTypes.CUSTOM_NAME);
        return new GuiElementBuilder(armor)
                .setName(name)
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("✨ Click for recipe").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> showItemInfo(player, armor));
    }
    
    // Helper class for organizing armor set display
    private static class ArmorSetDisplay {
        final net.minecraft.item.Item blockItem;
        final String name;
        final ItemStack sword, helmet, chestplate, leggings, boots;
        
        ArmorSetDisplay(net.minecraft.item.Item blockItem, String name,
                        ItemStack sword, ItemStack helmet, ItemStack chestplate, ItemStack leggings, ItemStack boots) {
            this.blockItem = blockItem;
            this.name = name;
            this.sword = sword;
            this.helmet = helmet;
            this.chestplate = chestplate;
            this.leggings = leggings;
            this.boots = boots;
        }
    }

    // Helper class for organizing tool set display
    private static class ToolSetDisplay {
        final net.minecraft.item.Item blockItem;
        final String name;
        final Formatting color;
        final ItemStack pickaxe, axe, shovel, hoe;
        
        ToolSetDisplay(net.minecraft.item.Item blockItem, String name,
                       ItemStack pickaxe, ItemStack axe, ItemStack shovel, ItemStack hoe) {
            this.blockItem = blockItem;
            this.name = name;
            this.color = Formatting.WHITE;
            this.pickaxe = pickaxe;
            this.axe = axe;
            this.shovel = shovel;
            this.hoe = hoe;
        }
        
        ToolSetDisplay(net.minecraft.item.Item blockItem, String name, Formatting color,
                       ItemStack pickaxe, ItemStack axe, ItemStack shovel, ItemStack hoe) {
            this.blockItem = blockItem;
            this.name = name;
            this.color = color;
            this.pickaxe = pickaxe;
            this.axe = axe;
            this.shovel = shovel;
            this.hoe = hoe;
        }
    }

    // ============================================================
    // BLOCK TOOLS MENU
    // ============================================================

    private static void openBlockToolsMenu(ServerPlayerEntity player, int page) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("⛏ Block Tools"));

        // Background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.DIAMOND_PICKAXE)
                .setName(Text.literal("⛏ Block Tools").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Block-themed tools with special abilities").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Click any tool to view recipe").formatted(Formatting.GRAY))
                .glow().build());

        // Build tool sets list
        List<ToolSetDisplay> sets = new ArrayList<>();
        sets.add(new ToolSetDisplay(Items.GLASS, "Glass",
            BlockTools.createGlassPickaxe(), BlockTools.createGlassAxe(), BlockTools.createGlassShovel(), BlockTools.createGlassHoe()));
        sets.add(new ToolSetDisplay(Items.OBSIDIAN, "Obsidian",
            BlockTools.createObsidianPickaxe(), BlockTools.createObsidianAxe(), BlockTools.createObsidianShovel(), BlockTools.createObsidianHoe()));
        sets.add(new ToolSetDisplay(Items.QUARTZ_BLOCK, "Quartz",
            BlockTools.createQuartzPickaxe(), BlockTools.createQuartzAxe(), BlockTools.createQuartzShovel(), BlockTools.createQuartzHoe()));
        sets.add(new ToolSetDisplay(Items.GLOWSTONE, "Glowstone",
            BlockTools.createGlowstonePickaxe(), BlockTools.createGlowstoneAxe(), BlockTools.createGlowstoneShovel(), BlockTools.createGlowstoneHoe()));
        sets.add(new ToolSetDisplay(Items.REDSTONE_BLOCK, "Redstone",
            BlockTools.createRedstonePickaxe(), BlockTools.createRedstoneAxe(), BlockTools.createRedstoneShovel(), BlockTools.createRedstoneHoe()));
        sets.add(new ToolSetDisplay(Items.NETHERRACK, "Netherrack",
            BlockTools.createNetherrackPickaxe(), BlockTools.createNetherrackAxe(), BlockTools.createNetherrackShovel(), BlockTools.createNetherrackHoe()));
        sets.add(new ToolSetDisplay(Items.END_STONE, "End Stone",
            BlockTools.createEndstonePickaxe(), BlockTools.createEndstoneAxe(), BlockTools.createEndstoneShovel(), BlockTools.createEndstoneHoe()));
        sets.add(new ToolSetDisplay(Items.PACKED_ICE, "Ice",
            BlockTools.createIcePickaxe(), BlockTools.createIceAxe(), BlockTools.createIceShovel(), BlockTools.createIceHoe()));
        sets.add(new ToolSetDisplay(Items.PRISMARINE, "Prismarine",
            BlockTools.createPrismarinePickaxe(), BlockTools.createPrismarineAxe(), BlockTools.createPrismarineShovel(), BlockTools.createPrismarineHoe()));
        sets.add(new ToolSetDisplay(Items.TERRACOTTA, "Terracotta",
            BlockTools.createTerracottaPickaxe(), BlockTools.createTerracottaAxe(), BlockTools.createTerracottaShovel(), BlockTools.createTerracottaHoe()));
        sets.add(new ToolSetDisplay(Items.MOSSY_COBBLESTONE, "Mossy",
            BlockTools.createMossyPickaxe(), BlockTools.createMossyAxe(), BlockTools.createMossyShovel(), BlockTools.createMossyHoe()));
        sets.add(new ToolSetDisplay(Items.SOUL_SAND, "Soul Sand",
            BlockTools.createSoulSandPickaxe(), BlockTools.createSoulSandAxe(), BlockTools.createSoulSandShovel(), BlockTools.createSoulSandHoe()));
        sets.add(new ToolSetDisplay(Items.MAGMA_BLOCK, "Magma",
            BlockTools.createMagmaPickaxe(), BlockTools.createMagmaAxe(), BlockTools.createMagmaShovel(), BlockTools.createMagmaHoe()));
        sets.add(new ToolSetDisplay(Items.SANDSTONE, "Sandstone",
            BlockTools.createSandstonePickaxe(), BlockTools.createSandstoneAxe(), BlockTools.createSandstoneShovel(), BlockTools.createSandstoneHoe()));
        sets.add(new ToolSetDisplay(Items.AMETHYST_BLOCK, "Amethyst",
            BlockTools.createAmethystPickaxe(), BlockTools.createAmethystAxe(), BlockTools.createAmethystShovel(), BlockTools.createAmethystHoe()));
        sets.add(new ToolSetDisplay(Items.COAL_BLOCK, "Coal",
            BlockTools.createCoalPickaxe(), BlockTools.createCoalAxe(), BlockTools.createCoalShovel(), BlockTools.createCoalHoe()));

        // 36+ NEW BLOCK TOOL SETS
        sets.add(new ToolSetDisplay(Items.DIAMOND_BLOCK, "Diamond Block",
            BlockTools.createDiamondBlockPickaxe(), BlockTools.createDiamondBlockAxe(), BlockTools.createDiamondBlockShovel(), BlockTools.createDiamondBlockHoe()));
        sets.add(new ToolSetDisplay(Items.EMERALD_BLOCK, "Emerald Block",
            BlockTools.createEmeraldBlockPickaxe(), BlockTools.createEmeraldBlockAxe(), BlockTools.createEmeraldBlockShovel(), BlockTools.createEmeraldBlockHoe()));
        sets.add(new ToolSetDisplay(Items.GOLD_BLOCK, "Gold Block",
            BlockTools.createGoldBlockPickaxe(), BlockTools.createGoldBlockAxe(), BlockTools.createGoldBlockShovel(), BlockTools.createGoldBlockHoe()));
        sets.add(new ToolSetDisplay(Items.IRON_BLOCK, "Iron Block",
            BlockTools.createIronBlockPickaxe(), BlockTools.createIronBlockAxe(), BlockTools.createIronBlockShovel(), BlockTools.createIronBlockHoe()));
        sets.add(new ToolSetDisplay(Items.LAPIS_BLOCK, "Lapis Block",
            BlockTools.createLapisBlockPickaxe(), BlockTools.createLapisBlockAxe(), BlockTools.createLapisBlockShovel(), BlockTools.createLapisBlockHoe()));
        sets.add(new ToolSetDisplay(Items.COPPER_BLOCK, "Copper Block",
            BlockTools.createCopperBlockPickaxe(), BlockTools.createCopperBlockAxe(), BlockTools.createCopperBlockShovel(), BlockTools.createCopperBlockHoe()));
        sets.add(new ToolSetDisplay(Items.ANCIENT_DEBRIS, "Ancient Debris",
            BlockTools.createAncientDebrisPickaxe(), BlockTools.createAncientDebrisAxe(), BlockTools.createAncientDebrisShovel(), BlockTools.createAncientDebrisHoe()));
        sets.add(new ToolSetDisplay(Items.BASALT, "Basalt",
            BlockTools.createBasaltPickaxe(), BlockTools.createBasaltAxe(), BlockTools.createBasaltShovel(), BlockTools.createBasaltHoe()));
        sets.add(new ToolSetDisplay(Items.BLACKSTONE, "Blackstone",
            BlockTools.createBlackstonePickaxe(), BlockTools.createBlackstoneAxe(), BlockTools.createBlackstoneShovel(), BlockTools.createBlackstoneHoe()));
        sets.add(new ToolSetDisplay(Items.BONE_BLOCK, "Bone Block",
            BlockTools.createBoneBlockPickaxe(), BlockTools.createBoneBlockAxe(), BlockTools.createBoneBlockShovel(), BlockTools.createBoneBlockHoe()));
        sets.add(new ToolSetDisplay(Items.BRICKS, "Brick",
            BlockTools.createBrickPickaxe(), BlockTools.createBrickAxe(), BlockTools.createBrickShovel(), BlockTools.createBrickHoe()));
        sets.add(new ToolSetDisplay(Items.CACTUS, "Cactus",
            BlockTools.createCactusPickaxe(), BlockTools.createCactusAxe(), BlockTools.createCactusShovel(), BlockTools.createCactusHoe()));
        sets.add(new ToolSetDisplay(Items.CALCITE, "Calcite",
            BlockTools.createCalcitePickaxe(), BlockTools.createCalciteAxe(), BlockTools.createCalciteShovel(), BlockTools.createCalciteHoe()));
        sets.add(new ToolSetDisplay(Items.DEEPSLATE, "Deepslate",
            BlockTools.createDeepslatePickaxe(), BlockTools.createDeepslateAxe(), BlockTools.createDeepslateShovel(), BlockTools.createDeepslateHoe()));
        sets.add(new ToolSetDisplay(Items.DRIPSTONE_BLOCK, "Dripstone",
            BlockTools.createDripstonePickaxe(), BlockTools.createDripstoneAxe(), BlockTools.createDripstoneShovel(), BlockTools.createDripstoneHoe()));
        sets.add(new ToolSetDisplay(Items.HAY_BLOCK, "Hay",
            BlockTools.createHayPickaxe(), BlockTools.createHayAxe(), BlockTools.createHayShovel(), BlockTools.createHayHoe()));
        sets.add(new ToolSetDisplay(Items.HONEYCOMB_BLOCK, "Honeycomb",
            BlockTools.createHoneycombPickaxe(), BlockTools.createHoneycombAxe(), BlockTools.createHoneycombShovel(), BlockTools.createHoneycombHoe()));
        sets.add(new ToolSetDisplay(Items.LILY_PAD, "Lily Pad",
            BlockTools.createLilyPadPickaxe(), BlockTools.createLilyPadAxe(), BlockTools.createLilyPadShovel(), BlockTools.createLilyPadHoe()));
        sets.add(new ToolSetDisplay(Items.MELON, "Melon",
            BlockTools.createMelonPickaxe(), BlockTools.createMelonAxe(), BlockTools.createMelonShovel(), BlockTools.createMelonHoe()));
        sets.add(new ToolSetDisplay(Items.MOSS_BLOCK, "Moss Block",
            BlockTools.createMossBlockPickaxe(), BlockTools.createMossBlockAxe(), BlockTools.createMossBlockShovel(), BlockTools.createMossBlockHoe()));
        sets.add(new ToolSetDisplay(Items.MYCELIUM, "Mycelium",
            BlockTools.createMyceliumPickaxe(), BlockTools.createMyceliumAxe(), BlockTools.createMyceliumShovel(), BlockTools.createMyceliumHoe()));
        sets.add(new ToolSetDisplay(Items.NETHER_BRICKS, "Nether Brick",
            BlockTools.createNetherBrickPickaxe(), BlockTools.createNetherBrickAxe(), BlockTools.createNetherBrickShovel(), BlockTools.createNetherBrickHoe()));
        sets.add(new ToolSetDisplay(Items.PUMPKIN, "Pumpkin",
            BlockTools.createPumpkinPickaxe(), BlockTools.createPumpkinAxe(), BlockTools.createPumpkinShovel(), BlockTools.createPumpkinHoe()));
        sets.add(new ToolSetDisplay(Items.PURPUR_BLOCK, "Purpur",
            BlockTools.createPurpurPickaxe(), BlockTools.createPurpurAxe(), BlockTools.createPurpurShovel(), BlockTools.createPurpurHoe()));
        sets.add(new ToolSetDisplay(Items.SAND, "Sand",
            BlockTools.createSandPickaxe(), BlockTools.createSandAxe(), BlockTools.createSandShovel(), BlockTools.createSandHoe()));
        sets.add(new ToolSetDisplay(Items.SCULK, "Sculk",
            BlockTools.createSculkPickaxe(), BlockTools.createSculkAxe(), BlockTools.createSculkShovel(), BlockTools.createSculkHoe()));
        sets.add(new ToolSetDisplay(Items.SHROOMLIGHT, "Shroomlight",
            BlockTools.createShroomlightPickaxe(), BlockTools.createShroomlightAxe(), BlockTools.createShroomlightShovel(), BlockTools.createShroomlightHoe()));
        sets.add(new ToolSetDisplay(Items.SLIME_BLOCK, "Slime",
            BlockTools.createSlimePickaxe(), BlockTools.createSlimeAxe(), BlockTools.createSlimeShovel(), BlockTools.createSlimeHoe()));
        sets.add(new ToolSetDisplay(Items.SMOOTH_STONE, "Smooth Stone",
            BlockTools.createSmoothStonePickaxe(), BlockTools.createSmoothStoneAxe(), BlockTools.createSmoothStoneShovel(), BlockTools.createSmoothStoneHoe()));
        sets.add(new ToolSetDisplay(Items.SNOW_BLOCK, "Snow",
            BlockTools.createSnowPickaxe(), BlockTools.createSnowAxe(), BlockTools.createSnowShovel(), BlockTools.createSnowHoe()));
        sets.add(new ToolSetDisplay(Items.SOUL_SOIL, "Soul Soil",
            BlockTools.createSoulSoilPickaxe(), BlockTools.createSoulSoilAxe(), BlockTools.createSoulSoilShovel(), BlockTools.createSoulSoilHoe()));
        sets.add(new ToolSetDisplay(Items.SPONGE, "Sponge",
            BlockTools.createSpongePickaxe(), BlockTools.createSpongeAxe(), BlockTools.createSpongeShovel(), BlockTools.createSpongeHoe()));
        sets.add(new ToolSetDisplay(Items.TARGET, "Target",
            BlockTools.createTargetPickaxe(), BlockTools.createTargetAxe(), BlockTools.createTargetShovel(), BlockTools.createTargetHoe()));
        sets.add(new ToolSetDisplay(Items.TNT, "TNT",
            BlockTools.createTntPickaxe(), BlockTools.createTntAxe(), BlockTools.createTntShovel(), BlockTools.createTntHoe()));
        sets.add(new ToolSetDisplay(Items.WARPED_STEM, "Warped",
            BlockTools.createWarpedPickaxe(), BlockTools.createWarpedAxe(), BlockTools.createWarpedShovel(), BlockTools.createWarpedHoe()));
        sets.add(new ToolSetDisplay(Items.WET_SPONGE, "Wet Sponge",
            BlockTools.createWetSpongePickaxe(), BlockTools.createWetSpongeAxe(), BlockTools.createWetSpongeShovel(), BlockTools.createWetSpongeHoe()));

        // Pagination - 3 sets per page (each set takes 2 rows)
        int setsPerPage = 3;
        int totalPages = Math.max(1, (sets.size() + setsPerPage - 1) / setsPerPage);
        int clampedPage = Math.min(page, totalPages - 1);
        int startIdx = clampedPage * setsPerPage;
        int endIdx = Math.min(startIdx + setsPerPage, sets.size());

        // Display sets in organized rows
        for (int i = startIdx; i < endIdx; i++) {
            ToolSetDisplay set = sets.get(i);
            int rowOffset = (i - startIdx) * 18; // 2 rows per set
            int baseSlot = 10 + rowOffset;
            
            // Row 1: Block | Pickaxe | Axe | Shovel | Hoe
            // Row 2: Set Name separator
            
            // Block material (left side)
            gui.setSlot(baseSlot, new GuiElementBuilder(set.blockItem)
                    .setName(Text.literal(set.name + " Tools").formatted(Formatting.YELLOW, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Material:").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal(set.name).formatted(Formatting.WHITE))
                    .glow()
                    .build());
            
            // Tools (middle to right)
            gui.setSlot(baseSlot + 1, createToolElement(set.pickaxe, player));
            gui.setSlot(baseSlot + 2, createToolElement(set.axe, player));
            gui.setSlot(baseSlot + 3, createToolElement(set.shovel, player));
            gui.setSlot(baseSlot + 4, createToolElement(set.hoe, player));
            
            // Add separator lore below
            Text setName = Text.literal("━━ " + set.name + " Tools ━━").formatted(Formatting.DARK_GRAY);
            for (int sep = baseSlot + 9; sep < baseSlot + 14 && sep < 54; sep++) {
                gui.setSlot(sep, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                        .setName(setName)
                        .build());
            }
        }

        // Back button
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to Menu").formatted(Formatting.YELLOW))
                .setCallback((slot, type, action) -> openMainMenu(player))
                .build());

        // Pagination
        if (clampedPage > 0) {
            final int prev = clampedPage - 1;
            gui.setSlot(46, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("← Previous Page").formatted(Formatting.YELLOW))
                    .addLoreLine(Text.literal("Page " + (clampedPage) + " / " + totalPages))
                    .setCallback((slot, type, action) -> openBlockToolsMenu(player, prev))
                    .build());
        }
        if (clampedPage < totalPages - 1) {
            final int next = clampedPage + 1;
            gui.setSlot(48, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("Next Page →").formatted(Formatting.YELLOW))
                    .addLoreLine(Text.literal("Page " + (clampedPage + 2) + " / " + totalPages))
                    .setCallback((slot, type, action) -> openBlockToolsMenu(player, next))
                    .build());
        }

        gui.setSlot(49, new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal("Page " + (clampedPage + 1) + " / " + totalPages).formatted(Formatting.WHITE))
                .build());

        gui.open();
    }

    // ============================================================
    // BLOCK WEAPONS MENU
    // ============================================================

    private static void openBlockWeaponsMenu(ServerPlayerEntity player, int page) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X4, player, false);
        gui.setTitle(Text.literal("⚔ Block Weapons"));

        // Background with dark theme
        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.QUARTZ)
                .setName(Text.literal("⚔ Block Weapons").formatted(Formatting.WHITE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Block-themed swords").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Click any weapon to view recipe").formatted(Formatting.GRAY))
                .glow().build());

        // All block swords
        ItemStack[] weapons = {
            BlockWeapons.createGlassSword(),
            BlockWeapons.createObsidianSword(),
            BlockWeapons.createQuartzSword(),
            BlockWeapons.createGlowstoneSword(),
            BlockWeapons.createRedstoneSword(),
            BlockWeapons.createNetherrackSword(),
            BlockWeapons.createEndstoneSword(),
            BlockWeapons.createIceSword(),
            BlockWeapons.createPrismarineSword(),
            BlockWeapons.createTerracottaSword(),
            BlockWeapons.createMossySword(),
            BlockWeapons.createSoulSandSword(),
            BlockWeapons.createMagmaSword(),
            BlockWeapons.createSandstoneSword(),
            BlockWeapons.createAmethystSword(),
            BlockWeapons.createCoalSword(),
            // 36+ NEW BLOCK SWORDS
            BlockWeapons.createDiamondBlockSword(),
            BlockWeapons.createEmeraldBlockSword(),
            BlockWeapons.createGoldBlockSword(),
            BlockWeapons.createIronBlockSword(),
            BlockWeapons.createLapisBlockSword(),
            BlockWeapons.createCopperBlockSword(),
            BlockWeapons.createAncientDebrisSword(),
            BlockWeapons.createBasaltSword(),
            BlockWeapons.createBlackstoneSword(),
            BlockWeapons.createBoneBlockSword(),
            BlockWeapons.createBrickSword(),
            BlockWeapons.createCactusSword(),
            BlockWeapons.createCalciteSword(),
            BlockWeapons.createDeepslateSword(),
            BlockWeapons.createDripstoneSword(),
            BlockWeapons.createHaySword(),
            BlockWeapons.createHoneycombSword(),
            BlockWeapons.createLilyPadSword(),
            BlockWeapons.createMelonSword(),
            BlockWeapons.createMossBlockSword(),
            BlockWeapons.createMyceliumSword(),
            BlockWeapons.createNetherBrickSword(),
            BlockWeapons.createPumpkinSword(),
            BlockWeapons.createPurpurSword(),
            BlockWeapons.createSandSword(),
            BlockWeapons.createSculkSword(),
            BlockWeapons.createShroomlightSword(),
            BlockWeapons.createSlimeSword(),
            BlockWeapons.createSmoothStoneSword(),
            BlockWeapons.createSnowSword(),
            BlockWeapons.createSoulSoilSword(),
            BlockWeapons.createSpongeSword(),
            BlockWeapons.createTargetSword(),
            BlockWeapons.createTntSword(),
            BlockWeapons.createWarpedSword(),
            BlockWeapons.createWetSpongeSword()
        };

        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29};
        int idx = 0;
        for (ItemStack weapon : weapons) {
            if (idx >= slots.length) break;
            gui.setSlot(slots[idx++], new GuiElementBuilder(weapon)
                    .setName(weapon.get(DataComponentTypes.CUSTOM_NAME))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("✨ Click for recipe").formatted(Formatting.YELLOW))
                    .setCallback((i, clickType, action) -> showItemInfo(player, weapon))
                    .build());
        }

        // Back button
        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to Menu").formatted(Formatting.YELLOW))
                .setCallback((slot, clickType, action) -> openMainMenu(player))
                .build());

        gui.open();
    }

    private static GuiElementBuilder createToolElement(ItemStack tool, ServerPlayerEntity player) {
        Text name = tool.get(DataComponentTypes.CUSTOM_NAME);
        return new GuiElementBuilder(tool)
                .setName(name)
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("✨ Click for recipe").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> showItemInfo(player, tool));
    }

    private static void openT2SwordsMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X4, player, false);
        gui.setTitle(Text.literal("⚔ T2 Bounty Swords"));

        // Background with dark theme
        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.DIAMOND_SWORD)
                .setName(Text.literal("⚔ T2 Bounty Swords").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Tier 2 slayer swords").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Click any sword to view recipe").formatted(Formatting.GRAY))
                .glow().build());

        // Swords in a grid pattern
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23};
        int idx = 0;
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            if (idx >= slots.length) break;
            ItemStack sword = SlayerItems.createUpgradedSlayerSword(type);
            gui.setSlot(slots[idx++], new GuiElementBuilder(sword)
                    .setName(sword.get(DataComponentTypes.CUSTOM_NAME))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("✨ Click to view recipe").formatted(Formatting.YELLOW))
                    .setCallback((i, clickType, action) -> showItemInfo(player, SlayerItems.createUpgradedSlayerSword(type)))
                    .build());
        }

        // Back button
        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to Menu").formatted(Formatting.YELLOW))
                .setCallback((slot, clickType, action) -> openMainMenu(player))
                .build());

        gui.open();
    }

    private static void openT1ArmorMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X4, player, false);
        gui.setTitle(Text.literal("🛡 T1 Bounty Armor"));

        // Background with dark border
        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.IRON_CHESTPLATE)
                .setName(Text.literal("🛡 T1 Bounty Armor").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Select a slayer type below").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("to view the full armor set").formatted(Formatting.GRAY))
                .glow().build());

        // Slayer type buttons - row 2 and 3
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23};
        int idx = 0;
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            if (idx >= slots.length) break;
            gui.setSlot(slots[idx++], new GuiElementBuilder(type.icon)
                    .setName(Text.literal("§ " + type.displayName + " Set").formatted(type.color, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("▸ Helmet").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("▸ Chestplate").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("▸ Leggings").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("▸ Boots").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                    .setCallback((i, clickType, action) -> openT1ArmorSetMenu(player, type))
                    .build());
        }

        // Back button
        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to Menu").formatted(Formatting.YELLOW))
                .setCallback((i, clickType, action) -> openMainMenu(player))
                .build());

        gui.open();
    }

    private static void openT1ArmorSetMenu(ServerPlayerEntity player, SlayerManager.SlayerType slayerType) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
        gui.setTitle(Text.literal("🛡 " + slayerType.displayName + " T1 Set"));

        // Background
        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        // Header with slayer type info
        gui.setSlot(4, new GuiElementBuilder(slayerType.icon)
                .setName(Text.literal(slayerType.displayName + " T1 Set").formatted(slayerType.color, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Armor + Weapon set").formatted(Formatting.GRAY))
                .glow().build());

        // Armor pieces in a row (slots 10-13)
        gui.setSlot(10, new GuiElementBuilder(SlayerItems.createSlayerHelmet(slayerType, 1))
                .setName(SlayerItems.createSlayerHelmet(slayerType, 1).get(DataComponentTypes.CUSTOM_NAME))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("✨ Click to view recipe").formatted(Formatting.YELLOW))
                .setCallback((i, clickType, action) -> showItemInfo(player, SlayerItems.createSlayerHelmet(slayerType, 1)))
                .build());

        gui.setSlot(11, new GuiElementBuilder(SlayerItems.createSlayerChestplate(slayerType, 1))
                .setName(SlayerItems.createSlayerChestplate(slayerType, 1).get(DataComponentTypes.CUSTOM_NAME))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("✨ Click to view recipe").formatted(Formatting.YELLOW))
                .setCallback((i, clickType, action) -> showItemInfo(player, SlayerItems.createSlayerChestplate(slayerType, 1)))
                .build());

        gui.setSlot(12, new GuiElementBuilder(SlayerItems.createSlayerLeggings(slayerType, 1))
                .setName(SlayerItems.createSlayerLeggings(slayerType, 1).get(DataComponentTypes.CUSTOM_NAME))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("✨ Click to view recipe").formatted(Formatting.YELLOW))
                .setCallback((i, clickType, action) -> showItemInfo(player, SlayerItems.createSlayerLeggings(slayerType, 1)))
                .build());

        gui.setSlot(13, new GuiElementBuilder(SlayerItems.createSlayerBoots(slayerType, 1))
                .setName(SlayerItems.createSlayerBoots(slayerType, 1).get(DataComponentTypes.CUSTOM_NAME))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("✨ Click to view recipe").formatted(Formatting.YELLOW))
                .setCallback((i, clickType, action) -> showItemInfo(player, SlayerItems.createSlayerBoots(slayerType, 1)))
                .build());

        // Sword counterpart (slot 14)
        ItemStack sword = SlayerItems.createSlayerSword(slayerType);
        gui.setSlot(14, new GuiElementBuilder(sword)
                .setName(sword.get(DataComponentTypes.CUSTOM_NAME))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("✨ Click to view recipe").formatted(Formatting.YELLOW))
                .setCallback((i, clickType, action) -> showItemInfo(player, SlayerItems.createSlayerSword(slayerType)))
                .build());

        // Back button
        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to T1 Armor").formatted(Formatting.YELLOW))
                .setCallback((i, clickType, action) -> openT1ArmorMenu(player))
                .build());

        gui.open();
    }

    private static void openT2ArmorMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X4, player, false);
        gui.setTitle(Text.literal("🛡 T2 Bounty Armor"));

        // Background with dark border
        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.DIAMOND_CHESTPLATE)
                .setName(Text.literal("🛡 T2 Bounty Armor").formatted(Formatting.DARK_AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Select a slayer type below").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("to view the full armor set").formatted(Formatting.GRAY))
                .glow().build());

        // Slayer type buttons - row 2 and 3
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23};
        int idx = 0;
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            if (idx >= slots.length) break;
            gui.setSlot(slots[idx++], new GuiElementBuilder(type.icon)
                    .setName(Text.literal("§ " + type.displayName + " Set").formatted(type.color, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("▸ Helmet").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("▸ Chestplate").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("▸ Leggings").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("▸ Boots").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                    .setCallback((i, clickType, action) -> openT2ArmorSetMenu(player, type))
                    .build());
        }

        // Back button
        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to Menu").formatted(Formatting.YELLOW))
                .setCallback((i, clickType, action) -> openMainMenu(player))
                .build());

        gui.open();
    }

    private static void openT2ArmorSetMenu(ServerPlayerEntity player, SlayerManager.SlayerType slayerType) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
        gui.setTitle(Text.literal("🛡 " + slayerType.displayName + " T2 Set"));

        // Background
        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        // Header with slayer type info
        gui.setSlot(4, new GuiElementBuilder(slayerType.icon)
                .setName(Text.literal(slayerType.displayName + " T2 Set").formatted(slayerType.color, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Armor + Weapon set").formatted(Formatting.GRAY))
                .glow().build());

        // Armor pieces in a row (slots 10-13)
        gui.setSlot(10, new GuiElementBuilder(SlayerItems.createSlayerHelmet(slayerType, 2))
                .setName(SlayerItems.createSlayerHelmet(slayerType, 2).get(DataComponentTypes.CUSTOM_NAME))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("✨ Click to view recipe").formatted(Formatting.YELLOW))
                .setCallback((i, clickType, action) -> showItemInfo(player, SlayerItems.createSlayerHelmet(slayerType, 2)))
                .build());

        gui.setSlot(11, new GuiElementBuilder(SlayerItems.createSlayerChestplate(slayerType, 2))
                .setName(SlayerItems.createSlayerChestplate(slayerType, 2).get(DataComponentTypes.CUSTOM_NAME))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("✨ Click to view recipe").formatted(Formatting.YELLOW))
                .setCallback((i, clickType, action) -> showItemInfo(player, SlayerItems.createSlayerChestplate(slayerType, 2)))
                .build());

        gui.setSlot(12, new GuiElementBuilder(SlayerItems.createSlayerLeggings(slayerType, 2))
                .setName(SlayerItems.createSlayerLeggings(slayerType, 2).get(DataComponentTypes.CUSTOM_NAME))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("✨ Click to view recipe").formatted(Formatting.YELLOW))
                .setCallback((i, clickType, action) -> showItemInfo(player, SlayerItems.createSlayerLeggings(slayerType, 2)))
                .build());

        gui.setSlot(13, new GuiElementBuilder(SlayerItems.createSlayerBoots(slayerType, 2))
                .setName(SlayerItems.createSlayerBoots(slayerType, 2).get(DataComponentTypes.CUSTOM_NAME))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("✨ Click to view recipe").formatted(Formatting.YELLOW))
                .setCallback((i, clickType, action) -> showItemInfo(player, SlayerItems.createSlayerBoots(slayerType, 2)))
                .build());

        // Sword counterpart (slot 14)
        ItemStack sword = SlayerItems.createUpgradedSlayerSword(slayerType);
        gui.setSlot(14, new GuiElementBuilder(sword)
                .setName(sword.get(DataComponentTypes.CUSTOM_NAME))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("✨ Click to view recipe").formatted(Formatting.YELLOW))
                .setCallback((i, clickType, action) -> showItemInfo(player, SlayerItems.createUpgradedSlayerSword(slayerType)))
                .build());

        // Back button
        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to T2 Armor").formatted(Formatting.YELLOW))
                .setCallback((i, clickType, action) -> openT2ArmorMenu(player))
                .build());

        gui.open();
    }

    private static void openSpecialItemsMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X4, player, false);
        gui.setTitle(Text.literal("🎁 Special Items"));

        // Background with dark theme
        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Text.literal("🎁 Special Items").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Unique and legendary items").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Click any item to view recipe").formatted(Formatting.GRAY))
                .glow().build());

        // Add special items in a grid
        ItemStack[] specialItems = {
            SlayerItems.createMidasSword(),
            SlayerItems.createCrownOfGreed(),
            SlayerItems.createCrownOfMidas(),
            SlayerItems.createVoidwalkerCrown(),
            CustomItemHandler.createHermesShoes(),
            CustomItemHandler.createTheGavel(),
            CustomItemHandler.createHarveysStick(),
            DanielsPickaxe.create()
        };

        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23};
        int idx = 0;
        for (ItemStack item : specialItems) {
            if (idx >= slots.length) break;
            gui.setSlot(slots[idx++], new GuiElementBuilder(item)
                    .setName(item.get(DataComponentTypes.CUSTOM_NAME))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("✨ Click to view recipe").formatted(Formatting.YELLOW))
                    .setCallback((slot, type2, action) -> showItemInfo(player, item))
                    .build());
        }

        // Back button
        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to Menu").formatted(Formatting.YELLOW))
                .setCallback((slot, type, action) -> openMainMenu(player))
                .build());

        gui.open();
    }

    // ============================================================
    // CUSTOM TOOLS MENU - 15 Legendary Tool Sets
    // ============================================================
    private static void openCustomToolsMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("⛏ Legendary Tools"));

        // Background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.NETHERITE_PICKAXE)
                .setName(Text.literal("⛏ Legendary Tools").formatted(Formatting.DARK_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("15 unique tool sets with abilities").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Pickaxe → Axe → Shovel → Hoe").formatted(Formatting.YELLOW))
                .glow().build());

        // Define tool sets: Block/Icon, Name, Color, Tools
        List<ToolSetDisplay> sets = new ArrayList<>();
        
        // Set 1: Dragon
        sets.add(new ToolSetDisplay(Items.DRAGON_HEAD, "Dragon", Formatting.DARK_RED,
            LegendaryTools.createDragonPickaxe(), LegendaryTools.createDragonAxe(),
            LegendaryTools.createDragonShovel(), LegendaryTools.createDragonHoe()));
        // Set 2: Aether
        sets.add(new ToolSetDisplay(Items.ELYTRA, "Aether", Formatting.AQUA,
            LegendaryTools.createAetherPickaxe(), LegendaryTools.createAetherAxe(),
            LegendaryTools.createAetherShovel(), LegendaryTools.createAetherHoe()));
        // Set 3: Void
        sets.add(new ToolSetDisplay(Items.ENDER_PEARL, "Void", Formatting.DARK_PURPLE,
            LegendaryTools.createVoidPickaxe(), LegendaryTools.createVoidAxe(),
            LegendaryTools.createVoidShovel(), LegendaryTools.createVoidHoe()));
        // Set 4: Nature
        sets.add(new ToolSetDisplay(Items.OAK_SAPLING, "Nature", Formatting.GREEN,
            LegendaryTools.createNaturePickaxe(), LegendaryTools.createNatureAxe(),
            LegendaryTools.createNatureShovel(), LegendaryTools.createNatureHoe()));
        // Set 5: Frost
        sets.add(new ToolSetDisplay(Items.SNOW_BLOCK, "Frost", Formatting.BLUE,
            LegendaryTools.createFrostPickaxe(), LegendaryTools.createFrostAxe(),
            LegendaryTools.createFrostShovel(), LegendaryTools.createFrostHoe()));
        // Set 6: Thunder
        sets.add(new ToolSetDisplay(Items.LIGHTNING_ROD, "Thunder", Formatting.YELLOW,
            LegendaryTools.createThunderPickaxe(), LegendaryTools.createThunderAxe(),
            LegendaryTools.createThunderShovel(), LegendaryTools.createThunderHoe()));
        // Set 7: Ocean
        sets.add(new ToolSetDisplay(Items.HEART_OF_THE_SEA, "Ocean", Formatting.DARK_BLUE,
            LegendaryTools.createOceanPickaxe(), LegendaryTools.createOceanAxe(),
            LegendaryTools.createOceanShovel(), LegendaryTools.createOceanHoe()));
        // Set 8: Lunar
        sets.add(new ToolSetDisplay(Items.PHANTOM_MEMBRANE, "Lunar", Formatting.LIGHT_PURPLE,
            LegendaryTools.createLunarPickaxe(), LegendaryTools.createLunarAxe(),
            LegendaryTools.createLunarShovel(), LegendaryTools.createLunarHoe()));
        // Set 9: Solar
        sets.add(new ToolSetDisplay(Items.FIRE_CHARGE, "Solar", Formatting.GOLD,
            LegendaryTools.createSolarPickaxe(), LegendaryTools.createSolarAxe(),
            LegendaryTools.createSolarShovel(), LegendaryTools.createSolarHoe()));
        // Set 10: Terra
        sets.add(new ToolSetDisplay(Items.GRASS_BLOCK, "Terra", Formatting.DARK_GREEN,
            LegendaryTools.createTerraPickaxe(), LegendaryTools.createTerraAxe(),
            LegendaryTools.createTerraShovel(), LegendaryTools.createTerraHoe()));
        // Set 11: Phantom
        sets.add(new ToolSetDisplay(Items.SPECTRAL_ARROW, "Phantom", Formatting.GRAY,
            LegendaryTools.createPhantomPickaxe(), LegendaryTools.createPhantomAxe(),
            LegendaryTools.createPhantomShovel(), LegendaryTools.createPhantomHoe()));
        // Set 12: Blood
        sets.add(new ToolSetDisplay(Items.NETHER_WART, "Blood", Formatting.DARK_RED,
            LegendaryTools.createBloodPickaxe(), LegendaryTools.createBloodAxe(),
            LegendaryTools.createBloodShovel(), LegendaryTools.createBloodHoe()));
        // Set 13: Celestial
        sets.add(new ToolSetDisplay(Items.NETHER_STAR, "Celestial", Formatting.DARK_AQUA,
            LegendaryTools.createCelestialPickaxe(), LegendaryTools.createCelestialAxe(),
            LegendaryTools.createCelestialShovel(), LegendaryTools.createCelestialHoe()));
        // Set 14: Shadow
        sets.add(new ToolSetDisplay(Items.BLACK_DYE, "Shadow", Formatting.BLACK,
            LegendaryTools.createShadowPickaxe(), LegendaryTools.createShadowAxe(),
            LegendaryTools.createShadowShovel(), LegendaryTools.createShadowHoe()));
        // Set 15: Crystal
        sets.add(new ToolSetDisplay(Items.DIAMOND, "Crystal", Formatting.WHITE,
            LegendaryTools.createCrystalPickaxe(), LegendaryTools.createCrystalAxe(),
            LegendaryTools.createCrystalShovel(), LegendaryTools.createCrystalHoe()));

        // Display sets in rows (3 sets per page, each set takes 2 rows)
        int setsPerPage = 3;
        int totalPages = Math.max(1, (sets.size() + setsPerPage - 1) / setsPerPage);
        int clampedPage = 0;
        int startIdx = 0;
        int endIdx = Math.min(setsPerPage, sets.size());

        for (int i = startIdx; i < endIdx; i++) {
            ToolSetDisplay set = sets.get(i);
            int rowOffset = (i - startIdx) * 18; // 2 rows per set
            int baseSlot = 10 + rowOffset;
            
            // Row 1: Icon | Pickaxe | Axe | Shovel | Hoe
            gui.setSlot(baseSlot, new GuiElementBuilder(set.blockItem)
                    .setName(Text.literal(set.name + " Set").formatted(set.color, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Material:").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal(set.name).formatted(set.color))
                    .glow()
                    .build());
            
            // Gap slot
            gui.setSlot(baseSlot + 1, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
            
            // Tools
            gui.setSlot(baseSlot + 2, createToolElement(set.pickaxe, player));
            gui.setSlot(baseSlot + 3, createToolElement(set.axe, player));
            gui.setSlot(baseSlot + 4, createToolElement(set.shovel, player));
            gui.setSlot(baseSlot + 5, createToolElement(set.hoe, player));
            
            // Separator row
            Text separator = Text.literal("━━ " + set.name + " Tools ━━").formatted(Formatting.DARK_GRAY);
            for (int sep = 0; sep < 9; sep++) {
                int sepSlot = baseSlot + 9 + sep;
                if (sepSlot < 54) {
                    gui.setSlot(sepSlot, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                            .setName(separator)
                            .build());
                }
            }
        }

        // Back button
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to Menu").formatted(Formatting.YELLOW))
                .setCallback((slot, type, action) -> openMainMenu(player))
                .build());

        if (totalPages > 1) {
            gui.setSlot(49, new GuiElementBuilder(Items.PAPER)
                    .setName(Text.literal("Page 1 / " + totalPages).formatted(Formatting.WHITE))
                    .build());
        }

        gui.open();
    }

    // ============================================================
    // SPECIAL ARMOR MENU - Legendary & special armor pieces
    // ============================================================
    private static void openSpecialArmorMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X4, player, false);
        gui.setTitle(Text.literal("🛡 Special Armor"));

        // Background with dark theme
        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.NETHERITE_HELMET)
                .setName(Text.literal("🛡 Special Armor").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Legendary & special armor pieces").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Click any item to view recipe").formatted(Formatting.GRAY))
                .glow().build());

        // Add special armor pieces in a grid
        ItemStack[] specialArmor = {
            SlayerItems.createZombieBerserkerHelmet(),
            SlayerItems.createSpiderLeggings(),
            SlayerItems.createSkeletonBow(),
            SlayerItems.createSlimeBoots(),
            SlayerItems.createWardenChestplate(),
            SlayerItems.createVoidwalkerCrown(),
            SlayerItems.createEnderPhaseHelmet()
        };

        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23};
        int idx = 0;
        for (ItemStack item : specialArmor) {
            if (idx >= slots.length) break;
            gui.setSlot(slots[idx++], new GuiElementBuilder(item)
                    .setName(item.get(DataComponentTypes.CUSTOM_NAME))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("✨ Click to view recipe").formatted(Formatting.YELLOW))
                    .setCallback((slot, type2, action) -> showItemInfo(player, item))
                    .build());
        }

        // Back button
        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to Menu").formatted(Formatting.YELLOW))
                .setCallback((slot, type, action) -> openMainMenu(player))
                .build());

        gui.open();
    }

    // ============================================================
    // BLOCK ARMOR MENU - Armor Sets in Rows Format
    // Each row: [Block] [Sword] [Helmet] [Chest] [Legs] [Boots]
    // ============================================================
    private static void openBlockArmorMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("💎 Gem & Block Armor"));

        // Background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.EMERALD)
                .setName(Text.literal("💎 Gem & Block Armor").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Armor sets displayed in rows:").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("[Block] [Sword] [Helmet] [Chest] [Legs] [Boots]").formatted(Formatting.YELLOW))
                .glow().build());

        // Define armor sets: Block Item, Name, Sword, Armor pieces
        ArmorSetDisplay[] sets = {
            new ArmorSetDisplay(Items.GLASS, "Glass", BlockWeapons.createGlassSword(),
                BlockArmor.createGlassHelmet(), BlockArmor.createGlassChestplate(),
                BlockArmor.createGlassLeggings(), BlockArmor.createGlassBoots()),
            new ArmorSetDisplay(Items.OBSIDIAN, "Obsidian", BlockWeapons.createObsidianSword(),
                BlockArmor.createObsidianHelmet(), BlockArmor.createObsidianChestplate(),
                BlockArmor.createObsidianLeggings(), BlockArmor.createObsidianBoots()),
            new ArmorSetDisplay(Items.QUARTZ_BLOCK, "Quartz", BlockWeapons.createQuartzSword(),
                BlockArmor.createQuartzHelmet(), BlockArmor.createQuartzChestplate(),
                BlockArmor.createQuartzLeggings(), BlockArmor.createQuartzBoots()),
            new ArmorSetDisplay(Items.GLOWSTONE, "Glowstone", BlockWeapons.createGlowstoneSword(),
                BlockArmor.createGlowstoneHelmet(), BlockArmor.createGlowstoneChestplate(),
                BlockArmor.createGlowstoneLeggings(), BlockArmor.createGlowstoneBoots()),
            new ArmorSetDisplay(Items.REDSTONE_BLOCK, "Redstone", BlockWeapons.createRedstoneSword(),
                BlockArmor.createRedstoneHelmet(), BlockArmor.createRedstoneChestplate(),
                BlockArmor.createRedstoneLeggings(), BlockArmor.createRedstoneBoots()),
            new ArmorSetDisplay(Items.NETHERRACK, "Netherrack", BlockWeapons.createNetherrackSword(),
                BlockArmor.createNetherrackHelmet(), BlockArmor.createNetherrackChestplate(),
                BlockArmor.createNetherrackLeggings(), BlockArmor.createNetherrackBoots()),
            new ArmorSetDisplay(Items.END_STONE, "End Stone", BlockWeapons.createEndstoneSword(),
                BlockArmor.createEndstoneHelmet(), BlockArmor.createEndstoneChestplate(),
                BlockArmor.createEndstoneLeggings(), BlockArmor.createEndstoneBoots()),
            new ArmorSetDisplay(Items.PACKED_ICE, "Ice", BlockWeapons.createIceSword(),
                BlockArmor.createIceHelmet(), BlockArmor.createIceChestplate(),
                BlockArmor.createIceLeggings(), BlockArmor.createIceBoots()),
            new ArmorSetDisplay(Items.PRISMARINE, "Prismarine", BlockWeapons.createPrismarineSword(),
                BlockArmor.createPrismarineHelmet(), BlockArmor.createPrismarineChestplate(),
                BlockArmor.createPrismarineLeggings(), BlockArmor.createPrismarineBoots()),
            new ArmorSetDisplay(Items.TERRACOTTA, "Terracotta", BlockWeapons.createTerracottaSword(),
                BlockArmor.createTerracottaHelmet(), BlockArmor.createTerracottaChestplate(),
                BlockArmor.createTerracottaLeggings(), BlockArmor.createTerracottaBoots()),
            new ArmorSetDisplay(Items.MOSSY_COBBLESTONE, "Mossy", BlockWeapons.createMossySword(),
                BlockArmor.createMossyHelmet(), BlockArmor.createMossyChestplate(),
                BlockArmor.createMossyLeggings(), BlockArmor.createMossyBoots()),
            new ArmorSetDisplay(Items.SOUL_SAND, "Soul Sand", BlockWeapons.createSoulSandSword(),
                BlockArmor.createSoulSandHelmet(), BlockArmor.createSoulSandChestplate(),
                BlockArmor.createSoulSandLeggings(), BlockArmor.createSoulSandBoots()),
            new ArmorSetDisplay(Items.MAGMA_BLOCK, "Magma", BlockWeapons.createMagmaSword(),
                BlockArmor.createMagmaHelmet(), BlockArmor.createMagmaChestplate(),
                BlockArmor.createMagmaLeggings(), BlockArmor.createMagmaBoots()),
            new ArmorSetDisplay(Items.SANDSTONE, "Sandstone", BlockWeapons.createSandstoneSword(),
                BlockArmor.createSandstoneHelmet(), BlockArmor.createSandstoneChestplate(),
                BlockArmor.createSandstoneLeggings(), BlockArmor.createSandstoneBoots()),
            new ArmorSetDisplay(Items.AMETHYST_BLOCK, "Amethyst", BlockWeapons.createAmethystSword(),
                BlockArmor.createAmethystHelmet(), BlockArmor.createAmethystChestplate(),
                BlockArmor.createAmethystLeggings(), BlockArmor.createAmethystBoots()),
            new ArmorSetDisplay(Items.COAL_BLOCK, "Coal", BlockWeapons.createCoalSword(),
                BlockArmor.createCoalHelmet(), BlockArmor.createCoalChestplate(),
                BlockArmor.createCoalLeggings(), BlockArmor.createCoalBoots()),
        };

        // Display armor sets in rows (4 sets per page)
        final int SETS_PER_PAGE = 4;
        int totalPages = (sets.length + SETS_PER_PAGE - 1) / SETS_PER_PAGE;
        int clampedPage = 0; // For now, just show page 0

        int startIdx = clampedPage * SETS_PER_PAGE;
        for (int i = 0; i < SETS_PER_PAGE && (startIdx + i) < sets.length; i++) {
            ArmorSetDisplay set = sets[startIdx + i];
            int row = i + 1; // Rows 1-4 (slots 9-44)

            // Row layout: [0:Block] [1:gap] [2:Sword] [3:Helmet] [4:Chest] [5:Legs] [6:Boots]
            int[] slots = { row * 9, row * 9 + 1, row * 9 + 2, row * 9 + 3, row * 9 + 4, row * 9 + 5, row * 9 + 6 };

            // Block (decorative background)
            gui.setSlot(slots[0], new GuiElementBuilder(set.blockItem)
                    .setName(Text.literal("§8" + set.blockItem.toString().replace("_", " ")).formatted(Formatting.DARK_GRAY))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Armor Set Material").formatted(Formatting.GRAY))
                    .build());
            
            // Gap slot (shifted armor 1 to the right)
            gui.setSlot(slots[1], new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());

            // Sword
            Text swordName = set.sword.get(DataComponentTypes.CUSTOM_NAME);
            gui.setSlot(slots[2], new GuiElementBuilder(set.sword.getItem())
                    .setName(swordName != null ? swordName : Text.literal("Sword"))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("✨ Click to view recipe").formatted(Formatting.YELLOW))
                    .setCallback((s, t, a) -> showItemInfo(player, set.sword))
                    .build());

            // Armor pieces: Helmet, Chestplate, Leggings, Boots (shifted 1 slot right)
            ItemStack[] armorPieces = { set.helmet, set.chestplate, set.leggings, set.boots };
            for (int j = 0; j < 4; j++) {
                ItemStack armor = armorPieces[j];
                Text armorName = armor.get(DataComponentTypes.CUSTOM_NAME);
                gui.setSlot(slots[3 + j], new GuiElementBuilder(armor.getItem())
                        .setName(armorName != null ? armorName : Text.literal("Armor"))
                        .addLoreLine(Text.literal(""))
                        .addLoreLine(Text.literal("✨ Click to view recipe").formatted(Formatting.YELLOW))
                        .setCallback((s, t, a) -> showItemInfo(player, armor))
                        .build());
            }
        }

        // Back button
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to Main Menu").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openMainMenu(player))
                .build());

        // Pagination buttons (if needed in future)
        if (totalPages > 1) {
            gui.setSlot(49, new GuiElementBuilder(Items.PAPER)
                    .setName(Text.literal("Page 1 / " + totalPages))
                    .build());
        }

        gui.open();
    }

    // ============================================================
    // GOLD ARMOR EVOLUTION MENU - All gold armor tiers
    // ============================================================
    private static void openGoldArmorMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X4, player, false);
        gui.setTitle(Text.literal("👑 Gold Armor Evolution"));

        // Background with dark theme
        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.GOLDEN_CHESTPLATE)
                .setName(Text.literal("👑 Gold Armor Evolution").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Pure → Polished → Shiny → Glistening → Gilded").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Click any item to view recipe").formatted(Formatting.GRAY))
                .glow().build());

        // Add gold armor tiers in a row
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23};
        ItemStack[] goldArmor = {
            SlayerItems.createPureGoldHelmet(),
            SlayerItems.createPureGoldChestplate(),
            SlayerItems.createPureGoldLeggings(),
            SlayerItems.createPureGoldBoots(),
            SlayerItems.createPolishedGoldHelmet(),
            SlayerItems.createPolishedGoldChestplate(),
            SlayerItems.createPolishedGoldLeggings(),
            SlayerItems.createPolishedGoldBoots(),
            SlayerItems.createShinyGoldHelmet(),
            SlayerItems.createShinyGoldChestplate(),
            SlayerItems.createShinyGoldLeggings(),
            SlayerItems.createShinyGoldBoots()
        };

        int idx = 0;
        for (ItemStack item : goldArmor) {
            if (idx >= slots.length) break;
            gui.setSlot(slots[idx++], new GuiElementBuilder(item)
                    .setName(item.get(DataComponentTypes.CUSTOM_NAME))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("✨ Click to view recipe").formatted(Formatting.YELLOW))
                    .setCallback((slot, type2, action) -> showItemInfo(player, item))
                    .build());
        }

        // Back button
        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to Menu").formatted(Formatting.YELLOW))
                .setCallback((slot, type, action) -> openMainMenu(player))
                .build());

        gui.open();
    }

    // ============================================================
    // ATTRIBUTE TOKENS MENU - Armor attribute tokens
    // ============================================================
    private static void openAttributeTokensMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X4, player, false);
        gui.setTitle(Text.literal("✧ Attribute Tokens"));

        // Background with dark theme
        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal("✧ Attribute Tokens").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Armor attribute tokens for crafting").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Click any token to view recipe").formatted(Formatting.GRAY))
                .glow().build());

        // Add all attribute tokens in a grid
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23};
        int idx = 0;
        for (ArmourAttribute attr : ArmourAttribute.values()) {
            if (idx >= slots.length) break;
            
            ItemStack token = SlayerItems.createAttributeToken(attr);
            gui.setSlot(slots[idx++], new GuiElementBuilder(token)
                    .setName(token.get(DataComponentTypes.CUSTOM_NAME))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("✨ Click to view recipe").formatted(Formatting.YELLOW))
                    .setCallback((slot, type2, action) -> showItemInfo(player, token))
                    .build());
        }

        // Back button
        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to Menu").formatted(Formatting.YELLOW))
                .setCallback((slot, type, action) -> openMainMenu(player))
                .build());

        gui.open();
    }

    private static void openPiglinItemsMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
        gui.setTitle(Text.literal("👑 Piglin Items"));

        // Background with dark theme
        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.GOLDEN_HELMET)
                .setName(Text.literal("👑 Piglin Items").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Piglin-themed items").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Click any item to view recipe").formatted(Formatting.GRAY))
                .glow().build());

        // Add piglin items in a row
        ItemStack[] piglinItems = {
            SlayerItems.createPiglinCore(),
            SlayerItems.createPiglinFlesh(),
            SlayerItems.createCrownOfGreed(),
            SlayerItems.createCrownOfMidas(),
            SlayerItems.createMidasSword()
        };

        int[] slots = {10, 11, 12, 13, 14};
        int idx = 0;
        for (ItemStack item : piglinItems) {
            if (idx >= slots.length) break;
            gui.setSlot(slots[idx++], new GuiElementBuilder(item)
                    .setName(item.get(DataComponentTypes.CUSTOM_NAME))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("✨ Click to view recipe").formatted(Formatting.YELLOW))
                    .setCallback((slot, type2, action) -> showItemInfo(player, item))
                    .build());
        }

        // Back button
        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to Menu").formatted(Formatting.YELLOW))
                .setCallback((slot, type, action) -> openMainMenu(player))
                .build());

        gui.open();
    }

    // ============================================================
    // MATERIALS MENU - Shows recent additions
    // ============================================================
    private static void openMaterialsMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X4, player, false);
        gui.setTitle(Text.literal("📦 Materials"));

        // Background with dark theme
        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.SLIME_BALL)
                .setName(Text.literal("📦 Materials").formatted(Formatting.GRAY, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("All crafting materials").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Click any item to view recipe").formatted(Formatting.GRAY))
                .glow().build());

        // Add cores and chunks in a grid
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23};
        int idx = 0;
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            if (idx >= slots.length) break;
            
            gui.setSlot(slots[idx++], new GuiElementBuilder(SlayerItems.createCore(type))
                    .setName(SlayerItems.createCore(type).get(DataComponentTypes.CUSTOM_NAME))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("✨ Click to view recipe").formatted(Formatting.YELLOW))
                    .setCallback((slot, type2, action) -> showItemInfo(player, SlayerItems.createCore(type)))
                    .build());
        }

        // Back button
        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to Menu").formatted(Formatting.YELLOW))
                .setCallback((slot, type, action) -> openMainMenu(player))
                .build());

        gui.open();
    }

    // ============================================================
    // WEAPON ATTRIBUTES MENU - Recent addition
    // ============================================================
    private static void openWeaponAttributesMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X4, player, false);
        gui.setTitle(Text.literal("⚔ Weapon Attributes"));

        // Background with dark theme
        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Text.literal("⚔ Weapon Attributes").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Weapon attribute tokens").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Click any token to view recipe").formatted(Formatting.GRAY))
                .glow().build());

        // Add all weapon attribute tokens in a grid
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23};
        int idx = 0;
        for (WeaponAttribute attr : WeaponAttribute.values()) {
            if (idx >= slots.length) break;
            
            ItemStack token = SlayerItems.createWeaponAttributeToken(attr);
            gui.setSlot(slots[idx++], new GuiElementBuilder(token)
                    .setName(token.get(DataComponentTypes.CUSTOM_NAME))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("✨ Click to view recipe").formatted(Formatting.YELLOW))
                    .setCallback((slot, type2, action) -> showItemInfo(player, token))
                    .build());
        }

        // Back button
        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to Menu").formatted(Formatting.YELLOW))
                .setCallback((slot, type, action) -> openMainMenu(player))
                .build());

        gui.open();
    }

    // ============================================================
    // HELPER: Show item information with actual recipe
    // ============================================================
    private static void showItemInfo(ServerPlayerEntity player, ItemStack resultItem) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X4, player, false);
        gui.setTitle(Text.literal("Recipe View"));

        // Background with dark theme
        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        // Header - Item name
        Text displayName = resultItem.get(DataComponentTypes.CUSTOM_NAME);
        if (displayName == null) {
            displayName = Text.literal(resultItem.getItem().toString());
        }
        gui.setSlot(4, new GuiElementBuilder(resultItem.getItem())
                .setName(displayName)
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Crafting Recipe").formatted(Formatting.GRAY))
                .glow().build());

        // Recipe grid slots (3x3)
        int[] gridSlots = {10, 11, 12, 19, 20, 21, 28, 29, 30};
        
        // Try to find the recipe from RecipeConfigManager (includes custom recipes)
        SlayerRecipes.Recipe recipe = findRecipeForOutput(resultItem);
        
        if (recipe != null && recipe.ingredients != null) {
            // Display the actual recipe
            for (int i = 0; i < 9 && i < recipe.ingredients.length; i++) {
                ItemStack input = recipe.ingredients[i];
                if (input != null && !input.isEmpty()) {
                    Text inputName = input.get(DataComponentTypes.CUSTOM_NAME);
                    if (inputName == null) {
                        inputName = Text.literal(input.getItem().toString());
                    }
                    gui.setSlot(gridSlots[i], new GuiElementBuilder(input.getItem())
                            .setName(inputName)
                            .setCount(input.getCount())
                            .build());
                } else {
                    // Empty slot indicator
                    gui.setSlot(gridSlots[i], new GuiElementBuilder(Items.LIGHT_GRAY_STAINED_GLASS_PANE)
                            .setName(Text.literal("Empty").formatted(Formatting.GRAY))
                            .build());
                }
            }
            
            // Arrow indicator
            gui.setSlot(23, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("→").formatted(Formatting.YELLOW))
                    .build());
            
            // Result
            gui.setSlot(24, new GuiElementBuilder(resultItem)
                    .setName(displayName)
                    .glow().build());
        } else {
            // No recipe found - show message
            gui.setSlot(22, new GuiElementBuilder(Items.BARRIER)
                    .setName(Text.literal("No recipe found").formatted(Formatting.RED))
                    .addLoreLine(Text.literal("This item may be obtained"))
                    .addLoreLine(Text.literal("through other means."))
                    .build());
        }

        // Back button
        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to Menu").formatted(Formatting.YELLOW))
                .setCallback((slot, type, action) -> openMainMenu(player))
                .build());

        gui.open();
    }

    // ============================================================
    // HELPER: Find recipe for output item (checks custom recipes too)
    // ============================================================
    private static SlayerRecipes.Recipe findRecipeForOutput(ItemStack output) {
        if (output == null || output.isEmpty()) return null;
        
        String outputId = SlayerItems.getCustomItemId(output);
        
        // Check all recipes (hardcoded + custom)
        for (SlayerRecipes.Recipe recipe : RecipeConfigManager.getRecipes()) {
            if (recipe.result == null || recipe.result.isEmpty()) continue;
            
            // Try to match by custom item ID first
            if (outputId != null) {
                String recipeId = SlayerItems.getCustomItemId(recipe.result);
                if (outputId.equals(recipeId)) {
                    // Check if recipe has actual ingredients (not blank)
                    if (hasValidIngredients(recipe)) {
                        return recipe;
                    }
                }
            }
            
            // Fallback: match by item type and custom name
            if (recipe.result.getItem() == output.getItem()) {
                Text recipeName = recipe.result.get(DataComponentTypes.CUSTOM_NAME);
                Text outputName = output.get(DataComponentTypes.CUSTOM_NAME);
                if (recipeName != null && outputName != null && recipeName.equals(outputName)) {
                    if (hasValidIngredients(recipe)) {
                        return recipe;
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Checks if a recipe has at least one non-empty ingredient.
     * Returns false if the recipe is blank (no ingredients or all empty).
     */
    private static boolean hasValidIngredients(SlayerRecipes.Recipe recipe) {
        if (recipe.ingredients == null) return false;
        for (ItemStack ing : recipe.ingredients) {
            if (ing != null && !ing.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    // ============================================================
    // SEARCH GUI - Sign-based search with partial matching
    // ============================================================
    private static void openSearchGui(ServerPlayerEntity player) {
        eu.pb4.sgui.api.gui.SignGui signGui = new eu.pb4.sgui.api.gui.SignGui(player) {
            @Override
            public void onClose() {
                String searchTerm = this.getLine(0).getString().trim();
                if (!searchTerm.isEmpty()) {
                    showSearchResults(player, searchTerm);
                } else {
                    openMainMenu(player);
                }
            }
        };
        signGui.setLine(0, Text.literal(""));
        signGui.setLine(1, Text.literal("^^^^^^^^^^^^^^").formatted(Formatting.YELLOW));
        signGui.setLine(2, Text.literal("Enter item name").formatted(Formatting.AQUA));
        signGui.setLine(3, Text.literal("(partial ok)").formatted(Formatting.GRAY));
        signGui.open();
    }
    
    private static void showSearchResults(ServerPlayerEntity player, String searchTerm) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("🔍 Search: " + searchTerm));
        
        // Background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }
        
        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.SPYGLASS)
                .setName(Text.literal("🔍 Search Results").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Search: \"" + searchTerm + "\"").formatted(Formatting.YELLOW))
                .glow().build());
        
        // Search through all recipes
        java.util.List<ItemStack> results = new java.util.ArrayList<>();
        String searchLower = searchTerm.toLowerCase();
        
        // Check all recipes
        for (SlayerRecipes.Recipe recipe : RecipeConfigManager.getRecipes()) {
            if (recipe.result != null && !recipe.result.isEmpty()) {
                Text nameText = recipe.result.get(DataComponentTypes.CUSTOM_NAME);
                String itemName = nameText != null ? nameText.getString().toLowerCase() : 
                                  recipe.result.getItem().toString().toLowerCase();
                
                if (itemName.contains(searchLower)) {
                    results.add(recipe.result);
                }
            }
        }
        
        // Display results
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 
                       28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};
        int slotIdx = 0;
        
        for (ItemStack result : results) {
            if (slotIdx >= slots.length) break;
            
            Text name = result.get(DataComponentTypes.CUSTOM_NAME);
            if (name == null) name = Text.literal(result.getItem().toString());
            
            gui.setSlot(slots[slotIdx++], new GuiElementBuilder(result)
                    .setName(name)
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("✨ Click for recipe").formatted(Formatting.YELLOW))
                    .setCallback((idx, type, action) -> showItemInfo(player, result))
                    .build());
        }
        
        if (results.isEmpty()) {
            gui.setSlot(22, new GuiElementBuilder(Items.BARRIER)
                    .setName(Text.literal("No results found").formatted(Formatting.RED))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Try a different search term").formatted(Formatting.GRAY))
                    .build());
        }
        
        // Back and search again buttons
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to Menu").formatted(Formatting.YELLOW))
                .setCallback((slot, type, action) -> openMainMenu(player))
                .build());
        
        gui.setSlot(46, new GuiElementBuilder(Items.SPYGLASS)
                .setName(Text.literal("🔍 Search Again").formatted(Formatting.AQUA))
                .setCallback((slot, type, action) -> openSearchGui(player))
                .build());
        
        gui.open();
    }

    // ============================================================
    // SPECIFIC RECIPE LOOKUP - Search by item name
    // ============================================================
    public static boolean openSpecificRecipe(ServerPlayerEntity player, String itemName) {
        String searchLower = itemName.toLowerCase().replace(" ", "_");
        
        // Search through all recipes for a match
        for (SlayerRecipes.Recipe recipe : RecipeConfigManager.getRecipes()) {
            if (recipe.result == null || recipe.result.isEmpty()) continue;
            
            Text nameText = recipe.result.get(DataComponentTypes.CUSTOM_NAME);
            String recipeItemName = nameText != null ? nameText.getString().toLowerCase().replace(" ", "_") : 
                                    recipe.result.getItem().toString().toLowerCase();
            
            // Check for match by name or custom item ID
            String customId = SlayerItems.getCustomItemId(recipe.result);
            boolean matches = recipeItemName.contains(searchLower) || searchLower.contains(recipeItemName) ||
                              (customId != null && customId.toLowerCase().contains(searchLower));
            
            if (matches && hasValidIngredients(recipe)) {
                showItemInfo(player, recipe.result);
                return true;
            }
        }
        
        // No recipe found - open main menu with message
        player.sendMessage(Text.literal("✖ No recipe found for: " + itemName).formatted(Formatting.RED), false);
        player.sendMessage(Text.literal("Use /recipe to browse all categories").formatted(Formatting.GRAY), false);
        openMainMenu(player);
        return false;
    }
}
