package com.political;

import com.political.backrooms.items.BackroomsItemsManager;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

/**
 * Recipe Editor GUI for creating and managing custom crafting recipes.
 * Provides a 3x3 crafting grid, result slot, and item picker.
 */
public class RecipeEditorGui {

    // Crafting grid slots (3x3) in a 9x6 GUI
    private static final int[] GRID_SLOTS = {10, 11, 12, 19, 20, 21, 28, 29, 30};
    private static final int SLOT_RESULT = 24;
    private static final int SLOT_ARROW = 23;
    private static final int SLOT_SAVE = 48;
    private static final int SLOT_CLEAR = 50;
    private static final int SLOT_CLOSE = 53;
    private static final int SLOT_PICKER = 45;

    // Current editing state
    private static final ItemStack[] currentGrid = new ItemStack[9];
    private static ItemStack currentResult = ItemStack.EMPTY;

    static {
        for (int i = 0; i < 9; i++) currentGrid[i] = ItemStack.EMPTY;
    }

    public static void open(ServerPlayerEntity player) {
        openEditor(player);
    }

    private static void openEditor(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("Recipe Editor").formatted(Formatting.GOLD));

        // Background fill
        GuiElementBuilder bg = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                .setName(Text.literal(""));
        for (int i = 0; i < 54; i++) gui.setSlot(i, bg.build());

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.KNOWLEDGE_BOOK)
                .setName(Text.literal("Recipe Editor").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal("Click slots to set ingredients").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Result slot sets the output").formatted(Formatting.GRAY))
                .glow().build());

        // 3x3 Crafting Grid
        for (int i = 0; i < 9; i++) {
            final int slotIndex = i;
            ItemStack item = currentGrid[i];

            if (!item.isEmpty()) {
                GuiElementBuilder eb = new GuiElementBuilder(item.getItem());
                Text name = item.get(DataComponentTypes.CUSTOM_NAME);
                if (name != null) eb.setName(name);
                if (item.getCount() > 1) eb.setCount(item.getCount());
                eb.setCallback((idx, type, action, gui1) -> {
                    openItemPicker(player, selected -> {
                        currentGrid[slotIndex] = selected;
                        openEditor(player);
                    });
                });
                gui.setSlot(GRID_SLOTS[i], eb.build());
            } else {
                gui.setSlot(GRID_SLOTS[i], new GuiElementBuilder(Items.LIGHT_GRAY_STAINED_GLASS_PANE)
                        .setName(Text.literal("Empty Slot").formatted(Formatting.DARK_GRAY))
                        .addLoreLine(Text.literal("Click to select item").formatted(Formatting.GRAY))
                        .setCallback((idx, type, action, gui1) -> {
                            openItemPicker(player, selected -> {
                                currentGrid[slotIndex] = selected;
                                openEditor(player);
                            });
                        })
                        .build());
            }
        }

        // Arrow
        gui.setSlot(SLOT_ARROW, new GuiElementBuilder(Items.SPECTRAL_ARROW)
                .setName(Text.literal("->").formatted(Formatting.YELLOW))
                .build());

        // Result slot
        if (!currentResult.isEmpty()) {
            GuiElementBuilder rb = new GuiElementBuilder(currentResult.getItem());
            Text resultName = currentResult.get(DataComponentTypes.CUSTOM_NAME);
            if (resultName != null) rb.setName(resultName);
            rb.glow();
            rb.setCallback((idx, type, action, gui1) -> {
                openItemPicker(player, selected -> {
                    currentResult = selected;
                    openEditor(player);
                });
            });
            gui.setSlot(SLOT_RESULT, rb.build());
        } else {
            gui.setSlot(SLOT_RESULT, new GuiElementBuilder(Items.LIME_STAINED_GLASS_PANE)
                    .setName(Text.literal("Result").formatted(Formatting.GREEN))
                    .addLoreLine(Text.literal("Click to set result item").formatted(Formatting.GRAY))
                    .setCallback((idx, type, action, gui1) -> {
                        openItemPicker(player, selected -> {
                            currentResult = selected;
                            openEditor(player);
                        });
                    })
                    .build());
        }

        // Item Picker button
        gui.setSlot(SLOT_PICKER, new GuiElementBuilder(Items.CHEST)
                .setName(Text.literal("Item Picker").formatted(Formatting.AQUA))
                .addLoreLine(Text.literal("Browse all available items").formatted(Formatting.GRAY))
                .setCallback((idx, type, action, gui1) -> {
                    openItemPicker(player, s -> openEditor(player));
                })
                .build());

        // Save button
        gui.setSlot(SLOT_SAVE, new GuiElementBuilder(Items.EMERALD)
                .setName(Text.literal("Save Recipe").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal("Save this recipe to config").formatted(Formatting.GRAY))
                .setCallback((idx, type, action, gui1) -> {
                    saveCurrentRecipe(player);
                })
                .build());

        // Clear button
        gui.setSlot(SLOT_CLEAR, new GuiElementBuilder(Items.REDSTONE)
                .setName(Text.literal("Clear Grid").formatted(Formatting.RED))
                .addLoreLine(Text.literal("Reset all slots to empty").formatted(Formatting.GRAY))
                .setCallback((idx, type, action, gui1) -> {
                    for (int i = 0; i < 9; i++) currentGrid[i] = ItemStack.EMPTY;
                    currentResult = ItemStack.EMPTY;
                    openEditor(player);
                })
                .build());

        // Close button
        gui.setSlot(SLOT_CLOSE, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("Close").formatted(Formatting.RED))
                .build());

        gui.open();
    }

    private static void openItemPicker(ServerPlayerEntity player, java.util.function.Consumer<ItemStack> onSelect) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("Select Item").formatted(Formatting.AQUA));

        // Background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        // Collect all available items
        List<ItemStack> availableItems = new ArrayList<>();

        // Add vanilla items commonly used in crafting
        for (Item item : new Item[] {
            Items.DIAMOND, Items.IRON_INGOT, Items.GOLD_INGOT, Items.NETHERITE_INGOT,
            Items.STICK, Items.BONE, Items.STRING, Items.SLIME_BALL, Items.BLAZE_ROD,
            Items.REDSTONE, Items.GLOWSTONE_DUST, Items.GUNPOWDER,
            Items.LEATHER, Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE,
            Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS,
            Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE,
            Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS,
            Items.DIAMOND_SWORD, Items.DIAMOND_PICKAXE, Items.DIAMOND_AXE,
            Items.BOW, Items.CROSSBOW,
            Items.GLASS_BOTTLE, Items.WATER_BUCKET, Items.SUGAR,
            Items.WHEAT, Items.AZURE_BLUET, Items.COPPER_INGOT,
            Items.AMETHYST_BLOCK, Items.EXPERIENCE_BOTTLE, Items.COMPASS,
            Items.FLINT_AND_STEEL
        }) {
            availableItems.add(new ItemStack(item));
        }

        // Add all slayer chunks and cores
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            availableItems.add(SlayerItems.createChunk(type));
            availableItems.add(SlayerItems.createCore(type));
        }

        // Add all slayer items
        availableItems.add(SlayerItems.createZombieSword());
        availableItems.add(SlayerItems.createZombieHelmet());
        availableItems.add(SlayerItems.createSpiderSword());
        availableItems.add(SlayerItems.createSpiderLeggings());
        availableItems.add(SlayerItems.createSkeletonSword());
        availableItems.add(SlayerItems.createSkeletonBow());
        availableItems.add(SlayerItems.createSlimeSword());
        availableItems.add(SlayerItems.createSlimeBoots());
        availableItems.add(SlayerItems.createEndermanSword());
        availableItems.add(SlayerItems.createTheGavel());
        availableItems.add(SlayerItems.createWardenChestplate());

        // Add backrooms items
        availableItems.addAll(BackroomsItemsManager.getAllItems().values());

        // Display items in grid
        int slot = 9;
        for (ItemStack item : availableItems) {
            if (slot >= 45) break;

            GuiElementBuilder eb = new GuiElementBuilder(item.getItem());
            Text name = item.get(DataComponentTypes.CUSTOM_NAME);
            if (name != null) {
                eb.setName(name);
            } else {
                eb.setName(Text.literal(item.getItem().getName().getString()).formatted(Formatting.WHITE));
            }
            if (item.getCount() > 1) eb.setCount(item.getCount());

            final ItemStack selectedItem = item.copy();
            eb.setCallback((idx, type, action, gui1) -> {
                gui.close();
                onSelect.accept(selectedItem);
            });

            gui.setSlot(slot, eb.build());
            slot++;
        }

        // Back button
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action, gui1) -> {
                    gui.close();
                    openEditor(player);
                })
                .build());

        // Empty selection (clear slot)
        gui.setSlot(53, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("Clear Slot").formatted(Formatting.RED))
                .addLoreLine(Text.literal("Set this slot to empty").formatted(Formatting.GRAY))
                .setCallback((idx, type, action, gui1) -> {
                    gui.close();
                    onSelect.accept(ItemStack.EMPTY);
                })
                .build());

        gui.open();
    }

    private static void saveCurrentRecipe(ServerPlayerEntity player) {
        // Check if result is set
        if (currentResult.isEmpty()) {
            player.sendMessage(Text.literal("Cannot save: No result item set!").formatted(Formatting.RED), false);
            return;
        }

        // Check if at least one ingredient is set
        boolean hasIngredient = false;
        for (int i = 0; i < 9; i++) {
            if (!currentGrid[i].isEmpty()) {
                hasIngredient = true;
                break;
            }
        }
        if (!hasIngredient) {
            player.sendMessage(Text.literal("Cannot save: No ingredients set!").formatted(Formatting.RED), false);
            return;
        }

        // Generate recipe name from result item
        Text resultName = currentResult.get(DataComponentTypes.CUSTOM_NAME);
        String recipeName = resultName != null ? resultName.getString() : currentResult.getItem().getName().getString();

        // Create the recipe
        ItemStack[] ingredients = new ItemStack[9];
        for (int i = 0; i < 9; i++) {
            ingredients[i] = currentGrid[i].copy();
        }

        SlayerRecipes.Recipe recipe = new SlayerRecipes.Recipe(
                recipeName,
                currentResult.copy(),
                ingredients,
                null,
                0
        );

        // Save to config
        RecipeConfigManager.addCustomRecipe(recipe);

        player.sendMessage(Text.literal("Recipe saved: " + recipeName).formatted(Formatting.GREEN), false);

        // Clear state
        for (int i = 0; i < 9; i++) currentGrid[i] = ItemStack.EMPTY;
        currentResult = ItemStack.EMPTY;
    }
}
