package com.political.core.economy;

/**
 * Reasons for a coin transaction. A caller that has not been given a specific reason yet uses {@link #UNSPECIFIED};
 * the ledger still records who asked, so the callers can be migrated one at a time (HANDOFF.md, M3).
 */
public final class Reasons {

    private Reasons() {
    }

    public static final String UNSPECIFIED = "unspecified";
    /** Coins earned in play (kills, quests, rewards); the callers that apply income multipliers use this. */
    public static final String INCOME = "income";
    /** Coins spent in a shop, the bazaar, an auction or on a service. */
    public static final String SPEND = "spend";
    /** A transfer from one player to another. */
    public static final String TRANSFER = "transfer";
    /** Taxes, fines and other coins taken by the government or the server. */
    public static final String LEVY = "levy";
    /** Interest paid by the bank. */
    public static final String INTEREST = "interest";
    /** An administrator changed the balance. */
    public static final String ADMIN = "admin";
    /** The server fixed a balance (migration, repair). */
    public static final String REPAIR = "repair";
}
