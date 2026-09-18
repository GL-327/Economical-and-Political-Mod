package com.political;


import com.political.recipe.RecipeConfigManager;
import com.political.recipe.SlayerRecipes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * Recipe bootstrap for the Living World pillar.
 *
 * <p>Everything registered here lands in {@code config/political/custom_recipes.json} through
 * {@link RecipeConfigManager#upsertCustomRecipesSilent(List)}, so it shows up in both
 * {@code /recipe} and {@code /setrecipe}.</p>
 *
 * <h2>What is and is not craftable</h2>
 * The three <b>keepsakes</b> (Almanac, Codex, Horn) are craftable from ordinary materials.
 * They carry no stats — they exist only so the Almanac, Bestiary and Hunt Board are reachable
 * without typing a command.
 *
 * <p>The four <b>hunt trophies</b> have no recipe at all. The only way to obtain one is to
 * fell a Great Hunt, which happens at most once every few hours world-wide. The single
 * crafted item with stats, the <b>Grand Trophy Crest</b>, consumes one of each trophy — four
 * separate hunt victories — so it cannot be reached by grinding materials.</p>
 *
 * <p>Recipe names are the map key in {@code custom_recipes.json}; every name here carries the
 * {@code "V5 "} prefix so nothing existing is overwritten.</p>
 */
public final class LivingWorldRecipeBootstrap {

    private LivingWorldRecipeBootstrap() {}

    /** Recipe-name prefix. Keep this on every recipe added by this class. */
    public static final String PREFIX = "V5 Living World ";

    /** Idempotent — safe to call more than once per boot. */
    public static void ensureRecipes() {
        List<SlayerRecipes.Recipe> out = new ArrayList<>();

        addKeepsakes(out);
        addGrandTrophy(out);

        if (out.isEmpty()) return;
        try {
            RecipeConfigManager.upsertCustomRecipesSilent(out);
            System.out.println("[LivingWorldRecipeBootstrap] Registered " + out.size()
                    + " Living World recipes.");
        } catch (Throwable t) {
            System.err.println("[LivingWorldRecipeBootstrap] Failed to register recipes: "
                    + t.getMessage());
        }
    }

    // ── Keepsakes ───────────────────────────────────────────────────────────

    private static void addKeepsakes(List<SlayerRecipes.Recipe> out) {
        // Almanac of Seasons — one token of each season around a book.
        //   [sapling] [sunflower] [wheat]
        //   [ paper ] [  book   ] [paper]
        //   [snowball] [ paper  ] [snowball]
        ItemStack[] almanac = new ItemStack[]{
                new ItemStack(Items.OAK_SAPLING), new ItemStack(Items.SUNFLOWER), new ItemStack(Items.WHEAT),
                new ItemStack(Items.PAPER),       new ItemStack(Items.BOOK),      new ItemStack(Items.PAPER),
                new ItemStack(Items.SNOWBALL),    new ItemStack(Items.PAPER),     new ItemStack(Items.SNOWBALL)
        };
        out.add(shaped("Almanac of Seasons", LivingWorldItems.createSeasonsAlmanac(), almanac));

        // Bestiary Codex — a trophy from each of the common families bound into a book.
        //   [bone]      [rotten flesh] [bone]
        //   [spider eye][    book    ] [gunpowder]
        //   [bone]      [ender pearl ] [bone]
        ItemStack[] codex = new ItemStack[]{
                new ItemStack(Items.BONE),       new ItemStack(Items.ROTTEN_FLESH), new ItemStack(Items.BONE),
                new ItemStack(Items.SPIDER_EYE), new ItemStack(Items.BOOK),         new ItemStack(Items.GUNPOWDER),
                new ItemStack(Items.BONE),       new ItemStack(Items.ENDER_PEARL),  new ItemStack(Items.BONE)
        };
        out.add(shaped("Bestiary Codex", LivingWorldItems.createBestiaryCodex(), codex));

        // Hunter's Horn — a goat horn banded in gold.
        //   [   ] [goat horn] [   ]
        //   [gold] [gold block] [gold]
        //   [   ] [ leather ] [   ]
        ItemStack[] horn = new ItemStack[]{
                ItemStack.EMPTY,                new ItemStack(Items.GOAT_HORN),   ItemStack.EMPTY,
                new ItemStack(Items.GOLD_INGOT), new ItemStack(Items.GOLD_BLOCK), new ItemStack(Items.GOLD_INGOT),
                ItemStack.EMPTY,                new ItemStack(Items.LEATHER),     ItemStack.EMPTY
        };
        out.add(shaped("Hunter's Horn", LivingWorldItems.createHuntersHorn(), horn));
    }

    // ── The one crafted item with stats ─────────────────────────────────────

    private static void addGrandTrophy(List<SlayerRecipes.Recipe> out) {
        // Grand Trophy Crest — one of each hunt trophy, i.e. four won hunts.
        //   [fang]      [gold block]       [crown]
        //   [gold block][netherite helmet] [gold block]
        //   [ashen]     [gold block]       [depths]
        ItemStack[] crest = new ItemStack[]{
                LivingWorldItems.createGnawingFang(1), new ItemStack(Items.GOLD_BLOCK),        LivingWorldItems.createHollowCrown(1),
                new ItemStack(Items.GOLD_BLOCK),       new ItemStack(Items.NETHERITE_HELMET),  new ItemStack(Items.GOLD_BLOCK),
                LivingWorldItems.createAshenHeart(1),  new ItemStack(Items.GOLD_BLOCK),        LivingWorldItems.createSilentDepths(1)
        };
        out.add(shaped("Grand Trophy Crest", LivingWorldItems.createGrandTrophyCrest(), crest));
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    /** Position-sensitive recipe (every slot locked). */
    private static SlayerRecipes.Recipe shaped(String name, ItemStack result, ItemStack[] grid) {
        boolean[] locked = new boolean[9];
        for (int i = 0; i < 9; i++) locked[i] = true;
        return new SlayerRecipes.Recipe(PREFIX + name, result, normalize(grid), locked, null, 0);
    }

    /** Guarantees a 9-length array with no nulls. */
    private static ItemStack[] normalize(ItemStack[] grid) {
        ItemStack[] out = new ItemStack[9];
        for (int i = 0; i < 9; i++) {
            ItemStack s = (grid != null && i < grid.length) ? grid[i] : null;
            out[i] = (s == null) ? ItemStack.EMPTY : s;
        }
        return out;
    }
}
