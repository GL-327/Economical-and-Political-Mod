package com.political;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * Emberforge: forty five identities cut into fourteen forms, so six hundred and thirty pieces.
 *
 * <p>The cuts are named for what the line is made of rather than for the vanilla item
 * underneath, so a emberforge piece reads as emberforge rather than as a renamed sword. The base item still
 * decides the slot, which is what keeps all six hundred and thirty of these usable.</p>
 */
public final class EmberforgeGear {

    private EmberforgeGear() {}

    public static final String ID_PREFIX = "ember";
    public static final String RECIPE_PREFIX = "VX Ember";

    public static final List<GearLine.Form> FORMS = List.of(
            new GearLine.Form("Emberblade", Items.IRON_SWORD, GearLine.Kind.WEAPON, 85, 0, 0,
                    "Still warm from whatever made it."),
            new GearLine.Form("Forgehammer", Items.IRON_AXE, GearLine.Kind.WEAPON, 100, 0, 0,
                    "Made for metal, works on anything."),
            new GearLine.Form("Slagmaul", Items.STONE_AXE, GearLine.Kind.WEAPON, 115, 0, 0,
                    "Everything they threw away, gathered."),
            new GearLine.Form("Cinderpike", Items.TRIDENT, GearLine.Kind.WEAPON, 90, 0, 0,
                    "Long enough to keep the heat off."),
            new GearLine.Form("Coalknife", Items.GOLDEN_SWORD, GearLine.Kind.WEAPON, 75, 0, 0,
                    "Black, and it draws a red line."),
            new GearLine.Form("Emberpick", Items.IRON_PICKAXE, GearLine.Kind.TOOL, 45, 0, 0,
                    "It cuts stone by convincing it."),
            new GearLine.Form("Ashspade", Items.IRON_SHOVEL, GearLine.Kind.TOOL, 35, 0, 0,
                    "There is always more of it."),
            new GearLine.Form("Kilnrake", Items.IRON_HOE, GearLine.Kind.TOOL, 32, 0, 0,
                    "It turns the ground and fires it."),
            new GearLine.Form("Emberhelm", Items.GOLDEN_HELMET, GearLine.Kind.HEAD, 0, 45, 35,
                    "The visor has never fully closed."),
            new GearLine.Form("Forgeplate", Items.GOLDEN_CHESTPLATE, GearLine.Kind.CHEST, 0, 70, 60,
                    "Thick where the sparks land."),
            new GearLine.Form("Cinderguard", Items.GOLDEN_LEGGINGS, GearLine.Kind.LEGS, 0, 55, 45,
                    "Scorched into the right shape."),
            new GearLine.Form("Slagboots", Items.GOLDEN_BOOTS, GearLine.Kind.FEET, 0, 40, 30,
                    "Nothing on the floor matters now."),
            new GearLine.Form("Emberbrand", Items.BLAZE_ROD, GearLine.Kind.CHARM, 0, 30, 25,
                    "Carried lit, always."),
            new GearLine.Form("Forgemark", Items.IRON_BLOCK, GearLine.Kind.CHARM, 0, 35, 30,
                    "Stamped by somebody who is gone.")
    );

    public static Ember motifOf(ItemStack stack) {
        return Ember.byKey(GearLine.motifKeyOf(ID_PREFIX, stack));
    }

    public static GearLine.Form formOf(ItemStack stack) {
        String k = GearLine.formKeyOf(ID_PREFIX, stack);
        if (k == null) return null;
        for (GearLine.Form f : FORMS) {
            if (GearLine.formKey(f).equals(k)) return f;
        }
        return null;
    }

    public static ItemStack create(Ember m, GearLine.Form f) {
        return GearLine.create(ID_PREFIX, "Emberforge", "Forge", m.motif(), f,
                "\u00a76" + m.powerText);
    }

    public static void ensureRecipes() {
        List<GearLine.Motif> motifs = new ArrayList<>();
        for (Ember m : Ember.values()) motifs.add(m.motif());
        GearLine.register(ID_PREFIX, RECIPE_PREFIX, "Emberforge", "Forge",
                motifs, FORMS, Items.BLAZE_ROD, Items.NETHERITE_SCRAP, LineCatalyst.PRIMAL_HEART,
                m -> "\u00a76" + Ember.byKey(m.key()).powerText);
    }
}
