package com.political;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * V5 Pillar 1 — commands for the Expedition system.
 *
 * <p>Everything here is a convenience. {@code /expedition} with no arguments simply opens
 * {@link ExpeditionGui}, and every subcommand below has an equivalent button in that GUI,
 * so the system is fully playable without ever typing a command.
 */
public final class ExpeditionCommands {

    private ExpeditionCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("expedition")
                .executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    ExpeditionGui.open(p);
                    return 1;
                })
                .then(Commands.literal("gui")
                        .executes(ctx -> {
                            ServerPlayer p = ctx.getSource().getPlayerOrException();
                            ExpeditionGui.open(p);
                            return 1;
                        }))
                .then(Commands.literal("status")
                        .executes(ctx -> {
                            ServerPlayer p = ctx.getSource().getPlayerOrException();
                            status(p);
                            return 1;
                        }))
                .then(Commands.literal("abandon")
                        .executes(ctx -> {
                            ServerPlayer p = ctx.getSource().getPlayerOrException();
                            return ExpeditionRunManager.abandon(p) ? 1 : 0;
                        }))
                .then(Commands.literal("admin")
                        .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                        .then(Commands.literal("cleanup")
                                .executes(ctx -> {
                                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                                    MinecraftServer server = ((ServerLevel) p.level()).getServer();
                                    int n = ExpeditionRunManager.forceCleanupAll(server);
                                    p.sendSystemMessage(Component.literal("§aEnded " + n
                                            + " run(s) and wiped every expedition instance."));
                                    return 1;
                                }))
                        .then(Commands.literal("resetcooldown")
                                .executes(ctx -> {
                                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                                    ExpeditionRunManager.clearCooldown(p.getUUID().toString());
                                    p.sendSystemMessage(Component.literal("§aExpedition cooldown cleared."));
                                    return 1;
                                }))));
    }

    private static void status(ServerPlayer p) {
        String uuid = p.getUUID().toString();
        ExpeditionDungeon run = ExpeditionRunManager.runOf(uuid);

        p.sendSystemMessage(Component.literal("─── Expeditions ───")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));

        if (run == null) {
            long cd = ExpeditionRunManager.cooldownRemaining(uuid);
            p.sendSystemMessage(Component.literal("§7No active run."));
            p.sendSystemMessage(Component.literal("§7Cooldown: "
                    + (cd <= 0 ? "§aready" : "§c" + ExpeditionRunManager.formatDuration(cd))));
            p.sendSystemMessage(Component.literal("§7Keys: §e" + ExpeditionRewards.countKeys(p)
                    + "   §7Sigils: §b" + ExpeditionRewards.countSigils(p)));
        } else {
            ExpeditionRunManager.Tier t = ExpeditionRunManager.Tier.byName(run.tier);
            long left = Math.max(0L, run.deadlineAt - System.currentTimeMillis());
            p.sendSystemMessage(Component.literal("§7Tier: ")
                    .append(Component.literal(t.displayName).withStyle(t.color)));
            p.sendSystemMessage(Component.literal("§7Floor §f" + run.floor + "§7/§f" + run.totalFloors
                    + "   §7Rooms §f" + run.roomsCleared + "§7/§f" + run.roomsTotalThisFloor));
            p.sendSystemMessage(Component.literal("§7Time left: §c"
                    + ExpeditionRunManager.formatDuration(left)));
        }
        p.sendSystemMessage(Component.literal("§8Open the GUI with /expedition or the Civilization hub."));
    }
}
