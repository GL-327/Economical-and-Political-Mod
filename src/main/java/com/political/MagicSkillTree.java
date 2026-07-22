package com.political;

import java.util.*;

public final class MagicSkillTree {

    public record SkillNode(
            String id,
            String name,
            String category,
            int xpCost,
            int levelRequired,
            String parentId,
            String description,
            String unlocksSpell
    ) {}

    public static final String[] CATEGORIES = {
            "ARCANE_KNOWLEDGE", "ELEMENTAL_MASTERY", "DARK_ARTS", "HOLY_LIGHT", "COSMIC_POWER"
    };
    public static final String[] CATEGORY_NAMES = {
            "Arcane Knowledge", "Elemental Mastery", "Dark Arts", "Holy Light", "Cosmic Power"
    };

    public static final List<SkillNode> NODES = List.of(
            // ── Arcane Knowledge ─────────────────────────────────────
            new SkillNode("ak1", "Mana Well I",      "ARCANE_KNOWLEDGE", 500,   1,  null, "+25 max mana", null),
            new SkillNode("ak2", "Mana Well II",     "ARCANE_KNOWLEDGE", 2000,  8,  "ak1", "+50 max mana", null),
            new SkillNode("ak3", "Quick Regen",      "ARCANE_KNOWLEDGE", 1500,  5,  "ak1", "+50% mana regen speed", null),
            new SkillNode("ak4", "Spell Power I",    "ARCANE_KNOWLEDGE", 5000,  12, "ak2", "+15% spell damage", null),
            new SkillNode("ak5", "Spell Power II",   "ARCANE_KNOWLEDGE", 15000, 20, "ak4", "+30% spell damage", null),
            new SkillNode("ak6", "Arcane Mastery",   "ARCANE_KNOWLEDGE", 30000, 30, "ak5", "-15% all spell cooldowns", null),

            // ── Elemental Mastery ────────────────────────────────────
            new SkillNode("em1", "Fire Initiate",    "ELEMENTAL_MASTERY", 300,   1,  null, "Unlock Fireball spell", "FIREBALL"),
            new SkillNode("em2", "Frost Initiate",   "ELEMENTAL_MASTERY", 1000,  3,  "em1", "Unlock Ice Spike spell", "ICE_SPIKE"),
            new SkillNode("em3", "Storm Caller",     "ELEMENTAL_MASTERY", 3000,  8,  "em2", "Unlock Lightning Strike", "LIGHTNING_STRIKE"),
            new SkillNode("em4", "Elemental Efficiency", "ELEMENTAL_MASTERY", 8000, 15, "em3", "-20% elemental mana cost", null),
            new SkillNode("em5", "Overcharge",       "ELEMENTAL_MASTERY", 20000, 25, "em4", "+25% elemental damage", null),
            new SkillNode("em6", "Elemental Fury",   "ELEMENTAL_MASTERY", 40000, 35, "em5", "-30% elemental cooldowns", null),

            // ── Dark Arts ────────────────────────────────────────────
            new SkillNode("da1", "Shadow Affinity",  "DARK_ARTS", 500,   2,  null, "Unlock Shadow Step", "SHADOW_STEP"),
            new SkillNode("da2", "Poison Mastery",   "DARK_ARTS", 1500,  5,  "da1", "Unlock Poison Cloud", "POISON_CLOUD"),
            new SkillNode("da3", "Soul Siphon",      "DARK_ARTS", 4000,  10, "da2", "Unlock Soul Drain", "SOUL_DRAIN"),
            new SkillNode("da4", "Dark Efficiency",  "DARK_ARTS", 10000, 18, "da3", "-20% dark spell mana cost", null),
            new SkillNode("da5", "Life Leech",       "DARK_ARTS", 25000, 28, "da4", "Soul Drain heals +50%", null),
            new SkillNode("da6", "Void Walk",        "DARK_ARTS", 45000, 38, "da5", "Shadow Step +4 block range", null),

            // ── Holy Light ───────────────────────────────────────────
            new SkillNode("hl1", "Healer's Touch",   "HOLY_LIGHT", 400,   1,  null, "Unlock Healing Wave", "HEALING_WAVE"),
            new SkillNode("hl2", "Divine Shield",    "HOLY_LIGHT", 1200,  4,  "hl1", "Unlock Arcane Shield", "ARCANE_SHIELD"),
            new SkillNode("hl3", "Greater Healing",  "HOLY_LIGHT", 3500,  10, "hl1", "Healing Wave +50% healing", null),
            new SkillNode("hl4", "Fortification",    "HOLY_LIGHT", 9000,  16, "hl2", "Arcane Shield +5 seconds", null),
            new SkillNode("hl5", "Resurrection",     "HOLY_LIGHT", 22000, 26, "hl4", "10% survive lethal hit at 1hp", null),
            new SkillNode("hl6", "Holy Aura",        "HOLY_LIGHT", 42000, 36, "hl3", "Heal grants Regen I to allies", null),

            // ── Cosmic Power ─────────────────────────────────────────
            new SkillNode("cp1", "Wind Mastery",     "COSMIC_POWER", 600,   3,  null, "Unlock Wind Gust", "WIND_GUST"),
            new SkillNode("cp2", "Mana Burst",       "COSMIC_POWER", 2000,  7,  "cp1", "Restore 50% mana (5m cd)", null),
            new SkillNode("cp3", "Meteor Strike",    "COSMIC_POWER", 15000, 40, "cp2", "Unlock Meteor spell", "METEOR"),
            new SkillNode("cp4", "Cosmic Efficiency","COSMIC_POWER", 25000, 42, "cp3", "-25% cosmic spell mana cost", null),
            new SkillNode("cp5", "Stellar Impact",   "COSMIC_POWER", 35000, 45, "cp4", "Meteor +40% damage", null),
            new SkillNode("cp6", "Cosmic Mastery",   "COSMIC_POWER", 50000, 48, "cp5", "+10% all damage, -10% all cd", null),

            // ── Late-game extensions ────────────────────────────────
            new SkillNode("ak7", "Runic Reservoir",  "ARCANE_KNOWLEDGE", 62000, 44, "ak6", "+75 max mana", null),
            new SkillNode("ak8", "Astral Focus",     "ARCANE_KNOWLEDGE", 78000, 50, "ak7", "+10% magic XP gain", null),
            new SkillNode("em7", "Pyroclasm",        "ELEMENTAL_MASTERY", 58000, 43, "em6", "Unlock Flame Wave mastery", "FLAME_WAVE"),
            new SkillNode("em8", "Absolute Zero",    "ELEMENTAL_MASTERY", 76000, 49, "em7", "Unlock Frost Nova mastery", "FROST_NOVA"),
            new SkillNode("da7", "Rift Chains",      "DARK_ARTS", 60000, 44, "da6", "Unlock Void Grasp mastery", "VOID_GRASP"),
            new SkillNode("da8", "Night Sovereign",  "DARK_ARTS", 82000, 50, "da7", "+10% dark spell damage", null),
            new SkillNode("hl7", "Seraph Chorus",    "HOLY_LIGHT", 60000, 44, "hl6", "Unlock Regen Aura mastery", "REGEN_AURA"),
            new SkillNode("hl8", "Aegis Eternal",    "HOLY_LIGHT", 82000, 50, "hl7", "+10% defensive casting bonuses", null),
            new SkillNode("cp7", "Stellar Core",     "COSMIC_POWER", 64000, 46, "cp6", "Unlock Arcane Nova mastery", "ARCANE_NOVA"),
            new SkillNode("cp8", "Event Horizon",    "COSMIC_POWER", 86000, 50, "cp7", "Unlock Star Shard mastery", "STAR_SHARD"),

            // ── V4: nodes for the 17 previously-orphaned spells ──────
            // Every MagicManager.Spell is now reachable from some node chain.

            // Arcane Knowledge — raw aether and mana economy
            new SkillNode("ak9",  "Aether Bolt",      "ARCANE_KNOWLEDGE", 800,   2,  "ak1",  "Unlock Arcane Burst", "ARCANE_BURST"),
            new SkillNode("ak10", "Blood Ledger",     "ARCANE_KNOWLEDGE", 3000,  10, "ak3",  "Unlock Life Tap", "LIFE_TAP"),
            new SkillNode("ak11", "Font of Mana",     "ARCANE_KNOWLEDGE", 28000, 30, "ak10", "Unlock Mana Surge", "MANA_SURGE"),

            // Elemental Mastery — the four elements in full
            new SkillNode("em9",  "Tidal Initiate",   "ELEMENTAL_MASTERY", 1200,  6,  "em2",  "Unlock Tide Push", "TIDE_PUSH"),
            new SkillNode("em10", "Arc Conduction",   "ELEMENTAL_MASTERY", 2500,  9,  "em3",  "Unlock Chain Spark", "CHAIN_SPARK"),
            new SkillNode("em11", "Solar Kindling",   "ELEMENTAL_MASTERY", 4500,  12, "em10", "Unlock Solar Flare", "SOLAR_FLARE"),
            new SkillNode("em12", "Rimepiercer",      "ELEMENTAL_MASTERY", 7000,  14, "em9",  "Unlock Ice Lance", "ICE_LANCE"),
            new SkillNode("em13", "Tectonic Grasp",   "ELEMENTAL_MASTERY", 12000, 19, "em4",  "Unlock Rock Spike", "ROCK_SPIKE"),

            // Dark Arts — nether, shadow and blood
            new SkillNode("da9",  "Nether Kindling",  "DARK_ARTS", 2200,  7,  "da1",  "Unlock Nether Bolt", "NETHER_BOLT"),
            new SkillNode("da10", "Umbral Artillery", "DARK_ARTS", 8000,  15, "da3",  "Unlock Shadow Bolt", "SHADOW_BOLT"),
            new SkillNode("da11", "Hemorrhage",       "DARK_ARTS", 18000, 21, "da10", "Unlock Blood Nova", "BLOOD_NOVA"),

            // Holy Light — souls, smiting and spectral grace
            new SkillNode("hl9",  "Soul Mender",      "HOLY_LIGHT", 2800,  9,  "hl1",  "Unlock Soul Spark", "SOUL_SPARK"),
            new SkillNode("hl10", "Consecration",     "HOLY_LIGHT", 8500,  16, "hl3",  "Unlock Holy Bolt", "HOLY_BOLT"),
            new SkillNode("hl11", "Spectral Grace",   "HOLY_LIGHT", 11000, 18, "hl2",  "Unlock Phantom Shift", "PHANTOM_SHIFT"),

            // Cosmic Power — wind, mass and the upper air
            new SkillNode("cp9",  "Zephyr Lore",      "COSMIC_POWER", 1400,  5,  "cp1",  "Unlock Wind Shear", "WIND_SHEAR"),
            new SkillNode("cp10", "Mass Manipulation","COSMIC_POWER", 5500,  12, "cp2",  "Unlock Gravity Well", "GRAVITY_WELL"),
            new SkillNode("cp11", "Thunderhead",      "COSMIC_POWER", 9000,  14, "cp10", "Unlock Storm Bolt", "STORM_BOLT")
    );

