package com.political;


import com.political.bounty.SlayerItems;
import com.political.economy.CoinManager;
import com.political.stats.PlayerStatsManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks collection progress and pays out the tiers.
 *
 * <p>Counting happens where the material actually enters a player's hands rather than on a timer, so
 * the number means something: blocks broken, mobs killed and fish caught all funnel through
 * {@link #record}. Progress is per player and permanent, and the stat bonuses it grants are folded
 * into the stats engine so they behave exactly like a bonus from a piece of gear.</p>
 */
public final class CollectionManager {

    private CollectionManager() {}

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path savePath;

    /** player uuid to collection key to lifetime count. */
    private static final Map<String, Map<String, Long>> COUNTS = new ConcurrentHashMap<>();
    /** player uuid to collection key to the highest tier already paid, so nothing pays twice. */
    private static final Map<String, Map<String, Integer>> PAID = new ConcurrentHashMap<>();

    // ── Persistence ───────────────────────────────────────────────────────

    public static void initialize(Path worldRoot) {
        savePath = worldRoot.resolve("political").resolve("collections.json");
        load();
    }

    private static final class Save {
        Map<String, Map<String, Long>> counts = new HashMap<>();
        Map<String, Map<String, Integer>> paid = new HashMap<>();
    }

    private static void load() {
        COUNTS.clear();
        PAID.clear();
        if (savePath == null || !Files.exists(savePath)) return;
        try (Reader r = Files.newBufferedReader(savePath)) {
            java.lang.reflect.Type t = new TypeToken<Save>() {}.getType();
            Save data = GSON.fromJson(r, t);
            if (data == null) return;
            if (data.counts != null) COUNTS.putAll(data.counts);
            if (data.paid != null) PAID.putAll(data.paid);
        } catch (Exception e) {
            System.out.println("[Collections] Could not read progress: " + e.getMessage());
        }
    }

    public static void save() {
        if (savePath == null) return;
        try {
            Files.createDirectories(savePath.getParent());
            Save data = new Save();
            data.counts = new HashMap<>(COUNTS);
            data.paid = new HashMap<>(PAID);
            try (Writer w = Files.newBufferedWriter(savePath)) {
                GSON.toJson(data, w);
            }
        } catch (Exception e) {
            System.out.println("[Collections] Could not write progress: " + e.getMessage());
        }
    }

    // ── Reading ───────────────────────────────────────────────────────────

    public static long countOf(ServerPlayer player, CollectionType c) {
        return countOf(player.getUUID().toString(), c);
    }

    public static long countOf(String uuid, CollectionType c) {
        Map<String, Long> mine = COUNTS.get(uuid);
        return mine == null ? 0L : mine.getOrDefault(c.key(), 0L);
    }

    public static int tierOf(ServerPlayer player, CollectionType c) {
        return c.tierFor(countOf(player, c));
    }

    /** How many collections this player has taken all the way to nine. */
    public static int maxedCount(ServerPlayer player) {
        int n = 0;
        for (CollectionType c : CollectionType.values()) {
            if (tierOf(player, c) >= CollectionType.MAX_TIER) n++;
        }
        return n;
    }

    /** Total tiers earned across every collection, which is the headline progress number. */
    public static int totalTiers(ServerPlayer player) {
        int n = 0;
        for (CollectionType c : CollectionType.values()) n += tierOf(player, c);
        return n;
    }

    // ── Counting ──────────────────────────────────────────────────────────

    /** Adds to a collection and pays out any tier the addition crossed. */
    public static void record(ServerPlayer player, CollectionType c, long amount) {
        if (c == null || amount <= 0) return;
        String uuid = player.getUUID().toString();

        Map<String, Long> mine = COUNTS.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>());
        long before = mine.getOrDefault(c.key(), 0L);
        long after = before + amount;
        mine.put(c.key(), after);

        int tierBefore = c.tierFor(before);
        int tierAfter = c.tierFor(after);
        if (tierAfter > tierBefore) awardTiers(player, c, tierBefore, tierAfter);
    }

    /** Matches a stack against every collection and counts it if it belongs to one. */
    public static void recordStack(ServerPlayer player, ItemStack stack) {
        if (stack == null || stack.isEmpty()) return;
        // Custom items are their own reward and would otherwise let a crafted piece feed a
        // collection it never came out of the ground for.
        if (SlayerItems.getCustomItemId(stack) != null) return;
        for (CollectionType c : CollectionType.values()) {
            if (stack.is(c.material)) {
                record(player, c, stack.getCount());
                return;
            }
        }
    }

    /**
     * Counts one broken block toward whichever collection its item form belongs to.
     *
     * <p>Deliberately one per block rather than per dropped item. A collection measures how much of
     * something a player has worked through, and a fortune enchant multiplying that by four would
     * make the number about the pickaxe instead.</p>
     */
    public static void recordBlock(ServerPlayer player,
                                   net.minecraft.world.level.block.state.BlockState state) {
        if (state == null) return;
        CollectionType c = forBlock(state);
        if (c != null) record(player, c, 1);
    }

    /**
     * Which collection a block feeds.
     *
     * <p>The block's own item is not enough on its own. An iron collection counts ingots but the
     * block is iron ore, and a wheat collection counts wheat but the block is a crop whose item is
     * seeds, so matching the block item straight against the collection material silently misses
     * every ore and every crop, which is most of mining and all of farming.</p>
     */
    private static CollectionType forBlock(net.minecraft.world.level.block.state.BlockState state) {
        net.minecraft.world.level.block.Block b = state.getBlock();

        // Ores, both stone and deepslate, mapped to what they are worth rather than what they are.
        if (b == Blocks.IRON_ORE || b == Blocks.DEEPSLATE_IRON_ORE) return CollectionType.IRON;
        if (b == Blocks.GOLD_ORE || b == Blocks.DEEPSLATE_GOLD_ORE || b == Blocks.NETHER_GOLD_ORE) return CollectionType.GOLD;
        if (b == Blocks.COAL_ORE || b == Blocks.DEEPSLATE_COAL_ORE) return CollectionType.COAL;
        if (b == Blocks.DIAMOND_ORE || b == Blocks.DEEPSLATE_DIAMOND_ORE) return CollectionType.DIAMOND;
        if (b == Blocks.EMERALD_ORE || b == Blocks.DEEPSLATE_EMERALD_ORE) return CollectionType.EMERALD;
        if (b == Blocks.LAPIS_ORE || b == Blocks.DEEPSLATE_LAPIS_ORE) return CollectionType.LAPIS;
        if (b == Blocks.REDSTONE_ORE || b == Blocks.DEEPSLATE_REDSTONE_ORE) return CollectionType.REDSTONE;
        if (b == Blocks.COPPER_ORE || b == Blocks.DEEPSLATE_COPPER_ORE) return CollectionType.COPPER;
        if (b == Blocks.NETHER_QUARTZ_ORE) return CollectionType.QUARTZ;
        if (b == Blocks.ANCIENT_DEBRIS) return CollectionType.ANCIENT_DEBRIS;
        if (b == Blocks.OBSIDIAN || b == Blocks.CRYING_OBSIDIAN) return CollectionType.OBSIDIAN;
        if (b == Blocks.AMETHYST_CLUSTER || b == Blocks.AMETHYST_BLOCK) return CollectionType.AMETHYST;
        if (b == Blocks.GLOWSTONE) return CollectionType.GLOWSTONE;
        if (b == Blocks.SCULK || b == Blocks.SCULK_CATALYST || b == Blocks.SCULK_SENSOR) return CollectionType.SCULK;

        // Crops, which drop produce rather than the block item.
        if (b == Blocks.WHEAT) return CollectionType.WHEAT;
        if (b == Blocks.CARROTS) return CollectionType.CARROT;
        if (b == Blocks.POTATOES) return CollectionType.POTATO;
        if (b == Blocks.MELON) return CollectionType.MELON;
        if (b == Blocks.PUMPKIN) return CollectionType.PUMPKIN;
        if (b == Blocks.SUGAR_CANE) return CollectionType.SUGAR_CANE;
        if (b == Blocks.COCOA) return CollectionType.COCOA;
        if (b == Blocks.NETHER_WART) return CollectionType.NETHER_WART;
        if (b == Blocks.CACTUS) return CollectionType.CACTUS;
        if (b == Blocks.RED_MUSHROOM || b == Blocks.BROWN_MUSHROOM) return CollectionType.MUSHROOM;
        if (b == Blocks.KELP || b == Blocks.KELP_PLANT) return CollectionType.KELP;
        if (b == Blocks.LILY_PAD) return CollectionType.LILY_PAD;
        if (b == Blocks.SEA_PICKLE) return CollectionType.HEART_OF_THE_SEA;

        // Everything else whose block item is the collection material already, which covers the
        // logs, the prismarine and the rest of foraging.
        net.minecraft.world.item.Item asItem = b.asItem();
        if (asItem == net.minecraft.world.item.Items.AIR) return null;
        for (CollectionType c : CollectionType.values()) {
            if (c.material == asItem) return c;
        }
        return null;
    }

    /**
     * Counts a kill against the combat collections. Block drops are read straight off the loot
     * table, but a mob's drops land as loose entities after the kill, so this maps the victim to its
     * signature material instead. That keeps the count honest and stops a looting enchant inflating
     * a collection that is supposed to measure how much you fought.
     */
    public static void onKill(ServerPlayer player, net.minecraft.world.entity.LivingEntity victim) {
        if (victim == null) return;
        CollectionType c = forVictim(victim);
        if (c != null) record(player, c, 1);
    }

    private static CollectionType forVictim(net.minecraft.world.entity.LivingEntity victim) {
        net.minecraft.world.entity.EntityType<?> t = victim.getType();
        if (t == net.minecraft.world.entity.EntityTypes.ZOMBIE
                || t == net.minecraft.world.entity.EntityTypes.HUSK
                || t == net.minecraft.world.entity.EntityTypes.DROWNED
                || t == net.minecraft.world.entity.EntityTypes.ZOMBIE_VILLAGER) return CollectionType.ROTTEN_FLESH;
        if (t == net.minecraft.world.entity.EntityTypes.SKELETON
                || t == net.minecraft.world.entity.EntityTypes.STRAY) return CollectionType.BONE;
        if (t == net.minecraft.world.entity.EntityTypes.WITHER_SKELETON) return CollectionType.WITHER_SKULL;
        if (t == net.minecraft.world.entity.EntityTypes.SPIDER) return CollectionType.STRING;
        if (t == net.minecraft.world.entity.EntityTypes.CAVE_SPIDER) return CollectionType.SPIDER_EYE;
        if (t == net.minecraft.world.entity.EntityTypes.CREEPER) return CollectionType.GUNPOWDER;
        if (t == net.minecraft.world.entity.EntityTypes.ENDERMAN) return CollectionType.ENDER_PEARL;
        if (t == net.minecraft.world.entity.EntityTypes.BLAZE) return CollectionType.BLAZE_ROD;
        if (t == net.minecraft.world.entity.EntityTypes.GHAST) return CollectionType.GHAST_TEAR;
        if (t == net.minecraft.world.entity.EntityTypes.SLIME
                || t == net.minecraft.world.entity.EntityTypes.MAGMA_CUBE) return CollectionType.SLIME;
        return null;
    }

    private static void awardTiers(ServerPlayer player, CollectionType c, int from, int to) {
        String uuid = player.getUUID().toString();
        Map<String, Integer> paid = PAID.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>());
        int alreadyPaid = paid.getOrDefault(c.key(), 0);

        for (int tier = from + 1; tier <= to; tier++) {
            if (tier <= alreadyPaid) continue;
            long coins = c.payoutFor(tier);
            CoinManager.giveCoinsQuiet(player, coins);
            paid.put(c.key(), tier);

            player.sendSystemMessage(Component.literal(""));
            player.sendSystemMessage(Component.literal("  " + c.colour + "§l" + c.display
                    + " CollectionType " + roman(tier)));
            player.sendSystemMessage(Component.literal("  §7Reward: §6" + GuiKit.num(coins)
                    + " coins §7and §a+" + c.statBonusFor(tier) + " " + statLabel(c.statKey)));
            if (tier >= CollectionType.MAX_TIER) {
                player.sendSystemMessage(Component.literal("  §d§lMaxed. Nothing further to find here."));
            }
            player.sendSystemMessage(Component.literal(""));

            if (player.level() instanceof ServerLevel level) {
                level.playSound(null, player.blockPosition(),
                        tier >= CollectionType.MAX_TIER ? SoundEvents.UI_TOAST_CHALLENGE_COMPLETE
                                : SoundEvents.PLAYER_LEVELUP,
                        SoundSource.PLAYERS, 0.7f, tier >= CollectionType.MAX_TIER ? 1.0f : 1.4f);
                Vfx.bloom(level, player.position().add(0, 1, 0));
            }
        }
        save();
    }

    // ── Stat bonuses ──────────────────────────────────────────────────────

    /**
     * Folds every earned tier into a stat sheet. Called from the stats engine so a collection bonus
     * is indistinguishable from a bonus on a piece of armour.
     */
    public static void applyBonuses(ServerPlayer player, PlayerStatsManager.StatSheet sheet) {
        String uuid = player.getUUID().toString();
        Map<String, Long> mine = COUNTS.get(uuid);
        if (mine == null || mine.isEmpty()) return;

        for (CollectionType c : CollectionType.values()) {
            long count = mine.getOrDefault(c.key(), 0L);
            if (count <= 0) continue;
            int tier = c.tierFor(count);
            if (tier <= 0) continue;
            PlayerStatsManager.Stat stat = statFor(c.statKey);
            if (stat != null) sheet.addExternal(stat, c.statBonusFor(tier));
        }
    }

    private static PlayerStatsManager.Stat statFor(String key) {
        return switch (key) {
            case "strength" -> PlayerStatsManager.Stat.STRENGTH;
            case "defense" -> PlayerStatsManager.Stat.DEFENSE;
            case "health" -> PlayerStatsManager.Stat.HEALTH;
            case "intelligence" -> PlayerStatsManager.Stat.INTELLIGENCE;
            case "crit_chance" -> PlayerStatsManager.Stat.CRIT_CHANCE;
            case "crit_damage" -> PlayerStatsManager.Stat.CRIT_DAMAGE;
            case "ferocity" -> PlayerStatsManager.Stat.FEROCITY;
            case "speed" -> PlayerStatsManager.Stat.SPEED;
            default -> null;
        };
    }

    public static String statLabel(String key) {
        PlayerStatsManager.Stat s = statFor(key);
        return s == null ? key : s.displayName;
    }

    public static ChatFormatting statColour(String key) {
        PlayerStatsManager.Stat s = statFor(key);
        return s == null ? ChatFormatting.WHITE : s.color;
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    public static String roman(int n) {
        return switch (n) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            case 6 -> "VI";
            case 7 -> "VII";
            case 8 -> "VIII";
            case 9 -> "IX";
            default -> String.valueOf(n);
        };
    }

    public static void clear(UUID uuid) {
        // Progress is permanent, so a disconnect drops nothing. The hook exists so the shape of
        // this manager matches the others.
    }
}
