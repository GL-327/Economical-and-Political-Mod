package com.political.government;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.political.government.ElectionPhase.Event;
import com.political.government.ElectionPhase.Facts;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** The election phases and the legal moves between them. */
class ElectionPhaseTest {

    private static final Facts RUNNING_TERM = new Facts(true, false, false, false);
    private static final Facts OPEN_ELECTION = new Facts(true, false, false, true);

    @Test
    void everyCombinationOfTheFourFactsHasOnePhaseAndTheStrongerConditionWins() {
        assertEquals(ElectionPhase.DISABLED, ElectionPhase.of(false, true, true, true));
        assertEquals(ElectionPhase.DISABLED, ElectionPhase.of(false, false, false, false));
        assertEquals(ElectionPhase.PAUSED, ElectionPhase.of(true, true, true, true));
        assertEquals(ElectionPhase.PAUSED, ElectionPhase.of(true, true, false, false));
        assertEquals(ElectionPhase.SUSPENDED, ElectionPhase.of(true, false, true, true));
        assertEquals(ElectionPhase.CAMPAIGN, ElectionPhase.of(true, false, false, true));
        assertEquals(ElectionPhase.TERM, ElectionPhase.of(true, false, false, false));
    }

    @Test
    void theClockRunsOnlyInATermOrACampaign() {
        for (ElectionPhase phase : ElectionPhase.values()) {
            assertEquals(phase == ElectionPhase.TERM || phase == ElectionPhase.CAMPAIGN, phase.runs(), phase.name());
        }
    }

    @Test
    void onlyACampaignTakesNominationsAndOnlyATermStartsAnElection() {
        for (ElectionPhase phase : ElectionPhase.values()) {
            assertEquals(phase == ElectionPhase.CAMPAIGN, phase.canNominate(), phase.name());
            assertEquals(phase == ElectionPhase.TERM, phase.canStartElection(), phase.name());
        }
    }

    @Test
    void anElectionStartsOnlyFromARunningTerm() {
        assertEquals(Optional.of(OPEN_ELECTION), ElectionPhase.apply(RUNNING_TERM, Event.START_ELECTION));
        assertEquals(Optional.empty(), ElectionPhase.apply(OPEN_ELECTION, Event.START_ELECTION), "already open");
        assertEquals(Optional.empty(), ElectionPhase.apply(new Facts(false, false, false, false), Event.START_ELECTION), "disabled");
        assertEquals(Optional.empty(), ElectionPhase.apply(new Facts(true, true, false, false), Event.START_ELECTION), "paused");
        assertEquals(Optional.empty(), ElectionPhase.apply(new Facts(true, false, true, false), Event.START_ELECTION), "dictatorship");
    }

    @Test
    void anElectionEndsOnlyWhenOneIsOpenEvenIfThePhaseIsPaused() {
        assertEquals(Optional.of(RUNNING_TERM), ElectionPhase.apply(OPEN_ELECTION, Event.END_ELECTION));
        assertEquals(Optional.empty(), ElectionPhase.apply(RUNNING_TERM, Event.END_ELECTION), "nothing to end");
        Facts pausedOpen = new Facts(true, true, false, true);
        assertEquals(Optional.of(new Facts(true, true, false, false)), ElectionPhase.apply(pausedOpen, Event.END_ELECTION));
    }

    @Test
    void theSwitchesAreAlwaysLegalAndChangeOnlyTheirOwnFact() {
        Facts start = new Facts(true, false, false, true);
        assertEquals(new Facts(false, false, false, true), ElectionPhase.apply(start, Event.DISABLE).orElseThrow());
        assertEquals(new Facts(true, true, false, true), ElectionPhase.apply(start, Event.PAUSE).orElseThrow());
        assertEquals(new Facts(true, false, true, true), ElectionPhase.apply(start, Event.DICTATORSHIP_BEGINS).orElseThrow());
        assertEquals(start, ElectionPhase.apply(start, Event.ENABLE).orElseThrow(), "enabling twice is a no-op");
        assertEquals(start, ElectionPhase.apply(start, Event.RESUME).orElseThrow());
        assertEquals(start, ElectionPhase.apply(start, Event.DICTATORSHIP_ENDS).orElseThrow());
    }

    @Test
    void aCampaignThatWasOpenWhenTheSystemWasPausedComesBackAsACampaign() {
        Facts paused = ElectionPhase.apply(OPEN_ELECTION, Event.PAUSE).orElseThrow();
        assertEquals(ElectionPhase.PAUSED, ElectionPhase.of(paused));
        Facts resumed = ElectionPhase.apply(paused, Event.RESUME).orElseThrow();
        assertEquals(ElectionPhase.CAMPAIGN, ElectionPhase.of(resumed));
        assertTrue(ElectionPhase.of(resumed).canNominate());
        assertFalse(ElectionPhase.of(paused).canNominate());
    }
}
