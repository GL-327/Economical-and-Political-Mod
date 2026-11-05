package com.political.government;

import java.util.Optional;

/**
 * The national election system as an explicit state machine (M4). The election code kept its state in separate booleans
 * (enabled, paused, an election open) plus a check for a dictatorship. {@link Facts} holds those four, {@link #of} names
 * the phase they add up to, and {@link #apply} is the list of legal moves: a move that is not legal returns nothing,
 * so a code path that tries it is a bug a test can find.
 *
 * <ul>
 *   <li>{@link #DISABLED}: the system is switched off; nothing runs.</li>
 *   <li>{@link #PAUSED}: switched on but paused by an admin; the clock stops.</li>
 *   <li>{@link #SUSPENDED}: a dictatorship is in force; no election runs.</li>
 *   <li>{@link #TERM}: the system runs and no election is open; the term clock counts down to the next election.</li>
 *   <li>{@link #CAMPAIGN}: an election is open: players can stand, vote and (for candidates) buy fake votes.</li>
 * </ul>
 *
 * <p>An election that is open when the system is paused, disabled or suspended stays open in the facts; the phase
 * reports the stronger condition first (DISABLED, then PAUSED, then SUSPENDED).</p>
 */
public enum ElectionPhase {
    DISABLED, PAUSED, SUSPENDED, TERM, CAMPAIGN;

    /** The four facts the election code keeps. */
    public record Facts(boolean enabled, boolean paused, boolean dictatorship, boolean electionOpen) {
    }

    /** Things that can happen to the system. */
    public enum Event {
        /** The term clock or an admin opens an election. Only a running term can; an open election cannot be opened again. */
        START_ELECTION,
        /** The election ends (the votes are counted). Only an open election can end. */
        END_ELECTION,
        ENABLE, DISABLE, PAUSE, RESUME, DICTATORSHIP_BEGINS, DICTATORSHIP_ENDS
    }

    /** The phase for a set of facts. */
    public static ElectionPhase of(Facts facts) {
        if (!facts.enabled()) {
            return DISABLED;
        }
        if (facts.paused()) {
            return PAUSED;
        }
        if (facts.dictatorship()) {
            return SUSPENDED;
        }
        return facts.electionOpen() ? CAMPAIGN : TERM;
    }

    public static ElectionPhase of(boolean enabled, boolean paused, boolean dictatorship, boolean electionOpen) {
        return of(new Facts(enabled, paused, dictatorship, electionOpen));
    }

    /**
     * The facts after an event, or empty if the event is not legal in these facts. Switching things on or off is always
     * legal (it can be a no-op); starting and ending an election are the guarded moves.
     */
    public static Optional<Facts> apply(Facts facts, Event event) {
        return switch (event) {
            case START_ELECTION -> of(facts).canStartElection()
                    ? Optional.of(new Facts(facts.enabled(), facts.paused(), facts.dictatorship(), true)) : Optional.empty();
            case END_ELECTION -> facts.electionOpen()
                    ? Optional.of(new Facts(facts.enabled(), facts.paused(), facts.dictatorship(), false)) : Optional.empty();
            case ENABLE -> Optional.of(new Facts(true, facts.paused(), facts.dictatorship(), facts.electionOpen()));
            case DISABLE -> Optional.of(new Facts(false, facts.paused(), facts.dictatorship(), facts.electionOpen()));
            case PAUSE -> Optional.of(new Facts(facts.enabled(), true, facts.dictatorship(), facts.electionOpen()));
            case RESUME -> Optional.of(new Facts(facts.enabled(), false, facts.dictatorship(), facts.electionOpen()));
            case DICTATORSHIP_BEGINS -> Optional.of(new Facts(facts.enabled(), facts.paused(), true, facts.electionOpen()));
            case DICTATORSHIP_ENDS -> Optional.of(new Facts(facts.enabled(), facts.paused(), false, facts.electionOpen()));
        };
    }

    /** Whether the election clock runs in this phase (the tick does anything). */
    public boolean runs() {
        return this == TERM || this == CAMPAIGN;
    }

    /** Whether a player can put themselves on the ballot. */
    public boolean canNominate() {
        return this == CAMPAIGN;
    }

    /** Whether an election can be started now (by the term clock or an admin). */
    public boolean canStartElection() {
        return this == TERM;
    }
}
