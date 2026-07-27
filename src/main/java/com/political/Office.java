package com.political;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.Locale;

/**
 * The forty five offices of the Regalia line.
 *
 * <p>Regalia is the line that reads the politics. Every other family asks what you are holding or
 * where you are standing. This one asks what your settlement is, whether you hold a seat in it and
 * whether your polity is currently at war, and it pays out accordingly. It is the exact mirror of
 * Voyager: Voyager is strongest a dozen Leagues from anywhere, Regalia is strongest at home with a
 * title, and a Utopia chair in full Regalia is the single strongest thing in the mod.</p>
 */
public enum Office {

    // ── Crown estate: the executive, and the weight it carries ─────────────
    CHAIRS_CIRCLET("Chair's Circlet", Estate.CROWN, ChatFormatting.GOLD, Items.GOLDEN_HELMET,
            "Light until the first vote goes against you.", "strength", 55, Mandate.COMMAND),
    VICE_SIGNET("Vice Signet", Estate.CROWN, ChatFormatting.YELLOW, Items.GOLD_INGOT,
            "Second, and unusually comfortable about it.", "defense", 50, Mandate.COMMAND),
    WRIT_OF_OFFICE("Writ of Office", Estate.CROWN, ChatFormatting.WHITE, Items.WRITABLE_BOOK,
            "It says you may. That is the whole magic.", "intelligence", 65, Mandate.AUTHORITY),
    CHAIN_OF_STATE("Chain of State", Estate.CROWN, ChatFormatting.GOLD, Items.GOLD_BLOCK,
            "Worn around the neck, felt everywhere else.", "health", 70, Mandate.AUTHORITY),
    THE_GAVEL_SEAT("The Gavel Seat", Estate.CROWN, ChatFormatting.RED, Items.ANVIL,
            "Nobody sits in it comfortably and everybody wants it.", "crit_damage", 60, Mandate.VERDICT),
    STATE_MANTLE("State Mantle", Estate.CROWN, ChatFormatting.LIGHT_PURPLE, Items.PURPLE_WOOL,
            "Heavier every year it is not taken off.", "defense", 60, Mandate.AEGIS),
    CROWN_OF_TERMS("Crown of Terms", Estate.CROWN, ChatFormatting.YELLOW, Items.CLOCK,
            "It has an expiry date and pretends otherwise.", "intelligence", 60, Mandate.TENURE),
    THE_CASTING_VOTE("The Casting Vote", Estate.CROWN, ChatFormatting.WHITE, Items.LEVER,
            "Used once a decade and remembered forever.", "crit_chance", 20, Mandate.VERDICT),
    SUCCESSION_KEY("Succession Key", Estate.CROWN, ChatFormatting.GRAY, Items.TRIPWIRE_HOOK,
            "Somebody is holding the other copy.", "ferocity", 15, Mandate.COMMAND),

    // ── Bench estate: law, argument, the written down parts ────────────────
    STATUTE_ROLL("Statute Roll", Estate.BENCH, ChatFormatting.WHITE, Items.PAPER,
            "Longer than anybody has read.", "intelligence", 70, Mandate.AUTHORITY),
    BENCH_ROBE("Bench Robe", Estate.BENCH, ChatFormatting.DARK_RED, Items.RED_WOOL,
            "It is supposed to make the person disappear.", "defense", 55, Mandate.AEGIS),
    OATH_STONE("Oath Stone", Estate.BENCH, ChatFormatting.GRAY, Items.STONE,
            "Sworn on by people who meant it at the time.", "health", 65, Mandate.CONCORD),
    THE_PRECEDENT("The Precedent", Estate.BENCH, ChatFormatting.GOLD, Items.BOOKSHELF,
            "It decided this before you were born.", "intelligence", 65, Mandate.VERDICT),
    WARRANT_SEAL("Warrant Seal", Estate.BENCH, ChatFormatting.RED, Items.HONEYCOMB,
            "Somebody's day is about to change.", "strength", 50, Mandate.VERDICT),
    RECUSAL_PIN("Recusal Pin", Estate.BENCH, ChatFormatting.AQUA, Items.IRON_NUGGET,
            "Worn by the only honest person in the room.", "crit_chance", 15, Mandate.CONCORD),
    HANSARD_LEAF("Hansard Leaf", Estate.BENCH, ChatFormatting.GREEN, Items.WRITTEN_BOOK,
            "It wrote down what you actually said.", "intelligence", 60, Mandate.TENURE),
    THE_LONG_DEBATE("The Long Debate", Estate.BENCH, ChatFormatting.YELLOW, Items.NOTE_BLOCK,
            "Nobody has changed their mind in nine hours.", "defense", 50, Mandate.FILIBUSTER),
    DISSENT_MARK("Dissent Mark", Estate.BENCH, ChatFormatting.DARK_PURPLE, Items.INK_SAC,
            "The minority opinion, kept on the record.", "crit_damage", 55, Mandate.FILIBUSTER),

