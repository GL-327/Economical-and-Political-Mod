package com.political;

import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class SlayerRecipes {

    public static class Recipe {
        public final String name;
        public final ItemStack result;
        public final ItemStack[] ingredients; // 9 slots (3x3 grid)
        public final SlayerManager.SlayerType requiredSlayer;
        public final int requiredLevel;

        public Recipe(String name, ItemStack result, ItemStack[] ingredients,
                      SlayerManager.SlayerType requiredSlayer, int requiredLevel) {
            this.name = name;
            this.result = result;
            this.ingredients = ingredients;
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

    private static final List<Recipe> RECIPES = new ArrayList<>();

    public static void registerRecipes() {
        // ===== ZOMBIE SLAYER RECIPES =====

        // Zombie Cleaver (Sword)
        RECIPES.add(new Recipe(
                "Zombie Cleaver",
                SlayerItems.createZombieSword(),
                new ItemStack[] {
                        ItemStack.EMPTY, SlayerItems.createChunk(SlayerManager.SlayerType.ZOMBIE), ItemStack.EMPTY,
                        ItemStack.EMPTY, SlayerItems.createChunk(SlayerManager.SlayerType.ZOMBIE), ItemStack.EMPTY,
                        ItemStack.EMPTY, new ItemStack(Items.STICK), ItemStack.EMPTY
                },
                SlayerManager.SlayerType.ZOMBIE, 3
        ));

        // Berserker Helmet
        RECIPES.add(new Recipe(
                "Berserker Helmet",
                SlayerItems.createZombieHelmet(),
                new ItemStack[] {
                        SlayerItems.createChunk(SlayerManager.SlayerType.ZOMBIE),
                        SlayerItems.createChunk(SlayerManager.SlayerType.ZOMBIE),
                        SlayerItems.createChunk(SlayerManager.SlayerType.ZOMBIE),
                        SlayerItems.createChunk(SlayerManager.SlayerType.ZOMBIE),
                        new ItemStack(Items.LEATHER_HELMET),
                        SlayerItems.createChunk(SlayerManager.SlayerType.ZOMBIE),
                        ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
                },
                SlayerManager.SlayerType.ZOMBIE, 5
        ));

        // ===== SPIDER SLAYER RECIPES =====

        // Spider Fang (Sword)
        RECIPES.add(new Recipe(
                "Spider Fang",
                SlayerItems.createSpiderSword(),
                new ItemStack[] {
                        ItemStack.EMPTY, SlayerItems.createChunk(SlayerManager.SlayerType.SPIDER), ItemStack.EMPTY,
                        ItemStack.EMPTY, SlayerItems.createChunk(SlayerManager.SlayerType.SPIDER), ItemStack.EMPTY,
                        ItemStack.EMPTY, new ItemStack(Items.STICK), ItemStack.EMPTY
                },
                SlayerManager.SlayerType.SPIDER, 3
        ));

        // Venomous Leggings
        RECIPES.add(new Recipe(
                "Venomous Leggings",
                SlayerItems.createSpiderLeggings(),
                new ItemStack[] {
                        SlayerItems.createChunk(SlayerManager.SlayerType.SPIDER),
                        SlayerItems.createChunk(SlayerManager.SlayerType.SPIDER),
                        SlayerItems.createChunk(SlayerManager.SlayerType.SPIDER),
                        SlayerItems.createChunk(SlayerManager.SlayerType.SPIDER),
                        new ItemStack(Items.LEATHER_LEGGINGS),
                        SlayerItems.createChunk(SlayerManager.SlayerType.SPIDER),
                        SlayerItems.createChunk(SlayerManager.SlayerType.SPIDER),
                        ItemStack.EMPTY,
                        SlayerItems.createChunk(SlayerManager.SlayerType.SPIDER)
                },
                SlayerManager.SlayerType.SPIDER, 5
        ));

        // ===== SKELETON SLAYER RECIPES =====

        // Bone Blade
        RECIPES.add(new Recipe(
                "Bone Blade",
                SlayerItems.createSkeletonSword(),
                new ItemStack[] {
                        ItemStack.EMPTY, SlayerItems.createChunk(SlayerManager.SlayerType.SKELETON), ItemStack.EMPTY,
                        ItemStack.EMPTY, SlayerItems.createChunk(SlayerManager.SlayerType.SKELETON), ItemStack.EMPTY,
                        ItemStack.EMPTY, new ItemStack(Items.BONE), ItemStack.EMPTY
                },
                SlayerManager.SlayerType.SKELETON, 3
        ));

        // Skeleton Bow
        RECIPES.add(new Recipe(
                "Bone Bow",
                SlayerItems.createSkeletonBow(),
                new ItemStack[] {
                        ItemStack.EMPTY, new ItemStack(Items.BONE), new ItemStack(Items.STRING),
                        SlayerItems.createChunk(SlayerManager.SlayerType.SKELETON), ItemStack.EMPTY, new ItemStack(Items.STRING),
                        ItemStack.EMPTY, new ItemStack(Items.BONE), new ItemStack(Items.STRING)
                },
                SlayerManager.SlayerType.SKELETON, 4
        ));

        // ===== SLIME SLAYER RECIPES =====

        // Slime Sword
        RECIPES.add(new Recipe(
                "Gelatinous Blade",
                SlayerItems.createSlimeSword(),
                new ItemStack[] {
                        ItemStack.EMPTY, SlayerItems.createChunk(SlayerManager.SlayerType.SLIME), ItemStack.EMPTY,
                        ItemStack.EMPTY, SlayerItems.createChunk(SlayerManager.SlayerType.SLIME), ItemStack.EMPTY,
                        ItemStack.EMPTY, new ItemStack(Items.SLIME_BALL), ItemStack.EMPTY
                },
                SlayerManager.SlayerType.SLIME, 3
        ));

        // Slime Boots
        RECIPES.add(new Recipe(
                "Slime Boots",
                SlayerItems.createSlimeBoots(),
                new ItemStack[] {
                        ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY,
                        SlayerItems.createChunk(SlayerManager.SlayerType.SLIME),
                        new ItemStack(Items.LEATHER_BOOTS),
                        SlayerItems.createChunk(SlayerManager.SlayerType.SLIME),
                        SlayerItems.createChunk(SlayerManager.SlayerType.SLIME),
                        ItemStack.EMPTY,
                        SlayerItems.createChunk(SlayerManager.SlayerType.SLIME)
                },
                SlayerManager.SlayerType.SLIME, 5
        ));

        // ===== ENDERMAN SLAYER RECIPES =====

        // Void Blade
        RECIPES.add(new Recipe(
                "Void Blade",
                SlayerItems.createEndermanSword(),
                new ItemStack[] {
                        ItemStack.EMPTY, SlayerItems.createChunk(SlayerManager.SlayerType.ENDERMAN), ItemStack.EMPTY,
                        ItemStack.EMPTY, SlayerItems.createChunk(SlayerManager.SlayerType.ENDERMAN), ItemStack.EMPTY,
                        ItemStack.EMPTY, new ItemStack(Items.BLAZE_ROD), ItemStack.EMPTY
                },
                SlayerManager.SlayerType.ENDERMAN, 3
        ));

        // ===== WARDEN SLAYER RECIPES =====

        // The Gavel
        RECIPES.add(new Recipe(
                "The Gavel",
                SlayerItems.createTheGavel(),
                new ItemStack[] {
                        SlayerItems.createCore(SlayerManager.SlayerType.WARDEN),
                        SlayerItems.createChunk(SlayerManager.SlayerType.WARDEN),
                        SlayerItems.createCore(SlayerManager.SlayerType.WARDEN),
                        ItemStack.EMPTY, new ItemStack(Items.NETHERITE_INGOT), ItemStack.EMPTY,
                        ItemStack.EMPTY, new ItemStack(Items.NETHERITE_INGOT), ItemStack.EMPTY
                },
                SlayerManager.SlayerType.WARDEN, 7
        ));

        // Warden Chestplate
        RECIPES.add(new Recipe(
                "Sculk Warden Chestplate",
                SlayerItems.createWardenChestplate(),
                new ItemStack[] {
                        SlayerItems.createChunk(SlayerManager.SlayerType.WARDEN),
                        SlayerItems.createCore(SlayerManager.SlayerType.WARDEN),
                        SlayerItems.createChunk(SlayerManager.SlayerType.WARDEN),
                        SlayerItems.createChunk(SlayerManager.SlayerType.WARDEN),
                        new ItemStack(Items.NETHERITE_CHESTPLATE),
                        SlayerItems.createChunk(SlayerManager.SlayerType.WARDEN),
                        SlayerItems.createChunk(SlayerManager.SlayerType.WARDEN),
                        SlayerItems.createChunk(SlayerManager.SlayerType.WARDEN),
                        SlayerItems.createChunk(SlayerManager.SlayerType.WARDEN)
                },
                SlayerManager.SlayerType.WARDEN, 9
        ));
        
        // ===== BACKROOMS RECIPES =====
        
        // Almond Water
        RECIPES.add(new Recipe(
                "Almond Water",
                com.political.backrooms.items.BackroomsItemsManager.getItem("almond_water"),
                new ItemStack[] {
                        new ItemStack(Items.WATER_BUCKET), new ItemStack(Items.SUGAR), new ItemStack(Items.GLASS_BOTTLE),
                        new ItemStack(Items.AZURE_BLUET), new ItemStack(Items.WHEAT), new ItemStack(Items.GLASS_BOTTLE),
                        ItemStack.EMPTY, new ItemStack(Items.SUGAR), ItemStack.EMPTY
                },
                null, 0
        ));
        
        // Energy Drink
        RECIPES.add(new Recipe(
                "Energy Drink",
                com.political.backrooms.items.BackroomsItemsManager.getItem("energy_drink"),
                new ItemStack[] {
                        new ItemStack(Items.SUGAR), new ItemStack(Items.REDSTONE), new ItemStack(Items.GLASS_BOTTLE),
                        new ItemStack(Items.GLOWSTONE_DUST), new ItemStack(Items.SUGAR), new ItemStack(Items.GLASS_BOTTLE),
                        ItemStack.EMPTY, new ItemStack(Items.REDSTONE), ItemStack.EMPTY
                },
                null, 0
        ));
        
        // Crawler Hide to Leather
        RECIPES.add(new Recipe(
                "Crawler Hide Processing",
                new ItemStack(Items.LEATHER, 3),
                new ItemStack[] {
                        com.political.backrooms.items.BackroomsItemsManager.getItem("crawler_hide"), new ItemStack(Items.WATER_BUCKET), ItemStack.EMPTY,
                        ItemStack.EMPTY, new ItemStack(Items.FLINT_AND_STEEL), ItemStack.EMPTY,
                        ItemStack.EMPTY, new ItemStack(Items.AIR), ItemStack.EMPTY
                },
                null, 0
        ));
        
        // Backrooms Iodine Block
        RECIPES.add(new Recipe(
                "Backrooms Iodine Block",
                new ItemStack(Items.AMETHYST_BLOCK),
                new ItemStack[] {
                        com.political.backrooms.items.BackroomsItemsManager.getItem("backrooms_iodine"), com.political.backrooms.items.BackroomsItemsManager.getItem("backrooms_iodine"), com.political.backrooms.items.BackroomsItemsManager.getItem("backrooms_iodine"),
                        com.political.backrooms.items.BackroomsItemsManager.getItem("backrooms_iodine"), com.political.backrooms.items.BackroomsItemsManager.getItem("backrooms_iodine"), com.political.backrooms.items.BackroomsItemsManager.getItem("backrooms_iodine"),
                        com.political.backrooms.items.BackroomsItemsManager.getItem("backrooms_iodine"), com.political.backrooms.items.BackroomsItemsManager.getItem("backrooms_iodine"), com.political.backrooms.items.BackroomsItemsManager.getItem("backrooms_iodine")
                },
                null, 0
        ));
        
        // Memory Fragment to Experience Bottle
        RECIPES.add(new Recipe(
                "Memory Extraction",
                new ItemStack(Items.EXPERIENCE_BOTTLE, 3),
                new ItemStack[] {
                        ItemStack.EMPTY, com.political.backrooms.items.BackroomsItemsManager.getItem("memory_fragment"), ItemStack.EMPTY,
                        new ItemStack(Items.GLASS_BOTTLE), new ItemStack(Items.GLOWSTONE_DUST), new ItemStack(Items.GLASS_BOTTLE),
                        ItemStack.EMPTY, new ItemStack(Items.DIAMOND), ItemStack.EMPTY
                },
                null, 0
        ));
        
        // Reality Anchor to Compass
        RECIPES.add(new Recipe(
                "Reality Anchor Calibration",
                new ItemStack(Items.COMPASS),
                new ItemStack[] {
                        ItemStack.EMPTY, com.political.backrooms.items.BackroomsItemsManager.getItem("reality_anchor"), ItemStack.EMPTY,
                        new ItemStack(Items.REDSTONE), new ItemStack(Items.IRON_INGOT), new ItemStack(Items.REDSTONE),
                        ItemStack.EMPTY, new ItemStack(Items.COPPER_INGOT), ItemStack.EMPTY
                },
                null, 0
        ));
    }

    public static List<Recipe> getRecipes() {
        return java.util.Collections.unmodifiableList(RECIPES);
    }

    public static List<Recipe> getAllRecipes() {
        return new ArrayList<>(RECIPES);
    }

    public static List<Recipe> getRecipesForSlayer(SlayerManager.SlayerType type) {
        List<Recipe> result = new ArrayList<>();
        for (Recipe recipe : RECIPES) {
            if (recipe.requiredSlayer == type) {
                result.add(recipe);
            }
        }
        return result;
    }

    public static boolean canPlayerCraft(String playerUuid, Recipe recipe) {
        int playerLevel = SlayerData.getSlayerLevel(playerUuid, recipe.requiredSlayer);
        return playerLevel >= recipe.requiredLevel;
    }
}
