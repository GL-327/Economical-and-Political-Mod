package com.political;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Locale;

/**
 * The forty five identities of the Thornwood line.
 *
 * <p>Every piece feeds the same <b>Growth</b> meter, which fills as you fight and spends
 * itself on whatever the piece in your hand does. Mixing identities across a set is a real
 * choice rather than a penalty, since the meter does not care which piece filled it.</p>
 */
public enum Thorn {

    // ── Grove ──
    ROOTFATHER("The Rootfather", Group.GROVE, ChatFormatting.DARK_GREEN, Items.OAK_LOG,
            "Down before he was ever up.", "health", 70,
            "Growth roots everything around you to the spot."),
    GREEN_LEDGER("The Green Ledger", Group.GROVE, ChatFormatting.GREEN, Items.MOSS_BLOCK,
            "Every season, written in rings.", "intelligence", 80,
            "Growth remembers your last three hits and repeats one."),
    BRAMBLE_COURT("The Bramble Court", Group.GROVE, ChatFormatting.DARK_GREEN, Items.SWEET_BERRIES,
            "Everyone in it is a reason to go around.", "defense", 65,
            "Growth wraps you in thorns that answer every hit."),
    SAP_VOW("The Sap Vow", Group.GROVE, ChatFormatting.GOLD, Items.HONEY_BOTTLE,
            "Slow, and it gets everywhere.", "strength", 45,
            "Growth slows your target until it can barely swing."),
    CANOPY_CROWN("The Canopy Crown", Group.GROVE, ChatFormatting.GREEN, Items.JUNGLE_LEAVES,
            "It decides what the ground sees.", "defense", 70,
            "Growth shades you and projectiles lose their way."),
    HOLLOW_TRUNK("The Hollow Trunk", Group.GROVE, ChatFormatting.DARK_GRAY, Items.STRIPPED_OAK_LOG,
            "Empty, and still standing.", "health", 80,
            "Growth hides you inside a trunk and heals you there."),
    SEEDBEARER("The Seedbearer", Group.GROVE, ChatFormatting.YELLOW, Items.WHEAT_SEEDS,
            "It only ever plans ahead.", "intelligence", 85,
            "Growth plants a seed in your target that bursts later."),
    DEADWOOD_WARDEN("The Deadwood Warden", Group.GROVE, ChatFormatting.GRAY, Items.STRIPPED_SPRUCE_LOG,
            "It guards what it used to be.", "defense", 75,
            "Growth raises a deadwood wall between you and them."),
    MYCELIUM_CHOIR("The Mycelium Choir", Group.GROVE, ChatFormatting.LIGHT_PURPLE, Items.MYCELIUM,
            "One organism, everywhere, quietly.", "ferocity", 18,
            "Growth spreads your damage to everything connected."),

    // ── Thicket ──
    NETTLE_BLADE("The Nettle Blade", Group.THICKET, ChatFormatting.DARK_GREEN, Items.FERN,
            "It objects to being held.", "crit_chance", 22,
            "Growth makes your target's own swings hurt it."),
    THORN_RAIN("The Thorn Rain", Group.THICKET, ChatFormatting.GREEN, Items.BAMBOO,
            "Straight down, all at once.", "crit_damage", 75,
            "Growth rains thorns over a wide circle."),
    CREEPING_VINE("The Creeping Vine", Group.THICKET, ChatFormatting.DARK_GREEN, Items.VINE,
            "It arrives in a way you cannot point to.", "strength", 50,
            "Growth binds and drags your target closer."),
    BARBED_HEDGE("The Barbed Hedge", Group.THICKET, ChatFormatting.GREEN, Items.AZALEA,
            "A door made of reasons.", "defense", 80,
            "Growth grows a hedge that blocks and cuts."),
    POISON_BLOOM("The Poison Bloom", Group.THICKET, ChatFormatting.LIGHT_PURPLE, Items.WITHER_ROSE,
            "Beautiful on the way in.", "ferocity", 20,
            "Growth blooms a poison that spreads on death."),
    CHOKING_IVY("The Choking Ivy", Group.THICKET, ChatFormatting.DARK_GREEN, Items.GLOW_BERRIES,
            "Patient about the whole thing.", "strength", 55,
            "Growth chokes your target and cuts its healing."),
    SPLINTER_RING("The Splinter Ring", Group.THICKET, ChatFormatting.YELLOW, Items.STICK,
            "Made of everything that broke.", "crit_damage", 80,
            "Growth splinters your weapon into a spray."),
    BURR_COAT("The Burr Coat", Group.THICKET, ChatFormatting.GRAY, Items.GRASS_BLOCK,
            "It comes home with you.", "health", 75,
            "Growth sticks a burr on your target that keeps hurting."),
    ROT_CIRCLE("The Rot Circle", Group.THICKET, ChatFormatting.DARK_GREEN, Items.BROWN_MUSHROOM,
            "Where the ground gave up.", "intelligence", 90,
            "Growth rots the ground and everything standing on it."),

