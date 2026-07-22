package com.political;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;

/**
 * One player's record against one {@link MobFamily}.
 *
 * <p>Gson-friendly: public fields, no-arg constructor, no behaviour that depends on
 * construction order.</p>
 */
public class BestiaryEntry {

    // ── The families ────────────────────────────────────────────────────────

    /**
     * Mobs are grouped into ten families. Milestones and damage bonuses are per <b>family</b>,
     * not per mob type, so a player who has genuinely fought their way through zombies also
     * gets credit against skeletons — but a player who has only ground one mob type does not
     * get a blanket bonus against everything.
     */
    public enum MobFamily {
        UNDEAD("Undead", "☠", ChatFormatting.DARK_GREEN, "§2", Items.ROTTEN_FLESH,
                "The walking dead and the bones they leave behind."),
        ARTHROPOD("Arthropod", "※", ChatFormatting.DARK_PURPLE, "§5", Items.SPIDER_EYE,
                "Many-legged things that nest in the dark."),
        NETHERBORN("Netherborn", "✹", ChatFormatting.RED, "§c", Items.BLAZE_POWDER,
                "Creatures born of ash, fire and the red waste."),
        ENDERBORN("Enderborn", "✧", ChatFormatting.LIGHT_PURPLE, "§d", Items.ENDER_PEARL,
                "Things that step sideways out of the world."),
        ILLAGER("Illager", "⚑", ChatFormatting.GRAY, "§7", Items.IRON_AXE,
                "Banished kin who returned with banners and grudges."),
        AQUATIC("Aquatic", "≈", ChatFormatting.AQUA, "§b", Items.PRISMARINE_SHARD,
                "Guardians of the drowned halls and the cold deep."),
        OOZE("Ooze", "◍", ChatFormatting.GREEN, "§a", Items.SLIME_BALL,
                "Things that split when struck and never quite die."),
        VOLATILE("Volatile", "✸", ChatFormatting.DARK_GREEN, "§2", Items.GUNPOWDER,
                "Quiet until they are not."),
        BEAST("Beast", "❦", ChatFormatting.GOLD, "§6", Items.LEATHER,
                "Ordinary animals — and the ones that bite back."),
        COLOSSUS("Colossus", "✦", ChatFormatting.DARK_RED, "§4", Items.NETHERITE_SCRAP,
                "The rare and the enormous. Every kill is an event.");

        public final String displayName;
        public final String symbol;
        public final ChatFormatting color;
        /** Legacy §-code equivalent of {@link #color}, for lore strings. */
        public final String legacy;
        public final Item icon;
        public final String description;

        MobFamily(String displayName, String symbol, ChatFormatting color, String legacy,
                  Item icon, String description) {
            this.displayName = displayName;
            this.symbol = symbol;
            this.color = color;
            this.legacy = legacy;
            this.icon = icon;
            this.description = description;
        }

        public String label() {
            return legacy + symbol + " " + displayName;
        }

        public static MobFamily byName(String name) {
            if (name == null) return null;
            for (MobFamily f : values()) {
                if (f.name().equalsIgnoreCase(name) || f.displayName.equalsIgnoreCase(name)) return f;
            }
            return null;
        }
    }

    // ── Persisted state ─────────────────────────────────────────────────────

    /** {@link MobFamily#name()}. Stored as a string so unknown families survive a downgrade. */
    public String family = MobFamily.UNDEAD.name();

    /** Total qualifying kills in this family. */
    public int kills = 0;

    /** Per-entity-type breakdown, keyed by the registry path (e.g. {@code "zombie"}). */
    public Map<String, Integer> byType = new HashMap<>();

    /** Highest milestone index reached (0 = none, 4 = all four). */
    public int milestone = 0;

    /** Epoch millis of the first and most recent qualifying kill — pure flavour for the GUI. */
    public long firstKillAt = 0L;
    public long lastKillAt = 0L;

    public BestiaryEntry() {}

    public BestiaryEntry(MobFamily family) {
        this.family = family.name();
    }

    public MobFamily familyEnum() {
        MobFamily f = MobFamily.byName(family);
        return f == null ? MobFamily.UNDEAD : f;
    }

    /** Number of distinct entity types recorded in this family. */
    public int distinctTypes() {
        return byType == null ? 0 : byType.size();
    }

    public int killsOfType(String typePath) {
        if (byType == null || typePath == null) return 0;
        return byType.getOrDefault(typePath, 0);
    }
}
