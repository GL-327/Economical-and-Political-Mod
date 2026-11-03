package com.political.power;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.political.core.config.Config;
import com.political.power.formula.BarrierRules;
import com.political.power.formula.BlackFlash;
import com.political.power.formula.DomainRules;
import com.political.power.formula.DomainState;
import com.political.power.formula.Impulses;
import com.political.power.formula.ResourcePool;
import com.political.power.formula.ReverseFormula;
import com.political.power.formula.Tiers;
import com.political.power.formula.Vow;
import com.political.power.formula.VowSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/** The numbers of the capability set, tested at their boundaries (program 10.10). */
class FormulaTest {

    private static final double EPS = 1e-9;

    @AfterEach
    void restore() {
        Config.reset();
    }

    // ── tiers ───────────────────────────────────────────────────────────────────────────────

    @Test
    void tiersAreTheFourSpineMilestones() {
        int[] expected = {0, 1, 1, 1, 2, 2, 2, 3, 3, 3, 4};
        for (int node = 0; node <= 10; node++) {
            assertEquals(expected[node], Tiers.tier(node), "highest spine node " + node);
        }
        assertEquals(4, Tiers.tier(14), "side nodes do not add tiers");
        assertEquals(7, Tiers.nodeForTier(3));
        assertEquals(-1, Tiers.nodeForTier(5));
        assertFalse(Tiers.capstone(9));
        assertTrue(Tiers.capstone(10));
    }

    // ── resource pool ───────────────────────────────────────────────────────────────────────

    @Test
    void poolMaximumGrowsWithTier() {
        assertEquals(100.0, ResourcePool.max(0, 0), EPS);
        assertEquals(125.0, ResourcePool.max(1, 0), EPS);
        assertEquals(200.0, ResourcePool.max(4, 0), EPS);
        assertEquals(220.0, ResourcePool.max(4, 20), EPS, "a no-armor vow adds its bonus");
        assertEquals(200.0, ResourcePool.max(9, 0), EPS, "tiers above 4 do not exist");
        assertEquals(1.0, ResourcePool.max(0, -500), EPS, "never below 1");
    }

    @Test
    void regenWaitsForTheDelayThenRunsAtTheConfiguredRate() {
        // Defaults: delay 60 ticks, 4 per second.
        assertEquals(10.0, ResourcePool.regen(10.0, 100.0, 60, 20, false), EPS, "exactly at the delay nothing has come back");
        assertEquals(10.2, ResourcePool.regen(10.0, 100.0, 61, 20, false), EPS, "one tick after the delay is one tick of regen");
        assertEquals(10.0 + 4.0, ResourcePool.regen(10.0, 100.0, 80, 20, false), EPS, "a full second of regen");
        assertEquals(10.0 + 4.0, ResourcePool.regen(10.0, 100.0, 5000, 20, false), EPS, "long idle does not regen more than the interval");
        assertEquals(10.0, ResourcePool.regen(10.0, 100.0, 5000, 0, false), EPS, "no time passed");
    }

    @Test
    void regenNeverExceedsTheMaximumAndPausesInADomain() {
        assertEquals(100.0, ResourcePool.regen(98.0, 100.0, 500, 20, false), EPS);
        assertEquals(50.0, ResourcePool.regen(50.0, 100.0, 500, 20, true), EPS, "a Domain pauses regen");
        assertEquals(120.0, ResourcePool.regen(120.0, 100.0, 500, 20, false), EPS, "above the maximum nothing is taken away");
    }

    @Test
    void deathHalvesTheCurrentAmountRoundingDown() {
        assertEquals(50.0, ResourcePool.afterDeath(100.0), EPS);
        assertEquals(16.0, ResourcePool.afterDeath(33.0), EPS);
        assertEquals(0.0, ResourcePool.afterDeath(1.0), EPS);
        assertEquals(0.0, ResourcePool.afterDeath(0.5), EPS);
        assertEquals(0.0, ResourcePool.afterDeath(-4.0), EPS);
    }

    @Test
    void clampingAfterALostBonus() {
        assertEquals(100.0, ResourcePool.clampToMax(115.0, 100.0), EPS);
        assertEquals(60.0, ResourcePool.clampToMax(60.0, 100.0), EPS);
        assertEquals(0.0, ResourcePool.clampToMax(-3.0, 100.0), EPS);
    }

    // ── Blue and Red ────────────────────────────────────────────────────────────────────────

