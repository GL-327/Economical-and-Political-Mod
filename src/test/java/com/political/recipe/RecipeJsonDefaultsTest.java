package com.political.recipe;

import com.google.gson.*;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RecipeJsonDefaultsTest {
    private JsonObject legacy() throws Exception {
        try (var reader = new InputStreamReader(getClass().getResourceAsStream("/recipes/legacy-generated-sample.json"), StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }
    private JsonObject recipe(String name, String id) {
        JsonObject r = new JsonObject();
        r.addProperty("name", name);
        JsonObject result = new JsonObject();
        result.addProperty("nbt", "{components:{\"minecraft:custom_data\":{custom_id:\"" + id + "\"}},id:\"minecraft:heavy_core\"}");
        r.add("result", result);
        return r;
    }
    private JsonObject root(JsonObject... recipes) {
        JsonObject root = new JsonObject();
        root.addProperty("owner-note", "retain this");
        JsonArray array = new JsonArray();
        for (JsonObject recipe : recipes) array.add(recipe);
        root.add("recipes", array);
        return root;
    }

    @Test void knownLegacyIsRepairedAndOwnerEditIsPreserved() throws Exception {
        JsonObject old = legacy();
        assertTrue(RecipeJsonDefaults.knownLegacy(old), "Cross-language canonical fingerprint must match the actual saved fixture");
        JsonObject fixed = old.deepCopy();
        fixed.addProperty("requiredLevel", 123);
        assertEquals(fixed, RecipeJsonDefaults.merge(root(old), List.of(fixed), new HashMap<>()).getAsJsonArray("recipes").get(0));
        JsonObject edited = old.deepCopy();
        edited.addProperty("owner-note", "my override");
        assertFalse(RecipeJsonDefaults.knownLegacy(edited));
        assertEquals(edited, RecipeJsonDefaults.merge(root(edited), List.of(fixed), new HashMap<>()).getAsJsonArray("recipes").get(0));
    }

    @Test void preservesAlternateOutputsAndRootFields() {
        var alternate = recipe("owner alternate", "core");
        var standard = recipe("default core", "core");
        var merged = RecipeJsonDefaults.merge(root(alternate), List.of(standard), new HashMap<>());
        assertEquals(2, merged.getAsJsonArray("recipes").size());
        assertEquals("retain this", merged.get("owner-note").getAsString());
    }

    @Test void onlyEarlierGeneratedProviderIsReplacedAndMergeIsIdempotent() {
        Map<String, String> owned = new HashMap<>();
        JsonObject initial = root(recipe("owner", "core"));
        var first = RecipeJsonDefaults.merge(initial, List.of(recipe("first provider", "core")), owned);
        var second = RecipeJsonDefaults.merge(first, List.of(recipe("last provider", "core")), owned);
        assertEquals(2, second.getAsJsonArray("recipes").size());
        assertEquals("owner", second.getAsJsonArray("recipes").get(0).getAsJsonObject().get("name").getAsString());
        assertEquals(second, RecipeJsonDefaults.merge(second, List.of(recipe("last provider", "core")), new HashMap<>()));
        assertEquals(initial.getAsJsonArray("recipes").size(), 1, "Input is not mutated");
    }

    @Test void ownerEditToAnEarlierGeneratedRecordIsRetainedByTheNextProvider() {
        Map<String, String> owned = new HashMap<>();
        var first = RecipeJsonDefaults.merge(root(), List.of(recipe("first provider", "core")), owned);
        var edited = first.getAsJsonArray("recipes").get(0).getAsJsonObject();
        edited.addProperty("owner-note", "changed after the first provider");
        var merged = RecipeJsonDefaults.merge(first, List.of(recipe("later provider", "core")), owned);
        assertEquals(2, merged.getAsJsonArray("recipes").size());
        assertEquals(edited, merged.getAsJsonArray("recipes").get(0));
        assertEquals("retain this", merged.get("owner-note").getAsString());
    }
}
