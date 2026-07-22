package com.political;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * {@code /season}, {@code /bestiary} and {@code /hunt}.
 *
 * <p>Every bare command opens the matching GUI — the subcommands exist for operators and for
 * anyone who prefers typing, but nothing in this pillar <i>requires</i> a command. The Living
 * World is fully playable from {@link LivingWorldGui} and the three craftable keepsakes.</p>
 *
 * <p>Register from a {@code CommandRegistrationCallback}:
 * {@code LivingWorldCommands.register(dispatcher);}</p>
 */
public final class LivingWorldCommands {

    private LivingWorldCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        registerSeason(dispatcher);
        registerBestiary(dispatcher);
        registerHunt(dispatcher);
    }

    // ── /season ─────────────────────────────────────────────────────────────

    private static void registerSeason(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("season")
                .executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    SeasonGui.open(p);
                    return 1;
                })
                .then(Commands.literal("info")
                        .executes(ctx -> {
                            ServerPlayer p = ctx.getSource().getPlayerOrException();
                            Season s = SeasonManager.current();
                            p.sendSystemMessage(Component.literal("§6§l━━━ The Almanac ━━━"));
                            p.sendSystemMessage(Component.literal("§7Season: " + s.label()));
                            p.sendSystemMessage(Component.literal("§7" + s.descriptionLine1));
                            p.sendSystemMessage(Component.literal("§7Crop growth: "
                                    + SeasonGui.fmtMult(s.cropGrowth)));
                            p.sendSystemMessage(Component.literal("§7Settlement food: "
                                    + SeasonGui.fmtMult(s.foodMultiplier)));
                            p.sendSystemMessage(Component.literal("§7Turns in: §f"
                                    + SeasonManager.formatRemaining()));
                            p.sendSystemMessage(Component.literal("§7Year: §f"
                                    + (SeasonManager.yearsElapsed() + 1)));
                            return 1;
                        }))
                .then(Commands.literal("next")
                        .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                        .executes(ctx -> {
                            SeasonManager.advance(ctx.getSource().getServer());
                            ctx.getSource().sendSuccess(() -> Component.literal(
                                    "✓ Advanced to " + SeasonManager.current().displayName)
                                    .withStyle(ChatFormatting.GREEN), true);
                            return 1;
                        }))
                .then(Commands.literal("set")
                        .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(ctx -> {
                                    String name = StringArgumentType.getString(ctx, "name");
                                    Season target = Season.byName(name);
                                    if (target == null) {
                                        ctx.getSource().sendFailure(Component.literal(
                                                "Unknown season: " + name
                                                        + " (spring/summer/autumn/winter)"));
                                        return 0;
                                    }
                                    SeasonManager.setSeason(ctx.getSource().getServer(), target, true);
                                    ctx.getSource().sendSuccess(() -> Component.literal(
                                            "✓ Season set to " + target.displayName)
                                            .withStyle(ChatFormatting.GREEN), true);
                                    return 1;
                                })))
                .then(Commands.literal("length")
                        .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                        .then(Commands.argument("minutes", IntegerArgumentType.integer(5, 100000))
                                .executes(ctx -> {
                                    int minutes = IntegerArgumentType.getInteger(ctx, "minutes");
                                    SeasonManager.setSeasonLengthMs(minutes * 60_000L);
                                    ctx.getSource().sendSuccess(() -> Component.literal(
                                            "✓ Season length set to " + minutes + " minutes")
                                            .withStyle(ChatFormatting.GREEN), true);
                                    return 1;
                                })))
                .then(Commands.literal("toggle")
                        .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                        .executes(ctx -> {
                            SeasonManager.setEnabled(!SeasonManager.isEnabled());
                            ctx.getSource().sendSuccess(() -> Component.literal(
                                    "✓ Seasons " + (SeasonManager.isEnabled() ? "enabled" : "disabled"))
                                    .withStyle(ChatFormatting.GREEN), true);
                            return 1;
                        })));
    }

    // ── /bestiary ───────────────────────────────────────────────────────────

    private static void registerBestiary(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("bestiary")
                .executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    BestiaryGui.open(p);
                    return 1;
                })
                .then(Commands.literal("info")
                        .executes(ctx -> {
                            ServerPlayer p = ctx.getSource().getPlayerOrException();
                            String uuid = p.getUUID().toString();
                            p.sendSystemMessage(Component.literal("§6§l━━━ Bestiary ━━━"));
                            for (BestiaryEntry.MobFamily f : BestiaryEntry.MobFamily.values()) {
                                int kills = BestiaryManager.kills(uuid, f);
                                double bonus = BestiaryManager.familyBonus(uuid, f);
                                p.sendSystemMessage(Component.literal("  " + f.label()
                                        + " §7— §f" + kills + " §7kills §8(+"
                                        + Math.round(bonus * 100) + "%)"));
                            }
                            p.sendSystemMessage(Component.literal("§7Scholar of the Wild: §a+"
                                    + Math.round(BestiaryManager.scholarBonus(uuid) * 100) + "%"));
                            p.sendSystemMessage(Component.literal("§8Total bonus is capped at +"
                                    + Math.round(BestiaryManager.MAX_TOTAL_BONUS * 100) + "%."));
                            return 1;
                        }))
                .then(Commands.literal("reset")
                        .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                        .then(Commands.argument("player", net.minecraft.commands.arguments.EntityArgument.player())
                                .executes(ctx -> {
                                    ServerPlayer target = net.minecraft.commands.arguments.EntityArgument
                                            .getPlayer(ctx, "player");
                                    BestiaryManager.reset(target.getUUID().toString());
                                    ctx.getSource().sendSuccess(() -> Component.literal(
                                            "✓ Cleared the bestiary of "
                                                    + target.getName().getString())
                                            .withStyle(ChatFormatting.GREEN), true);
                                    return 1;
                                }))));
    }

    // ── /hunt ───────────────────────────────────────────────────────────────

    private static void registerHunt(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("hunt")
                .executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    HuntGui.open(p);
                    return 1;
                })
                .then(Commands.literal("info")
                        .executes(ctx -> {
                            ServerPlayer p = ctx.getSource().getPlayerOrException();
                            HuntManager.ActiveHunt active = HuntManager.active();
                            p.sendSystemMessage(Component.literal("§6§l━━━ The Great Hunt ━━━"));
                            if (active == null) {
                                p.sendSystemMessage(Component.literal("§7No hunt is active."));
                                p.sendSystemMessage(Component.literal("§7Next in: §f"
                                        + PoliticalServer.formatTime(HuntManager.msUntilNextHunt())));
                            } else {
                                HuntManager.Archetype a = active.archetype();
                                p.sendSystemMessage(Component.literal("§c"
                                        + (a == null ? "Unknown quarry"
                                        : a.title() + ", " + a.epithet())));
                                p.sendSystemMessage(Component.literal("§7Last seen: §f"
                                        + HuntManager.locationHint()));
                                p.sendSystemMessage(Component.literal("§7Leaves in: §f"
                                        + PoliticalServer.formatTime(active.remainingMs())));
                            }
                            p.sendSystemMessage(Component.literal("§7Purse: §6"
                                    + HuntManager.fmt(HuntManager.currentReward()) + " coins"));
                            p.sendSystemMessage(Component.literal("§7Hunts felled: §f"
                                    + HuntManager.huntsCompleted()));
                            return 1;
                        }))
                .then(Commands.literal("start")
                        .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                        .executes(ctx -> {
                            HuntManager.forceSpawn(ctx.getSource().getServer());
                            ctx.getSource().sendSuccess(() -> Component.literal(
                                    "✓ Great Hunt started.").withStyle(ChatFormatting.GREEN), true);
                            return 1;
                        }))
                .then(Commands.literal("toggle")
                        .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                        .executes(ctx -> {
                            HuntManager.setEnabled(!HuntManager.isEnabled());
                            ctx.getSource().sendSuccess(() -> Component.literal(
                                    "✓ Hunts " + (HuntManager.isEnabled() ? "enabled" : "disabled"))
                                    .withStyle(ChatFormatting.GREEN), true);
                            return 1;
                        }))
                .then(Commands.literal("interval")
                        .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                        .then(Commands.argument("minutes", IntegerArgumentType.integer(15, 100000))
                                .executes(ctx -> {
                                    int minutes = IntegerArgumentType.getInteger(ctx, "minutes");
                                    HuntManager.setCooldownMs(minutes * 60_000L);
                                    ctx.getSource().sendSuccess(() -> Component.literal(
                                            "✓ Hunt interval set to " + minutes + " minutes")
                                            .withStyle(ChatFormatting.GREEN), true);
                                    return 1;
                                }))));
    }

    /** Convenience so operators can hand out the keepsakes without a recipe run. */
    public static void giveKeepsakes(ServerPlayer player) {
        if (player == null) return;
        LivingWorldItems.give(player, LivingWorldItems.createSeasonsAlmanac());
        LivingWorldItems.give(player, LivingWorldItems.createBestiaryCodex());
        LivingWorldItems.give(player, LivingWorldItems.createHuntersHorn());
        if (player.level() instanceof ServerLevel) {
            player.sendSystemMessage(Component.literal("§aReceived the Almanac, Codex and Horn."));
        }
    }
}
