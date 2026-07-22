package com.political;

import net.minecraft.ChatFormatting;

import java.util.*;

/**
 * JJK-style Techniques. Every player awakens exactly ONE technique at Magic level 5 via the
 * Rite of Awakening (consumes an Awakening Catalyst). 13 common techniques are freely rollable;
 * 11 exclusive techniques are server-unique (single holder each, tracked in
 * {@link TechniqueManager}). Each technique exposes 4-5 abilities gated behind a per-technique
 * mastery rank; ability effects are dispatched by {@link Archetype} in {@link TechniqueManager}.
 */
public enum Technique {
    // ── 10 Common (rollable by anyone) ──────────────────────────
    EMBERHEART("Emberheart", false, ChatFormatting.GOLD, "Fire coils in your veins.", List.of(
            new Ability("ember_lash",  "Ember Lash",  0, 15, 30,  "Hurl a searing bolt that ignites and burns.", Archetype.BURST, 10f, 0, 60),
            new Ability("cinder_nova", "Cinder Nova", 1, 30, 120, "Erupt in flame, torching all nearby foes.",   Archetype.NOVA,  9f, 6, 60),
            new Ability("pyre_charge", "Pyre Charge", 2, 25, 100, "Dash forward wreathed in fire.",              Archetype.BLINK, 8f, 4, 40),
            new Ability("phoenix_veil","Phoenix Veil",3, 40, 300, "Cloak of flame: resistance + fire immunity.", Archetype.BUFF,  0f, 0, 200))),

    GLACIER_SOUL("Glacier Soul", false, ChatFormatting.AQUA, "The cold answers your call.", List.of(
            new Ability("frost_lance","Frost Lance", 0, 15, 30,  "Impale a target in ice and slow it.",         Archetype.BURST, 11f, 0, 60),
            new Ability("rime_nova",  "Rime Nova",   1, 30, 120, "Freeze and shatter everything around you.",    Archetype.NOVA,  7f, 8, 60),
            new Ability("glacial_wall","Glacial Aegis",2,30,140, "Encase yourself in protective frost.",        Archetype.BARRIER,0f,0,60),
            new Ability("permafrost", "Permafrost",  3, 40, 240, "Root nearby foes solid in ice.",              Archetype.ROOT,  6f, 7, 80))),

    STORM_VEIN("Storm Vein", false, ChatFormatting.YELLOW, "Lightning runs your nerves.", List.of(
            new Ability("volt_strike","Volt Strike", 0, 15, 30,  "A cracking bolt to a single target.",         Archetype.BURST, 12f, 0, 40),
            new Ability("chain_storm","Chain Storm",  1, 30, 110, "Arcing storm across many foes.",              Archetype.NOVA,  8f, 8, 40),
            new Ability("thunder_step","Thunder Step",2, 25, 90,  "Blink in a crack of thunder.",                Archetype.BLINK, 6f, 5, 40),
            new Ability("overload",   "Overload",     3, 40, 260, "Haste + jump surge of raw voltage.",          Archetype.BUFF,  0f, 0, 200))),

    UMBRAL_STEP("Umbral Step", false, ChatFormatting.DARK_GRAY, "You slip between shadows.", List.of(
            new Ability("shade_slash","Shade Slash", 0, 15, 25,  "An armor-piercing shadow cut.",               Archetype.BURST, 10f, 0, 40),
            new Ability("void_blink", "Void Blink",  1, 20, 70,  "Teleport through the dark.",                  Archetype.BLINK, 0f, 12,0),
            new Ability("night_shroud","Night Shroud",2,30, 200, "Invisibility + speed for the hunt.",          Archetype.BUFF,  0f, 0, 160),
            new Ability("umbral_maw", "Umbral Maw",  3, 40, 200, "Rend a foe and steal its vitality.",          Archetype.DEVOUR,12f, 0, 0))),