    @Test
    void bluePullsTowardTheCasterAtSixTenthsOfABlockPerTick() {
        Impulses.Velocity v = Impulses.blue(0, 64, 0, 8, 64, 0);
        assertEquals(-0.6, v.x(), EPS);
        assertEquals(0.0, v.z(), EPS);
        assertEquals(0.0, v.y(), EPS, "same height, no vertical pull");
        Impulses.Velocity diagonal = Impulses.blue(0, 64, 0, 3, 64, 4);
        assertEquals(0.6, diagonal.horizontalSpeed(), EPS);
    }

    @Test
    void blueVerticalIsClampedBetweenMinusTwoAndPointThree() {
        assertEquals(0.3, Impulses.blue(0, 100, 0, 5, 0, 0).y(), EPS, "target far below the caster");
        assertEquals(-0.2, Impulses.blue(0, 0, 0, 5, 100, 0).y(), EPS, "target far above the caster");
        assertEquals(0.1, Impulses.blue(0, 65, 0, 5, 64, 0).y(), EPS, "a small difference is passed on");
    }

    @Test
    void blueOnATargetDirectlyAboveHasNoHorizontalPull() {
        Impulses.Velocity v = Impulses.blue(1, 64, 1, 1, 70, 1);
        assertEquals(0.0, v.horizontalSpeed(), EPS);
    }

    @Test
    void redPushesAwayAtOnePointFourWithLift() {
        Impulses.Velocity v = Impulses.red(0, 0, 0, 5, 1, 0);
        assertEquals(0.0, v.x(), EPS);
        assertEquals(1.4, v.z(), EPS);
        assertEquals(0.4, v.y(), EPS);
        Impulses.Velocity diagonal = Impulses.red(0, 0, 3, 4, 0, 1);
        assertEquals(1.4, diagonal.horizontalSpeed(), EPS);
        assertEquals(0.84, diagonal.x(), 1e-9);
    }

    @Test
    void redOnATargetDirectlyAboveUsesTheLookDirection() {
        Impulses.Velocity v = Impulses.red(2, 2, 2, 2, 0, -1);
        assertEquals(0.0, v.x(), EPS);
        assertEquals(-1.4, v.z(), EPS);
        assertEquals(0.0, Impulses.red(2, 2, 2, 2, 0, 0).horizontalSpeed(), EPS, "no direction at all gives no push");
    }

    // ── Reverse ─────────────────────────────────────────────────────────────────────────────

    @Test
    void reverseHealCapAndCost() {
        assertEquals(0.0, ReverseFormula.healAmount(0.0), EPS);
        assertEquals(3.5, ReverseFormula.healAmount(3.5), EPS);
        assertEquals(8.0, ReverseFormula.healAmount(8.0), EPS);
        assertEquals(8.0, ReverseFormula.healAmount(19.0), EPS, "capped at eight per cast");
        assertEquals(0.0, ReverseFormula.healAmount(-2.0), EPS);
        assertEquals(7.0, ReverseFormula.cost(3.5), EPS, "two per point actually healed");
        assertEquals(16.0, ReverseFormula.cost(8.0), EPS);
    }

    // ── Barrier ─────────────────────────────────────────────────────────────────────────────

    @Test
    void barrierBlocksFallButNotVoidOrKills() {
        assertTrue(BarrierRules.blocks(BarrierRules.DamageClass.NORMAL));
        assertTrue(BarrierRules.blocks(BarrierRules.DamageClass.FALL));
        assertFalse(BarrierRules.blocks(BarrierRules.DamageClass.VOID));
        assertFalse(BarrierRules.blocks(BarrierRules.DamageClass.UNBLOCKABLE));
    }

    @Test
    void barrierWindowIsFortyTicks() {
        assertFalse(BarrierRules.active(100, 99), "not before it starts");
        assertTrue(BarrierRules.active(100, 100));
        assertTrue(BarrierRules.active(100, 139));
        assertFalse(BarrierRules.active(100, 140), "over after exactly forty ticks");
    }

    // ── Black Flash ─────────────────────────────────────────────────────────────────────────

    @Test
    void blackFlashNeedsTierThreeAndTheWindow() {
        assertTrue(BlackFlash.qualifies(3, 0));
        assertTrue(BlackFlash.qualifies(4, 20));
        assertFalse(BlackFlash.qualifies(2, 5), "tier two is not enough");
        assertFalse(BlackFlash.qualifies(3, 21), "the window is twenty ticks");
        assertFalse(BlackFlash.qualifies(3, -1));
    }

    @Test
    void blackFlashAtChanceZeroNeverHappensAndAtChanceOneAlwaysDoes() {
        setChance(0.0);
        assertFalse(BlackFlash.apply(10, 1.0, 4, 0, 0.0).triggered(), "chance 0 never triggers, even at roll 0");
        setChance(1.0);
        BlackFlash.Result r = BlackFlash.apply(10, 1.0, 4, 0, 0.999999);
        assertTrue(r.triggered());
        assertEquals(25.0, r.damage(), EPS);
    }

