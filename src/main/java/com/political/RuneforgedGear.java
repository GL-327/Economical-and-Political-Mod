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
 * The Runeforge: forty runes struck into fifteen pieces, so six hundred items.
 *
 * <p>Both axes matter. The <b>piece</b> decides the shape of the fight, its stat spread and when it
 * fires. The <b>rune</b> decides what happens when it does. A Pyre Dagger and a Pyre Warhammer burn
 * the same way but play nothing alike, and a Pyre Dagger and a Deepfreeze Dagger swing the same way
 * and do opposite things.</p>
 */
public final class RuneforgedGear {

    private RuneforgedGear() {}

    /** The fifteen shapes a rune can be struck into. */
    public enum Piece {
        BLADE("Blade", Items.IRON_SWORD, Kind.WEAPON, 80, 40, 0, 0, "Quick and even handed."),
        CLEAVER("Cleaver", Items.IRON_AXE, Kind.WEAPON, 110, 55, 0, 0, "Heavy, and it commits."),
        DAGGER("Dagger", Items.IRON_SWORD, Kind.WEAPON, 55, 30, 0, 0, "Fast, close, and often first."),
        SPEAR("Spear", Items.TRIDENT, Kind.WEAPON, 95, 45, 0, 0, "Keeps the fight at arm's length."),
        WARHAMMER("Warhammer", Items.IRON_AXE, Kind.WEAPON, 130, 65, 10, 0, "Slow enough to be certain."),
        SCYTHE("Scythe", Items.IRON_HOE, Kind.WEAPON, 100, 50, 0, 0, "Wide arcs, several answers."),

        PICKAXE("Pickaxe", Items.IRON_PICKAXE, Kind.TOOL, 35, 20, 0, 0, "Stone first, everything else after."),
        SHOVEL("Shovel", Items.IRON_SHOVEL, Kind.TOOL, 30, 15, 0, 0, "Earth moves quickly."),
        HOE("Hoe", Items.IRON_HOE, Kind.TOOL, 25, 15, 0, 0, "The field answers to it."),
        HATCHET("Hatchet", Items.IRON_AXE, Kind.TOOL, 45, 25, 0, 0, "Timber, and trouble if needed."),

        HELM("Helm", Items.IRON_HELMET, Kind.HEAD, 0, 10, 35, 25, "Where the rune sits closest to thought."),
        HAUBERK("Hauberk", Items.IRON_CHESTPLATE, Kind.CHEST, 0, 20, 60, 45, "The weight of the whole rune."),
        FAULDS("Faulds", Items.IRON_LEGGINGS, Kind.LEGS, 0, 15, 45, 35, "Guarded where guarding counts."),
        SABATONS("Sabatons", Items.IRON_BOOTS, Kind.FEET, 0, 10, 30, 20, "The ground gives a little."),
        TALISMAN("Talisman", Items.AMETHYST_SHARD, Kind.CHARM, 0, 25, 15, 15, "Carried, not worn.");

        public enum Kind { WEAPON, TOOL, HEAD, CHEST, LEGS, FEET, CHARM }

        public final String display;
        public final Item base;
        public final Kind kind;
        public final int damage;
        public final int strength;
        public final int defense;
        public final int health;
        public final String blurb;

        Piece(String display, Item base, Kind kind, int damage, int strength,
              int defense, int health, String blurb) {
            this.display = display;
            this.base = base;
            this.kind = kind;
            this.damage = damage;
            this.strength = strength;
            this.defense = defense;
            this.health = health;
            this.blurb = blurb;
        }

        /** When this shape fires its rune. */
        public String triggerText() {
            return switch (kind) {
                case WEAPON -> "Fires when you land a strike.";
                case TOOL -> "Fires as you break blocks.";
                case CHARM -> "Fires when you are badly hurt.";
                default -> "Fires while worn, on a slow rhythm.";
            };
        }
    }

    public static String idFor(Rune r, Piece p) {
        return "rune_" + r.key() + "_" + p.name().toLowerCase(Locale.ROOT);
    }

    /** Reads the rune off a Runeforged item, or null. */
    public static Rune runeOf(ItemStack stack) {
        String id = SlayerItems.getCustomItemId(stack);
        if (id == null) return null;
        id = SlayerItems.stripSkyblockEnchantSuffix(id);
        if (!id.startsWith("rune_")) return null;
        // id shape is rune_<rune>_<piece>; the piece is the last segment.
        int last = id.lastIndexOf('_');
        if (last <= 5) return null;
        return Rune.byKey(id.substring(5, last));
    }

