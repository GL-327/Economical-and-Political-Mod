package com.political.core.gui;

import java.util.Locale;
import java.util.regex.Pattern;

/** Pure layout and matching rules for the three item catalogs. No game state or saved data. */
public final class CatalogModel {
    public static final int CARDS_PER_PAGE = 8;
    public static final int ITEMS_PER_PAGE = 20;
    private static final Pattern FORMATTING = Pattern.compile("§[0-9a-fk-or]", Pattern.CASE_INSENSITIVE);
    private static final Pattern WORDS = Pattern.compile("\\s+");

    private CatalogModel() {}

    public record Card(int iconSlot, int labelSlot, int badgeCount) {}

    public static Card card(int index, int itemCount) {
        if (index < 0 || index >= CARDS_PER_PAGE || itemCount < 0) {
            throw new IllegalArgumentException("invalid catalog card");
        }
        int slot = 10 + (index / 4) * 18 + (index % 4) * 2;
        return new Card(slot, slot + 9, Math.max(1, Math.min(64, itemCount)));
    }

    public static int[] itemSlots() {
        int[] slots = new int[ITEMS_PER_PAGE];
        for (int i = 0; i < slots.length; i++) slots[i] = 11 + (i / 5) * 9 + i % 5;
        return slots;
    }

    public static String normalize(String text) {
        return FORMATTING.matcher(text).replaceAll("").strip().toLowerCase(Locale.ROOT);
    }

    public static boolean matches(String name, String query) {
        String normalizedName = normalize(name);
        for (String word : WORDS.split(normalize(query))) {
            if (!normalizedName.contains(word)) return false;
        }
        return true;
    }
}
