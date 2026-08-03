package com.political;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * Thornwood: forty five identities cut into fourteen forms, so six hundred and thirty pieces.
 *
 * <p>The cuts are named for what the line is made of rather than for the vanilla item
 * underneath, so a thornwood piece reads as thornwood rather than as a renamed sword. The base item still
 * decides the slot, which is what keeps all six hundred and thirty of these usable.</p>
 */
public final class ThornwoodGear {

    private ThornwoodGear() {}

    public static final String ID_PREFIX = "thorn";
    public static final String RECIPE_PREFIX = "VX Thorn";

    public static final List<GearLine.Form> FORMS = List.of(
            new GearLine.Form("Thornblade", Items.IRON_SWORD, GearLine.Kind.WEAPON, 78, 0, 0,
                    "Grown to an edge over years."),
            new GearLine.Form("Rootaxe", Items.IRON_AXE, GearLine.Kind.WEAPON, 92, 0, 0,
                    "It takes what it is owed."),
            new GearLine.Form("Bramblemaul", Items.STONE_AXE, GearLine.Kind.WEAPON, 108, 0, 0,
                    "Heavy with everything caught in it."),
            new GearLine.Form("Barbspear", Items.TRIDENT, GearLine.Kind.WEAPON, 88, 0, 0,
                    "It goes in easily and stays."),
            new GearLine.Form("Sapknife", Items.GOLDEN_SWORD, GearLine.Kind.WEAPON, 68, 0, 0,
                    "It leaves something behind in the cut."),
            new GearLine.Form("Rootpick", Items.IRON_PICKAXE, GearLine.Kind.TOOL, 38, 0, 0,
                    "It finds the crack that was already there."),
            new GearLine.Form("Loamspade", Items.IRON_SHOVEL, GearLine.Kind.TOOL, 33, 0, 0,
                    "It knows good ground from bad."),
            new GearLine.Form("Grovehoe", Items.IRON_HOE, GearLine.Kind.TOOL, 35, 0, 0,
                    "Everything it turns comes up green."),
            new GearLine.Form("Leafcrown", Items.LEATHER_HELMET, GearLine.Kind.HEAD, 0, 38, 40,
                    "It is still growing, slowly."),
            new GearLine.Form("Barkplate", Items.LEATHER_CHESTPLATE, GearLine.Kind.CHEST, 0, 62, 65,
                    "Rings all the way through."),
            new GearLine.Form("Rootguard", Items.LEATHER_LEGGINGS, GearLine.Kind.LEGS, 0, 48, 50,
                    "It has taken hold of you a little."),
            new GearLine.Form("Mossboots", Items.LEATHER_BOOTS, GearLine.Kind.FEET, 0, 33, 35,
                    "You stop leaving tracks."),
            new GearLine.Form("Seedcharm", Items.WHEAT_SEEDS, GearLine.Kind.CHARM, 0, 22, 28,
                    "It has been waiting for the right year."),
            new GearLine.Form("Heartwood", Items.OAK_LOG, GearLine.Kind.CHARM, 0, 28, 32,
                    "Cut from the part that never saw sun.")
    );

    public static Thorn motifOf(ItemStack stack) {
        return Thorn.byKey(GearLine.motifKeyOf(ID_PREFIX, stack));
    }

    public static GearLine.Form formOf(ItemStack stack) {
        String k = GearLine.formKeyOf(ID_PREFIX, stack);
        if (k == null) return null;
        for (GearLine.Form f : FORMS) {
            if (GearLine.formKey(f).equals(k)) return f;
        }
        return null;
    }

    public static ItemStack create(Thorn m, GearLine.Form f) {
        return GearLine.create(ID_PREFIX, "Thornwood", "Wood", m.motif(), f,
                "\u00a76" + m.powerText);
    }

    public static void ensureRecipes() {
        List<GearLine.Motif> motifs = new ArrayList<>();
        for (Thorn m : Thorn.values()) motifs.add(m.motif());
        GearLine.register(ID_PREFIX, RECIPE_PREFIX, "Thornwood", "Wood",
                motifs, FORMS, Items.MOSS_BLOCK, Items.AMETHYST_SHARD, LineCatalyst.VERDANT_SEED,
                m -> "\u00a76" + Thorn.byKey(m.key()).powerText);
    }
}
