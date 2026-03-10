package com.political;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages custom crafting recipes that can be set via /setrecipe.
 * Recipes are persisted to config/political/custom_recipes.json and
 * loaded on server start. The combined list (hardcoded + custom) is
 * provided to BountyCraftingHandler for matching.
 */
public class RecipeConfigManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_FILE = Path.of("config/political/custom_recipes.json");

    /** Custom recipes added via /setrecipe (in addition to hardcoded ones). */
    private static final List<SlayerRecipes.Recipe> CUSTOM_RECIPES = new ArrayList<>();

    // ── Lifecycle ──────────────────────────────────────────────

    /** Load persisted custom recipes from disk. Call on server start. */
    public static void load() {
        CUSTOM_RECIPES.clear();
        if (!Files.exists(CONFIG_FILE)) return;
        try (Reader reader = Files.newBufferedReader(CONFIG_FILE)) {
            JsonArray arr = GSON.fromJson(reader, JsonArray.class);
            if (arr == null) return;
            for (var elem : arr) {
                SlayerRecipes.Recipe recipe = deserialize(elem.getAsJsonObject());
                if (recipe != null) CUSTOM_RECIPES.add(recipe);
            }
        } catch (Exception e) {
            System.err.println("[RecipeConfigManager] Failed to load custom recipes: " + e.getMessage());
        }
    }

    /** Save current custom recipes to disk. */
    public static void save() {
        try {
            Files.createDirectories(CONFIG_FILE.getParent());
            JsonArray arr = new JsonArray();
            for (SlayerRecipes.Recipe r : CUSTOM_RECIPES) {
                arr.add(serialize(r));
            }
            try (Writer writer = Files.newBufferedWriter(CONFIG_FILE)) {
                GSON.toJson(arr, writer);
            }
        } catch (Exception e) {
            System.err.println("[RecipeConfigManager] Failed to save custom recipes: " + e.getMessage());
        }
    }

    // ── Public API ─────────────────────────────────────────────

    /**
     * Returns all registered crafting recipes — both hardcoded SlayerRecipes
     * and any custom recipes added via /setrecipe.
     */
    public static List<SlayerRecipes.Recipe> getRecipes() {
        List<SlayerRecipes.Recipe> all = new ArrayList<>(SlayerRecipes.getRecipes());
        all.addAll(CUSTOM_RECIPES);
        return all;
    }

    /** Returns only the custom (admin-set) recipes. */
    public static List<SlayerRecipes.Recipe> getCustomRecipes() {
        return new ArrayList<>(CUSTOM_RECIPES);
    }

    /** Adds a new custom recipe and persists it. */
    public static void addCustomRecipe(SlayerRecipes.Recipe recipe) {
        CUSTOM_RECIPES.add(recipe);
        save();
    }

    /** Removes a custom recipe by name and persists. */
    public static boolean removeCustomRecipe(String name) {
        boolean removed = CUSTOM_RECIPES.removeIf(r -> r.name.equalsIgnoreCase(name));
        if (removed) save();
        return removed;
    }

    /** Finds a recipe by name (searches both hardcoded and custom). */
    public static SlayerRecipes.Recipe getRecipe(String name) {
        for (SlayerRecipes.Recipe r : getRecipes()) {
            if (r.name.equalsIgnoreCase(name)) return r;
        }
        return null;
    }

    // ── Serialization ──────────────────────────────────────────

    private static JsonObject serialize(SlayerRecipes.Recipe recipe) {
        JsonObject obj = new JsonObject();
        obj.addProperty("name", recipe.name);
        obj.addProperty("requiredSlayer", recipe.requiredSlayer != null ? recipe.requiredSlayer.name() : "");
        obj.addProperty("requiredLevel", recipe.requiredLevel);
        obj.addProperty("result", getItemId(recipe.result));

        JsonArray grid = new JsonArray();
        if (recipe.ingredients != null) {
            for (ItemStack ing : recipe.ingredients) {
                grid.add(ing == null || ing.isEmpty() ? "" : getItemId(ing));
            }
        } else {
            for (int i = 0; i < 9; i++) grid.add("");
        }
        obj.add("ingredients", grid);
        return obj;
    }

    private static SlayerRecipes.Recipe deserialize(JsonObject obj) {
        try {
            String name = obj.get("name").getAsString();
            String slayerStr = obj.get("requiredSlayer").getAsString();
            int level = obj.get("requiredLevel").getAsInt();
            String resultId = obj.get("result").getAsString();

            SlayerManager.SlayerType slayerType = null;
            if (!slayerStr.isEmpty()) {
                try { slayerType = SlayerManager.SlayerType.valueOf(slayerStr); } catch (Exception ignored) {}
            }

            ItemStack result = resolveItem(resultId);
            if (result.isEmpty()) return null;

            JsonArray gridArr = obj.getAsJsonArray("ingredients");
            ItemStack[] grid = new ItemStack[9];
            for (int i = 0; i < 9 && i < gridArr.size(); i++) {
                String id = gridArr.get(i).getAsString();
                grid[i] = id.isEmpty() ? ItemStack.EMPTY : resolveItem(id);
            }
            for (int i = 0; i < 9; i++) {
                if (grid[i] == null) grid[i] = ItemStack.EMPTY;
            }

            return new SlayerRecipes.Recipe(name, result, grid, slayerType, level);
        } catch (Exception e) {
            return null;
        }
    }

    private static String getItemId(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return "";
        // Prefer custom item ID for custom items
        String customId = SlayerItems.getCustomItemId(stack);
        if (customId != null) return "political:" + customId;
        // Fall back to registry ID
        Identifier id = Registries.ITEM.getId(stack.getItem());
        return id.toString();
    }

    private static ItemStack resolveItem(String id) {
        if (id == null || id.isEmpty()) return ItemStack.EMPTY;
        if (id.startsWith("political:")) {
            String customId = id.substring("political:".length());
            return resolveCustomItem(customId);
        }
        // Vanilla item
        try {
            Identifier itemId = Identifier.of(id);
            Item item = Registries.ITEM.get(itemId);
            if (item == Items.AIR) return ItemStack.EMPTY;
            return new ItemStack(item);
        } catch (Exception e) {
            return ItemStack.EMPTY;
        }
    }

    private static ItemStack resolveCustomItem(String customId) {
        // Map custom IDs to item creation methods
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            String typeLower = type.name().toLowerCase();
            if (customId.equals(typeLower + "_sword")) return SlayerItems.createSlayerSword(type);
            if (customId.equals(typeLower + "_sword_t2")) return SlayerItems.createUpgradedSlayerSword(type);
            if (customId.equals(typeLower + "_core")) return SlayerItems.createCore(type);
            if (customId.equals(typeLower + "_chunk")) return SlayerItems.createChunk(type);
        }
        if ("zombie_berserker_helmet".equals(customId)) return SlayerItems.createZombieBerserkerHelmet();
        if ("spider_leggings".equals(customId)) return SlayerItems.createSpiderLeggings();
        if ("skeleton_bow".equals(customId)) return SlayerItems.createSkeletonBow();
        if ("slime_boots".equals(customId)) return SlayerItems.createSlimeBoots();
        if ("warden_chestplate".equals(customId)) return SlayerItems.createWardenChestplate();
        return ItemStack.EMPTY;
    }
}
