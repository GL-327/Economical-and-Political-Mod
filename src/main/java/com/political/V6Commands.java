package com.political;


import com.political.settlement.SettlementEventGui;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public final class V6Commands {

    private V6Commands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("world").executes(ctx -> {
            VarietyGui.open(ctx.getSource().getPlayerOrException());
            return 1;
        }));
        dispatcher.register(Commands.literal("events").executes(ctx -> {
            SettlementEventGui.open(ctx.getSource().getPlayerOrException());
            return 1;
        }));
    }
}
