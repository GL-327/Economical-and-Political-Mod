package com.political;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryOps;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Manages custom crafting recipes with support for preset defaults and user overrides.
 * Preset recipes are loaded from the file initially and act as defaults.
 * User overrides via /setrecipe take precedence and are saved separately.
 * Both lists persist across server restarts.
 */
public class RecipeConfigManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    // Use server working-directory relative config path.
    private static final Path CONFIG_DIR = java.nio.file.Paths.get("config", "political");
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("custom_recipes.json");
    // Modular recipe file path (unused but kept for potential future use)
    private static final java.nio.file.Path MODULAR_FILE = CONFIG_DIR.resolve("modular_recipes.json");
    private static final Path DISABLED_FILE = CONFIG_DIR.resolve("disabled_recipes.json");

    static {
        boolean fileExists = java.nio.file.Files.exists(CONFIG_FILE);
        System.out.println("[RecipeConfigManager] JSON: " + (fileExists ? "OK" : "MISSING") + " | Path: " + CONFIG_FILE);
    }

    /** Custom recipes added via /setrecipe (override defaults). */
    private static final List<SlayerRecipes.Recipe> CUSTOM_RECIPES = new ArrayList<>();

    /** Names of recipes that are currently disabled. */
    private static final java.util.Set<String> DISABLED_RECIPES = new java.util.HashSet<>();

     private static final Object RECIPE_CACHE_LOCK = new Object();
     private static volatile List<SlayerRecipes.Recipe> RECIPE_CACHE = java.util.Collections.emptyList();
     private static volatile long RECIPE_CACHE_LAST_MODIFIED = -1L;
     private static volatile long RECIPE_CACHE_LAST_SIZE = -1L;
     private static volatile long LAST_CRAFTING_STATUS_PRINT_MS = 0L;

    // ── Lifecycle ──────────────────────────────────────────────

    /** Load persisted recipes from disk. Call on server start. */
    public static void load() {
        // Regenerate recipes from code on startup to ensure correct NBT data
        regenerateRecipes();
        invalidateRecipeCache();
        SlayerRecipes.RECIPES.clear();
        loadDisabled();
    }

    private static void invalidateRecipeCache() {
        synchronized (RECIPE_CACHE_LOCK) {
            RECIPE_CACHE_LAST_MODIFIED = -1L;
            RECIPE_CACHE_LAST_SIZE = -1L;
            RECIPE_CACHE = java.util.Collections.emptyList();
        }
    }

    private static List<SlayerRecipes.Recipe> getRecipesCached(String contextForLog) {
        try {
            if (!Files.exists(CONFIG_FILE)) {
                return java.util.Collections.emptyList();
            }

            var attrs = Files.readAttributes(CONFIG_FILE, java.nio.file.attribute.BasicFileAttributes.class);
            long modified = attrs.lastModifiedTime().toMillis();
            long size = attrs.size();

            if (modified == RECIPE_CACHE_LAST_MODIFIED && size == RECIPE_CACHE_LAST_SIZE && !RECIPE_CACHE.isEmpty()) {
                return RECIPE_CACHE;
            }

            synchronized (RECIPE_CACHE_LOCK) {
                // Double-check inside lock
                if (modified == RECIPE_CACHE_LAST_MODIFIED && size == RECIPE_CACHE_LAST_SIZE && !RECIPE_CACHE.isEmpty()) {
                    return RECIPE_CACHE;
                }

                List<SlayerRecipes.Recipe> recipes = new ArrayList<>();
                int failed = 0;

                String jsonString = Files.readString(CONFIG_FILE);
                JsonObject root = GSON.fromJson(jsonString, JsonObject.class);
                if (root != null && root.has("recipes")) {
                    JsonArray arr = root.getAsJsonArray("recipes");
                    for (var elem : arr) {
                        try {
                            SlayerRecipes.Recipe recipe = deserialize(elem.getAsJsonObject());
                            if (recipe != null && recipe.result != null && !recipe.result.isEmpty()) {
                                recipes.add(recipe);
                            } else {
                                failed++;
                            }
                        } catch (Exception e) {
                            failed++;
                        }
                    }
                }

                RECIPE_CACHE = recipes;
                RECIPE_CACHE_LAST_MODIFIED = modified;
                RECIPE_CACHE_LAST_SIZE = size;

                if (contextForLog != null && !contextForLog.isBlank()) {
                    // Only log crafting reloads (rate-limited) to avoid spam.
                    if ("crafting".equals(contextForLog)) {
                        long now = System.currentTimeMillis();
                        if (now - LAST_CRAFTING_STATUS_PRINT_MS > 10_000L) {
                            LAST_CRAFTING_STATUS_PRINT_MS = now;
                            System.out.println("[RecipeConfigManager] crafting: OK | " + recipes.size() + " recipes (" + failed + " failed)");
                        }
                    } else if ("recipe".equals(contextForLog)) {
                        System.out.println("[RecipeConfigManager] /recipe: loaded " + recipes.size() + " recipes (" + failed + " failed)");
                    }
                }

                return RECIPE_CACHE;
            }
        } catch (Exception e) {
            return java.util.Collections.emptyList();
        }
    }

    public static void logRecipeFileStatusForRecipeCommand() {
        try {
            boolean exists = Files.exists(CONFIG_FILE);
            boolean readable = Files.isReadable(CONFIG_FILE);
            long size = exists ? Files.size(CONFIG_FILE) : 0;
            System.out.println("[RecipeConfigManager] /recipe file: " + (exists && readable ? "OK" : "BAD") + " | size=" + size + " | " + CONFIG_FILE);
        } catch (Exception e) {
            System.out.println("[RecipeConfigManager] /recipe file: BAD | " + e.getMessage());
        }
    }

    /** Load the set of disabled recipe names from disk. */
    private static void loadDisabled() {
        DISABLED_RECIPES.clear();
        if (!Files.exists(DISABLED_FILE)) return;
        try (Reader reader = Files.newBufferedReader(DISABLED_FILE)) {
            JsonArray arr = GSON.fromJson(reader, JsonArray.class);
            if (arr == null) return;
            for (var elem : arr) {
                DISABLED_RECIPES.add(elem.getAsString());
            }
        } catch (Exception e) {
            System.err.println("[RecipeConfigManager] Failed to load disabled recipes: " + e.getMessage());
        }
    }

    /** Persist the set of disabled recipe names to disk. */
    private static void saveDisabled() {
        try {
            Files.createDirectories(CONFIG_DIR);
            JsonArray arr = new JsonArray();
            for (String name : DISABLED_RECIPES) arr.add(name);
            try (Writer writer = Files.newBufferedWriter(DISABLED_FILE)) {
                GSON.toJson(arr, writer);
            }
        } catch (Exception e) {
            System.err.println("[RecipeConfigManager] Failed to save disabled recipes: " + e.getMessage());
        }
    }

    // ── Public API ─────────────────────────────────────────────

    /**
     * Returns all recipes by reading directly from JSON file (real-time).
     */
    public static List<SlayerRecipes.Recipe> getRecipes() {
        // Used by /recipe UI; log when /recipe command is used
        return new ArrayList<>(getRecipesCached("recipe"));
    }
    
    /**
     * Returns all recipes from JSON file (real-time).
     */
    public static List<SlayerRecipes.Recipe> getCustomRecipes() {
        // Called frequently by crafting code; use cached recipes and only reload when file changes.
        return new ArrayList<>(getRecipesCached("crafting"));
    }

    /** Adds or updates a custom recipe override directly to JSON file */
    public static void addCustomRecipe(SlayerRecipes.Recipe recipe) {
        try {
            // Load existing recipes from disk
            Map<String, JsonObject> existingRecipes = new java.util.HashMap<>();
            if (Files.exists(CONFIG_FILE)) {
                String jsonString = Files.readString(CONFIG_FILE);
                JsonObject existing = GSON.fromJson(jsonString, JsonObject.class);
                if (existing != null && existing.has("recipes")) {
                    JsonArray arr = existing.getAsJsonArray("recipes");
                    for (var elem : arr) {
                        JsonObject obj = elem.getAsJsonObject();
                        if (obj.has("name")) {
                            existingRecipes.put(obj.get("name").getAsString(), obj);
                        }
                    }
                }
            }

            // Remove any existing recipe with same output (robust to nbt/id encoded stacks).
            String newOutputId = SlayerItems.getCustomItemId(recipe.result);
            int removed = 0;
            for (java.util.Iterator<Map.Entry<String, JsonObject>> it = existingRecipes.entrySet().iterator(); it.hasNext();) {
                Map.Entry<String, JsonObject> entry = it.next();
                JsonObject recipeObj = entry.getValue();
                if (!recipeObj.has("result")) continue;
                try {
                    ItemStack existingResult = deserializeStack(recipeObj.get("result"));
                    String existingOutputId = SlayerItems.getCustomItemId(existingResult);
                    if (newOutputId != null && !newOutputId.isEmpty() && newOutputId.equals(existingOutputId)) {
                        it.remove();
                        removed++;
                        continue;
                    }
                } catch (Exception ignored) {
                    // Fallback below for malformed entries.
                }

                // Fallback string-based match for legacy malformed entries.
                try {
                    JsonObject resultObj = recipeObj.getAsJsonObject("result");
                    if (resultObj != null && resultObj.has("nbt")) {
                        String nbt = resultObj.get("nbt").getAsString();
                        if (nbt.contains("custom_item_id\":\"" + newOutputId + "\"") || nbt.contains("custom_id\":\"" + newOutputId + "\"")) {
                            it.remove();
                            removed++;
                        }
                    }
                } catch (Exception ignored) {
                    // ignore malformed recipe object
                }
            }

            // Add new recipe
            existingRecipes.put(recipe.name, serialize(recipe));
            
            // Save back to file
            JsonArray recipesArr = new JsonArray();
            for (JsonObject obj : existingRecipes.values()) {
                recipesArr.add(obj);
            }
            
            JsonObject root = new JsonObject();
            root.add("recipes", recipesArr);
            
            Files.createDirectories(CONFIG_FILE.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_FILE)) {
                GSON.toJson(root, writer);
            }
            
            System.out.println("[RecipeConfigManager] /setrecipe: OK | Wrote '" + recipe.name + "' (" + removed + " replaced)");
            
            // Force cache refresh on next access
            invalidateRecipeCache();
            
        } catch (Exception e) {
            System.err.println("[RecipeConfigManager] Failed to save recipe: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /** Removes a custom recipe by name and persists directly to JSON */
    public static boolean removeCustomRecipe(String name) {
        try {
            // Load existing recipes
            Map<String, JsonObject> existingRecipes = new java.util.HashMap<>();
            if (Files.exists(CONFIG_FILE)) {
                String jsonString = Files.readString(CONFIG_FILE);
                JsonObject existing = GSON.fromJson(jsonString, JsonObject.class);
                if (existing != null && existing.has("recipes")) {
                    JsonArray arr = existing.getAsJsonArray("recipes");
                    for (var elem : arr) {
                        JsonObject obj = elem.getAsJsonObject();
                        if (obj.has("name")) {
                            existingRecipes.put(obj.get("name").getAsString(), obj);
                        }
                    }
                }
            }

            // Remove recipe by name
            boolean removed = existingRecipes.remove(name) != null;
            
            if (removed) {
                // Save back to file
                JsonArray recipesArr = new JsonArray();
                for (JsonObject obj : existingRecipes.values()) {
                    recipesArr.add(obj);
                }
                
                JsonObject root = new JsonObject();
                root.add("recipes", recipesArr);
                
                try (Writer writer = Files.newBufferedWriter(CONFIG_FILE)) {
                    GSON.toJson(root, writer);
                }
                
                System.out.println("[RecipeConfigManager] Removed recipe '" + name + "' from JSON");
                
                // Reload recipes into memory
                load();
            }
            
            return removed;
        } catch (Exception e) {
            System.err.println("[RecipeConfigManager] Failed to remove recipe: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /** Regenerates all recipes from code and saves to JSON file */
    public static void regenerateRecipes() {
        try {
            JsonArray recipes = RecipeGenerator.generateAllDefaultRecipes();
            
            JsonObject root = new JsonObject();
            root.add("recipes", recipes);
            
            Files.createDirectories(CONFIG_FILE.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_FILE)) {
                GSON.toJson(root, writer);
            }
            
            System.out.println("[RecipeConfigManager] Regenerated " + recipes.size() + " recipes to " + CONFIG_FILE);
            invalidateRecipeCache();
            
        } catch (Exception e) {
            System.err.println("[RecipeConfigManager] Failed to regenerate recipes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /** Returns true if a recipe with this name is currently enabled. */
    public static boolean isRecipeEnabled(String name) {
        return !DISABLED_RECIPES.contains(normalise(name));
    }

    /**
     * Toggles a recipe between enabled and disabled.
     * @return true if the recipe is now enabled, false if now disabled.
     */
    public static boolean toggleRecipe(String name) {
        String key = normalise(name);
        if (DISABLED_RECIPES.contains(key)) {
            DISABLED_RECIPES.remove(key);
        } else {
            DISABLED_RECIPES.add(key);
        }
        saveDisabled();
        return !DISABLED_RECIPES.contains(key);
    }

    /**
     * Returns all enabled crafting recipes — both hardcoded SlayerRecipes and custom ones,
     * excluding any that have been disabled via {@link #toggleRecipe}.
     */
    public static List<SlayerRecipes.Recipe> getEnabledRecipes() {
        List<SlayerRecipes.Recipe> all = getRecipes();
        all.removeIf(r -> !isRecipeEnabled(r.name));
        return all;
    }

    private static String normalise(String name) {
        return name == null ? "" : name.trim().toLowerCase();
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
        obj.add("result", serializeStack(recipe.result));

        JsonArray grid = new JsonArray();
        if (recipe.ingredients != null) {
            for (ItemStack ing : recipe.ingredients) {
                grid.add(serializeIngredient(ing));
            }
        } else {
            for (int i = 0; i < 9; i++) grid.add("");
        }
        obj.add("ingredients", grid);

        JsonArray locked = new JsonArray();
        if (recipe.lockedSlots != null) {
            for (boolean lockedSlot : recipe.lockedSlots) {
                locked.add(lockedSlot);
            }
        } else {
            for (int i = 0; i < 9; i++) locked.add(false);
        }
        obj.add("lockedSlots", locked);

        return obj;
    }

    // This method is kept for potential future use
    @SuppressWarnings("unused")
    private static SlayerRecipes.Recipe deserializeBaseRecipe(JsonObject obj) {
        try {
            String name = obj.get("name").getAsString();
            String slayerStr = obj.get("requiredSlayer").getAsString();
            int level = obj.get("requiredLevel").getAsInt();
            
            // Log recipe being loaded
            System.out.println("[RecipeConfigManager] Loading base recipe: " + name);
            
            // Handle result with nbt field
            JsonObject resultObj = obj.getAsJsonObject("result");
            ItemStack result = deserializeStack(resultObj);
            
            if (result.isEmpty()) {
                System.err.println("[RecipeConfigManager] FAILED to load base recipe '" + name + "': result item is empty");
                return null;
            }
            
            System.out.println("[RecipeConfigManager] Successfully parsed result for base recipe: " + name);

            SlayerManager.SlayerType slayerType = null;
            if (!slayerStr.isEmpty()) {
                try { slayerType = SlayerManager.SlayerType.valueOf(slayerStr); } catch (Exception ignored) {}
            }

            JsonArray gridArr = obj.getAsJsonArray("ingredients");
            ItemStack[] grid = new ItemStack[9];
            for (int i = 0; i < 9 && i < gridArr.size(); i++) {
                grid[i] = deserializeIngredient(gridArr.get(i));
            }
            for (int i = 0; i < 9; i++) {
                if (grid[i] == null) grid[i] = ItemStack.EMPTY;
            }

            // Deserialize locked slots
            boolean[] lockedSlots = new boolean[9];
            if (obj.has("lockedSlots")) {
                JsonArray lockedArr = obj.getAsJsonArray("lockedSlots");
                for (int i = 0; i < 9 && i < lockedArr.size(); i++) {
                    lockedSlots[i] = lockedArr.get(i).getAsBoolean();
                }
            }

            System.out.println("[RecipeConfigManager] Successfully loaded base recipe: " + name);
            return new SlayerRecipes.Recipe(name, result, grid, lockedSlots, slayerType, level);
        } catch (Exception e) {
            System.err.println("[RecipeConfigManager] Error deserializing base recipe: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private static SlayerRecipes.Recipe deserialize(JsonObject obj) {
        try {
            String name = obj.get("name").getAsString();
            String slayerStr = obj.get("requiredSlayer").getAsString();
            int level = obj.get("requiredLevel").getAsInt();

            ItemStack result = deserializeStack(obj.get("result"));
            
            if (result.isEmpty()) {
                System.err.println("[RecipeConfigManager] FAILED to load recipe '" + name + "': result item is empty");
                return null;
            }
            
            SlayerManager.SlayerType slayerType = null;
            if (!slayerStr.isEmpty()) {
                try { slayerType = SlayerManager.SlayerType.valueOf(slayerStr); } catch (Exception ignored) {}
            }

            JsonArray gridArr = obj.getAsJsonArray("ingredients");
            ItemStack[] grid = new ItemStack[9];
            for (int i = 0; i < 9 && i < gridArr.size(); i++) {
                grid[i] = deserializeIngredient(gridArr.get(i));
            }
            for (int i = 0; i < 9; i++) {
                if (grid[i] == null) grid[i] = ItemStack.EMPTY;
            }

            // Deserialize locked slots
            boolean[] lockedSlots = new boolean[9];
            if (obj.has("lockedSlots")) {
                JsonArray lockedArr = obj.getAsJsonArray("lockedSlots");
                for (int i = 0; i < 9 && i < lockedArr.size(); i++) {
                    lockedSlots[i] = lockedArr.get(i).getAsBoolean();
                }
            }

            return new SlayerRecipes.Recipe(name, result, grid, lockedSlots, slayerType, level);
        } catch (Exception e) {
            return null;
        }
    }

    private static com.google.gson.JsonElement serializeStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return new com.google.gson.JsonPrimitive("");
        
        try {
            RegistryOps<net.minecraft.nbt.NbtElement> ops = null;
            if (PoliticalServer.server != null) {
                ops = PoliticalServer.server.getRegistryManager().getOps(NbtOps.INSTANCE);
            } else {
                ops = net.minecraft.registry.BuiltinRegistries.createWrapperLookup().getOps(NbtOps.INSTANCE);
            }
            
            if (ops != null) {
                var result = ItemStack.CODEC.encodeStart(ops, stack);
                var nbtResult = result.result().orElse(null);
                if (nbtResult != null) {
                    JsonObject obj = new JsonObject();
                    obj.addProperty("nbt", nbtResult.toString());
                    obj.addProperty("count", stack.getCount());
                    return obj;
                }
            }
        } catch (Exception e) {
            System.err.println("[RecipeConfigManager] Failed to serialize stack: " + e.getMessage());
        }
        
        return new com.google.gson.JsonPrimitive("");
    }
    
    /**
     * Public method to serialize ItemStack for external use
     */
    public static com.google.gson.JsonElement serializeItemStack(ItemStack stack) {
        return serializeStack(stack);
    }

    private static com.google.gson.JsonElement serializeIngredient(ItemStack ing) {
        if (ing == null || ing.isEmpty()) return new com.google.gson.JsonPrimitive("");
        return serializeStack(ing);
    }

    private static ItemStack deserializeStack(com.google.gson.JsonElement elem) {
        if (elem == null || elem.isJsonNull()) return ItemStack.EMPTY;
        if (elem.isJsonPrimitive()) {
            String id = elem.getAsString();
            return id == null || id.isEmpty() ? ItemStack.EMPTY : resolveItem(id);
        }
        if (!elem.isJsonObject()) return ItemStack.EMPTY;
        JsonObject obj = elem.getAsJsonObject();

        // Try NBT parsing first (this is what most recipes use)
        if (obj.has("nbt")) {
            try {
                String nbtString = obj.get("nbt").getAsString();
                if (nbtString != null && !nbtString.isEmpty()) {
                    // IMPORTANT: This file stores SNBT (not JSON). Do NOT try to quote keys.
                    NbtCompound parsed = StringNbtReader.readCompound(nbtString);

                    // Normalize legacy or loosely-formatted components into the schema expected by ItemStack.CODEC.
                    normalizeParsedStackNbt(parsed);
                    
                    RegistryOps<net.minecraft.nbt.NbtElement> ops = null;
                    if (PoliticalServer.server != null) {
                        ops = PoliticalServer.server.getRegistryManager().getOps(NbtOps.INSTANCE);
                    } else {
                        ops = net.minecraft.registry.BuiltinRegistries.createWrapperLookup().getOps(NbtOps.INSTANCE);
                    }
                    
                    if (ops != null) {
                        var result = ItemStack.CODEC.parse(ops, parsed);
                        if (result.result().isPresent()) {
                            ItemStack stack = result.result().get();
                            int stackCount = obj.has("count") ? obj.get("count").getAsInt() : stack.getCount();
                            if (!stack.isEmpty() && stackCount > 1) stack.setCount(stackCount);
                            if (!stack.isEmpty()) return stack;
                        }
                    }

                    // Fallback: if SNBT parses but CODEC fails, try extracting minimal identifiers
                    ItemStack fallback = parseStackFromSnbtString(nbtString, obj);
                    if (fallback != null && !fallback.isEmpty()) return fallback;
                }
            } catch (Exception e) {
                try {
                    String nbtString = obj.get("nbt").getAsString();
                    ItemStack fallback = parseStackFromSnbtString(nbtString, obj);
                    if (fallback != null && !fallback.isEmpty()) return fallback;
                } catch (Exception ignored) {
                    // ignore
                }

            }
        }

        // Fallback: try manual parsing (id, count, customId, customName)
        String id = obj.has("id") ? obj.get("id").getAsString() : "";
        int count = obj.has("count") ? obj.get("count").getAsInt() : 1;
        String customId = obj.has("customId") ? obj.get("customId").getAsString() : "";
        String customName = obj.has("customName") ? obj.get("customName").getAsString() : "";

        if (id != null && !id.isEmpty()) {
            ItemStack stack = resolveItem(id);
            if (!stack.isEmpty()) {
                if (count > 1) stack.setCount(count);
                if (customId != null && !customId.isEmpty()) {
                    SlayerItems.setCustomItemId(stack, customId);
                }
                if (customName != null && !customName.isEmpty()) {
                    stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(customName));
                }
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    private static ItemStack parseStackFromSnbtString(String nbtString, JsonObject wrapperObj) {
        if (nbtString == null || nbtString.isEmpty()) return ItemStack.EMPTY;

        String customId = extractSnbtStringValue(nbtString, "custom_id");
        if (customId == null || customId.isEmpty()) {
            // legacy key used by some generators
            customId = extractSnbtStringValue(nbtString, "custom_item_id");
        }

        String id = extractSnbtStringValue(nbtString, "id");
        int count = wrapperObj != null && wrapperObj.has("count") ? wrapperObj.get("count").getAsInt() : 1;

        // Prefer resolving by custom id (ensures exact NBT matches SlayerItems.create* methods)
        ItemStack stack = ItemStack.EMPTY;
        if (customId != null && !customId.isEmpty()) {
            stack = resolveCustomItem(customId);
        }
        if ((stack == null || stack.isEmpty()) && id != null && !id.isEmpty()) {
            stack = resolveItem(id);
            if (!stack.isEmpty() && customId != null && !customId.isEmpty()) {
                SlayerItems.setCustomItemId(stack, customId);
            }
        }

        if (stack == null || stack.isEmpty()) return ItemStack.EMPTY;
        if (count > 1) stack.setCount(count);
        return stack;
    }

    private static String extractSnbtStringValue(String snbt, String key) {
        if (snbt == null || snbt.isEmpty() || key == null || key.isEmpty()) return null;

        // First try simple pattern: key:"value"
        String needle = key + ":\"";
        int start = snbt.indexOf(needle);
        if (start >= 0) {
            start += needle.length();
            int end = snbt.indexOf("\"", start);
            if (end > start) {
                return snbt.substring(start, end);
            }
        }

        // Custom ids are often nested under components -> minecraft:custom_data
        if ("custom_item_id".equals(key) || "custom_id".equals(key)) {
            // Support both quoted/unquoted minecraft:custom_data keys
            String[] customDataPatterns = {
                    "\"minecraft:custom_data\":{",
                    "minecraft:custom_data:{"
            };

            // Support both quoted/unquoted custom id keys
            String[] needles = {
                    "\"" + key + "\":\"", // quoted key
                    key + ":\""              // unquoted key
            };

            for (String customDataPattern : customDataPatterns) {
                int customDataStart = snbt.indexOf(customDataPattern);
                if (customDataStart < 0) continue;
                customDataStart += customDataPattern.length();

                for (String n : needles) {
                    start = snbt.indexOf(n, customDataStart);
                    if (start >= 0) {
                        start += n.length();
                        int end = snbt.indexOf("\"", start);
                        if (end > start) {
                            return snbt.substring(start, end);
                        }
                    }
                }
            }
        }

        return null;
    }

    private static void normalizeParsedStackNbt(NbtCompound parsed) {
        if (parsed == null) return;

        // Keep dyed_color as plain int - compound format causes CODEC errors
        if (!parsed.contains("components")) return;

        parsed.getCompound("components").ifPresent(components -> {
            // dyed_color stays as plain int, no conversion needed
        });
    }

    private static ItemStack deserializeIngredient(com.google.gson.JsonElement elem) {
        return deserializeStack(elem);
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

    // This method is kept for potential future use
    @SuppressWarnings("unused")
    private static ItemStack parseStackManually(JsonObject obj) {
        // Manual fallback parsing for malformed NBT
        String id = obj.has("id") ? obj.get("id").getAsString() : "";
        int count = obj.has("count") ? obj.get("count").getAsInt() : 1;
        
        // Extract custom_id from NBT string if present
        String customId = "";
        if (obj.has("nbt")) {
            String nbtString = obj.get("nbt").getAsString();
            if (nbtString.contains("custom_id:")) {
                int start = nbtString.indexOf("custom_id:") + 10;
                int end = nbtString.indexOf(",", start);
                if (end == -1) end = nbtString.indexOf("}", start);
                if (end > start) {
                    customId = nbtString.substring(start, end).replace("\"", "").trim();
                }
            }
        }
        
        ItemStack stack = resolveItem(id);
        if (stack.isEmpty() && !customId.isEmpty()) {
            stack = resolveCustomItem(customId);
        }
        
        if (!stack.isEmpty()) {
            stack.setCount(count);
        }
        
        return stack;
    }

    private static ItemStack resolveCustomItem(String customId) {
        // First try the custom crafter cores
        if ("custom_crafter_core_t1".equals(customId)) return SlayerItems.createCustomCrafterCoreT1();
        if ("custom_crafter_core_t2".equals(customId)) return SlayerItems.createCustomCrafterCoreT2();
        if ("custom_crafter_core_t3".equals(customId)) return SlayerItems.createCustomCrafterCoreT3();
        
        // Map custom IDs to item creation methods
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            String typeLower = type.name().toLowerCase();
            if (customId.equals(typeLower + "_sword")) return SlayerItems.createSlayerSword(type);
            if (customId.equals(typeLower + "_sword_t2")) return SlayerItems.createUpgradedSlayerSword(type);
            if (customId.equals(typeLower + "_core")) return SlayerItems.createCore(type);
            if (customId.equals(typeLower + "_chunk")) return SlayerItems.createChunk(type);
            // T1 armor pieces
            if (customId.equals(typeLower + "_t1_helmet"))     return SlayerItems.createSlayerHelmet(type, 1);
            if (customId.equals(typeLower + "_t1_chestplate")) return SlayerItems.createSlayerChestplate(type, 1);
            if (customId.equals(typeLower + "_t1_leggings"))   return SlayerItems.createSlayerLeggings(type, 1);
            if (customId.equals(typeLower + "_t1_boots"))      return SlayerItems.createSlayerBoots(type, 1);
            // T2 armor pieces
            if (customId.equals(typeLower + "_t2_helmet"))     return SlayerItems.createSlayerHelmet(type, 2);
            if (customId.equals(typeLower + "_t2_chestplate")) return SlayerItems.createSlayerChestplate(type, 2);
            if (customId.equals(typeLower + "_t2_leggings"))   return SlayerItems.createSlayerLeggings(type, 2);
            if (customId.equals(typeLower + "_t2_boots"))      return SlayerItems.createSlayerBoots(type, 2);
        }
        if ("zombie_berserker_helmet".equals(customId)) return SlayerItems.createZombieBerserkerHelmet();
        if ("spider_leggings".equals(customId)) return SlayerItems.createSpiderLeggings();
        if ("skeleton_bow".equals(customId)) return SlayerItems.createSkeletonBow();
        if ("slime_boots".equals(customId)) return SlayerItems.createSlimeBoots();
        if ("warden_chestplate".equals(customId)) return SlayerItems.createWardenChestplate();
        
        // Block armor items - direct mapping
        if (customId.startsWith("glass_")) {
            if ("glass_helmet".equals(customId)) return BlockArmor.createGlassHelmet();
            if ("glass_chestplate".equals(customId)) return BlockArmor.createGlassChestplate();
            if ("glass_leggings".equals(customId)) return BlockArmor.createGlassLeggings();
            if ("glass_boots".equals(customId)) return BlockArmor.createGlassBoots();
        }
        if (customId.startsWith("obsidian_")) {
            if ("obsidian_helmet".equals(customId)) return BlockArmor.createObsidianHelmet();
            if ("obsidian_chestplate".equals(customId)) return BlockArmor.createObsidianChestplate();
            if ("obsidian_leggings".equals(customId)) return BlockArmor.createObsidianLeggings();
            if ("obsidian_boots".equals(customId)) return BlockArmor.createObsidianBoots();
        }
        if (customId.startsWith("quartz_")) {
            if ("quartz_helmet".equals(customId)) return BlockArmor.createQuartzHelmet();
            if ("quartz_chestplate".equals(customId)) return BlockArmor.createQuartzChestplate();
            if ("quartz_leggings".equals(customId)) return BlockArmor.createQuartzLeggings();
            if ("quartz_boots".equals(customId)) return BlockArmor.createQuartzBoots();
        }
        if (customId.startsWith("glowstone_")) {
            if ("glowstone_helmet".equals(customId)) return BlockArmor.createGlowstoneHelmet();
            if ("glowstone_chestplate".equals(customId)) return BlockArmor.createGlowstoneChestplate();
            if ("glowstone_leggings".equals(customId)) return BlockArmor.createGlowstoneLeggings();
            if ("glowstone_boots".equals(customId)) return BlockArmor.createGlowstoneBoots();
        }
        if (customId.startsWith("redstone_")) {
            if ("redstone_helmet".equals(customId)) return BlockArmor.createRedstoneHelmet();
            if ("redstone_chestplate".equals(customId)) return BlockArmor.createRedstoneChestplate();
            if ("redstone_leggings".equals(customId)) return BlockArmor.createRedstoneLeggings();
            if ("redstone_boots".equals(customId)) return BlockArmor.createRedstoneBoots();
        }
        if (customId.startsWith("netherrack_")) {
            if ("netherrack_helmet".equals(customId)) return BlockArmor.createNetherrackHelmet();
            if ("netherrack_chestplate".equals(customId)) return BlockArmor.createNetherrackChestplate();
            if ("netherrack_leggings".equals(customId)) return BlockArmor.createNetherrackLeggings();
            if ("netherrack_boots".equals(customId)) return BlockArmor.createNetherrackBoots();
        }
        if (customId.startsWith("endstone_")) {
            if ("endstone_helmet".equals(customId)) return BlockArmor.createEndstoneHelmet();
            if ("endstone_chestplate".equals(customId)) return BlockArmor.createEndstoneChestplate();
            if ("endstone_leggings".equals(customId)) return BlockArmor.createEndstoneLeggings();
            if ("endstone_boots".equals(customId)) return BlockArmor.createEndstoneBoots();
        }
        if (customId.startsWith("ice_")) {
            if ("ice_helmet".equals(customId)) return BlockArmor.createIceHelmet();
            if ("ice_chestplate".equals(customId)) return BlockArmor.createIceChestplate();
            if ("ice_leggings".equals(customId)) return BlockArmor.createIceLeggings();
            if ("ice_boots".equals(customId)) return BlockArmor.createIceBoots();
        }
        if (customId.startsWith("prismarine_")) {
            if ("prismarine_helmet".equals(customId)) return BlockArmor.createPrismarineHelmet();
            if ("prismarine_chestplate".equals(customId)) return BlockArmor.createPrismarineChestplate();
            if ("prismarine_leggings".equals(customId)) return BlockArmor.createPrismarineLeggings();
            if ("prismarine_boots".equals(customId)) return BlockArmor.createPrismarineBoots();
        }
        if (customId.startsWith("terracotta_")) {
            if ("terracotta_helmet".equals(customId)) return BlockArmor.createTerracottaHelmet();
            if ("terracotta_chestplate".equals(customId)) return BlockArmor.createTerracottaChestplate();
            if ("terracotta_leggings".equals(customId)) return BlockArmor.createTerracottaLeggings();
            if ("terracotta_boots".equals(customId)) return BlockArmor.createTerracottaBoots();
        }
        if (customId.startsWith("mossy_")) {
            if ("mossy_helmet".equals(customId)) return BlockArmor.createMossyHelmet();
            if ("mossy_chestplate".equals(customId)) return BlockArmor.createMossyChestplate();
            if ("mossy_leggings".equals(customId)) return BlockArmor.createMossyLeggings();
            if ("mossy_boots".equals(customId)) return BlockArmor.createMossyBoots();
        }
        if (customId.startsWith("soul_sand_")) {
            if ("soul_sand_helmet".equals(customId)) return BlockArmor.createSoulSandHelmet();
            if ("soul_sand_chestplate".equals(customId)) return BlockArmor.createSoulSandChestplate();
            if ("soul_sand_leggings".equals(customId)) return BlockArmor.createSoulSandLeggings();
            if ("soul_sand_boots".equals(customId)) return BlockArmor.createSoulSandBoots();
        }
        if (customId.startsWith("sponge_")) {
            if ("sponge_helmet".equals(customId)) return BlockArmor.createSpongeHelmet();
            if ("sponge_chestplate".equals(customId)) return BlockArmor.createSpongeChestplate();
            if ("sponge_leggings".equals(customId)) return BlockArmor.createSpongeLeggings();
            if ("sponge_boots".equals(customId)) return BlockArmor.createSpongeBoots();
        }
        if (customId.startsWith("snow_")) {
            if ("snow_helmet".equals(customId)) return BlockArmor.createSnowHelmet();
            if ("snow_chestplate".equals(customId)) return BlockArmor.createSnowChestplate();
            if ("snow_leggings".equals(customId)) return BlockArmor.createSnowLeggings();
            if ("snow_boots".equals(customId)) return BlockArmor.createSnowBoots();
        }
        if (customId.startsWith("soul_soil_")) {
            if ("soul_soil_helmet".equals(customId)) return BlockArmor.createSoulSoilHelmet();
            if ("soul_soil_chestplate".equals(customId)) return BlockArmor.createSoulSoilChestplate();
            if ("soul_soil_leggings".equals(customId)) return BlockArmor.createSoulSoilLeggings();
            if ("soul_soil_boots".equals(customId)) return BlockArmor.createSoulSoilBoots();
        }
        if (customId.startsWith("sculk_")) {
            if ("sculk_helmet".equals(customId)) return BlockArmor.createSculkHelmet();
            if ("sculk_chestplate".equals(customId)) return BlockArmor.createSculkChestplate();
            if ("sculk_leggings".equals(customId)) return BlockArmor.createSculkLeggings();
            if ("sculk_boots".equals(customId)) return BlockArmor.createSculkBoots();
        }
        if (customId.startsWith("purpur_")) {
            if ("purpur_helmet".equals(customId)) return BlockArmor.createPurpurHelmet();
            if ("purpur_chestplate".equals(customId)) return BlockArmor.createPurpurChestplate();
            if ("purpur_leggings".equals(customId)) return BlockArmor.createPurpurLeggings();
            if ("purpur_boots".equals(customId)) return BlockArmor.createPurpurBoots();
        }
        if (customId.startsWith("smooth_stone_")) {
            if ("smooth_stone_helmet".equals(customId)) return BlockArmor.createSmoothStoneHelmet();
            if ("smooth_stone_chestplate".equals(customId)) return BlockArmor.createSmoothStoneChestplate();
            if ("smooth_stone_leggings".equals(customId)) return BlockArmor.createSmoothStoneLeggings();
            if ("smooth_stone_boots".equals(customId)) return BlockArmor.createSmoothStoneBoots();
        }
        if (customId.startsWith("crying_obsidian_")) {
            if ("crying_obsidian_helmet".equals(customId)) return BlockArmor.createCryingObsidianHelmet();
            if ("crying_obsidian_chestplate".equals(customId)) return BlockArmor.createCryingObsidianChestplate();
            if ("crying_obsidian_leggings".equals(customId)) return BlockArmor.createCryingObsidianLeggings();
            if ("crying_obsidian_boots".equals(customId)) return BlockArmor.createCryingObsidianBoots();
        }
        if (customId.startsWith("obsidian_block_")) {
            if ("obsidian_block_helmet".equals(customId)) return BlockArmor.createObsidianBlockHelmet();
            if ("obsidian_block_chestplate".equals(customId)) return BlockArmor.createObsidianBlockChestplate();
            if ("obsidian_block_leggings".equals(customId)) return BlockArmor.createObsidianBlockLeggings();
            if ("obsidian_block_boots".equals(customId)) return BlockArmor.createObsidianBlockBoots();
        }
        
        // Block Weapons - Only materials that exist in BOTH BlockArmor AND BlockWeapons (146 materials)
        if ("glass_sword".equals(customId)) return BlockWeapons.createGlassSword();
        if ("obsidian_sword".equals(customId)) return BlockWeapons.createObsidianSword();
        if ("quartz_sword".equals(customId)) return BlockWeapons.createQuartzSword();
        if ("glowstone_sword".equals(customId)) return BlockWeapons.createGlowstoneSword();
        if ("redstone_sword".equals(customId)) return BlockWeapons.createRedstoneSword();
        if ("netherrack_sword".equals(customId)) return BlockWeapons.createNetherrackSword();
        if ("endstone_sword".equals(customId)) return BlockWeapons.createEndstoneSword();
        if ("ice_sword".equals(customId)) return BlockWeapons.createIceSword();
        if ("prismarine_sword".equals(customId)) return BlockWeapons.createPrismarineSword();
        if ("terracotta_sword".equals(customId)) return BlockWeapons.createTerracottaSword();
        if ("mossy_sword".equals(customId)) return BlockWeapons.createMossySword();
        if ("soul_sand_sword".equals(customId)) return BlockWeapons.createSoulSandSword();
        if ("magma_sword".equals(customId)) return BlockWeapons.createMagmaSword();
        if ("sandstone_sword".equals(customId)) return BlockWeapons.createSandstoneSword();
        if ("amethyst_sword".equals(customId)) return BlockWeapons.createAmethystSword();
        if ("coal_sword".equals(customId)) return BlockWeapons.createCoalSword();
        if ("diamond_block_sword".equals(customId)) return BlockWeapons.createDiamondBlockSword();
        if ("emerald_block_sword".equals(customId)) return BlockWeapons.createEmeraldBlockSword();
        if ("gold_block_sword".equals(customId)) return BlockWeapons.createGoldBlockSword();
        if ("iron_block_sword".equals(customId)) return BlockWeapons.createIronBlockSword();
        if ("lapis_block_sword".equals(customId)) return BlockWeapons.createLapisBlockSword();
        if ("copper_block_sword".equals(customId)) return BlockWeapons.createCopperBlockSword();
        if ("ancient_debris_sword".equals(customId)) return BlockWeapons.createAncientDebrisSword();
        if ("basalt_sword".equals(customId)) return BlockWeapons.createBasaltSword();
        if ("blackstone_sword".equals(customId)) return BlockWeapons.createBlackstoneSword();
        if ("bone_block_sword".equals(customId)) return BlockWeapons.createBoneBlockSword();
        if ("brick_sword".equals(customId)) return BlockWeapons.createBrickSword();
        if ("cactus_sword".equals(customId)) return BlockWeapons.createCactusSword();
        if ("calcite_sword".equals(customId)) return BlockWeapons.createCalciteSword();
        if ("deepslate_sword".equals(customId)) return BlockWeapons.createDeepslateSword();
        if ("dripstone_sword".equals(customId)) return BlockWeapons.createDripstoneSword();
        if ("hay_sword".equals(customId)) return BlockWeapons.createHaySword();
        if ("honeycomb_sword".equals(customId)) return BlockWeapons.createHoneycombSword();
        if ("lily_pad_sword".equals(customId)) return BlockWeapons.createLilyPadSword();
        if ("melon_sword".equals(customId)) return BlockWeapons.createMelonSword();
        if ("moss_block_sword".equals(customId)) return BlockWeapons.createMossBlockSword();
        if ("mycelium_sword".equals(customId)) return BlockWeapons.createMyceliumSword();
        if ("nether_brick_sword".equals(customId)) return BlockWeapons.createNetherBrickSword();
        if ("pumpkin_sword".equals(customId)) return BlockWeapons.createPumpkinSword();
        if ("purpur_sword".equals(customId)) return BlockWeapons.createPurpurSword();
        if ("sand_sword".equals(customId)) return BlockWeapons.createSandSword();
        if ("sculk_sword".equals(customId)) return BlockWeapons.createSculkSword();
        if ("shroomlight_sword".equals(customId)) return BlockWeapons.createShroomlightSword();
        if ("slime_sword".equals(customId)) return BlockWeapons.createSlimeSword();
        if ("smooth_stone_sword".equals(customId)) return BlockWeapons.createSmoothStoneSword();
        if ("snow_sword".equals(customId)) return BlockWeapons.createSnowSword();
        if ("soul_soil_sword".equals(customId)) return BlockWeapons.createSoulSoilSword();
        if ("sponge_sword".equals(customId)) return BlockWeapons.createSpongeSword();
        if ("target_sword".equals(customId)) return BlockWeapons.createTargetSword();
        if ("tnt_sword".equals(customId)) return BlockWeapons.createTntSword();
        if ("warped_sword".equals(customId)) return BlockWeapons.createWarpedSword();
        if ("wet_sponge_sword".equals(customId)) return BlockWeapons.createWetSpongeSword();
        if ("crimson_stem_sword".equals(customId)) return BlockWeapons.createCrimsonStemSword();
        if ("crying_obsidian_sword".equals(customId)) return BlockWeapons.createCryingObsidianSword();
        if ("gilded_blackstone_sword".equals(customId)) return BlockWeapons.createGildedBlackstoneSword();
        if ("granite_sword".equals(customId)) return BlockWeapons.createGraniteSword();
        if ("diorite_sword".equals(customId)) return BlockWeapons.createDioriteSword();
        if ("andesite_sword".equals(customId)) return BlockWeapons.createAndesiteSword();
        if ("polished_granite_sword".equals(customId)) return BlockWeapons.createPolishedGraniteSword();
        if ("polished_diorite_sword".equals(customId)) return BlockWeapons.createPolishedDioriteSword();
        if ("polished_andesite_sword".equals(customId)) return BlockWeapons.createPolishedAndesiteSword();
        if ("packed_ice_sword".equals(customId)) return BlockWeapons.createPackedIceSword();
        if ("blue_ice_sword".equals(customId)) return BlockWeapons.createBlueIceSword();
        if ("nether_gold_ore_sword".equals(customId)) return BlockWeapons.createNetherGoldOreSword();
        if ("dark_oak_log_sword".equals(customId)) return BlockWeapons.createDarkOakLogSword();
        if ("jungle_log_sword".equals(customId)) return BlockWeapons.createJungleLogSword();
        if ("acacia_log_sword".equals(customId)) return BlockWeapons.createAcaciaLogSword();
        if ("mangrove_log_sword".equals(customId)) return BlockWeapons.createMangroveLogSword();
        if ("cherry_log_sword".equals(customId)) return BlockWeapons.createCherryLogSword();
        if ("bamboo_block_sword".equals(customId)) return BlockWeapons.createBambooBlockSword();
        if ("tuff_sword".equals(customId)) return BlockWeapons.createTuffSword();
        if ("polished_tuff_sword".equals(customId)) return BlockWeapons.createPolishedTuffSword();
        if ("nether_wart_block_sword".equals(customId)) return BlockWeapons.createNetherWartBlockSword();
        if ("warped_wart_block_sword".equals(customId)) return BlockWeapons.createWarpedWartBlockSword();
        if ("chiseled_deepslate_sword".equals(customId)) return BlockWeapons.createChiseledDeepslateSword();
        if ("reinforced_deepslate_sword".equals(customId)) return BlockWeapons.createReinforcedDeepslateSword();
        if ("chiseled_nether_brick_sword".equals(customId)) return BlockWeapons.createChiseledNetherBrickSword();
        if ("cracked_nether_brick_sword".equals(customId)) return BlockWeapons.createCrackedNetherBrickSword();
        if ("chiseled_stone_brick_sword".equals(customId)) return BlockWeapons.createChiseledStoneBrickSword();
        if ("cracked_stone_brick_sword".equals(customId)) return BlockWeapons.createCrackedStoneBrickSword();
        if ("end_stone_brick_sword".equals(customId)) return BlockWeapons.createEndStoneBrickSword();
        if ("red_sandstone_sword".equals(customId)) return BlockWeapons.createRedSandstoneSword();
        if ("raw_iron_block_sword".equals(customId)) return BlockWeapons.createRawIronBlockSword();
        if ("raw_gold_block_sword".equals(customId)) return BlockWeapons.createRawGoldBlockSword();
        if ("prismarine_bricks_sword".equals(customId)) return BlockWeapons.createPrismarineBricksSword();
        if ("dark_prismarine_sword".equals(customId)) return BlockWeapons.createDarkPrismarineSword();
        if ("sea_lantern_sword".equals(customId)) return BlockWeapons.createSeaLanternSword();
        if ("lodestone_sword".equals(customId)) return BlockWeapons.createLodestoneSword();
        if ("blackstone_bricks_sword".equals(customId)) return BlockWeapons.createBlackstoneBricksSword();
        if ("polished_blackstone_sword".equals(customId)) return BlockWeapons.createPolishedBlackstoneSword();
        if ("smooth_basalt_sword".equals(customId)) return BlockWeapons.createSmoothBasaltSword();
        if ("amethyst_cluster_sword".equals(customId)) return BlockWeapons.createAmethystClusterSword();
        if ("obsidian_block_sword".equals(customId)) return BlockWeapons.createObsidianBlockSword();
        if ("stripped_spruce_log_sword".equals(customId)) return BlockWeapons.createStrippedSpruceLogSword();
        if ("stripped_birch_log_sword".equals(customId)) return BlockWeapons.createStrippedBirchLogSword();
        if ("stripped_dark_oak_log_sword".equals(customId)) return BlockWeapons.createStrippedDarkOakLogSword();
        if ("stripped_jungle_log_sword".equals(customId)) return BlockWeapons.createStrippedJungleLogSword();
        if ("stripped_acacia_log_sword".equals(customId)) return BlockWeapons.createStrippedAcaciaLogSword();
        if ("stripped_mangrove_log_sword".equals(customId)) return BlockWeapons.createStrippedMangroveLogSword();
        if ("stripped_cherry_log_sword".equals(customId)) return BlockWeapons.createStrippedCherryLogSword();
        if ("stripped_bamboo_block_sword".equals(customId)) return BlockWeapons.createStrippedBambooBlockSword();
        if ("stripped_crimson_stem_sword".equals(customId)) return BlockWeapons.createStrippedCrimsonStemSword();
        if ("stripped_warped_stem_sword".equals(customId)) return BlockWeapons.createStrippedWarpedStemSword();
        if ("spruce_planks_sword".equals(customId)) return BlockWeapons.createSprucePlanksSword();
        if ("birch_planks_sword".equals(customId)) return BlockWeapons.createBirchPlanksSword();
        if ("jungle_planks_sword".equals(customId)) return BlockWeapons.createJunglePlanksSword();
        if ("acacia_planks_sword".equals(customId)) return BlockWeapons.createAcaciaPlanksSword();
        if ("dark_oak_planks_sword".equals(customId)) return BlockWeapons.createDarkOakPlanksSword();
        if ("mangrove_planks_sword".equals(customId)) return BlockWeapons.createMangrovePlanksSword();
        if ("cherry_planks_sword".equals(customId)) return BlockWeapons.createCherryPlanksSword();
        if ("bamboo_planks_sword".equals(customId)) return BlockWeapons.createBambooPlanksSword();
        if ("crimson_planks_sword".equals(customId)) return BlockWeapons.createCrimsonPlanksSword();
        if ("warped_planks_sword".equals(customId)) return BlockWeapons.createWarpedPlanksSword();
        if ("oak_planks_sword".equals(customId)) return BlockWeapons.createOakPlanksSword();
        if ("oak_log_sword".equals(customId)) return BlockWeapons.createOakLogSword();
        if ("spruce_log_sword".equals(customId)) return BlockWeapons.createSpruceLogSword();
        if ("birch_log_sword".equals(customId)) return BlockWeapons.createBirchLogSword();
        if ("stone_bricks_sword".equals(customId)) return BlockWeapons.createStoneBricksSword();
        if ("cobblestone_sword".equals(customId)) return BlockWeapons.createCobblestoneSword();
        if ("mossy_cobblestone_sword".equals(customId)) return BlockWeapons.createMossyCobblestoneSword();
        if ("cobbled_deepslate_sword".equals(customId)) return BlockWeapons.createCobbledDeepslateSword();
        if ("mud_bricks_sword".equals(customId)) return BlockWeapons.createMudBricksSword();
        if ("mangrove_roots_sword".equals(customId)) return BlockWeapons.createMangroveRootsSword();
        if ("muddy_mangrove_roots_sword".equals(customId)) return BlockWeapons.createMuddyMangroveRootsSword();
        if ("netherite_block_sword".equals(customId)) return BlockWeapons.createNetheriteBlockSword();
        if ("chiseled_copper_sword".equals(customId)) return BlockWeapons.createChiseledCopperSword();
        if ("cut_copper_sword".equals(customId)) return BlockWeapons.createCutCopperSword();
        if ("exposed_copper_sword".equals(customId)) return BlockWeapons.createExposedCopperSword();
        if ("weathered_copper_sword".equals(customId)) return BlockWeapons.createWeatheredCopperSword();
        if ("oxidised_copper_sword".equals(customId)) return BlockWeapons.createOxidisedCopperSword();
        if ("waxed_cut_copper_sword".equals(customId)) return BlockWeapons.createWaxedCutCopperSword();
        if ("polished_basalt_sword".equals(customId)) return BlockWeapons.createPolishedBasaltSword();
        if ("verdant_froglight_sword".equals(customId)) return BlockWeapons.createVerdantFroglightSword();
        if ("pearlescent_froglight_sword".equals(customId)) return BlockWeapons.createPearlescentFroglightSword();
        if ("ochre_froglight_sword".equals(customId)) return BlockWeapons.createOchreFroglightSword();
        if ("iron_nugget_sword".equals(customId)) return BlockWeapons.createIronNuggetSword();
        if ("gold_nugget_sword".equals(customId)) return BlockWeapons.createGoldNuggetSword();
        if ("copper_ingot_sword".equals(customId)) return BlockWeapons.createCopperIngotSword();
        if ("emerald_sword".equals(customId)) return BlockWeapons.createEmeraldSword();
        if ("lapis_lazuli_sword".equals(customId)) return BlockWeapons.createLapisLazuliSword();
        if ("amethyst_shard_sword".equals(customId)) return BlockWeapons.createAmethystShardSword();
        if ("flint_sword".equals(customId)) return BlockWeapons.createFlintSword();
        if ("bone_meal_sword".equals(customId)) return BlockWeapons.createBoneMealSword();
        if ("charcoal_sword".equals(customId)) return BlockWeapons.createCharcoalSword();
        if ("end_stone_sword".equals(customId)) return BlockWeapons.createEndStoneSword();
        if ("snow_block_sword".equals(customId)) return BlockWeapons.createSnowBlockSword();
        
        // Gem Armor & Weapons
        if ("emerald_helmet".equals(customId)) return GemGear.createEmeraldHelmet();
        if ("emerald_chestplate".equals(customId)) return GemGear.createEmeraldChestplate();
        if ("emerald_leggings".equals(customId)) return GemGear.createEmeraldLeggings();
        if ("emerald_boots".equals(customId)) return GemGear.createEmeraldBoots();
        if ("emerald_sword".equals(customId)) return GemGear.createEmeraldSword();
        if ("emerald_pickaxe".equals(customId)) return GemGear.createEmeraldPickaxe();
        if ("emerald_axe".equals(customId)) return GemGear.createEmeraldAxe();
        if ("emerald_shovel".equals(customId)) return GemGear.createEmeraldShovel();
        if ("emerald_hoe".equals(customId)) return GemGear.createEmeraldHoe();
        if ("lapis_helmet".equals(customId)) return GemGear.createLapisHelmet();
        if ("lapis_chestplate".equals(customId)) return GemGear.createLapisChestplate();
        if ("lapis_leggings".equals(customId)) return GemGear.createLapisLeggings();
        if ("lapis_boots".equals(customId)) return GemGear.createLapisBoots();
        if ("lapis_sword".equals(customId)) return GemGear.createLapisSword();
        if ("lapis_pickaxe".equals(customId)) return GemGear.createLapisPickaxe();
        if ("lapis_axe".equals(customId)) return GemGear.createLapisAxe();
        if ("lapis_shovel".equals(customId)) return GemGear.createLapisShovel();
        if ("lapis_hoe".equals(customId)) return GemGear.createLapisHoe();
        
        // Slayer Armor T1 & T2
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            String typeLower = type.name().toLowerCase();
            if (customId.equals(typeLower + "_t1_helmet")) return SlayerItems.createSlayerHelmet(type, 1);
            if (customId.equals(typeLower + "_t1_chestplate")) return SlayerItems.createSlayerChestplate(type, 1);
            if (customId.equals(typeLower + "_t1_leggings")) return SlayerItems.createSlayerLeggings(type, 1);
            if (customId.equals(typeLower + "_t1_boots")) return SlayerItems.createSlayerBoots(type, 1);
            if (customId.equals(typeLower + "_t2_helmet")) return SlayerItems.createSlayerHelmet(type, 2);
            if (customId.equals(typeLower + "_t2_chestplate")) return SlayerItems.createSlayerChestplate(type, 2);
            if (customId.equals(typeLower + "_t2_leggings")) return SlayerItems.createSlayerLeggings(type, 2);
            if (customId.equals(typeLower + "_t2_boots")) return SlayerItems.createSlayerBoots(type, 2);
            if (customId.equals(typeLower + "_sword")) return SlayerItems.createSlayerSword(type);
            if (customId.equals(typeLower + "_sword_t2")) return SlayerItems.createUpgradedSlayerSword(type);
        }
        
        // Special Armor
        if ("zombie_berserker_helmet".equals(customId)) return SlayerItems.createZombieBerserkerHelmet();
        if ("spider_leggings".equals(customId)) return SlayerItems.createSpiderLeggings();
        if ("skeleton_bow".equals(customId)) return SlayerItems.createSkeletonBow();
        if ("slime_boots".equals(customId)) return SlayerItems.createSlimeBoots();
        if ("warden_chestplate".equals(customId)) return SlayerItems.createWardenChestplate();
        if ("voidwalker_crown".equals(customId)) return SlayerItems.createVoidwalkerCrown();
        
        // Special Items
        if ("the_gavel".equals(customId)) return CustomItemHandler.createTheGavel();
        if ("harveys_stick".equals(customId)) return CustomItemHandler.createHarveysStick();
        if ("hermes_shoes".equals(customId)) return CustomItemHandler.createHermesShoes();
        if ("daniels_pickaxe".equals(customId)) return DanielsPickaxe.create();
        
        // NEW BOSS DROPS
        if ("undead_heart".equals(customId)) return SlayerItems.createUndeadHeart();
        if ("spectral_quiver".equals(customId)) return SlayerItems.createSpectralQuiver();
        if ("echoing_core".equals(customId)) return SlayerItems.createEchoingCore();
        if ("ender_sword".equals(customId)) return SlayerItems.createEnderSword();
        if ("abyssal_blade".equals(customId)) return SlayerItems.createAbyssalBlade();
        if ("bouncy_slime".equals(customId)) return SlayerItems.createBouncySlime();
        if ("venomous_dagger".equals(customId)) return SlayerItems.createVenomousDagger();
        
        // Dragon Chestplates (No Recipe - but add mapping for completeness)
        if ("dragon_chestplate_1".equals(customId)) return DragonChestplates.createDragonChestplate1();
        if ("dragon_chestplate_2".equals(customId)) return DragonChestplates.createDragonChestplate2();
        if ("dragon_chestplate_3".equals(customId)) return DragonChestplates.createDragonChestplate3();
        
        // HPEBM Weapons
        for (int mk = 1; mk <= 5; mk++) {
            if (customId.equals("hpbeam_mk" + mk)) return CustomItemHandler.createHPEBM(mk);
        }
        if ("ultra_overclocked_beam".equals(customId)) return CustomItemHandler.createUltraOverclockedBeam();
        
        // Legendary Tools (15 sets)
        if ("dragon_pickaxe".equals(customId)) return LegendaryTools.createDragonPickaxe();
        if ("dragon_axe".equals(customId)) return LegendaryTools.createDragonAxe();
        if ("dragon_shovel".equals(customId)) return LegendaryTools.createDragonShovel();
        if ("dragon_hoe".equals(customId)) return LegendaryTools.createDragonHoe();
        if ("aether_pickaxe".equals(customId)) return LegendaryTools.createAetherPickaxe();
        if ("aether_axe".equals(customId)) return LegendaryTools.createAetherAxe();
        if ("aether_shovel".equals(customId)) return LegendaryTools.createAetherShovel();
        if ("aether_hoe".equals(customId)) return LegendaryTools.createAetherHoe();
        if ("void_pickaxe".equals(customId)) return LegendaryTools.createVoidPickaxe();
        if ("void_axe".equals(customId)) return LegendaryTools.createVoidAxe();
        if ("void_shovel".equals(customId)) return LegendaryTools.createVoidShovel();
        if ("void_hoe".equals(customId)) return LegendaryTools.createVoidHoe();
        if ("nature_pickaxe".equals(customId)) return LegendaryTools.createNaturePickaxe();
        if ("nature_axe".equals(customId)) return LegendaryTools.createNatureAxe();
        if ("nature_shovel".equals(customId)) return LegendaryTools.createNatureShovel();
        if ("nature_hoe".equals(customId)) return LegendaryTools.createNatureHoe();
        if ("frost_pickaxe".equals(customId)) return LegendaryTools.createFrostPickaxe();
        if ("frost_axe".equals(customId)) return LegendaryTools.createFrostAxe();
        if ("frost_shovel".equals(customId)) return LegendaryTools.createFrostShovel();
        if ("frost_hoe".equals(customId)) return LegendaryTools.createFrostHoe();
        if ("thunder_pickaxe".equals(customId)) return LegendaryTools.createThunderPickaxe();
        if ("thunder_axe".equals(customId)) return LegendaryTools.createThunderAxe();
        if ("thunder_shovel".equals(customId)) return LegendaryTools.createThunderShovel();
        if ("thunder_hoe".equals(customId)) return LegendaryTools.createThunderHoe();
        if ("ocean_pickaxe".equals(customId)) return LegendaryTools.createOceanPickaxe();
        if ("ocean_axe".equals(customId)) return LegendaryTools.createOceanAxe();
        if ("ocean_shovel".equals(customId)) return LegendaryTools.createOceanShovel();
        if ("ocean_hoe".equals(customId)) return LegendaryTools.createOceanHoe();
        if ("lunar_pickaxe".equals(customId)) return LegendaryTools.createLunarPickaxe();
        if ("lunar_axe".equals(customId)) return LegendaryTools.createLunarAxe();
        if ("lunar_shovel".equals(customId)) return LegendaryTools.createLunarShovel();
        if ("lunar_hoe".equals(customId)) return LegendaryTools.createLunarHoe();
        if ("solar_pickaxe".equals(customId)) return LegendaryTools.createSolarPickaxe();
        if ("solar_axe".equals(customId)) return LegendaryTools.createSolarAxe();
        if ("solar_shovel".equals(customId)) return LegendaryTools.createSolarShovel();
        if ("solar_hoe".equals(customId)) return LegendaryTools.createSolarHoe();
        if ("terra_pickaxe".equals(customId)) return LegendaryTools.createTerraPickaxe();
        if ("terra_axe".equals(customId)) return LegendaryTools.createTerraAxe();
        if ("terra_shovel".equals(customId)) return LegendaryTools.createTerraShovel();
        if ("terra_hoe".equals(customId)) return LegendaryTools.createTerraHoe();
        if ("phantom_pickaxe".equals(customId)) return LegendaryTools.createPhantomPickaxe();
        if ("phantom_axe".equals(customId)) return LegendaryTools.createPhantomAxe();
        if ("phantom_shovel".equals(customId)) return LegendaryTools.createPhantomShovel();
        if ("phantom_hoe".equals(customId)) return LegendaryTools.createPhantomHoe();
        if ("blood_pickaxe".equals(customId)) return LegendaryTools.createBloodPickaxe();
        if ("blood_axe".equals(customId)) return LegendaryTools.createBloodAxe();
        if ("blood_shovel".equals(customId)) return LegendaryTools.createBloodShovel();
        if ("blood_hoe".equals(customId)) return LegendaryTools.createBloodHoe();
        if ("celestial_pickaxe".equals(customId)) return LegendaryTools.createCelestialPickaxe();
        if ("celestial_axe".equals(customId)) return LegendaryTools.createCelestialAxe();
        if ("celestial_shovel".equals(customId)) return LegendaryTools.createCelestialShovel();
        if ("celestial_hoe".equals(customId)) return LegendaryTools.createCelestialHoe();
        if ("shadow_pickaxe".equals(customId)) return LegendaryTools.createShadowPickaxe();
        if ("shadow_axe".equals(customId)) return LegendaryTools.createShadowAxe();
        if ("shadow_shovel".equals(customId)) return LegendaryTools.createShadowShovel();
        if ("shadow_hoe".equals(customId)) return LegendaryTools.createShadowHoe();
        if ("crystal_pickaxe".equals(customId)) return LegendaryTools.createCrystalPickaxe();
        if ("crystal_axe".equals(customId)) return LegendaryTools.createCrystalAxe();
        if ("crystal_shovel".equals(customId)) return LegendaryTools.createCrystalShovel();
        if ("crystal_hoe".equals(customId)) return LegendaryTools.createCrystalHoe();
        
        // Backrooms/Fracture 3 items
        // Backrooms rune items — try direct registry lookup for any politicalserver: item
        try {
            Identifier itemId = Identifier.of(customId);
            Item item = Registries.ITEM.get(itemId);
            if (item != Items.AIR) return new ItemStack(item);
        } catch (net.minecraft.util.InvalidIdentifierException ignored) {
            // malformed identifier — ignore silently
        } catch (Exception e) {
            System.err.println("[RecipeConfigManager] Unexpected error resolving item '" + customId + "': " + e.getMessage());
        }
        return ItemStack.EMPTY;
    }
}
