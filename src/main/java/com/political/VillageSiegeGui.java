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

/** 2D-style chest GUI: fortify lanes with party coins, then repel a raid. */
public final class VillageSiegeGui {

    private static final Map<UUID, int[]> WALLS = new ConcurrentHashMap<>();
    private static final Map<UUID, String> PARTY = new ConcurrentHashMap<>();

    public static void open(ServerPlayer player, String rulingPartyId) {
        WALLS.put(player.getUUID(), new int[5]);
        PARTY.put(player.getUUID(), rulingPartyId);
        redraw(player);
    }

    private static void redraw(ServerPlayer player) {
        String partyId = PARTY.get(player.getUUID());
        int[] w = WALLS.get(player.getUUID());
        if (partyId == null || w == null) return;

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Crisis: Village Lines").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE).setName(Component.literal("")).build());
        }
        gui.setSlot(4, new GuiElementBuilder(Items.VILLAGER_SPAWN_EGG)
                .setName(Component.literal("Fortify lanes — party purse").withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal("Budget: " + String.format("%,d", PoliticalPartyManager.getPartyBudget(partyId))).withStyle(ChatFormatting.GOLD))
                .build());

        int[] slots = {11, 13, 15, 21, 23};
        for (int i = 0; i < 5; i++) {
            int idx = i;
            int lvl = w[i];
            long cost = 50_000L * (lvl + 1);
            gui.setSlot(slots[i], new GuiElementBuilder(Items.OAK_FENCE)
                    .setName(Component.literal("Lane " + (i + 1) + " — Wall Lv " + lvl).withStyle(ChatFormatting.GREEN))
                    .addLoreLine(Component.literal("Next: " + String.format("%,d", cost) + " coins").withStyle(ChatFormatting.GOLD))
                    .setCallback((a, b, c, __slotGui) -> {
                        if (!PoliticalPartyManager.trySpendPartyBudget(partyId, cost)) {
                            player.sendSystemMessage(Component.literal("Party budget too low.").withStyle(ChatFormatting.RED), false);
                            return;
                        }
                        w[idx]++;
                        redraw(player);
                    })
                    .build());
        }

        gui.setSlot(31, new GuiElementBuilder(Items.IRON_GOLEM_SPAWN_EGG)
                .setName(Component.literal("§a§lREPULSE RAID").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Win: +12% party budget").withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal("Lose: −22% party budget").withStyle(ChatFormatting.RED))
                .setCallback((a, b, c, __slotGui) -> resolve(player, partyId, w))
                .build());

        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("Abandon (counts as loss)").withStyle(ChatFormatting.DARK_RED))
                .setCallback((a, b, c, __slotGui) -> fail(player, partyId, "The village was overrun (leader stood down)."))
                .build());

        gui.open();
    }

    private static void resolve(ServerPlayer player, String partyId, int[] w) {
        int threat = 8 + player.getRandom().nextInt(18);
        int power = 0;
        for (int v : w) {
            power += v * 3;
        }
        WALLS.remove(player.getUUID());
        PARTY.remove(player.getUUID());
        player.closeContainer();

        long budget = PoliticalPartyManager.getPartyBudget(partyId);
        if (power >= threat) {
            long gain = Math.max(1L, (long) (budget * 0.12));
            PoliticalPartyManager.addPartyBudgetCoins(partyId, gain);
            PartyCrisisManager.broadcastParty(player, Component.literal("[Crisis] Village held! Party purse +" + String.format("%,d", gain)).withStyle(ChatFormatting.GREEN));
        } else {
            long loss = Math.max(1L, (long) (budget * 0.22));
            PoliticalPartyManager.trySpendPartyBudget(partyId, loss);
            PartyCrisisManager.broadcastParty(player, Component.literal("[Crisis] Raid broke through! Party purse −" + String.format("%,d", loss)).withStyle(ChatFormatting.RED));
        }
        DataManager.save(PoliticalServer.server);
    }

    private static void fail(ServerPlayer player, String partyId, String msg) {
        long budget = PoliticalPartyManager.getPartyBudget(partyId);
        long loss = Math.max(1L, (long) (budget * 0.25));
        PoliticalPartyManager.trySpendPartyBudget(partyId, loss);
        WALLS.remove(player.getUUID());
        PARTY.remove(player.getUUID());
        player.closeContainer();
        PartyCrisisManager.broadcastParty(player, Component.literal(msg + " (−" + String.format("%,d", loss) + ")").withStyle(ChatFormatting.DARK_RED));
        DataManager.save(PoliticalServer.server);
    }
}
