package com.political;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Locale;

/**
 * The forty five identities of the Emberforge line.
 *
 * <p>Every piece feeds the same <b>Heat</b> meter, which fills as you fight and spends
 * itself on whatever the piece in your hand does. Mixing identities across a set is a real
 * choice rather than a penalty, since the meter does not care which piece filled it.</p>
 */
public enum Ember {

    // ── Forge ──
    CINDER_ANVIL("The Cinder Anvil", Group.FORGE, ChatFormatting.RED, Items.ANVIL,
            "It has never been allowed to cool.", "strength", 60,
            "Heat reforges your weapon mid swing for a heavier blow."),
    QUENCHING_TROUGH("The Quenching Trough", Group.FORGE, ChatFormatting.AQUA, Items.CAULDRON,
            "It knows exactly when to stop.", "defense", 70,
            "Heat quenches you and the next three hits do nothing."),
    BELLOWS_LUNG("The Bellows Lung", Group.FORGE, ChatFormatting.GOLD, Items.LEATHER,
            "In, out, and the fire obeys.", "ferocity", 20,
            "Heat blasts a cone of fire from your chest."),
    HAMMER_VOW("The Hammer Vow", Group.FORGE, ChatFormatting.GRAY, Items.IRON_INGOT,
            "Struck once for every promise.", "strength", 70,
            "Heat lands a second hammer blow behind your own."),
    SLAG_CROWN("The Slag Crown", Group.FORGE, ChatFormatting.DARK_GRAY, Items.GRAVEL,
            "Everything they threw away, worn.", "health", 75,
            "Heat covers you in slag that turns hits aside."),
    TONGS_OF_MAKING("The Tongs of Making", Group.FORGE, ChatFormatting.GOLD, Items.SHEARS,
            "It holds what nothing else can.", "intelligence", 85,
            "Heat holds your target in the fire."),
    INGOT_HEART("The Ingot Heart", Group.FORGE, ChatFormatting.YELLOW, Items.GOLD_BLOCK,
            "Poured, cooled, and never spent.", "defense", 80,
            "Heat pours a shell of metal over you."),
    CRUCIBLE_EYE("The Crucible Eye", Group.FORGE, ChatFormatting.GOLD, Items.BLAZE_POWDER,
            "It watches things become other things.", "crit_chance", 24,
            "Heat melts a share of the target's armour away."),
    ASH_LEDGER("The Ash Ledger", Group.FORGE, ChatFormatting.GRAY, Items.PAPER,
            "A record of everything burned.", "crit_damage", 80,
            "Heat repeats your biggest hit of the fight."),

    // ── Molten ──
    MAGMA_VEIN("The Magma Vein", Group.MOLTEN, ChatFormatting.RED, Items.MAGMA_BLOCK,
            "It runs where it likes.", "strength", 75,
            "Heat opens a vein of magma under your target."),
    OBSIDIAN_VOW("The Obsidian Vow", Group.MOLTEN, ChatFormatting.DARK_PURPLE, Items.OBSIDIAN,
            "Cooled in anger and kept that way.", "defense", 85,
            "Heat hardens you into obsidian for three seconds."),
    LAVA_CHOIR("The Lava Choir", Group.MOLTEN, ChatFormatting.GOLD, Items.LAVA_BUCKET,
            "It only sings one note.", "ferocity", 22,
            "Heat sets everything near you alight."),
    BASALT_SPINE("The Basalt Spine", Group.MOLTEN, ChatFormatting.BLACK, Items.BASALT,
            "Straight up out of the flow.", "health", 85,
            "Heat raises basalt spikes around you."),
    PYROCLAST("The Pyroclast", Group.MOLTEN, ChatFormatting.DARK_RED, Items.BLACKSTONE,
            "Faster than anything should be that heavy.", "speed", 6,
            "Heat carries you forward in a cloud of ash."),
    CALDERA_RING("The Caldera Ring", Group.MOLTEN, ChatFormatting.RED, Items.NETHERRACK,
            "What is left after the mountain gives up.", "defense", 90,
            "Heat digs a ring of fire nobody crosses."),
    FUMAROLE("The Fumarole", Group.MOLTEN, ChatFormatting.GRAY, Items.SOUL_SAND,
            "It breathes out and everything leaves.", "intelligence", 90,
            "Heat vents a gas that blinds and burns."),
    GLASS_RAIN("The Glass Rain", Group.MOLTEN, ChatFormatting.WHITE, Items.GLASS,
            "The sky gave it back sharpened.", "crit_damage", 90,
            "Heat rains volcanic glass on everything near."),
    SULPHUR_CROWN("The Sulphur Crown", Group.MOLTEN, ChatFormatting.YELLOW, Items.GLOWSTONE_DUST,
            "You smell it before you see it.", "crit_chance", 26,
            "Heat chokes your target and stops it healing."),

