package com.political;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * V7 Pillar B — registers every craftable Cursed Arsenal piece (and the Cursed Talismans) into
 * {@code /recipe}, {@code /setrecipe} and the new-content catalog via {@link RecipeConfigManager}.
 *
 * <p>Progression, not a shopping list: a Grade 4 piece is built from cheap metal plus Cursed
 * Residue, and every grade above it eats the <b>same</b> piece one grade lower plus rarer cursed
 * reagents (Cores, Orbs, and finally a Special Grade Relic). Those reagents come only from
 * exorcising cursed spirits, so the ladder is gated behind the curse hunt, not the coin economy.</p>
 *
 * <p>Every recipe carries at least one cursed reagent or a unique signature ingredient, so no two
 * arsenal recipes share a grid and none collide with a vanilla crafting recipe.</p>
 */
public final class CursedArsenalRecipeBootstrap {

    private CursedArsenalRecipeBootstrap() {}

    public static void ensureRecipes() {
        List<SlayerRecipes.Recipe> out = new ArrayList<>();

        for (CursedArsenal.Weapon w : CursedArsenal.Weapon.values()) {
            Item sig = weaponSignature(w);
            ItemStack lower = null;
            for (CursedGrade g : CursedArsenal.GRADES) {
                ItemStack result = CursedArsenal.buildWeapon(w, g);
                ItemStack[] grid = grid(g, sig, lower);
                out.add(recipe("V7 " + w.display + " " + label(g), result, grid));
                lower = CursedArsenal.buildWeapon(w, g);
            }
        }

        for (CursedArsenal.Piece p : CursedArsenal.Piece.values()) {
            Item sig = pieceSignature(p);
            ItemStack lower = null;
            for (CursedGrade g : CursedArsenal.GRADES) {
                ItemStack result = CursedArsenal.buildArmor(p, g);
                ItemStack[] grid = grid(g, sig, lower);
                out.add(recipe("V7 Cursed " + p.noun + " " + label(g), result, grid));
                lower = CursedArsenal.buildArmor(p, g);
            }
        }

        for (CursedArsenal.Charm c : CursedArsenal.Charm.values()) {
            Item sig = charmSignature(c);
            ItemStack lower = null;
            for (CursedGrade g : CursedArsenal.GRADES) {
                ItemStack result = CursedArsenal.buildCharm(c, g);
                ItemStack[] grid = grid(g, sig, lower);
                out.add(recipe("V7 " + c.display + " " + label(g), result, grid));
                lower = CursedArsenal.buildCharm(c, g);
            }
        }

        addTalismans(out);

        RecipeConfigManager.upsertCustomRecipesSilent(out);
    }

    // ── Uniform arsenal grid ────────────────────────────────────
    private static ItemStack[] grid(CursedGrade g, Item signature, ItemStack lower) {
        ItemStack mat = new ItemStack(matFor(g));
        ItemStack center = lower != null ? lower.copy() : new ItemStack(signature);

        ItemStack[] gr = new ItemStack[9];
        gr[0] = mat.copy();       gr[1] = reagentA(g);   gr[2] = mat.copy();
        gr[3] = reagentB(g);      gr[4] = center;        gr[5] = reagentB(g);
        gr[6] = mat.copy();       gr[7] = reagentA(g);   gr[8] = mat.copy();
        return gr;
    }

    private static Item matFor(CursedGrade g) {
        return switch (g) {
            case GRADE_4 -> Items.IRON_INGOT;
            case GRADE_3 -> Items.DIAMOND;
            case GRADE_2 -> Items.NETHERITE_INGOT;
            case GRADE_1 -> Items.NETHERITE_INGOT;
            case SPECIAL_GRADE -> Items.NETHERITE_BLOCK;
        };
    }

    private static ItemStack reagentA(CursedGrade g) {
        return switch (g) {
            case GRADE_4, GRADE_3, GRADE_2 -> CursedItems.createResidue();
            default -> CursedItems.createCore();
        };
    }

