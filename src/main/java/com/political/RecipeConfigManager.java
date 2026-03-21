package com.political;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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
 * Manages custom crafting recipes that can be set via /setrecipe.
 * Recipes are persisted to config/political/custom_recipes.json and
 * loaded on server start. The combined list (hardcoded + custom) is
 * provided to BountyCraftingHandler for matching.
 */
public class RecipeConfigManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve("political");
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("custom_recipes.json");
    private static final Path DISABLED_FILE = CONFIG_DIR.resolve("disabled_recipes.json");

    /** Custom recipes added via /setrecipe (in addition to hardcoded ones). */
    private static final List<SlayerRecipes.Recipe> CUSTOM_RECIPES = new ArrayList<>();

    /** Names of recipes (hardcoded or custom) that are currently disabled. */
    private static final java.util.Set<String> DISABLED_RECIPES = new java.util.HashSet<>();

    // ── Lifecycle ──────────────────────────────────────────────

    /** Load persisted custom recipes from disk. Call on server start. */
    public static void load() {
        CUSTOM_RECIPES.clear();
        if (Files.exists(CONFIG_FILE)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_FILE)) {
                JsonArray arr = GSON.fromJson(reader, JsonArray.class);
                if (arr != null) {
                    for (var elem : arr) {
                        SlayerRecipes.Recipe recipe = deserialize(elem.getAsJsonObject());
                        if (recipe != null) CUSTOM_RECIPES.add(recipe);
                    }
                }
            } catch (Exception e) {
                System.err.println("[RecipeConfigManager] Failed to load custom recipes: " + e.getMessage());
            }
        }
        loadDisabled();
    }

    /** Save current custom recipes to disk. */
    public static void save() {
        try {
            Files.createDirectories(CONFIG_DIR);
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
     * Returns all registered crafting recipes — only custom recipes from /setrecipe.
     * Hardcoded SlayerRecipes are no longer used to avoid overriding /setrecipe.
     */
    public static List<SlayerRecipes.Recipe> getRecipes() {
        return new ArrayList<>(CUSTOM_RECIPES);
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

    private static SlayerRecipes.Recipe deserialize(JsonObject obj) {
        try {
            String name = obj.get("name").getAsString();
            String slayerStr = obj.get("requiredSlayer").getAsString();
            int level = obj.get("requiredLevel").getAsInt();
            ItemStack result = deserializeStack(obj.get("result"));

            SlayerManager.SlayerType slayerType = null;
            if (!slayerStr.isEmpty()) {
                try { slayerType = SlayerManager.SlayerType.valueOf(slayerStr); } catch (Exception ignored) {}
            }
            if (result.isEmpty()) return null;

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

        if (obj.has("nbt") && PoliticalServer.server != null) {
            try {
                String nbtString = obj.get("nbt").getAsString();
                if (nbtString != null && !nbtString.isEmpty()) {
                    NbtCompound parsed = StringNbtReader.readCompound(nbtString);
                    RegistryOps<net.minecraft.nbt.NbtElement> ops = PoliticalServer.server.getRegistryManager().getOps(NbtOps.INSTANCE);
                    var result = ItemStack.CODEC.parse(ops, parsed);
                    ItemStack stack = result.result().orElse(ItemStack.EMPTY);
                    int count = obj.has("count") ? obj.get("count").getAsInt() : stack.getCount();
                    if (!stack.isEmpty() && count > 1) stack.setCount(count);
                    return stack;
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
    
    private static String snakeToPascal(String snake) {
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;
        for (char c : snake.toCharArray()) {
            if (c == '_') {
                capitalizeNext = true;
            } else {
                result.append(capitalizeNext ? Character.toUpperCase(c) : c);
                capitalizeNext = false;
            }
        }
        return result.toString();
    }
}
