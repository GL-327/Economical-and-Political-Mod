package com.political;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

/** /arsenal opens the Cursed Arsenal forge board. */
public final class CursedArsenalCommands {

    private CursedArsenalCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("arsenal").executes(ctx -> {
            CursedArsenalGui.open(ctx.getSource().getPlayerOrException());
            return 1;
        }));
    }
}
