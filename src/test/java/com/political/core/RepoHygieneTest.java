package com.political.core;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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

    /**
     * The mistake this pins: the "config/" ignore line (meant for the runtime config folder) also matched the mod's own
     * {@code core/config} source package, so ten source files were never committed and a clean checkout did not
     * compile. Any folder under {@code src/} that a bare folder rule of the ignore list would match must be
     * re-included by a "!src/..." line.
     */
    @Test
    void noSourceFolderIsHiddenByAFolderRule() throws IOException {
        List<String> lines = Files.readAllLines(Path.of(".gitignore"), StandardCharsets.UTF_8).stream()
                .map(String::strip).filter(line -> !line.isEmpty() && !line.startsWith("#")).collect(Collectors.toList());
        Set<String> bareFolderRules = new HashSet<>();
        List<PathMatcher> reIncluded = new ArrayList<>();
        for (String line : lines) {
            if (line.startsWith("!")) {
                String pattern = line.substring(1);
                if (pattern.endsWith("/")) {
                    pattern = pattern.substring(0, pattern.length() - 1);
                }
                reIncluded.add(FileSystems.getDefault().getPathMatcher("glob:" + pattern));
            } else if (line.endsWith("/") && line.indexOf('/') == line.length() - 1) {
                bareFolderRules.add(line.substring(0, line.length() - 1));
            }
        }
        List<String> hidden = new ArrayList<>();
        Path src = Path.of("src");
        try (Stream<Path> walk = Files.walk(src)) {
            walk.filter(Files::isDirectory).forEach(dir -> {
                String name = dir.getFileName().toString();
                String relative = dir.toString().replace('\\', '/');
                if (bareFolderRules.contains(name) && reIncluded.stream().noneMatch(m -> m.matches(Path.of(relative)))) {
                    hidden.add(relative);
                }
            });
        }
        assertTrue(hidden.isEmpty(), "these source folders are hidden by a .gitignore folder rule and need a \"!path/\" line: " + hidden);
    }
}
