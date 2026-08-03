package com.political;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Locale;

/**
 * The forty five identities of the Ashen Crown line.
 *
 * <p>Every piece feeds the same <b>The Crown</b> meter, which fills as you fight and spends
 * itself on whatever the piece in your hand does. Mixing identities across a set is a real
 * choice rather than a penalty, since the meter does not care which piece filled it.</p>
 */
public enum Ashen {

    // ── Court ──
    PAUPER_KING("The Pauper King", Group.COURT, ChatFormatting.GRAY, Items.BONE,
            "He kept the crown and nothing else.", "strength", 55,
            "The Crown claims a share of what you kill."),
    WIDOW_REGENT("The Widow Regent", Group.COURT, ChatFormatting.DARK_PURPLE, Items.BLACK_DYE,
            "She rules on behalf of a room.", "intelligence", 85,
            "The Crown rules one enemy for eight seconds."),
    SILENT_CHANCELLOR("The Silent Chancellor", Group.COURT, ChatFormatting.DARK_GRAY, Items.PAPER,
            "He signs everything and says nothing.", "defense", 70,
            "The Crown signs away a share of the damage you take."),
    HOLLOW_HEIR("The Hollow Heir", Group.COURT, ChatFormatting.WHITE, Items.SKELETON_SKULL,
            "Waiting for something already gone.", "health", 70,
            "The Crown gives you the health of whatever you killed."),
    GRAVE_MARSHAL("The Grave Marshal", Group.COURT, ChatFormatting.DARK_RED, Items.IRON_SWORD,
            "He still calls the muster.", "strength", 65,
            "The Crown raises three of the dead to fight for you."),
    ASH_STEWARD("The Ash Steward", Group.COURT, ChatFormatting.GRAY, Items.GUNPOWDER,
            "He keeps the accounts of a burned house.", "crit_chance", 22,
            "The Crown keeps a tally and pays it out at once."),
    BONE_HERALD("The Bone Herald", Group.COURT, ChatFormatting.WHITE, Items.BONE_MEAL,
            "He announces arrivals nobody wanted.", "ferocity", 20,
            "The Crown announces you and they lose their nerve."),
    WAX_CONFESSOR("The Wax Confessor", Group.COURT, ChatFormatting.YELLOW, Items.HONEYCOMB,
            "He hears it all and melts.", "intelligence", 90,
            "The Crown pulls one secret out and it hurts."),
    CRYPT_WARDEN("The Crypt Warden", Group.COURT, ChatFormatting.DARK_GRAY, Items.DEEPSLATE_BRICKS,
            "He locks a door nobody tries.", "defense", 80,
            "The Crown locks you out of harm for three seconds."),

    // ── Barrow ──
    BARROW_BLADE("The Barrow Blade", Group.BARROW, ChatFormatting.GRAY, Items.STONE_SWORD,
            "Buried with someone who used it.", "crit_damage", 80,
            "The Crown swings with every hand that held it."),
    GRAVE_GOODS("The Grave Goods", Group.BARROW, ChatFormatting.GOLD, Items.GOLD_NUGGET,
            "Everything they thought they would need.", "strength", 60,
            "The Crown pays you for every kill in the fight."),
    MOURNER_CHOIR("The Mourner Choir", Group.BARROW, ChatFormatting.DARK_BLUE, Items.NOTE_BLOCK,
            "They have not stopped.", "intelligence", 95,
            "The Crown mourns loudly and everything nearby weakens."),
    CAIRN_CROWN("The Cairn Crown", Group.BARROW, ChatFormatting.GRAY, Items.COBBLESTONE,
            "One stone for every one of them.", "defense", 85,
            "The Crown stacks a cairn that eats hits."),
    SHROUD("The Shroud", Group.BARROW, ChatFormatting.WHITE, Items.WHITE_WOOL,
            "It fits everyone eventually.", "health", 85,
            "The Crown wraps you and nothing can see you."),
    GRAVE_DUST("The Grave Dust", Group.BARROW, ChatFormatting.GRAY, Items.GRAVEL,
            "What is left of the argument.", "ferocity", 22,
            "The Crown throws dust that blinds and rots."),
    COFFIN_NAIL("The Coffin Nail", Group.BARROW, ChatFormatting.DARK_GRAY, Items.IRON_NUGGET,
            "The last thing said on the matter.", "crit_chance", 26,
            "The Crown finishes anything under a fifth of its health."),
    PASSAGE_COIN("The Passage Coin", Group.BARROW, ChatFormatting.GOLD, Items.GOLD_INGOT,
            "Paid once, on the way.", "health", 80,
            "The Crown buys you a way back from one death."),
    UNQUIET_BELL("The Unquiet Bell", Group.BARROW, ChatFormatting.YELLOW, Items.BELL,
            "Rung by nobody, all night.", "crit_damage", 85,
            "The Crown rings and everything undead nearby serves you."),

