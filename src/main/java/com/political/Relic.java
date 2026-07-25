package com.political;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/**
 * Relics. Each one is a single hand written effect rather than a stat stick, and only the ones in
 * your accessory bag are live, so carrying relics is a choice about what kind of fighter you are.
 *
 * <p>Effects are grouped by when they fire. {@link RelicManager} routes each group to the right
 * place, so a relic only has to declare what it is and how strong it is.</p>
 */
public enum Relic {

    // ── Combat, fire on hit ───────────────────────────────────────────────
    EMBER_CORE("Ember Core", Items.FIRE_CHARGE, ChatFormatting.GOLD, Trigger.ON_HIT, Family.COMBAT,
            "Your strikes set the air alight.", 15),
    THUNDER_SEED("Thunder Seed", Items.LIGHTNING_ROD, ChatFormatting.YELLOW, Trigger.ON_HIT, Family.COMBAT,
            "Every so often a strike calls the storm down.", 8),
    VENOM_SAC("Venom Sac", Items.SPIDER_EYE, ChatFormatting.DARK_GREEN, Trigger.ON_HIT, Family.COMBAT,
            "Wounds you open refuse to close cleanly.", 20),
    FROST_SHARD("Frost Shard", Items.BLUE_ICE, ChatFormatting.AQUA, Trigger.ON_HIT, Family.COMBAT,
            "What you hit slows, and keeps slowing.", 20),
    RIPPER_FANG("Ripper Fang", Items.NETHERITE_SCRAP, ChatFormatting.RED, Trigger.ON_HIT, Family.COMBAT,
            "A second bite lands a moment after the first.", 12),
    SUNDERING_NAIL("Sundering Nail", Items.IRON_NUGGET, ChatFormatting.GRAY, Trigger.ON_HIT, Family.COMBAT,
            "Armour matters a little less to you.", 18),
    CINDER_BRAND("Cinder Brand", Items.MAGMA_CREAM, ChatFormatting.RED, Trigger.ON_HIT, Family.COMBAT,
            "You mark your target, and the mark burns.", 14),

    // ── Combat, fire on kill ──────────────────────────────────────────────
    SOUL_LANTERN_RELIC("Soul Lantern", Items.SOUL_LANTERN, ChatFormatting.DARK_AQUA, Trigger.ON_KILL, Family.CURSED,
            "The dead give up a little of what they carried.", 100),
    GLUTTON_HEART("Glutton Heart", Items.ROTTEN_FLESH, ChatFormatting.DARK_RED, Trigger.ON_KILL, Family.CURSED,
            "Each kill knits your wounds shut.", 15),
    TROPHY_CHAIN("Trophy Chain", Items.IRON_BARS, ChatFormatting.GOLD, Trigger.ON_KILL, Family.FORTUNE,
            "You are paid for your work, quietly.", 40),
    MOMENTUM_COIL("Momentum Coil", Items.COPPER_INGOT, ChatFormatting.GOLD, Trigger.ON_KILL, Family.MOBILITY,
            "A kill puts wind behind you.", 60),
    WRAITH_CALL("Wraith Call", Items.WITHER_SKELETON_SKULL, ChatFormatting.DARK_GRAY, Trigger.ON_KILL, Family.CURSED,
            "Something answers when you kill. Briefly.", 10),

    // ── Survival, fire when you are hurt ──────────────────────────────────
    THORN_HEART("Thorn Heart", Items.CACTUS, ChatFormatting.DARK_GREEN, Trigger.ON_HURT, Family.SURVIVAL,
            "Striking you costs the striker.", 25),
    LAST_BREATH("Last Breath", Items.TOTEM_OF_UNDYING, ChatFormatting.GOLD, Trigger.ON_HURT, Family.SURVIVAL,
            "When it is nearly over, you get one moment more.", 30),
    STONE_SKIN("Stone Skin", Items.STONE, ChatFormatting.GRAY, Trigger.ON_HURT, Family.SURVIVAL,
            "A hard hit leaves you harder for a while.", 20),
    MIRROR_SCALE("Mirror Scale", Items.PRISMARINE_SHARD, ChatFormatting.AQUA, Trigger.ON_HURT, Family.SURVIVAL,
            "Some of what is done to you is sent back.", 15),
    EMBER_WARD("Ember Ward", Items.BLAZE_POWDER, ChatFormatting.GOLD, Trigger.ON_HURT, Family.SURVIVAL,
            "Fire that touches you feeds you instead.", 100),
    PANIC_STEP("Panic Step", Items.FEATHER, ChatFormatting.WHITE, Trigger.ON_HURT, Family.MOBILITY,
            "Pain makes you quick.", 40),

