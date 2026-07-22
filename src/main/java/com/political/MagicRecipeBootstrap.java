package com.political;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * Registers crafting recipes for every piece of Magic V4 gear into {@code custom_recipes.json}
 * so it all shows up in {@code /recipe} and {@code /setrecipe}.
 *
 * <p>Covers: per-school robes (4 pieces × 12 schools), per-school catalysts, per-school grimoires
 * (one per {@link MagicGearFactory#GRIMOIRE_TIERS} tier), the Archmage set, the Awakening Catalyst
 * and the Technique Relics for the <b>common</b> techniques.</p>
 *
 * <p><b>Deliberately NOT craftable:</b> Technique Relics for the 11 EXCLUSIVE techniques. Those are
 * server-unique; a craftable relic would let anyone mint the trophy of a technique they cannot hold.
 * They are obtained by {@code /magicgear relic <TECHNIQUE>} style admin grants and by holding the
 * technique itself — see {@link MagicGearFactory#createTechniqueRelic}.</p>
 *
 * <p>Recipe names are the unique upsert key, so every name here is prefixed {@code "Magic: "} to
 * avoid colliding with any other bootstrap in the mod.</p>
 */
public final class MagicRecipeBootstrap {

    private MagicRecipeBootstrap() {}

    /** Call from SERVER_STARTED, after RecipeConfigManager has loaded. */
    public static void ensureRecipes() {
        List<SlayerRecipes.Recipe> out = new ArrayList<>();

        for (MagicSchool school : MagicSchool.values()) {
            for (EquipmentSlot slot : MagicGearFactory.ARMOR_SLOTS) {
                out.add(robeRecipe(school, slot));
            }
            out.add(catalystRecipe(school));
            for (int tier : MagicGearFactory.GRIMOIRE_TIERS) {
                out.add(grimoireRecipe(school, tier));
            }
        }

        for (EquipmentSlot slot : MagicGearFactory.ARMOR_SLOTS) {
            out.add(archmageRecipe(slot));
        }

        out.add(awakeningCatalystRecipe());

        for (Technique t : Technique.commons()) {
            out.add(relicRecipe(t));
        }

        if (!out.isEmpty()) {
            RecipeConfigManager.upsertCustomRecipesSilent(out);
        }
    }

    // ── recipe builders ────────────────────────────────────────────────────

    /** 8 school reagents around the matching vanilla leather piece — the mod's standard armour shape. */
    private static SlayerRecipes.Recipe robeRecipe(MagicSchool school, EquipmentSlot slot) {
        ItemStack[] grid = ring(new ItemStack(schoolReagent(school), reagentCount(slot)),
                new ItemStack(leatherPiece(slot)));
        return new SlayerRecipes.Recipe(
                "Magic: " + school.displayName + " " + pieceLabel(slot),
                MagicGearFactory.createRobePiece(school, slot),
                grid, allLocked(), null, 0);
    }

    private static SlayerRecipes.Recipe catalystRecipe(MagicSchool school) {
        ItemStack[] grid = ring(new ItemStack(schoolReagent(school), 8),
                new ItemStack(Items.AMETHYST_BLOCK, 4));
        return new SlayerRecipes.Recipe(
                "Magic: " + school.displayName + " Catalyst",
                MagicGearFactory.createCatalyst(school),
                grid, allLocked(), null, 0);
    }

    private static SlayerRecipes.Recipe grimoireRecipe(MagicSchool school, int shards) {
        int per = Math.max(1, shards / 25);
        ItemStack[] grid = new ItemStack[9];
        for (int i = 0; i < 9; i++) grid[i] = ItemStack.EMPTY;
        grid[0] = new ItemStack(schoolReagent(school), per);
        grid[2] = new ItemStack(schoolReagent(school), per);
        grid[6] = new ItemStack(schoolReagent(school), per);
        grid[8] = new ItemStack(schoolReagent(school), per);
        grid[1] = new ItemStack(Items.LAPIS_LAZULI, per);
        grid[3] = new ItemStack(Items.LAPIS_LAZULI, per);
        grid[5] = new ItemStack(Items.LAPIS_LAZULI, per);
        grid[7] = new ItemStack(Items.LAPIS_LAZULI, per);
        grid[4] = new ItemStack(Items.BOOK);
        return new SlayerRecipes.Recipe(
                "Magic: Grimoire of " + school.displayName + " (" + shards + ")",
                MagicGearFactory.createGrimoire(school, shards),
                grid, allLocked(), null, 0);
    }

    private static SlayerRecipes.Recipe archmageRecipe(EquipmentSlot slot) {
        ItemStack[] grid = ring(new ItemStack(Items.NETHERITE_INGOT, 1),
                new ItemStack(leatherPiece(slot)));
        // Corners upgraded to nether stars: this is the endgame set.
        grid[0] = new ItemStack(Items.NETHER_STAR);
        grid[2] = new ItemStack(Items.NETHER_STAR);
        grid[6] = new ItemStack(Items.NETHER_STAR);
        grid[8] = new ItemStack(Items.NETHER_STAR);
        return new SlayerRecipes.Recipe(
                "Magic: Archmage " + pieceLabel(slot),
                MagicGearFactory.createArchmagePiece(slot),
                grid, allLocked(), null, 0);
    }

    private static SlayerRecipes.Recipe awakeningCatalystRecipe() {
        ItemStack[] grid = new ItemStack[9];
        for (int i = 0; i < 9; i++) grid[i] = ItemStack.EMPTY;
        grid[0] = new ItemStack(Items.DRAGON_BREATH, 8);
        grid[2] = new ItemStack(Items.DRAGON_BREATH, 8);
        grid[6] = new ItemStack(Items.DRAGON_BREATH, 8);
        grid[8] = new ItemStack(Items.DRAGON_BREATH, 8);
        grid[1] = new ItemStack(Items.AMETHYST_BLOCK, 16);
        grid[3] = new ItemStack(Items.AMETHYST_BLOCK, 16);
        grid[5] = new ItemStack(Items.AMETHYST_BLOCK, 16);
        grid[7] = new ItemStack(Items.AMETHYST_BLOCK, 16);
        grid[4] = new ItemStack(Items.NETHER_STAR);
        return new SlayerRecipes.Recipe(
                "Magic: Awakening Catalyst",
                MagicGearFactory.createAwakeningCatalyst(),
                grid, allLocked(), null, 0);
    }

    private static SlayerRecipes.Recipe relicRecipe(Technique t) {
        ItemStack[] grid = new ItemStack[9];
        for (int i = 0; i < 9; i++) grid[i] = ItemStack.EMPTY;
        Item themed = relicReagent(t);
        grid[0] = new ItemStack(Items.AMETHYST_SHARD, 16);
        grid[2] = new ItemStack(Items.AMETHYST_SHARD, 16);
        grid[6] = new ItemStack(Items.AMETHYST_SHARD, 16);
        grid[8] = new ItemStack(Items.AMETHYST_SHARD, 16);
        grid[1] = new ItemStack(themed, 8);
        grid[3] = new ItemStack(themed, 8);
        grid[5] = new ItemStack(themed, 8);
        grid[7] = new ItemStack(themed, 8);
        grid[4] = new ItemStack(Items.ECHO_SHARD);
        return new SlayerRecipes.Recipe(
                "Magic: Relic of " + t.displayName,
                MagicGearFactory.createTechniqueRelic(t),
                grid, allLocked(), null, 0);
    }

    // ── helpers ────────────────────────────────────────────────────────────

    private static ItemStack[] ring(ItemStack outer, ItemStack center) {
        ItemStack[] grid = new ItemStack[9];
        for (int i = 0; i < 9; i++) {
            grid[i] = i == 4 ? center.copy() : outer.copy();
        }
        return grid;
    }

    private static boolean[] allLocked() {
        boolean[] locked = new boolean[9];
        for (int i = 0; i < 9; i++) locked[i] = true;
        return locked;
    }

    private static int reagentCount(EquipmentSlot slot) {
        return switch (slot) {
            case CHEST -> 8;
            case LEGS -> 7;
            case HEAD -> 5;
            default -> 4;
        };
    }

    private static String pieceLabel(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> "Hood";
            case CHEST -> "Robe";
            case LEGS -> "Leggings";
            default -> "Slippers";
        };
    }

    private static Item leatherPiece(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> Items.LEATHER_HELMET;
            case CHEST -> Items.LEATHER_CHESTPLATE;
            case LEGS -> Items.LEATHER_LEGGINGS;
            default -> Items.LEATHER_BOOTS;
        };
    }

    /** Thematic vanilla reagent for each school. */
    private static Item schoolReagent(MagicSchool school) {
        return switch (school) {
            case PYROMANCY -> Items.BLAZE_POWDER;
            case FROSTWEAVING -> Items.BLUE_ICE;
            case STORMCALLING -> Items.LIGHTNING_ROD;
            case VOIDWALKING -> Items.ENDER_EYE;
            case BLOODBINDING -> Items.REDSTONE_BLOCK;
            case VERDANCY -> Items.MOSS_BLOCK;
            case RADIANCE -> Items.GLOWSTONE;
            case GRAVITAS -> Items.IRON_BLOCK;
            case CHRONOMANCY -> Items.CLOCK;
            case RESONANCE -> Items.NOTE_BLOCK;
            case SPOREWEAVING -> Items.BROWN_MUSHROOM_BLOCK;
            case ASTRALISM -> Items.END_ROD;
        };
    }

    /** Thematic vanilla reagent for each craftable (common) technique relic. */
    private static Item relicReagent(Technique t) {
        return switch (t) {
            case EMBERHEART -> Items.MAGMA_BLOCK;
            case GLACIER_SOUL -> Items.PACKED_ICE;
            case STORM_VEIN -> Items.COPPER_BLOCK;
            case UMBRAL_STEP -> Items.OBSIDIAN;
            case CRIMSON_PACT -> Items.REDSTONE_BLOCK;
            case WILDGROWTH -> Items.MOSS_BLOCK;
            case DAWNFORGED -> Items.GOLD_BLOCK;
            case IRONMARROW -> Items.IRON_BLOCK;
            case TIDEBOUND -> Items.PRISMARINE_BRICKS;
            case WHISPERWIND -> Items.PHANTOM_MEMBRANE;
            case RESONANT_ECHO -> Items.NOTE_BLOCK;
            case SPOREBLOOM -> Items.BROWN_MUSHROOM_BLOCK;
            case LODESTONE_HEART -> Items.LODESTONE;
            default -> Items.AMETHYST_BLOCK;
        };
    }
}