    // ── Phoenix ──
    EMBER_WING("The Ember Wing", Group.PHOENIX, ChatFormatting.GOLD, Items.FEATHER,
            "It has done this before.", "speed", 7,
            "Heat lifts you clear of whatever was about to land."),
    ASH_NEST("The Ash Nest", Group.PHOENIX, ChatFormatting.GRAY, Items.HAY_BLOCK,
            "Built out of the last one.", "health", 95,
            "Heat leaves a nest that heals you while you stand in it."),
    REBIRTH_COAL("The Rebirth Coal", Group.PHOENIX, ChatFormatting.RED, Items.COAL,
            "Still warm from the last time.", "health", 90,
            "Heat brings you back once from a killing blow."),
    LONG_BURN("The Long Burn", Group.PHOENIX, ChatFormatting.GOLD, Items.BLAZE_ROD,
            "Slow, and it never goes out.", "strength", 65,
            "Heat sets a fire on your target that grows."),
    FIRST_SPARK("The First Spark", Group.PHOENIX, ChatFormatting.YELLOW, Items.FLINT_AND_STEEL,
            "Everything after was an argument about it.", "crit_chance", 25,
            "Heat guarantees your next hit crits."),
    SOLAR_CREST("The Solar Crest", Group.PHOENIX, ChatFormatting.YELLOW, Items.SUNFLOWER,
            "Turned toward the only thing worth it.", "intelligence", 100,
            "Heat calls a beam that follows your look."),
    FEATHER_LEDGER("The Feather Ledger", Group.PHOENIX, ChatFormatting.GOLD, Items.WRITABLE_BOOK,
            "Every life, in order.", "crit_damage", 95,
            "Heat pays back every hit you took this fight."),
    MOLTEN_TALON("The Molten Talon", Group.PHOENIX, ChatFormatting.RED, Items.GOLDEN_SWORD,
            "It picks things up that should not be picked up.", "ferocity", 24,
            "Heat grips and burns whatever you are hitting."),
    CINDER_SONG("The Cinder Song", Group.PHOENIX, ChatFormatting.GOLD, Items.NOTE_BLOCK,
            "Sung on the way down.", "defense", 75,
            "Heat sings and the burning stops hurting you."),

    // ── Emberking ──
    ASHEN_THRONE("The Ashen Throne", Group.EMBERKING, ChatFormatting.DARK_RED, Items.BLACKSTONE,
            "Sat in once, briefly.", "strength", 95,
            "Heat plants a throne and everything near it burns."),
    COALKING_RING("The Coalking Ring", Group.EMBERKING, ChatFormatting.BLACK, Items.COAL_BLOCK,
            "Worth more the longer it waits.", "defense", 95,
            "Heat rings you in coal that ignites on contact."),
    FURNACE_WRIT("The Furnace Writ", Group.EMBERKING, ChatFormatting.GOLD, Items.FURNACE,
            "Signed in something that was not ink.", "intelligence", 105,
            "Heat smelts everything your target drops."),
    EMBER_DIADEM("The Ember Diadem", Group.EMBERKING, ChatFormatting.RED, Items.GOLDEN_HELMET,
            "Small fires, arranged.", "crit_chance", 30,
            "Heat gives every hit a chance to spread the fire."),
    SCORCH_MARK("The Scorch Mark", Group.EMBERKING, ChatFormatting.DARK_RED, Items.REDSTONE,
            "It stays after everything else goes.", "crit_damage", 105,
            "Heat marks a target and every hit on it is doubled."),
    KILN_HEART("The Kiln Heart", Group.EMBERKING, ChatFormatting.GOLD, Items.BRICKS,
            "Fired until it stopped being clay.", "health", 105,
            "Heat fires you hard enough to ignore one death."),
    SMELTING_CROWN("The Smelting Crown", Group.EMBERKING, ChatFormatting.YELLOW, Items.BLAST_FURNACE,
            "It takes the ore and keeps the metal.", "ferocity", 26,
            "Heat renders enemies down into coins."),
    LAST_FORGE("The Last Forge", Group.EMBERKING, ChatFormatting.DARK_GRAY, Items.SMITHING_TABLE,
            "Where the last good thing was made.", "strength", 110,
            "Heat reforges your weapon into something briefly perfect."),
    UNQUENCHED("The Unquenched", Group.EMBERKING, ChatFormatting.RED, Items.FIRE_CHARGE,
            "It was never put out. It was only left.", "ferocity", 30,
            "Heat never runs down while anything near you is burning."),

