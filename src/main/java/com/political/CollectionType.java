package com.political;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.Locale;

/**
 * The sixty collections.
 *
 * <p>A collection counts one material for the rest of a player's life on the server and pays out at
 * nine thresholds. It is the long tail of the mod: the reason to keep breaking wheat at hour four
 * hundred, and the only progression here that cannot be bought, traded or handed over. Every tier
 * grants coins and a small permanent stat bonus, so a maxed collection is worth roughly one good
 * armour piece and takes considerably longer to earn.</p>
 *
 * <p>Thresholds come from the band rather than being written out per collection, because the
 * interesting differences between collections are the material, the flavour and the stat they feed,
 * not nine hand tuned numbers each.</p>
 */
public enum CollectionType {

    // ── Farming: the slow, enormous numbers ───────────────────────────────
    WHEAT("Wheat", Field.FARMING, ChatFormatting.GOLD, Items.WHEAT, Band.COMMON, "health",
            "Four hundred hours of the same swing."),
    CARROT("Carrot", Field.FARMING, ChatFormatting.GOLD, Items.CARROT, Band.COMMON, "health",
            "Orange, underground, and quietly load bearing."),
    POTATO("Potato", Field.FARMING, ChatFormatting.YELLOW, Items.POTATO, Band.COMMON, "defense",
            "It has saved more countries than any army."),
    MELON("Melon", Field.FARMING, ChatFormatting.GREEN, Items.MELON_SLICE, Band.COMMON, "health",
            "Mostly water and entirely worth it."),
    PUMPKIN("Pumpkin", Field.FARMING, ChatFormatting.GOLD, Items.PUMPKIN, Band.UNCOMMON, "defense",
            "Grown for one night a year and eaten all of them."),
    SUGAR_CANE("Sugar Cane", Field.FARMING, ChatFormatting.GREEN, Items.SUGAR_CANE, Band.COMMON, "speed",
            "It only grows where it is inconvenient."),
    COCOA("Cocoa", Field.FARMING, ChatFormatting.GOLD, Items.COCOA_BEANS, Band.UNCOMMON, "intelligence",
            "An entire economy grew out of a pod."),
    MUSHROOM("Mushroom", Field.FARMING, ChatFormatting.RED, Items.RED_MUSHROOM, Band.UNCOMMON, "intelligence",
            "Neither plant nor animal and smug about it."),
    NETHER_WART("Nether Wart", Field.FARMING, ChatFormatting.DARK_RED, Items.NETHER_WART, Band.RARE, "intelligence",
            "Farmed by people who chose to live there."),
    CACTUS("Cactus", Field.FARMING, ChatFormatting.DARK_GREEN, Items.CACTUS, Band.UNCOMMON, "defense",
            "It asks only that you keep your distance."),

    // ── Mining: the reason anyone owns a pickaxe ──────────────────────────
    COAL("Coal", Field.MINING, ChatFormatting.DARK_GRAY, Items.COAL, Band.COMMON, "defense",
            "The first thing anybody finds and the last thing they respect."),
    IRON("Iron", Field.MINING, ChatFormatting.WHITE, Items.IRON_INGOT, Band.COMMON, "defense",
            "Everything is built out of it, including the tools that find it."),
    GOLD("Gold", Field.MINING, ChatFormatting.GOLD, Items.GOLD_INGOT, Band.UNCOMMON, "intelligence",
            "Soft, useless and the reason for most of history."),
    REDSTONE("Redstone", Field.MINING, ChatFormatting.RED, Items.REDSTONE, Band.COMMON, "intelligence",
            "It wants to be a machine and will settle for a door."),
    LAPIS("Lapis", Field.MINING, ChatFormatting.BLUE, Items.LAPIS_LAZULI, Band.UNCOMMON, "intelligence",
            "Ground up for a colour long before it was ground up for a spell."),
    DIAMOND("Diamond", Field.MINING, ChatFormatting.AQUA, Items.DIAMOND, Band.RARE, "crit_damage",
            "Carbon, arranged smugly."),
    EMERALD("Emerald", Field.MINING, ChatFormatting.GREEN, Items.EMERALD, Band.RARE, "intelligence",
            "Worth more as a promise than as a stone."),
    QUARTZ("Quartz", Field.MINING, ChatFormatting.WHITE, Items.QUARTZ, Band.UNCOMMON, "crit_chance",
            "The nether's one contribution to interior decorating."),
    OBSIDIAN("Obsidian", Field.MINING, ChatFormatting.DARK_PURPLE, Items.OBSIDIAN, Band.RARE, "defense",
            "Made by an argument between lava and water."),
    ANCIENT_DEBRIS("Ancient Debris", Field.MINING, ChatFormatting.DARK_RED, Items.ANCIENT_DEBRIS,
            Band.LEGENDARY, "strength", "Older than the dimension it is buried in."),

