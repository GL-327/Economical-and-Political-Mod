package com.political;

import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.Locale;

/**
 * What an incursion turns into, decided by the ground it opens on.
 *
 * <p>This is the part a fixed island cannot do. On a random seed the server has no idea what is
 * around a player, so instead of shipping one arena the incursion reads the biome underfoot and
 * becomes something appropriate to it. The same command in a swamp, a peak and the nether produces
 * three different fights with three different rosters, three different hazards and three different
 * bosses, and none of that had to be authored per location.</p>
 */
public enum IncursionTheme {

    VERDANT("Verdant Incursion", ChatFormatting.DARK_GREEN, Items.OAK_SAPLING,
            "The green comes in and does not stop.",
            "Overgrowth. The air is thick and everything is poisoned.",
            new EntityType<?>[]{EntityTypes.SPIDER, EntityTypes.CAVE_SPIDER, EntityTypes.WITCH,
                    EntityTypes.ZOMBIE, EntityTypes.SLIME},
            EntityTypes.RAVAGER, Hazard.POISON),

    FROZEN("Frozen Incursion", ChatFormatting.AQUA, Items.BLUE_ICE,
            "Something under the ice noticed you.",
            "The cold bites and the footing is gone.",
            new EntityType<?>[]{EntityTypes.STRAY, EntityTypes.SKELETON, EntityTypes.POLAR_BEAR,
                    EntityTypes.ZOMBIE, EntityTypes.PHANTOM},
            EntityTypes.IRON_GOLEM, Hazard.FREEZE),

    SCORCHED("Scorched Incursion", ChatFormatting.GOLD, Items.SAND,
            "The heat had been building for a while.",
            "Open ground, no shade, and the sand remembers.",
            new EntityType<?>[]{EntityTypes.HUSK, EntityTypes.BLAZE, EntityTypes.SKELETON,
                    EntityTypes.ZOMBIE, EntityTypes.VINDICATOR},
            EntityTypes.RAVAGER, Hazard.BURN),

    DROWNED("Drowned Incursion", ChatFormatting.BLUE, Items.PRISMARINE,
            "The water gave something back.",
            "Waterlogged and heavy. Everything here breathes better than you.",
            new EntityType<?>[]{EntityTypes.DROWNED, EntityTypes.GUARDIAN, EntityTypes.SLIME,
                    EntityTypes.ZOMBIE, EntityTypes.WITCH},
            EntityTypes.ELDER_GUARDIAN, Hazard.DROWN),

    HOLLOW("Hollow Incursion", ChatFormatting.DARK_AQUA, Items.SCULK,
            "It has been listening down here for a long time.",
            "The dark is total and the ground is listening.",
            new EntityType<?>[]{EntityTypes.SKELETON, EntityTypes.CAVE_SPIDER, EntityTypes.SILVERFISH,
                    EntityTypes.ZOMBIE, EntityTypes.CREEPER},
            EntityTypes.WARDEN, Hazard.BLIND),

    INFERNAL("Infernal Incursion", ChatFormatting.RED, Items.NETHERRACK,
            "A door that was already open opened further.",
            "Fire underfoot and nothing here is surprised to see you.",
            new EntityType<?>[]{EntityTypes.BLAZE, EntityTypes.ZOMBIFIED_PIGLIN, EntityTypes.MAGMA_CUBE,
                    EntityTypes.PIGLIN_BRUTE, EntityTypes.WITHER_SKELETON},
            EntityTypes.WITHER_SKELETON, Hazard.BURN),

    VOIDBORNE("Voidborne Incursion", ChatFormatting.DARK_PURPLE, Items.END_STONE,
            "Something stepped through sideways.",
            "The ground is a suggestion and the things here move wrong.",
            new EntityType<?>[]{EntityTypes.ENDERMAN, EntityTypes.SHULKER, EntityTypes.PHANTOM,
                    EntityTypes.ENDERMITE, EntityTypes.VEX},
            EntityTypes.ENDERMAN, Hazard.PHASE),

