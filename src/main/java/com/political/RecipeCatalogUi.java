package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Items;

/**
 * Shared pagination and filler slots for /recipe, /customitems, and /setrecipe category grids.
 */
public final class RecipeCatalogUi {

    public static final int ITEM_SLOTS_PER_PAGE = 28;

    private RecipeCatalogUi() {}

    public static boolean sparseMinTwoPagesCustom(CustomItemsGui.Category c) {
        return c == CustomItemsGui.Category.CUSTOM_ARROWS
                || c == CustomItemsGui.Category.FISHING_RODS
                || c == CustomItemsGui.Category.MAGIC_STAVES;
    }

    public static boolean sparseMinTwoPagesEditor(RecipeEditorGui.Category c) {
        return c == RecipeEditorGui.Category.CUSTOM_ARROWS
                || c == RecipeEditorGui.Category.FISHING_RODS
                || c == RecipeEditorGui.Category.MAGIC_STAVES;
    }

    public static int totalPages(int itemCount, boolean sparseMinTwo) {
        int p = Math.max(1, (itemCount + ITEM_SLOTS_PER_PAGE - 1) / ITEM_SLOTS_PER_PAGE);
        return sparseMinTwo ? Math.max(p, 2) : p;
    }

    /**
     * Fills unused slots in rows 1–4, columns 1–7. Last page of sparse categories shows "Coming soon".
     */
    public static void fillEmptyGridSlots(SimpleGui gui, boolean[] usedSlots, boolean lastPageComingSoon) {
        for (int row = 0; row < 4; row++) {
            for (int col = 1; col <= 7; col++) {
                int slot = (row + 1) * 9 + col;
                if (slot < 0 || slot >= usedSlots.length || usedSlots[slot]) {
                    continue;
                }
                GuiElementBuilder b = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE);
                if (lastPageComingSoon) {
                    b.setName(Component.literal("Coming soon").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
                            .addLoreLine(Component.literal("More items later").withStyle(ChatFormatting.DARK_GRAY));
                } else {
                    b.setName(Component.literal(""));
                }
                gui.setSlot(slot, b.build());
            }
        }
    }
}