    @Test
    void blackFlashRollBoundary() {
        // Default chance 0.05: a roll of exactly 0.05 misses, just below hits.
        assertTrue(BlackFlash.apply(10, 1.0, 3, 0, 0.0499).triggered());
        assertFalse(BlackFlash.apply(10, 1.0, 3, 0, 0.05).triggered());
    }

    @Test
    void blackFlashReplacesTheCritMultiplierInsteadOfStackingWithIt() {
        setChance(1.0);
        // A critical hit of 15 came from 10 x 1.5. A Black Flash makes it 10 x 2.5, not 15 x 2.5.
        BlackFlash.Result crit = BlackFlash.apply(15.0, 1.5, 4, 0, 0.0);
        assertEquals(25.0, crit.damage(), EPS);
        BlackFlash.Result plain = BlackFlash.apply(10.0, 1.0, 4, 0, 0.0);
        assertEquals(25.0, plain.damage(), EPS);
        // A bigger crit (Crit Damage +100 percent) is undone just as exactly.
        BlackFlash.Result bigCrit = BlackFlash.apply(20.0, 2.0, 4, 0, 0.0);
        assertEquals(25.0, bigCrit.damage(), EPS);
    }

    @Test
    void blackFlashLeavesDamageAloneWhenItDoesNotQualify() {
        setChance(1.0);
        BlackFlash.Result r = BlackFlash.apply(12.0, 1.5, 1, 0, 0.0);
        assertFalse(r.triggered());
        assertEquals(12.0, r.damage(), EPS);
    }

    private static void setChance(double chance) {
        // Build a store that sets the chance, the way the config file would.
        java.nio.file.Path file;
        try {
            file = java.nio.file.Files.createTempFile("power-test", ".json");
            java.nio.file.Files.writeString(file, "{\"power\": {\"black_flash\": {\"chance\": " + chance + "}}}");
            Config.useForTests(com.political.core.config.ConfigStore.load(file, com.political.core.config.ConfigCatalog.keys()));
            java.nio.file.Files.deleteIfExists(file);
        } catch (java.io.IOException e) {
            throw new IllegalStateException(e);
        }
    }

    // ── Domain ──────────────────────────────────────────────────────────────────────────────

    @Test
    void domainContainsPointsUpToTheRadius() {
        assertTrue(DomainRules.contains(0, 0, 0, 12, 0, 0), "the edge counts as inside");
        assertFalse(DomainRules.contains(0, 0, 0, 12.01, 0, 0));
        assertTrue(DomainRules.contains(0, 0, 0, 6, 6, 6));
        assertFalse(DomainRules.contains(0, 0, 0, 7, 7, 7), "the radius is a sphere: 7,7,7 is 12.12 away");
    }

    @Test
    void domainSpacingAtExactlyTwentyFourBlocks() {
        assertTrue(DomainRules.tooClose(true, 0, 0, 0, 24, 0, 0), "exactly 24 is refused");
        assertFalse(DomainRules.tooClose(true, 0, 0, 0, 24.01, 0, 0));
        assertTrue(DomainRules.tooClose(true, 0, 0, 0, 10, 0, 0));
        assertFalse(DomainRules.tooClose(false, 0, 0, 0, 1, 0, 0), "another dimension never overlaps");
    }

    @Test
    void domainMeleeBonus() {
        assertEquals(1.25, DomainRules.meleeMultiplier(true), EPS);
        assertEquals(1.0, DomainRules.meleeMultiplier(false), EPS);
    }

    @Test
    void domainLastsSixHundredTicksThenCoolsDownTwentyFourHundred() {
        DomainState d = DomainState.cast(1000);
        assertTrue(d.isActive(1000));
        assertTrue(d.isActive(1599));
        assertFalse(d.isActive(1600), "it ends after exactly 600 ticks");
        assertEquals(600 + 2400, d.cooldownRemaining(1000), "the cooldown counts from the moment it ends");
        assertEquals(2400, d.cooldownRemaining(1600));
        assertEquals(1, d.cooldownRemaining(3999));
        assertEquals(0, d.cooldownRemaining(4000));
    }

    @Test
    void endingADomainEarlyStartsTheCooldownThen() {
        DomainState d = DomainState.cast(0);
        d.endEarly(100); // the caster died
        assertFalse(d.isActive(100));
        assertEquals(2400, d.cooldownRemaining(100));
        assertEquals(0, d.cooldownRemaining(2500));
        d.endEarly(200); // ending again changes nothing
        assertEquals(2300, d.cooldownRemaining(200));
    }

    // ── Vows ────────────────────────────────────────────────────────────────────────────────

