package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

import java.util.List;

/**
 * V6 variety — the settlement-event board. Lists every live {@link SettlementEvents.ActiveEvent}
 * server-wide, with its effect and time remaining, and offers operator controls to force or clear
 * an event on the settlement the viewer is standing in / registered to. Everything the system does
 * is inspectable here; no command is ever required.
 */
public final class SettlementEventGui {

    private SettlementEventGui() {}

    private static final int[] LIST_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
    };

    public static void open(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Settlement Events").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        fill(gui);

        List<SettlementEvents.ActiveEvent> events = SettlementEvents.allActive();
        boolean op = isOperator(player);

        gui.setSlot(4, new GuiElementBuilder(Items.BELL)
                .setName(Component.literal("Live Events").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Flavourful, temporary swings in a"))
                .addLoreLine(Component.literal("§7settlement's fortunes."))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Active right now: §f" + events.size()))
                .addLoreLine(Component.literal("§8Started on a world timer, never by players."))
                .glow().build());

        if (events.isEmpty()) {
            gui.setSlot(22, new GuiElementBuilder(Items.STRUCTURE_VOID)
                    .setName(Component.literal("No events right now")
                            .withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§8The world is quiet. Check back soon."))
                    .build());
        }

        int i = 0;
        for (SettlementEvents.ActiveEvent e : events) {
            if (i >= LIST_SLOTS.length) break;
            Settlement s = SettlementManager.get(e.settlementId);
            SettlementEvents.Type t = e.typeEnum();
            GuiElementBuilder b = new GuiElementBuilder(t.icon)
                    .setName(Component.literal(t.colored())
                            .withStyle(t.isPositive() ? ChatFormatting.GREEN : ChatFormatting.RED,
                                    ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7At: §f" + (s == null ? "?" : s.name)))
                    .addLoreLine(Component.literal("§7" + t.flavour))
                    .addLoreLine(Component.literal(""));
            for (String line : effectLines(t)) b.addLoreLine(Component.literal(line));
            b.addLoreLine(Component.literal(""));
            b.addLoreLine(Component.literal("§7Ends in §f"
                    + PoliticalServer.formatTime(SettlementEvents.remainingMs(e))));
            if (t.isPositive()) b.glow();
            gui.setSlot(LIST_SLOTS[i++], b.build());
        }

        // ── Operator controls, scoped to the viewer's settlement ──
        Settlement home = CivilizationHubGui.currentOrHome(player);
        if (op && home != null) {
            SettlementEvents.ActiveEvent here = SettlementEvents.activeFor(home.id);
            gui.setSlot(48, new GuiElementBuilder(Items.NETHER_STAR)
                    .setName(Component.literal("Force a random event")
                            .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7On §f" + home.name))
                    .addLoreLine(Component.literal("§8[Operator]"))
                    .setCallback((a, c, d, g) -> {
                        ServerLevel lvl = (ServerLevel) player.level();
                        SettlementEvents.Type[] all = SettlementEvents.Type.values();
                        SettlementEvents.Type pick = all[(int) (Math.random() * all.length)];
                        SettlementEvents.start(lvl.getServer(), home, pick, System.currentTimeMillis());
                        open(player);
                    })
                    .build());

            gui.setSlot(50, new GuiElementBuilder(here != null ? Items.BARRIER : Items.GRAY_DYE)
                    .setName(Component.literal(here != null ? "Clear the event here" : "Nothing to clear")
                            .withStyle(here != null ? ChatFormatting.RED : ChatFormatting.DARK_GRAY,
                                    ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7On §f" + home.name))
                    .addLoreLine(Component.literal("§8[Operator]"))
                    .setCallback((a, c, d, g) -> {
                        if (here != null) {
                            ServerLevel lvl = (ServerLevel) player.level();
                            SettlementEvents.clear(lvl.getServer(), home.id);
                        }
                        open(player);
                    })
                    .build());
        }

        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((a, c, d, g) -> VarietyGui.open(player))
                .build());

        gui.open();
    }

    static String[] effectLines(SettlementEvents.Type t) {
        java.util.List<String> out = new java.util.ArrayList<>();
        if (t.happinessDelta != 0) out.add(signed("Happiness", t.happinessDelta, ""));
        if (t.appealDelta != 0) out.add(signed("Appeal", t.appealDelta, ""));
        if (t.foodDelta != 0) out.add(signed("Food", t.foodDelta, ""));
        if (t.incomePerCycle() > 0) out.add("§7Treasury §a+" + t.incomePerCycle() + "§7/cycle");
        if (t.oneTimeTheft) out.add("§7Treasury §craided §7at onset");
        if (t == SettlementEvents.Type.GOLD_RUSH) out.add("§7Treasury §awindfall §7at onset");
        if (t.playerBuff) out.add("§7Visitors feel §aa lift §7nearby");
        if (out.isEmpty()) out.add("§8Purely atmospheric.");
        return out.toArray(new String[0]);
    }

    private static String signed(String label, int v, String suffix) {
        String c = v >= 0 ? "§a+" : "§c";
        return "§7" + label + " " + c + v + suffix;
    }

    private static void fill(SimpleGui gui) {
        GuiElementBuilder pane = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                .setName(Component.literal(" "));
        for (int i = 0; i < 54; i++) gui.setSlot(i, pane.build());
    }

    static boolean isOperator(ServerPlayer player) {
        try {
            return SpawnProtectionManager.isOperator(player);
        } catch (Throwable t) {
            return false;
        }
    }
}
