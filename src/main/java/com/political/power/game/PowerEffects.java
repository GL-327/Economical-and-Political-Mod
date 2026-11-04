package com.political.power.game;

import com.political.core.gui.Palette;
import com.political.core.text.Text;
import com.political.power.PowerSettings;
import com.political.power.cast.EffectPlan;
import com.political.power.formula.Impulses;
import com.political.power.model.AbilitySpec;
import com.political.power.model.PowerCatalog;
import com.political.technique.Technique;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Applies a validated cast to the world. The cost and cooldown were already taken by the service; if anything here
 * throws, the service puts them back. Particles and sounds are server-spawned and reach 64 blocks (STYLE.md).
 */
final class PowerEffects {

    private PowerEffects() {
    }

    static void execute(ServerPlayer player, EffectPlan plan, GameFacts facts, long now) {
        ServerLevel world = player.level();
        AbilitySpec spec = plan.ability();
        LivingEntity target = facts.resolvedEntity();
        switch (spec.kind()) {
            case BLUE -> blue(player, world, target);
            case RED -> red(player, world, target);
            case BLAST -> blast(player, world, target);
            case REVERSE -> reverse(player, world, plan, target);
            case BARRIER -> barrier(player, world);
            case DOMAIN -> DomainManager.start(player, now);
            case LEGACY -> legacy(player, world, spec);
        }
    }

    private static void blue(ServerPlayer player, ServerLevel world, LivingEntity target) {
        Impulses.Velocity v = Impulses.blue(player.getX(), player.getY(), player.getZ(), target.getX(), target.getY(), target.getZ());
        target.setDeltaMovement(v.x(), v.y(), v.z());
        target.syncVelocity = true;
        int fx = PowerVfx.variant(world);
        PowerVfx.beam(world, ParticleTypes.SOUL_FIRE_FLAME, player.getEyePosition(), target.getBoundingBox().getCenter(), PowerVfx.pick(fx, 8, 12, 18));
        world.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.6f, PowerVfx.pick(fx, 0.5f, 0.6f, 0.75f));
    }

    private static void red(ServerPlayer player, ServerLevel world, LivingEntity target) {
        Vec3 look = player.getLookAngle();
        Impulses.Velocity v = Impulses.red(player.getX(), player.getZ(), target.getX(), target.getZ(), look.x, look.z);
        target.setDeltaMovement(v.x(), v.y(), v.z());
        target.syncVelocity = true;
        double damage = PowerSettings.RED_DAMAGE.get();
        if (damage > 0.0) {
            hurt(player, world, target, damage, PowerCatalog.RED);
        }
        int fx = PowerVfx.variant(world);
        PowerVfx.beam(world, ParticleTypes.FLAME, player.getEyePosition(), target.getBoundingBox().getCenter(), PowerVfx.pick(fx, 8, 12, 18));
        PowerVfx.particles(world, ParticleTypes.EXPLOSION, target.getX(), target.getY() + 1.0, target.getZ(), PowerVfx.pick(fx, 1, 1, 2), 0.2, 0.2, 0.2, 0.0);
        world.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.4f, PowerVfx.pick(fx, 1.4f, 1.6f, 1.8f));
    }

    private static void blast(ServerPlayer player, ServerLevel world, LivingEntity target) {
        hurt(player, world, target, PowerSettings.BLAST_DAMAGE.get(), PowerCatalog.CURSED_BLAST);
        int fx = PowerVfx.variant(world);
        PowerVfx.beam(world, ParticleTypes.WITCH, player.getEyePosition(), target.getBoundingBox().getCenter(), PowerVfx.pick(fx, 16, 24, 32));
        world.playSound(null, player.blockPosition(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 0.3f, PowerVfx.pick(fx, 1.7f, 1.9f, 2.0f));
    }

    private static void reverse(ServerPlayer player, ServerLevel world, EffectPlan plan, LivingEntity target) {
        LivingEntity patient = target != null ? target : player;
        patient.heal((float) plan.healAmount());
        int fx = PowerVfx.variant(world);
        PowerVfx.particles(world, ParticleTypes.HEART, patient.getX(), patient.getY() + 1.2, patient.getZ(), PowerVfx.pick(fx, 4, 6, 9), 0.4, 0.4, 0.4, 0.0);
        world.playSound(null, patient.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8f, PowerVfx.pick(fx, 1.2f, 1.4f, 1.6f));
    }

    private static void barrier(ServerPlayer player, ServerLevel world) {
        PowerService.raiseBarrier(player, PowerSettings.BARRIER_DURATION_TICKS.get());
        int fx = PowerVfx.variant(world);
        int points = PowerVfx.pick(fx, 12, 16, 24);
        PowerVfx.ring(world, ParticleTypes.END_ROD, player.getX(), player.getY() + 0.5, player.getZ(), 1.4, points);
        PowerVfx.ring(world, ParticleTypes.END_ROD, player.getX(), player.getY() + 1.5, player.getZ(), 1.4, points);
        world.playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.5f, PowerVfx.pick(fx, 1.6f, 1.8f, 2.0f));
    }

    private static void legacy(ServerPlayer player, ServerLevel world, AbilitySpec spec) {
        Technique technique = Technique.byName(spec.techniqueId());
        Technique.Ability ability = technique == null ? null : technique.abilityById(spec.id());
        if (ability == null) {
            throw new IllegalStateException("no older ability " + spec.id() + " in technique " + spec.techniqueId());
        }
        LegacyEffects.run(player, world, technique, ability);
    }

    /**
     * Armor-ignoring damage from a technique, scaled by the school tree and vows. It uses the plain magic damage
     * type, as the older techniques always did, so it does not run through the weapon damage chain and does not
     * change who gets kill credit.
     */
    private static void hurt(ServerPlayer player, ServerLevel world, LivingEntity target, double base, String techniqueId) {
        double amount = base * PowerService.techniqueDamageMultiplier(player.getUUID().toString(), techniqueId);
        target.hurtServer(world, player.damageSources().magic(), (float) amount);
    }

    /** What the player sees after a successful cast: the ability name and what it cost, on the action bar. */
    static void announce(ServerPlayer player, EffectPlan plan) {
        AbilitySpec spec = plan.ability();
        MutableComponent name;
        if (spec.kind() == com.political.power.model.AbilityKind.LEGACY) {
            Technique technique = Technique.byName(spec.techniqueId());
            Technique.Ability ability = technique == null ? null : technique.abilityById(spec.id());
            name = Text.plain(ability == null ? spec.id() : ability.name());
        } else {
            name = Text.trNamed("political.power.ability", spec.id());
        }
        MutableComponent bar = name.withStyle(Palette.ACCENT.bold());
        if (plan.cost() > 0) {
            bar.append(Text.plain("  -" + (int) Math.ceil(plan.cost())).withStyle(Palette.MUTED.style()));
        }
        player.sendSystemMessage(bar, true);
    }
}
