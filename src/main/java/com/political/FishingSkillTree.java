package com.political;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.ChatFormatting;

import java.util.*;

public final class FishingSkillTree {

    private FishingSkillTree() {}

    public enum Category {
        ROD_MASTERY("Rod Mastery", ChatFormatting.GRAY, Items.FISHING_ROD),
        SEA_HUNTER("Sea Hunter", ChatFormatting.RED, Items.TRIDENT),
        TREASURE_FINDER("Treasure Finder", ChatFormatting.GOLD, Items.CHEST),
        DEEP_SEA("Deep Sea", ChatFormatting.DARK_PURPLE, Items.MAGMA_BLOCK),
        TIDAL_FORCE("Tidal Force", ChatFormatting.AQUA, Items.HEART_OF_THE_SEA);

        public final String displayName;
        public final ChatFormatting color;
        public final Item icon;

        Category(String displayName, ChatFormatting color, Item icon) {
            this.displayName = displayName;
            this.color = color;
            this.icon = icon;
        }
    }

    public static class Node {
        public final String id;
        public final String name;
        public final String description;
        public final Item icon;
        public final Category category;
        public final int xpCost;
        public final String prerequisiteId; // null = root node
        public final int gridRow;
        public final int gridCol;

        public Node(String id, String name, String description, Item icon,
                    Category category, int xpCost, String prerequisiteId,
                    int gridRow, int gridCol) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.icon = icon;
            this.category = category;
            this.xpCost = xpCost;
            this.prerequisiteId = prerequisiteId;
            this.gridRow = gridRow;
            this.gridCol = gridCol;
        }
    }

    private static final List<Node> ALL_NODES = new ArrayList<>();
    private static final Map<String, Node> NODE_MAP = new LinkedHashMap<>();

    static {
        // Rod Mastery (col 1)
        add("rm_nimble_cast", "Nimble Cast", "+5% catch speed", Items.STRING, Category.ROD_MASTERY, 50, null, 0, 0);
        add("rm_steady_hand", "Steady Hand", "+10% catch speed", Items.IRON_INGOT, Category.ROD_MASTERY, 100, "rm_nimble_cast", 1, 0);
        add("rm_anglers_focus", "Angler's Focus", "+15% catch speed", Items.GOLD_INGOT, Category.ROD_MASTERY, 200, "rm_steady_hand", 2, 0);
        add("rm_precision_reel", "Precision Reel", "Better fish quality", Items.DIAMOND, Category.ROD_MASTERY, 400, "rm_anglers_focus", 3, 0);
        add("rm_master_angler", "Master Angler", "+25% catch speed, +10% rare fish", Items.NETHERITE_INGOT, Category.ROD_MASTERY, 800, "rm_precision_reel", 4, 0);

        // Sea Hunter (col 2)
        add("sh_bait_sense", "Bait Sense", "+3% sea creature spawn", Items.COD, Category.SEA_HUNTER, 75, null, 0, 1);
        add("sh_hook_barb", "Hook Barb", "+10% damage to sea creatures", Items.IRON_SWORD, Category.SEA_HUNTER, 150, "sh_bait_sense", 1, 1);
        add("sh_tide_caller", "Tide Caller", "+6% sea creature spawn", Items.NAUTILUS_SHELL, Category.SEA_HUNTER, 300, "sh_hook_barb", 2, 1);
        add("sh_sea_slayer", "Sea Slayer", "+25% damage to sea creatures", Items.DIAMOND_SWORD, Category.SEA_HUNTER, 600, "sh_tide_caller", 3, 1);
        add("sh_kraken_hunter", "Kraken Hunter", "Chance to spawn Epic+ creatures", Items.TRIDENT, Category.SEA_HUNTER, 1200, "sh_sea_slayer", 4, 1);
        add("sh_leviathan_bane", "Leviathan Bane", "+50% dmg to Legendary creatures", Items.NETHERITE_SWORD, Category.SEA_HUNTER, 2000, "sh_kraken_hunter", 5, 1);

        // Treasure Finder (col 3-4)
        add("tf_lucky_hook", "Lucky Hook", "5% treasure catch chance", Items.GOLD_NUGGET, Category.TREASURE_FINDER, 100, null, 0, 2);
        add("tf_magnet_line", "Magnet Line", "+5% treasure, chance for coins", Items.IRON_NUGGET, Category.TREASURE_FINDER, 200, "tf_lucky_hook", 1, 2);
        add("tf_double_haul", "Double Haul", "5% chance for double loot", Items.CHEST, Category.TREASURE_FINDER, 400, "tf_magnet_line", 2, 2);
        add("tf_sunken_vault", "Sunken Vault", "Rare custom item drops", Items.ENDER_CHEST, Category.TREASURE_FINDER, 800, "tf_double_haul", 3, 2);
        add("tf_golden_reel", "Golden Reel", "+15% treasure, coin bonus x2", Items.GOLD_BLOCK, Category.TREASURE_FINDER, 1500, "tf_sunken_vault", 4, 2);
        add("tf_fathom_fortune", "Fathom Fortune", "Guaranteed rare every 50 catches", Items.NETHER_STAR, Category.TREASURE_FINDER, 2500, "tf_golden_reel", 5, 2);

        // Deep Sea (col 5)
        add("ds_heat_proof", "Heat-Proof Line", "Can fish in lava (basic)", Items.MAGMA_CREAM, Category.DEEP_SEA, 150, null, 0, 3);
        add("ds_obsidian_hook", "Obsidian Hook", "Better lava loot tables", Items.OBSIDIAN, Category.DEEP_SEA, 300, "ds_heat_proof", 1, 3);
        add("ds_nether_angler", "Nether Angler", "Lava sea creatures", Items.BLAZE_ROD, Category.DEEP_SEA, 600, "ds_obsidian_hook", 2, 3);
        add("ds_void_fisher", "Void Fisher", "Fish in the End void", Items.ENDER_EYE, Category.DEEP_SEA, 1000, "ds_nether_angler", 3, 3);
        add("ds_abyss_diver", "Abyss Diver", "All biomes boosted, +XP", Items.END_CRYSTAL, Category.DEEP_SEA, 2000, "ds_void_fisher", 4, 3);

        // Tidal Force (col 6)
        add("tidal_wave", "Tidal Wave", "Active: AOE knockback+dmg", Items.HEART_OF_THE_SEA, Category.TIDAL_FORCE, 200, null, 0, 4);
        add("tidal_shield", "Tidal Shield", "Active: water shield (3s)", Items.PRISMARINE_SHARD, Category.TIDAL_FORCE, 400, "tidal_wave", 1, 4);
        add("tidal_rain", "Fish Rain", "Active: heal 4hp + feed", Items.TROPICAL_FISH, Category.TIDAL_FORCE, 600, "tidal_shield", 2, 4);
        add("tidal_whirlpool", "Whirlpool Trap", "Active: pull + slow enemies", Items.PRISMARINE_CRYSTALS, Category.TIDAL_FORCE, 1000, "tidal_rain", 3, 4);
        add("tidal_tsunami", "Tsunami Strike", "Active: massive water dmg", Items.CONDUIT, Category.TIDAL_FORCE, 2000, "tidal_whirlpool", 4, 4);
    }

    private static void add(String id, String name, String desc, Item icon,
                            Category cat, int cost, String prereq, int row, int col) {
        Node n = new Node(id, name, desc, icon, cat, cost, prereq, row, col);
        ALL_NODES.add(n);
        NODE_MAP.put(id, n);
    }

    public static List<Node> getAllNodes() {
        return Collections.unmodifiableList(ALL_NODES);
    }

    public static Node getNode(String id) {
        return NODE_MAP.get(id);
    }

    public static List<Node> getNodesByCategory(Category cat) {
        List<Node> nodes = new ArrayList<>();
        for (Node n : ALL_NODES) {
            if (n.category == cat) nodes.add(n);
        }
        return nodes;
    }

    public static boolean canUnlock(String nodeId, Set<String> unlockedNodes) {
        Node node = NODE_MAP.get(nodeId);
        if (node == null) return false;
        if (unlockedNodes.contains(nodeId)) return false;
        return node.prerequisiteId == null || unlockedNodes.contains(node.prerequisiteId);
    }
}
