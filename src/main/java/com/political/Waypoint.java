package com.political;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.Locale;

/**
 * The forty five waypoints of the Voyager line.
 *
 * <p>Voyager is the only line that asks where you are. Every waypoint belongs to a <b>reach</b>, and
 * a piece is quiet at home and loud in its own reach, so a Tides piece is nearly dead in a cave and
 * excellent at sea. On top of that the whole line scales with <b>Leagues</b>, which counts how far
 * you have travelled from the nearest settlement, so the gear rewards actually going somewhere.</p>
 */
public enum Waypoint {

    // ── Frontier reach: the surface, the road, the far side of the map ─────
    MILESTONE("The Milestone", Reach.FRONTIER, ChatFormatting.GRAY, Items.STONE_BRICKS,
            "Somebody counted, once, and stopped.", "speed", 5, Boon.STRIDE,
            "You move quicker the further out you are."),
    CAIRN("The Cairn", Reach.FRONTIER, ChatFormatting.WHITE, Items.COBBLESTONE,
            "Stacked by people who were not coming back this way.", "defense", 45, Boon.SHELTER,
            "You take less from everything while out in the open."),
    OLD_ROAD("The Old Road", Reach.FRONTIER, ChatFormatting.GOLD, Items.DIRT_PATH,
            "It goes somewhere that is no longer there.", "health", 55, Boon.ENDURE,
            "Hunger and exhaustion stop mattering out here."),
    WATCHFIRE("The Watchfire", Reach.FRONTIER, ChatFormatting.RED, Items.CAMPFIRE,
            "Kept alight by an argument nobody remembers starting.", "strength", 45, Boon.WARD,
            "Hostile things think twice about approaching."),
    TRAPPERS_MARK("The Trapper's Mark", Reach.FRONTIER, ChatFormatting.DARK_GREEN, Items.STRING,
            "Cut into the bark at eye height.", "crit_chance", 15, Boon.QUARRY,
            "Animals and monsters both drop more."),
    HIGH_PASS("The High Pass", Reach.FRONTIER, ChatFormatting.AQUA, Items.SNOW_BLOCK,
            "Open four months of the year, optimistically.", "defense", 50, Boon.CLIMB,
            "Height stops costing you anything."),
    STANDING_STONE("The Standing Stone", Reach.FRONTIER, ChatFormatting.LIGHT_PURPLE, Items.AMETHYST_BLOCK,
            "Older than the road that avoids it.", "intelligence", 60, Boon.LEY,
            "Leagues count double toward everything."),
    BOUNDARY_OAK("The Boundary Oak", Reach.FRONTIER, ChatFormatting.DARK_GREEN, Items.OAK_LOG,
            "Two counties have argued about it for centuries.", "health", 60, Boon.ROOTED,
            "You cannot be moved against your will."),
    LAST_INN("The Last Inn", Reach.FRONTIER, ChatFormatting.GOLD, Items.BREAD,
            "After this it is just weather.", "health", 65, Boon.REST,
            "You mend quickly whenever you stop moving."),

