package com.political;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.Locale;

/**
 * The forty five mechanisms of the Clockwork line.
 *
 * <p>Clockwork is the working line rather than the fighting one. A mechanism winds itself while you
 * carry it, building <b>Pressure</b>, and Pressure is spent by breaking blocks instead of by landing
 * hits. That puts the whole line on the mining and building side of the game, where Beastcraft sits
 * on the combat side and Starbound sits underneath both as a standing bonus.</p>
 */
public enum Mechanism {

    // ── Foundry works: heat, ore, the making of metal ──────────────────────
    BLAST_CORE("The Blast Core", Works.FOUNDRY, ChatFormatting.RED, Items.BLAST_FURNACE,
            "Hot enough that the argument is over.", "strength", 45, Duty.SMELT,
            "Ore breaks already smelted."),
    SMELTER_DRUM("The Smelter Drum", Works.FOUNDRY, ChatFormatting.GOLD, Items.FURNACE,
            "It turns slowly and never stops turning.", "defense", 40, Duty.SMELT,
            "Ore breaks already smelted, and sometimes twice over."),
    SLAG_TAP("The Slag Tap", Works.FOUNDRY, ChatFormatting.DARK_GRAY, Items.MAGMA_BLOCK,
            "Everything useless has to go somewhere.", "health", 50, Duty.VENT,
            "Venting Pressure burns whatever is standing too close."),
    INGOT_PRESS("The Ingot Press", Works.FOUNDRY, ChatFormatting.WHITE, Items.IRON_BLOCK,
            "One shape, endlessly, correctly.", "defense", 55, Duty.COMPRESS,
            "Broken stone compacts itself into the useful version."),
    COKE_BED("The Coke Bed", Works.FOUNDRY, ChatFormatting.DARK_GRAY, Items.COAL_BLOCK,
            "Patient fuel for impatient work.", "strength", 35, Duty.FUEL,
            "Pressure winds twice as fast underground."),
    FLUX_HOPPER("The Flux Hopper", Works.FOUNDRY, ChatFormatting.YELLOW, Items.HOPPER,
            "It knows the recipe better than you do.", "intelligence", 50, Duty.MAGNET,
            "Everything you break comes straight to you."),
    BLOOM_HAMMER("The Bloom Hammer", Works.FOUNDRY, ChatFormatting.GRAY, Items.ANVIL,
            "It hits the metal until the metal agrees.", "ferocity", 10, Duty.SHATTER,
            "Full Pressure breaks a three wide face at once."),
    QUENCH_LINE("The Quench Line", Works.FOUNDRY, ChatFormatting.AQUA, Items.WATER_BUCKET,
            "The moment the work decides what it is.", "health", 55, Duty.TEMPER,
            "Venting Pressure hardens you for a while."),
    CINDER_VENT("The Cinder Vent", Works.FOUNDRY, ChatFormatting.GOLD, Items.CAMPFIRE,
            "The building breathes out.", "crit_damage", 45, Duty.VENT,
            "Venting throws a ring of embers outward."),

