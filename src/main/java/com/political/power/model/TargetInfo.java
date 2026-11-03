package com.political.power.model;

/**
 * What target resolution found for a cast (program 10.6: the entity under the crosshair, then a block in
 * range, then the caster). All facts are plain values so the pipeline can be tested without a world.
 *
 * @param kind          what was found
 * @param distance      blocks from the caster's eyes to the target
 * @param valid         false for self where not allowed, dead, removed, protected types, PvP off, bosses not
 *                      allowed, another dimension
 * @param invalidReason short machine readable reason when {@code valid} is false, for logs
 * @param lineOfSight   whether nothing solid is between the caster and the target
 * @param fullHealth    true when the target is at maximum health (Reverse refuses those)
 * @param healthMissing health points the target lacks (0 when full)
 */
public record TargetInfo(Kind kind, double distance, boolean valid, String invalidReason,
                         boolean lineOfSight, boolean fullHealth, double healthMissing) {

    public enum Kind { NONE, ENTITY, PLAYER, BLOCK, SELF }

    public static TargetInfo none() {
        return new TargetInfo(Kind.NONE, 0.0, false, "none", false, false, 0.0);
    }

    public static TargetInfo entity(double distance, boolean lineOfSight, double healthMissing) {
        return new TargetInfo(Kind.ENTITY, distance, true, "", lineOfSight, healthMissing <= 0.0, Math.max(0.0, healthMissing));
    }

    public static TargetInfo player(double distance, boolean lineOfSight, double healthMissing) {
        return new TargetInfo(Kind.PLAYER, distance, true, "", lineOfSight, healthMissing <= 0.0, Math.max(0.0, healthMissing));
    }

    public static TargetInfo self(double healthMissing) {
        return new TargetInfo(Kind.SELF, 0.0, true, "", true, healthMissing <= 0.0, Math.max(0.0, healthMissing));
    }

    public static TargetInfo invalid(Kind kind, double distance, String reason) {
        return new TargetInfo(kind, distance, false, reason, true, false, 0.0);
    }
}
