package com.political;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

/** /changelog opens the version picker. The subcommands back the clickable chat prompts. */
public final class ChangelogCommands {

    private ChangelogCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // Collections live here rather than in their own file because this class is already the
        // home for the small player facing browsers.
        for (String alias : new String[]{"dungeons", "dungeon"}) {
            dispatcher.register(Commands.literal(alias)
                    .executes(ctx -> {
                        DungeonGui.open(ctx.getSource().getPlayerOrException());
                        return 1;
                    }));
        }

        for (String alias : new String[]{"hotreload", "livereload"}) {
            dispatcher.register(Commands.literal(alias)
                    .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                    .executes(ctx -> {
                        HotReloadGui.open(ctx.getSource().getPlayerOrException());
                        return 1;
                    })
                    .then(Commands.literal("data").executes(ctx -> {
                        HotReloadGui.runData(ctx.getSource().getPlayerOrException());
                        return 1;
                    }))
                    .then(Commands.literal("code").executes(ctx -> {
                        HotReloadGui.runCode(ctx.getSource().getPlayerOrException());
                        return 1;
                    })));
        }

        for (String alias : new String[]{"itemeditor", "edititem"}) {
            dispatcher.register(Commands.literal(alias)
                    .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                    .executes(ctx -> {
                        ItemEditorGui.open(ctx.getSource().getPlayerOrException());
                        return 1;
                    }));
        }

        for (String alias : new String[]{"enchants", "enchanting", "customenchants"}) {
            dispatcher.register(Commands.literal(alias)
                    .executes(ctx -> {
                        CustomEnchantGui.open(ctx.getSource().getPlayerOrException());
                        return 1;
                    }));
        }

        for (String alias : new String[]{"raredrops", "drops"}) {
            dispatcher.register(Commands.literal(alias)
                    .executes(ctx -> {
                        RareDropGui.open(ctx.getSource().getPlayerOrException(), 0);
                        return 1;
                    }));
        }

        for (String alias : new String[]{"materials", "mats", "refined"}) {
            dispatcher.register(Commands.literal(alias)
                    .executes(ctx -> {
                        MaterialGui.open(ctx.getSource().getPlayerOrException());
                        return 1;
                    }));
        }

        for (String alias : new String[]{"menus", "allmenus", "index"}) {
            dispatcher.register(Commands.literal(alias)
                    .executes(ctx -> {
                        AdminDirectoryGui.open(ctx.getSource().getPlayerOrException());
                        return 1;
                    }));
        }

        for (String alias : new String[]{"titles", "title"}) {
            dispatcher.register(Commands.literal(alias)
                    .executes(ctx -> {
                        TitleGui.open(ctx.getSource().getPlayerOrException());
                        return 1;
                    }));
        }

        for (String alias : new String[]{"commissions", "board", "quests", "requisitions"}) {
            dispatcher.register(Commands.literal(alias)
                    .executes(ctx -> {
                        CommissionGui.open(ctx.getSource().getPlayerOrException());
                        return 1;
                    }));
        }

        for (String alias : new String[]{"itemmodels", "models"}) {
            dispatcher.register(Commands.literal(alias)
                    .executes(ctx -> {
                        ItemModelGui.open(ctx.getSource().getPlayerOrException());
                        return 1;
                    }));
        }

        for (String alias : new String[]{"villages", "customvillages"}) {
            dispatcher.register(Commands.literal(alias)
                    .executes(ctx -> {
                        VillageAtlasGui.open(ctx.getSource().getPlayerOrException());
                        return 1;
                    }));
        }

        for (String alias : new String[]{"crates", "crate", "cases", "gamble"}) {
            dispatcher.register(Commands.literal(alias)
                    .executes(ctx -> {
                        CrateGui.open(ctx.getSource().getPlayerOrException());
                        return 1;
                    }));
        }
        for (String alias : new String[]{"progress", "tier", "settlementprogress"}) {
            dispatcher.register(Commands.literal(alias)
                    .executes(ctx -> {
                        SettlementProgressGui.openList(ctx.getSource().getPlayerOrException());
                        return 1;
                    }));
        }

        dispatcher.register(Commands.literal("sieges")
                .executes(ctx -> {
                    SiegeGui.open(ctx.getSource().getPlayerOrException());
                    return 1;
                }));

        dispatcher.register(Commands.literal("incursion")
                .executes(ctx -> {
                    IncursionGui.open(ctx.getSource().getPlayerOrException());
                    return 1;
                }));
        dispatcher.register(Commands.literal("incursions")
                .executes(ctx -> {
                    IncursionGui.open(ctx.getSource().getPlayerOrException());
                    return 1;
                }));

        dispatcher.register(Commands.literal("grudges")
                .executes(ctx -> {
                    NemesisGui.open(ctx.getSource().getPlayerOrException());
                    return 1;
                }));
        dispatcher.register(Commands.literal("nemesis")
                .executes(ctx -> {
                    NemesisGui.open(ctx.getSource().getPlayerOrException());
                    return 1;
                }));

        dispatcher.register(Commands.literal("minions")
                .executes(ctx -> {
                    MinionGui.open(ctx.getSource().getPlayerOrException());
                    return 1;
                }));

        dispatcher.register(Commands.literal("bazaar")
                .executes(ctx -> {
                    BazaarGui.open(ctx.getSource().getPlayerOrException());
                    return 1;
                }));
        dispatcher.register(Commands.literal("bz")
                .executes(ctx -> {
                    BazaarGui.open(ctx.getSource().getPlayerOrException());
                    return 1;
                }));

        dispatcher.register(Commands.literal("collections")
                .executes(ctx -> {
                    CollectionGui.open(ctx.getSource().getPlayerOrException());
                    return 1;
                }));
        dispatcher.register(Commands.literal("collection")
                .executes(ctx -> {
                    CollectionGui.open(ctx.getSource().getPlayerOrException());
                    return 1;
                }));

        dispatcher.register(Commands.literal("changelog")
                .executes(ctx -> {
                    ChangelogGui.open(ctx.getSource().getPlayerOrException());
                    return 1;
                })
                .then(Commands.literal("show")
                        .then(Commands.argument("version", StringArgumentType.word())
                                .executes(ctx -> {
                                    ChangelogManager.printVersion(ctx.getSource().getPlayerOrException(),
                                            StringArgumentType.getString(ctx, "version"));
                                    return 1;
                                })))
                .then(Commands.literal("detail")
                        .then(Commands.argument("key", StringArgumentType.word())
                                .executes(ctx -> {
                                    ChangelogManager.printDetail(ctx.getSource().getPlayerOrException(),
                                            StringArgumentType.getString(ctx, "key"));
                                    return 1;
                                }))));
    }
}
