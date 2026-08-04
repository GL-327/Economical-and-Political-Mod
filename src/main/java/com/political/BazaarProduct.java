package com.political;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Everything the bazaar trades.
 *
 * <p>The bazaar used to trade the sixty collection materials and the shop sold four hundred and
 * thirteen things at fixed prices. Folding one into the other meant the bazaar had to stop being a
 * list of collections, because moving to a single marketplace is only an improvement if the single
 * marketplace carries everything. Deleting the shop and leaving three hundred and fifty six items
 * with no source would have been a downgrade wearing the word "rework".</p>
 *
 * <p>So a product is either a collection material, which keeps its band and its flavour, or a plain
 * vanilla item lifted from the old shop catalogue with its old price as the anchor. Both look
 * identical to the order book, which never has to know the difference.</p>
 */
public final class BazaarProduct {

    public final String key;
    public final String display;
    public final Item material;
    public final ChatFormatting colour;
    public final String category;
    /** What one is nominally worth, which is what the traders mean revert toward. */
    public final long anchor;
    /** The collection this came from, or null when it came from the old shop catalogue. */
    public final CollectionType collection;

    private BazaarProduct(String key, String display, Item material, ChatFormatting colour,
                          String category, long anchor, CollectionType collection) {
        this.key = key;
        this.display = display;
        this.material = material;
        this.colour = colour;
        this.category = category;
        this.anchor = anchor;
        this.collection = collection;
    }

    // ── The catalogue ─────────────────────────────────────────────────────

    private static final Map<String, BazaarProduct> BY_KEY = new LinkedHashMap<>();
    private static final Map<String, List<BazaarProduct>> BY_CATEGORY = new LinkedHashMap<>();
    private static boolean built = false;

    /**
     * Builds the catalogue once.
     *
     * <p>Collections go in first and win any key collision, because a material that is also a
     * collection should keep its band, its colour and its flavour text rather than being flattened
     * into a shop row.</p>
     */
    public static void build() {
        if (built) return;
        BY_KEY.clear();
        BY_CATEGORY.clear();

        // Tracked by item rather than by key. A collection keys off its enum name while the shop
        // keys off the registry path, so MELON and MELON_SLICE would have produced two independent
        // order books for one physical item. Twenty seven items were in that position, and two
        // books for the same thing is riskless arbitrage that never closes.
        java.util.Set<Item> claimed = new java.util.HashSet<>();

        for (CollectionType c : CollectionType.values()) {
            BazaarProduct p = new BazaarProduct(c.key(), c.display, c.material, c.colour,
                    c.field.label, BazaarManager.baseValue(c), c);
            BY_KEY.put(p.key, p);
            claimed.add(c.material);
            BY_CATEGORY.computeIfAbsent(p.category, k -> new ArrayList<>()).add(p);
        }

        for (Map.Entry<String, List<Item>> e : ShopManager.CATEGORIES.entrySet()) {
            for (Item item : e.getValue()) {
                String key = keyOf(item);
                if (BY_KEY.containsKey(key) || !claimed.add(item)) continue;

                // The old shop price is the anchor, so nothing suddenly changes value the day the
                // shop closes. A player who knew what stone was worth still knows.
                long anchor = Math.max(1L, ShopManager.getSellPrice(item));
                BazaarProduct p = new BazaarProduct(key,
                        new net.minecraft.world.item.ItemStack(item).getHoverName().getString(),
                        item, ChatFormatting.WHITE, e.getKey(), anchor, null);
                BY_KEY.put(key, p);
                BY_CATEGORY.computeIfAbsent(p.category, k -> new ArrayList<>()).add(p);
            }
        }

        // ── The custom items ──
        //
        // The bazaar traded vanilla materials only, which meant the thousands of things the mod
        // itself makes had no market at all: you could sell iron but not a delve spoil. Everything
        // with a custom id and a sensible price now has an order book, which is the difference
        // between a marketplace and a vanilla shop that happens to live in a mod.
        addCustomItems(claimed);

        built = true;
        PoliticalServer.LOGGER.info("Bazaar catalogue: " + BY_KEY.size() + " products across "
                + BY_CATEGORY.size() + " categories");
    }

