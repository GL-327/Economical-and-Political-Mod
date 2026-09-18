package com.political;


import com.political.economy.BazaarManager;
import com.political.economy.CoinManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Minions: placed workers that gather one material on a timer.
 *
 * <p>This closes the loop the last two systems opened. A minion produces a material, the material
 * feeds a collection, and the collection pays a permanent stat while the surplus goes to the bazaar.
 * None of the three is much on its own and together they are the reason to keep a base.</p>
 *
 * <p>Two rules keep it from becoming the only thing worth doing. A minion only runs while its chunk
 * is loaded, so this is not an offline printer, and its output feeds collections at a reduced rate,
 * so hand gathering is always the faster way to a tier. It is meant to be a trickle you come home to
 * rather than a replacement for playing.</p>
 */
public final class MinionManager {

    private MinionManager() {}

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path savePath;

    /** How many minions one player may have placed at once. */
    public static final int MAX_PER_PLAYER = 12;
    /** Fraction of minion output that counts toward collections. */
    private static final double COLLECTION_RATE = 0.25;
    /** Tiers a minion can be upgraded through. */
    public static final int MAX_TIER = 11;

    public static final class Minion {
        public String id = UUID.randomUUID().toString();
        public String ownerUuid = "";
        public String ownerName = "";
        public String product = "";
        public int tier = 1;
        public String dim = "minecraft:overworld";
        public int x;
        public int y;
        public int z;
        /** Units sitting in the minion waiting to be collected. */
        public int stored;
        /** Server tick this minion last produced on. */
        public long lastTick;

        public CollectionType type() {
            return CollectionType.byKey(product);
        }
    }

    private static final List<Minion> MINIONS = new ArrayList<>();

    // ── Persistence ───────────────────────────────────────────────────────

    public static void initialize(Path worldRoot) {
        savePath = worldRoot.resolve("political").resolve("minions.json");
        MINIONS.clear();
        if (savePath == null || !Files.exists(savePath)) return;
        try (Reader r = Files.newBufferedReader(savePath)) {
            java.lang.reflect.Type t = new TypeToken<List<Minion>>() {}.getType();
            List<Minion> data = GSON.fromJson(r, t);
            if (data != null) MINIONS.addAll(data);
        } catch (Exception e) {
            System.out.println("[Minions] Could not read placements: " + e.getMessage());
        }
    }

    public static void save() {
        if (savePath == null) return;
        try {
            Files.createDirectories(savePath.getParent());
            try (Writer w = Files.newBufferedWriter(savePath)) {
                GSON.toJson(MINIONS, w);
            }
        } catch (Exception e) {
            System.out.println("[Minions] Could not write placements: " + e.getMessage());
        }
    }

    // ── Rules ─────────────────────────────────────────────────────────────

    /** Seconds between one unit of output. Tier one is slow on purpose. */
    public static int intervalSeconds(int tier) {
        return Math.max(4, 32 - (tier - 1) * 2);
    }

    /** How many units a minion holds before it stalls. */
    public static int capacity(int tier) {
        return 64 * Math.max(1, tier);
    }

    /** What it costs to take a minion from its current tier to the next. */
    public static long upgradeCost(CollectionType c, int fromTier) {
        long base = BazaarManager.baseValue(c);
        return base * 200L * (long) fromTier * fromTier;
    }

    /** What placing the first one costs. */
    public static long placementCost(CollectionType c) {
        return BazaarManager.baseValue(c) * 500L;
    }

    public static List<Minion> minionsOf(ServerPlayer player) {
        List<Minion> out = new ArrayList<>();
        String uuid = player.getUUID().toString();
        for (Minion m : MINIONS) {
            if (m.ownerUuid.equals(uuid)) out.add(m);
        }
        return out;
    }

    public static int countOf(ServerPlayer player) {
        return minionsOf(player).size();
    }

    // ── Placing and removing ──────────────────────────────────────────────

    public static boolean place(ServerPlayer player, CollectionType c) {
        if (countOf(player) >= MAX_PER_PLAYER) {
            player.sendSystemMessage(Component.literal("§cYou already have "
                    + MAX_PER_PLAYER + " minions placed."));
            return false;
        }
        long cost = placementCost(c);
        if (!CoinManager.removeCoins(player, cost)) {
            player.sendSystemMessage(Component.literal("§cThat costs " + GuiKit.num(cost)
                    + " coins and you do not have it."));
            return false;
        }

        BlockPos at = player.blockPosition();
        Minion m = new Minion();
        m.ownerUuid = player.getUUID().toString();
        m.ownerName = player.getName().getString();
        m.product = c.key();
        m.dim = player.level().dimension().identifier().toString();
        m.x = at.getX();
        m.y = at.getY();
        m.z = at.getZ();
        m.lastTick = 0;
        MINIONS.add(m);

        player.sendSystemMessage(Component.literal("§aPlaced a §f" + c.display
                + " §aminion here. It works while this chunk is loaded."));
        if (player.level() instanceof ServerLevel level) {
            Vfx.bloom(level, player.position().add(0, 1, 0));
        }
        save();
        return true;
    }

    /** Picks a minion up, returning whatever it was holding and half of what it cost. */
    public static boolean pickUp(ServerPlayer player, String minionId) {
        for (Minion m : new ArrayList<>(MINIONS)) {
            if (!m.id.equals(minionId)) continue;
            if (!m.ownerUuid.equals(player.getUUID().toString())) return false;

            CollectionType c = m.type();
            if (c != null && m.stored > 0) giveTo(player, c, m.stored);
            if (c != null) {
                long refund = placementCost(c) / 2;
                CoinManager.giveCoins(player, refund);
                player.sendSystemMessage(Component.literal("§ePicked the minion up. §7Refunded §6"
                        + GuiKit.num(refund) + " coins§7."));
            }
            MINIONS.remove(m);
            save();
            return true;
        }
        return false;
    }

