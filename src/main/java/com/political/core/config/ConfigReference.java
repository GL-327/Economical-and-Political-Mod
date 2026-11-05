package com.political.core.config;

import java.util.List;
import java.util.Locale;

/** Renders the config reference (docs/CONFIG.md) from the declared keys, so it can never drift from the code. */
public final class ConfigReference {

    private ConfigReference() {
    }

    public static String markdown(List<ConfigKey<?>> keys) {
        StringBuilder out = new StringBuilder();
        out.append("# Configuration reference\n\n");
        out.append("Generated from the code by `ConfigReferenceTest`. Do not edit by hand: change the key's description in\n");
        out.append("its settings class and run `./gradlew test -Dpolitical.updateDocs=true`.\n\n");
        out.append("The file is `config/political/config.json` in the server directory. It is created with every default the\n");
        out.append("first time the mod starts. Each dot in a path below is one level of nesting, so `tick.budget.server_ms` is\n");
        out.append("`{\"tick\": {\"budget\": {\"server_ms\": 5.0}}}`. A missing setting uses its default; a wrong type uses the\n");
        out.append("default; a number outside its range is clamped; unknown settings are logged and ignored. A broken file never\n");
        out.append("stops the server. Settings are read at startup.\n");
        String section = "";
        for (ConfigKey<?> key : keys) {
            String top = key.path().substring(0, key.path().indexOf('.'));
            if (!top.equals(section)) {
                section = top;
                out.append("\n## ").append(section).append("\n\n");
                out.append("| Setting | Type | Default | Allowed | What it does |\n");
                out.append("| --- | --- | --- | --- | --- |\n");
            }
            out.append("| `").append(key.path()).append("` | ")
                    .append(key.type().name().toLowerCase(Locale.ROOT)).append(" | `")
                    .append(key.defaultValue()).append("` | ")
                    .append(allowed(key)).append(" | ")
                    .append(key.description().replace("|", "\\|")).append(" |\n");
        }
        return out.toString();
    }

    private static String allowed(ConfigKey<?> key) {
        return switch (key.type()) {
            case INT, DOUBLE -> key.min() + " to " + key.max();
            case BOOLEAN -> "true or false";
            case CHOICE -> String.join(", ", key.choices());
        };
    }
}
