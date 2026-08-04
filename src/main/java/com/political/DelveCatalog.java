package com.political;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * The room catalogue: what exists, how big it is, and which floor it belongs to.
 *
 * <p>Seven hundred rooms came out of three structure packs, so the list is a resource rather than a
 * class file. The manifest carries each room's measured footprint alongside its id, which matters
 * because the layout has to know how much space a room needs <em>before</em> deciding where to put
 * it, and asking the game to load seven hundred templates to find that out would make opening the
 * menu a visible pause.</p>
 *
 * <p>The measurements were taken from the structure files themselves rather than guessed. Room
 * sizes across these packs run from nine blocks square to forty eight, which is exactly the sort of
 * thing that produces a broken delve if you assume a grid and hope.</p>
 */
public final class DelveCatalog {

    private DelveCatalog() {}

    /** One room, as the manifest describes it. */
    public record Room(String id, String name, int x, int y, int z, int blocks, String source) {

        public Identifier identifier() {
            return Identifier.parse(id);
        }

        /** The larger of the two horizontal extents, which is what the grid has to fit. */
        public int footprint() {
            return Math.max(x, z);
        }

        /** Where this room came from, for the credit line in the menu. */
        public String credit() {
            return switch (source) {
                case "epic" -> "Custom Delves";
                case "uw" -> "Underground Worlds";
                case "dnt" -> "Delves and Taverns";
                default -> source;
            };
        }
    }

    private static final Map<String, List<Room>> BY_POOL = new HashMap<>();
    private static boolean loaded = false;

    /**
     * Reads the manifest once.
     *
     * <p>Deliberately tolerant. A pool that fails to parse leaves that floor empty and says so in
     * the log rather than stopping the server, because a delve that will not open is a smaller
     * problem than a server that will not start.</p>
     */
    public static void load() {
        if (loaded) return;
        loaded = true;
        BY_POOL.clear();

        try (InputStream in = DelveCatalog.class.getClassLoader()
                .getResourceAsStream("data/politicalserver/dungeon_rooms.json")) {
            if (in == null) {
                PoliticalServer.LOGGER.error("Delve rooms manifest is missing from the jar");
                return;
            }
            JsonElement root = JsonParser.parseReader(
                    new InputStreamReader(in, StandardCharsets.UTF_8));
            if (!root.isJsonObject()) {
                PoliticalServer.LOGGER.error("Delve rooms manifest is not an object");
                return;
            }
            int total = 0;
            for (Map.Entry<String, JsonElement> e : root.getAsJsonObject().entrySet()) {
                List<Room> rooms = new ArrayList<>();
                if (!e.getValue().isJsonArray()) continue;
                for (JsonElement el : e.getValue().getAsJsonArray()) {
                    if (!el.isJsonObject()) continue;
                    JsonObject o = el.getAsJsonObject();
                    rooms.add(new Room(
                            o.get("id").getAsString(),
                            o.get("name").getAsString(),
                            o.get("x").getAsInt(),
                            o.get("y").getAsInt(),
                            o.get("z").getAsInt(),
                            o.has("blocks") ? o.get("blocks").getAsInt() : 0,
                            o.has("source") ? o.get("source").getAsString() : "?"));
                }
                BY_POOL.put(e.getKey(), rooms);
                total += rooms.size();
            }
            PoliticalServer.LOGGER.info("Delve rooms: " + total + " across "
                    + BY_POOL.size() + " pools");
        } catch (Exception ex) {
            PoliticalServer.LOGGER.error("Could not read the delve rooms manifest: "
                    + ex.getMessage());
        }
    }

    public static List<Room> pool(String key) {
        load();
        return BY_POOL.getOrDefault(key, Collections.emptyList());
    }

    /**
     * Everything a floor may lay out: its own pool plus the shared one.
     *
     * <p>The shared pool exists so no floor is ever short. Ice ships thirty nine rooms of its own,
     * which is enough for one run but not enough for two runs to feel different.</p>
     */
    public static List<Room> roomsFor(DelveDepth floor) {
        List<Room> out = new ArrayList<>(pool(floor.pool));
        out.addAll(pool("shared"));
        return out;
    }

    public static int count() {
        load();
        int n = 0;
        for (List<Room> v : BY_POOL.values()) n += v.size();
        return n;
    }

    public static int countFor(DelveDepth floor) {
        return roomsFor(floor).size();
    }

    /**
     * Picks rooms for one run, largest first and without repeats.
     *
     * <p>Largest first because the layout packs big rooms into the grid before small ones, and a
     * run that placed a nine block room in a forty eight block cell before discovering it needed
     * the space would have to start over.</p>
     */
    public static List<Room> draw(DelveDepth floor, int want, Random rng) {
        List<Room> all = new ArrayList<>(roomsFor(floor));
        if (all.isEmpty()) return all;
        Collections.shuffle(all, rng);
        List<Room> picked = new ArrayList<>();
        for (Room r : all) {
            if (picked.size() >= want) break;
            picked.add(r);
        }
        picked.sort((a, b) -> Integer.compare(b.footprint(), a.footprint()));
        return picked;
    }

    /**
     * Loads a template, or null when the structure is missing.
     *
     * <p>Null rather than a throw. A manifest that lists a room the jar does not carry should cost
     * that one room, not the whole delve.</p>
     */
    public static StructureTemplate template(MinecraftServer server, Room room) {
        try {
            return server.getStructureManager().get(room.identifier()).orElse(null);
        } catch (Exception e) {
            PoliticalServer.LOGGER.warn("Delve room would not load: " + room.id());
            return null;
        }
    }
}
