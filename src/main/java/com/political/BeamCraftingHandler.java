package com.political;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.List;

public class BeamCraftingHandler {

    public static void register() {
    }

    public static ItemStack checkBeamRecipe(List<ItemStack> grid) {
        if (grid.size() != 9) return null;

        ItemStack topMid = grid.get(1);
        ItemStack center = grid.get(4);
        ItemStack botMid = grid.get(7);

        // Center must be exactly 1 beam weapon
        if (!CustomItemHandler.isAnyBeamWeapon(center) || center.getCount() != 1) return null;

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

        int currentTier = CustomItemHandler.getBeamTier(center);

        return switch (currentTier) {
            case 1 -> CustomItemHandler.createUltraBeam();      // HPEBM -> Ultra
            case 2 -> CustomItemHandler.createUltraBeamMk(1);   // Ultra -> Mk1
            case 3 -> CustomItemHandler.createUltraBeamMk(2);   // Mk1 -> Mk2
            case 4 -> CustomItemHandler.createUltraBeamMk(3);   // Mk2 -> Mk3
            case 5 -> CustomItemHandler.createUltraBeamMk(4);   // Mk3 -> Mk4
            case 6 -> CustomItemHandler.createUltraBeamMk(5);   // Mk4 -> Mk5
            default -> null;
        };
    }

    public static int getRequiredStars(int currentTier) {
        return 6; // Always 6 (1 per slot)
    }

    public static int getRequiredBreath(int currentTier) {
        return 1; // Always 1
    }
}