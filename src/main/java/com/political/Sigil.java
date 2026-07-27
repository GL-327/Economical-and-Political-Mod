package com.political;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.Locale;

/**
 * The forty five sigils of the Grimoire line.
 *
 * <p>Grimoire is the line you steer. Starbound is always on, Beastcraft fills on its own and
 * Clockwork winds on its own, but a sigil does nothing at all until you sneak and use it. In
 * exchange it costs <b>Focus</b>, and Focus refills from your Intelligence, which finally gives that
 * stat a job outside the older magic trees.</p>
 */
public enum Sigil {

    // ── Circle of Kindling: fire, force, the direct answer ─────────────────
    EMBERMARK("Embermark", Circle.KINDLING, ChatFormatting.RED, Items.BLAZE_POWDER,
            "The first thing anybody learns and the last thing anybody stops using.",
            "strength", 40, 20, Casting.FIREBOLT,
            "Throws a bolt that burns what it lands on."),
    PYRECROWN("Pyrecrown", Circle.KINDLING, ChatFormatting.GOLD, Items.FIRE_CHARGE,
            "Wear it and the room warms without asking.", "crit_damage", 50, 35, Casting.PILLAR,
            "Drops a pillar of fire where you are looking."),
    CINDERSTEP("Cinderstep", Circle.KINDLING, ChatFormatting.GOLD, Items.MAGMA_BLOCK,
            "You leave the floor annoyed.", "speed", 5, 25, Casting.TRAIL,
            "Leaves a burning trail behind you for a while."),
    SCORCHWRIT("Scorchwrit", Circle.KINDLING, ChatFormatting.RED, Items.LAVA_BUCKET,
            "A short sentence, aggressively punctuated.", "ferocity", 15, 40, Casting.CONE,
            "Breathes a wide cone of flame."),
    SUNDERBRAND("Sunderbrand", Circle.KINDLING, ChatFormatting.YELLOW, Items.NETHERITE_SCRAP,
            "It marks a thing as already finished.", "crit_chance", 15, 45, Casting.BRAND,
            "Brands a target so everything hits it harder."),
    FORGEWORD("Forgeword", Circle.KINDLING, ChatFormatting.GOLD, Items.ANVIL,
            "Speak it at metal and the metal listens.", "defense", 45, 30, Casting.MEND,
            "Repairs what you are holding and hardens you."),
    ASHFALL("Ashfall", Circle.KINDLING, ChatFormatting.GRAY, Items.GUNPOWDER,
            "The quiet part of every fire.", "strength", 35, 35, Casting.RAIN,
            "Calls a fall of cinders over a wide area."),
    KILNHEART("Kilnheart", Circle.KINDLING, ChatFormatting.RED, Items.FURNACE,
            "It holds the heat so you do not have to.", "health", 55, 30, Casting.WARD,
            "Sets a ring of heat that keeps things at arm's length."),
    STARFIRE("Starfire", Circle.KINDLING, ChatFormatting.WHITE, Items.FIREWORK_ROCKET,
            "Borrowed from something much larger.", "intelligence", 65, 60, Casting.METEOR,
            "Calls something down that should not fit in a spell."),

