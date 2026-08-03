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

        for (CollectionType c : CollectionType.values()) {
            BazaarProduct p = new BazaarProduct(c.key(), c.display, c.material, c.colour,
                    c.field.label, BazaarManager.baseValue(c), c);
            BY_KEY.put(p.key, p);
            BY_CATEGORY.computeIfAbsent(p.category, k -> new ArrayList<>()).add(p);
        }

        for (Map.Entry<String, List<Item>> e : ShopManager.CATEGORIES.entrySet()) {
            for (Item item : e.getValue()) {
                String key = keyOf(item);
                if (BY_KEY.containsKey(key)) continue;

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

        built = true;
        PoliticalServer.LOGGER.info("Bazaar catalogue: " + BY_KEY.size() + " products across "
                + BY_CATEGORY.size() + " categories");
    }

    /** Throws the catalogue away and builds it again, for the live reload. */
    public static void rebuild() {
        built = false;
        build();
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