    // ── Combat: the drops nobody farms on purpose until they do ───────────
    ROTTEN_FLESH("Rotten Flesh", Field.COMBAT, ChatFormatting.DARK_GREEN, Items.ROTTEN_FLESH,
            Band.COMMON, "health", "Nobody has ever been pleased to receive this."),
    BONE("Bone", Field.COMBAT, ChatFormatting.WHITE, Items.BONE, Band.COMMON, "strength",
            "It was doing a job before you got hold of it."),
    STRING("String", Field.COMBAT, ChatFormatting.WHITE, Items.STRING, Band.COMMON, "speed",
            "Harvested from something that was busy."),
    SPIDER_EYE("Spider Eye", Field.COMBAT, ChatFormatting.DARK_RED, Items.SPIDER_EYE, Band.UNCOMMON,
            "crit_chance", "Eight of them, and it still did not see you coming."),
    GUNPOWDER("Gunpowder", Field.COMBAT, ChatFormatting.GRAY, Items.GUNPOWDER, Band.UNCOMMON,
            "ferocity", "The last thing a creeper contributes to society."),
    ENDER_PEARL("Ender Pearl", Field.COMBAT, ChatFormatting.DARK_AQUA, Items.ENDER_PEARL, Band.RARE,
            "intelligence", "It is still trying to go somewhere."),
    BLAZE_ROD("Blaze Rod", Field.COMBAT, ChatFormatting.GOLD, Items.BLAZE_ROD, Band.RARE, "strength",
            "Warm, and it stays warm for a worrying length of time."),
    GHAST_TEAR("Ghast Tear", Field.COMBAT, ChatFormatting.WHITE, Items.GHAST_TEAR, Band.RARE,
            "health", "Something enormous was very sad about this."),
    SLIME("Slime", Field.COMBAT, ChatFormatting.GREEN, Items.SLIME_BALL, Band.UNCOMMON, "defense",
            "It divides when threatened, which is a strategy."),
    WITHER_SKULL("Wither Skull", Field.COMBAT, ChatFormatting.DARK_GRAY, Items.WITHER_SKELETON_SKULL,
            Band.LEGENDARY, "crit_damage", "Three of these and you have made a mistake."),

    // ── Foraging: wood, and the pretending that it is simple ──────────────
    OAK("Oak", Field.FORAGING, ChatFormatting.GOLD, Items.OAK_LOG, Band.COMMON, "health",
            "The default tree, and it knows it."),
    BIRCH("Birch", Field.FORAGING, ChatFormatting.WHITE, Items.BIRCH_LOG, Band.COMMON, "defense",
            "Pale, straight and slightly smug about both."),
    SPRUCE("Spruce", Field.FORAGING, ChatFormatting.DARK_GREEN, Items.SPRUCE_LOG, Band.COMMON,
            "defense", "It grows where nothing else agreed to."),
    JUNGLE("Jungle", Field.FORAGING, ChatFormatting.GREEN, Items.JUNGLE_LOG, Band.UNCOMMON, "speed",
            "Enormous, and inconveniently far from everything."),
    ACACIA("Acacia", Field.FORAGING, ChatFormatting.GOLD, Items.ACACIA_LOG, Band.UNCOMMON, "strength",
            "It grew sideways on purpose."),
    DARK_OAK("Dark Oak", Field.FORAGING, ChatFormatting.DARK_GRAY, Items.DARK_OAK_LOG, Band.UNCOMMON,
            "defense", "Four saplings, or nothing at all."),
    MANGROVE("Mangrove", Field.FORAGING, ChatFormatting.DARK_RED, Items.MANGROVE_LOG, Band.RARE,
            "health", "It builds the ground it stands on."),
    CHERRY("Cherry", Field.FORAGING, ChatFormatting.LIGHT_PURPLE, Items.CHERRY_LOG, Band.RARE,
            "intelligence", "Beautiful for a fortnight and structural forever."),
    BAMBOO("Bamboo", Field.FORAGING, ChatFormatting.GREEN, Items.BAMBOO, Band.COMMON, "speed",
            "It grows faster than anybody is comfortable with."),
    CRIMSON("Crimson", Field.FORAGING, ChatFormatting.RED, Items.CRIMSON_STEM, Band.RARE, "strength",
            "A forest that never agreed to be one."),

