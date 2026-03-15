package com.political;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI for the /setrecipe admin command.
 *
 * Navigation hierarchy:
 *   Main Menu (category list)
 *     → Category Page (all items in that category)
 *       → Recipe Editor (3×3 grid editor, save/clear, ← Back)
 */
public class RecipeEditorGui {

    // ── Recipe-editor layout constants (9×6 chest) ──────────────
    private static final int[] GRID_SLOTS   = {10, 11, 12, 19, 20, 21, 28, 29, 30};
    private static final int SLOT_ARROW     = 23;
    private static final int SLOT_RESULT    = 24;
    private static final int SLOT_SAVE      = 33;
    private static final int SLOT_CLEAR     = 34;
    private static final int SLOT_LOCK      = 35;
    private static final int SLOT_BACK      = 45;
    private static final int SLOT_HEADER    = 4;

    // ── Category definitions ─────────────────────────────────────
    private enum Category {
        SWORDS_T1    ("⚔ Bounty Swords (T1)",               Items.IRON_SWORD,         Formatting.YELLOW),
        SWORDS_T2    ("⚔ Bounty Swords (T2)",               Items.DIAMOND_SWORD,      Formatting.AQUA),
        ARMOR_T1     ("🛡 Bounty Armor T1",                  Items.IRON_CHESTPLATE,    Formatting.GREEN),
        ARMOR_T2     ("🛡 Bounty Armor T2",                  Items.DIAMOND_CHESTPLATE, Formatting.DARK_AQUA),
        SPECIAL_ARMOR("🛡 Special / Legendary Armor",        Items.NETHERITE_HELMET,   Formatting.GOLD),
        HPEBM        ("⚡ HPEBM Weapons",                    Items.BLAZE_ROD,          Formatting.LIGHT_PURPLE),
        SPECIAL_ITEMS("🎁 Special Items",                    Items.STICK,              Formatting.GREEN),
        PIGLIN       ("👑 Piglin Items",                     Items.GOLDEN_HELMET,      Formatting.GOLD),
        GOLD_ARMOR   ("👑 Gold Armor Evolution",             Items.GOLDEN_CHESTPLATE,  Formatting.GOLD),
        MATERIALS    ("📦 Materials (Cores & Chunks)",       Items.SLIME_BALL,         Formatting.GRAY),
        ATTRIBUTE_TOKENS("✧ Attribute Tokens",               Items.PAPER,              Formatting.LIGHT_PURPLE);

        final String label;
        final net.minecraft.item.Item icon;
        final Formatting colour;

        Category(String label, net.minecraft.item.Item icon, Formatting colour) {
            this.label = label;
            this.icon  = icon;
            this.colour = colour;
        }
    }

