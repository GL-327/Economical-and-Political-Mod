package com.political;

import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;

/**
 * Hand-assigned distinct ability kits for the 95 numbered dragon chestplates
 * ({@code new_dragon_chestplate_01..95}).
 *
 * <p>Each numbered plate maps to a UNIQUE tuple of (archetype, buff effect, amplifier,
 * pulse interval, particle, sound). The tuple axes use pairwise-large cycle lengths so no
 * two of the 95 plates share the same full kit — every plate feels mechanically and
 * visually distinct instead of repeating the old (index-1)%15 cycle.</p>
 *
 * <p>{@link MoreDragonChestplateAbilities#tickAll} / {@code onHit} delegate the numbered
 * plates here; the 8 classic + 15 extended + 3 omega plates keep their bespoke handlers.</p>
 */
public final class DragonAbilityRegistry {

    private DragonAbilityRegistry() {}

    /** Number of numbered dragon chestplates handled by this registry. */
    public static final int NUMBERED_COUNT = 95;

    // ---- Archetypes: distinct tick behaviours ----------------------------------
    private static final int WARD = 0;        // periodic self-ward: buff + resistance
    private static final int HUNT = 1;        // aggressor: buff + strength while flying
    private static final int SWIFT = 2;       // mobility: buff + speed
    private static final int AURA_WEAKEN = 3; // debuffs nearby monsters (slowness)
    private static final int AURA_SEAR = 4;   // periodic magic damage to nearby monsters
    private static final int MEND = 5;        // sustain: buff + regeneration
    private static final int STORMCALL = 6;   // reactive: buff + spark burst
    private static final int ARCHETYPE_COUNT = 7;

    @SuppressWarnings("unchecked")
    private static final Holder<MobEffect>[] BUFFS = new Holder[]{
            MobEffects.STRENGTH,
            MobEffects.SPEED,
            MobEffects.RESISTANCE,
            MobEffects.REGENERATION,
            MobEffects.ABSORPTION,
            MobEffects.HASTE,
            MobEffects.JUMP_BOOST,
            MobEffects.FIRE_RESISTANCE,
            MobEffects.NIGHT_VISION,
            MobEffects.WATER_BREATHING,
            MobEffects.LUCK,
            MobEffects.SLOW_FALLING
    };

    private static final ParticleOptions[] PARTICLES = new ParticleOptions[]{
            ParticleTypes.FLAME,
            ParticleTypes.SOUL_FIRE_FLAME,
            ParticleTypes.SMALL_FLAME,
            ParticleTypes.ENCHANT,
            ParticleTypes.ENCHANTED_HIT,
            ParticleTypes.CRIT,
            ParticleTypes.CLOUD,
            ParticleTypes.END_ROD,
            ParticleTypes.ELECTRIC_SPARK,
            ParticleTypes.SNOWFLAKE,
            ParticleTypes.HAPPY_VILLAGER,
            ParticleTypes.PORTAL,
            ParticleTypes.LARGE_SMOKE,
            ParticleTypes.SCULK_SOUL,
            ParticleTypes.GLOW,
            ParticleTypes.WAX_ON
    };

    private static final SoundEvent[] SOUNDS = new SoundEvent[]{
            SoundEvents.AMETHYST_BLOCK_CHIME,
            SoundEvents.BEACON_ACTIVATE,
            SoundEvents.BLAZE_SHOOT,
            SoundEvents.ENDERMAN_TELEPORT,
            SoundEvents.FIRECHARGE_USE,
            SoundEvents.EXPERIENCE_ORB_PICKUP,
            SoundEvents.PLAYER_ATTACK_SWEEP,
            SoundEvents.TRIDENT_RETURN
    };

    /** A resolved ability kit for one numbered plate. */
    public record Kit(int archetype, Holder<MobEffect> buff, int amplifier, int interval,
                      ParticleOptions particle, SoundEvent sound) {}

    private static final Kit[] KITS = new Kit[NUMBERED_COUNT];

    static {
        for (int i = 0; i < NUMBERED_COUNT; i++) {
            int archetype = i % ARCHETYPE_COUNT;
            Holder<MobEffect> buff = BUFFS[i % BUFFS.length];
            int amplifier = (i / ARCHETYPE_COUNT) % 3;
            int interval = 30 + (i % 10) * 5;                 // 30..75 ticks
            ParticleOptions particle = PARTICLES[i % PARTICLES.length];
            SoundEvent sound = SOUNDS[i % SOUNDS.length];
            KITS[i] = new Kit(archetype, buff, amplifier, interval, particle, sound);
        }
    }

    /** True if the given id is one of the 95 numbered plates. */
    public static boolean isNumbered(String id) {
        return id != null && id.startsWith("new_dragon_chestplate_");
    }

    /** Kit for a 1-based numbered index (1..95), or null if out of range. */
    public static Kit kitFor(int index1Based) {
        if (index1Based < 1 || index1Based > NUMBERED_COUNT) return null;
        return KITS[index1Based - 1];
    }

