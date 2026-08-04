package com.political;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The delve armour: six families, six grades, four pieces, so a hundred and forty four.
 *
 * <p>The family says what a set is for and the grade says how much of it you get, which
 * means a Hallowed Bulwark is recognisably the same thing as a Worn Bulwark rather than a
 * different item that shares a word. Six grades is enough of a ladder to climb and few
 * enough that a player can hold all of them in their head.</p>
 *
 * <p>All of it works on the surface. It is simply better underground, and better again
 * once it has been marked, which is the whole shape of the reward loop.</p>
 */
public final class DelveArmour {

    private DelveArmour() {}

    /** One family, and the stats it leans on. */
    public record Family(String key, String display, ChatFormatting colour,
                         String base, String flavour, List<String> stats, List<Integer> amounts) {}

    /** One grade, and how much it multiplies the family's numbers by. */
    public record Grade(String key, String display, ChatFormatting colour, double scale) {}

    /** One piece, and its share of the set's total. */
    public record Piece(String key, String display, String suffix, double share) {}

    public static final List<Family> FAMILIES = List.of(
            new Family("bulwark", "Bulwark", ChatFormatting.GRAY, "IRON",
                    "Made for standing in a doorway and staying there.", List.of("defense", "health"), List.of(60, 45)),
            new Family("shade", "Shade", ChatFormatting.DARK_GRAY, "LEATHER",
                    "Nobody has ever heard it arrive.", List.of("speed", "crit_chance"), List.of(6, 12)),
            new Family("emberguard", "Emberguard", ChatFormatting.RED, "GOLDEN",
                    "Still warm from whatever it was quenched in.", List.of("strength", "defense"), List.of(55, 30)),
            new Family("tidebound", "Tidebound", ChatFormatting.AQUA, "CHAINMAIL",
                    "It has been underwater longer than it was above it.", List.of("health", "intelligence"), List.of(55, 60)),
            new Family("gravecall", "Gravecall", ChatFormatting.DARK_PURPLE, "IRON",
                    "Worn by somebody who did not come back up.", List.of("ferocity", "crit_damage"), List.of(14, 50)),
            new Family("lodestar", "Lodestar", ChatFormatting.GOLD, "DIAMOND",
                    "It points at whatever is worth having.", List.of("magic_find", "intelligence"), List.of(20, 45))
    );

    public static final List<Grade> GRADES = List.of(
            new Grade("worn", "Worn", ChatFormatting.GRAY, 1.00),
            new Grade("kept", "Kept", ChatFormatting.WHITE, 1.35),
            new Grade("proven", "Proven", ChatFormatting.GREEN, 1.75),
            new Grade("storied", "Storied", ChatFormatting.BLUE, 2.20),
            new Grade("hallowed", "Hallowed", ChatFormatting.LIGHT_PURPLE, 2.75),
            new Grade("undying", "Undying", ChatFormatting.GOLD, 3.40)
    );

    public static final List<Piece> PIECES = List.of(
            new Piece("helm", "Helm", "HELMET", 0.80),
            new Piece("chest", "Chestpiece", "CHESTPLATE", 1.40),
            new Piece("legs", "Greaves", "LEGGINGS", 1.10),
            new Piece("boots", "Boots", "BOOTS", 0.70)
    );

    /** The vanilla item a family's piece is built on. */
    public static Item baseItem(Family f, Piece p) {
        String name = f.base() + "_" + p.suffix();
        var found = net.minecraft.core.registries.BuiltInRegistries.ITEM.getOptional(
                net.minecraft.resources.Identifier.fromNamespaceAndPath("minecraft",
                        name.toLowerCase(Locale.ROOT)));
        return found.orElse(Items.LEATHER_CHESTPLATE);
    }

    public static String idOf(Family f, Grade g, Piece p) {
        return "delve_" + f.key() + "_" + g.key() + "_" + p.key();
    }

    public static String nameOf(Family f, Grade g, Piece p) {
        return (g.display().isEmpty() ? "" : g.display() + " ")
                + f.display() + " " + p.display();
    }

    public static int total() {
        return FAMILIES.size() * GRADES.size() * PIECES.size();
    }
}