    // ── Underdark reach: caves, depth, the dark that stays ─────────────────
    DEEPSHAFT("The Deepshaft", Reach.UNDERDARK, ChatFormatting.DARK_GRAY, Items.DEEPSLATE,
            "It was dug for a reason nobody wrote down.", "defense", 55, Boon.DELVE,
            "Depth makes you tougher instead of more fragile."),
    GLOWCAP("The Glowcap", Reach.UNDERDARK, ChatFormatting.AQUA, Items.GLOW_BERRIES,
            "The only honest light down here.", "intelligence", 55, Boon.NIGHT,
            "The dark is simply daylight to you."),
    DRIPSTONE_HALL("The Dripstone Hall", Reach.UNDERDARK, ChatFormatting.GOLD, Items.POINTED_DRIPSTONE,
            "It has been building this for a very long time.", "crit_damage", 55, Boon.SPIKE,
            "Falling and striking downward both hurt far more."),
    LOSTVEIN("The Lostvein", Reach.UNDERDARK, ChatFormatting.YELLOW, Items.RAW_GOLD,
            "Everybody has heard about it and nobody has seen it.", "crit_chance", 15, Boon.PROSPECT,
            "Ore pays out considerably more."),
    ECHO_CHAMBER("The Echo Chamber", Reach.UNDERDARK, ChatFormatting.DARK_AQUA, Items.ECHO_SHARD,
            "It repeats the last thing anybody said in it.", "intelligence", 65, Boon.SENSE,
            "You feel everything living nearby through the stone."),
    SCULK_MARCH("The Sculk March", Reach.UNDERDARK, ChatFormatting.DARK_PURPLE, Items.SCULK,
            "It is going somewhere, slowly, and it is winning.", "strength", 50, Boon.CREEP,
            "You make no noise at all when you move."),
    COLLAPSE("The Collapse", Reach.UNDERDARK, ChatFormatting.GRAY, Items.GRAVEL,
            "The reason the shaft was abandoned.", "strength", 55, Boon.BREAK,
            "Stone gives way much faster."),
    BLIND_SPRING("The Blind Spring", Reach.UNDERDARK, ChatFormatting.BLUE, Items.WATER_BUCKET,
            "Cold, clean and completely unexpected.", "health", 60, Boon.REST,
            "You mend quickly whenever you stop moving."),
    LOWEST_STEP("The Lowest Step", Reach.UNDERDARK, ChatFormatting.BLACK, Items.BEDROCK,
            "There is nothing under this.", "defense", 70, Boon.DELVE,
            "Depth makes you tougher instead of more fragile."),

    // ── Tides reach: water, coast, everything that floats ──────────────────
    HARBOUR_LIGHT("The Harbour Light", Reach.TIDES, ChatFormatting.YELLOW, Items.SEA_LANTERN,
            "It is the last argument against the rocks.", "intelligence", 55, Boon.NIGHT,
            "The dark is simply daylight to you."),
    DEEP_ANCHOR("The Deep Anchor", Reach.TIDES, ChatFormatting.GRAY, Items.IRON_BLOCK,
            "Whatever it was holding is long gone.", "defense", 60, Boon.ROOTED,
            "You cannot be moved against your will."),
    KELP_FOREST("The Kelp Forest", Reach.TIDES, ChatFormatting.DARK_GREEN, Items.KELP,
            "A whole country nobody has mapped.", "health", 60, Boon.BREATH,
            "Water carries you and you never drown."),
    CORAL_SHELF("The Coral Shelf", Reach.TIDES, ChatFormatting.LIGHT_PURPLE, Items.BRAIN_CORAL_BLOCK,
            "Built by something that never saw the plan.", "defense", 50, Boon.SHELTER,
            "You take less from everything while out in the open."),
    DROWNED_SPIRE("The Drowned Spire", Reach.TIDES, ChatFormatting.DARK_AQUA, Items.PRISMARINE_BRICKS,
            "It was a landmark before the water came.", "crit_damage", 60, Boon.SURGE,
            "Striking from in or under water hits far harder."),
    SHOAL("The Shoal", Reach.TIDES, ChatFormatting.AQUA, Items.COD,
            "Thousands of small decisions, made at once.", "crit_chance", 20, Boon.QUARRY,
            "Animals and monsters both drop more."),
    TIDE_POOL("The Tide Pool", Reach.TIDES, ChatFormatting.GREEN, Items.SEA_PICKLE,
            "A whole world with a schedule.", "health", 55, Boon.REST,
            "You mend quickly whenever you stop moving."),
    SUNKEN_HOLD("The Sunken Hold", Reach.TIDES, ChatFormatting.GOLD, Items.GOLD_INGOT,
            "Somebody was extremely unlucky here.", "crit_chance", 15, Boon.PROSPECT,
            "Ore pays out considerably more."),
    OPEN_WATER("The Open Water", Reach.TIDES, ChatFormatting.BLUE, Items.HEART_OF_THE_SEA,
            "No landmarks, which is the landmark.", "speed", 5, Boon.STRIDE,
            "You move quicker the further out you are."),

