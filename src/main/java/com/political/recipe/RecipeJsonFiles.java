package com.political.recipe;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

/** Writes the unchanged recipe JSON format atomically, retaining the pre-repair file once. */
public final class RecipeJsonFiles {
    private RecipeJsonFiles() {}
    public static Path backup(Path file) { return file.resolveSibling(file.getFileName() + ".before-m10-recipes.bak"); }
    public static void write(Path file, String json) throws IOException {
        if (Files.exists(file) && Files.readString(file).equals(json)) return;
        Files.createDirectories(file.toAbsolutePath().getParent());
        if (Files.exists(file) && !Files.exists(backup(file))) Files.copy(file, backup(file));
        Path temp = Files.createTempFile(file.toAbsolutePath().getParent(), "recipe-write-", ".tmp");
        try {
            try (FileChannel channel = FileChannel.open(temp, StandardOpenOption.WRITE)) {
                ByteBuffer bytes = StandardCharsets.UTF_8.encode(json);
                while (bytes.hasRemaining()) channel.write(bytes);
                channel.force(true);
            }
            try { Files.move(temp, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
            catch (AtomicMoveNotSupportedException e) { Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING); }
        } finally {
            Files.deleteIfExists(temp); // only this method's newly created temporary file
        }
    }
}
