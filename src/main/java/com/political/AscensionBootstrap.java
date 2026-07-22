package com.political;

import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** Default Ascension / Sector recipes for /recipe and /setrecipe pipeline. */
public final class AscensionBootstrap {
    private AscensionBootstrap() {}

    public static void ensureRecipes() {
        List<SlayerRecipes.Recipe> out = new ArrayList<>();

        CustomItemManager.CustomItem shard = CustomItemManager.getItem("asc_common_shard");
        CustomItemManager.CustomItem vein = CustomItemManager.getItem("asc_vein_shard");
        CustomItemManager.CustomItem blood = CustomItemManager.getItem("asc_blood_fragment");
        CustomItemManager.CustomItem star = CustomItemManager.getItem("asc_unstable_star");
        CustomItemManager.CustomItem core = CustomItemManager.getItem("asc_master_core");
        CustomItemManager.CustomItem essence = CustomItemManager.getItem("asc_rngesus_essence");

        if (shard != null && vein != null) {
            ItemStack[] g = new ItemStack[9];
            for (int i = 0; i < 9; i++) {
                g[i] = i < 5 ? shard.createStack(1) : ItemStack.EMPTY;
            }
            g[5] = shard.createStack(1);
            boolean[] u = new boolean[9];
            out.add(new SlayerRecipes.Recipe("Vein Shard (5 Sector Shards)", vein.createStack(1), g, u, null, 0));
        }

        if (shard != null && blood != null) {
            ItemStack[] g = new ItemStack[9];
            for (int i = 0; i < 4; i++) {
                g[i] = shard.createStack(1);
            }
            g[4] = shard.createStack(1);
            boolean[] u = new boolean[9];
            out.add(new SlayerRecipes.Recipe("Blood Fragment (5 Sector Shards)", blood.createStack(1), g, u, null, 0));
        }

        if (vein != null && blood != null && star != null) {
            ItemStack[] g = new ItemStack[9];
            g[0] = vein.createStack(1);
            g[1] = blood.createStack(1);
            g[2] = vein.createStack(1);
            g[3] = blood.createStack(1);
            g[4] = vein.createStack(1);
            g[5] = blood.createStack(1);
            g[6] = vein.createStack(1);
            g[7] = blood.createStack(1);
            g[8] = vein.createStack(1);
            boolean[] u = new boolean[9];
            out.add(new SlayerRecipes.Recipe("Unstable Star (mixed frags)", star.createStack(1), g, u, null, 0));
        }

        if (star != null && essence != null && core != null) {
            ItemStack[] g = new ItemStack[9];
            for (int i = 0; i < 9; i++) {
                g[i] = star.createStack(1);
            }
            boolean[] u = new boolean[9];
            out.add(new SlayerRecipes.Recipe("RNGesus Essence (9 Unstable Stars)", essence.createStack(1), g, u, null, 0));

            ItemStack[] g2 = new ItemStack[9];
            for (int i = 0; i < 9; i++) {
                g2[i] = essence.createStack(1);
            }
            boolean[] u2 = new boolean[9];
            out.add(new SlayerRecipes.Recipe("Ascension Master Core (9 Essences)", core.createStack(1), g2, u2, null, 0));
        }

        if (!out.isEmpty()) {
            RecipeConfigManager.upsertCustomRecipesSilent(out);
        }
    }
}
