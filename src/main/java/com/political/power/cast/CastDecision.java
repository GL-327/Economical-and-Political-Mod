package com.political.power.cast;

import com.political.power.model.FailureReason;

/** The pipeline's answer: the cast may go ahead with a plan, or it is refused for one reason. */
public sealed interface CastDecision {

    /** The cast passed every check. */
    record Approved(EffectPlan plan) implements CastDecision {
    }

    /**
     * The cast was refused.
     *
     * @param reason the first check that failed
     * @param detail a number worth showing, such as cooldown seconds left, or 0
     * @param note   short extra text for logs, such as why a target was invalid
     */
    record Rejected(FailureReason reason, long detail, String note) implements CastDecision {
    }
}
