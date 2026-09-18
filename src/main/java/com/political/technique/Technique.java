package com.political.technique;

import net.minecraft.ChatFormatting;

import java.util.List;

/**
 * Learned combat techniques. None are exclusive or clan-gated.
 * Rank up by using the equipped technique, not by a magic level gate.
 */
public enum Technique {
    EMBERHEART("Emberheart", ChatFormatting.GOLD, "Fire coils in your veins.", List.of(
            new Ability("ember_lash", "Ember Lash", 0, 16, 30, "Hurl a searing bolt that ignites the target."),
            new Ability("cinder_bloom", "Cinder Bloom", 1, 28, 100, "Detonate a ring of fire around you."),
            new Ability("pyre_dash", "Pyre Dash", 2, 24, 80, "Dash forward wreathed in flame."),
            new Ability("hearthguard", "Hearthguard", 3, 36, 220, "Fire immunity and a burning aura."))),

    GLACIER_SOUL("Glacier Soul", ChatFormatting.AQUA, "The cold answers your call.", List.of(
            new Ability("frost_lance", "Frost Lance", 0, 16, 30, "Impale a target in ice and slow it."),
            new Ability("rime_field", "Rime Field", 1, 28, 110, "Freeze the ground around you."),
            new Ability("glacial_aegis", "Glacial Aegis", 2, 30, 140, "Encase yourself in protective frost."),
            new Ability("permafrost", "Permafrost", 3, 38, 240, "Root nearby foes in ice."))),

    STORM_VEIN("Storm Vein", ChatFormatting.YELLOW, "Lightning runs your nerves.", List.of(
            new Ability("volt_strike", "Volt Strike", 0, 16, 28, "A cracking bolt to a single target."),
            new Ability("arc_chain", "Arc Chain", 1, 30, 100, "Lightning jumps between nearby foes."),
            new Ability("thunder_step", "Thunder Step", 2, 24, 80, "Blink in a crack of thunder."),
            new Ability("overload", "Overload", 3, 38, 240, "Haste and a shock aura."))),

    UMBRAL_STEP("Umbral Step", ChatFormatting.DARK_GRAY, "You slip between shadows.", List.of(
            new Ability("shade_cut", "Shade Cut", 0, 14, 24, "An armor-piercing shadow cut."),
            new Ability("void_slip", "Void Slip", 1, 20, 70, "Teleport through the dark."),
            new Ability("night_shroud", "Night Shroud", 2, 28, 180, "Invisibility and speed for a hunt."),
            new Ability("umbral_rend", "Umbral Rend", 3, 38, 200, "Rend a foe and steal its vitality."))),

    WILDGROWTH("Wildgrowth", ChatFormatting.GREEN, "Life blooms at your touch.", List.of(
            new Ability("thorn_lash", "Thorn Lash", 0, 14, 28, "Whip a foe with barbed vines."),
            new Ability("bloom_ward", "Bloom Ward", 1, 24, 140, "Regeneration for you and nearby allies."),
            new Ability("entangle", "Entangle", 2, 28, 120, "Snare nearby foes in living roots."),
            new Ability("verdant_form", "Verdant Form", 3, 38, 220, "Bark-skin: resistance and extra health."))),

    IRONMARROW("Ironmarrow", ChatFormatting.GRAY, "Your bones are living steel.", List.of(
            new Ability("iron_smash", "Iron Smash", 0, 16, 28, "A crushing strike that staggers."),
            new Ability("bulwark", "Bulwark", 1, 24, 140, "Resistance and knockback immunity."),
            new Ability("quake_stomp", "Quake Stomp", 2, 30, 120, "Slam the ground and launch foes."),
            new Ability("unbreakable", "Unbreakable", 3, 40, 260, "A brief window of near-invulnerability."))),

    TIDEBOUND("Tidebound", ChatFormatting.BLUE, "The tide bends to your will.", List.of(
            new Ability("tide_lance", "Tide Lance", 0, 14, 28, "A pressurized jet of water."),
            new Ability("undertow", "Undertow", 1, 24, 100, "Drag every nearby foe toward you."),
            new Ability("riptide", "Riptide", 2, 28, 110, "A wave that hurls foes away."),
            new Ability("deep_current", "Deep Current", 3, 36, 200, "Water breathing and swift swimming."))),

    WHISPERWIND("Whisperwind", ChatFormatting.WHITE, "The wind carries your intent.", List.of(
            new Ability("gale_cut", "Gale Cut", 0, 12, 22, "A razor of compressed air."),
            new Ability("cyclone", "Cyclone", 1, 24, 100, "Fling nearby foes skyward."),
            new Ability("zephyr_dash", "Zephyr Dash", 2, 18, 70, "Blink on the wind."),
            new Ability("tempest_form", "Tempest Form", 3, 36, 200, "Speed, jump, and a brief lift.")));

    public enum MasteryRank {
        NOVICE("Novice", 0),
        ADEPT("Adept", 15),
        EXPERT("Expert", 45),
        MASTER("Master", 100);

        public final String display;
        public final int usesRequired;

        MasteryRank(String display, int usesRequired) {
            this.display = display;
            this.usesRequired = usesRequired;
        }

        public static MasteryRank forUses(int uses) {
            MasteryRank best = NOVICE;
            for (MasteryRank rank : values()) {
                if (uses >= rank.usesRequired) best = rank;
            }
            return best;
        }
    }

    public record Ability(String id, String name, int rankIndex, int energyCost, int cooldownTicks, String description) {}

    public final String displayName;
    public final ChatFormatting color;
    public final String flavor;
    public final List<Ability> abilities;

    Technique(String displayName, ChatFormatting color, String flavor, List<Ability> abilities) {
        this.displayName = displayName;
        this.color = color;
        this.flavor = flavor;
        this.abilities = abilities;
    }

    public Ability abilityById(String id) {
        for (Ability ability : abilities) {
            if (ability.id().equals(id)) return ability;
        }
        return null;
    }

    public static Technique byName(String name) {
        if (name == null) return null;
        try {
            return valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static Technique byDisplay(String name) {
        if (name == null) return null;
        String key = name.trim().replace(' ', '_').toUpperCase();
        Technique direct = byName(key);
        if (direct != null) return direct;
        for (Technique technique : values()) {
            if (technique.displayName.equalsIgnoreCase(name.trim())) return technique;
        }
        return null;
    }

    public static List<Technique> all() {
        return List.of(values());
    }

    public static List<Technique> commons() {
        return all();
    }

    public static List<Technique> exclusives() {
        return List.of();
    }
}
