package com.political;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mojang.brigadier.tree.CommandNode;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Every command the server has, read from the dispatcher itself.
 *
 * <p>A hand written list of commands is wrong the moment anybody adds one, so this asks the server
 * what it actually knows rather than being told. That also means it covers vanilla commands and
 * anything another mod registered, not only this one, which is usually what somebody wants when they
 * are hunting for a command they half remember.</p>
 *
 * <p>Commands with arguments cannot be run from a click, since there is nothing sensible to fill
 * them with, so those are shown with their usage and copied into the chat box instead. The ones that
 * work on their own run directly.</p>
 */
public final class AdminCommandsGui {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminCommandsGui.class);


    private AdminCommandsGui() {}

    private static final int SIZE = 54;
    /** Set by the search box, so paging keeps the filter. */
    private static String filter = "";

    public static void open(ServerPlayer player, int page) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Every Command")
                .withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        GuiKit.frame(gui, SIZE, GuiKit.DANGER_EDGE);

        List<Node> all = collect(player);
        List<Node> shown = new ArrayList<>();
        for (Node n : all) {
            if (filter.isEmpty() || n.name.contains(filter)) shown.add(n);
        }

        GuiKit.header(gui, Items.COMMAND_BLOCK, "Every Command", ChatFormatting.RED,
                shown.size() + " of " + all.size() + " shown",
                filter.isEmpty() ? "Read live from this server" : "Filtered by \"" + filter + "\"");

        List<GuiElement> entries = new ArrayList<>();
        for (Node n : shown) {
            GuiElementBuilder b = new GuiElementBuilder(
                    n.runnable ? Items.COMMAND_BLOCK : Items.REPEATING_COMMAND_BLOCK)
                    .setName(Component.literal("/" + n.name)
                            .withStyle(n.runnable ? ChatFormatting.GREEN : ChatFormatting.GOLD,
                                    ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal(n.children == 0
                            ? "§7No arguments"
                            : "§7" + n.children + " sub commands"));
            if (n.runnable) {
                b.addLoreLine(Component.literal(""));
                b.addLoreLine(Component.literal("§aClick to run it now"));
            } else {
                b.addLoreLine(Component.literal(""));
                b.addLoreLine(Component.literal("§7Needs arguments."));
                b.addLoreLine(Component.literal("§eClick to see how to use it"));
            }

            entries.add(b.setCallback((a, t, ci, sg) -> {
                if (n.runnable) {
                    run(player, n.name);
                    player.closeContainer();
                } else {
                    usage(player, n.name);
                    player.closeContainer();
                }
            }).build());
        }

        GuiKit.paint(gui, entries, page, GuiKit.LIST_SLOTS.length);
        GuiKit.pager(gui, SIZE, page, entries.size(), GuiKit.LIST_SLOTS.length,
                p -> open(player, p));

        gui.setSlot(SIZE - 6, GuiKit.action(Items.SPYGLASS, "Search", ChatFormatting.AQUA,
                () -> GuiKit.prompt(player, "Command name",
                        text -> {
                            filter = text.toLowerCase(Locale.ROOT).replace("/", "").trim();
                            open(player, 0);
                        },
                        () -> open(player, 0)),
                filter.isEmpty() ? "Narrow the list" : "Currently \"" + filter + "\""));

        if (!filter.isEmpty()) {
            gui.setSlot(SIZE - 4, GuiKit.action(Items.BARRIER, "Clear search", ChatFormatting.RED,
                    () -> {
                        filter = "";
                        open(player, 0);
                    }, "Show everything again"));
        }

        GuiKit.footer(gui, SIZE, player, () -> AdminDirectoryGui.open(player));
        gui.open();
    }

    private record Node(String name, boolean runnable, int children) {}

    /**
     * Every root command this player is actually permitted to see.
     *
     * <p>Filtering by permission matters here. Listing commands an operator cannot run would be
     * noise, and listing them for somebody who is not an operator would be worse.</p>
     */
    private static List<Node> collect(ServerPlayer player) {
        List<Node> out = new ArrayList<>();
        try {
            MinecraftServer server = ((ServerLevel) player.level()).getServer();
            if (server == null) return out;
            CommandSourceStack source = player.createCommandSourceStack();
            for (CommandNode<CommandSourceStack> node
                    : server.getCommands().getDispatcher().getRoot().getChildren()) {
                try {
                    if (!node.canUse(source)) continue;
                } catch (Exception e) {
                    LOGGER.debug("AdminCommandsGui.collect: ignored an error", e);
                    continue;
                }
                out.add(new Node(node.getName().toLowerCase(Locale.ROOT),
                        node.getCommand() != null, node.getChildren().size()));
            }
        } catch (Exception e) {
            LOGGER.debug("AdminCommandsGui.collect: ignored an error", e);
            // A dispatcher that will not enumerate leaves an empty list rather than a broken screen.
        }
        out.sort((a, b) -> a.name.compareTo(b.name));
        return out;
    }

    private static void run(ServerPlayer player, String command) {
        MinecraftServer server = ((ServerLevel) player.level()).getServer();
        if (server == null) return;
        server.execute(() -> {
            try {
                server.getCommands().getDispatcher()
                        .execute(command, player.createCommandSourceStack());
            } catch (Exception e) {
                LOGGER.warn("AdminCommandsGui.run: ignored an error", e);
                player.sendSystemMessage(Component.literal("§c/" + command + " did not run: "
                        + (e.getMessage() == null ? "needs arguments" : e.getMessage())));
            }
        });
    }

    private static void usage(ServerPlayer player, String command) {
        MinecraftServer server = ((ServerLevel) player.level()).getServer();
        if (server == null) return;
        try {
            var dispatcher = server.getCommands().getDispatcher();
            var parse = dispatcher.parse(command + " ", player.createCommandSourceStack());
            player.sendSystemMessage(Component.literal("§6/" + command + " §7takes arguments:"));
            var usages = dispatcher.getSmartUsage(
                    parse.getContext().getRootNode().getChild(command),
                    player.createCommandSourceStack());
            int shown = 0;
            for (var e : usages.entrySet()) {
                if (shown++ >= 12) break;
                player.sendSystemMessage(Component.literal("  §8/" + command + " §7" + e.getValue()));
            }
            if (usages.isEmpty()) {
                player.sendSystemMessage(Component.literal("  §8Type it in chat to see the options."));
            }
        } catch (Exception e) {
            LOGGER.warn("AdminCommandsGui.usage: ignored an error", e);
            player.sendSystemMessage(Component.literal("§7Type §f/" + command
                    + "§7 in chat to see its options."));
        }
    }
}