    // ── Coffer estate: money, and everything money is actually for ─────────
    TREASURY_KEY("Treasury Key", Estate.COFFER, ChatFormatting.GOLD, Items.GOLD_NUGGET,
            "There are three, and nobody agrees who has the third.", "intelligence", 60, Mandate.TITHE),
    MINT_DIE("Mint Die", Estate.COFFER, ChatFormatting.YELLOW, Items.IRON_BLOCK,
            "It decides what a coin is worth by existing.", "strength", 45, Mandate.TITHE),
    LEDGER_CLASP("Ledger Clasp", Estate.COFFER, ChatFormatting.WHITE, Items.LEATHER,
            "It holds the book shut, mostly.", "defense", 45, Mandate.TITHE),
    TAX_ROLL("Tax Roll", Estate.COFFER, ChatFormatting.GREEN, Items.EMERALD,
            "Everybody is on it and nobody enjoys it.", "health", 60, Mandate.TITHE),
    BOND_SEAL("Bond Seal", Estate.COFFER, ChatFormatting.AQUA, Items.LAPIS_BLOCK,
            "A promise with a date on it.", "intelligence", 65, Mandate.PROSPERITY),
    GRANARY_TALLY("Granary Tally", Estate.COFFER, ChatFormatting.GOLD, Items.HAY_BLOCK,
            "The most important document in a bad year.", "health", 70, Mandate.PROSPERITY),
    TOLL_CHAIN("Toll Chain", Estate.COFFER, ChatFormatting.DARK_GRAY, Items.IRON_BARS,
            "Down until somebody pays.", "defense", 55, Mandate.AEGIS),
    GUILD_CHARTER("Guild Charter", Estate.COFFER, ChatFormatting.LIGHT_PURPLE, Items.ENCHANTED_BOOK,
            "It let a small group agree on prices, legally.", "crit_chance", 15, Mandate.PROSPERITY),
    THE_SURPLUS("The Surplus", Estate.COFFER, ChatFormatting.YELLOW, Items.GOLD_BLOCK,
            "Announced loudly, spent quietly.", "ferocity", 15, Mandate.PROSPERITY),

    // ── Guard estate: the part of politics that carries something ──────────
    MARSHALS_BATON("Marshal's Baton", Estate.GUARD, ChatFormatting.RED, Items.STICK,
            "Held rather than used, ideally.", "strength", 60, Mandate.MUSTER),
    WATCH_HORN("Watch Horn", Estate.GUARD, ChatFormatting.GOLD, Items.GOAT_HORN,
            "One note, and everybody knows what it means.", "ferocity", 20, Mandate.MUSTER),
    LEVY_BANNER("Levy Banner", Estate.GUARD, ChatFormatting.DARK_RED, Items.RED_BANNER,
            "Raised, and the fields empty out.", "strength", 55, Mandate.MUSTER),
    WALL_WARRANT("Wall Warrant", Estate.GUARD, ChatFormatting.GRAY, Items.STONE_BRICKS,
            "It funded the part nobody sees.", "defense", 70, Mandate.AEGIS),
    SIEGE_LEDGER("Siege Ledger", Estate.GUARD, ChatFormatting.DARK_GRAY, Items.CROSSBOW,
            "Days, rations, and one honest estimate.", "intelligence", 60, Mandate.WARFOOT),
    TRUCE_FLAG("Truce Flag", Estate.GUARD, ChatFormatting.WHITE, Items.WHITE_BANNER,
            "It works right up until it does not.", "health", 65, Mandate.CONCORD),
    ARMOURY_TAG("Armoury Tag", Estate.GUARD, ChatFormatting.AQUA, Items.NETHERITE_SCRAP,
            "Signed for, and rarely signed back in.", "crit_damage", 60, Mandate.WARFOOT),
    BORDER_STONE("Border Stone", Estate.GUARD, ChatFormatting.GREEN, Items.DEEPSLATE_BRICKS,
            "Moved twice, both times at night.", "defense", 65, Mandate.WARFOOT),
    THE_STANDING_ORDER("The Standing Order", Estate.GUARD, ChatFormatting.DARK_RED, Items.SHIELD,
            "Nobody remembers who wrote it and everybody follows it.", "health", 75, Mandate.AEGIS),

