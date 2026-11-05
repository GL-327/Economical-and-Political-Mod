package com.political.core.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ConfigStoreTest {

    // Paths start with "testcfg" so they never clash with a real setting.
    static final ConfigKey<Integer> COUNT = ConfigKey.integer("testcfg.limits.count", 10, 1, 100, "A whole number.");
    static final ConfigKey<Double> RATE = ConfigKey.decimal("testcfg.limits.rate", 0.5, 0.0, 1.0, "A fraction.");
    static final ConfigKey<Boolean> ENABLED = ConfigKey.bool("testcfg.feature.enabled", true, "A switch.");
    static final ConfigKey<String> MODE = ConfigKey.choice("testcfg.feature.mode", "fast", List.of("fast", "safe"), "A choice.");
    static final List<ConfigKey<?>> KEYS = List.of(COUNT, RATE, ENABLED, MODE);

    @TempDir
    Path dir;

    @AfterEach
    void restore() {
        Config.reset();
    }

    private ConfigStore loadText(String json) throws IOException {
        Path file = dir.resolve("config.json");
        Files.writeString(file, json);
        return ConfigStore.load(file, KEYS);
    }

    @Test
    void missingFileGivesDefaultsWithoutWarnings() {
        ConfigStore store = ConfigStore.load(dir.resolve("config.json"), KEYS);
        assertEquals(List.of(), store.warnings());
        assertEquals(null, store.valueOf(COUNT), "nothing was set, so callers fall back to the default");
    }

    @Test
    void validValuesAreUsed() throws IOException {
        ConfigStore store = loadText("""
                {"testcfg": {"limits": {"count": 42, "rate": 0.25}, "feature": {"enabled": false, "mode": "SAFE"}}}
                """);
        assertEquals(List.of(), store.warnings());
        assertEquals(42, store.valueOf(COUNT));
        assertEquals(0.25, (Double) store.valueOf(RATE), 1e-12);
        assertEquals(false, store.valueOf(ENABLED));
        assertEquals("safe", store.valueOf(MODE), "choices match without regard to case and come back in canonical form");
    }

    @Test
    void numbersOutsideTheirRangeAreClampedWithAWarning() throws IOException {
        ConfigStore store = loadText("""
                {"testcfg": {"limits": {"count": 5000, "rate": -3}}}
                """);
        assertEquals(100, store.valueOf(COUNT));
        assertEquals(0.0, (Double) store.valueOf(RATE), 1e-12);
        assertEquals(2, store.warnings().size());
        assertTrue(store.warnings().get(0).contains("testcfg.limits.count"), store.warnings().toString());
        assertTrue(store.warnings().get(0).contains("using 100"), store.warnings().toString());
    }

    @Test
    void wrongTypesFallBackToTheDefault() throws IOException {
        ConfigStore store = loadText("""
                {"testcfg": {"limits": {"count": "many", "rate": true}, "feature": {"enabled": "yes", "mode": "turbo"}}}
                """);
        assertEquals(10, store.valueOf(COUNT));
        assertEquals(0.5, (Double) store.valueOf(RATE), 1e-12);
        assertEquals(true, store.valueOf(ENABLED));
        assertEquals("fast", store.valueOf(MODE));
        assertEquals(4, store.warnings().size());
    }

    @Test
    void aFractionIsNotAWholeNumber() throws IOException {
        ConfigStore store = loadText("{\"testcfg\": {\"limits\": {\"count\": 7.5}}}");
        assertEquals(10, store.valueOf(COUNT));
        assertEquals(1, store.warnings().size());
    }

    @Test
    void unknownSettingsAreReportedButCommentsAreNot() throws IOException {
        ConfigStore store = loadText("""
                {"_comment": "free text", "testcfg": {"_notes": {"a": 1}, "limits": {"cuont": 5}}}
                """);
        assertEquals(1, store.warnings().size());
        assertTrue(store.warnings().get(0).contains("testcfg.limits.cuont"), store.warnings().toString());
    }

    @Test
    void brokenJsonNeverThrowsAndLeavesTheFileAlone() throws IOException {
        String broken = "{\"testcfg\": {\"limits\": {\"count\": ";
        Path file = dir.resolve("config.json");
        Files.writeString(file, broken);
        ConfigStore store = ConfigStore.load(file, KEYS);
        assertEquals(null, store.valueOf(COUNT));
        assertEquals(1, store.warnings().size());
        assertEquals(broken, Files.readString(file), "a broken file is not rewritten; the owner fixes it");
        assertFalse(store.writeDefaultsIfMissing(), "and defaults are not written over it");
    }

    @Test
    void aNonObjectRootGivesDefaults() throws IOException {
        ConfigStore store = loadText("[1, 2, 3]");
        assertEquals(null, store.valueOf(COUNT));
        assertEquals(1, store.warnings().size());
    }

    @Test
    void defaultsFileIsCreatedOnceAndReadsBackClean() throws IOException {
        Path file = dir.resolve("sub").resolve("config.json");
        ConfigStore first = ConfigStore.load(file, KEYS);
        assertTrue(first.writeDefaultsIfMissing());
        assertTrue(Files.exists(file));
        String written = Files.readString(file);
        assertTrue(written.contains("\"testcfg\""), written);

        ConfigStore second = ConfigStore.load(file, KEYS);
        assertEquals(List.of(), second.warnings());
        assertEquals(10, second.valueOf(COUNT));
        assertEquals("fast", second.valueOf(MODE));
        assertFalse(second.writeDefaultsIfMissing(), "an existing file is never overwritten");
        assertEquals(written, Files.readString(file));
    }

    @Test
    void keysReadTheActiveStoreAndFallBackToTheirDefault() throws IOException {
        assertEquals(10, COUNT.get());
        Config.useForTests(loadText("{\"testcfg\": {\"limits\": {\"count\": 33}}}"));
        assertEquals(33, COUNT.get());
        assertEquals(0.5, RATE.get(), 1e-12, "unset keys keep their default");
    }

    @Test
    void keyDeclarationsAreValidated() {
        assertThrows(IllegalArgumentException.class, () -> ConfigKey.integer("Bad.Path", 1, 0, 2, "x"));
        assertThrows(IllegalArgumentException.class, () -> ConfigKey.integer("single", 1, 0, 2, "x"));
        assertThrows(IllegalArgumentException.class, () -> ConfigKey.integer("testcfg.a.out_of_range", 5, 0, 2, "x"));
        assertThrows(IllegalArgumentException.class, () -> ConfigKey.integer("testcfg.a.no_description", 1, 0, 2, " "));
        assertThrows(IllegalArgumentException.class, () -> ConfigKey.choice("testcfg.a.bad_choice", "z", List.of("a", "b"), "x"));
        ConfigKey.bool("testcfg.a.once", false, "declared once");
        assertThrows(IllegalStateException.class, () -> ConfigKey.bool("testcfg.a.once", false, "declared twice"));
    }

    @Test
    void everyRealSettingIsWellFormed() {
        List<ConfigKey<?>> real = ConfigCatalog.keys();
        assertFalse(real.isEmpty());
        long distinct = real.stream().map(ConfigKey::path).distinct().count();
        assertEquals(real.size(), distinct);
        for (ConfigKey<?> key : real) {
            assertTrue(key.description().length() >= 20, key.path() + " needs a real description");
        }
    }
}