    // ── Starforge ──
    STARFORGE("The Starforge", Group.STARFORGE, ChatFormatting.AQUA, Items.NETHERITE_INGOT,
            "Lit off something that was not a fire.", "strength", 115,
            "Heat forges a blade out of a falling star."),
    VOID_BELLOWS("The Void Bellows", Group.STARFORGE, ChatFormatting.DARK_PURPLE, Items.SHULKER_SHELL,
            "It breathes in from somewhere else.", "intelligence", 115,
            "Heat pulls air from nowhere and the fire triples."),
    COLD_COAL("The Cold Coal", Group.STARFORGE, ChatFormatting.DARK_BLUE, Items.DEEPSLATE,
            "It burns without ever being warm.", "crit_damage", 115,
            "Heat burns cold and nothing resists it."),
    ANVIL_OF_WORLDS("The Anvil of Worlds", Group.STARFORGE, ChatFormatting.GOLD, Items.NETHERITE_BLOCK,
            "Everything solid was shaped on it.", "defense", 120,
            "Heat shapes the ground into a wall behind you."),
    NINEFOLD_FLAME("The Ninefold Flame", Group.STARFORGE, ChatFormatting.RED, Items.FIRE_CHARGE,
            "One fire pretending to be nine.", "ferocity", 32,
            "Heat splits into nine flames that each seek a target."),
    CINDER_COMPACT("The Cinder Compact", Group.STARFORGE, ChatFormatting.GRAY, Items.FLINT,
            "Agreed to by everything that burns.", "health", 115,
            "Heat spends your own burning to heal you."),
    HAMMER_OF_ENDS("The Hammer of Ends", Group.STARFORGE, ChatFormatting.DARK_RED, Items.MACE,
            "It was made for one job.", "strength", 125,
            "Heat lands one blow worth the whole fight."),
    QUENCHLESS_VOW("The Quenchless Vow", Group.STARFORGE, ChatFormatting.GOLD, Items.LAVA_BUCKET,
            "Nobody has found the water for it.", "crit_chance", 34,
            "Heat never falls below half, whatever happens."),
    LAST_EMBER("The Last Ember", Group.STARFORGE, ChatFormatting.RED, Items.CAMPFIRE,
            "When it goes out, that is the end of it.", "intelligence", 120,
            "Heat spends everything at once for one enormous strike.");

    /** The houses of the line, each with its own set bonus. */
    public enum Group {
        FORGE("Forge", ChatFormatting.AQUA, "Three of the forge forge: Heat fills a quarter faster."),
        MOLTEN("Molten", ChatFormatting.AQUA, "Three of the molten forge: Heat fills a quarter faster."),
        PHOENIX("Phoenix", ChatFormatting.AQUA, "Three of the phoenix forge: Heat fills a quarter faster."),
        EMBERKING("Emberking", ChatFormatting.AQUA, "Three of the emberking forge: Heat fills a quarter faster."),
        STARFORGE("Starforge", ChatFormatting.AQUA, "Three of the starforge forge: Heat fills a quarter faster.");

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

    Ember(String display, Group group, ChatFormatting colour, Item token,
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

    public static Ember byKey(String key) {
        if (key == null) return null;
        for (Ember v : values()) {
            if (v.key().equals(key)) return v;
        }
        return null;
    }

    /** The framework's view of this identity. */
    public GearLine.Motif motif() {
        return new GearLine.Motif(key(), display, colour, token, flavour,
                group.display, group.colour, group.bonus,
                List.of(GearLine.stat(statKind, statAmount)),
                "Heat fills at 16 hits.");
    }
}
