package com.political;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.TagParser;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.fabricmc.loader.api.FabricLoader;
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
    private static final Path REPO_CONFIG_DIR = FabricLoader.getInstance().getGameDir().getParent().resolve("config").resolve("political");
    private static final Path RUN_CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve("political");
    private static final Path SERVER_CONFIG_DIR = FabricLoader.getInstance().getGameDir().resolve("config").resolve("political");

    private static final Path CONFIG_DIR = FabricLoader.getInstance().isDevelopmentEnvironment() ? REPO_CONFIG_DIR : SERVER_CONFIG_DIR;
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("custom_recipes.json");
    // Modular recipe file path (unused but kept for potential future use)
    private static final java.nio.file.Path MODULAR_FILE = CONFIG_DIR.resolve("modular_recipes.json");
    private static final Path DISABLED_FILE = CONFIG_DIR.resolve("disabled_recipes.json");
    private static final Path RECIPE_JSON_MIRROR = FabricLoader.getInstance().isDevelopmentEnvironment()
            ? RUN_CONFIG_DIR.resolve("custom_recipes.json")
            : null;

    static {
        boolean fileExists = java.nio.file.Files.exists(CONFIG_FILE);
        System.out.println("[RecipeConfigManager] JSON: " + (fileExists ? "OK" : "MISSING") + " | Path: " + CONFIG_FILE.toAbsolutePath());
    }

    /**
     * Sorts the recipe array by name before writing.
     *
     * <p>Recipes are gathered from maps whose iteration order is not stable between runs, so an
     * export that simply wrote them out produced a different byte sequence every boot even when
     * nothing had changed. On a file this size that is tens of thousands of lines of pure noise in
     * any diff, which buries the changes that actually matter.</p>
     */
    private static void sortRecipesByName(JsonObject root) {
        try {
            if (root == null || !root.has("recipes") || !root.get("recipes").isJsonArray()) return;
            JsonArray arr = root.getAsJsonArray("recipes");
            java.util.List<com.google.gson.JsonElement> items = new java.util.ArrayList<>();
            arr.forEach(items::add);
            items.sort(java.util.Comparator.comparing(e -> {
                try {
                    return e.getAsJsonObject().get("name").getAsString();
                } catch (Exception ex) {
                    return "";
                }
            }));
            JsonArray sorted = new JsonArray();
            items.forEach(sorted::add);
            root.add("recipes", sorted);
        } catch (Exception ignored) {
            // A malformed root is still worth writing unsorted rather than not at all.
        }
    }

    private static void writeRecipeJsonRoot(JsonObject root) throws IOException {
        sortRecipesByName(root);
        Files.createDirectories(CONFIG_FILE.getParent());
        try (Writer writer = Files.newBufferedWriter(CONFIG_FILE)) {
            GSON.toJson(root, writer);
        }
        // Mirror to the "other" location so dev + dedicated server stay consistent.
        if (RECIPE_JSON_MIRROR != null) {
            Files.createDirectories(RECIPE_JSON_MIRROR.getParent());
            try (Writer writer = Files.newBufferedWriter(RECIPE_JSON_MIRROR)) {
                GSON.toJson(root, writer);
            }
        }
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

    /** Names of recipes that failed to resolve on the most recent load (diagnostic). */
    private static volatile List<String> LAST_FAILED_RECIPE_NAMES = new ArrayList<>();

    /** Recipes that could not be resolved on the last load — use to diagnose "N failed". */
    public static List<String> getLastFailedRecipeNames() {
        return new ArrayList<>(LAST_FAILED_RECIPE_NAMES);
    }

    // ── Lifecycle ──────────────────────────────────────────────

    /** Load persisted recipes from disk. Call on server start. */
    public static void load() {
        // JSON-only source of truth: never auto-regenerate on startup.
        ensureRecipeFileExists();
        logRecipeJsonSummary();
        invalidateRecipeCache();
        SlayerRecipes.RECIPES.clear();
        loadDisabled();
        SlayerRecipes.registerRecipes();
    }

    private static void ensureRecipeFileExists() {
        try {
            if (!Files.exists(CONFIG_FILE)) {
                JsonObject root = new JsonObject();
                root.add("recipes", new JsonArray());
                writeRecipeJsonRoot(root);
                System.out.println("[RecipeConfigManager] Created empty custom_recipes.json at " + CONFIG_FILE.toAbsolutePath());
                return;
            }

            String jsonString = Files.readString(CONFIG_FILE);
            if (jsonString == null || jsonString.isBlank()) {
                JsonObject root = new JsonObject();
                root.add("recipes", new JsonArray());
                writeRecipeJsonRoot(root);
                System.out.println("[RecipeConfigManager] custom_recipes.json was blank; initialized empty recipes at " + CONFIG_FILE.toAbsolutePath());
                return;
            }

            JsonObject root = GSON.fromJson(jsonString, JsonObject.class);
            if (root == null || !root.has("recipes") || !root.get("recipes").isJsonArray()) {
                // Never overwrite a non-trivial file — that was wiping large recipe lists on parse quirks.
                long bytes = Files.size(CONFIG_FILE);
                System.err.println("[RecipeConfigManager] custom_recipes.json missing valid \"recipes\" array — leaving file unchanged (size=" + bytes + " bytes). Path: " + CONFIG_FILE.toAbsolutePath());
            }
        } catch (Exception e) {
            System.err.println("[RecipeConfigManager] Failed to read custom_recipes.json: " + e.getMessage() + " | Path: " + CONFIG_FILE.toAbsolutePath());
        }
    }

    private static void logRecipeJsonSummary() {
        try {
            Path abs = CONFIG_FILE.toAbsolutePath();
            long bytes = Files.exists(CONFIG_FILE) ? Files.size(CONFIG_FILE) : 0;
            String jsonString = Files.readString(CONFIG_FILE);
            JsonObject root = GSON.fromJson(jsonString, JsonObject.class);
            int count = 0;
            if (root != null && root.has("recipes") && root.get("recipes").isJsonArray()) {
                count = root.getAsJsonArray("recipes").size();
            }
            System.out.println("[RecipeConfigManager] JSON recipes in file: " + count + " | size=" + bytes + " bytes | " + abs);
        } catch (Exception e) {
            System.err.println("[RecipeConfigManager] Could not summarize JSON recipes: " + e.getMessage() + " | Path: " + CONFIG_FILE.toAbsolutePath());
        }
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
                // Names of recipes that would not resolve, so a failure count is actionable
                // instead of mysterious. Exposed via getLastFailedRecipeNames() / /debugrecipes.
                List<String> failedNames = new ArrayList<>();

                String jsonString = Files.readString(CONFIG_FILE);
                JsonObject root = GSON.fromJson(jsonString, JsonObject.class);
                if (root != null && root.has("recipes")) {
                    JsonArray arr = root.getAsJsonArray("recipes");
                    for (var elem : arr) {
                        String name = "(unnamed)";
                        try {
                            JsonObject obj = elem.getAsJsonObject();
                            if (obj.has("name")) name = obj.get("name").getAsString();
                            SlayerRecipes.Recipe recipe = deserialize(obj);
                            if (recipe != null && recipe.result != null && !recipe.result.isEmpty()) {
                                recipe = RecipeIngredientHydrator.hydrate(recipe);
                                recipes.add(recipe);
                            } else {
                                failed++;
                                failedNames.add(name);
                            }
                        } catch (Exception e) {
                            failed++;
                            failedNames.add(name + " (" + e.getClass().getSimpleName() + ")");
                        }
                    }
                }
                LAST_FAILED_RECIPE_NAMES = failedNames;

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
                            if (failed > 0) {
                                System.out.println("[RecipeConfigManager] failed recipes: " + failedNames);
                            }
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

    /** Adds or updates a custom recipe override directly to JSON file (logs + registers immediately). */
    public static void addCustomRecipe(SlayerRecipes.Recipe recipe) {
        addCustomRecipeInternal(recipe, false, true);
    }

    /** Silent variant for server bootstrap (no log spam, no immediate register). */
    public static void addCustomRecipeSilent(SlayerRecipes.Recipe recipe) {
        addCustomRecipeInternal(recipe, true, false);
    }

    /** Bulk upsert for server bootstrap: writes JSON once, no log spam, does not auto-register. */
    public static void upsertCustomRecipesSilent(List<SlayerRecipes.Recipe> recipes) {
        if (recipes == null || recipes.isEmpty()) return;

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

            // Determine all custom outputs we are going to replace.
            java.util.Set<String> newOutputIds = new java.util.HashSet<>();
            for (SlayerRecipes.Recipe r : recipes) {
                String outId = SlayerItems.getCustomItemId(r.result);
                if (outId != null && !outId.isBlank()) newOutputIds.add(outId);
            }

            // Remove any existing recipe whose output custom id matches.
            for (java.util.Iterator<Map.Entry<String, JsonObject>> it = existingRecipes.entrySet().iterator(); it.hasNext();) {
                Map.Entry<String, JsonObject> entry = it.next();
                JsonObject recipeObj = entry.getValue();
                if (!recipeObj.has("result")) continue;

                try {
                    ItemStack existingResult = deserializeStack(recipeObj.get("result"));
                    String existingOutputId = SlayerItems.getCustomItemId(existingResult);
                    if (existingOutputId != null && newOutputIds.contains(existingOutputId)) {
                        it.remove();
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
                        for (String oid : newOutputIds) {
                            if (nbt.contains("custom_item_id\":\"" + oid + "\"") || nbt.contains("custom_id\":\"" + oid + "\"")) {
                                it.remove();
                                break;
                            }
                        }
                    }
                } catch (Exception ignored) {
                    // ignore malformed recipe object
                }
            }

            // Add all new/updated recipes
            for (SlayerRecipes.Recipe r : recipes) {
                existingRecipes.put(r.name, serialize(r));
            }

            // Save back to file once
            JsonArray recipesArr = new JsonArray();
            for (JsonObject obj : existingRecipes.values()) recipesArr.add(obj);

            JsonObject root = new JsonObject();
            root.add("recipes", recipesArr);

            writeRecipeJsonRoot(root);
            invalidateRecipeCache();

        } catch (Exception e) {
            // Bootstrap should never hard-crash the server because of recipe file I/O.
            System.err.println("[RecipeConfigManager] Bulk upsert failed: " + e.getMessage());
        }
    }

    private static void addCustomRecipeInternal(SlayerRecipes.Recipe recipe, boolean silent, boolean registerNow) {
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

            // Only replace the entry with the same recipe name so multiple ingredient sets can target one item.
            existingRecipes.remove(recipe.name);

            // Add new recipe
            existingRecipes.put(recipe.name, serialize(recipe));

            // Save back to file
            JsonArray recipesArr = new JsonArray();
            for (JsonObject obj : existingRecipes.values()) {
                recipesArr.add(obj);
            }

            JsonObject root = new JsonObject();
            root.add("recipes", recipesArr);

            writeRecipeJsonRoot(root);

            if (!silent) {
                System.out.println("[RecipeConfigManager] /setrecipe: OK | Wrote '" + recipe.name + "'");
            }

            // Force cache refresh on next access
            invalidateRecipeCache();
            if (registerNow) {
                SlayerRecipes.registerRecipes();
            }

        } catch (Exception e) {
            if (!silent) {
                System.err.println("[RecipeConfigManager] Failed to save recipe: " + e.getMessage());
                e.printStackTrace();
            }
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
                
                writeRecipeJsonRoot(root);
                
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
            
            writeRecipeJsonRoot(root);
            
            System.out.println("[RecipeConfigManager] Regenerated " + recipes.size() + " recipes to " + CONFIG_FILE);
            invalidateRecipeCache();
            SlayerRecipes.registerRecipes();
            
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
                // Avoid log spam for malformed/unsupported recipes; those entries are ignored anyway.
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
            RegistryOps<net.minecraft.nbt.Tag> ops = null;
            if (PoliticalServer.server != null) {
                ops = RegistryOps.create(NbtOps.INSTANCE, PoliticalServer.server.registryAccess());
            } else {
                ops = RegistryOps.create(NbtOps.INSTANCE, RegistryAccess.EMPTY);
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

    /**
     * Recovers an item whose stored SNBT names a base item that no longer exists (or never did)
     * by pulling {@code custom_item_id} out of the raw text and rebuilding from the known
     * factory. This is what keeps a single bad export from silently dropping a recipe.
     */
    private static ItemStack rebuildFromEmbeddedCustomId(String nbtString, JsonObject obj) {
        if (nbtString == null || nbtString.isEmpty()) return ItemStack.EMPTY;
        java.util.regex.Matcher m = EMBEDDED_CUSTOM_ID.matcher(nbtString);
        if (!m.find()) return ItemStack.EMPTY;
        String customId = m.group(1);
        if (customId == null || customId.isBlank()) return ItemStack.EMPTY;
        try {
            ItemStack rebuilt = resolveCustomItem(customId);
            if (rebuilt != null && !rebuilt.isEmpty()) {
                if (obj != null && obj.has("count")) {
                    rebuilt.setCount(Math.max(1, obj.get("count").getAsInt()));
                }
                return rebuilt;
            }
        } catch (Exception ignored) {
            // fall through
        }
        return ItemStack.EMPTY;
    }

    private static final java.util.regex.Pattern EMBEDDED_CUSTOM_ID =
            java.util.regex.Pattern.compile("custom_item_id\\s*:\\s*\"([^\"]+)\"");

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
                    CompoundTag parsed = TagParser.parseCompoundFully(nbtString);

                    // Normalize legacy or loosely-formatted components into the schema expected by ItemStack.CODEC.
                    normalizeParsedStackNbt(parsed);
                    
                    RegistryOps<net.minecraft.nbt.Tag> ops = null;
                    if (PoliticalServer.server != null) {
                        ops = RegistryOps.create(NbtOps.INSTANCE, PoliticalServer.server.registryAccess());
                    } else {
                        ops = RegistryOps.create(NbtOps.INSTANCE, RegistryAccess.EMPTY);
                    }
                    
                    if (ops != null) {
                        var result = ItemStack.CODEC.parse(ops, parsed);
                        if (result.result().isPresent()) {
                            ItemStack stack = result.result().get();
                            int stackCount = obj.has("count") ? obj.get("count").getAsInt() : stack.getCount();
                            if (!stack.isEmpty()) {
                                ItemStack ensured = ensureFullCustomItemComponents(stack);
                                if (ensured != null && !ensured.isEmpty()) {
                                    ensured.setCount(stackCount);
                                    return ensured;
                                }
                                stack.setCount(stackCount);
                                return stack;
                            }
                        }
                    }

                    // Fallback: if SNBT parses but CODEC fails, try extracting minimal identifiers
                    ItemStack fallback = parseStackFromSnbtString(nbtString, obj);
                    if (fallback != null && !fallback.isEmpty()) return fallback;

                    // Last resort: the stored base item id may not exist (bad/stale export, e.g.
                    // "minecraft:golden_crown"). If the SNBT still carries a custom_item_id we
                    // know how to build, rebuild the item from its factory instead of failing.
                    ItemStack rebuilt = rebuildFromEmbeddedCustomId(nbtString, obj);
                    if (rebuilt != null && !rebuilt.isEmpty()) return rebuilt;
                }
            } catch (Exception e) {
                try {
                    String nbtString = obj.get("nbt").getAsString();
                    ItemStack fallback = parseStackFromSnbtString(nbtString, obj);
                    if (fallback != null && !fallback.isEmpty()) return fallback;
                    ItemStack rebuilt = rebuildFromEmbeddedCustomId(nbtString, obj);
                    if (rebuilt != null && !rebuilt.isEmpty()) return rebuilt;
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
                    stack.set(DataComponents.CUSTOM_NAME, Component.literal(customName));
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

        // Root registry id — must NOT use naive key:" (matches suffix of custom_item_id:"...)
        if ("id".equals(key)) {
            String[] rootIdNeedles = { ",id:\"", "{id:\"" };
            for (String n : rootIdNeedles) {
                int pos = snbt.lastIndexOf(n);
                if (pos < 0) continue;
                int valStart = pos + n.length();
                int valEnd = snbt.indexOf("\"", valStart);
                if (valEnd > valStart) {
                    return snbt.substring(valStart, valEnd);
                }
            }
            return null;
        }

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

    private static void normalizeParsedStackNbt(CompoundTag parsed) {
        if (parsed == null) return;

        // Keep dyed_color as plain int - compound format causes CODEC errors
        if (!parsed.contains("components")) return;

        parsed.getCompound("components").ifPresent(components -> {
            // dyed_color stays as plain int, no conversion needed
        });
    }

    /**
     * Some exported recipe JSON entries store "partial" NBT for custom items
     * (often only custom_id/custom_name), which makes the resulting item
     * render blank/missing stats in GUIs.
     *
     * If we detect that key components are missing, rebuild the item from its
     * custom_item_id so it has the full lore/attributes/dyed color/etc.
     */
    private static ItemStack ensureFullCustomItemComponents(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return stack;

        String customId = SlayerItems.getCustomItemId(stack);
        if (customId == null || customId.isEmpty()) return stack;

        boolean looksLikeArmor = looksLikeBlockArmorCustomId(customId);
        boolean looksLikeTool = looksLikeBlockToolCustomId(customId);
        boolean looksLikeDragonChestplate = looksLikeDragonChestplateCustomId(customId);
        boolean looksLikeDragonSword = looksLikeDragonSwordCustomId(customId);
        boolean looksLikeOmegaDragonCore = "dragon_crafter_core_omega".equals(customId);
        boolean forceRebuildDragonGear =
                (customId != null && (customId.startsWith("new_dragon_chestplate_") || customId.startsWith("omega_dragon_chestplate_")));
        if (!looksLikeArmor && !looksLikeTool && !looksLikeDragonChestplate && !looksLikeDragonSword && !looksLikeOmegaDragonCore) return stack;

        boolean missing;
        if (looksLikeArmor) {
            // Leather armor items should carry dyed color unless it's a player head.
            boolean dyedColorMissing = !stack.is(Items.PLAYER_HEAD) && stack.get(DataComponents.DYED_COLOR) == null;
            missing = stack.get(DataComponents.LORE) == null
                    || stack.get(DataComponents.ATTRIBUTE_MODIFIERS) == null
                    || stack.get(DataComponents.MAX_DAMAGE) == null
                    || dyedColorMissing;
        } else if (looksLikeDragonChestplate) {
            missing = stack.get(DataComponents.DYED_COLOR) == null
                    || stack.get(DataComponents.LORE) == null
                    || stack.get(DataComponents.ATTRIBUTE_MODIFIERS) == null
                    || stack.get(DataComponents.MAX_DAMAGE) == null
                    || stack.get(DataComponents.CUSTOM_NAME) == null;
        } else if (looksLikeDragonSword) {
            missing = stack.get(DataComponents.LORE) == null
                    || stack.get(DataComponents.ATTRIBUTE_MODIFIERS) == null
                    || stack.get(DataComponents.MAX_DAMAGE) == null
                    || stack.get(DataComponents.CUSTOM_NAME) == null;
        } else if (looksLikeOmegaDragonCore) {
            missing = stack.get(DataComponents.LORE) == null
                    || stack.get(DataComponents.CUSTOM_NAME) == null;
        } else {
            missing = stack.get(DataComponents.LORE) == null
                    || stack.get(DataComponents.MAX_DAMAGE) == null
                    || stack.get(DataComponents.CUSTOM_NAME) == null;
        }

        if (!missing && !forceRebuildDragonGear) return stack;

        ItemStack rebuilt = resolveCustomItem(customId);
        return (rebuilt == null || rebuilt.isEmpty()) ? stack : rebuilt;
    }

    private static boolean looksLikeBlockArmorCustomId(String customId) {
        if (customId == null) return false;
        // Slayer bounty armor IDs include _t1_/_t2_ and may not use block-style dyed color.
        if (customId.contains("_t1_") || customId.contains("_t2_")) return false;
        return customId.endsWith("_helmet")
                || customId.endsWith("_chestplate")
                || customId.endsWith("_leggings")
                || customId.endsWith("_boots");
    }

    private static boolean looksLikeBlockToolCustomId(String customId) {
        if (customId == null) return false;
        return customId.endsWith("_sword")
                || customId.endsWith("_pickaxe")
                || customId.endsWith("_axe")
                || customId.endsWith("_shovel")
                || customId.endsWith("_hoe");
    }

    private static boolean looksLikeDragonChestplateCustomId(String customId) {
        return DragonChestplateRegistry.isRegistered(customId);
    }

    private static boolean looksLikeDragonSwordCustomId(String customId) {
        return customId != null && customId.startsWith("dragon_sword_");
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
            Identifier itemId = Identifier.parse(id.trim());
            Item item = BuiltInRegistries.ITEM.getValue(itemId);
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

    /**
     * Reflection fallback to reconstruct block-style armor from its custom_id
     * (e.g. {@code blackstone_helmet} -> {@code BlockArmor.createBlackstoneHelmet()}).
     *
     * This is important because some exported JSON entries store partial NBT,
     * and relying only on explicit mappings can leave items blank.
     */
    private static ItemStack tryCreateFromBlockArmorCustomId(String customId) {
        if (customId == null || customId.isEmpty()) return ItemStack.EMPTY;
        // Avoid accidental attempts for dragon chestplates / other non-block items.
        if (customId.contains("_dragon_")) return ItemStack.EMPTY;
        if (customId.contains("_t1_") || customId.contains("_t2_")) return ItemStack.EMPTY;

        String[] armorSuffixes = {"_helmet", "_chestplate", "_leggings", "_boots"};
        for (String suffix : armorSuffixes) {
            if (!customId.endsWith(suffix)) continue;
            String materialKey = customId.substring(0, customId.length() - suffix.length());
            if (materialKey.isEmpty()) return ItemStack.EMPTY;

            String piece = suffix.substring(1); // helmet/chestplate/...
            String piecePascal = capitalizeFirst(piece);
            String methodName = "create" + toPascalCase(materialKey) + piecePascal;

            try {
                var m = BlockArmor.class.getMethod(methodName);
                Object out = m.invoke(null);
                if (out instanceof ItemStack stack && !stack.isEmpty()) {
                    return stack;
                }
            } catch (Exception ignored) {
            }

            return ItemStack.EMPTY;
        }
        return ItemStack.EMPTY;
    }

    /**
     * Reflection fallback to reconstruct block-style tools from its custom_id
     * (e.g. {@code blackstone_bricks_sword} -> {@code BlockWeapons.createBlackstoneBricksSword()}).
     */
    private static ItemStack tryCreateFromBlockWeaponCustomId(String customId) {
        if (customId == null || customId.isEmpty()) return ItemStack.EMPTY;
        if (customId.contains("_dragon_")) return ItemStack.EMPTY;
        if (customId.contains("_t1_") || customId.contains("_t2_")) return ItemStack.EMPTY;

        String[] toolSuffixes = {"_sword", "_pickaxe", "_axe", "_shovel", "_hoe"};
        for (String suffix : toolSuffixes) {
            if (!customId.endsWith(suffix)) continue;
            String materialKey = customId.substring(0, customId.length() - suffix.length());
            if (materialKey.isEmpty()) return ItemStack.EMPTY;

            String piece = suffix.substring(1); // sword/pickaxe/...
            String piecePascal = capitalizeFirst(piece);
            String methodName = "create" + toPascalCase(materialKey) + piecePascal;

            try {
                var m = BlockWeapons.class.getMethod(methodName);
                Object out = m.invoke(null);
                if (out instanceof ItemStack stack && !stack.isEmpty()) {
                    return stack;
                }
            } catch (Exception ignored) {
            }

            return ItemStack.EMPTY;
        }
        return ItemStack.EMPTY;
    }

    private static String capitalizeFirst(String s) {
        if (s == null || s.isEmpty()) return s;
        if (s.length() == 1) return s.substring(0, 1).toUpperCase();
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private static String toPascalCase(String snake) {
        if (snake == null || snake.isEmpty()) return snake;
        String[] parts = snake.split("_");
        StringBuilder out = new StringBuilder();
        for (String p : parts) {
            if (p == null || p.isEmpty()) continue;
            out.append(capitalizeFirst(p));
        }
        return out.toString();
    }

    private static ItemStack resolveCustomItem(String customId) {
        customId = SlayerItems.canonicalizeCustomItemId(customId);
        // First try the custom crafter cores
        // Version X line catalysts. They are plain component items so the SNBT normally round
        // trips on its own, but naming them here means a hand edited recipe still resolves.
        LineCatalyst lineCatalyst = LineCatalyst.byId(customId);
        if (lineCatalyst != null) return lineCatalyst.create();
        if ("custom_crafter_core_t1".equals(customId)) return SlayerItems.createCustomCrafterCoreT1();
        if ("custom_crafter_core_t2".equals(customId)) return SlayerItems.createCustomCrafterCoreT2();
        if ("custom_crafter_core_t3".equals(customId)) return SlayerItems.createCustomCrafterCoreT3();
        if ("dragon_crafter_core_t1".equals(customId)) return SlayerItems.createDragonCrafterCoreT1();
        if ("dragon_crafter_core_t2".equals(customId)) return SlayerItems.createDragonCrafterCoreT2();
        if ("dragon_crafter_core_t3".equals(customId)) return SlayerItems.createDragonCrafterCoreT3();
        if ("dragon_crafter_core_omega".equals(customId)) return SlayerItems.createDragonCrafterCoreOmega();

        // Dragon swords (dragon_sword_XX)
        if (customId != null && customId.startsWith("dragon_sword_")) {
            try {
                int idx = Integer.parseInt(customId.substring("dragon_sword_".length()));
                return DragonSwords.createDragonSword(idx);
            } catch (Exception ignored) {
            }
        }

        // New dragon chestplates (new_dragon_chestplate_XX)
        if (customId != null && customId.startsWith("new_dragon_chestplate_")) {
            try {
                int idx = Integer.parseInt(customId.substring("new_dragon_chestplate_".length()));
                return MoreDragonChestplates.createNewDragonChestplate(idx);
            } catch (Exception ignored) {
            }
        }

        // Omega dragon chestplates (omega_dragon_chestplate_X)
        if (customId != null && customId.startsWith("omega_dragon_chestplate_")) {
            try {
                int idx = Integer.parseInt(customId.substring("omega_dragon_chestplate_".length()));
                return MoreDragonChestplates.createOmegaDragonChestplate(idx);
            } catch (Exception ignored) {
            }
        }

        // Known fixed dragon chestplates
        if ("inferno_dragon_chestplate".equals(customId)) return DragonChestplates.createDragonChestplate1();
        if ("storm_dragon_chestplate".equals(customId)) return DragonChestplates.createDragonChestplate2();
        if ("void_dragon_chestplate".equals(customId)) return DragonChestplates.createDragonChestplate3();
        if ("aurora_dragon_chestplate".equals(customId)) return DragonChestplates.createDragonChestplateAurora();
        if ("frost_dragon_chestplate".equals(customId)) return DragonChestplates.createDragonChestplateFrost();
        if ("venom_dragon_chestplate".equals(customId)) return DragonChestplates.createDragonChestplateVenom();
        if ("blood_dragon_chestplate".equals(customId)) return DragonChestplates.createDragonChestplateBlood();
        if ("eclipse_dragon_chestplate".equals(customId)) return DragonChestplates.createDragonChestplateEclipse();

        // Extended set (15)
        if ("dune_dragon_chestplate".equals(customId)) return MoreDragonChestplates.createDune();
        if ("reef_dragon_chestplate".equals(customId)) return MoreDragonChestplates.createReef();
        if ("ember_dragon_chestplate".equals(customId)) return MoreDragonChestplates.createEmber();
        if ("crystal_dragon_chestplate".equals(customId)) return MoreDragonChestplates.createCrystal();
        if ("shadow_dragon_chestplate".equals(customId)) return MoreDragonChestplates.createShadow();
        if ("solar_dragon_chestplate".equals(customId)) return MoreDragonChestplates.createSolar();
        if ("lunar_dragon_chestplate".equals(customId)) return MoreDragonChestplates.createLunar();
        if ("terra_dragon_chestplate".equals(customId)) return MoreDragonChestplates.createTerra();
        if ("tempest_dragon_chestplate".equals(customId)) return MoreDragonChestplates.createTempest();
        if ("auric_dragon_chestplate".equals(customId)) return MoreDragonChestplates.createAuric();
        if ("glacier_dragon_chestplate".equals(customId)) return MoreDragonChestplates.createGlacier();
        if ("mire_dragon_chestplate".equals(customId)) return MoreDragonChestplates.createMire();
        if ("starfall_dragon_chestplate".equals(customId)) return MoreDragonChestplates.createStarfall();
        if ("mirage_dragon_chestplate".equals(customId)) return MoreDragonChestplates.createMirage();
        if ("obsidian_wyrm_chestplate".equals(customId)) return MoreDragonChestplates.createObsidianWyrm();
        
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
            if ("obsidian_block_helmet".equals(customId)) return BlockArmor.createObsidianHelmet();
            if ("obsidian_block_chestplate".equals(customId)) return BlockArmor.createObsidianChestplate();
            if ("obsidian_block_leggings".equals(customId)) return BlockArmor.createObsidianLeggings();
            if ("obsidian_block_boots".equals(customId)) return BlockArmor.createObsidianBoots();
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
        if ("obsidian_block_sword".equals(customId)) return BlockWeapons.createObsidianSword();
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
        if ("end_stone_sword".equals(customId)) return BlockWeapons.createEndstoneSword();
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
        // The crowns are dyed leather helmets; older exports stored a bogus "minecraft:golden_crown"
        // base item, so they must resolve through their factories.
        if ("crown_of_greed".equals(customId)) return SlayerItems.createCrownOfGreed();
        if ("crown_of_midas".equals(customId)) return SlayerItems.createCrownOfMidas();
        if ("rift_mirror".equals(customId)) return CustomItemHandler.createRiftMirror();
        if ("ember_signet".equals(customId)) return CustomItemHandler.createEmberSignet();
        if ("frostbound_talisman".equals(customId)) return CustomItemHandler.createFrostboundTalisman();
        if ("echo_chime".equals(customId)) return CustomItemHandler.createEchoChime();
        if ("terra_relay".equals(customId)) return CustomItemHandler.createTerraRelay();
        if ("hermes_shoes".equals(customId)) return CustomItemHandler.createHermesShoes();
        if ("daniels".equals(customId)) return DanielsPickaxe.create(); // legacy malformed json alias
        if ("daniels_pickaxe".equals(customId)) return DanielsPickaxe.create();
        
        // NEW BOSS DROPS
        if ("undead_heart".equals(customId)) return SlayerItems.createUndeadHeart();
        if ("spectral_quiver".equals(customId)) return SlayerItems.createSpectralQuiver();
        if ("echoing_core".equals(customId)) return SlayerItems.createEchoingCore();
        if ("ender_sword".equals(customId)) return SlayerItems.createEnderSword();
        if ("abyssal_blade".equals(customId)) return SlayerItems.createAbyssalBlade();
        if ("bouncy_slime".equals(customId)) return SlayerItems.createBouncySlime();
        if ("venomous_dagger".equals(customId)) return SlayerItems.createVenomousDagger();
        
        // Dragon Chestplates
        if ("inferno_dragon_chestplate".equals(customId)) return DragonChestplates.createDragonChestplate1();
        if ("storm_dragon_chestplate".equals(customId)) return DragonChestplates.createDragonChestplate2();
        if ("void_dragon_chestplate".equals(customId)) return DragonChestplates.createDragonChestplate3();
        if ("aurora_dragon_chestplate".equals(customId)) return DragonChestplates.createDragonChestplateAurora();
        if ("frost_dragon_chestplate".equals(customId)) return DragonChestplates.createDragonChestplateFrost();
        if ("venom_dragon_chestplate".equals(customId)) return DragonChestplates.createDragonChestplateVenom();
        if ("blood_dragon_chestplate".equals(customId)) return DragonChestplates.createDragonChestplateBlood();
        if ("eclipse_dragon_chestplate".equals(customId)) return DragonChestplates.createDragonChestplateEclipse();
        if ("dune_dragon_chestplate".equals(customId)) return MoreDragonChestplates.createDune();
        if ("reef_dragon_chestplate".equals(customId)) return MoreDragonChestplates.createReef();
        if ("ember_dragon_chestplate".equals(customId)) return MoreDragonChestplates.createEmber();
        if ("crystal_dragon_chestplate".equals(customId)) return MoreDragonChestplates.createCrystal();
        if ("shadow_dragon_chestplate".equals(customId)) return MoreDragonChestplates.createShadow();
        if ("solar_dragon_chestplate".equals(customId)) return MoreDragonChestplates.createSolar();
        if ("lunar_dragon_chestplate".equals(customId)) return MoreDragonChestplates.createLunar();
        if ("terra_dragon_chestplate".equals(customId)) return MoreDragonChestplates.createTerra();
        if ("tempest_dragon_chestplate".equals(customId)) return MoreDragonChestplates.createTempest();
        if ("auric_dragon_chestplate".equals(customId)) return MoreDragonChestplates.createAuric();
        if ("glacier_dragon_chestplate".equals(customId)) return MoreDragonChestplates.createGlacier();
        if ("mire_dragon_chestplate".equals(customId)) return MoreDragonChestplates.createMire();
        if ("starfall_dragon_chestplate".equals(customId)) return MoreDragonChestplates.createStarfall();
        if ("mirage_dragon_chestplate".equals(customId)) return MoreDragonChestplates.createMirage();
        if ("obsidian_wyrm_chestplate".equals(customId)) return MoreDragonChestplates.createObsidianWyrm();
        
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
        
        // SlayerItems material/gear families (enchanted_*, compacted_*, super_compacted_*, gilded_*)
        // Reflective mapping to SlayerItems.create<PascalCase>() — restores the ~69 legacy recipes
        // whose "political:<id>" results had no explicit switch entry above.
        ItemStack slayerFamilyAttempt = SlayerItemFamilyResolver.resolve(customId);
        if (slayerFamilyAttempt != null && !slayerFamilyAttempt.isEmpty()) return slayerFamilyAttempt;

        // Reflection fallback (covers lots of block armor/tool custom ids)
        ItemStack blockArmorAttempt = tryCreateFromBlockArmorCustomId(customId);
        if (blockArmorAttempt != null && !blockArmorAttempt.isEmpty()) return blockArmorAttempt;
        ItemStack blockWeaponAttempt = tryCreateFromBlockWeaponCustomId(customId);
        if (blockWeaponAttempt != null && !blockWeaponAttempt.isEmpty()) return blockWeaponAttempt;
        
        // Backrooms/Fracture 3 items
        // Backrooms rune items — try direct registry lookup for any politicalserver: item
        try {
            Identifier itemId = Identifier.fromNamespaceAndPath(PoliticalServer.MOD_ID, customId);
            Item item = BuiltInRegistries.ITEM.getValue(itemId);
            if (item != Items.AIR) return new ItemStack(item);
        } catch (IllegalArgumentException ignored) {
            // malformed identifier — ignore silently
        } catch (Exception e) {
            System.err.println("[RecipeConfigManager] Unexpected error resolving item '" + customId + "': " + e.getMessage());
        }
        return ItemStack.EMPTY;
    }
}
