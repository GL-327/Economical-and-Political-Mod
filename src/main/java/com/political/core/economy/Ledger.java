package com.political.core.economy;

import com.political.core.Clock;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * The one place a coin balance changes (M3). Every change is a transaction with an account, an amount, a reason and a
 * source, and is checked here: a debit that the balance cannot cover changes nothing and says so; a transfer either
 * moves the whole amount or nothing; a credit saturates at {@link #MAX_BALANCE} instead of overflowing into a negative
 * number. The balances themselves stay where they were (the {@link Balances} adapter), so the saved format is
 * unchanged. The journal of recent transactions is kept in memory (bounded) for admins and tests.
 *
 * <p>Not thread safe on purpose: all balance changes happen on the server thread.</p>
 */
public final class Ledger {

    /** The largest balance an account can hold. Half of the long range, so adding two balances can never overflow. */
    public static final long MAX_BALANCE = Long.MAX_VALUE / 2;

    /** Journal length; older transactions fall off (the totals per reason do not). */
    public static final int JOURNAL_SIZE = 5_000;

    public enum Kind { CREDIT, DEBIT, SET }

    /**
     * One transaction.
     *
     * @param tick         server tick when it happened
     * @param account      whose balance (a player UUID as text)
     * @param kind         what happened
     * @param amount       the amount asked for
     * @param applied      the amount that really moved (a credit near the maximum applies less than asked)
     * @param balanceAfter the balance after it
     * @param reason       why (see {@link Reasons})
     * @param source       who asked: the class that called, or a name given by the caller
     */
    public record Entry(long tick, String account, Kind kind, long amount, long applied, long balanceAfter, String reason, String source) {
    }

    /** Where the balances live. */
    public interface Balances {
        long get(String account);

        void put(String account, long value);
    }

    private final Balances balances;
    private final Deque<Entry> journal = new ArrayDeque<>();
    private final Map<String, Long> netByReason = new TreeMap<>();
    private long refusedDebits;
    /** Told whenever the totals change, so a store can mark itself dirty; set once by {@code LedgerStore}. */
    private Runnable onChange = () -> { };

    public Ledger(Balances balances) {
        this.balances = balances;
    }

    /** What survives a restart: the running totals. The journal is a per-run diagnostic and is not saved. */
    public record Totals(Map<String, Long> netByReason, long refusedDebits) {
    }

    /** The totals now (a copy). */
    public Totals totals() {
        return new Totals(new TreeMap<>(netByReason), refusedDebits);
    }

    /** Replaces the totals with saved ones, at startup, before any transaction. Does not call the change listener. */
    public void restoreTotals(Totals saved) {
        netByReason.clear();
        netByReason.putAll(saved.netByReason());
        refusedDebits = Math.max(0L, saved.refusedDebits());
    }

    public void onChange(Runnable listener) {
        this.onChange = listener == null ? () -> { } : listener;
    }

    public long balance(String account) {
        return Math.max(0L, balances.get(account));
    }

    /** Adds coins. Returns the amount that was added (less than asked only at the maximum); amounts of zero or less change nothing. */
    public long credit(String account, long amount, String reason, String source) {
        if (amount <= 0) {
            return 0L;
        }
        long before = balance(account);
        long after = Math.min(MAX_BALANCE, before + Math.min(amount, MAX_BALANCE));
        balances.put(account, after);
        record(account, Kind.CREDIT, amount, after - before, after, reason, source);
        return after - before;
    }

    /** Takes coins if the balance covers them; otherwise nothing changes and the answer is false. An amount of zero or less is a no-op that succeeds. */
    public boolean debit(String account, long amount, String reason, String source) {
        if (amount <= 0) {
            return true;
        }
        long before = balance(account);
        if (before < amount) {
            refusedDebits++;
            onChange.run();
            return false;
        }
        balances.put(account, before - amount);
        record(account, Kind.DEBIT, amount, amount, before - amount, reason, source);
        return true;
    }

    /** Sets a balance (admin tools and migrations). Negative values become zero, and nothing goes above the maximum. */
    public void set(String account, long amount, String reason, String source) {
        long before = balance(account);
        long after = Math.max(0L, Math.min(MAX_BALANCE, amount));
        balances.put(account, after);
        record(account, Kind.SET, amount, after - before, after, reason, source);
    }

    /**
     * Moves coins from one account to another: all of it or nothing. Fails (changing nothing) if the payer cannot cover
     * it or the receiver would pass the maximum balance.
     */
    public boolean transfer(String from, String to, long amount, String reason, String source) {
        if (amount <= 0 || from.equals(to)) {
            return false;
        }
        if (balance(from) < amount || balance(to) + amount > MAX_BALANCE) {
            if (balance(from) < amount) {
                refusedDebits++;
            }
            return false;
        }
        debit(from, amount, reason, source);
        credit(to, amount, reason, source);
        return true;
    }

    private void record(String account, Kind kind, long amount, long applied, long balanceAfter, String reason, String source) {
        if (journal.size() >= JOURNAL_SIZE) {
            journal.removeFirst();
        }
        journal.addLast(new Entry(Clock.ticks(), account, kind, amount, applied, balanceAfter, reason, source));
        long signed = switch (kind) {
            case CREDIT, SET -> applied;
            case DEBIT -> -applied;
        };
        netByReason.merge(reason, signed, Long::sum);
        onChange.run();
    }

    /** The newest {@code count} transactions, newest first. */
    public List<Entry> recent(int count) {
        List<Entry> all = new ArrayList<>(journal);
        Collections.reverse(all);
        return all.subList(0, Math.min(count, all.size()));
    }

    /** The newest {@code count} transactions of one account, newest first. */
    public List<Entry> recentFor(String account, int count) {
        List<Entry> out = new ArrayList<>();
        for (Entry e : recent(JOURNAL_SIZE)) {
            if (e.account().equals(account)) {
                out.add(e);
                if (out.size() >= count) {
                    break;
                }
            }
        }
        return out;
    }

    /** Net coins created (positive) or destroyed (negative) by each reason since the totals began (they survive restarts, see {@code LedgerStore}). */
    public Map<String, Long> netByReason() {
        return new LinkedHashMap<>(netByReason);
    }

    /** How many debits were refused for lack of funds since the totals began. */
    public long refusedDebits() {
        return refusedDebits;
    }

    public int journalSize() {
        return journal.size();
    }
}
