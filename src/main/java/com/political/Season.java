package com.political;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/**
 * One of the four seasons of the Living World.
 *
 * <p>A season is a global, world-wide modifier layer. It is deliberately a <b>trade-off</b>
 * rather than a pure buff track: the season that grows crops fastest is not the season that
 * fills granaries fastest, and winter is a genuine penalty that settlements must have banked
 * against. That keeps the settlement economy from simply drifting upward over real time.</p>
 *
 * <pre>
 *   season   crop growth   settlement food   feel
 *   SPRING       1.35            1.15        planting — things grow, stores are still thin
 *   SUMMER       1.15            1.25        long days, steady work
 *   AUTUMN       1.00            1.35        harvest — the fat months
 *   WINTER       0.55            0.65        scarcity — you live on what autumn gave you
 * </pre>
 *
 * <p>Averaged over a full year the food multiplier is ~1.10, so a settlement that survives
 * winter is only marginally ahead of the pre-season baseline. Seasons add texture, not power.</p>
 */
public enum Season {

    SPRING("Spring", "❀", ChatFormatting.GREEN, "§a", Items.OAK_SAPLING,
            1.35, 1.15,
            "Thaw and green shoots. Everything planted races upward,",
            "but the granaries are still empty from winter."),

    SUMMER("Summer", "☀", ChatFormatting.YELLOW, "§e", Items.SUNFLOWER,
            1.15, 1.25,
            "Long days and hard light. Fields are worked from dawn",
            "to dusk and the roads are busy with trade."),

    AUTUMN("Autumn", "☘", ChatFormatting.GOLD, "§6", Items.WHEAT,
            1.00, 1.35,
            "The harvest. Growth all but stops, but the granaries",
            "fill faster than at any other time of the year."),

    WINTER("Winter", "❄", ChatFormatting.AQUA, "§b", Items.SNOWBALL,
            0.55, 0.65,
            "Frost and hunger. Little grows and settlements eat",
            "what autumn gave them. Prepare, or starve.");

    public final String displayName;
    public final String symbol;
    public final ChatFormatting color;
    /** Legacy §-code equivalent of {@link #color}, for use inside lore strings. */
    public final String legacy;
    public final Item icon;

    /** Multiplier applied to vanilla crop random-tick growth. */
    public final double cropGrowth;
    /** Multiplier applied to settlement FOOD production per economy cycle. */
    public final double foodMultiplier;

    public final String descriptionLine1;
    public final String descriptionLine2;

    Season(String displayName, String symbol, ChatFormatting color, String legacy, Item icon,
           double cropGrowth, double foodMultiplier,
           String descriptionLine1, String descriptionLine2) {
        this.displayName = displayName;
        this.symbol = symbol;
        this.color = color;
        this.legacy = legacy;
        this.icon = icon;
        this.cropGrowth = cropGrowth;
        this.foodMultiplier = foodMultiplier;
        this.descriptionLine1 = descriptionLine1;
        this.descriptionLine2 = descriptionLine2;
    }

    public Season next() {
        Season[] all = values();
        return all[(ordinal() + 1) % all.length];
    }

    public Season previous() {
        Season[] all = values();
        return all[(ordinal() + all.length - 1) % all.length];
    }

    /** "❀ Spring" with the season colour baked in as legacy codes. */
    public String label() {
        return legacy + symbol + " " + displayName;
    }

    /** Short one-line summary of the mechanical effect, for lore. */
    public String effectSummary() {
        return "§7Crops §f" + pct(cropGrowth) + "  §7Food §f" + pct(foodMultiplier);
    }

    private static String pct(double mult) {
        long p = Math.round(mult * 100.0);
        String c = p > 100 ? "§a+" : p < 100 ? "§c" : "§7";
        return c + (p - 100) + "%§r";
    }

    /** Parse by name, case-insensitive. Returns null when unknown. */
    public static Season byName(String name) {
        if (name == null) return null;
        for (Season s : values()) {
            if (s.name().equalsIgnoreCase(name) || s.displayName.equalsIgnoreCase(name)) return s;
        }
        return null;
    }
}
