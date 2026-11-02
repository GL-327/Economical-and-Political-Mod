package com.political.technique;


import com.political.bounty.SlayerItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;

/** Craftable / droppable manuals that teach a technique on right-click. */
public final class TechniqueItems {

    public static final String ID_PREFIX = "technique_manual_";

    private TechniqueItems() {}

    public static String idFor(Technique technique) {
        return ID_PREFIX + technique.name().toLowerCase();
    }

    public static Technique fromStack(ItemStack stack) {
        String id = SlayerItems.getCustomItemId(stack);
        if (id == null || !id.startsWith(ID_PREFIX)) return null;
        return Technique.byName(id.substring(ID_PREFIX.length()).toUpperCase());
    }

    public static ItemStack createManual(Technique technique) {
        ItemStack stack = new ItemStack(Items.BOOK);
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Technique Manual: " + technique.displayName)
                        .withStyle(technique.color, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal(technique.flavor).withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Right-click to learn as Novice.").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("Equip with /technique equip " + technique.displayName)
                .withStyle(ChatFormatting.DARK_GRAY));
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        SlayerItems.setCustomItemId(stack, idFor(technique));
        return stack;
    }

    public static List<ItemStack> allManuals() {
        List<ItemStack> out = new ArrayList<>();
        for (Technique technique : Technique.all()) {
            out.add(createManual(technique));
        }
        return out;
    }
}