    /** Returns all items belonging to a given category. */
    private static List<ItemStack> getItemsForCategory(Category cat) {
        List<ItemStack> items = new ArrayList<>();
        switch (cat) {
            case SWORDS_T1 -> {
                for (SlayerManager.SlayerType t : SlayerManager.SlayerType.values())
                    items.add(SlayerItems.createSlayerSword(t));
            }
            case SWORDS_T2 -> {
                for (SlayerManager.SlayerType t : SlayerManager.SlayerType.values())
                    items.add(SlayerItems.createUpgradedSlayerSword(t));
            }
            case ARMOR_T1 -> {
                for (SlayerManager.SlayerType t : SlayerManager.SlayerType.values()) {
                    items.add(SlayerItems.createSlayerHelmet(t, 1));
                    items.add(SlayerItems.createSlayerChestplate(t, 1));
                    items.add(SlayerItems.createSlayerLeggings(t, 1));
                    items.add(SlayerItems.createSlayerBoots(t, 1));
                }
            }
            case ARMOR_T2 -> {
                for (SlayerManager.SlayerType t : SlayerManager.SlayerType.values()) {
                    items.add(SlayerItems.createSlayerHelmet(t, 2));
                    items.add(SlayerItems.createSlayerChestplate(t, 2));
                    items.add(SlayerItems.createSlayerLeggings(t, 2));
                    items.add(SlayerItems.createSlayerBoots(t, 2));
                }
            }
            case SPECIAL_ARMOR -> {
                items.add(SlayerItems.createZombieBerserkerHelmet());
                items.add(SlayerItems.createSpiderLeggings());
                items.add(SlayerItems.createSkeletonBow());
                items.add(SlayerItems.createSlimeBoots());
                items.add(SlayerItems.createWardenChestplate());
                items.add(SlayerItems.createVoidwalkerCrown());
            }
            case HPEBM -> {
                for (int mk = 1; mk <= 5; mk++)
                    items.add(CustomItemHandler.createHPEBM(mk));
                items.add(CustomItemHandler.createUltraOverclockedBeam());
            }
            case SPECIAL_ITEMS -> {
                items.add(CustomItemHandler.createTheGavel());
                items.add(CustomItemHandler.createHarveysStick());
                items.add(CustomItemHandler.createHermesShoes());
                
                // NEW BOSS DROPS
                items.add(SlayerItems.createUndeadHeart());
                items.add(SlayerItems.createSpectralQuiver());
                items.add(SlayerItems.createEchoingCore());
                items.add(SlayerItems.createEnderSword());
                items.add(SlayerItems.createAbyssalBlade());
                items.add(SlayerItems.createBouncySlime());
                items.add(SlayerItems.createVenomousDagger());
            }
            case PIGLIN -> {
                items.add(SlayerItems.createCrownOfGreed());
                items.add(SlayerItems.createCrownOfMidas());
                items.add(SlayerItems.createMidasSword());
                items.add(SlayerItems.createPiglinCore());
                items.add(SlayerItems.createPiglinFlesh());
            }
            case GOLD_ARMOR -> {
                // Pure Gold Armor (Tier 1)
                items.add(SlayerItems.createPureGoldHelmet());
                items.add(SlayerItems.createPureGoldChestplate());
                items.add(SlayerItems.createPureGoldLeggings());
                items.add(SlayerItems.createPureGoldBoots());
                
                // Polished Gold Armor (Tier 2)
                items.add(SlayerItems.createPolishedGoldHelmet());
                items.add(SlayerItems.createPolishedGoldChestplate());
                items.add(SlayerItems.createPolishedGoldLeggings());
                items.add(SlayerItems.createPolishedGoldBoots());
                
                // Shiny Gold Armor (Tier 3)
                items.add(SlayerItems.createShinyGoldHelmet());
                items.add(SlayerItems.createShinyGoldChestplate());
                items.add(SlayerItems.createShinyGoldLeggings());
                items.add(SlayerItems.createShinyGoldBoots());
                
                // Glistening Gold Armor (Tier 4)
                items.add(SlayerItems.createGlisteningGoldHelmet());
                items.add(SlayerItems.createGlisteningGoldChestplate());
                items.add(SlayerItems.createGlisteningGoldLeggings());
                items.add(SlayerItems.createGlisteningGoldBoots());
                
                // Gilded Armor (Tier 5)
                items.add(SlayerItems.createGildedHelmet());
                items.add(SlayerItems.createGildedChestplate());
                items.add(SlayerItems.createGildedLeggings());
                items.add(SlayerItems.createGildedBoots());
                
                // Add enchanted blackstone for gilded armor crafting
                items.add(SlayerItems.createEnchantedBlackstone());
                items.add(SlayerItems.createEnchantedGildedBlackstone());
                
                // Add gilded netherite armor (ultimate tier)
                items.add(SlayerItems.createGildedNetheriteHelmet());
                items.add(SlayerItems.createGildedNetheriteChestplate());
                items.add(SlayerItems.createGildedNetheriteLeggings());
                items.add(SlayerItems.createGildedNetheriteBoots());
            }
            case MATERIALS -> {
                for (SlayerManager.SlayerType t : SlayerManager.SlayerType.values()) {
                    items.add(SlayerItems.createCore(t));
                    items.add(SlayerItems.createChunk(t));
                }
                // Add attribute cores
                for (com.political.ArmourAttribute attr : com.political.ArmourAttribute.values()) {
                    items.add(SlayerItems.createAttributeCore(attr));
                }
                // Add compacted materials
                items.add(SlayerItems.createCompactedIron());
                items.add(SlayerItems.createCompactedGold());
                items.add(SlayerItems.createCompactedDiamond());
                items.add(SlayerItems.createCompactedEmerald());
                items.add(SlayerItems.createCompactedNetherite());
                items.add(SlayerItems.createCompactedCopper());
                items.add(SlayerItems.createCompactedCoal());
                items.add(SlayerItems.createCompactedLapis());
                items.add(SlayerItems.createCompactedRedstone());
                items.add(SlayerItems.createCompactedQuartz());
                
                // Add enchanted compacted materials
                items.add(SlayerItems.createCompactedIron());
                items.add(SlayerItems.createCompactedGold());
                items.add(SlayerItems.createCompactedDiamond());
                items.add(SlayerItems.createCompactedEmerald());
                items.add(SlayerItems.createCompactedNetherite());
                items.add(SlayerItems.createCompactedCopper());
                items.add(SlayerItems.createCompactedCoal());
                items.add(SlayerItems.createCompactedLapis());
                items.add(SlayerItems.createCompactedRedstone());
                items.add(SlayerItems.createCompactedQuartz());
                
                // Add super compacted ores
                items.add(SlayerItems.createSuperCompactedGold());
                items.add(SlayerItems.createSuperCompactedIron());
                items.add(SlayerItems.createSuperCompactedDiamond());
                items.add(SlayerItems.createSuperCompactedEmerald());
                items.add(SlayerItems.createSuperCompactedNetherite());
                
                // Add enchanted blackstone for gilded armor
                items.add(SlayerItems.createEnchantedBlackstone());
            }
            case ATTRIBUTE_TOKENS -> {
                // Add all 10 attribute tokens
                items.add(SlayerItems.createAttributeToken(com.political.ArmourAttribute.BURNING));
                items.add(SlayerItems.createAttributeToken(com.political.ArmourAttribute.SIGHTLESS));
                items.add(SlayerItems.createAttributeToken(com.political.ArmourAttribute.FRENZIED));
                items.add(SlayerItems.createAttributeToken(com.political.ArmourAttribute.GROUNDED));
                items.add(SlayerItems.createAttributeToken(com.political.ArmourAttribute.WEBBED));
                items.add(SlayerItems.createAttributeToken(com.political.ArmourAttribute.FROST));
                items.add(SlayerItems.createAttributeToken(com.political.ArmourAttribute.PHANTOMSTEP));
                items.add(SlayerItems.createAttributeToken(com.political.ArmourAttribute.CURSED));
                items.add(SlayerItems.createAttributeToken(com.political.ArmourAttribute.OVERGROWN));
                items.add(SlayerItems.createAttributeToken(com.political.ArmourAttribute.VOLATILE));
            }
        }
        return items;
    }


