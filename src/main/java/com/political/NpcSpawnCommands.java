package com.political;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * Admin commands that place and remove the Auction Master and Stock Broker NPCs. Moved unchanged out of
 * the first command callback in PoliticalServer (M1.8).
 */
final class NpcSpawnCommands {

    private NpcSpawnCommands() {
    }

    static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // /placeauctionmaster lives in CommandRegistry.registerPlaceAuctionMaster. This class used to register
        // the same root first with the same requirement and shape; the later registration's executors were the
        // ones that ran, so the earlier copy was dropped in M1.8.

        // /removeauctionmaster command
        dispatcher.register(Commands.literal("removeauctionmaster")
                .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    ServerLevel world = context.getSource().getLevel();

                    if (AuctionMasterManager.removeAuctionMaster(world, player, 5.0)) {
                        context.getSource().sendSuccess(() ->
                                Component.literal("✓ Removed nearby Auction Master!").withStyle(ChatFormatting.GREEN), true);
                        return 1;
                    } else {
                        context.getSource().sendSuccess(() ->
                                Component.literal("✗ No Auction Master found within 5 blocks.").withStyle(ChatFormatting.RED), false);
                        return 0;
                    }
                })
                .then(Commands.literal("all")
                        .executes(context -> {
                            ServerLevel world = context.getSource().getLevel();
                            int removed = AuctionMasterManager.removeAllAuctionMasters(world);

                            if (removed > 0) {
                                int finalRemoved = removed;
                                context.getSource().sendSuccess(() ->
                                        Component.literal("✓ Removed " + finalRemoved + " Auction Master(s)!").withStyle(ChatFormatting.GREEN), true);
                            } else {
                                context.getSource().sendSuccess(() ->
                                        Component.literal("✗ No Auction Masters found in this dimension.").withStyle(ChatFormatting.RED), false);
                            }
                            return removed;
                        })
                )
        );

        // /placestockbroker command
        dispatcher.register(Commands.literal("placestockbroker")
                .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    ServerLevel world = context.getSource().getLevel();
                    StockBrokerManager.spawnStockBroker(world, player.getX(), player.getY(), player.getZ(), player.getYRot());
                    context.getSource().sendSuccess(() ->
                            Component.literal("✓ Spawned Stock Broker!").withStyle(ChatFormatting.BLUE), true);
                    return 1;
                })
                .then(Commands.argument("x", DoubleArgumentType.doubleArg())
                        .then(Commands.argument("y", DoubleArgumentType.doubleArg())
                                .then(Commands.argument("z", DoubleArgumentType.doubleArg())
                                        .executes(context -> {
                                            double x = DoubleArgumentType.getDouble(context, "x");
                                            double y = DoubleArgumentType.getDouble(context, "y");
                                            double z = DoubleArgumentType.getDouble(context, "z");
                                            ServerLevel world = context.getSource().getLevel();
                                            StockBrokerManager.spawnStockBroker(world, x, y, z, 0);
                                            context.getSource().sendSuccess(() ->
                                                    Component.literal("✓ Spawned Stock Broker at " + String.format("%.1f, %.1f, %.1f", x, y, z))
                                                            .withStyle(ChatFormatting.BLUE), true);
                                            return 1;
                                        })
                                )
                        )
                )
        );

        // /removestockbroker command
        dispatcher.register(Commands.literal("removestockbroker")
                .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    ServerLevel world = context.getSource().getLevel();

                    if (StockBrokerManager.removeStockBroker(world, player, 5.0)) {
                        context.getSource().sendSuccess(() ->
                                Component.literal("✓ Removed nearby Stock Broker!").withStyle(ChatFormatting.BLUE), true);
                        return 1;
                    } else {
                        context.getSource().sendSuccess(() ->
                                Component.literal("✗ No Stock Broker found within 5 blocks.").withStyle(ChatFormatting.RED), false);
                        return 0;
                    }
                })
                .then(Commands.literal("all")
                        .executes(context -> {
                            ServerLevel world = context.getSource().getLevel();
                            int removed = StockBrokerManager.removeAllStockBrokers(world);

                            if (removed > 0) {
                                int finalRemoved = removed;
                                context.getSource().sendSuccess(() ->
                                        Component.literal("✓ Removed " + finalRemoved + " Stock Broker(s)!").withStyle(ChatFormatting.BLUE), true);
                            } else {
                                context.getSource().sendSuccess(() ->
                                        Component.literal("✗ No Stock Brokers found in this dimension.").withStyle(ChatFormatting.RED), false);
                            }
                            return removed;
                        })
                )
        );
    }
}
