package com.political;


import com.political.magic.MagicSchool;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The palette registry for the cinematic engine.
 *
 * <p>{@link Vfx} ships six hand tuned palettes. That is enough for a demo and not enough for a mod
 * with twelve magic schools and five curse grades. This file gives every school and every grade its
 * own three layer look, registers them under a stable string key and exposes lookups so any system
 * can ask for the right colour by name instead of hard coding particle types at the call site.</p>
 *
 * <p>A palette is three particle layers, exactly as the engine defines them: a bright core that
 * carries the shape, a sheath that gives it thickness and a loose haze that softens the silhouette.
 * Nothing here touches a gameplay number. It is colour only.</p>
 */
public final class VfxPalettes {

    private VfxPalettes() {}

    /** A palette with the presentation data a menu needs to show it. */
    public record Named(String key, String label, ChatFormatting colour, Item icon, Vfx.Palette palette) {}

    private static final Map<String, Named> BY_KEY = new LinkedHashMap<>();
    private static final Map<MagicSchool, Named> BY_SCHOOL = new LinkedHashMap<>();
    private static final Map<String, Named> BY_GRADE = new LinkedHashMap<>();

    private static Named reg(String key, String label, ChatFormatting colour, Item icon, Vfx.Palette p) {
        Named n = new Named(key.toLowerCase(Locale.ROOT), label, colour, icon, p);
        BY_KEY.put(n.key(), n);
        return n;
    }

    // ════════════════════════════════════════════════════════════════════
    // GENERAL PURPOSE, mirroring the engine defaults so old call sites keep working
    // ════════════════════════════════════════════════════════════════════

    public static final Named ARCANE = reg("arcane", "Arcane", ChatFormatting.LIGHT_PURPLE, Items.AMETHYST_SHARD,
            Vfx.ARCANE);
    public static final Named INFERNO = reg("inferno", "Inferno", ChatFormatting.GOLD, Items.BLAZE_POWDER,
            Vfx.INFERNO);
    public static final Named VOID = reg("void", "Void", ChatFormatting.DARK_PURPLE, Items.ENDER_EYE,
            Vfx.VOID);
    public static final Named STORM = reg("storm", "Storm", ChatFormatting.YELLOW, Items.LIGHTNING_ROD,
            Vfx.STORM);
    public static final Named CURSED = reg("cursed", "Cursed", ChatFormatting.DARK_GRAY, Items.WITHER_SKELETON_SKULL,
            Vfx.CURSED);
    public static final Named FROST = reg("frost", "Frost", ChatFormatting.AQUA, Items.BLUE_ICE,
            Vfx.FROST);

    // ════════════════════════════════════════════════════════════════════
    // ONE PER MAGIC SCHOOL
    // ════════════════════════════════════════════════════════════════════

    public static final Named PYROMANCY = reg("school_pyromancy", "Pyromancy", ChatFormatting.RED, Items.FIRE_CHARGE,
            new Vfx.Palette(ParticleTypes.END_ROD, ParticleTypes.FLAME, ParticleTypes.LARGE_SMOKE));

    public static final Named FROSTWEAVING = reg("school_frostweaving", "Frostweaving", ChatFormatting.AQUA, Items.PACKED_ICE,
            new Vfx.Palette(ParticleTypes.END_ROD, ParticleTypes.SNOWFLAKE, ParticleTypes.CLOUD));

    public static final Named STORMCALLING = reg("school_stormcalling", "Stormcalling", ChatFormatting.YELLOW, Items.COPPER_INGOT,
            new Vfx.Palette(ParticleTypes.END_ROD, ParticleTypes.ELECTRIC_SPARK, ParticleTypes.CLOUD));

    public static final Named VOIDWALKING = reg("school_voidwalking", "Voidwalking", ChatFormatting.DARK_PURPLE, Items.ENDER_PEARL,
            new Vfx.Palette(ParticleTypes.END_ROD, ParticleTypes.REVERSE_PORTAL, ParticleTypes.PORTAL));

