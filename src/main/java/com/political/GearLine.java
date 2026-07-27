package com.political;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Shared machinery for the large combinatorial gear lines.
 *
 * <p>Every line works the same way underneath. A <b>motif</b> carries the identity, the flavour and
 * the bonus, a <b>form</b> decides the slot and the base numbers, and the cross product is the line.
 * What makes each line feel different is its handler, so this class deliberately handles only the
 * item building and the recipe registration and leaves the behaviour alone.</p>
 */
public final class GearLine {

    private GearLine() {}

    public enum Kind { WEAPON, TOOL, HEAD, CHEST, LEGS, FEET, CHARM, RANGED }

    /** One cut of a line. The base item sets the slot, the numbers set the floor. */
    public record Form(String display, Item base, Kind kind,
                       int damage, int defense, int health, String blurb) {

        public boolean isWorn() {
            return kind == Kind.HEAD || kind == Kind.CHEST || kind == Kind.LEGS || kind == Kind.FEET;
        }
    }

    /**
     * One identity in a line.
     *
     * @param statLines  already formatted lore stat lines, may be empty
     * @param effectText a short sentence describing a non stat bonus, may be null
     */
    public record Motif(String key, String display, ChatFormatting colour, Item token, String flavor,
                        String group, ChatFormatting groupColour, String groupBonus,
                        List<String> statLines, String effectText) {}

    /** Builds one finished piece. */
    public static ItemStack create(String idPrefix, String lineLabel, String groupWord,
                                   Motif m, Form f, String powerLine) {
        ItemStack s = new ItemStack(f.base());
        s.set(DataComponents.CUSTOM_NAME,
                Component.literal(m.display() + " " + f.display()).withStyle(m.colour(), ChatFormatting.BOLD));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal("§8" + lineLabel + ", " + groupWord + " of the " + m.group()));
        lore.add(Component.literal("§7" + f.blurb()));
        lore.add(Component.literal("§8\"" + m.flavor() + "\""));
        lore.add(Component.literal(""));

        if (f.damage() > 0) lore.add(Component.literal("§7⚔ Damage: §c+" + f.damage()));
        if (f.defense() > 0) lore.add(Component.literal("§7❈ Defense: §a+" + f.defense()));
        if (f.health() > 0) lore.add(Component.literal("§7❤ Health: §c+" + f.health()));
        for (String line : m.statLines()) lore.add(Component.literal(line));

        lore.add(Component.literal(""));
        if (m.effectText() != null) lore.add(Component.literal("§7" + m.effectText()));
        if (powerLine != null) lore.add(Component.literal(powerLine));
        lore.add(Component.literal("§8Three of the " + m.group() + " " + groupWord.toLowerCase(Locale.ROOT)
                + ": " + m.groupColour() + m.groupBonus()));

