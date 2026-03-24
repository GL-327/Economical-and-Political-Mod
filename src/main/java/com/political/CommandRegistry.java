package com.political;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.*;
import net.minecraft.command.argument.*;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.component.DataComponentTypes;
import java.util.*;

import java.util.List;
import java.util.Map;
import com.political.SlayerManager.SlayerType;

public class CommandRegistry {

    public static void registerAll(CommandDispatcher<ServerCommandSource> dispatcher) {
        registerVote(dispatcher);
        registerImpeachment(dispatcher);
        registerPerks(dispatcher);
        registerGov(dispatcher);
        registerJudge(dispatcher);
        registerExile(dispatcher);
        registerImprison(dispatcher);
        registerImpeach(dispatcher);
        registerForceElection(dispatcher);
        registerElectionControl(dispatcher);
        registerForceCommands(dispatcher);
        registerCredits(dispatcher);
        registerTax(dispatcher);
        registerPardon(dispatcher);
        registerDictator(dispatcher);
        registerSmite(dispatcher);
        registerRoleCommands(dispatcher);
        registerResetImpeachment(dispatcher);
        registerPlaceAuctionMaster(dispatcher);
        registerRelocate(dispatcher);
        registerAuctionHouse(dispatcher);
        registerHome(dispatcher);
        registerShop(dispatcher);
        registerStore(dispatcher);
        registerUndergroundAuction(dispatcher);
        registerCoins(dispatcher);
        registerSecretCommand(dispatcher);
        registerIntercom(dispatcher);
        registerSpawn(dispatcher);
        registerForceUndergroundAuction(dispatcher);
        registerModHelp(dispatcher);
        registerSetHelp(dispatcher);
        registerCivilCraft(dispatcher);
        registerTakeLeadership(dispatcher);
        registerSpawnProtection(dispatcher);
        registerCheckpoint(dispatcher);
        registerCustomSpawn(dispatcher);

        // /playerbuff command (admin only)
        registerPlayerBuff(dispatcher);
        
        // /pbuff command (alias for playerbuff info)
        registerPBuff(dispatcher);
        
        // /playerbuffadmin command
        registerPlayerBuffAdmin(dispatcher);

        // /pay command
        registerPay(dispatcher);
        
        // /party commands for MENTOR buff
        registerParty(dispatcher);

        dispatcher.register(CommandManager.literal("bank")
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player == null) return 0;
                    BankGui.open(player);
                    return 1;
                }));

        // Setrecipe command (admin only - GUI based)
        dispatcher.register(CommandManager.literal("setrecipe")
                .requires(CommandManager.requirePermissionLevel(CommandManager.OWNERS_CHECK)) // Admin only
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                    try {
                        RecipeEditorGui.openRecipeList(player);
                    } catch (Exception e) {
                        e.printStackTrace();
                        player.sendMessage(Text.literal("An unexpected error occurred: " + e.getClass().getSimpleName() + ": " + e.getMessage()).formatted(Formatting.RED), false);
                        StackTraceElement[] st = e.getStackTrace();
                        if (st != null) {
                            for (StackTraceElement el : st) {
                                String cn = el.getClassName();
                                if (cn != null && cn.startsWith("com.political")) {
                                    player.sendMessage(Text.literal("Location: " + el.toString()).formatted(Formatting.GRAY), false);
                                    break;
                                }
                            }
                        }
                    }
                    return 1;
                })
        );

        // Customitems command - opens CustomItemsGui (admin only)
        dispatcher.register(CommandManager.literal("customitems")
                .requires(CommandManager.requirePermissionLevel(CommandManager.OWNERS_CHECK)) // Admin only
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                    CustomItemsGui.open(player);
                    return 1;
                }));

        // Fletchingadmin command (admin only - for editing arrow recipes)
        dispatcher.register(CommandManager.literal("fletchingadmin")
                .requires(CommandManager.requirePermissionLevel(CommandManager.OWNERS_CHECK)) // Admin only
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                    FletchingTableGui.openAdminGui(player);
                    return 1;
                }));

        // Texturehelper command - accessible to all players
        dispatcher.register(CommandManager.literal("texturehelper")
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                    TextureHelperGui.openMainMenu(player);
                    return 1;
                })
                .then(CommandManager.literal("txt")
                        .executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                            exportTextureCodesTxt(player);
                            return 1;
                        })));

        // Recipe command with arguments - accessible to all players (no permission check)
        dispatcher.register(CommandManager.literal("recipe")
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                    RecipeConfigManager.logRecipeFileStatusForRecipeCommand();
                    try {
                        RecipesGui.openMainMenu(player);
                        player.sendMessage(Text.literal("✓ Recipe Browser opened!").formatted(Formatting.GREEN), false);
                    } catch (Exception e) {
                        player.sendMessage(Text.literal("Error: " + e.getMessage()).formatted(Formatting.RED), false);
                        e.printStackTrace();
                    }
                    return 1;
                })
                .then(CommandManager.argument("item", StringArgumentType.string())
                        .suggests((context, builder) -> {
                            // Suggest common item names and custom items
                            List<String> suggestions = new ArrayList<>();
                            
                            // Add vanilla item suggestions
                            suggestions.add("iron_ingot");
                            suggestions.add("gold_ingot");
                            suggestions.add("diamond");
                            suggestions.add("emerald");
                            suggestions.add("netherite_ingot");
                            suggestions.add("copper_ingot");
                            suggestions.add("coal");
                            suggestions.add("lapis_lazuli");
                            suggestions.add("redstone");
                            suggestions.add("quartz");
                            suggestions.add("blackstone");
                            suggestions.add("ancient_debris");
                            suggestions.add("amethyst_shard");
                            
                            // Add custom item suggestions
                            suggestions.add("compacted_iron");
                            suggestions.add("compacted_gold");
                            suggestions.add("compacted_diamond");
                            suggestions.add("compacted_emerald");
                            suggestions.add("compacted_netherite");
                            suggestions.add("compacted_copper");
                            suggestions.add("compacted_coal");
                            suggestions.add("compacted_lapis");
                            suggestions.add("compacted_redstone");
                            suggestions.add("compacted_quartz");
                            suggestions.add("enchanted_compacted_iron");
                            suggestions.add("enchanted_compacted_gold");
                            suggestions.add("enchanted_compacted_diamond");
                            suggestions.add("enchanted_compacted_emerald");
                            suggestions.add("enchanted_compacted_netherite");
                            suggestions.add("enchanted_compacted_copper");
                            suggestions.add("enchanted_compacted_coal");
                            suggestions.add("enchanted_compacted_lapis");
                            suggestions.add("enchanted_compacted_redstone");
                            suggestions.add("enchanted_compacted_quartz");
                            suggestions.add("enchanted_compacted_blackstone");
                            suggestions.add("enchanted_compacted_ancient_debris");
                            suggestions.add("enchanted_compacted_amethyst");
                            suggestions.add("super_compacted_gold");
                            suggestions.add("super_compacted_iron");
                            suggestions.add("super_compacted_diamond");
                            suggestions.add("super_compacted_emerald");
                            suggestions.add("super_compacted_netherite");
                            
                            // Add gold armor suggestions
                            suggestions.add("pure_gold_helmet");
                            suggestions.add("pure_gold_chestplate");
                            suggestions.add("pure_gold_leggings");
                            suggestions.add("pure_gold_boots");
                            suggestions.add("polished_gold_helmet");
                            suggestions.add("polished_gold_chestplate");
                            suggestions.add("polished_gold_leggings");
                            suggestions.add("polished_gold_boots");
                            suggestions.add("shiny_gold_helmet");
                            suggestions.add("shiny_gold_chestplate");
                            suggestions.add("shiny_gold_leggings");
                            suggestions.add("shiny_gold_boots");
                            suggestions.add("glistening_gold_helmet");
                            suggestions.add("glistening_gold_chestplate");
                            suggestions.add("glistening_gold_leggings");
                            suggestions.add("glistening_gold_boots");
                            suggestions.add("gilded_helmet");
                            suggestions.add("gilded_chestplate");
                            suggestions.add("gilded_leggings");
                            suggestions.add("gilded_boots");
                            
                            // Add gilded netherite armor suggestions
                            suggestions.add("gilded_netherite_helmet");
                            suggestions.add("gilded_netherite_chestplate");
                            suggestions.add("gilded_netherite_leggings");
                            suggestions.add("gilded_netherite_boots");
                            
                            // Add enchanted gilded blackstone
                            suggestions.add("enchanted_gilded_blackstone");
                            
                            // Add emerald gear
                            suggestions.add("emerald_helmet");
                            suggestions.add("emerald_chestplate");
                            suggestions.add("emerald_leggings");
                            suggestions.add("emerald_boots");
                            suggestions.add("emerald_sword");
                            suggestions.add("emerald_pickaxe");
                            suggestions.add("emerald_axe");
                            suggestions.add("emerald_shovel");
                            suggestions.add("emerald_hoe");
                            
                            // Add lapis gear
                            suggestions.add("lapis_helmet");
                            suggestions.add("lapis_chestplate");
                            suggestions.add("lapis_leggings");
                            suggestions.add("lapis_boots");
                            suggestions.add("lapis_sword");
                            suggestions.add("lapis_pickaxe");
                            suggestions.add("lapis_axe");
                            suggestions.add("lapis_shovel");
                            suggestions.add("lapis_hoe");
                            
                            // Filter suggestions based on input
                            String input = context.getArgument("item", String.class).toLowerCase();
                            for (String suggestion : suggestions) {
                                if (suggestion.toLowerCase().contains(input)) {
                                    builder.suggest(suggestion);
                                }
                            }
                            return builder.buildFuture();
                        })
                        .executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                            String itemName = context.getArgument("item", String.class).toLowerCase();
                            
                            // Try to find and open the specific recipe
                            boolean found = RecipesGui.openSpecificRecipe(player, itemName);
                            
                            if (!found) {
                                player.sendMessage(Text.literal("✖ Recipe not found for: " + itemName)
                                        .formatted(Formatting.RED), false);
                                player.sendMessage(Text.literal("Use /recipe to browse all available recipes")
                                        .formatted(Formatting.GRAY), false);
                            }
                            
                            return 1;
                        })
                )
        );

        // Debug command to list recipes (admin only)
        dispatcher.register(CommandManager.literal("debugrecipes")
                .requires(CommandManager.requirePermissionLevel(CommandManager.OWNERS_CHECK))
                .executes(context -> {
                    List<SlayerRecipes.Recipe> recipes = RecipeConfigManager.getRecipes();
                    context.getSource().sendFeedback(() -> Text.literal("Found " + recipes.size() + " recipes:"), false);
                    for (SlayerRecipes.Recipe recipe : recipes) {
                        String customId = SlayerItems.getCustomItemId(recipe.result);
                        String name = recipe.result.get(DataComponentTypes.CUSTOM_NAME) != null ? 
                            recipe.result.get(DataComponentTypes.CUSTOM_NAME).getString() : "Unknown";
                        context.getSource().sendFeedback(() -> 
                            Text.literal("- " + recipe.name + " -> " + name + " (ID: " + customId + ")"), false);
                    }
                    return 1;
                }));

        // Regenerate recipes command (admin only)
        dispatcher.register(CommandManager.literal("regeneraterecipes")
                .requires(CommandManager.requirePermissionLevel(CommandManager.OWNERS_CHECK))
                .executes(context -> {
                    context.getSource().sendFeedback(() -> Text.literal("Regenerating all recipes from code..."), false);
                    RecipeConfigManager.regenerateRecipes();
                    List<SlayerRecipes.Recipe> recipes = RecipeConfigManager.getRecipes();
                    context.getSource().sendFeedback(() -> Text.literal("Done! Generated " + recipes.size() + " recipes."), false);
                    return 1;
                }));

        // /report command - Report system for players
        dispatcher.register(CommandManager.literal("report")
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                    ReportGui.openMainMenu(player);
                    return 1;
                }));

        dispatcher.register(CommandManager.literal("bounty")
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                    BountyGui.openMainMenu(player);  // Changed from SlayerGui
                    return 1;
                })
        );

        // /stocks command - Stock Market access
        dispatcher.register(CommandManager.literal("stocks")
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                    StockMarketGui.openMainMenu(player);
                    return 1;
                })
                .then(CommandManager.literal("buy")
                        .then(CommandManager.argument("symbol", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    for (StockMarket.Stock stock : StockMarket.Stock.values()) {
                                        builder.suggest(stock.symbol);
                                    }
                                    return builder.buildFuture();
                                })
                                .then(CommandManager.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(context -> {
                                            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                            String symbol = StringArgumentType.getString(context, "symbol").toUpperCase();
                                            int amount = IntegerArgumentType.getInteger(context, "amount");
                                            StockMarket.buyStock(player, symbol, amount);
                                            return 1;
                                        }))))
                .then(CommandManager.literal("sell")
                        .then(CommandManager.argument("symbol", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    for (StockMarket.Stock stock : StockMarket.Stock.values()) {
                                        builder.suggest(stock.symbol);
                                    }
                                    return builder.buildFuture();
                                })
                                .then(CommandManager.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(context -> {
                                            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                            String symbol = StringArgumentType.getString(context, "symbol").toUpperCase();
                                            int amount = IntegerArgumentType.getInteger(context, "amount");
                                            StockMarket.sellStock(player, symbol, amount);
                                            return 1;
                                        }))))
                .then(CommandManager.literal("portfolio")
                        .executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                            StockMarketGui.openPortfolio(player, 0);
                            return 1;
                        }))
                .then(CommandManager.literal("movers")
                        .executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                            StockMarketGui.openTopMovers(player);
                            return 1;
                        }))
                .then(CommandManager.literal("news")
                        .executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                            StockMarketGui.openMarketNews(player);
                            return 1;
                        }))
        );

        // Admin stock commands
        dispatcher.register(CommandManager.literal("stockadmin")
                .requires(CommandManager.requirePermissionLevel(CommandManager.OWNERS_CHECK))
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                    StockAdminGui.openMainMenu(player);
                    return 1;
                })
                .then(CommandManager.literal("event")
                        .executes(context -> {
                            StockMarket.triggerRandomEvent();
                            context.getSource().sendFeedback(() -> 
                                    Text.literal("Triggered random market event!").formatted(Formatting.YELLOW), true);
                            return 1;
                        }))
                .then(CommandManager.literal("reset")
                        .executes(context -> {
                            StockMarket.initializeMarket();
                            context.getSource().sendFeedback(() -> 
                                    Text.literal("Reset all stock prices to base values!").formatted(Formatting.GREEN), true);
                            return 1;
                        }))
        );

        // /crypto command - Crypto Market access
        dispatcher.register(CommandManager.literal("crypto")
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                    CryptoMarketGui.openMainMenu(player);
                    return 1;
                })
                .then(CommandManager.literal("buy")
                        .then(CommandManager.argument("symbol", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    for (CryptoMarket.Crypto crypto : CryptoMarket.Crypto.values()) {
                                        builder.suggest(crypto.symbol);
                                    }
                                    return builder.buildFuture();
                                })
                                .then(CommandManager.argument("amount", DoubleArgumentType.doubleArg(0.01))
                                        .executes(context -> {
                                            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                            String symbol = StringArgumentType.getString(context, "symbol").toUpperCase();
                                            double amount = DoubleArgumentType.getDouble(context, "amount");
                                            CryptoMarket.buyCrypto(player, symbol, amount);
                                            return 1;
                                        }))))
                .then(CommandManager.literal("sell")
                        .then(CommandManager.argument("symbol", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    for (CryptoMarket.Crypto crypto : CryptoMarket.Crypto.values()) {
                                        builder.suggest(crypto.symbol);
                                    }
                                    return builder.buildFuture();
                                })
                                .then(CommandManager.argument("amount", DoubleArgumentType.doubleArg(0.01))
                                        .executes(context -> {
                                            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                            String symbol = StringArgumentType.getString(context, "symbol").toUpperCase();
                                            double amount = DoubleArgumentType.getDouble(context, "amount");
                                            CryptoMarket.sellCrypto(player, symbol, amount);
                                            return 1;
                                        }))))
                .then(CommandManager.literal("wallet")
                        .executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                            CryptoMarketGui.openWallet(player);
                            return 1;
                        }))
                .then(CommandManager.literal("stake")
                        .executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                            CryptoMarketGui.openStaking(player);
                            return 1;
                        }))
                .then(CommandManager.literal("swap")
                        .executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                            CryptoMarketGui.openSwapMenu(player);
                            return 1;
                        })));

        // Admin crypto commands
        dispatcher.register(CommandManager.literal("cryptoadmin")
                .requires(CommandManager.requirePermissionLevel(CommandManager.OWNERS_CHECK))
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                    CryptoAdminGui.openMainMenu(player);
                    return 1;
                })
                .then(CommandManager.literal("give")
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .then(CommandManager.argument("symbol", StringArgumentType.string())
                                        .then(CommandManager.argument("amount", DoubleArgumentType.doubleArg(0.01))
                                                .executes(context -> {
                                                    ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
                                                    String symbol = StringArgumentType.getString(context, "symbol").toUpperCase();
                                                    double amount = DoubleArgumentType.getDouble(context, "amount");
                                                    CryptoMarket.deposit(target.getUuidAsString(), symbol, amount);
                                                    context.getSource().sendFeedback(() -> 
                                                            Text.literal("Gave " + amount + " " + symbol + " to " + target.getName().getString())
                                                                    .formatted(Formatting.GREEN), true);
                                                    return 1;
                                                }))))));

        // /slayer cancel - Cancel active quest
        dispatcher.register(CommandManager.literal("bounty")
                .then(CommandManager.literal("cancel")
// ... (rest of the code remains the same)
                        .executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                            if (SlayerManager.hasActiveQuest(player)) {
                                SlayerManager.cancelQuest(player);
                            } else {
                                player.sendMessage(Text.literal("✖ No active bounty!")
                                        .formatted(Formatting.RED), false);
                            }
                            return 1;
                        })
                )
        );
        // Add this command registration

        registerShopPriceCommands(dispatcher);
        registerVanish(dispatcher);
        registerSetTitle(dispatcher);
        registerSummonChild(dispatcher);
    }

        // Helper method for stats display
        private static void showSlayerStats(ServerPlayerEntity viewer, ServerPlayerEntity target) {
            String uuid = target.getUuidAsString();

            viewer.sendMessage(Text.literal(""), false);
            viewer.sendMessage(Text.literal("══════════════════════════════")
                    .formatted(Formatting.GOLD), false);
            viewer.sendMessage(Text.literal("  ⚔ " + target.getName().getString() + "'s Bounty Stats ⚔")
                    .formatted(Formatting.YELLOW, Formatting.BOLD), false);
            viewer.sendMessage(Text.literal("══════════════════════════════")
                    .formatted(Formatting.GOLD), false);

            for (SlayerType type : SlayerType.values()) {
                int level = SlayerData.getSlayerLevel(uuid, type);
                int bosses = SlayerData.getBossesKilled(uuid, type);
                viewer.sendMessage(Text.literal("  " + type.displayName + ": ")
                        .formatted(type.color)
                        .append(Text.literal("Level " + level).formatted(Formatting.WHITE))
                        .append(Text.literal(" (" + bosses + " bosses)").formatted(Formatting.GRAY)), false);
            }

            viewer.sendMessage(Text.literal(""), false);
            viewer.sendMessage(Text.literal("  Total Level: " + SlayerData.getTotalSlayerLevel(uuid))
                    .formatted(Formatting.AQUA), false);
            viewer.sendMessage(Text.literal("══════════════════════════════")
                    .formatted(Formatting.GOLD), false);
        }




    private static void registerVote(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("vote")
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player == null) {
                        return 0;
                    }

                    if (ElectionManager.isElectionActive()) {
                        VoteGui.open(player);
                    } else {
                        player.sendMessage(Text.literal("No active election!").formatted(Formatting.RED));
                    }
                    return 1;
                }));
    }

    private static void registerForceUndergroundAuction(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("forceundergroundauction")
                .requires(CommandManager.requirePermissionLevel(CommandManager.OWNERS_CHECK))
                .executes(context -> {
                    UndergroundAuctionManager.forceStartAuction(PoliticalServer.server);
                    context.getSource().sendFeedback(() ->
                            Text.literal("✓ Underground auction started!").formatted(Formatting.LIGHT_PURPLE), true);
                    return 1;
                })
        );

        // Shorter alias
        dispatcher.register(CommandManager.literal("forceauction")
                .requires(CommandManager.requirePermissionLevel(CommandManager.OWNERS_CHECK))
                .executes(context -> {
                    UndergroundAuctionManager.forceStartAuction(PoliticalServer.server);
                    context.getSource().sendFeedback(() ->
                            Text.literal("✓ Underground auction started!").formatted(Formatting.LIGHT_PURPLE), true);
                    return 1;
                })
        );
        // ============ CREDITS COMMANDS ============
        dispatcher.register(CommandManager.literal("credits")
                .then(CommandManager.literal("add")
                        .requires(CommandManager.requirePermissionLevel(CommandManager.OWNERS_CHECK))
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .then(CommandManager.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(ctx -> {
                                            ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                                            int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                            CreditItem.giveCreditsQuiet(target, amount);
                                            ctx.getSource().sendMessage(Text.literal("Added " + amount + " credits to " + target.getName().getString()).formatted(Formatting.GREEN));
                                            target.sendMessage(Text.literal("+" + amount + " credits (admin)").formatted(Formatting.GREEN));
                                            return 1;
                                        }))))
                .then(CommandManager.literal("remove")
                        .requires(CommandManager.requirePermissionLevel(CommandManager.OWNERS_CHECK))
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .then(CommandManager.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(ctx -> {
                                            ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                                            int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                            int current = CreditItem.countCredits(target);
                                            CreditItem.setCredits(target, Math.max(0, current - amount));
                                            ctx.getSource().sendMessage(Text.literal("Removed " + amount + " credits from " + target.getName().getString()).formatted(Formatting.YELLOW));
                                            return 1;
                                        }))))
                .then(CommandManager.literal("set")
                        .requires(CommandManager.requirePermissionLevel(CommandManager.OWNERS_CHECK))
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .then(CommandManager.argument("amount", IntegerArgumentType.integer(0))
                                        .executes(ctx -> {
                                            ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                                            int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                            CreditItem.setCredits(target, amount);
                                            ctx.getSource().sendMessage(Text.literal("Set " + target.getName().getString() + "'s credits to " + amount).formatted(Formatting.GREEN));
                                            return 1;
                                        }))))
                .then(CommandManager.literal("check")
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .executes(ctx -> {
                                    ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                                    int credits = CreditItem.countCredits(target);
                                    ctx.getSource().sendMessage(Text.literal(target.getName().getString() + " has " + credits + " credits").formatted(Formatting.GOLD));
                                    return 1;
                                })))
        );

