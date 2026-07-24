package com.political;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;

/**
 * V6.6 — builds {@link CursedTool} item stacks and registers the craftable grades into
 * {@code /recipe} and {@code /setrecipe} via {@link RecipeConfigManager}.
 */
public final class CursedToolFactory {

    private CursedToolFactory() {}

    public static ItemStack create(CursedTool tool) {
        ItemStack s = new ItemStack(tool.base);
        s.set(DataComponents.CUSTOM_NAME, Component.literal("✦ " + tool.display)
                .withStyle(tool.color, ChatFormatting.BOLD));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal("§8Cursed Tool — " + gradeName(tool)));
        lore.add(Component.literal(""));
        for (String l : tool.statLore) lore.add(Component.literal(l));
        lore.add(Component.literal(""));
        lore.add(Component.literal(tool.signature));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Wielded by a Jujutsu Sorcerer."));
        s.set(DataComponents.LORE, new ItemLore(lore));
        s.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);

        SlayerItems.setCustomItemId(s, tool.id); // LAST
        return s;
    }

    private static String gradeName(CursedTool tool) {
        return switch (tool) {
            case GRADE_4 -> "Grade 4";
            case GRADE_3 -> "Grade 3";
            case GRADE_2 -> "Grade 2";
            case GRADE_1 -> "Grade 1";
            case SPECIAL_GRADE -> "Special Grade";
        };
    }

    // ── Recipes ─────────────────────────────────────────────────

    public static void ensureRecipes() {
        List<SlayerRecipes.Recipe> out = new ArrayList<>();
        addGrade4(out);
        addGrade3(out);
        addGrade2(out);
        addGrade1(out);
        RecipeConfigManager.upsertCustomRecipesSilent(out);
    }

    private static void addGrade4(List<SlayerRecipes.Recipe> out) {
        // iron + bone tools, sealed with a wither skull.
        ItemStack[] g = new ItemStack[9];
        g[0] = new ItemStack(Items.IRON_INGOT);   g[1] = new ItemStack(Items.SOUL_SAND);      g[2] = new ItemStack(Items.IRON_INGOT);
        g[3] = new ItemStack(Items.IRON_INGOT);    g[4] = new ItemStack(Items.WITHER_SKELETON_SKULL); g[5] = new ItemStack(Items.IRON_INGOT);
        g[6] = ItemStack.EMPTY;                    g[7] = new ItemStack(Items.BLAZE_ROD);       g[8] = ItemStack.EMPTY;
        out.add(recipe("V6 Cursed Blade (Grade 4)", CursedTool.GRADE_4, g));
    }

    private static void addGrade3(List<SlayerRecipes.Recipe> out) {
        ItemStack[] g = new ItemStack[9];
        g[0] = new ItemStack(Items.DIAMOND);       g[1] = new ItemStack(Items.SOUL_SAND);      g[2] = new ItemStack(Items.DIAMOND);
        g[3] = new ItemStack(Items.DIAMOND_BLOCK); g[4] = createLower(CursedTool.GRADE_4);      g[5] = new ItemStack(Items.DIAMOND_BLOCK);
        g[6] = new ItemStack(Items.GHAST_TEAR);    g[7] = new ItemStack(Items.BLAZE_ROD);       g[8] = new ItemStack(Items.GHAST_TEAR);
        out.add(recipe("V6 Slaughter Demon (Grade 3)", CursedTool.GRADE_3, g));
    }

    private static void addGrade2(List<SlayerRecipes.Recipe> out) {
        ItemStack[] g = new ItemStack[9];
        g[0] = new ItemStack(Items.NETHERITE_INGOT); g[1] = new ItemStack(Items.WITHER_SKELETON_SKULL); g[2] = new ItemStack(Items.NETHERITE_INGOT);
        g[3] = new ItemStack(Items.DIAMOND_BLOCK);   g[4] = createLower(CursedTool.GRADE_3);              g[5] = new ItemStack(Items.DIAMOND_BLOCK);
        g[6] = new ItemStack(Items.ECHO_SHARD);      g[7] = new ItemStack(Items.NETHERITE_INGOT);         g[8] = new ItemStack(Items.ECHO_SHARD);
        out.add(recipe("V6 Split-Soul Katana (Grade 2)", CursedTool.GRADE_2, g));
    }

    private static void addGrade1(List<SlayerRecipes.Recipe> out) {
        ItemStack[] g = new ItemStack[9];
        g[0] = new ItemStack(Items.NETHERITE_BLOCK); g[1] = new ItemStack(Items.NETHER_STAR);  g[2] = new ItemStack(Items.NETHERITE_BLOCK);
        g[3] = new ItemStack(Items.ECHO_SHARD);      g[4] = createLower(CursedTool.GRADE_2);    g[5] = new ItemStack(Items.ECHO_SHARD);
        g[6] = new ItemStack(Items.ANCIENT_DEBRIS);  g[7] = new ItemStack(Items.NETHER_STAR);   g[8] = new ItemStack(Items.ANCIENT_DEBRIS);
        out.add(recipe("V6 Playful Cloud (Grade 1)", CursedTool.GRADE_1, g));
    }

    /** A lower-grade tool used as a crafting ingredient (matched by custom id). */
    private static ItemStack createLower(CursedTool tool) {
        return create(tool);
    }

    private static SlayerRecipes.Recipe recipe(String name, CursedTool tool, ItemStack[] grid) {
        boolean[] locked = new boolean[9];
        for (int i = 0; i < 9; i++) locked[i] = grid[i] != null && !grid[i].isEmpty();
        return new SlayerRecipes.Recipe(name, create(tool), grid, locked, null, 0);
    }
}
