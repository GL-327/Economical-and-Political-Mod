package com.political;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Tiered gold armor evolution: surround previous tier (or vanilla gold) with materials.
 * Uses position-locked 3×3 grids so {@link BountyCraftingHandler} matches exactly.
 */
public final class GoldArmorRecipes {

    private static final boolean[] LOCK = {
            true, true, true,
            true, true, true,
            true, true, true
    };

    private GoldArmorRecipes() {}

    private static ItemStack one(ItemStack template) {
        ItemStack c = template.copy();
        c.setCount(1);
        return c;
    }

    private static ItemStack[] helmLike(ItemStack center, net.minecraft.world.item.Item border) {
        return new ItemStack[] {
                new ItemStack(border), new ItemStack(border), new ItemStack(border),
                new ItemStack(border), one(center), new ItemStack(border),
                ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
        };
    }

    private static ItemStack[] chestLike(ItemStack center, net.minecraft.world.item.Item border) {
        return new ItemStack[] {
                new ItemStack(border), new ItemStack(border), new ItemStack(border),
                new ItemStack(border), one(center), new ItemStack(border),
                new ItemStack(border), new ItemStack(border), new ItemStack(border)
        };
    }

    private static ItemStack[] legsLike(ItemStack center, net.minecraft.world.item.Item border) {
        return new ItemStack[] {
                new ItemStack(border), new ItemStack(border), new ItemStack(border),
                new ItemStack(border), one(center), new ItemStack(border),
                new ItemStack(border), ItemStack.EMPTY, new ItemStack(border)
        };
    }

    private static ItemStack[] bootsLike(ItemStack center, net.minecraft.world.item.Item border) {
        return helmLike(center, border);
    }

    private static void add(String name, ItemStack result, ItemStack[] ingredients) {
        SlayerRecipes.RECIPES.add(new SlayerRecipes.Recipe(name, result, ingredients, LOCK, null, 0));
    }

    private static ItemStack resIron(String customId, net.minecraft.world.item.Item piece) {
        ItemStack r = new ItemStack(piece);
        SlayerItems.setCustomItemId(r, customId);
        return r;
    }