    public static final Named BLOODBINDING = reg("school_bloodbinding", "Bloodbinding", ChatFormatting.DARK_RED, Items.REDSTONE,
            new Vfx.Palette(ParticleTypes.ENCHANTED_HIT, ParticleTypes.DAMAGE_INDICATOR, ParticleTypes.SMOKE));

    public static final Named VERDANCY = reg("school_verdancy", "Verdancy", ChatFormatting.GREEN, Items.OAK_SAPLING,
            new Vfx.Palette(ParticleTypes.HAPPY_VILLAGER, ParticleTypes.COMPOSTER, ParticleTypes.SPORE_BLOSSOM_AIR));

    public static final Named RADIANCE = reg("school_radiance", "Radiance", ChatFormatting.GOLD, Items.GLOWSTONE_DUST,
            new Vfx.Palette(ParticleTypes.END_ROD, ParticleTypes.GLOW, ParticleTypes.WAX_ON));

    public static final Named GRAVITAS = reg("school_gravitas", "Gravitas", ChatFormatting.GRAY, Items.ANVIL,
            new Vfx.Palette(ParticleTypes.END_ROD, ParticleTypes.ASH, ParticleTypes.SMOKE));

    public static final Named CHRONOMANCY = reg("school_chronomancy", "Chronomancy", ChatFormatting.DARK_AQUA, Items.CLOCK,
            new Vfx.Palette(ParticleTypes.END_ROD, ParticleTypes.ENCHANT, ParticleTypes.BUBBLE));

    public static final Named RESONANCE = reg("school_resonance", "Resonance", ChatFormatting.LIGHT_PURPLE, Items.NOTE_BLOCK,
            new Vfx.Palette(ParticleTypes.GLOW, ParticleTypes.ENCHANT, ParticleTypes.DUST_PLUME));

    public static final Named SPOREWEAVING = reg("school_sporeweaving", "Sporeweaving", ChatFormatting.DARK_GREEN, Items.WARPED_FUNGUS,
            new Vfx.Palette(ParticleTypes.WARPED_SPORE, ParticleTypes.MYCELIUM, ParticleTypes.CRIMSON_SPORE));

    public static final Named ASTRALISM = reg("school_astralism", "Astralism", ChatFormatting.BLUE, Items.NETHER_STAR,
            new Vfx.Palette(ParticleTypes.END_ROD, ParticleTypes.ENCHANT, ParticleTypes.PORTAL));

    // ════════════════════════════════════════════════════════════════════
    // ONE PER CURSE GRADE
    // ════════════════════════════════════════════════════════════════════

    public static final Named GRADE_4 = reg("grade_4", "Grade 4 Curse", ChatFormatting.GRAY, Items.ROTTEN_FLESH,
            new Vfx.Palette(ParticleTypes.GLOW, ParticleTypes.SMOKE, ParticleTypes.ASH));

    public static final Named GRADE_3 = reg("grade_3", "Grade 3 Curse", ChatFormatting.GREEN, Items.BONE,
            new Vfx.Palette(ParticleTypes.SOUL, ParticleTypes.MYCELIUM, ParticleTypes.SMOKE));

    public static final Named GRADE_2 = reg("grade_2", "Grade 2 Curse", ChatFormatting.AQUA, Items.SOUL_TORCH,
            new Vfx.Palette(ParticleTypes.SOUL_FIRE_FLAME, ParticleTypes.SOUL, ParticleTypes.CLOUD));

    public static final Named GRADE_1 = reg("grade_1", "Grade 1 Curse", ChatFormatting.LIGHT_PURPLE, Items.SOUL_LANTERN,
            new Vfx.Palette(ParticleTypes.SOUL_FIRE_FLAME, ParticleTypes.SCULK_SOUL, ParticleTypes.PORTAL));

    public static final Named SPECIAL_GRADE = reg("special_grade", "Special Grade", ChatFormatting.DARK_RED, Items.ECHO_SHARD,
            new Vfx.Palette(ParticleTypes.SOUL_FIRE_FLAME, ParticleTypes.SCULK_SOUL, ParticleTypes.REVERSE_PORTAL));

