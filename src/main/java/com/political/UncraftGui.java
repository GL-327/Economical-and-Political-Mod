package com.political;

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

/**
 * GUI for uncrafting compacted, super compacted, and enchanted items back to their base materials.
 */
public class UncraftGui {

    public static void openUncraftMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X4, player, false);
        gui.setTitle(Text.literal("♻ Uncraft Items"));

        // Background
        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.CRAFTING_TABLE)
                .setName(Text.literal("♻ Uncraft Items").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Convert custom items back to base materials").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Categories:").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("• Compacted Items → 64 base").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("• Super Compacted → 576 base").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("• Enchanted Blocks → 64 base").formatted(Formatting.GRAY))
                .glow().build());

        // Compacted Items button
        gui.setSlot(10, new GuiElementBuilder(Items.RAW_IRON)
                .setName(Text.literal("⬛ Compacted Items").formatted(Formatting.DARK_GRAY, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Uncraft compacted materials").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Returns 64 base materials each").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§eClick to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openCompactedUncraft(player, 0))
                .build());

        // Super Compacted button
        gui.setSlot(12, new GuiElementBuilder(Items.GOLD_BLOCK)
                .setName(Text.literal("⭐ Super Compacted").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Uncraft super compacted materials").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Returns 576 base materials each").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§eClick to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openSuperCompactedUncraft(player, 0))
                .build());

        // Enchanted Blocks button
        gui.setSlot(14, new GuiElementBuilder(Items.COBBLESTONE)
                .setName(Text.literal("✧ Enchanted Blocks").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Uncraft enchanted blocks").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Returns 64 base blocks each").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§eClick to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openEnchantedUncraft(player, 0))
                .build());

        // Enchanted Crops button
        gui.setSlot(16, new GuiElementBuilder(Items.WHEAT)
                .setName(Text.literal("✧ Enchanted Crops").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Uncraft enchanted crops & flowers").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Returns 64 base items each").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§eClick to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openEnchantedCropsUncraft(player, 0))
                .build());

        // Custom Recipes button (from /setrecipe)
        gui.setSlot(22, new GuiElementBuilder(Items.KNOWLEDGE_BOOK)
                .setName(Text.literal("📖 Custom Recipes").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Reverse custom recipes from /setrecipe").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Returns ingredients used in crafting").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("§eClick to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openCustomRecipesUncraft(player, 0))
                .build());

        gui.open();
    }

    private static void openCompactedUncraft(ServerPlayerEntity player, int page) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("⬛ Uncraft Compacted Items"));

        // Background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.RAW_IRON)
                .setName(Text.literal("⬛ Compacted → 64 Base").formatted(Formatting.DARK_GRAY, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click an item to uncraft it").formatted(Formatting.YELLOW))
                .glow().build());

        // Compacted items
        ItemStack[] compactedItems = {
            SlayerItems.createCompactedIron(),
            SlayerItems.createCompactedGold(),
            SlayerItems.createCompactedDiamond(),
            SlayerItems.createCompactedEmerald(),
            SlayerItems.createCompactedNetherite(),
            SlayerItems.createCompactedCopper(),
            SlayerItems.createCompactedCoal(),
            SlayerItems.createCompactedLapis(),
            SlayerItems.createCompactedRedstone(),
            SlayerItems.createCompactedQuartz()
        };

        Item[] baseItems = {
            Items.IRON_INGOT, Items.GOLD_INGOT, Items.DIAMOND, Items.EMERALD,
            Items.NETHERITE_INGOT, Items.COPPER_INGOT, Items.COAL,
            Items.LAPIS_LAZULI, Items.REDSTONE, Items.QUARTZ
        };

        int startSlot = 19;
        for (int i = 0; i < compactedItems.length && i < 7; i++) {
            final int idx = i;
            final Item baseItem = baseItems[i];
            gui.setSlot(startSlot + i, new GuiElementBuilder(compactedItems[i])
                    .setName(compactedItems[i].get(DataComponentTypes.CUSTOM_NAME))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Returns: 64x " + baseItem.getName().getString()).formatted(Formatting.GREEN))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("§eClick to uncraft").formatted(Formatting.YELLOW))
                    .setCallback((slot, type, action) -> performUncraft(player, compactedItems[idx], baseItem, 64))
                    .build());
        }

        // Second row
        startSlot = 28;
        for (int i = 7; i < compactedItems.length && i < 10; i++) {
            final int idx = i;
            final Item baseItem = baseItems[i];
            gui.setSlot(startSlot + (i - 7), new GuiElementBuilder(compactedItems[i])
                    .setName(compactedItems[i].get(DataComponentTypes.CUSTOM_NAME))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Returns: 64x " + baseItem.getName().getString()).formatted(Formatting.GREEN))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("§eClick to uncraft").formatted(Formatting.YELLOW))
                    .setCallback((slot, type, action) -> performUncraft(player, compactedItems[idx], baseItem, 64))
                    .build());
        }

        // Back button
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((slot, type, action) -> openUncraftMenu(player))
                .build());

        gui.open();
    }

    private static void openSuperCompactedUncraft(ServerPlayerEntity player, int page) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X4, player, false);
        gui.setTitle(Text.literal("⭐ Uncraft Super Compacted"));

        // Background
        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.GOLD_BLOCK)
                .setName(Text.literal("⭐ Super Compacted → 576 Base").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click an item to uncraft it").formatted(Formatting.YELLOW))
                .glow().build());

        // Super compacted items
        ItemStack[] superItems = {
            SlayerItems.createSuperCompactedGold(),
            SlayerItems.createSuperCompactedIron(),
            SlayerItems.createSuperCompactedDiamond(),
            SlayerItems.createSuperCompactedEmerald(),
            SlayerItems.createSuperCompactedNetherite()
        };

        Item[] baseItems = {
            Items.GOLD_INGOT, Items.IRON_INGOT, Items.DIAMOND, Items.EMERALD, Items.NETHERITE_INGOT
        };

        int startSlot = 10;
        for (int i = 0; i < superItems.length; i++) {
            final int idx = i;
            final Item baseItem = baseItems[i];
            gui.setSlot(startSlot + i * 2, new GuiElementBuilder(superItems[i])
                    .setName(superItems[i].get(DataComponentTypes.CUSTOM_NAME))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Returns: 576x (9 stacks) " + baseItem.getName().getString()).formatted(Formatting.GREEN))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("§eClick to uncraft").formatted(Formatting.YELLOW))
                    .setCallback((slot, type, action) -> performUncraft(player, superItems[idx], baseItem, 576))
                    .build());
        }

        // Back button
        gui.setSlot(27, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((slot, type, action) -> openUncraftMenu(player))
                .build());

        gui.open();
    }

    private static void openEnchantedUncraft(ServerPlayerEntity player, int page) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X4, player, false);
        gui.setTitle(Text.literal("✧ Uncraft Enchanted Blocks"));

        // Background
        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.COBBLESTONE)
                .setName(Text.literal("✧ Enchanted → 64 Base").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click an item to uncraft it").formatted(Formatting.YELLOW))
                .glow().build());

        // Enchanted blocks
        ItemStack[] enchantedItems = {
            SlayerItems.createEnchantedCobblestone(),
            SlayerItems.createEnchantedStone(),
            SlayerItems.createEnchantedIronBlock(),
            SlayerItems.createEnchantedGoldBlock(),
            SlayerItems.createEnchantedDiamondBlock(),
            SlayerItems.createEnchantedBlackstone(),
            SlayerItems.createEnchantedOakLog()
        };

        Item[] baseItems = {
            Items.COBBLESTONE, Items.STONE, Items.IRON_BLOCK, Items.GOLD_BLOCK,
            Items.DIAMOND_BLOCK, Items.BLACKSTONE, Items.OAK_LOG
        };

        int startSlot = 10;
        for (int i = 0; i < enchantedItems.length; i++) {
            final int idx = i;
            final Item baseItem = baseItems[i];
            int slot = startSlot + (i < 4 ? i : i + 2);
            gui.setSlot(slot, new GuiElementBuilder(enchantedItems[i])
                    .setName(enchantedItems[i].get(DataComponentTypes.CUSTOM_NAME))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Returns: 64x " + baseItem.getName().getString()).formatted(Formatting.GREEN))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("§eClick to uncraft").formatted(Formatting.YELLOW))
                    .setCallback((s, type, action) -> performUncraft(player, enchantedItems[idx], baseItem, 64))
                    .build());
        }

        // Back button
        gui.setSlot(27, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((slot, type, action) -> openUncraftMenu(player))
                .build());

        gui.open();
    }

    private static void openEnchantedCropsUncraft(ServerPlayerEntity player, int page) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
        gui.setTitle(Text.literal("✧ Uncraft Enchanted Crops"));

        // Background
        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.WHEAT)
                .setName(Text.literal("✧ Enchanted Crops → 64 Base").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click an item to uncraft it").formatted(Formatting.YELLOW))
                .glow().build());

        // Enchanted crops
        ItemStack[] enchantedItems = {
            SlayerItems.createEnchantedWheat(),
            SlayerItems.createEnchantedCarrot(),
            SlayerItems.createEnchantedPotato(),
            SlayerItems.createEnchantedSugarCane(),
            SlayerItems.createEnchantedRose(),
            SlayerItems.createEnchantedDandelion()
        };

        Item[] baseItems = {
            Items.WHEAT, Items.CARROT, Items.POTATO, Items.SUGAR_CANE, Items.POPPY, Items.DANDELION
        };

        int startSlot = 10;
        for (int i = 0; i < enchantedItems.length; i++) {
            final int idx = i;
            final Item baseItem = baseItems[i];
            gui.setSlot(startSlot + i, new GuiElementBuilder(enchantedItems[i])
                    .setName(enchantedItems[i].get(DataComponentTypes.CUSTOM_NAME))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Returns: 64x " + baseItem.getName().getString()).formatted(Formatting.GREEN))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("§eClick to uncraft").formatted(Formatting.YELLOW))
                    .setCallback((s, type, action) -> performUncraft(player, enchantedItems[idx], baseItem, 64))
                    .build());
        }

        // Back button
        gui.setSlot(18, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((slot, type, action) -> openUncraftMenu(player))
                .build());

        gui.open();
    }

    private static void performUncraft(ServerPlayerEntity player, ItemStack customItem, Item baseItem, int count) {
        // Check if player has the custom item
        String customId = SlayerItems.getCustomItemId(customItem);
        if (customId == null) {
            player.sendMessage(Text.literal("✖ Invalid item!").formatted(Formatting.RED), false);
            return;
        }

        // Find the custom item in player's inventory
        int foundSlot = -1;
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack invStack = player.getInventory().getStack(i);
            if (!invStack.isEmpty() && SlayerItems.getCustomItemId(invStack) != null 
                    && SlayerItems.getCustomItemId(invStack).equals(customId)) {
                foundSlot = i;
                break;
            }
        }

        if (foundSlot == -1) {
            player.sendMessage(Text.literal("✖ You don't have this item!").formatted(Formatting.RED), false);
            return;
        }

        // Check inventory space (need space for output items)
        int stacksNeeded = (count + 63) / 64; // Number of stacks
        int emptySlots = 0;
        for (int i = 0; i < 36; i++) {
            if (player.getInventory().getStack(i).isEmpty()) {
                emptySlots++;
            }
        }

        if (emptySlots < stacksNeeded) {
            player.sendMessage(Text.literal("✖ Not enough inventory space! Need " + stacksNeeded + " empty slots.").formatted(Formatting.RED), false);
            return;
        }

        // Remove the custom item
        player.getInventory().getStack(foundSlot).decrement(1);

        // Give base materials
        int remaining = count;
        while (remaining > 0) {
            int stackSize = Math.min(remaining, 64);
            ItemStack output = new ItemStack(baseItem, stackSize);
            player.getInventory().insertStack(output);
            remaining -= stackSize;
        }

        Text itemName = customItem.get(DataComponentTypes.CUSTOM_NAME);
        String nameStr = itemName != null ? itemName.getString() : "Item";
        player.sendMessage(Text.literal("✓ Uncrafted " + nameStr + " into " + count + "x " + baseItem.getName().getString())
                .formatted(Formatting.GREEN), false);
    }

    /**
     * Opens the custom recipes uncraft menu - reverses recipes from /setrecipe.
     */
    private static void openCustomRecipesUncraft(ServerPlayerEntity player, int page) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("📖 Uncraft Custom Recipes"));

        // Background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.KNOWLEDGE_BOOK)
                .setName(Text.literal("📖 Custom Recipe Reversal").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Reverse recipes from /setrecipe").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Click result to get ingredients back").formatted(Formatting.YELLOW))
                .glow().build());

        // Get custom recipes from RecipeConfigManager
        java.util.List<SlayerRecipes.Recipe> customRecipes = RecipeConfigManager.getCustomRecipes();
        
        int startSlot = 9;
        int maxSlots = 36;
        
        for (int i = 0; i < customRecipes.size() && i < maxSlots; i++) {
            SlayerRecipes.Recipe recipe = customRecipes.get(i);
            ItemStack result = recipe.result.copy();
            
            // Count non-empty ingredients
            int ingredientCount = 0;
            for (ItemStack ing : recipe.ingredients) {
                if (ing != null && !ing.isEmpty()) ingredientCount++;
            }
            
            gui.setSlot(startSlot + i, new GuiElementBuilder(result)
                    .setName(result.get(DataComponentTypes.CUSTOM_NAME))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Returns " + ingredientCount + " ingredients").formatted(Formatting.GREEN))
                    .addLoreLine(Text.literal("Recipe: " + recipe.name).formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("§eClick to uncraft").formatted(Formatting.YELLOW))
                    .setCallback((slot, type, action) -> performRecipeUncraft(player, recipe))
                    .build());
        }

        // Info if no custom recipes
        if (customRecipes.isEmpty()) {
            gui.setSlot(22, new GuiElementBuilder(Items.BARRIER)
                    .setName(Text.literal("No Custom Recipes").formatted(Formatting.RED, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Use /setrecipe to create custom recipes").formatted(Formatting.GRAY))
                    .build());
        }

        // Back button
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((slot, type, action) -> openUncraftMenu(player))
                .build());

        gui.open();
    }

    /**
     * Performs uncrafting of a custom recipe - returns ingredients.
     */
    private static void performRecipeUncraft(ServerPlayerEntity player, SlayerRecipes.Recipe recipe) {
        ItemStack result = recipe.result.copy();
        String resultId = SlayerItems.getCustomItemId(result);
        
        // Find the result item in player's inventory
        int foundSlot = -1;
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack invStack = player.getInventory().getStack(i);
            if (!invStack.isEmpty()) {
                String invId = SlayerItems.getCustomItemId(invStack);
                if (resultId != null && resultId.equals(invId)) {
                    foundSlot = i;
                    break;
                } else if (invStack.getItem() == result.getItem() && invStack.get(DataComponentTypes.CUSTOM_NAME) == null) {
                    foundSlot = i;
                    break;
                }
            }
        }

        if (foundSlot == -1) {
            player.sendMessage(Text.literal("✖ You don't have this item!").formatted(Formatting.RED), false);
            return;
        }

        // Count non-empty ingredients
        int ingredientCount = 0;
        for (ItemStack ing : recipe.ingredients) {
            if (ing != null && !ing.isEmpty()) ingredientCount++;
        }

        // Check inventory space
        int emptySlots = 0;
        for (int i = 0; i < 36; i++) {
            if (player.getInventory().getStack(i).isEmpty()) {
                emptySlots++;
            }
        }

        if (emptySlots < ingredientCount) {
            player.sendMessage(Text.literal("✖ Not enough inventory space! Need " + ingredientCount + " empty slots.").formatted(Formatting.RED), false);
            return;
        }

        // Remove the result item
        player.getInventory().getStack(foundSlot).decrement(1);

        // Give back ingredients
        for (ItemStack ing : recipe.ingredients) {
            if (ing != null && !ing.isEmpty()) {
                player.getInventory().insertStack(ing.copy());
            }
        }

        Text itemName = result.get(DataComponentTypes.CUSTOM_NAME);
        String nameStr = itemName != null ? itemName.getString() : "Item";
        player.sendMessage(Text.literal("✓ Uncrafted " + nameStr + " into " + ingredientCount + " ingredients")
                .formatted(Formatting.GREEN), false);
    }
}
