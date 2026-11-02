package com.political;
import com.political.mixin.BestiaryDamageMixin;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The Bestiary — a permanent, per-player record of what a player has actually fought.
 *
 * <h2>Milestones</h2>
 * Kills accumulate per {@link BestiaryEntry.MobFamily}. Four milestones grant a small,
 * permanent outgoing-damage bonus against that family:
 * <pre>
 *   10 kills   →  +2%      (Noted)
 *   50 kills   →  +4%      (Studied)
 *  250 kills   →  +7%      (Practised)
 * 1000 kills   →  +10%     (Authority)
 * </pre>
 * On top of that, a player who reaches Authority in several families earns the
 * <b>Scholar of the Wild</b> bonus: +1% per completed family, capped at +5%.
 *
 * <p>The two stack to a hard ceiling of <b>+15%</b> ({@link #MAX_TOTAL_BONUS}). That is
 * deliberately below the swing a single gear upgrade provides — the bestiary is meant to
 * reward familiarity, not replace equipment.</p>
 *
 * <h2>Why this cannot be AFK-farmed</h2>
 * A kill only counts when <b>all</b> of the following hold:
 * <ol>
 *   <li>The player personally dealt at least {@link #REQUIRED_CONTRIBUTION} of the mob's
 *       maximum health. Damage is tracked in a short-lived ledger fed by the damage mixin,
 *       so mobs killed by fall damage, lava, cacti, iron golems, other players' AoE or a
 *       crusher farm credit nobody.</li>
 *   <li>The player was within {@link #CREDIT_RANGE} blocks when the mob died — a player
 *       standing in an AFK box while a farm kills for them is out of range.</li>
 *   <li>Baby mobs never count, which removes breed-and-slaughter loops from the beast family.</li>
 *   <li>A rolling rate limit of {@link #RATE_LIMIT_KILLS} counted kills per
 *       {@link #RATE_LIMIT_WINDOW_MS}. Legitimate combat never reaches it; a mob-grinder
 *       dumping hundreds of kills a minute is clipped to a trickle.</li>
 * </ol>
 * The top milestone is 1000 real, contributed kills in a single family. At the rate limit's
 * ceiling that is still many hours of genuine fighting, and the payoff is 10%.
 */
public final class BestiaryManager {

    private BestiaryManager() {}

    // ── Tunables ────────────────────────────────────────────────────────────

    public static final int[] MILESTONES = {10, 50, 250, 1000};
    public static final double[] MILESTONE_BONUS = {0.02, 0.04, 0.07, 0.10};
    public static final String[] MILESTONE_TITLE = {"Noted", "Studied", "Practised", "Authority"};

    /** Per-completed-family bonus for the Scholar of the Wild rank. */
    private static final double SCHOLAR_PER_FAMILY = 0.01;
    private static final double SCHOLAR_CAP = 0.05;

    /** Absolute ceiling on the multiplier returned by {@link #damageBonus}. */
    public static final double MAX_TOTAL_BONUS = 0.15;

    /** Share of a mob's max health the player must personally deal for the kill to count. */
    private static final double REQUIRED_CONTRIBUTION = 0.50;

    /** How close the player must be when the mob dies. */
    private static final double CREDIT_RANGE = 32.0;

    private static final int RATE_LIMIT_KILLS = 120;
    private static final long RATE_LIMIT_WINDOW_MS = 5L * 60L * 1000L;

    /** Ledger entries older than this are dropped. */
    private static final long LEDGER_TTL_MS = 60L * 1000L;

    // ── State ───────────────────────────────────────────────────────────────

    /** playerUuid -> familyName -> entry */
    private static Map<String, Map<String, BestiaryEntry>> data = new HashMap<>();

    /** Short-lived damage ledger: victim entity uuid -> attacker uuid -> damage dealt. */
    private static final Map<UUID, Map<String, Float>> LEDGER = new HashMap<>();
    private static final Map<UUID, Long> LEDGER_TOUCHED = new HashMap<>();
    private static long lastLedgerSweep = 0L;

    /** playerUuid -> timestamps of recently counted kills (rate limiter). */
    private static final Map<String, Deque<Long>> RECENT_COUNTED = new HashMap<>();

    private static Path file;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static boolean dirty = false;

    // ── Persistence ─────────────────────────────────────────────────────────

    public static void initialize(Path worldRoot) {
        file = worldRoot.resolve("bestiary.json");
        if (Files.exists(file)) {
            try (Reader r = Files.newBufferedReader(file)) {
                Map<String, Map<String, BestiaryEntry>> loaded = GSON.fromJson(
                        r, new TypeToken<Map<String, Map<String, BestiaryEntry>>>() {}.getType());
                if (loaded != null) data = loaded;
            } catch (Exception e) {
                PoliticalServer.LOGGER.warn("Failed to load bestiary.json", e);
            }
        }
        // Recompute milestones so a change to the thresholds is picked up on boot.
        for (Map<String, BestiaryEntry> perPlayer : data.values()) {
            for (BestiaryEntry entry : perPlayer.values()) {
                entry.milestone = milestoneFor(entry.kills);
            }
        }
    }

    public static void save() {
        if (file == null) return;
        try (Writer w = Files.newBufferedWriter(file)) {
            GSON.toJson(data, w);
            dirty = false;
        } catch (Exception e) {
            PoliticalServer.LOGGER.warn("Failed to save bestiary.json", e);
        }
    }

    /** Called from the server tick; batches writes so a busy fight is not a write storm. */
    public static void tick(net.minecraft.server.MinecraftServer server) {
        long now = System.currentTimeMillis();
        if (now - lastLedgerSweep > 15_000L) {
            lastLedgerSweep = now;
            sweepLedger(now);
            if (dirty) save();
        }
    }

    private static void sweepLedger(long now) {
        List<UUID> stale = new ArrayList<>();
        for (Map.Entry<UUID, Long> e : LEDGER_TOUCHED.entrySet()) {
            if (now - e.getValue() > LEDGER_TTL_MS) stale.add(e.getKey());
        }
        for (UUID id : stale) {
            LEDGER.remove(id);
            LEDGER_TOUCHED.remove(id);
        }
    }

    // ── Family classification ───────────────────────────────────────────────

    private static final Map<String, BestiaryEntry.MobFamily> FAMILY_BY_TYPE = new HashMap<>();

    private static void map(BestiaryEntry.MobFamily family, String... paths) {
        for (String p : paths) FAMILY_BY_TYPE.put(p, family);
    }

    static {
        map(BestiaryEntry.MobFamily.UNDEAD,
                "zombie", "husk", "drowned", "zombie_villager", "zombified_piglin",
                "skeleton", "stray", "bogged", "wither_skeleton", "skeleton_horse",
                "phantom", "zoglin", "zombie_horse");
        map(BestiaryEntry.MobFamily.ARTHROPOD,
                "spider", "cave_spider", "silverfish", "endermite", "bee");
        map(BestiaryEntry.MobFamily.NETHERBORN,
                "blaze", "ghast", "happy_ghast", "hoglin", "piglin", "piglin_brute",
                "strider", "magma_cube");
        map(BestiaryEntry.MobFamily.ENDERBORN,
                "enderman", "shulker");
        map(BestiaryEntry.MobFamily.ILLAGER,
                "pillager", "vindicator", "evoker", "illusioner", "vex", "witch", "ravager");
        map(BestiaryEntry.MobFamily.AQUATIC,
                "guardian", "squid", "glow_squid", "dolphin", "cod", "salmon",
                "tropical_fish", "pufferfish", "tadpole", "axolotl", "turtle", "frog");
        map(BestiaryEntry.MobFamily.OOZE,
                "slime");
        map(BestiaryEntry.MobFamily.VOLATILE,
                "creeper", "breeze");
        map(BestiaryEntry.MobFamily.BEAST,
                "cow", "mooshroom", "pig", "sheep", "chicken", "rabbit", "wolf", "cat",
                "ocelot", "fox", "polar_bear", "panda", "llama", "trader_llama", "goat",
                "horse", "donkey", "mule", "camel", "sniffer", "armadillo", "parrot",
                "bat", "allay", "iron_golem", "snow_golem", "creaking");
        map(BestiaryEntry.MobFamily.COLOSSUS,
                "warden", "ender_dragon", "wither", "elder_guardian", "giant");
    }

    /** Registry path for an entity type, e.g. {@code "zombie"}. Never null. */
    public static String typePath(Entity entity) {
        if (entity == null) return "unknown";
        return typePath(entity.getType());
    }

    public static String typePath(EntityType<?> type) {
        try {
            var key = BuiltInRegistries.ENTITY_TYPE.getKey(type);
            return key == null ? "unknown" : key.getPath();
        } catch (Throwable t) {
            return "unknown";
        }
    }

    /** The family this entity belongs to, or null if it is not tracked (villagers, players...). */
    public static BestiaryEntry.MobFamily familyOf(Entity entity) {
        if (entity == null) return null;
        if (entity instanceof ServerPlayer) return null;
        return FAMILY_BY_TYPE.get(typePath(entity));
    }

    /** Every entity-type path known to belong to a family, in declaration order. */
    public static List<String> typesIn(BestiaryEntry.MobFamily family) {
        List<String> out = new ArrayList<>();
        for (Map.Entry<String, BestiaryEntry.MobFamily> e : FAMILY_BY_TYPE.entrySet()) {
            if (e.getValue() == family) out.add(e.getKey());
        }
        out.sort(String::compareTo);
        return out;
    }

    /** Pretty display name for a registry path: {@code "cave_spider"} → {@code "Cave Spider"}. */
    public static String prettyType(String path) {
        if (path == null || path.isEmpty()) return "Unknown";
        StringBuilder sb = new StringBuilder();
        for (String word : path.split("_")) {
            if (word.isEmpty()) continue;
            sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(' ');
        }
        return sb.toString().trim();
    }

    // ── Damage ledger (fed by BestiaryDamageMixin) ──────────────────────────

    /**
     * Records that {@code attacker} dealt {@code amount} damage to {@code victim}. Called on
     * every successful player-sourced hit; cheap and bounded by the sweep above.
     */
    public static void recordDamage(ServerPlayer attacker, LivingEntity victim, float amount) {
        if (attacker == null || victim == null || amount <= 0f) return;
        if (victim instanceof ServerPlayer) return;
        if (familyOf(victim) == null && !victim.entityTags().contains(HuntManager.HUNT_TAG)) return;

        UUID id = victim.getUUID();
        LEDGER.computeIfAbsent(id, k -> new HashMap<>())
                .merge(attacker.getUUID().toString(), amount, Float::sum);
        LEDGER_TOUCHED.put(id, System.currentTimeMillis());
    }

    /** Total damage a player is on record for against this entity. */
    public static float contributionOf(UUID victimId, String playerUuid) {
        Map<String, Float> m = LEDGER.get(victimId);
        if (m == null) return 0f;
        return m.getOrDefault(playerUuid, 0f);
    }

    /** The full contribution map for an entity — used by {@link HuntManager} for payout shares. */
    public static Map<String, Float> contributions(UUID victimId) {
        Map<String, Float> m = LEDGER.get(victimId);
        return m == null ? Map.of() : new LinkedHashMap<>(m);
    }

    public static void forgetEntity(UUID victimId) {
        LEDGER.remove(victimId);
        LEDGER_TOUCHED.remove(victimId);
    }

    // ── Kill recording ──────────────────────────────────────────────────────

    /**
     * Records a kill if it qualifies. Returns true when the kill was counted.
     *
     * <p>Called from the death listener. Every rejection path here is an anti-farm gate;
     * see the class javadoc.</p>
     */
    public static boolean onKill(ServerPlayer killer, LivingEntity victim) {
        if (killer == null || victim == null) return false;
        BestiaryEntry.MobFamily family = familyOf(victim);
        if (family == null) return false;

        // Gate 1: babies never count (no breed-and-slaughter loops).
        if (victim instanceof AgeableMob age && age.isBaby()) return false;

        // Gate 2: the player must have been there.
        if (killer.level() != victim.level()) return false;
        if (killer.distanceToSqr(victim) > CREDIT_RANGE * CREDIT_RANGE) return false;

        // Gate 3: the player must have done the work.
        double maxHealth = 20.0;
        var attr = victim.getAttribute(Attributes.MAX_HEALTH);
        if (attr != null) maxHealth = Math.max(1.0, attr.getValue());
        float dealt = contributionOf(victim.getUUID(), killer.getUUID().toString());
        if (dealt < maxHealth * REQUIRED_CONTRIBUTION) return false;

        // Gate 4: rolling rate limit.
        if (!consumeRateLimit(killer.getUUID().toString())) return false;

        String uuid = killer.getUUID().toString();
        Map<String, BestiaryEntry> perPlayer = data.computeIfAbsent(uuid, k -> new HashMap<>());
        BestiaryEntry entry = perPlayer.computeIfAbsent(family.name(), k -> new BestiaryEntry(family));

        int before = entry.milestone;
        entry.kills++;
        if (entry.byType == null) entry.byType = new HashMap<>();
        entry.byType.merge(typePath(victim), 1, Integer::sum);
        long now = System.currentTimeMillis();
        if (entry.firstKillAt == 0L) entry.firstKillAt = now;
        entry.lastKillAt = now;
        entry.milestone = milestoneFor(entry.kills);
        dirty = true;

        if (entry.milestone > before) {
            announceMilestone(killer, family, entry);
        }
        forgetEntity(victim.getUUID());
        return true;
    }

    private static boolean consumeRateLimit(String uuid) {
        long now = System.currentTimeMillis();
        Deque<Long> q = RECENT_COUNTED.computeIfAbsent(uuid, k -> new ArrayDeque<>());
        while (!q.isEmpty() && now - q.peekFirst() > RATE_LIMIT_WINDOW_MS) q.pollFirst();
        if (q.size() >= RATE_LIMIT_KILLS) return false;
        q.addLast(now);
        return true;
    }

    private static void announceMilestone(ServerPlayer player, BestiaryEntry.MobFamily family,
                                          BestiaryEntry entry) {
        int idx = entry.milestone - 1;
        if (idx < 0 || idx >= MILESTONE_TITLE.length) return;

        player.sendSystemMessage(Component.literal("─────────────────────").withStyle(ChatFormatting.DARK_GRAY));
        player.sendSystemMessage(Component.literal("✦ BESTIARY: " + MILESTONE_TITLE[idx].toUpperCase())
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        player.sendSystemMessage(Component.literal("  " + family.label()
                + " §7— §f" + entry.kills + " §7kills"));
        player.sendSystemMessage(Component.literal("  §7Damage vs this family: §a+"
                + Math.round(MILESTONE_BONUS[idx] * 100) + "%"));
        player.sendSystemMessage(Component.literal("─────────────────────").withStyle(ChatFormatting.DARK_GRAY));

        if (player.level() instanceof ServerLevel sl) {
            sl.playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP,
                    SoundSource.PLAYERS, 0.6f, 1.5f);
        }
    }

    public static int milestoneFor(int kills) {
        int m = 0;
        for (int threshold : MILESTONES) {
            if (kills >= threshold) m++;
        }
        return m;
    }

    // ── Bonus lookup ────────────────────────────────────────────────────────

    /**
     * Outgoing-damage multiplier for {@code player} against {@code target}.
     *
     * <p>Returns {@code 1.0} when there is no bonus, and never more than
     * {@code 1.0 + }{@link #MAX_TOTAL_BONUS}. Safe to call on every hit: it is a couple of
     * map lookups and no allocation.</p>
     *
     * @return a multiplier to apply to the outgoing damage figure
     */
    public static double damageBonus(ServerPlayer player, Entity target) {
        if (player == null || target == null) return 1.0;
        BestiaryEntry.MobFamily family = familyOf(target);
        if (family == null) return 1.0;

        String uuid = player.getUUID().toString();
        double bonus = familyBonus(uuid, family) + scholarBonus(uuid);
        if (bonus <= 0) return 1.0;
        return 1.0 + Math.min(MAX_TOTAL_BONUS, bonus);
    }

    /** The bonus fraction (not multiplier) from milestones in one family. */
    public static double familyBonus(String uuid, BestiaryEntry.MobFamily family) {
        BestiaryEntry entry = entry(uuid, family);
        if (entry == null || entry.milestone <= 0) return 0.0;
        int idx = Math.min(entry.milestone, MILESTONE_BONUS.length) - 1;
        return MILESTONE_BONUS[idx];
    }

    /** +1% per family at full Authority, capped at +5%. */
    public static double scholarBonus(String uuid) {
        int complete = 0;
        for (BestiaryEntry.MobFamily f : BestiaryEntry.MobFamily.values()) {
            BestiaryEntry e = entry(uuid, f);
            if (e != null && e.milestone >= MILESTONES.length) complete++;
        }
        return Math.min(SCHOLAR_CAP, complete * SCHOLAR_PER_FAMILY);
    }

    public static int completedFamilies(String uuid) {
        int complete = 0;
        for (BestiaryEntry.MobFamily f : BestiaryEntry.MobFamily.values()) {
            BestiaryEntry e = entry(uuid, f);
            if (e != null && e.milestone >= MILESTONES.length) complete++;
        }
        return complete;
    }

    // ── Accessors for the GUI ───────────────────────────────────────────────

    /** The entry for a player/family, or null if they have never recorded a kill there. */
    public static BestiaryEntry entry(String uuid, BestiaryEntry.MobFamily family) {
        Map<String, BestiaryEntry> perPlayer = data.get(uuid);
        if (perPlayer == null) return null;
        return perPlayer.get(family.name());
    }

    /** Kills in a family (0 when unrecorded). */
    public static int kills(String uuid, BestiaryEntry.MobFamily family) {
        BestiaryEntry e = entry(uuid, family);
        return e == null ? 0 : e.kills;
    }

    /** Total qualifying kills across every family. */
    public static int totalKills(String uuid) {
        Map<String, BestiaryEntry> perPlayer = data.get(uuid);
        if (perPlayer == null) return 0;
        int total = 0;
        for (BestiaryEntry e : perPlayer.values()) total += e.kills;
        return total;
    }

    /** Kills still needed for the next milestone, or -1 when the family is complete. */
    public static int killsToNext(String uuid, BestiaryEntry.MobFamily family) {
        int kills = kills(uuid, family);
        for (int threshold : MILESTONES) {
            if (kills < threshold) return threshold - kills;
        }
        return -1;
    }

    /** The next milestone threshold, or -1 when complete. */
    public static int nextThreshold(String uuid, BestiaryEntry.MobFamily family) {
        int kills = kills(uuid, family);
        for (int threshold : MILESTONES) {
            if (kills < threshold) return threshold;
        }
        return -1;
    }

    /** A 20-cell progress bar toward the next milestone in this family. */
    public static String progressBar(String uuid, BestiaryEntry.MobFamily family) {
        int kills = kills(uuid, family);
        int next = nextThreshold(uuid, family);
        if (next < 0) return "§6||||||||||||||||||||";

        int previous = 0;
        for (int threshold : MILESTONES) {
            if (threshold == next) break;
            previous = threshold;
        }
        int span = Math.max(1, next - previous);
        int done = Math.max(0, kills - previous);
        int filled = (int) Math.round(Math.min(1.0, (double) done / span) * 20);

        StringBuilder sb = new StringBuilder(family.legacy);
        for (int i = 0; i < 20; i++) {
            if (i == filled) sb.append("§8");
            sb.append('|');
        }
        return sb.toString();
    }

    /** Admin/debug: wipe one player's record. */
    public static void reset(String uuid) {
        data.remove(uuid);
        RECENT_COUNTED.remove(uuid);
        dirty = true;
        save();
    }
}
