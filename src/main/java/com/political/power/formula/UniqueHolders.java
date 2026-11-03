package com.political.power.formula;

import com.political.power.PowerSettings;
import java.util.Map;

/**
 * The one-holder rule for unique techniques (C1): a technique that is switched to unique in the settings can
 * belong to one player at a time. Server-enforced, and kept in the saved data so a restart cannot hand it to a
 * second player.
 */
public final class UniqueHolders {

    /** What happened when someone tried to take a technique. */
    public enum Claim { NOT_UNIQUE, CLAIMED, ALREADY_YOURS, TAKEN }

    private UniqueHolders() {
    }

    /** Whether the settings make this technique unique. Older techniques are never unique. */
    public static boolean isUnique(String techniqueId) {
        if (techniqueId == null) {
            return false;
        }
        return switch (techniqueId) {
            case "BLUE" -> PowerSettings.UNIQUE_BLUE.get();
            case "RED" -> PowerSettings.UNIQUE_RED.get();
            case "CURSED_BLAST" -> PowerSettings.UNIQUE_CURSED_BLAST.get();
            case "REVERSE" -> PowerSettings.UNIQUE_REVERSE.get();
            case "BARRIER" -> PowerSettings.UNIQUE_BARRIER.get();
            case "DOMAIN" -> PowerSettings.UNIQUE_DOMAIN.get();
            default -> false;
        };
    }

    /** Tries to give a unique technique to a player; {@code holders} maps technique id to holder UUID. */
    public static Claim claim(Map<String, String> holders, String techniqueId, String uuid) {
        if (!isUnique(techniqueId)) {
            return Claim.NOT_UNIQUE;
        }
        String holder = holders.get(techniqueId);
        if (holder == null) {
            holders.put(techniqueId, uuid);
            return Claim.CLAIMED;
        }
        return holder.equals(uuid) ? Claim.ALREADY_YOURS : Claim.TAKEN;
    }

    /** Frees a unique technique if this player holds it. Returns true if it was released. */
    public static boolean release(Map<String, String> holders, String techniqueId, String uuid) {
        return holders.remove(techniqueId, uuid);
    }
}
