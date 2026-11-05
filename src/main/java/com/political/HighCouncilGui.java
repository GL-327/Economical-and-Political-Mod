package com.political;


import com.political.settlement.Settlement;
import com.political.settlement.SettlementManager;
import com.political.settlement.WarGui;
import com.political.settlement.WarManager;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

import java.util.Map;

/** High Council: the Prime Seat, its ministries, and the edicts they enact. */
public final class HighCouncilGui {

    private HighCouncilGui() {}

    /** Where Back leads from this screen: the menu that opened it, remembered per player. */
    private static final com.political.core.gui.ReturnTo FROM = new com.political.core.gui.ReturnTo();

    /** Opens the screen from a parent menu (Back returns there), or with {@code null} from a command or the hub. */
    public static void openFrom(ServerPlayer player, Runnable back) {
        FROM.set(player.getUUID(), back);
        open(player);
    }

    public static void open(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("High Council").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        WarGui.fill(gui, 54);

        String prime = HighCouncilManager.getPrimeSeatUuid();
        String seatId = HighCouncilManager.getPrimeSettlementId();
        Settlement seat = seatId == null ? null : SettlementManager.get(seatId);
        boolean isPrime = HighCouncilManager.isPrimeSeat(player);
        boolean isMinister = HighCouncilManager.isMinister(player.getUUID().toString());

        if (prime == null) {
            gui.setSlot(22, new GuiElementBuilder(Items.BARRIER)
                    .setName(Component.literal("No Council Sits")
                            .withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7The High Council forms only when a"))
                    .addLoreLine(Component.literal("§7settlement reaches §dUTOPIA§7."))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§8Requires 250 sheltered beds, 200 citizens,"))
                    .addLoreLine(Component.literal("§82500 appeal, 10M treasury and a Wonder."))
                    .build());
            gui.setSlot(45, WarGui.back(FROM.getOr(player.getUUID(), () -> CivilizationHubGui.open(player))));
            gui.open();
            return;
        }

        gui.setSlot(4, new GuiElementBuilder(Items.BEACON)
                .setName(Component.literal("Prime Seat").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§f" + DataManager.getPlayerName(prime)))
                .addLoreLine(Component.literal("§7Seat of power: §f"
                        + (seat != null ? seat.name : "unknown")))
                .glow().build());

        // Ministries
        int[] slots = {19, 20, 21, 22};
        HighCouncilManager.Ministry[] ministries = HighCouncilManager.Ministry.values();
        Map<HighCouncilManager.Ministry, String> ministers = HighCouncilManager.getMinisters();
        for (int i = 0; i < ministries.length && i < slots.length; i++) {
            HighCouncilManager.Ministry m = ministries[i];
            String holder = ministers.get(m);
            gui.setSlot(slots[i], new GuiElementBuilder(
                    holder == null ? Items.CONCRETE.gray() : Items.GOLDEN_HELMET)
                    .setName(Component.literal(m.displayName).withStyle(m.color, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§f" + (holder == null ? "§8vacant"
                            : DataManager.getPlayerName(holder))))
                    .addLoreLine(Component.literal("§8War council weight: "
                            + WarManager.WarRole.MINISTER.weight))
                    .addLoreLine(Component.literal(isPrime
                            ? "§8Appoint with /council appoint" : ""))
                    .build());
        }

        // Edicts
        int[] edictSlots = {28, 29, 30, 31, 32};
        HighCouncilManager.Edict[] edicts = HighCouncilManager.Edict.values();
        for (int i = 0; i < edicts.length && i < edictSlots.length; i++) {
            HighCouncilManager.Edict e = edicts[i];
            boolean active = HighCouncilManager.isEdictActive(e);
            boolean canEnact = (isPrime || isMinister) && !active;
            long minsLeft = HighCouncilManager.edictRemainingMs(e) / 60000L;

            GuiElementBuilder b = new GuiElementBuilder(
                    active ? Items.CONCRETE.lime() : canEnact ? Items.PAPER : Items.CONCRETE.gray())
                    .setName(Component.literal(e.displayName).withStyle(e.color, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7" + e.description))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§7Cost: §6" + e.cost + " §7from the seat treasury"))
                    .addLoreLine(Component.literal("§7Duration: §f" + e.durationHours + "h"));

            if (active) {
                b.addLoreLine(Component.literal("§a§lIN FORCE §7— " + minsLeft + "m left"));
                b.glow();
                if (isPrime) {
                    b.addLoreLine(Component.literal("§eClick to repeal"));
                    b.setCallback((a, c, d, f) -> {
                        HighCouncilManager.repealEdict(player, e);
                        open(player);
                    });
                }
            } else if (canEnact) {
                b.addLoreLine(Component.literal("§eClick to enact"));
                b.setCallback((a, c, d, f) -> {
                    HighCouncilManager.enactEdict(player, e);
                    open(player);
                });
            } else {
                b.addLoreLine(Component.literal("§cOnly the Council may enact edicts."));
            }
            gui.setSlot(edictSlots[i], b.build());
        }

        gui.setSlot(49, new GuiElementBuilder(Items.WRITABLE_BOOK)
                .setName(Component.literal("Council Log").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§8Recent acts of the Council:"))
                .build());

        var log = HighCouncilManager.getLog();
        GuiElementBuilder logBtn = new GuiElementBuilder(Items.WRITABLE_BOOK)
                .setName(Component.literal("Council Log").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD));
        if (log.isEmpty()) logBtn.addLoreLine(Component.literal("§8Nothing recorded yet."));
        else for (int i = 0; i < Math.min(8, log.size()); i++) {
            logBtn.addLoreLine(Component.literal("§8• §7" + log.get(i)));
        }
        gui.setSlot(49, logBtn.build());

        gui.setSlot(45, WarGui.back(FROM.getOr(player.getUUID(), () -> CivilizationHubGui.open(player))));
        gui.open();
    }
}
