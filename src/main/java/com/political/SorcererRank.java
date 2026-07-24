package com.political;

import net.minecraft.ChatFormatting;

/**
 * V6.6 — Sorcerer grades for players (the human answer to the curses).
 *
 * <p>Ranks are <b>earned</b>, never bought. Each requires a number of cursed spirits slain,
 * a running total of cursed energy spent (so progression demands actually <i>using</i> the
 * path, not idling), and a floor of cursed-technique mastery (read from
 * {@link TechniqueManager#totalUses}).</p>
 *
 * <p>Perks are deliberately tiny. The strongest passive combat bonus any player can reach from
 * rank alone is <b>+20% melee damage and +10% max health</b> at Special Grade — comparable to a
 * single good armour piece, not a power spike.</p>
 */
public enum SorcererRank {

    // display                title          colour                       spirits  ceSpent  mastery  melee  health  ceBonus regen  vowSlots
    GRADE_4  ("Grade 4",      ChatFormatting.GRAY,          0,      0L,     0,   0.04, 0.00,  0,   1.00, 2),
    GRADE_3  ("Grade 3",      ChatFormatting.WHITE,         25,     500L,   0,   0.08, 0.03,  25,  1.10, 3),
    GRADE_2  ("Grade 2",      ChatFormatting.YELLOW,        75,     2000L,  5,   0.12, 0.05,  50,  1.20, 4),
    GRADE_1  ("Grade 1",      ChatFormatting.GOLD,          200,    6000L,  15,  0.16, 0.08,  75,  1.35, 5),
    SPECIAL_GRADE("Special Grade", ChatFormatting.LIGHT_PURPLE, 500, 20000L, 30,  0.20, 0.10,  100, 1.50, 6);

    public final String display;
    public final ChatFormatting color;
    /** Cursed spirits (hostile mobs) that must be slain to hold this rank. */
    public final int reqSpirits;
    /** Lifetime cursed energy that must have been spent to hold this rank. */
    public final long reqCeSpent;
    /** Cursed-technique mastery (TechniqueManager total ability uses) required. */
    public final int reqMastery;
    /** Passive melee damage bonus (fraction). Capped envelope: +0.20 at Special Grade. */
    public final double meleePct;
    /** Passive max-health bonus (fraction of base, applied as an attribute modifier). */
    public final double healthPct;
    /** Flat bonus to max cursed energy. */
    public final int maxCeBonus;
    /** Cursed-energy regeneration multiplier. */
    public final double regenMult;
    /** How many binding vows can be held at once at this rank. */
    public final int vowSlots;

    SorcererRank(String display, ChatFormatting color, int reqSpirits, long reqCeSpent, int reqMastery,
                 double meleePct, double healthPct, int maxCeBonus, double regenMult, int vowSlots) {
        this.display = display;
        this.color = color;
        this.reqSpirits = reqSpirits;
        this.reqCeSpent = reqCeSpent;
        this.reqMastery = reqMastery;
        this.meleePct = meleePct;
        this.healthPct = healthPct;
        this.maxCeBonus = maxCeBonus;
        this.regenMult = regenMult;
        this.vowSlots = vowSlots;
    }

    /** The next grade up, or {@code null} if already Special Grade. */
    public SorcererRank next() {
        int i = ordinal();
        return i + 1 < values().length ? values()[i + 1] : null;
    }

    public boolean isAtLeast(SorcererRank other) {
        return ordinal() >= other.ordinal();
    }

    /** True if the given progress figures satisfy this rank's requirements. */
    public boolean meetsRequirements(int spirits, long ceSpent, int mastery) {
        return spirits >= reqSpirits && ceSpent >= reqCeSpent && mastery >= reqMastery;
    }

    public static SorcererRank byName(String name) {
        if (name == null) return GRADE_4;
        try {
            return valueOf(name);
        } catch (IllegalArgumentException e) {
            return GRADE_4;
        }
    }

    /** Coloured title component, e.g. §d Special Grade §7Sorcerer. */
    public net.minecraft.network.chat.Component title() {
        return net.minecraft.network.chat.Component.literal(display + " Sorcerer").withStyle(color);
    }
}