    // ── Bloom ──
    BLOOMSPIRE("The Bloomspire", Group.BLOOM, ChatFormatting.LIGHT_PURPLE, Items.PEONY,
            "Once a year, and it is worth it.", "intelligence", 95,
            "Growth opens a spire of flowers that heals everyone with you."),
    POLLEN_VEIL("The Pollen Veil", Group.BLOOM, ChatFormatting.YELLOW, Items.DANDELION,
            "Everywhere before anyone noticed.", "speed", 6,
            "Growth hides you in pollen and they lose you."),
    NECTAR_VOW("The Nectar Vow", Group.BLOOM, ChatFormatting.GOLD, Items.HONEYCOMB,
            "Sweet, and it costs something.", "health", 85,
            "Growth trades your health for theirs, favourably."),
    PETAL_CROWN("The Petal Crown", Group.BLOOM, ChatFormatting.LIGHT_PURPLE, Items.ALLIUM,
            "Light, and it commands anyway.", "defense", 70,
            "Growth scatters petals that turn hits aside."),
    ORCHARD_WRIT("The Orchard Writ", Group.BLOOM, ChatFormatting.RED, Items.APPLE,
            "Signed at every harvest.", "strength", 60,
            "Growth drops fruit that heals when you land a kill."),
    SUNWARD_FACE("The Sunward Face", Group.BLOOM, ChatFormatting.YELLOW, Items.SUNFLOWER,
            "It has one opinion and holds it.", "crit_chance", 24,
            "Growth turns your crits toward whatever is weakest."),
    DEW_LEDGER("The Dew Ledger", Group.BLOOM, ChatFormatting.AQUA, Items.GLASS_BOTTLE,
            "Written every morning, gone by noon.", "intelligence", 100,
            "Growth refreshes one of your cooldowns."),
    THORNED_ROSE("The Thorned Rose", Group.BLOOM, ChatFormatting.RED, Items.ROSE_BUSH,
            "It never pretended otherwise.", "crit_damage", 90,
            "Growth answers your crits with a burst of thorns."),
    WILD_MEADOW("The Wild Meadow", Group.BLOOM, ChatFormatting.GREEN, Items.TALL_GRASS,
            "Nobody planted any of it.", "speed", 7,
            "Growth speeds you and everyone standing with you."),

    // ── Ancient ──
    WORLD_ROOT("The World Root", Group.ANCIENT, ChatFormatting.DARK_GREEN, Items.SCULK,
            "It goes further down than the map goes.", "health", 110,
            "Growth ties you to the ground and nothing moves you."),
    ELDER_BARK("The Elder Bark", Group.ANCIENT, ChatFormatting.GRAY, Items.DARK_OAK_LOG,
            "Thick enough to have forgotten the middle.", "defense", 105,
            "Growth thickens your skin until most of it stops mattering."),
    AMBER_VOW("The Amber Vow", Group.ANCIENT, ChatFormatting.GOLD, Items.HONEY_BLOCK,
            "It kept a promise for a million years.", "intelligence", 110,
            "Growth freezes your target in amber."),
    PETRIFIED_CROWN("The Petrified Crown", Group.ANCIENT, ChatFormatting.GRAY, Items.DEEPSLATE,
            "It stopped being wood a long time ago.", "defense", 100,
            "Growth turns you to stone and you take almost nothing."),
    SEED_OF_THE_FIRST("The Seed of the First", Group.ANCIENT, ChatFormatting.GREEN, Items.BEETROOT_SEEDS,
            "Everything green owes it money.", "strength", 95,
            "Growth plants the first seed, and it grows into a fight."),
    ROT_THRONE("The Rot Throne", Group.ANCIENT, ChatFormatting.DARK_GRAY, Items.MUD,
            "Sat in by whatever comes after.", "ferocity", 28,
            "Growth spreads rot from a throne under you."),
    HEARTWOOD("The Heartwood", Group.ANCIENT, ChatFormatting.RED, Items.CHERRY_LOG,
            "The part that was never outside.", "health", 115,
            "Growth mends you steadily and cannot be stopped."),
    LONG_SEASON("The Long Season", Group.ANCIENT, ChatFormatting.YELLOW, Items.HAY_BLOCK,
            "It has been the same one for centuries.", "strength", 100,
            "Growth makes every effect on you last twice as long."),
    GREEN_SILENCE("The Green Silence", Group.ANCIENT, ChatFormatting.DARK_GREEN, Items.MOSS_CARPET,
            "Nothing here needs to say anything.", "crit_damage", 110,
            "Growth silences your target completely."),

