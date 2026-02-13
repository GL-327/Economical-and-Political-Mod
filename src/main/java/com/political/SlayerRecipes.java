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
    public static final int SLIME_BOOTS_LEVEL_REQ = 8;
    public static final int WARDEN_CHESTPLATE_LEVEL_REQ = 12;
    public static final int SKELETON_BOW_LEVEL_REQ = 10;
    public static final int SPIDER_LEGGINGS_LEVEL_REQ = 10;
    // ============================================================
// WARDEN CHESTPLATE - With increased durability
// ============================================================
    public static ItemStack createWardenChestplate() {
        ItemStack chestplate = new ItemStack(Items.NETHERITE_CHESTPLATE);

        // Set custom name
        chestplate.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("💀 Sculk Terror Chestplate")
                        .formatted(Formatting.DARK_AQUA, Formatting.BOLD));

        // INCREASED DURABILITY - 10x default netherite chestplate (407 -> 4070)
        chestplate.set(DataComponentTypes.MAX_DAMAGE, NETHERITE_CHESTPLATE_DURABILITY);
        chestplate.set(DataComponentTypes.DAMAGE, 0);

        // Add lore
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("LEGENDARY ARMOR").formatted(Formatting.GOLD, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Forged from the sculk of").formatted(Formatting.GRAY));
        lore.add(Text.literal("the deepest ancient cities.").formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ EFFECTS ━━━").formatted(Formatting.DARK_AQUA));
        lore.add(Text.literal(""));
        lore.add(Text.literal("👁 ESP: ").formatted(Formatting.WHITE)
                .append(Text.literal("See noisy entities").formatted(Formatting.AQUA)));
        lore.add(Text.literal("🌙 Night Vision: ").formatted(Formatting.WHITE)
                .append(Text.literal("Permanent").formatted(Formatting.YELLOW)));
        lore.add(Text.literal("🛡 Darkness Immunity").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("⚠ Requires: Warden Bounty Lvl " + WARDEN_CHESTPLATE_LEVEL_REQ)
                .formatted(Formatting.RED));

        chestplate.set(DataComponentTypes.LORE, new LoreComponent(lore));
        chestplate.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);

        return chestplate;
    }

    // ============================================================
// SKELETON BOW - With increased durability
// ============================================================
    public static ItemStack createSkeletonBow() {
        ItemStack bow = new ItemStack(Items.BOW);

        // Set custom name
        bow.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("🏹 Bone Desperado's Longbow")
                        .formatted(Formatting.WHITE, Formatting.BOLD));

        // INCREASED DURABILITY - 10x default bow (384 -> 3840)
        bow.set(DataComponentTypes.MAX_DAMAGE, BOW_DURABILITY);
        bow.set(DataComponentTypes.DAMAGE, 0);

        // Add lore
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("LEGENDARY WEAPON").formatted(Formatting.GOLD, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Carved from the spine of").formatted(Formatting.GRAY));
        lore.add(Text.literal("The Bone Desperado himself.").formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ EFFECTS ━━━").formatted(Formatting.WHITE));
        lore.add(Text.literal(""));
        lore.add(Text.literal("🎯 Auto-Lock: ").formatted(Formatting.WHITE)
                .append(Text.literal("Arrows home to targets").formatted(Formatting.YELLOW)));
        lore.add(Text.literal("💀 Headshot: ").formatted(Formatting.WHITE)
                .append(Text.literal("5x Damage").formatted(Formatting.RED, Formatting.BOLD)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("⚠ Requires: Skeleton Bounty Lvl " + SKELETON_BOW_LEVEL_REQ)
                .formatted(Formatting.RED));

        bow.set(DataComponentTypes.LORE, new LoreComponent(lore));
        bow.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);

        return bow;
    }
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
    // In SlayerItems.java - Add durability component when creating armor

    public static ItemStack createZombieHelmet() {
        ItemStack helmet = new ItemStack(Items.LEATHER_HELMET);

        // Set custom name
        helmet.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Berserker Helmet").formatted(Formatting.DARK_GREEN, Formatting.BOLD));

        // INCREASE DURABILITY - Set max damage higher than default leather (55)
        helmet.set(DataComponentTypes.MAX_DAMAGE, 550); // 10x durability
        helmet.set(DataComponentTypes.DAMAGE, 0);

        // Set leather color (dark red for zombie berserker)
        helmet.set(DataComponentTypes.DYED_COLOR,
                new net.minecraft.component.type.DyedColorComponent(0x8B0000));

        // Add lore
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8§m                              "));
        lore.add(Text.literal("§2§lZOMBIE SLAYER SET"));
        lore.add(Text.literal("§8§m                              "));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§7Passive: §cBerserker Rage"));
        lore.add(Text.literal("§7Deal more damage at low HP"));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§7Bonus Armor: §a+3"));
        lore.add(Text.literal("§7Bonus Toughness: §a+2"));
        helmet.set(DataComponentTypes.LORE, new net.minecraft.component.type.LoreComponent(lore));

        return helmet;
    }

// Apply same pattern to ALL custom armor pieces:
// Spider Leggings: 750 durability (default 75)
// Skeleton Chestplate: 800 durability (default 80)
// Slime Boots: 650 durability (default 65)
// Warden Chestplate: 1000 durability (default 80)
}