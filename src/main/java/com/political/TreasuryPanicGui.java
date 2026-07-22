package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Pay escalating seals from party purse or leak treasury. */
public final class TreasuryPanicGui {

    private static final Map<UUID, Integer> SEALS = new ConcurrentHashMap<>();
    private static final Map<UUID, String> PARTY = new ConcurrentHashMap<>();

    public static void open(ServerPlayer player, String rulingPartyId) {
        SEALS.put(player.getUUID(), 0);
        PARTY.put(player.getUUID(), rulingPartyId);
        redraw(player);
    }

    private static void redraw(ServerPlayer player) {
        String partyId = PARTY.get(player.getUUID());
        Integer s = SEALS.get(player.getUUID());
        if (partyId == null || s == null) return;

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal("Crisis: Treasury Leak").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE).setName(Component.literal("")).build());
        }
        long nextCost = 200_000L * (s + 1);
        gui.setSlot(13, new GuiElementBuilder(Items.IRON_BARS)
                .setName(Component.literal("Seal breach (" + s + " placed)").withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal("Next seal: " + String.format("%,d", nextCost)).withStyle(ChatFormatting.GOLD))
                .addLoreLine(Component.literal("Each seal reduces leak severity").withStyle(ChatFormatting.GRAY))
                .setCallback((a, b, c, __slotGui) -> {
                    if (!PoliticalPartyManager.trySpendPartyBudget(partyId, nextCost)) {
                        player.sendSystemMessage(Component.literal("Party budget too low — resolve now!").withStyle(ChatFormatting.RED), false);
                        return;
                    }
                    SEALS.put(player.getUUID(), s + 1);
                    redraw(player);
                })
                .build());

        gui.setSlot(22, new GuiElementBuilder(Items.HONEY_BOTTLE)
                .setName(Component.literal("§aContain leak").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .setCallback((a, b, c, __slotGui) -> finish(player, partyId))
                .build());
        gui.open();
    }

    private static void finish(ServerPlayer player, String partyId) {
        int s = SEALS.getOrDefault(player.getUUID(), 0);
        SEALS.remove(player.getUUID());
        PARTY.remove(player.getUUID());
        player.closeContainer();
        long budget = PoliticalPartyManager.getPartyBudget(partyId);
        if (s >= 3) {
            long gain = Math.max(1L, (long) (budget * 0.05));
            PoliticalPartyManager.addPartyBudgetCoins(partyId, gain);
            PartyCrisisManager.broadcastParty(player, Component.literal("[Crisis] Leak sealed! Stability bonus +" + String.format("%,d", gain)).withStyle(ChatFormatting.GREEN));
        } else {
            long loss = Math.max(1L, (long) (budget * (0.20 - s * 0.04)));
            PoliticalPartyManager.trySpendPartyBudget(partyId, loss);
            PartyCrisisManager.broadcastParty(player, Component.literal("[Crisis] Treasury drained −" + String.format("%,d", loss)).withStyle(ChatFormatting.RED));
        }
        DataManager.save(PoliticalServer.server);
    }
}
