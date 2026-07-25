package com.political;

import net.minecraft.ChatFormatting;

/**
 * The forty runes of the Runeforge.
 *
 * <p>A rune decides what a piece <i>does</i> when it fires. Every rune pairs an effect archetype
 * with its own magnitude, colour and attunement palette, so two runes sharing an archetype still
 * behave and look different from one another.</p>
 */
public enum Rune {

    // ── Burning ───────────────────────────────────────────────────────────
    //    display        colour                      effect             mag  attunement
    KINDLE("Kindle", ChatFormatting.GOLD, Effect.IGNITE, 60, AetherAttunement.EMBER,
            "A patient heat that catches late."),
    PYRE("Pyre", ChatFormatting.RED, Effect.IGNITE, 140, AetherAttunement.EMBER,
            "It does not go out on its own."),
    SUNBRAND("Sunbrand", ChatFormatting.YELLOW, Effect.IGNITE, 100, AetherAttunement.SOLARIS,
            "Marked by light, and light is hot."),

    // ── Cold ──────────────────────────────────────────────────────────────
    RIME("Rime", ChatFormatting.AQUA, Effect.CHILL, 60, AetherAttunement.GLACIUS,
            "Frost gathers where you struck."),
    DEEPFREEZE("Deepfreeze", ChatFormatting.BLUE, Effect.CHILL, 120, AetherAttunement.GLACIUS,
            "Joints stop answering."),
    HOARFROST("Hoarfrost", ChatFormatting.WHITE, Effect.CHILL, 90, AetherAttunement.ABYSSAL,
            "The cold of deep water, not of air."),

    // ── Lightning ─────────────────────────────────────────────────────────
    SPARK("Spark", ChatFormatting.YELLOW, Effect.SHOCK, 40, AetherAttunement.TEMPEST,
            "A small unkindness, delivered fast."),
    THUNDERCLAP("Thunderclap", ChatFormatting.GOLD, Effect.SHOCK, 110, AetherAttunement.TEMPEST,
            "Everything nearby hears it land."),
    STORMBIND("Stormbind", ChatFormatting.AQUA, Effect.SHOCK, 75, AetherAttunement.ASTRAL,
            "The sky owes you a favour."),

    // ── Venom and rot ─────────────────────────────────────────────────────
    SERPENT("Serpent", ChatFormatting.DARK_GREEN, Effect.POISON, 80, AetherAttunement.VERDANT,
            "Slow, certain, unhurried."),
    BLIGHT("Blight", ChatFormatting.GREEN, Effect.POISON, 140, AetherAttunement.VERDANT,
            "Nothing grows back quite right."),
    GRAVEROT("Graverot", ChatFormatting.DARK_GRAY, Effect.WITHER, 80, AetherAttunement.UMBRA,
            "The wound forgets how to close."),
    HOLLOWING("Hollowing", ChatFormatting.DARK_PURPLE, Effect.WITHER, 140, AetherAttunement.VOIDREND,
            "Something is being taken out."),

    // ── Life stealing ─────────────────────────────────────────────────────
    THIRST("Thirst", ChatFormatting.DARK_RED, Effect.LEECH, 20, AetherAttunement.SANGUINE,
            "A small red tax on every strike."),
    CRIMSON_DRAW("Crimson Draw", ChatFormatting.RED, Effect.LEECH, 45, AetherAttunement.SANGUINE,
            "It takes enough to matter."),
    SOULSIP("Soulsip", ChatFormatting.DARK_AQUA, Effect.LEECH, 30, AetherAttunement.VOIDREND,
            "Not blood. Something under it."),

    // ── Armour breaking ───────────────────────────────────────────────────
    SUNDER("Sunder", ChatFormatting.GRAY, Effect.REND, 60, AetherAttunement.TEMPEST,
            "Plate is a suggestion."),
    SHATTERPOINT("Shatterpoint", ChatFormatting.WHITE, Effect.REND, 120, AetherAttunement.GLACIUS,
            "Find the seam, widen the seam."),

    // ── Control ───────────────────────────────────────────────────────────
    MIRE("Mire", ChatFormatting.DARK_GREEN, Effect.STAGGER, 80, AetherAttunement.ABYSSAL,
            "The ground holds on."),
    ANCHOR("Anchor", ChatFormatting.BLUE, Effect.STAGGER, 140, AetherAttunement.ABYSSAL,
            "Weight arrives from nowhere."),
    GLOOM("Gloom", ChatFormatting.DARK_GRAY, Effect.BLIND, 60, AetherAttunement.UMBRA,
            "The light goes out for a moment."),
    NIGHTFALL("Nightfall", ChatFormatting.BLACK, Effect.BLIND, 110, AetherAttunement.UMBRA,
            "Longer than a moment."),
    UPDRAFT("Updraft", ChatFormatting.WHITE, Effect.LEVITATE, 30, AetherAttunement.ASTRAL,
            "The floor stops being relevant."),
    STARFALL_RUNE("Starfall", ChatFormatting.LIGHT_PURPLE, Effect.LEVITATE, 50, AetherAttunement.ASTRAL,
            "Up first. Down is the point."),
    MAELSTROM("Maelstrom", ChatFormatting.DARK_AQUA, Effect.PULL, 100, AetherAttunement.VOIDREND,
            "Everything leans toward you."),
    CONCUSSION("Concussion", ChatFormatting.GOLD, Effect.PUSH, 100, AetherAttunement.TEMPEST,
            "Everything leans away."),

