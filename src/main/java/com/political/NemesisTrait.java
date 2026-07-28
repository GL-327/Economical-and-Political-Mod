package com.political;

import net.minecraft.ChatFormatting;

import java.util.Locale;

/**
 * The traits a nemesis can carry.
 *
 * <p>A trait is the whole point of the system. When an ordinary zombie kills a player it does not
 * come back as a slightly stronger zombie, it comes back with two or three of these bolted on, and
 * the combination is what makes the rematch a fight rather than a chore. Traits are rolled once and
 * kept forever, so every nemesis on a server is a different problem.</p>
 *
 * <p>They are also written to be legible. A player who reads "Armoured, Bloodscent, Undying" in the
 * grudge menu should be able to picture the fight before it starts, which matters far more than the
 * numbers behind them.</p>
 */
public enum NemesisTrait {

    // ── Defensive ─────────────────────────────────────────────────────────
    ARMOURED("Armoured", ChatFormatting.GRAY, Kind.DEFENSIVE,
            "Takes a third less from everything.",
            "Plated in whatever it could find."),
    IRON_HIDE("Iron Hide", ChatFormatting.WHITE, Kind.DEFENSIVE,
            "Ignores the first hit of every exchange.",
            "The first swing never seems to land properly."),
    REGENERATING("Regenerating", ChatFormatting.RED, Kind.DEFENSIVE,
            "Mends steadily unless it is burning.",
            "Whatever you did to it last time did not stay done."),
    THORNED("Thorned", ChatFormatting.DARK_GREEN, Kind.DEFENSIVE,
            "Returns a share of melee damage to the attacker.",
            "Hitting it is its own punishment."),
    UNDYING("Undying", ChatFormatting.GOLD, Kind.DEFENSIVE,
            "Survives one killing blow, once per fight.",
            "It has already refused to die at least once."),
    WARDED("Warded", ChatFormatting.LIGHT_PURPLE, Kind.DEFENSIVE,
            "Takes far less from magic and projectiles.",
            "Something is standing between it and your spells."),

    // ── Offensive ─────────────────────────────────────────────────────────
    BRUTAL("Brutal", ChatFormatting.DARK_RED, Kind.OFFENSIVE,
            "Hits half again as hard.",
            "It has stopped pretending to be careful."),
    VENOMOUS("Venomous", ChatFormatting.DARK_GREEN, Kind.OFFENSIVE,
            "Its hits poison and weaken.",
            "Something on its claws is still wet."),
    VAMPIRIC("Vampiric", ChatFormatting.DARK_RED, Kind.OFFENSIVE,
            "Heals for a share of the damage it deals.",
            "Every wound it opens closes one of its own."),
    EXPLOSIVE("Explosive", ChatFormatting.GOLD, Kind.OFFENSIVE,
            "Detonates when it dies, and sometimes when it does not.",
            "It has been getting closer than it needs to."),
    MARKSMAN("Marksman", ChatFormatting.AQUA, Kind.OFFENSIVE,
            "Throws something unpleasant from range.",
            "Distance stopped being safety."),
    FRENZIED("Frenzied", ChatFormatting.RED, Kind.OFFENSIVE,
            "Attacks faster the lower its health goes.",
            "It fights better when it is losing."),
    STORMTOUCHED("Stormtouched", ChatFormatting.YELLOW, Kind.OFFENSIVE,
            "Calls lightning where you were standing.",
            "The weather takes its side."),
    WITHERING("Withering", ChatFormatting.DARK_GRAY, Kind.OFFENSIVE,
            "Its hits stop you healing for a while.",
            "The wound refuses to close."),

    // ── Movement and control ──────────────────────────────────────────────
    SWIFT("Swift", ChatFormatting.WHITE, Kind.MOBILITY,
            "Moves considerably faster than it should.",
            "It arrives before you have decided anything."),
    PHASING("Phasing", ChatFormatting.DARK_PURPLE, Kind.MOBILITY,
            "Blinks to you when you try to open distance.",
            "Running has stopped working."),
    ENSNARING("Ensnaring", ChatFormatting.DARK_GREEN, Kind.MOBILITY,
            "Roots you in place on a hit.",
            "The ground gets involved."),
    DREADFUL("Dreadful", ChatFormatting.DARK_PURPLE, Kind.MOBILITY,
            "Blinds and sickens anyone who stands too close.",
            "Being near it is its own attack."),

    // ── Support and escalation ────────────────────────────────────────────
    SUMMONER("Summoner", ChatFormatting.LIGHT_PURPLE, Kind.SUPPORT,
            "Calls help when the fight turns against it.",
            "It does not intend to fight you alone."),
    BLOODSCENT("Bloodscent", ChatFormatting.RED, Kind.SUPPORT,
            "Finds you faster and hits harder when you are already hurt.",
            "It knows exactly how badly the last fight went."),
    COLOSSAL("Colossal", ChatFormatting.GOLD, Kind.SUPPORT,
            "Much larger, much tougher, much slower to give ground.",
            "It has been eating well."),
    CUNNING("Cunning", ChatFormatting.AQUA, Kind.SUPPORT,
            "Flees at low health and comes back with a new scar.",
            "It has worked out that dying is optional."),
    VOIDBOUND("Voidbound", ChatFormatting.DARK_PURPLE, Kind.SUPPORT,
            "Drags you toward it and refuses to be knocked back.",
            "Something else is holding it in place.");

    public enum Kind { DEFENSIVE, OFFENSIVE, MOBILITY, SUPPORT }

    public final String display;
    public final ChatFormatting colour;
    public final Kind kind;
    /** What it does, in the words a player needs. */
    public final String effect;
    /** Flavour, shown underneath. */
    public final String flavor;

    NemesisTrait(String display, ChatFormatting colour, Kind kind, String effect, String flavor) {
        this.display = display;
        this.colour = colour;
        this.kind = kind;
        this.effect = effect;
        this.flavor = flavor;
    }

    public String key() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static NemesisTrait byKey(String key) {
        if (key == null) return null;
        for (NemesisTrait t : values()) {
            if (t.key().equalsIgnoreCase(key)) return t;
        }
        return null;
    }

    /**
     * Picks a trait that is not already carried, biased away from stacking two of the same kind so a
     * nemesis ends up with a shape rather than three versions of the same idea.
     */
    public static NemesisTrait roll(java.util.List<NemesisTrait> existing, java.util.Random rng) {
        java.util.List<NemesisTrait> pool = new java.util.ArrayList<>();
        for (NemesisTrait t : values()) {
            if (existing.contains(t)) continue;
            pool.add(t);
            // A kind it does not have yet goes in twice, which nudges toward variety without
            // ever making a combination impossible.
            boolean hasKind = existing.stream().anyMatch(e -> e.kind == t.kind);
            if (!hasKind) pool.add(t);
        }
        if (pool.isEmpty()) return null;
        return pool.get(rng.nextInt(pool.size()));
    }
}