    public static boolean upgrade(ServerPlayer player, String minionId) {
        for (Minion m : MINIONS) {
            if (!m.id.equals(minionId)) continue;
            if (!m.ownerUuid.equals(player.getUUID().toString())) return false;
            if (m.tier >= MAX_TIER) {
                player.sendSystemMessage(Component.literal("§eThat minion is already at the top."));
                return false;
            }
            CollectionType c = m.type();
            if (c == null) return false;

            long cost = upgradeCost(c, m.tier);
            if (!CoinManager.removeCoins(player, cost)) {
                player.sendSystemMessage(Component.literal("§cUpgrading costs "
                        + GuiKit.num(cost) + " coins."));
                return false;
            }
            m.tier++;
            player.sendSystemMessage(Component.literal("§a" + c.display + " minion is now tier §f"
                    + m.tier + "§a, one unit every §f" + intervalSeconds(m.tier) + "s§a."));
            save();
            return true;
        }
        return false;
    }

    /** Empties one minion into the owner's inventory. */
    public static int collect(ServerPlayer player, String minionId) {
        for (Minion m : MINIONS) {
            if (!m.id.equals(minionId)) continue;
            if (!m.ownerUuid.equals(player.getUUID().toString())) return 0;
            CollectionType c = m.type();
            if (c == null || m.stored <= 0) return 0;
            int got = m.stored;
            m.stored = 0;
            giveTo(player, c, got);
            player.sendSystemMessage(Component.literal("§aCollected §f" + got + " " + c.display));
            save();
            return got;
        }
        return 0;
    }

    /** Empties every minion the player owns, which is what anybody actually does. */
    public static int collectAll(ServerPlayer player) {
        int total = 0;
        for (Minion m : minionsOf(player)) {
            CollectionType c = m.type();
            if (c == null || m.stored <= 0) continue;
            total += m.stored;
            giveTo(player, c, m.stored);
            m.stored = 0;
        }
        if (total > 0) {
            player.sendSystemMessage(Component.literal("§aCollected §f" + total
                    + " §aitems from your minions."));
            save();
        } else {
            player.sendSystemMessage(Component.literal("§7Nothing to collect yet."));
        }
        return total;
    }

    // ── Running ───────────────────────────────────────────────────────────

    /** Ticked by the server. Each minion produces on its own interval. */
    public static void tick(MinecraftServer server) {
        if (server.getTickCount() % 20 != 0) return;
        long now = server.getTickCount();
        boolean dirty = false;

        for (Minion m : new ArrayList<>(MINIONS)) {
            try {
            CollectionType c = m.type();
            if (c == null) continue;
            if (m.stored >= capacity(m.tier)) continue;

            long due = m.lastTick + intervalSeconds(m.tier) * 20L;
            if (now < due) continue;

            ServerLevel level = levelOf(server, m);
            // A minion is not an offline printer. No loaded chunk, no work.
            if (level == null || !level.isLoaded(new BlockPos(m.x, m.y, m.z))) {
                m.lastTick = now;
                continue;
            }

            m.stored = Math.min(capacity(m.tier), m.stored + 1);
            m.lastTick = now;
            dirty = true;

            if (now % 100 == 0) {
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        m.x + 0.5, m.y + 1.2, m.z + 0.5, 3, 0.3, 0.3, 0.3, 0.0);
            }

            // A fraction of the output counts toward the owner's collection, so minions help
            // without ever being the fast way to a tier.
            ServerPlayer owner = ownerOf(server, m);
            if (owner != null && Math.random() < COLLECTION_RATE) {
                CollectionManager.record(owner, c, 1);
            }
            } catch (Exception e) {
                // One broken minion is not worth stopping the other eleven.
            }
        }

        if (dirty && now % 1200 == 0) save();
    }

    private static ServerPlayer ownerOf(MinecraftServer server, Minion m) {
        try {
            if (m.ownerUuid == null || m.ownerUuid.isEmpty()) return null;
            return server.getPlayerList().getPlayer(UUID.fromString(m.ownerUuid));
        } catch (Exception e) {
            // A corrupt id in the save file must not take down the tick loop.
            return null;
        }
    }

    private static ServerLevel levelOf(MinecraftServer server, Minion m) {
        for (ServerLevel level : server.getAllLevels()) {
            if (level.dimension().identifier().toString().equals(m.dim)) return level;
        }
        return null;
    }

    private static void giveTo(ServerPlayer player, CollectionType c, int count) {
        int left = count;
        while (left > 0) {
            int stack = Math.min(left, c.material.getDefaultMaxStackSize());
            ItemStack give = new ItemStack(c.material, stack);
            if (!player.getInventory().add(give)) player.drop(give, false);
            left -= stack;
        }
    }

    /** A short description of what a minion is doing, for the menu. */
    public static String statusOf(Minion m) {
        CollectionType c = m.type();
        if (c == null) return "broken";
        if (m.stored >= capacity(m.tier)) return "§cfull, collect it";
        return "§aworking, one every " + intervalSeconds(m.tier) + "s";
    }

    public static ChatFormatting tierColour(int tier) {
        if (tier >= 10) return ChatFormatting.LIGHT_PURPLE;
        if (tier >= 7) return ChatFormatting.GOLD;
        if (tier >= 4) return ChatFormatting.AQUA;
        return ChatFormatting.GREEN;
    }
}
