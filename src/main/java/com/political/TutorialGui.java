package com.political;

import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * The admin side of the tutorial: who has seen it, who should, and whether it runs at all.
 *
 * <p>The gamerule can be flipped from here rather than typed, because an operator setting up a
 * server should not have to know the rule is called doTutorialOnStart to find it.</p>
 */
public final class TutorialGui {

    private TutorialGui() {}

    private static final int SIZE = 54;

    public static void open(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Tutorial")
                .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        GuiKit.frame(gui, SIZE);

        boolean on = PoliticalServer.server != null && TutorialManager.enabled(PoliticalServer.server);

        GuiKit.header(gui, Items.KNOWLEDGE_BOOK, "Tutorial", ChatFormatting.YELLOW,
                on ? "Runs on first join" : "Off, nobody gets it automatically",
                TutorialManager.stepCount() + " steps",
                TutorialManager.seenCount() + " players have seen it");

        gui.setSlot(20, GuiKit.action(on ? Items.LIME_DYE : Items.GRAY_DYE,
                on ? "On for new players" : "Off",
                on ? ChatFormatting.GREEN : ChatFormatting.GRAY,
                () -> {
                    toggle(player, !on);
                    open(player);
                },
                on ? "Every first join gets the walk through"
                   : "Nobody gets it unless you run it on them",
                "",
                "§8Game rule: " + TutorialManager.RULE_NAME,
                "§8Also settable with /gamerule."));

        gui.setSlot(22, GuiKit.action(Items.BOOK, "Run it on myself", ChatFormatting.AQUA,
                () -> {
                    TutorialManager.start(player, true);
                    player.sendSystemMessage(Component.literal("§7Starting."));
                },
                "See exactly what a new player sees",
                "",
                "§8The best way to check it reads well."));

        gui.setSlot(24, GuiKit.info(Items.PAPER, "Running it on somebody else",
                ChatFormatting.WHITE,
                "  §f/tutorial <player>",
                "",
                "Starts it for them whether or not they",
                "have seen it before.",
                "",
                "§8They can stop it with /tutorial stop."));

        // Everybody online, and whether they have had it.
        List<GuiElement> rows = new ArrayList<>();
        if (PoliticalServer.server != null) {
            for (ServerPlayer p : PoliticalServer.server.getPlayerList().getPlayers()) {
                boolean seen = TutorialManager.hasSeen(p);
                boolean running = TutorialManager.isRunning(p);
                rows.add(new GuiElementBuilder(Items.PLAYER_HEAD)
                        .setName(Component.literal(p.getName().getString())
                                .withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD))
                        .addLoreLine(Component.literal(running ? "§ePartway through"
                                : seen ? "§aHas seen it" : "§7Has not seen it"))
                        .addLoreLine(Component.literal(""))
                        .addLoreLine(Component.literal("§eLeft click to run it on them."))
                        .addLoreLine(Component.literal("§eRight click to forget they saw it."))
                        .setCallback((s, t, ci, sg) -> {
                            if (t.isRight) {
                                TutorialManager.reset(p.getUUID());
                                player.sendSystemMessage(Component.literal(
                                        "§7" + p.getName().getString()
                                                + " will get it again on their next join."));
                            } else {
                                TutorialManager.start(p, true);
                                player.sendSystemMessage(Component.literal(
                                        "§aStarted it for " + p.getName().getString() + "."));
                            }
                            open(player);
                        }).build());
            }
        }
        GuiKit.paint(gui, rows, 0, GuiKit.LIST_SLOTS.length);

        gui.setSlot(SIZE - 5, GuiKit.info(Items.WRITABLE_BOOK, "What it covers",
                ChatFormatting.WHITE,
                "One path, not a tour of everything.",
                "",
                "  The Chair, and what it controls",
                "  Perks, and why an election matters",
                "  How to stand and how to vote",
                "  Where coins come from",
                "  Bounties and delves",
                "",
                "Each step is a clickable line, so a player",
                "can act on it while it is still on screen.",
                "",
                "§8Fifty thousand coins at the end."));

        GuiKit.footer(gui, SIZE, player, () -> AdminPanel.open(player));
        gui.open();
    }

    /**
     * Flips the gamerule.
     *
     * <p>Set through the command dispatcher rather than by poking the value directly, so the change
     * is broadcast and saved exactly as if an operator had typed it.</p>
     */
    private static void toggle(ServerPlayer player, boolean want) {
        try {
            if (PoliticalServer.server == null) return;
            PoliticalServer.server.getCommands().performPrefixedCommand(
                    PoliticalServer.server.createCommandSourceStack(),
                    "gamerule " + TutorialManager.RULE_NAME + " " + want);
            player.sendSystemMessage(Component.literal(want
                    ? "§aNew players will get the walk through."
                    : "§7New players will not get the walk through."));
        } catch (Exception e) {
            player.sendSystemMessage(Component.literal(
                    "§cCould not change that rule: " + e.getMessage()));
        }
    }
}
