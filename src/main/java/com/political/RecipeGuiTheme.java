package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Shared visual language for /recipe, /customitems, and /setrecipe:
 * framed panels, accent strips, and a clear content well — not identical layouts, but consistent polish.
 */
public final class RecipeGuiTheme {

    private RecipeGuiTheme() {}

    /** 9×6 main catalog hubs: light top strip, blue bottom, dark sides, gray content well. */
    public static void fillDoubleChestCatalog(SimpleGui gui) {
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(paneForCatalogSlot(i)).setName(Component.literal("")).build());
        }
    }

    private static Item paneForCatalogSlot(int index) {
        int row = index / 9;
        int col = index % 9;
        if (row == 0) {
            return Items.LIGHT_BLUE_STAINED_GLASS_PANE;
        }
        if (row == 5) {
            return Items.BLUE_STAINED_GLASS_PANE;
        }
        if (col == 0 || col == 8) {
            return Items.BLACK_STAINED_GLASS_PANE;
        }
        if (row >= 1 && row <= 4 && col >= 1 && col <= 7) {
            return Items.GRAY_STAINED_GLASS_PANE;
        }
        return ((row + col) % 2 == 0) ? Items.GRAY_STAINED_GLASS_PANE
                                       : Items.GRAY_STAINED_GLASS_PANE;
    }

    /**
     * Category pages (item grids in rows 1–4, cols 1–7).
     * @param goldTrim stronger frame (e.g. Skyblock trinkets)
     */
    public static void fillCategoryItemGrid(SimpleGui gui, boolean goldTrim) {
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(paneForCategorySlot(i, goldTrim)).setName(Component.literal("")).build());
        }
    }

    private static Item paneForCategorySlot(int index, boolean goldTrim) {
        int row = index / 9;
        int col = index % 9;
        if (goldTrim) {
            if (row == 0) {
                return Items.YELLOW_STAINED_GLASS_PANE;
            }
            if (row == 5) {
                return Items.ORANGE_STAINED_GLASS_PANE;
            }
            if (col == 0 || col == 8) {
                return Items.YELLOW_STAINED_GLASS_PANE;
            }
            if (row >= 1 && row <= 4 && col >= 1 && col <= 7) {
                return Items.GRAY_STAINED_GLASS_PANE;
            }
            return ((row + col) % 2 == 0) ? Items.BLACK_STAINED_GLASS_PANE
                                           : Items.GRAY_STAINED_GLASS_PANE;
        }
        if (row == 0) {
            return Items.CYAN_STAINED_GLASS_PANE;
        }
        if (row == 5) {
            return Items.BLUE_STAINED_GLASS_PANE;
        }
        if (col == 0 || col == 8) {
            return Items.BLACK_STAINED_GLASS_PANE;
        }
        if (row >= 1 && row <= 4 && col >= 1 && col <= 7) {
            return Items.GRAY_STAINED_GLASS_PANE;
        }
        return ((row + col) % 2 == 0) ? Items.GRAY_STAINED_GLASS_PANE
                                       : Items.GRAY_STAINED_GLASS_PANE;
    }

    /** 9×4 sub-hubs (sector picks, mob drops, etc.). */
    public static void fillChest4Hub(SimpleGui gui) {
        for (int i = 0; i < 36; i++) {
            int row = i / 9;
            int col = i % 9;
            Item pane;
            if (row == 0) {
                pane = Items.LIGHT_BLUE_STAINED_GLASS_PANE;
            } else if (row == 3) {
                pane = Items.BLUE_STAINED_GLASS_PANE;
            } else if (col == 0 || col == 8) {
                pane = Items.BLACK_STAINED_GLASS_PANE;
            } else if (row >= 1 && row <= 2 && col >= 1 && col <= 7) {
                pane = Items.GRAY_STAINED_GLASS_PANE;
            } else {
                pane = Items.GRAY_STAINED_GLASS_PANE;
            }
            gui.setSlot(i, new GuiElementBuilder(pane).setName(Component.literal("")).build());
        }
    }

    /** 9×3 compact menus (bounty set preview, piglin row, etc.). */
    public static void fillChest3Hub(SimpleGui gui) {
        for (int i = 0; i < 27; i++) {
            int row = i / 9;
            int col = i % 9;
            Item pane;
            if (row == 0) {
                pane = Items.LIGHT_BLUE_STAINED_GLASS_PANE;
            } else if (row == 2) {
                pane = Items.BLUE_STAINED_GLASS_PANE;
            } else if (col == 0 || col == 8) {
                pane = Items.BLACK_STAINED_GLASS_PANE;
            } else if (row == 1 && col >= 1 && col <= 7) {
                pane = Items.GRAY_STAINED_GLASS_PANE;
            } else {
                pane = Items.GRAY_STAINED_GLASS_PANE;
            }
            gui.setSlot(i, new GuiElementBuilder(pane).setName(Component.literal("")).build());
        }
    }

    /** 9×5 read-only recipe preview (legacy recipe view). */
    public static void fillChest5Hub(SimpleGui gui) {
        for (int i = 0; i < 45; i++) {
            int row = i / 9;
            int col = i % 9;
            Item pane;
            if (row == 0) {
                pane = Items.LIGHT_BLUE_STAINED_GLASS_PANE;
            } else if (row == 4) {
                pane = Items.BLUE_STAINED_GLASS_PANE;
            } else if (col == 0 || col == 8) {
                pane = Items.BLACK_STAINED_GLASS_PANE;
            } else if (row >= 1 && row <= 3 && col >= 1 && col <= 7) {
                pane = Items.GRAY_STAINED_GLASS_PANE;
            } else {
                pane = Items.GRAY_STAINED_GLASS_PANE;
            }
            gui.setSlot(i, new GuiElementBuilder(pane).setName(Component.literal("")).build());
        }
    }

    /** 3×3 crafting grid slots in 9×4 recipe UIs (same indices as RecipesGui / CustomItemsGui). */
    private static final int[] RECIPE_GRID_9x4 = {10, 11, 12, 19, 20, 21, 28, 29, 30};

    /**
     * Like {@link #fillChest4Hub} but leaves the 3×3 crafting area unset so it shows as empty inventory slots.
     */
    public static void fillChest4HubRecipeFrame(SimpleGui gui) {
        boolean[] skip = new boolean[36];
        for (int s : RECIPE_GRID_9x4) {
            if (s >= 0 && s < 36) {
                skip[s] = true;
            }
        }
        for (int i = 0; i < 36; i++) {
            if (skip[i]) {
                continue;
            }
            int row = i / 9;
            int col = i % 9;
            Item pane;
            if (row == 0) {
                pane = Items.LIGHT_BLUE_STAINED_GLASS_PANE;
            } else if (row == 3) {
                pane = Items.BLUE_STAINED_GLASS_PANE;
            } else if (col == 0 || col == 8) {
                pane = Items.BLACK_STAINED_GLASS_PANE;
            } else {
                pane = Items.GRAY_STAINED_GLASS_PANE;
            }
            gui.setSlot(i, new GuiElementBuilder(pane).setName(Component.literal("")).build());
        }
    }

    /** 3×3 crafting grid slots centered in a 9×6 double chest. */
    public static final int[] RECIPE_GRID_DOUBLE = {19, 20, 21, 28, 29, 30, 37, 38, 39};

    /** 9×6 double chest recipe view: centered 3×3 grid with spacious layout. */
    public static void fillDoubleChestRecipeFrame(SimpleGui gui) {
        int[] gridSlots = {19, 20, 21, 28, 29, 30, 37, 38, 39};
        boolean[] skip = new boolean[54];
        for (int s : gridSlots) skip[s] = true;
        for (int i = 0; i < 54; i++) {
            if (skip[i]) continue;
            int row = i / 9;
            int col = i % 9;
            Item pane;
            if (row == 0) pane = Items.LIGHT_BLUE_STAINED_GLASS_PANE;
            else if (row == 5) pane = Items.BLUE_STAINED_GLASS_PANE;
            else if (col == 0 || col == 8) pane = Items.BLACK_STAINED_GLASS_PANE;
            else pane = Items.GRAY_STAINED_GLASS_PANE;
            gui.setSlot(i, new GuiElementBuilder(pane).setName(Component.literal("")).build());
        }
    }

    /** Empty crafting cell: no icon (vanilla-style clear slot). */
    public static void setRecipeEmptyGridSlot(SimpleGui gui, int slotIndex) {
        gui.setSlot(slotIndex, new GuiElementBuilder(ItemStack.EMPTY).build());
    }

    private static final int[] RECIPE_GRID_9x5 = {10, 11, 12, 19, 20, 21, 28, 29, 30};

    /** 9×5 recipe read-only view: frame only, 3×3 grid left clear. */
    public static void fillChest5HubRecipeFrame(SimpleGui gui) {
        boolean[] skip = new boolean[45];
        for (int s : RECIPE_GRID_9x5) {
            if (s >= 0 && s < 45) {
                skip[s] = true;
            }
        }
        for (int i = 0; i < 45; i++) {
            if (skip[i]) {
                continue;
            }
            int row = i / 9;
            int col = i % 9;
            Item pane;
            if (row == 0) {
                pane = Items.LIGHT_BLUE_STAINED_GLASS_PANE;
            } else if (row == 4) {
                pane = Items.BLUE_STAINED_GLASS_PANE;
            } else if (col == 0 || col == 8) {
                pane = Items.BLACK_STAINED_GLASS_PANE;
            } else {
                pane = Items.GRAY_STAINED_GLASS_PANE;
            }
            gui.setSlot(i, new GuiElementBuilder(pane).setName(Component.literal("")).build());
        }
    }
}
