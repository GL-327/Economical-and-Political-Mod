package com.political.power;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.political.SchoolTree;
import com.political.magic.MagicSchool;
import com.political.power.formula.Tiers;
import com.political.power.formula.TreeAdvice;
import com.political.power.formula.TreeAdvice.Status;
import com.political.power.model.PowerCatalog;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** What the tree screen tells a player: the status of a node, the next step and what it would bring. */
class TreeAdviceTest {

    private static final MagicSchool GRAVITAS = MagicSchool.GRAVITAS;

    private static SchoolTree.SchoolNode node(String id) {
        SchoolTree.SchoolNode node = SchoolTree.getNode(id);
        assertNotNull(node, id);
        return node;
    }

    private static Set<String> spine(MagicSchool school, int upTo) {
        Set<String> out = new HashSet<>();
        for (int i = 1; i <= upTo; i++) {
            out.add(school.prefix + i);
        }
        return out;
    }

    @Test
    void spineNumbersAreOneToTenAndSideNodesAreZero() {
        assertEquals(1, TreeAdvice.spineNumber(node("gra1")));
        assertEquals(10, TreeAdvice.spineNumber(node("gra10")));
        assertEquals(0, TreeAdvice.spineNumber(node("graa")));
        assertEquals(0, TreeAdvice.spineNumber(node("grad")));
    }

    @Test
    void statusChecksParentThenLevelThenShardsLikeThePurchase() {
        Set<String> none = Set.of();
        assertEquals(Status.READY, TreeAdvice.status(node("gra1"), none, 2, 4));
        assertEquals(Status.NEEDS_SHARDS, TreeAdvice.status(node("gra1"), none, 2, 3));
        assertEquals(Status.NEEDS_LEVEL, TreeAdvice.status(node("gra1"), none, 1, 99));
        assertEquals(Status.PARENT_LOCKED, TreeAdvice.status(node("gra2"), none, 50, 99));
        assertEquals(Status.UNLOCKED, TreeAdvice.status(node("gra1"), Set.of("gra1"), 1, 0));
        // with both a level and a shard shortage the level is reported first, as unlockNode does
        assertEquals(Status.NEEDS_LEVEL, TreeAdvice.status(node("gra2"), Set.of("gra1"), 2, 0));
    }

    @Test
    void firstStepOfAFreshPlayerIsNodeOneAndShowsWhatIsMissing() {
        TreeAdvice.NextStep step = TreeAdvice.nextSpine(GRAVITAS, Set.of(), 1, 0);
        assertNotNull(step);
        assertEquals("gra1", step.node().id());
        assertEquals(1, step.spineNumber());
        assertEquals(1, step.tierAfter());
        assertEquals(Status.NEEDS_LEVEL, step.status());
        assertEquals(1, step.levelsMissing());
        assertEquals(step.node().shardCost(), step.shardsMissing());
        assertEquals(List.of("IRONMARROW"), step.unlocks());
    }

    @Test
    void nextStepSkipsWhatIsBoughtAndFollowsTheSpine() {
        TreeAdvice.NextStep step = TreeAdvice.nextSpine(GRAVITAS, spine(GRAVITAS, 3), 99, 99);
        assertEquals("gra4", step.node().id());
        assertEquals(Status.READY, step.status());
        assertEquals(0, step.shardsMissing());
        assertEquals(0, step.levelsMissing());
        assertEquals(2, step.tierAfter());
        assertTrue(step.unlocks().contains(PowerCatalog.BLUE), "node 4 of Gravitas grants Blue");
    }

    @Test
    void sideNodesNeverCountAsTheNextSpineStep() {
        Set<String> bought = spine(GRAVITAS, 2);
        bought.add("graa");
        assertEquals("gra3", TreeAdvice.nextSpine(GRAVITAS, bought, 99, 99).node().id());
    }

    @Test
    void thereIsNoNextStepOnceTheCapstoneIsBought() {
        assertNull(TreeAdvice.nextSpine(GRAVITAS, spine(GRAVITAS, 10), 50, 0));
    }

    @Test
    void theCapstoneGrantsDomainInEverySchool() {
        for (MagicSchool school : MagicSchool.values()) {
            TreeAdvice.NextStep step = TreeAdvice.nextSpine(school, spine(school, 9), 50, 30);
            assertEquals(school.prefix + "10", step.node().id());
            assertEquals(Tiers.MAX_TIER, step.tierAfter());
            assertTrue(step.unlocks().contains(PowerCatalog.DOMAIN), school.name());
        }
    }

    @Test
    void techniquesAreListedAtTheNodeThatGrantsThem() {
        assertEquals(List.of("REVERSE"), TreeAdvice.unlocksAt(MagicSchool.RADIANCE, 1));
        assertEquals(List.of("BARRIER"), TreeAdvice.unlocksAt(MagicSchool.RADIANCE, 4));
        assertEquals(List.of(PowerCatalog.DOMAIN), TreeAdvice.unlocksAt(MagicSchool.RADIANCE, 10));
        assertEquals(List.of(), TreeAdvice.unlocksAt(MagicSchool.RADIANCE, 2));
        assertEquals(List.of(PowerCatalog.DOMAIN), TreeAdvice.unlocksAt(MagicSchool.BLOODBINDING, 10), "a dormant school still shows its capstone");
    }
}
