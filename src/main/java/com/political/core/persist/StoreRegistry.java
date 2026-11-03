package com.political.core.persist;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The stores the mod keeps, so one tick service can flush the dirty ones on a timer and the shutdown
 * hook can flush whatever is left. A store that is not registered here is the owner's to save.
 */
public final class StoreRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(StoreRegistry.class);
    private static final StoreRegistry INSTANCE = new StoreRegistry();

    private final Map<String, VersionedStore<?>> stores = new LinkedHashMap<>();

    /** The registry the server uses. Tests make their own with {@code new StoreRegistry()}. */
    public static StoreRegistry global() {
        return INSTANCE;
    }

    public StoreRegistry() {
    }

    public void register(VersionedStore<?> store) {
        if (stores.putIfAbsent(store.name(), store) != null) {
            throw new IllegalArgumentException("two stores are called " + store.name());
        }
    }

    public int size() {
        return stores.size();
    }

    /** Saves every dirty store; returns how many files were written. Used by the periodic flush. */
    public int saveDirty() {
        int written = 0;
        for (VersionedStore<?> store : new ArrayList<>(stores.values())) {
            if (store.saveIfDirty()) {
                written++;
            }
        }
        return written;
    }

    /** Final flush at server stop: saves what is dirty and returns the names of stores that failed. */
    public List<String> saveAllOnShutdown() {
        List<String> failed = new ArrayList<>();
        for (VersionedStore<?> store : new ArrayList<>(stores.values())) {
            if (store.isDirty() && !store.saveIfDirty()) {
                failed.add(store.name());
            }
        }
        if (!failed.isEmpty()) {
            LOGGER.error("These stores could not be saved at shutdown: {}", failed);
        }
        return failed;
    }
}