    /** Reads the piece off a Runeforged item, or null. */
    public static Piece pieceOf(ItemStack stack) {
        String id = SlayerItems.getCustomItemId(stack);
        if (id == null) return null;
        id = SlayerItems.stripSkyblockEnchantSuffix(id);
        if (!id.startsWith("rune_")) return null;
        int last = id.lastIndexOf('_');
        if (last < 0) return null;
        String tail = id.substring(last + 1);
        for (Piece p : Piece.values()) {
            if (p.name().equalsIgnoreCase(tail)) return p;
        }
        return null;
    }

    /** Builds one Runeforged item. */
    public static ItemStack create(Rune r, Piece p) {
        ItemStack s = new ItemStack(p.base);
        s.set(DataComponents.CUSTOM_NAME,
                Component.literal(r.display + " " + p.display).withStyle(r.colour, ChatFormatting.BOLD));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal("§8Runeforged, " + r.display + " rune"));
        lore.add(Component.literal("§7" + p.blurb));
        lore.add(Component.literal("§8\"" + r.flavor + "\""));
        lore.add(Component.literal(""));

        if (p.damage > 0) lore.add(Component.literal("§7⚔ Damage: §c+" + p.damage));
        if (p.strength > 0) lore.add(Component.literal("§7❁ Strength: §c+" + p.strength));
        if (p.defense > 0) lore.add(Component.literal("§7❈ Defense: §a+" + p.defense));
        if (p.health > 0) lore.add(Component.literal("§7❤ Health: §c+" + p.health));
        if (p.kind == Piece.Kind.WEAPON) lore.add(Component.literal("§7☠ Crit Damage: §9+20%"));
        if (p.kind == Piece.Kind.CHARM) lore.add(Component.literal("§7✎ Intelligence: §b+40"));

        lore.add(Component.literal(""));
        lore.add(Component.literal("§7Rune: " + r.colour + r.display));
        lore.add(Component.literal("§8" + r.describe()));
        lore.add(Component.literal("§8" + p.triggerText()));

        s.set(DataComponents.LORE, new ItemLore(lore));
        s.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        SlayerItems.setCustomItemId(s, idFor(r, p));
        return s;
    }

    /** The material that keys a rune in its recipe. */
    private static Item reagentFor(Rune r) {
        return switch (r.effect) {
            case IGNITE -> Items.BLAZE_POWDER;
            case CHILL -> Items.BLUE_ICE;
            case SHOCK -> Items.LIGHTNING_ROD;
            case POISON -> Items.SPIDER_EYE;
            case WITHER -> Items.WITHER_SKELETON_SKULL;
            case LEECH -> Items.GHAST_TEAR;
            case REND -> Items.NETHERITE_SCRAP;
            case STAGGER -> Items.SLIME_BALL;
            case BLIND -> Items.INK_SAC;
            case LEVITATE -> Items.SHULKER_SHELL;
            case PULL -> Items.ENDER_PEARL;
            case PUSH -> Items.PISTON;
            case HASTE_SELF -> Items.SUGAR;
            case SHIELD_SELF -> Items.SHIELD;
            case HEAL_SELF -> Items.GLISTERING_MELON_SLICE;
            case SPEED_SELF -> Items.RABBIT_FOOT;
            case ENERGY -> Items.ECHO_SHARD;
            case COIN -> Items.GOLD_INGOT;
            case XP -> Items.EXPERIENCE_BOTTLE;
            case MARK -> Items.GLOWSTONE_DUST;
        };
    }

    public static void ensureRecipes() {
        List<SlayerRecipes.Recipe> out = new ArrayList<>();
        for (Rune r : Rune.values()) {
            Item reagent = reagentFor(r);
            for (Piece p : Piece.values()) {
                ItemStack[] grid = new ItemStack[9];
                for (int i = 0; i < 9; i++) grid[i] = ItemStack.EMPTY;
                grid[4] = new ItemStack(p.base);
                grid[1] = new ItemStack(reagent, 8);
                grid[7] = new ItemStack(reagent, 8);
                grid[3] = new ItemStack(Items.LAPIS_LAZULI, 16);
                grid[5] = new ItemStack(Items.AMETHYST_SHARD, 6);

                boolean[] locked = new boolean[9];
                for (int i = 0; i < 9; i++) locked[i] = !grid[i].isEmpty();

                out.add(new SlayerRecipes.Recipe(
                        "VX Rune " + r.display + " " + p.display,
                        create(r, p), grid, locked, null, 0));
            }
        }
        RecipeConfigManager.upsertCustomRecipesSilent(out);
    }

    /** Everything in the Runeforge, for catalogues. */
    public static List<ItemStack> allStacks() {
        List<ItemStack> out = new ArrayList<>();
        for (Rune r : Rune.values()) {
            for (Piece p : Piece.values()) out.add(create(r, p));
        }
        return out;
    }
}
