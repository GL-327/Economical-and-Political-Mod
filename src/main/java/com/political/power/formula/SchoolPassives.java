package com.political.power.formula;

import com.political.SchoolTree;
import com.political.magic.MagicSchool;
import java.util.Set;

/**
 * Passive bonuses from unlocked school tree nodes: damage percent, extra resource, and cooldown percent.
 * Every number comes from the node definitions in {@link SchoolTree}; the totals are bounded by the tree
 * itself (the most a school can give is +67 percent damage, and cooldown reduction is capped at 40 percent).
 */
public final class SchoolPassives {

    /** Cooldown reduction from one school's nodes never exceeds this many percent. */
    public static final int COOLDOWN_CAP_PERCENT = 40;

    private SchoolPassives() {
    }

    /** The highest spine node (1 to 10) unlocked in a school, 0 if none. Side nodes do not count. */
    public static int highestSpineNode(MagicSchool school, Set<String> unlocked) {
        int highest = 0;
        for (int n = 1; n <= 10; n++) {
            if (unlocked.contains(school.prefix + n)) {
                highest = n;
            }
        }
        return highest;
    }

    /** Damage bonus, as a fraction, from the nodes of one school. */
    public static double damageBonus(MagicSchool school, Set<String> unlocked) {
        int pct = 0;
        for (SchoolTree.SchoolNode node : SchoolTree.nodesFor(school)) {
            if (unlocked.contains(node.id())) {
                pct += node.dmgPct();
            }
        }
        return pct / 100.0;
    }

    /** Extra maximum resource from all unlocked nodes of the given school. */
    public static int resourceBonus(MagicSchool school, Set<String> unlocked) {
        int bonus = 0;
        for (SchoolTree.SchoolNode node : SchoolTree.nodesFor(school)) {
            if (unlocked.contains(node.id())) {
                bonus += node.manaBonus();
            }
        }
        return bonus;
    }

    /** Cooldown reduction, as a fraction, from the nodes of one school, capped. */
    public static double cooldownReduction(MagicSchool school, Set<String> unlocked) {
        int pct = 0;
        for (SchoolTree.SchoolNode node : SchoolTree.nodesFor(school)) {
            if (unlocked.contains(node.id())) {
                pct += node.cdPct();
            }
        }
        return Math.min(pct, COOLDOWN_CAP_PERCENT) / 100.0;
    }
}