    // ── Circle of Ward: shields, denial, holding the line ──────────────────
    BULWARK_RUNE("Bulwark Rune", Circle.WARD, ChatFormatting.WHITE, Items.SHIELD,
            "Drawn by people who intend to stand still.", "defense", 60, 30, Casting.BARRIER,
            "Raises a dome that eats what comes at it."),
    THORNSEAL("Thornseal", Circle.WARD, ChatFormatting.DARK_GREEN, Items.SWEET_BERRIES,
            "It agrees to be hit exactly once.", "defense", 50, 25, Casting.REFLECT,
            "Returns a share of the next hits to whoever sent them."),
    STILLGLYPH("Stillglyph", Circle.WARD, ChatFormatting.AQUA, Items.BLUE_ICE,
            "Everything stops arguing for a moment.", "health", 60, 40, Casting.FREEZE,
            "Holds everything nearby exactly where it is."),
    OATHBAND("Oathband", Circle.WARD, ChatFormatting.GOLD, Items.GOLD_INGOT,
            "A promise with consequences attached.", "defense", 55, 35, Casting.SANCTUARY,
            "Marks ground you cannot be moved off."),
    HUSHMARK("Hushmark", Circle.WARD, ChatFormatting.GRAY, Items.WHITE_WOOL,
            "It takes the sound out of a room.", "intelligence", 50, 25, Casting.SILENCE,
            "Silences and blinds whatever is close."),
    IRONWRIT("Ironwrit", Circle.WARD, ChatFormatting.WHITE, Items.IRON_BLOCK,
            "Written once, on purpose, in something permanent.", "defense", 65, 45, Casting.PLATE,
            "Plates you until the Focus runs out."),
    TIDEWALL("Tidewall", Circle.WARD, ChatFormatting.BLUE, Items.PRISMARINE,
            "The sea has been doing this for a while.", "health", 65, 40, Casting.PUSH,
            "Pushes everything away from you at once."),
    CANDLEWATCH("Candlewatch", Circle.WARD, ChatFormatting.YELLOW, Items.LANTERN,
            "Somebody has to stay awake.", "intelligence", 55, 30, Casting.REVEAL,
            "Shows everything living nearby through walls."),
    LASTWORD("Lastword", Circle.WARD, ChatFormatting.LIGHT_PURPLE, Items.TOTEM_OF_UNDYING,
            "Saved for the moment it is needed.", "health", 70, 80, Casting.SECOND_WIND,
            "Spends everything to pull you back from a bad moment."),

    // ── Circle of Hollow: the dead, the drained, the owed ──────────────────
    GRAVEWRIT("Gravewrit", Circle.HOLLOW, ChatFormatting.DARK_GRAY, Items.BONE,
            "The paperwork of dying.", "strength", 40, 30, Casting.DRAIN,
            "Takes life from what is near and hands it to you."),
    SOULTITHE("Soultithe", Circle.HOLLOW, ChatFormatting.DARK_PURPLE, Items.SOUL_SAND,
            "Everybody pays it eventually.", "intelligence", 60, 40, Casting.HARVEST,
            "Pulls a share of life from everything in a wide ring."),
    PALEBRAND("Palebrand", Circle.HOLLOW, ChatFormatting.WHITE, Items.SKELETON_SKULL,
            "It makes a thing easier to finish.", "crit_damage", 55, 35, Casting.WITHER,
            "Rots a target so it stops recovering."),
    LICHKNOT("Lichknot", Circle.HOLLOW, ChatFormatting.DARK_GREEN, Items.WITHER_SKELETON_SKULL,
            "Tied so it cannot be untied.", "health", 60, 50, Casting.PHYLACTERY,
            "Stores the damage you take and gives it back later."),
    CARRIONMARK("Carrionmark", Circle.HOLLOW, ChatFormatting.DARK_RED, Items.ROTTEN_FLESH,
            "It calls the things that clean up.", "ferocity", 15, 35, Casting.SWARM,
            "Sends a cloud of hungry nothing at a target."),
    DIRGE("Dirge", Circle.HOLLOW, ChatFormatting.GRAY, Items.NOTE_BLOCK,
            "Slow, and everybody knows the words.", "intelligence", 55, 30, Casting.SLOW,
            "Drags at everything within earshot."),
    HOLLOWCUP("Hollowcup", Circle.HOLLOW, ChatFormatting.DARK_PURPLE, Items.GLASS_BOTTLE,
            "Never full, and always offered.", "crit_chance", 15, 40, Casting.SIPHON,
            "Turns damage you deal into Focus."),
    BONECHOIR("Bonechoir", Circle.HOLLOW, ChatFormatting.WHITE, Items.BONE_BLOCK,
            "They all remember the same song.", "strength", 45, 45, Casting.SUMMON,
            "Raises a circle of grasping hands around a point."),
    ENDNOTE("Endnote", Circle.HOLLOW, ChatFormatting.DARK_RED, Items.WITHER_ROSE,
            "The sentence that closes the book.", "crit_damage", 70, 70, Casting.EXECUTE,
            "Finishes anything already close to finished."),

