package com.political.power;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.political.core.config.Config;
import com.political.power.cast.CastDecision;
import com.political.power.cast.CastFacts;
import com.political.power.cast.CastPipeline;
import com.political.power.cast.EffectPlan;
import com.political.power.model.AbilityKind;
import com.political.power.model.AbilitySpec;
import com.political.power.model.FailureReason;
import com.political.power.model.TargetInfo;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/** The 15 checks of the validation pipeline, one by one, in order, and at their boundaries. */
class CastPipelineTest {

    /** A caster for whom everything passes, unless a test changes one thing. */
    private static final class Facts implements CastFacts {
        boolean casterValid = true;
        boolean enabled = true;
        AbilitySpec selected = AbilitySpec.aimed("blast", "CURSED_BLAST", AbilityKind.BLAST, 25, 40, 24.0, true);
        boolean unlocked = true;
        boolean foreignDomain = false;
        long sinceLastCast = Long.MAX_VALUE;
        int castsInWindow = 0;
        long cooldownRemaining = 0;
        TargetInfo target = TargetInfo.entity(5.0, true, 0.0);
        boolean domainOverlap = false;
        double resource = 100.0;
        double cooldownReduction = 0.0;
        int targetAsked = 0;

        @Override public boolean casterValid() { return casterValid; }
        @Override public boolean systemEnabled() { return enabled; }
        @Override public AbilitySpec selected() { return selected; }
        @Override public boolean unlocked(AbilitySpec ability) { return unlocked; }
        @Override public boolean suppressedByForeignDomain() { return foreignDomain; }
        @Override public long ticksSinceLastCast() { return sinceLastCast; }
        @Override public int castsWithin(int windowTicks) { return castsInWindow; }
        @Override public long cooldownRemainingTicks(AbilitySpec ability) { return cooldownRemaining; }
        @Override public TargetInfo target(AbilitySpec ability) { targetAsked++; return target; }
        @Override public boolean domainOverlap() { return domainOverlap; }
        @Override public double resource() { return resource; }
        @Override public double cooldownReduction() { return cooldownReduction; }
    }

    @AfterEach
    void restore() {
        Config.reset();
    }

    private static FailureReason refusedWith(CastFacts facts) {
        CastDecision d = CastPipeline.validate(facts);
        return assertInstanceOf(CastDecision.Rejected.class, d, "expected a refusal, got " + d).reason();
    }

    private static EffectPlan approved(CastFacts facts) {
        CastDecision d = CastPipeline.validate(facts);
        return assertInstanceOf(CastDecision.Approved.class, d, "expected approval, got " + d).plan();
    }

    // ── each reason on its own ──────────────────────────────────────────────────────────────

    @Test
    void aCleanCastIsApprovedWithItsPlan() {
        EffectPlan plan = approved(new Facts());
        assertEquals(25.0, plan.cost());
        assertEquals(40, plan.cooldownTicks());
        assertEquals("blast", plan.ability().id());
        assertEquals(TargetInfo.Kind.ENTITY, plan.target().kind());
        assertEquals(0.0, plan.healAmount());
    }

    @Test
    void casterInvalid() {
        Facts f = new Facts();
        f.casterValid = false;
        assertEquals(FailureReason.CASTER_INVALID, refusedWith(f));
    }

    @Test
    void systemDisabled() {
        Facts f = new Facts();
        f.enabled = false;
        assertEquals(FailureReason.SYSTEM_DISABLED, refusedWith(f));
    }

    @Test
    void noTechniqueSelected() {
        Facts f = new Facts();
        f.selected = null;
        assertEquals(FailureReason.NO_TECHNIQUE_SELECTED, refusedWith(f));
    }

    @Test
    void notUnlocked() {
        Facts f = new Facts();
        f.unlocked = false;
        assertEquals(FailureReason.NOT_UNLOCKED, refusedWith(f));
    }

