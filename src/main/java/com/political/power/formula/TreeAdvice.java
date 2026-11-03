package com.political.power.formula;

import com.political.SchoolTree;
import com.political.magic.MagicSchool;
import com.political.power.model.PowerCatalog;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * What the school tree screen tells a player: where each node stands, what the next step is and what it costs,
 * and which techniques a step would bring. Pure logic over the node definitions, so the screen only draws it.
 * The order of the checks matches {@code PowerService.unlockNode}, so the screen never promises a purchase the
 * service then refuses.
 */
public final class TreeAdvice {

    /** Where one node stands for one player. */
    public enum Status { UNLOCKED, READY, NEEDS_SHARDS, NEEDS_LEVEL, PARENT_LOCKED }

    /**
     * The next spine node to buy.
     *
     * @param node          the node
     * @param status        why it can or cannot be bought now
     * @param shardsMissing shards short (0 when enough)
     * @param levelsMissing power levels short (0 when high enough)
     * @param spineNumber   1 to 10, its place on the spine
     * @param tierAfter     the tier the player has once this node is bought
     * @param unlocks       technique ids that this node grants
     */
    public record NextStep(SchoolTree.SchoolNode node, Status status, int shardsMissing, int levelsMissing,
                           int spineNumber, int tierAfter, List<String> unlocks) {
    }

    private TreeAdvice() {
    }

    /** The place of a node on the 10 node spine (1 to 10), or 0 for a side node. */
    public static int spineNumber(SchoolTree.SchoolNode node) {
        String id = node.id();
        String prefix = node.school().prefix;
        if (!id.startsWith(prefix)) {
            return 0;
        }
        String rest = id.substring(prefix.length());
        try {
            int n = Integer.parseInt(rest);
            return n >= 1 && n <= Tiers.CAPSTONE_NODE ? n : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static Status status(SchoolTree.SchoolNode node, Set<String> unlocked, int level, int shards) {
        if (unlocked.contains(node.id())) {
            return Status.UNLOCKED;
        }
        if (node.parentId() != null && !unlocked.contains(node.parentId())) {
            return Status.PARENT_LOCKED;
        }
        if (level < node.levelRequired()) {
            return Status.NEEDS_LEVEL;
        }
        if (shards < node.shardCost()) {
            return Status.NEEDS_SHARDS;
        }
        return Status.READY;
    }

    /** Techniques that become available when the spine reaches {@code spineNumber} in {@code school}. */
    public static List<String> unlocksAt(MagicSchool school, int spineNumber) {
        List<String> out = new ArrayList<>();
        for (PowerCatalog.TechniqueDef def : PowerCatalog.techniquesOf(school.name())) {
            if (def.unlockNode() == spineNumber) {
                out.add(def.id());
            }
        }
        if (spineNumber == Tiers.CAPSTONE_NODE) {
            out.add(PowerCatalog.DOMAIN);
        }
        return out;
    }

    /** The first spine node the player has not bought yet, or null when the capstone is unlocked. */
    public static NextStep nextSpine(MagicSchool school, Set<String> unlocked, int level, int shards) {
        for (SchoolTree.SchoolNode node : SchoolTree.nodesFor(school)) {
            int spine = spineNumber(node);
            if (spine == 0 || unlocked.contains(node.id())) {
                continue;
            }
            Status status = status(node, unlocked, level, shards);
            int shardsMissing = Math.max(0, node.shardCost() - shards);
            int levelsMissing = Math.max(0, node.levelRequired() - level);
            return new NextStep(node, status, shardsMissing, levelsMissing, spine, Tiers.tier(spine), unlocksAt(school, spine));
        }
        return null;
    }
}