    /** Throws the catalogue away and builds it again, for the live reload. */
    public static void rebuild() {
        built = false;
        build();
    }


    /**
     * Puts the mod's own items on the book.
     *
     * <p>Priced from what they are rather than by hand: a delve spoil already knows what a collector
     * pays for it, and everything else is priced off how much work its recipe represents. Anything
     * that cannot be priced sensibly is left off, because a market with a made up number in it is
     * worse than one item short.</p>
     */
    private static void addCustomItems(java.util.Set<Item> claimed) {
        // Delve spoils. These are the bulk of it and they carry their own value already.
        for (DelveSpoils.Kind k : DelveSpoils.KINDS) {
            for (DelveSpoils.Depth d : DelveSpoils.DEPTHS) {
                for (DelveSpoils.Variant v : DelveSpoils.VARIANTS) {
                    String key = DelveSpoils.idOf(k, d, v);
                    if (BY_KEY.containsKey(key)) continue;
                    long anchor = Math.max(1L, DelveSpoils.valueOf(k, d, v));
                    BazaarProduct p = new BazaarProduct(key,
                            DelveSpoils.nameOf(k, d, v), k.base(), d.colour(),
                            "Delve Spoils", anchor, null);
                    BY_KEY.put(key, p);
                    BY_CATEGORY.computeIfAbsent(p.category, x -> new ArrayList<>()).add(p);
                }
            }
        }

        // Refined materials, which are the other thing players hold in quantity.
        try {
            for (RefinedMaterial m : RefinedMaterial.values()) {
                for (RefinedMaterial.Step step : RefinedMaterial.Step.values()) {
                    String key = "refined_" + m.name().toLowerCase(Locale.ROOT)
                            + "_" + step.name().toLowerCase(Locale.ROOT);
                    if (BY_KEY.containsKey(key)) continue;
                    // The step already carries what its raw form is worth, so the price comes from
                    // the data rather than from a number invented here.
                    long anchor = Math.max(1L, step.rawWorth);
                    BazaarProduct p = new BazaarProduct(key,
                            step.prefix + " " + m.display, m.seed, step.colour,
                            "Refined Materials", anchor, null);
                    BY_KEY.put(key, p);
                    BY_CATEGORY.computeIfAbsent(p.category, x -> new ArrayList<>()).add(p);
                }
            }
        } catch (Throwable ignored) {
            // The refined material shape is not guaranteed. Missing a category is survivable;
            // failing to build the catalogue at all is not.
        }
    }

    private static String keyOf(Item item) {
        try {
            return net.minecraft.core.registries.BuiltInRegistries.ITEM
                    .getKey(item).getPath().toLowerCase(Locale.ROOT);
        } catch (Exception e) {
            return "unknown";
        }
    }

    /** The stable id used on orders. Kept as a method so call sites read the same either way. */
    public String key() {
        return key;
    }

    public static BazaarProduct byKey(String key) {
        build();
        return key == null ? null : BY_KEY.get(key);
    }

    /** Looks up the product for a plain item, or null when it is not traded. */
    public static BazaarProduct forItem(Item item) {
        build();
        return BY_KEY.get(keyOf(item));
    }

    public static List<BazaarProduct> all() {
        build();
        return new ArrayList<>(BY_KEY.values());
    }

    public static List<String> categories() {
        build();
        return new ArrayList<>(BY_CATEGORY.keySet());
    }

    public static List<BazaarProduct> inCategory(String category) {
        build();
        List<BazaarProduct> out = BY_CATEGORY.get(category);
        return out == null ? new ArrayList<>() : new ArrayList<>(out);
    }

    public static int count() {
        build();
        return BY_KEY.size();
    }

    /** An icon for a category tile, chosen from the first product in it. */
    public static Item iconFor(String category) {
        List<BazaarProduct> in = inCategory(category);
        return in.isEmpty() ? Items.CHEST : in.get(0).material;
    }

    @Override
    public String toString() {
        return key;
    }
}
