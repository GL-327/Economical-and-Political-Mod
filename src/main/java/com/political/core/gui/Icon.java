package com.political.core.gui;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

/**
 * The icon set of the layout module (STYLE.md): which item stands for which role, so the same job looks
 * the same on every screen. The ids are plain strings and are looked up when used, which keeps this list
 * testable without starting Minecraft; {@code GuiSelfCheck} logs any id that does not resolve.
 */
public enum Icon {
    FRAME_DARK("minecraft:black_stained_glass_pane", "Interior of the frame"),
    FRAME_EDGE("minecraft:gray_stained_glass_pane", "Edge of the standard frame"),
    FRAME_ACCENT("minecraft:cyan_stained_glass_pane", "Edge of an information screen"),
    FRAME_DANGER("minecraft:red_stained_glass_pane", "Edge of a screen that deletes or hands out power"),
    BACK("minecraft:arrow", "Back, bottom left"),
    CLOSE("minecraft:barrier", "Close, bottom right"),
    PREVIOUS("minecraft:spectral_arrow", "Previous page"),
    NEXT("minecraft:spectral_arrow", "Next page"),
    PAGE("minecraft:paper", "Page counter"),
    CONFIRM("minecraft:lime_concrete", "Confirm button"),
    CANCEL("minecraft:red_concrete", "Cancel button"),
    NOTICE("minecraft:command_block", "What a confirmation is about"),
    ON("minecraft:lime_dye", "A switch that is on"),
    OFF("minecraft:gray_dye", "A switch that is off");

    private final String itemId;
    private final String use;

    Icon(String itemId, String use) {
        this.itemId = itemId;
        this.use = use;
    }

    public String itemId() {
        return itemId;
    }

    public String use() {
        return use;
    }

    /** The item for this role. Call only once the game registries exist (any time after startup). */
    public Item item() {
        return BuiltInRegistries.ITEM.getValue(Identifier.parse(itemId));
    }
}
