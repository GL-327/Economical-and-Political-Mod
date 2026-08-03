package com.political;

import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

import java.util.List;

/**
 * The live reload screen.
 *
 * <p>Says plainly what it can and cannot do before you press anything, because the worst version of
 * a feature like this is one that looks like it worked. Data reloading always works. Code reloading
 * depends on how the JVM was started, and when it is unavailable this screen tells you the exact
 * flag to add rather than leaving you to find out.</p>
 */
public final class HotReloadGui {

    private HotReloadGui() {}

    private static final int SIZE = 54;

    public static void open(ServerPlayer player) {
        open(player, null);
    }

    private static void open(ServerPlayer player, HotReloadManager.Result last) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Live reload")
                .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        GuiKit.frame(gui, SIZE);

        boolean code = HotReloadAgent.available();
        int pending = HotReloadManager.pendingClasses();

        GuiKit.header(gui, Items.CLOCK, "Live reload", ChatFormatting.AQUA,
                "Change the server without stopping it",
                code ? "Code and data" : "Data only, see below",
                pending + " class files waiting to swap");

        gui.setSlot(20, GuiKit.action(Items.WRITABLE_BOOK, "Reload data", ChatFormatting.GREEN,
                () -> {
                    HotReloadManager.Result r = HotReloadManager.reloadData();
                    HotReloadManager.announce(player, "Server data reloaded");
                    open(player, r);
                },
                "Recipes, item edits, bazaar catalogue",
                "and everybody's stats.",
                "",
                "Always works. Nothing to set up.",
                "Nobody gets disconnected.",
                "",
                "§8The recipe file is large, so the server",
                "§8pauses while this runs. It reports how",
                "§8long it took."));

        gui.setSlot(22, GuiKit.action(code ? Items.COMMAND_BLOCK : Items.BARRIER,
                "Reload code", code ? ChatFormatting.GOLD : ChatFormatting.RED,
                () -> {
                    HotReloadManager.Result r = HotReloadManager.reloadClasses();
                    if (r.ok()) HotReloadManager.announce(player, "Server code reloaded");
                    open(player, r);
                },
                code ? pending + " class files ready" : "Not available on this server",
                "",
                "Swaps recompiled classes into the running",
                "server. Method bodies only: a change that",
                "adds a method or a field needs a restart.",
                "",
                "§8Drop .class files in the hotswap folder."));

        gui.setSlot(24, GuiKit.action(Items.CHEST, "Where to put files", ChatFormatting.YELLOW,
                () -> {
                    player.sendSystemMessage(Component.literal("§b⟳ §7Hotswap folder:"));
                    player.sendSystemMessage(Component.literal("§f"
                            + HotReloadManager.swapDir().toAbsolutePath()));
                    open(player, last);
                },
                "Prints the folder path into chat",
                "so you can copy it out."));

        gui.setSlot(30, GuiKit.info(Items.PAPER, "How to reload code", ChatFormatting.WHITE,
                "1. Change the source and compile it.",
                "2. Copy the .class files from",
                "   build/classes/java/main into the",
                "   hotswap folder.",
                "3. Press Reload code.",
                "",
                "Inner classes count as separate files,",
                "so copy Foo$Bar.class too."));

        gui.setSlot(32, GuiKit.info(Items.REDSTONE_TORCH, "What needs a restart",
                ChatFormatting.WHITE,
                "The JVM will swap what a method does.",
                "It will not swap:",
                "",
                "  a new or removed method",
                "  a new or removed field",
                "  a changed parent class",
                "",
                "Those are limits of the platform, not",
                "of this button. It will say so plainly",
                "rather than pretending."));

        if (!code) {
            gui.setSlot(40, GuiKit.info(Items.BARRIER, "Turning code reload on",
                    ChatFormatting.RED,
                    HotReloadAgent.describe(),
                    "",
                    "Add this to the server start command",
                    "and restart once:",
                    "",
                    "  §f-Djdk.attach.allowAttachSelf=true",
                    "",
                    "That is the last restart you need for",
                    "code changes."));
        }

        if (last != null) {
            String[] lines = last.lines().toArray(new String[0]);
            gui.setSlot(last.ok() ? 4 : 4, GuiKit.info(
                    last.ok() ? Items.LIME_CONCRETE : Items.RED_CONCRETE,
                    last.ok() ? "Reloaded" : "Did not reload",
                    last.ok() ? ChatFormatting.GREEN : ChatFormatting.RED,
                    lines));
            // Also into chat, since a tooltip cannot be scrolled and some of these run long.
            for (String l : last.lines()) {
                if (!l.isBlank()) {
                    player.sendSystemMessage(Component.literal("§8  " + l));
                }
            }
        }

        GuiKit.footer(gui, SIZE, player, () -> AdminPanel.open(player));
        gui.open();
    }

    /** Chat only, for the command form. */
    public static void runData(ServerPlayer player) {
        HotReloadManager.Result r = HotReloadManager.reloadData();
        say(player, r.ok() ? "§aReloaded." : "§cReloaded with problems.", r.lines());
        HotReloadManager.announce(player, "Server data reloaded");
    }

    public static void runCode(ServerPlayer player) {
        HotReloadManager.Result r = HotReloadManager.reloadClasses();
        say(player, r.ok() ? "§aCode swapped." : "§cCode not swapped.", r.lines());
        if (r.ok()) HotReloadManager.announce(player, "Server code reloaded");
    }

    private static void say(ServerPlayer player, String head, List<String> lines) {
        player.sendSystemMessage(Component.literal("§b⟳ " + head));
        for (String l : lines) {
            if (!l.isBlank()) player.sendSystemMessage(Component.literal("§8  " + l));
        }
    }
}
