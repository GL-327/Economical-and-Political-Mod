package com.political;

import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.Locale;

/**
 * The drop table: forty four things that cannot be crafted and only fall off something.
 *
 * <p>Everything else in the mod has a recipe. These do not, deliberately. A drop only item is the
 * one kind of reward that cannot be planned for, bought or worked toward directly, and a server
 * needs a few of those or every item becomes a shopping list. The chance is printed on the item and
 * in the bestiary, so it is a known long shot rather than a mystery.</p>
 *
 * <p>Announcements follow the tier. A common find tells the finder, a rare one tells everybody
 * nearby, and the two top tiers tell the whole server, which is the part that makes somebody else's
 * luck feel like an event rather than a statistic.</p>
 */
public enum RareDrop {

    // ── Undead ────────────────────────────────────────────────────────────
    ROTTED_HEART("Rotted Heart", ChatFormatting.DARK_GREEN, Items.ROTTEN_FLESH,
            EntityTypes.ZOMBIE, 0.012, Tier.RARE,
            "It is still beating, which is the worrying part.",
            "Feeds the Rotten Flesh collection by a hundred."),
    GRAVE_DUST("Grave Dust", ChatFormatting.GRAY, Items.GUNPOWDER,
            EntityTypes.ZOMBIE, 0.02, Tier.RARE,
            "Swept from somewhere nobody visits.", "A reagent the board pays well for."),
    PALE_MARROW("Pale Marrow", ChatFormatting.WHITE, Items.BONE_MEAL,
            EntityTypes.SKELETON, 0.012, Tier.RARE,
            "Taken from the middle of something that had no middle left.",
            "Feeds the Bone collection by a hundred."),
    ARCHERS_EYE("Archer's Eye", ChatFormatting.AQUA, Items.ENDER_EYE,
            EntityTypes.SKELETON, 0.004, Tier.CRAZY,
            "It was aiming at something a long way off.", "Worth a small fortune to a collector."),
    STRAY_FROSTHEART("Stray's Frostheart", ChatFormatting.AQUA, Items.BLUE_ICE,
            EntityTypes.STRAY, 0.008, Tier.CRAZY,
            "It never thawed and it is not going to.", "A cold reagent for cold work."),
    HUSK_SALT("Husk Salt", ChatFormatting.YELLOW, Items.GUNPOWDER,
            EntityTypes.HUSK, 0.015, Tier.RARE,
            "Dried out somewhere with no shade at all.", "Preserves anything it is packed with."),
    DROWNED_LUNG("Drowned Lung", ChatFormatting.BLUE, Items.SPONGE,
            EntityTypes.DROWNED, 0.01, Tier.RARE,
            "It has not needed air for a very long time.", "Sells high to anybody building deep."),
    WITHERED_CROWN("Withered Crown", ChatFormatting.DARK_GRAY, Items.GOLDEN_HELMET,
            EntityTypes.WITHER_SKELETON, 0.003, Tier.CRAZY,
            "It ruled a corridor, briefly.", "One of the rarer things in the fortress."),
    SOULBOUND_ASH("Soulbound Ash", ChatFormatting.DARK_PURPLE, Items.DYE.black(),
            EntityTypes.WITHER_SKELETON, 0.0012, Tier.INSANE,
            "It refuses to scatter.", "The board treats this as a Great Work in a bottle."),

    // ── Arthropods ────────────────────────────────────────────────────────
    SILK_GLAND("Silk Gland", ChatFormatting.WHITE, Items.STRING,
            EntityTypes.SPIDER, 0.015, Tier.RARE,
            "Still full, still working.", "Feeds the String collection by a hundred."),
    VENOM_SAC("Venom Sac", ChatFormatting.DARK_GREEN, Items.SPIDER_EYE,
            EntityTypes.CAVE_SPIDER, 0.01, Tier.RARE,
            "Sealed, thankfully.", "A reagent nobody handles casually."),
    BROODMOTHER_FANG("Broodmother's Fang", ChatFormatting.DARK_RED, Items.NETHERITE_SCRAP,
            EntityTypes.SPIDER, 0.002, Tier.CRAZY,
            "Considerably larger than the spider it came from.",
            "Somebody bigger was in that cave."),
    CHITIN_PLATE("Chitin Plate", ChatFormatting.DARK_GREEN, Items.SHIELD,
            EntityTypes.CAVE_SPIDER, 0.006, Tier.CRAZY,
            "Lighter than steel and nearly as stubborn.", "Armourers will pay for this."),

    // ── Explosive and slime ───────────────────────────────────────────────
    UNSPENT_CHARGE("Unspent Charge", ChatFormatting.GREEN, Items.TNT,
            EntityTypes.CREEPER, 0.008, Tier.CRAZY,
            "It did not go off. It has not stopped intending to.",
            "Miners pay a premium and handle it carefully."),
    CREEPER_HEARTSTONE("Creeper Heartstone", ChatFormatting.GREEN, Items.EMERALD,
            EntityTypes.CREEPER, 0.0015, Tier.INSANE,
            "The quiet centre of a very loud animal.", "Almost nobody has seen one."),
    PRIME_SLIME("Prime Slime", ChatFormatting.GREEN, Items.SLIME_BLOCK,
            EntityTypes.SLIME, 0.01, Tier.RARE,
            "The one the others divided from.", "Bounces considerably better than it should."),
    MAGMA_CORE("Magma Core", ChatFormatting.GOLD, Items.MAGMA_BLOCK,
            EntityTypes.MAGMA_CUBE, 0.008, Tier.CRAZY,
            "Still liquid somewhere in the middle.", "A forge reagent worth going after."),

