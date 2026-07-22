package com.political;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.ArrayList;
import java.util.List;

/** Scaling gear tied to Verdant Core (garden heart) tiers — one piece per tier. */
public final class SectorHeartItems {

    private SectorHeartItems() {}

    public static ItemStack createGardenOveralls(int tier) {
        tier = Math.max(1, Math.min(10, tier));
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        int green = 0x166534 + tier * 0x020402;
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(green));
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Verdant Overalls " + roman(tier)).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        int chance = 5 * tier;          // matches GardenHeartOverallsHandler: 0.05f * tier
        boolean doubleRoll = tier >= 8; // matches GardenHeartOverallsHandler: t8+ rolls twice
        int defense = 5 * tier;
        int speed = tier >= 5 ? 5 : 0;

        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("Sector: Garden Heart").withStyle(ChatFormatting.DARK_GREEN));
        lore.add(Component.literal("§8[Woven from living stems on the Verdant Core loom]"));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_GREEN));
        lore.add(Component.literal("❈ Defense: +" + defense).withStyle(ChatFormatting.GREEN));
        if (speed > 0) {
            lore.add(Component.literal("✦ Speed: +" + speed).withStyle(ChatFormatting.WHITE));
        }
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ VERDANT BOUNTY ━━━").withStyle(ChatFormatting.DARK_GREEN));
        lore.add(Component.literal("Harvesting a fully grown crop has a").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(chance + "% chance to duplicate its drop.").withStyle(ChatFormatting.AQUA));
        if (doubleRoll) {
            lore.add(Component.literal("Tier " + tier + " rolls this chance twice per harvest.").withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        lore.add(Component.literal("Only mature crops count — no early picking.").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Unlock tier " + tier + " in §a/sectorhearts §7→ Verdant Core").withStyle(ChatFormatting.GRAY));
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, tier >= 7);
        SlayerItems.setCustomItemId(stack, "garden_heart_overalls_t" + tier);
        return stack;
    }

    public static ItemStack createSyndicateCharter() {
        ItemStack s = new ItemStack(Items.WRITTEN_BOOK);
        s.set(DataComponents.CUSTOM_NAME, Component.literal("Deepstrike Syndicate Charter").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal("§7Proof of §6100M §7entry"));
        lore.add(Component.literal("§7+18% coin income (drops & rewards)"));
        SlayerItems.setCustomItemId(s, "deepstrike_charter_display");
        s.set(DataComponents.LORE, new ItemLore(lore));
        s.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        return s;
    }

    private static String roman(int t) {
        return switch (t) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            case 6 -> "VI";
            case 7 -> "VII";
            case 8 -> "VIII";
            case 9 -> "IX";
            default -> "X";
        };
    }
}
