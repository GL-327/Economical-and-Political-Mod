package com.political;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.Locale;

/**
 * The forty five storms of the Tempest line.
 *
 * <p>Tempest is the only line that cares what your friends are holding. Every storm hangs a
 * <b>charge</b> on whatever you hit, charges linger for a few seconds, and when a target is carrying
 * two different charges at once they react into something neither one does alone. Scald plus Frost
 * shatters, Shock plus Brine conducts, Gale plus Dust blinds a whole area. Two players running
 * different fronts get results neither could reach on their own.</p>
 */
public enum Storm {

    // ── Thermal front: heat, pressure, the air that will not sit still ─────
    HEATWAVE("Heatwave", Front.THERMAL, ChatFormatting.GOLD, Items.MAGMA_BLOCK,
            "The whole valley gives up at noon.", "strength", 45, Charge.SCALD),
    FIRESTORM("Firestorm", Front.THERMAL, ChatFormatting.RED, Items.BLAZE_POWDER,
            "It makes its own weather and then uses it.", "ferocity", 15, Charge.SCALD),
    SIROCCO("Sirocco", Front.THERMAL, ChatFormatting.YELLOW, Items.SAND,
            "Hot wind carrying somebody else's desert.", "speed", 5, Charge.DUST),
    THERMAL_UPDRAFT("Thermal Updraft", Front.THERMAL, ChatFormatting.GOLD, Items.FEATHER,
            "The ground lets go of the air.", "crit_chance", 15, Charge.GALE),
    ASHCLOUD("Ashcloud", Front.THERMAL, ChatFormatting.DARK_GRAY, Items.GUNPOWDER,
            "It settles on everything for a year.", "defense", 45, Charge.DUST),
    STEAMBURST("Steamburst", Front.THERMAL, ChatFormatting.WHITE, Items.CAULDRON,
            "Water, extremely annoyed.", "crit_damage", 55, Charge.SCALD),
    EMBERFALL("Emberfall", Front.THERMAL, ChatFormatting.GOLD, Items.CAMPFIRE,
            "It comes down slowly, which is worse.", "strength", 40, Charge.EMBER),
    DROUGHT("Drought", Front.THERMAL, ChatFormatting.YELLOW, Items.DEAD_BUSH,
            "A storm made of nothing happening.", "health", 60, Charge.DUST),
    CORONA("Corona", Front.THERMAL, ChatFormatting.WHITE, Items.GLOWSTONE,
            "The part of the fire that is not the fire.", "intelligence", 70, Charge.EMBER),

    // ── Polar front: cold, weight, the slow kind of danger ─────────────────
    BLIZZARD("Blizzard", Front.POLAR, ChatFormatting.WHITE, Items.SNOW_BLOCK,
            "It removes the horizon first.", "defense", 55, Charge.FROST),
    HAILSTORM("Hailstorm", Front.POLAR, ChatFormatting.AQUA, Items.PACKED_ICE,
            "The sky throws rocks.", "crit_damage", 60, Charge.FROST),
    WHITEOUT("Whiteout", Front.POLAR, ChatFormatting.WHITE, Items.POWDER_SNOW_BUCKET,
            "Every direction agreed to look the same.", "intelligence", 55, Charge.FROST),
    RIMEFOG("Rimefog", Front.POLAR, ChatFormatting.AQUA, Items.BLUE_ICE,
            "It leaves a crust on everything it touched.", "health", 65, Charge.FROST),
    COLD_SNAP("Cold Snap", Front.POLAR, ChatFormatting.BLUE, Items.ICE,
            "An hour ago this was fine.", "crit_chance", 15, Charge.FROST),
    GLACIER_WIND("Glacier Wind", Front.POLAR, ChatFormatting.DARK_AQUA, Items.SNOWBALL,
            "It has been travelling downhill for a century.", "strength", 40, Charge.GALE),
    SLEET("Sleet", Front.POLAR, ChatFormatting.GRAY, Items.WATER_BUCKET,
            "Rain that decided to be unpleasant.", "defense", 45, Charge.BRINE),
    FROSTHEAVE("Frostheave", Front.POLAR, ChatFormatting.WHITE, Items.DIRT,
            "The ground lifts itself overnight.", "health", 70, Charge.FROST),
    POLAR_NIGHT("Polar Night", Front.POLAR, ChatFormatting.DARK_BLUE, Items.BLACK_WOOL,
            "Months of it, and no promises.", "intelligence", 75, Charge.GLOOM),

