package com.political;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Locale;

/**
 * The forty five identities of the Tidecaller line.
 *
 * <p>Every piece feeds the same <b>Current</b> meter, which fills as you fight and spends
 * itself on whatever the piece in your hand does. Mixing identities across a set is a real
 * choice rather than a penalty, since the meter does not care which piece filled it.</p>
 */
public enum Tide {

    // ── Deep ──
    ABYSSAL_MAW("The Abyssal Maw", Group.DEEP, ChatFormatting.DARK_BLUE, Items.PRISMARINE_SHARD,
            "It has never needed light.", "strength", 55,
            "Current drags everything nearby into one place and bites."),
    DROWNED_CHOIR("The Drowned Choir", Group.DEEP, ChatFormatting.BLUE, Items.INK_SAC,
            "Forty voices, one lung between them.", "intelligence", 90,
            "Current sings, and everything that hears it forgets its target."),
    PRESSURE_CROWN("The Pressure Crown", Group.DEEP, ChatFormatting.DARK_AQUA, Items.HEART_OF_THE_SEA,
            "Worn by whatever the depth agreed to keep.", "defense", 70,
            "Current crushes the space around your target."),
    LIGHTLESS_REACH("The Lightless Reach", Group.DEEP, ChatFormatting.BLACK, Items.BLACK_DYE,
            "An arm long enough to have opinions.", "ferocity", 20,
            "Current pulls a distant enemy into your reach."),
    SUNKEN_BELL("The Sunken Bell", Group.DEEP, ChatFormatting.GRAY, Items.BELL,
            "Still ringing on a schedule nobody set.", "health", 70,
            "Current rings once and staggers everything that hears it."),
    TRENCH_WARDEN("The Trench Warden", Group.DEEP, ChatFormatting.DARK_GRAY, Items.TUBE_CORAL,
            "It guards a door that has no other side.", "defense", 80,
            "Current builds a wall of water you can stand behind."),
    HADAL_EYE("The Hadal Eye", Group.DEEP, ChatFormatting.DARK_PURPLE, Items.ENDER_EYE,
            "Open the whole time, seeing nothing, missing nothing.", "crit_chance", 25,
            "Current shows you exactly where the next hit should land."),
    COLD_BELOW("The Cold Below", Group.DEEP, ChatFormatting.AQUA, Items.BLUE_ICE,
            "Older than the water above it.", "crit_damage", 75,
            "Current freezes your target solid for a moment."),
    SILT_KING("The Silt King", Group.DEEP, ChatFormatting.GRAY, Items.CLAY_BALL,
            "He settles. That is the whole of it.", "health", 60,
            "Current buries your target to the waist."),

    // ── Shoal ──
    REEF_MOTHER("The Reef Mother", Group.SHOAL, ChatFormatting.GREEN, Items.BRAIN_CORAL,
            "She has been building since before the shore.", "health", 65,
            "Current grows coral that shields you and cuts them."),
    GLASS_SHOAL("The Glass Shoal", Group.SHOAL, ChatFormatting.AQUA, Items.GLASS_PANE,
            "Ten thousand of it, turning at once.", "speed", 6,
            "Current scatters you into the shoal and back."),
    BARBED_ANEMONE("The Barbed Anemone", Group.SHOAL, ChatFormatting.LIGHT_PURPLE, Items.FIRE_CORAL,
            "Pretty until you are inside it.", "ferocity", 15,
            "Current stings everything that touches you for six seconds."),
    TIDEPOOL_KEEPER("The Tidepool Keeper", Group.SHOAL, ChatFormatting.DARK_GREEN, Items.SEA_PICKLE,
            "Small water, absolute authority.", "intelligence", 70,
            "Current restores you and everything you are standing with."),
    KELP_GALLOWS("The Kelp Gallows", Group.SHOAL, ChatFormatting.DARK_GREEN, Items.KELP,
            "It only ever holds on.", "strength", 40,
            "Current roots your target where it stands."),
    PEARL_CHOIR("The Pearl Choir", Group.SHOAL, ChatFormatting.WHITE, Items.NAUTILUS_SHELL,
            "Layer on layer over the same complaint.", "defense", 55,
            "Current adds a shell that eats the next three hits."),
    NACRE_BLADE("The Nacre Blade", Group.SHOAL, ChatFormatting.WHITE, Items.NAUTILUS_SHELL,
            "Beautiful, and it will take your hand.", "crit_damage", 65,
            "Current opens a cut that will not close."),
    URCHIN_COURT("The Urchin Court", Group.SHOAL, ChatFormatting.DARK_PURPLE, Items.FIRE_CORAL,
            "Everyone here is a reason to leave.", "defense", 50,
            "Current answers every hit with a spine."),
    SALT_HERALD("The Salt Herald", Group.SHOAL, ChatFormatting.YELLOW, Items.SUGAR,
            "It arrives before the water does.", "speed", 5,
            "Current announces you, and they lose their footing."),

