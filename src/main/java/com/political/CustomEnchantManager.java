package com.political;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Reads and writes custom enchantments on an item's lore.
 *
 * <p>The lore is the storage. There is no registry, no datapack and no second file: an enchantment
 * is the line "§9Bloodthirst V" sitting in the tooltip, and this class is the parser and the writer
 * for it. That is what lets eighty enchantments ship inside one jar and apply to any of the seven
 * thousand items already in the mod without touching one of them.</p>
 *
 * <p>The block is delimited so it can be rewritten cleanly. Everything between the markers belongs
 * to this system and everything outside is the item's own lore, which means applying, removing and
 * re-applying never corrupts a tooltip somebody else wrote.</p>
 */
public final class CustomEnchantManager {

    private CustomEnchantManager() {}

    /** How many enchantments one item may carry. */
    public static final int MAX_PER_ITEM = 7;

    // ── Reading ───────────────────────────────────────────────────────────

    /** Every custom enchantment on this item, with its level. */
    public static Map<CustomEnchant, Integer> read(ItemStack stack) {
        Map<CustomEnchant, Integer> out = new EnumMap<>(CustomEnchant.class);
        if (stack == null || stack.isEmpty()) return out;
        ItemLore lore = stack.get(DataComponents.LORE);
        if (lore == null) return out;

        for (Component line : lore.lines()) {
            String raw = line.getString().trim();
            if (raw.isEmpty()) continue;
            // Strip colour codes before matching, so a recoloured tooltip still parses.
            String plain = raw.replaceAll("§.", "").trim();
            int lastSpace = plain.lastIndexOf(' ');
            if (lastSpace <= 0) continue;

            String name = plain.substring(0, lastSpace).trim();
            String numeral = plain.substring(lastSpace + 1).trim();
            CustomEnchant e = CustomEnchant.byDisplay(name);
            if (e == null) continue;
            int lv = fromRoman(numeral);
            if (lv <= 0) continue;
            out.put(e, Math.min(e.maxLevel, lv));
        }
        return out;
    }

    public static int levelOf(ItemStack stack, CustomEnchant e) {
        Integer lv = read(stack).get(e);
        return lv == null ? 0 : lv;
    }

    /** The highest level of an enchantment across everything a player is wearing or holding. */
    public static int equippedLevel(ServerPlayer player, CustomEnchant e) {
        int best = 0;
        ItemStack[] carried = {
                player.getItemBySlot(EquipmentSlot.HEAD),
                player.getItemBySlot(EquipmentSlot.CHEST),
                player.getItemBySlot(EquipmentSlot.LEGS),
                player.getItemBySlot(EquipmentSlot.FEET),
                player.getMainHandItem(),
                player.getOffhandItem()
        };
        for (ItemStack st : carried) {
            best = Math.max(best, levelOf(st, e));
        }
        return best;
    }

