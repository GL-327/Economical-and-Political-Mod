package com.political;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * Prismatic: forty five identities cut into fourteen forms, so six hundred and thirty pieces.
 *
 * <p>The cuts are named for what the line is made of rather than for the vanilla item
 * underneath, so a prismatic piece reads as prismatic rather than as a renamed sword. The base item still
 * decides the slot, which is what keeps all six hundred and thirty of these usable.</p>
 */
public final class PrismaticGear {

    private PrismaticGear() {}

    public static final String ID_PREFIX = "prism";
    public static final String RECIPE_PREFIX = "VX Prism";

    public static final List<GearLine.Form> FORMS = List.of(
            new GearLine.Form("Prismblade", Items.IRON_SWORD, GearLine.Kind.WEAPON, 84, 0, 0,
                    "The edge is where the light gives up."),
            new GearLine.Form("Facetaxe", Items.IRON_AXE, GearLine.Kind.WEAPON, 94, 0, 0,
                    "Every angle is doing something."),
            new GearLine.Form("Lensmaul", Items.STONE_AXE, GearLine.Kind.WEAPON, 106, 0, 0,
                    "It focuses the whole swing to a point."),
            new GearLine.Form("Beamspear", Items.TRIDENT, GearLine.Kind.WEAPON, 89, 0, 0,
                    "Straight, obviously."),
            new GearLine.Form("Shardknife", Items.GOLDEN_SWORD, GearLine.Kind.WEAPON, 71, 0, 0,
                    "Broken off something larger."),
            new GearLine.Form("Prismpick", Items.IRON_PICKAXE, GearLine.Kind.TOOL, 41, 0, 0,
                    "It shows you the seam before it cuts."),
            new GearLine.Form("Glassspade", Items.IRON_SHOVEL, GearLine.Kind.TOOL, 34, 0, 0,
                    "Sharper than a shovel needs to be."),
            new GearLine.Form("Lightrake", Items.IRON_HOE, GearLine.Kind.TOOL, 33, 0, 0,
                    "Everything it touches grows toward it."),
            new GearLine.Form("Facetcrown", Items.DIAMOND_HELMET, GearLine.Kind.HEAD, 0, 44, 36,
                    "It scatters what it cannot stop."),
            new GearLine.Form("Prismplate", Items.DIAMOND_CHESTPLATE, GearLine.Kind.CHEST, 0, 72, 58,
                    "You are harder to look at now."),
            new GearLine.Form("Refractguard", Items.DIAMOND_LEGGINGS, GearLine.Kind.LEGS, 0, 54, 46,
                    "Their aim goes somewhere else."),
            new GearLine.Form("Glassstep", Items.DIAMOND_BOOTS, GearLine.Kind.FEET, 0, 39, 31,
                    "You leave a bright line behind."),
            new GearLine.Form("Prismcore", Items.AMETHYST_SHARD, GearLine.Kind.CHARM, 0, 29, 27,
                    "It has never been the same colour twice."),
            new GearLine.Form("Lensward", Items.AMETHYST_BLOCK, GearLine.Kind.CHARM, 0, 33, 31,
                    "It has been focusing something for years.")
    );

    public static Prism motifOf(ItemStack stack) {
        return Prism.byKey(GearLine.motifKeyOf(ID_PREFIX, stack));
    }

    public static GearLine.Form formOf(ItemStack stack) {
        String k = GearLine.formKeyOf(ID_PREFIX, stack);
        if (k == null) return null;
        for (GearLine.Form f : FORMS) {
            if (GearLine.formKey(f).equals(k)) return f;
        }
        return null;
    }

    public static ItemStack create(Prism m, GearLine.Form f) {
        return GearLine.create(ID_PREFIX, "Prismatic", "Angle", m.motif(), f,
                "\u00a76" + m.powerText);
    }

    public static void ensureRecipes() {
        List<GearLine.Motif> motifs = new ArrayList<>();
        for (Prism m : Prism.values()) motifs.add(m.motif());
        GearLine.register(ID_PREFIX, RECIPE_PREFIX, "Prismatic", "Angle",
                motifs, FORMS, Items.AMETHYST_SHARD, Items.GLASS, LineCatalyst.PRISM_CORE,
                m -> "\u00a76" + Prism.byKey(m.key()).powerText);
    }
}
