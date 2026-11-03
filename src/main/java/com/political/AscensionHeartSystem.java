package com.political;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.political.economy.CoinManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Four parallel Skyblock-style heart trees (mining / forest / sea / garden) with heavy coin costs.
 * Separate from {@link MasteryHeartTree}. Vanilla-themed perks only.
 */
public final class AscensionHeartSystem {
    private static final Logger LOGGER = LoggerFactory.getLogger(AscensionHeartSystem.class);


    public enum HeartTree {
        MINING_DEPTHS("Mining Depths", "§8Like HotM — ores & stone", Items.NETHERITE_PICKAXE),
        FOREST_HEART("Grove Ascendancy", "§2Logs, leaves & wilderness", Items.OAK_SAPLING),
        TIDAL_HEART("Tidal Ascendancy", "§bFishing & ocean", Items.HEART_OF_THE_SEA),
        GARDEN_HEART("Verdant Core", "§aCrops & soil", Items.GOLDEN_HOE);

        public final String title;
        public final String subtitle;
        public final Item icon;

        HeartTree(String title, String subtitle, Item icon) {
            this.title = title;
            this.subtitle = subtitle;
            this.icon = icon;
        }
    }

    public record HeartNode(
            HeartTree tree,
            String id,
            String name,
            int row,
            int col,
            String parentId,
            long coinCost,
            String description
    ) {}

    private static final List<HeartNode> NODES = new ArrayList<>();
    private static final Map<String, HeartNode> BY_ID = new HashMap<>();

    static {
        buildTree(HeartTree.MINING_DEPTHS, "md", "Excavation", new long[]{
                900_000, 3_500_000, 12_000_000, 40_000_000, 120_000_000, 300_000_000, 560_000_000, 820_000_000, 1_100_000_000L, 1_400_000_000L
        }, new String[]{
                "+8% break speed on stone & ores",
                "+12% extra vanilla ore loot rolls",
                "+18% mastery XP while mining",
                "Treat deepslate as standard ore for bonuses",
                "+10% damage vs stone-class mobs",
                "+15% coin drops while holding a pickaxe",
                "Ancient debris counts triple for collection rolls",
                "+22% damage in Nether while mining",
                "Reveal nearby ores briefly after breaks",
                "§6§lDepth Ten: §7+35% mining potency on all block tools"
        });
        buildTree(HeartTree.FOREST_HEART, "gf", "Sylvan", new long[]{
                700_000, 2_800_000, 10_000_000, 32_000_000, 92_000_000, 245_000_000, 460_000_000, 700_000_000, 980_000_000, 1_280_000_000L
        }, new String[]{
                "+8% log & leaf break speed",
                "+12% apple & sapling drops",
                "+10% damage in forest biomes",
                "Axe abilities trigger 15% more often",
                "Passive regen I near logs",
                "+18% XP from chopping",
                "Strip logs in 3x3 columns when sneaking (cooldown)",
                "Summon temporary bees on crit vs mobs",
                "Walk faster on grass & moss",
                "§2§lCanopy Ten: §7+30% forest tool potency"
        });
        buildTree(HeartTree.TIDAL_HEART, "th", "Abyssal", new long[]{
                800_000, 3_100_000, 11_000_000, 35_000_000, 100_000_000, 260_000_000, 500_000_000, 760_000_000, 1_020_000_000, 1_320_000_000L
        }, new String[]{
                "+10% fishing speed in water",
                "Unlock lava-line fishing yields",
                "+15% treasure chance while fishing",
                "Sea creatures deal 10% less to you",
                "+12% prismarine & kelp drops",
                "Trident loyalty bonus while in rain",
                "Dolphins Grace II passive in ocean",
                "Hook mobs toward you (weak pull)",
                "Squid ink blinds attackers briefly",
                "§b§lTrench Ten: §7+40% lava & deep fishing rewards"
        });
        buildTree(HeartTree.GARDEN_HEART, "vg", "Cultivar", new long[]{
                700_000, 2_900_000, 10_500_000, 34_000_000, 96_000_000, 250_000_000, 470_000_000, 720_000_000, 980_000_000, 1_250_000_000L
        }, new String[]{
                "+10% crop age on hoe till",
                "+8% bonus crop drops",
                "Bonemeal radius +1 rarely",
                "Immune to trampling (crops)",
                "+15% saturation from farmed food",
                "Poison clears slowly on farmland",
                "Beetroot & carrot fortune feel (+drops)",
                "Night growth pulse under moonlight",
                "Scarecrow effect: mobs avoid plots",
                "§a§lHarvest Ten: §7+35% garden gear & crop events"
        });
    }

    private static void buildTree(HeartTree tree, String prefix, String branchName, long[] costs, String[] desc) {
        String prev = null;
        for (int i = 0; i < costs.length; i++) {
            int tier = i + 1;
            String id = prefix + "_p0_t" + tier;
            HeartNode n = new HeartNode(tree, id, branchName + " " + tier, 0, i, prev, costs[i], desc[i]);
            NODES.add(n);
            BY_ID.put(id, n);
            prev = id;
        }
    }

    private static Path dataFile;
    /** uuid -> tree name -> unlocked ids */
    private static final Map<String, Map<String, Set<String>>> unlocked = new ConcurrentHashMap<>();

    public static void initialize(Path worldRoot) {
        dataFile = worldRoot.resolve("ascension_hearts.json");
        load();
    }

