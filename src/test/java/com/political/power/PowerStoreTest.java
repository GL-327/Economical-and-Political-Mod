package com.political.power;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.political.core.persist.StoreRegistry;
import com.political.core.persist.VersionedStore;
import com.political.power.model.PlayerPower;
import com.political.power.model.PowerData;
import com.political.power.store.LegacyImport;
import com.political.power.store.PowerStore;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * The migration of the three old files into power.json, with fixtures in the shapes the old code wrote
 * (src/test/resources/power), and the store around it.
 */
class PowerStoreTest {

    private static final String P1 = "aaaaaaaa-0000-0000-0000-000000000001"; // techniques + magic level, no tree nodes
    private static final String P2 = "aaaaaaaa-0000-0000-0000-000000000002"; // magic + tree nodes in two schools, bogus node
    private static final String P3 = "aaaaaaaa-0000-0000-0000-000000000003"; // awakening-era single technique field
    private static final String P4 = "aaaaaaaa-0000-0000-0000-000000000004"; // unknown technique, invalid selected ability

    @TempDir
    Path world;

    private static String fixture(String name) throws IOException {
        try (InputStream in = PowerStoreTest.class.getResourceAsStream("/power/" + name)) {
            assertNotNull(in, "missing fixture " + name);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static JsonObject json(String name) throws IOException {
        return JsonParser.parseString(fixture(name)).getAsJsonObject();
    }

    private PowerData imported() throws IOException {
        return LegacyImport.run(json("legacy-techniques.json"), json("legacy-magic_data.json"), json("legacy-magic_schools.json")).data();
    }

    // ── the import ──────────────────────────────────────────────────────────────────────────

    @Test
    void techniquesAndMagicLevelMergeForOnePlayer() throws IOException {
        PlayerPower p = imported().players.get(P1);
        assertEquals(Set.of("EMBERHEART", "GLACIER_SOUL"), p.learned);
        assertEquals("EMBERHEART", p.equipped);
        assertEquals("cinder_bloom", p.selectedAbility);
        assertEquals(50, p.uses.get("EMBERHEART"));
        assertEquals(3, p.uses.get("GLACIER_SOUL"));
        assertEquals(3, p.level);
        assertEquals(300, p.xp);
        assertEquals(72.5, p.resource, 1e-9, "technique energy wins over the never-persisted mana of the same player");
        assertNull(p.school, "no tree nodes, no school");
    }

    @Test
    void treeNodesShardsAndTheSchoolChoice() throws IOException {
        PowerData data = imported();
        PlayerPower p = data.players.get(P2);
        assertEquals(3, p.shards);
        assertEquals(40, p.shardsEarnedTotal);
        assertEquals(Set.of("gra1", "gra2", "gra3", "pyr1"), p.unlockedNodes, "valid nodes of every school are kept");
        assertEquals("GRAVITAS", p.school, "the school with the most unlocked nodes becomes the active school");
        assertEquals(12, p.level);
        assertEquals(2400, p.xp);
        assertEquals(55.0, p.resource, 1e-9, "no technique file entry, so the old mana is used");
    }

    @Test
    void anUnknownTreeNodeIsQuarantinedNotDropped() throws IOException {
        PowerData data = imported();
        assertEquals("[\"bogus9\"]", data.quarantine.get(P2 + ".unlocked"));
    }

    @Test
    void spellsAndSkillTreeUnlocksAreQuarantined() throws IOException {
        PowerData data = imported();
        assertEquals("[\"FIREBALL\",\"ICE_SPIKE\"]", data.quarantine.get(P2 + ".unlockedSpells"));
        assertEquals("\"FIREBALL\"", data.quarantine.get(P2 + ".activeSpell"));
        assertEquals("[\"n1\",\"n2\"]", data.quarantine.get(P2 + ".skillTreeUnlocks"));
        assertFalse(data.quarantine.containsKey(P2 + ".activeCommission"), "null and zero values are nothing worth keeping");
        assertFalse(data.quarantine.containsKey(P2 + ".commissionProgress"));
        assertFalse(data.quarantine.containsKey(P1 + ".unlockedSpells"), "an empty list is nothing worth keeping");
    }

    @Test
    void theAwakeningEraTechniqueFieldAndAbilityCountsAreMigratedLikeTheOldCodeDid() throws IOException {
        PlayerPower p = imported().players.get(P3);
        assertEquals("STORM_VEIN", p.equipped);
        assertEquals(Set.of("STORM_VEIN"), p.learned);
        assertEquals(6, p.uses.get("STORM_VEIN"), "4 + 2 ability uses are summed into the equipped technique");
        assertEquals(100.0, p.resource, 1e-9);
    }

    @Test
    void unknownTechniquesAndInvalidSelectionsAreQuarantined() throws IOException {
        PowerData data = imported();
        PlayerPower p = data.players.get(P4);
        assertEquals(Set.of("UMBRAL_STEP"), p.learned);
        assertNull(p.equipped, "the equipped technique was unknown");
        assertNull(p.selectedAbility);
        assertEquals(1, p.uses.get("UMBRAL_STEP"));
        assertFalse(p.uses.containsKey("SHADOW_CLAN"));
        assertEquals("[\"SHADOW_CLAN\"]", data.quarantine.get(P4 + ".learned"));
        assertEquals("\"SHADOW_CLAN\"", data.quarantine.get(P4 + ".equipped"));
        assertEquals("\"bogus_ability\"", data.quarantine.get(P4 + ".selectedAbility"));
        assertEquals("{\"SHADOW_CLAN\":9}", data.quarantine.get(P4 + ".usesByTechnique"));
    }

    @Test
    void theReportCountsWhatHappened() throws IOException {
        LegacyImport.Result result = LegacyImport.run(json("legacy-techniques.json"), json("legacy-magic_data.json"), json("legacy-magic_schools.json"));
        String last = result.report().get(result.report().size() - 1);
        assertTrue(last.startsWith("imported 4 players, quarantined 8 old value(s), left behind 2 timing value(s)"), last);
    }

    @Test
    void missingFilesAreFine() throws IOException {
        PowerData onlyTechniques = LegacyImport.run(json("legacy-techniques.json"), null, null).data();
        assertEquals(3, onlyTechniques.players.size());
        PowerData nothing = LegacyImport.run(null, null, null).data();
        assertTrue(nothing.players.isEmpty());
    }

    // ── the store ───────────────────────────────────────────────────────────────────────────

    private void copyFixtures() throws IOException {
        Files.writeString(world.resolve("techniques.json"), fixture("legacy-techniques.json"));
        Files.writeString(world.resolve("magic_data.json"), fixture("legacy-magic_data.json"));
        Files.writeString(world.resolve("magic_schools.json"), fixture("legacy-magic_schools.json"));
    }

    @Test
    void firstOpenImportsThenSavesPowerJsonAndKeepsTheOldFiles() throws IOException {
        copyFixtures();
        String originalTechniques = Files.readString(world.resolve("techniques.json"));

        PowerStore store = PowerStore.open(world);
        assertEquals(VersionedStore.Outcome.FRESH, store.outcome());
        assertEquals(4, store.data().players.size());
        assertTrue(store.isDirty(), "power.json is written by the next save");
        assertFalse(Files.exists(world.resolve("power.json")));
        assertFalse(store.importReport().isEmpty());

        // nothing about the old files changed, and each has a copy aside
        assertEquals(originalTechniques, Files.readString(world.resolve("techniques.json")));
        assertEquals(originalTechniques, Files.readString(world.resolve("techniques.json.pre-power")));
        assertTrue(Files.exists(world.resolve("magic_data.json.pre-power")));
        assertTrue(Files.exists(world.resolve("magic_schools.json.pre-power")));

        assertTrue(store.saveIfDirty());
        assertTrue(Files.exists(world.resolve("power.json")));
    }

    @Test
    void secondOpenReadsPowerJsonAndDoesNotImportAgain() throws IOException {
        copyFixtures();
        PowerStore first = PowerStore.open(world);
        first.saveIfDirty();
        PowerData saved = first.data();

        PowerStore second = PowerStore.open(world);
        assertEquals(VersionedStore.Outcome.LOADED, second.outcome());
        assertTrue(second.importReport().isEmpty());
        assertFalse(second.isDirty());
        assertEquals(saved.players.keySet(), second.data().players.keySet());
        for (String uuid : saved.players.keySet()) {
            assertTrue(saved.players.get(uuid).sameAs(second.data().players.get(uuid)), "player " + uuid + " round-trips");
        }
        assertEquals(saved.quarantine, second.data().quarantine);
    }

    @Test
    void editingPowerJsonLaterIsNotUndoneByTheOldFiles() throws IOException {
        copyFixtures();
        PowerStore first = PowerStore.open(world);
        first.data().player(P1).shards = 99;
        first.markDirty();
        first.saveIfDirty();
        PowerStore second = PowerStore.open(world);
        assertEquals(99, second.data().players.get(P1).shards, "the old file says 0, power.json wins");
    }

    @Test
    void anUnreadableOldFileIsLeftInPlaceAndReported() throws IOException {
        Files.writeString(world.resolve("techniques.json"), "{ not json");
        PowerStore store = PowerStore.open(world);
        assertTrue(store.importReport().stream().anyMatch(l -> l.startsWith("techniques.json could not be read")), store.importReport().toString());
        assertEquals("{ not json", Files.readString(world.resolve("techniques.json")));
    }

    @Test
    void noOldFilesMeansAnEmptyStoreAndNothingWritten() throws IOException {
        PowerStore store = PowerStore.open(world);
        assertEquals(VersionedStore.Outcome.FRESH, store.outcome());
        assertTrue(store.data().players.isEmpty());
        assertFalse(store.isDirty());
    }

    @Test
    void aCorruptPowerJsonIsCopiedAsideAndTheOldFilesAreNotReimported() throws IOException {
        copyFixtures();
        Files.writeString(world.resolve("power.json"), "{\"schema_version\": 1, \"data\": {\"players\": ");
        PowerStore store = PowerStore.open(world);
        assertEquals(VersionedStore.Outcome.RECOVERED, store.outcome());
        assertTrue(store.data().players.isEmpty(), "stale old data must not replace what may have been newer");
        long copies;
        try (var list = Files.list(world)) {
            copies = list.filter(p -> p.getFileName().toString().startsWith("power.json.corrupt-")).count();
        }
        assertEquals(1, copies);
    }

    @Test
    void theStoreCanBeRegisteredForTheFlush() throws IOException {
        PowerStore store = PowerStore.open(world);
        StoreRegistry registry = new StoreRegistry();
        store.register(registry);
        store.data().player(P1).shards = 5;
        store.markDirty();
        assertEquals(1, registry.saveDirty());
        assertTrue(Files.exists(world.resolve("power.json")));
    }

    // ── schema 2: the innate technique ──────────────────────────────────────────────────────

    private static final String OLD = "aaaaaaaa-0000-0000-0000-0000000000a1";

    @Test
    void aSchemaOneFileOpensWithNothingLostAndNoInnateTechniqueYet() throws IOException {
        Files.writeString(world.resolve("power.json"), fixture("power-v1.json"), StandardCharsets.UTF_8);
        PowerStore store = PowerStore.open(world);
        PlayerPower p = store.data().players.get(OLD);
        assertNotNull(p);
        assertEquals("RADIANCE", p.school);
        assertEquals(12, p.shards);
        assertEquals(60, p.shardsEarnedTotal);
        assertEquals(Set.of("rad1", "rad2", "rad3", "rad4"), p.unlockedNodes);
        assertEquals(Set.of("BLUE"), p.learned);
        assertEquals("REVERSE", p.equipped);
        assertEquals("reverse", p.selectedAbility);
        assertEquals(7, p.uses.get("REVERSE"));
        assertEquals(88.5, p.resource, 1e-9);
        assertEquals(Set.of("NO_SPRINT"), p.vows);
        assertEquals(120L, p.vowWaitTicks);
        assertNull(p.innate, "the draw or the legacy choice happens at the next join");
        assertFalse(p.awakened);
        assertEquals(0, p.rerolls);
        assertTrue(store.data().quarantine.containsKey(OLD + ".spells"), "quarantined data stays");
    }

    @Test
    void theFirstSaveWritesSchemaTwoKeepsTheOldFileAsABackupAndRoundTripsTheNewFields() throws IOException {
        Files.writeString(world.resolve("power.json"), fixture("power-v1.json"), StandardCharsets.UTF_8);
        PowerStore store = PowerStore.open(world);
        PlayerPower p = store.data().players.get(OLD);
        p.innate = "REVERSE";
        p.awakened = true;
        p.rerolls = 2;
        p.rerollCharges = 1;
        store.markDirty();
        store.saveIfDirty();

        String written = Files.readString(world.resolve("power.json"), StandardCharsets.UTF_8);
        assertTrue(written.replaceAll("\s", "").contains("\"schema_version\":2"), written);
        assertTrue(Files.exists(world.resolve("power.json.v1.bak")), "the version 1 file is kept");

        PlayerPower again = PowerStore.open(world).data().players.get(OLD);
        assertTrue(p.sameAs(again));
        assertEquals("REVERSE", again.innate);
        assertTrue(again.awakened);
        assertEquals(2, again.rerolls);
        assertEquals(1, again.rerollCharges);
        assertEquals("RADIANCE", again.school, "the old school is kept even though the game no longer reads it");
    }
}
