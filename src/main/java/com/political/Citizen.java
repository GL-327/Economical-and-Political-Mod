package com.political;

import net.minecraft.ChatFormatting;

/**
 * A tracked citizen NPC (villager). Gson-friendly. Personality is derived deterministically
 * from the villager UUID so it is stable across restarts even before the JSON is re-read.
 */
public class Citizen {

    public String uuid;             // villager entity UUID string
    public String name;
    public String settlementId;
    public String job = CitizenJob.CITIZEN.name();

    // Personality (all normalized). Deterministic from UUID via fromUuid().
    public double partyLean;        // -1 (party A / progressive) .. +1 (party B / traditional)
    public double taxTolerance;     // 0 (hates tax) .. 1 (accepts tax)
    public double appealSensitivity;// 0 (indifferent) .. 1 (demands beauty)
    public double charisma;         // 0 .. 1 (electability as a candidate)

    public Citizen() {}

    public enum CitizenJob {
        CITIZEN("Citizen", ChatFormatting.GRAY, 0),
        COURIER("Courier", ChatFormatting.AQUA, 1),
        MERCHANT("Merchant", ChatFormatting.GREEN, 1),
        PRIEST("Priest", ChatFormatting.WHITE, 1),
        SCHOLAR("Scholar", ChatFormatting.BLUE, 1),
        BLACKSMITH("Blacksmith", ChatFormatting.DARK_GRAY, 1),
        INNKEEPER("Innkeeper", ChatFormatting.YELLOW, 1),
        TAX_COLLECTOR("Tax Collector", ChatFormatting.GOLD, 2),
        DEPUTY("Deputy", ChatFormatting.DARK_AQUA, 3),
        SHERIFF("Sheriff", ChatFormatting.RED, 4);

        public final String displayName;
        public final ChatFormatting color;
        public final int rank; // higher outranks lower; SHERIFF > DEPUTY > rest

        CitizenJob(String displayName, ChatFormatting color, int rank) {
            this.displayName = displayName;
            this.color = color;
            this.rank = rank;
        }
    }

    public CitizenJob jobEnum() {
        try {
            return CitizenJob.valueOf(job);
        } catch (Exception e) {
            return CitizenJob.CITIZEN;
        }
    }

    public void setJob(CitizenJob j) { this.job = j.name(); }

    /** Deterministic personality + generated name from the villager UUID. */
    public static Citizen fromUuid(java.util.UUID id, String settlementId) {
        Citizen c = new Citizen();
        c.uuid = id.toString();
        c.settlementId = settlementId;

        long hi = id.getMostSignificantBits();
        long lo = id.getLeastSignificantBits();
        // Use a splittable-style mix so each trait draws from a different portion of the bits.
        c.partyLean = norm(mix(hi ^ 0x9E3779B97F4A7C15L)) * 2.0 - 1.0;
        c.taxTolerance = norm(mix(lo ^ 0xC2B2AE3D27D4EB4FL));
        c.appealSensitivity = norm(mix(hi + 0x165667B19E3779F9L));
        c.charisma = norm(mix(lo + 0xD6E8FEB86659FD93L));

        c.name = generateName(id);
        c.job = CitizenJob.CITIZEN.name();
        return c;
    }

    private static long mix(long z) {
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }

    /** Map a mixed long to [0,1). */
    private static double norm(long v) {
        return (v >>> 11) * (1.0 / (1L << 53));
    }

    private static final String[] FIRST = {
            "Alden", "Bryn", "Cora", "Doran", "Elsa", "Finn", "Greta", "Hale", "Ivo", "Juna",
            "Kael", "Lyra", "Milo", "Nara", "Oren", "Petra", "Quill", "Rosa", "Silas", "Tova",
            "Ulric", "Vesna", "Wren", "Xander", "Yara", "Zeke"
    };
    private static final String[] LAST = {
            "Ashford", "Blackwood", "Crane", "Dunmore", "Everly", "Frost", "Gale", "Holt",
            "Ironwood", "Jarrow", "Kingsley", "Lowe", "Marsh", "Nightingale", "Orwell",
            "Payne", "Quarry", "Rook", "Stone", "Thorne", "Vance", "Whitlock"
    };

    public static String generateName(java.util.UUID id) {
        int a = (int) (Math.abs(id.getMostSignificantBits() >>> 8) % FIRST.length);
        int b = (int) (Math.abs(id.getLeastSignificantBits() >>> 8) % LAST.length);
        return FIRST[a] + " " + LAST[b];
    }
}