    // ── Commons estate: the people the rest of it is theoretically for ─────
    PETITION_ROLL("Petition Roll", Estate.COMMONS, ChatFormatting.GREEN, Items.PAPER,
            "Four hundred names and one very tired clerk.", "intelligence", 55, Mandate.CONCORD),
    WARD_TOKEN("Ward Token", Estate.COMMONS, ChatFormatting.WHITE, Items.CLAY_BALL,
            "It proves you live where you say you live.", "health", 55, Mandate.HEARTH),
    COMMONS_KEY("Commons Key", Estate.COMMONS, ChatFormatting.GOLD, Items.IRON_INGOT,
            "It opens the hall, which is the point of the hall.", "defense", 50, Mandate.HEARTH),
    HEARTH_RIGHT("Hearth Right", Estate.COMMONS, ChatFormatting.RED, Items.CAMPFIRE,
            "Older than any law that mentions it.", "health", 65, Mandate.HEARTH),
    MARKET_CHARTER("Market Charter", Estate.COMMONS, ChatFormatting.YELLOW, Items.BARREL,
            "One day a week, and the whole town turns up.", "intelligence", 55, Mandate.PROSPERITY),
    THE_ROLL_CALL("The Roll Call", Estate.COMMONS, ChatFormatting.AQUA, Items.BELL,
            "Everybody is counted, including the awkward ones.", "crit_chance", 15, Mandate.TENURE),
    FIELD_RIGHT("Field Right", Estate.COMMONS, ChatFormatting.DARK_GREEN, Items.WHEAT,
            "Grazing, gleaning and one very old argument.", "health", 60, Mandate.HEARTH),
    APPRENTICE_BOND("Apprentice Bond", Estate.COMMONS, ChatFormatting.GOLD, Items.CRAFTING_TABLE,
            "Seven years, and then you are somebody.", "crit_damage", 50, Mandate.PROSPERITY),
    THE_QUIET_MAJORITY("The Quiet Majority", Estate.COMMONS, ChatFormatting.GRAY, Items.WHITE_WOOL,
            "It has not said anything and it decides everything.", "defense", 60, Mandate.CONCORD);

    /** The five estates. Three pieces of an estate lights its standing bonus. */
    public enum Estate {
        CROWN("Crown", ChatFormatting.GOLD, "Holding a seat is worth far more, and losing one hurts."),
        BENCH("Bench", ChatFormatting.WHITE, "Your settlement's tier counts one step higher than it is."),
        COFFER("Coffer", ChatFormatting.YELLOW, "Everything you earn at home earns again."),
        GUARD("Guard", ChatFormatting.RED, "Wartime turns the whole set from defensive to lethal."),
        COMMONS("Commons", ChatFormatting.GREEN, "The set works for every registered citizen, seat or not.");

        public final String label;
        public final ChatFormatting colour;
        public final String bonusText;

        Estate(String label, ChatFormatting colour, String bonusText) {
            this.label = label;
            this.colour = colour;
            this.bonusText = bonusText;
        }
    }

    /** What an office grants once Standing is high enough. */
    public enum Mandate {
        COMMAND, AUTHORITY, VERDICT, AEGIS, TENURE, FILIBUSTER,
        TITHE, PROSPERITY, MUSTER, WARFOOT, CONCORD, HEARTH
    }

    public final String display;
    public final Estate estate;
    public final ChatFormatting colour;
    public final Item token;
    public final String flavor;
    public final String statKey;
    public final int statAmount;
    public final Mandate mandate;

    Office(String display, Estate estate, ChatFormatting colour, Item token, String flavor,
           String statKey, int statAmount, Mandate mandate) {
        this.display = display;
        this.estate = estate;
        this.colour = colour;
        this.token = token;
        this.flavor = flavor;
        this.statKey = statKey;
        this.statAmount = statAmount;
        this.mandate = mandate;
    }

    public String key() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String powerText() {
        return "§6" + switch (mandate) {
            case COMMAND -> "Standing turns into raw force while you hold a seat.";
            case AUTHORITY -> "Standing turns into Intelligence and reach.";
            case VERDICT -> "Standing sharpens every strike you land at home.";
            case AEGIS -> "Standing becomes armour that grows with your settlement.";
            case TENURE -> "The longer your side holds office, the stronger this gets.";
            case FILIBUSTER -> "You cannot be moved, slowed or silenced on home ground.";
            case TITHE -> "A share of your settlement's take reaches you directly.";
            case PROSPERITY -> "Coin and experience both come in faster at home.";
            case MUSTER -> "Allies standing with you take your Standing as their own.";
            case WARFOOT -> "Wartime turns your Standing into damage against the enemy.";
            case CONCORD -> "You mend steadily while inside your own settlement.";
            case HEARTH -> "Registered citizens near you are healed and shielded.";
        };
    }

    public GearLine.Motif motif() {
        return new GearLine.Motif(key(), display, colour, token, flavor,
                estate.label, estate.colour, estate.bonusText,
                java.util.List.of(GearLine.stat(statKey, statAmount)),
                "Scales with Standing: your settlement's tier plus your seat.");
    }

    public static Office byKey(String key) {
        if (key == null) return null;
        for (Office o : values()) {
            if (o.key().equalsIgnoreCase(key)) return o;
        }
        return null;
    }
}