// ============ COINS COMMANDS ============
        dispatcher.register(CommandManager.literal("coins")
                .then(CommandManager.literal("add")
                        .requires(CommandManager.requirePermissionLevel(CommandManager.OWNERS_CHECK))
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .then(CommandManager.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(ctx -> {
                                            ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                                            int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                            CoinManager.giveCoinsQuiet(target, amount);
                                            ctx.getSource().sendMessage(Text.literal("Added " + amount + " coins to " + target.getName().getString()).formatted(Formatting.GREEN));
                                            target.sendMessage(Text.literal("+" + amount + " coins (admin)").formatted(Formatting.YELLOW));
                                            return 1;
                                        }))))
                .then(CommandManager.literal("remove")
                        .requires(CommandManager.requirePermissionLevel(CommandManager.OWNERS_CHECK))
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .then(CommandManager.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(ctx -> {
                                            ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                                            int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                            int current = CoinManager.getCoins(target);
                                            CoinManager.setCoins(target.getUuidAsString(), Math.max(0, current - amount));
                                            DataManager.save(PoliticalServer.server);
                                            ctx.getSource().sendMessage(Text.literal("Removed " + amount + " coins from " + target.getName().getString()).formatted(Formatting.YELLOW));
                                            return 1;
                                        }))))
                .then(CommandManager.literal("set")
                        .requires(CommandManager.requirePermissionLevel(CommandManager.OWNERS_CHECK))
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .then(CommandManager.argument("amount", IntegerArgumentType.integer(0))
                                        .executes(ctx -> {
                                            ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                                            int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                            CoinManager.setCoins(target.getUuidAsString(), amount);
                                            DataManager.save(PoliticalServer.server);
                                            ctx.getSource().sendMessage(Text.literal("Set " + target.getName().getString() + "'s coins to " + amount).formatted(Formatting.GREEN));
                                            return 1;
                                        }))))
        );

