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
 * Builds the refined material items and registers the whole crafting chain.
 *
 * <p>Every step is a real recipe in the same store as everything else, so the chain shows in the
 * recipe browser, can be edited in the recipe editor and is discoverable without documentation. That
 * is the whole reason to do it this way rather than hiding conversions behind a command.</p>
 */
public final class RefinedMaterialItems {

    private RefinedMaterialItems() {}

    public static final String RECIPE_PREFIX = "VX Material";

    // ── Items ─────────────────────────────────────────────────────────────

    public static ItemStack create(RefinedMaterial m, RefinedMaterial.Step step) {
        ItemStack s = new ItemStack(m.baseFor(step));
        s.set(DataComponents.CUSTOM_NAME,
                Component.literal(m.displayFor(step)).withStyle(m.colour, ChatFormatting.BOLD));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal("§8Refined Material, " + step.label));
        lore.add(Component.literal("§8\"" + m.flavor + "\""));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§7Worth §f" + GuiKit.num(step.rawWorth)
                + " §7raw " + m.display.toLowerCase()));
        lore.add(Component.literal("§7Value §6" + GuiKit.num(m.valueFor(step)) + " coins"));

        RefinedMaterial.Step below = step.below();
        if (below != null) {
            lore.add(Component.literal(""));
            lore.add(Component.literal("§8Made from nine " + m.displayFor(below)));
        } else {
            lore.add(Component.literal(""));
            lore.add(Component.literal("§8Smelted from "
                    + m.seedCost + " " + new ItemStack(m.seed).getHoverName().getString()));
            lore.add(Component.literal("§8and " + new ItemStack(m.reagent).getHoverName().getString()));
        }

        if (step.ordinal() >= RefinedMaterial.Step.ENCHANTED.ordinal()) {
            s.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        }
        s.set(DataComponents.LORE, new ItemLore(lore));
        SlayerItems.setCustomItemId(s, m.idFor(step));
        return s;
    }

    /** Reads a material back off a stack, or null when it is not one. */
    public static RefinedMaterial materialOf(ItemStack stack) {
        String id = SlayerItems.getCustomItemId(stack);
        if (id == null || !id.startsWith("mat_")) return null;
        id = SlayerItems.stripSkyblockEnchantSuffix(id);
        for (RefinedMaterial m : RefinedMaterial.values()) {
            for (RefinedMaterial.Step st : RefinedMaterial.Step.values()) {
                if (m.idFor(st).equals(id)) return m;
            }
        }
        return null;
    }

    public static RefinedMaterial.Step stepOf(ItemStack stack) {
        String id = SlayerItems.getCustomItemId(stack);
        if (id == null || !id.startsWith("mat_")) return null;
        id = SlayerItems.stripSkyblockEnchantSuffix(id);
        for (RefinedMaterial m : RefinedMaterial.values()) {
            for (RefinedMaterial.Step st : RefinedMaterial.Step.values()) {
                if (m.idFor(st).equals(id)) return st;
            }
        }
        return null;
    }

    // ── Recipes ───────────────────────────────────────────────────────────

    /**
     * Registers the full chain: a seed recipe for each raw lump, then four nine to one steps.
     *
     * <p>The raw recipe is deliberately a crafting core recipe like everything else in the mod, so
     * a player who has learned how anything else is made already knows how to make these.</p>
     */
    public static void ensureRecipes() {
        List<SlayerRecipes.Recipe> out = new ArrayList<>();

        for (RefinedMaterial m : RefinedMaterial.values()) {
            // Raw, out of vanilla goods plus a core. This is the door into the whole line.
            ItemStack[] raw = new ItemStack[9];
            for (int i = 0; i < 9; i++) raw[i] = ItemStack.EMPTY;
            raw[0] = new ItemStack(m.seed, Math.min(64, m.seedCost));
            raw[2] = new ItemStack(m.seed, Math.min(64, m.seedCost));
            raw[4] = new ItemStack(m.reagent, 4);
            raw[6] = new ItemStack(m.seed, Math.min(64, m.seedCost));
            raw[8] = new ItemStack(m.seed, Math.min(64, m.seedCost));
            raw[7] = LineCatalyst.Tier.T1.core();

            boolean[] rawLock = new boolean[9];
            for (int i = 0; i < 9; i++) rawLock[i] = !raw[i].isEmpty();

            out.add(new SlayerRecipes.Recipe(
                    RECIPE_PREFIX + " " + m.display,
                    create(m, RefinedMaterial.Step.RAW), raw, rawLock, null, 0));

            // Then four steps of nine to one, which is the ratio the whole idea rests on.
            RefinedMaterial.Step[] steps = RefinedMaterial.Step.values();
            for (int i = 1; i < steps.length; i++) {
                RefinedMaterial.Step step = steps[i];
                RefinedMaterial.Step below = steps[i - 1];

                ItemStack[] grid = new ItemStack[9];
                for (int j = 0; j < 9; j++) grid[j] = create(m, below);
                boolean[] lock = new boolean[9];
                for (int j = 0; j < 9; j++) lock[j] = true;

                out.add(new SlayerRecipes.Recipe(
                        RECIPE_PREFIX + " " + m.displayFor(step),
                        create(m, step), grid, lock, null, 0));
            }
        }

        RecipeConfigManager.upsertCustomRecipesSilent(out);
    }

    /** Every material item, for the catalogue screens. */
    public static List<ItemStack> allStacks() {
        List<ItemStack> out = new ArrayList<>();
        for (RefinedMaterial m : RefinedMaterial.values()) {
            for (RefinedMaterial.Step st : RefinedMaterial.Step.values()) {
                out.add(create(m, st));
            }
        }
        return out;
    }
}
