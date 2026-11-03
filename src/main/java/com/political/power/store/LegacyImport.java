package com.political.power.store;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.political.SchoolTree;
import com.political.magic.MagicSchool;
import com.political.power.model.AbilitySpec;
import com.political.power.model.PlayerPower;
import com.political.power.model.PowerCatalog;
import com.political.power.model.PowerData;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Builds the new power data from the three old files (M2.4): {@code techniques.json} (the eight
 * techniques), {@code magic_data.json} (mana, level, spells) and {@code magic_schools.json} (shards and
 * school tree nodes).
 *
 * <p><b>Nothing is dropped silently.</b> Whatever maps cleanly moves into the new model. Whatever does not
 * (spells, skill tree nodes, unknown technique names, an invalid selected ability) is copied into the
 * quarantine section as JSON text and counted in the report. The only things left behind are timing values
 * that mean nothing after a restart (cooldown timestamps and the energy clock), and the report says so.
 * The old files themselves are never touched; the caller copies them aside.</p>
 */
public final class LegacyImport {

    /** The result of an import. */
    public record Result(PowerData data, List<String> report) {
    }

    private final PowerData data = new PowerData();
    private final List<String> report = new ArrayList<>();
    private int quarantined;
    private int droppedTransient;

    private LegacyImport() {
    }

    /** Any of the three roots may be null when that file does not exist. */
    public static Result run(JsonObject techniques, JsonObject magicData, JsonObject magicSchools) {
        LegacyImport in = new LegacyImport();
        if (techniques != null) {
            in.importTechniques(techniques);
        }
        if (magicData != null) {
            in.importMagicData(magicData);
        }
        if (magicSchools != null) {
            in.importSchools(magicSchools);
        }
        for (PlayerPower p : in.data.players.values()) {
            in.chooseSchool(p);
        }
        in.report.add("imported " + in.data.players.size() + " players, quarantined " + in.quarantined
                + " old value(s), left behind " + in.droppedTransient + " timing value(s) (cooldowns and the energy clock, "
                + "which mean nothing after a restart)");
        return new Result(in.data, in.report);
    }

    // ── techniques.json ─────────────────────────────────────────────────────────────────────

    private void importTechniques(JsonObject root) {
        JsonObject players = obj(root.get("players"));
        if (players == null) {
            report.add("techniques.json has no players object; nothing imported from it");
            return;
        }
        for (Map.Entry<String, JsonElement> entry : players.entrySet()) {
            JsonObject old = obj(entry.getValue());
            if (old == null) {
                quarantine(entry.getKey(), "techniques_entry", entry.getValue());
                continue;
            }
            String uuid = entry.getKey();
            PlayerPower p = data.player(uuid);

            // learned
            List<String> unknownLearned = new ArrayList<>();
            for (String name : strings(old.get("learned"))) {
                if (isOlderTechnique(name)) {
                    p.learned.add(name);
                } else {
                    unknownLearned.add(name);
                }
            }
            if (!unknownLearned.isEmpty()) {
                quarantine(uuid, "learned", toArray(unknownLearned));
            }

            // the awakening-era single technique field, migrated the way the old code did
            String equipped = str(old.get("equipped"));
            String awakening = str(old.get("technique"));
            if (equipped == null && awakening != null) {
                if (isOlderTechnique(awakening)) {
                    p.learned.add(awakening);
                    equipped = awakening;
                } else {
                    quarantine(uuid, "technique", old.get("technique"));
                }
            }
            if (equipped != null) {
                if (isOlderTechnique(equipped)) {
                    p.learned.add(equipped);
                    p.equipped = equipped;
                } else {
                    quarantine(uuid, "equipped", old.get("equipped"));
                }
            }

            // selected ability
            String selected = str(old.get("selectedAbility"));
            if (selected != null) {
                if (p.equipped != null && abilityBelongsTo(selected, p.equipped)) {
                    p.selectedAbility = selected;
                } else {
                    quarantine(uuid, "selectedAbility", old.get("selectedAbility"));
                }
            }

            // uses per technique
            JsonObject uses = obj(old.get("usesByTechnique"));
            JsonObject unknownUses = new JsonObject();
            if (uses != null) {
                for (Map.Entry<String, JsonElement> use : uses.entrySet()) {
                    int count = intValue(use.getValue());
                    if (isOlderTechnique(use.getKey())) {
                        p.uses.merge(use.getKey(), count, Integer::sum);
                    } else {
                        unknownUses.add(use.getKey(), use.getValue());
                    }
                }
            }
            if (unknownUses.size() > 0) {
                quarantine(uuid, "usesByTechnique", unknownUses);
            }

            // awakening-era per ability counts: summed into the equipped technique, as the old code did
            JsonObject abilityUses = obj(old.get("abilityUses"));
            if (abilityUses != null && abilityUses.size() > 0) {
                int total = 0;
                for (JsonElement count : abilityUses.asMap().values()) {
                    total += intValue(count);
                }
                if (p.equipped != null) {
                    p.uses.merge(p.equipped, total, Integer::sum);
                } else {
                    quarantine(uuid, "abilityUses", abilityUses);
                }
            }

            // the resource: technique energy, when the file has it
            if (old.has("energy") && old.get("energy").isJsonPrimitive()) {
                p.resource = old.get("energy").getAsDouble();
            }

            // timing values that mean nothing after a restart
            for (String transientField : List.of("cooldowns", "lastEnergyMs")) {
                if (old.has(transientField) && !isEmpty(old.get(transientField))) {
                    droppedTransient++;
                }
            }
        }
    }

