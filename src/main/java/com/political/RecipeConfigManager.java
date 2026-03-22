package com.political;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
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
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages custom crafting recipes with support for preset defaults and user overrides.
 * Preset recipes are loaded from the file initially and act as defaults.
 * User overrides via /setrecipe take precedence and are saved separately.
 * Both lists persist across server restarts.
 */
public class RecipeConfigManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve("political");
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("custom_recipes.json");
    private static final Path DISABLED_FILE = CONFIG_DIR.resolve("disabled_recipes.json");

    /** Custom recipes added via /setrecipe (override defaults). */
    private static final List<SlayerRecipes.Recipe> CUSTOM_RECIPES = new ArrayList<>();

    /** Names of recipes that are currently disabled. */
    private static final java.util.Set<String> DISABLED_RECIPES = new java.util.HashSet<>();

    // ── Lifecycle ──────────────────────────────────────────────

    /** Load persisted recipes from disk. Call on server start. */
    public static void load() {
        CUSTOM_RECIPES.clear();
        
        // First, load all base recipes and add them as custom recipes
        List<SlayerRecipes.Recipe> baseRecipes = getBaseRecipes();
        for (SlayerRecipes.Recipe recipe : baseRecipes) {
            if (recipe != null) {
                CUSTOM_RECIPES.add(recipe);
            }
        }
        System.out.println("[RecipeConfigManager] Added " + baseRecipes.size() + " base recipes as custom recipes");
        
        // Then load any additional custom overrides from file (these will replace base recipes with same output)
        if (Files.exists(CONFIG_FILE)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_FILE)) {
                JsonObject root = GSON.fromJson(reader, JsonObject.class);
                
                if (root != null && root.has("custom")) {
                    JsonArray arr = root.getAsJsonArray("custom");
                    for (var elem : arr) {
                        SlayerRecipes.Recipe recipe = deserialize(elem.getAsJsonObject());
                        if (recipe != null) {
                            // Remove existing recipe with same output
                            String newOutputId = SlayerItems.getCustomItemId(recipe.result);
                            CUSTOM_RECIPES.removeIf(r -> {
                                String existingId = SlayerItems.getCustomItemId(r.result);
                                return existingId != null && existingId.equals(newOutputId);
                            });
                            CUSTOM_RECIPES.add(recipe);
                            System.out.println("[RecipeConfigManager] Loaded custom override: " + recipe.name);
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("[RecipeConfigManager] Failed to load custom recipes: " + e.getMessage());
            }
        }
        
        System.out.println("[RecipeConfigManager] Total custom recipes: " + CUSTOM_RECIPES.size());
        loadDisabled();
    }

    /** Save custom recipe overrides to disk. */
    public static void save() {
        try {
            Files.createDirectories(CONFIG_DIR);
            
            JsonObject root = new JsonObject();
            
            // Only save custom overrides - defaults are hardcoded
            JsonArray customArr = new JsonArray();
            for (SlayerRecipes.Recipe r : CUSTOM_RECIPES) {
                customArr.add(serialize(r));
            }
            root.add("custom", customArr);
            
            try (Writer writer = Files.newBufferedWriter(CONFIG_FILE)) {
                GSON.toJson(root, writer);
            }
            
            System.out.println("[RecipeConfigManager] Saved " + CUSTOM_RECIPES.size() + " custom recipe overrides");
        } catch (Exception e) {
            System.err.println("[RecipeConfigManager] Failed to save recipes: " + e.getMessage());
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
     * Returns all recipes — base recipes from JSON merged with custom overrides.
     * Custom recipes take precedence over defaults with the same output.
     */
    public static List<SlayerRecipes.Recipe> getRecipes() {
        // Start with all base recipes from JSON
        List<SlayerRecipes.Recipe> merged = new ArrayList<>(getBaseRecipes());
        
        // Add custom recipes, replacing any defaults with same output
        for (SlayerRecipes.Recipe custom : CUSTOM_RECIPES) {
            String customOutputId = SlayerItems.getCustomItemId(custom.result);
            
            // Remove any base recipe that produces the same output
            merged.removeIf(r -> {
                String baseOutputId = SlayerItems.getCustomItemId(r.result);
                return baseOutputId != null && baseOutputId.equals(customOutputId);
            });
            
            merged.add(custom);
        }
        
        return merged;
    }

    /** Returns only the custom (admin-set) override recipes. */
    public static List<SlayerRecipes.Recipe> getCustomRecipes() {
        return new ArrayList<>(CUSTOM_RECIPES);
    }
    
    /** Returns the base recipes from JSON (not custom overrides) */
    public static List<SlayerRecipes.Recipe> getBaseRecipes() {
        List<SlayerRecipes.Recipe> baseRecipes = new ArrayList<>();
        try {
            Path configFile = Paths.get("config/political/custom_recipes.json");
            if (!Files.exists(configFile)) {
                return baseRecipes;
            }
            
            String content = Files.readString(configFile);
            JsonObject json = JsonParser.parseString(content).getAsJsonObject();
            
            if (json.has("recipes")) {
                JsonArray recipesArray = json.getAsJsonArray("recipes");
                for (JsonElement element : recipesArray) {
                    try {
                        SlayerRecipes.Recipe recipe = deserialize(element.getAsJsonObject());
                        if (recipe != null) {
                            baseRecipes.add(recipe);
                        }
                    } catch (Exception e) {
                        System.err.println("[RecipeConfigManager] Failed to load base recipe: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[RecipeConfigManager] Error loading base recipes: " + e.getMessage());
        }
        return baseRecipes;
    }

    /**
     * Adds or updates a custom recipe override.
     * If a recipe produces the same output as a default, the default is hidden.
     */
    public static void addCustomRecipe(SlayerRecipes.Recipe recipe) {
        // Remove any existing custom recipe with same output
        String newOutputId = SlayerItems.getCustomItemId(recipe.result);
        CUSTOM_RECIPES.removeIf(r -> {
            String existingId = SlayerItems.getCustomItemId(r.result);
            return existingId != null && existingId.equals(newOutputId);
        });
        
        CUSTOM_RECIPES.add(recipe);
        save();
    }

    /** Removes a custom recipe by name and persists. */
    public static boolean removeCustomRecipe(String name) {
        boolean removed = CUSTOM_RECIPES.removeIf(r -> r.name.equalsIgnoreCase(name));
        if (removed) save();
        return removed;
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
            
            // Log recipe being loaded
            System.out.println("[RecipeConfigManager] Loading recipe: " + name);
            
            ItemStack result = deserializeStack(obj.get("result"));
            
            if (result.isEmpty()) {
                System.err.println("[RecipeConfigManager] FAILED to load recipe '" + name + "': result item is empty");
                return null;
            }
            
            System.out.println("[RecipeConfigManager] Successfully parsed result for: " + name);

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

            System.out.println("[RecipeConfigManager] Successfully loaded recipe: " + name);
            return new SlayerRecipes.Recipe(name, result, grid, lockedSlots, slayerType, level);
        } catch (Exception e) {
            String name = obj.has("name") ? obj.get("name").getAsString() : "UNKNOWN";
            System.err.println("[RecipeConfigManager] EXCEPTION loading recipe '" + name + "': " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private static com.google.gson.JsonElement serializeStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return new com.google.gson.JsonPrimitive("");

        if (PoliticalServer.server != null) {
            try {
                RegistryOps<net.minecraft.nbt.NbtElement> ops = PoliticalServer.server.getRegistryManager().getOps(NbtOps.INSTANCE);
                var result = ItemStack.CODEC.encodeStart(ops, stack);
                if (result.result().isPresent()) {
                    JsonObject obj = new JsonObject();
                    obj.addProperty("nbt", result.result().get().toString());
                    obj.addProperty("count", stack.getCount());
                    return obj;
                }
            } catch (Exception e) {
                System.err.println("[RecipeConfigManager] Failed to serialize stack with CODEC: " + e.getMessage());
            }
        }

        JsonObject obj = new JsonObject();
        Identifier registryId = Registries.ITEM.getId(stack.getItem());
        obj.addProperty("id", registryId.toString());
        obj.addProperty("count", stack.getCount());

        String customId = SlayerItems.getCustomItemId(stack);
        if (customId != null) obj.addProperty("customId", customId);

        Text customName = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (customName != null) obj.addProperty("customName", customName.getString());
        return obj;
    }

    private static ItemStack deserializeStack(com.google.gson.JsonElement elem) {
        if (elem == null || elem.isJsonNull()) return ItemStack.EMPTY;
        if (elem.isJsonPrimitive()) {
            String id = elem.getAsString();
            return id == null || id.isEmpty() ? ItemStack.EMPTY : resolveItem(id);
        }
        if (!elem.isJsonObject()) return ItemStack.EMPTY;
        JsonObject obj = elem.getAsJsonObject();

        if (obj.has("nbt")) {
            try {
                String nbtString = obj.get("nbt").getAsString();
                if (nbtString != null && !nbtString.isEmpty()) {
                    NbtCompound parsed = StringNbtReader.readCompound(nbtString);
                    
                    // Try to get registry ops - prefer server if available, otherwise use builtin
                    RegistryOps<net.minecraft.nbt.NbtElement> ops = null;
                    if (PoliticalServer.server != null) {
                        ops = PoliticalServer.server.getRegistryManager().getOps(NbtOps.INSTANCE);
                    } else {
                        // Fallback: use builtin registries for early loading
                        ops = net.minecraft.registry.BuiltinRegistries.createWrapperLookup().getOps(NbtOps.INSTANCE);
                    }
                    
                    if (ops != null) {
                        var result = ItemStack.CODEC.parse(ops, parsed);
                        ItemStack stack = result.result().orElse(ItemStack.EMPTY);
                        int count = obj.has("count") ? obj.get("count").getAsInt() : stack.getCount();
                        if (!stack.isEmpty() && count > 1) stack.setCount(count);
                        if (!stack.isEmpty()) return stack;
                    }
                }
            } catch (Exception e) {
                System.err.println("[RecipeConfigManager] Failed to deserialize stack with CODEC: " + e.getMessage());
            }
        }

        String id = obj.has("id") ? obj.get("id").getAsString() : "";
        int count = obj.has("count") ? obj.get("count").getAsInt() : 1;
        String customId = obj.has("customId") ? obj.get("customId").getAsString() : "";
        String customName = obj.has("customName") ? obj.get("customName").getAsString() : "";

        ItemStack stack;

        // Backward compatibility: older saves stored political:<customId> in the id field
        if (id != null && id.startsWith("political:")) {
            stack = resolveItem(id);
        } else {
            stack = (id == null || id.isEmpty()) ? ItemStack.EMPTY : resolveItem(id);
            if (!stack.isEmpty() && customId != null && !customId.isEmpty()) {
                SlayerItems.setCustomItemId(stack, customId);
            }
            if (!stack.isEmpty() && customName != null && !customName.isEmpty()) {
                stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(customName));
            }
        }

        if (!stack.isEmpty() && count > 1) stack.setCount(count);
        return stack;
    }

    private static com.google.gson.JsonElement serializeIngredient(ItemStack ing) {
        if (ing == null || ing.isEmpty()) return new com.google.gson.JsonPrimitive("");
        return serializeStack(ing);
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

    private static ItemStack resolveCustomItem(String customId) {
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
        
        if ("custom_crafter_core_t1".equals(customId)) return SlayerItems.createCustomCrafterCoreT1();
        if ("custom_crafter_core_t2".equals(customId)) return SlayerItems.createCustomCrafterCoreT2();
        if ("custom_crafter_core_t3".equals(customId)) return SlayerItems.createCustomCrafterCoreT3();
        
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
