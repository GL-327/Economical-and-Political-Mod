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

        // ─── Special Armor Recipes (all use 9 slots exactly) ───────────────

        // Zombie Berserker Helmet: 5 Zombie Cores + 1 Zombie Head + 3 Rotten Flesh
        if (totalItems == 9
                && countGridCores(grid, SlayerManager.SlayerType.ZOMBIE) == 5
                && countGridVanilla(grid, Items.ZOMBIE_HEAD) == 1
                && countGridVanilla(grid, Items.ROTTEN_FLESH) == 3) {
            int level = SlayerData.getSlayerLevel(serverPlayer.getUuidAsString(), SlayerManager.SlayerType.ZOMBIE);
            if (level < SlayerItems.ZOMBIE_BERSERKER_LEVEL_REQ) return null;
            return SlayerItems.createZombieBerserkerHelmet();
        }

        // Spider Leggings: 5 Spider Cores + 3 String + 1 Spider Eye
        if (totalItems == 9
                && countGridCores(grid, SlayerManager.SlayerType.SPIDER) == 5
                && countGridVanilla(grid, Items.STRING) == 3
                && countGridVanilla(grid, Items.SPIDER_EYE) == 1) {
            int level = SlayerData.getSlayerLevel(serverPlayer.getUuidAsString(), SlayerManager.SlayerType.SPIDER);
            if (level < SlayerItems.SPIDER_LEGGINGS_LEVEL_REQ) return null;
            return SlayerItems.createSpiderLeggings();
        }

        // Skeleton Bow: 5 Skeleton Cores + 1 Bow + 3 Bones
        if (totalItems == 9
                && countGridCores(grid, SlayerManager.SlayerType.SKELETON) == 5
                && countGridVanilla(grid, Items.BOW) == 1
                && countGridVanilla(grid, Items.BONE) == 3) {
            int level = SlayerData.getSlayerLevel(serverPlayer.getUuidAsString(), SlayerManager.SlayerType.SKELETON);
            if (level < SlayerItems.SKELETON_BOW_LEVEL_REQ) return null;
            return SlayerItems.createSkeletonBow();
        }

        // Slime Boots: 5 Slime Cores + 3 Slime Balls + 1 Leather Boots
        if (totalItems == 9
                && countGridCores(grid, SlayerManager.SlayerType.SLIME) == 5
                && countGridVanilla(grid, Items.SLIME_BALL) == 3
                && countGridVanilla(grid, Items.LEATHER_BOOTS) == 1) {
            int level = SlayerData.getSlayerLevel(serverPlayer.getUuidAsString(), SlayerManager.SlayerType.SLIME);
            if (level < SlayerItems.SLIME_BOOTS_LEVEL_REQ) return null;
            return SlayerItems.createSlimeBoots();
        }

        // Warden Chestplate: 5 Warden Cores + 3 Echo Shards + 1 Netherite Chestplate
        if (totalItems == 9
                && countGridCores(grid, SlayerManager.SlayerType.WARDEN) == 5
                && countGridVanilla(grid, Items.ECHO_SHARD) == 3
                && countGridVanilla(grid, Items.NETHERITE_CHESTPLATE) == 1) {
            int level = SlayerData.getSlayerLevel(serverPlayer.getUuidAsString(), SlayerManager.SlayerType.WARDEN);
            if (level < SlayerItems.WARDEN_CHESTPLATE_LEVEL_REQ) return null;
            return SlayerItems.createWardenChestplate();
        }

        // ─── T1 Bounty Swords: 3 Cores + 1 Iron Sword + 2 Sticks = 6 items ──
        if (totalItems == 6 && countGridVanilla(grid, Items.IRON_SWORD) == 1
                && countGridVanilla(grid, Items.STICK) == 2) {
            for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
                if (countGridCores(grid, type) == 3) {
                    int level = SlayerData.getSlayerLevel(serverPlayer.getUuidAsString(), type);
                    if (level < SlayerItems.BASIC_SWORD_LEVEL_REQ) return null;
                    return SlayerItems.createSlayerSword(type);
                }
            }
        }

        // ─── T2 Upgraded Swords: 5 Cores + 1 Diamond Sword + 1 T1 Sword = 7 items ─
        if (totalItems == 7 && countGridVanilla(grid, Items.DIAMOND_SWORD) == 1) {
            for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
                if (countGridCores(grid, type) == 5 && countGridT1Swords(grid, type) == 1) {
                    int level = SlayerData.getSlayerLevel(serverPlayer.getUuidAsString(), type);
                    if (level < SlayerItems.UPGRADED_SWORD_LEVEL_REQ) return null;
                    return SlayerItems.createUpgradedSlayerSword(type);
                }
            }
        }

        // ─── T1 Armor: 3 Cores + 1 Base Leather Armor + 2 Secondary = 6 items ──
        if (totalItems == 6) {
            for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
                if (countGridCores(grid, type) != 3) continue;
                Item secondary = getSecondaryMaterial(type);
                if (countGridVanilla(grid, secondary) != 2) continue;
                for (SlayerItems.ArmorPiece piece : SlayerItems.ArmorPiece.values()) {
                    Item base = getBaseLeatherArmor(piece);
                    if (countGridVanilla(grid, base) == 1) {
                        int level = SlayerData.getSlayerLevel(serverPlayer.getUuidAsString(), type);
                        if (level < SlayerItems.T1_ARMOR_LEVEL_REQ) return null;
                        return SlayerItems.createT1Armor(type, piece);
                    }
                }
            }
        }

        // ─── T2 Armor: 5 Cores + 1 T1 Armor + 2 Secondary + 1 Diamond = 9 items ─
        if (totalItems == 9 && countGridVanilla(grid, Items.DIAMOND) == 1) {
            for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
                if (countGridCores(grid, type) != 5) continue;
                Item secondary = getSecondaryMaterial(type);
                if (countGridVanilla(grid, secondary) != 2) continue;
                for (SlayerItems.ArmorPiece piece : SlayerItems.ArmorPiece.values()) {
                    if (countGridT1Armor(grid, type, piece) == 1) {
                        int level = SlayerData.getSlayerLevel(serverPlayer.getUuidAsString(), type);
                        if (level < SlayerItems.T2_ARMOR_LEVEL_REQ) return null;
                        return SlayerItems.createT2Armor(type, piece);
                    }
                }
            }
        }

        return null;
    }

    // ============================================================
    // GRID COUNTING HELPERS
    // ============================================================

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
     * Excludes custom-named items to avoid counting custom equipment as vanilla materials.
     */
    private static int countGridVanilla(List<ItemStack> grid, Item item) {
        int count = 0;
        for (ItemStack stack : grid) {
            if (stack.isEmpty()) continue;
            if (stack.isOf(item) && !stack.contains(DataComponentTypes.CUSTOM_NAME)) {
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
            case WARDEN   -> Items.SCULK;
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
}
