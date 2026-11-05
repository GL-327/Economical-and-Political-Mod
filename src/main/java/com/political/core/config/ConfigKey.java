package com.political.core.config;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * One setting: where it lives in the file, its type, default, allowed range and a description. Keys are
 * declared once, as constants in a holder class listed in {@link ConfigCatalog}, so the config reference
 * can be generated from the code and a value in the file can be checked against what the key allows.
 *
 * <p>The path is dotted and lower case, for example {@code tick.budget.server_ms}. In the file each dot
 * is one level of nesting: {@code {"tick": {"budget": {"server_ms": 5.0}}}}.</p>
 *
 * @param <T> {@link Integer}, {@link Double}, {@link Boolean} or {@link String}
 */
public final class ConfigKey<T> {

    /** The kinds of value a key can hold. */
    public enum Type { INT, DOUBLE, BOOLEAN, CHOICE }

    private final String path;
    private final Type type;
    private final T defaultValue;
    private final T min;
    private final T max;
    private final List<String> choices;
    private final String description;

    private ConfigKey(String path, Type type, T defaultValue, T min, T max, List<String> choices, String description) {
        if (!path.matches("[a-z][a-z0-9_]*(\\.[a-z][a-z0-9_]*)+")) {
            throw new IllegalArgumentException("config path must be lower case, dotted, and have at least two parts: " + path);
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("config key " + path + " needs a description");
        }
        this.path = path;
        this.type = type;
        this.defaultValue = defaultValue;
        this.min = min;
        this.max = max;
        this.choices = choices;
        this.description = description;
        ConfigRegistry.register(this);
    }

    public static ConfigKey<Integer> integer(String path, int defaultValue, int min, int max, String description) {
        if (min > max || defaultValue < min || defaultValue > max) {
            throw new IllegalArgumentException("config key " + path + ": default " + defaultValue + " is outside " + min + ".." + max);
        }
        return new ConfigKey<>(path, Type.INT, defaultValue, min, max, List.of(), description);
    }

    public static ConfigKey<Double> decimal(String path, double defaultValue, double min, double max, String description) {
        if (Double.isNaN(defaultValue) || Double.isNaN(min) || Double.isNaN(max) || min > max || defaultValue < min || defaultValue > max) {
            throw new IllegalArgumentException("config key " + path + ": default " + defaultValue + " is outside " + min + ".." + max);
        }
        return new ConfigKey<>(path, Type.DOUBLE, defaultValue, min, max, List.of(), description);
    }

    public static ConfigKey<Boolean> bool(String path, boolean defaultValue, String description) {
        return new ConfigKey<>(path, Type.BOOLEAN, defaultValue, null, null, List.of(), description);
    }

    public static ConfigKey<String> choice(String path, String defaultValue, List<String> allowed, String description) {
        List<String> canonical = List.copyOf(allowed);
        if (canonical.isEmpty() || !canonical.contains(defaultValue)) {
            throw new IllegalArgumentException("config key " + path + ": default " + defaultValue + " is not one of " + allowed);
        }
        return new ConfigKey<>(path, Type.CHOICE, defaultValue, null, null, canonical, description);
    }

    public String path() {
        return path;
    }

    public Type type() {
        return type;
    }

    public T defaultValue() {
        return defaultValue;
    }

    /** Smallest allowed value, or null for booleans and choices. */
    public T min() {
        return min;
    }

    /** Largest allowed value, or null for booleans and choices. */
    public T max() {
        return max;
    }

    public List<String> choices() {
        return choices;
    }

    public String description() {
        return description;
    }

    /** The value the running server uses: the file's value if it is valid, otherwise the default. */
    @SuppressWarnings("unchecked")
    public T get() {
        Object value = Config.active().valueOf(this);
        return (T) (value == null ? defaultValue : value);
    }

    /**
     * Checks a raw value from the file. Returns the value to use and, through {@code warnings}, why it was
     * changed; a value that cannot be used at all becomes the default.
     */
    @SuppressWarnings("unchecked")
    T coerce(Object raw, List<String> warnings) {
        switch (type) {
            case INT -> {
                if (!(raw instanceof Number n) || n.doubleValue() != Math.rint(n.doubleValue())) {
                    warnings.add(path + ": expected a whole number, got " + raw + "; using the default " + defaultValue);
                    return defaultValue;
                }
                long whole = n.longValue();
                int lo = (Integer) min;
                int hi = (Integer) max;
                if (whole < lo || whole > hi) {
                    int clamped = (int) Math.max(lo, Math.min(hi, whole));
                    warnings.add(path + ": " + whole + " is outside " + lo + ".." + hi + "; using " + clamped);
                    return (T) Integer.valueOf(clamped);
                }
                return (T) Integer.valueOf((int) whole);
            }
            case DOUBLE -> {
                if (!(raw instanceof Number n) || Double.isNaN(n.doubleValue()) || Double.isInfinite(n.doubleValue())) {
                    warnings.add(path + ": expected a number, got " + raw + "; using the default " + defaultValue);
                    return defaultValue;
                }
                double lo = (Double) min;
                double hi = (Double) max;
                double v = n.doubleValue();
                if (v < lo || v > hi) {
                    double clamped = Math.max(lo, Math.min(hi, v));
                    warnings.add(path + ": " + v + " is outside " + lo + ".." + hi + "; using " + clamped);
                    return (T) Double.valueOf(clamped);
                }
                return (T) Double.valueOf(v);
            }
            case BOOLEAN -> {
                if (!(raw instanceof Boolean b)) {
                    warnings.add(path + ": expected true or false, got " + raw + "; using the default " + defaultValue);
                    return defaultValue;
                }
                return (T) b;
            }
            case CHOICE -> {
                if (raw instanceof String s) {
                    for (String allowed : choices) {
                        if (allowed.equals(s.toLowerCase(Locale.ROOT)) || allowed.equalsIgnoreCase(s)) {
                            return (T) allowed;
                        }
                    }
                }
                warnings.add(path + ": " + raw + " is not one of " + choices + "; using the default " + defaultValue);
                return defaultValue;
            }
            default -> throw new IllegalStateException("unknown config type " + type);
        }
    }

    @Override
    public String toString() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ConfigKey<?> other && other.path.equals(path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }
}
