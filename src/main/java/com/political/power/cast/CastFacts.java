package com.political.power.cast;

import com.political.power.model.AbilitySpec;
import com.political.power.model.TargetInfo;

/**
 * Everything the validation pipeline needs to know about the world and the caster, as questions. The game
 * implements it from a real player; tests implement it with plain values. Methods that cost real work
 * (target resolution) are only asked when an earlier check has not already refused the cast.
 */
public interface CastFacts {

    /** The caster is a real player, alive and not a spectator. */
    boolean casterValid();

    /** The power system is switched on. */
    boolean systemEnabled();

    /** The ability the caster has selected, or null when none is selected. */
    AbilitySpec selected();

    /** The caster has unlocked the selected ability (learned, granted or drawn from the school tree, and ranked up). */
    boolean unlocked(AbilitySpec ability);

    /** The caster stands inside a Domain that is not their own, which keeps them from casting. */
    boolean suppressedByForeignDomain();

    /** Ticks since the caster's last cast, or {@link Long#MAX_VALUE} if they never cast. */
    long ticksSinceLastCast();

    /** How many casts the caster made in the last {@code windowTicks} ticks. */
    int castsWithin(int windowTicks);

    /** Ticks left on this ability's cooldown, 0 when it is ready. */
    long cooldownRemainingTicks(AbilitySpec ability);

    /** What target resolution found for this ability, or null when nothing was found at all. */
    TargetInfo target(AbilitySpec ability);

    /** Another Domain in the same dimension is closer than the minimum separation. */
    boolean domainOverlap();

    /** The caster's current amount of the cursed resource. */
    double resource();

    /** Share of every cooldown removed by vows and school bonuses, 0 to 0.9. */
    double cooldownReduction();
}
