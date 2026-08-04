package com.political;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public static ItemStack checkBountyGridRecipe(List<ItemStack> grid, Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) return null;
        if (grid.size() != 9) return null;

        // Only check custom recipes from RecipeConfigManager (/setrecipe)
        for (SlayerRecipes.Recipe customRecipe : RecipeConfigManager.getCustomRecipes()) {
            if (!RecipeConfigManager.isRecipeEnabled(customRecipe.name)) continue;
            if (matchesCustomRecipeShapeless(grid, customRecipe)) {
                int reqLevel = customRecipe.requiredLevel;
                SlayerManager.SlayerType reqType = customRecipe.requiredSlayer;
                if (reqType != null && reqLevel > 0) {
                    int level = SlayerData.getSlayerLevel(serverPlayer.getUUID().toString(), reqType);
                    if (level < reqLevel) return null;
                }
                return customRecipe.result.copy();
            }
        }

        return null;
    }

    /**
     * Returns true if {@code player} meets the recipe's requiredSlayer / requiredLevel gate.
     * Mirrors the gate enforced in {@link #checkBountyGridRecipe} and {@link #getMatchedCustomRecipe}
     * so alternative crafting paths (e.g. the /recipe Autocraft button, which bypasses the
     * grid-matching gate) can enforce the exact same restriction before handing over the result.
     *
     * @return true when the gate is satisfied (or the recipe has no gate); false otherwise
     */
    public static boolean meetsRecipeGate(ServerPlayer player, SlayerRecipes.Recipe recipe) {
        if (player == null || recipe == null) return false;
        int reqLevel = recipe.requiredLevel;
        SlayerManager.SlayerType reqType = recipe.requiredSlayer;
        if (reqType != null && reqLevel > 0) {
            int level = SlayerData.getSlayerLevel(player.getUUID().toString(), reqType);
            if (level < reqLevel) return false;
        }
        return true;
    }

    /**
     * Returns the matched custom (/setrecipe) recipe for this grid, or null if none match.
     * This is used by the crafting output slot logic to consume the correct ingredient counts.
     */
    public static SlayerRecipes.Recipe getMatchedCustomRecipe(List<ItemStack> grid, Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) return null;
        if (grid == null || grid.size() != 9) return null;

        // Only check custom recipes
        for (SlayerRecipes.Recipe customRecipe : RecipeConfigManager.getCustomRecipes()) {
            if (!RecipeConfigManager.isRecipeEnabled(customRecipe.name)) continue;
            if (!matchesCustomRecipeShapeless(grid, customRecipe)) continue;

            int reqLevel = customRecipe.requiredLevel;
            SlayerManager.SlayerType reqType = customRecipe.requiredSlayer;
            if (reqType != null && reqLevel > 0) {
                int level = SlayerData.getSlayerLevel(serverPlayer.getUUID().toString(), reqType);
                if (level < reqLevel) continue;
            }

            return customRecipe;
        }

        return null;
    }

    /**
     * Consumes ingredients for one craft of a custom (/setrecipe) recipe.
     * Locked recipes: per-slot amounts. Unlocked: slots with count &gt; 1 consume from that grid slot only;
     * count-1 ingredients are consumed shapelessly from remaining stacks.
     */
    public static void consumeCustomRecipeIngredients(CraftingContainer craftSlots, SlayerRecipes.Recipe recipe) {
        if (recipe.ingredients == null) return;

        if (recipe.lockedSlots != null && hasLockedSlots(recipe.lockedSlots)) {
            for (int i = 0; i < 9; i++) {
                ItemStack ing = recipe.ingredients[i];
                if (ing == null || ing.isEmpty()) continue;
                ItemStack slotStack = craftSlots.getItem(i);
                if (slotStack.isEmpty()) continue;
                slotStack.shrink(ing.getCount());
            }
            craftSlots.setChanged();
            return;
        }

        for (int i = 0; i < 9; i++) {
            ItemStack ing = recipe.ingredients[i];
            if (ing == null || ing.isEmpty()) continue;
            if (ing.getCount() <= 1) continue;
            ItemStack slotStack = craftSlots.getItem(i);
            if (slotStack.isEmpty()) continue;
            slotStack.shrink(ing.getCount());
        }

        Map<String, Integer> required = new HashMap<>();
        for (int i = 0; i < 9; i++) {
            ItemStack ing = recipe.ingredients[i];
            if (ing == null || ing.isEmpty()) continue;
            if (ing.getCount() > 1) continue;
            String key = getIngredientKey(ing);
            required.merge(key, ing.getCount(), Integer::sum);
        }

        for (int i = 0; i < 9; i++) {
            ItemStack slotStack = craftSlots.getItem(i);
            if (slotStack.isEmpty()) continue;
            String key = getIngredientKey(slotStack);
            Integer needed = required.get(key);
            if (needed == null || needed <= 0) continue;

            int take = Math.min(needed, slotStack.getCount());
            slotStack.shrink(take);
            needed -= take;

            if (needed <= 0) required.remove(key);
            else required.put(key, needed);

            if (required.isEmpty()) break;
        }

        craftSlots.setChanged();
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
            Component name = stack.get(DataComponents.CUSTOM_NAME);
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
            if (stack.is(item) && !CustomItemHandler.isProtectedItem(stack)) {
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

    /** Total count of all items in the grid (including stacks with count > 1). */
    private static int getTotalItemCount(List<ItemStack> grid) {
        int count = 0;
        for (ItemStack stack : grid) {
            if (!stack.isEmpty()) count += stack.getCount();
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
            case HUSK -> Items.SAND;
            case CAVE_SPIDER -> Items.COBWEB;
            case STRAY -> Items.SNOWBALL;
            case MAGMA_CUBE -> Items.MAGMA_CREAM;
            case BLAZE -> Items.BLAZE_POWDER;
            case HOGLIN -> Items.LEATHER;
            case WITHER_SKELETON -> Items.COAL;
            case RAVAGER -> Items.IRON_INGOT;
            case NANITE -> Items.ECHO_SHARD;
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
     * Checks whether the given 9-slot grid matches a custom recipe when no slots are locked:
     * ingredients with count &gt; 1 must match in that grid slot; count-1 ingredients are matched
     * shapelessly against the grid after subtracting multi-count amounts from their slots.
     */
    private static boolean matchesCustomRecipeShapeless(List<ItemStack> grid, SlayerRecipes.Recipe recipe) {
        if (recipe.ingredients == null) return false;

        // Check slot locks first - if recipe has locked slots, positions must match exactly
        if (recipe.lockedSlots != null && hasLockedSlots(recipe.lockedSlots)) {
            return matchesCustomRecipeExact(grid, recipe);
        }

        // Per-slot requirements when count > 1 in that slot (unlocked recipes still need the full stack in-place)
        for (int i = 0; i < 9; i++) {
            ItemStack recipeItem = recipe.ingredients[i];
            if (recipeItem == null || recipeItem.isEmpty()) continue;
            if (recipeItem.getCount() <= 1) continue;

            ItemStack gridItem = grid.get(i);
            if (gridItem.isEmpty()) return false;
            String gk = getIngredientKey(gridItem);
            String rk = getIngredientKey(recipeItem);
            if (!gk.equals(rk)) return false;
            if (gridItem.getCount() < recipeItem.getCount()) return false;
        }

        List<ItemStack> remaining = new ArrayList<>(9);
        for (int i = 0; i < 9; i++) {
            ItemStack g = grid.get(i);
            remaining.add(g.isEmpty() ? ItemStack.EMPTY : g.copy());
        }
        for (int i = 0; i < 9; i++) {
            ItemStack recipeItem = recipe.ingredients[i];
            if (recipeItem == null || recipeItem.isEmpty()) continue;
            if (recipeItem.getCount() <= 1) continue;
            ItemStack stack = remaining.get(i);
            if (stack.isEmpty()) return false;
            stack.shrink(recipeItem.getCount());
        }

        java.util.Map<String, Integer> gridCounts = new java.util.HashMap<>();
        for (ItemStack stack : remaining) {
            if (stack.isEmpty()) continue;
            String key = getIngredientKey(stack);
            gridCounts.merge(key, stack.getCount(), Integer::sum);
        }

        java.util.Map<String, Integer> recipeCounts = new java.util.HashMap<>();
        for (ItemStack ing : recipe.ingredients) {
            if (ing == null || ing.isEmpty()) continue;
            if (ing.getCount() > 1) continue;
            String key = getIngredientKey(ing);
            recipeCounts.merge(key, ing.getCount(), Integer::sum);
        }

        for (java.util.Map.Entry<String, Integer> req : recipeCounts.entrySet()) {
            Integer gridCount = gridCounts.get(req.getKey());
            if (gridCount == null || gridCount < req.getValue()) {
                return false;
            }
        }

        for (java.util.Map.Entry<String, Integer> gridEntry : gridCounts.entrySet()) {
            if (!recipeCounts.containsKey(gridEntry.getKey())) {
                return false;
            }
        }

        return true;
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

            // Check if items match (both empty or same type with same count)
            if (gridItem.isEmpty() && (recipeItem == null || recipeItem.isEmpty())) {
                continue; // Both empty - good
            }
            if (!gridItem.isEmpty() && recipeItem != null && !recipeItem.isEmpty()) {
                // Both have items — match type; grid must contain *at least* the recipe count per slot
                // (recipes use count 1; players almost always place stacks > 1).
                String gridKey = getIngredientKey(gridItem);
                String recipeKey = getIngredientKey(recipeItem);
                if (!gridKey.equals(recipeKey)) {
                    return false; // Different items in this slot
                }
                if (gridItem.getCount() < recipeItem.getCount()) {
                    return false; // Not enough items in this slot
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
        return ItemRegistryIds.asString(stack.getItem());
    }
}
