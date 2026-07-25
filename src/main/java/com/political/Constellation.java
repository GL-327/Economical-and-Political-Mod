package com.political;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/**
 * The thirty six constellations.
 *
 * <p>Where a rune fires on an event, a constellation is always on. Each one grants a standing
 * bonus while its piece is carried, and each belongs to a <b>house</b>. Wearing three pieces of the
 * same house lights the house bonus on top, so the collection has a reason to be a collection.</p>
 */
public enum Constellation {

    // ── House of the Forge: making, mending, enduring ──────────────────────
    ANVIL("The Anvil", House.FORGE, ChatFormatting.GRAY, Items.ANVIL,
            "Struck often, broken never.", Boon.DEFENSE, 45),
    BELLOWS("The Bellows", House.FORGE, ChatFormatting.GOLD, Items.CAMPFIRE,
            "Air feeds the fire that feeds the work.", Boon.HASTE, 1),
    CRUCIBLE("The Crucible", House.FORGE, ChatFormatting.RED, Items.CAULDRON,
            "Everything worth having is melted first.", Boon.FIRE_WARD, 1),
    HAMMER("The Hammer", House.FORGE, ChatFormatting.WHITE, Items.IRON_INGOT,
            "The argument that ends arguments.", Boon.STRENGTH, 35),
    TONGS("The Tongs", House.FORGE, ChatFormatting.DARK_GRAY, Items.SHEARS,
            "Hold what others cannot.", Boon.HEALTH, 40),
    QUENCH("The Quench", House.FORGE, ChatFormatting.AQUA, Items.WATER_BUCKET,
            "The moment steel decides what it is.", Boon.REGEN, 1),

    // ── House of the Hunt: pursuit, precision, the kill ────────────────────
    HOUND("The Hound", House.HUNT, ChatFormatting.GOLD, Items.BONE,
            "It has your scent and its own patience.", Boon.SPEED, 1),
    SNARE("The Snare", House.HUNT, ChatFormatting.DARK_GREEN, Items.STRING,
            "Waiting is most of hunting.", Boon.STRENGTH, 30),
    QUIVER("The Quiver", House.HUNT, ChatFormatting.GREEN, Items.ARROW,
            "Counted, and never quite enough.", Boon.CRIT_CHANCE, 10),
    FANG("The Fang", House.HUNT, ChatFormatting.RED, Items.NETHERITE_SCRAP,
            "One good bite ends the argument.", Boon.CRIT_DAMAGE, 45),
    TRACKER("The Tracker", House.HUNT, ChatFormatting.YELLOW, Items.COMPASS,
            "Nothing walks without leaving word.", Boon.NIGHT_EYE, 1),
    HORN("The Horn", House.HUNT, ChatFormatting.GOLD, Items.GOAT_HORN,
            "Called once, answered by many.", Boon.FEROCITY, 10),

    // ── House of the Deep: pressure, patience, the dark below ──────────────
    LANTERNFISH("The Lanternfish", House.DEEP, ChatFormatting.AQUA, Items.GLOW_INK_SAC,
            "Its own small light, and nothing else.", Boon.NIGHT_EYE, 1),
    TRENCH("The Trench", House.DEEP, ChatFormatting.BLUE, Items.PRISMARINE,
            "There is always further down.", Boon.DEFENSE, 55),
    CURRENT("The Current", House.DEEP, ChatFormatting.DARK_AQUA, Items.HEART_OF_THE_SEA,
            "It decides where you are going.", Boon.WATER_GRACE, 1),
    PEARL("The Pearl", House.DEEP, ChatFormatting.WHITE, Items.NAUTILUS_SHELL,
            "An irritation, patiently made beautiful.", Boon.INTELLIGENCE, 60),
    KRAKEN("The Kraken", House.DEEP, ChatFormatting.DARK_PURPLE, Items.INK_SAC,
            "Old, and content to stay unseen.", Boon.STRENGTH, 40),
    TIDEMARK("The Tidemark", House.DEEP, ChatFormatting.AQUA, Items.SEA_PICKLE,
            "It records how high things reached.", Boon.HEALTH, 55),

    // ── House of the Crown: rule, wealth, the weight of office ─────────────
    SCEPTRE("The Sceptre", House.CROWN, ChatFormatting.GOLD, Items.GOLDEN_SWORD,
            "Carried by those who need not use it.", Boon.STRENGTH, 45),
    COFFER("The Coffer", House.CROWN, ChatFormatting.YELLOW, Items.GOLD_BLOCK,
            "Full, and never full enough.", Boon.COIN_FIND, 1),
    SEAL("The Seal", House.CROWN, ChatFormatting.RED, Items.HONEYCOMB,
            "A pressed shape that ends discussion.", Boon.DEFENSE, 50),
    THRONE("The Throne", House.CROWN, ChatFormatting.LIGHT_PURPLE, Items.PURPUR_BLOCK,
            "Comfortable for nobody.", Boon.HEALTH, 65),
    DIADEM("The Diadem", House.CROWN, ChatFormatting.WHITE, Items.GOLDEN_HELMET,
            "Light on the head, heavy elsewhere.", Boon.INTELLIGENCE, 70),
    LEDGER("The Ledger", House.CROWN, ChatFormatting.GRAY, Items.WRITABLE_BOOK,
            "It remembers what you would rather it did not.", Boon.XP_GAIN, 1),

