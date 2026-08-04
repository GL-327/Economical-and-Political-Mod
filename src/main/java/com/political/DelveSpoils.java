package com.political;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.List;

/**
 * The spoils: ten kinds across seven depths in fifteen grades, so one thousand and fifty.
 *
 * <p>This is what a delve actually pays out in. You gather them, you sell them, and you
 * spend them on marks, so the pile in your bag is the progress bar for the whole system.</p>
 *
 * <p>Rarity is carried by the depth rather than by a separate roll. A Hollow shard is
 * obviously worth more than a Catacomb one without anybody having to read a tooltip to
 * find out, which is the difference between a thousand items and a thousand items nobody
 * can tell apart.</p>
 */
public final class DelveSpoils {

    private DelveSpoils() {}

    public record Kind(String key, String display, ChatFormatting colour, Item base, String flavour) {}

    public record Depth(String key, String display, ChatFormatting colour, int tier) {}

    public record Variant(String key, String display, double weight) {}

    public static final List<Kind> KINDS = List.of(
            new Kind("essence", "Essence", ChatFormatting.AQUA, Items.AMETHYST_SHARD, "Rendered down from something that lived here."),
            new Kind("shard", "Shard", ChatFormatting.WHITE, Items.QUARTZ, "A piece of a thing that used to be whole."),
            new Kind("sigilfragment", "Sigil Fragment", ChatFormatting.LIGHT_PURPLE, Items.PRISMARINE_SHARD, "Part of a mark somebody else earned."),
            new Kind("keyshard", "Key Shard", ChatFormatting.GOLD, Items.GOLD_NUGGET, "Enough of these open a door."),
            new Kind("relic", "Relic", ChatFormatting.YELLOW, Items.GOLDEN_APPLE, "Somebody thought this was worth burying."),
            new Kind("dust", "Dust", ChatFormatting.GRAY, Items.GUNPOWDER, "What is left after the rest of it went."),
            new Kind("core", "Core", ChatFormatting.RED, Items.FIRE_CHARGE, "Still doing whatever it was made to do."),
            new Kind("ember", "Ember", ChatFormatting.GOLD, Items.BLAZE_POWDER, "It has not gone out in a very long time."),
            new Kind("token", "Token", ChatFormatting.GREEN, Items.EMERALD, "Currency, of a sort, to whoever was down here."),
            new Kind("scrap", "Scrap", ChatFormatting.DARK_GRAY, Items.IRON_NUGGET, "Worth something only in quantity.")
    );

    public static final List<Depth> DEPTHS = List.of(
            new Depth("catacombs", "Catacomb", ChatFormatting.GRAY, 1),
            new Depth("sunken", "Sunken", ChatFormatting.DARK_AQUA, 2),
            new Depth("ice", "Frostbound", ChatFormatting.AQUA, 3),
            new Depth("sands", "Buried", ChatFormatting.YELLOW, 4),
            new Depth("toxic", "Blighted", ChatFormatting.DARK_GREEN, 5),
            new Depth("citadel", "Warded", ChatFormatting.GOLD, 6),
            new Depth("endcastle", "Hollow", ChatFormatting.DARK_PURPLE, 7)
    );

    public static final List<Variant> VARIANTS = List.of(
            new Variant("lesser", "Lesser", 0.5),
            new Variant("common", "", 1.0),
            new Variant("fine", "Fine", 1.4),
            new Variant("keen", "Keen", 1.8),
            new Variant("deep", "Deep", 2.3),
            new Variant("pale", "Pale", 2.9),
            new Variant("bright", "Bright", 3.6),
            new Variant("heavy", "Heavy", 4.4),
            new Variant("true", "True", 5.3),
            new Variant("elder", "Elder", 6.3),
            new Variant("veiled", "Veiled", 7.4),
            new Variant("sundered", "Sundered", 8.6),
            new Variant("radiant", "Radiant", 9.9),
            new Variant("perfect", "Perfect", 11.3),
            new Variant("primal", "Primal", 12.8)
    );

    public static String idOf(Kind k, Depth d, Variant v) {
        return "delve_" + d.key() + "_" + k.key() + "_" + v.key();
    }

    public static String nameOf(Kind k, Depth d, Variant v) {
        return (v.display().isEmpty() ? "" : v.display() + " ")
                + d.display() + " " + k.display();
    }

    /** What one is worth, from how deep it came and how good a one it is. */
    public static long valueOf(Kind k, Depth d, Variant v) {
        return Math.round(120.0 * d.tier() * d.tier() * v.weight());
    }

    public static int total() {
        return KINDS.size() * DEPTHS.size() * VARIANTS.size();
    }
}
