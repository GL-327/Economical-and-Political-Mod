package com.political;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A settlement centered on a placed Cornerstone. Gson-friendly (public fields, no-arg ctor).
 * Positions are stored as primitive ints so Gson round-trips cleanly without BlockPos codecs.
 */
public class Settlement {

    public String id;                       // stable unique id (uuid string)
    public String name = "Unnamed Settlement";
    public String ownerUuid;                // founder
    public String worldDim = "minecraft:overworld"; // ResourceKey<Level> location string

    public int cornerstoneX;
    public int cornerstoneY;
    public int cornerstoneZ;

    public String tier = SettlementTier.OUTPOST.name();
    public int population = 0;              // citizen NPCs currently tracked
    public int beds = 0;
    public int appeal = 0;
    public int jobSites = 0;
    public int wonders = 0;                 // full beacon pyramids etc.
    public long treasury = 0L;             // settlement coin treasury

    // ── Fallout-style economy (recomputed each SettlementEconomy cycle) ──
    public int food = 0;                   // food produced per cycle
    public int goods = 0;                  // trade goods produced per cycle
    public int defense = 0;                // guard strength (raid protection)
    public int happiness = 50;             // 0..100 — gates growth, multiplies income
    public long lastIncome = 0L;           // coins earned last cycle (after tax)
    public long lastTaxPaid = 0L;          // remitted to the national government last cycle
    public int rawBeds = 0;                // every bed seen (vs. `beds` = sheltered only)
    public int footprintCells = 0;         // developed 16x16 cells
    public java.util.Map<String, Integer> appealByCategory = new HashMap<>();

    public Set<String> registeredPlayers = new HashSet<>();
    public List<String> citizenUuids = new ArrayList<>();
    public String nearestMetropolisId = null;

    /** role name -> uuid (player or NPC). e.g. "constituent", "chair", "vice", "sheriff". */
    public Map<String, String> leaders = new HashMap<>();

    public long foundedAt = 0L;
    public long lastScanAt = 0L;

    public Settlement() {}

    public Settlement(String id, String name, String ownerUuid, String worldDim,
                      int x, int y, int z) {
        this.id = id;
        this.name = name;
        this.ownerUuid = ownerUuid;
        this.worldDim = worldDim;
        this.cornerstoneX = x;
        this.cornerstoneY = y;
        this.cornerstoneZ = z;
        this.foundedAt = System.currentTimeMillis();
    }

    // ── helpers (defensive against null collections after Gson load) ──

    public Set<String> registered() {
        if (registeredPlayers == null) registeredPlayers = new HashSet<>();
        return registeredPlayers;
    }

    public List<String> citizens() {
        if (citizenUuids == null) citizenUuids = new ArrayList<>();
        return citizenUuids;
    }

    public Map<String, String> leaderMap() {
        if (leaders == null) leaders = new HashMap<>();
        return leaders;
    }

    public SettlementTier tierEnum() {
        try {
            return SettlementTier.valueOf(tier);
        } catch (Exception e) {
            return SettlementTier.OUTPOST;
        }
    }

    public void setTier(SettlementTier t) {
        this.tier = t.name();
    }

    public net.minecraft.core.BlockPos cornerstonePos() {
        return new net.minecraft.core.BlockPos(cornerstoneX, cornerstoneY, cornerstoneZ);
    }

    public double distanceSqTo(Settlement other) {
        double dx = this.cornerstoneX - other.cornerstoneX;
        double dy = this.cornerstoneY - other.cornerstoneY;
        double dz = this.cornerstoneZ - other.cornerstoneZ;
        return dx * dx + dy * dy + dz * dz;
    }
}
