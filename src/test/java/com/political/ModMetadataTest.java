package com.political;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Guards the parts of fabric.mod.json that other tooling and the server-only rule depend on. */
class ModMetadataTest {

    private static JsonObject metadata;

    @BeforeAll
    static void load() throws IOException {
        try (InputStream in = ModMetadataTest.class.getResourceAsStream("/fabric.mod.json")) {
            assertNotNull(in, "fabric.mod.json is missing from the main resources");
            metadata = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
        }
    }

    @Test
    void modIdIsPoliticalserver() {
        assertEquals("politicalserver", metadata.get("id").getAsString());
    }

    @Test
    void environmentIsServerOnly() {
        assertEquals("server", metadata.get("environment").getAsString());
    }

    @Test
    void hasNoClientEntrypoint() {
        JsonObject entrypoints = metadata.getAsJsonObject("entrypoints");
        assertNotNull(entrypoints, "entrypoints block is missing");
        assertTrue(!entrypoints.has("client"), "a client entrypoint would break the server-only rule");
        JsonArray server = entrypoints.getAsJsonArray("server");
        assertEquals(1, server.size());
        assertEquals("com.political.PoliticalServer", server.get(0).getAsString());
    }

    @Test
    void licenseFieldMatchesLicenseFile() throws IOException {
        // The LICENSE file is the CC0 1.0 legal text, so the metadata must say the same.
        assertEquals("CC0-1.0", metadata.get("license").getAsString());
        String text = java.nio.file.Files.readString(java.nio.file.Path.of("LICENSE"), StandardCharsets.UTF_8);
        assertTrue(text.contains("CC0 1.0 Universal"), "LICENSE is no longer the CC0 text");
    }

    @Test
    void authorIsNotThePlaceholder() {
        JsonArray authors = metadata.getAsJsonArray("authors");
        assertEquals(1, authors.size());
        assertTrue(!authors.get(0).getAsString().equals("You"), "authors still holds the template placeholder");
    }
}
