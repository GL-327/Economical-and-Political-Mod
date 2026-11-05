package com.political.core.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Every {@link ConfigKey} that has been declared, by path. Keys add themselves when they are created. */
final class ConfigRegistry {

    private static final Map<String, ConfigKey<?>> KEYS = Collections.synchronizedMap(new TreeMap<>());

    private ConfigRegistry() {
    }

    static void register(ConfigKey<?> key) {
        ConfigKey<?> previous = KEYS.putIfAbsent(key.path(), key);
        if (previous != null) {
            throw new IllegalStateException("config key declared twice: " + key.path());
        }
    }

    /** All declared keys, sorted by path. */
    static List<ConfigKey<?>> all() {
        synchronized (KEYS) {
            return new ArrayList<>(KEYS.values());
        }
    }

    static Collection<String> paths() {
        synchronized (KEYS) {
            return new ArrayList<>(KEYS.keySet());
        }
    }

    static ConfigKey<?> find(String path) {
        return KEYS.get(path);
    }
}