    // ── Circle of Auspex: sight, knowing, arriving first ───────────────────
    FARSIGHT("Farsight", Circle.AUSPEX, ChatFormatting.AQUA, Items.SPYGLASS,
            "Distance is a rumour.", "intelligence", 60, 25, Casting.REVEAL,
            "Marks everything alive for a long while."),
    LEYMARK("Leymark", Circle.AUSPEX, ChatFormatting.LIGHT_PURPLE, Items.AMETHYST_SHARD,
            "The world has lines in it if you look.", "speed", 5, 30, Casting.BLINK,
            "Steps you forward through whatever is in the way."),
    OMENGLASS("Omenglass", Circle.AUSPEX, ChatFormatting.WHITE, Items.GLASS,
            "It shows the next thirty seconds, badly.", "crit_chance", 20, 35, Casting.FORESIGHT,
            "Your next few hits cannot miss and always crit."),
    THREADREAD("Threadread", Circle.AUSPEX, ChatFormatting.YELLOW, Items.STRING,
            "Follow it back and it explains everything.", "intelligence", 65, 30, Casting.TETHER,
            "Ties you to a spot you can snap back to."),
    STARCHART("Starchart", Circle.AUSPEX, ChatFormatting.BLUE, Items.FILLED_MAP,
            "Somebody wrote it down so you would not have to.", "speed", 5, 25, Casting.PATHFIND,
            "Marks the way to the nearest thing worth finding."),
    ECHOSCRIPT("Echoscript", Circle.AUSPEX, ChatFormatting.DARK_AQUA, Items.ECHO_SHARD,
            "It repeats what the room already said.", "intelligence", 70, 45, Casting.ECHO,
            "Repeats your last cast at no cost."),
    DOWSER("Dowser", Circle.AUSPEX, ChatFormatting.GOLD, Items.STICK,
            "Undignified and reliably correct.", "crit_chance", 15, 20, Casting.SEEK,
            "Points at whatever ore is nearest."),
    NIGHTREAD("Nightread", Circle.AUSPEX, ChatFormatting.DARK_BLUE, Items.ENDER_EYE,
            "The dark has never hidden anything from anyone patient.", "intelligence", 55, 20, Casting.NIGHT,
            "Gives you the dark as if it were daylight."),
    AUGURSEAL("Augurseal", Circle.AUSPEX, ChatFormatting.LIGHT_PURPLE, Items.ENDER_PEARL,
            "It is right often enough to be dangerous.", "crit_damage", 60, 55, Casting.RIFT,
            "Opens a rift that drags everything toward one point."),

    // ── Circle of Flux: change, growth, the shape of things ────────────────
    GREENWRIT("Greenwrit", Circle.FLUX, ChatFormatting.GREEN, Items.BONE_MEAL,
            "It asks the ground nicely and the ground says yes.", "health", 55, 20, Casting.GROW,
            "Everything growing nearby grows at once."),
    STONECHANGE("Stonechange", Circle.FLUX, ChatFormatting.GRAY, Items.STONE,
            "One thing becomes another and nobody is consulted.", "defense", 50, 35, Casting.TRANSMUTE,
            "Turns nearby stone into something better."),
    FLOWSCRIPT("Flowscript", Circle.FLUX, ChatFormatting.AQUA, Items.WATER_BUCKET,
            "It reminds a thing that it used to be liquid.", "speed", 5, 30, Casting.PUSH,
            "Sweeps a current outward from you."),
    SEEDCROWN("Seedcrown", Circle.FLUX, ChatFormatting.GREEN, Items.WHEAT_SEEDS,
            "Small, and extremely confident.", "health", 60, 30, Casting.HEAL,
            "Mends you and everyone standing with you."),
    MOULTMARK("Moultmark", Circle.FLUX, ChatFormatting.WHITE, Items.PHANTOM_MEMBRANE,
            "Leave the old shape behind and keep walking.", "speed", 5, 40, Casting.CLEANSE,
            "Strips everything wrong with you and speeds you up."),
    RUSTWRIT("Rustwrit", Circle.FLUX, ChatFormatting.GOLD, Items.COPPER_INGOT,
            "Time, but faster and on purpose.", "ferocity", 10, 35, Casting.CORRODE,
            "Eats the armour off whatever it touches."),
    BLOOMSEAL("Bloomseal", Circle.FLUX, ChatFormatting.LIGHT_PURPLE, Items.PINK_PETALS,
            "Brief, and worth the trouble.", "intelligence", 55, 30, Casting.BLESS,
            "Blesses everyone near you for a short while."),
    MIRESCRIPT("Mirescript", Circle.FLUX, ChatFormatting.DARK_GREEN, Items.MUD,
            "The ground stops cooperating with everyone but you.", "defense", 45, 30, Casting.MIRE,
            "Turns the floor into something nobody can cross."),
    WORLDTURN("Worldturn", Circle.FLUX, ChatFormatting.DARK_PURPLE, Items.NETHER_STAR,
            "The largest word in the book, used sparingly.", "intelligence", 80, 90, Casting.DOMAIN,
            "Collapses a wide space into one very bad second.");

