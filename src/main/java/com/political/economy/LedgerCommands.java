package com.political.economy;

import com.mojang.brigadier.CommandDispatcher;
import com.political.DataManager;
import com.political.core.economy.Ledger;
import com.political.core.text.Text;
import java.util.Map;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

/**
 * {@code /ledger}: for owners, the coins the server made and destroyed since it started, by reason, and the recent coin
 * transactions of one player with who asked for each (M3). Read only.
 */
public final class LedgerCommands {

    private LedgerCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("ledger")
                .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                .executes(context -> {
                    Ledger ledger = DataManager.LEDGER;
                    context.getSource().sendSuccess(() -> Text.tr("political.ledger.title", ledger.journalSize(), ledger.refusedDebits()), false);
                    for (Map.Entry<String, Long> e : ledger.netByReason().entrySet()) {
                        context.getSource().sendSuccess(() -> Text.plain("  " + e.getKey() + ": " + (e.getValue() >= 0 ? "+" : "") + e.getValue()), false);
                    }
                    return 1;
                })
                .then(Commands.literal("player")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> {
                                    ServerPlayer target = EntityArgument.getPlayer(context, "player");
                                    String uuid = target.getUUID().toString();
                                    context.getSource().sendSuccess(() -> Text.tr("political.ledger.player", Text.plain(target.getName().getString()),
                                            DataManager.LEDGER.balance(uuid)), false);
                                    for (Ledger.Entry e : DataManager.LEDGER.recentFor(uuid, 12)) {
                                        context.getSource().sendSuccess(() -> Text.plain("  " + e.kind() + " " + e.amount()
                                                + (e.applied() != e.amount() ? " (applied " + e.applied() + ")" : "")
                                                + " -> " + e.balanceAfter() + "  [" + e.reason() + ", " + e.source().substring(e.source().lastIndexOf('.') + 1) + "]"), false);
                                    }
                                    return 1;
                                }))));
    }
}
