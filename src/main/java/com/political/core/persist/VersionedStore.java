package com.political.core.persist;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.political.core.Clock;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * One JSON file with a schema version, a migration per version step, safe writes and recovery. This is
 * the persistence layer from M1.6; new state uses it and old stores move onto it one at a time.
 *
 * <p><b>File format.</b> {@code {"schema_version": 2, "data": ...}}. A file without those two keys is a
 * legacy file and counts as version 0, with the whole file as the data.</p>
 *
 * <p><b>Loading</b> ({@link #load()}) never throws and never loses data:</p>
 * <ul>
 *   <li>no file: defaults, outcome {@link Outcome#FRESH};</li>
 *   <li>older version: every registered step is applied in order, outcome {@link Outcome#MIGRATED}, the
 *       store is marked dirty so the upgraded file is written by the next flush, and the old file is
 *       first copied to {@code <name>.v<from>.bak} (kept, never overwritten);</li>
 *   <li>unreadable JSON, a failing migration or data the codec rejects: the file is copied to
 *       {@code <name>.corrupt-<UTC timestamp>}, defaults are used, outcome {@link Outcome#RECOVERED};</li>
 *   <li>a version newer than this code knows: defaults are used and the store becomes read only, so
 *       the newer file is never overwritten by an older build, outcome {@link Outcome#TOO_NEW}.</li>
 * </ul>
 *
 * <p><b>Saving</b> ({@link #save()}) writes a temporary file in the same directory, forces it to disk,
 * then moves it over the original in one atomic step, so a crash leaves either the old file or the new
 * one. A failed save keeps the store dirty and is retried by the next flush.</p>
 *
 * <p>All calls come from the server thread; the class is not synchronised.</p>
 *
 * @param <T> the value type kept in the file
 */
public final class VersionedStore<T> {

    public static final String VERSION_KEY = "schema_version";
    public static final String DATA_KEY = "data";

    private static final Logger LOGGER = LoggerFactory.getLogger(VersionedStore.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").withZone(ZoneOffset.UTC);

    /** What {@link #load()} did. */
    public enum Outcome { FRESH, LOADED, MIGRATED, RECOVERED, TOO_NEW }

    private final String name;
    private final Path file;
    private final int currentVersion;
    private final Codec<T> codec;
    private final Supplier<T> defaults;
    private final NavigableMap<Integer, UnaryOperator<JsonElement>> migrations;
    private final boolean keepBackup;
    private final LongSupplier wallClock;

    private T value;
    private boolean loaded;
    private boolean dirty;
    private boolean readOnly;
    private int saveCount;
    private Path lastCorruptCopy;

    private VersionedStore(Builder<T> b) {
        this.name = b.name;
        this.file = b.file;
        this.currentVersion = b.currentVersion;
        this.codec = b.codec;
        this.defaults = b.defaults;
        this.migrations = new TreeMap<>(b.migrations);
        this.keepBackup = b.keepBackup;
        this.wallClock = b.wallClock;
    }

    public static <T> Builder<T> builder(String name, Path file, int currentVersion, Codec<T> codec, Supplier<T> defaults) {
        return new Builder<>(name, file, currentVersion, codec, defaults);
    }

    public static final class Builder<T> {
        private final String name;
        private final Path file;
        private final int currentVersion;
        private final Codec<T> codec;
        private final Supplier<T> defaults;
        private final TreeMap<Integer, UnaryOperator<JsonElement>> migrations = new TreeMap<>();
        private boolean keepBackup;
        private LongSupplier wallClock = Clock::wallMillis;

        private Builder(String name, Path file, int currentVersion, Codec<T> codec, Supplier<T> defaults) {
            this.name = Objects.requireNonNull(name, "name");
            this.file = Objects.requireNonNull(file, "file");
            if (currentVersion < 1) {
                throw new IllegalArgumentException("store " + name + ": the current schema version starts at 1");
            }
            this.currentVersion = currentVersion;
            this.codec = Objects.requireNonNull(codec, "codec");
            this.defaults = Objects.requireNonNull(defaults, "defaults");
        }

        /**
         * Registers the step that turns data of version {@code fromVersion} into version
         * {@code fromVersion + 1}. Version 0 is a legacy file with no version marker. The step receives
         * the data part and returns the new data part; it may throw to reject the data.
         */
        public Builder<T> migration(int fromVersion, UnaryOperator<JsonElement> step) {
            if (fromVersion < 0 || fromVersion >= currentVersion) {
                throw new IllegalArgumentException("store " + name + ": migration from " + fromVersion
                        + " does not lead towards current version " + currentVersion);
            }
            if (migrations.put(fromVersion, Objects.requireNonNull(step, "step")) != null) {
                throw new IllegalArgumentException("store " + name + ": two migrations from version " + fromVersion);
            }
            return this;
        }

        /** Also keep the previous file as {@code <name>.bak} on every save. For small stores that matter a lot. */
        public Builder<T> keepBackup() {
            this.keepBackup = true;
            return this;
        }

        /** Test hook: the wall clock used for the corrupt-file timestamp. */
        public Builder<T> wallClock(LongSupplier clock) {
            this.wallClock = Objects.requireNonNull(clock, "clock");
            return this;
        }

        public VersionedStore<T> build() {
            if (!migrations.isEmpty()) {
                int first = migrations.firstKey();
                for (int v = first; v < currentVersion; v++) {
                    if (!migrations.containsKey(v)) {
                        throw new IllegalStateException("store " + name + ": no migration from version " + v
                                + " to " + (v + 1) + "; steps must cover every version up to " + currentVersion);
                    }
                }
            }
            return new VersionedStore<>(this);
        }
    }

    // ── state ───────────────────────────────────────────────────────────────────────────────

    public String name() {
        return name;
    }

    public Path file() {
        return file;
    }

    /** The value in memory. {@link #load()} must have run. */
    public T get() {
        requireLoaded();
        return value;
    }

    /** Replaces the value and marks the store dirty. */
    public void set(T newValue) {
        requireLoaded();
        this.value = Objects.requireNonNull(newValue, "value");
        this.dirty = true;
    }

    /** Applies a change to the value and marks the store dirty. */
    public void update(UnaryOperator<T> change) {
        set(change.apply(get()));
    }

    /** Call after changing the object returned by {@link #get()} in place. */
    public void markDirty() {
        requireLoaded();
        this.dirty = true;
    }

    public boolean isDirty() {
        return dirty;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    /** How many times the file was actually written. */
    public int saveCount() {
        return saveCount;
    }

    /** The copy made when the last load found a corrupt file, or null. */
    public Path lastCorruptCopy() {
        return lastCorruptCopy;
    }

    private void requireLoaded() {
        if (!loaded) {
            throw new IllegalStateException("store " + name + " used before load()");
        }
    }

    // ── load ────────────────────────────────────────────────────────────────────────────────

    public Outcome load() {
        loaded = true;
        dirty = false;
        readOnly = false;
        lastCorruptCopy = null;
        if (!Files.exists(file)) {
            value = defaults.get();
            return Outcome.FRESH;
        }
        String text;
        JsonElement root;
        try {
            text = Files.readString(file, StandardCharsets.UTF_8);
            root = JsonParser.parseString(text);
        } catch (IOException | RuntimeException e) {
            return recover("the file cannot be read as JSON", e);
        }

        int version = 0;
        JsonElement data = root;
        if (root.isJsonObject() && root.getAsJsonObject().has(VERSION_KEY) && root.getAsJsonObject().has(DATA_KEY)) {
            JsonObject obj = root.getAsJsonObject();
            try {
                version = obj.get(VERSION_KEY).getAsInt();
            } catch (RuntimeException e) {
                return recover("schema_version is not a number", e);
            }
            data = obj.get(DATA_KEY);
        }

        if (version > currentVersion) {
            value = defaults.get();
            readOnly = true;
            LOGGER.error("Store {} ({}) was written by a newer version of the mod (schema {}, this build knows {}). "
                    + "It is left untouched and not saved over; update the mod to use it.", name, file, version, currentVersion);
            return Outcome.TOO_NEW;
        }
        if (version < currentVersion && !migrations.containsKey(version)) {
            return recover("no migration path from schema " + version + " to " + currentVersion, null);
        }

        int from = version;
        try {
            for (int v = version; v < currentVersion; v++) {
                data = migrations.get(v).apply(data);
            }
        } catch (RuntimeException e) {
            return recover("migration from schema " + version + " failed", e);
        }

        DataResult<T> decoded = codec.parse(JsonOps.INSTANCE, data);
        if (decoded.result().isEmpty()) {
            return recover("the data does not match schema " + currentVersion + ": "
                    + decoded.error().map(DataResult.Error::message).orElse("unknown error"), null);
        }
        value = decoded.result().get();
        if (from < currentVersion) {
            keepPreMigrationCopy(from);
            dirty = true;
            LOGGER.info("Store {} migrated from schema {} to {}; the upgraded file is written by the next save.",
                    name, from, currentVersion);
            return Outcome.MIGRATED;
        }
        return Outcome.LOADED;
    }

    private Outcome recover(String reason, Throwable cause) {
        value = defaults.get();
        Path copy = file.resolveSibling(file.getFileName() + ".corrupt-" + STAMP.format(Instant.ofEpochMilli(wallClock.getAsLong())));
        try {
            Files.copy(file, copy, StandardCopyOption.COPY_ATTRIBUTES);
            lastCorruptCopy = copy;
            LOGGER.warn("Store {} ({}) could not be loaded: {}. The file was copied to {} and defaults are in use.",
                    name, file, reason, copy.getFileName(), cause);
        } catch (IOException e) {
            // The copy matters more than the defaults: without it the next save would destroy the original.
            readOnly = true;
            LOGGER.error("Store {} ({}) could not be loaded ({}) and the file could not be copied aside; "
                    + "the store is read only so the original survives.", name, file, reason, e);
        }
        return Outcome.RECOVERED;
    }

    private void keepPreMigrationCopy(int fromVersion) {
        Path copy = file.resolveSibling(file.getFileName() + ".v" + fromVersion + ".bak");
        if (Files.exists(copy)) {
            return;
        }
        try {
            Files.copy(file, copy, StandardCopyOption.COPY_ATTRIBUTES);
        } catch (IOException e) {
            readOnly = true;
            LOGGER.error("Store {} ({}) could not keep a copy of the schema {} file before migrating; "
                    + "the store is read only so the original survives.", name, file, fromVersion, e);
        }
    }

    // ── save ────────────────────────────────────────────────────────────────────────────────

    /** Saves when dirty. Returns true if the file was written. */
    public boolean saveIfDirty() {
        return dirty && save();
    }

    /** Writes the file now. Returns false (and stays dirty) if it could not. */
    public boolean save() {
        requireLoaded();
        if (readOnly) {
            LOGGER.warn("Store {} ({}) is read only and was not saved.", name, file);
            return false;
        }
        String json;
        try {
            JsonElement encoded = codec.encodeStart(JsonOps.INSTANCE, value).getOrThrow(IllegalStateException::new);
            JsonObject root = new JsonObject();
            root.addProperty(VERSION_KEY, currentVersion);
            root.add(DATA_KEY, encoded);
            json = GSON.toJson(root);
        } catch (RuntimeException e) {
            LOGGER.error("Store {} could not be encoded and was not saved", name, e);
            return false;
        }
        Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
        try {
            Path dir = file.toAbsolutePath().getParent();
            if (dir != null) {
                Files.createDirectories(dir);
            }
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            try (FileChannel channel = FileChannel.open(tmp, StandardOpenOption.CREATE, StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING)) {
                ByteBuffer buffer = ByteBuffer.wrap(bytes);
                while (buffer.hasRemaining()) {
                    channel.write(buffer);
                }
                channel.force(true);
            }
            if (keepBackup && Files.exists(file)) {
                Files.copy(file, file.resolveSibling(file.getFileName() + ".bak"), StandardCopyOption.REPLACE_EXISTING);
            }
            try {
                Files.move(tmp, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                LOGGER.debug("Store {}: atomic move not supported here, replacing the file instead", name);
                Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            LOGGER.error("Store {} ({}) could not be saved; it stays dirty and will be retried", name, file, e);
            return false;
        }
        dirty = false;
        saveCount++;
        return true;
    }
}