    CRIMSON_PACT("Crimson Pact", false, ChatFormatting.DARK_RED, "Blood is power, freely spent.", List.of(
            new Ability("blood_spike","Blood Spike", 0, 12, 30,  "Lance a foe and drink its blood.",            Archetype.DEVOUR,9f, 0, 0),
            new Ability("hemo_nova",  "Hemorrhage",  1, 25, 100, "Bleed all nearby, healing yourself.",         Archetype.NOVA,  8f, 5, 60),
            new Ability("crimson_ward","Crimson Ward",2,30, 160, "Blood shield: absorption + regen.",           Archetype.BUFF,  0f, 0, 200),
            new Ability("sanguine_surge","Sanguine Surge",3,40,240,"Spend health for a devastating burst.",     Archetype.NOVA, 16f, 6, 40))),

    WILDGROWTH("Wildgrowth", false, ChatFormatting.GREEN, "Life blooms at your touch.", List.of(
            new Ability("thorn_lash", "Thorn Lash",  0, 14, 30,  "Whip a foe with barbed vines.",               Archetype.BURST, 9f, 0, 60),
            new Ability("bloom_ward", "Bloom Ward",   1, 25, 140, "Regeneration for you and allies near.",       Archetype.HEAL,  0f, 6, 120),
            new Ability("entangle",   "Entangle",     2, 30, 120, "Snare nearby foes in living roots.",          Archetype.ROOT,  5f, 6, 100),
            new Ability("verdant_form","Verdant Form",3,40, 220, "Bark-skin: resistance + health boost.",       Archetype.BUFF,  0f, 0, 220))),

    DAWNFORGED("Dawnforged", false, ChatFormatting.YELLOW, "Light forged into your soul.", List.of(
            new Ability("holy_smite", "Holy Smite",  0, 15, 30,  "Smite a foe; bonus vs the undead.",           Archetype.BURST, 11f, 0, 40),
            new Ability("radiant_nova","Radiant Nova",1,30, 110, "A burst of searing light around you.",        Archetype.NOVA,  9f, 5, 40),
            new Ability("aegis_light","Aegis of Light",2,30,150, "Ward of light: absorption + resistance.",     Archetype.BARRIER,0f,0,80),
            new Ability("dawnbreak",  "Dawnbreak",   3, 40, 240, "Heal fully and bless nearby allies.",         Archetype.HEAL,  0f, 8, 160))),

    IRONMARROW("Ironmarrow", false, ChatFormatting.GRAY, "Your bones are living steel.", List.of(
            new Ability("iron_smash", "Iron Smash",  0, 15, 30,  "A crushing strike that staggers.",            Archetype.BURST, 10f, 0, 40),
            new Ability("bulwark",    "Bulwark",      1, 25, 140, "Resistance + slow-fall bastion stance.",      Archetype.BUFF,  0f, 0, 200),
            new Ability("quake_stomp","Quake Stomp",  2, 30, 120, "Slam the ground, launching foes.",           Archetype.NOVA,  7f, 6, 40),
            new Ability("unbreakable","Unbreakable",  3, 40, 260, "Brief invulnerability window.",              Archetype.BARRIER,0f,0,60))),

    TIDEBOUND("Tidebound", false, ChatFormatting.BLUE, "The tide bends to your will.", List.of(
            new Ability("tide_lance", "Tide Lance",  0, 14, 30,  "A pressurized jet of water.",                 Archetype.BURST, 9f, 0, 40),
            new Ability("undertow",   "Undertow",     1, 25, 100, "Drag every nearby foe toward you.",           Archetype.TETHER,5f, 8, 40),
            new Ability("riptide_wave","Riptide Wave",2,30, 110, "A wave that hurls foes away.",                Archetype.NOVA,  7f, 7, 40),
            new Ability("deep_current","Deep Current",3,40, 200, "Dolphin's grace + water breathing.",          Archetype.BUFF,  0f, 0, 220))),

