package com.political.core.economy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * The rules of the coin ledger. They are written as the behavior the old DataManager and CoinManager code had (a debit
 * that cannot be covered returns false and changes nothing; a negative balance is never stored), plus the two cases the
 * old code got wrong: a credit could overflow into a negative number (which was then stored as zero), and a transfer
 * was a debit followed by a credit with nothing to undo the debit.
 */
class LedgerTest {

    private final Map<String, Long> store = new HashMap<>();
    private final Ledger ledger = new Ledger(new Ledger.Balances() {
        @Override
        public long get(String account) {
            return store.getOrDefault(account, 0L);
        }

        @Override
        public void put(String account, long value) {
            store.put(account, value);
        }
    });

    private static final String A = "a";
    private static final String B = "b";

    @Test
    void anUnknownAccountHasZeroCoins() {
        assertEquals(0L, ledger.balance(A));
    }

    @Test
    void creditsAddUp() {
        assertEquals(50L, ledger.credit(A, 50, Reasons.INCOME, "test"));
        assertEquals(25L, ledger.credit(A, 25, Reasons.INCOME, "test"));
        assertEquals(75L, ledger.balance(A));
    }

    @Test
    void creditsOfZeroOrLessChangeNothingAndAreNotRecorded() {
        assertEquals(0L, ledger.credit(A, 0, Reasons.INCOME, "test"));
        assertEquals(0L, ledger.credit(A, -9, Reasons.INCOME, "test"));
        assertEquals(0L, ledger.balance(A));
        assertEquals(0, ledger.journalSize());
    }

    @Test
    void aDebitTakesCoinsWhenTheBalanceCoversItAndExactlyTheBalanceIsEnough() {
        ledger.credit(A, 100, Reasons.INCOME, "test");
        assertTrue(ledger.debit(A, 30, Reasons.SPEND, "test"));
        assertEquals(70L, ledger.balance(A));
        assertTrue(ledger.debit(A, 70, Reasons.SPEND, "test"));
        assertEquals(0L, ledger.balance(A));
    }

    @Test
    void aDebitTheBalanceCannotCoverChangesNothing() {
        ledger.credit(A, 100, Reasons.INCOME, "test");
        assertFalse(ledger.debit(A, 101, Reasons.SPEND, "test"));
        assertEquals(100L, ledger.balance(A));
        assertEquals(1L, ledger.refusedDebits());
        assertEquals(1, ledger.journalSize(), "a refused debit is not a transaction");
    }

    @Test
    void aDebitOfZeroOrLessSucceedsAndChangesNothing() {
        assertTrue(ledger.debit(A, 0, Reasons.SPEND, "test"));
        assertTrue(ledger.debit(A, -5, Reasons.SPEND, "test"));
        assertEquals(0L, ledger.balance(A));
        assertEquals(0, ledger.journalSize());
    }

    @Test
    void aCreditNearTheMaximumSaturatesInsteadOfOverflowing() {
        ledger.set(A, Ledger.MAX_BALANCE - 10, Reasons.ADMIN, "test");
        assertEquals(10L, ledger.credit(A, 1_000, Reasons.INCOME, "test"), "only the room that is left is added");
        assertEquals(Ledger.MAX_BALANCE, ledger.balance(A));
        assertEquals(0L, ledger.credit(A, 5, Reasons.INCOME, "test"));
        assertEquals(Ledger.MAX_BALANCE, ledger.credit(B, Long.MAX_VALUE, Reasons.INCOME, "test"), "a huge credit is capped too");
    }

    @Test
    void setClampsNegativeValuesToZeroAndHugeOnesToTheMaximum() {
        ledger.set(A, -50, Reasons.ADMIN, "test");
        assertEquals(0L, ledger.balance(A));
        ledger.set(A, Long.MAX_VALUE, Reasons.ADMIN, "test");
        assertEquals(Ledger.MAX_BALANCE, ledger.balance(A));
    }

