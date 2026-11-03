package com.political.core;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * Rule 4.10 of the modernization program: nothing that holds a local path or a user name is committed.
 * Test and dev runs write into the project folder, and "git add -A" once swept the log files up, so the
 * ignore list is pinned here.
 */
class RepoHygieneTest {

    private static final List<String> MUST_BE_IGNORED = List.of(
            "run/", "run-verify/", "build/", ".gradle/", ".claude/*.local.json", "logs/", "crash-reports/");

    @Test
    void gitignoreCoversTheFoldersRunsWriteInto() throws IOException {
        List<String> lines = Files.readAllLines(Path.of(".gitignore"), StandardCharsets.UTF_8).stream()
                .map(String::strip).collect(Collectors.toList());
        for (String entry : MUST_BE_IGNORED) {
            assertTrue(lines.contains(entry), ".gitignore must list " + entry);
        }
    }
}
