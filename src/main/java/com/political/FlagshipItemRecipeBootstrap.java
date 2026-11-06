package com.political;


import com.political.bounty.SlayerItems;
import com.political.recipe.RecipeConfigManager;
import com.political.recipe.SlayerRecipes;
import com.political.recipe.GearRecipes;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * Default crafting recipes for the flagship custom items whose abilities are
 * implemented by {@link VoidwalkerHandler}, {@link EnderPhaseHandler} and
 * {@link VenomHandler}.
 *
 * <p>Without these, the Ender Phase Helmet in particular had no obtainment path at all
 * outside the admin GUI. All recipes are exact-positional (every slot locked) so the
 * shaped layout is meaningful, and every recipe name is prefixed "Flagship:" so it
 * cannot collide with — or overwrite — an existing recipe key.</p>
 */
public final class FlagshipItemRecipeBootstrap {

    private FlagshipItemRecipeBootstrap() {}

    public static void ensureRecipes() {
        List<SlayerRecipes.Recipe> out = new ArrayList<>();

        // Vanilla helmet footprints, with the crafting core in the central hole.
        ItemStack[] crown = GearRecipes.grid("helmet", new ItemStack(Items.OBSIDIAN, 32), SlayerItems.createCustomCrafterCoreT3());
        crown[0] = new ItemStack(Items.ECHO_SHARD, 8);
        crown[1] = new ItemStack(Items.NETHERITE_HELMET);
        crown[2] = new ItemStack(Items.ECHO_SHARD, 8);
        out.add(new SlayerRecipes.Recipe("Flagship: Voidwalker's Crown",
                SlayerItems.createVoidwalkerCrown(), crown, allLocked(), null, 0));

        ItemStack[] phase = GearRecipes.grid("helmet", new ItemStack(Items.CHORUS_FRUIT, 32), SlayerItems.createCustomCrafterCoreT3());
        phase[0] = new ItemStack(Items.ENDER_EYE, 8);
        phase[1] = new ItemStack(Items.NETHERITE_HELMET);
        phase[2] = new ItemStack(Items.ENDER_EYE, 8);
        out.add(new SlayerRecipes.Recipe("Flagship: Ender Phase Helmet",
                SlayerItems.createEnderPhaseHelmet(), phase, allLocked(), null, 0));

        // ── Venomous Dagger ──────────────────────────────────────────
        //        spidereye
        // fermnt sword     fermnt
        //        iron
        ItemStack[] dagger = new ItemStack[9];
        dagger[0] = ItemStack.EMPTY;
        dagger[1] = new ItemStack(Items.SPIDER_EYE, 32);
        dagger[2] = ItemStack.EMPTY;
        dagger[3] = new ItemStack(Items.FERMENTED_SPIDER_EYE, 8);
        dagger[4] = new ItemStack(Items.IRON_SWORD, 1);
        dagger[5] = new ItemStack(Items.FERMENTED_SPIDER_EYE, 8);
        dagger[6] = ItemStack.EMPTY;
        dagger[7] = SlayerItems.createCustomCrafterCoreT2();
        dagger[8] = ItemStack.EMPTY;
        out.add(new SlayerRecipes.Recipe("Flagship: Venomous Dagger",
                SlayerItems.createVenomousDagger(), dagger, allLocked(), null, 0));

        if (!out.isEmpty()) {
            RecipeConfigManager.upsertCustomRecipesSilent(out);
        }
    }

    /** Exact positional match: every slot (including the empty ones) is enforced. */
    private static boolean[] allLocked() {
        boolean[] locked = new boolean[9];
        java.util.Arrays.fill(locked, true);
        return locked;
    }
}
