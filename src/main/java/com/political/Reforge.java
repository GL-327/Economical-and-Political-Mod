package com.political;

import net.minecraft.ChatFormatting;

/**
 * Reforges. Every custom item in the mod can carry one, which is what turns a single Cursed Blade
 * into dozens of meaningfully different Cursed Blades.
 *
 * <p>A reforge renames the item with a prefix and adds its own stat block on top of whatever the
 * item already gives. Stat values follow the mod rule of multiples of five, with values under ten
 * allowed to be any integer. Nothing here replaces an item's base stats, it layers on them, and a
 * new reforge always cleanly replaces the previous one.</p>
 */
public enum Reforge {

    // ── Weapon reforges ────────────────────────────────────────────────────
    //        display        colour                       class      str def  hp  cc  cd  spd int fer  weight
    FABLED   ("Fabled",     ChatFormatting.LIGHT_PURPLE, Kind.WEAPON, 20,  0,  0,  5, 30,  0,  0,  0, 6),
    SPICY    ("Spicy",      ChatFormatting.RED,          Kind.WEAPON, 15,  0,  0, 10, 25,  0,  0,  0, 8),
    LEGENDARY("Legendary",  ChatFormatting.GOLD,         Kind.WEAPON, 15,  5, 10,  5, 15,  0, 15,  0, 5),
    HEROIC   ("Heroic",     ChatFormatting.AQUA,         Kind.WEAPON, 25,  0,  0,  5, 20,  0,  0,  5, 5),
    SHARP    ("Sharp",      ChatFormatting.WHITE,        Kind.WEAPON, 10,  0,  0, 15, 20,  0,  0,  0,10),
    WITHERED ("Withered",   ChatFormatting.DARK_GRAY,    Kind.WEAPON, 30,  0,  0,  0, 20,  0,  0,  0, 4),
    SUSPICIOUS("Suspicious",ChatFormatting.DARK_RED,     Kind.WEAPON,  5,  0,  0, 20, 35,  0,  0,  0, 5),
    GILDED   ("Gilded",     ChatFormatting.YELLOW,       Kind.WEAPON, 20,  0,  0,  0, 10,  0,  0, 10, 6),
    WARPED   ("Warped",     ChatFormatting.DARK_PURPLE,  Kind.WEAPON, 15,  0,  0,  5, 15,  5, 25,  0, 6),
    BLOODSOAKED("Bloodsoaked", ChatFormatting.DARK_RED,  Kind.WEAPON, 25,  0, -5, 10, 25,  0,  0,  5, 4),
    PRECISE  ("Precise",    ChatFormatting.BLUE,         Kind.WEAPON,  5,  0,  0, 20, 30,  0,  0,  0, 7),
    FEROCIOUS("Ferocious",  ChatFormatting.RED,          Kind.WEAPON, 10,  0,  0,  5, 10,  0,  0, 15, 4),
    ODD      ("Odd",        ChatFormatting.GREEN,        Kind.WEAPON,  0,  0,  0, 25, 20,  0, 20,  0, 6),
    COLDFORGED("Coldforged",ChatFormatting.AQUA,         Kind.WEAPON, 15,  5,  5,  5, 15,  0,  0,  0, 8),
    EXECUTIONER("Executioner", ChatFormatting.DARK_RED,  Kind.WEAPON, 35,  0,  0,  0, 25,  0,  0,  0, 3),

    // ── Armour reforges ────────────────────────────────────────────────────
    TITANIC  ("Titanic",    ChatFormatting.DARK_GREEN,   Kind.ARMOR,   0, 35, 25,  0,  0,  0,  0,  0, 6),
    BULWARK  ("Bulwark",    ChatFormatting.GRAY,         Kind.ARMOR,   0, 40, 10,  0,  0, -5,  0,  0, 6),
    ANCIENT  ("Ancient",    ChatFormatting.GOLD,         Kind.ARMOR,  10, 25, 20,  0,  0,  0, 20,  0, 4),
    REINFORCED("Reinforced",ChatFormatting.WHITE,        Kind.ARMOR,   0, 30, 15,  0,  0,  0,  0,  0,10),
    MOSSY    ("Mossy",      ChatFormatting.GREEN,        Kind.ARMOR,   0, 20, 30,  0,  0,  0,  0,  0, 7),
    NIMBLE   ("Nimble",     ChatFormatting.AQUA,         Kind.ARMOR,   0, 15,  5,  0,  0, 15,  0,  0, 7),
    WISE     ("Wise",       ChatFormatting.DARK_AQUA,    Kind.ARMOR,   0, 15, 10,  0,  0,  0, 60,  0, 6),
    PURE     ("Pure",       ChatFormatting.WHITE,        Kind.ARMOR,   5, 20, 15,  5,  5,  5, 15,  0, 5),
    HARDENED ("Hardened",   ChatFormatting.DARK_GRAY,    Kind.ARMOR,   0, 45,  5,  0,  0,  0,  0,  0, 5),
    SPIRITUAL("Spiritual",  ChatFormatting.LIGHT_PURPLE, Kind.ARMOR,   5, 15, 10, 10, 15,  0, 30,  0, 4),
    CURSEDBOUND("Cursedbound", ChatFormatting.DARK_PURPLE,Kind.ARMOR, 15, 25,  0,  5, 10,  0, 20,  0, 3),
    GUARDED  ("Guarded",    ChatFormatting.BLUE,         Kind.ARMOR,   0, 35, 20,  0,  0, -5,  0,  0, 7),
    VETERAN  ("Veteran",    ChatFormatting.GOLD,         Kind.ARMOR,  10, 25, 25,  0,  0,  0,  0,  0, 5),
    SHADED   ("Shaded",     ChatFormatting.DARK_GRAY,    Kind.ARMOR,   0, 20, 10,  5,  0, 10,  0,  0, 6),

