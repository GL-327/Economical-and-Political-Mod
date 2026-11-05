package com.political.core.config;

import java.nio.file.Path;

/**
 * The settings the running server uses. {@link #load(Path)} is called once at startup and again to
 * reload; until then every key answers with its default, so code that reads a setting early still works.
 */
public final class Config {

    private static volatile ConfigStore active = ConfigStore.defaults(ConfigCatalog.keys());

    private Config() {
    }

    static ConfigStore active() {
        return active;
    }

    /** Reads {@code file}, creates it with the defaults if it is missing, and makes it the active settings. */
    public static ConfigStore load(Path file) {
        ConfigStore loaded = ConfigStore.load(file, ConfigCatalog.keys());
        loaded.writeDefaultsIfMissing();
        active = loaded;
        return loaded;
    }

    /** Test hook: use these settings until {@link #reset()}. */
    public static void useForTests(ConfigStore store) {
        active = store;
    }

    /** Test hook: back to all defaults. */
    public static void reset() {
        active = ConfigStore.defaults(ConfigCatalog.keys());
    }
}
