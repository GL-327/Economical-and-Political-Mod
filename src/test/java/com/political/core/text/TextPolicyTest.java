package com.political.core.text;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.political.core.SourceFiles;
import org.junit.jupiter.api.Test;

/**
 * Rules for player-facing text (M1.9):
 * <ul>
 *   <li>the language file is well formed;</li>
 *   <li>every key used with {@code Text.tr("...")} exists in it;</li>
 *   <li>the number of direct {@code Component.literal(} calls per file only goes down (the old code is
 *       converted system by system in the milestone that rebuilds it; docs/TEXT_MIGRATION.md).</li>
 * </ul>
 */
class TextPolicyTest {

    private static final Path LANG = Path.of("src", "main", "resources", "assets", "politicalserver", "lang", "en_us.json");
    private static final Path ALLOWLIST = Path.of("src", "test", "resources", "text-literal-allowlist.txt");
    private static final Pattern LITERAL = Pattern.compile("Component\\s*\\.\\s*literal\\s*\\(");
    private static final Pattern TR_CALL = Pattern.compile("Text\\s*\\.\\s*tr\\s*\\(\\s*\"([^\"]+)\"");
    private static final Pattern KEY = Pattern.compile("political(\\.[a-z0-9_]+)+");
    private static final Pattern GOOD_PERCENT = Pattern.compile("%(?:\\d+\\$)?s|%%");

    // ── language file ───────────────────────────────────────────────────────────────────────

    @Test
    void languageFileIsWellFormed() throws IOException {
        String raw = Files.readString(LANG, StandardCharsets.UTF_8);
        JsonObject json = JsonParser.parseString(raw).getAsJsonObject();
        assertFalse(json.keySet().isEmpty(), "en_us.json has no keys");

        Set<String> seen = new HashSet<>();
        Matcher keys = Pattern.compile("^\\s*\"([^\"]+)\"\\s*:", Pattern.MULTILINE).matcher(raw);
        while (keys.find()) {
            assertTrue(seen.add(keys.group(1)), "duplicate key in en_us.json: " + keys.group(1));
        }
        for (Map.Entry<String, com.google.gson.JsonElement> entry : json.entrySet()) {
            String key = entry.getKey();
            assertTrue(KEY.matcher(key).matches(), "key must look like political.area.thing: " + key);
            String value = entry.getValue().getAsString();
            assertFalse(value.isBlank(), "empty text for " + key);
            String withoutValid = GOOD_PERCENT.matcher(value).replaceAll("");
            assertFalse(withoutValid.contains("%"), "stray % in " + key + " (use %s, %1$s or %%): " + value);
        }
    }

    @Test
    void everyKeyUsedInCodeExists() throws IOException {
        Set<String> known = JsonParser.parseString(Files.readString(LANG, StandardCharsets.UTF_8)).getAsJsonObject().keySet();
        List<String> missing = new ArrayList<>();
        for (Path file : SourceFiles.javaFiles()) {
            Matcher m = TR_CALL.matcher(SourceFiles.read(file));
            while (m.find()) {
                if (!known.contains(m.group(1))) {
                    missing.add(m.group(1) + " in " + SourceFiles.relative(file));
                }
            }
        }
        assertTrue(missing.isEmpty(), "Text.tr keys missing from en_us.json: " + missing);
    }

    // ── Text.tr and Text.plain ──────────────────────────────────────────────────────────────

    @Test
    void trUsesTheEnglishTextAsFallbackAndKeepsArguments() {
        var component = Text.tr("political.error.generic");
        var contents = (net.minecraft.network.chat.contents.TranslatableContents) component.getContents();
        assertEquals("political.error.generic", contents.getKey());
        assertEquals("Something went wrong. Please tell an admin.", contents.getFallback());

        var withArgs = (net.minecraft.network.chat.contents.TranslatableContents) Text.tr("political.error.generic", "a", 2).getContents();
        assertEquals(2, withArgs.getArgs().length);
    }

