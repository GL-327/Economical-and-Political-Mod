package com.political;


import com.political.government.ChairGui;
import com.political.government.DictatorManager;
import com.political.government.Perk;
import com.political.government.PerkManager;
import com.political.government.PerksGui;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.List;
import java.util.Map;

public class RulerGui {

    public static void open(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Ruler Dashboard"));

        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Component.literal(""))
                    .build());
        }

        String uuid = player.getUUID().toString();
        String chair = DataManager.getChair();
        String viceChair = DataManager.getViceChair();
        boolean isChair = uuid.equals(chair);
        boolean isDictator = DictatorManager.isDictator(uuid);

        gui.setSlot(4, new GuiElementBuilder(Items.GOLDEN_HELMET)
                .setName(Component.literal("Ruler Dashboard").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Your Role: " + (isDictator ? "DICTATOR" : (isChair ? "Chair" : "Vice Chair"))).withStyle(isDictator ? ChatFormatting.DARK_RED : ChatFormatting.GREEN))
                .glow()
                .build());

        gui.setSlot(19, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Component.literal("Active Perks").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(buildPerkLore())
                .build());

        List<String> activePerks = PerkManager.getActivePerks();
        int perkSlot = 28;
        for (String perkId : activePerks) {
            if (perkSlot >= 35) break;
            Perk perk = PerkManager.getPerk(perkId);
            if (perk != null) {
                gui.setSlot(perkSlot, new GuiElementBuilder(Items.ENCHANTED_BOOK)
                        .setName(Component.literal(perk.name).withStyle(ChatFormatting.GREEN))
                        .addLoreLine(Component.literal(perk.description).withStyle(ChatFormatting.GRAY))
                        .addLoreLine(Component.literal("Points: " + perk.pointValue).withStyle(ChatFormatting.YELLOW))
                        .build());
                perkSlot++;
            }
        }

        boolean taxEnabled = TaxManager.isTaxEnabled() || DictatorManager.isDictatorTaxEnabled();
        gui.setSlot(21, new GuiElementBuilder(taxEnabled ? Items.GOLD_BLOCK : Items.GOLD_INGOT)
                .setName(Component.literal("Tax System").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Status: " + (taxEnabled ? "ENABLED" : "DISABLED")).withStyle(taxEnabled ? ChatFormatting.GREEN : ChatFormatting.RED))
                .addLoreLine(Component.literal(
                        DictatorManager.isDictatorTaxEnabled()
                                ? ("Dictator Rate: " + DictatorManager.getDictatorTaxAmount() + " coins/day")
                                : ("Daily Rate: " + String.format("%.1f", TaxManager.getDailyTaxPercentage() * 100.0f) + "% of coins"))
                        .withStyle(ChatFormatting.YELLOW))
                .build());

        Map<String, Integer> allTaxes = TaxManager.getAllTaxOwed();
        int totalOwed = 0;
        int playersInDebt = 0;

        for (Map.Entry<String, Integer> entry : allTaxes.entrySet()) {
            if (entry.getValue() > 0) {
                totalOwed += entry.getValue();
                playersInDebt++;
            }
        }

        gui.setSlot(23, new GuiElementBuilder(Items.PAPER)
                .setName(Component.literal("Tax Statistics").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Total Tax Owed: " + totalOwed + " credits").withStyle(ChatFormatting.RED))
                .addLoreLine(Component.literal("Players in Debt: " + playersInDebt).withStyle(ChatFormatting.GOLD))
                .build());

        gui.setSlot(25, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Component.literal("Players in Debt").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Click to view debtors").withStyle(ChatFormatting.GRAY))
                .setCallback((index, type, containerInput, slotGui) -> {
                    openDebtorsPage(player, "ruler");
                })
                .build());

        if (isChair || isDictator) {
            gui.setSlot(49, new GuiElementBuilder(Items.COMMAND_BLOCK)
                    .setName(Component.literal("Open Perks Menu").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("Click to modify perks").withStyle(ChatFormatting.GRAY))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        if (isDictator) {
                            DictatorGui.open(player);
                        } else {
                            PerksGui.open(player, true);
                        }
                    })
                    .build());
        }

        gui.open();
    }

    private static Component buildPerkLore() {
        List<String> perks = PerkManager.getActivePerks();
        if (perks.isEmpty()) {
            return Component.literal("No active perks").withStyle(ChatFormatting.GRAY);
        }
        return Component.literal(perks.size() + " perks active").withStyle(ChatFormatting.GREEN);
    }

    // CHANGED: Added returnTo parameter
    public static void openDebtorsPage(ServerPlayer player, String returnTo) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Players in Debt"));

        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Component.literal(""))
                    .build());
        }

        Map<String, Integer> allTaxes = TaxManager.getAllTaxOwed();
        boolean isDictator = DictatorManager.isDictator(player.getUUID().toString());

        int slot = 10;
        for (Map.Entry<String, Integer> entry : allTaxes.entrySet()) {
            if (slot == 17) slot = 19;
            if (slot == 26) slot = 28;
            if (slot == 35) slot = 37;
            if (slot >= 44) break;

            if (entry.getValue() <= 0) continue;

            String debtorUuid = entry.getKey();
            String debtorName = DataManager.getPlayerName(debtorUuid);
            int owed = entry.getValue();

            GuiElementBuilder builder = new GuiElementBuilder(Items.PLAYER_HEAD)
                    .setName(Component.literal(debtorName).withStyle(owed >= 50 ? ChatFormatting.RED : ChatFormatting.YELLOW))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Owes: " + owed + " credits").withStyle(ChatFormatting.GOLD))
                    .addLoreLine(Component.literal(owed >= 50 ? "RESTRICTED (Adventure Mode)" : "In Good Standing").withStyle(owed >= 50 ? ChatFormatting.RED : ChatFormatting.GREEN));

            if (isDictator && owed >= 50) {
                builder.addLoreLine(Component.literal(""))
                        .addLoreLine(Component.literal("Click to summon player").withStyle(ChatFormatting.LIGHT_PURPLE))
                        .setCallback((index, type, containerInput, slotGui) -> {
                            if (DictatorManager.canSummon()) {
                                DictatorManager.summonPlayer(player, debtorUuid);
                            } else {
                                player.sendSystemMessage(Component.literal("Summon on cooldown!").withStyle(ChatFormatting.RED));
                            }
                        });
            }

            gui.setSlot(slot, builder.build());
            slot++;
        }

        // CHANGED: Back button now goes to correct GUI based on returnTo parameter
        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((index, type, containerInput, slotGui) -> {
                    switch (returnTo) {
                        case "admin" -> AdminGui.open(player);
                        case "chair" -> ChairGui.open(player);
                        case "dictator" -> DictatorGui.open(player);
                        default -> open(player);
                    }
                })
                .build());

        gui.open();
    }
}