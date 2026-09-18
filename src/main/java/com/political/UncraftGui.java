package com.political;


import com.political.bounty.SlayerItems;
import com.political.recipe.RecipeConfigManager;
import com.political.recipe.SlayerRecipes;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

/**
 * GUI for uncrafting compacted, super compacted, and enchanted items back to their base materials.
 */
public class UncraftGui {

    public static void openUncraftMenu(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x4, player, false);
        gui.setTitle(Component.literal("♻ Uncraft Items"));

        // Background
        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Component.literal(""))
                    .build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.CRAFTING_TABLE)
                .setName(Component.literal("♻ Uncraft Items").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Convert custom items back to base materials").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Categories:").withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal("• Compacted Items → 64 base").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("• Super Compacted → 576 base").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("• Enchanted Blocks → 64 base").withStyle(ChatFormatting.GRAY))
                .glow().build());

        // Compacted Items button
        gui.setSlot(10, new GuiElementBuilder(Items.RAW_IRON)
                .setName(Component.literal("⬛ Compacted Items").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Uncraft compacted materials").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Returns 64 base materials each").withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to browse").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> openCompactedUncraft(player, 0))
                .build());

        // Super Compacted button
        gui.setSlot(12, new GuiElementBuilder(Items.GOLD_BLOCK)
                .setName(Component.literal("⭐ Super Compacted").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Uncraft super compacted materials").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Returns 576 base materials each").withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to browse").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> openSuperCompactedUncraft(player, 0))
                .build());

        // Enchanted Blocks button
        gui.setSlot(14, new GuiElementBuilder(Items.COBBLESTONE)
                .setName(Component.literal("✧ Enchanted Blocks").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Uncraft enchanted blocks").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Returns 64 base blocks each").withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to browse").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> openEnchantedUncraft(player, 0))
                .build());

        // Enchanted Crops button
        gui.setSlot(16, new GuiElementBuilder(Items.WHEAT)
                .setName(Component.literal("✧ Enchanted Crops").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Uncraft enchanted crops & flowers").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Returns 64 base items each").withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to browse").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> openEnchantedCropsUncraft(player, 0))
                .build());

        // Custom Recipes button (from /setrecipe)
        gui.setSlot(22, new GuiElementBuilder(Items.KNOWLEDGE_BOOK)
                .setName(Component.literal("📖 Custom Recipes").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Reverse custom recipes from /setrecipe").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Returns ingredients used in crafting").withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to browse").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> openCustomRecipesUncraft(player, 0))
                .build());

        gui.open();
    }

    private static void openCompactedUncraft(ServerPlayer player, int page) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("⬛ Uncraft Compacted Items"));

        // Background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Component.literal(""))
                    .build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.RAW_IRON)
                .setName(Component.literal("⬛ Compacted → 64 Base").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click an item to uncraft it").withStyle(ChatFormatting.YELLOW))
                .glow().build());

        // Compacted items
        ItemStack[] compactedItems = {
            SlayerItems.createCompactedIron(),
            SlayerItems.createCompactedGold(),
            SlayerItems.createCompactedDiamond(),
            SlayerItems.createCompactedEmerald(),
                        SlayerItems.createCompactedCopper(),
            SlayerItems.createCompactedCoal(),
            SlayerItems.createCompactedLapis(),
            SlayerItems.createCompactedRedstone(),
            SlayerItems.createCompactedQuartz()
        };

        Item[] baseItems = {
            Items.IRON_INGOT, Items.GOLD_INGOT, Items.DIAMOND, Items.EMERALD,
            Items.COPPER_INGOT, Items.COAL,
            Items.LAPIS_LAZULI, Items.REDSTONE, Items.QUARTZ
        };

        int startSlot = 19;
        for (int i = 0; i < compactedItems.length && i < 7; i++) {
            final int idx = i;
            final Item baseItem = baseItems[i];
            gui.setSlot(startSlot + i, new GuiElementBuilder(compactedItems[i])
                    .setName(compactedItems[i].get(DataComponents.CUSTOM_NAME))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Returns: 64x " + new ItemStack(baseItem).getHoverName().getString()).withStyle(ChatFormatting.GREEN))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§eClick to uncraft").withStyle(ChatFormatting.YELLOW))
                    .setCallback((slot, type, containerInput, slotGui) -> performUncraft(player, compactedItems[idx], baseItem, 64))
                    .build());
        }

        // Second row
        startSlot = 28;
        for (int i = 7; i < compactedItems.length && i < 10; i++) {
            final int idx = i;
            final Item baseItem = baseItems[i];
            gui.setSlot(startSlot + (i - 7), new GuiElementBuilder(compactedItems[i])
                    .setName(compactedItems[i].get(DataComponents.CUSTOM_NAME))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Returns: 64x " + new ItemStack(baseItem).getHoverName().getString()).withStyle(ChatFormatting.GREEN))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§eClick to uncraft").withStyle(ChatFormatting.YELLOW))
                    .setCallback((slot, type, containerInput, slotGui) -> performUncraft(player, compactedItems[idx], baseItem, 64))
                    .build());
        }

        // Back button
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((slot, type, containerInput, slotGui) -> openUncraftMenu(player))
                .build());

        gui.open();
    }

    private static void openSuperCompactedUncraft(ServerPlayer player, int page) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x4, player, false);
        gui.setTitle(Component.literal("⭐ Uncraft Super Compacted"));

        // Background
        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Component.literal(""))
                    .build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.GOLD_BLOCK)
                .setName(Component.literal("⭐ Super Compacted → 576 Base").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click an item to uncraft it").withStyle(ChatFormatting.YELLOW))
                .glow().build());

        // Super compacted items
        ItemStack[] superItems = {
            SlayerItems.createSuperCompactedGold(),
            SlayerItems.createSuperCompactedIron(),
            SlayerItems.createSuperCompactedDiamond(),
            SlayerItems.createSuperCompactedEmerald(),
                    };

        Item[] baseItems = {
            Items.GOLD_INGOT, Items.IRON_INGOT, Items.DIAMOND, Items.EMERALD
        };

        int startSlot = 10;
        for (int i = 0; i < superItems.length; i++) {
            final int idx = i;
            final Item baseItem = baseItems[i];
            gui.setSlot(startSlot + i * 2, new GuiElementBuilder(superItems[i])
                    .setName(superItems[i].get(DataComponents.CUSTOM_NAME))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Returns: 576x (9 stacks) " + new ItemStack(baseItem).getHoverName().getString()).withStyle(ChatFormatting.GREEN))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§eClick to uncraft").withStyle(ChatFormatting.YELLOW))
                    .setCallback((slot, type, containerInput, slotGui) -> performUncraft(player, superItems[idx], baseItem, 576))
                    .build());
        }

        // Back button
        gui.setSlot(27, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((slot, type, containerInput, slotGui) -> openUncraftMenu(player))
                .build());

        gui.open();
    }

    private static void openEnchantedUncraft(ServerPlayer player, int page) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x4, player, false);
        gui.setTitle(Component.literal("✧ Uncraft Enchanted Blocks"));

        // Background
        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Component.literal(""))
                    .build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.COBBLESTONE)
                .setName(Component.literal("✧ Enchanted → 64 Base").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click an item to uncraft it").withStyle(ChatFormatting.YELLOW))
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
                    .setName(enchantedItems[i].get(DataComponents.CUSTOM_NAME))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Returns: 64x " + new ItemStack(baseItem).getHoverName().getString()).withStyle(ChatFormatting.GREEN))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§eClick to uncraft").withStyle(ChatFormatting.YELLOW))
                    .setCallback((s, type, containerInput, slotGui) -> performUncraft(player, enchantedItems[idx], baseItem, 64))
                    .build());
        }

        // Back button
        gui.setSlot(27, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((slot, type, containerInput, slotGui) -> openUncraftMenu(player))
                .build());

        gui.open();
    }

    private static void openEnchantedCropsUncraft(ServerPlayer player, int page) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal("✧ Uncraft Enchanted Crops"));

        // Background
        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Component.literal(""))
                    .build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.WHEAT)
                .setName(Component.literal("✧ Enchanted Crops → 64 Base").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click an item to uncraft it").withStyle(ChatFormatting.YELLOW))
                .glow().build());

        // Enchanted crops
        ItemStack[] enchantedItems = {
            SlayerItems.createEnchantedWheat(),
            SlayerItems.createEnchantedCarrot(),
            SlayerItems.createEnchantedPotato(),
            SlayerItems.createEnchantedRose(),
            SlayerItems.createEnchantedDandelion()
        };

        Item[] baseItems = {
            Items.WHEAT, Items.CARROT, Items.POTATO, Items.POPPY, Items.DANDELION
        };

        int startSlot = 10;
        for (int i = 0; i < enchantedItems.length; i++) {
            final int idx = i;
            final Item baseItem = baseItems[i];
            gui.setSlot(startSlot + i, new GuiElementBuilder(enchantedItems[i])
                    .setName(enchantedItems[i].get(DataComponents.CUSTOM_NAME))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Returns: 64x " + new ItemStack(baseItem).getHoverName().getString()).withStyle(ChatFormatting.GREEN))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§eClick to uncraft").withStyle(ChatFormatting.YELLOW))
                    .setCallback((s, type, containerInput, slotGui) -> performUncraft(player, enchantedItems[idx], baseItem, 64))
                    .build());
        }

        // Back button
        gui.setSlot(18, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((slot, type, containerInput, slotGui) -> openUncraftMenu(player))
                .build());

        gui.open();
    }

    private static void performUncraft(ServerPlayer player, ItemStack customItem, Item baseItem, int count) {
        // Check if player has the custom item
        String customId = SlayerItems.getCustomItemId(customItem);
        if (customId == null) {
            player.sendSystemMessage(Component.literal("✖ Invalid item!").withStyle(ChatFormatting.RED), false);
            return;
        }

        // Find the custom item in player's inventory
        int foundSlot = -1;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack invStack = player.getInventory().getItem(i);
            if (!invStack.isEmpty() && SlayerItems.getCustomItemId(invStack) != null 
                    && SlayerItems.getCustomItemId(invStack).equals(customId)) {
                foundSlot = i;
                break;
            }
        }

        if (foundSlot == -1) {
            player.sendSystemMessage(Component.literal("✖ You don't have this item!").withStyle(ChatFormatting.RED), false);
            return;
        }

        // Check inventory space (need space for output items)
        int stacksNeeded = (count + 63) / 64; // Number of stacks
        int emptySlots = 0;
        for (int i = 0; i < 36; i++) {
            if (player.getInventory().getItem(i).isEmpty()) {
                emptySlots++;
            }
        }

        if (emptySlots < stacksNeeded) {
            player.sendSystemMessage(Component.literal("✖ Not enough inventory space! Need " + stacksNeeded + " empty slots.").withStyle(ChatFormatting.RED), false);
            return;
        }

        // Remove the custom item
        player.getInventory().getItem(foundSlot).shrink(1);

        // Give base materials
        int remaining = count;
        while (remaining > 0) {
            int stackSize = Math.min(remaining, 64);
            ItemStack output = new ItemStack(baseItem, stackSize);
            player.addItem(output);
            remaining -= stackSize;
        }

        Component itemName = customItem.get(DataComponents.CUSTOM_NAME);
        String nameStr = itemName != null ? itemName.getString() : "Item";
        player.sendSystemMessage(Component.literal("✓ Uncrafted " + nameStr + " into " + count + "x " + new ItemStack(baseItem).getHoverName().getString())
                .withStyle(ChatFormatting.GREEN), false);
    }

    /**
     * Opens the custom recipes uncraft menu - reverses recipes from /setrecipe.
     */
    private static void openCustomRecipesUncraft(ServerPlayer player, int page) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("📖 Uncraft Custom Recipes"));

        // Background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Component.literal(""))
                    .build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.KNOWLEDGE_BOOK)
                .setName(Component.literal("📖 Custom Recipe Reversal").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Reverse recipes from /setrecipe").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Click result to get ingredients back").withStyle(ChatFormatting.YELLOW))
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
                    .setName(result.get(DataComponents.CUSTOM_NAME))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Returns " + ingredientCount + " ingredients").withStyle(ChatFormatting.GREEN))
                    .addLoreLine(Component.literal("Recipe: " + recipe.name).withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§eClick to uncraft").withStyle(ChatFormatting.YELLOW))
                    .setCallback((slot, type, containerInput, slotGui) -> performRecipeUncraft(player, recipe))
                    .build());
        }

        // Info if no custom recipes
        if (customRecipes.isEmpty()) {
            gui.setSlot(22, new GuiElementBuilder(Items.BARRIER)
                    .setName(Component.literal("No Custom Recipes").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Use /setrecipe to create custom recipes").withStyle(ChatFormatting.GRAY))
                    .build());
        }

        // Back button
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((slot, type, containerInput, slotGui) -> openUncraftMenu(player))
                .build());

        gui.open();
    }

    /**
     * Performs uncrafting of a custom recipe - returns ingredients.
     */
    private static void performRecipeUncraft(ServerPlayer player, SlayerRecipes.Recipe recipe) {
        ItemStack result = recipe.result.copy();
        String resultId = SlayerItems.getCustomItemId(result);
        
        // Find the result item in player's inventory
        int foundSlot = -1;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack invStack = player.getInventory().getItem(i);
            if (!invStack.isEmpty()) {
                String invId = SlayerItems.getCustomItemId(invStack);
                if (resultId != null && resultId.equals(invId)) {
                    foundSlot = i;
                    break;
                } else if (invStack.getItem() == result.getItem() && invStack.get(DataComponents.CUSTOM_NAME) == null) {
                    foundSlot = i;
                    break;
                }
            }
        }

        if (foundSlot == -1) {
            player.sendSystemMessage(Component.literal("✖ You don't have this item!").withStyle(ChatFormatting.RED), false);
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
            if (player.getInventory().getItem(i).isEmpty()) {
                emptySlots++;
            }
        }

        if (emptySlots < ingredientCount) {
            player.sendSystemMessage(Component.literal("✖ Not enough inventory space! Need " + ingredientCount + " empty slots.").withStyle(ChatFormatting.RED), false);
            return;
        }

        // Remove the result item
        player.getInventory().getItem(foundSlot).shrink(1);

        // Give back ingredients
        for (ItemStack ing : recipe.ingredients) {
            if (ing != null && !ing.isEmpty()) {
                player.addItem(ing.copy());
            }
        }

        Component itemName = result.get(DataComponents.CUSTOM_NAME);
        String nameStr = itemName != null ? itemName.getString() : "Item";
        player.sendSystemMessage(Component.literal("✓ Uncrafted " + nameStr + " into " + ingredientCount + " ingredients")
                .withStyle(ChatFormatting.GREEN), false);
    }
}
