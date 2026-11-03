package com.political.power.cast;

import com.political.power.model.AbilitySpec;
import com.political.power.model.TargetInfo;

/**
 * What a validated cast will do, worked out as data before anything changes: the ability, what it costs,
 * how long it then rests, the target it resolved, and for Reverse how much it heals. The service deducts
 * the cost and sets the cooldown from this plan, runs it, and puts both back if running it throws.
 *
 * @param ability       the ability being cast
 * @param cost          resource to deduct
 * @param cooldownTicks cooldown to set after reductions
 * @param target        the resolved target, or null for abilities that need none
 * @param healAmount    health Reverse will restore, 0 for other abilities
 */
public record EffectPlan(AbilitySpec ability, double cost, int cooldownTicks, TargetInfo target, double healAmount) {
}
