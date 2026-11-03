package com.political.core.persist;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Covers the persistence layer with a small made-up store (a pouch of items and coins) that has gone
 * through two schema changes, using fixture files of every version in {@code src/test/resources/persist}.
 */
class VersionedStoreTest {

    record Entry(String id, int count) {
        static final Codec<Entry> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.fieldOf("id").forGetter(Entry::id),
                Codec.INT.fieldOf("count").forGetter(Entry::count)).apply(i, Entry::new));
    }

    record Pouch(List<Entry> items, int coins) {
        static final Codec<Pouch> CODEC = RecordCodecBuilder.create(i -> i.group(
                Entry.CODEC.listOf().fieldOf("items").forGetter(Pouch::items),
                Codec.INT.fieldOf("coins").forGetter(Pouch::coins)).apply(i, Pouch::new));
    }

    private static final Pouch EXPECTED = new Pouch(List.of(new Entry("sword", 1), new Entry("apple", 1)), 12);
    private static final long FIXED_WALL_CLOCK = 1_700_000_000_000L; // 2023-11-14 22:13:20 UTC

    @TempDir
    Path dir;

    /** Version 0: {"inventory": [...], "gold": n}  ->  version 1: {"items": [...], "coins": n}. */
    private static JsonElement v0ToV1(JsonElement legacy) {
        JsonObject old = legacy.getAsJsonObject();
        JsonObject out = new JsonObject();
        out.add("items", old.get("inventory"));
        out.add("coins", old.get("gold"));
        return out;
    }

    /** Version 1 items were bare ids  ->  version 2 items are {"id", "count"}. */
    private static JsonElement v1ToV2(JsonElement v1) {
        JsonObject old = v1.getAsJsonObject();
        JsonArray items = new JsonArray();
        for (JsonElement id : old.getAsJsonArray("items")) {
            JsonObject entry = new JsonObject();
            entry.add("id", id);
            entry.addProperty("count", 1);
            items.add(entry);
        }
        JsonObject out = new JsonObject();
        out.add("items", items);
        out.add("coins", old.get("coins"));
        return out;
    }

    private VersionedStore<Pouch> pouchStore(Path file) {
        return pouchStore("pouch", file);
    }

    private VersionedStore<Pouch> pouchStore(String name, Path file) {
        return VersionedStore.builder(name, file, 2, Pouch.CODEC, () -> new Pouch(List.of(), 0))
                .migration(0, VersionedStoreTest::v0ToV1)
                .migration(1, VersionedStoreTest::v1ToV2)
                .wallClock(() -> FIXED_WALL_CLOCK)
                .build();
    }

    private Path fixture(String name) throws IOException {
        Path target = dir.resolve("pouch.json");
        try (InputStream in = VersionedStoreTest.class.getResourceAsStream("/persist/" + name)) {
            assertNotNull(in, "missing fixture " + name);
            Files.write(target, in.readAllBytes());
        }
        return target;
    }

    private List<String> filesInDir() throws IOException {
        try (Stream<Path> list = Files.list(dir)) {
            return list.map(p -> p.getFileName().toString()).sorted().toList();
        }
    }

    @Test
    void noFileMeansDefaultsAndNothingIsWrittenYet() throws IOException {
        VersionedStore<Pouch> store = pouchStore(dir.resolve("pouch.json"));
        assertEquals(VersionedStore.Outcome.FRESH, store.load());
        assertEquals(new Pouch(List.of(), 0), store.get());
        assertFalse(store.isDirty());
        assertEquals(List.of(), filesInDir());
    }

    @Test
    void saveThenLoadRoundTripsAndWritesTheCurrentVersion() throws IOException {
        Path file = dir.resolve("pouch.json");
        VersionedStore<Pouch> store = pouchStore(file);
        store.load();
        store.set(EXPECTED);
        assertTrue(store.isDirty());
        assertTrue(store.save());
        assertFalse(store.isDirty());
        assertEquals(List.of("pouch.json"), filesInDir(), "no temporary file is left behind");

        JsonObject written = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
        assertEquals(2, written.get(VersionedStore.VERSION_KEY).getAsInt());

        VersionedStore<Pouch> reloaded = pouchStore(file);
        assertEquals(VersionedStore.Outcome.LOADED, reloaded.load());
        assertEquals(EXPECTED, reloaded.get());
    }

    @Test
    void savingIsOnlyDoneWhenDirty() throws IOException {
        VersionedStore<Pouch> store = pouchStore(dir.resolve("pouch.json"));
        store.load();
        assertFalse(store.saveIfDirty(), "a freshly loaded store has nothing to write");
        store.update(p -> new Pouch(p.items(), p.coins() + 1));
        assertTrue(store.saveIfDirty());
        assertFalse(store.saveIfDirty(), "clean again after the save");
        assertEquals(1, store.saveCount());
    }

    @Test
    void currentVersionFixtureLoadsUnchanged() throws IOException {
        Path file = fixture("pouch-v2.json");
        String before = Files.readString(file);
        VersionedStore<Pouch> store = pouchStore(file);
        assertEquals(VersionedStore.Outcome.LOADED, store.load());
        assertEquals(EXPECTED, store.get());
        assertFalse(store.isDirty());
        assertEquals(before, Files.readString(file));
    }

    @Test
    void version1FixtureMigratesToCurrent() throws IOException {
        Path file = fixture("pouch-v1.json");
        VersionedStore<Pouch> store = pouchStore(file);
        assertEquals(VersionedStore.Outcome.MIGRATED, store.load());
        assertEquals(EXPECTED, store.get());
        assertTrue(store.isDirty(), "the upgraded file is written by the next flush");
    }

    @Test
    void legacyFileWithoutVersionMigratesThroughEveryStep() throws IOException {
        Path file = fixture("pouch-v0.json");
        String original = Files.readString(file);
        VersionedStore<Pouch> store = pouchStore(file);
        assertEquals(VersionedStore.Outcome.MIGRATED, store.load());
        assertEquals(EXPECTED, store.get());
        assertEquals(original, Files.readString(file), "loading alone does not touch the old file");
        assertEquals(original, Files.readString(dir.resolve("pouch.json.v0.bak")), "the old file is kept before any upgrade");

        assertTrue(store.saveIfDirty());
        JsonObject upgraded = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
        assertEquals(2, upgraded.get(VersionedStore.VERSION_KEY).getAsInt());
        assertEquals(original, Files.readString(dir.resolve("pouch.json.v0.bak")), "the backup is not overwritten by the upgrade");
        VersionedStore<Pouch> again = pouchStore(file);
        assertEquals(VersionedStore.Outcome.LOADED, again.load());
        assertEquals(EXPECTED, again.get());
    }

    @Test
    void unreadableJsonIsCopiedAsideAndDefaultsLoad() throws IOException {
        Path file = fixture("pouch-corrupt.json");
        String original = Files.readString(file);
        VersionedStore<Pouch> store = pouchStore(file);
        assertEquals(VersionedStore.Outcome.RECOVERED, store.load());
        assertEquals(new Pouch(List.of(), 0), store.get());
        Path copy = store.lastCorruptCopy();
        assertNotNull(copy);
        assertEquals("pouch.json.corrupt-20231114-221320", copy.getFileName().toString());
        assertEquals(original, Files.readString(copy));
        assertEquals(original, Files.readString(file), "the original is still there until something is saved");
        assertFalse(store.isDirty());
    }

    @Test
    void dataTheCodecRejectsIsTreatedAsCorrupt() throws IOException {
        Path file = fixture("pouch-wrong-shape.json");
        String original = Files.readString(file);
        VersionedStore<Pouch> store = pouchStore(file);
        assertEquals(VersionedStore.Outcome.RECOVERED, store.load());
        assertEquals(new Pouch(List.of(), 0), store.get());
        assertEquals(original, Files.readString(store.lastCorruptCopy()));
    }

    @Test
    void aFailingMigrationIsTreatedAsCorrupt() throws IOException {
        Path file = fixture("pouch-v1.json");
        VersionedStore<Pouch> store = VersionedStore.builder("pouch", file, 2, Pouch.CODEC, () -> new Pouch(List.of(), 0))
                .migration(0, VersionedStoreTest::v0ToV1)
                .migration(1, data -> {
                    throw new IllegalStateException("cannot upgrade this one");
                })
                .wallClock(() -> FIXED_WALL_CLOCK)
                .build();
        assertEquals(VersionedStore.Outcome.RECOVERED, store.load());
        assertNotNull(store.lastCorruptCopy());
        assertTrue(Files.exists(store.lastCorruptCopy()));
    }

    @Test
    void aFileFromANewerBuildIsNeverOverwritten() throws IOException {
        Path file = fixture("pouch-too-new.json");
        String original = Files.readString(file);
        VersionedStore<Pouch> store = pouchStore(file);
        assertEquals(VersionedStore.Outcome.TOO_NEW, store.load());
        assertTrue(store.isReadOnly());
        store.set(EXPECTED);
        assertFalse(store.save(), "an old build must not write over a newer file");
        assertEquals(original, Files.readString(file));
        assertNull(store.lastCorruptCopy());
    }

    @Test
    void keepBackupKeepsThePreviousFile() throws IOException {
        Path file = dir.resolve("pouch.json");
        VersionedStore<Pouch> store = VersionedStore.builder("pouch", file, 2, Pouch.CODEC, () -> new Pouch(List.of(), 0))
                .migration(0, VersionedStoreTest::v0ToV1)
                .migration(1, VersionedStoreTest::v1ToV2)
                .keepBackup()
                .build();
        store.load();
        store.set(new Pouch(List.of(new Entry("first", 1)), 1));
        store.save();
        String first = Files.readString(file);
        store.set(new Pouch(List.of(new Entry("second", 2)), 2));
        store.save();
        assertEquals(first, Files.readString(dir.resolve("pouch.json.bak")));
        assertTrue(Files.readString(file).contains("second"));
    }

    @Test
    void migrationStepsMustBeContiguous() {
        Path file = dir.resolve("pouch.json");
        IllegalStateException e = assertThrows(IllegalStateException.class, () ->
                VersionedStore.builder("pouch", file, 3, Pouch.CODEC, () -> new Pouch(List.of(), 0))
                        .migration(0, VersionedStoreTest::v0ToV1)
                        .migration(2, VersionedStoreTest::v1ToV2)
                        .build());
        assertTrue(e.getMessage().contains("from version 1"), e.getMessage());
    }

    @Test
    void usingAStoreBeforeLoadIsAnError() {
        VersionedStore<Pouch> store = pouchStore(dir.resolve("pouch.json"));
        assertThrows(IllegalStateException.class, store::get);
    }

    @Test
    void registrySavesOnlyDirtyStoresAndReportsFailuresAtShutdown() throws IOException {
        StoreRegistry registry = new StoreRegistry();
        VersionedStore<Pouch> a = pouchStore("a", dir.resolve("a.json"));
        VersionedStore<Pouch> b = pouchStore("b", dir.resolve("b.json"));
        registry.register(a);
        registry.register(b);
        assertThrows(IllegalArgumentException.class, () -> registry.register(pouchStore("a", dir.resolve("other.json"))),
                "two stores called a");
        a.load();
        b.load();
        a.set(EXPECTED);
        assertEquals(1, registry.saveDirty());
        assertEquals(0, registry.saveDirty());
        assertEquals(List.of("a.json"), filesInDir());

        // A store that cannot write (its parent path is a file) is reported, not thrown.
        Path blocker = dir.resolve("blocker");
        Files.writeString(blocker, "not a directory");
        StoreRegistry shutdown = new StoreRegistry();
        VersionedStore<Pouch> stuck = VersionedStore.builder("stuck", blocker.resolve("x.json"), 2, Pouch.CODEC,
                () -> new Pouch(List.of(), 0)).build();
        stuck.load();
        stuck.set(EXPECTED);
        shutdown.register(stuck);
        assertEquals(List.of("stuck"), shutdown.saveAllOnShutdown());
        assertTrue(stuck.isDirty(), "still dirty so a later flush retries");
    }
}