    // ──────────────────────────────────────────────────────────────
    // Page 0: Main Category Menu
    // ──────────────────────────────────────────────────────────────

    /** Entry point — opens the main category selection menu. */
    public static void openMainMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
        gui.setTitle(Text.literal("§6§l/setrecipe — Choose Category"));

        GuiElementBuilder bg = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE).setName(Text.literal(""));
        for (int i = 0; i < 27; i++) gui.setSlot(i, bg.build());

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.KNOWLEDGE_BOOK)
                .setName(Text.literal("§6§l/setrecipe").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal("§7Select a category to view and edit recipes"))
                .glow().build());

        // Place category buttons (skip slot 4 = header)
        Category[] cats = Category.values();
        int[] catSlots = {0, 1, 2, 3, 5, 6, 7, 8};
        for (int i = 0; i < cats.length && i < catSlots.length; i++) {
            Category cat = cats[i];
            int slot = catSlots[i];
            gui.setSlot(slot, new GuiElementBuilder(cat.icon)
                    .setName(Text.literal("§l" + cat.label).formatted(cat.colour, Formatting.BOLD))
                    .addLoreLine(Text.literal("§7Click to browse items"))
                    .setCallback((idx, type, action) -> openCategoryPage(player, cat, 0))
                    .build());
        }

        // Row 2: More categories
        int[] row2Slots = {9, 10, 11, 12, 13, 14, 15, 16, 17};
        int extra = cats.length - catSlots.length;
        for (int i = 0; i < extra; i++) {
            Category cat = cats[catSlots.length + i];
            gui.setSlot(row2Slots[i], new GuiElementBuilder(cat.icon)
                    .setName(Text.literal("§l" + cat.label).formatted(cat.colour, Formatting.BOLD))
                    .addLoreLine(Text.literal("§7Click to browse items"))
                    .setCallback((idx, type, action) -> openCategoryPage(player, cat, 0))
                    .build());
        }

        gui.open();
    }

    // ──────────────────────────────────────────────────────────────
    // Page 1: Category Item List
    // ──────────────────────────────────────────────────────────────

    public static void openCategoryPage(ServerPlayerEntity player, Category cat, int page) {
        List<SlayerRecipes.Recipe> existingRecipes = RecipeConfigManager.getRecipes();
        List<ItemStack> items = getItemsForCategory(cat);

        final int ITEMS_PER_PAGE = 28;
        int totalPages = Math.max(1, (items.size() + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE);
        int clampedPage = Math.min(page, totalPages - 1);

        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("§6" + cat.label));

        GuiElementBuilder bg = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE).setName(Text.literal(""));
        for (int i = 0; i < 54; i++) gui.setSlot(i, bg.build());

        // Header
        gui.setSlot(4, new GuiElementBuilder(cat.icon)
                .setName(Text.literal("§l" + cat.label).formatted(cat.colour, Formatting.BOLD))
                .addLoreLine(Text.literal("§7Click an item to edit its recipe"))
                .glow().build());

        // Item slots (7 per row, rows 1-4, columns 1-7)
        int start = clampedPage * ITEMS_PER_PAGE;
        int shown = 0;
        for (int idx = start; idx < items.size() && shown < ITEMS_PER_PAGE; idx++) {
            ItemStack item = items.get(idx);
            int row = shown / 7;
            int col = shown % 7 + 1;
            int slot = row * 9 + col;

            Text displayName = item.get(DataComponentTypes.CUSTOM_NAME);
            String customId = SlayerItems.getCustomItemId(item);

            // Check for existing recipe
            boolean hasRecipe = false;
            SlayerRecipes.Recipe existingRecipe = null;
            if (customId != null) {
                for (SlayerRecipes.Recipe r : existingRecipes) {
                    if (customId.equals(SlayerItems.getCustomItemId(r.result))) {
                        hasRecipe = true;
                        existingRecipe = r;
                        break;
                    }
                }
            }

            final ItemStack finalItem = item;
            final SlayerRecipes.Recipe finalRecipe = existingRecipe;
            final String finalRecipeName = existingRecipe != null ? existingRecipe.name : null;
            final boolean finalHasRecipe = hasRecipe;
            final boolean recipeEnabled = finalRecipeName != null && RecipeConfigManager.isRecipeEnabled(finalRecipeName);

            GuiElementBuilder elem = new GuiElementBuilder(item.getItem())
                    .setName(displayName != null ? displayName
                            : Text.literal("§e" + (customId != null ? customId : "Unknown")))
                    .addLoreLine(Text.literal(""));
            if (finalHasRecipe) {
                elem.addLoreLine(recipeEnabled
                        ? Text.literal("§a✔ Recipe ENABLED")
                        : Text.literal("§c✘ Recipe DISABLED"));
                elem.addLoreLine(Text.literal("§7Left-click: edit  |  Right-click: toggle on/off"));
            } else {
                elem.addLoreLine(Text.literal("§e➕ No recipe — click to create"));
            }
            elem.setCallback((i, t, a) -> {
                if (finalHasRecipe && t == eu.pb4.sgui.api.ClickType.MOUSE_RIGHT) {
                    boolean nowEnabled = RecipeConfigManager.toggleRecipe(finalRecipeName);
                    String status = nowEnabled ? "§aENABLED" : "§cDISABLED";
                    player.sendMessage(Text.literal("Recipe '" + finalRecipeName + "' is now " + status), false);
                    openCategoryPage(player, cat, clampedPage);
                } else {
                    openRecipeEditor(player, finalItem, finalRecipe, cat);
                }
            });
            gui.setSlot(slot, elem.build());
            shown++;
        }

        // ← Back to main menu
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to Categories").formatted(Formatting.YELLOW))
                .setCallback((i, t, a) -> openMainMenu(player)).build());

        // Pagination
        if (clampedPage > 0) {
            final int prev = clampedPage - 1;
            gui.setSlot(46, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("← Previous Page").formatted(Formatting.YELLOW))
                    .addLoreLine(Text.literal("§7Page " + clampedPage + " / " + totalPages))
                    .setCallback((i, t, a) -> openCategoryPage(player, cat, prev)).build());
        }
        if (clampedPage < totalPages - 1) {
            final int next = clampedPage + 1;
            gui.setSlot(53, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("Next Page →").formatted(Formatting.YELLOW))
                    .addLoreLine(Text.literal("§7Page " + (clampedPage + 2) + " / " + totalPages))
                    .setCallback((i, t, a) -> openCategoryPage(player, cat, next)).build());
        }

        gui.setSlot(49, new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal("§7Page " + (clampedPage + 1) + " / " + totalPages))
                .build());

        gui.open();
    }

    // ──────────────────────────────────────────────────────────────
    // Page 2: Recipe Editor  (unchanged core logic)
    // ──────────────────────────────────────────────────────────────

    /**
     * Opens the editable recipe grid for the given result item.
     *
     * @param player     the admin
     * @param resultItem the item the recipe produces
     * @param existing   an existing recipe to pre-populate (may be null)
     * @param backCat    the category to return to when ← Back is clicked (may be null → opens main menu)
     */
    public static void openRecipeEditor(ServerPlayerEntity player, ItemStack resultItem,
                                         SlayerRecipes.Recipe existing, Category backCat) {
        ItemStack[] grid = new ItemStack[9];
        boolean[] lockedSlots = new boolean[9]; // Track which slots are locked
        for (int i = 0; i < 9; i++) {
            grid[i] = (existing != null && existing.ingredients != null && existing.ingredients[i] != null)
                    ? existing.ingredients[i].copy() : ItemStack.EMPTY;
            lockedSlots[i] = false; // Start with all slots unlocked
        }

        String resultCustomId = SlayerItems.getCustomItemId(resultItem);

        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false) {
            @Override
            public boolean onAnyClick(int index, ClickType type, SlotActionType action) {
                for (int g = 0; g < 9; g++) {
                    if (index == GRID_SLOTS[g]) {
                        // Handle middle-click to toggle lock
                        if (type == ClickType.MOUSE_MIDDLE) {
                            lockedSlots[g] = !lockedSlots[g];
                            String status = lockedSlots[g] ? "§aLOCKED" : "§eUNLOCKED";
                            player.sendMessage(Text.literal("Slot " + (g + 1) + " is now " + status), false);
                            refreshGrid(this, grid, resultItem, lockedSlots);
                            return true;
                        }
                        
                        // Prevent modification if slot is locked
                        if (lockedSlots[g]) {
                            player.sendMessage(Text.literal("§cThis slot is locked! Use middle-click to unlock."), false);
                            return true;
                        }
                        
                        ItemStack cursor = player.currentScreenHandler.getCursorStack();
                        if (!cursor.isEmpty()) {
                            // Allow full count for compacted materials and other items
                            grid[g] = cursor.copy();
                        } else {
                            grid[g] = ItemStack.EMPTY;
                        }
                        refreshGrid(this, grid, resultItem, lockedSlots);
                        return true;
                    }
                }
                if (index == SLOT_SAVE) {
                    saveRecipe(player, resultItem, grid, lockedSlots);
                    player.closeHandledScreen();
                    return true;
                }
                if (index == SLOT_CLEAR) {
                    for (int g = 0; g < 9; g++) {
                        if (!lockedSlots[g]) { // Only clear unlocked slots
                            grid[g] = ItemStack.EMPTY;
                        }
                    }
                    refreshGrid(this, grid, resultItem, lockedSlots);
                    return true;
                }
                if (index == SLOT_LOCK) {
                    // Toggle all slots lock state
                    boolean allLocked = true;
                    for (boolean locked : lockedSlots) {
                        if (!locked) {
                            allLocked = false;
                            break;
                        }
                    }
                    
                    // If all are locked, unlock all. Otherwise, lock all.
                    for (int g = 0; g < 9; g++) {
                        lockedSlots[g] = !allLocked;
                    }
                    
                    String status = allLocked ? "§eAll slots UNLOCKED" : "§aAll slots LOCKED";
                    player.sendMessage(Text.literal(status), false);
                    refreshGrid(this, grid, resultItem, lockedSlots);
                    return true;
                }
                if (index == SLOT_BACK) {
                    player.closeHandledScreen();
                    if (backCat != null) openCategoryPage(player, backCat, 0);
                    else openMainMenu(player);
                    return true;
                }
                return super.onAnyClick(index, type, action);
            }
        };

        Text resultName = resultItem.get(DataComponentTypes.CUSTOM_NAME);
        gui.setTitle(Text.literal("§6Edit Recipe: §e")
                .append(resultName != null ? resultName
                        : Text.literal(resultCustomId != null ? resultCustomId : "?")));

        GuiElementBuilder bg = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE).setName(Text.literal(""));
        for (int i = 0; i < 54; i++) gui.setSlot(i, bg.build());

        gui.setSlot(SLOT_HEADER, new GuiElementBuilder(Items.WRITABLE_BOOK)
                .setName(Text.literal("§6Recipe Editor"))
                .addLoreLine(Text.literal("§7Hold an item and left-click a grid slot to set it"))
                .addLoreLine(Text.literal("§7Left-click empty cursor on a slot to clear it"))
                .build());

        gui.setSlot(SLOT_ARROW, new GuiElementBuilder(Items.SPECTRAL_ARROW)
                .setName(Text.literal("→").formatted(Formatting.YELLOW)).build());

        GuiElementBuilder res = new GuiElementBuilder(resultItem.getItem())
                .setName(resultName != null ? resultName
                        : Text.literal(resultCustomId != null ? resultCustomId : "?"))
                .glow();
        gui.setSlot(SLOT_RESULT, res.build());

        gui.setSlot(SLOT_SAVE, new GuiElementBuilder(Items.LIME_DYE)
                .setName(Text.literal("§a§lSave Recipe"))
                .addLoreLine(Text.literal("§7Click to save this recipe."))
                .glow().build());

        gui.setSlot(SLOT_CLEAR, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("§c§lClear Grid"))
                .addLoreLine(Text.literal("§7Click to clear all ingredient slots."))
                .addLoreLine(Text.literal("§7Locked slots will not be cleared."))
                .build());

        gui.setSlot(SLOT_LOCK, new GuiElementBuilder(Items.TRIPWIRE_HOOK)
                .setName(Text.literal("§6§lToggle All Locks"))
                .addLoreLine(Text.literal("§7Click to lock/unlock all slots."))
                .addLoreLine(Text.literal("§7Middle-click individual slots to toggle."))
                .build());

        gui.setSlot(SLOT_BACK, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal(backCat != null ? "§7Return to " + backCat.label : "§7Return to main menu"))
                .build());

        // Initial grid display
        refreshGrid(gui, grid, resultItem, lockedSlots);
        gui.open();
    }

    /** Convenience overload — back goes to main menu. */
    public static void openRecipeEditor(ServerPlayerEntity player, ItemStack resultItem,
                                         SlayerRecipes.Recipe existing) {
        openRecipeEditor(player, resultItem, existing, null);
    }

    /** Re-draws the 3×3 grid slots. */
    private static void refreshGrid(SimpleGui gui, ItemStack[] grid, ItemStack resultItem, boolean[] lockedSlots) {
        for (int g = 0; g < 9; g++) {
            ItemStack ing = grid[g];
            boolean isLocked = lockedSlots != null && lockedSlots[g];
            
            if (ing == null || ing.isEmpty()) {
                GuiElementBuilder builder = new GuiElementBuilder(
                    isLocked ? Items.RED_STAINED_GLASS_PANE : Items.LIGHT_GRAY_STAINED_GLASS_PANE)
                        .setName(Text.literal(isLocked ? "§c[Locked — middle-click to unlock]" : "§8[Empty — click with item to set]"));
                
                if (isLocked) {
                    builder.addLoreLine(Text.literal("§7Slot is locked"))
                            .addLoreLine(Text.literal("§7Middle-click to unlock"));
                } else {
                    builder.addLoreLine(Text.literal("§7Middle-click to lock slot"));
                }
                
                gui.setSlot(GRID_SLOTS[g], builder.build());
            } else {
                Text ingName = ing.get(DataComponentTypes.CUSTOM_NAME);
                GuiElementBuilder builder = new GuiElementBuilder(ing.getItem())
                        .setName(ingName != null ? ingName : Text.of(ing.getItem().toString()));
                
                // Add lock status to lore
                if (isLocked) {
                    builder.addLoreLine(Text.literal("§c§lLOCKED SLOT"));
                    builder.addLoreLine(Text.literal("§7Middle-click to unlock"));
                } else {
                    builder.addLoreLine(Text.literal("§7Click with item to replace, empty-hand to clear"));
                    builder.addLoreLine(Text.literal("§7Middle-click to lock slot"));
                }
                
                // Show count if more than 1
                if (ing.getCount() > 1) {
                    builder.setCount(ing.getCount());
                    builder.addLoreLine(Text.literal("§7Amount: " + ing.getCount()).formatted(Formatting.YELLOW));
                }
                
                // Add red dye overlay for locked slots
                if (isLocked) {
                    builder.glow();
                }
                
                gui.setSlot(GRID_SLOTS[g], builder.build());
            }
        }
    }

    /** Saves the recipe to RecipeConfigManager. */
    private static void saveRecipe(ServerPlayerEntity player, ItemStack resultItem, ItemStack[] grid, boolean[] lockedSlots) {
        Text nameTxt = resultItem.get(DataComponentTypes.CUSTOM_NAME);
        String recipeName = nameTxt != null ? nameTxt.getString() : "Custom Recipe";

        String resultId = SlayerItems.getCustomItemId(resultItem);
        RecipeConfigManager.removeCustomRecipe(recipeName);
        if (resultId != null) {
            for (SlayerRecipes.Recipe existing : new ArrayList<>(RecipeConfigManager.getCustomRecipes())) {
                if (resultId.equals(SlayerItems.getCustomItemId(existing.result))) {
                    RecipeConfigManager.removeCustomRecipe(existing.name);
                }
            }
        }

        ItemStack[] ingredientsCopy = new ItemStack[9];
        for (int i = 0; i < 9; i++) {
            ingredientsCopy[i] = (grid[i] == null || grid[i].isEmpty()) ? ItemStack.EMPTY : grid[i].copy();
        }

        SlayerRecipes.Recipe newRecipe = new SlayerRecipes.Recipe(
                recipeName, resultItem.copy(), ingredientsCopy, lockedSlots, null, 0);
        RecipeConfigManager.addCustomRecipe(newRecipe);
        player.sendMessage(Text.literal("§a✔ Recipe saved for §e" + recipeName + "§a!"), false);
    }

    // ──────────────────────────────────────────────────────────────
    // Legacy public entry-points (backwards compatibility)
    // ──────────────────────────────────────────────────────────────

    /** Opens the main category menu. Called by /setrecipe command. */
    public static void openRecipeList(ServerPlayerEntity player) {
        openMainMenu(player);
    }

    /** Retained for backward-compat — now opens the main menu (page is ignored). */
    public static void openRecipeList(ServerPlayerEntity player, int page) {
        openMainMenu(player);
    }

    // ──────────────────────────────────────────────────────────────
    // Legacy: read-only recipe view (still used by /recipes GUI)
    // ──────────────────────────────────────────────────────────────

    public static void openRecipeView(ServerPlayerEntity player, SlayerRecipes.Recipe recipe) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X5, player, false);
        gui.setTitle(Text.literal("§6Recipe: §e" + recipe.name));

        GuiElementBuilder bg = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE).setName(Text.literal(""));
        for (int i = 0; i < 45; i++) gui.setSlot(i, bg.build());

        Text resultName = recipe.result.get(DataComponentTypes.CUSTOM_NAME);
        gui.setSlot(SLOT_HEADER, new GuiElementBuilder(Items.KNOWLEDGE_BOOK)
                .setName(resultName != null ? resultName
                        : Text.literal(recipe.name).formatted(Formatting.GOLD))
                .glow().build());

        int[] viewGrid = {10, 11, 12, 19, 20, 21, 28, 29, 30};
        for (int i = 0; i < 9; i++) {
            ItemStack ing = (recipe.ingredients != null && i < recipe.ingredients.length)
                    ? recipe.ingredients[i] : null;
            if (ing != null && !ing.isEmpty()) {
                GuiElementBuilder eb = new GuiElementBuilder(ing.getItem());
                Text ingName = ing.get(DataComponentTypes.CUSTOM_NAME);
                if (ingName != null) eb.setName(ingName);
                if (ing.getCount() > 1) eb.setCount(ing.getCount());
                gui.setSlot(viewGrid[i], eb.build());
            }
        }

        gui.setSlot(23, new GuiElementBuilder(Items.SPECTRAL_ARROW)
                .setName(Text.literal("→").formatted(Formatting.YELLOW)).build());
        if (!recipe.result.isEmpty()) {
            GuiElementBuilder res = new GuiElementBuilder(recipe.result.getItem());
            Text rName = recipe.result.get(DataComponentTypes.CUSTOM_NAME);
            if (rName != null) res.setName(rName);
            res.glow();
            gui.setSlot(24, res.build());
        }

        String slayerName = recipe.requiredSlayer != null ? recipe.requiredSlayer.displayName : "Any";
        gui.setSlot(34, new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal("§eRecipe Info"))
                .addLoreLine(Text.literal("§7Type: §f" + slayerName))
                .addLoreLine(Text.literal("§7Required Level: §f" + recipe.requiredLevel)).build());

        gui.setSlot(36, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openMainMenu(player)).build());

        gui.open();
    }
}