    // ── magic_data.json ─────────────────────────────────────────────────────────────────────

    private void importMagicData(JsonObject root) {
        for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
            JsonObject old = obj(entry.getValue());
            if (old == null) {
                quarantine(entry.getKey(), "magic_entry", entry.getValue());
                continue;
            }
            String uuid = entry.getKey();
            PlayerPower p = data.player(uuid);
            if (old.has("level")) {
                p.level = Math.max(1, intValue(old.get("level")));
            }
            if (old.has("xp")) {
                p.xp = Math.max(0, intValue(old.get("xp")));
            }
            if (old.has("mana") && p.resource < 0 && old.get("mana").isJsonPrimitive()) {
                p.resource = old.get("mana").getAsDouble();
            }
            for (Map.Entry<String, JsonElement> field : old.entrySet()) {
                String name = field.getKey();
                if (name.equals("level") || name.equals("xp") || name.equals("mana")) {
                    continue;
                }
                if (!isEmpty(field.getValue())) {
                    quarantine(uuid, name, field.getValue());
                }
            }
        }
    }

    // ── magic_schools.json ──────────────────────────────────────────────────────────────────

    private void importSchools(JsonObject root) {
        for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
            JsonObject old = obj(entry.getValue());
            if (old == null) {
                quarantine(entry.getKey(), "school_entry", entry.getValue());
                continue;
            }
            String uuid = entry.getKey();
            PlayerPower p = data.player(uuid);
            p.shards = Math.max(0, intValue(old.get("shards")));
            p.shardsEarnedTotal = Math.max(p.shards, intValue(old.get("shardsEarnedTotal")));
            List<String> unknownNodes = new ArrayList<>();
            for (String node : strings(old.get("unlocked"))) {
                if (SchoolTree.getNode(node) != null) {
                    p.unlockedNodes.add(node);
                } else {
                    unknownNodes.add(node);
                }
            }
            if (!unknownNodes.isEmpty()) {
                quarantine(uuid, "unlocked", toArray(unknownNodes));
            }
        }
    }

    // ── choosing a school ───────────────────────────────────────────────────────────────────

    /**
     * A player who unlocked nodes gets the selectable school where they unlocked the most (ties go to the
     * school listed first). Nodes of other schools stay unlocked; they are simply not the active school.
     */
    private void chooseSchool(PlayerPower p) {
        if (p.school != null || p.unlockedNodes.isEmpty()) {
            return;
        }
        String best = null;
        int bestCount = 0;
        Set<String> selectable = PowerCatalog.selectableSchools();
        for (MagicSchool school : MagicSchool.values()) {
            if (!selectable.contains(school.name())) {
                continue;
            }
            int count = 0;
            for (SchoolTree.SchoolNode node : SchoolTree.nodesFor(school)) {
                if (p.unlockedNodes.contains(node.id())) {
                    count++;
                }
            }
            if (count > bestCount) {
                best = school.name();
                bestCount = count;
            }
        }
        p.school = best;
    }

    // ── helpers ─────────────────────────────────────────────────────────────────────────────

    private static boolean isOlderTechnique(String name) {
        return PowerCatalog.technique(name).map(PowerCatalog.TechniqueDef::legacy).orElse(false);
    }

    private static boolean abilityBelongsTo(String abilityId, String techniqueId) {
        for (AbilitySpec spec : PowerCatalog.abilities(techniqueId)) {
            if (spec.id().equals(abilityId)) {
                return true;
            }
        }
        return false;
    }

    private void quarantine(String uuid, String field, JsonElement value) {
        data.quarantine.put(uuid + "." + field, value == null ? "null" : value.toString());
        quarantined++;
    }

    private static JsonObject obj(JsonElement e) {
        return e != null && e.isJsonObject() ? e.getAsJsonObject() : null;
    }

    private static String str(JsonElement e) {
        return e != null && e.isJsonPrimitive() && e.getAsJsonPrimitive().isString() ? e.getAsString() : null;
    }

    private static int intValue(JsonElement e) {
        try {
            return e != null && e.isJsonPrimitive() ? e.getAsInt() : 0;
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private static List<String> strings(JsonElement e) {
        Set<String> out = new TreeSet<>();
        if (e != null && e.isJsonArray()) {
            for (JsonElement item : e.getAsJsonArray()) {
                if (item.isJsonPrimitive()) {
                    out.add(item.getAsString());
                }
            }
        }
        return new ArrayList<>(out);
    }

    private static JsonArray toArray(List<String> values) {
        JsonArray array = new JsonArray();
        for (String v : values) {
            array.add(v);
        }
        return array;
    }

    /** null, an empty array or object, zero, false or an empty string: nothing worth keeping. */
    private static boolean isEmpty(JsonElement e) {
        if (e == null || e.isJsonNull()) {
            return true;
        }
        if (e.isJsonArray()) {
            return e.getAsJsonArray().isEmpty();
        }
        if (e.isJsonObject()) {
            return e.getAsJsonObject().size() == 0;
        }
        if (e.isJsonPrimitive()) {
            if (e.getAsJsonPrimitive().isBoolean()) {
                return !e.getAsBoolean();
            }
            if (e.getAsJsonPrimitive().isNumber()) {
                return e.getAsDouble() == 0.0;
            }
            return e.getAsString().isEmpty();
        }
        return false;
    }
}
