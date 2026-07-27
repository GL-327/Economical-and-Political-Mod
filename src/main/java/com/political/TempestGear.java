package com.political;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Tempest: forty five storms cut into fourteen forms, so six hundred and thirty pieces.
 *
 * <p>The forms are reach weapons and light armour because Tempest wants you moving between targets
 * rather than standing on one. The line is deliberately weaker than Beastcraft on a single hit and
 * far stronger once two charges are in play, which is the whole trade.</p>
 */
public final class TempestGear {

    private TempestGear() {}

    public static final String ID_PREFIX = "storm";
    public static final String RECIPE_PREFIX = "VX Storm";

    public static final List<GearLine.Form> FORMS = List.of(
            new GearLine.Form("Lash", Items.IRON_SWORD, GearLine.Kind.WEAPON, 65, 0, 0,
                    "It reaches further than it looks."),
            new GearLine.Form("Pike", Items.GOLDEN_SWORD, GearLine.Kind.WEAPON, 75, 0, 0,
                    "Keeps the argument at a comfortable distance."),
            new GearLine.Form("Cleaver", Items.IRON_AXE, GearLine.Kind.WEAPON, 95, 0, 0,
                    "Wide, and it carries the weather with it."),
            new GearLine.Form("Talons", Items.GOLDEN_AXE, GearLine.Kind.WEAPON, 80, 0, 0,
                    "Two of them, both in a hurry."),
            new GearLine.Form("Bolt", Items.BOW, GearLine.Kind.RANGED, 60, 0, 0,
                    "The charge travels with the shot."),

            new GearLine.Form("Splitter", Items.IRON_PICKAXE, GearLine.Kind.TOOL, 35, 0, 0,
                    "Stone has weather too, apparently."),
            new GearLine.Form("Furrow", Items.IRON_HOE, GearLine.Kind.TOOL, 30, 0, 0,
                    "It leaves a line the ground remembers."),
            new GearLine.Form("Drift", Items.IRON_SHOVEL, GearLine.Kind.TOOL, 30, 0, 0,
                    "Moves what the wind already loosened."),

            new GearLine.Form("Weathercap", Items.CHAINMAIL_HELMET, GearLine.Kind.HEAD, 0, 35, 25,
                    "Wide brimmed and thoroughly tested."),
            new GearLine.Form("Stormcoat", Items.CHAINMAIL_CHESTPLATE, GearLine.Kind.CHEST, 0, 55, 45,
                    "It has been outside longer than you have."),
            new GearLine.Form("Waders", Items.CHAINMAIL_LEGGINGS, GearLine.Kind.LEGS, 0, 45, 35,
                    "For weather that comes up to the knee."),
            new GearLine.Form("Gripsole", Items.CHAINMAIL_BOOTS, GearLine.Kind.FEET, 0, 30, 25,
                    "The ground stops being a suggestion."),

            new GearLine.Form("Vane", Items.COPPER_INGOT, GearLine.Kind.CHARM, 0, 20, 15,
                    "It points at the trouble before you notice."),
            new GearLine.Form("Barometer", Items.CLOCK, GearLine.Kind.CHARM, 0, 15, 20,
                    "Falling, which is never good news.")
    );

    public static Storm stormOf(ItemStack stack) {
        return Storm.byKey(GearLine.motifKeyOf(ID_PREFIX, stack));
    }

    public static GearLine.Form formOf(ItemStack stack) {
        String k = GearLine.formKeyOf(ID_PREFIX, stack);
        if (k == null) return null;
        for (GearLine.Form f : FORMS) {
            if (f.display().toLowerCase(Locale.ROOT).replace(' ', '_').equals(k)) return f;
        }
        return null;
    }

    public static ItemStack create(Storm s, GearLine.Form f) {
        return GearLine.create(ID_PREFIX, "Tempest", "Front", s.motif(), f, s.powerText());
    }

    public static void ensureRecipes() {
        List<GearLine.Motif> motifs = new ArrayList<>();
        for (Storm s : Storm.values()) motifs.add(s.motif());
        GearLine.register(ID_PREFIX, RECIPE_PREFIX, "Tempest", "Front",
                motifs, FORMS, Items.COPPER_INGOT, Items.PRISMARINE_SHARD, LineCatalyst.STORM_NUCLEUS,
                mo -> {
                    Storm s = Storm.byKey(mo.key());
                    return s == null ? null : s.powerText();
                });
    }

    public static List<ItemStack> allStacks() {
        List<ItemStack> out = new ArrayList<>();
        for (Storm s : Storm.values()) {
            for (GearLine.Form f : FORMS) out.add(create(s, f));
        }
        return out;
    }
}
