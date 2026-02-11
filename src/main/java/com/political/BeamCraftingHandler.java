package com.political;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.List;import net.minecraft.server.network.ServerPlayerEntity;

public class BeamCraftingHandler {

    public static void register() {
    }

    public static ItemStack checkBeamRecipe(List<ItemStack> grid) {
        if (grid.size() != 9) return null;

        ItemStack center = grid.get(4);

        // Center must be exactly 1 beam weapon
        if (!CustomItemHandler.isAnyBeamWeapon(center) || center.getCount() != 1) return null;

        int currentTier = CustomItemHandler.getBeamTier(center);

        // ═══════════════════════════════════════════════════════════════
        // TIER 7 RECIPE: Ultra Overclocked (from Mk5)
        // [Warden Core] [Warden Core] [Warden Core]
        // [Warden Core] [   Mk5     ] [Warden Core]
        // [Nether Star] [Nether Star] [Nether Star]
        // ═══════════════════════════════════════════════════════════════
        if (currentTier == 6) { // Mk5 -> Ultra Overclocked
            boolean validUltraOverclocked =
                    CustomItemHandler.isWardenCore(grid.get(0)) && grid.get(0).getCount() == 1 &&
                            CustomItemHandler.isWardenCore(grid.get(1)) && grid.get(1).getCount() == 1 &&
                            CustomItemHandler.isWardenCore(grid.get(2)) && grid.get(2).getCount() == 1 &&
                            CustomItemHandler.isWardenCore(grid.get(3)) && grid.get(3).getCount() == 1 &&
                            CustomItemHandler.isWardenCore(grid.get(5)) && grid.get(5).getCount() == 1 &&
                            grid.get(6).isOf(Items.NETHER_STAR) && grid.get(6).getCount() == 1 &&
                            grid.get(7).isOf(Items.NETHER_STAR) && grid.get(7).getCount() == 1 &&
                            grid.get(8).isOf(Items.NETHER_STAR) && grid.get(8).getCount() == 1;

            if (validUltraOverclocked) {
                return CustomItemHandler.createUltraOverclockedBeam();
            }
        }

        // ═══════════════════════════════════════════════════════════════
        // STANDARD UPGRADE RECIPE (Tiers 0-5)
        // [Nether Star] [Warden Core] [Nether Star]
        // [Nether Star] [   Beam    ] [Nether Star]
        // [Nether Star] [Dragon Brth] [Nether Star]
        // ═══════════════════════════════════════════════════════════════
        ItemStack topMid = grid.get(1);
        ItemStack botMid = grid.get(7);

        // Top middle must be exactly 1 Warden's Core
        if (!CustomItemHandler.isWardenCore(topMid) || topMid.getCount() != 1) return null;

        // Bottom middle must be exactly 1 Dragon's Breath
        if (!botMid.isOf(Items.DRAGON_BREATH) || botMid.getCount() != 1) return null;

        // Each star slot must have exactly 1 Nether Star
        int[] starSlots = {0, 2, 3, 5, 6, 8};
        for (int slot : starSlots) {
            ItemStack stack = grid.get(slot);
            if (!stack.isOf(Items.NETHER_STAR) || stack.getCount() != 1) {
                return null;
            }
        }

        return switch (currentTier) {
            case 0 -> CustomItemHandler.createUltraBeam();      // HPEBM -> Ultra
            case 1 -> CustomItemHandler.createUltraBeamMk(1);   // Ultra -> Mk1
            case 2 -> CustomItemHandler.createUltraBeamMk(2);   // Mk1 -> Mk2
            case 3 -> CustomItemHandler.createUltraBeamMk(3);   // Mk2 -> Mk3
            case 4 -> CustomItemHandler.createUltraBeamMk(4);   // Mk3 -> Mk4
            case 5 -> CustomItemHandler.createUltraBeamMk(5);   // Mk4 -> Mk5
            default -> null;
        };
    }

    public static int getRequiredStars(int currentTier) {
        if (currentTier == 6) return 3; // Ultra Overclocked uses 3 stars
        return 6; // Standard upgrade uses 6
    }

    public static int getRequiredBreath(int currentTier) {
        if (currentTier == 6) return 0; // Ultra Overclocked doesn't use dragon breath
        return 1;
    }

    public static int getRequiredWardenCores(int currentTier) {
        if (currentTier == 6) return 5; // Ultra Overclocked uses 5 cores
        return 1; // Standard upgrade uses 1
    }

    public static boolean tryZombieBerserkerHelmetCraft(ServerPlayerEntity player, ItemStack[] craftingGrid) {
        // Recipe: 5 Zombie Cores + 1 Iron Helmet
        // [Core] [Core] [Core]
        // [Core] [Helmet] [Core]
        // [    ] [    ] [    ]

        int coreCount = 0;
        boolean hasHelmet = false;

        // Check positions
        int[] corePositions = {0, 1, 2, 3, 5}; // Top row + sides of middle
        int helmetPosition = 4; // Center
        int[] emptyPositions = {6, 7, 8}; // Bottom row

        for (int i = 0; i < 9; i++) {
            ItemStack stack = craftingGrid[i];

            if (contains(corePositions, i)) {
                if (SlayerItems.isSlayerCore(stack) &&
                        SlayerItems.getCoreType(stack) == SlayerManager.SlayerType.ZOMBIE) {
                    coreCount++;
                } else if (!stack.isEmpty()) {
                    return false; // Wrong item in core slot
                }
            } else if (i == helmetPosition) {
                if (stack.isOf(Items.IRON_HELMET)) {
                    hasHelmet = true;
                } else if (!stack.isEmpty()) {
                    return false; // Wrong item in helmet slot
                }
            } else if (contains(emptyPositions, i)) {
                if (!stack.isEmpty()) {
                    return false; // Should be empty
                }
            }
        }

        return coreCount == 5 && hasHelmet;
    }

    private static boolean contains(int[] arr, int value) {
        for (int i : arr) {
            if (i == value) return true;
        }
        return false;
    }

    public static ItemStack getZombieBerserkerHelmetResult() {
        return SlayerItems.createZombieBerserkerHelmet();
    }
}