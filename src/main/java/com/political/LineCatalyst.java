package com.political;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;

/**
 * The nine line catalysts.
 *
 * <p>Every Version X gear line is keyed on one custom material rather than on raw vanilla loot. The
 * catalyst is what makes the line a line: you craft the catalyst once from a crafting core, then
 * every piece in that family wants the catalyst plus a core of its own tier. That keeps the whole
 * expansion inside the existing crafting economy instead of sitting beside it, and it means the
 * cores finally have somewhere to go at the top end.</p>
 *
 * <p>Each catalyst is itself craftable and shows up in {@code /recipe} and {@code /setrecipe} the
 * same as anything else.</p>
 */
public enum LineCatalyst {

    STAR_SHARD("vx_star_shard", "Star Shard", ChatFormatting.YELLOW, Items.PRISMARINE_CRYSTALS,
            "A piece of a constellation that came loose.",
            "Keys the Starbound line.", Tier.T2,
            new Item[]{Items.AMETHYST_SHARD, Items.GLOWSTONE_DUST, Items.ENDER_EYE, Items.GOLD_INGOT}),

    PRIMAL_HEART("vx_primal_heart", "Primal Heart", ChatFormatting.RED, Items.HEART_OF_THE_SEA,
            "Still warm, which is the worrying part.",
            "Keys the Beastcraft line.", Tier.T2,
            new Item[]{Items.BONE_BLOCK, Items.LEATHER, Items.ROTTEN_FLESH, Items.NETHERITE_SCRAP}),

    CLOCKWORK_ASSEMBLY("vx_clockwork_assembly", "Clockwork Assembly", ChatFormatting.GRAY, Items.OBSERVER,
            "Forty parts, none of which understand the job.",
            "Keys the Clockwork line.", Tier.T2,
            new Item[]{Items.IRON_INGOT, Items.REDSTONE_BLOCK, Items.COPPER_INGOT, Items.PISTON}),

    BOUND_PAGE("vx_bound_page", "Bound Page", ChatFormatting.LIGHT_PURPLE, Items.PAPER,
            "Torn out of something that objected.",
            "Keys the Grimoire line.", Tier.T2,
            new Item[]{Items.LAPIS_BLOCK, Items.GLOWSTONE, Items.ENCHANTED_BOOK, Items.AMETHYST_SHARD}),

    STORM_NUCLEUS("vx_storm_nucleus", "Storm Nucleus", ChatFormatting.AQUA, Items.LIGHTNING_ROD,
            "The quiet middle of something very loud.",
            "Keys the Tempest line.", Tier.T2,
            new Item[]{Items.COPPER_BLOCK, Items.PRISMARINE_SHARD, Items.BLUE_ICE, Items.BLAZE_POWDER}),

    SURVEYORS_SEAL("vx_surveyors_seal", "Surveyor's Seal", ChatFormatting.GREEN, Items.COMPASS,
            "Stamped by somebody who walked the whole thing.",
            "Keys the Voyager line.", Tier.T2,
            new Item[]{Items.FILLED_MAP, Items.ENDER_PEARL, Items.GOLD_INGOT, Items.SPYGLASS}),

    RUNE_INGOT("vx_rune_ingot", "Runed Ingot", ChatFormatting.AQUA, Items.NETHERITE_SCRAP,
            "The mark was struck while it was still soft.",
            "Keys the Runeforge line.", Tier.T2,
            new Item[]{Items.LAPIS_BLOCK, Items.AMETHYST_SHARD, Items.IRON_BLOCK, Items.DIAMOND}),

    AETHER_MOTE("vx_aether_mote", "Aether Mote", ChatFormatting.LIGHT_PURPLE, Items.AMETHYST_SHARD,
            "It has not decided which element it is.",
            "Keys the Aether Arsenal line.", Tier.T3,
            new Item[]{Items.AMETHYST_BLOCK, Items.NETHERITE_INGOT, Items.END_CRYSTAL, Items.NETHER_STAR}),