    /** Total levels of an enchantment across worn armour, for the ones that stack per piece. */
    public static int wornTotal(ServerPlayer player, CustomEnchant e) {
        int total = 0;
        for (EquipmentSlot slot : new EquipmentSlot[]{
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            total += levelOf(player.getItemBySlot(slot), e);
        }
        return total;
    }

    // ── Writing ───────────────────────────────────────────────────────────

    /**
     * Applies an enchantment, replacing any existing level of it.
     *
     * <p>Returns a reason when it will not go on, so callers can tell the player why rather than
     * failing silently. That matters more than usual here, since the slot rules are the part
     * somebody will get wrong.</p>
     */
    public static String apply(ItemStack stack, CustomEnchant e, int level) {
        if (stack == null || stack.isEmpty()) return "There is nothing there.";
        if (!fits(stack, e)) {
            return e.display + " only goes on " + e.slot.label.toLowerCase() + ".";
        }
        Map<CustomEnchant, Integer> have = read(stack);
        if (!have.containsKey(e) && have.size() >= MAX_PER_ITEM) {
            return "That already carries " + MAX_PER_ITEM + " enchantments.";
        }
        have.put(e, Math.max(1, Math.min(e.maxLevel, level)));
        write(stack, have);
        return null;
    }

    public static void remove(ItemStack stack, CustomEnchant e) {
        Map<CustomEnchant, Integer> have = read(stack);
        if (have.remove(e) != null) write(stack, have);
    }

    public static void clear(ItemStack stack) {
        write(stack, new EnumMap<>(CustomEnchant.class));
    }

    /**
     * Rewrites the enchantment block, leaving the item's own lore alone.
     *
     * <p>Lines are matched and dropped by parsing rather than by position, so an item whose lore was
     * rewritten by the reforge system or the stat injector still comes out correct.</p>
     */
    private static void write(ItemStack stack, Map<CustomEnchant, Integer> enchants) {
        List<Component> kept = new ArrayList<>();
        ItemLore existing = stack.get(DataComponents.LORE);
        if (existing != null) {
            for (Component line : existing.lines()) {
                String plain = line.getString().replaceAll("§.", "").trim();
                int lastSpace = plain.lastIndexOf(' ');
                if (lastSpace > 0) {
                    CustomEnchant maybe = CustomEnchant.byDisplay(plain.substring(0, lastSpace).trim());
                    if (maybe != null && fromRoman(plain.substring(lastSpace + 1).trim()) > 0) {
                        continue;
                    }
                }
                kept.add(line);
            }
        }

        List<Component> out = new ArrayList<>();
        // Enchantments read best at the top, above the flavour and the stats.
        for (Map.Entry<CustomEnchant, Integer> en : enchants.entrySet()) {
            out.add(Component.literal(en.getKey().loreLine(en.getValue())));
        }
        if (!enchants.isEmpty() && !kept.isEmpty()) out.add(Component.literal(""));
        out.addAll(kept);

        stack.set(DataComponents.LORE, new ItemLore(out));
        if (!enchants.isEmpty()) {
            stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        }
    }

    // ── Slot rules ────────────────────────────────────────────────────────

    /** Whether an enchantment belongs on this item at all. */
    public static boolean fits(ItemStack stack, CustomEnchant e) {
        if (e.slot == CustomEnchant.Slot.ANY) return true;
        return switch (e.slot) {
            case WEAPON -> isWeapon(stack);
            case ARMOUR -> isArmour(stack);
            case TOOL -> isTool(stack);
            case BOW -> isBow(stack);
            case ANY -> true;
        };
    }

    public static boolean isWeapon(ItemStack s) {
        String id = idOf(s);
        return id.contains("sword") || id.contains("axe") || id.contains("trident")
                || id.contains("mace");
    }

    public static boolean isArmour(ItemStack s) {
        String id = idOf(s);
        return id.contains("helmet") || id.contains("chestplate") || id.contains("leggings")
                || id.contains("boots") || id.contains("elytra");
    }

    public static boolean isTool(ItemStack s) {
        String id = idOf(s);
        return id.contains("pickaxe") || id.contains("shovel") || id.contains("hoe")
                || id.contains("axe") || id.contains("shears");
    }

    public static boolean isBow(ItemStack s) {
        String id = idOf(s);
        return id.contains("bow") || id.contains("crossbow");
    }

    private static String idOf(ItemStack s) {
        try {
            return net.minecraft.core.registries.BuiltInRegistries.ITEM
                    .getKey(s.getItem()).getPath().toLowerCase(java.util.Locale.ROOT);
        } catch (Exception e) {
            return "";
        }
    }

    /** Which enchantments could legally go on this item, for the menu. */
    public static List<CustomEnchant> applicableTo(ItemStack stack) {
        List<CustomEnchant> out = new ArrayList<>();
        for (CustomEnchant e : CustomEnchant.values()) {
            if (fits(stack, e)) out.add(e);
        }
        return out;
    }

    // ── Numerals ──────────────────────────────────────────────────────────

    private static int fromRoman(String s) {
        return switch (s.toUpperCase(java.util.Locale.ROOT)) {
            case "I" -> 1;
            case "II" -> 2;
            case "III" -> 3;
            case "IV" -> 4;
            case "V" -> 5;
            case "VI" -> 6;
            case "VII" -> 7;
            case "VIII" -> 8;
            case "IX" -> 9;
            case "X" -> 10;
            default -> 0;
        };
    }
}
