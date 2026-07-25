package com.political;

import com.mojang.brigadier.CommandDispatcher;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

import java.util.List;

/** The contract board. Take work, watch it tick over, come back and get paid. */
public final class ContractGui {

    private ContractGui() {}

    private static final int[] OFFER_CELLS = {10, 11, 12, 13, 14, 15, 16, 19};
    private static final int[] ACTIVE_CELLS = {29, 31, 33};

    public static void open(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Contract Board").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        WarGui.fill(gui, 54);

        long mins = ContractManager.nextRotationMs() / 60000L;
        gui.setSlot(4, new GuiElementBuilder(Items.WRITABLE_BOOK)
                .setName(Component.literal("Contract Board").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Board rewrites in §f" + mins + " §7minutes."))
                .addLoreLine(Component.literal("§7You may hold §f" + ContractManager.MAX_ACTIVE + " §7at once."))
                .addLoreLine(Component.literal("§7Left this rotation: §f"
                        + ContractManager.remainingThisRotation(uuid)))
                .glow().build());

        // Offers.
        List<Contract> offers = ContractManager.board();
        for (int i = 0; i < offers.size() && i < OFFER_CELLS.length; i++) {
            Contract c = offers.get(i);
            boolean held = ContractManager.find(uuid, c) != null;

            GuiElementBuilder b = new GuiElementBuilder(c.icon)
                    .setName(Component.literal(c.display).withStyle(c.colour, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§8" + c.kind.colour + c.kind.label
                            + " §8difficulty " + c.difficulty))
                    .addLoreLine(Component.literal("§7" + c.flavor))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§7Requires: §f" + c.required))
                    .addLoreLine(Component.literal("§7Pays: §6" + c.reward + " coins"))
                    .addLoreLine(Component.literal("§7And: §b" + c.energyReward() + " cursed energy"))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal(held ? "§a§lTAKEN" : "§eClick to take this work"));
            if (held) b.glow();
            else b.setCallback((a, ct, d, g) -> {
                ContractManager.accept(player, c);
                open(player);
            });
            gui.setSlot(OFFER_CELLS[i], b.build());
        }

        gui.setSlot(22, new GuiElementBuilder(Items.PAPER)
                .setName(Component.literal("Your Contracts").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7What you are carrying right now."))
                .build());

        // Active work.
        List<ContractManager.Active> active = ContractManager.activeOf(uuid);
        for (int i = 0; i < ACTIVE_CELLS.length; i++) {
            if (i >= active.size()) {
                gui.setSlot(ACTIVE_CELLS[i], new GuiElementBuilder(Items.LIGHT_GRAY_STAINED_GLASS_PANE)
                        .setName(Component.literal("Open slot").withStyle(ChatFormatting.DARK_GRAY))
                        .addLoreLine(Component.literal("§8Take something from the board."))
                        .build());
                continue;
            }
            ContractManager.Active a = active.get(i);
            Contract c = Contract.byId(a.contract);
            if (c == null) continue;
            boolean done = a.progress >= c.required;

            GuiElementBuilder b = new GuiElementBuilder(done ? Items.LIME_CONCRETE : c.icon)
                    .setName(Component.literal(c.display).withStyle(c.colour, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7Progress: §f" + a.progress + "§7/§f" + c.required))
                    .addLoreLine(Component.literal(bar(a.progress, c.required)))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal(done
                            ? "§a§lREADY §7- click to claim"
                            : "§eLeft click to abandon"));
            if (done) b.glow();
            b.setCallback((idx, ct, d, g) -> {
                if (done) ContractManager.claim(player, c);
                else ContractManager.abandon(player, c);
                open(player);
            });
            gui.setSlot(ACTIVE_CELLS[i], b.build());
        }

        gui.setSlot(49, new GuiElementBuilder(Items.BOOK)
                .setName(Component.literal("How this works").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Hunt contracts count hostile kills."))
                .addLoreLine(Component.literal("§7Craft contracts count what you make."))
                .addLoreLine(Component.literal("§7Explore contracts count expeditions and escapes."))
                .addLoreLine(Component.literal("§8Pay is deliberately modest. Going out and"))
                .addLoreLine(Component.literal("§8playing will always earn you more."))
                .build());

        gui.setSlot(48, WarGui.back(() -> CivilizationHubGui.open(player)));
        gui.open();
    }

    private static String bar(int at, int of) {
        int filled = of <= 0 ? 0 : Math.min(20, (int) ((at / (double) of) * 20));
        StringBuilder sb = new StringBuilder("§a");
        for (int i = 0; i < 20; i++) {
            if (i == filled) sb.append("§8");
            sb.append('|');
        }
        return sb.toString();
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("contracts").executes(ctx -> {
            open(ctx.getSource().getPlayerOrException());
            return 1;
        }));
    }
}
