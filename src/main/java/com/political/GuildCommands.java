package com.political;
import com.political.government.Treasury;



import com.political.settlement.Settlement;
import com.political.settlement.SettlementManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * /guild — a shortcut to {@link GuildGui}. Nothing in Pillar 2 <i>requires</i> a command:
 * every subcommand here has a clickable equivalent inside the GUI.
 */
public final class GuildCommands {

    private GuildCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(Commands.literal("guild")
                .executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    GuildGui.open(p);
                    return 1;
                })
                .then(Commands.literal("info").executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    Guild g = GuildManager.getGuildOf(p);
                    if (g == null) {
                        p.sendSystemMessage(Component.literal("§7You are not in a guild. §8Use /guild to found one."));
                        return 0;
                    }
                    p.sendSystemMessage(Component.literal("§6" + g.name + " " + g.coloredTag()));
                    p.sendSystemMessage(Component.literal("§7Level §b" + g.level
                            + " §7• Members §f" + g.memberCount()
                            + " §7• Treasury §6" + GuildManager.fmt(g.treasury)));
                    Settlement hall = g.hallSettlementId == null ? null : SettlementManager.get(g.hallSettlementId);
                    p.sendSystemMessage(Component.literal("§7Hall: §f" + (hall == null ? "§8none" : hall.name)));
                    p.sendSystemMessage(Component.literal("§7Your rank: "
                            + (g.rankOf(p.getUUID().toString()) == null ? "§8?"
                            : g.rankOf(p.getUUID().toString()).colored())));
                    return 1;
                }))
                .then(Commands.literal("roster").executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    GuildGui.openRoster(p, 0);
                    return 1;
                }))
                .then(Commands.literal("treasury").executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    GuildGui.openTreasury(p);
                    return 1;
                }))
                .then(Commands.literal("perks").executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    GuildGui.openPerks(p);
                    return 1;
                }))
                .then(Commands.literal("log").executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    GuildGui.openLog(p, 0);
                    return 1;
                }))
                .then(Commands.literal("hall").executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    GuildGui.openHall(p);
                    return 1;
                }))
                .then(Commands.literal("invites").executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    GuildGui.openInvitations(p);
                    return 1;
                }))
                .then(Commands.literal("list").executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    GuildGui.openDirectory(p, 0);
                    return 1;
                }))
                .then(Commands.literal("invite")
                        .then(Commands.argument("player", StringArgumentType.word()).executes(ctx -> {
                            ServerPlayer p = ctx.getSource().getPlayerOrException();
                            String targetName = StringArgumentType.getString(ctx, "player");
                            ServerPlayer target = PoliticalServer.server == null ? null
                                    : PoliticalServer.server.getPlayerList().getPlayerByName(targetName);
                            if (target == null) {
                                p.sendSystemMessage(Component.literal("§cThat player is not online."));
                                return 0;
                            }
                            return GuildManager.invite(p, target) ? 1 : 0;
                        })))
                .then(Commands.literal("leave").executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    return GuildManager.leave(p) ? 1 : 0;
                }))
                // ── operator tools ──
                .then(Commands.literal("admin")
                        .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                        .then(Commands.literal("charter").executes(ctx -> {
                            ServerPlayer p = ctx.getSource().getPlayerOrException();
                            GuildItems.giveCharter(p);
                            p.sendSystemMessage(Component.literal("§a✓ Guild Charter granted.")
                                    .withStyle(ChatFormatting.GREEN));
                            return 1;
                        }))
                        .then(Commands.literal("disband")
                                .then(Commands.argument("guild", StringArgumentType.greedyString()).executes(ctx -> {
                                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                                    String name = StringArgumentType.getString(ctx, "guild");
                                    Guild g = GuildManager.byName(name);
                                    if (g == null) g = GuildManager.byTag(name);
                                    if (g == null) {
                                        p.sendSystemMessage(Component.literal("§cNo guild by that name or tag."));
                                        return 0;
                                    }
                                    return GuildManager.disband(p, g) ? 1 : 0;
                                })))
                        .then(Commands.literal("save").executes(ctx -> {
                            GuildManager.save();
                            ctx.getSource().sendSuccess(
                                    () -> Component.literal("§a✓ guilds.json saved."), false);
                            return 1;
                        })))
        );
    }
}
