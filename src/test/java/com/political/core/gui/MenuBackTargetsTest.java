package com.political.core.gui;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * M7: a Back button wired straight to another menu ({@code footer(..., () -> Target.open(player))}) must lead to a menu that
 * opens this one, because that is how the player got here. A Back that goes to a class which never mentions this menu is a
 * Back that sends the player somewhere they did not come from. Menus that can be reached from several places take their
 * parent as an argument ({@link ReturnTo}) and are not caught by this check, which only covers the fixed wiring. A text scan;
 * the real-client crawler ({@code scripts/power-client-test.sh setup,menus,adminmenus}) clicks through the live menus.
 */
class MenuBackTargetsTest {

    private static final Pattern BACK = Pattern.compile(
            "(?:footer\\([^;]*?|\\.back\\()\\s*\\(\\)\\s*->\\s*(?:\\{\\s*)?(?:[\\w.]*\\.)?([A-Z]\\w*)\\.(\\w+)\\(", Pattern.DOTALL);

    @Test
    void thePatternFindsTheBackWiringsTheCheckIsAbout() {
        Matcher footer = BACK.matcher("GuiKit.footer(gui, SIZE, player, () -> ParentGui.open(player));");
        assertTrue(footer.find());
        assertTrue(footer.group(1).equals("ParentGui") && footer.group(2).equals("open"));
        Matcher screen = BACK.matcher("Screen.chest(player, 6, t).footer(() -> HubScreen.open(player)).open();");
        assertTrue(screen.find() && screen.group(1).equals("HubScreen"));
        Matcher element = BACK.matcher("gui.setSlot(49, Elements.back(() -> { OtherGui.openPage(player, 2); }));");
        assertTrue(element.find() && element.group(1).equals("OtherGui") && element.group(2).equals("openPage"));
        assertTrue(!BACK.matcher("GuiKit.footer(gui, SIZE, player, null);").find());
    }

    @Test
    void everyFixedBackLeadsToAMenuThatOpensTheOneItLeaves() throws IOException {
        Map<String, String> sources = new HashMap<>();
        try (Stream<Path> walk = Files.walk(Path.of("src/main/java"))) {
            for (Path path : (Iterable<Path>) walk.filter(p -> p.toString().endsWith(".java"))::iterator) {
                String name = path.getFileName().toString();
                sources.put(name.substring(0, name.length() - ".java".length()), Files.readString(path, StandardCharsets.UTF_8));
            }
        }
        List<String> hits = new ArrayList<>();
        for (Map.Entry<String, String> source : sources.entrySet()) {
            String name = source.getKey();
            Matcher m = BACK.matcher(source.getValue());
            while (m.find()) {
                String target = m.group(1);
                if (target.equals(name) || !sources.containsKey(target)) {
                    continue;
                }
                if (!Pattern.compile("\\b" + Pattern.quote(name) + "\\b").matcher(sources.get(target)).find()) {
                    int line = 1 + (int) source.getValue().substring(0, m.start()).chars().filter(c -> c == '\n').count();
                    hits.add(name + ":" + line + " goes Back to " + target + "." + m.group(2) + ", which never opens " + name);
                }
            }
        }
        assertTrue(hits.isEmpty(), String.join("\n", hits));
    }
}