    private static void load() {
        if (dataFile == null || !Files.exists(dataFile)) return;
        try (Reader r = Files.newBufferedReader(dataFile)) {
            Gson gson = new Gson();
            Type t = new TypeToken<Map<String, Map<String, List<String>>>>() {}.getType();
            Map<String, Map<String, List<String>>> raw = gson.fromJson(r, t);
            unlocked.clear();
            if (raw == null) return;
            for (Map.Entry<String, Map<String, List<String>>> e : raw.entrySet()) {
                Map<String, Set<String>> m = new ConcurrentHashMap<>();
                for (Map.Entry<String, List<String>> te : e.getValue().entrySet()) {
                    Set<String> s = ConcurrentHashMap.newKeySet();
                    if (te.getValue() != null) s.addAll(te.getValue());
                    m.put(te.getKey(), s);
                }
                unlocked.put(e.getKey(), m);
            }
        } catch (Exception ex) {
            LOGGER.error("load failed", ex);
        }
    }

    public static void save() {
        if (dataFile == null) return;
        try {
            Files.createDirectories(dataFile.getParent());
            Map<String, Map<String, List<String>>> out = new HashMap<>();
            for (Map.Entry<String, Map<String, Set<String>>> e : unlocked.entrySet()) {
                Map<String, List<String>> m = new HashMap<>();
                for (Map.Entry<String, Set<String>> te : e.getValue().entrySet()) {
                    m.put(te.getKey(), new ArrayList<>(te.getValue()));
                }
                out.put(e.getKey(), m);
            }
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            try (Writer w = Files.newBufferedWriter(dataFile)) {
                gson.toJson(out, w);
            }
        } catch (Exception ex) {
            LOGGER.error("save failed", ex);
        }
    }

    public static List<HeartNode> nodesFor(HeartTree tree) {
        return NODES.stream().filter(n -> n.tree == tree).toList();
    }

    public static Set<String> getUnlocked(String uuid, HeartTree tree) {
        return Set.copyOf(unlocked.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(tree.name(), k -> ConcurrentHashMap.newKeySet()));
    }

    public static boolean hasNode(String uuid, String nodeId) {
        HeartNode n = BY_ID.get(nodeId);
        if (n == null) return false;
        return unlocked.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(n.tree.name(), k -> ConcurrentHashMap.newKeySet())
                .contains(nodeId);
    }

    public static int countUnlockedInTree(String uuid, HeartTree tree) {
        return getUnlocked(uuid, tree).size();
    }

    public static boolean canUnlock(ServerPlayer player, String nodeId) {
        HeartNode n = BY_ID.get(nodeId);
        if (n == null) return false;
        Set<String> u = new HashSet<>(getUnlocked(player.getUUID().toString(), n.tree));
        if (u.contains(n.id)) return false;
        if (n.parentId == null) return true;
        return u.contains(n.parentId);
    }

    public static boolean tryPurchase(ServerPlayer player, String nodeId) {
        HeartNode n = BY_ID.get(nodeId);
        if (n == null) {
            player.sendSystemMessage(Component.literal("Unknown node.").withStyle(ChatFormatting.RED), false);
            return false;
        }
        String uuid = player.getUUID().toString();
        if (hasNode(uuid, nodeId)) {
            player.sendSystemMessage(Component.literal("Already unlocked.").withStyle(ChatFormatting.YELLOW), false);
            return false;
        }
        if (!canUnlock(player, nodeId)) {
            player.sendSystemMessage(Component.literal("Unlock the previous tier first.").withStyle(ChatFormatting.RED), false);
            return false;
        }
        if (!CoinManager.hasCoins(player, n.coinCost)) {
            player.sendSystemMessage(Component.literal("Need " + String.format("%,d", n.coinCost) + " coins.").withStyle(ChatFormatting.RED), false);
            return false;
        }
        if (!CoinManager.removeCoins(player, n.coinCost)) {
            player.sendSystemMessage(Component.literal("Payment failed.").withStyle(ChatFormatting.RED), false);
            return false;
        }
        unlocked.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(n.tree.name(), k -> ConcurrentHashMap.newKeySet())
                .add(nodeId);
        save();
        player.sendSystemMessage(Component.literal("♦ ").withStyle(ChatFormatting.GOLD)
                .append(Component.literal(n.tree.title + ": " + n.name).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)), false);
        player.sendSystemMessage(Component.literal(n.description).withStyle(ChatFormatting.GRAY), false);
        return true;
    }

    /** 1.0 + small bonus per tier unlocked for scaling block gear. */
    public static double miningPotency(String uuid) {
        return 1.0 + 0.025 * countUnlockedInTree(uuid, HeartTree.MINING_DEPTHS);
    }

    public static double forestPotency(String uuid) {
        return 1.0 + 0.022 * countUnlockedInTree(uuid, HeartTree.FOREST_HEART);
    }

    public static double tidalPotency(String uuid) {
        return 1.0 + 0.028 * countUnlockedInTree(uuid, HeartTree.TIDAL_HEART);
    }

    public static double gardenPotency(String uuid) {
        return 1.0 + 0.026 * countUnlockedInTree(uuid, HeartTree.GARDEN_HEART);
    }

    public static int gardenTier(String uuid) {
        return countUnlockedInTree(uuid, HeartTree.GARDEN_HEART);
    }

    public static int tidalTier(String uuid) {
        return countUnlockedInTree(uuid, HeartTree.TIDAL_HEART);
    }
}
