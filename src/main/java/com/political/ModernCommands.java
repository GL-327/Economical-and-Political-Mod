package com.political;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

/**
 * V7 Pillar C — convenience commands for the featured board. Both {@code /featured} and
 * {@code /whatsnew} open {@link ModernShowcaseGui}. The board is also reachable from the
 * Civilization Hub, so neither command is ever required to use the feature.
 */
public final class ModernCommands {

    private ModernCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("featured")
                .executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    ModernShowcaseGui.open(p);
                    return 1;
                }));
        dispatcher.register(Commands.literal("whatsnew")
                .executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    ModernShowcaseGui.open(p);
                    return 1;
                }));
    }
}
