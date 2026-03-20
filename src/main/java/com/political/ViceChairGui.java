package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class ViceChairGui {

    public static void open(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X5, player, false);
        gui.setTitle(Text.literal("🎖 Vice Chair Dashboard"));

        // Dark elegant background
        for (int i = 0; i < 45; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        // Decorative border with cyan
        int[] cyanBorder = {0, 1, 2, 3, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44};
        for (int slot : cyanBorder) {
            gui.setSlot(slot, new GuiElementBuilder(Items.CYAN_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        // Header with badge
        gui.setSlot(4, new GuiElementBuilder(Items.IRON_HELMET)
                .setName(Text.literal("🎖 Vice Chair Dashboard").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Welcome, Vice Chair!").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal("Assist the Chair in governing").formatted(Formatting.GRAY))
                .glow()
                .build());

        // ── Row 1: Chair Info (with gaps) ──
        String chair = DataManager.getChair();
        String chairName = chair != null ? DataManager.getPlayerName(chair) : "None";
        gui.setSlot(10, new GuiElementBuilder(Items.GOLDEN_HELMET)
                .setName(Text.literal("👑 Current Chair").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal(chairName).formatted(Formatting.YELLOW))
                .build());

        // Gap at 11
        gui.setSlot(12, new GuiElementBuilder(TaxManager.isTaxEnabled() ? Items.LIME_DYE : Items.RED_DYE)
                .setName(Text.literal("📋 Tax Status").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Status: " + (TaxManager.isTaxEnabled() ? "§aENABLED" : "§cDISABLED")))
                .addLoreLine(Text.literal("Amount: " + TaxManager.getDailyTaxAmount() + " credits/day").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("(Chair controls taxes)").formatted(Formatting.GRAY))
                .build());

        // ── Row 2: Perks (with gaps) ──
        List<String> activePerks = PerkManager.getActivePerks();
        gui.setSlot(19, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Text.literal("✨ Active Perks").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal(activePerks.size() + " perks active").formatted(Formatting.GREEN))
                .glow().build());

        // Show active perks
        int perkSlot = 20;
        for (String perkId : activePerks) {
            if (perkSlot >= 26) break;
            Perk perk = PerkManager.getPerk(perkId);
            if (perk != null) {
                gui.setSlot(perkSlot, new GuiElementBuilder(Items.ENCHANTED_BOOK)
                        .setName(Text.literal(perk.name).formatted(Formatting.GREEN))
                        .addLoreLine(Text.literal(perk.description).formatted(Formatting.GRAY))
                        .build());
                perkSlot += 2; // Gap between perks
            }
        }

        // Perks selection button
        boolean canChangePerks = PerkManager.canChangePerks(false);
        if (canChangePerks) {
            gui.setSlot(31, new GuiElementBuilder(Items.NETHER_STAR)
                    .setName(Text.literal("🌟 Select Perks").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Choose perks for this term").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("You have 2 perk points").formatted(Formatting.YELLOW))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Note: You are exempt from").formatted(Formatting.GREEN))
                    .addLoreLine(Text.literal("perk cooldowns!").formatted(Formatting.GREEN))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Click to select").formatted(Formatting.AQUA))
                    .setCallback((index, type, action) -> PerksGui.open(player, false))
                    .glow().build());
        } else {
            gui.setSlot(31, new GuiElementBuilder(Items.BARRIER)
                    .setName(Text.literal("🔒 Perks Locked").formatted(Formatting.RED, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Perks already selected").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("for this term!").formatted(Formatting.GRAY))
                    .build());
        }

        // Close button
        gui.setSlot(40, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("✖ Close").formatted(Formatting.RED, Formatting.BOLD))
                .setCallback((slot, clickType, action) -> gui.close())
                .build());

        gui.open();
    }
}