package com.political.core.gui;

import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

/**
 * Puts a Back button on a hand-built menu that has none (M7). The menu keeps its own layout; this finds the first
 * empty or filler slot on the bottom row, left to right, and puts {@link Elements#back} there, so it never covers a real button.
 */
public final class BackSlot {

    private BackSlot() {
    }

    /** Adds Back leading to {@code parent} when there is one and the bottom row has room; returns whether it was added. */
    public static boolean add(SimpleGui gui, Runnable parent) {
        if (parent == null) {
            return false;
        }
        int size = gui.getVirtualSize();
        for (int slot = size - 9; slot < size; slot++) {
            if (isFree(gui.getGuiElement(slot))) {
                gui.setSlot(slot, Elements.back(parent));
                return true;
            }
        }
        return false;
    }

    /** A slot nothing useful is in: no element, an empty stack, or a pane with a blank name. */
    static boolean isFree(GuiElement element) {
        if (element == null) {
            return true;
        }
        ItemStack stack = element.getItemStack();
        if (stack.isEmpty() || stack.getHoverName().getString().isBlank()) {
            return true;
        }
        // a decorative pane, even when it carries a symbol as its name
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().endsWith("glass_pane");
    }
}
