package com.political;

import net.minecraft.ChatFormatting;

/**
 * One posting on the requisition board.
 *
 * <p>Commissions are generated rather than written. A good, a count bracket and a framing combine
 * into an index, and that index is the commission, so the whole board is a pure function of three
 * numbers and nothing has to be stored except which indices a player is currently holding. That is
 * what makes eleven thousand of them practical.</p>
 *
 * <p>Difficulty is value times count, straight out of the goods table, and everything else follows
 * from it: the band, the coins, the experience and whether the reward is worth an armour piece. No
 * commission is balanced by hand, which is the only way a set this size stays consistent.</p>
 */
public final class Commission {

    /** The framings. Same request, different voice, so the board reads like a place. */
    public enum Framing {
        QUARTERMASTER("Quartermaster's Requisition", "The stores are short. Make them not short.",
                ChatFormatting.GOLD),
        ROADWARDEN("Roadwarden's Notice", "Posted where the roads meet. Nobody has taken it down.",
                ChatFormatting.YELLOW),
        GUILD("Guild Order", "Stamped, sealed and filed in triplicate.", ChatFormatting.AQUA),
        FRONTIER("Frontier Request", "Written in a hurry by somebody a long way out.",
                ChatFormatting.GREEN),
        CROWN("Crown Levy", "It is not phrased as a request.", ChatFormatting.LIGHT_PURPLE),
        SALVAGE("Salvage Claim", "Somebody wants what is left. They are not saying why.",
                ChatFormatting.GRAY),
        WAGER("Standing Wager", "Nailed up by somebody who does not expect to pay out.",
                ChatFormatting.RED);

        public final String label;
        public final String flavor;
        public final ChatFormatting colour;

        Framing(String label, String flavor, ChatFormatting colour) {
            this.label = label;
            this.flavor = flavor;
            this.colour = colour;
        }
    }

    /** What a commission is worth, derived from difficulty rather than chosen. */
    public enum Band {
        ERRAND("Errand", ChatFormatting.DARK_GRAY, 0),
        TASK("Task", ChatFormatting.GRAY, 400),
        JOB("Job", ChatFormatting.WHITE, 1_600),
        UNDERTAKING("Undertaking", ChatFormatting.GREEN, 6_000),
        CHARTER("Charter", ChatFormatting.AQUA, 20_000),
        GREAT_WORK("Great Work", ChatFormatting.LIGHT_PURPLE, 70_000),
        IMPOSSIBILITY("Impossibility", ChatFormatting.GOLD, 250_000);

        public final String label;
        public final ChatFormatting colour;
        /** Lowest difficulty that reaches this band. */
        public final long threshold;

        Band(String label, ChatFormatting colour, long threshold) {
            this.label = label;
            this.colour = colour;
            this.threshold = threshold;
        }

        public static Band forDifficulty(long difficulty) {
            Band best = ERRAND;
            for (Band b : values()) {
                if (difficulty >= b.threshold) best = b;
            }
            return best;
        }
    }

    public final int index;
    public final CommissionGoods.Good good;
    public final int count;
    public final Framing framing;
    public final long difficulty;
    public final Band band;

    private Commission(int index, CommissionGoods.Good good, int count, Framing framing) {
        this.index = index;
        this.good = good;
        this.count = count;
        this.framing = framing;
        this.difficulty = (long) good.value() * count;
        this.band = Band.forDifficulty(difficulty);
    }

    /** How many distinct commissions exist. */
    public static int total() {
        return CommissionGoods.size() * CommissionGoods.STEPS * Framing.values().length;
    }

    /** Rebuilds a commission from its index. The index is the only thing worth storing. */
    public static Commission byIndex(int index) {
        int frames = Framing.values().length;
        int steps = CommissionGoods.STEPS;
        int size = CommissionGoods.size();
        if (size == 0) return null;

        int i = Math.floorMod(index, size * steps * frames);
        int goodIdx = i % size;
        int stepIdx = (i / size) % steps;
        int frameIdx = (i / (size * steps)) % frames;

        CommissionGoods.Good good = CommissionGoods.byIndex(goodIdx);
        if (good == null) return null;
        int count = CommissionGoods.countFor(good, stepIdx);
        return new Commission(i, good, count, Framing.values()[frameIdx]);
    }

    // ── Rewards ───────────────────────────────────────────────────────────

    /**
     * Coins paid on completion.
     *
     * <p>Deliberately below what the same goods fetch at the bazaar for the easy bands and well
     * above it at the hard ones. A commission should never be the efficient way to sell dirt, and
     * should absolutely be the reason somebody goes after sixty four netherite blocks.</p>
     */
    public long coinReward() {
        double mult = switch (band) {
            case ERRAND -> 0.6;
            case TASK -> 0.9;
            case JOB -> 1.3;
            case UNDERTAKING -> 1.8;
            case CHARTER -> 2.4;
            case GREAT_WORK -> 3.2;
            case IMPOSSIBILITY -> 4.5;
        };
        return Math.max(20L, Math.round(difficulty * mult));
    }

    /** Vanilla experience, which is the reward players feel most immediately. */
    public int xpReward() {
        return (int) Math.min(6000, 8 + difficulty / 40);
    }

    /** Board standing, which is what unlocks further slots. */
    public int standingReward() {
        return switch (band) {
            case ERRAND -> 1;
            case TASK -> 2;
            case JOB -> 4;
            case UNDERTAKING -> 7;
            case CHARTER -> 12;
            case GREAT_WORK -> 20;
            case IMPOSSIBILITY -> 35;
        };
    }

    /** Crate keys, which start appearing once a commission is a real piece of work. */
    public Crate keyReward() {
        return switch (band) {
            case ERRAND, TASK -> null;
            case JOB -> Crate.WOODEN;
            case UNDERTAKING -> Crate.IRON;
            case CHARTER -> Crate.GOLDEN;
            case GREAT_WORK -> Crate.ARCANE;
            case IMPOSSIBILITY -> Crate.CELESTIAL;
        };
    }

    /** Whether finishing this is worth a piece of the warrant set. */
    public boolean awardsWarrantPiece() {
        return band == Band.GREAT_WORK || band == Band.IMPOSSIBILITY;
    }

    /** The line printed on the posting. */
    public String title() {
        return "Deliver " + count + " " + good.display();
    }

    public String colouredTitle() {
        return framing.colour + title();
    }
}