    private static final Map<String, SkillNode> BY_ID = new HashMap<>();
    static {
        for (SkillNode n : NODES) BY_ID.put(n.id, n);
    }

    public static SkillNode getNode(String id) {
        return BY_ID.get(id);
    }

    public static List<SkillNode> getNodesForCategory(String category) {
        return NODES.stream().filter(n -> n.category().equals(category)).toList();
    }

    /** Total node count — use this instead of a hardcoded number in GUIs. */
    public static int totalNodes() {
        return NODES.size();
    }

    /** Every spell name reachable from some node. Should cover all of MagicManager.Spell. */
    public static Set<String> allUnlockableSpells() {
        Set<String> out = new LinkedHashSet<>();
        for (SkillNode n : NODES) {
            if (n.unlocksSpell() != null) out.add(n.unlocksSpell());
        }
        return out;
    }

    // ════════════════════════════════════════════════════════════
    // PASSIVE BONUS COMPUTATION
    // ════════════════════════════════════════════════════════════

    public static int getManaBonus(Set<String> unlocks) {
        int bonus = 0;
        if (unlocks.contains("ak1")) bonus += 25;
        if (unlocks.contains("ak2")) bonus += 50;
        if (unlocks.contains("ak7")) bonus += 75;
        return bonus;
    }

