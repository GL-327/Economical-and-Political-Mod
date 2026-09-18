package com.political;


import com.political.bounty.SlayerItems;
import com.political.recipe.RecipeConfigManager;
import com.political.recipe.SlayerRecipes;
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

        // ── Voidwalker's Crown ───────────────────────────────────────
        // echo  helmet  echo
        // eye   star    eye
        // obsid breath  obsid
        ItemStack[] crown = new ItemStack[9];
        crown[0] = new ItemStack(Items.ECHO_SHARD, 8);
        crown[1] = new ItemStack(Items.NETHERITE_HELMET, 1);
        crown[2] = new ItemStack(Items.ECHO_SHARD, 8);
        crown[3] = new ItemStack(Items.ENDER_EYE, 16);
        crown[4] = new ItemStack(Items.NETHER_STAR, 1);
        crown[5] = new ItemStack(Items.ENDER_EYE, 16);
        crown[6] = new ItemStack(Items.OBSIDIAN, 32);
        crown[7] = new ItemStack(Items.DRAGON_BREATH, 4);
        crown[8] = new ItemStack(Items.OBSIDIAN, 32);
        out.add(new SlayerRecipes.Recipe("Flagship: Voidwalker's Crown",
                SlayerItems.createVoidwalkerCrown(), crown, allLocked(), null, 0));

        // ── Ender Phase Helmet ───────────────────────────────────────
        // eye    helmet   eye
        // chorus echo     chorus
        // (empty row)
        ItemStack[] phase = new ItemStack[9];
        phase[0] = new ItemStack(Items.ENDER_EYE, 8);
        phase[1] = new ItemStack(Items.NETHERITE_HELMET, 1);
        phase[2] = new ItemStack(Items.ENDER_EYE, 8);
        phase[3] = new ItemStack(Items.CHORUS_FRUIT, 32);
        phase[4] = new ItemStack(Items.ECHO_SHARD, 6);
        phase[5] = new ItemStack(Items.CHORUS_FRUIT, 32);
        phase[6] = ItemStack.EMPTY;
        phase[7] = new ItemStack(Items.PHANTOM_MEMBRANE, 16);
        phase[8] = ItemStack.EMPTY;
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
        dagger[7] = new ItemStack(Items.IRON_INGOT, 16);
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
