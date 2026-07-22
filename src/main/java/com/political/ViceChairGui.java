package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.List;

public class ViceChairGui {

    public static void open(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x5, player, false);
        gui.setTitle(Component.literal("🎖 Vice Chair Dashboard"));

        // Dark elegant background
        for (int i = 0; i < 45; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Component.literal("")).build());
        }

        // Decorative border with cyan
        int[] cyanBorder = {0, 1, 2, 3, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44};
        for (int slot : cyanBorder) {
            gui.setSlot(slot, new GuiElementBuilder(Items.CYAN_STAINED_GLASS_PANE)
                    .setName(Component.literal("")).build());
        }

        // Header with badge
        gui.setSlot(4, new GuiElementBuilder(Items.IRON_HELMET)
                .setName(Component.literal("🎖 Vice Chair Dashboard").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Welcome, Vice Chair!").withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal("Assist the Chair in governing").withStyle(ChatFormatting.GRAY))
                .glow()
                .build());

        // ── Row 1: Chair Info (with gaps) ──
        String chair = DataManager.getChair();
        String chairName = chair != null ? DataManager.getPlayerName(chair) : "None";
        gui.setSlot(10, new GuiElementBuilder(Items.GOLDEN_HELMET)
                .setName(Component.literal("👑 Current Chair").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal(chairName).withStyle(ChatFormatting.YELLOW))
                .build());

        // Gap at 11
        gui.setSlot(12, new GuiElementBuilder(TaxManager.isTaxEnabled() ? Items.LIME_DYE : Items.RED_DYE)
                .setName(Component.literal("📋 Tax Status").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Status: " + (TaxManager.isTaxEnabled() ? "§aENABLED" : "§cDISABLED")))
                .addLoreLine(Component.literal("Rate: " + String.format("%.1f", TaxManager.getDailyTaxPercentage() * 100.0f) + "% of coins/day").withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("(Chair controls taxes)").withStyle(ChatFormatting.GRAY))
                .build());

        // ── Row 2: Perks (with gaps) ──
        List<String> activePerks = PerkManager.getActivePerks();
        gui.setSlot(19, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Component.literal("✨ Active Perks").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal(activePerks.size() + " perks active").withStyle(ChatFormatting.GREEN))
                .glow().build());

        // Show active perks
        int perkSlot = 20;
        for (String perkId : activePerks) {
            if (perkSlot >= 26) break;
            Perk perk = PerkManager.getPerk(perkId);
            if (perk != null) {
                gui.setSlot(perkSlot, new GuiElementBuilder(Items.ENCHANTED_BOOK)
                        .setName(Component.literal(perk.name).withStyle(ChatFormatting.GREEN))
                        .addLoreLine(Component.literal(perk.description).withStyle(ChatFormatting.GRAY))
                        .build());
                perkSlot += 2; // Gap between perks
            }
        }

        // Perks selection button
        boolean canChangePerks = PerkManager.canChangePerks(false);
        if (canChangePerks) {
            gui.setSlot(31, new GuiElementBuilder(Items.NETHER_STAR)
                    .setName(Component.literal("🌟 Select Perks").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Choose perks for this term").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("You have 2 perk points").withStyle(ChatFormatting.YELLOW))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Note: You are exempt from").withStyle(ChatFormatting.GREEN))
                    .addLoreLine(Component.literal("perk cooldowns!").withStyle(ChatFormatting.GREEN))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to select").withStyle(ChatFormatting.AQUA))
                    .setCallback((index, type, containerInput, slotGui) -> PerksGui.open(player, false))
                    .glow().build());
        } else {
            gui.setSlot(31, new GuiElementBuilder(Items.BARRIER)
                    .setName(Component.literal("🔒 Perks Locked").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Perks already selected").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("for this term!").withStyle(ChatFormatting.GRAY))
                    .build());
        }

        // Close button
        gui.setSlot(40, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("✖ Close").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .setCallback((slot, clicktype, containerInput, slotGui) -> gui.close())
                .build());

        gui.open();
    }
}