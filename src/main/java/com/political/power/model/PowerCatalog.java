package com.political.power.model;

import com.political.power.PowerSettings;
import com.political.technique.Technique;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Every technique and ability of the power system and where each one lives (docs/POWER_DESIGN.md).
 * Techniques are the unit of power; a school's tree grants them at set spine nodes. The eight older
 * techniques keep their four abilities, unlocked by mastery rank; the cursed techniques have one ability each
 * whose numbers come from the settings.
 *
 * <p>The mapping of techniques onto schools is data in this class, so the owner can re-map it in one
 * place. Schools with no technique yet are dormant: they cannot be chosen.</p>
 */
public final class PowerCatalog {

    /** Ids of the cursed techniques added in M2. */
    public static final String BLUE = "BLUE";
    public static final String RED = "RED";
    public static final String CURSED_BLAST = "CURSED_BLAST";
    public static final String REVERSE = "REVERSE";
    public static final String BARRIER = "BARRIER";
    public static final String DOMAIN = "DOMAIN";

    /**
     * A technique.
     *
     * @param id         stable id
     * @param schoolId   the school that grants it (the name of a {@code MagicSchool}), or null if only a capstone does
     * @param unlockNode spine node (1 to 10) of that school that grants it; 0 when {@code schoolId} is null
     * @param legacy     one of the eight older techniques, whose abilities unlock by mastery rank
     */
    public record TechniqueDef(String id, String schoolId, int unlockNode, boolean legacy) {
    }

    private static final Map<String, TechniqueDef> TECHNIQUES = new LinkedHashMap<>();

    private static void add(String id, String school, int node, boolean legacy) {
        TECHNIQUES.put(id, new TechniqueDef(id, school, node, legacy));
    }

    static {
        add("EMBERHEART", "PYROMANCY", 1, true);
        add("GLACIER_SOUL", "FROSTWEAVING", 1, true);
        add("TIDEBOUND", "FROSTWEAVING", 4, true);
        add("STORM_VEIN", "STORMCALLING", 1, true);
        add("WHISPERWIND", "STORMCALLING", 4, true);
        add("UMBRAL_STEP", "VOIDWALKING", 1, true);
        add(CURSED_BLAST, "VOIDWALKING", 4, false);
        add("WILDGROWTH", "VERDANCY", 1, true);
        add("IRONMARROW", "GRAVITAS", 1, true);
        add(BLUE, "GRAVITAS", 4, false);
        add(RED, "GRAVITAS", 7, false);
        add(REVERSE, "RADIANCE", 1, false);
        add(BARRIER, "RADIANCE", 4, false);
        add(DOMAIN, null, 0, false);
    }

    private PowerCatalog() {
    }

    public static List<TechniqueDef> techniques() {
        return List.copyOf(TECHNIQUES.values());
    }

    public static Optional<TechniqueDef> technique(String id) {
        return Optional.ofNullable(id == null ? null : TECHNIQUES.get(id));
    }

    public static boolean isTechnique(String id) {
        return id != null && TECHNIQUES.containsKey(id);
    }

    /** Techniques a school grants, in the order of their spine nodes. */
    public static List<TechniqueDef> techniquesOf(String schoolId) {
        List<TechniqueDef> out = new ArrayList<>();
        for (TechniqueDef def : TECHNIQUES.values()) {
            if (schoolId != null && schoolId.equals(def.schoolId())) {
                out.add(def);
            }
        }
        out.sort((a, b) -> Integer.compare(a.unlockNode(), b.unlockNode()));
        return out;
    }

    /** Schools that grant at least one technique; the others are dormant and cannot be chosen. */
    public static Set<String> selectableSchools() {
        Set<String> out = new java.util.LinkedHashSet<>();
        for (TechniqueDef def : TECHNIQUES.values()) {
            if (def.schoolId() != null) {
                out.add(def.schoolId());
            }
        }
        return out;
    }

    /** Techniques the tree grants a player whose highest unlocked spine node in {@code schoolId} is {@code highestNode}. */
    public static List<TechniqueDef> grantedBy(String schoolId, int highestNode) {
        List<TechniqueDef> out = new ArrayList<>();
        for (TechniqueDef def : techniquesOf(schoolId)) {
            if (highestNode >= def.unlockNode()) {
                out.add(def);
            }
        }
        return out;
    }

    // ── abilities ───────────────────────────────────────────────────────────────────────────

