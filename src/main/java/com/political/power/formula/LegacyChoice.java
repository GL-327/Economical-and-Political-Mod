package com.political.power.formula;

import com.political.magic.MagicSchool;
import com.political.power.model.PowerCatalog;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Players who already had a technique before techniques were rolled (a school, an equipped technique, or techniques
 * learned from manuals or an admin) keep one of them as their innate technique and count as awakened. Nobody loses
 * what they had: the old school, nodes and learned techniques stay in the saved data, and learned techniques stay usable.
 */
public final class LegacyChoice {

    private LegacyChoice() {
    }

    /** The technique to keep, or null when the player had nothing and gets a random one. */
    public static String of(String equipped, Collection<String> learned, String school, Set<String> unlockedNodes) {
        if (isKeepable(equipped)) {
            return equipped;
        }
        List<String> sorted = new ArrayList<>(learned);
        java.util.Collections.sort(sorted);
        for (String id : sorted) {
            if (isKeepable(id)) {
                return id;
            }
        }
        if (school != null) {
            MagicSchool magic = MagicSchool.byName(school);
            if (magic != null) {
                int node = SchoolPassives.highestSpineNode(magic, unlockedNodes);
                for (PowerCatalog.TechniqueDef def : PowerCatalog.grantedBy(magic.name(), node)) {
                    if (isKeepable(def.id())) {
                        return def.id();
                    }
                }
            }
        }
        return null;
    }

    /** A known technique that can be innate (the Domain cannot: it comes from mastery). */
    private static boolean isKeepable(String id) {
        return id != null && PowerCatalog.isTechnique(id) && !PowerCatalog.DOMAIN.equals(id);
    }
}
