package com.political.power;

import com.political.core.config.ConfigKey;

/**
 * Every number of the power system (M2) as a documented setting. The defaults are the numbers of the
 * program's capability set; the config reference (docs/CONFIG.md) is generated from this class.
 * Tick values are game ticks, 20 per second.
 */
public final class PowerSettings {

    private PowerSettings() {
    }

    // ── master switch and casting rules ─────────────────────────────────────────────────────

    public static final ConfigKey<Boolean> ENABLED = ConfigKey.bool(
            "power.enabled", true,
            "Master switch for the power system. When off, no technique can be cast (the cast is refused with SYSTEM_DISABLED).");

    public static final ConfigKey<Integer> GLOBAL_COOLDOWN_TICKS = ConfigKey.integer(
            "power.cast.global_cooldown_ticks", 10, 0, 200,
            "Ticks that must pass after any cast before the same player can cast again.");

    public static final ConfigKey<Integer> RATE_LIMIT_CASTS = ConfigKey.integer(
            "power.cast.rate_limit_casts", 8, 1, 100,
            "How many casts a player may make inside the rate limit window; the next one is refused with RATE_LIMITED.");

    public static final ConfigKey<Integer> RATE_LIMIT_WINDOW_TICKS = ConfigKey.integer(
            "power.cast.rate_limit_window_ticks", 20, 1, 1200,
            "Length of the rate limit window, in ticks.");

    // ── resource pool (C2) ──────────────────────────────────────────────────────────────────

    public static final ConfigKey<Integer> RESOURCE_BASE_MAX = ConfigKey.integer(
            "power.resource.base_max", 100, 1, 100_000,
            "Maximum of the cursed resource (the mana pool) for a player with no school tier, before bonuses from stats, vows and gear.");

    public static final ConfigKey<Integer> RESOURCE_PER_TIER = ConfigKey.integer(
            "power.resource.max_per_tier", 25, 0, 10_000,
            "How much the maximum grows for every school tier the player has reached (tier 0 to 4).");

    public static final ConfigKey<Integer> RESOURCE_REGEN_DELAY_TICKS = ConfigKey.integer(
            "power.resource.regen_delay_ticks", 60, 0, 1200,
            "Ticks after spending the resource before it starts to come back.");

    public static final ConfigKey<Double> RESOURCE_REGEN_PER_SECOND = ConfigKey.decimal(
            "power.resource.regen_per_second", 4.0, 0.0, 1000.0,
            "Resource regained per second once regeneration has started. Regeneration never goes above the maximum.");

    public static final ConfigKey<Double> RESOURCE_DEATH_KEEP_FRACTION = ConfigKey.decimal(
            "power.resource.death_keep_fraction", 0.5, 0.0, 1.0,
            "Fraction of the current resource kept when the player dies (0.5 halves it).");

    // ── Blue, Red, Cursed Blast, Reverse (C3 to C6) ─────────────────────────────────────────

    public static final ConfigKey<Integer> BLUE_COST = ConfigKey.integer(
            "power.blue.cost", 15, 0, 10_000, "Resource cost of Blue, which pulls a target toward the caster.");
    public static final ConfigKey<Integer> BLUE_COOLDOWN_TICKS = ConfigKey.integer(
            "power.blue.cooldown_ticks", 80, 0, 72_000, "Cooldown of Blue, in ticks.");
    public static final ConfigKey<Double> BLUE_RANGE = ConfigKey.decimal(
            "power.blue.range", 10.0, 1.0, 128.0, "How far away Blue can pick its target, in blocks.");
    public static final ConfigKey<Double> BLUE_PULL = ConfigKey.decimal(
            "power.blue.pull_per_tick", 0.6, 0.0, 5.0, "Horizontal speed given to the target toward the caster, in blocks per tick.");
    public static final ConfigKey<Double> BLUE_VERTICAL_MIN = ConfigKey.decimal(
            "power.blue.vertical_min", -0.2, -5.0, 5.0, "Lowest vertical speed Blue may give the target.");
    public static final ConfigKey<Double> BLUE_VERTICAL_MAX = ConfigKey.decimal(
            "power.blue.vertical_max", 0.3, -5.0, 5.0, "Highest vertical speed Blue may give the target.");
    public static final ConfigKey<Boolean> BLUE_AFFECTS_BOSSES = ConfigKey.bool(
            "power.blue.affects_bosses", false, "Whether Blue and Red can move bosses. Off by default.");

