package com.political;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;

/**
 * V7 Pillar C — an opt in helper that upgrades an existing custom item's presentation so older
 * gear reads as modern and cool, without touching {@link ItemStatInjector}. It ensures a glint
 * on rare and above items and can add a tasteful "reforge" style prefix to the display name.
 *
 * <p>This is purely cosmetic. It never adds stats, never changes an item id and never alters
 * gameplay. {@link #applyModernPresentation(ItemStack)} is safe to call on any custom stack and
 * guards against being applied twice.
 */
public final class ItemShine {

    private ItemShine() {}

    /** Flag written into CUSTOM_DATA so we never double-apply the presentation pass. */
    private static final String SHINE_FLAG = "modern_shine";

    /** Reforge style prefixes, chosen deterministically per item so a stack always reads the same. */
    private static final String[] PREFIXES = {
            "Gilded", "Fabled", "Radiant", "Storied", "Pristine",
            "Ancient", "Hallowed", "Vaulted", "Crowned", "Eternal"
    };

    /**
     * Applies the modern presentation pass to a custom item stack: a glint on rare and above
     * items, plus a reforge style prefix on the display name. Safe on any stack and idempotent,
     * a second call on the same stack does nothing.
     *
     * @return true if it applied changes, false if the stack was not eligible or was already done.
     */
    public static boolean applyModernPresentation(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (SlayerItems.getCustomItemId(stack) == null) return false;
        if (alreadyApplied(stack)) return false;

        boolean rare = isRareOrAbove(stack);
        if (rare) {
            stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        }
        applyPrefix(stack);
        markApplied(stack);
        return true;
    }

    /**
     * Returns a reforge style prefix suggested for this stack, without modifying it. Useful for
     * callers that want to show the option before committing. Never null.
     */
    public static String reforgePrefix(ItemStack stack) {
        String id = stack == null ? null : SlayerItems.getCustomItemId(stack);
        int idx = id == null ? 0 : Math.abs(id.hashCode()) % PREFIXES.length;
        return PREFIXES[idx];
    }

    /** Returns true if this stack's rarity footer marks it rare or higher. */
    public static boolean isRareOrAbove(ItemStack stack) {
        ItemLore lore = stack.get(DataComponents.LORE);
        if (lore == null) return false;
        for (Component line : lore.lines()) {
            String s = line.getString().toUpperCase(java.util.Locale.ROOT);
            if (s.contains("RARE") || s.contains("EPIC")
                    || s.contains("LEGENDARY") || s.contains("MYTHIC")) {
                return true;
            }
        }
        return false;
    }

    // ── internals ──

    private static void applyPrefix(ItemStack stack) {
        Component current = stack.get(DataComponents.CUSTOM_NAME);
        if (current == null) return;
        String prefix = reforgePrefix(stack);
        Component name = Component.literal(prefix + " ")
                .withStyle(ChatFormatting.GRAY)
                .append(current);
        stack.set(DataComponents.CUSTOM_NAME, name);
    }

    private static boolean alreadyApplied(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return false;
        return data.copyTag().getBooleanOr(SHINE_FLAG, false);
    }

    private static void markApplied(ItemStack stack) {
        CustomData existing = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag nbt = (existing != null) ? existing.copyTag() : new CompoundTag();
        nbt.putBoolean(SHINE_FLAG, true);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
    }
}
