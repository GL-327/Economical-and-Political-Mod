package com.political.recipe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.political.DanielsPickaxe;
import com.political.PoliticalServer;
import com.political.RecipeIngredientHydrator;
import com.political.SlayerItemFamilyResolver;
import com.political.bounty.SlayerItems;
import com.political.bounty.SlayerManager;
import com.political.items.CustomItemHandler;
import com.political.items.CustomItemManager;
import com.political.items.DragonChestplateRegistry;
import com.political.items.DragonChestplates;
import com.political.items.LegendaryTools;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(RecipeConfigManager.class);


    private static final Map<String, String> GENERATED_THIS_BOOT = new java.util.HashMap<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    /** Live recipe JSON is the game config dir so loom boots do not rewrite the git stub. */
    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve("political");
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("custom_recipes.json");
    private static final Path REPO_RECIPE_STUB = FabricLoader.getInstance().getGameDir().getParent()
            .resolve("config").resolve("political").resolve("custom_recipes.json");
    // Modular recipe file path (unused but kept for potential future use)
    private static final java.nio.file.Path MODULAR_FILE = CONFIG_DIR.resolve("modular_recipes.json");
    private static final Path DISABLED_FILE = CONFIG_DIR.resolve("disabled_recipes.json");
    /**
     * Combinatorial leftover dumps were ~26MB. A healthy generated list (core + extras)
     * pretty-prints to about 1MB, so only treat multi-megabyte files as slop.
     */
    private static final long SLOP_RECIPE_FILE_BYTES = 8L * 1024L * 1024L;

    static {
        boolean fileExists = java.nio.file.Files.exists(CONFIG_FILE);
        LOGGER.info("JSON: {} | Path: {}", (fileExists ? "OK" : "MISSING"), CONFIG_FILE.toAbsolutePath());
    }

    /**
     * Sorts the recipe array by name before writing.
     *
     * <p>Recipes are gathered from maps whose iteration order is not stable between runs, so an
     * export that simply wrote them out produced a different byte sequence every boot even when
     * nothing had changed.</p>
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
                    LOGGER.debug("RecipeConfigManager.sortRecipesByName: ignored an error", ex);
                    return "";
                }
            }));
            JsonArray sorted = new JsonArray();
            items.forEach(sorted::add);
            root.add("recipes", sorted);
        } catch (Exception ignored) {
            LOGGER.debug("RecipeConfigManager.sortRecipesByName: ignored an error", ignored);
            // A malformed root is still worth writing unsorted rather than not at all.
        }
    }

    private static void writeRecipeJsonRoot(JsonObject root) throws IOException {
        sortRecipesByName(root);
        RecipeJsonFiles.write(CONFIG_FILE, GSON.toJson(root));
    }

    /**
     * Reports oversized legacy recipe catalogs. Their contents are preserved for the owner.
     */
    public static void pruneSlopRecipeFile() {
        pruneIfHuge(CONFIG_FILE);
        pruneIfHuge(REPO_RECIPE_STUB);
    }

    private static void pruneIfHuge(Path path) {
        try {
            if (path != null && Files.exists(path) && Files.size(path) > SLOP_RECIPE_FILE_BYTES)
                LOGGER.warn("Large recipe file preserved without pruning: {}", path);
        } catch (IOException e) { LOGGER.error("Could not inspect recipe file {}", path, e); }
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

    /** Recipe count from JSON structure only — does not deserialize item stacks. */
    public static int countRecipesOnDisk() {
        try {
            if (!Files.exists(CONFIG_FILE) || Files.size(CONFIG_FILE) == 0) return 0;
            String jsonString = Files.readString(CONFIG_FILE);
            if (jsonString == null || jsonString.isBlank()) return 0;
            JsonObject root = GSON.fromJson(jsonString, JsonObject.class);
            if (root == null || !root.has("recipes") || !root.get("recipes").isJsonArray()) return 0;
            return root.getAsJsonArray("recipes").size();
        } catch (Exception e) {
            LOGGER.debug("RecipeConfigManager.countRecipesOnDisk: ignored an error", e);
            return 0;
        }
    }

    // ── Lifecycle ──────────────────────────────────────────────

    /** Load persisted recipes from disk. Call on server start. */
    public static void load() {
        pruneSlopRecipeFile();
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
                LOGGER.info("Created empty custom_recipes.json at {}", CONFIG_FILE.toAbsolutePath());
                return;
            }

            String jsonString = Files.readString(CONFIG_FILE);
            if (jsonString == null || jsonString.isBlank()) {
                JsonObject root = new JsonObject();
                root.add("recipes", new JsonArray());
                writeRecipeJsonRoot(root);
                LOGGER.info("custom_recipes.json was blank; initialized empty recipes at {}", CONFIG_FILE.toAbsolutePath());
                return;
            }

            JsonObject root = GSON.fromJson(jsonString, JsonObject.class);
            if (root == null || !root.has("recipes") || !root.get("recipes").isJsonArray()) {
                // Never overwrite a non-trivial file — that was wiping large recipe lists on parse quirks.
                long bytes = Files.size(CONFIG_FILE);
                LOGGER.error("custom_recipes.json missing valid \"recipes\" array — leaving file unchanged (size={} bytes). Path: {}", bytes, CONFIG_FILE.toAbsolutePath());
            }
        } catch (Exception e) {
            LOGGER.error("Failed to read custom_recipes.json | Path: {}", CONFIG_FILE.toAbsolutePath(), e);
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
            LOGGER.info("JSON recipes in file: {} | size={} bytes | {}", count, bytes, abs);
        } catch (Exception e) {
            LOGGER.error("Could not summarize JSON recipes | Path: {}", CONFIG_FILE.toAbsolutePath(), e);
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
                            LOGGER.warn("RecipeConfigManager.getRecipesCached: ignored an error", e);
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
                            LOGGER.info("crafting: OK | {} recipes ({} failed)", recipes.size(), failed);
                            if (failed > 0) {
                                LOGGER.info("failed recipes: {}", failedNames);
                            }
                        }
                    } else if ("recipe".equals(contextForLog)) {
                        LOGGER.info("/recipe: loaded {} recipes ({} failed)", recipes.size(), failed);
                    }
                }

                return RECIPE_CACHE;
            }
        } catch (Exception e) {
            LOGGER.debug("RecipeConfigManager.getRecipesCached: ignored an error", e);
            return java.util.Collections.emptyList();
        }
    }

    public static void logRecipeFileStatusForRecipeCommand() {
        try {
            boolean exists = Files.exists(CONFIG_FILE);
            boolean readable = Files.isReadable(CONFIG_FILE);
            long size = exists ? Files.size(CONFIG_FILE) : 0;
            LOGGER.info("/recipe file: {} | size={} | {}", (exists && readable ? "OK" : "BAD"), size, CONFIG_FILE);
        } catch (Exception e) {
            LOGGER.error("/recipe file: BAD", e);
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
            LOGGER.error("Failed to load disabled recipes", e);
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
            LOGGER.error("Failed to save disabled recipes", e);
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
            JsonObject existing;
            if (Files.exists(CONFIG_FILE)) {
                existing = JsonParser.parseString(Files.readString(CONFIG_FILE)).getAsJsonObject();
                if (!existing.has("recipes") || !existing.get("recipes").isJsonArray()) {
                    LOGGER.error("Malformed recipe catalog preserved; defaults were not written");
                    return;
                }
            } else {
                existing = new JsonObject();
                existing.add("recipes", new JsonArray());
            }
            List<JsonObject> generated = new ArrayList<>();
            for (SlayerRecipes.Recipe recipe : recipes) if (recipe != null) generated.add(serialize(recipe));
            writeRecipeJsonRoot(RecipeJsonDefaults.merge(existing, generated, GENERATED_THIS_BOOT));
            invalidateRecipeCache();
        } catch (Exception e) { LOGGER.error("Bulk recipe default update failed; existing data preserved", e); }
    }

    /** Overwrites the recipe file with exactly this list (no merge with leftover JSON). */
    public static void replaceAllCustomRecipes(List<SlayerRecipes.Recipe> recipes) {
        try {
            JsonArray recipesArr = new JsonArray();
            if (recipes != null) {
                for (SlayerRecipes.Recipe r : recipes) {
                    if (r == null || r.name == null || r.name.isBlank()) continue;
                    recipesArr.add(serialize(r));
                }
            }
            JsonObject root = new JsonObject();
            root.add("recipes", recipesArr);
            writeRecipeJsonRoot(root);
            invalidateRecipeCache();
            LOGGER.info("Replaced recipe file with {} recipes", recipesArr.size());
        } catch (Exception e) {
            LOGGER.error("replaceAllCustomRecipes failed", e);
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
                LOGGER.info("/setrecipe: OK | Wrote '{}'", recipe.name);
            }

            // Force cache refresh on next access
            invalidateRecipeCache();
            if (registerNow) {
                SlayerRecipes.registerRecipes();
            }

        } catch (Exception e) {
            if (!silent) {
                LOGGER.error("Failed to save recipe", e);
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
                
                LOGGER.info("Removed recipe '{}' from JSON", name);
                
                // Reload recipes into memory
                load();
            }
            
            return removed;
        } catch (Exception e) {
            LOGGER.error("Failed to remove recipe", e);
            return false;
        }
    }

    /** Regenerates the small default recipe list. Extra bootstraps re-apply on the next server start. */
    public static void regenerateRecipes() {
        try {
            pruneSlopRecipeFile();
            DefaultRecipeBootstrap.ensureRecipes();
            invalidateRecipeCache();
            SlayerRecipes.registerRecipes();
            LOGGER.info("Regenerated default recipes to {}", CONFIG_FILE);
        } catch (Exception e) {
            LOGGER.error("Failed to regenerate recipes", e);
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
            LOGGER.debug("Loading base recipe: {}", name);
            
            // Handle result with nbt field
            JsonObject resultObj = obj.getAsJsonObject("result");
            ItemStack result = deserializeStack(resultObj);
            
            if (result.isEmpty()) {
                LOGGER.error("FAILED to load base recipe '{}': result item is empty", name);
                return null;
            }
            
            LOGGER.debug("Successfully parsed result for base recipe: {}", name);

            SlayerManager.SlayerType slayerType = null;
            if (!slayerStr.isEmpty()) {
                try { slayerType = SlayerManager.SlayerType.valueOf(slayerStr); } catch (Exception ignored) { LOGGER.debug("RecipeConfigManager.deserializeBaseRecipe: ignored an error", ignored); }
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

            LOGGER.debug("Successfully loaded base recipe: {}", name);
            return new SlayerRecipes.Recipe(name, result, grid, lockedSlots, slayerType, level);
        } catch (Exception e) {
            LOGGER.error("Error deserializing base recipe", e);
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
                slayerType = SlayerManager.SlayerType.valueOf(slayerStr);
            }

            JsonArray gridArr = obj.getAsJsonArray("ingredients");
            if (gridArr == null || gridArr.size() != 9) throw new IllegalArgumentException("Expected nine ingredient cells");
            ItemStack[] grid = new ItemStack[9];
            boolean any = false;
            for (int i = 0; i < 9; i++) {
                var raw = gridArr.get(i);
                boolean empty = raw == null || raw.isJsonNull() || (raw.isJsonPrimitive() && raw.getAsString().isEmpty());
                if (!empty && raw.isJsonObject() && raw.getAsJsonObject().has("count")
                        && raw.getAsJsonObject().get("count").getAsInt() <= 0)
                    throw new IllegalArgumentException("Nonpositive ingredient count in cell " + i);
                grid[i] = deserializeIngredient(raw);
                if (!empty && grid[i].isEmpty()) throw new IllegalArgumentException("Unresolved ingredient in cell " + i);
                any |= !grid[i].isEmpty();
            }
            if (!any) throw new IllegalArgumentException("Recipe has no ingredients");

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
            LOGGER.error("Recipe {} was not registered; saved entry preserved", obj.get("name"), e);
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
            LOGGER.error("Failed to serialize stack", e);
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
            LOGGER.debug("RecipeConfigManager.rebuildFromEmbeddedCustomId: ignored an error", ignored);
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
                LOGGER.debug("RecipeConfigManager.deserializeStack: ignored an error", e);
                try {
                    String nbtString = obj.get("nbt").getAsString();
                    ItemStack fallback = parseStackFromSnbtString(nbtString, obj);
                    if (fallback != null && !fallback.isEmpty()) return fallback;
                    ItemStack rebuilt = rebuildFromEmbeddedCustomId(nbtString, obj);
                    if (rebuilt != null && !rebuilt.isEmpty()) return rebuilt;
                } catch (Exception ignored) {
                    LOGGER.debug("RecipeConfigManager.deserializeStack: ignored an error", ignored);
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

        boolean looksLikeDragonChestplate = looksLikeDragonChestplateCustomId(customId);
        if (!looksLikeDragonChestplate) return stack;

        boolean missing = stack.get(DataComponents.LORE) == null
                || stack.get(DataComponents.ATTRIBUTE_MODIFIERS) == null
                || stack.get(DataComponents.CUSTOM_NAME) == null;
        if (!missing) return stack;

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
            LOGGER.debug("RecipeConfigManager.resolveItem: ignored an error", e);
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
        customId = SlayerItems.canonicalizeCustomItemId(customId);
        if ("custom_crafter_core_t1".equals(customId)) return SlayerItems.createCustomCrafterCoreT1();
        if ("custom_crafter_core_t2".equals(customId)) return SlayerItems.createCustomCrafterCoreT2();
        if ("custom_crafter_core_t3".equals(customId)) return SlayerItems.createCustomCrafterCoreT3();
        if ("dragon_crafter_core_t1".equals(customId)) return SlayerItems.createDragonCrafterCoreT1();
        if ("dragon_crafter_core_t2".equals(customId)) return SlayerItems.createDragonCrafterCoreT2();
        if ("dragon_crafter_core_t3".equals(customId)) return SlayerItems.createDragonCrafterCoreT3();
        if ("dragon_crafter_core_omega".equals(customId)) return SlayerItems.createDragonCrafterCoreOmega();

        // Original dragon chestplates
        if ("inferno_dragon_chestplate".equals(customId)) return DragonChestplates.createDragonChestplate1();
        if ("storm_dragon_chestplate".equals(customId)) return DragonChestplates.createDragonChestplate2();
        if ("void_dragon_chestplate".equals(customId)) return DragonChestplates.createDragonChestplate3();
        if ("aurora_dragon_chestplate".equals(customId)) return DragonChestplates.createDragonChestplateAurora();
        if ("frost_dragon_chestplate".equals(customId)) return DragonChestplates.createDragonChestplateFrost();
        if ("venom_dragon_chestplate".equals(customId)) return DragonChestplates.createDragonChestplateVenom();
        if ("blood_dragon_chestplate".equals(customId)) return DragonChestplates.createDragonChestplateBlood();
        if ("eclipse_dragon_chestplate".equals(customId)) return DragonChestplates.createDragonChestplateEclipse();

        if ("daniels_pickaxe".equals(customId)) return DanielsPickaxe.create();
        if ("harveys_stick".equals(customId)) return CustomItemHandler.createHarveysStick();
        if ("hermes_shoes".equals(customId)) return CustomItemHandler.createHermesShoes();
        if ("rift_mirror".equals(customId)) return CustomItemHandler.createRiftMirror();
        if ("ember_signet".equals(customId)) return CustomItemHandler.createEmberSignet();
        if ("frostbound_talisman".equals(customId)) return CustomItemHandler.createFrostboundTalisman();
        if ("echo_chime".equals(customId)) return CustomItemHandler.createEchoChime();
        if ("terra_relay".equals(customId)) return CustomItemHandler.createTerraRelay();
        if ("crown_of_greed".equals(customId)) return SlayerItems.createCrownOfGreed();
        if ("crown_of_midas".equals(customId)) return SlayerItems.createCrownOfMidas();
        if ("midas_sword".equals(customId)) return SlayerItems.createMidasSword();
        if ("voidwalker_crown".equals(customId)) return SlayerItems.createVoidwalkerCrown();
        if ("ender_phase_helmet".equals(customId)) return SlayerItems.createEnderPhaseHelmet();
        if ("venomous_dagger".equals(customId)) return SlayerItems.createVenomousDagger();

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
        
        // Legendary Tools (15 sets)
        if ("dragon_pickaxe".equals(customId)) return LegendaryTools.createDragonPickaxe();
        if ("dragon_axe".equals(customId)) return LegendaryTools.createDragonAxe();
        if ("dragon_shovel".equals(customId)) return LegendaryTools.createDragonShovel();
        if ("dragon_hoe".equals(customId)) return LegendaryTools.createDragonHoe();
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
        
        // SlayerItems material/gear families (enchanted_*, compacted_*, super_compacted_*, gilded_*)
        // Reflective mapping to SlayerItems.create<PascalCase>() — restores the ~69 legacy recipes
        // whose "political:<id>" results had no explicit switch entry above.
        ItemStack slayerFamilyAttempt = SlayerItemFamilyResolver.resolve(customId);
        if (slayerFamilyAttempt != null && !slayerFamilyAttempt.isEmpty()) return slayerFamilyAttempt;

        try {
            CustomItemManager.CustomItem def = CustomItemManager.getItem(customId);
            if (def != null) {
                ItemStack fromDef = def.createStack(1);
                if (fromDef != null && !fromDef.isEmpty()) return fromDef;
            }
        } catch (Exception ignored) {
            LOGGER.debug("RecipeConfigManager.resolveCustomItem: ignored an error", ignored);
            // Custom items may not be initialized yet during early recipe load.
        }

        // Unknown custom ids: try the mod item registry, then give up.
        try {
            Identifier itemId = Identifier.fromNamespaceAndPath(PoliticalServer.MOD_ID, customId);
            Item item = BuiltInRegistries.ITEM.getValue(itemId);
            if (item != Items.AIR) return new ItemStack(item);
        } catch (IllegalArgumentException ignored) {
            // malformed identifier — ignore silently
        } catch (Exception e) {
            LOGGER.error("Unexpected error resolving item '{}'", customId, e);
        }
        return ItemStack.EMPTY;
    }
}