    // ── Cinder reach: the burning side of the door ─────────────────────────
    BASALT_ROAD("The Basalt Road", Reach.CINDER, ChatFormatting.DARK_GRAY, Items.BASALT,
            "Somebody paved it. That is the worrying part.", "defense", 55, Boon.EMBERWARD,
            "Fire and lava both treat you gently."),
    SOUL_FLATS("The Soul Flats", Reach.CINDER, ChatFormatting.DARK_AQUA, Items.SOUL_SAND,
            "The ground remembers more than it should.", "intelligence", 65, Boon.SENSE,
            "You feel everything living nearby through the stone."),
    CRIMSON_STAND("The Crimson Stand", Reach.CINDER, ChatFormatting.RED, Items.CRIMSON_STEM,
            "A forest that never agreed to be one.", "strength", 50, Boon.QUARRY,
            "Animals and monsters both drop more."),
    WARPED_HOLLOW("The Warped Hollow", Reach.CINDER, ChatFormatting.DARK_AQUA, Items.WARPED_STEM,
            "It is quiet in a way that is not restful.", "intelligence", 60, Boon.CREEP,
            "You make no noise at all when you move."),
    LAVA_FALL("The Lava Fall", Reach.CINDER, ChatFormatting.GOLD, Items.LAVA_BUCKET,
            "Slow, orange and entirely uninterested.", "strength", 60, Boon.EMBERWARD,
            "Fire and lava both treat you gently."),
    BASTION_GATE("The Bastion Gate", Reach.CINDER, ChatFormatting.YELLOW, Items.GILDED_BLACKSTONE,
            "Built by something that likes gold and dislikes you.", "defense", 65, Boon.WARD,
            "Hostile things think twice about approaching."),
    DEBRIS_SEAM("The Debris Seam", Reach.CINDER, ChatFormatting.DARK_RED, Items.ANCIENT_DEBRIS,
            "Older than the dimension it is buried in.", "crit_damage", 65, Boon.PROSPECT,
            "Ore pays out considerably more."),
    ASHEN_WASTE("The Ashen Waste", Reach.CINDER, ChatFormatting.GRAY, Items.BLACKSTONE,
            "It ends where somebody stopped looking.", "health", 65, Boon.ENDURE,
            "Hunger and exhaustion stop mattering out here."),
    THE_DOOR("The Door", Reach.CINDER, ChatFormatting.LIGHT_PURPLE, Items.OBSIDIAN,
            "Two rooms that should not be adjacent.", "intelligence", 70, Boon.LEY,
            "Leagues count double toward everything."),

