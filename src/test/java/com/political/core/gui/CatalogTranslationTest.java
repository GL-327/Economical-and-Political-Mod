package com.political.core.gui;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class CatalogTranslationTest {
    @Test void everyCategoryHasATranslatedNameIncludingCategoriesOnLaterPages() throws Exception {
        var language = JsonParser.parseString(Files.readString(Path.of(
                "src/main/resources/assets/politicalserver/lang/en_us.json"))).getAsJsonObject();
        for (String source : new String[]{"CustomItemsGui.java", "recipe/RecipeEditorGui.java"}) {
            String java = Files.readString(Path.of("src/main/java/com/political", source));
            String categories = java.substring(java.indexOf("public enum Category"), java.indexOf("final String label"));
            var names = Pattern.compile("(\\w+)\\s*\\(\\s*\"").matcher(categories);
            int checked = 0;
            while (names.find()) {
                String key = "political.catalog.category." + names.group(1).toLowerCase(Locale.ROOT);
                assertTrue(language.has(key), source + ": missing " + key);
                checked++;
            }
            assertTrue(checked > CatalogModel.CARDS_PER_PAGE, "check must include later category pages");
        }
    }
}