    // ── The nether ────────────────────────────────────────────────────────
    BLAZE_HEART("Blaze Heart", ChatFormatting.GOLD, Items.BLAZE_POWDER,
            EntityTypes.BLAZE, 0.006, Tier.CRAZY,
            "It kept burning after everything else stopped.",
            "The reagent every brewer eventually needs."),
    GHASTLY_TEARDUCT("Ghastly Tearduct", ChatFormatting.WHITE, Items.GHAST_TEAR,
            EntityTypes.GHAST, 0.005, Tier.CRAZY,
            "Something enormous was inconsolable.", "Rarer than the tears themselves."),
    PIGLIN_SIGNET("Piglin Signet", ChatFormatting.GOLD, Items.GOLD_INGOT,
            EntityTypes.PIGLIN_BRUTE, 0.004, Tier.CRAZY,
            "It meant something to somebody.", "Bastion currency, apparently."),
    HOGLIN_TUSK("Hoglin Tusk", ChatFormatting.RED, Items.NETHERITE_SCRAP,
            EntityTypes.HOGLIN, 0.008, Tier.CRAZY,
            "It was used, repeatedly, on purpose.", "Weaponsmiths know what to do with it."),
    ZOGLIN_SPITE("Zoglin Spite", ChatFormatting.DARK_RED, Items.WITHER_ROSE,
            EntityTypes.ZOGLIN, 0.003, Tier.CRAZY,
            "Whatever was left after it stopped being alive.", "It has not calmed down."),

    // ── The end ───────────────────────────────────────────────────────────
    ENDER_FRAGMENT("Ender Fragment", ChatFormatting.DARK_PURPLE, Items.ENDER_PEARL,
            EntityTypes.ENDERMAN, 0.01, Tier.RARE,
            "A piece of somewhere else.", "Feeds the Ender Pearl collection by a hundred."),
    VOIDGLASS_SHARD("Voidglass Shard", ChatFormatting.DARK_PURPLE, Items.AMETHYST_SHARD,
            EntityTypes.ENDERMAN, 0.0025, Tier.CRAZY,
            "You can see through it and there is nothing on the far side.",
            "One of the few things worth farming endermen for."),
    SHULKER_CORE("Shulker Core", ChatFormatting.LIGHT_PURPLE, Items.SHULKER_SHELL,
            EntityTypes.SHULKER, 0.006, Tier.CRAZY,
            "The part that decides where things go.", "Storage builders will empty a bank for it."),
    ENDERMITE_HUSK("Endermite Husk", ChatFormatting.DARK_PURPLE, Items.PHANTOM_MEMBRANE,
            EntityTypes.ENDERMITE, 0.02, Tier.RARE,
            "Small, and it came a very long way.", "A curiosity that sells anyway."),

    // ── The overworld's stranger residents ────────────────────────────────
    PHANTOM_SIGH("Phantom's Sigh", ChatFormatting.DARK_GRAY, Items.PHANTOM_MEMBRANE,
            EntityTypes.PHANTOM, 0.01, Tier.RARE,
            "Proof that not sleeping has consequences.", "Alchemists ask no questions."),
    WITCHS_LEDGER("Witch's Ledger", ChatFormatting.DARK_GREEN, Items.WRITTEN_BOOK,
            EntityTypes.WITCH, 0.006, Tier.CRAZY,
            "Everything she brewed, in a hand nobody can read.",
            "Worth a great deal to the right buyer."),
    GUARDIAN_LENS("Guardian's Lens", ChatFormatting.AQUA, Items.PRISMARINE_CRYSTALS,
            EntityTypes.GUARDIAN, 0.008, Tier.CRAZY,
            "It was focusing on something.", "It still focuses, faintly."),
    ELDER_MANDATE("Elder's Mandate", ChatFormatting.DARK_AQUA, Items.HEART_OF_THE_SEA,
            EntityTypes.ELDER_GUARDIAN, 0.05, Tier.INSANE,
            "The instruction the others were following.", "Only one thing carries this."),
    RAVAGER_HORN("Ravager Horn", ChatFormatting.DARK_RED, Items.GOAT_HORN,
            EntityTypes.RAVAGER, 0.03, Tier.CRAZY,
            "It was audible from the next valley.", "Worth the fight it took."),
    EVOKER_SIGIL("Evoker's Sigil", ChatFormatting.LIGHT_PURPLE, Items.TOTEM_OF_UNDYING,
            EntityTypes.EVOKER, 0.02, Tier.CRAZY,
            "The shape he was drawing when it ended.", "It still wants to finish."),
    PILLAGER_ORDERS("Pillager Orders", ChatFormatting.GRAY, Items.PAPER,
            EntityTypes.PILLAGER, 0.012, Tier.RARE,
            "Somebody higher up wrote this.", "Somebody higher up still exists."),
    VINDICATOR_EDGE("Vindicator's Edge", ChatFormatting.WHITE, Items.IRON_AXE,
            EntityTypes.VINDICATOR, 0.008, Tier.CRAZY,
            "Kept sharper than anything in a raid needs.", "Smiths recognise the work."),
    WARDEN_ECHO("Warden's Echo", ChatFormatting.DARK_AQUA, Items.ECHO_SHARD,
            EntityTypes.WARDEN, 0.35, Tier.INSANE,
            "It heard you. It is still hearing you.",
            "Almost nobody survives long enough to collect one."),