    WHISPERWIND("Whisperwind", false, ChatFormatting.WHITE, "The wind carries your intent.", List.of(
            new Ability("gale_cut",   "Gale Cut",    0, 12, 25,  "A razor of compressed air.",                  Archetype.BURST, 8f, 0, 40),
            new Ability("cyclone",    "Cyclone",      1, 25, 100, "Fling all nearby foes skyward.",             Archetype.NOVA,  6f, 7, 40),
            new Ability("zephyr_dash","Zephyr Dash",  2, 20, 70,  "Blink on the wind.",                         Archetype.BLINK, 0f, 10,0),
            new Ability("tempest_form","Tempest Form",3,40, 200, "Speed + jump + brief levitation.",           Archetype.BUFF,  0f, 0, 180))),

    // ── V4: 3 further commons ───────────────────────────────────
    RESONANT_ECHO("Resonant Echo", false, ChatFormatting.LIGHT_PURPLE,
            "Every sound you make comes back heavier than it left.", List.of(
            new Ability("tone_lance",   "Tone Lance",    0, 14, 30,  "A focused note punches straight through armor.", Archetype.BURST, 10f, 0, 40),
            new Ability("shatter_chord","Shatter Chord", 1, 28, 110, "A struck chord cracks bone and stone all around.", Archetype.NOVA,  8f, 7, 40),
            new Ability("echo_step",    "Echo Step",     2, 22, 80,  "Ride a returning sound to where it started.",   Archetype.BLINK, 5f, 9, 0),
            new Ability("standing_wave","Standing Wave", 3, 35, 200, "Hold one note; nearby foes are pinned in it.",  Archetype.ROOT,  6f, 6, 100))),

    SPOREBLOOM("Sporebloom", false, ChatFormatting.DARK_GREEN,
            "You exhale gardens, and the gardens are hungry.", List.of(
            new Ability("spore_dart",  "Spore Dart",     0, 13, 30,  "A dart of hardened fungus that festers in the wound.", Archetype.BURST, 9f, 0, 40),
            new Ability("bloom_burst", "Bloom Burst",    1, 26, 105, "Fruiting bodies erupt from the ground and rupture.",   Archetype.NOVA,  8f, 6, 40),
            new Ability("mycelial_net","Mycelial Net",   2, 30, 130, "Thread the soil with fungus that holds foes fast.",    Archetype.ROOT,  5f, 7, 110),
            new Ability("fungal_carapace","Fungal Carapace",3,38,220,"Grow living plate armor of bracket fungus.",           Archetype.BUFF,  0f, 0, 220))),

    LODESTONE_HEART("Lodestone Heart", false, ChatFormatting.DARK_GRAY,
            "Iron leans toward you. Given time, so does everything.", List.of(
            new Ability("magnet_slug", "Magnet Slug",    0, 15, 30,  "Rip a slug of raw iron clean through a target.", Archetype.BURST, 11f, 0, 40),
            new Ability("lodestone_pull","Lodestone Pull",1,26, 95,  "Drag every nearby foe into your orbit.",         Archetype.TETHER,6f, 8, 40),
            new Ability("polarity_snap","Polarity Snap", 2, 30, 115, "Reverse polarity and hurl everything outward.",  Archetype.NOVA,  9f, 7, 40),
            new Ability("ferrous_skin","Ferrous Skin",   3, 38, 230, "Sheathe yourself in suspended iron filings.",    Archetype.BUFF,  0f, 0, 220))),

    // ── 8 Exclusive (single holder server-wide) ─────────────────
    HOLLOW_INFINITY("Hollow Infinity", true, ChatFormatting.LIGHT_PURPLE, "Infinity separates you from all harm.", List.of(
            new Ability("infinity_touch","Infinity Touch",0,20,40,"A point of collapsed space punches a foe.",  Archetype.BURST, 14f, 0, 40),
            new Ability("blue_pull",  "Cursed Blue",  1, 30, 100, "Attractive void drags foes together.",       Archetype.TETHER,8f, 10,40),
            new Ability("red_burst",  "Cursed Red",   2, 40, 140, "Repulsive void detonates outward.",          Archetype.NOVA, 16f, 7, 40),
            new Ability("infinity_barrier","Infinity",3, 50, 300, "Untouchable: negate all damage briefly.",    Archetype.BARRIER,0f,0,80),
            new Ability("hollow_purple","Hollow Purple",4,80,600, "Imaginary mass annihilates a line of foes.", Archetype.METEOR,26f, 6, 40))),