    // ── Outer reach: the end, the void, the parts left over ────────────────
    VOID_MARGIN("The Void Margin", Reach.OUTER, ChatFormatting.DARK_GRAY, Items.END_STONE,
            "The edge, and then the honest answer.", "defense", 60, Boon.FEATHER,
            "Falling stops being a problem."),
    CHORUS_STAND("The Chorus Stand", Reach.OUTER, ChatFormatting.LIGHT_PURPLE, Items.CHORUS_FRUIT,
            "It grows in a place with no weather.", "health", 65, Boon.BLINK,
            "You slip a short way forward whenever you sprint."),
    OBSIDIAN_SPIRE("The Obsidian Spire", Reach.OUTER, ChatFormatting.DARK_PURPLE, Items.OBSIDIAN,
            "Somebody built it and then left immediately.", "defense", 70, Boon.SHELTER,
            "You take less from everything while out in the open."),
    END_GATE("The End Gate", Reach.OUTER, ChatFormatting.AQUA, Items.END_CRYSTAL,
            "It is not asking whether you are ready.", "intelligence", 75, Boon.LEY,
            "Leagues count double toward everything."),
    SHULKER_HOLD("The Shulker Hold", Reach.OUTER, ChatFormatting.LIGHT_PURPLE, Items.SHULKER_SHELL,
            "It closed and it is not opening for you.", "defense", 65, Boon.ROOTED,
            "You cannot be moved against your will."),
    ELYTRA_RUN("The Elytra Run", Reach.OUTER, ChatFormatting.WHITE, Items.PHANTOM_MEMBRANE,
            "The first person to try it was very brave.", "speed", 5, Boon.FEATHER,
            "Falling stops being a problem."),
    PEARL_FIELD("The Pearl Field", Reach.OUTER, ChatFormatting.DARK_GREEN, Items.ENDER_PEARL,
            "They are all going somewhere and nowhere.", "crit_chance", 20, Boon.BLINK,
            "You slip a short way forward whenever you sprint."),
    SILENT_ISLE("The Silent Isle", Reach.OUTER, ChatFormatting.GRAY, Items.PURPUR_BLOCK,
            "Nothing has happened here and nothing will.", "intelligence", 70, Boon.SENSE,
            "You feel everything living nearby through the stone."),
    LAST_LEAGUE("The Last League", Reach.OUTER, ChatFormatting.WHITE, Items.NETHER_STAR,
            "There is a furthest anybody has been.", "crit_damage", 75, Boon.STRIDE,
            "You move quicker the further out you are.");

    /** The five reaches. A piece is loud in its own reach and quiet everywhere else. */
    public enum Reach {
        FRONTIER("Frontier", ChatFormatting.GREEN, "The open road pays you for staying on it."),
        UNDERDARK("Underdark", ChatFormatting.DARK_GRAY, "Depth stops being dangerous and starts being useful."),
        TIDES("Tides", ChatFormatting.AQUA, "Water carries you and hides nothing from you."),
        CINDER("Cinder", ChatFormatting.RED, "Heat is a floor you walk on rather than a threat."),
        OUTER("Outer", ChatFormatting.LIGHT_PURPLE, "Distance and height both stop applying to you.");

        public final String label;
        public final ChatFormatting colour;
        public final String bonusText;

        Reach(String label, ChatFormatting colour, String bonusText) {
            this.label = label;
            this.colour = colour;
            this.bonusText = bonusText;
        }
    }

    /** What a waypoint grants while you stand in its reach. */
    public enum Boon {
        STRIDE, SHELTER, ENDURE, WARD, QUARRY, CLIMB, LEY, ROOTED, REST,
        DELVE, NIGHT, SPIKE, PROSPECT, SENSE, CREEP, BREAK,
        BREATH, SURGE, EMBERWARD, FEATHER, BLINK
    }

    public final String display;
    public final Reach reach;
    public final ChatFormatting colour;
    public final Item token;
    public final String flavor;
    public final String statKey;
    public final int statAmount;
    public final Boon boon;
    public final String powerText;

    Waypoint(String display, Reach reach, ChatFormatting colour, Item token, String flavor,
             String statKey, int statAmount, Boon boon, String powerText) {
        this.display = display;
        this.reach = reach;
        this.colour = colour;
        this.token = token;
        this.flavor = flavor;
        this.statKey = statKey;
        this.statAmount = statAmount;
        this.boon = boon;
        this.powerText = powerText;
    }

    public String key() {
        return name().toLowerCase(Locale.ROOT);
    }

    public GearLine.Motif motif() {
        return new GearLine.Motif(key(), display, colour, token, flavor,
                reach.label, reach.colour, reach.bonusText,
                java.util.List.of(GearLine.stat(statKey, statAmount)),
                "Only wakes up in the " + reach.label + " reach.");
    }

    public static Waypoint byKey(String key) {
        if (key == null) return null;
        for (Waypoint w : values()) {
            if (w.key().equalsIgnoreCase(key)) return w;
        }
        return null;
    }
}
