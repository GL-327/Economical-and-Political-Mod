package com.political.power.cast;

import com.political.power.PowerSettings;
import com.political.power.formula.ReverseFormula;
import com.political.power.model.AbilityKind;
import com.political.power.model.AbilitySpec;
import com.political.power.model.FailureReason;
import com.political.power.model.TargetInfo;

/**
 * The validation pipeline (program 10.5). The checks run in a fixed order and the first one that fails
 * stops the cast. When every check passes the result is an {@link EffectPlan} computed as data; nothing
 * has been changed yet. This class has no knowledge of Minecraft, so every rule is covered by unit tests.
 *
 * <ol>
 *   <li>CASTER_INVALID: a player, alive, not a spectator</li>
 *   <li>SYSTEM_DISABLED</li>
 *   <li>NO_TECHNIQUE_SELECTED</li>
 *   <li>NOT_UNLOCKED</li>
 *   <li>DOMAIN_SUPPRESSED</li>
 *   <li>GLOBAL_COOLDOWN</li>
 *   <li>RATE_LIMITED</li>
 *   <li>ON_COOLDOWN</li>
 *   <li>NO_TARGET</li>
 *   <li>TARGET_INVALID</li>
 *   <li>OUT_OF_RANGE</li>
 *   <li>NO_LINE_OF_SIGHT</li>
 *   <li>TARGET_FULL_HEALTH (Reverse only)</li>
 *   <li>DOMAIN_OVERLAP (Domain only)</li>
 *   <li>INSUFFICIENT_RESOURCE</li>
 * </ol>
 */
public final class CastPipeline {

    private CastPipeline() {
    }

    public static CastDecision validate(CastFacts facts) {
        if (!facts.casterValid()) {
            return reject(FailureReason.CASTER_INVALID);
        }
        if (!facts.systemEnabled()) {
            return reject(FailureReason.SYSTEM_DISABLED);
        }
        AbilitySpec ability = facts.selected();
        if (ability == null) {
            return reject(FailureReason.NO_TECHNIQUE_SELECTED);
        }
        if (!facts.unlocked(ability)) {
            return reject(FailureReason.NOT_UNLOCKED);
        }
        if (facts.suppressedByForeignDomain()) {
            return reject(FailureReason.DOMAIN_SUPPRESSED);
        }
        if (facts.ticksSinceLastCast() < PowerSettings.GLOBAL_COOLDOWN_TICKS.get()) {
            return reject(FailureReason.GLOBAL_COOLDOWN);
        }
        if (facts.castsWithin(PowerSettings.RATE_LIMIT_WINDOW_TICKS.get()) >= PowerSettings.RATE_LIMIT_CASTS.get()) {
            return reject(FailureReason.RATE_LIMITED);
        }
        long remaining = facts.cooldownRemainingTicks(ability);
        if (remaining > 0) {
            return new CastDecision.Rejected(FailureReason.ON_COOLDOWN, remaining, "");
        }

        TargetInfo target = null;
        double healAmount = 0.0;
        if (ability.needsTarget()) {
            target = facts.target(ability);
            if (target == null || target.kind() == TargetInfo.Kind.NONE || target.kind() == TargetInfo.Kind.BLOCK) {
                return reject(FailureReason.NO_TARGET);
            }
            boolean isSelf = target.kind() == TargetInfo.Kind.SELF;
            boolean isPlayer = target.kind() == TargetInfo.Kind.PLAYER || isSelf;
            if (!target.valid() || (isSelf && !ability.allowSelf()) || (ability.playersOnly() && !isPlayer)) {
                String note = !target.valid() ? target.invalidReason()
                        : isSelf && !ability.allowSelf() ? "self not allowed" : "players only";
                return new CastDecision.Rejected(FailureReason.TARGET_INVALID, 0, note);
            }
            if (target.distance() > ability.range()) {
                return reject(FailureReason.OUT_OF_RANGE);
            }
            if (ability.needsLineOfSight() && !target.lineOfSight()) {
                return reject(FailureReason.NO_LINE_OF_SIGHT);
            }
            if (ability.kind() == AbilityKind.REVERSE) {
                if (target.fullHealth()) {
                    return reject(FailureReason.TARGET_FULL_HEALTH);
                }
                healAmount = ReverseFormula.healAmount(target.healthMissing());
            }
        }
        if (ability.kind() == AbilityKind.DOMAIN && facts.domainOverlap()) {
            return reject(FailureReason.DOMAIN_OVERLAP);
        }

        double cost = ability.kind() == AbilityKind.REVERSE ? ReverseFormula.cost(healAmount) : ability.cost();
        if (facts.resource() < cost) {
            return new CastDecision.Rejected(FailureReason.INSUFFICIENT_RESOURCE, (long) Math.ceil(cost), "");
        }

        double reduction = Math.max(0.0, Math.min(0.9, facts.cooldownReduction()));
        int cooldown = (int) Math.max(0, Math.round(ability.cooldownTicks() * (1.0 - reduction)));
        return new CastDecision.Approved(new EffectPlan(ability, cost, cooldown, target, healAmount));
    }

    private static CastDecision reject(FailureReason reason) {
        return new CastDecision.Rejected(reason, 0, "");
    }
}