    public static float getManaRegenBonus(Set<String> unlocks) {
        float bonus = 0;
        if (unlocks.contains("ak3")) bonus += 50;
        return bonus;
    }

    public static int getSpellDamageBonus(Set<String> unlocks) {
        int bonus = 0;
        if (unlocks.contains("ak4")) bonus += 15;
        if (unlocks.contains("ak5")) bonus += 30;
        if (unlocks.contains("em5")) bonus += 25;
        if (unlocks.contains("da8")) bonus += 10;
        if (unlocks.contains("cp6")) bonus += 10;
        return bonus;
    }

    public static int getCooldownReduction(Set<String> unlocks) {
        int pct = 0;
        if (unlocks.contains("ak6")) pct += 15;
        if (unlocks.contains("em6")) pct += 30;
        if (unlocks.contains("cp6")) pct += 10;
        return Math.min(pct, 50);
    }

    public static int getManaCostReduction(Set<String> unlocks) {
        int pct = 0;
        if (unlocks.contains("em4")) pct += 20;
        if (unlocks.contains("da4")) pct += 20;
        if (unlocks.contains("cp4")) pct += 25;
        return Math.min(pct, 50);
    }

    public static int getXpBonus(Set<String> unlocks) {
        return unlocks.contains("ak8") ? 10 : 0;
    }

    // ════════════════════════════════════════════════════════════
    // PERK CHECKS
    // ════════════════════════════════════════════════════════════

    public static boolean hasShadowStepRange(Set<String> unlocks)  { return unlocks.contains("da6"); }
    public static boolean hasGreaterHealing(Set<String> unlocks)   { return unlocks.contains("hl3"); }
    public static boolean hasFortification(Set<String> unlocks)    { return unlocks.contains("hl4"); }
    public static boolean hasResurrection(Set<String> unlocks)     { return unlocks.contains("hl5"); }
    public static boolean hasHolyAura(Set<String> unlocks)         { return unlocks.contains("hl6"); }
    public static boolean hasLifeLeech(Set<String> unlocks)        { return unlocks.contains("da5"); }
    public static boolean hasStellarImpact(Set<String> unlocks)    { return unlocks.contains("cp5"); }
    public static boolean hasManaBurst(Set<String> unlocks)        { return unlocks.contains("cp2"); }
}
