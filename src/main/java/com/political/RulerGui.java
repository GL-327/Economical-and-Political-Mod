package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.Map;

public class RulerGui {

    public static void open(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("Ruler Dashboard"));

        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        String uuid = player.getUuidAsString();
        String chair = DataManager.getChair();
        String viceChair = DataManager.getViceChair();
        boolean isChair = uuid.equals(chair);
        boolean isDictator = DictatorManager.isDictator(uuid);

        gui.setSlot(4, new GuiElementBuilder(Items.GOLDEN_HELMET)
                .setName(Text.literal("Ruler Dashboard").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Your Role: " + (isDictator ? "DICTATOR" : (isChair ? "Chair" : "Vice Chair"))).formatted(isDictator ? Formatting.DARK_RED : Formatting.GREEN))
                .glow()
                .build());

        gui.setSlot(19, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Text.literal("Active Perks").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(buildPerkLore())
                .build());

        List<String> activePerks = PerkManager.getActivePerks();
        int perkSlot = 28;
        for (String perkId : activePerks) {
            if (perkSlot >= 35) break;
            Perk perk = PerkManager.getPerk(perkId);
            if (perk != null) {
                gui.setSlot(perkSlot, new GuiElementBuilder(Items.ENCHANTED_BOOK)
                        .setName(Text.literal(perk.name).formatted(Formatting.GREEN))
                        .addLoreLine(Text.literal(perk.description).formatted(Formatting.GRAY))
                        .addLoreLine(Text.literal("Points: " + perk.pointValue).formatted(Formatting.YELLOW))
                        .build());
                perkSlot++;
            }
        }

        boolean taxEnabled = TaxManager.isTaxEnabled() || DictatorManager.isDictatorTaxEnabled();
        gui.setSlot(21, new GuiElementBuilder(taxEnabled ? Items.GOLD_BLOCK : Items.GOLD_INGOT)
                .setName(Text.literal("Tax System").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Status: " + (taxEnabled ? "ENABLED" : "DISABLED")).formatted(taxEnabled ? Formatting.GREEN : Formatting.RED))
                .addLoreLine(Text.literal("Daily Amount: " + (DictatorManager.isDictatorTaxEnabled() ? DictatorManager.getDictatorTaxAmount() : TaxManager.getDailyTaxAmount()) + " credits").formatted(Formatting.YELLOW))
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
                .setName(Text.literal("Tax Statistics").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Total Tax Owed: " + totalOwed + " credits").formatted(Formatting.RED))
                .addLoreLine(Text.literal("Players in Debt: " + playersInDebt).formatted(Formatting.GOLD))
                .build());

        gui.setSlot(25, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Text.literal("Players in Debt").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal("Click to view debtors").formatted(Formatting.GRAY))
                .setCallback((index, type, action) -> {
                    openDebtorsPage(player, "ruler");
                })
                .build());

        if (isChair || isDictator) {
            gui.setSlot(49, new GuiElementBuilder(Items.COMMAND_BLOCK)
                    .setName(Text.literal("Open Perks Menu").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                    .addLoreLine(Text.literal("Click to modify perks").formatted(Formatting.GRAY))
                    .setCallback((index, type, action) -> {
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

    private static Text buildPerkLore() {
        List<String> perks = PerkManager.getActivePerks();
        if (perks.isEmpty()) {
            return Text.literal("No active perks").formatted(Formatting.GRAY);
        }
        return Text.literal(perks.size() + " perks active").formatted(Formatting.GREEN);
    }

    // CHANGED: Added returnTo parameter
    public static void openDebtorsPage(ServerPlayerEntity player, String returnTo) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("Players in Debt"));

        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }

        Map<String, Integer> allTaxes = TaxManager.getAllTaxOwed();
        boolean isDictator = DictatorManager.isDictator(player.getUuidAsString());

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
                    .setName(Text.literal(debtorName).formatted(owed >= 50 ? Formatting.RED : Formatting.YELLOW))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Owes: " + owed + " credits").formatted(Formatting.GOLD))
                    .addLoreLine(Text.literal(owed >= 50 ? "RESTRICTED (Adventure Mode)" : "In Good Standing").formatted(owed >= 50 ? Formatting.RED : Formatting.GREEN));

            if (isDictator && owed >= 50) {
                builder.addLoreLine(Text.literal(""))
                        .addLoreLine(Text.literal("Click to summon player").formatted(Formatting.LIGHT_PURPLE))
                        .setCallback((index, type, action) -> {
                            if (DictatorManager.canSummon()) {
                                DictatorManager.summonPlayer(player, debtorUuid);
                            } else {
                                player.sendMessage(Text.literal("Summon on cooldown!").formatted(Formatting.RED));
                            }
                        });
            }

            gui.setSlot(slot, builder.build());
            slot++;
        }

        // CHANGED: Back button now goes to correct GUI based on returnTo parameter
        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("Back").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> {
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