package com.political;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

/** /clan opens the clan management screen. Everything is clickable from there. */
public final class ClanCommands {

    private ClanCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("clan").executes(ctx -> {
            ClanGui.open(ctx.getSource().getPlayerOrException());
            return 1;
        }));
        dispatcher.register(Commands.literal("clans").executes(ctx -> {
            ClanGui.openDirectory(ctx.getSource().getPlayerOrException(), 0);
            return 1;
        }));
    }
}
