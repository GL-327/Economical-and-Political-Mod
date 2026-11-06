package com.political.recipe;

import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class RecipeJsonFilesTest {
    @TempDir Path directory;
    @Test void keepsOriginalBackupAcrossSubsequentAtomicWrites() throws Exception {
        Path file = directory.resolve("custom_recipes.json");
        Files.writeString(file, "original owner data");
        RecipeJsonFiles.write(file, "first repair");
        RecipeJsonFiles.write(file, "second repair");
        assertEquals("original owner data", Files.readString(RecipeJsonFiles.backup(file)));
        assertEquals("second repair", Files.readString(file));
        try (var files = Files.list(directory)) { assertEquals(2, files.count()); }
    }
}
