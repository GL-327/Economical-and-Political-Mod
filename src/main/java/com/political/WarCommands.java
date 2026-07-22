package com.political;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/** /war — declare, debate and end wars between sovereign polities. */
public final class WarCommands {

    private WarCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // Everything here is reachable by clicking; the bare command opens the War Room GUI.
        dispatcher.register(Commands.literal("civ").executes(ctx -> {
            CivilizationHubGui.open(ctx.getSource().getPlayerOrException());
            return 1;
        }));
        dispatcher.register(Commands.literal("civilization").executes(ctx -> {
            CivilizationHubGui.open(ctx.getSource().getPlayerOrException());
            return 1;
        }));

        dispatcher.register(Commands.literal("war")
                .executes(ctx -> {
                    WarGui.open(ctx.getSource().getPlayerOrException());
                    return 1;
                })
                .then(Commands.literal("status").executes(ctx -> {
                    WarManager.printStatus(ctx.getSource().getPlayerOrException());
                    return 1;
                }))
                .then(Commands.literal("council").executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    Settlement polity = WarManager.polityOf(p);
                    if (polity == null) {
                        p.sendSystemMessage(Component.literal("§cYou belong to no polity."));
                        return 0;
                    }
                    WarManager.printCouncil(p, polity);
                    return 1;
                }))
                .then(Commands.literal("shadow").executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    Settlement polity = WarManager.polityOf(p);
                    if (polity == null) {
                        p.sendSystemMessage(Component.literal("§cYou belong to no polity."));
                        return 0;
                    }
                    ShadowGovernmentManager.printRoster(p, polity);
                    return 1;
                }))
                .then(Commands.literal("declare")
                        .then(Commands.argument("polity", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                                    String want = StringArgumentType.getString(ctx, "polity");
                                    Settlement target = findByName(want);
                                    if (target == null) {
                                        p.sendSystemMessage(Component.literal(
                                                "§cNo settlement named '" + want + "'. §8/settlement list"));
                                        return 0;
                                    }
                                    WarManager.proposeWar(p, target);
                                    return 1;
                                })))
                .then(Commands.literal("vote")
                        .then(Commands.literal("yes").executes(ctx -> {
                            WarManager.castVote(ctx.getSource().getPlayerOrException(), true);
                            return 1;
                        }))
                        .then(Commands.literal("no").executes(ctx -> {
                            WarManager.castVote(ctx.getSource().getPlayerOrException(), false);
                            return 1;
                        })))
                .then(Commands.literal("peace").executes(ctx -> {
                    WarManager.proposePeace(ctx.getSource().getPlayerOrException());
                    return 1;
                })));
    }

    private static Settlement findByName(String name) {
        for (Settlement s : SettlementManager.getAllSettlements()) {
            if (s.name != null && s.name.equalsIgnoreCase(name)) return s;
        }
        for (Settlement s : SettlementManager.getAllSettlements()) {
            if (s.name != null && s.name.toLowerCase().contains(name.toLowerCase())) return s;
        }
        return null;
    }
}
