package com.political.power.formula;

import com.political.power.model.PowerCatalog;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The innate technique: every player is given one at random when they first join, and can reroll it later. This is the
 * draw. The odds are weights, not config keys: the eight older techniques are common, Blue, Red and Cursed Blast are
 * rarer, Reverse and Barrier the rarest. Domain is never drawn; it is unlocked by mastering a technique.
 */
public final class Innate {

    private static final Map<String, Integer> WEIGHTS = new LinkedHashMap<>();

    static {
        for (PowerCatalog.TechniqueDef def : PowerCatalog.techniques()) {
            if (PowerCatalog.DOMAIN.equals(def.id())) {
                continue;
            }
            WEIGHTS.put(def.id(), weightOf(def));
        }
    }

    private Innate() {
    }

    private static int weightOf(PowerCatalog.TechniqueDef def) {
        if (def.legacy()) {
            return 8;
        }
        return switch (def.id()) {
            case PowerCatalog.BLUE, PowerCatalog.RED, PowerCatalog.CURSED_BLAST -> 4;
            case PowerCatalog.BARRIER -> 3;
            case PowerCatalog.REVERSE -> 2;
            default -> 1;
        };
    }

    /** Every technique that can be drawn, with its weight, in catalog order. */
    public static Map<String, Integer> weights() {
        return Map.copyOf(WEIGHTS);
    }

    /** The ids that can be drawn, in catalog order. */
    public static List<String> pool() {
        return List.copyOf(WEIGHTS.keySet());
    }

    /** The share of draws, as a fraction, that gives this technique when nothing is excluded. */
    public static double chance(String techniqueId) {
        int total = WEIGHTS.values().stream().mapToInt(Integer::intValue).sum();
        return WEIGHTS.getOrDefault(techniqueId, 0) / (double) total;
    }

    /**
     * Draws a technique. {@code roll} is a uniform number in [0, 1); {@code excluded} are techniques that cannot come up
     * (the one being rerolled away from, and unique techniques somebody else holds). Null when nothing is left to draw.
     */
    public static String draw(double roll, Collection<String> excluded) {
        List<String> ids = new ArrayList<>();
        int total = 0;
        for (Map.Entry<String, Integer> e : WEIGHTS.entrySet()) {
            if (excluded != null && excluded.contains(e.getKey())) {
                continue;
            }
            ids.add(e.getKey());
            total += e.getValue();
        }
        if (ids.isEmpty()) {
            return null;
        }
        double clamped = Math.max(0.0, Math.min(Math.nextDown(1.0), roll));
        double target = clamped * total;
        int running = 0;
        for (String id : ids) {
            running += WEIGHTS.get(id);
            if (target < running) {
                return id;
            }
        }
        return ids.get(ids.size() - 1);
    }
}