    STORMWRACKED("Stormwracked Incursion", ChatFormatting.YELLOW, Items.LIGHTNING_ROD.weathering().unaffected(),
            "The weather picked a side.",
            "Exposed, loud, and the sky keeps intervening.",
            new EntityType<?>[]{EntityTypes.PHANTOM, EntityTypes.SKELETON, EntityTypes.VEX,
                    EntityTypes.WITCH, EntityTypes.PILLAGER},
            EntityTypes.EVOKER, Hazard.SHOCK),

    BLIGHTED("Blighted Incursion", ChatFormatting.DARK_GRAY, Items.WITHER_ROSE,
            "Nothing has grown here in a long time.",
            "Sour ground. Whatever lived here left something behind.",
            new EntityType<?>[]{EntityTypes.ZOMBIE, EntityTypes.SKELETON, EntityTypes.WITCH,
                    EntityTypes.HUSK, EntityTypes.ZOGLIN},
            EntityTypes.WITHER_SKELETON, Hazard.WITHER);

    /** The standing hazard that makes each theme feel like its own place. */
    public enum Hazard {
        POISON("Choking pollen", ChatFormatting.DARK_GREEN),
        FREEZE("Killing cold", ChatFormatting.AQUA),
        BURN("Open heat", ChatFormatting.GOLD),
        DROWN("Rising water", ChatFormatting.BLUE),
        BLIND("Total dark", ChatFormatting.DARK_GRAY),
        PHASE("Unstable ground", ChatFormatting.DARK_PURPLE),
        SHOCK("Live air", ChatFormatting.YELLOW),
        WITHER("Sour ground", ChatFormatting.DARK_GRAY);

        public final String label;
        public final ChatFormatting colour;

        Hazard(String label, ChatFormatting colour) {
            this.label = label;
            this.colour = colour;
        }
    }

    public final String display;
    public final ChatFormatting colour;
    public final Item icon;
    public final String flavor;
    public final String conditions;
    public final EntityType<?>[] roster;
    public final EntityType<?> boss;
    public final Hazard hazard;

    IncursionTheme(String display, ChatFormatting colour, Item icon, String flavor,
                   String conditions, EntityType<?>[] roster, EntityType<?> boss, Hazard hazard) {
        this.display = display;
        this.colour = colour;
        this.icon = icon;
        this.flavor = flavor;
        this.conditions = conditions;
        this.roster = roster;
        this.boss = boss;
        this.hazard = hazard;
    }

    public String key() {
        return name().toLowerCase(Locale.ROOT);
    }

    /**
     * Reads a biome id and a depth and decides what opens.
     *
     * <p>Matching on the biome path rather than on tags is deliberate. Tag sets move between
     * versions and a mismatch there would silently collapse every incursion into one theme, whereas
     * a path that stops matching simply falls through to the sensible default for that dimension.</p>
     */
    public static IncursionTheme forLocation(String dimensionId, String biomeId, int y) {
        String d = dimensionId == null ? "" : dimensionId.toLowerCase(Locale.ROOT);
        String b = biomeId == null ? "" : biomeId.toLowerCase(Locale.ROOT);

        if (d.contains("the_nether")) return INFERNAL;
        if (d.contains("the_end")) return VOIDBORNE;

        // Deep underground overrides the surface biome, because at that depth the surface is not
        // what the player is actually standing in.
        if (y < 20) return HOLLOW;
        if (b.contains("deep_dark") || b.contains("lush_caves") || b.contains("dripstone")) return HOLLOW;

        if (b.contains("ocean") || b.contains("river") || b.contains("swamp")
                || b.contains("beach") || b.contains("mangrove")) return DROWNED;
        if (b.contains("snow") || b.contains("ice") || b.contains("frozen")
                || b.contains("cold") || b.contains("taiga")) return FROZEN;
        if (b.contains("desert") || b.contains("badlands") || b.contains("savanna")
                || b.contains("mesa")) return SCORCHED;
        if (b.contains("peak") || b.contains("windswept") || b.contains("mountain")
                || b.contains("hills") || b.contains("grove") || b.contains("meadow")) return STORMWRACKED;
        if (b.contains("forest") || b.contains("jungle") || b.contains("plains")
                || b.contains("birch") || b.contains("bamboo") || b.contains("cherry")) return VERDANT;

        return BLIGHTED;
    }
}
