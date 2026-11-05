package com.political;


import com.political.bounty.SlayerItems;
import com.political.items.ItemStatInjector;
import com.political.stats.PlayerStatsManager;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.equipment.Equippable;

import java.util.ArrayList;
import java.util.List;

/**
 * Shortened lore: same rules for catalog GUIs and for custom gear at creation time
 * ({@link SlayerItems#setCustomItemId} applies this in-place after the id is set).
 */
public final class CustomItemLore {

    /** Compact rows for GUI slot icons (double chest grids). */
    public static final int CATALOG_MAX_LINES = 6;
    public static final int CATALOG_MAX_LINE_CHARS = 44;

    /** Slightly roomier caps for real item stacks (still summarized vs. original wall-of-text). */
    public static final int ITEM_LORE_MAX_LINES = 12;
    public static final int ITEM_LORE_MAX_LINE_CHARS = 52;

    private CustomItemLore() {}

    /**
     * Compress lore on the stack in place (skip blank and pure-separator lines; cap lines / width).
     * No-op if empty or no lore.
     */
    public static void applySummarizedLore(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        ItemLore lore = stack.get(DataComponents.LORE);
        List<Component> base;
        if (lore == null || lore.lines().isEmpty()) {
            base = new ArrayList<>();
        } else {
            base = summarizeLines(lore.lines(), ITEM_LORE_MAX_LINES, ITEM_LORE_MAX_LINE_CHARS);
        }
        // Inject deterministic stat lines (⚔/❈/❤/❁/☣/☠) so every custom item feeds the
        // V3 PlayerStatsManager. No-op if the stack has no custom id or already carries stats.
        List<Component> withStats = ItemStatInjector.appendStats(stack, base);
        if (!withStats.isEmpty()) {
            stack.set(DataComponents.LORE, new ItemLore(withStats));
        }
    }

    /** Display copy with trimmed lore; preserves count, name, attributes, enchantments, etc. */
    public static ItemStack forCatalogDisplay(ItemStack source) {
        if (source == null || source.isEmpty()) {
            return source;
        }
        ItemStack copy = source.copy();
        ItemLore lore = copy.get(DataComponents.LORE);
        if (lore == null || lore.lines().isEmpty()) {
            return copy;
        }
        copy.set(DataComponents.LORE, new ItemLore(summarizeLinesForCatalog(lore.lines())));
        return copy;
    }

    /**
     * Category/select pages: full item for icon (components, glint, color) with short rewritten lore.
     * Armor pieces get a fresh “what it does” digest from ability/stat lines; other items use catalog limits.
     */
    public static ItemStack forSelectPageIcon(ItemStack source) {
        if (source == null || source.isEmpty()) {
            return source;
        }
        ItemStack copy = source.copy();
        ItemLore lore = copy.get(DataComponents.LORE);
        if (lore == null || lore.lines().isEmpty()) {
            return copy;
        }
        if (isEquippableArmor(copy)) {
            copy.set(DataComponents.LORE, new ItemLore(buildArmorGameplaySummary(lore.lines())));
        } else {
            copy.set(DataComponents.LORE, new ItemLore(summarizeLinesForCatalog(lore.lines())));
        }
        return copy;
    }

    private static boolean isEquippableArmor(ItemStack stack) {
        Equippable e = stack.get(DataComponents.EQUIPPABLE);
        if (e == null) {
            return false;
        }
        EquipmentSlot s = e.slot();
        return s == EquipmentSlot.HEAD || s == EquipmentSlot.CHEST
                || s == EquipmentSlot.LEGS || s == EquipmentSlot.FEET;
    }

    private static List<Component> buildArmorGameplaySummary(List<Component> original) {
        List<Component> out = new ArrayList<>();
        out.add(Component.literal("What it does").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        List<String> picked = new ArrayList<>();
        for (Component line : original) {
            String s = line.getString().replace('\n', ' ').trim();
            if (s.isEmpty() || isSeparatorOnly(s)) {
                continue;
            }
            if (s.contains("§8") && s.length() < 30) {
                continue;
            }
            boolean statOrBonus = s.contains("Armor")
                    || s.contains("🛡")
                    || s.contains("Toughness")
                    || s.contains("✦")
                    || s.contains("SET")
                    || s.contains("Set bonus")
                    || s.contains("ABILITY")
                    || s.contains("Ability")
                    || s.contains("•")
                    || s.contains("Durability");
            if (statOrBonus) {
                String t = s.length() > CATALOG_MAX_LINE_CHARS ? s.substring(0, CATALOG_MAX_LINE_CHARS - 1) + "…" : s;
                picked.add(t);
            }
            if (picked.size() >= 5) {
                break;
            }
        }
        if (picked.isEmpty()) {
            return summarizeLinesForCatalog(original);
        }
        for (String p : picked) {
            out.add(Component.literal(p).withStyle(ChatFormatting.GRAY));
        }
        return out;
    }

    static List<Component> summarizeLines(List<Component> original, int maxLines, int maxLineChars) {
        List<Component> out = new ArrayList<>();
        for (Component line : original) {
            String s = line.getString().replace('\n', ' ').trim();
            if (s.isEmpty()) continue;
            if (isSeparatorOnly(s)) continue;
            if (out.size() >= maxLines) {
                out.add(Component.literal("…").withStyle(ChatFormatting.DARK_GRAY));
                return out;
            }
            // a line that fits and needs no trimming keeps its own parts and colours (a white label with a coloured value stays that way)
            boolean unchanged = s.length() <= maxLineChars && s.equals(line.getString());
            if (s.length() > maxLineChars) {
                s = s.substring(0, maxLineChars - 1) + "…";
            }
            out.add(unchanged ? line : Component.literal(s).withStyle(line.getStyle()));
        }
        return out;
    }

    /** Catalog / GUI copy: tighter limits than {@link #applySummarizedLore}. */
    static List<Component> summarizeLinesForCatalog(List<Component> original) {
        return summarizeLines(original, CATALOG_MAX_LINES, CATALOG_MAX_LINE_CHARS);
    }

    private static boolean isSeparatorOnly(String s) {
        if (s.length() > 24) return false;
        String t = s.replace("━", "").replace("─", "").replace("—", "").trim();
        return t.isEmpty();
    }
}
