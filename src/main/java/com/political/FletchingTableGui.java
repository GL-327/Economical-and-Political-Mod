package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Custom GUI for the Fletching Table to craft custom arrows.
 * Works like a smithing table - place items in input slots, see result, click to craft.
 */
public class FletchingTableGui {

    // Input slots (like smithing table)
    private static final int INPUT_SLOT_1 = 10;  // Arrow base slot
    private static final int INPUT_SLOT_2 = 11;  // Tip material slot
    private static final int INPUT_SLOT_3 = 12;  // Modifier slot
    private static final int OUTPUT_SLOT = 15;   // Result slot

    // Track input items per player
    private static final Map<UUID, ItemStack[]> playerInputs = new HashMap<>();

    /**
     * Opens the fletching table GUI for the specified player.
     * Works like a smithing table - place items, see result, click to craft.
     */
    public static void openGui(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
        gui.setTitle(Text.literal("Fletching Table").formatted(Formatting.GOLD, Formatting.BOLD));

        // Fill background
        fillBackground(gui);

        // Title
        gui.setSlot(4, new GuiElementBuilder(Items.FLETCHING_TABLE)
                .setName(Text.literal("◆ ARROW CRAFTING ◆").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Place items in slots to craft").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("custom arrows!").formatted(Formatting.GRAY))
                .build());

        // Input slot labels
        gui.setSlot(1, new GuiElementBuilder(Items.OAK_SIGN)
                .setName(Text.literal("Inputs").formatted(Formatting.YELLOW))
                .build());

        // Arrow icon between inputs and output
        gui.setSlot(13, new GuiElementBuilder(Items.SPECTRAL_ARROW)
                .setName(Text.literal("→ Result →").formatted(Formatting.GREEN))
                .build());

        // Get stored inputs for this player
        ItemStack[] inputs = playerInputs.getOrDefault(player.getUuid(), new ItemStack[]{ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY});

        // Input slot 1 - Arrow base
        gui.setSlot(INPUT_SLOT_1, createInputSlot(player, inputs[0], Items.ARROW, "Arrow Base", 0));

        // Input slot 2 - Tip material
        gui.setSlot(INPUT_SLOT_2, createInputSlot(player, inputs[1], Items.IRON_INGOT, "Tip Material", 1));

        // Input slot 3 - Modifier (optional)
        gui.setSlot(INPUT_SLOT_3, createInputSlot(player, inputs[2], Items.GUNPOWDER, "Modifier", 2));

        // Determine result based on inputs
        CustomArrows.ArrowType resultType = determineResult(inputs);
        boolean canCraft = resultType != null && hasCorrectAmounts(inputs, resultType);

        // Output slot
        if (canCraft && resultType != null) {
            int outputCount = CustomArrows.getOutputCount(resultType);
            ItemStack result = CustomArrows.createArrow(resultType, outputCount);
            gui.setSlot(OUTPUT_SLOT, new GuiElementBuilder(result)
                    .setName(Text.literal("✓ Craft ").formatted(Formatting.GREEN, Formatting.BOLD)
                            .append(Text.literal(outputCount + "x").formatted(Formatting.WHITE))
                            .append(Text.literal(" " + resultType.getName()).formatted(resultType.getColor())))
                    .glow()
                    .setCallback((slot, clickType, action) -> {
                        craftArrows(player, resultType);
                        gui.close();
                    })
                    .build());
        } else {
            gui.setSlot(OUTPUT_SLOT, new GuiElementBuilder(Items.BARRIER)
                    .setName(Text.literal("Invalid Recipe").formatted(Formatting.RED))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Place valid materials to craft").formatted(Formatting.GRAY))
                    .build());
        }

        // Recipe book button
        gui.setSlot(22, new GuiElementBuilder(Items.KNOWLEDGE_BOOK)
                .setName(Text.literal("View Recipes").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to see all arrow recipes").formatted(Formatting.GRAY))
                .setCallback((slot, clickType, action) -> openRecipeBook(player))
                .build());

        // Admin button (only for operators)
        if (CommandManager.requirePermissionLevel(CommandManager.OWNERS_CHECK).test(player.getCommandSource())) {
            gui.setSlot(26, new GuiElementBuilder(Items.COMMAND_BLOCK)
                    .setName(Text.literal("⚙ Admin").formatted(Formatting.RED))
                    .setCallback((slot, clickType, action) -> openAdminGui(player))
                    .build());
        }

        gui.open();
    }

    /**
     * Creates an input slot that accepts items from player inventory.
     */
    private static GuiElementBuilder createInputSlot(ServerPlayerEntity player, ItemStack current, net.minecraft.item.Item defaultItem, String label, int inputIndex) {
        if (current == null || current.isEmpty()) {
            return new GuiElementBuilder(defaultItem)
                    .setName(Text.literal(label).formatted(Formatting.YELLOW))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Click with item to place").formatted(Formatting.GRAY))
                    .setCallback((slot, clickType, action) -> {
                        // Handle item placement
                        handleInputClick(player, inputIndex, action, clickType);
                    });
        } else {
            return new GuiElementBuilder(current.copy())
                    .setName(Text.literal(label).formatted(Formatting.YELLOW))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Click to remove").formatted(Formatting.RED))
                    .setCallback((slot, clickType, action) -> {
                        // Return item to player
                        returnInputToPlayer(player, inputIndex);
                        openGui(player);
                    });
        }
    }

    /**
     * Handle click on input slot - place or remove items.
     */
    private static void handleInputClick(ServerPlayerEntity player, int inputIndex, net.minecraft.screen.slot.SlotActionType actionType, eu.pb4.sgui.api.ClickType clickType) {
        // Get the item the player is holding on cursor
        ItemStack cursor = player.currentScreenHandler.getCursorStack();
        
        if (cursor != null && !cursor.isEmpty()) {
            // Place item in input slot
            ItemStack[] inputs = playerInputs.getOrDefault(player.getUuid(), new ItemStack[]{ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY});
            
            // Return existing item if any
            if (inputs[inputIndex] != null && !inputs[inputIndex].isEmpty()) {
                player.getInventory().offerOrDrop(inputs[inputIndex].copy());
            }
            
            inputs[inputIndex] = cursor.copy();
            playerInputs.put(player.getUuid(), inputs);
            
            // Clear cursor
            player.currentScreenHandler.setCursorStack(ItemStack.EMPTY);
            
            player.getEntityWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.5f, 1.0f);
        }
        
        openGui(player);
    }

    /**
     * Return an input item to the player's inventory.
     */
    private static void returnInputToPlayer(ServerPlayerEntity player, int inputIndex) {
        ItemStack[] inputs = playerInputs.get(player.getUuid());
        if (inputs != null && inputs[inputIndex] != null && !inputs[inputIndex].isEmpty()) {
            player.getInventory().offerOrDrop(inputs[inputIndex].copy());
            inputs[inputIndex] = ItemStack.EMPTY;
            playerInputs.put(player.getUuid(), inputs);
        }
    }

    /**
     * Determine the arrow type based on input items.
     */
    private static CustomArrows.ArrowType determineResult(ItemStack[] inputs) {
        if (inputs == null || inputs[0] == null || inputs[0].isEmpty()) return null;
        
        // Check if slot 0 has arrows
        if (inputs[0].getItem() != Items.ARROW) return null;
        
        ItemStack tip = inputs[1];
        ItemStack modifier = inputs[2];
        
        // Match recipes
        if (tip != null && !tip.isEmpty()) {
            // Iron tipped
            if (tip.getItem() == Items.IRON_INGOT && (modifier == null || modifier.isEmpty())) {
                return CustomArrows.ArrowType.IRON_TIPPED;
            }
            // Steel tipped (iron block)
            if (tip.getItem() == Items.IRON_BLOCK && (modifier == null || modifier.isEmpty())) {
                return CustomArrows.ArrowType.STEEL_TIPPED;
            }
            // Diamond tipped
            if (tip.getItem() == Items.DIAMOND && (modifier == null || modifier.isEmpty())) {
                return CustomArrows.ArrowType.DIAMOND_TIPPED;
            }
            // Netherite tipped
            if (tip.getItem() == Items.NETHERITE_INGOT && (modifier == null || modifier.isEmpty())) {
                return CustomArrows.ArrowType.NETHERITE_TIPPED;
            }
            // Explosive (TNT + gunpowder)
            if (tip.getItem() == Items.TNT && modifier != null && modifier.getItem() == Items.GUNPOWDER) {
                return CustomArrows.ArrowType.EXPLOSIVE_ARROW;
            }
            // Incendiary (blaze powder + fire charge)
            if (tip.getItem() == Items.BLAZE_POWDER && modifier != null && modifier.getItem() == Items.FIRE_CHARGE) {
                return CustomArrows.ArrowType.INCENDIARY_ARROW;
            }
            // Poison (spider eye + pufferfish)
            if (tip.getItem() == Items.SPIDER_EYE && modifier != null && modifier.getItem() == Items.PUFFERFISH) {
                return CustomArrows.ArrowType.POISON_ARROW;
            }
            // Void (ender pearl + echo shard)
            if (tip.getItem() == Items.ENDER_PEARL && modifier != null && modifier.getItem() == Items.ECHO_SHARD) {
                return CustomArrows.ArrowType.VOID_ARROW;
            }
            // Spectral (glowstone + ghast tear)
            if (tip.getItem() == Items.GLOWSTONE_DUST && modifier != null && modifier.getItem() == Items.GHAST_TEAR) {
                return CustomArrows.ArrowType.SPECTRAL_ARROW;
            }
            // Frost (ice + blue ice)
            if (tip.getItem() == Items.ICE && modifier != null && modifier.getItem() == Items.BLUE_ICE) {
                return CustomArrows.ArrowType.FROST_ARROW;
            }
            // Lightning (copper + lightning rod)
            if (tip.getItem() == Items.COPPER_INGOT && modifier != null && modifier.getItem() == Items.LIGHTNING_ROD) {
                return CustomArrows.ArrowType.LIGHTNING_ARROW;
            }
            // Healing (golden apple + glistering melon)
            if (tip.getItem() == Items.GOLDEN_APPLE && modifier != null && modifier.getItem() == Items.GLISTERING_MELON_SLICE) {
                return CustomArrows.ArrowType.HEALING_ARROW;
            }
            // Gravity (ender eye + chorus fruit)
            if (tip.getItem() == Items.ENDER_EYE && modifier != null && modifier.getItem() == Items.CHORUS_FRUIT) {
                return CustomArrows.ArrowType.GRAVITY_ARROW;
            }
            // Bouncing (slime ball + honey block)
            if (tip.getItem() == Items.SLIME_BALL && modifier != null && modifier.getItem() == Items.HONEY_BLOCK) {
                return CustomArrows.ArrowType.BOUNCING_ARROW;
            }
        }
        
        return null;
    }

    /**
     * Check if the input amounts are correct for the recipe.
     */
    private static boolean hasCorrectAmounts(ItemStack[] inputs, CustomArrows.ArrowType type) {
        ItemStack[] recipe = CustomArrows.getRecipe(type);
        if (recipe == null) return false;
        
        for (int i = 0; i < recipe.length; i++) {
            if (recipe[i] == null) continue;
            if (inputs[i] == null || inputs[i].isEmpty()) return false;
            if (inputs[i].getItem() != recipe[i].getItem()) return false;
            if (inputs[i].getCount() < recipe[i].getCount()) return false;
        }
        
        return true;
    }

    /**
     * Craft the arrows, consuming inputs and giving result.
     */
    private static void craftArrows(ServerPlayerEntity player, CustomArrows.ArrowType type) {
        ItemStack[] inputs = playerInputs.get(player.getUuid());
        if (inputs == null) return;
        
        ItemStack[] recipe = CustomArrows.getRecipe(type);
        if (recipe == null) return;
        
        // Consume inputs
        for (int i = 0; i < recipe.length; i++) {
            if (recipe[i] == null || inputs[i] == null) continue;
            
            int toConsume = recipe[i].getCount();
            if (inputs[i].getCount() > toConsume) {
                inputs[i].decrement(toConsume);
            } else {
                inputs[i] = ItemStack.EMPTY;
            }
        }
        
        playerInputs.put(player.getUuid(), inputs);
        
        // Give result
        int outputCount = CustomArrows.getOutputCount(type);
        ItemStack result = CustomArrows.createArrow(type, outputCount);
        player.getInventory().offerOrDrop(result);
        
        // Feedback
        player.sendMessage(
                Text.literal("✓ Crafted ").formatted(Formatting.GREEN)
                        .append(Text.literal(outputCount + "x ").formatted(Formatting.WHITE, Formatting.BOLD))
                        .append(Text.literal(type.getName()).formatted(type.getColor())),
                true
        );
        player.getEntityWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_VILLAGER_WORK_FLETCHER, SoundCategory.PLAYERS, 1.0f, 1.0f);
    }

