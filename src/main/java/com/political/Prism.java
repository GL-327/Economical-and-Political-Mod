package com.political;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Locale;

/**
 * The forty five identities of the Prismatic line.
 *
 * <p>Every piece feeds the same <b>Light</b> meter, which fills as you fight and spends
 * itself on whatever the piece in your hand does. Mixing identities across a set is a real
 * choice rather than a penalty, since the meter does not care which piece filled it.</p>
 */
public enum Prism {

    // ── Spectrum ──
    FIRST_REFRACTION("The First Refraction", Group.SPECTRUM, ChatFormatting.WHITE, Items.GLASS,
            "One light, and then seven.", "intelligence", 85,
            "Light splits your next hit across seven targets."),
    RED_ANGLE("The Red Angle", Group.SPECTRUM, ChatFormatting.RED, Items.RED_STAINED_GLASS,
            "The one that bends least.", "strength", 55,
            "Light bends toward whatever has the most health."),
    AMBER_ANGLE("The Amber Angle", Group.SPECTRUM, ChatFormatting.GOLD, Items.ORANGE_STAINED_GLASS,
            "Caught between two decisions.", "ferocity", 18,
            "Light holds your target in place mid swing."),
    GOLD_ANGLE("The Gold Angle", Group.SPECTRUM, ChatFormatting.YELLOW, Items.YELLOW_STAINED_GLASS,
            "The one everyone notices.", "crit_chance", 22,
            "Light makes every third hit a guaranteed crit."),
    GREEN_ANGLE("The Green Angle", Group.SPECTRUM, ChatFormatting.GREEN, Items.GREEN_STAINED_GLASS,
            "The one in the middle.", "health", 70,
            "Light restores you for a share of what it passes through."),
    BLUE_ANGLE("The Blue Angle", Group.SPECTRUM, ChatFormatting.BLUE, Items.BLUE_STAINED_GLASS,
            "The one that goes furthest.", "defense", 70,
            "Light shields you from everything at range."),
    INDIGO_ANGLE("The Indigo Angle", Group.SPECTRUM, ChatFormatting.DARK_BLUE, Items.LIGHT_BLUE_STAINED_GLASS,
            "The one nobody can point to.", "crit_damage", 80,
            "Light lands a hit nothing can see coming."),
    VIOLET_ANGLE("The Violet Angle", Group.SPECTRUM, ChatFormatting.LIGHT_PURPLE, Items.PURPLE_STAINED_GLASS,
            "The one that bends most.", "speed", 6,
            "Light bends you around the next attack."),
    FULL_SPECTRUM("The Full Spectrum", Group.SPECTRUM, ChatFormatting.WHITE, Items.BEACON,
            "All of it, at once, briefly.", "intelligence", 100,
            "Light fires every angle at the same target."),

    // ── Shadow ──
    SHADOW_CAST("The Shadow Cast", Group.SHADOW, ChatFormatting.BLACK, Items.BLACK_STAINED_GLASS,
            "It only exists because of the light.", "strength", 60,
            "Light casts a shadow that fights beside you."),
    UMBRAL_EDGE("The Umbral Edge", Group.SHADOW, ChatFormatting.DARK_GRAY, Items.GRAY_STAINED_GLASS,
            "Where the light gave up exactly.", "crit_damage", 85,
            "Light cuts along the edge of your own shadow."),
    ECLIPSE_VOW("The Eclipse Vow", Group.SHADOW, ChatFormatting.DARK_PURPLE, Items.OBSIDIAN,
            "Made in the two minutes it lasted.", "defense", 80,
            "Light goes out and nothing can find you."),
    PENUMBRA("The Penumbra", Group.SHADOW, ChatFormatting.GRAY, Items.LIGHT_GRAY_STAINED_GLASS,
            "Neither one thing nor the other.", "health", 75,
            "Light halves everything that hits you at the edges."),
    NIGHTGLASS("The Nightglass", Group.SHADOW, ChatFormatting.BLACK, Items.TINTED_GLASS,
            "It shows you the dark clearly.", "crit_chance", 24,
            "Light shows you exactly where they are weakest."),
    SILHOUETTE("The Silhouette", Group.SHADOW, ChatFormatting.DARK_GRAY, Items.BLACK_CONCRETE,
            "The shape without any of the detail.", "speed", 6,
            "Light leaves a silhouette they attack instead."),
    VOID_ANGLE("The Void Angle", Group.SHADOW, ChatFormatting.BLACK, Items.ENDER_PEARL,
            "It bends into nothing at all.", "ferocity", 22,
            "Light folds your target somewhere else briefly."),
    BLACKBODY("The Blackbody", Group.SHADOW, ChatFormatting.DARK_GRAY, Items.COAL_BLOCK,
            "It takes everything and gives back heat.", "defense", 85,
            "Light absorbs damage and returns it as fire."),
    LONG_SHADOW("The Long Shadow", Group.SHADOW, ChatFormatting.DARK_BLUE, Items.BLUE_CONCRETE,
            "Cast at the end of the day.", "strength", 70,
            "Light stretches your reach across the field."),