    // ── Animals, for the people who farm them ─────────────────────────────
    GOLDEN_FLEECE("Golden Fleece", ChatFormatting.GOLD, Items.GOLD_BLOCK,
            EntityTypes.SHEEP, 0.001, Tier.INSANE,
            "One sheep in a thousand, and nobody knows which.",
            "A story people will not believe."),
    PRIZE_HIDE("Prize Hide", ChatFormatting.GOLD, Items.LEATHER,
            EntityTypes.COW, 0.004, Tier.RARE,
            "Flawless, which is rarer than it sounds.", "Tanners bid against each other."),
    LUCKY_FOOT("Lucky Foot", ChatFormatting.LIGHT_PURPLE, Items.RABBIT_FOOT,
            EntityTypes.RABBIT, 0.003, Tier.CRAZY,
            "It was not lucky for the rabbit.", "It might be for you."),
    HONEYED_COMB("Honeyed Comb", ChatFormatting.GOLD, Items.HONEYCOMB,
            EntityTypes.BEE, 0.01, Tier.RARE,
            "Denser and darker than the rest of the hive.", "Worth several ordinary combs."),
    TURTLE_ELDER_SCUTE("Elder Scute", ChatFormatting.DARK_GREEN, Items.TURTLE_SCUTE,
            EntityTypes.TURTLE, 0.006, Tier.CRAZY,
            "It has been growing for longer than the beach has existed.",
            "Armourers know exactly what this is."),
    POLAR_CLAW("Polar Claw", ChatFormatting.WHITE, Items.NETHERITE_SCRAP,
            EntityTypes.POLAR_BEAR, 0.007, Tier.CRAZY,
            "It was not defending anything at the time.", "That is the concerning part."),
    DOLPHIN_PEARL("Dolphin's Pearl", ChatFormatting.AQUA, Items.NAUTILUS_SHELL,
            EntityTypes.DOLPHIN, 0.005, Tier.CRAZY,
            "It was carrying this and it was not sharing.", "Now you are carrying it."),
    AXOLOTL_TEAR("Axolotl Tear", ChatFormatting.LIGHT_PURPLE, Items.GLOW_INK_SAC,
            EntityTypes.AXOLOTL, 0.004, Tier.CRAZY,
            "Regret, from something that regrows everything else.",
            "Alchemists consider it close to priceless."),
    ALLAY_CHIME("Allay Chime", ChatFormatting.AQUA, Items.AMETHYST_SHARD,
            EntityTypes.ALLAY, 0.02, Tier.INSANE,
            "It hums the note it was listening for.",
            "Nobody has worked out how to make another.");

    /** How loudly a find is announced. */
    public enum Tier {
        RARE("RARE DROP!", ChatFormatting.YELLOW, Reach.FINDER),
        CRAZY("CRAZY RARE DROP!", ChatFormatting.GOLD, Reach.NEARBY),
        INSANE("INSANE DROP!", ChatFormatting.LIGHT_PURPLE, Reach.SERVER);

        public enum Reach { FINDER, NEARBY, SERVER }

        public final String shout;
        public final ChatFormatting colour;
        public final Reach reach;

        Tier(String shout, ChatFormatting colour, Reach reach) {
            this.shout = shout;
            this.colour = colour;
            this.reach = reach;
        }
    }

    public final String display;
    public final ChatFormatting colour;
    public final Item base;
    public final EntityType<?> source;
    /** Chance per kill, before any luck the player has earned. */
    public final double chance;
    public final Tier tier;
    public final String flavor;
    public final String use;

    RareDrop(String display, ChatFormatting colour, Item base, EntityType<?> source,
             double chance, Tier tier, String flavor, String use) {
        this.display = display;
        this.colour = colour;
        this.base = base;
        this.source = source;
        this.chance = chance;
        this.tier = tier;
        this.flavor = flavor;
        this.use = use;
    }

    public String key() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String itemId() {
        return "drop_" + key();
    }

    /** The chance as a percentage string, for the tooltip and the bestiary. */
    public String chanceText() {
        if (chance >= 0.01) return String.format("%.1f%%", chance * 100);
        return String.format("%.2f%%", chance * 100);
    }

    public static RareDrop byKey(String key) {
        if (key == null) return null;
        for (RareDrop d : values()) {
            if (d.key().equalsIgnoreCase(key)) return d;
        }
        return null;
    }
}
