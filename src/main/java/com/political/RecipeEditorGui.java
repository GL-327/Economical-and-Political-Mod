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

import java.util.List;

public class RecipeEditorGui {

    private static final int[] GRID_SLOTS = {10, 11, 12, 19, 20, 21, 28, 29, 30};
    private static final int SLOT_ARROW  = 14;
    private static final int SLOT_RESULT = 15;
    private static final int SLOT_BACK   = 36;
    private static final int SLOT_HEADER = 4;

    /** Opens the paginated recipe list GUI. */
    public static void openRecipeList(ServerPlayerEntity player) {
        List<SlayerRecipes.Recipe> recipes = RecipeConfigManager.getRecipes();
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("§6§l/setrecipe — Recipe Editor"));

        GuiElementBuilder bg = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                .setName(Text.literal(""));
        for (int i = 0; i < 54; i++) gui.setSlot(i, bg.build());

        // Show up to 28 recipes (rows 0–3, cols 1–7)
        int shown = 0;
        for (int i = 0; i < recipes.size() && shown < 28; i++) {
            SlayerRecipes.Recipe recipe = recipes.get(i);
            int row = shown / 7;
            int col = shown % 7 + 1;
            int slot = row * 9 + col;

            ItemStack result = recipe.result.copy();
            Text displayName = result.get(DataComponentTypes.CUSTOM_NAME);
            GuiElementBuilder eb = new GuiElementBuilder(result.getItem())
                    .setName(displayName != null ? displayName : Text.literal("§e" + recipe.name))
                    .addLoreLine(Text.literal("§7Click to view recipe").formatted(Formatting.GRAY));
            if (result.getCount() > 1) eb.setCount(result.getCount());

            final SlayerRecipes.Recipe finalRecipe = recipe;
            gui.setSlot(slot, eb.setCallback((index, clickType, actionType) ->
                    openRecipeView(player, finalRecipe)).build());
            shown++;
        }

        gui.open();
    }

    /** Opens a single recipe detail view. */
    public static void openRecipeView(ServerPlayerEntity player, SlayerRecipes.Recipe recipe) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X5, player, false);
        gui.setTitle(Text.literal("§6Recipe: §e" + recipe.name));

        GuiElementBuilder bg = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                .setName(Text.literal(""));
        for (int i = 0; i < 45; i++) gui.setSlot(i, bg.build());

        // Header
        Text resultName = recipe.result.get(DataComponentTypes.CUSTOM_NAME);
        gui.setSlot(SLOT_HEADER, new GuiElementBuilder(Items.KNOWLEDGE_BOOK)
                .setName(resultName != null ? resultName
                        : Text.literal(recipe.name).formatted(Formatting.GOLD))
                .glow()
                .build());

        // 3×3 crafting grid
        for (int i = 0; i < 9; i++) {
            ItemStack ing = (recipe.ingredients != null && i < recipe.ingredients.length)
                    ? recipe.ingredients[i] : null;
            if (ing != null && !ing.isEmpty()) {
                GuiElementBuilder eb = new GuiElementBuilder(ing.getItem());
                Text ingName = ing.get(DataComponentTypes.CUSTOM_NAME);
                if (ingName != null) eb.setName(ingName);
                if (ing.getCount() > 1) eb.setCount(ing.getCount());
                gui.setSlot(GRID_SLOTS[i], eb.build());
            }
        }

        // Arrow
        gui.setSlot(SLOT_ARROW, new GuiElementBuilder(Items.SPECTRAL_ARROW)
                .setName(Text.literal("→").formatted(Formatting.YELLOW))
                .build());

        // Result
        if (!recipe.result.isEmpty()) {
            GuiElementBuilder res = new GuiElementBuilder(recipe.result.getItem());
            Text rName = recipe.result.get(DataComponentTypes.CUSTOM_NAME);
            if (rName != null) res.setName(rName);
            res.glow();
            gui.setSlot(SLOT_RESULT, res.build());
        }

        // Recipe info
        String slayerName = recipe.requiredSlayer != null
                ? recipe.requiredSlayer.displayName : "Any";
        gui.setSlot(34, new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal("§eRecipe Info"))
                .addLoreLine(Text.literal("§7Type: §f" + slayerName))
                .addLoreLine(Text.literal("§7Required Level: §f" + recipe.requiredLevel))
                .build());

        // Back button
        gui.setSlot(SLOT_BACK, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openRecipeList(player))
                .build());

        gui.open();
    }
}