    // ── Mirror ──
    MIRROR_COURT("The Mirror Court", Group.MIRROR, ChatFormatting.AQUA, Items.GLASS_PANE,
            "Everyone here is somebody else.", "intelligence", 90,
            "Light copies your last ability onto an ally."),
    SILVERED_BACK("The Silvered Back", Group.MIRROR, ChatFormatting.WHITE, Items.IRON_INGOT,
            "The part that does the work.", "defense", 90,
            "Light reflects a share of every hit straight back."),
    FACET_CROWN("The Facet Crown", Group.MIRROR, ChatFormatting.LIGHT_PURPLE, Items.AMETHYST_CLUSTER,
            "Fifty answers to the same question.", "crit_chance", 26,
            "Light gives every crit a chance to crit again."),
    TWIN_ANGLE("The Twin Angle", Group.MIRROR, ChatFormatting.AQUA, Items.AMETHYST_SHARD,
            "Identical, and it resents it.", "crit_damage", 90,
            "Light lands your hit twice."),
    REFLECTION_VOW("The Reflection Vow", Group.MIRROR, ChatFormatting.WHITE, Items.QUARTZ_BLOCK,
            "Sworn to whatever is looking.", "health", 90,
            "Light heals you for what it reflects."),
    GLASS_SEA("The Glass Sea", Group.MIRROR, ChatFormatting.AQUA, Items.PRISMARINE,
            "Flat, and it goes on.", "speed", 7,
            "Light lets you slide across anything at speed."),
    LENS_OF_MAKING("The Lens of Making", Group.MIRROR, ChatFormatting.YELLOW, Items.SPYGLASS,
            "It focuses until something happens.", "strength", 80,
            "Light focuses to a point and burns through."),
    KALEIDOSCOPE("The Kaleidoscope", Group.MIRROR, ChatFormatting.LIGHT_PURPLE, Items.AMETHYST_BLOCK,
            "The same thing, arranged and rearranged.", "ferocity", 24,
            "Light multiplies your hits in a widening pattern."),
    MIRROR_VOW("The Mirror Vow", Group.MIRROR, ChatFormatting.WHITE, Items.GLASS,
            "It only ever agrees with you.", "defense", 95,
            "Light copies your best stat onto every other one."),

    // ── Radiant ──
    STARLIGHT_WRIT("The Starlight Writ", Group.RADIANT, ChatFormatting.YELLOW, Items.NETHER_STAR,
            "Written by something a long way off.", "intelligence", 110,
            "Light calls a beam from something very far away."),
    SOLAR_CROWN("The Solar Crown", Group.RADIANT, ChatFormatting.GOLD, Items.GLOWSTONE,
            "Worn by whatever is at the centre.", "strength", 100,
            "Light burns everything you can see."),
    AURORA_RING("The Aurora Ring", Group.RADIANT, ChatFormatting.GREEN, Items.SEA_LANTERN,
            "Weather made out of light.", "speed", 8,
            "Light sheets across the field and speeds everyone with you."),
    RADIANT_VOW("The Radiant Vow", Group.RADIANT, ChatFormatting.WHITE, Items.BEACON,
            "Made where everybody could see.", "health", 110,
            "Light keeps everyone near you alive a little longer."),
    ZENITH_ANGLE("The Zenith Angle", Group.RADIANT, ChatFormatting.YELLOW, Items.SHROOMLIGHT,
            "Straight down, no shadow at all.", "crit_damage", 110,
            "Light drops straight down and nothing blocks it."),
    CORONA("The Corona", Group.RADIANT, ChatFormatting.GOLD, Items.MAGMA_BLOCK,
            "The part you only see when the rest is hidden.", "ferocity", 28,
            "Light rings you in a corona that burns."),
    WHITE_SILENCE("The White Silence", Group.RADIANT, ChatFormatting.WHITE, Items.WHITE_CONCRETE,
            "Too much of it to make anything out.", "defense", 110,
            "Light whites out everything and they cannot aim."),
    LAST_PHOTON("The Last Photon", Group.RADIANT, ChatFormatting.AQUA, Items.END_ROD,
            "The one that arrived after the source went out.", "crit_chance", 32,
            "Light lands one hit that cannot miss and cannot be reduced."),
    PRIME_COLOUR("The Prime Colour", Group.RADIANT, ChatFormatting.LIGHT_PURPLE, Items.DRAGON_EGG,
            "Before it split, this is what it was.", "strength", 115,
            "Light returns to one colour and hits like all of them."),