    // ── Tool reforges ──────────────────────────────────────────────────────
    STURDY   ("Sturdy",     ChatFormatting.GRAY,         Kind.TOOL,    5, 20, 15,  0,  0,  0,  0,  0, 9),
    FLEET    ("Fleet",      ChatFormatting.AQUA,         Kind.TOOL,    5,  0,  0,  0,  0, 15,  0,  0, 8),
    LUSTROUS ("Lustrous",   ChatFormatting.YELLOW,       Kind.TOOL,    5,  0,  5,  0,  0,  5, 25,  0, 7),
    RUGGED   ("Rugged",     ChatFormatting.DARK_GREEN,   Kind.TOOL,   10, 15, 10,  0,  0,  0,  0,  0, 8),
    EXCAVATING("Excavating",ChatFormatting.GOLD,         Kind.TOOL,    5,  0,  0,  5,  0,  5, 20,  0, 6),
    PROSPECTOR("Prospector",ChatFormatting.GREEN,        Kind.TOOL,    0,  5, 10,  0,  0, 10, 30,  0, 5),

    // ── Accessory and universal reforges ───────────────────────────────────
    BIZARRE  ("Bizarre",    ChatFormatting.DARK_PURPLE,  Kind.ANY,    10,  0,  0, 10, 20,  0, 25,  0, 5),
    ITCHY    ("Itchy",      ChatFormatting.GREEN,        Kind.ANY,    15,  0,  0,  0, 15,  5,  0,  0, 8),
    OMINOUS  ("Ominous",    ChatFormatting.DARK_RED,     Kind.ANY,    10,  5,  0,  5, 15,  0, 15,  0, 6),
    SILKY    ("Silky",      ChatFormatting.LIGHT_PURPLE, Kind.ANY,     5, 10, 10,  5,  5, 10, 10,  0, 7),
    STELLAR  ("Stellar",    ChatFormatting.AQUA,         Kind.ANY,    10, 10, 15,  5, 10,  5, 25,  0, 4),
    UNSTABLE ("Unstable",   ChatFormatting.RED,          Kind.ANY,    20,  0,  0, 15, 30, -5,  0,  0, 4),
    SERENE   ("Serene",     ChatFormatting.WHITE,        Kind.ANY,     0, 20, 20,  0,  0,  5, 20,  0, 6),
    RADIANT  ("Radiant",    ChatFormatting.YELLOW,       Kind.GENERIC,10, 15, 10,  5, 10,  5, 20,  0, 4),
    ETHEREAL ("Ethereal",   ChatFormatting.DARK_AQUA,    Kind.ANY,     5, 10,  5,  5,  5, 20, 20,  0, 3),
    MYTHRIL  ("Mythril",    ChatFormatting.DARK_AQUA,    Kind.ANY,    15, 20, 15,  5, 10,  0,  0,  5, 3);

    /** Which gear a reforge can be applied to. ANY and GENERIC fit everything. */
    public enum Kind { WEAPON, ARMOR, TOOL, ANY, GENERIC }

    public final String display;
    public final ChatFormatting colour;
    public final Kind kind;
    public final int strength;
    public final int defense;
    public final int health;
    public final int critChance;
    public final int critDamage;
    public final int speed;
    public final int intelligence;
    public final int ferocity;
    /** Relative roll weight. Rarer, stronger reforges have a lower weight. */
    public final int weight;

    Reforge(String display, ChatFormatting colour, Kind kind,
            int strength, int defense, int health, int critChance, int critDamage,
            int speed, int intelligence, int ferocity, int weight) {
        this.display = display;
        this.colour = colour;
        this.kind = kind;
        this.strength = strength;
        this.defense = defense;
        this.health = health;
        this.critChance = critChance;
        this.critDamage = critDamage;
        this.speed = speed;
        this.intelligence = intelligence;
        this.ferocity = ferocity;
        this.weight = weight;
    }

    /** True if this reforge may be rolled onto the given gear class. */
    public boolean fits(Kind gear) {
        if (kind == Kind.ANY || kind == Kind.GENERIC) return true;
        return kind == gear;
    }

    public static Reforge byName(String name) {
        if (name == null) return null;
        for (Reforge r : values()) {
            if (r.name().equalsIgnoreCase(name)) return r;
        }
        return null;
    }

    /** The stat lines this reforge contributes, in the exact format the stats engine reads. */
    public java.util.List<String> statLines() {
        java.util.List<String> out = new java.util.ArrayList<>();
        if (strength != 0) out.add("§7❁ Strength: " + sign(strength));
        if (defense != 0) out.add("§7❈ Defense: " + sign(defense));
        if (health != 0) out.add("§7❤ Health: " + sign(health));
        if (critChance != 0) out.add("§7☣ Crit Chance: " + sign(critChance) + "%");
        if (critDamage != 0) out.add("§7☠ Crit Damage: " + sign(critDamage) + "%");
        if (speed != 0) out.add("§7✦ Speed: " + sign(speed));
        if (intelligence != 0) out.add("§7✎ Intelligence: " + sign(intelligence));
        if (ferocity != 0) out.add("§7⫽ Ferocity: " + sign(ferocity));
        return out;
    }

    private static String sign(int v) {
        return v >= 0 ? "§a+" + v : "§c" + v;
    }
}
