package com.political;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * Tidecaller: forty five identities cut into fourteen forms, so six hundred and thirty pieces.
 *
 * <p>The cuts are named for what the line is made of rather than for the vanilla item
 * underneath, so a tidecaller piece reads as tidecaller rather than as a renamed sword. The base item still
 * decides the slot, which is what keeps all six hundred and thirty of these usable.</p>
 */
public final class TidecallerGear {

    private TidecallerGear() {}

    public static final String ID_PREFIX = "tide";
    public static final String RECIPE_PREFIX = "VX Tide";

    public static final List<GearLine.Form> FORMS = List.of(
            new GearLine.Form("Tideblade", Items.IRON_SWORD, GearLine.Kind.WEAPON, 80, 0, 0,
                    "Wet the whole way down the edge."),
            new GearLine.Form("Harpoon", Items.TRIDENT, GearLine.Kind.WEAPON, 95, 0, 0,
                    "It only ever goes one direction."),
            new GearLine.Form("Coralmace", Items.STONE_AXE, GearLine.Kind.WEAPON, 110, 0, 0,
                    "Grown, not forged, and heavier for it."),
            new GearLine.Form("Tidehook", Items.IRON_AXE, GearLine.Kind.WEAPON, 90, 0, 0,
                    "For bringing things in."),
            new GearLine.Form("Reefknife", Items.GOLDEN_SWORD, GearLine.Kind.WEAPON, 70, 0, 0,
                    "Small work in tight places."),
            new GearLine.Form("Dredge", Items.IRON_SHOVEL, GearLine.Kind.TOOL, 35, 0, 0,
                    "It brings up whatever is down there."),
            new GearLine.Form("Coralpick", Items.IRON_PICKAXE, GearLine.Kind.TOOL, 40, 0, 0,
                    "It knows where the soft parts are."),
            new GearLine.Form("Netrake", Items.IRON_HOE, GearLine.Kind.TOOL, 30, 0, 0,
                    "Four rows and everything in them."),
            new GearLine.Form("Diadem", Items.LEATHER_HELMET, GearLine.Kind.HEAD, 0, 40, 35,
                    "Salt has been at it for a while."),
            new GearLine.Form("Mantle", Items.LEATHER_CHESTPLATE, GearLine.Kind.CHEST, 0, 65, 60,
                    "It holds the water out and the cold in."),
            new GearLine.Form("Tideguard", Items.LEATHER_LEGGINGS, GearLine.Kind.LEGS, 0, 50, 45,
                    "Layered the way a hull is."),
            new GearLine.Form("Wadeboots", Items.LEATHER_BOOTS, GearLine.Kind.FEET, 0, 35, 30,
                    "You stop noticing the depth."),
            new GearLine.Form("Conch", Items.NAUTILUS_SHELL, GearLine.Kind.CHARM, 0, 25, 25,
                    "Held to the ear once, and never again."),
            new GearLine.Form("Anchorstone", Items.PRISMARINE, GearLine.Kind.CHARM, 0, 30, 30,
                    "It has an opinion about staying.")
    );

    public static Tide motifOf(ItemStack stack) {
        return Tide.byKey(GearLine.motifKeyOf(ID_PREFIX, stack));
    }

    public static GearLine.Form formOf(ItemStack stack) {
        String k = GearLine.formKeyOf(ID_PREFIX, stack);
        if (k == null) return null;
        for (GearLine.Form f : FORMS) {
            if (GearLine.formKey(f).equals(k)) return f;
        }
        return null;
    }

    public static ItemStack create(Tide m, GearLine.Form f) {
        return GearLine.create(ID_PREFIX, "Tidecaller", "Depth", m.motif(), f,
                "\u00a76" + m.powerText);
    }

    public static void ensureRecipes() {
        List<GearLine.Motif> motifs = new ArrayList<>();
        for (Tide m : Tide.values()) motifs.add(m.motif());
        GearLine.register(ID_PREFIX, RECIPE_PREFIX, "Tidecaller", "Depth",
                motifs, FORMS, Items.PRISMARINE_SHARD, Items.NAUTILUS_SHELL, LineCatalyst.ABYSSAL_PEARL,
                m -> "\u00a76" + Tide.byKey(m.key()).powerText);
    }
}
