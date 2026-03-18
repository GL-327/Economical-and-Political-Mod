package com.political;

import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.Item;
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
        public final boolean[] lockedSlots; // Which slots are locked (true = locked)
        public final SlayerManager.SlayerType requiredSlayer;
        public final int requiredLevel;

        public Recipe(String name, ItemStack result, ItemStack[] ingredients,
                      SlayerManager.SlayerType requiredSlayer, int requiredLevel) {
            this(name, result, ingredients, null, requiredSlayer, requiredLevel);
        }

        public Recipe(String name, ItemStack result, ItemStack[] ingredients, boolean[] lockedSlots,
                      SlayerManager.SlayerType requiredSlayer, int requiredLevel) {
            this.name = name;
            this.result = result;
            this.ingredients = ingredients;
            this.lockedSlots = lockedSlots != null ? lockedSlots.clone() : new boolean[9];
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

        // ===== IRON_GOLEM SLAYER RECIPES =====

        // The Gavel
        RECIPES.add(new Recipe(
                "The Gavel",
                SlayerItems.createTheGavel(),
                new ItemStack[] {
                        SlayerItems.createCore(SlayerManager.SlayerType.IRON_GOLEM),
                        SlayerItems.createChunk(SlayerManager.SlayerType.IRON_GOLEM),
                        SlayerItems.createCore(SlayerManager.SlayerType.IRON_GOLEM),
                        ItemStack.EMPTY, new ItemStack(Items.NETHERITE_INGOT), ItemStack.EMPTY,
                        ItemStack.EMPTY, new ItemStack(Items.NETHERITE_INGOT), ItemStack.EMPTY
                },
                SlayerManager.SlayerType.IRON_GOLEM, 7
        ));

        // Warden Chestplate
        RECIPES.add(new Recipe(
                "Sculk Warden Chestplate",
                SlayerItems.createWardenChestplate(),
                new ItemStack[] {
                        SlayerItems.createChunk(SlayerManager.SlayerType.IRON_GOLEM),
                        SlayerItems.createCore(SlayerManager.SlayerType.IRON_GOLEM),
                        SlayerItems.createChunk(SlayerManager.SlayerType.IRON_GOLEM),
                        SlayerItems.createChunk(SlayerManager.SlayerType.IRON_GOLEM),
                        new ItemStack(Items.NETHERITE_CHESTPLATE),
                        SlayerItems.createChunk(SlayerManager.SlayerType.IRON_GOLEM),
                        SlayerItems.createChunk(SlayerManager.SlayerType.IRON_GOLEM),
                        SlayerItems.createChunk(SlayerManager.SlayerType.IRON_GOLEM),
                        SlayerItems.createChunk(SlayerManager.SlayerType.IRON_GOLEM)
                },
                SlayerManager.SlayerType.IRON_GOLEM, 9
        ));

        // ===== SPECIAL RECIPES =====
        
        // Midas Sword
        RECIPES.add(new Recipe(
                "Midas Sword",
                SlayerItems.createMidasSword(),
                new ItemStack[] {
                        SlayerItems.createChunk(SlayerManager.SlayerType.ENDERMAN),
                        SlayerItems.createChunk(SlayerManager.SlayerType.ENDERMAN),
                        SlayerItems.createChunk(SlayerManager.SlayerType.ENDERMAN),
                        SlayerItems.createChunk(SlayerManager.SlayerType.ENDERMAN),
                        SlayerItems.createChunk(SlayerManager.SlayerType.ENDERMAN),
                        new ItemStack(Items.GOLDEN_APPLE),
                        SlayerItems.createChunk(SlayerManager.SlayerType.ENDERMAN),
                        SlayerItems.createChunk(SlayerManager.SlayerType.ENDERMAN)
                },
                SlayerManager.SlayerType.ENDERMAN, 7
        ));

        // Voidwalker Crown
        RECIPES.add(new Recipe(
                "Voidwalker Crown",
                SlayerItems.createVoidwalkerCrown(),
                new ItemStack[] {
                        SlayerItems.createChunk(SlayerManager.SlayerType.ENDERMAN),
                        SlayerItems.createChunk(SlayerManager.SlayerType.ENDERMAN),
                        SlayerItems.createChunk(SlayerManager.SlayerType.ENDERMAN),
                        SlayerItems.createChunk(SlayerManager.SlayerType.ENDERMAN),
                        SlayerItems.createChunk(SlayerManager.SlayerType.ENDERMAN),
                        SlayerItems.createChunk(SlayerManager.SlayerType.ENDERMAN),
                        SlayerItems.createChunk(SlayerManager.SlayerType.ENDERMAN),
                        SlayerItems.createChunk(SlayerManager.SlayerType.ENDERMAN),
                        SlayerItems.createChunk(SlayerManager.SlayerType.ENDERMAN)
                },
                SlayerManager.SlayerType.ENDERMAN, 9
        ));
        
        // ===== GEM ARMOR RECIPES (No slayer requirement) =====
        
        // Lapis Helmet
        RECIPES.add(new Recipe(
                "Lapis Helmet",
                GemArmor.createLapisHelmet(),
                new ItemStack[] {
                        new ItemStack(Items.LAPIS_LAZULI), new ItemStack(Items.LAPIS_LAZULI), new ItemStack(Items.LAPIS_LAZULI),
                        new ItemStack(Items.LAPIS_LAZULI), new ItemStack(Items.LEATHER_HELMET), new ItemStack(Items.LAPIS_LAZULI),
                        ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
                },
                null, 0
        ));
        
        // Lapis Chestplate
        RECIPES.add(new Recipe(
                "Lapis Chestplate",
                GemArmor.createLapisChestplate(),
                new ItemStack[] {
                        new ItemStack(Items.LAPIS_LAZULI), new ItemStack(Items.LAPIS_LAZULI), new ItemStack(Items.LAPIS_LAZULI),
                        new ItemStack(Items.LAPIS_LAZULI), new ItemStack(Items.LEATHER_CHESTPLATE), new ItemStack(Items.LAPIS_LAZULI),
                        new ItemStack(Items.LAPIS_LAZULI), new ItemStack(Items.LAPIS_LAZULI), new ItemStack(Items.LAPIS_LAZULI)
                },
                null, 0
        ));
        
        // Lapis Leggings
        RECIPES.add(new Recipe(
                "Lapis Leggings",
                GemArmor.createLapisLeggings(),
                new ItemStack[] {
                        new ItemStack(Items.LAPIS_LAZULI), new ItemStack(Items.LAPIS_LAZULI), new ItemStack(Items.LAPIS_LAZULI),
                        new ItemStack(Items.LAPIS_LAZULI), new ItemStack(Items.LEATHER_LEGGINGS), new ItemStack(Items.LAPIS_LAZULI),
                        new ItemStack(Items.LAPIS_LAZULI), ItemStack.EMPTY, new ItemStack(Items.LAPIS_LAZULI)
                },
                null, 0
        ));
        
        // Lapis Boots
        RECIPES.add(new Recipe(
                "Lapis Boots",
                GemArmor.createLapisBoots(),
                new ItemStack[] {
                        ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY,
                        new ItemStack(Items.LAPIS_LAZULI), new ItemStack(Items.LEATHER_BOOTS), new ItemStack(Items.LAPIS_LAZULI),
                        new ItemStack(Items.LAPIS_LAZULI), ItemStack.EMPTY, new ItemStack(Items.LAPIS_LAZULI)
                },
                null, 0
        ));
        
        // Emerald Helmet
        RECIPES.add(new Recipe(
                "Emerald Helmet",
                GemArmor.createEmeraldHelmet(),
                new ItemStack[] {
                        new ItemStack(Items.EMERALD), new ItemStack(Items.EMERALD), new ItemStack(Items.EMERALD),
                        new ItemStack(Items.EMERALD), new ItemStack(Items.LEATHER_HELMET), new ItemStack(Items.EMERALD),
                        ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
                },
                null, 0
        ));
        
        // Emerald Chestplate
        RECIPES.add(new Recipe(
                "Emerald Chestplate",
                GemArmor.createEmeraldChestplate(),
                new ItemStack[] {
                        new ItemStack(Items.EMERALD), new ItemStack(Items.EMERALD), new ItemStack(Items.EMERALD),
                        new ItemStack(Items.EMERALD), new ItemStack(Items.LEATHER_CHESTPLATE), new ItemStack(Items.EMERALD),
                        new ItemStack(Items.EMERALD), new ItemStack(Items.EMERALD), new ItemStack(Items.EMERALD)
                },
                null, 0
        ));
        
        // Emerald Leggings
        RECIPES.add(new Recipe(
                "Emerald Leggings",
                GemArmor.createEmeraldLeggings(),
                new ItemStack[] {
                        new ItemStack(Items.EMERALD), new ItemStack(Items.EMERALD), new ItemStack(Items.EMERALD),
                        new ItemStack(Items.EMERALD), new ItemStack(Items.LEATHER_LEGGINGS), new ItemStack(Items.EMERALD),
                        new ItemStack(Items.EMERALD), ItemStack.EMPTY, new ItemStack(Items.EMERALD)
                },
                null, 0
        ));
        
        // Emerald Boots
        RECIPES.add(new Recipe(
                "Emerald Boots",
                GemArmor.createEmeraldBoots(),
                new ItemStack[] {
                        ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY,
                        new ItemStack(Items.EMERALD), new ItemStack(Items.LEATHER_BOOTS), new ItemStack(Items.EMERALD),
                        new ItemStack(Items.EMERALD), ItemStack.EMPTY, new ItemStack(Items.EMERALD)
                },
                null, 0
        ));
        
        // ===== BLOCK ARMOR RECIPES (No slayer requirement) =====
        
        // Glass Armor (all pieces)
        addBlockArmorRecipe("Glass Helmet", BlockArmor.createGlassHelmet(), Items.GLASS, Items.LEATHER_HELMET);
        addBlockArmorRecipe("Glass Chestplate", BlockArmor.createGlassChestplate(), Items.GLASS, Items.LEATHER_CHESTPLATE);
        addBlockArmorRecipe("Glass Leggings", BlockArmor.createGlassLeggings(), Items.GLASS, Items.LEATHER_LEGGINGS);
        addBlockArmorRecipe("Glass Boots", BlockArmor.createGlassBoots(), Items.GLASS, Items.LEATHER_BOOTS);
        
        // Obsidian Armor
        addBlockArmorRecipe("Obsidian Helmet", BlockArmor.createObsidianHelmet(), Items.OBSIDIAN, Items.LEATHER_HELMET);
        addBlockArmorRecipe("Obsidian Chestplate", BlockArmor.createObsidianChestplate(), Items.OBSIDIAN, Items.LEATHER_CHESTPLATE);
        addBlockArmorRecipe("Obsidian Leggings", BlockArmor.createObsidianLeggings(), Items.OBSIDIAN, Items.LEATHER_LEGGINGS);
        addBlockArmorRecipe("Obsidian Boots", BlockArmor.createObsidianBoots(), Items.OBSIDIAN, Items.LEATHER_BOOTS);
        
        // Quartz Armor
        addBlockArmorRecipe("Quartz Helmet", BlockArmor.createQuartzHelmet(), Items.QUARTZ_BLOCK, Items.LEATHER_HELMET);
        addBlockArmorRecipe("Quartz Chestplate", BlockArmor.createQuartzChestplate(), Items.QUARTZ_BLOCK, Items.LEATHER_CHESTPLATE);
        addBlockArmorRecipe("Quartz Leggings", BlockArmor.createQuartzLeggings(), Items.QUARTZ_BLOCK, Items.LEATHER_LEGGINGS);
        addBlockArmorRecipe("Quartz Boots", BlockArmor.createQuartzBoots(), Items.QUARTZ_BLOCK, Items.LEATHER_BOOTS);
        
        // Glowstone Armor
        addBlockArmorRecipe("Glowstone Helmet", BlockArmor.createGlowstoneHelmet(), Items.GLOWSTONE, Items.LEATHER_HELMET);
        addBlockArmorRecipe("Glowstone Chestplate", BlockArmor.createGlowstoneChestplate(), Items.GLOWSTONE, Items.LEATHER_CHESTPLATE);
        addBlockArmorRecipe("Glowstone Leggings", BlockArmor.createGlowstoneLeggings(), Items.GLOWSTONE, Items.LEATHER_LEGGINGS);
        addBlockArmorRecipe("Glowstone Boots", BlockArmor.createGlowstoneBoots(), Items.GLOWSTONE, Items.LEATHER_BOOTS);
        
        // Redstone Armor
        addBlockArmorRecipe("Redstone Helmet", BlockArmor.createRedstoneHelmet(), Items.REDSTONE_BLOCK, Items.LEATHER_HELMET);
        addBlockArmorRecipe("Redstone Chestplate", BlockArmor.createRedstoneChestplate(), Items.REDSTONE_BLOCK, Items.LEATHER_CHESTPLATE);
        addBlockArmorRecipe("Redstone Leggings", BlockArmor.createRedstoneLeggings(), Items.REDSTONE_BLOCK, Items.LEATHER_LEGGINGS);
        addBlockArmorRecipe("Redstone Boots", BlockArmor.createRedstoneBoots(), Items.REDSTONE_BLOCK, Items.LEATHER_BOOTS);
        
        // Netherrack Armor
        addBlockArmorRecipe("Netherrack Helmet", BlockArmor.createNetherrackHelmet(), Items.NETHERRACK, Items.LEATHER_HELMET);
        addBlockArmorRecipe("Netherrack Chestplate", BlockArmor.createNetherrackChestplate(), Items.NETHERRACK, Items.LEATHER_CHESTPLATE);
        addBlockArmorRecipe("Netherrack Leggings", BlockArmor.createNetherrackLeggings(), Items.NETHERRACK, Items.LEATHER_LEGGINGS);
        addBlockArmorRecipe("Netherrack Boots", BlockArmor.createNetherrackBoots(), Items.NETHERRACK, Items.LEATHER_BOOTS);
        
        // End Stone Armor
        addBlockArmorRecipe("End Stone Helmet", BlockArmor.createEndstoneHelmet(), Items.END_STONE, Items.LEATHER_HELMET);
        addBlockArmorRecipe("End Stone Chestplate", BlockArmor.createEndstoneChestplate(), Items.END_STONE, Items.LEATHER_CHESTPLATE);
        addBlockArmorRecipe("End Stone Leggings", BlockArmor.createEndstoneLeggings(), Items.END_STONE, Items.LEATHER_LEGGINGS);
        addBlockArmorRecipe("End Stone Boots", BlockArmor.createEndstoneBoots(), Items.END_STONE, Items.LEATHER_BOOTS);
        
        // Ice Armor
        addBlockArmorRecipe("Ice Helmet", BlockArmor.createIceHelmet(), Items.PACKED_ICE, Items.LEATHER_HELMET);
        addBlockArmorRecipe("Ice Chestplate", BlockArmor.createIceChestplate(), Items.PACKED_ICE, Items.LEATHER_CHESTPLATE);
        addBlockArmorRecipe("Ice Leggings", BlockArmor.createIceLeggings(), Items.PACKED_ICE, Items.LEATHER_LEGGINGS);
        addBlockArmorRecipe("Ice Boots", BlockArmor.createIceBoots(), Items.PACKED_ICE, Items.LEATHER_BOOTS);
        
        // Prismarine Armor
        addBlockArmorRecipe("Prismarine Helmet", BlockArmor.createPrismarineHelmet(), Items.PRISMARINE, Items.LEATHER_HELMET);
        addBlockArmorRecipe("Prismarine Chestplate", BlockArmor.createPrismarineChestplate(), Items.PRISMARINE, Items.LEATHER_CHESTPLATE);
        addBlockArmorRecipe("Prismarine Leggings", BlockArmor.createPrismarineLeggings(), Items.PRISMARINE, Items.LEATHER_LEGGINGS);
        addBlockArmorRecipe("Prismarine Boots", BlockArmor.createPrismarineBoots(), Items.PRISMARINE, Items.LEATHER_BOOTS);
        
        // Terracotta Armor
        addBlockArmorRecipe("Terracotta Helmet", BlockArmor.createTerracottaHelmet(), Items.TERRACOTTA, Items.LEATHER_HELMET);
        addBlockArmorRecipe("Terracotta Chestplate", BlockArmor.createTerracottaChestplate(), Items.TERRACOTTA, Items.LEATHER_CHESTPLATE);
        addBlockArmorRecipe("Terracotta Leggings", BlockArmor.createTerracottaLeggings(), Items.TERRACOTTA, Items.LEATHER_LEGGINGS);
        addBlockArmorRecipe("Terracotta Boots", BlockArmor.createTerracottaBoots(), Items.TERRACOTTA, Items.LEATHER_BOOTS);
        
        // Mossy Armor
        addBlockArmorRecipe("Mossy Helmet", BlockArmor.createMossyHelmet(), Items.MOSSY_COBBLESTONE, Items.LEATHER_HELMET);
        addBlockArmorRecipe("Mossy Chestplate", BlockArmor.createMossyChestplate(), Items.MOSSY_COBBLESTONE, Items.LEATHER_CHESTPLATE);
        addBlockArmorRecipe("Mossy Leggings", BlockArmor.createMossyLeggings(), Items.MOSSY_COBBLESTONE, Items.LEATHER_LEGGINGS);
        addBlockArmorRecipe("Mossy Boots", BlockArmor.createMossyBoots(), Items.MOSSY_COBBLESTONE, Items.LEATHER_BOOTS);
        
        // Soul Sand Armor
        addBlockArmorRecipe("Soul Sand Helmet", BlockArmor.createSoulSandHelmet(), Items.SOUL_SAND, Items.LEATHER_HELMET);
        addBlockArmorRecipe("Soul Sand Chestplate", BlockArmor.createSoulSandChestplate(), Items.SOUL_SAND, Items.LEATHER_CHESTPLATE);
        addBlockArmorRecipe("Soul Sand Leggings", BlockArmor.createSoulSandLeggings(), Items.SOUL_SAND, Items.LEATHER_LEGGINGS);
        addBlockArmorRecipe("Soul Sand Boots", BlockArmor.createSoulSandBoots(), Items.SOUL_SAND, Items.LEATHER_BOOTS);
        
        // Magma Armor
        addBlockArmorRecipe("Magma Helmet", BlockArmor.createMagmaHelmet(), Items.MAGMA_BLOCK, Items.LEATHER_HELMET);
        addBlockArmorRecipe("Magma Chestplate", BlockArmor.createMagmaChestplate(), Items.MAGMA_BLOCK, Items.LEATHER_CHESTPLATE);
        addBlockArmorRecipe("Magma Leggings", BlockArmor.createMagmaLeggings(), Items.MAGMA_BLOCK, Items.LEATHER_LEGGINGS);
        addBlockArmorRecipe("Magma Boots", BlockArmor.createMagmaBoots(), Items.MAGMA_BLOCK, Items.LEATHER_BOOTS);
        
        // Sandstone Armor
        addBlockArmorRecipe("Sandstone Helmet", BlockArmor.createSandstoneHelmet(), Items.SANDSTONE, Items.LEATHER_HELMET);
        addBlockArmorRecipe("Sandstone Chestplate", BlockArmor.createSandstoneChestplate(), Items.SANDSTONE, Items.LEATHER_CHESTPLATE);
        addBlockArmorRecipe("Sandstone Leggings", BlockArmor.createSandstoneLeggings(), Items.SANDSTONE, Items.LEATHER_LEGGINGS);
        addBlockArmorRecipe("Sandstone Boots", BlockArmor.createSandstoneBoots(), Items.SANDSTONE, Items.LEATHER_BOOTS);
        
        // Amethyst Armor
        addBlockArmorRecipe("Amethyst Helmet", BlockArmor.createAmethystHelmet(), Items.AMETHYST_BLOCK, Items.LEATHER_HELMET);
        addBlockArmorRecipe("Amethyst Chestplate", BlockArmor.createAmethystChestplate(), Items.AMETHYST_BLOCK, Items.LEATHER_CHESTPLATE);
        addBlockArmorRecipe("Amethyst Leggings", BlockArmor.createAmethystLeggings(), Items.AMETHYST_BLOCK, Items.LEATHER_LEGGINGS);
        addBlockArmorRecipe("Amethyst Boots", BlockArmor.createAmethystBoots(), Items.AMETHYST_BLOCK, Items.LEATHER_BOOTS);
        
        // Coal Armor
        addBlockArmorRecipe("Coal Helmet", BlockArmor.createCoalHelmet(), Items.COAL_BLOCK, Items.LEATHER_HELMET);
        addBlockArmorRecipe("Coal Chestplate", BlockArmor.createCoalChestplate(), Items.COAL_BLOCK, Items.LEATHER_CHESTPLATE);
        addBlockArmorRecipe("Coal Leggings", BlockArmor.createCoalLeggings(), Items.COAL_BLOCK, Items.LEATHER_LEGGINGS);
        addBlockArmorRecipe("Coal Boots", BlockArmor.createCoalBoots(), Items.COAL_BLOCK, Items.LEATHER_BOOTS);
        
        // ===== CUSTOM WEAPON RECIPES (No slayer requirement) =====
        
        // Reinforced Iron Sword
        RECIPES.add(new Recipe(
                "Reinforced Iron Sword",
                SlayerItems.createReinforcedIronSword(),
                new ItemStack[] {
                        new ItemStack(Items.IRON_INGOT), new ItemStack(Items.IRON_INGOT), new ItemStack(Items.IRON_INGOT),
                        ItemStack.EMPTY, new ItemStack(Items.IRON_SWORD), ItemStack.EMPTY,
                        ItemStack.EMPTY, new ItemStack(Items.IRON_INGOT), ItemStack.EMPTY
                },
                null, 0
        ));
        
        // Hardened Diamond Sword
        RECIPES.add(new Recipe(
                "Hardened Diamond Sword",
                SlayerItems.createHardenedDiamondSword(),
                new ItemStack[] {
                        new ItemStack(Items.DIAMOND), new ItemStack(Items.DIAMOND), new ItemStack(Items.DIAMOND),
                        ItemStack.EMPTY, new ItemStack(Items.DIAMOND_SWORD), ItemStack.EMPTY,
                        ItemStack.EMPTY, new ItemStack(Items.DIAMOND), ItemStack.EMPTY
                },
                null, 0
        ));
        
        // Void-Touched Blade
        RECIPES.add(new Recipe(
                "Void-Touched Blade",
                SlayerItems.createVoidTouchedSword(),
                new ItemStack[] {
                        new ItemStack(Items.NETHERITE_SCRAP), new ItemStack(Items.NETHERITE_SCRAP), new ItemStack(Items.NETHERITE_SCRAP),
                        ItemStack.EMPTY, new ItemStack(Items.NETHERITE_SWORD), ItemStack.EMPTY,
                        ItemStack.EMPTY, new ItemStack(Items.NETHERITE_INGOT), ItemStack.EMPTY
                },
                null, 0
        ));
        
        // Sharpened Gold Sword
        RECIPES.add(new Recipe(
                "Sharpened Gold Sword",
                SlayerItems.createSharpenedGoldSword(),
                new ItemStack[] {
                        new ItemStack(Items.GOLD_INGOT), new ItemStack(Items.GOLD_INGOT), new ItemStack(Items.GOLD_INGOT),
                        ItemStack.EMPTY, new ItemStack(Items.GOLDEN_SWORD), ItemStack.EMPTY,
                        ItemStack.EMPTY, new ItemStack(Items.GOLD_INGOT), ItemStack.EMPTY
                },
                null, 0
        ));
        
        // Heavy Stone Sword
        RECIPES.add(new Recipe(
                "Heavy Stone Sword",
                SlayerItems.createHeavyStoneSword(),
                new ItemStack[] {
                        new ItemStack(Items.STONE), new ItemStack(Items.STONE), new ItemStack(Items.STONE),
                        ItemStack.EMPTY, new ItemStack(Items.STONE_SWORD), ItemStack.EMPTY,
                        ItemStack.EMPTY, new ItemStack(Items.STONE), ItemStack.EMPTY
                },
                null, 0
        ));
        
        // Swift Wooden Sword
        RECIPES.add(new Recipe(
                "Swift Wooden Sword",
                SlayerItems.createSwiftWoodenSword(),
                new ItemStack[] {
                        new ItemStack(Items.OAK_PLANKS), new ItemStack(Items.OAK_PLANKS), new ItemStack(Items.OAK_PLANKS),
                        ItemStack.EMPTY, new ItemStack(Items.WOODEN_SWORD), ItemStack.EMPTY,
                        ItemStack.EMPTY, new ItemStack(Items.OAK_PLANKS), ItemStack.EMPTY
                },
                null, 0
        ));
        
        // ===== BLOCK WEAPON RECIPES (Match Block Armor sets) =====
        
        // Glass Blade
        addBlockWeaponRecipe("Glass Blade", BlockWeapons.createGlassSword(), Items.GLASS, Items.DIAMOND_SWORD);
        
        // Obsidian Blade
        addBlockWeaponRecipe("Obsidian Blade", BlockWeapons.createObsidianSword(), Items.OBSIDIAN, Items.NETHERITE_SWORD);
        
        // Quartz Blade
        addBlockWeaponRecipe("Quartz Blade", BlockWeapons.createQuartzSword(), Items.QUARTZ_BLOCK, Items.DIAMOND_SWORD);
        
        // Glowstone Blade
        addBlockWeaponRecipe("Glowstone Blade", BlockWeapons.createGlowstoneSword(), Items.GLOWSTONE, Items.GOLDEN_SWORD);
        
        // Redstone Blade
        addBlockWeaponRecipe("Redstone Blade", BlockWeapons.createRedstoneSword(), Items.REDSTONE_BLOCK, Items.IRON_SWORD);
        
        // Netherrack Blade
        addBlockWeaponRecipe("Netherrack Blade", BlockWeapons.createNetherrackSword(), Items.NETHERRACK, Items.STONE_SWORD);
        
        // Endstone Blade
        addBlockWeaponRecipe("Endstone Blade", BlockWeapons.createEndstoneSword(), Items.END_STONE, Items.DIAMOND_SWORD);
        
        // Ice Blade
        addBlockWeaponRecipe("Ice Blade", BlockWeapons.createIceSword(), Items.PACKED_ICE, Items.DIAMOND_SWORD);
        
        // Prismarine Blade
        addBlockWeaponRecipe("Prismarine Blade", BlockWeapons.createPrismarineSword(), Items.PRISMARINE, Items.DIAMOND_SWORD);
        
        // Terracotta Blade
        addBlockWeaponRecipe("Terracotta Blade", BlockWeapons.createTerracottaSword(), Items.TERRACOTTA, Items.IRON_SWORD);
        
        // Mossy Blade
        addBlockWeaponRecipe("Mossy Blade", BlockWeapons.createMossySword(), Items.MOSSY_COBBLESTONE, Items.STONE_SWORD);
        
        // Soul Blade
        addBlockWeaponRecipe("Soul Blade", BlockWeapons.createSoulSandSword(), Items.SOUL_SAND, Items.GOLDEN_SWORD);
        
        // Magma Blade
        addBlockWeaponRecipe("Magma Blade", BlockWeapons.createMagmaSword(), Items.MAGMA_BLOCK, Items.GOLDEN_SWORD);
        
        // Sandstone Blade
        addBlockWeaponRecipe("Sandstone Blade", BlockWeapons.createSandstoneSword(), Items.SANDSTONE, Items.STONE_SWORD);
        
        // Amethyst Blade
        addBlockWeaponRecipe("Amethyst Blade", BlockWeapons.createAmethystSword(), Items.AMETHYST_BLOCK, Items.DIAMOND_SWORD);
        
        // Coal Blade
        addBlockWeaponRecipe("Coal Blade", BlockWeapons.createCoalSword(), Items.COAL_BLOCK, Items.STONE_SWORD);
        
        // ===== ARMOR CRAFTER CORES =====
        
        // T1 Armor Crafter Core
        RECIPES.add(new Recipe(
                "Armor Crafter Core (T1)",
                SlayerItems.createCustomCrafterCoreT1(),
                new ItemStack[] {
                        new ItemStack(Items.IRON_INGOT), new ItemStack(Items.IRON_INGOT), new ItemStack(Items.IRON_INGOT),
                        new ItemStack(Items.IRON_INGOT), new ItemStack(Items.NETHERITE_SCRAP), new ItemStack(Items.IRON_INGOT),
                        new ItemStack(Items.IRON_INGOT), new ItemStack(Items.IRON_INGOT), new ItemStack(Items.IRON_INGOT)
                },
                null, 0
        ));
        
        // T2 Armor Crafter Core
        RECIPES.add(new Recipe(
                "Armor Crafter Core (T2)",
                SlayerItems.createCustomCrafterCoreT2(),
                new ItemStack[] {
                        new ItemStack(Items.DIAMOND), new ItemStack(Items.DIAMOND), new ItemStack(Items.DIAMOND),
                        new ItemStack(Items.DIAMOND), new ItemStack(Items.NETHERITE_INGOT), new ItemStack(Items.DIAMOND),
                        new ItemStack(Items.DIAMOND), new ItemStack(Items.DIAMOND), new ItemStack(Items.DIAMOND)
                },
                null, 0
        ));
        
        // T3 Armor Crafter Core
        RECIPES.add(new Recipe(
                "Armor Crafter Core (T3)",
                SlayerItems.createCustomCrafterCoreT3(),
                new ItemStack[] {
                        new ItemStack(Items.NETHERITE_INGOT), new ItemStack(Items.NETHERITE_INGOT), new ItemStack(Items.NETHERITE_INGOT),
                        new ItemStack(Items.NETHERITE_INGOT), new ItemStack(Items.NETHER_STAR), new ItemStack(Items.NETHERITE_INGOT),
                        new ItemStack(Items.NETHERITE_INGOT), new ItemStack(Items.NETHERITE_INGOT), new ItemStack(Items.NETHERITE_INGOT)
                },
                null, 0
        ));
        
        // ===== BLOCK TOOLS =====
        
        // Glass Tools
        addBlockToolRecipe("Glass Pickaxe", BlockTools.createGlassPickaxe(), Items.GLASS, Items.DIAMOND_PICKAXE);
        addBlockToolRecipe("Glass Axe", BlockTools.createGlassAxe(), Items.GLASS, Items.DIAMOND_AXE);
        addBlockToolRecipe("Glass Shovel", BlockTools.createGlassShovel(), Items.GLASS, Items.DIAMOND_SHOVEL);
        addBlockToolRecipe("Glass Hoe", BlockTools.createGlassHoe(), Items.GLASS, Items.DIAMOND_HOE);
        
        // Obsidian Tools
        addBlockToolRecipe("Obsidian Pickaxe", BlockTools.createObsidianPickaxe(), Items.OBSIDIAN, Items.NETHERITE_PICKAXE);
        addBlockToolRecipe("Obsidian Axe", BlockTools.createObsidianAxe(), Items.OBSIDIAN, Items.NETHERITE_AXE);
        addBlockToolRecipe("Obsidian Shovel", BlockTools.createObsidianShovel(), Items.OBSIDIAN, Items.NETHERITE_SHOVEL);
        addBlockToolRecipe("Obsidian Hoe", BlockTools.createObsidianHoe(), Items.OBSIDIAN, Items.NETHERITE_HOE);
        
        // Quartz Tools
        addBlockToolRecipe("Quartz Pickaxe", BlockTools.createQuartzPickaxe(), Items.QUARTZ_BLOCK, Items.DIAMOND_PICKAXE);
        addBlockToolRecipe("Quartz Axe", BlockTools.createQuartzAxe(), Items.QUARTZ_BLOCK, Items.DIAMOND_AXE);
        addBlockToolRecipe("Quartz Shovel", BlockTools.createQuartzShovel(), Items.QUARTZ_BLOCK, Items.DIAMOND_SHOVEL);
        addBlockToolRecipe("Quartz Hoe", BlockTools.createQuartzHoe(), Items.QUARTZ_BLOCK, Items.DIAMOND_HOE);
        
        // Glowstone Tools
        addBlockToolRecipe("Glowstone Pickaxe", BlockTools.createGlowstonePickaxe(), Items.GLOWSTONE, Items.GOLDEN_PICKAXE);
        addBlockToolRecipe("Glowstone Axe", BlockTools.createGlowstoneAxe(), Items.GLOWSTONE, Items.GOLDEN_AXE);
        addBlockToolRecipe("Glowstone Shovel", BlockTools.createGlowstoneShovel(), Items.GLOWSTONE, Items.GOLDEN_SHOVEL);
        addBlockToolRecipe("Glowstone Hoe", BlockTools.createGlowstoneHoe(), Items.GLOWSTONE, Items.GOLDEN_HOE);
        
        // Redstone Tools
        addBlockToolRecipe("Redstone Pickaxe", BlockTools.createRedstonePickaxe(), Items.REDSTONE_BLOCK, Items.IRON_PICKAXE);
        addBlockToolRecipe("Redstone Axe", BlockTools.createRedstoneAxe(), Items.REDSTONE_BLOCK, Items.IRON_AXE);
        addBlockToolRecipe("Redstone Shovel", BlockTools.createRedstoneShovel(), Items.REDSTONE_BLOCK, Items.IRON_SHOVEL);
        addBlockToolRecipe("Redstone Hoe", BlockTools.createRedstoneHoe(), Items.REDSTONE_BLOCK, Items.IRON_HOE);
        
        // Netherrack Tools
        addBlockToolRecipe("Netherrack Pickaxe", BlockTools.createNetherrackPickaxe(), Items.NETHERRACK, Items.STONE_PICKAXE);
        addBlockToolRecipe("Netherrack Axe", BlockTools.createNetherrackAxe(), Items.NETHERRACK, Items.STONE_AXE);
        addBlockToolRecipe("Netherrack Shovel", BlockTools.createNetherrackShovel(), Items.NETHERRACK, Items.STONE_SHOVEL);
        addBlockToolRecipe("Netherrack Hoe", BlockTools.createNetherrackHoe(), Items.NETHERRACK, Items.STONE_HOE);
        
        // End Stone Tools
        addBlockToolRecipe("End Stone Pickaxe", BlockTools.createEndstonePickaxe(), Items.END_STONE, Items.DIAMOND_PICKAXE);
        addBlockToolRecipe("End Stone Axe", BlockTools.createEndstoneAxe(), Items.END_STONE, Items.DIAMOND_AXE);
        addBlockToolRecipe("End Stone Shovel", BlockTools.createEndstoneShovel(), Items.END_STONE, Items.DIAMOND_SHOVEL);
        addBlockToolRecipe("End Stone Hoe", BlockTools.createEndstoneHoe(), Items.END_STONE, Items.DIAMOND_HOE);
        
        // Ice Tools
        addBlockToolRecipe("Ice Pickaxe", BlockTools.createIcePickaxe(), Items.PACKED_ICE, Items.DIAMOND_PICKAXE);
        addBlockToolRecipe("Ice Axe", BlockTools.createIceAxe(), Items.PACKED_ICE, Items.DIAMOND_AXE);
        addBlockToolRecipe("Ice Shovel", BlockTools.createIceShovel(), Items.PACKED_ICE, Items.DIAMOND_SHOVEL);
        addBlockToolRecipe("Ice Hoe", BlockTools.createIceHoe(), Items.PACKED_ICE, Items.DIAMOND_HOE);
        
        // Prismarine Tools
        addBlockToolRecipe("Prismarine Pickaxe", BlockTools.createPrismarinePickaxe(), Items.PRISMARINE, Items.DIAMOND_PICKAXE);
        addBlockToolRecipe("Prismarine Axe", BlockTools.createPrismarineAxe(), Items.PRISMARINE, Items.DIAMOND_AXE);
        addBlockToolRecipe("Prismarine Shovel", BlockTools.createPrismarineShovel(), Items.PRISMARINE, Items.DIAMOND_SHOVEL);
        addBlockToolRecipe("Prismarine Hoe", BlockTools.createPrismarineHoe(), Items.PRISMARINE, Items.DIAMOND_HOE);
        
        // Terracotta Tools
        addBlockToolRecipe("Terracotta Pickaxe", BlockTools.createTerracottaPickaxe(), Items.TERRACOTTA, Items.IRON_PICKAXE);
        addBlockToolRecipe("Terracotta Axe", BlockTools.createTerracottaAxe(), Items.TERRACOTTA, Items.IRON_AXE);
        addBlockToolRecipe("Terracotta Shovel", BlockTools.createTerracottaShovel(), Items.TERRACOTTA, Items.IRON_SHOVEL);
        addBlockToolRecipe("Terracotta Hoe", BlockTools.createTerracottaHoe(), Items.TERRACOTTA, Items.IRON_HOE);
        
        // Mossy Tools
        addBlockToolRecipe("Mossy Pickaxe", BlockTools.createMossyPickaxe(), Items.MOSSY_COBBLESTONE, Items.IRON_PICKAXE);
        addBlockToolRecipe("Mossy Axe", BlockTools.createMossyAxe(), Items.MOSSY_COBBLESTONE, Items.IRON_AXE);
        addBlockToolRecipe("Mossy Shovel", BlockTools.createMossyShovel(), Items.MOSSY_COBBLESTONE, Items.IRON_SHOVEL);
        addBlockToolRecipe("Mossy Hoe", BlockTools.createMossyHoe(), Items.MOSSY_COBBLESTONE, Items.IRON_HOE);
        
        // Soul Sand Tools
        addBlockToolRecipe("Soul Sand Pickaxe", BlockTools.createSoulSandPickaxe(), Items.SOUL_SAND, Items.STONE_PICKAXE);
        addBlockToolRecipe("Soul Sand Axe", BlockTools.createSoulSandAxe(), Items.SOUL_SAND, Items.STONE_AXE);
        addBlockToolRecipe("Soul Sand Shovel", BlockTools.createSoulSandShovel(), Items.SOUL_SAND, Items.STONE_SHOVEL);
        addBlockToolRecipe("Soul Sand Hoe", BlockTools.createSoulSandHoe(), Items.SOUL_SAND, Items.STONE_HOE);
        
        // Magma Tools
        addBlockToolRecipe("Magma Pickaxe", BlockTools.createMagmaPickaxe(), Items.MAGMA_BLOCK, Items.IRON_PICKAXE);
        addBlockToolRecipe("Magma Axe", BlockTools.createMagmaAxe(), Items.MAGMA_BLOCK, Items.IRON_AXE);
        addBlockToolRecipe("Magma Shovel", BlockTools.createMagmaShovel(), Items.MAGMA_BLOCK, Items.IRON_SHOVEL);
        addBlockToolRecipe("Magma Hoe", BlockTools.createMagmaHoe(), Items.MAGMA_BLOCK, Items.IRON_HOE);
        
        // Sandstone Tools
        addBlockToolRecipe("Sandstone Pickaxe", BlockTools.createSandstonePickaxe(), Items.SANDSTONE, Items.IRON_PICKAXE);
        addBlockToolRecipe("Sandstone Axe", BlockTools.createSandstoneAxe(), Items.SANDSTONE, Items.IRON_AXE);
        addBlockToolRecipe("Sandstone Shovel", BlockTools.createSandstoneShovel(), Items.SANDSTONE, Items.IRON_SHOVEL);
        addBlockToolRecipe("Sandstone Hoe", BlockTools.createSandstoneHoe(), Items.SANDSTONE, Items.IRON_HOE);
        
        // Amethyst Tools
        addBlockToolRecipe("Amethyst Pickaxe", BlockTools.createAmethystPickaxe(), Items.AMETHYST_BLOCK, Items.DIAMOND_PICKAXE);
        addBlockToolRecipe("Amethyst Axe", BlockTools.createAmethystAxe(), Items.AMETHYST_BLOCK, Items.DIAMOND_AXE);
        addBlockToolRecipe("Amethyst Shovel", BlockTools.createAmethystShovel(), Items.AMETHYST_BLOCK, Items.DIAMOND_SHOVEL);
        addBlockToolRecipe("Amethyst Hoe", BlockTools.createAmethystHoe(), Items.AMETHYST_BLOCK, Items.DIAMOND_HOE);
        
        // Coal Tools
        addBlockToolRecipe("Coal Pickaxe", BlockTools.createCoalPickaxe(), Items.COAL_BLOCK, Items.STONE_PICKAXE);
        addBlockToolRecipe("Coal Axe", BlockTools.createCoalAxe(), Items.COAL_BLOCK, Items.STONE_AXE);
        addBlockToolRecipe("Coal Shovel", BlockTools.createCoalShovel(), Items.COAL_BLOCK, Items.STONE_SHOVEL);
        addBlockToolRecipe("Coal Hoe", BlockTools.createCoalHoe(), Items.COAL_BLOCK, Items.STONE_HOE);
        
    }
    
    private static void addBlockWeaponRecipe(String name, ItemStack result, Item blockItem, Item weaponBase) {
        ItemStack[] ingredients = new ItemStack[] {
            new ItemStack(blockItem), new ItemStack(blockItem), new ItemStack(blockItem),
            new ItemStack(blockItem), new ItemStack(weaponBase), new ItemStack(blockItem),
            ItemStack.EMPTY, new ItemStack(blockItem), ItemStack.EMPTY
        };
        RECIPES.add(new Recipe(name, result, ingredients, null, 0));
    }
    
    private static void addBlockToolRecipe(String name, ItemStack result, Item blockItem, Item toolBase) {
        ItemStack[] ingredients = new ItemStack[] {
            new ItemStack(blockItem), new ItemStack(blockItem), new ItemStack(blockItem),
            new ItemStack(blockItem), new ItemStack(toolBase), new ItemStack(blockItem),
            ItemStack.EMPTY, new ItemStack(blockItem), ItemStack.EMPTY
        };
        RECIPES.add(new Recipe(name, result, ingredients, null, 0));
    }
    
    private static void addBlockArmorRecipe(String name, ItemStack result, Item blockItem, Item armorBase) {
        ItemStack[] ingredients;
        if (name.contains("Helmet")) {
            ingredients = new ItemStack[] {
                new ItemStack(blockItem), new ItemStack(blockItem), new ItemStack(blockItem),
                new ItemStack(blockItem), new ItemStack(armorBase), new ItemStack(blockItem),
                ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
            };
        } else if (name.contains("Chestplate")) {
            ingredients = new ItemStack[] {
                new ItemStack(blockItem), new ItemStack(blockItem), new ItemStack(blockItem),
                new ItemStack(blockItem), new ItemStack(armorBase), new ItemStack(blockItem),
                new ItemStack(blockItem), new ItemStack(blockItem), new ItemStack(blockItem)
            };
        } else if (name.contains("Leggings")) {
            ingredients = new ItemStack[] {
                new ItemStack(blockItem), new ItemStack(blockItem), new ItemStack(blockItem),
                new ItemStack(blockItem), new ItemStack(armorBase), new ItemStack(blockItem),
                new ItemStack(blockItem), ItemStack.EMPTY, new ItemStack(blockItem)
            };
        } else { // Boots
            ingredients = new ItemStack[] {
                ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY,
                new ItemStack(blockItem), new ItemStack(armorBase), new ItemStack(blockItem),
                new ItemStack(blockItem), ItemStack.EMPTY, new ItemStack(blockItem)
            };
        }
        RECIPES.add(new Recipe(name, result, ingredients, null, 0));
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
    
    /**
     * Finds a recipe that produces the given output item.
     * Compares by custom item ID if available, otherwise by item type.
     */
    public static Recipe getRecipeForOutput(ItemStack output) {
        if (output == null || output.isEmpty()) return null;
        
        String outputId = SlayerItems.getCustomItemId(output);
        
        for (Recipe recipe : RECIPES) {
            if (recipe.result == null || recipe.result.isEmpty()) continue;
            
            // Try to match by custom item ID first
            if (outputId != null) {
                String recipeId = SlayerItems.getCustomItemId(recipe.result);
                if (outputId.equals(recipeId)) {
                    return recipe;
                }
            }
            
            // Fallback: match by item type and custom name
            if (recipe.result.getItem() == output.getItem()) {
                Text recipeName = recipe.result.get(DataComponentTypes.CUSTOM_NAME);
                Text outputName = output.get(DataComponentTypes.CUSTOM_NAME);
                if (recipeName == null && outputName == null) return recipe;
                if (recipeName != null && recipeName.equals(outputName)) return recipe;
            }
        }
        return null;
    }
}
