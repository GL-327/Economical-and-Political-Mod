package com.political;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.Locale;

/**
 * The forty five beasts of the Beastcraft line.
 *
 * <p>Beastcraft is the loud counterpart to the Starbound line. Nothing here is always on. Instead
 * every hit you land feeds <b>Instinct</b>, and when Instinct fills the beast in your hand takes
 * over for a moment and does what that beast does. Different clades fill Instinct at different
 * speeds, so a swarm piece spends often and small while a leviathan piece spends rarely and huge.</p>
 */
public enum Beast {

    // ── Predator clade: pursuit, teeth, the short fight ────────────────────
    DIREWOLF("The Direwolf", Clade.PREDATOR, ChatFormatting.GRAY, Items.BONE,
            "It runs with the pack it refuses to lead.", "strength", 40, Trait.PACK_RUSH,
            "Instinct calls three spectral hounds that worry your target."),
    SABRECAT("The Sabrecat", Clade.PREDATOR, ChatFormatting.GOLD, Items.LEATHER,
            "Patience, then twelve feet of it at once.", "crit_damage", 50, Trait.AMBUSH,
            "Instinct lands a pounce that always crits."),
    RIPPERBEAK("The Ripperbeak", Clade.PREDATOR, ChatFormatting.RED, Items.FEATHER,
            "A bird that decided teeth were the better idea.", "ferocity", 15, Trait.REND,
            "Instinct opens a wound that bleeds for six seconds."),
    BLACKMANE("The Blackmane", Clade.PREDATOR, ChatFormatting.DARK_GRAY, Items.BLACK_WOOL,
            "The quiet one at the edge of the firelight.", "strength", 45, Trait.STALK,
            "Instinct hides your approach and doubles the next hit."),
    HOLLOWJAW("The Hollowjaw", Clade.PREDATOR, ChatFormatting.WHITE, Items.SKELETON_SKULL,
            "It eats and stays empty.", "crit_chance", 15, Trait.FEED,
            "Instinct drains life from everything close and hands it to you."),
    SPLITHOOF("The Splithoof", Clade.PREDATOR, ChatFormatting.YELLOW, Items.RABBIT_HIDE,
            "Fast enough that the ground complains.", "speed", 5, Trait.TRAMPLE,
            "Instinct charges forward and flattens what is in the way."),
    THORNCAT("The Thorncat", Clade.PREDATOR, ChatFormatting.DARK_GREEN, Items.SWEET_BERRIES,
            "Beautiful, and covered in reasons not to touch it.", "defense", 45, Trait.GORE,
            "Instinct answers your attacker with a spray of quills."),
    NIGHTHOUND("The Nighthound", Clade.PREDATOR, ChatFormatting.DARK_PURPLE, Items.ENDER_PEARL,
            "It arrives before its own footsteps.", "crit_chance", 20, Trait.PHASE,
            "Instinct steps you behind whatever you were looking at."),
    GRIMSTALKER("The Grimstalker", Clade.PREDATOR, ChatFormatting.DARK_RED, Items.REDSTONE,
            "It has been following since you left.", "crit_damage", 60, Trait.HOWL,
            "Instinct freezes everything nearby with a sound they cannot place."),

