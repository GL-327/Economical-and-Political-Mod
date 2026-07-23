package com.political;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

/** /changelog opens the version picker. The subcommands back the clickable chat prompts. */
public final class ChangelogCommands {

    private ChangelogCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("changelog")
                .executes(ctx -> {
                    ChangelogGui.open(ctx.getSource().getPlayerOrException());
                    return 1;
                })
                .then(Commands.literal("show")
                        .then(Commands.argument("version", StringArgumentType.word())
                                .executes(ctx -> {
                                    ChangelogManager.printVersion(ctx.getSource().getPlayerOrException(),
                                            StringArgumentType.getString(ctx, "version"));
                                    return 1;
                                })))
                .then(Commands.literal("detail")
                        .then(Commands.argument("key", StringArgumentType.word())
                                .executes(ctx -> {
                                    ChangelogManager.printDetail(ctx.getSource().getPlayerOrException(),
                                            StringArgumentType.getString(ctx, "key"));
                                    return 1;
                                }))));
    }
}