    public static final ConfigKey<Integer> RED_COST = ConfigKey.integer(
            "power.red.cost", 20, 0, 10_000, "Resource cost of Red, which pushes a target away from the caster.");
    public static final ConfigKey<Integer> RED_COOLDOWN_TICKS = ConfigKey.integer(
            "power.red.cooldown_ticks", 100, 0, 72_000, "Cooldown of Red, in ticks.");
    public static final ConfigKey<Double> RED_RANGE = ConfigKey.decimal(
            "power.red.range", 8.0, 1.0, 128.0, "How far away Red can pick its target, in blocks.");
    public static final ConfigKey<Double> RED_PUSH = ConfigKey.decimal(
            "power.red.push_per_tick", 1.4, 0.0, 5.0, "Horizontal speed given to the target away from the caster, in blocks per tick.");
    public static final ConfigKey<Double> RED_LIFT = ConfigKey.decimal(
            "power.red.lift", 0.4, 0.0, 5.0, "Upward speed Red adds to the target.");
    public static final ConfigKey<Double> RED_DAMAGE = ConfigKey.decimal(
            "power.red.damage", 0.0, 0.0, 1000.0, "Damage Red deals on top of the push. Zero by default.");

    public static final ConfigKey<Integer> BLAST_COST = ConfigKey.integer(
            "power.blast.cost", 25, 0, 10_000, "Resource cost of Cursed Blast, an instant ray that hits the first living target.");
    public static final ConfigKey<Integer> BLAST_COOLDOWN_TICKS = ConfigKey.integer(
            "power.blast.cooldown_ticks", 40, 0, 72_000, "Cooldown of Cursed Blast, in ticks.");
    public static final ConfigKey<Double> BLAST_RANGE = ConfigKey.decimal(
            "power.blast.range", 24.0, 1.0, 128.0, "Reach of Cursed Blast, in blocks. The ray stops at the first solid block.");
    public static final ConfigKey<Double> BLAST_DAMAGE = ConfigKey.decimal(
            "power.blast.damage", 6.0, 0.0, 1000.0, "Damage of Cursed Blast. It uses a damage type that ignores armor.");

    public static final ConfigKey<Double> REVERSE_MAX_HEAL = ConfigKey.decimal(
            "power.reverse.max_heal", 8.0, 0.0, 1000.0, "Most health Reverse can restore in one cast, in half-hearts (2 = one heart).");
    public static final ConfigKey<Double> REVERSE_COST_PER_HP = ConfigKey.decimal(
            "power.reverse.cost_per_hp", 2.0, 0.0, 1000.0, "Resource charged for every point of health Reverse actually restores.");
    public static final ConfigKey<Integer> REVERSE_COOLDOWN_TICKS = ConfigKey.integer(
            "power.reverse.cooldown_ticks", 200, 0, 72_000, "Cooldown of Reverse, in ticks.");
    public static final ConfigKey<Double> REVERSE_RANGE = ConfigKey.decimal(
            "power.reverse.range", 6.0, 1.0, 128.0, "How far away Reverse can reach another player, in blocks.");

    // ── Barrier (C7) ────────────────────────────────────────────────────────────────────────

    public static final ConfigKey<Integer> BARRIER_COST = ConfigKey.integer(
            "power.barrier.cost", 30, 0, 10_000, "Resource cost of the Barrier.");
    public static final ConfigKey<Integer> BARRIER_COOLDOWN_TICKS = ConfigKey.integer(
            "power.barrier.cooldown_ticks", 300, 0, 72_000, "Cooldown of the Barrier, in ticks.");
    public static final ConfigKey<Integer> BARRIER_DURATION_TICKS = ConfigKey.integer(
            "power.barrier.duration_ticks", 40, 1, 1200, "How long the Barrier blocks incoming damage, in ticks.");
    public static final ConfigKey<Boolean> BARRIER_BLOCKS_FALL = ConfigKey.bool(
            "power.barrier.blocks_fall_damage", true, "Whether the Barrier also blocks fall damage.");
    public static final ConfigKey<Boolean> BARRIER_BLOCKS_VOID = ConfigKey.bool(
            "power.barrier.blocks_void_damage", false,
            "Whether the Barrier blocks void damage. Off: falling out of the world still kills, as nothing in the game can prevent it.");

