package com.political;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * /backrooms [enter|exit|locate|info]  (+ admin: /backrooms regen)
 */
public final class BackroomsCommands {

    private BackroomsCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("backrooms")
                // Bare command opens the GUI; everything here is clickable.
                .executes(ctx -> {
                    BackroomsGui.open(ctx.getSource().getPlayerOrException());
                    return 1;
                })
                .then(Commands.literal("info")
                        .executes(ctx -> { info(ctx.getSource()); return 1; }))
                .then(Commands.literal("enter")
                        .executes(ctx -> {
                            ServerPlayer p = ctx.getSource().getPlayerOrException();
                            if (BackroomsManager.isBackrooms(p.level())) {
                                p.sendSystemMessage(Component.literal("§7You are already in the Backrooms.").withStyle(ChatFormatting.GRAY));
                                return 0;
                            }
                            BackroomsManager.enterBackrooms(p);
                            return 1;
                        }))
                .then(Commands.literal("exit")
                        .executes(ctx -> {
                            ServerPlayer p = ctx.getSource().getPlayerOrException();
                            if (!BackroomsManager.isBackrooms(p.level())) {
                                p.sendSystemMessage(Component.literal("§7You are not in the Backrooms.").withStyle(ChatFormatting.GRAY));
                                return 0;
                            }
                            BackroomsManager.leave(p);
                            return 1;
                        }))
                .then(Commands.literal("locate")
                        .executes(ctx -> {
                            ServerPlayer p = ctx.getSource().getPlayerOrException();
                            if (!BackroomsManager.isBackrooms(p.level())) {
                                p.sendSystemMessage(Component.literal("§7You are not in the Backrooms.").withStyle(ChatFormatting.GRAY));
                                return 0;
                            }
                            BlockPos exit = BackroomsManager.nearestExit(p);
                            if (exit == null) {
                                p.sendSystemMessage(Component.literal("§7No exit portal discovered yet. Keep wandering...").withStyle(ChatFormatting.GRAY));
                            } else {
                                p.sendSystemMessage(Component.literal("§bNearest known exit: §f"
                                        + exit.getX() + ", " + exit.getY() + ", " + exit.getZ()).withStyle(ChatFormatting.AQUA));
                            }
                            return 1;
                        }))
                .then(Commands.literal("regen")
                        .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                        .executes(ctx -> {
                            ServerPlayer p = ctx.getSource().getPlayerOrException();
                            if (!BackroomsManager.isBackrooms(p.level())) {
                                p.sendSystemMessage(Component.literal("§cStand inside the Backrooms to regen.").withStyle(ChatFormatting.RED));
                                return 0;
                            }
                            int cleared = BackroomsManager.regenAround(p);
                            p.sendSystemMessage(Component.literal("§a[ADMIN] Cleared " + cleared + " cells; they will re-stamp.").withStyle(ChatFormatting.GREEN));
                            return 1;
                        })));
    }

    private static void info(CommandSourceStack source) {
        source.sendSystemMessage(Component.literal("§e=== The Backrooms ===").withStyle(ChatFormatting.GOLD));
        source.sendSystemMessage(Component.literal("§7An endless maze of humming rooms."));
        source.sendSystemMessage(Component.literal("§e/backrooms enter §7- no-clip in"));
        source.sendSystemMessage(Component.literal("§e/backrooms exit §7- step back out (no reward)"));
        source.sendSystemMessage(Component.literal("§e/backrooms locate §7- nearest known exit portal"));
        source.sendSystemMessage(Component.literal("§7Escape via a §bexit portal§7 or §bEscape Beacon§7 for rewards."));
        source.sendSystemMessage(Component.literal("§8Generated cells: " + BackroomsManager.getGeneratedCellCount()
                + " | Known exits: " + BackroomsManager.getKnownExitCount()));
    }
}
