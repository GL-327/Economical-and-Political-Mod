package com.political;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.HashSet;
import java.util.Set;

/**
 * Ensures dragon gear recipes exist in JSON (without manually editing huge files).
 */
public final class DragonGearRecipeBootstrap {
    private DragonGearRecipeBootstrap() {}

    public static void ensureRecipes() {
        Set<String> existingOutputIds = new HashSet<>();
        for (SlayerRecipes.Recipe r : RecipeConfigManager.getRecipes()) {
            String id = SlayerItems.getCustomItemId(r.result);
            if (id != null && !id.isBlank()) existingOutputIds.add(id);
        }

        ensureChestplateRecipes(existingOutputIds);
        ensureSwordRecipes(existingOutputIds);
    }

    private static void ensureChestplateRecipes(Set<String> existingOutputIds) {
        for (int i = 1; i <= 60; i++) {
            ItemStack result = MoreDragonChestplates.createNewDragonChestplate(i);
            String id = SlayerItems.getCustomItemId(result);
            if (existingOutputIds.contains(id)) continue;
            ItemStack core = (i <= 20) ? SlayerItems.createDragonCrafterCoreT1()
                    : (i <= 40) ? SlayerItems.createDragonCrafterCoreT2()
                    : SlayerItems.createDragonCrafterCoreT3();
            ItemStack[] ing = new ItemStack[] {
                    s(Items.NETHERITE_INGOT), s(Items.DRAGON_EGG), s(Items.NETHERITE_INGOT),
                    s(Items.DIAMOND), core.copy(), s(Items.DIAMOND),
                    s(Items.NETHERITE_INGOT), s(Items.NETHERITE_CHESTPLATE), s(Items.NETHERITE_INGOT)
            };
            RecipeConfigManager.addCustomRecipe(new SlayerRecipes.Recipe(
                    "dragon_chestplate_extra_" + String.format("%02d", i), result, ing, null, 0
            ));
        }

        for (int i = 1; i <= 3; i++) {
            ItemStack result = MoreDragonChestplates.createOmegaDragonChestplate(i);
            String id = SlayerItems.getCustomItemId(result);
            if (existingOutputIds.contains(id)) continue;
            ItemStack[] ing = new ItemStack[] {
                    s(Items.NETHER_STAR), SlayerItems.createDragonCrafterCoreOmega(), s(Items.NETHER_STAR),
                    s(Items.DRAGON_EGG), s(Items.NETHERITE_CHESTPLATE), s(Items.DRAGON_EGG),
                    s(Items.NETHERITE_INGOT), SlayerItems.createDragonCrafterCoreT3(), s(Items.NETHERITE_INGOT)
            };
            RecipeConfigManager.addCustomRecipe(new SlayerRecipes.Recipe(
                    "dragon_chestplate_omega_" + i, result, ing, null, 0
            ));
        }
    }

    private static void ensureSwordRecipes(Set<String> existingOutputIds) {
        for (int i = 1; i <= 15; i++) {
            ItemStack result = DragonSwords.createDragonSword(i);
            String id = SlayerItems.getCustomItemId(result);
            if (existingOutputIds.contains(id)) continue;
            ItemStack core = (i <= 5) ? SlayerItems.createDragonCrafterCoreT1()
                    : (i <= 10) ? SlayerItems.createDragonCrafterCoreT2()
                    : SlayerItems.createDragonCrafterCoreT3();

            ItemStack[] ing = new ItemStack[] {
                    s(Items.AIR), s(Items.DRAGON_EGG), s(Items.AIR),
                    s(Items.AIR), core.copy(), s(Items.AIR),
                    s(Items.AIR), s(Items.NETHERITE_SWORD), s(Items.BLAZE_ROD)
            };
            RecipeConfigManager.addCustomRecipe(new SlayerRecipes.Recipe(
                    "dragon_sword_" + String.format("%02d", i), result, ing, null, 0
            ));
        }
    }

    private static ItemStack s(net.minecraft.item.Item item) {
        return new ItemStack(item);
    }
}

