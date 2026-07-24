package com.political;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;

/**
 * V6.3 — every item the Cursed Spirits pillar can hand out or let you craft.
 *
 * <p>Two families:</p>
 * <ul>
 *   <li><b>Currency / materials</b> (drops only): Cursed Energy Shards &amp; Orbs redeem into your
 *       {@link CursedEnergyManager} balance from {@link CursedSpiritGui}; grade Residue, Cursed Cores
 *       and the Special Grade Relic are crafting reagents dropped by spirits.</li>
 *   <li><b>Gear</b> (craftable via {@link CursedRecipeBootstrap}): dressed vanilla weapons and charms
 *       whose stat lines are read by {@link PlayerStatsManager} (all values multiples of 5).</li>
 * </ul>
 *
 * <p>Every stack carries a custom_item_id (set LAST) so texture packs and handlers can key off it.</p>
 */
public final class CursedItems {

    private CursedItems() {}

    // Custom ids ---------------------------------------------------------------
    public static final String ID_SHARD        = "cursed_energy_shard";
    public static final String ID_ORB          = "cursed_energy_orb";
    public static final String ID_RESIDUE_LOW  = "cursed_residue_lesser";   // Grade 4/3 drops
    public static final String ID_CORE         = "cursed_core";             // Grade 2/1 drops
    public static final String ID_RELIC        = "special_grade_relic";     // Special Grade drop
    public static final String ID_BLADE        = "cursed_blade";
    public static final String ID_CHARM_HEART  = "reverse_cursed_charm";
    public static final String ID_TALISMAN     = "six_eyes_talisman";
    public static final String ID_AMULET       = "cursed_technique_amulet";

    /** Energy a Shard / Orb is worth when redeemed in the codex GUI. */
    public static final int SHARD_ENERGY = 5;
    public static final int ORB_ENERGY   = 25;

    // ── Helpers ───────────────────────────────────────────────────────────────
    private static ItemStack base(net.minecraft.world.item.Item item, String name,
                                  ChatFormatting... style) {
        ItemStack s = new ItemStack(item);
        s.set(DataComponents.CUSTOM_NAME, Component.literal(name).withStyle(style));
        return s;
    }

    private static void lore(ItemStack s, List<Component> lines) {
        s.set(DataComponents.LORE, new ItemLore(lines));
    }

    // ── Currency / material drops ─────────────────────────────────────────────
    public static ItemStack createShard() {
        ItemStack s = base(Items.AMETHYST_SHARD, "✦ Cursed Energy Shard",
                ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD);
        List<Component> l = new ArrayList<>();
        l.add(Component.literal("§8Condensed malice."));
        l.add(Component.literal("§7Redeem for §d" + SHARD_ENERGY + " Cursed Energy§7 in /curses."));
        l.add(Component.literal("§8Dropped by any Cursed Spirit."));
        lore(s, l);
        s.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        SlayerItems.setCustomItemId(s, ID_SHARD);
        return s;
    }

    public static ItemStack createOrb() {
        ItemStack s = base(Items.ENDER_EYE, "✦ Cursed Energy Orb",
                ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD);
        List<Component> l = new ArrayList<>();
        l.add(Component.literal("§8A whole grudge, wound tight."));
        l.add(Component.literal("§7Redeem for §d" + ORB_ENERGY + " Cursed Energy§7 in /curses."));
        l.add(Component.literal("§8Dropped by Grade 2 spirits and above."));
        lore(s, l);
        s.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        SlayerItems.setCustomItemId(s, ID_ORB);
        return s;
    }

    public static ItemStack createResidue() {
        ItemStack s = base(Items.GUNPOWDER, "Cursed Residue", ChatFormatting.GRAY);
        List<Component> l = new ArrayList<>();
        l.add(Component.literal("§8Soot left where a lesser curse died."));
        l.add(Component.literal("§7A reagent for cursed gear."));
        l.add(Component.literal("§8Dropped by Grade 4 & Grade 3 spirits."));
        lore(s, l);
        SlayerItems.setCustomItemId(s, ID_RESIDUE_LOW);
        return s;
    }

    public static ItemStack createCore() {
        ItemStack s = base(Items.ECHO_SHARD, "Cursed Core", ChatFormatting.AQUA, ChatFormatting.BOLD);
        List<Component> l = new ArrayList<>();
        l.add(Component.literal("§8The knotted heart of a named curse."));
        l.add(Component.literal("§7A reagent for high cursed gear."));
        l.add(Component.literal("§8Dropped by Grade 2 & Grade 1 spirits."));
        lore(s, l);
        s.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        SlayerItems.setCustomItemId(s, ID_CORE);
        return s;
    }

