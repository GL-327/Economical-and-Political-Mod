package com.political.power.formula;

/**
 * School tiers. A school's spine is ten nodes in a chain, so unlocking node {@code n} means nodes 1 to
 * {@code n} are unlocked. The tier is how many of the milestones 1, 4, 7 and 10 the spine has reached:
 * tier 4 is the Awakened capstone (node 10), tier 3 needs node 7.
 */
public final class Tiers {

    public static final int[] MILESTONES = {1, 4, 7, 10};
    public static final int CAPSTONE_NODE = 10;
    public static final int MAX_TIER = MILESTONES.length;

    private Tiers() {
    }

    /** The tier for a school whose highest unlocked spine node is {@code highestSpineNode} (0 if none). */
    public static int tier(int highestSpineNode) {
        int tier = 0;
        for (int milestone : MILESTONES) {
            if (highestSpineNode >= milestone) {
                tier++;
            }
        }
        return tier;
    }

    /** The spine node that must be unlocked to reach {@code tier} (1 to 4), or -1 for a tier that does not exist. */
    public static int nodeForTier(int tier) {
        return tier >= 1 && tier <= MAX_TIER ? MILESTONES[tier - 1] : -1;
    }

    /** Whether the capstone, which Domain needs, is reached. */
    public static boolean capstone(int highestSpineNode) {
        return highestSpineNode >= CAPSTONE_NODE;
    }
}
