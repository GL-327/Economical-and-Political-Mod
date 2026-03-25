package com.political;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks recently opened lootable containers a player opened for admin review (/view ... prevchests).
 */
public final class ChestAccessHistory {
    public static final int MAX_ENTRIES = 150;

    public static final class Entry {
        public final long tick;
        public final String worldId;
        public final int x;
        public final int y;
        public final int z;
        public final String blockId;

        public Entry(long tick, String worldId, int x, int y, int z, String blockId) {
            this.tick = tick;
            this.worldId = worldId;
            this.x = x;
            this.y = y;
            this.z = z;
            this.blockId = blockId;
        }

        public BlockPos pos() {
            return new BlockPos(x, y, z);
        }
    }

    private static final Map<UUID, Deque<Entry>> BY_PLAYER = new ConcurrentHashMap<>();

    public static void record(ServerPlayerEntity player, String worldId, BlockPos pos, String blockId) {
        if (player == null || player.getEntityWorld().getServer() == null) return;
        long tick = player.getEntityWorld().getServer().getTicks();
        Deque<Entry> q = BY_PLAYER.computeIfAbsent(player.getUuid(), u -> new ArrayDeque<>(MAX_ENTRIES + 1));
        synchronized (q) {
            q.addFirst(new Entry(tick, worldId, pos.getX(), pos.getY(), pos.getZ(), blockId));
            while (q.size() > MAX_ENTRIES) q.removeLast();
        }
    }

    public static List<Entry> copyFor(UUID playerUuid) {
        Deque<Entry> q = BY_PLAYER.get(playerUuid);
        if (q == null) return List.of();
        synchronized (q) {
            return new ArrayList<>(q);
        }
    }
}
