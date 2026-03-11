package com.political;

import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Manages a server-wide spawn-protection zone.
 *
 * <p>When active, non-operator players cannot:
 * <ul>
 *   <li>Break blocks inside the region</li>
 *   <li>Place blocks inside the region</li>
 *   <li>Deal or receive PvP damage while inside the region</li>
 * </ul>
 *
 * <p>Region data is persisted via {@link DataManager.SaveData}.
 */
public class SpawnProtectionManager {

    private static boolean active = false;
    private static int minX, maxX;
    private static int minY, maxY;
    private static int minZ, maxZ;

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
     *
     * @param x1   X-coordinate of the first corner
     * @param y1   Y-coordinate of the first corner (inclusive)
     * @param z1   Z-coordinate of the first corner
     * @param x2   X-coordinate of the second corner
     * @param y2   Y-coordinate of the second corner (inclusive)
     * @param z2   Z-coordinate of the second corner
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
     * Always returns {@code false} when protection is inactive.
     */
    public static boolean isBlockProtected(int x, int y, int z) {
        if (!active) return false;
        return x >= minX && x <= maxX
                && y >= minY && y <= maxY
                && z >= minZ && z <= maxZ;
    }

    /**
     * Returns {@code true} if the given world position (entity coords) lies within the protected
     * region. Uses floor-math to map to the containing block. Always returns {@code false} when
     * protection is inactive.
     */
    public static boolean isPosProtected(double x, double y, double z) {
        return isBlockProtected((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
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
