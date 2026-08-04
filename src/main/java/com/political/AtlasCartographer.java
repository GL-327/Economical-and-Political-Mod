package com.political;

import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.material.MapColor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Draws the Atlas.
 *
 * <p>The Atlas used to be a grid of glass panes pretending to be a map. This draws a real one: a
 * 128x128 image, painted a pixel at a time into a locked vanilla map, which the player then holds
 * in their hand and reads like any other map.</p>
 *
 * <p>The picture is built in two layers. The land underneath comes from the biome source, sampled
 * directly rather than from loaded chunks, so a map can cover two thousand blocks of world the
 * player has never walked without generating a single chunk. The politics on top — territory,
 * roads, wars, names — are drawn from the settlement records.</p>
 *
 * <p>The land layer is the slow half and never changes, so it is rendered off the server thread and
 * cached. The politics layer is cheap and changes constantly, so it is repainted every time.</p>
 */
public final class AtlasCartographer {

    private AtlasCartographer() {}

    /** Every vanilla map is this wide and this tall. Nothing here is configurable for that reason. */
    public static final int SIZE = 128;

    // ── Scales ────────────────────────────────────────────────────────────

    /**
     * The three useful zooms.
     *
     * <p>Anything tighter than four blocks a pixel is drawing biome data at a resolution biome data
     * does not have, so it is left out rather than offered and disappointing.</p>
     */
    public enum Zoom {
        LOCAL(2, "Local Chart", "Your own ground, close in"),
        REGION(3, "Regional Map", "Your neighbours and the roads between"),
        REALM(4, "Realm Atlas", "Everything anybody has founded");

        /** Vanilla map scale byte: blocks per pixel is 1 << scale. */
        public final byte scale;
        public final String display;
        public final String blurb;

        Zoom(int scale, String display, String blurb) {
            this.scale = (byte) scale;
            this.display = display;
            this.blurb = blurb;
        }

        public int blocksPerPixel() {
            return 1 << scale;
        }

        /** How many blocks across the whole picture covers. */
        public int span() {
            return blocksPerPixel() * SIZE;
        }
    }

    // ── The land cache ────────────────────────────────────────────────────