    @Test
    void domainSuppressed() {
        Facts f = new Facts();
        f.foreignDomain = true;
        assertEquals(FailureReason.DOMAIN_SUPPRESSED, refusedWith(f));
    }

    @Test
    void globalCooldownBoundary() {
        Facts f = new Facts();
        f.sinceLastCast = 9;
        assertEquals(FailureReason.GLOBAL_COOLDOWN, refusedWith(f));
        f.sinceLastCast = 10;
        approved(f);
        f.sinceLastCast = 0;
        assertEquals(FailureReason.GLOBAL_COOLDOWN, refusedWith(f));
    }

    @Test
    void rateLimitBoundary() {
        Facts f = new Facts();
        f.castsInWindow = 7;
        approved(f);
        f.castsInWindow = 8;
        assertEquals(FailureReason.RATE_LIMITED, refusedWith(f));
        f.castsInWindow = 20;
        assertEquals(FailureReason.RATE_LIMITED, refusedWith(f));
    }

    @Test
    void onCooldownCarriesTheTicksLeft() {
        Facts f = new Facts();
        f.cooldownRemaining = 1;
        CastDecision.Rejected r = assertInstanceOf(CastDecision.Rejected.class, CastPipeline.validate(f));
        assertEquals(FailureReason.ON_COOLDOWN, r.reason());
        assertEquals(1, r.detail());
        f.cooldownRemaining = 0;
        approved(f);
    }

    @Test
    void noTarget() {
        Facts f = new Facts();
        f.target = null;
        assertEquals(FailureReason.NO_TARGET, refusedWith(f));
        f.target = TargetInfo.none();
        assertEquals(FailureReason.NO_TARGET, refusedWith(f));
        f.target = new TargetInfo(TargetInfo.Kind.BLOCK, 3.0, true, "", true, false, 0.0);
        assertEquals(FailureReason.NO_TARGET, refusedWith(f), "a block is not a target for a ray that wants a living thing");
    }

    @Test
    void targetInvalid() {
        Facts f = new Facts();
        f.target = TargetInfo.invalid(TargetInfo.Kind.ENTITY, 4.0, "dead");
        CastDecision.Rejected r = assertInstanceOf(CastDecision.Rejected.class, CastPipeline.validate(f));
        assertEquals(FailureReason.TARGET_INVALID, r.reason());
        assertEquals("dead", r.note());
        f.target = TargetInfo.self(0.0);
        assertEquals(FailureReason.TARGET_INVALID, refusedWith(f), "self where the ability does not allow it");
    }

    @Test
    void outOfRangeBoundary() {
        Facts f = new Facts();
        f.target = TargetInfo.entity(24.0, true, 0.0);
        approved(f);
        f.target = TargetInfo.entity(24.01, true, 0.0);
        assertEquals(FailureReason.OUT_OF_RANGE, refusedWith(f));
    }

    @Test
    void noLineOfSight() {
        Facts f = new Facts();
        f.target = TargetInfo.entity(5.0, false, 0.0);
        assertEquals(FailureReason.NO_LINE_OF_SIGHT, refusedWith(f));
        f.selected = AbilitySpec.aimed("pull", "X", AbilityKind.LEGACY, 5, 5, 20.0, false);
        approved(f);
    }

    @Test
    void domainOverlapOnlyStopsADomain() {
        Facts f = new Facts();
        f.domainOverlap = true;
        approved(f);
        f.selected = AbilitySpec.selfOnly("domain", "DOMAIN", AbilityKind.DOMAIN, 150, 2400);
        assertEquals(FailureReason.DOMAIN_OVERLAP, refusedWith(f));
        f.domainOverlap = false;
        f.resource = 150;
        approved(f);
    }

    @Test
    void insufficientResourceBoundary() {
        Facts f = new Facts();
        f.resource = 25.0;
        approved(f);
        f.resource = 24.99;
        CastDecision.Rejected r = assertInstanceOf(CastDecision.Rejected.class, CastPipeline.validate(f));
        assertEquals(FailureReason.INSUFFICIENT_RESOURCE, r.reason());
        assertEquals(25, r.detail());
    }