    // ── Electric front: charge, discharge, the moment before ───────────────
    THUNDERHEAD("Thunderhead", Front.ELECTRIC, ChatFormatting.DARK_GRAY, Items.LIGHTNING_ROD,
            "It has been building all afternoon.", "strength", 50, Charge.SHOCK),
    DRY_LIGHTNING("Dry Lightning", Front.ELECTRIC, ChatFormatting.YELLOW, Items.FIRE_CHARGE,
            "All the danger, none of the rain.", "crit_damage", 65, Charge.SHOCK),
    BALL_LIGHTNING("Ball Lightning", Front.ELECTRIC, ChatFormatting.WHITE, Items.SLIME_BALL,
            "Nobody has ever agreed on what it is.", "intelligence", 65, Charge.SHOCK),
    STATIC_FIELD("Static Field", Front.ELECTRIC, ChatFormatting.AQUA, Items.COPPER_INGOT,
            "Your hair knows before you do.", "crit_chance", 20, Charge.SHOCK),
    ARC_FLASH("Arc Flash", Front.ELECTRIC, ChatFormatting.WHITE, Items.COPPER_BLOCK,
            "Faster than the decision to blink.", "ferocity", 15, Charge.SHOCK),
    SKYFIRE("Skyfire", Front.ELECTRIC, ChatFormatting.GOLD, Items.FIREWORK_ROCKET,
            "The upper air, arguing with itself.", "crit_damage", 70, Charge.EMBER),
    IONWIND("Ionwind", Front.ELECTRIC, ChatFormatting.LIGHT_PURPLE, Items.AMETHYST_SHARD,
            "It smells like a thunderstorm and there is no storm.", "speed", 5, Charge.GALE),
    GROUNDING("Grounding", Front.ELECTRIC, ChatFormatting.GRAY, Items.IRON_BLOCK,
            "Somewhere for all that to go.", "defense", 60, Charge.SHOCK),
    AURORA("Aurora", Front.ELECTRIC, ChatFormatting.GREEN, Items.PRISMARINE_CRYSTALS,
            "Violence, seen from far enough away to be pretty.", "intelligence", 80, Charge.GLOOM),

    // ── Aeolian front: wind, distance, the thing that carries other things ─
    GALEFRONT("Galefront", Front.AEOLIAN, ChatFormatting.WHITE, Items.FEATHER,
            "It arrives before the news about it.", "speed", 5, Charge.GALE),
    SQUALL("Squall", Front.AEOLIAN, ChatFormatting.GRAY, Items.STRING,
            "Brief, and it takes the roof.", "crit_chance", 15, Charge.GALE),
    CYCLONE("Cyclone", Front.AEOLIAN, ChatFormatting.DARK_AQUA, Items.WIND_CHARGE,
            "A hole in the weather with edges.", "strength", 55, Charge.GALE),
    DOWNBURST("Downburst", Front.AEOLIAN, ChatFormatting.AQUA, Items.ANVIL,
            "The sky pushes straight down and means it.", "crit_damage", 60, Charge.GALE),
    SANDSTORM("Sandstorm", Front.AEOLIAN, ChatFormatting.YELLOW, Items.SANDSTONE,
            "It rewrites the map while you are in it.", "defense", 50, Charge.DUST),
    TRADEWIND("Tradewind", Front.AEOLIAN, ChatFormatting.AQUA, Items.WHITE_WOOL,
            "Reliable, which is why anybody built ships.", "speed", 5, Charge.GALE),
    HOWLING_PASS("Howling Pass", Front.AEOLIAN, ChatFormatting.GRAY, Items.STONE,
            "The mountain has one note and plays it constantly.", "ferocity", 10, Charge.GALE),
    DUST_DEVIL("Dust Devil", Front.AEOLIAN, ChatFormatting.GOLD, Items.RED_SAND,
            "Small, brief and weirdly personal.", "crit_chance", 20, Charge.DUST),
    JETSTREAM("Jetstream", Front.AEOLIAN, ChatFormatting.WHITE, Items.ELYTRA,
            "The road nobody had to build.", "intelligence", 60, Charge.GALE),