    // ── Unseen ──
    UNSEEN_ANGLE("The Unseen Angle", Group.UNSEEN, ChatFormatting.DARK_PURPLE, Items.ECHO_SHARD,
            "It is there. You simply cannot get to it.", "intelligence", 120,
            "Light bends into an angle nothing can block."),
    COLOUR_BEYOND("The Colour Beyond", Group.UNSEEN, ChatFormatting.LIGHT_PURPLE, Items.AMETHYST_CLUSTER,
            "There is one more and it is not for us.", "crit_damage", 120,
            "Light shows a colour that hurts to be hit by."),
    SHATTER_VOW("The Shatter Vow", Group.UNSEEN, ChatFormatting.WHITE, Items.GLASS_BOTTLE,
            "Made knowing exactly what happens next.", "ferocity", 32,
            "Light shatters and every shard finds somebody."),
    TOTAL_INTERNAL("The Total Internal", Group.UNSEEN, ChatFormatting.AQUA, Items.TINTED_GLASS,
            "Nothing gets out at all.", "defense", 120,
            "Light traps every hit inside and hands it back."),
    INTERFERENCE("The Interference", Group.UNSEEN, ChatFormatting.BLUE, Items.GLASS_PANE,
            "Two of it, cancelling.", "health", 120,
            "Light cancels the next three things that hit you."),
    DIFFRACTION_CROWN("The Diffraction Crown", Group.UNSEEN, ChatFormatting.GREEN, Items.SEA_LANTERN,
            "It spreads out on the way through.", "crit_chance", 34,
            "Light spreads your crit across everything in a line."),
    STANDING_WAVE("The Standing Wave", Group.UNSEEN, ChatFormatting.YELLOW, Items.REDSTONE_LAMP,
            "It goes nowhere very energetically.", "strength", 115,
            "Light holds a wave in place and everything in it burns."),
    EVENT_ANGLE("The Event Angle", Group.UNSEEN, ChatFormatting.BLACK, Items.CRYING_OBSIDIAN,
            "Past it, none of this applies.", "speed", 8,
            "Light folds you past the point anything can reach."),
    FIRST_LIGHT("The First Light", Group.UNSEEN, ChatFormatting.WHITE, Items.GLOWSTONE,
            "Before it, there was nothing to see by.", "strength", 125,
            "Light returns to the beginning and everything is lit.");

    /** The houses of the line, each with its own set bonus. */
    public enum Group {
        SPECTRUM("Spectrum", ChatFormatting.AQUA, "Three of the spectrum angle: Light fills a quarter faster."),
        SHADOW("Shadow", ChatFormatting.AQUA, "Three of the shadow angle: Light fills a quarter faster."),
        MIRROR("Mirror", ChatFormatting.AQUA, "Three of the mirror angle: Light fills a quarter faster."),
        RADIANT("Radiant", ChatFormatting.AQUA, "Three of the radiant angle: Light fills a quarter faster."),
        UNSEEN("Unseen", ChatFormatting.AQUA, "Three of the unseen angle: Light fills a quarter faster.");

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

    Prism(String display, Group group, ChatFormatting colour, Item token,
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

    public static Prism byKey(String key) {
        if (key == null) return null;
        for (Prism v : values()) {
            if (v.key().equals(key)) return v;
        }
        return null;
    }

    /** The framework's view of this identity. */
    public GearLine.Motif motif() {
        return new GearLine.Motif(key(), display, colour, token, flavour,
                group.display, group.colour, group.bonus,
                List.of(GearLine.stat(statKind, statAmount)),
                "Light fills at 16 hits.");
    }
}
