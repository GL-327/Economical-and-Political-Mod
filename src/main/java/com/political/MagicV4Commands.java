package com.political;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Locale;

/** Command surface for Magic V4: /technique, /school, /awaken, /magicgear. */
public final class MagicV4Commands {

    private MagicV4Commands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(Commands.literal("technique")
                .executes(ctx -> {
                    TechniqueGui.open(ctx.getSource().getPlayerOrException());
                    return 1;
                })
                .then(Commands.literal("info").executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    printInfo(p);
                    return 1;
                }))
                .then(Commands.literal("abilities").executes(ctx -> {
                    TechniqueGui.open(ctx.getSource().getPlayerOrException());
                    return 1;
                }))
                .then(Commands.literal("awaken").executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    TechniqueManager.attemptAwaken(p);
                    return 1;
                }))
                .then(Commands.literal("renounce").executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    TechniqueManager.renounce(p);
                    return 1;
                }))
                .then(Commands.literal("grant")
                        .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                        .then(Commands.argument("technique", StringArgumentType.word())
                                .executes(ctx -> {
                                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                                    String name = StringArgumentType.getString(ctx, "technique")
                                            .toUpperCase(Locale.ROOT);
                                    try {
                                        Technique t = Technique.valueOf(name);
                                        if (TechniqueManager.grant(p, t)) {
                                            p.sendSystemMessage(Component.literal("§aGranted " + t.displayName));
                                        } else {
                                            p.sendSystemMessage(Component.literal(
                                                    "§cCould not grant (exclusive already held?)"));
                                        }
                                    } catch (IllegalArgumentException e) {
                                        p.sendSystemMessage(Component.literal("§cUnknown technique: " + name));
                                    }
                                    return 1;
                                }))));

        dispatcher.register(Commands.literal("awaken").executes(ctx -> {
            TechniqueManager.attemptAwaken(ctx.getSource().getPlayerOrException());
            return 1;
        }));

        dispatcher.register(Commands.literal("school")
                .executes(ctx -> {
                    SchoolTreeGui.openSchoolList(ctx.getSource().getPlayerOrException());
                    return 1;
                })
                .then(Commands.literal("list").executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    String uuid = p.getUUID().toString();
                    p.sendSystemMessage(Component.literal("§6§lSchools of Magic"));
                    for (MagicSchool s : MagicSchool.values()) {
                        p.sendSystemMessage(Component.literal("  " + s.legacyColor + s.symbol + " "
                                + s.displayName + " §7— nodes " + SchoolTreeManager.unlockedCount(uuid, s)
                                + "/" + SchoolTree.nodesPerSchool(s)
                                + " §7| affinity " + SchoolTreeManager.getAffinity(p, s)));
                    }
                    p.sendSystemMessage(Component.literal("§7Arcane Shards: §b"
                            + SchoolTreeManager.getShards(uuid)));
                    return 1;
                }))
                .then(Commands.literal("tree")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(ctx -> {
                                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                                    String want = StringArgumentType.getString(ctx, "name")
                                            .toUpperCase(Locale.ROOT);
                                    for (MagicSchool s : MagicSchool.values()) {
                                        if (s.name().equals(want) || s.prefix.equalsIgnoreCase(want)
                                                || s.displayName.equalsIgnoreCase(want)) {
                                            SchoolTreeGui.openTree(p, s);
                                            return 1;
                                        }
                                    }
                                    p.sendSystemMessage(Component.literal("§cUnknown school: " + want));
                                    return 0;
                                })))
                .then(Commands.literal("shards")
                        .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                        .then(Commands.argument("amount", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1))
                                .executes(ctx -> {
                                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                                    int amt = com.mojang.brigadier.arguments.IntegerArgumentType
                                            .getInteger(ctx, "amount");
                                    SchoolTreeManager.addShards(p, amt);
                                    return 1;
                                }))));

        dispatcher.register(Commands.literal("magicgear")
                .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                .then(Commands.literal("catalyst").executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    p.getInventory().add(MagicGearFactory.createAwakeningCatalyst());
                    p.sendSystemMessage(Component.literal("§aGave Awakening Catalyst"));
                    return 1;
                }))
                .then(Commands.literal("robes")
                        .then(Commands.argument("school", StringArgumentType.word()).executes(ctx -> {
                            ServerPlayer p = ctx.getSource().getPlayerOrException();
                            String want = StringArgumentType.getString(ctx, "school").toUpperCase(Locale.ROOT);
                            for (MagicSchool s : MagicSchool.values()) {
                                if (s.name().equals(want) || s.prefix.equalsIgnoreCase(want)) {
                                    for (net.minecraft.world.entity.EquipmentSlot slot :
                                            new net.minecraft.world.entity.EquipmentSlot[]{
                                                    net.minecraft.world.entity.EquipmentSlot.HEAD,
                                                    net.minecraft.world.entity.EquipmentSlot.CHEST,
                                                    net.minecraft.world.entity.EquipmentSlot.LEGS,
                                                    net.minecraft.world.entity.EquipmentSlot.FEET}) {
                                        p.getInventory().add(MagicGearFactory.createRobePiece(s, slot));
                                    }
                                    p.sendSystemMessage(Component.literal("§aGave " + s.displayName + " robes"));
                                    return 1;
                                }
                            }
                            p.sendSystemMessage(Component.literal("§cUnknown school"));
                            return 0;
                        }))));
    }

    private static void printInfo(ServerPlayer p) {
        String uuid = p.getUUID().toString();
        if (!TechniqueManager.hasTechnique(uuid)) {
            p.sendSystemMessage(Component.literal("§7You have not awakened a technique yet.")
                    .withStyle(ChatFormatting.GRAY));
            p.sendSystemMessage(Component.literal("§7Reach Magic Level §e"
                    + TechniqueManager.AWAKEN_LEVEL + " §7and use an §dAwakening Catalyst§7."));
            return;
        }
        Technique t = TechniqueManager.getTechnique(uuid);
        p.sendSystemMessage(Component.literal("§6§l━━━ Your Technique ━━━"));
        p.sendSystemMessage(Component.literal("  ").append(
                Component.literal("✦ " + t.displayName).withStyle(t.color, ChatFormatting.BOLD)));
        p.sendSystemMessage(Component.literal("  §7" + t.flavor));
        p.sendSystemMessage(Component.literal("  §7Rank: §f" + TechniqueManager.getRank(uuid).display
                + " §8(" + TechniqueManager.totalUses(uuid) + " uses)"));
        p.sendSystemMessage(Component.literal("  §7Energy: §b" + (int) TechniqueManager.getEnergy(uuid)
                + "§7/§b" + TechniqueManager.getMaxEnergy(uuid)));
        if (t.exclusive) {
            p.sendSystemMessage(Component.literal("  §d§lEXCLUSIVE §7— only you hold this."));
        }
        Technique.Ability sel = TechniqueManager.getSelectedAbility(uuid);
        p.sendSystemMessage(Component.literal("  §7Selected: §f"
                + (sel == null ? "none" : sel.name())));
        p.sendSystemMessage(Component.literal("§8Sneak + right-click a Magic Staff to cast."));
    }
}