    // ── Self buffs ────────────────────────────────────────────────────────
    QUICKSILVER("Quicksilver", ChatFormatting.WHITE, Effect.HASTE_SELF, 100, AetherAttunement.TEMPEST,
            "Your hands get ahead of you."),
    FORGEHEART("Forgeheart", ChatFormatting.GOLD, Effect.HASTE_SELF, 160, AetherAttunement.EMBER,
            "Work becomes easy and then quick."),
    AEGIS("Aegis", ChatFormatting.BLUE, Effect.SHIELD_SELF, 100, AetherAttunement.SOLARIS,
            "A held breath of protection."),
    BULWARK_RUNE("Bulwark", ChatFormatting.GRAY, Effect.SHIELD_SELF, 160, AetherAttunement.GLACIUS,
            "Braced before the blow lands."),
    MENDING_LEAF("Mending Leaf", ChatFormatting.GREEN, Effect.HEAL_SELF, 25, AetherAttunement.VERDANT,
            "Green closes what red opened."),
    LIFEBLOOM("Lifebloom", ChatFormatting.DARK_GREEN, Effect.HEAL_SELF, 50, AetherAttunement.VERDANT,
            "It keeps mending after you stop."),
    FLEETFOOT("Fleetfoot", ChatFormatting.AQUA, Effect.SPEED_SELF, 120, AetherAttunement.TEMPEST,
            "Distance shortens itself."),
    PHASESTEP("Phasestep", ChatFormatting.DARK_PURPLE, Effect.SPEED_SELF, 180, AetherAttunement.VOIDREND,
            "You are briefly somewhere else."),

    // ── Resource ──────────────────────────────────────────────────────────
    CURSEDRAW("Cursedraw", ChatFormatting.DARK_GRAY, Effect.ENERGY, 3, AetherAttunement.UMBRA,
            "Cursed energy collects on the edge."),
    SOULTITHE("Soultithe", ChatFormatting.DARK_AQUA, Effect.ENERGY, 6, AetherAttunement.VOIDREND,
            "A toll, taken at the moment of ending."),
    AVARICE("Avarice", ChatFormatting.YELLOW, Effect.COIN, 12, AetherAttunement.SOLARIS,
            "Somebody always pays."),
    PROSPECT("Prospect", ChatFormatting.GOLD, Effect.COIN, 25, AetherAttunement.SOLARIS,
            "Coin has a way of turning up."),
    INSIGHT("Insight", ChatFormatting.LIGHT_PURPLE, Effect.XP, 8, AetherAttunement.ASTRAL,
            "You learn something from the doing."),
    REVELATION("Revelation", ChatFormatting.AQUA, Effect.XP, 18, AetherAttunement.ASTRAL,
            "More than you wanted to know."),
    QUARRY_MARK("Quarry Mark", ChatFormatting.RED, Effect.MARK, 120, AetherAttunement.SANGUINE,
            "It cannot hide from you now.");

    /** What a rune actually does when it fires. */
    public enum Effect {
        IGNITE, CHILL, SHOCK, POISON, WITHER, LEECH, REND, STAGGER, BLIND, LEVITATE,
        PULL, PUSH, HASTE_SELF, SHIELD_SELF, HEAL_SELF, SPEED_SELF, ENERGY, COIN, XP, MARK
    }

    public final String display;
    public final ChatFormatting colour;
    public final Effect effect;
    /** Meaning depends on the effect: ticks, percent, or a flat amount. */
    public final int magnitude;
    /** Which attunement's look and signature this rune borrows. */
    public final AetherAttunement attunement;
    public final String flavor;

    Rune(String display, ChatFormatting colour, Effect effect, int magnitude,
         AetherAttunement attunement, String flavor) {
        this.display = display;
        this.colour = colour;
        this.effect = effect;
        this.magnitude = magnitude;
        this.attunement = attunement;
        this.flavor = flavor;
    }

    public String key() {
        return name().toLowerCase(java.util.Locale.ROOT);
    }

    /** A short line describing the effect, for lore. */
    public String describe() {
        return switch (effect) {
            case IGNITE -> "Sets the target burning.";
            case CHILL -> "Slows the target sharply.";
            case SHOCK -> "Arcs through the target and its neighbours.";
            case POISON -> "Leaves a poison that lingers.";
            case WITHER -> "Withers the wound so it will not close.";
            case LEECH -> "Returns part of the damage to you as health.";
            case REND -> "Weakens the target's guard.";
            case STAGGER -> "Roots the target in place.";
            case BLIND -> "Blinds the target.";
            case LEVITATE -> "Lifts the target off the ground.";
            case PULL -> "Drags nearby enemies toward the impact.";
            case PUSH -> "Throws nearby enemies away from the impact.";
            case HASTE_SELF -> "Quickens your hands.";
            case SHIELD_SELF -> "Braces you against the next blows.";
            case HEAL_SELF -> "Closes some of your own wounds.";
            case SPEED_SELF -> "Carries you forward.";
            case ENERGY -> "Draws cursed energy from the kill.";
            case COIN -> "Shakes loose a little coin.";
            case XP -> "Turns the moment into experience.";
            case MARK -> "Marks the target so it cannot hide.";
        };
    }

    public static Rune byKey(String key) {
        if (key == null) return null;
        for (Rune r : values()) {
            if (r.key().equalsIgnoreCase(key) || r.name().equalsIgnoreCase(key)) return r;
        }
        return null;
    }
}
