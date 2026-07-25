package com.political;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * Clockwork: forty five mechanisms cut into fourteen forms, so six hundred and thirty pieces.
 *
 * <p>The forms lean toward the pick and the shovel because this is the line you take underground.
 * Diamond bases sit under the tools so a Clockwork pick is worth carrying on its own, and the armour
 * is iron plate rather than hide or gold, which keeps the three big lines visually separate at a
 * glance.</p>
 */
public final class ClockworkGear {

    private ClockworkGear() {}

    public static final String ID_PREFIX = "cog";
    public static final String RECIPE_PREFIX = "VX Cog";

    public static final List<GearLine.Form> FORMS = List.of(
            new GearLine.Form("Bore", Items.DIAMOND_PICKAXE, GearLine.Kind.TOOL, 40, 0, 0,
                    "It goes in straight and does not stop."),
            new GearLine.Form("Auger", Items.IRON_PICKAXE, GearLine.Kind.TOOL, 35, 0, 0,
                    "Turning is the whole idea."),
            new GearLine.Form("Scoop", Items.DIAMOND_SHOVEL, GearLine.Kind.TOOL, 30, 0, 0,
                    "Moves more per swing than it should."),
            new GearLine.Form("Ripper", Items.DIAMOND_AXE, GearLine.Kind.TOOL, 85, 0, 0,
                    "Built for timber, happy anywhere."),
            new GearLine.Form("Tiller", Items.DIAMOND_HOE, GearLine.Kind.TOOL, 30, 0, 0,
                    "It works ground nobody else would bother with."),
            new GearLine.Form("Shear", Items.SHEARS, GearLine.Kind.TOOL, 25, 0, 0,
                    "Two edges and no opinion."),

            new GearLine.Form("Wrench", Items.IRON_SWORD, GearLine.Kind.WEAPON, 70, 0, 0,
                    "A tool, until it is not."),
            new GearLine.Form("Piledriver", Items.IRON_AXE, GearLine.Kind.WEAPON, 105, 0, 0,
                    "One purpose, delivered downward."),
            new GearLine.Form("Sparklance", Items.GOLDEN_SWORD, GearLine.Kind.WEAPON, 80, 0, 0,
                    "It arcs before it lands."),

            new GearLine.Form("Visor", Items.IRON_HELMET, GearLine.Kind.HEAD, 0, 40, 30,
                    "Smoked glass and a lamp bracket."),
            new GearLine.Form("Plating", Items.IRON_CHESTPLATE, GearLine.Kind.CHEST, 0, 65, 55,
                    "Riveted where it matters."),
            new GearLine.Form("Bracing", Items.IRON_LEGGINGS, GearLine.Kind.LEGS, 0, 50, 40,
                    "It carries the weight so your knees do not."),
            new GearLine.Form("Treads", Items.IRON_BOOTS, GearLine.Kind.FEET, 0, 35, 25,
                    "Grip on everything, including things you should not stand on."),

            new GearLine.Form("Governor Pin", Items.CLOCK, GearLine.Kind.CHARM, 0, 25, 20,
                    "Carried, and quietly correcting you.")
    );

    public static Mechanism mechanismOf(ItemStack stack) {
        return Mechanism.byKey(GearLine.motifKeyOf(ID_PREFIX, stack));
    }

    public static GearLine.Form formOf(ItemStack stack) {
        String k = GearLine.formKeyOf(ID_PREFIX, stack);
        if (k == null) return null;
        for (GearLine.Form f : FORMS) {
            if (f.display().toLowerCase(java.util.Locale.ROOT).replace(' ', '_').equals(k)) return f;
        }
        return null;
    }

    public static ItemStack create(Mechanism m, GearLine.Form f) {
        return GearLine.create(ID_PREFIX, "Clockwork", "Works", m.motif(), f, "§b" + m.powerText);
    }

    public static void ensureRecipes() {
        List<GearLine.Motif> motifs = new ArrayList<>();
        for (Mechanism m : Mechanism.values()) motifs.add(m.motif());
        GearLine.register(ID_PREFIX, RECIPE_PREFIX, "Clockwork", "Works",
                motifs, FORMS, Items.IRON_INGOT, Items.REDSTONE,
                mo -> {
                    Mechanism m = Mechanism.byKey(mo.key());
                    return m == null ? null : "§b" + m.powerText;
                });
    }

    public static List<ItemStack> allStacks() {
        List<ItemStack> out = new ArrayList<>();
        for (Mechanism m : Mechanism.values()) {
            for (GearLine.Form f : FORMS) out.add(create(m, f));
        }
        return out;
    }
}