    /**
     * Call after {@link HardcodedRecipes} batches so these replace any broken placeholders
     * with the same display names (removeBrokenGoldArmorRecipes runs first).
     */
    public static void register() {
        // Pure (vanilla gold + gold ingots)
        add("Pure Gold Helmet", resIron("pure_gold_helmet", Items.IRON_HELMET),
                helmLike(new ItemStack(Items.GOLDEN_HELMET), Items.GOLD_INGOT));
        add("Pure Gold Chestplate", resIron("pure_gold_chestplate", Items.IRON_CHESTPLATE),
                chestLike(new ItemStack(Items.GOLDEN_CHESTPLATE), Items.GOLD_INGOT));
        add("Pure Gold Leggings", resIron("pure_gold_leggings", Items.IRON_LEGGINGS),
                legsLike(new ItemStack(Items.GOLDEN_LEGGINGS), Items.GOLD_INGOT));
        add("Pure Gold Boots", resIron("pure_gold_boots", Items.IRON_BOOTS),
                bootsLike(new ItemStack(Items.GOLDEN_BOOTS), Items.GOLD_INGOT));

        // Polished (+ gold blocks)
        add("Polished Gold Helmet", resIron("polished_gold_helmet", Items.IRON_HELMET),
                helmLike(SlayerItems.createPureGoldHelmet(), Items.GOLD_BLOCK));
        add("Polished Gold Chestplate", resIron("polished_gold_chestplate", Items.IRON_CHESTPLATE),
                chestLike(SlayerItems.createPureGoldChestplate(), Items.GOLD_BLOCK));
        add("Polished Gold Leggings", resIron("polished_gold_leggings", Items.IRON_LEGGINGS),
                legsLike(SlayerItems.createPureGoldLeggings(), Items.GOLD_BLOCK));
        add("Polished Gold Boots", resIron("polished_gold_boots", Items.IRON_BOOTS),
                bootsLike(SlayerItems.createPureGoldBoots(), Items.GOLD_BLOCK));

        // Shiny (+ diamond)
        add("Shiny Gold Helmet", resIron("shiny_gold_helmet", Items.IRON_HELMET),
                helmLike(SlayerItems.createPolishedGoldHelmet(), Items.DIAMOND));
        add("Shiny Gold Chestplate", resIron("shiny_gold_chestplate", Items.IRON_CHESTPLATE),
                chestLike(SlayerItems.createPolishedGoldChestplate(), Items.DIAMOND));
        add("Shiny Gold Leggings", resIron("shiny_gold_leggings", Items.IRON_LEGGINGS),
                legsLike(SlayerItems.createPolishedGoldLeggings(), Items.DIAMOND));
        add("Shiny Gold Boots", resIron("shiny_gold_boots", Items.IRON_BOOTS),
                bootsLike(SlayerItems.createPolishedGoldBoots(), Items.DIAMOND));

        // Glistening (+ amethyst shards)
        add("Glistening Gold Helmet", resIron("glistening_gold_helmet", Items.IRON_HELMET),
                helmLike(SlayerItems.createShinyGoldHelmet(), Items.AMETHYST_SHARD));
        add("Glistening Gold Chestplate", resIron("glistening_gold_chestplate", Items.IRON_CHESTPLATE),
                chestLike(SlayerItems.createShinyGoldChestplate(), Items.AMETHYST_SHARD));
        add("Glistening Gold Leggings", resIron("glistening_gold_leggings", Items.IRON_LEGGINGS),
                legsLike(SlayerItems.createShinyGoldLeggings(), Items.AMETHYST_SHARD));
        add("Glistening Gold Boots", resIron("glistening_gold_boots", Items.IRON_BOOTS),
                bootsLike(SlayerItems.createShinyGoldBoots(), Items.AMETHYST_SHARD));

        // Gilded (+ netherite ingots)
        add("Gilded Helmet", resIron("gilded_helmet", Items.IRON_HELMET),
                helmLike(SlayerItems.createGlisteningGoldHelmet(), Items.NETHERITE_INGOT));
        add("Gilded Chestplate", resIron("gilded_chestplate", Items.IRON_CHESTPLATE),
                chestLike(SlayerItems.createGlisteningGoldChestplate(), Items.NETHERITE_INGOT));
        add("Gilded Leggings", resIron("gilded_leggings", Items.IRON_LEGGINGS),
                legsLike(SlayerItems.createGlisteningGoldLeggings(), Items.NETHERITE_INGOT));
        add("Gilded Boots", resIron("gilded_boots", Items.IRON_BOOTS),
                bootsLike(SlayerItems.createGlisteningGoldBoots(), Items.NETHERITE_INGOT));

        // Gilded Netherite (+ netherite blocks)
        add("Gilded Netherite Helmet", resIron("gilded_netherite_helmet", Items.IRON_HELMET),
                helmLike(SlayerItems.createGildedHelmet(), Items.NETHERITE_BLOCK));
        add("Gilded Netherite Chestplate", resIron("gilded_netherite_chestplate", Items.IRON_CHESTPLATE),
                chestLike(SlayerItems.createGildedChestplate(), Items.NETHERITE_BLOCK));
        add("Gilded Netherite Leggings", resIron("gilded_netherite_leggings", Items.IRON_LEGGINGS),
                legsLike(SlayerItems.createGildedLeggings(), Items.NETHERITE_BLOCK));
        add("Gilded Netherite Boots", resIron("gilded_netherite_boots", Items.IRON_BOOTS),
                bootsLike(SlayerItems.createGildedBoots(), Items.NETHERITE_BLOCK));
    }

    /** Removes placeholder gold-armor recipes from generated batches (empty / invalid ingredients). */
    public static void removePlaceholdersFromList() {
        SlayerRecipes.RECIPES.removeIf(r -> {
            if (r == null || r.name == null) return false;
            return r.name.equals("Pure Gold Helmet")
                    || r.name.equals("Pure Gold Chestplate")
                    || r.name.equals("Pure Gold Leggings")
                    || r.name.equals("Pure Gold Boots")
                    || r.name.equals("Polished Gold Helmet")
                    || r.name.equals("Polished Gold Chestplate")
                    || r.name.equals("Polished Gold Leggings")
                    || r.name.equals("Polished Gold Boots")
                    || r.name.equals("Shiny Gold Helmet")
                    || r.name.equals("Shiny Gold Chestplate")
                    || r.name.equals("Shiny Gold Leggings")
                    || r.name.equals("Shiny Gold Boots")
                    || r.name.equals("Glistening Gold Helmet")
                    || r.name.equals("Glistening Gold Chestplate")
                    || r.name.equals("Glistening Gold Leggings")
                    || r.name.equals("Glistening Gold Boots")
                    || r.name.equals("Gilded Helmet")
                    || r.name.equals("Gilded Chestplate")
                    || r.name.equals("Gilded Leggings")
                    || r.name.equals("Gilded Boots")
                    || r.name.equals("Gilded Netherite Helmet")
                    || r.name.equals("Gilded Netherite Chestplate")
                    || r.name.equals("Gilded Netherite Leggings")
                    || r.name.equals("Gilded Netherite Boots");
        });
    }
}
