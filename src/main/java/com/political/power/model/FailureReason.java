package com.political.power.model;

/**
 * Why a cast was refused, in the order the checks run (program 10.5). The first failing check stops the
 * cast. The text shown to the player is the key {@code political.power.fail.<name in lower case>}.
 */
public enum FailureReason {
    CASTER_INVALID,
    SYSTEM_DISABLED,
    NO_TECHNIQUE_SELECTED,
    NOT_UNLOCKED,
    DOMAIN_SUPPRESSED,
    GLOBAL_COOLDOWN,
    RATE_LIMITED,
    ON_COOLDOWN,
    NO_TARGET,
    TARGET_INVALID,
    OUT_OF_RANGE,
    NO_LINE_OF_SIGHT,
    TARGET_FULL_HEALTH,
    DOMAIN_OVERLAP,
    INSUFFICIENT_RESOURCE;

    public String messageKey() {
        return "political.power.fail." + name().toLowerCase(java.util.Locale.ROOT);
    }
}