    // ── Wildwood ──
    WILDWOOD_THRONE("The Wildwood Throne", Group.WILDWOOD, ChatFormatting.DARK_GREEN, Items.AZALEA_LEAVES,
            "Nobody agreed to it. It is simply there.", "strength", 115,
            "Growth claims the ground and everything on it fights for you."),
    BLOODBRIAR("The Bloodbriar", Group.WILDWOOD, ChatFormatting.DARK_RED, Items.WITHER_ROSE,
            "It has been fed properly.", "ferocity", 32,
            "Growth drinks from your target and hands it to you."),
    SUNKEN_GROVE("The Sunken Grove", Group.WILDWOOD, ChatFormatting.DARK_AQUA, Items.BIG_DRIPLEAF,
            "It kept growing after the water came.", "health", 115,
            "Growth grows through anything, including you."),
    SPORE_SOVEREIGN("The Spore Sovereign", Group.WILDWOOD, ChatFormatting.LIGHT_PURPLE, Items.RED_MUSHROOM_BLOCK,
            "It has plans for the whole valley.", "intelligence", 120,
            "Growth spreads spores that turn kills into allies."),
    IRONBARK_VOW("The Ironbark Vow", Group.WILDWOOD, ChatFormatting.GRAY, Items.MANGROVE_LOG,
            "Grown against something specific.", "defense", 120,
            "Growth hardens into ironbark and stops most of it."),
    CENTURY_BLOOM("The Century Bloom", Group.WILDWOOD, ChatFormatting.YELLOW, Items.TORCHFLOWER,
            "Once, and everybody remembers.", "crit_damage", 115,
            "Growth blooms once and everything near takes it."),
    ROOT_TITHE("The Root Tithe", Group.WILDWOOD, ChatFormatting.DARK_GREEN, Items.HANGING_ROOTS,
            "Everything above pays something down.", "crit_chance", 34,
            "Growth takes a share of every hit landed anywhere near."),
    GREEN_CROWN("The Green Crown", Group.WILDWOOD, ChatFormatting.GREEN, Items.FLOWERING_AZALEA,
            "Worn by whatever the forest picked.", "speed", 8,
            "Growth speeds you through anything growing."),
    LAST_GROVE("The Last Grove", Group.WILDWOOD, ChatFormatting.DARK_GREEN, Items.BIRCH_LOG,
            "There is one left and this came from it.", "strength", 120,
            "Growth plants the last grove and it does not fall.");

    /** The houses of the line, each with its own set bonus. */
    public enum Group {
        GROVE("Grove", ChatFormatting.AQUA, "Three of the grove wood: Growth fills a quarter faster."),
        THICKET("Thicket", ChatFormatting.AQUA, "Three of the thicket wood: Growth fills a quarter faster."),
        BLOOM("Bloom", ChatFormatting.AQUA, "Three of the bloom wood: Growth fills a quarter faster."),
        ANCIENT("Ancient", ChatFormatting.AQUA, "Three of the ancient wood: Growth fills a quarter faster."),
        WILDWOOD("Wildwood", ChatFormatting.AQUA, "Three of the wildwood wood: Growth fills a quarter faster.");

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

    Thorn(String display, Group group, ChatFormatting colour, Item token,
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

    public static Thorn byKey(String key) {
        if (key == null) return null;
        for (Thorn v : values()) {
            if (v.key().equals(key)) return v;
        }
        return null;
    }

    /** The framework's view of this identity. */
    public GearLine.Motif motif() {
        return new GearLine.Motif(key(), display, colour, token, flavour,
                group.display, group.colour, group.bonus,
                List.of(GearLine.stat(statKind, statAmount)),
                "Growth fills at 16 hits.");
    }
}
