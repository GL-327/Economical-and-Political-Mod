package com.political.power.store;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.political.core.persist.StoreRegistry;
import com.political.core.persist.VersionedStore;
import com.political.power.model.PowerData;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Where the power system keeps its state: {@code power.json} in the world root, a {@link VersionedStore}
 * (schema version, atomic writes, corrupt-file recovery) that the periodic flush and the shutdown hook save.
 *
 * <p>The first time the file does not exist and any of the three old files does, their content is imported
 * by {@link LegacyImport}. The old files are left in place and copied to {@code <name>.pre-power}
 * so nothing can be lost; after the import the mod no longer reads or writes them.</p>
 */
public final class PowerStore {

    public static final int SCHEMA_VERSION = 1;
    private static final Logger LOGGER = LoggerFactory.getLogger(PowerStore.class);

    private static final String[] LEGACY_FILES = {"techniques.json", "magic_data.json", "magic_schools.json"};

    private final VersionedStore<PowerData> store;
    private final List<String> importReport = new ArrayList<>();
    private final VersionedStore.Outcome outcome;

    private PowerStore(VersionedStore<PowerData> store, VersionedStore.Outcome outcome) {
        this.store = store;
        this.outcome = outcome;
    }

    /** Opens (and if needed creates or imports) the store for the world at {@code worldRoot}. */
    public static PowerStore open(Path worldRoot) {
        Path file = worldRoot.resolve("power.json");
        VersionedStore<PowerData> store = VersionedStore
                .builder("power", file, SCHEMA_VERSION, PowerData.CODEC, PowerData::new)
                .keepBackup()
                .build();
        VersionedStore.Outcome outcome = store.load();
        PowerStore result = new PowerStore(store, outcome);
        if (outcome == VersionedStore.Outcome.FRESH) {
            result.importLegacy(worldRoot);
        } else if (outcome == VersionedStore.Outcome.RECOVERED) {
            LOGGER.error("power.json could not be read; players start with empty power state until it is restored "
                    + "from the .corrupt copy next to it. The old technique and magic files were NOT imported, to avoid "
                    + "bringing back stale data over what may have been newer.");
        }
        return result;
    }

    private void importLegacy(Path worldRoot) {
        boolean any = false;
        JsonObject[] roots = new JsonObject[LEGACY_FILES.length];
        for (int i = 0; i < LEGACY_FILES.length; i++) {
            Path legacy = worldRoot.resolve(LEGACY_FILES[i]);
            if (!Files.exists(legacy)) {
                continue;
            }
            any = true;
            roots[i] = readObject(legacy);
            copyAside(legacy);
        }
        if (!any) {
            return;
        }
        LegacyImport.Result result = LegacyImport.run(roots[0], roots[1], roots[2]);
        importReport.addAll(result.report());
        store.set(result.data());
        for (String line : result.report()) {
            LOGGER.info("Power import: {}", line);
        }
        LOGGER.info("Power state was built from the old technique and magic files; power.json is written by the next save");
    }

    private JsonObject readObject(Path file) {
        try {
            JsonElement root = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8));
            if (root.isJsonObject()) {
                return root.getAsJsonObject();
            }
            importReport.add(file.getFileName() + " is not a JSON object; it was left in place and not imported");
        } catch (IOException | RuntimeException e) {
            importReport.add(file.getFileName() + " could not be read (" + e.getMessage() + "); it was left in place and not imported");
            LOGGER.error("Could not read {} for the power import", file, e);
        }
        return null;
    }

    private void copyAside(Path legacy) {
        Path copy = legacy.resolveSibling(legacy.getFileName() + ".pre-power");
        if (Files.exists(copy)) {
            return;
        }
        try {
            Files.copy(legacy, copy, StandardCopyOption.COPY_ATTRIBUTES);
        } catch (IOException e) {
            LOGGER.error("Could not copy {} aside before the power import", legacy, e);
        }
    }

    /** Makes the periodic flush and the shutdown hook save this store. */
    public PowerStore register(StoreRegistry registry) {
        registry.register(store);
        return this;
    }

    public PowerData data() {
        return store.get();
    }

    public void markDirty() {
        store.markDirty();
    }

    public boolean saveIfDirty() {
        return store.saveIfDirty();
    }

    public boolean isDirty() {
        return store.isDirty();
    }

    public VersionedStore.Outcome outcome() {
        return outcome;
    }

    /** What the legacy import did, one line each; empty when nothing was imported. */
    public List<String> importReport() {
        return List.copyOf(importReport);
    }
}