    // ── Engine works: motion, force, the parts that move ───────────────────
    PISTON_BANK("The Piston Bank", Works.ENGINE, ChatFormatting.GRAY, Items.PISTON,
            "Eight of them, agreeing.", "strength", 50, Duty.SHATTER,
            "Full Pressure drives a shaft three blocks deep."),
    FLYWHEEL("The Flywheel", Works.ENGINE, ChatFormatting.WHITE, Items.IRON_INGOT,
            "It remembers the last hour of effort.", "speed", 5, Duty.MOMENTUM,
            "Pressure keeps winding for a while after you stop."),
    BOILER("The Boiler", Works.ENGINE, ChatFormatting.RED, Items.CAULDRON,
            "Watched, always, by someone nervous.", "health", 60, Duty.OVERPRESSURE,
            "Holding full Pressure hurts, and hits much harder."),
    GOVERNOR("The Governor", Works.ENGINE, ChatFormatting.DARK_GRAY, Items.LEVER,
            "It exists to say no.", "defense", 50, Duty.REGULATE,
            "Pressure never overruns and never wastes."),
    CRANK_SHAFT("The Crank Shaft", Works.ENGINE, ChatFormatting.GOLD, Items.COPPER_INGOT,
            "It turns a push into a circle.", "haste", 1, Duty.HASTE,
            "Your hands stay quick while Pressure is above half."),
    CAMSHAFT("The Camshaft", Works.ENGINE, ChatFormatting.YELLOW, Items.COPPER_BLOCK,
            "Timing, made physical.", "crit_chance", 15, Duty.TIMING,
            "Every fourth block broken pays double."),
    PRESSURE_VALVE("The Pressure Valve", Works.ENGINE, ChatFormatting.AQUA, Items.IRON_TRAPDOOR,
            "The last honest part of the machine.", "defense", 45, Duty.VENT,
            "You can vent early, for a smaller effect."),
    DRIVE_BELT("The Drive Belt", Works.ENGINE, ChatFormatting.DARK_GRAY, Items.LEATHER,
            "It does the boring half of everything.", "speed", 5, Duty.MOMENTUM,
            "Pressure carries across tools instead of resetting."),
    TURBINE("The Turbine", Works.ENGINE, ChatFormatting.WHITE, Items.QUARTZ_BLOCK,
            "Wind, taken seriously.", "intelligence", 60, Duty.LIFT,
            "Full Pressure throws you up and sets you down gently."),

    // ── Circuit works: signal, logic, memory ───────────────────────────────
    LOGIC_SLATE("The Logic Slate", Works.CIRCUIT, ChatFormatting.DARK_RED, Items.REDSTONE,
            "It has already worked out what you meant.", "intelligence", 65, Duty.SORT,
            "Broken drops arrive sorted into what you already carry."),
    COMPARATOR("The Comparator", Works.CIRCUIT, ChatFormatting.RED, Items.COMPARATOR,
            "It only cares which is larger.", "crit_chance", 15, Duty.TIMING,
            "The rarer the block, the more Pressure it pays."),
    LATCH("The Latch", Works.CIRCUIT, ChatFormatting.GRAY, Items.REPEATER,
            "It holds a decision for you.", "defense", 45, Duty.REGULATE,
            "Pressure holds at full instead of discharging early."),
    BUS_LINE("The Bus Line", Works.CIRCUIT, ChatFormatting.DARK_GRAY, Items.REDSTONE_BLOCK,
            "Everything talks over everything else.", "haste", 1, Duty.HASTE,
            "Pressure powers your speed as well as your tools."),
    CAPACITOR("The Capacitor", Works.CIRCUIT, ChatFormatting.YELLOW, Items.LIGHTNING_ROD,
            "Patient, then suddenly not.", "crit_damage", 55, Duty.OVERPRESSURE,
            "Pressure caps higher and discharges much harder."),
    OSCILLATOR("The Oscillator", Works.CIRCUIT, ChatFormatting.AQUA, Items.NOTE_BLOCK,
            "It never once considered stopping.", "speed", 5, Duty.MOMENTUM,
            "Pressure winds even while you stand still."),
    RELAY("The Relay", Works.CIRCUIT, ChatFormatting.WHITE, Items.OBSERVER,
            "It passes the message and takes no credit.", "intelligence", 55, Duty.CHAIN,
            "Full Pressure spreads the break to matching blocks nearby."),
    FUSE("The Fuse", Works.CIRCUIT, ChatFormatting.GOLD, Items.TNT,
            "It fails so the rest does not.", "health", 55, Duty.TEMPER,
            "A hit that would break you spends all Pressure instead."),
    MEMORY_CORE("The Memory Core", Works.CIRCUIT, ChatFormatting.LIGHT_PURPLE, Items.ECHO_SHARD,
            "It kept everything, including the mistakes.", "intelligence", 70, Duty.RECALL,
            "Full Pressure returns you to where you started digging."),

