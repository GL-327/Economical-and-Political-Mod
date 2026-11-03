package com.political.core.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * STYLE.md has hand-written rules and tables generated from the layout module. This test renders the tables
 * and fails when the file is stale; {@code ./gradlew test -Dpolitical.updateDocs=true} rewrites them.
 */
class StyleGuideTest {

    private static final Path STYLE = Path.of("STYLE.md");

    private static Map<String, String> generated() {
        Map<String, String> blocks = new LinkedHashMap<>();

        StringBuilder palette = new StringBuilder("| Name | Color | Used for |\n| --- | --- | --- |\n");
        for (Palette p : Palette.values()) {
            palette.append("| `").append(p.name()).append("` | `").append(p.hex()).append("` | ").append(p.use()).append(" |\n");
        }
        blocks.put("palette", palette.toString());

        StringBuilder icons = new StringBuilder("| Role | Item | Used for |\n| --- | --- | --- |\n");
        for (Icon i : Icon.values()) {
            icons.append("| `").append(i.name()).append("` | `").append(i.itemId()).append("` | ").append(i.use()).append(" |\n");
        }
        blocks.put("icons", icons.toString());

        StringBuilder grid = new StringBuilder("| Size | Rows | Back | Close | Previous | Page info | Next | List slots per page |\n"
                + "| ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |\n");
        for (int rows = Layout.MIN_ROWS; rows <= Layout.MAX_ROWS; rows++) {
            int size = Layout.size(rows);
            grid.append("| ").append(size).append(" | ").append(rows).append(" | ")
                    .append(Layout.backSlot(size)).append(" | ").append(Layout.closeSlot(size)).append(" | ")
                    .append(Layout.previousPageSlot(size)).append(" | ").append(Layout.pageInfoSlot(size)).append(" | ")
                    .append(Layout.nextPageSlot(size)).append(" | ").append(Layout.perPage(Layout.size(rows))).append(" |\n");
        }
        grid.append("\nHeader slot: ").append(Layout.HEADER_SLOT).append(". Confirmation screens are ")
                .append(Layout.CONFIRM_ROWS).append(" rows: what it is about in slot ").append(Layout.CONFIRM_INFO_SLOT)
                .append(", Confirm in slot ").append(Layout.CONFIRM_YES_SLOT).append(", Cancel in slot ")
                .append(Layout.CONFIRM_NO_SLOT).append(".\n");
        blocks.put("grid", grid.toString());

        StringBuilder sounds = new StringBuilder("| Event | Sound | Volume | Pitch | Used for |\n| --- | --- | ---: | ---: | --- |\n");
        for (UiSound s : UiSound.values()) {
            sounds.append("| `").append(s.name()).append("` | `").append(s.soundId()).append("` | ").append(s.volume())
                    .append(" | ").append(s.pitch()).append(" | ").append(s.use()).append(" |\n");
        }
        blocks.put("sounds", sounds.toString());
        return blocks;
    }

    private static String fill(String text, Map<String, String> blocks) {
        String out = text;
        for (Map.Entry<String, String> block : blocks.entrySet()) {
            String begin = "<!-- BEGIN GENERATED " + block.getKey() + " -->\n";
            String end = "<!-- END GENERATED " + block.getKey() + " -->";
            int a = out.indexOf(begin);
            int b = out.indexOf(end);
            assertTrue(a >= 0 && b > a, "STYLE.md is missing the generated block '" + block.getKey() + "'");
            out = out.substring(0, a + begin.length()) + block.getValue() + out.substring(b);
        }
        return out;
    }

    @Test
    void styleGuideTablesMatchTheCode() throws IOException {
        String current = Files.readString(STYLE, StandardCharsets.UTF_8).replace("\r\n", "\n");
        String expected = fill(current, generated());
        if (Boolean.getBoolean("political.updateDocs")) {
            Files.writeString(STYLE, expected, StandardCharsets.UTF_8);
            current = expected;
        }
        assertEquals(expected, current, "STYLE.md tables are out of date; run ./gradlew test -Dpolitical.updateDocs=true");
    }
}
