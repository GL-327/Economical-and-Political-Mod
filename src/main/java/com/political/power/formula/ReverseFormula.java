package com.political.power.formula;

import com.political.power.PowerSettings;

/** Reverse (C6): how much it heals and what that costs. */
public final class ReverseFormula {

    private ReverseFormula() {
    }

    /** Health restored: what the target lacks, up to the configured cap per cast. Never negative. */
    public static double healAmount(double healthMissing) {
        return Math.max(0.0, Math.min(PowerSettings.REVERSE_MAX_HEAL.get(), healthMissing));
    }

    /** Resource charged for a heal: the configured charge for every point actually restored. */
    public static double cost(double healed) {
        return Math.max(0.0, healed) * PowerSettings.REVERSE_COST_PER_HP.get();
    }
}
