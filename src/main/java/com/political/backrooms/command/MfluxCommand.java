package com.political.backrooms.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.political.backrooms.fracture3.Fracture3DimensionManager;
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
                .then(CommandManager.argument("number", IntegerArgumentType.integer(3, 3))
                    .executes(ctx -> {
                        ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                        Fracture3DimensionManager.teleportToFracture3(player);
                        ctx.getSource().sendFeedback(() ->
                            Text.literal("Teleporting to Fracture 3...")
                                .formatted(Formatting.GOLD),
                            true);
                        return 1;
                    })
                )
            )
        );
        
        // Direct command to enter Fracture 3
        dispatcher.register(CommandManager.literal("fracture3")
            .requires(CommandManager.requirePermissionLevel(CommandManager.OWNERS_CHECK))
            .executes(ctx -> {
                ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                Fracture3DimensionManager.teleportToFracture3(player);
                ctx.getSource().sendFeedback(() ->
                    Text.literal("Teleporting to Fracture 3...")
                        .formatted(Formatting.GOLD),
                    true);
                return 1;
            })
        );
    }
}