    // ── Optic works: light, focus, sight ───────────────────────────────────
    LENS_ARRAY("The Lens Array", Works.OPTIC, ChatFormatting.AQUA, Items.GLASS,
            "Everything at once, in the right place.", "intelligence", 60, Duty.SURVEY,
            "Ore inside the stone glows for you."),
    PRISM_RACK("The Prism Rack", Works.OPTIC, ChatFormatting.LIGHT_PURPLE, Items.AMETHYST_BLOCK,
            "One light, argued into seven.", "crit_damage", 50, Duty.BEAM,
            "Full Pressure fires a beam that bores through stone."),
    MIRROR_WALL("The Mirror Wall", Works.OPTIC, ChatFormatting.WHITE, Items.GLASS_PANE,
            "It returns exactly what it is given.", "defense", 50, Duty.TEMPER,
            "Venting throws part of the damage back."),
    FOCUS_RING("The Focus Ring", Works.OPTIC, ChatFormatting.YELLOW, Items.GOLD_NUGGET,
            "Turn it until the world stops arguing.", "crit_chance", 20, Duty.TIMING,
            "Pressure spends on precision instead of force."),
    APERTURE("The Aperture", Works.OPTIC, ChatFormatting.DARK_GRAY, Items.SPYGLASS,
            "It decides how much of the world gets in.", "defense", 45, Duty.SURVEY,
            "You see what is worth digging toward."),
    BEAM_SPLITTER("The Beam Splitter", Works.OPTIC, ChatFormatting.AQUA, Items.PRISMARINE_CRYSTALS,
            "Two answers from one question.", "ferocity", 10, Duty.CHAIN,
            "Full Pressure breaks two seams at once."),
    SCOPE("The Scope", Works.OPTIC, ChatFormatting.GREEN, Items.ENDER_EYE,
            "Far away stops mattering.", "crit_chance", 15, Duty.SURVEY,
            "Rare blocks are marked from much further out."),
    FILAMENT("The Filament", Works.OPTIC, ChatFormatting.GOLD, Items.GLOWSTONE_DUST,
            "It gives light by being consumed.", "health", 45, Duty.LIGHT,
            "Where you dig lights itself behind you."),
    SIGNAL_LAMP("The Signal Lamp", Works.OPTIC, ChatFormatting.YELLOW, Items.REDSTONE_LAMP,
            "The whole valley can read it.", "intelligence", 50, Duty.LIGHT,
            "Venting lights a wide area and blinds what lives in it."),

    // ── Automaton works: the machine that runs itself ──────────────────────
    GEAR_TRAIN("The Gear Train", Works.AUTOMATON, ChatFormatting.GOLD, Items.IRON_NUGGET,
            "Nothing in it knows what it is for.", "haste", 1, Duty.HASTE,
            "Every tooth turns a little faster than the last."),
    ESCAPEMENT("The Escapement", Works.AUTOMATON, ChatFormatting.WHITE, Items.CLOCK,
            "It lets the spring go, one moment at a time.", "crit_chance", 15, Duty.REGULATE,
            "Pressure spends evenly instead of all at once."),
    MAINSPRING("The Mainspring", Works.AUTOMATON, ChatFormatting.GRAY, Items.IRON_BARS,
            "Wound tight and pretending otherwise.", "strength", 45, Duty.OVERPRESSURE,
            "Pressure winds much faster and hits much harder."),
    ARMATURE("The Armature", Works.AUTOMATON, ChatFormatting.DARK_GRAY, Items.IRON_BARS,
            "The shape the machine wears.", "defense", 55, Duty.TEMPER,
            "Full Pressure plates you until it is spent."),
    SERVO("The Servo", Works.AUTOMATON, ChatFormatting.AQUA, Items.DISPENSER,
            "It does the small thing perfectly, forever.", "speed", 5, Duty.MAGNET,
            "Drops are gathered without you stopping."),
    LINKAGE("The Linkage", Works.AUTOMATON, ChatFormatting.WHITE, Items.STICK,
            "Simple, and impossible to improve.", "ferocity", 10, Duty.CHAIN,
            "Full Pressure carries the break along the seam."),
    CHASSIS("The Chassis", Works.AUTOMATON, ChatFormatting.GRAY, Items.NETHERITE_SCRAP,
            "Everything else bolts onto this.", "health", 70, Duty.REGULATE,
            "Pressure protects you before it does anything else."),
    RATCHET("The Ratchet", Works.AUTOMATON, ChatFormatting.YELLOW, Items.TRIPWIRE_HOOK,
            "It refuses to give ground back.", "strength", 40, Duty.MOMENTUM,
            "Pressure never falls below whatever you last reached."),
    PINION("The Pinion", Works.AUTOMATON, ChatFormatting.GOLD, Items.GOLD_INGOT,
            "Small, and the whole thing waits on it.", "intelligence", 55, Duty.SORT,
            "Pressure sorts and stacks whatever you bring up.");