    // ── Fishing: patience, mostly ─────────────────────────────────────────
    COD("Cod", Field.FISHING, ChatFormatting.AQUA, Items.COD, Band.COMMON, "health",
            "It built and then destroyed several economies."),
    SALMON("Salmon", Field.FISHING, ChatFormatting.RED, Items.SALMON, Band.COMMON, "strength",
            "It swims uphill out of principle."),
    TROPICAL_FISH("Tropical Fish", Field.FISHING, ChatFormatting.YELLOW, Items.TROPICAL_FISH,
            Band.UNCOMMON, "speed", "Too pretty to eat and everybody has tried."),
    PUFFERFISH("Pufferfish", Field.FISHING, ChatFormatting.GOLD, Items.PUFFERFISH, Band.UNCOMMON,
            "defense", "Its entire plan is to become inconvenient."),
    PRISMARINE("Prismarine", Field.FISHING, ChatFormatting.DARK_AQUA, Items.PRISMARINE_SHARD,
            Band.RARE, "intelligence", "Somebody built with it before anybody could breathe down there."),
    INK("Ink", Field.FISHING, ChatFormatting.DARK_GRAY, Items.INK_SAC, Band.COMMON, "crit_chance",
            "A defence mechanism that became a stationery industry."),
    NAUTILUS("Nautilus Shell", Field.FISHING, ChatFormatting.WHITE, Items.NAUTILUS_SHELL, Band.RARE,
            "intelligence", "The maths in it is older than the maths about it."),
    KELP("Kelp", Field.FISHING, ChatFormatting.DARK_GREEN, Items.KELP, Band.COMMON, "health",
            "An entire country nobody has mapped."),
    LILY_PAD("Lily Pad", Field.FISHING, ChatFormatting.GREEN, Items.LILY_PAD, Band.UNCOMMON, "speed",
            "You can stand on it if you commit."),
    HEART_OF_THE_SEA("Heart of the Sea", Field.FISHING, ChatFormatting.AQUA, Items.HEART_OF_THE_SEA,
            Band.LEGENDARY, "intelligence", "The sea kept one and somebody found it."),

    // ── Arcane: the materials the mod invented reasons for ────────────────
    AMETHYST("Amethyst", Field.ARCANE, ChatFormatting.LIGHT_PURPLE, Items.AMETHYST_SHARD, Band.UNCOMMON,
            "intelligence", "It grows in the dark and rings when struck."),
    ECHO("Echo Shard", Field.ARCANE, ChatFormatting.DARK_AQUA, Items.ECHO_SHARD, Band.RARE,
            "intelligence", "It kept a sound nobody meant to leave behind."),
    GLOWSTONE("Glowstone", Field.ARCANE, ChatFormatting.YELLOW, Items.GLOWSTONE_DUST, Band.COMMON,
            "intelligence", "Light, sold by the handful."),
    SCULK("Sculk", Field.ARCANE, ChatFormatting.DARK_BLUE, Items.SCULK, Band.RARE, "crit_damage",
            "It is going somewhere, slowly, and it is winning."),
    NETHER_STAR("Nether Star", Field.ARCANE, ChatFormatting.WHITE, Items.NETHER_STAR, Band.LEGENDARY,
            "strength", "Nothing gives one of these up willingly."),
    DRAGON_BREATH("Dragon Breath", Field.ARCANE, ChatFormatting.DARK_PURPLE, Items.DRAGON_BREATH,
            Band.LEGENDARY, "crit_damage", "Bottled, which was somebody's very brave idea."),
    SHULKER("Shulker Shell", Field.ARCANE, ChatFormatting.LIGHT_PURPLE, Items.SHULKER_SHELL, Band.RARE,
            "defense", "It closed and it is not opening for you."),
    PHANTOM("Phantom Membrane", Field.ARCANE, ChatFormatting.DARK_GRAY, Items.PHANTOM_MEMBRANE,
            Band.UNCOMMON, "speed", "Proof that not sleeping has consequences."),
    HONEY("Honey", Field.ARCANE, ChatFormatting.GOLD, Items.HONEYCOMB, Band.UNCOMMON, "health",
            "Made by a committee that never argues."),
    COPPER("Copper", Field.ARCANE, ChatFormatting.GOLD, Items.COPPER_INGOT, Band.COMMON, "crit_chance",
            "It changes colour to tell you how long you have been away.");

