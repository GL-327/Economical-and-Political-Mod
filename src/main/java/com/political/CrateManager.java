package com.political;


import com.political.economy.CoinManager;
import com.political.recipe.RecipeConfigManager;
import com.political.recipe.SlayerRecipes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Runs the crates.
 *
 * <p>Rewards are drawn from the live recipe catalogue rather than a hand written table, so a crate
 * opened today can contain anything the mod knows how to make, including gear lines added after the
 * crate itself was written. Band is decided first from the crate's published odds, then an item is
 * picked from whatever sits in that band.</p>
 *
 * <p>Two things keep this on the right side of being a shop rather than a slot machine. Odds are
 * printed in full before purchase, and a pity counter guarantees a good pull after a published
 * number of bad ones, so a losing streak has a known ceiling.</p>
 */
public final class CrateManager {

    private CrateManager() {}

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Random RNG = new Random();
    private static Path savePath;

    /** player uuid to crate key to how many keys they hold. */
    private static final Map<String, Map<String, Integer>> KEYS = new HashMap<>();
    /** player uuid to crate key to pulls since their last good one. */
    private static final Map<String, Map<String, Integer>> PITY = new HashMap<>();
    /** Newest first, capped, purely so the GUI can show what has been coming out. */
    private static final List<String> FEED = new ArrayList<>();
    private static final int FEED_MAX = 40;

    // ── Persistence ───────────────────────────────────────────────────────

    private static final class Save {
        Map<String, Map<String, Integer>> keys = new HashMap<>();
        Map<String, Map<String, Integer>> pity = new HashMap<>();
        List<String> feed = new ArrayList<>();
    }

    public static void initialize(Path worldRoot) {
        savePath = worldRoot.resolve("political").resolve("crates.json");
        KEYS.clear();
        PITY.clear();
        FEED.clear();
        if (savePath == null || !Files.exists(savePath)) return;
        try (Reader r = Files.newBufferedReader(savePath)) {
            java.lang.reflect.Type t = new TypeToken<Save>() {}.getType();
            Save data = GSON.fromJson(r, t);
            if (data == null) return;
            if (data.keys != null) KEYS.putAll(data.keys);
            if (data.pity != null) PITY.putAll(data.pity);
            if (data.feed != null) FEED.addAll(data.feed);
        } catch (Exception e) {
            System.out.println("[Crates] Could not read state: " + e.getMessage());
        }
    }

    public static void save() {
        if (savePath == null) return;
        try {
            Files.createDirectories(savePath.getParent());
            Save data = new Save();
            data.keys = new HashMap<>(KEYS);
            data.pity = new HashMap<>(PITY);
            data.feed = new ArrayList<>(FEED);
            try (Writer w = Files.newBufferedWriter(savePath)) {
                GSON.toJson(data, w);
            }
        } catch (Exception e) {
            System.out.println("[Crates] Could not write state: " + e.getMessage());
        }
    }

    // ── Keys ──────────────────────────────────────────────────────────────

    public static int keysOf(ServerPlayer player, Crate crate) {
        Map<String, Integer> mine = KEYS.get(player.getUUID().toString());
        return mine == null ? 0 : mine.getOrDefault(crate.key(), 0);
    }

    public static int totalKeys(ServerPlayer player) {
        Map<String, Integer> mine = KEYS.get(player.getUUID().toString());
        if (mine == null) return 0;
        int n = 0;
        for (int v : mine.values()) n += v;
        return n;
    }

    public static void giveKeys(ServerPlayer player, Crate crate, int count) {
        if (count <= 0) return;
        KEYS.computeIfAbsent(player.getUUID().toString(), k -> new HashMap<>())
                .merge(crate.key(), count, Integer::sum);
        save();
    }

    /** Buys keys with coins. Returns how many were actually bought. */
    public static int buyKeys(ServerPlayer player, Crate crate, int count) {
        if (count <= 0) return 0;
        long cost = crate.price * count;
        if (!CoinManager.removeCoins(player, cost)) {
            player.sendSystemMessage(Component.literal("§cThat costs " + GuiKit.num(cost)
                    + " coins and you have " + GuiKit.num(CoinManager.getCoinsLong(player)) + "."));
            return 0;
        }
        giveKeys(player, crate, count);
        player.sendSystemMessage(Component.literal("§aBought §f" + count + " "
                + crate.display + (count == 1 ? " key" : " keys") + " §7for §6"
                + GuiKit.num(cost) + " coins§7."));
        return count;
    }

