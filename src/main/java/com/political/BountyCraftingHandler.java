package com.political;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.List;

public class BountyCraftingHandler {

    /**
     * Previously used UseBlockCallback to intercept crafting table right-clicks.
     * Now crafting is handled via CraftingResultMixin which calls checkBountyGridRecipe.
     * BeamCraftingHandler still uses UseBlockCallback for the smithing table (HPEBM upgrades).
     */
    public static void register() {
        // Grid-based crafting is handled via CraftingResultMixin.
        // HPEBM Mk1 and The Gavel are Underground Auction exclusive — no crafting recipes.
    }

    // ============================================================
    // GRID-BASED RECIPE CHECKER
    // Called by CraftingResultMixin when the crafting grid changes.
    // Returns the result ItemStack if the grid matches a bounty recipe,
    // or null if no recipe matches (or the player doesn't meet the level).
    // Ingredient consumption is handled automatically by vanilla (each
    // non-empty slot is decremented by 1 when the player takes the result).
    // ============================================================
    public static ItemStack checkBountyGridRecipe(List<ItemStack> grid, PlayerEntity player) {
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return null;
        if (grid.size() != 9) return null;

        int totalItems = getTotalGridItems(grid);

        // ─── Check custom recipes from RecipeConfigManager first ───────────
        for (SlayerRecipes.Recipe customRecipe : RecipeConfigManager.getCustomRecipes()) {
            if (!RecipeConfigManager.isRecipeEnabled(customRecipe.name)) continue;
            if (matchesCustomRecipeShapeless(grid, customRecipe)) {
                int reqLevel = customRecipe.requiredLevel;
                SlayerManager.SlayerType reqType = customRecipe.requiredSlayer;
                if (reqType != null && reqLevel > 0) {
                    int level = SlayerData.getSlayerLevel(serverPlayer.getUuidAsString(), reqType);
                    if (level < reqLevel) return null;
                }
                return customRecipe.result.copy();
            }
        }

        // ─── Special Armor Recipes (all use 6 slots) ───────────────────────

        // Zombie Berserker Helmet: 3 Zombie Cores + 1 Zombie Head + 2 Rotten Flesh
        if (totalItems == 6
                && countGridCores(grid, SlayerManager.SlayerType.ZOMBIE) == 3
                && countGridVanilla(grid, Items.ZOMBIE_HEAD) == 1
                && countGridVanilla(grid, Items.ROTTEN_FLESH) == 2) {
            int level = SlayerData.getSlayerLevel(serverPlayer.getUuidAsString(), SlayerManager.SlayerType.ZOMBIE);
            if (level < SlayerItems.ZOMBIE_BERSERKER_LEVEL_REQ) return null;
            return SlayerItems.createZombieBerserkerHelmet();
        }

        // Spider Leggings: 3 Spider Cores + 2 String + 1 Spider Eye
        if (totalItems == 6
                && countGridCores(grid, SlayerManager.SlayerType.SPIDER) == 3
                && countGridVanilla(grid, Items.STRING) == 2
                && countGridVanilla(grid, Items.SPIDER_EYE) == 1) {
            int level = SlayerData.getSlayerLevel(serverPlayer.getUuidAsString(), SlayerManager.SlayerType.SPIDER);
            if (level < SlayerItems.SPIDER_LEGGINGS_LEVEL_REQ) return null;
            return SlayerItems.createSpiderLeggings();
        }

        // Skeleton Bow: 3 Skeleton Cores + 1 Bow + 2 Bones
        if (totalItems == 6
                && countGridCores(grid, SlayerManager.SlayerType.SKELETON) == 3
                && countGridVanilla(grid, Items.BOW) == 1
                && countGridVanilla(grid, Items.BONE) == 2) {
            int level = SlayerData.getSlayerLevel(serverPlayer.getUuidAsString(), SlayerManager.SlayerType.SKELETON);
            if (level < SlayerItems.SKELETON_BOW_LEVEL_REQ) return null;
            return SlayerItems.createSkeletonBow();
        }

        // Slime Boots: 3 Slime Cores + 2 Slime Balls + 1 Custom Crafting Core (T1)
        if (totalItems == 6
                && countGridCores(grid, SlayerManager.SlayerType.SLIME) == 3
                && countGridVanilla(grid, Items.SLIME_BALL) == 2
                && countGridCustomCores(grid, "custom_crafter_core_t1") == 1) {
            int level = SlayerData.getSlayerLevel(serverPlayer.getUuidAsString(), SlayerManager.SlayerType.SLIME);
            if (level < SlayerItems.SLIME_BOOTS_LEVEL_REQ) return null;
            return SlayerItems.createSlimeBoots();
        }

        // Warden Chestplate: 3 Warden Cores + 2 Echo Shards + 1 Custom Crafting Core (T2)
        if (totalItems == 6
                && countGridCores(grid, SlayerManager.SlayerType.IRON_GOLEM) == 3
                && countGridVanilla(grid, Items.ECHO_SHARD) == 2
                && countGridCustomCores(grid, "custom_crafter_core_t2") == 1) {
            int level = SlayerData.getSlayerLevel(serverPlayer.getUuidAsString(), SlayerManager.SlayerType.IRON_GOLEM);
            if (level < SlayerItems.IRON_GOLEM_CHESTPLATE_LEVEL_REQ) return null;
            return SlayerItems.createWardenChestplate();
        }

        // ─── T1 Bounty Swords: 3 Cores + 1 Custom Crafting Core (T1) + 1 Iron Sword + 1 Stick = 6 items ──
        if (totalItems == 6 && countGridVanilla(grid, Items.IRON_SWORD) == 1
                && countGridVanilla(grid, Items.STICK) == 1) {
            for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
                if (countGridCores(grid, type) == 3 && countGridCustomCores(grid, "custom_crafter_core_t1") == 1) {
                    int level = SlayerData.getSlayerLevel(serverPlayer.getUuidAsString(), type);
                    if (level < SlayerItems.BASIC_SWORD_LEVEL_REQ) return null;
                    return SlayerItems.createSlayerSword(type);
                }
            }
        }