    // ── Surge ──
    STORM_SPINE("The Storm Spine", Group.SURGE, ChatFormatting.YELLOW, Items.LIGHTNING_ROD,
            "Straight up, and it means it.", "crit_damage", 80,
            "Current calls a bolt down your own arm."),
    RIPTIDE_FANG("The Riptide Fang", Group.SURGE, ChatFormatting.BLUE, Items.TRIDENT,
            "It only pulls one way.", "strength", 60,
            "Current drags your target out and back."),
    WHITEWATER_CROWN("The Whitewater Crown", Group.SURGE, ChatFormatting.WHITE, Items.SNOWBALL,
            "Noise made into a shape.", "speed", 7,
            "Current carries you forward through anything in the way."),
    UNDERTOW("The Undertow", Group.SURGE, ChatFormatting.DARK_BLUE, Items.GRAVEL,
            "The part of the sea that lies.", "strength", 50,
            "Current drags everything near your target down."),
    SQUALL_BEARER("The Squall Bearer", Group.SURGE, ChatFormatting.GRAY, Items.PHANTOM_MEMBRANE,
            "It brings weather as a gift.", "ferocity", 18,
            "Current whips a squall that blinds and cuts."),
    BREAKER_FIST("The Breaker Fist", Group.SURGE, ChatFormatting.AQUA, Items.PRISMARINE_BRICKS,
            "One blow, then it starts again.", "strength", 70,
            "Current lands a wave that flattens a line of them."),
    FOAM_DANCER("The Foam Dancer", Group.SURGE, ChatFormatting.WHITE, Items.WHITE_WOOL,
            "Weightless on purpose.", "speed", 8,
            "Current makes you untouchable for two seconds."),
    GALE_THROAT("The Gale Throat", Group.SURGE, ChatFormatting.LIGHT_PURPLE, Items.FEATHER,
            "It has been shouting since the first shore.", "intelligence", 85,
            "Current pushes everything away and holds it there."),
    TEMPEST_ANCHOR("The Tempest Anchor", Group.SURGE, ChatFormatting.DARK_AQUA, Items.IRON_BLOCK,
            "Heavy enough to argue with weather.", "defense", 75,
            "Current fixes you in place and nothing can move you."),

    // ── Glacier ──
    FROSTMERE("The Frostmere", Group.GLACIER, ChatFormatting.AQUA, Items.PACKED_ICE,
            "Still water that decided against it.", "defense", 65,
            "Current stills your target where it stands."),
    CALVING_FACE("The Calving Face", Group.GLACIER, ChatFormatting.WHITE, Items.ICE,
            "It leaves in pieces the size of houses.", "strength", 65,
            "Current drops a wall of ice on them."),
    RIME_CROWN("The Rime Crown", Group.GLACIER, ChatFormatting.AQUA, Items.POWDER_SNOW_BUCKET,
            "Grown in place, one night at a time.", "crit_chance", 22,
            "Current frosts your weapon so every hit slows."),
    BLUE_VEIN("The Blue Vein", Group.GLACIER, ChatFormatting.BLUE, Items.BLUE_ICE,
            "Compressed until it stopped being white.", "crit_damage", 85,
            "Current shatters what you froze."),
    LONG_WINTER("The Long Winter", Group.GLACIER, ChatFormatting.WHITE, Items.SNOW_BLOCK,
            "It does not end, it just pauses.", "health", 80,
            "Current keeps you standing through anything for four seconds."),
    MELTWATER_PATH("The Meltwater Path", Group.GLACIER, ChatFormatting.AQUA, Items.BUCKET,
            "It carves and never looks back.", "speed", 6,
            "Current cuts a path and everything on it is hit."),
    HOARFROST_VOW("The Hoarfrost Vow", Group.GLACIER, ChatFormatting.WHITE, Items.QUARTZ,
            "Made once, kept forever.", "intelligence", 95,
            "Current freezes your own cooldowns for six seconds."),
    ICEBERG_HEART("The Iceberg Heart", Group.GLACIER, ChatFormatting.DARK_AQUA, Items.BLUE_ICE,
            "Most of it is the part you did not see.", "defense", 90,
            "Current hides nine tenths of the damage you take."),
    SHIVERING_DEEP("The Shivering Deep", Group.GLACIER, ChatFormatting.DARK_BLUE, Items.SOUL_SAND,
            "Cold that thinks about it first.", "ferocity", 22,
            "Current makes everything nearby flinch."),