    // ── Swarm clade: many small, endlessly ─────────────────────────────────
    HIVEQUEEN("The Hivequeen", Clade.SWARM, ChatFormatting.YELLOW, Items.HONEYCOMB,
            "She has never had to ask twice.", "intelligence", 55, Trait.SWARMCALL,
            "Instinct sends a cloud of workers to sting whatever you are fighting."),
    LOCUSTFATHER("The Locust Father", Clade.SWARM, ChatFormatting.GREEN, Items.WHEAT,
            "Where he goes the year ends early.", "ferocity", 10, Trait.SWARMCALL,
            "Instinct strips a field of enemies down to nothing."),
    GLASSWING("The Glasswing", Clade.SWARM, ChatFormatting.AQUA, Items.GLASS_PANE,
            "Visible only when it turns.", "speed", 5, Trait.MOLT,
            "Instinct sheds a decoy and leaves you somewhere safer."),
    CARRIONFLY("The Carrionfly", Clade.SWARM, ChatFormatting.DARK_GREEN, Items.ROTTEN_FLESH,
            "It is grateful for your worst day.", "strength", 30, Trait.VENOM,
            "Instinct leaves a spreading rot on everything near the target."),
    SPINEMITE("The Spinemite", Clade.SWARM, ChatFormatting.GRAY, Items.FLINT,
            "Small, sharp and extremely numerous.", "crit_chance", 10, Trait.STING,
            "Instinct fires a volley of splinters in a wide arc."),
    LANTERNMOTH("The Lantern Moth", Clade.SWARM, ChatFormatting.GOLD, Items.GLOWSTONE_DUST,
            "It carries its own funeral.", "intelligence", 45, Trait.EMBER,
            "Instinct hangs burning motes that chase whoever comes near."),
    BROODSPIDER("The Broodspider", Clade.SWARM, ChatFormatting.DARK_PURPLE, Items.STRING,
            "One of her is a problem. There is never one.", "crit_damage", 40, Trait.CONSTRICT,
            "Instinct webs your target in place and squeezes."),
    MIRECRAWLER("The Mirecrawler", Clade.SWARM, ChatFormatting.DARK_GRAY, Items.CLAY_BALL,
            "The ground here is not ground.", "defense", 40, Trait.ROOT,
            "Instinct turns the floor under your enemies into a grasping mire."),
    ASHWASP("The Ashwasp", Clade.SWARM, ChatFormatting.RED, Items.BLAZE_POWDER,
            "It nests in things that are still burning.", "ferocity", 15, Trait.EMBER,
            "Instinct sets a ring of cinders that clings and burns."),

    // ── Leviathan clade: slow, enormous, final ─────────────────────────────
    DEEPMOTHER("The Deepmother", Clade.LEVIATHAN, ChatFormatting.DARK_AQUA, Items.HEART_OF_THE_SEA,
            "Older than the coastline and less interested.", "health", 85, Trait.TIDE,
            "Instinct pulls a wave through everything in front of you."),
    STONEBACK("The Stoneback", Clade.LEVIATHAN, ChatFormatting.GRAY, Items.DEEPSLATE,
            "Islands have grown on things like this.", "defense", 75, Trait.CARAPACE,
            "Instinct plates you in living shell that eats the next four hits."),
    GULFMAW("The Gulfmaw", Clade.LEVIATHAN, ChatFormatting.BLUE, Items.PRISMARINE_SHARD,
            "It does not chase. It simply opens.", "strength", 60, Trait.DROWN,
            "Instinct swallows the space ahead and drags everything inward."),
    TIDEWYRM("The Tidewyrm", Clade.LEVIATHAN, ChatFormatting.AQUA, Items.KELP,
            "The current with an opinion.", "ferocity", 20, Trait.TIDE,
            "Instinct sends a coiling surge that strikes three times."),
    BONEREEF("The Bonereef", Clade.LEVIATHAN, ChatFormatting.WHITE, Items.BONE_BLOCK,
            "Built out of everything that tried before you.", "health", 90, Trait.ROOT,
            "Instinct raises a reef of bone that pins whatever touches it."),
    SALTBEHEMOTH("The Salt Behemoth", Clade.LEVIATHAN, ChatFormatting.WHITE, Items.QUARTZ,
            "It dried out and kept walking.", "defense", 70, Trait.QUAKE,
            "Instinct drops a shock through the floor in every direction."),
    ABYSSEYE("The Abysseye", Clade.LEVIATHAN, ChatFormatting.DARK_PURPLE, Items.ENDER_EYE,
            "It has been looking up for a long time.", "intelligence", 80, Trait.PETRIFY,
            "Instinct fixes everything looking at you where it stands."),
    THUNDERFIN("The Thunderfin", Clade.LEVIATHAN, ChatFormatting.YELLOW, Items.LIGHTNING_ROD,
            "The storm learned to swim.", "crit_damage", 65, Trait.GALE,
            "Instinct calls a strike down the line you are facing."),
    OLDSHELL("The Oldshell", Clade.LEVIATHAN, ChatFormatting.DARK_GREEN, Items.TURTLE_SCUTE,
            "It has outlived four names for the sea.", "health", 80, Trait.CARAPACE,
            "Instinct hardens you and pushes back everything that closes in."),

