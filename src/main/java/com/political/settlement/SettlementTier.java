package com.political.settlement;

import net.minecraft.ChatFormatting;

/**
 * Settlement tiers. Ordinal is the rank (OUTPOST lowest .. ELYSIUM highest).
 * Thresholds are the MINIMUMS required to reach that tier (all must be met).
 * The CITY+ "1 registered player" gate is enforced in SettlementManager (not here),
 * because classify() only sees the physical/economic metrics.
 */
public enum SettlementTier {
    OUTPOST("Outpost",        ChatFormatting.GRAY,          "§7",     0,   0,    0,          0L, 0),
    HAMLET("Hamlet",          ChatFormatting.WHITE,         "§f",     3,   0,    0,          0L, 0),
    VILLAGE("Village",        ChatFormatting.GREEN,         "§a",     8,   5,    0,          0L, 0),
    TOWN("Town",              ChatFormatting.DARK_GREEN,    "§2",    20,  15,   50,          0L, 0),
    CITY("City",              ChatFormatting.YELLOW,        "§e",    40,  30,  150,          0L, 0),
    METROPOLIS("Metropolis",  ChatFormatting.GOLD,          "§6",    80,  60,  400,     100_000L, 0),
    MEGALOPOLIS("Megalopolis",ChatFormatting.RED,           "§c",   150, 120, 1000,   1_000_000L, 0),
    UTOPIA("Utopia",          ChatFormatting.LIGHT_PURPLE,  "§d",   250, 200, 2500,  10_000_000L, 1),
    ELYSIUM("Elysium",        ChatFormatting.AQUA,          "§b§l", 400, 400, 6000, 100_000_000L, 3);

    public final String displayName;
    public final ChatFormatting color;
    public final String colorCode;
    public final int minBeds;
    public final int minPop;
    public final int minAppeal;
    public final long minTreasury;
    public final int minWonders;

    SettlementTier(String displayName, ChatFormatting color, String colorCode,
                   int minBeds, int minPop, int minAppeal, long minTreasury, int minWonders) {
        this.displayName = displayName;
        this.color = color;
        this.colorCode = colorCode;
        this.minBeds = minBeds;
        this.minPop = minPop;
        this.minAppeal = minAppeal;
        this.minTreasury = minTreasury;
        this.minWonders = minWonders;
    }

    public int rank() { return ordinal(); }

    public boolean isAtLeast(SettlementTier other) { return ordinal() >= other.ordinal(); }

    public SettlementTier next() {
        SettlementTier[] v = values();
        return ordinal() < v.length - 1 ? v[ordinal() + 1] : this;
    }

    public SettlementTier prev() {
        return ordinal() > 0 ? values()[ordinal() - 1] : this;
    }

    /** Colored display name for chat/GUI. */
    public String colored() { return colorCode + displayName; }

    /**
     * Returns the highest tier whose ALL thresholds are satisfied by the given metrics.
     * Never returns null (OUTPOST is the floor).
     */
    public static SettlementTier classify(int beds, int pop, int appeal, long treasury, int wonders) {
        SettlementTier result = OUTPOST;
        for (SettlementTier t : values()) {
            if (beds >= t.minBeds && pop >= t.minPop && appeal >= t.minAppeal
                    && treasury >= t.minTreasury && wonders >= t.minWonders) {
                result = t;
            }
        }
        return result;
    }
}
