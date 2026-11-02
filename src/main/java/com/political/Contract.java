package com.political;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/**
 * Contract templates. The board rolls a fresh set from these every rotation, so what is on offer
 * changes through the day without any of it being hand placed.
 */
public enum Contract {

    // ── Hunt ──────────────────────────────────────────────────────────────
    CULL_UNDEAD("Cull the Undead", Kind.HUNT, Items.ROTTEN_FLESH, ChatFormatting.DARK_GREEN,
            "The graveyards are restless again.", 25, 2500, 1),
    THIN_THE_SPIDERS("Thin the Spiders", Kind.HUNT, Items.STRING, ChatFormatting.GRAY,
            "Webs across every doorway in the low town.", 20, 2200, 1),
    CREEPER_CONTROL("Creeper Control", Kind.HUNT, Items.GUNPOWDER, ChatFormatting.GREEN,
            "Too many holes in the walls this month.", 15, 2400, 1),
    BONE_TITHE("Bone Tithe", Kind.HUNT, Items.BONE, ChatFormatting.WHITE,
            "The bonepickers pay well and ask nothing.", 30, 3000, 2),
    DEEP_PURGE("Deep Purge", Kind.HUNT, Items.NETHERITE_SCRAP, ChatFormatting.DARK_RED,
            "Something down there has grown bold.", 40, 6500, 3),
    CURSE_SWEEP("Curse Sweep", Kind.HUNT, Items.WITHER_SKELETON_SKULL, ChatFormatting.DARK_GRAY,
            "Grade four sightings, and climbing.", 12, 7000, 3),
    NIGHT_WATCH("Night Watch", Kind.HUNT, Items.TORCH, ChatFormatting.YELLOW,
            "Stand the wall until dawn and kill what comes.", 35, 4200, 2),
    APEX_BOUNTY("Apex Bounty", Kind.HUNT, Items.DIAMOND_SWORD, ChatFormatting.AQUA,
            "One beast. Bring back proof.", 5, 9000, 4),

    // ── Gather ────────────────────────────────────────────────────────────
    GRAIN_LEVY("Grain Levy", Kind.GATHER, Items.WHEAT, ChatFormatting.GOLD,
            "The granary is thin and winter is not.", 64, 1800, 1),
    TIMBER_ORDER("Timber Order", Kind.GATHER, Items.OAK_LOG, ChatFormatting.DARK_GREEN,
            "Scaffolding for the new quarter.", 96, 2000, 1),
    STONE_QUOTA("Stone Quota", Kind.GATHER, Items.COBBLESTONE, ChatFormatting.GRAY,
            "The road will not pave itself.", 128, 1900, 1),
    IRON_REQUISITION("Iron Requisition", Kind.GATHER, Items.IRON_INGOT, ChatFormatting.WHITE,
            "Every smith in the district is asking.", 32, 3200, 2),
    GOLD_TRIBUTE("Gold Tribute", Kind.GATHER, Items.GOLD_INGOT, ChatFormatting.YELLOW,
            "The treasury would like it quietly.", 24, 4000, 2),
    GEM_CONSIGNMENT("Gem Consignment", Kind.GATHER, Items.DIAMOND, ChatFormatting.AQUA,
            "A jeweller with more money than patience.", 12, 6000, 3),
    ARCANE_REAGENTS("Arcane Reagents", Kind.GATHER, Items.AMETHYST_SHARD, ChatFormatting.LIGHT_PURPLE,
            "The scholars burn through these faster than sense.", 20, 4500, 2),
    ANCIENT_SALVAGE("Ancient Salvage", Kind.GATHER, Items.ANCIENT_DEBRIS, ChatFormatting.DARK_RED,
            "Whatever the deep will give up.", 6, 9500, 4),

    // ── Deliver ───────────────────────────────────────────────────────────
    BREAD_RUN("Bread Run", Kind.DELIVER, Items.BREAD, ChatFormatting.GOLD,
            "Feed the outer row before they riot.", 32, 1600, 1),
    MEDICINE_RUN("Medicine Run", Kind.DELIVER, Items.GOLDEN_APPLE, ChatFormatting.RED,
            "The infirmary is out and people are waiting.", 8, 3800, 2),
    POWDER_RUN("Powder Run", Kind.DELIVER, Items.GUNPOWDER, ChatFormatting.DARK_GRAY,
            "Do not ask what the quarry needs it for.", 24, 2800, 2),
    RELIC_TRANSFER("Relic Transfer", Kind.DELIVER, Items.ECHO_SHARD, ChatFormatting.DARK_AQUA,
            "Carry it, do not open it.", 4, 7500, 3),