    // ── Wyrm clade: hoard, flame, ruin ─────────────────────────────────────
    EMBERWYRM("The Emberwyrm", Clade.WYRM, ChatFormatting.RED, Items.MAGMA_CREAM,
            "Sleeping on money, dreaming of more.", "strength", 55, Trait.EMBER,
            "Instinct breathes a cone of fire that lingers on the ground."),
    FROSTWYRM("The Frostwyrm", Clade.WYRM, ChatFormatting.AQUA, Items.BLUE_ICE,
            "It exhales and the argument stops.", "defense", 60, Trait.PETRIFY,
            "Instinct freezes the ground and everything standing on it."),
    STORMWYRM("The Stormwyrm", Clade.WYRM, ChatFormatting.YELLOW, Items.COPPER_INGOT,
            "It lives where the weather is made.", "crit_damage", 70, Trait.GALE,
            "Instinct throws a chain of lightning between nearby enemies."),
    GRAVEWYRM("The Gravewyrm", Clade.WYRM, ChatFormatting.DARK_GRAY, Items.SOUL_SAND,
            "It hoards names instead of gold.", "intelligence", 70, Trait.FEED,
            "Instinct takes a portion of everything it hurts and gives it to you."),
    GOLDWYRM("The Goldwyrm", Clade.WYRM, ChatFormatting.GOLD, Items.GOLD_BLOCK,
            "It counts you as inventory.", "strength", 50, Trait.CRUSH,
            "Instinct slams a weight of gold down on your target."),
    GLASSWYRM("The Glasswyrm", Clade.WYRM, ChatFormatting.WHITE, Items.AMETHYST_SHARD,
            "You can see straight through it and still not find it.", "crit_chance", 20, Trait.PHASE,
            "Instinct splits you into three and only one of them is real."),
    ROOTWYRM("The Rootwyrm", Clade.WYRM, ChatFormatting.DARK_GREEN, Items.MOSS_BLOCK,
            "It has been under the forest since the forest.", "health", 75, Trait.ROOT,
            "Instinct erupts roots that hold and drain what they catch."),
    VOIDWYRM("The Voidwyrm", Clade.WYRM, ChatFormatting.DARK_PURPLE, Items.CHORUS_FRUIT,
            "It stopped needing a place to be.", "intelligence", 85, Trait.PHASE,
            "Instinct tears a rift that pulls in everything close."),
    SUNWYRM("The Sunwyrm", Clade.WYRM, ChatFormatting.YELLOW, Items.GLOWSTONE,
            "Looking at it is a decision.", "ferocity", 20, Trait.EMBER,
            "Instinct calls a pillar of light down on the target."),

