package com.political;


import com.political.magic.MagicManager;
import com.political.magic.MagicSchool;
import com.political.magic.MagicSkillTree;
import com.political.magic.MagicSpellHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.lang.reflect.Type;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-player school-tree progression.
 *
 * <p><b>Arcane Shards</b> — a NEW spendable currency, intentionally separate from the shared,
 * buggy magic-XP spend pool used by {@link MagicSkillTree}. Shards are earned from magic-creature
 * kills and magic level-ups (see {@code MagicSpellHandler} / {@code MagicManager.addXp} hooks) and
 * are the ONLY cost of unlocking {@link SchoolTree} nodes. Because shards are their own pool, the
 * legacy double-spend bug (XP consumed both for leveling and for tree nodes) cannot recur here.</p>
 *
 * <p>Persisted to {@code magic_schools.json} in the world root.</p>
 */
public final class SchoolTreeManager {

    private SchoolTreeManager() {}

    public static class PlayerSchoolData {
        public int shards = 0;
        public int shardsEarnedTotal = 0;
        public Set<String> unlocked = new HashSet<>();
    }

    private static final Map<String, PlayerSchoolData> DATA = new ConcurrentHashMap<>();
    private static Path dataFile;

    // ── Persistence ─────────────────────────────────────────────

    public static void initialize(Path worldRoot) {
        dataFile = worldRoot.resolve("magic_schools.json");
        load();
    }

    private static void load() {
        if (dataFile == null || !Files.exists(dataFile)) return;
        try {
            String content = Files.readString(dataFile);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Type type = new TypeToken<Map<String, PlayerSchoolData>>() {}.getType();
            Map<String, PlayerSchoolData> loaded = gson.fromJson(content, type);
            if (loaded != null) {
                DATA.clear();
                DATA.putAll(loaded);
            }
        } catch (Exception e) {
            System.err.println("[SchoolTreeManager] Failed to load: " + e.getMessage());
        }
    }

    public static void save() {
        if (dataFile == null) return;
        try {
            Files.createDirectories(dataFile.getParent());
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            try (java.io.Writer w = Files.newBufferedWriter(dataFile)) {
                gson.toJson(DATA, w);
            }
        } catch (Exception e) {
            System.err.println("[SchoolTreeManager] Failed to save: " + e.getMessage());
        }
    }

    public static PlayerSchoolData get(String uuid) {
        return DATA.computeIfAbsent(uuid, k -> new PlayerSchoolData());
    }

    // ── Arcane Shard currency ───────────────────────────────────

    public static int getShards(String uuid) {
        return get(uuid).shards;
    }

    public static void addShards(ServerPlayer player, int amount) {
        if (amount <= 0) return;
        PlayerSchoolData d = get(player.getUUID().toString());
        d.shards += amount;
        d.shardsEarnedTotal += amount;
        player.sendSystemMessage(Component.literal("✦ +" + amount + " Arcane Shard" + (amount == 1 ? "" : "s"))
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        save();
    }

    /** Bare add without message (used for bulk grants such as grimoires). */
    public static void grantShardsSilent(String uuid, int amount) {
        if (amount <= 0) return;
        PlayerSchoolData d = get(uuid);
        d.shards += amount;
        d.shardsEarnedTotal += amount;
    }

    // ── Node unlocking ──────────────────────────────────────────

    public static boolean isUnlocked(String uuid, String nodeId) {
        return get(uuid).unlocked.contains(nodeId);
    }

    public static boolean unlockNode(ServerPlayer player, String nodeId) {
        String uuid = player.getUUID().toString();
        PlayerSchoolData d = get(uuid);
        SchoolTree.SchoolNode node = SchoolTree.getNode(nodeId);
        if (node == null) return false;
        if (d.unlocked.contains(nodeId)) return false;
        if (MagicManager.getLevel(uuid) < node.levelRequired()) return false;
        if (d.shards < node.shardCost()) return false;
        if (node.parentId() != null && !d.unlocked.contains(node.parentId())) return false;

        d.shards -= node.shardCost();
        d.unlocked.add(nodeId);
        save();
        return true;
    }

    public static int unlockedCount(String uuid, MagicSchool school) {
        PlayerSchoolData d = get(uuid);
        int c = 0;
        for (SchoolTree.SchoolNode n : SchoolTree.nodesFor(school)) {
            if (d.unlocked.contains(n.id())) c++;
        }
        return c;
    }

    // ── Passive getters (queried by the casting pipeline / gear) ─

    /** Percent bonus to damage of spells belonging to a school (additive). */
    public static int getSchoolDamageBonus(String uuid, MagicSchool school) {
        PlayerSchoolData d = get(uuid);
        int pct = 0;
        for (SchoolTree.SchoolNode n : SchoolTree.nodesFor(school)) {
            if (d.unlocked.contains(n.id())) pct += n.dmgPct();
        }
        return pct;
    }

    /** Total max-mana bonus from ALL unlocked school nodes. */
    public static int getManaBonus(String uuid) {
        PlayerSchoolData d = get(uuid);
        int mana = 0;
        for (String id : d.unlocked) {
            SchoolTree.SchoolNode n = SchoolTree.getNode(id);
            if (n != null) mana += n.manaBonus();
        }
        return mana;
    }

    /** Percent cooldown reduction from a school's unlocked nodes (capped). */
    public static int getCooldownReduction(String uuid, MagicSchool school) {
        PlayerSchoolData d = get(uuid);
        int pct = 0;
        for (SchoolTree.SchoolNode n : SchoolTree.nodesFor(school)) {
            if (d.unlocked.contains(n.id())) pct += n.cdPct();
        }
        return Math.min(pct, 40);
    }

    /** Whether a school's Awakened capstone (spine node 10) is unlocked. */
    public static boolean isAwakened(String uuid, MagicSchool school) {
        return get(uuid).unlocked.contains(school.prefix + "10");
    }

    /**
     * Effective school affinity used by robe set bonuses and technique synergy:
     * unlocked-node count plus equipped robe set bonus.
     */
    public static int getAffinity(ServerPlayer player, MagicSchool school) {
        return unlockedCount(player.getUUID().toString(), school);
    }
}