    private static final Vow.Loadout NAKED_AND_STILL = new Vow.Loadout(true, true, false);

    @Test
    void aVowCanOnlyBeTakenWhileItsConditionHolds() {
        VowSet v = new VowSet();
        assertEquals(VowSet.AcceptResult.CONDITION_NOT_MET, v.accept(Vow.NO_ARMOR, new Vow.Loadout(false, true, false), 0));
        assertEquals(VowSet.AcceptResult.CONDITION_NOT_MET, v.accept(Vow.NO_OFFHAND, new Vow.Loadout(true, false, false), 0));
        assertEquals(VowSet.AcceptResult.CONDITION_NOT_MET, v.accept(Vow.NO_SPRINT, new Vow.Loadout(true, true, true), 0));
        assertEquals(VowSet.AcceptResult.ACCEPTED, v.accept(Vow.NO_ARMOR, NAKED_AND_STILL, 0));
        assertEquals(VowSet.AcceptResult.ALREADY_ACTIVE, v.accept(Vow.NO_ARMOR, NAKED_AND_STILL, 0));
    }

    @Test
    void atMostTwoVowsAreActive() {
        VowSet v = new VowSet();
        assertEquals(VowSet.AcceptResult.ACCEPTED, v.accept(Vow.NO_ARMOR, NAKED_AND_STILL, 0));
        assertEquals(VowSet.AcceptResult.ACCEPTED, v.accept(Vow.NO_OFFHAND, NAKED_AND_STILL, 0));
        assertEquals(VowSet.AcceptResult.LIMIT_REACHED, v.accept(Vow.NO_SPRINT, NAKED_AND_STILL, 0));
        assertEquals(Set.of(Vow.NO_ARMOR, Vow.NO_OFFHAND), v.active());
    }

    @Test
    void vowBonuses() {
        VowSet v = new VowSet();
        assertEquals(0.0, v.resourceBonus());
        v.accept(Vow.NO_ARMOR, NAKED_AND_STILL, 0);
        v.accept(Vow.NO_SPRINT, NAKED_AND_STILL, 0);
        assertEquals(20.0, v.resourceBonus(), EPS);
        assertEquals(0.0, v.cooldownReduction(), EPS);
        assertEquals(0.10, v.damageBonus(), EPS);
    }

    @Test
    void breakingAVowRemovesItAndStartsTheSixThousandTickWait() {
        VowSet v = new VowSet();
        v.accept(Vow.NO_ARMOR, NAKED_AND_STILL, 0);
        v.accept(Vow.NO_SPRINT, NAKED_AND_STILL, 0);
        assertEquals(List.of(), v.check(NAKED_AND_STILL, 20), "nothing broke");

        List<Vow> broken = v.check(new Vow.Loadout(false, true, false), 100); // put armor on
        assertEquals(List.of(Vow.NO_ARMOR), broken);
        assertEquals(Set.of(Vow.NO_SPRINT), v.active());
        assertEquals(0.0, v.resourceBonus(), EPS, "the bonus is gone");
        assertEquals(6000, v.reacceptRemaining(100));

        assertEquals(VowSet.AcceptResult.ON_COOLDOWN, v.accept(Vow.NO_ARMOR, NAKED_AND_STILL, 6099));
        assertEquals(VowSet.AcceptResult.ACCEPTED, v.accept(Vow.NO_ARMOR, NAKED_AND_STILL, 6100), "allowed again after exactly 6000 ticks");
    }

    @Test
    void deathClearsEveryVowWithoutAWait() {
        VowSet v = new VowSet();
        v.accept(Vow.NO_ARMOR, NAKED_AND_STILL, 0);
        v.accept(Vow.NO_OFFHAND, NAKED_AND_STILL, 0);
        v.clearOnDeath();
        assertEquals(Set.of(), v.active());
        assertEquals(0, v.reacceptRemaining(0));
        assertEquals(VowSet.AcceptResult.ACCEPTED, v.accept(Vow.NO_ARMOR, NAKED_AND_STILL, 1));
    }

    @Test
    void sprintingBreaksTheNoSprintVow() {
        VowSet v = new VowSet();
        v.accept(Vow.NO_SPRINT, NAKED_AND_STILL, 0);
        assertEquals(List.of(Vow.NO_SPRINT), v.check(new Vow.Loadout(true, true, true), 40));
    }

    @Test
    void loadedVowsAreActiveWithoutAWait() {
        VowSet v = new VowSet(Set.of(Vow.NO_OFFHAND));
        assertTrue(v.isActive(Vow.NO_OFFHAND));
        assertEquals(0.10, v.cooldownReduction(), EPS);
    }
}