    private static boolean consumeKey(ServerPlayer player, Crate crate) {
        Map<String, Integer> mine = KEYS.get(player.getUUID().toString());
        if (mine == null) return false;
        int have = mine.getOrDefault(crate.key(), 0);
        if (have <= 0) return false;
        mine.put(crate.key(), have - 1);
        return true;
    }

    // ── Opening ───────────────────────────────────────────────────────────

    public static final class Pull {
        public Crate crate;
        public Crate.Band band;
        public ItemStack reward = ItemStack.EMPTY;
        public boolean pityFired;
        public String label = "";
    }

    /** Opens one crate. Returns null when the player has no key for it. */
    public static Pull open(ServerPlayer player, Crate crate) {
        if (!consumeKey(player, crate)) {
            player.sendSystemMessage(Component.literal("§cYou have no " + crate.display + " key."));
            return null;
        }

        String uuid = player.getUUID().toString();
        Map<String, Integer> pity = PITY.computeIfAbsent(uuid, k -> new HashMap<>());
        int since = pity.getOrDefault(crate.key(), 0);

        Crate.Band band = crate.roll(RNG);
        boolean pityFired = false;
        if (since + 1 >= crate.pityAfter() && band.ordinal() < crate.pityBand().ordinal()) {
            band = crate.pityBand();
            pityFired = true;
        }

        // A good pull resets the counter, a poor one advances it.
        if (band.ordinal() >= crate.pityBand().ordinal()) {
            pity.put(crate.key(), 0);
        } else {
            pity.put(crate.key(), since + 1);
        }

        Pull pull = new Pull();
        pull.crate = crate;
        pull.band = band;
        pull.pityFired = pityFired;
        pull.reward = rollReward(band);
        pull.label = pull.reward.isEmpty() ? "Coins" : pull.reward.getHoverName().getString();

        // Nothing is ever a total loss. An empty pool pays coins instead of dropping air.
        if (pull.reward.isEmpty()) {
            long coins = crate.price / 2 + (long) band.ordinal() * crate.price / 4;
            CoinManager.giveCoins(player, coins);
            pull.label = GuiKit.num(coins) + " coins";
        } else if (!player.getInventory().add(pull.reward.copy())) {
            player.drop(pull.reward.copy(), false);
        }

        record(player, crate, band, pull.label);
        celebrate(player, crate, band);
        save();
        return pull;
    }

    /**
     * Picks a reward for a band out of the live recipe catalogue.
     *
     * <p>Band is inferred from the recipe name because that is what actually tracks value here: the
     * big combinatorial gear lines are the good pulls, and raw materials are the filler.</p>
     */
    private static ItemStack rollReward(Crate.Band band) {
        try {
            List<ItemStack> pool = poolFor(band);
            if (pool.isEmpty()) return ItemStack.EMPTY;
            return pool.get(RNG.nextInt(pool.size())).copy();
        } catch (Exception e) {
            return ItemStack.EMPTY;
        }
    }

    // ── Reward index ──────────────────────────────────────────────────────

    /**
     * Bands to the items in them, built once rather than per lookup.
     *
     * <p>This existed as a full walk of the recipe catalogue on every single call, which meant one
     * pull scanned seven thousand recipes and the odds screen did it ten times over just to draw
     * itself. On the server thread that is a visible stall for something nobody would think to
     * blame. The catalogue only changes when recipes are registered, so the index is rebuilt when
     * the recipe count moves and reused otherwise.</p>
     */
    private static final java.util.EnumMap<Crate.Band, List<ItemStack>> POOLS =
            new java.util.EnumMap<>(Crate.Band.class);
    private static int indexedAtCount = -1;

