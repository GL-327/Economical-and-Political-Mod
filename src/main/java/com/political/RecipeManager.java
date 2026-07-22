package com.political;

import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Custom Recipe System
 * Create, manage, and view custom crafting recipes
 */
public class RecipeManager {

    // ════════════════════════════════════════════════════════════
    // RECIPE DEFINITION
    // ════════════════════════════════════════════════════════════

    public static class CustomRecipe {
        public final String id;
        public final String name;
        public final ItemStack result;
        public final String[] pattern; // 3 rows of 3 chars each
        public final Map<Character, Item> ingredients;
        public final boolean shapeless;
        public final String category;

        public CustomRecipe(String id, String name, ItemStack result, String[] pattern, 
                           Map<Character, Item> ingredients, boolean shapeless, String category) {
            this.id = id;
            this.name = name;
            this.result = result;
            this.pattern = pattern;
            this.ingredients = ingredients;
            this.shapeless = shapeless;
            this.category = category;
        }
    }

    // ════════════════════════════════════════════════════════════
    // STORAGE
    // ════════════════════════════════════════════════════════════

    private static final Map<String, CustomRecipe> customRecipes = new ConcurrentHashMap<>();
    private static Path dataPath;
    private static final String DATA_FILE = "custom_recipes.dat";

    // ════════════════════════════════════════════════════════════
    // INITIALIZATION
    // ════════════════════════════════════════════════════════════

    public static void initialize(Path savePath) {
        dataPath = savePath;
        loadDefaultRecipes();
        loadData();
    }

    private static void loadDefaultRecipes() {
        // Skill Tomes
        registerRecipe(new CustomRecipe("skill_tome_combat", "Combat Skill Tome",
            CustomItemManager.getItem("skill_tome_combat").createStack(1),
            new String[]{" B ", "BPB", " B "},
            Map.of('B', Items.BLAZE_ROD, 'P', Items.BOOK),
            false, "skill_items"));
        
        registerRecipe(new CustomRecipe("skill_tome_mining", "Mining Skill Tome",
            CustomItemManager.getItem("skill_tome_mining").createStack(1),
            new String[]{" I ", "IPI", " I "},
            Map.of('I', Items.IRON_PICKAXE, 'P', Items.BOOK),
            false, "skill_items"));
        
        registerRecipe(new CustomRecipe("skill_tome_farming", "Farming Skill Tome",
            CustomItemManager.getItem("skill_tome_farming").createStack(1),
            new String[]{" W ", "WPW", " W "},
            Map.of('W', Items.WHEAT, 'P', Items.BOOK),
            false, "skill_items"));
        
        // Coin Pouches
        registerRecipe(new CustomRecipe("coin_pouch_small", "Small Coin Pouch",
            CustomItemManager.getItem("coin_pouch_small").createStack(1),
            new String[]{" G ", "GPG", " G "},
            Map.of('G', Items.GOLD_NUGGET, 'P', Items.PAPER),
            false, "coins"));
        
        registerRecipe(new CustomRecipe("coin_pouch_medium", "Medium Coin Pouch",
            CustomItemManager.getItem("coin_pouch_medium").createStack(1),
            new String[]{"GGG", "GPG", "GGG"},
            Map.of('G', Items.GOLD_INGOT, 'P', Items.PAPER),
            false, "coins"));
        
        registerRecipe(new CustomRecipe("coin_pouch_large", "Large Coin Pouch",
            CustomItemManager.getItem("coin_pouch_large").createStack(1),
            new String[]{"GBG", "BPG", "GBG"},
            Map.of('G', Items.GOLD_BLOCK, 'B', Items.DIAMOND, 'P', Items.PAPER),
            false, "coins"));
        
        // Class Change Token
        registerRecipe(new CustomRecipe("class_change_token", "Class Change Token",
            CustomItemManager.getItem("class_change_token").createStack(1),
            new String[]{"NEN", "ETE", "NEN"},
            Map.of('N', Items.NETHER_STAR, 'E', Items.ENDER_EYE, 'T', Items.NAME_TAG),
            false, "class_items"));
        
        // Hunt Skip Token
        registerRecipe(new CustomRecipe("hunt_skip_token", "Hunt Skip Token",
            CustomItemManager.getItem("hunt_skip_token").createStack(4),
            new String[]{" R ", "RPR", " R "},
            Map.of('R', Items.RABBIT_HIDE, 'P', Items.PAPER),
            false, "hunt_items"));
        
        // Additional Skill Tomes
        registerRecipe(new CustomRecipe("skill_tome_agility", "Agility Skill Tome",
            CustomItemManager.getItem("skill_tome_agility").createStack(1),
            new String[]{" F ", "FPF", " F "},
            Map.of('F', Items.FEATHER, 'P', Items.BOOK),
            false, "skill_items"));
        
        registerRecipe(new CustomRecipe("skill_tome_defense", "Defense Skill Tome",
            CustomItemManager.getItem("skill_tome_defense").createStack(1),
            new String[]{" I ", "IPI", " I "},
            Map.of('I', Items.SHIELD, 'P', Items.BOOK),
            false, "skill_items"));
        
    }