    /**
     * Fill background with brown stained glass panes.
     */
    private static void fillBackground(SimpleGui gui) {
        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BROWN_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }
    }

    /**
     * Opens a recipe book showing all arrow recipes.
     */
    public static void openRecipeBook(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("Arrow Recipes").formatted(Formatting.AQUA, Formatting.BOLD));

        fillBackground(gui);

        // Title
        gui.setSlot(4, new GuiElementBuilder(Items.KNOWLEDGE_BOOK)
                .setName(Text.literal("◆ ARROW RECIPES ◆").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("All custom arrow crafting recipes").formatted(Formatting.GRAY))
                .build());

        // Show all arrow types and their recipes
        CustomArrows.ArrowType[] types = CustomArrows.getAllArrowTypes();
        // 6 rows = 54 slots, use slots 10-16, 19-25, 28-34, 37-43 for display (21 slots)
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};

        for (int i = 0; i < types.length && i < slots.length; i++) {
            CustomArrows.ArrowType type = types[i];
            ItemStack displayArrow = CustomArrows.createArrow(type, 1);
            ItemStack[] recipe = CustomArrows.getRecipe(type);

            GuiElementBuilder builder = new GuiElementBuilder(displayArrow.getItem())
                    .setName(Text.literal(type.getName()).formatted(type.getColor(), Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal(type.getDescription()).formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Recipe:").formatted(Formatting.YELLOW));

            if (recipe != null) {
                for (ItemStack req : recipe) {
                    if (req != null) {
                        builder.addLoreLine(Text.literal("  " + req.getCount() + "x " + req.getName().getString()).formatted(Formatting.GRAY));
                    }
                }
            }

            builder.addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Output: " + CustomArrows.getOutputCount(type) + " arrows").formatted(Formatting.GREEN));

            gui.setSlot(slots[i], builder.build());
        }

        // Back button
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((slot, clickType, action) -> openGui(player))
                .build());

        gui.open();
    }

    /**
     * Opens the admin GUI for editing arrow recipes.
     */
    public static void openAdminGui(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("Fletching Table Admin").formatted(Formatting.RED, Formatting.BOLD));

        fillBackground(gui);

        // Title
        gui.setSlot(4, new GuiElementBuilder(Items.COMMAND_BLOCK)
                .setName(Text.literal("◆ RECIPE EDITOR ◆").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click an arrow to edit its recipe").formatted(Formatting.GRAY))
                .build());

        // Add arrow type selection buttons for editing
        CustomArrows.ArrowType[] types = CustomArrows.getAllArrowTypes();
        // 6 rows = 54 slots, use slots 10-16, 19-25, 28-34, 37-43 for display (21 slots)
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};

        for (int i = 0; i < types.length && i < slots.length; i++) {
            CustomArrows.ArrowType type = types[i];
            ItemStack displayArrow = CustomArrows.createArrow(type, 1);

            gui.setSlot(slots[i], new GuiElementBuilder(displayArrow.getItem())
                    .setName(Text.literal(type.getName()).formatted(type.getColor(), Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Click to edit recipe").formatted(Formatting.YELLOW))
                    .setCallback((slot, clickType, action) -> openRecipeEditor(player, type))
                    .build());
        }

        // Back button
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((slot, clickType, action) -> openGui(player))
                .build());

        gui.open();
    }

    /**
     * Opens the recipe editor for a specific arrow type.
     */
    private static void openRecipeEditor(ServerPlayerEntity player, CustomArrows.ArrowType type) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
        gui.setTitle(Text.literal("Edit: " + type.getName()).formatted(type.getColor(), Formatting.BOLD));

        fillBackground(gui);

        // Current recipe display
        gui.setSlot(4, new GuiElementBuilder(Items.WRITABLE_BOOK)
                .setName(Text.literal("Current Recipe").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Recipe editing requires server config").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Edit CustomArrows.java to change recipes").formatted(Formatting.GRAY))
                .build());

        // Show current recipe
        ItemStack[] recipe = CustomArrows.getRecipe(type);
        int[] recipeSlots = {10, 11, 12};
        if (recipe != null) {
            for (int i = 0; i < recipe.length && i < recipeSlots.length; i++) {
                if (recipe[i] != null) {
                    gui.setSlot(recipeSlots[i], new GuiElementBuilder(recipe[i].copy())
                            .setName(Text.literal("§e" + recipe[i].getCount() + "x ").append(recipe[i].getName()))
                            .build());
                }
            }
        }

        // Output count
        int outputCount = CustomArrows.getOutputCount(type);
        gui.setSlot(16, new GuiElementBuilder(CustomArrows.createArrow(type, outputCount))
                .setName(Text.literal("Output: ").formatted(Formatting.GREEN)
                        .append(Text.literal(outputCount + " arrows").formatted(Formatting.WHITE)))
                .build());

        // Back button
        gui.setSlot(0, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((slot, clickType, action) -> openAdminGui(player))
                .build());

        gui.open();
    }
}