    SEAL_OF_OFFICE("vx_seal_of_office", "Seal of Office", ChatFormatting.GOLD, Items.HONEYCOMB,
            "Pressed once, and the argument was over.",
            "Keys the Regalia line.", Tier.T3,
            new Item[]{Items.GOLD_BLOCK, Items.EMERALD_BLOCK, Items.WRITABLE_BOOK, Items.NETHERITE_INGOT});

    /** Which crafting core a recipe reaches for. */
    public enum Tier {
        T1, T2, T3;

        public ItemStack core() {
            return switch (this) {
                case T1 -> SlayerItems.createCustomCrafterCoreT1();
                case T2 -> SlayerItems.createCustomCrafterCoreT2();
                case T3 -> SlayerItems.createCustomCrafterCoreT3();
            };
        }

        public String label() {
            return switch (this) {
                case T1 -> "Custom Crafting Core (T1)";
                case T2 -> "Custom Crafting Core (T2)";
                case T3 -> "Custom Crafting Core (T3)";
            };
        }
    }

    public final String id;
    public final String display;
    public final ChatFormatting colour;
    public final Item base;
    public final String flavor;
    public final String purpose;
    public final Tier tier;
    public final Item[] mats;

    LineCatalyst(String id, String display, ChatFormatting colour, Item base, String flavor,
                 String purpose, Tier tier, Item[] mats) {
        this.id = id;
        this.display = display;
        this.colour = colour;
        this.base = base;
        this.flavor = flavor;
        this.purpose = purpose;
        this.tier = tier;
        this.mats = mats;
    }

    public ItemStack create() {
        return create(1);
    }

    public ItemStack create(int count) {
        ItemStack s = new ItemStack(base, count);
        s.set(DataComponents.CUSTOM_NAME,
                Component.literal("✦ " + display).withStyle(colour, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("✦ Crafting Component")
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        lore.add(Component.literal("§7" + purpose));
        lore.add(Component.literal("§8\"" + flavor + "\""));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Craft it from a " + tier.label() + "."));
        lore.add(Component.literal("§8LEGENDARY CATALYST").withStyle(ChatFormatting.GOLD));
        s.set(DataComponents.LORE, new ItemLore(lore));
        s.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        SlayerItems.setCustomItemId(s, id);
        return s;
    }

    /**
     * A core in the middle, the line's four signature materials around it. Yields four, which is
     * exactly what one piece of gear asks for.
     */
    public void registerRecipe(List<SlayerRecipes.Recipe> out) {
        ItemStack[] grid = new ItemStack[9];
        grid[0] = new ItemStack(mats[0], 8);
        grid[1] = new ItemStack(mats[1], 8);
        grid[2] = new ItemStack(mats[0], 8);
        grid[3] = new ItemStack(mats[2], 8);
        grid[4] = tier.core();
        grid[5] = new ItemStack(mats[2], 8);
        grid[6] = new ItemStack(mats[3], 4);
        grid[7] = new ItemStack(mats[1], 8);
        grid[8] = new ItemStack(mats[3], 4);

        boolean[] locked = new boolean[9];
        for (int i = 0; i < 9; i++) locked[i] = true;

        out.add(new SlayerRecipes.Recipe(
                "VX Catalyst " + display, create(4), grid, locked, null, 0));
    }

    public static void ensureRecipes() {
        List<SlayerRecipes.Recipe> out = new ArrayList<>();
        for (LineCatalyst c : values()) c.registerRecipe(out);
        RecipeConfigManager.upsertCustomRecipesSilent(out);
    }

    public static LineCatalyst byId(String id) {
        if (id == null) return null;
        for (LineCatalyst c : values()) {
            if (c.id.equalsIgnoreCase(id)) return c;
        }
        return null;
    }

    public static List<ItemStack> allStacks() {
        List<ItemStack> out = new ArrayList<>();
        for (LineCatalyst c : values()) out.add(c.create());
        return out;
    }
}