    // ── Wight ──
    WIGHT_SOVEREIGN("The Wight Sovereign", Group.WIGHT, ChatFormatting.DARK_AQUA, Items.SOUL_LANTERN,
            "Cold, and still holding court.", "strength", 80,
            "The Crown drains the warmth out of everything near."),
    FROZEN_OATH("The Frozen Oath", Group.WIGHT, ChatFormatting.AQUA, Items.BLUE_ICE,
            "Sworn once and never revisited.", "defense", 90,
            "The Crown holds your defence at its highest all fight."),
    GRIM_LEDGER("The Grim Ledger", Group.WIGHT, ChatFormatting.DARK_GRAY, Items.WRITABLE_BOOK,
            "Every debt, and it collects.", "intelligence", 100,
            "The Crown collects a debt from every enemy you touched."),
    BARROW_WIGHT("The Barrow Wight", Group.WIGHT, ChatFormatting.DARK_BLUE, Items.SOUL_SOIL,
            "It only wants what it had.", "ferocity", 24,
            "The Crown takes a share of their stats for the fight."),
    PALE_RIDER("The Pale Rider", Group.WIGHT, ChatFormatting.WHITE, Items.SADDLE,
            "He arrives on schedule.", "speed", 7,
            "The Crown carries you across the field instantly."),
    HUNGERING_DARK("The Hungering Dark", Group.WIGHT, ChatFormatting.BLACK, Items.BLACK_CONCRETE,
            "It has been patient for long enough.", "strength", 85,
            "The Crown eats the light and everything in it."),
    SOUL_TITHE("The Soul Tithe", Group.WIGHT, ChatFormatting.LIGHT_PURPLE, Items.SOUL_TORCH,
            "A tenth of everything, forever.", "crit_damage", 95,
            "The Crown takes a tenth of their health outright."),
    RIME_SEPULCHRE("The Rime Sepulchre", Group.WIGHT, ChatFormatting.AQUA, Items.PACKED_ICE,
            "Cold enough to keep them honest.", "defense", 95,
            "The Crown seals you in ice that heals while it holds."),
    LONG_VIGIL("The Long Vigil", Group.WIGHT, ChatFormatting.GRAY, Items.LANTERN,
            "Kept by somebody who forgot why.", "health", 95,
            "The Crown keeps you standing while anything near you dies."),

    // ── Sovereign ──
    ASHEN_CROWN("The Ashen Crown", Group.SOVEREIGN, ChatFormatting.DARK_RED, Items.GOLDEN_HELMET,
            "The one everything else is named after.", "strength", 105,
            "The Crown does whatever the last piece you wore did, again."),
    THRONE_OF_NAMES("The Throne of Names", Group.SOVEREIGN, ChatFormatting.GOLD, Items.GOLD_BLOCK,
            "Every one of them is carved into it.", "intelligence", 115,
            "The Crown names your target and every ally hits it harder."),
    DEAD_REALM("The Dead Realm", Group.SOVEREIGN, ChatFormatting.DARK_PURPLE, Items.END_STONE,
            "It has borders and no people.", "defense", 110,
            "The Crown draws a border and nothing crosses it."),
    BONE_SCEPTRE("The Bone Sceptre", Group.SOVEREIGN, ChatFormatting.WHITE, Items.BONE_BLOCK,
            "Held by whoever is left.", "crit_damage", 115,
            "The Crown strikes with the weight of everything buried."),
    LAST_DECREE("The Last Decree", Group.SOVEREIGN, ChatFormatting.DARK_RED, Items.PAPER,
            "Nobody was there to hear it.", "crit_chance", 32,
            "The Crown decrees a kill, and it happens."),
    MOURNING_STAR("The Mourning Star", Group.SOVEREIGN, ChatFormatting.LIGHT_PURPLE, Items.NETHER_STAR,
            "It rose the night the court fell.", "ferocity", 30,
            "The Crown drops a star on everything you can see."),
    GRAVE_EMPIRE("The Grave Empire", Group.SOVEREIGN, ChatFormatting.DARK_GRAY, Items.OBSIDIAN,
            "Larger every year, by definition.", "health", 120,
            "The Crown adds a subject for every kill, and they fight."),
    SILENT_CORONATION("The Silent Coronation", Group.SOVEREIGN, ChatFormatting.GRAY, Items.GOLDEN_CARROT,
            "Attended by nobody at all.", "defense", 115,
            "The Crown crowns you and nothing can interrupt you."),
    END_OF_THE_LINE("The End of the Line", Group.SOVEREIGN, ChatFormatting.BLACK, Items.WITHER_SKELETON_SKULL,
            "There is no heir. That is the point.", "strength", 120,
            "The Crown ends it, whatever it was."),

