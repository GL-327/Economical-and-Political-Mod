package com.political;

import net.minecraft.ChatFormatting;

/**
 * V6.1 — Cursed Spirit grading, modelled on Jujutsu Kaisen curse grades.
 *
 * <p>Ordinal is the threat rank: {@link #GRADE_4} is the weakest and most common,
 * {@link #SPECIAL_GRADE} is the strongest and rarest. Every grade carries:</p>
 * <ul>
 *   <li>a coloured display label,</li>
 *   <li>a health / attack multiplier applied to the manager's BASE stats
 *       (so scaling is consistent no matter which vanilla mob a spirit is dressed as),</li>
 *   <li>a base Cursed Energy reward paid to the slayer,</li>
 *   <li>a spawn weight (Grade 4 very common, Special Grade very rare),</li>
 *   <li>a minimum region-danger required for the grade to appear at all.</li>
 * </ul>
 *
 * <p>None of these values mint coins — Cursed Energy is a wholly separate currency
 * (see {@link CursedEnergyManager}).</p>
 */
public enum CursedGrade {

    //          label              colour                     hMult  dMult  reward  weight  minDanger
    GRADE_4    ("Grade 4 Curse",   ChatFormatting.GRAY,        1.5,   1.25,     5,     50,      0),
    GRADE_3    ("Grade 3 Curse",   ChatFormatting.GREEN,       3.0,   1.75,    12,     28,      0),
    GRADE_2    ("Grade 2 Curse",   ChatFormatting.AQUA,        7.0,   2.50,    30,     14,      2),
    GRADE_1    ("Grade 1 Curse",   ChatFormatting.LIGHT_PURPLE,15.0,  3.50,    75,      6,      4),
    SPECIAL_GRADE("Special Grade", ChatFormatting.DARK_RED,    40.0,  5.50,   200,      1,      6);

    /** Manager-wide base stats every grade multiplies. Keeps grades balanced across dressed mobs. */
    public static final double BASE_HEALTH = 20.0;
    public static final double BASE_DAMAGE = 4.0;

    public final String label;
    public final ChatFormatting color;
    public final double healthMultiplier;
    public final double damageMultiplier;
    public final int cursedEnergyReward;
    public final int spawnWeight;
    public final int minRegionDanger;

    CursedGrade(String label, ChatFormatting color, double healthMultiplier, double damageMultiplier,
                int cursedEnergyReward, int spawnWeight, int minRegionDanger) {
        this.label = label;
        this.color = color;
        this.healthMultiplier = healthMultiplier;
        this.damageMultiplier = damageMultiplier;
        this.cursedEnergyReward = cursedEnergyReward;
        this.spawnWeight = spawnWeight;
        this.minRegionDanger = minRegionDanger;
    }

    /** Absolute max-health the manager assigns before region/season scaling. */
    public double baseHealth() {
        return BASE_HEALTH * healthMultiplier;
    }

    /** Absolute attack-damage the manager assigns before region/season scaling. */
    public double baseDamage() {
        return BASE_DAMAGE * damageMultiplier;
    }

    /** Legacy §-code for embedding the grade colour inside a lore string. */
    public String legacy() {
        return "§" + color.getChar();
    }

    /** Entity tag used to re-identify a spirit's grade after a chunk reload. */
    public String tag() {
        return "political_grade_" + name().toLowerCase();
    }

    /** Reverse of {@link #tag()}; null if the tag is not a grade tag. */
    public static CursedGrade fromTag(String tag) {
        if (tag == null || !tag.startsWith("political_grade_")) return null;
        String key = tag.substring("political_grade_".length()).toUpperCase();
        for (CursedGrade g : values()) {
            if (g.name().equals(key)) return g;
        }
        return null;
    }

    /** The next grade up, or null if this is already Special Grade. */
    public CursedGrade next() {
        int i = ordinal() + 1;
        return i < values().length ? values()[i] : null;
    }
}
