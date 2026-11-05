package com.political.core.economy;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.political.core.persist.StoreRegistry;
import com.political.core.persist.VersionedStore;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Keeps the coin ledger's running totals (net coins made or destroyed per reason, refused debits) in {@code ledger.json} in the world
 * root, through a {@link VersionedStore} (schema version, atomic write, corrupt file kept aside), so {@code /ledger} shows what the
 * server has made and destroyed over its life and not only since the last restart. Balances are not here: they live where they always
 * did. The journal of recent transactions is not saved; it is a per-run diagnostic. The file is written by the periodic flush and at
 * shutdown; a coin change only marks the store dirty, and the live totals are copied in just before a write.
 */
public final class LedgerStore {

    public static final int SCHEMA_VERSION = 1;
    private static final Logger LOGGER = LoggerFactory.getLogger(LedgerStore.class);

    /** The saved shape. */
    public record Data(Map<String, Long> netByReason, long refusedDebits) {
        public static final Codec<Data> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.unboundedMap(Codec.STRING, Codec.LONG).optionalFieldOf("net_by_reason", Map.of()).forGetter(Data::netByReason),
                Codec.LONG.optionalFieldOf("refused_debits", 0L).forGetter(Data::refusedDebits)).apply(i, Data::new));

        static Data empty() {
            return new Data(new TreeMap<>(), 0L);
        }
    }

    private final VersionedStore<Data> store;
    private final VersionedStore.Outcome outcome;

    private LedgerStore(VersionedStore<Data> store, VersionedStore.Outcome outcome) {
        this.store = store;
        this.outcome = outcome;
    }

    /** Opens {@code ledger.json} under {@code worldRoot}, restores the totals into {@code ledger} and keeps the file current from then on. */
    public static LedgerStore open(Path worldRoot, Ledger ledger) {
        @SuppressWarnings("unchecked")
        VersionedStore<Data>[] self = new VersionedStore[1];
        self[0] = VersionedStore
                .builder("ledger", worldRoot.resolve("ledger.json"), SCHEMA_VERSION, Data.CODEC, Data::empty)
                .beforeSave(() -> {
                    Ledger.Totals totals = ledger.totals();
                    self[0].set(new Data(totals.netByReason(), totals.refusedDebits()));
                })
                .build();
        VersionedStore<Data> store = self[0];
        VersionedStore.Outcome outcome = store.load();
        if (outcome == VersionedStore.Outcome.RECOVERED) {
            LOGGER.error("ledger.json could not be read; the coin totals start from zero. The unreadable file was kept next to it as a .corrupt copy.");
        }
        Data saved = store.get();
        ledger.restoreTotals(new Ledger.Totals(saved.netByReason(), saved.refusedDebits()));
        ledger.onChange(store::markDirty);
        return new LedgerStore(store, outcome);
    }

    /** Makes the periodic flush and the shutdown hook save this store. */
    public LedgerStore register(StoreRegistry registry) {
        registry.register(store);
        return this;
    }

    public VersionedStore<Data> store() {
        return store;
    }

    public VersionedStore.Outcome outcome() {
        return outcome;
    }
}
