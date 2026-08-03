package com.political;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * The synthetic traders: two and a half thousand people who are not there.
 *
 * <p>An order book needs somebody on the other side. On a server with four players online that
 * somebody does not exist, so a bazaar becomes a noticeboard where you post a price and wait for a
 * week. The usual fix is a house that buys and sells at fixed prices, which works but means prices
 * never move and there is nothing to trade against.</p>
 *
 * <p>These are the other fix. Each trader has its own view of what a thing is worth, posts real
 * orders onto the same book players use, and takes real orders when the price suits it. A player
 * selling into the book is selling to one of them, and the sale moves the price, because that is
 * what a sale does.</p>
 *
 * <h2>Why this cannot be duped</h2>
 *
 * <p>A synthetic seller mints the goods it delivers, exactly as the house always did, so it is no
 * more a source than the thing it replaces. What stops it running away is that each product has a
 * finite synthetic inventory that only replenishes on a clock, and the price a trader will pay
 * falls as that inventory fills. Sell a thousand of something into the book and the bid collapses,
 * which is what stops a sell loop from being profitable. Coins paid to a synthetic trader leave the
 * economy rather than landing in a second wallet.</p>
 *
 * <h2>Why arbitrage closes</h2>
 *
 * <p>A trader occasionally misprices, crossing the spread on the wrong side. Every other trader can
 * see that, and one of them will take it inside about a minute. A player watching the book can get
 * there first, which is the whole appeal, but nobody can farm it, because the window shuts on its
 * own whether a player is looking or not.</p>
 */
public final class BazaarTraders {

    private BazaarTraders() {}

    /** How many synthetic traders exist. */
    public static final int POPULATION = 2500;

    /** How many act in one round. */
    private static final int ACTORS_PER_ROUND = 48;

    /** Ticks between rounds. Two seconds, so the book visibly moves while somebody watches it. */
    private static final int ROUND_TICKS = 40;

    /** The most synthetic orders allowed to sit on the book at once, per product. */
    private static final int MAX_ORDERS_PER_PRODUCT = 14;

    /** Ceiling on synthetic stock of one product, which is what caps the supply side. */
    private static final int STOCK_CAP = 20_000;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path savePath;

    private static final Random RNG = new Random(0x8BADF00DL);

    // ── The traders ───────────────────────────────────────────────────────

    /**
     * One trader.
     *
     * <p>Deliberately thin. Two and a half thousand of these live in memory for the whole uptime of
     * the server, so anything stored per trader is paid for two and a half thousand times.</p>
     */
    private static final class Trader {
        final String id;
        final String name;
        /** How far off the mid this one is willing to be, as a fraction. */
        final double edge;
        /** Positive leans toward buying, negative toward selling. */
        final double bias;
        /** How large its orders run. */
        final int size;

        Trader(String id, String name, double edge, double bias, int size) {
            this.id = id;
            this.name = name;
            this.edge = edge;
            this.bias = bias;
            this.size = size;
        }
    }

    private static final List<Trader> TRADERS = new ArrayList<>();

    /**
     * Per product state: where the price is, and how much synthetic stock exists.
     *
     * <p>The mid is the thing that makes this feel like a market. It drifts, it mean reverts toward
     * what the material is actually worth, and real player flow pushes it. A player who buys out
     * the sell side raises the price for the next person, which is the behaviour the whole system
     * exists to produce.</p>
     */
    private static final class Product {
        double mid;
        int stock;
        double momentum;
    }

    private static final Map<String, Product> PRODUCTS = new HashMap<>();

    // ── Names ─────────────────────────────────────────────────────────────

