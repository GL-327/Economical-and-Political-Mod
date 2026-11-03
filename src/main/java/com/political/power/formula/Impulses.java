package com.political.power.formula;

import com.political.power.PowerSettings;

/**
 * The velocity Blue (pull) and Red (push) give their target, as pure vector arithmetic. The game applies
 * the result with {@code setDeltaMovement} and a velocity sync.
 */
public final class Impulses {

    /** A velocity in blocks per tick. */
    public record Velocity(double x, double y, double z) {
        public double horizontalSpeed() {
            return Math.hypot(x, z);
        }
    }

    private Impulses() {
    }

    /**
     * Blue: the target is pulled horizontally toward the caster at the configured speed (blocks per tick).
     * The vertical part follows the height difference gently and is clamped to the configured range, so a
     * target far above or below is not flung. A target exactly above or below the caster gets no
     * horizontal pull.
     */
    public static Velocity blue(double casterX, double casterY, double casterZ, double targetX, double targetY, double targetZ) {
        double dx = casterX - targetX;
        double dz = casterZ - targetZ;
        double length = Math.hypot(dx, dz);
        double pull = PowerSettings.BLUE_PULL.get();
        double vx = length < 1.0e-6 ? 0.0 : dx / length * pull;
        double vz = length < 1.0e-6 ? 0.0 : dz / length * pull;
        double vy = clamp((casterY - targetY) * 0.1, PowerSettings.BLUE_VERTICAL_MIN.get(), PowerSettings.BLUE_VERTICAL_MAX.get());
        return new Velocity(vx, vy, vz);
    }

    /**
     * Red: the target is pushed horizontally away from the caster at the configured speed, with the
     * configured lift. When the target is exactly above or below the caster the push goes along the
     * caster's look direction ({@code lookX}, {@code lookZ}) so it is never zero.
     */
    public static Velocity red(double casterX, double casterZ, double targetX, double targetZ, double lookX, double lookZ) {
        double dx = targetX - casterX;
        double dz = targetZ - casterZ;
        double length = Math.hypot(dx, dz);
        if (length < 1.0e-6) {
            dx = lookX;
            dz = lookZ;
            length = Math.hypot(dx, dz);
        }
        double push = PowerSettings.RED_PUSH.get();
        double vx = length < 1.0e-6 ? 0.0 : dx / length * push;
        double vz = length < 1.0e-6 ? 0.0 : dz / length * push;
        return new Velocity(vx, PowerSettings.RED_LIFT.get(), vz);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
