package com.political;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * One hand-assigned, uniquely named full-set (4/4) bonus per block-armor material.
 *
 * Every entry pairs an {@link Archetype} (the mechanic) with its own numbers, particle
 * and display name, so no two materials play the same. Unknown prefixes fall back to
 * BlockArmorHandler's generated signature, so new materials never lose their bonus.
 *
 * Lore stays in sync automatically: BlockGearLore reads {@link #loreName(String)} /
 * {@link #loreDescription(String)}.
 */
public final class UniqueArmorSetBonuses {

    private UniqueArmorSetBonuses() {}

    // ════════════════════════════════════════════════════════════
    // MECHANIC ARCHETYPES
    // ════════════════════════════════════════════════════════════

    public enum Archetype {
        SCORCH_AURA("ignites hostiles within %R blocks"),
        PULSE_DAMAGE("magic burst hits hostiles within %R blocks for %P"),
        FROST_NOVA("chills hostiles within %R blocks"),
        SUPPRESSION("weakens hostiles within %R blocks"),
        VEIL("darkness pulse within %R blocks; you keep night vision"),
        MEND_PULSE("regenerates %P HP on a steady rhythm"),
        BULWARK("builds absorption shields over time"),
        WAR_RHYTHM("timed Haste + Strength combat windows"),
        SURGE("periodic forward surge of speed"),
        SKYSTEP("bursts of jump power and soft landings"),
        LIFELINE("emergency heal when dropping below 30%% HP"),
        RETRIBUTION("counter-blast hits attackers within %R blocks for %P"),
        STORM_CHARGE("every few hits taken discharges shock damage (%P) nearby"),
        REPULSE_FIELD("periodically shoves hostiles away"),
        GRAVITY_WELL("periodically drags hostiles toward you"),
        NIGHT_PROWL("Speed + Strength while in darkness"),
        PHOTOSYNTHESIS("regeneration + food while in daylight"),
        AQUATIC_GRACE("Dolphin's Grace + water breathing; swim like a current"),
        DEPTH_FOCUS("Haste + night vision while underground (Y < 20)"),
        LEECH_FIELD("drains %P HP from hostiles within %R blocks, healing you");

        final String descTemplate;

        Archetype(String descTemplate) {
            this.descTemplate = descTemplate;
        }
    }

    /** One material's unique full-set bonus. */
    public record SetBonus(String name, Archetype archetype, int cadence, int radius,
                           float power, int amplifier, ParticleOptions particle) {

        String describe() {
            return archetype.descTemplate
                    .replace("%R", String.valueOf(radius))
                    .replace("%P", trim(power));
        }

        private static String trim(float v) {
            return v == Math.floor(v) ? String.valueOf((int) v) : String.valueOf(v);
        }
    }

    // ════════════════════════════════════════════════════════════
    // REGISTRY — one unique entry per material prefix
    // ════════════════════════════════════════════════════════════

    private static final Map<String, SetBonus> REGISTRY = new LinkedHashMap<>();

    private static void e(String prefix, String name, Archetype arch, int cadence, int radius,
                          float power, int amplifier, ParticleOptions particle) {
        REGISTRY.put(prefix, new SetBonus(name, arch, cadence, radius, power, amplifier, particle));
    }

    static {
        // ── Overworld woods: logs lean sustain, planks lean tempo ──────────────
        e("oak_log", "Heartwood Guard", Archetype.BULWARK, 70, 3, 1.0f, 0, ParticleTypes.COMPOSTER);
        e("oak_planks", "Carpenter's Tempo", Archetype.WAR_RHYTHM, 62, 3, 1.0f, 0, ParticleTypes.CRIT);
        e("spruce_log", "Evergreen Vigil", Archetype.MEND_PULSE, 55, 3, 1.0f, 0, ParticleTypes.SPORE_BLOSSOM_AIR);
        e("spruce_planks", "Timberline March", Archetype.SURGE, 58, 3, 0.3f, 0, ParticleTypes.CLOUD);
        e("birch_log", "Pale Bark Ward", Archetype.RETRIBUTION, 40, 3, 2.0f, 0, ParticleTypes.END_ROD);
        e("birch_planks", "Whitewood Waltz", Archetype.SKYSTEP, 66, 2, 1.0f, 1, ParticleTypes.CLOUD);
        e("jungle_log", "Canopy Stride", Archetype.SURGE, 46, 3, 0.35f, 1, ParticleTypes.HAPPY_VILLAGER);
        e("jungle_planks", "Vine Dancer", Archetype.SKYSTEP, 52, 2, 1.0f, 2, ParticleTypes.SPORE_BLOSSOM_AIR);
        e("acacia_log", "Savanna Charge", Archetype.SURGE, 40, 3, 0.45f, 0, ParticleTypes.CRIT);
        e("acacia_planks", "Sunbaked Rhythm", Archetype.PHOTOSYNTHESIS, 48, 3, 1.0f, 0, ParticleTypes.HAPPY_VILLAGER);
        e("dark_oak_log", "Rootgloom Pact", Archetype.NIGHT_PROWL, 44, 3, 1.0f, 0, ParticleTypes.SMOKE);
        e("dark_oak_planks", "Umbral Timber", Archetype.VEIL, 68, 3, 1.0f, 0, ParticleTypes.LARGE_SMOKE);
        e("mangrove_log", "Tideroot Embrace", Archetype.AQUATIC_GRACE, 50, 3, 1.0f, 0, ParticleTypes.BUBBLE);
        e("mangrove_planks", "Brackish Bulwark", Archetype.BULWARK, 76, 3, 1.0f, 1, ParticleTypes.DRIPPING_WATER);
        e("mangrove_roots", "Rootsnare Field", Archetype.FROST_NOVA, 56, 3, 1.0f, 1, ParticleTypes.SPORE_BLOSSOM_AIR);
        e("muddy_mangrove_roots", "Mire Grip", Archetype.GRAVITY_WELL, 74, 4, 1.0f, 0, ParticleTypes.SPLASH);
        e("cherry_log", "Blossom Drift", Archetype.SKYSTEP, 48, 2, 1.0f, 1, ParticleTypes.CHERRY_LEAVES);
        e("cherry_planks", "Petal Cyclone", Archetype.REPULSE_FIELD, 62, 3, 1.0f, 0, ParticleTypes.CHERRY_LEAVES);
        e("bamboo_block", "Cane Sprint", Archetype.SURGE, 36, 2, 0.4f, 1, ParticleTypes.COMPOSTER);
        e("bamboo_planks", "Reedwall Riposte", Archetype.RETRIBUTION, 42, 3, 1.5f, 0, ParticleTypes.CRIT);
        e("crimson_stem", "Crimson Pulse", Archetype.LEECH_FIELD, 64, 3, 1.0f, 0, ParticleTypes.CRIMSON_SPORE);
        e("crimson_planks", "Nether Grain Fury", Archetype.WAR_RHYTHM, 54, 3, 1.0f, 1, ParticleTypes.CRIMSON_SPORE);
        e("warped", "Warped Whisper", Archetype.VEIL, 58, 4, 1.0f, 0, ParticleTypes.WARPED_SPORE);
        e("warped_planks", "Fungal Footwork", Archetype.SKYSTEP, 50, 2, 1.0f, 2, ParticleTypes.WARPED_SPORE);
        e("warped_wart_block", "Sporeheart Mend", Archetype.MEND_PULSE, 50, 3, 1.5f, 0, ParticleTypes.WARPED_SPORE);
        e("nether_wart_block", "Wartbloom Leech", Archetype.LEECH_FIELD, 58, 3, 1.5f, 0, ParticleTypes.CRIMSON_SPORE);

        // ── Stripped woods: lighter, faster variants ───────────────────────────
        e("stripped_acacia_log", "Barebark Bolt", Archetype.SURGE, 34, 2, 0.5f, 1, ParticleTypes.CLOUD);
        e("stripped_bamboo_block", "Split Cane Step", Archetype.SKYSTEP, 44, 2, 1.0f, 2, ParticleTypes.COMPOSTER);
        e("stripped_birch_log", "Sapline Mend", Archetype.MEND_PULSE, 48, 2, 1.0f, 0, ParticleTypes.HAPPY_VILLAGER);
        e("stripped_cherry_log", "Bloomcore Chime", Archetype.BULWARK, 64, 2, 1.0f, 0, ParticleTypes.CHERRY_LEAVES);
        e("stripped_crimson_stem", "Flayed Ember", Archetype.SCORCH_AURA, 60, 2, 1.0f, 0, ParticleTypes.FLAME);
        e("stripped_dark_oak_log", "Shadegrain Prowl", Archetype.NIGHT_PROWL, 40, 3, 1.0f, 1, ParticleTypes.SMOKE);
        e("stripped_jungle_log", "Lianaless Leap", Archetype.SKYSTEP, 42, 2, 1.0f, 3, ParticleTypes.HAPPY_VILLAGER);
        e("stripped_mangrove_log", "Peeled Tide Pull", Archetype.GRAVITY_WELL, 68, 3, 1.0f, 0, ParticleTypes.BUBBLE);
        e("stripped_spruce_log", "Heartplank Haste", Archetype.WAR_RHYTHM, 58, 2, 1.0f, 0, ParticleTypes.CRIT);
        e("stripped_warped_stem", "Bared Warp Veil", Archetype.VEIL, 52, 3, 1.0f, 1, ParticleTypes.WARPED_SPORE);

        // ── Stone family: defense and counters ─────────────────────────────────
        e("stone_bricks", "Mason's Bulwark", Archetype.BULWARK, 60, 3, 1.0f, 1, ParticleTypes.CRIT);
        e("smooth_stone", "Polished Poise", Archetype.MEND_PULSE, 60, 3, 0.75f, 0, ParticleTypes.CLOUD);
        e("cobblestone", "Rubble Riposte", Archetype.RETRIBUTION, 36, 2, 2.5f, 0, ParticleTypes.CRIT);
        e("cracked_stone_brick", "Fracture Feedback", Archetype.STORM_CHARGE, 3, 3, 2.0f, 0, ParticleTypes.CRIT);
        e("chiseled_stone_brick", "Runework Ward", Archetype.SUPPRESSION, 56, 3, 1.0f, 0, ParticleTypes.ENCHANT);
        e("mossy_cobblestone", "Mossback Mend", Archetype.PHOTOSYNTHESIS, 54, 3, 1.0f, 0, ParticleTypes.COMPOSTER);
        e("mossy", "Verdant Overgrowth", Archetype.MEND_PULSE, 44, 3, 1.25f, 0, ParticleTypes.COMPOSTER);
        e("andesite", "Grayrock Grit", Archetype.BULWARK, 72, 3, 1.0f, 0, ParticleTypes.POOF);
        e("polished_andesite", "Honed Grayrock", Archetype.RETRIBUTION, 44, 3, 2.0f, 0, ParticleTypes.POOF);
        e("diorite", "Speckle Shield", Archetype.REPULSE_FIELD, 70, 3, 0.8f, 0, ParticleTypes.CLOUD);
        e("polished_diorite", "Marble Rebuke", Archetype.RETRIBUTION, 48, 2, 2.25f, 0, ParticleTypes.END_ROD);
        e("granite", "Redgrain Wrath", Archetype.WAR_RHYTHM, 60, 3, 1.0f, 1, ParticleTypes.LAVA);
        e("polished_granite", "Ember Polish", Archetype.SCORCH_AURA, 64, 2, 1.0f, 0, ParticleTypes.LAVA);
        e("tuff", "Ashfall Guard", Archetype.SUPPRESSION, 62, 3, 1.0f, 1, ParticleTypes.WHITE_ASH);
        e("polished_tuff", "Cinder Sheen", Archetype.PULSE_DAMAGE, 58, 3, 2.0f, 0, ParticleTypes.ASH);
        e("calcite", "Chalkline Calm", Archetype.MEND_PULSE, 52, 3, 1.0f, 1, ParticleTypes.WHITE_ASH);
        e("dripstone", "Stalactite Spite", Archetype.RETRIBUTION, 38, 3, 3.0f, 0, ParticleTypes.DRIPPING_WATER);
        e("smooth_basalt", "Columnar Calm", Archetype.BULWARK, 66, 3, 1.0f, 1, ParticleTypes.ASH);
        e("basalt", "Basalt Barrage", Archetype.PULSE_DAMAGE, 62, 3, 2.5f, 0, ParticleTypes.ASH);
        e("polished_basalt", "Polished Deflection", Archetype.REPULSE_FIELD, 60, 3, 1.1f, 0, ParticleTypes.ASH);

        // ── Deepslate & underground ────────────────────────────────────────────
        e("deepslate", "Deepslate Doctrine", Archetype.DEPTH_FOCUS, 40, 3, 1.0f, 0, ParticleTypes.CRIT);
        e("cobbled_deepslate", "Undercity Counter", Archetype.RETRIBUTION, 40, 3, 2.5f, 0, ParticleTypes.SMOKE);
        e("chiseled_deepslate", "Abyssal Sigil", Archetype.VEIL, 60, 3, 1.0f, 0, ParticleTypes.SCULK_SOUL);
        e("reinforced_deepslate", "Unbreakable Core", Archetype.BULWARK, 50, 3, 1.0f, 2, ParticleTypes.END_ROD);
        e("sculk", "Sculk Resonance", Archetype.LEECH_FIELD, 52, 4, 1.5f, 0, ParticleTypes.SCULK_SOUL);

        // ── Blackstone family ──────────────────────────────────────────────────
        e("blackstone", "Blackreach Prowl", Archetype.NIGHT_PROWL, 42, 3, 1.0f, 0, ParticleTypes.LARGE_SMOKE);
        e("blackstone_bricks", "Bastion Brickwork", Archetype.BULWARK, 58, 3, 1.0f, 1, ParticleTypes.SMOKE);
        e("polished_blackstone", "Onyx Edge", Archetype.WAR_RHYTHM, 52, 3, 1.0f, 1, ParticleTypes.SMOKE);
        e("gilded_blackstone", "Gilded Greed", Archetype.LEECH_FIELD, 56, 3, 2.0f, 0, ParticleTypes.WAX_ON);

        // ── Nether stones ──────────────────────────────────────────────────────
        e("netherrack", "Netherflesh Burn", Archetype.SCORCH_AURA, 50, 3, 1.0f, 0, ParticleTypes.FLAME);
        e("nether_brick", "Kilnforged Wall", Archetype.RETRIBUTION, 44, 3, 2.0f, 0, ParticleTypes.FLAME);
        e("cracked_nether_brick", "Brimstone Feedback", Archetype.STORM_CHARGE, 4, 3, 3.0f, 0, ParticleTypes.LAVA);
        e("chiseled_nether_brick", "Hellmark Hex", Archetype.SUPPRESSION, 52, 3, 1.0f, 1, ParticleTypes.SOUL_FIRE_FLAME);
        e("magma", "Magma Mantle", Archetype.SCORCH_AURA, 40, 2, 1.0f, 1, ParticleTypes.LAVA);
        e("soul_sand", "Soulmire Drain", Archetype.LEECH_FIELD, 60, 3, 1.25f, 0, ParticleTypes.SOUL);
        e("soul_soil", "Soulflame Stride", Archetype.NIGHT_PROWL, 46, 3, 1.0f, 1, ParticleTypes.SOUL_FIRE_FLAME);
        e("ancient_debris", "Debris Aegis", Archetype.BULWARK, 46, 3, 1.0f, 2, ParticleTypes.FLAME);
        e("netherite_block", "Netherite Dominion", Archetype.PULSE_DAMAGE, 50, 3, 3.5f, 0, ParticleTypes.SOUL_FIRE_FLAME);
        e("nether_gold_ore", "Piglin's Promise", Archetype.WAR_RHYTHM, 56, 3, 1.0f, 0, ParticleTypes.WAX_ON);

        // ── Ice & snow ─────────────────────────────────────────────────────────
        e("ice", "Glacier Grasp", Archetype.FROST_NOVA, 48, 3, 1.0f, 1, ParticleTypes.SNOWFLAKE);
        e("packed_ice", "Packed Permafrost", Archetype.FROST_NOVA, 40, 3, 1.0f, 2, ParticleTypes.SNOWFLAKE);
        e("blue_ice", "Cryoshock", Archetype.PULSE_DAMAGE, 56, 3, 2.5f, 0, ParticleTypes.SNOWFLAKE);
        e("snow", "Whiteout Veil", Archetype.VEIL, 62, 3, 1.0f, 0, ParticleTypes.SNOWFLAKE);
        e("snow_block", "Blizzard Bulwark", Archetype.REPULSE_FIELD, 58, 3, 1.0f, 0, ParticleTypes.SNOWFLAKE);

        // ── Sand & desert ──────────────────────────────────────────────────────
        e("sand", "Dune Drift", Archetype.SURGE, 44, 3, 0.4f, 0, ParticleTypes.POOF);
        e("sandstone", "Sunstone Stamina", Archetype.PHOTOSYNTHESIS, 46, 3, 1.0f, 1, ParticleTypes.HAPPY_VILLAGER);
        e("red_sandstone", "Mesa Mirage", Archetype.VEIL, 64, 3, 1.0f, 1, ParticleTypes.POOF);
        e("terracotta", "Fired Clay Aegis", Archetype.BULWARK, 62, 3, 1.0f, 0, ParticleTypes.ASH);
        e("cactus", "Spineback", Archetype.RETRIBUTION, 30, 2, 2.0f, 0, ParticleTypes.COMPOSTER);

        // ── Ocean & prismarine ─────────────────────────────────────────────────
        e("prismarine", "Prismarine Current", Archetype.AQUATIC_GRACE, 44, 3, 1.0f, 0, ParticleTypes.BUBBLE);
        e("prismarine_bricks", "Guardian's Wake", Archetype.PULSE_DAMAGE, 54, 3, 2.5f, 0, ParticleTypes.BUBBLE_POP);
        e("dark_prismarine", "Abyss Undertow", Archetype.GRAVITY_WELL, 66, 4, 1.0f, 0, ParticleTypes.SQUID_INK);
        e("sea_lantern", "Lanternlight Ward", Archetype.SUPPRESSION, 50, 4, 1.0f, 0, ParticleTypes.END_ROD);
        e("sponge", "Sponge Soak", Archetype.LEECH_FIELD, 62, 3, 1.0f, 0, ParticleTypes.SPLASH);
        e("wet_sponge", "Saturated Slam", Archetype.REPULSE_FIELD, 54, 3, 1.2f, 0, ParticleTypes.SPLASH);
        e("lily_pad", "Padskip", Archetype.SKYSTEP, 40, 2, 1.0f, 1, ParticleTypes.SPLASH);

        // ── End family ─────────────────────────────────────────────────────────
        e("endstone", "Endshell Endurance", Archetype.BULWARK, 54, 3, 1.0f, 1, ParticleTypes.PORTAL);
        e("end_stone_brick", "Voidmason's Ward", Archetype.SUPPRESSION, 48, 3, 1.0f, 1, ParticleTypes.PORTAL);
        e("purpur", "Purpur Phase", Archetype.VEIL, 54, 4, 1.0f, 1, ParticleTypes.REVERSE_PORTAL);

        // ── Copper family: everything sparks ───────────────────────────────────
        e("copper_block", "Live Wire", Archetype.STORM_CHARGE, 3, 3, 2.5f, 0, ParticleTypes.ELECTRIC_SPARK);
        e("copper_ingot", "Conductor's Pace", Archetype.SURGE, 42, 3, 0.4f, 1, ParticleTypes.ELECTRIC_SPARK);
        e("cut_copper", "Faceted Discharge", Archetype.STORM_CHARGE, 4, 4, 2.0f, 0, ParticleTypes.ELECTRIC_SPARK);
        e("chiseled_copper", "Etched Capacitor", Archetype.PULSE_DAMAGE, 60, 3, 2.0f, 0, ParticleTypes.ELECTRIC_SPARK);
        e("exposed_copper", "Oxidizing Edge", Archetype.RETRIBUTION, 46, 3, 2.0f, 0, ParticleTypes.ELECTRIC_SPARK);
        e("weathered_copper", "Weathered Ward", Archetype.BULWARK, 68, 3, 1.0f, 0, ParticleTypes.WAX_OFF);
        e("oxidised_copper", "Verdigris Hex", Archetype.SUPPRESSION, 58, 3, 1.0f, 0, ParticleTypes.WAX_OFF);
        e("waxed_cut_copper", "Waxlock Armor", Archetype.MEND_PULSE, 56, 3, 1.0f, 0, ParticleTypes.WAX_ON);

        // ── Metals & minerals ──────────────────────────────────────────────────
        e("iron_block", "Ironclad Rebuke", Archetype.RETRIBUTION, 42, 3, 2.5f, 0, ParticleTypes.CRIT);
        e("iron_nugget", "Scrapline Scrapper", Archetype.WAR_RHYTHM, 64, 3, 1.0f, 0, ParticleTypes.CRIT);
        e("raw_iron_block", "Raw Iron Resolve", Archetype.BULWARK, 62, 3, 1.0f, 1, ParticleTypes.POOF);
        e("gold_block", "Midas Aura", Archetype.LEECH_FIELD, 54, 3, 1.75f, 0, ParticleTypes.WAX_ON);
        e("gold_nugget", "Glitter Feint", Archetype.SURGE, 40, 3, 0.45f, 1, ParticleTypes.WAX_ON);
        e("raw_gold_block", "Unrefined Riches", Archetype.SCORCH_AURA, 58, 3, 1.0f, 0, ParticleTypes.WAX_ON);
        e("diamond_block", "Diamond Resonance", Archetype.PULSE_DAMAGE, 48, 3, 3.0f, 0, ParticleTypes.END_ROD);
        e("emerald", "Merchant's Guile", Archetype.SUPPRESSION, 54, 3, 1.0f, 1, ParticleTypes.HAPPY_VILLAGER);
        e("emerald_block", "Emerald Dominance", Archetype.GRAVITY_WELL, 62, 4, 1.0f, 0, ParticleTypes.HAPPY_VILLAGER);
        e("lapis_lazuli", "Scholar's Spark", Archetype.MEND_PULSE, 50, 3, 1.0f, 0, ParticleTypes.ENCHANT);
        e("lapis_block", "Enchanter's Bastion", Archetype.BULWARK, 56, 3, 1.0f, 1, ParticleTypes.ENCHANT);
        e("quartz", "Quartz Tempo", Archetype.WAR_RHYTHM, 48, 3, 1.0f, 1, ParticleTypes.END_ROD);
        e("amethyst", "Chime Ward", Archetype.RETRIBUTION, 36, 3, 2.0f, 0, ParticleTypes.END_ROD);
        e("amethyst_cluster", "Cluster Resonance", Archetype.PULSE_DAMAGE, 52, 3, 2.75f, 0, ParticleTypes.END_ROD);
        e("amethyst_shard", "Shardsong", Archetype.STORM_CHARGE, 3, 3, 2.25f, 0, ParticleTypes.END_ROD);
        e("coal", "Coalheart Prowl", Archetype.NIGHT_PROWL, 44, 3, 1.0f, 0, ParticleTypes.SMOKE);
        e("charcoal", "Charline Smokescreen", Archetype.VEIL, 56, 3, 1.0f, 0, ParticleTypes.LARGE_SMOKE);
        e("flint", "Flintspark Counter", Archetype.RETRIBUTION, 34, 2, 1.75f, 0, ParticleTypes.FLAME);
        e("bone_block", "Ossuary Aura", Archetype.SUPPRESSION, 46, 3, 1.0f, 1, ParticleTypes.WHITE_ASH);
        e("bone_meal", "Marrow Bloom", Archetype.PHOTOSYNTHESIS, 42, 3, 1.0f, 1, ParticleTypes.COMPOSTER);
        e("obsidian", "Obsidian Bastille", Archetype.BULWARK, 48, 3, 1.0f, 2, ParticleTypes.LARGE_SMOKE);
        e("crying_obsidian", "Weeping Ward", Archetype.LEECH_FIELD, 56, 3, 1.5f, 0, ParticleTypes.PORTAL);
        e("glass", "Glasscannon Gambit", Archetype.PULSE_DAMAGE, 44, 3, 3.25f, 0, ParticleTypes.END_ROD);
        e("glowstone", "Radiant Rebuff", Archetype.REPULSE_FIELD, 50, 3, 1.0f, 0, ParticleTypes.GLOW);
        e("redstone", "Redstone Overclock", Archetype.WAR_RHYTHM, 40, 3, 1.0f, 1, ParticleTypes.ELECTRIC_SPARK);
        e("lodestone", "Lodestone Lure", Archetype.GRAVITY_WELL, 58, 4, 1.0f, 0, ParticleTypes.ENCHANT);
        e("target", "Bullseye Bane", Archetype.PULSE_DAMAGE, 46, 4, 2.0f, 0, ParticleTypes.CRIT);
        e("tnt", "Short Fuse", Archetype.STORM_CHARGE, 5, 4, 4.0f, 0, ParticleTypes.LAVA);
        e("brick", "Kiln Discipline", Archetype.MEND_PULSE, 58, 3, 1.0f, 0, ParticleTypes.ASH);
        e("mud_bricks", "Mudwall Stand", Archetype.BULWARK, 64, 3, 1.0f, 0, ParticleTypes.SPLASH);

        // ── Nature & farmland ──────────────────────────────────────────────────
        e("moss_block", "Mossmantle Mending", Archetype.MEND_PULSE, 42, 3, 1.5f, 0, ParticleTypes.COMPOSTER);
        e("mycelium", "Mycelial Web", Archetype.GRAVITY_WELL, 60, 3, 1.0f, 0, ParticleTypes.MYCELIUM);
        e("hay", "Harvest Vigor", Archetype.PHOTOSYNTHESIS, 40, 3, 1.5f, 0, ParticleTypes.COMPOSTER);
        e("pumpkin", "Gourd Guard", Archetype.REPULSE_FIELD, 56, 3, 1.0f, 0, ParticleTypes.COMPOSTER);
        e("melon", "Melon Momentum", Archetype.SURGE, 48, 3, 0.35f, 1, ParticleTypes.ITEM_SLIME);
        e("honeycomb", "Hivebound Shield", Archetype.BULWARK, 58, 3, 1.0f, 0, ParticleTypes.FALLING_HONEY);
        e("slime", "Slimestep Slam", Archetype.SKYSTEP, 38, 2, 1.0f, 2, ParticleTypes.ITEM_SLIME);
        e("shroomlight", "Sporelight Halo", Archetype.PHOTOSYNTHESIS, 44, 3, 1.25f, 1, ParticleTypes.GLOW);
        e("ochre_froglight", "Ochre Beacon", Archetype.SUPPRESSION, 50, 4, 1.0f, 0, ParticleTypes.GLOW);
        e("verdant_froglight", "Verdant Beacon", Archetype.MEND_PULSE, 46, 3, 1.25f, 0, ParticleTypes.GLOW);
        e("pearlescent_froglight", "Pearl Beacon", Archetype.SKYSTEP, 46, 2, 1.0f, 2, ParticleTypes.GLOW);
    }

    // ════════════════════════════════════════════════════════════
    // PER-PLAYER STATE (proc cooldowns / hit counters)
    // ════════════════════════════════════════════════════════════

    private static final Map<UUID, Integer> hitCounters = new HashMap<>();
    private static final Map<UUID, Long> lifelineCooldown = new HashMap<>();

    public static boolean isRegistered(String prefix) {
        return REGISTRY.containsKey(prefix);
    }

    public static String loreName(String prefix) {
        SetBonus b = REGISTRY.get(prefix);
        return b == null ? null : b.name();
    }

    public static String loreDescription(String prefix) {
        SetBonus b = REGISTRY.get(prefix);
        return b == null ? null : b.describe();
    }

    /**
     * Tick the unique bonus for a player wearing a full 4/4 set of {@code prefix}.
     * Returns false when the prefix has no registered bonus (caller falls back).
     */
    public static boolean tick(ServerPlayer player, ServerLevel sl, String prefix) {
        SetBonus bonus = REGISTRY.get(prefix);
        if (bonus == null) return false;

        switch (bonus.archetype()) {
            case SCORCH_AURA -> {
                if (onCadence(player, bonus)) {
                    ensure(player, MobEffects.FIRE_RESISTANCE, 90, 0);
                    forNearbyMonsters(player, bonus.radius(), m -> m.setRemainingFireTicks(50 + bonus.amplifier() * 30));
                    ring(sl, player, bonus.particle(), 8);
                }
            }
            case PULSE_DAMAGE -> {
                if (onCadence(player, bonus)) {
                    boolean[] hit = {false};
                    forNearbyMonsters(player, bonus.radius(), m -> {
                        m.hurtServer(sl, player.damageSources().magic(), bonus.power());
                        hit[0] = true;
                    });
                    if (hit[0]) {
                        ring(sl, player, bonus.particle(), 10);
                        sl.playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP,
                                SoundSource.PLAYERS, 0.5f, 1.5f);
                    }
                }
            }
            case FROST_NOVA -> {
                if (onCadence(player, bonus)) {
                    forNearbyMonsters(player, bonus.radius(), m ->
                            m.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, bonus.amplifier(), true, false, false)));
                    ring(sl, player, bonus.particle(), 9);
                }
            }
            case SUPPRESSION -> {
                if (onCadence(player, bonus)) {
                    forNearbyMonsters(player, bonus.radius(), m ->
                            m.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 70, bonus.amplifier(), true, false, false)));
                    ring(sl, player, bonus.particle(), 7);
                }
            }
            case VEIL -> {
                ensure(player, MobEffects.NIGHT_VISION, 260, 0);
                if (onCadence(player, bonus)) {
                    forNearbyMonsters(player, bonus.radius(), m ->
                            m.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 50, 0, true, false, false)));
                    ring(sl, player, bonus.particle(), 8);
                }
            }
            case MEND_PULSE -> {
                if (onCadence(player, bonus) && player.getHealth() < player.getMaxHealth()) {
                    player.heal(bonus.power());
                    burst(sl, player, bonus.particle(), 4);
                }
            }
            case BULWARK -> {
                if (onCadence(player, bonus)) {
                    ensure(player, MobEffects.ABSORPTION, 140, bonus.amplifier());
                    burst(sl, player, bonus.particle(), 5);
                }
            }
            case WAR_RHYTHM -> {
                if (onCadence(player, bonus)) {
                    ensure(player, MobEffects.HASTE, 80, bonus.amplifier());
                    ensure(player, MobEffects.STRENGTH, 70, 0);
                    burst(sl, player, bonus.particle(), 6);
                }
            }
            case SURGE -> {
                if (onCadence(player, bonus) && player.isSprinting()) {
                    Vec3 look = player.getLookAngle().normalize().scale(bonus.power());
                    player.push(look.x, 0.04, look.z);
                    burst(sl, player, bonus.particle(), 6);
                }
            }
            case SKYSTEP -> {
                ensure(player, MobEffects.JUMP_BOOST, 60, bonus.amplifier());
                if (player.fallDistance > 2.5f) {
                    ensure(player, MobEffects.SLOW_FALLING, 40, 0);
                    if (player.tickCount % 4 == 0) burst(sl, player, bonus.particle(), 2);
                }
            }
            case LIFELINE -> {
                if (player.getHealth() < player.getMaxHealth() * 0.3f) {
                    long now = sl.getServer().getTickCount();
                    long last = lifelineCooldown.getOrDefault(player.getUUID(), -10000L);
                    if (now - last >= 1200) { // 60s internal cooldown
                        lifelineCooldown.put(player.getUUID(), now);
                        player.heal(8.0f);
                        ensure(player, MobEffects.RESISTANCE, 100, 1);
                        burst(sl, player, ParticleTypes.TOTEM_OF_UNDYING, 24);
                        sl.playSound(null, player.blockPosition(), SoundEvents.TOTEM_USE,
                                SoundSource.PLAYERS, 0.6f, 1.4f);
                        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                "§a§lLIFELINE! §r§7Your armor pulls you back from the brink"), true);
                    }
                }
            }
            case RETRIBUTION -> {
                if (player.hurtTime == 10) {
                    boolean[] hit = {false};
                    forNearbyMonsters(player, bonus.radius(), m -> {
                        m.hurtServer(sl, player.damageSources().thorns(player), bonus.power());
                        hit[0] = true;
                    });
                    if (hit[0]) {
                        ring(sl, player, bonus.particle(), 12);
                        sl.playSound(null, player.blockPosition(), SoundEvents.THORNS_HIT,
                                SoundSource.PLAYERS, 0.7f, 1.0f);
                    }
                }
            }
            case STORM_CHARGE -> {
                if (player.hurtTime == 10) {
                    int hits = hitCounters.merge(player.getUUID(), 1, Integer::sum);
                    if (hits >= bonus.cadence()) { // cadence doubles as "hits to charge"
                        hitCounters.put(player.getUUID(), 0);
                        forNearbyMonsters(player, bonus.radius(), m -> {
                            m.hurtServer(sl, player.damageSources().magic(), bonus.power());
                            m.knockback(0.6f, player.getX() - m.getX(), player.getZ() - m.getZ());
                        });
                        ring(sl, player, bonus.particle(), 16);
                        sl.playSound(null, player.blockPosition(), SoundEvents.LIGHTNING_BOLT_IMPACT,
                                SoundSource.PLAYERS, 0.5f, 1.6f);
                    }
                }
            }
            case REPULSE_FIELD -> {
                if (onCadence(player, bonus)) {
                    boolean[] hit = {false};
                    forNearbyMonsters(player, bonus.radius(), m -> {
                        m.knockback(1.0f + bonus.power() * 0.3f, player.getX() - m.getX(), player.getZ() - m.getZ());
                        hit[0] = true;
                    });
                    if (hit[0]) ring(sl, player, bonus.particle(), 10);
                }
            }
            case GRAVITY_WELL -> {
                if (onCadence(player, bonus)) {
                    boolean[] hit = {false};
                    forNearbyMonsters(player, bonus.radius(), m -> {
                        Vec3 pull = new Vec3(player.getX() - m.getX(), 0.05, player.getZ() - m.getZ())
                                .normalize().scale(0.55);
                        m.push(pull.x, pull.y, pull.z);
                        hit[0] = true;
                    });
                    if (hit[0]) ring(sl, player, bonus.particle(), 10);
                }
            }
            case NIGHT_PROWL -> {
                int light = sl.getMaxLocalRawBrightness(player.blockPosition());
                if (light <= 7) {
                    ensure(player, MobEffects.SPEED, 60, bonus.amplifier());
                    ensure(player, MobEffects.STRENGTH, 60, 0);
                    if (player.tickCount % 30 == 0) burst(sl, player, bonus.particle(), 3);
                }
            }
            case PHOTOSYNTHESIS -> {
                if (sl.isBrightOutside() && sl.canSeeSky(player.blockPosition())) {
                    if (onCadence(player, bonus)) {
                        if (player.getHealth() < player.getMaxHealth()) player.heal(bonus.power());
                        player.getFoodData().eat(1, 0.2f);
                        burst(sl, player, bonus.particle(), 4);
                    }
                }
            }
            case AQUATIC_GRACE -> {
                if (player.isInWater()) {
                    ensure(player, MobEffects.DOLPHINS_GRACE, 60, bonus.amplifier());
                    ensure(player, MobEffects.WATER_BREATHING, 120, 0);
                    if (player.tickCount % 20 == 0) burst(sl, player, bonus.particle(), 4);
                }
            }
            case DEPTH_FOCUS -> {
                if (player.getY() < 20) {
                    ensure(player, MobEffects.HASTE, 60, bonus.amplifier());
                    ensure(player, MobEffects.NIGHT_VISION, 260, 0);
                    if (player.tickCount % 40 == 0) burst(sl, player, bonus.particle(), 3);
                }
            }
            case LEECH_FIELD -> {
                if (onCadence(player, bonus)) {
                    float[] drained = {0f};
                    forNearbyMonsters(player, bonus.radius(), m -> {
                        m.hurtServer(sl, player.damageSources().magic(), bonus.power());
                        drained[0] += bonus.power() * 0.5f;
                    });
                    if (drained[0] > 0) {
                        player.heal(Math.min(3.0f, drained[0]));
                        burst(sl, player, ParticleTypes.HEART, 2);
                        ring(sl, player, bonus.particle(), 8);
                    }
                }
            }
        }
        return true;
    }

    // ════════════════════════════════════════════════════════════
    // HELPERS
    // ════════════════════════════════════════════════════════════

    private static boolean onCadence(ServerPlayer player, SetBonus bonus) {
        return player.tickCount % Math.max(20, bonus.cadence()) == 0;
    }

    private static void ensure(ServerPlayer player, net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect,
                               int duration, int amplifier) {
        MobEffectInstance cur = player.getEffect(effect);
        if (cur == null || cur.getDuration() < duration / 4 || cur.getAmplifier() < amplifier) {
            player.addEffect(new MobEffectInstance(effect, duration, amplifier, true, false, false));
        }
    }

    private static void forNearbyMonsters(ServerPlayer player, int radius,
                                          java.util.function.Consumer<LivingEntity> action) {
        for (LivingEntity e : player.level().getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(radius),
                ent -> ent != player && ent.isAlive() && ent instanceof Monster)) {
            action.accept(e);
        }
    }

    private static void ring(ServerLevel sl, ServerPlayer player, ParticleOptions particle, int points) {
        for (int i = 0; i < points; i++) {
            double angle = (Math.PI * 2 * i) / points;
            sl.sendParticles(particle,
                    player.getX() + Math.cos(angle) * 1.6, player.getY() + 0.4, player.getZ() + Math.sin(angle) * 1.6,
                    1, 0.05, 0.05, 0.05, 0.0);
        }
    }

    private static void burst(ServerLevel sl, ServerPlayer player, ParticleOptions particle, int count) {
        sl.sendParticles(particle, player.getX(), player.getY() + 1.0, player.getZ(),
                count, 0.35, 0.4, 0.35, 0.02);
    }
}
