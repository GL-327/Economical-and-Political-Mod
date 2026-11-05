package com.political.power;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.political.power.formula.Innate;
import com.political.power.formula.LegacyChoice;
import com.political.power.formula.Mastery;
import com.political.power.formula.Reroll;
import com.political.power.formula.Tiers;
import com.political.power.model.PowerCatalog;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** The innate technique: the random draw, the rules of rerolling and mastery, and what players from before keep. */
class InnateTest {

    // ── the draw ────────────────────────────────────────────────────────────────────────────

    @Test
    void theDrawNeverGivesTheDomainAndCoversEveryOtherTechnique() {
        assertFalse(Innate.pool().contains(PowerCatalog.DOMAIN));
        Set<String> expected = new HashSet<>();
        for (PowerCatalog.TechniqueDef def : PowerCatalog.techniques()) {
            if (!PowerCatalog.DOMAIN.equals(def.id())) {
                expected.add(def.id());
            }
        }
        assertEquals(expected, new HashSet<>(Innate.pool()));
    }

    @Test
    void everyTechniqueCanBeDrawnWithTheRightRoll() {
        Map<String, Integer> seen = new HashMap<>();
        int steps = 10_000;
        for (int i = 0; i < steps; i++) {
            seen.merge(Innate.draw(i / (double) steps, Set.of()), 1, Integer::sum);
        }
        assertEquals(Innate.pool().size(), seen.size(), "every technique shows up over the whole range of rolls");
        for (Map.Entry<String, Integer> e : seen.entrySet()) {
            double share = e.getValue() / (double) steps;
            assertEquals(Innate.chance(e.getKey()), share, 0.002, e.getKey());
        }
    }

    @Test
    void theOddsAddUpAndTheRareOnesAreRarer() {
        double total = 0.0;
        for (String id : Innate.pool()) {
            total += Innate.chance(id);
        }
        assertEquals(1.0, total, 1e-9);
        assertTrue(Innate.chance("EMBERHEART") > Innate.chance(PowerCatalog.BLUE));
        assertTrue(Innate.chance(PowerCatalog.BLUE) > Innate.chance(PowerCatalog.REVERSE));
    }

    @Test
    void anExcludedTechniqueIsNeverDrawn() {
        Set<String> excluded = Set.of(PowerCatalog.BLUE, "EMBERHEART");
        for (int i = 0; i < 2_000; i++) {
            String drawn = Innate.draw(i / 2_000.0, excluded);
            assertFalse(excluded.contains(drawn));
        }
    }

    @Test
    void rollsAtTheEdgesAreSafeAndNothingLeftGivesNull() {
        assertEquals(Innate.pool().get(0), Innate.draw(0.0, Set.of()));
        assertEquals(Innate.pool().get(Innate.pool().size() - 1), Innate.draw(0.9999999999, Set.of()));
        assertEquals(Innate.pool().get(Innate.pool().size() - 1), Innate.draw(5.0, Set.of()), "a roll above 1 counts as the top");
        assertEquals(Innate.pool().get(0), Innate.draw(-1.0, Set.of()), "a negative roll counts as the bottom");
        assertNull(Innate.draw(0.5, new HashSet<>(Innate.pool())));
    }

    // ── mastery ─────────────────────────────────────────────────────────────────────────────

    @Test
    void aSleepingTechniqueIsTierZeroAndTheRankSetsTheTierOnceAwake() {
        assertEquals(0, Mastery.tier(false, 500));
        assertEquals(1, Mastery.tier(true, 0));
        assertEquals(1, Mastery.tier(true, 14));
        assertEquals(2, Mastery.tier(true, 15));
        assertEquals(3, Mastery.tier(true, 45));
        assertEquals(4, Mastery.tier(true, 100));
        assertEquals(1, Mastery.tier(true, -5), "a negative count is treated as zero");
    }