    // ════════════════════════════════════════════════════════════
    // RECIPE MANAGEMENT
    // ════════════════════════════════════════════════════════════

    public static void registerRecipe(CustomRecipe recipe) {
        customRecipes.put(recipe.id.toLowerCase(), recipe);
        saveData();
    }

    public static CustomRecipe getRecipe(String id) {
        return customRecipes.get(id.toLowerCase());
    }

    public static Collection<CustomRecipe> getAllRecipes() {
        return customRecipes.values();
    }

    public static List<CustomRecipe> getRecipesByCategory(String category) {
        List<CustomRecipe> result = new ArrayList<>();
        for (CustomRecipe recipe : customRecipes.values()) {
            if (recipe.category.equalsIgnoreCase(category)) {
                result.add(recipe);
            }
        }
        return result;
    }

    public static boolean deleteRecipe(String id) {
        if (customRecipes.remove(id.toLowerCase()) != null) {
            saveData();
            return true;
        }
        return false;
    }

    // ════════════════════════════════════════════════════════════
    // RECIPE CREATION
    // ════════════════════════════════════════════════════════════

    public static boolean createRecipe(String id, String name, ItemStack result, 
                                       String[] pattern, Map<Character, Item> ingredients, 
                                       boolean shapeless, String category) {
        // Validate pattern
        if (!shapeless) {
            if (pattern == null || pattern.length != 3) return false;
            for (String row : pattern) {
                if (row == null || row.length() != 3) return false;
            }
        }
        
        if (ingredients == null || ingredients.isEmpty()) return false;
        
        CustomRecipe recipe = new CustomRecipe(id.toLowerCase(), name, result, pattern, ingredients, shapeless, category);
        registerRecipe(recipe);
        return true;
    }

    // ════════════════════════════════════════════════════════════
    // RECIPE VIEWING
    // ════════════════════════════════════════════════════════════

