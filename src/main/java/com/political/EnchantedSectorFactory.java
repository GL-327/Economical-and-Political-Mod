package com.political;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/** Skyblock-style “enchanted” presentation: glint + suffix id + lore (no vanilla ench book). */
public final class EnchantedSectorFactory {

    private EnchantedSectorFactory() {}

    public static ItemStack skyblockEnchantVisual(ItemStack base) {
        ItemStack c = base.copy();
        String id = SlayerItems.getCustomItemId(c);
        c.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        List<Component> lore = new ArrayList<>();
        var existing = c.get(DataComponents.LORE);
        if (existing != null) {
            for (var line : existing.lines()) {
                lore.add(line);
            }
        }
        lore.add(Component.literal(""));
        lore.add(Component.literal("§d✦ Enchanted variant §8(Skyblock-style)"));
        lore.add(Component.literal("§7Default craft: §9160 Lapis §7in grid §8(/recipe)"));
        c.set(DataComponents.LORE, new ItemLore(lore));
        if (id != null) {
            SlayerItems.setCustomItemId(c, id + "_sbl_ench");
        } else {
            CustomItemLore.applySummarizedLore(c);
        }
        return c;
    }
}
