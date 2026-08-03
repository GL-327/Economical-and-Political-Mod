package com.political;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * Ashen Crown: forty five identities cut into fourteen forms, so six hundred and thirty pieces.
 *
 * <p>The cuts are named for what the line is made of rather than for the vanilla item
 * underneath, so a ashen crown piece reads as ashen crown rather than as a renamed sword. The base item still
 * decides the slot, which is what keeps all six hundred and thirty of these usable.</p>
 */
public final class AshencrownGear {

    private AshencrownGear() {}

    public static final String ID_PREFIX = "ashen";
    public static final String RECIPE_PREFIX = "VX Ashen";

    public static final List<GearLine.Form> FORMS = List.of(
            new GearLine.Form("Ashenblade", Items.IRON_SWORD, GearLine.Kind.WEAPON, 82, 0, 0,
                    "It has been in a tomb and it shows."),
            new GearLine.Form("Barrowaxe", Items.IRON_AXE, GearLine.Kind.WEAPON, 96, 0, 0,
                    "Grave goods, still in use."),
            new GearLine.Form("Cryptmaul", Items.STONE_AXE, GearLine.Kind.WEAPON, 112, 0, 0,
                    "It was for the door, originally."),
            new GearLine.Form("Sceptre", Items.TRIDENT, GearLine.Kind.WEAPON, 92, 0, 0,
                    "It has always been a weapon."),
            new GearLine.Form("Nailknife", Items.GOLDEN_SWORD, GearLine.Kind.WEAPON, 72, 0, 0,
                    "Short, and it finishes things."),
            new GearLine.Form("Gravepick", Items.IRON_PICKAXE, GearLine.Kind.TOOL, 42, 0, 0,
                    "It has opened more than stone."),
            new GearLine.Form("Bierspade", Items.IRON_SHOVEL, GearLine.Kind.TOOL, 36, 0, 0,
                    "It has done honest work and other work."),
            new GearLine.Form("Bonerake", Items.IRON_HOE, GearLine.Kind.TOOL, 31, 0, 0,
                    "It turns up more than soil."),
            new GearLine.Form("Ashcrown", Items.CHAINMAIL_HELMET, GearLine.Kind.HEAD, 0, 42, 38,
                    "Worn by somebody with no realm left."),
            new GearLine.Form("Gravemail", Items.CHAINMAIL_CHESTPLATE, GearLine.Kind.CHEST, 0, 68, 62,
                    "Cold in a way that is not the metal."),
            new GearLine.Form("Cryptguard", Items.CHAINMAIL_LEGGINGS, GearLine.Kind.LEGS, 0, 52, 48,
                    "It remembers the shape of somebody else."),
            new GearLine.Form("Palefoot", Items.CHAINMAIL_BOOTS, GearLine.Kind.FEET, 0, 38, 32,
                    "You do not make a sound now."),
            new GearLine.Form("Signet", Items.GOLD_INGOT, GearLine.Kind.CHARM, 0, 28, 26,
                    "It seals things that should stay sealed."),
            new GearLine.Form("Reliquary", Items.BONE_BLOCK, GearLine.Kind.CHARM, 0, 32, 30,
                    "Somebody important is in here.")
    );

    public static Ashen motifOf(ItemStack stack) {
        return Ashen.byKey(GearLine.motifKeyOf(ID_PREFIX, stack));
    }

    public static GearLine.Form formOf(ItemStack stack) {
        String k = GearLine.formKeyOf(ID_PREFIX, stack);
        if (k == null) return null;
        for (GearLine.Form f : FORMS) {
            if (GearLine.formKey(f).equals(k)) return f;
        }
        return null;
    }

    public static ItemStack create(Ashen m, GearLine.Form f) {
        return GearLine.create(ID_PREFIX, "Ashen Crown", "House", m.motif(), f,
                "\u00a76" + m.powerText);
    }

    public static void ensureRecipes() {
        List<GearLine.Motif> motifs = new ArrayList<>();
        for (Ashen m : Ashen.values()) motifs.add(m.motif());
        GearLine.register(ID_PREFIX, RECIPE_PREFIX, "Ashen Crown", "House",
                motifs, FORMS, Items.BONE_BLOCK, Items.GOLD_INGOT, LineCatalyst.GRAVE_SIGIL,
                m -> "\u00a76" + Ashen.byKey(m.key()).powerText);
    }
}