    @Test
    void aTransferMovesTheWholeAmountOrNothing() {
        ledger.credit(A, 100, Reasons.INCOME, "test");
        assertTrue(ledger.transfer(A, B, 40, Reasons.TRANSFER, "test"));
        assertEquals(60L, ledger.balance(A));
        assertEquals(40L, ledger.balance(B));
        assertFalse(ledger.transfer(A, B, 61, Reasons.TRANSFER, "test"), "more than the payer has");
        assertEquals(60L, ledger.balance(A));
        assertEquals(40L, ledger.balance(B));
    }

    @Test
    void aTransferThatWouldOverflowTheReceiverRestoresThePayer() {
        ledger.set(B, Ledger.MAX_BALANCE - 5, Reasons.ADMIN, "test");
        ledger.credit(A, 100, Reasons.INCOME, "test");
        assertFalse(ledger.transfer(A, B, 10, Reasons.TRANSFER, "test"));
        assertEquals(100L, ledger.balance(A), "the payer keeps the coins");
        assertEquals(Ledger.MAX_BALANCE - 5, ledger.balance(B));
    }

    @Test
    void aTransferToYourselfOrOfNothingIsRefused() {
        ledger.credit(A, 100, Reasons.INCOME, "test");
        assertFalse(ledger.transfer(A, A, 10, Reasons.TRANSFER, "test"));
        assertFalse(ledger.transfer(A, B, 0, Reasons.TRANSFER, "test"));
        assertFalse(ledger.transfer(A, B, -3, Reasons.TRANSFER, "test"));
        assertEquals(100L, ledger.balance(A));
    }

    @Test
    void theJournalRecordsReasonSourceAmountAndTheBalanceAfter() {
        ledger.credit(A, 100, Reasons.INCOME, "KillReward");
        ledger.debit(A, 30, Reasons.SPEND, "Shop");
        List<Ledger.Entry> recent = ledger.recent(10);
        assertEquals(2, recent.size());
        Ledger.Entry newest = recent.get(0);
        assertEquals(Ledger.Kind.DEBIT, newest.kind());
        assertEquals(30L, newest.amount());
        assertEquals(70L, newest.balanceAfter());
        assertEquals(Reasons.SPEND, newest.reason());
        assertEquals("Shop", newest.source());
        assertEquals("KillReward", recent.get(1).source());
    }

    @Test
    void theJournalIsBoundedButTheTotalsPerReasonAreNot() {
        for (int i = 0; i < Ledger.JOURNAL_SIZE + 100; i++) {
            ledger.credit(A, 1, Reasons.INCOME, "test");
        }
        assertEquals(Ledger.JOURNAL_SIZE, ledger.journalSize());
        assertEquals(Ledger.JOURNAL_SIZE + 100L, ledger.netByReason().get(Reasons.INCOME));
    }

    @Test
    void netByReasonShowsWhereCoinsAreMadeAndDestroyed() {
        ledger.credit(A, 500, Reasons.INCOME, "test");
        ledger.debit(A, 120, Reasons.SPEND, "test");
        ledger.credit(A, 5, Reasons.INTEREST, "test");
        Map<String, Long> net = ledger.netByReason();
        assertEquals(500L, net.get(Reasons.INCOME));
        assertEquals(-120L, net.get(Reasons.SPEND));
        assertEquals(5L, net.get(Reasons.INTEREST));
    }

    @Test
    void recentForOneAccountFiltersAndLimits() {
        ledger.credit(A, 1, Reasons.INCOME, "x");
        ledger.credit(B, 2, Reasons.INCOME, "x");
        ledger.credit(A, 3, Reasons.INCOME, "x");
        ledger.credit(A, 4, Reasons.INCOME, "x");
        List<Ledger.Entry> mine = ledger.recentFor(A, 2);
        assertEquals(2, mine.size());
        assertEquals(4L, mine.get(0).amount());
        assertEquals(3L, mine.get(1).amount());
    }
}