    SHADOW_MONARCH("Shadow Monarch", true, ChatFormatting.DARK_PURPLE, "Arise. The dead answer your throne.", List.of(
            new Ability("dagger_rain","Shadow Dagger",0,18,40,  "Fling a shadow blade at a foe.",              Archetype.BURST, 12f, 0, 40),
            new Ability("arise",      "Arise",        1, 35, 160, "Summon 2 undead soldiers to fight for you.", Archetype.SUMMON, 0f, 0, 0),
            new Ability("shadow_exchange","Shadow Exchange",2,25,90,"Blink to a shadow you mark.",             Archetype.BLINK, 0f, 14,0),
            new Ability("monarch_domain","Monarch's Domain",3,50,300,"Weaken all foes; empower your thralls.",  Archetype.NOVA,  8f, 9, 100),
            new Ability("rulers_authority","Ruler's Authority",4,70,500,"Crush and drag every foe to their knees.",Archetype.TETHER,18f,12,60))),

    CHRONOSHEAR("Chronoshear", true, ChatFormatting.AQUA, "Time is a wound you can reopen.", List.of(
            new Ability("temporal_cut","Temporal Cut",0,18,40, "A blade that struck a moment ago.",            Archetype.BURST, 13f, 0, 40),
            new Ability("echo_mark",  "Echo",         1, 30, 160, "Record your position and health.",           Archetype.BUFF,  0f, 0, 0),
            new Ability("rewind",     "Rewind",       2, 40, 220, "Snap back to your recorded echo.",           Archetype.HEAL,  0f, 0, 0),
            new Ability("stasis_field","Stasis Field",3,45, 240, "Freeze nearby foes in stopped time.",         Archetype.ROOT,  6f, 8, 120),
            new Ability("time_collapse","Time Collapse",4,75,540,"Collapse a moment into devastating force.",   Archetype.METEOR,24f, 6, 40))),

    STARFALL_SOVEREIGN("Starfall Sovereign", true, ChatFormatting.GOLD, "The sky bows and falls for you.", List.of(
            new Ability("star_bolt", "Star Bolt",    0, 18, 40,  "A falling shard strikes your target.",        Archetype.BURST, 13f, 0, 40),
            new Ability("comet_dash","Comet Dash",   1, 25, 100, "Streak across the sky as a comet.",           Archetype.BLINK, 9f, 6, 40),
            new Ability("meteor_call","Meteor",       2, 40, 180, "Call a meteor down on a point.",             Archetype.METEOR,18f, 5, 40),
            new Ability("star_ward", "Astral Ward",   3, 45, 220, "Absorption + resistance under the stars.",    Archetype.BUFF,  0f, 0, 200),
            new Ability("meteor_barrage","Meteor Barrage",4,80,600,"Rain a barrage of meteors across the field.",Archetype.METEOR,30f,8, 40))),

    THREADWEAVER("Threadweaver", true, ChatFormatting.WHITE, "Every soul dangles on your strings.", List.of(
            new Ability("thread_cut", "Thread Cut",  0, 16, 35,  "A wire so fine it parts armor.",              Archetype.BURST, 12f, 0, 40),
            new Ability("puppet_pull","Puppet Pull",  1, 28, 90,  "Reel every nearby foe to you.",              Archetype.TETHER,7f, 9, 40),
            new Ability("marionette", "Marionette",   2, 35, 160, "Suspend and root foes in a web.",            Archetype.ROOT,  6f, 8, 100),
            new Ability("cats_cradle","Cat's Cradle", 3, 45, 220, "Lattice of wire shreds all around.",         Archetype.NOVA, 14f, 7, 40),
            new Ability("severance",  "Severance",     4, 70, 480, "Cut the strings — a lethal ranged burst.",   Archetype.BURST, 26f, 0, 40))),

