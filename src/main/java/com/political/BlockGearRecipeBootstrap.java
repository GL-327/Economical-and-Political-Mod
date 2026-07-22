package com.political;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Auto-upserts crafting recipes for all block/gem armor, weapons, and tools into custom_recipes.json.
 * Uses the same shapes as {@link HardcodedRecipes}: armor uses 8 blocks + crafter core; swords/tools include crafter core.
 */
public final class BlockGearRecipeBootstrap {

    private BlockGearRecipeBootstrap() {}

    private static final Map<String, String> PREFIX_ALIASES = new HashMap<>();
    static {
        PREFIX_ALIASES.put("endstone", "end_stone");
        PREFIX_ALIASES.put("lapis", "lapis_block");
        PREFIX_ALIASES.put("lapis_lazuli", "lapis_lazuli");
        PREFIX_ALIASES.put("quartz", "quartz_block");
        PREFIX_ALIASES.put("emerald", "emerald_block");
        PREFIX_ALIASES.put("diamond_block", "diamond_block");
        PREFIX_ALIASES.put("gold_block", "gold_block");
        PREFIX_ALIASES.put("iron_block", "iron_block");
        PREFIX_ALIASES.put("copper_block", "copper_block");
        PREFIX_ALIASES.put("netherite_block", "netherite_block");
        PREFIX_ALIASES.put("snow", "snow_block");
        PREFIX_ALIASES.put("soul_sand", "soul_sand");
        PREFIX_ALIASES.put("soulsand", "soul_sand");
        PREFIX_ALIASES.put("soul_soil", "soul_soil");
        PREFIX_ALIASES.put("mossy", "mossy_cobblestone");
        PREFIX_ALIASES.put("moss_block", "moss_block");
        PREFIX_ALIASES.put("hay", "hay_block");
        PREFIX_ALIASES.put("honeycomb", "honeycomb_block");
    }

    public static void ensureRecipes() {
        List<SlayerRecipes.Recipe> out = new ArrayList<>();
        collectFromClass(BlockArmor.class, out);
        collectFromClass(BlockWeapons.class, out);
        collectFromClass(BlockTools.class, out);
        collectFromClass(GemArmor.class, out);
        collectFromClass(GemGear.class, out);
        if (!out.isEmpty()) {
            RecipeConfigManager.upsertCustomRecipesSilent(out);
        }
    }

    private static void collectFromClass(Class<?> clazz, List<SlayerRecipes.Recipe> out) {
        for (Method m : clazz.getDeclaredMethods()) {
            if (!Modifier.isPublic(m.getModifiers()) || !Modifier.isStatic(m.getModifiers())) continue;
            if (!m.getName().startsWith("create")) continue;
            if (m.getParameterCount() != 0) continue;
            if (!ItemStack.class.equals(m.getReturnType())) continue;
            try {
                ItemStack result = (ItemStack) m.invoke(null);
                if (result == null || result.isEmpty()) continue;
                String id = SlayerItems.getCustomItemId(result);
                if (id == null || id.isBlank()) continue;
                SlayerRecipes.Recipe recipe = recipeFor(id, result, m.getName());
                if (recipe != null) {
                    out.add(recipe);
                }
            } catch (Exception ignored) {
            }
        }
    }

    private static SlayerRecipes.Recipe recipeFor(String customId, ItemStack result, String factoryName) {
        String suffix = pieceSuffix(customId);
        if (suffix == null) return null;
        String prefix = customId.substring(0, customId.length() - suffix.length() - 1);
        ItemStack mat = resolveMaterialStack(prefix);
        if (mat == null || mat.isEmpty()) {
            mat = new ItemStack(Items.COBBLESTONE);
        }
        ItemStack core = coreFor(result);
        String title = titleCase(factoryName.replace("create", "").replace("_", " "));

        if (suffix.equals("helmet") || suffix.equals("chestplate") || suffix.equals("leggings") || suffix.equals("boots")) {
            return armorRecipe(title, result, mat, core, suffix);
        }
        if (suffix.equals("sword")) {
            return swordRecipe(title, result, mat, core);
        }
        if (suffix.equals("pickaxe")) {
            return pickaxeRecipe(title, result, mat, core);
        }
        if (suffix.equals("axe")) {
            return axeRecipe(title, result, mat, core);
        }
        if (suffix.equals("shovel")) {
            return shovelRecipe(title, result, mat, core);
        }
        if (suffix.equals("hoe")) {
            return hoeRecipe(title, result, mat, core);
        }
        return null;
    }

    private static String pieceSuffix(String id) {
        String[] suf = {"helmet", "chestplate", "leggings", "boots", "sword", "pickaxe", "axe", "shovel", "hoe"};
        for (String s : suf) {
            if (id.endsWith("_" + s)) return s;
        }
        return null;
    }

