package com.political;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * V5 Pillar 1 — data model for one instanced Expedition run.
 *
 * <p>Gson-friendly: public fields, no-arg constructor, primitives / Strings only
 * (no BlockPos, no enums stored directly — enum names are stored as Strings so a
 * renamed constant degrades gracefully instead of throwing on load).
 *
 * <p>Owned and mutated exclusively by {@link ExpeditionRunManager}. The legacy
 * orphaned {@code ExpeditionManager} is untouched and unrelated to this class.
 */
public class ExpeditionDungeon {

    // ── identity ──
    public String id;                          // uuid string, also used as the entity-tag salt
    public String leaderUuid;
    public List<String> partyUuids = new ArrayList<>();

    // ── difficulty ──
    public String tier = "DELVE";              // ExpeditionRunManager.Tier name
    public int floor = 1;                      // 1-based current floor
    public int totalFloors = 1;

    // ── instance placement (End dimension "expedition lane") ──
    public int slotIndex = -1;                 // reserved instance slot; -1 = none
    public int originX;
    public int originY;
    public int originZ;
    public String worldDim = "minecraft:the_end";

    // ── layout of the CURRENT floor ──
    public List<Room> rooms = new ArrayList<>();
    public long seed;

    // ── progress ──
    public int roomsCleared;
    public int roomsTotalThisFloor;
    public int roomsClearedLifetime;           // across every floor of this run
    public boolean bossSpawned;
    public boolean bossDefeated;
    public boolean lootRolled;                 // completion loot is granted exactly once
    public boolean sigilConsumed;              // bonus loot roll purchased at launch
    public boolean keyConsumed;                // cooldown bypass purchased at launch

    // ── lifecycle ──
    public String state = "ACTIVE";            // ACTIVE | FLOOR_CLEARED | COMPLETE | FAILED | CLEANUP
    public long startedAt;
    public long floorStartedAt;
    public long deadlineAt;                    // hard wall-clock time limit; past this the run fails
    public long floorAdvanceAt;                // when state==FLOOR_CLEARED, tick over at this time

    // ── tie-ins ──
    public String settlementId;                // settlement the run launched from (treasury payout)

    // ── return positions, so we can always put people back ──
    public List<ReturnPoint> returns = new ArrayList<>();

    public ExpeditionDungeon() {}

    // ────────────────────────────────────────────────────────────
    // Nested data
    // ────────────────────────────────────────────────────────────

    /** One room on the current floor. gx/gz are grid coordinates, not world coordinates. */
    public static class Room {
        public int gx;
        public int gz;
        public String type = "COMBAT";   // ENTRY | COMBAT | PUZZLE | VAULT | BOSS
        public boolean activated;        // mobs/puzzle have been armed (player walked in)
        public boolean cleared;
        public int mobsSpawned;

        public Room() {}

        public Room(int gx, int gz, String type) {
            this.gx = gx;
            this.gz = gz;
            this.type = type;
        }
    }

    /** Where a member stood before entering, so abandon/fail/complete can restore them. */
    public static class ReturnPoint {
        public String uuid;
        public String dim = "minecraft:overworld";
        public double x;
        public double y;
        public double z;
        public float yaw;
        public float pitch;

        public ReturnPoint() {}

        public ReturnPoint(String uuid, String dim, double x, double y, double z, float yaw, float pitch) {
            this.uuid = uuid;
            this.dim = dim;
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
            this.pitch = pitch;
        }
    }

    // ────────────────────────────────────────────────────────────
    // Null-safe helpers (Gson can leave collections null)
    // ────────────────────────────────────────────────────────────

    public List<String> party() {
        if (partyUuids == null) partyUuids = new ArrayList<>();
        return partyUuids;
    }

    public List<Room> roomList() {
        if (rooms == null) rooms = new ArrayList<>();
        return rooms;
    }

    public List<ReturnPoint> returnList() {
        if (returns == null) returns = new ArrayList<>();
        return returns;
    }

    public Set<String> partySet() {
        return new HashSet<>(party());
    }

    public Room roomAt(int gx, int gz) {
        for (Room r : roomList()) {
            if (r.gx == gx && r.gz == gz) return r;
        }
        return null;
    }

    public Room entryRoom() {
        for (Room r : roomList()) {
            if ("ENTRY".equals(r.type)) return r;
        }
        return roomList().isEmpty() ? null : roomList().get(0);
    }

    public Room bossRoom() {
        for (Room r : roomList()) {
            if ("BOSS".equals(r.type)) return r;
        }
        return null;
    }

    /** True once every non-boss room on this floor is cleared. */
    public boolean nonBossRoomsCleared() {
        for (Room r : roomList()) {
            if ("BOSS".equals(r.type)) continue;
            if (!r.cleared) return false;
        }
        return true;
    }

    public boolean isFinalFloor() {
        return floor >= totalFloors;
    }

    public boolean isActive() {
        return "ACTIVE".equals(state) || "FLOOR_CLEARED".equals(state);
    }

    public ReturnPoint returnFor(String uuid) {
        for (ReturnPoint rp : returnList()) {
            if (uuid.equals(rp.uuid)) return rp;
        }
        return null;
    }

    /** Entity tag applied to every mob this run spawns, so cleanup can find them all. */
    public String entityTag() {
        return "political_exp_" + (id == null ? "unknown" : id.replace("-", ""));
    }

    /** Per-room entity tag, so per-room clear checks are cheap and exact. */
    public String roomTag(Room r) {
        return entityTag() + "_r" + r.gx + "_" + r.gz;
    }
}