    ABYSSAL_MAW("Abyssal Maw", true, ChatFormatting.DARK_GRAY, "The deep hungers, and it is you.", List.of(
            new Ability("gnaw",       "Gnaw",        0, 16, 35,  "Bite a foe and drink its essence.",           Archetype.DEVOUR,11f, 0, 0),
            new Ability("maw_pull",   "Maw's Pull",   1, 28, 90,  "The abyss drags foes into reach.",           Archetype.TETHER,6f, 9, 40),
            new Ability("consume",    "Consume",      2, 40, 160, "Rend a foe and heal massively.",             Archetype.DEVOUR,20f, 0, 0),
            new Ability("abyssal_dive","Abyssal Dive",3,30, 120, "Phase into the void, resistance surge.",      Archetype.BUFF,  0f, 0, 160),
            new Ability("devour_all", "Devour",       4, 75, 540, "Swallow everything nearby whole.",           Archetype.NOVA, 28f, 8, 40))),

    MIRROR_SOVEREIGN("Mirror Sovereign", true, ChatFormatting.LIGHT_PURPLE, "What is done to you is done to them.", List.of(
            new Ability("glass_shard","Glass Shard",  0, 16, 35,  "A shard of mirror slices a foe.",            Archetype.BURST, 12f, 0, 40),
            new Ability("reflection", "Reflection",   1, 30, 140, "Reflect the next damage back at attackers.", Archetype.MIRROR,0f, 0, 100),
            new Ability("silver_ward","Silvered Ward",2,30, 150, "Mirror barrier negates a hit.",              Archetype.BARRIER,0f,0,60),
            new Ability("shatterback","Shatterback",  3, 45, 220, "Detonate stored reflections outward.",       Archetype.NOVA, 15f, 7, 40),
            new Ability("hall_mirrors","Hall of Mirrors",4,70,480,"Copy and return the last spell used on you.", Archetype.MIRROR,22f,0, 0))),

    WORLDROOT_AVATAR("Worldroot Avatar", true, ChatFormatting.DARK_GREEN, "The world's roots move for you.", List.of(
            new Ability("root_spear", "Root Spear",  0, 16, 35,  "A spear of living root erupts at a foe.",     Archetype.BURST, 12f, 0, 40),
            new Ability("groundswell","Groundswell",  1, 28, 100, "Heave the earth, launching foes.",           Archetype.NOVA,  8f, 7, 40),
            new Ability("worldbind",  "Worldbind",    2, 35, 150, "Chain all nearby foes to the ground.",       Archetype.ROOT,  6f, 8, 120),
            new Ability("living_bark","Living Bark",  3, 40, 200, "Health boost + resistance from the grove.",   Archetype.BUFF,  0f, 0, 220),
            new Ability("avatar_form","Avatar of the Wild",4,75,540,"Become the grove: heal, root, and crush.",  Archetype.HEAL,  0f, 10,160))),

    // ── V4: 3 further EXCLUSIVES (single holder server-wide) ────
    SOVEREIGN_OF_ASH("Sovereign of Ash", true, ChatFormatting.RED,
            "What you burn does not end. It kneels.", List.of(
            new Ability("cinder_mark",  "Cinder Mark",     0, 20, 40,  "Brand a foe with a coal that will not go out.",      Archetype.BURST, 14f, 0, 40),
            new Ability("ashfall",      "Ashfall",         1, 32, 120, "Grey snow falls, choking and searing everything near.", Archetype.NOVA,  15f, 8, 40),
            new Ability("emberstride",  "Emberstride",     2, 28, 100, "Scatter into cinders and reform far ahead.",          Archetype.BLINK, 10f, 10, 0),
            new Ability("pyre_crown",   "Pyre Crown",      3, 45, 260, "A crown of fire: burning aura, strength, immunity.",  Archetype.BUFF,  0f, 0, 240),
            new Ability("ash_barrage",  "Ashen Dominion",  4, 80, 600, "The field becomes ash under a barrage of falling fire.", Archetype.METEOR,30f, 9, 40))),

