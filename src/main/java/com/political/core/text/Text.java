package com.political.core.text;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Where player-facing text is made (M1.9).
 *
 * <p>Words shown to players are keys in {@code assets/politicalserver/lang/en_us.json} such as
 * {@code political.technique.blue.name}, built with {@link #tr}. A vanilla client has no copy of that file,
 * so two things make the words appear: the server translation library (bundled) replaces the key with the
 * text of the player's language in every packet, and each component also carries the English text as
 * its fallback, so even if the library did nothing a player would read English, not a raw key.</p>
 *
 * <p>Text that is data rather than words (a player's name, a number, a coordinate) is made with
 * {@link #plain}. New code does not call {@code Component.literal} directly; {@code TextPolicyTest}
 * keeps the old calls from growing and lists them per file for the migration (docs/TEXT_MIGRATION.md).</p>
 */
public final class Text {

    /** Every key starts with this and is lower case words separated by dots, for example political.tree.admin.title. */
    public static final String PREFIX = "political.";

    private static final Pattern KEY = Pattern.compile("political(\\.[a-z0-9_]+)+");
    private static final Logger LOGGER = LoggerFactory.getLogger(Text.class);
    private static final String ENGLISH_RESOURCE = "/assets/politicalserver/lang/en_us.json";

    private static final Map<String, String> ENGLISH = loadEnglish();
    private static final Set<String> WARNED = Collections.synchronizedSet(new HashSet<>());

    private Text() {
    }

    /**
     * Translatable text for a key from en_us.json, with that English text as the fallback. An unknown key is
     * logged once and shows as the key itself; it never throws at runtime, but a key that is not in the file
     * fails {@code TextPolicyTest}.
     *
     * @param args values for %s / %1$s in the text, such as a name or an amount
     */
    public static MutableComponent tr(String key, Object... args) {
        if (!KEY.matcher(key).matches()) {
            throw new IllegalArgumentException("text keys look like political.area.thing, got: " + key);
        }
        String english = ENGLISH.get(key);
        if (english == null) {
            if (WARNED.add(key)) {
                LOGGER.warn("Text key {} is not in en_us.json; players will see the key", key);
            }
            english = key;
        }
        return Component.translatableWithFallback(key, english, args);
    }

    /** Text that is data, not words: a name, a number, a coordinate. Words go through {@link #tr}. */
    public static MutableComponent plain(String data) {
        return Component.literal(data);
    }

    /** The English text of a key, or null; for logs and tests. */
    public static String english(String key) {
        return ENGLISH.get(key);
    }

    /** All keys in en_us.json. */
    public static Set<String> keys() {
        return Collections.unmodifiableSet(ENGLISH.keySet());
    }

    private static Map<String, String> loadEnglish() {
        Map<String, String> out = new HashMap<>();
        try (InputStream in = Text.class.getResourceAsStream(ENGLISH_RESOURCE)) {
            if (in == null) {
                LOGGER.error("{} is missing from the mod; every text key will show as its fallback", ENGLISH_RESOURCE);
                return out;
            }
            JsonObject json = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
            for (Map.Entry<String, JsonElement> e : json.entrySet()) {
                out.put(e.getKey(), e.getValue().getAsString());
            }
        } catch (IOException | RuntimeException e) {
            LOGGER.error("{} could not be read; every text key will show as its fallback", ENGLISH_RESOURCE, e);
        }
        return out;
    }
}