    @Test
    void anUnknownKeyShowsAsItselfAndNeverThrows() {
        var contents = (net.minecraft.network.chat.contents.TranslatableContents) Text.tr("political.not.in.the.file").getContents();
        assertEquals("political.not.in.the.file", contents.getFallback());
    }

    @Test
    void aMalformedKeyIsRejected() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> Text.tr("hello world"));
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> Text.tr("Political.Upper"));
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> Text.tr("minecraft.thing"));
    }

    // ── direct Component.literal ratchet ────────────────────────────────────────────────────

    private static Map<String, Integer> currentLiteralCounts() throws IOException {
        Map<String, Integer> counts = new TreeMap<>();
        for (Path file : SourceFiles.javaFiles()) {
            Matcher m = LITERAL.matcher(SourceFiles.codeOnly(SourceFiles.read(file)));
            int n = 0;
            while (m.find()) {
                n++;
            }
            if (n > 0) {
                counts.put(SourceFiles.relative(file), n);
            }
        }
        return counts;
    }

    private static Map<String, Integer> readAllowlist() throws IOException {
        Map<String, Integer> out = new TreeMap<>();
        if (!Files.exists(ALLOWLIST)) {
            return out;
        }
        for (String line : Files.readAllLines(ALLOWLIST, StandardCharsets.UTF_8)) {
            String t = line.strip();
            if (t.isEmpty() || t.startsWith("#")) {
                continue;
            }
            String[] parts = t.split("\\s*\\|\\s*");
            assertEquals(2, parts.length, "expected 'path | count': " + line);
            out.put(parts[0], Integer.parseInt(parts[1]));
        }
        return out;
    }

    @Test
    void directComponentLiteralCallsOnlyGoDown() throws IOException {
        Map<String, Integer> actual = currentLiteralCounts();
        if (Boolean.getBoolean("political.updateDocs")) {
            Map<String, Integer> recorded = readAllowlist();
            for (Map.Entry<String, Integer> e : actual.entrySet()) {
                Integer was = recorded.get(e.getKey());
                if (!recorded.isEmpty() && (was == null || e.getValue() > was)) {
                    throw new AssertionError("refusing to raise the allowance for " + e.getKey()
                            + ": use Text.tr for words and Text.plain for data instead of Component.literal");
                }
            }
            StringBuilder sb = new StringBuilder();
            sb.append("# Direct Component.literal( calls per file. WallClock-style ratchet: the count may only go down.\n");
            sb.append("# New player-facing words use Text.tr(\"political.area.thing\") with a key in en_us.json; data such as\n");
            sb.append("# names and numbers use Text.plain(...). Regenerate after converting a file with\n");
            sb.append("# ./gradlew test -Dpolitical.updateDocs=true (it refuses to raise any number).\n");
            sb.append("# format: path | calls\n");
            for (Map.Entry<String, Integer> e : actual.entrySet()) {
                sb.append(e.getKey()).append(" | ").append(e.getValue()).append('\n');
            }
            Files.writeString(ALLOWLIST, sb.toString(), StandardCharsets.UTF_8);
        }
        Map<String, Integer> allowed = readAllowlist();
        List<String> problems = new ArrayList<>();
        for (Map.Entry<String, Integer> e : actual.entrySet()) {
            Integer max = allowed.get(e.getKey());
            if (max == null) {
                problems.add(e.getKey() + " has " + e.getValue() + " Component.literal calls and is not listed; use Text.tr / Text.plain");
            } else if (e.getValue() > max) {
                problems.add(e.getKey() + " went from " + max + " to " + e.getValue() + " Component.literal calls; use Text.tr / Text.plain");
            } else if (e.getValue() < max) {
                problems.add(e.getKey() + " is down to " + e.getValue() + " calls; lower its number in text-literal-allowlist.txt (was " + max + ")");
            }
        }
        for (String listed : allowed.keySet()) {
            if (!actual.containsKey(listed)) {
                problems.add(listed + " has no Component.literal calls any more; remove it from text-literal-allowlist.txt");
            }
        }
        assertTrue(problems.isEmpty(), String.join("\n", problems.subList(0, Math.min(problems.size(), 30))));
    }
}
