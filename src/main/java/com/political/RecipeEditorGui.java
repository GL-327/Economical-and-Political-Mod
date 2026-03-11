package com.political;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.component.DataComponentTypes;
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
 * <ul>
 *   <li>Page 1 – Recipe list: shows all custom items. Click one to open the editor.</li>
 *   <li>Page 2 – Recipe editor: 3×3 ingredient grid + result slot + Save button.
 *       The admin clicks a grid slot, then the item from their cursor (main inventory
 *       interaction) is recorded as the ingredient for that slot.</li>
 * </ul>
 */
public class RecipeEditorGui {

    // ── Layout constants (9×6 chest) ────────────────────────────
    private static final int[] GRID_SLOTS   = {10, 11, 12, 19, 20, 21, 28, 29, 30};
    private static final int SLOT_ARROW     = 23;
    private static final int SLOT_RESULT    = 24;
    private static final int SLOT_SAVE      = 33;
    private static final int SLOT_CLEAR     = 34;
    private static final int SLOT_BACK      = 45;
    private static final int SLOT_HEADER    = 4;

    // ── All custom items that can be given a recipe ─────────────
    private static List<ItemStack> getAllCustomItems() {
        List<ItemStack> items = new ArrayList<>();
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            items.add(SlayerItems.createSlayerSword(type));
            items.add(SlayerItems.createUpgradedSlayerSword(type));
            // T1 armor pieces
            items.add(SlayerItems.createSlayerHelmet(type, 1));
            items.add(SlayerItems.createSlayerChestplate(type, 1));
            items.add(SlayerItems.createSlayerLeggings(type, 1));
            items.add(SlayerItems.createSlayerBoots(type, 1));
            // T2 armor pieces
            items.add(SlayerItems.createSlayerHelmet(type, 2));
            items.add(SlayerItems.createSlayerChestplate(type, 2));
            items.add(SlayerItems.createSlayerLeggings(type, 2));
            items.add(SlayerItems.createSlayerBoots(type, 2));
            // Cores and chunks
            items.add(SlayerItems.createCore(type));
            items.add(SlayerItems.createChunk(type));
        }
        // Legendary armor
        items.add(SlayerItems.createZombieBerserkerHelmet());
        items.add(SlayerItems.createSpiderLeggings());
        items.add(SlayerItems.createSkeletonBow());
        items.add(SlayerItems.createSlimeBoots());
        items.add(SlayerItems.createWardenChestplate());
        // Piglin items
        items.add(SlayerItems.createCrownOfGreed());
        items.add(SlayerItems.createPiglinCore());
        items.add(SlayerItems.createPiglinFlesh());
        // Special weapons
        items.add(CustomItemHandler.createTheGavel());
        items.add(CustomItemHandler.createGavel());
        items.add(CustomItemHandler.createHarveysStick());
        items.add(CustomItemHandler.createHermesShoes());
        items.add(CustomItemHandler.createHPEBM(1));
        items.add(CustomItemHandler.createHPEBM(2));
        items.add(CustomItemHandler.createHPEBM(3));
        items.add(CustomItemHandler.createHPEBM(4));
        items.add(CustomItemHandler.createHPEBM(5));
        items.add(CustomItemHandler.createUltraOverclockedBeam());
        return items;
    }

    // ────────────────────────────────────────────────────────────
    // Page 1: Recipe list
    // ────────────────────────────────────────────────────────────

    public static void openRecipeList(ServerPlayerEntity player) {
        openRecipeList(player, 0);
    }

    public static void openRecipeList(ServerPlayerEntity player, int page) {
        List<SlayerRecipes.Recipe> existingRecipes = RecipeConfigManager.getRecipes();
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("§6§l/setrecipe — Recipe Editor"));

        GuiElementBuilder bg = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE).setName(Text.literal(""));
        for (int i = 0; i < 54; i++) gui.setSlot(i, bg.build());

        // Header
        gui.setSlot(SLOT_HEADER, new GuiElementBuilder(Items.KNOWLEDGE_BOOK)
                .setName(Text.literal("§6§l/setrecipe — Recipe Editor"))
                .addLoreLine(Text.literal("§7Click a custom item to edit its recipe"))
                .addLoreLine(Text.literal("§7Or click an existing recipe to view/edit"))
                .glow().build());

        // Show all custom items (that could have a recipe set)
        List<ItemStack> allItems = getAllCustomItems();
        final int ITEMS_PER_PAGE = 28;
        int totalPages = (allItems.size() + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE;
        int clampedPage = Math.min(page, totalPages - 1);

        int start = clampedPage * ITEMS_PER_PAGE;
        int shown = 0;
        for (int idx = start; idx < allItems.size() && shown < ITEMS_PER_PAGE; idx++) {
            ItemStack item = allItems.get(idx);
            int row = shown / 7;
            int col = shown % 7 + 1;
            int slot = row * 9 + col;

            Text displayName = item.get(DataComponentTypes.CUSTOM_NAME);
            String customId = SlayerItems.getCustomItemId(item);

            // Check if this item already has a custom recipe
            boolean hasRecipe = false;
            for (SlayerRecipes.Recipe r : existingRecipes) {
                if (SlayerItems.getCustomItemId(r.result) != null
                        && SlayerItems.getCustomItemId(r.result).equals(customId)) {
                    hasRecipe = true;
                    break;
                }
            }

            final ItemStack finalItem = item;
            final SlayerRecipes.Recipe existingRecipe;
            // Find existing recipe for this item to pre-populate
            SlayerRecipes.Recipe found = null;
            if (customId != null) {
                for (SlayerRecipes.Recipe r : existingRecipes) {
                    if (customId.equals(SlayerItems.getCustomItemId(r.result))) {
                        found = r;
                        break;
                    }
                }
            }
            existingRecipe = found;
            GuiElementBuilder eb = new GuiElementBuilder(item.getItem())
                    .setName(displayName != null ? displayName : Text.literal("§e" + (customId != null ? customId : "Unknown")))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(hasRecipe
                            ? Text.literal("§a✔ Has recipe — click to edit").formatted(Formatting.GREEN)
                            : Text.literal("§e➕ No recipe — click to create").formatted(Formatting.YELLOW))
                    .setCallback((index, clickType, actionType) -> openRecipeEditor(player, finalItem, existingRecipe));
            gui.setSlot(slot, eb.build());
            shown++;
        }

        // Pagination buttons
        if (clampedPage > 0) {
            final int prevPage = clampedPage - 1;
            gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("← Previous Page").formatted(Formatting.YELLOW))
                    .addLoreLine(Text.literal("§7Page " + clampedPage + " / " + totalPages))
                    .setCallback((idx, t, a) -> openRecipeList(player, prevPage)).build());
        }
        if (clampedPage < totalPages - 1) {
            final int nextPage = clampedPage + 1;
            gui.setSlot(53, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("Next Page →").formatted(Formatting.YELLOW))
                    .addLoreLine(Text.literal("§7Page " + (clampedPage + 2) + " / " + totalPages))
                    .setCallback((idx, t, a) -> openRecipeList(player, nextPage)).build());
        }

        // Page indicator
        gui.setSlot(49, new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal("§7Page " + (clampedPage + 1) + " / " + totalPages))
                .build());

        gui.open();
    }

    // ────────────────────────────────────────────────────────────
    // Page 2: Recipe editor
    // ────────────────────────────────────────────────────────────

    /**
     * Opens the editable recipe grid for the given result item.
     *
     * @param player     the admin
     * @param resultItem the item the recipe produces
     * @param existing   an existing recipe to pre-populate (may be null)
     */
    public static void openRecipeEditor(ServerPlayerEntity player, ItemStack resultItem,
                                         SlayerRecipes.Recipe existing) {
        // Working copies of the 9-slot grid
        ItemStack[] grid = new ItemStack[9];
        for (int i = 0; i < 9; i++) {
            grid[i] = (existing != null && existing.ingredients != null && existing.ingredients[i] != null)
                    ? existing.ingredients[i].copy() : ItemStack.EMPTY;
        }

        String resultCustomId = SlayerItems.getCustomItemId(resultItem);

        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false) {
            @Override
            public boolean onAnyClick(int index, ClickType type, SlotActionType action) {
                // Check if the admin clicked a grid slot
                for (int g = 0; g < 9; g++) {
                    if (index == GRID_SLOTS[g]) {
                        ItemStack cursor = player.currentScreenHandler.getCursorStack();
                        if (!cursor.isEmpty()) {
                            // Place a single copy of the cursor item as the ingredient
                            grid[g] = cursor.copyWithCount(1);
                        } else {
                            // Clear the slot
                            grid[g] = ItemStack.EMPTY;
                        }
                        refreshGrid(this, grid, resultItem);
                        return true;
                    }
                }
                // Save button
                if (index == SLOT_SAVE) {
                    saveRecipe(player, resultItem, grid);
                    player.closeHandledScreen();
                    return true;
                }
                // Clear grid button
                if (index == SLOT_CLEAR) {
                    for (int g = 0; g < 9; g++) grid[g] = ItemStack.EMPTY;
                    refreshGrid(this, grid, resultItem);
                    return true;
                }
                // Back button
                if (index == SLOT_BACK) {
                    player.closeHandledScreen();
                    openRecipeList(player);
                    return true;
                }
                return super.onAnyClick(index, type, action);
            }
        };

        Text resultName = resultItem.get(DataComponentTypes.CUSTOM_NAME);
        gui.setTitle(Text.literal("§6Edit Recipe: §e")
                .append(resultName != null ? resultName : Text.literal(resultCustomId != null ? resultCustomId : "?")));

        // Background
        GuiElementBuilder bg = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE).setName(Text.literal(""));
        for (int i = 0; i < 54; i++) gui.setSlot(i, bg.build());

        // Header
        gui.setSlot(SLOT_HEADER, new GuiElementBuilder(Items.WRITABLE_BOOK)
                .setName(Text.literal("§6Recipe Editor"))
                .addLoreLine(Text.literal("§7Left-click a grid slot while holding an item"))
                .addLoreLine(Text.literal("§7to set it as an ingredient."))
                .addLoreLine(Text.literal("§7Left-click an empty slot to clear it."))
                .build());

        // Arrow
        gui.setSlot(SLOT_ARROW, new GuiElementBuilder(Items.SPECTRAL_ARROW)
                .setName(Text.literal("→").formatted(Formatting.YELLOW))
                .build());

        // Result item (fixed display)
        GuiElementBuilder res = new GuiElementBuilder(resultItem.getItem())
                .setName(resultName != null ? resultName : Text.literal(resultCustomId != null ? resultCustomId : "?"))
                .glow();
        gui.setSlot(SLOT_RESULT, res.build());

        // Save button
        gui.setSlot(SLOT_SAVE, new GuiElementBuilder(Items.LIME_DYE)
                .setName(Text.literal("§a§lSave Recipe").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal("§7Click to save this recipe."))
                .addLoreLine(Text.literal("§7It will be usable at a crafting table."))
                .glow().build());

        // Clear button
        gui.setSlot(SLOT_CLEAR, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("§c§lClear Grid").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal("§7Click to clear all ingredient slots."))
                .build());

        // Back button
        gui.setSlot(SLOT_BACK, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .build());

        // Populate grid with existing ingredients
        refreshGrid(gui, grid, resultItem);

        gui.open();
    }

    /** Re-draws the 3×3 grid slots based on the current grid array. */
    private static void refreshGrid(SimpleGui gui, ItemStack[] grid, ItemStack resultItem) {
        for (int g = 0; g < 9; g++) {
            ItemStack ing = grid[g];
            if (ing == null || ing.isEmpty()) {
                gui.setSlot(GRID_SLOTS[g], new GuiElementBuilder(Items.LIGHT_GRAY_STAINED_GLASS_PANE)
                        .setName(Text.literal("§8[Empty — click with item to set]"))
                        .build());
            } else {
                Text ingName = ing.get(DataComponentTypes.CUSTOM_NAME);
                gui.setSlot(GRID_SLOTS[g], new GuiElementBuilder(ing.getItem())
                        .setName(ingName != null ? ingName : Text.of(ing.getItem().toString()))
                        .addLoreLine(Text.literal("§7Left-click to replace, right-click to clear"))
                        .build());
            }
        }
    }

    /** Saves the recipe to RecipeConfigManager (and to disk). */
    private static void saveRecipe(ServerPlayerEntity player, ItemStack resultItem, ItemStack[] grid) {
        Text nameTxt = resultItem.get(DataComponentTypes.CUSTOM_NAME);
        String recipeName = nameTxt != null ? nameTxt.getString() : "Custom Recipe";

        // Remove existing custom recipe with same name (or same result ID)
        String resultId = SlayerItems.getCustomItemId(resultItem);
        RecipeConfigManager.removeCustomRecipe(recipeName);
        if (resultId != null) {
            // Also remove any recipe whose result has the same custom_item_id
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
                recipeName,
                resultItem.copy(),
                ingredientsCopy,
                null, // no slayer type restriction for custom recipes
                0
        );

        RecipeConfigManager.addCustomRecipe(newRecipe);
        player.sendMessage(Text.literal("§a✔ Recipe saved for §e" + recipeName + "§a!"), false);
    }

    // ────────────────────────────────────────────────────────────
    // Legacy: read-only recipe view (still used by /recipes GUI)
    // ────────────────────────────────────────────────────────────

    public static void openRecipeView(ServerPlayerEntity player, SlayerRecipes.Recipe recipe) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X5, player, false);
        gui.setTitle(Text.literal("§6Recipe: §e" + recipe.name));

        GuiElementBuilder bg = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE).setName(Text.literal(""));
        for (int i = 0; i < 45; i++) gui.setSlot(i, bg.build());

        // Header
        Text resultName = recipe.result.get(DataComponentTypes.CUSTOM_NAME);
        gui.setSlot(SLOT_HEADER, new GuiElementBuilder(Items.KNOWLEDGE_BOOK)
                .setName(resultName != null ? resultName : Text.literal(recipe.name).formatted(Formatting.GOLD))
                .glow().build());

        // 3×3 crafting grid
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

        // Arrow & result
        gui.setSlot(23, new GuiElementBuilder(Items.SPECTRAL_ARROW)
                .setName(Text.literal("→").formatted(Formatting.YELLOW)).build());
        if (!recipe.result.isEmpty()) {
            GuiElementBuilder res = new GuiElementBuilder(recipe.result.getItem());
            Text rName = recipe.result.get(DataComponentTypes.CUSTOM_NAME);
            if (rName != null) res.setName(rName);
            res.glow();
            gui.setSlot(24, res.build());
        }

        // Recipe info
        String slayerName = recipe.requiredSlayer != null ? recipe.requiredSlayer.displayName : "Any";
        gui.setSlot(34, new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal("§eRecipe Info"))
                .addLoreLine(Text.literal("§7Type: §f" + slayerName))
                .addLoreLine(Text.literal("§7Required Level: §f" + recipe.requiredLevel)).build());

        // Back button
        gui.setSlot(36, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openRecipeList(player)).build());

        gui.open();
    }
}