    // ── Myth ──
    LEVIATHAN_WAKE("The Leviathan Wake", Group.MYTH, ChatFormatting.DARK_PURPLE, Items.DRAGON_BREATH,
            "You see it after it has gone.", "strength", 90,
            "Current leaves a wake that hits everything behind you."),
    KRAKEN_SIGNET("The Kraken Signet", Group.MYTH, ChatFormatting.DARK_BLUE, Items.INK_SAC,
            "Eight signatures, all the same.", "ferocity", 25,
            "Current seizes up to eight enemies at once."),
    SIREN_LEDGER("The Siren Ledger", Group.MYTH, ChatFormatting.LIGHT_PURPLE, Items.WRITABLE_BOOK,
            "Every name it ever collected.", "intelligence", 110,
            "Current makes one enemy fight for you."),
    CHARYBDIS_RING("The Charybdis Ring", Group.MYTH, ChatFormatting.BLACK, Items.ENDER_PEARL,
            "It only takes.", "strength", 85,
            "Current opens a spiral that pulls and grinds."),
    NEREID_MIRROR("The Nereid Mirror", Group.MYTH, ChatFormatting.AQUA, Items.GLASS,
            "It shows the sea what it wants.", "crit_chance", 28,
            "Current copies your last hit onto every enemy near."),
    DROWNED_STAR("The Drowned Star", Group.MYTH, ChatFormatting.BLUE, Items.NETHER_STAR,
            "It fell in and kept burning.", "crit_damage", 100,
            "Current detonates the water around your target."),
    TIDEBOUND_OATH("The Tidebound Oath", Group.MYTH, ChatFormatting.DARK_AQUA, Items.HEART_OF_THE_SEA,
            "Sworn twice a day, forever.", "health", 100,
            "Current returns you to full once every long while."),
    MAELSTROM_CROWN("The Maelstrom Crown", Group.MYTH, ChatFormatting.DARK_BLUE, Items.CONDUIT,
            "Worn by whatever is at the centre.", "defense", 100,
            "Current turns everything around you into open water."),
    FIRST_WAVE("The First Wave", Group.MYTH, ChatFormatting.WHITE, Items.WATER_BUCKET,
            "Before it, there was only the shore.", "strength", 100,
            "Current sends the wave that started all of them.");

    /** The houses of the line, each with its own set bonus. */
    public enum Group {
        DEEP("Deep", ChatFormatting.AQUA, "Three of the deep depth: Current fills a quarter faster."),
        SHOAL("Shoal", ChatFormatting.AQUA, "Three of the shoal depth: Current fills a quarter faster."),
        SURGE("Surge", ChatFormatting.AQUA, "Three of the surge depth: Current fills a quarter faster."),
        GLACIER("Glacier", ChatFormatting.AQUA, "Three of the glacier depth: Current fills a quarter faster."),
        MYTH("Myth", ChatFormatting.AQUA, "Three of the myth depth: Current fills a quarter faster.");

        public final String display;
        public final ChatFormatting colour;
        public final String bonus;

        Group(String display, ChatFormatting colour, String bonus) {
            this.display = display;
            this.colour = colour;
            this.bonus = bonus;
        }
    }

    public final String display;
    public final Group group;
    public final ChatFormatting colour;
    public final Item token;
    public final String flavour;
    public final String statKind;
    public final int statAmount;
    public final String powerText;

    Tide(String display, Group group, ChatFormatting colour, Item token,
            String flavour, String statKind, int statAmount, String powerText) {
        this.display = display;
        this.group = group;
        this.colour = colour;
        this.token = token;
        this.flavour = flavour;
        this.statKind = statKind;
        this.statAmount = statAmount;
        this.powerText = powerText;
    }

    public String key() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static Tide byKey(String key) {
        if (key == null) return null;
        for (Tide v : values()) {
            if (v.key().equals(key)) return v;
        }
        return null;
    }

    /** The framework's view of this identity. */
    public GearLine.Motif motif() {
        return new GearLine.Motif(key(), display, colour, token, flavour,
                group.display, group.colour, group.bonus,
                List.of(GearLine.stat(statKind, statAmount)),
                "Current fills at 16 hits.");
    }
}
