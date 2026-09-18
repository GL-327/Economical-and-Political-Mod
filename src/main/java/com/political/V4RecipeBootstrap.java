package com.political;


import com.political.recipe.RecipeConfigManager;
import com.political.recipe.SlayerRecipes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * Central recipe bootstrap for every physical (non-magic) item added by V4.
 *
 * Magic gear (robes, catalysts, grimoires, technique relics, the Archmage set and the
 * Awakening Catalyst) is registered by the magic bootstrap and is intentionally NOT
 * handled here.
 *
 * Everything registered here goes through {@link RecipeConfigManager#upsertCustomRecipesSilent(List)}
 * so it lands in {@code config/political/custom_recipes.json} and therefore shows up in BOTH
 * {@code /recipe} and {@code /setrecipe}.
 *
 * <h3>Why this class exists</h3>
 * the in-memory {@code SlayerRecipes.RECIPES} list. That list is {@code clear()}ed by
 * {@code SlayerRecipes.registerRecipes()}, which the server calls AFTER the backrooms
 * registration ran — so the four Backrooms recipes were silently wiped every boot and never
 * persisted to JSON. {@code CornerstoneItem.registerRecipe()} was never called at all, leaving
 * the entry point to the whole settlement system uncraftable.
 *
 * <h3>Naming</h3>
 * The recipe name is the map key in custom_recipes.json — duplicates silently overwrite each
 * other. Every name here is prefixed {@code "V4 "} and was checked against the existing 2047
 * entries for collisions.
 */
public final class V4RecipeBootstrap {

    private V4RecipeBootstrap() {}

    /** Recipe-name prefix. Keep this on every recipe added by this class. */
    public static final String PREFIX = "V4 ";

    /** Idempotent — safe to call more than once per boot (upsert replaces by output id). */
    public static void ensureRecipes() {
        List<SlayerRecipes.Recipe> out = new ArrayList<>();

        addSettlementRecipes(out);

        if (!out.isEmpty()) {
            try {
                RecipeConfigManager.upsertCustomRecipesSilent(out);
                System.out.println("[V4RecipeBootstrap] Registered " + out.size() + " V4 recipes.");
            } catch (Throwable t) {
                System.err.println("[V4RecipeBootstrap] Failed to register recipes: " + t.getMessage());
            }
        }
    }

    // ── Settlements ───────────────────────────────────────────────────────────

    private static void addSettlementRecipes(List<SlayerRecipes.Recipe> out) {
        // The Cornerstone — entry point to the entire settlement system.
        // Shaped: emerald blocks on the corners, gold blocks on the edges, a bell in the middle.
        //   [E] [G] [E]
        //   [G] [B] [G]
        //   [E] [G] [E]
        ItemStack[] grid = new ItemStack[]{
                new ItemStack(Items.EMERALD_BLOCK), new ItemStack(Items.GOLD_BLOCK),  new ItemStack(Items.EMERALD_BLOCK),
                new ItemStack(Items.GOLD_BLOCK),    new ItemStack(Items.BELL),        new ItemStack(Items.GOLD_BLOCK),
                new ItemStack(Items.EMERALD_BLOCK), new ItemStack(Items.GOLD_BLOCK),  new ItemStack(Items.EMERALD_BLOCK)
        };
        out.add(shaped("The Cornerstone", CornerstoneItem.create(), grid));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Position-sensitive recipe (every slot locked). */
    private static SlayerRecipes.Recipe shaped(String name, ItemStack result, ItemStack[] grid) {
        boolean[] locked = new boolean[9];
        for (int i = 0; i < 9; i++) locked[i] = true;
        return new SlayerRecipes.Recipe(PREFIX + name, result, normalize(grid), locked, null, 0);
    }

    /** Order-independent recipe (no slot locked). */
    private static SlayerRecipes.Recipe shapeless(String name, ItemStack result, ItemStack[] grid) {
        boolean[] locked = new boolean[9]; // all false
        return new SlayerRecipes.Recipe(PREFIX + name, result, normalize(grid), locked, null, 0);
    }

    /** Guarantees a 9-length array with no nulls. */
    private static ItemStack[] normalize(ItemStack[] grid) {
        ItemStack[] fixed = new ItemStack[9];
        for (int i = 0; i < 9; i++) {
            ItemStack s = (grid != null && i < grid.length) ? grid[i] : null;
            fixed[i] = (s == null) ? ItemStack.EMPTY : s;
        }
        return fixed;
    }
}
