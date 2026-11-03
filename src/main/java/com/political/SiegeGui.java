package com.political;


import com.political.settlement.Settlement;
import com.political.settlement.SettlementManager;
import com.political.settlement.SettlementTier;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * The siege board.
 *
 * <p>Shows what is under attack right now and what each settlement would face if something came for
 * it tonight. The second half matters more than it looks: a player deciding whether to push their
 * town to a city should be able to see that the city gets an extra wave for the privilege.</p>
 */
public final class SiegeGui {

    private SiegeGui() {}

    private static final int SIZE = 54;

    public static void open(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Sieges")
                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        GuiKit.frame(gui, SIZE, GuiKit.DANGER_EDGE);

        GuiKit.header(gui, Items.BANNER.red(), "Sieges", ChatFormatting.DARK_RED,
                SiegeManager.count() + " under attack right now",
                "What you built is what is at stake");

        List<GuiElement> entries = new ArrayList<>();

        // Anything happening right now goes first.
        for (SiegeManager.Active a : SiegeManager.all()) {
            entries.add(GuiKit.info(Items.BANNER.red(), a.settlementName, ChatFormatting.RED,
                    "§cUnder siege",
                    "Wave " + a.wave + " of " + a.totalWaves,
                    a.attackers.size() + " still standing",
                    a.slain + " put down",
                    a.breachTicks > 0
                            ? "§4The cornerstone is being taken"
                            : "§aThe cornerstone is held",
                    "At " + a.cornerstone.getX() + ", " + a.cornerstone.getZ()));
        }

        // Then everything that could be attacked, with what it would face.
        for (Settlement s : SettlementManager.getAllSettlements()) {
            if (SiegeManager.forSettlement(s.id) != null) continue;
            SettlementTier tier;
            try {
                tier = SettlementTier.valueOf(s.tier);
            } catch (Exception e) {
                tier = SettlementTier.OUTPOST;
            }
            final SettlementTier t = tier;
            entries.add(GuiKit.info(Items.CAMPFIRE, s.name, t.color,
                    t.displayName,
                    "Would face " + SiegeManager.wavesFor(t) + " waves",
                    "Holding pays about "
                            + GuiKit.num(SiegeManager.rewardFor(t, 40)) + " coins",
                    "Losing costs appeal and people",
                    "Population " + s.population + ", appeal " + s.appeal));
        }

        GuiKit.paint(gui, entries, 0, GuiKit.LIST_SLOTS.length);

        gui.setSlot(49, GuiKit.info(Items.WRITABLE_BOOK, "How a siege is lost", ChatFormatting.WHITE,
                "Attackers walk in from outside and head",
                "for the cornerstone.",
                "",
                "Let them stand on it unopposed for thirty",
                "seconds and the settlement pays: appeal",
                "and a fifth of its people.",
                "",
                "Stand on it yourself and the clock stops."));

        GuiKit.footer(gui, SIZE, player, null);
        gui.open();
    }

    /** The operator side: pick a settlement and send something at it now. */
    public static void openAdmin(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Call a siege")
                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        GuiKit.frame(gui, SIZE, GuiKit.DANGER_EDGE);
        GuiKit.header(gui, Items.BANNER.red(), "Call a siege", ChatFormatting.DARK_RED,
                "Starts one immediately",
                "Scale comes from the settlement's tier");

        MinecraftServer server = ((ServerLevel) player.level()).getServer();
        List<GuiElement> entries = new ArrayList<>();
        for (Settlement s : SettlementManager.getAllSettlements()) {
            boolean busy = SiegeManager.forSettlement(s.id) != null;
            SettlementTier tier;
            try {
                tier = SettlementTier.valueOf(s.tier);
            } catch (Exception e) {
                tier = SettlementTier.OUTPOST;
            }
            final SettlementTier t = tier;
            entries.add(GuiKit.action(busy ? Items.BARRIER : Items.CAMPFIRE, s.name,
                    busy ? ChatFormatting.DARK_GRAY : t.color,
                    () -> {
                        if (busy) return;
                        GuiKit.confirm(player, "Besiege " + s.name,
                                SiegeManager.wavesFor(t) + " waves, starting now. Losing costs "
                                        + "the settlement appeal and people.",
                                () -> {
                                    SiegeManager.begin(server, s);
                                    openAdmin(player);
                                },
                                () -> openAdmin(player));
                    },
                    busy ? "§cAlready under siege" : t.displayName,
                    SiegeManager.wavesFor(t) + " waves",
                    "Population " + s.population));
        }

        GuiKit.paint(gui, entries, 0, GuiKit.LIST_SLOTS.length);
        GuiKit.footer(gui, SIZE, player, () -> AdminPanel.open(player));
        gui.open();
    }
}
