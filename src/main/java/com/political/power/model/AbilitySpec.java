package com.political.power.model;

/**
 * One castable ability: what it costs, how long it rests and what kind of target it needs. Specs are built
 * from the settings when asked for, so a change to the config applies to the next cast.
 *
 * @param id               stable id such as {@code blue} or {@code ember_lash}
 * @param techniqueId      the technique it belongs to, such as {@code BLUE} or {@code EMBERHEART}
 * @param kind             what it does
 * @param cost             resource cost; for {@link AbilityKind#REVERSE} the cost is worked out per cast
 * @param cooldownTicks    rest after a cast, before cooldown reductions
 * @param range            how far its target may be, in blocks (0 when it needs no target)
 * @param needsTarget      whether a target must be found
 * @param allowSelf        whether the caster is a valid target when nothing else is under the crosshair
 * @param playersOnly      whether only players (and the caster) are valid targets
 * @param needsLineOfSight whether the target must be in the open
 */
public record AbilitySpec(String id, String techniqueId, AbilityKind kind, double cost, int cooldownTicks,
                          double range, boolean needsTarget, boolean allowSelf, boolean playersOnly,
                          boolean needsLineOfSight) {

    public AbilitySpec {
        if (id == null || id.isBlank() || techniqueId == null || techniqueId.isBlank()) {
            throw new IllegalArgumentException("an ability needs an id and a technique");
        }
        if (cost < 0 || cooldownTicks < 0 || range < 0) {
            throw new IllegalArgumentException("cost, cooldown and range cannot be negative: " + id);
        }
    }

    /** An ability that needs no target: a self buff, an area burst around the caster, a barrier. */
    public static AbilitySpec selfOnly(String id, String techniqueId, AbilityKind kind, double cost, int cooldownTicks) {
        return new AbilitySpec(id, techniqueId, kind, cost, cooldownTicks, 0.0, false, false, false, false);
    }

    /** An ability aimed at the entity under the crosshair. */
    public static AbilitySpec aimed(String id, String techniqueId, AbilityKind kind, double cost, int cooldownTicks,
                                    double range, boolean needsLineOfSight) {
        return new AbilitySpec(id, techniqueId, kind, cost, cooldownTicks, range, true, false, false, needsLineOfSight);
    }
}
