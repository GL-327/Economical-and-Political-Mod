package com.political.core.damage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.political.core.damage.DamagePipeline.Entry;
import com.political.core.damage.DamagePipeline.Outcome;
import com.political.core.damage.DamagePipeline.Result;
import com.political.core.damage.DamagePipeline.Step;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class DamagePipelineTest {

    private static Step times(String name, float factor) {
        return Step.of(name, amount -> Result.next(amount * factor));
    }

    @Test
    void noStagesLeavesTheDamageAlone() {
        Outcome outcome = DamagePipeline.run(7.5f, List.of());
        assertEquals(7.5f, outcome.amount());
        assertTrue(outcome.trace().isEmpty());
    }

    @Test
    void stagesRunInTheOrderGivenAndEachSeesTheLastResult() {
        List<String> seen = new ArrayList<>();
        List<Step> steps = List.of(
                Step.of("add", a -> {
                    seen.add("add:" + a);
                    return Result.next(a + 2);
                }),
                Step.of("double", a -> {
                    seen.add("double:" + a);
                    return Result.next(a * 2);
                }));
        assertEquals(24.0f, DamagePipeline.amount(10.0f, steps));
        assertEquals(List.of("add:10.0", "double:12.0"), seen);
        // the order changes the result: the same two rules the other way round
        List<Step> reversed = List.of(steps.get(1), steps.get(0));
        assertEquals(22.0f, DamagePipeline.amount(10.0f, reversed));
    }

    @Test
    void aStageThatIsDoneEndsTheCalculationAndLaterStagesDoNotRun() {
        boolean[] ran = {false};
        List<Step> steps = List.of(
                times("first", 2.0f),
                Step.of("stop", a -> Result.done(a + 1)),
                Step.of("never", a -> {
                    ran[0] = true;
                    return Result.next(a * 100);
                }));
        Outcome outcome = DamagePipeline.run(5.0f, steps);
        assertEquals(11.0f, outcome.amount());
        assertFalse(ran[0]);
        assertEquals(2, outcome.trace().size());
        assertTrue(outcome.trace().get(1).last());
    }

    @Test
    void anAbsorbedHitIsDoneWithZero() {
        List<Step> steps = List.of(Step.of("absorb", a -> Result.done(0.0f)), times("defence", 0.5f));
        assertEquals(0.0f, DamagePipeline.amount(30.0f, steps));
    }

    @Test
    void theTraceNamesEveryStageWithWhatItReceivedAndReturned() {
        Outcome outcome = DamagePipeline.run(10.0f, List.of(times("strength", 1.5f), times("crit", 2.0f)));
        assertEquals(List.of(
                new Entry("strength", 10.0f, 15.0f, false),
                new Entry("crit", 15.0f, 30.0f, false)), outcome.trace());
    }

    @Test
    void aStageCannotProduceNegativeOrNotANumberDamage() {
        assertEquals(0.0f, DamagePipeline.amount(10.0f, List.of(times("broken", -1.0f))));
        assertEquals(0.0f, DamagePipeline.amount(10.0f, List.of(times("broken", Float.NaN))));
        Outcome outcome = DamagePipeline.run(10.0f, List.of(times("broken", Float.NaN), times("after", 3.0f)));
        assertEquals(0.0f, outcome.amount());
        assertEquals(0.0f, outcome.trace().get(0).after());
    }

    @Test
    void infiniteDamageIsKeptAsItIsSoTheCallerCanDecide() {
        assertEquals(Float.POSITIVE_INFINITY, DamagePipeline.amount(1.0f, List.of(times("huge", Float.POSITIVE_INFINITY))));
    }

    @Test
    void theSameStagesGiveTheSameAnswerEveryTime() {
        List<Step> steps = List.of(times("a", 1.25f), times("b", 0.8f), times("c", 3.0f));
        float first = DamagePipeline.amount(8.0f, steps);
        for (int i = 0; i < 100; i++) {
            assertEquals(first, DamagePipeline.amount(8.0f, steps));
        }
    }
}