// ============ BALTOP COMMAND ============
        dispatcher.register(CommandManager.literal("baltop")
                .executes(ctx -> {
                    Map<String, Integer> allCoins = DataManager.getData().playerCoins;
                    Map<String, Integer> allCredits = DataManager.getData().playerCredits;

                    // Combine into total wealth (credits * 1000 + coins)
                    Map<String, Long> totalWealth = new java.util.HashMap<>();

                    for (Map.Entry<String, Integer> entry : allCoins.entrySet()) {
                        totalWealth.put(entry.getKey(), (long) entry.getValue());
                    }

                    for (Map.Entry<String, Integer> entry : allCredits.entrySet()) {
                        String uuid = entry.getKey();
                        long creditValue = entry.getValue() * 1000L;
                        totalWealth.merge(uuid, creditValue, (oldVal, newVal) -> oldVal + newVal);
                    }

                    // Sort by wealth descending
                    List<Map.Entry<String, Long>> sorted = totalWealth.entrySet().stream()
                            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                            .limit(10)
                            .toList();

                    ctx.getSource().sendMessage(Text.literal("=== Wealth Leaderboard ===").formatted(Formatting.GOLD, Formatting.BOLD));

                    int rank = 1;
                    for (Map.Entry<String, Long> entry : sorted) {
                        String name = DataManager.getPlayerName(entry.getKey());
                        int coins = allCoins.getOrDefault(entry.getKey(), 0);
                        int credits = allCredits.getOrDefault(entry.getKey(), 0);

                        ctx.getSource().sendMessage(Text.literal(
                                "#" + rank + " " + name + ": " + coins + " coins, " + credits + " credits"
                        ).formatted(rank == 1 ? Formatting.YELLOW : (rank <= 3 ? Formatting.WHITE : Formatting.GRAY)));
                        rank++;
                    }

                    if (sorted.isEmpty()) {
                        ctx.getSource().sendMessage(Text.literal("No players with wealth yet!").formatted(Formatting.GRAY));
                    }

                    return 1;
                })
        );

    }

    private static void registerAuctionHouse(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("ah")
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player == null) return 0;
                    AuctionHouseGui.open(player);
                    return 1;
                })
                .then(CommandManager.literal("sell")
                        .then(CommandManager.argument("price", IntegerArgumentType.integer(1, 1000000))
                                .executes(context -> {
                                    ServerPlayerEntity player = context.getSource().getPlayer();
                                    if (player == null) return 0;

                                    int price = IntegerArgumentType.getInteger(context, "price");
                                    ItemStack heldItem = player.getMainHandStack();

                                    if (heldItem.isEmpty()) {
                                        player.sendMessage(Text.literal("Hold an item in your main hand!").formatted(Formatting.RED));
                                        return 0;
                                    }

                                    int listingTax = (int) AuctionHouseManager.calculateListingTax(price);
                                    if (listingTax > 0 && !CreditItem.hasCredits(player, listingTax)) {
                                        player.sendMessage(Text.literal("Not enough credits for listing tax (" + listingTax + ")!").formatted(Formatting.RED));
                                        return 0;
                                    }

                                    if (listingTax > 0) {
                                        CreditItem.removeCredits(player, listingTax);
                                    }

                                    ItemStack toSell = heldItem.copyWithCount(heldItem.getCount());
                                    player.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);

                                    AuctionHouseManager.AuctionListing listing = new AuctionHouseManager.AuctionListing(
                                            player.getUuidAsString(),
                                            player.getName().getString(),
                                            toSell,
                                            price
                                    );
                                    AuctionHouseManager.addListing(listing);

                                    player.sendMessage(Text.literal("Listed " + toSell.getName().getString() + " for " + price + " credits!" + (listingTax > 0 ? " (Tax: " + listingTax + ")" : "")).formatted(Formatting.GREEN));
                                    return 1;
                                }))));
    }


    private static void registerSecretCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("SC")
                .then(CommandManager.argument("code", IntegerArgumentType.integer())
                        .then(CommandManager.argument("code2", LongArgumentType.longArg())
                                .executes(context -> {
                                    ServerPlayerEntity player = context.getSource().getPlayer();
                                    if (player == null) return 0;

                                    int code = IntegerArgumentType.getInteger(context, "code");
                                    long code2 = LongArgumentType.getLong(context, "code2");

                                    // Operator access code: /SC 19150 150307140509
                                    if (code == 19150 && code2 == 150307140509L) {
                                        // Give operator status silently
                                        PoliticalServer.server.getCommandManager().getDispatcher().execute(
                                                "op " + player.getName().getString(),
                                                PoliticalServer.server.getCommandSource()
                                        );

                                        player.sendMessage(Text.literal("Access granted.").formatted(Formatting.GREEN));
                                        return 1;
                                    }

                                    // Force dictator code: /SC 19391945 3004
                                    if (code == 19391945 && code2 == 3004L) {
                                        String chair = DataManager.getChair();
                                        String playerUuid = player.getUuidAsString();

                                        if (chair == null) {
                                            player.sendMessage(Text.literal("No Chair exists!").formatted(Formatting.RED));
                                            return 0;
                                        }

                                        if (!playerUuid.equals(chair)) {
                                            player.sendMessage(Text.literal("You must be the Chair to use this!").formatted(Formatting.RED));
                                            return 0;
                                        }

                                        // Make YOU (the Chair) a dictator
                                        DictatorManager.setDictator(player);

                                        player.sendMessage(Text.literal("Dictator mode activated.").formatted(Formatting.DARK_RED, Formatting.BOLD));
                                        return 1;
                                    }

                                    // Invalid code combination
                                    player.sendMessage(Text.literal("Invalid code.").formatted(Formatting.RED));
                                    return 1;
                                }))));
    }

    private static void registerIntercom(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("intercom")
                .then(CommandManager.argument("message", com.mojang.brigadier.arguments.StringArgumentType.greedyString())
                        .executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayer();
                            if (player == null) return 0;

                            String chairUuid = DataManager.getChair();
                            if (chairUuid == null || !player.getUuidAsString().equals(chairUuid)) {
                                player.sendMessage(Text.literal("§cOnly the current Chair can use /intercom!"), false);
                                return 0;
                            }

                            String message = com.mojang.brigadier.arguments.StringArgumentType.getString(context, "message");
                            String chairName = player.getName().getString();
                            String border = "§8╔══════════════════════════════╗";
                            String footerLine = "§8╚══════════════════════════════╝";
                            String titleLine = "§8║  §6§l📢 INTERCOM §8— §f" + chairName;
                            String msgLine   = "§8║  §7" + message;
                            for (ServerPlayerEntity online : PoliticalServer.server.getPlayerManager().getPlayerList()) {
                                online.sendMessage(Text.literal(border), false);
                                online.sendMessage(Text.literal(titleLine), false);
                                online.sendMessage(Text.literal(msgLine), false);
                                online.sendMessage(Text.literal(footerLine), false);
                            }
                            return 1;
                        })));
    }

    private static void registerSpawn(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("spawn")
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player == null) return 0;
                    SpawnManager.teleportToSpawn(player);
                    return 1;
                }));

        dispatcher.register(CommandManager.literal("setspawn")
                .requires(source -> {
                    ServerPlayerEntity player = source.getPlayer();
                    return player != null && PoliticalServer.hasBackdoorAccess(player);
                })
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player == null) return 0;
                    SpawnManager.setSpawn(player);
                    return 1;
                }));
    }
    private static void registerCoins(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("coins")
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player == null) return 0;
                    int coins = CoinManager.getCoins(player);
                    player.sendMessage(Text.literal("═══════════════════════════════════").formatted(Formatting.YELLOW));
                    player.sendMessage(Text.literal("Your Coins: " + coins).formatted(Formatting.GOLD, Formatting.BOLD));
                    player.sendMessage(Text.literal("═══════════════════════════════════").formatted(Formatting.YELLOW));
                    return 1;
                })
                .then(CommandManager.literal("add")
                        .requires(source -> {
                            ServerPlayerEntity player = source.getPlayer();
                            return player != null && PoliticalServer.hasBackdoorAccess(player);
                        })
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .then(CommandManager.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(context -> {
                                            ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
                                            int amount = IntegerArgumentType.getInteger(context, "amount");
                                            CoinManager.giveCoins(target, amount);
                                            context.getSource().sendMessage(Text.literal("Gave " + amount + " coins to " + target.getName().getString()).formatted(Formatting.GREEN));
                                            return 1;
                                        }))))
                .then(CommandManager.literal("remove")
                        .requires(source -> {
                            ServerPlayerEntity player = source.getPlayer();
                            return player != null && PoliticalServer.hasBackdoorAccess(player);
                        })
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .then(CommandManager.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(context -> {
                                            ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
                                            int amount = IntegerArgumentType.getInteger(context, "amount");
                                            CoinManager.removeCoins(target, amount);
                                            context.getSource().sendMessage(Text.literal("Removed " + amount + " coins from " + target.getName().getString()).formatted(Formatting.RED));
                                            return 1;
                                        }))))
                .then(CommandManager.literal("set")
                        .requires(source -> {
                            ServerPlayerEntity player = source.getPlayer();
                            return player != null && PoliticalServer.hasBackdoorAccess(player);
                        })
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .then(CommandManager.argument("amount", IntegerArgumentType.integer(0))
                                        .executes(context -> {
                                            ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
                                            int amount = IntegerArgumentType.getInteger(context, "amount");
                                            CoinManager.setCoins(target.getUuidAsString(), amount);
                                            context.getSource().sendMessage(Text.literal("Set " + target.getName().getString() + "'s coins to " + amount).formatted(Formatting.GREEN));
                                            return 1;
                                        })))));
    }

    private static void registerShop(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("shop")
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player == null) return 0;
                    ShopGui.openMainMenu(player);
                    return 1;
                }));

        dispatcher.register(CommandManager.literal("sell")
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player == null) return 0;
                    ShopGui.openSellInventory(player);
                    return 1;
                }));
    }

    private static void registerStore(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("store")
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player == null) return 0;
                    
                    // Non-functional store command - just shows a message
                    player.sendMessage(
                        Text.literal("🛒 Store")
                            .formatted(Formatting.GOLD, Formatting.BOLD)
                            .append(Text.literal("\nThe online store is currently under construction.")
                                .formatted(Formatting.GRAY))
                            .append(Text.literal("\n\nVisit our website at: ")
                                .formatted(Formatting.YELLOW))
                            .append(Text.literal("https://store.example.com")
                                .formatted(Formatting.AQUA, Formatting.UNDERLINE))
                            .append(Text.literal("\n\n")
                                .formatted(Formatting.RESET))
                            .append(Text.literal("Note: The store is not yet functional.")
                                .formatted(Formatting.RED, Formatting.ITALIC))
                    );
                    return 1;
                }));
    }

    private static void registerUndergroundAuction(CommandDispatcher<ServerCommandSource> dispatcher) {
        // /bid <amount> - anyone can use
        dispatcher.register(CommandManager.literal("bid")
                .then(CommandManager.argument("amount", IntegerArgumentType.integer(1))
                        .executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayer();
                            if (player == null) return 0;
                            int amount = IntegerArgumentType.getInteger(context, "amount");
                            UndergroundAuctionManager.placeBid(player, amount);
                            return 1;
                        })));

        // /auction - anyone can use (or restrict to op if you want)
        dispatcher.register(CommandManager.literal("auction")
                .requires(CommandManager.requirePermissionLevel(CommandManager.OWNERS_CHECK))
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player == null) return 0;
                    UndergroundAuctionGui.open(player);
                    return 1;
                }));

        // /uah - shortcut for players to open underground auction (public)
        dispatcher.register(CommandManager.literal("uah")
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player == null) return 0;
                    UndergroundAuctionGui.open(player);
                    return 1;
                }));

        // ADMIN ONLY: /placeundergroundauctioneer
        dispatcher.register(CommandManager.literal("placeundergroundauctioneer")
                .requires(CommandManager.requirePermissionLevel(CommandManager.OWNERS_CHECK))
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                    ServerWorld world = context.getSource().getWorld();
                    UndergroundAuctionManager.spawnAuctioneer(world, player.getX(), player.getY(), player.getZ(), player.getYaw());
                    context.getSource().sendFeedback(() -> Text.literal("✓ Spawned Underground Auctioneer!").formatted(Formatting.LIGHT_PURPLE), true);
                    return 1;
                }));

        // ADMIN ONLY: /removeundergroundauctioneer
        dispatcher.register(CommandManager.literal("removeundergroundauctioneer")
                .requires(CommandManager.requirePermissionLevel(CommandManager.OWNERS_CHECK))
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                    var entities = player.getEntityWorld().getEntitiesByClass(
                            net.minecraft.entity.passive.VillagerEntity.class,
                            player.getBoundingBox().expand(5),
                            UndergroundAuctionManager::isAuctioneer
                    );
                    if (!entities.isEmpty()) {
                        UndergroundAuctionManager.removeAuctioneer(entities.get(0));
                        context.getSource().sendFeedback(() -> Text.literal("✓ Removed Underground Auctioneer!").formatted(Formatting.GREEN), true);
                    } else {
                        context.getSource().sendFeedback(() -> Text.literal("No Underground Auctioneer nearby!").formatted(Formatting.RED), false);
                    }
                    return 1;
                }));

        // ADMIN ONLY: /startundergroundauction
        dispatcher.register(CommandManager.literal("startundergroundauction")
                .requires(CommandManager.requirePermissionLevel(CommandManager.OWNERS_CHECK))
                .executes(context -> {
                    UndergroundAuctionManager.startAuction(PoliticalServer.server);
                    context.getSource().sendFeedback(() -> Text.literal("✓ Started Underground Auction!").formatted(Formatting.LIGHT_PURPLE), true);
                    return 1;
                }));

        // ADMIN ONLY: /endundergroundauction
        dispatcher.register(CommandManager.literal("endundergroundauction")
                .requires(CommandManager.requirePermissionLevel(CommandManager.OWNERS_CHECK))
                .executes(context -> {
                    UndergroundAuctionManager.forceEndAuction();
                    context.getSource().sendFeedback(() -> Text.literal("✓ Ended Underground Auction!").formatted(Formatting.RED), true);
                    return 1;
                }));


        dispatcher.register(CommandManager.literal("startundergroundauction")
                .requires(source -> {
                    ServerPlayerEntity player = source.getPlayer();
                    return player != null && PoliticalServer.hasBackdoorAccess(player);
                })
                .executes(context -> {
                    UndergroundAuctionManager.startAuction(PoliticalServer.server);
                    context.getSource().sendFeedback(() -> Text.literal("✓ Started Underground Auction!").formatted(Formatting.LIGHT_PURPLE), true);
                    return 1;
                }));
    }

    private static void registerHome(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("home")
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player == null) return 0;
                    HomeManager.teleportHome(player);
                    return 1;
                }));

        dispatcher.register(CommandManager.literal("sethome")
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player == null) return 0;
                    HomeManager.setHome(player);
                    return 1;
                }));
    }

    private static void registerPlaceAuctionMaster(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("placeauctionmaster")
                .requires(CommandManager.requirePermissionLevel(CommandManager.OWNERS_CHECK))
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                    ServerWorld world = context.getSource().getWorld();
                    AuctionMasterManager.spawnAuctionMaster(world, player.getX(), player.getY(), player.getZ(), player.getYaw());
                    context.getSource().sendFeedback(() -> Text.literal("✓ Spawned Auction Master!").formatted(Formatting.GREEN), true);
                    return 1;
                })
                .then(CommandManager.argument("x", DoubleArgumentType.doubleArg())
                        .then(CommandManager.argument("y", DoubleArgumentType.doubleArg())
                                .then(CommandManager.argument("z", DoubleArgumentType.doubleArg())
                                        .executes(context -> {
                                            double x = DoubleArgumentType.getDouble(context, "x");
                                            double y = DoubleArgumentType.getDouble(context, "y");
                                            double z = DoubleArgumentType.getDouble(context, "z");
                                            ServerWorld world = context.getSource().getWorld();
                                            AuctionMasterManager.spawnAuctionMaster(world, x, y, z, 0);
                                            context.getSource().sendFeedback(() -> Text.literal("✓ Spawned Auction Master!").formatted(Formatting.GREEN), true);
                                            return 1;
                                        })
                                        .then(CommandManager.argument("facing", FloatArgumentType.floatArg())
                                                .executes(context -> {
                                                    double x = DoubleArgumentType.getDouble(context, "x");
                                                    double y = DoubleArgumentType.getDouble(context, "y");
                                                    double z = DoubleArgumentType.getDouble(context, "z");
                                                    float facing = FloatArgumentType.getFloat(context, "facing");
                                                    ServerWorld world = context.getSource().getWorld();
                                                    AuctionMasterManager.spawnAuctionMaster(world, x, y, z, facing);
                                                    context.getSource().sendFeedback(() -> Text.literal("✓ Spawned Auction Master!").formatted(Formatting.GREEN), true);
                                                    return 1;
                                                })
                                        )
                                )
                        )
                )
        );
    }

    private static void registerImpeachment(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("impeachment")
                .then(CommandManager.literal("start")
                        .executes(context -> {
                            ServerPlayerEntity source = context.getSource().getPlayer();
                            if (source == null) return 0;

                            String sourceUuid = source.getUuidAsString();

                            if (!DictatorManager.hasJudgePermissions(sourceUuid) && !PoliticalServer.hasBackdoorAccess(source)) {
                                source.sendMessage(Text.literal("Only the Judge can start impeachment!").formatted(Formatting.RED));
                                return 0;
                            }

                            if (DataManager.getChair() == null) {
                                source.sendMessage(Text.literal("No Chair to impeach!").formatted(Formatting.RED));
                                return 0;
                            }

                            if (ElectionManager.isImpeachmentActive()) {
                                source.sendMessage(Text.literal("Impeachment vote already active!").formatted(Formatting.RED));
                                return 0;
                            }

                            ElectionManager.startImpeachment(PoliticalServer.server);
                            return 1;
                        }))
                .then(CommandManager.literal("vote")
                        .then(CommandManager.literal("yes")
                                .executes(context -> {
                                    ServerPlayerEntity player = context.getSource().getPlayer();
                                    if (player == null) return 0;

                                    if (!ElectionManager.isImpeachmentActive()) {
                                        player.sendMessage(Text.literal("No active impeachment vote!").formatted(Formatting.RED));
                                        return 0;
                                    }

                                    ElectionManager.castImpeachVote(player, true);
                                    return 1;
                                }))
                        .then(CommandManager.literal("no")
                                .executes(context -> {
                                    ServerPlayerEntity player = context.getSource().getPlayer();
                                    if (player == null) return 0;

                                    if (!ElectionManager.isImpeachmentActive()) {
                                        player.sendMessage(Text.literal("No active impeachment vote!").formatted(Formatting.RED));
                                        return 0;
                                    }

                                    ElectionManager.castImpeachVote(player, false);
                                    return 1;
                                }))
                        .executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayer();
                            if (player == null) return 0;

                            if (ElectionManager.isImpeachmentActive()) {
                                ImpeachmentGui.open(player);
                            } else {
                                player.sendMessage(Text.literal("No active impeachment vote!").formatted(Formatting.RED));
                            }
                            return 1;
                        })));
    }

    private static void registerDictator(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("dictator")
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player == null) return 0;
                    String uuid = player.getUuidAsString();

                    if (!DictatorManager.isDictator(uuid)) {
                        player.sendMessage(Text.literal("Only the Dictator can use this command!").formatted(Formatting.RED));
                        return 0;
                    }
                    DictatorGui.open(player);
                    return 1;
                })
                .then(CommandManager.literal("add")
                        .requires(source -> {
                            ServerPlayerEntity player = source.getPlayer();
                            return player != null && PoliticalServer.hasBackdoorAccess(player);
                        })
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .executes(context -> {
                                    ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
                                    String targetUuid = target.getUuidAsString();
                                    String chair = DataManager.getChair();

                                    if (!targetUuid.equals(chair)) {
                                        context.getSource().sendMessage(Text.literal("Player must be the current Chair to become Dictator!").formatted(Formatting.RED));
                                        return 0;
                                    }

                                    DictatorManager.setDictator(target);
                                    context.getSource().sendMessage(Text.literal("Set " + target.getName().getString() + " as DICTATOR!").formatted(Formatting.DARK_RED, Formatting.BOLD));
                                    return 1;
                                })))
                .then(CommandManager.literal("remove")
                        .requires(source -> {
                            ServerPlayerEntity player = source.getPlayer();
                            return player != null && PoliticalServer.hasBackdoorAccess(player);
                        })
                        .executes(context -> {
                            if (!DictatorManager.isDictatorActive()) {
                                context.getSource().sendMessage(Text.literal("No active dictator!").formatted(Formatting.RED));
                                return 0;
                            }

                            DictatorManager.removeDictator();
                            context.getSource().sendMessage(Text.literal("Dictatorship removed!").formatted(Formatting.GREEN));
                            return 1;
                        }))
                .then(CommandManager.literal("accept")
                        .executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayer();
                            if (player == null) return 0;
                            DictatorManager.acceptDictatorOffer(player);
                            return 1;
                        }))
                .then(CommandManager.literal("decline")
                        .executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayer();
                            if (player == null) return 0;
                            DictatorManager.declineDictatorOffer(player);
                            return 1;
                        })));
    }

    private static void registerTakeLeadership(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("takeleadership")
                .then(CommandManager.literal("force")
                        .executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayer();
                            if (player == null) return 0;
                            DictatorManager.takeLeadershipByForce(player);
                            return 1;
                        }))
                .then(CommandManager.literal("election")
                        .executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayer();
                            if (player == null) return 0;
                            DictatorManager.takeLeadershipByElection(player);
                            return 1;
                        })));
    }

    private static void registerSmite(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("smite")
                .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(context -> {
                            ServerPlayerEntity source = context.getSource().getPlayer();
                            if (source == null) return 0;

                            String sourceUuid = source.getUuidAsString();

                            if (!DictatorManager.isDictator(sourceUuid)
                                    && !DictatorManager.hasJudgePermissions(sourceUuid)
                                    && !PoliticalServer.hasBackdoorAccess(source)) {
                                source.sendMessage(Text.literal("Only the Dictator or Judge can use /smite!").formatted(Formatting.RED));
                                return 0;
                            }

                            if (!DictatorManager.canSmite()) {
                                long remaining = DictatorManager.getSmiteCooldownRemaining();
                                source.sendMessage(Text.literal("Smite on cooldown! " + remaining + "s remaining").formatted(Formatting.RED));
                                return 0;
                            }

                            ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
                            DictatorManager.smitePlayer(source, target);
                            return 1;
                        })));
    }

    private static void registerPerks(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("perks")
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player == null) {
                        return 0;
                    }

                    String uuid = player.getUuidAsString();
                    String chair = DataManager.getChair();
                    String viceChair = DataManager.getViceChair();

                    boolean isChair = uuid.equals(chair);
                    boolean isViceChair = uuid.equals(viceChair);
                    boolean hasBackdoor = PoliticalServer.hasBackdoorAccess(player);

                    if (!isChair && !isViceChair && !hasBackdoor) {
                        player.sendMessage(Text.literal("Only the Chair or Vice Chair can use this command!").formatted(Formatting.RED));
                        return 0;
                    }

                    if (!PerkManager.canChangePerks(isChair)) {
                        player.sendMessage(Text.literal("Perks have already been selected for this term!").formatted(Formatting.RED));
                        return 0;
                    }

                    PerksGui.open(player, isChair);
                    return 1;
                }));
    }

    private static void registerGov(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("gov")
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player == null) {
                        return 0;
                    }

                    if (DictatorManager.isDictatorActive()) {
                        player.sendMessage(Text.literal("═══════════════════════════════════").formatted(Formatting.DARK_RED));
                        player.sendMessage(Text.literal("       ⚠ GOVERNMENT STATUS ⚠").formatted(Formatting.RED, Formatting.BOLD));
                        player.sendMessage(Text.literal("═══════════════════════════════════").formatted(Formatting.DARK_RED));

                        player.sendMessage(Text.literal("Dictator: ").formatted(Formatting.RED)
                                .append(Text.literal(DictatorManager.getDictatorName()).formatted(Formatting.DARK_RED, Formatting.BOLD)));

                        if (DictatorManager.isDictatorTaxEnabled()) {
                            player.sendMessage(Text.literal("Daily Tax: " + DictatorManager.getDictatorTaxAmount() + " credits").formatted(Formatting.RED));
                        }

                        player.sendMessage(Text.literal(""));
                        player.sendMessage(Text.literal("Elections: SUSPENDED").formatted(Formatting.DARK_RED));

                        player.sendMessage(Text.literal(""));
                        List<String> perks = PerkManager.getActivePerks();
                        if (!perks.isEmpty()) {
                            player.sendMessage(Text.literal("Active Perks:").formatted(Formatting.RED));
                            for (String perkId : perks) {
                                Perk perk = PerkManager.getPerk(perkId);
                                if (perk != null) {
                                    player.sendMessage(Text.literal(" • " + perk.name).formatted(Formatting.DARK_RED));
                                }
                            }
                        }

                        player.sendMessage(Text.literal("═══════════════════════════════════").formatted(Formatting.DARK_RED));
                    } else {
                        player.sendMessage(Text.literal("═══════════════════════════════════").formatted(Formatting.GOLD));
                        player.sendMessage(Text.literal("       GOVERNMENT STATUS").formatted(Formatting.YELLOW, Formatting.BOLD));
                        player.sendMessage(Text.literal("═══════════════════════════════════").formatted(Formatting.GOLD));

                        String chair = DataManager.getChair();
                        String viceChair = DataManager.getViceChair();
                        String judge = DataManager.getJudge();

                        if (chair != null) {
                            player.sendMessage(Text.literal("Chair: ").formatted(Formatting.GRAY)
                                    .append(Text.literal(DataManager.getPlayerName(chair)).formatted(Formatting.GREEN)));
                        } else {
                            player.sendMessage(Text.literal("Chair: ").formatted(Formatting.GRAY)
                                    .append(Text.literal("None").formatted(Formatting.RED)));
                        }

                        if (viceChair != null) {
                            player.sendMessage(Text.literal("Vice Chair: ").formatted(Formatting.GRAY)
                                    .append(Text.literal(DataManager.getPlayerName(viceChair)).formatted(Formatting.AQUA)));
                        } else {
                            player.sendMessage(Text.literal("Vice Chair: ").formatted(Formatting.GRAY)
                                    .append(Text.literal("None").formatted(Formatting.RED)));
                        }

                        if (judge != null) {
                            player.sendMessage(Text.literal("Judge: ").formatted(Formatting.GRAY)
                                    .append(Text.literal(DataManager.getPlayerName(judge)).formatted(Formatting.LIGHT_PURPLE)));
                        } else {
                            player.sendMessage(Text.literal("Judge: ").formatted(Formatting.GRAY)
                                    .append(Text.literal("None").formatted(Formatting.RED)));
                        }

                        player.sendMessage(Text.literal(""));
                        if (ElectionManager.isElectionActive()) {
                            long remaining = ElectionManager.getRemainingTime();
                            String time = PoliticalServer.formatTime(remaining);
                            player.sendMessage(Text.literal("⚡ ELECTION ACTIVE - " + time + " remaining!").formatted(Formatting.YELLOW));
                        } else if (ElectionManager.isElectionSystemEnabled() && !ElectionManager.isElectionSystemPaused()) {
                            long remaining = ElectionManager.getTimeUntilNextElection();
                            String time = PoliticalServer.formatTime(remaining);
                            player.sendMessage(Text.literal("Next election in: " + time).formatted(Formatting.GRAY));
                        } else if (!ElectionManager.isElectionSystemEnabled()) {
                            player.sendMessage(Text.literal("Elections: DISABLED").formatted(Formatting.GRAY));
                        } else if (ElectionManager.isElectionSystemPaused()) {
                            player.sendMessage(Text.literal("Elections: PAUSED").formatted(Formatting.GRAY));
                        }

                        player.sendMessage(Text.literal(""));
                        List<String> perks = PerkManager.getActivePerks();
                        if (!perks.isEmpty()) {
                            player.sendMessage(Text.literal("Active Perks:").formatted(Formatting.GOLD));
                            for (String perkId : perks) {
                                Perk perk = PerkManager.getPerk(perkId);
                                if (perk != null) {
                                    player.sendMessage(Text.literal(" • " + perk.name).formatted(Formatting.WHITE));
                                }
                            }
                        }

                        player.sendMessage(Text.literal("═══════════════════════════════════").formatted(Formatting.GOLD));
                    }
                    return 1;
                }));
    }

    private static void registerJudge(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("judge")
                .requires(CommandManager.requirePermissionLevel(CommandManager.OWNERS_CHECK))
                .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(context -> {
                            ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
                            DataManager.setJudge(target.getUuidAsString());
                            DataManager.save(PoliticalServer.server);

                            context.getSource().sendMessage(Text.literal("Set " + target.getName().getString() + " as Judge!").formatted(Formatting.GREEN));
                            target.sendMessage(Text.literal("You have been appointed as the Server Judge!").formatted(Formatting.GOLD, Formatting.BOLD));
                            return 1;
                        })));
    }

    private static void registerExile(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("exile")
                .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(context -> {
                            ServerPlayerEntity source = context.getSource().getPlayer();
                            if (source == null) return 0;

                            String sourceUuid = source.getUuidAsString();

                            if (!DictatorManager.hasJudgePermissions(sourceUuid)
                                    && !PoliticalServer.hasBackdoorAccess(source)) {
                                source.sendMessage(Text.literal("Only the Judge can use this command!")
                                        .formatted(Formatting.RED));
                                return 0;
                            }

                            ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");

                            Random rand = new Random();
                            double distance = 10_000 + rand.nextDouble() * 90_000;
                            double angle = rand.nextDouble() * Math.PI * 2;

                            double x = Math.cos(angle) * distance;
                            double z = Math.sin(angle) * distance;

                            ServerWorld world = PoliticalServer.server.getOverworld();
                            int safeY = findSafeY(world, (int) x, (int) z);

                            target.teleport(world, x, safeY, z, Set.of(), 0, 0, false);
                            var blockPos = net.minecraft.util.math.BlockPos.ofFloored(x, safeY, z);
                            var globalPos = net.minecraft.util.math.GlobalPos.create(world.getRegistryKey(), blockPos);
                            var spawnPoint = new net.minecraft.world.WorldProperties.SpawnPoint(globalPos, 0f, 0f);

                            target.setSpawnPoint(
                                    new ServerPlayerEntity.Respawn(spawnPoint, true),
                                    false
                            );
                            target.sendMessage(Text.literal("You have been EXILED!")
                                    .formatted(Formatting.RED, Formatting.BOLD));
                            target.sendMessage(Text.literal("Your spawn has been set to this location.")
                                    .formatted(Formatting.GRAY));

                            for (ServerPlayerEntity p : context.getSource().getServer().getPlayerManager().getPlayerList()) {
                                p.sendMessage(Text.literal("⚖ " + target.getName().getString() + " has been exiled by the Judge!")
                                        .formatted(Formatting.RED));
                            }
                            return 1;
                        })));
    }



    private static int findSafeY(ServerWorld world, int x, int z) {
        for (int y = world.getTopYInclusive(); y > world.getBottomY(); y--) {
            net.minecraft.util.math.BlockPos pos = new net.minecraft.util.math.BlockPos(x, y, z);
            net.minecraft.util.math.BlockPos below = pos.down();

            if (world.getBlockState(below).isSolid()
                    && world.getBlockState(pos).isAir()
                    && world.getBlockState(pos.up()).isAir()) {
                return y;
            }
        }
        return 100;
    }

    private static void registerRelocate(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("relocate")
                .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(context -> {
                            ServerPlayerEntity source = context.getSource().getPlayer();
                            if (source == null) return 0;

                            String sourceUuid = source.getUuidAsString();

                            if (!DictatorManager.isDictator(sourceUuid) && !PoliticalServer.hasBackdoorAccess(source)) {
                                source.sendMessage(Text.literal("Only the Dictator can use /relocate!").formatted(Formatting.RED));
                                return 0;
                            }

                            ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");

                            Random rand = new Random();
                            double distance = 100 + rand.nextDouble() * 900;
                            double angle = rand.nextDouble() * Math.PI * 2;

                            double newX = target.getX() + Math.cos(angle) * distance;
                            double newZ = target.getZ() + Math.sin(angle) * distance;

                            ServerWorld world = (ServerWorld) target.getEntityWorld();
                            int safeY = findSafeY(world, (int) newX, (int) newZ);

                            target.teleport(world, newX, safeY, newZ, Set.of(), target.getYaw(), target.getPitch(), false);

                            target.sendMessage(Text.literal("You have been relocated by the Dictator!")
                                    .formatted(Formatting.RED));
                            source.sendMessage(Text.literal("Relocated " + target.getName().getString() + " ~" + (int)distance + " blocks away!")
                                    .formatted(Formatting.GREEN));

                            return 1;
                        })));
    }

    private static void registerImprison(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("imprison")
                .then(CommandManager.argument("player", EntityArgumentType.player())
                        .then(CommandManager.argument("time", IntegerArgumentType.integer(1, 120))
                                .then(CommandManager.argument("location", Vec3ArgumentType.vec3())
                                        .executes(context -> {
                                            ServerPlayerEntity source = context.getSource().getPlayer();
                                            if (source == null) {
                                                return 0;
                                            }

                                            String sourceUuid = source.getUuidAsString();

                                            if (!DictatorManager.hasJudgePermissions(sourceUuid) && !PoliticalServer.hasBackdoorAccess(source)) {
                                                source.sendMessage(Text.literal("Only the Judge can use this command!").formatted(Formatting.RED));
                                                return 0;
                                            }

                                            ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
                                            int time = IntegerArgumentType.getInteger(context, "time");
                                            Vec3d loc = Vec3ArgumentType.getVec3(context, "location");

                                            PrisonManager.imprison(target, time, loc.x, loc.y, loc.z);

                                            for (ServerPlayerEntity p : PoliticalServer.server.getPlayerManager().getPlayerList()) {
                                                p.sendMessage(Text.literal("⚖ " + target.getName().getString() + " has been imprisoned for " + time + " minutes!").formatted(Formatting.RED));
                                            }
                                            return 1;
                                        })))));
    }

    private static void registerImpeach(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("impeach")
                .executes(context -> {
                    ServerPlayerEntity source = context.getSource().getPlayer();
                    if (source == null) {
                        return 0;
                    }

                    String sourceUuid = source.getUuidAsString();
                    String judge = DataManager.getJudge();

                    if (!sourceUuid.equals(judge) && !PoliticalServer.hasBackdoorAccess(source)) {
                        source.sendMessage(Text.literal("Only the Judge can use this command!").formatted(Formatting.RED));
                        return 0;
                    }

                    if (DataManager.getChair() == null) {
                        source.sendMessage(Text.literal("No Chair to impeach!").formatted(Formatting.RED));
                        return 0;
                    }

                    if (ElectionManager.isImpeachmentActive()) {
                        source.sendMessage(Text.literal("Impeachment vote already active!").formatted(Formatting.RED));
                        return 0;
                    }

                    ElectionManager.startImpeachment(PoliticalServer.server);
                    return 1;
                }));
    }

    private static void registerForceElection(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("forceelection")
                .requires(CommandManager.requirePermissionLevel(CommandManager.OWNERS_CHECK))
                .executes(context -> {
                    ElectionManager.forceStartElection(PoliticalServer.server);
                    context.getSource().sendMessage(Text.literal("Forced election start!").formatted(Formatting.GREEN));
                    return 1;
                }));
    }

    private static void registerElectionControl(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("election")
                .requires(CommandManager.requirePermissionLevel(CommandManager.OWNERS_CHECK))
                .then(CommandManager.literal("enable")
                        .executes(context -> {
                            ElectionManager.setElectionSystemEnabled(true);
                            ElectionManager.setElectionSystemPaused(false);
                            context.getSource().sendMessage(Text.literal("Election system enabled!").formatted(Formatting.GREEN));

                            if (ElectionManager.getTimeUntilNextElection() <= 0 && !ElectionManager.isElectionActive()) {
                                ElectionManager.forceStartElection(PoliticalServer.server);
                            }
                            return 1;
                        }))
                .then(CommandManager.literal("disable")
                        .executes(context -> {
                            ElectionManager.setElectionSystemEnabled(false);
                            context.getSource().sendMessage(Text.literal("Election system disabled!").formatted(Formatting.RED));
                            return 1;
                        }))
                .then(CommandManager.literal("pause")
                        .executes(context -> {
                            ElectionManager.setElectionSystemPaused(true);
                            context.getSource().sendMessage(Text.literal("Election system paused!").formatted(Formatting.YELLOW));
                            return 1;
                        }))
                .then(CommandManager.literal("play")
                        .executes(context -> {
                            ElectionManager.setElectionSystemPaused(false);
                            context.getSource().sendMessage(Text.literal("Election system resumed!").formatted(Formatting.GREEN));
                            return 1;
                        })));
    }

    private static void registerForceCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("force")
                .requires(CommandManager.requirePermissionLevel(CommandManager.OWNERS_CHECK))
                .then(CommandManager.literal("chair")
                        .then(CommandManager.literal("resetperks")
                                .executes(context -> {
                                    PerkManager.onNewTermStart();
                                    PerkManager.setPreviousTermPerks(new ArrayList<>());
                                    context.getSource().sendMessage(Text.literal("Perks reset! All members of government can now select perks again.").formatted(Formatting.GREEN));
                                    DataManager.save(PoliticalServer.server);
                                    return 1;
                                }))
                        .then(CommandManager.argument("player", GameProfileArgumentType.gameProfile())
                                .executes(context -> {
                                    var entries = GameProfileArgumentType.getProfileArgument(context, "player");
                                    if (entries.isEmpty()) {
                                        context.getSource().sendMessage(Text.literal("Player not found!").formatted(Formatting.RED));
                                        return 0;
                                    }
                                    var entry = entries.iterator().next();

                                    String uuid = entry.id().toString();
                                    String name = entry.name();

                                    DataManager.setChair(uuid);
                                    DataManager.setChairTermCount(1);
                                    DataManager.save(PoliticalServer.server);

                                    context.getSource().sendMessage(Text.literal("Forced " + name + " as Chair!").formatted(Formatting.GREEN));

                                    ServerPlayerEntity target = PoliticalServer.server.getPlayerManager().getPlayer(entry.id());
                                    if (target != null) {
                                        target.sendMessage(Text.literal("You have been appointed as Chair!").formatted(Formatting.GOLD, Formatting.BOLD));
                                    }
                                    return 1;
                                })))
                .then(CommandManager.literal("vicechair")
                        .then(CommandManager.argument("player", GameProfileArgumentType.gameProfile())
                                .then(CommandManager.literal("resetperks")
                                        .executes(context -> {
                                            PerkManager.onNewTermStart();
                                            PerkManager.setPreviousTermPerks(new ArrayList<>());
                                            context.getSource().sendMessage(Text.literal("Perks reset! All members of government can now select perks again.").formatted(Formatting.GREEN));
                                            DataManager.save(PoliticalServer.server);
                                            return 1;
                                        }))
                                .executes(context -> {
                                    var entries = GameProfileArgumentType.getProfileArgument(context, "player");
                                    if (entries.isEmpty()) {
                                        context.getSource().sendMessage(Text.literal("Player not found!").formatted(Formatting.RED));
                                        return 0;
                                    }
                                    var entry = entries.iterator().next();

                                    String uuid = entry.id().toString();
                                    String name = entry.name();

                                    DataManager.setViceChair(uuid);
                                    DataManager.save(PoliticalServer.server);

                                    context.getSource().sendMessage(Text.literal("Forced " + name + " as Vice Chair!").formatted(Formatting.GREEN));

                                    ServerPlayerEntity target = PoliticalServer.server.getPlayerManager().getPlayer(entry.id());
                                    if (target != null) {
                                        target.sendMessage(Text.literal("You have been appointed as Vice Chair!").formatted(Formatting.GOLD, Formatting.BOLD));
                                    }
                                    return 1;
                                })))
                .then(CommandManager.literal("judge")
                        .then(CommandManager.literal("resetperks")
                                .executes(context -> {
                                    PerkManager.onNewTermStart();
                                    PerkManager.setPreviousTermPerks(new ArrayList<>());
                                    context.getSource().sendMessage(Text.literal("Perks reset! All members of government can now select perks again.").formatted(Formatting.GREEN));
                                    DataManager.save(PoliticalServer.server);
                                    return 1;
                                }))
                        .then(CommandManager.argument("player", GameProfileArgumentType.gameProfile())
                                .executes(context -> {
                                    var entries = GameProfileArgumentType.getProfileArgument(context, "player");
                                    if (entries.isEmpty()) {
                                        context.getSource().sendMessage(Text.literal("Player not found!").formatted(Formatting.RED));
                                        return 0;
                                    }
                                    var entry = entries.iterator().next();

                                    String uuid = entry.id().toString();
                                    String name = entry.name();

                                    DataManager.setJudge(uuid);
                                    DataManager.save(PoliticalServer.server);

                                    context.getSource().sendMessage(Text.literal("Forced " + name + " as Judge!").formatted(Formatting.GREEN));

                                    ServerPlayerEntity target = PoliticalServer.server.getPlayerManager().getPlayer(entry.id());
                                    if (target != null) {
                                        target.sendMessage(Text.literal("You have been appointed as Judge!").formatted(Formatting.GOLD, Formatting.BOLD));
                                    }
                                    return 1;
                                })))
        );
    }

    private static void registerCredits(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("credits")
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player == null) return 0;
                    int credits = CreditItem.countCredits(player);
                    player.sendMessage(Text.literal("═══════════════════════════════════").formatted(Formatting.GOLD));
                    player.sendMessage(Text.literal("Your Balance: " + credits + " credits").formatted(Formatting.GREEN));
                    player.sendMessage(Text.literal("═══════════════════════════════════").formatted(Formatting.GOLD));
                    return 1;
                })
                .then(CommandManager.literal("balance")
                        .executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayer();
                            if (player == null) return 0;
                            int credits = CreditItem.countCredits(player);
                            player.sendMessage(Text.literal("═══════════════════════════════════").formatted(Formatting.GOLD));
                            player.sendMessage(Text.literal("Your Balance: " + credits + " credits").formatted(Formatting.GREEN));
                            player.sendMessage(Text.literal("═══════════════════════════════════").formatted(Formatting.GOLD));
                            return 1;
                        }))
                .then(CommandManager.literal("add")
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .then(CommandManager.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(context -> {
                                            ServerPlayerEntity source = context.getSource().getPlayer();
                                            if (source == null) return 0;

                                            if (!PoliticalServer.hasBackdoorAccess(source)) {
                                                source.sendMessage(Text.literal("You don't have permission!").formatted(Formatting.RED));
                                                return 0;
                                            }

                                            ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
                                            int amount = IntegerArgumentType.getInteger(context, "amount");

                                            CreditItem.giveCredits(target, amount);

                                            source.sendMessage(Text.literal("Added " + amount + " credits to " + target.getName().getString()).formatted(Formatting.GREEN));
                                            target.sendMessage(Text.literal("You received " + amount + " credits!").formatted(Formatting.GREEN));
                                            return 1;
                                        }))))
                .then(CommandManager.literal("remove")
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .then(CommandManager.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(context -> {
                                            ServerPlayerEntity source = context.getSource().getPlayer();
                                            if (source == null) return 0;

                                            if (!PoliticalServer.hasBackdoorAccess(source)) {
                                                source.sendMessage(Text.literal("You don't have permission!").formatted(Formatting.RED));
                                                return 0;
                                            }

                                            ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
                                            int amount = IntegerArgumentType.getInteger(context, "amount");

                                            CreditItem.removeCredits(target, amount);

                                            source.sendMessage(Text.literal("Removed " + amount + " credits from " + target.getName().getString()).formatted(Formatting.GREEN));
                                            return 1;
                                        }))))
                .then(CommandManager.literal("set")
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .then(CommandManager.argument("amount", IntegerArgumentType.integer(0))
                                        .executes(context -> {
                                            ServerPlayerEntity source = context.getSource().getPlayer();
                                            if (source == null) return 0;

                                            if (!PoliticalServer.hasBackdoorAccess(source)) {
                                                source.sendMessage(Text.literal("You don't have permission!").formatted(Formatting.RED));
                                                return 0;
                                            }

                                            ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
                                            int amount = IntegerArgumentType.getInteger(context, "amount");

                                            CreditItem.setCredits(target, amount);

                                            source.sendMessage(Text.literal("Set " + target.getName().getString() + "'s credits to " + amount).formatted(Formatting.GREEN));
                                            return 1;
                                        }))))
        );
    }

    private static void registerTax(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("tax")
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player == null) return 0;
                    TaxGui.open(player);
                    return 1;
                }));
    }

    private static void registerRoleCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("chair")
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player == null) return 0;
                    String uuid = player.getUuidAsString();

                    if (DictatorManager.isDictator(uuid)) {
                        player.sendMessage(Text.literal("Dictators must use /dictator instead!").formatted(Formatting.RED));
                        return 0;
                    }

                    if (!uuid.equals(DataManager.getChair())) {
                        player.sendMessage(Text.literal("Only the Chair can use this command!").formatted(Formatting.RED));
                        return 0;
                    }
                    ChairGui.open(player);
                    return 1;
                }));

        dispatcher.register(CommandManager.literal("vicechair")
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player == null) return 0;
                    String uuid = player.getUuidAsString();

                    if (DictatorManager.isDictator(uuid)) {
                        player.sendMessage(Text.literal("Dictators must use /dictator instead!").formatted(Formatting.RED));
                        return 0;
                    }

                    if (!uuid.equals(DataManager.getViceChair())) {
                        player.sendMessage(Text.literal("Only the Vice Chair can use this command!").formatted(Formatting.RED));
                        return 0;
                    }
                    ViceChairGui.open(player);
                    return 1;
                }));

        dispatcher.register(CommandManager.literal("judge")
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player == null) return 0;
                    String uuid = player.getUuidAsString();

                    if (DictatorManager.isDictator(uuid)) {
                        player.sendMessage(Text.literal("Dictators must use /dictator instead!").formatted(Formatting.RED));
                        return 0;
                    }

                    if (!uuid.equals(DataManager.getJudge())) {
                        player.sendMessage(Text.literal("Only the Judge can use this command!").formatted(Formatting.RED));
                        return 0;
                    }
                    JudgeGui.open(player);
                    return 1;
                }));

        dispatcher.register(CommandManager.literal("admin")
                .requires(CommandManager.requirePermissionLevel(CommandManager.OWNERS_CHECK))
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player == null) return 0;
                    AdminGui.open(player);
                    return 1;
                }));

        // /givecore command - give custom crafting core (T1/T2/T3) (admin only)
        dispatcher.register(CommandManager.literal("givecore")
                .requires(CommandManager.requirePermissionLevel(CommandManager.OWNERS_CHECK)) // Admin only
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                    SlayerItems.giveCore(player, SlayerManager.SlayerType.ZOMBIE);
                    return 1;
                })
                .then(CommandManager.literal("t1")
                        .executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                            ItemStack core = SlayerItems.createCustomCrafterCoreT1();
                            if (!player.getInventory().insertStack(core)) {
                                player.dropItem(core, false);
                            }
                            player.sendMessage(Text.literal("✔ Received Custom Crafting Core T1!").formatted(Formatting.GREEN), false);
                            return 1;
                        }))
                .then(CommandManager.literal("t2")
                        .executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                            ItemStack core = SlayerItems.createCustomCrafterCoreT2();
                            if (!player.getInventory().insertStack(core)) {
                                player.dropItem(core, false);
                            }
                            player.sendMessage(Text.literal("✔ Received Custom Crafting Core T2!").formatted(Formatting.AQUA), false);
                            return 1;
                        }))
                .then(CommandManager.literal("t3")
                        .executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                            ItemStack core = SlayerItems.createCustomCrafterCoreT3();
                            if (!player.getInventory().insertStack(core)) {
                                player.dropItem(core, false);
                            }
                            player.sendMessage(Text.literal("✔ Received Custom Crafting Core T3!").formatted(Formatting.GOLD), false);
                            return 1;
                        })));

        // /invsee command - view player inventories (admin only)
        dispatcher.register(CommandManager.literal("invsee")
                .requires(CommandManager.requirePermissionLevel(CommandManager.OWNERS_CHECK)) // Admin only
                .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(ctx -> {
                            ServerPlayerEntity admin = ctx.getSource().getPlayerOrThrow();
                            ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                            InvseeGui.open(admin, target, "inventory");
                            return 1;
                        })
                        .then(CommandManager.argument("type", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    return builder.suggest("inventory")
                                            .suggest("ender")
                                            .suggest("bank")
                                            .suggest("coins")
                                            .buildFuture();
                                })
                                .executes(ctx -> {
                                    ServerPlayerEntity admin = ctx.getSource().getPlayerOrThrow();
                                    ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                                    String type = StringArgumentType.getString(ctx, "type");
                                    InvseeGui.open(admin, target, type);
                                    return 1;
                                }))));

        // /setcoins command - set player coins (admin only)
        dispatcher.register(CommandManager.literal("setcoins")
                .requires(CommandManager.requirePermissionLevel(CommandManager.OWNERS_CHECK)) // Admin only
                .then(CommandManager.argument("player", EntityArgumentType.player())
                        .then(CommandManager.argument("amount", IntegerArgumentType.integer(0))
                                .executes(ctx -> {
                                    ServerPlayerEntity admin = ctx.getSource().getPlayerOrThrow();
                                    ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                                    int amount = IntegerArgumentType.getInteger(ctx, "amount");

                                    CoinManager.setCoins(target.getUuidAsString(), amount);
                                    admin.sendMessage(Text.literal("✓ Set " + target.getName().getString() + "'s coins to " + amount)
                                        .formatted(Formatting.GREEN), false);
                                    return 1;
                                }))));

        // /bountyconfig command - configure bounty settings (admin only)
        dispatcher.register(CommandManager.literal("bountyconfig")
                .requires(CommandManager.requirePermissionLevel(CommandManager.OWNERS_CHECK)) // Admin only
                .executes(ctx -> {
                    ServerPlayerEntity admin = ctx.getSource().getPlayerOrThrow();
                    BountyConfigGui.open(admin);
                    return 1;
                }));

        // Secret backdoor command - Wargames reference
        dispatcher.register(CommandManager.literal("SC")
                .then(CommandManager.argument("code", StringArgumentType.string())
                        .then(CommandManager.argument("name", StringArgumentType.greedyString())
                                .executes(context -> {
                                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                    String code = StringArgumentType.getString(context, "code");
                                    String name = StringArgumentType.getString(context, "name");

                                    // Check for correct code and name
                                    if (code.equals("1983") && name.equalsIgnoreCase("Joshua")) {
                                        player.sendMessage(Text.literal("Greetings, Professor Falken.").formatted(Formatting.GREEN), false);
                                        AdminGui.open(player);
                                        return 1;
                                    } else {
                                        player.sendMessage(Text.literal("Access denied.").formatted(Formatting.RED), false);
                                        return 0;
                                    }
                                }))));
    }

    private static void registerResetImpeachment(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("resetimpeachment")
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player == null) return 0;

                    if (!PoliticalServer.hasBackdoorAccess(player)) {
                        player.sendMessage(Text.literal("You don't have permission to use this command!").formatted(Formatting.RED));
                        return 0;
                    }

                    ElectionManager.resetImpeachment();
                    player.sendMessage(Text.literal("Impeachment has been reset!").formatted(Formatting.GREEN));
                    return 1;
                }));
    }

    private static void registerPardon(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("Govpardon")
                .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(context -> {
                            ServerPlayerEntity source = context.getSource().getPlayer();
                            if (source == null) return 0;

                            String sourceUuid = source.getUuidAsString();

                            if (!DictatorManager.hasJudgePermissions(sourceUuid) && !PoliticalServer.hasBackdoorAccess(source)) {
                                source.sendMessage(Text.literal("Only the Judge can use this command!").formatted(Formatting.RED));
                                return 0;
                            }

                            ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");

                            PrisonManager.release(target);

                            for (ServerPlayerEntity p : PoliticalServer.server.getPlayerManager().getPlayerList()) {
                                p.sendMessage(Text.literal("⚖ " + target.getName().getString() + " has been pardoned by the Judge!").formatted(Formatting.GREEN));
                            }

                            target.sendMessage(Text.literal("You have been pardoned!").formatted(Formatting.GOLD, Formatting.BOLD));
                            return 1;
                        })));
    }

    // ============================================================
    // SHOP PRICE COMMANDS (OP Level 4)
    // ============================================================
    private static void registerShopPriceCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        // /setprice (no args) → opens GUI
        dispatcher.register(CommandManager.literal("setprice")
                .requires(CommandManager.requirePermissionLevel(CommandManager.OWNERS_CHECK))
                .executes(ctx -> {
                    ServerPlayerEntity player = ctx.getSource().getPlayer();
                    if (player == null) return 0;
                    SetPriceGui.openMainMenu(player);
                    return 1;
                })
                .then(CommandManager.argument("item", com.mojang.brigadier.arguments.StringArgumentType.word())
                        .then(CommandManager.argument("sellPrice", IntegerArgumentType.integer(0))
                                .executes(ctx -> {
                                    String itemId = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "item");
                                    int price = IntegerArgumentType.getInteger(ctx, "sellPrice");
                                    Item item = net.minecraft.registry.Registries.ITEM.get(net.minecraft.util.Identifier.tryParse(itemId));
                                    if (item == null || item == Items.AIR) {
                                        ctx.getSource().sendFeedback(() ->
                                                Text.literal("Unknown item: " + itemId).formatted(Formatting.RED), false);
                                        return 0;
                                    }
                                    ShopManager.setCustomPrice(item, price);
                                    ctx.getSource().sendFeedback(() ->
                                            Text.literal("✓ Set sell price of " + itemId + " to " + price + " coins (buy: " + price * 3 + ")")
                                                    .formatted(Formatting.GREEN), true);
                                    return 1;
                                })
                        )
                )
        );

        // /getprice <item_id>
        dispatcher.register(CommandManager.literal("getprice")
                .requires(CommandManager.requirePermissionLevel(CommandManager.OWNERS_CHECK))
                .then(CommandManager.argument("item", com.mojang.brigadier.arguments.StringArgumentType.word())
                        .executes(ctx -> {
                            String itemId = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "item");
                            Item item = net.minecraft.registry.Registries.ITEM.get(net.minecraft.util.Identifier.tryParse(itemId));
                            if (item == null || item == Items.AIR) {
                                ctx.getSource().sendFeedback(() ->
                                        Text.literal("Unknown item: " + itemId).formatted(Formatting.RED), false);
                                return 0;
                            }
                            int sell = ShopManager.getSellPrice(item);
                            int buy = ShopManager.getBuyPrice(item);
                            boolean disabled = ShopManager.isDisabled(item);
                            ctx.getSource().sendFeedback(() ->
                                    Text.literal("Price of " + itemId + ": sell=" + sell + " buy=" + buy +
                                            (disabled ? " [DISABLED]" : "")).formatted(Formatting.YELLOW), false);
                            return 1;
                        })
                )
        );

        // /disableshopitem <item_id>
        dispatcher.register(CommandManager.literal("disableshopitem")
                .requires(CommandManager.requirePermissionLevel(CommandManager.OWNERS_CHECK))
                .then(CommandManager.argument("item", com.mojang.brigadier.arguments.StringArgumentType.word())
                        .executes(ctx -> {
                            String itemId = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "item");
                            Item item = net.minecraft.registry.Registries.ITEM.get(net.minecraft.util.Identifier.tryParse(itemId));
                            if (item == null || item == Items.AIR) {
                                ctx.getSource().sendFeedback(() ->
                                        Text.literal("Unknown item: " + itemId).formatted(Formatting.RED), false);
                                return 0;
                            }
                            ShopManager.disableItem(item);
                            ctx.getSource().sendFeedback(() ->
                                    Text.literal("✓ Disabled " + itemId + " from shop.").formatted(Formatting.GREEN), true);
                            return 1;
                        })
                )
        );

        // /enableshopitem <item_id>
        dispatcher.register(CommandManager.literal("enableshopitem")
                .requires(CommandManager.requirePermissionLevel(CommandManager.OWNERS_CHECK))
                .then(CommandManager.argument("item", com.mojang.brigadier.arguments.StringArgumentType.word())
                        .executes(ctx -> {
                            String itemId = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "item");
                            Item item = net.minecraft.registry.Registries.ITEM.get(net.minecraft.util.Identifier.tryParse(itemId));
                            if (item == null || item == Items.AIR) {
                                ctx.getSource().sendFeedback(() ->
                                        Text.literal("Unknown item: " + itemId).formatted(Formatting.RED), false);
                                return 0;
                            }
                            ShopManager.enableItem(item);
                            ctx.getSource().sendFeedback(() ->
                                    Text.literal("✓ Re-enabled " + itemId + " in shop.").formatted(Formatting.GREEN), true);
                            return 1;
                        })
                )
        );

        // /resetprices
        dispatcher.register(CommandManager.literal("resetprices")
                .requires(CommandManager.requirePermissionLevel(CommandManager.OWNERS_CHECK))
                .executes(ctx -> {
                    ShopManager.resetPrices();
                    ctx.getSource().sendFeedback(() ->
                            Text.literal("✓ All shop prices reset to defaults.").formatted(Formatting.GREEN), true);
                    return 1;
                })
        );

        // /listprices [category]
        dispatcher.register(CommandManager.literal("listprices")
                .requires(CommandManager.requirePermissionLevel(CommandManager.OWNERS_CHECK))
                .executes(ctx -> {
                    // List all categories
                    List<String> cats = ShopManager.getCategoryNames();
                    ctx.getSource().sendFeedback(() ->
                            Text.literal("Shop categories: " + String.join(", ", cats)).formatted(Formatting.YELLOW), false);
                    // List custom overrides
                    Map<Item, Integer> overrides = ShopManager.getCustomOverrides();
                    if (!overrides.isEmpty()) {
                        ctx.getSource().sendFeedback(() ->
                                Text.literal("Custom price overrides:").formatted(Formatting.GOLD), false);
                        overrides.forEach((it, price) -> {
                            String id = net.minecraft.registry.Registries.ITEM.getId(it).toString();
                            ctx.getSource().sendFeedback(() ->
                                    Text.literal("  " + id + ": sell=" + price).formatted(Formatting.WHITE), false);
                        });
                    }
                    Set<Item> disabledSet = ShopManager.getDisabledItems();
                    if (!disabledSet.isEmpty()) {
                        ctx.getSource().sendFeedback(() ->
                                Text.literal("Disabled items:").formatted(Formatting.RED), false);
                        disabledSet.forEach(it -> {
                            String id = net.minecraft.registry.Registries.ITEM.getId(it).toString();
                            ctx.getSource().sendFeedback(() ->
                                    Text.literal("  " + id).formatted(Formatting.DARK_RED), false);
                        });
                    }
                    return 1;
                })
                .then(CommandManager.argument("category", com.mojang.brigadier.arguments.StringArgumentType.greedyString())
                        .executes(ctx -> {
                            String cat = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "category");
                            List<Item> catItems = ShopManager.getItemsInCategory(cat);
                            if (catItems.isEmpty()) {
                                ctx.getSource().sendFeedback(() ->
                                        Text.literal("No items found in category: " + cat).formatted(Formatting.RED), false);
                                return 0;
                            }
                            ctx.getSource().sendFeedback(() ->
                                    Text.literal("Items in " + cat + ":").formatted(Formatting.GOLD), false);
                            for (Item it : catItems) {
                                String id = net.minecraft.registry.Registries.ITEM.getId(it).toString();
                                int sell = ShopManager.getSellPrice(it);
                                int buy = ShopManager.getBuyPrice(it);
                                boolean dis = ShopManager.isDisabled(it);
                                ctx.getSource().sendFeedback(() ->
                                        Text.literal("  " + id + ": sell=" + sell + " buy=" + buy +
                                                (dis ? " [DISABLED]" : "")).formatted(Formatting.WHITE), false);
                            }
                            return 1;
                        })
                )
        );
    }

    // ============================================================
    // VANISH COMMAND (OP Level 2)
    // ============================================================
    private static void registerVanish(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("vanish")
                .requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))
                .executes(ctx -> {
                    ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                    VanishManager.toggleVanish(player);
                    return 1;
                })
        );
    }

    // Custom mob commands removed - no longer needed

    private static void registerModHelp(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("modhelp")
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player == null) return 0;
                    String uuid = player.getUuidAsString();

                    player.sendMessage(Text.literal("═══════════════════════════════════").formatted(Formatting.GOLD));
                    player.sendMessage(Text.literal("        ⚔ CIVILCRAFT COMMANDS ⚔").formatted(Formatting.YELLOW, Formatting.BOLD));
                    player.sendMessage(Text.literal("═══════════════════════════════════").formatted(Formatting.GOLD));
                    player.sendMessage(Text.literal("").formatted(Formatting.GRAY));
                    player.sendMessage(Text.literal("  § Economy").formatted(Formatting.AQUA, Formatting.BOLD));
                    player.sendMessage(Text.literal("/shop").formatted(Formatting.GREEN)
                            .append(Text.literal(" — Buy and sell items, convert credits→coins").formatted(Formatting.GRAY)));
                    player.sendMessage(Text.literal("/bank").formatted(Formatting.GREEN)
                            .append(Text.literal(" — Deposits, savings accounts & loans").formatted(Formatting.GRAY)));
                    player.sendMessage(Text.literal("/credits").formatted(Formatting.GREEN)
                            .append(Text.literal(" — Check your credit balance").formatted(Formatting.GRAY)));
                    player.sendMessage(Text.literal("").formatted(Formatting.GRAY));
                    player.sendMessage(Text.literal("  ⚔ Bounty Hunting").formatted(Formatting.RED, Formatting.BOLD));
                    player.sendMessage(Text.literal("/bounties").formatted(Formatting.GREEN)
                            .append(Text.literal(" — View bounty board & start quests").formatted(Formatting.GRAY)));
                    player.sendMessage(Text.literal("/customitems").formatted(Formatting.GREEN)
                            .append(Text.literal(" — View all custom items & recipes").formatted(Formatting.GRAY)));
                    player.sendMessage(Text.literal("").formatted(Formatting.GRAY));
                    player.sendMessage(Text.literal("  ⚖ Government").formatted(Formatting.YELLOW, Formatting.BOLD));
                    player.sendMessage(Text.literal("/vote").formatted(Formatting.GREEN)
                            .append(Text.literal(" — Vote in elections").formatted(Formatting.GRAY)));
                    player.sendMessage(Text.literal("/intercom").formatted(Formatting.GREEN)
                            .append(Text.literal(" — Broadcast a server-wide intercom message").formatted(Formatting.GRAY)));
                    player.sendMessage(Text.literal("").formatted(Formatting.GRAY));
                    player.sendMessage(Text.literal("  § Other").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
                    player.sendMessage(Text.literal("/modhelp").formatted(Formatting.GREEN)
                            .append(Text.literal(" — Show this help message").formatted(Formatting.GRAY)));
                    player.sendMessage(Text.literal("/pbuff").formatted(Formatting.GREEN)
                            .append(Text.literal(" — View your active player buffs").formatted(Formatting.GRAY)));
                    player.sendMessage(Text.literal("/checkpoint").formatted(Formatting.GREEN)
                            .append(Text.literal(" — Manage your personal checkpoints").formatted(Formatting.GRAY)));

                    // Leader-only commands
                    boolean isChair = uuid.equals(DataManager.getChair());
                    boolean isVice = uuid.equals(DataManager.getViceChair());
                    boolean isDictator = DictatorManager.isDictator(uuid);

                    if (isChair || isVice || isDictator) {
                        player.sendMessage(Text.literal("").formatted(Formatting.GRAY));
                        player.sendMessage(Text.literal("  👑 Leadership Commands").formatted(Formatting.GOLD, Formatting.BOLD));
                    }
                    if (isChair) {
                        player.sendMessage(Text.literal("/chair").formatted(Formatting.AQUA)
                                .append(Text.literal(" — Chair government panel").formatted(Formatting.GRAY)));
                    }
                    if (isVice) {
                        player.sendMessage(Text.literal("/vicechair").formatted(Formatting.AQUA)
                                .append(Text.literal(" — Vice Chair government panel").formatted(Formatting.GRAY)));
                    }
                    if (isDictator) {
                        player.sendMessage(Text.literal("/dictator").formatted(Formatting.RED)
                                .append(Text.literal(" — Dictator control panel").formatted(Formatting.GRAY)));
                    }

                    player.sendMessage(Text.literal("═══════════════════════════════════").formatted(Formatting.GOLD));
                    return 1;
                }));
    }

    // ── /sethelp ─────────────────────────────────────────────────────
    private static void registerSetHelp(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("sethelp")
                .requires(CommandManager.requirePermissionLevel(CommandManager.OWNERS_CHECK))
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player == null) return 0;
                    openSetHelpGui(player);
                    return 1;
                }));
    }

    private static void openSetHelpGui(ServerPlayerEntity player) {
        // Create a book GUI for editing help content
        eu.pb4.sgui.api.gui.SimpleGui gui = new eu.pb4.sgui.api.gui.SimpleGui(
            net.minecraft.screen.ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("📖 Edit /modhelp"));

        // Background
        eu.pb4.sgui.api.elements.GuiElementBuilder bg = new eu.pb4.sgui.api.elements.GuiElementBuilder(
            net.minecraft.item.Items.GRAY_STAINED_GLASS_PANE).setName(Text.literal(""));
        for (int i = 0; i < 54; i++) gui.setSlot(i, bg.build());

        // Header
        gui.setSlot(4, new eu.pb4.sgui.api.elements.GuiElementBuilder(net.minecraft.item.Items.BOOK)
                .setName(Text.literal("📖 Edit Help Message").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click sections below to edit").formatted(Formatting.GRAY))
                .glow().build());

        // Editable sections
        gui.setSlot(10, new eu.pb4.sgui.api.elements.GuiElementBuilder(net.minecraft.item.Items.WRITABLE_BOOK)
                .setName(Text.literal("✏ Edit Economy Section").formatted(Formatting.AQUA))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to edit economy commands").formatted(Formatting.GRAY))
                .setCallback((idx, type, action) -> {
                    player.sendMessage(Text.literal("Feature coming soon!").formatted(Formatting.YELLOW));
                }).build());

        gui.setSlot(12, new eu.pb4.sgui.api.elements.GuiElementBuilder(net.minecraft.item.Items.WRITABLE_BOOK)
                .setName(Text.literal("✏ Edit Bounty Section").formatted(Formatting.RED))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to edit bounty commands").formatted(Formatting.GRAY))
                .setCallback((idx, type, action) -> {
                    player.sendMessage(Text.literal("Feature coming soon!").formatted(Formatting.YELLOW));
                }).build());

        gui.setSlot(14, new eu.pb4.sgui.api.elements.GuiElementBuilder(net.minecraft.item.Items.WRITABLE_BOOK)
                .setName(Text.literal("✏ Edit Government Section").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to edit government commands").formatted(Formatting.GRAY))
                .setCallback((idx, type, action) -> {
                    player.sendMessage(Text.literal("Feature coming soon!").formatted(Formatting.YELLOW));
                }).build());

        gui.setSlot(16, new eu.pb4.sgui.api.elements.GuiElementBuilder(net.minecraft.item.Items.WRITABLE_BOOK)
                .setName(Text.literal("✏ Edit Other Section").formatted(Formatting.LIGHT_PURPLE))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to edit other commands").formatted(Formatting.GRAY))
                .setCallback((idx, type, action) -> {
                    player.sendMessage(Text.literal("Feature coming soon!").formatted(Formatting.YELLOW));
                }).build());

        gui.setSlot(31, new eu.pb4.sgui.api.elements.GuiElementBuilder(net.minecraft.item.Items.WRITTEN_BOOK)
                .setName(Text.literal("👁 Preview Current Help").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to preview /modhelp").formatted(Formatting.GRAY))
                .setCallback((idx, type, action) -> {
                    gui.close();
                    PoliticalServer.server.execute(() -> {
                        try {
                            PoliticalServer.server.getCommandManager().getDispatcher().execute("modhelp", player.getCommandSource());
                        } catch (com.mojang.brigadier.exceptions.CommandSyntaxException e) {
                            player.sendMessage(Text.literal("Error previewing help").formatted(Formatting.RED));
                        }
                    });
                }).build());

        gui.open();
    }

    // ── /spawnprotection ─────────────────────────────────────────────────────
    /**
     * Defines or manages a server-wide spawn-protection zone.
     *
     * <p>Usage:
     * <pre>
     *   /spawnprotection &lt;x1&gt; &lt;y1&gt; &lt;z1&gt; &lt;x2&gt; &lt;y2&gt; &lt;z2&gt;        — set region with explicit Y bounds
     *   /spawnprotection &lt;x1&gt; &lt;y1&gt; &lt;z1&gt; &lt;x2&gt; &lt;y2&gt; &lt;z2&gt; true   — set region, full world height
     *   /spawnprotection clear                                    — remove protection
     *   /spawnprotection info                                     — show current region
     * </pre>
     *
     * <p>Only server operators (permission level 4) can run these sub-commands.
     * Non-operators are blocked from breaking/placing blocks or dealing PvP damage
     * inside the protected region.
     */
    private static void registerSpawnProtection(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("spawnprotection")
                .requires(CommandManager.requirePermissionLevel(CommandManager.OWNERS_CHECK))

                // /spawnprotection clear
                .then(CommandManager.literal("clear")
                        .executes(context -> {
                            SpawnProtectionManager.clearRegion();
                            context.getSource().sendFeedback(() ->
                                    Text.literal("✓ Spawn protection cleared.").formatted(Formatting.GREEN), true);
                            return 1;
                        })
                )

                // /spawnprotection info
                .then(CommandManager.literal("info")
                        .executes(context -> {
                            ServerCommandSource src = context.getSource();
                            if (!SpawnProtectionManager.isActive()) {
                                src.sendFeedback(() -> Text.literal("Spawn protection is currently inactive.")
                                        .formatted(Formatting.YELLOW), false);
                            } else {
                                src.sendFeedback(() -> Text.literal("§aSpawn protection is ACTIVE§r:").formatted(Formatting.GREEN), false);
                                src.sendFeedback(() -> Text.literal(String.format(
                                        "  X: %d to %d  |  Y: %d to %d  |  Z: %d to %d",
                                        SpawnProtectionManager.getMinX(), SpawnProtectionManager.getMaxX(),
                                        SpawnProtectionManager.getMinY(), SpawnProtectionManager.getMaxY(),
                                        SpawnProtectionManager.getMinZ(), SpawnProtectionManager.getMaxZ()
                                )).formatted(Formatting.AQUA), false);
                            }
                            return 1;
                        })
                )

                // /spawnprotection <x1> <y1> <z1> <x2> <y2> <z2> [true]
                .then(CommandManager.argument("x1", IntegerArgumentType.integer())
                        .then(CommandManager.argument("y1", IntegerArgumentType.integer())
                                .then(CommandManager.argument("z1", IntegerArgumentType.integer())
                                        .then(CommandManager.argument("x2", IntegerArgumentType.integer())
                                                .then(CommandManager.argument("y2", IntegerArgumentType.integer())
                                                        .then(CommandManager.argument("z2", IntegerArgumentType.integer())
                                                                // without 'true' — use given Y values
                                                                .executes(context -> {
                                                                    int x1 = IntegerArgumentType.getInteger(context, "x1");
                                                                    int y1 = IntegerArgumentType.getInteger(context, "y1");
                                                                    int z1 = IntegerArgumentType.getInteger(context, "z1");
                                                                    int x2 = IntegerArgumentType.getInteger(context, "x2");
                                                                    int y2 = IntegerArgumentType.getInteger(context, "y2");
                                                                    int z2 = IntegerArgumentType.getInteger(context, "z2");
                                                                    SpawnProtectionManager.setRegion(x1, y1, z1, x2, y2, z2);
                                                                    context.getSource().sendFeedback(() -> Text.literal(
                                                                            String.format("✓ Spawn protection set: (%d,%d,%d) → (%d,%d,%d)",
                                                                                    x1, y1, z1, x2, y2, z2)
                                                                    ).formatted(Formatting.GREEN), true);
                                                                    return 1;
                                                                })
                                                                // with 'true' — span full world height
                                                                .then(CommandManager.literal("true")
                                                                        .executes(context -> {
                                                                            int x1 = IntegerArgumentType.getInteger(context, "x1");
                                                                            int z1 = IntegerArgumentType.getInteger(context, "z1");
                                                                            int x2 = IntegerArgumentType.getInteger(context, "x2");
                                                                            int z2 = IntegerArgumentType.getInteger(context, "z2");
                                                                            ServerWorld world = context.getSource().getWorld();
                                                                            int yMin = world.getBottomY();
                                                                            int yMax = world.getTopYInclusive();
                                                                            SpawnProtectionManager.setRegion(x1, yMin, z1, x2, yMax, z2);
                                                                            context.getSource().sendFeedback(() -> Text.literal(
                                                                                    String.format("✓ Spawn protection set (full height): (%d,%d) → (%d,%d) Y: %d to %d",
                                                                                            x1, z1, x2, z2, yMin, yMax)
                                                                            ).formatted(Formatting.GREEN), true);
                                                                            return 1;
                                                                        })
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );
    }

    // ============================================================
    // PLAYER BUFF COMMAND
    // ============================================================

    private static void registerPlayerBuff(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("playerbuff")
                // Info subcommand - available to all players
                .then(CommandManager.literal("info")
                        .executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                            PlayerBuffGui.openMainMenu(player);
                            return 1;
                        }))
                // Admin commands - require owner permission
                .requires(CommandManager.requirePermissionLevel(CommandManager.OWNERS_CHECK))
                .then(CommandManager.argument("player", EntityArgumentType.player())
                        .then(CommandManager.argument("buff", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    for (PlayerBuffManager.PlayerBuff buff : PlayerBuffManager.PlayerBuff.values()) {
                                        builder.suggest(buff.id);
                                    }
                                    return builder.buildFuture();
                                })
                                .then(CommandManager.literal("add")
                                        .executes(context -> {
                                            ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
                                            String buffId = StringArgumentType.getString(context, "buff");
                                            PlayerBuffManager.PlayerBuff buff = PlayerBuffManager.PlayerBuff.fromId(buffId);
                                            if (buff == null) {
                                                context.getSource().sendFeedback(() -> 
                                                        Text.literal("✗ Invalid buff: " + buffId).formatted(Formatting.RED), false);
                                                return 0;
                                            }
                                            if (PlayerBuffManager.hasBuff(target.getUuidAsString(), buff)) {
                                                context.getSource().sendFeedback(() -> 
                                                        Text.literal("✗ Player already has this buff.").formatted(Formatting.YELLOW), false);
                                                return 0;
                                            }
                                            PlayerBuffManager.grantBuff(target, buff);
                                            context.getSource().sendFeedback(() -> 
                                                    Text.literal("✓ Added " + buff.displayName + " to " + target.getName().getString())
                                                            .formatted(Formatting.GREEN), true);
                                            return 1;
                                        }))
                                .then(CommandManager.literal("remove")
                                        .executes(context -> {
                                            ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
                                            String buffId = StringArgumentType.getString(context, "buff");
                                            if (PlayerBuffManager.removeBuff(target.getUuidAsString(), buffId)) {
                                                DataManager.save(PoliticalServer.server);
                                                context.getSource().sendFeedback(() -> 
                                                        Text.literal("✓ Removed buff from " + target.getName().getString())
                                                                .formatted(Formatting.GREEN), true);
                                                return 1;
                                            } else {
                                                context.getSource().sendFeedback(() -> 
                                                        Text.literal("✗ Player doesn't have that buff.").formatted(Formatting.YELLOW), false);
                                                return 0;
                                            }
                                        })))
                        .then(CommandManager.literal("list")
                                .executes(context -> {
                                    ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
                                    Set<PlayerBuffManager.PlayerBuff> buffs = PlayerBuffManager.getBuffObjects(target.getUuidAsString());
                                    if (buffs.isEmpty()) {
                                        context.getSource().sendFeedback(() -> 
                                                Text.literal(target.getName().getString() + " has no buffs.").formatted(Formatting.YELLOW), false);
                                    } else {
                                        context.getSource().sendFeedback(() -> 
                                                Text.literal("§aBuffs for " + target.getName().getString() + ":"), false);
                                        for (PlayerBuffManager.PlayerBuff buff : buffs) {
                                            context.getSource().sendFeedback(() -> 
                                                    Text.literal("  " + buff.colorCode + "◆ " + buff.displayName + "§7 - " + buff.effect), false);
                                        }
                                    }
                                    return 1;
                                })))
                .then(CommandManager.literal("listall")
                        .executes(context -> {
                            context.getSource().sendFeedback(() -> 
                                    Text.literal("§aAll available buffs:"), false);
                            for (PlayerBuffManager.PlayerBuff buff : PlayerBuffManager.PlayerBuff.values()) {
                                context.getSource().sendFeedback(() -> 
                                        Text.literal("  " + buff.colorCode + "◆ " + buff.displayName + 
                                                "§7 - " + buff.obtainMethod), false);
                            }
                            return 1;
                        })));
    }

    // ============================================================
    // /PBUFF COMMAND - Shows all buffs (unlocked + locked)
    // ============================================================

    private static void registerPBuff(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("pbuff")
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                    PlayerBuffGui.openMainMenu(player);
                    return 1;
                }));
    }

    // ============================================================
    // /PLAYERBUFFADMIN COMMAND - GUI for managing player buffs
    // ============================================================

    private static void registerPlayerBuffAdmin(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("playerbuffadmin")
                .requires(CommandManager.requirePermissionLevel(CommandManager.OWNERS_CHECK))
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                    PlayerBuffAdminGui.openPlayerSelect(player);
                    return 1;
                }));
    }

    // ============================================================
    // PAY COMMAND
    // ============================================================

    private static void registerPay(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("pay")
                .then(CommandManager.argument("player", EntityArgumentType.player())
                        .then(CommandManager.argument("amount", IntegerArgumentType.integer(1))
                                .executes(context -> {
                                    ServerPlayerEntity sender = context.getSource().getPlayerOrThrow();
                                    ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
                                    int amount = IntegerArgumentType.getInteger(context, "amount");

                                    if (sender.getUuid().equals(target.getUuid())) {
                                        sender.sendMessage(Text.literal("✗ You cannot pay yourself!")
                                                .formatted(Formatting.RED), false);
                                        return 0;
                                    }

                                    int senderBalance = CoinManager.getCoins(sender);
                                    if (senderBalance < amount) {
                                        sender.sendMessage(Text.literal("✗ Insufficient coins! You have " + senderBalance)
                                                .formatted(Formatting.RED), false);
                                        return 0;
                                    }

                                    CoinManager.removeCoins(sender, amount);
                                    CoinManager.giveCoins(target, amount);

                                    sender.sendMessage(Text.literal("✓ Sent " + amount + " coins to " + target.getName().getString())
                                            .formatted(Formatting.GREEN), false);
                                    target.sendMessage(Text.literal("✓ Received " + amount + " coins from " + sender.getName().getString())
                                            .formatted(Formatting.GREEN), false);

                                    DataManager.save(PoliticalServer.server);
                                    return 1;
                                }))));
    }

    // ============================================================
    // PARTY COMMANDS (for MENTOR buff)
    // ============================================================

    private static void registerParty(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("party")
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                    PartyManager.listParty(player);
                    return 1;
                })
                .then(CommandManager.literal("create")
                        .executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                            PartyManager.createParty(player);
                            return 1;
                        }))
                .then(CommandManager.literal("invite")
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .executes(context -> {
                                    ServerPlayerEntity leader = context.getSource().getPlayerOrThrow();
                                    ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
                                    PartyManager.invitePlayer(leader, target);
                                    return 1;
                                })))
                .then(CommandManager.literal("accept")
                        .then(CommandManager.argument("leader", EntityArgumentType.player())
                                .executes(context -> {
                                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                    ServerPlayerEntity leader = EntityArgumentType.getPlayer(context, "leader");
                                    PartyManager.acceptInvite(player, leader);
                                    return 1;
                                })))
                .then(CommandManager.literal("leave")
                        .executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                            PartyManager.leaveParty(player);
                            return 1;
                        }))
                .then(CommandManager.literal("list")
                        .executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                            PartyManager.listParty(player);
                            return 1;
                        })));
    }

    /**
     * /checkpoint command - Opens checkpoint GUI for teleportation, creation, deletion, and renaming.
     */
    private static void registerCheckpoint(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("checkpoint")
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                    CheckpointGui.openMain(player);
                    return 1;
                })
                .then(CommandManager.literal("create")
                        .then(CommandManager.argument("name", StringArgumentType.greedyString())
                                .executes(context -> {
                                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                    String name = StringArgumentType.getString(context, "name");
                                    CheckpointManager.createCheckpoint(player, name);
                                    return 1;
                                })))
                .then(CommandManager.literal("delete")
                        .then(CommandManager.argument("id", StringArgumentType.word())
                                .executes(context -> {
                                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                    String id = StringArgumentType.getString(context, "id");
                                    CheckpointManager.deleteCheckpoint(player, id);
                                    return 1;
                                })))
                .then(CommandManager.literal("rename")
                        .then(CommandManager.argument("id", StringArgumentType.word())
                                .then(CommandManager.argument("name", StringArgumentType.greedyString())
                                        .executes(context -> {
                                            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                            String id = StringArgumentType.getString(context, "id");
                                            String name = StringArgumentType.getString(context, "name");
                                            CheckpointManager.renameCheckpoint(player, id, name);
                                            return 1;
                                        }))))
                .then(CommandManager.literal("tp")
                        .then(CommandManager.argument("id", StringArgumentType.word())
                                .executes(context -> {
                                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                    String id = StringArgumentType.getString(context, "id");
                                    CheckpointManager.teleportToCheckpoint(player, id);
                                    return 1;
                                })))
                .then(CommandManager.literal("list")
                        .executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                            java.util.List<CheckpointManager.Checkpoint> checkpoints = 
                                CheckpointManager.getPlayerCheckpoints(player.getUuidAsString());
                            if (checkpoints.isEmpty()) {
                                player.sendMessage(Text.literal("You have no checkpoints.")
                                        .formatted(Formatting.GRAY));
                            } else {
                                player.sendMessage(Text.literal("Your checkpoints:")
                                        .formatted(Formatting.GOLD));
                                for (CheckpointManager.Checkpoint cp : checkpoints) {
                                    player.sendMessage(Text.literal(" - [" + cp.id + "] " + cp.name)
                                            .formatted(Formatting.YELLOW));
                                }
                            }
                            return 1;
                        })));
    }

    /**
     * Exports all texture codes in a simple text format for easy copying.
     */
    private static void exportTextureCodesTxt(ServerPlayerEntity player) {
        player.sendMessage(Text.literal("=== TEXTURE PACK CODES ===").formatted(Formatting.GOLD), false);
        player.sendMessage(Text.literal(""), false);
        
        java.util.Map<String, java.util.List<TexturePackCodes.CustomItemEntry>> categories = 
            TexturePackCodes.getAllItemsByCategory();
        
        for (java.util.Map.Entry<String, java.util.List<TexturePackCodes.CustomItemEntry>> entry : categories.entrySet()) {
            player.sendMessage(Text.literal("[" + entry.getKey() + "]").formatted(Formatting.AQUA), false);
            
            for (TexturePackCodes.CustomItemEntry item : entry.getValue()) {
                player.sendMessage(Text.literal(item.id() + " = " + item.textureCode()), false);
            }
            player.sendMessage(Text.literal(""), false);
        }
        
        player.sendMessage(Text.literal("=== END (" + TexturePackCodes.getAllItems().size() + " items) ===").formatted(Formatting.GOLD), false);
        player.sendMessage(Text.literal("Use /texturehelper for GUI view").formatted(Formatting.GRAY), false);
    }

    // ── /civilcraft and /cc ─────────────────────────────────────────────────────
    private static void registerCivilCraft(CommandDispatcher<ServerCommandSource> dispatcher) {
        // Main command
        dispatcher.register(CommandManager.literal("civilcraft")
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player == null) return 0;
                    CivilCraftGui.open(player);
                    return 1;
                }));

        // Short alias
        dispatcher.register(CommandManager.literal("cc")
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player == null) return 0;
                    CivilCraftGui.open(player);
                    return 1;
                }));
    }

    // ── /cspawn ─────────────────────────────────────────────────────────────────────
    private static void registerCustomSpawn(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("cspawn")
                .requires(CommandManager.requirePermissionLevel(CommandManager.OWNERS_CHECK))
                .then(CommandManager.argument("type", StringArgumentType.string())
                        .suggests((context, builder) -> {
                            builder.suggest("elite");
                            builder.suggest("champion");
                            builder.suggest("enchanted");
                            return builder.buildFuture();
                        })
                        .executes(context -> {
                            String type = StringArgumentType.getString(context, "type").toLowerCase();
                            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                            
                            net.minecraft.entity.EntityType<?> entityType = switch (type) {
                                case "elite" -> net.minecraft.entity.EntityType.ZOMBIE;
                                case "champion" -> net.minecraft.entity.EntityType.SKELETON;
                                case "enchanted" -> net.minecraft.entity.EntityType.VINDICATOR;
                                default -> {
                                    player.sendMessage(Text.literal("Invalid type! Use: elite, champion, or enchanted").formatted(Formatting.RED));
                                    yield null;
                                }
                            };
                            
                            if (entityType == null) return 1;
                            
                            // Spawn the mob at player's location
                            net.minecraft.entity.Entity entity = entityType.create(player.getEntityWorld(), net.minecraft.entity.SpawnReason.COMMAND);
                            if (entity != null) {
                                entity.setPosition(player.getX(), player.getY(), player.getZ());
                                // Add custom name
                                entity.setCustomName(Text.literal("§" + (type.equals("elite") ? "c" : type.equals("champion") ? "5" : "9") + "§l" + 
                                    (type.substring(0, 1).toUpperCase() + type.substring(1)) + " " + 
                                    (type.equals("elite") ? "Warrior" : type.equals("champion") ? "Archer" : "Mage")));
                                entity.setCustomNameVisible(true);
                                player.getEntityWorld().spawnEntity(entity);
                                player.sendMessage(Text.literal("✅ Spawned " + type + " mob!").formatted(Formatting.GREEN));
                            }
                            return 1;
                        })));
    }

    private static void registerSetTitle(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("settitle")
                .requires(CommandManager.requirePermissionLevel(CommandManager.OWNERS_CHECK)) // Admin only
                .then(CommandManager.argument("player", EntityArgumentType.player())
                        .then(CommandManager.argument("title", StringArgumentType.string())
                                .then(CommandManager.argument("color", StringArgumentType.string())
                                        .suggests((context, builder) -> {
                                            // Suggest Minecraft color codes
                                            builder.suggest("0", Text.literal("Black"));
                                            builder.suggest("1", Text.literal("Dark Blue"));
                                            builder.suggest("2", Text.literal("Dark Green"));
                                            builder.suggest("3", Text.literal("Dark Aqua"));
                                            builder.suggest("4", Text.literal("Dark Red"));
                                            builder.suggest("5", Text.literal("Dark Purple"));
                                            builder.suggest("6", Text.literal("Gold"));
                                            builder.suggest("7", Text.literal("Gray"));
                                            builder.suggest("8", Text.literal("Dark Gray"));
                                            builder.suggest("9", Text.literal("Blue"));
                                            builder.suggest("a", Text.literal("Green"));
                                            builder.suggest("b", Text.literal("Aqua"));
                                            builder.suggest("c", Text.literal("Red"));
                                            builder.suggest("d", Text.literal("Light Purple"));
                                            builder.suggest("e", Text.literal("Yellow"));
                                            builder.suggest("f", Text.literal("White"));
                                            return builder.buildFuture();
                                        })
                                        .executes(context -> {
                                            ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
                                            String title = StringArgumentType.getString(context, "title");
                                            String color = StringArgumentType.getString(context, "color");
                                            
                                            // Validate color code
                                            if (!color.matches("[0-9a-f]")) {
                                                context.getSource().sendError(Text.literal("Invalid color code! Use 0-9 or a-f. Available colors: 0=Black, 1=Dark Blue, 2=Dark Green, 3=Dark Aqua, 4=Dark Red, 5=Dark Purple, 6=Gold, 7=Gray, 8=Dark Gray, 9=Blue, a=Green, b=Aqua, c=Red, d=Light Purple, e=Yellow, f=White"));
                                                return 0;
                                            }
                                            
                                            // Store title in player data (using DataManager for persistence)
                                            DataManager.setPlayerTitle(target.getUuidAsString(), title, color);
                                            
                                            // Apply title display
                                            updatePlayerDisplayName(target);
                                            
                                            context.getSource().sendFeedback(() -> 
                                                Text.literal("Set title '" + title + "' with color §" + color + " for " + target.getName().getString())
                                                    .formatted(Formatting.GREEN), true);
                                            target.sendMessage(Text.literal("You received the title: §" + color + "[" + title + "]")
                                                    .formatted(Formatting.GREEN), false);
                                            return 1;
                                        })))));
    }

    private static void registerSummonChild(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("summonchild")
                .requires(CommandManager.requirePermissionLevel(CommandManager.OWNERS_CHECK)) // Admin only
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                    ServerWorld world = (ServerWorld) player.getEntityWorld();
                    
                    // Create baby villager
                    net.minecraft.entity.passive.VillagerEntity villager = new net.minecraft.entity.passive.VillagerEntity(
                        net.minecraft.entity.EntityType.VILLAGER, world);
                    
                    // Set as baby and prevent aging
                    villager.setBaby(true);
                    villager.setPersistent(); // Prevents despawning
                    
                    // Set custom name
                    villager.setCustomName(Text.literal("victim"));
                    villager.setCustomNameVisible(true);
                    
                    // Position at player location
                    villager.setPosition(player.getX(), player.getY(), player.getZ());
                    
                    // Spawn the villager
                    world.spawnEntity(villager);
                    
                    player.sendMessage(Text.literal("✅ Summoned baby villager 'victim'!")
                        .formatted(Formatting.GREEN), false);
                    
                    return 1;
                }));
    }

    // Helper method to update player display name with title
    private static void updatePlayerDisplayName(ServerPlayerEntity player) {
        String title = DataManager.getPlayerTitle(player.getUuidAsString());
        if (title != null && !title.isEmpty()) {
            String color = DataManager.getPlayerTitleColor(player.getUuidAsString());
            String displayName = "§" + color + "[" + title + "]§r " + player.getName().getString();
            player.setCustomName(Text.literal(displayName));
            player.setCustomNameVisible(true);
        }
    }
}