    /** The six fields a collection can belong to. */
    public enum Field {
        FARMING("Farming", ChatFormatting.GOLD, Items.GOLDEN_HOE),
        MINING("Mining", ChatFormatting.AQUA, Items.DIAMOND_PICKAXE),
        COMBAT("Combat", ChatFormatting.RED, Items.NETHERITE_SWORD),
        FORAGING("Foraging", ChatFormatting.DARK_GREEN, Items.OAK_LOG),
        FISHING("Fishing", ChatFormatting.BLUE, Items.FISHING_ROD),
        ARCANE("Arcane", ChatFormatting.LIGHT_PURPLE, Items.AMETHYST_CLUSTER);

        public final String label;
        public final ChatFormatting colour;
        public final Item icon;

        Field(String label, ChatFormatting colour, Item icon) {
            this.label = label;
            this.colour = colour;
            this.icon = icon;
        }
    }

    /**
     * How common the material is. The band picks the nine thresholds, so a Nether Star collection
     * does not ask for twenty five thousand of them.
     */
    public enum Band {
        COMMON(new int[]{50, 250, 1000, 2500, 5000, 10000, 25000, 50000, 100000}, 1),
        UNCOMMON(new int[]{25, 100, 500, 1000, 2500, 5000, 10000, 25000, 50000}, 2),
        RARE(new int[]{10, 50, 150, 400, 1000, 2500, 5000, 10000, 20000}, 4),
        LEGENDARY(new int[]{5, 15, 40, 100, 250, 500, 1000, 2500, 5000}, 10);

        public final int[] thresholds;
        /** Multiplies the coin payout, so rarer work pays better per unit. */
        public final int payMultiplier;

        Band(int[] thresholds, int payMultiplier) {
            this.thresholds = thresholds;
            this.payMultiplier = payMultiplier;
        }
    }

    public static final int MAX_TIER = 9;

    public final String display;
    public final Field field;
    public final ChatFormatting colour;
    public final Item material;
    public final Band band;
    /** Which stat this collection feeds. Values are multiples of five. */
    public final String statKey;
    public final String flavor;

    CollectionType(String display, Field field, ChatFormatting colour, Item material, Band band,
               String statKey, String flavor) {
        this.display = display;
        this.field = field;
        this.colour = colour;
        this.material = material;
        this.band = band;
        this.statKey = statKey;
        this.flavor = flavor;
    }

    public String key() {
        return name().toLowerCase(Locale.ROOT);
    }

    /** How many of the material tier n asks for. Tier is one based. */
    public int thresholdFor(int tier) {
        if (tier < 1 || tier > MAX_TIER) return Integer.MAX_VALUE;
        return band.thresholds[tier - 1];
    }

    /** The tier a given count has reached, zero through nine. */
    public int tierFor(long count) {
        int tier = 0;
        for (int i = 0; i < MAX_TIER; i++) {
            if (count >= band.thresholds[i]) tier = i + 1;
            else break;
        }
        return tier;
    }

    /** Coins paid on reaching a tier. */
    public long payoutFor(int tier) {
        return (long) tier * tier * 250L * band.payMultiplier;
    }

    /** The permanent stat bonus a given tier is worth. Five per tier, so forty five at the top. */
    public int statBonusFor(int tier) {
        return Math.max(0, tier) * 5;
    }

    public static CollectionType byKey(String key) {
        if (key == null) return null;
        for (CollectionType c : values()) {
            if (c.key().equalsIgnoreCase(key)) return c;
        }
        return null;
    }

    /** Every collection in one field, in declaration order. */
    public static java.util.List<CollectionType> inField(Field f) {
        java.util.List<CollectionType> out = new java.util.ArrayList<>();
        for (CollectionType c : values()) {
            if (c.field == f) out.add(c);
        }
        return out;
    }
}