    // ── Explore ───────────────────────────────────────────────────────────
    MAP_THE_DEEP("Map the Deep", Kind.EXPLORE, Items.MAP, ChatFormatting.AQUA,
            "Go down until it stops going down.", 1, 3500, 2),
    CHART_THE_WASTES("Chart the Wastes", Kind.EXPLORE, Items.COMPASS, ChatFormatting.GOLD,
            "Walk out past where the roads end.", 1, 3200, 2),
    DELVE_EXPEDITION("Delve Commission", Kind.EXPLORE, Items.ENDER_EYE, ChatFormatting.DARK_PURPLE,
            "Clear an expedition and bring back what you saw.", 1, 8000, 3),
    BACKROOMS_SURVEY("Deep Cave Survey", Kind.EXPLORE, Items.YELLOW_CONCRETE, ChatFormatting.YELLOW,
            "Map a cave system and come back with a report.", 1, 8500, 4),

    // ── Craft ─────────────────────────────────────────────────────────────
    SMITHS_BACKLOG("Smith's Backlog", Kind.CRAFT, Items.ANVIL, ChatFormatting.GRAY,
            "Orders stacked to the rafters.", 10, 2600, 1),
    ARMOURY_ORDER("Armoury Order", Kind.CRAFT, Items.IRON_CHESTPLATE, ChatFormatting.WHITE,
            "The watch needs kit and needs it now.", 6, 3400, 2),
    ENCHANTERS_FAVOUR("Enchanter's Favour", Kind.CRAFT, Items.ENCHANTED_BOOK, ChatFormatting.LIGHT_PURPLE,
            "Something with a little power in it.", 4, 5200, 3),

    // ── Civic ─────────────────────────────────────────────────────────────
    KEEP_THE_PEACE("Keep the Peace", Kind.CIVIC, Items.SHIELD, ChatFormatting.BLUE,
            "Be seen, be armed, be dull.", 20, 3000, 2),
    TAX_COLLECTION("Tax Collection", Kind.CIVIC, Items.GOLD_NUGGET, ChatFormatting.YELLOW,
            "Nobody enjoys this one.", 15, 3600, 2),
    FESTIVAL_PREP("Festival Preparation", Kind.CIVIC, Items.FIREWORK_ROCKET, ChatFormatting.LIGHT_PURPLE,
            "The season turns and the square is bare.", 24, 2900, 1),
    WALL_REPAIR("Wall Repair", Kind.CIVIC, Items.STONE_BRICKS, ChatFormatting.GRAY,
            "Before something notices the gap.", 64, 2400, 1);

    public enum Kind {
        HUNT("Hunt", ChatFormatting.RED),
        GATHER("Gather", ChatFormatting.GREEN),
        DELIVER("Deliver", ChatFormatting.GOLD),
        EXPLORE("Explore", ChatFormatting.AQUA),
        CRAFT("Craft", ChatFormatting.LIGHT_PURPLE),
        CIVIC("Civic", ChatFormatting.BLUE);

        public final String label;
        public final ChatFormatting colour;

        Kind(String label, ChatFormatting colour) {
            this.label = label;
            this.colour = colour;
        }
    }

    public final String display;
    public final Kind kind;
    public final Item icon;
    public final ChatFormatting colour;
    public final String flavor;
    /** How much of the thing is required. */
    public final int required;
    /** Coin payout on completion. */
    public final long reward;
    /** 1 easy, 4 hard. Drives cursed energy payout and the time limit. */
    public final int difficulty;

    Contract(String display, Kind kind, Item icon, ChatFormatting colour, String flavor,
             int required, long reward, int difficulty) {
        this.display = display;
        this.kind = kind;
        this.icon = icon;
        this.colour = colour;
        this.flavor = flavor;
        this.required = required;
        this.reward = reward;
        this.difficulty = difficulty;
    }

    public String id() {
        return name().toLowerCase(java.util.Locale.ROOT);
    }

    /** Cursed energy paid alongside the coins, scaled to difficulty. */
    public int energyReward() {
        return difficulty * 5;
    }

    public static Contract byId(String id) {
        if (id == null) return null;
        for (Contract c : values()) {
            if (c.id().equalsIgnoreCase(id) || c.name().equalsIgnoreCase(id)) return c;
        }
        return null;
    }
}
