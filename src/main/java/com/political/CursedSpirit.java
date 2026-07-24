package com.political;

import net.minecraft.world.entity.EntityType;

import java.util.List;

/**
 * V6.1 — an archetype of Cursed Spirit.
 *
 * <p>Every spirit in the world is a dressed vanilla mob. This record describes <i>which</i>
 * dressing to apply: a stable id, a display name, the vanilla {@link EntityType} it wears,
 * its {@link CursedGrade}, and the {@link Ability} flavour {@link CursedSpiritManager} drives
 * on tick. {@link #ARCHETYPES} holds the full roster (13 distinct curses across the grades).</p>
 */
public record CursedSpirit(String id, String name, EntityType<?> baseType,
                           CursedGrade grade, Ability ability, String flavour) {

    /** On-tick behaviour a spirit expresses. The manager reads this to decide its aura/effects. */
    public enum Ability {
        ROT_AURA,     // pulses Hunger/Weakness on nearby players
        WEB_SNARE,    // Slowness aura, tugs the careless
        VENOM,        // Poison aura
        CHILL,        // Weakness + Mining Fatigue aura
        CHOKE,        // Nausea + Slowness, throat curse
        DECAY,        // Wither aura
        DREAD,        // periodic Blindness/Darkness pulses
        PLAGUE,       // Hunger + Poison, thrown bile flavour
        WITHER_FLAME, // Wither + sets brief fire feel (particles)
        GAZE,         // Darkness + brief Levitation, flying watcher
        QUAKE,        // knock nearby players, Mining Fatigue
        DOMAIN,       // teleports toward its quarry, Strength on itself
        CATACLYSM     // Special Grade: rotating cocktail + roar + teleport
    }

    /** Entity tag that pins the archetype id so the codex can credit an "encountered" curse. */
    public String archTag() {
        return "political_arch_" + id;
    }

    public static CursedSpirit byId(String id) {
        if (id == null) return null;
        for (CursedSpirit s : ARCHETYPES) {
            if (s.id.equals(id)) return s;
        }
        return null;
    }

    /** All archetypes of one grade (used by weighted spawn selection). */
    public static List<CursedSpirit> ofGrade(CursedGrade grade) {
        java.util.ArrayList<CursedSpirit> out = new java.util.ArrayList<>();
        for (CursedSpirit s : ARCHETYPES) {
            if (s.grade == grade) out.add(s);
        }
        return out;
    }

    // ── The roster ──────────────────────────────────────────────────────────
    public static final List<CursedSpirit> ARCHETYPES = List.of(
            // Grade 4 — the ambient dread of any populated place.
            new CursedSpirit("finger_bearer", "Finger-Bearer", EntityType.ZOMBIE,
                    CursedGrade.GRADE_4, Ability.ROT_AURA,
                    "A shambling husk swollen around a cursed object it swallowed."),
            new CursedSpirit("veil_lurker", "Veil-Lurker", EntityType.SPIDER,
                    CursedGrade.GRADE_4, Ability.WEB_SNARE,
                    "It clings to the edge of sight and drags the careless into shadow."),
            new CursedSpirit("gnash_curse", "Gnash-Curse", EntityType.CAVE_SPIDER,
                    CursedGrade.GRADE_4, Ability.VENOM,
                    "Born of a bitten grudge; its fangs weep an old poison."),
            new CursedSpirit("wailing_shade", "Wailing Shade", EntityType.SKELETON,
                    CursedGrade.GRADE_4, Ability.CHILL,
                    "The echo of someone who died calling a name no one answered."),

            // Grade 3 — a curse with intent.
            new CursedSpirit("throat_spirit", "Throat-Spirit", EntityType.HUSK,
                    CursedGrade.GRADE_3, Ability.CHOKE,
                    "It settles in the windpipe of the grieving and squeezes."),
            new CursedSpirit("rot_curse", "Rot-Curse", EntityType.ZOMBIE_VILLAGER,
                    CursedGrade.GRADE_3, Ability.DECAY,
                    "Rooms it passes through go soft and brown with rot."),
            new CursedSpirit("hollow_wailer", "Hollow Wailer", EntityType.SKELETON,
                    CursedGrade.GRADE_3, Ability.DREAD,
                    "A grief that learned to scream instead of weep."),

            // Grade 2 — a name whispered among sorcerers.
            new CursedSpirit("bile_spirit", "Bile-Spirit", EntityType.WITCH,
                    CursedGrade.GRADE_2, Ability.PLAGUE,
                    "It boils resentment into bile and spits it at the living."),
            new CursedSpirit("ashen_revenant", "Ashen Revenant", EntityType.WITHER_SKELETON,
                    CursedGrade.GRADE_2, Ability.WITHER_FLAME,
                    "What is left when a curse burns everything, including itself."),
            new CursedSpirit("eye_cluster", "Eye-Cluster", EntityType.PHANTOM,
                    CursedGrade.GRADE_2, Ability.GAZE,
                    "A knot of a hundred watching eyes that will not close."),

            // Grade 1 — a disaster in the shape of a body.
            new CursedSpirit("womb_curse", "Womb-Curse", EntityType.RAVAGER,
                    CursedGrade.GRADE_1, Ability.QUAKE,
                    "A curse grown fat enough to birth curses of its own."),
            new CursedSpirit("domain_wraith", "Domain-Wraith", EntityType.VINDICATOR,
                    CursedGrade.GRADE_1, Ability.DOMAIN,
                    "It carries a scrap of a domain and folds space to reach you."),

            // Special Grade — the ones the old texts refuse to name.
            new CursedSpirit("disgraced_one", "The Disgraced One", EntityType.RAVAGER,
                    CursedGrade.SPECIAL_GRADE, Ability.CATACLYSM,
                    "A Special Grade. Its true name has been struck from every register.")
    );
}
