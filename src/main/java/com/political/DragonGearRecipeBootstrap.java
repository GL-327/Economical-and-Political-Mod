package com.political;


import com.political.bounty.SlayerItems;
import com.political.items.DragonChestplates;
import com.political.recipe.RecipeConfigManager;
import com.political.recipe.SlayerRecipes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Ensures dragon gear recipes exist in JSON (without manually editing huge files).
 */
public final class DragonGearRecipeBootstrap {
    private DragonGearRecipeBootstrap() {}

    public static List<SlayerRecipes.Recipe> buildRecipes() {
        List<SlayerRecipes.Recipe> out = new ArrayList<>();
        ensureFixedChestplateRecipes(out);
        return out;
    }

    public static void ensureRecipes() {
        RecipeConfigManager.upsertCustomRecipesSilent(buildRecipes());
    }

    private static void ensureFixedChestplateRecipes(List<SlayerRecipes.Recipe> out) {
        // Fixed 8 dragon chestplates (Inferno → Eclipse).
        addFixedChestplate(out, "inferno_dragon_chestplate",
                DragonChestplates.createDragonChestplate1());
        addFixedChestplate(out, "storm_dragon_chestplate",
                DragonChestplates.createDragonChestplate2());
        addFixedChestplate(out, "void_dragon_chestplate",
                DragonChestplates.createDragonChestplate3());
        addFixedChestplate(out, "aurora_dragon_chestplate",
                DragonChestplates.createDragonChestplateAurora());
        addFixedChestplate(out, "frost_dragon_chestplate",
                DragonChestplates.createDragonChestplateFrost());
        addFixedChestplate(out, "venom_dragon_chestplate",
                DragonChestplates.createDragonChestplateVenom());
        addFixedChestplate(out, "blood_dragon_chestplate",
                DragonChestplates.createDragonChestplateBlood());
        addFixedChestplate(out, "eclipse_dragon_chestplate",
                DragonChestplates.createDragonChestplateEclipse());
    }

    private static void addFixedChestplate(List<SlayerRecipes.Recipe> out, String fixedCustomId, ItemStack result) {
        // Core requirement matches existing balance: fixed chestplates use T3 core.
        ItemStack coreT3 = SlayerItems.createDragonCrafterCoreT3();
        Block themeBlock = themeBlockForFixedChestplate(fixedCustomId);
        boolean[] locked = lockedAll();

        ItemStack[] ing = new ItemStack[] {
                s(themeBlock), s(Items.NETHER_STAR), s(themeBlock),
                s(Items.DIAMOND), coreT3, s(Items.DIAMOND),
                s(themeBlock), s(Items.NETHERITE_CHESTPLATE), s(themeBlock)
        };

        // Use stable recipe names; output custom id drives replacement.
        out.add(new SlayerRecipes.Recipe(
                titleCaseForRecipeName(result) + " (Hard Core+Star)",
                result,
                ing,
                locked,
                null,
                0
        ));
    }

    private static boolean[] lockedAll() {
        boolean[] locked = new boolean[9];
        for (int idx = 0; idx < 9; idx++) locked[idx] = true;
        return locked;
    }

    private static ItemStack s(net.minecraft.world.level.ItemLike item) {
        return new ItemStack(item);
    }

    private static Block themeBlockForFixedChestplate(String fixedCustomId) {
        // Fixed chestplates: pick block that matches their existing theme recipes.
        return switch (fixedCustomId) {
            case "inferno_dragon_chestplate" -> Blocks.MAGMA_BLOCK;
            case "storm_dragon_chestplate" -> Blocks.PRISMARINE_BRICKS;
            case "void_dragon_chestplate" -> Blocks.BLACKSTONE;
            case "aurora_dragon_chestplate" -> Blocks.AMETHYST_BLOCK;
            case "frost_dragon_chestplate" -> Blocks.PACKED_ICE;
            case "venom_dragon_chestplate" -> Blocks.SLIME_BLOCK;
            case "blood_dragon_chestplate" -> Blocks.RED_NETHER_BRICKS;
            case "eclipse_dragon_chestplate" -> Blocks.OBSIDIAN;
            default -> Blocks.DEEPSLATE;
        };
    }

    private static Block themeBlockForThemePrefix(String themePrefix) {
        String t = themePrefix == null ? "" : themePrefix.toLowerCase(Locale.ROOT);

        if (containsAny(t, "cinder", "ember", "inferno", "sun", "flare", "volcan", "solar")) return Blocks.MAGMA_BLOCK;
        if (containsAny(t, "frost", "ice", "glacier", "rime", "winter", "cryo", "rimev")) return Blocks.PACKED_ICE;
        if (containsAny(t, "tide", "reef", "coral", "prismarine")) return Blocks.PRISMARINE_BRICKS;
        if (containsAny(t, "void", "abyss", "eclipse", "shadow", "obsidian", "night", "noctur")) return Blocks.BLACKSTONE;
        if (containsAny(t, "terra", "verdant", "moss", "swamp", "bloom", "pollen", "thorn", "leaf")) return Blocks.MOSS_BLOCK;
        if (containsAny(t, "venom", "blight", "hollow")) return Blocks.SLIME_BLOCK;
        if (containsAny(t, "star", "comet", "astral", "moon", "dawn")) return Blocks.GLOWSTONE;
        if (containsAny(t, "storm", "thunder", "tempest", "vortex", "seeker")) return Blocks.LIGHTNING_ROD;
        if (containsAny(t, "prism", "arcane", "rune", "aurora")) return Blocks.AMETHYST_BLOCK;

        // Safe fallback.
        return Blocks.DEEPSLATE;
    }

    private static boolean containsAny(String t, String... needles) {
        for (String n : needles) {
            if (t.contains(n)) return true;
        }
        return false;
    }

    private static String titleCaseForRecipeName(ItemStack result) {
        // Kept stable even if theme names include symbols.
        String id = SlayerItems.getCustomItemId(result);
        if (id == null || id.isBlank()) return "Dragon Item";
        return id.replace('_', ' ');
    }
}

