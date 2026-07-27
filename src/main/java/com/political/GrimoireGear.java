package com.political;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Grimoire: forty five sigils cut into fourteen forms, so six hundred and thirty pieces.
 *
 * <p>The forms here are implements rather than weapons. A Grimoire piece is a staff, a focus or a
 * page, and the numbers lean on Intelligence because Focus refills from it. Wearing the vestments
 * pushes the pool higher, so a full Grimoire loadout casts more often than a single held sigil.</p>
 */
public final class GrimoireGear {

    private GrimoireGear() {}

    public static final String ID_PREFIX = "sigil";
    public static final String RECIPE_PREFIX = "VX Sigil";

    public static final List<GearLine.Form> FORMS = List.of(
            new GearLine.Form("Rod", Items.BLAZE_ROD, GearLine.Kind.WEAPON, 55, 0, 0,
                    "Short, and it points where you point."),
            new GearLine.Form("Stave", Items.STICK, GearLine.Kind.WEAPON, 45, 0, 0,
                    "Long enough to lean on between castings."),
            new GearLine.Form("Athame", Items.GOLDEN_SWORD, GearLine.Kind.WEAPON, 70, 0, 0,
                    "It cuts things that are not strictly there."),
            new GearLine.Form("Censer", Items.LANTERN, GearLine.Kind.WEAPON, 50, 0, 0,
                    "The smoke does most of the work."),
            new GearLine.Form("Codex", Items.ENCHANTED_BOOK, GearLine.Kind.WEAPON, 40, 0, 0,
                    "Heavy, and it argues back."),

            new GearLine.Form("Chisel", Items.GOLDEN_PICKAXE, GearLine.Kind.TOOL, 30, 0, 0,
                    "For cutting the mark into something that will hold it."),
            new GearLine.Form("Scriber", Items.GOLDEN_HOE, GearLine.Kind.TOOL, 25, 0, 0,
                    "Fine work, done slowly, on purpose."),

            new GearLine.Form("Hood", Items.LEATHER_HELMET, GearLine.Kind.HEAD, 0, 30, 25,
                    "Drawn up when the work starts."),
            new GearLine.Form("Vestment", Items.LEATHER_CHESTPLATE, GearLine.Kind.CHEST, 0, 50, 45,
                    "Older than whoever is wearing it."),
            new GearLine.Form("Skirting", Items.LEATHER_LEGGINGS, GearLine.Kind.LEGS, 0, 40, 35,
                    "Long, and it drags through the chalk."),
            new GearLine.Form("Slippers", Items.LEATHER_BOOTS, GearLine.Kind.FEET, 0, 25, 20,
                    "You are not supposed to scuff the circle."),

            new GearLine.Form("Focus", Items.AMETHYST_SHARD, GearLine.Kind.CHARM, 0, 15, 15,
                    "Held in the off hand and never put down."),
            new GearLine.Form("Phylactery", Items.GLASS_BOTTLE, GearLine.Kind.CHARM, 0, 20, 25,
                    "Something is kept in here."),
            new GearLine.Form("Page", Items.PAPER, GearLine.Kind.CHARM, 0, 10, 10,
                    "One casting, written out, folded twice.")
    );

    public static Sigil sigilOf(ItemStack stack) {
        return Sigil.byKey(GearLine.motifKeyOf(ID_PREFIX, stack));
    }

    public static GearLine.Form formOf(ItemStack stack) {
        String k = GearLine.formKeyOf(ID_PREFIX, stack);
        if (k == null) return null;
        for (GearLine.Form f : FORMS) {
            if (f.display().toLowerCase(Locale.ROOT).replace(' ', '_').equals(k)) return f;
        }
        return null;
    }

    public static ItemStack create(Sigil s, GearLine.Form f) {
        return GearLine.create(ID_PREFIX, "Grimoire", "Circle", s.motif(), f, "§d" + s.powerText);
    }

    public static void ensureRecipes() {
        List<GearLine.Motif> motifs = new ArrayList<>();
        for (Sigil s : Sigil.values()) motifs.add(s.motif());
        GearLine.register(ID_PREFIX, RECIPE_PREFIX, "Grimoire", "Circle",
                motifs, FORMS, Items.LAPIS_LAZULI, Items.GLOWSTONE_DUST, LineCatalyst.BOUND_PAGE,
                mo -> {
                    Sigil s = Sigil.byKey(mo.key());
                    return s == null ? null : "§d" + s.powerText;
                });
    }

    public static List<ItemStack> allStacks() {
        List<ItemStack> out = new ArrayList<>();
        for (Sigil s : Sigil.values()) {
            for (GearLine.Form f : FORMS) out.add(create(s, f));
        }
        return out;
    }
}
