package com.political.power.store;

import java.util.Map;
import java.util.Objects;

/** Append-only reset recovery records. Quarantine keys are opaque strings in the existing schema. */
public final class PowerArchives {
    private PowerArchives() {}

    public static String archiveReset(Map<String, String> quarantine, String uuid, long timestamp, String encoded) {
        Objects.requireNonNull(encoded, "encoded recovery state");
        String prefix = uuid + ".reset." + timestamp;
        String key = prefix;
        long suffix = 1;
        while (quarantine.containsKey(key)) key = prefix + "." + suffix++;
        quarantine.put(key, encoded);
        return key;
    }
}
