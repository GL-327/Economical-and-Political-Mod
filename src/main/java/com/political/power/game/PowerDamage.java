package com.political.power.game;

import com.political.core.Clock;
import com.political.core.text.Text;
import com.political.power.formula.BlackFlash;
import com.political.power.formula.DomainRules;
import com.political.stats.PlayerStatsManager;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

/**
 * The power system's two hooks in the damage path, both called from {@code BountyDamageMixin}:
 * <ul>
 *   <li>incoming: the Barrier ({@link #absorbs});</li>
 *   <li>outgoing: Black Flash and the Domain, as the last stage after the weapon, stat and slayer multipliers
 *       ({@link #outgoing}). M5 turns the whole chain into an explicit list of stages; until then this is one call.</li>
 * </ul>
 */
public final class PowerDamage {

    /** True while a Domain strike is dealing its damage, so that strike does not run through the chain again. */
    private static boolean inDomainStrike;

    private PowerDamage() {
    }

    /** A hit made by the Domain itself: leave its damage exactly as computed (no Defense, no other multipliers). */
    public static boolean inDomainStrike() {
        return inDomainStrike;
    }

    /** Incoming: whether the victim's Barrier takes this hit completely. */
    public static boolean absorbs(ServerPlayer victim, DamageSource source) {
        return PowerService.absorbsDamage(victim, source);
    }

    /**
     * Outgoing, last stage. Only the attacker's own melee counts (not projectiles, not magic, not a Ferocity strike).
     * Black Flash replaces the crit multiplier. Inside the attacker's own Domain the hit gets the Domain bonus and
     * ignores armor and the Defense stat: it is dealt as plain magic damage and the original hit becomes zero.
     */
    public static float outgoing(ServerPlayer attacker, LivingEntity target, DamageSource source, float amount) {
        double critMultiplier = PlayerStatsManager.takeLastCritMultiplier();
        if (inDomainStrike || !isOwnMelee(attacker, source) || target == attacker) {
            return amount;
        }
        String uuid = attacker.getUUID().toString();
        float result = amount;

        BlackFlash.Result flash = BlackFlash.apply(result, critMultiplier, PowerService.tier(uuid),
                PowerService.ticksSinceLastCast(attacker), attacker.getRandom().nextDouble());
        if (flash.triggered()) {
            result = (float) flash.damage();
            attacker.sendSystemMessage(Text.tr("political.power.black_flash"), true);
            if (attacker.level() instanceof ServerLevel world) {
                PowerVfx.particles(world, ParticleTypes.CRIT, target.getX(), target.getY() + target.getBbHeight() * 0.6, target.getZ(), 16, 0.3, 0.3, 0.3, 0.3);
                PowerVfx.particles(world, ParticleTypes.SOUL_FIRE_FLAME, target.getX(), target.getY() + target.getBbHeight() * 0.6, target.getZ(), 10, 0.3, 0.3, 0.3, 0.05);
                world.playSound(null, target.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.4f, 2.0f);
            }
        }

        if (DomainManager.inDomainOf(attacker.getUUID(), target, Clock.ticks()) && target.level() instanceof ServerLevel world) {
            result *= (float) DomainRules.meleeMultiplier(true);
            inDomainStrike = true;
            try {
                target.hurtServer(world, attacker.damageSources().magic(), result);
            } finally {
                inDomainStrike = false;
            }
            return 0.0f;
        }
        return result;
    }

    private static boolean isOwnMelee(ServerPlayer attacker, DamageSource source) {
        return source.getDirectEntity() == attacker && !source.is(DamageTypeTags.IS_PROJECTILE);
    }
}
