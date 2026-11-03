package com.political.core.text;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.nucleoid.server.translations.api.language.ServerLanguage;

/**
 * Asks the server translation library, once at startup, whether it can see our language file. If it can
 * not, players still read English through each component's fallback, but other languages would not work,
 * and this line in the log says so.
 */
public final class TextSelfCheck {

    private static final Logger LOGGER = LoggerFactory.getLogger(TextSelfCheck.class);
    private static final String KEY = "political.text.selfcheck";

    private TextSelfCheck() {
    }

    /** Returns true when the library resolves {@link #KEY} to the text in en_us.json. */
    public static boolean run() {
        try {
            String resolved = ServerLanguage.getLanguage("en_us").serverTranslations().getOrNull(KEY);
            String expected = Text.english(KEY);
            if (resolved != null && resolved.equals(expected)) {
                LOGGER.info("Server-side translation works: {} resolves to \"{}\" ({} political.* keys)",
                        KEY, resolved, Text.keys().size());
                return true;
            }
            LOGGER.warn("Server-side translation does not see {} (got {}); players read the English fallback text, "
                    + "other languages will not work", KEY, resolved);
        } catch (RuntimeException | LinkageError e) {
            LOGGER.warn("Server-side translation could not be checked; players read the English fallback text", e);
        }
        return false;
    }
}
