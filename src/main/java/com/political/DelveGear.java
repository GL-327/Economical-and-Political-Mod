package com.political;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.List;

/**
 * The delve weapons and tools: eight lines cut eight ways, so sixty four.
 *
 * <p>Deliberately fewer than the armour. A player wears four pieces and carries one
 * weapon, so a hundred swords nobody ever compares is worse than sixty they do.</p>
 */
public final class DelveGear {

    private DelveGear() {}

    public record Line(String key, String display, ChatFormatting colour,
                       String flavour, String stat, int amount) {}

    public record Form(String key, String display, Item base, String kind, double scale) {}

    public static final List<Line> LINES = List.of(
            new Line("delveblade", "Delveblade", ChatFormatting.WHITE,
                    "Cut for corridors, not for fields.", "strength", 70),
            new Line("grimhook", "Grimhook", ChatFormatting.DARK_GRAY,
                    "It brings things closer.", "ferocity", 16),
            new Line("emberfang", "Emberfang", ChatFormatting.RED,
                    "It leaves a mark that keeps working.", "strength", 85),
            new Line("tidepiercer", "Tidepiercer", ChatFormatting.AQUA,
                    "Thin enough to find a gap.", "crit_chance", 18),
            new Line("gravemaul", "Gravemaul", ChatFormatting.DARK_PURPLE,
                    "Heavy, and it only needs to land once.", "crit_damage", 90),
            new Line("lodefinder", "Lodefinder", ChatFormatting.GOLD,
                    "It knows where the good rooms are.", "magic_find", 25),
            new Line("wardenspike", "Wardenspike", ChatFormatting.GREEN,
                    "Made for the things that guard doors.", "strength", 78),
            new Line("hollowedge", "Hollowedge", ChatFormatting.LIGHT_PURPLE,
                    "There is very little of it left.", "crit_damage", 100)
    );

    public static final List<Form> FORMS = List.of(
            new Form("sword", "Sword", Items.IRON_SWORD, "WEAPON", 1.00),
            new Form("cleaver", "Cleaver", Items.IRON_AXE, "WEAPON", 1.25),
            new Form("dagger", "Dagger", Items.GOLDEN_SWORD, "WEAPON", 0.70),
            new Form("spear", "Spear", Items.TRIDENT, "WEAPON", 1.10),
            new Form("maul", "Maul", Items.STONE_AXE, "WEAPON", 1.45),
            new Form("pick", "Pick", Items.IRON_PICKAXE, "TOOL", 0.60),
            new Form("spade", "Spade", Items.IRON_SHOVEL, "TOOL", 0.50),
            new Form("bow", "Bow", Items.BOW, "BOW", 0.90)
    );

    public static String idOf(Line l, Form f) {
        return "delve_" + l.key() + "_" + f.key();
    }

    public static String nameOf(Line l, Form f) {
        return l.display() + " " + f.display();
    }

    public static int total() {
        return LINES.size() * FORMS.size();
    }
}
