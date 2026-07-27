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
 * Starbound gear: thirty six constellations cut into twelve forms, so four hundred and thirty two
 * pieces.
 *
 * <p>This line reads differently from the Runeforge. A rune waits for an event, a constellation is
 * simply always on, so Starbound gear is what you wear when you want a standing shape to your
 * character rather than a proc. The <b>form</b> sets the base numbers and the slot, the
 * <b>constellation</b> adds its boon on top, and three pieces of one house light that house's
 * standing bonus.</p>
 */
public final class StarboundGear {

    private StarboundGear() {}

    /** The twelve forms a constellation can be cut into. */
    public enum Form {
        SABRE("Sabre", Items.GOLDEN_SWORD, Kind.WEAPON, 70, 0, 0, "A duelling edge, light in the hand."),
        GLAIVE("Glaive", Items.GOLDEN_AXE, Kind.WEAPON, 95, 0, 0, "Reach and weight together."),
        RAPIER("Rapier", Items.IRON_SWORD, Kind.WEAPON, 60, 0, 0, "Precision over force."),
        MAUL("Maul", Items.STONE_AXE, Kind.WEAPON, 115, 0, 0, "Force over everything."),

        DRILL("Drill", Items.GOLDEN_PICKAXE, Kind.TOOL, 30, 0, 0, "Stone gives up quickly."),
        SPADE("Spade", Items.GOLDEN_SHOVEL, Kind.TOOL, 25, 0, 0, "Earth moves without complaint."),
        SICKLE("Sickle", Items.GOLDEN_HOE, Kind.TOOL, 25, 0, 0, "Kind to fields, unkind to weeds."),

        CIRCLET("Circlet", Items.GOLDEN_HELMET, Kind.HEAD, 0, 30, 25, "Worn where thought happens."),
        AEGIS("Aegis", Items.GOLDEN_CHESTPLATE, Kind.CHEST, 0, 55, 45, "The centre of the constellation."),
        GREAVES("Greaves", Items.GOLDEN_LEGGINGS, Kind.LEGS, 0, 40, 35, "Guarded where it counts."),
        STRIDERS("Striders", Items.GOLDEN_BOOTS, Kind.FEET, 0, 25, 20, "The ground is friendlier."),
        SIGIL("Sigil", Items.NETHER_STAR, Kind.CHARM, 0, 15, 15, "Carried, and quietly loud.");

        public enum Kind { WEAPON, TOOL, HEAD, CHEST, LEGS, FEET, CHARM }

        public final String display;
        public final Item base;
        public final Kind kind;
        public final int damage;
        public final int defense;
        public final int health;
        public final String blurb;

        Form(String display, Item base, Kind kind, int damage, int defense, int health, String blurb) {
            this.display = display;
            this.base = base;
            this.kind = kind;
            this.damage = damage;
            this.defense = defense;
            this.health = health;
            this.blurb = blurb;
        }

        public boolean isWorn() {
            return kind == Kind.HEAD || kind == Kind.CHEST || kind == Kind.LEGS || kind == Kind.FEET;
        }
    }

    /** Same tier rule the shared lines use, expressed against this line's own cuts. */
    private static LineCatalyst.Tier tierFor(Form f) {
        return switch (f.kind) {
            case TOOL, CHARM -> LineCatalyst.Tier.T1;
            case CHEST, LEGS -> LineCatalyst.Tier.T3;
            default -> LineCatalyst.Tier.T2;
        };
    }

    public static String idFor(Constellation c, Form f) {
        return "star_" + c.key() + "_" + f.name().toLowerCase(Locale.ROOT);
    }

    public static Constellation constellationOf(ItemStack stack) {
        String id = SlayerItems.getCustomItemId(stack);
        if (id == null) return null;
        id = SlayerItems.stripSkyblockEnchantSuffix(id);
        if (!id.startsWith("star_")) return null;
        int last = id.lastIndexOf('_');
        if (last <= 5) return null;
        return Constellation.byKey(id.substring(5, last));
    }

    public static Form formOf(ItemStack stack) {
        String id = SlayerItems.getCustomItemId(stack);
        if (id == null) return null;
        id = SlayerItems.stripSkyblockEnchantSuffix(id);
        if (!id.startsWith("star_")) return null;
        int last = id.lastIndexOf('_');
        if (last < 0) return null;
        String tail = id.substring(last + 1);
        for (Form f : Form.values()) {
            if (f.name().equalsIgnoreCase(tail)) return f;
        }
        return null;
    }

    public static ItemStack create(Constellation c, Form f) {
        ItemStack s = new ItemStack(f.base);
        s.set(DataComponents.CUSTOM_NAME,
                Component.literal(c.display + " " + f.display).withStyle(c.colour, ChatFormatting.BOLD));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal("§8Starbound, House of the " + c.house.label));
        lore.add(Component.literal("§7" + f.blurb));
        lore.add(Component.literal("§8\"" + c.flavor + "\""));
        lore.add(Component.literal(""));

        if (f.damage > 0) lore.add(Component.literal("§7⚔ Damage: §c+" + f.damage));
        if (f.defense > 0) lore.add(Component.literal("§7❈ Defense: §a+" + f.defense));
        if (f.health > 0) lore.add(Component.literal("§7❤ Health: §c+" + f.health));

        // The constellation's own boon, either a stat line or a described effect.
        String stat = c.statLine();
        if (stat != null) {
            lore.add(Component.literal(stat));
        }
        String eff = c.effectText();
        if (eff != null) {
            lore.add(Component.literal("§7" + eff));
        }

        lore.add(Component.literal(""));
        lore.add(Component.literal("§7Constellation: " + c.colour + c.display));
        lore.add(Component.literal("§8Always on while carried."));
        lore.add(Component.literal("§8Three of the " + c.house.label + " house: "
                + c.house.colour + c.house.bonusText));

        s.set(DataComponents.LORE, new ItemLore(lore));
        s.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        SlayerItems.setCustomItemId(s, idFor(c, f));
        return s;
    }

    public static void ensureRecipes() {
        List<SlayerRecipes.Recipe> out = new ArrayList<>();
        for (Constellation c : Constellation.values()) {
            for (Form f : Form.values()) {
                LineCatalyst.Tier tier = tierFor(f);

                ItemStack[] grid = new ItemStack[9];
                grid[0] = LineCatalyst.STAR_SHARD.create(2);
                grid[1] = new ItemStack(c.token, 8);
                grid[2] = LineCatalyst.STAR_SHARD.create(2);
                grid[3] = new ItemStack(Items.GOLD_INGOT, 12);
                grid[4] = new ItemStack(f.base);
                grid[5] = new ItemStack(Items.GOLD_INGOT, 12);
                grid[6] = new ItemStack(Items.AMETHYST_SHARD, 6);
                grid[7] = tier.core();
                grid[8] = new ItemStack(Items.AMETHYST_SHARD, 6);

                boolean[] locked = new boolean[9];
                for (int i = 0; i < 9; i++) locked[i] = true;

                out.add(new SlayerRecipes.Recipe(
                        "VX Star " + c.display + " " + f.display,
                        create(c, f), grid, locked, null, 0));
            }
        }
        RecipeConfigManager.upsertCustomRecipesSilent(out);
    }

    public static List<ItemStack> allStacks() {
        List<ItemStack> out = new ArrayList<>();
        for (Constellation c : Constellation.values()) {
            for (Form f : Form.values()) out.add(create(c, f));
        }
        return out;
    }
}