    THE_LAST_WINTER("The Last Winter", true, ChatFormatting.DARK_AQUA,
            "You are not cold. You are the season that never lifts.", List.of(
            new Ability("hoarfrost_nail","Hoarfrost Nail", 0, 20, 40,  "A nail of black ice driven clean through a foe.",     Archetype.BURST, 14f, 0, 40),
            new Ability("whiteout",      "Whiteout",       1, 32, 130, "A killing squall erupts in every direction at once.", Archetype.NOVA,  15f, 8, 40),
            new Ability("glacial_tomb",  "Glacial Tomb",   2, 40, 200, "Entomb every nearby foe in ice that will not break.", Archetype.ROOT,  8f, 8, 140),
            new Ability("winter_mantle", "Mantle of Winter",3,45, 260, "Cold cannot reach you; you are where it comes from.", Archetype.BUFF,  0f, 0, 240),
            new Ability("absolute_zero", "Absolute Zero",  4, 78, 560, "Halt every mote of motion inside a killing radius.",  Archetype.NOVA,  30f, 10, 40))),

    VOICE_OF_RUIN("Voice of Ruin", true, ChatFormatting.DARK_RED,
            "Speak, and the world quietly agrees to end.", List.of(
            new Ability("edict",         "Edict",          0, 20, 40,  "One spoken word cracks a foe apart where it stands.", Archetype.BURST, 14f, 0, 40),
            new Ability("dirge",         "Dirge",          1, 30, 120, "A funeral note rolls out and breaks all it meets.",   Archetype.NOVA,  14f, 8, 40),
            new Ability("hush",          "Hush",           2, 36, 180, "Command silence; nothing nearby may move or act.",    Archetype.ROOT,  7f, 8, 140),
            new Ability("answering_air", "Answering Air",  3, 45, 240, "The air answers on your behalf and refuses all harm.", Archetype.BARRIER,0f, 0, 80),
            new Ability("unmaking_word", "The Unmaking Word",4,80,600, "Say the last word. Everything near it is undone.",    Archetype.NOVA,  32f, 10, 40)));

    public enum MasteryRank {
        NOVICE("Novice", 0),
        ADEPT("Adept", 15),
        EXPERT("Expert", 45),
        MASTER("Master", 100),
        TRANSCENDENT("Transcendent", 200);

        public final String display;
        public final int usesRequired;

        MasteryRank(String display, int usesRequired) {
            this.display = display;
            this.usesRequired = usesRequired;
        }

        /** Highest rank whose use threshold is met. */
        public static MasteryRank forUses(int uses) {
            MasteryRank best = NOVICE;
            for (MasteryRank r : values()) {
                if (uses >= r.usesRequired) best = r;
            }
            return best;
        }
    }

    public enum Archetype {
        BURST, NOVA, BLINK, BUFF, HEAL, SUMMON, METEOR, TETHER, BARRIER, DEVOUR, MIRROR, ROOT
    }

    /**
     * @param rankIndex ordinal into {@link MasteryRank#values()} required to use this ability.
     */
    public record Ability(String id, String name, int rankIndex, int energyCost, int cooldownTicks,
                          String description, Archetype archetype, float power, double radius, int durationTicks) {}

    public final String displayName;
    public final boolean exclusive;
    public final ChatFormatting color;
    public final String flavor;
    public final List<Ability> abilities;

    Technique(String displayName, boolean exclusive, ChatFormatting color, String flavor, List<Ability> abilities) {
        this.displayName = displayName;
        this.exclusive = exclusive;
        this.color = color;
        this.flavor = flavor;
        this.abilities = abilities;
    }

    public Ability abilityById(String id) {
        for (Ability a : abilities) {
            if (a.id().equals(id)) return a;
        }
        return null;
    }

    public static Technique byName(String name) {
        if (name == null) return null;
        try { return valueOf(name); } catch (IllegalArgumentException e) { return null; }
    }

    public static List<Technique> commons() {
        List<Technique> list = new ArrayList<>();
        for (Technique t : values()) if (!t.exclusive) list.add(t);
        return list;
    }

    public static List<Technique> exclusives() {
        List<Technique> list = new ArrayList<>();
        for (Technique t : values()) if (t.exclusive) list.add(t);
        return list;
    }
}
