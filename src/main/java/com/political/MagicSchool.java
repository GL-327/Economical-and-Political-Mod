package com.political;

import net.minecraft.ChatFormatting;

/**
 * The 12 schools of Magic V4. Each school owns a HotM-style {@link SchoolTree} and grants
 * passive bonuses queried through {@link SchoolTreeManager}. Schools do NOT unlock spells
 * (the 33-spell engine is fed exclusively by {@link MagicSkillTree} through
 * {@code MagicManager.unlockedSpells}); schools grant scaling passives + Awakened capstones.
 */
public enum MagicSchool {
    PYROMANCY   ("Pyromancy",   "pyr", ChatFormatting.RED,          '♨', "§c", "Fire & flame — raw burning damage."),
    FROSTWEAVING("Frostweaving","fro", ChatFormatting.AQUA,         '❄', "§b", "Ice & control — slows and shatters."),
    STORMCALLING("Stormcalling","sto", ChatFormatting.YELLOW,       '⚡', "§e", "Lightning & chain — bursts and jumps."),
    VOIDWALKING ("Voidwalking", "voi", ChatFormatting.DARK_PURPLE,  '◉', "§5", "Shadow & void — armor-piercing decay."),
    BLOODBINDING("Bloodbinding","blo", ChatFormatting.DARK_RED,     '❤', "§4", "Blood & sacrifice — lifesteal power."),
    VERDANCY    ("Verdancy",    "ver", ChatFormatting.GREEN,        '⚘', "§a", "Nature & growth — healing and roots."),
    RADIANCE    ("Radiance",    "rad", ChatFormatting.GOLD,         '☀', "§6", "Holy light — smite and protection."),
    GRAVITAS    ("Gravitas",    "gra", ChatFormatting.GRAY,         '⚙', "§7", "Gravity & mass — pulls and crush."),

    // ── V4 expansion: four further disciplines ──────────────────────────────
    CHRONOMANCY ("Chronomancy", "chr", ChatFormatting.DARK_AQUA,    '⌛', "§3", "Time & tempo — haste, delay, rewind."),
    RESONANCE   ("Resonance",   "res", ChatFormatting.LIGHT_PURPLE, '♫', "§d", "Sound & vibration — shattering sonic force."),
    SPOREWEAVING("Sporeweaving","spo", ChatFormatting.DARK_GREEN,   '❦', "§2", "Fungal blight — spreading toxin and rot."),
    ASTRALISM   ("Astralism",   "ast", ChatFormatting.BLUE,         '★', "§9", "Stars & cosmos — distant, inevitable light.");

    public final String displayName;
    public final String prefix;
    public final ChatFormatting color;
    public final char symbol;
    public final String legacyColor;
    public final String description;

    MagicSchool(String displayName, String prefix, ChatFormatting color, char symbol, String legacyColor, String description) {
        this.displayName = displayName;
        this.prefix = prefix;
        this.color = color;
        this.symbol = symbol;
        this.legacyColor = legacyColor;
        this.description = description;
    }

    public String coloredName() {
        return legacyColor + symbol + " " + displayName;
    }

    public static MagicSchool byName(String name) {
        if (name == null) return null;
        for (MagicSchool s : values()) {
            if (s.name().equalsIgnoreCase(name) || s.displayName.equalsIgnoreCase(name) || s.prefix.equalsIgnoreCase(name)) {
                return s;
            }
        }
        return null;
    }
}
