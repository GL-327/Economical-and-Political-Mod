package com.political.core;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * Source level rules from the modernization program. They read src/main/java directly, so a violation
 * fails the build instead of waiting for a review.
 */
class SourcePolicyTest {

    private static final Path SOURCE_ROOT = Path.of("src", "main", "java");

    /** Returns "relative/path.java:line" for every line of code (comments stripped) that matches. */
    static List<String> findLines(Pattern pattern) throws IOException {
        List<String> hits = new ArrayList<>();
        List<Path> files;
        try (Stream<Path> walk = Files.walk(SOURCE_ROOT)) {
            files = walk.filter(p -> p.toString().endsWith(".java")).sorted().toList();
        }
        for (Path file : files) {
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            boolean inBlockComment = false;
            for (int i = 0; i < lines.size(); i++) {
                String code = lines.get(i);
                if (inBlockComment) {
                    int end = code.indexOf("*/");
                    if (end < 0) {
                        continue;
                    }
                    inBlockComment = false;
                    code = code.substring(end + 2);
                }
                int start = code.indexOf("/*");
                if (start >= 0 && code.indexOf("*/", start) < 0) {
                    inBlockComment = true;
                    code = code.substring(0, start);
                }
                int lineComment = code.indexOf("//");
                if (lineComment >= 0) {
                    code = code.substring(0, lineComment);
                }
                if (pattern.matcher(code).find()) {
                    String relative = SOURCE_ROOT.relativize(file).toString().replace(file.getFileSystem().getSeparator(), "/");
                    hits.add(relative + ":" + (i + 1));
                }
            }
        }
        return hits;
    }

    @Test
    void noConsolePrintsOrStackTraces() throws IOException {
        List<String> hits = findLines(Pattern.compile("System\\s*\\.\\s*(out|err)\\b|\\.printStackTrace\\s*\\("));
        assertTrue(hits.isEmpty(), "use an SLF4J logger instead of System.out, System.err or printStackTrace: " + hits);
    }
}