    /**
     * Rendered land, keyed by dimension, centre and zoom.
     *
     * <p>Biomes do not move, so a base that has been drawn once is good forever. The cap is small
     * because each entry is sixteen kilobytes and a busy server would otherwise accumulate one per
     * player per zoom per place they have ever stood.</p>
     */
    private static final Map<String, byte[]> LAND_CACHE =
            new LinkedHashMap<>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, byte[]> eldest) {
                    return size() > 48;
                }
            };

    /** Keys currently being drawn, so two players asking at once only draws once. */
    private static final java.util.Set<String> IN_FLIGHT = java.util.Collections.synchronizedSet(
            new java.util.HashSet<>());

    /**
     * One background thread. Drawing a base is a fraction of a second and nobody is waiting on it
     * with a stopwatch, so a pool would be more machinery than the job needs.
     */
    private static final java.util.concurrent.ExecutorService DRAFTSMAN =
            java.util.concurrent.Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "atlas-cartographer");
                t.setDaemon(true);
                return t;
            });

    private static String key(ServerLevel level, int cx, int cz, Zoom zoom) {
        return level.dimension().identifier() + "@" + cx + "," + cz + "/" + zoom.scale;
    }

    /**
     * Hands back a finished picture.
     *
     * <p>If the land underneath has already been drawn the callback fires immediately on the calling
     * thread. If it has not, the land is drawn in the background and the callback fires on the
     * server thread once it is ready, which is why this takes a callback rather than returning.</p>
     */
    public static void draw(ServerLevel level, int cx, int cz, Zoom zoom, ServerPlayer viewer,
                            java.util.function.Consumer<byte[]> onReady) {
        String k = key(level, cx, cz, zoom);
        byte[] land;
        synchronized (LAND_CACHE) {
            land = LAND_CACHE.get(k);
        }
        if (land != null) {
            onReady.accept(paintPolitics(land, level, cx, cz, zoom, viewer));
            return;
        }

        if (!IN_FLIGHT.add(k)) {
            // Somebody else is already drawing this exact picture. Give them the parchment now and
            // let them ask again rather than queue a second identical render.
            onReady.accept(paintPolitics(parchment(), level, cx, cz, zoom, viewer));
            return;
        }

        DRAFTSMAN.submit(() -> {
            byte[] drawn;
            try {
                drawn = renderLand(level, cx, cz, zoom);
            } catch (Throwable t) {
                // A biome source that will not answer off-thread is not worth crashing a map over.
                PoliticalServer.LOGGER.warn("Atlas could not read the land: " + t);
                drawn = parchment();
            }
            final byte[] finished = drawn;
            synchronized (LAND_CACHE) {
                LAND_CACHE.put(k, finished);
            }
            IN_FLIGHT.remove(k);

            if (PoliticalServer.server != null) {
                PoliticalServer.server.execute(() ->
                        onReady.accept(paintPolitics(finished, level, cx, cz, zoom, viewer)));
            }
        });
    }

    /** Throws away the cached land for a dimension, so a world change can be re-surveyed. */
    public static void forget() {
        synchronized (LAND_CACHE) {
            LAND_CACHE.clear();
        }
    }

    // ── Layer one: the land ───────────────────────────────────────────────

    /**
     * Paints the ground from the biome source.
     *
     * <p>Runs off the server thread. It reads the generator's biome source and nothing else, which
     * is the same thing worldgen threads do, and it never touches a chunk or an entity.</p>
     */
    private static byte[] renderLand(ServerLevel level, int cx, int cz, Zoom zoom) {
        byte[] img = new byte[SIZE * SIZE];
        int bpp = zoom.blocksPerPixel();
        int half = SIZE / 2;

        // Which biome each pixel sits in. Kept separately from the colours so the coastline pass
        // below can ask "is my neighbour wet" without unpicking a packed colour byte.
        boolean[] wet = new boolean[SIZE * SIZE];
        Map<Long, String> seen = new HashMap<>();

        for (int py = 0; py < SIZE; py++) {
            int worldZ = cz + (py - half) * bpp;
            for (int px = 0; px < SIZE; px++) {
                int worldX = cx + (px - half) * bpp;

                int qx = QuartPos.fromBlock(worldX);
                int qz = QuartPos.fromBlock(worldZ);
                long qk = (((long) qx) << 32) ^ (qz & 0xFFFFFFFFL);

                String biome = seen.get(qk);
                if (biome == null) {
                    biome = nameOf(level, qx, qz);
                    seen.put(qk, biome);
                }

                int i = py * SIZE + px;
                wet[i] = isWater(biome);
                img[i] = groundColour(biome, worldX, worldZ);
            }
        }

        // A coastline. One darker pixel wherever land meets water is the single cheapest thing that
        // makes a coloured blob read as a map.
        byte[] out = img.clone();
        for (int py = 0; py < SIZE; py++) {
            for (int px = 0; px < SIZE; px++) {
                int i = py * SIZE + px;
                if (wet[i]) continue;
                boolean shore = (px > 0 && wet[i - 1])
                        || (px < SIZE - 1 && wet[i + 1])
                        || (py > 0 && wet[i - SIZE])
                        || (py < SIZE - 1 && wet[i + SIZE]);
                if (shore) out[i] = MapColor.COLOR_BROWN.getPackedId(MapColor.Brightness.LOW);
            }
        }
        return out;
    }

    private static String nameOf(ServerLevel level, int qx, int qz) {
        try {
            // Sea level in quarts. Sampling here rather than at the surface keeps oceans reading as
            // oceans instead of as whatever cave biome sits beneath them.
            var holder = level.getUncachedNoiseBiome(qx, QuartPos.fromBlock(64), qz);
            ResourceKey<Biome> key = holder.unwrapKey().orElse(null);
            return key == null ? "plains" : key.identifier().getPath();
        } catch (Throwable t) {
            return "plains";
        }
    }

    private static boolean isWater(String b) {
        return b.contains("ocean") || b.contains("river");
    }

    /**
     * A biome's colour on the map.
     *
     * <p>Matched on the name rather than on a registry lookup so that biomes added by this mod, or
     * by anything else installed alongside it, land somewhere sensible instead of grey.</p>
     */
    private static byte groundColour(String b, int worldX, int worldZ) {
        MapColor colour;
        MapColor.Brightness bright = MapColor.Brightness.NORMAL;

        if (b.contains("frozen_ocean") || b.contains("frozen_river")) {
            colour = MapColor.ICE;
        } else if (b.contains("deep") && b.contains("ocean")) {
            colour = MapColor.WATER;
            bright = MapColor.Brightness.LOW;
        } else if (b.contains("ocean")) {
            colour = MapColor.WATER;
        } else if (b.contains("river")) {
            colour = MapColor.WATER;
            bright = MapColor.Brightness.HIGH;
        } else if (b.contains("beach") || b.contains("shore")) {
            colour = MapColor.SAND;
            bright = MapColor.Brightness.HIGH;
        } else if (b.contains("desert")) {
            colour = MapColor.SAND;
        } else if (b.contains("badlands") || b.contains("mesa")) {
            colour = MapColor.TERRACOTTA_ORANGE;
        } else if (b.contains("mushroom")) {
            colour = MapColor.COLOR_PURPLE;
        } else if (b.contains("cherry")) {
            colour = MapColor.COLOR_PINK;
        } else if (b.contains("swamp") || b.contains("mangrove")) {
            colour = MapColor.COLOR_GREEN;
            bright = MapColor.Brightness.LOW;
        } else if (b.contains("jungle") || b.contains("bamboo")) {
            colour = MapColor.COLOR_GREEN;
        } else if (b.contains("dark_forest")) {
            colour = MapColor.PODZOL;
        } else if (b.contains("taiga") || b.contains("grove") || b.contains("forest")) {
            colour = MapColor.PLANT;
        } else if (b.contains("snow") || b.contains("ice") || b.contains("frozen")
                || b.contains("peaks")) {
            colour = MapColor.SNOW;
        } else if (b.contains("stony") || b.contains("windswept") || b.contains("slope")
                || b.contains("mountain")) {
            colour = MapColor.STONE;
        } else if (b.contains("savanna")) {
            colour = MapColor.GRASS;
            bright = MapColor.Brightness.LOW;
        } else if (b.contains("crimson")) {
            colour = MapColor.CRIMSON_NYLIUM;
        } else if (b.contains("warped")) {
            colour = MapColor.WARPED_NYLIUM;
        } else if (b.contains("basalt") || b.contains("soul")) {
            colour = MapColor.COLOR_BLACK;
        } else if (b.contains("nether")) {
            colour = MapColor.NETHER;
        } else if (b.contains("end") || b.contains("void")) {
            colour = MapColor.SAND;
            bright = MapColor.Brightness.LOW;
        } else if (b.contains("lush") || b.contains("dripstone") || b.contains("cave")
                || b.contains("dark")) {
            colour = MapColor.DIRT;
        } else {
            colour = MapColor.GRASS;
        }

        // A whisper of noise so a wide plain does not read as a flat rectangle of one colour. It is
        // deterministic on world position, so the same ground draws the same way every time.
        if (bright == MapColor.Brightness.NORMAL && noise(worldX, worldZ)) {
            bright = MapColor.Brightness.HIGH;
        }
        return colour.getPackedId(bright);
    }

    private static boolean noise(int x, int z) {
        int h = x * 374_761_393 + z * 668_265_263;
        h = (h ^ (h >>> 13)) * 1_274_126_177;
        return ((h ^ (h >>> 16)) & 7) == 0;
    }

    /** Blank paper, for when the land cannot be read. */
    private static byte[] parchment() {
        byte[] img = new byte[SIZE * SIZE];
        byte pale = MapColor.SAND.getPackedId(MapColor.Brightness.HIGH);
        java.util.Arrays.fill(img, pale);
        return img;
    }

    // ── Layer two: the politics ───────────────────────────────────────────

    /**
     * Draws everything that can change between one glance at the map and the next: who owns what,
     * who is at war, where the roads run, and where the reader is standing.
     */
    private static byte[] paintPolitics(byte[] land, ServerLevel level, int cx, int cz,
                                        Zoom zoom, ServerPlayer viewer) {
        byte[] img = land.clone();
        Canvas c = new Canvas(img);
        int bpp = zoom.blocksPerPixel();
        int half = SIZE / 2;
        String dim = level.dimension().identifier().toString();

        List<Settlement> here = new ArrayList<>();
        for (Settlement s : SettlementManager.getAllSettlements()) {
            if (!dim.equals(s.worldDim)) continue;
            int px = half + Math.floorDiv(s.cornerstoneX - cx, bpp);
            int py = half + Math.floorDiv(s.cornerstoneZ - cz, bpp);
            if (px < -32 || px > SIZE + 32 || py < -32 || py > SIZE + 32) continue;
            here.add(s);
        }

        // Roads first, so a settlement's own mark sits on top of the line rather than under it.
        for (Settlement s : here) {
            if (s.nearestMetropolisId == null) continue;
            Settlement metro = SettlementManager.get(s.nearestMetropolisId);
            if (metro == null || !dim.equals(metro.worldDim)) continue;
            c.dottedLine(half + Math.floorDiv(s.cornerstoneX - cx, bpp),
                    half + Math.floorDiv(s.cornerstoneZ - cz, bpp),
                    half + Math.floorDiv(metro.cornerstoneX - cx, bpp),
                    half + Math.floorDiv(metro.cornerstoneZ - cz, bpp),
                    MapColor.COLOR_BROWN.getPackedId(MapColor.Brightness.NORMAL), 3);
        }

        // War fronts. A solid red line between two capitals is legible at a glance in a way that a
        // list of active wars in a tooltip never is.
        byte warRed = MapColor.FIRE.getPackedId(MapColor.Brightness.NORMAL);
        for (WarManager.War war : WarManager.activeWars()) {
            Settlement a = SettlementManager.get(war.aId);
            Settlement b = SettlementManager.get(war.bId);
            if (a == null || b == null) continue;
            if (!dim.equals(a.worldDim) || !dim.equals(b.worldDim)) continue;
            c.line(half + Math.floorDiv(a.cornerstoneX - cx, bpp),
                    half + Math.floorDiv(a.cornerstoneZ - cz, bpp),
                    half + Math.floorDiv(b.cornerstoneX - cx, bpp),
                    half + Math.floorDiv(b.cornerstoneZ - cz, bpp),
                    warRed);
        }

        // Territory, then the mark, then the name. Biggest settlements drawn last so a metropolis
        // is never buried under the hamlet next door.
        here.sort(java.util.Comparator.comparingInt(s -> s.tierEnum().rank()));

        String nationalSeat = null;
        Settlement capital = SettlementManager.findLargestMetropolis();
        if (capital != null) nationalSeat = capital.id;

        for (Settlement s : here) {
            int px = half + Math.floorDiv(s.cornerstoneX - cx, bpp);
            int py = half + Math.floorDiv(s.cornerstoneZ - cz, bpp);
            SettlementTier tier = s.tierEnum();

            int radius = territoryRadius(tier, bpp);
            byte tint = tierColour(tier);

            // Hatched rather than solid, so the land underneath still reads through the claim. A map
            // that hides its own geography under political colour is a worse map.
            c.hatchedDisc(px, py, radius, tint);
            c.ring(px, py, radius, tint);

            boolean seat = s.id != null && s.id.equals(nationalSeat);
            c.mark(px, py, tier, seat);
        }

        // Names last of all, so nothing is drawn over them.
        for (Settlement s : here) {
            int px = half + Math.floorDiv(s.cornerstoneX - cx, bpp);
            int py = half + Math.floorDiv(s.cornerstoneZ - cz, bpp);
            String label = s.name == null ? "?" : s.name.toUpperCase(java.util.Locale.ROOT);
            if (label.length() > 14) label = label.substring(0, 14);
            int w = PixelFont.width(label);
            c.label(px - w / 2, py + territoryRadius(s.tierEnum(), bpp) + 2, label);
        }

        // The reader.
        if (viewer != null && dim.equals(
                ((ServerLevel) viewer.level()).dimension().identifier().toString())) {
            int px = half + Math.floorDiv(viewer.blockPosition().getX() - cx, bpp);
            int py = half + Math.floorDiv(viewer.blockPosition().getZ() - cz, bpp);
            c.cross(px, py, MapColor.FIRE.getPackedId(MapColor.Brightness.HIGH));
        }

        frame(c, zoom);
        return img;
    }

    /** How wide a settlement's claim is drawn, in pixels, floored so it never vanishes entirely. */
    private static int territoryRadius(SettlementTier tier, int blocksPerPixel) {
        // Roughly the ground a settlement of that size actually occupies, converted to pixels.
        int blocks = 48 + tier.rank() * 56;
        return Math.max(2, Math.min(40, blocks / blocksPerPixel));
    }

    private static byte tierColour(SettlementTier tier) {
        return switch (tier) {
            case OUTPOST -> MapColor.COLOR_GRAY.getPackedId(MapColor.Brightness.NORMAL);
            case HAMLET -> MapColor.COLOR_LIGHT_GRAY.getPackedId(MapColor.Brightness.NORMAL);
            case VILLAGE -> MapColor.COLOR_LIGHT_GREEN.getPackedId(MapColor.Brightness.NORMAL);
            case TOWN -> MapColor.COLOR_GREEN.getPackedId(MapColor.Brightness.HIGH);
            case CITY -> MapColor.COLOR_YELLOW.getPackedId(MapColor.Brightness.HIGH);
            case METROPOLIS -> MapColor.GOLD.getPackedId(MapColor.Brightness.HIGH);
            case MEGALOPOLIS -> MapColor.COLOR_RED.getPackedId(MapColor.Brightness.HIGH);
            case UTOPIA -> MapColor.COLOR_MAGENTA.getPackedId(MapColor.Brightness.HIGH);
            case ELYSIUM -> MapColor.DIAMOND.getPackedId(MapColor.Brightness.HIGH);
        };
    }

    // ── Furniture ─────────────────────────────────────────────────────────

    /** The border, the compass and the scale bar. Thin on purpose: the map is the point. */
    private static void frame(Canvas c, Zoom zoom) {
        byte ink = MapColor.COLOR_BLACK.getPackedId(MapColor.Brightness.LOW);
        byte paper = MapColor.WOOD.getPackedId(MapColor.Brightness.HIGH);

        for (int i = 0; i < SIZE; i++) {
            c.set(i, 0, paper);
            c.set(i, SIZE - 1, paper);
            c.set(0, i, paper);
            c.set(SIZE - 1, i, paper);
            c.set(i, 1, ink);
            c.set(i, SIZE - 2, ink);
            c.set(1, i, ink);
            c.set(SIZE - 2, i, ink);
        }

        // Compass, top right. An arrow and an N, which is all a map has ever needed.
        int nx = SIZE - 12;
        int ny = 5;
        c.plate(nx - 2, ny - 1, 10, 16);
        for (int i = 0; i < 8; i++) c.set(nx + 3, ny + i, ink);
        c.set(nx + 2, ny + 1, ink);
        c.set(nx + 4, ny + 1, ink);
        c.set(nx + 1, ny + 2, ink);
        c.set(nx + 5, ny + 2, ink);
        PixelFont.draw(c, "N", nx + 2, ny + 10, ink);

        // Scale bar, bottom left: one bar is a hundred blocks of real ground.
        int bar = Math.max(4, 100 / zoom.blocksPerPixel());
        int bx = 5;
        int by = SIZE - 11;
        c.plate(bx - 1, by - 1, bar + 2, 9);
        for (int i = 0; i < bar; i++) {
            c.set(bx + i, by, ink);
            c.set(bx + i, by + 2, ink);
        }
        c.set(bx, by + 1, ink);
        c.set(bx + bar - 1, by + 1, ink);
        PixelFont.draw(c, "100M", bx, by + 4, ink);
    }

    // ── The drawing board ─────────────────────────────────────────────────

    /** A 128x128 byte grid with the handful of shapes a map needs. Nothing clever, all bounds checked. */
    public static final class Canvas {
        private final byte[] px;

        Canvas(byte[] px) {
            this.px = px;
        }

        public void set(int x, int y, byte colour) {
            if (x < 0 || y < 0 || x >= SIZE || y >= SIZE) return;
            px[y * SIZE + x] = colour;
        }

        byte get(int x, int y) {
            if (x < 0 || y < 0 || x >= SIZE || y >= SIZE) return 0;
            return px[y * SIZE + x];
        }

        void line(int x0, int y0, int x1, int y1, byte colour) {
            stroke(x0, y0, x1, y1, colour, 1);
        }

        void dottedLine(int x0, int y0, int x1, int y1, byte colour, int period) {
            stroke(x0, y0, x1, y1, colour, period);
        }

        /** Bresenham, drawing every {@code period}-th pixel so one routine covers solid and dotted. */
        private void stroke(int x0, int y0, int x1, int y1, byte colour, int period) {
            int dx = Math.abs(x1 - x0);
            int dy = -Math.abs(y1 - y0);
            int sx = x0 < x1 ? 1 : -1;
            int sy = y0 < y1 ? 1 : -1;
            int err = dx + dy;
            int step = 0;
            int guard = 0;
            while (guard++ < 4096) {
                if (step % period == 0) set(x0, y0, colour);
                step++;
                if (x0 == x1 && y0 == y1) break;
                int e2 = 2 * err;
                if (e2 >= dy) {
                    err += dy;
                    x0 += sx;
                }
                if (e2 <= dx) {
                    err += dx;
                    y0 += sy;
                }
            }
        }

        /** A claim: every other pixel, so the ground underneath still shows. */
        void hatchedDisc(int cx, int cy, int r, byte colour) {
            int rr = r * r;
            for (int y = -r; y <= r; y++) {
                for (int x = -r; x <= r; x++) {
                    if (x * x + y * y > rr) continue;
                    if (((cx + x) + (cy + y)) % 2 != 0) continue;
                    set(cx + x, cy + y, colour);
                }
            }
        }

        void ring(int cx, int cy, int r, byte colour) {
            int outer = r * r;
            int inner = (r - 1) * (r - 1);
            for (int y = -r; y <= r; y++) {
                for (int x = -r; x <= r; x++) {
                    int d = x * x + y * y;
                    if (d <= outer && d >= inner) set(cx + x, cy + y, colour);
                }
            }
        }

        /** The settlement itself: a filled square, bigger and haloed for anything that matters. */
        void mark(int cx, int cy, SettlementTier tier, boolean nationalSeat) {
            byte ink = MapColor.COLOR_BLACK.getPackedId(MapColor.Brightness.LOW);
            byte fill = tierColour(tier);
            int r = tier.isAtLeast(SettlementTier.METROPOLIS) ? 3
                    : tier.isAtLeast(SettlementTier.TOWN) ? 2 : 1;

            for (int y = -r - 1; y <= r + 1; y++) {
                for (int x = -r - 1; x <= r + 1; x++) {
                    set(cx + x, cy + y, ink);
                }
            }
            for (int y = -r; y <= r; y++) {
                for (int x = -r; x <= r; x++) {
                    set(cx + x, cy + y, fill);
                }
            }
            if (nationalSeat) {
                // A crown: four points around the capital, so the seat of government is findable
                // without reading a single name.
                byte gold = MapColor.GOLD.getPackedId(MapColor.Brightness.HIGH);
                for (int i = 2; i <= 4; i++) {
                    set(cx + r + i, cy, gold);
                    set(cx - r - i, cy, gold);
                    set(cx, cy + r + i, gold);
                    set(cx, cy - r - i, gold);
                }
            }
        }

        void cross(int cx, int cy, byte colour) {
            byte ink = MapColor.COLOR_BLACK.getPackedId(MapColor.Brightness.LOW);
            for (int i = -4; i <= 4; i++) {
                set(cx + i, cy - 1, ink);
                set(cx + i, cy + 1, ink);
                set(cx - 1, cy + i, ink);
                set(cx + 1, cy + i, ink);
            }
            for (int i = -4; i <= 4; i++) {
                set(cx + i, cy, colour);
                set(cx, cy + i, colour);
            }
        }

        /** A pale rectangle to write on, so text never sits on top of the terrain it describes. */
        void plate(int x, int y, int w, int h) {
            byte paper = MapColor.WOOL.getPackedId(MapColor.Brightness.HIGH);
            for (int j = 0; j < h; j++) {
                for (int i = 0; i < w; i++) {
                    set(x + i, y + j, paper);
                }
            }
        }

        /** A name, on its own backing plate, in black. */
        void label(int x, int y, String text) {
            int w = PixelFont.width(text);
            plate(x - 1, y - 1, w + 2, PixelFont.HEIGHT + 2);
            PixelFont.draw(this, text, x, y,
                    MapColor.COLOR_BLACK.getPackedId(MapColor.Brightness.LOW));
        }
    }

    // ── Type ──────────────────────────────────────────────────────────────

    /**
     * A three by five pixel alphabet.
     *
     * <p>A map with no names on it is a picture. Minecraft will not render text into map pixels for
     * us, so the letters are drawn by hand — which at this size is thirty odd glyphs and is the
     * difference between "some coloured dots" and "a map of the realm".</p>
     */
    static final class PixelFont {

        private PixelFont() {}

        static final int HEIGHT = 5;
        private static final int ADVANCE = 4;

        private static final Map<Character, String[]> GLYPHS = new HashMap<>();

        private static void put(char c, String a, String b, String d, String e, String f) {
            GLYPHS.put(c, new String[]{a, b, d, e, f});
        }

        static {
            put('A', "010", "101", "111", "101", "101");
            put('B', "110", "101", "110", "101", "110");
            put('C', "011", "100", "100", "100", "011");
            put('D', "110", "101", "101", "101", "110");
            put('E', "111", "100", "110", "100", "111");
            put('F', "111", "100", "110", "100", "100");
            put('G', "011", "100", "101", "101", "011");
            put('H', "101", "101", "111", "101", "101");
            put('I', "111", "010", "010", "010", "111");
            put('J', "001", "001", "001", "101", "010");
            put('K', "101", "101", "110", "101", "101");
            put('L', "100", "100", "100", "100", "111");
            put('M', "101", "111", "111", "101", "101");
            put('N', "101", "111", "111", "111", "101");
            put('O', "010", "101", "101", "101", "010");
            put('P', "110", "101", "110", "100", "100");
            put('Q', "010", "101", "101", "110", "011");
            put('R', "110", "101", "110", "101", "101");
            put('S', "011", "100", "010", "001", "110");
            put('T', "111", "010", "010", "010", "010");
            put('U', "101", "101", "101", "101", "011");
            put('V', "101", "101", "101", "101", "010");
            put('W', "101", "101", "111", "111", "101");
            put('X', "101", "101", "010", "101", "101");
            put('Y', "101", "101", "010", "010", "010");
            put('Z', "111", "001", "010", "100", "111");
            put('0', "111", "101", "101", "101", "111");
            put('1', "010", "110", "010", "010", "111");
            put('2', "110", "001", "010", "100", "111");
            put('3', "110", "001", "010", "001", "110");
            put('4', "101", "101", "111", "001", "001");
            put('5', "111", "100", "110", "001", "110");
            put('6', "011", "100", "110", "101", "010");
            put('7', "111", "001", "010", "010", "010");
            put('8', "010", "101", "010", "101", "010");
            put('9', "010", "101", "011", "001", "110");
            put('.', "000", "000", "000", "000", "010");
            put(',', "000", "000", "000", "010", "100");
            put('-', "000", "000", "111", "000", "000");
            put(':', "000", "010", "000", "010", "000");
            put('\'', "010", "010", "000", "000", "000");
            put('!', "010", "010", "010", "000", "010");
            put('?', "110", "001", "010", "000", "010");
            put(' ', "000", "000", "000", "000", "000");
        }

        static int width(String text) {
            return text.isEmpty() ? 0 : text.length() * ADVANCE - 1;
        }

        static void draw(Canvas c, String text, int x, int y, byte colour) {
            int cursor = x;
            for (char ch : text.toCharArray()) {
                String[] g = GLYPHS.get(ch);
                if (g == null) g = GLYPHS.get('?');
                for (int row = 0; row < HEIGHT; row++) {
                    String bits = g[row];
                    for (int col = 0; col < bits.length(); col++) {
                        if (bits.charAt(col) == '1') c.set(cursor + col, y + row, colour);
                    }
                }
                cursor += ADVANCE;
            }
        }
    }
}