    /** The five works. Three pieces of a works lights its standing bonus. */
    public enum Works {
        FOUNDRY("Foundry", ChatFormatting.RED, 60,
                "Ore pays out more and stone stops slowing you."),
        ENGINE("Engine", ChatFormatting.GOLD, 80,
                "Pressure winds faster and carries further."),
        CIRCUIT("Circuit", ChatFormatting.DARK_RED, 100,
                "Everything you break is counted, sorted and remembered."),
        OPTIC("Optic", ChatFormatting.AQUA, 70,
                "The stone stops hiding what is inside it."),
        AUTOMATON("Automaton", ChatFormatting.GRAY, 90,
                "The work continues whether you are paying attention or not.");

        public final String label;
        public final ChatFormatting colour;
        /** Pressure needed before a discharge. */
        public final int capacity;
        public final String bonusText;

        Works(String label, ChatFormatting colour, int capacity, String bonusText) {
            this.label = label;
            this.colour = colour;
            this.capacity = capacity;
            this.bonusText = bonusText;
        }
    }

    /** What a mechanism does when Pressure discharges. */
    public enum Duty {
        SMELT, VENT, COMPRESS, FUEL, MAGNET, SHATTER, TEMPER,
        MOMENTUM, OVERPRESSURE, REGULATE, HASTE, TIMING, LIFT,
        SORT, CHAIN, RECALL, SURVEY, BEAM, LIGHT
    }

    public final String display;
    public final Works works;
    public final ChatFormatting colour;
    public final Item token;
    public final String flavor;
    public final String statKey;
    public final int statAmount;
    public final Duty duty;
    public final String powerText;

    Mechanism(String display, Works works, ChatFormatting colour, Item token, String flavor,
              String statKey, int statAmount, Duty duty, String powerText) {
        this.display = display;
        this.works = works;
        this.colour = colour;
        this.token = token;
        this.flavor = flavor;
        this.statKey = statKey;
        this.statAmount = statAmount;
        this.duty = duty;
        this.powerText = powerText;
    }

    public String key() {
        return name().toLowerCase(Locale.ROOT);
    }

    public GearLine.Motif motif() {
        java.util.List<String> stats = statKey.equals("haste")
                ? java.util.List.of()
                : java.util.List.of(GearLine.stat(statKey, statAmount));
        return new GearLine.Motif(key(), display, colour, token, flavor,
                works.label, works.colour, works.bonusText, stats,
                "Pressure caps at " + works.capacity + ".");
    }

    public static Mechanism byKey(String key) {
        if (key == null) return null;
        for (Mechanism m : values()) {
            if (m.key().equalsIgnoreCase(key)) return m;
        }
        return null;
    }
}
