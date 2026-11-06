package com.political.recipe;

import java.util.Arrays;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Constructs exact-position recipes from the shared vanilla gear silhouettes. */
public final class GearRecipes {
    private GearRecipes() {}

    public static ItemStack[] grid(String piece, ItemStack material, ItemStack core) {
        String[] pattern = switch (piece) {
            case "helmet", "chestplate", "leggings", "boots" -> GearPatterns.armor(piece);
            default -> GearPatterns.tool(piece);
        };
        ItemStack[] grid = new ItemStack[9];
        for (int i = 0; i < 9; i++) grid[i] = switch (pattern[i]) {
            case "block" -> material.copy();
            case "core" -> core.copy();
            case "stick" -> new ItemStack(Items.STICK);
            default -> ItemStack.EMPTY;
        };
        return grid;
    }

    public static boolean[] locked() {
        boolean[] slots = new boolean[9];
        Arrays.fill(slots, true);
        return slots;
    }
}
