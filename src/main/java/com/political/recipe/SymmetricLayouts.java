package com.political.recipe;

import java.util.*;

/** Distinct symmetric arrangements of one ingredient multiset; preserves ingredient costs. */
public final class SymmetricLayouts {
    private SymmetricLayouts() {}

    public static List<String> variant(List<String> original, int variant) {
        if (original.size() != 9 || variant < 0) throw new IllegalArgumentException("Invalid layout");
        List<List<String>> layouts = new ArrayList<>();
        if (symmetric(original)) layouts.add(List.copyOf(original));
        TreeMap<String, Integer> counts = new TreeMap<>();
        original.forEach(key -> counts.merge(key, 1, Integer::sum));
        collect(counts, new ArrayList<>(), layouts, variant + 1);
        if (variant >= layouts.size()) throw new IllegalArgumentException("Not enough symmetric layouts");
        return layouts.get(variant);
    }

    private static void collect(Map<String, Integer> counts, List<String> row, List<List<String>> out, int limit) {
        if (out.size() >= limit) return;
        if (row.size() == 9) {
            if (symmetric(row) && !out.contains(row)) out.add(List.copyOf(row));
            return;
        }
        for (String key : counts.keySet()) {
            int available = counts.get(key);
            if (available == 0) continue;
            counts.put(key, available - 1);
            row.add(key);
            collect(counts, row, out, limit);
            row.removeLast();
            counts.put(key, available);
            if (out.size() >= limit) return;
        }
    }

    public static boolean symmetric(List<String> cells) {
        boolean vertical = true, horizontal = true;
        for (int i = 0; i < 9; i++) {
            vertical &= cells.get(i).equals(cells.get((i / 3) * 3 + 2 - i % 3));
            horizontal &= cells.get(i).equals(cells.get((2 - i / 3) * 3 + i % 3));
        }
        return vertical || horizontal;
    }
}
