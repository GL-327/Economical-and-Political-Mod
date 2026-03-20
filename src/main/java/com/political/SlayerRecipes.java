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
                        new ItemStack(Items.LAPIS_LAZULI), SlayerItems.createCustomCrafterCoreT1(), new ItemStack(Items.LAPIS_LAZULI),
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
                        new ItemStack(Items.LAPIS_LAZULI), SlayerItems.createCustomCrafterCoreT1(), new ItemStack(Items.LAPIS_LAZULI),
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
                        new ItemStack(Items.LAPIS_LAZULI), SlayerItems.createCustomCrafterCoreT1(), new ItemStack(Items.LAPIS_LAZULI),
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
                        new ItemStack(Items.LAPIS_LAZULI), SlayerItems.createCustomCrafterCoreT1(), new ItemStack(Items.LAPIS_LAZULI),
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
                        new ItemStack(Items.EMERALD), SlayerItems.createCustomCrafterCoreT1(), new ItemStack(Items.EMERALD),
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
                        new ItemStack(Items.EMERALD), SlayerItems.createCustomCrafterCoreT1(), new ItemStack(Items.EMERALD),
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
                        new ItemStack(Items.EMERALD), SlayerItems.createCustomCrafterCoreT1(), new ItemStack(Items.EMERALD),
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
                        new ItemStack(Items.EMERALD), SlayerItems.createCustomCrafterCoreT1(), new ItemStack(Items.EMERALD),
                        new ItemStack(Items.EMERALD), ItemStack.EMPTY, new ItemStack(Items.EMERALD)
                },
                null, 0
        ));
        
        // ===== BLOCK ARMOR RECIPES (No slayer requirement) =====
        
        // Glass Armor (all pieces) - T1 (basic material)
        addBlockArmorRecipeWithCore("Glass Helmet", BlockArmor.createGlassHelmet(), Items.GLASS, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Glass Chestplate", BlockArmor.createGlassChestplate(), Items.GLASS, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Glass Leggings", BlockArmor.createGlassLeggings(), Items.GLASS, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Glass Boots", BlockArmor.createGlassBoots(), Items.GLASS, Items.LEATHER_BOOTS, 1);
        
        // Obsidian Armor - T2 (durable material, blast resistant)
        addBlockArmorRecipeWithCore("Obsidian Helmet", BlockArmor.createObsidianHelmet(), Items.OBSIDIAN, Items.LEATHER_HELMET, 2);
        addBlockArmorRecipeWithCore("Obsidian Chestplate", BlockArmor.createObsidianChestplate(), Items.OBSIDIAN, Items.LEATHER_CHESTPLATE, 2);
        addBlockArmorRecipeWithCore("Obsidian Leggings", BlockArmor.createObsidianLeggings(), Items.OBSIDIAN, Items.LEATHER_LEGGINGS, 2);
        addBlockArmorRecipeWithCore("Obsidian Boots", BlockArmor.createObsidianBoots(), Items.OBSIDIAN, Items.LEATHER_BOOTS, 2);
        
        // Quartz Armor - T1 (basic nether material)
        addBlockArmorRecipeWithCore("Quartz Helmet", BlockArmor.createQuartzHelmet(), Items.QUARTZ_BLOCK, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Quartz Chestplate", BlockArmor.createQuartzChestplate(), Items.QUARTZ_BLOCK, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Quartz Leggings", BlockArmor.createQuartzLeggings(), Items.QUARTZ_BLOCK, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Quartz Boots", BlockArmor.createQuartzBoots(), Items.QUARTZ_BLOCK, Items.LEATHER_BOOTS, 1);
        
        // Glowstone Armor - T1 (light-emitting material)
        addBlockArmorRecipeWithCore("Glowstone Helmet", BlockArmor.createGlowstoneHelmet(), Items.GLOWSTONE, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Glowstone Chestplate", BlockArmor.createGlowstoneChestplate(), Items.GLOWSTONE, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Glowstone Leggings", BlockArmor.createGlowstoneLeggings(), Items.GLOWSTONE, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Glowstone Boots", BlockArmor.createGlowstoneBoots(), Items.GLOWSTONE, Items.LEATHER_BOOTS, 1);
        
        // Redstone Armor - T1 (common material)
        addBlockArmorRecipeWithCore("Redstone Helmet", BlockArmor.createRedstoneHelmet(), Items.REDSTONE_BLOCK, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Redstone Chestplate", BlockArmor.createRedstoneChestplate(), Items.REDSTONE_BLOCK, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Redstone Leggings", BlockArmor.createRedstoneLeggings(), Items.REDSTONE_BLOCK, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Redstone Boots", BlockArmor.createRedstoneBoots(), Items.REDSTONE_BLOCK, Items.LEATHER_BOOTS, 1);
        
        // Netherrack Armor - T1 (common nether material)
        addBlockArmorRecipeWithCore("Netherrack Helmet", BlockArmor.createNetherrackHelmet(), Items.NETHERRACK, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Netherrack Chestplate", BlockArmor.createNetherrackChestplate(), Items.NETHERRACK, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Netherrack Leggings", BlockArmor.createNetherrackLeggings(), Items.NETHERRACK, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Netherrack Boots", BlockArmor.createNetherrackBoots(), Items.NETHERRACK, Items.LEATHER_BOOTS, 1);
        
        // End Stone Armor - T2 (end material, decent durability)
        addBlockArmorRecipeWithCore("End Stone Helmet", BlockArmor.createEndstoneHelmet(), Items.END_STONE, Items.LEATHER_HELMET, 2);
        addBlockArmorRecipeWithCore("End Stone Chestplate", BlockArmor.createEndstoneChestplate(), Items.END_STONE, Items.LEATHER_CHESTPLATE, 2);
        addBlockArmorRecipeWithCore("End Stone Leggings", BlockArmor.createEndstoneLeggings(), Items.END_STONE, Items.LEATHER_LEGGINGS, 2);
        addBlockArmorRecipeWithCore("End Stone Boots", BlockArmor.createEndstoneBoots(), Items.END_STONE, Items.LEATHER_BOOTS, 2);
        
        // Ice Armor - T1 (basic material)
        addBlockArmorRecipeWithCore("Ice Helmet", BlockArmor.createIceHelmet(), Items.PACKED_ICE, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Ice Chestplate", BlockArmor.createIceChestplate(), Items.PACKED_ICE, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Ice Leggings", BlockArmor.createIceLeggings(), Items.PACKED_ICE, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Ice Boots", BlockArmor.createIceBoots(), Items.PACKED_ICE, Items.LEATHER_BOOTS, 1);
        
        // Prismarine Armor - T2 (ocean material, decent durability)
        addBlockArmorRecipeWithCore("Prismarine Helmet", BlockArmor.createPrismarineHelmet(), Items.PRISMARINE, Items.LEATHER_HELMET, 2);
        addBlockArmorRecipeWithCore("Prismarine Chestplate", BlockArmor.createPrismarineChestplate(), Items.PRISMARINE, Items.LEATHER_CHESTPLATE, 2);
        addBlockArmorRecipeWithCore("Prismarine Leggings", BlockArmor.createPrismarineLeggings(), Items.PRISMARINE, Items.LEATHER_LEGGINGS, 2);
        addBlockArmorRecipeWithCore("Prismarine Boots", BlockArmor.createPrismarineBoots(), Items.PRISMARINE, Items.LEATHER_BOOTS, 2);
        
        // Terracotta Armor - T1 (basic material)
        addBlockArmorRecipeWithCore("Terracotta Helmet", BlockArmor.createTerracottaHelmet(), Items.TERRACOTTA, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Terracotta Chestplate", BlockArmor.createTerracottaChestplate(), Items.TERRACOTTA, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Terracotta Leggings", BlockArmor.createTerracottaLeggings(), Items.TERRACOTTA, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Terracotta Boots", BlockArmor.createTerracottaBoots(), Items.TERRACOTTA, Items.LEATHER_BOOTS, 1);
        
        // Mossy Armor - T1 (basic material)
        addBlockArmorRecipeWithCore("Mossy Helmet", BlockArmor.createMossyHelmet(), Items.MOSSY_COBBLESTONE, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Mossy Chestplate", BlockArmor.createMossyChestplate(), Items.MOSSY_COBBLESTONE, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Mossy Leggings", BlockArmor.createMossyLeggings(), Items.MOSSY_COBBLESTONE, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Mossy Boots", BlockArmor.createMossyBoots(), Items.MOSSY_COBBLESTONE, Items.LEATHER_BOOTS, 1);
        
        // Soul Sand Armor - T1 (soul material)
        addBlockArmorRecipeWithCore("Soul Sand Helmet", BlockArmor.createSoulSandHelmet(), Items.SOUL_SAND, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Soul Sand Chestplate", BlockArmor.createSoulSandChestplate(), Items.SOUL_SAND, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Soul Sand Leggings", BlockArmor.createSoulSandLeggings(), Items.SOUL_SAND, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Soul Sand Boots", BlockArmor.createSoulSandBoots(), Items.SOUL_SAND, Items.LEATHER_BOOTS, 1);
        
        // Magma Armor - T2 (nether material, fire resistant)
        addBlockArmorRecipeWithCore("Magma Helmet", BlockArmor.createMagmaHelmet(), Items.MAGMA_BLOCK, Items.LEATHER_HELMET, 2);
        addBlockArmorRecipeWithCore("Magma Chestplate", BlockArmor.createMagmaChestplate(), Items.MAGMA_BLOCK, Items.LEATHER_CHESTPLATE, 2);
        addBlockArmorRecipeWithCore("Magma Leggings", BlockArmor.createMagmaLeggings(), Items.MAGMA_BLOCK, Items.LEATHER_LEGGINGS, 2);
        addBlockArmorRecipeWithCore("Magma Boots", BlockArmor.createMagmaBoots(), Items.MAGMA_BLOCK, Items.LEATHER_BOOTS, 2);
        
        // Sandstone Armor - T1 (basic material)
        addBlockArmorRecipeWithCore("Sandstone Helmet", BlockArmor.createSandstoneHelmet(), Items.SANDSTONE, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Sandstone Chestplate", BlockArmor.createSandstoneChestplate(), Items.SANDSTONE, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Sandstone Leggings", BlockArmor.createSandstoneLeggings(), Items.SANDSTONE, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Sandstone Boots", BlockArmor.createSandstoneBoots(), Items.SANDSTONE, Items.LEATHER_BOOTS, 1);
        
        // Amethyst Armor - T2 (rare crystal material)
        addBlockArmorRecipeWithCore("Amethyst Helmet", BlockArmor.createAmethystHelmet(), Items.AMETHYST_BLOCK, Items.LEATHER_HELMET, 2);
        addBlockArmorRecipeWithCore("Amethyst Chestplate", BlockArmor.createAmethystChestplate(), Items.AMETHYST_BLOCK, Items.LEATHER_CHESTPLATE, 2);
        addBlockArmorRecipeWithCore("Amethyst Leggings", BlockArmor.createAmethystLeggings(), Items.AMETHYST_BLOCK, Items.LEATHER_LEGGINGS, 2);
        addBlockArmorRecipeWithCore("Amethyst Boots", BlockArmor.createAmethystBoots(), Items.AMETHYST_BLOCK, Items.LEATHER_BOOTS, 2);
        
        // Coal Armor - T1 (common material)
        addBlockArmorRecipeWithCore("Coal Helmet", BlockArmor.createCoalHelmet(), Items.COAL_BLOCK, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Coal Chestplate", BlockArmor.createCoalChestplate(), Items.COAL_BLOCK, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Coal Leggings", BlockArmor.createCoalLeggings(), Items.COAL_BLOCK, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Coal Boots", BlockArmor.createCoalBoots(), Items.COAL_BLOCK, Items.LEATHER_BOOTS, 1);

        // ===== NEW ARMOR SETS WITH CRAFTING CORES =====
        // Stripped Log Armor (T1 - weaker than diamond)
        addBlockArmorRecipeWithCore("Stripped Spruce Log Helmet", BlockArmor.createStrippedSpruceLogHelmet(), Items.STRIPPED_SPRUCE_LOG, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Stripped Spruce Log Chestplate", BlockArmor.createStrippedSpruceLogChestplate(), Items.STRIPPED_SPRUCE_LOG, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Stripped Spruce Log Leggings", BlockArmor.createStrippedSpruceLogLeggings(), Items.STRIPPED_SPRUCE_LOG, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Stripped Spruce Log Boots", BlockArmor.createStrippedSpruceLogBoots(), Items.STRIPPED_SPRUCE_LOG, Items.LEATHER_BOOTS, 1);
        addBlockArmorRecipeWithCore("Stripped Birch Log Helmet", BlockArmor.createStrippedBirchLogHelmet(), Items.STRIPPED_BIRCH_LOG, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Stripped Birch Log Chestplate", BlockArmor.createStrippedBirchLogChestplate(), Items.STRIPPED_BIRCH_LOG, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Stripped Birch Log Leggings", BlockArmor.createStrippedBirchLogLeggings(), Items.STRIPPED_BIRCH_LOG, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Stripped Birch Log Boots", BlockArmor.createStrippedBirchLogBoots(), Items.STRIPPED_BIRCH_LOG, Items.LEATHER_BOOTS, 1);
        addBlockArmorRecipeWithCore("Stripped Dark Oak Log Helmet", BlockArmor.createStrippedDarkOakLogHelmet(), Items.STRIPPED_DARK_OAK_LOG, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Stripped Dark Oak Log Chestplate", BlockArmor.createStrippedDarkOakLogChestplate(), Items.STRIPPED_DARK_OAK_LOG, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Stripped Dark Oak Log Leggings", BlockArmor.createStrippedDarkOakLogLeggings(), Items.STRIPPED_DARK_OAK_LOG, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Stripped Dark Oak Log Boots", BlockArmor.createStrippedDarkOakLogBoots(), Items.STRIPPED_DARK_OAK_LOG, Items.LEATHER_BOOTS, 1);
        addBlockArmorRecipeWithCore("Stripped Jungle Log Helmet", BlockArmor.createStrippedJungleLogHelmet(), Items.STRIPPED_JUNGLE_LOG, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Stripped Jungle Log Chestplate", BlockArmor.createStrippedJungleLogChestplate(), Items.STRIPPED_JUNGLE_LOG, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Stripped Jungle Log Leggings", BlockArmor.createStrippedJungleLogLeggings(), Items.STRIPPED_JUNGLE_LOG, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Stripped Jungle Log Boots", BlockArmor.createStrippedJungleLogBoots(), Items.STRIPPED_JUNGLE_LOG, Items.LEATHER_BOOTS, 1);
        addBlockArmorRecipeWithCore("Stripped Acacia Log Helmet", BlockArmor.createStrippedAcaciaLogHelmet(), Items.STRIPPED_ACACIA_LOG, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Stripped Acacia Log Chestplate", BlockArmor.createStrippedAcaciaLogChestplate(), Items.STRIPPED_ACACIA_LOG, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Stripped Acacia Log Leggings", BlockArmor.createStrippedAcaciaLogLeggings(), Items.STRIPPED_ACACIA_LOG, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Stripped Acacia Log Boots", BlockArmor.createStrippedAcaciaLogBoots(), Items.STRIPPED_ACACIA_LOG, Items.LEATHER_BOOTS, 1);
        addBlockArmorRecipeWithCore("Stripped Mangrove Log Helmet", BlockArmor.createStrippedMangroveLogHelmet(), Items.STRIPPED_MANGROVE_LOG, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Stripped Mangrove Log Chestplate", BlockArmor.createStrippedMangroveLogChestplate(), Items.STRIPPED_MANGROVE_LOG, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Stripped Mangrove Log Leggings", BlockArmor.createStrippedMangroveLogLeggings(), Items.STRIPPED_MANGROVE_LOG, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Stripped Mangrove Log Boots", BlockArmor.createStrippedMangroveLogBoots(), Items.STRIPPED_MANGROVE_LOG, Items.LEATHER_BOOTS, 1);
        addBlockArmorRecipeWithCore("Stripped Cherry Log Helmet", BlockArmor.createStrippedCherryLogHelmet(), Items.STRIPPED_CHERRY_LOG, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Stripped Cherry Log Chestplate", BlockArmor.createStrippedCherryLogChestplate(), Items.STRIPPED_CHERRY_LOG, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Stripped Cherry Log Leggings", BlockArmor.createStrippedCherryLogLeggings(), Items.STRIPPED_CHERRY_LOG, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Stripped Cherry Log Boots", BlockArmor.createStrippedCherryLogBoots(), Items.STRIPPED_CHERRY_LOG, Items.LEATHER_BOOTS, 1);
        addBlockArmorRecipeWithCore("Bamboo Block Helmet", BlockArmor.createBambooBlockHelmet(), Items.BAMBOO_BLOCK, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Bamboo Block Chestplate", BlockArmor.createBambooBlockChestplate(), Items.BAMBOO_BLOCK, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Bamboo Block Leggings", BlockArmor.createBambooBlockLeggings(), Items.BAMBOO_BLOCK, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Bamboo Block Boots", BlockArmor.createBambooBlockBoots(), Items.BAMBOO_BLOCK, Items.LEATHER_BOOTS, 1);
        addBlockArmorRecipeWithCore("Stripped Crimson Stem Helmet", BlockArmor.createStrippedCrimsonStemHelmet(), Items.STRIPPED_CRIMSON_STEM, Items.LEATHER_HELMET, 2);
        addBlockArmorRecipeWithCore("Stripped Crimson Stem Chestplate", BlockArmor.createStrippedCrimsonStemChestplate(), Items.STRIPPED_CRIMSON_STEM, Items.LEATHER_CHESTPLATE, 2);
        addBlockArmorRecipeWithCore("Stripped Crimson Stem Leggings", BlockArmor.createStrippedCrimsonStemLeggings(), Items.STRIPPED_CRIMSON_STEM, Items.LEATHER_LEGGINGS, 2);
        addBlockArmorRecipeWithCore("Stripped Crimson Stem Boots", BlockArmor.createStrippedCrimsonStemBoots(), Items.STRIPPED_CRIMSON_STEM, Items.LEATHER_BOOTS, 2);
        addBlockArmorRecipeWithCore("Stripped Warped Stem Helmet", BlockArmor.createStrippedWarpedStemHelmet(), Items.STRIPPED_WARPED_STEM, Items.LEATHER_HELMET, 2);
        addBlockArmorRecipeWithCore("Stripped Warped Stem Chestplate", BlockArmor.createStrippedWarpedStemChestplate(), Items.STRIPPED_WARPED_STEM, Items.LEATHER_CHESTPLATE, 2);
        addBlockArmorRecipeWithCore("Stripped Warped Stem Leggings", BlockArmor.createStrippedWarpedStemLeggings(), Items.STRIPPED_WARPED_STEM, Items.LEATHER_LEGGINGS, 2);
        addBlockArmorRecipeWithCore("Stripped Warped Stem Boots", BlockArmor.createStrippedWarpedStemBoots(), Items.STRIPPED_WARPED_STEM, Items.LEATHER_BOOTS, 2);

        // Plank Armor (T1)
        addBlockArmorRecipeWithCore("Spruce Planks Helmet", BlockArmor.createSprucePlanksHelmet(), Items.SPRUCE_PLANKS, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Spruce Planks Chestplate", BlockArmor.createSprucePlanksChestplate(), Items.SPRUCE_PLANKS, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Spruce Planks Leggings", BlockArmor.createSprucePlanksLeggings(), Items.SPRUCE_PLANKS, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Spruce Planks Boots", BlockArmor.createSprucePlanksBoots(), Items.SPRUCE_PLANKS, Items.LEATHER_BOOTS, 1);
        addBlockArmorRecipeWithCore("Birch Planks Helmet", BlockArmor.createBirchPlanksHelmet(), Items.BIRCH_PLANKS, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Birch Planks Chestplate", BlockArmor.createBirchPlanksChestplate(), Items.BIRCH_PLANKS, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Birch Planks Leggings", BlockArmor.createBirchPlanksLeggings(), Items.BIRCH_PLANKS, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Birch Planks Boots", BlockArmor.createBirchPlanksBoots(), Items.BIRCH_PLANKS, Items.LEATHER_BOOTS, 1);
        addBlockArmorRecipeWithCore("Jungle Planks Helmet", BlockArmor.createJunglePlanksHelmet(), Items.JUNGLE_PLANKS, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Jungle Planks Chestplate", BlockArmor.createJunglePlanksChestplate(), Items.JUNGLE_PLANKS, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Jungle Planks Leggings", BlockArmor.createJunglePlanksLeggings(), Items.JUNGLE_PLANKS, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Jungle Planks Boots", BlockArmor.createJunglePlanksBoots(), Items.JUNGLE_PLANKS, Items.LEATHER_BOOTS, 1);
        addBlockArmorRecipeWithCore("Acacia Planks Helmet", BlockArmor.createAcaciaPlanksHelmet(), Items.ACACIA_PLANKS, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Acacia Planks Chestplate", BlockArmor.createAcaciaPlanksChestplate(), Items.ACACIA_PLANKS, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Acacia Planks Leggings", BlockArmor.createAcaciaPlanksLeggings(), Items.ACACIA_PLANKS, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Acacia Planks Boots", BlockArmor.createAcaciaPlanksBoots(), Items.ACACIA_PLANKS, Items.LEATHER_BOOTS, 1);
        addBlockArmorRecipeWithCore("Dark Oak Planks Helmet", BlockArmor.createDarkOakPlanksHelmet(), Items.DARK_OAK_PLANKS, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Dark Oak Planks Chestplate", BlockArmor.createDarkOakPlanksChestplate(), Items.DARK_OAK_PLANKS, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Dark Oak Planks Leggings", BlockArmor.createDarkOakPlanksLeggings(), Items.DARK_OAK_PLANKS, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Dark Oak Planks Boots", BlockArmor.createDarkOakPlanksBoots(), Items.DARK_OAK_PLANKS, Items.LEATHER_BOOTS, 1);
        addBlockArmorRecipeWithCore("Mangrove Planks Helmet", BlockArmor.createMangrovePlanksHelmet(), Items.MANGROVE_PLANKS, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Mangrove Planks Chestplate", BlockArmor.createMangrovePlanksChestplate(), Items.MANGROVE_PLANKS, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Mangrove Planks Leggings", BlockArmor.createMangrovePlanksLeggings(), Items.MANGROVE_PLANKS, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Mangrove Planks Boots", BlockArmor.createMangrovePlanksBoots(), Items.MANGROVE_PLANKS, Items.LEATHER_BOOTS, 1);
        addBlockArmorRecipeWithCore("Cherry Planks Helmet", BlockArmor.createCherryPlanksHelmet(), Items.CHERRY_PLANKS, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Cherry Planks Chestplate", BlockArmor.createCherryPlanksChestplate(), Items.CHERRY_PLANKS, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Cherry Planks Leggings", BlockArmor.createCherryPlanksLeggings(), Items.CHERRY_PLANKS, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Cherry Planks Boots", BlockArmor.createCherryPlanksBoots(), Items.CHERRY_PLANKS, Items.LEATHER_BOOTS, 1);
        addBlockArmorRecipeWithCore("Bamboo Planks Helmet", BlockArmor.createBambooPlanksHelmet(), Items.BAMBOO_PLANKS, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Bamboo Planks Chestplate", BlockArmor.createBambooPlanksChestplate(), Items.BAMBOO_PLANKS, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Bamboo Planks Leggings", BlockArmor.createBambooPlanksLeggings(), Items.BAMBOO_PLANKS, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Bamboo Planks Boots", BlockArmor.createBambooPlanksBoots(), Items.BAMBOO_PLANKS, Items.LEATHER_BOOTS, 1);
        addBlockArmorRecipeWithCore("Crimson Planks Helmet", BlockArmor.createCrimsonPlanksHelmet(), Items.CRIMSON_PLANKS, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Crimson Planks Chestplate", BlockArmor.createCrimsonPlanksChestplate(), Items.CRIMSON_PLANKS, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Crimson Planks Leggings", BlockArmor.createCrimsonPlanksLeggings(), Items.CRIMSON_PLANKS, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Crimson Planks Boots", BlockArmor.createCrimsonPlanksBoots(), Items.CRIMSON_PLANKS, Items.LEATHER_BOOTS, 1);
        addBlockArmorRecipeWithCore("Warped Planks Helmet", BlockArmor.createWarpedPlanksHelmet(), Items.WARPED_PLANKS, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Warped Planks Chestplate", BlockArmor.createWarpedPlanksChestplate(), Items.WARPED_PLANKS, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Warped Planks Leggings", BlockArmor.createWarpedPlanksLeggings(), Items.WARPED_PLANKS, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Warped Planks Boots", BlockArmor.createWarpedPlanksBoots(), Items.WARPED_PLANKS, Items.LEATHER_BOOTS, 1);
        addBlockArmorRecipeWithCore("Oak Planks Helmet", BlockArmor.createOakPlanksHelmet(), Items.OAK_PLANKS, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Oak Planks Chestplate", BlockArmor.createOakPlanksChestplate(), Items.OAK_PLANKS, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Oak Planks Leggings", BlockArmor.createOakPlanksLeggings(), Items.OAK_PLANKS, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Oak Planks Boots", BlockArmor.createOakPlanksBoots(), Items.OAK_PLANKS, Items.LEATHER_BOOTS, 1);

        // Stone/Mud/Log Armor
        addBlockArmorRecipeWithCore("Oak Log Helmet", BlockArmor.createOakLogHelmet(), Items.OAK_LOG, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Oak Log Chestplate", BlockArmor.createOakLogChestplate(), Items.OAK_LOG, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Oak Log Leggings", BlockArmor.createOakLogLeggings(), Items.OAK_LOG, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Oak Log Boots", BlockArmor.createOakLogBoots(), Items.OAK_LOG, Items.LEATHER_BOOTS, 1);
        addBlockArmorRecipeWithCore("Spruce Log Helmet", BlockArmor.createSpruceLogHelmet(), Items.SPRUCE_LOG, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Spruce Log Chestplate", BlockArmor.createSpruceLogChestplate(), Items.SPRUCE_LOG, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Spruce Log Leggings", BlockArmor.createSpruceLogLeggings(), Items.SPRUCE_LOG, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Spruce Log Boots", BlockArmor.createSpruceLogBoots(), Items.SPRUCE_LOG, Items.LEATHER_BOOTS, 1);
        addBlockArmorRecipeWithCore("Birch Log Helmet", BlockArmor.createBirchLogHelmet(), Items.BIRCH_LOG, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Birch Log Chestplate", BlockArmor.createBirchLogChestplate(), Items.BIRCH_LOG, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Birch Log Leggings", BlockArmor.createBirchLogLeggings(), Items.BIRCH_LOG, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Birch Log Boots", BlockArmor.createBirchLogBoots(), Items.BIRCH_LOG, Items.LEATHER_BOOTS, 1);
        addBlockArmorRecipeWithCore("Stone Bricks Helmet", BlockArmor.createStoneBricksHelmet(), Items.STONE_BRICKS, Items.LEATHER_HELMET, 2);
        addBlockArmorRecipeWithCore("Stone Bricks Chestplate", BlockArmor.createStoneBricksChestplate(), Items.STONE_BRICKS, Items.LEATHER_CHESTPLATE, 2);
        addBlockArmorRecipeWithCore("Stone Bricks Leggings", BlockArmor.createStoneBricksLeggings(), Items.STONE_BRICKS, Items.LEATHER_LEGGINGS, 2);
        addBlockArmorRecipeWithCore("Stone Bricks Boots", BlockArmor.createStoneBricksBoots(), Items.STONE_BRICKS, Items.LEATHER_BOOTS, 2);
        addBlockArmorRecipeWithCore("Cobblestone Helmet", BlockArmor.createCobblestoneHelmet(), Items.COBBLESTONE, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Cobblestone Chestplate", BlockArmor.createCobblestoneChestplate(), Items.COBBLESTONE, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Cobblestone Leggings", BlockArmor.createCobblestoneLeggings(), Items.COBBLESTONE, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Cobblestone Boots", BlockArmor.createCobblestoneBoots(), Items.COBBLESTONE, Items.LEATHER_BOOTS, 1);
        addBlockArmorRecipeWithCore("Mossy Cobblestone Helmet", BlockArmor.createMossyCobblestoneHelmet(), Items.MOSSY_COBBLESTONE, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Mossy Cobblestone Chestplate", BlockArmor.createMossyCobblestoneChestplate(), Items.MOSSY_COBBLESTONE, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Mossy Cobblestone Leggings", BlockArmor.createMossyCobblestoneLeggings(), Items.MOSSY_COBBLESTONE, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Mossy Cobblestone Boots", BlockArmor.createMossyCobblestoneBoots(), Items.MOSSY_COBBLESTONE, Items.LEATHER_BOOTS, 1);
        addBlockArmorRecipeWithCore("Cobbled Deepslate Helmet", BlockArmor.createCobbledDeepslateHelmet(), Items.COBBLED_DEEPSLATE, Items.LEATHER_HELMET, 2);
        addBlockArmorRecipeWithCore("Cobbled Deepslate Chestplate", BlockArmor.createCobbledDeepslateChestplate(), Items.COBBLED_DEEPSLATE, Items.LEATHER_CHESTPLATE, 2);
        addBlockArmorRecipeWithCore("Cobbled Deepslate Leggings", BlockArmor.createCobbledDeepslateLeggings(), Items.COBBLED_DEEPSLATE, Items.LEATHER_LEGGINGS, 2);
        addBlockArmorRecipeWithCore("Cobbled Deepslate Boots", BlockArmor.createCobbledDeepslateBoots(), Items.COBBLED_DEEPSLATE, Items.LEATHER_BOOTS, 2);
        addBlockArmorRecipeWithCore("Mud Bricks Helmet", BlockArmor.createMudBricksHelmet(), Items.MUD_BRICKS, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Mud Bricks Chestplate", BlockArmor.createMudBricksChestplate(), Items.MUD_BRICKS, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Mud Bricks Leggings", BlockArmor.createMudBricksLeggings(), Items.MUD_BRICKS, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Mud Bricks Boots", BlockArmor.createMudBricksBoots(), Items.MUD_BRICKS, Items.LEATHER_BOOTS, 1);
        addBlockArmorRecipeWithCore("Mangrove Roots Helmet", BlockArmor.createMangroveRootsHelmet(), Items.MANGROVE_ROOTS, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Mangrove Roots Chestplate", BlockArmor.createMangroveRootsChestplate(), Items.MANGROVE_ROOTS, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Mangrove Roots Leggings", BlockArmor.createMangroveRootsLeggings(), Items.MANGROVE_ROOTS, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Mangrove Roots Boots", BlockArmor.createMangroveRootsBoots(), Items.MANGROVE_ROOTS, Items.LEATHER_BOOTS, 1);
        addBlockArmorRecipeWithCore("Muddy Mangrove Roots Helmet", BlockArmor.createMuddyMangroveRootsHelmet(), Items.MUDDY_MANGROVE_ROOTS, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Muddy Mangrove Roots Chestplate", BlockArmor.createMuddyMangroveRootsChestplate(), Items.MUDDY_MANGROVE_ROOTS, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Muddy Mangrove Roots Leggings", BlockArmor.createMuddyMangroveRootsLeggings(), Items.MUDDY_MANGROVE_ROOTS, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Muddy Mangrove Roots Boots", BlockArmor.createMuddyMangroveRootsBoots(), Items.MUDDY_MANGROVE_ROOTS, Items.LEATHER_BOOTS, 1);

        // Copper/Netherite/Froglight/Basalt Armor
        addBlockArmorRecipeWithCore("Netherite Block Helmet", BlockArmor.createNetheriteBlockHelmet(), Items.NETHERITE_BLOCK, Items.LEATHER_HELMET, 3);
        addBlockArmorRecipeWithCore("Netherite Block Chestplate", BlockArmor.createNetheriteBlockChestplate(), Items.NETHERITE_BLOCK, Items.LEATHER_CHESTPLATE, 3);
        addBlockArmorRecipeWithCore("Netherite Block Leggings", BlockArmor.createNetheriteBlockLeggings(), Items.NETHERITE_BLOCK, Items.LEATHER_LEGGINGS, 3);
        addBlockArmorRecipeWithCore("Netherite Block Boots", BlockArmor.createNetheriteBlockBoots(), Items.NETHERITE_BLOCK, Items.LEATHER_BOOTS, 3);
        addBlockArmorRecipeWithCore("Chiseled Copper Helmet", BlockArmor.createChiseledCopperHelmet(), Items.CHISELED_COPPER, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Chiseled Copper Chestplate", BlockArmor.createChiseledCopperChestplate(), Items.CHISELED_COPPER, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Chiseled Copper Leggings", BlockArmor.createChiseledCopperLeggings(), Items.CHISELED_COPPER, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Chiseled Copper Boots", BlockArmor.createChiseledCopperBoots(), Items.CHISELED_COPPER, Items.LEATHER_BOOTS, 1);
        addBlockArmorRecipeWithCore("Cut Copper Helmet", BlockArmor.createCutCopperHelmet(), Items.CUT_COPPER, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Cut Copper Chestplate", BlockArmor.createCutCopperChestplate(), Items.CUT_COPPER, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Cut Copper Leggings", BlockArmor.createCutCopperLeggings(), Items.CUT_COPPER, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Cut Copper Boots", BlockArmor.createCutCopperBoots(), Items.CUT_COPPER, Items.LEATHER_BOOTS, 1);
        addBlockArmorRecipeWithCore("Exposed Copper Helmet", BlockArmor.createExposedCopperHelmet(), Items.EXPOSED_COPPER, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Exposed Copper Chestplate", BlockArmor.createExposedCopperChestplate(), Items.EXPOSED_COPPER, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Exposed Copper Leggings", BlockArmor.createExposedCopperLeggings(), Items.EXPOSED_COPPER, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Exposed Copper Boots", BlockArmor.createExposedCopperBoots(), Items.EXPOSED_COPPER, Items.LEATHER_BOOTS, 1);
        addBlockArmorRecipeWithCore("Weathered Copper Helmet", BlockArmor.createWeatheredCopperHelmet(), Items.WEATHERED_COPPER, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Weathered Copper Chestplate", BlockArmor.createWeatheredCopperChestplate(), Items.WEATHERED_COPPER, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Weathered Copper Leggings", BlockArmor.createWeatheredCopperLeggings(), Items.WEATHERED_COPPER, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Weathered Copper Boots", BlockArmor.createWeatheredCopperBoots(), Items.WEATHERED_COPPER, Items.LEATHER_BOOTS, 1);
        addBlockArmorRecipeWithCore("Oxidised Copper Helmet", BlockArmor.createOxidisedCopperHelmet(), Items.OXIDIZED_COPPER, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Oxidised Copper Chestplate", BlockArmor.createOxidisedCopperChestplate(), Items.OXIDIZED_COPPER, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Oxidised Copper Leggings", BlockArmor.createOxidisedCopperLeggings(), Items.OXIDIZED_COPPER, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Oxidised Copper Boots", BlockArmor.createOxidisedCopperBoots(), Items.OXIDIZED_COPPER, Items.LEATHER_BOOTS, 1);
        addBlockArmorRecipeWithCore("Waxed Cut Copper Helmet", BlockArmor.createWaxedCutCopperHelmet(), Items.WAXED_CUT_COPPER, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Waxed Cut Copper Chestplate", BlockArmor.createWaxedCutCopperChestplate(), Items.WAXED_CUT_COPPER, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Waxed Cut Copper Leggings", BlockArmor.createWaxedCutCopperLeggings(), Items.WAXED_CUT_COPPER, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Waxed Cut Copper Boots", BlockArmor.createWaxedCutCopperBoots(), Items.WAXED_CUT_COPPER, Items.LEATHER_BOOTS, 1);
        addBlockArmorRecipeWithCore("Polished Basalt Helmet", BlockArmor.createPolishedBasaltHelmet(), Items.POLISHED_BASALT, Items.LEATHER_HELMET, 2);
        addBlockArmorRecipeWithCore("Polished Basalt Chestplate", BlockArmor.createPolishedBasaltChestplate(), Items.POLISHED_BASALT, Items.LEATHER_CHESTPLATE, 2);
        addBlockArmorRecipeWithCore("Polished Basalt Leggings", BlockArmor.createPolishedBasaltLeggings(), Items.POLISHED_BASALT, Items.LEATHER_LEGGINGS, 2);
        addBlockArmorRecipeWithCore("Polished Basalt Boots", BlockArmor.createPolishedBasaltBoots(), Items.POLISHED_BASALT, Items.LEATHER_BOOTS, 2);
        addBlockArmorRecipeWithCore("Verdant Froglight Helmet", BlockArmor.createVerdantFroglightHelmet(), Items.VERDANT_FROGLIGHT, Items.LEATHER_HELMET, 2);
        addBlockArmorRecipeWithCore("Verdant Froglight Chestplate", BlockArmor.createVerdantFroglightChestplate(), Items.VERDANT_FROGLIGHT, Items.LEATHER_CHESTPLATE, 2);
        addBlockArmorRecipeWithCore("Verdant Froglight Leggings", BlockArmor.createVerdantFroglightLeggings(), Items.VERDANT_FROGLIGHT, Items.LEATHER_LEGGINGS, 2);
        addBlockArmorRecipeWithCore("Verdant Froglight Boots", BlockArmor.createVerdantFroglightBoots(), Items.VERDANT_FROGLIGHT, Items.LEATHER_BOOTS, 2);
        addBlockArmorRecipeWithCore("Pearlescent Froglight Helmet", BlockArmor.createPearlescentFroglightHelmet(), Items.PEARLESCENT_FROGLIGHT, Items.LEATHER_HELMET, 2);
        addBlockArmorRecipeWithCore("Pearlescent Froglight Chestplate", BlockArmor.createPearlescentFroglightChestplate(), Items.PEARLESCENT_FROGLIGHT, Items.LEATHER_CHESTPLATE, 2);
        addBlockArmorRecipeWithCore("Pearlescent Froglight Leggings", BlockArmor.createPearlescentFroglightLeggings(), Items.PEARLESCENT_FROGLIGHT, Items.LEATHER_LEGGINGS, 2);
        addBlockArmorRecipeWithCore("Pearlescent Froglight Boots", BlockArmor.createPearlescentFroglightBoots(), Items.PEARLESCENT_FROGLIGHT, Items.LEATHER_BOOTS, 2);
        addBlockArmorRecipeWithCore("Ochre Froglight Helmet", BlockArmor.createOchreFroglightHelmet(), Items.OCHRE_FROGLIGHT, Items.LEATHER_HELMET, 2);
        addBlockArmorRecipeWithCore("Ochre Froglight Chestplate", BlockArmor.createOchreFroglightChestplate(), Items.OCHRE_FROGLIGHT, Items.LEATHER_CHESTPLATE, 2);
        addBlockArmorRecipeWithCore("Ochre Froglight Leggings", BlockArmor.createOchreFroglightLeggings(), Items.OCHRE_FROGLIGHT, Items.LEATHER_LEGGINGS, 2);
        addBlockArmorRecipeWithCore("Ochre Froglight Boots", BlockArmor.createOchreFroglightBoots(), Items.OCHRE_FROGLIGHT, Items.LEATHER_BOOTS, 2);

        // Material Item Armor
        addBlockArmorRecipeWithCore("Iron Nugget Helmet", BlockArmor.createIronNuggetHelmet(), Items.IRON_NUGGET, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Iron Nugget Chestplate", BlockArmor.createIronNuggetChestplate(), Items.IRON_NUGGET, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Iron Nugget Leggings", BlockArmor.createIronNuggetLeggings(), Items.IRON_NUGGET, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Iron Nugget Boots", BlockArmor.createIronNuggetBoots(), Items.IRON_NUGGET, Items.LEATHER_BOOTS, 1);
        addBlockArmorRecipeWithCore("Gold Nugget Helmet", BlockArmor.createGoldNuggetHelmet(), Items.GOLD_NUGGET, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Gold Nugget Chestplate", BlockArmor.createGoldNuggetChestplate(), Items.GOLD_NUGGET, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Gold Nugget Leggings", BlockArmor.createGoldNuggetLeggings(), Items.GOLD_NUGGET, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Gold Nugget Boots", BlockArmor.createGoldNuggetBoots(), Items.GOLD_NUGGET, Items.LEATHER_BOOTS, 1);
        addBlockArmorRecipeWithCore("Copper Ingot Helmet", BlockArmor.createCopperIngotHelmet(), Items.COPPER_INGOT, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Copper Ingot Chestplate", BlockArmor.createCopperIngotChestplate(), Items.COPPER_INGOT, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Copper Ingot Leggings", BlockArmor.createCopperIngotLeggings(), Items.COPPER_INGOT, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Copper Ingot Boots", BlockArmor.createCopperIngotBoots(), Items.COPPER_INGOT, Items.LEATHER_BOOTS, 1);
        addBlockArmorRecipeWithCore("Emerald Helmet", BlockArmor.createEmeraldHelmet(), Items.EMERALD, Items.LEATHER_HELMET, 2);
        addBlockArmorRecipeWithCore("Emerald Chestplate", BlockArmor.createEmeraldChestplate(), Items.EMERALD, Items.LEATHER_CHESTPLATE, 2);
        addBlockArmorRecipeWithCore("Emerald Leggings", BlockArmor.createEmeraldLeggings(), Items.EMERALD, Items.LEATHER_LEGGINGS, 2);
        addBlockArmorRecipeWithCore("Emerald Boots", BlockArmor.createEmeraldBoots(), Items.EMERALD, Items.LEATHER_BOOTS, 2);
        addBlockArmorRecipeWithCore("Lapis Lazuli Helmet", BlockArmor.createLapisLazuliHelmet(), Items.LAPIS_LAZULI, Items.LEATHER_HELMET, 2);
        addBlockArmorRecipeWithCore("Lapis Lazuli Chestplate", BlockArmor.createLapisLazuliChestplate(), Items.LAPIS_LAZULI, Items.LEATHER_CHESTPLATE, 2);
        addBlockArmorRecipeWithCore("Lapis Lazuli Leggings", BlockArmor.createLapisLazuliLeggings(), Items.LAPIS_LAZULI, Items.LEATHER_LEGGINGS, 2);
        addBlockArmorRecipeWithCore("Lapis Lazuli Boots", BlockArmor.createLapisLazuliBoots(), Items.LAPIS_LAZULI, Items.LEATHER_BOOTS, 2);
        addBlockArmorRecipeWithCore("Amethyst Shard Helmet", BlockArmor.createAmethystShardHelmet(), Items.AMETHYST_SHARD, Items.LEATHER_HELMET, 2);
        addBlockArmorRecipeWithCore("Amethyst Shard Chestplate", BlockArmor.createAmethystShardChestplate(), Items.AMETHYST_SHARD, Items.LEATHER_CHESTPLATE, 2);
        addBlockArmorRecipeWithCore("Amethyst Shard Leggings", BlockArmor.createAmethystShardLeggings(), Items.AMETHYST_SHARD, Items.LEATHER_LEGGINGS, 2);
        addBlockArmorRecipeWithCore("Amethyst Shard Boots", BlockArmor.createAmethystShardBoots(), Items.AMETHYST_SHARD, Items.LEATHER_BOOTS, 2);
        addBlockArmorRecipeWithCore("Flint Helmet", BlockArmor.createFlintHelmet(), Items.FLINT, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Flint Chestplate", BlockArmor.createFlintChestplate(), Items.FLINT, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Flint Leggings", BlockArmor.createFlintLeggings(), Items.FLINT, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Flint Boots", BlockArmor.createFlintBoots(), Items.FLINT, Items.LEATHER_BOOTS, 1);
        addBlockArmorRecipeWithCore("Bone Meal Helmet", BlockArmor.createBoneMealHelmet(), Items.BONE_MEAL, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Bone Meal Chestplate", BlockArmor.createBoneMealChestplate(), Items.BONE_MEAL, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Bone Meal Leggings", BlockArmor.createBoneMealLeggings(), Items.BONE_MEAL, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Bone Meal Boots", BlockArmor.createBoneMealBoots(), Items.BONE_MEAL, Items.LEATHER_BOOTS, 1);
        addBlockArmorRecipeWithCore("Charcoal Helmet", BlockArmor.createCharcoalHelmet(), Items.CHARCOAL, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Charcoal Chestplate", BlockArmor.createCharcoalChestplate(), Items.CHARCOAL, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Charcoal Leggings", BlockArmor.createCharcoalLeggings(), Items.CHARCOAL, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Charcoal Boots", BlockArmor.createCharcoalBoots(), Items.CHARCOAL, Items.LEATHER_BOOTS, 1);
        addBlockArmorRecipeWithCore("End Stone Helmet", BlockArmor.createEndStoneHelmet(), Items.END_STONE, Items.LEATHER_HELMET, 2);
        addBlockArmorRecipeWithCore("End Stone Chestplate", BlockArmor.createEndStoneChestplate(), Items.END_STONE, Items.LEATHER_CHESTPLATE, 2);
        addBlockArmorRecipeWithCore("End Stone Leggings", BlockArmor.createEndStoneLeggings(), Items.END_STONE, Items.LEATHER_LEGGINGS, 2);
        addBlockArmorRecipeWithCore("End Stone Boots", BlockArmor.createEndStoneBoots(), Items.END_STONE, Items.LEATHER_BOOTS, 2);
        addBlockArmorRecipeWithCore("Snow Block Helmet", BlockArmor.createSnowBlockHelmet(), Items.SNOW_BLOCK, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Snow Block Chestplate", BlockArmor.createSnowBlockChestplate(), Items.SNOW_BLOCK, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Snow Block Leggings", BlockArmor.createSnowBlockLeggings(), Items.SNOW_BLOCK, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Snow Block Boots", BlockArmor.createSnowBlockBoots(), Items.SNOW_BLOCK, Items.LEATHER_BOOTS, 1);

        // Additional Block Armor recipes - Diamond/Emerald/Gold/Iron/Lapis/Copper blocks
        addBlockArmorRecipeWithCore("Diamond Block Helmet", BlockArmor.createDiamondBlockHelmet(), Items.DIAMOND_BLOCK, Items.LEATHER_HELMET, 3);
        addBlockArmorRecipeWithCore("Diamond Block Chestplate", BlockArmor.createDiamondBlockChestplate(), Items.DIAMOND_BLOCK, Items.LEATHER_CHESTPLATE, 3);
        addBlockArmorRecipeWithCore("Diamond Block Leggings", BlockArmor.createDiamondBlockLeggings(), Items.DIAMOND_BLOCK, Items.LEATHER_LEGGINGS, 3);
        addBlockArmorRecipeWithCore("Diamond Block Boots", BlockArmor.createDiamondBlockBoots(), Items.DIAMOND_BLOCK, Items.LEATHER_BOOTS, 3);
        addBlockArmorRecipeWithCore("Emerald Block Helmet", BlockArmor.createEmeraldBlockHelmet(), Items.EMERALD_BLOCK, Items.LEATHER_HELMET, 2);
        addBlockArmorRecipeWithCore("Emerald Block Chestplate", BlockArmor.createEmeraldBlockChestplate(), Items.EMERALD_BLOCK, Items.LEATHER_CHESTPLATE, 2);
        addBlockArmorRecipeWithCore("Emerald Block Leggings", BlockArmor.createEmeraldBlockLeggings(), Items.EMERALD_BLOCK, Items.LEATHER_LEGGINGS, 2);
        addBlockArmorRecipeWithCore("Emerald Block Boots", BlockArmor.createEmeraldBlockBoots(), Items.EMERALD_BLOCK, Items.LEATHER_BOOTS, 2);
        addBlockArmorRecipeWithCore("Gold Block Helmet", BlockArmor.createGoldBlockHelmet(), Items.GOLD_BLOCK, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Gold Block Chestplate", BlockArmor.createGoldBlockChestplate(), Items.GOLD_BLOCK, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Gold Block Leggings", BlockArmor.createGoldBlockLeggings(), Items.GOLD_BLOCK, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Gold Block Boots", BlockArmor.createGoldBlockBoots(), Items.GOLD_BLOCK, Items.LEATHER_BOOTS, 1);
        addBlockArmorRecipeWithCore("Iron Block Helmet", BlockArmor.createIronBlockHelmet(), Items.IRON_BLOCK, Items.LEATHER_HELMET, 2);
        addBlockArmorRecipeWithCore("Iron Block Chestplate", BlockArmor.createIronBlockChestplate(), Items.IRON_BLOCK, Items.LEATHER_CHESTPLATE, 2);
        addBlockArmorRecipeWithCore("Iron Block Leggings", BlockArmor.createIronBlockLeggings(), Items.IRON_BLOCK, Items.LEATHER_LEGGINGS, 2);
        addBlockArmorRecipeWithCore("Iron Block Boots", BlockArmor.createIronBlockBoots(), Items.IRON_BLOCK, Items.LEATHER_BOOTS, 2);
        addBlockArmorRecipeWithCore("Lapis Block Helmet", BlockArmor.createLapisBlockHelmet(), Items.LAPIS_BLOCK, Items.LEATHER_HELMET, 2);
        addBlockArmorRecipeWithCore("Lapis Block Chestplate", BlockArmor.createLapisBlockChestplate(), Items.LAPIS_BLOCK, Items.LEATHER_CHESTPLATE, 2);
        addBlockArmorRecipeWithCore("Lapis Block Leggings", BlockArmor.createLapisBlockLeggings(), Items.LAPIS_BLOCK, Items.LEATHER_LEGGINGS, 2);
        addBlockArmorRecipeWithCore("Lapis Block Boots", BlockArmor.createLapisBlockBoots(), Items.LAPIS_BLOCK, Items.LEATHER_BOOTS, 2);
        addBlockArmorRecipeWithCore("Copper Block Helmet", BlockArmor.createCopperBlockHelmet(), Items.COPPER_BLOCK, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Copper Block Chestplate", BlockArmor.createCopperBlockChestplate(), Items.COPPER_BLOCK, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Copper Block Leggings", BlockArmor.createCopperBlockLeggings(), Items.COPPER_BLOCK, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Copper Block Boots", BlockArmor.createCopperBlockBoots(), Items.COPPER_BLOCK, Items.LEATHER_BOOTS, 1);
        addBlockArmorRecipeWithCore("Ancient Debris Helmet", BlockArmor.createAncientDebrisHelmet(), Items.ANCIENT_DEBRIS, Items.LEATHER_HELMET, 3);
        addBlockArmorRecipeWithCore("Ancient Debris Chestplate", BlockArmor.createAncientDebrisChestplate(), Items.ANCIENT_DEBRIS, Items.LEATHER_CHESTPLATE, 3);
        addBlockArmorRecipeWithCore("Ancient Debris Leggings", BlockArmor.createAncientDebrisLeggings(), Items.ANCIENT_DEBRIS, Items.LEATHER_LEGGINGS, 3);
        addBlockArmorRecipeWithCore("Ancient Debris Boots", BlockArmor.createAncientDebrisBoots(), Items.ANCIENT_DEBRIS, Items.LEATHER_BOOTS, 3);
        addBlockArmorRecipeWithCore("Basalt Helmet", BlockArmor.createBasaltHelmet(), Items.BASALT, Items.LEATHER_HELMET, 2);
        addBlockArmorRecipeWithCore("Basalt Chestplate", BlockArmor.createBasaltChestplate(), Items.BASALT, Items.LEATHER_CHESTPLATE, 2);
        addBlockArmorRecipeWithCore("Basalt Leggings", BlockArmor.createBasaltLeggings(), Items.BASALT, Items.LEATHER_LEGGINGS, 2);
        addBlockArmorRecipeWithCore("Basalt Boots", BlockArmor.createBasaltBoots(), Items.BASALT, Items.LEATHER_BOOTS, 2);
        addBlockArmorRecipeWithCore("Blackstone Helmet", BlockArmor.createBlackstoneHelmet(), Items.BLACKSTONE, Items.LEATHER_HELMET, 2);
        addBlockArmorRecipeWithCore("Blackstone Chestplate", BlockArmor.createBlackstoneChestplate(), Items.BLACKSTONE, Items.LEATHER_CHESTPLATE, 2);
        addBlockArmorRecipeWithCore("Blackstone Leggings", BlockArmor.createBlackstoneLeggings(), Items.BLACKSTONE, Items.LEATHER_LEGGINGS, 2);
        addBlockArmorRecipeWithCore("Blackstone Boots", BlockArmor.createBlackstoneBoots(), Items.BLACKSTONE, Items.LEATHER_BOOTS, 2);
        addBlockArmorRecipeWithCore("Bone Block Helmet", BlockArmor.createBoneBlockHelmet(), Items.BONE_BLOCK, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Bone Block Chestplate", BlockArmor.createBoneBlockChestplate(), Items.BONE_BLOCK, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Bone Block Leggings", BlockArmor.createBoneBlockLeggings(), Items.BONE_BLOCK, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Bone Block Boots", BlockArmor.createBoneBlockBoots(), Items.BONE_BLOCK, Items.LEATHER_BOOTS, 1);
        addBlockArmorRecipeWithCore("Brick Helmet", BlockArmor.createBrickHelmet(), Items.BRICKS, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Brick Chestplate", BlockArmor.createBrickChestplate(), Items.BRICKS, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Brick Leggings", BlockArmor.createBrickLeggings(), Items.BRICKS, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Brick Boots", BlockArmor.createBrickBoots(), Items.BRICKS, Items.LEATHER_BOOTS, 1);
        addBlockArmorRecipeWithCore("Cactus Helmet", BlockArmor.createCactusHelmet(), Items.CACTUS, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Cactus Chestplate", BlockArmor.createCactusChestplate(), Items.CACTUS, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Cactus Leggings", BlockArmor.createCactusLeggings(), Items.CACTUS, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Cactus Boots", BlockArmor.createCactusBoots(), Items.CACTUS, Items.LEATHER_BOOTS, 1);
        addBlockArmorRecipeWithCore("Calcite Helmet", BlockArmor.createCalciteHelmet(), Items.CALCITE, Items.LEATHER_HELMET, 2);
        addBlockArmorRecipeWithCore("Calcite Chestplate", BlockArmor.createCalciteChestplate(), Items.CALCITE, Items.LEATHER_CHESTPLATE, 2);
        addBlockArmorRecipeWithCore("Calcite Leggings", BlockArmor.createCalciteLeggings(), Items.CALCITE, Items.LEATHER_LEGGINGS, 2);
        addBlockArmorRecipeWithCore("Calcite Boots", BlockArmor.createCalciteBoots(), Items.CALCITE, Items.LEATHER_BOOTS, 2);
        addBlockArmorRecipeWithCore("Deepslate Helmet", BlockArmor.createDeepslateHelmet(), Items.DEEPSLATE, Items.LEATHER_HELMET, 2);
        addBlockArmorRecipeWithCore("Deepslate Chestplate", BlockArmor.createDeepslateChestplate(), Items.DEEPSLATE, Items.LEATHER_CHESTPLATE, 2);
        addBlockArmorRecipeWithCore("Deepslate Leggings", BlockArmor.createDeepslateLeggings(), Items.DEEPSLATE, Items.LEATHER_LEGGINGS, 2);
        addBlockArmorRecipeWithCore("Deepslate Boots", BlockArmor.createDeepslateBoots(), Items.DEEPSLATE, Items.LEATHER_BOOTS, 2);
        addBlockArmorRecipeWithCore("Dripstone Helmet", BlockArmor.createDripstoneHelmet(), Items.DRIPSTONE_BLOCK, Items.LEATHER_HELMET, 2);
        addBlockArmorRecipeWithCore("Dripstone Chestplate", BlockArmor.createDripstoneChestplate(), Items.DRIPSTONE_BLOCK, Items.LEATHER_CHESTPLATE, 2);
        addBlockArmorRecipeWithCore("Dripstone Leggings", BlockArmor.createDripstoneLeggings(), Items.DRIPSTONE_BLOCK, Items.LEATHER_LEGGINGS, 2);
        addBlockArmorRecipeWithCore("Dripstone Boots", BlockArmor.createDripstoneBoots(), Items.DRIPSTONE_BLOCK, Items.LEATHER_BOOTS, 2);
        addBlockArmorRecipeWithCore("Nether Brick Helmet", BlockArmor.createNetherBrickHelmet(), Items.NETHER_BRICKS, Items.LEATHER_HELMET, 2);
        addBlockArmorRecipeWithCore("Nether Brick Chestplate", BlockArmor.createNetherBrickChestplate(), Items.NETHER_BRICKS, Items.LEATHER_CHESTPLATE, 2);
        addBlockArmorRecipeWithCore("Nether Brick Leggings", BlockArmor.createNetherBrickLeggings(), Items.NETHER_BRICKS, Items.LEATHER_LEGGINGS, 2);
        addBlockArmorRecipeWithCore("Nether Brick Boots", BlockArmor.createNetherBrickBoots(), Items.NETHER_BRICKS, Items.LEATHER_BOOTS, 2);
        addBlockArmorRecipeWithCore("Purpur Helmet", BlockArmor.createPurpurHelmet(), Items.PURPUR_BLOCK, Items.LEATHER_HELMET, 2);
        addBlockArmorRecipeWithCore("Purpur Chestplate", BlockArmor.createPurpurChestplate(), Items.PURPUR_BLOCK, Items.LEATHER_CHESTPLATE, 2);
        addBlockArmorRecipeWithCore("Purpur Leggings", BlockArmor.createPurpurLeggings(), Items.PURPUR_BLOCK, Items.LEATHER_LEGGINGS, 2);
        addBlockArmorRecipeWithCore("Purpur Boots", BlockArmor.createPurpurBoots(), Items.PURPUR_BLOCK, Items.LEATHER_BOOTS, 2);
        addBlockArmorRecipeWithCore("Sculk Helmet", BlockArmor.createSculkHelmet(), Items.SCULK, Items.LEATHER_HELMET, 2);
        addBlockArmorRecipeWithCore("Sculk Chestplate", BlockArmor.createSculkChestplate(), Items.SCULK, Items.LEATHER_CHESTPLATE, 2);
        addBlockArmorRecipeWithCore("Sculk Leggings", BlockArmor.createSculkLeggings(), Items.SCULK, Items.LEATHER_LEGGINGS, 2);
        addBlockArmorRecipeWithCore("Sculk Boots", BlockArmor.createSculkBoots(), Items.SCULK, Items.LEATHER_BOOTS, 2);
        addBlockArmorRecipeWithCore("Shroomlight Helmet", BlockArmor.createShroomlightHelmet(), Items.SHROOMLIGHT, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Shroomlight Chestplate", BlockArmor.createShroomlightChestplate(), Items.SHROOMLIGHT, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Shroomlight Leggings", BlockArmor.createShroomlightLeggings(), Items.SHROOMLIGHT, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Shroomlight Boots", BlockArmor.createShroomlightBoots(), Items.SHROOMLIGHT, Items.LEATHER_BOOTS, 1);
        addBlockArmorRecipeWithCore("Smooth Stone Helmet", BlockArmor.createSmoothStoneHelmet(), Items.SMOOTH_STONE, Items.LEATHER_HELMET, 2);
        addBlockArmorRecipeWithCore("Smooth Stone Chestplate", BlockArmor.createSmoothStoneChestplate(), Items.SMOOTH_STONE, Items.LEATHER_CHESTPLATE, 2);
        addBlockArmorRecipeWithCore("Smooth Stone Leggings", BlockArmor.createSmoothStoneLeggings(), Items.SMOOTH_STONE, Items.LEATHER_LEGGINGS, 2);
        addBlockArmorRecipeWithCore("Smooth Stone Boots", BlockArmor.createSmoothStoneBoots(), Items.SMOOTH_STONE, Items.LEATHER_BOOTS, 2);
        addBlockArmorRecipeWithCore("Soul Soil Helmet", BlockArmor.createSoulSoilHelmet(), Items.SOUL_SOIL, Items.LEATHER_HELMET, 2);
        addBlockArmorRecipeWithCore("Soul Soil Chestplate", BlockArmor.createSoulSoilChestplate(), Items.SOUL_SOIL, Items.LEATHER_CHESTPLATE, 2);
        addBlockArmorRecipeWithCore("Soul Soil Leggings", BlockArmor.createSoulSoilLeggings(), Items.SOUL_SOIL, Items.LEATHER_LEGGINGS, 2);
        addBlockArmorRecipeWithCore("Soul Soil Boots", BlockArmor.createSoulSoilBoots(), Items.SOUL_SOIL, Items.LEATHER_BOOTS, 2);
        addBlockArmorRecipeWithCore("Sponge Helmet", BlockArmor.createSpongeHelmet(), Items.SPONGE, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Sponge Chestplate", BlockArmor.createSpongeChestplate(), Items.SPONGE, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Sponge Leggings", BlockArmor.createSpongeLeggings(), Items.SPONGE, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Sponge Boots", BlockArmor.createSpongeBoots(), Items.SPONGE, Items.LEATHER_BOOTS, 1);

        // Missing armor sets that were added later
        addBlockArmorRecipeWithCore("Hay Block Helmet", BlockArmor.createHayBlockHelmet(), Items.HAY_BLOCK, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Hay Block Chestplate", BlockArmor.createHayBlockChestplate(), Items.HAY_BLOCK, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Hay Block Leggings", BlockArmor.createHayBlockLeggings(), Items.HAY_BLOCK, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Hay Block Boots", BlockArmor.createHayBlockBoots(), Items.HAY_BLOCK, Items.LEATHER_BOOTS, 1);
        addBlockArmorRecipeWithCore("Honeycomb Block Helmet", BlockArmor.createHoneycombBlockHelmet(), Items.HONEYCOMB_BLOCK, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Honeycomb Block Chestplate", BlockArmor.createHoneycombBlockChestplate(), Items.HONEYCOMB_BLOCK, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Honeycomb Block Leggings", BlockArmor.createHoneycombBlockLeggings(), Items.HONEYCOMB_BLOCK, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Honeycomb Block Boots", BlockArmor.createHoneycombBlockBoots(), Items.HONEYCOMB_BLOCK, Items.LEATHER_BOOTS, 1);
        addBlockArmorRecipeWithCore("Lily Pad Helmet", BlockArmor.createLilyPadHelmet(), Items.LILY_PAD, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Lily Pad Chestplate", BlockArmor.createLilyPadChestplate(), Items.LILY_PAD, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Lily Pad Leggings", BlockArmor.createLilyPadLeggings(), Items.LILY_PAD, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Lily Pad Boots", BlockArmor.createLilyPadBoots(), Items.LILY_PAD, Items.LEATHER_BOOTS, 1);
        addBlockArmorRecipeWithCore("Melon Helmet", BlockArmor.createMelonHelmet(), Items.MELON, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Melon Chestplate", BlockArmor.createMelonChestplate(), Items.MELON, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Melon Leggings", BlockArmor.createMelonLeggings(), Items.MELON, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Melon Boots", BlockArmor.createMelonBoots(), Items.MELON, Items.LEATHER_BOOTS, 1);
        addBlockArmorRecipeWithCore("Moss Block Helmet", BlockArmor.createMossBlockHelmet(), Items.MOSS_BLOCK, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Moss Block Chestplate", BlockArmor.createMossBlockChestplate(), Items.MOSS_BLOCK, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Moss Block Leggings", BlockArmor.createMossBlockLeggings(), Items.MOSS_BLOCK, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Moss Block Boots", BlockArmor.createMossBlockBoots(), Items.MOSS_BLOCK, Items.LEATHER_BOOTS, 1);
        addBlockArmorRecipeWithCore("Mycelium Helmet", BlockArmor.createMyceliumHelmet(), Items.MYCELIUM, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Mycelium Chestplate", BlockArmor.createMyceliumChestplate(), Items.MYCELIUM, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Mycelium Leggings", BlockArmor.createMyceliumLeggings(), Items.MYCELIUM, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Mycelium Boots", BlockArmor.createMyceliumBoots(), Items.MYCELIUM, Items.LEATHER_BOOTS, 1);
        addBlockArmorRecipeWithCore("Pumpkin Helmet", BlockArmor.createPumpkinHelmet(), Items.PUMPKIN, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Pumpkin Chestplate", BlockArmor.createPumpkinChestplate(), Items.PUMPKIN, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Pumpkin Leggings", BlockArmor.createPumpkinLeggings(), Items.PUMPKIN, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Pumpkin Boots", BlockArmor.createPumpkinBoots(), Items.PUMPKIN, Items.LEATHER_BOOTS, 1);
        addBlockArmorRecipeWithCore("Sand Helmet", BlockArmor.createSandHelmet(), Items.SAND, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Sand Chestplate", BlockArmor.createSandChestplate(), Items.SAND, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Sand Leggings", BlockArmor.createSandLeggings(), Items.SAND, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Sand Boots", BlockArmor.createSandBoots(), Items.SAND, Items.LEATHER_BOOTS, 1);
        addBlockArmorRecipeWithCore("Slime Block Helmet", BlockArmor.createSlimeHelmet(), Items.SLIME_BLOCK, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Slime Block Chestplate", BlockArmor.createSlimeChestplate(), Items.SLIME_BLOCK, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Slime Block Leggings", BlockArmor.createSlimeLeggings(), Items.SLIME_BLOCK, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Slime Block Boots", BlockArmor.createSlimeBoots(), Items.SLIME_BLOCK, Items.LEATHER_BOOTS, 1);
        addBlockArmorRecipeWithCore("Target Helmet", BlockArmor.createTargetHelmet(), Items.TARGET, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Target Chestplate", BlockArmor.createTargetChestplate(), Items.TARGET, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Target Leggings", BlockArmor.createTargetLeggings(), Items.TARGET, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Target Boots", BlockArmor.createTargetBoots(), Items.TARGET, Items.LEATHER_BOOTS, 1);
        addBlockArmorRecipeWithCore("TNT Helmet", BlockArmor.createTntHelmet(), Items.TNT, Items.LEATHER_HELMET, 2);
        addBlockArmorRecipeWithCore("TNT Chestplate", BlockArmor.createTntChestplate(), Items.TNT, Items.LEATHER_CHESTPLATE, 2);
        addBlockArmorRecipeWithCore("TNT Leggings", BlockArmor.createTntLeggings(), Items.TNT, Items.LEATHER_LEGGINGS, 2);
        addBlockArmorRecipeWithCore("TNT Boots", BlockArmor.createTntBoots(), Items.TNT, Items.LEATHER_BOOTS, 2);
        addBlockArmorRecipeWithCore("Warped Stem Helmet", BlockArmor.createWarpedHelmet(), Items.WARPED_STEM, Items.LEATHER_HELMET, 2);
        addBlockArmorRecipeWithCore("Warped Stem Chestplate", BlockArmor.createWarpedChestplate(), Items.WARPED_STEM, Items.LEATHER_CHESTPLATE, 2);
        addBlockArmorRecipeWithCore("Warped Stem Leggings", BlockArmor.createWarpedLeggings(), Items.WARPED_STEM, Items.LEATHER_LEGGINGS, 2);
        addBlockArmorRecipeWithCore("Warped Stem Boots", BlockArmor.createWarpedBoots(), Items.WARPED_STEM, Items.LEATHER_BOOTS, 2);
        addBlockArmorRecipeWithCore("Wet Sponge Helmet", BlockArmor.createWetSpongeHelmet(), Items.WET_SPONGE, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Wet Sponge Chestplate", BlockArmor.createWetSpongeChestplate(), Items.WET_SPONGE, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Wet Sponge Leggings", BlockArmor.createWetSpongeLeggings(), Items.WET_SPONGE, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Wet Sponge Boots", BlockArmor.createWetSpongeBoots(), Items.WET_SPONGE, Items.LEATHER_BOOTS, 1);
        
        // NEW BLOCK ARMOR RECIPES
        addBlockArmorRecipeWithCore("Crimson Stem Helmet", BlockArmor.createCrimsonStemHelmet(), Items.CRIMSON_STEM, Items.LEATHER_HELMET, 3);
        addBlockArmorRecipeWithCore("Crimson Stem Chestplate", BlockArmor.createCrimsonStemChestplate(), Items.CRIMSON_STEM, Items.LEATHER_CHESTPLATE, 3);
        addBlockArmorRecipeWithCore("Crimson Stem Leggings", BlockArmor.createCrimsonStemLeggings(), Items.CRIMSON_STEM, Items.LEATHER_LEGGINGS, 3);
        addBlockArmorRecipeWithCore("Crimson Stem Boots", BlockArmor.createCrimsonStemBoots(), Items.CRIMSON_STEM, Items.LEATHER_BOOTS, 3);
        addBlockArmorRecipeWithCore("Crying Obsidian Helmet", BlockArmor.createCryingObsidianHelmet(), Items.CRYING_OBSIDIAN, Items.LEATHER_HELMET, 3);
        addBlockArmorRecipeWithCore("Crying Obsidian Chestplate", BlockArmor.createCryingObsidianChestplate(), Items.CRYING_OBSIDIAN, Items.LEATHER_CHESTPLATE, 3);
        addBlockArmorRecipeWithCore("Crying Obsidian Leggings", BlockArmor.createCryingObsidianLeggings(), Items.CRYING_OBSIDIAN, Items.LEATHER_LEGGINGS, 3);
        addBlockArmorRecipeWithCore("Crying Obsidian Boots", BlockArmor.createCryingObsidianBoots(), Items.CRYING_OBSIDIAN, Items.LEATHER_BOOTS, 3);
        addBlockArmorRecipeWithCore("Gilded Blackstone Helmet", BlockArmor.createGildedBlackstoneHelmet(), Items.GILDED_BLACKSTONE, Items.LEATHER_HELMET, 3);
        addBlockArmorRecipeWithCore("Gilded Blackstone Chestplate", BlockArmor.createGildedBlackstoneChestplate(), Items.GILDED_BLACKSTONE, Items.LEATHER_CHESTPLATE, 3);
        addBlockArmorRecipeWithCore("Gilded Blackstone Leggings", BlockArmor.createGildedBlackstoneLeggings(), Items.GILDED_BLACKSTONE, Items.LEATHER_LEGGINGS, 3);
        addBlockArmorRecipeWithCore("Gilded Blackstone Boots", BlockArmor.createGildedBlackstoneBoots(), Items.GILDED_BLACKSTONE, Items.LEATHER_BOOTS, 3);
        addBlockArmorRecipeWithCore("Granite Helmet", BlockArmor.createGraniteHelmet(), Items.GRANITE, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Granite Chestplate", BlockArmor.createGraniteChestplate(), Items.GRANITE, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Granite Leggings", BlockArmor.createGraniteLeggings(), Items.GRANITE, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Granite Boots", BlockArmor.createGraniteBoots(), Items.GRANITE, Items.LEATHER_BOOTS, 1);
        addBlockArmorRecipeWithCore("Diorite Helmet", BlockArmor.createDioriteHelmet(), Items.DIORITE, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Diorite Chestplate", BlockArmor.createDioriteChestplate(), Items.DIORITE, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Diorite Leggings", BlockArmor.createDioriteLeggings(), Items.DIORITE, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Diorite Boots", BlockArmor.createDioriteBoots(), Items.DIORITE, Items.LEATHER_BOOTS, 1);
        addBlockArmorRecipeWithCore("Andesite Helmet", BlockArmor.createAndesiteHelmet(), Items.ANDESITE, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Andesite Chestplate", BlockArmor.createAndesiteChestplate(), Items.ANDESITE, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Andesite Leggings", BlockArmor.createAndesiteLeggings(), Items.ANDESITE, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Andesite Boots", BlockArmor.createAndesiteBoots(), Items.ANDESITE, Items.LEATHER_BOOTS, 1);
        addBlockArmorRecipeWithCore("Polished Granite Helmet", BlockArmor.createPolishedGraniteHelmet(), Items.POLISHED_GRANITE, Items.LEATHER_HELMET, 2);
        addBlockArmorRecipeWithCore("Polished Granite Chestplate", BlockArmor.createPolishedGraniteChestplate(), Items.POLISHED_GRANITE, Items.LEATHER_CHESTPLATE, 2);
        addBlockArmorRecipeWithCore("Polished Granite Leggings", BlockArmor.createPolishedGraniteLeggings(), Items.POLISHED_GRANITE, Items.LEATHER_LEGGINGS, 2);
        addBlockArmorRecipeWithCore("Polished Granite Boots", BlockArmor.createPolishedGraniteBoots(), Items.POLISHED_GRANITE, Items.LEATHER_BOOTS, 2);
        addBlockArmorRecipeWithCore("Polished Diorite Helmet", BlockArmor.createPolishedDioriteHelmet(), Items.POLISHED_DIORITE, Items.LEATHER_HELMET, 2);
        addBlockArmorRecipeWithCore("Polished Diorite Chestplate", BlockArmor.createPolishedDioriteChestplate(), Items.POLISHED_DIORITE, Items.LEATHER_CHESTPLATE, 2);
        addBlockArmorRecipeWithCore("Polished Diorite Leggings", BlockArmor.createPolishedDioriteLeggings(), Items.POLISHED_DIORITE, Items.LEATHER_LEGGINGS, 2);
        addBlockArmorRecipeWithCore("Polished Diorite Boots", BlockArmor.createPolishedDioriteBoots(), Items.POLISHED_DIORITE, Items.LEATHER_BOOTS, 2);
        addBlockArmorRecipeWithCore("Polished Andesite Helmet", BlockArmor.createPolishedAndesiteHelmet(), Items.POLISHED_ANDESITE, Items.LEATHER_HELMET, 2);
        addBlockArmorRecipeWithCore("Polished Andesite Chestplate", BlockArmor.createPolishedAndesiteChestplate(), Items.POLISHED_ANDESITE, Items.LEATHER_CHESTPLATE, 2);
        addBlockArmorRecipeWithCore("Polished Andesite Leggings", BlockArmor.createPolishedAndesiteLeggings(), Items.POLISHED_ANDESITE, Items.LEATHER_LEGGINGS, 2);
        addBlockArmorRecipeWithCore("Polished Andesite Boots", BlockArmor.createPolishedAndesiteBoots(), Items.POLISHED_ANDESITE, Items.LEATHER_BOOTS, 2);
        addBlockArmorRecipeWithCore("Packed Ice Helmet", BlockArmor.createPackedIceHelmet(), Items.PACKED_ICE, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Packed Ice Chestplate", BlockArmor.createPackedIceChestplate(), Items.PACKED_ICE, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Packed Ice Leggings", BlockArmor.createPackedIceLeggings(), Items.PACKED_ICE, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Packed Ice Boots", BlockArmor.createPackedIceBoots(), Items.PACKED_ICE, Items.LEATHER_BOOTS, 1);
        addBlockArmorRecipeWithCore("Blue Ice Helmet", BlockArmor.createBlueIceHelmet(), Items.BLUE_ICE, Items.LEATHER_HELMET, 3);
        addBlockArmorRecipeWithCore("Blue Ice Chestplate", BlockArmor.createBlueIceChestplate(), Items.BLUE_ICE, Items.LEATHER_CHESTPLATE, 3);
        addBlockArmorRecipeWithCore("Blue Ice Leggings", BlockArmor.createBlueIceLeggings(), Items.BLUE_ICE, Items.LEATHER_LEGGINGS, 3);
        addBlockArmorRecipeWithCore("Blue Ice Boots", BlockArmor.createBlueIceBoots(), Items.BLUE_ICE, Items.LEATHER_BOOTS, 3);
        addBlockArmorRecipeWithCore("Nether Gold Ore Helmet", BlockArmor.createNetherGoldOreHelmet(), Items.NETHER_GOLD_ORE, Items.LEATHER_HELMET, 3);
        addBlockArmorRecipeWithCore("Nether Gold Ore Chestplate", BlockArmor.createNetherGoldOreChestplate(), Items.NETHER_GOLD_ORE, Items.LEATHER_CHESTPLATE, 3);
        addBlockArmorRecipeWithCore("Nether Gold Ore Leggings", BlockArmor.createNetherGoldOreLeggings(), Items.NETHER_GOLD_ORE, Items.LEATHER_LEGGINGS, 3);
        addBlockArmorRecipeWithCore("Nether Gold Ore Boots", BlockArmor.createNetherGoldOreBoots(), Items.NETHER_GOLD_ORE, Items.LEATHER_BOOTS, 3);
        addBlockArmorRecipeWithCore("Dark Oak Log Helmet", BlockArmor.createDarkOakLogHelmet(), Items.DARK_OAK_LOG, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Dark Oak Log Chestplate", BlockArmor.createDarkOakLogChestplate(), Items.DARK_OAK_LOG, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Dark Oak Log Leggings", BlockArmor.createDarkOakLogLeggings(), Items.DARK_OAK_LOG, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Dark Oak Log Boots", BlockArmor.createDarkOakLogBoots(), Items.DARK_OAK_LOG, Items.LEATHER_BOOTS, 1);
        addBlockArmorRecipeWithCore("Jungle Log Helmet", BlockArmor.createJungleLogHelmet(), Items.JUNGLE_LOG, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Jungle Log Chestplate", BlockArmor.createJungleLogChestplate(), Items.JUNGLE_LOG, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Jungle Log Leggings", BlockArmor.createJungleLogLeggings(), Items.JUNGLE_LOG, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Jungle Log Boots", BlockArmor.createJungleLogBoots(), Items.JUNGLE_LOG, Items.LEATHER_BOOTS, 1);
        addBlockArmorRecipeWithCore("Acacia Log Helmet", BlockArmor.createAcaciaLogHelmet(), Items.ACACIA_LOG, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Acacia Log Chestplate", BlockArmor.createAcaciaLogChestplate(), Items.ACACIA_LOG, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Acacia Log Leggings", BlockArmor.createAcaciaLogLeggings(), Items.ACACIA_LOG, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Acacia Log Boots", BlockArmor.createAcaciaLogBoots(), Items.ACACIA_LOG, Items.LEATHER_BOOTS, 1);
        addBlockArmorRecipeWithCore("Mangrove Log Helmet", BlockArmor.createMangroveLogHelmet(), Items.MANGROVE_LOG, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Mangrove Log Chestplate", BlockArmor.createMangroveLogChestplate(), Items.MANGROVE_LOG, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Mangrove Log Leggings", BlockArmor.createMangroveLogLeggings(), Items.MANGROVE_LOG, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Mangrove Log Boots", BlockArmor.createMangroveLogBoots(), Items.MANGROVE_LOG, Items.LEATHER_BOOTS, 1);
        addBlockArmorRecipeWithCore("Cherry Log Helmet", BlockArmor.createCherryLogHelmet(), Items.CHERRY_LOG, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Cherry Log Chestplate", BlockArmor.createCherryLogChestplate(), Items.CHERRY_LOG, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Cherry Log Leggings", BlockArmor.createCherryLogLeggings(), Items.CHERRY_LOG, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Cherry Log Boots", BlockArmor.createCherryLogBoots(), Items.CHERRY_LOG, Items.LEATHER_BOOTS, 1);
        addBlockArmorRecipeWithCore("Bamboo Block Helmet", BlockArmor.createBambooBlockHelmet(), Items.BAMBOO_BLOCK, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Bamboo Block Chestplate", BlockArmor.createBambooBlockChestplate(), Items.BAMBOO_BLOCK, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Bamboo Block Leggings", BlockArmor.createBambooBlockLeggings(), Items.BAMBOO_BLOCK, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Bamboo Block Boots", BlockArmor.createBambooBlockBoots(), Items.BAMBOO_BLOCK, Items.LEATHER_BOOTS, 1);
        addBlockArmorRecipeWithCore("Tuff Helmet", BlockArmor.createTuffHelmet(), Items.TUFF, Items.LEATHER_HELMET, 1);
        addBlockArmorRecipeWithCore("Tuff Chestplate", BlockArmor.createTuffChestplate(), Items.TUFF, Items.LEATHER_CHESTPLATE, 1);
        addBlockArmorRecipeWithCore("Tuff Leggings", BlockArmor.createTuffLeggings(), Items.TUFF, Items.LEATHER_LEGGINGS, 1);
        addBlockArmorRecipeWithCore("Tuff Boots", BlockArmor.createTuffBoots(), Items.TUFF, Items.LEATHER_BOOTS, 1);
        addBlockArmorRecipeWithCore("Polished Tuff Helmet", BlockArmor.createPolishedTuffHelmet(), Items.POLISHED_TUFF, Items.LEATHER_HELMET, 2);
        addBlockArmorRecipeWithCore("Polished Tuff Chestplate", BlockArmor.createPolishedTuffChestplate(), Items.POLISHED_TUFF, Items.LEATHER_CHESTPLATE, 2);
        addBlockArmorRecipeWithCore("Polished Tuff Leggings", BlockArmor.createPolishedTuffLeggings(), Items.POLISHED_TUFF, Items.LEATHER_LEGGINGS, 2);
        addBlockArmorRecipeWithCore("Polished Tuff Boots", BlockArmor.createPolishedTuffBoots(), Items.POLISHED_TUFF, Items.LEATHER_BOOTS, 2);
        addBlockArmorRecipeWithCore("Nether Wart Block Helmet", BlockArmor.createNetherWartBlockHelmet(), Items.NETHER_WART_BLOCK, Items.LEATHER_HELMET, 3);
        addBlockArmorRecipeWithCore("Nether Wart Block Chestplate", BlockArmor.createNetherWartBlockChestplate(), Items.NETHER_WART_BLOCK, Items.LEATHER_CHESTPLATE, 3);
        addBlockArmorRecipeWithCore("Nether Wart Block Leggings", BlockArmor.createNetherWartBlockLeggings(), Items.NETHER_WART_BLOCK, Items.LEATHER_LEGGINGS, 3);
        addBlockArmorRecipeWithCore("Nether Wart Block Boots", BlockArmor.createNetherWartBlockBoots(), Items.NETHER_WART_BLOCK, Items.LEATHER_BOOTS, 3);

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
        
        // Glass Blade - T1 (basic material)
        addBlockWeaponRecipeWithCore("Glass Blade", BlockWeapons.createGlassSword(), Items.GLASS, Items.DIAMOND_SWORD, 1);
        
        // Obsidian Blade - T3 (very durable material)
        addBlockWeaponRecipeWithCore("Obsidian Blade", BlockWeapons.createObsidianSword(), Items.OBSIDIAN, Items.NETHERITE_SWORD, 3);
        
        // Quartz Blade - T1 (basic nether material)
        addBlockWeaponRecipeWithCore("Quartz Blade", BlockWeapons.createQuartzSword(), Items.QUARTZ_BLOCK, Items.DIAMOND_SWORD, 1);
        
        // Glowstone Blade - T1 (light-emitting material)
        addBlockWeaponRecipeWithCore("Glowstone Blade", BlockWeapons.createGlowstoneSword(), Items.GLOWSTONE, Items.GOLDEN_SWORD, 1);
        
        // Redstone Blade - T1 (common material)
        addBlockWeaponRecipeWithCore("Redstone Blade", BlockWeapons.createRedstoneSword(), Items.REDSTONE_BLOCK, Items.IRON_SWORD, 1);
        
        // Netherrack Blade - T1 (common nether material)
        addBlockWeaponRecipeWithCore("Netherrack Blade", BlockWeapons.createNetherrackSword(), Items.NETHERRACK, Items.STONE_SWORD, 1);
        
        // Endstone Blade - T2 (end material, decent durability)
        addBlockWeaponRecipeWithCore("Endstone Blade", BlockWeapons.createEndstoneSword(), Items.END_STONE, Items.DIAMOND_SWORD, 2);
        
        // Ice Blade - T1 (basic material)
        addBlockWeaponRecipeWithCore("Ice Blade", BlockWeapons.createIceSword(), Items.PACKED_ICE, Items.DIAMOND_SWORD, 1);
        
        // Prismarine Blade - T2 (ocean material, decent durability)
        addBlockWeaponRecipeWithCore("Prismarine Blade", BlockWeapons.createPrismarineSword(), Items.PRISMARINE, Items.DIAMOND_SWORD, 2);
        
        // Terracotta Blade - T1 (basic material)
        addBlockWeaponRecipeWithCore("Terracotta Blade", BlockWeapons.createTerracottaSword(), Items.TERRACOTTA, Items.IRON_SWORD, 1);
        
        // Mossy Blade - T1 (basic material)
        addBlockWeaponRecipeWithCore("Mossy Blade", BlockWeapons.createMossySword(), Items.MOSSY_COBBLESTONE, Items.STONE_SWORD, 1);
        
        // Soul Blade - T1 (soul material)
        addBlockWeaponRecipeWithCore("Soul Blade", BlockWeapons.createSoulSandSword(), Items.SOUL_SAND, Items.GOLDEN_SWORD, 1);
        
        // Magma Blade - T2 (nether material, fire resistant)
        addBlockWeaponRecipeWithCore("Magma Blade", BlockWeapons.createMagmaSword(), Items.MAGMA_BLOCK, Items.GOLDEN_SWORD, 2);
        
        // Sandstone Blade - T1 (basic material)
        addBlockWeaponRecipeWithCore("Sandstone Blade", BlockWeapons.createSandstoneSword(), Items.SANDSTONE, Items.STONE_SWORD, 1);
        
        // Amethyst Blade - T2 (rare crystal material)
        addBlockWeaponRecipeWithCore("Amethyst Blade", BlockWeapons.createAmethystSword(), Items.AMETHYST_BLOCK, Items.DIAMOND_SWORD, 2);
        
        // Coal Blade - T1 (common material)
        addBlockWeaponRecipeWithCore("Coal Blade", BlockWeapons.createCoalSword(), Items.COAL_BLOCK, Items.STONE_SWORD, 1);

        // ===== NEW WEAPON RECIPES WITH CRAFTING CORES =====
        // Stripped Log Swords
        addBlockWeaponRecipeWithCore("Stripped Spruce Log Blade", BlockWeapons.createStrippedSpruceLogSword(), Items.STRIPPED_SPRUCE_LOG, Items.IRON_SWORD, 1);
        addBlockWeaponRecipeWithCore("Stripped Birch Log Blade", BlockWeapons.createStrippedBirchLogSword(), Items.STRIPPED_BIRCH_LOG, Items.IRON_SWORD, 1);
        addBlockWeaponRecipeWithCore("Stripped Dark Oak Log Blade", BlockWeapons.createStrippedDarkOakLogSword(), Items.STRIPPED_DARK_OAK_LOG, Items.IRON_SWORD, 1);
        addBlockWeaponRecipeWithCore("Stripped Jungle Log Blade", BlockWeapons.createStrippedJungleLogSword(), Items.STRIPPED_JUNGLE_LOG, Items.IRON_SWORD, 1);
        addBlockWeaponRecipeWithCore("Stripped Acacia Log Blade", BlockWeapons.createStrippedAcaciaLogSword(), Items.STRIPPED_ACACIA_LOG, Items.IRON_SWORD, 1);
        addBlockWeaponRecipeWithCore("Stripped Mangrove Log Blade", BlockWeapons.createStrippedMangroveLogSword(), Items.STRIPPED_MANGROVE_LOG, Items.IRON_SWORD, 1);
        addBlockWeaponRecipeWithCore("Stripped Cherry Log Blade", BlockWeapons.createStrippedCherryLogSword(), Items.STRIPPED_CHERRY_LOG, Items.IRON_SWORD, 1);
        addBlockWeaponRecipeWithCore("Bamboo Block Blade", BlockWeapons.createStrippedBambooBlockSword(), Items.BAMBOO_BLOCK, Items.IRON_SWORD, 1);
        addBlockWeaponRecipeWithCore("Stripped Crimson Stem Blade", BlockWeapons.createStrippedCrimsonStemSword(), Items.STRIPPED_CRIMSON_STEM, Items.IRON_SWORD, 2);
        addBlockWeaponRecipeWithCore("Stripped Warped Stem Blade", BlockWeapons.createStrippedWarpedStemSword(), Items.STRIPPED_WARPED_STEM, Items.IRON_SWORD, 2);
        // Plank Swords
        addBlockWeaponRecipeWithCore("Spruce Planks Blade", BlockWeapons.createSprucePlanksSword(), Items.SPRUCE_PLANKS, Items.IRON_SWORD, 1);
        addBlockWeaponRecipeWithCore("Birch Planks Blade", BlockWeapons.createBirchPlanksSword(), Items.BIRCH_PLANKS, Items.IRON_SWORD, 1);
        addBlockWeaponRecipeWithCore("Jungle Planks Blade", BlockWeapons.createJunglePlanksSword(), Items.JUNGLE_PLANKS, Items.IRON_SWORD, 1);
        addBlockWeaponRecipeWithCore("Acacia Planks Blade", BlockWeapons.createAcaciaPlanksSword(), Items.ACACIA_PLANKS, Items.IRON_SWORD, 1);
        addBlockWeaponRecipeWithCore("Dark Oak Planks Blade", BlockWeapons.createDarkOakPlanksSword(), Items.DARK_OAK_PLANKS, Items.IRON_SWORD, 1);
        addBlockWeaponRecipeWithCore("Mangrove Planks Blade", BlockWeapons.createMangrovePlanksSword(), Items.MANGROVE_PLANKS, Items.IRON_SWORD, 1);
        addBlockWeaponRecipeWithCore("Cherry Planks Blade", BlockWeapons.createCherryPlanksSword(), Items.CHERRY_PLANKS, Items.IRON_SWORD, 1);
        addBlockWeaponRecipeWithCore("Bamboo Planks Blade", BlockWeapons.createBambooPlanksSword(), Items.BAMBOO_PLANKS, Items.IRON_SWORD, 1);
        addBlockWeaponRecipeWithCore("Crimson Planks Blade", BlockWeapons.createCrimsonPlanksSword(), Items.CRIMSON_PLANKS, Items.IRON_SWORD, 1);
        addBlockWeaponRecipeWithCore("Warped Planks Blade", BlockWeapons.createWarpedPlanksSword(), Items.WARPED_PLANKS, Items.IRON_SWORD, 1);
        addBlockWeaponRecipeWithCore("Oak Planks Blade", BlockWeapons.createOakPlanksSword(), Items.OAK_PLANKS, Items.IRON_SWORD, 1);
        // Stone/Mud/Log Swords
        addBlockWeaponRecipeWithCore("Oak Log Blade", BlockWeapons.createOakLogSword(), Items.OAK_LOG, Items.IRON_SWORD, 1);
        addBlockWeaponRecipeWithCore("Spruce Log Blade", BlockWeapons.createSpruceLogSword(), Items.SPRUCE_LOG, Items.IRON_SWORD, 1);
        addBlockWeaponRecipeWithCore("Birch Log Blade", BlockWeapons.createBirchLogSword(), Items.BIRCH_LOG, Items.IRON_SWORD, 1);
        addBlockWeaponRecipeWithCore("Stone Bricks Blade", BlockWeapons.createStoneBricksSword(), Items.STONE_BRICKS, Items.DIAMOND_SWORD, 2);
        addBlockWeaponRecipeWithCore("Cobblestone Blade", BlockWeapons.createCobblestoneSword(), Items.COBBLESTONE, Items.STONE_SWORD, 1);
        addBlockWeaponRecipeWithCore("Mossy Cobblestone Blade", BlockWeapons.createMossyCobblestoneSword(), Items.MOSSY_COBBLESTONE, Items.STONE_SWORD, 1);
        addBlockWeaponRecipeWithCore("Cobbled Deepslate Blade", BlockWeapons.createCobbledDeepslateSword(), Items.COBBLED_DEEPSLATE, Items.DIAMOND_SWORD, 2);
        addBlockWeaponRecipeWithCore("Mud Bricks Blade", BlockWeapons.createMudBricksSword(), Items.MUD_BRICKS, Items.IRON_SWORD, 1);
        addBlockWeaponRecipeWithCore("Mangrove Roots Blade", BlockWeapons.createMangroveRootsSword(), Items.MANGROVE_ROOTS, Items.IRON_SWORD, 1);
        addBlockWeaponRecipeWithCore("Muddy Mangrove Roots Blade", BlockWeapons.createMuddyMangroveRootsSword(), Items.MUDDY_MANGROVE_ROOTS, Items.IRON_SWORD, 1);
        // Copper/Netherite/Froglight Swords
        addBlockWeaponRecipeWithCore("Netherite Block Blade", BlockWeapons.createNetheriteBlockSword(), Items.NETHERITE_BLOCK, Items.NETHERITE_SWORD, 3);
        addBlockWeaponRecipeWithCore("Chiseled Copper Blade", BlockWeapons.createChiseledCopperSword(), Items.CHISELED_COPPER, Items.IRON_SWORD, 1);
        addBlockWeaponRecipeWithCore("Cut Copper Blade", BlockWeapons.createCutCopperSword(), Items.CUT_COPPER, Items.IRON_SWORD, 1);
        addBlockWeaponRecipeWithCore("Exposed Copper Blade", BlockWeapons.createExposedCopperSword(), Items.EXPOSED_COPPER, Items.IRON_SWORD, 1);
        addBlockWeaponRecipeWithCore("Weathered Copper Blade", BlockWeapons.createWeatheredCopperSword(), Items.WEATHERED_COPPER, Items.IRON_SWORD, 1);
        addBlockWeaponRecipeWithCore("Oxidised Copper Blade", BlockWeapons.createOxidisedCopperSword(), Items.OXIDIZED_COPPER, Items.IRON_SWORD, 1);
        addBlockWeaponRecipeWithCore("Waxed Cut Copper Blade", BlockWeapons.createWaxedCutCopperSword(), Items.WAXED_CUT_COPPER, Items.IRON_SWORD, 1);
        addBlockWeaponRecipeWithCore("Polished Basalt Blade", BlockWeapons.createPolishedBasaltSword(), Items.POLISHED_BASALT, Items.DIAMOND_SWORD, 2);
        addBlockWeaponRecipeWithCore("Diamond Block Blade", BlockWeapons.createDiamondBlockSword(), Items.DIAMOND_BLOCK, Items.DIAMOND_SWORD, 3);
        addBlockWeaponRecipeWithCore("Emerald Block Blade", BlockWeapons.createEmeraldBlockSword(), Items.EMERALD_BLOCK, Items.DIAMOND_SWORD, 2);
        addBlockWeaponRecipeWithCore("Gold Block Blade", BlockWeapons.createGoldBlockSword(), Items.GOLD_BLOCK, Items.GOLDEN_SWORD, 1);
        addBlockWeaponRecipeWithCore("Iron Block Blade", BlockWeapons.createIronBlockSword(), Items.IRON_BLOCK, Items.IRON_SWORD, 2);
        addBlockWeaponRecipeWithCore("Lapis Block Blade", BlockWeapons.createLapisBlockSword(), Items.LAPIS_BLOCK, Items.DIAMOND_SWORD, 2);
        addBlockWeaponRecipeWithCore("Copper Block Blade", BlockWeapons.createCopperBlockSword(), Items.COPPER_BLOCK, Items.IRON_SWORD, 1);
        addBlockWeaponRecipeWithCore("Ancient Debris Blade", BlockWeapons.createAncientDebrisSword(), Items.ANCIENT_DEBRIS, Items.NETHERITE_SWORD, 3);
        addBlockWeaponRecipeWithCore("Basalt Blade", BlockWeapons.createBasaltSword(), Items.BASALT, Items.STONE_SWORD, 2);
        addBlockWeaponRecipeWithCore("Blackstone Blade", BlockWeapons.createBlackstoneSword(), Items.BLACKSTONE, Items.STONE_SWORD, 2);
        addBlockWeaponRecipeWithCore("Bone Block Blade", BlockWeapons.createBoneBlockSword(), Items.BONE_BLOCK, Items.IRON_SWORD, 1);
        addBlockWeaponRecipeWithCore("Brick Blade", BlockWeapons.createBrickSword(), Items.BRICKS, Items.IRON_SWORD, 1);
        addBlockWeaponRecipeWithCore("Cactus Blade", BlockWeapons.createCactusSword(), Items.CACTUS, Items.STONE_SWORD, 1);
        addBlockWeaponRecipeWithCore("Calcite Blade", BlockWeapons.createCalciteSword(), Items.CALCITE, Items.STONE_SWORD, 2);
        addBlockWeaponRecipeWithCore("Deepslate Blade", BlockWeapons.createDeepslateSword(), Items.DEEPSLATE, Items.STONE_SWORD, 2);
        addBlockWeaponRecipeWithCore("Dripstone Blade", BlockWeapons.createDripstoneSword(), Items.DRIPSTONE_BLOCK, Items.STONE_SWORD, 2);
        addBlockWeaponRecipeWithCore("Nether Brick Blade", BlockWeapons.createNetherBrickSword(), Items.NETHER_BRICKS, Items.STONE_SWORD, 2);
        addBlockWeaponRecipeWithCore("Purpur Blade", BlockWeapons.createPurpurSword(), Items.PURPUR_BLOCK, Items.DIAMOND_SWORD, 2);
        addBlockWeaponRecipeWithCore("Sculk Blade", BlockWeapons.createSculkSword(), Items.SCULK, Items.STONE_SWORD, 2);
        addBlockWeaponRecipeWithCore("Shroomlight Blade", BlockWeapons.createShroomlightSword(), Items.SHROOMLIGHT, Items.GOLDEN_SWORD, 1);
        addBlockWeaponRecipeWithCore("Smooth Stone Blade", BlockWeapons.createSmoothStoneSword(), Items.SMOOTH_STONE, Items.STONE_SWORD, 2);
        addBlockWeaponRecipeWithCore("Soul Soil Blade", BlockWeapons.createSoulSoilSword(), Items.SOUL_SOIL, Items.STONE_SWORD, 2);
        addBlockWeaponRecipeWithCore("Sponge Blade", BlockWeapons.createSpongeSword(), Items.SPONGE, Items.IRON_SWORD, 1);

        addBlockWeaponRecipeWithCore("Copper Ingot Blade", BlockWeapons.createCopperIngotSword(), Items.COPPER_INGOT, Items.IRON_SWORD, 1);
        addBlockWeaponRecipeWithCore("Emerald Blade", BlockWeapons.createEmeraldSword(), Items.EMERALD, Items.DIAMOND_SWORD, 2);
        addBlockWeaponRecipeWithCore("Lapis Lazuli Blade", BlockWeapons.createLapisLazuliSword(), Items.LAPIS_LAZULI, Items.DIAMOND_SWORD, 2);
        addBlockWeaponRecipeWithCore("Amethyst Shard Blade", BlockWeapons.createAmethystShardSword(), Items.AMETHYST_SHARD, Items.DIAMOND_SWORD, 2);
        addBlockWeaponRecipeWithCore("Flint Blade", BlockWeapons.createFlintSword(), Items.FLINT, Items.STONE_SWORD, 1);
        addBlockWeaponRecipeWithCore("Bone Meal Blade", BlockWeapons.createBoneMealSword(), Items.BONE_MEAL, Items.STONE_SWORD, 1);
        addBlockWeaponRecipeWithCore("Charcoal Blade", BlockWeapons.createCharcoalSword(), Items.CHARCOAL, Items.STONE_SWORD, 1);
        addBlockWeaponRecipeWithCore("End Stone Blade", BlockWeapons.createEndStoneSword(), Items.END_STONE, Items.DIAMOND_SWORD, 2);
        addBlockWeaponRecipeWithCore("Snow Block Blade", BlockWeapons.createSnowBlockSword(), Items.SNOW_BLOCK, Items.IRON_SWORD, 1);
        
        // NEW BLOCK WEAPON RECIPES
        addBlockWeaponRecipeWithCore("Crimson Stem Blade", BlockWeapons.createCrimsonStemSword(), Items.CRIMSON_STEM, Items.WOODEN_SWORD, 3);
        addBlockWeaponRecipeWithCore("Crying Obsidian Blade", BlockWeapons.createCryingObsidianSword(), Items.CRYING_OBSIDIAN, Items.NETHERITE_SWORD, 3);
        addBlockWeaponRecipeWithCore("Gilded Blackstone Blade", BlockWeapons.createGildedBlackstoneSword(), Items.GILDED_BLACKSTONE, Items.STONE_SWORD, 3);
        addBlockWeaponRecipeWithCore("Granite Blade", BlockWeapons.createGraniteSword(), Items.GRANITE, Items.STONE_SWORD, 1);
        addBlockWeaponRecipeWithCore("Diorite Blade", BlockWeapons.createDioriteSword(), Items.DIORITE, Items.STONE_SWORD, 1);
        addBlockWeaponRecipeWithCore("Andesite Blade", BlockWeapons.createAndesiteSword(), Items.ANDESITE, Items.STONE_SWORD, 1);
        addBlockWeaponRecipeWithCore("Polished Granite Blade", BlockWeapons.createPolishedGraniteSword(), Items.POLISHED_GRANITE, Items.IRON_SWORD, 2);
        addBlockWeaponRecipeWithCore("Polished Diorite Blade", BlockWeapons.createPolishedDioriteSword(), Items.POLISHED_DIORITE, Items.IRON_SWORD, 2);
        addBlockWeaponRecipeWithCore("Polished Andesite Blade", BlockWeapons.createPolishedAndesiteSword(), Items.POLISHED_ANDESITE, Items.IRON_SWORD, 2);
        addBlockWeaponRecipeWithCore("Packed Ice Blade", BlockWeapons.createPackedIceSword(), Items.PACKED_ICE, Items.STONE_SWORD, 1);
        addBlockWeaponRecipeWithCore("Blue Ice Blade", BlockWeapons.createBlueIceSword(), Items.BLUE_ICE, Items.IRON_SWORD, 3);
        addBlockWeaponRecipeWithCore("Nether Gold Ore Blade", BlockWeapons.createNetherGoldOreSword(), Items.NETHER_GOLD_ORE, Items.STONE_SWORD, 3);
        addBlockWeaponRecipeWithCore("Dark Oak Log Blade", BlockWeapons.createDarkOakLogSword(), Items.DARK_OAK_LOG, Items.STONE_SWORD, 1);
        addBlockWeaponRecipeWithCore("Jungle Log Blade", BlockWeapons.createJungleLogSword(), Items.JUNGLE_LOG, Items.STONE_SWORD, 1);
        addBlockWeaponRecipeWithCore("Acacia Log Blade", BlockWeapons.createAcaciaLogSword(), Items.ACACIA_LOG, Items.STONE_SWORD, 1);
        addBlockWeaponRecipeWithCore("Mangrove Log Blade", BlockWeapons.createMangroveLogSword(), Items.MANGROVE_LOG, Items.STONE_SWORD, 1);
        addBlockWeaponRecipeWithCore("Cherry Log Blade", BlockWeapons.createCherryLogSword(), Items.CHERRY_LOG, Items.STONE_SWORD, 1);
        addBlockWeaponRecipeWithCore("Bamboo Block Blade", BlockWeapons.createBambooBlockSword(), Items.BAMBOO_BLOCK, Items.WOODEN_SWORD, 1);
        addBlockWeaponRecipeWithCore("Tuff Blade", BlockWeapons.createTuffSword(), Items.TUFF, Items.STONE_SWORD, 1);
        addBlockWeaponRecipeWithCore("Polished Tuff Blade", BlockWeapons.createPolishedTuffSword(), Items.POLISHED_TUFF, Items.IRON_SWORD, 2);
        addBlockWeaponRecipeWithCore("Nether Wart Block Blade", BlockWeapons.createNetherWartBlockSword(), Items.NETHER_WART_BLOCK, Items.WOODEN_SWORD, 3);
        
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
        
        // Glass Tools - T1 (basic material)
        addBlockToolRecipeWithCore("Glass Pickaxe", BlockTools.createGlassPickaxe(), Items.GLASS, Items.DIAMOND_PICKAXE, 1);
        addBlockToolRecipeWithCore("Glass Axe", BlockTools.createGlassAxe(), Items.GLASS, Items.DIAMOND_AXE, 1);
        addBlockToolRecipeWithCore("Glass Shovel", BlockTools.createGlassShovel(), Items.GLASS, Items.DIAMOND_SHOVEL, 1);
        addBlockToolRecipeWithCore("Glass Hoe", BlockTools.createGlassHoe(), Items.GLASS, Items.DIAMOND_HOE, 1);
        
        // Netherrack Tools - T1 (common nether material)
        addBlockToolRecipeWithCore("Netherrack Pickaxe", BlockTools.createNetherrackPickaxe(), Items.NETHERRACK, Items.STONE_PICKAXE, 1);
        addBlockToolRecipeWithCore("Netherrack Axe", BlockTools.createNetherrackAxe(), Items.NETHERRACK, Items.STONE_AXE, 1);
        addBlockToolRecipeWithCore("Netherrack Shovel", BlockTools.createNetherrackShovel(), Items.NETHERRACK, Items.STONE_SHOVEL, 1);
        addBlockToolRecipeWithCore("Netherrack Hoe", BlockTools.createNetherrackHoe(), Items.NETHERRACK, Items.STONE_HOE, 1);
        
        // End Stone Tools - T2 (end material, decent durability)
        addBlockToolRecipeWithCore("End Stone Pickaxe", BlockTools.createEndstonePickaxe(), Items.END_STONE, Items.DIAMOND_PICKAXE, 2);
        addBlockToolRecipeWithCore("End Stone Axe", BlockTools.createEndstoneAxe(), Items.END_STONE, Items.DIAMOND_AXE, 2);
        addBlockToolRecipeWithCore("End Stone Shovel", BlockTools.createEndstoneShovel(), Items.END_STONE, Items.DIAMOND_SHOVEL, 2);
        addBlockToolRecipeWithCore("End Stone Hoe", BlockTools.createEndstoneHoe(), Items.END_STONE, Items.DIAMOND_HOE, 2);
        
        // Ice Tools - T1 (basic material)
        addBlockToolRecipeWithCore("Ice Pickaxe", BlockTools.createIcePickaxe(), Items.PACKED_ICE, Items.DIAMOND_PICKAXE, 1);
        addBlockToolRecipeWithCore("Ice Axe", BlockTools.createIceAxe(), Items.PACKED_ICE, Items.DIAMOND_AXE, 1);
        addBlockToolRecipeWithCore("Ice Shovel", BlockTools.createIceShovel(), Items.PACKED_ICE, Items.DIAMOND_SHOVEL, 1);
        addBlockToolRecipeWithCore("Ice Hoe", BlockTools.createIceHoe(), Items.PACKED_ICE, Items.DIAMOND_HOE, 1);
        
        // Terracotta Tools - T1 (basic material)
        addBlockToolRecipeWithCore("Terracotta Pickaxe", BlockTools.createTerracottaPickaxe(), Items.TERRACOTTA, Items.IRON_PICKAXE, 1);
        addBlockToolRecipeWithCore("Terracotta Axe", BlockTools.createTerracottaAxe(), Items.TERRACOTTA, Items.IRON_AXE, 1);
        addBlockToolRecipeWithCore("Terracotta Shovel", BlockTools.createTerracottaShovel(), Items.TERRACOTTA, Items.IRON_SHOVEL, 1);
        addBlockToolRecipeWithCore("Terracotta Hoe", BlockTools.createTerracottaHoe(), Items.TERRACOTTA, Items.IRON_HOE, 1);
        
        // Obsidian Tools - T3 (very durable material)
        addBlockToolRecipeWithCore("Obsidian Pickaxe", BlockTools.createObsidianPickaxe(), Items.OBSIDIAN, Items.NETHERITE_PICKAXE, 3);
        addBlockToolRecipeWithCore("Obsidian Axe", BlockTools.createObsidianAxe(), Items.OBSIDIAN, Items.NETHERITE_AXE, 3);
        addBlockToolRecipeWithCore("Obsidian Shovel", BlockTools.createObsidianShovel(), Items.OBSIDIAN, Items.NETHERITE_SHOVEL, 3);
        addBlockToolRecipeWithCore("Obsidian Hoe", BlockTools.createObsidianHoe(), Items.OBSIDIAN, Items.NETHERITE_HOE, 3);
        
        // Quartz Tools - T1 (basic nether material)
        addBlockToolRecipeWithCore("Quartz Pickaxe", BlockTools.createQuartzPickaxe(), Items.QUARTZ_BLOCK, Items.DIAMOND_PICKAXE, 1);
        addBlockToolRecipeWithCore("Quartz Axe", BlockTools.createQuartzAxe(), Items.QUARTZ_BLOCK, Items.DIAMOND_AXE, 1);
        addBlockToolRecipeWithCore("Quartz Shovel", BlockTools.createQuartzShovel(), Items.QUARTZ_BLOCK, Items.DIAMOND_SHOVEL, 1);
        addBlockToolRecipeWithCore("Quartz Hoe", BlockTools.createQuartzHoe(), Items.QUARTZ_BLOCK, Items.DIAMOND_HOE, 1);
        
        // Glowstone Tools - T1 (light-emitting material)
        addBlockToolRecipeWithCore("Glowstone Pickaxe", BlockTools.createGlowstonePickaxe(), Items.GLOWSTONE, Items.GOLDEN_PICKAXE, 1);
        addBlockToolRecipeWithCore("Glowstone Axe", BlockTools.createGlowstoneAxe(), Items.GLOWSTONE, Items.GOLDEN_AXE, 1);
        addBlockToolRecipeWithCore("Glowstone Shovel", BlockTools.createGlowstoneShovel(), Items.GLOWSTONE, Items.GOLDEN_SHOVEL, 1);
        addBlockToolRecipeWithCore("Glowstone Hoe", BlockTools.createGlowstoneHoe(), Items.GLOWSTONE, Items.GOLDEN_HOE, 1);
        
        // Redstone Tools - T1 (common material)
        addBlockToolRecipeWithCore("Redstone Pickaxe", BlockTools.createRedstonePickaxe(), Items.REDSTONE_BLOCK, Items.IRON_PICKAXE, 1);
        addBlockToolRecipeWithCore("Redstone Axe", BlockTools.createRedstoneAxe(), Items.REDSTONE_BLOCK, Items.IRON_AXE, 1);
        addBlockToolRecipeWithCore("Redstone Shovel", BlockTools.createRedstoneShovel(), Items.REDSTONE_BLOCK, Items.IRON_SHOVEL, 1);
        addBlockToolRecipeWithCore("Redstone Hoe", BlockTools.createRedstoneHoe(), Items.REDSTONE_BLOCK, Items.IRON_HOE, 1);
        
        // Netherrack Tools - T1 (common nether material)
        addBlockToolRecipeWithCore("Netherrack Pickaxe", BlockTools.createNetherrackPickaxe(), Items.NETHERRACK, Items.STONE_PICKAXE, 1);
        addBlockToolRecipeWithCore("Netherrack Axe", BlockTools.createNetherrackAxe(), Items.NETHERRACK, Items.STONE_AXE, 1);
        addBlockToolRecipeWithCore("Netherrack Shovel", BlockTools.createNetherrackShovel(), Items.NETHERRACK, Items.STONE_SHOVEL, 1);
        addBlockToolRecipeWithCore("Netherrack Hoe", BlockTools.createNetherrackHoe(), Items.NETHERRACK, Items.STONE_HOE, 1);
        
        // End Stone Tools - T2 (end material, decent durability)
        addBlockToolRecipeWithCore("End Stone Pickaxe", BlockTools.createEndstonePickaxe(), Items.END_STONE, Items.DIAMOND_PICKAXE, 2);
        addBlockToolRecipeWithCore("End Stone Axe", BlockTools.createEndstoneAxe(), Items.END_STONE, Items.DIAMOND_AXE, 2);
        addBlockToolRecipeWithCore("End Stone Shovel", BlockTools.createEndstoneShovel(), Items.END_STONE, Items.DIAMOND_SHOVEL, 2);
        addBlockToolRecipeWithCore("End Stone Hoe", BlockTools.createEndstoneHoe(), Items.END_STONE, Items.DIAMOND_HOE, 2);
        
        // Ice Tools - T1 (basic material)
        addBlockToolRecipeWithCore("Ice Pickaxe", BlockTools.createIcePickaxe(), Items.PACKED_ICE, Items.DIAMOND_PICKAXE, 1);
        addBlockToolRecipeWithCore("Ice Axe", BlockTools.createIceAxe(), Items.PACKED_ICE, Items.DIAMOND_AXE, 1);
        addBlockToolRecipeWithCore("Ice Shovel", BlockTools.createIceShovel(), Items.PACKED_ICE, Items.DIAMOND_SHOVEL, 1);
        addBlockToolRecipeWithCore("Ice Hoe", BlockTools.createIceHoe(), Items.PACKED_ICE, Items.DIAMOND_HOE, 1);
        
        // Prismarine Tools - T2 (ocean material, decent durability)
        addBlockToolRecipeWithCore("Prismarine Pickaxe", BlockTools.createPrismarinePickaxe(), Items.PRISMARINE, Items.DIAMOND_PICKAXE, 2);
        addBlockToolRecipeWithCore("Prismarine Axe", BlockTools.createPrismarineAxe(), Items.PRISMARINE, Items.DIAMOND_AXE, 2);
        addBlockToolRecipeWithCore("Prismarine Shovel", BlockTools.createPrismarineShovel(), Items.PRISMARINE, Items.DIAMOND_SHOVEL, 2);
        addBlockToolRecipeWithCore("Prismarine Hoe", BlockTools.createPrismarineHoe(), Items.PRISMARINE, Items.DIAMOND_HOE, 2);
        
        // Mossy Tools - T1 (basic material)
        addBlockToolRecipeWithCore("Mossy Pickaxe", BlockTools.createMossyPickaxe(), Items.MOSSY_COBBLESTONE, Items.IRON_PICKAXE, 1);
        addBlockToolRecipeWithCore("Mossy Axe", BlockTools.createMossyAxe(), Items.MOSSY_COBBLESTONE, Items.IRON_AXE, 1);
        addBlockToolRecipeWithCore("Mossy Shovel", BlockTools.createMossyShovel(), Items.MOSSY_COBBLESTONE, Items.IRON_SHOVEL, 1);
        addBlockToolRecipeWithCore("Mossy Hoe", BlockTools.createMossyHoe(), Items.MOSSY_COBBLESTONE, Items.IRON_HOE, 1);
        
        // Soul Sand Tools - T1 (soul material)
        addBlockToolRecipeWithCore("Soul Sand Pickaxe", BlockTools.createSoulSandPickaxe(), Items.SOUL_SAND, Items.STONE_PICKAXE, 1);
        addBlockToolRecipeWithCore("Soul Sand Axe", BlockTools.createSoulSandAxe(), Items.SOUL_SAND, Items.STONE_AXE, 1);
        addBlockToolRecipeWithCore("Soul Sand Shovel", BlockTools.createSoulSandShovel(), Items.SOUL_SAND, Items.STONE_SHOVEL, 1);
        addBlockToolRecipeWithCore("Soul Sand Hoe", BlockTools.createSoulSandHoe(), Items.SOUL_SAND, Items.STONE_HOE, 1);
        
        // Magma Tools - T2 (nether material, fire resistant)
        addBlockToolRecipeWithCore("Magma Pickaxe", BlockTools.createMagmaPickaxe(), Items.MAGMA_BLOCK, Items.IRON_PICKAXE, 2);
        addBlockToolRecipeWithCore("Magma Axe", BlockTools.createMagmaAxe(), Items.MAGMA_BLOCK, Items.IRON_AXE, 2);
        addBlockToolRecipeWithCore("Magma Shovel", BlockTools.createMagmaShovel(), Items.MAGMA_BLOCK, Items.IRON_SHOVEL, 2);
        addBlockToolRecipeWithCore("Magma Hoe", BlockTools.createMagmaHoe(), Items.MAGMA_BLOCK, Items.IRON_HOE, 2);
        
        // Sandstone Tools - T1 (basic material)
        addBlockToolRecipeWithCore("Sandstone Pickaxe", BlockTools.createSandstonePickaxe(), Items.SANDSTONE, Items.IRON_PICKAXE, 1);
        addBlockToolRecipeWithCore("Sandstone Axe", BlockTools.createSandstoneAxe(), Items.SANDSTONE, Items.IRON_AXE, 1);
        addBlockToolRecipeWithCore("Sandstone Shovel", BlockTools.createSandstoneShovel(), Items.SANDSTONE, Items.IRON_SHOVEL, 1);
        addBlockToolRecipeWithCore("Sandstone Hoe", BlockTools.createSandstoneHoe(), Items.SANDSTONE, Items.IRON_HOE, 1);
        
        // Amethyst Tools - T2 (rare crystal material)
        addBlockToolRecipeWithCore("Amethyst Pickaxe", BlockTools.createAmethystPickaxe(), Items.AMETHYST_BLOCK, Items.DIAMOND_PICKAXE, 2);
        addBlockToolRecipeWithCore("Amethyst Axe", BlockTools.createAmethystAxe(), Items.AMETHYST_BLOCK, Items.DIAMOND_AXE, 2);
        addBlockToolRecipeWithCore("Amethyst Shovel", BlockTools.createAmethystShovel(), Items.AMETHYST_BLOCK, Items.DIAMOND_SHOVEL, 2);
        addBlockToolRecipeWithCore("Amethyst Hoe", BlockTools.createAmethystHoe(), Items.AMETHYST_BLOCK, Items.DIAMOND_HOE, 2);
        
        // Coal Tools - T1 (common material)
        addBlockToolRecipeWithCore("Coal Pickaxe", BlockTools.createCoalPickaxe(), Items.COAL_BLOCK, Items.STONE_PICKAXE, 1);
        addBlockToolRecipeWithCore("Coal Axe", BlockTools.createCoalAxe(), Items.COAL_BLOCK, Items.STONE_AXE, 1);
        addBlockToolRecipeWithCore("Coal Shovel", BlockTools.createCoalShovel(), Items.COAL_BLOCK, Items.STONE_SHOVEL, 1);
        addBlockToolRecipeWithCore("Coal Hoe", BlockTools.createCoalHoe(), Items.COAL_BLOCK, Items.STONE_HOE, 1);
        // ===== NEW TOOL RECIPES WITH CRAFTING CORES =====
        // Stripped Log Tools
        addBlockToolRecipeWithCore("Stripped Spruce Log Pickaxe", BlockTools.createStrippedSpruceLogPickaxe(), Items.STRIPPED_SPRUCE_LOG, Items.IRON_PICKAXE, 1);
        addBlockToolRecipeWithCore("Stripped Spruce Log Axe", BlockTools.createStrippedSpruceLogAxe(), Items.STRIPPED_SPRUCE_LOG, Items.IRON_AXE, 1);
        addBlockToolRecipeWithCore("Stripped Spruce Log Shovel", BlockTools.createStrippedSpruceLogShovel(), Items.STRIPPED_SPRUCE_LOG, Items.IRON_SHOVEL, 1);
        addBlockToolRecipeWithCore("Stripped Spruce Log Hoe", BlockTools.createStrippedSpruceLogHoe(), Items.STRIPPED_SPRUCE_LOG, Items.IRON_HOE, 1);
        addBlockToolRecipeWithCore("Stripped Birch Log Pickaxe", BlockTools.createStrippedBirchLogPickaxe(), Items.STRIPPED_BIRCH_LOG, Items.IRON_PICKAXE, 1);
        addBlockToolRecipeWithCore("Stripped Birch Log Axe", BlockTools.createStrippedBirchLogAxe(), Items.STRIPPED_BIRCH_LOG, Items.IRON_AXE, 1);
        addBlockToolRecipeWithCore("Stripped Birch Log Shovel", BlockTools.createStrippedBirchLogShovel(), Items.STRIPPED_BIRCH_LOG, Items.IRON_SHOVEL, 1);
        addBlockToolRecipeWithCore("Stripped Birch Log Hoe", BlockTools.createStrippedBirchLogHoe(), Items.STRIPPED_BIRCH_LOG, Items.IRON_HOE, 1);
        addBlockToolRecipeWithCore("Stripped Dark Oak Log Pickaxe", BlockTools.createStrippedDarkOakLogPickaxe(), Items.STRIPPED_DARK_OAK_LOG, Items.IRON_PICKAXE, 1);
        addBlockToolRecipeWithCore("Stripped Dark Oak Log Axe", BlockTools.createStrippedDarkOakLogAxe(), Items.STRIPPED_DARK_OAK_LOG, Items.IRON_AXE, 1);
        addBlockToolRecipeWithCore("Stripped Dark Oak Log Shovel", BlockTools.createStrippedDarkOakLogShovel(), Items.STRIPPED_DARK_OAK_LOG, Items.IRON_SHOVEL, 1);
        addBlockToolRecipeWithCore("Stripped Dark Oak Log Hoe", BlockTools.createStrippedDarkOakLogHoe(), Items.STRIPPED_DARK_OAK_LOG, Items.IRON_HOE, 1);
        addBlockToolRecipeWithCore("Stripped Jungle Log Pickaxe", BlockTools.createStrippedJungleLogPickaxe(), Items.STRIPPED_JUNGLE_LOG, Items.IRON_PICKAXE, 1);
        addBlockToolRecipeWithCore("Stripped Jungle Log Axe", BlockTools.createStrippedJungleLogAxe(), Items.STRIPPED_JUNGLE_LOG, Items.IRON_AXE, 1);
        addBlockToolRecipeWithCore("Stripped Jungle Log Shovel", BlockTools.createStrippedJungleLogShovel(), Items.STRIPPED_JUNGLE_LOG, Items.IRON_SHOVEL, 1);
        addBlockToolRecipeWithCore("Stripped Jungle Log Hoe", BlockTools.createStrippedJungleLogHoe(), Items.STRIPPED_JUNGLE_LOG, Items.IRON_HOE, 1);
        addBlockToolRecipeWithCore("Stripped Acacia Log Pickaxe", BlockTools.createStrippedAcaciaLogPickaxe(), Items.STRIPPED_ACACIA_LOG, Items.IRON_PICKAXE, 1);
        addBlockToolRecipeWithCore("Stripped Acacia Log Axe", BlockTools.createStrippedAcaciaLogAxe(), Items.STRIPPED_ACACIA_LOG, Items.IRON_AXE, 1);
        addBlockToolRecipeWithCore("Stripped Acacia Log Shovel", BlockTools.createStrippedAcaciaLogShovel(), Items.STRIPPED_ACACIA_LOG, Items.IRON_SHOVEL, 1);
        addBlockToolRecipeWithCore("Stripped Acacia Log Hoe", BlockTools.createStrippedAcaciaLogHoe(), Items.STRIPPED_ACACIA_LOG, Items.IRON_HOE, 1);
        addBlockToolRecipeWithCore("Stripped Mangrove Log Pickaxe", BlockTools.createStrippedMangroveLogPickaxe(), Items.STRIPPED_MANGROVE_LOG, Items.IRON_PICKAXE, 1);
        addBlockToolRecipeWithCore("Stripped Mangrove Log Axe", BlockTools.createStrippedMangroveLogAxe(), Items.STRIPPED_MANGROVE_LOG, Items.IRON_AXE, 1);
        addBlockToolRecipeWithCore("Stripped Mangrove Log Shovel", BlockTools.createStrippedMangroveLogShovel(), Items.STRIPPED_MANGROVE_LOG, Items.IRON_SHOVEL, 1);
        addBlockToolRecipeWithCore("Stripped Mangrove Log Hoe", BlockTools.createStrippedMangroveLogHoe(), Items.STRIPPED_MANGROVE_LOG, Items.IRON_HOE, 1);
        addBlockToolRecipeWithCore("Stripped Cherry Log Pickaxe", BlockTools.createStrippedCherryLogPickaxe(), Items.STRIPPED_CHERRY_LOG, Items.IRON_PICKAXE, 1);
        addBlockToolRecipeWithCore("Stripped Cherry Log Axe", BlockTools.createStrippedCherryLogAxe(), Items.STRIPPED_CHERRY_LOG, Items.IRON_AXE, 1);
        addBlockToolRecipeWithCore("Stripped Cherry Log Shovel", BlockTools.createStrippedCherryLogShovel(), Items.STRIPPED_CHERRY_LOG, Items.IRON_SHOVEL, 1);
        addBlockToolRecipeWithCore("Stripped Cherry Log Hoe", BlockTools.createStrippedCherryLogHoe(), Items.STRIPPED_CHERRY_LOG, Items.IRON_HOE, 1);
        addBlockToolRecipeWithCore("Bamboo Block Pickaxe", BlockTools.createStrippedBambooBlockPickaxe(), Items.BAMBOO_BLOCK, Items.IRON_PICKAXE, 1);
        addBlockToolRecipeWithCore("Bamboo Block Axe", BlockTools.createStrippedBambooBlockAxe(), Items.BAMBOO_BLOCK, Items.IRON_AXE, 1);
        addBlockToolRecipeWithCore("Bamboo Block Shovel", BlockTools.createStrippedBambooBlockShovel(), Items.BAMBOO_BLOCK, Items.IRON_SHOVEL, 1);
        addBlockToolRecipeWithCore("Bamboo Block Hoe", BlockTools.createStrippedBambooBlockHoe(), Items.BAMBOO_BLOCK, Items.IRON_HOE, 1);
        addBlockToolRecipeWithCore("Stripped Crimson Stem Pickaxe", BlockTools.createStrippedCrimsonStemPickaxe(), Items.STRIPPED_CRIMSON_STEM, Items.IRON_PICKAXE, 2);
        addBlockToolRecipeWithCore("Stripped Crimson Stem Axe", BlockTools.createStrippedCrimsonStemAxe(), Items.STRIPPED_CRIMSON_STEM, Items.IRON_AXE, 2);
        addBlockToolRecipeWithCore("Stripped Crimson Stem Shovel", BlockTools.createStrippedCrimsonStemShovel(), Items.STRIPPED_CRIMSON_STEM, Items.IRON_SHOVEL, 2);
        addBlockToolRecipeWithCore("Stripped Crimson Stem Hoe", BlockTools.createStrippedCrimsonStemHoe(), Items.STRIPPED_CRIMSON_STEM, Items.IRON_HOE, 2);
        addBlockToolRecipeWithCore("Stripped Warped Stem Pickaxe", BlockTools.createStrippedWarpedStemPickaxe(), Items.STRIPPED_WARPED_STEM, Items.IRON_PICKAXE, 2);
        addBlockToolRecipeWithCore("Stripped Warped Stem Axe", BlockTools.createStrippedWarpedStemAxe(), Items.STRIPPED_WARPED_STEM, Items.IRON_AXE, 2);
        addBlockToolRecipeWithCore("Stripped Warped Stem Shovel", BlockTools.createStrippedWarpedStemShovel(), Items.STRIPPED_WARPED_STEM, Items.IRON_SHOVEL, 2);
        addBlockToolRecipeWithCore("Stripped Warped Stem Hoe", BlockTools.createStrippedWarpedStemHoe(), Items.STRIPPED_WARPED_STEM, Items.IRON_HOE, 2);
        // Plank Tools
        addBlockToolRecipeWithCore("Spruce Planks Pickaxe", BlockTools.createSprucePlanksPickaxe(), Items.SPRUCE_PLANKS, Items.IRON_PICKAXE, 1);
        addBlockToolRecipeWithCore("Birch Planks Pickaxe", BlockTools.createBirchPlanksPickaxe(), Items.BIRCH_PLANKS, Items.IRON_PICKAXE, 1);
        addBlockToolRecipeWithCore("Jungle Planks Pickaxe", BlockTools.createJunglePlanksPickaxe(), Items.JUNGLE_PLANKS, Items.IRON_PICKAXE, 1);
        addBlockToolRecipeWithCore("Acacia Planks Pickaxe", BlockTools.createAcaciaPlanksPickaxe(), Items.ACACIA_PLANKS, Items.IRON_PICKAXE, 1);
        addBlockToolRecipeWithCore("Dark Oak Planks Pickaxe", BlockTools.createDarkOakPlanksPickaxe(), Items.DARK_OAK_PLANKS, Items.IRON_PICKAXE, 1);
        addBlockToolRecipeWithCore("Mangrove Planks Pickaxe", BlockTools.createMangrovePlanksPickaxe(), Items.MANGROVE_PLANKS, Items.IRON_PICKAXE, 1);
        addBlockToolRecipeWithCore("Cherry Planks Pickaxe", BlockTools.createCherryPlanksPickaxe(), Items.CHERRY_PLANKS, Items.IRON_PICKAXE, 1);
        addBlockToolRecipeWithCore("Bamboo Planks Pickaxe", BlockTools.createBambooPlanksPickaxe(), Items.BAMBOO_PLANKS, Items.IRON_PICKAXE, 1);
        addBlockToolRecipeWithCore("Crimson Planks Pickaxe", BlockTools.createCrimsonPlanksPickaxe(), Items.CRIMSON_PLANKS, Items.IRON_PICKAXE, 1);
        addBlockToolRecipeWithCore("Warped Planks Pickaxe", BlockTools.createWarpedPlanksPickaxe(), Items.WARPED_PLANKS, Items.IRON_PICKAXE, 1);
        addBlockToolRecipeWithCore("Oak Planks Pickaxe", BlockTools.createOakPlanksPickaxe(), Items.OAK_PLANKS, Items.IRON_PICKAXE, 1);
        // Stone/Mud/Log Tools
        addBlockToolRecipeWithCore("Oak Log Pickaxe", BlockTools.createOakLogPickaxe(), Items.OAK_LOG, Items.IRON_PICKAXE, 1);
        addBlockToolRecipeWithCore("Spruce Log Pickaxe", BlockTools.createSpruceLogPickaxe(), Items.SPRUCE_LOG, Items.IRON_PICKAXE, 1);
        addBlockToolRecipeWithCore("Birch Log Pickaxe", BlockTools.createBirchLogPickaxe(), Items.BIRCH_LOG, Items.IRON_PICKAXE, 1);
        addBlockToolRecipeWithCore("Stone Bricks Pickaxe", BlockTools.createStoneBricksPickaxe(), Items.STONE_BRICKS, Items.DIAMOND_PICKAXE, 2);
        addBlockToolRecipeWithCore("Cobblestone Pickaxe", BlockTools.createCobblestonePickaxe(), Items.COBBLESTONE, Items.STONE_PICKAXE, 1);
        addBlockToolRecipeWithCore("Mossy Cobblestone Pickaxe", BlockTools.createMossyCobblestonePickaxe(), Items.MOSSY_COBBLESTONE, Items.STONE_PICKAXE, 1);
        addBlockToolRecipeWithCore("Cobbled Deepslate Pickaxe", BlockTools.createCobbledDeepslatePickaxe(), Items.COBBLED_DEEPSLATE, Items.DIAMOND_PICKAXE, 2);
        addBlockToolRecipeWithCore("Mud Bricks Pickaxe", BlockTools.createMudBricksPickaxe(), Items.MUD_BRICKS, Items.IRON_PICKAXE, 1);
        addBlockToolRecipeWithCore("Mangrove Roots Pickaxe", BlockTools.createMangroveRootsPickaxe(), Items.MANGROVE_ROOTS, Items.IRON_PICKAXE, 1);
        addBlockToolRecipeWithCore("Muddy Mangrove Roots Pickaxe", BlockTools.createMuddyMangroveRootsPickaxe(), Items.MUDDY_MANGROVE_ROOTS, Items.IRON_PICKAXE, 1);
        // Copper/Netherite/Froglight Tools
        addBlockToolRecipeWithCore("Netherite Block Pickaxe", BlockTools.createNetheriteBlockPickaxe(), Items.NETHERITE_BLOCK, Items.NETHERITE_PICKAXE, 3);
        addBlockToolRecipeWithCore("Chiseled Copper Pickaxe", BlockTools.createChiseledCopperPickaxe(), Items.CHISELED_COPPER, Items.IRON_PICKAXE, 1);
        addBlockToolRecipeWithCore("Cut Copper Pickaxe", BlockTools.createCutCopperPickaxe(), Items.CUT_COPPER, Items.IRON_PICKAXE, 1);
        addBlockToolRecipeWithCore("Exposed Copper Pickaxe", BlockTools.createExposedCopperPickaxe(), Items.EXPOSED_COPPER, Items.IRON_PICKAXE, 1);
        addBlockToolRecipeWithCore("Weathered Copper Pickaxe", BlockTools.createWeatheredCopperPickaxe(), Items.WEATHERED_COPPER, Items.IRON_PICKAXE, 1);
        addBlockToolRecipeWithCore("Oxidised Copper Pickaxe", BlockTools.createOxidisedCopperPickaxe(), Items.OXIDIZED_COPPER, Items.IRON_PICKAXE, 1);
        addBlockToolRecipeWithCore("Waxed Cut Copper Pickaxe", BlockTools.createWaxedCutCopperPickaxe(), Items.WAXED_CUT_COPPER, Items.IRON_PICKAXE, 1);
        addBlockToolRecipeWithCore("Polished Basalt Pickaxe", BlockTools.createPolishedBasaltPickaxe(), Items.POLISHED_BASALT, Items.DIAMOND_PICKAXE, 2);
        addBlockToolRecipeWithCore("Verdant Froglight Pickaxe", BlockTools.createVerdantFroglightPickaxe(), Items.VERDANT_FROGLIGHT, Items.DIAMOND_PICKAXE, 2);
        addBlockToolRecipeWithCore("Pearlescent Froglight Pickaxe", BlockTools.createPearlescentFroglightPickaxe(), Items.PEARLESCENT_FROGLIGHT, Items.DIAMOND_PICKAXE, 2);
        addBlockToolRecipeWithCore("Ochre Froglight Pickaxe", BlockTools.createOchreFroglightPickaxe(), Items.OCHRE_FROGLIGHT, Items.DIAMOND_PICKAXE, 2);
        // Material Item Tools
        addBlockToolRecipeWithCore("Iron Nugget Pickaxe", BlockTools.createIronNuggetPickaxe(), Items.IRON_NUGGET, Items.STONE_PICKAXE, 1);
        addBlockToolRecipeWithCore("Gold Nugget Pickaxe", BlockTools.createGoldNuggetPickaxe(), Items.GOLD_NUGGET, Items.GOLDEN_PICKAXE, 1);
        addBlockToolRecipeWithCore("Copper Ingot Pickaxe", BlockTools.createCopperIngotPickaxe(), Items.COPPER_INGOT, Items.IRON_PICKAXE, 1);
        addBlockToolRecipeWithCore("Emerald Pickaxe", BlockTools.createEmeraldPickaxe(), Items.EMERALD, Items.DIAMOND_PICKAXE, 2);
        addBlockToolRecipeWithCore("Lapis Lazuli Pickaxe", BlockTools.createLapisLazuliPickaxe(), Items.LAPIS_LAZULI, Items.DIAMOND_PICKAXE, 2);
        addBlockToolRecipeWithCore("Amethyst Shard Pickaxe", BlockTools.createAmethystShardPickaxe(), Items.AMETHYST_SHARD, Items.DIAMOND_PICKAXE, 2);
        addBlockToolRecipeWithCore("Flint Pickaxe", BlockTools.createFlintPickaxe(), Items.FLINT, Items.STONE_PICKAXE, 1);
        addBlockToolRecipeWithCore("Bone Meal Pickaxe", BlockTools.createBoneMealPickaxe(), Items.BONE_MEAL, Items.STONE_PICKAXE, 1);
        addBlockToolRecipeWithCore("Charcoal Pickaxe", BlockTools.createCharcoalPickaxe(), Items.CHARCOAL, Items.STONE_PICKAXE, 1);
        addBlockToolRecipeWithCore("End Stone Pickaxe", BlockTools.createEndStonePickaxe(), Items.END_STONE, Items.DIAMOND_PICKAXE, 2);
        addBlockToolRecipeWithCore("Snow Block Pickaxe", BlockTools.createSnowBlockPickaxe(), Items.SNOW_BLOCK, Items.IRON_PICKAXE, 1);
        
        // NEW BLOCK TOOL RECIPES
        addBlockToolRecipeWithCore("Crimson Stem Pickaxe", BlockTools.createCrimsonStemPickaxe(), Items.CRIMSON_STEM, Items.WOODEN_PICKAXE, 2);
        addBlockToolRecipeWithCore("Crimson Stem Axe", BlockTools.createCrimsonStemAxe(), Items.CRIMSON_STEM, Items.WOODEN_AXE, 2);
        addBlockToolRecipeWithCore("Crimson Stem Shovel", BlockTools.createCrimsonStemShovel(), Items.CRIMSON_STEM, Items.WOODEN_SHOVEL, 2);
        addBlockToolRecipeWithCore("Crimson Stem Hoe", BlockTools.createCrimsonStemHoe(), Items.CRIMSON_STEM, Items.WOODEN_HOE, 2);
        addBlockToolRecipeWithCore("Crying Obsidian Pickaxe", BlockTools.createCryingObsidianPickaxe(), Items.CRYING_OBSIDIAN, Items.NETHERITE_PICKAXE, 3);
        addBlockToolRecipeWithCore("Crying Obsidian Axe", BlockTools.createCryingObsidianAxe(), Items.CRYING_OBSIDIAN, Items.NETHERITE_AXE, 3);
        addBlockToolRecipeWithCore("Crying Obsidian Shovel", BlockTools.createCryingObsidianShovel(), Items.CRYING_OBSIDIAN, Items.NETHERITE_SHOVEL, 3);
        addBlockToolRecipeWithCore("Crying Obsidian Hoe", BlockTools.createCryingObsidianHoe(), Items.CRYING_OBSIDIAN, Items.NETHERITE_HOE, 3);
        addBlockToolRecipeWithCore("Gilded Blackstone Pickaxe", BlockTools.createGildedBlackstonePickaxe(), Items.GILDED_BLACKSTONE, Items.STONE_PICKAXE, 2);
        addBlockToolRecipeWithCore("Gilded Blackstone Axe", BlockTools.createGildedBlackstoneAxe(), Items.GILDED_BLACKSTONE, Items.STONE_AXE, 2);
        addBlockToolRecipeWithCore("Gilded Blackstone Shovel", BlockTools.createGildedBlackstoneShovel(), Items.GILDED_BLACKSTONE, Items.STONE_SHOVEL, 2);
        addBlockToolRecipeWithCore("Gilded Blackstone Hoe", BlockTools.createGildedBlackstoneHoe(), Items.GILDED_BLACKSTONE, Items.STONE_HOE, 2);
        addBlockToolRecipeWithCore("Granite Pickaxe", BlockTools.createGranitePickaxe(), Items.GRANITE, Items.STONE_PICKAXE, 1);
        addBlockToolRecipeWithCore("Granite Axe", BlockTools.createGraniteAxe(), Items.GRANITE, Items.STONE_AXE, 1);
        addBlockToolRecipeWithCore("Granite Shovel", BlockTools.createGraniteShovel(), Items.GRANITE, Items.STONE_SHOVEL, 1);
        addBlockToolRecipeWithCore("Granite Hoe", BlockTools.createGraniteHoe(), Items.GRANITE, Items.STONE_HOE, 1);
        addBlockToolRecipeWithCore("Diorite Pickaxe", BlockTools.createDioritePickaxe(), Items.DIORITE, Items.STONE_PICKAXE, 1);
        addBlockToolRecipeWithCore("Diorite Axe", BlockTools.createDioriteAxe(), Items.DIORITE, Items.STONE_AXE, 1);
        addBlockToolRecipeWithCore("Diorite Shovel", BlockTools.createDioriteShovel(), Items.DIORITE, Items.STONE_SHOVEL, 1);
        addBlockToolRecipeWithCore("Diorite Hoe", BlockTools.createDioriteHoe(), Items.DIORITE, Items.STONE_HOE, 1);
        addBlockToolRecipeWithCore("Andesite Pickaxe", BlockTools.createAndesitePickaxe(), Items.ANDESITE, Items.STONE_PICKAXE, 1);
        addBlockToolRecipeWithCore("Andesite Axe", BlockTools.createAndesiteAxe(), Items.ANDESITE, Items.STONE_AXE, 1);
        addBlockToolRecipeWithCore("Andesite Shovel", BlockTools.createAndesiteShovel(), Items.ANDESITE, Items.STONE_SHOVEL, 1);
        addBlockToolRecipeWithCore("Andesite Hoe", BlockTools.createAndesiteHoe(), Items.ANDESITE, Items.STONE_HOE, 1);
        addBlockToolRecipeWithCore("Polished Granite Pickaxe", BlockTools.createPolishedGranitePickaxe(), Items.POLISHED_GRANITE, Items.IRON_PICKAXE, 2);
        addBlockToolRecipeWithCore("Polished Granite Axe", BlockTools.createPolishedGraniteAxe(), Items.POLISHED_GRANITE, Items.IRON_AXE, 2);
        addBlockToolRecipeWithCore("Polished Granite Shovel", BlockTools.createPolishedGraniteShovel(), Items.POLISHED_GRANITE, Items.IRON_SHOVEL, 2);
        addBlockToolRecipeWithCore("Polished Granite Hoe", BlockTools.createPolishedGraniteHoe(), Items.POLISHED_GRANITE, Items.IRON_HOE, 2);
        addBlockToolRecipeWithCore("Polished Diorite Pickaxe", BlockTools.createPolishedDioritePickaxe(), Items.POLISHED_DIORITE, Items.IRON_PICKAXE, 2);
        addBlockToolRecipeWithCore("Polished Diorite Axe", BlockTools.createPolishedDioriteAxe(), Items.POLISHED_DIORITE, Items.IRON_AXE, 2);
        addBlockToolRecipeWithCore("Polished Diorite Shovel", BlockTools.createPolishedDioriteShovel(), Items.POLISHED_DIORITE, Items.IRON_SHOVEL, 2);
        addBlockToolRecipeWithCore("Polished Diorite Hoe", BlockTools.createPolishedDioriteHoe(), Items.POLISHED_DIORITE, Items.IRON_HOE, 2);
        addBlockToolRecipeWithCore("Polished Andesite Pickaxe", BlockTools.createPolishedAndesitePickaxe(), Items.POLISHED_ANDESITE, Items.IRON_PICKAXE, 2);
        addBlockToolRecipeWithCore("Polished Andesite Axe", BlockTools.createPolishedAndesiteAxe(), Items.POLISHED_ANDESITE, Items.IRON_AXE, 2);
        addBlockToolRecipeWithCore("Polished Andesite Shovel", BlockTools.createPolishedAndesiteShovel(), Items.POLISHED_ANDESITE, Items.IRON_SHOVEL, 2);
        addBlockToolRecipeWithCore("Polished Andesite Hoe", BlockTools.createPolishedAndesiteHoe(), Items.POLISHED_ANDESITE, Items.IRON_HOE, 2);
        addBlockToolRecipeWithCore("Packed Ice Pickaxe", BlockTools.createPackedIcePickaxe(), Items.PACKED_ICE, Items.STONE_PICKAXE, 1);
        addBlockToolRecipeWithCore("Packed Ice Axe", BlockTools.createPackedIceAxe(), Items.PACKED_ICE, Items.STONE_AXE, 1);
        addBlockToolRecipeWithCore("Packed Ice Shovel", BlockTools.createPackedIceShovel(), Items.PACKED_ICE, Items.STONE_SHOVEL, 1);
        addBlockToolRecipeWithCore("Packed Ice Hoe", BlockTools.createPackedIceHoe(), Items.PACKED_ICE, Items.STONE_HOE, 1);
        addBlockToolRecipeWithCore("Blue Ice Pickaxe", BlockTools.createBlueIcePickaxe(), Items.BLUE_ICE, Items.IRON_PICKAXE, 2);
        addBlockToolRecipeWithCore("Blue Ice Axe", BlockTools.createBlueIceAxe(), Items.BLUE_ICE, Items.IRON_AXE, 2);
        addBlockToolRecipeWithCore("Blue Ice Shovel", BlockTools.createBlueIceShovel(), Items.BLUE_ICE, Items.IRON_SHOVEL, 2);
        addBlockToolRecipeWithCore("Blue Ice Hoe", BlockTools.createBlueIceHoe(), Items.BLUE_ICE, Items.IRON_HOE, 2);
        addBlockToolRecipeWithCore("Nether Gold Ore Pickaxe", BlockTools.createNetherGoldOrePickaxe(), Items.NETHER_GOLD_ORE, Items.STONE_PICKAXE, 2);
        addBlockToolRecipeWithCore("Nether Gold Ore Axe", BlockTools.createNetherGoldOreAxe(), Items.NETHER_GOLD_ORE, Items.STONE_AXE, 2);
        addBlockToolRecipeWithCore("Nether Gold Ore Shovel", BlockTools.createNetherGoldOreShovel(), Items.NETHER_GOLD_ORE, Items.STONE_SHOVEL, 2);
        addBlockToolRecipeWithCore("Nether Gold Ore Hoe", BlockTools.createNetherGoldOreHoe(), Items.NETHER_GOLD_ORE, Items.STONE_HOE, 2);
        addBlockToolRecipeWithCore("Dark Oak Log Pickaxe", BlockTools.createDarkOakLogPickaxe(), Items.DARK_OAK_LOG, Items.STONE_PICKAXE, 1);
        addBlockToolRecipeWithCore("Dark Oak Log Axe", BlockTools.createDarkOakLogAxe(), Items.DARK_OAK_LOG, Items.STONE_AXE, 1);
        addBlockToolRecipeWithCore("Dark Oak Log Shovel", BlockTools.createDarkOakLogShovel(), Items.DARK_OAK_LOG, Items.STONE_SHOVEL, 1);
        addBlockToolRecipeWithCore("Dark Oak Log Hoe", BlockTools.createDarkOakLogHoe(), Items.DARK_OAK_LOG, Items.STONE_HOE, 1);
        addBlockToolRecipeWithCore("Jungle Log Pickaxe", BlockTools.createJungleLogPickaxe(), Items.JUNGLE_LOG, Items.STONE_PICKAXE, 1);
        addBlockToolRecipeWithCore("Jungle Log Axe", BlockTools.createJungleLogAxe(), Items.JUNGLE_LOG, Items.STONE_AXE, 1);
        addBlockToolRecipeWithCore("Jungle Log Shovel", BlockTools.createJungleLogShovel(), Items.JUNGLE_LOG, Items.STONE_SHOVEL, 1);
        addBlockToolRecipeWithCore("Jungle Log Hoe", BlockTools.createJungleLogHoe(), Items.JUNGLE_LOG, Items.STONE_HOE, 1);
        addBlockToolRecipeWithCore("Acacia Log Pickaxe", BlockTools.createAcaciaLogPickaxe(), Items.ACACIA_LOG, Items.STONE_PICKAXE, 1);
        addBlockToolRecipeWithCore("Acacia Log Axe", BlockTools.createAcaciaLogAxe(), Items.ACACIA_LOG, Items.STONE_AXE, 1);
        addBlockToolRecipeWithCore("Acacia Log Shovel", BlockTools.createAcaciaLogShovel(), Items.ACACIA_LOG, Items.STONE_SHOVEL, 1);
        addBlockToolRecipeWithCore("Acacia Log Hoe", BlockTools.createAcaciaLogHoe(), Items.ACACIA_LOG, Items.STONE_HOE, 1);
        addBlockToolRecipeWithCore("Mangrove Log Pickaxe", BlockTools.createMangroveLogPickaxe(), Items.MANGROVE_LOG, Items.STONE_PICKAXE, 1);
        addBlockToolRecipeWithCore("Mangrove Log Axe", BlockTools.createMangroveLogAxe(), Items.MANGROVE_LOG, Items.STONE_AXE, 1);
        addBlockToolRecipeWithCore("Mangrove Log Shovel", BlockTools.createMangroveLogShovel(), Items.MANGROVE_LOG, Items.STONE_SHOVEL, 1);
        addBlockToolRecipeWithCore("Mangrove Log Hoe", BlockTools.createMangroveLogHoe(), Items.MANGROVE_LOG, Items.STONE_HOE, 1);
        addBlockToolRecipeWithCore("Cherry Log Pickaxe", BlockTools.createCherryLogPickaxe(), Items.CHERRY_LOG, Items.STONE_PICKAXE, 1);
        addBlockToolRecipeWithCore("Cherry Log Axe", BlockTools.createCherryLogAxe(), Items.CHERRY_LOG, Items.STONE_AXE, 1);
        addBlockToolRecipeWithCore("Cherry Log Shovel", BlockTools.createCherryLogShovel(), Items.CHERRY_LOG, Items.STONE_SHOVEL, 1);
        addBlockToolRecipeWithCore("Cherry Log Hoe", BlockTools.createCherryLogHoe(), Items.CHERRY_LOG, Items.STONE_HOE, 1);
        addBlockToolRecipeWithCore("Bamboo Block Pickaxe", BlockTools.createBambooBlockPickaxe(), Items.BAMBOO_BLOCK, Items.WOODEN_PICKAXE, 1);
        addBlockToolRecipeWithCore("Bamboo Block Axe", BlockTools.createBambooBlockAxe(), Items.BAMBOO_BLOCK, Items.WOODEN_AXE, 1);
        addBlockToolRecipeWithCore("Bamboo Block Shovel", BlockTools.createBambooBlockShovel(), Items.BAMBOO_BLOCK, Items.WOODEN_SHOVEL, 1);
        addBlockToolRecipeWithCore("Bamboo Block Hoe", BlockTools.createBambooBlockHoe(), Items.BAMBOO_BLOCK, Items.WOODEN_HOE, 1);
        addBlockToolRecipeWithCore("Tuff Pickaxe", BlockTools.createTuffPickaxe(), Items.TUFF, Items.STONE_PICKAXE, 1);
        addBlockToolRecipeWithCore("Tuff Axe", BlockTools.createTuffAxe(), Items.TUFF, Items.STONE_AXE, 1);
        addBlockToolRecipeWithCore("Tuff Shovel", BlockTools.createTuffShovel(), Items.TUFF, Items.STONE_SHOVEL, 1);
        addBlockToolRecipeWithCore("Tuff Hoe", BlockTools.createTuffHoe(), Items.TUFF, Items.STONE_HOE, 1);
        addBlockToolRecipeWithCore("Polished Tuff Pickaxe", BlockTools.createPolishedTuffPickaxe(), Items.POLISHED_TUFF, Items.IRON_PICKAXE, 2);
        addBlockToolRecipeWithCore("Polished Tuff Axe", BlockTools.createPolishedTuffAxe(), Items.POLISHED_TUFF, Items.IRON_AXE, 2);
        addBlockToolRecipeWithCore("Polished Tuff Shovel", BlockTools.createPolishedTuffShovel(), Items.POLISHED_TUFF, Items.IRON_SHOVEL, 2);
        addBlockToolRecipeWithCore("Polished Tuff Hoe", BlockTools.createPolishedTuffHoe(), Items.POLISHED_TUFF, Items.IRON_HOE, 2);
        addBlockToolRecipeWithCore("Nether Wart Block Pickaxe", BlockTools.createNetherWartBlockPickaxe(), Items.NETHER_WART_BLOCK, Items.WOODEN_PICKAXE, 2);
        addBlockToolRecipeWithCore("Nether Wart Block Axe", BlockTools.createNetherWartBlockAxe(), Items.NETHER_WART_BLOCK, Items.WOODEN_AXE, 2);
        addBlockToolRecipeWithCore("Nether Wart Block Shovel", BlockTools.createNetherWartBlockShovel(), Items.NETHER_WART_BLOCK, Items.WOODEN_SHOVEL, 2);
        addBlockToolRecipeWithCore("Nether Wart Block Hoe", BlockTools.createNetherWartBlockHoe(), Items.NETHER_WART_BLOCK, Items.WOODEN_HOE, 2);
        
        // ===== ENCHANTED BLOCK RECIPES (64 base = 1 enchanted) =====
        addEnchantedBlockRecipe("Enchanted Cobblestone", SlayerItems.createEnchantedCobblestone(), Items.COBBLESTONE);
        addEnchantedBlockRecipe("Enchanted Stone", SlayerItems.createEnchantedStone(), Items.STONE);
        addEnchantedBlockRecipe("Enchanted Iron Block", SlayerItems.createEnchantedIronBlock(), Items.IRON_BLOCK);
        addEnchantedBlockRecipe("Enchanted Gold Block", SlayerItems.createEnchantedGoldBlock(), Items.GOLD_BLOCK);
        addEnchantedBlockRecipe("Enchanted Diamond Block", SlayerItems.createEnchantedDiamondBlock(), Items.DIAMOND_BLOCK);
        addEnchantedBlockRecipe("Enchanted Emerald Block", SlayerItems.createEnchantedEmeraldBlock(), Items.EMERALD_BLOCK);
        addEnchantedBlockRecipe("Enchanted Lapis Block", SlayerItems.createEnchantedLapisBlock(), Items.LAPIS_BLOCK);
        addEnchantedBlockRecipe("Enchanted Coal Block", SlayerItems.createEnchantedCoalBlock(), Items.COAL_BLOCK);
        addEnchantedBlockRecipe("Enchanted Copper Block", SlayerItems.createEnchantedCopperBlock(), Items.COPPER_BLOCK);
        addEnchantedBlockRecipe("Enchanted Netherite Block", SlayerItems.createEnchantedNetheriteBlock(), Items.NETHERITE_BLOCK);
        addEnchantedBlockRecipe("Enchanted Quartz Block", SlayerItems.createEnchantedQuartzBlock(), Items.QUARTZ_BLOCK);
        addEnchantedBlockRecipe("Enchanted Redstone Block", SlayerItems.createEnchantedRedstoneBlock(), Items.REDSTONE_BLOCK);
        addEnchantedBlockRecipe("Enchanted Blackstone", SlayerItems.createEnchantedBlackstone(), Items.BLACKSTONE);
        addEnchantedBlockRecipe("Enchanted Gilded Blackstone", SlayerItems.createEnchantedGildedBlackstone(), Items.GILDED_BLACKSTONE);
        addEnchantedBlockRecipe("Enchanted Oak Log", SlayerItems.createEnchantedOakLog(), Items.OAK_LOG);
        addEnchantedBlockRecipe("Enchanted Sand", SlayerItems.createEnchantedSand(), Items.SAND);
        addEnchantedBlockRecipe("Enchanted Red Sand", SlayerItems.createEnchantedRedSand(), Items.RED_SAND);
        addEnchantedBlockRecipe("Enchanted Gravel", SlayerItems.createEnchantedGravel(), Items.GRAVEL);
        addEnchantedBlockRecipe("Enchanted Netherrack", SlayerItems.createEnchantedNetherrack(), Items.NETHERRACK);
        addEnchantedBlockRecipe("Enchanted End Stone", SlayerItems.createEnchantedEndStone(), Items.END_STONE);
        addEnchantedBlockRecipe("Enchanted Obsidian", SlayerItems.createEnchantedObsidian(), Items.OBSIDIAN);
        addEnchantedBlockRecipe("Enchanted Glowstone", SlayerItems.createEnchantedGlowstone(), Items.GLOWSTONE);
        addEnchantedBlockRecipe("Enchanted Prismarine", SlayerItems.createEnchantedPrismarine(), Items.PRISMARINE);
        addEnchantedBlockRecipe("Enchanted Basalt", SlayerItems.createEnchantedBasalt(), Items.BASALT);
        addEnchantedBlockRecipe("Enchanted Deepslate", SlayerItems.createEnchantedDeepslate(), Items.DEEPSLATE);
        addEnchantedBlockRecipe("Enchanted Cobbled Deepslate", SlayerItems.createEnchantedCobbledDeepslate(), Items.COBBLED_DEEPSLATE);
        addEnchantedBlockRecipe("Enchanted Tuff", SlayerItems.createEnchantedTuff(), Items.TUFF);
        addEnchantedBlockRecipe("Enchanted Calcite", SlayerItems.createEnchantedCalcite(), Items.CALCITE);
        addEnchantedBlockRecipe("Enchanted Amethyst Block", SlayerItems.createEnchantedAmethystBlock(), Items.AMETHYST_BLOCK);
        addEnchantedBlockRecipe("Enchanted Glass", SlayerItems.createEnchantedGlass(), Items.GLASS);
        addEnchantedBlockRecipe("Enchanted Ice", SlayerItems.createEnchantedIce(), Items.ICE);
        addEnchantedBlockRecipe("Enchanted Packed Ice", SlayerItems.createEnchantedPackedIce(), Items.PACKED_ICE);
        addEnchantedBlockRecipe("Enchanted Blue Ice", SlayerItems.createEnchantedBlueIce(), Items.BLUE_ICE);
        addEnchantedBlockRecipe("Enchanted Terracotta", SlayerItems.createEnchantedTerracotta(), Items.TERRACOTTA);
        addEnchantedBlockRecipe("Enchanted Clay", SlayerItems.createEnchantedClay(), Items.CLAY);
        addEnchantedBlockRecipe("Enchanted Bricks", SlayerItems.createEnchantedBrick(), Items.BRICKS);
        addEnchantedBlockRecipe("Enchanted Nether Bricks", SlayerItems.createEnchantedNetherBrick(), Items.NETHER_BRICKS);
        addEnchantedBlockRecipe("Enchanted Purpur Block", SlayerItems.createEnchantedPurpurBlock(), Items.PURPUR_BLOCK);
        addEnchantedBlockRecipe("Enchanted Sea Lantern", SlayerItems.createEnchantedSeaLantern(), Items.SEA_LANTERN);
        addEnchantedBlockRecipe("Enchanted Shroomlight", SlayerItems.createEnchantedShroomlight(), Items.SHROOMLIGHT);
        
        // Enchanted Crops
        addEnchantedBlockRecipe("Enchanted Wheat", SlayerItems.createEnchantedWheat(), Items.WHEAT);
        addEnchantedBlockRecipe("Enchanted Carrot", SlayerItems.createEnchantedCarrot(), Items.CARROT);
        addEnchantedBlockRecipe("Enchanted Potato", SlayerItems.createEnchantedPotato(), Items.POTATO);
        addEnchantedBlockRecipe("Enchanted Sugar Cane", SlayerItems.createEnchantedSugarCane(), Items.SUGAR_CANE);
        addEnchantedBlockRecipe("Enchanted Rose", SlayerItems.createEnchantedRose(), Items.POPPY);
        addEnchantedBlockRecipe("Enchanted Dandelion", SlayerItems.createEnchantedDandelion(), Items.DANDELION);
    }
    
    private static void addBlockWeaponRecipe(String name, ItemStack result, Item blockItem, Item weaponBase) {
        ItemStack[] ingredients = new ItemStack[] {
            new ItemStack(blockItem), new ItemStack(blockItem), new ItemStack(blockItem),
            new ItemStack(blockItem), new ItemStack(weaponBase), new ItemStack(blockItem),
            ItemStack.EMPTY, new ItemStack(blockItem), ItemStack.EMPTY
        };
        RECIPES.add(new Recipe(name, result, ingredients, null, 0));
    }
    
    private static void addBlockToolRecipeWithCore(String name, ItemStack result, Item blockItem, Item toolBase, int coreTier) {
        ItemStack core = coreTier == 1 ? SlayerItems.createCustomCrafterCoreT1() : 
                         coreTier == 2 ? SlayerItems.createCustomCrafterCoreT2() : 
                         SlayerItems.createCustomCrafterCoreT3();
        ItemStack[] ingredients = new ItemStack[] {
            new ItemStack(blockItem), new ItemStack(blockItem), new ItemStack(blockItem),
            new ItemStack(blockItem), core, new ItemStack(blockItem),
            ItemStack.EMPTY, new ItemStack(toolBase), ItemStack.EMPTY
        };
        RECIPES.add(new Recipe(name, result, ingredients, null, 0));
    }
    
    /**
     * Adds an enchanted block recipe - 64 base items fill all 9 slots (7 full stacks + 1 partial)
     */
    private static void addEnchantedBlockRecipe(String name, ItemStack result, Item baseItem) {
        // 64 items = 7 stacks of 9 + 1 item = fill all 9 slots with base item
        ItemStack[] ingredients = new ItemStack[] {
            new ItemStack(baseItem, 64), new ItemStack(baseItem, 64), new ItemStack(baseItem, 64),
            new ItemStack(baseItem, 64), new ItemStack(baseItem, 64), new ItemStack(baseItem, 64),
            new ItemStack(baseItem, 64), new ItemStack(baseItem, 64), new ItemStack(baseItem, 64)
        };
        RECIPES.add(new Recipe(name, result, ingredients, null, 0));
    }
    
    private static void addBlockArmorRecipe(String name, ItemStack result, Item blockItem, Item armorBase) {
        ItemStack[] ingredients;
        if (name.contains("Helmet")) {
            ingredients = new ItemStack[] {
                new ItemStack(blockItem), new ItemStack(blockItem), new ItemStack(blockItem),
                new ItemStack(blockItem), SlayerItems.createCustomCrafterCoreT1(), new ItemStack(blockItem),
                ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
            };
        } else if (name.contains("Chestplate")) {
            ingredients = new ItemStack[] {
                new ItemStack(blockItem), new ItemStack(blockItem), new ItemStack(blockItem),
                new ItemStack(blockItem), SlayerItems.createCustomCrafterCoreT1(), new ItemStack(blockItem),
                new ItemStack(blockItem), new ItemStack(blockItem), new ItemStack(blockItem)
            };
        } else if (name.contains("Leggings")) {
            ingredients = new ItemStack[] {
                new ItemStack(blockItem), new ItemStack(blockItem), new ItemStack(blockItem),
                new ItemStack(blockItem), SlayerItems.createCustomCrafterCoreT1(), new ItemStack(blockItem),
                new ItemStack(blockItem), ItemStack.EMPTY, new ItemStack(blockItem)
            };
        } else { // Boots
            ingredients = new ItemStack[] {
                ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY,
                new ItemStack(blockItem), SlayerItems.createCustomCrafterCoreT1(), new ItemStack(blockItem),
                new ItemStack(blockItem), ItemStack.EMPTY, new ItemStack(blockItem)
            };
        }
        RECIPES.add(new Recipe(name, result, ingredients, null, 0));
    }

    private static void addBlockArmorRecipeWithCore(String name, ItemStack result, Item blockItem, Item armorBase, int coreTier) {
        ItemStack core = coreTier == 1 ? SlayerItems.createCustomCrafterCoreT1() : 
                         coreTier == 2 ? SlayerItems.createCustomCrafterCoreT2() : 
                         SlayerItems.createCustomCrafterCoreT3();
        ItemStack[] ingredients;
        if (name.contains("Helmet")) {
            ingredients = new ItemStack[] {
                new ItemStack(blockItem), new ItemStack(blockItem), new ItemStack(blockItem),
                new ItemStack(blockItem), core, new ItemStack(blockItem),
                ItemStack.EMPTY, new ItemStack(armorBase), ItemStack.EMPTY
            };
        } else if (name.contains("Chestplate")) {
            ingredients = new ItemStack[] {
                new ItemStack(blockItem), new ItemStack(blockItem), new ItemStack(blockItem),
                new ItemStack(blockItem), core, new ItemStack(blockItem),
                new ItemStack(blockItem), new ItemStack(armorBase), new ItemStack(blockItem)
            };
        } else if (name.contains("Leggings")) {
            ingredients = new ItemStack[] {
                new ItemStack(blockItem), new ItemStack(blockItem), new ItemStack(blockItem),
                new ItemStack(blockItem), core, new ItemStack(blockItem),
                new ItemStack(blockItem), new ItemStack(armorBase), new ItemStack(blockItem)
            };
        } else { // Boots
            ingredients = new ItemStack[] {
                ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY,
                new ItemStack(blockItem), core, new ItemStack(blockItem),
                new ItemStack(blockItem), new ItemStack(armorBase), new ItemStack(blockItem)
            };
        }
        RECIPES.add(new Recipe(name, result, ingredients, null, 0));
    }

    private static void addBlockWeaponRecipeWithCore(String name, ItemStack result, Item blockItem, Item weaponBase, int coreTier) {
        ItemStack core = coreTier == 1 ? SlayerItems.createCustomCrafterCoreT1() : 
                         coreTier == 2 ? SlayerItems.createCustomCrafterCoreT2() : 
                         SlayerItems.createCustomCrafterCoreT3();
        ItemStack[] ingredients = new ItemStack[] {
            new ItemStack(blockItem), new ItemStack(blockItem), new ItemStack(blockItem),
            new ItemStack(blockItem), core, new ItemStack(blockItem),
            ItemStack.EMPTY, new ItemStack(weaponBase), ItemStack.EMPTY
        };
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