    public static void displayRecipe(ServerPlayer player, String id) {
        CustomRecipe recipe = getRecipe(id);
        if (recipe == null) {
            player.sendSystemMessage(Component.literal("§cRecipe not found: " + id), false);
            return;
        }
        
        player.sendSystemMessage(Component.literal("═══════════════════════════════════")
            .withStyle(ChatFormatting.GOLD), false);
        player.sendSystemMessage(Component.literal("       " + recipe.name)
            .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
        player.sendSystemMessage(Component.literal("═══════════════════════════════════")
            .withStyle(ChatFormatting.GOLD), false);
        
        player.sendSystemMessage(Component.literal("§7ID: §f" + recipe.id), false);
        player.sendSystemMessage(Component.literal("§7Category: §f" + recipe.category), false);
        player.sendSystemMessage(Component.literal("§7Type: " + (recipe.shapeless ? "§bShapeless" : "§eShaped")), false);
        player.sendSystemMessage(Component.literal(""), false);
        
        if (!recipe.shapeless && recipe.pattern != null) {
            player.sendSystemMessage(Component.literal("§6Pattern:"), false);
            for (String row : recipe.pattern) {
                player.sendSystemMessage(Component.literal("§f  " + row.replace(" ", "§7·§f")), false);
            }
            player.sendSystemMessage(Component.literal(""), false);
        }
        
        player.sendSystemMessage(Component.literal("§6Ingredients:"), false);
        for (Map.Entry<Character, Item> entry : recipe.ingredients.entrySet()) {
            String itemName = ItemRegistryIds.asId(entry.getValue()).getPath().replace("_", " ");
            player.sendSystemMessage(Component.literal("  §e" + entry.getKey() + " §7= §f" + itemName), false);
        }
        player.sendSystemMessage(Component.literal(""), false);
        
        String resultName = recipe.result.getHoverName().getString();
        player.sendSystemMessage(Component.literal("§aResult: §f" + resultName + " §7x" + recipe.result.getCount()), false);
    }

    public static void listRecipes(ServerPlayer player, String category, int page) {
        List<CustomRecipe> recipes = category != null ? getRecipesByCategory(category) : new ArrayList<>(customRecipes.values());
        
        int itemsPerPage = 10;
        int totalPages = (int) Math.ceil(recipes.size() / (double) itemsPerPage);
        page = Math.max(1, Math.min(page, totalPages));
        
        String title = category != null ? category.toUpperCase() + " RECIPES" : "ALL CUSTOM RECIPES";
        
        player.sendSystemMessage(Component.literal("═══════════════════════════════════")
            .withStyle(ChatFormatting.GOLD), false);
        player.sendSystemMessage(Component.literal("       " + title + " (Page " + page + "/" + totalPages + ")")
            .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
        player.sendSystemMessage(Component.literal("═══════════════════════════════════")
            .withStyle(ChatFormatting.GOLD), false);
        
        int start = (page - 1) * itemsPerPage;
        int end = Math.min(start + itemsPerPage, recipes.size());
        
        for (int i = start; i < end; i++) {
            CustomRecipe recipe = recipes.get(i);
            player.sendSystemMessage(Component.literal("§7" + recipe.id + " §f- §e" + recipe.name), false);
        }
        
        player.sendSystemMessage(Component.literal("§7Use /recipe list <page> or /recipe list <category> <page>"), false);
        player.sendSystemMessage(Component.literal("§7Categories: skill_items, coins, class_items, hunt_items"), false);
    }

    // ════════════════════════════════════════════════════════════
    // DATA PERSISTENCE
    // ════════════════════════════════════════════════════════════

    private static void loadData() {
        if (dataPath == null) return;
        
        File file = dataPath.resolve(DATA_FILE).toFile();
        if (!file.exists()) return;

        try (DataInputStream dis = new DataInputStream(new FileInputStream(file))) {
            int count = dis.readInt();
            for (int i = 0; i < count; i++) {
                String id = dis.readUTF();
                String name = dis.readUTF();
                
                // Load result
                String resultItemId = dis.readUTF();
                int resultCount = dis.readInt();
                Item resultItem = BuiltInRegistries.ITEM.getValue(Identifier.tryParse(resultItemId));
                ItemStack result = new ItemStack(resultItem, resultCount);
                
                // Load pattern
                boolean shapeless = dis.readBoolean();
                String[] pattern = null;
                if (!shapeless) {
                    pattern = new String[3];
                    for (int j = 0; j < 3; j++) {
                        pattern[j] = dis.readUTF();
                    }
                }
                
                // Load ingredients
                int ingCount = dis.readInt();
                Map<Character, Item> ingredients = new HashMap<>();
                for (int j = 0; j < ingCount; j++) {
                    char c = dis.readChar();
                    String itemId = dis.readUTF();
                    ingredients.put(c, BuiltInRegistries.ITEM.getValue(Identifier.tryParse(itemId)));
                }
                
                String category = dis.readUTF();
                
                customRecipes.put(id.toLowerCase(), new CustomRecipe(id, name, result, pattern, ingredients, shapeless, category));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveData() {
        if (dataPath == null) return;
        
        File file = dataPath.resolve(DATA_FILE).toFile();
        file.getParentFile().mkdirs();

        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(file))) {
            dos.writeInt(customRecipes.size());
            
            for (CustomRecipe recipe : customRecipes.values()) {
                dos.writeUTF(recipe.id);
                dos.writeUTF(recipe.name);
                
                // Save result
                dos.writeUTF(ItemRegistryIds.asString(recipe.result.getItem()));
                dos.writeInt(recipe.result.getCount());
                
                // Save pattern
                dos.writeBoolean(recipe.shapeless);
                if (!recipe.shapeless && recipe.pattern != null) {
                    for (String row : recipe.pattern) {
                        dos.writeUTF(row);
                    }
                }
                
                // Save ingredients
                dos.writeInt(recipe.ingredients.size());
                for (Map.Entry<Character, Item> entry : recipe.ingredients.entrySet()) {
                    dos.writeChar(entry.getKey());
                    dos.writeUTF(ItemRegistryIds.asString(entry.getValue()));
                }
                
                dos.writeUTF(recipe.category);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
