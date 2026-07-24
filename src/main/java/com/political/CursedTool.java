package com.political;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * V6.6 — Graded cursed tools (weapons), Grade 4 up to Special Grade.
 *
 * <p>Every tool carries a stat line (all multiples of 5) that feeds the existing
 * {@link PlayerStatsManager}/{@link CustomItemHandler} lore-stat pipeline, plus a signature
 * on-hit effect enforced in {@link SorcererManager}'s damage hook (gated by custom item id).</p>
 *
 * <p>The four lower grades are craftable (see {@link CursedToolFactory#ensureRecipes()}). The
 * Special Grade tool is <b>not</b> craftable — it is claimed once, for free, the moment a player
 * actually reaches Special Grade (see {@link SorcererGui}).</p>
 */
public enum CursedTool {

    GRADE_4("cursed_tool_g4", "Cursed Blade", ChatFormatting.GRAY, Items.IRON_SWORD, true,
            new String[]{"§7⚔ Damage: §c+30", "§7❁ Strength: §c+15", "§7☠ Crit Damage: §9+20%"},
            "§7On hit: 20% chance to inflict §8Wither§7."),

    GRADE_3("cursed_tool_g3", "Slaughter Demon", ChatFormatting.WHITE, Items.DIAMOND_SWORD, true,
            new String[]{"§7⚔ Damage: §c+55", "§7❁ Strength: §c+30", "§7☠ Crit Damage: §9+25%", "§7⫽ Ferocity: §c+5"},
            "§7On hit: §4Bleed §7— deals bonus true damage."),

    GRADE_2("cursed_tool_g2", "Split-Soul Katana", ChatFormatting.YELLOW, Items.NETHERITE_SWORD, true,
            new String[]{"§7⚔ Damage: §c+90", "§7❁ Strength: §c+45", "§7☠ Crit Damage: §9+35%", "§7⫽ Ferocity: §c+5"},
            "§7On hit: 25% chance to §esoul-split §7for an extra strike."),

    GRADE_1("cursed_tool_g1", "Playful Cloud", ChatFormatting.GOLD, Items.NETHERITE_SWORD, true,
            new String[]{"§7⚔ Damage: §c+130", "§7❁ Strength: §c+60", "§7☠ Crit Damage: §9+45%",
                    "§7⫽ Ferocity: §c+10", "§7☣ Crit Chance: §9+10%"},
            "§7On hit: §6shockwave §7— knock back and wither nearby foes."),

    SPECIAL_GRADE("cursed_tool_special", "Kamutoke, the Divine Slash", ChatFormatting.LIGHT_PURPLE, Items.NETHERITE_SWORD, false,
            new String[]{"§7⚔ Damage: §c+180", "§7❁ Strength: §c+75", "§7☠ Crit Damage: §9+55%",
                    "§7⫽ Ferocity: §c+15", "§7☣ Crit Chance: §9+15%"},
            "§7On hit: §dDismantle §7— true damage and a cleave that strikes every nearby enemy.");

    public final String id;
    public final String display;
    public final ChatFormatting color;
    public final Item base;
    public final boolean craftable;
    public final String[] statLore;
    public final String signature;

    CursedTool(String id, String display, ChatFormatting color, Item base, boolean craftable,
               String[] statLore, String signature) {
        this.id = id;
        this.display = display;
        this.color = color;
        this.base = base;
        this.craftable = craftable;
        this.statLore = statLore;
        this.signature = signature;
    }

    public static CursedTool byId(String id) {
        if (id == null) return null;
        String canon = SlayerItems.stripSkyblockEnchantSuffix(id);
        for (CursedTool t : values()) if (t.id.equals(canon)) return t;
        return null;
    }

    /** Returns the cursed tool a stack represents, or {@code null}. */
    public static CursedTool of(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        return byId(SlayerItems.getCustomItemId(stack));
    }
}