    // ════════════════════════════════════════════════════════════
    // TICK
    // ════════════════════════════════════════════════════════════

    /** Passive per-tick effect for a numbered plate. Called from MoreDragonChestplateAbilities. */
    public static void tickNumbered(ServerPlayer player, int index1Based) {
        Kit kit = kitFor(index1Based);
        if (kit == null) return;
        ServerLevel world = (ServerLevel) player.level();

        // Dragon plates grant flight; flight carries a light hunger drawback so it isn't free.
        ArmorAbilityHandler.applyConditionalCreativeFlight(player, true);
        if (player.getAbilities().flying && player.tickCount % 20 == 0) {
            player.getFoodData().addExhaustion(0.4f);
        }

        boolean pulse = player.tickCount % kit.interval() == 0;

        switch (kit.archetype()) {
            case WARD -> {
                if (pulse) {
                    apply(player, kit.buff(), 140, kit.amplifier());
                    apply(player, MobEffects.RESISTANCE, 120, 0);
                }
            }
            case HUNT -> {
                if (pulse) {
                    apply(player, kit.buff(), 140, kit.amplifier());
                    if (player.getAbilities().flying) {
                        apply(player, MobEffects.STRENGTH, 120, 0);
                    }
                }
            }
            case SWIFT -> {
                if (pulse) {
                    apply(player, kit.buff(), 140, kit.amplifier());
                    apply(player, MobEffects.SPEED, 120, 0);
                }
            }
            case AURA_WEAKEN -> {
                if (pulse) {
                    apply(player, kit.buff(), 140, kit.amplifier());
                    for (Monster m : world.getEntitiesOfClass(Monster.class,
                            player.getBoundingBox().inflate(4.0), LivingEntity::isAlive)) {
                        m.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 1, true, false, true));
                        m.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0, true, false, true));
                    }
                }
            }
            case AURA_SEAR -> {
                if (pulse) {
                    apply(player, kit.buff(), 140, kit.amplifier());
                    float dmg = 1.5f + kit.amplifier();
                    for (Monster m : world.getEntitiesOfClass(Monster.class,
                            player.getBoundingBox().inflate(3.5), LivingEntity::isAlive)) {
                        m.hurtServer(world, player.damageSources().magic(), dmg);
                    }
                }
            }
            case MEND -> {
                if (pulse) {
                    apply(player, kit.buff(), 140, kit.amplifier());
                    apply(player, MobEffects.REGENERATION, 80, 0);
                }
            }
            case STORMCALL -> {
                if (pulse) {
                    apply(player, kit.buff(), 140, kit.amplifier());
                    world.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                            player.getX(), player.getY() + 1.0, player.getZ(),
                            10, 0.4, 0.6, 0.4, 0.1);
                }
            }
            default -> {
            }
        }

        trail(player, world, kit.particle());
        if (pulse) {
            world.playSound(null, player.blockPosition(), kit.sound(), SoundSource.PLAYERS, 0.35f, 1.0f);
        }
    }

    // ════════════════════════════════════════════════════════════
    // ON HIT
    // ════════════════════════════════════════════════════════════

    /** Per-hit rider for a numbered plate. Called from MoreDragonChestplateAbilities.onHit. */
    public static void onHitNumbered(ServerPlayer player, LivingEntity target, int index1Based) {
        Kit kit = kitFor(index1Based);
        if (kit == null || !(player.level() instanceof ServerLevel world)) return;

        switch (kit.archetype()) {
            case AURA_WEAKEN -> target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1, false, true, true));
            case AURA_SEAR -> {
                target.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0, false, true, true));
                target.setRemainingFireTicks(40);
            }
            case STORMCALL -> target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0, false, true, true));
            case HUNT -> target.hurtServer(world, player.damageSources().magic(), 2.0f + kit.amplifier());
            case MEND -> {
                float heal = Math.min(1.5f, player.getMaxHealth() - player.getHealth());
                if (heal > 0) player.heal(heal);
            }
            case WARD -> player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 80, 0, true, false, false));
            case SWIFT -> target.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 10, 0, false, true, true));
            default -> {
            }
        }
        world.sendParticles(kit.particle(),
                target.getX(), target.getY() + target.getBbHeight() * 0.6, target.getZ(),
                6, 0.3, 0.3, 0.3, 0.05);
    }

    // ════════════════════════════════════════════════════════════
    // HELPERS
    // ════════════════════════════════════════════════════════════

    private static void apply(ServerPlayer player, Holder<MobEffect> effect, int duration, int amplifier) {
        player.addEffect(new MobEffectInstance(effect, duration, amplifier, true, false, false));
    }

    private static void trail(ServerPlayer player, ServerLevel world, ParticleOptions particle) {
        if (player.tickCount % 6 != 0) return;
        var look = player.getViewVector(1.0f);
        double bx = -look.z * 0.35;
        double bz = look.x * 0.35;
        world.sendParticles(particle, player.getX() + bx, player.getY() + 0.12, player.getZ() + bz,
                1, 0, 0, 0, 0);
    }
}