    // ── Passive auras ─────────────────────────────────────────────────────
    HEARTHSTONE("Hearthstone", Items.CAMPFIRE, ChatFormatting.GOLD, Trigger.PASSIVE, Family.SURVIVAL,
            "You mend slowly, always.", 1),
    WANDERER_BOOT("Wanderer's Sole", Items.LEATHER_BOOTS, ChatFormatting.GREEN, Trigger.PASSIVE, Family.MOBILITY,
            "The road is kinder to you.", 1),
    DEEP_LUNG("Deep Lung", Items.PUFFERFISH, ChatFormatting.BLUE, Trigger.PASSIVE, Family.SURVIVAL,
            "Water is no longer a clock.", 1),
    NIGHT_EYE("Night Eye", Items.ENDER_EYE, ChatFormatting.DARK_PURPLE, Trigger.PASSIVE, Family.ARCANE,
            "The dark holds nothing from you.", 1),
    MINERS_LAMP("Miner's Lamp", Items.LANTERN, ChatFormatting.YELLOW, Trigger.PASSIVE, Family.FORTUNE,
            "Stone yields faster under your hands.", 1),
    ARCANE_BATTERY("Arcane Battery", Items.AMETHYST_CLUSTER, ChatFormatting.LIGHT_PURPLE, Trigger.PASSIVE, Family.ARCANE,
            "You hold more power than your frame should allow.", 1),
    SILENT_TREAD("Silent Tread", Items.WHITE_WOOL, ChatFormatting.GRAY, Trigger.PASSIVE, Family.MOBILITY,
            "Nothing hears you coming.", 1),
    IRON_STOMACH("Iron Stomach", Items.BREAD, ChatFormatting.GOLD, Trigger.PASSIVE, Family.SURVIVAL,
            "Hunger comes for you more slowly.", 1),
    CURSED_MARROW("Cursed Marrow", Items.BONE, ChatFormatting.DARK_GRAY, Trigger.PASSIVE, Family.CURSED,
            "Cursed energy pools in you on its own.", 1),
    LUCKY_TOOTH("Lucky Tooth", Items.RABBIT_FOOT, ChatFormatting.GREEN, Trigger.PASSIVE, Family.FORTUNE,
            "Things fall your way a little more often.", 1),
    SCHOLARS_LENS("Scholar's Lens", Items.SPYGLASS, ChatFormatting.AQUA, Trigger.PASSIVE, Family.ARCANE,
            "You learn faster than you should.", 1),
    WARDING_SIGIL("Warding Sigil", Items.SHIELD, ChatFormatting.BLUE, Trigger.PASSIVE, Family.SURVIVAL,
            "A thin ward sits over your skin.", 1),
    TIDE_CHARM("Tide Charm", Items.NAUTILUS_SHELL, ChatFormatting.AQUA, Trigger.PASSIVE, Family.MOBILITY,
            "Water carries you rather than holding you.", 1),
    EMBERWALK("Emberwalk", Items.NETHERRACK, ChatFormatting.RED, Trigger.PASSIVE, Family.SURVIVAL,
            "Heat treats you as one of its own.", 1),
    GRAVE_TOKEN("Grave Token", Items.SKELETON_SKULL, ChatFormatting.DARK_GRAY, Trigger.PASSIVE, Family.CURSED,
            "The restless dead look past you.", 1),
    MERCHANTS_RING("Merchant's Ring", Items.GOLD_NUGGET, ChatFormatting.YELLOW, Trigger.PASSIVE, Family.FORTUNE,
            "Coin finds you in greater number.", 1),
    STORM_CHARM("Storm Charm", Items.COPPER_BLOCK, ChatFormatting.YELLOW, Trigger.PASSIVE, Family.ARCANE,
            "The sky is on your side.", 1),
    VERDANT_SEED("Verdant Seed", Items.WHEAT_SEEDS, ChatFormatting.GREEN, Trigger.PASSIVE, Family.FORTUNE,
            "Green things grow eager around you.", 1),
    BULWARK_CREST("Bulwark Crest", Items.NETHERITE_INGOT, ChatFormatting.DARK_GRAY, Trigger.PASSIVE, Family.SURVIVAL,
            "You are simply harder to move.", 1),
    SEERS_COIN("Seer's Coin", Items.CLOCK, ChatFormatting.LIGHT_PURPLE, Trigger.PASSIVE, Family.ARCANE,
            "You feel the shape of what is coming.", 1),
    BLOOD_PACT_CHARM("Blood Pact Charm", Items.REDSTONE, ChatFormatting.DARK_RED, Trigger.PASSIVE, Family.CURSED,
            "You trade a little life for a lot of force.", 1),
    FEATHER_FALL("Featherfall", Items.PHANTOM_MEMBRANE, ChatFormatting.WHITE, Trigger.PASSIVE, Family.MOBILITY,
            "The ground is gentler than it should be.", 1),
    ECHO_PENDANT("Echo Pendant", Items.ECHO_SHARD, ChatFormatting.DARK_AQUA, Trigger.PASSIVE, Family.ARCANE,
            "Your techniques cool faster.", 1),
    GOLEM_CORE("Golem Core", Items.IRON_BLOCK, ChatFormatting.GRAY, Trigger.PASSIVE, Family.SURVIVAL,
            "You carry more of yourself into a fight.", 1),
    HUNTERS_MARK("Hunter's Mark", Items.BOW, ChatFormatting.GREEN, Trigger.PASSIVE, Family.COMBAT,
            "Beasts fall to you more readily.", 1),
    SPELUNKERS_ROPE("Spelunker's Rope", Items.LEAD, ChatFormatting.GOLD, Trigger.PASSIVE, Family.FORTUNE,
            "The deep gives up its ores to you.", 1),
    WARLORD_BANNER("Warlord's Banner", Items.WHITE_BANNER, ChatFormatting.RED, Trigger.PASSIVE, Family.COMBAT,
            "You fight harder when your realm is at war.", 1);

