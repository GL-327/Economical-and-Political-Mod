package com.political.government;
import com.political.DataManager;



import com.political.party.PartyJoinGui;
import com.political.party.PoliticalPartyManager;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.gui.SignGui;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.Locale;

/**
 * Party budgets — tax and bounty levies flow into all parties equally.
 * Members donate coins for support. Ruling-party leader spends on national programs (via /leader proposals + council).
 */
public class GovernmentTreasuryGui {

    public static void open(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        String partyId = PoliticalPartyManager.getPlayerPartyId(uuid);
        PoliticalPartyManager.Party party = partyId != null ? PoliticalPartyManager.getParty(partyId) : null;

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x4, player, false);
        gui.setTitle(Component.literal("Party budgets").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.yellow())
                    .setName(Component.literal("")).build());
        }

        long total = 0L;
        for (PoliticalPartyManager.Party p : PoliticalPartyManager.getParties()) {
            total += p.budgetCoins;
        }

        gui.setSlot(4, new GuiElementBuilder(Items.GOLD_BLOCK)
                .setName(Component.literal("Political party economy").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("All parties combined: " + fmt(total) + " coins").withStyle(ChatFormatting.WHITE))
                .addLoreLine(Component.literal("Tax & bounty levies split equally.").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal(party != null
                        ? "Your party: " + party.displayName
                        : "You are not in a party").withStyle(ChatFormatting.AQUA))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Members cannot withdraw — only salaries,").withStyle(ChatFormatting.DARK_GRAY))
                .addLoreLine(Component.literal("approved programs, and leader ads.").withStyle(ChatFormatting.DARK_GRAY))
                .glow()
                .build());

        if (party != null) {
            gui.setSlot(19, new GuiElementBuilder(Items.EMERALD)
                    .setName(Component.literal("Donate to " + party.displayName).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("Party pot: " + fmt(party.budgetCoins)).withStyle(ChatFormatting.WHITE))
                    .addLoreLine(Component.literal("+1 support per " + PoliticalPartyManager.COINS_PER_SUPPORT_POINT + " coins").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("Click to enter amount (sign)").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> openDonateSign(player))
                    .build());
        } else {
            gui.setSlot(19, new GuiElementBuilder(Items.BARRIER)
                    .setName(Component.literal("Join a party to donate").withStyle(ChatFormatting.RED))
                    .addLoreLine(Component.literal("/pp join caine or abel").withStyle(ChatFormatting.GRAY))
                    .build());
        }

        gui.setSlot(21, new GuiElementBuilder(Items.PAPER)
                .setName(Component.literal("Browse parties").withStyle(ChatFormatting.WHITE))
                .addLoreLine(Component.literal("Opens party picker").withStyle(ChatFormatting.GRAY))
                .setCallback((index, type, containerInput, slotGui) -> PartyJoinGui.open(player))
                .build());

        gui.setSlot(23, new GuiElementBuilder(Items.WRITABLE_BOOK)
                .setName(Component.literal("Leader tools").withStyle(ChatFormatting.LIGHT_PURPLE))
                .addLoreLine(Component.literal("/leader — ads & ruling programs").withStyle(ChatFormatting.GRAY))
                .build());

        String rulingId = PoliticalPartyManager.getRulingPartyId();
        if (rulingId != null) {
            PoliticalPartyManager.Party rp = PoliticalPartyManager.getParty(rulingId);
            String leaderUuid = PoliticalPartyManager.getPartyLeaderUuid(rulingId);
            String ln = leaderUuid != null ? DataManager.getPlayerName(leaderUuid) : "—";
            gui.setSlot(22, new GuiElementBuilder(Items.NETHER_STAR)
                    .setName(Component.literal("Ruling party").withStyle(ChatFormatting.GOLD))
                    .addLoreLine(Component.literal(rp != null ? rp.displayName : rulingId).withStyle(ChatFormatting.WHITE))
                    .addLoreLine(Component.literal("Leader: " + ln).withStyle(ChatFormatting.GRAY))
                    .build());
        }

        gui.setSlot(31, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("Close").withStyle(ChatFormatting.RED))
                .setCallback((index, type, containerInput, slotGui) -> player.closeContainer())
                .build());

        gui.open();
    }

    private static void openDonateSign(ServerPlayer player) {
        SignGui signGui = new SignGui(player) {
            @Override
            public void onManualClose() {
                String input = this.getLine(0).getString().trim().replaceAll("[^0-9]", "");
                if (input.isEmpty()) {
                    GovernmentTreasuryGui.open(player);
                    return;
                }
                try {
                    long amount = Long.parseLong(input);
                    if (amount <= 0) {
                        player.sendSystemMessage(Component.literal("✗ Amount must be positive!").withStyle(ChatFormatting.RED), false);
                        GovernmentTreasuryGui.open(player);
                        return;
                    }
                    PoliticalPartyManager.donateCoins(player, amount);
                } catch (NumberFormatException e) {
                    player.sendSystemMessage(Component.literal("✗ Invalid number!").withStyle(ChatFormatting.RED), false);
                }
                GovernmentTreasuryGui.open(player);
            }
        };
        signGui.setLine(0, Component.literal(""));
        signGui.setLine(1, Component.literal("^^^^^^^^^^^^^^").withStyle(ChatFormatting.YELLOW));
        signGui.setLine(2, Component.literal("Coin amount").withStyle(ChatFormatting.AQUA));
        signGui.setLine(3, Component.literal("to party pot").withStyle(ChatFormatting.GRAY));
        signGui.open();
    }

    private static String fmt(long amount) {
        return String.format(Locale.US, "%,d", amount);
    }
}