    // ── Rite ──
    NAMELESS_RITE("The Nameless Rite", Group.RITE, ChatFormatting.DARK_PURPLE, Items.CANDLE,
            "Performed by people who did not sign it.", "intelligence", 120,
            "The Crown performs a rite and something answers."),
    SALT_CIRCLE("The Salt Circle", Group.RITE, ChatFormatting.WHITE, Items.GLASS_BOTTLE,
            "Drawn once and never crossed.", "defense", 120,
            "The Crown draws a circle nothing dead can enter."),
    WIDOW_KNOT("The Widow Knot", Group.RITE, ChatFormatting.DARK_GRAY, Items.STRING,
            "Tied so it can never be undone.", "health", 120,
            "The Crown ties your fate to your target's."),
    GRAVE_WATCH("The Grave Watch", Group.RITE, ChatFormatting.GRAY, Items.SOUL_CAMPFIRE,
            "Somebody has to.", "crit_chance", 34,
            "The Crown watches, and nothing gets behind you."),
    UNSAID_NAME("The Unsaid Name", Group.RITE, ChatFormatting.BLACK, Items.BOOK,
            "Everybody knows it and nobody uses it.", "crit_damage", 120,
            "The Crown says it, and the target simply stops."),
    BONE_CHOIR("The Bone Choir", Group.RITE, ChatFormatting.WHITE, Items.BONE_MEAL,
            "Forty voices, none of them breathing.", "ferocity", 32,
            "The Crown sings and everything undead nearby rises for you."),
    MOURNING_COIN("The Mourning Coin", Group.RITE, ChatFormatting.GOLD, Items.GOLD_NUGGET,
            "Spent on the way out, always.", "strength", 115,
            "The Crown spends a coin and buys a kill."),
    LONG_PROCESSION("The Long Procession", Group.RITE, ChatFormatting.DARK_BLUE, Items.LANTERN,
            "It has not reached the gate yet.", "speed", 8,
            "The Crown carries you forward and everything follows."),
    FINAL_RITE("The Final Rite", Group.RITE, ChatFormatting.DARK_RED, Items.SOUL_LANTERN,
            "After this there is nothing to perform.", "strength", 125,
            "The Crown performs the last one and the field empties.");

    /** The houses of the line, each with its own set bonus. */
    public enum Group {
        COURT("Court", ChatFormatting.AQUA, "Three of the court house: The Crown fills a quarter faster."),
        BARROW("Barrow", ChatFormatting.AQUA, "Three of the barrow house: The Crown fills a quarter faster."),
        WIGHT("Wight", ChatFormatting.AQUA, "Three of the wight house: The Crown fills a quarter faster."),
        SOVEREIGN("Sovereign", ChatFormatting.AQUA, "Three of the sovereign house: The Crown fills a quarter faster."),
        RITE("Rite", ChatFormatting.AQUA, "Three of the rite house: The Crown fills a quarter faster.");

        public final String display;
        public final ChatFormatting colour;
        public final String bonus;

        Group(String display, ChatFormatting colour, String bonus) {
            this.display = display;
            this.colour = colour;
            this.bonus = bonus;
        }
    }

    public final String display;
    public final Group group;
    public final ChatFormatting colour;
    public final Item token;
    public final String flavour;
    public final String statKind;
    public final int statAmount;
    public final String powerText;

    Ashen(String display, Group group, ChatFormatting colour, Item token,
            String flavour, String statKind, int statAmount, String powerText) {
        this.display = display;
        this.group = group;
        this.colour = colour;
        this.token = token;
        this.flavour = flavour;
        this.statKind = statKind;
        this.statAmount = statAmount;
        this.powerText = powerText;
    }

    public String key() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static Ashen byKey(String key) {
        if (key == null) return null;
        for (Ashen v : values()) {
            if (v.key().equals(key)) return v;
        }
        return null;
    }

    /** The framework's view of this identity. */
    public GearLine.Motif motif() {
        return new GearLine.Motif(key(), display, colour, token, flavour,
                group.display, group.colour, group.bonus,
                List.of(GearLine.stat(statKind, statAmount)),
                "The Crown fills at 16 hits.");
    }
}
