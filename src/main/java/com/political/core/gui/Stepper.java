package com.political.core.gui;

/**
 * The click rule of a number stepper: left click adds the step, right click subtracts it, and holding shift
 * multiplies either by ten. That covers almost every number an operator wants to change without typing.
 */
public final class Stepper {

    public static final long SHIFT_FACTOR = 10L;

    private Stepper() {
    }

    public static long delta(long step, boolean rightClick, boolean shift) {
        long amount = shift ? Math.multiplyExact(step, SHIFT_FACTOR) : step;
        return rightClick ? -amount : amount;
    }

    /** Applies a delta and keeps the result inside min .. max. */
    public static long clamped(long current, long delta, long min, long max) {
        long next = current + delta;
        return Math.max(min, Math.min(max, next));
    }
}