    private static ItemStack resolveMaterialStack(String prefix) {
        String key = PREFIX_ALIASES.getOrDefault(prefix, prefix);
        Optional<Item> opt = BuiltInRegistries.ITEM.getOptional(Identifier.fromNamespaceAndPath("minecraft", key));
        if (opt.isPresent()) {
            return new ItemStack(opt.get());
        }
        opt = BuiltInRegistries.ITEM.getOptional(Identifier.fromNamespaceAndPath("minecraft", prefix));
        return opt.map(ItemStack::new).orElse(ItemStack.EMPTY);
    }

    private static ItemStack coreFor(ItemStack result) {
        if (result.is(Items.NETHERITE_SWORD) || result.is(Items.NETHERITE_PICKAXE) || result.is(Items.NETHERITE_AXE)
                || result.is(Items.NETHERITE_SHOVEL) || result.is(Items.NETHERITE_HOE)) {
            return SlayerItems.createCustomCrafterCoreT3();
        }
        if (result.is(Items.DIAMOND_SWORD) || result.is(Items.DIAMOND_PICKAXE)) {
            return SlayerItems.createCustomCrafterCoreT2();
        }
        return SlayerItems.createCustomCrafterCoreT1();
    }

    private static SlayerRecipes.Recipe armorRecipe(String name, ItemStack result, ItemStack mat, ItemStack core, String piece) {
        String[] pattern = RecipeGenerator.getArmorPattern(piece);
        ItemStack[] ing = new ItemStack[9];
        boolean[] locked = new boolean[9];
        for (int i = 0; i < 9; i++) {
            switch (pattern[i]) {
                case "block" -> ing[i] = mat.copy();
                case "core" -> ing[i] = core.copy();
                default -> ing[i] = ItemStack.EMPTY;
            }
            locked[i] = !"core".equals(pattern[i]);
        }
        return new SlayerRecipes.Recipe(name, result, ing, locked, null, 0);
    }

    private static SlayerRecipes.Recipe swordRecipe(String name, ItemStack result, ItemStack mat, ItemStack core) {
        ItemStack[] ing = new ItemStack[] {
                mat.copy(), mat.copy(), core.copy(),
                new ItemStack(Items.STICK), new ItemStack(Items.STICK),
                ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
        };
        boolean[] locked = {true, true, true, true, true, false, false, false, false};
        return new SlayerRecipes.Recipe(name, result, ing, locked, null, 0);
    }

    private static SlayerRecipes.Recipe pickaxeRecipe(String name, ItemStack result, ItemStack mat, ItemStack core) {
        ItemStack[] ing = new ItemStack[] {
                mat.copy(), mat.copy(), mat.copy(),
                ItemStack.EMPTY, core.copy(), ItemStack.EMPTY,
                ItemStack.EMPTY, new ItemStack(Items.STICK), ItemStack.EMPTY
        };
        boolean[] locked = {true, true, true, false, false, false, false, true, false};
        return new SlayerRecipes.Recipe(name, result, ing, locked, null, 0);
    }

    private static SlayerRecipes.Recipe axeRecipe(String name, ItemStack result, ItemStack mat, ItemStack core) {
        ItemStack[] ing = new ItemStack[] {
                mat.copy(), mat.copy(), ItemStack.EMPTY,
                mat.copy(), core.copy(), ItemStack.EMPTY,
                ItemStack.EMPTY, new ItemStack(Items.STICK), ItemStack.EMPTY
        };
        boolean[] locked = {true, true, false, true, false, false, false, true, false};
        return new SlayerRecipes.Recipe(name, result, ing, locked, null, 0);
    }

    private static SlayerRecipes.Recipe shovelRecipe(String name, ItemStack result, ItemStack mat, ItemStack core) {
        ItemStack[] ing = new ItemStack[] {
                ItemStack.EMPTY, mat.copy(), ItemStack.EMPTY,
                ItemStack.EMPTY, core.copy(), ItemStack.EMPTY,
                new ItemStack(Items.STICK), ItemStack.EMPTY, ItemStack.EMPTY
        };
        boolean[] locked = {false, true, false, false, false, false, true, false, false};
        return new SlayerRecipes.Recipe(name, result, ing, locked, null, 0);
    }

    private static SlayerRecipes.Recipe hoeRecipe(String name, ItemStack result, ItemStack mat, ItemStack core) {
        ItemStack[] ing = new ItemStack[] {
                mat.copy(), mat.copy(), ItemStack.EMPTY,
                ItemStack.EMPTY, core.copy(), ItemStack.EMPTY,
                new ItemStack(Items.STICK), ItemStack.EMPTY, ItemStack.EMPTY
        };
        boolean[] locked = {true, true, false, false, false, false, true, false, false};
        return new SlayerRecipes.Recipe(name, result, ing, locked, null, 0);
    }
    private static String titleCase(String s) {
        String[] p = s.split(" ");
        StringBuilder b = new StringBuilder();
        for (String w : p) {
            if (w.isEmpty()) continue;
            if (b.length() > 0) b.append(' ');
            b.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1).toLowerCase());
        }
        return b.toString();
    }
}
