package com.political.core.config;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Every setting the mod has. Settings are declared as {@code public static final ConfigKey<...>} fields
 * in holder classes, and each holder is listed once in {@link #HOLDERS}. Listing the class is all it
 * takes: the keys are found by reflection, so none can be missed by the config reference or by the
 * unknown-setting check.
 */
public final class ConfigCatalog {

    /**
     * Add new holder classes here, by name, so the core package does not depend on the gameplay packages
     * that own the settings.
     */
    private static final List<String> HOLDERS = List.of(
            "com.political.core.config.CoreSettings",
            "com.political.core.config.FeatureSettings",
            "com.political.power.PowerSettings");

    private ConfigCatalog() {
    }

    /** All keys, sorted by path. */
    public static List<ConfigKey<?>> keys() {
        List<ConfigKey<?>> keys = new ArrayList<>();
        for (String holderName : HOLDERS) {
            Class<?> holder;
            try {
                holder = Class.forName(holderName);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("config holder class not found: " + holderName, e);
            }
            for (Field field : holder.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) && ConfigKey.class.isAssignableFrom(field.getType())) {
                    try {
                        field.setAccessible(true);
                        keys.add((ConfigKey<?>) field.get(null));
                    } catch (IllegalAccessException e) {
                        throw new IllegalStateException("cannot read config key " + holder.getName() + "." + field.getName(), e);
                    }
                }
            }
        }
        keys.sort(Comparator.comparing(ConfigKey::path));
        return keys;
    }
}