    private static synchronized void rebuildIndexIfStale() {
        List<SlayerRecipes.Recipe> recipes;
        try {
            recipes = RecipeConfigManager.getRecipes();
        } catch (Exception e) {
            return;
        }
        int count = recipes.size();
        if (count == indexedAtCount && !POOLS.isEmpty()) return;

        POOLS.clear();
        for (Crate.Band b : Crate.Band.values()) POOLS.put(b, new ArrayList<>());
        try {
            for (SlayerRecipes.Recipe r : recipes) {
                if (r.result == null || r.result.isEmpty()) continue;
                POOLS.get(bandOf(r.name)).add(r.result);
            }
            indexedAtCount = count;
        } catch (Exception e) {
            // A catalogue that will not enumerate leaves the pools empty, which pays coins
            // instead of items rather than failing a pull.
        }
    }

    private static List<ItemStack> poolFor(Crate.Band band) {
        rebuildIndexIfStale();
        List<ItemStack> pool = POOLS.get(band);
        return pool == null ? java.util.Collections.emptyList() : pool;
    }

    /** Which band a given recipe belongs to, from its name. */
    public static Crate.Band bandOf(String recipeName) {
        String n = recipeName == null ? "" : recipeName.toLowerCase(java.util.Locale.ROOT);
        if (n.startsWith("vx regalia ") || n.startsWith("vx star ")) return Crate.Band.LEGENDARY;
        if (n.startsWith("vx aether ") || n.startsWith("vx rune ")
                || n.startsWith("vx sigil ")) return Crate.Band.EPIC;
        if (n.startsWith("vx beast ") || n.startsWith("vx storm ")
                || n.startsWith("vx way ") || n.startsWith("vx cog ")) return Crate.Band.RARE;
        if (n.startsWith("vx catalyst ") || n.contains("enchanted ")) return Crate.Band.UNCOMMON;
        return Crate.Band.COMMON;
    }

    private static void celebrate(ServerPlayer player, Crate crate, Crate.Band band) {
        if (!(player.level() instanceof ServerLevel level)) return;
        boolean big = band.ordinal() >= Crate.Band.EPIC.ordinal();
        level.playSound(null, player.blockPosition(),
                big ? SoundEvents.UI_TOAST_CHALLENGE_COMPLETE : SoundEvents.NOTE_BLOCK_BELL.value(),
                SoundSource.PLAYERS, 0.8f, big ? 1.0f : 1.4f);
        if (big) {
            Vfx.bloom(level, player.position().add(0, 1, 0));
        }
    }

    private static void record(ServerPlayer player, Crate crate, Crate.Band band, String label) {
        String line = band.colour + band.label + " §8| §7" + player.getName().getString()
                + " §8drew §f" + label + " §8from a " + crate.display;
        FEED.add(0, line);
        while (FEED.size() > FEED_MAX) FEED.remove(FEED.size() - 1);

        // Only the genuinely good pulls are worth interrupting everybody for.
        if (band.ordinal() >= Crate.Band.EPIC.ordinal() && PoliticalServer.server != null) {
            for (ServerPlayer p : PoliticalServer.server.getPlayerList().getPlayers()) {
                p.sendSystemMessage(Component.literal("  " + band.colour + "§l" + band.label
                        + " §r§7" + player.getName().getString() + " drew §f" + label));
            }
        }
    }

    public static List<String> feed() {
        return new ArrayList<>(FEED);
    }

    public static int pitySince(ServerPlayer player, Crate crate) {
        Map<String, Integer> mine = PITY.get(player.getUUID().toString());
        return mine == null ? 0 : mine.getOrDefault(crate.key(), 0);
    }

    /** A representative sample of what a band actually contains, for the odds screen. */
    public static List<String> sampleOf(Crate.Band band, int limit) {
        List<String> out = new ArrayList<>();
        try {
            List<ItemStack> pool = poolFor(band);
            for (int i = 0; i < pool.size() && out.size() < limit; i++) {
                out.add(pool.get(i).getHoverName().getString());
            }
        } catch (Exception ignored) {
            // An unavailable catalogue simply shows nothing rather than failing the screen.
        }
        return out;
    }

    /** How many distinct items sit in a band right now. */
    public static int poolSize(Crate.Band band) {
        try {
            return poolFor(band).size();
        } catch (Exception e) {
            return 0;
        }
    }

    public static void clear(UUID uuid) {
        // Keys and pity are owned progress and deliberately survive a disconnect.
    }
}
