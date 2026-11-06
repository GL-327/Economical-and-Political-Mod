package com.political.power;

import com.political.power.store.PowerArchives;
import com.political.power.store.PowerStore;
import com.political.core.persist.VersionedStore;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class PowerArchivesTest {
    private static final String UUID = "aaaaaaaa-0000-0000-0000-000000000001";
    @TempDir Path world;

    @Test void successiveResetsAtTheSameTimeKeepEveryRecoveryRecord() {
        Map<String, String> records = new TreeMap<>();
        String first = PowerArchives.archiveReset(records, UUID, 1000, "original progression");
        String second = PowerArchives.archiveReset(records, UUID, 1000, "second progression");
        String third = PowerArchives.archiveReset(records, UUID, 1000, "third progression");
        assertEquals(UUID + ".reset.1000", first, "retain the existing key for the first reset");
        assertNotEquals(first, second);
        assertNotEquals(second, third);
        assertEquals(3, records.size());
        assertEquals("original progression", records.get(first));
        assertEquals("second progression", records.get(second));
        assertEquals("third progression", records.get(third));
    }

    @Test void collisionsWithPreviouslySavedSuffixesNeverOverwriteThem() {
        String prefix = UUID + ".reset.1000";
        Map<String, String> old = Map.of(prefix, "first", prefix + ".1", "older collision", "legacy-import", "owner value");
        Map<String, String> records = new TreeMap<>(old);
        String added = PowerArchives.archiveReset(records, UUID, 1000, "new reset");
        assertEquals(4, records.size());
        old.forEach((key, value) -> assertEquals(value, records.get(key)));
        assertEquals("new reset", records.get(added));
    }

    @Test void legacyAndSuffixedKeysRoundTripThroughTheUnchangedPowerSaveSchema() {
        PowerStore first = PowerStore.open(world);
        first.data().quarantine.put(UUID + ".reset.1000", "legacy recovery state");
        PowerArchives.archiveReset(first.data().quarantine, UUID, 1000, "new recovery state");
        Map<String, String> expected = Map.copyOf(first.data().quarantine);
        first.markDirty();
        assertTrue(first.saveIfDirty());

        PowerStore reloaded = PowerStore.open(world);
        assertEquals(VersionedStore.Outcome.LOADED, reloaded.outcome());
        assertEquals(expected, reloaded.data().quarantine);
        assertEquals(2, reloaded.data().quarantine.size());
        assertEquals(2, PowerStore.SCHEMA_VERSION);
    }
}
