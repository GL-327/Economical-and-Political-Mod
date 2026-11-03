package com.political;


import com.political.party.PoliticalPartyManager;
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

/** Three-sector border spend-down; win adds modest purse, lose shaves hard. */
public final class BorderRaidGui {

    private static final Map<UUID, int[]> FORT = new ConcurrentHashMap<>();
    private static final Map<UUID, String> PARTY = new ConcurrentHashMap<>();

    public static void open(ServerPlayer player, String rulingPartyId) {
        FORT.put(player.getUUID(), new int[3]);
        PARTY.put(player.getUUID(), rulingPartyId);
        redraw(player);
    }

    private static void redraw(ServerPlayer player) {
        String partyId = PARTY.get(player.getUUID());
        int[] f = FORT.get(player.getUUID());
        if (partyId == null || f == null) return;

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x4, player, false);
        gui.setTitle(Component.literal("Crisis: Border Raid").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.black()).setName(Component.literal("")).build());
        }
        int[] slots = {12, 14, 16};
        for (int i = 0; i < 3; i++) {
            int idx = i;
            int lvl = f[i];
            long cost = 120_000L * (lvl + 1);
            gui.setSlot(slots[i], new GuiElementBuilder(Items.STONE_BRICK_WALL)
                    .setName(Component.literal("Sector " + (i + 1) + " — Lv " + lvl).withStyle(ChatFormatting.AQUA))
                    .addLoreLine(Component.literal("Reinforce: " + String.format("%,d", cost)).withStyle(ChatFormatting.YELLOW))
                    .setCallback((a, b, c, __slotGui) -> {
                        if (!PoliticalPartyManager.trySpendPartyBudget(partyId, cost)) {
                            player.sendSystemMessage(Component.literal("Party budget too low.").withStyle(ChatFormatting.RED), false);
                            return;
                        }
                        f[idx]++;
                        redraw(player);
                    })
                    .build());
        }
        gui.setSlot(31, new GuiElementBuilder(Items.BELL)
                .setName(Component.literal("§aSound the defense").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Win: +8%  Lose: −18%").withStyle(ChatFormatting.GRAY))
                .setCallback((a, b, c, __slotGui) -> resolve(player, partyId, f))
                .build());
        gui.open();
    }

    private static void resolve(ServerPlayer player, String partyId, int[] f) {
        int need = 6 + player.getRandom().nextInt(10);
        int have = f[0] + f[1] + f[2];
        FORT.remove(player.getUUID());
        PARTY.remove(player.getUUID());
        player.closeContainer();
        long budget = PoliticalPartyManager.getPartyBudget(partyId);
        if (have >= need) {
            long g = Math.max(1L, (long) (budget * 0.08));
            PoliticalPartyManager.addPartyBudgetCoins(partyId, g);
            PartyCrisisManager.broadcastParty(player, Component.literal("[Crisis] Border held! +" + String.format("%,d", g)).withStyle(ChatFormatting.GREEN));
        } else {
            long loss = Math.max(1L, (long) (budget * 0.18));
            PoliticalPartyManager.trySpendPartyBudget(partyId, loss);
            PartyCrisisManager.broadcastParty(player, Component.literal("[Crisis] Sector breach! −" + String.format("%,d", loss)).withStyle(ChatFormatting.RED));
        }
        DataManager.save(PoliticalServer.server);
    }
}
