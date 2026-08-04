package com.political;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/**
 * What a room is for.
 *
 * <p>Taken from the shape of a Skyblock floor rather than its contents: mostly rooms that fight
 * you, a few that ask something else, one that is worth finding, and a boss at the far end. The
 * proportions are what make a floor feel like a place instead of a corridor with mobs in it.</p>
 */
public enum DelveRoomKind {

    ENTRANCE("Entrance", ChatFormatting.GREEN, Items.OAK_DOOR,
            "Where you came in. Nothing lives here.", 0, 0),

    NORMAL("Chamber", ChatFormatting.GRAY, Items.STONE_BRICKS,
            "Occupied. Clear it to move on.", 5, 1),

    PUZZLE("Puzzle", ChatFormatting.AQUA, Items.LEVER,
            "Locked from the inside. Work out how.", 0, 2),

    TRAP("Trap", ChatFormatting.RED, Items.TRIPWIRE_HOOK,
            "It was set a long time ago and still works.", 3, 1),

    TREASURE("Treasure", ChatFormatting.GOLD, Items.CHEST,
            "Somebody left something. Somebody is guarding it.", 4, 2),

    MINIBOSS("Miniboss", ChatFormatting.LIGHT_PURPLE, Items.WITHER_SKELETON_SKULL,
            "One of them is much bigger than the others.", 1, 2),

    BOSS("Boss", ChatFormatting.DARK_RED, Items.DRAGON_HEAD,
            "The reason the floor is sealed.", 0, 0);

    public final String display;
    public final ChatFormatting colour;
    public final Item icon;
    public final String blurb;
    /** Roughly how many ordinary mobs this room holds. */
    public final int mobs;
    /** How many secrets this kind of room can hide. */
    public final int secretSlots;

    DelveRoomKind(String display, ChatFormatting colour, Item icon, String blurb,
                    int mobs, int secretSlots) {
        this.display = display;
        this.colour = colour;
        this.icon = icon;
        this.blurb = blurb;
        this.mobs = mobs;
        this.secretSlots = secretSlots;
    }

    /** Whether clearing this room counts toward the floor being finished. */
    public boolean countsForClear() {
        return this != ENTRANCE && this != BOSS;
    }
}