    private static ItemStack reagentB(CursedGrade g) {
        return switch (g) {
            case GRADE_4, GRADE_3 -> CursedItems.createResidue();
            case GRADE_2 -> CursedItems.createCore();
            case GRADE_1 -> CursedItems.createOrb();
            case SPECIAL_GRADE -> CursedItems.createRelic();
        };
    }

    // ── Unique g4 signatures (make every family's base grid distinct) ──
    private static Item weaponSignature(CursedArsenal.Weapon w) {
        return switch (w) {
            case SWORD -> Items.IRON_SWORD;
            case SCYTHE -> Items.IRON_HOE;
            case KATANA -> Items.GOLDEN_SWORD;
            case GAUNTLET -> Items.COPPER_BLOCK;
        };
    }

    private static Item pieceSignature(CursedArsenal.Piece p) {
        return switch (p) {
            case HELMET -> Items.IRON_HELMET;
            case CHEST -> Items.IRON_CHESTPLATE;
            case LEGS -> Items.IRON_LEGGINGS;
            case BOOTS -> Items.IRON_BOOTS;
        };
    }

    private static Item charmSignature(CursedArsenal.Charm c) {
        return switch (c) {
            case MALEVOLENT -> Items.MAGMA_CREAM;
            case WARDING -> Items.NAUTILUS_SHELL;
        };
    }

    // ── Talismans ───────────────────────────────────────────────
    private static void addTalismans(List<SlayerRecipes.Recipe> out) {
        // Each grade uses a distinct reagent mix so no two talisman grids match.
        out.add(recipe("V7 Cursed Talisman (Grade 4)", MoreCursedSpirits.createTalisman(CursedGrade.GRADE_4),
                talismanGrid(Items.SOUL_SAND, new ItemStack(Items.BONE), CursedItems.createResidue())));
        out.add(recipe("V7 Cursed Talisman (Grade 3)", MoreCursedSpirits.createTalisman(CursedGrade.GRADE_3),
                talismanGrid(Items.BONE_BLOCK, CursedItems.createResidue(), new ItemStack(Items.SKELETON_SKULL))));
        out.add(recipe("V7 Cursed Talisman (Grade 2)", MoreCursedSpirits.createTalisman(CursedGrade.GRADE_2),
                talismanGrid(Items.SOUL_SOIL, CursedItems.createResidue(), new ItemStack(Items.WITHER_SKELETON_SKULL))));
        out.add(recipe("V7 Cursed Talisman (Grade 1)", MoreCursedSpirits.createTalisman(CursedGrade.GRADE_1),
                talismanGrid(Items.ANCIENT_DEBRIS, CursedItems.createCore(), CursedItems.createOrb())));
        out.add(recipe("V7 Cursed Talisman (Special Grade)", MoreCursedSpirits.createTalisman(CursedGrade.SPECIAL_GRADE),
                talismanGrid(Items.NETHERITE_INGOT, CursedItems.createCore(), CursedItems.createRelic())));
    }

    private static ItemStack[] talismanGrid(Item corner, ItemStack edge, ItemStack center) {
        ItemStack[] gr = new ItemStack[9];
        gr[0] = new ItemStack(corner); gr[1] = edge.copy();  gr[2] = new ItemStack(corner);
        gr[3] = edge.copy();           gr[4] = center;        gr[5] = edge.copy();
        gr[6] = new ItemStack(corner); gr[7] = edge.copy();  gr[8] = new ItemStack(corner);
        return gr;
    }

    // ── Shared ──────────────────────────────────────────────────
    private static String label(CursedGrade g) {
        return switch (g) {
            case GRADE_4 -> "(Grade 4)";
            case GRADE_3 -> "(Grade 3)";
            case GRADE_2 -> "(Grade 2)";
            case GRADE_1 -> "(Grade 1)";
            case SPECIAL_GRADE -> "(Special Grade)";
        };
    }

    private static SlayerRecipes.Recipe recipe(String name, ItemStack result, ItemStack[] grid) {
        boolean[] locked = new boolean[9];
        for (int i = 0; i < 9; i++) locked[i] = grid[i] != null && !grid[i].isEmpty();
        return new SlayerRecipes.Recipe(name, result, grid, locked, null, 0);
    }
}
