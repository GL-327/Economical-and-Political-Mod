package com.political.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/**
 * Keeps the time policy honest: gameplay timing reads {@link Clock}, and every remaining raw
 * {@code System.currentTimeMillis()} is listed in {@code wallclock-allowlist.txt} with a category and
 * the milestone that moves it. The recorded number must match the code exactly, so the list shrinks
 * as systems move and can never quietly grow.
 */
class WallClockPolicyTest {

    private static final Pattern WALL_CLOCK = Pattern.compile("System\\s*\\.\\s*currentTimeMillis\\s*\\(");
    private static final Set<String> CATEGORIES = Set.of("P", "S", "D", "N");

    private record Entry(int max, String category, String milestone, String reason) {
    }

    private static Map<String, Entry> loadAllowlist() throws IOException {
        Map<String, Entry> entries = new TreeMap<>();
        try (InputStream in = WallClockPolicyTest.class.getResourceAsStream("/wallclock-allowlist.txt")) {
            assertTrue(in != null, "wallclock-allowlist.txt is missing from the test resources");
            for (String line : new String(in.readAllBytes(), StandardCharsets.UTF_8).split("\n")) {
                String trimmed = line.strip();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                String[] parts = trimmed.split("\\s*\\|\\s*", 5);
                assertEquals(5, parts.length, "expected 'path | max | category | milestone | reason': " + line);
                assertTrue(CATEGORIES.contains(parts[2]), "unknown category in: " + line);
                assertTrue(parts[3].matches("M\\d"), "milestone must look like M3 in: " + line);
                assertTrue(parts[4].length() >= 10, "give a real reason in: " + line);
                Entry previous = entries.put(parts[0], new Entry(Integer.parseInt(parts[1]), parts[2], parts[3], parts[4]));
                assertTrue(previous == null, "listed twice: " + parts[0]);
            }
        }
        return entries;
    }

    @Test
    void rawWallClockUsesMatchTheRecordedList() throws IOException {
        Map<String, Entry> allowed = loadAllowlist();
        Map<String, Integer> actual = new TreeMap<>();
        for (Path file : SourceFiles.javaFiles()) {
            String code = SourceFiles.codeOnly(SourceFiles.read(file));
            Matcher m = WALL_CLOCK.matcher(code);
            int count = 0;
            while (m.find()) {
                count++;
            }
            if (count > 0) {
                actual.put(SourceFiles.relative(file), count);
            }
        }
        List<String> problems = new ArrayList<>();
        for (Map.Entry<String, Integer> e : actual.entrySet()) {
            Entry entry = allowed.get(e.getKey());
            if (entry == null) {
                problems.add(e.getKey() + " has " + e.getValue() + " raw System.currentTimeMillis() calls and is not listed; use Clock");
            } else if (e.getValue() > entry.max()) {
                problems.add(e.getKey() + " went from " + entry.max() + " to " + e.getValue() + " raw calls; use Clock");
            } else if (e.getValue() < entry.max()) {
                problems.add(e.getKey() + " is down to " + e.getValue() + " raw calls; lower its number in wallclock-allowlist.txt (was " + entry.max() + ")");
            }
        }
        for (String listed : allowed.keySet()) {
            if (!actual.containsKey(listed)) {
                problems.add(listed + " no longer uses the wall clock; remove it from wallclock-allowlist.txt");
            }
        }
        assertTrue(problems.isEmpty(), String.join("\n", problems));
    }
}
