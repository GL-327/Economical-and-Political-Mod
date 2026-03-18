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
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X4, player, false);
        gui.setTitle(Text.literal("Vice Chair Dashboard"));

        // Fill with glass panes
        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.LIGHT_BLUE_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.IRON_HELMET)
                .setName(Text.literal("Vice Chair Dashboard").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Welcome, Vice Chair!").formatted(Formatting.GREEN))
                .glow()
                .build());

        // Active Perks Display
        gui.setSlot(19, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Text.literal("Active Perks").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal(PerkManager.getActivePerks().size() + " perks active").formatted(Formatting.GREEN))
                .build());

        // Show active perks
        List<String> activePerks = PerkManager.getActivePerks();
        int perkSlot = 10;
        for (String perkId : activePerks) {
            if (perkSlot >= 17) break;
            Perk perk = PerkManager.getPerk(perkId);
            if (perk != null) {
                gui.setSlot(perkSlot, new GuiElementBuilder(Items.ENCHANTED_BOOK)
                        .setName(Text.literal(perk.name).formatted(Formatting.GREEN))
                        .addLoreLine(Text.literal(perk.description).formatted(Formatting.GRAY))
                        .build());
                perkSlot++;
            }
        }

        // Chair Info
        String chair = DataManager.getChair();
        String chairName = chair != null ? DataManager.getPlayerName(chair) : "None";
        gui.setSlot(21, new GuiElementBuilder(Items.GOLDEN_HELMET)
                .setName(Text.literal("Current Chair").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal(chairName).formatted(Formatting.YELLOW))
                .build());

        // Perks Menu Button - with lock check
        // Vice Chair is exempt from cooldowns but still needs lock for "already selected this term"
        boolean canChangePerks = PerkManager.canChangePerks(false);  // false = Vice Chair
        if (canChangePerks) {
            gui.setSlot(23, new GuiElementBuilder(Items.NETHER_STAR)
                    .setName(Text.literal("Select Perks").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Choose perks for this term").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("You have 2 perk points").formatted(Formatting.YELLOW))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Note: You are exempt from").formatted(Formatting.GREEN))
                    .addLoreLine(Text.literal("perk cooldowns!").formatted(Formatting.GREEN))
                    .setCallback((index, type, action) -> {
                        PerksGui.open(player, false); // false = isViceChair (NOT chair)
                    })
                    .build());
        } else {
            gui.setSlot(23, new GuiElementBuilder(Items.BARRIER)
                    .setName(Text.literal("Perks Locked").formatted(Formatting.RED, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Perks have already been").formatted(Formatting.GRAY))
                    .addLoreLine(Text.literal("selected for this term!").formatted(Formatting.GRAY))
                    .build());
        }

        // Tax Info (view only for Vice Chair)
        boolean taxEnabled = TaxManager.isTaxEnabled();
        gui.setSlot(25, new GuiElementBuilder(taxEnabled ? Items.GOLD_BLOCK : Items.GOLD_INGOT)
                .setName(Text.literal("Tax Status").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Status: " + (taxEnabled ? "ENABLED" : "DISABLED")).formatted(taxEnabled ? Formatting.GREEN : Formatting.RED))
                .addLoreLine(Text.literal("Daily Amount: " + TaxManager.getDailyTaxAmount() + " credits").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("(Chair controls taxes)").formatted(Formatting.GRAY))
                .build());

        gui.open();
    }
}