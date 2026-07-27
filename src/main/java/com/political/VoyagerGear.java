package com.political;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Voyager: forty five waypoints cut into fourteen forms, so six hundred and thirty pieces.
 *
 * <p>The forms are travelling kit rather than war gear. A Voyager piece is a pack, a lamp or a walking
 * axe, and the numbers are modest on purpose because the line makes its money from Leagues instead of
 * from raw stats. Sit at home in a Voyager set and it is the weakest thing in the mod. Take it four
 * thousand blocks out into its own reach and very little competes.</p>
 */
public final class VoyagerGear {

    private VoyagerGear() {}

    public static final String ID_PREFIX = "way";
    public static final String RECIPE_PREFIX = "VX Way";

    public static final List<GearLine.Form> FORMS = List.of(
            new GearLine.Form("Trailblade", Items.IRON_SWORD, GearLine.Kind.WEAPON, 60, 0, 0,
                    "It has cut more branches than anything else."),
            new GearLine.Form("Handaxe", Items.IRON_AXE, GearLine.Kind.WEAPON, 85, 0, 0,
                    "Firewood first, everything else second."),
            new GearLine.Form("Walking Spear", Items.TRIDENT, GearLine.Kind.WEAPON, 90, 0, 0,
                    "A staff that solved a problem once."),
            new GearLine.Form("Sling", Items.BOW, GearLine.Kind.RANGED, 55, 0, 0,
                    "Quiet, and it uses whatever is on the ground."),

            new GearLine.Form("Prospector", Items.IRON_PICKAXE, GearLine.Kind.TOOL, 35, 0, 0,
                    "It has opened more hills than mines."),
            new GearLine.Form("Camp Spade", Items.IRON_SHOVEL, GearLine.Kind.TOOL, 25, 0, 0,
                    "For a fire pit, a latrine and once a grave."),
            new GearLine.Form("Forager", Items.IRON_HOE, GearLine.Kind.TOOL, 25, 0, 0,
                    "It knows which roots are worth the trouble."),

            new GearLine.Form("Wayhood", Items.LEATHER_HELMET, GearLine.Kind.HEAD, 0, 30, 25,
                    "Weathered into the shape of a head."),
            new GearLine.Form("Travelcoat", Items.LEATHER_CHESTPLATE, GearLine.Kind.CHEST, 0, 50, 45,
                    "Patched in eleven places and still going."),
            new GearLine.Form("Roadwraps", Items.LEATHER_LEGGINGS, GearLine.Kind.LEGS, 0, 40, 35,
                    "Wound on every morning without thinking."),
            new GearLine.Form("Longboots", Items.LEATHER_BOOTS, GearLine.Kind.FEET, 0, 30, 25,
                    "They have been resoled more than once."),

            new GearLine.Form("Waypack", Items.BUNDLE, GearLine.Kind.CHARM, 0, 20, 25,
                    "Everything you own, and it is not much."),
            new GearLine.Form("Traillamp", Items.LANTERN, GearLine.Kind.CHARM, 0, 15, 15,
                    "It has been lit in four dimensions."),
            new GearLine.Form("Marker Stone", Items.COMPASS, GearLine.Kind.CHARM, 0, 20, 20,
                    "Carried so somebody could find the way back.")
    );

    public static Waypoint waypointOf(ItemStack stack) {
        return Waypoint.byKey(GearLine.motifKeyOf(ID_PREFIX, stack));
    }

    public static GearLine.Form formOf(ItemStack stack) {
        String k = GearLine.formKeyOf(ID_PREFIX, stack);
        if (k == null) return null;
        for (GearLine.Form f : FORMS) {
            if (GearLine.formKey(f).equals(k)) return f;
        }
        return null;
    }

    public static ItemStack create(Waypoint w, GearLine.Form f) {
        return GearLine.create(ID_PREFIX, "Voyager", "Reach", w.motif(), f, "§a" + w.powerText);
    }

    public static void ensureRecipes() {
        List<GearLine.Motif> motifs = new ArrayList<>();
        for (Waypoint w : Waypoint.values()) motifs.add(w.motif());
        GearLine.register(ID_PREFIX, RECIPE_PREFIX, "Voyager", "Reach",
                motifs, FORMS, Items.LEATHER, Items.ENDER_PEARL, LineCatalyst.SURVEYORS_SEAL,
                mo -> {
                    Waypoint w = Waypoint.byKey(mo.key());
                    return w == null ? null : "§a" + w.powerText;
                });
    }

    public static List<ItemStack> allStacks() {
        List<ItemStack> out = new ArrayList<>();
        for (Waypoint w : Waypoint.values()) {
            for (GearLine.Form f : FORMS) out.add(create(w, f));
        }
        return out;
    }
}
