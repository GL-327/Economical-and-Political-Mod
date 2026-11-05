package com.political.core.damage;

import java.util.ArrayList;
import java.util.List;

/**
 * An ordered list of named damage stages (M5). The damage hook used to be one long method that multiplied a number
 * through twenty rules, some of which stopped the rest; this runs the same rules as a list so the order is visible,
 * a stage that ends the calculation says so, and a test can pin the order and the arithmetic.
 *
 * <p>A stage receives the damage so far and returns the damage after it. {@link Result#next} continues with the next
 * stage; {@link Result#done} ends the calculation with that amount (an absorbed hit is {@code done(0)}). The final
 * amount is never negative and never NaN: a stage that produces one is replaced by zero and the trace says so.</p>
 */
public final class DamagePipeline {

    private DamagePipeline() {
    }

    /** What a stage returns: the damage after it, and whether the calculation ends here. */
    public record Result(float amount, boolean last) {
        public static Result next(float amount) {
            return new Result(amount, false);
        }

        public static Result done(float amount) {
            return new Result(amount, true);
        }
    }

    @FunctionalInterface
    public interface Stage {
        Result apply(float amount);
    }

    /** A stage with a name for the trace. */
    public record Step(String name, Stage stage) {
        public static Step of(String name, Stage stage) {
            return new Step(name, stage);
        }
    }

    /** One line of the trace: the stage, the damage it received and the damage it returned. */
    public record Entry(String stage, float before, float after, boolean last) {
    }

    /** The final damage and how it was reached. */
    public record Outcome(float amount, List<Entry> trace) {
    }

    /** Runs the stages in order from {@code start}. */
    public static Outcome run(float start, List<Step> steps) {
        List<Entry> trace = new ArrayList<>(steps.size());
        float amount = start;
        for (Step step : steps) {
            Result result = step.stage().apply(amount);
            float after = result.amount();
            if (Float.isNaN(after) || after < 0.0f) {
                after = 0.0f;
            }
            trace.add(new Entry(step.name(), amount, after, result.last()));
            amount = after;
            if (result.last()) {
                break;
            }
        }
        return new Outcome(amount, trace);
    }

    /** The final damage only. */
    public static float amount(float start, List<Step> steps) {
        return run(start, steps).amount();
    }
}
