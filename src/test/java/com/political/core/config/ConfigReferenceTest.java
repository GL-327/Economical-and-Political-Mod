package com.political.core.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/**
 * docs/CONFIG.md is generated from the declared settings. This test fails when the file is stale;
 * {@code ./gradlew test -Dpolitical.updateDocs=true} rewrites it.
 */
class ConfigReferenceTest {

    private static final Path DOC = Path.of("docs", "CONFIG.md");

    @Test
    void configReferenceMatchesTheCode() throws IOException {
        String expected = ConfigReference.markdown(ConfigCatalog.keys());
        if (Boolean.getBoolean("political.updateDocs")) {
            Files.createDirectories(DOC.getParent());
            Files.writeString(DOC, expected, StandardCharsets.UTF_8);
        }
        String actual = Files.exists(DOC) ? Files.readString(DOC, StandardCharsets.UTF_8).replace("\r\n", "\n") : "";
        assertEquals(expected, actual, "docs/CONFIG.md is out of date; run ./gradlew test -Dpolitical.updateDocs=true");
    }
}
