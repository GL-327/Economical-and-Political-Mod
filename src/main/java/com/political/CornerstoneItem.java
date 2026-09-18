package com.political;
import com.political.mixin.CornerstonePlacementMixin;



import com.political.bounty.SlayerItems;
import com.political.recipe.RecipeConfigManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;

/**
 * "The Cornerstone" — a Bell dressed with custom_item_id {@code settlement_cornerstone}.
 * Placing it founds a settlement (see CornerstonePlacementMixin). Still rings as a bell.
 */
public class CornerstoneItem {

    public static final String ID = "settlement_cornerstone";

    public static ItemStack create() {
        ItemStack s = new ItemStack(Items.BELL);
        s.set(DataComponents.CUSTOM_NAME,
                Component.literal("✦ The Cornerstone")
                        .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

        s.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        // Set the id first (auto-summarizes lore), then overwrite lore so our flavor lines win.
        SlayerItems.setCustomItemId(s, ID);

        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal("§7The civic heart of a new realm."));
        lore.add(Component.literal("§7Place it to found a §eSettlement§7 at that spot."));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§7• Attracts wandering §acitizens"));
        lore.add(Component.literal("§7• Grows through §fbeds§7, §dappeal§7 and §6treasury"));
        lore.add(Component.literal("§7• Cities elect leaders; metropolises rule"));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§6§lSETTLEMENT CORE"));
        s.set(DataComponents.LORE, new ItemLore(lore));
        return s;
    }

    public static boolean isCornerstone(ItemStack stack) {
        return SlayerItems.hasCustomItemId(stack, ID);
    }

    /**
     * Registers the crafting recipe (bell + gold blocks + emerald blocks).
     *
     * <p>Delegates to {@link V4RecipeBootstrap}, which owns every non-magic V4 recipe and
     * persists them via {@code RecipeConfigManager.upsertCustomRecipesSilent} so they show up
     * in {@code /recipe} and {@code /setrecipe}.
     *
     * <p>Historically this method was never called from anywhere, so the Cornerstone — the
     * entry point to the entire settlement system — was obtainable only via the owner-level
     * {@code /settlement cornerstone} command.
     */
    public static void registerRecipe() {
        V4RecipeBootstrap.ensureRecipes();
    }
}
