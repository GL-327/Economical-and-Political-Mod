package com.political.core.economy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.political.core.persist.StoreRegistry;
import com.political.core.persist.VersionedStore;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** The coin totals survive a restart through ledger.json, and a bad file never loses the balances or stops the server. */
class LedgerStoreTest {

    @TempDir
    Path world;

    private static Ledger newLedger() {
        Map<String, Long> store = new HashMap<>();
        return new Ledger(new Ledger.Balances() {
            @Override
            public long get(String account) {
                return store.getOrDefault(account, 0L);
            }

            @Override
            public void put(String account, long value) {
                store.put(account, value);
            }
        });
    }

    @Test
    void aFreshWorldStartsAtZeroAndWritesNothingUntilACoinMoves() {
        Ledger ledger = newLedger();
        LedgerStore store = LedgerStore.open(world, ledger);
        assertEquals(VersionedStore.Outcome.FRESH, store.outcome());
        assertFalse(store.store().isDirty());
        assertEquals(0, ledger.totals().netByReason().size());
        ledger.credit("a", 10, Reasons.INCOME, "test");
        assertTrue(store.store().isDirty());
    }

    @Test
    void theTotalsComeBackAfterARestart() {
        Ledger first = newLedger();
        LedgerStore store = LedgerStore.open(world, first);
        first.credit("a", 100, Reasons.INCOME, "test");
        first.debit("a", 30, Reasons.SPEND, "test");
        first.debit("a", 999, Reasons.SPEND, "test"); // refused
        assertTrue(store.store().save());

        Ledger second = newLedger(); // a new run: empty journal, balances restored elsewhere
        LedgerStore.open(world, second);
        assertEquals(first.netByReason(), second.netByReason());
        assertEquals(100L, second.netByReason().get(Reasons.INCOME));
        assertEquals(-30L, second.netByReason().get(Reasons.SPEND));
        assertEquals(1L, second.refusedDebits());
        assertEquals(0, second.journalSize(), "the journal is a per-run diagnostic and is not saved");
    }

    @Test
    void totalsKeepAddingAcrossRuns() {
        Ledger first = newLedger();
        LedgerStore store = LedgerStore.open(world, first);
        first.credit("a", 40, Reasons.INCOME, "test");
        store.store().save();
        Ledger second = newLedger();
        LedgerStore second2 = LedgerStore.open(world, second);
        second.credit("a", 2, Reasons.INCOME, "test");
        second2.store().save();
        Ledger third = newLedger();
        LedgerStore.open(world, third);
        assertEquals(42L, third.netByReason().get(Reasons.INCOME));
    }

    @Test
    void theFileCarriesTheSchemaVersionAndTheTotals() throws IOException {
        Ledger ledger = newLedger();
        LedgerStore store = LedgerStore.open(world, ledger);
        ledger.credit("a", 7, Reasons.INCOME, "test");
        store.store().save();
        String text = Files.readString(world.resolve("ledger.json"), StandardCharsets.UTF_8);
        assertTrue(text.contains("\"schema_version\": 1"), text);
        assertTrue(text.contains("\"net_by_reason\""), text);
        assertTrue(text.contains("\"" + Reasons.INCOME + "\": 7"), text);
    }

    @Test
    void aFileWithoutTheOptionalFieldsStillLoads() throws IOException {
        Files.writeString(world.resolve("ledger.json"), "{\"schema_version\":1,\"data\":{}}", StandardCharsets.UTF_8);
        Ledger ledger = newLedger();
        LedgerStore store = LedgerStore.open(world, ledger);
        assertEquals(VersionedStore.Outcome.LOADED, store.outcome());
        assertEquals(0, ledger.netByReason().size());
        assertEquals(0L, ledger.refusedDebits());
    }

    @Test
    void aCorruptFileIsKeptAsideAndTheTotalsStartFromZero() throws IOException {
        Files.writeString(world.resolve("ledger.json"), "{ not json", StandardCharsets.UTF_8);
        Ledger ledger = newLedger();
        LedgerStore store = LedgerStore.open(world, ledger);
        assertEquals(VersionedStore.Outcome.RECOVERED, store.outcome());
        assertEquals(0, ledger.netByReason().size());
        assertTrue(store.store().lastCorruptCopy() != null && Files.exists(store.store().lastCorruptCopy()));
    }

    @Test
    void theRegistryFlushesTheLedgerWithTheOtherStores() {
        Ledger ledger = newLedger();
        StoreRegistry registry = new StoreRegistry();
        LedgerStore.open(world, ledger).register(registry);
        ledger.credit("a", 5, Reasons.INCOME, "test");
        assertEquals(1, registry.saveDirty());
        assertTrue(Files.exists(world.resolve("ledger.json")));
        assertEquals(0, registry.saveDirty(), "nothing changed since");
    }
}
