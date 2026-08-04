package com.political;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;

/**
 * Turns the delve catalogues into real stacks, and puts them all in the recipe book.
 *
 * <p>One place that knows how a delve item is dressed, so armour, weapons and spoils all read the
 * same way and a change to the house style is one edit rather than three. Everything registered
 * here reaches {@code /recipe}, {@code /setrecipe} and {@code /customitems} like the rest of the
 * mod, because an item that cannot be looked up may as well not exist.</p>
 *
 * <p>All twelve hundred are craftable. The spoils are cheap and mostly exist to be spent, the gear
 * is expensive and mostly exists to be earned, and the recipes reflect that: a spoil costs the
 * spoil below it, and a piece of armour costs spoils from the depth it belongs to.</p>
 */
public final class DelveItems {

    private DelveItems() {}

    /** Marks every delve item, so the forge and the drop tables can recognise one cheaply. */
    public static final String PREFIX = "delve_";

    // ── Dressing ──────────────────────────────────────────────────────────

    /**
     * The house style for a delve item.
     *
     * <p>Kept in one method because a thousand items dressed slightly differently look like a
     * thousand mistakes, and because the stat lines have to match the exact symbols the stat parser
     * reads or the numbers on the tooltip are decoration.</p>
     */
    private static ItemStack dress(net.minecraft.world.item.Item base, String id, String name,
                                   ChatFormatting colour, String flavour,
                                   List<String> statLines, String rarity, String... extra) {
        ItemStack s = new ItemStack(base);
        s.set(DataComponents.CUSTOM_NAME,
                Component.literal(name).withStyle(colour, ChatFormatting.BOLD));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal("§8" + flavour));
        if (!statLines.isEmpty()) {
            lore.add(Component.literal(""));
            for (String line : statLines) lore.add(Component.literal(line));
        }
        for (String e : extra) {
            lore.add(Component.literal(e));
        }
        lore.add(Component.literal(""));
        lore.add(Component.literal(rarity));
        s.set(DataComponents.LORE, new ItemLore(lore));
        s.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);

        // Last, always. Everything above is overwritten by the lore summariser this calls.
        SlayerItems.setCustomItemId(s, id);
        return s;
    }

    private static String rarityLine(int tier) {
        return switch (Math.max(1, Math.min(7, tier))) {
            case 1 -> "§f§lCOMMON";
            case 2 -> "§a§lUNCOMMON";
            case 3 -> "§9§lRARE";
            case 4 -> "§5§lEPIC";
            case 5 -> "§6§lLEGENDARY";
            case 6 -> "§d§lMYTHIC";
            default -> "§4§lPRIMAL";
        };
    }

    // ── Armour ────────────────────────────────────────────────────────────

    /**
     * One piece of delve armour.
     *
     * <p>The numbers are the family's lean, multiplied by the grade and then by the piece's share,
     * so a chestpiece is always the biggest part of a set and a Hallowed one is always worth more
     * than a Proven one. Rounded to fives, which is the mod's convention everywhere else.</p>
     */
    public static ItemStack armour(DelveArmour.Family f, DelveArmour.Grade g, DelveArmour.Piece p) {
        List<String> stats = new ArrayList<>();
        for (int i = 0; i < f.stats().size(); i++) {
            int amount = round5((int) Math.round(f.amounts().get(i) * g.scale() * p.share()));
            stats.add(GearLine.stat(f.stats().get(i), amount));
        }
        int grade = DelveArmour.GRADES.indexOf(g) + 1;
        return dress(DelveArmour.baseItem(f, p),
                DelveArmour.idOf(f, g, p),
                DelveArmour.nameOf(f, g, p),
                g.colour(),
                f.flavour(),
                stats,
                rarityLine(grade + 1),
                "",
                "§5Delve gear. Stronger underground.",
                "§8Delveforge it to mark it up.");
    }

    // ── Weapons and tools ─────────────────────────────────────────────────

    public static ItemStack gear(DelveGear.Line l, DelveGear.Form f) {
        List<String> stats = new ArrayList<>();
        stats.add(GearLine.stat(l.stat(), round5((int) Math.round(l.amount() * f.scale()))));
        // A weapon that only did one thing would be a stat stick, so everything gets a second,
        // smaller line that suits its cut.
        String second = switch (f.kind()) {
            case "TOOL" -> "speed";
            case "BOW" -> "crit_chance";
            default -> "ferocity";
        };
        stats.add(GearLine.stat(second, round5((int) Math.round(
                (second.equals("speed") ? 4 : second.equals("ferocity") ? 8 : 10) * f.scale()))));

        return dress(f.base(), DelveGear.idOf(l, f), DelveGear.nameOf(l, f), l.colour(),
                l.flavour(), stats, rarityLine(5),
                "",
                "§5Delve gear. Stronger underground.",
                "§8Delveforge it to mark it up.");
    }

    // ── Spoils ────────────────────────────────────────────────────────────

    public static ItemStack spoil(DelveSpoils.Kind k, DelveSpoils.Depth d, DelveSpoils.Variant v) {
        return dress(k.base(), DelveSpoils.idOf(k, d, v), DelveSpoils.nameOf(k, d, v),
                d.colour(), k.flavour(), List.of(), rarityLine(d.tier()),
                "",
                "§7Worth §6" + GuiKit.num(DelveSpoils.valueOf(k, d, v)) + " §7to a collector",
                "§8Spent on delveforging and marks.");
    }

    private static int round5(int n) {
        // The mod's rule: multiples of five above ten, anything below ten as it lies.
        return n < 10 ? Math.max(1, n) : Math.round(n / 5f) * 5;
    }

    // ── Recipes ───────────────────────────────────────────────────────────

    /**
     * Registers every delve item.
     *
     * <p>A spoil is made from three of the grade below it, so the ladder is a real one and the
     * thousand items are a chain rather than a thousand dead ends. Gear costs spoils from its own
     * depth plus the crafting core the rest of the mod uses, so delve gear sits inside the existing
     * economy instead of beside it.</p>
     */
    public static void ensureRecipes() {
        List<SlayerRecipes.Recipe> out = new ArrayList<>();

        // ── Spoils: each grade is three of the one below, so the pile means something. ──
        for (DelveSpoils.Kind k : DelveSpoils.KINDS) {
            for (DelveSpoils.Depth d : DelveSpoils.DEPTHS) {
                for (int i = 1; i < DelveSpoils.VARIANTS.size(); i++) {
                    DelveSpoils.Variant lower = DelveSpoils.VARIANTS.get(i - 1);
                    DelveSpoils.Variant v = DelveSpoils.VARIANTS.get(i);
                    ItemStack[] grid = new ItemStack[9];
                    ItemStack in = spoil(k, d, lower);
                    for (int slot : new int[]{0, 1, 3}) {
                        ItemStack one = in.copy();
                        one.setCount(3);
                        grid[slot] = one;
                    }
                    boolean[] locked = new boolean[9];
                    java.util.Arrays.fill(locked, true);
                    out.add(new SlayerRecipes.Recipe(
                            "Delve " + DelveSpoils.nameOf(k, d, v),
                            spoil(k, d, v), grid, locked, null, 0));
                }
            }
        }

        // ── Armour: spoils from its depth, plus the tier three core. ──
        for (DelveArmour.Family f : DelveArmour.FAMILIES) {
            for (int gi = 0; gi < DelveArmour.GRADES.size(); gi++) {
                DelveArmour.Grade g = DelveArmour.GRADES.get(gi);
                // A family is tied to a depth so its recipe costs that depth's spoils, which is
                // what makes a set feel like it came from somewhere.
                DelveSpoils.Depth depth = DelveSpoils.DEPTHS.get(
                        Math.min(DelveSpoils.DEPTHS.size() - 1,
                                DelveArmour.FAMILIES.indexOf(f)));
                DelveSpoils.Variant tier = DelveSpoils.VARIANTS.get(
                        Math.min(DelveSpoils.VARIANTS.size() - 1, 4 + gi * 2));

                for (DelveArmour.Piece p : DelveArmour.PIECES) {
                    ItemStack[] grid = new ItemStack[9];
                    ItemStack essence = spoil(DelveSpoils.KINDS.get(0), depth, tier);
                    ItemStack shard = spoil(DelveSpoils.KINDS.get(1), depth, tier);
                    essence.setCount(8);
                    shard.setCount(8);
                    grid[0] = essence.copy();
                    grid[2] = essence.copy();
                    grid[3] = shard.copy();
                    grid[5] = shard.copy();
                    grid[4] = new ItemStack(DelveArmour.baseItem(f, p));
                    grid[7] = SlayerItems.createCustomCrafterCoreT3();
                    boolean[] locked = new boolean[9];
                    java.util.Arrays.fill(locked, true);
                    out.add(new SlayerRecipes.Recipe(
                            "Delve " + DelveArmour.nameOf(f, g, p),
                            armour(f, g, p), grid, locked, null, 0));
                }
            }
        }

        // ── Weapons and tools. ──
        for (DelveGear.Line l : DelveGear.LINES) {
            DelveSpoils.Depth depth = DelveSpoils.DEPTHS.get(
                    Math.min(DelveSpoils.DEPTHS.size() - 1, DelveGear.LINES.indexOf(l)));
            for (DelveGear.Form f : DelveGear.FORMS) {
                ItemStack[] grid = new ItemStack[9];
                ItemStack core = spoil(DelveSpoils.KINDS.get(6), depth,
                        DelveSpoils.VARIANTS.get(9));
                ItemStack ember = spoil(DelveSpoils.KINDS.get(7), depth,
                        DelveSpoils.VARIANTS.get(7));
                core.setCount(6);
                ember.setCount(10);
                grid[1] = core.copy();
                grid[3] = ember.copy();
                grid[5] = ember.copy();
                grid[4] = new ItemStack(f.base());
                grid[7] = SlayerItems.createCustomCrafterCoreT3();
                boolean[] locked = new boolean[9];
                java.util.Arrays.fill(locked, true);
                out.add(new SlayerRecipes.Recipe(
                        "Delve " + DelveGear.nameOf(l, f),
                        gear(l, f), grid, locked, null, 0));
            }
        }

        RecipeConfigManager.upsertCustomRecipesSilent(out);
        PoliticalServer.LOGGER.info("Delve items: " + out.size() + " recipes ("
                + DelveArmour.total() + " armour, " + DelveGear.total() + " gear, "
                + DelveSpoils.total() + " spoils)");
    }

    /** Total item count, for the menu. */
    public static int total() {
        return DelveArmour.total() + DelveGear.total() + DelveSpoils.total();
    }
}
