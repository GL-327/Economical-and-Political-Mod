package com.political.core;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/**
 * Source level rules from the modernization program. They read src/main/java directly, so a violation
 * fails the build instead of waiting for a review.
 */
class SourcePolicyTest {

    private static final Pattern CONSOLE = Pattern.compile("System\\s*\\.\\s*(out|err)\\b|\\.printStackTrace\\s*\\(");

    /** catch (Exception e) and catch (Throwable t), also inside a multi-catch union. */
    private static final Pattern BROAD_CATCH =
            Pattern.compile("catch\\s*\\(\\s*(?:final\\s+)?(?:[\\w.]+\\s*\\|\\s*)*(?:Exception|Throwable)(?:\\s*\\|\\s*[\\w.]+)*\\s+\\w+\\s*\\)\\s*\\{");

    @Test
    void noConsolePrintsOrStackTraces() throws IOException {
        List<String> hits = new ArrayList<>();
        for (Path file : SourceFiles.javaFiles()) {
            String code = SourceFiles.codeOnly(SourceFiles.read(file));
            Matcher m = CONSOLE.matcher(code);
            while (m.find()) {
                hits.add(SourceFiles.relative(file) + ":" + SourceFiles.lineOf(code, m.start()));
            }
        }
        assertTrue(hits.isEmpty(), "use an SLF4J logger instead of System.out, System.err or printStackTrace: " + hits);
    }

    @Test
    void broadCatchesNeverSwallowSilently() throws IOException {
        List<String> hits = new ArrayList<>();
        for (Path file : SourceFiles.javaFiles()) {
            String code = SourceFiles.codeOnly(SourceFiles.read(file));
            Matcher m = BROAD_CATCH.matcher(code);
            while (m.find()) {
                int open = m.end() - 1;
                String body = code.substring(open + 1, SourceFiles.matchBrace(code, open));
                boolean logs = body.contains("LOGGER") || body.contains("getLogger(");
                boolean rethrows = Pattern.compile("\\bthrow\\b").matcher(body).find();
                if (!logs && !rethrows) {
                    hits.add(SourceFiles.relative(file) + ":" + SourceFiles.lineOf(code, m.start()));
                }
            }
        }
        assertTrue(hits.isEmpty(),
                "a catch of Exception or Throwable must log with context or rethrow, or use a narrower type: " + hits);
    }
}