        s.set(DataComponents.LORE, new ItemLore(lore));
        s.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        SlayerItems.setCustomItemId(s, idFor(idPrefix, m, f));
        return s;
    }

    /**
     * The form half of an id. Spaces are stripped rather than turned into underscores, because the
     * id is split on its last underscore and a form like "Sword of State" would otherwise eat most
     * of the motif key on the way back out.
     */
    public static String formKey(Form f) {
        return f.display().toLowerCase(Locale.ROOT).replace(" ", "");
    }

    public static String idFor(String idPrefix, Motif m, Form f) {
        return idPrefix + "_" + m.key() + "_" + formKey(f);
    }

    /** Pulls the motif key back out of a stack, or null if the stack is not from this line. */
    public static String motifKeyOf(String idPrefix, ItemStack stack) {
        String id = rawId(idPrefix, stack);
        if (id == null) return null;
        int last = id.lastIndexOf('_');
        int start = idPrefix.length() + 1;
        if (last <= start) return null;
        return id.substring(start, last);
    }

    /** Pulls the form key back out of a stack, or null if the stack is not from this line. */
    public static String formKeyOf(String idPrefix, ItemStack stack) {
        String id = rawId(idPrefix, stack);
        if (id == null) return null;
        int last = id.lastIndexOf('_');
        if (last < 0) return null;
        return id.substring(last + 1);
    }

    private static String rawId(String idPrefix, ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        String id = SlayerItems.getCustomItemId(stack);
        if (id == null) return null;
        id = SlayerItems.stripSkyblockEnchantSuffix(id);
        return id.startsWith(idPrefix + "_") ? id : null;
    }

    /** The six slots worth checking for a carried piece. */
    public static ItemStack[] carried(net.minecraft.server.level.ServerPlayer p) {
        return new ItemStack[]{
                p.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD),
                p.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST),
                p.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.LEGS),
                p.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.FEET),
                p.getMainHandItem(),
                p.getOffhandItem()
        };
    }

    /**
     * The crafting core a given cut deserves. Tools and charms are the cheap end of a line, weapons
     * and the light armour sit in the middle, and the chest and legs are the pieces worth a T3.
     */
    public static LineCatalyst.Tier tierFor(Form f) {
        return switch (f.kind()) {
            case TOOL, CHARM -> LineCatalyst.Tier.T1;
            case CHEST, LEGS -> LineCatalyst.Tier.T3;
            default -> LineCatalyst.Tier.T2;
        };
    }

    /**
     * Registers the whole cross product under one recipe name prefix.
     *
     * <p>The grid reads the same way in every line. The piece you are upgrading sits in the middle,
     * the motif's own token runs down both sides, the line catalyst holds the top corners and the
     * crafting core sits underneath. Anybody who has crafted one piece can craft any of them.</p>
     */
    public static void register(String idPrefix, String recipePrefix, String lineLabel, String groupWord,
                                List<Motif> motifs, List<Form> forms,
                                Item binder, Item secondary, LineCatalyst catalyst,
                                java.util.function.Function<Motif, String> power) {
        List<SlayerRecipes.Recipe> out = new ArrayList<>();
        for (Motif m : motifs) {
            for (Form f : forms) {
                LineCatalyst.Tier tier = tierFor(f);

                ItemStack[] grid = new ItemStack[9];
                grid[0] = catalyst.create(2);
                grid[1] = new ItemStack(m.token(), 8);
                grid[2] = catalyst.create(2);
                grid[3] = new ItemStack(binder, 12);
                grid[4] = new ItemStack(f.base());
                grid[5] = new ItemStack(binder, 12);
                grid[6] = new ItemStack(secondary, 6);
                grid[7] = tier.core();
                grid[8] = new ItemStack(secondary, 6);

                boolean[] locked = new boolean[9];
                for (int i = 0; i < 9; i++) locked[i] = true;

                out.add(new SlayerRecipes.Recipe(
                        recipePrefix + " " + m.display() + " " + f.display(),
                        create(idPrefix, lineLabel, groupWord, m, f, power.apply(m)),
                        grid, locked, null, 0));
            }
        }
        RecipeConfigManager.upsertCustomRecipesSilent(out);
    }

    /** Convenience for a stat line, kept here so every line formats identically. */
    public static String stat(String which, int amount) {
        return switch (which) {
            case "strength" -> "§7❁ Strength: §c+" + amount;
            case "defense" -> "§7❈ Defense: §a+" + amount;
            case "health" -> "§7❤ Health: §c+" + amount;
            case "intelligence" -> "§7✎ Intelligence: §b+" + amount;
            case "crit_chance" -> "§7☣ Crit Chance: §9+" + amount + "%";
            case "crit_damage" -> "§7☠ Crit Damage: §9+" + amount + "%";
            case "ferocity" -> "§7⫽ Ferocity: §c+" + amount;
            case "speed" -> "§7✦ Speed: §f+" + amount;
            default -> "§7⚔ Damage: §c+" + amount;
        };
    }

    /** A standard fourteen cut form list, reused by lines that do not need something special. */
    public static List<Form> standardForms(String[] names, Item[] bases) {
        List<Form> out = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            out.add(new Form(names[i], bases[i], Kind.WEAPON, 60, 0, 0, ""));
        }
        return out;
    }

    static final Item DEFAULT_BINDER = Items.GOLD_INGOT;
}