    // ── Reverse ─────────────────────────────────────────────────────────────────────────────

    private static AbilitySpec reverse() {
        return new AbilitySpec("reverse", "REVERSE", AbilityKind.REVERSE, 0, 200, 6.0, true, true, true, false);
    }

    @Test
    void reverseHealsWhatIsMissingUpToTheCapAndChargesTwoPerPoint() {
        Facts f = new Facts();
        f.selected = reverse();
        f.target = TargetInfo.self(5.0);
        EffectPlan small = approved(f);
        assertEquals(5.0, small.healAmount());
        assertEquals(10.0, small.cost());

        f.target = TargetInfo.self(20.0);
        EffectPlan capped = approved(f);
        assertEquals(8.0, capped.healAmount());
        assertEquals(16.0, capped.cost());
        assertEquals(200, capped.cooldownTicks());
    }

    @Test
    void reverseRefusesAFullHealthTarget() {
        Facts f = new Facts();
        f.selected = reverse();
        f.target = TargetInfo.self(0.0);
        assertEquals(FailureReason.TARGET_FULL_HEALTH, refusedWith(f));
        f.target = TargetInfo.player(3.0, true, 0.0);
        assertEquals(FailureReason.TARGET_FULL_HEALTH, refusedWith(f));
    }

    @Test
    void reverseRefusesDeadTargetsAndNonPlayers() {
        Facts f = new Facts();
        f.selected = reverse();
        f.target = TargetInfo.invalid(TargetInfo.Kind.PLAYER, 3.0, "dead");
        assertEquals(FailureReason.TARGET_INVALID, refusedWith(f));
        f.target = TargetInfo.entity(3.0, true, 4.0);
        assertEquals(FailureReason.TARGET_INVALID, refusedWith(f), "only players and the caster can be healed");
    }

    @Test
    void reverseNeedsTheResourceForWhatItWouldHeal() {
        Facts f = new Facts();
        f.selected = reverse();
        f.target = TargetInfo.self(8.0);
        f.resource = 15.9;
        assertEquals(FailureReason.INSUFFICIENT_RESOURCE, refusedWith(f));
        f.resource = 16.0;
        approved(f);
    }

    @Test
    void reverseRangeIsTheSixBlockRange() {
        Facts f = new Facts();
        f.selected = reverse();
        f.target = TargetInfo.player(6.0, true, 3.0);
        approved(f);
        f.target = TargetInfo.player(6.5, true, 3.0);
        assertEquals(FailureReason.OUT_OF_RANGE, refusedWith(f));
    }

    // ── cooldown reductions and targets that are not asked for ──────────────────────────────

    @Test
    void cooldownReductionShortensTheCooldownAndIsCapped() {
        Facts f = new Facts();
        f.selected = AbilitySpec.aimed("blue", "BLUE", AbilityKind.BLUE, 15, 80, 10.0, true);
        f.cooldownReduction = 0.10;
        assertEquals(72, approved(f).cooldownTicks());
        f.cooldownReduction = 5.0;
        assertEquals(8, approved(f).cooldownTicks(), "reductions stop at 90 percent");
        f.cooldownReduction = -1.0;
        assertEquals(80, approved(f).cooldownTicks(), "a negative reduction is ignored");
    }

    @Test
    void anAbilityWithoutATargetNeverAsksForOne() {
        Facts f = new Facts();
        f.selected = AbilitySpec.selfOnly("barrier", "BARRIER", AbilityKind.BARRIER, 30, 300);
        f.target = null;
        EffectPlan plan = approved(f);
        assertEquals(0, f.targetAsked);
        assertEquals(null, plan.target());
    }

    // ── order ───────────────────────────────────────────────────────────────────────────────