    public static ItemStack createRelic() {
        ItemStack s = base(Items.NETHER_STAR, "❈ Special Grade Relic",
                ChatFormatting.DARK_RED, ChatFormatting.BOLD);
        List<Component> l = new ArrayList<>();
        l.add(Component.literal("§8Torn from a curse that should not have died."));
        l.add(Component.literal("§7The rarest cursed reagent."));
        l.add(Component.literal("§8Dropped only by Special Grade spirits."));
        lore(s, l);
        s.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        SlayerItems.setCustomItemId(s, ID_RELIC);
        return s;
    }

    // ── Craftable gear ────────────────────────────────────────────────────────
    public static ItemStack createCursedBlade() {
        ItemStack s = base(Items.NETHERITE_SWORD, "☠ Cursed Blade",
                ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD);
        List<Component> l = new ArrayList<>();
        l.add(Component.literal("§8A sword sheathed in another's hatred."));
        l.add(Component.literal(""));
        l.add(Component.literal("§7⚔ Damage: §c+60"));
        l.add(Component.literal("§7❁ Strength: §c+40"));
        l.add(Component.literal("§7☠ Crit Damage: §9+30%"));
        l.add(Component.literal(""));
        l.add(Component.literal("§5「 Cursed Gear 」"));
        lore(s, l);
        s.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        SlayerItems.setCustomItemId(s, ID_BLADE);
        return s;
    }

    public static ItemStack createReverseCharm() {
        ItemStack s = base(Items.GHAST_TEAR, "❤ Reverse Cursed Charm",
                ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD);
        List<Component> l = new ArrayList<>();
        l.add(Component.literal("§8Positive energy turned outward as healing."));
        l.add(Component.literal(""));
        l.add(Component.literal("§7❤ Health: §c+25"));
        l.add(Component.literal("§7❈ Defense: §a+20"));
        l.add(Component.literal(""));
        l.add(Component.literal("§7§oHold in your off-hand or hotbar."));
        l.add(Component.literal("§5「 Cursed Gear 」"));
        lore(s, l);
        s.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        SlayerItems.setCustomItemId(s, ID_CHARM_HEART);
        return s;
    }

    public static ItemStack createCursedAmulet() {
        ItemStack s = base(Items.AMETHYST_CLUSTER, "✎ Cursed Technique Amulet",
                ChatFormatting.DARK_AQUA, ChatFormatting.BOLD);
        List<Component> l = new ArrayList<>();
        l.add(Component.literal("§8It hums with a borrowed technique."));
        l.add(Component.literal(""));
        l.add(Component.literal("§7✎ Intelligence: §b+30"));
        l.add(Component.literal("§7✦ Speed: §f+5"));
        l.add(Component.literal(""));
        l.add(Component.literal("§5「 Cursed Gear 」"));
        lore(s, l);
        s.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        SlayerItems.setCustomItemId(s, ID_AMULET);
        return s;
    }

    public static ItemStack createSixEyesTalisman() {
        ItemStack s = base(Items.ENDER_EYE, "❈ Six Eyes Talisman",
                ChatFormatting.AQUA, ChatFormatting.BOLD);
        List<Component> l = new ArrayList<>();
        l.add(Component.literal("§8Sight that unpicks the weave of a curse."));
        l.add(Component.literal(""));
        l.add(Component.literal("§7✎ Intelligence: §b+50"));
        l.add(Component.literal("§7☣ Crit Chance: §9+10%"));
        l.add(Component.literal("§7✦ Speed: §f+5"));
        l.add(Component.literal(""));
        l.add(Component.literal("§c「 Special Grade Gear 」"));
        lore(s, l);
        s.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        SlayerItems.setCustomItemId(s, ID_TALISMAN);
        return s;
    }

    /** True if the stack is one of our redeemable energy tokens; returns its energy value or 0. */
    public static int redeemValue(ItemStack stack) {
        String id = SlayerItems.getCustomItemId(stack);
        if (ID_SHARD.equals(id)) return SHARD_ENERGY;
        if (ID_ORB.equals(id)) return ORB_ENERGY;
        return 0;
    }
}
