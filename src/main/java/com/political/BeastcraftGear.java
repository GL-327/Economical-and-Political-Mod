package com.political;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * Beastcraft: forty five beasts cut into fourteen forms, so six hundred and thirty pieces.
 *
 * <p>The forms here are deliberately rougher than the Starbound cuts. Beastcraft is bone, hide and
 * whatever the thing was wearing, so a Beastcraft chestpiece is a hide, a Beastcraft blade is a fang
 * and the charm is a trophy. Every piece feeds the same Instinct meter, which means mixing beasts is
 * a real choice rather than a penalty.</p>
 */
public final class BeastcraftGear {

    private BeastcraftGear() {}

    public static final String ID_PREFIX = "beast";
    public static final String RECIPE_PREFIX = "VX Beast";

    public static final List<GearLine.Form> FORMS = List.of(
            new GearLine.Form("Fang", Items.IRON_SWORD, GearLine.Kind.WEAPON, 75, 0, 0,
                    "Taken from the front of the mouth."),
            new GearLine.Form("Claw", Items.IRON_AXE, GearLine.Kind.WEAPON, 90, 0, 0,
                    "Still curved the way it was used."),
            new GearLine.Form("Tusk", Items.STONE_AXE, GearLine.Kind.WEAPON, 110, 0, 0,
                    "Heavy, and it wants to keep swinging."),
            new GearLine.Form("Barb", Items.IRON_SHOVEL, GearLine.Kind.WEAPON, 65, 0, 0,
                    "Small enough to get in, shaped to stay."),
            new GearLine.Form("Spur", Items.GOLDEN_SWORD, GearLine.Kind.WEAPON, 70, 0, 0,
                    "Quick work, close in."),

            new GearLine.Form("Talon", Items.IRON_PICKAXE, GearLine.Kind.TOOL, 35, 0, 0,
                    "It was digging long before you were."),
            new GearLine.Form("Rake", Items.IRON_HOE, GearLine.Kind.TOOL, 30, 0, 0,
                    "Four lines at once, every time."),
            new GearLine.Form("Digger", Items.GOLDEN_SHOVEL, GearLine.Kind.TOOL, 25, 0, 0,
                    "Made for burrows, works on anything."),

            new GearLine.Form("Skull", Items.LEATHER_HELMET, GearLine.Kind.HEAD, 0, 35, 30,
                    "Worn the way it was worn before."),
            new GearLine.Form("Hide", Items.LEATHER_CHESTPLATE, GearLine.Kind.CHEST, 0, 60, 55,
                    "Thicker than it looks and it looks thick."),
            new GearLine.Form("Ribguard", Items.LEATHER_LEGGINGS, GearLine.Kind.LEGS, 0, 45, 40,
                    "Bone where bone was already."),
            new GearLine.Form("Padfoot", Items.LEATHER_BOOTS, GearLine.Kind.FEET, 0, 30, 25,
                    "You stop making noise when you walk."),

            new GearLine.Form("Trophy", Items.ZOMBIE_HEAD, GearLine.Kind.CHARM, 0, 20, 20,
                    "Carried where everyone can see it."),
            new GearLine.Form("Totem", Items.BONE_BLOCK, GearLine.Kind.CHARM, 0, 25, 25,
                    "Not a decoration. An arrangement.")
    );

    public static Beast beastOf(ItemStack stack) {
        return Beast.byKey(GearLine.motifKeyOf(ID_PREFIX, stack));
    }

    public static GearLine.Form formOf(ItemStack stack) {
        String k = GearLine.formKeyOf(ID_PREFIX, stack);
        if (k == null) return null;
        for (GearLine.Form f : FORMS) {
            if (f.display().equalsIgnoreCase(k)) return f;
        }
        return null;
    }

    public static ItemStack create(Beast b, GearLine.Form f) {
        return GearLine.create(ID_PREFIX, "Beastcraft", "Clade", b.motif(), f,
                "§6" + b.powerText);
    }

    public static void ensureRecipes() {
        List<GearLine.Motif> motifs = new ArrayList<>();
        for (Beast b : Beast.values()) motifs.add(b.motif());
        GearLine.register(ID_PREFIX, RECIPE_PREFIX, "Beastcraft", "Clade",
                motifs, FORMS, Items.BONE, Items.LEATHER, LineCatalyst.PRIMAL_HEART,
                m -> {
                    Beast b = Beast.byKey(m.key());
                    return b == null ? null : "§6" + b.powerText;
                });
    }

    public static List<ItemStack> allStacks() {
        List<ItemStack> out = new ArrayList<>();
        for (Beast b : Beast.values()) {
            for (GearLine.Form f : FORMS) out.add(create(b, f));
        }
        return out;
    }
}