    // ── House of the Veil: shadow, secrets, the unseen ─────────────────────
    MOTH("The Moth", House.VEIL, ChatFormatting.DARK_GRAY, Items.PHANTOM_MEMBRANE,
            "Drawn to the thing that will end it.", Boon.SPEED, 1),
    KEYHOLE("The Keyhole", House.VEIL, ChatFormatting.GRAY, Items.TRIPWIRE_HOOK,
            "Small, and enough.", Boon.CRIT_CHANCE, 15),
    WHISPER("The Whisper", House.VEIL, ChatFormatting.DARK_PURPLE, Items.SCULK_SENSOR,
            "Travels further than shouting.", Boon.INTELLIGENCE, 55),
    CLOAK("The Cloak", House.VEIL, ChatFormatting.BLACK, Items.BLACK_WOOL,
            "Not invisibility. Just disinterest.", Boon.DEFENSE, 40),
    DAGGER_STAR("The Dagger", House.VEIL, ChatFormatting.DARK_RED, Items.IRON_SWORD,
            "Close work, quietly done.", Boon.CRIT_DAMAGE, 55),
    ASHES("The Ashes", House.VEIL, ChatFormatting.GRAY, Items.GUNPOWDER,
            "What is left when the story ends.", Boon.FIRE_WARD, 1),

    // ── House of the Bloom: growth, plenty, renewal ────────────────────────
    SEEDLING("The Seedling", House.BLOOM, ChatFormatting.GREEN, Items.WHEAT_SEEDS,
            "Absurd optimism, buried on purpose.", Boon.REGEN, 1),
    ORCHARD("The Orchard", House.BLOOM, ChatFormatting.GOLD, Items.APPLE,
            "Planted by people who will not eat from it.", Boon.HEALTH, 60),
    THORNVINE("The Thornvine", House.BLOOM, ChatFormatting.DARK_GREEN, Items.VINE,
            "Generous and armed.", Boon.STRENGTH, 35),
    HARVEST("The Harvest", House.BLOOM, ChatFormatting.YELLOW, Items.HAY_BLOCK,
            "The year finally pays out.", Boon.COIN_FIND, 1),
    ROOTWORK("The Rootwork", House.BLOOM, ChatFormatting.DARK_GREEN, Items.MOSS_BLOCK,
            "Most of it is underneath.", Boon.DEFENSE, 45),
    POLLEN("The Pollen", House.BLOOM, ChatFormatting.LIGHT_PURPLE, Items.PINK_PETALS,
            "Carried by things that never agreed to it.", Boon.XP_GAIN, 1);

    /** The six houses. Three pieces of a house lights its standing bonus. */
    public enum House {
        FORGE("Forge", ChatFormatting.GOLD, "Your gear holds together and your hands stay quick."),
        HUNT("Hunt", ChatFormatting.RED, "You find what you are looking for and end it faster."),
        DEEP("Deep", ChatFormatting.BLUE, "Pressure stops mattering and the dark stops hiding things."),
        CROWN("Crown", ChatFormatting.YELLOW, "Coin and standing accrue to you without asking."),
        VEIL("Veil", ChatFormatting.DARK_GRAY, "You are harder to notice and worse to be noticed by."),
        BLOOM("Bloom", ChatFormatting.GREEN, "You mend, you grow, and the ground is generous.");

        public final String label;
        public final ChatFormatting colour;
        public final String bonusText;

        House(String label, ChatFormatting colour, String bonusText) {
            this.label = label;
            this.colour = colour;
            this.bonusText = bonusText;
        }
    }

    /** What a constellation grants while carried. */
    public enum Boon {
        STRENGTH, DEFENSE, HEALTH, INTELLIGENCE, CRIT_CHANCE, CRIT_DAMAGE, FEROCITY,
        SPEED, HASTE, REGEN, NIGHT_EYE, WATER_GRACE, FIRE_WARD, COIN_FIND, XP_GAIN
    }

    public final String display;
    public final House house;
    public final ChatFormatting colour;
    public final Item token;
    public final String flavor;
    public final Boon boon;
    /** Stat amount for stat boons, or a level for effect boons. */
    public final int amount;

    Constellation(String display, House house, ChatFormatting colour, Item token,
                  String flavor, Boon boon, int amount) {
        this.display = display;
        this.house = house;
        this.colour = colour;
        this.token = token;
        this.flavor = flavor;
        this.boon = boon;
        this.amount = amount;
    }

    public String key() {
        return name().toLowerCase(java.util.Locale.ROOT);
    }

    /** The stat line this constellation contributes, or null if it grants an effect instead. */
    public String statLine() {
        return switch (boon) {
            case STRENGTH -> "§7❁ Strength: §c+" + amount;
            case DEFENSE -> "§7❈ Defense: §a+" + amount;
            case HEALTH -> "§7❤ Health: §c+" + amount;
            case INTELLIGENCE -> "§7✎ Intelligence: §b+" + amount;
            case CRIT_CHANCE -> "§7☣ Crit Chance: §9+" + amount + "%";
            case CRIT_DAMAGE -> "§7☠ Crit Damage: §9+" + amount + "%";
            case FEROCITY -> "§7⫽ Ferocity: §c+" + amount;
            default -> null;
        };
    }

    /** A short description of an effect boon, or null for stat boons. */
    public String effectText() {
        return switch (boon) {
            case SPEED -> "You move quicker while this is carried.";
            case HASTE -> "Your hands work quicker while this is carried.";
            case REGEN -> "You mend slowly while this is carried.";
            case NIGHT_EYE -> "The dark does not hide things from you.";
            case WATER_GRACE -> "Water carries you and you do not drown.";
            case FIRE_WARD -> "Fire treats you gently.";
            case COIN_FIND -> "Kills shake loose a little extra coin.";
            case XP_GAIN -> "You take a little more experience from everything.";
            default -> null;
        };
    }

    public static Constellation byKey(String key) {
        if (key == null) return null;
        for (Constellation c : values()) {
            if (c.key().equalsIgnoreCase(key) || c.name().equalsIgnoreCase(key)) return c;
        }
        return null;
    }
}
