package com.political;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

/**
 * Command entry points for the V6 systems. Every one opens a GUI, so nothing here is ever
 * required to type. They exist as convenient shortcuts to the same screens the hub links.
 */
public final class V6Commands {

    private V6Commands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("curses").executes(ctx -> {
            CursedSpiritGui.open(ctx.getSource().getPlayerOrException());
            return 1;
        }));

        dispatcher.register(Commands.literal("sorcerer").executes(ctx -> {
            SorcererGui.open(ctx.getSource().getPlayerOrException());
            return 1;
        }));

        dispatcher.register(Commands.literal("vows").executes(ctx -> {
            BindingVowGui.open(ctx.getSource().getPlayerOrException());
            return 1;
        }));

        dispatcher.register(Commands.literal("domainexpansion").executes(ctx -> {
            PlayerDomainExpansion.expand(ctx.getSource().getPlayerOrException());
            return 1;
        }));

        dispatcher.register(Commands.literal("specialgrade").executes(ctx -> {
            SpecialGradeGui.open(ctx.getSource().getPlayerOrException());
            return 1;
        }));

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