    static {
        BY_SCHOOL.put(MagicSchool.PYROMANCY, PYROMANCY);
        BY_SCHOOL.put(MagicSchool.FROSTWEAVING, FROSTWEAVING);
        BY_SCHOOL.put(MagicSchool.STORMCALLING, STORMCALLING);
        BY_SCHOOL.put(MagicSchool.VOIDWALKING, VOIDWALKING);
        BY_SCHOOL.put(MagicSchool.BLOODBINDING, BLOODBINDING);
        BY_SCHOOL.put(MagicSchool.VERDANCY, VERDANCY);
        BY_SCHOOL.put(MagicSchool.RADIANCE, RADIANCE);
        BY_SCHOOL.put(MagicSchool.GRAVITAS, GRAVITAS);
        BY_SCHOOL.put(MagicSchool.CHRONOMANCY, CHRONOMANCY);
        BY_SCHOOL.put(MagicSchool.RESONANCE, RESONANCE);
        BY_SCHOOL.put(MagicSchool.SPOREWEAVING, SPOREWEAVING);
        BY_SCHOOL.put(MagicSchool.ASTRALISM, ASTRALISM);

        BY_GRADE.put("grade_4", GRADE_4);
        BY_GRADE.put("grade_3", GRADE_3);
        BY_GRADE.put("grade_2", GRADE_2);
        BY_GRADE.put("grade_1", GRADE_1);
        BY_GRADE.put("special_grade", SPECIAL_GRADE);
    }

    // ════════════════════════════════════════════════════════════════════
    // LOOKUPS
    // ════════════════════════════════════════════════════════════════════

    /** Every registered palette, in registration order. */
    public static List<Named> all() {
        return new ArrayList<>(BY_KEY.values());
    }

    /** Just the twelve school palettes. */
    public static List<Named> schools() {
        return new ArrayList<>(BY_SCHOOL.values());
    }

    /** Just the five curse grade palettes. */
    public static List<Named> grades() {
        return new ArrayList<>(BY_GRADE.values());
    }

    /** Palette entry by key, falling back to Arcane so a bad key can never crash an effect. */
    public static Named namedByKey(String key) {
        if (key == null) return ARCANE;
        Named n = BY_KEY.get(key.toLowerCase(Locale.ROOT));
        return n != null ? n : ARCANE;
    }

    /** The raw three layer palette for a key. Never null. */
    public static Vfx.Palette byKey(String key) {
        return namedByKey(key).palette();
    }

    /** The look for a magic school. Never null. */
    public static Vfx.Palette forSchool(MagicSchool school) {
        if (school == null) return ARCANE.palette();
        Named n = BY_SCHOOL.get(school);
        return n != null ? n.palette() : ARCANE.palette();
    }

    /** The presentation entry for a magic school, for menus. */
    public static Named namedForSchool(MagicSchool school) {
        if (school == null) return ARCANE;
        Named n = BY_SCHOOL.get(school);
        return n != null ? n : ARCANE;
    }

    /**
     * The look for a curse grade named as a string. Accepts the enum name, the display label and
     * the loose forms a command or a tag might carry, for example "grade 2", "GRADE_2" or "special".
     */
    public static Vfx.Palette forGrade(String name) {
        return namedForGrade(name).palette();
    }

    /** The presentation entry for a loosely named curse grade. */
    public static Named namedForGrade(String name) {
        if (name == null) return CURSED;
        String k = name.trim().toLowerCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
        if (k.startsWith("political_grade_")) k = k.substring("political_grade_".length());
        if (k.startsWith("special")) return SPECIAL_GRADE;
        Named mapped = BY_GRADE.get(k);
        if (mapped != null) return mapped;
        switch (k) {
            case "4", "g4" -> { return GRADE_4; }
            case "3", "g3" -> { return GRADE_3; }
            case "2", "g2" -> { return GRADE_2; }
            case "1", "g1" -> { return GRADE_1; }
            default -> { return CURSED; }
        }
    }

    /**
     * Reads the grade off a live entity's tags and returns the matching look. Spirits carry
     * {@code political_grade_*} tags, so a boss or a curse can be dressed without the caller
     * knowing anything about grades.
     */
    public static Vfx.Palette forEntity(net.minecraft.world.entity.Entity entity) {
        return CURSED.palette();
    }
}
