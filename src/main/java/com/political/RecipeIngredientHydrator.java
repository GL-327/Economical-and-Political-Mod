package com.political;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Fills in block / chunk / stick ingredients when JSON only stored the crafter core (common after bad exports).
 * Keeps the deserialized core stack so tier and NBT stay correct.
 */
public final class RecipeIngredientHydrator {

    private static final Map<String, String> MATERIAL_LOOKUP = new HashMap<>();
    private static final Pattern SLAYER_ARMOR_TIER = Pattern.compile("^(zombie|skeleton|spider|slime)_(helmet|chestplate|leggings|boots)_t([12])$");
    private static final Pattern SLAYER_SWORD_TIER = Pattern.compile("^(zombie|skeleton|spider|slime)_sword_t([12])$");

    static {
        RecipeMaterials.registerAll(MATERIAL_LOOKUP);
        // Prefer multi-block forms for gear that used shortened keys in custom_item_id
        MATERIAL_LOOKUP.putIfAbsent("end_stone_brick", "minecraft:end_stone_bricks");
    }

    private RecipeIngredientHydrator() {}

    public static SlayerRecipes.Recipe hydrate(SlayerRecipes.Recipe recipe) {
        if (recipe == null) return null;
        if (recipe.requiredSlayer != null) return recipe;
        if (!isOnlyCrafterCoreIngredients(recipe)) return recipe;

        String cid = SlayerItems.getCustomItemId(recipe.result);
        if (cid == null || cid.isEmpty()) return recipe;

        ItemStack core = soleNonEmptyIngredient(recipe);
        if (core == null || core.isEmpty()) return recipe;

        Matcher mArmor = SLAYER_ARMOR_TIER.matcher(cid);
        if (mArmor.matches()) {
            SlayerManager.SlayerType type = parseSlayerType(mArmor.group(1));
            if (type == null) return recipe;
            String piece = mArmor.group(2);
            ItemStack chunk = SlayerItems.createChunk(type);
            return buildFromPattern(recipe, RecipeGenerator.getArmorPattern(piece), core, chunk, null);
        }

        Matcher mSword = SLAYER_SWORD_TIER.matcher(cid);
        if (mSword.matches()) {
            SlayerManager.SlayerType type = parseSlayerType(mSword.group(1));
            if (type == null) return recipe;
            String[] pattern = {"", "slayer", "", "", "core", "", "", "stick", ""};
            ItemStack chunk = SlayerItems.createChunk(type);
            return buildFromPattern(recipe, pattern, core, chunk, stackForId("minecraft:stick"));
        }

        ParsedArmorOrTool parsed = parseMaterialAndPiece(cid);
        if (parsed == null) return recipe;

        String blockId = resolveMaterialBlockId(parsed.materialKey);
        if (blockId == null) return recipe;

        ItemStack blockStack = stackForId(blockId);
        if (blockStack.isEmpty()) return recipe;

        String[] pattern;
        if (parsed.kind == Kind.ARMOR) {
            pattern = RecipeGenerator.getArmorPattern(parsed.pieceName);
        } else {
            pattern = RecipeGenerator.getToolPattern(parsed.pieceName);
        }
        return buildFromPattern(recipe, pattern, core, blockStack, stackForId("minecraft:stick"));
    }

    private enum Kind { ARMOR, TOOL }

    private static final class ParsedArmorOrTool {
        final String materialKey;
        final String pieceName;
        final Kind kind;

        ParsedArmorOrTool(String materialKey, String pieceName, Kind kind) {
            this.materialKey = materialKey;
            this.pieceName = pieceName;
            this.kind = kind;
        }
    }

    private static ParsedArmorOrTool parseMaterialAndPiece(String cid) {
        String[] armorSuffixes = {"_chestplate", "_leggings", "_helmet", "_boots"};
        for (String suf : armorSuffixes) {
            if (cid.endsWith(suf)) {
                String mat = cid.substring(0, cid.length() - suf.length());
                if (!mat.isEmpty()) {
                    return new ParsedArmorOrTool(mat, suf.substring(1), Kind.ARMOR);
                }
            }
        }
        String[] toolSuffixes = {"_pickaxe", "_shovel", "_hoe", "_axe", "_sword"};
        for (String suf : toolSuffixes) {
            if (cid.endsWith(suf)) {
                String mat = cid.substring(0, cid.length() - suf.length());
                if (!mat.isEmpty()) {
                    return new ParsedArmorOrTool(mat, suf.substring(1), Kind.TOOL);
                }
            }
        }
        return null;
    }

    private static String resolveMaterialBlockId(String materialKey) {
        String id = MATERIAL_LOOKUP.get(materialKey);
        if (id != null) return id;
        if (materialKey.endsWith("_brick")) {
            id = MATERIAL_LOOKUP.get(materialKey + "s");
            if (id != null) return id;
        }
        return null;
    }

    private static SlayerManager.SlayerType parseSlayerType(String s) {
        try {
            return SlayerManager.SlayerType.valueOf(s.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }

    private static SlayerRecipes.Recipe buildFromPattern(SlayerRecipes.Recipe recipe, String[] pattern,
                                                         ItemStack core, ItemStack blockOrSlayer, ItemStack stick) {
        ItemStack[] grid = new ItemStack[9];
        for (int i = 0; i < 9; i++) {
            String cell = pattern[i];
            if ("core".equals(cell)) {
                grid[i] = core.copy();
            } else if ("block".equals(cell)) {
                grid[i] = blockOrSlayer.copy();
            } else if ("slayer".equals(cell)) {
                grid[i] = blockOrSlayer.copy();
            } else if ("stick".equals(cell)) {
                grid[i] = stick != null ? stick.copy() : new ItemStack(Items.STICK, 1);
            } else {
                grid[i] = ItemStack.EMPTY;
            }
        }
        return new SlayerRecipes.Recipe(
                recipe.name,
                recipe.result,
                grid,
                recipe.lockedSlots,
                recipe.requiredSlayer,
                recipe.requiredLevel
        );
    }

    private static boolean isOnlyCrafterCoreIngredients(SlayerRecipes.Recipe recipe) {
        if (recipe.ingredients == null || recipe.ingredients.length != 9) return false;
        int count = 0;
        for (ItemStack s : recipe.ingredients) {
            if (s != null && !s.isEmpty()) count++;
        }
        if (count != 1) return false;
        ItemStack only = soleNonEmptyIngredient(recipe);
        String id = SlayerItems.getCustomItemId(only);
        return "custom_crafter_core_t1".equals(id)
                || "custom_crafter_core_t2".equals(id)
                || "custom_crafter_core_t3".equals(id);
    }

    private static ItemStack soleNonEmptyIngredient(SlayerRecipes.Recipe recipe) {
        for (ItemStack s : recipe.ingredients) {
            if (s != null && !s.isEmpty()) return s;
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack stackForId(String id) {
        try {
            Item item = Registries.ITEM.get(Identifier.of(id));
            if (item == Items.AIR) return ItemStack.EMPTY;
            return new ItemStack(item, 1);
        } catch (Exception e) {
            return ItemStack.EMPTY;
        }
    }
}
