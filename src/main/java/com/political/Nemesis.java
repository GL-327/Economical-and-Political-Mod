package com.political;

import net.minecraft.ChatFormatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * One mob that killed a player and came back for it.
 *
 * <p>A nemesis is remembered rather than spawned from a table. It keeps the type of mob that landed
 * the kill, a generated name, the traits it rolled, how many times it has killed that specific player
 * and how many times it has been put down. Everything about it is personal to one player, which is
 * the whole reason the system exists on a random seed world: the interesting content is not a place
 * the server built, it is a history the player wrote by losing.</p>
 */
public final class Nemesis {

    /** How far a nemesis can climb. Each rank adds traits and multiplies its numbers. */
    public enum Rank {
        STALKER("Stalker", ChatFormatting.GRAY, 2, 1.6, 1.3),
        HUNTER("Hunter", ChatFormatting.GREEN, 3, 2.4, 1.6),
        SLAYER("Slayer", ChatFormatting.AQUA, 4, 3.4, 2.0),
        WARLORD("Warlord", ChatFormatting.LIGHT_PURPLE, 5, 5.0, 2.6),
        NIGHTMARE("Nightmare", ChatFormatting.GOLD, 6, 7.5, 3.4);

        public final String label;
        public final ChatFormatting colour;
        /** How many traits a nemesis of this rank carries. */
        public final int traitCount;
        public final double healthMultiplier;
        public final double damageMultiplier;

        Rank(String label, ChatFormatting colour, int traitCount,
             double healthMultiplier, double damageMultiplier) {
            this.label = label;
            this.colour = colour;
            this.traitCount = traitCount;
            this.healthMultiplier = healthMultiplier;
            this.damageMultiplier = damageMultiplier;
        }

        public Rank next() {
            int i = ordinal();
            return i + 1 < values().length ? values()[i + 1] : this;
        }

        public boolean isTop() {
            return this == NIGHTMARE;
        }
    }

    // ── Name generation ───────────────────────────────────────────────────

    private static final String[] FIRST = {
            "Grask", "Volm", "Ashen", "Kerrin", "Uld", "Threx", "Morrow", "Sable", "Vurn", "Hakk",
            "Dreg", "Isolde", "Karn", "Rook", "Vasp", "Ombre", "Skell", "Torvin", "Marrow", "Gale",
            "Nix", "Bram", "Cinder", "Quell", "Yarrow", "Fenn", "Orrin", "Wither", "Talla", "Skarn"
    };

    private static final String[] TITLE = {
            "the Patient", "the Unfinished", "who Waits", "the Second Time", "of the Long Grudge",
            "the Unburied", "who Remembers", "the Returned", "of Broken Teeth", "the Persistent",
            "who Counts", "the Uninvited", "of the Cold Hour", "the Unpaid", "who Followed",
            "the Twice Killed", "of the Quiet Road", "the Owed", "who Does Not Sleep", "the Debt"
    };

    public String id = UUID.randomUUID().toString();
    /** The player this nemesis belongs to. Nobody else can settle it. */
    public String victimUuid = "";
    public String victimName = "";
    /** Registry id of the mob type that landed the original kill. */
    public String entityType = "minecraft:zombie";
    public String name = "";
    public String title = "";
    public String rank = Rank.STALKER.name();
    public List<String> traits = new ArrayList<>();
    /** How many times it has killed its victim. */
    public int kills = 1;
    /** How many times the victim has put it down. */
    public int deaths;
    /** Times it fled a fight it was losing. Each one is a visible scar. */
    public int scars;
    /** Whether it is currently walking around somewhere. */
    public boolean active;
    /** The uuid of the live entity while it is active. */
    public String entityUuid = "";
    public long lastSeen;
    public String lastDim = "minecraft:overworld";
    public int lastX;
    public int lastY;
    public int lastZ;

    public Rank rank() {
        try {
            return Rank.valueOf(rank);
        } catch (IllegalArgumentException | NullPointerException e) {
            return Rank.STALKER;
        }
    }

    public List<NemesisTrait> traitList() {
        List<NemesisTrait> out = new ArrayList<>();
        for (String t : traits) {
            NemesisTrait tr = NemesisTrait.byKey(t);
            if (tr != null) out.add(tr);
        }
        return out;
    }

    public boolean has(NemesisTrait t) {
        return traits.contains(t.key());
    }

    /** The full name as it appears above its head and in chat. */
    public String fullName() {
        return name + " " + title;
    }

    public String colouredName() {
        return rank().colour + "" + ChatFormatting.BOLD + fullName();
    }

    /** The coins settling this nemesis is worth, which grows with everything it has done. */
    public long bounty() {
        Rank r = rank();
        long base = 2_500L * (r.ordinal() + 1);
        return base + kills * 1_500L + scars * 1_000L;
    }

    /**
     * Builds a fresh nemesis out of the mob that just landed a kill. Traits are rolled here once and
     * never rerolled, so the thing that comes back is a specific individual.
     */
    public static Nemesis create(String victimUuid, String victimName, String entityTypeId,
                                 Random rng) {
        Nemesis n = new Nemesis();
        n.victimUuid = victimUuid;
        n.victimName = victimName;
        n.entityType = entityTypeId;
        n.name = FIRST[rng.nextInt(FIRST.length)];
        n.title = TITLE[rng.nextInt(TITLE.length)];
        n.rank = Rank.STALKER.name();
        n.kills = 1;
        n.lastSeen = System.currentTimeMillis();
        n.rollTraitsUpTo(Rank.STALKER.traitCount, rng);
        return n;
    }

    /** Tops the trait list up to a count, used on creation and on every promotion. */
    public void rollTraitsUpTo(int count, Random rng) {
        List<NemesisTrait> have = traitList();
        while (have.size() < count) {
            NemesisTrait t = NemesisTrait.roll(have, rng);
            if (t == null) break;
            have.add(t);
            traits.add(t.key());
        }
    }

    /** Promotes after another kill. Returns true when the rank actually moved. */
    public boolean promote(Random rng) {
        kills++;
        Rank current = rank();
        if (current.isTop()) {
            // Already at the top, so it gets an extra trait instead of a rank it cannot have.
            rollTraitsUpTo(traitList().size() + 1, rng);
            return false;
        }
        Rank next = current.next();
        rank = next.name();
        rollTraitsUpTo(next.traitCount, rng);
        return true;
    }

    /** Called when it escapes a fight. It comes back meaner rather than weaker. */
    public void flee(Random rng) {
        scars++;
        active = false;
        entityUuid = "";
        rollTraitsUpTo(traitList().size() + 1, rng);
    }

    public double healthMultiplier() {
        // Scars make it tougher on top of its rank, which is what makes a runner worth chasing.
        return rank().healthMultiplier * (1.0 + scars * 0.15);
    }

    public double damageMultiplier() {
        return rank().damageMultiplier * (1.0 + scars * 0.10);
    }

    /** A one line summary for the grudge list. */
    public String summary() {
        StringBuilder sb = new StringBuilder();
        sb.append(rank().colour).append(rank().label);
        sb.append(" §8| §7").append(kills).append(kills == 1 ? " kill" : " kills");
        if (deaths > 0) sb.append(" §8| §7").append(deaths).append(" put down");
        if (scars > 0) sb.append(" §8| §c").append(scars).append(" escaped");
        return sb.toString();
    }
}