    // ── Black Flash (C8) ────────────────────────────────────────────────────────────────────

    public static final ConfigKey<Integer> BLACK_FLASH_MIN_TIER = ConfigKey.integer(
            "power.black_flash.min_tier", 3, 0, 4, "Lowest school tier (0 to 4) at which Black Flash can happen.");
    public static final ConfigKey<Double> BLACK_FLASH_CHANCE = ConfigKey.decimal(
            "power.black_flash.chance", 0.05, 0.0, 1.0, "Chance that a qualifying melee hit becomes a Black Flash.");
    public static final ConfigKey<Double> BLACK_FLASH_MULTIPLIER = ConfigKey.decimal(
            "power.black_flash.multiplier", 2.5, 1.0, 100.0,
            "Damage multiplier of a Black Flash. It replaces the critical hit multiplier; the two never stack.");
    public static final ConfigKey<Integer> BLACK_FLASH_WINDOW_TICKS = ConfigKey.integer(
            "power.black_flash.window_ticks", 20, 1, 1200,
            "A melee hit only qualifies this many ticks after the player's last technique cast.");

    // ── Domain (C9) ─────────────────────────────────────────────────────────────────────────

    public static final ConfigKey<Integer> DOMAIN_COST = ConfigKey.integer(
            "power.domain.cost", 150, 0, 100_000, "Resource cost of a Domain, available from a school capstone.");
    public static final ConfigKey<Integer> DOMAIN_DURATION_TICKS = ConfigKey.integer(
            "power.domain.duration_ticks", 600, 20, 72_000, "How long a Domain lasts, in ticks.");
    public static final ConfigKey<Integer> DOMAIN_COOLDOWN_TICKS = ConfigKey.integer(
            "power.domain.cooldown_ticks", 2400, 0, 720_000, "Cooldown that starts when the Domain ends, in ticks.");
    public static final ConfigKey<Double> DOMAIN_RADIUS = ConfigKey.decimal(
            "power.domain.radius", 12.0, 1.0, 128.0, "Radius of a Domain around its caster, in blocks.");
    public static final ConfigKey<Double> DOMAIN_MIN_SEPARATION = ConfigKey.decimal(
            "power.domain.min_separation", 24.0, 0.0, 512.0,
            "A Domain cannot be cast if another one in the same dimension is closer than this, in blocks.");
    public static final ConfigKey<Double> DOMAIN_DAMAGE_BONUS = ConfigKey.decimal(
            "power.domain.damage_bonus", 0.25, 0.0, 10.0,
            "Extra damage the caster's melee deals inside the Domain (0.25 is +25 percent). Melee also ignores armor and the Defense stat there.");

    // ── Binding vows (C10) ──────────────────────────────────────────────────────────────────

    public static final ConfigKey<Integer> VOW_MAX_ACTIVE = ConfigKey.integer(
            "power.vow.max_active", 2, 0, 3, "How many binding vows a player can hold at once.");
    public static final ConfigKey<Integer> VOW_NO_ARMOR_BONUS = ConfigKey.integer(
            "power.vow.no_armor_resource_bonus", 20, 0, 10_000, "Maximum resource gained from the no-armor vow.");
    public static final ConfigKey<Double> VOW_NO_OFFHAND_COOLDOWN_REDUCTION = ConfigKey.decimal(
            "power.vow.no_offhand_cooldown_reduction", 0.10, 0.0, 0.9, "Share of every cooldown removed by the no-offhand vow.");
    public static final ConfigKey<Double> VOW_NO_SPRINT_DAMAGE_BONUS = ConfigKey.decimal(
            "power.vow.no_sprint_damage_bonus", 0.10, 0.0, 10.0, "Extra technique damage from the no-sprint vow (0.10 is +10 percent).");
    public static final ConfigKey<Integer> VOW_CHECK_INTERVAL_TICKS = ConfigKey.integer(
            "power.vow.check_interval_ticks", 20, 1, 1200, "How often vows are checked while a player plays, in ticks.");
    public static final ConfigKey<Integer> VOW_REACCEPT_COOLDOWN_TICKS = ConfigKey.integer(
            "power.vow.reaccept_cooldown_ticks", 6000, 0, 720_000, "Ticks a player must wait to take a vow again after breaking one.");
}