    /** Abilities of a technique, built from the current settings. */
    public static List<AbilitySpec> abilities(String techniqueId) {
        if (techniqueId == null) {
            return List.of();
        }
        return switch (techniqueId) {
            case BLUE -> List.of(AbilitySpec.aimed("blue", BLUE, AbilityKind.BLUE,
                    PowerSettings.BLUE_COST.get(), PowerSettings.BLUE_COOLDOWN_TICKS.get(), PowerSettings.BLUE_RANGE.get(), true));
            case RED -> List.of(AbilitySpec.aimed("red", RED, AbilityKind.RED,
                    PowerSettings.RED_COST.get(), PowerSettings.RED_COOLDOWN_TICKS.get(), PowerSettings.RED_RANGE.get(), true));
            case CURSED_BLAST -> List.of(AbilitySpec.aimed("cursed_blast", CURSED_BLAST, AbilityKind.BLAST,
                    PowerSettings.BLAST_COST.get(), PowerSettings.BLAST_COOLDOWN_TICKS.get(), PowerSettings.BLAST_RANGE.get(), true));
            case REVERSE -> List.of(new AbilitySpec("reverse", REVERSE, AbilityKind.REVERSE, 0.0,
                    PowerSettings.REVERSE_COOLDOWN_TICKS.get(), PowerSettings.REVERSE_RANGE.get(), true, true, true, false));
            case BARRIER -> List.of(AbilitySpec.selfOnly("barrier", BARRIER, AbilityKind.BARRIER,
                    PowerSettings.BARRIER_COST.get(), PowerSettings.BARRIER_COOLDOWN_TICKS.get()));
            case DOMAIN -> List.of(AbilitySpec.selfOnly("domain", DOMAIN, AbilityKind.DOMAIN,
                    PowerSettings.DOMAIN_COST.get(), PowerSettings.DOMAIN_COOLDOWN_TICKS.get()));
            default -> legacyAbilities(techniqueId);
        };
    }

    /** One ability by id across all techniques, or empty. */
    public static Optional<AbilitySpec> ability(String abilityId) {
        if (abilityId == null) {
            return Optional.empty();
        }
        for (String technique : TECHNIQUES.keySet()) {
            for (AbilitySpec spec : abilities(technique)) {
                if (spec.id().equals(abilityId)) {
                    return Optional.of(spec);
                }
            }
        }
        return Optional.empty();
    }

    /** The first ability of a technique, which is what a freshly equipped technique selects. */
    public static Optional<AbilitySpec> firstAbility(String techniqueId) {
        List<AbilitySpec> all = abilities(techniqueId);
        return all.isEmpty() ? Optional.empty() : Optional.of(all.get(0));
    }

    /** Mastery rank index an ability needs (0 to 3); the cursed techniques need none. */
    public static int rankIndexOf(AbilitySpec spec) {
        if (spec.kind() != AbilityKind.LEGACY) {
            return 0;
        }
        Technique technique = Technique.byName(spec.techniqueId());
        if (technique == null) {
            return 0;
        }
        Technique.Ability ability = technique.abilityById(spec.id());
        return ability == null ? 0 : ability.rankIndex();
    }

    /**
     * Targets of the 32 older abilities, from how their effect code behaves: the number is how far
     * ahead the ability looks for the entity it hits; 0 means it needs no target (a self effect or an area
     * around the caster). The older code did not check for walls, so none of them require line of sight.
     */
    private static final Map<String, Double> LEGACY_TARGET_RANGE = Map.ofEntries(
            Map.entry("ember_lash", 20.0), Map.entry("frost_lance", 20.0), Map.entry("volt_strike", 22.0),
            Map.entry("shade_cut", 16.0), Map.entry("umbral_rend", 12.0), Map.entry("thorn_lash", 16.0),
            Map.entry("iron_smash", 8.0), Map.entry("tide_lance", 18.0), Map.entry("gale_cut", 16.0));

    private static List<AbilitySpec> legacyAbilities(String techniqueId) {
        Technique technique = Technique.byName(techniqueId);
        if (technique == null) {
            return List.of();
        }
        List<AbilitySpec> out = new ArrayList<>();
        for (Technique.Ability ability : technique.abilities) {
            double range = LEGACY_TARGET_RANGE.getOrDefault(ability.id(), 0.0);
            if (range > 0.0) {
                out.add(new AbilitySpec(ability.id(), techniqueId, AbilityKind.LEGACY, ability.energyCost(),
                        ability.cooldownTicks(), range, true, false, false, false));
            } else {
                out.add(AbilitySpec.selfOnly(ability.id(), techniqueId, AbilityKind.LEGACY, ability.energyCost(), ability.cooldownTicks()));
            }
        }
        return out;
    }
}