        // ─── T2 Upgraded Swords: 3 Cores + 1 Custom Crafting Core (T2) + 1 T1 Sword = 5 items ─
        if (totalItems == 5) {
            for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
                if (countGridCores(grid, type) == 3 && countGridCustomCores(grid, "custom_crafter_core_t2") == 1 && countGridT1Swords(grid, type) == 1) {
                    int level = SlayerData.getSlayerLevel(serverPlayer.getUuidAsString(), type);
                    if (level < SlayerItems.UPGRADED_SWORD_LEVEL_REQ) return null;
                    return SlayerItems.createUpgradedSlayerSword(type);
                }
            }
        }

        // ─── T1 Armor: 3 Cores + 1 Custom Crafting Core (T1) + 2 Secondary = 6 items ──
        if (totalItems == 6) {
            for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
                if (countGridCores(grid, type) != 3) continue;
                Item secondary = getSecondaryMaterial(type);
                if (countGridVanilla(grid, secondary) != 2) continue;
                if (countGridCustomCores(grid, "custom_crafter_core_t1") == 1) {
                    int level = SlayerData.getSlayerLevel(serverPlayer.getUuidAsString(), type);
                    if (level < SlayerItems.T1_ARMOR_LEVEL_REQ) return null;
                    // Determine armor piece based on secondary material type
                    SlayerItems.ArmorPiece piece = getArmorPieceFromSecondary(secondary);
                    if (piece != null) {
                        return SlayerItems.createT1Armor(type, piece);
                    }
                }
            }
        }

        // ─── T2 Armor: 3 Cores + 1 Custom Crafting Core (T2) + 2 Secondary = 6 items ─
        if (totalItems == 6) {
            for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
                if (countGridCores(grid, type) != 3) continue;
                Item secondary = getSecondaryMaterial(type);
                if (countGridVanilla(grid, secondary) != 2) continue;
                if (countGridCustomCores(grid, "custom_crafter_core_t2") == 1) {
                    int level = SlayerData.getSlayerLevel(serverPlayer.getUuidAsString(), type);
                    if (level < SlayerItems.T2_ARMOR_LEVEL_REQ) return null;
                    // Determine armor piece based on secondary material type
                    SlayerItems.ArmorPiece piece = getArmorPieceFromSecondary(secondary);
                    if (piece != null) {
                        return SlayerItems.createT2Armor(type, piece);
                    }
                }
            }
        }

        // Crown of Greed: 2 Piglin Cores + 4 Piglin Flesh
        if (totalItems == 6) {
            int piglinCores = 0;
            int piglinFlesh = 0;
            for (ItemStack stack : grid) {
                if (stack.isEmpty()) continue;
                String cid = SlayerItems.getCustomItemId(stack);
                if ("piglin_core".equals(cid)) piglinCores++;
                else if ("piglin_flesh".equals(cid)) piglinFlesh++;
            }
            if (piglinCores == 2 && piglinFlesh == 4) {
                return SlayerItems.createCrownOfGreed();
            }
        }

        return null;
    }

    /** Count slots in grid that contain a Core of the given slayer type. */
    private static int countGridCores(List<ItemStack> grid, SlayerManager.SlayerType type) {
        String coreName = type.displayName + " Core";
        return countGridCores(grid, coreName);
    }

    private static int countGridCores(List<ItemStack> grid, String coreName) {
        int count = 0;
        for (ItemStack stack : grid) {
            if (stack.isEmpty()) continue;
            Text name = stack.get(DataComponentTypes.CUSTOM_NAME);
            if (name != null) {
                String n = name.getString();
                if (n.contains(coreName) && n.contains("✦")) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Count slots in grid that contain the given vanilla item.
     * Excludes protected (mod-custom) items to avoid counting custom equipment as vanilla materials.
     */
    private static int countGridVanilla(List<ItemStack> grid, Item item) {
        int count = 0;
        for (ItemStack stack : grid) {
            if (stack.isEmpty()) continue;
            if (stack.isOf(item) && !CustomItemHandler.isProtectedItem(stack)) {
                count++;
            }
        }
        return count;
    }

    /** Count slots in grid that contain a T1 bounty sword of the given type. */
    private static int countGridT1Swords(List<ItemStack> grid, SlayerManager.SlayerType type) {
        int count = 0;
        for (ItemStack stack : grid) {
            if (stack.isEmpty()) continue;
            if (SlayerItems.isSlayerSword(stack) && SlayerItems.getSwordSlayerType(stack) == type) {
                count++;
            }
        }
        return count;
    }

    /** Count slots in grid that contain a T1 armor piece of the given type and slot. */
    private static int countGridT1Armor(List<ItemStack> grid, SlayerManager.SlayerType type,
                                        SlayerItems.ArmorPiece piece) {
        int count = 0;
        for (ItemStack stack : grid) {
            if (stack.isEmpty()) continue;
            if (SlayerItems.isT1SlayerArmor(stack)
                    && SlayerItems.getArmorSlayerType(stack) == type
                    && SlayerItems.getArmorPiece(stack) == piece) {
                count++;
            }
        }
        return count;
    }

    /** Count slots in grid that contain a Custom Crafting Core with the given ID. */
    private static int countGridCustomCores(List<ItemStack> grid, String coreId) {
        int count = 0;
        for (ItemStack stack : grid) {
            if (stack.isEmpty()) continue;
            if (SlayerItems.hasCustomItemId(stack, coreId)) {
                count++;
            }
        }
        return count;
    }

    /** Total number of non-empty slots in the crafting grid. */
    private static int getTotalGridItems(List<ItemStack> grid) {
        int count = 0;
        for (ItemStack stack : grid) {
            if (!stack.isEmpty()) count++;
        }
        return count;
    }

    // ============================================================
    // RECIPE CONSTANTS HELPERS
    // ============================================================

    /** Returns the vanilla secondary material used in T1 and T2 armor for the given type. */
    private static Item getSecondaryMaterial(SlayerManager.SlayerType type) {
        return switch (type) {
            case ZOMBIE   -> Items.ROTTEN_FLESH;
            case SPIDER   -> Items.STRING;
            case SKELETON -> Items.BONE;
            case SLIME    -> Items.SLIME_BALL;
            case IRON_GOLEM -> Items.SCULK;
            case PIGLIN   -> Items.GOLD_NUGGET;
            case ENDERMAN -> Items.ENDER_PEARL;
        };
    }

    /** Returns the base vanilla leather armor item for the given armor piece slot. */
    private static Item getBaseLeatherArmor(SlayerItems.ArmorPiece piece) {
        return switch (piece) {
            case HELMET     -> Items.LEATHER_HELMET;
            case CHESTPLATE -> Items.LEATHER_CHESTPLATE;
            case LEGGINGS   -> Items.LEATHER_LEGGINGS;
            case BOOTS      -> Items.LEATHER_BOOTS;
        };
    }

    /** Returns the armor piece that corresponds to the given slayer type's secondary material. */
    private static SlayerItems.ArmorPiece getArmorPieceFromSecondary(Item secondary) {
        if (secondary == Items.ROTTEN_FLESH) return SlayerItems.ArmorPiece.HELMET;     // Zombie -> Helmet
        if (secondary == Items.STRING) return SlayerItems.ArmorPiece.LEGGINGS;         // Spider -> Leggings
        if (secondary == Items.BONE) return SlayerItems.ArmorPiece.CHESTPLATE;         // Skeleton -> Chestplate
        if (secondary == Items.SLIME_BALL) return SlayerItems.ArmorPiece.BOOTS;        // Slime -> Boots
        if (secondary == Items.SCULK) return SlayerItems.ArmorPiece.CHESTPLATE;        // Iron Golem -> Chestplate
        if (secondary == Items.GOLD_NUGGET) return SlayerItems.ArmorPiece.BOOTS;       // Piglin -> Boots
        if (secondary == Items.ENDER_PEARL) return SlayerItems.ArmorPiece.HELMET;      // Enderman -> Helmet
        return null;
    }

    /**
     * Checks whether the given 9-slot grid matches a custom recipe using shapeless matching
     * (counts items, ignoring position). Custom recipes stored by /setrecipe are shapeless.
     */
    private static boolean matchesCustomRecipeShapeless(List<ItemStack> grid, SlayerRecipes.Recipe recipe) {
        if (recipe.ingredients == null) return false;

        // Check slot locks first - if recipe has locked slots, positions must match exactly
        if (recipe.lockedSlots != null && hasLockedSlots(recipe.lockedSlots)) {
            return matchesCustomRecipeExact(grid, recipe);
        }

        // Build a count map of what's in the grid
        java.util.Map<String, Integer> gridCounts = new java.util.HashMap<>();
        for (ItemStack stack : grid) {
            if (stack.isEmpty()) continue;
            String key = getIngredientKey(stack);
            gridCounts.merge(key, 1, Integer::sum);
        }

        // Build a count map of what the recipe requires
        java.util.Map<String, Integer> recipeCounts = new java.util.HashMap<>();
        for (ItemStack ing : recipe.ingredients) {
            if (ing == null || ing.isEmpty()) continue;
            String key = getIngredientKey(ing);
            recipeCounts.merge(key, 1, Integer::sum);
        }

        return gridCounts.equals(recipeCounts);
    }

    /**
     * Checks whether the given 9-slot grid exactly matches a recipe (position-sensitive).
     * Used when the recipe has locked slots.
     */
    private static boolean matchesCustomRecipeExact(List<ItemStack> grid, SlayerRecipes.Recipe recipe) {
        if (recipe.ingredients == null || recipe.ingredients.length != 9) return false;

        for (int i = 0; i < 9; i++) {
            ItemStack gridItem = grid.get(i);
            ItemStack recipeItem = recipe.ingredients[i];

            // Check if items match (both empty or same type)
            if (gridItem.isEmpty() && (recipeItem == null || recipeItem.isEmpty())) {
                continue; // Both empty - good
            }
            if (!gridItem.isEmpty() && recipeItem != null && !recipeItem.isEmpty()) {
                // Both have items - check if they match
                String gridKey = getIngredientKey(gridItem);
                String recipeKey = getIngredientKey(recipeItem);
                if (!gridKey.equals(recipeKey)) {
                    return false; // Different items in this slot
                }
                continue; // Items match - good
            }
            // One is empty, other has item - mismatch
            return false;
        }
        return true;
    }

    /**
     * Checks if the recipe has any locked slots.
     */
    private static boolean hasLockedSlots(boolean[] lockedSlots) {
        if (lockedSlots == null) return false;
        for (boolean locked : lockedSlots) {
            if (locked) return true;
        }
        return false;
    }

    /** Returns a stable string key for a given ItemStack for recipe matching. */
    private static String getIngredientKey(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return "";
        // Prefer custom_item_id for mod items
        String customId = SlayerItems.getCustomItemId(stack);
        if (customId != null) return "political:" + customId;
        // Use vanilla registry name
        return net.minecraft.registry.Registries.ITEM.getId(stack.getItem()).toString();
    }
}
