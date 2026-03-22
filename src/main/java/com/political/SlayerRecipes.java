package com.political;

import net.minecraft.item.ItemStack;
import java.util.ArrayList;
import java.util.List;

public class SlayerRecipes {

    public static class Recipe {
        public final String name;
        public final ItemStack result;
        public final ItemStack[] ingredients; // 9 slots (3x3 grid)
        public final boolean[] lockedSlots; // Which slots are locked (true = locked)
        public final SlayerManager.SlayerType requiredSlayer;
        public final int requiredLevel;

        public Recipe(String name, ItemStack result, ItemStack[] ingredients,
                      SlayerManager.SlayerType requiredSlayer, int requiredLevel) {
            this(name, result, ingredients, null, requiredSlayer, requiredLevel);
        }

        public Recipe(String name, ItemStack result, ItemStack[] ingredients, boolean[] lockedSlots,
                      SlayerManager.SlayerType requiredSlayer, int requiredLevel) {
            this.name = name;
            this.result = result;
            this.ingredients = ingredients;
            this.lockedSlots = lockedSlots != null ? lockedSlots.clone() : new boolean[9];
            this.requiredSlayer = requiredSlayer;
            this.requiredLevel = requiredLevel;
        }
    }
    public static final int LEATHER_HELMET_DURABILITY = 550;      // Default: 55
    public static final int LEATHER_CHESTPLATE_DURABILITY = 800;  // Default: 80
    public static final int LEATHER_LEGGINGS_DURABILITY = 750;    // Default: 75
    public static final int LEATHER_BOOTS_DURABILITY = 650;       // Default: 65
    public static final int NETHERITE_CHESTPLATE_DURABILITY = 4070; // Default: 407
    public static final int BOW_DURABILITY = 3840;                // Default: 384
    // Level requirements are defined canonically in SlayerItems

    static final List<Recipe> RECIPES = new ArrayList<>();

    public static void registerRecipes() {
        RECIPES.clear();
        // All recipes are now loaded as custom recipes in RecipeConfigManager.load()
        // Just copy them from CUSTOM_RECIPES to RECIPES
        List<Recipe> customRecipes = RecipeConfigManager.getCustomRecipes();
        RECIPES.addAll(customRecipes);
        
        System.out.println("[SlayerRecipes] Registered " + RECIPES.size() + " total recipes (all treated as custom)");
    }

    public static List<Recipe> getRecipes() {
        return java.util.Collections.unmodifiableList(RECIPES);
    }

    public static List<Recipe> getAllRecipes() {
        return new ArrayList<>(RECIPES);
    }

    // Find recipe that produces the given output item
    public static Recipe getRecipeForOutput(ItemStack output) {
        for (Recipe recipe : RECIPES) {
            if (ItemStack.areItemsAndComponentsEqual(recipe.result, output)) {
                return recipe;
            }
        }
        return null;
    }
}