    /** The five circles. Three pieces of a circle lights its standing bonus. */
    public enum Circle {
        KINDLING("Kindling", ChatFormatting.RED, "Casts cost less and burn considerably more."),
        WARD("Ward", ChatFormatting.WHITE, "Focus doubles as armour and refills when you are struck."),
        HOLLOW("Hollow", ChatFormatting.DARK_PURPLE, "Every cast takes a little life back for you."),
        AUSPEX("Auspex", ChatFormatting.AQUA, "Focus refills far quicker and casts reach further."),
        FLUX("Flux", ChatFormatting.GREEN, "Casts affect everyone standing with you, not only you.");

        public final String label;
        public final ChatFormatting colour;
        public final String bonusText;

        Circle(String label, ChatFormatting colour, String bonusText) {
            this.label = label;
            this.colour = colour;
            this.bonusText = bonusText;
        }
    }

    /** What a sigil does when you sneak and use it. */
    public enum Casting {
        FIREBOLT, PILLAR, TRAIL, CONE, BRAND, MEND, RAIN, WARD, METEOR,
        BARRIER, REFLECT, FREEZE, SANCTUARY, SILENCE, PLATE, PUSH, REVEAL, SECOND_WIND,
        DRAIN, HARVEST, WITHER, PHYLACTERY, SWARM, SLOW, SIPHON, SUMMON, EXECUTE,
        BLINK, FORESIGHT, TETHER, PATHFIND, ECHO, SEEK, NIGHT, RIFT,
        GROW, TRANSMUTE, HEAL, CLEANSE, CORRODE, BLESS, MIRE, DOMAIN
    }

    public final String display;
    public final Circle circle;
    public final ChatFormatting colour;
    public final Item token;
    public final String flavor;
    public final String statKey;
    public final int statAmount;
    /** Focus this casting costs. */
    public final int cost;
    public final Casting casting;
    public final String powerText;

    Sigil(String display, Circle circle, ChatFormatting colour, Item token, String flavor,
          String statKey, int statAmount, int cost, Casting casting, String powerText) {
        this.display = display;
        this.circle = circle;
        this.colour = colour;
        this.token = token;
        this.flavor = flavor;
        this.statKey = statKey;
        this.statAmount = statAmount;
        this.cost = cost;
        this.casting = casting;
        this.powerText = powerText;
    }

    public String key() {
        return name().toLowerCase(Locale.ROOT);
    }

    public GearLine.Motif motif() {
        return new GearLine.Motif(key(), display, colour, token, flavor,
                circle.label, circle.colour, circle.bonusText,
                java.util.List.of(GearLine.stat(statKey, statAmount)),
                "Sneak and use to cast. Costs " + cost + " Focus.");
    }

    public static Sigil byKey(String key) {
        if (key == null) return null;
        for (Sigil s : values()) {
            if (s.key().equalsIgnoreCase(key)) return s;
        }
        return null;
    }
}