    // ── Telluric front: the weather the ground makes ───────────────────────
    MONSOON("Monsoon", Front.TELLURIC, ChatFormatting.BLUE, Items.WATER_BUCKET,
            "The year arrives in one week.", "health", 70, Charge.BRINE),
    STORMSURGE("Stormsurge", Front.TELLURIC, ChatFormatting.DARK_AQUA, Items.PRISMARINE,
            "The sea comes to have a look.", "strength", 55, Charge.BRINE),
    FLOODTIDE("Floodtide", Front.TELLURIC, ChatFormatting.AQUA, Items.KELP,
            "Patient, and then all at once.", "defense", 55, Charge.BRINE),
    MIREFOG("Mirefog", Front.TELLURIC, ChatFormatting.DARK_GREEN, Items.MUD,
            "The ground exhales and nobody enjoys it.", "intelligence", 55, Charge.GLOOM),
    ASHFALL_STORM("Ashfall", Front.TELLURIC, ChatFormatting.DARK_GRAY, Items.BASALT,
            "The mountain sends a letter.", "defense", 60, Charge.DUST),
    SEAFRET("Seafret", Front.TELLURIC, ChatFormatting.WHITE, Items.NAUTILUS_SHELL,
            "It walks inland and stays for a day.", "health", 60, Charge.BRINE),
    RIPTIDE("Riptide", Front.TELLURIC, ChatFormatting.DARK_BLUE, Items.HEART_OF_THE_SEA,
            "It is taking you somewhere and it does not care where.", "ferocity", 15, Charge.BRINE),
    LANDSLIP("Landslip", Front.TELLURIC, ChatFormatting.GRAY, Items.GRAVEL,
            "The hill changed its mind.", "strength", 60, Charge.DUST),
    DEEPGLOOM("Deepgloom", Front.TELLURIC, ChatFormatting.DARK_PURPLE, Items.SCULK_CATALYST,
            "Weather that has never seen the sky.", "intelligence", 85, Charge.GLOOM);

    /** The five fronts. Three pieces of a front lights its standing bonus. */
    public enum Front {
        THERMAL("Thermal", ChatFormatting.GOLD, "Your charges last longer and burn on the way out."),
        POLAR("Polar", ChatFormatting.AQUA, "Charged targets move like they are wading."),
        ELECTRIC("Electric", ChatFormatting.YELLOW, "Charges jump to a second target on their own."),
        AEOLIAN("Aeolian", ChatFormatting.WHITE, "Reactions reach much further and knock harder."),
        TELLURIC("Telluric", ChatFormatting.DARK_GREEN, "Reactions leave the ground hostile behind them.");

        public final String label;
        public final ChatFormatting colour;
        public final String bonusText;

        Front(String label, ChatFormatting colour, String bonusText) {
            this.label = label;
            this.colour = colour;
            this.bonusText = bonusText;
        }
    }

    /** What a storm hangs on the target. Two different charges react. */
    public enum Charge {
        SCALD("Scald", ChatFormatting.RED),
        FROST("Frost", ChatFormatting.AQUA),
        SHOCK("Shock", ChatFormatting.YELLOW),
        GALE("Gale", ChatFormatting.WHITE),
        DUST("Dust", ChatFormatting.GOLD),
        BRINE("Brine", ChatFormatting.BLUE),
        EMBER("Ember", ChatFormatting.GOLD),
        GLOOM("Gloom", ChatFormatting.DARK_PURPLE);

        public final String label;
        public final ChatFormatting colour;

        Charge(String label, ChatFormatting colour) {
            this.label = label;
            this.colour = colour;
        }
    }

    public final String display;
    public final Front front;
    public final ChatFormatting colour;
    public final Item token;
    public final String flavor;
    public final String statKey;
    public final int statAmount;
    public final Charge charge;

    Storm(String display, Front front, ChatFormatting colour, Item token, String flavor,
          String statKey, int statAmount, Charge charge) {
        this.display = display;
        this.front = front;
        this.colour = colour;
        this.token = token;
        this.flavor = flavor;
        this.statKey = statKey;
        this.statAmount = statAmount;
        this.charge = charge;
    }

    public String key() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String powerText() {
        return "§7Hits hang " + charge.colour + charge.label + " §7on the target.";
    }

    public GearLine.Motif motif() {
        return new GearLine.Motif(key(), display, colour, token, flavor,
                front.label, front.colour, front.bonusText,
                java.util.List.of(GearLine.stat(statKey, statAmount)),
                "Two different charges on one target react.");
    }

    public static Storm byKey(String key) {
        if (key == null) return null;
        for (Storm s : values()) {
            if (s.key().equalsIgnoreCase(key)) return s;
        }
        return null;
    }
}