    @Test
    void checksRunInTheDocumentedOrder() {
        // A caster who fails every check. Fixing the failures one at a time, in the documented order,
        // must reveal the next reason each time, ending in approval.
        Facts f = new Facts();
        f.casterValid = false;
        f.enabled = false;
        f.selected = reverse();
        f.unlocked = false;
        f.foreignDomain = true;
        f.sinceLastCast = 0;
        f.castsInWindow = 8;
        f.cooldownRemaining = 5;
        f.target = null;
        f.resource = 0.0;

        assertEquals(FailureReason.CASTER_INVALID, refusedWith(f));
        f.casterValid = true;
        assertEquals(FailureReason.SYSTEM_DISABLED, refusedWith(f));
        f.enabled = true;
        f.selected = null;
        assertEquals(FailureReason.NO_TECHNIQUE_SELECTED, refusedWith(f));
        f.selected = reverse();
        assertEquals(FailureReason.NOT_UNLOCKED, refusedWith(f));
        f.unlocked = true;
        assertEquals(FailureReason.DOMAIN_SUPPRESSED, refusedWith(f));
        f.foreignDomain = false;
        assertEquals(FailureReason.GLOBAL_COOLDOWN, refusedWith(f));
        f.sinceLastCast = 10;
        assertEquals(FailureReason.RATE_LIMITED, refusedWith(f));
        f.castsInWindow = 0;
        assertEquals(FailureReason.ON_COOLDOWN, refusedWith(f));
        f.cooldownRemaining = 0;
        assertEquals(FailureReason.NO_TARGET, refusedWith(f));
        f.target = TargetInfo.invalid(TargetInfo.Kind.PLAYER, 40.0, "other dimension");
        assertEquals(FailureReason.TARGET_INVALID, refusedWith(f));
        f.target = TargetInfo.player(40.0, false, 0.0);
        assertEquals(FailureReason.OUT_OF_RANGE, refusedWith(f));
        f.selected = new AbilitySpec("reverse", "REVERSE", AbilityKind.REVERSE, 0, 200, 60.0, true, true, true, true);
        assertEquals(FailureReason.NO_LINE_OF_SIGHT, refusedWith(f));
        f.target = TargetInfo.player(40.0, true, 0.0);
        assertEquals(FailureReason.TARGET_FULL_HEALTH, refusedWith(f));
        f.target = TargetInfo.player(40.0, true, 8.0);
        assertEquals(FailureReason.INSUFFICIENT_RESOURCE, refusedWith(f));
        f.resource = 100.0;
        approved(f);
    }

    @Test
    void domainOverlapComesAfterTheTargetChecksAndBeforeTheResource() {
        Facts f = new Facts();
        f.selected = AbilitySpec.selfOnly("domain", "DOMAIN", AbilityKind.DOMAIN, 150, 2400);
        f.domainOverlap = true;
        f.resource = 0;
        assertEquals(FailureReason.DOMAIN_OVERLAP, refusedWith(f));
        f.domainOverlap = false;
        assertEquals(FailureReason.INSUFFICIENT_RESOURCE, refusedWith(f));
    }

    // ── the reasons themselves ──────────────────────────────────────────────────────────────

    @Test
    void theFifteenReasonsAreInTheProgramOrder() {
        List<String> expected = List.of(
                "CASTER_INVALID", "SYSTEM_DISABLED", "NO_TECHNIQUE_SELECTED", "NOT_UNLOCKED", "DOMAIN_SUPPRESSED",
                "GLOBAL_COOLDOWN", "RATE_LIMITED", "ON_COOLDOWN", "NO_TARGET", "TARGET_INVALID", "OUT_OF_RANGE",
                "NO_LINE_OF_SIGHT", "TARGET_FULL_HEALTH", "DOMAIN_OVERLAP", "INSUFFICIENT_RESOURCE");
        assertEquals(expected, java.util.Arrays.stream(FailureReason.values()).map(Enum::name).toList());
        assertEquals("political.power.fail.on_cooldown", FailureReason.ON_COOLDOWN.messageKey());
        assertFalse(FailureReason.values().length != 15);
        assertTrue(true);
    }
}