    // ── Spirit clade: the beasts nobody can point at ───────────────────────
    PALEHART("The Palehart", Clade.SPIRIT, ChatFormatting.WHITE, Items.WHITE_WOOL,
            "It leaves prints and nothing else.", "health", 65, Trait.MOLT,
            "Instinct mends you and clears what was wrong with you."),
    ECHOSTAG("The Echostag", Clade.SPIRIT, ChatFormatting.AQUA, Items.ECHO_SHARD,
            "You hear it a second after it passes.", "intelligence", 65, Trait.SCREECH,
            "Instinct sends a pulse that staggers everything it reaches."),
    ASHMOTHER("The Ashmother", Clade.SPIRIT, ChatFormatting.GRAY, Items.GUNPOWDER,
            "She counts what is left after.", "crit_damage", 55, Trait.FEED,
            "Instinct burns your target and heals you for what it took."),
    LANTERNSHADE("The Lanternshade", Clade.SPIRIT, ChatFormatting.GOLD, Items.LANTERN,
            "It carries a light for people who are lost. Then it moves.", "intelligence", 60, Trait.SCREECH,
            "Instinct blinds and scatters whatever was closing on you."),
    THRENODY("The Threnody", Clade.SPIRIT, ChatFormatting.DARK_PURPLE, Items.NOTE_BLOCK,
            "A grief that learned to walk.", "strength", 45, Trait.HOWL,
            "Instinct roots everything nearby in a moment of grief."),
    HOLLOWCROWN("The Hollow Crown", Clade.SPIRIT, ChatFormatting.YELLOW, Items.GOLDEN_HELMET,
            "It rules nothing and has never accepted that.", "defense", 55, Trait.CARAPACE,
            "Instinct crowns you and turns damage back on the sender."),
    DUSKMARE("The Duskmare", Clade.SPIRIT, ChatFormatting.DARK_BLUE, Items.PHANTOM_MEMBRANE,
            "It only runs at the hour you cannot name.", "speed", 5, Trait.STALK,
            "Instinct carries you forward faster than anything can follow."),
    SILENTHERD("The Silent Herd", Clade.SPIRIT, ChatFormatting.GRAY, Items.LEATHER_HORSE_ARMOR,
            "Many of them, and no sound at all.", "ferocity", 15, Trait.TRAMPLE,
            "Instinct runs a soundless stampede straight through your enemies."),
    LASTBEAST("The Last Beast", Clade.SPIRIT, ChatFormatting.DARK_RED, Items.NETHER_STAR,
            "There is a final one of everything.", "crit_damage", 75, Trait.CRUSH,
            "Instinct ends the fight in one strike, or comes very close.");

    /** The five clades. Three pieces of a clade lights its standing bonus. */
    public enum Clade {
        PREDATOR("Predator", ChatFormatting.RED, 10,
                "Your hits land harder the longer a fight runs."),
        SWARM("Swarm", ChatFormatting.YELLOW, 6,
                "Instinct fills faster and spends smaller."),
        LEVIATHAN("Leviathan", ChatFormatting.DARK_AQUA, 16,
                "You take far less and give back far more when Instinct breaks."),
        WYRM("Wyrm", ChatFormatting.GOLD, 12,
                "Coin and burning things both come easier to you."),
        SPIRIT("Spirit", ChatFormatting.LIGHT_PURPLE, 9,
                "Instinct also mends you, and the dead notice you less.");

        public final String label;
        public final ChatFormatting colour;
        /** Hits needed to fill Instinct for this clade. */
        public final int threshold;
        public final String bonusText;

        Clade(String label, ChatFormatting colour, int threshold, String bonusText) {
            this.label = label;
            this.colour = colour;
            this.threshold = threshold;
            this.bonusText = bonusText;
        }
    }

    /** What a beast does when Instinct fills. */
    public enum Trait {
        PACK_RUSH, AMBUSH, REND, STALK, FEED, TRAMPLE, GORE, PHASE, HOWL,
        SWARMCALL, MOLT, VENOM, STING, EMBER, CONSTRICT, ROOT,
        TIDE, CARAPACE, DROWN, QUAKE, PETRIFY, GALE, CRUSH, SCREECH
    }

    public final String display;
    public final Clade clade;
    public final ChatFormatting colour;
    public final Item token;
    public final String flavor;
    public final String statKey;
    public final int statAmount;
    public final Trait trait;
    public final String powerText;

    Beast(String display, Clade clade, ChatFormatting colour, Item token, String flavor,
          String statKey, int statAmount, Trait trait, String powerText) {
        this.display = display;
        this.clade = clade;
        this.colour = colour;
        this.token = token;
        this.flavor = flavor;
        this.statKey = statKey;
        this.statAmount = statAmount;
        this.trait = trait;
        this.powerText = powerText;
    }

    public String key() {
        return name().toLowerCase(Locale.ROOT);
    }

    public GearLine.Motif motif() {
        return new GearLine.Motif(key(), display, colour, token, flavor,
                clade.label, clade.colour, clade.bonusText,
                java.util.List.of(GearLine.stat(statKey, statAmount)),
                "Instinct fills at " + clade.threshold + " hits.");
    }

    public static Beast byKey(String key) {
        if (key == null) return null;
        for (Beast b : values()) {
            if (b.key().equalsIgnoreCase(key)) return b;
        }
        return null;
    }
}