    @Test
    void masteryAddsDamageAndCooldownOnlyAboveNovice() {
        assertEquals(0.0, Mastery.damageBonus(0), 1e-12);
        assertEquals(0.0, Mastery.damageBonus(1), 1e-12);
        assertEquals(0.05, Mastery.damageBonus(2), 1e-12);
        assertEquals(0.15, Mastery.damageBonus(4), 1e-12);
        assertEquals(0.0, Mastery.cooldownReduction(1), 1e-12);
        assertEquals(0.09, Mastery.cooldownReduction(4), 1e-12);
    }

    @Test
    void theDomainUnlocksAtMasterAndTheNextRankCountdownEndsThere() {
        assertFalse(Mastery.domainUnlocked(Tiers.MAX_TIER - 1));
        assertTrue(Mastery.domainUnlocked(Tiers.MAX_TIER));
        assertEquals(15, Mastery.usesToNextRank(0));
        assertEquals(1, Mastery.usesToNextRank(14));
        assertEquals(31, Mastery.usesToNextRank(69), "69 uses is Expert, 100 is Master");
        assertEquals(-1, Mastery.usesToNextRank(100));
        assertEquals(-1, Mastery.usesToNextRank(900));
    }

    // ── rerolls ─────────────────────────────────────────────────────────────────────────────

    @Test
    void theShardPriceGrowsWithEveryRerollIsHigherWhenAwakenedAndIsCapped() {
        assertEquals(10, Reroll.shardCost(0, false));
        assertEquals(15, Reroll.shardCost(1, false));
        assertEquals(20, Reroll.shardCost(2, false), "10 + 5 x 2");
        assertEquals(40, Reroll.shardCost(2, true), "doubled once awakened");
    }

    @Test
    void theCapAndTheChargeLimitHold() {
        assertEquals(100, Reroll.shardCost(500, true));
        assertEquals(100, Reroll.shardCost(500, false));
        assertEquals(1, Reroll.chargesAfterGrant(0));
        assertEquals(3, Reroll.chargesAfterGrant(2));
        assertEquals(3, Reroll.chargesAfterGrant(3), "never above the maximum");
    }

    // ── players from before ─────────────────────────────────────────────────────────────────

    @Test
    void aPlayerWithNothingGetsAnotherDrawNotALegacyChoice() {
        assertNull(LegacyChoice.of(null, List.of(), null, Set.of()));
    }

    @Test
    void theEquippedTechniqueIsKeptThenTheFirstLearnedThenWhatTheSchoolGranted() {
        assertEquals("STORM_VEIN", LegacyChoice.of("STORM_VEIN", List.of("EMBERHEART"), "PYROMANCY", Set.of("pyr1")));
        assertEquals("EMBERHEART", LegacyChoice.of(null, List.of("WILDGROWTH", "EMBERHEART"), null, Set.of()), "sorted, so the first by name");
        assertEquals("EMBERHEART", LegacyChoice.of(null, List.of(), "PYROMANCY", Set.of("pyr1", "pyr2")));
        assertEquals("IRONMARROW", LegacyChoice.of(null, List.of(), "GRAVITAS", Set.of("gra1", "gra2", "gra3", "gra4")),
                "node 4 of Gravitas grants Ironmarrow and Blue; the first one in catalog order is kept");
    }

    @Test
    void theDomainIsNeverAKeptTechniqueAndUnknownNamesAreIgnored() {
        assertNull(LegacyChoice.of(PowerCatalog.DOMAIN, List.of(PowerCatalog.DOMAIN), null, Set.of()));
        assertNull(LegacyChoice.of("NOT_A_TECHNIQUE", List.of("ALSO_NOT"), "NOT_A_SCHOOL", Set.of()));
        assertNotEquals(PowerCatalog.DOMAIN, LegacyChoice.of(PowerCatalog.DOMAIN, List.of("EMBERHEART"), null, Set.of()));
    }
}
