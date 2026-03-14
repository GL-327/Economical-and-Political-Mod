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

        // Almond Water Flask
        RECIPES.add(new Recipe(
                "Almond Water Flask",
                com.political.backrooms.items.BackroomsItemsManager.getItem("almond_water_flask"),
                new ItemStack[] {
                        new ItemStack(Items.POTION), new ItemStack(Items.SUGAR), new ItemStack(Items.WHEAT),
                        ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY,
                        ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
                },
                null, 0
        ));

        // Wallpaper Wrap Helmet
        RECIPES.add(new Recipe(
                "Wallpaper Wrap Hood",
                com.political.backrooms.items.BackroomsItemsManager.getItem("wallpaper_helmet"),
                new ItemStack[] {
                        com.political.backrooms.items.BackroomsItemsManager.getItem("wallpaper_fibre"), com.political.backrooms.items.BackroomsItemsManager.getItem("wallpaper_fibre"), com.political.backrooms.items.BackroomsItemsManager.getItem("wallpaper_fibre"),
                        com.political.backrooms.items.BackroomsItemsManager.getItem("wallpaper_fibre"), ItemStack.EMPTY, com.political.backrooms.items.BackroomsItemsManager.getItem("wallpaper_fibre"),
                        ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
                },
                null, 0
        ));

        // Wallpaper Wrap Chestplate
        RECIPES.add(new Recipe(
                "Wallpaper Wrap Cloak",
                com.political.backrooms.items.BackroomsItemsManager.getItem("wallpaper_chestplate"),
                new ItemStack[] {
                        com.political.backrooms.items.BackroomsItemsManager.getItem("wallpaper_fibre"), ItemStack.EMPTY, com.political.backrooms.items.BackroomsItemsManager.getItem("wallpaper_fibre"),
                        com.political.backrooms.items.BackroomsItemsManager.getItem("wallpaper_fibre"), com.political.backrooms.items.BackroomsItemsManager.getItem("wallpaper_fibre"), com.political.backrooms.items.BackroomsItemsManager.getItem("wallpaper_fibre"),
                        com.political.backrooms.items.BackroomsItemsManager.getItem("wallpaper_fibre"), com.political.backrooms.items.BackroomsItemsManager.getItem("wallpaper_fibre"), com.political.backrooms.items.BackroomsItemsManager.getItem("wallpaper_fibre")
                },
                null, 0
        ));

        // Static Shell Chestplate
        RECIPES.add(new Recipe(
                "Static Shell Chestplate",
                com.political.backrooms.items.BackroomsItemsManager.getItem("static_chestplate"),
                new ItemStack[] {
                        com.political.backrooms.items.BackroomsItemsManager.getItem("static_residue"), ItemStack.EMPTY, com.political.backrooms.items.BackroomsItemsManager.getItem("static_residue"),
                        com.political.backrooms.items.BackroomsItemsManager.getItem("static_residue"), com.political.backrooms.items.BackroomsItemsManager.getItem("static_residue"), com.political.backrooms.items.BackroomsItemsManager.getItem("static_residue"),
                        com.political.backrooms.items.BackroomsItemsManager.getItem("static_residue"), com.political.backrooms.items.BackroomsItemsManager.getItem("static_residue"), com.political.backrooms.items.BackroomsItemsManager.getItem("static_residue")
                },
                null, 0
        ));

        // Void Plate Chestplate
        RECIPES.add(new Recipe(
                "Void Plate Chestplate",
                com.political.backrooms.items.BackroomsItemsManager.getItem("void_chestplate"),
                new ItemStack[] {
                        com.political.backrooms.items.BackroomsItemsManager.getItem("void_shard"), ItemStack.EMPTY, com.political.backrooms.items.BackroomsItemsManager.getItem("void_shard"),
                        com.political.backrooms.items.BackroomsItemsManager.getItem("void_shard"), com.political.backrooms.items.BackroomsItemsManager.getItem("void_shard"), com.political.backrooms.items.BackroomsItemsManager.getItem("void_shard"),
                        com.political.backrooms.items.BackroomsItemsManager.getItem("void_shard"), com.political.backrooms.items.BackroomsItemsManager.getItem("void_shard"), com.political.backrooms.items.BackroomsItemsManager.getItem("void_shard")
                },
                null, 0
        ));

        // Hound Leather Chestplate
        RECIPES.add(new Recipe(
                "Hound Leather Chestplate",
                com.political.backrooms.items.BackroomsItemsManager.getItem("hound_chestplate"),
                new ItemStack[] {
                        com.political.backrooms.items.BackroomsItemsManager.getItem("hound_pelt"), ItemStack.EMPTY, com.political.backrooms.items.BackroomsItemsManager.getItem("hound_pelt"),
                        com.political.backrooms.items.BackroomsItemsManager.getItem("hound_pelt"), com.political.backrooms.items.BackroomsItemsManager.getItem("hound_pelt"), com.political.backrooms.items.BackroomsItemsManager.getItem("hound_pelt"),
                        com.political.backrooms.items.BackroomsItemsManager.getItem("hound_pelt"), com.political.backrooms.items.BackroomsItemsManager.getItem("hound_pelt"), com.political.backrooms.items.BackroomsItemsManager.getItem("hound_pelt")
                },
                null, 0
        ));

        // Corridor Blade
        RECIPES.add(new Recipe(
                "Corridor Blade",
                com.political.backrooms.items.BackroomsItemsManager.getItem("corridor_blade"),
                new ItemStack[] {
                        ItemStack.EMPTY, com.political.backrooms.items.BackroomsItemsManager.getItem("wallpaper_fibre"), ItemStack.EMPTY,
                        ItemStack.EMPTY, com.political.backrooms.items.BackroomsItemsManager.getItem("wallpaper_fibre"), ItemStack.EMPTY,
                        ItemStack.EMPTY, new ItemStack(Items.STICK), ItemStack.EMPTY
                },
                null, 0
        ));

        // Static Lance
        RECIPES.add(new Recipe(
                "Static Lance",
                com.political.backrooms.items.BackroomsItemsManager.getItem("static_lance"),
                new ItemStack[] {
                        ItemStack.EMPTY, com.political.backrooms.items.BackroomsItemsManager.getItem("static_residue"), ItemStack.EMPTY,
                        ItemStack.EMPTY, com.political.backrooms.items.BackroomsItemsManager.getItem("static_residue"), ItemStack.EMPTY,
                        ItemStack.EMPTY, new ItemStack(Items.BLAZE_ROD), ItemStack.EMPTY
                },
                null, 0
        ));

        // Void Drill
        RECIPES.add(new Recipe(
                "Void Drill",
                com.political.backrooms.items.BackroomsItemsManager.getItem("void_drill"),
                new ItemStack[] {
                        com.political.backrooms.items.BackroomsItemsManager.getItem("void_shard"), com.political.backrooms.items.BackroomsItemsManager.getItem("void_shard"), com.political.backrooms.items.BackroomsItemsManager.getItem("void_shard"),
                        ItemStack.EMPTY, new ItemStack(Items.STICK), ItemStack.EMPTY,
                        ItemStack.EMPTY, new ItemStack(Items.STICK), ItemStack.EMPTY
                },
                null, 0
        ));

        // Corridor Lantern
        RECIPES.add(new Recipe(
                "Corridor Lantern",
                com.political.backrooms.items.BackroomsItemsManager.getItem("corridor_lantern"),
                new ItemStack[] {
                        new ItemStack(Items.IRON_INGOT), new ItemStack(Items.IRON_INGOT), new ItemStack(Items.IRON_INGOT),
                        new ItemStack(Items.IRON_INGOT), com.political.backrooms.items.BackroomsItemsManager.getItem("fluorescent_dust"), new ItemStack(Items.IRON_INGOT),
                        new ItemStack(Items.IRON_INGOT), new ItemStack(Items.IRON_INGOT), new ItemStack(Items.IRON_INGOT)
                },
                null, 0
        ));

        // Static Shell Helmet
        RECIPES.add(new Recipe(
                "Static Shell Helmet",
                com.political.backrooms.items.BackroomsItemsManager.getItem("static_helmet"),
                new ItemStack[] {
                        com.political.backrooms.items.BackroomsItemsManager.getItem("static_residue"), com.political.backrooms.items.BackroomsItemsManager.getItem("static_residue"), com.political.backrooms.items.BackroomsItemsManager.getItem("static_residue"),
                        com.political.backrooms.items.BackroomsItemsManager.getItem("static_residue"), ItemStack.EMPTY, com.political.backrooms.items.BackroomsItemsManager.getItem("static_residue"),
                        ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
                },
                null, 0
        ));

        // Void Plate Helmet
        RECIPES.add(new Recipe(
                "Void Plate Helmet",
                com.political.backrooms.items.BackroomsItemsManager.getItem("void_helmet"),
                new ItemStack[] {
                        com.political.backrooms.items.BackroomsItemsManager.getItem("void_shard"), com.political.backrooms.items.BackroomsItemsManager.getItem("void_shard"), com.political.backrooms.items.BackroomsItemsManager.getItem("void_shard"),
                        com.political.backrooms.items.BackroomsItemsManager.getItem("void_shard"), ItemStack.EMPTY, com.political.backrooms.items.BackroomsItemsManager.getItem("void_shard"),
                        ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
                },
                null, 0
        ));

        // Hound Leather Helmet
        RECIPES.add(new Recipe(
                "Hound Leather Helmet",
                com.political.backrooms.items.BackroomsItemsManager.getItem("hound_helmet"),
                new ItemStack[] {
                        com.political.backrooms.items.BackroomsItemsManager.getItem("hound_pelt"), com.political.backrooms.items.BackroomsItemsManager.getItem("hound_pelt"), com.political.backrooms.items.BackroomsItemsManager.getItem("hound_pelt"),
                        com.political.backrooms.items.BackroomsItemsManager.getItem("hound_pelt"), ItemStack.EMPTY, com.political.backrooms.items.BackroomsItemsManager.getItem("hound_pelt"),
                        ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
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