    /** When the relic does its work. */
    public enum Trigger { ON_HIT, ON_KILL, ON_HURT, PASSIVE }

    /** Loose theme, used for grouping in the bag. */
    public enum Family {
        COMBAT("Combat", ChatFormatting.RED),
        SURVIVAL("Survival", ChatFormatting.GREEN),
        MOBILITY("Mobility", ChatFormatting.AQUA),
        FORTUNE("Fortune", ChatFormatting.GOLD),
        ARCANE("Arcane", ChatFormatting.LIGHT_PURPLE),
        CURSED("Cursed", ChatFormatting.DARK_GRAY);

        public final String label;
        public final ChatFormatting colour;

        Family(String label, ChatFormatting colour) {
            this.label = label;
            this.colour = colour;
        }
    }

    public final String display;
    public final Item icon;
    public final ChatFormatting colour;
    public final Trigger trigger;
    public final Family family;
    public final String flavor;
    /** Meaning depends on the trigger: a percent chance, a magnitude, or a flat amount. */
    public final int power;

    Relic(String display, Item icon, ChatFormatting colour, Trigger trigger, Family family,
          String flavor, int power) {
        this.display = display;
        this.icon = icon;
        this.colour = colour;
        this.trigger = trigger;
        this.family = family;
        this.flavor = flavor;
        this.power = power;
    }

    public String id() {
        return "relic_" + name().toLowerCase(java.util.Locale.ROOT);
    }

    public static Relic byId(String id) {
        if (id == null) return null;
        for (Relic r : values()) {
            if (r.id().equalsIgnoreCase(id) || r.name().equalsIgnoreCase(id)) return r;
        }
        return null;
    }
}
