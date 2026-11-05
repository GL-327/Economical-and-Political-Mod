package com.political.power.game;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.political.core.text.Text;
import com.political.power.model.PlayerPower;
import com.political.power.model.PowerCatalog;
import java.util.Locale;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@code /poweradmin}: shortcuts for the administration, so an admin can act on a player from the console or a
 * script. Everything here is also reachable from the admin panel ({@link PowerAdminScreens}); players reach their own
 * power through the hub ({@link PowerScreens}), not through a command. The whole command needs
 * {@link PowerPermissions#ADMIN}.
 */
public final class PowerCommands {

    private static final Logger LOGGER = LoggerFactory.getLogger(PowerCommands.class);

    private static final SuggestionProvider<CommandSourceStack> TECHNIQUES = (context, builder) -> SharedSuggestionProvider.suggest(
            PowerCatalog.techniques().stream().map(def -> def.id().toLowerCase(Locale.ROOT)), builder);

    private PowerCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // One requirement on the root covers every sub-command: Brigadier checks each node on the path.
        dispatcher.register(Commands.literal("poweradmin")
                .requires(PowerPermissions.adminCommand())
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayer();
                    if (player == null) {
                        return 0;
                    }
                    PowerAdminScreens.open(player, 0);
                    return 1;
                })
                .then(Commands.literal("info")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(PowerCommands::info)))
                .then(Commands.literal("grant")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("technique", StringArgumentType.word())
                                        .suggests(TECHNIQUES)
                                        .executes(PowerCommands::grant))))
                .then(Commands.literal("revoke")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("technique", StringArgumentType.word())
                                        .suggests(TECHNIQUES)
                                        .executes(PowerCommands::revoke))))
                .then(Commands.literal("awaken")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(PowerCommands::awaken)))
                .then(Commands.literal("sleep")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(PowerCommands::sleep)))
                .then(Commands.literal("reroll")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(PowerCommands::reroll)
                                .then(Commands.argument("technique", StringArgumentType.word())
                                        .suggests(TECHNIQUES)
                                        .executes(PowerCommands::rerollTo))))
                .then(Commands.literal("mastery")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("casts", IntegerArgumentType.integer(0, 100_000))
                                        .executes(PowerCommands::mastery))))
                .then(Commands.literal("charge")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(PowerCommands::charge)))
                .then(Commands.literal("reset")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.literal("confirm")
                                        .executes(PowerCommands::reset)))));
    }

    private static int reply(CommandContext<CommandSourceStack> context, Component message) {
        context.getSource().sendSuccess(() -> message, false);
        return 1;
    }

    private static int refuse(CommandContext<CommandSourceStack> context, Component message) {
        context.getSource().sendFailure(message);
        return 0;
    }

    /** The technique id the admin typed (any case), or null if there is no such technique. */
    private static String techniqueId(CommandContext<CommandSourceStack> context) {
        String id = StringArgumentType.getString(context, "technique").toUpperCase(Locale.ROOT);
        return PowerCatalog.isTechnique(id) ? id : null;
    }

    private static int info(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "player");
        String uuid = target.getUUID().toString();
        PlayerPower p = PowerService.state(uuid);
        String innate = p.innate;
        reply(context, Text.tr("political.power.admin.info.head", Text.plain(target.getName().getString()),
                innate == null ? Text.plain("-") : PowerService.techniqueName(innate),
                Text.tr(PowerService.awakened(uuid) ? "political.power.gui.state.awake" : "political.power.gui.state.asleep"),
                PowerService.tier(uuid), p.shards));
        for (PowerCatalog.TechniqueDef def : PowerService.heldTechniques(uuid)) {
            reply(context, Text.plain("  ").append(PowerService.techniqueName(def.id()))
                    .append(Text.tr(p.learned.contains(def.id()) ? "political.power.admin.info.learned" : "political.power.admin.info.tree")));
        }
        return 1;
    }

    private static int grant(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "player");
        String id = techniqueId(context);
        if (id == null) {
            return refuse(context, Text.tr("political.power.admin.unknown_technique"));
        }
        PowerService.LearnResult result = PowerService.learn(target, id);
        LOGGER.info("{} granted {} to {} by command: {}", context.getSource().getTextName(), id, target.getName().getString(), result);
        return reply(context, Text.trNamed("political.power.learn.result", result.name(), PowerService.techniqueName(id)));
    }

    private static int revoke(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "player");
        String id = techniqueId(context);
        if (id == null) {
            return refuse(context, Text.tr("political.power.admin.unknown_technique"));
        }
        String uuid = target.getUUID().toString();
        boolean had = PowerService.revoke(uuid, id);
        LOGGER.info("{} revoked {} from {} by command", context.getSource().getTextName(), id, target.getName().getString());
        Component name = PowerService.techniqueName(id);
        reply(context, Text.tr(had ? "political.power.admin.revoked" : "political.power.admin.revoke.nothing", name));
        if (had && PowerService.holds(uuid, id)) {
            reply(context, Text.tr("political.power.admin.revoke.still", name));
        }
        return had ? 1 : 0;
    }

    private static int awaken(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "player");
        boolean changed = PowerService.awaken(target);
        LOGGER.info("{} awakened {} by command: {}", context.getSource().getTextName(), target.getName().getString(), changed);
        return reply(context, Text.tr(changed ? "political.power.admin.awaken.done" : "political.power.admin.awaken.already"));
    }

    private static int sleep(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "player");
        PowerService.sleep(target.getUUID().toString());
        LOGGER.info("{} put the technique of {} to sleep by command", context.getSource().getTextName(), target.getName().getString());
        return reply(context, Text.tr("political.power.admin.sleep.done"));
    }

    private static int reroll(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "player");
        PowerService.RerollResult result = PowerService.rerollFree(target);
        LOGGER.info("{} rerolled {} by command: {}", context.getSource().getTextName(), target.getName().getString(), result);
        String innate = PowerService.state(target.getUUID().toString()).innate;
        return reply(context, Text.trNamed("political.power.reroll.result", result.name(), innate == null ? Text.plain("-") : PowerService.techniqueName(innate)));
    }

    private static int rerollTo(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "player");
        String id = techniqueId(context);
        if (id == null) {
            return refuse(context, Text.tr("political.power.admin.unknown_technique"));
        }
        PowerService.RerollResult result = PowerService.rerollTo(target, id);
        LOGGER.info("{} set the technique of {} to {} by command: {}", context.getSource().getTextName(), target.getName().getString(), id, result);
        return reply(context, Text.trNamed("political.power.reroll.result", result.name(), PowerService.techniqueName(id)));
    }

    private static int mastery(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "player");
        int casts = IntegerArgumentType.getInteger(context, "casts");
        PowerService.setUses(target.getUUID().toString(), casts);
        LOGGER.info("{} set the casts of {} to {} by command", context.getSource().getTextName(), target.getName().getString(), casts);
        return reply(context, Text.tr("political.power.admin.mastery.done", casts));
    }

    private static int charge(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "player");
        PowerService.grantRerollCharge(target);
        LOGGER.info("{} gave a reroll charge to {} by command", context.getSource().getTextName(), target.getName().getString());
        return reply(context, Text.tr("political.power.admin.charge.done"));
    }

    private static int reset(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "player");
        PowerService.reset(target.getUUID().toString());
        LOGGER.info("{} reset the power state of {} by command", context.getSource().getTextName(), target.getName().getString());
        return reply(context, Text.tr("political.power.admin.reset.done"));
    }
}
