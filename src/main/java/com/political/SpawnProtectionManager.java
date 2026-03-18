package com.political;

import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages a server-wide Government Protection zone.
 *
 * <p>When active, non-operator players cannot:
 * <ul>
 *   <li>Break blocks inside the region</li>
 *   <li>Place blocks inside the region</li>
 *   <li>Deal or receive any damage while inside the region</li>
 *   <li>Attack entities while inside the region</li>
 * </ul>
 *
 * <p>Region data is persisted via {@link DataManager.SaveData}.
 */
public class SpawnProtectionManager {

    private static boolean active = false;
    private static int minX, maxX;
    private static int minY, maxY;
    private static int minZ, maxZ;

    // Per-player tracking for boundary enter/leave warnings
    private static final Map<UUID, Boolean> playerWasInZone = new HashMap<>();

    // ── Operator helper ───────────────────────────────────────────────────────

    /**
     * Returns {@code true} if the player has operator permission (level 4, same as
     * {@link net.minecraft.server.command.CommandManager#OWNERS_CHECK}) and should
     * be allowed to bypass spawn protection.
     */
    public static boolean isOperator(ServerPlayerEntity player) {
        return CommandManager.requirePermissionLevel(CommandManager.OWNERS_CHECK)
                .test(player.getCommandSource());
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    /** Returns whether spawn protection is currently active. */
    public static boolean isActive() {
        return active;
    }

    /**
     * Defines the protected region and activates it.
     */
    public static void setRegion(int x1, int y1, int z1, int x2, int y2, int z2) {
        minX = Math.min(x1, x2);
        maxX = Math.max(x1, x2);
        minY = Math.min(y1, y2);
        maxY = Math.max(y1, y2);
        minZ = Math.min(z1, z2);
        maxZ = Math.max(z1, z2);
        active = true;
        persistToDataManager();
    }

    /** Deactivates spawn protection. */
    public static void clearRegion() {
        active = false;
        persistToDataManager();
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    /**
     * Returns {@code true} if the given block position lies within the protected region.
     */
    public static boolean isBlockProtected(int x, int y, int z) {
        if (!active) return false;
        return x >= minX && x <= maxX
                && y >= minY && y <= maxY
                && z >= minZ && z <= maxZ;
    }

    /**
     * Returns {@code true} if the given world position lies within the protected region.
     */
    public static boolean isPosProtected(double x, double y, double z) {
        return isBlockProtected((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
    }

    // ── Boundary warning tick ─────────────────────────────────────────────────

    /**
     * Called every tick (or every few ticks) per player to detect zone transitions
     * and send actionbar boundary warnings.
     */
    public static void tickPlayerBoundary(ServerPlayerEntity player) {
        if (!active) return;
        UUID uuid = player.getUuid();
        boolean nowInZone = isPosProtected(player.getX(), player.getY(), player.getZ());
        Boolean wasInZone = playerWasInZone.get(uuid);

        if (wasInZone == null) {
            // First tick for this player — record state without sending message
            playerWasInZone.put(uuid, nowInZone);
            return;
        }

        if (!wasInZone && nowInZone) {
            // Entered the zone
            player.sendMessage(
                    Text.literal("⚠ Entering Government Protected Land").formatted(Formatting.GOLD),
                    true);
        } else if (wasInZone && !nowInZone) {
            // Left the zone
            player.sendMessage(
                    Text.literal("⚠ Leaving Government Protected Land").formatted(Formatting.GRAY),
                    true);
        }

        playerWasInZone.put(uuid, nowInZone);
    }

    /** Called when a player disconnects to clean up tracking state. */
    public static void onPlayerDisconnect(UUID uuid) {
        playerWasInZone.remove(uuid);
    }

    // ── Getters (for display / save-load) ────────────────────────────────────

    public static int getMinX() { return minX; }
    public static int getMaxX() { return maxX; }
    public static int getMinY() { return minY; }
    public static int getMaxY() { return maxY; }
    public static int getMinZ() { return minZ; }
    public static int getMaxZ() { return maxZ; }

    // ── Load from DataManager ─────────────────────────────────────────────────

    /** Called by {@link DataManager#load} after reading the save file. */
    public static void loadFromData(DataManager.SaveData data) {
        active = data.spawnProtectionActive;
        minX   = data.spawnProtectionMinX;
        maxX   = data.spawnProtectionMaxX;
        minY   = data.spawnProtectionMinY;
        maxY   = data.spawnProtectionMaxY;
        minZ   = data.spawnProtectionMinZ;
        maxZ   = data.spawnProtectionMaxZ;
    }

    /** Writes current state into the provided {@link DataManager.SaveData} object. */
    public static void saveToData(DataManager.SaveData data) {
        data.spawnProtectionActive = active;
        data.spawnProtectionMinX   = minX;
        data.spawnProtectionMaxX   = maxX;
        data.spawnProtectionMinY   = minY;
        data.spawnProtectionMaxY   = maxY;
        data.spawnProtectionMinZ   = minZ;
        data.spawnProtectionMaxZ   = maxZ;
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /** Immediately persists the current state to DataManager's in-memory data. */
    private static void persistToDataManager() {
        if (PoliticalServer.server != null) {
            saveToData(DataManager.getData());
            DataManager.save(PoliticalServer.server);
        }
    }
}