    private static final String[] FIRST = {
            "Ald", "Bran", "Cor", "Dov", "Eln", "Fen", "Gar", "Hald", "Irn", "Jor",
            "Kel", "Lom", "Mar", "Ner", "Oss", "Perr", "Quin", "Rhen", "Sel", "Tor",
            "Ul", "Ver", "Wyl", "Xan", "Yor", "Zeb", "Ash", "Bex", "Cray", "Dell",
            "Esk", "Fal", "Grim", "Hess", "Ilk", "Jed", "Karn", "Lund", "Mox", "Nyle",
            "Ovin", "Pike", "Quarl", "Rook", "Stane", "Thal", "Umber", "Vance", "Wren", "Yarrow"
    };
    private static final String[] LAST = {
            "wick", "ford", "mere", "stone", "hollow", "barrow", "cross", "gate", "moor", "vale",
            "brook", "haven", "reach", "march", "field", "hall", "ridge", "combe", "thorpe", "burn",
            "leigh", "worth", "shaw", "dale", "wold", "bank", "grove", "mill", "quay", "wharf",
            "keep", "yard", "row", "lane", "end", "gill", "holt", "rise", "syke", "toft",
            "beck", "carr", "dean", "ey", "garth", "ham", "ing", "ness", "rick", "wick"
    };

