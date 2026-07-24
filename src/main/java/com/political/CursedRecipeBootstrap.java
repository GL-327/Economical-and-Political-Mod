package com.political;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * V6.3 — registers the Cursed Spirits craftables into /recipe and /setrecipe.
 *
 * <p>Recipe names are prefixed "V6 " so they cannot collide with any existing recipe key.
 * Every grid is fully locked (exact-ingredient) because cursed gear should be a genuine sink
 * on materials that only spirits drop — you cannot buy your way past fighting curses.</p>
 */
public final class CursedRecipeBootstrap {

    private CursedRecipeBootstrap() {}

    public static void ensureRecipes() {
        List<SlayerRecipes.Recipe> out = new ArrayList<>();
        addCursedBlade(out);
        addReverseCharm(out);
        addCursedAmulet(out);
        addSixEyesTalisman(out);
        RecipeConfigManager.upsertCustomRecipesSilent(out);
    }

    private static boolean[] allLocked() {
        boolean[] locked = new boolean[9];
        for (int i = 0; i < 9; i++) locked[i] = true;
        return locked;
    }

    // Cursed Blade — 4 Residue + 4 Core + a netherite sword. Mid-tier cursed weapon.
    private static void addCursedBlade(List<SlayerRecipes.Recipe> out) {
        ItemStack[] g = new ItemStack[9];
        g[0] = CursedItems.createResidue();
        g[1] = CursedItems.createCore();
        g[2] = CursedItems.createResidue();
        g[3] = CursedItems.createCore();
        g[4] = new ItemStack(Items.NETHERITE_SWORD);
        g[5] = CursedItems.createCore();
        g[6] = CursedItems.createResidue();
        g[7] = CursedItems.createCore();
        g[8] = CursedItems.createResidue();
        out.add(new SlayerRecipes.Recipe("V6 Cursed Blade",
                CursedItems.createCursedBlade(), g, allLocked(), null, 0));
    }

    // Reverse Cursed Charm — a healing charm around a golden apple.
    private static void addReverseCharm(List<SlayerRecipes.Recipe> out) {
        ItemStack[] g = new ItemStack[9];
        g[0] = CursedItems.createResidue();
        g[1] = new ItemStack(Items.GHAST_TEAR);
        g[2] = CursedItems.createResidue();
        g[3] = CursedItems.createCore();
        g[4] = new ItemStack(Items.ENCHANTED_GOLDEN_APPLE);
        g[5] = CursedItems.createCore();
        g[6] = CursedItems.createResidue();
        g[7] = CursedItems.createCore();
        g[8] = CursedItems.createResidue();
        out.add(new SlayerRecipes.Recipe("V6 Reverse Cursed Charm",
                CursedItems.createReverseCharm(), g, allLocked(), null, 0));
    }

    // Cursed Technique Amulet — cheaper intelligence charm, mostly Residue.
    private static void addCursedAmulet(List<SlayerRecipes.Recipe> out) {
        ItemStack[] g = new ItemStack[9];
        g[0] = CursedItems.createResidue();
        g[1] = new ItemStack(Items.AMETHYST_SHARD);
        g[2] = CursedItems.createResidue();
        g[3] = CursedItems.createResidue();
        g[4] = new ItemStack(Items.AMETHYST_CLUSTER);
        g[5] = CursedItems.createResidue();
        g[6] = CursedItems.createResidue();
        g[7] = CursedItems.createCore();
        g[8] = CursedItems.createResidue();
        out.add(new SlayerRecipes.Recipe("V6 Cursed Technique Amulet",
                CursedItems.createCursedAmulet(), g, allLocked(), null, 0));
    }

    // Six Eyes Talisman — end-game: a Special Grade Relic ringed by 8 Cursed Cores.
    private static void addSixEyesTalisman(List<SlayerRecipes.Recipe> out) {
        ItemStack[] g = new ItemStack[9];
        g[0] = CursedItems.createCore();
        g[1] = CursedItems.createRelic();
        g[2] = CursedItems.createCore();
        g[3] = CursedItems.createCore();
        g[4] = new ItemStack(Items.ENDER_EYE);
        g[5] = CursedItems.createCore();
        g[6] = CursedItems.createCore();
        g[7] = CursedItems.createCore();
        g[8] = CursedItems.createCore();
        out.add(new SlayerRecipes.Recipe("V6 Six Eyes Talisman",
                CursedItems.createSixEyesTalisman(), g, allLocked(), null, 0));
    }
}
