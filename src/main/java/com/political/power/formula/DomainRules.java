package com.political.power.formula;

import com.political.power.PowerSettings;

/** Geometry and damage rules of a Domain (C9). Pure, so the boundaries are tested. */
public final class DomainRules {

    private DomainRules() {
    }

    /** Straight line distance between two points. */
    public static double distance(double ax, double ay, double az, double bx, double by, double bz) {
        double dx = ax - bx;
        double dy = ay - by;
        double dz = az - bz;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /** Whether a point is inside the Domain of a caster standing at the centre. The edge counts as inside. */
    public static boolean contains(double centerX, double centerY, double centerZ, double x, double y, double z) {
        return distance(centerX, centerY, centerZ, x, y, z) <= PowerSettings.DOMAIN_RADIUS.get();
    }

    /**
     * Whether a new Domain at A would be too close to an existing one at B: the same dimension and no
     * further apart than the minimum separation. Exactly the minimum separation is too close.
     */
    public static boolean tooClose(boolean sameDimension, double ax, double ay, double az, double bx, double by, double bz) {
        return sameDimension && distance(ax, ay, az, bx, by, bz) <= PowerSettings.DOMAIN_MIN_SEPARATION.get();
    }

    /** Damage multiplier of the caster's melee against something inside their Domain. */
    public static double meleeMultiplier(boolean targetInsideDomain) {
        return targetInsideDomain ? 1.0 + PowerSettings.DOMAIN_DAMAGE_BONUS.get() : 1.0;
    }
}
