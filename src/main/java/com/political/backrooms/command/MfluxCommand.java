package com.political.backrooms.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.political.backrooms.BackroomsDimensionManager;
import com.political.backrooms.testing.BackroomsTestSuite;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class MfluxCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("mflux")
            .requires(CommandManager.requirePermissionLevel(CommandManager.OWNERS_CHECK))
            .then(CommandManager.literal("fracture")
                .then(CommandManager.argument("number", IntegerArgumentType.integer(1, 3))
                    .executes(ctx -> {
                        ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                        int fracture = IntegerArgumentType.getInteger(ctx, "number");
                        BackroomsDimensionManager.teleportToFracture(player, fracture);
                        ctx.getSource().sendFeedback(() ->
                            Text.literal("Teleporting to Fracture " + fracture + "...")
                                .formatted(Formatting.GOLD),
                            true);
                        return 1;
                    })
                )
            )
        );
        
        // Add test commands
        dispatcher.register(CommandManager.literal("backrooms-test")
            .requires(CommandManager.requirePermissionLevel(CommandManager.OWNERS_CHECK))
            .executes(ctx -> {
                ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                BackroomsTestSuite.runFullTestSuite(player);
                ctx.getSource().sendFeedback(() ->
                    Text.literal("Backrooms test suite completed!")
                        .formatted(Formatting.GREEN),
                    true);
                return 1;
            })
        );
        
        dispatcher.register(CommandManager.literal("backrooms-health")
            .requires(CommandManager.requirePermissionLevel(CommandManager.OWNERS_CHECK))
            .executes(ctx -> {
                ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                BackroomsTestSuite.quickHealthCheck(player);
                ctx.getSource().sendFeedback(() ->
                    Text.literal("Health check completed!")
                        .formatted(Formatting.GREEN),
                    true);
                return 1;
            })
        );
    }
}
