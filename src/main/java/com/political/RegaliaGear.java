package com.political;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Regalia: forty five offices cut into fourteen forms, so six hundred and thirty pieces.
 *
 * <p>The forms are ceremonial rather than practical, which is the joke and also the point. A Regalia
 * blade is a Sword of State and a Regalia chestpiece is a set of Robes, and none of it is worth much
 * on its own. What makes it the strongest gear in the mod is Standing, and Standing is something you
 * have to build a settlement and win an election to get.</p>
 */
public final class RegaliaGear {

    private RegaliaGear() {}

    public static final String ID_PREFIX = "regal";
    public static final String RECIPE_PREFIX = "VX Regalia";

    public static final List<GearLine.Form> FORMS = List.of(
            new GearLine.Form("Sword of State", Items.GOLDEN_SWORD, GearLine.Kind.WEAPON, 70, 0, 0,
                    "Carried in front of somebody important."),
            new GearLine.Form("Mace of Office", Items.GOLDEN_AXE, GearLine.Kind.WEAPON, 100, 0, 0,
                    "Ceremonial, and extremely heavy."),
            new GearLine.Form("Halberd", Items.NETHERITE_AXE, GearLine.Kind.WEAPON, 115, 0, 0,
                    "Stood beside a door for three hundred years."),
            new GearLine.Form("Rapier of Rank", Items.NETHERITE_SWORD, GearLine.Kind.WEAPON, 85, 0, 0,
                    "Worn at the hip, drawn almost never."),
            new GearLine.Form("Standard", Items.TRIDENT, GearLine.Kind.WEAPON, 90, 0, 0,
                    "Everybody looks at where it is."),

            new GearLine.Form("Groundbreaker", Items.GOLDEN_PICKAXE, GearLine.Kind.TOOL, 35, 0, 0,
                    "One symbolic swing, then somebody else finishes."),
            new GearLine.Form("Ribbon Shears", Items.SHEARS, GearLine.Kind.TOOL, 25, 0, 0,
                    "It has opened more buildings than it has cut."),
            new GearLine.Form("Ceremonial Spade", Items.GOLDEN_SHOVEL, GearLine.Kind.TOOL, 30, 0, 0,
                    "Polished, and it has never met soil."),

            new GearLine.Form("Coronet", Items.GOLDEN_HELMET, GearLine.Kind.HEAD, 0, 40, 35,
                    "It sits where everybody can see it."),
            new GearLine.Form("Robes", Items.GOLDEN_CHESTPLATE, GearLine.Kind.CHEST, 0, 65, 60,
                    "Layered until the person underneath stops mattering."),
            new GearLine.Form("Sash", Items.GOLDEN_LEGGINGS, GearLine.Kind.LEGS, 0, 50, 45,
                    "Worn across, never around."),
            new GearLine.Form("Court Shoes", Items.GOLDEN_BOOTS, GearLine.Kind.FEET, 0, 35, 30,
                    "Made for standing still for hours."),

            new GearLine.Form("Signet", Items.GOLD_NUGGET, GearLine.Kind.CHARM, 0, 25, 25,
                    "Pressed into wax and that settles it."),
            new GearLine.Form("Charter", Items.WRITTEN_BOOK, GearLine.Kind.CHARM, 0, 30, 30,
                    "The document the whole place rests on.")
    );

    public static Office officeOf(ItemStack stack) {
        return Office.byKey(GearLine.motifKeyOf(ID_PREFIX, stack));
    }

    public static GearLine.Form formOf(ItemStack stack) {
        String k = GearLine.formKeyOf(ID_PREFIX, stack);
        if (k == null) return null;
        for (GearLine.Form f : FORMS) {
            if (GearLine.formKey(f).equals(k)) return f;
        }
        return null;
    }

    public static ItemStack create(Office o, GearLine.Form f) {
        return GearLine.create(ID_PREFIX, "Regalia", "Estate", o.motif(), f, o.powerText());
    }

    public static void ensureRecipes() {
        List<GearLine.Motif> motifs = new ArrayList<>();
        for (Office o : Office.values()) motifs.add(o.motif());
        GearLine.register(ID_PREFIX, RECIPE_PREFIX, "Regalia", "Estate",
                motifs, FORMS, Items.GOLD_INGOT, Items.EMERALD, LineCatalyst.SEAL_OF_OFFICE,
                mo -> {
                    Office o = Office.byKey(mo.key());
                    return o == null ? null : o.powerText();
                });
    }

    public static List<ItemStack> allStacks() {
        List<ItemStack> out = new ArrayList<>();
        for (Office o : Office.values()) {
            for (GearLine.Form f : FORMS) out.add(create(o, f));
        }
        return out;
    }
}
