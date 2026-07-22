package com.political;

import net.minecraft.server.MinecraftServer;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Admin-configurable toggles for Civil Anti-Cheat (see /CivilAC).
 */
public final class CivilAntiCheatManager {
    private static final AtomicBoolean DUPE_SCAN = new AtomicBoolean(true);
    private static final AtomicBoolean CLAMP_ILLEGAL = new AtomicBoolean(true);
    private static final AtomicBoolean NOTIFY_OPS = new AtomicBoolean(true);
    private static final AtomicBoolean BLOCK_INSTABREAK = new AtomicBoolean(false);

    public static boolean isDupeScanEnabled() {
        return DUPE_SCAN.get();
    }

    public static void setDupeScanEnabled(boolean v) {
        DUPE_SCAN.set(v);
    }

    public static boolean isClampIllegalStacksEnabled() {
        return CLAMP_ILLEGAL.get();
    }

    public static void setClampIllegalStacksEnabled(boolean v) {
        CLAMP_ILLEGAL.set(v);
    }

    public static boolean isNotifyOperatorsEnabled() {
        return NOTIFY_OPS.get();
    }

    public static void setNotifyOperatorsEnabled(boolean v) {
        NOTIFY_OPS.set(v);
    }

    public static boolean isBlockInstabreakEnabled() {
        return BLOCK_INSTABREAK.get();
    }

    public static void setBlockInstabreakEnabled(boolean v) {
        BLOCK_INSTABREAK.set(v);
    }

    public static void broadcastToOps(MinecraftServer server, net.minecraft.network.chat.Component message) {
        if (server == null || !NOTIFY_OPS.get()) return;
        for (var p : server.getPlayerList().getPlayers()) {
            if (p != null && SpawnProtectionManager.isOperator(p)) {
                p.sendSystemMessage(message, false);
            }
        }
    }
}
