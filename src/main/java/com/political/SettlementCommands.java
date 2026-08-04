package com.political;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Locale;

/** /settlement, /atlas, /council — the V4 civic command surface. */
public final class SettlementCommands {

    private SettlementCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(Commands.literal("settlement")
                .executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    Settlement s = here(p);
                    if (s == null) {
                        p.sendSystemMessage(Component.literal(
                                "§7You are not standing in a settlement. §8Try /settlement list"));
                        return 0;
                    }
                    SettlementGui.open(p, s);
                    return 1;
                })
                .then(Commands.literal("info").executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    Settlement s = here(p);
                    if (s == null) {
                        s = SettlementManager.getRegisteredSettlement(p.getUUID().toString());
                    }
                    if (s == null) {
                        p.sendSystemMessage(Component.literal("§7No settlement here and you are not registered."));
                        return 0;
                    }
                    printInfo(p, s);
                    return 1;
                }))
                .then(Commands.literal("map").executes(ctx -> {
                    AtlasMapGui.open(ctx.getSource().getPlayerOrException());
                    return 1;
                }))
                .then(Commands.literal("economy").executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    Settlement s = here(p);
                    if (s == null) s = SettlementManager.getRegisteredSettlement(p.getUUID().toString());
                    if (s == null) {
                        p.sendSystemMessage(Component.literal("§cNo settlement here."));
                        return 0;
                    }
                    SettlementEconomyGui.open(p, s);
                    return 1;
                }))
                .then(Commands.literal("appeal").executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    Settlement s = here(p);
                    if (s == null) {
                        p.sendSystemMessage(Component.literal("§cNo settlement here."));
                        return 0;
                    }
                    p.sendSystemMessage(Component.literal("§6§l━━━ " + s.name + " — Appeal ━━━"));
                    p.sendSystemMessage(Component.literal("§7Total: §f" + s.appeal));
                    if (s.appealByCategory != null && !s.appealByCategory.isEmpty()) {
                        s.appealByCategory.forEach((cat, val) ->
                                p.sendSystemMessage(Component.literal("  §7" + cat + ": §f" + val)));
                    }
                    p.sendSystemMessage(Component.literal("§7Developed area: §f" + s.footprintCells
                            + " §7cells §8(spreads appeal, prevents tower-stacking)"));
                    p.sendSystemMessage(Component.literal("§7Beds: §f" + s.beds
                            + " §7sheltered §8of " + s.rawBeds + " total"));
                    p.sendSystemMessage(Component.literal(
                            "§8Each category is capped — variety beats stacking one block."));
                    return 1;
                }))
                .then(Commands.literal("list").executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    var all = SettlementManager.getAllSettlements();
                    if (all.isEmpty()) {
                        p.sendSystemMessage(Component.literal("§7No settlements have been founded yet."));
                        p.sendSystemMessage(Component.literal("§8Place a Cornerstone to found one."));
                        return 1;
                    }
                    p.sendSystemMessage(Component.literal("§6§lSettlements §7(" + all.size() + ")"));
                    for (Settlement s : all) {
                        SettlementTier t = s.tierEnum();
                        p.sendSystemMessage(Component.literal("  " + t.colorCode + s.name
                                + " §8[" + t.displayName + "] §7pop " + s.population
                                + " §8| appeal " + s.appeal));
                    }
                    return 1;
                }))
                .then(Commands.literal("register").executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    Settlement s = here(p);
                    if (s == null) {
                        p.sendSystemMessage(Component.literal("§cStand inside a settlement to register."));
                        return 0;
                    }
                    SettlementManager.registerPlayer(p, s);
                    return 1;
                }))
                .then(Commands.literal("leave").executes(ctx -> {
                    SettlementManager.leaveSettlement(ctx.getSource().getPlayerOrException());
                    return 1;
                }))
                .then(Commands.literal("rename")
                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                                    Settlement s = here(p);
                                    if (s == null) {
                                        p.sendSystemMessage(Component.literal("§cStand inside your settlement."));
                                        return 0;
                                    }
                                    SettlementManager.renameSettlement(p, s,
                                            StringArgumentType.getString(ctx, "name"));
                                    return 1;
                                })))
                .then(Commands.literal("scan")
                        .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                        .executes(ctx -> {
                            ServerPlayer p = ctx.getSource().getPlayerOrException();
                            Settlement s = here(p);
                            if (s == null) {
                                p.sendSystemMessage(Component.literal("§cNo settlement here."));
                                return 0;
                            }
                            SettlementManager.rescanNow((ServerLevel) p.level(), s);

                            p.sendSystemMessage(Component.literal("§aRescanned §f" + s.name
                                    + " §7— beds " + s.beds + ", appeal " + s.appeal
                                    + ", tier " + s.tierEnum().displayName));
                            return 1;
                        }))
                .then(Commands.literal("cornerstone")
                        .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                        .executes(ctx -> {
                            ServerPlayer p = ctx.getSource().getPlayerOrException();
                            p.getInventory().add(CornerstoneItem.create());
                            p.sendSystemMessage(Component.literal("§aGave a Cornerstone."));
                            return 1;
                        })));

        // /atlas hands over the map itself. The table is there for choosing a different sheet or
        // finding a settlement by name, but nobody should have to open a screen to get a map.
        dispatcher.register(Commands.literal("atlas")
                .executes(ctx -> {
                    AtlasMap.give(ctx.getSource().getPlayerOrException(),
                            AtlasCartographer.Zoom.REGION);
                    return 1;
                })
                .then(Commands.literal("table").executes(ctx -> {
                    AtlasMapGui.open(ctx.getSource().getPlayerOrException());
                    return 1;
                }))
                .then(Commands.literal("local").executes(ctx -> {
                    AtlasMap.give(ctx.getSource().getPlayerOrException(),
                            AtlasCartographer.Zoom.LOCAL);
                    return 1;
                }))
                .then(Commands.literal("region").executes(ctx -> {
                    AtlasMap.give(ctx.getSource().getPlayerOrException(),
                            AtlasCartographer.Zoom.REGION);
                    return 1;
                }))
                .then(Commands.literal("realm").executes(ctx -> {
                    AtlasMap.give(ctx.getSource().getPlayerOrException(),
                            AtlasCartographer.Zoom.REALM);
                    return 1;
                }))
                .then(Commands.literal("redraw").executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    int n = AtlasMap.refreshHeld(p);
                    p.sendSystemMessage(Component.literal(n == 0
                            ? "§7You are not carrying an atlas. §8Try /atlas"
                            : "§a" + n + " sheet" + (n == 1 ? "" : "s") + " redrawn."));
                    return 1;
                }))
                .then(Commands.literal("resurvey")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .executes(ctx -> {
                            AtlasCartographer.forget();
                            ctx.getSource().sendSuccess(() -> Component.literal(
                                    "§aSurveyed land forgotten. The next atlas re-reads the world."),
                                    true);
                            return 1;
                        })));

        dispatcher.register(Commands.literal("council")
                .executes(ctx -> {
                    HighCouncilGui.open(ctx.getSource().getPlayerOrException());
                    return 1;
                })
                .then(Commands.literal("status").executes(ctx -> {
                    HighCouncilManager.printStatus(ctx.getSource().getPlayerOrException());
                    return 1;
                }))
                .then(Commands.literal("enact")
                        .then(Commands.argument("edict", StringArgumentType.word())
                                .executes(ctx -> {
                                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                                    String want = StringArgumentType.getString(ctx, "edict");
                                    HighCouncilManager.Edict e = HighCouncilManager.Edict.byName(want);
                                    if (e == null) {
                                        p.sendSystemMessage(Component.literal("§cUnknown edict. Options: §7"
                                                + HighCouncilManager.edictNames()));
                                        return 0;
                                    }
                                    HighCouncilManager.enactEdict(p, e);
                                    return 1;
                                })))
                .then(Commands.literal("repeal")
                        .then(Commands.argument("edict", StringArgumentType.word())
                                .executes(ctx -> {
                                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                                    HighCouncilManager.Edict e = HighCouncilManager.Edict.byName(
                                            StringArgumentType.getString(ctx, "edict"));
                                    if (e == null) {
                                        p.sendSystemMessage(Component.literal("§cUnknown edict."));
                                        return 0;
                                    }
                                    HighCouncilManager.repealEdict(p, e);
                                    return 1;
                                })))
                .then(Commands.literal("appoint")
                        .then(Commands.argument("ministry", StringArgumentType.word())
                                .then(Commands.argument("player", StringArgumentType.word())
                                        .executes(ctx -> {
                                            ServerPlayer p = ctx.getSource().getPlayerOrException();
                                            String mName = StringArgumentType.getString(ctx, "ministry")
                                                    .toUpperCase(Locale.ROOT);
                                            String target = StringArgumentType.getString(ctx, "player");
                                            HighCouncilManager.Ministry ministry;
                                            try {
                                                ministry = HighCouncilManager.Ministry.valueOf(mName);
                                            } catch (IllegalArgumentException e) {
                                                p.sendSystemMessage(Component.literal(
                                                        "§cMinistries: treasury, defense, arcana, commerce"));
                                                return 0;
                                            }
                                            ServerPlayer targetPlayer = ((ServerLevel) p.level()).getServer() == null ? null
                                                    : ((ServerLevel) p.level()).getServer().getPlayerList().getPlayerByName(target);
                                            if (targetPlayer == null) { p.sendSystemMessage(Component.literal("§cThat player must be online.")); return 0; }
                                            String uuid = targetPlayer.getUUID().toString();
                                            if (false)
                                                    { }

                                            if (uuid == null) {
                                                p.sendSystemMessage(Component.literal("§cUnknown player: " + target));
                                                return 0;
                                            }
                                            HighCouncilManager.appointMinister(p, ministry, uuid);
                                            return 1;
                                        })))));
    }

    private static Settlement here(ServerPlayer p) {
        return SettlementManager.getContaining(
                p.level().dimension().identifier().toString(), p.blockPosition());
    }

    private static void printInfo(ServerPlayer p, Settlement s) {
        SettlementTier t = s.tierEnum();
        p.sendSystemMessage(Component.literal("§6§l━━━ " + s.name + " ━━━"));
        p.sendSystemMessage(Component.literal("§7Tier: " + t.colorCode + t.displayName));
        p.sendSystemMessage(Component.literal("§7Population §f" + s.population
                + " §8| §7Beds §f" + s.beds + " §8| §7Appeal §f" + s.appeal));
        p.sendSystemMessage(Component.literal("§7Treasury: §6" + s.treasury));
        SettlementTier next = t.next();
        if (next != null) {
            p.sendSystemMessage(Component.literal("§7Next (" + next.colorCode + next.displayName
                    + "§7): beds " + next.minBeds + ", pop " + next.minPop
                    + ", appeal " + next.minAppeal
                    + (next.minTreasury > 0 ? ", treasury " + next.minTreasury : "")));
        }
        if (s.leaders != null && !s.leaders.isEmpty()) {
            s.leaders.forEach((role, uuid) -> p.sendSystemMessage(
                    Component.literal("§7" + role + ": §f" + DataManager.getPlayerName(uuid))));
        }
    }
}
