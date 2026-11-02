package com.political;


import com.political.settlement.Settlement;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * V5 Pillar 3 commands. Everything the system does is reachable by clicking from
 * {@link TradeGui} (itself reachable from the Civilization Hub); these are conveniences only.
 */
public final class TradeCommands {

    private TradeCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("trade")
                .executes(ctx -> {
                    TradeGui.open(ctx.getSource().getPlayerOrException());
                    return 1;
                })
                .then(Commands.literal("routes").executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    Settlement home = CivilizationHubGui.currentOrHome(p);
                    if (home == null) {
                        p.sendSystemMessage(Component.literal("§7You belong to no settlement."));
                        return 0;
                    }
                    TradeGui.openRoutes(p, home);
                    return 1;
                }))
                .then(Commands.literal("caravans").executes(ctx -> {
                    TradeGui.openCaravans(ctx.getSource().getPlayerOrException(), 0);
                    return 1;
                }))
                .then(Commands.literal("market").executes(ctx -> {
                    MarketGui.open(ctx.getSource().getPlayerOrException());
                    return 1;
                }))
                .then(Commands.literal("give")
                        .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                        .then(Commands.literal("charter").executes(ctx -> {
                            ServerPlayer p = ctx.getSource().getPlayerOrException();
                            TradeItems.give(p, TradeItems.createCharter());
                            p.sendSystemMessage(Component.literal("§aGave a Caravan Charter."));
                            return 1;
                        }))
                        .then(Commands.literal("seal").executes(ctx -> {
                            ServerPlayer p = ctx.getSource().getPlayerOrException();
                            TradeItems.give(p, TradeItems.createMerchantSeal());
                            p.sendSystemMessage(Component.literal("§aGave a Merchant's Seal."));
                            return 1;
                        }))
                        .then(Commands.literal("marque").executes(ctx -> {
                            ServerPlayer p = ctx.getSource().getPlayerOrException();
                            TradeItems.give(p, TradeItems.createLetterOfMarque());
                            p.sendSystemMessage(Component.literal("§aGave a Letter of Marque."));
                            return 1;
                        })))
        );

        dispatcher.register(Commands.literal("market").executes(ctx -> {
            MarketGui.open(ctx.getSource().getPlayerOrException());
            return 1;
        }));

        dispatcher.register(Commands.literal("caravans").executes(ctx -> {
            TradeGui.openCaravans(ctx.getSource().getPlayerOrException(), 0);
            return 1;
        }));
    }
}
