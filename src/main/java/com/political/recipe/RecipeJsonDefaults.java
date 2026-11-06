package com.political.recipe;

import com.google.gson.*;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Pattern;

/** Repairs known generated defaults, while retaining owner edits, alternate recipes and unknown fields. */
public final class RecipeJsonDefaults {
    private static final Gson JSON = new GsonBuilder().disableHtmlEscaping().create();
    private static final Pattern CUSTOM = Pattern.compile("custom_(?:item_)?id\\s*:\\s*[\"']([^\"']+)");
    private static final Map<String, String> LEGACY = loadLegacy();
    private RecipeJsonDefaults() {}

    private static Map<String, String> loadLegacy() {
        var stream = RecipeJsonDefaults.class.getResourceAsStream("/data/politicalserver/recipe_defaults/legacy-v0.json");
        if (stream == null) throw new IllegalStateException("Missing legacy recipe fingerprints");
        try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            var manifest = JsonParser.parseReader(reader).getAsJsonObject();
            if (manifest.get("schema_version").getAsInt() != 1) throw new IllegalStateException("Unsupported recipe fingerprint manifest");
            var hashes = manifest.getAsJsonObject("recipes");
            Map<String, String> result = new HashMap<>();
            hashes.entrySet().forEach(entry -> result.put(entry.getKey(), entry.getValue().getAsString()));
            return Map.copyOf(result);
        } catch (IOException e) { throw new IllegalStateException("Could not read legacy recipe fingerprints", e); }
    }

    private static String canonical(JsonElement value) {
        if (value.isJsonObject()) {
            List<String> fields = new ArrayList<>();
            new TreeSet<>(value.getAsJsonObject().keySet()).forEach(key -> fields.add(JSON.toJson(key) + ":" + canonical(value.getAsJsonObject().get(key))));
            return "{" + String.join(",", fields) + "}";
        }
        if (value.isJsonArray()) {
            List<String> entries = new ArrayList<>();
            value.getAsJsonArray().forEach(entry -> entries.add(canonical(entry)));
            return "[" + String.join(",", entries) + "]";
        }
        return JSON.toJson(value);
    }

    public static String fingerprint(JsonElement value) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(canonical(value).getBytes(StandardCharsets.UTF_8))); }
        catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }

    static boolean knownLegacy(JsonObject recipe) {
        return recipe.has("name") && fingerprint(recipe).equals(LEGACY.get(recipe.get("name").getAsString()));
    }

    private static String output(JsonObject recipe) {
        if (!recipe.has("result") || !recipe.get("result").isJsonObject()) return "";
        JsonObject stack = recipe.getAsJsonObject("result");
        if (stack.has("nbt")) {
            var match = CUSTOM.matcher(stack.get("nbt").getAsString());
            if (match.find()) return "custom:" + match.group(1);
        }
        return stack.has("id") ? stack.get("id").getAsString() : "";
    }

    /** generatedThisBoot only owns exact records written by earlier bootstrap providers in this boot. */
    public static JsonObject merge(JsonObject original, List<JsonObject> generated, Map<String, String> generatedThisBoot) {
        JsonObject root = original.deepCopy();
        JsonArray entries = root.has("recipes") && root.get("recipes").isJsonArray()
                ? root.getAsJsonArray("recipes") : new JsonArray();
        for (JsonObject incoming : generated) {
            String name = incoming.get("name").getAsString();
            String key = output(incoming);
            JsonArray kept = new JsonArray();
            boolean found = false;
            for (JsonElement entry : entries) {
                if (!entry.isJsonObject() || !entry.getAsJsonObject().has("name")) { kept.add(entry); continue; }
                JsonObject existing = entry.getAsJsonObject();
                String oldName = existing.get("name").getAsString();
                String oldHash = fingerprint(existing);
                if (oldName.equals(name)) {
                    found = true;
                    if (existing.equals(incoming) || knownLegacy(existing) || oldHash.equals(generatedThisBoot.get(oldName))) {
                        kept.add(incoming.deepCopy());
                        generatedThisBoot.put(name, fingerprint(incoming));
                    } else kept.add(existing); // owner override: do not overwrite it
                } else if (!key.isEmpty() && key.equals(output(existing)) && oldHash.equals(generatedThisBoot.get(oldName))) {
                    // An earlier provider in this boot produced this default; the later provider takes precedence.
                } else kept.add(existing); // including distinct owner-named variants of this output
            }
            if (!found) {
                kept.add(incoming.deepCopy());
                generatedThisBoot.put(name, fingerprint(incoming));
            }
            entries = kept;
        }
        root.add("recipes", entries);
        return root;
    }
}
