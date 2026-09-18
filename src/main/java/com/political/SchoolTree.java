package com.political;


import com.political.magic.MagicSchool;
import com.political.magic.MagicSkillTree;
import java.util.*;

/**
 * HotM-style tree definitions for every {@link MagicSchool} (12 as of V4 — the original eight
 * plus Chronomancy, Resonance, Sporeweaving and Astralism). Each school has 14 nodes:
 * a 10-node spine (ids {@code <prefix>1..<prefix>10}) plus 4 branching side nodes
 * (ids {@code <prefix>a..<prefix>d}). Nodes form a DAG via {@code parentId}.
 *
 * <p>Nodes cost <b>Arcane Shards</b> (a spendable currency separate from the shared magic-XP
 * pool — see {@link SchoolTreeManager}) and are level-gated. Node effects are pure passives
 * (school damage %, max mana, cooldown %); the tenth spine node is an <b>Awakened</b> capstone
 * that grants a large affinity bonus. No node unlocks a spell — spells come from
 * {@link MagicSkillTree} only, which keeps the casting pipeline single-sourced.</p>
 */
public final class SchoolTree {

    private SchoolTree() {}

    public record SchoolNode(
            String id,
            MagicSchool school,
            String name,
            int shardCost,
            int levelRequired,
            String parentId,
            String description,
            int dmgPct,
            int manaBonus,
            int cdPct,
            boolean capstone
    ) {}

    private static final Map<String, SchoolNode> BY_ID = new LinkedHashMap<>();
    private static final Map<MagicSchool, List<SchoolNode>> BY_SCHOOL = new EnumMap<>(MagicSchool.class);

    static {
        for (MagicSchool s : MagicSchool.values()) build(s);
    }

    private static void add(SchoolNode n) {
        BY_ID.put(n.id(), n);
        BY_SCHOOL.computeIfAbsent(n.school(), k -> new ArrayList<>()).add(n);
    }

    private static void build(MagicSchool s) {
        String p = s.prefix;
        String prev = null;
        for (int i = 1; i <= 10; i++) {
            String id = p + i;
            int cost = 2 + i * 2;                     // 4,6,8,... shards
            int lvl = Math.min(50, 2 + (i - 1) * 5);  // 2,7,12,...47
            int dmg = 0, mana = 0, cd = 0;
            boolean cap = false;
            String desc;
            if (i == 10) {
                cap = true;
                dmg = 20;
                mana = 40;
                cd = 10;
                cost = 30;
                lvl = 50;
                desc = "AWAKENED " + s.displayName + ": +20% " + s.displayName + " damage, +40 mana, -10% cooldown";
            } else {
                switch (i % 3) {
                    case 1 -> { dmg = 5 + i; desc = "+" + dmg + "% " + s.displayName + " damage"; }
                    case 2 -> { mana = 10 + i * 2; desc = "+" + mana + " max mana"; }
                    default -> { cd = 3 + i / 2; desc = "-" + cd + "% " + s.displayName + " cooldowns"; }
                }
            }
            add(new SchoolNode(id, s, s.displayName + " Path " + i, cost, lvl, prev, desc, dmg, mana, cd, cap));
            prev = id;
        }
        // Four side nodes branching off the early/mid spine.
        add(new SchoolNode(p + "a", s, s.displayName + " Focus",   6,  6,  p + "2", "+8% " + s.displayName + " damage",   8, 0, 0, false));
        add(new SchoolNode(p + "b", s, s.displayName + " Wellspring", 8, 10, p + "3", "+25 max mana",                     0, 25, 0, false));
        add(new SchoolNode(p + "c", s, s.displayName + " Flow",     12, 18, p + "5", "-6% " + s.displayName + " cooldowns", 0, 0, 6, false));
        add(new SchoolNode(p + "d", s, s.displayName + " Ascendant", 20, 30, p + "7", "+12% " + s.displayName + " damage", 12, 0, 0, false));
    }

    public static SchoolNode getNode(String id) {
        return id == null ? null : BY_ID.get(id);
    }

    public static List<SchoolNode> nodesFor(MagicSchool school) {
        return Collections.unmodifiableList(BY_SCHOOL.getOrDefault(school, List.of()));
    }

    public static int totalNodes() {
        return BY_ID.size();
    }

    public static int nodesPerSchool(MagicSchool school) {
        return BY_SCHOOL.getOrDefault(school, List.of()).size();
    }

    /** Every school that has a generated tree (i.e. all of them). */
    public static List<MagicSchool> schools() {
        return new ArrayList<>(BY_SCHOOL.keySet());
    }

    /** Capstone (spine node 10) id for a school. */
    public static String capstoneId(MagicSchool school) {
        return school.prefix + "10";
    }
}
