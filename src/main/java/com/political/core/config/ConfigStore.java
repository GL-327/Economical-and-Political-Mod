package com.political.core.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The values read from one config file, checked against a list of keys.
 *
 * <p>Loading never throws and never stops the server. A file that is missing gives all defaults. A file
 * that is not valid JSON gives all defaults and one error in the log; it is not touched, so the owner can
 * fix it. A value of the wrong type becomes the default, a number outside its range is clamped, and a
 * path that no key declares is reported as a likely typo. Every such case adds a line to
 * {@link #warnings()} and to the log.</p>
 *
 * <p>The file is edited by people, so this class never rewrites it, except that
 * {@link #writeDefaultsIfMissing()} creates it with every default the first time.</p>
 */
public final class ConfigStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigStore.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private final Path file;
    private final List<ConfigKey<?>> keys;
    private final Map<String, Object> values = new HashMap<>();
    private final List<String> warnings = new ArrayList<>();

    private ConfigStore(Path file, List<ConfigKey<?>> keys) {
        this.file = file;
        this.keys = List.copyOf(keys);
    }

    /** A store holding only defaults, for tests and for before the config has loaded. */
    public static ConfigStore defaults(List<ConfigKey<?>> keys) {
        return new ConfigStore(null, keys);
    }

    /** Reads {@code file} and checks it against {@code keys}. Never throws. */
    public static ConfigStore load(Path file, List<ConfigKey<?>> keys) {
        ConfigStore store = new ConfigStore(file, keys);
        store.read();
        return store;
    }

    private void read() {
        if (file == null || !Files.exists(file)) {
            return;
        }
        JsonElement root;
        try {
            root = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8));
        } catch (IOException | RuntimeException e) {
            warn("the config file " + file.getFileName() + " could not be read as JSON (" + e.getMessage()
                    + "); every setting uses its default until it is fixed", e);
            return;
        }
        if (!root.isJsonObject()) {
            warn("the config file " + file.getFileName() + " must contain a JSON object; every setting uses its default", null);
            return;
        }
        Map<String, JsonElement> leaves = new TreeMap<>();
        flatten("", root.getAsJsonObject(), leaves);

        Map<String, ConfigKey<?>> byPath = new HashMap<>();
        for (ConfigKey<?> key : keys) {
            byPath.put(key.path(), key);
        }
        for (Map.Entry<String, JsonElement> leaf : leaves.entrySet()) {
            String path = leaf.getKey();
            ConfigKey<?> key = byPath.get(path);
            if (key == null) {
                if (!isComment(path)) {
                    warn("unknown setting " + path + " (a typo? it is ignored)", null);
                }
                continue;
            }
            List<String> problems = new ArrayList<>();
            Object coerced = key.coerce(plain(leaf.getValue()), problems);
            values.put(path, coerced);
            for (String problem : problems) {
                warn(problem, null);
            }
        }
    }

    private static boolean isComment(String path) {
        for (String part : path.split("\\.")) {
            if (part.startsWith("_")) {
                return true;
            }
        }
        return false;
    }

    private static void flatten(String prefix, JsonObject object, Map<String, JsonElement> out) {
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            String path = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            if (entry.getValue().isJsonObject()) {
                flatten(path, entry.getValue().getAsJsonObject(), out);
            } else {
                out.put(path, entry.getValue());
            }
        }
    }

    /** JSON primitive as a plain Java value; arrays, objects and null become null (and are then rejected). */
    private static Object plain(JsonElement element) {
        if (!element.isJsonPrimitive()) {
            return null;
        }
        JsonPrimitive p = element.getAsJsonPrimitive();
        if (p.isBoolean()) {
            return p.getAsBoolean();
        }
        if (p.isNumber()) {
            return p.getAsNumber();
        }
        return p.getAsString();
    }

    private void warn(String message, Throwable cause) {
        warnings.add(message);
        if (cause != null) {
            LOGGER.error("Config: {}", message, cause);
        } else {
            LOGGER.warn("Config: {}", message);
        }
    }

    /** The checked value for a key, or null when the file did not set it (the caller then uses the default). */
    Object valueOf(ConfigKey<?> key) {
        return values.get(key.path());
    }

    /** What was wrong with the file, one line each; empty when it was fine or absent. */
    public List<String> warnings() {
        return List.copyOf(warnings);
    }

    /**
     * Creates the file with every key at its default if it does not exist yet, so there is something to
     * edit. Returns true if it wrote the file. Never overwrites.
     */
    public boolean writeDefaultsIfMissing() {
        if (file == null || Files.exists(file)) {
            return false;
        }
        JsonObject root = new JsonObject();
        for (ConfigKey<?> key : new ArrayList<>(keys)) {
            JsonObject level = root;
            String[] parts = key.path().split("\\.");
            for (int i = 0; i < parts.length - 1; i++) {
                JsonElement next = level.get(parts[i]);
                if (next == null || !next.isJsonObject()) {
                    next = new JsonObject();
                    level.add(parts[i], next);
                }
                level = next.getAsJsonObject();
            }
            Object def = key.defaultValue();
            String leaf = parts[parts.length - 1];
            if (def instanceof Boolean b) {
                level.addProperty(leaf, b);
            } else if (def instanceof Number n) {
                level.addProperty(leaf, n);
            } else {
                level.addProperty(leaf, String.valueOf(def));
            }
        }
        try {
            Path dir = file.toAbsolutePath().getParent();
            if (dir != null) {
                Files.createDirectories(dir);
            }
            Files.writeString(file, GSON.toJson(root) + System.lineSeparator(), StandardCharsets.UTF_8);
            return true;
        } catch (IOException e) {
            LOGGER.error("Config: could not create {}", file, e);
            return false;
        }
    }
}