    private static String nameFor(Random r) {
        return FIRST[r.nextInt(FIRST.length)] + LAST[r.nextInt(LAST.length)];
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────

    public static void initialize(Path worldRoot) {
        savePath = worldRoot.resolve("political").resolve("bazaar_traders.json");
        buildPopulation();
        load();
        seedProducts();
        PoliticalServer.LOGGER.info("Bazaar traders: " + TRADERS.size()
                + " across " + PRODUCTS.size() + " products");
    }

    /**
     * Builds the population from a fixed seed.
     *
     * <p>Fixed on purpose. The same server always has the same traders with the same names, so a
     * player who notices they keep selling to Branwick is noticing something real rather than a
     * coincidence that resets every restart.</p>
     */
    private static void buildPopulation() {
        TRADERS.clear();
        Random r = new Random(0xB47AA2L);
        for (int i = 0; i < POPULATION; i++) {
            // Most traders are unremarkable and a few are aggressive, which is what gives the book
            // a tight core with occasional wide quotes rather than a uniform smear.
            double edge = 0.02 + Math.abs(r.nextGaussian()) * 0.05;
            double bias = r.nextGaussian() * 0.35;
            int size = 8 + r.nextInt(120);
            TRADERS.add(new Trader("npc:" + i, nameFor(r), Math.min(edge, 0.35), bias, size));
        }
    }

    private static void seedProducts() {
        for (BazaarProduct c : BazaarProduct.all()) {
            Product p = PRODUCTS.computeIfAbsent(c.key(), k -> new Product());
            if (p.mid <= 0) {
                p.mid = BazaarManager.baseValue(c);
                p.stock = STOCK_CAP / 4;
            }
        }
    }

    // ── The round ─────────────────────────────────────────────────────────

    /**
     * One round of trading.
     *
     * <p>A fixed number of traders act per round rather than all of them, so the cost is flat no
     * matter how large the population is. Raising POPULATION changes how varied the book looks and
     * not how much work this does.</p>
     */
    public static void tick() {
        if (PoliticalServer.server == null) return;
        if (TRADERS.isEmpty()) return;
        if (PoliticalServer.server.getTickCount() % ROUND_TICKS != 0) return;

        driftPrices();

        for (int i = 0; i < ACTORS_PER_ROUND; i++) {
            Trader t = TRADERS.get(RNG.nextInt(TRADERS.size()));
            act(t);
        }

        uncrossOne();
        expireStale();

        BazaarManager.settle();
    }

    /**
     * Moves every price a little.
     *
     * <p>Three forces. A random walk so nothing sits still, mean reversion toward what the material
     * is worth so nothing wanders off forever, and momentum so a move that started keeps going for
     * a bit, which is what makes a chart look like a chart rather than noise.</p>
     */
    private static int driftCursor = 0;

    private static void driftPrices() {
        List<BazaarProduct> all = BazaarProduct.all();
        if (all.isEmpty()) return;
        int window = Math.min(64, all.size());
        for (int n = 0; n < window; n++) {
            BazaarProduct c = all.get((driftCursor + n) % all.size());
            Product p = PRODUCTS.get(c.key());
            if (p == null) continue;
            double anchor = BazaarManager.baseValue(c);

            double noise = RNG.nextGaussian() * 0.006;
            double reversion = (anchor - p.mid) / Math.max(1.0, anchor) * 0.02;
            p.momentum = p.momentum * 0.85 + noise;

            // Stock pressure. A product the traders are drowning in gets cheaper, which is the
            // brake on somebody selling the same thing into the book all afternoon.
            double glut = (p.stock / (double) STOCK_CAP) - 0.5;
            double supply = -glut * 0.01;

            p.mid *= 1.0 + p.momentum + reversion + supply;
            p.mid = Math.max(anchor * 0.35, Math.min(anchor * 3.0, p.mid));

            // Stock creeps back toward the middle, so a drained product recovers and a flooded one
            // works itself off rather than staying broken forever.
            if (p.stock < STOCK_CAP / 2) p.stock += 3;
            else if (p.stock > STOCK_CAP / 2) p.stock -= 2;
        }
        driftCursor = (driftCursor + window) % all.size();
    }

    /**
     * Clears one crossed book a round.
     *
     * <p>A book where the best bid is above the best ask is free money for whoever sees it, and a
     * player is allowed a moment to see it, since that is the only interesting thing about watching
     * a market. What is not allowed is for it to still be there tomorrow. One a round is enough to
     * keep the window at a few seconds without making the book feel policed.</p>
     */
    private static int uncrossCursor = 0;

    private static void uncrossOne() {
        List<BazaarProduct> all = BazaarProduct.all();
        if (all.isEmpty()) return;
        // A rotating window rather than the whole catalogue, so the cost per round does not grow
        // with the catalogue. Every product still comes up inside about twenty seconds, which is
        // short enough that a crossed book is a window and not a business model.
        int window = Math.min(48, all.size());
        for (int n = 0; n < window; n++) {
            BazaarProduct c = all.get((uncrossCursor + n) % all.size());
            BazaarManager.Order bid = BazaarManager.bestBuyOrder(c);
            BazaarManager.Order ask = BazaarManager.bestSellOrder(c);
            if (bid == null || ask == null) continue;
            if (bid.unitPrice <= ask.unitPrice) continue;

            int units = Math.min(bid.remaining(), ask.remaining());
            if (units <= 0) continue;
            // Both sides are filled against each other, so neither counterparty is short changed.
            BazaarManager.fillAsSynthetic(bid, units);
            BazaarManager.fillAsSynthetic(ask, units);

            Product p = PRODUCTS.get(c.key());
            if (p != null) p.mid = (bid.unitPrice + ask.unitPrice) / 2.0;
            uncrossCursor = (uncrossCursor + n + 1) % all.size();
            return;
        }
        uncrossCursor = (uncrossCursor + window) % all.size();
    }

    /** One trader does one thing. */
    private static void act(Trader t) {
        List<BazaarProduct> all = BazaarProduct.all();
        if (all.isEmpty()) return;
        BazaarProduct c = all.get(RNG.nextInt(all.size()));
        Product p = PRODUCTS.get(c.key());
        if (p == null) return;

        // Take first. A trader that sees a price it likes on the book hits it rather than posting
        // beside it, which is what closes the arbitrage windows the mispricers open.
        if (tryTake(t, c, p)) return;

        // Otherwise quote, unless this product already has plenty of synthetic depth.
        if (syntheticOrdersFor(c) >= MAX_ORDERS_PER_PRODUCT) return;
        quote(t, c, p);
    }

    /**
     * Looks for an order worth taking, including ones players posted.
     *
     * <p>This is the half that makes the book usable. A player who posts a sell offer at a sensible
     * price gets filled by one of these within a round or two, without anybody having to be
     * online.</p>
     */
    private static boolean tryTake(Trader t, BazaarProduct c, Product p) {
        double want = p.mid * (1.0 + t.bias * 0.02);

        BazaarManager.Order bestSell = BazaarManager.bestSellOrder(c);
        if (bestSell != null && bestSell.unitPrice < want * (1.0 - t.edge * 0.4)) {
            int take = Math.min(t.size, bestSell.remaining());
            if (take > 0) {
                int got = BazaarManager.fillAsSynthetic(bestSell, take);
                p.stock = Math.min(STOCK_CAP, p.stock + got);
                // Buying pressure lifts the price a little, scaled by how much moved.
                p.mid *= 1.0 + Math.min(0.02, got / 4000.0);
                return got > 0;
            }
        }

        BazaarManager.Order bestBuy = BazaarManager.bestBuyOrder(c);
        if (bestBuy != null && bestBuy.unitPrice > want * (1.0 + t.edge * 0.4)) {
            // It will only sell what the synthetic side actually holds, which is the supply cap.
            int take = Math.min(Math.min(t.size, bestBuy.remaining()), p.stock);
            if (take > 0) {
                int got = BazaarManager.fillAsSynthetic(bestBuy, take);
                p.stock = Math.max(0, p.stock - got);
                p.mid *= 1.0 - Math.min(0.02, got / 4000.0);
                return got > 0;
            }
        }
        return false;
    }

    /** Posts a quote on one side. */
    private static void quote(Trader t, BazaarProduct c, Product p) {
        boolean buy = RNG.nextDouble() < 0.5 + t.bias * 0.15;

        // One quote in about sixty is wrong, which is where the arbitrage comes from. It is not
        // free money, since the other traders are looking at the same book.
        boolean misprice = RNG.nextInt(60) == 0;
        double edge = misprice ? -t.edge * 0.5 : t.edge;

        long price;
        int amount = Math.max(1, t.size / 2 + RNG.nextInt(Math.max(1, t.size)));

        if (buy) {
            price = Math.max(1L, Math.round(p.mid * (1.0 - edge)));
        } else {
            price = Math.max(2L, Math.round(p.mid * (1.0 + edge)));
            // A seller cannot offer more than the synthetic side holds.
            amount = Math.min(amount, Math.max(0, p.stock - 200));
            if (amount <= 0) return;
        }

        // A quote that would cross the book executes instead of resting on it, which is how every
        // real venue behaves and is what shuts an arbitrage window on its own. Without this a
        // mispriced quote can sit there paying anybody who notices, forever.
        if (buy) {
            BazaarManager.Order resting = BazaarManager.bestSellOrder(c);
            if (resting != null && resting.unitPrice <= price) {
                int got = BazaarManager.fillAsSynthetic(resting,
                        Math.min(amount, resting.remaining()));
                p.stock = Math.min(STOCK_CAP, p.stock + got);
                p.mid *= 1.0 + Math.min(0.02, got / 4000.0);
                return;
            }
        } else {
            BazaarManager.Order resting = BazaarManager.bestBuyOrder(c);
            if (resting != null && resting.unitPrice >= price) {
                int got = BazaarManager.fillAsSynthetic(resting,
                        Math.min(Math.min(amount, resting.remaining()), p.stock));
                p.stock = Math.max(0, p.stock - got);
                p.mid *= 1.0 - Math.min(0.02, got / 4000.0);
                return;
            }
        }

        BazaarManager.Order o = new BazaarManager.Order();
        o.ownerUuid = t.id;
        o.ownerName = t.name;
        o.product = c.key();
        o.unitPrice = price;
        o.amount = amount;
        o.buy = buy;
        o.createdAt = System.currentTimeMillis();
        BazaarManager.addSyntheticOrder(o);
    }

    /**
     * Pulls synthetic quotes that have drifted well away from the current price.
     *
     * <p>Without this the book fills with orders posted at prices nobody would take, which reads to
     * a player as a market full of nonsense.</p>
     */
    private static void expireStale() {
        long now = System.currentTimeMillis();
        // One pass over the book a round, rather than one pass per quote. Same result, and it does
        // not cost more as the book grows.
        for (BazaarManager.Order o : BazaarManager.liveOrders()) {
            if (!BazaarManager.isSynthetic(o)) continue;
            Product p = PRODUCTS.get(o.product);
            if (p == null) continue;
            boolean stale = now - o.createdAt > 240_000L;
            boolean far = o.unitPrice > p.mid * 2.0 || o.unitPrice < p.mid * 0.5;
            if (stale || far) BazaarManager.removeSynthetic(o.id);
        }
    }

    private static int syntheticOrdersFor(BazaarProduct c) {
        int n = 0;
        for (BazaarManager.Order o : BazaarManager.liveOrders()) {
            if (BazaarManager.isSynthetic(o) && o.product.equals(c.key()) && o.remaining() > 0) n++;
        }
        return n;
    }

    // ── Player flow ───────────────────────────────────────────────────────

    /**
     * Tells the market that a player moved size.
     *
     * <p>Called from the instant buy and sell paths. Without it a player could dump ten thousand of
     * something and the price would not notice, which is the single most obviously fake thing a
     * market can do.</p>
     */
    public static void onPlayerFlow(BazaarProduct c, int units, boolean playerBought) {
        Product p = PRODUCTS.get(c.key());
        if (p == null || units <= 0) return;
        double impact = Math.min(0.06, units / 2500.0);
        if (playerBought) {
            p.mid *= 1.0 + impact;
            p.stock = Math.max(0, p.stock - units);
            p.momentum += impact * 0.5;
        } else {
            p.mid *= 1.0 - impact;
            p.stock = Math.min(STOCK_CAP, p.stock + units);
            p.momentum -= impact * 0.5;
        }
        double anchor = BazaarManager.baseValue(c);
        p.mid = Math.max(anchor * 0.35, Math.min(anchor * 3.0, p.mid));
    }

    // ── Readouts, for the menu ────────────────────────────────────────────

    public static long midOf(BazaarProduct c) {
        Product p = PRODUCTS.get(c.key());
        return p == null ? BazaarManager.baseValue(c) : Math.max(1L, Math.round(p.mid));
    }

    /** How the price has moved lately, as a percentage. */
    public static double momentumOf(BazaarProduct c) {
        Product p = PRODUCTS.get(c.key());
        return p == null ? 0 : p.momentum * 100.0;
    }

    public static int stockOf(BazaarProduct c) {
        Product p = PRODUCTS.get(c.key());
        return p == null ? 0 : p.stock;
    }

    public static int population() {
        return TRADERS.size();
    }

    // ── Persistence ───────────────────────────────────────────────────────

    public static void save() {
        if (savePath == null) return;
        try {
            Files.createDirectories(savePath.getParent());
            JsonObject root = new JsonObject();
            List<String> keys = new ArrayList<>(PRODUCTS.keySet());
            java.util.Collections.sort(keys);
            for (String k : keys) {
                Product p = PRODUCTS.get(k);
                JsonObject j = new JsonObject();
                j.addProperty("mid", p.mid);
                j.addProperty("stock", p.stock);
                j.addProperty("momentum", p.momentum);
                root.add(k, j);
            }
            try (Writer w = Files.newBufferedWriter(savePath, StandardCharsets.UTF_8)) {
                GSON.toJson(root, w);
            }
        } catch (Exception e) {
            PoliticalServer.LOGGER.error("Could not save bazaar traders: " + e.getMessage());
        }
    }

    private static void load() {
        PRODUCTS.clear();
        if (savePath == null || !Files.exists(savePath)) return;
        try (Reader r = Files.newBufferedReader(savePath, StandardCharsets.UTF_8)) {
            JsonElement root = JsonParser.parseReader(r);
            if (!root.isJsonObject()) return;
            for (Map.Entry<String, JsonElement> e : root.getAsJsonObject().entrySet()) {
                if (!e.getValue().isJsonObject()) continue;
                JsonObject j = e.getValue().getAsJsonObject();
                Product p = new Product();
                p.mid = j.has("mid") ? j.get("mid").getAsDouble() : 0;
                p.stock = j.has("stock") ? j.get("stock").getAsInt() : STOCK_CAP / 4;
                p.momentum = j.has("momentum") ? j.get("momentum").getAsDouble() : 0;
                PRODUCTS.put(e.getKey(), p);
            }
        } catch (Exception e) {
            PoliticalServer.LOGGER.error("Could not read bazaar traders: " + e.getMessage());
        }
    }
}
