package com.political;

import java.util.List;

public class RecipeConfigManager {

    /** Returns all registered crafting recipes. */
    public static List<SlayerRecipes.Recipe> getRecipes() {
        return SlayerRecipes.getRecipes();
    }

    /** Finds a recipe by name. */
    public static SlayerRecipes.Recipe getRecipe(String name) {
        for (SlayerRecipes.Recipe r : getRecipes()) {
            if (r.name.equalsIgnoreCase(name)) return r;
        }
        return null;
    }
}
